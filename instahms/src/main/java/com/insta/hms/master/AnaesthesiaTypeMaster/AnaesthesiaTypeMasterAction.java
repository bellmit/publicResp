package com.insta.hms.master.AnaesthesiaTypeMaster;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.InputValidator;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupRepository;
import com.insta.hms.xls.exportimport.ChargesImportExporter;
import com.insta.hms.xls.exportimport.DetailsImportExporter;

import flexjson.JSONSerializer;


public class AnaesthesiaTypeMasterAction  extends DispatchAction {
	static Logger logger = LoggerFactory.getLogger(AnaesthesiaTypeMasterAction.class);

	public ActionForward list(ActionMapping map, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException , IOException, ParseException {
		AnaesthesiaTypeMasterDAO dao = new AnaesthesiaTypeMasterDAO();
		AnaesthesiaTypeChargesDAO cdao = new AnaesthesiaTypeChargesDAO();

		Map requestParams = new HashMap();
		requestParams.putAll(request.getParameterMap());
		String orgId = request.getParameter("org_id");
		if ( (orgId == null) || orgId.isEmpty()) {
			String[] org_id = {"ORG0001"};
			requestParams.put("org_id", org_id);
			orgId = "ORG0001";
		}

		String chargeType = request.getParameter("_chargeType");
		if ( (chargeType == null) || chargeType.isEmpty()) {
			chargeType = "min_charge";
		}

		PagedList list = dao.search(requestParams, ConversionUtils.getListingParameter(requestParams));

		List<String> ids = new ArrayList<String>();
		for (Map obj : (List<Map>) list.getDtoList()) {
			ids.add((String) obj.get("anesthesia_type_id"));
		}

		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();
		List chargeList = cdao.getAllChargesForOrg(orgId, ids);
		Map chargesMap = ConversionUtils.listBeanToMapMapBean(chargeList, "anesthesia_type_id", "bed_type");

		JSONSerializer js = new JSONSerializer().exclude("class");

		request.setAttribute("pagedList", list);
		request.setAttribute("bedTypes", bedTypes);
		request.setAttribute("charges", chargesMap);
		request.setAttribute("namesJSON", js.serialize(dao.getAllNames()));
		request.setAttribute("org_id", orgId);
		request.setAttribute("chargeType", chargeType);

		return map.findForward("list");
	}

	public ActionForward  add(ActionMapping map, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException , IOException {
		return addShow(map, form, request, response);
	}

	public ActionForward show(ActionMapping map, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException , IOException {
		return addShow(map, form, request, response);
	}

	private ActionForward addShow(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException {

		AnaesthesiaTypeMasterDAO dao = new AnaesthesiaTypeMasterDAO();
		JSONSerializer json = new JSONSerializer().exclude("class");

		String orgId = req.getParameter("org_id");
		if ( (orgId == null) || orgId.isEmpty() ) {
			orgId = "ORG0001";
		}

		String method = req.getParameter("_method");
		JSONSerializer js = new JSONSerializer().exclude("class");

		if (method.equals("show")) {
			String id = req.getParameter("anesthesia_type_id");

			BasicDynaBean bean = dao.anaesthesiaTypeDetails(id, orgId);
			String groupId = new ServiceSubGroupDAO().findByKey("service_sub_group_id", bean.get("service_sub_group_id")).get("service_group_id").toString();
			req.setAttribute("groupId", groupId);
			List<BasicDynaBean> activeInsurance = dao.getActiveInsuranceCategories(id);
			StringBuilder activeInsuranceCategories = new StringBuilder();
			for (BasicDynaBean activeInsuranceCategory : activeInsurance) {
				activeInsuranceCategories.append(activeInsuranceCategory.get("insurance_category_id"));
				activeInsuranceCategories.append(",");
			}
			req.setAttribute("insurance_categories", activeInsuranceCategories.toString());
			req.setAttribute("bean", bean);
			req.setAttribute("anaesthesiaTypeLists", js.serialize(dao.getAnaesthesiaNamesAndIds()) );
			req.setAttribute("method", "update");
			req.setAttribute("taxsubgroup", ConversionUtils.copyListDynaBeansToMap(dao.getAnaesthesiaItemSubGroupDetails(id)));
		} else {
			req.setAttribute("method", "create");
		}
		req.setAttribute("serviceSubGroupsList", js.serialize(ServiceSubGroupDAO.getAllActiveServiceSubGroups()));
		req.setAttribute("namesList", json.serialize(dao.getNames()));
		
		req.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(new GenericDAO("item_group_type").findAllByKey("item_group_type_id","TAX")));
		req.setAttribute("itemGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(new GenericDAO("item_groups").findAllByKey("status","A"))));
		//req.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(new GenericDAO("item_sub_groups").findAllByKey("status","A"))));
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


		AnaesthesiaTypeMasterDAO dao = new AnaesthesiaTypeMasterDAO();
		AnaesthesiaTypeChargesDAO cdao = new AnaesthesiaTypeChargesDAO();
		JSONSerializer js = new JSONSerializer().exclude("class");

		String orgId = request.getParameter("org_id");
		if ( (orgId == null) || orgId.isEmpty() ) {
			orgId = "ORG0001";
		}

		String id = request.getParameter("anesthesia_type_id");

		BasicDynaBean bean = dao.anaesthesiaTypeDetails(id, orgId);
		List<BasicDynaBean> chargeList = cdao.getAllChargesForOrgAnaesthesiaType(orgId, id);
		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();

		List<BasicDynaBean> derivedRatePlanDetails = cdao.getDerivedRatePlanDetails(orgId, id);

		if(derivedRatePlanDetails.size()<0)
			request.setAttribute("derivedRatePlanDetails", js.serialize(Collections.EMPTY_LIST));
        else
        	request.setAttribute("derivedRatePlanDetails", js.serialize(ConversionUtils.copyListDynaBeansToMap(derivedRatePlanDetails)));

		request.setAttribute("bedTypes", bedTypes);
		request.setAttribute("bean", bean);
		request.setAttribute("charges", ConversionUtils.listBeanToMapMap(chargeList, "bed_type"));
		request.setAttribute("anaesthesiaTypeLists", js.serialize(dao.getAnaesthesiaNamesAndIds()) );
		request.setAttribute("method", "update");
		request.setAttribute("method", "updateCharges");

		return mapping.findForward("showCharges");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, IOException, Exception {

		AnaesthesiaTypeMasterDAO sdao = new AnaesthesiaTypeMasterDAO();
		AnaesthesiaTypeChargesDAO cdao = new AnaesthesiaTypeChargesDAO();

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		String orgId = "ORG0001";
		ArrayList errors = new ArrayList();
		BasicDynaBean anaesthesia = sdao.getBean();

		String userName = (String)req.getSession(false).getAttribute("userid");
		ConversionUtils.copyToDynaBean(req.getParameterMap(), anaesthesia, errors, true);
		String anaesthesiaId = sdao.getNextId();
		anaesthesia.set("anesthesia_type_id", anaesthesiaId);

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
			return redirect;
		}

		Connection con = null;
		boolean allSuccess = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			// 1. Insert the anaesthesia type
			boolean success = sdao.insert(con, anaesthesia);
			if (!success) {
				flash.put("error", "Anaesthesia Type with the same name already exists");
				return redirect;
			}

			success = cdao.initItemCharges(con, anaesthesiaId, userName);
			
			if(success) {
				success = saveOrUpdateItemSubGroup(anaesthesiaId,con,req);
			}

			if(success) {
				success = saveOrUpdateInsuranceCategory(anaesthesiaId,con,req);
			}

			if (!success) {
				flash.put("error", "Anaesthesia Type failed to insert charges.");
				return redirect;
			}

			allSuccess = true;
			flash.put("success", "Anaesthesia added successfully");
			redirect=new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("anesthesia_type_id", anaesthesiaId);
			redirect.addParameter("org_id",orgId);

		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}
		return redirect;
	}

	/*
	 * update: POST method to update an existing anaesthesia type and charges.
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, IOException {

		AnaesthesiaTypeMasterDAO sdao = new AnaesthesiaTypeMasterDAO();
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		String orgId = req.getParameter("org_id");
		String anaesthesiaId = req.getParameter("anesthesia_type_id");

		ArrayList errors = new ArrayList();
		BasicDynaBean anaesthesia = sdao.getBean();
		ConversionUtils.copyToDynaBean(req.getParameterMap(), anaesthesia, errors, true);

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
			return redirect;
		}

		Connection con = null;
		boolean allSuccess = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			// Update the anaesthesia type
			boolean success = (1 == sdao.update(con, anaesthesia.getMap(), "anesthesia_type_id", anaesthesiaId));
			if (!success) {
				flash.put("error", "Anaesthesia Type with the same name already exists");

			}
			if(success) {
				success = saveOrUpdateItemSubGroup(anaesthesiaId,con,req);
			}

			if(success) {
				success = saveOrUpdateInsuranceCategory(anaesthesiaId, con, req);
			}

			allSuccess = true;
			flash.put("success", "Anaesthesia Type updated successfully");
			redirect.addParameter("org_id", orgId);
			redirect.addParameter("anesthesia_type_id", req.getParameter("anesthesia_type_id"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

			return redirect;

		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}
	}

	public ActionForward updateCharges(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws SQLException, IOException,Exception {

		AnaesthesiaTypeChargesDAO cdao = new AnaesthesiaTypeChargesDAO();
		AnesthesiaTypeOrgDetailsDAO odao = new AnesthesiaTypeOrgDetailsDAO();
		FlashScope flash = FlashScope.getScope(request);

		String anaesthesiaId = request.getParameter("anesthesia_type_id");
		String orgId = request.getParameter("org_id");
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showChargesRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		String[] derivedRateplanIds = request.getParameterValues("ratePlanId");
        String[] ratePlanApplicable = request.getParameterValues("applicable");

		ArrayList errors = new ArrayList();
		BasicDynaBean orgDetails = odao.getBean();
		ConversionUtils.copyToDynaBean(request.getParameterMap(), orgDetails, errors);
		orgDetails.set("anesthesia_type_id", anaesthesiaId);
		orgDetails.set("applicable", true);

		String[] beds = request.getParameterValues("bed_type");
		List<BasicDynaBean> chargeList = new ArrayList();
		for (int i=0; i<beds.length; i++) {
			BasicDynaBean charge = cdao.getBean();
			ConversionUtils.copyToDynaBean(request.getParameterMap(), charge, errors);
			ConversionUtils.copyIndexToDynaBean(request.getParameterMap(), i, charge, errors);
			chargeList.add(charge);
		}

		redirect.addParameter("org_id", orgId);
		redirect.addParameter("anesthesia_type_id", request.getParameter("anesthesia_type_id"));

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
			return redirect;
		}

		Connection con = null;
		boolean allSuccess = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			odao.updateWithNames(con, orgDetails.getMap(), new String[] {"anesthesia_type_id", "org_id"});

			// Update the charge for all ORG0001 and all bed types
			for (BasicDynaBean c: chargeList) {
				cdao.updateWithNames(con, c.getMap(), new String[] {"anesthesia_type_id", "org_id", "bed_type"});
			}

			if(null != derivedRateplanIds && derivedRateplanIds.length > 0) {
				cdao.updateOrgForDerivedRatePlans(con, derivedRateplanIds, ratePlanApplicable, anaesthesiaId);

				String[] min_charge = request.getParameterValues("min_charge");
				String[] incr_charge = request.getParameterValues("incr_charge");
				String[] slab_1_charge = request.getParameterValues("slab_1_charge");
				String[] min_discount = request.getParameterValues("min_charge_discount");
				String[] incr_discount = request.getParameterValues("incr_charge_discount");
				String[] slab_discount = request.getParameterValues("slab_1_charge_discount");

				Double[] minChg = new Double[min_charge.length];
				Double[] incrChg = new Double[incr_charge.length];
				Double[] slabChg = new Double[slab_1_charge.length];
				Double[] minDisc = new Double[min_discount.length];
				Double[] incrDisc = new Double[incr_discount.length];
				Double[] slabDisc = new Double[slab_discount.length];

				for(int i=0; i<min_charge.length; i++) {
					minChg[i] = new Double(min_charge[i]);
					incrChg[i] = new Double(incr_charge[i]);
					slabChg[i] = new Double(slab_1_charge[i]);
					minDisc[i] = new Double(min_discount[i]);
					incrDisc[i] = new Double(incr_discount[i]);
					slabDisc[i] = new Double(slab_discount[i]);
				}

				cdao.updateChargesForDerivedRatePlans(con, orgId, derivedRateplanIds, beds,
						minChg, incrChg, slabChg, anaesthesiaId,minDisc,incrDisc,slabDisc,ratePlanApplicable);
			}

			allSuccess = true;

			RateMasterDao rdao = new RateMasterDao();
			List<BasicDynaBean> allDerivedRatePlanIds = rdao.getDerivedRatePlanIds(orgId);
			if(null != allDerivedRatePlanIds) {
				cdao.updateApplicableflagForDerivedRatePlans(con, allDerivedRatePlanIds, "anesthesia", "anesthesia_type_id",
						anaesthesiaId, "anesthesia_type_org_details", orgId);
			}


			flash.put("success", "Anaesthesia Type charges updated successfully");

			return redirect;

		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}

	}


	/*
	 * Group Update: called from the main list screen, updates the charges of all/selected
	 * Anesthesia type by a formula: +/- a certain amount or percentage,
	 */
	public ActionForward groupUpdate(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		FlashScope flash = FlashScope.getScope(req);
		AnaesthesiaTypeChargesDAO cdao = new AnaesthesiaTypeChargesDAO();

		ActionRedirect redirect = new ActionRedirect(req.getHeader("Referer").
				replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		String orgId = req.getParameter("org_id");
		String amtType = req.getParameter("amtType");
		String incType = req.getParameter("incType");
		//String chargeType = req.getParameter("chargeType");
		String chargeType = InputValidator.getSafeSpecialString("Charge Type", req.getParameter("chargeType"), 50, false);
		String updateTable = req.getParameter("updateTable");
		String userName = (String)req.getSession().getAttribute("userid");

		String allAnesthesiaTypes = req.getParameter("allAnaesthesiaTypes");
		String allBedTypes = req.getParameter("allBedTypes");

		List<String> anesthesiaTypes = null;
		if (!allAnesthesiaTypes.equals("yes"))
			anesthesiaTypes = ConversionUtils.getParamAsList(req.getParameterMap(), "selectAnesthesia");

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

		AnaesthesiaTypeChargesDAO dao = new AnaesthesiaTypeChargesDAO();

		Connection con = null;
		boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			dao.groupIncreaseCharges(con, orgId, chargeType, bedTypes,
					anesthesiaTypes, amount, amtType.equals("%"),round,updateTable);
			success = true;
		} finally {
			DataBaseUtil.commitClose(con, success);
		}
		if(success)
			cdao.updateChargesForDerivedRatePlans(orgId, userName, "anaesthesia",false);


		if (success)
			flash.put("success", "Charges updated successfully");
		else
			flash.put("error", "Error updating charges");

		return redirect;
	}


	private static ChargesImportExporter importExporter;

	static {
		importExporter = new ChargesImportExporter("anesthesia_type_master", "anesthesia_type_org_details",
				"anesthesia_type_charges", null, "anesthesia_type_id", null, null,
				new String[] {"anesthesia_type_name", "status"}, new String[] {"Anesthesia Name", "Status"},
				new String[] {"applicable", "item_code"}, new String[] {"Applicable", "Code"},
				new String[] {"min_charge", "min_charge_discount", "incr_charge", "incr_charge_discount",
				"slab_1_charge", "slab_1_charge_discount"},
				new String[] {"Min Charge", "Min Charge Discount", "Incr Charge", "Incr Charge Discount",
				"Slab1 Charge", "Slab1 Charge Discount"});

		importExporter.setItemWhereFieldKeys(new String[] {"anesthesia_type_id"});
		importExporter.setOrgWhereFieldKeys(new String[] {"anesthesia_type_id", "org_id"});
		importExporter.setChargeWhereFieldKeys(new String[] {"anesthesia_type_id", "org_id"});
		importExporter.setMandatoryFields(new String[] {"anesthesia_type_name"});
		importExporter.setItemName("anesthesia_type_name");
		importExporter.setUserColumnName(null);
	}

	public ActionForward exportChargesToXls(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {

		String orgId = request.getParameter("org_id");
		String orgName = (String)OrgMasterDao.getOrgdetailsDynaBean(orgId).get("org_name");
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet workSheet = workbook.createSheet("ANAESTHESIA TYPE CHARGES");
		importExporter.exportCharges(orgId, workSheet, null, "A");

		response.setHeader("Content-type", "application/vnd.ms-excel");
		response.setHeader("Content-disposition", "attachment; filename="+"\"AnaesthesiaCharges_"+ orgName +".xls\"");
		response.setHeader("Readonly", "true");
		java.io.OutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		outputStream.flush();
		outputStream.close();

		return null;
	}

	public ActionForward importChargesFromXLS(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException, SQLException, Exception {

		String orgId = request.getParameter("org_id");
		AnaesthesiaTypeChargesDAO cdao = new AnaesthesiaTypeChargesDAO();
		AnaesthesiaUploadForm suForm = (AnaesthesiaUploadForm) form;
		XSSFWorkbook workBook = new XSSFWorkbook(suForm.getXlsAnaesthesiaFile().getInputStream());
		XSSFSheet sheet = workBook.getSheetAt(0);
		this.errors = new StringBuilder();

		FlashScope flash = FlashScope.getScope(request);
		String referer = request.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		String userName = (String)request.getSession(false).getAttribute("userid");

		AnaesthesiaTypeChargesDAO.backupCharges(orgId, userName);
		importExporter.importCharges(true, orgId, sheet, userName, this.errors);
		cdao.updateChargesForDerivedRatePlans(orgId, userName, "anaesthesia",true);


		if (this.errors.length() > 0)
			flash.put("error", this.errors);
		else
			flash.put("info", "File successfully uploaded");

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;

	}


	public ActionForward exportAnaesthesiaTypeDetailsToXls(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, java.io.IOException {

		List<String> AnaesthesiaTypeTabelColumns=Arrays.asList(new String[]
		                           {"anesthesia_type_id", "anesthesia_type_name", "status",
					"service group", "service sub group", "min_duration","incr_duration", "slab1 threshold", "duration unit"});
		AnaesthesiaTypeMasterDAO dao = new AnaesthesiaTypeMasterDAO();
		 Map<String, List> columnNamesMap = new HashMap<String, List>();
		 columnNamesMap.put("mainItems", AnaesthesiaTypeTabelColumns);
		 XSSFWorkbook workbook = new XSSFWorkbook();
		 XSSFSheet anaesthesiaWorkSheet = workbook.createSheet("ANESTHESIA_TYPE_MASTER");
		 List<BasicDynaBean>anaestesiaTypeList=dao.getAnaesthesiaDetails();
		 HsSfWorkbookUtils.createPhysicalCellsWithValues(anaestesiaTypeList, columnNamesMap, anaesthesiaWorkSheet, true);
		 res.setHeader("Content-type", "application/vnd.ms-excel");
		 res.setHeader("Content-disposition","attachment; filename=anaesthesiaDefinationDetails.xls");
		 res.setHeader("Readonly", "true");
		 java.io.OutputStream os = res.getOutputStream();
		 workbook.write(os);
		 os.flush();
		 os.close();

		return null;
	}

	public static DetailsImportExporter detailsImporExp;

	static {
		detailsImporExp = new DetailsImportExporter("anesthesia_type_master", "anesthesia_type_org_details", "anesthesia_type_charges");

	}

	public ActionForward importAnaesthesiaTypeDetailsFromXls(ActionMapping am, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException, SQLException {

		AnaesthesiaUploadForm anaesthesiaForm = (AnaesthesiaUploadForm) form;
		ByteArrayInputStream byteStream = new ByteArrayInputStream(anaesthesiaForm.getXlsDetailsFile().getFileData());
		XSSFWorkbook workBook = new XSSFWorkbook(byteStream);
		XSSFSheet sheet = workBook.getSheetAt(0);

		this.errors = new StringBuilder();
		Connection con = null;
		boolean success = false;
		Map<String, String> aliasMap = new HashMap<String, String>();

		aliasMap.put("name", "anesthesia_type_name");
		aliasMap.put("duration unit", "duration_unit_minutes");
		aliasMap.put("min duration", "min_duration");
		aliasMap.put("slab1 threshold", "slab_1_threshold");
		aliasMap.put("status", "status");
		aliasMap.put("incr duration", "incr_duration");
		aliasMap.put("service sub group", "service_sub_group");
		aliasMap.put("service group", "service_group");

		List<String> charges = Arrays.asList("min_charge", "slab_1_charge", "incr_charge");
		List<String> mandatoryList = Arrays.asList("anesthesia_type_name", "status", "duration_unit_minutes",
				"service_sub_group", "service_group", "min_duration", "slab_1_threshold", "incr_duration");
		List<String> oddFields = Arrays.asList("");
		List<String> exemptFromNullCheck = Arrays.asList("anesthesia_type_id");

		detailsImporExp.setTableDbName("anesthesia_type_name");
		detailsImporExp.setAliasUnmsToDBnmsMap(aliasMap);
		detailsImporExp.setBed("bed_type");
		detailsImporExp.setCharges(charges);
		detailsImporExp.setOddFields(oddFields);
		detailsImporExp.setId("anesthesia_type_id");
		detailsImporExp.setIdForOrgTab("anesthesia_type_id");
		detailsImporExp.setOrgNameForChgTab("org_id");
		detailsImporExp.setMandatoryFields(mandatoryList);
		detailsImporExp.setOrgId("org_id");
		detailsImporExp.setSerSubGrpName("service_sub_group");
		detailsImporExp.setSerGrpName("service_group");
		detailsImporExp.setOrderType("Service");
		detailsImporExp.setDbCodeName("service_code");
		detailsImporExp.setIdForChgTab("anesthesia_type_id");
		detailsImporExp.setCodeAliasRequired(false);
		detailsImporExp.setDeptNotExist(true);
		detailsImporExp.setUsingHospIdPatterns(true);
		detailsImporExp.setUsingSequencePattern(false);
		detailsImporExp.setUsingUniqueNumber(false);
		detailsImporExp.setIsDateRequired(false);
		detailsImporExp.setIsUserNameRequired(false);
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


	private StringBuilder errors;
	
	private boolean saveOrUpdateItemSubGroup(String anesthesiatypeId, Connection con,HttpServletRequest request)
			throws SQLException, IOException{
			Map params = request.getParameterMap();
			List errors = new ArrayList();
			
			boolean flag = true;
			String [] itemSubgroupId = request.getParameterValues("item_subgroup_id");
			String [] delete = request.getParameterValues("deleted");
			
			if (errors.isEmpty()) {
				if(itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")){
					GenericDAO itemsubgroupdao = new GenericDAO("anesthesia_item_sub_groups");
					BasicDynaBean itemsubgroupbean = itemsubgroupdao.getBean();
					ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
					List records = itemsubgroupdao.findAllByKey("anesthesia_type_id", anesthesiatypeId);
					if (records.size() > 0)
						flag = itemsubgroupdao.delete(con, "anesthesia_type_id", anesthesiatypeId);
					
					for(int i=0; i<itemSubgroupId.length; i++){
						if(itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
							if (delete[i].equalsIgnoreCase("false")) {
								itemsubgroupbean.set("anesthesia_type_id", anesthesiatypeId);
								itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
								flag = itemsubgroupdao.insert(con, itemsubgroupbean);
							}
						}
					}
				}	
			}
			return flag;

		}

  private boolean saveOrUpdateInsuranceCategory(String anesthesiaTypeId,
      Connection con, HttpServletRequest request) throws SQLException, IOException {
      boolean flag = true;
      String[] insuranceCategories = request.getParameterValues("insurance_category_id");
      if (insuranceCategories != null && insuranceCategories.length > 0
        && !insuranceCategories[0].equals("")) {
        GenericDAO insuranceCategoryDAO =
          new GenericDAO("anesthesia_types_insurance_category_mapping");
        BasicDynaBean insuranceCategoryBean = insuranceCategoryDAO.getBean();
        List<BasicDynaBean> records = insuranceCategoryDAO.findAllByKey("anesthesia_type_id", anesthesiaTypeId);
        if (records != null && records.size() > 0) {
          flag = insuranceCategoryDAO.delete(con,"anesthesia_type_id", anesthesiaTypeId);
        }
        for (String insuranceCategory :  insuranceCategories) {
          insuranceCategoryBean.set("anesthesia_type_id", anesthesiaTypeId);
          insuranceCategoryBean.set("insurance_category_id", Integer.parseInt(insuranceCategory));
          flag = insuranceCategoryDAO.insert(con,insuranceCategoryBean);
        }
      }
      return flag;
  }

}