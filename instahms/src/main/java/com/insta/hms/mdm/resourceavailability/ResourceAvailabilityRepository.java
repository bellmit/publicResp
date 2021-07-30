package com.insta.hms.mdm.resourceavailability;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;
import com.insta.hms.resourcescheduler.ResourceDTO;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// TODO: Auto-generated Javadoc
/** The Class ResourceAvailabilityRepository. */
@Repository
public class ResourceAvailabilityRepository extends MasterRepository<Integer> {

  /** The pref service. */
  @LazyAutowired private GenericPreferencesService prefService;

  /** Instantiates a new resource availability repository. */
  public ResourceAvailabilityRepository() {
    super("scheduler_master", "res_sch_id");
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterRepository#getSearchQuery()
   */
  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(Category_TABLES);
  }

  /** The Category FIELDS. */
  public static final String Category_FIELDS = "SELECT * ";

  /** The Category COUNT. */
  public static final String Category_COUNT = "SELECT count(*)";

  /** The Category TABLES. */
  public static final String Category_TABLES = " FROM (SELECT sm.res_sch_id,sm.res_sch_category,"
      + " sm.res_sch_type,sm.dept,sm.res_sch_name,sm.description,sm.default_duration,"
      + "coalesce((case when sm.res_sch_category='DOC' then d.doctor_name "
      + " when sm.res_sch_category='OPE' THEN "
      + " (CASE WHEN res_sch_type = 'SUR'  THEN ope.operation_name "
      + " WHEN res_sch_type = 'THID' THEN tm.theatre_name"
      + " ELSE (CASE WHEN sm.res_sch_name != '*' THEN grm.generic_resource_name end) end)"
      + " WHEN sm.res_sch_category='DIA' THEN "
      + " (CASE WHEN res_sch_type='TST' THEN dia.test_name "
      + " WHEN res_sch_type = 'EQID' THEN tem.equipment_name "
      + " ELSE (CASE WHEN sm.res_sch_name != '*' THEN grm.generic_resource_name end) end)"
      + " WHEN sm.res_sch_category='SNP' THEN "
      + " (CASE WHEN res_sch_type = 'SER' THEN serv.service_name "
      + " WHEN res_sch_type ='SRID' THEN srm.serv_resource_name "
      + " ELSE (CASE WHEN sm.res_sch_name != '*' THEN grm.generic_resource_name end) end) end),"
      + " CASE WHEN res_sch_category = 'GEN' THEN grm.generic_resource_name end) as resource_name,"
      + " CASE when sm.res_sch_type = 'DOC' AND res_sch_name != '*' THEN "
      + " (SELECT dept_name from department where dept_id = sm.dept) "
      + " when sm.res_sch_type = 'SER' AND res_sch_name != '*' THEN "
      + " (SELECT department from services_departments where serv_dept_id::text = sm.dept) "
      + " when sm.res_sch_type = 'TST' AND res_sch_name != '*' THEN "
      + " (SELECT ddept_name from diagnostics_departments where ddept_id = sm.dept) "
      + " when sm.res_sch_type = 'SUR' AND res_sch_name != '*' THEN "
      + " (SELECT dept_name from department where dept_id = sm.dept)"
      + "   else 'Any' END AS dept_name, sm.status, "
      + " CASE when sm.res_sch_type = 'DOC' AND res_sch_name != '*' THEN "
      + " (SELECT status from doctors where doctor_id = sm.res_sch_name) "
      + " when sm.res_sch_type = 'SER' AND res_sch_name != '*' THEN "
      + " (SELECT status from services where service_id = sm.res_sch_name) "
      + " when sm.res_sch_type = 'THID' AND res_sch_name != '*' THEN "
      + " (SELECT status from theatre_master where theatre_id = sm.res_sch_name) "
      + " when sm.res_sch_type = 'SRID' AND res_sch_name != '*' THEN "
      + " (SELECT status from service_resource_master where serv_res_id::text = sm.res_sch_name) "
      + " when sm.res_sch_type = 'EQID' AND res_sch_name != '*' THEN "
      + " (SELECT status from test_equipment_master where eq_id::text = sm.res_sch_name) "
      + " when sm.res_sch_type = 'SUR' AND res_sch_name != '*' THEN "
      + " (SELECT status from operation_master where op_id = sm.res_sch_name) "
      + " when sm.res_sch_type = 'TST' AND res_sch_name != '*' THEN "
      + " (SELECT status from diagnostics where test_id = sm.res_sch_name) "
      + " else "
      + " (case when sm.res_sch_name != '*' then grm.status else 'A' end) END AS resource_status,"
      + " case when sm.res_sch_type NOT IN('SER','THID','SRID','EQID','SUR','TST','DOC') "
      + " THEN grt.status ELSE 'A' end AS generic_resource_type_status"
      + " FROM scheduler_master sm left join doctors d on (d.doctor_id = sm.res_sch_name)"
      + " left join theatre_master tm on(tm.theatre_id = sm.res_sch_name) "
      + " left join department dept on(dept.dept_id = sm.dept) "
      + " left join services_departments sd ON"
      + " (sd.serv_dept_id::text = sm.dept AND sm.res_sch_type = 'SER')"
      + " left join diagnostics_departments dd ON(dd.ddept_id = sm.dept)"
      + " left join service_resource_master srm ON(srm.serv_res_id::text = sm.res_sch_name)"
      + " left join test_equipment_master tem on(tem.eq_id::text = sm.res_sch_name)"
      + " left join operation_master ope ON(ope.op_id=sm.res_sch_name) "
      + " left join diagnostics dia ON(dia.test_id=sm.res_sch_name) "
      + " left join generic_resource_type grt ON(sm.res_sch_type = grt.scheduler_resource_type)"
      + " left join generic_resource_master grm ON(grm.generic_resource_id::text = sm.res_sch_name)"
      + " left join services serv ON(serv.service_id=sm.res_sch_name)) AS foo";

