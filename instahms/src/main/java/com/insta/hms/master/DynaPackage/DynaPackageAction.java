package com.insta.hms.master.DynaPackage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import com.bob.hms.common.HsSfWorkbookUtils;
import com.bob.hms.common.RequestContext;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.csvutils.TableDataHandler;
import com.insta.hms.jobs.MasterChargesCronSchedulerDetailsRepository;
import com.insta.hms.master.DynaPackageCategory.DynaPackageCategoryMasterDAO;
import com.insta.hms.master.ServiceMaster.ServiceDepartmentDAO;
import com.insta.hms.mdm.dynapackage.DynaPackageService;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupRepository;
import com.insta.hms.xls.exportimport.ChargesImportExporter;
import com.insta.hms.xls.exportimport.DetailsImportExporter;

import flexjson.JSONSerializer;

public class DynaPackageAction extends DispatchAction {
	
	private static DynaPackageService dynaPackageService = ApplicationContextProvider.getBean(DynaPackageService.class);

  private static final String NEW_DYNA_PACKAGE = "NewDynaPKG";

  private static final String UPDATE_DYNA_PACKAGE = "UpdateDynaPKG";

  DynaPackageDAO dao = new DynaPackageDAO();
	DynaPackageChargesDAO cdao = new DynaPackageChargesDAO();
	DynaPackageOrgDAO odao = new DynaPackageOrgDAO();
	DynaPackageCategoryMasterDAO catdao = new DynaPackageCategoryMasterDAO();
	DynaPackageCategoryLimitsDAO limitdao = new DynaPackageCategoryLimitsDAO();

