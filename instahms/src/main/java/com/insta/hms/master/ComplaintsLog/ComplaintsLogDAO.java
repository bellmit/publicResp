package com.insta.hms.master.ComplaintsLog;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ComplaintsLogDAO extends GenericDAO{

	public ComplaintsLogDAO() {
		super("complaintslog");
	}

	public int getNextComplaintId() throws SQLException {
		int complaintId = getNextSequence();
		return complaintId;
	}

	public static final String GET_ALL_COMPLAINT_DETAILS = "SELECT complaint_id, logged_by, logged_date, updated_by, updated_date, " +
			"complaint_module, complaint_summary, complaint_desc, complaint_status, complaint_closure_note  " +
			"FROM complaintslog  " ;

	public static List getAllComplaintMasters(){

		Connection con = null;
		PreparedStatement ps = null;
		ArrayList complaintsLogList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_COMPLAINT_DETAILS);
		complaintsLogList = DataBaseUtil.queryToArrayList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return complaintsLogList;
	}

	public static final String GET_COMPLAINT_DETAILS = "SELECT cl.complaint_id, cl.logged_by, cl.logged_date, cl.updated_by, cl.updated_date, " +
			"cl.complaint_module, cl.complaint_summary, cl.complaint_desc, cl.complaint_status, " +
			"cl.complaint_closure_note, version_no FROM complaintslog cl  " +
			"where cl.complaint_id= ? ";

	public static List getComplaintMasters(BigDecimal complaint_id){

	Connection con = null;
	PreparedStatement ps = null;
	ArrayList complaintsLogList = null;

	try {
		con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GET_COMPLAINT_DETAILS);
		ps.setBigDecimal(1, complaint_id);
		complaintsLogList = DataBaseUtil.queryToArrayList(ps);

	} catch (SQLException e) {
		Logger.log(e);
	}finally{
		DataBaseUtil.closeConnections(con, ps);
	}
	return complaintsLogList;
	}


	public static final String GET_COMPLAINT_LOG_DETAILS = " ";

	public static List<BasicDynaBean> getComplaintsLogDetails(){

	Connection con = null;
	PreparedStatement ps = null;
	 List<BasicDynaBean> beanList = null;

	try {
		con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GET_COMPLAINT_LOG_DETAILS);
		beanList = DataBaseUtil.queryToDynaList(ps);

	} catch (SQLException e) {
		Logger.log(e);
	}finally{
		DataBaseUtil.closeConnections(con, ps);
	}
	return beanList;
	}


	private static String COMPLAINT_FIELDS = " select  cl.logged_by, cl.logged_date, cl.updated_by, cl.updated_date, "  +
			" cl.complaint_module, cl.complaint_summary, cl.complaint_desc,(CASE WHEN cl.complaint_status ='Open' " +
			" WHEN cl.complaint_status = 'Clarify' " +
			" WHEN cl.complaint_status = 'Pending' " +
			" WHEN cl.complaint_status = 'Fixed' " +
			" WHEN cl.complaint_status ='Not In Scope' " +
			" WHEN cl.complaint_status ='Prod Enh'  ELSE 'NONE' END)  as comp_status  ";

	private static String COMPLAINT_COUNT = " SELECT count(*) ";

	private static String COMPLAINT_TABLES = " from complaintslog cl " ;

	public PagedList getComplaintsLogDetails(Map<LISTING, Object> pagingParams) throws SQLException {
		Connection con = DataBaseUtil.getReadOnlyConnection();
		String sortField = (String) pagingParams.get(LISTING.SORTCOL);
		boolean sortReverse = (Boolean)pagingParams.get(LISTING.SORTASC);
		int pageSize = (Integer)pagingParams.get(LISTING.PAGESIZE);
		int pageNum = (Integer)pagingParams.get(LISTING.PAGENUM);

		SearchQueryBuilder qb =
			new SearchQueryBuilder(con, COMPLAINT_FIELDS, COMPLAINT_COUNT, COMPLAINT_TABLES,
					null, sortField,sortReverse, pageSize, pageNum);

		qb.addSecondarySort("complaint_id", true);

		qb.build();

		PreparedStatement psData = qb.getDataStatement();
		PreparedStatement psCount = qb.getCountStatement();

		List complaintsLogList = DataBaseUtil.queryToDynaList(psData);

		int count = Integer.parseInt(DataBaseUtil.getStringValueFromDb(psCount));

		psData.close();psCount.close();con.close();

		return new PagedList(complaintsLogList,count,20,pageNum);
	}
}

