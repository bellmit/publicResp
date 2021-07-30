/**
 *
 */
package com.insta.hms.master.Accounting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AbstractCachingDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import net.sf.ehcache.Cache;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author krishna.t
 *
 */
public class ChargeHeadsDAO extends AbstractCachingDAO{

	public static final int NONE = 0;
	public static final int CHARGE_GROUP_NAME = 1;
	public static final int CHARGE_HEAD_ID = 2;
	public static final int CHARGE_HEAD_NAME = 3;
	public static final int ACCOUNT_HEAD_NAME = 4;
	public static final int DISPLAY_ORDER = 5;

	public static final String[] sortOrder = {
		"", "cg.chargegroup_name", "ch.chargehead_id",
		"ch.chargehead_name", "ah.account_head_name", "ch.display_order"};

	private final static String table = "chargehead_constants";

	public ChargeHeadsDAO() {
		super(table);
	}

	@Override
	protected Cache newCache(String region) {
		return new Cache(region, 100, MemoryStoreEvictionPolicy.LRU, false, "/tmp", true, 0, 0, false, 0,
				null);
	}

	private final static String CHARGE_HEADS =
		"SELECT ch.chargehead_id, ch.chargehead_name,ch.payment_eligible, "
		+ "  cg.chargegroup_name, ah.account_head_name, ch.display_order "
		+ " FROM chargehead_constants ch "
		+ "  JOIN chargegroup_constants cg ON ch.chargegroup_id=cg.chargegroup_id "
		+ "  JOIN bill_account_heads ah ON ch.account_head_id=ah.account_head_id ";

	public static List getChargeHeads(int sortColumn) throws SQLException{
		String query = CHARGE_HEADS;
		if (sortColumn != 0) {
			query = CHARGE_HEADS + " order by " + sortOrder[sortColumn];
		}
		return DataBaseUtil.queryToDynaList(query);
	}

	public List getAllChargeHeads() throws SQLException {
		return listAll();
	}

	private final static String ACOUNT_HEADS = "select distinct account_head_id from chargehead_constants";
	/**
	 * returns the list of distinct account heads that are used by charge heads.
	 *
	 * @return list of distinct account heads.
	 * @throws SQLException
	 */
	public static List getAccountHeadsInUse() throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;

		try {
			ps = con.prepareStatement(ACOUNT_HEADS);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private final static String ELIGIBLE_FOR_PAYMENT = "SELECT payment_eligible FROM chargehead_constants WHERE chargehead_id = ?";

	public static boolean isEligibleForPayment(String chargeHead) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(ELIGIBLE_FOR_PAYMENT);
			ps.setString(1, chargeHead);
			String eligible = DataBaseUtil.getStringValueFromDb(ps);
			return "Y".equals(eligible);
		}
		finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_CHARGEHEAD_DETAILS = " select chargehead_id,chargehead_name from chargehead_constants  " +
			" where payment_eligible='Y' order by display_order ";

