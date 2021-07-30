package com.insta.hms.core.medicalrecords.codification.coderclaimreview;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class CoderClaimReviewRepository.
 */
@Repository
public class CoderClaimReviewRepository extends GenericRepository {

  /** The key column. */
  private String keyColumn;

  /** The look up columns. */
  private String[] lookUpColumns = null;

  /** The coder claim review validator. */
  @LazyAutowired
  CoderClaimReviewValidator coderClaimReviewValidator;

  /** The session service. */
  @LazyAutowired
  SessionService sessionService;

  /**
   * Instantiates a new coder claim review repository.
   */
  public CoderClaimReviewRepository() {
    super("reviews");
    this.keyColumn = "id";
    this.lookUpColumns = new String[] { "title", "body", "status" };
  }

  /**
   * Gets the look up columns.
   *
   * @return the look up columns
   */
  public String[] getLookUpColumns() {
    return this.lookUpColumns;
  }

  /**
   * Gets the key column.
   *
   * @return the key column
   */
  public String getKeyColumn() {
    return this.keyColumn;
  }

  /** The Constant TICKET_SEARCH_QUERY. */
  private static final String TICKET_SEARCH_QUERY = "SELECT "
      + "DISTINCT t.id ticket_id, "
      + "t.title, t.created_by, t.created_at, t.status, "
      + "t.patient_id, u2.temp_username created_by_fullname, "
      + "pr.mr_no, sm.salutation || ' ' || pd.patient_name || "
      + " CASE WHEN coalesce(pd.middle_name, '') = '' "
      + "      THEN '' ELSE (' ' || pd.middle_name) END || "
      + " CASE WHEN coalesce(pd.last_name, '') = '' "
      + "      THEN '' ELSE (' ' || pd.last_name) END "
      + "AS patient_name , "
      + "tr.user_id assignedTo, uu.temp_username assignedToName, "
      + "ctd.assigned_to_role, "
      + "ur.role_name assigned_to_role_name, review_type_id FROM reviews t "
      + "INNER JOIN review_details ctd ON t.id = ctd.ticket_id  "
      + "LEFT JOIN review_recipients tr ON t.id=tr.ticket_id "
      + "INNER JOIN patient_registration pr on t.patient_id = pr.patient_id "
      + "INNER JOIN patient_details pd ON pr.mr_no = pd.mr_no "
      + "INNER JOIN salutation_master sm ON pd.salutation = sm.salutation_id "
      + "INNER JOIN u_role ur ON ur.role_id = ctd.assigned_to_role "
      + "INNER JOIN u_user u2 ON u2.emp_username = t.created_by "
      + "LEFT JOIN u_user uu ON tr.user_id = uu.emp_username  ";

  /**
   * Gets the search query.
   *
   * @return the search query
   */
  @SuppressWarnings("unchecked")
  public SearchQuery getSearchQuery() {

    Integer centerId = RequestContext.getCenterId();
    String whereClause = "";
    Integer loggedInRoleId = (Integer) sessionService.getSessionAttributes()
        .get("roleId");
    String loggedInUserIdStr = sessionService.getSessionAttributes()
        .get("userId").toString();
    if (loggedInRoleId > 2 && !loggedInUserIdStr.isEmpty()) {
      whereClause += " WHERE (t.created_by= '" + loggedInUserIdStr
          + "' OR tr.user_id= '" + loggedInUserIdStr + "'";
      whereClause += " OR ctd.assigned_to_role= '" + loggedInRoleId + "'";
      whereClause += " OR ctd.review_type_id in "
          + "( SELECT review_type_id FROM review_types rt "
          + " JOIN review_categories rc "
          + "   ON rt.review_category_id = rc.category_id "
          + " JOIN review_category_role rcr "
          + "   ON rc.category_id = rcr.category_id  "
          + " WHERE rt.status = 'A' AND rcr.role_id = " + loggedInRoleId
          + " ) ) ";
    }
    if (centerId > 0 && loggedInRoleId > 2) {
      whereClause += (whereClause.isEmpty()) ? " WHERE " : " AND ";
      whereClause += " pr.center_id = " + centerId;
    }

    String ticketListFinalQuery = " FROM ( " + TICKET_SEARCH_QUERY;
    ticketListFinalQuery += whereClause + " ) AS foo ";

    return new SearchQuery(ticketListFinalQuery);
  }

