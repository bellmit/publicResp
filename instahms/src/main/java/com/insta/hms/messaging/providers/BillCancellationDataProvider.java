package com.insta.hms.messaging.providers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class BillCancellationDataProvider.
 *
 * @author nikunj.s
 */
public class BillCancellationDataProvider extends MapDataProvider {

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.messaging.providers.MapDataProvider#getTokens()
   */
  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "mr_no", "patient_name", "admission_date",
        "admission_date_yyyy_mm_dd", "admission_time", "admission_time_12hr", "next_of_kin_contact",
        "next_of_kin_name", "center_name", "admitted_by", "department", "patient_phone",
        "doctor_name", "referal_doctor", "doctor_mobile", "referal_doctor_mobile",
        "receipient_id__", "receipient_type__", "bill_no", "open_date", "finalized_date",
        "closed_date", "opened_by", "closed_by", "finalized_by", "total_amount", "total_claim",
        "claim_recd_amount", "approval_amount", "primary_approval_amount",
        "secondary_approval_amount", "primary_total_claim", "secondary_total_claim",
        "insurance_deduction", "discount_auth", "disc_auth_name", "total_discount", "cancel_reason",
        "total_approved_amt_for_current_month", "total_approved_amt_for_current_year", "entity_id",
        "batch_id" };
    List<String> tokenList = super.getTokens();

    if (null == tokenList) {
      tokenList = new ArrayList<String>();
    }
    for (String token : tokens) {
      if (!tokenList.contains(token)) {
        tokenList.add(token);
      }
    }
    return tokenList;
  }
}
