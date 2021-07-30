package com.insta.hms.core.inventory.issues;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class StockIssueDetailsRepository extends GenericRepository {

  public StockIssueDetailsRepository() {
    super("stock_issue_details");
  }

  @Override
  public Integer getNextSequence() {
    return DatabaseHelper.getInteger("SELECT nextval(?)", "store_issue_details_sequence");
  }

  private static final String RESET_RETURN_QUANTITY = "UPDATE stock_issue_details"
      + " set return_qty = 0 " + " WHERE item_issue_no = (SELECT bac.activity_id::integer"
      + " from bill_activity_charge bac "
      + " left join bill_charge bc ON(bc.charge_id = bac.charge_id) "
      + " where bc.charge_head = 'INVITE' AND bc.charge_id = ?)";

  public Integer resetReturnQuantity(String chargeId) {
    return DatabaseHelper.update(RESET_RETURN_QUANTITY, chargeId);
  }

  private static final String GET_ISSUE_ITEM_CHARGE = " SELECT bc.charge_id, bc.act_description_id,"
      + " bc.insurance_claim_amount,bc.amount,bc.act_quantity,"
      + " bc.return_qty,bc.discount/(bc.act_quantity+bc.return_qty)"
      + " as discount FROM bill_charge bc " + " JOIN bill_activity_charge USING(charge_id)"
      + " WHERE activity_code = 'PHI' AND activity_id  = ? ";

  public BasicDynaBean getIssueItemCharge(String itemIssueNo) {
    return DatabaseHelper.queryToDynaBean(GET_ISSUE_ITEM_CHARGE, itemIssueNo);
  }

}
