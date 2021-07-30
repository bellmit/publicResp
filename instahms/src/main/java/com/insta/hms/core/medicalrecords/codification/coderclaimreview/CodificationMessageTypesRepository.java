package com.insta.hms.core.medicalrecords.codification.coderclaimreview;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class CodificationMessageTypesRepository.
 *
 * @author allabakash
 */
@Repository
public class CodificationMessageTypesRepository
    extends MasterRepository<Integer> {

  /**
   * Instantiates a new codification message types repository.
   */
  public CodificationMessageTypesRepository() {
    super("review_types", "review_type_id", "review_type");
  }

  /** The Constant REVIEW_TYPES_TABLE. */
  private static final String REVIEW_TYPES_TABLE = " FROM (SELECT "
      + "review_type_id, review_type, review_title, "
      + "review_content, message_category, review_category_id "
      + "FROM review_types WHERE status = 'A' ORDER BY UPPER(review_type) ) as foo ";

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterRepository#getSearchQuery()
   */
  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(REVIEW_TYPES_TABLE);
  }

  /** The Constant REVIEW_TYPES_WITH_ROLE. */
  private static final String REVIEW_TYPES = "SELECT "
      + "cmt.review_type_id, cmt.review_type, cmt.review_title, "
      + "cmt.status AS review_type_status, rcr.status AS review_category_status,"
      + "cmt.review_content, cmt.review_category as old_category, rcr.category_id, "
      + "CASE WHEN (rcr.category_type = 'P') THEN 'physician' ELSE '' END as review_category "
      + "FROM review_types cmt "
      + "INNER JOIN review_categories rcr ON cmt.review_category_id = rcr.category_id "
      + "WHERE cmt.status='A' ";
  
  /** The Constant ORDER_BY_STRING. */
  private static final String ORDER_BY_STRING = " ORDER BY UPPER(cmt.review_type) ";
  
  /**
   * Gets the message types list.
   *
   * @return the message types list
   */
  public List<BasicDynaBean> getMessageTypesList() {
    return DatabaseHelper.queryToDynaList(REVIEW_TYPES + ORDER_BY_STRING);
  }
  
  /**
   * Gets the message types list.
   *
   * @param reviewTypeId the review type id
   * @return the message types list
   */
  public List<BasicDynaBean> getMessageTypesList(Integer reviewTypeId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("reviewTypeId", reviewTypeId);
    return DatabaseHelper.queryToDynaList(REVIEW_TYPES 
        + " OR cmt.review_type_id = :reviewTypeId " 
        + ORDER_BY_STRING, params);
  }

  /** The Constant REVIEW_TYPES_WITH_ROLE. */
  private static final String REVIEW_TYPES_WITH_ROLE = "SELECT "
      + "cmtr.role_id, cmtr.review_type_id, cmtr.center_id  "
      + "FROM review_types cmt "
      + "INNER JOIN review_type_center_role cmtr ON cmt.review_type_id = cmtr.review_type_id "
      + "WHERE cmtr.center_id in (0, ?) ORDER BY cmtr.center_id DESC";

  /**
   * Gets the message types list.
   *
   * @return the message types list
   */
  public List<BasicDynaBean> getRoleForCenter() {

    Integer centerId = 0;
    if (RequestContext.getCenterId() > 0) {
      centerId = RequestContext.getCenterId();
    }
    Object[] params = new Object[] { centerId };
    return DatabaseHelper.queryToDynaList(REVIEW_TYPES_WITH_ROLE, params);
  }

  /** The Constant REVIEW_TYPES_CATEGORY_DETAILS. */
  private static final String REVIEW_TYPES_CATEGORY_DETAILS = "SELECT "
      + "cmt.review_type_id, rc.category_id, rc.category_name, rc.category_type, rc.send_email, "
      + "rc.status FROM review_types cmt "
      + "INNER JOIN review_categories rc ON cmt.review_category_id = rc.category_id "
      + "WHERE cmt.review_type_id = ?";


  /**
   * Gets the review category details.
   *
   * @param reviewTypeId the review type id
   * @return the review category details
   */
  public BasicDynaBean getReviewCategoryDetails(Integer reviewTypeId) {

    Object[] params = new Object[] { reviewTypeId };
    return DatabaseHelper.queryToDynaBean(REVIEW_TYPES_CATEGORY_DETAILS,
        params);
  }

  /** The Constant LOOKUP_QUERY. */
  private static final String LOOKUP_QUERY = "SELECT * "
      + "FROM (SELECT cmt.review_type_id, cmt.review_type, cmt.review_title, "
      + "cmt.review_content, cmt.review_category, cmtr.role_id "
      + "FROM review_types cmt INNER JOIN codification_message_type_role cmtr "
      + "ON cmt.review_type_id = cmtr.message_type_id ) AS foo ";

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
