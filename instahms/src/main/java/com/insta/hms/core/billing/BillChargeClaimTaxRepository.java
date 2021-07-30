package com.insta.hms.core.billing;


import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BillChargeClaimTaxRepository extends GenericRepository{

	public BillChargeClaimTaxRepository() {
		super("bill_charge_claim_tax");
	}

	private static final String GET_BILL_CHARGE_CLAIM_TAX_ENTRY = " SELECT count(*)::INTEGER AS rec_count FROM bill_charge_claim_tax "
			+ " WHERE charge_id=? AND claim_id=? AND tax_sub_group_id=? ";
	
	public Boolean isBillChargeClaimTaxExist(String chargeId, String claimId,
			Integer taxSubGroupId) {
		// TODO Auto-generated method stub
		return DatabaseHelper.getInteger(GET_BILL_CHARGE_CLAIM_TAX_ENTRY, new Object[]{chargeId, claimId, taxSubGroupId}) > 0;
	}
	
  private static final String CANCEL_BILL_CHARGE_CLAIM_TAX = "UPDATE bill_charge_claim_tax SET sponsor_tax_amount = 0 "
      + " WHERE charge_id = ? ";

  public int cancelBillChargeClaimTax(String chargeId) {
    return DatabaseHelper.update(CANCEL_BILL_CHARGE_CLAIM_TAX, chargeId);
  }

  public int[] cancelBillChargeClaimTax(List<String> chargeIdsList) {
    List<Object[]> values = new ArrayList<>();
    for (String chargeId : chargeIdsList) {
      values.add(new Object[] { chargeId });
    }
    return DatabaseHelper.batchUpdate(CANCEL_BILL_CHARGE_CLAIM_TAX, values);
  }

	private static final String CHECK_FOR_TAX_ADJUSTMENTS = "select * from bill_charge_claim_tax where charge_id = ? and adj_amt ='Y' ";

	public List<BasicDynaBean> checkForTaxAdjustments(String chargeId) {
		return DatabaseHelper.queryToDynaList(CHECK_FOR_TAX_ADJUSTMENTS, new Object[]{chargeId});
	}
	

}
