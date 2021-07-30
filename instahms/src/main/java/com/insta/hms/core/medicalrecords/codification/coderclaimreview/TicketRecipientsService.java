package com.insta.hms.core.medicalrecords.codification.coderclaimreview;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * The Class TicketRecipientsService.
 */
@Service
public class TicketRecipientsService {

  /** The codification message types service. */
  @LazyAutowired
  private CodificationMessageTypesService codificationMessageTypesService;

  /** The user service. */
  @LazyAutowired
  private UserService userService;

  /** The coder claim review repository. */
  @LazyAutowired
  private CoderClaimReviewRepository coderClaimReviewRepository;

  /** The ticket recipients repository. */
  @LazyAutowired
  private TicketRecipientsRepository ticketRecipientsRepository;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /**
   * Delete.
   *
   * @param key
   *          the key
   * @param identifier
   *          the identifier
   * @return the integer
   */
  public Integer delete(String key, String identifier) {
    return ticketRecipientsRepository.delete(key, identifier);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return ticketRecipientsRepository.getBean();
  }

  /**
   * Insert.
   *
   * @param activityBean
   *          the activity bean
   * @return the integer
   */
  public Integer insert(BasicDynaBean activityBean) {
    return ticketRecipientsRepository.insert(activityBean);
  }

  /**
   * Update.
   *
   * @param activityBean
   *          the activity bean
   * @param key
   *          the key
   * @return the integer
   */
  public Integer update(BasicDynaBean activityBean, Map<String, Object> key) {
    return ticketRecipientsRepository.update(activityBean, key);
  }

}
