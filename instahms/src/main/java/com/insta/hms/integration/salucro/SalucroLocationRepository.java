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
 * The Class SalucroLocationRepository.
 */
@Repository
public class SalucroLocationRepository extends GenericRepository {

  /**
   * Instantiates a new salucro location repository.
   */
  public SalucroLocationRepository() {
    super("salucro_location_mapping");
  }


  /** To get salucro location details. */
  private static final String GET_LOCATION_DETAILS = "Select * "
        + "from salucro_location_mapping where #STATUS_FILTER# #COUNTER_FILTER# #CENTER_FILTER# ";


  /** The Constant COUNTER_FILTER. */
  private static final String COUNTER_FILTER = " AND counter_id = :counterId ";

  /** The Constant CENTER_FILTER. */
  private static final String CENTER_FILTER = " AND center_id = :centerId ";

  /** The Constant STATUS_FILTER. */
  private static final String STATUS_FILTER = " status in ( :status ) ";

  /** The Constant COUNTER_FIELDS. */
  private static final String COUNTER_FIELDS = "SELECT *" ; 

  /** The Constant COUNT_FIELD. */
  private static final String COUNT_FIELD = "SELECT count(*) ";

  /** The Constant TABLES. */
  private static final String TABLES = " FROM (select slm.*, "
      + " cc.counter_no, cc.counter_type,hcm.center_name "
      + " From salucro_location_mapping slm "
      + " LEFT JOIN counters cc ON(slm.counter_id = cc.counter_id) "
      + " LEFT JOIN hospital_center_master hcm ON (slm.center_id = hcm.center_id)) as foo";

  /** The Constant ACTIVE_STATUS. */
  private static final String ACTIVE_STATUS = "A";

  /** The Constant INACTIVE_STATUS. */
  private static final String INACTIVE_STATUS = "I";

  /**
   * Gets the salucro location details.
   *
   * @param counterId the counter id
   * @param centerId the center id
   * @return the salucro location details
   */
  public List<BasicDynaBean> getSalucroLocationDetailsList(String counterId, Integer centerId) {
    String query = GET_LOCATION_DETAILS;
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    List<String> statusList = new ArrayList<String>();
    query = query.replace("#STATUS_FILTER#", STATUS_FILTER);
    statusList.add(ACTIVE_STATUS);
    parameters.addValue("status", statusList);
    if ( counterId != null ) {
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
    return DatabaseHelper.queryToDynaList(query, parameters);
  }

  /**
   * Check if counter exists.
   *
   * @param bean the bean
   * @return the string
   */
  public String checkIfCounterExists(Map bean, String actionType) {
    String counterId = (String) bean.get("counter_id");
    Integer centerId = (Integer) bean.get("center_id");
    String status = (String) bean.get("status");
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    List<String> statusList = new ArrayList<String>();
    if ( actionType.equalsIgnoreCase("update") && status != null) {
      statusList.add(status);      
    } else {
      statusList.add(ACTIVE_STATUS);
      statusList.add(INACTIVE_STATUS);
    }
    String query = GET_LOCATION_DETAILS;
    if ( !StringUtils.isEmpty(counterId) && centerId != null ) {
      query = query.replace("#STATUS_FILTER#", STATUS_FILTER);
      parameters.addValue("status", statusList);
      query = query.replace("#COUNTER_FILTER#", COUNTER_FILTER);
      parameters.addValue("counterId", counterId);
      query = query.replace("#CENTER_FILTER#", CENTER_FILTER);
      parameters.addValue("centerId", centerId);
      List<BasicDynaBean> userList = DatabaseHelper.queryToDynaList(query, parameters);
      BasicDynaBean userBean = userList != null  
           && userList.size() > 0 ? userList.get(0) : null;
      if (userBean != null) {
        return (String) userBean.get("counter_id");
      }
    }
    return null;
  }

  /**
   * Gets the counter deatils map.
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

    SearchQueryAssembler qb = new SearchQueryAssembler(COUNTER_FIELDS, COUNT_FIELD, TABLES, null,
        sortField, sortRev, pageSize, pageNum);
    if (params != null) {
      if (params.containsKey("status") 
              && !params.get("status")[0].trim().isEmpty()) {
        qb.addFilter(QueryAssembler.STRING, "status","=",
              params.get("status")[0].trim());
      }

      if (params.containsKey("counter_no")
              && !params.get("counter_no")[0].trim().isEmpty()) {
        qb.addFilter(QueryAssembler.STRING,"counter_no","ILIKE",
              params.get("counter_no")[0].trim());
      }
    }
    int centerId = RequestContext.getCenterId();
    if (centerId != 0) {
      qb.addFilter(QueryAssembler.INTEGER, "center_id", "=", centerId);
    }
    qb.build();
    PagedList counterDetailList = qb.getMappedPagedList();
    return counterDetailList;
  }

  /**
   * Update.
   *
   * @param salucroBean the salucro bean
   * @param editCounterId the edit counter id
   * @return the integer
   */
  public Integer update(BasicDynaBean salucroBean, Long editCounterId) {
    Map<String, Object> keys = new HashMap<String, Object>(); 
    keys.put("salucro_location_mapping_id", editCounterId);
    keys.put("center_id", salucroBean.get("center_id"));
    return update(salucroBean, keys);
  }
}