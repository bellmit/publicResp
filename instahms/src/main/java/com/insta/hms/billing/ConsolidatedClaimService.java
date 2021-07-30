package com.insta.hms.billing;

import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ConsolidatedClaimService {

	protected boolean insertChargeClaim(Connection con, String billNo, String chargeId, String chargeHead) throws SQLException, IOException {
		BillChargeClaimDAO bccdao = new BillChargeClaimDAO();
		return bccdao.insert(con, getDefaultBillChargeClaimBean(con, billNo, chargeId, chargeHead, bccdao.getBean()));
	}

	protected BasicDynaBean getDefaultBillChargeClaimBean(Connection con, String billNo, String chargeId, String chargeHead,
			BasicDynaBean billChargeClaimBean) throws SQLException {
		BasicDynaBean bcbean = new BillClaimDAO().getPrimaryBillClaim(con, billNo);
		String sponsorId = null, claimId = null;
		if (null != bcbean) {
			sponsorId = (String)bcbean.get("sponsor_id");
			claimId = (String)bcbean.get("claim_id");
		}
		billChargeClaimBean.set("bill_no", billNo);
		billChargeClaimBean.set("charge_id", chargeId);
		billChargeClaimBean.set("insurance_claim_amt", BigDecimal.ZERO);
		billChargeClaimBean.set("claim_status", "O");
		billChargeClaimBean.set("claim_recd_total", BigDecimal.ZERO);
		billChargeClaimBean.set("return_insurance_claim_amt", BigDecimal.ZERO);
		billChargeClaimBean.set("charge_head", chargeHead);
		billChargeClaimBean.set("claim_id", claimId);
		billChargeClaimBean.set("sponsor_id", sponsorId);
		return billChargeClaimBean;
	}

	public boolean updateClaimAmount(Connection con, String chargeId) throws SQLException, IOException {

		GenericDAO bcdao = new GenericDAO("bill_charge");
		BasicDynaBean chrg = bcdao.findByKey(con, "charge_id", chargeId);
		return updateClaimAmount(con, chrg);
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
}
