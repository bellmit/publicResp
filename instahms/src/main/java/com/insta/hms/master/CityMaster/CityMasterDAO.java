package com.insta.hms.master.CityMaster;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.common.annotations.MigratedTo;
import com.insta.hms.mdm.cities.CityRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityMasterDAO extends GenericDAO{
	Connection con = null;


	public CityMasterDAO(){
		super("city");
	}

	@MigratedTo(value = CityRepository.class, method = "getNextId")
	public String getNextCityId() throws SQLException {

		String cityId = null;
		cityId = AutoIncrementId.getNewIncrUniqueId("CITY_ID","CITY", "CITY ID");

		return cityId;
	}


	private static String CITY_FIELDS = "SELECT * " ;

	private static String CITY_COUNT = "SELECT count(*)";

	private static String CITY_TABLES = " FROM (SELECT c.city_id,c.city_name,c.status as citystatus ,c.state_id,s.state_name FROM " +
			"	city c LEFT OUTER JOIN state_master s ON(c.state_id=s.state_id)) AS foo";

	public PagedList getCityDetailPages(Map map,Map pagingParams)throws SQLException, ParseException {

		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb =
				new SearchQueryBuilder(con,CITY_FIELDS,CITY_COUNT,
						CITY_TABLES,pagingParams);

			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("city_name", false);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static final String GET_ALL_CITYS = "SELECT city_id,city_name,state_id FROM city " ;

	public static List getAvalCitynames() {
		PreparedStatement ps = null;
		ArrayList cityNames = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_CITYS);
			cityNames = DataBaseUtil.queryToArrayList(ps);
		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return cityNames;
	}

    private static final String patientCityList = "SELECT city_name, city_id, state_id FROM city WHERE status = 'A' ";

	public static List getPatientCityList(boolean mappedList) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(patientCityList);
			if (mappedList) {
				List cityList = DataBaseUtil.queryToDynaList(ps);
				return cityList == null ? null : ConversionUtils.copyListDynaBeansToMap(cityList);
			} else {
				return DataBaseUtil.queryToArrayList(ps);
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
private static final String cityMap = "SELECT city_name, city_id FROM city WHERE status = 'A' ";
	
	public static Map<String, String> getCityMap() throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		Map<String, String> cityIdMap = new HashMap<String, String>(); 
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(cityMap);
			rs =ps.executeQuery();
			while(rs.next()){
				cityIdMap.put(rs.getString("city_id"), rs.getString("city_name"));
			}
			return cityIdMap;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}
	
	
	private static final String CITY_STATE_COUNTRY_LIST = " SELECT c.city_name,c.city_id, dm.district_id, dm.district_name, " +
			" s.state_name, s.state_id,t.country_name,t.country_id, c.city_name ||' - '||s.state_name||' - '||t.country_name AS city_state_country_name, " +
	    " c.city_name || ' - ' ||COALESCE(dm.district_name,'')||CASE WHEN dm.district_name IS NULL THEN '' ELSE ' - ' END " + 
			" ||s.state_name||' - '||t.country_name AS city_district_state_country_name " +
			" FROM city c " +
			" JOIN state_master s ON (s.state_id=c.state_id) " +
			" LEFT JOIN district_master dm on (dm.district_id = c.district_id AND dm.state_id = s.state_id) " +
			" JOIN country_master t ON (t.country_id=s.country_id) WHERE c.status = 'A'";

	/* 
	 * Migrated method is not having argument. Because queryToArrayList is deprecated and also not required to 
	 * handle condition which is handling now.
	 */
	
	@MigratedTo(value = CityRepository.class, method = "getPatientCityStateCountryList")
	public static List getPatientCityStateCountryList(boolean mappedList)
			throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(CITY_STATE_COUNTRY_LIST);
			if (mappedList) {
				List cityStateCountryList = DataBaseUtil.queryToDynaList(ps);
				return cityStateCountryList == null ? null : ConversionUtils.copyListDynaBeansToMap(cityStateCountryList);
			} else {
				return DataBaseUtil.queryToArrayList(ps);
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	
	
	
	public static final String getCityList = "SELECT CITY_ID,CITY_NAME, STATE_ID, STATUS FROM CITY WHERE STATE_ID=? ORDER BY CITY_NAME ASC";

	public static ArrayList getCitiesList(String stateId) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ArrayList stateList = null;
        try{
            con = DataBaseUtil.getConnection();
            ps = con.prepareStatement(getCityList);
            ps.setString(1,stateId);
            stateList = DataBaseUtil.queryToArrayList(ps);
        }finally{
        	DataBaseUtil.closeConnections(con, ps);
        }
        return stateList ;
    }
}
