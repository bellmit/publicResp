package com.insta.hms.core.scheduler.resourcelist;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.scheduler.AdditionalResourcesQueryProvider;
import com.insta.hms.core.scheduler.AppointmentCategory;
import com.insta.hms.core.scheduler.AppointmentCategoryFactory;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class SchedulerResourceRepository.
 */
@Repository
public class SchedulerResourceRepository extends GenericRepository {

  /** The logger. */
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  /** The gen pref service. */
  @LazyAutowired
  private GenericPreferencesService genPrefService;
  
  @LazyAutowired
  private AdditionalResourcesQueryProvider additionalResourcesQueryProvider;

  /** The scheduler factory. */
  @LazyAutowired
  private AppointmentCategoryFactory schedulerFactory;

  /** The Constant TODAYS_RESOURCES. */
  private static final String TODAYS_RESOURCES = "SELECT ra.res_sch_name AS resource_id "
      + "FROM sch_resource_availability ra "
      + " JOIN sch_resource_availability_details rad ON (ra.res_avail_id=rad.res_avail_id "
      + "and ra.availability_date = :availabilityDate and rad.availability_status = 'A') "
      + " UNION "
      + " SELECT sm.res_sch_name AS resource_id from scheduler_master sm"
      + " JOIN sch_default_res_availability_details sdrd on (sm.res_sch_id=sdrd.res_sch_id "
      + "and sm.res_sch_name != '*' and res_sch_category = :resSchCategory and "
      + "sdrd.day_of_week = :dayOfWeek and sdrd.availability_status = 'A') "
      + " EXCEPT "
      + " SELECT ra.res_sch_name AS resource_id FROM sch_resource_availability ra"
      + " JOIN sch_resource_availability_details rad ON (ra.res_avail_id=rad.res_avail_id "
      + "and rad.availability_status='N' and rad.from_time = '00:00:00' and rad.to_time='23:59:00'"
      + " and ra.availability_date = :availabilityDate)) as foo";

  /** The Constant GENERIC_RESOURCES. */
  private static final String GENERIC_RESOURCES = " select generic_resource_id::text as "
      + "resource_id,generic_resource_name as resourcename,grt.scheduler_resource_type "
      + "as resource_type,center_id, overbook_limit "
      + " from generic_resource_master grm join generic_resource_type grt ON"
      + "(grt.generic_resource_type_id = grm.generic_resource_type_id) "
      + " where grm.status = 'A' AND grt.status = 'A' AND schedule = true ";

  /**
   * Instantiates a new scheduler resource repository.
   */
  public SchedulerResourceRepository() {
    super("scheduler_master");
  }

  /**
   * Parses the bool.
   *
   * @param values the values
   * @return true, if successful
   */
  private boolean parseBool(String values) {
    return values != null && Arrays.asList("true", "y", "yes", "1").contains(values.toLowerCase());
  }

  /**
   * Gets the query.
   *
   * @param table the table
   * @param fields the fields
   * @param joins the joins
   * @param filters the filters
   * @param groupBy the group by
   * @param sortOn the sort on
   * @param joinsori the joinsori
   * @return the query
   */
  private String getQuery(String table, List<String> fields, List<String> joins,
      List<String> filters, List<String> groupBy, List<String> sortOn, List<String> joinsori) {
    List<String> queryParts = new ArrayList<String>(Arrays.asList("SELECT"));
    queryParts.add(StringUtils.collectionToDelimitedString(fields, ", "));
    queryParts.add("FROM " + table);
    if (!joins.isEmpty()) {
      queryParts.add("LEFT JOIN " + StringUtils.collectionToDelimitedString(joins, " LEFT JOIN "));
    }
    if (!joinsori.isEmpty()) {
      queryParts.add("JOIN " + StringUtils.collectionToDelimitedString(joinsori, " JOIN "));
    }
    if (!filters.isEmpty()) {
      queryParts.add("WHERE " + StringUtils.collectionToDelimitedString(filters, " AND "));
    }
    if (!groupBy.isEmpty()) {
      queryParts.add("GROUP BY " + StringUtils.collectionToDelimitedString(groupBy, ", "));
    }
    if (!sortOn.isEmpty()) {
      queryParts.add("ORDER BY " + StringUtils.collectionToDelimitedString(sortOn, ", "));
    }
    return StringUtils.collectionToDelimitedString(queryParts, " ");
  }