	@SuppressWarnings("unchecked")
	public ActionForward list (ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException, Exception {

		JSONSerializer json = new JSONSerializer().exclude("class");
		DynaPackageDAO dao = new DynaPackageDAO();
		Map requestParams = new HashMap();
		requestParams.putAll(req.getParameterMap());
		String orgId = req.getParameter("org_id");
		if ( (orgId == null) || orgId.equals("")) {
			String[] org_id = {"ORG0001"};
			requestParams.put("org_id", org_id);
			orgId = "ORG0001";
		}

		PagedList list = dao.search(requestParams, ConversionUtils.getListingParameter(req.getParameterMap()));
		req.setAttribute("pagedList", list);

		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();
		req.setAttribute("bedTypes", bedTypes);
		req.setAttribute("org_id", orgId);

		List<BasicDynaBean> chargeList = cdao.getAllPackageChargesForOrganisation(orgId);
		req.setAttribute("charges", ConversionUtils.listBeanToMapMapBean(chargeList, "dyna_package_id", "bed_type"));
		req.setAttribute("categories", ConversionUtils.listBeanToListMap(catdao.listAll()));
		req.setAttribute("dynapackageNamesJSON", json.serialize(dao.getColumnList("dyna_package_name")));
		
	    Map<String, Object> cronJobKeys = new HashMap<String, Object>();
	    cronJobKeys.put("entity", "NewDynaPKG");
	    ArrayList<String> status = new ArrayList<String>();
	    status.add("F");
	    status.add("P");
	    cronJobKeys.put("status", status);
	    MasterChargesCronSchedulerDetailsRepository masterChargesCronSchedulerDetailsRepository =
	           ApplicationContextProvider.getBean(MasterChargesCronSchedulerDetailsRepository.class);
	    List<BasicDynaBean> masterCronJobDetails =
	        masterChargesCronSchedulerDetailsRepository.findByCriteria(cronJobKeys);
	    req.setAttribute("masterCronJobDetails",
	        ConversionUtils.listBeanToListMap(masterCronJobDetails));

		return m.findForward("list");
	}

	public ActionForward add (ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		List<BasicDynaBean> categories = catdao.getCategories();
		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();
		req.setAttribute("derivedRatePlanDetails", js.serialize(Collections.EMPTY_LIST));
		req.setAttribute("bedTypes", bedTypes);
		req.setAttribute("categories", ConversionUtils.listBeanToListMap(categories));
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

		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, IOException, Exception {

		HttpSession session = req.getSession();
    	String userName = (String)session.getAttribute("userid");
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect=new ActionRedirect(mapping.findForward("addRedirect"));

		ArrayList errors = new ArrayList();
		BasicDynaBean dynaPackage = dao.getBean();
		BasicDynaBean orgDetails = odao.getBean();
		dynaPackage.set("username", userName);
		ConversionUtils.copyToDynaBean(req.getParameterMap(), dynaPackage, errors, true);

		BasicDynaBean exists = dao.findExistsByKey("dyna_package_name", dynaPackage.get("dyna_package_name"));
		if(exists != null) {
			flash.error( "Duplicate dyna package name: "+ dynaPackage.get("dyna_package_name"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

		int dynaPackageId = dao.getNextSequence();
		String dynaPackageName = req.getParameterValues("dyna_package_name")[0].toString();
		dynaPackage.set("dyna_package_id", dynaPackageId);
		ConversionUtils.copyToDynaBean(req.getParameterMap(), orgDetails, errors);
		orgDetails.set("dyna_package_id", dynaPackageId);
		String[] beds = req.getParameterValues("bed_type");
		String[] categoryIds = req.getParameterValues("dyna_pkg_cat_id");

		List<BasicDynaBean> chargeList = new ArrayList<BasicDynaBean>();
		for (int i=0; i<beds.length; i++) {
			BasicDynaBean charge = cdao.getBean();
			charge.set("username", userName);
			ConversionUtils.copyToDynaBean(req.getParameterMap(), charge, errors);
			ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, charge, errors);
			charge.set("dyna_package_id", dynaPackageId);
			if (charge.get("charge") == null)
				charge.set("charge", BigDecimal.ZERO);
			chargeList.add(charge);
		}

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

		Connection con = null;
		boolean success = false;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			success = dao.insert(con, dynaPackage);
			
			if(success) {
				int dynapackId = (Integer) dynaPackage.get("dyna_package_id");
				success = saveOrUpdateItemSubGroup(dynapackId,con,req);
			}

			if (success)
				success = odao.insert(con, orgDetails);

			if (success) {
				cdao.insertAll(con, chargeList);
			}

			Map from = req.getParameterMap();

			List<BasicDynaBean> limitsList = new ArrayList<BasicDynaBean>();
			if (categoryIds != null && categoryIds.length > 0) {
				for (int j = 0; j < categoryIds.length; j++) {
					for (int i = 0; i < beds.length; i++) {
						BasicDynaBean charge = limitdao.getBean();

						charge.set("dyna_package_id", (Integer)dynaPackage.get("dyna_package_id"));
						charge.set("dyna_pkg_cat_id", new Integer(categoryIds[j]));
						charge.set("bed_type", beds[i]);
						charge.set("org_id", (String)orgDetails.get("org_id"));

						Object[] object = (Object[])from.get(categoryIds[j]+"."+"pkg_included");
						if (object != null && object.length > i && object[i] != null)
							charge.set("pkg_included", new String(object[i].toString()));

						object = (Object[])from.get(categoryIds[j]+"."+"amount_limit");
						if (object != null && object.length > i && object[i] != null) {
							if (object[i].toString().trim().equals(""))
								charge.set("amount_limit", new BigDecimal(0));
							else
								charge.set("amount_limit", new BigDecimal(object[i].toString()));
						}

						object = (Object[])from.get(categoryIds[j]+"."+"qty_limit");
						if (object != null && object.length > i && object[i] != null) {
							if (object[i].toString().trim().equals(""))
								charge.set("qty_limit", new BigDecimal(0));
							else
								charge.set("qty_limit", new BigDecimal(object[i].toString()));

						}

						charge.set("username", userName);
						limitsList.add(charge);
					}
				}
			}
			if (success) {
				limitdao.insertAll(con, limitsList);
			}

			if (success) {
				scheduleDynaPackageCreation(dynaPackageId, dynaPackageName, NEW_DYNA_PACKAGE, null, null, null, null, userName);
			}

			if (success) {
				redirect=new ActionRedirect(mapping.findForward("showRedirect"));
				flash.put("success", "Dyna package details inserted successfully");
			}
			else
				flash.put("error", "Failed to insert dyna package details");

			redirect.addParameter("dyna_package_id", dynaPackageId);
			redirect.addParameter("org_id", req.getParameter("org_id"));

		} finally {
			DataBaseUtil.commitClose(con, success);
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward show (ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException, Exception {

		String orgId = req.getParameter("org_id");
		String dynaPackageId = req.getParameter("dyna_package_id");

		JSONSerializer js = new JSONSerializer().exclude("class");

		DynaPackageDAO dao = new DynaPackageDAO();
		BasicDynaBean bean = dao.getDynaPackageDetailsBean(Integer.parseInt(dynaPackageId), orgId);
		req.setAttribute("bean", bean);

		List<BasicDynaBean> categories = catdao.getCategories();
		List<BasicDynaBean> chargeList = cdao.getAllChargesForOrg(orgId, dynaPackageId, categories);
		req.setAttribute("charges", ConversionUtils.listBeanToMapMapBean(chargeList, "bed_type", "dyna_pkg_cat_id"));

		List<BasicDynaBean> derivedRatePlanDetails = cdao.getDerivedRatePlanDetails(orgId, dynaPackageId);

		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();
		req.setAttribute("bedTypes", bedTypes);
		req.setAttribute("categories", ConversionUtils.listBeanToListMap(categories));
		   if(derivedRatePlanDetails.size()<0)
			   req.setAttribute("derivedRatePlanDetails", js.serialize(Collections.EMPTY_LIST));
	        else
	        	req.setAttribute("derivedRatePlanDetails", js.serialize(ConversionUtils.copyListDynaBeansToMap(derivedRatePlanDetails)));
		  
		   req.setAttribute("taxsubgroup", ConversionUtils.copyListDynaBeansToMap(dao.getDynaPackItemSubGroupDetails(Integer.parseInt(dynaPackageId)))); 
		   req.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(new GenericDAO("item_group_type").findAllByKey("item_group_type_id", "TAX")));
		   req.setAttribute("itemGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(new GenericDAO("item_groups").findAllByKey("status", "A"))));
			//req.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(new GenericDAO("item_sub_groups").findAllByKey("status", "A"))));
			List <BasicDynaBean> itemSubGroupList = new TaxSubGroupRepository().getItemSubGroup(new java.sql.Date(new java.util.Date().getTime()));
			Iterator<BasicDynaBean> itemSubGroupListIterator = itemSubGroupList.iterator();
			List<BasicDynaBean> validateItemSubGrouList = new ArrayList<BasicDynaBean>();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String currentDateStr = sdf.format(new java.util.Date());
			while(itemSubGroupListIterator.hasNext()) {
				BasicDynaBean itemSubGroupbean = itemSubGroupListIterator.next();
				if(itemSubGroupbean.get("validity_end") != null){
					Date endDate = (Date)itemSubGroupbean.get("validity_end");
					
					try {
						if(sdf.parse(currentDateStr).getTime() <= endDate.getTime()) {
							validateItemSubGrouList.add(itemSubGroupbean);
						}
					} catch (ParseException e) {
						continue;
					}
				} else {
					validateItemSubGrouList.add(itemSubGroupbean);
				}
			}
			req.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(validateItemSubGrouList)));

		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, IOException, Exception {

		HttpSession session = req.getSession();
    	String userName = (String)session.getAttribute("userid");
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect= new ActionRedirect(mapping.findForward("showRedirect"));
		ArrayList errors = new ArrayList();

		BasicDynaBean dynaPackageBean = dao.getBean();
		ConversionUtils.copyToDynaBean(req.getParameterMap(), dynaPackageBean, errors, true);

		String[] derivedRateplanIds = req.getParameterValues("ratePlanId");
	    String[] ratePlanApplicable = req.getParameterValues("applicable");

		String dynaPackageId = req.getParameter("dyna_package_id");
		String dynaPackageName = req.getParameterValues("dyna_package_name")[0].toString();
		BasicDynaBean orgDetails = odao.getBean();
		dynaPackageBean.set("username", userName);
		ConversionUtils.copyToDynaBean(req.getParameterMap(), orgDetails, errors);
		orgDetails.set("applicable", true);
		String[] beds = req.getParameterValues("bed_type");
		String[] categoryIds = req.getParameterValues("dyna_pkg_cat_id");

		List<BasicDynaBean> chargeList = new ArrayList();
		for (int i = 0; i < beds.length; i++) {
			BasicDynaBean charge = cdao.getBean();
			ConversionUtils.copyToDynaBean(req.getParameterMap(), charge, errors);
			ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, charge, errors);
			charge.set("username", userName);
			if (charge.get("charge") == null) {
				charge.set("charge", BigDecimal.ZERO);
				chargeList.add(charge);
			} else if (((BigDecimal) charge.get("charge")).signum() == -1) {
				flash.put("error", "Negative value supplied for charge");
			} else {
				chargeList.add(charge);
			}
		}

		Map from = req.getParameterMap();

		List<BasicDynaBean> limitsList = new ArrayList();
		if (categoryIds != null && categoryIds.length > 0) {
			for (int j = 0; j < categoryIds.length; j++) {
				for (int i = 0; i < beds.length; i++) {
					BasicDynaBean charge = limitdao.getBean();

					charge.set("dyna_package_id", (Integer)dynaPackageBean.get("dyna_package_id"));
					charge.set("dyna_pkg_cat_id", new Integer(categoryIds[j]));
					charge.set("bed_type", beds[i]);
					charge.set("org_id", (String)orgDetails.get("org_id"));

					Object[] object = (Object[])from.get(categoryIds[j]+"."+"pkg_included");
					if (object != null && object.length > i && object[i] != null)
						charge.set("pkg_included", new String(object[i].toString()));

					object = (Object[])from.get(categoryIds[j]+"."+"amount_limit");
					if (object != null && object.length > i && object[i] != null) {
						if (object[i].toString().trim().equals(""))
							charge.set("amount_limit", new BigDecimal(0));
						else
							charge.set("amount_limit", new BigDecimal(object[i].toString()));
					}

					object = (Object[])from.get(categoryIds[j]+"."+"qty_limit");
					if (object != null && object.length > i && object[i] != null) {
						if (object[i].toString().trim().equals(""))
							charge.set("qty_limit", new BigDecimal(0));
						else
							charge.set("qty_limit", new BigDecimal(object[i].toString()));
					}

					charge.set("username", userName);
					limitsList.add(charge);
				}
			}
		}

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
		}

		Connection con = null;
		boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			success = (1 == dao.update(con, dynaPackageBean.getMap(), "dyna_package_id", Integer.parseInt(dynaPackageId)));
			
			if(success) {
				int dynapackId = (Integer) dynaPackageBean.get("dyna_package_id");
				success = saveOrUpdateItemSubGroup(dynapackId,con,req);
			}

			if (success)
				odao.updateWithNames(con, orgDetails.getMap(), new String[] {"dyna_package_id", "org_id"});

			if (success) {
				for (BasicDynaBean c: chargeList) {
					cdao.updateWithNames(con, c.getMap(), new String[] {"dyna_package_id", "org_id", "bed_type"});
				}
			}

			if (success) {
				for (BasicDynaBean lmt: limitsList) {
					limitdao.updateWithNames(con, lmt.getMap(), new String[] {"dyna_package_id", "dyna_pkg_cat_id", "org_id", "bed_type"});
				}
			}

		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		  if(null != derivedRateplanIds && derivedRateplanIds.length > 0) {
			  
			  scheduleDynaPackageCreation(Integer.valueOf(dynaPackageId), dynaPackageName, UPDATE_DYNA_PACKAGE
						, derivedRateplanIds, ratePlanApplicable, orgDetails, categoryIds, userName);
		  }

		redirect.addParameter("dyna_package_id", req.getParameter("dyna_package_id"));
		redirect.addParameter("org_id", req.getParameter("org_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}


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

		String allPackages = req.getParameter("allPackages");
		String allBedTypes = req.getParameter("allBedTypes");

		String userName = (String)req.getSession(false).getAttribute("userid");

		List<String> packages = null;
		if (!allPackages.equals("yes"))
			packages = ConversionUtils.getParamAsList(req.getParameterMap(), "selectPackage");

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

		Connection con = null;
		boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			cdao.groupIncreaseCharges(con, orgId, bedTypes, packages, amount, amtType.equals("%"),
					round, updateTable, (String)req.getSession(false).getAttribute("userid"));
			success = true;
			if(success)
				con.commit();

		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		if(success) {
			cdao.updateChargesForDerivedRatePlans(orgId, userName, "dynaPackages",false);
		}

		if (success)
			flash.put("success", "Charges updated successfully");
		else
			flash.put("error", "Error updating charges");

		return redirect;
	}

	public ActionForward exportPackageDetailsToXls(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, java.io.IOException {

		List<String> packageColumnTableColumns = Arrays.asList(new String[] {"dyna_package_id", "dyna_package_name", "status"});

		 XSSFWorkbook workbook = new XSSFWorkbook();
		 XSSFSheet packageWorkSheet = workbook.createSheet("PACKAGES");
		 List<BasicDynaBean> packageList = DynaPackageDAO.getDynaPackageDetails();
		 Map<String, List> columnNamesMap = new HashMap<String, List>();
		 columnNamesMap.put("mainItems", packageColumnTableColumns);
		 HsSfWorkbookUtils.createPhysicalCellsWithValues(packageList,columnNamesMap,packageWorkSheet, true);
		 res.setHeader("Content-type", "application/vnd.ms-excel");
		 res.setHeader("Content-disposition","attachment; filename=PackageDefinitionDetails.xls");
		 res.setHeader("Readonly", "true");
		 java.io.OutputStream os = res.getOutputStream();
		 workbook.write(os);
		 os.flush();
		 os.close();
		return null;
	}

	public static DetailsImportExporter detailsImporExp;

	static {
		detailsImporExp = new DetailsImportExporter("dyna_packages", "dyna_package_org_details", "dyna_package_charges");

	}

	public ActionForward importDynaPkgDetailsFromXls(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {


		String orgId = request.getParameter("orgId");
		DynaPkgUploadForm dynaForm = (DynaPkgUploadForm) form;
		ByteArrayInputStream byteStream = new ByteArrayInputStream(dynaForm.getXlsDetailsForm().getFileData());
		XSSFWorkbook workBook = new XSSFWorkbook(byteStream);
		XSSFSheet sheet = workBook.getSheetAt(0);

		this.errors = new StringBuilder();
		HashMap servDeptHashMap=ServiceDepartmentDAO.getServiceDepartmentHashMap();

		boolean success = false;
		Map<String, String> aliasMap = new HashMap<String, String>();

		aliasMap.put("name", "dyna_package_name");
		aliasMap.put("status", "status");

		List<String> charges = Arrays.asList("charge");
		List<String> mandatoryList = Arrays.asList("dyna_package_name");
		List<String> oddFields = Arrays.asList("item_code");
		List<String> exemptFromNullCheck = Arrays.asList("dyna_package_id");

		detailsImporExp.setTableDbName("dyna_package_name");
		detailsImporExp.setAliasUnmsToDBnmsMap(aliasMap);
		detailsImporExp.setBed("bed_type");
		detailsImporExp.setCharges(charges);
		detailsImporExp.setOddFields(oddFields);
		detailsImporExp.setId("dyna_package_id");
		detailsImporExp.setIdForOrgTab("dyna_package_id");
		detailsImporExp.setOrgNameForChgTab("org_id");
		detailsImporExp.setMandatoryFields(mandatoryList);
		detailsImporExp.setOrgId("org_id");
		detailsImporExp.setType(null);
		detailsImporExp.setDeptName("");
		detailsImporExp.setDeptMap(servDeptHashMap);
		detailsImporExp.setSerSubGrpName("");
		detailsImporExp.setSerGrpName("");
		detailsImporExp.setOrderType(null);
		detailsImporExp.setDbCodeName("item_code");
		detailsImporExp.setIdForChgTab("dyna_package_id");
		detailsImporExp.setCodeAliasRequired(false);
		detailsImporExp.setDeptNotExist(true);
		detailsImporExp.setUsingHospIdPatterns(false);
		detailsImporExp.setUsingSequencePattern(false);
		detailsImporExp.setUsingUniqueNumber(false);
		detailsImporExp.setIsDateRequired(false);
		detailsImporExp.setExemptFromNullCheck(exemptFromNullCheck);
		detailsImporExp.setColumnNameForUser("username");
		detailsImporExp.setIsUserNameRequired(true);
		detailsImporExp.setUseAuditLogHint(true);

		String referer = request.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		FlashScope flash = FlashScope.getScope(request);
		detailsImporExp.importDetailsToXls(sheet, orgId, errors,
					(String)request.getSession(false).getAttribute("userid"));

		if (this.errors.length() > 0)
			flash.put("error", this.errors);
		else {
			flash.put("info", "File successfully uploaded");
			Connection con = DataBaseUtil.getConnection();
			DynaPackageCategoryLimitsDAO.fixMissingLimits(con);
			con.close();
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	private static ChargesImportExporter importExporter;

	static {
		importExporter = new ChargesImportExporter("dyna_packages", "dyna_package_org_details",
				"dyna_package_charges", null, "dyna_package_id", null, null,
				new String[] {"dyna_package_name", "status"}, new String[] {"Dyna Package Name", "Status"},
				new String[] {"applicable", "item_code"}, new String[] {"Applicable", "Code"},
				new String[] {"charge"}, new String[] {"Charge"});

		importExporter.setItemWhereFieldKeys(new String[] {"dyna_package_id"});
		importExporter.setOrgWhereFieldKeys(new String[] {"dyna_package_id", "org_id"});
		importExporter.setChargeWhereFieldKeys(new String[] {"dyna_package_id", "org_id"});
		importExporter.setMandatoryFields(new String[] {"dyna_package_name"});
		importExporter.setItemName("dyna_package_name");
	}


	public ActionForward exportChargesToXls(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {

		String orgId = request.getParameter("org_id");
		String orgName = (String)OrgMasterDao.getOrgdetailsDynaBean(orgId).get("org_name");
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet workSheet = workbook.createSheet("PACKAGE CHARGES");
		importExporter.exportCharges(orgId, workSheet, null, "A");

		response.setHeader("Content-type", "application/vnd.ms-excel");
		response.setHeader("Content-disposition","attachment; filename="+"\"PackageCharges_"+orgName+".xls\"");
		response.setHeader("Readonly", "true");
		java.io.OutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		outputStream.flush();
		outputStream.close();

		return null;
	}

	public StringBuilder errors;

	public ActionForward importChargesFromXls(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, Exception {

		DynaPkgUploadForm suForm = (DynaPkgUploadForm) form;
		String orgId = suForm.getOrg_id();
		XSSFWorkbook workBook = new XSSFWorkbook(suForm.getXlsChargesForm().getInputStream());
		XSSFSheet sheet = workBook.getSheetAt(0);
		this.errors = new StringBuilder();

		FlashScope flash = FlashScope.getScope(request);
		String referer = request.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		String userName = (String)request.getSession(false).getAttribute("userid");

		/*
		 * Keep a backup of the rates for safety: TODO: be able to checkpoint and revert
		 * to a previous version if required.
		 */
		DynaPackageChargesDAO.backupCharges(userName, orgId);
		DynaPackageCategoryLimitsDAO.backupCharges(userName, orgId);
		importExporter.setUseAuditLogHint(true);
		importExporter.importCharges(true, orgId, sheet, userName, this.errors);

		cdao.updateChargesForDerivedRatePlans(orgId, userName, "dynaPackages",true);

		if (this.errors.length() > 0)
			flash.put("error", this.errors);
		else
			flash.put("info", "File successfully uploaded");

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	// private static TableDataHandler theLimitsCsvHandler = null;

	private TableDataHandler getLimitsCsvHandler() {
		return getLimitsCsvHandler(null);
	}

	private TableDataHandler getLimitsCsvHandler(String orgId) {
			boolean filterByOrgId = (null != orgId && !"".equals(orgId));
			String orgIdFilter = (filterByOrgId) ? "dyna_package_category_limits.org_id = '" + orgId + "'" : "";
			String filters[] = null;

			if (filterByOrgId) {
				filters = new String[] {"dyna_packages.status='A'","organization_details.status = 'A'",
						"bed_types.status = 'A'",
						"bed_types.billing_bed_type='Y'", orgIdFilter};
			} else {
				filters = new String[] {"dyna_packages.status='A'","organization_details.status = 'A'",
						"bed_types.status = 'A'",
						"bed_types.billing_bed_type='Y'"};
			}
			TableDataHandler limitsCsvHandler = new TableDataHandler(
					"dyna_package_category_limits",		// table name
					new String[]{"dyna_package_id", "org_id", "bed_type", "dyna_pkg_cat_id"},	// keys
					new String[]{"pkg_included", "amount_limit", "qty_limit"},		// other fields
					new String[][]{	// masters
						// our field        ref table        ref table id field  ref table name field
						{"dyna_package_id", "dyna_packages", "dyna_package_id", "dyna_package_name"},
						{"dyna_pkg_cat_id", "dyna_package_category", "dyna_pkg_cat_id","dyna_pkg_cat_name"},
						{"org_id", "organization_details", "org_id","org_name"},
						{"bed_type", "bed_types", "bed_type_name","bed_type_name"}
					}, filters );
			limitsCsvHandler.setAlias("org_id", "rate_plan");

		return limitsCsvHandler;
	}


	public ActionForward exportLimitsToCsv(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		String orgId = (String)req.getParameter("org_id");
		String orgName = (String)OrgMasterDao.getOrgdetailsDynaBean(orgId).get("org_name");
		getLimitsCsvHandler(orgId).exportTable(res, "DynaPackageCategoryLimits_" + orgName);
		return null;
	}

	public ActionForward importLimitsFromCsv(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws Exception {

		FlashScope flash = FlashScope.getScope(req);
		String referer = req.getHeader("Referer");
		String orgId = (String)req.getParameter("org_id");
		String userName = (String)req.getSession(false).getAttribute("userid");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		DynaPkgUploadForm uploadForm = (DynaPkgUploadForm) f;
		InputStreamReader isReader = new InputStreamReader(uploadForm.getCsvLimitsFile().getInputStream());

		StringBuilder infoMsg = new StringBuilder();
		String error = getLimitsCsvHandler().importTable(isReader, infoMsg);

		cdao.updateChargesForDerivedRatePlans(orgId, userName, "dynaPackCategoryLimits",true);

		if (error != null) {
			flash.put("error", error);
			return redirect;
		}

		flash.put("info", infoMsg.toString());
		return redirect;

	}
	
	private boolean saveOrUpdateItemSubGroup(int dynapackId, Connection con,HttpServletRequest request)
			throws SQLException, IOException{
			Map params = request.getParameterMap();
			List errors = new ArrayList();
			
			boolean flag = true;
			String [] itemSubgroupId = request.getParameterValues("item_subgroup_id");
			String [] delete = request.getParameterValues("deleted");
			
			if (errors.isEmpty()) {
				if(itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")){
					GenericDAO itemsubgroupdao = new GenericDAO("dyna_package_item_sub_groups");
					BasicDynaBean itemsubgroupbean = itemsubgroupdao.getBean();
					ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
					List records = itemsubgroupdao.findAllByKey("dyna_package_id", dynapackId);
					if (records.size() > 0)
						flag = itemsubgroupdao.delete(con, "dyna_package_id", dynapackId);
					
					for(int i=0; i<itemSubgroupId.length; i++){
						if(itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
							if (delete[i].equalsIgnoreCase("false")) {
								itemsubgroupbean.set("dyna_package_id", dynapackId);
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
	 * Schedule dynaPackage creation.
	 *
	 * @param dynaPackageId   dynaPackageId
	 * @param dynaPackageName dynaPackageName
	 * @param processKey tells to update/insert
	 * @param derivedRatePlanIds derivedRatePlanIds
	 * @param ratePlanapplicable ratePlanapplicable
	 * @param orgDetails orgDetails
	 * @param categoryIds categoryIds
	 * @param userName 
	 * 
	 */
	private void scheduleDynaPackageCreation(int dynaPackageId, String dynaPackageName, String processKey
			,String[] derivedRatePlanIds,String[] ratePlanapplicable,BasicDynaBean orgDetails, String[] categoryIds, String userName) {
		Map<String, Object> jobData = new HashMap<>();

		jobData.put("dynaPackageId", dynaPackageId);
		jobData.put("dynaPackageName", dynaPackageName);
		jobData.put("processKey", processKey);
		jobData.put("derivedRatePlanIds", derivedRatePlanIds);
		jobData.put("ratePlanapplicable", ratePlanapplicable);
		jobData.put("orgDetails", orgDetails);
		jobData.put("categoryIds", categoryIds);
		jobData.put("userName", userName);
		jobData.put("schema", RequestContext.getSchema());
		jobData.put("center_id", RequestContext.getCenterId());

		dynaPackageService.scheduleDynaPackageCreation(jobData);
	}

}

