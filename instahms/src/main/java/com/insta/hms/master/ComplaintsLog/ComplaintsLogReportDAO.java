package com.insta.hms.master.ComplaintsLog;

import au.com.bytecode.opencsv.CSVWriter;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.SearchQueryBuilder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ComplaintsLogReportDAO {


	private static final String COMLOG_EXT_QUERY_FIELDS =
		"SELECT * " ;

	private static final String COMLOG_EXT_QUERY_COUNT = "SELECT count(*) ";

	private static final String COMLOG_EXT_QUERY_TABLES = " FROM complaintslog cl";


	public static void complaintsLogExportCSV(CSVWriter writer, java.util.Date from, java.util.Date to,
			String status)
		throws SQLException, IOException {

		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = new SearchQueryBuilder(con,
				COMLOG_EXT_QUERY_FIELDS , COMLOG_EXT_QUERY_COUNT, COMLOG_EXT_QUERY_TABLES,
				null, "cl.complaint_id", false, 0, 0);

		if (!status.equals("*")) qb.addFilter(qb.STRING, "cl.complaint_status", "=", status);
		qb.addFilter(qb.DATE,   "date_trunc('day', cl.updated_date)", ">=", from);
		qb.addFilter(qb.DATE,   "date_trunc('day', cl.updated_date)", "<=", to);

		qb.build();

		PreparedStatement ps = qb.getDataStatement();
		ResultSet rs = ps.executeQuery();

		// write as CSV
		writer.writeAll(rs, true);

		// cleanup
		qb.close(); con.close();
	}

}
