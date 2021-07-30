/**
 *
 */
package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author lakshmi.p
 *
 */
public class BillRemittanceDAO {

	private static final String GET_BILL_PAYMENTS =
		  " SELECT COALESCE(ipu.remittance_id, ir.remittance_id) AS remittance_id, COALESCE(ir.detail_level, 'B') AS detail_level, "
		+ " COALESCE(ir.received_date, ipu.payment_recd_date) AS received_date, "
		+ " ipu.payment_reference, ipu.amount_recd, ipu.unalloc_amount, ipu.payment_id "
		+ "	FROM insurance_payment_unalloc_amount ipu"
		+ " LEFT JOIN insurance_remittance ir ON (ir.remittance_id = ipu.remittance_id)"
		+ " WHERE bill_no = ? ORDER BY remittance_id DESC";

	public static List<BasicDynaBean> getBillPayments(String billNo) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_BILL_PAYMENTS, billNo);
	}

	private static final String GET_CHARGE_PAYMENTS =
		 " SELECT ipu.remittance_id,bc.charge_id, ipu.payment_reference, COALESCE(SUM(ipa.amount),0) AS amount " +
		 "  FROM bill_charge bc " +
		 " LEFT JOIN insurance_payment_allocation ipa ON (ipa.charge_id = bc.charge_id) " +
		 " LEFT JOIN insurance_payment_unalloc_amount ipu ON (ipu.bill_no = bc.bill_no) " +
		 " WHERE bc.bill_no = ? GROUP BY bc.charge_id,ipu.payment_reference,ipu.remittance_id " +
		 " ORDER BY bc.charge_id, ipu.remittance_id DESC ";

	public static List<BasicDynaBean> getChargePayments(String billNo) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_CHARGE_PAYMENTS, billNo);
	}

	private static final String GET_CHARGE_ALLOC_PAYMENTS =
		" SELECT bill_no, remittance_id, charge_id,payment_reference, SUM(amount) AS amount " +
		"	FROM " +
		" (SELECT bc.bill_no, COALESCE(ipu.remittance_id, ir.remittance_id) AS remittance_id, " +
		"  bc.charge_id, ipu.payment_reference, 0 AS amount " +
		" FROM bill_charge bc " +
		" LEFT JOIN insurance_payment_unalloc_amount ipu ON (ipu.bill_no = bc.bill_no) " +
		" LEFT JOIN insurance_remittance ir USING (remittance_id) " +
		"	UNION " +
		" SELECT bc.bill_no, ipa.remittance_id, ipa.charge_id, ipa.payment_reference, ipa.amount " +
		" FROM insurance_payment_allocation ipa " +
		" JOIN bill_charge bc USING (charge_id)) as foo " +
		" WHERE bill_no = ? GROUP BY bill_no, charge_id, payment_reference, remittance_id ORDER BY charge_id, payment_reference ";

	public static List<BasicDynaBean> getChargeAllocPayments(String billNo) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_CHARGE_ALLOC_PAYMENTS, billNo);
	}

	private static final String DELETE_ALLOCATIONS =
			"DELETE FROM insurance_payment_allocation WHERE charge_id IN (SELECT charge_id FROM bill_charge WHERE bill_no = ?)";

	public static void deleteAllocations(Connection con, String billNo) throws SQLException {
		PreparedStatement ps = con.prepareStatement(DELETE_ALLOCATIONS);
		ps.setString(1, billNo);
		ps.executeUpdate();
		if (ps != null)
			ps.close();
	}

	private static final String GET_CHARGE_ITEM_LEVEL_REMITTANCE =
		 "  SELECT remittance_id, charge_id, detail_level,file_name FROM insurance_payment_allocation " +
		 " JOIN insurance_remittance USING (remittance_id) " +
		 " WHERE charge_id IN (SELECT charge_id FROM bill_charge WHERE bill_no = ? ) AND detail_level = 'I'" ;

	public static List<BasicDynaBean> getChargeItemLevelRemittance(String billNo) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_CHARGE_ITEM_LEVEL_REMITTANCE, billNo);
	}

}
