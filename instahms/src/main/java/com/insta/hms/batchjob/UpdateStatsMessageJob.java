package com.insta.hms.batchjob;

import java.util.ArrayList;
import java.util.List;

/**
 * # Updates the predefined stats messages # Disable if not a local server: this includes backup
 * servers # # is_local_server || exit 0 # # Updates param1 , param2 fields for Stats messages. #
 *
 * @author insta
 *
 *         run_in_all_schemas
 *
 *         -- --- Update the system stats messages -- To run this, use run_in_all_schemas --
 *
 */

public class UpdateStatsMessageJob extends SQLUpdateJob {

  private static List<String> queryList = new ArrayList<String>();

  static {

    queryList.add("UPDATE system_messages SET param1=(select count(*) from bill "
        + " where status ='A' and date(open_date) < current_date "
        + "and bill_type ='P') WHERE system_type ='Stats1' ");

    queryList.add("UPDATE system_messages SET param1=(select count(*) from bill "
        + " where status ='A' and date(open_date) < current_date and bill_type ='M' "
        + "and visit_type ='r') WHERE system_type ='Stats2'");

    queryList.add("UPDATE system_messages SET param1=(select count(*) from "
        + " tests_prescribed tp join diagnostics d using (test_id) "
        + " join diagnostics_departments dd using (ddept_id) where conducted in ('N','P') "
        + " and pres_date < current_date "
        + " and dd.category='DEP_LAB') WHERE system_type ='Stats3'");

    queryList.add("UPDATE system_messages SET param1=(select count(*) from "
        + " tests_prescribed tp join diagnostics d using (test_id) "
        + " join diagnostics_departments dd using (ddept_id) "
        + " where conducted in ('N','P') and pres_date < current_date "
        + "and dd.category='DEP_RAD') WHERE system_type ='Stats4'");

    queryList.add("UPDATE system_messages SET param1=(select count(*) from services_prescribed "
        + " where conducted ='N' and date(presc_date) < current_date ) "
        + "WHERE system_type ='Stats5' ");

    queryList.add("UPDATE system_messages SET param1=(select count(*) from patient_registration "
        + " where status ='A' and visit_type ='o' "
        + "and date(reg_date) < current_date ) WHERE system_type ='Stats6'");

    queryList
        .add("UPDATE system_messages SET param1=(select count(*) from patient_registration pr  "
            + " where pr.status='A' and pr.visit_type ='i' "
            + " and date(pr.reg_date) < current_date and "
            + " patient_id not in (select patient_id from admission) ) "
            + " WHERE system_type ='Stats7'");

    queryList.add("UPDATE system_messages SET param2 = (SELECT substr(current_time::text, 0,6)) "
        + " WHERE system_type LIKE 'Stats%'");

  }

  @Override
  protected List<String> getQueryList() {
    return queryList;
  }

}
