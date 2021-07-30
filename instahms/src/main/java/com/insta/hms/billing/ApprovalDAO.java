package com.insta.hms.billing;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.PagedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Contains data access methods for storing/retrieving
 * the approvals object, stored in the table bill_approvals
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ApprovalDAO {

	static Logger logger = LoggerFactory.getLogger(ApprovalDAO.class);

	private Connection con = null;

	/*
	 * Constants used for sort order
	 */
	public static final int FIELD_NONE = 0;
	public static final int FIELD_APPROVAL_ID = 1;
	public static final int FIELD_APPROVAL_DATE = 2;
	public static final int FIELD_USERNAME = 3;
	public static final int FIELD_EXPORTED = 4;
	public static final int FIELD_NUM_TXNS = 5;
	// please also change approvalFieldList if the above is changing
	private static final String[] approvalFieldList = {
		"", "approval_id", "approval_date", "username", "exported", "num_txns"
	};

	/*
	 * constructor: we need a connection to be constructed
	 */
	public ApprovalDAO(Connection con) {
		this.con = con;
	}

	/*
	 * ID generation: get the next available approval ID
	 */
	public String getNextApprovalId() throws SQLException {
		return AutoIncrementId.getSequenceId("approval_id_sequence", "APPROVAL");
	}

	/*
	 * Search the approvals in a paged manner, given various filters.
	 * todo: this can become a utility class of its own.
	 */
	private static final String SEARCH_APPROVALS_QUERY_FIELDS = " SELECT * ";
	private static final String SEARCH_APPROVALS_QUERY_COUNT = " SELECT count(*) ";
	private static final String SEARCH_APPROVALS_QUERY_TABLES = " FROM bill_approvals ";

	public PagedList searchApprovals(java.sql.Date fromDate, java.sql.Date toDate,
				String username, String exportStatus,
				int sortOrder, int pageSize, int pageNum)
		throws SQLException {

		/*
		 * construct a WHERE clause based on the given filter
		 */
		StringBuilder where = new StringBuilder();
		if (fromDate != null) {
			DataBaseUtil.addWhereFieldOpValue(where, "approval_date", ">=", fromDate);
		}
		if (toDate != null) {
			DataBaseUtil.addWhereFieldOpValue(where, "approval_date", "<=", toDate);
		}

		if((null != username) && !(username.equalsIgnoreCase("A"))){
			DataBaseUtil.addWhereFieldOpValue(where, "username", "=", username);
		}

		if((null != exportStatus) && !(exportStatus.equalsIgnoreCase("A"))){
			DataBaseUtil.addWhereFieldOpValue(where, "exported", "=", exportStatus);
		}

		StringBuilder dataQuery = new StringBuilder();
		dataQuery.append(SEARCH_APPROVALS_QUERY_FIELDS);
		dataQuery.append(SEARCH_APPROVALS_QUERY_TABLES);
		dataQuery.append(where);

		if ( (sortOrder != FIELD_NONE) && (sortOrder < approvalFieldList.length) ) {
			dataQuery.append(" ORDER BY ").append(approvalFieldList[sortOrder]);
		}

		if (pageSize != 0) {
			dataQuery.append(" LIMIT ?");
			dataQuery.append(" OFFSET ?");
		}
		logger.debug("Data query: " + dataQuery);

		StringBuilder countQuery = new StringBuilder();
		countQuery.append(SEARCH_APPROVALS_QUERY_COUNT);
		countQuery.append(SEARCH_APPROVALS_QUERY_TABLES);
		countQuery.append(where);

		logger.debug("Count query: " + countQuery);

    PagedList ret = null;
    try (PreparedStatement dataStmt = con.prepareStatement(dataQuery.toString());
        PreparedStatement countStmt = con.prepareStatement(countQuery.toString());) {

      int i = 1;
      if (fromDate != null) {
        dataStmt.setDate(i, fromDate);
        countStmt.setDate(i, fromDate);
        i++;
      }
      if (toDate != null) {
        dataStmt.setDate(i, toDate);
        countStmt.setDate(i, toDate);
        i++;
      }
      if (username != null) {
        if (!(username.equalsIgnoreCase("A"))) {
          dataStmt.setString(i, username);
          countStmt.setString(i, username);
          i++;
        }
      }
      if (exportStatus != null) {
        if (!(exportStatus.equalsIgnoreCase("A"))) {
          dataStmt.setString(i, exportStatus);
          countStmt.setString(i, exportStatus);
          i++;
        }
      }

      if (pageSize != 0) {
        dataStmt.setInt(i++, pageSize);
        dataStmt.setInt(i++, (pageNum - 1) * pageSize);
      }

      ArrayList list = new ArrayList();
      try (ResultSet rs = dataStmt.executeQuery();) {
        while (rs.next()) {
          Approval appr = new Approval();
          populateApprovalDTO(appr, rs);
          list.add(appr);
        }
      }

      int totalCount = 0;
      try (ResultSet rs = countStmt.executeQuery();) {
        if (rs.next()) {
          totalCount = rs.getInt(1);
        }
      }
      ret = new PagedList(list, totalCount, pageSize, pageNum);
    }
    return ret;
  }

	/*
	 * Insert one approval record
	 */
	private static final String INSERT_APPROVAL = "INSERT into bill_approvals " +
		" (approval_id, approval_date, username, exported, num_txns, min_txn_date, max_txn_date) " +
		" VALUES (?,?,?,?,?,?,?) ";

	public boolean insertApproval(Approval appr) throws SQLException {
    int count = 0;
		try(PreparedStatement ps = con.prepareStatement(INSERT_APPROVAL);) {
		int i=1;
		ps.setString(i++, appr.getApprovalId());
		ps.setDate(i++, appr.getApprovalDate());
		ps.setString(i++, appr.getUsername());
		ps.setString(i++, appr.getExported());
		ps.setInt(i++, appr.getNumTransactions());
		ps.setDate(i++, appr.getMinTxnDate());
		ps.setDate(i++, appr.getMaxTxnDate());

		count = ps.executeUpdate();
		}
		return (count == 1);
	}

	private void populateApprovalDTO(Approval appr, ResultSet rs) throws SQLException {
		appr.setApprovalId(rs.getString("approval_id"));
		appr.setApprovalDate(rs.getDate("approval_date"));
		appr.setUsername(rs.getString("username"));
		appr.setExported(rs.getString("exported"));
		appr.setNumTransactions(rs.getInt("num_txns"));
		appr.setMinTxnDate(rs.getDate("min_txn_date"));
		appr.setMaxTxnDate(rs.getDate("max_txn_date"));
	}
}

