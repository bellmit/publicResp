package com.insta.hms.stores;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.HsSfWorkbookUtils;
import com.insta.hms.common.AbstractDataHandlerAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ExcelImporter;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.UploadForm;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.csvutils.TableDataHandler;
import com.insta.hms.master.CityMaster.CityMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.StateMaster.StateMasterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SupplierMasterAction extends AbstractDataHandlerAction {

	static Logger logger = LoggerFactory.getLogger(SupplierMasterAction.class);

	static SupplierMasterDAO dao = new SupplierMasterDAO();

	@IgnoreConfidentialFilters
	public ActionForward getSupplierDashBoard(ActionMapping mapping,ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		JSONSerializer js = new JSONSerializer().exclude("class");
		Map map = getParameterMap(request);
		PagedList list = SupplierMasterDAO.searchSuppliers(map,ConversionUtils.getListingParameter(map));
		int maxCenters = Integer.parseInt(new GenericPreferencesDAO().getAllPrefs().get("max_centers_inc_default").toString());
		request.setAttribute("pagedList", list);
		request.setAttribute("maxCenters", maxCenters);
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			request.setAttribute("suppliers", js.serialize(ConversionUtils.copyListDynaBeansToMap(new StoreDAO(con).getSupplierNames())));
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return mapping.findForward("supplierdashboard");
	}

	@IgnoreConfidentialFilters
	public ActionForward getSupplierDetailsScreen(ActionMapping mapping,ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			SQLException {
		JSONSerializer js = new JSONSerializer().exclude("class");
		String supplierId = request.getParameter("supplier_id");
		if (supplierId != null) {
			BasicDynaBean suppdto = SupplierMasterDAO.getSelectedSuppDetails(supplierId);
			request.setAttribute("fromDB", "Y");
			request.setAttribute("suppdto", suppdto);
			request.setAttribute("suppliersLists",js.serialize(SupplierMasterDAO.getSuppliersNamesAndIds()));

		}
		ArrayList<String> Supplier = SupplierMasterDAO.getSupplierNamesInMaster();
		request.setAttribute("cityStateCountryList", js.serialize(CityMasterDAO.getPatientCityStateCountryList(true)));
		request.setAttribute("stateList", js.serialize(StateMasterDAO.getStateIdName()));
		request.setAttribute("stateCountryList", js.serialize(StateMasterDAO.getStateCountryList()));
		request.setAttribute("supp", Supplier);
		request.setAttribute("genPrefs", GenericPreferencesDAO.getPrefsBean().getMap());
		return mapping.findForward("supplierdetails");
	}

	public ActionForward saveSupplierDetails(ActionMapping mapping,ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, Exception {
		FlashScope flash = FlashScope.getScope(request);
		BasicDynaBean bean = null;
		ActionRedirect redirect = new ActionRedirect("suppdetails.do");
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("_method", "getSupplierDetailsScreen");
		String operation = request.getParameter("operation");
		String pharmacy = request.getParameter("pharmacy");
		String inventory = request.getParameter("inventory");
		String supplier_code = request.getParameter("supplier_code");
		String supplier_name = request.getParameter("supplier_name");
		String supplier = request.getParameter("supplier");
		Map params = request.getParameterMap();
		List errors = new ArrayList();
		SupplierCenterDAO sdao = new SupplierCenterDAO();
		boolean flag = true;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			bean = dao.getBean();
			boolean isCodeExist = false; 
			String supCustCode = (null == request.getParameter("cust_supplier_code") || "".equalsIgnoreCase(request.getParameter("cust_supplier_code").trim()))? null : request.getParameter("cust_supplier_code");
			BasicDynaBean supCustCodeExist = null;
			if (operation.equalsIgnoreCase("insert")) {
				//bean = dao.getBean();
				if (dao.exist("supplier_name", supplier_name)) {
					flash.put("error", "Duplicate Supplier Name: "+ supplier_name + " already exists");
					flag = false;
				} else {
					ConversionUtils.copyToDynaBean(params, bean, errors);
					supplier_code = AutoIncrementId.getNewIncrUniqueId("SUPPLIER_CODE","SUPPLIER_MASTER", "Supplier Code");
					bean.set("supplier_code", supplier_code);
				}
				
    			if(null != supCustCode && !supCustCode.trim().isEmpty()){
    				supCustCodeExist = dao.findByKey("cust_supplier_code", supCustCode);
	    			if(supCustCodeExist == null){
	    				bean.set("cust_supplier_code", supCustCode);
	    			}else{
	    				isCodeExist = true;
	    			}
    			}else{
    				bean.set("cust_supplier_code", supCustCode);
    			}
    			if(!isCodeExist){
    				flag = dao.insert(con, bean);
    			}else{
					flag = false;
					flash.error("Duplicate supplier code : "+ supCustCode +" already exists.");
				}
				//flag = dao.insert(con, bean);
				if (flag) {
					BasicDynaBean sBean = sdao.getBean();
					sBean.set("supp_center_id", sdao.getNextSequence());
					sBean.set("center_id", 0);
					sBean.set("supplier_code", supplier_code);
					sBean.set("status", "A");

					flag &= sdao.insert(con, sBean);
				}
			} else {
				//bean = dao.getBean();
				ConversionUtils.copyToDynaBean(params, bean, errors);
				if (!supplier.equals(supplier_name)) {
					if (dao.exist("supplier_name", supplier_name)) {
						flash.put("error", "Duplicate Supplier Name: "+ supplier_name + " already exists");
						flag = false;
					}
				}
				if("".equals(bean.get("cust_supplier_code"))){
					bean.set("cust_supplier_code", supCustCode);
				}
				if(null != supCustCode && !supCustCode.trim().isEmpty()){
    				supCustCodeExist = dao.findByKey("cust_supplier_code", supCustCode);
	    			if(supCustCodeExist == null){
	    				bean.set("cust_supplier_code", supCustCode);
	    			}else{
	    				isCodeExist = true;
	    			}
    			}
				
				if (flag)
					if(!isCodeExist){
						flag = dao.update(con, bean.getMap(), "supplier_code",supplier_code) > 0;
					}else{
						flag = false;
					}
			}
			if (flag) {
				con.commit();
				if (operation.equalsIgnoreCase("insert"))
					flash.put("success", "Supplier :" + supplier_name + " Details are Successfully Inserted");
				else
					flash.put("success", "Supplier :" + supplier_name + " Details are Successfully Updated");
			}
			else{
				if(!isCodeExist){
					con.rollback();
					flash.error("Failed to update the supplier details..");
				}else{
					con.rollback();
					flash.error("Duplicate supplier code "+ supCustCode +" already exists.");
				}
			}
		} catch (Exception e) {
			con.rollback();
			flash.put("error", "Transaction Failure");
		} finally {
			DataBaseUtil.closeConnections(con, null);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			redirect.addParameter("supplier_id", bean.get("supplier_code"));
		}
		return redirect;
	}

	private static TableDataHandler supplierMasterHandler = null;

	@Override
	protected TableDataHandler getDataHandler() {
		if (supplierMasterHandler == null) {
			supplierMasterHandler = new TableDataHandler(
					"supplier_master", // table name
					new String[] { "supplier_code" }, // keys
					new String[] { "supplier_name", "status", "credit_period",
							"supp_category_id", "supplier_address",
							"supplier_city", "supplier_state",
							"supplier_country", "supplier_pin",
							"supplier_phone1", "supplier_phone2",
							"supplier_fax", "supplier_mailid",
							"supplier_website", "contact_person_name",
							"contact_person_mobile_number",
							"contact_person_mailid", "tin/gstin_number", "drug_license_no", "pan_no", "cin_no" },
					new String[][] {
					// our field ref table ref table id field ref table name
					// field
					{ "supp_category_id", "supplier_category_master",
							"supp_category_id", "supp_category_name" }, }, null);
		}

		supplierMasterHandler.setAutoIncrName("Supplier Code");
		return supplierMasterHandler;
	}

	@Override
	public ActionForward exportMaster(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		XSSFWorkbook workbook = new XSSFWorkbook();
		
		// Sheet for supplier_master table
		List<String> suppliersDetailsColumnNames = null;
		suppliersDetailsColumnNames = Arrays.asList(new String[] {
				"supplier_code", "supplier_name", "cust_supplier_code", "status", "supplier_address",
				"supplier_city", "supplier_state", "supplier_country",
				"supplier_pin", "supplier_phone1", "supplier_phone2",
				"supplier_fax", "supplier_mailid", "supplier_website",
				"contact_person_name", "contact_person_mobile_number",
				"contact_person_mailid", "tin/gstin_number", "credit_period", "supp_category_name","is_registered",
				"drug_license_no", "pan_no", "cin_no", "tcs_applicable" });

		XSSFSheet supplierDetailsWorkSheet = workbook.createSheet("supplier_master");
		List<BasicDynaBean> supplierDetails = SupplierMasterDAO.getSupplierDetail();
		Map<String, List> columnNamesMap = new HashMap<String, List>();
		columnNamesMap.put("mainItems", suppliersDetailsColumnNames);
		HsSfWorkbookUtils.createPhysicalCellsWithValues(supplierDetails,columnNamesMap, supplierDetailsWorkSheet, false);

		// sheet for center supplier
		List<String> centerSupplierColumnNames = null;
		centerSupplierColumnNames = Arrays.asList(new String[] { "center_id",
				"supplier_code", "city_id", "state_id", "supp_center_id", 
				"center_name", "supplier_name", "status", "city_name", "state_name" });

		XSSFSheet centerSuppliersworksheet = workbook.createSheet("SUPPLIER_CENTER");
		centerSuppliersworksheet.setColumnHidden(0, true);
		centerSuppliersworksheet.setColumnHidden(1, true);
		centerSuppliersworksheet.setColumnHidden(2, true);
		centerSuppliersworksheet.setColumnHidden(3, true);
		centerSuppliersworksheet.setColumnHidden(4, true);

		List<BasicDynaBean> centerSupplierDetails = SupplierCenterDAO.getSupplierCenterDetail();
		Map<String, List> columnNamesMap2 = new HashMap<String, List>();
		columnNamesMap2.put("mainItems", centerSupplierColumnNames);
		HsSfWorkbookUtils.createPhysicalCellsWithValues(centerSupplierDetails,columnNamesMap2, centerSuppliersworksheet, true);

		res.setHeader("Content-type", "application/vnd.ms-excel");
		res.setHeader("Content-disposition","attachment; filename=supplier_master.xls");
		res.setHeader("Readonly", "true");
		java.io.OutputStream os = res.getOutputStream();
		workbook.write(os);
		os.flush();
		os.close();

		return null;

	}


	@Override
	public ActionForward importMaster(ActionMapping mapping, ActionForm f,HttpServletRequest req, HttpServletResponse response)
			throws java.io.IOException, SQLException {
	  StringBuilder errors;
		UploadForm uploadForm = (UploadForm) f;
		String fileName = uploadForm.getUploadFile().getFileName();
		String referer = req.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		FlashScope flash = FlashScope.getScope(req);

		if (fileName.contains(".")) {
			String fileExtenstion = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
			if (!(fileExtenstion.equalsIgnoreCase("xls") || fileExtenstion.equalsIgnoreCase("xlsx"))) {
				flash.put("error", "only xls files are allowed to Upload");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
		} else {
			flash.put("error", "only xls files are allowed to Upload");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}
		
		try {
			ByteArrayInputStream byteStream = new ByteArrayInputStream(uploadForm.getUploadFile().getFileData());
			String SheetName = "";
			errors = new StringBuilder();
			XSSFWorkbook workBook = new XSSFWorkbook(byteStream);
			if (workBook.getNumberOfSheets() > 0)
				SheetName = workBook.getSheetName(0);
			
			// SupplierMaster
			XSSFSheet supplierMasterSheet = null;
			String supplierMasterSheetName = null;
			if (SheetName.equalsIgnoreCase("SUPPLIER_MASTER")) {
				supplierMasterSheet = workBook.getSheetAt(0);
				supplierMasterSheetName = workBook.getSheetName(0);
				supplierMasterUpload(supplierMasterSheet, errors);
			} else {
				if (workBook.getNumberOfSheets() > 1) {
					supplierMasterSheet = workBook.getSheetAt(1);
					supplierMasterSheetName = workBook.getSheetName(1);
					supplierMasterUpload(supplierMasterSheet, errors);
				}
			}

			// CenterSupplier
			XSSFSheet supplierCenterSheet = null;
			String supplierCenterSheetName = null;
			if (SheetName.equalsIgnoreCase("SUPPLIER_CENTER")) {
				supplierCenterSheet = workBook.getSheetAt(0);
				supplierCenterSheetName = workBook.getSheetName(0);
				supplierCenterMasterUpload(supplierCenterSheet, errors);
			} else {
				if (workBook.getNumberOfSheets() > 1) {
					supplierCenterSheet = workBook.getSheetAt(1);
					supplierCenterSheetName = workBook.getSheetName(1);
					supplierCenterMasterUpload(supplierCenterSheet, errors);
				}
			}

			SupplierCenterDAO dao = new SupplierCenterDAO();
			dao.deleteDefaultCenter();
			dao.insertDefaultCenter();

			if (errors.length() > 0)
				flash.put("error", errors);
			else
				flash.put("info", "File successfully uploaded");
		} catch (Exception e) {
			flash.put("error", "Unabled to upload file.");
		}
		// only xls files are allowed to Upload
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;

	}
	
	private void supplierMasterUpload(XSSFSheet supplierMasterSheet,StringBuilder errors)
			throws SQLException, IOException {
		LinkedHashMap aliasMap = new LinkedHashMap();
		aliasMap.put("supplier_code", "supplier_code");
		aliasMap.put("supplier_name", "supplier_name");
		aliasMap.put("cust_supplier_code", "cust_supplier_code");
		aliasMap.put("status", "status");
		aliasMap.put("supplier_address", "supplier_address");
		aliasMap.put("supplier_city", "supplier_city");
		aliasMap.put("supplier_state", "supplier_state");
		aliasMap.put("supplier_country", "supplier_country");
		aliasMap.put("supplier_pin", "supplier_pin");
		aliasMap.put("supplier_phone1", "supplier_phone1");
		aliasMap.put("supplier_phone2", "supplier_phone2");
		aliasMap.put("supplier_fax", "supplier_fax");
		aliasMap.put("supplier_mailid", "supplier_mailid");
		aliasMap.put("supplier_website", "supplier_website");
		aliasMap.put("contact_person_name", "contact_person_name");
		aliasMap.put("contact_person_mobile_number", "contact_person_mobile_number");
		aliasMap.put("contact_person_mailid", "contact_person_mailid");
		aliasMap.put("tin/gstin_number", "supplier_tin_no");
		aliasMap.put("credit_period", "credit_period");
		aliasMap.put("supp_category_name","supp_category_name");
		aliasMap.put("is_registered","is_registered");
		aliasMap.put("drug_license_no","drug_license_no");
		aliasMap.put("pan_no","pan_no");
		aliasMap.put("cin_no","cin_no");
		aliasMap.put("tcs_applicable","tcs_applicable");

		List mandatoryFieldsList = Arrays.asList("supplier_name");
		List skipFieldsList = new ArrayList();
		
		String suppCategoryCheckFieldArray[] = { "supp_category_id",// field needed in main master table
				"supp_category_id",// field in checking table
				"supp_category_name",// Field uploaded in main master
				"supplier_category_master" // Checking table name
		};
		
		//If inserting value in two columns then add  '@'   ex= "columnname1@clumnname2"
		//If want to check casesensitive/caseinsensitive then add sensitive/insensitive ex= "columnname1@clumnname2@sensitive"
		String suppCityCheckFieldArray[] = { "city_id@supplier_city@insensitive",// field needed in main master table
                "city_id",// field in checking table
                "city_name",// Field uploaded in main master
                "city" // Checking table name
        };
		
		//If inserting value in two columns then add  '@'   ex= "columnname1@clumnname2"
	    //If want to check casesensitive/caseinsensitive then add sensitive/insensitive ex= "columnname1@clumnname2@sensitive"
        String suppStateCheckFieldArray[] = { "state_id@supplier_state@sensitive",// field needed in main master table
                "state_id",// field in checking table
                "state_name",// Field uploaded in main master
                "state_master" // Checking table name
        };
        
        LinkedHashMap columnValueCheckerMap = new LinkedHashMap();
		columnValueCheckerMap.put("supp_category_name",suppCategoryCheckFieldArray);
		columnValueCheckerMap.put("supplier_city",suppCityCheckFieldArray);
		columnValueCheckerMap.put("supplier_state",suppStateCheckFieldArray);

		//If table doesn't have any sequence and we are generating the sequence based on combination of fields 
		//then we will form the unique key using the following information
		//Ex: we are using getNewIncrUniqueId to get unique key so this method expect the column name and column type as input
		// so we are passing this information in custom_unique_key@column type@column name combination.
		ExcelImporter exImporter = new ExcelImporter("supplier_master", "custom_unique_key@Supplier Code@supplier_code");
		exImporter.setId("supplier_code");
		exImporter.setNoOfRowsSkip(0);
		exImporter.setInsertEnable(true);
		exImporter.uploadExcelToDB(supplierMasterSheet, aliasMap,columnValueCheckerMap,
				mandatoryFieldsList, errors, null,skipFieldsList);

	}

	private void supplierCenterMasterUpload(XSSFSheet supplierCenterSheet,StringBuilder errors)
			throws SQLException, IOException {

		LinkedHashMap aliasMap = new LinkedHashMap();
		aliasMap.clear();
		aliasMap.put("center_id", "center_id");
		aliasMap.put("center_name", "center_name");
		aliasMap.put("supplier_code", "supplier_code");
		aliasMap.put("supplier_name", "supplier_name");
		aliasMap.put("city_id", "city_id");
		aliasMap.put("state_id", "state_id");
		aliasMap.put("status", "status");
		aliasMap.put("supp_center_id", "supp_center_id");

		List centerSupplierMandatoryFieldsList = Arrays.asList("center_name", "supplier_name");
		List centerSupplierSkipFieldsList = new ArrayList();
		centerSupplierSkipFieldsList.add("city_name");
		centerSupplierSkipFieldsList.add("state_name");

		String centerNameCheckFieldArray[] = { "center_id",// field needed in main master table
				"center_id",// field in checking table
				"center_name",// Field uploaded in main master
				"hospital_center_master" // Checking table name
		};

		String supplierCodeCheckFieldArray[] = { "supplier_code",// field needed in main master table
				"supplier_code",// field in checking table
				"supplier_code",// Field uploaded in main master
				"supplier_master" // Checking table name
		};
		String supplierNameCheckFieldArray[] = { "supplier_code",// field needed in main master table
				"supplier_code",// field in checking table
				"supplier_name",// Field uploaded in main master
				"supplier_master" // Checking table name
		};
		String supplierCityIdCheckFieldArray[] = { "center_id",// field needed in main master table
				"center_id",// field in checking table
				"city_id",// Field uploaded in main master
				"hospital_center_master" // Checking table name
		};
		String stateIdCheckFieldArray[] = { "center_id",// field needed in main master table
				"center_id",// field in checking table
				"state_id",// Field uploaded in main master
				"hospital_center_master" // Checking table name
		};

		LinkedHashMap centerSupplierColumnValueCheckerMap = new LinkedHashMap();
		centerSupplierColumnValueCheckerMap.put("center_name",centerNameCheckFieldArray);
		centerSupplierColumnValueCheckerMap.put("supplier_code",supplierCodeCheckFieldArray);
		centerSupplierColumnValueCheckerMap.put("supplier_name",supplierNameCheckFieldArray);
		centerSupplierColumnValueCheckerMap.put("city_id",supplierCityIdCheckFieldArray);
		centerSupplierColumnValueCheckerMap.put("state_id",stateIdCheckFieldArray);
		
		ExcelImporter centerSupplierExImporter = new ExcelImporter("supplier_center_master", "supplier_center_master_seq");
		centerSupplierExImporter.setId("supp_center_id");
		centerSupplierExImporter.setNoOfRowsSkip(0);
		centerSupplierExImporter.setInsertEnable(true);
		centerSupplierExImporter.uploadExcelToDB(supplierCenterSheet, aliasMap,centerSupplierColumnValueCheckerMap,
				centerSupplierMandatoryFieldsList, errors, null,centerSupplierSkipFieldsList);

	}

}