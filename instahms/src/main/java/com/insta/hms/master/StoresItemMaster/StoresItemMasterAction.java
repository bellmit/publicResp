package com.insta.hms.master.StoresItemMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.HsSfWorkbookUtils;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ExcelImporter;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.UploadForm;
import com.insta.hms.csvutils.TableDataHandler;
import com.insta.hms.integration.scm.inventory.ScmItemMasterOutBoundService;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.MedicineRoute.MedicineRouteDAO;
import com.insta.hms.master.PackageUOM.PackageUOMDAO;
import com.insta.hms.master.StoreItemRates.StoreItemRatesDAO;
import com.insta.hms.master.StoresRatePlanMaster.StoresRatePlanDAO;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupRepository;
import com.insta.hms.modules.ModulesDAO;
import com.insta.hms.stores.DirectStockEntryDAO;
import com.insta.hms.stores.PharmacymasterDAO;
import com.insta.hms.stores.StockEntryDAO;
import com.insta.hms.stores.StoreDAO;
import com.insta.hms.stores.StoreItemCodesDAO;
import com.insta.hms.stores.StoresDBTablesUtil;
import com.insta.hms.stores.SupplierCenterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
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
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import flexjson.JSONSerializer;

public class StoresItemMasterAction extends BaseAction{

	private static final GenericDAO itemGroupTypeDao = new GenericDAO("item_group_type");
  static Logger logger = LoggerFactory.getLogger(StoresItemMasterAction.class);
	private static MedicineRouteDAO mrDao = new MedicineRouteDAO();
	private static StoresItemDAO stDAO = new StoresItemDAO();
	private static StoresRatePlanDAO strateDAO = new StoresRatePlanDAO();
	private static GenericPreferencesDAO genPref = new GenericPreferencesDAO() ;
	private static PackageUOMDAO uomDAO = new PackageUOMDAO();
  private static StoreItemRatesDAO itemRatesDAO = new StoreItemRatesDAO();
  private static ModulesDAO modulesDao = new ModulesDAO();
  private static ScmItemMasterOutBoundService itemScmService = ApplicationContextProvider
      .getBean(ScmItemMasterOutBoundService.class);

