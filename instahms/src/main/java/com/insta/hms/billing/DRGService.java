package com.insta.hms.billing;

import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DRGService {

	public boolean createMarginClaim(Connection con, String billNo, String chargeId) throws SQLException, IOException {
		return insertChargeClaim(con, billNo, chargeId, "MARDRG");
	}

	public boolean createOutlierClaim(Connection con, String billNo, String chargeId) throws SQLException, IOException {
		return insertChargeClaim(con, billNo, chargeId, "OUTDRG");
	}

	public boolean updateClaimAmount(Connection con, BasicDynaBean chrg) throws SQLException, IOException {
		String billNo = (String)chrg.get("bill_no");
		int cnt = 0;
		boolean success = false;
		if (null != billNo && !billNo.equals("")) {
			BasicDynaBean bcdao = new BillClaimDAO().getPrimaryBillClaim(con, billNo);
			BillChargeClaimDAO bccdao = new BillChargeClaimDAO();
			Map keyMap = new HashMap();
			keyMap.put("bill_no", billNo);
			keyMap.put("claim_id", (String)bcdao.get("claim_id"));
			keyMap.put("charge_id", chrg.get("charge_id"));
			BasicDynaBean bccbean = bccdao.findByKey(con, keyMap);
			if (null == bccbean) {
				insertChargeClaim(con, billNo, (String)chrg.get("charge_id"), (String)chrg.get("charge_head"));
				bccbean = bccdao.findByKey(con, keyMap);
			}
			bccbean.set("insurance_claim_amt", chrg.get("insurance_claim_amount"));
			cnt = bccdao.update(con, bccbean.getMap(), keyMap);
		}
		if (cnt > 0) success = true;
		return success;
	}

	public boolean updateSalesClaimAmount(Connection con, String saleId) throws SQLException, IOException {
		boolean success = false;
		List<BasicDynaBean> saleItemlist = new GenericDAO("store_sales_details").findAllByKey(con,"sale_id", saleId);
		GenericDAO salesClaimDAO = new GenericDAO("sales_claim_details");
		int count = 0;
		if(null != saleItemlist){
			for(BasicDynaBean bean : saleItemlist){
				int saleItemId = (Integer)bean.get("sale_item_id");
				BasicDynaBean saleItemClaimBean = salesClaimDAO.findByKey(con, "sale_item_id", saleItemId);
				saleItemClaimBean.set("insurance_claim_amt", BigDecimal.ZERO);
				saleItemClaimBean.set("return_insurance_claim_amt", BigDecimal.ZERO);
				count = salesClaimDAO.update(con, saleItemClaimBean.getMap(), "sale_item_id", saleItemId);
			}
		}
		if(count > 0) success = true;
		return success;

	}

	private boolean insertChargeClaim(Connection con, String billNo, String chargeId, String chargeHead)
	throws SQLException, IOException {

		BillChargeClaimDAO bccdao = new BillChargeClaimDAO();
		BasicDynaBean bcbean = new BillClaimDAO().getPrimaryBillClaim(con, billNo);// bcdao.findByKey(con, keyMap);
		String sponsorId = null, claimId = null;
		if (null != bcbean) {
			sponsorId = (String)bcbean.get("sponsor_id");
			claimId = (String)bcbean.get("claim_id");
		}
		BasicDynaBean bccbean = bccdao.getBean();
		bccbean.set("bill_no", billNo);
		bccbean.set("charge_id", chargeId);
		bccbean.set("insurance_claim_amt", BigDecimal.ZERO);
		bccbean.set("claim_status", "O");
		bccbean.set("claim_recd_total", BigDecimal.ZERO);
		bccbean.set("return_insurance_claim_amt", BigDecimal.ZERO);
		bccbean.set("charge_head", chargeHead);
		bccbean.set("claim_id", claimId);
		bccbean.set("sponsor_id", sponsorId);

		return bccdao.insert(con, bccbean);
	}

	public boolean updateClaimAmount(Connection con, String chargeId) throws SQLException, IOException {

		GenericDAO bcdao = new GenericDAO("bill_charge");
		BasicDynaBean chrg = bcdao.findByKey(con, "charge_id", chargeId);
		return updateClaimAmount(con, chrg);
	}

}
