package com.insta.hms.mdm.resourceoverride;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class ResourceOverrideRepository.
 */
@Repository
public class ResourceOverrideRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new resource override repository.
   */
  public ResourceOverrideRepository() {
    super("sch_resource_availability", "res_avail_id");
  }

  /** The Constant GET_RESOURCE_DETAILS. */
  public static final String GET_RESOURCE_DETAILS = " SELECT * FROM sch_resource_availability"
      + " sm where  res_sch_type = ? AND res_sch_name= ? ";

  /**
   * Gets the resources.
   *
   * @param resourceType the resource type
   * @param resourceName the resource name
   * @return the resources
   */
  public List<BasicDynaBean> getResources(String resourceType, String resourceName) {
    return DatabaseHelper.queryToDynaList(GET_RESOURCE_DETAILS, new Object[] { resourceType,
        resourceName });
  }

  /** The Constant GET_RESOURCE_DETAILS_BY_AVAILABILTY_DATE. */
  public static final String GET_RESOURCE_DETAILS_BY_AVAILABILTY_DATE = " SELECT * FROM "
      + "sch_resource_availability sm where  res_sch_type = ? AND res_sch_name= ? "
      + "AND availability_date = ? ";

  /** The Constant GET_RESOURCE_DETAILS_BETWEEN_TWO_DATE. */
  public static final String GET_RESOURCE_DETAILS_BETWEEN_TWO_DATE = " SELECT * FROM "
      + "sch_resource_availability sm where  res_sch_type = ? AND res_sch_name= ? "
      + "AND availability_date >= ? AND availability_date <= ? ";

  /**
   * Gets the resource by avail date.
   *
   * @param resourceType the resource type
   * @param resourceName the resource name
   * @param availDate the avail date
   * @param fromDate the from date
   * @param toDate the to date
   * @return the resource by avail date
   */
  public List<BasicDynaBean> getResourceByAvailDate(String resourceType, String resourceName,
      Date availDate, Date fromDate, Date toDate) {
    if (availDate != null) {
      return DatabaseHelper.queryToDynaList(GET_RESOURCE_DETAILS_BY_AVAILABILTY_DATE, new Object[] {
          resourceType, resourceName, availDate });
    } else {
      return DatabaseHelper.queryToDynaList(GET_RESOURCE_DETAILS_BETWEEN_TWO_DATE, new Object[] {
          resourceType, resourceName, fromDate, toDate });
    }
  }

  /** The Constant GET_DEFAULT_RESOURCE_DETAILS. */
  public static final String GET_DEFAULT_RESOURCE_DETAILS = " SELECT * FROM "
      + "scheduler_master sm where  res_sch_type = ? AND res_sch_name='*'";

  /**
   * Gets the default category.
   *
   * @param resourceType the resource type
   * @return the default category
   */
  public BasicDynaBean getDefaultCategory(String resourceType) {
    return DatabaseHelper.queryToDynaBean(GET_DEFAULT_RESOURCE_DETAILS,
        new Object[] { resourceType });
  }

  /** The Constant GET_RES_DETAILS. */
  public static final String GET_RES_DETAILS = " SELECT sra.*,# FROM "
      + "sch_resource_availability sra ";
  
  /** The dynamic query doctors. */
  private static final String DYNAMIC_QUERY_DOCTORS = "d.doctor_name AS resource_name ";
  
  /** The dynamic query test equipment. */
  private static final String DYNAMIC_QUERY_TEST_EQUIPMENT = "te.equipment_name AS resource_name ";
  
  /** The dynamic query service resources. */
  private static final String DYNAMIC_QUERY_SERVICE_RESOURCES = "srm.serv_resource_name "
      + "AS resource_name ";
  
  /** The dynamic query ot. */
  private static final String DYNAMIC_QUERY_OT = "tm.theatre_name AS resource_name ";
  
  /** The dynamic query test. */
  private static final String DYNAMIC_QUERY_TEST = "d.test_name AS resource_name ";
  
  /** The dynamic query service. */
  private static final String DYNAMIC_QUERY_SERVICE = "sv.service_name ||'('|| sd.department ||')'"
      + " AS resource_name ";
  
  /** The dynamic query surgery. */
  private static final String DYNAMIC_QUERY_SURGERY = "op.operation_name AS resource_name ";
  
  /** The dynamic query generic resource. */
  private static final String DYNAMIC_QUERY_GENERIC_RESOURCE = "grm.generic_resource_name "
      + "AS resource_name ";
  
  /** The get doctor details. */
  private static final String GET_DOCTOR_DETAILS = "JOIN doctors d ON"
      + "(d.doctor_id = sra.res_sch_name) ";
  
  /** The get equipment details. */
  private static final String GET_EQUIPMENT_DETAILS = " JOIN test_equipment_master te "
      + "ON(te.eq_id::text = sra.res_sch_name) ";
  
  /** The get ot details. */
  private static final String GET_OT_DETAILS = " JOIN theatre_master tm "
      + "ON(tm.theatre_id = sra.res_sch_name)";
  
  /** The get service resource details. */
  private static final String GET_SERVICE_RESOURCE_DETAILS = " JOIN service_resource_master srm "
      + "ON(srm.serv_res_id::text = sra.res_sch_name)";
  
  /** The get test details. */
  private static final String GET_TEST_DETAILS = " JOIN diagnostics d on(res_sch_name = test_id)";
  
  /** The get service details. */
  private static final String GET_SERVICE_DETAILS = " JOIN services sv "
      + "ON(sv.service_id= sra.res_sch_name) "
      + " JOIN services_departments sd ON(sd.serv_dept_id = sv.serv_dept_id)";
  
  /** The get operation details. */
  private static final String GET_OPERATION_DETAILS = " JOIN operation_master op "
      + "ON(op.op_id = sra.res_sch_name)";
  
  /** The get generic resource details. */
  private static final String GET_GENERIC_RESOURCE_DETAILS = " JOIN generic_resource_master grm "
      + "ON(grm.generic_resource_id::text = sra.res_sch_name)";

  /**
   * Gets the resource details.
   *
   * @param resAvailId the res avail id
   * @param resourceType the resource type
   * @return the resource details
   */
  public BasicDynaBean getResourceDetails(int resAvailId, String resourceType) {
    String query = GET_RES_DETAILS;
    if (resourceType.equals("DOC")) {
      query = query.replace("#", DYNAMIC_QUERY_DOCTORS) + GET_DOCTOR_DETAILS;
    } else if (resourceType.equals("EQID")) {
      query = query.replace("#", DYNAMIC_QUERY_TEST_EQUIPMENT) + GET_EQUIPMENT_DETAILS;
    } else if (resourceType.equals("SRID")) {
      query = query.replace("#", DYNAMIC_QUERY_SERVICE_RESOURCES) + GET_SERVICE_RESOURCE_DETAILS;
    } else if (resourceType.equals("THID")) {
      query = query.replace("#", DYNAMIC_QUERY_OT) + GET_OT_DETAILS;
    } else if (resourceType.equals("SER")) {
      query = query.replace("#", DYNAMIC_QUERY_SERVICE) + GET_SERVICE_DETAILS;
    } else if (resourceType.equals("SUR")) {
      query = query.replace("#", DYNAMIC_QUERY_SURGERY) + GET_OPERATION_DETAILS;
    } else if (resourceType.equals("TST")) {
      query = query.replace("#", DYNAMIC_QUERY_TEST) + GET_TEST_DETAILS;
    } else {
      query = query.replace("#", DYNAMIC_QUERY_GENERIC_RESOURCE) + GET_GENERIC_RESOURCE_DETAILS;
    }
    query = query + " WHERE res_sch_type= ? AND res_avail_id = ?";

    return DatabaseHelper.queryToDynaBean(query, new Object[] { resourceType, resAvailId });
  }

  /**
   * Gets the resource details by date.
   *
   * @param resSchType the res sch type
   * @param resSchName the res sch name
   * @return the resource details by date
   */
  public BasicDynaBean getResourceDetailsByDate(String resSchType, String resSchName) {
    String query = GET_RES_DETAILS;
    if (resSchType.equals("DOC")) {
      query = query.replace("#", DYNAMIC_QUERY_DOCTORS) + GET_DOCTOR_DETAILS;
    } else if (resSchType.equals("EQID")) {
      query = query.replace("#", DYNAMIC_QUERY_TEST_EQUIPMENT) + GET_EQUIPMENT_DETAILS;
    } else if (resSchType.equals("SRID")) {
      query = query.replace("#", DYNAMIC_QUERY_SERVICE_RESOURCES) + GET_SERVICE_RESOURCE_DETAILS;
    } else if (resSchType.equals("THID")) {
      query = query.replace("#", DYNAMIC_QUERY_OT) + GET_OT_DETAILS;
    } else if (resSchType.equals("SER")) {
      query = query.replace("#", DYNAMIC_QUERY_SERVICE) + GET_SERVICE_DETAILS;
    } else if (resSchType.equals("SUR")) {
      query = query.replace("#", DYNAMIC_QUERY_SURGERY) + GET_OPERATION_DETAILS;
    } else if (resSchType.equals("TST")) {
      query = query.replace("#", DYNAMIC_QUERY_TEST) + GET_TEST_DETAILS;
    } else {
      query = query.replace("#", " ");
    }
    query = query
        + " WHERE res_sch_type= ? AND res_sch_name = ? ORDER BY availability_date desc limit 1 ";
    return DatabaseHelper.queryToDynaBean(query, new Object[] { resSchType, resSchName });
  }

  /** The Constant GET_RESOURCES. */
  public static final String GET_RESOURCES = " SELECT * FROM sch_resource_availability sra where "
      + "res_sch_type = ? AND res_sch_name = ?  order by availability_date ";

  /**
   * Gets the resources list.
   *
   * @param resourceType the resource type
   * @param resourceName the resource name
   * @return the resources list
   */
  public List<BasicDynaBean> getResourcesList(String resourceType, String resourceName) {
    return DatabaseHelper.queryToDynaList(GET_RESOURCES,
        new Object[] { resourceType, resourceName });
  }

  /**
   * Delete resource timings.
   *
   * @param resAvailId the res avail id
   * @param resAvailDetId the res avail det id
   * @return true, if successful
   */
  public boolean deleteResourceTimings(int resAvailId, List resAvailDetId) {
    StringBuilder deleteResourceTiming = new StringBuilder(
        "delete from sch_resource_availability_details where res_avail_id = " + resAvailId);
    boolean success = true;
    int counter = 1;
    Iterator iterator = resAvailDetId.iterator();
    deleteResourceTiming.append(" AND res_avail_details_id NOT IN (");
    while (iterator.hasNext()) {
      if (counter == resAvailDetId.size()) {
        deleteResourceTiming.append(iterator.next());
      } else {
        deleteResourceTiming.append(iterator.next() + ",");
      }
      counter++;
    }
    deleteResourceTiming.append(")");
    int status = DatabaseHelper.delete(deleteResourceTiming.toString());
    if (status > 0) {
      return success;
    } else {
      return false;
    }

  }

  /** The Constant GET_RESOURCE_AVAILABLETIMING_LIST. */
  public static final String GET_RESOURCE_AVAILABLETIMING_LIST = " SELECT srad.*, hcm.center_name "
      + " from  sch_resource_availability_details srad "
      + " LEFT JOIN hospital_center_master hcm ON (srad.center_id = hcm.center_id) "
      + " WHERE res_avail_id = ? ORDER BY from_time ";

  /**
   * Gets the resource availtiming list.
   *
   * @param resAvailId the res avail id
   * @return the resource availtiming list
   */
  public static List<BasicDynaBean> getResourceAvailtimingList(int resAvailId) {
    return DatabaseHelper.queryToDynaList(GET_RESOURCE_AVAILABLETIMING_LIST,
        new Object[] { resAvailId });
  }

  /** The Constant GET_RESOURCE_OVERRIDES. */
  private static final String GET_RESOURCE_OVERRIDES = "SELECT *, sra.res_sch_name"
      + " AS resource_id FROM sch_resource_availability sra "
      + " JOIN sch_resource_availability_details srad ON (srad.res_avail_id = sra.res_avail_id)   "
      + " WHERE  res_sch_type = :resourceType AND res_sch_name IN (:resourceIds) AND "
      + " #AVAIL_START# #AVAIL_END# from_time != to_time  #  @ order by srad.from_time";

  /** The Constant CENTER_ID_WHERE_CLAUSE. */
  private static final String CENTER_ID_WHERE_CLAUSE = " AND center_id = :centerId  ";

  /** The Constant AVAILABILITY_STATUS_WHERE_CLAUSE. */
  private static final String AVAILABILITY_STATUS_WHERE_CLAUSE = " AND availability_status ="
      + " :availabilityStatus ";


  /**
   * Gets the resource overrides.
   *
   * @param resourceType the resource type
   * @param fromDate the from date
   * @param endDate the end date
   * @param resourceIds the resource ids
   * @param availabilityStatus the availability status
   * @param centerId the center id
   * @param centersIncDefault the centers inc default
   * @return the resource overrides
   */
  public List<BasicDynaBean> getResourceOverrides(String resourceType, Date fromDate, Date endDate,
      List<String> resourceIds, String availabilityStatus, Integer centerId,
      Integer centersIncDefault) {

    MapSqlParameterSource parameters = new MapSqlParameterSource();
    String query = GET_RESOURCE_OVERRIDES;
    parameters.addValue("resourceType", resourceType);
    parameters.addValue("resourceIds", resourceIds);
    if (fromDate != null) {
      query = query.replace("#AVAIL_START#", "availability_date >= :fromDate AND ");
      parameters.addValue("fromDate", fromDate);
    } else {
      query = query.replace("#AVAIL_START#", "");
    }
    if (endDate != null) {
      query = query.replace("#AVAIL_END#", "availability_date <= :endDate AND ");
      parameters.addValue("endDate", endDate);
    } else {
      query = query.replace("#AVAIL_END#", "");
    }
    if (availabilityStatus != null) {
      query = query.replace("#", AVAILABILITY_STATUS_WHERE_CLAUSE);
      parameters.addValue("availabilityStatus", availabilityStatus);
    } else {
      query = query.replace("#", "");
    }
    if (centerId != null && centersIncDefault > 1) {
      query = query.replace("@", CENTER_ID_WHERE_CLAUSE);
      parameters.addValue("centerId", centerId);
    } else {
      query = query.replace("@", "");
    }
    return DatabaseHelper.queryToDynaList(query, parameters);

  }

  /** The Constant CHECK_OVERRIDES_FOR_RANGE. */
  private static final String CHECK_OVERRIDES_FOR_RANGE = " select res_sch_name from "
      + " sch_resource_availability where res_sch_name in (#) and "
      + " availability_date>=? and availability_date<=? ";

  /**
   * Check override exists.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param resourceIds the resource ids
   * @return true, if successful
   */
  public boolean checkOverrideExists(java.util.Date startDate, java.util.Date endDate,
      String[] resourceIds) {
    StringBuilder sbf = new StringBuilder();
    Object[] params = new Object[resourceIds.length + 2];
    int inc = 0;
    for (inc = 0; inc < resourceIds.length; inc++) {
      params[inc] = resourceIds[inc];
      sbf.append("?,");

    }
    params[inc++] = startDate;
    params[inc++] = startDate;

    String query = CHECK_OVERRIDES_FOR_RANGE;
    query = query.replace("#", sbf.deleteCharAt(sbf.length() - 1).toString());
    return DatabaseHelper.queryToDynaList(query, params).size() > 0;
  }
  
  /** The Constant DELETE_OVERRIDE_DETAILS_IN_RANGE. */
  private static final String DELETE_OVERRIDE_DETAILS_IN_RANGE = "delete from "
      + "sch_resource_availability_details where res_avail_id in "
      + "(select res_avail_id from sch_resource_availability where res_sch_name in (#) "
      + "and availability_date>=? and availability_date<=? )";
  
  /** The Constant DELETE_OVERRIDE_IN_RANGE. */
  private static final String DELETE_OVERRIDE_IN_RANGE = " delete from "
      + " sch_resource_availability where res_sch_name in (#) and "
      + " availability_date>=? and availability_date<=? ";

  /**
   * Delete existing override.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param resourceIds the resource ids
   * @return true, if successful
   */
  public boolean deleteExistingOverride(java.util.Date startDate, java.util.Date endDate,
      String[] resourceIds) {
    StringBuilder sbf = new StringBuilder();
    Object[] params = new Object[resourceIds.length + 2];
    int inc = 0;
    for (inc = 0; inc < resourceIds.length; inc++) {
      params[inc] = resourceIds[inc];
      sbf.append("?,");

    }
    params[inc++] = startDate;
    params[inc++] = endDate;

    String paramString = sbf.deleteCharAt(sbf.length() - 1).toString();
    String queryDetails = DELETE_OVERRIDE_DETAILS_IN_RANGE;
    queryDetails = queryDetails.replace("#", paramString);

    String query = DELETE_OVERRIDE_IN_RANGE;
    query = query.replace("#", paramString);
    int detailsDeleted = DatabaseHelper.delete(queryDetails, params);
    int overrideDeleted = DatabaseHelper.delete(query, params);
    return overrideDeleted > 0 && detailsDeleted > 0;
  }

  /** The Constant Resource_TABLES. */
  public static final String Resource_TABLES = "FROM (SELECT res_avail_id,"
      + " res_sch_type,res_sch_name,availability_date, "
      + " (CASE WHEN sra.res_sch_type = 'SER' THEN s.service_name "
      + " WHEN sra.res_sch_type = 'DOC' THEN d.doctor_name "
      + " WHEN sra.res_sch_type = 'EQID' THEN tq.equipment_name "
      + " WHEN sra.res_sch_type = 'SRID' THEN sr.serv_resource_name "
      + " WHEN sra.res_sch_type = 'THID' THEN th.theatre_name "
      + " WHEN sra.res_sch_type = 'TST' THEN diag.test_name "
      + " WHEN sra.res_sch_type = 'SER' THEN s.service_name "
      + " WHEN sra.res_sch_type = 'SUR' THEN om.operation_name "
      + " ELSE grm.generic_resource_name END) AS resource_name "
      + " FROM sch_resource_availability sra "
      + " LEFT JOIN doctors d ON d.doctor_id = sra.res_sch_name AND sra.res_sch_type = 'DOC' "
      + " LEFT JOIN test_equipment_master tq ON tq.eq_id::text = sra.res_sch_name::text "
      + " AND sra.res_sch_type = 'EQID' "
      + " LEFT JOIN service_resource_master sr ON sr.serv_res_id::text = sra.res_sch_name::text "
      + " AND sra.res_sch_type = 'SRID' "
      + " LEFT JOIN theatre_master th ON th.theatre_id = sra.res_sch_name "
      + " AND sra.res_sch_type = 'THID' "
      + " LEFT JOIN diagnostics diag ON diag.test_id = sra.res_sch_name "
      + " AND sra.res_sch_type = 'TST' "
      + " LEFT JOIN services s ON s.service_id = sra.res_sch_name "
      + " AND sra.res_sch_type = 'SER' "
      + " LEFT JOIN operation_master om ON om.op_id = sra.res_sch_name "
      + " AND sra.res_sch_type = 'SUR' "
      + " LEFT JOIN generic_resource_master grm ON grm.generic_resource_id::text = "
      + " sra.res_sch_name::text) AS FOO ";

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterRepository#getSearchQuery()
   */
  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(Resource_TABLES);
  }

  /** The Constant RESOURCE_OVERRIDE_EXISTS. */
  private static final String RESOURCE_OVERRIDE_EXISTS = " Select exists(Select 1 from "
      + " sch_resource_availability ra JOIN sch_resource_availability_details rad "
      + " ON (ra.res_avail_id = rad.res_avail_id)"
      + " where ra.res_sch_name = ? and "
      + " ra.availability_date >= ?::date and rad.to_time >= ?::time "
      + " and visit_mode in (?, ?)) ";
  

  /**
   * Resource override exists.
   *
   * @param resSchId the res sch id
   * @param currentDateTime the current date time
   * @param visitMode the visit mode
   * @return true, if successful
   */
  public boolean resourceOverrideExists(String resSchId, 
      Timestamp currentDateTime, String visitMode) {
    String query = RESOURCE_OVERRIDE_EXISTS;
    Date date = new Date(currentDateTime.getTime());
    Time time = new Time(currentDateTime.getTime());
    String bothVisitMode = "B";
    Object[] params = new Object[] {resSchId, date, time, visitMode, bothVisitMode};
    return DatabaseHelper.getBoolean(query,params);
  }

}