	public ActionForward list(ActionMapping m,ActionForm f,HttpServletRequest req,
			HttpServletResponse res) throws IOException ,SQLException, Exception{
		Map map= getParameterMap(req);
		PagedList list = PharmacymasterDAO.searchMedicine(map, ConversionUtils.getListingParameter(map));
		req.setAttribute("pagedList", list);
		req.setAttribute("noOfStoreRatePlans", strateDAO.listAll().size());
		req.setAttribute("codeTypes", StoreItemCodesDAO.getItemCodeTypes("Drug"));
		req.setAttribute("medicineNamesListJSON", StoresDBTablesUtil.getNamesInJSON(StoresDBTablesUtil.GET_MEDICINE_NAMES_IN_MASTER));
		req.setAttribute("medicineListJSON", StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GET_MEDICINE_NAMES_IN_MASTER));
		req.setAttribute("manfacturers", StoresDBTablesUtil.getNamesInJSON(StoresDBTablesUtil.GET_MANFNAMES_IN_MASTER));
		return m.findForward("list");
	}
	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		Map<String, Object> filterMap = new HashMap<String, Object>();
		filterMap.put("status", "A");
		List list = PharmacymasterDAO.getCategoryIdentifiactions();
		GenericDAO gDAO = new GenericDAO("manf_master");
		req.setAttribute("manfList", js.serialize(ConversionUtils.listBeanToListMap(gDAO.listAll())));
		req.setAttribute("identList", js.serialize(list));
		req.setAttribute("medicineRouteList", mrDao.listAll(null, "status", "A"));
		GenericDAO defaultControlType = new GenericDAO("store_item_controltype");
		String control_type_id = defaultControlType.findByKey("control_type_name", "Normal").get("control_type_id").toString();
		req.setAttribute("defaultControlTypeID", control_type_id);
		req.setAttribute("packageUOMList", js.serialize(ConversionUtils.listBeanToListMap(uomDAO.listAll("package_uom"))));
		req.setAttribute("issueUOMs", js.serialize(ConversionUtils.listBeanToListMap(PackageUOMDAO.getAllIssueUOMs())));
		req.setAttribute("defPkgUOM", GenericPreferencesDAO.getGenericPreferences().getPackageUOM());
		req.setAttribute("defPkgSize", GenericPreferencesDAO.getGenericPreferences().getPackageSize());
		req.setAttribute("defIssUOM", GenericPreferencesDAO.getGenericPreferences().getIssueUOM());
		req.setAttribute("packageUOMListWithoutJson", ConversionUtils.listBeanToListMap(
				uomDAO.listAll("package_uom")));
		req.setAttribute("isuuePackageList", js.serialize(ConversionUtils.listBeanToListMap(
				uomDAO.listAll("package_uom"))));
		req.setAttribute("issueUOMListWithoutJson", ConversionUtils.listBeanToListMap(PackageUOMDAO.getAllIssueUOMs()));
		req.setAttribute("healthAuthorities", new GenericDAO("health_authority_master").listAll());
		req.setAttribute("healthAuthSpecificCodeTypesJson", js.serialize(ConversionUtils.listBeanToListMap(HealthAuthorityPreferencesDAO.getItemCodeTypesByHealthAuthority("Drug"))));
		req.setAttribute("max_centers_inc_default", genPref.getAllPrefs().get("max_centers_inc_default").toString());
		req.setAttribute("centerList", new GenericDAO("hospital_center_master").listAll(null,filterMap,"center_name"));
		filterMap.put("item_group_type_id", "TAX");
		req.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(itemGroupTypeDao.listAll(null, filterMap, null)));
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
		req.setAttribute("last_updated_item",stDAO.getLatestItemUpdated());
		return m.findForward("addshow");
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		Map<String, Object> filterMap = new HashMap<String, Object>();
		int maxCenters = Integer.parseInt(genPref.getAllPrefs().get("max_centers_inc_default").toString());

		filterMap.put("status", "A");
		BasicDynaBean bean = PharmacymasterDAO.getSelectedMedicineDetails(req.getParameter("medicine_id"));
		req.setAttribute("bean", bean);
		req.setAttribute("storeItemsLists", js.serialize(PharmacymasterDAO.getStoreItemsNamesAndIds()));
		req.setAttribute("medicineRouteList", mrDao.listAll(null, "status", "A"));

		List storeMedList = StoreDAO.getStoreWiseMed((req.getParameter("medicine_id")));
		req.setAttribute("swItemList", storeMedList);

		List list = PharmacymasterDAO.getCategoryIdentifiactions();
		req.setAttribute("identList", js.serialize(list));

		GenericDAO gDAO = new GenericDAO("manf_master");
		Integer medicineId = Integer.parseInt(req.getParameter("medicine_id"));
		List<BasicDynaBean> activeInsurance = StoreDAO.getActiveInsuranceCategories(medicineId);
		StringBuilder activeInsuranceCategories = new StringBuilder();
		for (BasicDynaBean activeInsuranceCategory : activeInsurance) {
			activeInsuranceCategories.append(activeInsuranceCategory.get("insurance_category_id"));
			activeInsuranceCategories.append(",");
		}
		req.setAttribute("insurance_categories", activeInsuranceCategories.toString());
		req.setAttribute("manfList", js.serialize(ConversionUtils.listBeanToListMap(gDAO.listAll())));
		req.setAttribute("issueUOMListWithoutJson", ConversionUtils.listBeanToListMap(PackageUOMDAO.getAllIssueUOMs()));
		req.setAttribute("packageUOMListWithoutJson", ConversionUtils.listBeanToListMap(uomDAO.listAll("package_uom")));
		req.setAttribute("isuuePackageList", js.serialize(ConversionUtils.listBeanToListMap(
				uomDAO.listAll("package_uom"))));

		req.setAttribute("packageUOMList", js.serialize(ConversionUtils.listBeanToListMap(uomDAO.listAll("package_uom"))));
		req.setAttribute("issueUOMs", js.serialize(ConversionUtils.listBeanToListMap(PackageUOMDAO.getAllIssueUOMs())));
		req.setAttribute("storeRatePlans", strateDAO.listAll(null,"status","A",null));
		req.setAttribute("healthAuthorityCodeTypes", new GenericDAO("ha_item_code_type").listAll(null, "medicine_id", Integer.parseInt(req.getParameter("medicine_id")),"code_type"));
		req.setAttribute("healthAuthorities", new GenericDAO("health_authority_master").listAll());
		req.setAttribute("healthAuthSpecificCodeTypesJson", js.serialize(ConversionUtils.listBeanToListMap(HealthAuthorityPreferencesDAO.getItemCodeTypesByHealthAuthority("Drug"))));
		req.setAttribute("centerList", new GenericDAO("hospital_center_master").listAll(null,filterMap,"center_name"));
		req.setAttribute("supplierCenters", ConversionUtils.copyListDynaBeansToMap(new SupplierCenterDAO().getCenterSupplierDetails(Integer.parseInt(req.getParameter("medicine_id")))));
		if(maxCenters == 1) {
			List <BasicDynaBean> supplist = new SupplierCenterDAO().getCenterSupplierDetails(Integer.parseInt(req.getParameter("medicine_id")));
			if (supplist.size() > 0) {
				req.setAttribute("supplierId", supplist.get(0).get("supplier_code"));
			}
		}
		req.setAttribute("max_centers_inc_default", genPref.getAllPrefs().get("max_centers_inc_default").toString());
		req.setAttribute("taxsubgroup", ConversionUtils.copyListDynaBeansToMap(stDAO.getStoreItemSubGroupDetails(Integer.parseInt(req.getParameter("medicine_id")))));
		req.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(itemGroupTypeDao.findAllByKey("item_group_type_id","TAX")));
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
		req.setAttribute("last_updated_item",stDAO.getLatestItemUpdated());
		return m.findForward("addshow");
	}


	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = null;
		String[] healthAuth = req.getParameterValues("health_authority");
		String[] codeTypes = req.getParameterValues("code_type");
		GenericDAO haCodeDao = new GenericDAO("ha_item_code_type");
		int centerId = (Integer) req.getSession(false).getAttribute("centerId");
		BasicDynaBean itemCodeExist = null;
		BasicDynaBean genericPreferences = GenericPreferencesDAO.getAllPrefs();
		boolean success = true;
    BasicDynaBean module = modulesDao.findByKey("module_id", "mod_scm");

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			PharmacymasterDAO masterDAO = new PharmacymasterDAO(con);

			String barCodePref = GenericPreferencesDAO.getGenericPreferences().getBarcodeForItem();
			String customBarcode = req.getParameter("item_barcode_id");
			BasicDynaBean bean = masterDAO.getBean();
			bean.set("created_timestamp",DateUtil.getCurrentTimestamp());
			ConversionUtils.copyToDynaBean(params, bean, errors);
			if (bean.get("issue_units") == null || bean.get("issue_units").equals("")) {
				bean.set("issue_units", req.getParameter("issue_units_hidden"));
			}
			if (bean.get("package_uom") == null || bean.get("package_uom").equals("")) {
				bean.set("package_uom", req.getParameter("package_uom_hidden"));
			}
			String[] medicineRoutesList = req.getParameterValues("route_of_admin");
			String commaSepListOfRoutes = "";
			boolean isfirst = true;
			if (medicineRoutesList != null) {
				for (int i=0; i<medicineRoutesList.length; i++) {
					if (isfirst) {
						commaSepListOfRoutes += medicineRoutesList[i];
						isfirst = false;
					} else {
						commaSepListOfRoutes += ","+ medicineRoutesList[i];
					}
				}
			}
			bean.set("route_of_admin", commaSepListOfRoutes);
			if (bean.get("item_strength") == null || bean.get("item_strength").equals(""))
				bean.set("item_strength_units", null);

			FlashScope flash = FlashScope.getScope(req);

			if (errors.isEmpty()) {
				BasicDynaBean exists = masterDAO.findByKey("medicine_name", bean.get("medicine_name"));
				BasicDynaBean barcodeExists = masterDAO.findByKey("item_barcode_id", customBarcode);
					if (exists == null) {
						if(barcodeExists == null) {
							bean.set("medicine_id", PharmacymasterDAO.getNextValFromSequence());
							String manfId = DirectStockEntryDAO.manfNameToId(req.getParameter("manf_name"));
				    		if (manfId == null || manfId.equals("")) {
				    			// Bug ID : 7505 : naren

				    			if(req.getParameter("manf_name").length() >= 4 )
				    			{
				    			String manfCode = req.getParameter("manf_name").substring(0, 4);
				    			manfId = masterDAO.insertManfMaster(req.getParameter("manf_name"),manfCode);
				    			bean.set("manf_name", manfId);
				    			}
				    			else
				    			{
				    				String manfCode = req.getParameter("manf_name").substring(0, req.getParameter("manf_name").length());
					    			manfId = masterDAO.insertManfMaster(req.getParameter("manf_name"),manfCode);
					    			bean.set("manf_name", manfId);
				    			}
				    		}else {
				    			bean.set("manf_name", manfId);
				    		}
							String genericId = PharmacymasterDAO.getGenNameToId(con,(String)bean.get("generic_name"));
				    		bean.set("generic_name", genericId);
				    		if (barCodePref.equalsIgnoreCase("Y")) {
								if (req.getParameter("itembarcodechk")!= null) bean.set("item_barcode_id", StockEntryDAO.getBarCode());
								else bean.set("item_barcode_id", req.getParameter("item_barcode_id").equals("") ? null : req.getParameter("item_barcode_id"));
							}
				    		int medCategoryId =  (Integer) bean.get("med_category_id");
				    		boolean isCodeExist = false;
				    		String itmCode = "";
				    		bean.set("cust_item_code", null);
				    		
				    		if( req.getParameter("cust_item_code_chk")!= null ||
                                "Y".equals(genericPreferences.get("force_generate_cust_item_code"))){
				    			itmCode =  StockEntryDAO.getNextItemNo(medCategoryId,centerId);
				    			itemCodeExist = masterDAO.findByKey("cust_item_code", itmCode);
				    			if(itemCodeExist == null){
				    				bean.set("cust_item_code", itmCode);
				    			}else{
				    				isCodeExist = true;
				    			}

				    		}else{
				    			itmCode = req.getParameter("cust_item_code").equals("") ? null : req.getParameter("cust_item_code");
				    			if(null != itmCode){
				    				itemCodeExist = masterDAO.findByKey("cust_item_code", itmCode);
					    			if(itemCodeExist == null){
					    				bean.set("cust_item_code", itmCode);
					    			}else{
					    				isCodeExist = true;
					    			}
                                  }
				    		}

				    		if(!isCodeExist){
				    			if (bean.get("consumption_capacity") == null)
					    			bean.set("consumption_capacity", new BigDecimal(1)); // setting the default value
				    			success = masterDAO.insert(con, bean);

								//inserting charges for all store rate plan for the new item

								List<BasicDynaBean> storeItemRatePlans = strateDAO.listAll();//list all of store rate plans
//								BasicDynaBean storeItemRates = itemRatesDAO.getBean();

								for ( BasicDynaBean storeRatePlan : storeItemRatePlans ) {
									BasicDynaBean storeItemRates = itemRatesDAO.getBean();

									storeItemRates.set("store_rate_plan_id", storeRatePlan.get("store_rate_plan_id"));
									storeItemRates.set("medicine_id", bean.get("medicine_id"));
									storeItemRates.set("selling_price", BigDecimal.ZERO);

									success &= itemRatesDAO.insert(con, storeItemRates);

								}

								if (success) {
									success = false;
									int item_id = (Integer) bean.get("medicine_id");
									success = saveOrUpdateStockLevel(params ,item_id, con,req);
									if (!success)
										flash.error("Fail to update stock details....");
								}

								if(success) {
									int item_id = (Integer) bean.get("medicine_id");
									success = saveOrUpdateHealthAuthorityCodeTypes(item_id,con,req);
								}

								if(success) {
									int item_id = (Integer) bean.get("medicine_id");
									success = saveOrUpdateItemSubGroup(item_id,con,req);
								}

              if (success) {
                int itemId = (Integer) bean.get("medicine_id");
                success = saveItemSubGroupForStoreTariff(itemId, con, req);
              }

								if(success) {
									Integer medicineId = Integer.parseInt(bean.get("medicine_id").toString());
									success = saveOrUpdateInsuranceCategory(medicineId,con,req);
								}

								if(success) {
									int item_id = (Integer) bean.get("medicine_id");
									success = saveOrUpdateSupplierCenterTypes(item_id,con,req);
								}

								if (success) {
										con.commit();
										if (module != null &&
						            ((String)module.get("activation_status")).equals("Y")) {
										  List<Map<String, Object>> cacheIssueTxns = new ArrayList<>();
										  BasicDynaBean savedBean = PharmacymasterDAO.getSelectedMedicineDetails(bean.get("medicine_id").toString());
										  cacheIssueTxns.add(itemScmService.getItemMasterJobMap(savedBean));
										  itemScmService.scheduleTxnExport(cacheIssueTxns, "ITEM_MASTER");
						        }
										flash.info("Item updated successfully.");
										flash.put("masterModified", "Y");
										ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
										redirect.addParameter("medicine_id", bean.get("medicine_id"));
										redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
										return redirect;
								} else {
									con.rollback();
									flash.error("Failed to add  Item..");
								}
							}else{
								con.rollback();
								flash.error("Item with this Item code "+ itmCode +" already exists.");
							}

					    }else {
					    	flash.error("Item with this barcode already exists");
					    }

					} else {
						flash.error("item name already exists..");

					}

			} else {
				flash.error("Incorrectly formatted values supplied");
			}

			if (success)
				flash.info("Item updated successfully.");
			ActionRedirect redirect = new ActionRedirect(m.findForward("addRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = null;
		String[] healthAuth = req.getParameterValues("health_authority");
		String[] codeTypes = req.getParameterValues("code_type");
		GenericDAO haCodeDao = new GenericDAO("ha_item_code_type");
		String[] ha_code_type_id = req.getParameterValues("ha_code_type_id");
    BasicDynaBean module = modulesDao.findByKey("module_id", "mod_scm");

		BasicDynaBean itemCodeExist = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			PharmacymasterDAO masterDAO = new PharmacymasterDAO(con);
			BasicDynaBean bean = masterDAO.getBean();
			String customBarcode = req.getParameter("item_barcode_id");
			bean.set("updated_timestamp",DateUtil.getCurrentTimestamp());
			ConversionUtils.copyToDynaBean(params, bean, errors);
			if (bean.get("issue_units") == null || bean.get("issue_units").equals("")) {
				bean.set("issue_units", req.getParameter("issue_units_hidden"));
			}
			if (bean.get("package_uom") == null || bean.get("package_uom").equals("")) {
				bean.set("package_uom", req.getParameter("package_uom_hidden"));
			}
			String[] medicineRoutesList = req.getParameterValues("route_of_admin");
			String commaSepListOfRoutes = "";
			boolean isfirst = true;
			if (medicineRoutesList != null) {
				for (int i=0; i<medicineRoutesList.length; i++) {
					if (isfirst) {
						commaSepListOfRoutes += medicineRoutesList[i];
						isfirst = false;
					} else {
						commaSepListOfRoutes += ","+ medicineRoutesList[i];
					}
				}
			}
			bean.set("route_of_admin", commaSepListOfRoutes);
			if (bean.get("item_strength") == null || bean.get("item_strength").equals(""))
				bean.set("item_strength_units", null);

			HttpSession session = req.getSession();
			String username = (String) session.getAttribute("userid");

			FlashScope flash = FlashScope.getScope(req);
			Object key = Integer.parseInt(req.getParameter("medicine_id"));
			Map<String, Object> keys = new HashMap<String, Object>();
			keys.put("medicine_id", key);
			String medName =  req.getParameter("medicine_name").trim();

			if (errors.isEmpty()) {
				BasicDynaBean idExists = masterDAO.findByKey("medicine_name", medName);
				if (null != idExists && !key.equals(idExists.get("medicine_id"))) {
					flash.error("Item name "+medName+" already exists..");
				}
				else {
					BasicDynaBean exists = masterDAO.findByKey("medicine_id", bean.get("medicine_id"));
					BasicDynaBean barcodeExists = masterDAO.findByKey("item_barcode_id", customBarcode);
					if(barcodeExists != null && !customBarcode.equals(exists.get("item_barcode_id"))) {
						flash.error("Item with this barcode already exists..");
					} else {
					String manfId = DirectStockEntryDAO.manfNameToId(req.getParameter("manf_name"));
		    		if (manfId == null || manfId.equals("")) {
		    			String manfCode = req.getParameter("manf_name");
		    			if (manfCode.length() >= 4)
		    				manfCode = manfCode.substring(0, 4);
		    			manfId = masterDAO.insertManfMaster(req.getParameter("manf_name"),manfCode);
		    			bean.set("manf_name", manfId);
		    		}else {
		    			bean.set("manf_name", manfId);
		    		}

		    		boolean isCodeExist = false;
		    		String itmCode = (null == req.getParameter("cust_item_code") && "".equalsIgnoreCase(req.getParameter("cust_item_code")))? null : req.getParameter("cust_item_code");
		    		if("".equals(itmCode)){
		    		  if (module != null && ((String) module.get("activation_status")).equals("Y")) {
		    		    int medCategoryId =  (Integer) bean.get("med_category_id");
		    		    int centerId = (Integer) req.getSession(false).getAttribute("centerId");
                itmCode = StockEntryDAO.getNextItemNo(medCategoryId, centerId);
                itemCodeExist = masterDAO.findByKey("cust_item_code", itmCode);
                if (itemCodeExist == null) {
                  bean.set("cust_item_code", itmCode);
                } else {
                  isCodeExist = true;
                }

              } else {
                itmCode = null;
                bean.set("cust_item_code", itmCode);
              }

    				}
		    		if(null != itmCode){
	    				itemCodeExist = masterDAO.findByKey("cust_item_code", itmCode);
		    			if(itemCodeExist == null){
		    				bean.set("cust_item_code", itmCode);
		    			}else{
		    				isCodeExist = true;
		    			}
	    			}

					String genericId = PharmacymasterDAO.getGenNameToId(con,(String)bean.get("generic_name"));
		    		bean.set("generic_name", genericId);
		    		if (bean.get("consumption_capacity") == null)
		    			bean.set("consumption_capacity", new BigDecimal(1)); // setting the default value

		    			boolean success = false;
		    			int result = 0;
		    			if(!isCodeExist){
		    				result = masterDAO.update(con, bean.getMap(), keys);
		    			}
						if (result > 0) {
							int item_id = (Integer) bean.get("medicine_id");
							success = saveOrUpdateStockLevel(params,item_id,con,req);
						}

						if(success) {
							int item_id = (Integer) bean.get("medicine_id");
							success = saveOrUpdateHealthAuthorityCodeTypes(item_id,con,req);
						}

						if(success) {
							int item_id = (Integer) bean.get("medicine_id");
							success = saveOrUpdateItemSubGroup(item_id,con,req);
						}

						if(success) {
							Integer medicineId = Integer.parseInt(bean.get("medicine_id").toString());
							success = saveOrUpdateInsuranceCategory(medicineId,con,req);
						}

						if(success) {
							int item_id = (Integer) bean.get("medicine_id");
							success = saveOrUpdateSupplierCenterTypes(item_id,con,req);
						}

						if (success){
								con.commit();
                if (module != null &&
                    ((String)module.get("activation_status")).equals("Y")) {
                  List<Map<String, Object>> cacheIssueTxns = new ArrayList<>();
                  BasicDynaBean savedBean = PharmacymasterDAO.getSelectedMedicineDetails(req.getParameter("medicine_id"));
                  cacheIssueTxns.add(itemScmService.getItemMasterJobMap(savedBean));
                  itemScmService.scheduleTxnExport(cacheIssueTxns, "ITEM_MASTER");
                }
								flash.info("Item  details updated successfully..");
								flash.put("masterModified", "Y");

						}else {
							if(!isCodeExist){
								con.rollback();
								flash.error("Failed to update item details..");
							}else{
								con.rollback();
								flash.error("Item with this Item code "+ itmCode +" already exists.");
							}

						}

					con.close();
				 }
			  }
			}
			else {
				flash.error("Incorrectly formatted values supplied");
			}
			ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("medicine_id", bean.get("medicine_id"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

			return redirect;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public ActionForward editItemCode(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		String medicineId = req.getParameter("medicine_id");
		List<BasicDynaBean> storeItemCodes = StoreItemCodesDAO.getItemCodeAndCodeTypes(Integer.parseInt(medicineId));
		req.setAttribute("storeItemCodes", storeItemCodes);
		req.setAttribute("itemBean", new GenericDAO("store_item_details").findByKey("medicine_id", Integer.parseInt(medicineId)));
		req.setAttribute("medicine_id", medicineId);

		return m.findForward("itemcodedetails");
	}

	public ActionForward saveItemCode(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		String medicineId = req.getParameter("medicine_id");
		String[] drugCodeType = req.getParameterValues("code_type");
		String[] drugCode = req.getParameterValues("item_code");
		String[] codeId = req.getParameterValues("code_id");
		StoreItemCodesDAO codeDAO = new StoreItemCodesDAO();
		BasicDynaBean module = modulesDao.findByKey("module_id", "mod_scm");
		Connection con = null;
		BasicDynaBean itemCodeBean = null;
		ActionRedirect redirect = new ActionRedirect(m.findForward("editItemCodeRedirect"));
		Map<String,Object> keys = null;
		FlashScope flash = FlashScope.getScope(req);
		boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if(drugCodeType != null) {
				for(int i=0;i<drugCodeType.length;i++) {
					itemCodeBean = codeDAO.getBean();
					itemCodeBean.set("medicine_id", Integer.parseInt(medicineId));
					itemCodeBean.set("code_type", drugCodeType[i]);
					itemCodeBean.set("item_code", drugCode[i]);
					itemCodeBean.set("desc_name", null);
					if(codeId != null && codeId[i] != null && !codeId[i].isEmpty()) {
						keys = new HashMap<String,Object>();
						keys.put("code_id", Integer.parseInt(codeId[i]));
						codeDAO.update(con, itemCodeBean.getMap(), keys);
					} else {
						itemCodeBean.set("code_id", codeDAO.getNextSequence());
						codeDAO.insert(con, itemCodeBean);
					}
				}
			}
			success = true;
			con.commit();
			if (module != null &&
          ((String)module.get("activation_status")).equals("Y")) {
        List<Map<String, Object>> cacheIssueTxns = new ArrayList<>();
        BasicDynaBean savedBean = PharmacymasterDAO.getSelectedMedicineDetails(medicineId);
        cacheIssueTxns.add(itemScmService.getItemMasterJobMap(savedBean));
        itemScmService.scheduleTxnExport(cacheIssueTxns, "ITEM_MASTER");
      }
		} catch(SQLException se) {
			flash.put("error", "Duplicate drug code types are not allowed for same item");
		}finally {
		  DataBaseUtil.commitClose(con, success);
		}
		redirect.addParameter("medicine_id", medicineId);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
	private boolean saveOrUpdateStockLevel(Map mform, int medicine_id, Connection con,HttpServletRequest request)
	throws SQLException, IOException{

		boolean flag = true;
		String [] dept_id = request.getParameterValues("hdepartment");
		Map<String, String[]> itemsMap = request.getParameterMap();
		int elelen = dept_id != null ?dept_id.length : 0;
		BigDecimal [] min_level = StoresDBTablesUtil.copyStringArrayTOBigdecimal(elelen,itemsMap.get("hiddenMinLevel"));
		BigDecimal [] max_level = StoresDBTablesUtil.copyStringArrayTOBigdecimal(elelen,itemsMap.get("hiddenMaxLevel"));
		BigDecimal [] danger_level = StoresDBTablesUtil.copyStringArrayTOBigdecimal(elelen,itemsMap.get("hiddenDangerLevel"));
		BigDecimal [] reorder_level = StoresDBTablesUtil.copyStringArrayTOBigdecimal(elelen,itemsMap.get("hiddenReorderLevel"));
		String [] deptoldrnew = request.getParameterValues("deptoldrnew");
		String [] delete = request.getParameterValues("hdeleted");
		String [] bin = request.getParameterValues("hiddenBin");


		if(dept_id != null){
			for(int i=0; i<elelen; i++){
				GenericDAO storedao = new GenericDAO ("item_store_level_details");
				BasicDynaBean bean  = storedao.getBean();
				bean.set("dept_id", Integer.parseInt(dept_id[i]));
				bean.set("medicine_id", medicine_id);
				bean.set("min_level", min_level[i]);
				bean.set("max_level", max_level[i]);
				bean.set("danger_level", danger_level[i]);
				bean.set("reorder_level", reorder_level[i]);
				if(!bin[i].isEmpty())
					bean.set("bin", bin[i]);

				if (deptoldrnew[i].equalsIgnoreCase("new") && delete[i].equalsIgnoreCase("false")) {

					Map reorderkeys = new HashMap();
					reorderkeys.put("dept_id", Integer.parseInt(dept_id[i]));
					reorderkeys.put("medicine_id", medicine_id);
					if ( storedao.findByKey(con,reorderkeys) != null) continue;//dept_id,medicine_id should be unique

					if (flag) flag = storedao.insert(con, bean);

					//inserting charges for all store rate plan for the new item

					List<BasicDynaBean> storeItemRatePlans = strateDAO.listAll();//list all of store rate plans
					StoreItemRatesDAO itemRatesDAO = new StoreItemRatesDAO();
					BasicDynaBean storeItemRates = itemRatesDAO.getBean();
					Map keys = new HashMap();
					BasicDynaBean itemRateBean = null;

					for ( BasicDynaBean storeRatePlan : storeItemRatePlans ) {

						storeItemRates = itemRatesDAO.getBean();
						keys.put("store_rate_plan_id", storeRatePlan.get("store_rate_plan_id"));
						keys.put("medicine_id", bean.get("medicine_id"));
						itemRateBean = itemRatesDAO.findByKey(con,keys);//checking for the item of this store_rate_plan_id
						if ( itemRateBean != null ) continue;

						storeItemRates.set("store_rate_plan_id", storeRatePlan.get("store_rate_plan_id"));
						storeItemRates.set("medicine_id", bean.get("medicine_id"));
						storeItemRates.set("selling_price", BigDecimal.ZERO);

						flag &= itemRatesDAO.insert(con, storeItemRates);

					}


				}else if (deptoldrnew[i].equalsIgnoreCase("old") && delete[i].equalsIgnoreCase("false")) {
					Map<String, Integer> keys = new HashMap<String, Integer>();
					keys.put("dept_id", Integer.parseInt(dept_id[i]));
					keys.put("medicine_id", (medicine_id));
					if (flag) flag = storedao.update(con, bean.getMap(), keys) > 0;
				}else if (deptoldrnew[i].equalsIgnoreCase("old") && delete[i].equalsIgnoreCase("true")) {
					if (flag) flag = storedao.delete(con, "dept_id", Integer.parseInt(dept_id[i]), "medicine_id", medicine_id);
				}

			}

		}
		return flag;
	}

	private boolean saveOrUpdateHealthAuthorityCodeTypes(int medicine_id, Connection con,HttpServletRequest request)
	throws SQLException, IOException{

		boolean flag = true;
		String [] healthAuth = request.getParameterValues("h_health_authority");
		String [] codeTypes = request.getParameterValues("h_code_type");
		String [] ha_code_type_id = request.getParameterValues("h_ha_code_type_id");
		String [] hacodeoldrnew = request.getParameterValues("hacodeoldrnew");
		String [] delete = request.getParameterValues("h_ha_deleted");
		StoreItemCodesDAO stdao = new StoreItemCodesDAO();
		GenericDAO hacodetypedao = new GenericDAO("ha_item_code_type");


		if(healthAuth != null){
			for(int i=0; i<healthAuth.length; i++){
				GenericDAO haItemCodeDao = new GenericDAO ("ha_item_code_type");
				BasicDynaBean bean  = haItemCodeDao.getBean();
				bean.set("medicine_id", medicine_id);
				bean.set("health_authority", healthAuth[i]);
				bean.set("code_type", codeTypes[i]);
				if (hacodeoldrnew[i].equalsIgnoreCase("new") && delete[i].equalsIgnoreCase("false")) {
					bean.set("ha_code_type_id", haItemCodeDao.getNextSequence());
					flag = haItemCodeDao.insert(con, bean);
				}else if (hacodeoldrnew[i].equalsIgnoreCase("old") && delete[i].equalsIgnoreCase("false")) {
					if(ha_code_type_id != null && ha_code_type_id[i] != null && !ha_code_type_id[i].isEmpty()) {
						Map<String, Integer> keys = new HashMap<String, Integer>();
						bean.set("ha_code_type_id", Integer.parseInt(ha_code_type_id[i]));
						keys.put("ha_code_type_id", Integer.parseInt(ha_code_type_id[i]));

						if(flag)
							flag = haItemCodeDao.update(con, bean.getMap(), keys) > 0;
					}
				}else if (hacodeoldrnew[i].equalsIgnoreCase("old") && delete[i].equalsIgnoreCase("true")) {
					if(ha_code_type_id != null && ha_code_type_id[i] != null && !ha_code_type_id[i].isEmpty()) {
						if (flag) flag = haItemCodeDao.delete(con, "ha_code_type_id", Integer.parseInt(ha_code_type_id[i]));
					}
				}

			}

		}
		return flag;
	}

	private boolean saveOrUpdateSupplierCenterTypes(int medicine_id, Connection con,HttpServletRequest request)
			throws SQLException, IOException{
			Map params = request.getParameterMap();
			List errors = new ArrayList();

			SupplierCenterDAO dao = new SupplierCenterDAO();
			BasicDynaBean storebean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, storebean, errors);
			boolean flag = true;
			Object key = request.getParameter("medicine_id");
			String [] prefer_supplier_id = request.getParameterValues("prefer_supplier_id");
			String [] center_id = request.getParameterValues("center_id");
			String [] supplier_code = request.getParameterValues("supplier_code");
			String [] suppoldrnew = request.getParameterValues("added");
			String [] delete = request.getParameterValues("selectedrow");
			int maxCenters = Integer.parseInt(genPref.getAllPrefs().get("max_centers_inc_default").toString());


			if (errors.isEmpty()) {
					GenericDAO centerdao = new GenericDAO("item_supplier_prefer_supplier");
					BasicDynaBean centerbean = centerdao.getBean();
					if(maxCenters == 1) {
						String supplier = request.getParameter("preferred_supplier");
						if (dao.isSupplierCenterExist(medicine_id, 0)) {
							centerbean.set("medicine_id", Integer.parseInt(key.toString()));
							centerbean.set("center_id", 0);
							centerbean.set("supplier_code", supplier);
							Map<String, Object> keys = new HashMap<String,Object>();
							keys.put("medicine_id", Integer.parseInt(key.toString()));
							keys.put("center_id",  0);
							int k = centerdao.update(con, centerbean.getMap(), keys);
							if(k>0) flag = true;
						} else {
							centerbean.set("prefer_supplier_id", centerdao.getNextSequence());
							centerbean.set("medicine_id", medicine_id);
							centerbean.set("center_id", 0);
							centerbean.set("supplier_code", supplier);
								flag = centerdao.insert(con, centerbean);
						}
					} else {
						for(int i=0; i<center_id.length; i++){
							if(!center_id[i].equals("")) {
								GenericDAO suppCenterDao = new GenericDAO ("item_supplier_prefer_supplier");
								BasicDynaBean bean  = suppCenterDao.getBean();
								bean.set("medicine_id", medicine_id);
								bean.set("supplier_code", supplier_code[i]);
								bean.set("center_id", Integer.parseInt(center_id[i]));
									if (suppoldrnew[i].equalsIgnoreCase("Y") && delete[i].equalsIgnoreCase("false")) {
										bean.set("prefer_supplier_id", suppCenterDao.getNextSequence());
										flag = suppCenterDao.insert(con, bean);
									} else if (suppoldrnew[i].equalsIgnoreCase("N") && delete[i].equalsIgnoreCase("false")) {
										if(prefer_supplier_id != null && prefer_supplier_id[i] != null && !prefer_supplier_id[i].isEmpty()) {
											Map<String, Integer> keys = new HashMap<String, Integer>();
											bean.set("prefer_supplier_id", Integer.parseInt(prefer_supplier_id[i]));
											keys.put("prefer_supplier_id", Integer.parseInt(prefer_supplier_id[i]));

											if(flag)
												flag = suppCenterDao.update(con, bean.getMap(), keys) > 0;
										}
									} else if (suppoldrnew[i].equalsIgnoreCase("N") && delete[i].equalsIgnoreCase("true")) {
										if(prefer_supplier_id != null && prefer_supplier_id[i] != null && !prefer_supplier_id[i].isEmpty()) {
											if (flag) flag = suppCenterDao.delete(con, "prefer_supplier_id", Integer.parseInt(prefer_supplier_id[i]));

										}
									}
							}
						}
					}
				}
			return flag;
		}

	private static TableDataHandler itemMasterHandler = null;

	private TableDataHandler getItemMasterHandler(String codeType) {
		String drugCodeType = "(code_type ='"+codeType+"' OR (code_type is null OR code_type = ''))";
		if (itemMasterHandler == null) {
			itemMasterHandler = new TableDataHandler(
					"store_item_codes_view",		// table name
					new String[]{"medicine_id"},	// keys
					new String[]{"medicine_name", "medicine_short_name", "item_barcode_id", "status",
						"batch_no_applicable", "item_strength", "item_strength_units",
						"item_form_id", "manf_name", "generic_name", "tax_rate", "tax_type", "bin",
						"package_type", "package_uom", "issue_units",
						"med_category_id", "service_sub_group_id", "control_type_id",
						"max_cost_price", "value",
						"insurance_category_id", "prior_auth_required","code_type","item_code",
						"item_selling_price, high_cost_consumable"
					},
					new String[][]{	// masters
						// our field        ref table        ref table id field  ref table name field
						{"item_form_id", "item_form_master", "item_form_id", "item_form_name"},
						{"item_strength_units", "strength_units", "unit_id", "unit_name"},
						{"manf_name", "manf_master", "manf_code", "manf_name"},
						{"generic_name", "generic_name", "generic_code", "generic_name"},
						{"med_category_id", "store_category_master", "category_id", "category"},
						{"service_sub_group_id", "service_sub_groups",
							"service_sub_group_id", "service_sub_group_name"},
						{"insurance_category_id", "item_insurance_categories",
							"insurance_category_id", "insurance_category_name"},
						{"control_type_id", "store_item_controltype",
							"control_type_id", "control_type_name"},
						{"package_uom", "package_uoms_view", "package_uom", "package_uom" },
						{"issue_units", "issue_uoms_view", "issue_uom", "issue_uom" }
					},
					new String[] {drugCodeType}
			);
		}

		itemMasterHandler.setAlias("item_strength_units", "item_strength_units");
		itemMasterHandler.setSequenceName("item_id_seq");
		return itemMasterHandler;
	}

	private TableDataHandler getImportItemMasterHandler(String codeType) {
		String drugCodeType = "(code_type ='"+codeType+"' OR (code_type is null OR code_type = ''))";
		if (itemMasterHandler == null) {
			itemMasterHandler = new TableDataHandler(
					"store_item_details",		// table name
					new String[]{"medicine_id"},	// keys
					new String[]{"medicine_name", "medicine_short_name", "item_barcode_id", "status",
						"batch_no_applicable", "item_strength", "item_strength_units",
						"item_form_id", "manf_name", "generic_name", "tax_rate", "tax_type", "bin",
						"package_type", "package_uom", "issue_units",
						"med_category_id", "service_sub_group_id", "control_type_id",
						"max_cost_price", "value",
						"insurance_category_id", "prior_auth_required",
						"item_selling_price, high_cost_consumable"
					},
					new String[][]{	// masters
						// our field        ref table        ref table id field  ref table name field
						{"item_form_id", "item_form_master", "item_form_id", "item_form_name"},
						{"item_strength_units", "strength_units", "unit_id", "unit_name"},
						{"manf_name", "manf_master", "manf_code", "manf_name"},
						{"generic_name", "generic_name", "generic_code", "generic_name"},
						{"med_category_id", "store_category_master", "category_id", "category"},
						{"service_sub_group_id", "service_sub_groups",
							"service_sub_group_id", "service_sub_group_name"},
						{"insurance_category_id", "item_insurance_categories",
							"insurance_category_id", "insurance_category_name"},
						{"control_type_id", "store_item_controltype",
							"control_type_id", "control_type_name"},
						{"package_uom", "package_uoms_view", "package_uom", "package_uom" },
						{"issue_units", "issue_uoms_view", "issue_uom", "issue_uom" }
					},
					new String[] {drugCodeType}
			);
		}

		itemMasterHandler.setAlias("item_strength_units", "item_strength_units");
		itemMasterHandler.setSequenceName("item_id_seq");
		return itemMasterHandler;
	}

	private TableDataHandler getExportItemCodeMasterHandler() {
		itemMasterHandler = null;
		if (itemMasterHandler == null) {
			itemMasterHandler = new TableDataHandler(
					"store_item_ha_code_types_view",		// table name
					new String[]{"code_id"},	// keys
					new String[]{"medicine_name","code_type","item_code"},
					new String[][]{},
					null
			);
		}
		itemMasterHandler.setAlias("medicine_name", "medicine_id");
		return itemMasterHandler;
	}

	private TableDataHandler getImportItemCodeMasterHandler() {
		if (itemMasterHandler == null) {
			itemMasterHandler = new TableDataHandler(
					"store_item_codes",		// table name
					new String[]{"code_id"},	// keys
					new String[]{"medicine_id","code_type","item_code"},
					new String[][]{
						{"medicine_id","store_item_details","medicine_id","medicine_name"},
						{"code_type","mrd_supported_codes","code_type","code_type"}
					},
					null
			);
		}
		itemMasterHandler.setSequenceName("store_item_codes_seq");
		return itemMasterHandler;
	}

	private TableDataHandler getExportHealthCodeTypeMasterHandler() {
		if (itemMasterHandler == null) {
			itemMasterHandler = new TableDataHandler(
					"store_item_health_authority_code_type_view",		// table name
					new String[]{"ha_code_type_id"},	// keys
					new String[]{"medicine_name","health_authority","code_type"},
					new String[][]{},
					null
			);
		}
		itemMasterHandler.setAlias("medicine_name", "medicine_id");
		return itemMasterHandler;

	}

	private TableDataHandler getImportItemHealthCodeTypeMasterHandler() {
		if (itemMasterHandler == null) {
			itemMasterHandler = new TableDataHandler(
					"ha_item_code_type",		// table name
					new String[]{"ha_code_type_id"},	// keys
					new String[]{"medicine_id","health_authority","code_type"},
					new String[][]{
							{"medicine_id","store_item_details","medicine_id","medicine_name"},
							{"code_type","mrd_supported_codes","code_type","code_type"},
							{"health_authority","health_authority_master","health_authority","health_authority"}
					},
					null
			);
		}
		itemMasterHandler.setSequenceName("ha_item_code_type_seq");
		return itemMasterHandler;

	}

	public ActionForward exportMaster(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		String[] codeTypes = req.getParameterValues("code_type");
		String codeStatus = req.getParameter("code_status");
		List<String> storeItemDelatilsColumnNames = null;
		storeItemDelatilsColumnNames = Arrays.asList(new String[]
				{ "medicine_id","medicine_name","medicine_short_name","item_barcode_id" ,
				"status","batch_no_applicable","item_strength","item_strength_units" ,
				"item_form_name","manf_name","generic_name","tax_rate","tax_type" ,
				"bin","package_type","package_uom","issue_uom","category" ,
				"service_sub_group_name","control_type_name","max_cost_price" ,
				"value","insurance_category_name","prior_auth_required","code_type" ,
				"item_code","item_selling_price", "high_cost_consumable", "cust_item_code"
				});


		List<String> reorderLabelsColumnNames = null;

		reorderLabelsColumnNames = Arrays.asList(new String[]
		                 {"medicine_id","dept_id","medicine_name","dept_name","bin","danger_level","min_level","reorder_level","max_level"});

		SXSSFWorkbook workbook = new SXSSFWorkbook(100);
		// Sheet for Store Item Details table
		SXSSFSheet StoreItemDetailsWorkSheet = workbook.createSheet("Store item details");
		StoreItemDetailsWorkSheet.setColumnHidden(0, true);
		List<BasicDynaBean> storeDetails = StockEntryDAO.storeItemDetails(codeTypes[0],codeStatus);
		Map<String, List> columnNamesMap = new HashMap<String, List>();
		columnNamesMap.put("mainItems", storeItemDelatilsColumnNames);
		HsSfWorkbookUtils.createPhysicalCellsWithValues(storeDetails, columnNamesMap, StoreItemDetailsWorkSheet, true);

		// Sheet for store_reorder_levels table
		SXSSFSheet storereorderlevelsworksheet = workbook.createSheet("Item store level details");
		storereorderlevelsworksheet.setColumnHidden(0, true);
		storereorderlevelsworksheet.setColumnHidden(1, true);
		List<BasicDynaBean> storereorderlevelsDetails = StockEntryDAO.reorderLevelDetails();
		Map<String, List> columnNamesMap2 = new HashMap<String, List>();
		columnNamesMap2.put("mainItems", reorderLabelsColumnNames);
		HsSfWorkbookUtils.createPhysicalCellsWithValues(storereorderlevelsDetails,columnNamesMap2, storereorderlevelsworksheet, true);

		res.setHeader("Content-type", "application/vnd.ms-excel");
		res.setHeader("Content-disposition","attachment; filename=store_item_details.xls");
		res.setHeader("Readonly", "true");
		java.io.OutputStream os = res.getOutputStream();
		workbook.write(os);
		os.flush();
		os.close();

		return null;
	}

	public ActionForward exportItemCodeDetailsToXls(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		getExportItemCodeMasterHandler().exportTable(res,"store_item_codes");
		itemMasterHandler = null;
		return null;
	}

	public ActionForward importItemCodeDetailsFromXls(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws Exception {

		FlashScope flash = FlashScope.getScope(req);
		String referer = req.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		UploadForm uploadForm = (UploadForm) f;
		InputStreamReader isReader = new InputStreamReader(uploadForm.getUploadFile().getInputStream());

		StringBuilder infoMsg = new StringBuilder();
		String error = getImportItemCodeMasterHandler().importTable(isReader, infoMsg);
		itemMasterHandler = null;

		if (error != null) {
			flash.put("error", error);
			return redirect;
		}

		flash.put("info", infoMsg.toString());
		return redirect;

	}

	public ActionForward exportItemHealthCodeTypeDetailsToXls(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws Exception {

		getExportHealthCodeTypeMasterHandler().exportTable(res,"HealthAuthorityCodeType");
		itemMasterHandler = null;
		return null;
	}

	public ActionForward importItemHealthCodeTypeDetailsFromXls(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws Exception {

		FlashScope flash = FlashScope.getScope(req);
		String referer = req.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		UploadForm uploadForm = (UploadForm) f;
		InputStreamReader isReader = new InputStreamReader(uploadForm.getUploadFile().getInputStream());

		StringBuilder infoMsg = new StringBuilder();
		String error = getImportItemHealthCodeTypeMasterHandler().importTable(isReader, infoMsg);
		itemMasterHandler = null;

		if (error != null) {
			flash.put("error", error);
			return redirect;
		}

		flash.put("info", infoMsg.toString());
		return redirect;

	}

	// upload file
	private StringBuilder errors;

	public ActionForward importMaster(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, Exception {

		UploadForm uploadForm = (UploadForm) f;
		String fileName = uploadForm.getUploadFile().getFileName();

		String referer = req.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		FlashScope flash = FlashScope.getScope(req);

		if (fileName.contains(".")) {
			String fileExtenstion = fileName.substring(
					fileName.lastIndexOf(".") + 1, fileName.length());
			if (!(fileExtenstion.equalsIgnoreCase("xls") || fileExtenstion
					.equalsIgnoreCase("xlsx"))) {

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
			XSSFWorkbook workBook = new XSSFWorkbook(byteStream);

			String SheetName = "";
			this.errors = new StringBuilder();
			XSSFSheet storesItemMasterSheet = null;
		    boolean flag = false;
			// Store Item Details
			if (workBook.getNumberOfSheets() > 0)
				SheetName = workBook.getSheetName(0);
			if (SheetName.equalsIgnoreCase("Store item details")) {
				storesItemMasterSheet = workBook.getSheetAt(0);
				storesItemMasterUpload(storesItemMasterSheet, errors);
				flag = true;
			} else {
				if (workBook.getNumberOfSheets() > 1) {
					storesItemMasterSheet = workBook.getSheetAt(1);
					String stockReorderLevelSheetName = workBook
							.getSheetName(1);
					storesItemMasterUpload(storesItemMasterSheet, errors);
					flag = true;
				}
			}

			// Stock Reorder Levels
			XSSFSheet stockReorderLevelSheet = null;
			if (SheetName.equalsIgnoreCase("Item store level details")) {
				stockReorderLevelSheet = workBook.getSheetAt(0);
				String storesItemMasterSheetName = workBook.getSheetName(0);
				stockReorderLevelUpload(stockReorderLevelSheet, errors);
				flag = true;
			} else {
				if (workBook.getNumberOfSheets() > 1) {
					stockReorderLevelSheet = workBook.getSheetAt(1);
					String stockReorderLevelSheetName = workBook
							.getSheetName(1);
					stockReorderLevelUpload(stockReorderLevelSheet, errors);
					flag = true;
				}
			}



		   if (this.errors.length() > 0){
				flash.put("error", this.errors);
			}else {
				if(flag){
					flash.put("info", "File successfully uploaded");
				}else{
					flash.put("error", "Unable to upload file.");
				}
			}

		} catch (Exception e) {
		  e.printStackTrace();
			flash.put("error", "Unable to upload file.");
		}
		// only xls files are allowed to Upload
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;

	}


	private void storesItemMasterUpload(XSSFSheet storesItemMasterSheet,StringBuilder errors)
	 			throws SQLException, IOException {
	     LinkedHashMap aliasMap = new LinkedHashMap();
	     List skipFieldsList=new ArrayList();
	     LinkedHashMap columnValueCheckerMap = new LinkedHashMap();

	     aliasMap.put("medicine_id", "medicine_id");
	     aliasMap.put("medicine_name", "medicine_name");
	     aliasMap.put("medicine_short_name", "medicine_short_name");
	     aliasMap.put("item_barcode_id", "item_barcode_id");
	     aliasMap.put("status", "status");
	     aliasMap.put("batch_no_applicable", "batch_no_applicable");
	     aliasMap.put("item_strength", "item_strength");
	     aliasMap.put("item_strength_units", "item_strength_units");
	     aliasMap.put("item_form_name", "item_form_name");
	     aliasMap.put("manf_name", "manf_name");
	     aliasMap.put("generic_name", "generic_name");
	     aliasMap.put("tax_rate", "tax_rate");
	     aliasMap.put("tax_type", "tax_type");
	     aliasMap.put("bin", "bin");
	     aliasMap.put("package_type", "package_type");
	     aliasMap.put("package_uom", "package_uom");
	     aliasMap.put("issue_uom", "issue_units");
	     aliasMap.put("category", "category");
	     aliasMap.put("service_sub_group_name", "service_sub_group_name");
	     aliasMap.put("control_type_name", "control_type_name");
	     aliasMap.put("max_cost_price", "max_cost_price");
	     aliasMap.put("value", "value");
	     aliasMap.put("insurance_category_name", "insurance_category_name");
	     aliasMap.put("prior_auth_required", "prior_auth_required");
	     aliasMap.put("item_selling_price", "item_selling_price");
	     aliasMap.put("high_cost_consumable", "high_cost_consumable");
	     aliasMap.put("cust_item_code", "cust_item_code");


	     skipFieldsList.add("code_type");
	     skipFieldsList.add("item_code");


	     List mandatoryFieldsList = Arrays.asList("medicine_name","medicine_short_name","manf_name","category","service_sub_group_name","value","status","tax_rate","tax_type","package_uom","issue_units");

	     String manfnameCheckFieldArray[] = {"manf_name",// field needed in main master table
	    "manf_code",//field in checking table
	    "manf_name",//Field uploaded in main master
	    "manf_master" // Checking table name
	        };
	     String categoryCheckFieldArray[] = {"med_category_id",// field needed in main master table
	    "category_id",//field in checking table
	    "category",//Field uploaded in main master
	    "store_category_master" // Checking table name
	        };
	     String servicesubgroupnameCheckFieldArray[] = {"service_sub_group_id",// field needed in main master table
	    "service_sub_group_id",//field in checking table
	    "service_sub_group_name",//Field uploaded in main master
	    "service_sub_groups" // Checking table name
	        };
	     String itemformnameCheckFieldArray[] = {"item_form_id",// field needed in main master table
	    "item_form_id",//field in checking table
	    "item_form_name",//Field uploaded in main master
	    "item_form_master" // Checking table name
	        };
	     String genericcodeCheckFieldArray[] = {"generic_name",// field needed in main master table
	    "generic_code",//field in checking table
	    "generic_name",//Field uploaded in main master
	    "generic_name" // Checking table name
	        };
	     String packageuomCheckFieldArray[] = {"package_uom",// field needed in main master table
	    "package_uom",//field in checking table
	    "package_uom",//Field uploaded in main master
	    "package_issue_uom" // Checking table name
	        };
	     String issueuomCheckFieldArray[] = {"issue_units",// field needed in main master table
	    "issue_uom",//field in checking table
	    "issue_uom",//Field uploaded in main master
	    "package_issue_uom" // Checking table name
	        };
	     String controltypenameCheckFieldArray[] = {"control_type_id",// field needed in main master table
	    "control_type_id",//field in checking table
	    "control_type_name",//Field uploaded in main master
	    "store_item_controltype" // Checking table name
	        };
	     String insurancecategorynameCheckFieldArray[] = {"insurance_category_id",// field needed in main master table
	    "insurance_category_id",//field in checking table
	    "insurance_category_name",//Field uploaded in main master
	    "item_insurance_categories" // Checking table name
	        };

	     columnValueCheckerMap.put("manf_name", manfnameCheckFieldArray);
	     columnValueCheckerMap.put("category", categoryCheckFieldArray);
	     columnValueCheckerMap.put("service_sub_group_name", servicesubgroupnameCheckFieldArray);
	     columnValueCheckerMap.put("item_form_name", itemformnameCheckFieldArray);
	     columnValueCheckerMap.put("generic_name", genericcodeCheckFieldArray);
	     columnValueCheckerMap.put("package_uom", packageuomCheckFieldArray);
	     columnValueCheckerMap.put("issue_uom", issueuomCheckFieldArray);
	     columnValueCheckerMap.put("control_type_name", controltypenameCheckFieldArray);
	     columnValueCheckerMap.put("insurance_category_name", insurancecategorynameCheckFieldArray);

	     ExcelImporter exImporter=new ExcelImporter("store_item_details","item_id_seq"){
	       @Override
	      protected boolean insertRecord(GenericDAO dao, BasicDynaBean itemBean, Connection con)
	          throws SQLException, IOException {

	         boolean result = false;
	         boolean success = false;
	         Savepoint insertSavePoint = null;
	         try{
	           insertSavePoint = con.setSavepoint();
	           result =  super.insertRecord(dao, itemBean, con);
	           success = true;
	           if(result){

	             List<BasicDynaBean> storeItemRatePlans = strateDAO.listAll();//list all of store rate plans
	             for ( BasicDynaBean storeRatePlan : storeItemRatePlans ) {
	               BasicDynaBean storeItemRates = itemRatesDAO.getBean();

	               storeItemRates.set("store_rate_plan_id", storeRatePlan.get("store_rate_plan_id"));
	               storeItemRates.set("medicine_id", itemBean.get("medicine_id"));
	               storeItemRates.set("selling_price", BigDecimal.ZERO);
	               success &= itemRatesDAO.insert(con, storeItemRates);

	             }
	           }

	           if(!(success && result)){
	             con.rollback(insertSavePoint);
	           }else{
	             con.releaseSavepoint(insertSavePoint);
	           }

	         }catch(SQLException se){
	           if(insertSavePoint != null){
	             con.rollback(insertSavePoint);
	           }
	           throw se;
	         }

	         return result && success;
	      }

	     };
	     exImporter.setId("medicine_id");
	     exImporter.setNoOfRowsSkip(0);
	     exImporter.setInsertEnable(true);
	     exImporter.setColumnNameForDate("updated_timestamp");
	     exImporter.setDateRequired(true);

	     exImporter.uploadExcelToDB(storesItemMasterSheet, aliasMap, columnValueCheckerMap, mandatoryFieldsList, errors, null, skipFieldsList);
	}

	private void stockReorderLevelUpload(XSSFSheet stockReorderLevelSheet,StringBuilder errors)
			throws SQLException, IOException {
	     //reorder level
		// int sheetCount= stockReorderLevelSheet.getSheetAt(1);
	     LinkedHashMap aliasMap = new LinkedHashMap();
	     aliasMap.clear();
	     aliasMap.put("medicine_id", "medicine_id");
	     aliasMap.put("medicine_name", "medicine_name");
	     aliasMap.put("dept_id", "dept_id");
	     aliasMap.put("dept_name", "dept_name");
	     aliasMap.put("min_level", "min_level");
	     aliasMap.put("max_level", "max_level");
	     aliasMap.put("reorder_level", "reorder_level");
	     aliasMap.put("danger_level", "danger_level");
	     aliasMap.put("bin", "bin");

	     List reorderLevelMandatoryFieldsList = Arrays.asList("medicine_name","dept_name");
	     LinkedHashMap reorderLevelColumnValueCheckerMap = new LinkedHashMap();
	     List reorderLevelSkipFieldsList=new ArrayList();

	     String medicineCheckFieldArray[] = {"medicine_id",// field needed in main master table
	      "medicine_id",//field in checking table
	      "medicine_name",//Field uploaded in main master
	      "store_item_details" // Checking table name
	          };

	     String deptCheckFieldArray[] = {"dept_id",// field needed in main master table
	      "dept_id",//field in checking table
	      "dept_name",//Field uploaded in main master
	      "stores" // Checking table name
	          };

	     reorderLevelColumnValueCheckerMap.put("medicine_name", medicineCheckFieldArray);
	     reorderLevelColumnValueCheckerMap.put("dept_name", deptCheckFieldArray);
	     ExcelImporter reorderLevelExImporter=new ExcelImporter("item_store_level_details",null);
	     reorderLevelExImporter.setCompositePrimaryKey(true);
	     List compositeKeys = new ArrayList();
	     compositeKeys.add("medicine_id");
	     compositeKeys.add("dept_id");
	     reorderLevelExImporter.setCompositePrimaryKeys(compositeKeys);
	     reorderLevelExImporter.setNoOfRowsSkip(0);
	     reorderLevelExImporter.setInsertEnable(true);
	     reorderLevelExImporter.uploadExcelToDB(stockReorderLevelSheet, aliasMap, reorderLevelColumnValueCheckerMap, reorderLevelMandatoryFieldsList, errors, null, reorderLevelSkipFieldsList);
	}

	private boolean saveOrUpdateItemSubGroup(int medicine_id, Connection con,HttpServletRequest request)
			throws SQLException, IOException{
			Map params = request.getParameterMap();
			List errors = new ArrayList();

			boolean flag = true;
			String [] itemSubgroupId = request.getParameterValues("item_subgroup_id");
			String [] delete = request.getParameterValues("deleted");

			if (errors.isEmpty()) {
				if(itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")){
					GenericDAO itemsubgroupdao = new GenericDAO("store_item_sub_groups");
					BasicDynaBean itemsubgroupbean = itemsubgroupdao.getBean();
					ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
					List records = itemsubgroupdao.findAllByKey("medicine_id", medicine_id);
					if (records.size() > 0)
						flag = itemsubgroupdao.delete(con, "medicine_id", medicine_id);

					for(int i=0; i<itemSubgroupId.length; i++){
						if(itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
							if (delete[i].equalsIgnoreCase("false")) {
								itemsubgroupbean.set("medicine_id", medicine_id);
								itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
								flag = itemsubgroupdao.insert(con, itemsubgroupbean);
							}
						}
					}
				}
			}
			return flag;

		}

	private boolean saveOrUpdateInsuranceCategory(int medicineId,
	  Connection con, HttpServletRequest request) throws SQLException, IOException {
		boolean flag = true;
		String[] insuranceCategories = request.getParameterValues("insurance_category_id");
		if (insuranceCategories != null && insuranceCategories.length > 0
		    && !insuranceCategories[0].equals("")) {
		  GenericDAO insuranceCategoryDAO =
		      new GenericDAO("store_items_insurance_category_mapping");
		  BasicDynaBean insuranceCategoryBean = insuranceCategoryDAO.getBean();
		  List<BasicDynaBean> records = insuranceCategoryDAO.findAllByKey("medicine_id", medicineId);
		  if (records != null && records.size() > 0) {
		    flag = insuranceCategoryDAO.delete(con,"medicine_id", medicineId);
		  }
		  for (String insuranceCategory :  insuranceCategories) {
		    insuranceCategoryBean.set("medicine_id", medicineId);
		    insuranceCategoryBean.set("insurance_category_id", Integer.parseInt(insuranceCategory));
		    flag = insuranceCategoryDAO.insert(con,insuranceCategoryBean);
		  }
		}
	return flag;
	}

	private boolean saveItemSubGroupForStoreTariff(int medicine_id, Connection con,HttpServletRequest request)
 throws SQLException, IOException {

    boolean flag = true;
    String[] itemSubgroupId = request.getParameterValues("item_subgroup_id");
    String[] delete = request.getParameterValues("deleted");

    if (itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")) {
      GenericDAO itemsubgroupdao = new GenericDAO("store_tariff_item_sub_groups");
      BasicDynaBean itemsubgroupbean = itemsubgroupdao.getBean();
      for (int i = 0; i < itemSubgroupId.length; i++) {
        if (itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()
            && delete[i].equalsIgnoreCase("false")) {
          List<BasicDynaBean> listOfStoreTariff = new GenericDAO("store_rate_plans").listAll();
          for(BasicDynaBean bean: listOfStoreTariff) {
            itemsubgroupbean.set("item_id", medicine_id);
            itemsubgroupbean.set("store_rate_plan_id",bean.get("store_rate_plan_id"));
            itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
            flag &= itemsubgroupdao.insert(con, itemsubgroupbean);
          }
        }
      }
    }

    return flag;

  }

}
