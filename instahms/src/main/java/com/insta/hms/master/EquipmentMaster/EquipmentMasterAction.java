package com.insta.hms.master.EquipmentMaster;

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
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.HsSfWorkbookUtils;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.InputValidator;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.DepartmentMaster.DepartmentMasterDAO;
import com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupRepository;
import com.insta.hms.xls.exportimport.ChargesImportExporter;
import com.insta.hms.xls.exportimport.DetailsImportExporter;

import flexjson.JSONSerializer;

/*
 * Tables involved:
 *   equipment_master: all equipment definitions (eq_id, name, etc.)
 *     -> EquipmentMasterDAO
 *   	equipment_charges: daily Charge, min Charge, incr Charge,tax charges on a equip_id,org_id,bed_type basis
 *     -> EquipmentChargeDAO
 *
 *  Forms:
 *   We use EquipmentUploadForm only for the CSV upload. All other submits use req.getParameter
 *   directly. For this reason, the action mapping for the upload alone is different.
 *   The reason is that ActionForm is the most convenient way to deal with multipart/form-data
 */

public class EquipmentMasterAction extends DispatchAction {

	static Logger logger = LoggerFactory.getLogger(EquipmentMasterAction.class);

	/*
	 * Lists all the Equipments as a filtered search result. (pages/master/EquipmentMasters/list.jsp)
	 */
	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, Exception {

		EquipmentMasterDAO dao = new EquipmentMasterDAO();
		EquipmentChargeDAO cdao = new EquipmentChargeDAO();

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
			chargeType = "daily_charge";
		}

		PagedList list = dao.search(requestParams, ConversionUtils.getListingParameter(requestParams));

		List<String> ids = new ArrayList<String>();
		for (Map obj : (List<Map>) list.getDtoList()) {
			ids.add((String) obj.get("eq_id"));
		}

		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();
		List chargeList = cdao.getAllChargesForOrg(orgId, ids);
		Map chargesMap = ConversionUtils.listBeanToMapMapBean(chargeList, "equip_id", "bed_type");

		JSONSerializer js = new JSONSerializer().exclude("class");

		req.setAttribute("pagedList", list);
		req.setAttribute("bedTypes", bedTypes);
		req.setAttribute("charges", chargesMap);
		req.setAttribute("namesJSON", js.serialize(dao.getAllNames()));
		req.setAttribute("org_id", orgId);
		req.setAttribute("chargeType", chargeType);

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

		EquipmentMasterDAO dao = new EquipmentMasterDAO();
		JSONSerializer json = new JSONSerializer().exclude("class");

		String orgId = req.getParameter("org_id");
		if ( (orgId == null) || orgId.isEmpty() ) {
			orgId = "ORG0001";
		}

		String method = req.getParameter("_method");

		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();
		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("bedTypes", bedTypes);

		if (method.equals("show")) {
			String id = req.getParameter("equip_id");

			BasicDynaBean bean = dao.getEquipmentDetails(id, orgId);

			String groupId = new ServiceSubGroupDAO().findByKey("service_sub_group_id", bean.get("service_sub_group_id")).get("service_group_id").toString();
			req.setAttribute("groupId", groupId);
			req.setAttribute("insurance_category_id", bean.get("insurance_category_id"));
			req.setAttribute("bean", bean);
			req.setAttribute("equipmentsLists", js.serialize(dao.getEquipmentsNamesAndIds()) );
			req.setAttribute("method", "update");
			req.setAttribute("taxsubgroup", ConversionUtils.copyListDynaBeansToMap(dao.getEquipItemSubGroupDetails(id)));
		} else {
			req.setAttribute("method", "create");
		}
		req.setAttribute("serviceSubGroupsList", js.serialize(ServiceSubGroupDAO.getAllActiveServiceSubGroups()));
		req.setAttribute("hl7Interfaces", new GenericDAO("hl7_lab_interfaces").listAll());

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

		EquipmentMasterDAO dao = new EquipmentMasterDAO();
		EquipmentChargeDAO cdao = new EquipmentChargeDAO();

		String orgId = request.getParameter("org_id");
		if ( (orgId == null) || orgId.isEmpty() ) {
			orgId = "ORG0001";
		}

		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("bedTypes", bedTypes);
		String id = request.getParameter("equip_id");

		BasicDynaBean bean = dao.getEquipmentDetails(id, orgId);
		List<BasicDynaBean> chargeList = cdao.getAllChargesForOrgEquipment(orgId, id);

