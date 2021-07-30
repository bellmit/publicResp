package com.insta.hms.core.billing;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class BillChargeTaxRepository extends GenericRepository{

	public BillChargeTaxRepository() {
		super("bill_charge_tax");
	}
	
	private static final String GET_BILL_CHARGE_TAX_BEAN = "SELECT * FROM bill_charge_tax WHERE charge_id=? AND tax_sub_group_id = ? ";

	public BasicDynaBean getbillChargeTaxBean(String chargeId,
			Integer itemSubGrpId) {
		return DatabaseHelper.queryToDynaBean(GET_BILL_CHARGE_TAX_BEAN, new Object[]{chargeId,itemSubGrpId});
	}
	
	private static final String GET_ITEM_SUBGROUP_CODES = "SELECT bct.tax_amount, bct.tax_sub_group_id "
	    + " as item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code, "+
			" isg.integration_subgroup_id FROM bill_charge_tax bct "+
			" JOIN item_sub_groups isg ON(bct.tax_sub_group_id = isg.item_subgroup_id) " +
			" JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) " +
			" WHERE bct.charge_id = ? ";

	public List<BasicDynaBean> getItemSubgroupCodes(String chargeId) {
		return DatabaseHelper.queryToDynaList(GET_ITEM_SUBGROUP_CODES, chargeId);
	}

  private static final String CANCEL_BILL_CHARGE_TAX = "UPDATE bill_charge_tax set tax_amount = 0 WHERE charge_id = ? ";

  public int[] cancelBillChargeTax(List<String> chargeIdsList) {
    List<Object[]> values = new ArrayList<>();
    for (String chargeId : chargeIdsList) {
      values.add(new Object[] { chargeId });
    }

    return DatabaseHelper.batchUpdate(CANCEL_BILL_CHARGE_TAX, values);
  }

	private static final String UPDATE_BILL_CHARGE_TAX_FOR_EXEMPT = 
			" update bill_charge_tax set tax_amount = coalesce ((select coalesce(sum(sponsor_tax_amount),0) "+ 
			" from bill_charge_claim_tax bclt " +
			" JOIN bill_charge_claim bcl on (bcl.charge_id = bclt.charge_id " +
			" and bcl.claim_id = bclt.claim_id) " +
			" where bclt.charge_id = ? and adj_amt ='Y' group by bclt.charge_id,tax_sub_group_id),0.00) " +
			" where charge_id = ? ";
				 
	
	public void updateAdjustedTaxAmts(String chargeId) {
		DatabaseHelper.update(UPDATE_BILL_CHARGE_TAX_FOR_EXEMPT, new Object[]{chargeId,chargeId});
	}

	private static final String UPDATE_BILL_CHARGE_CLAIM_TAX_AMT_TO_ZERO = "UPDATE bill_charge_claim_tax bcct SET sponsor_tax_amount = 0 "+
			" FROM bill_charge bc "+
			" WHERE bc.charge_id = bcct.charge_id AND bc.bill_no = ? ";
	public Boolean updateBillChargeClaimTaxAmtToZero(String billNo) {
		// TODO Auto-generated method stub
		return DatabaseHelper.update(UPDATE_BILL_CHARGE_CLAIM_TAX_AMT_TO_ZERO, new Object[]{billNo}) >= 0;

	}
	
	private static final String GET_BILL_CHARGE_TAX_ENTRY = " SELECT count(*)::INTEGER AS rec_count FROM bill_charge_tax "
			+ " WHERE charge_id=? AND tax_sub_group_id=? ";
	
	public boolean isBillChargeTaxExist(String chargeId, Integer taxSubGroupId) {
		// TODO Auto-generated method stub
		return DatabaseHelper.getInteger(GET_BILL_CHARGE_TAX_ENTRY, new Object[]{chargeId, taxSubGroupId}) > 0;
	}

	/**
   * Find list of beans
   *
   * @param map the map
   * @return the list
   */
  public List<BasicDynaBean> findAllByKey(Map map) {
    return super.findByCriteria(map);
  }
  
  private static final String  GET_HOSPITAL_ITEMS_CONTAINING_TOTAL_TAX ="SELECT bct.charge_id from bill_charge_tax bct "
  		+ " JOIN bill_charge bc ON bc.charge_id=bct.charge_id "
  		+ " JOIN bill b ON b.bill_no = bc.bill_no "
  		+ " WHERE b.visit_id = ? AND bc.status='A';";

	public List<BasicDynaBean> getAllHospitalItemsContainingTotalTax(String visitId) {
		return DatabaseHelper.queryToDynaList(GET_HOSPITAL_ITEMS_CONTAINING_TOTAL_TAX, new Object[]{visitId});
	}

	private static String GET_DETAILS = " select isg.*, ig.group_code from item_sub_groups isg "
			+ " join item_groups ig ON (isg.item_group_id = ig.item_group_id) where isg.item_subgroup_id = ? ";

	public BasicDynaBean getMasterSubGroupDetails(int subGrpID){
		return DatabaseHelper.queryToDynaBean(GET_DETAILS, new Object[]{subGrpID});
	}

	private static final String UPDATE_BILL_CHARGE_TAX_AMT_TO_ZERO = "UPDATE bill_charge_tax "
	    + " SET tax_rate = 0.00, tax_amount = 0.00, original_tax_amt = 0.00 WHERE charge_id = ? ";
	
  public Boolean updateBillChargeTaxAmtToZero(String pkgChargeIdRef) {
    return DatabaseHelper.update(UPDATE_BILL_CHARGE_TAX_AMT_TO_ZERO, new Object[]{pkgChargeIdRef}) >= 0;
  }

}
