package com.insta.hms.core.medicalrecords.codification.coderclaimreview;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class TicketCommentsService.
 */
@Service
public class TicketCommentsService {

  /** The codification message types service. */
  @LazyAutowired
  private CodificationMessageTypesService codificationMessageTypesService;

  /** The user service. */
  @LazyAutowired
  private UserService userService;

  /** The coder claim review repository. */
  @LazyAutowired
  private CoderClaimReviewRepository coderClaimReviewRepository;

  /** The coder claim review service. */
  @LazyAutowired
  private CoderClaimReviewService coderClaimReviewService;

  /** The coder claim review activity service. */
  @LazyAutowired
  private CoderClaimReviewActivityService coderClaimReviewActivityService;

  /** The ticket comments repository. */
  @LazyAutowired
  private TicketCommentsRepository ticketCommentsRepository;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The ticket recipients repository. */
  @LazyAutowired
  private TicketRecipientsRepository ticketRecipientsRepository;

  /** The log. */
  private static Logger log = LoggerFactory
      .getLogger(TicketCommentsService.class);
  
  /** The Constant STATUS. */
  private static final String STATUS = "status";
  
  /** The Constant TICKET_ID. */
  private static final String TICKET_ID = "ticket_id";
  
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
    return ticketCommentsRepository.delete(key, identifier);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return ticketCommentsRepository.getBean();
  }

  /**
   * Insert.
   *
   * @param activityBean
   *          the activity bean
   * @return the integer
   */
  public Integer insert(BasicDynaBean activityBean) {
    return ticketCommentsRepository.insert(activityBean);
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
    return ticketCommentsRepository.update(activityBean, key);
  }

  /**
   * Save ticket comments.
   *
   * @param request
   *          the request
   */
  @SuppressWarnings("unchecked")
  public void saveTicketComments(Map<String, Object> request) {
    Map<String, Object> key = new HashMap<>();
    Integer ticketId = Integer.parseInt((String) request.get(TICKET_ID));

    key.put(TICKET_ID, ticketId);
    BasicDynaBean commentBean = ticketCommentsRepository.getBean();

    commentBean.set(TICKET_ID, ticketId);
    commentBean.set("comment", request.get("commentText"));
    String commentedBy = (String) sessionService.getSessionAttributes()
                                                .get("userId");
    commentBean.set("comment_by", commentedBy);
    insert(commentBean);

    Map<String, Object> ticketKey = new HashMap<>();
    ticketKey.put("id", ticketId);
    BasicDynaBean coderClaimReviewBean = coderClaimReviewRepository
        .findByKey(ticketKey);
    BasicDynaBean oldReviewBean = null;
    if (coderClaimReviewBean != null) {
      Map<String, Object> keys = new HashMap<>();
      keys.put("id", ticketId);
      String status = (String) coderClaimReviewBean.get(STATUS);
      Map<String, Object> urlRightsMaps = (Map<String, Object>) sessionService
          .getSessionAttributes(new String[] { "urlRightsMap" }).get("urlRightsMap");

      if (status.equalsIgnoreCase("open") 
          && !"A".equals(urlRightsMaps.get("update_mrd"))) {
        coderClaimReviewBean.set(STATUS, "inprogress");
      }
      if (status.equalsIgnoreCase("inprogress") 
          && "A".equals(urlRightsMaps.get("update_mrd"))) {
        coderClaimReviewBean.set(STATUS, "open");
      }
      oldReviewBean = coderClaimReviewRepository.findByKey(ticketKey);
      coderClaimReviewRepository.update(coderClaimReviewBean, keys);
      BasicDynaBean newReviewBean = coderClaimReviewRepository
          .findByKey(ticketKey);
      Map<String, Object[]> changeMap = coderClaimReviewService
          .getChangeMap(oldReviewBean,
          newReviewBean);
      List<BasicDynaBean> activityBeanList = null;
      try {
        activityBeanList = coderClaimReviewService
            .processChangeMap(changeMap, (Integer) ticketId);
        for (BasicDynaBean activityBean : activityBeanList) {
          coderClaimReviewActivityService.add(activityBean);
        }
      } catch (Exception exp) {
        log.info(
            "Unable to add activity. Exception message ::" + exp.getMessage());
      }
    }

  }

  /**
   * Gets the comments list.
   *
   * @param ticketId
   *          the ticket id
   * @return the comments list
   */
  public List<BasicDynaBean> getCommentsList(Integer ticketId) {
    return ticketCommentsRepository.getTicketComments(ticketId);
  }
}