  /**
   * Gets the scheduler secondary resources.
   *
   * @param appointmentCategory the appointment category
   * @param params the params
   * @param advanced the advanced
   * @return the scheduler secondary resources
   */
  public Map<String, Object> getSchedulerSecondaryResources(
      AppointmentCategory appointmentCategory, Map<String, String[]> params, boolean advanced) {
    Date today = DateUtil.getCurrentDate();
    // ?
    boolean userIsDoctor = params.get("user_is_doctor") != null ? parseBool(params
        .get("user_is_doctor")[0]) : false;
    boolean loginControlsApplicable = params.get("login_controls_applicable") != null
        ? parseBool(params.get("login_controls_applicable")[0]) : false;

    String category = params.get("res_sch_category") != null ? params.get("res_sch_category")[0]
        : "DOC";
    String[] deptList = params.get("departments") != null ? params.get("departments") : null;
    List<String> filters = new ArrayList<String>();
    List<String> joins = new ArrayList<String>();
    List<String> joinsorigi = new ArrayList<String>();
    String table = null;
    Integer centerId = Integer.parseInt(params.get("center_id")[0]);
    Integer centersIncDefault = Integer.parseInt(params.get("max_centers_inc_default")[0]);
    String masterData = appointmentCategory.getPrimaryResourceMasterQuery();
    MapSqlParameterSource sqlParams = new MapSqlParameterSource();
    // center applicability for resources
    if (centersIncDefault > 0 && centerId != 0) {
      masterData = masterData.replace("#CENTER_FILTER#", "AND center_id IN (:centerIds)");
      sqlParams.addValue("centerIds", Arrays.asList(0, centerId));
    } else {
      masterData = masterData.replace("#CENTER_FILTER#", "");
    }

    // if department filter applies
    if ((deptList != null && deptList.length > 0 && deptList[0] != null && !deptList[0].equals("")
        && !deptList[0].isEmpty()) && appointmentCategory.getCategory().equals("DOC")
        && (!userIsDoctor || !loginControlsApplicable)) {
      String[] finalDeptList = deptList[0].split(",");
      masterData = masterData.replace("#DEPT_FILTER#", appointmentCategory.getDeptFilterClause());
      sqlParams.addValue("deptIds", Arrays.asList(finalDeptList));
    } else {
      masterData = masterData.replace("#DEPT_FILTER#", "");
    }

    // user logged in is doctor
    if (appointmentCategory.getCategory().equals("DOC") && userIsDoctor
        && loginControlsApplicable) {
      String doctorId = params.get("doctor")[0];
      String doctorIdFilter = "AND d.doctor_id = '" + doctorId + "' ";
      masterData = masterData.replace("#RESOURCE_FILTER#", doctorIdFilter);
    } else if (appointmentCategory.getCategory().equals("DOC") && userIsDoctor) {
      String doctorId = params.get("doctor")[0];
      String doctorIdFilter = "AND d.doctor_id != '" + doctorId + "' ";
      masterData = masterData.replace("#RESOURCE_FILTER#", doctorIdFilter);
    } else {
      masterData = masterData.replace("#RESOURCE_FILTER#", "");
    }
    boolean isTodaysRes = params.get("today") != null ? parseBool(params.get("today")[0]) : false;
    if (isTodaysRes) {
      // TODO:: Query does not return resources whose default availability belongs to '*' group
      table = "(" + TODAYS_RESOURCES + ")";
      joins.add("(" + masterData + ") AS resource_data USING (resource_id) ");
      sqlParams.addValue("resSchCategory", category);
      sqlParams.addValue("availabilityDate", today);
      int weekDayNo = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
      sqlParams.addValue("dayOfWeek", weekDayNo);
    } else {
      table = "(" + masterData + ") AS resource_data ";
      joins
          .add("scheduler_master sm ON (sm.res_sch_category = 'DOC' AND sm.res_sch_name = "
              + "resource_id AND sm.status = 'A' AND sm.res_sch_type = 'DOC')");
      joinsorigi
          .add("scheduler_master sm_all ON (sm_all.res_sch_category = 'DOC' AND "
              + "sm_all.res_sch_name = '*' AND sm_all.status = 'A' AND "
              + "sm_all.res_sch_type = 'DOC')");
    }
    String[] paramFindString = params.get("find_string");
    String resourceName = (paramFindString == null || paramFindString[0] == null) ? ""
        : paramFindString[0].trim();
    if (!resourceName.isEmpty()) {
      filters.add("resource_name ilike :resourceName");
      sqlParams.addValue("resourceName", "%" + resourceName + "%");
    }
    List<String> fields = new ArrayList<String>();
    fields.addAll(Arrays.asList("resource_id", "resource_name", "resource_type", "dept_id",
        "contact_number", "dept_id", "dept_name", "overbook_limit",
        "coalesce(sm.default_duration,sm_all.default_duration) AS default_duration"));
    String[] resourceIds = params.get("resources");
    if (category.equalsIgnoreCase("DOC")) {
      fields.add("available_for_online_consults");
    }
    List<String> sortOn = new ArrayList<String>();
    String sortOrder;
    if (resourceIds != null) {
      List<String> resList = new ArrayList<String>();
      for (String resourceId : resourceIds) {
        resList.add(resourceId);
      }
      String resQuery = "resource_id in (:reslist)  DESC , ";
      String sortParam = params.get("sort_order") != null ? params.get("sort_order")[0]
          : "lower(resource_name)";
      sortOrder = resQuery + sortParam;
      sqlParams.addValue("reslist", resList);
    } else {
      sortOrder = params.get("sort_order") != null ? params.get("sort_order")[0]
          : "lower(resource_name)";
    }
    sortOn.add(sortOrder);
    boolean withCount = params.get("with_count") != null ? parseBool(params.get("with_count")[0])
        : false;
    if (withCount) {
      fields.add("count(*) over (partition by 1) as cn");
    }
    List<String> groupBy = new ArrayList<String>();
    String querySql = getQuery(table, fields, joins, filters, groupBy, sortOn, joinsorigi);
    // pagination
    int pageSize = params.get("page_size") != null ? Integer.parseInt(params.get("page_size")[0])
        : 15;
    int pageNum = params.get("page_num") != null ? Integer.parseInt(params.get("page_num")[0]) : 1;
    // removing this restriction as the client can have this validation
    // pageSize = pageSize > 100 ? 100 : pageSize;
    if (pageSize != 0) {
      querySql += " LIMIT :limit";
      sqlParams.addValue("limit", pageSize);
    }
    if (pageNum != 0) {
      querySql += " OFFSET :offset";
      sqlParams.addValue("offset", (pageNum - 1) * pageSize);
    }
    List<BasicDynaBean> resourceList = DatabaseHelper.queryToDynaList(querySql, sqlParams);
    Map<String, Object> result = new HashMap<String, Object>();
    List<Map<String, Object>> resultresourceList = ConversionUtils.listBeanToListMap(resourceList);
    result.put(
        "total_records",
        (withCount && !resourceList.isEmpty()) ? Integer.parseInt(resourceList.get(0).get("cn")
            .toString()) : 0);
    result.put("page_size", resourceList.size());
    result.put("page_num", pageNum);
    result.put("resources", resultresourceList);
    return result;
  }

  /**
   * Gets the additional resources by resource type.
   *
   * @param appointmentCategory the appointment category
   * @param resTypes the res types
   * @param centerId the center id
   * @return the additional resources by resource type
   */
  public List<BasicDynaBean> getAdditionalResourcesByResourceType(
      AppointmentCategory appointmentCategory, String[] resTypes, Integer centerId) {
    List<BasicDynaBean> resourceList = new ArrayList<BasicDynaBean>();
    for (String resType : resTypes) {
      String query = 
          additionalResourcesQueryProvider.getAdditionalResourceMasterQueryByType(resType);
      MapSqlParameterSource sqlParams = new MapSqlParameterSource();
      if (centerId != null) {
        query = query.replace("#CENTER_FILTER#", " AND center_id IN (:centerIds) ");
        query = query.replace("#GEN_TYPE_FILTER#", " AND grt.scheduler_resource_type = :resType ");
        sqlParams.addValue("centerIds", Arrays.asList(0, centerId));
        sqlParams.addValue("resType",resType);
      }
      List<BasicDynaBean> list = DatabaseHelper.queryToDynaList(query, sqlParams);
      resourceList.addAll(list);
    }
    return resourceList;
  }
}