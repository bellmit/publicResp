package com.insta.hms.mdm.integration.item;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.mdm.bulk.BulkDataIntegrationService;
import com.insta.hms.mdm.bulk.CsvRowContext;
import com.insta.hms.mdm.integration.controltype.ControlTypeIntegrationService;
import com.insta.hms.mdm.integration.itemforms.ItemFormIntegrationService;
import com.insta.hms.mdm.integration.iteminsurancecategory.ItemInsuranceCategoriesIntegrationService;
import com.insta.hms.mdm.integration.medicineroute.MedicineRouteIntegrationService;
import com.insta.hms.mdm.integration.packageuom.PackageUomIntegrationService;
import com.insta.hms.mdm.integration.servicesubgroup.ServiceSubgroupIntegrationService;
import com.insta.hms.mdm.integration.storecategory.StoreCategoryIntegrationService;
import com.insta.hms.mdm.integration.stores.ManufacturerIntegrationService;
import com.insta.hms.mdm.integration.stores.genericnames.GenericNamesIntegrationService;
import com.insta.hms.mdm.integration.strengthunits.StrengthUnitsIntegrationService;
import com.insta.hms.mdm.integration.taxgroups.TaxGroupsIntegrationService;
import com.insta.hms.mdm.integration.taxsubgroups.TaxSubGroupsIntegrationService;
import com.insta.hms.mdm.item.StoreItemInsuranceCategoryMappingRepository;
import com.insta.hms.mdm.itemtaxuploaddownloads.StoreItemSubGroupService;
import com.insta.hms.mdm.storeitemrates.StoreItemRatesService;
import com.insta.hms.mdm.storeitemrates.taxsubgroup.StoreTariffItemSubgroupService;
import com.insta.hms.mdm.storesrateplanmaster.StoresRatePlanService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class StoreItemDetailIntegrationService extends BulkDataIntegrationService {

  @LazyAutowired
  private StoreCategoryIntegrationService storeCategoryIntegrationService;

  @LazyAutowired
  private ServiceSubgroupIntegrationService serviceSubgroupIntegrationService;

  @LazyAutowired
  private ManufacturerIntegrationService manufacturerIntegrationService;

  @LazyAutowired
  private GenericNamesIntegrationService genericNamesIntegrationService;

  @LazyAutowired
  private ControlTypeIntegrationService controlTypeIntegrationService;

  @LazyAutowired
  private ItemFormIntegrationService itemFormIntegrationService;

  @LazyAutowired
  private ItemInsuranceCategoriesIntegrationService itemInsuranceCategoriesIntegrationService;

  @LazyAutowired
  private PackageUomIntegrationService packageUomIntegrationService;

  @LazyAutowired
  private StoreItemInsuranceCategoryMappingRepository storeItemInsuranceCategoryMappingRepository;

  @LazyAutowired
  private TaxSubGroupsIntegrationService taxSubGroupsIntegrationService;

  @LazyAutowired
  private StoreItemSubGroupService storeItemSubGroupService;
  
  @LazyAutowired
  private StoresRatePlanService storesRatePlanService;
  
  @LazyAutowired
  private StoreItemRatesService storeItemRatesService;
  
  @LazyAutowired
  private StoreTariffItemSubgroupService storeTariffItemSubgroupService;

  @LazyAutowired
  private StoreItemDetailsIntegrationRepository storeItemDetailsIntegrationRepository;
  
  @LazyAutowired
  private GenericPreferencesService genPrefService;
  
  @LazyAutowired
  private TaxGroupsIntegrationService taxGroupsIntegrationService;
  
  @LazyAutowired
  private StrengthUnitsIntegrationService strengthUnitsIntegrationService;
  
  @LazyAutowired
  private MedicineRouteIntegrationService medicineRouteIntegrationService;
  
  @LazyAutowired
  private StoreItemDetailsIntegrationValidator storeItemDetailsIntegrationValidator;
  
  @LazyAutowired
  private MessageUtil messageUtil;

  private static final String HTTP_STATUS = "http-status";
  private static final String MEDICINE_NAME = "medicine_name";
  private static final String MEDICINE_SHORT_NAME = "medicine_short_name";
  private static final String CUST_ITEM_CODE = "cust_item_code";
  private static final String RETURN_CODE = "return_code";
  private static final String RETURN_MESSAGE = "return_message";
  private static final String INTEGRATION_UOM_ID = "integration_uom_id";
  private static final String INTEGRATION_CATEGORY_ID = "integration_category_id";
  private static final String INTEGRATION_MANF_ID = "integration_manf_id";
  private static final String INTEGRATION_STRENGTH_UNIT_ID = "integration_strength_unit_id";
  private static final String INTEGRATION_FORM_ID = "integration_form_id";
  private static final String INTEGRATION_MED_ROUTE_ID = "integration_medicine_route_id";
  private static final String INTEGRATION_INSU_CAT_ID = "integration_insurance_category_id";
  private static final String INTEGRATION_SUB_GROUP_ID = "integration_subgroup_id";
  private static final String MAPPING_NOT_FOUND = "exception.item.integration.key.mapping.notfound";
  private static final String NOT_NULL_KEY = "exception.notnull.value";

  
  private static final Map<String, String> ALIASES = new HashMap<String, String>() {
    {
      put("manf_name", "manf_code");
      put("generic_name", "generic_name_code");
    }
  };
  
  private static final Set<String> TAX_BASIS = new HashSet<>();

  public StoreItemDetailIntegrationService(StoreItemDetailsIntegrationRepository repository,
      StoreItemDetailsIntegrationValidator validator,
      StoreItemDetailsIntegrationBulkDataEntity csvEntity) {
    super(repository, validator, csvEntity);
    storeItemDetailsIntegrationValidator = validator;
  }

  @Override
  public Map<String, List<BasicDynaBean>> getMasterData() {
    Map<String, List<BasicDynaBean>> masterData = new HashMap<>();
    masterData.put("med_category_id", storeCategoryIntegrationService.lookup(false));
    masterData.put("manf_name", manufacturerIntegrationService.lookup(false));
    masterData.put("generic_name", genericNamesIntegrationService.lookup(false));
    masterData.put("control_type_id", controlTypeIntegrationService.lookup(false));
    masterData.put("item_form_id", itemFormIntegrationService.lookup(false));
    // masterData.put("insurance_category_id",
    // itemInsuranceCategoriesIntegrationService.lookup(false));
    return masterData;
  }

  @Override
  protected boolean preUpdate(CsvRowContext rowContext) {
    boolean ok = true;

    String[] row = rowContext.getRow();
    String[] headers = rowContext.getHeaders();
    String[] insuranceCategoryIds = row[ArrayUtils.indexOf(headers, "insurance_category_id")]
        .split(",");

    for (String insuranceCategoryId : insuranceCategoryIds) {
      BasicDynaBean insuranceCategoryBean = itemInsuranceCategoriesIntegrationService
          .findByIntegrationId(insuranceCategoryId);
      if (insuranceCategoryBean == null) {
        ok = false;
        addWarning(rowContext.getWarnings(), rowContext.getLineNumber(),
            "exception.inventory.item.invalid.insurance.category", insuranceCategoryId);
      }
    }

    if (ArrayUtils.indexOf(headers, "tax_sub_groups") != -1) {
      String[] taxSubgroups = row[ArrayUtils.indexOf(headers, "tax_sub_groups")].split(",");
      for (String taxSubgroup : taxSubgroups) {
        if (taxSubgroup.trim().isEmpty()) {
          continue;
        }
        BasicDynaBean taxSubgroupBean = taxSubGroupsIntegrationService
            .findByIntegrationId(taxSubgroup);
        if (taxSubgroupBean == null) {
          ok = false;
          addWarning(rowContext.getWarnings(), rowContext.getLineNumber(),
              "exception.inventory.item.invalid.tax.subgroup", taxSubgroup);
        }
      }
    }

    BasicDynaBean bean = rowContext.getBean();
    bean.set("service_sub_group_id", 26);

    int uomIndex = ArrayUtils.indexOf(headers, "uom_id");
    if (uomIndex == -1) {
      ok = false;
      addWarning(rowContext.getWarnings(), rowContext.getLineNumber(), "exception.csv.uom.needed");
    } else {
      String packageUomIntegraionId = row[uomIndex];
      BasicDynaBean packageUomBean = packageUomIntegrationService
          .findByIntegrationId(packageUomIntegraionId);
      if (packageUomBean != null) {
        bean.set("issue_units", packageUomBean.get("issue_uom"));
        bean.set("package_uom", packageUomBean.get("package_uom"));
        bean.set("issue_base_unit", packageUomBean.get("package_size"));
      } else {
        ok = false;
        addWarning(rowContext.getWarnings(), rowContext.getLineNumber(),
            "exception.csv.invalid.uom", packageUomIntegraionId);
      }
    }

    return super.preUpdate(rowContext) && ok;
  }

  @Override
  protected void postUpdate(CsvRowContext rowContext) {

    String[] row = rowContext.getRow();
    String[] headers = rowContext.getHeaders();
    String[] insuranceCategoryIds = row[ArrayUtils.indexOf(headers, "insurance_category_id")]
        .split(",");
    BasicDynaBean medicineBean = rowContext.getBean();

    storeItemInsuranceCategoryMappingRepository.delete("medicine_id",
        medicineBean.get("medicine_id"));

    for (String insuranceCategoryId : insuranceCategoryIds) {
      BasicDynaBean insuranceCategoryBean = itemInsuranceCategoriesIntegrationService
          .findByIntegrationId(insuranceCategoryId);
      BasicDynaBean mappingBean = storeItemInsuranceCategoryMappingRepository.getBean();
      mappingBean.set("medicine_id", medicineBean.get("medicine_id"));
      mappingBean.set("insurance_category_id", insuranceCategoryBean.get("insurance_category_id"));
      storeItemInsuranceCategoryMappingRepository.insert(mappingBean);
    }

    if (ArrayUtils.indexOf(headers, "tax_sub_groups") != -1) {
      storeItemSubGroupService.delete("medicine_id", medicineBean.get("medicine_id"));
      String[] taxSubgroups = row[ArrayUtils.indexOf(headers, "tax_sub_groups")].split(",");
      for (String taxSubgroup : taxSubgroups) {
        if (taxSubgroup.trim().isEmpty()) {
          continue;
        }
        BasicDynaBean taxSubgroupBean = taxSubGroupsIntegrationService
            .findByIntegrationId(taxSubgroup);
        BasicDynaBean mappingBean = storeItemSubGroupService.getBean();
        mappingBean.set("medicine_id", medicineBean.get("medicine_id"));
        mappingBean.set("item_subgroup_id", taxSubgroupBean.get("item_subgroup_id"));
        storeItemSubGroupService.insert(mappingBean);
      }
    }

  }

  @Override
  public Map<String, String> getDynamicHeaderAliases() {
    return ALIASES;
  }

  public BasicDynaBean getBean() {
    return storeItemDetailsIntegrationRepository.getBean();
  }

  /**
   * create item.
   * @param params the params
   * @return map
   */
  public Map<String, Object> createItem(Map<String, Object> params) {
    Map<String, Object> responseData = new HashMap<>();
    List<String> errors = new ArrayList<>();
    List<String> info = new ArrayList<>();
    BasicDynaBean storeItemBean = getBean();
    ConversionUtils.copyToDynaBean(params, storeItemBean, errors);
    String endPoint = (String) params.get("endPoint");

    String medicineName = (String) storeItemBean.get(MEDICINE_NAME);
    String customItemCode = (String) storeItemBean.get(CUST_ITEM_CODE);

    BasicDynaBean medicineNameExists = null;
    BasicDynaBean customItemExists = null;
    if (null != medicineName && !"".equals(medicineName)) {
      medicineNameExists = storeItemDetailsIntegrationRepository.findByKey(MEDICINE_NAME,
          medicineName);
    }

    if (null != customItemCode && !"".equals(customItemCode)) {
      customItemExists = storeItemDetailsIntegrationRepository.findByKey(CUST_ITEM_CODE,
          customItemCode);
    }
    if (medicineNameExists == null && null == customItemExists) {
      storeItemBean.set("created_timestamp", DateUtil.getCurrentTimestamp());
      storeItemBean.set("medicine_id", storeItemDetailsIntegrationRepository.getNextId());
      String medicineShortName = (String) params.get(MEDICINE_SHORT_NAME);
      if (null != medicineShortName && medicineShortName != "") {
        storeItemBean.set(MEDICINE_SHORT_NAME, medicineShortName);
      } else {
        storeItemBean.set(MEDICINE_SHORT_NAME, medicineName);
      }
      if (null != endPoint && "API".equals(endPoint)) {
        storeItemDetailsIntegrationValidator.mappedValidValueForInsertItem(storeItemBean, info);
        checkValuesForIntegrationKeys(params, storeItemBean, responseData, info, errors);
        if (errors.isEmpty()) {
          validator.validateInsert(storeItemBean);
        }
      } else {
        validator.validateInsert(storeItemBean);
      }

      if (errors.isEmpty()) {
        int returnedValue = storeItemDetailsIntegrationRepository.insert(storeItemBean);
        
        if (returnedValue == 1) {
          // inserting charges for all store rate plan for the new item
          List<BasicDynaBean> storeItemRatePlans = storesRatePlanService.listAll();
          for (BasicDynaBean storeRatePlan : storeItemRatePlans) {
            BasicDynaBean storeItemRates = storeItemRatesService.getBean();
            storeItemRates.set("store_rate_plan_id", storeRatePlan.get("store_rate_plan_id"));
            storeItemRates.set("medicine_id", storeItemBean.get("medicine_id"));
            storeItemRates.set("selling_price", BigDecimal.ZERO);
            storeItemRatesService.insert(storeItemRates);
          }
        }
        
        if (returnedValue == 1) {
          responseData.put(HTTP_STATUS, HttpStatus.CREATED);
          responseData.put(RETURN_CODE, "201");
          responseData.put(RETURN_MESSAGE, messageUtil.getMessage("msg.item.created.successfully",
              new Object[] { medicineName }));
        }
      } else {
        responseData.put(RETURN_CODE, "400");
        responseData.put(RETURN_MESSAGE,
            messageUtil.getMessage("exception.validation.failed"));
      }
    } else {
      responseData.put(HTTP_STATUS, HttpStatus.CONFLICT);
      responseData.put(RETURN_CODE, "409");
      if (medicineNameExists != null) {
        errors.add(
            messageUtil.getMessage("exception.item.already.exist", new Object[] { medicineName }));
      }
      if (customItemExists != null) {
        errors.add(messageUtil.getMessage("exception.item.already.exist",
            new Object[] { customItemCode }));
      }
    }
    if (!info.isEmpty()) {
      responseData.put("INFO", info);
    }
    if (!errors.isEmpty()) {
      responseData.put("validationErrors", errors);
    }
    return responseData;

  }
  
  /**
   * check values for integration keys.
   * @param params the params
   * @param bean the bean
   * @param responseData the responseData
   * @param info the info
   * @param errors the errors
   */
  public void checkValuesForIntegrationKeys(Map<String, Object> params, BasicDynaBean bean,
      Map<String, Object> responseData, List<String> info, List<String> errors) {
    String integrationUomId = (String) params.get(INTEGRATION_UOM_ID);
    if (null != integrationUomId && !"".equals(integrationUomId)) {
      BasicDynaBean packageIssueUomBean = packageUomIntegrationService
          .findByIntegrationId(integrationUomId);
      if (null != packageIssueUomBean) {
        bean.set("package_uom", packageIssueUomBean.get("package_uom"));
        bean.set("issue_units", packageIssueUomBean.get("issue_uom"));
        bean.set("issue_base_unit", packageIssueUomBean.get("package_size"));
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { INTEGRATION_UOM_ID, integrationUomId }));
      }
    } else {
      BasicDynaBean gprefs = genPrefService.getAllPreferences();
      String packageUom = (String) gprefs.get("package_uom");
      bean.set("package_uom", packageUom);
      String issueUom = (String) gprefs.get("issue_uom");
      bean.set("issue_units", issueUom);
      BigDecimal packageSize = (BigDecimal) gprefs.get("package_size");
      bean.set("issue_base_unit", packageSize);
    }

    String categoryId = (String) params.get(INTEGRATION_CATEGORY_ID);
    if (null != categoryId && !"".equals(categoryId)) {
      BasicDynaBean categoryBean = storeCategoryIntegrationService.findByIntegrationId(categoryId);
      if (null != categoryBean) {
        bean.set("med_category_id", (int) categoryBean.get("category_id"));
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { INTEGRATION_CATEGORY_ID, categoryId }));
      }
    } else {
      responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
      errors.add(messageUtil.getMessage(NOT_NULL_KEY, new Object[] {INTEGRATION_CATEGORY_ID}));
    }

    String integrationManfId = (String) params.get(INTEGRATION_MANF_ID);
    if (null != integrationManfId && !"".equals(integrationManfId)) {
      BasicDynaBean manufacturerBean = manufacturerIntegrationService
          .findByIntegrationId(integrationManfId);
      if (null != manufacturerBean) {
        bean.set("manf_name", manufacturerBean.get("manf_code"));
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { INTEGRATION_MANF_ID, integrationManfId }));
      }
    } else {
      responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
      errors.add(messageUtil.getMessage(NOT_NULL_KEY, new Object[] { INTEGRATION_MANF_ID }));
    }

    String integrationStrengthId = (String) params.get(INTEGRATION_STRENGTH_UNIT_ID);
    if (null != integrationStrengthId && !"".equals(integrationStrengthId)) {
      BasicDynaBean strengthUnitFormBean = strengthUnitsIntegrationService
          .findByIntegrationId(integrationStrengthId);
      if (null != strengthUnitFormBean) {
        bean.set("item_strength_units", (int) strengthUnitFormBean.get("unit_id"));
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { INTEGRATION_STRENGTH_UNIT_ID, integrationStrengthId }));
      }
    }

    String itemFormId = (String) params.get(INTEGRATION_FORM_ID);
    if (null != itemFormId && !"".equals(itemFormId)) {
      BasicDynaBean itemFormBean = itemFormIntegrationService.findByIntegrationId(itemFormId);
      if (null != itemFormBean) {
        bean.set("item_form_id", (int) itemFormBean.get("item_form_id"));
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { INTEGRATION_FORM_ID, itemFormId }));
      }
    }

    Object integrationRouteObj = params.get(INTEGRATION_MED_ROUTE_ID);
    List<String> medicineRouteIdList = null;

    if (integrationRouteObj instanceof String) {
      medicineRouteIdList = new ArrayList<>();
      medicineRouteIdList.add((String) integrationRouteObj);
    }
    if (integrationRouteObj instanceof List) {
      medicineRouteIdList = (ArrayList<String>) integrationRouteObj;
    }
    if (null != medicineRouteIdList && !medicineRouteIdList.isEmpty()) {
      Set<String> medicineRouteIds = new HashSet<>(medicineRouteIdList);
      String commaSepListOfRoutes = "";
      boolean isfirst = true;

      for (String routeId : medicineRouteIds) {
        BasicDynaBean medicineRouteBean = medicineRouteIntegrationService
            .findByIntegrationId(routeId);
        if (null != medicineRouteBean) {
          if (isfirst) {
            commaSepListOfRoutes += medicineRouteBean.get("route_id");
            isfirst = false;
          } else {
            commaSepListOfRoutes += "," + medicineRouteBean.get("route_id");
          }
        } else {
          responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
          errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
              new Object[] { INTEGRATION_MED_ROUTE_ID, routeId }));
        }
      }
      bean.set("route_of_admin", commaSepListOfRoutes);
    }

    String genericCode = (String) params.get("integration_generic_name_id");
    if (null != genericCode && !"".equals(genericCode)) {
      BasicDynaBean genericNameBean = genericNamesIntegrationService
          .findByIntegrationId(genericCode);
      if (null != genericNameBean) {
        bean.set("generic_name", genericNameBean.get("generic_code"));
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { "integration_generic_name_id", genericCode }));
      }
    }

    String serviceSubGroupId = (String) params.get("integration_service_sub_group_id");
    if (null != serviceSubGroupId && !"".equals(serviceSubGroupId)) {
      BasicDynaBean serviceSubGroupBean = serviceSubgroupIntegrationService
          .findByIntegrationId(serviceSubGroupId);
      if (null != serviceSubGroupBean) {
        bean.set("service_sub_group_id", (int) serviceSubGroupBean.get("service_sub_group_id"));
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { "integration_service_sub_group_id", serviceSubGroupId }));
      }
    }

    String controlTypeId = (String) params.get("integration_control_type_id");
    if (null != controlTypeId && !"".equals(controlTypeId)) {
      BasicDynaBean controlTypeBean = controlTypeIntegrationService
          .findByIntegrationId(controlTypeId);
      if (null != controlTypeBean) {
        bean.set("control_type_id", (int) controlTypeBean.get("control_type_id"));
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { "integration_control_type_id", controlTypeId }));
      }
    } else {
      responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
      errors.add(
          messageUtil.getMessage(NOT_NULL_KEY, new Object[] { "integration_control_type_id" }));
    }

    Object insuCateIdObj = params.get(INTEGRATION_INSU_CAT_ID);
    List<String> insuCateIdList = null;
    if (insuCateIdObj instanceof String) {
      insuCateIdList = new ArrayList<>();
      insuCateIdList.add((String) insuCateIdObj);
    }
    if (insuCateIdObj instanceof List) {
      insuCateIdList = (ArrayList<String>) insuCateIdObj;
    }
         
    if (null != insuCateIdList && !insuCateIdList.isEmpty()) {
      Set<String> insuCateIdSet = new HashSet<>(insuCateIdList);
      saveOrUpdateInsuranceCategory(responseData, (int) bean.get("medicine_id"), insuCateIdSet,
          errors);
    } else {
      responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
      errors.add(messageUtil.getMessage(NOT_NULL_KEY, new Object[] {INTEGRATION_INSU_CAT_ID}));
    }

    Object itemSubGroupCategoryObj = params.get(INTEGRATION_SUB_GROUP_ID);
    List<String> itemSubGroupCategoriesList = null;
    if (itemSubGroupCategoryObj instanceof String) {
      itemSubGroupCategoriesList = new ArrayList<>();
      itemSubGroupCategoriesList.add((String) itemSubGroupCategoryObj);
    }
    if (itemSubGroupCategoryObj instanceof List) {
      itemSubGroupCategoriesList = (ArrayList<String>) itemSubGroupCategoryObj;
    }
     
    if (null != itemSubGroupCategoriesList && !itemSubGroupCategoriesList.isEmpty()) {
      Set<String> itemSubGroupCategoriesSet = new HashSet<>(itemSubGroupCategoriesList);
      saveOrUpdateItemSubGroup(responseData, (int) bean.get("medicine_id"),
          itemSubGroupCategoriesSet, errors);
      saveItemSubGroupForStoreTariff(responseData, (int) bean.get("medicine_id"),
          itemSubGroupCategoriesSet, errors);
    }

    String billingGroupId = (String) params.get("integration_group_id");
    if (null != billingGroupId && !"".equals(billingGroupId)) {
      BasicDynaBean billingGroupBean = taxGroupsIntegrationService
          .findByIntegrationId(billingGroupId);
      if (null != billingGroupBean) {
        bean.set("billing_group_id", (int) billingGroupBean.get("item_group_id"));
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { "integration_group_id", billingGroupId }));
      }
    }

  }
  
  /**
   * check values for integration keys in update.
   * @param params the params
   * @param bean the bean
   * @param responseData the responseData
   * @param info the info
   * @param errors the errors
   */
  public void checkValuesForIntegrationKeysInUpdate(Map<String, Object> params, BasicDynaBean bean,
      Map<String, Object> responseData, List<String> info, List<String> errors) {
    String integrationUomId = (String) params.get(INTEGRATION_UOM_ID);
    if (null != integrationUomId && !"".equals(integrationUomId)) {
      BasicDynaBean packageIssueUomBean = packageUomIntegrationService
          .findByIntegrationId(integrationUomId);
      if (null != packageIssueUomBean) {
        bean.set("package_uom", packageIssueUomBean.get("package_uom"));
        bean.set("issue_units", packageIssueUomBean.get("issue_uom"));
        bean.set("issue_base_unit", packageIssueUomBean.get("package_size"));
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { INTEGRATION_UOM_ID, integrationUomId }));
      }
    }

    String categoryId = (String) params.get(INTEGRATION_CATEGORY_ID);
    if (null != categoryId && !"".equals(categoryId)) {
      BasicDynaBean categoryBean = storeCategoryIntegrationService.findByIntegrationId(categoryId);
      if (null != categoryBean) {
        bean.set("med_category_id", (int) categoryBean.get("category_id"));
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { INTEGRATION_CATEGORY_ID, categoryId }));
      }

    }

    String integrationManfId = (String) params.get(INTEGRATION_MANF_ID);
    if (null != integrationManfId && !"".equals(integrationManfId)) {
      BasicDynaBean manufacturerBean = manufacturerIntegrationService
          .findByIntegrationId(integrationManfId);
      if (null != manufacturerBean) {
        bean.set("manf_name", manufacturerBean.get("manf_code"));
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { INTEGRATION_MANF_ID, integrationManfId }));
      }
    }

    String integrationStrengthId = (String) params.get(INTEGRATION_STRENGTH_UNIT_ID);
    if (null != integrationStrengthId && !"".equals(integrationStrengthId)) {
      BasicDynaBean strengthUnitFormBean = strengthUnitsIntegrationService
          .findByIntegrationId(integrationStrengthId);
      if (null != strengthUnitFormBean) {
        bean.set("item_strength_units", (int) strengthUnitFormBean.get("unit_id"));
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { INTEGRATION_STRENGTH_UNIT_ID, integrationStrengthId }));
      }
    }

    String itemFormId = (String) params.get(INTEGRATION_FORM_ID);
    if (null != itemFormId && !"".equals(itemFormId)) {
      BasicDynaBean itemFormBean = itemFormIntegrationService.findByIntegrationId(itemFormId);
      if (null != itemFormBean) {
        bean.set("item_form_id", (int) itemFormBean.get("item_form_id"));
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { INTEGRATION_FORM_ID, itemFormId }));
      }
    }

    Object integrationRouteObj = params.get(INTEGRATION_MED_ROUTE_ID);
    List<String> medicineRouteIdList = null;

    if (integrationRouteObj instanceof String) {
      medicineRouteIdList = new ArrayList<>();
      medicineRouteIdList.add((String) integrationRouteObj);
    }
    if (integrationRouteObj instanceof List) {
      medicineRouteIdList = (ArrayList<String>) integrationRouteObj;
    }
    
    if (null != medicineRouteIdList && !medicineRouteIdList.isEmpty()) {
      Set<String> medicineRouteIds = new HashSet<>(medicineRouteIdList);
      String commaSepListOfRoutes = "";
      boolean isfirst = true;

      for (String routeId : medicineRouteIds) {
        BasicDynaBean medicineRouteBean = medicineRouteIntegrationService
            .findByIntegrationId(routeId);
        if (null != medicineRouteBean) {
          if (isfirst) {
            commaSepListOfRoutes += medicineRouteBean.get("route_id");
            isfirst = false;
          } else {
            commaSepListOfRoutes += "," + medicineRouteBean.get("route_id");
          }
        } else {
          responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
          errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
              new Object[] { INTEGRATION_MED_ROUTE_ID, routeId }));
        }
      }
      bean.set("route_of_admin", commaSepListOfRoutes);
    }

    String genericCode = (String) params.get("integration_generic_name_id");
    if (null != genericCode && !"".equals(genericCode)) {
      BasicDynaBean genericNameBean = genericNamesIntegrationService
          .findByIntegrationId(genericCode);
      if (null != genericNameBean) {
        bean.set("generic_name", genericNameBean.get("generic_code"));
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { "integration_generic_name_id", genericCode }));
      }
    }

    String serviceSubGroupId = (String) params.get("integration_service_sub_group_id");
    if (null != serviceSubGroupId && !"".equals(serviceSubGroupId)) {
      BasicDynaBean serviceSubGroupBean = serviceSubgroupIntegrationService
          .findByIntegrationId(serviceSubGroupId);
      if (null != serviceSubGroupBean) {
        bean.set("service_sub_group_id", (int) serviceSubGroupBean.get("service_sub_group_id"));
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { "integration_service_sub_group_id", serviceSubGroupId }));
      }
    }

    String controlTypeId = (String) params.get("integration_control_type_id");
    if (null != controlTypeId) {
      if (!"".equals(controlTypeId)) {
        BasicDynaBean controlTypeBean = controlTypeIntegrationService
            .findByIntegrationId(controlTypeId);
        if (null != controlTypeBean) {
          bean.set("control_type_id", (int) controlTypeBean.get("control_type_id"));
        } else {
          responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
          errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
              new Object[] { "integration_control_type_id", controlTypeId }));
        }
      } else {
        bean.set("control_type_id", null);
      }

    }

    Object insuCateIdObj = params.get(INTEGRATION_INSU_CAT_ID);
    List<String> insuCateIdList = null;
    if (insuCateIdObj instanceof String) {
      insuCateIdList = new ArrayList<>();
      insuCateIdList.add((String) insuCateIdObj);
    }
    if (insuCateIdObj instanceof List) {
      insuCateIdList = (ArrayList<String>) insuCateIdObj;
    }
    if (null != insuCateIdList && !insuCateIdList.isEmpty()) {
      Set<String> insuCateIdSet = new HashSet<>(insuCateIdList);
      saveOrUpdateInsuranceCategory(responseData, (int) bean.get("medicine_id"), insuCateIdSet,
          errors);
    } 
    
    Object itemSubGroupCategoryObj = params.get(INTEGRATION_SUB_GROUP_ID);
    List<String> itemSubGroupCategoriesList = null;
    if (itemSubGroupCategoryObj instanceof String) {
      itemSubGroupCategoriesList = new ArrayList<>();
      itemSubGroupCategoriesList.add((String) itemSubGroupCategoryObj);
    }
    if (itemSubGroupCategoryObj instanceof List) {
      itemSubGroupCategoriesList = (ArrayList<String>) itemSubGroupCategoryObj;
    }
    
    if (null != itemSubGroupCategoriesList && !itemSubGroupCategoriesList.isEmpty()) {
      Set<String> itemSubGroupCategoriesSet = new HashSet<>(itemSubGroupCategoriesList);
      saveOrUpdateItemSubGroup(responseData, (int) bean.get("medicine_id"),
          itemSubGroupCategoriesSet, errors);
    }

    String billingGroupId = (String) params.get("integration_group_id");
    if (null != billingGroupId && !"".equals(billingGroupId)) {
      BasicDynaBean billingGroupBean = taxGroupsIntegrationService
          .findByIntegrationId(billingGroupId);
      if (null != billingGroupBean) {
        bean.set("billing_group_id", (int) billingGroupBean.get("item_group_id"));
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { "integration_group_id", billingGroupId }));
      }
    }

  }

  /**
   * update item.
   * @param params the params
   * @return map
   */
  public Map<String, Object> updateItem(Map<String, Object> params) {

    Map<String, Object> responseData = new HashMap<>();
    List<String> errors = new ArrayList<>();
    List<String> info = new ArrayList<>();
    String endPoint = (String) params.get("endPoint");
    String customItemCode = (String) params.get(CUST_ITEM_CODE);

    Map<String, Object> keys = new HashMap<>();
    keys.put(CUST_ITEM_CODE, customItemCode);

    String medicineName = (String) params.get(MEDICINE_NAME);

    BasicDynaBean storeItemBean = null;
    BasicDynaBean medicineNameExists = null;
    if (null != customItemCode && !"".equals(customItemCode)) {
      storeItemBean = storeItemDetailsIntegrationRepository.findByKey(CUST_ITEM_CODE,
          customItemCode);

      if (storeItemBean != null && null != medicineName && !"".equals(medicineName)
          && !medicineName.equals(storeItemBean.get(MEDICINE_NAME))) {
        medicineNameExists = storeItemDetailsIntegrationRepository.findByKey(MEDICINE_NAME,
            medicineName);
      }

    } else {
      responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
      responseData.put(RETURN_CODE, "400");
      responseData.put("error-message",
          messageUtil.getMessage("exception.item.custom.item.code.mandatory"));
      return responseData;
    }

    if (null != storeItemBean && medicineNameExists == null) {
      ConversionUtils.copyToDynaBean(params, storeItemBean, errors);
      storeItemBean.set("updated_timestamp", DateUtil.getCurrentTimestamp());
      if (null != endPoint && "API".equals(endPoint)) {
        storeItemDetailsIntegrationValidator.mappedValidValueForUpdateItem(storeItemBean, info);
        checkValuesForIntegrationKeysInUpdate(params, storeItemBean, responseData, info, errors);
        if (errors.isEmpty()) {
          validator.validateUpdate(storeItemBean);
        }
      } else {
        validator.validateUpdate(storeItemBean);
      }

      if (errors.isEmpty()) {
        int returnedValue = storeItemDetailsIntegrationRepository.update(storeItemBean, keys);
        if (returnedValue == 1) {
          responseData.put(HTTP_STATUS, HttpStatus.ACCEPTED);
          responseData.put(RETURN_CODE, "202");
          responseData.put(RETURN_MESSAGE, messageUtil.getMessage("msg.item.updated.successfully"));
        }
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        responseData.put(RETURN_CODE, "400");
        responseData.put(RETURN_MESSAGE,
            messageUtil.getMessage("exception.validation.failed"));
      }
    } else {
      responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
      if (null == storeItemBean) {
        responseData.put(RETURN_CODE, "400");
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        responseData.put(RETURN_MESSAGE,
            messageUtil.getMessage("exception.item.custom.item.code.doesnot.exist"));
      } else {
        responseData.put(RETURN_CODE, "409");
        responseData.put(HTTP_STATUS, HttpStatus.CONFLICT);
        responseData.put(RETURN_MESSAGE, messageUtil.getMessage(
            "exception.item.name.mapped.to.other.custom.item.code", new Object[] { medicineName }));
      }
    }
    if (!info.isEmpty()) {
      responseData.put("INFO", info);
    }
    if (!errors.isEmpty()) {
      responseData.put("validationErrors", errors);
    }
    return responseData;

  }
  
  private void saveOrUpdateInsuranceCategory(Map<String, Object> responseData, int medicineId,
      Set<String> insuranceCategoryIds, List<String> errors) {
    List<BasicDynaBean> insertBeanList = new ArrayList<>();
    for (String insuranceCategoryId : insuranceCategoryIds) {
      BasicDynaBean insuranceCategoryBean = itemInsuranceCategoriesIntegrationService
          .findByIntegrationId(insuranceCategoryId);
      BasicDynaBean mappingBean = storeItemInsuranceCategoryMappingRepository.getBean();
      if (null != insuranceCategoryBean) {
        mappingBean.set("medicine_id", medicineId);
        mappingBean.set("insurance_category_id",
            insuranceCategoryBean.get("insurance_category_id"));
        insertBeanList.add(mappingBean);
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { INTEGRATION_INSU_CAT_ID, insuranceCategoryId }));
      }
    }
    if (errors.isEmpty()) {
      storeItemInsuranceCategoryMappingRepository.delete("medicine_id", medicineId);
      storeItemInsuranceCategoryMappingRepository.batchInsert(insertBeanList);
    }
  }
  
  private void saveOrUpdateItemSubGroup(Map<String, Object> responseData, int medicineId,
      Set<String> taxSubgroups, List<String> errors) {
    List<BasicDynaBean> insertBeanList = new ArrayList<>();
    for (String taxSubgroup : taxSubgroups) {
      BasicDynaBean taxSubgroupBean = taxSubGroupsIntegrationService
          .findByIntegrationId(taxSubgroup);
      BasicDynaBean mappingBean = storeItemSubGroupService.getBean();
      if (null != taxSubgroupBean) {
        mappingBean.set("medicine_id", medicineId);
        mappingBean.set("item_subgroup_id", taxSubgroupBean.get("item_subgroup_id"));
        insertBeanList.add(mappingBean);
      } else {
        responseData.put(HTTP_STATUS, HttpStatus.BAD_REQUEST);
        errors.add(messageUtil.getMessage(MAPPING_NOT_FOUND,
            new Object[] { INTEGRATION_SUB_GROUP_ID, taxSubgroup }));
      }
    }
    if (errors.isEmpty()) {
      storeItemSubGroupService.delete("medicine_id", medicineId);
      storeItemSubGroupService.batchInsert(insertBeanList);
    }
  }
  
  private void saveItemSubGroupForStoreTariff(Map<String, Object> responseData, int medicineId,
      Set<String> taxSubgroups, List<String> errors) {
    List<BasicDynaBean> insertBeanList = new ArrayList<>();
    for (String taxSubgroup : taxSubgroups) {
      BasicDynaBean taxSubgroupBean = taxSubGroupsIntegrationService
          .findByIntegrationId(taxSubgroup);
      if (null != taxSubgroupBean) {
        List<BasicDynaBean> listOfStoreTariff = storesRatePlanService.listAll();
        for (BasicDynaBean bean : listOfStoreTariff) {
          BasicDynaBean storeTariffItemSubgroupBean = storeTariffItemSubgroupService.getBean();
          storeTariffItemSubgroupBean.set("item_id", medicineId);
          storeTariffItemSubgroupBean.set("item_subgroup_id",
              taxSubgroupBean.get("item_subgroup_id"));
          storeTariffItemSubgroupBean.set("store_rate_plan_id", bean.get("store_rate_plan_id"));
          insertBeanList.add(storeTariffItemSubgroupBean);
        }
      }
    }
    if (errors.isEmpty()) {
      storeTariffItemSubgroupService.delete("item_id", medicineId);
      storeTariffItemSubgroupService.batchInsert(insertBeanList);
    }
  }

}