	public static List getChargeHeadDetails() throws SQLException {
		PreparedStatement ps = null;
		List chargeHeadDetails = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_CHARGEHEAD_DETAILS);
			chargeHeadDetails = DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return chargeHeadDetails;
	}

	private static final String GET_PAYABLE_CHARGE_GROUPS_FIELDS = "SELECT DISTINCT cgc.* ";

    private static final String GET_PAYABLE_CHARGE_GROUPS_TABLES = "FROM chargegroup_constants cgc "+
		"	JOIN chargehead_constants chc using (chargegroup_id) where chc.payment_eligible ='Y' ";

	public static List getPayableChargeGroups() throws SQLException {
		PreparedStatement ps = null;
		List chargeGroups = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			String GET_PAYABLE_CHARGE_GROUPS = GET_PAYABLE_CHARGE_GROUPS_FIELDS +
				GET_PAYABLE_CHARGE_GROUPS_TABLES;
			ps = con.prepareStatement(GET_PAYABLE_CHARGE_GROUPS);
			chargeGroups = DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return chargeGroups;
	}

	public static final String PAYABLE_CHARGE_HEAD="SELECT * FROM chargehead_constants "+
		" WHERE payment_eligible ='Y' ";

	public static List getPayableChargeHeads() throws SQLException{
		return DataBaseUtil.queryToDynaList(PAYABLE_CHARGE_HEAD);
	}
	private static String CHARGE_FIELDS = "SELECT * ";

	private static String CHARGE_COUNT = " SELECT COUNT(*) ";

	private static String CHARGE_TABLE = " FROM(select ch.chargehead_id, ch.chargehead_name,ch.payment_eligible, "
		+ " cg.chargegroup_name, ah.account_head_name, ch.display_order from chargehead_constants ch "
		+ " join chargegroup_constants cg on ch.chargegroup_id=cg.chargegroup_id "
		+ " join bill_account_heads ah on ch.account_head_id=ah.account_head_id) AS foo ";



	public PagedList getChargeHeadDetails(Map map, Map pagingParams)
	throws Exception, ParseException {
		Connection con = DataBaseUtil.getReadOnlyConnection();
		SearchQueryBuilder qb = null;
		try {
			qb = new SearchQueryBuilder(con, CHARGE_FIELDS,
				CHARGE_COUNT, CHARGE_TABLE, pagingParams);

			qb.addFilterFromParamMap(map);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
}

	public static final String OP_CONSULTATION_CHARGE_HEADS="SELECT * FROM chargehead_constants " +
			" WHERE chargegroup_id = 'DOC' AND op_applicable = 'Y' ORDER BY display_order";

	public static List getOPConsultationChargeHeads() throws SQLException {
		return DataBaseUtil.queryToDynaList(OP_CONSULTATION_CHARGE_HEADS);
	}

	public static final String COUNT_OF_INSURANCE_PAYABLE_CHARGE_HEADS = "SELECT count(*) FROM chargehead_constants ";
	public static int countOfInsurancePayableChargeHeads() throws SQLException {
		return DataBaseUtil.getIntValueFromDb(COUNT_OF_INSURANCE_PAYABLE_CHARGE_HEADS);
	}

	private static final String GET_OT_DOC_CHARGE_HEADS =
		" SELECT chargehead_id, chargehead_name, display_order FROM chargehead_constants " +
		" WHERE chargehead_id IN ('ANAOPE','SUOPE','AANOPE','ASUOPE','COSOPE')" +
		" ORDER BY display_order ";

	public static List getOtDoctorChargeHeads() throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_OT_DOC_CHARGE_HEADS);
	}

	public static final String CHARGE_HEAD_QUERY =
		" SELECT cc.*,service_group_id FROM chargehead_constants cc LEFT JOIN service_sub_groups USING(service_sub_group_id) "+
		" WHERE chargehead_id = ? ";
	public static BasicDynaBean getChargeHeadBean(String chargeheadId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(CHARGE_HEAD_QUERY);
			ps.setString(1, chargeheadId);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

	}
	public static final String CHARGE_HEAD_QUERY_ =
			" SELECT cc.*,service_group_id FROM chargehead_constants cc LEFT JOIN service_sub_groups USING(service_sub_group_id) "+
			"   ";
		public static List<BasicDynaBean> getChargeHeadBean(List<String> chargeheadId) throws SQLException{
			Connection con = null;
			PreparedStatement ps = null;
			StringBuilder query = new StringBuilder();
			query.append(CHARGE_HEAD_QUERY_);
			try{
				DataBaseUtil.addWhereFieldInList(query, "chargehead_id", chargeheadId, false);
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(query.toString());
				//ps.setString(1, chargeheadId);
				int i = 1;
				for(int j=0;j<chargeheadId.size();j++) {
					ps.setString(i++, chargeheadId.get(j));
				}
				return DataBaseUtil.queryToDynaList(ps);
			}finally{
				DataBaseUtil.closeConnections(con, ps);
			}

		}
	
	private static final String CHARGE_HEAD_LIST = "select * from chargehead_constants where chargegroup_id != 'MED' order by chargehead_name";
	
	public static List getDiscountApplicableChargeHead() throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
			try{
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(CHARGE_HEAD_LIST);
				return DataBaseUtil.queryToDynaList(ps);
			}finally {
				DataBaseUtil.closeConnections(con, ps);
			}
	}
	
	public Map<String, BasicDynaBean> getChargeHeadMap() {
    // TODO Auto-generated method stub
    List<BasicDynaBean> chargeHeadList;
    Map<String,BasicDynaBean> chargeHeadMap = new HashMap<String, BasicDynaBean>();
    try {
      chargeHeadList = listAll();
      chargeHeadMap = ConversionUtils.listBeanToMapBean(chargeHeadList, "chargehead_id");
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    return chargeHeadMap;
  }
}
