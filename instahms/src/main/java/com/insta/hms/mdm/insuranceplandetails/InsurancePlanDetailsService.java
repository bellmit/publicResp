package com.insta.hms.mdm.insuranceplandetails;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.HMSException;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.insuranceitemcategories.InsuranceItemCategoryRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class InsurancePlanDetailsService.
 */
@Service
public class InsurancePlanDetailsService extends MasterService {

  /** The insurance plan details repository. */
  @LazyAutowired
  private InsurancePlanDetailsRepository insurancePlanDetailsRepository;
  
  /** The insurance category repository. */
  @LazyAutowired
  private InsuranceItemCategoryRepository insuranceItemCategoryRepository;
  
  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;
  
  /**
   * Instantiates a new insurance plan details service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public InsurancePlanDetailsService(InsurancePlanDetailsRepository repository,
      InsurancePlanDetailsValidator validator) {
    super(repository, validator);
  }

  /**
   * List all.
   *
   * @param filterMap the filter map
   * @return the list
   */
  public List<BasicDynaBean> listAll(Map<String, Object> filterMap) {
    return this.getRepository().listAll(null, filterMap, null);
  }
  
  /**
   * List all.
   *
   * @param filterMap the filter map
   * @return the list
   */
  public List<BasicDynaBean> listAll(List<String> columns, 
      Map<String, Object> filterMap, String sortby) {
    return this.getRepository().listAll(columns, filterMap, sortby);
  }

  /**
   * Gets the mapped plan details.
   *
   * @param planId the plan id
   * @param visitType the visit type
   * @return the mapped plan details
   */
  public List<BasicDynaBean> getMappedPlanDetails(Integer planId, String visitType) {
    return ((InsurancePlanDetailsRepository) getRepository()).getMappedPlanDetails(new Object[] {
        planId, visitType });
  }

  /**
   * Gets the charge amt for plan.
   *
   * @param planId the plan id
   * @param categoryId the category id
   * @param visitType the visit type
   * @return the charge amt for plan
   */
  public BasicDynaBean getChargeAmtForPlan(int planId, int categoryId, String visitType) {
    return ((InsurancePlanDetailsRepository) getRepository()).getChargeAmtForPlan(new Object[] {
        planId, categoryId, visitType });
  }
  
  /**
   * validates insurance plan details data and converts to bean.
   * 
   * @param map the map
   * @return bean insurance details bean
   */
  public BasicDynaBean validateInsPlanDetailsDataAndConvertToBean(Map<String, Object> map) {
    BasicDynaBean bean = insurancePlanDetailsRepository.getBean();
    List<String> errors = new ArrayList<String>();
    BasicDynaBean tempBean = null;
    try {
      map.put("username", RequestContext.getUserName());

      // Plan Item-Category
      final String INSURANCE_CATEGORY_NAME = "insurance_category_name";
      tempBean = insuranceItemCategoryRepository.findByKey(INSURANCE_CATEGORY_NAME,
          map.get(INSURANCE_CATEGORY_NAME));
      if (tempBean != null) {
        map.put("insurance_category_id", tempBean.get("insurance_category_id"));
      } else {
        errors.add(INSURANCE_CATEGORY_NAME);
      }

      if (map.get("insurance_payable") != null) {
        String insPayable = map.get("insurance_payable").toString();
        if (insPayable.equalsIgnoreCase("Yes") || insPayable.equalsIgnoreCase("Y")) {
          map.put("category_payable", "Y");
        } else {
          map.put("category_payable", "N");
        }
      }

      final String CATEGORY_PRIOR_AUTH_REQUIRED = "category_prior_auth_required";
      if (map.get(CATEGORY_PRIOR_AUTH_REQUIRED) != null) {
        String catPriorAuthValue = map.get(CATEGORY_PRIOR_AUTH_REQUIRED).toString();
        if (catPriorAuthValue.equalsIgnoreCase("Not Defined")
            || catPriorAuthValue.equalsIgnoreCase("")) {
          map.put(CATEGORY_PRIOR_AUTH_REQUIRED, "");
        } else if (catPriorAuthValue.equalsIgnoreCase("Never")
            || catPriorAuthValue.equalsIgnoreCase("N")) {
          map.put(CATEGORY_PRIOR_AUTH_REQUIRED, "N");
        } else if (catPriorAuthValue.equalsIgnoreCase("Sometimes")
            || catPriorAuthValue.equalsIgnoreCase("S")) {
          map.put(CATEGORY_PRIOR_AUTH_REQUIRED, "S");
        } else if (catPriorAuthValue.equalsIgnoreCase("Always")
            || catPriorAuthValue.equalsIgnoreCase("A")) {
          map.put(CATEGORY_PRIOR_AUTH_REQUIRED, "A");
        } else {
          errors.add(CATEGORY_PRIOR_AUTH_REQUIRED);
        }
      } else if (map.get(CATEGORY_PRIOR_AUTH_REQUIRED) == null) {
        map.put(CATEGORY_PRIOR_AUTH_REQUIRED, "");
      }

      if (map.get("sponsor_limit") != null) {
        map.put("per_treatment_limit", map.get("sponsor_limit"));
      }

      if (map.get("deductible_category") != null) {
        map.put("patient_amount_per_category", map.get("deductible_category"));
      }

      if (map.get("deductible_item") != null) {
        map.put("patient_amount", map.get("deductible_item"));
      }

      if (map.get("copay_percentage") != null) {
        map.put("patient_percent", map.get("copay_percentage"));
      }

      if (map.get("max_copay") != null) {
        map.put("patient_amount_cap", map.get("max_copay"));
      }

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
