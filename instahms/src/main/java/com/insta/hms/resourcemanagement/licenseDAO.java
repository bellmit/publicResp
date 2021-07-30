package com.insta.hms.resourcemanagement;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class licenseDAO extends GenericDAO {

	public licenseDAO() {
		super("licenses");
	}


	private static String LICENSE_FIELDS = "select l.license_id,l.license_desc,l.license_type_id,l.license_start_date,l.license_end_date," +
			" l.license_renewal_date,l.license_value,l.license_note,lt.license_type,l.license_status" ;

	private static String LICENSE_COUNT = "SELECT count(*)";

	private static String LICENSE_TABLES = " from licenses l JOIN license_type_master lt on (l.license_type_id=lt.license_type_id)  ";

	public PagedList getlicenseDetails(Map<LISTING, Object> pagingParams,ArrayList<String> licenseTypeList,
			Date RenewalFromDate,Date RenewalToDate,Date expiryFromDate,Date expiryToDate, List status)throws SQLException {

		int pageNum = (Integer)pagingParams.get(LISTING.PAGENUM);
		Connection con = DataBaseUtil.getReadOnlyConnection();
		SearchQueryBuilder qb =
			new SearchQueryBuilder(con,LICENSE_FIELDS,LICENSE_COUNT,
					LICENSE_TABLES,null,null,"l.license_desc",false,25,pageNum);
		qb.addFilter(SearchQueryBuilder.STRING,"lt.license_type","IN",licenseTypeList);
		qb.addFilter(qb.DATE, "l.license_renewal_date", ">=", RenewalFromDate);
		qb.addFilter(qb.DATE, "l.license_renewal_date", "<=", RenewalToDate);
		qb.addFilter(qb.DATE, "l.license_end_date", ">=", expiryFromDate);
		qb.addFilter(qb.DATE, "l.license_end_date", "<=", expiryToDate);
		qb.addFilter(qb.STRING, "license_status", "IN", status);

		qb.build();
		PreparedStatement psData = qb.getDataStatement();
		PreparedStatement psCount = qb.getCountStatement();

		List licenseList = DataBaseUtil.queryToDynaList(psData);
		int count = Integer.parseInt((DataBaseUtil.getStringValueFromDb(psCount)));

		psData.close();
		psCount.close();
		con.close();

		return new PagedList(licenseList,count,25,pageNum);
	}


	private static final String INSERT_VALUES =
		" INSERT into licenses (license_desc,license_type_id,license_start_date,license_end_date, " +
		" license_renewal_date,license_value,license_note," +
		" license_status,license_id,contractor_id) " +
		" VALUES(?,?,?,?,?,?,?,?,?,?) ";

	public boolean insertFieldValues(Connection con,licenseForm lf,int licenseId,Date startDate,Date endDate,Date renewalDate) throws SQLException {

		try(PreparedStatement ps = con.prepareStatement(INSERT_VALUES)) {
			int i=1;
			ps.setString(i++, lf.getLicense_desc());
			ps.setInt(i++, 	lf.getLicense_type_id());
			ps.setDate(i++, 	startDate);
			ps.setDate(i++, 	endDate);
			ps.setDate(i++, 	renewalDate);
			ps.setBigDecimal(i++, 	lf.getLicense_value());
			ps.setString(i++, lf.getLicense_note());
			ps.setString(i++, lf.getLicense_status());
			ps.setInt(i++, 	licenseId);
			ps.setInt(i++, lf.getContractor_id());
			int result = ps.executeUpdate();
			return (result > 0 );
		}
	}


	private static final String UPDATE_FILE =
		"UPDATE licenses SET license_attachment=? WHERE license_id=?";

	public boolean updateFile(Connection con,int licneseId, InputStream file, int size) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(UPDATE_FILE)) {
			ps.setBinaryStream(1, file, size);
			ps.setInt(2, licneseId);
			int result = ps.executeUpdate();
			return (result > 0 );
		}
	}


	private static final String GET_LICENSE_DETAILS = " select license_id,license_desc,license_type_id,to_char(license_start_date,'DD-MM-YYYY')as license_start_date,  " +
			" to_char(license_end_date,'DD-MM-YYYY')as license_end_date,to_char(license_renewal_date,'DD-MM-YYYY')as license_renewal_date,license_value,license_note," +
			" license_status,contractor_id " +
			" from licenses where license_id=? ";

	public static BasicDynaBean getLicenseDetails(int licenseId) throws SQLException {
		List l = DataBaseUtil.queryToDynaList(GET_LICENSE_DETAILS, licenseId);
		return (BasicDynaBean) l.get(0);
	}


	private static final String UPDATE_VALUES =
		" UPDATE licenses SET license_desc=?,license_type_id=?,license_start_date=?,license_end_date=?, " +
		" license_renewal_date=?,license_value=?,license_note=?, " +
		" license_status=?, contractor_id=? " +
		" where license_id=? ";

	public boolean updateFields(Connection con,licenseForm lf,int licenseId,Date startDate,Date endDate,Date renewalDate) throws SQLException {

		try(PreparedStatement ps = con.prepareStatement(UPDATE_VALUES)) {
			int i=1;
			ps.setString(i++, lf.getLicense_desc());
			ps.setInt(i++, 	lf.getLicense_type_id());
			ps.setDate(i++, startDate);
			ps.setDate(i++, endDate);
			ps.setDate(i++, renewalDate);
			ps.setBigDecimal(i++, 	lf.getLicense_value());
			ps.setString(i++, lf.getLicense_note());
			ps.setString(i++, lf.getLicense_status());
			ps.setInt(i++, lf.getContractor_id());
			ps.setInt(i++, 	licenseId);
			int result = ps.executeUpdate();
			return (result > 0 );
		}
	}

	private static final String LICENSE_TYPE_LIST = "select license_type_id,license_type from license_type_master order by license_type";

	public ArrayList getLicenseTypes()throws SQLException {
		Connection con =DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps =con.prepareStatement(LICENSE_TYPE_LIST);
		ArrayList al = DataBaseUtil.queryToArrayList(ps);
		ps.close();
		con.close();
		return al;
	}

	public ArrayList<String> getlicenseNames()throws SQLException{
		Connection con =DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps =con.prepareStatement(LICENSE_TYPE_LIST);
		ArrayList<String> al = DataBaseUtil.queryToOnlyArrayList(ps);
		ps.close();
		con.close();
		return al;
	}

	private static final String GET_FORM = " SELECT license_attachment FROM licenses WHERE license_id=?";

	public static InputStream getlicenseForm(int lId) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_FORM);
			ps.setInt(1, lId);
			rs = ps.executeQuery();
			if (rs.next())
				return rs.getBinaryStream(1);
			else
				return null;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}

	private String DUPLICATE_LICENSE = " select license_desc from licenses where license_desc=? ";

	public boolean checkLicenseName(Connection con, String licenseName) throws SQLException{
		boolean duplicatelicense = false;
		if (licenseName.equals("")) return true;
		try(PreparedStatement ps = con.prepareStatement(DUPLICATE_LICENSE)){
		  ps.setString(1, licenseName);
		  try(ResultSet rs = ps.executeQuery()){
		    if(rs.next()){
		      duplicatelicense=true;
		      }
		    }
		  }
		return duplicatelicense;
	}

	public static final String GET_ACTIVE_LICENSE_DETAILS = " select l.license_id, l.license_desc, l.license_start_date, "+
	" l.license_end_date, l.license_renewal_date, 'Expired License' as status from licenses l   where  "+
	" CURRENT_DATE > l.license_renewal_date and l.license_status='A' "+
	" union all "+
	" select l.license_id, l.license_desc, l.license_start_date,  "+
	" l.license_end_date, l.license_renewal_date, 'Coming up for Renewal' as status from licenses l   where  "+
	" CURRENT_DATE <= l.license_renewal_date and l.license_status='A'";

	public static List<BasicDynaBean> getActiveLicenseDetails(){

		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> beanList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ACTIVE_LICENSE_DETAILS);
			beanList = DataBaseUtil.queryToDynaList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return beanList;
	}


}