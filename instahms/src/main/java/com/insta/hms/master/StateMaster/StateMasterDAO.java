package com.insta.hms.master.StateMaster;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BasicCachingDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class StateMasterDAO extends BasicCachingDAO {

	public StateMasterDAO() {
		super("state_master");
	}

	public String getNextStateId() throws SQLException {
		String stateNewId = null;
		stateNewId =	AutoIncrementId.getNewIncrId("STATE_ID", "STATE_MASTER", "STATEMASTER");

		return stateNewId;
	}
	private static String STATE_FIELDS = "SELECT * " ;

	private static String STATE_COUNT = "SELECT count(*)";

	private static String STATE_TABLES = " FROM (SELECT s.state_id,s.state_name,s.status,c.country_id,c.country_name FROM " +
			"	state_master s LEFT OUTER JOIN country_master c ON(c.country_id=s.country_id)) AS foo";

	public PagedList getStateDetails(Map map,Map pagingParams)throws SQLException, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb = new SearchQueryBuilder(con,STATE_FIELDS,STATE_COUNT ,
						 STATE_TABLES,pagingParams);

			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("state_name", false);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static final String getStateIdName = "SELECT STATE_ID,STATE_NAME FROM STATE_MASTER ORDER BY STATE_NAME ASC";

	public static ArrayList getStateIdName() throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ArrayList stateIdName = null;
        try{
            con = DataBaseUtil.getConnection();
            ps = con.prepareStatement(getStateIdName);
            stateIdName = DataBaseUtil.queryToArrayList(ps);
        }finally{
        	DataBaseUtil.closeConnections(con, ps);
        }
        return stateIdName ;
    }

	public  List getStateList(boolean mappedList) throws SQLException {
		List stateList = listAll(null, "status", "A");
		if (mappedList)
			return ConversionUtils.copyListDynaBeansToMap(stateList);
		else
			return stateList;
	}
	
	public static final String getStatesList = "SELECT STATE_ID,STATE_NAME,COUNTRY_ID,STATUS FROM STATE_MASTER WHERE COUNTRY_ID=? ORDER BY STATE_NAME ASC";

	public static ArrayList getStateList(String countryId) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ArrayList stateList = null;
        try{
            con = DataBaseUtil.getConnection();
            ps = con.prepareStatement(getStatesList);
            ps.setString(1,countryId);
            stateList = DataBaseUtil.queryToArrayList(ps);
        }finally{
        	DataBaseUtil.closeConnections(con, ps);
        }
        return stateList ;
    }
	public static final String STATE_COUNTRY_LIST = " SELECT s.state_name, s.state_id,t.country_name,t.country_id FROM  state_master s   JOIN country_master t ON (t.country_id=s.country_id) WHERE s.status = 'A' ";
	public static  List getStateCountryList() throws SQLException {
		Connection con = null;
        PreparedStatement ps = null;
        ArrayList stateCountryList = null;
        try{
            con = DataBaseUtil.getConnection();
            ps = con.prepareStatement(STATE_COUNTRY_LIST);
            stateCountryList = DataBaseUtil.queryToArrayList(ps);
        }finally{
        	DataBaseUtil.closeConnections(con, ps);
        }
        return stateCountryList;
	}
}