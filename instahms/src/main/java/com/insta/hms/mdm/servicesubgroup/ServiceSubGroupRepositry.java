package com.insta.hms.mdm.servicesubgroup;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class ServiceSubGroupRepositry.
 */
@Repository
public class ServiceSubGroupRepositry extends MasterRepository<Integer> {

  /**
   * Instantiates a new service sub group repositry.
   */
  public ServiceSubGroupRepositry() {
    super("service_sub_groups", "service_sub_group_id", "service_sub_group_name",
        new String[] { "service_sub_group_name", "service_sub_group_id", "service_group_id" });
  }

  /** The Constant GET_ALL_SERVICE_SUB_GRPS. */
  private static final String GET_ALL_SERVICE_SUB_GRPS = "select service_sub_group_id from "
      + " service_sub_groups ";

  /**
   * Gets the all service sub grps.
   *
   * @param grpIdList
   *          the grp id list
   * @return the all service sub grps
   */
  public List getAllServiceSubGrps(List grpIdList) {
    String[] placeholdersArr = new String[grpIdList.size()];
    Arrays.fill(placeholdersArr, "?");
    StringBuilder query = new StringBuilder();
    query.append(GET_ALL_SERVICE_SUB_GRPS);
    query.append("WHERE service_group_id in (")
        .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");
    return DatabaseHelper.queryToDynaList(query.toString(), grpIdList.toArray());
  }

}
