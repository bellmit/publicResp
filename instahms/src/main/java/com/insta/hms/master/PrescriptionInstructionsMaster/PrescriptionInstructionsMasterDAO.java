package com.insta.hms.master.PrescriptionInstructionsMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
/**
 * @author nikunj.s
 *
 */
public class PrescriptionInstructionsMasterDAO extends GenericDAO {

	public PrescriptionInstructionsMasterDAO() {
		super("presc_instr_master");
	}

	private static final String PRESCRIPTION_INSTRUCTIONS_FILEDS = "SELECT * ";
	private static final String PRESCRIPTION_INSTRUCTIONS_FROM = " FROM presc_instr_master ";
	private static final String COUNT = " SELECT count(instruction_id) ";

	public PagedList getPrescriptionInstructionsMasterDetails(Map params, Map<LISTING, Object> listingParams) throws SQLException,
	ParseException {
	Connection con = DataBaseUtil.getConnection();
	SearchQueryBuilder qb = null;
	try {
		qb = new SearchQueryBuilder(con, PRESCRIPTION_INSTRUCTIONS_FILEDS, COUNT, PRESCRIPTION_INSTRUCTIONS_FROM, listingParams);
		qb.addFilterFromParamMap(params);
		qb.build();

		return qb.getDynaPagedList();
	} finally {
		DataBaseUtil.closeConnections(con, null);
		if (qb != null) qb.close();
		}
	}

	public static final String GET_ALL_PRESCRIPTION_INSTRUCTIONS_MASTER = " SELECT instruction_id,instruction_desc FROM presc_instr_master ";

	public static List getAllPrescriptionInstructions() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_PRESCRIPTION_INSTRUCTIONS_MASTER);
			return DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}

