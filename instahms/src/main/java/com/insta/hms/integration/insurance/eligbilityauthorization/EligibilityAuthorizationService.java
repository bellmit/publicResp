package com.insta.hms.integration.insurance.eligbilityauthorization;

import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EligibilityAuthorizationService {

  @LazyAutowired
  private EligibilityAuthorizationRepository eligibilityAuthRepo;

  public List listAll() {
    return eligibilityAuthRepo.listAll();
  }

}
