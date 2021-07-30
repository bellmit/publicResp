package com.insta.hms.master.Dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;



public class DialAccessTypesDAO extends GenericDAO {

	public DialAccessTypesDAO() {

		super("dialysis_access_types");
	}

	private static final String GET_ALL_ATYPES = "SELECT access_type_id, access_type FROM dialysis_access_types";

	public List getAvalDialAccessTypes() {

		Connection con = null;
		PreparedStatement ps = null;
		ArrayList dialATypes = null;

		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_ATYPES);
			dialATypes = DataBaseUtil.queryToArrayList(ps);
		}catch(SQLException e){
			Logger.log(e);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

		return dialATypes;
	}
	
	private static final String GET_ALL_ACTIVE_ATYPES = " SELECT * FROM dialysis_access_types WHERE status='A' AND access_mode='P' ORDER BY access_type_id"; 

	public static List<BasicDynaBean> getActiveAvalDialAccessTypes() { 

	Connection con = null; 
	PreparedStatement ps = null; 
	List<BasicDynaBean> dialActiveATypes = null; 
	 try { 
		con = DataBaseUtil.getConnection(); 
		ps = con.prepareStatement(GET_ALL_ACTIVE_ATYPES); 
		dialActiveATypes = DataBaseUtil.queryToDynaList(ps); 
	} catch(SQLException e) { 
		Logger.log(e); 
	}finally{ 
		DataBaseUtil.closeConnections(con, ps); 
	} 
	return dialActiveATypes; 
	}
}