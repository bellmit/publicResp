/**
 *
 */
package com.insta.hms.master.SponsorProcedureMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lakshmi.p
 *
 */
public class SponsorProcedureMasterDAO extends GenericDAO{

	static Logger logger = LoggerFactory.getLogger(SponsorProcedureMasterDAO.class);

	public SponsorProcedureMasterDAO() {
		super("sponsor_procedure_limit");

	}

	private static String SPONSOR_FIELDS = " SELECT * ";

	private static String SPONSOR_COUNT = " SELECT count(*) ";

	private static String SPONSOR_TABLES = " FROM (SELECT spl.procedure_no, spl.tpa_id, spl.procedure_code, spl.procedure_name, " +
			"	tpa.tpa_name,spl.status, spl.procedure_limit, spl.remarks  FROM sponsor_procedure_limit spl " +
										" JOIN tpa_master tpa USING (tpa_id))AS FOO ";


	public PagedList getSponsorProcedureList(Map listingParams, Map<LISTING, Object> pagingParams) throws SQLException, ParseException {

		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, SPONSOR_FIELDS,
					SPONSOR_COUNT, SPONSOR_TABLES, pagingParams);

			qb.addFilterFromParamMap(listingParams);
			qb.build();

			PagedList l = qb.getMappedPagedList();

			return l;
		}finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private String GET_PROCEDURE = "SELECT * FROM sponsor_procedure_limit WHERE procedure_code = ? AND tpa_id = ?";

	public BasicDynaBean getExistingBean(String code, String tpa_id) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con  = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PROCEDURE);
			ps.setObject(1, code);
			ps.setObject(2, tpa_id);
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

	private static String GET_ALL_PROCEDURES = " select procedure_code ||'-' || procedure_name from sponsor_procedure_limit " +
			" order by procedure_name " ;

	public static List getAllProcedureNames() throws SQLException {
		PreparedStatement ps = null;
		ArrayList procedureNameList = null;
		Connection con = null;
		try {
				con = DataBaseUtil.getConnection();
				ps = con.prepareStatement(GET_ALL_PROCEDURES);
				procedureNameList = DataBaseUtil.queryToArrayList1(ps);
			} catch (SQLException ex) {
				logger.error(ex.getMessage());
			} finally {
				DataBaseUtil.closeConnections(con, ps);
		}
		return procedureNameList;
	}

	private static String GET_ALL_TPA_PROCEDURES = " select procedure_no,procedure_code, procedure_name, procedure_limit " +
			" from sponsor_procedure_limit where tpa_id = ? and status = 'A' " +
			" UNION " +
			" select procedure_no,procedure_code, procedure_name, procedure_limit " +
			" from sponsor_procedure_limit where procedure_no " +
			"	= (select procedure_no from bill where bill_no =? and tpa_id = ?) order by procedure_name  " ;

	public static List<BasicDynaBean> getAllTPAProcedureDetails(String tpa_id, String billNo) throws SQLException {
		PreparedStatement ps = null;
		List procedureNameList = null;
		Connection con = null;
		try {
				con = DataBaseUtil.getConnection();
				ps = con.prepareStatement(GET_ALL_TPA_PROCEDURES);
				ps.setString(1, tpa_id);
				ps.setString(2, billNo);
				ps.setString(3, tpa_id);
				procedureNameList = DataBaseUtil.queryToDynaList(ps);
			} catch (SQLException ex) {
				logger.error(ex.getMessage());
			} finally {
				DataBaseUtil.closeConnections(con, ps);
		}
		return procedureNameList;
	}
	
	public static List<BasicDynaBean> getAllTPAProcedureDetails(Connection con, String tpa_id, String billNo) throws SQLException {
    PreparedStatement ps = null;
    List procedureNameList = null;
    try {
        ps = con.prepareStatement(GET_ALL_TPA_PROCEDURES);
        ps.setString(1, tpa_id);
        ps.setString(2, billNo);
        ps.setString(3, tpa_id);
        procedureNameList = DataBaseUtil.queryToDynaList(ps);
      } catch (SQLException ex) {
        logger.error(ex.getMessage());
      } finally {
        DataBaseUtil.closeConnections(null, ps);
    }
    return procedureNameList;
  }
}
