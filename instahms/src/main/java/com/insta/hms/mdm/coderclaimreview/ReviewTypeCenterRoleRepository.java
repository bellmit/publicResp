package com.insta.hms.mdm.coderclaimreview;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class ReviewTypeCenterRoleRepository.
 */
@Repository
public class ReviewTypeCenterRoleRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new review type center role repository.
   */
  public ReviewTypeCenterRoleRepository() {
    super("review_type_center_role", "id");
  }

  /** The Constant LIST_BY_MESSAGE_ID. */
  private static final String LIST_BY_MESSAGE_ID = "SELECT "
      + "r.role_id, r.center_id, c.center_name "
      + "FROM review_type_center_role r left join hospital_center_master c "
      + "on (r.center_id = c.center_id) "
      + "WHERE r.review_type_id = ?";

  /**
   * Gets the by message id.
   *
   * @param messageId
   *          the message id
   * @return the by message id
   */
  public List getByMessageId(Integer messageId) {
    return DatabaseHelper.queryToDynaList(LIST_BY_MESSAGE_ID,
        (Object) messageId);
  }
}