  /** The Constant GET_CATEGORY_DETAILS. */
  public static final String GET_CATEGORY_DETAILS =
      " SELECT res_sch_id,res_sch_type,"
          + "res_sch_category,dept,res_sch_name,description,status,"
          + " default_duration,"
          + " height_in_px FROM scheduler_master WHERE res_sch_id=? ";

  /**
   * Gets the category details.
   *
   * @param resSchId the res sch id
   * @return the category details
   */
  public BasicDynaBean getCategoryDetails(Integer resSchId) {
    BasicDynaBean bean =
        DatabaseHelper.queryToDynaBean(GET_CATEGORY_DETAILS, new Object[] {resSchId});
    if (bean != null) {
      return bean;
    } else {
      return null;
    }
  }

  /** The Constant GET_CATEGORY_DESCRIPTION. */
  public static final String GET_CATEGORY_DESCRIPTION =
      "SELECT res_sch_type,resource_description "
          + " from scheduler_resource_types srt"
          + " JOIN scheduler_master sm ON(sm.res_sch_type = srt.resource_type) "
          + " AND (srt.category = sm.res_sch_category) "
          + " AND res_sch_name = '*'"
          + " WHERE srt.resource_group is null #andprimaryCategory# "
          + " UNION "
          + " SELECT scheduler_resource_type AS res_sch_type,"
          + " resource_type_desc AS resource_description "
          + " FROM generic_resource_type grt "
          + " JOIN scheduler_resource_types srt ON"
          + " (srt.resource_type = grt.scheduler_resource_type)"
          + "  AND resource_group = 'GEN' #whereprimaryCategory#  ";

  /**
   * Gets the category description.
   *
   * @return the category description
   */
  public List<BasicDynaBean> getCategoryDescription(boolean primaryCategory) {
    String getCategoryDescription = GET_CATEGORY_DESCRIPTION;
    if (primaryCategory) {
      getCategoryDescription = getCategoryDescription.replace("#andprimaryCategory#",
          " and srt.primary_resource = 't' ");
      getCategoryDescription = getCategoryDescription.replace("#whereprimaryCategory#",
          " where srt.primary_resource = 't' ");
    } else {
      getCategoryDescription = getCategoryDescription.replace("#andprimaryCategory#", " ");
      getCategoryDescription = getCategoryDescription.replace("#whereprimaryCategory#", " ");
    }
    return DatabaseHelper.queryToDynaList(getCategoryDescription);
  }

  /** The Constant GET_ALL_ResourceS_ADD. */
  public static final String GET_ALL_ResourceS_ADD =
      " select srt.resource_type,'' as resource_id,"
          + " srt.primary_resource"
          + " from  scheduler_resource_types srt "
          + " WHERE srt.category = ? AND srt.primary_resource='false'";

  /** The Constant GET_ALL_ResourceS_EDIT. */
  public static final String GET_ALL_ResourceS_EDIT =
      " select sim.resource_type,sim.resource_id,"
          + " srt.primary_resource from "
          + " scheduler_item_master sim "
          + " left join  scheduler_master  sm on sm.res_sch_id = sim.res_sch_id "
          + " left join scheduler_resource_types srt on (srt.category = sm.res_sch_category and "
          + " sim.resource_type = srt.resource_type)"
          + " WHERE sim.res_sch_id=? AND srt.primary_resource='false'";

  /**
   * Gets the resource details.
   *
   * @param category the category
   * @param resSchId the res sch id
   * @return the resource details
   */
  public List getResourceDetails(String category, int resSchId) {
    List<BasicDynaBean> categoryNames = null;
    if (resSchId == 0) {
      categoryNames =
          DatabaseHelper.queryToDynaList(GET_ALL_ResourceS_ADD, new Object[] {category});
    } else {
      categoryNames =
          DatabaseHelper.queryToDynaList(GET_ALL_ResourceS_EDIT, new Object[] {resSchId});
    }
    return categoryNames;
  }

  /** The get resource deatils. */
  public static final String GET_RESOURCE_DEATILS =
      "SELECT * FROM scheduler_master "
          + " where res_sch_type = ? and res_sch_name = ? AND status = 'A'";

  /**
   * Gets the resource details.
   *
   * @param resSchType the res sch type
   * @param resSchName the res sch name
   * @return the resource details
   */
  public BasicDynaBean getResourceDetails(String resSchType, String resSchName) {
    return DatabaseHelper.queryToDynaBean(
        GET_RESOURCE_DEATILS, new Object[] {resSchType, resSchName});
  }

  /** The Constant RESOURCE_TYPES. */
  private static final String RESOURCE_TYPES =
      "select * from scheduler_resource_types"
          + " where resource_type NOT IN('TST','SUR','SER') @ ";

  /**
   * Gets the resource types.
   *
   * @param needPrimaryResource the need primary resource
   * @return the resource types
   */
  public List getResourceTypes(boolean needPrimaryResource) {
    String query = RESOURCE_TYPES;
    List resourceTypelist = null;
    if (!needPrimaryResource) {
      query = query.replace("@", "AND primary_resource =false");
    } else {
      query = query.replace("@", "");
    }
    resourceTypelist = DatabaseHelper.queryToDynaList(query);
    return resourceTypelist;
  }