  /** The Constant GET_TICKET_LIST. */
  public static final String GET_TICKET_LIST = "SELECT "
      + "DISTINCT t.id ticket_id, "
      + "t.title,t.body, t.created_by, t.status, "
      + "ctd.review_type_id, t.patient_id,  "
      + "u.role_id created_role_id, u.temp_username created_by_fullname, "
      + "t.created_at, ctd.assigned_to_role, rc.category_id, "
      + "CASE WHEN (rc.category_type = 'P') "
      + "     THEN 'physician' ELSE '' END as review_category, "
      + "tr.user_id assignedTo, u2.doctor_id FROM reviews t "
      + "JOIN review_details ctd ON t.id = ctd.ticket_id "
      + "JOIN review_types cmt ON "
      + " ctd.review_type_id = cmt.review_type_id "
      + "JOIN review_categories rc ON "
      + " cmt.review_category_id = rc.category_id "
      + "LEFT JOIN review_recipients tr ON t.id=tr.ticket_id "
      + "JOIN u_user u on t.created_by = u.emp_username "
      + "LEFT JOIN u_user u2 ON u2.emp_username = tr.user_id "
      + "WHERE t.id = ?";

  /**
   * Gets the ticket details.
   *
   * @param id
   *          the id
   * @return the ticket details
   */
  public List<BasicDynaBean> getTicketDetails(Integer id) {
    Object[] paramObject = new Object[] { id };
    return DatabaseHelper.queryToDynaList(GET_TICKET_LIST, paramObject);
  }

  /** The Constant GET_PATIENT_DETAILS. */
  public static final String GET_PATIENT_DETAILS = "SELECT "
      + " pr.patient_id,pr.codification_status, "
      + " CASE WHEN codification_status = 'P' THEN 'Inprogress' "
      + " WHEN codification_status = 'C' THEN 'Completed' "
      + " WHEN codification_status = 'R' THEN 'Completed-Needs Verification' "
      + " WHEN codification_status = 'V' THEN 'Verified and Closed' "
      + "  ELSE '' END AS codification_status_label, "
      + " pr.mr_no, u.emp_username as duty_doctor_user_id, "
      + " u.role_id as duty_doctor_role_id, "
      + " dc.consultation_id, u.doctor_id, pr.center_id  "
      + " FROM patient_registration pr "
      + " LEFT JOIN u_user u ON pr.doctor = u.doctor_id AND u.emp_status = 'A' "
      + " AND (pr.center_id = u.center_id OR u.center_id = 0) "
      + " LEFT JOIN doctor_consultation dc ON dc.doctor_name = pr.doctor "
      + " and dc.patient_id = pr.patient_id WHERE pr.patient_id = ?"
      + " ORDER BY u.center_id DESC";

  /**
   * Gets the patient details.
   *
   * @param patientId
   *          the patient id
   * @return the patient details
   */
  public List<BasicDynaBean> getPatientDetails(String patientId) {
    Object[] paramObject = new Object[] { patientId };
    return DatabaseHelper.queryToDynaList(GET_PATIENT_DETAILS, paramObject);
  }

  /** The Constant GET_TICKET_RECENTLY_INSERT_ID. */
  public static final String GET_TICKET_RECENTLY_INSERT_ID = "SELECT t.id "
      + "FROM reviews t ORDER BY t.id DESC LIMIT 1";

  /**
   * Gets the recent inserted id.
   *
   * @return the recent inserted id
   */
  public BasicDynaBean getRecentInsertedId() {
    return DatabaseHelper.queryToDynaBean(GET_TICKET_RECENTLY_INSERT_ID);
  }
  
