package com.insta.hms.mdm.discountplans;

import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.billing.BillClaimService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.inventory.stocks.StoreCategoryService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.mdm.MasterDetailsService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;
import com.insta.hms.mdm.insuranceitemcategories.InsuranceItemCategoryService;
import com.insta.hms.mdm.insuranceplandetails.InsurancePlanDetailsService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DiscountPlanService.
 */
@Service("discountPlanService")
public class DiscountPlanService extends MasterDetailsService {

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The insurance item category service. */
  @LazyAutowired
  private InsuranceItemCategoryService insuranceItemCategoryService;

  /** The charge heads service. */
  @LazyAutowired
  private ChargeHeadsService chargeHeadsService;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The bill service. */
  @LazyAutowired
  private BillService billService;

  /** The bill claim service. */
  @LazyAutowired
  private BillClaimService billClaimService;

  /** The patient insurance plans service. */
  @LazyAutowired
  private PatientInsurancePlansService patientInsurancePlansService;

  /** The insurance plan service. */
  @LazyAutowired
  private InsurancePlanService insurancePlanService;

  /** The insurance plan details service. */
  @LazyAutowired
  private InsurancePlanDetailsService insurancePlanDetailsService;

  /** The discount plan details repo. */
  private DiscountPlanDetailsRepository discountPlanDetailsRepo;

  /** Store Category Service. */
  @LazyAutowired
  private StoreCategoryService storeCatService;

  /**
   * Instantiates a new discount plan service.
   *
   * @param discountPlanRepository
   *          the discount plan repository
   * @param discountPlanValidator
   *          the discount plan validator
   * @param discountPlanDetailsRepository
   *          the discount plan details repository
   */
  public DiscountPlanService(DiscountPlanRepository discountPlanRepository,
      DiscountPlanValidator discountPlanValidator,
      DiscountPlanDetailsRepository discountPlanDetailsRepository) {
    super(discountPlanRepository, discountPlanValidator, discountPlanDetailsRepository);
    this.discountPlanDetailsRepo = discountPlanDetailsRepository;
  }

  /**
   * Gets the list page lookup.
   *
   * @param params
   *          the params
   * @return the list page lookup
   */
  @SuppressWarnings("rawtypes")
  public Map<String, List<BasicDynaBean>> getListPageLookup(Map params) {
    Map<String, List<BasicDynaBean>> listReference = new HashMap<String, List<BasicDynaBean>>();
    List<BasicDynaBean> genPrefs = new ArrayList<BasicDynaBean>();
    genPrefs.add(genericPreferencesService.getPreferences());
    listReference.put("genPrefs", genPrefs);
    listReference.put("insuranceCategoryList", insuranceItemCategoryService.lookup(false));
    listReference.put("centers", centerService.listAll(true));
    return listReference;
  }

  /**
   * Gets the adds the show page data.
   *
   * @param paramMap
   *          the param map
   * @return the adds the show page data
   */
  @SuppressWarnings("rawtypes")
  public Map<String, List<BasicDynaBean>> getAddShowPageData(Map paramMap) {

    Map<String, List<BasicDynaBean>> addEditReference = new HashMap<String, List<BasicDynaBean>>();
    List<BasicDynaBean> genPrefs = new ArrayList<BasicDynaBean>();
    genPrefs.add(genericPreferencesService.getPreferences());
    addEditReference.put("genPrefs", genPrefs);
    List<String> insuranceCategoryList = new ArrayList<String>();
    insuranceCategoryList.add("insurance_category_id");
    insuranceCategoryList.add("insurance_category_name");
    Map<String, Object> filterInsuranceCategory = new HashMap<String, Object>();
    filterInsuranceCategory.put("insurance_payable", "Y");
    List<String> itemCategoryList = new ArrayList<String>();
    itemCategoryList.add("category_id");
    itemCategoryList.add("category");
    Map<String, Object> filterItemCategory = new HashMap<String, Object>();
    filterItemCategory.put("status", "A");
    addEditReference.put("itemCategoryList",
        storeCatService.listAll(itemCategoryList, filterItemCategory, "category"));
    addEditReference.put("insuranceCategoryList",
        insuranceItemCategoryService.listInsuranceCategory(insuranceCategoryList,
            filterInsuranceCategory, "insurance_category_name"));
    addEditReference.put("chargeHeadList", chargeHeadsService.getDiscountApplicableChargeHead());
    addEditReference.put("discountPlanbean", ((DiscountPlanRepository) getRepository()).listAll());
    return addEditReference;
  }

