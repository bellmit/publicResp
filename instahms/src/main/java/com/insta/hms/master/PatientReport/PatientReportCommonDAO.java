package com.insta.hms.master.PatientReport;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/*
 * Holds common functions that deals with multiple tables of the templates
 */
public class PatientReportCommonDAO {

	public PatientReportCommonDAO() {
		// we deal with multiple tables here, so cannot use the generic methods.
		//super(null);
	}

	/*
	 * Search function to list all templates (without the contents)
	 */
	private static final String SELECT_FIELD = "SELECT id, caption, title, type, format, status ";
	private static final String SELECT_COUNT = "SELECT count(*)";
	private static final String FROM_TABLE = "FROM all_patient_reports_view ";

	public PagedList list(Map requestParams, Map<LISTING, Object> listingParams)
		throws SQLException, ParseException {

		Connection con = DataBaseUtil.getReadOnlyConnection();
		SearchQueryBuilder qb = new SearchQueryBuilder(con, SELECT_FIELD,
				SELECT_COUNT, FROM_TABLE, null, listingParams);
		try {
			qb.addFilterFromParamMap(requestParams);
			qb.addSecondarySort("id");
			qb.build();
			return qb.getMappedPagedList();
		} finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String LIST_ALL_ACTIVE = SELECT_FIELD +
		" FROM all_patient_reports_view WHERE status='A'";

	public List listAllActive() throws SQLException {
		return DataBaseUtil.queryToDynaList(LIST_ALL_ACTIVE);
	}

}

