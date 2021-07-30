package com.insta.hms.mdm.insuranceplans;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.mdm.MasterDetailsService;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.discountplans.DiscountPlanRepository;
import com.insta.hms.mdm.insurancecategory.InsuranceCategoryRepository;
import com.insta.hms.mdm.insurancecategory.InsuranceCategoryService;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyRepository;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyService;
import com.insta.hms.mdm.insuranceitemcategories.InsuranceItemCategoryService;
import com.insta.hms.mdm.insuranceplandetails.InsurancePlanDetailsRepository;
import com.insta.hms.mdm.insuranceplandetails.InsurancePlanDetailsService;
import com.insta.hms.mdm.organization.OrganizationRepository;
import com.insta.hms.mdm.tpas.TpaRepository;
import com.insta.hms.mdm.tpas.TpaService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: Auto-generated Javadoc
/** Class InsurancePlanService. */
@Service
public class InsurancePlanService extends MasterDetailsService {

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The insurance company service. */
  @LazyAutowired
  private InsuranceCompanyService insuranceCompanyService;

  /** The insurance category service. */
  @LazyAutowired
  private InsuranceCategoryService insuranceCategoryService;

  /** The insurance item category service. */
  @LazyAutowired
  private InsuranceItemCategoryService insuranceItemCategoryService;

  /** The tpa service. */
  @LazyAutowired
  private TpaService tpaService;

  /** The insurance plan repository. */
  @LazyAutowired
  private InsurancePlanRepository insurancePlanRepository;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The insurance plan service. */
  @LazyAutowired
  private InsurancePlanDetailsService insurancePlanDetailsService;

  /** The insurance plan details repository. */
  @LazyAutowired
  private InsurancePlanDetailsRepository insurancePlanDetailsRepository;

  /** The insurance company repository. */
  @LazyAutowired
  private InsuranceCompanyRepository insuranceCompanyRepository;

  /** The tpa repository. */
  @LazyAutowired
  private TpaRepository tpaRepository;

  /** The insurance category repository. */
  @LazyAutowired
  private InsuranceCategoryRepository insuranceCategoryRepository;

  /** The organization repository. */
  @LazyAutowired
  private OrganizationRepository organizationRepository;

  /** The discount plan repository. */
  @LazyAutowired
  private DiscountPlanRepository discountPlanRepository;

  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;
  
  /** The Constant CENTER_ID. */
  private static final String CENTER_ID = "centerId";

  /**
   * Instantiates a new insurance plan service.
   *
   * @param repository        the repository
   * @param validator         the validator
   * @param detailsRepository the detailsRepository
   */
  public InsurancePlanService(InsurancePlanRepository repository, InsurancePlanValidator validator,
      InsurancePlanDetailsRepository detailsRepository) {
    super(repository, validator, detailsRepository);
  }

  /**
   * List all.
   *
   * @param filterMap the filter map
   * @return the list
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<BasicDynaBean> listAll(Map filterMap) {
    return this.getRepository().listAll(null, filterMap, null);
  }

  /**
   * Gets the SPONSERS.
   *
   * @param categoryId the category id
   * 
   * @return sponsors
   */
  public List<BasicDynaBean> getSponserByCategory(Integer categoryId) {
    return ((InsurancePlanRepository) getRepository()).getSponserByCategory(categoryId);
  }
  
  /**
   * Gets the mapped plans.
   *
   * @param categoryId the category id
   * @param tpaId      the tpa id
   * @return the mapped plans
   */
  public List<BasicDynaBean> getMappedPlans(Integer categoryId, String tpaId) {
    return ((InsurancePlanRepository) getRepository()).getMappedPlans(categoryId, tpaId);
  }

  /**
   * Gets the plan default rate plan.
   *
   * @param planId the plan id
   * @return the plan default rate plan
   */
  public List<BasicDynaBean> getPlanDefaultRatePlan(Integer planId) {
    return ((InsurancePlanRepository) getRepository()).getPlanDefaultRatePlan(planId);
  }