  /** The Constant GET_CODIFICATION_STATUS. */
  private static final String GET_CODIFICATION_STATUS = "SELECT codification_status "
      + "FROM patient_registration pr WHERE pr.patient_id = ?";
  
  /**
   * Gets the codification status.
   * @param patientId 
   *
   * @return the recent inserted id
   */
  public BasicDynaBean getCodificationStatus(String patientId) {
    return DatabaseHelper.queryToDynaBean(GET_CODIFICATION_STATUS, patientId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.GenericRepository#search(java.util.Map)
   */
  @Override
  public PagedList search(Map params) {
    Map<LISTING, Object> listingParams = ConversionUtils
        .getListingParameter(params);
    SearchQueryAssembler qb = getSearchQueryAssembler(params, listingParams);
    if (qb != null) {
      qb.build();
      return qb.getMappedPagedList();
    }
    return null;
  }

  /**
   * Gets the search query assembler.
   *
   * @param params
   *          the params
   * @param listingParams
   *          the listing params
   * @return the search query assembler
   */
  protected SearchQueryAssembler getSearchQueryAssembler(Map params,
      Map<LISTING, Object> listingParams) {

    SearchQuery query = this.getSearchQuery();
    if (query == null) {
      return null;
    }
    SearchQueryAssembler qb = new SearchQueryAssembler(query.getFieldList(),
        query.getCountQuery(), query.getSelectTables(), listingParams);
    if (params != null) {
      qb.addFilterFromParamMap(params);
    }

    String secondarySortCol = query.getSecondarySortColumn();
    if (secondarySortCol != null) {
      qb.addSecondarySort(secondarySortCol);
    }
    return qb;
  }

  /**
   * Find by PK.
   *
   * @param params
   *          the params
   * @return the basic dyna bean
   */
  public BasicDynaBean findByPk(Map params) {

    String keyColumnKey = null;
    String[] keyValue = null;
    keyColumnKey = getKeyColumn();
    if (keyColumnKey != null && params != null
        && params.containsKey(keyColumnKey)) {
      keyValue = (String[]) params.get(keyColumnKey);
    }
    Integer id = (keyValue != null) ? (Integer.parseInt(keyValue[0])) : 0;
    return findByKey(keyColumnKey, id);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.common.GenericRepository#insert(org.apache.commons.beanutils.
   * BasicDynaBean)
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public Integer insert(BasicDynaBean bean) {
    coderClaimReviewValidator.validateInsert(bean);
    return super.insert(bean);
  }

  /**
   * Update.
   *
   * @param bean
   *          the bean
   * @return the integer
   */
  @Transactional(rollbackFor = Exception.class)
  public Integer update(BasicDynaBean bean) {
    Map<String, Object> keys = new HashMap<>();
    coderClaimReviewValidator.validateUpdate(bean);
    String keyColumnKey = getKeyColumn();
    if (keyColumnKey != null) {
      keys.put(keyColumnKey, bean.get(keyColumn));
    }
    return this.update(bean, keys);
  }

  /** The Constant GET_CATEGORY_ROLE_QUERY. */
  public static final String GET_CATEGORY_ROLE_QUERY = "SELECT "
      + "COUNT(rcr.id) AS categories_count FROM review_types rt "
      + "JOIN review_categories rc ON rt.review_category_id = rc.category_id "
      + "JOIN review_category_role rcr ON rc.category_id = rcr.category_id "
      + "WHERE rcr.category_id = ? AND rcr.role_id = ?";

  /**
   * Gets the recent inserted id.
   *
   * @return the recent inserted id
   */
  public BasicDynaBean getCategoriesCountForRoleId(Integer ticketCategoryId,
      Integer roleId) {
    Object[] params = new Object[] { ticketCategoryId, roleId };
    return DatabaseHelper.queryToDynaBean(GET_CATEGORY_ROLE_QUERY, params);
  }

}
