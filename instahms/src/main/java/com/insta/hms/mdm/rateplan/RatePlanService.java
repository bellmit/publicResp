package com.insta.hms.mdm.rateplan;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;


@Service
public class RatePlanService extends MasterService {

  public RatePlanService(RatePlanRepository repo, RatePlanValidator validator) {
    super(repo, validator);
    // TODO Auto-generated constructor stub
  }

}