		List<BasicDynaBean> derivedRatePlanDetails = cdao.getDerivedRatePlanDetails(orgId, id);

		if(derivedRatePlanDetails.size()<0)
			request.setAttribute("derivedRatePlanDetails", js.serialize(Collections.EMPTY_LIST));
        else
        	request.setAttribute("derivedRatePlanDetails", js.serialize(ConversionUtils.copyListDynaBeansToMap(derivedRatePlanDetails)));

		request.setAttribute("bean", bean);
		request.setAttribute("charges", ConversionUtils.listBeanToMapMap(chargeList, "bed_type"));
		request.setAttribute("equipmentsLists", js.serialize(dao.getEquipmentsNamesAndIds()) );
		request.setAttribute("method", "updateCharges");

		return mapping.findForward("showCharges");
	}

	/*
	 * create: POST method to create a new equipment
	 */
	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, IOException, Exception {

		EquipmentMasterDAO sdao = new EquipmentMasterDAO();
		EquipmentChargeDAO cdao = new EquipmentChargeDAO();

		FlashScope flash = FlashScope.getScope(req);
		HttpSession session = req.getSession();
		String userName = (String)session.getAttribute("userid");
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		String orgId = "ORG0001";

		ArrayList errors = new ArrayList();
		BasicDynaBean equipment = sdao.getBean();
		ConversionUtils.copyToDynaBean(req.getParameterMap(), equipment, errors, true);
		String equipmentId = sdao.getNextId();
		equipment.set("eq_id", equipmentId);

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
			return redirect;
		}

		Connection con = null;
		boolean allSuccess = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			// 1. Insert the equipment
			boolean success = sdao.insert(con, equipment);
			if (!success) {
				flash.put("error", "Equipment with the same name already exists");
				return redirect;
			}
			if(success) {
				String eq_id = (String) equipment.get("eq_id");
				success = saveOrUpdateItemSubGroup(eq_id,con,req);
			}

			success = cdao.initItemCharges(con, equipmentId, userName);
			if (!success) {
				flash.put("error", "Equipment failed to insert charges.");
				return redirect;
			}

			allSuccess = true;
			flash.put("success", "Equipment added successfully");
			redirect=new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			redirect.addParameter("equip_id", equipmentId);
			redirect.addParameter("org_id",orgId);

		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}
		return redirect;
	}

	/*
	 * update: POST method to update an existing equipment and charges.
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, IOException {

		EquipmentMasterDAO sdao = new EquipmentMasterDAO();
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		String orgId = req.getParameter("org_id");

		ArrayList errors = new ArrayList();
		BasicDynaBean equipment = sdao.getBean();
		ConversionUtils.copyToDynaBean(req.getParameterMap(), equipment, errors, true);
		String equipmentId = req.getParameter("equip_id");

		redirect.addParameter("org_id", orgId);
		redirect.addParameter("equip_id", req.getParameter("equip_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
			return redirect;
		}

		Connection con = null;
		boolean allSuccess = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			// Update the equipment
			boolean success = (1 == sdao.update(con, equipment.getMap(), "eq_id", equipmentId));
			if (!success) {
				flash.put("error", "Equipment with the same name already exists");
				return redirect;
			}
			if(success) {
				//String eq_id = (String) equipment.get("eq_id");
				success = saveOrUpdateItemSubGroup(equipmentId,con,req);
			}

			allSuccess = true;
			flash.put("success", "Equipment updated successfully");

			return redirect;
		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}
	}

	public ActionForward updateCharges(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, IOException, Exception {

		EquipmentChargeDAO cdao = new EquipmentChargeDAO();
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showChargesRedirect"));
		String orgId = req.getParameter("org_id");
		ArrayList errors = new ArrayList();

		GenericDAO odao = new GenericDAO("equip_org_details");
		BasicDynaBean orgDetails = odao.getBean();

		String equipmentId = req.getParameter("equip_id");
		String[] beds = req.getParameterValues("bed_type");

		String[] derivedRateplanIds = req.getParameterValues("ratePlanId");

		List<BasicDynaBean> chargeList = new ArrayList();
		for (int i=0; i<beds.length; i++) {
			BasicDynaBean charge = cdao.getBean();
			ConversionUtils.copyToDynaBean(req.getParameterMap(), charge, errors);
			ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, charge, errors);
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

			ConversionUtils.copyToDynaBean(req.getParameterMap(), orgDetails, errors);
			orgDetails.set("equip_id", equipmentId);
			orgDetails.set("org_id", orgId);
			orgDetails.set("applicable", true);
			odao.updateWithNames(con, orgDetails.getMap(), new String[] {"equip_id", "org_id"});

			// Update the charge for all ORG0001 and all bed types
			for (BasicDynaBean c: chargeList) {
				cdao.updateWithNames(con, c.getMap(), new String[] {"equip_id", "org_id", "bed_type"});
			}

			if(null != derivedRateplanIds && derivedRateplanIds.length > 0) {

				String[] daily_charge = req.getParameterValues("daily_charge");
				String[] min_charge = req.getParameterValues("min_charge");
				String[] incr_charge = req.getParameterValues("incr_charge");
				String[] slab_1_charge = req.getParameterValues("slab_1_charge");

				String[] daily_charge_discount = req.getParameterValues("daily_charge_discount");
				String[] min_charge_discount = req.getParameterValues("min_charge_discount");
				String[] incr_charge_discount = req.getParameterValues("incr_charge_discount");
				String[] slab_1_charge_discount = req.getParameterValues("slab_1_charge_discount");
				String[] tax = req.getParameterValues("tax");

				Double[] dailyChg = new Double[daily_charge.length];
				Double[] minChg = new Double[min_charge.length];
				Double[] incrChg = new Double[incr_charge.length];
				Double[] slabChg = new Double[slab_1_charge.length];
				Double[] dailyDisc = new Double[daily_charge_discount.length];
				Double[] minDisc = new Double[min_charge_discount.length];
				Double[] incrDisc = new Double[incr_charge_discount.length];
				Double[] slabDisc = new Double[slab_1_charge_discount.length];
				Double[] equipTax = new Double[tax.length];

				for(int i=0; i<daily_charge.length; i++) {
					dailyChg[i] = new Double(daily_charge[i]);
					minChg[i] = new Double(min_charge[i]);
					incrChg[i] = new Double(incr_charge[i]);
					slabChg[i] = new Double(slab_1_charge[i]);
					dailyDisc[i] = new Double(daily_charge_discount[i]);
					minDisc[i] = new Double(min_charge_discount[i]);
					incrDisc[i] = new Double(incr_charge_discount[i]);
					slabDisc[i] = new Double(slab_1_charge_discount[i]);
					equipTax[i] = new Double(tax[i]);
				}

				cdao.updateChargesForDerivedRatePlans(con, orgId, derivedRateplanIds, beds, dailyChg,
						minChg, incrChg, slabChg, equipmentId, dailyDisc,minDisc,incrDisc,slabDisc,equipTax);
			}

			allSuccess = true;
			flash.put("success", "Equipment Charges updated successfully");
			redirect.addParameter("org_id", orgId);
			redirect.addParameter("equip_id", req.getParameter("equip_id"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

			return redirect;

		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}
	}

	/*
	 * Group Update: called from the main list screen, updates the charges of all/selected
	 * Equipments by a formula: +/- a certain amount or percentage,
	 */
	public ActionForward groupUpdate(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		FlashScope flash = FlashScope.getScope(req);

		ActionRedirect redirect = new ActionRedirect(req.getHeader("Referer").
				replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		String userName = (String) req.getSession(false).getAttribute("userid");
		String orgId = req.getParameter("org_id");
		String amtType = req.getParameter("amtType");
		String incType = req.getParameter("incType");
		//String chargeType = req.getParameter("chargeType");
		String chargeType = InputValidator.getSafeSpecialString("Charge Type", req.getParameter("chargeType"), 50, false);
		String updateTable = req.getParameter("updateTable");

		String allEquipments = req.getParameter("allEquipment");
		String allBedTypes = req.getParameter("allBedTypes");

		List<String> equipments = null;
		if (!allEquipments.equals("yes"))
			equipments = ConversionUtils.getParamAsList(req.getParameterMap(), "selectEquipment");

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

		EquipmentChargeDAO dao = new EquipmentChargeDAO();

		Connection con = null;
		boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			dao.groupIncreaseCharges(con, orgId, chargeType, bedTypes,
					equipments, amount, amtType.equals("%"),round,updateTable);
			success = true;
		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		if(success) {
			dao.updateChargesForDerivedRatePlans(orgId, userName, "equipment",false);
		}

		if (success)
			flash.put("success", "Charges updated successfully");
		else
			flash.put("error", "Error updating charges");

		return redirect;
	}


	private static ChargesImportExporter importExporter;

	static {
		importExporter = new ChargesImportExporter("equipment_master", null,
				"equipement_charges", "department", "eq_id", "dept_id", "dept_name",
				new String[] {"equipment_name", "status"}, new String[] {"Equipment Name", "Status"},
				new String[]{}, new String[]{},
				new String[] {"daily_charge", "daily_charge_discount", "min_charge", "min_charge_discount",
				"incr_charge", "incr_charge_discount", "slab_1_charge", "slab_1_charge_discount", "tax"},
				new String[] {"Daily Charge", "Daily Charge Discount", "Min Charge", "Min Charge Discount",
				"Incr Charge", "Incr Charge Discount", "Slab1 Charge", "Slab1 Charge Discount", "Tax"});

		importExporter.setOrgKey("");
		importExporter.setChargeKey("equip_id");
		importExporter.setItemWhereFieldKeys(new String[] {"eq_id"});
		importExporter.setOrgWhereFieldKeys(new String[] {""});
		importExporter.setChargeWhereFieldKeys(new String[] {"equip_id", "org_id"});
		importExporter.setMandatoryFields(new String[] {"equipment_name"});
		importExporter.setItemName("equipment_name");
		importExporter.setUserColumnName(null);
	}

	public ActionForward exportChargesToXls(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {

		String orgId = request.getParameter("org_id");
		BasicDynaBean orgBean = OrgMasterDao.getOrgdetailsDynaBean(orgId);
		String orgName = (String) orgBean.get("org_name");

		XSSFWorkbook workBook = new XSSFWorkbook();
		XSSFSheet opChargeSheet = workBook.createSheet("EQUIPMENT CHARGES");
		importExporter.exportCharges(orgId, opChargeSheet, null, "A");

		response.setHeader("Content-type", "application/vnd.ms-excel");
		response.setHeader("Content-disposition","attachment; filename="+"\"EquipmentCharges_"+ orgName +".xls\"");
		response.setHeader("Readonly", "true");
		java.io.OutputStream outputStream = response.getOutputStream();
		workBook.write(outputStream);
		outputStream.flush();
		outputStream.close();

		return null;
	}

	public ActionForward importChargesFromXLS(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException, SQLException, Exception {


		String orgId = request.getParameter("org_id");
		EquipmentUploadForm suForm = (EquipmentUploadForm) form;
		XSSFWorkbook workBook = new XSSFWorkbook(suForm.getXlsEquipmentFile().getInputStream());
		XSSFSheet sheet = workBook.getSheetAt(0);
		this.errors = new StringBuilder();

		FlashScope flash = FlashScope.getScope(request);
		String referer = request.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		Connection con = null;
		String userName = (String)request.getSession(false).getAttribute("userid");
		EquipmentChargeDAO cdao = new EquipmentChargeDAO();

		try {
			con = DataBaseUtil.getConnection();

			/*
			 * Keep a backup of the rates for safety: TODO: be able to checkpoint and revert
			 * to a previous version if required.
			 */
			cdao.backupCharges(con, orgId, userName);
			importExporter.importCharges(true, orgId, sheet, userName, this.errors);

		} finally {
			if (con != null)
				con.close();
		}

		cdao.updateChargesForDerivedRatePlans(orgId, userName, "equipment",true);

		if (this.errors.length() > 0)
			flash.put("error", this.errors);
		else
			flash.put("info", "File successfully uploaded");

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;

	}


	public ActionForward exportEquipmentDetailsToXls(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, java.io.IOException {

		List<String> equipmentTabelColumns=Arrays.asList(new String[]
		                           {"eq_id", "name", "dept name", "status","service group", "service sub group", "min duration", "incr duration",
				 					"equipment code", "unit size", "slab1 threshold"});
		 EquipmentMasterDAO dao = new EquipmentMasterDAO();
		 Map<String, List> columnNamesMap = new HashMap<String, List>();
		 columnNamesMap.put("mainItems", equipmentTabelColumns);
		 XSSFWorkbook workbook = new XSSFWorkbook();
		 XSSFSheet equipmentWorkSheet = workbook.createSheet("EQUIPMENT_MASTER");
		 List<BasicDynaBean>eqipmentList=dao.getEquipmentDetails();
		 HsSfWorkbookUtils.createPhysicalCellsWithValues(eqipmentList, columnNamesMap, equipmentWorkSheet, true);
		 res.setHeader("Content-type", "application/vnd.ms-excel");
		 res.setHeader("Content-disposition","attachment; filename=equipmentDefinationDetails.xls");
		 res.setHeader("Readonly", "true");
		 java.io.OutputStream os = res.getOutputStream();
		 workbook.write(os);
		 os.flush();
		 os.close();

		return null;
	}


	public static DetailsImportExporter detailsImporExp;

	static {
		detailsImporExp = new DetailsImportExporter("equipment_master", "equip_org_details", "equipement_charges");

	}

	public ActionForward importEquipmentDetailsFromXls(ActionMapping am, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {

		EquipmentUploadForm equipForm = (EquipmentUploadForm) form;
		ByteArrayInputStream byteStream = new ByteArrayInputStream(equipForm.getXlsEquipmentFile().getFileData());
		XSSFWorkbook workBook = new XSSFWorkbook(byteStream);
		XSSFSheet sheet = workBook.getSheetAt(0);

		this.errors = new StringBuilder();
		Map deptMap = DepartmentMasterDAO.getAlldepartmentsMap();
		Map<String, String> aliasMap = new HashMap<String, String>();

		aliasMap.put("name", "equipment_name");
		aliasMap.put("equipment code", "equipment_code");
		aliasMap.put("status", "status");
		aliasMap.put("dept name", "dept_id");
		aliasMap.put("service sub group", "service_sub_group");
		aliasMap.put("service group", "service_group");
		aliasMap.put("min duration", "min_duration");
		aliasMap.put("incr duration", "incr_duration");
		aliasMap.put("unit size", "duration_unit_minutes");
		aliasMap.put("slab1 threshold", "slab_1_threshold");

		List<String> charges = Arrays.asList("daily_charge", "min_charge", "incr_charge", "tax", "slab_1_charge");
		List<String> mandatoryList = Arrays.asList("equipment_name", "status", "dept_id", "service_sub_group",
				"service_group", "min_duration", "incr_duration", "slab_1_threshold");
		List<String> exemptFromNullCheck = Arrays.asList("eq_id");
		List<String> oddFields = Arrays.asList("");

		detailsImporExp.setTableDbName("equipment_name");
		detailsImporExp.setAliasUnmsToDBnmsMap(aliasMap);
		detailsImporExp.setBed("bed_type");
		detailsImporExp.setCharges(charges);
		detailsImporExp.setOddFields(oddFields);
		detailsImporExp.setId("eq_id");
		detailsImporExp.setIdForOrgTab("equip_id");
		detailsImporExp.setOrgNameForChgTab("org_id");
		detailsImporExp.setMandatoryFields(mandatoryList);
		detailsImporExp.setOrgId("org_id");
		detailsImporExp.setType("EQIPMENTID");
		detailsImporExp.setDeptName("dept_id");
		detailsImporExp.setDeptMap(deptMap);
		detailsImporExp.setSerSubGrpName("service_sub_group");
		detailsImporExp.setSerGrpName("service_group");
		detailsImporExp.setOrderType("Equipment");
		detailsImporExp.setDbCodeName("equipment_code");
		detailsImporExp.setIdForChgTab("equip_id");
		detailsImporExp.setCodeAliasRequired(true);
		detailsImporExp.setDeptNotExist(false);
		detailsImporExp.setUsingHospIdPatterns(false);
		detailsImporExp.setUsingSequencePattern(false);
		detailsImporExp.setUsingUniqueNumber(true);
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
	
	private boolean saveOrUpdateItemSubGroup(String eq_id, Connection con,HttpServletRequest request)
			throws SQLException, IOException{
			Map params = request.getParameterMap();
			List errors = new ArrayList();
			
			boolean flag = true;
			String [] itemSubgroupId = request.getParameterValues("item_subgroup_id");
			String [] delete = request.getParameterValues("deleted");
			
			if (errors.isEmpty()) {
				if(itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")){
					GenericDAO itemsubgroupdao = new GenericDAO("equipment_item_sub_groups");
					BasicDynaBean itemsubgroupbean = itemsubgroupdao.getBean();
					ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
					List records = itemsubgroupdao.findAllByKey("eq_id", eq_id);
					if (records.size() > 0)
						flag = itemsubgroupdao.delete(con, "eq_id", eq_id);
					
					for(int i=0; i<itemSubgroupId.length; i++){
						if(itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
							if (delete[i].equalsIgnoreCase("false")) {
								itemsubgroupbean.set("eq_id", eq_id);
								itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
								flag = itemsubgroupdao.insert(con, itemsubgroupbean);
							}
						}
					}
				}	
			}
			return flag;

		}

	private StringBuilder errors;

}

