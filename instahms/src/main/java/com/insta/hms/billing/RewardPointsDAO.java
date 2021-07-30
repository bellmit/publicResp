/**
 *
 */
package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author lakshmi
 *
 */
public class RewardPointsDAO {


	public static final String BILL_REWARD_POINTS_DETAILS =
		" SELECT pr.mr_no, COALESCE(rps.points_redeemed, 0) AS total_points_redeemed, " +
		"  COALESCE(rps.points_earned, 0) AS total_points_earned," +
		"  COALESCE(rps.open_points_redeemed, 0) AS total_open_points_redeemed," +
		"  visit_id, bill_no, COALESCE(b.points_earned,0) AS points_earned," +
		"  COALESCE(b.points_redeemed,0) AS points_redeemed," +
		"  COALESCE(b.points_redeemed_amt,0) AS points_redeemed_amt" +
		" FROM bill b" +
		" JOIN patient_registration pr ON (pr.patient_id = b.visit_id) " +
		" LEFT JOIN reward_points_status rps ON (pr.mr_no = rps.mr_no) " +
		" WHERE bill_no=? ";

	public static BasicDynaBean getBillRewardPointsDetails(String billNo) throws SQLException {
		return DataBaseUtil.queryToDynaBean(BILL_REWARD_POINTS_DETAILS, billNo);
	}

	public static BasicDynaBean getBillRewardPointsDetails(Connection con, String billNo) throws SQLException {
    return DataBaseUtil.queryToDynaBean(con, BILL_REWARD_POINTS_DETAILS, billNo);
  }
	
	public static final String GET_REWARD_POINTS_DETAILS =
		" SELECT mr_no, COALESCE(points_redeemed, 0) AS total_points_redeemed, " +
		"  COALESCE(points_earned, 0) AS total_points_earned," +
		"  COALESCE(open_points_redeemed, 0) AS total_open_points_redeemed " +
		" FROM reward_points_status " +
		" WHERE mr_no=?";

	public static BasicDynaBean getPatientRewardPointsDetails(String mrno)
			throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_REWARD_POINTS_DETAILS, mrno);
	}

	public static final String REWARD_POINTS_FIELDS = " SELECT * ";
	public static final String REWARD_POINTS_COUNT  = " SELECT count(mr_no)";
	public static final String REWARD_POINTS_TABLES = " FROM (" +
			" SELECT pd.mr_no, pd.salutation, pd.salutation_id, pd.patient_name, " +
			"	pd.last_name, pd.patient_gender, pd.patient_phone, pd.full_name, " +
			"	pd.patient_city, pd.city_name, pd.patient_state, pd.state_name, " +
			"	pd.country, pd.country_name, pd.patient_area, pd.dateofbirth, " +
			"	pd.expected_dob, pd.age, pd.agein, pd.visit_status, " +
			"	COALESCE(rps.points_earned,0) as total_points_earned," +
			"	COALESCE(rps.points_redeemed,0) as total_points_redeemed," +
			"	COALESCE(rps.open_points_redeemed,0) as total_open_points_redeemed," +
			"	(COALESCE(rps.points_earned,0) - " +
			"		COALESCE(rps.points_redeemed,0) - " +
			"		COALESCE(rps.open_points_redeemed,0)) as total_points_available" +
			" FROM patient_details_display_view pd" +
			" LEFT JOIN reward_points_status rps ON (rps.mr_no = pd.mr_no)" +
			" WHERE pd.visit_status != 'N' ) AS foo ";

	public static PagedList searchPatients(Map filter, Map listing)
			throws SQLException, ParseException {

		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			String sortField = (String) listing.get(LISTING.SORTCOL);
			boolean sortReverse = (Boolean) listing.get(LISTING.SORTASC);
			int pageSize = (Integer) listing.get(LISTING.PAGESIZE);
			int pageNum = (Integer) listing.get(LISTING.PAGENUM);

			SearchQueryBuilder qb = new SearchQueryBuilder(con,
					REWARD_POINTS_FIELDS, REWARD_POINTS_COUNT,
					REWARD_POINTS_TABLES, null, null, sortField, sortReverse,
					pageSize, pageNum);

			qb.addFilterFromParamMap(filter);
			qb.build();

			PagedList l = null;

			PreparedStatement psData = qb.getDataStatement();
			PreparedStatement psCount = qb.getCountStatement();

			List list = DataBaseUtil.queryToDynaList(psData);

			int totalCount = 0;
			try(ResultSet rsCount = psCount.executeQuery();){
			  if (rsCount.next()) {
	        totalCount = rsCount.getInt(1);
	      }
			}
			
			l = new PagedList(list, totalCount, pageSize, pageNum);

			qb.close();
			return l;

		}finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}
	
	private static final String GET_TOTAL_POINTS_EARNED = "SELECT sum(points) from reward_points_earnings where mr_no = ? ";

	public int getTotalpointsEarned(Connection con, String mr_no) throws SQLException{
		PreparedStatement ps = null;
		try{
		 ps = con.prepareStatement(GET_TOTAL_POINTS_EARNED);
		ps.setString(1, mr_no);
		return DataBaseUtil.getIntValueFromDb(ps);
			
		}finally{
			if(ps!=null) ps.close();	
		}
	}

	private static final String GET_TOTAL_AVAILABLE_POINTS = "SELECT sum(points_earned - points_redeemed - open_points_redeemed) from reward_points_status where mr_no = ? ";
	
	public BigDecimal getTotalPointsAvailable(String mrNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_TOTAL_AVAILABLE_POINTS);
		ps.setString(1, mrNo);
		return DataBaseUtil.getBigDecimalValueFromDb(ps);
			
		}finally{
			DataBaseUtil.closeConnections(con, ps);
			
		}
	}
}
