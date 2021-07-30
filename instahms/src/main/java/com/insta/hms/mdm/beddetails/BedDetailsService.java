package com.insta.hms.mdm.beddetails;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

/**
 * The Class BedDetailsService.
 */
@Service
public class BedDetailsService {

  @LazyAutowired
  BedDetailsRepository bedDetailsRepository;

  /**
   * Gets the bed details bean.
   *
   * @param actDescriptionId
   *          the act description id
   * @return the bed details bean
   */
  public BasicDynaBean getBedDetailsBean(String actDescriptionId) {
    return bedDetailsRepository.getBedDetailsBean(actDescriptionId);
  }

  /**
   * Gets the ICU bed charges.
   *
   * @param bedId
   *          the bed id
   * @param orgId
   *          the org id
   * @param bedType
   *          the bed type
   * @return the ICU bed charges
   */
  public BasicDynaBean getIcuBedCharges(int bedId, String orgId, String bedType) {
    return bedDetailsRepository.getIcuBedCharges(bedId, orgId, bedType);
  }

  /**
   * Gets the normal bed charges bean.
   *
   * @param bedType
   *          the bed type
   * @param orgId
   *          the org id
   * @return the normal bed charges bean
   */
  public BasicDynaBean getNormalBedChargesBean(String bedType, String orgId) {
    return bedDetailsRepository.getNormalBedChargesBean(bedType, orgId);
  }
}
