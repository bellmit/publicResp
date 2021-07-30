package com.insta.hms.core.inventory.issues;

import com.insta.hms.common.DatabaseHelper;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.Iterator;
import java.util.List;

/**
 * The Class PatientIssueReturnsRepository.
 */
@Repository
public class PatientIssueReturnsRepository {

  /** The Constant GET_ISSUE_RETURN_CHARGES. */
  private static final String GET_ISSUE_RETURN_CHARGES = "SELECT bc.charge_id, "
      + "bac.act_description_id as medicine_id, b.visit_id, sird.item_batch_id, "
      + "sirm.dept_to, bc.act_quantity " + "FROM bill_charge bc "
      + "LEFT JOIN bill b ON (bc.bill_no = b.bill_no) "
      + "LEFT JOIN bill_activity_charge bac ON (bc.charge_id = bac.charge_id) "
      + "LEFT JOIN store_issue_returns_details sird ON (bac.activity_id::integer = item_return_no) "
      + "LEFT JOIN store_issue_returns_main sirm on (sird.user_return_no = sirm.user_return_no) "
      + "WHERE bc.charge_head = 'INVRET' AND b.visit_id = ? order by sirm.user_return_no ";

  /**
   * Gets the issue return charges.
   *
   * @param visitId the visit id
   * @return the issue return charges
   */
  public List<BasicDynaBean> getIssueReturnCharges(String visitId) {
    return DatabaseHelper.queryToDynaList(GET_ISSUE_RETURN_CHARGES, visitId);
  }

  /** The Constant VISIT_ISSUES_OF_A_MEDICINE_FOR_RETURN. */
  private static final String VISIT_ISSUES_OF_A_MEDICINE_FOR_RETURN = 
      " SELECT  bc.charge_id, bc.act_rate, bc.act_quantity, sid.qty,"
      + " (SELECT COALESCE(SUM(tax_amount), 0) from bill_charge_tax bct"
      + " WHERE bct.charge_id = bc.charge_id) tax_amt, "
      + "  (SELECT COALESCE(SUM(original_tax_amt), 0) from bill_charge_tax bct"
      + " WHERE bct.charge_id = bc.charge_id) original_tax_amt, "
      + " sid.medicine_id, sid.item_batch_id, sid.return_qty, sid.item_issue_no "
      + " FROM stock_issue_main sim JOIN stock_issue_details sid USING(user_issue_no) "
      + " JOIN bill_activity_charge bac ON (item_issue_no = activity_id::integer"
      + " AND activity_code = 'PHI') " + " JOIN bill_charge bc ON (bc.charge_id = bac.charge_id) "
      + " WHERE issued_to = ? AND medicine_id = ? AND sid.return_qty < qty AND"
      + " item_batch_id = ? AND dept_from = ? "
      + " AND substring(bc.charge_id from 3)::integer < ?"
      + " ORDER BY user_issue_no DESC";

  /**
   * Gets the visit item issues.
   *
   * @param visitId the visit id
   * @param medicineId the medicine id
   * @param itemBatchId the item batch id
   * @param storeId the store id
   * @param returnChargeId the return charge id
   * @return the visit item issues
   */
  public List<BasicDynaBean> getVisitItemIssues(String visitId, Integer medicineId, int itemBatchId,
      int storeId, String returnChargeId) {
    return DatabaseHelper.queryToDynaList(VISIT_ISSUES_OF_A_MEDICINE_FOR_RETURN, visitId,
        medicineId, itemBatchId, storeId, Integer.parseInt(returnChargeId.substring(2)));
  }

  /** The Constant UPDATE_CHARGE_FOR_ISSUE_RETURNS. */
  private static final String UPDATE_CHARGE_FOR_ISSUE_RETURNS = " UPDATE bill_charge"
      + " SET return_insurance_claim_amt = return_insurance_claim_amt + ?, "
      + " return_amt= return_amt + ?, return_qty= return_qty + ?, discount = discount- ? , "
      + "overall_discount_amt = overall_discount_amt - ? ,amount = amount + ?,"
      + " return_tax_amt = return_tax_amt + ?,"
      + " return_original_tax_amt = return_original_tax_amt + ? " + " WHERE charge_id = ?";

  /**
   * Update charges for returns.
   *
   * @param saleIdChargesToUpdate the sale id charges to update
   * @return true, if successful
   */
  public boolean updateChargesForReturns(List<BasicDynaBean> saleIdChargesToUpdate) {

    Iterator<BasicDynaBean> iterator = saleIdChargesToUpdate.iterator();
    while (iterator.hasNext()) {
      BasicDynaBean charge = iterator.next();

      int rows = DatabaseHelper.update(UPDATE_CHARGE_FOR_ISSUE_RETURNS,
          new Object[] { charge.get("return_insurance_claim_amt"), charge.get("return_amt"),
              charge.get("return_qty"), charge.get("discount"), charge.get("discount"),
              charge.get("discount"), charge.get("return_tax_amt"),
              charge.get("return_original_tax_amt"), charge.get("charge_id") });

      if (rows == 0) {
        return false;
      }
    }
    return true;
  }

}
