package com.insta.hms.core.medicalrecords.codification.coderclaimreview;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class CodificationMessageTypeRoleRepository.
 */
@Repository
public class CodificationMessageTypeRoleRepository
    extends MasterRepository<Integer> {

  /**
   * Instantiates a new codification message type role repository.
   */
  public CodificationMessageTypeRoleRepository() {
    super("codification_message_type_role", "id");
  }

  /** The Constant CODIFICATION_MSG_TYPE_ROLE. */
  private static final String CODIFICATION_MSG_TYPE_ROLE = " FROM (SELECT "
      + "id, review_type_id, role_id "
      + "FROM codification_message_type_role ) as foo ";

  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.mdm.MasterRepository#getSearchQuery()
   */
  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(CODIFICATION_MSG_TYPE_ROLE);
  }

  /** The Constant REVIEW_TYPES_ROLE. */
  private static final String REVIEW_TYPES_ROLE = "SELECT "
      + "cmtr.id, cmtr.role_id, cmtr.message_type_id  "
      + "FROM codification_message_type_role cmtr "
      + "WHERE cmtr.message_type_id = ?";

  /**
   * Gets the message type role.
   *
   * @param messageTypeId
   *          the message type id
   * @return the message type role
   */
  public List<BasicDynaBean> getMessageTypeRole(
      Integer messageTypeId) {
    Object[] params = new Object[] { messageTypeId };
    return DatabaseHelper.queryToDynaList(REVIEW_TYPES_ROLE, params);
  }

  /** The Constant LOOKUP_QUERY. */
  private static final String LOOKUP_QUERY = "SELECT * "
      + "FROM (SELECT cmt.message_type_id, " + "cmt.message_type, "
      + "cmt.message_title, cmt.message_content, cmt.review_category, "
      + "cmtr.role_id  FROM review_types cmt "
      + "INNER JOIN codification_message_type_role cmtr "
      + " ON cmt.id = cmtr.message_type_id ) AS foo ";

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterRepository#getLookupQuery()
   */
  @Override
  public String getLookupQuery() {
    return LOOKUP_QUERY;
  }

}
