package com.insta.hms.mdm.stores;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.mdm.accounting.AccountingGroupService;
import com.insta.hms.mdm.bulk.BulkDataService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.counters.CounterService;
import com.insta.hms.mdm.grnprinttemplates.GrnPrintTemplatesService;
import com.insta.hms.mdm.pharmacyprinttemplate.PharmacyPrintTemplateService;
import com.insta.hms.mdm.prescriptionslabelprinttemplates.PrescriptionsLabelPrintTemplateService;
import com.insta.hms.mdm.prescriptionsprinttemplates.PrescriptionsTemplateService;
import com.insta.hms.mdm.printersettings.PrinterSettingService;
import com.insta.hms.mdm.storesrateplanmaster.StoresRatePlanService;
import com.insta.hms.mdm.storetypes.StoreTypeService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/** The Class StoreService. */
@Service
public class StoreService extends BulkDataService {

  /** The gst aliases. */
  private static Map<String, String> gstAliases = new HashMap<>();

  /** The vat aliases. */
  private static Map<String, String> vatAliases = new HashMap<>();

  /** The Constant TAX_IDENTIFIER_LABEL. */
  private static final String TAX_IDENTIFIER_LABEL = "pharmacy_tin_no";

  /** The Constant PURCHASE_PREFIX_LABEL. */
  private static final String PURCHASE_PREFIX_LABEL = "purchases_store_vat_account_prefix";

  /** The Constant PURCHASE_SALES_PREFIX_LABEL. */
  private static final String PURCHASE_SALES_PREFIX_LABEL = "purchases_store_cst_account_prefix";

  /** The Constant SALES_PREFIX_LABEL. */
  private static final String SALES_PREFIX_LABEL = "sales_store_vat_account_prefix";

  static {
    gstAliases.put(TAX_IDENTIFIER_LABEL, "pharmacy_gstin_no");
    gstAliases.put(PURCHASE_PREFIX_LABEL, "purchases_store_gst_account_prefix");
    gstAliases.put(PURCHASE_SALES_PREFIX_LABEL, "purchases_store_igst_account_prefix");
    gstAliases.put(SALES_PREFIX_LABEL, "sales_store_gst_account_prefix");

    vatAliases.put(TAX_IDENTIFIER_LABEL, "pharmacy_tin_no");
    vatAliases.put(PURCHASE_PREFIX_LABEL, "purchases_store_vat_account_prefix");
    vatAliases.put(PURCHASE_SALES_PREFIX_LABEL, "purchases_store_cst_account_prefix");
    vatAliases.put(SALES_PREFIX_LABEL, "sales_store_vat_account_prefix");
  }

  /** The pref service. */
  @LazyAutowired private GenericPreferencesService prefService;

  /** The center service. */
  @LazyAutowired private CenterService centerService;

  /** The counter service. */
  @LazyAutowired private CounterService counterService;

  /** The plp label print template service. */
  @LazyAutowired private PrescriptionsLabelPrintTemplateService plpLabelPrintTemplateService;

  /** The prescriptions template service. */
  @LazyAutowired private PrescriptionsTemplateService prescriptionsTemplateService;

  /** The acc grp service. */
  @LazyAutowired private AccountingGroupService accGrpService;

  /** The store type. */
  @LazyAutowired private StoreTypeService storeType;

  /** The stores rate plan service. */
  @LazyAutowired private StoresRatePlanService storesRatePlanService;

  /** The ppt service. */
  @LazyAutowired private PharmacyPrintTemplateService pptService;

  /** The printer setting service. */
  @LazyAutowired
  private PrinterSettingService printerSettingService;
  

  /** The grn print template service. */
  @LazyAutowired
  private GrnPrintTemplatesService grnPrintTemplateService;

  /**
   * Instantiates a new store service.
   *
   * @param repository the repository
   * @param validator the validator
   * @param csvEntity the csv entity
   */
  public StoreService(
      StoreRepository repository, StoreValidator validator, StoresCsvBulkDataEntity csvEntity) {
    super(repository, validator, csvEntity);
  }

  /**
   * Gets the list page data.
   *
   * @param requestParams the request params
   * @return the list page data
   */
  public Map<String, List<BasicDynaBean>> getListPageData(Map<String, String[]> requestParams) {
    Map<String, List<BasicDynaBean>> map = new HashMap<>();
    List<BasicDynaBean> centers = centerService.listAll(false);
    map.put("centers", centers);
    return map;
  }

