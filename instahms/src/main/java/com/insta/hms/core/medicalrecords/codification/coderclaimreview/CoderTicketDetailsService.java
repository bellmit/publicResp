package com.insta.hms.core.medicalrecords.codification.coderclaimreview;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class CoderTicketDetailsService.
 */
@Service
public class CoderTicketDetailsService {

  /** The codification message types service. */
  @LazyAutowired
  private CodificationMessageTypesService codificationMessageTypesService;

  /** The ticket recipients repository. */
  @LazyAutowired
  private TicketRecipientsRepository ticketRecptsRepo;

  /** The user service. */
  @LazyAutowired
  private UserService userService;

  /** The coder claim review repository. */
  @LazyAutowired
  private CoderClaimReviewRepository coderClaimReviewRepository;

  /** The coder ticket details repository. */
  @LazyAutowired
  private CoderTicketDetailsRepository coderTicketDetailsRepository;

  /** The session service. */

  @LazyAutowired
  private SessionService sessionService;
  
  /** The Constant REVIEW_TYPE_ID. */
  private static final String REVIEW_TYPE_ID = "review_type_id";

  /** The Constant TICKET_ID. */
  private static final String TICKET_ID = "ticket_id";

  /** The Constant ASSIGNED_TO_ROLE. */
  private static final String ASSIGNED_TO_ROLE = "assigned_to_role";

  /** The Constant USER_ID. */
  private static final String USER_ID = "user_id";

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
    return coderTicketDetailsRepository.delete(key, identifier);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return coderTicketDetailsRepository.getBean();
  }

  /**
   * Insert.
   *
   * @param activityBean
   *          the activity bean
   * @return the integer
   */
  public Integer insert(BasicDynaBean activityBean) {
    return coderTicketDetailsRepository.insert(activityBean);
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
  public Integer update(BasicDynaBean activityBean,
      Map<String, Object> key) {
    return coderTicketDetailsRepository.update(activityBean, key);
  }

  /**
   * Update ticket details.
   *
   * @param request
   *          the request
   * @param ticketId
   *          the ticket id
   */
  @SuppressWarnings("unused")
  public void updateTicketDetails(Map<String, Object> request,
      Integer ticketId) {
    Integer messageTypeId = Integer
        .parseInt((String) request.get(REVIEW_TYPE_ID));
    Integer roleId = Integer.parseInt((String) request.get("role_id"));
    BasicDynaBean ctdBean = null;
    if (ticketId != null) {
      ctdBean = coderTicketDetailsRepository.findByKey(TICKET_ID, ticketId);
      if (ctdBean == null) {
        ctdBean = coderTicketDetailsRepository.getBean();
        ctdBean.set(TICKET_ID, ticketId);
        ctdBean.set(REVIEW_TYPE_ID, messageTypeId);
        ctdBean.set(ASSIGNED_TO_ROLE, roleId);
        insert(ctdBean);
      } else {
        Map<String, Object> keys = new HashMap<>();
        keys.put("id", ctdBean.get("id"));
        if (messageTypeId > 0) {
          ctdBean.set(REVIEW_TYPE_ID, messageTypeId);
          ctdBean.set(ASSIGNED_TO_ROLE, roleId);
          update(ctdBean, keys);
        }
      }
      Map<String, Object> filterMap = new HashMap<>();
      filterMap.put(TICKET_ID, ticketId);
      BasicDynaBean ticketRecipientBean = ticketRecptsRepo.findByKey(filterMap);
      String recipients = null;
      recipients = (String) request.get("recipientsDropDown");
      if (ticketRecipientBean == null 
          && (ticketId > 0 && recipients != null && !recipients.isEmpty())) {
        ticketRecipientBean = ticketRecptsRepo.getBean();
        ticketRecipientBean.set(TICKET_ID, ticketId);
        ticketRecipientBean.set(USER_ID, recipients);
        ticketRecptsRepo.insert(ticketRecipientBean);
      } else if (ticketRecipientBean != null) {
        String ticketRecipient = (String) ticketRecipientBean.get(USER_ID);
        if (recipients != null && !ticketRecipient.equals(recipients)
            && !recipients.isEmpty()) {
          Map<String, Object> keys = new HashMap<>();
          ticketRecipientBean.set(USER_ID, recipients);
          ticketRecipientBean.set("read_status", 0);
          keys.put(TICKET_ID, ticketId);
          ticketRecptsRepo.update(ticketRecipientBean, keys);
        }
        // recipient is exist, but admin wants to unassigns, then delete the row
        if (recipients != null && recipients.isEmpty()) {
          Map<String, Object> keys = new HashMap<>();
          keys.put(TICKET_ID, ticketId);
          ticketRecptsRepo.delete(keys);
        }
      }
    } else if (ticketId > 0) {
      updateCodificationReviewDetails(ticketId, messageTypeId, roleId,
          (String) request.get("recipientsDropDown"));
    }
  }

  /**
   * Update codification review details.
   *
   * @param ticketId the ticket id
   * @param messageTypeId the message type id
   * @param roleId the role id
   * @param recipients the recipients
   */
  private void updateCodificationReviewDetails(Integer ticketId,
      Integer messageTypeId,
      Integer roleId, String recipients) {
    BasicDynaBean ctdBeanObj = coderTicketDetailsRepository.getBean();
    ctdBeanObj.set(TICKET_ID, ticketId);
    ctdBeanObj.set(REVIEW_TYPE_ID, messageTypeId);
    ctdBeanObj.set(ASSIGNED_TO_ROLE, roleId);
    insert(ctdBeanObj);
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put(TICKET_ID, ticketId);
    BasicDynaBean ticketRecipientBean = ticketRecptsRepo.findByKey(filterMap);
    if (ticketRecipientBean == null && ticketId > 0 && recipients != null
        && !recipients.isEmpty()) {
      ticketRecipientBean = ticketRecptsRepo.getBean();
      ticketRecipientBean.set(TICKET_ID, ticketId);
      ticketRecipientBean.set(USER_ID, recipients);
      ticketRecptsRepo.insert(ticketRecipientBean);
    }
  }

}
