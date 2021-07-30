/**
 *
 */
package com.insta.hms.master.CenterMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.common.annotations.MigratedTo;
import com.insta.hms.common.utils.JsonUtility;
import com.insta.hms.master.CountryMaster.CountryMasterDAO;
import com.insta.hms.mdm.centers.CenterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author krishna
 *
 */
@MigratedTo(CenterRepository.class)
// Removed BasicCachingDAO because cached are not refreshing after saving screen from spring  
public class CenterMasterDAO extends GenericDAO {
	private static CountryMasterDAO countryMasterDao = new CountryMasterDAO();
  static Logger logger = LoggerFactory.getLogger(CenterMasterDAO.class);
	
	/*
	 * centerId with value 0(default center) inserted manually.
	 */
	public CenterMasterDAO() {
		super("hospital_center_master");
	}
	
	private static final String CENTERS_FILEDS = "SELECT * ";
	private static final String CENTERS_FROM = " FROM hospital_center_master ";
	private static final String COUNT = " SELECT count(center_id) ";
	public PagedList searchCenters(Map params, Map<LISTING, Object> listingParams) throws SQLException,
		ParseException {
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		try {
			qb = new SearchQueryBuilder(con, CENTERS_FILEDS, COUNT, CENTERS_FROM, listingParams);
			qb.addFilterFromParamMap(params);
			qb.build();

			return qb.getDynaPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}

	public boolean exists(int centerId, String centerName) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT center_name FROM hospital_center_master " +
					"	where center_id!=? and upper(trim(center_name))=upper(trim(?))");
			ps.setInt(1, centerId);
			ps.setString(2, centerName);
			rs = ps.executeQuery();
			if (rs.next()) return true;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return false;
	}

	public static List<BasicDynaBean> getAllCentersAndSuperCenterAsFirst() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT * FROM hospital_center_master WHERE center_id=0 AND status='A' " +
					"	UNION ALL (SELECT * FROM hospital_center_master WHERE center_id!=0 AND status='A' ORDER BY center_name)");
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	public static List<BasicDynaBean> getAllCentersExceptSuper() throws SQLException {
		return DataBaseUtil.queryToDynaList("SELECT * FROM hospital_center_master WHERE status='A' and center_id!=0 order by center_name");
	}
	public static List<BasicDynaBean> getAllCenters() throws SQLException {
		return DataBaseUtil.queryToDynaList("SELECT * FROM hospital_center_master WHERE status='A' order by center_name");
	}

	public static List<BasicDynaBean> getCentersWithoutCompName() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT * FROM hospital_center_master where coalesce(accounting_company_name,'') = '' ");
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static List<BasicDynaBean> getCentersWithoutServiceReg() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT * FROM hospital_center_master where coalesce(hospital_center_service_reg_no,'') = '' ");
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static BasicDynaBean getUserCenterDetails(String userName) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT hcm.center_id,hcm.center_name FROM hospital_center_master hcm " +
					" LEFT JOIN u_user uu ON (uu.center_id = hcm.center_id) " +
					" WHERE emp_username = ?");
			ps.setString(1, userName);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static List getCentersList() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT city_name, state_name, center_name, s.state_id, c.city_id, center_id, hcm.status " +
					"	FROM hospital_center_master hcm " +
					"		LEFT JOIN city c ON (c.city_id=hcm.city_id)" +
					"		LEFT JOIN state_master s ON (c.state_id=s.state_id) WHERE hcm.center_id != 0 ORDER BY center_name");
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_CENTERS =
		"SELECT center_id,center_name from hospital_center_master order by center_id";
	public static Map getAllCentersMap() throws SQLException {
		Map<String, String> centerMap = new HashMap<String, String>();
		PreparedStatement ps = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_CENTERS);
			rs = ps.executeQuery();
			while(rs.next()) {
				centerMap.put(rs.getString(2), rs.getString(1));
			}

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return centerMap;
	}

	private static final String GET_HEALTH_AUTHORITY =
		"SELECT health_authority from hospital_center_master WHERE center_id = ? order by center_id";

	public static String getHealthAuthorityForCenter(Integer centerId) throws SQLException {
		PreparedStatement ps = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_HEALTH_AUTHORITY);
			ps.setInt(1, (null == centerId ? -1 : centerId));
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static List<BasicDynaBean> getCentersId() throws SQLException {
		return DataBaseUtil.queryToDynaList("SELECT center_id FROM hospital_center_master WHERE status='A' and center_id != 0");
	}

	public static List<BasicDynaBean> getAllCentersDetails() throws SQLException {
		List l = null;
		Connection con = null;
		PreparedStatement ps = null;
		//String userName = RequestContext.getUserName();
		int centerId = RequestContext.getCenterId();
		try {
			con = DataBaseUtil.getConnection();
			if(centerId != 0){
				ps = con.prepareStatement("SELECT * FROM hospital_center_master where  center_id = ? and center_id != 0");
				ps.setInt(1, centerId);
		
			}else {
				ps = con.prepareStatement("SELECT * FROM hospital_center_master WHERE status='A' order by center_name");
			
			}
			l = DataBaseUtil.queryToDynaList(ps);

			return l;
		}
		finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	
	public static boolean isCenterExists(String centerName) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT center_name FROM hospital_center_master WHERE upper(trim(center_name))=upper(trim(?))");
			ps.setString(1, centerName);
			rs = ps.executeQuery();
			if (rs.next()) return true;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return false;
	}
	/*
	 * Get country_code for given center
	 */
	public String getCountryCode(int centerId){
		try{
			String countryId= (String)this.findByKey("center_id", centerId).get("country_id");
			return countryMasterDao.getCountryCode(countryId);			
		}
		catch(SQLException ex){
			logger.info("Unable to fetch country code of center with centerId:" + centerId);
			return null;
			
		}
		
	}
	
	public BasicDynaBean getCountryBean(int centerId){
		
		try {			
			String countryId = (String)this.findByKey("center_id", centerId).get("country_id");
			return countryMasterDao.findByKey("country_id",countryId);
		} catch (SQLException e) {
			logger.info("Unable to fetch countryBean for centerId " + centerId);
			return null;
		}
		
	}
	
	public static final String GET_CENTERS_FOR_GROUP = "SELECT hcm.center_id, center_name "
			+ " FROM center_group_details cgd"
			+ " JOIN hospital_center_master hcm ON(hcm.center_id = cgd.center_id AND hcm.status = 'A')"
			+ " WHERE cgd.center_group_id = ? AND cgd.status = 'A'";
	
	public static List<String> getCentersForGroup(int groupID)throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		List<String> centerNameList = new ArrayList<String>();
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(GET_CENTERS_FOR_GROUP);
			pstmt.setInt(1, groupID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				centerNameList.add(rs.getString("center_name"));
			}
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
		return centerNameList;
	}
	
  public Map<String,Object> getReportingMeta(Integer centerId) {
    try {
		return JsonUtility.toObjectMap((String) findByKey("center_id", centerId).get("reporting_meta"));
	} catch (SQLException e) {
		return new HashMap<String,Object>();
	}
  }

}