  /**
   * Gets the adds the edit page data.
   *
   * @param params the params
   * @return the adds the edit page data
   */
  @SuppressWarnings("rawtypes")
  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map params) {

    Map<String, List<BasicDynaBean>> referenceMap = new HashMap<>();
    List<BasicDynaBean> templates = pptService.lookup(false);
    referenceMap.put("templates", templates);
    List<BasicDynaBean> printerSetting = printerSettingService.lookup(false);
    referenceMap.put("printer_setting", printerSetting);
    List<BasicDynaBean> pharmacyCounters = counterService.getPharmacyActiveCounters();
    referenceMap.put("pharmacy_counters", pharmacyCounters);
    List<BasicDynaBean> prescLblTemplates = plpLabelPrintTemplateService.lookup(false);
    referenceMap.put("presc_lbl_templates", prescLblTemplates);
    referenceMap.put("presc_templates", prescriptionsTemplateService.lookup(false));
    List<BasicDynaBean> centers = centerService.listAll(false);
    referenceMap.put("centers", centers);
    referenceMap.put("account_group_master", accGrpService.lookup(true));
    referenceMap.put("store_type_master", storeType.lookup(false));
    referenceMap.put("store_rate_plans", storesRatePlanService.lookup(true));
    referenceMap.put("grn_print_templates", grnPrintTemplateService.lookup(false));

    // TODO: Remove hack for generic Prefs after react pages start calling
    // generic prefs endpoint
    BasicDynaBean genericPreferences = prefService.getAllPreferences();
    List<BasicDynaBean> listGenPrefs = new ArrayList<>();
    listGenPrefs.add(genericPreferences);
    referenceMap.put("genPrefs", listGenPrefs);
    return referenceMap;
  }

  /**
   * Get the master data.
   *
   * @return the master data
   * @see com.insta.hms.mdm.bulk.BulkDataService#getMasterData()
   */
  @Override
  public Map<String, List<BasicDynaBean>> getMasterData() {
    Map<String, List<BasicDynaBean>> masterData = new HashMap<>();
    masterData.put("counter_id", counterService.lookup(false));
    masterData.put("account_group", accGrpService.lookup(false));
    masterData.put("store_type_id", storeType.lookup(false));
    masterData.put("center_id", centerService.lookup(false));
    masterData.put("store_rate_plan_id", storesRatePlanService.lookup(false));
    return masterData;
  }

  /**
   * Get the Dynamic Header Aliases.
   *
   * @return the dynamic header aliases
   * @see com.insta.hms.mdm.bulk.BulkDataService#getDynamicHeaderAliases()
   */
  @Override
  public Map<String, String> getDynamicHeaderAliases() {
    String taxMode = (String) prefService.getPreferences().get("procurement_tax_label");
    return null != taxMode && "G".equals(taxMode) ? gstAliases : vatAliases;
  }

  /**
   * This method is used to get Search Query data.
   *
   * @param params the params
   * @param listingParams the listing params
   * @return the search query assembler
   * @see com.insta.hms.mdm.MasterService#getSearchQueryAssembler(java.util.Map, java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected SearchQueryAssembler getSearchQueryAssembler(
      Map params, Map<LISTING, Object> listingParams) {
    SearchQueryAssembler qa = super.getSearchQueryAssembler(params, listingParams);
    qa.addSecondarySort("dept_id");
    return qa;
  }

  /**
   * Search and return the pageList.
   *
   * @param params the params
   * @param listingParams the listing params
   * @return the paged list
   * @see com.insta.hms.mdm.MasterService#search(java.util.Map, java.util.Map)
   */
  @Override
  public PagedList search(Map params, Map<LISTING, Object> listingParams) {
    return super.search(params, listingParams, true);
  }

  /**
   * Auto complete field name.
   *
   * @param parameters the parameters
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<String> autoCompleteFieldName(Map<String, String[]> parameters) {

    SearchQueryAssembler qb = null;
    final String region_fields = "SELECT * ";
    final String region_form = " FROM stores ";
    qb =
        new SearchQueryAssembler(
            region_fields, null, region_form, ConversionUtils.getListingParameter(parameters));
    qb.addFilter(QueryAssembler.STRING, "dept_name", "ILIKE", parameters.get("filterText")[0]);
    qb.build();
    PagedList pagedList = qb.getMappedPagedList();
    List<String> resultSet = new ArrayList<>();
    resultSet = pagedList.getDtoList();
    return resultSet;
  }

  /** The Constant ACCOUNT_ID. */
  // TODO Remove this from Service.
  private static final String ACCOUNT_ID =
      "SELECT account_group FROM stores " + "WHERE counter_id = ? limit 1";

  /**
   * Gets the account id.
   *
   * @param counterId the counter id
   * @return the account id
   */
  public Integer getAccountId(String counterId) {
    return DatabaseHelper.getInteger(ACCOUNT_ID, counterId);
  }

  /**
   * Gets the account group details.
   *
   * @param bean the bean
   * @return the account group details
   */
  public List<BasicDynaBean> getAccountGroupDetails(BasicDynaBean bean) {
    return ((StoreRepository) getRepository())
        .getAccountGroupDetails(
            (String) bean.get("counter_id"),
            (Integer) bean.get("account_group"),
            (Integer) bean.get("dept_id"));
  }

  /**
   * Gets the diagnostics store dependents.
   *
   * @param bean the bean
   * @return the diagnostics store dependents
   */
  public List<BasicDynaBean> getDiagnosticsStoreDependents(BasicDynaBean bean) {
    return ((StoreRepository) getRepository())
        .getDiagnosticsStoreDependents((Integer) bean.get("dept_id"));
  }

  /**
   * Gets the user default store dependents.
   *
   * @param bean the bean
   * @return the user default store dependents
   */
  public List<BasicDynaBean> getUserDefaultStoreDependents(BasicDynaBean bean) {
    return ((StoreRepository) getRepository())
        .getUserDefaultStoreDependents(String.valueOf(bean.get("dept_id")));
  }

  /**
   * Gets the user multi store dependents.
   *
   * @param bean the bean
   * @return the user multi store dependents
   */
  public List<BasicDynaBean> getUserMultiStoreDependents(BasicDynaBean bean) {
    return ((StoreRepository) getRepository())
        .getUserMultiStoreDependents(String.valueOf(bean.get("dept_id")));
  }

  /**
   * This method is used to get assigned stores for user.
   *
   * @param userName the user name
   * @return the list
   */
  public List<BasicDynaBean> findByUser(String userName) {
    return ((StoreRepository) getRepository()).findByUser(userName);
  }

  /**
   * This method is used to get all active stores.
   *
   * @return the list
   */
  public List<BasicDynaBean> listAllActive() {
    return ((StoreRepository) getRepository()).listAll(null, "status", "A","dept_name");
  }
  
  /**
   * Find by store.
   *
   * @param storeId
   *          the store id
   * @return the basic dyna bean
   */
  public BasicDynaBean findByStore(Integer storeId) {
    // public static final String GET_STORE_INFO="SELECT * FROM stores WHERE dept_id = ? AND
    // status='A' ORDER BY dept_name";
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("dept_id", storeId);
    filterMap.put("status", "A");
    List<BasicDynaBean> result = ((StoreRepository) getRepository()).findByCriteria(filterMap,
        "dept_name");
    return result.isEmpty() ? null : result.get(0);

  }
  
  /**
   * Checks if the storeId is for a valid store.
   *
   * @param storeId the store id
   * @return true, if is store valid
   */
  public boolean isStoreValid(int storeId) {
    return ((StoreRepository)getRepository()).exist("dept_id", storeId);
  }
  
  /**
   * User has access.
   *
   * @param storeId the store id
   * @param userId the user id
   * @return true, if successful
   */
  public boolean userHasAccess(int storeId, String userId) {
    List<BasicDynaBean> userStores = findByUser(userId);
    for (BasicDynaBean userStore : userStores) {
      if ((Integer)userStore.get("dept_id") == storeId) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Lists all store accessible to the user based on his role, logged in center
   * and the store access granted.
   * 
   * @return List of {@link BasicDynaBean} of stores.
   */
  public List<BasicDynaBean> listByUserAccess() {
    Integer loggedInCenter = RequestContext.getCenterId();
    String username = RequestContext.getUserName();
    Integer roleId = RequestContext.getRoleId();
    Map<String, Object> filterMap = new HashMap<String, Object>();
    if (roleId == 1 || roleId == 2) {
      filterMap.put("status", "A");
      if (0 != loggedInCenter) {
        filterMap.put("center_id", loggedInCenter);
      }
      return getRepository().listAll(null, filterMap, "dept_name");
    } else {
      return findByUser(username);
    }
  }
}
