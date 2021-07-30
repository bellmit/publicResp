package com.insta.hms.mdm.opvisittyperules;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * The Class OpVisitTypeRuleDetailsService.
 */
@Service("opVisitTypeRuleDetailsService")
public class OpVisitTypeRuleDetailsService extends MasterService {
  
  /**
   * Instantiates a new Op Visit Type Rule Details Service.
   *
   * @param repository
   *          the op visit type rules repository
   * @param validator
   *          the op visit type rules validator
   */
  public OpVisitTypeRuleDetailsService(OpVisitTypeRuleDetailsRepository repository,
      OpVisitTypeRuleDetailsValidator validator) {
    super(repository, validator);
  }

}
