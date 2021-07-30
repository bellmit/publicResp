package com.insta.hms.master.PaymentRule;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PaymentRuleDAO extends GenericDAO {

	public PaymentRuleDAO() {
		super("payment_rules");
	}

	private static String PAYMENT_ALL_FIELDS =
		"( select doctor_category, case when (pr.referrer_category is null or  pr.referrer_category='') "+
		" then 'R' else pr.referrer_category end as referrer_category , "+
		" case when (pr.prescribed_category is null or pr.prescribed_category = '' ) then 'P' else "+
		" pr.prescribed_category end  as prescribed_category rate_plan, charge_head, activity_id, "+
		" precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value,  "+
		" presc_payment_option, presc_payment_value, dr_pkg_amt, od.org_name, cgc.chargegroup_id,  "+
		" ct.cat_name as doctor_category_name, ctm.cat_name as referer_category_name, "+
		" cth.cat_name as prescribed_category_name, chc.chargehead_name, cgc.chargegroup_name, "+
		" d.test_name,s.service_name, chc.payment_eligible, p.package_name ";

	private static String PAYMENT_COUNT = "SELECT count(*) ";

	private static String PAYMENT_FIELDS = "SELECT * ";

	private static String PAYMENT_TABLES = " FROM  ("+
		" SELECT doctor_category, case when (pr.referrer_category is null or  pr.referrer_category='') "+
		" then 'R' else pr.referrer_category end as referrer_category , "+
		" case when (pr.prescribed_category is null or pr.prescribed_category = '' ) then 'P' else "+
		" pr.prescribed_category end  as prescribed_category,pr.center_id,hcm.center_name,rate_plan, charge_head, activity_id, "+
		" precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value,  "+
		" presc_payment_option, presc_payment_value, dr_pkg_amt, od.org_name, cgc.chargegroup_id,  "+
		" ct.cat_name as doctor_category_name, ctm.cat_name as referer_category_name, "+
		" cth.cat_name as prescribed_category_name, chc.chargehead_name, cgc.chargegroup_name, payment_id,"+
		" d.test_name,s.service_name, p.package_name, chc.payment_eligible, dr_payment_expr, ref_payment_expr, presc_payment_expr "+
		" FROM payment_rules pr LEFT OUTER JOIN organization_details od ON(pr.rate_plan=od.org_id) "+
		" LEFT OUTER JOIN category_type_master ct ON(pr.doctor_category=ct.cat_id::varchar) "+
		" LEFT OUTER JOIN category_type_master ctm ON(pr.referrer_category=ctm.cat_id::varchar) "+
		" LEFT OUTER JOIN category_type_master cth ON(pr.prescribed_category=cth.cat_id::varchar) "+
		" LEFT OUTER JOIN chargehead_constants chc ON(chc.chargehead_id=pr.charge_head) "+
		" LEFT OUTER JOIN chargegroup_constants cgc ON(cgc.chargegroup_id=chc.chargegroup_id) "+
		" LEFT OUTER JOIN diagnostics d ON(d.test_id=pr.activity_id) " +
		" LEFT OUTER JOIN services s ON(s.service_id=pr.activity_id) " +
		" LEFT OUTER JOIN hospital_center_master hcm ON(hcm.center_id::text = pr.center_id)"+
		" LEFT OUTER JOIN packages p on (p.package_id::varchar = pr.activity_id)) as rules ";

	public PagedList getPaymentRuleDetails(Map filters,Map<LISTING, Object> pagingParams) throws SQLException, ParseException {

		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb = new SearchQueryBuilder(con, PAYMENT_FIELDS,
					PAYMENT_COUNT, PAYMENT_TABLES, pagingParams);

			qb.addFilterFromParamMap(filters);
			qb.addSecondarySort("payment_id");
			qb.build();
			PagedList l= qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String GET_RULES = "select * from payment_rules where charge_head=? " +
		" AND (activity_id is null OR activity_id = '') OR activity_id=? ORDER BY precedance ASC";

	public static List getRules(String chargehead, String activity)
			throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_RULES);
			ps.setString(1, chargehead);
			ps.setString(2, activity);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static String GET_PAYMENT_RULE_RECORD = "SELECT * FROM ( "+
		" SELECT doctor_category, case when (pr.referrer_category is null or  pr.referrer_category='') "+
		" then 'R' else pr.referrer_category end as referrer_category , "+
		" case when (pr.prescribed_category is null or pr.prescribed_category = '' ) then 'P' else "+
		" pr.prescribed_category end  as prescribed_category, rate_plan, charge_head, activity_id, "+
		" precedance, dr_payment_option, dr_payment_value, ref_payment_option, payment_id, chc.chargehead_id,"+
		" ref_payment_value, presc_payment_option, presc_payment_value, dr_pkg_amt, od.org_name, "+
		" cgc.chargegroup_id, ct.cat_name as doctor_category_name, ctm.cat_name as referer_category_name, "+
		" cth.cat_name as prescribed_category_name, chc.chargehead_name, cgc.chargegroup_name, cgc.chargegroup_id,pr.center_id, "+
		" d.test_name,s.service_name, p.package_name, chc.payment_eligible,pr.dr_payment_expr, ref_payment_expr, presc_payment_expr,pr.use_discounted_amount" +
		" FROM payment_rules pr "+
		" LEFT OUTER JOIN organization_details od ON(pr.rate_plan=od.org_id) "+
		" LEFT OUTER JOIN category_type_master ct ON(pr.doctor_category=ct.cat_id::varchar) "+
		" LEFT OUTER JOIN category_type_master ctm ON(pr.referrer_category=ctm.cat_id::varchar) "+
		" LEFT OUTER JOIN category_type_master cth ON(pr.prescribed_category=cth.cat_id::varchar) "+
		" LEFT OUTER JOIN chargehead_constants chc ON(chc.chargehead_id=pr.charge_head) "+
		" LEFT OUTER JOIN chargegroup_constants cgc ON(cgc.chargegroup_id=chc.chargegroup_id) "+
		" LEFT OUTER JOIN diagnostics d ON(d.test_id=pr.activity_id) "+
		" LEFT OUTER JOIN services s ON(s.service_id=pr.activity_id) " +
		" LEFT OUTER JOIN packages p on (p.package_id::varchar = pr.activity_id)) as foo "+
		" where payment_id=? ";

	public BasicDynaBean getPaymentRecord(int paymentId)throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;

		try {
			ps = con.prepareStatement(GET_PAYMENT_RULE_RECORD);
			ps.setInt(1, paymentId);
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

	private static final String GET_ALL_RULES = "select * from payment_rules order by precedance asc";

	public boolean updatePrecedenceValues() throws SQLException, IOException {
		boolean status = true;

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		Statement stmt = null;
		List list = DataBaseUtil.queryToDynaList(GET_ALL_RULES);
		int precedance = 10;
		for (Object object : list) {
			BasicDynaBean bean = (BasicDynaBean)object;
			bean.set("precedance", precedance);
			precedance +=10;
		}

		try {
			stmt = con.createStatement();
			stmt.execute("delete from payment_rules");
			insertAll(con, list);
			con.commit();
		} finally {
			DataBaseUtil.closeConnections(con, stmt);
		}

		return status;
	}

	private  String DUP_PAYMENT_RULE_CHECK =
		"SELECT precedance FROM payment_rules " +
		" WHERE rate_plan=? AND doctor_category=? AND referrer_category=? AND prescribed_category=? " +
		"  AND charge_head=? AND coalesce(activity_id,'')=? # ";

	public int duplicatePaymentRule(BasicDynaBean bean) throws SQLException {
		Connection con = null; PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			String centerId = (String)bean.get("center_id");
			if(centerId != null)
				DUP_PAYMENT_RULE_CHECK = DUP_PAYMENT_RULE_CHECK.replace("#", " AND center_id = ?");
			else
				DUP_PAYMENT_RULE_CHECK = DUP_PAYMENT_RULE_CHECK.replace("#", " ");

			ps = con.prepareStatement(DUP_PAYMENT_RULE_CHECK);
			int i=1;
			ps.setString(i++, (String) bean.get("rate_plan"));
			ps.setString(i++, (String) bean.get("doctor_category"));
			ps.setString(i++, (String) bean.get("referrer_category"));
			ps.setString(i++, (String) bean.get("prescribed_category"));
			ps.setString(i++, (String) bean.get("charge_head"));
			ps.setString(i++, (String) bean.get("activity_id"));

			if(centerId != null)
				ps.setString(i++, centerId);


			return DataBaseUtil.getIntValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	public static final String PAYMENT_LAB_ACTIVITIES = "select test_id, test_name from diagnostics "+
		" where test_id in (select activity_id from payment_rules where charge_head='LTDIA') ";

	public static final String PAYMENT_RAD_ACTIVITIES = "select test_id, test_name from diagnostics "+
		        " where test_id in (select activity_id from payment_rules where charge_head='RTDIA') ";

	public static final String PAYMENT_SERVICE_ACTIVITIES ="select service_id, service_name from services where service_id in (select activity_id from payment_rules) ";

	public static ArrayList paymentRuleLabTests()throws SQLException {
		return DataBaseUtil.queryToArrayList(PAYMENT_LAB_ACTIVITIES);
	}

	public static ArrayList paymentRuleRadTests() throws SQLException {
		return DataBaseUtil.queryToArrayList(PAYMENT_RAD_ACTIVITIES);
	}

	public static ArrayList paymentRuleServices()throws SQLException {
		return DataBaseUtil.queryToArrayList(PAYMENT_SERVICE_ACTIVITIES);
	}

	public static final String MAX_OF_ALL_STAR = "SELECT precedance FROM payment_rules " +
			"WHERE charge_head = ? AND doctor_category = '*' AND referrer_category = '*' AND rate_plan = '*' " +
			"AND prescribed_category = '*' ORDER BY precedance DESC LIMIT 1";

	public static final String MAX_OF_ALL_WITHOUT_STAR = " SELECT precedance FROM payment_rules " +
			"where charge_head = ? AND (doctor_category != '*' OR referrer_category != '*' OR rate_Plan != '*' " +
			"OR prescribed_category != '*') ORDER BY precedance DESC LIMIT 1";

	public static Integer getPrecedance(boolean allStarRule, String chargeId)throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			if (allStarRule)
				pstmt = con.prepareStatement(MAX_OF_ALL_WITHOUT_STAR);
			else
				pstmt = con.prepareStatement(MAX_OF_ALL_STAR);
			pstmt.setString(1, chargeId);
			rs = pstmt.executeQuery();
			if (rs.next())
				return rs.getInt(1);
			return null;
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}

	}


}
