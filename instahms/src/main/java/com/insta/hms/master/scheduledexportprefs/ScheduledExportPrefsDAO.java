/**
 *
 */
package com.insta.hms.master.scheduledexportprefs;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author krishna
 *
 */
public class ScheduledExportPrefsDAO extends GenericDAO {
	public ScheduledExportPrefsDAO() {
		super("scheduled_export_prefs");
	}

	private static final String SCHEDULED_JOB_FIELDS = " SELECT ste.*, account_group_name ";
	private static final String FROM_TABLES = " FROM scheduled_export_prefs ste " +
		"	JOIN account_group_master agm ON (ste.account_group=agm.account_group_id) ";
	private static final String COUNT = "select count(schedule_name) ";
	public PagedList searchScheduledJobs(Map params, Map listing) throws SQLException, ParseException {
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		try {
			qb = new SearchQueryBuilder(con, SCHEDULED_JOB_FIELDS, COUNT, FROM_TABLES, listing);
			qb.addFilterFromParamMap(params);
			qb.build();
			return qb.getDynaPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}

	private static final String SCHEDULE_NAME_EXISTS = "SELECT schedule_name FROM scheduled_export_prefs " +
		" WHERE upper(schedule_name)=upper(?) and schedule_id!=?";
	public boolean exists(Connection con, String scheduleName, Integer scheduleId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(SCHEDULE_NAME_EXISTS);
			ps.setString(1, scheduleName);
			ps.setInt(2, scheduleId);
			rs = ps.executeQuery();
			if (rs.next())
				return true;
		} finally {
			DataBaseUtil.closeConnections(null, ps, rs);
		}
		return false;
	}

	private static final String PREFS_LIST = "SELECT * FROM scheduled_export_prefs ";
	public List getList(Connection con) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(PREFS_LIST);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}


}
