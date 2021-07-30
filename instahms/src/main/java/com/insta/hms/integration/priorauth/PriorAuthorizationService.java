package com.insta.hms.integration.priorauth;

import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class PriorAuthorizationService.
 */
@Service
public class PriorAuthorizationService {

  /**
   * The prior auth repo.
   */
  @LazyAutowired
  private PriorAuthorizationRepository priorAuthRepo;

  /**
   * List all.
   *
   * @return the list
   */
  public List listAll() {
    return priorAuthRepo.listAll();
  }

}
