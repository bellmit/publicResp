package com.insta.hms.mdm.anesthesiatypecharges;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

@Service
public class AnesthesiaTypeChargesService {

  @LazyAutowired
  private AnesthesiaTypeChargesRepository anesthesiaTypeChargesRepository;

  public BasicDynaBean getAnesthesiaTypeCharge(String anesthesiaTypeId, String bedType,
      String orgId) {
    return anesthesiaTypeChargesRepository.getAnesthesiaTypeCharge(anesthesiaTypeId, bedType,
        orgId);
  }

}
