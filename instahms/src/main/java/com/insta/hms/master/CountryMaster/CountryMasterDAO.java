package com.insta.hms.master.CountryMaster;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

//Removed BasicCachingDAO because cached are not refrishing after saving screen from spring
public class CountryMasterDAO extends GenericDAO {
	static Logger logger = LoggerFactory.getLogger(CountryMasterDAO.class);

	public CountryMasterDAO() {
		super("country_master");
	}

	public String getNextId() throws SQLException {
		String countryNewId = null;
		countryNewId = AutoIncrementId.getNewIncrId("COUNTRY_ID",
				"COUNTRY_MASTER", "COUNTRYMASTER");
		return countryNewId;
	}

	private static final String getCountries = "SELECT * FROM country_master WHERE status = 'A'";
	public static List getCountryList(boolean dynaMappedList) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		con = DataBaseUtil.getConnection();
		ps = con.prepareStatement(getCountries);
		try {
			if (!dynaMappedList) {
				return DataBaseUtil.queryToArrayList(ps);
			} else {
				List countryList = DataBaseUtil.queryToDynaList(ps);
				return countryList == null ? null : ConversionUtils.copyListDynaBeansToMap(countryList);
			}
		}
		finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	public String getCountryCode(String  countryId) throws SQLException{
		if(countryId == null || countryId.isEmpty()){
			logger.info("countryId cannot be empty ");
			throw new SQLException();
		}
		//find the country_code from country_id
		String countryCode=(String)this.
				findByKey("country_id", countryId).get("country_code");
		
		if(countryCode == null || countryCode.isEmpty() ){
			logger.info("Country code cannot be empty ");
			throw new SQLException();
		}
		return countryCode;
		
	}


}
