package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillClaimDAO extends GenericDAO {

	static final Logger log = LoggerFactory.getLogger(BillClaimDAO.class);

	public BillClaimDAO() {
		super("bill_claim");
	}

	private BasicDynaBean getBillClaim(Connection con, String billNo, String claimId)
		throws SQLException {
		if (null == billNo || billNo.equals("")) {
			log.error("Invalid Bill No :" + billNo + " , cannot update claim status");
			return null;
		}

		if (null == claimId || claimId.equals("")) {
			log.error("Invalid Claim Id :" + claimId + " , cannot update claim status");
			return null;
		}
		Map<String, Object> keyMap = new HashMap<String, Object>();
		keyMap.put("bill_no", billNo);
		keyMap.put("claim_id", claimId);
		return findByKey(con, keyMap);
	}

	private boolean updateBillClaimStatus(Connection con, String billNo, String claimId, String status)
		throws SQLException, IOException {
		Map<String, Object> keyMap = new HashMap<String, Object>();
		keyMap.put("bill_no", billNo);
		keyMap.put("claim_id", claimId);
		BasicDynaBean bcbean = getBillClaim(con, billNo, claimId);
		if (null != bcbean) {
			bcbean.set("claim_status", status);
			update(con, bcbean.getMap(), keyMap);
			return true;
		}
		return false;
	}

	public boolean closeBillClaim(Connection con, String billNo, String claimId) throws SQLException, IOException {
		return updateBillClaimStatus(con, billNo, claimId, "C");
	}

	public BasicDynaBean getPrimaryBillClaim(Connection con, String billNo) throws SQLException {
		HashMap keyMap = new HashMap();
		keyMap.put("bill_no", billNo);
		keyMap.put("priority", new Integer(1));
		return findByKey(con, keyMap);
	}

	public BasicDynaBean getPrimaryBillClaim(String billNo) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		BasicDynaBean b = getPrimaryBillClaim(con, billNo);
		DataBaseUtil.closeConnections(con, null);
		return b;
	}


	public BasicDynaBean getSecondaryBillClaim(Connection con, String billNo) throws SQLException {
		HashMap keyMap = new HashMap();
		keyMap.put("bill_no", billNo);
		keyMap.put("priority", new Integer(2));
		return findByKey(con, keyMap);
	}

	public BasicDynaBean getSecondaryBillClaim(String billNo) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		BasicDynaBean b = getSecondaryBillClaim(con, billNo);
		DataBaseUtil.closeConnections(con, null);
		return b;
	}

	private static final String OPEN_BILL_CLAIMS = "SELECT * FROM bill_claim " +
	" WHERE bill_no = ? AND claim_status != 'C'";

	public List<BasicDynaBean> getOpenBillClaims(Connection con, String billNo) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<BasicDynaBean> l = null;

		ps = con.prepareStatement(OPEN_BILL_CLAIMS);
		ps.setString(1, billNo);
		rs = ps.executeQuery();
		RowSetDynaClass rsd = new RowSetDynaClass(rs);
		l = rsd.getRows();

		if (rs != null) rs.close();
		if (ps != null) ps.close();
		return l;
	}
	
	private static final String GET_PRI_CLAIM_ID = "SELECT bcl.claim_id FROM bill_claim bcl WHERE bcl.bill_no = ?  AND bcl.priority = 1 ";

  public String getPrimaryClaimID(String billNo) throws SQLException{
    return DataBaseUtil.getStringValueFromDb(GET_PRI_CLAIM_ID, billNo);
  }
}
