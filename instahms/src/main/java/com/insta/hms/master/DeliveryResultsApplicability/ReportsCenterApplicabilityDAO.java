package com.insta.hms.master.DeliveryResultsApplicability;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class ReportsCenterApplicabilityDAO extends GenericDAO{

	public ReportsCenterApplicabilityDAO() {
		super("center_report_deliv_times_default");
		// TODO Auto-generated constructor stub
	}
	
	public static final String GET_REPORTS_AVAILABILITIES = "SELECT *,day_of_week::text AS day_of_week_text FROM center_report_deliv_times_default where center_id = ?";

	public static List getReportsAvailabilities(int centerId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_REPORTS_AVAILABILITIES);
			ps.setInt(1, centerId);
			return DataBaseUtil.queryToDynaList(ps);

		} finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public StringBuilder DELETE_REPORTS_TIMINGS = new StringBuilder("delete from center_report_deliv_times_default " +
			   "where center_id = ?");

	public  boolean deleteReportsTimings(Connection con,int centerId,List resAvailDetId) throws Exception{
		PreparedStatement ps = null;
		try {
			DataBaseUtil.addNotInWhereFieldInList(DELETE_REPORTS_TIMINGS, "rep_deliv_default_id", resAvailDetId,true);
			ps = con.prepareStatement(DELETE_REPORTS_TIMINGS.toString());
			ps.setInt(1, centerId);
			int index = 1;
			for(int i=0;i<resAvailDetId.size();i++) {
				ps.setInt(++index, (Integer)resAvailDetId.get(i));
			}
				return ps.executeUpdate() >= 0;

			} finally{
				DataBaseUtil.closeConnections(null, ps);
			}
		}
	}
