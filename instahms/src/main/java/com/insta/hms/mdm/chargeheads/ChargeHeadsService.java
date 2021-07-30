package com.insta.hms.mdm.chargeheads;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.servicesubgroup.ServiceSubGroupService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ChargeHeadsService.
 */
@Service
public class ChargeHeadsService extends MasterService {

  /** The servicesubgroupservice. */
  @LazyAutowired
  private ServiceSubGroupService servicesubgroupservice;

  private ChargeHeadsRepository chargeHeadsRepository;

  /**
   * Instantiates a new charge heads service.
   *
   * @param repo
   *          the repo
   * @param validator
   *          the validator
   */
  public ChargeHeadsService(ChargeHeadsRepository repo, ChargeHeadsValidator validator) {
    super(repo, validator);
    this.chargeHeadsRepository = repo;
  }

  /**
   * Gets the adds the edit page data.
   *
   * @param params
   *          the params
   * @return the adds the edit page data
   */
  @SuppressWarnings({ "rawtypes" })
  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map params) {
    Map<String, List<BasicDynaBean>> map = new HashMap<>();

    if (((String[]) params.get("chargehead_id")) != null) {
      String chargeheadId = String.valueOf(((String[]) params.get("chargehead_id"))[0]);
      map.put("chargehead", chargeHeadsRepository.getChargeHeadBean(chargeheadId));
    }
    map.put("chargeHeadsJSON", chargeHeadsRepository.listAll());
    map.put("serviceSubGroups", servicesubgroupservice.listActiveRecord());
    return map;
  }

  /**
   * List all.
   *
   * @param columns
   *          the columns
   * @param filterMap
   *          the filter map
   * @param sortColumn
   *          the sort column
   * @return the list
   */
  public List<BasicDynaBean> listAll(List<String> columns, Map<String, Object> filterMap,
      String sortColumn) {
    return chargeHeadsRepository.listAll(columns, filterMap, sortColumn);

  }

  /**
   * Gets the discount applicable charge head.
   *
   * @return the discount applicable charge head
   */
  public List<BasicDynaBean> getDiscountApplicableChargeHead() {
    return chargeHeadsRepository.getDiscountApplicableChargeHead();
  }

  /**
   * Checks if is eligible for payment.
   *
   * @param mainChargeHead
   *          the main charge head
   * @return true, if is eligible for payment
   */
  public boolean isEligibleForPayment(String mainChargeHead) {
    return chargeHeadsRepository.isEligibleForPayment(new Object[] { mainChargeHead }).equals("Y");
  }

  /**
   * Gets the charge head bean.
   *
   * @param chBillServiceCharge
   *          the ch bill service charge
   * @return the charge head bean
   */
  public BasicDynaBean getChargeHeadBean(String chBillServiceCharge) {
    List<BasicDynaBean> chargeBeans = chargeHeadsRepository.getChargeHeadBean(chBillServiceCharge);
    if (chargeBeans != null && !chargeBeans.isEmpty()) {
      return chargeBeans.get(0);
    }
    return null;
  }

  /**
   * Get ot doctor charge heads.
   * 
   * @return ot doctor charge head list
   */
  public List<BasicDynaBean> getOtDoctorChargeHeads() {
    return (((ChargeHeadsRepository) 
        getRepository()).getOtDoctorChargeHeads());
  }
  
  public BasicDynaBean findByKey(String chargeHeadId) {
    return ((ChargeHeadsRepository) getRepository()).findByKey("chargehead_id", chargeHeadId);
  }

  /**
   * Get charge head map.
   * 
   * @return the charge head map
   */
  public Map<String, BasicDynaBean> getChargeHeadMap() {
    // TODO Auto-generated method stub
    List<BasicDynaBean> chargeHeadList = ((ChargeHeadsRepository) 
        getRepository()).listAll();
    Map<String, BasicDynaBean> chargeHeadMap =
        ConversionUtils.listBeanToMapBean(chargeHeadList, "chargehead_id");
    return chargeHeadMap;
  }

  /**
   * Gets the charge head map.
   *
   * @param chargeHeadIds the chargeHeadIds
   * @return the charge head map
   */
  public Map<String, String> getChargeHeadNames(List<String> chargeHeadIds) {
    Map<String, String> chargeHeadBeanMap = new HashMap<>();
    List<BasicDynaBean> chargeHeadBeans = chargeHeadsRepository.getChargeHeadBean(chargeHeadIds);
    for (BasicDynaBean chargeHeadBean : chargeHeadBeans) {
      chargeHeadBeanMap.put(
              (String) chargeHeadBean.get("chargehead_id"),
              ((String) chargeHeadBean.get("chargehead_name")));
    }
    return chargeHeadBeanMap;
  }

}
