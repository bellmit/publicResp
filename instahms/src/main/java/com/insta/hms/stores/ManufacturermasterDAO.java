package com.insta.hms.stores;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

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

public class ManufacturermasterDAO extends GenericDAO{



	static Logger logger = LoggerFactory.getLogger(ManufacturermasterDAO.class);

	Connection con=null;


	public ManufacturermasterDAO() {
		super("manf_master");
	}

	public static String GET_MANUFACTURER_DETAILS="SELECT manf_code,manf_name,manf_mnemonic,"
			    +" case when status='A' then 'Active' else 'Inactive' end as manfstatus FROM manf_master ";

	public static String GET_STATE_NAMES="SELECT  * FROM state_master ORDER BY state_name";

	public static String GET_COUNTRY_NAMES="SELECT  * FROM country_master ORDER BY country_name";

	public static String GET_CITY_NAMES="SELECT city_id,city_name,state_id FROM city ORDER BY city_name";

	public static String GET_STATE_CITY_XMLCONTENT_DETAILS="SELECT city_id,city_name,state_id FROM city ORDER BY city_name";

	public static String GET_MANFACTURER_XMLCONTENT_DETAILS="SELECT * FROM manf_master ORDER BY manf_name";

	public static String GET_UPDATED_MANUFACTURER_DETAILS="UPDATE manf_master SET manf_name=?,manf_address=?,manf_city=?,manf_state=?,manf_country=?,"
		                 +" manf_pin=?,manf_phone1=?,manf_phone2=?,manf_fax=?,manf_mailid=?,manf_website=?,status=?,manf_mnemonic=?,pharmacy=?,inventory=? WHERE manf_code=?";

	public static String GET_INSERTED_MANUFACTURER_DETAILS="INSERT INTO manf_master(manf_code,manf_name,manf_address,manf_city,manf_state,manf_country,"
		                 +" manf_pin,manf_phone1,manf_phone2,manf_fax,manf_mailid,manf_website,status,manf_mnemonic,pharmacy,inventory) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public static String GET_DEACTIVATE_MANUFACTURER="UPDATE manf_master SET status='I' WHERE manf_code=?";



	public ArrayList getStateMasterDetails() throws SQLException {

		PreparedStatement ps = null;
		con = DataBaseUtil.getConnection();
		ps = con.prepareStatement(GET_STATE_NAMES);
		ArrayList stateMasterLists = DataBaseUtil.queryToArrayList(ps);
		ps.close();
		return stateMasterLists;
	}

	public ArrayList getCountryMasterDetails() throws SQLException {

		PreparedStatement ps = null;
		con = DataBaseUtil.getConnection();
		ps = con.prepareStatement(GET_COUNTRY_NAMES);
		ArrayList countryMasterLists = DataBaseUtil.queryToArrayList(ps);
		ps.close();
		return countryMasterLists;
	}

	public ArrayList getCityMasterDetails() throws SQLException {

		PreparedStatement ps = null;
		con = DataBaseUtil.getConnection();
		ps = con.prepareStatement(GET_CITY_NAMES);
		ArrayList cityMasterLists = DataBaseUtil.queryToArrayList(ps);
		ps.close();
		return cityMasterLists;
	}


	public boolean modifyManufacturerDetails(ManufacturerDTO mdto) throws SQLException {

		PreparedStatement ps = null;
		con = DataBaseUtil.getConnection();
		boolean target=false;
		ps = con.prepareStatement(GET_UPDATED_MANUFACTURER_DETAILS);
		ps.setString(1,mdto.getName());
		ps.setString(2,mdto.getAdress());
		ps.setString(3, mdto.getCity());
		ps.setString(4, mdto.getState());
		ps.setString(5, mdto.getCountry());
		ps.setString(6, mdto.getPinNumber());
		ps.setString(7, mdto.getPhoneNumber1());
		ps.setString(8, mdto.getPhoneNumber2());
		ps.setString(9, mdto.getFaxNumber());
		ps.setString(10,mdto.getMailId());
		ps.setString(11, mdto.getWebsite());
		ps.setString(12, mdto.getDactivate());
		ps.setString(13, mdto.getManfuMnemonic());
		ps.setBoolean(14, mdto.isPharmacy());
		ps.setBoolean(15, mdto.isInventory());
		ps.setString(16, mdto.getManufacturerCode());

		int resultCount=ps.executeUpdate();
		if(resultCount>0)
			target=true;
		ps.close();
		return target;
	}

