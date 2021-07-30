package com.insta.hms.batchjob;

import java.util.ArrayList;
import java.util.List;

public class PackageJob extends SQLUpdateJob {

  /*
   * inactivate the active packages which are not valid today.
   */

  private static final String INACTIVATE_PACKAGES = "update packages "
      + "set status='I' where status='A' and "
      + "NOT (current_date between coalesce(valid_from, current_date) and "
      + "coalesce(valid_till, current_date))";

  /*
   * activate the inactive packages which are valid today.
   */

  private static final String ACTIVATE_PACKAGES = "update packages "
      + "set status='A' where status='I' and case when valid_from"
      + " is not null and "
      + " valid_till is null then current_date >= valid_from when valid_till "
      + " is not null and "
      + " valid_from is null then current_date <= valid_till when valid_from "
      + " is not null and "
      + " valid_till is not null then current_date between valid_from and valid_till"
      + " else false end";

  @Override
  protected List<String> getQueryList() {
    List<String> queryList = new ArrayList<String>();
    queryList.add(INACTIVATE_PACKAGES);
    queryList.add(ACTIVATE_PACKAGES);
    return queryList;
  }
}
