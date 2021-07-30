package com.insta.hms.core.medicalrecords.codification.coderclaimreview;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class CoderClaimReviewActivityService.
 */
@Service
public class CoderClaimReviewActivityService {

  /** The coder claim review activity repository. */
  @LazyAutowired
  private CoderClaimReviewActivityRepository coderClaimReviewActivityRepository;

  /**
   * Adds the.
   *
   * @param activityBean
   *          the activity bean
   */
  public void add(BasicDynaBean activityBean) {
    coderClaimReviewActivityRepository.insert(activityBean);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return coderClaimReviewActivityRepository.getBean();
  }

  /**
   * Gets the activity.
   *
   * @param ticketId
   *          the ticket id
   * @return the activity
   */
  public List<BasicDynaBean> getActivity(Integer ticketId) {
    return 
        coderClaimReviewActivityRepository
        .listAll(null, "ticket_id", ticketId, "activity_id");
  }

  /**
   * Gets the comments and activity.
   *
   * @param ticketId
   *          the ticket id
   * @return the comments and activity
   */
  public List<BasicDynaBean> getCommentsAndActivity(Integer ticketId) {
    return 
        coderClaimReviewActivityRepository.getCommentsAndActivity(ticketId);
  }

  /**
   * Gets the comments count.
   *
   * @param ticketId
   *          the ticket id
   * @return the comments count
   */
  public List<BasicDynaBean> getCommentsCount(Integer ticketId) {
    return coderClaimReviewActivityRepository.getCommentsCount(ticketId);
  }
}