	public boolean manufacturerDetailsInsert(ManufacturerDTO mdto) throws SQLException {

		PreparedStatement ps = null;
		boolean target=false;
		String manfacturerID=AutoIncrementId.getSequenceId("manufacturer_id_seq","Manufacturer");
		con = DataBaseUtil.getConnection();
		ps = con.prepareStatement(GET_INSERTED_MANUFACTURER_DETAILS);
		ps.setString(1,manfacturerID);
		ps.setString(2,mdto.getName());
		ps.setString(3,mdto.getAdress());
		ps.setString(4, mdto.getCity());
		ps.setString(5, mdto.getState());
		ps.setString(6, mdto.getCountry());
		ps.setString(7, mdto.getPinNumber());
		ps.setString(8, mdto.getPhoneNumber1());
		ps.setString(9, mdto.getPhoneNumber2());
		ps.setString(10, mdto.getFaxNumber());
		ps.setString(11,mdto.getMailId());
		ps.setString(12, mdto.getWebsite());
		ps.setString(13,mdto.getDactivate());
		ps.setString(14, mdto.getManfuMnemonic());
		ps.setBoolean(15, mdto.isPharmacy());
		ps.setBoolean(16, mdto.isInventory());

		int resultCount=ps.executeUpdate();
		if(resultCount>0)
			target=true;
		ps.close();
		return target;

	}

	private static String GET_ALL_MANUFACTURER_NAMES = "SELECT manf_code,manf_name FROM manf_master";

	public static List getAllManufacturerNames() throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_ALL_MANUFACTURER_NAMES);
	}

	public static ArrayList getAllManfs() throws SQLException {
		Connection con = null; PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_MANUFACTURER_NAMES);
			return DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String MANF_EXT_QUERY_FIELDS = " SELECT *";

	private static final String MANF_EXT_QUERY_COUNT =
		" SELECT count(MANF_CODE) ";

	private static final String MANF_EXT_QUERY_TABLES =
		  " FROM MANF_MASTER";

	public static PagedList searchManufacturers(Map filter, Map listing)
		throws SQLException, ParseException {

		Connection con = DataBaseUtil.getReadOnlyConnection();

		SearchQueryBuilder qb = new SearchQueryBuilder(con,
				MANF_EXT_QUERY_FIELDS, MANF_EXT_QUERY_COUNT, MANF_EXT_QUERY_TABLES, listing);

		qb.addFilterFromParamMap(filter);
		qb.addSecondarySort("manf_code");
		qb.build();

		PagedList l = qb.getMappedPagedList();

		qb.close();
		con.close();

		return l;
	}

	public static String GET_MANF_DETAILS="SELECT * from manf_master where manf_code=?";


	public static BasicDynaBean getSelectedManfDetails (String manfId) throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_MANF_DETAILS);
			ps.setString(1, manfId);
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

	private static final String MANUFACTURERS_NAMESAND_iDS="SELECT manf_name,manf_code FROM  manf_master";


    public static List getManufacturersNamesAndIds() throws SQLException{

	  return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(MANUFACTURERS_NAMESAND_iDS));
}

    public static HashMap getManufacturerDetails() throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;
		HashMap <String,String>manufacturerHasMap = new HashMap <String, String>();
		ResultSet rs=null;
		con=DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(MANUFACTURERS_NAMESAND_iDS);
		rs=ps.executeQuery();
		while(rs.next()){
			manufacturerHasMap.put(rs.getString("manf_name"), rs.getString("manf_code"));
		}
		DataBaseUtil.closeConnections(con, ps,rs);
		return manufacturerHasMap;
	}


}