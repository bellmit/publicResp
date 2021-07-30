package com.insta.hms.master.Dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DialysisMachineMasterDAO extends GenericDAO {

	public DialysisMachineMasterDAO() {

		super("dialysis_machine_master");
	}


	private static String DIAL_MACHINE_MFIELDS = "select * ";

	private static String DIAL_MACHINE_MCOUNT = "SELECT count(*) ";

	private static String DIAL_MACHINE_MTABLE =" FROM (SELECT center_id, machine_id, machine_name, machine_type, dmm.status," +
		"dmm.location_id, network_address, network_port, substring(dmm.remarks,0,20) AS remarks, location_name " +
		"from dialysis_machine_master dmm LEFT JOIN location_master USING (location_id)) AS foo";

	public PagedList getDialMachineMasterList(Map map, Map pagingParams)
		throws SQLException, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb = new SearchQueryBuilder(con, DIAL_MACHINE_MFIELDS, DIAL_MACHINE_MCOUNT,
						DIAL_MACHINE_MTABLE, pagingParams);
			qb.addFilterFromParamMap(map);
			int centerId = RequestContext.getCenterId();
			if (centerId != 0)
				qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
			qb.build();
			PagedList l= qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String GET_ALL_MNAMES = "SELECT machine_id, machine_name FROM dialysis_machine_master";

	public List getAvalMachineNames() {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList machNames = null;

		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_MNAMES);
			machNames = DataBaseUtil.queryToArrayList(ps);
		}catch(SQLException e){
			Logger.log(e);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

		return machNames;
	}

	public static List getMachines(int centerId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT machine_id, machine_name FROM dialysis_machine_master dm " +
					"	JOIN location_master lm ON (dm.location_id=lm.location_id) where (lm.center_id=? or ?=0) AND dm.status='A' AND lm.status='A'");
			ps.setInt(1, centerId);
			ps.setInt(2, centerId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	private static final String GET_NETWORK_ADDRESS = "SELECT network_address FROM dialysis_machine_master " +
		"WHERE network_address = ? AND network_port = ? AND status IN ('A','T')";

	public boolean isIpAddressSame(String netAddress, String netPort)throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_NETWORK_ADDRESS);
			ps.setString(1, netAddress);
			ps.setString(2, netPort);
			rs = ps.executeQuery();
			if(rs.next()) {
				return true;
			} else {
				return false;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}

	}

	private static final String NOT_ASSIGNED_MACHINES = "SELECT dms.assigned_status, dms.assigned_order_id, " +
		" dm.machine_id, machine_name FROM dialysis_machine_master dm " +
		" JOIN location_master lm ON (dm.location_id=lm.location_id) " +
		" LEFT JOIN dialysis_machine_status dms ON (dm.machine_id = dms.machine_id) " +
		" where (lm.center_id=? or ?=0) AND dm.status='A' AND lm.status='A'" ;

	public static List<BasicDynaBean> getNotAssignedMachines(int visitCenter, String orderId)throws SQLException {

		List<BasicDynaBean> filteredList = new ArrayList<BasicDynaBean>();
		Connection con = null;
		PreparedStatement pstmt = null;
		int orderidint = -1;

		if (orderId != null && !orderId.equals("")) {

			orderidint = Integer.parseInt(orderId);
		}

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(NOT_ASSIGNED_MACHINES);
			pstmt.setInt(1, visitCenter);
			pstmt.setInt(2, visitCenter);
			List<BasicDynaBean> dynaList = DataBaseUtil.queryToDynaList(pstmt);

			for (int i=0; i<dynaList.size(); i++) {
				BasicDynaBean bean = dynaList.get(i);
				if (bean.get("assigned_status") == null || !((String)bean.get("assigned_status")).equals("A") ||
						(((String)bean.get("assigned_status")).equals("A") && bean.get("assigned_order_id").equals(orderidint))) {
					filteredList.add(bean);
				}
			}

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}

		return filteredList;
	}

	public static String getMachineCount() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(" SELECT count(*) FROM dialysis_machine_master where status='A' ");
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

	}
}
