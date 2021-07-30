package com.insta.hms.master.AreaMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.common.annotations.MigratedTo;
import com.insta.hms.mdm.areas.AreaRepository;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AreaMasterDAO extends GenericDAO {

	public AreaMasterDAO() {
		super("area_master");

	}

	@MigratedTo(value = AreaRepository.class, method = "getNextId")
	public String getNextAreaId() throws SQLException {
		String areaId = null;
		areaId = getNextFormattedId();
		return areaId;
	}

	public List getAreaList(boolean mappedDynaList) throws SQLException{
		List areaList = listAll(null, "status", "A");
		if (mappedDynaList)
			return ConversionUtils.copyListDynaBeansToMap(areaList);
		else
			return areaList;
	}

	private static String AREA_FIELDS = " SELECT *  ";

	private static String AREA_COUNT = " SELECT count(*) ";

	private static String AREA_TABLES = " FROM (SELECT area_id,area_name,city_name,am.city_id,am.status FROM area_master am "
			+ " JOIN city USING (city_id) ) as foo ";

	public PagedList getAreaDetails(Map map, Map pagingParams)
		throws Exception, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, AREA_FIELDS,
					AREA_COUNT, AREA_TABLES, pagingParams);

			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("area_id", false);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static final String GET_ALL_AREA = " SELECT area_id,area_name,city_id FROM area_master ";

	public static List getAvlAreas() {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList areaList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_AREA);
			areaList = DataBaseUtil.queryToArrayList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return areaList;
	}

	private String GET_AREA_FOR_CITY = "SELECT * FROM area_master WHERE city_id = ? AND area_name = ?";

	public BasicDynaBean getAreaByCity(String cityid, String area)
			throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_AREA_FOR_CITY);
			ps.setObject(1, cityid);
			ps.setObject(2, area);
			List list = DataBaseUtil.queryToDynaList(ps);
			if (list.size() > 0) {
				return (BasicDynaBean) list.get(0);
			} else {
				return null;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	private static final String patientAreaList = "SELECT city_id AS patient_city,area_name AS patient_area," +
			" status FROM area_master ";

	public static List getPatientAreaList() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		con = DataBaseUtil.getConnection();
		ArrayList areaList = null;

		ps = con.prepareStatement(patientAreaList,
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		try {
			areaList = DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return areaList;
	}
	
	private static final String GET_ALL_AREA_CITY_STATE_COUNTRY_LIST =  " select area_id, city_id, state_id, country_id, area_name,city_name, state_name ,country_name from area_master am join city using(city_id) "+
																		" join state_master sm using(state_id) "+
																		" join country_master cm using(country_id) where area_id=? and am.status = 'A'";

	public static BasicDynaBean getAreaCityStateCountryList(String areaId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List areaList = null;
		BasicDynaBean bean = null;
		
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_AREA_CITY_STATE_COUNTRY_LIST);
			ps.setString(1, areaId);
			areaList = DataBaseUtil.queryToDynaList(ps);
			if(!areaList.isEmpty()) {
				bean = (BasicDynaBean) areaList.get(0);
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return bean;
	}

	private static String FIND_AREA = " SELECT count(*) from area_master WHERE city_id=? and area_name=? ";

	private static String INSERT_AREA = " INSERT INTO area_master (area_id,area_name,city_id) VALUES (?,?,?) ";

	public boolean checkAreaAndInsert(String cityid, String areaName)
			throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		PreparedStatement ps1 = null;
		ResultSet rs = null;
		int count = 0;
		boolean success = true;

		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(FIND_AREA);
			ps.setString(1, cityid);
			ps.setString(2, areaName);
			rs = ps.executeQuery();

			while (rs.next()) {
				count = rs.getInt(1);
			}
			if (count == 0) {
				GenericDAO gdao = new GenericDAO("area_master");
				String areaId = gdao.getNextFormattedId();

				ps1 = con.prepareStatement(INSERT_AREA);
				ps1.setString(1, areaId);
				ps1.setString(2, areaName);
				ps1.setString(3, cityid);

				success = ps1.executeUpdate() > 0;
			}
		} finally {
			if (ps1 != null) ps1.close();
			DataBaseUtil.closeConnections(con, ps, rs);
		}

		return success;
	}


	private static String AREA_NAME_EXIST =
		"SELECT area_name FROM area_master where city_id=? and upper(area_name)=upper(?)";
	public boolean exist(String cityId, String areaName) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = con.prepareStatement(AREA_NAME_EXIST);
			ps.setString(1, cityId);
			ps.setString(2, areaName);
			rs = ps.executeQuery();
			if (rs.next()) {
				return true;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return false;
	}

	private static final String patientareaall = "SELECT area_name,area_id FROM area_master WHERE status = 'A'" ;

	public static List getPatientAreaAll(boolean mappedList)
			throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(patientareaall);
			if (mappedList) {
				List areaAllList = DataBaseUtil.queryToDynaList(ps);
				return areaAllList == null ? null : ConversionUtils
						.copyListDynaBeansToMap(areaAllList);
			} else {
				return DataBaseUtil.queryToArrayList(ps);
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String FIND_AREAS = "SELECT * FROM area_master " +
			"	WHERE city_id=? AND (area_name ilike ? or area_name ilike ?) AND status='A'";
	public static List findAreasForCity(String areaName, String cityId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(FIND_AREAS);
			ps.setString(1, cityId);
			ps.setString(2, areaName+"%");
			ps.setString(3, "% "+areaName+"%");
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public static final String getAreaList = "SELECT AREA_ID,AREA_NAME, CITY_ID, STATUS FROM AREA_MASTER WHERE CITY_ID=? ORDER BY AREA_NAME ASC";

	public static ArrayList getAreasList(String stateId) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ArrayList stateList = null;
        try{
            con = DataBaseUtil.getConnection();
            ps = con.prepareStatement(getAreaList);
            ps.setString(1,stateId);
            stateList = DataBaseUtil.queryToArrayList(ps);
        }finally{
        	DataBaseUtil.closeConnections(con, ps);
        }
        return stateList ;
    }
	
	private static final String AREA_CITY_STATE_COUNTRY_LIST = " SELECT  a.area_id, a.area_name,c.city_name, " + 
	    " c.city_id, dm.district_id, dm.district_name, s.state_name," +
			" s.state_id,t.country_name,t.country_id, " + 
	    " c.city_name ||' - '||s.state_name||' - '||t.country_name AS city_state_country_name, " +
			" a.area_name||'-'||c.city_name ||' - '||s.state_name||' - '||t.country_name AS area_city_state_country_name, " +
			
      " c.city_name ||dm.district_name||CASE WHEN dm.district_name IS NULL THEN '' ELSE ' - ' END " + 
      " ||s.state_name||' - '||t.country_name AS city_district_state_country_name, " +
      " a.area_name||'-'||c.city_name ||' - '||" +
      " COALESCE(dm.district_name,'')||CASE WHEN dm.district_name IS NULL THEN '' ELSE ' - ' END||s.state_name||' - '||t.country_name " +
      " AS area_city_district_state_country_name " +
			
			" FROM area_master a " +
			" JOIN city c ON (a.city_id=c.city_id) " +
			" JOIN state_master s ON (s.state_id=c.state_id) " +
			" LEFT JOIN district_master dm on (dm.district_id = c.district_id AND dm.state_id = s.state_id) " +
			" JOIN country_master t ON (t.country_id=s.country_id) WHERE c.status = 'A' and a.status='A' ";

	public static List getPatientAreaList(boolean mappedList)
			throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(AREA_CITY_STATE_COUNTRY_LIST);
			if (mappedList) {
				List areaList = DataBaseUtil.queryToDynaList(ps);
				return areaList == null ? null : ConversionUtils.copyListDynaBeansToMap(areaList);
			} else {
				return DataBaseUtil.queryToArrayList(ps);
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
}
