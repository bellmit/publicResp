package com.insta.hms.core.medicalrecords.codification.coderclaimreview;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class CoderClaimReviewActivityRepository.
 */
@Repository
public class CoderClaimReviewActivityRepository extends GenericRepository {

  /** The Constant ACTIVITY_CREATE_REVIEW. */
  public static final String ACTIVITY_CREATE_REVIEW = "CREATE_REVIEW";

  /** The Constant ACTIVITY_UPDATE_BODY. */
  public static final String ACTIVITY_UPDATE_BODY = "UPDATE_BODY";

  /** The Constant ACTIVITY_UPDATE_STATUS. */
  public static final String ACTIVITY_UPDATE_STATUS = "UPDATE_STATUS";

  /** The Constant ACTIVITY_COMMENT. */
  public static final String ACTIVITY_COMMENT = "COMMENT";

  /** The Constant ACTIVITY_UPDATE_TITLE. */
  public static final String ACTIVITY_UPDATE_TITLE = "UPDATE_TITLE";

  /** The Constant ACT_UPDATE_MESSAGE_TYPE. */
  public static final String ACT_UPDATE_MESSAGE_TYPE = "UPDATE_MESSAGE_TYPE";

  /** The Constant ACTIVITY_UPDATE_ROLE. */
  public static final String ACTIVITY_UPDATE_ROLE = "UPDATE_ROLE";

  /** The Constant ACTIVITY_UPDATE_ASSIGNEE. */
  public static final String ACTIVITY_UPDATE_ASSIGNEE = "UPDATE_ASSIGNEE";

  /**
   * Instantiates a new coder claim review activity repository.
   */
  public CoderClaimReviewActivityRepository() {
    super("review_activities");
  }

  /** The Constant GET_COMMENTS_AND_ACTIVITY. */
  public static final String GET_COMMENTS_AND_ACTIVITY = "SELECT "
      + "ta.user_id,ta.activity, ta.old_value,ta.new_value,ta.changeset, "
      + "ta.change_at,sr.rights codification_screen_right_status, ta.user_id, "
      + "u.temp_username user_fullname, u.role_id activtiy_role_id "
      + "FROM review_activities ta "
      + "LEFT JOIN u_user u ON ta.user_id = u.emp_username "
      + "LEFT JOIN screen_rights sr ON sr.role_id = u.role_id "
      + "AND sr.screen_id = 'update_mrd' "
      + "WHERE ta.ticket_id = ? "
      + "AND ta.activity != 'CREATE_REVIEW' ORDER BY ta.change_at";

  /**
   * Gets the comments and activity.
   *
   * @param ticketId
   *          the ticket id
   * @return the comments and activity
   */
  public List<BasicDynaBean> getCommentsAndActivity(Integer ticketId) {
    Object[] paramObject = new Object[] { ticketId };
    return DatabaseHelper.queryToDynaList(GET_COMMENTS_AND_ACTIVITY,
        paramObject);

  }

  /** The Constant GET_COMMENTS_COUNT. */
  public static final String GET_COMMENTS_COUNT = "SELECT "
      + "count(activity_id) comments_count "
      + "FROM review_activities "
      + "WHERE activity = 'COMMENT' AND ticket_id = ? ";

  /**
   * Gets the comments count.
   *
   * @param ticketId
   *          the ticket id
   * @return the comments count
   */
  public List<BasicDynaBean> getCommentsCount(Integer ticketId) {
    Object[] paramObject = new Object[] { ticketId };
    return DatabaseHelper.queryToDynaList(GET_COMMENTS_COUNT, paramObject);

  }

}
