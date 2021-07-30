package com.insta.hms.batchjob;

import java.util.ArrayList;
import java.util.List;

public class SponsorApprovalsJob extends SQLUpdateJob {

  private static String visitBillFinalize = "UPDATE bill b SET status = 'F' "
      + " FROM patient_registration pr " + " JOIN consolidated_patient_bill cpb  "
      + " ON(pr.main_visit_id = cpb.main_visit_id) "
      + " WHERE b.visit_id = pr.patient_id AND cpb.bill_no = b.bill_no AND b.status = 'A' "
      + " AND pr.status = 'I'";

  private static String visitBillsClose = "UPDATE bill b set status = 'C' "
      + " FROM consolidated_patient_bill cpb WHERE cpb.bill_no = b.bill_no "
      + " AND b.status = 'F' " + " AND (b.total_amount - total_claim - total_receipts) = 0 "
      + " AND (b.total_claim - "
      + " b.primary_total_sponsor_receipts - b.secondary_total_sponsor_receipts) = 0";

  private static String consolidatedBillsClose = "UPDATE "
      + " consolidated_patient_bill cpb set status='C'"
      + " FROM (SELECT count(*) as closedBillsCnt, consolidated_bill_no"
      + " from consolidated_patient_bill cpb "
      + " join bill b on(cpb.bill_no = b.bill_no) where b.status='C' "
      + " group by consolidated_bill_no) as closedBills "
      + " JOIN (SELECT count(*) as billCnt, consolidated_bill_no "
      + " from consolidated_patient_bill cpb "
      + " join bill b on(cpb.bill_no = b.bill_no) group by "
      + " consolidated_bill_no) as totalBills "
      + " ON(closedBills.consolidated_bill_no = totalBills.consolidated_bill_no) "
      + " WHERE closedBills.consolidated_bill_no = cpb.consolidated_bill_no "
      + " AND closedBills.closedBillsCnt = totalBills.billCnt AND cpb.status = 'F'";

  private static String expireApprovals = "UPDATE patient_sponsor_approvals "
      + " SET status='C' WHERE validity_end < current_date";

  private static String finalizeConsolidatedBills = "UPDATE consolidated_patient_bill cpb "
      + " SET status = 'F' "
      + " FROM bill b " + " WHERE b.bill_no = cpb.bill_no AND cpb.status = 'A' AND "
      + " b.open_date::date between (date_trunc('month', now()) - interval '1 month') AND "
      + " (date_trunc('month', now())::date - 1) AND b.status != 'A' ";

  @Override
  protected List<String> getQueryList() {
    List<String> queryList = new ArrayList<String>();
    queryList.add(visitBillFinalize);
    queryList.add(visitBillsClose);
    queryList.add(finalizeConsolidatedBills);
    queryList.add(consolidatedBillsClose);
    queryList.add(expireApprovals);
    return queryList;
  }

}