  /** The Constant THEATRES. */
  private static final String THEATRES =
      "SELECT theatre_id,theatre_name,overbook_limit,center_id "
          + " FROM theatre_master WHERE status='A' and schedule = true";

  /** The Constant GET_THEATRES_CENTERWISE. */
  private static final String GET_THEATRES_CENTERWISE =
      "SELECT theatre_id,theatre_name, "
          + " overbook_limit,center_id "
          + " FROM theatre_master WHERE status='A' and schedule = true "
          + " and (center_id = ? OR center_id = 0)";

  /** The Constant EQUIPMENTS. */
  private static final String EQUIPMENTS =
      "SELECT eq_id::text,equipment_name,'' AS dept_id,"
          + " overbook_limit,center_id from test_equipment_master "
          + " where status='A' and schedule = true";

  /** The Constant GET_EQUIPMENTS_CENTERWISE. */
  private static final String GET_EQUIPMENTS_CENTERWISE =
      "SELECT eq_id::text,"
          + " equipment_name,'' AS dept_id, "
          + " overbook_limit,center_id from test_equipment_master "
          + " where status='A' and schedule = true and (center_id = ? OR center_id = 0)";

  /** The Constant BEDS. */
  private static final String BEDS =
      "SELECT bed_type,bed_name,bed_id " + " FROM bed_names WHERE status='A'";

  /** The Constant SERVICES. */
  private static final String SERVICES =
      "SELECT service_id, service_name, "
          + " serv_dept_id AS dept_name FROM services ser"
          + " LEFT JOIN services_departments sd USING(serv_dept_id) "
          + " WHERE ser.status='A' ORDER BY service_name";

  /** The Constant TESTS. */
  private static final String TESTS =
      "SELECT test_id,test_name,"
          + " d.ddept_id,category FROM diagnostics d "
          + " LEFT JOIN diagnostics_departments dd ON(d.ddept_id = dd.ddept_id "
          + " and category in ('DEP_LAB','DEP_RAD'))"
          + " WHERE d.status='A' and dd.status='A' ORDER BY test_name";

  /** The Constant OPERATIONS. */
  private static final String OPERATIONS =
      "SELECT op_id,operation_name," + " dept_id FROM operation_master WHERE status='A'";

  /** The Constant SCHEDULERDOCTORS. */
  private static final String SCHEDULERDOCTORS =
      "SELECT d.doctor_id, d.doctor_name,"
          + " d.dept_id, d.ot_doctor_flag,d.available_for_online_consults,"
          + " (case when schedule=true then 'T' else 'F' end ) as schedule,"
          + " overbook_limit, dcm.center_id "
          + " FROM doctors d"
          + " JOIN doctor_center_master dcm ON (d.doctor_id = dcm.doctor_id)"
          + " WHERE d.status = 'A' and dcm.status='A' AND d.schedule = true order by doctor_name";

  /** The Constant GET_SCHEDULERDOCTORS_CENTERWISE. */
  private static final String GET_SCHEDULERDOCTORS_CENTERWISE =
      "SELECT d.doctor_id, d.doctor_name,d.available_for_online_consults,"
          + " d.dept_id, d.ot_doctor_flag,"
          + " (case when schedule=true then 'T' else 'F' end ) as schedule,"
          + " overbook_limit, dcm.center_id "
          + " FROM doctors d"
          + " JOIN doctor_center_master dcm ON (d.doctor_id = dcm.doctor_id)"
          + " WHERE d.status = 'A' and dcm.status='A' AND d.schedule = true AND "
          + " (dcm.center_id = ? OR dcm.center_id = 0) order by doctor_name";

  /** The Constant LABTECHNICIANS. */
  private static final String LABTECHNICIANS =
      "SELECT d.doctor_id, d.doctor_name, d.dept_id, "
          + " d.ot_doctor_flag,dcm.center_id FROM doctors d"
          + " JOIN doctor_center_master dcm ON(d.doctor_id = dcm.doctor_id)"
          + " JOIN hospital_center_master using(center_id) "
          + " WHERE d.status = 'A' and d.dept_id in ('DEP_LAB','DEP_RAD') AND "
          + " d.schedule = true order by doctor_name";

  /** The Constant GET_LABTECHNICIANS_CENTERWISE. */
  private static final String GET_LABTECHNICIANS_CENTERWISE =
      "SELECT d.doctor_id, d.doctor_name, "
          + " d.dept_id, d.ot_doctor_flag,dcm.center_id FROM doctors d"
          + " JOIN doctor_center_master dcm ON(d.doctor_id = dcm.doctor_id)"
          + " JOIN hospital_center_master using(center_id) "
          + " WHERE d.status = 'A' and d.dept_id in ('DEP_LAB','DEP_RAD') AND "
          + " d.schedule = true AND (dcm.center_id = ? OR dcm.center_id = 0) order by doctor_name";

  /** The Constant GENERIC_RESOURCES. */
  private static final String GENERIC_RESOURCES =
      "SELECT *,'' as dept_id,"
          + " (case when schedule=true then 'T' else 'F' end ) as scheduleable,center_id"
          + " FROM generic_resource_master grm"
          + " JOIN generic_resource_type grt ON"
          + " (grt.generic_resource_type_id = grm.generic_resource_type_id) "
          + " WHERE grm.status = 'A' AND grt.status ='A' order by generic_resource_name";

