package com.insta.hms.mdm.stores;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.BulkDataController;
import com.insta.hms.mdm.BulkDataResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * The Class StoreController.
 *
 * @author yashwant
 * @since February 2017
 */

@Controller
@RequestMapping(URLRoute.STORE_PATH)
public class StoreController extends BulkDataController {

  /** The Constant FILE_NAME. */
  private static final String FILE_NAME = "stores";

  /**
   * Instantiates a new store controller.
   *
   * @param service the service
   */
  public StoreController(StoreService service) {
    super(service, BulkDataResponseRouter.STORE_ROUTER, FILE_NAME);
  }

  /**
   * Get filter look up list of data from stores
   *
   * @see com.insta.hms.mdm.MasterController#getFilterLookupLists(java.util.Map)
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    return ((StoreService) getService()).getListPageData(params);
  }

  /**
   * Get list of reference data from stores
   *
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((StoreService) getService()).getAddEditPageData(params);
  }

  /*
   * @RequestMapping(value = "/exportMaster", method = RequestMethod.GET) public ModelAndView
   * exportMaster(HttpServletRequest request, ModelMap mmap, HttpServletResponse response) throws
   * ParseException, IOException {
   * 
   * List<BasicDynaBean> data = getCSVDataHandler().exportTable(null); List<Map<String, Object>>
   * csvData = ConversionUtils.copyListDynaBeansToMap(data);
   * 
   * CSVViewImpl view = new CSVViewImpl(); view.setFileName("stores.csv"); ModelAndView model = new
   * ModelAndView(view); model.addObject("csvData", csvData); model.addObject("csvHeader",
   * getHeaders(data.get(0)));
   * 
   * return model; }
   * 
   * private CSVHandler csvHandler = null;
   * 
   * protected CSVHandler getCSVDataHandler() { if (csvHandler == null) { csvHandler = new
   * CSVHandler("stores", // table name new String[] { "dept_id" }, // keys new String[] {
   * "dept_name", "counter_id", "status", "pharmacy_tin_no", "pharmacy_drug_license_no",
   * "account_group", "store_type_id", "is_super_store", "is_sterile_store", "sale_unit",
   * "center_id", "allowed_raise_bill", "is_sales_store", "auto_fill_indents",
   * "auto_fill_prescriptions", "purchases_store_vat_account_prefix",
   * "purchases_store_cst_account_prefix", "sales_store_vat_account_prefix", "store_rate_plan_id",
   * "use_batch_mrp", "auto_po_generation_frequency_in_days", "allow_auto_po_generation",
   * "auto_cancel_po_frequency_in_days", "allow_auto_cancel_po" }, new String[][] { // our field ref
   * table ref table id field ref table // name field { "counter_id", "counters", "counter_id",
   * "counter_no" }, { "account_group", "account_group_master", "account_group_id",
   * "account_group_name" }, { "store_type_id", "store_type_master", "store_type_id",
   * "store_type_name" }, { "center_id", "hospital_center_master", "center_id", "center_name" }, {
   * "store_rate_plan_id", "store_rate_plans", "store_rate_plan_id", "store_rate_plan_name" }, },
   * null); }
   * 
   * csvHandler.setSequenceName("stores_seq"); csvHandler.setAlias("store_rate_plan_id",
   * "store_tariff_name"); return csvHandler; }
   * 
   * @RequestMapping(value = "/importMaster", method = RequestMethod.POST) public ModelAndView
   * importMaster(HttpServletRequest request, ModelMap mmap, HttpServletResponse response,
   * RedirectAttributes redirect, @RequestPart("uploadFile") MultipartFile file) throws
   * SQLException, ParseException, IOException {
   * 
   * ModelAndView mav = new ModelAndView(); String referer = request.getHeader("Referer");
   * 
   * InputStreamReader isReader = new InputStreamReader(file.getInputStream()); StringBuilder
   * infoMsg = new StringBuilder(); String error = getCSVDataHandler().importTable(isReader,
   * infoMsg);
   * 
   * if (error != null) { redirect.addFlashAttribute("error", error); mav.addObject("error", error);
   * } else { redirect.addFlashAttribute("info", infoMsg); mav.addObject("infoMsg",
   * infoMsg.toString()); }
   * 
   * if (referer != null && !referer.isEmpty()) { RedirectView redirectView = new
   * RedirectView(referer, true); redirectView.setExposeModelAttributes(false);
   * mav.setView(redirectView); } else { mav.setViewName(URLRoute.STORE_MASTER_LIST); }
   * 
   * return mav; }
   * 
   * @RequestMapping(value = "/autoComplete", method = RequestMethod.GET) public ModelMap
   * lookUp(HttpServletRequest request) throws ParseException { Map<String, String[]> parameters =
   * request.getParameterMap(); List<String> filterSet = ((StoreMasterService)
   * getService()).autoCompleteFieldName(parameters); ModelMap modelMap = new ModelMap();
   * modelMap.addAttribute("dtoList", filterSet); modelMap.addAttribute("listSize",
   * filterSet.size()); return modelMap; }
   * 
   * @RequestMapping(value = "/getAccountGrpId", method = RequestMethod.GET) public ModelAndView
   * getAccountGrpId(HttpServletRequest request) throws ParseException {
   * 
   * ModelAndView mav = new ModelAndView(); String counterId = request.getParameter("counterId");
   * Integer account_group = ((StoreMasterService) getService()).getAccountId(counterId);
   * mav.addObject("account_group", account_group); return mav; }
   * 
   * private String[] getHeaders(BasicDynaBean data) { List<String> headers = new
   * ArrayList<String>(); for (DynaProperty property : data.getDynaClass().getDynaProperties()) {
   * headers.add(property.getName()); } return headers.toArray(new String[headers.size()]);
   * 
   * }
   */
}
