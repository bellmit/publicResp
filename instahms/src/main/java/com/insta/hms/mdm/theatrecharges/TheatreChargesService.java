package com.insta.hms.mdm.theatrecharges;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

@Service
public class TheatreChargesService {

  @LazyAutowired
  private TheatreChargesRepository theatreChargesRepository;

  public BasicDynaBean getTheatreChargeDetails(String theatreId, String bedType, String orgid) {
    return theatreChargesRepository.getTheatreChargeDetails(theatreId, bedType, orgid);
  }

}
