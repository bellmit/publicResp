
package com.insta.hms.master.PlanMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AbstractCachingDAO;

import net.sf.ehcache.Cache;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlanDetailsDAO extends AbstractCachingDAO {

	private Logger log = LoggerFactory.getLogger(PlanDetailsDAO.class);
	
	protected Cache newCache(String region) {
		return new Cache(region, 100, MemoryStoreEvictionPolicy.LRU, false, "/tmp", true, 0, 0, false, 0,
				null);
	}

	public  PlanDetailsDAO() {
		super("insurance_plan_details");
	}
	
	private static final String INSERT_PLAN_CHARGES_IP = " INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_amount_per_category, patient_percent, patient_amount_cap, per_treatment_limit, patient_type,username, category_payable)( " +
			" SELECT ?::integer AS plan_id, insurance_category_id, 0::numeric AS patient_amount, 0::numeric as patient_amount_per_category, 0::numeric AS patient_percent, null AS patient_amount_cap, null AS per_treatment_limit,'i' AS patient_type,? as username, insurance_payable "+
			" FROM  (SELECT * FROM item_insurance_categories ORDER BY insurance_category_id ) AS foo  ) ";

	private static final String INSERT_PLAN_CHARGES_OP = " INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_amount_per_category, patient_percent, patient_amount_cap, per_treatment_limit, patient_type,username, category_payable) ( "+
	" SELECT ?::integer AS plan_id, insurance_category_id, 0::numeric AS patient_amount,0::numeric as patient_amount_per_category, 0::numeric AS patient_percent, null AS patient_amount_cap, null AS per_treatment_limit, 'o' AS patient_type,? as username, insurance_payable "+
	" FROM  (SELECT * FROM item_insurance_categories ORDER BY insurance_category_id ) AS foo  ) ";

	public Boolean insertChargesForPlan(int planId,String username) throws SQLException {

		Connection con = null;
		Boolean status = false;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(INSERT_PLAN_CHARGES_IP);
			ps.setInt(1, planId);
			ps.setString(2, username);
			int i = ps.executeUpdate();
			ps = con.prepareStatement(INSERT_PLAN_CHARGES_OP);
			ps.setInt(1, planId);
			ps.setString(2, username);
			int j = ps.executeUpdate();
			if (i > 0 && j > 0) {
				status = true;
			}
			
			if (status && cachingEnabled()) {
				if (log.isDebugEnabled()) {
					log.debug(getTable() + ": Cache invalidated as a result of insert operation.");
				}
				getCache().removeAll();
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps, null);
		}
		return status;
	}


	public Boolean insertChargesWithoutExistCheck(Object planId) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		Boolean status = false;
		PreparedStatement ps = con.prepareStatement(INSERT_PLAN_CHARGES_IP);
		try{
			 ps.setInt(1, Integer.parseInt(planId.toString()));
			 int i = ps.executeUpdate();
			 ps = con.prepareStatement(INSERT_PLAN_CHARGES_OP);
			 int j = ps.executeUpdate();
				if(i>0 && j>0){
					status = true;
				}
		}finally{
			DataBaseUtil.closeConnections(con, ps, null);
		}
		return status;
	}


	private static final String GET_PLAN_CHARGES_WITHOUT_ID =
	" SELECT null as plan_id, insurance_category_id, insurance_category_name, 0::numeric AS patient_amount,0::numeric as patient_amount_per_category, 0::numeric AS patient_percent, null AS patient_amount_cap, null AS per_treatment_limit , " +
	"'i' AS patient_type,'Unknown' AS  username, current_timestamp AS mod_time,'Y' AS category_payable, null as category_prior_auth_required "+
	" FROM  (SELECT * FROM item_insurance_categories WHERE  insurance_payable='Y' ) AS foo  " +
	" UNION " +
	" SELECT null as plan_id, insurance_category_id, insurance_category_name, 0::numeric AS patient_amount, 0::numeric as patient_amount_per_category, 0::numeric AS patient_percent,null AS patient_amount_cap, null AS per_treatment_limit , " +
	" 'o' AS patient_type, 'Unknown' AS username, current_timestamp AS mod_time, 'Y' AS category_payable, null as category_prior_auth_required "+
	" FROM  (SELECT * FROM item_insurance_categories WHERE  insurance_payable='Y' ) AS foo ORDER BY insurance_category_name ";

	public List getAllPlanCharges() throws SQLException{
	   return DataBaseUtil.queryToDynaList(GET_PLAN_CHARGES_WITHOUT_ID);
	}

	private static final String GET_PLAN_CHARGES_FOR_PLAN_ID =
	" SELECT  plan_id, ipm.insurance_category_id, insurance_category_name, patient_type, patient_amount, patient_amount_per_category, patient_percent, " +
	" patient_amount_cap, per_treatment_limit , ch.insurance_payable, ch.system_category, " +
	" foo.ip_applicable, foo.op_applicable, foo.is_copay_pc_on_post_discnt_amt, ipm.category_payable, ipm.category_prior_auth_required "+
	" FROM  insurance_plan_details ipm" +
	" JOIN item_insurance_categories ch on ipm.insurance_category_id=ch.insurance_category_id " +
	" LEFT JOIN ( SELECT ip_applicable, op_applicable, plan_id, is_copay_pc_on_post_discnt_amt FROM " +
	" insurance_plan_main) AS foo USING (plan_id)  " +
	" WHERE plan_id=? ";

	private static final String GET_PATIENT_PLAN_DETAILS_FOR_VISIT_ID ="select pd.plan_id,pd.insurance_category_id,ch.insurance_category_name, " +
			"pd.patient_amount,pd.patient_percent,pd.patient_amount_cap,pd.per_treatment_limit,ch.insurance_payable,pd.patient_type, " +
			"pd.patient_amount_per_category,COALESCE(ipd.category_payable,ch.insurance_payable) as category_payable, ch.system_category "+
			" FROM patient_insurance_plan_details pd " +
			" JOIN patient_registration pr ON (pd.visit_id = pr.patient_id) "+
			" JOIN patient_details pdl ON (pdl.mr_no = pr.mr_no)" +
			" LEFT JOIN insurance_plan_details ipd ON(pd.plan_id = ipd.plan_id AND ipd.insurance_category_id = pd.insurance_category_id "+
			"   AND ipd.patient_type = pd.patient_type) "+
			" JOIN item_insurance_categories ch on ch.insurance_category_id=pd.insurance_category_id " +
			" where pd.visit_id=? and pd.plan_id=? and (patient_confidentiality_check(pdl.patient_group,pdl.mr_no)) "+ 
			" ORDER BY ch.display_order";

	private static final String GET_PATIENT_PLAN_DETAILS_FOR_MR_NO ="select pd.plan_id,pd.insurance_category_id,ch.insurance_category_name, " +
			"pd.patient_amount,pd.patient_percent,pd.patient_amount_cap,pd.per_treatment_limit,ch.insurance_payable,pd.patient_type, " +
			"pd.patient_amount_per_category, COALESCE(ipd.category_payable,ch.insurance_payable) as category_payable "+
			" FROM patient_insurance_plan_details pd " +
			" LEFT JOIN insurance_plan_details ipd ON(pd.plan_id = ipd.plan_id AND ipd.insurance_category_id = pd.insurance_category_id "+
			"   AND ipd.patient_type = pd.patient_type) "+
			" JOIN item_insurance_categories ch on ch.insurance_category_id=pd.insurance_category_id " +
			" where pd.visit_id= (select patient_id " +
			" from patient_registration pr "+
			" JOIN patient_details pd ON (pd.mr_no = pr.mr_no) "+
			" where main_visit_id=? and pr.mr_no=? and "+
			" (patient_confidentiality_check(pd.patient_group,pd.mr_no)) limit 1) and pd.plan_id=? " +
			" ORDER BY ch.insurance_category_name";


	public List getAllPlanCharges(int planId) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = con.prepareStatement(GET_PLAN_CHARGES_FOR_PLAN_ID+" ORDER BY ch.display_order");
		ps.setInt(1, planId);
		List lst = DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);
		return lst;
	}
	
	public List getAllPlanCharges(Connection con, int planId) throws SQLException{
    PreparedStatement ps = con.prepareStatement(GET_PLAN_CHARGES_FOR_PLAN_ID+" ORDER BY ch.display_order");
    ps.setInt(1, planId);
    List lst = DataBaseUtil.queryToDynaList(ps);
    DataBaseUtil.closeConnections(null, ps);
    return lst;
  }


	public List getAllPlanCharges (int planId, String visitType) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		if(visitType == null || visitType.equals(""))
			return getAllPlanCharges(planId);
		if(visitType.equalsIgnoreCase("o"))
			ps = con.prepareStatement(GET_PLAN_CHARGES_FOR_PLAN_ID+ " AND op_applicable = 'Y' ORDER BY  ch.display_order ");
		else if(visitType.equalsIgnoreCase("i"))
			ps = con.prepareStatement(GET_PLAN_CHARGES_FOR_PLAN_ID+ " AND ip_applicable = 'Y' ORDER BY  ch.display_order ");
		else
			ps = con.prepareStatement(GET_PLAN_CHARGES_FOR_PLAN_ID+" ORDER BY ch.display_order");
		ps.setInt(1, planId);
		List lst = DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);
		return lst;
	}

	public List getAllPlanChargesBasedonPatientType (int planId, String patientType, String visitId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		List lst = new ArrayList();
		try{
			if(patientType == null || patientType.equals(""))
				return getAllPlanCharges(planId);
			if(null !=visitId && !visitId.equals("")){
				ps=con.prepareStatement(GET_PATIENT_PLAN_DETAILS_FOR_VISIT_ID);
				ps.setString(1, visitId);
				ps.setInt(2,planId);
				lst=DataBaseUtil.queryToDynaList(ps);
			}
			if(lst.size()== 0){
				ps = null;
				if(patientType.equalsIgnoreCase("o"))
					ps = con.prepareStatement(GET_PLAN_CHARGES_FOR_PLAN_ID+ " AND patient_type = 'o' ORDER BY  ch.display_order ");
				else if(patientType.equalsIgnoreCase("i"))
					ps = con.prepareStatement(GET_PLAN_CHARGES_FOR_PLAN_ID+ " AND patient_type = 'i' ORDER BY  ch.display_order ");
				else
					ps = con.prepareStatement(GET_PLAN_CHARGES_FOR_PLAN_ID+" ORDER BY ch.display_order");
				ps.setInt(1, planId);
				lst=DataBaseUtil.queryToDynaList(ps);
			}
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return lst;
	}

	public static String DEDUCTION_FOR_CHARGE_HEAD = " SELECT ipd.plan_id, ipd.insurance_category_id, ipd.patient_amount, " +
	 " CASE WHEN iic.insurance_payable = 'N' THEN 100 ELSE " +
	 " ipd.patient_percent END AS patient_percent, ipd.patient_amount_cap, "+
	 "	ipd.per_treatment_limit, ipd.patient_type, ipd.patient_amount_per_category FROM insurance_plan_details ipd " +
	 " LEFT JOIN item_insurance_categories iic ON iic.insurance_category_id = ipd.insurance_category_id "+
	 " WHERE ipd.insurance_category_id=? AND plan_id= (SELECT pr.plan_id  " +
	 "   FROM patient_registration pr  " +
	 "     LEFT JOIN  bill b ON pr.patient_id=b.visit_id " +
	 "   WHERE bill_no=? LIMIT 1)";

	public static BasicDynaBean getChargeAmtForCategory(String billNo,
			int itemCategory) throws SQLException {
		PreparedStatement ps = null;
		BasicDynaBean chargeDedcn = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(DEDUCTION_FOR_CHARGE_HEAD);
			ps.setInt(1, itemCategory);
			ps.setString(2, billNo);
			chargeDedcn = DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return chargeDedcn;
	}

	public static String DEDUCTION_FOR_PLAN = " SELECT ipd.plan_id, ipd.insurance_category_id, ipd.patient_amount, CASE WHEN iic.insurance_payable = 'N' THEN 100 ELSE ipd.patient_percent END AS patient_percent, patient_amount_cap, "
			+ "	ipd.per_treatment_limit, ipd.patient_type, ipd.patient_amount_per_category, foo.ip_applicable, foo.op_applicable, iic.insurance_category_name, iic.insurance_payable, foo.is_copay_pc_on_post_discnt_amt,  "
			+ " ipd.category_payable, case when patient_type = 'o' then  foo.op_copay_percent else foo.ip_copay_percent end as copay_percentage"
			+ " FROM insurance_plan_details  ipd "
	    	+ " LEFT JOIN (SELECT ip_applicable, op_applicable, plan_id, is_copay_pc_on_post_discnt_amt, op_copay_percent, ip_copay_percent "
	    	+ " FROM insurance_plan_main ) AS foo USING (plan_id) "
	    	+ " LEFT JOIN item_insurance_categories iic ON iic.insurance_category_id = ipd.insurance_category_id "
			+ " WHERE plan_id=? AND ipd.insurance_category_id=? AND patient_type=? ";

	public static String IS_IP_APPLICABLE = " AND ip_applicable = 'Y' ";

	public static String IS_OP_APPLICABLE = " AND op_applicable = 'Y' ";

	public static BasicDynaBean getChargeAmtForPlan(int plan_id,
			int itemCategory, String visitType) throws SQLException {
		PreparedStatement ps = null;
		BasicDynaBean chargeDedcn = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			if(visitType.equalsIgnoreCase("o"))
				ps = con.prepareStatement(DEDUCTION_FOR_PLAN+IS_OP_APPLICABLE);
			else
				ps = con.prepareStatement(DEDUCTION_FOR_PLAN+IS_IP_APPLICABLE);
			ps.setInt(1, plan_id);
			ps.setInt(2, itemCategory);
			ps.setString(3, visitType);
			chargeDedcn = DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return chargeDedcn;
	}

	private static final String UPDATE_PLAN_CHARGE =
		" UPDATE insurance_plan_main " +
		" SET patient_deduction=?, " +
		" WHERE plan_id=? AND insurance_co_id=? AND category_id=? AND plan_name=?";

	public boolean updatePlanChargeList(Connection con, List<BasicDynaBean> chargeList) throws SQLException {

		PreparedStatement ps = con.prepareStatement(UPDATE_PLAN_CHARGE);

		for (BasicDynaBean c: chargeList) {
			ps.setBigDecimal(1, (BigDecimal) c.get("patient_deduction"));
			ps.setInt(2, Integer.parseInt(c.get("plan_id").toString()));
			ps.setString(3, (String) c.get("insurance_co_id"));
			ps.setInt(4, Integer.parseInt(c.get("category_id").toString()));
 			ps.setString(5, (String) c.get("plan_name"));
			ps.addBatch();
		}

		int results[] = ps.executeBatch();
		boolean status = DataBaseUtil.checkBatchUpdates(results);
		ps.close();
		if (status && cachingEnabled()) {
			if (log.isDebugEnabled()) {
				log.debug(getTable() + ": Cache invalidated as a result of insert operation.");
			}
			getCache().removeAll();
		}
		return status;
	}

	public List getInsurancePlanDetailsForFollowUpVisit(int planId, String mrno, String patientType, String mainvisitId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		List lst = new ArrayList();
		try{
			ps = con.prepareStatement(GET_PATIENT_PLAN_DETAILS_FOR_MR_NO);
			ps.setString(1, mainvisitId);
			ps.setString(2, mrno);
			ps.setInt(3, planId);
			lst = DataBaseUtil.queryToDynaList(ps);
			if(lst.size()== 0){
				ps = null;
				if(patientType.equalsIgnoreCase("o"))
					ps = con.prepareStatement(GET_PLAN_CHARGES_FOR_PLAN_ID+ " AND patient_type = 'o' ORDER BY  ch.display_order ");
				else if(patientType.equalsIgnoreCase("i"))
					ps = con.prepareStatement(GET_PLAN_CHARGES_FOR_PLAN_ID+ " AND patient_type = 'i' ORDER BY  ch.display_order ");
				else
					ps = con.prepareStatement(GET_PLAN_CHARGES_FOR_PLAN_ID+" ORDER BY ch.display_order");
				ps.setInt(1, planId);
				lst=DataBaseUtil.queryToDynaList(ps);
			}
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return lst;
	}
}
