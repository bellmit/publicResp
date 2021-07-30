package com.insta.hms.mdm.incominghospitals;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.rateplan.RatePlanService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class IncomingHospitalsService.
 */
@Service
public class IncomingHospitalsService extends MasterService {
  
  /** The rate plan service. */
  @LazyAutowired private RatePlanService ratePlanService;

  /**
   * Instantiates a new incoming hospitals service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public IncomingHospitalsService(
      IncomingHospitalsRepository repo, IncomingHospitalsValidator validator) {
    super(repo, validator);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> refData = new HashMap<String, List<BasicDynaBean>>();
    refData.put("hospitalsLists", lookup(false));
    refData.put("HospitalList", lookup(false));
    List<BasicDynaBean> ratePlanDetails = ratePlanService.lookup(false);
    refData.put("ratePlanDetails", ratePlanDetails);
    return refData;
  }
}
