/**
 *
 */
package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author lakshmi.p
 *
 */
public class ClaimReceiptsDAO extends GenericDAO {

	public ClaimReceiptsDAO() {
		super("insurance_claim_receipt");
	}

	public String getNextClaimReceiptNo() throws SQLException {
		return DataBaseUtil.getNextPatternId("insurance_claim_receipt");
	}

	private static final String CLAIM_RECEIPTS_FIELDS = "SELECT receipt_no, payment_reference, reference_no,"
		+ " insurance_co_id,insurance_co_name,"
		+ " tpa_id,tpa_name, display_date,"
		+ " amount, payment_mode_id, payment_mode, card_type_id, card_type, "
		+ " counter, counter_no, counter_type, bank_name, remarks, username, mod_time, remittance_total_amount ";

	private static final String CLAIM_RECEIPTS_COUNT = "SELECT count(receipt_no)";

	private static final String CLAIM_RECEIPTS_TABLES = " FROM (" +
			" SELECT r.receipt_no, r.amount, r.display_date, r.mod_time, r.counter," +
			" r.payment_mode_id, pm.payment_mode, r.card_type_id, cm.card_type, r.bank_name, r.payment_reference,r.reference_no, r.username, " +
			" r.remarks, counter_type, counter_no, r.tpa_id, tpa_name, r.insurance_co_id,insurance_co_name,remittance_total_amount " +
			" FROM insurance_claim_receipt r  " +
			"   JOIN payment_mode_master pm ON (pm.mode_id = r.payment_mode_id) " +
			"   LEFT JOIN card_type_master cm ON (cm.card_type_id = r.card_type_id) " +
			" 	LEFT JOIN counters c on r.counter=counter_id " +
			"   LEFT JOIN tpa_master tp ON (tp.tpa_id = r.tpa_id)"+
			"   LEFT JOIN insurance_company_master icm ON(icm.insurance_co_id = r.insurance_co_id)" +
			"   LEFT JOIN (SELECT sum(amount) AS remittance_total_amount, " +
			"				payment_reference FROM  insurance_payment_allocation GROUP BY payment_reference) AS pamt " +
			"	ON (pamt.payment_reference = r.payment_reference) ) AS foo";


	public static PagedList searchClaimReceipts(Map filter, Map<LISTING, Object> listing) throws ParseException, SQLException {
		Connection con = DataBaseUtil.getReadOnlyConnection();

		SearchQueryBuilder qb = new SearchQueryBuilder(con,
				CLAIM_RECEIPTS_FIELDS, CLAIM_RECEIPTS_COUNT,
				CLAIM_RECEIPTS_TABLES, listing);

		qb.addFilterFromParamMap(filter);
		qb.build();
		PagedList l = qb.getMappedPagedList();
		qb.close();
		con.close();
		return l;
	}

	/*
	 * Consolidated sponsor claim receipts (for remittance)
	 * between the given dates. Used in Tally Export.
	 */

	private static final String ALL_CLAIM_RECEIPTS =
		" SELECT * FROM (SELECT receipt_no, payment_reference, icr.insurance_co_id, icr.tpa_id, display_date, " +
		"   amount, counter, bank_name, reference_no, remarks, username," +
		"	mod_time, payment_mode_id, icr.card_type_id, bank_batch_no, card_auth_code," +
		"   card_holder_name, currency_id, exchange_rate, exchange_date," +
		"   currency_amt, card_expdate, card_number, " +
		"   c.counter_no, tm.tpa_name, icm.insurance_co_name, 1 AS account_group_id, " +
		"	pm.spl_account_name, pm.bank_required, pm.ref_required, hcm.center_code, c.center_id " +
		" FROM insurance_claim_receipt icr " +
		" JOIN tpa_master tm ON (tm.tpa_id = icr.tpa_id)" +
		" JOIN counters c ON (c.counter_id = icr.counter) " +
		" JOIN payment_mode_master pm ON (pm.mode_id = icr.payment_mode_id)" +
		" JOIN hospital_center_master hcm ON (hcm.center_id = c.center_id)" +
		" LEFT JOIN card_type_master ctm ON (ctm.card_type_id = icr.card_type_id)" +
		" LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = icr.insurance_co_id)" +
		" WHERE c.collection_counter = 'Y') AS foo ";

	public static List<BasicDynaBean> getAllClaimReceipts(Connection con, java.sql.Timestamp fromDate,
			java.sql.Timestamp toDate, Integer accountGroup, int centerId, List receiptNos) throws SQLException {
		PreparedStatement ps = null;
		try {
			String query = ALL_CLAIM_RECEIPTS;

			StringBuilder where = new StringBuilder();
			where.append(" WHERE 1 = 1 ");

			if (fromDate != null && toDate != null) {
				where.append(" AND mod_time BETWEEN ? AND ? AND account_group_id = ? ");
				if (centerId != 0)
					where.append(" AND center_id=? ");
			} else {
				if (receiptNos == null || receiptNos.isEmpty()) return Collections.EMPTY_LIST;
				DataBaseUtil.addWhereFieldInList(where, "receipt_no", receiptNos);
			}
			
			query = query + where.toString();
			ps = con.prepareStatement(query);

			if (fromDate != null && toDate != null) {
				ps.setTimestamp(1, fromDate);
				ps.setTimestamp(2, toDate);
				ps.setInt(3, accountGroup);
				if (centerId != 0)
					ps.setInt(4, centerId);
			} else {
				Iterator it =  receiptNos.iterator();
				int i = 1;
				while (it.hasNext()) {
					ps.setString(i++, (String) ((Map) it.next()).get("voucher_no"));
				}
			}
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}
}
