package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class BillChargeClaimTaxDAO extends GenericDAO{

	public BillChargeClaimTaxDAO(){
		super("bill_charge_claim_tax");
	}
	
	private static final String CANCEL_BILL_CHARGE_CLAIM_TAX ="UPDATE bill_charge_claim_tax SET sponsor_tax_amount = 0 "+
			 " WHERE charge_id = ? ";

	public boolean cancelBillChargeClaimTax(Connection con, String chargeId) throws SQLException {
		// TODO Auto-generated method stub
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(CANCEL_BILL_CHARGE_CLAIM_TAX);
			ps.setString(1, chargeId);
			return ps.executeUpdate() > 0;
		}finally{
			if(null != ps) ps.close();
		}
		
	}

	public void cancelBillChargeClaimTax(Connection con,
			List<ChargeDTO> updateBillChargeList) throws SQLException{
		// TODO Auto-generated method stub
		for(ChargeDTO charge:updateBillChargeList){
			String chargeId = charge.getChargeId();
			if(charge.getStatus().equals("X"))
				cancelBillChargeClaimTax(con, chargeId);
		}
	}

	private static final String GET_BILL_CHARGE_CLAIM_TAX_ENTRY = " SELECT count(*)::INTEGER AS rec_count FROM bill_charge_claim_tax "
			+ " WHERE charge_id=? AND claim_id=? AND tax_sub_group_id=? ";
	
	public boolean isBillChargeClaimTaxExist(Connection con, String chargeId, String claimId, Integer taxSubGroupId) throws SQLException {
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(GET_BILL_CHARGE_CLAIM_TAX_ENTRY);
			ps.setString(1, chargeId);
			ps.setString(2, claimId);
			ps.setInt(3, taxSubGroupId);
			BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
			if(bean == null) {
				return false;
			} else {
				Integer count = (Integer)bean.get("rec_count");
				if(count > 0) {
					return true;
				} else {
					return false;
				}
			}
		} finally  {
			DataBaseUtil.closeConnections(null, ps);
		}	
	}
}
