package com.insta.hms.integration.salucro;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.SearchQueryAssembler;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class SalucroRoleRepository.
 */
@Repository
public class SalucroRoleRepository extends GenericRepository {

  /**
   * Instantiates a new salucro role repository.
   */
  public SalucroRoleRepository() {
    super("salucro_role_mapping");
  }

  /** To get salucro role details. */
  private static final String GET_ROLE_DETAILS = "Select * from "
      + " salucro_role_mapping where  #USER_FILTER#  "
      + " #CENTER_FILTER# #ROLE_FILTER# #COUNTER_FILTER# #STATUS_FILTER# ";

  /** The Constant STATUS_FILTER. */
  private static final String STATUS_FILTER = " AND status in ( :status ) ";

  /** The Constant USER_FILTER. */
  private static final String USER_FILTER = " emp_username=:userName ";

  /** The Constant CENTER_FILTER. */
  private static final String CENTER_FILTER = " AND center_id = :centerId ";

  /** The Constant ROLE_FILTER. */
  private static final String ROLE_FILTER = " AND role = :role ";

  /** The Constant COUNTER_FILTER. */
  private static final String COUNTER_FILTER = " AND counter_id = :counterId ";

  /** The Constant USER_FIELDS. */
  private static final String USER_FIELDS = "SELECT *" ; 

  /** The Constant COUNT_FIELD. */
  private static final String COUNT_FIELD = "SELECT count(*) ";

  /** The Constant TABLES. */
  private static final String TABLES = " FROM (select srm.*, "
      + " uu.temp_username, hcm.center_name, cc.counter_no  "
      + " From salucro_role_mapping srm "
      + " LEFT JOIN u_user uu ON(srm.emp_username = uu.emp_username) "
      + " LEFT JOIN counters cc on (srm.counter_id = cc.counter_id) "
      + " LEFT JOIN hospital_center_master hcm ON (srm.center_id = hcm.center_id)) as foo"; 

  /** The Constant ACTIVE_STATUS. */
  private static final String ACTIVE_STATUS = "A";

  /** The Constant INACTIVE_STATUS. */
  private static final String INACTIVE_STATUS = "I"; 
  
  /**
   * Gets the salucro role details list.
   *
   * @param userName the user name
   * @param centerId the center id
   * @return the salucro role details
   */
  public List<BasicDynaBean> getSalucroRoleDetailsList(
        String userName, Integer centerId, String counterId) {
    String query = GET_ROLE_DETAILS;
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    List<String> statusList = new ArrayList<String>();
    statusList.add(ACTIVE_STATUS);
    query = query.replace("#STATUS_FILTER#", STATUS_FILTER);
    parameters.addValue("status", statusList );
    query = query.replace("#USER_FILTER#", USER_FILTER);
    parameters.addValue("userName", userName);
    if ( counterId != null) {
      query = query.replace("#COUNTER_FILTER#", COUNTER_FILTER);
      parameters.addValue("counterId", counterId);
    } else {
      query = query.replace("#COUNTER_FILTER#", "");   
    }

    if ( centerId != null ) {
      query = query.replace("#CENTER_FILTER#", CENTER_FILTER);
      parameters.addValue("centerId", centerId);
    } else {
      query = query.replace("#CENTER_FILTER#", "");
    }
    query = query.replace("#ROLE_FILTER#", "");
    return DatabaseHelper.queryToDynaList(query, parameters);
  }

  /**
   * Check if user exists.
   *
   * @param bean the bean
   * @return the string
   */
  public String checkIfUserExists(Map bean, String actionType) {
    String userName = (String) bean.get("emp_username");
    Integer centerId = (Integer) bean.get("center_id");
    String role = (String) bean.get("role");
    String status = (String) bean.get("status");
    String counterId = (String) bean.get("counter_id");   
    String query = GET_ROLE_DETAILS;
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    List<String> statusList = new ArrayList<String>();
    if ( actionType.equalsIgnoreCase("update") && status != null ) {
      statusList.add(status);      
    } else {
      statusList.add(ACTIVE_STATUS);
      statusList.add(INACTIVE_STATUS);
    }
    if ( !StringUtils.isEmpty(userName) && centerId != null 
         && !StringUtils.isEmpty(role) ) {
      query = query.replace("#USER_FILTER#", USER_FILTER);
      parameters.addValue("userName", userName);
      query = query.replace("#CENTER_FILTER#", CENTER_FILTER);
      parameters.addValue("centerId", centerId);
      query = query.replace("#ROLE_FILTER#", ROLE_FILTER);
      parameters.addValue("role", role);
      
      if ( counterId != null ) {
        query = query.replace("#COUNTER_FILTER#", COUNTER_FILTER);
        parameters.addValue("counterId", counterId);
      } else {
        query = query.replace("#COUNTER_FILTER#", "");
      }
      query = query.replace("#STATUS_FILTER#", STATUS_FILTER);
      parameters.addValue("status", statusList);
      List<BasicDynaBean> userList = DatabaseHelper.queryToDynaList(query, parameters);
      BasicDynaBean userBean = userList != null  
           && userList.size() > 0 ? userList.get(0) : null;
      if (userBean != null) {
        return (String) userBean.get("emp_username");
      }
    }
    return null;
  }

  /**
   * Gets the user deatils map.
   *
   * @param params          the params
   * @param listingParams the listing params
   * @return the user deatils map
   * @throws ParseException           the parse exception
   */
  public PagedList search(
        Map<String, String[]> params, Map<LISTING, Object> listingParams ) throws ParseException {
    int pageSize = (Integer) listingParams.get(LISTING.PAGESIZE);
    int pageNum = (Integer) listingParams.get(LISTING.PAGENUM);
    String sortField = (String) listingParams.get(LISTING.SORTCOL);
    boolean sortRev = (Boolean) listingParams.get(LISTING.SORTASC);

    SearchQueryAssembler qb = new SearchQueryAssembler(USER_FIELDS, COUNT_FIELD, TABLES, null,
        sortField, sortRev, pageSize, pageNum);
    if (params != null) {
      if (params.containsKey("status") 
              && !params.get("status")[0].trim().isEmpty()) {
        qb.addFilter(QueryAssembler.STRING, "status","=",
              params.get("status")[0].trim());
      }

      if (params.containsKey("emp_username")
              && !params.get("emp_username")[0].trim().isEmpty()) {
        qb.addFilter(QueryAssembler.STRING,"emp_username","ILIKE",
              params.get("emp_username")[0].trim());
      }
    }
    int centerId = RequestContext.getCenterId();
    if (centerId != 0) {
      qb.addFilter(QueryAssembler.INTEGER, "center_id", "=", centerId);
    }
    qb.build();
    PagedList userDetailList = qb.getMappedPagedList();
    return userDetailList;
  }

  /**
   * Update.
   *
   * @param salucroBean the salucro bean
   * @param editUserId the edit user id
   * @return the integer
   */
  public Integer update(BasicDynaBean salucroBean, Long editUserId) {
    Map<String, Object> keys = new HashMap<String, Object>(); 
    keys.put("salucro_role_mapping_id", editUserId);
    keys.put("center_id", salucroBean.get("center_id"));
    return update(salucroBean, keys);
  }

}