  /**
   * Gets the plan names for sponsor.
   *
   * @param tpaId      the tpa id
   * @param categoryId the category id
   * @param centerId   the center id
   * @return the plan names for sponsor
   */
  public List<BasicDynaBean> getPlanNamesForSponsor(String tpaId, Integer categoryId,
      Integer centerId) {
    return ((InsurancePlanRepository) getRepository()).getPlanNamesForSponsor(tpaId, categoryId,
        centerId);
  }

  /**
   * Gets the search query assembler.
   *
   * @param params        the params
   * @param listingParams the listing params
   * @return the search query assembler
   */
  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterService#getSearchQueryAssembler(java.util.Map,
   * java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected SearchQueryAssembler getSearchQueryAssembler(Map params,
      Map<LISTING, Object> listingParams) {
    int centerId = RequestContext.getCenterId();
    int totalCenters = (Integer) genericPreferencesService.getAllPreferences()
        .get("max_centers_inc_default");
    return ((InsurancePlanRepository) getRepository()).getInsurancePlanMainDetails(params,
        listingParams, centerId, totalCenters);
  }

  /**
   * Gets the adds the show page data.
   *
   * @param paramMap the param map
   * @return the adds the show page data
   */
  @SuppressWarnings("rawtypes")
  public Map<String, List<BasicDynaBean>> getAddShowPageData(Map paramMap) {

    Map<String, List<BasicDynaBean>> addEditReference = new HashMap<String, List<BasicDynaBean>>();

    List<BasicDynaBean> itemCatList = insuranceItemCategoryService.getItemInsuranceCategory();

    addEditReference.put("insuPayableList", itemCatList);
    addEditReference.put("tpaMasterLists", tpaService.getTpasNamesAndIds());
    int totalCenters = (Integer) genericPreferencesService.getAllPreferences()
        .get("max_centers_inc_default");
    addEditReference.put("categoryLists",
        insuranceCategoryService.getInsuranceCategoryActiveList(totalCenters));
    addEditReference.put("insuranceCompaniesTpaLists", tpaService.getCompanyTpaList());
    addEditReference.put("defaultDiscountPlanList",
        InsurancePlanRepository.getDefaultDiscountPlanList());
    BasicDynaBean planBean = null;
    if (((String[]) paramMap.get("plan_id")) != null) {

      int[] planIds = new int[((String[]) paramMap.get("plan_id")).length];
      int planId = 0;
      for (int i = 0; i < planIds.length; i++) {
        planIds[i] = Integer.parseInt(((String[]) paramMap.get("plan_id"))[i]);
        planId = planIds[i];
      }

      planBean = ((InsurancePlanRepository) getRepository()).findPlan("plan_id", planId);
      List<BasicDynaBean> planDetailsbean = ((InsurancePlanDetailsRepository) getDetailRepository()
          .get(0)).getAllPlanCharges((Integer) (planBean.get("plan_id")));
      addEditReference.put("chargeBean", planDetailsbean);
      addEditReference.put("planLists", ((InsurancePlanRepository) getRepository()).lookup(true));
    } else {
      // addEditReference.put(
      // "chargeBean",
      // ((InsurancePlanDetailsRepository)
      // getDetailRepository().get(0)).getAllPlanCharges());
      addEditReference.put("insuranceCompaniesLists",
          insuranceCompanyService.getInsuranceCompaniesNamesAndIds());
    }

    return addEditReference;
  }

