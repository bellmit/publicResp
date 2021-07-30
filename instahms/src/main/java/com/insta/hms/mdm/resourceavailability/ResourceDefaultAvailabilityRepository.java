package com.insta.hms.mdm.resourceavailability;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

@Repository
public class ResourceDefaultAvailabilityRepository extends MasterRepository<Integer> {

  public ResourceDefaultAvailabilityRepository() {
    super("sch_default_res_availability_details", "res_sch_id");
  }

  private static String RES_AVAIL_TABLES = "FROM sch_resource_availability sra ";

  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(RES_AVAIL_TABLES);
  }

  public static final String GET_RESOURCE_DETAILS_BY_AVAILABILTY_DATE = " SELECT * FROM "
      + " sch_resource_availability sm "
      + "where  res_sch_type = ? AND res_sch_name= ? AND availability_date = ?";

  public BasicDynaBean getResourceByAvailDate(String resourceType, String resourceName,
      java.sql.Date date) {
    return DatabaseHelper.queryToDynaBean(GET_RESOURCE_DETAILS_BY_AVAILABILTY_DATE, new Object[] {
        resourceType, resourceName, date });
  }

}