  /**
   * Gets the resource master list.
   *
   * @param category the category
   * @param loggedCenterId the logged center id
   * @return the resource master list
   */
  public List getResourceMasterList(String category, int loggedCenterId) {
    List list = null;
    if (loggedCenterId == 0) {
      if (category.equals("DOC")) {
        list = DatabaseHelper.queryToDynaList(SCHEDULERDOCTORS);
      } else if (category.equals("THID")) {
        list = DatabaseHelper.queryToDynaList(THEATRES);
      } else if (category.equals("EQID")) {
        list = DatabaseHelper.queryToDynaList(EQUIPMENTS);
      } else if (category.equals("BED")) {
        list = DatabaseHelper.queryToDynaList(BEDS);
      } else if (category.equals("OPID")) {
        list = DatabaseHelper.queryToDynaList(OPERATIONS);
      } else if (category.equals("DGC")) {
        list = DatabaseHelper.queryToDynaList(TESTS);
      } else if (category.equals("SERV")) {
        list = DatabaseHelper.queryToDynaList(SERVICES);
      } else if (category.equals("LABTECH")) {
        list = DatabaseHelper.queryToDynaList(LABTECHNICIANS);
      } else if (category.equals("GEN")) {
        list = DatabaseHelper.queryToDynaList(GENERIC_RESOURCES);
      }
    } else {
      if (category.equals("DOC")) {
        list =
            DatabaseHelper.queryToDynaList(
                GET_SCHEDULERDOCTORS_CENTERWISE, new Object[] {loggedCenterId});
      } else if (category.equals("THID")) {
        list =
            DatabaseHelper.queryToDynaList(GET_THEATRES_CENTERWISE, new Object[] {loggedCenterId});
      } else if (category.equals("EQID")) {
        list =
            DatabaseHelper.queryToDynaList(
                GET_EQUIPMENTS_CENTERWISE, new Object[] {loggedCenterId});
      } else if (category.equals("BED")) {
        list = DatabaseHelper.queryToDynaList(BEDS);
      } else if (category.equals("OPID")) {
        list = DatabaseHelper.queryToDynaList(OPERATIONS);
      } else if (category.equals("DGC")) {
        list = DatabaseHelper.queryToDynaList(TESTS);
      } else if (category.equals("SERV")) {
        list = DatabaseHelper.queryToDynaList(SERVICES);
      } else if (category.equals("LABTECH")) {
        list =
            DatabaseHelper.queryToDynaList(
                GET_LABTECHNICIANS_CENTERWISE, new Object[] {loggedCenterId});
      } else if (category.equals("GEN")) {
        list = DatabaseHelper.queryToDynaList(GENERIC_RESOURCES);
      }
    }

    return list;
  }

  /** The Constant GET_SERVICE_RESOURCE_SCHEDULES. */
  private static final String GET_SERVICE_RESOURCE_SCHEDULES =
      "select srm.serv_res_id::text,"
          + " srm.serv_resource_name,'' AS dept_id,overbook_limit,center_id"
          + " FROM  service_resource_master srm "
          + " WHERE srm.schedule = true AND srm.status = 'A' # order by srm.serv_resource_name";

  /**
   * Gets the serv resource schedules.
   *
   * @param category the category
   * @param centerId the center id
   * @return the serv resource schedules
   */
  public List<BasicDynaBean> getServResourceSchedules(String category, int centerId) {
    String query = GET_SERVICE_RESOURCE_SCHEDULES;
    String centerClause = "And center_id = ?";
    List schedulesList = new ArrayList();

    if ((Integer) (prefService.getPreferences().get("max_centers_inc_default")) == 1) {
      query = query.replace("#", "");
      schedulesList = DatabaseHelper.queryToDynaList(query);
    } else {
      if (centerId != 0) {
        query = query.replace("#", centerClause);
        schedulesList = DatabaseHelper.queryToDynaList(query, new Integer[] {centerId});
      } else {
        query = query.replace("#", "");
        schedulesList = DatabaseHelper.queryToDynaList(query);
      }
    }
    return schedulesList;
  }

  /** The Constant GET_ALL_RESOURCES. */
  public static final String GET_ALL_RESOURCES =
      " SELECT doctor_name as resource_name,"
          + " d.doctor_id as resource_id,'DOC' as resource_type,'' "
          + " as scheduler_resource_type,dept_id "
          + " From doctors d "
          + " LEFT JOIN doctor_center_master dcm on (d.doctor_id = dcm.doctor_id)"
          + " WHERE schedule = true AND d.status='A' and dcm.status='A' "
          + "UNION"
          + " SELECT theatre_name as resource_name,theatre_id as resource_id,"
          + " 'THID' as resource_type,'' as scheduler_resource_type,'' as dept_id "
          + " FROM theatre_master"
          + " WHERE schedule = true AND status='A' "
          + "UNION "
          + " SELECT serv_resource_name as resource_name,serv_res_id::text as resource_id,"
          + " 'SRID' as resource_type,'' as scheduler_resource_type,'' as dept_id "
          + " FROM service_resource_master "
          + " WHERE schedule = true AND status='A' "
          + "UNION "
          + " SELECT equipment_name as resource_name,eq_id::text as resource_id,"
          + " 'EQID' as resource_type,'' as scheduler_resource_type,'' as dept_id "
          + " FROM test_equipment_master "
          + " WHERE schedule=true AND status='A' "
          + "UNION "
          + " SELECT test_name  as resource_name,test_id as resource_id,"
          + " 'TST' as resource_type,'' as scheduler_resource_type,ddept_id as dept_id "
          + " FROM diagnostics "
          + " WHERE status = 'A'"
          + "UNION "
          + " SELECT operation_name as resource_name,op_id as resource_id,"
          + " 'SUR' as resource_type,'' as scheduler_resource_type,dept_id "
          + " FROM operation_master "
          + " WHERE status = 'A' "
          + "UNION "
          + " SELECT service_name||'('||department||')'  as resource_name,"
          + " service_id as resource_id,"
          + " 'SER' as resource_type,'' as scheduler_resource_type,s.serv_dept_id::text as dept_id "
          + " FROM services s"
          + " JOIN services_departments sd ON(sd.serv_dept_id=s.serv_dept_id) "
          + " WHERE s.status = 'A' "
          + "UNION "
          + " SELECT generic_resource_name as resource_name,"
          + " generic_resource_id::text as resource_id,"
          + " 'GEN' as resource_type,scheduler_resource_type,'' as dept_id "
          + " FROM generic_resource_master grm"
          + " JOIN generic_resource_type grt ON"
          + " (grt.generic_resource_type_id = grm.generic_resource_type_id)"
          + " WHERE grm.status='A' AND grt.status = 'A' AND schedule=true ";

