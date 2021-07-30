package com.insta.hms.batchjob;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class FinalizeOpenOPBills.
 *
 * @author insta This class will filized all OP bills
 */
public class FinalizeOpenOPBills extends SQLUpdateJob {

  private static final String QUERY = "UPDATE bill set status = 'F',"
      + " finalized_date=now(), last_finalized_at=now(), username='auto_update',"
      + " finalized_by = 'auto_update',mod_time=now() "
      + "where status = 'A' and bill_type = 'C' and visit_id in"
      + " (select patient_id from patient_registration where visit_type = 'o') "
      + " and open_date::date = (now() - interval '1 day')::date";

  @Override
  protected List<String> getQueryList() {
    List<String> queryList = new ArrayList<String>();
    queryList.add(QUERY);
    return queryList;
  }

}
