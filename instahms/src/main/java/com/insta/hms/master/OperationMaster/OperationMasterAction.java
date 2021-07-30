package com.insta.hms.master.OperationMaster;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.adminmasters.organization.RateMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.HsSfWorkbookUtils;
import com.bob.hms.otmasters.theamaster.TheatreMasterDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.InputValidator;
import com.insta.hms.common.PagedList;
import com.insta.hms.jobs.MasterChargesCronSchedulerDetailsRepository;
import com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupRepository;
import com.insta.hms.resourcescheduler.ResourceDAO;
import com.insta.hms.xls.exportimport.ChargesImportExporter;
import com.insta.hms.xls.exportimport.DetailsImportExporter;

import flexjson.JSONSerializer;




/*
 * Tables involved:
 *   operation_master: all operation definitions (op_id, name, etc.)
 *     -> OperationMasterDAO
 *   operation_org_details: item_code and applicable flag per operation-org_id
 *     -> OperationOrgDetailsDAO
 *   operation_charges: SAC, Surgeon, anaethetist charges on a operation_id,org_id,bed_type basis
 *     -> OperationChargeDAO
 *
 *  Forms:
 *   We use OperationUploadForm only for the CSV upload. All other submits use req.getParameter
 *   directly. For this reason, the action mapping for the upload alone is different.
 *   The reason is that ActionForm is the most convenient way to deal with multipart/form-data
 */

public class OperationMasterAction extends DispatchAction {

	static Logger logger = LoggerFactory.getLogger(OperationMasterAction.class);

	/*
	 * Lists all the operations as a filtered search result. (pages/master/OperationMasters/list.jsp)
	 */
	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, Exception {

		OperationMasterDAO dao = new OperationMasterDAO();
		OperationChargeDAO cdao = new OperationChargeDAO();

		Map requestParams = new HashMap();
		requestParams.putAll(req.getParameterMap());
		String orgId = req.getParameter("org_id");
		if ( (orgId == null) || orgId.isEmpty()) {
			String[] org_id = {"ORG0001"};
			requestParams.put("org_id", org_id);
			orgId = "ORG0001";
		}

		String chargeType = req.getParameter("_chargeType");
		if ( (chargeType == null) || chargeType.isEmpty()) {
			chargeType = "surg_asstance_charge";
		}

		PagedList list = dao.search(requestParams,
					ConversionUtils.getListingParameter(req.getParameterMap()));

		List<String> ids = new ArrayList<String>();
		for (Map obj : (List<Map>) list.getDtoList()) {
			ids.add((String) obj.get("op_id"));
		}

		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();
		List chargeList = cdao.getAllChargesForOrg(orgId, ids);
		Map chargesMap = ConversionUtils.listBeanToMapMapBean(chargeList, "op_id", "bed_type");

		JSONSerializer js = new JSONSerializer().exclude("class");

		req.setAttribute("pagedList", list);
		req.setAttribute("bedTypes", bedTypes);
		req.setAttribute("charges", chargesMap);
		req.setAttribute("namesJSON", js.serialize(dao.getAllNames()));
		req.setAttribute("org_id", orgId);
		req.setAttribute("chargeType", chargeType);
		MasterChargesCronSchedulerDetailsRepository masterChargesCronSchedulerDetailsRepository =
        ApplicationContextProvider.getBean(MasterChargesCronSchedulerDetailsRepository.class);
    Map<String, Object> cronJobKeys = new HashMap<String, Object>();
    cronJobKeys.put("entity", "OPERATION");
    ArrayList<String> status = new ArrayList<String>();
    status.add("F");
    status.add("P");
    cronJobKeys.put("status", status);
    List<BasicDynaBean> masterCronJobDetails =
        masterChargesCronSchedulerDetailsRepository.findByCriteria(cronJobKeys);
    req.setAttribute("masterCronJobDeatils",
        ConversionUtils.listBeanToListMap(masterCronJobDetails));

		return mapping.findForward("list");
	}

	/*
	 * add: returns the "add" screen for adding a new item
	 */
	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException {
		return addShow(mapping, form, req, res);
	}

