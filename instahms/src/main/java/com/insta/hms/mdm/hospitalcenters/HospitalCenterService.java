package com.insta.hms.mdm.hospitalcenters;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

/**
 * The Class HospitalCenterService.
 */
@Service
public class HospitalCenterService extends MasterService {

  /** The hospital center repo. */
  @LazyAutowired private HospitalCenterRepository hospitalCenterRepo;

  /**
   * Instantiates a new hospital center service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public HospitalCenterService(HospitalCenterRepository repo, HospitalCenterValidator validator) {
    super(repo, validator);
  }

  /**
   * Find by key.
   *
   * @param centerId the center id
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(int centerId) {
    return hospitalCenterRepo.findByKey("center_id", centerId);
  }
}
