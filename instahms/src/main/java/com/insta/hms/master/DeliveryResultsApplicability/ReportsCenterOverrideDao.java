package com.insta.hms.master.DeliveryResultsApplicability;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class ReportsCenterOverrideDao extends GenericDAO {

	public ReportsCenterOverrideDao() {
		super("center_report_deliv_days_overrides");
		// TODO Auto-generated constructor stub
	}
	
	private static String RES_AVAIL_FIELDS = " SELECT *  ";

	private static String RES_AVAIL_COUNT = " SELECT count(*) ";

	private static String RES_AVAIL_TABLES = "FROM center_report_deliv_days_overrides sra ";

	public PagedList getReportsAvailabilityList(Map map, Map pagingParams, int centerId)
		throws Exception, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb = new SearchQueryBuilder(con, RES_AVAIL_FIELDS,
					RES_AVAIL_COUNT, RES_AVAIL_TABLES,  pagingParams);
			if (centerId != 0)
				qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
			qb.addSecondarySort("day",false);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}
	
	public String GET_REPORTS = " SELECT * FROM center_report_deliv_days_overrides sra where  center_id = ? order by day ";
	
	public List<BasicDynaBean> getReportsList(Integer centerId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_REPORTS);
			ps.setInt(1, centerId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public String GET_REPORTS_DAY = " SELECT * FROM center_report_deliv_days_overrides sra where  center_id = ? ";
	
	public BasicDynaBean getReportsListSize(Integer centerId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_REPORTS_DAY);
			ps.setInt(1, centerId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
		
	public String REPORTS_LIST = "select * from center_report_deliv_override_details where rep_deliv_override_id=?";
	
	public List<BasicDynaBean> getOverrideReportsList(Integer centerId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(REPORTS_LIST);
			ps.setInt(1, centerId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public String REPORTS_LIST_COUNT = "select count(*) from center_report_deliv_override_details where rep_deliv_override_id=?";
	
	public int getOverrideReportsListCount(Connection con,Integer repDelivId) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(REPORTS_LIST_COUNT);
			ps.setInt(1, repDelivId);
			return DataBaseUtil.getIntValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

}
