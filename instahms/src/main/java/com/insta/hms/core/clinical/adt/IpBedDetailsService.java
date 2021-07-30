package com.insta.hms.core.clinical.adt;

import com.insta.hms.common.annotations.LazyAutowired;
import org.apache.commons.beanutils.BasicDynaBean;

import java.util.List;

public class IpBedDetailsService {

  @LazyAutowired
  private IpBedDetailsService ipBedDetailsRepository;

  public List<BasicDynaBean> getVistMainBeds(String visitId) {
    return ipBedDetailsRepository.getVistMainBeds(visitId);
  }

  public BasicDynaBean getActiveBedDetails(String visitId) {
    return ipBedDetailsRepository.getActiveBedDetails(visitId);
  }

  public BasicDynaBean getAdmissionDetails(String visitId) {
    return ipBedDetailsRepository.getAdmissionDetails(visitId);
  }

  public List<BasicDynaBean> getReferencedBeds(String visitId, int mainAdmitId) {
    return ipBedDetailsRepository.getReferencedBeds(visitId, mainAdmitId);
  }

}