	/*
	 * show: returns the "edit" screen for showing/editing an existing item.
	 */
	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException {
		return addShow(mapping, form, req, res);
	}

	/*
	 * Common method for add/show. Returns an add/show screen.
	 */
	private ActionForward addShow(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException {

		OperationMasterDAO dao = new OperationMasterDAO();
		OperationChargeDAO cdao = new OperationChargeDAO();
		TheatreMasterDAO otdao= new TheatreMasterDAO();

		String orgId = req.getParameter("org_id");
		if ( (orgId == null) || orgId.isEmpty() ) {
			orgId = "ORG0001";
		}

		int centerId = (Integer)req.getSession(false).getAttribute("centerId");
		String method = req.getParameter("_method");

		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();
		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("bedTypes", bedTypes);
    String id = req.getParameter("op_id");
		if (method.equals("show")) {

			BasicDynaBean bean = dao.getOperationDetails(id, orgId);
			List<BasicDynaBean> chargeList = cdao.getAllChargesForOrgOperation(orgId, id);
			String groupId = new ServiceSubGroupDAO().findByKey("service_sub_group_id", bean.get("service_sub_group_id")).get("service_group_id").toString();
			req.setAttribute("groupId", groupId);
			List<BasicDynaBean> activeInsurance = dao.getActiveInsuranceCategories(id);
			StringBuilder activeInsuranceCategories = new StringBuilder();
			for (BasicDynaBean activeInsuranceCategory : activeInsurance) {
			activeInsuranceCategories.append(activeInsuranceCategory.get("insurance_category_id"));
			activeInsuranceCategories.append(",");
			}
			req.setAttribute("insurance_categories", activeInsuranceCategories.toString());
			req.setAttribute("operation_default_duration", -1);
			req.setAttribute("bean", bean);
			req.setAttribute("charges", ConversionUtils.listBeanToMapMap(chargeList, "bed_type"));
			req.setAttribute("opLists",js.serialize(cdao.getOpNamesAndIds()));
			req.setAttribute("taxsubgroup", ConversionUtils.copyListDynaBeansToMap(dao.getOpItemSubGroupDetails(id)));
			req.setAttribute("method", "update");
		} else {
			req.setAttribute("method", "create");
			req.setAttribute("operation_default_duration", new ResourceDAO().getDefaultDurationSurgery());
		}
		req.setAttribute("centerId", centerId);
		List<BasicDynaBean> theaterList = otdao.getTheatersByCenter(centerId);
		List<BasicDynaBean> mappedResourceList = otdao.getMappedTheater(id, centerId);
		List<String> selectedTheaterList = new ArrayList<>();
	    for (BasicDynaBean theater : mappedResourceList) {
	    	selectedTheaterList.add((String) theater.get("theatre_id"));
	    }
	    req.setAttribute("theatres", theaterList);
	    req.setAttribute("selectedTheatreList", selectedTheaterList);
		req.setAttribute("serviceSubGroupsList", js.serialize(ServiceSubGroupDAO.getAllActiveServiceSubGroups()));
		req.setAttribute("operationNames", js.deepSerialize(dao.getOperationNames()));
		req.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(new GenericDAO("item_group_type").findAllByKey("item_group_type_id", "TAX")));
		req.setAttribute("itemGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(new GenericDAO("item_groups").findAllByKey("status", "A"))));
		//req.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(new GenericDAO("item_sub_groups").findAllByKey("status", "A"))));
		List <BasicDynaBean> itemSubGroupList = new TaxSubGroupRepository().getItemSubGroup(new java.sql.Date(new java.util.Date().getTime()));
		Iterator<BasicDynaBean> itemSubGroupListIterator = itemSubGroupList.iterator();
		List<BasicDynaBean> validateItemSubGrouList = new ArrayList<BasicDynaBean>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currentDateStr = sdf.format(new java.util.Date());
		while(itemSubGroupListIterator.hasNext()) {
			BasicDynaBean itenSubGroupbean = itemSubGroupListIterator.next();
			if(itenSubGroupbean.get("validity_end") != null){
				Date endDate = (Date)itenSubGroupbean.get("validity_end");
				
				try {
					if(sdf.parse(currentDateStr).getTime() <= endDate.getTime()) {
						validateItemSubGrouList.add(itenSubGroupbean);
					}
				} catch (ParseException e) {
					continue;
				}
			} else {
				validateItemSubGrouList.add(itenSubGroupbean);
			}
		}
		req.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(validateItemSubGrouList)));
		return mapping.findForward("addshow");
	}

	public ActionForward showCharges(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws SQLException {

		OperationMasterDAO dao = new OperationMasterDAO();
		OperationChargeDAO cdao = new OperationChargeDAO();

		String orgId = request.getParameter("org_id");
		if ( (orgId == null) || orgId.isEmpty() ) {
			orgId = "ORG0001";
		}
		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("bedTypes", bedTypes);
		String id = request.getParameter("op_id");
		BasicDynaBean bean = dao.getOperationDetails(id, orgId);
		List<BasicDynaBean> chargeList = cdao.getAllChargesForOrgOperation(orgId, id);

		List<BasicDynaBean> derivedRatePlanDetails = cdao.getDerivedRatePlanDetails(orgId, id);

		if(derivedRatePlanDetails.size()<0)
			request.setAttribute("derivedRatePlanDetails", js.serialize(Collections.EMPTY_LIST));
        else
        	request.setAttribute("derivedRatePlanDetails", js.serialize(ConversionUtils.copyListDynaBeansToMap(derivedRatePlanDetails)));

		request.setAttribute("method", "updateCharges");
		request.setAttribute("bean", bean);
		request.setAttribute("charges", ConversionUtils.listBeanToMapMap(chargeList, "bed_type"));
		request.setAttribute("opLists",js.serialize(cdao.getOpNamesAndIds()));
		MasterChargesCronSchedulerDetailsRepository masterChargesCronSchedulerDetailsRepository =
        ApplicationContextProvider.getBean(MasterChargesCronSchedulerDetailsRepository.class);
    Map<String, Object> searchMap = new HashMap<String, Object>();
    searchMap.put("entity", "OPERATION");
    searchMap.put("entity_id", id);
    ArrayList<String> status = new ArrayList<String>();
    status.add("F");
    status.add("P");
    searchMap.put("status", status);
    List<BasicDynaBean> masterJobData =
        masterChargesCronSchedulerDetailsRepository.findByCriteria(searchMap);
    request.setAttribute("masterJobCount", masterJobData.size());

		return mapping.findForward("showCharges");
	}


	/*
	 * create: POST method to create a new operation
	 */
	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, IOException, Exception {

		OperationMasterDAO sdao = new OperationMasterDAO();
		OperationChargeDAO cdao = new OperationChargeDAO();
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect=new ActionRedirect(mapping.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		HttpSession session = req.getSession();
    	String userName = (String)session.getAttribute("userid");
		ArrayList errors = new ArrayList();
		BasicDynaBean operation = sdao.getBean();
		operation.set("username", userName);

		ConversionUtils.copyToDynaBean(req.getParameterMap(), operation, errors, true);
		String operationId = sdao.getNextId();
		operation.set("op_id", operationId);

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
		}

		boolean success = false;
		Connection con = null;
		boolean allSuccess = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			// 1. Insert the operation
			success = sdao.insert(con, operation);
			if (!success) {
				flash.put("error", "Operation with the same name already exists");
				return redirect;
			}

			if(success) {
			success = saveOrUpdateInsuranceCategory(operationId, con, req);
			}
			
			if(success) {
			success=saveOrUpdateOperationTheatreMapping(operationId, con, req);	
			}

			// 1.save or update itemsubgroup(tax sub group) based on operation
			if(success) {
				String op_id = (String) operation.get("op_id");
				success = saveOrUpdateItemSubGroup(op_id,con,req);
			}
			
			success &= cdao.initItemCharges(con, operationId, userName);
			if (success) {
				cdao.operationChargeScheduleJob(operationId, userName);
			}
			if (!success) {
				flash.put("error", "Operation failed to insert charges.");
				return redirect;
			}

			allSuccess = true;
			flash.put("success", "Operation added successfully");
			redirect=new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("org_id", req.getParameter("org_id"));
			redirect.addParameter("op_id", operationId);

		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}

		return redirect;
	}

	/*
	 * update: POST method to update an existing operation and charges.
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, IOException {

		OperationMasterDAO sdao = new OperationMasterDAO();
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect=new ActionRedirect(mapping.findForward("showRedirect"));
		HttpSession session = req.getSession();
    	String userName = (String)session.getAttribute("userid");

		ArrayList errors = new ArrayList();
		BasicDynaBean operation = sdao.getBean();
		operation.set("username", userName);
		ConversionUtils.copyToDynaBean(req.getParameterMap(), operation, errors, true);
		String operationId = req.getParameter("op_id");

		redirect.addParameter("org_id", req.getParameter("org_id"));
		redirect.addParameter("op_id", req.getParameter("op_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");

		}

		Connection con = null;
		boolean allSuccess = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			// Update the operation
			boolean success = (1 == sdao.update(con, operation.getMap(), "op_id", operationId));

			if(success) {
			success = saveOrUpdateInsuranceCategory(operationId, con, req);
			}
			
			if(success) {
			success=saveOrUpdateOperationTheatreMapping(operationId, con, req);	
			}
			if(success) {
				String op_id = (String) operation.get("op_id");
				success = saveOrUpdateItemSubGroup(op_id,con,req);
			}
			
			if (!success) {
				flash.put("error", "Operation with the same name already exists");
				return redirect;
			} else {
				flash.put("success", "Operation updated successfully");
			}

			allSuccess = true;

		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}

		return redirect;
	}


	public ActionForward updateCharges(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws SQLException, IOException, Exception {

		OperationOrgDetailsDAO odao = new OperationOrgDetailsDAO();
		OperationChargeDAO cdao = new OperationChargeDAO();

		BasicDynaBean orgDetails = odao.getBean();
		String operationId = request.getParameter("op_id");
		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect=new ActionRedirect(mapping.findForward("showChargesRedirect"));

		ArrayList errors = new ArrayList();
		HttpSession session = request.getSession();
    	String userName = (String)session.getAttribute("userid");

		ConversionUtils.copyToDynaBean(request.getParameterMap(), orgDetails, errors);
		orgDetails.set("operation_id", operationId);
		orgDetails.set("applicable", true);
		redirect.addParameter("org_id", request.getParameter("org_id"));
		redirect.addParameter("op_id", request.getParameter("op_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		String[] beds = request.getParameterValues("bed_type");
		String opId = request.getParameter("op_id");
		String orgId = request.getParameter("org_id");

		String[] derivedRateplanIds = request.getParameterValues("ratePlanId");
        String[] ratePlanApplicable = request.getParameterValues("applicable");

		List<BasicDynaBean> chargeList = new ArrayList();
		for (int i=0; i<beds.length; i++) {
			BasicDynaBean charge = cdao.getBean();
			charge.set("username", userName);
			ConversionUtils.copyToDynaBean(request.getParameterMap(), charge, errors);
			ConversionUtils.copyIndexToDynaBean(request.getParameterMap(), i, charge, errors);
			chargeList.add(charge);
		}

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
			return redirect;
		}

		Connection con = null;
		boolean allSuccess = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			odao.updateWithNames(con, orgDetails.getMap(), new String[] {"operation_id", "org_id"});

			// Update the charge for all ORG0001 and all bed types
			for (BasicDynaBean c: chargeList) {
				cdao.updateWithNames(con, c.getMap(), new String[] {"op_id", "org_id", "bed_type"});
			}

			if(null != derivedRateplanIds && derivedRateplanIds.length > 0) {
				allSuccess = cdao.updateOrgForDerivedRatePlans(con, derivedRateplanIds, ratePlanApplicable, opId);

				String[] surg_asstance_charge = request.getParameterValues("surg_asstance_charge");
				String[] surgeon_charge = request.getParameterValues("surgeon_charge");
				String[] anesthetist_charge = request.getParameterValues("anesthetist_charge");

				String[] surg_asst_discount = request.getParameterValues("surg_asst_discount");
				String[] surg_discount = request.getParameterValues("surg_discount");
				String[] anest_discount = request.getParameterValues("anest_discount");

				Double[] surgAsstCharge = new Double[surg_asstance_charge.length];
				Double[] surgCharge = new Double[surgeon_charge.length];
				Double[] anesthCharge = new Double[anesthetist_charge.length];

				Double[] surgAsstDisc = new Double[surg_asst_discount.length];
				Double[] surgDisc = new Double[surg_discount.length];
				Double[] anestDisc = new Double[anest_discount.length];

				for(int i=0; i<surg_asstance_charge.length; i++) {
					surgAsstCharge[i] = new Double(surg_asstance_charge[i]);
					surgCharge[i] = new Double(surgeon_charge[i]);
					anesthCharge[i] = new Double(anesthetist_charge[i]);

					surgAsstDisc[i] = new Double(surg_asst_discount[i]);
					surgDisc[i] = new Double(surg_discount[i]);
					anestDisc[i] = new Double(anest_discount[i]);

				}

				allSuccess = cdao.updateChargesForDerivedRatePlans(con, orgId, derivedRateplanIds, beds,
						surgAsstCharge,surgCharge,anesthCharge,opId,surgAsstDisc,surgDisc,anestDisc, ratePlanApplicable);
			}

			allSuccess = true;

			RateMasterDao rdao = new RateMasterDao();
			List<BasicDynaBean> allDerivedRatePlanIds = rdao.getDerivedRatePlanIds(orgId);
			if(null != allDerivedRatePlanIds) {
				cdao.updateApplicableflagForDerivedRatePlans(con, allDerivedRatePlanIds, "operations", "operation_id",
						opId, "operation_org_details", orgId);
			}

			flash.put("success", "Operation updated successfully");

		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}

		return redirect;
	}

	/*
	 * Group Update: called from the main list screen, updates the charges of all/selected
	 * operations by a formula: +/- a certain amount or percentage,
	 */
	public ActionForward groupUpdate(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		FlashScope flash = FlashScope.getScope(req);

		ActionRedirect redirect = new ActionRedirect(req.getHeader("Referer").
				replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		String userName = (String) req.getSession().getAttribute("userid");

		String orgId = req.getParameter("org_id");
		String amtType = req.getParameter("amtType");
		String incType = req.getParameter("incType");
		String chargeType = InputValidator.getSafeSpecialString("Charge Type", req.getParameter("chargeType"), 50, false);
		//String chargeType = req.getParameter("chargeType");
		String updateTable = req.getParameter("updateTable");

		String allOperations = req.getParameter("allOperations");
		String allBedTypes = req.getParameter("allBedTypes");

		List<String> operations = null;
		if (!allOperations.equals("yes"))
			operations = ConversionUtils.getParamAsList(req.getParameterMap(), "selectOperation");

		List<String> bedTypes = null;
		if (allBedTypes == null)
			bedTypes = ConversionUtils.getParamAsList(req.getParameterMap(), "selectBedType");

		BigDecimal amount;
		BigDecimal round;
		try {
			amount = new BigDecimal(req.getParameter("amount"));
			round = new BigDecimal(req.getParameter("round"));
		} catch (NumberFormatException e) {
			flash.put("error", "Incorrectly formatted parameters");
			return redirect;
		}

		if (incType.equals("-"))
			amount = amount.negate();

		OperationChargeDAO dao = new OperationChargeDAO();

		Connection con = null;
		boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			dao.groupIncreaseCharges(con, orgId, chargeType, bedTypes,
					operations, amount, amtType.equals("%"), round, updateTable, (String)req.getSession(false).getAttribute("userid"));
			success = true;
			if(success )con.commit();

		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		if(success)
			dao.updateChargesForDerivedRatePlans(orgId,userName,"operations",false);

		if (success)
			flash.put("success", "Charges updated successfully");
		else
			flash.put("error", "Error updating charges");

		return redirect;
	}

	private static ChargesImportExporter importExporter;

	static {

		importExporter = new ChargesImportExporter("operation_master", "operation_org_details",
				"operation_charges", "department", "op_id", "dept_id", "dept_name",
				new String[] {"operation_name", "status"}, new String[] {"Operation Name", "Status"},
				new String[] {"applicable", "item_code"}, new String[] {"Applicable", "Code"},
				new String[] {"surg_asstance_charge", "surg_asst_discount", "surgeon_charge", "surg_discount",
				"anesthetist_charge", "anest_discount"},
				new String[] {"Surgical Assistance Charge", "Surgical Assistance Discount", "Surgeon Charge",
				"Surgeon Discount",	"Anesthetist Charge", "Anesthetist Discount"});

		importExporter.setOrgKey("operation_id");
		//TODO: should not be required to set this if the key is same as the item key.
		importExporter.setItemWhereFieldKeys(new String[] {"op_id"});
		importExporter.setOrgWhereFieldKeys(new String[] {"operation_id", "org_id"});	// TODO: required?
		importExporter.setChargeWhereFieldKeys(new String[] {"op_id", "org_id"});
		importExporter.setMandatoryFields(new String[] {"operation_name"});
		importExporter.setItemName("operation_name");
	}


	public ActionForward exportChargesToXls(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {

		String orgId = request.getParameter("org_id");
		String orgName = (String)OrgMasterDao.getOrgdetailsDynaBean(orgId).get("org_name");
		XSSFWorkbook workBook = new XSSFWorkbook();
		XSSFSheet opChargeSheet = workBook.createSheet("OPERATION CHARGES");
		importExporter.exportCharges(orgId, opChargeSheet, null, "A");

		response.setHeader("Content-type", "application/vnd.ms-excel");
		response.setHeader("Content-disposition","attachment; filename="+"\"OperationCharges_"+orgName+".xlsx\"");
		response.setHeader("Readonly", "true");
		java.io.OutputStream outputStream = response.getOutputStream();
		workBook.write(outputStream);
		outputStream.flush();
		outputStream.close();

		return null;
	}

	public ActionForward importChargesFromXLS(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException, SQLException,Exception {

		String orgId = request.getParameter("org_id");
		OperationUploadForm suForm = (OperationUploadForm) form;
		XSSFWorkbook workBook = new XSSFWorkbook(suForm.getXlsOperationFile().getInputStream());
		XSSFSheet sheet = workBook.getSheetAt(0);
		this.errors = new StringBuilder();
		String userName = (String)request.getSession().getAttribute("userid");

		FlashScope flash = FlashScope.getScope(request);
		String referer = request.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		Connection con = DataBaseUtil.getConnection();
		OperationChargeDAO cdao = new OperationChargeDAO();
		/*
		 * Keep a backup of the rates for safety: TODO: be able to checkpoint and revert
		 * to a previous version if required.
		 */
		cdao.backupCharges(con, orgId, (String) request.getSession().getAttribute("userid"));
		if (con != null)
			con.close();
		importExporter.setUseAuditLogHint(true);
		importExporter.importCharges(true, orgId, sheet,
				(String)request.getSession().getAttribute("userid"), this.errors);

		cdao.updateChargesForDerivedRatePlans(orgId,userName,"operations",true);

		if (this.errors.length() > 0)
			flash.put("error", this.errors);
		else
			flash.put("info", "File successfully uploaded");

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;

	}

	public ActionForward exportOperationDetailsToXls(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, java.io.IOException {

		 List<String> operationTabelColumns=Arrays.asList(new String[]
		                                    {"op_id","Operation Name","Dept Name","Status","Service Group"
				 ,"Service Sub Group","Conduction Applicable","Alias","Operation Duration"});
		 XSSFWorkbook workbook = new XSSFWorkbook();
		 XSSFSheet operationWorkSheet = workbook.createSheet("OPERATIONS");
		 List<BasicDynaBean> operationList=OperationChargeDAO.getOperationDetails();
		 Map<String, List> columnNamesMap = new HashMap<String, List>();
		 columnNamesMap.put("mainItems", operationTabelColumns);
		 HsSfWorkbookUtils.createPhysicalCellsWithValues(operationList,columnNamesMap, operationWorkSheet, true);
		 res.setHeader("Content-type", "application/vnd.ms-excel");
		 res.setHeader("Content-disposition","attachment; filename=OperationDefinationDetails.xlsx");
		 res.setHeader("Readonly", "true");
		 java.io.OutputStream os = res.getOutputStream();
		 workbook.write(os);
		 os.flush();
		 os.close();
		return null;
	}

	private static Map<String, String> aliasMap;
	static {
		aliasMap = new HashMap<String, String>();
			aliasMap.put("dept_name", "dept_id");
	}

	public static DetailsImportExporter detailsImporExp;

	static {
		detailsImporExp = new DetailsImportExporter("operation_master", "operation_org_details", "operation_charges");

	}

	public ActionForward importOperationDetailsFromXls(ActionMapping am, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {

		OperationUploadForm serviceForm = (OperationUploadForm) form;
		ByteArrayInputStream byteStream = new ByteArrayInputStream(serviceForm.getXlsOperationFile().getFileData());
		XSSFWorkbook workBook = new XSSFWorkbook(byteStream);
		XSSFSheet sheet = workBook.getSheetAt(0);

		this.errors = new StringBuilder();
		HashMap deptHashMap = OperationMasterDAO.getOperationDepartmentHashMap();
		Map<String, String> aliasMap = new HashMap<String, String>();

		aliasMap.put("operation name", "operation_name");
		aliasMap.put("code", "operation_code");
		aliasMap.put("status", "status");
		aliasMap.put("conduction applicable", "conduction_applicable");
		aliasMap.put("dept name", "dept_id");
		aliasMap.put("service sub group", "service_sub_group");
		aliasMap.put("service group", "service_group");
		aliasMap.put("alias", "operation_code");
		aliasMap.put("operation duration", "operation_duration");

		List<String> charges = Arrays.asList("surg_asstance_charge", "surgeon_charge", "anesthetist_charge");
		List<String> mandatoryList = Arrays.asList("operation_name", "status", "dept_id", "service_sub_group", "service_group","operation_duration");
		List<String> oddFields = Arrays.asList("operation_code");
		List<String> exemptFromNullCheck = Arrays.asList("op_id");

		detailsImporExp.setTableDbName("operation_name");
		detailsImporExp.setAliasUnmsToDBnmsMap(aliasMap);
		detailsImporExp.setBed("bed_type");
		detailsImporExp.setCharges(charges);
		detailsImporExp.setOddFields(oddFields);
		detailsImporExp.setId("op_id");
		detailsImporExp.setIdForOrgTab("operation_id");
		detailsImporExp.setOrgNameForChgTab("org_id");
		detailsImporExp.setMandatoryFields(mandatoryList);
		detailsImporExp.setOrgId("org_id");
		detailsImporExp.setType("OPERATIONID");
		detailsImporExp.setDeptName("dept_id");
		detailsImporExp.setDeptMap(deptHashMap);
		detailsImporExp.setSerSubGrpName("service_sub_group");
		detailsImporExp.setSerGrpName("service_group");
		detailsImporExp.setOrderType("Operation");
		detailsImporExp.setDbCodeName("operation_code");
		detailsImporExp.setIdForChgTab("op_id");
		detailsImporExp.setCodeAliasRequired(true);
		detailsImporExp.setDeptNotExist(false);
		detailsImporExp.setUsingHospIdPatterns(false);
		detailsImporExp.setUsingSequencePattern(false);
		detailsImporExp.setUsingUniqueNumber(true);
		detailsImporExp.setIsDateRequired(false);

		// This is required for tracking the user in the audit log.
		detailsImporExp.setColumnNameForUser("username");
		detailsImporExp.setIsUserNameRequired(true);
		detailsImporExp.setUseAuditLogHint(true);

		detailsImporExp.setExemptFromNullCheck(exemptFromNullCheck);

		String referer = request.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		FlashScope flash = FlashScope.getScope(request);

		detailsImporExp.importDetailsToXls(sheet, null, errors, (String)request.getSession(false).getAttribute("userid"));

		if (this.errors.length() > 0)
			flash.put("error", this.errors);
		else
			flash.put("info", "File successfully uploaded");
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	private boolean saveOrUpdateItemSubGroup(String op_id, Connection con,HttpServletRequest request)
			throws SQLException, IOException{
			Map params = request.getParameterMap();
			List errors = new ArrayList();
			
			boolean flag = true;
			String [] itemSubgroupId = request.getParameterValues("item_subgroup_id");
			String [] delete = request.getParameterValues("deleted");
			
			if (errors.isEmpty()) {
				if(itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")){
					GenericDAO itemsubgroupdao = new GenericDAO("operation_item_sub_groups");
					BasicDynaBean itemsubgroupbean = itemsubgroupdao.getBean();
					ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
					List records = itemsubgroupdao.findAllByKey("op_id", op_id);
					if (records.size() > 0)
						flag = itemsubgroupdao.delete(con, "op_id", op_id);
					
					for(int i=0; i<itemSubgroupId.length; i++){
						if(itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
							if (delete[i].equalsIgnoreCase("false")) {
								itemsubgroupbean.set("op_id", op_id);
								itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
								flag = itemsubgroupdao.insert(con, itemsubgroupbean);
							}
						}
					}
				}	
			}
			return flag;

		}

	/**
	  * Save or update Insurance Category.
	  *
	  * @param opId
	  *          the operation Id
	  * @param request
	  *          the request
	  * @return true, if successful
	  */
	private boolean saveOrUpdateInsuranceCategory(String opId,
	  Connection con, HttpServletRequest request) throws SQLException, IOException {
	boolean flag = true;
	String[] insuranceCategories = request.getParameterValues("insurance_category_id");
	if (insuranceCategories != null && insuranceCategories.length > 0
	    && !insuranceCategories[0].equals("")) {
	  GenericDAO insuranceCategoryDAO =
	      new GenericDAO("operation_insurance_category_mapping");
	  BasicDynaBean insuranceCategoryBean = insuranceCategoryDAO.getBean();
	  List<BasicDynaBean> records = insuranceCategoryDAO.findAllByKey("operation_id", opId);
	  if (records != null && records.size() > 0) {
	    flag = insuranceCategoryDAO.delete(con,"operation_id", opId);
	  }
	  for (String insuranceCategory :  insuranceCategories) {
	    insuranceCategoryBean.set("operation_id", opId);
	    insuranceCategoryBean.set("insurance_category_id", Integer.parseInt(insuranceCategory));
	    flag = insuranceCategoryDAO.insert(con,insuranceCategoryBean);
	  }
	}
	return flag;
	}

  /*
   * Save or update Theatre Category.
   * 
   * @param opId operation ID
   * 
   * @return true, if success
   */
  public boolean saveOrUpdateOperationTheatreMapping(String opId, Connection con,
      HttpServletRequest request) throws SQLException, IOException {
    boolean flag = true;
    String[] theatreIds = request.getParameterValues("theatre_id");
    OperationTheatreMappingDAO operationTheatreMappingDao = new OperationTheatreMappingDAO();
    List<BasicDynaBean> existingMappedRecords = operationTheatreMappingDao
        .findAllByKey("operation_id", opId);
    if (existingMappedRecords != null && existingMappedRecords.size() > 0) {
      flag = operationTheatreMappingDao.delete(con, "operation_id", opId);
    }
    if (theatreIds != null && theatreIds.length > 0 && !theatreIds[0].equals("")) {
      List<BasicDynaBean> beans = new ArrayList<BasicDynaBean>();
      for (String theatreId : theatreIds) {
        BasicDynaBean operationTheatreMappingBean = operationTheatreMappingDao.getBean();
        operationTheatreMappingBean.set("operation_id", opId);
        operationTheatreMappingBean.set("theatre_id", theatreId);
        beans.add(operationTheatreMappingBean);
      }
      flag = operationTheatreMappingDao.insertAll(con, beans);
    }
    return flag;

  }
	private StringBuilder errors;

}
