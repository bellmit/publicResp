package com.insta.hms.security.usermanager;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.malaffi.MalaffiRoleComparator;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The Class UserRepository.
 */
@Repository
public class UserRepository extends MasterRepository<String> {

  /** The Constant USER_FIELDS. */
  private static final String USER_FIELDS = "select uu.emp_username,uu.mod_user,uu.mod_date,"
      + "ur.role_name,uu.center_id,ur.role_id";

  /** The Constant COUNT_FIELD. */
  private static final String COUNT_FIELD = "SELECT count(*) ";

  /** The Constant TABLES. */
  private static final String TABLES = " From u_user uu "
      + "   RIGHT JOIN u_role ur ON(ur.role_id=uu.role_id AND hosp_user='Y') ";

  /** The Constant WHERE. */
  private static final String WHERE = " WHERE uu.doctor_id = '' AND uu.emp_status = 'A' ";

  /** The get users query. */
  private static final String USERS_QUERY = "SELECT u.emp_username, u.role_id, "
      + " COALESCE(u.temp_username,u.emp_username) AS temp_username, "
      + " u.emp_status FROM u_user u WHERE u.hosp_user = 'Y' ";

  /** The get users query. */
  private static final String USERS_HOSPITAL_QUERY = "SELECT u.emp_username, u.role_id, "
      + " COALESCE(u.temp_username,u.emp_username) AS temp_username, "
      + " u.emp_status FROM u_user u JOIN u_role ur ON u.role_id = ur.role_id "
      + " WHERE u.hosp_user = 'Y' ";

  /**
   * Instantiates a new user repository.
   */
  public UserRepository() {
    super("u_user","emp_username");
  }

  /**
   * Gets the user deatils map.
   *
   * @param params
   *          the params
   * @return the user deatils map
   * @throws ParseException
   *           the parse exception
   */
  public PagedList getUserDeatilsMap(Map<String, String[]> params) throws ParseException {
    Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(params);
    int pageSize = (Integer) listingParams.get(LISTING.PAGESIZE);
    int pageNum = (Integer) listingParams.get(LISTING.PAGENUM);
    String sortField = (String) listingParams.get(LISTING.SORTCOL);
    boolean sortRev = (Boolean) listingParams.get(LISTING.SORTASC);

    SearchQueryAssembler qb = new SearchQueryAssembler(USER_FIELDS, COUNT_FIELD, TABLES, WHERE,
        sortField, sortRev, pageSize, pageNum);
    qb.addFilterFromParamMap(params);
    int centerId = RequestContext.getCenterId();
    if (centerId != 0) {
      qb.addFilter(SearchQueryBuilder.INTEGER, "uu.center_id", "=", centerId);
    }
    qb.build();
    PagedList userDetailList = qb.getMappedPagedList();
    return userDetailList;
  }

  /**
   * Gets the user with default center.
   *
   * @return the user with default center
   */
  public List<BasicDynaBean> getUserWithDefaultCenter() {
    Integer centerId = RequestContext.getCenterId();
    return getUsersOfCenter(centerId);
  }
  
  /**
   * Gets the users of center.
   *
   * @return the users of center
   */
  public List<BasicDynaBean> getUsersOfCenter(Integer centerId) {
    String centerWhereClause = "";
    MapSqlParameterSource parameters = new MapSqlParameterSource();

    if (centerId != 0) {
      centerWhereClause = " AND u.center_id in (:centerId,0)";
      parameters.addValue("centerId", centerId);

    }
    String orderQuery = " ORDER BY u.temp_username ";
    return DatabaseHelper.queryToDynaList(USERS_QUERY + centerWhereClause + orderQuery, parameters);
  }
  
  /**
   * Gets the users of center.
   *
   * @return the users of center
   */
  public List<BasicDynaBean> getHospitalUsersOfCenter(Integer centerId, String portalType) {
   
    StringBuilder whereClause = new StringBuilder(USERS_HOSPITAL_QUERY);
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    if (centerId != 0) {
      whereClause.append(" AND u.center_id in ( :centerId, 0 )");
      parameters.addValue("centerId", centerId);
    }
    whereClause.append(" AND ur.portal_id = :portalType");
    parameters.addValue("portalType", StringUtils.trimToEmpty(portalType));

    whereClause.append(" ORDER BY u.temp_username ");
    return DatabaseHelper.queryToDynaList(whereClause.toString() ,parameters);
  }
  
  /**
   * Gets the users of center.
   *
   * @return the users of center
   */
  public List<BasicDynaBean> getHospitalUsersOfCenter(Integer centerId, String portalType, 
      List<String> userIdFilter) {
    StringBuilder whereClause = new StringBuilder(USERS_HOSPITAL_QUERY);
    
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    
    if (centerId != 0) {
      whereClause.append(" AND u.center_id in ( :centerId, 0 )");
      parameters.addValue("centerId", centerId);
    }
    whereClause.append(" AND ur.portal_id = :portalType");
    parameters.addValue("portalType", StringUtils.trimToEmpty(portalType));
    
    if (!userIdFilter.isEmpty()) {
      whereClause.append(" AND u.emp_username in (:userIds)");
      parameters.addValue("userIds", userIdFilter);
    }

    whereClause.append(" ORDER BY u.temp_username ");
    return DatabaseHelper.queryToDynaList(whereClause.toString() ,parameters);
  }

  /** The get user hospital role ids query. */
  private static final String USERS_HOSPITAL_ROLE_IDS_QUERY = "SELECT hosp_role_id "
      + " FROM user_hosp_role_master WHERE u_user = ?";

  /**
   * Gets the user hospital role ids.
   *
   * @param username the username
   * @return the user hospital role ids
   */
  public List<BasicDynaBean> getUserHospitalRoleIds(String username) {
    return DatabaseHelper.queryToDynaList(USERS_HOSPITAL_ROLE_IDS_QUERY, username);
  }

  /** The get user hospital role ids query. */
  private static final String USERS_MALAFFIL_ROLES_QUERY = "SELECT hrm.malaffi_role "
      + " FROM hospital_roles_master hrm JOIN user_hosp_role_master uhrm "
      + " ON hrm.hosp_role_id = uhrm.hosp_role_id WHERE uhrm.u_user = ?";

  /**
   * Gets the highest level malaffi role for the user.
   *
   * @param username the username
   * @return the malaffi role
   */
  public String getUserMalaffiRole(String username) {
    List<BasicDynaBean> malaffiRoleBeans = DatabaseHelper
        .queryToDynaList(USERS_MALAFFIL_ROLES_QUERY, username);
    SortedSet<String> malaffiRoles = new TreeSet<>(new MalaffiRoleComparator());
    for (BasicDynaBean malaffiRoleBean : malaffiRoleBeans) {
      malaffiRoles.add((String) malaffiRoleBean.get("malaffi_role"));
    }
    if (malaffiRoles.isEmpty()) {
      return null;
    }
    return getMalaffiRoleName(malaffiRoles.first());
  }

  /**
   * Gets the malaffi role name.
   *
   * @param malaffiRole the malaffi role
   * @return the malaffi role name
   */
  private String getMalaffiRoleName(String malaffiRole) {
    if (malaffiRole == null) {
      return null;
    }
    if (malaffiRole.equals("P")) {
      return "Primary Provider";
    }
    if (malaffiRole.equals("S")) {
      return "Secondary Provider";
    }
    if (malaffiRole.equals("T")) {
      return "Tertiary Provider";
    }
    if (malaffiRole.equals("F")) {
      return "Front Desk";
    }
    return null;
  }
}
