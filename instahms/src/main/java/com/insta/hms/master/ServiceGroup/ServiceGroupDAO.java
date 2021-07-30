package com.insta.hms.master.ServiceGroup;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ServiceGroupDAO extends GenericDAO {

	public ServiceGroupDAO() {
		super("service_groups");
	}

	public static final String GET_ALL_SERVICE_GROUPS = " SELECT service_group_id,service_group_name, " +
			" display_order,status,username,to_char(mod_time,'dd-mm-yyyy hh:mm'),service_group_code FROM service_groups";

	public static List getAllServiceGroups() throws SQLException{
        List l =null;
        Connection con = null;
        PreparedStatement ps = null;
        try {
        	con = DataBaseUtil.getReadOnlyConnection();
        	ps = con.prepareStatement(GET_ALL_SERVICE_GROUPS);
       	 	l = DataBaseUtil.queryToArrayList(ps);
        } finally {
            DataBaseUtil.closeConnections(con, ps);
        }

        return l;
    }

	private static final String GET_SERVICE_GROUPS = " SELECT service_group_id, service_group_name," +
			" status, display_order, service_group_code FROM service_groups" +
			" WHERE status = 'A'";

	public static List<BasicDynaBean> getServiceGroupsDetails()throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(GET_SERVICE_GROUPS);
			return DataBaseUtil.queryToDynaList(pstmt);
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

}