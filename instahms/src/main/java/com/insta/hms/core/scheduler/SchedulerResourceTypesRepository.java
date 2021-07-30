package com.insta.hms.core.scheduler;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class SchedulerResourceTypesRepository.
 */
@Repository
public class SchedulerResourceTypesRepository extends GenericRepository {

  /** The Constant GET_ALL_RESOURCES_ADD. */
  private static final String GET_ALL_RESOURCES_ADD = " select srt.resource_type,"
      + "'' as resource_id,srt.primary_resource"
      + " from  scheduler_resource_types srt   WHERE srt.category = ? "
      + "AND srt.primary_resource='false'";

  /** The Constant GET_PRIMARY_RESOURCE_FOR_CATEGORY. */
  private static final String GET_PRIMARY_RESOURCE_FOR_CATEGORY = " select srt.resource_type,''"
      + " as resource_id,srt.primary_resource"
      + " from  scheduler_resource_types srt   WHERE srt.category = ? AND"
      + " srt.primary_resource='true'";

  /** The Constant RESOURCE_TYPES. */
  private static final String RESOURCE_TYPES = "select * from scheduler_resource_types"
      + " where resource_type NOT IN('TST','SUR','SER') @ ";

  /** The Constant GET_ALL_RESOURCES_EDIT. */
  private static final String GET_ALL_RESOURCES_EDIT = " select sim.resource_type,"
      + "sim.resource_id,srt.primary_resource  from "
      + " scheduler_item_master sim "
      + " left join  scheduler_master  sm on sm.res_sch_id = sim.res_sch_id "
      + " left join scheduler_resource_types srt on (srt.category = sm.res_sch_category "
      + "and sim.resource_type = srt.resource_type)"
      + " WHERE sim.res_sch_id=? AND srt.primary_resource='false'";
  
  /** The Constant GET_ADDITIONAL_RESOURCE_TYPES. */
  private static final String GET_ADDITIONAL_RESOURCE_TYPES = "SELECT resource_type,"
      + "resource_description FROM"
      + "  scheduler_resource_types  where category= :category and primary_resource =false "
      + "AND resource_type NOT IN('SUR','SER','THID','TST') AND resource_group is null "
      + "  UNION"
      + "  SELECT scheduler_resource_type AS resource_type,resource_type_desc AS "
      + "resource_description FROM generic_resource_type grt "
      + "  JOIN scheduler_resource_types srt ON(srt.resource_type = grt.scheduler_resource_type)"
      + " WHERE category = :category AND resource_group = 'GEN'";

  /**
   * Instantiates a new scheduler resource types repository.
   */
  public SchedulerResourceTypesRepository() {
    super("scheduler_resource_types");
  }

  /**
   * Gets the primary resource type.
   *
   * @param category the category
   * @return the primary resource type
   */
  public List<BasicDynaBean> getPrimaryResourceType(String category) {
    return DatabaseHelper.queryToDynaList(GET_PRIMARY_RESOURCE_FOR_CATEGORY,
        new Object[] { category });
  }

  /**
   * Gets the resource details.
   *
   * @param category the category
   * @param resourceScheduleId the resource schedule id
   * @return the resource details
   */
  public List<BasicDynaBean> getResourceDetails(String category, int resourceScheduleId) {
    List<BasicDynaBean> categoryNames = null;
    if (resourceScheduleId == 0) {
      categoryNames = DatabaseHelper.queryToDynaList(GET_ALL_RESOURCES_ADD,
          new Object[] { category });
    } else {
      categoryNames = DatabaseHelper.queryToDynaList(GET_ALL_RESOURCES_EDIT,
          new Object[] { resourceScheduleId });
    }
    return categoryNames;
  }

  /**
   * Gets the additional resource types.
   *
   * @param category the category
   * @return the additional resource types
   */
  public List<BasicDynaBean> getAdditionalResourceTypes(String category) {
    String query = GET_ADDITIONAL_RESOURCE_TYPES;
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("category", category);
    return DatabaseHelper.queryToDynaList(query, parameters);
  }

  /**
   * Gets the resource types.
   *
   * @param needPrimaryResource the need primary resource
   * @return the resource types
   */
  public List<BasicDynaBean> getResourceTypes(boolean needPrimaryResource) {
    String resourceTypes = RESOURCE_TYPES;
    if (!needPrimaryResource) {
      resourceTypes = resourceTypes.replace("@", "AND primary_resource =false");
    } else {
      resourceTypes = resourceTypes.replace("@", "");
    }
    return DatabaseHelper.queryToDynaList(resourceTypes);
  }

  /** The Constant GET_RESOURCE_LIST. */
  private static final String GET_RESOURCE_LIST = " SELECT sim.resource_type,"
      + "sim.resource_id,sim.center_id "
      + " FROM scheduler_master sm" + " JOIN scheduler_item_master sim using(res_sch_id)"
      + " WHERE sm.res_sch_name = ? AND sm.res_sch_type = ? AND status = 'A' #CENTER_FILTER#";

  /**
   * Gets the default resource list.
   *
   * @param category the category
   * @param resourceScheduleName the resource schedule name
   * @param centersIncDefault the centers inc default
   * @param centerId the center id
   * @return the default resource list
   */
  public List<BasicDynaBean> getDefaultResourceList(String category, String resourceScheduleName,
      Integer centersIncDefault, Integer centerId) {
    String resourceType = null;
    List<BasicDynaBean> defaultResourceList = null;
    String secondaryResquery = GET_RESOURCE_LIST;
    String resQuery = "";
    if (category.equals("DOC")) {
      resourceType = category;
    }
    if (centersIncDefault > 0 && centerId != 0) {
      resQuery = secondaryResquery.replace("#CENTER_FILTER#", "AND center_id = ?");
      defaultResourceList = DatabaseHelper.queryToDynaList(resQuery,
          new Object[] { resourceScheduleName, resourceType, centerId });
    } else {
      resQuery = secondaryResquery.replace("#CENTER_FILTER#", "");
      defaultResourceList = DatabaseHelper.queryToDynaList(resQuery,
          new Object[] { resourceScheduleName, resourceType });
    }
    return defaultResourceList;
  }
}