  /** The Constant GET_ALL_RESOURCES_CenterWise. */
  public static final String GET_ALL_RESOURCES_CenterWise =
      " SELECT doctor_name as resource_name,"
          + " d.doctor_id as resource_id,'DOC' as resource_type,"
          + " '' as scheduler_resource_type,dept_id"
          + " From doctors d "
          + " LEFT JOIN doctor_center_master dcm on (d.doctor_id = dcm.doctor_id)"
          + " WHERE schedule = true AND d.status='A' AND (dcm.center_id = 0 OR dcm.center_id = ? )"
          + " and dcm.status='A' "
          + " UNION"
          + "   SELECT theatre_name as resource_name,theatre_id as resource_id,"
          + "   'THID' as resource_type,'' as scheduler_resource_type,'' as dept_id "
          + "   FROM theatre_master"
          + " WHERE schedule = true AND status='A' "
          + " UNION "
          + "   SELECT serv_resource_name as resource_name,serv_res_id::text as resource_id,"
          + "   'SRID' as resource_type,'' as scheduler_resource_type,'' as dept_id "
          + "   FROM service_resource_master "
          + " WHERE schedule = true AND status='A' "
          + " UNION "
          + "   SELECT equipment_name as resource_name,eq_id::text as resource_id,"
          + "   'EQID' as resource_type,'' as scheduler_resource_type,'' as dept_id "
          + "   FROM test_equipment_master "
          + " WHERE schedule=true AND status='A' "
          + " UNION "
          + "   SELECT test_name  as resource_name,test_id as resource_id,"
          + "   'TST' as resource_type,'' as scheduler_resource_type,ddept_id as dept_id "
          + "   FROM diagnostics "
          + " WHERE status = 'A'"
          + "UNION "
          + "   SELECT operation_name  as resource_name,op_id as resource_id,"
          + "   'SUR' as resource_type,'' as scheduler_resource_type,dept_id "
          + "   FROM operation_master "
          + " WHERE status = 'A' "
          + " UNION "
          + "   SELECT service_name||'('||department||')'  as resource_name,"
          + "   service_id as resource_id,"
          + " 'SER' as resource_type,'' as scheduler_resource_type,"
          + "  s.serv_dept_id::text as dept_id "
          + " FROM services s"
          + "   JOIN services_departments sd ON(sd.serv_dept_id=s.serv_dept_id) "
          + "   WHERE s.status = 'A' "
          + " UNION "
          + " SELECT generic_resource_name as resource_name,"
          + " generic_resource_id::text as resource_id,"
          + " 'GEN' as resource_type,scheduler_resource_type,'' as dept_id "
          + " FROM generic_resource_master grm"
          + " JOIN generic_resource_type grt ON"
          + " (grt.generic_resource_type_id = grm.generic_resource_type_id)"
          + " WHERE grm.status='A' AND grt.status = 'A' AND schedule=true ";

  /**
   * Gets the all resources.
   *
   * @return the all resources
   */  
  public List<BasicDynaBean> getAllResources(int centerId) {
    List resourcesList = new ArrayList();
    if (centerId == 0) {
      resourcesList = DatabaseHelper.queryToDynaList(GET_ALL_RESOURCES);
    } else {
      resourcesList =
          DatabaseHelper.queryToDynaList(GET_ALL_RESOURCES_CenterWise, new Object[] {centerId});
    }
    return resourcesList;
  }
  
  /**
   * Gets the all resources.
   *
   * @return the all resources
   */
  public List<BasicDynaBean> getAllResources() {
    return getAllResources(RequestContext.getCenterId());
  }

  /** The Constant GET_RESOURCE_AVAILABILITIES. */
  public static final String GET_RESOURCE_AVAILABILITIES =
      "SELECT *,"
          + " day_of_week::text AS day_of_week_text,hc.center_name "
          + " FROM sch_default_res_availability_details sc "
          + " LEFT JOIN hospital_center_master hc ON (sc.center_id=hc.center_id) "
          + " where res_sch_id = ? AND from_time != to_time order by from_time ";

  /**
   * Gets the resource availabilities.
   *
   * @param resourceId the resource id
   * @return the resource availabilities
   */
  public List getResourceAvailabilities(Object resourceId) {
    List resourcesAvailList = new ArrayList();
    return resourcesAvailList =
        DatabaseHelper.queryToDynaList(GET_RESOURCE_AVAILABILITIES, resourceId);
  }