  /**
   * To bean.
   *
   * @param requestParams the request params
   * @param fileMap       the file map
   * @return the basic dyna bean
   */
  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterService#toBean(java.util.Map, java.util.Map)
   */
  @Override
  public BasicDynaBean toBean(Map<String, String[]> requestParams,
      Map<String, MultipartFile> fileMap) {
    BasicDynaBean bean = super.toBean(requestParams, fileMap);
    Map session = sessionService.getSessionAttributes();
    bean.set("username", session.get("userId"));
    bean.set("mod_time", DateUtil.getCurrentTimestamp());
    return bean;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterService#toBeanList(java.util.Map,
   * org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public Map<String, List<BasicDynaBean>> toBeanList(Map<String, String[]> requestParams,
      BasicDynaBean type) {
    Map<String, List<BasicDynaBean>> beanListMap = super.toBeanList(requestParams, type);
    Map session = sessionService.getSessionAttributes();
    List<MasterRepository<?>> detailsRepositories = getDetailRepository();
    if (detailsRepositories == null || detailsRepositories.isEmpty()) {
      return beanListMap;
    }
    MasterRepository<?> detailRepo = detailsRepositories.get(0);
    List<BasicDynaBean> insertedBeans = beanListMap.get(detailRepo.getBeanName() + "_inserted");
    List<BasicDynaBean> updatedBeans = beanListMap.get(detailRepo.getBeanName() + "_updated");
    for (BasicDynaBean bean : insertedBeans) {
      bean.set("username", session.get("userId"));
    }
    for (BasicDynaBean bean : updatedBeans) {
      bean.set("username", session.get("userId"));
    }
    return beanListMap;
  }

  /**
   * Gets the charge amt for plan.
   *
   * @param planId     the plan id
   * @param categoryId the category id
   * @param visitType  the visit type
   * @return the charge amt for plan
   */
  public BasicDynaBean getChargeAmtForPlan(int planId, int categoryId, String visitType) {
    return ((InsurancePlanRepository) getRepository()).getChargeAmtForPlan(planId, categoryId,
        visitType);
  }

  /**
   * Find by key.
   *
   * @param keys the keys
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(Map keys) {
    return insurancePlanRepository.findByKey(keys);
  }

  /**
   * Find by key.
   * 
   * @param planId the plan id
   * @return the insurance plan bean
   */
  public BasicDynaBean findByKey(int planId) {
    return insurancePlanRepository.findByKey("plan_id", planId);
  }
  
  /**
   * Find by criteria.
   *
   * @param criteria the criteria
   * @return the list of basic dyna beans
   */
  public List<BasicDynaBean> findByCriteria(Map criteria) {
    return insurancePlanRepository.findByCriteria(criteria);
  }

  /**
   * Gets the insu cat mapped to insu comp.
   *
   * @param insuranceCoId the insurance co id
   * @return the insu cat mapped to insu comp
   */
  public List<BasicDynaBean> getInsuCatMappedToInsuComp(String insuranceCoId) {
    return ((InsurancePlanDetailsRepository) getDetailRepository().get(0))
        .getAllPlanCharges(insuranceCoId);
  }

  /**
   * Gets the cat id based on plan ids.
   *
   * @param query       the query
   * @param listItemIds the list item ids
   * @param planIds     the plan ids
   * @param visitType   the visit type
   * @return the cat id based on plan ids
   */
  public List<BasicDynaBean> getCatIdBasedOnPlanIds(String query, List<String> listItemIds,
      Set<Integer> planIds, String visitType) {
    return insurancePlanRepository.getCatIdBasedOnPlanIds(query, listItemIds, planIds, visitType);
  }

  /**
   * Check is general category exists for reg charges.
   *
   * @param visitId   the visit id
   * @param planId    the plan id
   * @param visitType the visit type
   * @return the boolean
   */
  public Boolean checkIsGeneralCategoryExistsForRegCharges(String visitId, int planId,
      String visitType) {
    return insurancePlanRepository.checkIsGeneralCategoryExistsForRegCharges(visitId, planId,
        visitType);
  }

  /**
   * Get plan by categoryId and insuranceCompanyId.
   *
   * @param categoryId         identifier of insurance category
   * @param insuranceCompanyId identifier of insurance company
   * @return response map
   */
  public Map<String, Object> getPlansByCategory(Integer categoryId, String insuranceCompanyId) {
    Map<String, Object> result = new HashMap<>();
    result.put("planList", ConversionUtils.listBeanToListMap(
        insurancePlanRepository.getPlansByCategory(categoryId, insuranceCompanyId)));
    return result;
  }

  /**
   * Gets the plans by category and company id.
   *
   * @param categoryId         the category id
   * @param insuranceCompanyId the insurance company id
   * @return the plans by category and company id
   */
  public Map<String, Object> getPlansByCategoryAndSponsor(Integer categoryId,
      String insuranceCompanyId, String sponsorId) {
    Map<String, Object> result = new HashMap<>();
    result.put("planList", ConversionUtils.listBeanToListMap(insurancePlanRepository
        .getPlansByCategoryAndCompanyId(categoryId, insuranceCompanyId, sponsorId)));
    return result;
  }
  
  /**
   * Get Categories by insurance company id.
   *
   * @param insuranceCompanyId identifier of insurance company
   * @return response map
   */
  public Map<String, Object> getCategoriesByCompanyId(String insuranceCompanyId, String sponsorId) {
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get(CENTER_ID);
    Map<String, Object> result = new HashMap<>();
    
    if ( centerId > 0 ) {
      result.put("category_details", ConversionUtils
          .listBeanToListMap(insurancePlanRepository
                 .getCategoriesByCompanyId(insuranceCompanyId, sponsorId,centerId)));
    } else {
      result.put("category_details", ConversionUtils
          .listBeanToListMap(insurancePlanRepository
                  .getCategoriesByCompanyId(insuranceCompanyId, sponsorId)));
    }
    return result;
  }

  /**
   * Get TPA Details from PlanCode.
   * 
   * @param planCode   - Plan code associated with the Insurance plan
   * @param categoryId - Patient Category ID
   * @param visitType  - OP/IP
   * @return list of tpa details as DynaBeans
   */
  public List<BasicDynaBean> getTPADetailsFromCode(String planCode, Integer categoryId,
      String visitType) {
    return insurancePlanRepository.getTpaDetailsFromPlanCode(planCode, categoryId, visitType);
  }

  /**
   * Gets the plans by category ids.
   *
   * @param categoryIdsStrArr the category ids str arr
   * @return the plans by category ids
   */
  public Map<String, Object> getPlansByCategoryIdsAndSponsor(String[] categoryIdsStrArr,
      String sponsorId, String searchQuery, Integer limit) {
    Integer[] categoryIds = new Integer[categoryIdsStrArr.length];
    List<Integer> categoryIdList = new ArrayList<>();
    for (int i = 0; i < categoryIdsStrArr.length; i++) {
      categoryIdList.add(Integer.parseInt(categoryIdsStrArr[i]));
      categoryIds[i] = Integer.parseInt(categoryIdsStrArr[i]);
    }
    List<BasicDynaBean> planList = insurancePlanRepository
        .getPlansByCategoryIds(categoryIdList, sponsorId, searchQuery, limit);

    Map<String, Object> resultMap = new HashMap<>();
    resultMap.put("planList", ConversionUtils.listBeanToListMap(planList));
    return resultMap;
  }

  /**
   * Gets the plan type list by sponsor.
   *
   * @param sponsorId the sponsor id
   * @return the plan type list by sponsor
   */
  public Map<String, Object> getPlanTypeListBySponsor(String sponsorId) {
    
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get(CENTER_ID);
    List<BasicDynaBean> planTypeList = null;
    
    if ( centerId > 0 ) {
      planTypeList = insurancePlanRepository.getPlanTypeListBySponsor(sponsorId,centerId);
    } else {
      planTypeList = insurancePlanRepository.getPlanTypeListBySponsor(sponsorId);
    }

    Map<String, Object> resultMap = new HashMap<>();
    resultMap.put("planTypeList", ConversionUtils.listBeanToListMap(planTypeList));
    return resultMap;
  }

  /**
   * imports bulk insurance plans.
   * 
   * @param map the map
   */
  public void importBulkInsurancePlans(Map<String, List<Map<String, Object>>> map) {
    List<Map<String, Object>> planDetailsSheetData = map.get("PlanDetails");
    List<Map<String, Object>> copayLimitOPSheetData = map.get("Copay_Limit_OP");
    List<Map<String, Object>> copayLimitIPSheetData = map.get("Copay_Limit_IP");

    BasicDynaBean insMainBean = null;
    BasicDynaBean insDetailsBean = null;
    List<BasicDynaBean> insDetailsBeanList = null;
    Map<String, Object> insuranceBean = null;
    List<Map<String, Object>> insuranceBeanList = new ArrayList<Map<String, Object>>();
    List<String> errors = new ArrayList<String>();

    for (Map<String, Object> insPlan : planDetailsSheetData) {
      try {
        insuranceBean = new HashMap<String, Object>();
        insMainBean = validateInsPlanMainDataAndConvertToBean(insPlan);
        for (Map<String, Object> checkExistance : insuranceBeanList) {
          BasicDynaBean exInsBean = (BasicDynaBean) checkExistance.get("insurance_plan_main");
          if (exInsBean.get("plan_name").equals(insMainBean.get("plan_name"))) {
            errors.add("Contains duplicate insuramce plan : " + insMainBean.get("plan_name"));
          }
        }
        insuranceBean.put("insurance_plan_main", insMainBean);
        insDetailsBeanList = new ArrayList<BasicDynaBean>();
        for (Map<String, Object> oplimit : copayLimitOPSheetData) {
          if (oplimit.get("plan_name").toString()
              .equalsIgnoreCase(insPlan.get("plan_name").toString())) {
            oplimit.put("patient_type", "o");
            insDetailsBean = insurancePlanDetailsService
                .validateInsPlanDetailsDataAndConvertToBean(oplimit);
            insDetailsBeanList.add(insDetailsBean);
          }
        }

        for (Map<String, Object> iplimit : copayLimitIPSheetData) {
          if (iplimit.get("plan_name").toString()
              .equalsIgnoreCase(insPlan.get("plan_name").toString())) {
            iplimit.put("patient_type", "i");
            insDetailsBean = insurancePlanDetailsService
                .validateInsPlanDetailsDataAndConvertToBean(iplimit);
            insDetailsBeanList.add(insDetailsBean);
          }
        }
        if (insDetailsBeanList.isEmpty()) {
          errors.add("No Copay Limit Data for OP or IP :" + insMainBean.get("plan_name"));
        } else {
          insuranceBean.put("insurance_plan_details", insDetailsBeanList);
          insuranceBeanList.add(insuranceBean);
        }
      } catch (HMSException exception) {
        errors.addAll(exception.getErrorsList());
      }
    }

    if (errors.isEmpty()) {
      insertBulkInsuranceBeans(insuranceBeanList);
    } else {
      throw new HMSException(errors);
    }
  }

  /**
   * imports bulk insuranc plan beans.
   * 
   * @param list the list of insurance beans
   */
  @SuppressWarnings("unchecked")
  @Transactional(rollbackFor = Exception.class)
  private void insertBulkInsuranceBeans(List<Map<String, Object>> list) {
    BasicDynaBean insurancePlanMainBean = null;
    Map<String, List<BasicDynaBean>> insurancePlanDetailsBeans = null;
    TransactionStatus insTs = DatabaseHelper.startTransaction("ins_plan_upload_begin");
    boolean validData = true;
    List<String> errors = new ArrayList<String>();
    for (Map<String, Object> insBean : list) {
      insurancePlanDetailsBeans = new HashMap<String, List<BasicDynaBean>>();
      insurancePlanMainBean = (BasicDynaBean) insBean.get("insurance_plan_main");
      insurancePlanDetailsBeans.put("insurance_plan_details_inserted",
          (List<BasicDynaBean>) insBean.get("insurance_plan_details"));
      try {
        insertDetails(insurancePlanMainBean, insurancePlanDetailsBeans);
      } catch (Exception exception) {
        validData = false;
        errors.add(exception.toString() + insurancePlanMainBean.get("plan_name"));
      }
    }
    if (validData) {
      DatabaseHelper.commit(insTs);
    } else {
      DatabaseHelper.rollback(insTs);
      throw new HMSException(errors);
    }
  }

  /**
   * validates insurance data and convert to bean.
   * 
   * @param map the map
   * @return dynabean of insurance
   */
  private BasicDynaBean validateInsPlanMainDataAndConvertToBean(Map<String, Object> map) {
    BasicDynaBean bean = insurancePlanRepository.getBean();
    List<String> errors = new ArrayList<String>();
    Map<String, Object> tempMap = null;
    BasicDynaBean tempBean = null;
    try {
      map.put("username", RequestContext.getUserName());
      // insurance company name
      tempBean = insuranceCompanyRepository.findByKey("insurance_co_name",
          map.get("insurance_company_name"));
      if (tempBean != null) {
        map.put("insurance_co_id", tempBean.get("insurance_co_id"));
      } else {
        errors.add("Invalid insurance_company_name : " + map.get("insurance_company_name"));
      }

      // Status
      if (map.get("status") != null) {
        String status = map.get("status").toString();
        if (status.equalsIgnoreCase("Active") || status.equalsIgnoreCase("A")) {
          map.put("status", "A");
        } else if (status.equalsIgnoreCase("Inactive") || status.equalsIgnoreCase("I")) {
          map.put("status", "I");
        } else {
          errors.add("Invalid status " + status);
        }
      } else {
        map.put("status", "A");
      }

      // sponsor
      if (map.get("sponsor") != null) {
        tempBean = tpaRepository.findByKey("tpa_name", map.get("sponsor"));
        if (tempBean != null) {
          map.put("sponsor_id", tempBean.get("tpa_id"));
        } else {
          errors.add("Invalid sponsor :" + map.get("sponsor"));
        }
      }

      // Plan Type
      if (map.get("plan_type") != null) {
        tempMap = new HashMap<String, Object>();
        tempMap.put("insurance_co_id", map.get("insurance_co_id"));
        tempMap.put("category_name", map.get("plan_type"));
        tempBean = insuranceCategoryRepository.findByKey(tempMap);
        if (tempBean != null) {
          map.put("category_id", tempBean.get("category_id"));
        } else {
          errors.add("Invalid plan_type :" + map.get("plan_type"));
        }
      }

      // Default Rate Plan
      if (map.get("default_rate_plan") != null) {
        tempBean = null;
        tempBean = organizationRepository.findByKey("org_name", map.get("default_rate_plan"));
        if (tempBean != null) {
          map.put("default_rate_plan", tempBean.get("org_id"));
        } else {
          errors.add("Invalid default_rate_plan :" + map.get("default_rate_plan"));
        }
      }

      // Default Discount Plan
      if (map.get("default_discount_plan") != null) {
        tempBean = discountPlanRepository.findByKey("discount_plan_name",
            map.get("default_discount_plan"));
        if (tempBean != null) {
          map.put("discount_plan_id", tempBean.get("discount_plan_id"));
        } else {
          errors.add("Invalid default_discount_plan :" + map.get("default_discount_plan"));
        }
      }

      // Co-pay % applicable on post-discounted amount
      if (map.get("copay_percent_applicable_on_post_discounted_amount").toString()
          .equalsIgnoreCase("Yes")
          || map.get("copay_percent_applicable_on_post_discounted_amount").toString()
              .equalsIgnoreCase("Y")) {
        map.put("is_copay_pc_on_post_discnt_amt", "Y");
      } else {
        map.put("is_copay_pc_on_post_discnt_amt", "N");
      }

      // IP-Applicable
      if (map.get("ip_applicable").toString().equalsIgnoreCase("Yes")
          || map.get("ip_applicable").toString().equalsIgnoreCase("Y")) {
        map.put("ip_applicable", "Y");
      } else {
        map.put("ip_applicable", "N");
      }

      // OP-Applicable
      if (map.get("op_applicable").toString().equalsIgnoreCase("Yes")
          || map.get("op_applicable").toString().equalsIgnoreCase("Y")) {
        map.put("op_applicable", "Y");
      } else {
        map.put("op_applicable", "N");
      }

      // Limit Type
      if (map.get("limit_type").toString().equalsIgnoreCase("Category Based")) {
        map.put("limit_type", "C");
      } else if (map.get("limit_type").toString().equalsIgnoreCase("Case rate Based")) {
        map.put("limit_type", "R");
        if ((map.get("number_of_case_rates_allowed") != null)
            && (map.get("number_of_case_rates_allowed").toString().equals("1")
                || map.get("number_of_case_rates_allowed").toString().equals("2"))) {
          map.put("case_rate_count", map.get("number_of_case_rates_allowed"));
        } else {
          errors.add(
              "Invalid number_of_case_rates_allowed :" + map.get("number_of_case_rates_allowed"));
        }
      } else {
        errors.add("Invalid Limit_Type :" + map.get("limit_type"));
      }

      // Limits Include Follow-up Visits
      if (map.get("limits_include_followup") == null) {
        map.put("limits_include_followup", "N");
        map.put("op_episode_limit", "0.00");
      } else {
        if (map.get("limits_include_followup").toString().equalsIgnoreCase("Yes")
            || map.get("limits_include_followup").toString().equalsIgnoreCase("Y")) {
          map.put("limits_include_followup", "Y");
          if (map.get("op_episode_limit") == null) {
            map.put("op_episode_limit", "0.00");
          }
        } else {
          map.put("limits_include_followup", "N");
          map.put("op_episode_limit", "0.00");
        }
      }

      // OP Plan Limit
      if (map.get("op_plan_limit") == null) {
        map.put("op_plan_limit", "0.00");
      }

      // OP Visit Sponsor Limit
      if (map.get("op_visit_limit") == null) {
        map.put("op_visit_limit", "0.00");
      }

      // OP Visit Deductible
      if (map.get("op_visit_deductible") == null) {
        map.put("op_visit_deductible", "0.00");
      }

      // OP Visit Copay
      if (map.get("op_copay_percent") == null) {
        map.put("op_copay_percent", "0.00");
      }

      // OP Visit Max Copay
      if (map.get("op_visit_copay_limit") == null) {
        map.put("op_visit_copay_limit", "0.00");
      }

      // IP Plan Limit
      if (map.get("ip_plan_limit") == null) {
        map.put("ip_plan_limit", "0.00");
      }

      // IP Visit Sponsor Limit
      if (map.get("ip_visit_limit") == null) {
        map.put("ip_visit_limit", "0.00");
      }

      // IP Per Day Limit
      if (map.get("ip_per_day_limit") == null) {
        map.put("ip_per_day_limit", "0.00");
      }

      // IP Visit Deductible
      if (map.get("ip_visit_deductible") == null) {
        map.put("ip_visit_deductible", "0.00");
      }

      // IP Visit Copay
      if (map.get("ip_copay_percent") == null) {
        map.put("ip_copay_percent", "0.00");
      }

      // IP Visit Max Copay
      if (map.get("ip_visit_copay_limit") == null) {
        map.put("ip_visit_copay_limit", "0.00");
      }

      // Plan code
      if ((map.get("plan_code") != null) && (map.get("plan_code").toString().length() < 4)) {
        errors.add("Plan code must be atleast 4 characters in length :" + map.get("plan_name"));
      }

      // Add On Payment Factor
      if (map.get("add_on_payment_factor") != null) {
        int addOnPaymentFactor = Integer.parseInt(map.get("add_on_payment_factor").toString());
        if (addOnPaymentFactor < 0 && addOnPaymentFactor > 100) {
          errors.add("Add On Payment factor should be between 0-100 :" + map.get("plan_name"));
        } else {
          map.put("add_on_payment_factor", addOnPaymentFactor);
        }
      } else {
        map.put("add_on_payment_factor", 75.00);
      }

      map.put("require_pbm_authorization", "N");
      ConversionUtils.copyJsonToDynaBean(map, bean, errors, true);
    } catch (Exception exception) {
      errors.add(messageUtil.getMessage("exception.insurance.plan.column.not.available"));
    }
    if (errors.isEmpty()) {
      return bean;
    } else {
      throw new HMSException(errors);
    }
  }
}
