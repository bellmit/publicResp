package com.insta.hms.billing;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.beanutils.BasicDynaBean;

import com.insta.hms.core.billing.BillChargeService;

public class PerdiemService extends ConsolidatedClaimService {


	public boolean createMarginClaim(Connection con, String billNo, String chargeId) throws SQLException, IOException {
		return insertChargeClaim(con, billNo, chargeId, BillChargeService.CH_PERDIEM);
	}

	@Override
	protected boolean insertChargeClaim(Connection con, String billNo, String chargeId, String chargeHead)
			throws SQLException, IOException {
		BillChargeClaimDAO bccdao = new BillChargeClaimDAO();
		BasicDynaBean billChargeClaimBean = getDefaultBillChargeClaimBean(con, billNo, chargeId, chargeHead, bccdao.getBean());
		// Insurance category ID for new Perdiem bill claim is set to 0.
		billChargeClaimBean.set("insurance_category_id", 0);
		return bccdao.insert(con, billChargeClaimBean);
	}
}
