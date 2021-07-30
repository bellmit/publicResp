/**
 *
 */
package com.insta.hms.billing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

/**
 * @author lakshmi.p
 *
 */
public class DepositRealizationDAO {
	
	private static SimpleDateFormat uiDateFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private static String DEPOSIT_REALIZATION_FIELDS = "SELECT * ";

	private static String DEPOSIT_REALIZATION_COUNT = " SELECT count(*) ";

	private static String DEPOSIT_REALIZATION_TABLES = " FROM (SELECT pdp.mr_no, full_name as name,"+
		" pdp.receipt_id as deposit_no, pdp.receipt_type as deposit_type, pdp.amount, pdp.display_date as deposit_date, pdp.bank_name, "+
		" pdp.reference_no, pdp.created_by AS username FROM receipts pdp JOIN patient_details_display_view pdv " +
		" ON (pdv.mr_no = pdp.mr_no) WHERE realized='N' AND pdp.is_deposit #deposit_date#) as realization ";


	public PagedList getDepositRealizationList(Map filter, Map listing)throws SQLException,
		   ParseException {
		String[] depositDate = (String[]) filter.get("deposit_date");
		String dateRange;
		if ("".equals(depositDate[0]) && "".equals(depositDate[1])) {
			dateRange = "";
		} else {
			if (!"".equals(depositDate[0]) && "".equals(depositDate[1])) {
				Date fromDate = uiDateFormatter.parse(depositDate[0]);
				dateRange = "AND pdp.display_date::date >= '" + dateFormat.format(fromDate) + "'";
			} else if ("".equals(depositDate[0]) && !"".equals(depositDate[1])) {
				Date toDate = uiDateFormatter.parse(depositDate[1]);
				dateRange = "AND pdp.display_date::date <= '" + dateFormat.format(toDate) + "'";
			} else {
				Date fromDate = uiDateFormatter.parse(depositDate[0]);
				Date toDate = uiDateFormatter.parse(depositDate[1]);
				dateRange = "AND pdp.display_date::date >= '" + dateFormat.format(fromDate) + "' AND pdp.display_date::date <= '"
						+ dateFormat.format(toDate) + "'";
			}
		}
		filter.remove("deposit_date");
		
		Connection con = DataBaseUtil.getReadOnlyConnection();
		SearchQueryBuilder qb =
			new SearchQueryBuilder(con,DEPOSIT_REALIZATION_FIELDS,DEPOSIT_REALIZATION_COUNT,
					DEPOSIT_REALIZATION_TABLES.replace("#deposit_date#", dateRange), listing);

		qb.addFilterFromParamMap(filter);
		qb.build();

		PagedList l = qb.getMappedPagedList();

		qb.close();
		con.close();

		return l;
	}

	private static final String REALIZE_DEPOSITS = "UPDATE receipts SET realized='Y' WHERE receipt_id=?";

	public static String realizeDeposits(String[] realizeDepositChecks) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		String error = "Failed to realize deposits";
		boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(REALIZE_DEPOSITS);
			for (String deposit_no : realizeDepositChecks) {
				ps.setString(1, deposit_no);
				ps.addBatch();
			}
			int[] results = ps.executeBatch();
			success = DataBaseUtil.checkBatchUpdates(results);

			if (success)
				error = null;
		} finally {
		  if(ps != null) {
		    ps.close();
		  }
			DataBaseUtil.commitClose(con, (error == null));
		}
		return error;
	}
}