  /**
   * List all.
   *
   * @param keys
   *          the keys
   * @param sortColumn
   *          the sort column
   * @return the list
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<BasicDynaBean> listAll(Map keys, String sortColumn) {
    return this.getRepository().listAll(null, keys, sortColumn);
  }

  /**
   * List all discount plan details.
   *
   * @param columns the columns
   * @param filterBy the filter by
   * @param filterValue the filter value
   * @param sortColumn the sort column
   * @return the list
   */
  public List<BasicDynaBean> listAllDiscountPlanDetails(List<String> columns, String filterBy,
      Object filterValue, String sortColumn) {
    return discountPlanDetailsRepo.listAll(columns, filterBy, filterValue, sortColumn);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected SearchQueryAssembler getSearchQueryAssembler(Map params,
      Map<LISTING, Object> listingParams) {

    return ((DiscountPlanRepository) getRepository()).getDiscountPlanMainDetailsQa(params,
        listingParams);
  }

  /**
   * List discount plan details.
   *
   * @param keys
   *          the keys
   * @param sortColumn
   *          the sort column
   * @return the list
   */
  public List<BasicDynaBean> listDiscountPlanDetails(Map keys, String sortColumn) {
    return getDetailRepository().get(0).listAll(null, keys, sortColumn);
  }

  /**
   * Apply discount rule.
   *
   * @param charge
   *          the charge
   * @param billDiscountPlanId
   *          the bill discount plan id
   * @param discountPlanDetails
   *          the discount plan details
   * @param visitType
   *          the visit type
   */
  public void applyDiscountRule(BasicDynaBean charge, Integer billDiscountPlanId,
      List<BasicDynaBean> discountPlanDetails, String visitType) {
    boolean discountCategoryExists = false;
    if (0 != billDiscountPlanId) {
      discountCategoryExists = billDiscountPlanId != 0;
      boolean isItemCategoryPayable = isItemCategoryPayable(charge, visitType);
      Boolean isDoctorExcluded = null;
      if (null != charge.get("item_excluded_from_doctor")) {
        isDoctorExcluded = (Boolean) charge.get("item_excluded_from_doctor");
      }
      // gets suitable discount rule for the charge
      BasicDynaBean discountRule = getDiscountRule(charge, discountPlanDetails);
      if (discountCategoryExists && discountRule != null && (isDoctorExcluded != null
          ? !isDoctorExcluded : isItemCategoryPayable)) {
        BigDecimal disPerc = BigDecimal.ZERO;
        BigDecimal disaAmt = BigDecimal.ZERO;

        BigDecimal actRate = (BigDecimal) charge.get("act_rate");
        BigDecimal actQuantity = (BigDecimal) charge.get("act_quantity");

        if (discountRule != null) {

          if (((String) discountRule.get("discount_type")).equals("P")) {
            disPerc = (BigDecimal) discountRule.get("discount_value");
          }

          if (((String) discountRule.get("discount_type")).equals("A")) {
            disaAmt = (BigDecimal) discountRule.get("discount_value");
            int result = disaAmt.compareTo((actRate.multiply(actQuantity)));
            if (result != -1) {
              disaAmt = actRate.multiply(actQuantity);
            } else {
              disaAmt = (BigDecimal) discountRule.get("discount_value");
            }
          } else {
            disaAmt = actRate.multiply(actQuantity).multiply(disPerc).divide(new BigDecimal(100));
          }
        }
        // amount might be after rate plan discount.Hence calculating
        // amount again.Overriding rate plan discount.
        BigDecimal amt = actRate.multiply(actQuantity);
        amt = amt.subtract(disaAmt);
        charge.set("amount", amt);
        charge.set("discount", disaAmt);
        charge.set("overall_discount_amt", disaAmt);
        charge.set("overall_discount_auth", 0);
      }
    }

  }

  /**
   * Gets the discount rule.
   *
   * @param charge
   *          the charge
   * @param discountPlanDetails
   *          the discount plan details
   * @return the discount rule
   */
  public BasicDynaBean getDiscountRule(BasicDynaBean charge,
      List<BasicDynaBean> discountPlanDetails) {
    BasicDynaBean discountRuleBean = null;

    /*
     * applicable_type tells on which to apply discount rule.It can have 3 values. N : insurance
     * category id of item in the charge C : charge head of the charge I : item id of the charge. :
     * if it is item id there is one more parameter which will decide which type of item to look at
     * it.
     */
    for (BasicDynaBean detailBean : discountPlanDetails) {
      String applicableToId = ((String) detailBean.get("applicable_to_id")).trim();
      String applicableType = (String) detailBean.get("applicable_type");
      if (("N".equals(applicableType)
          && (Integer) charge.get("insurance_category_id") == Integer.parseInt(applicableToId))
          || ("C".equals(applicableType) && charge.get("charge_head").equals(applicableToId))
          || ("I".equals(applicableType)
              && charge.get("act_description_id").equals(applicableToId))) {
        discountRuleBean = detailBean;
        break;
      }
    }
    return discountRuleBean;
  }

  /**
   * Sets the discount rule.
   *
   * @param charge
   *          the charge
   * @param discountRule
   *          the discount rule
   */
  public void setDiscountRule(BasicDynaBean charge, BasicDynaBean discountRule) {
    BigDecimal disPerc = BigDecimal.ZERO;
    BigDecimal disaAmt = BigDecimal.ZERO;
    BigDecimal actRate = (BigDecimal) charge.get("act_rate");
    BigDecimal actQuantity = (BigDecimal) charge.get("act_quantity");

    if (discountRule != null) {

      if (((String) discountRule.get("discount_type")).equals("P")) {
        disPerc = (BigDecimal) discountRule.get("discount_value");
      }

      if (((String) discountRule.get("discount_type")).equals("A")) {
        disaAmt = (BigDecimal) discountRule.get("discount_value");
      } else {
        disaAmt = (actRate.multiply(actQuantity)).multiply(disPerc).divide(new BigDecimal(100));
      }
    }

    BigDecimal amt = actRate.multiply(actQuantity);

    if ((disaAmt.compareTo(amt)) != -1) {
      disaAmt = amt;
    }
    amt = amt.subtract(disaAmt);
    charge.set("amount", amt);
    charge.set("discount", disaAmt);
    charge.set("overall_discount_amt", disaAmt);
    charge.set("overall_discount_auth", 0);
  }

  /**
   * Checks if is item category payable.
   *
   * @param charge
   *          the charge
   * @param visitType
   *          the visit type
   * @return true, if is item category payable
   */
  public boolean isItemCategoryPayable(BasicDynaBean charge, String visitType) {
    String billNo = (String) charge.get("bill_no");
    if (null != billNo) {
      BasicDynaBean billBean = billService.findByKey("bill_no", billNo);
      boolean isTpa = (Boolean) billBean.get("is_tpa");
      if (isTpa) {
        Map<String, Object> keyMap = new HashMap<String, Object>();
        keyMap.put("bill_no", billNo);
        keyMap.put("priority", 1);

        BasicDynaBean bclBean = billClaimService.findByKey(keyMap);
        int planId = 0;
        String patientId = (String) billBean.get("visit_id");

        if (null != bclBean) {
          planId = (Integer) bclBean.get("plan_id");
        }
        if (null == bclBean && null != patientId) {
          Map<String, Object> paramsKeys = new HashMap<String, Object>();
          paramsKeys.put("patient_id", patientId);
          paramsKeys.put("priority", 1);
          BasicDynaBean patientPlanBean = patientInsurancePlansService.findByKey(paramsKeys);
          planId = (Integer) patientPlanBean.get("plan_id");
        }

        if (planId != 0) {
          Map<String, Object> paramKeys = new HashMap<String, Object>();
          paramKeys.put("plan_id", planId);
          BasicDynaBean planBean = insurancePlanService.findByPk(paramKeys);
          int discPlanId = planBean.get("discount_plan_id") != null
              ? (Integer) planBean.get("discount_plan_id") : 0;
          if (discPlanId > 0) {
            keyMap.clear();
            keyMap.put("plan_id", planId);
            keyMap.put("insurance_category_id", charge.get("insurance_category_id"));
            keyMap.put("patient_type", visitType);
            List<BasicDynaBean> planDetailsBeanList = insurancePlanDetailsService.listAll(keyMap);
            BasicDynaBean planDetBean = planDetailsBeanList != null ? planDetailsBeanList.get(0)
                : null;
            if (planDetBean != null) {
              String isCategoryPayable = (String) planDetBean.get("category_payable");
              if (isCategoryPayable.equals("N")) {
                return false;
              }
            }
          }
        }
      }
    }
    return true;
  }

  /**
   * Checks if is item category payable.
   *
   * @param planId
   *          the plan id
   * @param visitType
   *          the visit type
   * @param insCatId
   *          the ins cat id
   * @param isTpaBill
   *          the is tpa bill
   * @return true, if is item category payable
   */
  public boolean isItemCategoryPayable(int planId, String visitType, int insCatId,
      boolean isTpaBill) {
    if (planId > 0) {
      Map<String, Object> keyMap = new HashMap<String, Object>();
      Map<String, Object> paramKeys = new HashMap<String, Object>();
      paramKeys.put("plan_id", planId);
      BasicDynaBean planBean = insurancePlanService.findByPk(paramKeys);
      int discPlanId = planBean.get("discount_plan_id") != null
          ? (Integer) planBean.get("discount_plan_id") : 0;
      if (discPlanId > 0) {
        keyMap.clear();
        keyMap.put("plan_id", planId);
        keyMap.put("insurance_category_id", insCatId);
        keyMap.put("patient_type", visitType);
        // cannot use find by PK because it adds condition for primary key of table.
        List<BasicDynaBean> planDetBeanList = insurancePlanDetailsService.listAll(keyMap);
        BasicDynaBean planDetBean = null;
        if (null != planDetBeanList && !planDetBeanList.isEmpty()) {
          planDetBean = planDetBeanList.get(0);
        }
        if (planDetBean == null) {
          return false;
        }
        String isCategoryPayable = (String) planDetBean.get("category_payable");
        if (isCategoryPayable.equals("N")) {
          return false;
        }
      }

    }
    return true;
  }

}
