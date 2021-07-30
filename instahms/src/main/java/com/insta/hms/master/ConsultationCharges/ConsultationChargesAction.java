package com.insta.hms.master.ConsultationCharges;

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

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.adminmasters.organization.RateMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.HsSfWorkbookUtils;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.ConsultationTypes.ConsultationTypesDAO;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupRepository;
import com.insta.hms.xls.exportimport.ChargesImportExporter;
import com.insta.hms.xls.exportimport.DetailsImportExporter;

import flexjson.JSONSerializer;

public class ConsultationChargesAction extends DispatchAction {

	ConsultationChargesDAO consChargesDao = new ConsultationChargesDAO();
	JSONSerializer js = new JSONSerializer();
    private static final GenericDAO consultationItemSubGroupsDAO =
        new GenericDAO("consultation_item_sub_groups");
    private static final GenericDAO consultationOrgDetailsDAO =
        new GenericDAO("consultation_org_details");
    private static final GenericDAO itemGroupTypeDAO = new GenericDAO("item_group_type");
    private static final GenericDAO itemGroupsDAO = new GenericDAO("item_groups");

	public ActionForward list(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException, Exception {

		Map requestParams = new HashMap();
		requestParams.putAll(req.getParameterMap());
		String orgId = req.getParameter("org_id");
		if ( (orgId == null) || orgId.equals("")) {
			String[] org_id = {"ORG0001"};
			requestParams.put("org_id", org_id);
			orgId = "ORG0001";
		}
		PagedList pagedList = ConsultationChargesDAO.searchList(requestParams, ConversionUtils.getListingParameter(req.getParameterMap()));
		req.setAttribute("pagedList", pagedList);

		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();
		req.setAttribute("bedTypes", bedTypes);
		req.setAttribute("org_id", orgId);

		ConsultationChargesDAO dao = new ConsultationChargesDAO();
		List<BasicDynaBean> chargeList = dao.getAllChargesForOrganisation(orgId);
		req.setAttribute("charges", ConversionUtils.listBeanToMapMapBean(chargeList, "consultation_type_id", "bed_type"));


		return m.findForward("list");
	}

	public ActionForward edit(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException, Exception {

		ConsultationChargesDAO dao = new ConsultationChargesDAO();
		JSONSerializer js = new JSONSerializer().exclude("class");
		String orgId = req.getParameter("org_id");
		String consultationId = req.getParameter("consultation_type_id");

		BasicDynaBean consultationTypeBean = new ConsultationTypesDAO().findByKey("consultation_type_id", Integer.parseInt(consultationId));
		req.setAttribute("consultationTypeBean", consultationTypeBean);

		BasicDynaBean orgBean = new GenericDAO("organization_details").findByKey("org_id", orgId);
		req.setAttribute("orgBean", orgBean);
		req.setAttribute("org_id", orgId);

		List<BasicDynaBean> chargeList = dao.getAllChargesForOrg(orgId, consultationId);
		req.setAttribute("charges", ConversionUtils.listBeanToMapMap(chargeList, "bed_type"));

		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();
		req.setAttribute("bedTypes", bedTypes);

		BasicDynaBean consultOrgBean = dao.getConsultOrgDetailsBean(consultationId, orgId);
		req.setAttribute("consultOrgBean", consultOrgBean);

		List<BasicDynaBean> derivedRatePlanDetails = consChargesDao.getDerivedRatePlanDetails(orgId, consultationId);
		if(derivedRatePlanDetails.size()<0)
        	req.setAttribute("derivedRatePlanDetails", js.serialize(Collections.EMPTY_LIST));
        else
        	req.setAttribute("derivedRatePlanDetails", js.serialize(ConversionUtils.copyListDynaBeansToMap(derivedRatePlanDetails)));

		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException, Exception {

		ConsultationChargesDAO dao = new ConsultationChargesDAO();
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		ArrayList errors = new ArrayList();

		String orgId = req.getParameter("org_id");
		String consultationId = req.getParameter("consultation_type_id");
		String[] beds = req.getParameterValues("bed_type");

		String[] regularCharges = req.getParameterValues("charge");
		String[] consDiscounts = req.getParameterValues("discount");
		String[] derivedRateplanIds = req.getParameterValues("ratePlanId");
	    String[] ratePlanApplicable = req.getParameterValues("applicable");

	    Double[] charges  = new Double[regularCharges.length];
	    Double[] discounts = new Double[consDiscounts.length];
        for(int i = 0; i < regularCharges.length; i++) {
        	charges[i] = new Double(regularCharges[i]);
        	discounts[i] = new Double(consDiscounts[i]);
        }

		Object key1 = req.getParameter("consultation_type_id");
		Object key2 = req.getParameter("org_id");
		Map keys = new HashMap();
		keys.put("consultation_type_id", Integer.parseInt(key1.toString()));
		keys.put("org_id", key2.toString());
		BasicDynaBean bean = consultationOrgDetailsDAO.getBean();
		ConversionUtils.copyToDynaBean(req.getParameterMap(), bean, errors, true);
		bean.set("applicable", true);

		List<BasicDynaBean> chargeList = new ArrayList();
		for (int i=0; i<beds.length; i++) {
			BasicDynaBean charge = dao.getBean();
			ConversionUtils.copyToDynaBean(req.getParameterMap(), charge, errors);
			ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, charge, errors);
			chargeList.add(charge);
		}

		Connection con = null;
		boolean allSuccess = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			int success = consultationOrgDetailsDAO.update(con, bean.getMap(), keys);
			
			if (success > 0)
				allSuccess = true;

			for (BasicDynaBean c: chargeList) {
				dao.updateWithNames(con, c.getMap(), new String[] {"consultation_type_id", "org_id", "bed_type"});
			}

			 if(null != derivedRateplanIds && derivedRateplanIds.length > 0) {
				 allSuccess = allSuccess && consChargesDao.updateOrgForDerivedRatePlans(con,derivedRateplanIds,ratePlanApplicable,consultationId);
				 allSuccess = allSuccess && consChargesDao.updateChargesForDerivedRatePlans(con,orgId,derivedRateplanIds,beds,
						 charges, consultationId,discounts,ratePlanApplicable);
	        }
			RateMasterDao rdao = new RateMasterDao();
			List<BasicDynaBean> allDerivedRatePlanIds = rdao.getDerivedRatePlanIds(orgId);
			if(null != allDerivedRatePlanIds) {
				dao.updateApplicableflagForDerivedRatePlans(con, allDerivedRatePlanIds, "consultation", "consultation_type_id",
						consultationId, "consultation_org_details", orgId);
			}

			allSuccess = true;
			flash.put("success", "Consultation charges updated successfully");
			redirect.addParameter("org_id", orgId);
			redirect.addParameter("consultation_type_id", consultationId);
		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}

		return redirect;
	}

	public ActionForward editTypes(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException, Exception {

		ConsultationTypesDAO dao = new ConsultationTypesDAO();
		BasicDynaBean bean = dao.findByKey("consultation_type_id", Integer.parseInt(req.getParameter("consultation_type_id")));
		req.setAttribute("bean", bean);
		req.setAttribute("org_id", req.getParameter("org_id"));
		Integer consultationTypeId = Integer.parseInt(req.getParameter("consultation_type_id"));
		List<BasicDynaBean> activeInsurance = dao.getActiveInsuranceCategories(consultationTypeId);
		StringBuilder activeInsuranceCategories = new StringBuilder();
		for (BasicDynaBean activeInsuranceCategory : activeInsurance) {
			activeInsuranceCategories.append(activeInsuranceCategory.get("insurance_category_id"));
			activeInsuranceCategories.append(",");
		}
		req.setAttribute("insurance_categories", activeInsuranceCategories.toString());
		req.setAttribute("taxsubgroup", ConversionUtils.copyListDynaBeansToMap(dao.getConsultationItemSubGroupDetails(Integer.parseInt(req.getParameter("consultation_type_id")))));
		req.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(itemGroupTypeDAO.findAllByKey("item_group_type_id","TAX")));
		req.setAttribute("itemGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(itemGroupsDAO.findAllByKey("status","A"))));
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
		return m.findForward("editTypes");
	}

	public ActionForward updateTypes(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws IOException, ServletException, Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		HttpSession session = req.getSession();
		String userName = (String)session.getAttribute("userid");

		ConsultationTypesDAO dao = new ConsultationTypesDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		bean.set("username", userName);
		bean.set("mod_time", DateUtil.getCurrentTimestamp());
		ActionRedirect redirect = new ActionRedirect(m.findForward("showTypesRedirect"));

		Object key = req.getParameter("consultation_type_id");
		Map keys = new HashMap();
		keys.put("consultation_type_id", Integer.parseInt(key.toString()));
		FlashScope flash = FlashScope.getScope(req);

		try {
			if (errors.isEmpty()) {
				int success = dao.update(con, bean.getMap(), keys);
				
				if(success > 0) {
					int consultation_type_id = (Integer) bean.get("consultation_type_id");
					success = updateItemSubGroup(consultation_type_id,con,req);
				}

				if (success > 0) {
					Integer consultationTypeId = Integer.parseInt(bean.get("consultation_type_id").toString());
					success = saveOrUpdateInsuranceCategory(consultationTypeId, con, req);
				}
				
				if (success > 0) {
					con.commit();
					redirect.addParameter("consultation_type_id", req.getParameter("consultation_type_id"));
					redirect.addParameter("org_id",req.getParameter("org_id"));
					return redirect;
				} else {
					con.rollback();
					flash.error("Failed to update Consultation Type details..");
				}

			} else {
				flash.error("Incorrectly formatted values supplied..");
			}
		} finally {
			if (con!=null)
				con.close();
		}
		redirect.addParameter("consultation_type_id", req.getParameter("consultation_type_id"));
		redirect.addParameter("org_id",req.getParameter("org_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward add(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException, Exception {

		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();
		req.setAttribute("bedTypes", bedTypes);
		req.setAttribute("org_id", req.getParameter("org_id"));
		req.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(itemGroupTypeDAO.findAllByKey("item_group_type_id","TAX")));
		req.setAttribute("itemGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(itemGroupsDAO.findAllByKey("status","A"))));
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
		List columnsList = new ArrayList();
		columnsList.add("res_sch_type");
		columnsList.add("res_sch_category");
		columnsList.add("dept");
		columnsList.add("res_sch_name");
		columnsList.add("status");
		columnsList.add("default_duration");
		Map<String, Object> identifiers = new HashMap<>();
		identifiers.put("res_sch_type", "DOC");
		identifiers.put("res_sch_category", "DOC");
		identifiers.put("dept", "*");
		identifiers.put("res_sch_name", "*");
		identifiers.put("status", "A");
		BasicDynaBean schedulerMasterBean = new GenericDAO("scheduler_master").findByKey(columnsList,identifiers);
		req.setAttribute("default_duration", schedulerMasterBean.get("default_duration"));
		req.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(validateItemSubGrouList)));
		return m.findForward("editTypes");
	}

	public ActionForward create(ActionMapping m, ActionForm f,HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException,  Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		HttpSession session = req.getSession();
		String userName = (String)session.getAttribute("userid");

		ConsultationTypesDAO dao = new ConsultationTypesDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		bean.set("username", userName);
		bean.set("mod_time", DateUtil.getCurrentTimestamp());

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;
		boolean success = false;

		int newConsultTypeId= DataBaseUtil.getNextSequence("consultation_type_id_seq");

		try {
			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.findByKey("consultation_type", bean.get("consultation_type"));
				if (exists == null) {
					bean.set("consultation_type_id", newConsultTypeId);
					success = dao.insert(con, bean);

					if (success) {
						new ConsultationChargesDAO().initItemCharges(con, newConsultTypeId, userName);
					}
					
					if(success) {
						int consultation_type_id = (Integer) bean.get("consultation_type_id");
						success = saveItemSubGroup(consultation_type_id,con,req);
					}

					if (success) {
						Integer consultationTypeId = Integer.parseInt(bean.get("consultation_type_id").toString());
						int update = saveOrUpdateInsuranceCategory(consultationTypeId, con, req);
						if (update <= 0) {
							success = false;
						}
					}

					if (success) {
						con.commit();

					} else {
						con.rollback();
						flash.error("Failed to add  Consultation Type..");
					}
				} else {
					flash.error("Consultation Type Name already exists..");

				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
		} finally {
			if (con!=null)
				con.close();
		}
		if (success) {
			redirect = new ActionRedirect(m.findForward("showTypesRedirect"));
			redirect.addParameter("consultation_type_id", newConsultTypeId);
		}
		else
			redirect = new ActionRedirect(m.findForward("addRedirect"));

		redirect.addParameter("org_id", req.getParameter("org_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward exportConsultationDetails(ActionMapping  mapping, ActionForm form,
														HttpServletRequest request, HttpServletResponse response)
																throws ServletException, IOException, SQLException {

		String orgId = request.getParameter("org_id");
		List<String> displayColumns = Arrays.asList("consultation_type_id", "consultation_type", "status",
			" consultation_code", "patient_type", "doctor_charge_type", "charge_head","duration");
		Map<String, List> columnNamesMap = new HashMap<String, List>();
		columnNamesMap.put("mainItems", displayColumns);
		List<BasicDynaBean> consTypeList = ConsultationTypesDAO.getConsultationTypeDetails();
		XSSFWorkbook consWorkBook = new XSSFWorkbook();
		XSSFSheet consSheet = consWorkBook.createSheet("CONSULTATION TYPE");
		HsSfWorkbookUtils.createPhysicalCellsWithValues(consTypeList, columnNamesMap, consSheet, true);
		response.setHeader("Content-type", "application/vnd.ms-excel");
		response.setHeader("Content-disposition", "attachment; filename=ConsultationTypeDetails.xls");
		response.setHeader("Readonly", "true");
		java.io.OutputStream os = response.getOutputStream();
		consWorkBook.write(os);
		os.flush();
		os.close();
		return null;
	}
	private StringBuilder errors;
	public static DetailsImportExporter detailsImporExp;

	static {
		detailsImporExp = new DetailsImportExporter("consultation_types", "consultation_org_details", "consultation_charges");

	}

	public ActionForward importConsultationDetails(ActionMapping mapping, ActionForm form, HttpServletRequest request,
														HttpServletResponse response)throws ServletException, IOException, SQLException {

		ConsultationUploadForm consultationForm = (ConsultationUploadForm ) form;
		ByteArrayInputStream byteStream = new ByteArrayInputStream(consultationForm.getXlsConsultaionDetails().getFileData());
		XSSFWorkbook workBook = new XSSFWorkbook(byteStream);
		XSSFSheet sheet = workBook.getSheetAt(0);
		this.errors = new StringBuilder();
		Map<String, String> aliasMap = new HashMap<String, String>();

		aliasMap.put("name", "consultation_type");
		aliasMap.put("code", "consultation_code");
		aliasMap.put("status", "status");
		aliasMap.put("patient type", "patient_type");
		aliasMap.put("doctor charge type", "doctor_charge_type");
		aliasMap.put("charge head", "charge_head");
		aliasMap.put("duration", "duration");

		List<String> charges = Arrays.asList("charge");
		List<String> mandatoryList = Arrays.asList("consultation_type", "status", "doctor_charge_type", "charge_head", "patient_type","duration");
		List<String> exemptFromNullCheck = Arrays.asList("consultation_type_id");
		List<String> oddFields = Arrays.asList("consultation_code");

		detailsImporExp.setTableDbName("consultation_type");
		detailsImporExp.setAliasUnmsToDBnmsMap(aliasMap);
		detailsImporExp.setBed("bed_type");
		detailsImporExp.setCharges(charges);
		detailsImporExp.setOddFields(oddFields);
		detailsImporExp.setId("consultation_type_id");
		detailsImporExp.setIdForOrgTab("consultation_type_id");
		detailsImporExp.setOrgNameForChgTab("org_id");
		detailsImporExp.setMandatoryFields(mandatoryList);
		detailsImporExp.setOrgId("org_id");
		detailsImporExp.setType(null);
		detailsImporExp.setDeptName("");
		detailsImporExp.setDeptMap(null);
		detailsImporExp.setSerSubGrpName("");
		detailsImporExp.setSerGrpName("");
		detailsImporExp.setOrderType("");
		detailsImporExp.setDbCodeName("service_code");
		detailsImporExp.setIdForChgTab("consultation_type_id");
		detailsImporExp.setCodeAliasRequired(false);
		detailsImporExp.setDeptNotExist(true);
		detailsImporExp.setUsingHospIdPatterns(false);
		detailsImporExp.setUsingSequencePattern(true);
		detailsImporExp.setSequenceName("consultation_type_id_seq");
		detailsImporExp.setUsingUniqueNumber(false);
		detailsImporExp.setIsDateRequired(true);
		detailsImporExp.setIsUserNameRequired(true);
		detailsImporExp.setColumnNameForDate("mod_time");
		detailsImporExp.setColumnNameForUser("username");
		detailsImporExp.setExemptFromNullCheck(exemptFromNullCheck);
		detailsImporExp.setUseAuditLogHint(true);

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

	private static ChargesImportExporter importExporter;

	static {

		importExporter = new ChargesImportExporter("consultation_types", "consultation_org_details",
				"consultation_charges", null, "consultation_type_id", null, null,
				new String[] {"consultation_type", "status"}, new String[] {"Consultation name", "Status"},
				new String[] {"applicable", "item_code"}, new String[] {"Applicable", "Code"},
				new String[] {"charge", "discount"}, new String[] {"Charge", "Discount"});

		importExporter.setItemWhereFieldKeys(new String[] {"consultation_type_id"});
		importExporter.setOrgWhereFieldKeys(new String[] {"consultation_type_id", "org_id"});
		importExporter.setChargeWhereFieldKeys(new String[] {"consultation_type_id", "org_id"});
		importExporter.setMandatoryFields(new String[] {"consultation_type"});
		importExporter.setItemName("consultation_type");
	}


	public ActionForward exportConsultationCharges(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {

		String orgId = request.getParameter("orgId");
		String orgName = (String)OrgMasterDao.getOrgdetailsDynaBean(orgId).get("org_name");
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet workSheet = workbook.createSheet("CONSULTATION CHARGES");
		importExporter.exportCharges(orgId, workSheet, null, "A");

		response.setHeader("Content-type", "application/vnd.ms-excel");
		response.setHeader("Content-disposition","attachment; filename="+"\"ConsultationCharges_"+orgName+".xls\"");
		response.setHeader("Readonly", "true");
		java.io.OutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		outputStream.flush();
		outputStream.close();

		return null;
	}

	public ActionForward importConsultationCharges(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, Exception {

		//String orgId = request.getParameter("orgId");
		ConsultationUploadForm consultationForm = (ConsultationUploadForm) form;
		String orgId = consultationForm.getOrg_id();
		XSSFWorkbook workBook = new XSSFWorkbook(consultationForm.getXlsConsultaionCharges().getInputStream());
		XSSFSheet sheet = workBook.getSheetAt(0);
		this.errors = new StringBuilder();

		FlashScope flash = FlashScope.getScope(request);
		String referer = request.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		String userName = (String)request.getSession(false).getAttribute("userid");

		ConsultationChargesDAO.backupCharges(orgId, userName);
		importExporter.setUseAuditLogHint(true);
		importExporter.importCharges(true, orgId, sheet, userName, this.errors);

		consChargesDao.updateChargesForDerivedRatePlans(orgId,userName,"consultation",true);


		if (this.errors.length() > 0)
			flash.put("error", this.errors);
		else
			flash.put("info", "File successfully uploaded");

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	/*
	 * Group Update: called from the main list screen, updates the charges of all/selected
	 * consultation type by a formula: +/- a certain amount or percentage,
	 */
	public ActionForward groupUpdate(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		FlashScope flash = FlashScope.getScope(req);

		ActionRedirect redirect = new ActionRedirect(req.getHeader("Referer").
				replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		String orgId = req.getParameter("org_id");
		String amtType = req.getParameter("amtType");
		String incType = req.getParameter("incType");
		String updateTable = req.getParameter("updateTable");
		String userName = (String) req.getSession().getAttribute("userid");

		String allConsultations = req.getParameter("allConsultations");
		String allBedTypes = req.getParameter("allBedTypes");

		List consultations = null;
		if (!allConsultations.equals("yes"))
			consultations = ConversionUtils.getParamAsList(req.getParameterMap(), "selectConsultation");

		List bedTypes = null;
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

		ConsultationChargesDAO dao = new ConsultationChargesDAO();

		Connection con = null;
		boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			dao.groupIncreaseCharges(con, orgId, bedTypes, consultations, amount, amtType.equals("%"),
					round, updateTable, (String)req.getSession(false).getAttribute("userid"));
			success = true;
			if(success)con.commit();
		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		if(success)
			consChargesDao.updateChargesForDerivedRatePlans(orgId,userName,"consultation",false);


		if (success)
			flash.put("success", "Charges updated successfully");
		else
			flash.put("error", "Error updating charges");

		return redirect;
	}
	
	private boolean saveItemSubGroup(int consultation_type_id, Connection con,HttpServletRequest request)
			throws SQLException, IOException{
			Map params = request.getParameterMap();
			List errors = new ArrayList();
			
			boolean flag = true;
			String [] itemSubgroupId = request.getParameterValues("item_subgroup_id");
			String [] delete = request.getParameterValues("deleted");
			
			if (errors.isEmpty()) {
				if(itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")){
					BasicDynaBean itemsubgroupbean = consultationItemSubGroupsDAO.getBean();
					ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
					List records = consultationItemSubGroupsDAO.findAllByKey("consultation_type_id", consultation_type_id);
					if (records.size() > 0)
						flag = consultationItemSubGroupsDAO.delete(con, "consultation_type_id", consultation_type_id);
					
					for(int i=0; i<itemSubgroupId.length; i++){
						if(itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
							if (delete[i].equalsIgnoreCase("false")) {
								itemsubgroupbean.set("consultation_type_id", consultation_type_id);
								itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
								flag = consultationItemSubGroupsDAO.insert(con, itemsubgroupbean);
							}
						}
					}
				}	
			}
			return flag;

		}
	
	private int updateItemSubGroup(int consultation_type_id, Connection con,HttpServletRequest request)
			throws SQLException, IOException{
			Map params = request.getParameterMap();
			List errors = new ArrayList();
			
			int flag = 1;
			String [] itemSubgroupId = request.getParameterValues("item_subgroup_id");
			String [] delete = request.getParameterValues("deleted");
			
			if (errors.isEmpty()) {
				if(itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")){
					BasicDynaBean itemsubgroupbean = consultationItemSubGroupsDAO.getBean();
					ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
					List records = consultationItemSubGroupsDAO.findAllByKey("consultation_type_id", consultation_type_id);
					if (records.size() > 0)
						flag = (consultationItemSubGroupsDAO.delete(con, "consultation_type_id", consultation_type_id)) ? 1: 0;
					
					for(int i=0; i<itemSubgroupId.length; i++){
						if(itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
							if (delete[i].equalsIgnoreCase("false")) {
								itemsubgroupbean.set("consultation_type_id", consultation_type_id);
								itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
								flag = consultationItemSubGroupsDAO.insert(con, itemsubgroupbean) ? 1:0;
							}
						}
					}
					
				}	
			}
			return flag;

		}

	private int saveOrUpdateInsuranceCategory(Integer consultationTypeId,
		Connection con, HttpServletRequest request) throws SQLException, IOException {
		int flag = 1;
		String[] insuranceCategories = request.getParameterValues("insurance_category_id");
		if (insuranceCategories != null && insuranceCategories.length > 0
			&& !insuranceCategories[0].equals("")) {
			GenericDAO insuranceCategoryDAO =
				new GenericDAO("consultation_types_insurance_category_mapping");
			BasicDynaBean insuranceCategoryBean = insuranceCategoryDAO.getBean();
			List<BasicDynaBean> records = insuranceCategoryDAO.findAllByKey("consultation_type_id", consultationTypeId);
			if (records != null && records.size() > 0) {
				flag = insuranceCategoryDAO.delete(con,"consultation_type_id", consultationTypeId) ? 1:0;
			}
			for (String insuranceCategory :  insuranceCategories) {
				insuranceCategoryBean.set("consultation_type_id", consultationTypeId);
				insuranceCategoryBean.set("insurance_category_id", Integer.parseInt(insuranceCategory));
				flag = insuranceCategoryDAO.insert(con,insuranceCategoryBean) ? 1:0;
			}
		}
		return flag;
	}

}
