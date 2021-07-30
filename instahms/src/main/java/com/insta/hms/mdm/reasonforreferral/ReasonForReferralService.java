package com.insta.hms.mdm.reasonforreferral;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;


/**
 * The Class ReasonForReferralService.
 */

@Service
public class ReasonForReferralService extends MasterService {
  
  /** The session service. */
  @LazyAutowired
  SessionService sessionService;

  /** The reason for referral repository. */
  @LazyAutowired
  private ReasonForReferralRepository reasonForReferralRepository;

  @LazyAutowired
  private ReasonForReferralValidator reasonForReferralValidator;
  
  /**
   * Instantiates a new reason for referral service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public ReasonForReferralService(ReasonForReferralRepository repo,
      ReasonForReferralValidator validator) {
    super(repo, validator);
  }

  /**
   * Find reason for referral by filter.
   *
   * @param params filter parameters
   * @return result map
   */
  public Map<String, Object> findByFilters(Map<String, String> params) {
    return reasonForReferralRepository.findByFilters(params);
  }

  /**
   * Insert.
   *
   * @param bean the bean
   * @return the integer
   */
  @Transactional(rollbackFor = Exception.class)
  public Integer insert(BasicDynaBean bean) {
    String userId = (String) sessionService.getSessionAttributes().get("userId");
    Timestamp now = DateUtil.getCurrentTimestamp();
    bean.set("modified_at", now);
    bean.set("created_at", now);
    bean.set("created_by", userId);
    bean.set("modified_by", userId);
    return super.insert(bean);
  }

  /**
   * Update.
   *
   * @param bean the bean
   * @return the integer
   */
  @Transactional(rollbackFor = Exception.class)
  public Integer update(BasicDynaBean bean) {
    String userId = (String) sessionService.getSessionAttributes().get("userId");
    Timestamp now = DateUtil.getCurrentTimestamp();
    bean.set("modified_at", now);
    bean.set("modified_by", userId);
    return super.update(bean);
  }

}
