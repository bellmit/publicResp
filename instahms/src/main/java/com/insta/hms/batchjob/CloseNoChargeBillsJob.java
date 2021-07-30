package com.insta.hms.batchjob;

import com.insta.hms.common.DatabaseHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author insta
 *
 *         # # Daily run of closing bills which have no charges, or where billing module is not
 *         enabled. # This is run as a cron-job before close_op_visits since closing of visits
 *         depends on open bills ] #
 *
 * 
 */
public class CloseNoChargeBillsJob extends SQLUpdateJob {

  private static Logger logger = LoggerFactory.getLogger(CloseNoChargeBillsJob.class);

  @Override
  protected List<String> getQueryList() {

    List<String> queryList = new ArrayList<String>();

    String allBillSchemas = "SELECT auto_close_nocharge_op_bills FROM generic_preferences "
        + " where auto_close_nocharge_op_bills='A'";
    if (DatabaseHelper.queryToDynaList(allBillSchemas).size() > 0) {
      logger.debug("Apply Closing bills for ***All Bill schema*****  ");
      String closeBillForAllBill = "UPDATE bill SET status='C', payment_status='P', "
          + " finalized_date=now(), last_finalized_at=now(), "
          + " closed_date=now(), username='auto_update', "
          + " remarks='Bills with zero amount closed', discharge_status = 'Y' "
          + " WHERE total_amount = 0 AND visit_type = 'o' AND status = 'A' "
          + "AND open_date < current_timestamp - interval '4 hours'";
      queryList.add(closeBillForAllBill);
    }

    String billNowSchemas = "SELECT auto_close_nocharge_op_bills FROM generic_preferences "
        + " where auto_close_nocharge_op_bills ='B'";
    if (DatabaseHelper.queryToDynaList(billNowSchemas).size() > 0) {
      logger.debug("Apply Closing bills for *****Paid Bill schema***** ");
      String closePaidBill = "UPDATE bill SET status='C', payment_status='P', "
          + " finalized_date=now(), last_finalized_at=now(), "
          + " closed_date=now(), username='auto_update', "
          + " remarks='Bills with zero amount closed', discharge_status = 'Y' "
          + " WHERE total_amount = 0 AND total_receipts = 0 AND visit_type = 'o'"
          + " AND status = 'A' "
          + " AND bill_type = 'P' AND open_date < current_timestamp - interval '4 hours'";

      queryList.add(closePaidBill);
    }

    return queryList;
  }

}
