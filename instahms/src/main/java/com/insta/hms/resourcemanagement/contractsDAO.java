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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class contractsDAO extends GenericDAO {

	public contractsDAO(){
		super("contracts");
	}


	private static String CONTRACT_FIELDS = "select c.contract_id,c.contract_company,c.contract_type_id,c.contract_start_date,c.contract_end_date," +
	" c.contract_renewal_date,c.contract_value,c.contract_note,ct.contract_type,c.contract_status" ;

	private static String CONTRACT_COUNT = "SELECT count(*)";

	private static String CONTRACT_TABLES = " from contracts c JOIN contract_type_master ct on (c.contract_type_id=ct.contract_type_id)  ";

	public PagedList getcontractsDetails(Map<LISTING, Object> pagingParams,
			Date RenewalFromDate,Date RenewalToDate,Date expiryFromDate,Date expiryToDate, List status, int contract_type_id)throws SQLException {

		int pageNum = (Integer)pagingParams.get(LISTING.PAGENUM);
		Connection con = DataBaseUtil.getReadOnlyConnection();
		SearchQueryBuilder qb =
			new SearchQueryBuilder(con,CONTRACT_FIELDS,CONTRACT_COUNT,
					CONTRACT_TABLES,null,null,"c.contract_company",false,25,pageNum);
		qb.addFilter(qb.DATE, "c.contract_renewal_date", ">=", RenewalFromDate);
		qb.addFilter(qb.DATE, "c.contract_renewal_date", "<=", RenewalToDate);
		qb.addFilter(qb.DATE, "c.contract_end_date", ">=", expiryFromDate);
		qb.addFilter(qb.DATE, "c.contract_end_date", "<=", expiryToDate);
		qb.addFilter(qb.STRING, "contract_status", "IN", status);
		if(contract_type_id != 0)
		qb.addFilter(qb.INTEGER, "c.contract_type_id", "=", contract_type_id);

		qb.build();
		PreparedStatement psData = qb.getDataStatement();
		PreparedStatement psCount = qb.getCountStatement();

		List contractList = DataBaseUtil.queryToDynaList(psData);
		int count = Integer.parseInt((DataBaseUtil.getStringValueFromDb(psCount)));

		psData.close();
		psCount.close();
		con.close();

		return new PagedList(contractList,count,25,pageNum);
	}


	private static final String INSERT_VALUES =
		" INSERT into contracts (contract_company,contract_type_id,contract_start_date,contract_end_date, " +
		" contract_renewal_date,contract_value,contract_note,contract_id," +
		" contract_status,contractor_id) " +
		" VALUES(?,?,?,?,?,?,?,?,?,?) ";


	private static final String UPDATE_FILE =
		"UPDATE contracts SET contract_attachment=? WHERE contract_id=?";

	public boolean updateFile(Connection con,int contractId, InputStream file, int size) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(UPDATE_FILE);
			ps.setBinaryStream(1, file, size);
			ps.setInt(2, contractId);
			int result = ps.executeUpdate();
			return (result > 0 );
		} finally {
		  if(ps!=null){
			ps.close();
		  }
		}
	}

	private static final String GET_CONTRACT_DETAILS = " select contract_id,contract_company,contract_type_id,to_char(contract_start_date,'DD-MM-YYYY')as contract_start_date,  " +
	" to_char(contract_end_date,'DD-MM-YYYY')as contract_end_date,to_char(contract_renewal_date,'DD-MM-YYYY')as contract_renewal_date,contract_value,contract_note," +
	" contract_status,contractor_id, contract_file_name " +
	" from contracts where contract_id=? ";

	public static BasicDynaBean getContractDetails(int contractId) throws SQLException {
		List l = DataBaseUtil.queryToDynaList(GET_CONTRACT_DETAILS, contractId);
		return (BasicDynaBean) l.get(0);
	}

	private static final String UPDATE_VALUES =
		" UPDATE contracts SET contract_company=?,contract_type_id=?,contract_start_date=?, " +
		" contract_end_date=?, contract_renewal_date=?,contract_value=?,contract_note=?, " +
		" contract_status=?, contractor_id=?" +
		" where contract_id=? ";


	private static final String CONTRACT_TYPE_LIST = "select contract_type_id,contract_type from contract_type_master order by contract_type";

	public ArrayList getcontractTypes()throws SQLException {
		Connection con =DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps =con.prepareStatement(CONTRACT_TYPE_LIST);
		ArrayList al = DataBaseUtil.queryToArrayList(ps);
		ps.close();
		con.close();
		return al;
	}

	public ArrayList<String> getcontractNames()throws SQLException{
		Connection con =DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps =con.prepareStatement(CONTRACT_TYPE_LIST);
		ArrayList<String> al = DataBaseUtil.queryToOnlyArrayList(ps);
		ps.close();
		con.close();
		return al;
	}

	private static final String GET_UPLOAD = " SELECT contract_attachment, content_type, contract_file_name FROM contracts WHERE contract_id=?";

	public static Map<String,Object> getContractUpload(int CId) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Map<String,Object> uploadData = new HashMap<String,Object>();
		InputStream uploadFile = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_UPLOAD);
			ps.setInt(1, CId);
			rs = ps.executeQuery();
			if (rs.next()){
				uploadFile = rs.getBinaryStream(1);
				uploadData.put("uploadfile", uploadFile);
				String contentType = rs.getString(2);
				uploadData.put("contenttype", contentType);
				String fileName = rs.getString(3);
				uploadData.put("contract_file_name", fileName);
				return uploadData;
			}else{
				return null;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}

	private String DUPLICATE_CONTRACT = " select contract_company from contracts where contract_company=? ";

	public boolean checkContractName(Connection con, String cName) throws SQLException{
		boolean duplicate = false;
		if (cName.equals("")) return true;
		try(PreparedStatement ps = con.prepareStatement(DUPLICATE_CONTRACT)){
		  ps.setString(1, cName);
		  try(ResultSet rs = ps.executeQuery()){
		    if(rs.next()){
		      duplicate=true;
		      }
		    }
		  }
		return duplicate;
	}

	public static final String GET_ACTIVE_CONTRACT_DETAILS = " select c.contract_id, c.contract_company, c.contract_start_date, "+
		" c.contract_end_date, c.contract_renewal_date, 'Expired Contract' as status from contracts c   where  "+
		" CURRENT_DATE > c.contract_renewal_date and c.contract_status='A' "+
		" union all "+
		" select c.contract_id, c.contract_company, c.contract_start_date,  "+
		" c.contract_end_date, c.contract_renewal_date, 'Coming up for Renewal' as status from contracts c   where  "+
		" CURRENT_DATE <= c.contract_renewal_date and c.contract_status='A'";

	public static List<BasicDynaBean> getActiveContractDetails(){

		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> beanList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ACTIVE_CONTRACT_DETAILS);
			beanList = DataBaseUtil.queryToDynaList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return beanList;
	}

}