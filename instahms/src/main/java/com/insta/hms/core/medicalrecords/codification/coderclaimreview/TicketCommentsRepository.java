package com.insta.hms.core.medicalrecords.codification.coderclaimreview;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class TicketCommentsRepository.
 */
@Repository
public class TicketCommentsRepository extends GenericRepository {

  /**
   * Instantiates a new ticket comments repository.
   */
  public TicketCommentsRepository() {
    super("review_comments");
  }

  /** The Constant GET_REVIEW_COMMENTS. */
  public static final String GET_REVIEW_COMMENTS = "SELECT "
      + "u.temp_username user_name, c.id, c.ticket_id, "
      + "c.comment_by, c.comment_at, c.comment, u.role_id commenter_role_id "
      + "FROM review_comments c " + "INNER JOIN u_user u ON c.comment_by = u.emp_username "
      + "WHERE ticket_id = ? ORDER BY c.comment_at ASC";

  /**
   * Gets the ticket comments.
   *
   * @param ticketId the ticket id
   * @return the ticket comments
   */
  public List<BasicDynaBean> getTicketComments(Integer ticketId) {
    Object[] params = new Object[] { ticketId };
    return DatabaseHelper.queryToDynaList(GET_REVIEW_COMMENTS, params);

  }

}