  /** INSERT,UPDATE and DELETE of Resources. */
  public static final String INSERT_RESOURCE =
      "INSERT INTO scheduler_item_master"
          + " (res_sch_id,resource_type,resource_id, center_id) values (?,?,?,?)";

  /*
   * Insert a list of Resources
   */

  /**
   * Insert resources.
   *
   * @param list the list
   * @return true, if successful
   */
  public boolean insertResources(List list) {
    boolean success = true;
    String query = INSERT_RESOURCE;
    List<Object[]> insertList = new ArrayList<Object[]>();
    Iterator iterator = list.iterator();
    while (iterator.hasNext()) {
      List<Object> queryParams = new ArrayList<Object>();
      ResourceDTO rdto = (ResourceDTO) iterator.next();
      queryParams.add(rdto.getRes_sch_id());
      queryParams.add(rdto.getResourceType());
      queryParams.add(rdto.getResourceId());
      queryParams.add(rdto.getCenterId());
      insertList.add(queryParams.toArray());
    }
    int[] results = DatabaseHelper.batchInsert(query, insertList);

    for (int p = 0; p < results.length; p++) {
      if (results[p] <= 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  /** The Constant UPDATE_RESOURCE. */
  public static final String UPDATE_RESOURCE =
      "UPDATE scheduler_item_master SET "
          + "resource_id=? WHERE res_sch_id=? and resource_type=? and resource_id=?";

  /**
   * Update resources.
   *
   * @param list the list
   * @return true, if successful
   */
  public boolean updateResources(List list) {
    boolean success = true;
    List<Object[]> updateList = new ArrayList<Object[]>();
    Iterator iterator = list.iterator();
    while (iterator.hasNext()) {
      List<Object> queryParams = new ArrayList<Object>();
      ResourceDTO rdto = (ResourceDTO) iterator.next();
      queryParams.add(rdto.getResourceId());
      queryParams.add(rdto.getRes_sch_id());
      queryParams.add(rdto.getResourceType());
      queryParams.add(rdto.getItem_id());
      updateList.add(queryParams.toArray());
    }

    int[] results = DatabaseHelper.batchUpdate(UPDATE_RESOURCE, updateList);

    for (int p = 0; p < results.length; p++) {
      if (results[p] <= 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  /** The Constant DELETE_RESOURCE. */
  private static final String DELETE_RESOURCE =
      "DELETE FROM scheduler_item_master "
          + " WHERE res_sch_id=? and resource_id=? and resource_type=?";

  /**
   * Delete resources.
   *
   * @param list the list
   * @return true, if successful
   */
  public boolean deleteResources(List list) {
    boolean success = true;
    List<Object[]> deleteList = new ArrayList<Object[]>();

    Iterator iterator = list.iterator();
    while (iterator.hasNext()) {
      List<Object> queryParams = new ArrayList<Object>();
      ResourceDTO rdto = (ResourceDTO) iterator.next();
      queryParams.add(rdto.getRes_sch_id());
      queryParams.add(rdto.getResourceId());
      queryParams.add(rdto.getResourceType());
      deleteList.add(queryParams.toArray());
    }
    int[] results = DatabaseHelper.batchDelete(DELETE_RESOURCE, deleteList);
    for (int p = 0; p < results.length; p++) {
      if (results[p] <= 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  /** The get category details by resourcetype. */
  public static final String GET_CATEGORY_DETAILS_BY_RESOURCETYPE = "SELECT * FROM "
      + " scheduler_master WHERE res_sch_type = ? AND res_sch_name = ? AND status = 'A'";

  /** The get default attributes for resource. */
  public static final String GET_DEFAULT_ATTRIBUTES_FOR_RESOURCE = "SELECT * FROM scheduler_master "
      + " WHERE res_sch_type = ? AND res_sch_name = ? AND res_sch_id != ? AND status = 'A'";

  /**
   * Gets the category details by resource.
   *
   * @param resSchName the res sch name
   * @param resourceType the resource type
   * @param resSchId the res sch id
   * @return the category details by resource
   */
  public BasicDynaBean getCategoryDetailsByResource(
      String resSchName, String resourceType, Integer resSchId) {
    if (resSchId != null) {
      return DatabaseHelper.queryToDynaBean(
          GET_DEFAULT_ATTRIBUTES_FOR_RESOURCE, resSchName, resourceType, resSchId);
    } else {
      return DatabaseHelper.queryToDynaBean(
          GET_CATEGORY_DETAILS_BY_RESOURCETYPE, resSchName, resourceType);
    }
  }

  /** The Constant GET_CATEGORY_TYPE. */
  public static final String GET_CATEGORY_TYPE =
      "select res_sch_category from "
          + " scheduler_master where res_sch_name = '*' AND res_sch_type = ?";

  /**
   * Gets the category type.
   *
   * @param resourceType the resource type
   * @return the category type
   */
  public String getCategoryType(String resourceType) {
    return DatabaseHelper.getString(GET_CATEGORY_TYPE, resourceType);
  }

  /** The Constant GET_TOTAL_RESOURCES_IN_DB. */
  public static final String GET_TOTAL_RESOURCES_IN_DB =
      "select count(*) AS resource_count from " + " scheduler_master WHERE res_sch_name ='*'";

  /**
   * Gets the total resources.
   *
   * @return the total resources
   */
  public static Integer getTotalResources() {
    return DatabaseHelper.getInteger(GET_TOTAL_RESOURCES_IN_DB);
  }

  /** The Constant GET_DEFAULT_CATEGORY_DURATION. */
  public static final String GET_DEFAULT_CATEGORY_DURATION =
      "select default_duration from "
          + " scheduler_master where res_sch_name= ? and res_sch_category=? LIMIT 1;";

  /**
   * Gets the default category duration.
   *
   * @param category the category
   * @return the default category duration
   */
  public Integer getDefaultCategoryDuration(String category) {
    return DatabaseHelper.getInteger(GET_DEFAULT_CATEGORY_DURATION, new Object[] {'*', category});
  }

  /**
   * Gets the default category height.
   *
   * @param category the category
   * @return the default category height
   */
  public Integer getDefaultCategoryHeight(String category) {
    return DatabaseHelper.getInteger(GET_DEFAULT_CATEGORY_DURATION, new Object[] {'*', category});
  }

  /** The Constant GET_SCH_DURATION. */
  public static final String GET_SCH_DURATION =
      "select default_duration from "
          + " scheduler_master where res_sch_name= ? and res_sch_type= ? and status = ? ";

  /**
   * Gets the scheduler duration.
   *
   * @param resourceType the resource type
   * @return the scheduler duration
   */
  public Integer getSchedulerDuration(String resourceType) {
    return DatabaseHelper.getInteger(GET_SCH_DURATION, new Object[] {'*', resourceType, 'A'});
  }

  /* (non-Javadoc)
   * @see com.insta.hms.common.GenericRepository#getNextSequence()
   */
  @Override
  public Integer getNextSequence() {
    // TODO Auto-generated method stub
    return DatabaseHelper.getNextSequence("res_sch_id");
  }

  /** The doctor query. */
  public static final String DOCTOR_QUERY = " JOIN doctors res ON(res.doctor_id = sai.resource_id)";

  /** The equipment query. */
  public static final String EQUIPMENT_QUERY =
      " JOIN test_equipment_master res ON" + " (res.eq_id::text = sai.resource_id)";

  /** The service resources query. */
  public static final String SERVICE_RESOURCES_QUERY =
      " JOIN service_resource_master res ON" + " (res.serv_res_id::text = sai.resource_id)";

  /** The theatre query. */
  public static final String THEATRE_QUERY =
      " JOIN theatre_master res ON" + "(res.theatre_id = sai.resource_id)";

  /**
   * Gets the resource appointment details.
   *
   * @param resourceType the resource type
   * @param resourceId the resource id
   * @return the resource appointment details
   */
  public List<BasicDynaBean> getResourceAppointmentDetails(String resourceType, String resourceId) {
    Date currentDate = DateUtil.getCurrentDate();
    String query =
        " SELECT *,appointment_time as appointment_start_time, "
            + " appointment_time+(duration||' mins')::interval AS end_appointment_time"
            + " FROM scheduler_appointments sa"
            + " JOIN scheduler_appointment_items sai ON(sa.appointment_id = sai.appointment_id) #"
            + " WHERE appointment_status IN('Booked','Confirmed') "
            + " AND appointment_time::date >= ? @";
    if (resourceType.equals("DOC")) {
      query = query.replace("#", DOCTOR_QUERY);
    } else if (resourceType.equals("THID")) {
      query = query.replace("#", THEATRE_QUERY);
    } else if (resourceType.equals("EQID")) {
      query = query.replace("#", EQUIPMENT_QUERY);
    } else if (resourceType.equals("SRID")) {
      query = query.replace("#", SERVICE_RESOURCES_QUERY);
    }

    if (!resourceId.equals("*")) {
      query = query.replace("@", " AND resource_id::text = ?");
    } else {
      query = query.replace("@", " ");
    }

    return DatabaseHelper.queryToDynaList(
        query,
        resourceId.equals("*")
            ? new Object[] {currentDate}
            : new Object[] {currentDate, resourceId});
  }

  /** The get non avail resource deatils. */
  public static final String GET_NON_AVAIL_RESOURCE_DEATILS =
      "SELECT * FROM "
          + " sch_default_res_availability_details "
          + " WHERE res_sch_id = ? AND availability_status = ?";

  /**
   * Gets the non avail resource details.
   *
   * @param resSchId the res sch id
   * @return the non avail resource details
   */
  public List<BasicDynaBean> getNonAvailResourceDetails(int resSchId) {
    List<BasicDynaBean> resourceUnavailabilityDetails = null;
    resourceUnavailabilityDetails =
        DatabaseHelper.queryToDynaList(
            GET_NON_AVAIL_RESOURCE_DEATILS, new Object[] {resSchId, "N"});
    return resourceUnavailabilityDetails;
  }

  /** The Constant GET_DEFAULT_RESOURCE_AVAILABILITIES. */
  private static final String GET_DEFAULT_RESOURCE_AVAILABILITIES =
      "SELECT * FROM"
          + " scheduler_master sm "
          + " JOIN sch_default_res_availability_details sdra ON (sdra.res_sch_id = sm.res_sch_id) "
          + " WHERE #DAY_FILTER# sm.res_sch_name = ? AND res_sch_type = ? "
          + " AND from_time != to_time  AND sm.status = 'A'  #  @  ORDER BY sdra.from_time";

  /** The Constant CENTER_ID_WHERE_CLAUSE. */
  private static final String CENTER_ID_WHERE_CLAUSE = " AND center_id = ?  ";

  /** The Constant AVAILABILITY_STATUS_WHERE_CLAUSE. */
  private static final String AVAILABILITY_STATUS_WHERE_CLAUSE = " AND availability_status = ? ";


  /**
   * Gets the Weekly Availability of a resource for a given day.
   *
   * @param resourceId - The unique identifier for resource
   * @param dayOfWeek - The day of week. 0 represents SUNDAY, 1 represents MONDAY, 2 TUESDAY..., 6
   *     represents SATURDAY
   * @param resourceType - The type of resource. Eg : Doctors, Operation theaters
   * @param availabilityStatus - Whether the resource is available. 'A' for Available , 'N' for Not
   *     Available
   * @param centerId - The center for which weekly availability is to be fetched
   * @param centersIncDefault - Total centers including default center
   * @return list of beans
   */
  public List<BasicDynaBean> getDefaultResourceAvailabilities(
      String resourceId,
      Integer dayOfWeek,
      String resourceType,
      String availabilityStatus,
      Integer centerId,
      int centersIncDefault) {
    List<Object> queryParams = new ArrayList<Object>();

    String query = GET_DEFAULT_RESOURCE_AVAILABILITIES;

    if (dayOfWeek != null) {
      query = query.replace("#DAY_FILTER#", "day_of_week = ? AND ");
      queryParams.add(dayOfWeek);
    } else {
      query = query.replace("#DAY_FILTER#", "");
    }
    if (availabilityStatus != null) {
      query = query.replace("#", AVAILABILITY_STATUS_WHERE_CLAUSE);
    } else {
      query = query.replace("#", "");
    }
    if (centerId != null && centersIncDefault > 1) {
      query = query.replace("@", CENTER_ID_WHERE_CLAUSE);
    } else {
      query = query.replace("@", "");
    }
    queryParams.add(resourceId);
    queryParams.add(resourceType);
    if (availabilityStatus != null) {
      queryParams.add(availabilityStatus);
    }
    if (centerId != null && centersIncDefault > 1) {
      queryParams.add(centerId);
    }
    return DatabaseHelper.queryToDynaList(query, queryParams.toArray());
  }

  /** The Constant GET_CATEGORY_DETAILS_BY_RESOURCE_ID. */
  public static final String GET_CATEGORY_DETAILS_BY_RESOURCE_ID =
      "SELECT * FROM scheduler_master "
          + " WHERE res_sch_type = ? AND res_sch_name = ? AND res_sch_id != ? AND status = 'A'";

  /**
   * Gets the category details by resource id.
   *
   * @param resSchName the res sch name
   * @param resourceType the resource type
   * @param resourceId the resource id
   * @return the category details by resource id
   */
  public BasicDynaBean getCategoryDetailsByResourceId(
      String resSchName, String resourceType, int resourceId) {
    return DatabaseHelper.queryToDynaBean(
        GET_CATEGORY_DETAILS_BY_RESOURCE_ID, new Object[] {resourceType, resSchName, resourceId});
  }

  /** The Constant GET_DEFAULT_DURATION. */
  public static final String GET_DEFAULT_DURATION = "select default_duration"
      + " from scheduler_master where res_sch_name=? AND res_sch_category=? AND status='A' ";

  /**
   * Gets the default duration.
   *
   * @param resourceId the resource id
   * @param category the category
   * @return the default duration
   */
  public Integer getDefaultDuration(String resourceId, String category) {
    Integer duration = DatabaseHelper.getInteger(GET_DEFAULT_DURATION, resourceId, category);
    if (duration == null) {
      duration = DatabaseHelper.getInteger(GET_DEFAULT_DURATION, "*", category);
    }
    return duration;
  }
  
  /** The Constant GET_DEFAULT_AVAILABILITY_FOR_RESOURCE. */
  private static final String GET_DEFAULT_AVAILABILITY_FOR_RESOURCE = "SELECT sdra.res_sch_id "
      + " FROM scheduler_master sm "
      + " JOIN sch_default_res_availability_details sdra ON (sdra.res_sch_id = sm.res_sch_id) "
      + " WHERE sm.res_sch_name = ? AND sdra.day_of_week = ? "
      + " AND sdra.center_id = ? limit 1 ";
  
  /**
   * Gets the default avaibility for resource.
   *
   * @param filterValues the filter values
   * @return the default avaibility for resource
   */
  public BasicDynaBean getDefaultAvaibilityForResource(List filterValues) {
    return DatabaseHelper.queryToDynaBean(GET_DEFAULT_AVAILABILITY_FOR_RESOURCE,
        filterValues.toArray());
  }
  
  /** The Constant DEFAULT_AVAILABILITY_FOR_RESOURCE_EXISTS. */
  private static final String DEFAULT_AVAILABILITY_FOR_RESOURCE_EXISTS = "SELECT exists( select 1 "
      + " FROM scheduler_master sm "
      + " JOIN sch_default_res_availability_details sdra ON (sdra.res_sch_id = sm.res_sch_id) "
      + " WHERE sm.res_sch_name = ? and sdra.visit_mode in (?,?) )";
 
  /**
   * Resource availibilites exists.
   *
   * @param resSchId the res sch id
   * @param visitMode the visit mode
   * @return true, if successful
   */
  public boolean resourceAvailibilitesExists(String resSchId, String visitMode) {
    String query = DEFAULT_AVAILABILITY_FOR_RESOURCE_EXISTS;
    String bothVisitMode = "B";
    Object[] params = new Object[] {resSchId, visitMode, bothVisitMode};
    boolean flag = DatabaseHelper.getBoolean(query, params);
    if (!flag) {
      params = new Object[] {"*", visitMode, bothVisitMode};  
      return DatabaseHelper.getBoolean(query, params);
    } else {
      return true;
    }
  } 
}
