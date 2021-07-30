package com.insta.hms.billing;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.InputValidator;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.editvisitdetails.EditVisitDetailsDAO;
import com.insta.hms.master.Accounting.ChargeHeadsDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PlanMaster.PlanMasterDAO;
import com.insta.hms.master.ServiceMaster.ServiceMasterDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;
import com.insta.hms.stores.MedicineStockDAO;
import org.apache.commons.beanutils.BasicDynaBean;
import org.owasp.esapi.errors.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
 * Contains data access methods for storing/retrieving
 * the bill object, using the table "bill"
 */
public class BillDAO extends GenericDAO{

	static Logger logger = LoggerFactory.getLogger(BillDAO.class);
	
	private static GenericDAO billDAO = new GenericDAO("bill");
	private static GenericDAO billChargeDAO = new GenericDAO("bill_charge");
	
	/*
	 * Constants used for sort order
	 */
	public static final int FIELD_NONE = 0;
	public static final int FIELD_BILL_NO = 1;
	public static final int FIELD_BILL_TYPE = 2;
	public static final int FIELD_BILL_DATE = 3;
	public static final int FIELD_BILL_STATUS = 4;
	public static final int FIELD_VISIT_ID = 5;
	public static final int FIELD_MRNO = 6;

	private Connection con = null;
	
	private static BillChargeClaimDAO billChargeClaimDAO = new BillChargeClaimDAO();
	private static BillClaimDAO billClaimDAO = new BillClaimDAO();

	public BillDAO(Connection con) {
		super("bill");
		this.con = con;
	}

	public BillDAO() {
		super("bill");
		// use this for utility method calling (i.e., connection is independent for the method.)
	}

	/*
	 * Bill number generation methods: this is based on a set of preferences on how the
	 * type controls the prefix and sequence to use.
	 */
	
	private static final String BILL_SEQUENCE_PATTERN =
			" SELECT pattern_id "+
			" FROM (SELECT min(priority) as priority, pattern_id FROM hosp_bill_seq_prefs " +
			"  WHERE (bill_type = ? or bill_type ='*') AND " +
			"        (visit_type = ? or visit_type = '*') AND " +
			"        (restriction_type = ? OR restriction_type = '*') AND " +
			"        (center_id = ? OR center_id = 0) AND " +
			"        (is_tpa = ? OR is_tpa = '*') AND " +
			"        (is_credit_note =? OR is_credit_note ='*')" +
			"		GROUP BY pattern_id ORDER BY priority limit 1) as foo";
			

	public static final String getBillNoPattern(String billType, String visitType,
			String restrictionType, int centerId,boolean isTpa, boolean isCreditNote) throws SQLException {

		Connection con = null;
		PreparedStatement stmt = null;

		try {
			con = DataBaseUtil.getConnection();
			stmt = con.prepareStatement(BILL_SEQUENCE_PATTERN);
			stmt.setString(1, billType);
			stmt.setString(2, visitType);
			stmt.setString(3, restrictionType);
			stmt.setInt(4, centerId);
			stmt.setString(5, (isTpa==true?"t":"f"));
			stmt.setString(6, (isCreditNote == true?"t":"f"));

			List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(stmt);
			BasicDynaBean b = l.get(0);
			return (String) b.get("pattern_id");
		} finally {
			DataBaseUtil.closeConnections(con, stmt);
		}
	}

	public static final String getNextBillNo(Connection con, String billType, String visitType,
			String restrictionType, int centerId,boolean isTpa,boolean isCreditNote) throws SQLException {

		String patternId = getBillNoPattern(billType, visitType, restrictionType, centerId,isTpa,isCreditNote);
		return DataBaseUtil.getNextPatternId(con, patternId);
	}

	private static final String BILL_QUERY = "SELECT b.bill_no, b.visit_id, pr.mr_no, b.visit_type, " +
		" total_amount, b.bill_type, b.open_date, b.mod_time, b.bill_signature," +
		" pic.status as primary_claim_status, sic.status as secondary_claim_status, b.claim_recd_amount, " +
		" b.status, b.payment_status, pr.status as visit_status, b.discharge_status, " +
		" b.finalized_date, b.closed_date, b.opened_by, b.username, b.closed_by, b.finalized_by, b.account_group, " +
		" b.last_finalized_at, uc.temp_username as closed_by_name, " +
		" uco.temp_username as opened_by_name, " +
		" ufo.temp_username as finalized_by_name, " +
		" b.app_modified, b.remarks,b.credit_note_reasons, b.discount_auth, pr.ready_to_discharge, " +
		" pr.org_id, pr.bed_type, pr.dept_name, b.total_amount, b.total_tax, b.total_discount,b.total_claim,b.total_claim_return, " +
		" b.approval_amount, b.primary_approval_amount, b.secondary_approval_amount, " +
		" b.primary_total_claim, b.secondary_total_claim, u.temp_username, b.deposit_set_off," +
		" b.points_earned, b.points_redeemed, b.points_redeemed_amt, " +
		" da.disc_auth_name, b.restriction_type, b.procedure_no, sp.procedure_code, sp.procedure_name, " +
		" sp.procedure_limit, b.total_claim, b.sponsor_bill_no, b.total_receipts, " +
		" b.primary_total_sponsor_receipts, b.secondary_total_sponsor_receipts, 0 as insurance_deduction, " +
		" b.dyna_package_id, b.dyna_package_charge, dyp.dyna_package_name, " +
		" dpo.item_code as dyna_pkg_rate_plan_code,is_tpa, is_primary_bill, b.dyna_pkg_processed, " +
		" bill_rate_plan_id, od.org_name, b.bill_printed, b.cancel_reason, b.reopen_reason, " +
		" coalesce(b.bill_label_id, -1) AS bill_label_id," +
		" b.points_earned, b.points_redeemed, b.points_redeemed_amt, pbc.plan_id as primary_plan_id, "+
		" sbc.plan_id as secondary_plan_id, b.patient_writeoff, b.sponsor_writeoff, b.writeoff_remarks, " +
		" b.sponsor_writeoff_remarks, b.cancellation_approval_status, b.discount_category_id," +
		" dcm.discount_plan_name discount_cat_name,b.cancellation_approved_by, b.ip_deposit_set_off,"+
		" pic.closure_type as primary_closure_type ,sic.closure_type as secondary_closure_type,"+
		" pbc.claim_id as primaryClaimId, sbc.claim_id as secondaryClaimId, pd.financial_discharge_date, "+
		" pd.financial_discharge_time, b.total_claim_tax, pr.center_id, pr.ip_credit_limit_amount " ;

		static final String BILL_QUERY_TABLES = " FROM bill b " +
			" LEFT JOIN bill_claim pbc ON (b.bill_no = pbc.bill_no AND pbc.priority = 1) "+
			" LEFT JOIN bill_claim sbc ON (b.bill_no = sbc.bill_no AND sbc.priority = 2) "+
			" LEFT JOIN insurance_claim pic ON (pic.claim_id=pbc.claim_id AND pic.plan_id=pbc.plan_id) "+
			" LEFT JOIN insurance_claim sic ON (sic.claim_id=sbc.claim_id AND sic.plan_id=sbc.plan_id) "+
			" LEFT join patient_registration pr on (pr.patient_id = b.visit_id) " +
			" LEFT JOIN u_user u ON (u.emp_username = b.username) " +
			" LEFT JOIN u_user uc ON (uc.emp_username = b.closed_by) " +
			" LEFT JOIN u_user uco ON (uco.emp_username = b.opened_by) " +
			" LEFT JOIN u_user ufo ON (ufo.emp_username = b.finalized_by) " +
			" LEFT JOIN discount_authorizer da ON (da.disc_auth_id = b.discount_auth) "+
			" LEFT JOIN discount_plan_main dcm ON (dcm.discount_plan_id = b.discount_category_id) "+
			" LEFT JOIN sponsor_procedure_limit sp ON (sp.procedure_no = b.procedure_no) "+
			" LEFT JOIN dyna_packages dyp ON (dyp.dyna_package_id = b.dyna_package_id) " +
			" LEFT JOIN dyna_package_org_details dpo ON (dpo.dyna_package_id = b.dyna_package_id AND dpo.org_id = pr.org_id)"+
			" LEFT JOIN organization_details od ON (b.bill_rate_plan_id = od.org_id ) "+
			" LEFT JOIN patient_discharge pd ON (b.visit_id = pd.patient_id ) "+
			" LEFT JOIN patient_details patd ON patd.mr_no = pr.mr_no ";

	private static final String GET_BILL = BILL_QUERY + BILL_QUERY_TABLES + " WHERE b.bill_no=? ";
	
	private static final String PAT_CONFIDENTIALITY_FILTER = " AND (patient_confidentiality_check(COALESCE(patd.patient_group, 0),patd.mr_no) )";

	public Bill getBill(String billNo) throws SQLException {

		PreparedStatement ps = con.prepareStatement(GET_BILL + PAT_CONFIDENTIALITY_FILTER);
		ps.setString(1, billNo);
		ResultSet rs = ps.executeQuery();

		Bill bill = null;
		if (rs.next()) {
			bill = new Bill();
			populateBill(bill, rs);
		}
		rs.close();
		ps.close();
		return bill;
	}
		
  static final String BILL_QUERY_TABLES_FOR_CENTER = " FROM bill b "
      + " LEFT join patient_registration pr on (pr.patient_id = b.visit_id) "
      + " LEFT JOIN bill_claim pbc ON (b.bill_no = pbc.bill_no AND pbc.priority = 1) "
      + " LEFT JOIN bill_claim sbc ON (b.bill_no = sbc.bill_no AND sbc.priority = 2) "
      + " LEFT JOIN insurance_claim pic ON (pic.claim_id=pbc.claim_id AND pic.plan_id=pbc.plan_id) "
      + " LEFT JOIN insurance_claim sic ON (sic.claim_id=sbc.claim_id AND sic.plan_id=sbc.plan_id) "
      + " LEFT JOIN store_retail_customers phc ON (b.visit_id = phc.customer_id) "
      + " LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id) "
      + " LEFT JOIN u_user u ON (u.emp_username = b.username) "
      + " LEFT JOIN u_user uc ON (uc.emp_username = b.closed_by) "
      + " LEFT JOIN u_user uco ON (uco.emp_username = b.opened_by) "
      + " LEFT JOIN u_user ufo ON (ufo.emp_username = b.finalized_by) "
      + " LEFT JOIN discount_authorizer da ON (da.disc_auth_id = b.discount_auth) "
      + " LEFT JOIN discount_plan_main dcm ON (dcm.discount_plan_id = b.discount_category_id) "
      + " LEFT JOIN sponsor_procedure_limit sp ON (sp.procedure_no = b.procedure_no) "
      + " LEFT JOIN dyna_packages dyp ON (dyp.dyna_package_id = b.dyna_package_id) "
      + " LEFT JOIN dyna_package_org_details dpo ON (dpo.dyna_package_id = b.dyna_package_id AND dpo.org_id = pr.org_id)"
      + " LEFT JOIN organization_details od ON (b.bill_rate_plan_id = od.org_id ) "
      + " LEFT JOIN patient_discharge pd ON (b.visit_id = pd.patient_id ) "
      + " LEFT JOIN patient_details patd ON patd.mr_no = pr.mr_no ";

  private static final String GET_BILL_WITH_CENTER = BILL_QUERY + BILL_QUERY_TABLES_FOR_CENTER
      + " WHERE b.bill_no = ? AND coalesce(pr.center_id, isr.center_id, phc.center_id) = ? ";

  public Bill getBill(String billNo, int centerId) throws SQLException {

    PreparedStatement ps = con.prepareStatement(GET_BILL_WITH_CENTER + PAT_CONFIDENTIALITY_FILTER);
    ps.setString(1, billNo);
    ps.setInt(2, centerId);
    ResultSet rs = ps.executeQuery();

    Bill bill = null;
    if (rs.next()) {
      bill = new Bill();
      populateBill(bill, rs);
    }
    rs.close();
    ps.close();
    return bill;
  }

	/*
	 * Bill Bean query: most used bill query, giving a convenient set of fields for the bill
	 * including some very often used joined fields.
	 */
	private static final String BILL_BEAN_QUERY =
		"SELECT b.bill_no, b.visit_id, pr.mr_no, b.visit_type, b.bill_type, b.restriction_type, b.status, " +
		"  b.payment_status, b.discharge_status, b.bill_signature," +
		"  b.primary_claim_status, b.secondary_claim_status, b.app_modified, is_primary_bill, " +
		"  pr.status as visit_status, pr.ready_to_discharge, pr.org_id, pr.bed_type, pr.dept_name, " +
		"  pr.category_id, pr.primary_sponsor_id, pr.secondary_sponsor_id, pr.plan_id, b.is_tpa, ppd.member_id, " +
		"  pr.primary_insurance_co, pr.secondary_insurance_co," +
		"  icmp.insurance_co_name AS primary_insurance_co_name, " +
		"  icms.insurance_co_name AS secondary_insurance_co_name, " +
		"  b.account_group, " +
		"  b.open_date, b.mod_time, b.finalized_date, b.closed_date, b.last_finalized_at, " +
		"  b.opened_by, b.username, b.closed_by, b.finalized_by, u.temp_username, " +
		"  uc.temp_username as closed_by_name, uco.temp_username as opened_by_name, ufo.temp_username as finalized_by_name, " +
		"  b.total_amount, b.total_tax, b.total_claim, b.claim_recd_amount, " +
		"  b.approval_amount, b.primary_approval_amount, b.secondary_approval_amount, " +
		"  b.primary_total_claim, b.secondary_total_claim, b.last_finalized_at," +
		"  b.deposit_set_off, b.points_earned, b.points_redeemed, b.points_redeemed_amt, " +
		"	COALESCE(rps.points_earned,0) as total_points_earned," +
		"	COALESCE(rps.points_redeemed,0) as total_points_redeemed," +
		"	COALESCE(rps.open_points_redeemed,0) as total_open_points_redeemed," +
		"	(COALESCE(rps.points_earned,0) - " +
		"		COALESCE(rps.points_redeemed,0) - " +
		"		COALESCE(rps.open_points_redeemed,0)) as total_points_available," +
		"  b.sponsor_bill_no, b.total_receipts, " +
		"  (COALESCE(b.primary_total_sponsor_receipts,0) + COALESCE(b.secondary_total_sponsor_receipts,0)) AS total_sponsor_receipts, " +
		"  b.primary_total_sponsor_receipts, b.secondary_total_sponsor_receipts, b.insurance_deduction, " +
		"  b.remarks, b.discount_auth, da.disc_auth_name,b.total_discount, " +
		"  b.procedure_no, sp.procedure_code, sp.procedure_name, sp.procedure_limit, " +
		"  b.dyna_package_id, b.dyna_package_charge, dyp.dyna_package_name, " +
		"  dpo.item_code as dyna_pkg_rate_plan_code,b.bill_rate_plan_id, b.dyna_pkg_processed, " +
		"  b.national_claim_no, b.bill_printed, b.cancel_reason, b.reopen_reason, blm.bill_label_name, " +
		"  pbc.plan_id as primary_plan_id, sbc.plan_id as secondary_plan_id, b.cancellation_approved_by, b.cancellation_approved_date, "+
		"  b.discount_category_id,dcm.discount_plan_name discount_cat_name,0.00 as discount_cat_perc,b.total_tax, b.total_claim_tax "+
		" FROM bill b " +
		" LEFT JOIN bill_claim pbc ON(b.bill_no = pbc.bill_no AND pbc.priority = 1)  "+
		" LEFT JOIN bill_claim sbc ON(b.bill_no = sbc.bill_no AND sbc.priority= 2) "+
		"  LEFT JOIN patient_registration pr on (pr.patient_id = b.visit_id) " +
		"  LEFT JOIN u_user u ON (u.emp_username = b.username) " +
		"  LEFT JOIN u_user uc ON (uc.emp_username = b.closed_by) " +
		"  LEFT JOIN u_user uco ON (uco.emp_username = b.opened_by) " +
		"  LEFT JOIN u_user ufo ON (ufo.emp_username = b.finalized_by) " +
		"  LEFT JOIN discount_authorizer da ON (da.disc_auth_id = b.discount_auth) "+
		"  LEFT JOIN sponsor_procedure_limit sp ON (sp.procedure_no = b.procedure_no) "+
		"  LEFT JOIN dyna_packages dyp ON (dyp.dyna_package_id = b.dyna_package_id) " +
		"  LEFT JOIN dyna_package_org_details dpo ON (dpo.dyna_package_id = b.dyna_package_id " +
		"   AND dpo.org_id = pr.org_id)" +
		"  LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pr.patient_policy_id) "+
		"  LEFT JOIN insurance_company_master icmp ON (icmp.insurance_co_id = pr.primary_insurance_co) " +
		"  LEFT JOIN insurance_company_master icms ON (icms.insurance_co_id = pr.secondary_insurance_co)" +
		"  LEFT JOIN bill_label_master blm ON (b.bill_label_id = blm.bill_label_id)" +
		"  LEFT JOIN reward_points_status rps ON (rps.mr_no = pr.mr_no) " +
		"  LEFT JOIN discount_plan_main dcm ON (dcm.discount_plan_id = b.discount_category_id) " +
		" WHERE b.bill_no=? ";

	public static BasicDynaBean getBillBean(String billNo) throws SQLException {
		return DataBaseUtil.queryToDynaBean(BILL_BEAN_QUERY, billNo);
	}

	public static BasicDynaBean getBillBean(Connection pcon, String billNo) throws SQLException {

		PreparedStatement ps = pcon.prepareStatement(BILL_BEAN_QUERY);
		ps.setString(1, billNo);
		List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(ps);
		if ( (l != null) && (l.size() > 0) ) {
			return l.get(0);
		}
		return null;
	}
	
	private static final String BILL_BEAN_WITHOUT_DYNA_PKG_CODE_QUERY =
        "SELECT b.bill_no, b.visit_id, pr.mr_no, b.visit_type, b.bill_type, b.restriction_type, b.status, " +
        "  b.payment_status, b.discharge_status, " +
        "  b.primary_claim_status, b.secondary_claim_status, b.app_modified, is_primary_bill, " +
        "  pr.status as visit_status, pr.ready_to_discharge, pr.org_id, pr.bed_type, pr.dept_name, " +
        "  pr.category_id, pr.primary_sponsor_id, pr.secondary_sponsor_id, pr.plan_id, b.is_tpa, ppd.member_id, " +
        "  pr.primary_insurance_co, pr.secondary_insurance_co," +
        "  icmp.insurance_co_name AS primary_insurance_co_name, " +
        "  icms.insurance_co_name AS secondary_insurance_co_name, " +
        "  b.account_group, " +
        "  b.open_date, b.mod_time, b.finalized_date, b.closed_date, b.last_finalized_at, " +
        "  b.opened_by, b.username, b.closed_by, b.finalized_by, u.temp_username, " +
        "  uc.temp_username as closed_by_name, uco.temp_username as opened_by_name, ufo.temp_username as finalized_by_name, " +
        "  b.total_amount, b.total_tax, b.total_claim, b.claim_recd_amount, " +
        "  b.approval_amount, b.primary_approval_amount, b.secondary_approval_amount, " +
        "  b.primary_total_claim, b.secondary_total_claim, b.last_finalized_at," +
        "  b.deposit_set_off, b.points_earned, b.points_redeemed, b.points_redeemed_amt, " +
        "   COALESCE(rps.points_earned,0) as total_points_earned," +
        "   COALESCE(rps.points_redeemed,0) as total_points_redeemed," +
        "   COALESCE(rps.open_points_redeemed,0) as total_open_points_redeemed," +
        "   (COALESCE(rps.points_earned,0) - " +
        "       COALESCE(rps.points_redeemed,0) - " +
        "       COALESCE(rps.open_points_redeemed,0)) as total_points_available," +
        "  b.sponsor_bill_no, b.total_receipts, " +
        "  (COALESCE(b.primary_total_sponsor_receipts,0) + COALESCE(b.secondary_total_sponsor_receipts,0)) AS total_sponsor_receipts, " +
        "  b.primary_total_sponsor_receipts, b.secondary_total_sponsor_receipts, b.insurance_deduction, " +
        "  b.remarks, b.discount_auth, da.disc_auth_name,b.total_discount, " +
        "  b.procedure_no, sp.procedure_code, sp.procedure_name, sp.procedure_limit, " +
        "  b.dyna_package_id, b.dyna_package_charge, NULL AS dyna_pkg_rate_plan_code, " +
        "  NULL AS dyna_package_name, b.bill_rate_plan_id, b.dyna_pkg_processed, " +
        "  b.national_claim_no, b.bill_printed, b.cancel_reason, b.reopen_reason, blm.bill_label_name, " +
        "  pbc.plan_id as primary_plan_id, sbc.plan_id as secondary_plan_id, b.cancellation_approved_by, b.cancellation_approved_date, "+
        "  b.discount_category_id,dcm.discount_plan_name discount_cat_name,0.00 as discount_cat_perc,b.total_tax, b.total_claim_tax "+
        " FROM bill b " +
        " LEFT JOIN bill_claim pbc ON(b.bill_no = pbc.bill_no AND pbc.priority = 1)  "+
        " LEFT JOIN bill_claim sbc ON(b.bill_no = sbc.bill_no AND sbc.priority= 2) "+
        "  LEFT JOIN patient_registration pr on (pr.patient_id = b.visit_id) " +
        "  LEFT JOIN u_user u ON (u.emp_username = b.username) " +
        "  LEFT JOIN u_user uc ON (uc.emp_username = b.closed_by) " +
        "  LEFT JOIN u_user uco ON (uco.emp_username = b.opened_by) " +
        "  LEFT JOIN u_user ufo ON (ufo.emp_username = b.finalized_by) " +
        "  LEFT JOIN discount_authorizer da ON (da.disc_auth_id = b.discount_auth) "+
        "  LEFT JOIN sponsor_procedure_limit sp ON (sp.procedure_no = b.procedure_no) "+
        "  LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pr.patient_policy_id) "+
        "  LEFT JOIN insurance_company_master icmp ON (icmp.insurance_co_id = pr.primary_insurance_co) " +
        "  LEFT JOIN insurance_company_master icms ON (icms.insurance_co_id = pr.secondary_insurance_co)" +
        "  LEFT JOIN bill_label_master blm ON (b.bill_label_id = blm.bill_label_id)" +
        "  LEFT JOIN reward_points_status rps ON (rps.mr_no = pr.mr_no) " +
        "  LEFT JOIN discount_plan_main dcm ON (dcm.discount_plan_id = b.discount_category_id) " +
        " WHERE b.bill_no=? ";
	
	public static BasicDynaBean getBillBeanWithoutDynaPkgCode(String billNo) throws SQLException {
        return DataBaseUtil.queryToDynaBean(BILL_BEAN_WITHOUT_DYNA_PKG_CODE_QUERY, billNo);
    }

	private static void populateBill(Bill bill, ResultSet rs) throws SQLException {
		bill.setBillNo(rs.getString("bill_no"));
		bill.setVisitId(rs.getString("visit_id"));
		bill.setVisitType(rs.getString("visit_type"));
		bill.setBillType(rs.getString("bill_type"));
		bill.setOpenDate(rs.getTimestamp("open_date"));
		bill.setOpenDateTime(rs.getTimestamp("open_date"));
		bill.setModTime(rs.getTimestamp("mod_time"));
		bill.setStatus(rs.getString("status"));
		bill.setPaymentStatus(rs.getString("payment_status"));
		bill.setPrimaryClaimStatus(rs.getString("primary_claim_status"));
		bill.setSecondaryClaimStatus(rs.getString("secondary_claim_status"));
		bill.setClaimRecdAmount(rs.getBigDecimal("claim_recd_amount"));
		bill.setFinalizedDate(rs.getTimestamp("finalized_date"));
		bill.setClosedDate(rs.getTimestamp("closed_date"));
		bill.setOpenedBy(rs.getString("opened_by"));
		bill.setUserName(rs.getString("username"));
		bill.setClosedBy(rs.getString("closed_by"));
		bill.setFinalizedBy(rs.getString("finalized_by"));
		bill.setOkToDischarge(rs.getString("discharge_status"));
		bill.setAppModified(rs.getString("app_modified"));
		bill.setBillRemarks(rs.getString("remarks"));
		bill.setCreditNoteReasons(rs.getString("credit_note_reasons"));
		bill.setWriteOffRemarks(rs.getString("writeoff_remarks"));
		bill.setSpnrWriteOffRemarks(rs.getString("sponsor_writeoff_remarks"));
		bill.setBillDiscountAuth(rs.getInt("discount_auth"));
		bill.setReadyToDischarge(rs.getString("ready_to_discharge"));
		bill.setDepositSetOff(rs.getBigDecimal("deposit_set_off"));
		bill.setApprovalAmount(rs.getBigDecimal("approval_amount"));
		bill.setPrimaryApprovalAmount(rs.getBigDecimal("primary_approval_amount"));
		bill.setSecondaryApprovalAmount(rs.getBigDecimal("secondary_approval_amount"));
		bill.setPrimaryTotalClaim(rs.getBigDecimal("primary_total_claim"));
		bill.setSecondaryTotalClaim(rs.getBigDecimal("secondary_total_claim"));
		bill.setDiscountAuthName(rs.getString("disc_auth_name"));
		bill.setRestrictionType(rs.getString("restriction_type"));
		bill.setTotalAmount(rs.getBigDecimal("total_amount"));
		bill.setTotalDiscount(rs.getBigDecimal("total_discount"));
		bill.setProcedureCode(rs.getString("procedure_code"));
		bill.setProcedureName(rs.getString("procedure_name"));
		bill.setProcedureNo(rs.getInt("procedure_no"));
		bill.setSponsorBillNo(rs.getString("sponsor_bill_no"));
		bill.setTotalReceipts(rs.getBigDecimal("total_receipts"));
		bill.setTotalPrimarySponsorReceipts(rs.getBigDecimal("primary_total_sponsor_receipts"));
		bill.setTotalSecondarySponsorReceipts(rs.getBigDecimal("secondary_total_sponsor_receipts"));
		bill.setInsuranceDeduction(rs.getBigDecimal("insurance_deduction"));
		bill.setDynaPkgId(rs.getInt("dyna_package_id"));
		bill.setDynaPkgCharge(rs.getBigDecimal("dyna_package_charge"));
		bill.setDynaPkgName(rs.getString("dyna_package_name"));
		bill.setIs_tpa(rs.getBoolean("is_tpa"));
		bill.setIsPrimaryBill(rs.getString("is_primary_bill"));
		bill.setAccount_group(rs.getInt("account_group"));
		bill.setBillRatePlanId(rs.getString("bill_rate_plan_id"));
		bill.setBillRatePlanName(rs.getString("org_name"));
		bill.setTotalClaim(rs.getBigDecimal("total_claim"));
		bill.setDynaPkgProcessed(rs.getString("dyna_pkg_processed"));
		bill.setBillPrinted(rs.getString("bill_printed"));
		bill.setCancelReason(rs.getString("cancel_reason"));
		bill.setReopenReason(rs.getString("reopen_reason"));
		bill.setBillLabelId(rs.getInt("bill_label_id"));
		bill.setRewardPointsEarned(rs.getInt("points_earned"));
		bill.setRewardPointsRedeemed(rs.getInt("points_redeemed"));
		bill.setRewardPointsRedeemedAmount(rs.getBigDecimal("points_redeemed_amt"));
		bill.setPrimaryPlanId(rs.getInt("primary_plan_id"));
		bill.setSecondaryPlanId(rs.getInt("secondary_plan_id"));
		bill.setPatientWriteOff(rs.getString("patient_writeoff"));
		bill.setSponsorWriteOff(rs.getString("sponsor_writeoff"));
		bill.setCancellationApprovalStatus(rs.getString("cancellation_approval_status"));
		bill.setBillDiscountCategory(rs.getInt("discount_category_id"));
		bill.setDiscountCategoryName(rs.getString("discount_cat_name"));
		bill.setCancellationApprovedBy(rs.getString("cancellation_approved_by"));
		bill.setIpDepositSetOff(rs.getBigDecimal("ip_deposit_set_off"));
		bill.setPrimaryClosureType(rs.getString("primary_closure_type"));
		bill.setSecondaryClosureType(rs.getString("secondary_closure_type"));
		bill.setPrimaryClaimID(rs.getString("primaryClaimId"));
		bill.setFinancialDisDate(rs.getDate("financial_discharge_date"));
		bill.setFinancialDisTime(rs.getTimestamp("financial_discharge_time"));
		bill.setTotalTax(rs.getBigDecimal("total_tax"));
		bill.setTotalClaimTax(rs.getBigDecimal("total_claim_tax"));
		//KSA Changes
		bill.setCenterId(rs.getInt("center_id"));
		bill.setMrno(rs.getString("mr_no"));
		/* Set discharge to a bill object should not be done in populate bill method.
		 * Commenting this line to fix a bug 42302.
		 * bill.setDischarge(rs.getString("discharge_status"));
		 */
		bill.setIpCreditLimitAmount(rs.getBigDecimal("ip_credit_limit_amount"));
		bill.setLastFinalizedAt(rs.getTimestamp("last_finalized_at"));
		bill.setBillSignature(rs.getString("bill_signature"));
	}

	/*
	 * Return a list of all bills for the patient
	 */
	private static final String GET_PATIENT_BILLS = BILL_QUERY + BILL_QUERY_TABLES + " WHERE b.visit_id=? ";

	public List getPatientBills(String visitId) throws SQLException {
		PreparedStatement ps = con.prepareStatement(GET_PATIENT_BILLS+ " ORDER BY b.open_date ");
		ps.setString(1, visitId);
		ResultSet rs = ps.executeQuery();

		ArrayList list = new ArrayList();
		while (rs.next()) {
			Bill bill = new Bill();
			populateBill(bill, rs);
			list.add(bill);
		}
		rs.close();
		ps.close();
		return list;
	}

	/**
	 * Original methods need a connection,this one will open a new connection
	 * @param visitId
	 * @return
	 * @throws SQLException
	 */
	public List getVisitBills(String visitId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList list = new ArrayList();

		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PATIENT_BILLS);
			ps.setString(1, visitId);
			rs = ps.executeQuery();

			while (rs.next()) {
				Bill bill = new Bill();
				populateBill(bill, rs);
				list.add(bill);
			}
		}finally{
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return list;
	}

	private static final String GET_PATIENTS_CREDIT_BILLS = "SELECT visit_id,bill_type,discharge_status,bill_no " +
		" FROM bill b " +
		"  INNER JOIN patient_registration pr on (b.visit_id = pr.patient_id) " +
		" WHERE pr.visit_type ='i' AND pr.status = 'A'";

	public List getVisitDischargestatus(String patientId)throws SQLException{
		PreparedStatement ps = con.prepareStatement(GET_PATIENTS_CREDIT_BILLS+"" +
				" and b.visit_id = ? and b.restriction_type='N'	GROUP BY visit_id,bill_type,discharge_status,bill_no");
		ps.setString(1, patientId);
		ResultSet rs = ps.executeQuery();
		ArrayList list = new ArrayList();
		while (rs.next()) {
			Hashtable hashtable = new Hashtable();
			hashtable.put("VISIT_ID", rs.getString(1));
			hashtable.put("BILL_TYPE", rs.getString(2));
			hashtable.put("DISCHARGE_STATUS", rs.getString(3));
			hashtable.put("BILL_NO", rs.getString(4));
			list.add(hashtable);
		}
		rs.close();
		ps.close();
		return list;
	}

	private static final String CREDIT_BILLS = BILL_QUERY + BILL_QUERY_TABLES
		+ " WHERE bill_type = 'C' AND  b.status != 'X' AND b.bill_type='C' AND b.restriction_type='N'";
 	public List getCreditBills()throws SQLException{
 		PreparedStatement ps = null;
 		try{
 			ps = con.prepareStatement(CREDIT_BILLS);
 			return DataBaseUtil.queryToArrayList(ps);
 		}finally{
 			if(ps != null)ps.close();
 		}
 	}

	/*
	 * Return the current Credit bill for a patient (visit ID).
	 * If no credit bill that is not cancelled, returns null. Can return a bill even
	 * if the patient is inactive.
	 */
	private static final String GET_VISIT_CREDIT_BILL = BILL_QUERY + BILL_QUERY_TABLES +
		" WHERE b.bill_type='C' AND b.status!='X' AND b.restriction_type='N' AND b.visit_id=? " +
		"  AND is_primary_bill = 'Y'";

	private static final String GET_VISIT_OPEN_CREDIT_BILL = BILL_QUERY + BILL_QUERY_TABLES +
		" WHERE b.bill_type='C' AND b.status='A' AND b.restriction_type='N' AND b.visit_id=? " +
		"  AND is_primary_bill = 'Y'";

	public static Bill getVisitCreditBill(String patientId, boolean onlyOpenBill) throws SQLException {
		return getVisitCreditBill(null, patientId, onlyOpenBill);
	}

	public static Bill getVisitCreditBill(Connection con, String patientId, boolean onlyOpenBill)
		throws SQLException {
		boolean newConnection = (con == null);
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if (newConnection) {
				con = DataBaseUtil.getReadOnlyConnection();
			}

			if (onlyOpenBill)
				ps = con.prepareStatement(GET_VISIT_OPEN_CREDIT_BILL);
			else
				ps = con.prepareStatement(GET_VISIT_CREDIT_BILL);

			ps.setString(1, patientId);
			rs = ps.executeQuery();

			Bill bill = null;
			if (rs.next()) {
				bill = new Bill();
				populateBill(bill, rs);
			}
			return bill;
		} finally {
			if (newConnection) {
				DataBaseUtil.closeConnections(con,ps,rs);
			}
		}
	}

	/*
	 * Get the MR number, given a bill number
	 */
	private static final String GET_MR_NO = "SELECT mr_no FROM patient_registration "
		+ " JOIN bill ON bill.visit_id = patient_registration.patient_id "
		+ " WHERE bill.bill_no = ?";

	public String getMrNo(String billNo) throws SQLException {
		PreparedStatement ps = con.prepareStatement(GET_MR_NO);
		ps.setString(1, billNo);

		ResultSet rs = ps.executeQuery();
		String mrNo = null;
		if (rs.next()) {
			mrNo = rs.getString(1);
		}
		rs.close();
		ps.close();
		return mrNo;
	}

	/*
	 * Get the total of all approval amounts in all bills for a given visit
	 */
/*	private static final String GET_TOTAL_APPROVAL =
		" SELECT SUM(COALESCE(approval_amount,0)) FROM bill WHERE visit_id IN " +
		" (SELECT patient_id FROM  patient_registration WHERE main_visit_id = " +
		" (SELECT main_visit_id FROM patient_registration WHERE patient_id = ? )) AND is_tpa ";
*/
	private static final String GET_TOTAL_APPROVAL =
		" SELECT SUM(COALESCE(approval_amount,0)) FROM bill WHERE visit_id IN " +
		" (SELECT patient_id FROM patient_registration WHERE patient_id = ?) AND is_tpa ";

	public static BigDecimal getBillApprovalAmountsTotal(String visitId) throws SQLException {
		Connection localCon = DataBaseUtil.getConnection();
		PreparedStatement localPs = localCon.prepareStatement(GET_TOTAL_APPROVAL);
		localPs.setString(1, visitId);
		BigDecimal retVal = DataBaseUtil.getBigDecimalValueFromDb(localPs);
		localPs.close(); localCon.close();
		return retVal;
	}
	
	public static BigDecimal getBillApprovalAmountsTotal(Connection localCon, String visitId) throws SQLException {
	  BigDecimal retVal;
	  
    try(PreparedStatement localPs = localCon.prepareStatement(GET_TOTAL_APPROVAL);){
      localPs.setString(1, visitId);
      retVal = DataBaseUtil.getBigDecimalValueFromDb(localPs);
    }
    return retVal;
  }

	/*
	 * Create a new bill.
	 */
	private static final String CREATE_BILL = "INSERT INTO bill " +
		" (bill_no, visit_id, visit_type, bill_type, open_date, mod_time, status, payment_status, " +
		"   finalized_date, closed_date, opened_by, username, discharge_status, " +
		"   app_modified, closed_by, finalized_by, approval_amount, " +
		"	primary_approval_amount, secondary_approval_amount, " +
		"	primary_total_claim, secondary_total_claim," +
		"	restriction_type, account_group, deposit_set_off, ip_deposit_set_off, " +
		"   insurance_deduction, is_tpa, primary_claim_status, secondary_claim_status, " +
		"	is_primary_bill,bill_rate_plan_id,discount_category_id, last_finalized_at, locked)" +
		" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public boolean createBill(Bill bill) throws SQLException {
		PreparedStatement ps = con.prepareStatement(CREATE_BILL);
		int i = 1;
		ps.setString(i++, bill.getBillNo());
		ps.setString(i++, bill.getVisitId());
		ps.setString(i++, bill.getVisitType());
		ps.setString(i++, bill.getBillType());
		ps.setTimestamp(i++, new Timestamp(bill.getOpenDate().getTime()));
		ps.setTimestamp(i++, new Timestamp(bill.getModTime().getTime()));
		ps.setString(i++, bill.getStatus());
		ps.setString(i++, bill.getPaymentStatus());

		if (null != bill.getFinalizedDate()) {
			ps.setTimestamp(i++, new Timestamp(bill.getFinalizedDate().getTime()));
		} else {
			ps.setTimestamp(i++, null);
		}
		if (null != bill.getClosedDate()) {
			ps.setTimestamp(i++, new Timestamp(bill.getClosedDate().getTime()));
		} else {
			ps.setTimestamp(i++, null);
		}

		ps.setString(i++, bill.getOpenedBy());
		ps.setString(i++, bill.getUserName());
		ps.setString(i++, bill.getOkToDischarge());
		ps.setString(i++, bill.getAppModified());
		ps.setString(i++, bill.getClosedBy());
		ps.setString(i++, bill.getFinalizedBy());
		ps.setBigDecimal(i++, bill.getApprovalAmount());
		ps.setBigDecimal(i++,bill.getPrimaryApprovalAmount());
		ps.setBigDecimal(i++,bill.getSecondaryApprovalAmount());
		ps.setBigDecimal(i++,bill.getPrimaryTotalClaim()==null?
				BigDecimal.ZERO:bill.getPrimaryTotalClaim());
		ps.setBigDecimal(i++,bill.getSecondaryTotalClaim()==null?
				BigDecimal.ZERO:bill.getSecondaryTotalClaim());
		ps.setString(i++, bill.getRestrictionType());
		ps.setInt(i++, bill.getAccount_group() > 0 ? bill.getAccount_group() : 1);
		ps.setBigDecimal(i++, bill.getDepositSetOff());
		ps.setBigDecimal(i++, bill.getIpDepositSetOff());
		ps.setBigDecimal(i++,bill.getInsuranceDeduction()==null?
				BigDecimal.ZERO:bill.getInsuranceDeduction());
		ps.setBoolean(i++, bill.getIs_tpa());
		ps.setString(i++, bill.getPrimaryClaimStatus());
		ps.setString(i++, bill.getSecondaryClaimStatus());
		ps.setString(i++, bill.getIsPrimaryBill());
		ps.setString(i++, bill.getBillRatePlanId());
		ps.setInt(i++, bill.getBillDiscountCategory());
		if (null != bill.getLastFinalizedAt()) {
			ps.setTimestamp(i++, new Timestamp(bill.getLastFinalizedAt().getTime()));
		} else {
			ps.setTimestamp(i++, null);
		}
		ps.setBoolean(i++, bill.getLocked());

		int count = ps.executeUpdate();
		ps.close();
		return (count == 1);
	}

	/*
	 * Update a bill (the bill alone, no details)
	 */
	private static final String UPDATE_BILL = "UPDATE bill SET " +
		"  visit_id=?, visit_type=?, bill_type=?, status=?, payment_status=?, mod_time=?, " +
		"  primary_claim_status=?,  secondary_claim_status=?, bill_signature=?, "+
		"  claim_recd_amount=?, finalized_date=?, closed_date=?,  discharge_status=?, app_modified=?, " +
		"  remarks=?, discount_auth=?, closed_by=?, finalized_by=?, username=?, deposit_set_off=? ," +
		"  approval_amount=?, primary_approval_amount=?, secondary_approval_amount=?, " +
		"  primary_total_claim=?, secondary_total_claim=?," +
		"  procedure_no=?, insurance_deduction=?, dyna_package_id=?, dyna_package_charge=?, is_tpa=?, " +
		"  bill_rate_plan_id = ?, dyna_pkg_processed=?, bill_printed=?, cancel_reason=?, reopen_reason=?," +
		"  bill_label_id=?, points_redeemed = ?, points_redeemed_amt= ?, writeoff_remarks = ?, " +
		"  sponsor_writeoff_remarks = ?, discount_category_id = ?,  ip_deposit_set_off = ?,open_date = ?,sponsor_writeoff=?, " +
		"  last_finalized_at = ? WHERE bill_no=?";

	public boolean updateBill(Bill bill) throws SQLException {
		PreparedStatement ps=null;
		int count=0;
		ps = con.prepareStatement(UPDATE_BILL);

		int i = 1;
		ps.setString(i++, bill.getVisitId());
		ps.setString(i++, bill.getVisitType());
		ps.setString(i++, bill.getBillType());
		ps.setString(i++, bill.getStatus());
		ps.setString(i++, bill.getPaymentStatus());
		ps.setTimestamp(i++, new Timestamp(bill.getModTime().getTime()));
		ps.setString(i++, bill.getPrimaryClaimStatus());
		ps.setString(i++, bill.getSecondaryClaimStatus());
		ps.setString(i++, bill.getBillSignature());
		ps.setBigDecimal(i++, bill.getClaimRecdAmount());

		if (null != bill.getFinalizedDate()) {
			ps.setTimestamp(i++, new Timestamp(bill.getFinalizedDate().getTime()));
		} else {
			ps.setTimestamp(i++, null);
		}
		if (null != bill.getClosedDate()) {
			ps.setTimestamp(i++, new Timestamp(bill.getClosedDate().getTime()));
		} else {
			ps.setTimestamp(i++, null);
		}

		ps.setString(i++, bill.getOkToDischarge());
		ps.setString(i++, bill.getAppModified());
		ps.setString(i++, bill.getBillRemarks());
		ps.setInt(i++, bill.getBillDiscountAuth());
		ps.setString(i++, bill.getClosedBy());
		ps.setString(i++, bill.getFinalizedBy());
		ps.setString(i++, bill.getUserName());
		ps.setBigDecimal(i++, bill.getDepositSetOff());
		ps.setBigDecimal(i++, bill.getApprovalAmount());
		ps.setBigDecimal(i++,bill.getPrimaryApprovalAmount());
		ps.setBigDecimal(i++,bill.getSecondaryApprovalAmount());
		ps.setBigDecimal(i++,bill.getPrimaryTotalClaim()==null?
				BigDecimal.ZERO:bill.getPrimaryTotalClaim());
		ps.setBigDecimal(i++,bill.getSecondaryTotalClaim()==null?
				BigDecimal.ZERO:bill.getSecondaryTotalClaim());
		ps.setInt(i++, bill.getProcedureNo());
		ps.setBigDecimal(i++, bill.getInsuranceDeduction());
		ps.setInt(i++, bill.getDynaPkgId());
		ps.setBigDecimal(i++, bill.getDynaPkgCharge());
		ps.setBoolean(i++, bill.getIs_tpa());
		ps.setString(i++, bill.getBillRatePlanId());
		ps.setString(i++, bill.getDynaPkgProcessed());
		ps.setString(i++, bill.getBillPrinted());
		ps.setString(i++, bill.getCancelReason());
		ps.setString(i++, bill.getReopenReason());
		if (bill.getBillLabelId() != -1) {
			ps.setInt(i++, bill.getBillLabelId());
		} else {
			ps.setObject(i++, null);
		}

		ps.setInt(i++, bill.getRewardPointsRedeemed());
		ps.setBigDecimal(i++, bill.getRewardPointsRedeemedAmount());
		ps.setString(i++, bill.getWriteOffRemarks());
		ps.setString(i++, bill.getSpnrWriteOffRemarks());
		ps.setInt(i++, bill.getBillDiscountCategory());
		ps.setBigDecimal(i++, bill.getIpDepositSetOff());
		java.util.Date openDate = bill.getOpenDate();
		if(openDate.getTime() < 0) {
		  logger.error("Bill open date is before 1970 ");
		  return false;
		} 
		if(openDate.after(DateUtil.getCurrentTimestamp())) {
		  logger.error("Bill open date is in future ");
      return false;
		}
		ps.setTimestamp(i++, new Timestamp(bill.getOpenDate().getTime()));
		if(null != bill.getSponsorWriteOff() && !bill.getSponsorWriteOff().equals(""))
			ps.setString(i++, bill.getSponsorWriteOff());
		else
			ps.setString(i++, "N");

		if (null != bill.getLastFinalizedAt()) {
			ps.setTimestamp(i++, new Timestamp(bill.getLastFinalizedAt().getTime()));
		} else {
			ps.setTimestamp(i++, null);
		}
		// where bill_no=?
		ps.setString(i++, bill.getBillNo());
		count = ps.executeUpdate();

		ps.close();

		return (count == 1);
	}

	private static final String UPDATE_PRIMARY =
		" UPDATE bill SET is_primary_bill=? WHERE bill_no=?";

	public void updatePrimaryBill(String billNo, String primary) throws SQLException {
		PreparedStatement ps = con.prepareStatement(UPDATE_PRIMARY);
		ps.setString(1, primary);
		ps.setString(2, billNo);
		ps.executeUpdate();
		ps.close();
	}

	/*
	 * Set the primary flag if the visit has no other credit bills which are primary
	 */
	private static final String SET_PRIMARY_CONDITIONAL =
		" UPDATE bill b SET is_primary_bill='Y' " +
		" WHERE bill_no=? " +
		"   AND NOT EXISTS (SELECT bill_no FROM bill b2 WHERE b2.visit_id=b.visit_id " +
		"     AND b2.bill_no != b.bill_no AND b2.bill_type='C' AND b2.is_primary_bill='Y')";

	public int setPrimaryBillConditional(String billNo) throws SQLException {
		PreparedStatement ps = con.prepareStatement(SET_PRIMARY_CONDITIONAL);
		ps.setString(1, billNo);
		int count = ps.executeUpdate();
		ps.close();
		return count;
	}

	private static final String UPDATE_PAYMENT_STATUS =
		" UPDATE bill SET payment_status=?,discharge_status = ? WHERE bill_no=?";

	public void updatePaymentStatus(String billNo, String paymentStatus,String dischargeStatus) throws SQLException {
		PreparedStatement ps = con.prepareStatement(UPDATE_PAYMENT_STATUS);
		ps.setString(1, paymentStatus);
		ps.setString(2, dischargeStatus);
		ps.setString(3, billNo);
		ps.executeUpdate();
		ps.close();
	}

	private static final String UPDATE_BILL_CLAIM_STATUS = "UPDATE bill SET " +
			" primary_claim_status=?, secondary_claim_status=? WHERE bill_no=? ";

	public void updateBillClaimStatus(String billNo, String primaryClaimStatus, String secondaryClaimStatus)
			throws SQLException {
		PreparedStatement stmt = con.prepareStatement(UPDATE_BILL_CLAIM_STATUS);
		stmt.setString(1, primaryClaimStatus);
		stmt.setString(2, secondaryClaimStatus);
		stmt.setString(3, billNo);
		stmt.execute();
		stmt.close();
	}


	/* This method will return  credit bill  number
	 * if u pass visitid*/

	private static final String GET_CREDIT_BILL = "SELECT bill_no FROM bill "
		+ " WHERE visit_id=? AND bill_type='C' AND status != 'X' AND restriction_type='N' ";

	public String getPatientCreditBillOpenOnly(String visitId, boolean openOnly, boolean unpaidBills) throws SQLException{
		String query = GET_CREDIT_BILL;
		if (openOnly) {
			query += " AND status='A' ";
		}
		if (unpaidBills) {
			query += " AND payment_status='U'";
		}
		PreparedStatement ps = con.prepareStatement(query);
		ResultSet rs = null;
		String billNo = null;
		ps.setString(1, visitId);
		rs = ps.executeQuery();
		if (rs.next()) {
			billNo = rs.getString(1);
		}
		rs.close();
		ps.close();
		return billNo;
	}
	public String getPatientCreditBillNo(String visitId) throws SQLException {
		return getPatientCreditBillOpenOnly(visitId, false, false);
	}

	/*
	 * Get whether, based on the bills, the visit is OK to discharge. It is OK as long
	 * as there are no pending bills. A pending bill is one where the status is
	 * not indicated as OK to discharge, or it is not settled, closed or cancelled.
	 */
	private static final String GET_DISCHARGE_NOTOK_BILLS =
		"SELECT bill_no FROM bill " +
		" WHERE discharge_status != 'Y'  AND visit_id=? AND status != 'X' ";

	public boolean getOkToDischarge(String patientId) throws SQLException {
		PreparedStatement ps = con.prepareStatement(GET_DISCHARGE_NOTOK_BILLS);
		ps.setString(1, patientId);
		ResultSet rs = ps.executeQuery();

		boolean result = true;
		if (rs.next()) {
			result = false;
		}
		rs.close();
		ps.close();
		return result;
	}

	/*
	 * Search function over the bills: extended information includes MRno, patient name etc.
	 * WARNING: this is a static function that gets its own connection, and can be called
	 * directly from Action classes, without a BO wrapper function.
	 */
	private static final String BILL_EXT_QUERY_FIELDS ="SELECT * ";

	private static final String BILL_EXT_QUERY_COUNT =
		" SELECT count(bill_no)  ";

	private static final String BILL_EXT_QUERY_TABLES =
			" FROM (SELECT b.bill_no, b.visit_id, b.visit_type, b.status, p.original_mr_no, " +
					"  CASE WHEN bill_type = 'C' THEN 'C' ELSE 'P' END as bill_type, bill_type AS actual_bill_type, " +
					"  b.open_date, b.mod_time, b.finalized_date, b.last_finalized_at, b.closed_date, b.opened_by, b.username, b.closed_by, b.finalized_by, " +
					"  b.discharge_status, b.app_modified, b.claim_recd_amount,b.patient_writeoff, b.sponsor_writeoff, " +
					"  b.total_amount, b.total_discount, b.total_claim, total_receipts, b.total_tax, b.total_claim_tax, p.mr_no, " +
					"  CASE WHEN (b.total_amount < 0) THEN 'Y' ELSE 'N' END AS creditnote, "+
					"  coalesce(get_patient_name(p.salutation, p.patient_name, p.middle_name, p.last_name), " +
					"    isr.patient_name, phc.customer_name) as patient_name, COALESCE(p.patient_group,0) as patient_group, " +
					"  coalesce(p.dateofbirth, expected_dob) as dob, p.patient_gender, pr.reg_date, pr.reg_time, " +
					"  get_patient_age(p.dateofbirth, expected_dob) as patient_age, pr.mlc_status,p.vip_status, " +
					"  CASE WHEN current_date - COALESCE(p.dateofbirth, p.expected_dob) < 31 THEN 'D'  "+
					"  WHEN (current_date - COALESCE(p.dateofbirth, p.expected_dob) < 730) THEN 'M'  ELSE 'Y' END  AS patient_age_in,"+
					"  CASE WHEN  pr.primary_sponsor_id= '' THEN null ELSE pr.primary_sponsor_id END AS primary_sponsor_id, " +
					"  CASE WHEN  pr.secondary_sponsor_id= '' THEN null ELSE pr.secondary_sponsor_id END AS secondary_sponsor_id, " +
					"	tpap.tpa_name AS primary_tpa_name, tpas.tpa_name AS secondary_tpa_name, b.remarks, " +
					"  b.discount_auth, pr.ready_to_discharge, pr.discharge_date, " +
					"  pr.status as visit_status, b.restriction_type, pr.org_id, od.org_name, " +
					"  (COALESCE(total_amount,0)-COALESCE(total_receipts,0)) AS adv_to_be_collected, " +
					"  b.mod_time::date as cl_date, " +
					"  CASE WHEN (b.sponsor_bill_no IS NOT NULL AND b.sponsor_bill_no != '') THEN 'Y' ELSE 'N' " +
					"   END as sponsor_bill_no," +
					"	 CASE WHEN coalesce(pic.last_submission_batch_id, sic.last_submission_batch_id, '') != '' THEN 'Y'  ELSE  'N' END as submission_batch_id," +
					"  CASE WHEN (pic.status IS NOT NULL AND pic.status != '' AND pic.status IN ('O','B','M','C'))  " +
					"  THEN pic.status ELSE 'N' END as primary_claim_status," +
					"	CASE WHEN (sic.status IS NOT NULL AND sic.status != '' AND sic.status IN ('O','B','M','C'))  " +
					"  THEN sic.status ELSE 'N' END as secondary_claim_status," +
					"  b.payment_status, " +
					"  bn.bed_type, bn.bed_name, wn.ward_name, doc.doctor_name, dep.dept_name, " +
					"  pr.plan_id, pr.category_id, b.is_tpa, pr.op_type,  " +
					"	pr.primary_insurance_co, pr.secondary_insurance_co, " +
					"	coalesce(pr.center_id, isr.center_id, phc.center_id) as center_id, b.bill_printed, blm.bill_label_id," +
					"  b.remarks, blm.bill_label_name,pr.collection_center_id, primary_total_sponsor_receipts, " +
					"  secondary_total_sponsor_receipts, primary_total_claim, secondary_total_claim," +
					"  deposit_set_off, points_redeemed_amt, pbc.claim_id as pri_claim_id, sbc.claim_id as sec_claim_id, " +
					"  pr.patient_discharge_status " +
					"  FROM bill b " +
					"  JOIN organization_details od on (od.org_id = b.bill_rate_plan_id) " +
					"  LEFT JOIN patient_registration pr on (b.visit_id = pr.patient_id) " +
					"  LEFT JOIN store_retail_customers phc on (b.visit_id = phc.customer_id) " +
					"  LEFT JOIN patient_details p on (pr.mr_no = p.mr_no) " +
					"  LEFT JOIN tpa_master tpap on (tpap.tpa_id = pr.primary_sponsor_id) " +
					"  LEFT JOIN tpa_master tpas on (tpas.tpa_id = pr.secondary_sponsor_id) " +
					"  LEFT JOIN incoming_sample_registration isr on (isr.incoming_visit_id = b.visit_id) " +
					"  LEFT JOIN admission adm ON (adm.patient_id = b.visit_id)" +
					"  LEFT JOIN bed_names bn ON (bn.bed_id = adm.bed_id) "+
					"  LEFT JOIN ward_names wn ON (wn.ward_no = bn.ward_no) "+
					"  LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor) "+
					"  LEFT JOIN department dep ON (dep.dept_id = pr.dept_name) " +
					"  LEFT JOIN bill_label_master blm ON (blm.bill_label_id = b.bill_label_id) " +
					"  LEFT JOIN bill_claim pbc ON (pbc.bill_no = b.bill_no and pbc.priority = 1) " +
					"  LEFT JOIN insurance_claim pic ON (pic.claim_id = pbc.claim_id) " +
					"  LEFT JOIN bill_claim sbc ON (sbc.bill_no = b.bill_no and sbc.priority = 2) " +
					"  LEFT JOIN insurance_claim sic ON (sic.claim_id = sbc.claim_id) " +
					" ) AS foo";

	public static PagedList searchBillsExtended(String claimId, Map filter, Map listing)
		throws SQLException, ParseException {

		Connection con = null;
		PagedList list = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb = null;

			if(claimId != null && !claimId.equals("")){
			  claimId = InputValidator.getSafeString("claimId", claimId, 18, false);
				String initWhere = " WHERE pri_claim_id = '"+claimId+"' OR sec_claim_id = '"+claimId+"'";
				qb = new SearchQueryBuilder(con,
						BILL_EXT_QUERY_FIELDS, BILL_EXT_QUERY_COUNT, BILL_EXT_QUERY_TABLES, initWhere, listing);
			}else{
				qb = new SearchQueryBuilder(con,
						BILL_EXT_QUERY_FIELDS, BILL_EXT_QUERY_COUNT, BILL_EXT_QUERY_TABLES, listing);
			}

			qb.addFilterFromParamMap(filter);
			qb.appendToQuery("patient_confidentiality_check(foo.patient_group,foo.mr_no)");
			/*
			 * Always add a secondary sort on the primary key. This ensures that the order of the
			 * result is always predictable, which is required for pagination using LIMIT and OFFSET.
			 * We need to add this even if the user has selected any sort order, since that sort order
			 * may not be a unique field. Sorting on the primary key is usually fast.
			 */
			qb.addSecondarySort("bill_no");
			qb.build();

			list = qb.getMappedPagedList();
			qb.close();
		} catch (ValidationException valExp) {
      logger.error("Exception: "+valExp.getMessage());
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
		return list;
	}

	public static List getAllOpenAndFinalizedUnPaidBills(String mrNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			String query = BILL_EXT_QUERY_FIELDS + BILL_EXT_QUERY_TABLES ;
			query += " WHERE (payment_status = 'U') AND (status IN ('A','F')) AND (mr_no = ?) ORDER BY open_date DESC , bill_no";
			ps = con.prepareStatement(query);
			ps.setString(1, mrNo);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public List getAllBills(String patientid) throws SQLException {
		PreparedStatement ps = null;
		List bills = new ArrayList<Bill>();
		ResultSet rs = null;
		try{
			ps = con.prepareStatement(GET_PATIENT_BILLS+" and b.status not in('A','X')");
			ps.setString(1, patientid);
			rs = ps.executeQuery();
			while (rs.next()) {
				Bill bill = new Bill();
				populateBill(bill, rs);
				bills.add(bill);
			}
		}finally{
			if(ps != null)ps.close();
		}
		return bills;
	}

	public List getAllBills(Connection con, String patientid) throws SQLException {
		PreparedStatement ps = null;
		List bills = new ArrayList<Bill>();
		ResultSet rs = null;
		try{
			ps = con.prepareStatement(GET_PATIENT_BILLS+" and b.status not in('A','X')");
			ps.setString(1, patientid);
			rs = ps.executeQuery();
			while (rs.next()) {
				Bill bill = new Bill();
				populateBill(bill, rs);
				bills.add(bill);
			}
		}finally{
			if(ps != null)ps.close();
		}
		return bills;
	}

	private static final String GET_BILL_AMOUNTS =
		"SELECT bill_no, total_amount, total_discount, total_claim, total_claim_return, insurance_deduction, " +
		" deposit_set_off, points_redeemed_amt, total_receipts, total_tax, total_claim_tax, " +
		" primary_total_sponsor_receipts, secondary_total_sponsor_receipts," +
		" claim_recd_amount, total_claim_return, primary_total_claim, secondary_total_claim " +
		" FROM bill WHERE bill_no=?";

	public BasicDynaBean getBillAmounts(String billNo) throws SQLException {
		PreparedStatement ps = con.prepareStatement(GET_BILL_AMOUNTS);
		ps.setString(1, billNo);
		return DataBaseUtil.queryToDynaBean(ps);
	}

	private static final String ALL_BILLS_TOTALS =
		"SELECT (sum(total_amount)-sum(total_claim)+sum(insurance_deduction)) " +
		" 			- (sum(total_receipts) +sum(deposit_set_off)+sum(points_redeemed_amt)) FROM bill b " +
		" WHERE bill_no in " +
		"		(select bill_no from bill_claim bc where claim_id in " +
		"			(select claim_id from bill_claim WHERE bill_no = ?)) ";

	public static boolean isAllReturnBillsTotalsOK(Connection con, String billNo) throws SQLException {
		PreparedStatement ps = con.prepareStatement(ALL_BILLS_TOTALS);
		ps.setString(1, billNo);
		BigDecimal totals = DataBaseUtil.getBigDecimalValueFromDb(ps);
		totals = (totals) == null ? BigDecimal.ZERO : totals;
		if (ps != null) ps.close();
		return (totals.compareTo(BigDecimal.ZERO) == 0);
	}

	private static final String CLAIM_ALL_BILLS_TOTALS =
		"SELECT (sum(total_amount)-sum(total_claim)+sum(insurance_deduction)) " +
		" 			- (sum(total_receipts) +sum(deposit_set_off)+sum(points_redeemed_amt)) FROM bill b " +
		" JOIN bill_claim bc ON (b.bill_no = bc.bill_no) WHERE bc.claim_id = ? ";

	public static boolean isClaimAllBillsTotalsOK(Connection con, String claim_id) throws SQLException {
		PreparedStatement ps = con.prepareStatement(CLAIM_ALL_BILLS_TOTALS);
		ps.setString(1, claim_id);
		BigDecimal totals = DataBaseUtil.getBigDecimalValueFromDb(ps);
		totals = (totals) == null ? BigDecimal.ZERO : totals;
		
		/*List<BasicDynaBean> billList = new GenericDAO("bill_claim").findAllByKey("claim_id", claim_id);
		
		for(BasicDynaBean bill : billList){
			String billNo = (String)bill.get("bill_no");
			BasicDynaBean creditNoteBean = new BillDAO().getCreditNoteDetails(con, billNo);
			if(null != creditNoteBean){
				BigDecimal sponsorCreditNoteAmt = (BigDecimal)creditNoteBean.get("total_claim");
				totals = totals.subtract(sponsorCreditNoteAmt);
			}
		}*/
		
		if (ps != null) ps.close();
		return (totals.compareTo(BigDecimal.ZERO) == 0);
	}

	public static boolean isClaimAllBillsTotalsOK(String claim_id) throws SQLException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			return isClaimAllBillsTotalsOK(con, claim_id);
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String GET_OTHER_UNPAID_BILL_NOS =
		" SELECT bill_no FROM bill " +
		" WHERE visit_id=? AND bill_no!=? AND payment_status='U' AND status!='X' ";

	public static final List<BasicDynaBean> getOtherUnpaidBillNos(String visitId, String billNo)
		throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_OTHER_UNPAID_BILL_NOS, visitId, billNo);
	}
	
	public static final List<BasicDynaBean> getOtherUnpaidBillNos(Connection con, String visitId, String billNo)
	    throws SQLException {
	    return DataBaseUtil.queryToDynaList(con, GET_OTHER_UNPAID_BILL_NOS, new Object[] {visitId, billNo});
	  }

	public static String getVisitId(String strBillNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		String billNo = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("SELECT VISIT_ID FROM BILL WHERE BILL_NO=?");
			ps.setString(1, strBillNo);
			billNo = DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return billNo;
	}

	private static final String UPDATE_BILL_STATUS = "UPDATE bill SET "
		+ " status=?, discharge_status=?, finalized_date=?, finalized_by = ?, "
		+ " closed_date=?, mod_time=?, closed_by=?, payment_status=?, username=?, last_finalized_at=? WHERE bill_no=?";

	public boolean updateBillStatus(String userId, String billNo, String billStatus, String paymentStatus, String disChargeStatus,
			Timestamp finalizedDate, Timestamp closedDate, String closedBy, String finalizedBy, Timestamp lastFinalizedAt)
	throws SQLException {

		PreparedStatement ps = con.prepareStatement(UPDATE_BILL_STATUS);
		int count = 0;
		boolean status = false;
		int i=1;
		ps.setString(i++,billStatus );
		ps.setString(i++, disChargeStatus);
		ps.setTimestamp(i++, finalizedDate);
		ps.setString(i++, finalizedBy);
		ps.setTimestamp(i++, closedDate);
		ps.setTimestamp(i++, new Timestamp(new java.util.Date().getTime()));
		ps.setString(i++, closedBy);
		ps.setString(i++, paymentStatus);
		ps.setString(i++, userId);
    ps.setTimestamp(i++, lastFinalizedAt);

		ps.setString(i++,billNo );

		count = ps.executeUpdate();
		if (count > 0)
			status = true;

		ps.close();
		return status;
	}


	private static final String UPDATE_BILL_FINALIZED_DATES = "UPDATE bill SET finalized_date=?, "
	  + " mod_time=?, last_finalized_at = ? WHERE bill_no=?";

	public static boolean updateVisitBillsFinalizedDates(Connection con, List billList, String disDate,
			String disTime)
	throws SQLException, ParseException {

		PreparedStatement ps = con.prepareStatement(UPDATE_BILL_FINALIZED_DATES);
		boolean success = true;
		int i = 1;
		String timestamp = disDate + " " + disTime;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");
		java.util.Date parsedDate = (java.util.Date) dateFormat.parse(timestamp);
		java.sql.Timestamp datetime = new java.sql.Timestamp(parsedDate.getTime());
		java.sql.Timestamp now = new Timestamp(new java.util.Date().getTime());

		Iterator iterator = billList.iterator();
		while (iterator.hasNext()) {
			Bill billobj = (Bill) iterator.next();
			i = 1;
			ps.setTimestamp(i++, new Timestamp(datetime.getTime()));
			ps.setTimestamp(i++, now);
			ps.setTimestamp(i++, now);
			ps.setString(i++, billobj.getBillNo());
			ps.addBatch();
		}

		int results[] = ps.executeBatch();

		ps.close();
		for (int p = 0; p < results.length; p++) {
			if (results[p] <= 0) {
				success = false;
				logger.error("Error updating Finalized Dates: " + p);
				break;
			}
		}
		return success;
	}
	private static final String GET_CHAGELIST_HAVING_ACTIVITY = "select bc.*,bac.activity_id,bac.charge_head" +
    		" from bill_charge bc join  bill_activity_charge bac on bac.charge_id = bc.charge_id"+
    		" and bc.bill_no = ? ";

    public static List<BasicDynaBean> getChargeListForBillHavingActivity(String billNo)throws SQLException{
    	List<BasicDynaBean> al = null;
    	Connection con = null;
    	PreparedStatement ps = null;
    	try{
    		con =DataBaseUtil.getReadOnlyConnection();
    		ps = con.prepareStatement(GET_CHAGELIST_HAVING_ACTIVITY);
    		ps.setString(1,billNo);
    		al = DataBaseUtil.queryToDynaList(ps);

    	}finally{
    		DataBaseUtil.closeConnections(con, ps);
    	}
    	return al;
    }

	public static Bill getBillTypeAndVisitType(String billNo) throws SQLException {
		Connection con = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	Bill bill = null;
    	try{
    		con =DataBaseUtil.getReadOnlyConnection();
    		ps = con.prepareStatement(GET_BILL);
    		ps.setString(1, billNo);
    		rs = ps.executeQuery();

    		if (rs.next()) {
    			bill = new Bill();
    			populateBillnVisitType(bill, rs);
    		}
    	}finally{
    		DataBaseUtil.closeConnections(con, ps, rs);
    	}
		return bill;
	}


	public static Bill getBillTypeAndVisitType(Connection con, String billNo) throws SQLException {
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	Bill bill = null;
    	try{
    		ps = con.prepareStatement(GET_BILL);
    		ps.setString(1, billNo);
    		rs = ps.executeQuery();

    		if (rs.next()) {
    			bill = new Bill();
    			populateBillnVisitType(bill, rs);
    		}
    	}finally{
    		DataBaseUtil.closeConnections(null, ps, rs);
    	}
		return bill;
	}

	private static void populateBillnVisitType(Bill bill, ResultSet rs) throws SQLException {
		bill.setBillNo(rs.getString("bill_no"));
		bill.setVisitId(rs.getString("visit_id"));
		bill.setVisitType(rs.getString("visit_type"));
		bill.setBillType(rs.getString("bill_type"));
		if ((MedicineStockDAO.doesColumnExist(rs, "is_tpa"))){
			bill.setIs_tpa(rs.getBoolean("is_tpa"));
		}
	}

	/*private static final String BILL_STATUS_COUNT = "SELECT status,count(*) as count FROM bill WHERE visit_id = ? GROUP BY status";

	public static Map getVisitBillsStatusCountMap(String visitId) throws SQLException {
		List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(BILL_STATUS_COUNT, visitId);
		Map m = ConversionUtils.listBeanToMapNumeric(l, "status", "count");
		return m;
	}

	private static final String BILL_PAID_STATUS_COUNT = "SELECT payment_status,count(*) as count FROM bill " +
			" WHERE visit_id = ? and status != 'X' GROUP BY payment_status";

	public static Map getVisitBillsPaymentStatusCountMap(String visitId) throws SQLException {
		List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(BILL_PAID_STATUS_COUNT, visitId);
		Map m = ConversionUtils.listBeanToMapNumeric(l, "payment_status", "count");
		return m;
	}*/

	private static final String TPA_BILLS_COUNT = "SELECT count(*) as count FROM bill " +
			"	WHERE visit_id = ? AND is_tpa AND status != 'X' ";

	public static int getVisitTpaBillsCountExcludePrimary(String visitId) throws SQLException {
		Connection con = null; PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(TPA_BILLS_COUNT + " AND is_primary_bill = 'N' ");
			ps.setString(1, visitId);
			return DataBaseUtil.getIntValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static int getVisitTpaBills(String visitId) throws SQLException {
		Connection con = null; PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(TPA_BILLS_COUNT);
			ps.setString(1, visitId);
			return DataBaseUtil.getIntValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
			DataBaseUtil.commitClose(con, true);
		}
	}
	
	public static int getVisitTpaBills(Connection con, String visitId) throws SQLException {
   PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(TPA_BILLS_COUNT);
      ps.setString(1, visitId);
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

	private static final String ACCOUNT_GROUP_TPA_BILLS =  " SELECT * FROM bill b "+
		" JOIN bill_claim bc ON(b.visit_id = bc.visit_id and b.bill_no = bc.bill_no) "+
		" JOIN insurance_plan_main ipm ON(ipm.plan_id = bc.plan_id) "+
		" LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)"+
		" LEFT JOIN patient_details pd ON (pd.mr_no= pr.mr_no)"+
		" WHERE b.visit_id = ? AND is_tpa AND account_group = ? ";

	public static List<BasicDynaBean> getVisitAccountGroupTpaBills(String visitId, int accountGroup) throws SQLException {
		Connection con = null; PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(ACCOUNT_GROUP_TPA_BILLS);
			ps.setString(1, visitId);
			ps.setInt(2, accountGroup);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static List getAllActiveBills(String patientid) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List bills = new ArrayList<Bill>();
		ResultSet rs = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PATIENT_BILLS +" and b.status = 'A' and b.restriction_type='N' ORDER BY open_date desc");
			ps.setString(1, patientid);
			rs = ps.executeQuery();
			while (rs.next()) {
				Bill bill = new Bill();
				populateBill(bill, rs);
				bills.add(bill);
			}
			return bills;
		}finally{
			if(rs!=null)rs.close();
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	public static Bill getLatestOpenBillLaterElseBillNow(String patientid) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Bill bill = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PATIENT_BILLS +" and b.status = 'A' " +
					" and b.payment_status = 'U' and b.restriction_type='N' " +
					" ORDER BY bill_type, open_date desc limit 1");
			ps.setString(1, patientid);
			rs = ps.executeQuery();
			if (rs.next()) {
				bill = new Bill();
				populateBill(bill, rs);
			}
			return bill;
		} finally{
			if(rs!=null)rs.close();
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static Bill getFirstTpaBillLaterOrBillNow(String patientid) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Bill bill = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(GET_PATIENT_BILLS +" and is_tpa and b.status != 'X' " +
					" ORDER BY open_date limit 1");
			ps.setString(1, patientid);
			rs = ps.executeQuery();
			if (rs.next()) {
				bill = new Bill();
				populateBill(bill, rs);
			}
			return bill;
		} finally{
			if(rs!=null)rs.close();
			DataBaseUtil.closeConnections(null, ps);
			DataBaseUtil.commitClose(con, true);
		}
	}
	
	public static Bill getFirstTpaBillLaterOrBillNow(Connection con, String patientid) throws SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    Bill bill = null;
    try{
      ps = con.prepareStatement(GET_PATIENT_BILLS +" and is_tpa and b.status != 'X' " +
          " ORDER BY open_date limit 1");
      ps.setString(1, patientid);
      rs = ps.executeQuery();
      if (rs.next()) {
        bill = new Bill();
        populateBill(bill, rs);
      }
      return bill;
    } finally{
      if(rs!=null)rs.close();
      DataBaseUtil.closeConnections(null, ps);
    }
  }

	private static final String GET_ALL_CREDIT_BILLS =
		"SELECT b.visit_id, b.bill_no, b.status, b.account_group "
		+ " FROM bill b "
		+ " LEFT JOIN patient_registration pr on pr.patient_id = b.visit_id "
    + " LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no AND "
    + " patient_confidentiality_check(COALESCE(pd.patient_group,0),pd.mr_no)) "
		+ " WHERE  b.bill_type = 'C' AND b.status != 'X' AND b.visit_id=? ";

	public static List<BasicDynaBean> getPatientAllCreditBills(String visitId,
			String pharmacySeperateCreditbill) throws SQLException {
		Connection local_con = null;
		PreparedStatement ps = null;
		try {
			local_con = DataBaseUtil.getReadOnlyConnection();
			if (pharmacySeperateCreditbill != null && pharmacySeperateCreditbill.equals("Y")) {
				ps = local_con.prepareStatement(GET_ALL_CREDIT_BILLS + " AND b.restriction_type = 'P' ");
			} else {
				ps = local_con.prepareStatement(GET_ALL_CREDIT_BILLS + " AND b.restriction_type = 'N' ");
			}
			ps.setString(1, visitId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(local_con, ps);
		}
	}

	private static final String GET_VISITID_CREDIT_BILLS =
		" SELECT pr.mr_no, b.visit_id, b.bill_no, b.status,b.payment_status, b.account_group, " +
		"  b.approval_amount, b.total_amount, b.deposit_set_off, b.total_receipts, b.discount_category_id, " +
		"  pr.primary_sponsor_id, b.is_tpa, b.bill_rate_plan_id,to_char(b.open_date , 'DD-MM-YYYY HH24:MI') as open_date " +
		" FROM bill b " +
		"  JOIN patient_registration pr ON (b.visit_id = pr.patient_id)" +
		" WHERE b.status='A' AND b.visit_id = ? AND b.bill_no NOT IN (select mvb.bill_no from multivisit_bills_view mvb JOIN bill bl ON(bl.bill_no = mvb.bill_no) where bl.visit_id = ?)";

	public static List<BasicDynaBean> getVisitCreditBills(Connection con, String visitId,
			String pharmacySeperateCreditbill) throws SQLException {
		Connection local_con = con;
		PreparedStatement ps = null;
		try {
			if (local_con == null) {
				local_con = DataBaseUtil.getReadOnlyConnection();
			}
			if (pharmacySeperateCreditbill != null && pharmacySeperateCreditbill.equals("Y")) {
				ps = local_con.prepareStatement(GET_VISITID_CREDIT_BILLS + " AND b.bill_type = 'C' AND b.restriction_type = 'P' AND payment_status != 'P'");
			} else {
				ps = local_con.prepareStatement(GET_VISITID_CREDIT_BILLS + " AND " +
						"( ( b.bill_type = 'C' AND b.restriction_type IN ('P', 'N')  ) AND payment_status != 'P') ");
			}
			ps.setString(1, visitId);
			ps.setString(2, visitId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			if (con == null) {
				// we would have allocated the connection, close it.
				DataBaseUtil.closeConnections(local_con, ps);
			} else {
				DataBaseUtil.closeConnections(null, ps);
			}
		}

	}

	private static final String GET_VISITID_OPEN_BILLS =
		" SELECT pr.mr_no, b.visit_id, b.bill_no, b.status, b.payment_status, b.account_group, " +
		"  b.approval_amount, b.total_amount, b.deposit_set_off, b.total_receipts, " +
		"  pr.primary_sponsor_id, b.is_tpa, b.bill_rate_plan_id,b.open_date::date, " +
		"  b.is_primary_bill, b.bill_type FROM bill b " +
		"  JOIN patient_registration pr ON (b.visit_id = pr.patient_id)" +
		" WHERE b.status='A' AND b.bill_type IN ('C','P') AND b.restriction_type = 'N' AND "+
		" b.visit_id = ? AND b.bill_no NOT IN (select mvb.bill_no from multivisit_bills_view mvb JOIN bill bl ON(bl.bill_no = mvb.bill_no) where bl.visit_id = ?)" +
		" ORDER BY is_primary_bill DESC";

	public static List<BasicDynaBean> getVisitOpenBills(String visitId)
			throws SQLException {
		Connection local_con = DataBaseUtil.getConnection(true);
		PreparedStatement ps = null;
		try {
			ps = local_con.prepareStatement(GET_VISITID_OPEN_BILLS);
			ps.setString(1, visitId);
			ps.setString(2, visitId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(local_con, ps);
		}

	}

	private static final String GET_MAIN_VISITID_OPEN_BILLS =
			" SELECT pr.mr_no, b.visit_id, b.bill_no, b.status, b.account_group, " +
					"  b.approval_amount, b.total_amount, b.deposit_set_off, b.total_receipts, pr.primary_sponsor_id " +
					" FROM bill b JOIN patient_registration pr ON (b.visit_id = pr.patient_id)" +
					" WHERE b.status='A' AND b.bill_type IN ('C','P') AND b.restriction_type = 'N' " +
					" AND b.visit_id = ? ";

	public static List<BasicDynaBean> getVisitAllOpenBills(Connection con, String visitId) throws SQLException {
		Connection local_con = con;
		PreparedStatement ps = null;
		try {
			if (local_con == null) {
				local_con = DataBaseUtil.getReadOnlyConnection();
			}
			ps = local_con.prepareStatement(GET_MAIN_VISITID_OPEN_BILLS);
			ps.setString(1, visitId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			if (con == null) {
				// we would have allocated the connection, close it.
				DataBaseUtil.closeConnections(local_con, ps);
			} else {
				DataBaseUtil.closeConnections(null, ps);
			}
		}

	}

	/**
     * Method to get the patient's bill nos which are credit and prepaid type only
	 * (excludes other bill types like pharmacy billnow etc...)
     */
	private static final String GET_BILL_LIST = "SELECT b.visit_id, b.bill_no, b.bill_type, b.visit_type, " +
		"  b.status, b.payment_status, pr.op_type, b.restriction_type, b.claim_recd_amount, " +
		"  b.approval_amount, b.total_amount, b.deposit_set_off, b.total_receipts, pr.primary_sponsor_id, b.is_tpa, " +
		"  b.total_claim, b.insurance_deduction, b.is_primary_bill,to_char(open_date , 'DD-MM-YYYY HH24:MI') as open_date" +
		"  ,discount_category_id, b.bill_rate_plan_id, b.dyna_package_id, b.total_tax, b.total_claim_tax " +
		" FROM bill b " +
		" JOIN patient_registration pr on pr.patient_id = b.visit_id " +
		" JOIN patient_details pd ON (pd.mr_no = pr.mr_no AND patient_confidentiality_check(pd.patient_group,pd.mr_no) )" +
		" WHERE b.visit_id=? AND b.bill_type IN ('C','P')  ";

	public enum bill_type { PREPAID, CREDIT, BOTH };
	public static List<BasicDynaBean> getActiveHospitalBills(String visitId, bill_type billType,
			Boolean isWidtRestrictn) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			String query = GET_BILL_LIST +  " AND b.status='A' " ;
			String appendBillType = " ";
			if (billType.equals(bill_type.BOTH)) {
				appendBillType += "AND b.bill_type IN ('C', 'P') ";
			} else if (billType.equals(bill_type.PREPAID)) {
				appendBillType += " AND b.bill_type = 'P' ";
			} else if (billType.equals(bill_type.CREDIT)) {
				appendBillType += " AND b.bill_type = 'C' ";
			}
			query = isWidtRestrictn? GET_BILL_LIST +  " AND b.restriction_type='N' ": GET_BILL_LIST ;
			ps = con.prepareStatement(query + appendBillType);
			ps.setString(1, visitId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			ps.close();
			con.close();
		}
    }

	public static List<BasicDynaBean> getActiveUnpaidBills(String visitId, bill_type billType) throws SQLException {
		return getActiveUnpaidBills(visitId, billType, false, null);
	}

	/**
	 * Returns list of all bills (uncancelled) of specified bill type
	 * @param visitId
	 * @param billType
	 * @param includePhrmBills
	 * @param isTpa
	 * @return List<BasicDynaBean>
	 * @throws SQLException
	 */

	public static List<BasicDynaBean> getVisitBills(String visitId, bill_type billType,
			boolean includePhrmBills, Boolean isTpa) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		String query = GET_BILL_LIST +  " AND b.status != 'X' " ;
		if (isTpa != null) {
			query = (isTpa) ? query.concat(" AND b.is_tpa = true AND b.total_amount >= 0") : query.concat(" AND b.is_tpa = false ");
		}

		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			String appendBillType = " ";
			if (billType.equals(bill_type.BOTH)) {
				appendBillType += "AND b.bill_type IN ('C', 'P') ";
			} else if (billType.equals(bill_type.PREPAID)) {
				appendBillType += " AND b.bill_type = 'P' ";
			} else if (billType.equals(bill_type.CREDIT)) {
				appendBillType += " AND b.bill_type = 'C' ";
			}
			query = !includePhrmBills ? query.concat(" AND b.restriction_type='N' ") : query ;
			ps = con.prepareStatement(query.concat(appendBillType));
			ps.setString(1, visitId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(null, ps);
			DataBaseUtil.commitClose(con, true);
		}
	}

	/**
	 * Returns list of unpaid open bills of specified bill type
	 * @param visitId
	 * @param billType
	 * @param includePhrmBills
	 * @param isTpa
	 * @return
	 * @throws SQLException
	 */
	public static List<BasicDynaBean> getActiveUnpaidBills(String visitId, bill_type billType,
			boolean includePhrmBills, Boolean isTpa) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		String query = GET_BILL_LIST +  " AND b.status='A' AND payment_status = 'U' " ;
		if (isTpa != null) {
			query = (isTpa) ? query.concat(" AND b.is_tpa = true ") : query.concat(" AND b.is_tpa = false ");
		}

		try{
			con = DataBaseUtil.getConnection();

			String appendBillType = " ";
			if (billType.equals(bill_type.BOTH)) {
				appendBillType += "AND b.bill_type IN ('C', 'P') ";
			} else if (billType.equals(bill_type.PREPAID)) {
				appendBillType += " AND b.bill_type = 'P' ";
			} else if (billType.equals(bill_type.CREDIT)) {
				appendBillType += " AND b.bill_type = 'C' ";
			}
			query = !includePhrmBills ? query.concat(" AND b.restriction_type='N' ") : query ;
			ps = con.prepareStatement(query.concat(appendBillType));
			ps.setString(1, visitId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	public static List<BasicDynaBean> getActiveHospitalBills(String visitId, bill_type billType) throws SQLException {
		return getActiveHospitalBills(visitId,billType, true);
	}

	public static final String BILL_STATUS= "SELECT status FROM bill WHERE bill_no=? ";

	public String getBillStatus(String billNo) throws SQLException{
		String status = null;
		if(con == null) {
			con = DataBaseUtil.getConnection();
		}
		PreparedStatement ps = con.prepareStatement(BILL_STATUS);
		ps.setString(1, billNo);
		ResultSet rs = ps.executeQuery();
		if (rs.next())
			return rs.getString(1);
		if (rs != null) rs.close();
		if (ps != null) ps.close();
		return status;
	}

	  private static final String OPEN_BILLS_BILL_NOW =
		  "SELECT count(bill_no) FROM bill WHERE bill_type = 'P' AND status = 'A' ";
	  public static int billNowOpenBills() throws SQLException {
		  Connection con = DataBaseUtil.getConnection();
		  PreparedStatement ps = null;
		  ResultSet rs = null;
		  try {
			  ps = con.prepareStatement(OPEN_BILLS_BILL_NOW);
			  rs = ps.executeQuery();
			  if (rs.next())
				  return rs.getInt(1);
		  } finally {
			  DataBaseUtil.closeConnections(con, ps);
		  }
		  return 0;
	  }

	  private static final String CLOSED_BILLS =
		  "SELECT count(bill_no) FROM bill WHERE status = 'C'";
	  public static int closedBills() throws SQLException {
		  Connection con = DataBaseUtil.getConnection();
		  PreparedStatement ps = null;
		  ResultSet rs = null;

		  try {
			  ps = con.prepareStatement(CLOSED_BILLS);
			  rs = ps.executeQuery();
			  if (rs.next())
				  return rs.getInt(1);
		  } finally {
			  DataBaseUtil.closeConnections(con, ps);
		  }
		  return 0;
	  }

	  private static final String CANCELLED_BILLS = "SELECT count(bill_no) FROM bill WHERE status = 'X'";
	  public static int cancelledBills() throws SQLException {
		  Connection con = DataBaseUtil.getConnection();
		  PreparedStatement ps = null;
		  ResultSet rs = null;
		  try {
			  ps = con.prepareStatement(CANCELLED_BILLS);
			  rs = ps.executeQuery();
			  if (rs.next())
				  return rs.getInt(1);
		  } finally {
			  DataBaseUtil.closeConnections(con, ps);
		  }
		  return 0;
	  }

	  private static final String ACTIVE_CREDIT_BILLS =
		  "SELECT count(bill_no) FROM bill WHERE status = 'A' AND bill_type = 'C'";
	  public static int activeCreditBills() throws SQLException {
		  Connection con = DataBaseUtil.getConnection();
		  PreparedStatement ps = null;
		  ResultSet rs = null;
		  try {
			  ps = con.prepareStatement(ACTIVE_CREDIT_BILLS);
			  rs = ps.executeQuery();
			  if (rs.next())
				  return rs.getInt(1);
		  } finally {
			  DataBaseUtil.closeConnections(con, ps);
		  }
		  return 0;
	  }

	  private static final String UPDATE_VISIT_ID =
		  "UPDATE bill SET visit_id=?, visit_type=? where bill_no=?";

	  public boolean updateVisitId(String oldBillNo, String visitId, String visitType) throws SQLException {
		boolean status = false;
		PreparedStatement ps = con.prepareStatement(UPDATE_VISIT_ID);
		ps.setString(1, visitId);
		ps.setString(2, visitType);
		ps.setString(3, oldBillNo);
		int result = ps.executeUpdate();
		if (result > 0)
			status = true;
		if (ps != null) ps.close();
		return status;
	}

	private static final String UPDATE_BILL_CLAIM = "UPDATE bill SET claim_id = ? WHERE bill_no = ? AND is_tpa ";

	public void updateBillClaim(String origBillNo, String claimId)
			throws SQLException {
		PreparedStatement stmt = con.prepareStatement(UPDATE_BILL_CLAIM);
		stmt.setString(1, claimId);
		stmt.setString(2, origBillNo);
		stmt.execute();
		stmt.close();
	}

	private static final String OPEN_HOSPITAL_CREDIT_BILLS_IP =
		"SELECT * FROM BILL WHERE status='A' AND visit_type='i' AND bill_type='C' AND restriction_type='N' ";
	public static List getIPOpenCreditBills(List<String> visitsList) throws SQLException {
		//Connection con = null;
		//PreparedStatement ps = null;
		try {
			//con = DataBaseUtil.getReadOnlyConnection();
			 StringBuilder query = new StringBuilder("");
       String[] placeHolderArr = new String[visitsList.size()];
       Arrays.fill(placeHolderArr, "?");
       String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
       query.append(" AND visit_id IN ( " + placeHolders  + ")");
       
			/*if (visitsList != null && !visitsList.isEmpty()) {
				patientId.append( " AND visit_id IN (" );
				boolean first = true;
				for (String visit : visitsList) {
					if (!first)
						patientId.append(",");

					patientId.append("'"+ visit + "'");
					first = false;
				}
				patientId.append(")");
			}*/

			//ps = con.prepareStatement(OPEN_HOSPITAL_CREDIT_BILLS_IP + patientId.toString());
			return DataBaseUtil.queryToDynaList(OPEN_HOSPITAL_CREDIT_BILLS_IP + query.toString(), visitsList.toArray());
       } finally {
    	   //DataBaseUtil.closeConnections(con, ps);
       }
	}

	public static List getIPOpenCreditBills(String visit) throws SQLException {
		return getIPOpenCreditBills(visit, false);
	}

	public static List getIPOpenCreditBills(String visitId, boolean unpaidBills) throws SQLException {
		String query = OPEN_HOSPITAL_CREDIT_BILLS_IP + " AND visit_id=?"
		    + (unpaidBills ? " AND payment_status='U' " : "");
		try (Connection con = DataBaseUtil.getReadOnlyConnection();
		    PreparedStatement ps = con.prepareStatement(query)) {
		  ps.setString(1, visitId);
			return DataBaseUtil.queryToDynaList(ps);
   }

	}
	private static final String DISCHARGE_STATUSES =
		" SELECT visit_id, bill_no FROM BILL " +
		" WHERE discharge_status!='Y' AND status NOT IN ('F','S','C','X') " +
		"	AND bill_type='C' AND restriction_type='N' ";
	public static List getDischargeStatuses(List<String> visitsList) throws SQLException{
	    //Connection con = null;
	    //PreparedStatement ps = null;
	    List dischargeStatuses = null;
	    try {
	    	//con = DataBaseUtil.getReadOnlyConnection();
	      StringBuilder query = new StringBuilder("");
	      String[] placeHolderArr = new String[visitsList.size()];
	      Arrays.fill(placeHolderArr, "?");
	      String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
	      query.append(" AND visit_id IN ( " + placeHolders  + ")");
	      	    	
  			/*if (visitsList != null && !visitsList.isEmpty()) {
  
  				patientId.append( " AND visit_id IN (" );
  				boolean first = true;
  				for (String visit : visitsList) {
  					if (!first)
  						patientId.append(",");
  					patientId.append("'"+ visit + "'");
  					first = false;
  				}
  				patientId.append(")");
  			}*/

	      //ps 	=  con.prepareStatement(DISCHARGE_STATUSES + patientId.toString());
	      dischargeStatuses =  DataBaseUtil.queryToDynaList(DISCHARGE_STATUSES +query.toString() , visitsList.toArray());
	    } finally {
	    	//DataBaseUtil.closeConnections(con, ps);
	    }
	    return dischargeStatuses;
	}

	public static List getDischargeStatuses(String visitId) throws SQLException{
	    List dischargeStatuses = null;
	    try (Connection con = DataBaseUtil.getReadOnlyConnection();
	        PreparedStatement ps = con.prepareStatement(DISCHARGE_STATUSES + " AND visit_id=?")) {
	      ps.setString(1, visitId);
	      dischargeStatuses =  DataBaseUtil.queryToDynaList(ps);
	    }
	    return dischargeStatuses;
	}

	private static final String GET_FINALIZED_BILLS =
		"SELECT bill_no, b.visit_id, bill_type,COALESCE(pr.mr_no,incoming_visit_id) as mr_no,b.visit_type,category "
		+ " FROM bill b "
		+ "   LEFT JOIN patient_registration pr ON (pr.patient_id=b.visit_id) "
		+ "   LEFT JOIN patient_details p ON (p.mr_no =pr.mr_no) "
		+ "   LEFT JOIN incoming_sample_registration isr on  (incoming_visit_id=b.visit_id) "
		+ " WHERE (date(finalized_date) BETWEEN ? AND ?) "
		+ "   AND b.visit_type IN ('i','o','t') AND (b.visit_type=?  or '*'= ?) "
		+ "   AND b.bill_type  IN ('P','C') AND (bill_type=?  or '*'= ?) "
		+ "   AND b.restriction_type IN ('N','T') AND b.status NOT IN ('A','X') "
		+ "   AND ( patient_confidentiality_check(p.patient_group,p.mr_no) )"
		+ "  ORDER BY bill_no ";

	public static List<BasicDynaBean> getFinalizedBillList(String billType,
			String patientType, Date fromDate, Date toDate) throws SQLException {

		PreparedStatement ps = null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GET_FINALIZED_BILLS);
		ps.setDate(1, fromDate);
		ps.setDate(2, toDate);
		ps.setString(3, patientType );
		ps.setString(4, patientType);
		ps.setString(5, billType);
		ps.setString(6, billType);
		List list = DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);

		return list;
	}

	private static final String GET_PHARMACY_RETAIL_FINALIZED_BILLS ="SELECT bill_no, b.visit_id, bill_type, "
		+ " COALESCE(customer_id,pr.mr_no) as mr_no ,ssm.sale_id "
		+ "   FROM bill b "
		+ "		JOIN  store_sales_main  ssm using(bill_no) "
		+ "		LEFT JOIN patient_registration pr ON (pr.patient_id=b.visit_id)  "
		+ "     LEFT JOIN patient_details p ON (p.mr_no =pr.mr_no) "
		+ " 	LEFT JOIN store_retail_customers src on  (customer_id=b.visit_id) "
		+ "	  WHERE (date(finalized_date) BETWEEN ? AND  ?) "
		+ " 	AND b.bill_type  IN ('P','C') AND b.visit_type IN ('i','o','r') "
		+ "		AND b.restriction_type='P' AND b.status NOT IN ('A','X') "
		+ "     AND ( patient_confidentiality_check(p.patient_group,p.mr_no) )"
		+ "	  ORDER BY bill_no ";

	public static List<BasicDynaBean> getPharmacyRetailFinalizedBillList(Date fromDate, Date toDate)
	throws SQLException {

		PreparedStatement ps=null;
		Connection con=DataBaseUtil.getReadOnlyConnection();
		ps=con.prepareStatement(GET_PHARMACY_RETAIL_FINALIZED_BILLS);
		ps.setDate(1, fromDate);
		ps.setDate(2, toDate);
		List list=DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);

		return list;

	}

	private static final String CHECK_IF_SPONSOR_BILL =
		" SELECT is_tpa FROM bill WHERE bill_no=?";

	public static boolean checkIfsponsorBill(String billNo) throws SQLException {

		if (billNo == null || billNo.equals(""))
			return false;

		BasicDynaBean b = DataBaseUtil.queryToDynaBean(CHECK_IF_SPONSOR_BILL, billNo);
		if (b == null)
			return false;
		else
			return (Boolean) b.get("is_tpa");
	}

	/*
	 * Bill is insured only when
	 * Prepaid bill and generic preference allow_bill_now_insurance is 'Y' and bill.is_tpa is true (OR)
	 * Credit bill and bill.is_tpa is true
	 */
	public static boolean isBillInsuranceAllowed(String billType, boolean is_tpa) throws SQLException {

		if (billType != null && !billType.equals("") && is_tpa) {
			if (billType.equals(Bill.BILL_TYPE_PREPAID)) {

				boolean billNowTpaAllowed = GenericPreferencesDAO.isBillNowTpaAllowed();
				return billNowTpaAllowed;

			}else if (billType.equals(Bill.BILL_TYPE_CREDIT) && is_tpa) {
				return true;
			}
		}
		return false;
	}

	private static final String GET_FINALIZD_OR_CLOSED_LIST = "SELECT b.visit_id, b.bill_no, b.bill_type, b.visit_type, " +
	"  b.approval_amount, b.total_amount, b.deposit_set_off, b.total_receipts, pr.primary_sponsor_id, b.is_tpa, b.claim_id, " +
	"  b.total_claim, b.insurance_deduction " +
	" FROM bill b " +
	" JOIN patient_registration pr on pr.patient_id = b.visit_id " +
	" WHERE visit_id=? AND ( b.status IN ('C','F')  OR b.payment_status = 'P') AND b.bill_type IN ('C','P') AND b.is_tpa ";

	private static List getClosedOrFinalizedTpaBillsForAVisit(String visitId) throws SQLException {
		PreparedStatement ps=null;
		Connection con=DataBaseUtil.getReadOnlyConnection();
		ps=con.prepareStatement(GET_FINALIZD_OR_CLOSED_LIST);
		ps.setString(1, visitId);
		List list=DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);
		return list;
	}

	private static final String UPDATE_PHARMACY_BILL = "UPDATE store_sales_main SET bill_no = ? WHERE bill_no = ?";

	public void updatePharmacyBill(String billNo, String newBillNo) throws SQLException {
		PreparedStatement stmt = con.prepareStatement(UPDATE_PHARMACY_BILL);
		stmt.setString(1, newBillNo);
		stmt.setString(2, billNo);
		stmt.execute();
		stmt.close();
	}

	private static final String GET_BILL_DETAILS =
		" SELECT b.status, b.payment_status, b.bill_no, b.visit_id, doctor_charge_type, bc.charge_id, " +
		"	bc.consultation_type_id, dc.status as consultation_status " +
		" FROM bill b " +
		"	JOIN bill_charge bc USING (bill_no) " +
		"	JOIN bill_activity_charge bac ON (bac.charge_id=bc.charge_id) " +
		" 	JOIN doctor_consultation dc ON (bac.activity_id=dc.consultation_id::text and bac.activity_code='DOC') "+
		// if doctor is included in a package, consultation_type_id will be zero. that will not be there in consultation types master.
		// retrieve that also.
		" 	LEFT JOIN consultation_types ct ON (bc.consultation_type_id=ct.consultation_type_id) " +
		" WHERE dc.consultation_id=?";
	public BasicDynaBean getBillDetailsForConsultId(int consultationId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_BILL_DETAILS);
			ps.setInt(1, consultationId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_CLAIM_BILLS = "SELECT b.visit_id, b.bill_no, b.is_tpa " +
			" FROM bill b " +
			" JOIN bill_claim bc ON (b.bill_no = bc.bill_no) "+
			" WHERE bc.claim_id = ?";

	public List<BasicDynaBean> getBillsWithClaimId(String claimId) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_CLAIM_BILLS);
			ps.setString(1, claimId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String GET_VISIT_BILLS = "SELECT visit_id, bill_no,is_tpa FROM bill WHERE visit_id = ? and is_tpa";

	public List<BasicDynaBean> getBillsWithVisitId(String visitId) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_VISIT_BILLS);
			ps.setString(1, visitId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String GET_TOTAL_AMOUNT_WITHOUT_DRG =
		" SELECT SUM(COALESCE(amount, 0)) AS bill_total " +
		" FROM bill_charge WHERE bill_no = ? AND status != 'X' AND charge_head NOT IN ('MARDRG', 'OUTDRG') ";

	public BigDecimal getBillTotalAmountWithoutDRG(String billNo) throws SQLException {
		PreparedStatement ps = con.prepareStatement(GET_TOTAL_AMOUNT_WITHOUT_DRG);
		ps.setString(1, billNo);
		BigDecimal retVal = DataBaseUtil.getBigDecimalValueFromDb(ps);
		retVal = (retVal == null) ? BigDecimal.ZERO : retVal;
		if(ps != null) ps.close();
		return retVal;
	}

	private static final String GET_TOTAL_COPAY_WITHOUT_PDM =
		" SELECT SUM(COALESCE(amount, 0) - COALESCE(insurance_claim_amount, 0)) AS copay_total " +
		" FROM bill_charge WHERE bill_no = ? AND status != 'X' AND charge_head NOT IN ('MARPDM') ";

	public BigDecimal getBillTotalCopayWithoutPDM(String billNo) throws SQLException {
		PreparedStatement ps = con.prepareStatement(GET_TOTAL_COPAY_WITHOUT_PDM);
		ps.setString(1, billNo);
		BigDecimal retVal = DataBaseUtil.getBigDecimalValueFromDb(ps);
		retVal = (retVal == null) ? BigDecimal.ZERO : retVal;
		if(ps != null) ps.close();
		return retVal;
	}


	public static final String UPDATE_BILL_PRINTED_STATUS = " UPDATE bill SET bill_printed='Y' WHERE bill_no=?";

	public static void updateBillPrintedStatus(String billNo) throws SQLException{
		if(billNo == null)
			return;

		BillBO billBOObj = new BillBO();
		Bill bill = billBOObj.getBill(billNo);

		if(bill == null)
			return;

		if(bill.getStatus()!= null && bill.getStatus().equals(Bill.BILL_STATUS_CLOSED)) {
			Connection con = null;
			PreparedStatement ps = null;
			try{
				con = DataBaseUtil.getConnection();
				ps = con.prepareStatement(UPDATE_BILL_PRINTED_STATUS);
				ps.setString(1, billNo);
				ps.executeUpdate();
			} finally{
				DataBaseUtil.closeConnections(con, ps);
			}
		}
	}

	public static final String SPONSOR_DETAILS = "SELECT patient_id, plan_id, primary_sponsor_id," +
			" secondary_sponsor_id, tpa_name, sec_tpa_name, sponsor_type, sec_sponsor_type " +
			" FROM patient_sponsor_details_view" +
			" WHERE patient_id = (SELECT visit_id FROM bill WHERE bill_no = ?) ";

	public static BasicDynaBean getSponsorDetails(String billNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(SPONSOR_DETAILS);
			ps.setString(1, billNo);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(null, ps);
			DataBaseUtil.commitClose(con, true);
		}

	}

	public static BigDecimal getNationalSponsorAmount(BigDecimal totalClaimAmt, int noOfDays,
			String sponsorId, BasicDynaBean billbean, boolean allowZeroClaim) throws SQLException {
		BigDecimal claimAmt = BigDecimal.ZERO;

		String visitType = (String)billbean.get("visit_type");
		boolean isPrimary = billbean.get("is_primary_bill") != null && ((String)billbean.get("is_primary_bill")).equals("Y");
		Bill firstTpaBill = getFirstTpaBillLaterOrBillNow((String)billbean.get("visit_id"));
		int visitTpaBills = getVisitTpaBills((String)billbean.get("visit_id"));
		Connection con = null;
		BasicDynaBean nationalSpnsrBean = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			con.setAutoCommit(false);
			nationalSpnsrBean = new TpaMasterDAO().findByKey(con,"tpa_id", sponsorId);
		}finally{
			DataBaseUtil.commitClose(con, true);
		}
		// TODO : sponsor type migration - perdayrate
		BigDecimal perDayReimbursement = (nationalSpnsrBean != null && nationalSpnsrBean.get("per_day_rate") != null)
							? (BigDecimal)nationalSpnsrBean.get("per_day_rate") : BigDecimal.ZERO;

		BigDecimal perVisitCoPayOP = (nationalSpnsrBean != null && nationalSpnsrBean.get("per_visit_copay_op") != null)
							? (BigDecimal)nationalSpnsrBean.get("per_visit_copay_op") : BigDecimal.ZERO;

		BigDecimal perVisitCoPayIP = (nationalSpnsrBean != null && nationalSpnsrBean.get("per_visit_copay_ip") != null)
							? (BigDecimal)nationalSpnsrBean.get("per_visit_copay_ip") : BigDecimal.ZERO;

		if (allowZeroClaim) {
			if (visitType != null && visitType.equals("i")) {
				if (isPrimary) {
					// set only the copay
					claimAmt = perVisitCoPayIP;
				}
			}else {
				if (isPrimary || visitTpaBills == 1 ||
						(visitTpaBills > 1 && (firstTpaBill != null && firstTpaBill.getBillNo().equals((String)billbean.get("bill_no"))))) {
					claimAmt = perVisitCoPayOP;
				}
			}
		}else {
			if (visitType != null && visitType.equals("i")) {
				if (isPrimary) {
					if (perDayReimbursement == null || perDayReimbursement.compareTo(BigDecimal.ZERO) == 0) {
						claimAmt = ((totalClaimAmt.subtract(perVisitCoPayIP)).compareTo(BigDecimal.ZERO) > 0) ? (totalClaimAmt.subtract(perVisitCoPayIP)) : totalClaimAmt;
						//claimAmt = totalClaimAmt.subtract(perVisitCoPayIP);
					}else {

						BigDecimal overallClaimAmt = (perDayReimbursement.multiply(new BigDecimal(noOfDays))).subtract(perVisitCoPayIP);
						claimAmt = (totalClaimAmt.compareTo(overallClaimAmt) <= 0) ? totalClaimAmt :
								(overallClaimAmt.compareTo(BigDecimal.ZERO) > 0 ? overallClaimAmt : BigDecimal.ZERO);
					}
				}else {
					claimAmt = ((totalClaimAmt.subtract(perVisitCoPayIP)).compareTo(BigDecimal.ZERO) > 0) ? (totalClaimAmt.subtract(perVisitCoPayIP)) : totalClaimAmt;
				}
			}else {
				if (isPrimary || visitTpaBills == 1 ||
						(visitTpaBills > 1 && (firstTpaBill != null && firstTpaBill.getBillNo().equals((String)billbean.get("bill_no"))))) {
					claimAmt = ((totalClaimAmt.subtract(perVisitCoPayOP)).compareTo(BigDecimal.ZERO) > 0) ? (totalClaimAmt.subtract(perVisitCoPayOP)) : totalClaimAmt;
				}else {
					claimAmt = totalClaimAmt;
				}
			}
		}

		return claimAmt;
	}

	// Get the primary and secondary total claim amount when bill_charge claim amounts are edited.
	public static Map getPrimarySecondarySponsorClaimAmounts(BasicDynaBean billbean, Map deductionMap, boolean allowZeroClaim) throws SQLException {

		Map claimMap = new HashMap();
		String priSponsorType = null , secSponsorType = null;
		String priSponsorId = null , secSponsorId = null;
		BigDecimal billDeductionAmount = null;
		BigDecimal priDeductionAmount = null;
		BigDecimal secDeductionAmount = null;
		BigDecimal claimAmt = null;
		BigDecimal priApprovalAmt = null;
		BigDecimal secApprovalAmt = null;
		BigDecimal priClaimAmt = null;
		BigDecimal secClaimAmt = null;
		boolean priPerDayReimApplicable = false;
		boolean secPerDayReimApplicable = false;

		BasicDynaBean spnsrbean = getSponsorDetails((String)billbean.get("bill_no"));
		if (spnsrbean == null) return claimMap;

		priSponsorType = spnsrbean.get("sponsor_type") != null ? (String)spnsrbean.get("sponsor_type") : "";
		secSponsorType = spnsrbean.get("sec_sponsor_type") != null ? (String)spnsrbean.get("sec_sponsor_type") : "";

		priSponsorId = spnsrbean.get("primary_sponsor_id") != null ? (String)spnsrbean.get("primary_sponsor_id") : null;
		secSponsorId = spnsrbean.get("secondary_sponsor_id") != null ? (String)spnsrbean.get("secondary_sponsor_id") : null;

		billDeductionAmount = billbean.get("insurance_deduction") != null ? (BigDecimal)billbean.get("insurance_deduction") : BigDecimal.ZERO;
		priDeductionAmount = deductionMap.get("primary_deduction_amount") != null ? (BigDecimal)deductionMap.get("primary_deduction_amount") : BigDecimal.ZERO;
		secDeductionAmount = deductionMap.get("secondary_deduction_amount") != null ? (BigDecimal)deductionMap.get("secondary_deduction_amount") : BigDecimal.ZERO;

		priPerDayReimApplicable = deductionMap.get("primary_per_day_reim_applicable") != null ? (Boolean)deductionMap.get("primary_per_day_reim_applicable") : false;
		secPerDayReimApplicable = deductionMap.get("secondary_per_day_reim_applicable") != null ? (Boolean)deductionMap.get("secondary_per_day_reim_applicable") : false;

		claimAmt = billbean.get("total_claim") != null ? (BigDecimal)billbean.get("total_claim") : BigDecimal.ZERO;
		priApprovalAmt = billbean.get("primary_approval_amount") != null ? (BigDecimal)billbean.get("primary_approval_amount") : BigDecimal.ZERO;
		secApprovalAmt = billbean.get("secondary_approval_amount") != null ? (BigDecimal)billbean.get("secondary_approval_amount") : BigDecimal.ZERO;

		Timestamp opendate = (Timestamp)billbean.get("open_date");
		Timestamp finalizedate = billbean.get("finalized_date") != null
						? (Timestamp)billbean.get("finalized_date") : DateUtil.getCurrentTimestamp();

		int[] noOfDaysHours = DateUtil.getDaysHours(opendate, finalizedate, true);
		int noOfDays = noOfDaysHours[0];
		int noOfHours = noOfDaysHours[1];

		if (allowZeroClaim)
			noOfDays = (noOfDays == 0 && claimAmt.compareTo(BigDecimal.ZERO) >= 0) ? 1 : noOfDays;
		else
			noOfDays = (noOfDays == 0 && claimAmt.compareTo(BigDecimal.ZERO) > 0) ? 1 : noOfDays;

		//if (noOfHours > 0) noOfDays = noOfDays + 1;

		/**
			If sponsor type is national, the initial sponsor amount is calculated as:
		      If per-day reimbursement is 0 or null:
		            total claim - per visit co-pay
		      If per-day reimbursement is a valid value:
		            minimum of (total claim) OR (number of days * per-day reimbursement - per visit co-pay)
		      If sponsor type is corporate or insurance, then the initial sponsor amount is = total claim

			An example of National sponsor calculations when total_claim = 4000,
			co-pay = 100, per-day reimbursement is 1000, and 3 days of stay:

		    number of days * per-day = 3000
		    After reducing co-pay, this is 3000 - 100 = 2900
		    Minimum of total claim and 2900 is 2900
		    Thus, sponsor amount is 2900

			An example of National sponsor calculations
			when total_claim = 4000, co-pay = 100, per-day reimbursement is 0, and 3 days of stay:

			total claim - per visit co-pay = 4000 - 100 = 3900
		*/

		BasicDynaBean visitbean = VisitDetailsDAO.getVisitDetails((String)billbean.get("visit_id"));
		String opType = (String)visitbean.get("op_type");
		String visitType = (String)visitbean.get("visit_type");

		// Primary & Secondary Sponsor may be of National or not National.
		// Check for National
		if ((priSponsorType != null && priSponsorType.equals("N")) || (secSponsorType != null && secSponsorType.equals("N"))) {
			if (priSponsorType != null && priSponsorType.equals("N")) {

				// If not follow up then calculate the sponsor amount as per rules mentioned above.
				if (!opType.equals("F") && !opType.equals("D")) {
					priClaimAmt = getNationalSponsorAmount(claimAmt, noOfDays, priSponsorId, billbean, allowZeroClaim);
				}else {
					priClaimAmt = claimAmt;
				}

				if (!priPerDayReimApplicable && priClaimAmt.compareTo(BigDecimal.ZERO) > 0)
					priClaimAmt = priClaimAmt.add(priDeductionAmount);

				// Miminum of approval and sponsor amount is considered.
				priClaimAmt = (priClaimAmt.compareTo(priApprovalAmt) <= 0 || priApprovalAmt.compareTo(BigDecimal.ZERO) == 0) ? priClaimAmt : priApprovalAmt;

				if (secSponsorType != null && secSponsorType.equals("N")) {
					// If not follow up then calculate the sponsor amount as per rules mentioned above.
					if (!opType.equals("F") && !opType.equals("D")) {
						secClaimAmt = getNationalSponsorAmount((claimAmt.subtract(priClaimAmt)), noOfDays, secSponsorId, billbean, allowZeroClaim);
					}else {
						secClaimAmt = ((claimAmt.subtract(priClaimAmt)).compareTo(BigDecimal.ZERO) > 0) ? (claimAmt.subtract(priClaimAmt)) : BigDecimal.ZERO;
					}
				}else {
					secClaimAmt = ((claimAmt.subtract(priClaimAmt)).compareTo(BigDecimal.ZERO) > 0) ? (claimAmt.subtract(priClaimAmt)) : BigDecimal.ZERO;
				}

				if (visitType.equals("i") && !secPerDayReimApplicable && secClaimAmt.compareTo(BigDecimal.ZERO) > 0)
					secClaimAmt = secClaimAmt.add(secDeductionAmount);

				// Miminum of approval and sponsor amount is considered.
				secClaimAmt = (secClaimAmt.compareTo(secApprovalAmt) <= 0 || secApprovalAmt.compareTo(BigDecimal.ZERO) == 0) ? secClaimAmt : secApprovalAmt;

			}else if (secSponsorType != null && secSponsorType.equals("N")) {

				// If not follow up then calculate the sponsor amount as per rules mentioned above.
				if (!opType.equals("F") && !opType.equals("D")) {
					secClaimAmt = getNationalSponsorAmount(claimAmt, noOfDays, secSponsorId, billbean, allowZeroClaim);
				}else {
					secClaimAmt = claimAmt;
				}

				if (!secPerDayReimApplicable && secClaimAmt.compareTo(BigDecimal.ZERO) > 0)
					secClaimAmt = secClaimAmt.add(secDeductionAmount);

				// Miminum of approval and sponsor amount is considered.
				secClaimAmt = (secClaimAmt.compareTo(secApprovalAmt) <= 0 || secApprovalAmt.compareTo(BigDecimal.ZERO) == 0) ? secClaimAmt : secApprovalAmt;

				if (priSponsorType != null && priSponsorType.equals("N")) {
					// If not follow up then calculate the sponsor amount as per rules mentioned above.
					if (!opType.equals("F") && !opType.equals("D")) {
						priClaimAmt = getNationalSponsorAmount((claimAmt.subtract(secClaimAmt)), noOfDays, secSponsorId, billbean, allowZeroClaim);
					}else {
						priClaimAmt = ((claimAmt.subtract(secClaimAmt)).compareTo(BigDecimal.ZERO) > 0) ? (claimAmt.subtract(secClaimAmt)) : BigDecimal.ZERO;
					}
				}else {
					priClaimAmt = ((claimAmt.subtract(secClaimAmt)).compareTo(BigDecimal.ZERO) > 0) ? (claimAmt.subtract(secClaimAmt)) : BigDecimal.ZERO;
				}

				if (visitType.equals("i") && !priPerDayReimApplicable && priClaimAmt.compareTo(BigDecimal.ZERO) > 0)
					priClaimAmt = priClaimAmt.add(priDeductionAmount);

				// Miminum of approval and sponsor amount is considered.
				priClaimAmt = (priClaimAmt.compareTo(priApprovalAmt) <= 0 || priApprovalAmt.compareTo(BigDecimal.ZERO) == 0) ? priClaimAmt : priApprovalAmt;
			}
		}else {
			if (priSponsorType != null && priSponsorType.equals("I")) {

				priClaimAmt = (claimAmt.compareTo(priApprovalAmt) <= 0 || priApprovalAmt.compareTo(BigDecimal.ZERO) == 0) ? claimAmt : priApprovalAmt;
				if (!priPerDayReimApplicable && priClaimAmt.compareTo(BigDecimal.ZERO) > 0)
					priClaimAmt = priClaimAmt.add(priDeductionAmount);
				secClaimAmt = ((claimAmt.subtract(priClaimAmt)).compareTo(BigDecimal.ZERO) > 0) ? (claimAmt.subtract(priClaimAmt)) : BigDecimal.ZERO;
				if (visitType.equals("i") && !secPerDayReimApplicable && secClaimAmt.compareTo(BigDecimal.ZERO) > 0)
					secClaimAmt = secClaimAmt.add(secDeductionAmount);
				secClaimAmt = (secClaimAmt.compareTo(secApprovalAmt) <= 0 || secApprovalAmt.compareTo(BigDecimal.ZERO) == 0) ? secClaimAmt : secApprovalAmt;

			}else if (secSponsorType != null && secSponsorType.equals("I")) {

				secClaimAmt = (claimAmt.compareTo(secApprovalAmt) <= 0 || secApprovalAmt.compareTo(BigDecimal.ZERO) == 0) ? claimAmt : secApprovalAmt;
				if (!secPerDayReimApplicable && secClaimAmt.compareTo(BigDecimal.ZERO) > 0)
					secClaimAmt = secClaimAmt.add(secDeductionAmount);
				priClaimAmt = ((claimAmt.subtract(secClaimAmt)).compareTo(BigDecimal.ZERO) > 0) ? (claimAmt.subtract(secClaimAmt)) : BigDecimal.ZERO;
				if (visitType.equals("i") && !priPerDayReimApplicable && priClaimAmt.compareTo(BigDecimal.ZERO) > 0)
					priClaimAmt = priClaimAmt.add(priDeductionAmount);
				priClaimAmt = (priClaimAmt.compareTo(priApprovalAmt) <= 0 || priApprovalAmt.compareTo(BigDecimal.ZERO) == 0) ? priClaimAmt : priApprovalAmt;

			}else {
				priClaimAmt = (claimAmt.compareTo(priApprovalAmt) <= 0 || priApprovalAmt.compareTo(BigDecimal.ZERO) == 0) ? claimAmt : priApprovalAmt;
				if (!priPerDayReimApplicable && priClaimAmt.compareTo(BigDecimal.ZERO) > 0)
					priClaimAmt = priClaimAmt.add(priDeductionAmount);
				secClaimAmt = ((claimAmt.subtract(priClaimAmt)).compareTo(BigDecimal.ZERO) > 0) ? (claimAmt.subtract(priClaimAmt)) : BigDecimal.ZERO;
				if (visitType.equals("i") && !secPerDayReimApplicable && secClaimAmt.compareTo(BigDecimal.ZERO) > 0)
					secClaimAmt = secClaimAmt.add(secDeductionAmount);
				secClaimAmt = (secClaimAmt.compareTo(secApprovalAmt) <= 0 || secApprovalAmt.compareTo(BigDecimal.ZERO) == 0) ? secClaimAmt : secApprovalAmt;
			}
		}

		boolean multiPlanExists = isMultiPlanExists((String)billbean.get("visit_id"));
		if(multiPlanExists){
			priClaimAmt = getTotalClaimAmtFrmBillChgClaim((String)billbean.get("visit_id"),(String)billbean.get("bill_no"),1);

			priClaimAmt = (priClaimAmt.compareTo(priApprovalAmt) <= 0
					|| priApprovalAmt.compareTo(BigDecimal.ZERO) == 0) ? priClaimAmt : priApprovalAmt;

			secClaimAmt = getTotalClaimAmtFrmBillChgClaim((String)billbean.get("visit_id"),(String)billbean.get("bill_no"),2);

			secClaimAmt = (secClaimAmt.compareTo(secApprovalAmt) <= 0
					|| secApprovalAmt.compareTo(BigDecimal.ZERO) == 0) ? secClaimAmt : secApprovalAmt;
		}

		claimMap.put("primary_claim_amount", priClaimAmt);
		claimMap.put("secondary_claim_amount", secClaimAmt);

		return claimMap;
	}

	private static final String IS_MULTI_PLAN_EXISTS = "SELECT COUNT(*) FROM patient_insurance_plans " +
			" WHERE plan_id IS NOT NULL AND plan_id != 0 AND patient_id =? ";

	public static boolean isMultiPlanExists(String visitId)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		int planCount = 0;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(IS_MULTI_PLAN_EXISTS);
			ps.setString(1, visitId);
			planCount = DataBaseUtil.getIntValueFromDb(ps);
			return planCount >= 1;
		}finally{
			DataBaseUtil.closeConnections(null, ps);
			DataBaseUtil.commitClose(con, true);
		}
	}

	private static final String GET_TOTAL_CLAIM_AMT = "SELECT sum(insurance_claim_amt) "+
	 	" FROM  patient_insurance_plans pip "+
	 	" JOIN bill_claim bc ON(bc.visit_id = pip.patient_id and pip.plan_id = bc.plan_id) " +
	 	" JOIN bill_charge_claim bcc ON(bcc.bill_no = bc.bill_no and bcc.claim_id = bc.claim_id) "+
	 	" JOIN bill_charge bch ON(bch.charge_id = bcc.charge_id) "+
	 	" WHERE bcc.bill_no = ? and pip.priority = ? and bc.visit_id = ? AND bch.charge_head != 'PHCRET' " +
	 	" and bch.status != 'X' ";

	public static BigDecimal getTotalClaimAmtFrmBillChgClaim(String visitId, String billNo, int priority)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		BigDecimal totalClaim = BigDecimal.ZERO;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(GET_TOTAL_CLAIM_AMT);
			ps.setString(1, billNo);
			ps.setInt(2, priority);
			ps.setString(3, visitId);
			totalClaim =  DataBaseUtil.getBigDecimalValueFromDb(ps);
			totalClaim = null == totalClaim ? BigDecimal.ZERO : totalClaim;
			return totalClaim;
		}finally{
			DataBaseUtil.closeConnections(null, ps);
			DataBaseUtil.commitClose(con, true);
		}
	}

	private static final String GET_TOTAL_CLAIM_AMT_EXLUDING_SPONSORADJ = "SELECT sum(insurance_claim_amt) "+
	 	" FROM  patient_insurance_plans pip "+
	 	" JOIN bill_claim bc ON(bc.visit_id = pip.patient_id and pip.plan_id = bc.plan_id) " +
	 	" JOIN bill_charge_claim bcc ON(bcc.bill_no = bc.bill_no and bcc.claim_id = bc.claim_id) "+
	 	" JOIN bill_charge bch ON(bch.charge_id = bcc.charge_id) "+
	 	" WHERE bcc.bill_no = ? and pip.priority = ? and bc.visit_id = ? AND bch.charge_head != 'PHCRET' " +
	 	" AND bcc.charge_head != 'SPNADJ' and bch.status != 'X' ";

	public static BigDecimal getTotalClaimAmtFrmBillChgClaimExclSpnrAdj(String visitId, String billNo, int priority)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		BigDecimal totalClaim = BigDecimal.ZERO;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(GET_TOTAL_CLAIM_AMT_EXLUDING_SPONSORADJ);
			ps.setString(1, billNo);
			ps.setInt(2, priority);
			ps.setString(3, visitId);
			totalClaim =  DataBaseUtil.getBigDecimalValueFromDb(ps);
			totalClaim = null == totalClaim ? BigDecimal.ZERO : totalClaim;
			return totalClaim;
		}finally{
			DataBaseUtil.closeConnections(null, ps);
			DataBaseUtil.commitClose(con, true);
		}
	}

	private static BasicDynaBean getChargeBean(List<BasicDynaBean> charges, String chargeHead) {
		for (BasicDynaBean charge : charges) {
			String charge_head = (String)charge.get("charge_head");
			String status = (String)charge.get("status");
			if (status.equals("A") && charge_head.equals(chargeHead))
				return charge;
		}
		return null;
	}

	private static BigDecimal getServiceChargeApplicableTotal(List<BasicDynaBean> charges) {
		BigDecimal total = BigDecimal.ZERO;
		for (BasicDynaBean charge : charges) {
			String chargehead = (String)charge.get("charge_head");
			String status = (String)charge.get("status");

			if (status.equals("X"))
				continue;

			if (chargehead.equals(ChargeDTO.CH_BILL_SERVICE_CHARGE))
				continue;

			if (chargehead.equals(ChargeDTO.CH_CLAIM_SERVICE_TAX))
				continue;

			if (chargehead.equals(ChargeDTO.CH_ROUND_OFF))
				continue;

			String serv_applicable = (String)charge.get("service_charge_applicable");
			if (serv_applicable.equals("Y"))
				total = total.add((BigDecimal)charge.get("amount"));
		}
		return total;
	}

	private static BigDecimal getClaimTaxApplicableTotal(List<BasicDynaBean> charges) {
		BigDecimal total = BigDecimal.ZERO;
		for (BasicDynaBean charge : charges) {
			String chargehead = (String)charge.get("charge_head");
			String status = (String)charge.get("status");

			if (status.equals("X"))
				continue;

			if (chargehead.equals(ChargeDTO.CH_BILL_SERVICE_CHARGE))
				continue;

			if (chargehead.equals(ChargeDTO.CH_CLAIM_SERVICE_TAX))
				continue;

			if (chargehead.equals(ChargeDTO.CH_ROUND_OFF))
				continue;

			String claim_tax_applicable = (String)charge.get("claim_service_tax_applicable");
			if (claim_tax_applicable.equals("Y"))
				total = total.add((BigDecimal)charge.get("insurance_claim_amount"));
		}
		return total;
	}


	private static final String GET_BILL_TOTALS_EXCLUDE_CHARGES =
		" SELECT SUM(COALESCE(amount, 0)) AS bill_total," +
		" SUM(COALESCE(tax_amt, 0)) AS bill_tax, "+
		" SUM(COALESCE(insurance_claim_amount, 0)) AS claim_total, " +
		" SUM(COALESCE(sponsor_tax_amt,0)) AS claim_tax "+
		" FROM bill_charge WHERE bill_no = ? AND status != 'X' ";

	public static BasicDynaBean getBillChExclTotals(String billNo, List<String> chargeHeads) 
	    throws SQLException {
	  BasicDynaBean returnBean = null;
		try {
			StringBuilder query = new StringBuilder(GET_BILL_TOTALS_EXCLUDE_CHARGES);
			String[] placeHolderArr = new String[chargeHeads.size()];
      Arrays.fill(placeHolderArr, "?");
      String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
      query.append("AND charge_head NOT IN ( " + placeHolders  + ")");

      List<String> array = new ArrayList<>();
      array.add(billNo);
      array.addAll(chargeHeads);
      returnBean = DataBaseUtil.queryToDynaBean(query.toString(), array.toArray());
    } catch(SQLException exp) {
      logger.error("ERROR : Error while loading bill charges,", exp.getMessage());
      throw new SQLException(exp);

    }
		
		return returnBean;
	}

	public static String resetServiceChargeClaimTax(String billNo) throws IOException, SQLException {
		Connection con = null;
		boolean success = true;
		String err = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			GenericDAO chargedao = new GenericDAO("bill_charge");
			String billServCharge = ChargeDAO.getChargeUsingChargeHead(con, billNo, ChargeDTO.CH_BILL_SERVICE_CHARGE);
			String claimServTaxCharge = ChargeDAO.getChargeUsingChargeHead(con, billNo, ChargeDTO.CH_CLAIM_SERVICE_TAX);

			if (billServCharge != null || claimServTaxCharge != null) {
			  List<BasicDynaBean> charges = new ChargeDAO(con).getBillChargesDynaList(billNo);

			  List<String> chargeHeads = new ArrayList<String>();
			  chargeHeads.add(ChargeDTO.CH_BILL_SERVICE_CHARGE);
			  chargeHeads.add(ChargeDTO.CH_CLAIM_SERVICE_TAX);

			  BasicDynaBean billChExclTotBean = getBillChExclTotals(billNo, chargeHeads);
			  BigDecimal billAmt = (BigDecimal)billChExclTotBean.get("bill_total");
			  BigDecimal claimAmt = (BigDecimal)billChExclTotBean.get("claim_total");

			  // For Bed charges update user will be auto_update as session is null.
			  HttpSession session	= RequestContext.getSession();
			  String userid = session == null ? "auto_update" : (String)session.getAttribute("userId");
			  userid = userid != null ? userid.toString() : "auto_update";
			  Timestamp currentTimestamp = DateUtil.getCurrentTimestamp();
			  BasicDynaBean genPrefs = GenericPreferencesDAO.getPrefsBean();

			  // Recalculate bill service charge if exists.
			  BasicDynaBean billServiceChBean = getChargeBean(charges, ChargeDTO.CH_BILL_SERVICE_CHARGE);
			  if (billServiceChBean != null) {

				String servChargeId = (String)billServiceChBean.get("charge_id");
				BigDecimal serviceChargeableTotalAmt = getServiceChargeApplicableTotal(charges);

				BigDecimal serviceChargePer = genPrefs.get("bill_service_charge_percent") != null ?
						(BigDecimal)genPrefs.get("bill_service_charge_percent") : BigDecimal.ZERO;

				BigDecimal servAmt = (BigDecimal)billServiceChBean.get("amount");
				BasicDynaBean serChrgHeadBean = ChargeHeadsDAO.getChargeHeadBean(ChargeDTO.CH_BILL_SERVICE_CHARGE);
				String serChrgInsPayable = (String)serChrgHeadBean.get("service_charge_applicable");

				BigDecimal newServAmt = ConversionUtils.setScale(serviceChargePer.multiply(serviceChargeableTotalAmt)
											.divide(new BigDecimal("100"),BigDecimal.ROUND_HALF_UP));
				if (newServAmt.compareTo(servAmt) != 0) {
					String remarks = "" + serviceChargePer + "% on " + serviceChargeableTotalAmt;
					billServiceChBean = billChargeDAO.getBean();
					billServiceChBean.set("charge_id", servChargeId);
					billServiceChBean.set("act_remarks", remarks);
					billServiceChBean.set("mod_time", currentTimestamp);
					billServiceChBean.set("username", userid);
					billServiceChBean.set("act_rate", newServAmt);
					billServiceChBean.set("amount", newServAmt);
					if (serChrgInsPayable.equals("Y"))
						billServiceChBean.set("insurance_claim_amount", newServAmt);
					int result = billChargeDAO.updateWithName(con, billServiceChBean.getMap(), "charge_id");
					success = success && (result > 0);

					if (!success)
						return err = "Error while updating bill service charge for bill no: "+billNo;

					servAmt = newServAmt;
				}

				// add the service charge amount to the total
				billAmt = billAmt.add(servAmt);
				if (serChrgInsPayable.equals("Y"))
					claimAmt = claimAmt.add(servAmt);
			}

			// Recalculate claim service tax if exists.
			BasicDynaBean claimTaxChBean =  getChargeBean(charges, ChargeDTO.CH_CLAIM_SERVICE_TAX);
			if (claimTaxChBean != null) {

				String claimTaxChargeId = (String)claimTaxChBean.get("charge_id");
				BigDecimal claimTaxeableTotalAmt = getClaimTaxApplicableTotal(charges);

				BigDecimal claimTaxPer = genPrefs.get("claim_service_tax") != null ?
						(BigDecimal)genPrefs.get("claim_service_tax") : BigDecimal.ZERO;

				BigDecimal claimTaxAmt = (BigDecimal)claimTaxChBean.get("insurance_claim_amount");
				BigDecimal newTaxAmt = ConversionUtils.setScale(claimTaxPer.multiply(claimTaxeableTotalAmt)
											.divide(new BigDecimal("100"),BigDecimal.ROUND_HALF_UP));

				if (newTaxAmt.compareTo(claimTaxAmt) != 0) {
					String remarks = "" + claimTaxPer + "% on " + claimTaxeableTotalAmt;
					claimTaxChBean = billChargeDAO.getBean();
					claimTaxChBean.set("charge_id", claimTaxChargeId);
					claimTaxChBean.set("act_remarks", remarks);
					claimTaxChBean.set("mod_time", currentTimestamp);
					claimTaxChBean.set("username", userid);
					claimTaxChBean.set("act_rate", newTaxAmt);
					claimTaxChBean.set("amount", newTaxAmt);
					claimTaxChBean.set("insurance_claim_amount", newTaxAmt);
					int result = billChargeDAO.updateWithName(con, claimTaxChBean.getMap(), "charge_id");
					success = success && (result > 0);

					if (!success)
						return err = "Error while updating claim service tax for bill no: "+billNo;

					claimTaxAmt = newTaxAmt;
				}

				// add the claim amount to the total
				billAmt = billAmt.add(claimTaxAmt);
				claimAmt = claimAmt.add(claimTaxAmt);
			}
		  }
		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		return err;
	}

	public static String resetRoundOff(String billNo) throws IOException, SQLException {
		Connection con = null;
		boolean success = true;
		String err = null;

		List<String> chargeHeads = new ArrayList<String>();
		chargeHeads.add(ChargeDTO.CH_ROUND_OFF);

		BasicDynaBean billChExclTotBean = getBillChExclTotals(billNo, chargeHeads);
		if (billChExclTotBean == null) {
		  return "No active charges!";
		}
		BigDecimal billAmt = (BigDecimal)billChExclTotBean.get("bill_total");
		BigDecimal billTax = (BigDecimal)billChExclTotBean.get("bill_tax");
		BigDecimal claimAmt = (BigDecimal)billChExclTotBean.get("claim_total");
		BigDecimal claimTax = (BigDecimal)billChExclTotBean.get("claim_tax");
		
		BigDecimal existingTotalAmt = BigDecimal.ZERO;
		BigDecimal existingClaimAmt = BigDecimal.ZERO;
		BigDecimal roundOff = BigDecimal.ZERO;
		BigDecimal insRoundOff = BigDecimal.ZERO;
		BigDecimal newRoundOff = BigDecimal.ZERO;
		BigDecimal newInsRoundOff = BigDecimal.ZERO;
		
		// For Bed charges update user will be auto_update as session is null.
		HttpSession session	= RequestContext.getSession();
		String userId = session == null ? "auto_update" : (String)session.getAttribute("userId");
		userId = userId != null ? userId.toString() : "auto_update";
		Timestamp currentTimestamp = DateUtil.getCurrentTimestamp();
		

		try {
	          con = DataBaseUtil.getConnection();
		  con.setAutoCommit(false);
		  String roundOffCharge = ChargeDAO.getChargeUsingChargeHead(con, billNo, ChargeDTO.CH_ROUND_OFF);
		  if (roundOffCharge != null) {
		    BasicDynaBean existingbillbean = billDAO.findByKey(con, "bill_no", billNo);
		    existingTotalAmt = (BigDecimal) existingbillbean.get("total_amount");
		    existingClaimAmt = (BigDecimal) existingbillbean.get("total_claim");

		    List<BasicDynaBean> charges = new ChargeDAO(con).getBillChargesDynaList(billNo);

	            // Recalculate round-off if exists.
		    BasicDynaBean roundOffChBean =  getChargeBean(charges, ChargeDTO.CH_ROUND_OFF);
			if (roundOffChBean != null) {

			  String roundOffChargeId = (String)roundOffChBean.get("charge_id");
			  roundOff = (BigDecimal)roundOffChBean.get("amount");
			  insRoundOff = (BigDecimal)roundOffChBean.get("insurance_claim_amount");
			  newRoundOff = ConversionUtils.getRoundOffAmount(billAmt.add(billTax));
			  newInsRoundOff = ConversionUtils.getRoundOffAmount(claimAmt.add(claimTax));
			  
			  if (newRoundOff.compareTo(roundOff) != 0 || newInsRoundOff.compareTo(insRoundOff) != 0) {
			    roundOffChBean = billChargeDAO.getBean();
			    roundOffChBean.set("charge_id", roundOffChargeId);
			    roundOffChBean.set("mod_time", currentTimestamp);
			    roundOffChBean.set("username", userId);
			    roundOffChBean.set("act_rate", newRoundOff);
			    roundOffChBean.set("amount", newRoundOff);
			    roundOffChBean.set("insurance_claim_amount", newInsRoundOff);
			    roundOffChBean.set("is_claim_locked", true);
			    int result = billChargeDAO.updateWithName(con, roundOffChBean.getMap(), "charge_id");
			    success = success && (result > 0);
					
			    if (success) {
			    	Map<String,Object> keys = new HashMap<String,Object>();
				keys.put("bill_no", billNo);
				keys.put("priority", 1);
				BasicDynaBean billClaimBean = billClaimDAO.findByKey(keys);
				if (null != billClaimBean && null != billClaimBean.get("claim_id")) {
				  String claimId = (String)billClaimBean.get("claim_id");
				  billChargeClaimDAO.updateRoundOffInBillChargeClaim(con, roundOffChBean, claimId);
				}
			     }

			     if (!success) {
			       return err = "Error while updating round-off for bill no: "+billNo;
			     }
			  }
		      }
		   }
		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		 try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

      if (newRoundOff.compareTo(roundOff) != 0 || newInsRoundOff.compareTo(insRoundOff) != 0) {
				// add the round off to the total
				billAmt = existingTotalAmt.add(newRoundOff);
				claimAmt = existingClaimAmt.add(newInsRoundOff);
				
				BasicDynaBean billbean = billDAO.getBean();
				billbean.set("total_amount", billAmt);
				billbean.set("total_claim", claimAmt);
				billbean.set("username", userId);
				billbean.set("mod_time", currentTimestamp);
				billbean.set("bill_no", billNo);
				int result = billDAO.updateWithName(con, billbean.getMap(), "bill_no");
				success = success && (result > 0);

				if (!success)
					return err = "Error while updating bill totals for bill no: "+billNo;
			}
		}finally {
			DataBaseUtil.commitClose(con, success);
		} 

		return err;
	}

	public static void resetTotalsOrReProcess(String billNo) throws SQLException, IOException {
		resetTotalsOrReProcess(billNo, true, true, true);
	}

	public static void resetTotalsOrReProcess(String billNo, boolean reProcess) throws SQLException, IOException {
		resetTotalsOrReProcess(billNo, reProcess, reProcess, reProcess);
	}

	/**
	 *  1. Re-process perdiem.
	 *  2. Re-process dyna package.
	 *  3. Reset round off and taxes.
	 *  4. Re-calculate and update sponsor claim totals for insured bills.
	 */
	public static void resetTotalsOrReProcess(String billNo, boolean dynaReProcess,
						boolean perdiemReProcess, boolean perdiemRecalc) throws SQLException, IOException {

		// Except from bill screen and edit pharmacy item amounts screen,
		// automatic perdiem process is done from other screens if a charge is added/cancelled.
		if (perdiemReProcess || perdiemRecalc) {
			// Process perdiem only if perdiem process is done once.
			String err = new BillBO().perdiemProcess(billNo, perdiemReProcess, perdiemRecalc);
			if (err != null) {
				logger.error("ERROR : Error while re-processing perdiem for bill no: "+billNo+"  Error: "+ err);
			}
		}

		// Except from bill screen and edit pharmacy item amounts screen,
		// automatic process is done from other screens if a charge is added/cancelled.
		if (dynaReProcess) {
			// Process dyna package only if package process is done once.
			String err = new DynaPackageProcessor().process(billNo, dynaReProcess);
			if (err != null) {
				logger.error("ERROR : Error while re-processing dyna package for bill no: "+billNo+"  Error: "+ err);
			}
		}

		// Reset service charge, claim service tax if exists in bill.
		String err = resetServiceChargeClaimTax(billNo);
		if (err != null) {
			logger.error("ERROR : BillDAO: resetServiceChargeClaimTax: " + err);
		}

		// Reset round off if exists in bill.
		err = resetRoundOff(billNo);
		if (err != null) {
			logger.error("ERROR : BillDAO: resetRoundOff: " + err);
		}
		
		Map drgCodeMap = ChargeDAO.getBillDRGCode(billNo);
		if(null != drgCodeMap && null != drgCodeMap.get("drg_charge_id")){
			 new DRGCalculator().processDRG(billNo, (String)drgCodeMap.get("drg_code"));
		}

		// Re-calculate and update sponsor claim totals for insured bills.
		resetSponsorTotals(billNo);
	}
	
	public static void resetTotalsOrReProcessNew(String billNo) throws SQLException, IOException{
		resetTotalsOrReProcessNew(billNo,true,true,true);
	}
	
	public static void resetTotalsOrReProcessNew(String billNo, boolean dynaReProcess,
			boolean perdiemReProcess, boolean perdiemRecalc) throws SQLException, IOException {

		// Except from bill screen and edit pharmacy item amounts screen,
		// automatic perdiem process is done from other screens if a charge is added/cancelled.
		if (perdiemReProcess || perdiemRecalc) {
		// Process perdiem only if perdiem process is done once.
		String err = new BillBO().perdiemProcess(billNo, perdiemReProcess, perdiemRecalc);
		if (err != null) {
			logger.error("ERROR : Error while re-processing perdiem for bill no: "+billNo+"  Error: "+ err);
		}
		}
		
		// Except from bill screen and edit pharmacy item amounts screen,
		// automatic process is done from other screens if a charge is added/cancelled.
		if (dynaReProcess) {
		// Process dyna package only if package process is done once.
		String err = new DynaPackageProcessor().process(billNo, dynaReProcess);
		if (err != null) {
			logger.error("ERROR : Error while re-processing dyna package for bill no: "+billNo+"  Error: "+ err);
		}
		}
		
		// Reset service charge, claim service tax if exists in bill.
		String err = resetServiceChargeClaimTax(billNo);
		if (err != null) {
		logger.error("ERROR : BillDAO: resetServiceChargeClaimTax: " + err);
		}
		
		// Reset round off if exists in bill.
		err = resetRoundOff(billNo);
		if (err != null) {
		logger.error("ERROR : BillDAO: resetRoundOff: " + err);
		}
		
		Map drgCodeMap = ChargeDAO.getBillDRGCode(billNo);
		if(null != drgCodeMap && null != drgCodeMap.get("drg_charge_id")){
		 new DRGCalculator().processDRG(billNo, (String)drgCodeMap.get("drg_code"));
		}

		// Re-calculate and update sponsor claim totals for insured bills.
		//resetSponsorTotals(billNo);
	}
	
	private static final  String UPDATE_BILL_CLAIM_TOTALS = "UPDATE bill SET " +
		"  mod_time=?, approval_amount=?, primary_approval_amount=?, secondary_approval_amount=?, " +
		"  primary_total_claim=?, secondary_total_claim=?, " +
		"  insurance_deduction=?  WHERE bill_no=?";


	public static void resetSponsorTotals(String billNo) throws SQLException, IOException {

		BigDecimal approvalAmount = null;
		BigDecimal priApprovalAmt = null;
		BigDecimal secApprovalAmt = null;

		BigDecimal billDeductionAmount = BigDecimal.ZERO;
		BigDecimal priClaimAmt = BigDecimal.ZERO;
		BigDecimal secClaimAmt = BigDecimal.ZERO;

		// Get the final claim, bill totals details to reset sponsor totals.
		BasicDynaBean billbean = billDAO.findByKey("bill_no", billNo);

		if ((Boolean)billbean.get("is_tpa")) {

			// Get the primary and secondary sponsor details.
			BasicDynaBean spnsrbean = getSponsorDetails(billNo);
			int planId =  spnsrbean.get("plan_id") == null? 0: (Integer) spnsrbean.get("plan_id");

			if (billbean != null) {
				billDeductionAmount = billbean.get("insurance_deduction") != null ? (BigDecimal)billbean.get("insurance_deduction") : BigDecimal.ZERO;
			}

			String visitType   = billbean.get("visit_type") != null ? (String)billbean.get("visit_type") : null;
			boolean hasPlanVisitCopayLimit = false;
			BasicDynaBean planBean = new PlanMasterDAO().findByKey("plan_id", planId);

			Map deductionMap = null;
			if (!hasPlanVisitCopayLimit) {
				deductionMap = getSponsorDeductionAmounts(spnsrbean, billbean);
			}

			billbean.set("total_claim", ((BigDecimal)billbean.get("total_claim")).subtract(billDeductionAmount));

			Map claimMap = null;

			if (hasPlanVisitCopayLimit) {
				claimMap = getSponsorClaimAmount(billbean, planBean, false);
				billDeductionAmount = claimMap.get("primary_deduction_amount") != null ? (BigDecimal)claimMap.get("primary_deduction_amount") : BigDecimal.ZERO;

			}else {
				claimMap = getPrimarySecondarySponsorClaimAmounts(billbean, deductionMap, false);
			}

			if (spnsrbean.get("primary_sponsor_id") != null && !((String)spnsrbean.get("primary_sponsor_id")).equals("")) {
				priApprovalAmt = billbean.get("primary_approval_amount") != null ? (BigDecimal)billbean.get("primary_approval_amount") : BigDecimal.ZERO;
				priClaimAmt = claimMap.get("primary_claim_amount") != null ? (BigDecimal)claimMap.get("primary_claim_amount") : BigDecimal.ZERO;
			}
			if (spnsrbean.get("secondary_sponsor_id") != null && !((String)spnsrbean.get("secondary_sponsor_id")).equals("")) {
				secApprovalAmt = billbean.get("secondary_approval_amount") != null ? (BigDecimal)billbean.get("secondary_approval_amount") : BigDecimal.ZERO;
				secClaimAmt = claimMap.get("secondary_claim_amount") != null ? (BigDecimal)claimMap.get("secondary_claim_amount") : BigDecimal.ZERO;
			}

			approvalAmount = (priApprovalAmt != null && secApprovalAmt != null) ? priApprovalAmt.add(secApprovalAmt) :
									(priApprovalAmt != null ? priApprovalAmt : (secApprovalAmt != null ? secApprovalAmt : null));
		}

		boolean isMultiPlanExists = isMultiPlanExists((String)billbean.get("visit_id"));
		if(isMultiPlanExists){
			priClaimAmt = getTotalClaimAmtFrmBillChgClaim((String)billbean.get("visit_id"),billNo, 1);

			if(null != priApprovalAmt) {
				priClaimAmt = (priClaimAmt.compareTo(priApprovalAmt) <= 0
						|| priApprovalAmt.compareTo(BigDecimal.ZERO) == 0) ? priClaimAmt : priApprovalAmt;
			}

			secClaimAmt = getTotalClaimAmtFrmBillChgClaim((String)billbean.get("visit_id"),billNo, 2);

			if(null != secApprovalAmt) {
				secClaimAmt = (secClaimAmt.compareTo(secApprovalAmt) <= 0
						|| secApprovalAmt.compareTo(BigDecimal.ZERO) == 0) ? secClaimAmt : secApprovalAmt;
			}
		}

		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(UPDATE_BILL_CLAIM_TOTALS);

			int i = 1;
			ps.setTimestamp(i++, DateUtil.getCurrentTimestamp());
			ps.setBigDecimal(i++, approvalAmount);
			ps.setBigDecimal(i++, priApprovalAmt);
			ps.setBigDecimal(i++, secApprovalAmt);
			ps.setBigDecimal(i++, priClaimAmt);
			ps.setBigDecimal(i++, secClaimAmt);
			ps.setBigDecimal(i++, billDeductionAmount);
			ps.setString(i++, billNo);
			ps.executeUpdate();

		} finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static Map getSponsorClaimAmount(BasicDynaBean billbean, BasicDynaBean planBean,
			boolean allowZeroClaim) throws SQLException {

		Map claimMap = new HashMap();
		BigDecimal billDeductionAmount = null;
		BigDecimal priDeductionAmount = null;
		BigDecimal claimAmt = null;
		BigDecimal billCopayAmt = null;
		BigDecimal priApprovalAmt = null;
		BigDecimal priClaimAmt = null;
		BigDecimal visitCoPay = null;

		BasicDynaBean spnsrbean = getSponsorDetails((String)billbean.get("bill_no"));
		if (spnsrbean == null) return claimMap;

		String billNo = (String)billbean.get("bill_no");
		String patientId = (String)billbean.get("visit_id");
		String restrictionType = (String)billbean.get("restriction_type");
		Map planDetailsMap = getPlanCopayDetails(planBean, billbean);
		visitCoPay = planDetailsMap.get("copayamount") != null ? (BigDecimal)planDetailsMap.get("copayamount") : BigDecimal.ZERO;

		billDeductionAmount = billbean.get("insurance_deduction") != null ? (BigDecimal)billbean.get("insurance_deduction") : BigDecimal.ZERO;

		// Copay only for hospital bills.
		if (restrictionType.equals("N") && visitCoPay.compareTo(BigDecimal.ZERO) != 0) {
			boolean success = true;
			Connection con = null;
			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				/* boolean rateChanged = isRateChanged(billNo);
				if (rateChanged) {} */
				success = EditVisitDetailsDAO.updateBillChargesForPolicy(con, patientId, false, billNo, 0);
				if (success)
					success = EditVisitDetailsDAO.updateBillChargesForPolicy(con, patientId, true, billNo, (Integer)planBean.get("plan_id"));
			}catch (Exception e) {
				logger.error("Exception: "+e.getMessage());
			}finally {
				DataBaseUtil.commitClose(con, success);
			}

			billCopayAmt = getBillCopayAmount(billbean);

			// Process bill charges if plan has visit copay limit.
			String error = new BillBO().setBillChargesCopayZero(billNo);
			if (error != null) {
				logger.error("ERROR : Error while processing copay charges for bill no: "+billNo+"  Error: "+ error);
			}
		}
		Connection con = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			con.setAutoCommit(false);
			billbean = billDAO.findByKey(con,"bill_no", billNo);
		}finally {
			DataBaseUtil.commitClose(con, true);
		}

		claimAmt = billbean.get("total_claim") != null ? (BigDecimal)billbean.get("total_claim") : BigDecimal.ZERO;
		priApprovalAmt = billbean.get("primary_approval_amount") != null ? (BigDecimal)billbean.get("primary_approval_amount") : BigDecimal.ZERO;

		priClaimAmt = claimAmt;

		Timestamp opendate = (Timestamp)billbean.get("open_date");
		Timestamp finalizedate = billbean.get("finalized_date") != null
						? (Timestamp)billbean.get("finalized_date") : DateUtil.getCurrentTimestamp();

		int[] noOfDaysHours = DateUtil.getDaysHours(opendate, finalizedate, true);
		int noOfDays = noOfDaysHours[0];
		int noOfHours = noOfDaysHours[1];

		if (allowZeroClaim)
			noOfDays = (noOfDays == 0 && claimAmt.compareTo(BigDecimal.ZERO) >= 0) ? 1 : noOfDays;
		else
			noOfDays = (noOfDays == 0 && claimAmt.compareTo(BigDecimal.ZERO) > 0) ? 1 : noOfDays;

		//if (noOfHours > 0) noOfDays = noOfDays + 1;
		billCopayAmt = billCopayAmt == null ? BigDecimal.ZERO : billCopayAmt;

		// Consider minimum of bill copay amount (or) plan copay amount.
		priDeductionAmount = (visitCoPay.compareTo(BigDecimal.ZERO) != 0 && billCopayAmt.compareTo(BigDecimal.ZERO) != 0)
										? billCopayAmt.min(visitCoPay) : billCopayAmt;

		priDeductionAmount = priDeductionAmount == null ? BigDecimal.ZERO : priDeductionAmount;
		priClaimAmt = (priClaimAmt.compareTo(BigDecimal.ZERO) != 0) ? priClaimAmt.subtract(priDeductionAmount) : priClaimAmt;

		// Miminum of approval and sponsor amount is considered.
		priClaimAmt = (priClaimAmt.compareTo(priApprovalAmt) <= 0
						|| priApprovalAmt.compareTo(BigDecimal.ZERO) == 0) ? priClaimAmt : priApprovalAmt;

		claimMap.put("primary_deduction_amount", priDeductionAmount);
		claimMap.put("primary_claim_amount", priClaimAmt);
		claimMap.put("secondary_claim_amount", BigDecimal.ZERO);

		return claimMap;
	}

	private static boolean isRateChanged(String billNo) throws SQLException {
		boolean rateChanged = false;

		Map<String, String> filterMap = new HashMap<String, String>();
		filterMap.put("bill_no", billNo);
		filterMap.put("status", "A");

		List<BasicDynaBean> charges = billChargeDAO.listAll(null, filterMap, null);

		if (!charges.isEmpty()) {
			for (BasicDynaBean chrg : charges) {
				if (((BigDecimal)chrg.get("act_rate")).compareTo((BigDecimal)chrg.get("orig_rate")) != 0) {
					rateChanged = true;
					break;
				}
			}
		}
		return rateChanged;
	}

	private static BigDecimal getBillCopayAmount(BasicDynaBean billbean) throws SQLException {
		BigDecimal patientAmount = BigDecimal.ZERO;

		Map<String, String> filterMap = new HashMap<String, String>();
		filterMap.put("bill_no", (String)billbean.get("bill_no"));
		filterMap.put("status", "A");
		List<BasicDynaBean> charges = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			charges = billChargeDAO.listAll(con,null, filterMap, null);
		}finally{
			DataBaseUtil.commitClose(con, true);
		}

		if (!charges.isEmpty()) {

			for (BasicDynaBean chrg : charges) {

				BigDecimal amount = (BigDecimal)chrg.get("amount");
				BigDecimal retAmt = (BigDecimal)chrg.get("return_amt");
				BigDecimal insurance_claim_amount = (BigDecimal)chrg.get("insurance_claim_amount");
				BigDecimal orig_insurance_claim_amount = (BigDecimal)chrg.get("orig_insurance_claim_amount");

				if ((Boolean)chrg.get("hasactivity") && chrg.get("charge_head").equals("PHCMED"))
					amount = amount.add(retAmt);

				if ((Boolean)chrg.get("hasactivity") &&
						(chrg.get("charge_head").equals("PHCRET")))
					continue;

				// If item claim is edited, get the item deduction
				if (insurance_claim_amount.compareTo((BigDecimal)chrg.get("amount")) != 0) {
					patientAmount = patientAmount.add(amount.subtract(insurance_claim_amount));

					// If item claim is not edited, get the original item deduction
					// This can be recalculated but for performance, used
					// orig_insurance_claim_amount in bill_charge table.
				}else if (orig_insurance_claim_amount.compareTo(BigDecimal.ZERO) != 0
								&& orig_insurance_claim_amount.compareTo((BigDecimal)chrg.get("amount")) != 0) {
					patientAmount = patientAmount.add(amount.subtract(orig_insurance_claim_amount));
				}
			}
		}
		return patientAmount;
	}

	private static Map getPlanCopayDetails(BasicDynaBean planBean, BasicDynaBean billbean) throws SQLException {
		Map deductionMap = new HashMap();
		BigDecimal visitCoPay = null;

		String visitType = (String)billbean.get("visit_type");
		String restrictionType = (String)billbean.get("restriction_type");
		boolean isPrimary = billbean.get("is_primary_bill") != null && ((String)billbean.get("is_primary_bill")).equals("Y");

		if (planBean != null) {

			if (visitType.equals("o"))

				visitCoPay = planBean.get("op_visit_copay_limit") != null
					? (BigDecimal)planBean.get("op_visit_copay_limit") : BigDecimal.ZERO;

			else if (visitType.equals("i"))

				visitCoPay = planBean.get("ip_visit_copay_limit") != null
					? (BigDecimal)planBean.get("ip_visit_copay_limit") : BigDecimal.ZERO;
		}

		Bill firstTpaBill = getFirstTpaBillLaterOrBillNow((String)billbean.get("visit_id"));
		List<BasicDynaBean> tpaBills = getVisitBills((String)billbean.get("visit_id"), BillDAO.bill_type.BOTH, false, true);
		int visitTpaBillsExcludePh = tpaBills != null ? tpaBills.size() : 0;

		// Bug 36836 : Pharmacy new bill with insurance need to be allowed for visit co-pay plans.
		if (visitTpaBillsExcludePh > 0
						&& (firstTpaBill != null && firstTpaBill.getRestrictionType().equals("N")
								&& firstTpaBill.getBillNo().equals((String)billbean.get("bill_no")))) {

		}else {
			visitCoPay = BigDecimal.ZERO;
		}

		deductionMap.put("copayamount", visitCoPay);

		return deductionMap;
	}

	public static Map getSponsorDeductionAmounts(BasicDynaBean spnsrbean, BasicDynaBean billbean) throws SQLException {
		Map deductionMap = new HashMap();
		BigDecimal priDeductionAmount = BigDecimal.ZERO;
		BigDecimal secDeductionAmount = BigDecimal.ZERO;
		BigDecimal priPerDayReimbursement = null;
		BigDecimal secPerDayReimbursement = null;
		BigDecimal priPerVisitCoPayOP = null;
		BigDecimal secPerVisitCoPayOP = null;
		BigDecimal priPerVisitCoPayIP = null;
		BigDecimal secPerVisitCoPayIP = null;
		boolean priPerDayReimApplicable = false;
		boolean secPerDayReimApplicable = false;

		String visitType = (String)billbean.get("visit_type");
		BasicDynaBean visitbean = VisitDetailsDAO.getVisitDetails((String)billbean.get("visit_id"));
		String opType = (String)visitbean.get("op_type");
		boolean isPrimary = billbean.get("is_primary_bill") != null && ((String)billbean.get("is_primary_bill")).equals("Y");

		if (spnsrbean != null) {

			String priSponsorId = spnsrbean.get("primary_sponsor_id") != null ? (String)spnsrbean.get("primary_sponsor_id") : null;
			String secSponsorId = spnsrbean.get("secondary_sponsor_id") != null ? (String)spnsrbean.get("secondary_sponsor_id") : null;

			BasicDynaBean priNationalSpnsrBean = null;
			BasicDynaBean secNationalSpnsrBean = null;
			Connection con = null;
			try{
				con = DataBaseUtil.getReadOnlyConnection();
				con.setAutoCommit(false);
				priNationalSpnsrBean = new TpaMasterDAO().findByKey(con,"tpa_id", priSponsorId);
				secNationalSpnsrBean = new TpaMasterDAO().findByKey(con,"tpa_id", secSponsorId);
			}finally{
				DataBaseUtil.commitClose(con, true);
			}
			// TODO: sponsor type migration
			priPerDayReimbursement = (priNationalSpnsrBean != null && priNationalSpnsrBean.get("per_day_rate") != null)
								? (BigDecimal)priNationalSpnsrBean.get("per_day_rate") : BigDecimal.ZERO;


			secPerDayReimbursement = (secNationalSpnsrBean != null && secNationalSpnsrBean.get("per_day_rate") != null)
								? (BigDecimal)secNationalSpnsrBean.get("per_day_rate") : BigDecimal.ZERO;

			priPerVisitCoPayOP = (priNationalSpnsrBean != null && priNationalSpnsrBean.get("per_visit_copay_op") != null)
								? (BigDecimal)priNationalSpnsrBean.get("per_visit_copay_op") : BigDecimal.ZERO;

			secPerVisitCoPayOP = (secNationalSpnsrBean != null && secNationalSpnsrBean.get("per_visit_copay_op") != null)
								? (BigDecimal)secNationalSpnsrBean.get("per_visit_copay_op") : BigDecimal.ZERO;

			priPerVisitCoPayIP = (priNationalSpnsrBean != null && priNationalSpnsrBean.get("per_visit_copay_ip") != null)
								? (BigDecimal)priNationalSpnsrBean.get("per_visit_copay_ip") : BigDecimal.ZERO;

			secPerVisitCoPayIP = (secNationalSpnsrBean != null && secNationalSpnsrBean.get("per_visit_copay_ip") != null)
								? (BigDecimal)secNationalSpnsrBean.get("per_visit_copay_ip") : BigDecimal.ZERO;
		}

		if (visitType.equals("i")) {
			priDeductionAmount =  priPerVisitCoPayIP;
			secDeductionAmount =  secPerVisitCoPayIP;

			priPerDayReimApplicable = (priPerDayReimbursement.compareTo(BigDecimal.ZERO) != 0);
			secPerDayReimApplicable = (secPerDayReimbursement.compareTo(BigDecimal.ZERO) != 0);

		}else {
			priDeductionAmount = priPerVisitCoPayOP;
			secDeductionAmount = secPerVisitCoPayOP;
		}

		Bill firstTpaBill = getFirstTpaBillLaterOrBillNow((String)billbean.get("visit_id"));
		int visitTpaBills = getVisitTpaBills((String)billbean.get("visit_id"));

		if ((!opType.equals("F") && !opType.equals("D")) && (isPrimary || visitTpaBills == 1 ||
				(visitTpaBills > 1 && (firstTpaBill != null && firstTpaBill.getBillNo().equals((String)billbean.get("bill_no")))))) {

		}else {
			priDeductionAmount = BigDecimal.ZERO;
			secDeductionAmount = BigDecimal.ZERO;
		}

		deductionMap.put("primary_deduction_amount", priDeductionAmount);
		deductionMap.put("secondary_deduction_amount", secDeductionAmount);
		deductionMap.put("primary_per_day_reim_applicable", priPerDayReimApplicable);
		deductionMap.put("secondary_per_day_reim_applicable", secPerDayReimApplicable);


		return deductionMap;
	}

	/** Set deduction and sponsor claim totals for newly created insured bills. */


	public static void setDeductionAndSponsorClaimTotals(String billNo) throws SQLException {
		Connection con = null;
		BasicDynaBean billbean = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			con.setAutoCommit(false);
			billbean = billDAO.findByKey("bill_no", billNo);
		}finally{
			DataBaseUtil.commitClose(con, true);
		}

		BigDecimal approvalAmount = null;
		BigDecimal priApprovalAmt = null;
		BigDecimal secApprovalAmt = null;

		boolean allowZeroClaim = false;
		BigDecimal billDeductionAmount = BigDecimal.ZERO;
		BigDecimal priClaimAmt = BigDecimal.ZERO;
		BigDecimal secClaimAmt = BigDecimal.ZERO;

		if ((Boolean)billbean.get("is_tpa")) {
			BigDecimal newClaimAmount = (BigDecimal)billbean.get("total_claim");

			BigDecimal priDeductionAmount = BigDecimal.ZERO;
			BigDecimal secDeductionAmount = BigDecimal.ZERO;

			// Get the primary and secondary sponsor details.
			// TODO MP : change this call to PatientInsurancePlanDAO.getSponsorDetails();
			BasicDynaBean spnsrbean = getSponsorDetails(billNo);
			int planId =  spnsrbean.get("plan_id") == null? 0: (Integer) spnsrbean.get("plan_id");
			String visitType   = billbean.get("visit_type") != null ? (String)billbean.get("visit_type") : null;
			// TODO MP : The below method should be called only in case of insurance plans and not in case of corporate / national
			boolean hasPlanVisitCopayLimit = false;
			BasicDynaBean planBean = null;
			try{
				con = DataBaseUtil.getReadOnlyConnection();
				con.setAutoCommit(false);
				planBean = new PlanMasterDAO().findByKey(con,"plan_id", planId);
			}finally{
				DataBaseUtil.commitClose(con, true);
			}

			Map deductionMap = null;
			// TODO MP : This block is to be for each sponsor which is an insurance plan with visitlevel copay == 0 OR if it is national or corporate
			if (!hasPlanVisitCopayLimit) {
				deductionMap = getSponsorDeductionAmounts(spnsrbean, billbean);
				priDeductionAmount = deductionMap.get("primary_deduction_amount") != null ? (BigDecimal)deductionMap.get("primary_deduction_amount") : BigDecimal.ZERO;
				secDeductionAmount = deductionMap.get("secondary_deduction_amount") != null ? (BigDecimal)deductionMap.get("secondary_deduction_amount") : BigDecimal.ZERO;
			}

			billDeductionAmount = priDeductionAmount.add(secDeductionAmount);

			billbean.set("total_claim", ((BigDecimal)billbean.get("total_claim")).subtract(billDeductionAmount));
			allowZeroClaim = (newClaimAmount.compareTo(BigDecimal.ZERO) == 0);
			Map claimMap = null;

			if (hasPlanVisitCopayLimit) {
				// TODO MP : take it from BillChargeClaimDAO
				claimMap = getSponsorClaimAmount(billbean, planBean, allowZeroClaim);
				billDeductionAmount = claimMap.get("primary_deduction_amount") != null ? (BigDecimal)claimMap.get("primary_deduction_amount") : BigDecimal.ZERO;
			}else {
				// TODO MP : take it from BillChargeClaimDAO
				claimMap = getPrimarySecondarySponsorClaimAmounts(billbean, deductionMap, allowZeroClaim);
			}

			if (spnsrbean.get("primary_sponsor_id") != null && !((String)spnsrbean.get("primary_sponsor_id")).equals("")) {
				priApprovalAmt = billbean.get("primary_approval_amount") != null ? (BigDecimal)billbean.get("primary_approval_amount") : null;
				priClaimAmt = claimMap.get("primary_claim_amount") != null ? (BigDecimal)claimMap.get("primary_claim_amount") : BigDecimal.ZERO;
			}
			if (spnsrbean.get("secondary_sponsor_id") != null && !((String)spnsrbean.get("secondary_sponsor_id")).equals("")) {
				secApprovalAmt = billbean.get("secondary_approval_amount") != null ? (BigDecimal)billbean.get("secondary_approval_amount") : null;
				secClaimAmt = claimMap.get("secondary_claim_amount") != null ? (BigDecimal)claimMap.get("secondary_claim_amount") : BigDecimal.ZERO;
			}

			approvalAmount = (priApprovalAmt != null && secApprovalAmt != null) ? priApprovalAmt.add(secApprovalAmt) :
									(priApprovalAmt != null ? priApprovalAmt : (secApprovalAmt != null ? secApprovalAmt : null));
		}

		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(UPDATE_BILL_CLAIM_TOTALS);

			int i = 1;
			ps.setTimestamp(i++, DateUtil.getCurrentTimestamp());
			ps.setBigDecimal(i++, approvalAmount);
			ps.setBigDecimal(i++, priApprovalAmt);
			ps.setBigDecimal(i++, secApprovalAmt);
			ps.setBigDecimal(i++, allowZeroClaim ? BigDecimal.ZERO : priClaimAmt);
			ps.setBigDecimal(i++, allowZeroClaim ? BigDecimal.ZERO : secClaimAmt);
			ps.setBigDecimal(i++, billDeductionAmount);
			ps.setString(i++, billNo);
			ps.executeUpdate();

		} finally{
			DataBaseUtil.closeConnections(null, ps);
			DataBaseUtil.commitClose(con, true);
		}
	}

	public static List getOpenBillsForMrNo(String mrNo) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(
					" SELECT bill_no FROM bill b join patient_registration pr ON (b.visit_id=pr.patient_id) " +
					"	WHERE pr.mr_no=? AND b.status='A'");
			ps.setString(1, mrNo);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_BILL_ITEMS = "SELECT bc.charge_id,bc.status FROM bill b" +
			"	JOIN bill_charge bc ON(bc.bill_no=b.bill_no) WHERE b.bill_no = ?";

	public static int getBillItemsCount(String billNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		int itemCount = 0;
		List<BasicDynaBean> billItems = new ArrayList<BasicDynaBean>();
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_BILL_ITEMS);
			ps.setString(1, billNo);
			billItems = DataBaseUtil.queryToDynaList(ps);
			for(BasicDynaBean bean : billItems) {
				if(bean.get("status").equals("A"))
					itemCount ++;
			}
			return itemCount;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_ALL_INSURANCE_BILLS = "SELECT * FROM bill b " +
			" WHERE b.is_tpa AND b.visit_id = ? AND b.status != 'X' ";

	public static final List<BasicDynaBean> getAllInsuranceBills(Connection con, String visitId)throws SQLException{
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(GET_ALL_INSURANCE_BILLS);
			ps.setString(1, visitId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			if(ps != null) ps.close();
		}
	}

	public static boolean markBillForWriteOff(String billNo, BasicDynaBean billBean) throws SQLException,IOException{
		Connection con = null;
		boolean success = true;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			billDAO.update(con, billBean.getMap(), "bill_no", billNo);
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		return success;

	}

	public static boolean requestForBillCancellation(String billNo, BasicDynaBean billBean) throws SQLException,IOException {
		Connection con = null;
		boolean success = true;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			billDAO.update(con, billBean.getMap(), "bill_no", billNo);
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		return success;
	}
	
	private static final String GET_BILL_WRITE_OFF_AMOUNT = " SELECT "
			+ " pr.primary_sponsor_id AS primary_tpa_id, pr.secondary_sponsor_id AS secondary_tpa_id, "
			+ " CASE WHEN b.sponsor_writeoff='A' THEN (COALESCE(btcdv.pri_claim_amt,0) + COALESCE(btcdv.pri_claim_amt_tax,0) - (COALESCE(btcdv.pri_claim_recd_total,0)+COALESCE(b.primary_total_sponsor_receipts,0))) ELSE 0 END "
			+ " AS primary_sponsor_writeoff_amt, "
			+ " CASE WHEN b.sponsor_writeoff = 'A' THEN (COALESCE(btcdv.sec_claim_amt,0) + COALESCE(btcdv.sec_claim_amt_tax,0) -(COALESCE(btcdv.sec_claim_recd_total,0)+COALESCE(b.secondary_total_sponsor_receipts,0))) ELSE 0 END " 
			+ " AS secondry_sponsor_writeoff_amt, "
			+ " CASE WHEN b.patient_writeoff = 'A' THEN (b.total_amount + b.total_tax + b.insurance_deduction - b.total_receipts - b.total_claim - b.total_claim_tax - b.deposit_set_off-b.points_redeemed_amt) ELSE 0::NUMERIC END "
			+ " AS patient_writeoff_amt, pr.mr_no "
			+ " FROM patient_registration pr JOIN bill b on (b.visit_id = pr.patient_id) "
			+ " LEFT JOIN (SELECT pri_claim.claim_recd_total as pri_claim_recd_total, "
			+ " sec_claim.claim_recd_total as sec_claim_recd_total, "
			+ " pri_claim.total_claim as pri_claim_amt, "
			+ " pri_claim.total_claim_tax as pri_claim_amt_tax,"
			+ " sec_claim.total_claim as sec_claim_amt,"
			+ " sec_claim.total_claim_tax as sec_claim_amt_tax,"
			+ " pri_claim.bill_no "
			+ " FROM  (SELECT sum(claim_recd_total) as claim_recd_total, "
			+ " sum(COALESCE(bcc.insurance_claim_amt, 0)) as total_claim, "
			+ " sum(COALESCE(bcc.tax_amt, 0)) as total_claim_tax, "
			+ " bc.bill_no "
			+ " FROM bill_charge_claim bcc "
			+ " JOIN bill_claim bc ON(bc.claim_id=bcc.claim_id AND bc.bill_no=bcc.bill_no) "
			+ " WHERE bc.priority = 1 "
			+ " GROUP BY bc.bill_no) pri_claim "
			+ " LEFT JOIN (SELECT sum(claim_recd_total) as claim_recd_total, "
			+ " sum(COALESCE(bcc.insurance_claim_amt, 0)) as total_claim, "
			+ " sum(COALESCE(bcc.tax_amt, 0)) as total_claim_tax, "
			+ " bc.bill_no "
			+ " FROM bill_charge_claim bcc "
			+ " JOIN bill_claim bc ON(bc.claim_id=bcc.claim_id AND bc.bill_no=bcc.bill_no) "
			+ " WHERE bc.priority = 2 "
			+ " GROUP BY bc.bill_no) sec_claim ON pri_claim.bill_no = sec_claim.bill_no )  btcdv ON (btcdv.bill_no = b.bill_no) "
			+ " WHERE b.bill_no = ? ";
	public static BasicDynaBean getWriteOffBillAmount(String billNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_BILL_WRITE_OFF_AMOUNT);
			ps.setString(1, billNo);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
		  DataBaseUtil.closeConnections(con, null);
		}
	}
	
	private static final String GET_CREDIT_NOTE_AMOUNTS = "SELECT "
	    + "COALESCE(pri.amount, 0) AS primary_sponsor_amount, "
	    + "COALESCE(sec.amount, 0) AS secondary_sponsor_amount, "
	    + "COALESCE(pat.amount, 0) AS patient_amount "
	    + "FROM bill b LEFT JOIN("
	    + "SELECT bcn.bill_no AS bill_no, SUM(bcc.insurance_claim_amt) AS amount "
	    + "FROM bill_credit_notes bcn "
	    + "JOIN bill b ON (bcn.credit_note_bill_no = b.bill_no) "
	    + "JOIN bill_claim bc ON(b.bill_no=bc.bill_no AND bc.priority=1) "
	    + "JOIN bill_charge_claim bcc ON(b.bill_no=bcc.bill_no AND bc.claim_id = bcc.claim_id) "
	    + "WHERE NOT b.status='X' GROUP BY bcn.bill_no) AS pri ON (b.bill_no = pri.bill_no)"
	    + "LEFT JOIN ("
	    + "SELECT bcn.bill_no AS bill_no, SUM(bcc.insurance_claim_amt) AS amount "
	    + "FROM bill_credit_notes bcn "
	    + "JOIN bill b ON (bcn.credit_note_bill_no = b.bill_no) "
	    + "JOIN bill_claim bc ON(b.bill_no=bc.bill_no AND bc.priority=2) "
	    + "JOIN bill_charge_claim bcc ON(b.bill_no=bcc.bill_no AND bc.claim_id = bcc.claim_id) "
	    + "WHERE NOT b.status='X' GROUP BY bcn.bill_no) AS sec ON (b.bill_no = sec.bill_no) "
	    + "LEFT JOIN (SELECT bcn.bill_no AS bill_no,SUM(b.total_amount-b.total_claim) AS amount "
	    + "FROM bill_credit_notes bcn "
	    + "JOIN bill b ON (bcn.credit_note_bill_no = b.bill_no AND NOT b.is_tpa) "
	    + "GROUP BY bcn.bill_no) AS pat ON (b.bill_no = pat.bill_no) "
	    + "WHERE b.bill_no = ?";
	
	public static BasicDynaBean getCreditNotesAmount(String billNo) throws SQLException {
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(GET_CREDIT_NOTE_AMOUNTS);) {
      ps.setString(1, billNo);
      return DataBaseUtil.queryToDynaBean(ps);
    }
  }

	private static final String BILL_WRITE_OFF_QUERY_FIELDS ="SELECT * ";

	private static final String BILL_WRITE_OFF_QUERY_COUNT =
		" SELECT count(bill_no)  ";

	private static final String BILL_WRITE_OFF_QUERY_TABLES =
		" FROM (SELECT b.bill_no, b.visit_id, b.visit_type, b.status, p.patient_group, " +
		" creditNote.total_credit_amount, " +
		" CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN creditNote.total_credit_amount - creditNote.total_credit_claim ELSE 0.00 END as total_credit_patient ," +
		" CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN creditNote.total_credit_claim ELSE 0.00 END AS total_credit_sponsor , " +
		"  CASE WHEN bill_type = 'C' THEN 'C' ELSE 'P' END as bill_type, bill_type AS actual_bill_type, " +
		"  b.open_date, b.mod_time, b.finalized_date, b.closed_date, b.opened_by, b.username, pr.center_id, " +
		"  b.claim_recd_amount, b.patient_writeoff, b.writeoff_remarks, b.sponsor_writeoff,  " +
		"  b.total_amount, b.total_discount, b.total_claim, b.total_tax, b.total_claim_tax, total_receipts, p.mr_no, " +
		"  coalesce(get_patient_name(p.salutation, p.patient_name, p.middle_name, p.last_name), " +
		"    isr.patient_name, phc.customer_name) as patient_name, b.sponsor_writeoff_remarks, " +
		"  coalesce(p.dateofbirth, expected_dob) as dob, p.patient_gender, pr.reg_date, pr.reg_time, " +
		"	b.primary_total_claim, b.secondary_total_claim, b.deposit_set_off, b.claim_recd_amount, "+
		"	b.points_redeemed, b.points_redeemed_amt, b.primary_total_sponsor_receipts, b.secondary_total_sponsor_receipts, "+
		" CASE WHEN patient_writeoff = 'A' AND creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' " + 
		" THEN b.total_amount-b.primary_total_claim-b.secondary_total_claim-b.total_receipts-b.deposit_set_off-b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim " + 
		" WHEN  patient_writeoff = 'A' THEN b.total_amount-b.primary_total_claim-b.secondary_total_claim-b.total_receipts-b.deposit_set_off-b.points_redeemed_amt " + 
		" ELSE 0.00 END AS writtenoff_amt," +
		"  	b.total_amount-b.primary_total_claim-b.secondary_total_claim-b.total_receipts-b.deposit_set_off-b.points_redeemed_amt  as patient_due, "+
		"   b.primary_total_claim + b.secondary_total_claim - b.primary_total_sponsor_receipts - b.secondary_total_sponsor_receipts - b.claim_recd_amount as sponsor_due ,"+
		"  CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN b.total_amount + b.total_tax -b.primary_total_claim-b.secondary_total_claim - b.total_claim_tax - b.total_receipts-b.deposit_set_off-b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim ELSE b.total_amount + b.total_tax - b.primary_total_claim - b.secondary_total_claim - b.total_claim_tax - b.total_receipts-b.deposit_set_off-b.points_redeemed_amt END AS net_patient_due, " +
		"  CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN b.primary_total_claim + b.secondary_total_claim + b.total_claim_tax - b.primary_total_sponsor_receipts - b.secondary_total_sponsor_receipts - b.claim_recd_amount + creditNote.total_credit_claim ELSE b.primary_total_claim + b.secondary_total_claim + b.total_claim_tax - b.primary_total_sponsor_receipts - b.secondary_total_sponsor_receipts - b.claim_recd_amount END AS net_sponsor_due " +
		"  FROM bill b " +
		"  JOIN organization_details od on (od.org_id = b.bill_rate_plan_id) " +
		"  LEFT JOIN patient_registration pr on (b.visit_id = pr.patient_id) " +
		"  LEFT JOIN store_retail_customers phc on (b.visit_id = phc.customer_id) " +
		"  LEFT JOIN patient_details p on (pr.mr_no = p.mr_no) "+
		"  LEFT JOIN tpa_master tpap on (tpap.tpa_id = pr.primary_sponsor_id) " +
		"  LEFT JOIN tpa_master tpas on (tpas.tpa_id = pr.secondary_sponsor_id) " +
		"  LEFT JOIN incoming_sample_registration isr on (isr.incoming_visit_id = b.visit_id) " +
		"  LEFT JOIN admission adm ON (adm.patient_id = b.visit_id)" +
		"  LEFT JOIN bed_names bn ON (bn.bed_id = adm.bed_id) "+
		"  LEFT JOIN ward_names wn ON (wn.ward_no = bn.ward_no) "+
		"  LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor) "+
		"  LEFT JOIN department dep ON (dep.dept_id = pr.dept_name) " +
		"  LEFT JOIN bill_label_master blm ON (blm.bill_label_id = b.bill_label_id)" +
		"  LEFT JOIN ( SELECT bcn.bill_no , sum(cn.total_amount) AS total_credit_amount, sum(cn.total_claim) AS total_credit_claim " + 
		"  FROM bill_credit_notes bcn " + 
		"  LEFT JOIN bill cn ON(bcn.credit_note_bill_no = cn.bill_no ) GROUP BY bcn.bill_no  ) " + 
		"  AS creditNote ON(creditNote.bill_no = b.bill_no)" +
		" ) AS foo";

	private static final String PATIENT_WRITEOFF_WHERE = " WHERE  patient_writeoff in ('M','A') AND net_patient_due != 0.00 ";
	private static final String SPONSOR_WRITEOFF_WHERE = " WHERE ((net_sponsor_due != 0.00 AND status = 'F') OR (sponsor_writeoff in ('A','M') AND " +
			"	net_sponsor_due != 0.00 AND status = 'C')) ";


	public static PagedList getWriteOffBillList(Map filter, Map listing, String writeoffType)
	throws SQLException, ParseException {

		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb = null;
			if(writeoffType.equals("P")){
				qb = new SearchQueryBuilder(con,
					BILL_WRITE_OFF_QUERY_FIELDS, BILL_WRITE_OFF_QUERY_COUNT, BILL_WRITE_OFF_QUERY_TABLES, PATIENT_WRITEOFF_WHERE, listing);
			}else{
				qb = new SearchQueryBuilder(con,
						BILL_WRITE_OFF_QUERY_FIELDS, BILL_WRITE_OFF_QUERY_COUNT, BILL_WRITE_OFF_QUERY_TABLES, SPONSOR_WRITEOFF_WHERE, listing);
			}
			qb.addFilterFromParamMap(filter);
			qb.appendToQuery("(patient_confidentiality_check(foo.patient_group,foo.mr_no))");
			qb.addSecondarySort("bill_no");
			
			qb.build();

			PagedList l = qb.getMappedPagedList();
			qb.close();
			return l;
		}finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static boolean updatePatientWriteOffStatus(String billNo) throws SQLException,IOException{

		Connection con = null;
		boolean success = true;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BasicDynaBean billBean = billDAO.findByKey(con, "bill_no", billNo);
			billBean.set("patient_writeoff", "A");
			success = billDAO.update(con, billBean.getMap(), "bill_no", billNo) > 0;
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		return false;
	}

	private static final String GET_PATIENTDUE = "SELECT total_amount+total_tax-primary_total_claim-"
	    + " secondary_total_claim-total_receipts-deposit_set_off-points_redeemed_amt "
	    + " FROM bill WHERE bill_no=? ";

	public static BigDecimal getPatientDue(String billNo) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_PATIENTDUE);
			ps.setString(1, billNo);
			return DataBaseUtil.getBigDecimalValueFromDb(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_NET_PATIENTDUE =
			"  SELECT CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN b.total_amount-b.primary_total_claim-b.secondary_total_claim-b.total_receipts-b.deposit_set_off-b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim ELSE b.total_amount-b.primary_total_claim-b.secondary_total_claim-b.total_receipts-b.deposit_set_off-b.points_redeemed_amt END AS net_patient_due " +
			"  FROM bill b " +
			"  LEFT JOIN ( SELECT bcn.bill_no , sum(cn.total_amount) AS total_credit_amount, sum(cn.total_claim) AS total_credit_claim " + 
			"  FROM bill_credit_notes bcn " + 
			"  LEFT JOIN bill cn ON(bcn.credit_note_bill_no = cn.bill_no ) GROUP BY bcn.bill_no  ) " + 
			"  AS creditNote ON(creditNote.bill_no = b.bill_no)" +
			"  WHERE b.bill_no=?";
	
	public static BigDecimal getNetPatientDue(String billNo) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_NET_PATIENTDUE);
			ps.setString(1, billNo);
			return DataBaseUtil.getBigDecimalValueFromDb(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String GET_BILL_SPONSORDUE = "SELECT primary_total_claim+secondary_total_claim+total_claim_tax-primary_total_sponsor_receipts-secondary_total_sponsor_receipts-claim_recd_amount" +
	" FROM bill WHERE bill_no=? ";

	public static BigDecimal getSponsorDue(Connection con, String billNo) throws SQLException {
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(GET_BILL_SPONSORDUE);
			ps.setString(1, billNo);
			return DataBaseUtil.getBigDecimalValueFromDb(ps);
		}finally{
			if(null != ps) ps.close();
		}
	}

	public static BigDecimal getSponsorDue(String billNo) throws SQLException {
		Connection con = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			return getSponsorDue(con, billNo);
		}finally{
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String GET_VISIT_PATIENTDUE = "SELECT sum(b.total_amount+b.total_tax-b.total_claim-b.total_claim_tax-b.total_receipts-b.deposit_set_off-b.points_redeemed_amt) "
			+ " AS visit_patient_due FROM bill b WHERE b.visit_id=? GROUP BY b.visit_id ";

	public static BigDecimal getVisitPatientDue(String visitId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_VISIT_PATIENTDUE);
			ps.setString(1, visitId);
			BigDecimal visitPatientDue = DataBaseUtil.getBigDecimalValueFromDb(ps);
			return visitPatientDue == null ? BigDecimal.ZERO : visitPatientDue;
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public static BigDecimal getVisitPatientDue(Connection con, String visitId) throws SQLException{
    PreparedStatement ps = null;
    try{
      ps = con.prepareStatement(GET_VISIT_PATIENTDUE);
      ps.setString(1, visitId);
      BigDecimal visitPatientDue = DataBaseUtil.getBigDecimalValueFromDb(ps);
      return visitPatientDue == null ? BigDecimal.ZERO : visitPatientDue;
    }finally{
      DataBaseUtil.closeConnections(null, ps);
    }
  }
	
	private static final String GET_VISIT_PATIENTDUE_BY_EXCLUDE_BILL = "SELECT sum(b.total_amount+b.total_tax-b.total_claim-b.total_claim_tax-b.total_receipts-b.deposit_set_off-b.points_redeemed_amt) "
			+ " AS visit_patient_due FROM bill b WHERE b.visit_id=? AND b.bill_no !=? GROUP BY b.visit_id ";

	public static BigDecimal getVisitPatientDueByExcludeBill(String visitId, String excludeBillNo) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_VISIT_PATIENTDUE_BY_EXCLUDE_BILL);
			ps.setString(1, visitId);
			ps.setString(2, excludeBillNo);
			BigDecimal due = DataBaseUtil.getBigDecimalValueFromDb(ps);
			return due != null ? due : BigDecimal.ZERO;
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static BigDecimal getVisitPatientDueByExcludeBill(Connection con, String visitId, String excludeBillNo) throws SQLException{
    PreparedStatement ps = null;
    try{
      ps = con.prepareStatement(GET_VISIT_PATIENTDUE_BY_EXCLUDE_BILL);
      ps.setString(1, visitId);
      ps.setString(2, excludeBillNo);
      BigDecimal due = DataBaseUtil.getBigDecimalValueFromDb(ps);
      return due != null ? due : BigDecimal.ZERO;
    }finally{
      DataBaseUtil.closeConnections(null, ps);
    }
  }
	
	private static final String UPDATE_WRITEOFF_REMARKS = "UPDATE bill SET writeoff_remarks=? WHERE bill_no=? ";
	private static final String UPDATE_SPONSOR_WRITEOFF_REMARKS = "UPDATE bill SET sponsor_writeoff_remarks=? WHERE bill_no=? ";
	public static boolean updateWriteOffRemarks(String billNo,String writeOffRemarks,String writeOffType) throws SQLException {
		boolean success = false;
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if(writeOffType.equals("P"))
				ps = con.prepareStatement(UPDATE_WRITEOFF_REMARKS);
			else
				ps = con.prepareStatement(UPDATE_SPONSOR_WRITEOFF_REMARKS);
			ps.setString(1, writeOffRemarks);
			ps.setString(2, billNo);
			success = ps.executeUpdate() >= 0;
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		return success;
	}

	public static boolean updateSponsorWriteOffStatus(String billNo) throws SQLException,IOException{

		Connection con = null;
		boolean success = true;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BasicDynaBean billBean = billDAO.findByKey(con, "bill_no", billNo);
			billBean.set("sponsor_writeoff", "A");
			success = billDAO.update(con, billBean.getMap(), "bill_no", billNo) > 0;
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		return false;
	}

	private static final String TOTAL_APPROVED_AMT_FOR_CURRENT_MONTH = "SELECT SUM(cancel_approve_amount) as total_approved_amt " +
			" FROM bill where cancellation_approved_by = ? AND cancellation_approval_status = 'A' AND date_trunc('month', cancellation_approved_date) = date_trunc('month', current_date) ";
	private static final String TOTAL_APPROVED_AMT_FOR_CURRENT_YEAR = "SELECT SUM(cancel_approve_amount) as total_approved_amt " +
			" FROM bill where cancellation_approved_by = ? AND cancellation_approval_status = 'A' AND date_trunc('year', cancellation_approved_date) = date_trunc('year', current_date) ";
	public static String getTotalApprovedAmount(String userName , String rangeType) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		String totalApprovedAmt = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if(rangeType.equals("M"))
				ps = con.prepareStatement(TOTAL_APPROVED_AMT_FOR_CURRENT_MONTH);
			else
				ps = con.prepareStatement(TOTAL_APPROVED_AMT_FOR_CURRENT_YEAR);
			ps.setString(1, userName);
			totalApprovedAmt = DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return totalApprovedAmt;
	}


	public static final String GET_DISCOUNT_AUTH_LIST = " SELECT * " +
			"	FROM( "+
			"			SELECT disc_auth_id, disc_auth_name, status, created_timestamp, " +
			"			updated_timestamp, regexp_split_to_table(center_id, E',') AS center_id "+
			"			FROM discount_authorizer" +
			"		) AS foo " +
			"	WHERE foo.center_id=? OR foo.center_id='0' " ;


	public static List<BasicDynaBean> getDiscountAuthList(String centerId)throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_DISCOUNT_AUTH_LIST);
			ps.setString(1, centerId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
  private static final String GET_MAIN_AND_FOLLOWUP_VISIT_BILLS_CHARGES = "SELECT * FROM ( "
      + " SELECT bc.charge_id, # as amount, bc.discount, bc.charge_group, bc.charge_head, bc.act_description_id as act_description_id, "
      + "   CASE WHEN (CASE WHEN bc.charge_group='PKG' THEN" +
		  " (select insurance_payable from chargehead_constants where chargehead_id = 'PKGPKG') ELSE chc.insurance_payable END)='Y'"
      + " THEN true ELSE false END AS is_charge_head_payable, "
      + "   bc.insurance_claim_amount, bc.insurance_category_id, bc.is_claim_locked, bc.include_in_claim_calc, "
      + "   bc. copay_ded_adj, bc.max_copay_adj, bc.sponsor_limit_adj, bc.copay_perc_adj, 'hospital' as charge_type, "
      + " CASE WHEN bc.charge_head = 'INVITE' THEN scm.claimable ELSE true END AS store_item_category_payable, bc.amount_included, "
      + "   bc.consultation_type_id, bc.op_id, "
      + " tpa.claim_amount_includes_tax,tpa.limit_includes_tax,b.bill_no,b.is_tpa , bc.item_excluded_from_doctor, bc.item_excluded_from_doctor_remarks, "
      + " bc.package_id "
      + "   FROM bill b "
      + "   JOIN bill_charge bc ON(b.bill_no = bc.bill_no) "
      + " LEFT JOIN bill_claim pbcl ON(pbcl.bill_no = b.bill_no AND pbcl.priority=1) "
      + " LEFT JOIN tpa_master tpa ON(tpa.tpa_id = pbcl.sponsor_id) "
      + "   JOIN patient_registration pr ON(pr.patient_id = b.visit_id) "
      + "   JOIN chargehead_constants chc ON(chc.chargehead_id = bc.charge_head) "
      + " LEFT JOIN store_item_details sid ON(sid.medicine_id::text = bc.act_description_id AND bc.charge_head='INVITE') "
      + " LEFT JOIN store_category_master scm ON(scm.category_id = sid.med_category_id) "
      + "   WHERE (pr.patient_id in(##)) AND b.is_tpa AND pr.op_type IN ('M', 'F','D','R','O') AND "
      + "   bc.charge_head NOT IN('PHCMED','PHMED','PHRET','PHCRET','INVRET') AND bc.status != 'X' AND b.total_amount >= 0"
      + "   UNION ALL "
      + "   SELECT bc.charge_id || '-' || ssd.sale_item_id as charge_id, @ as amount, 0 as discount, bc.charge_group, bc.charge_head, "
      + "   ssd.medicine_id::text as act_description_id, CASE WHEN (CASE WHEN bc.charge_group='PKG' " +
		  "THEN (select insurance_payable from chargehead_constants where chargehead_id = 'PKGPKG') ELSE chc.insurance_payable END)='Y' THEN true"
      + " ELSE false END AS is_charge_head_payable, "
      + "   ssd.insurance_claim_amt as insurance_claim_amount, ssd.insurance_category_id, ssd.is_claim_locked, ssd.include_in_claim_calc, "
      + " 0 AS copay_ded_adj, 0 AS max_copay_adj, 0 AS sponsor_limit_adj, 0 AS copay_perc_adj, 'pharmacy' as charge_type, "
      + " true, ssd.amount_included, bc.consultation_type_id, bc.op_id,  "
      + " tpa.claim_amount_includes_tax,tpa.limit_includes_tax,b.bill_no,b.is_tpa, bc.item_excluded_from_doctor, bc.item_excluded_from_doctor_remarks, "
      + " bc.package_id "
      + "   FROM bill b "
      + "   JOIN store_sales_main ssm ON(b.bill_no = ssm.bill_no AND ssm.type = 'S') "
      + "   JOIN bill_charge bc ON(bc.charge_id = ssm.charge_id) "
      + " LEFT JOIN bill_claim pbcl ON(pbcl.bill_no = b.bill_no AND pbcl.priority=1) "
      + " LEFT JOIN tpa_master tpa ON(tpa.tpa_id = pbcl.sponsor_id) "
      + "   JOIN store_sales_details ssd ON(ssm.sale_id = ssd.sale_id) "
      + "   JOIN chargehead_constants chc ON(chc.chargehead_id = bc.charge_head) "
      + "   JOIN patient_registration pr ON(pr.patient_id = b.visit_id) "
      + "   WHERE (pr.patient_id in(##)) AND b.is_tpa AND pr.op_type IN ('M', 'F','D','R','O') AND "
      + "   bc.status != 'X' AND b.total_amount >= 0) AS foo ORDER BY foo.charge_id ";


  private static final String GET_VISIT_BILLS_CHARGES = "SELECT * FROM ( "
      + "	SELECT bc.charge_id, # as amount, bc.discount, bc.charge_group, bc.charge_head, bc.act_description_id as act_description_id, "
      + " 	CASE WHEN (CASE WHEN bc.charge_group='PKG' THEN (select insurance_payable from chargehead_constants where chargehead_id = 'PKGPKG') ELSE chc.insurance_payable END)='Y' THEN true ELSE false"
      + " END AS is_charge_head_payable, "
      + " 	bc.insurance_claim_amount, bc.insurance_category_id, bc.is_claim_locked, bc.include_in_claim_calc,  "
      + "   bc. copay_ded_adj, bc.max_copay_adj, bc.sponsor_limit_adj, bc.copay_perc_adj, 'hospital' as charge_type, "
      + "	CASE WHEN bc.charge_head = 'INVITE' THEN scm.claimable ELSE true END AS store_item_category_payable, bc.amount_included, bc.consultation_type_id, bc.op_id, "
      + "	tpa.claim_amount_includes_tax,tpa.limit_includes_tax,b.bill_no,b.is_tpa, bc.item_excluded_from_doctor, bc.item_excluded_from_doctor_remarks, "
      + "   bc.package_id "
      + " 	FROM bill b "
      + " 	JOIN bill_charge bc ON(b.bill_no = bc.bill_no) "
      + "	LEFT JOIN bill_claim pbcl ON(pbcl.bill_no = b.bill_no AND pbcl.priority=1) "
      + "	LEFT JOIN tpa_master tpa ON(tpa.tpa_id = pbcl.sponsor_id) "
      + " 	JOIN chargehead_constants chc ON(chc.chargehead_id = bc.charge_head) "
      + "	LEFT JOIN store_item_details sid ON(sid.medicine_id::text = bc.act_description_id AND bc.charge_head='INVITE') "
      + "	LEFT JOIN store_category_master scm ON(scm.category_id = sid.med_category_id) "
      + " 	WHERE b.visit_id = ? AND b.is_tpa AND "
      + " 	bc.charge_head NOT IN('PHCMED','PHMED','PHRET','PHCRET','INVRET') AND bc.status != 'X' AND b.total_amount >= 0 "
      + " 	UNION ALL "
      + " 	SELECT bc.charge_id || '-' || ssd.sale_item_id as charge_id, @ as amount, 0 as discount, bc.charge_group, bc.charge_head,"
      + "   ssd.medicine_id::text as act_description_id, CASE WHEN (CASE WHEN bc.charge_group='PKG' THEN (select insurance_payable from chargehead_constants where chargehead_id = 'PKGPKG') ELSE chc.insurance_payable END)='Y' THEN true ELSE false"
      + "   END AS is_charge_head_payable, "
      + " 	ssd.insurance_claim_amt as insurance_claim_amount, ssd.insurance_category_id, ssd.is_claim_locked, ssd.include_in_claim_calc,  "
      + "   bc. copay_ded_adj, bc.max_copay_adj, bc.sponsor_limit_adj, bc.copay_perc_adj, 'pharmacy' as charge_type, "
      + "	true AS store_item_category_payable, ssd.amount_included, bc.consultation_type_id, bc.op_id, "
      + "	tpa.claim_amount_includes_tax,tpa.limit_includes_tax,b.bill_no,b.is_tpa,"
	  + "   bc.item_excluded_from_doctor, bc.item_excluded_from_doctor_remarks,bc.package_id  "
      + " 	FROM bill b "
      + " 	JOIN store_sales_main ssm ON(b.bill_no = ssm.bill_no AND ssm.type = 'S') "
      + " 	JOIN bill_charge bc ON(bc.charge_id = ssm.charge_id) "
      + "	LEFT JOIN bill_claim pbcl ON(pbcl.bill_no = b.bill_no AND pbcl.priority=1) "
      + "	LEFT JOIN tpa_master tpa ON(tpa.tpa_id = pbcl.sponsor_id) "
      + " 	JOIN store_sales_details ssd ON(ssm.sale_id = ssd.sale_id) "
      + " 	JOIN chargehead_constants chc ON(chc.chargehead_id = bc.charge_head) "
      + " 	WHERE b.visit_id = ? AND b.is_tpa AND bc.status != 'X'  AND b.total_amount >= 0 "
      + " 	 ) AS foo ORDER BY foo.charge_id ";

	public List<BasicDynaBean> getVisitBillCharges(String visitId, Boolean includeFollowUpVisits, String followUpvisits) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con =DataBaseUtil.getReadOnlyConnection();
			String isClaimAmtIncludesTax = "N";
			String isLimitIncludesTax = "N";
			PatientInsurancePlanDAO pipDao = new PatientInsurancePlanDAO();
			BasicDynaBean priSponDetailsBean = pipDao.getPrimarySponsorDetails(con, visitId);
			if(priSponDetailsBean != null) {
				isClaimAmtIncludesTax = (String)priSponDetailsBean.get("claim_amount_includes_tax");
				isLimitIncludesTax = (String)priSponDetailsBean.get("limit_includes_tax");
			}
			
			String query = includeFollowUpVisits ? GET_MAIN_AND_FOLLOWUP_VISIT_BILLS_CHARGES : GET_VISIT_BILLS_CHARGES;
			
			if(isClaimAmtIncludesTax.equals("Y") && isLimitIncludesTax.equals("Y")) {
				query = query.replaceAll(" # ", " (bc.amount+bc.return_amt+bc.original_tax_amt+bc.return_original_tax_amt) ");
				query = query.replaceAll(" @ ", " (((ssd.amount - ssd.tax) + ssd.original_tax_amt) + ((ssd.return_amt + ssd.return_tax_amt) - ssd.return_original_tax_amt)) "); //ssd.amount is already has tax amount
			} else {
				query = query.replaceAll(" # ", " (bc.amount+bc.return_amt) ");
				query = query.replaceAll(" @ ", " ((ssd.amount+ssd.return_amt)-(ssd.tax+ssd.return_tax_amt)) ");
			}
			
      if (includeFollowUpVisits) {
        query = query.replaceAll("##", followUpvisits);
      }

      ps = con.prepareStatement(query);
      if (!includeFollowUpVisits) {
        ps.setString(1, visitId);
        ps.setString(2, visitId);
      }

			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	  private static final String GET_MAIN_AND_FOLLOWUP_VISIT_BILL_CHARGE_CLAIM =" SELECT * FROM ( " +
	    "   SELECT bcc.charge_id, bcc.claim_id, bcc.sponsor_id, bcc.insurance_claim_amt, "+
	    "   bcc.copay_ded_adj, bcc.max_copay_adj, bcc.sponsor_limit_adj, bcc.copay_perc_adj, bcc.insurance_category_id, "+
	    " bcc.include_in_claim_calc, COALESCE(bcc.tax_amt, 0) AS tax_amt "+
	    " FROM bill b "+
	    " JOIN bill_claim bcl ON(b.bill_no = bcl.bill_no) " +
	    " JOIN bill_charge_claim bcc ON(bcl.bill_no = bcc.bill_no AND bcl.claim_id = bcc.claim_id) " +
	    " JOIN bill_charge bc ON(bc.charge_id = bcc.charge_id) "+
	    " JOIN patient_registration pr ON(pr.patient_id = bcl.visit_id) "+
	    "   WHERE (pr.patient_id in(##)) AND bcl.plan_id = ? AND pr.op_type IN ('M', 'F','D','R','O') AND " +
	    " bcc.charge_head NOT IN('PHCMED','PHMED','PHRET','PHCRET','INVRET') AND bc.status != 'X' AND b.total_amount >= 0 " +
	    " UNION ALL "+
	    " SELECT bc.charge_id || '-' || ssd.sale_item_id as charge_id, scd.claim_id, scd.sponsor_id, scd.ref_insurance_claim_amount as insurance_claim_amt,  "+
	    " 0 as copay_ded_adj, 0 as max_copay_adj, 0 as sponsor_limit_adj, 0 as copay_perc_adj, scd.insurance_category_id, "+
	    "   scd.include_in_claim_calc, COALESCE(scd.tax_amt, 0) AS tax_amt "+
	    "   FROM bill b "+
	    " JOIN bill_claim bcl ON(b.bill_no = bcl.bill_no) "+
	    " JOIN store_sales_main ssm ON(bcl.bill_no = ssm.bill_no AND ssm.type = 'S') "+
	    " JOIN bill_charge bc ON(ssm.charge_id = bc.charge_id) "+
	    " JOIN store_sales_details ssd ON(ssd.sale_id = ssm.sale_id) "+
	    " JOIN sales_claim_details scd ON(scd.sale_item_id = ssd.sale_item_id AND bcl.claim_id = scd.claim_id) "+
	    " JOIN patient_registration pr ON(pr.patient_id = bcl.visit_id) "+
	    " WHERE (pr.patient_id in(##)) AND bcl.plan_id = ? AND pr.op_type IN ('M', 'F','D','R','O') "+
	    "   AND bc.status != 'X' AND b.total_amount >= 0 "+
	    " ) AS foo ";

	private static final String GET_VISIT_BILL_CHARGE_CLAIM =" SELECT * FROM ( " +
		"  SELECT bcc.charge_id, bcc.claim_id, bcc.sponsor_id, bcc.insurance_claim_amt,  "+
		"  bcc.copay_ded_adj, bcc.max_copay_adj, bcc.sponsor_limit_adj, bcc.copay_perc_adj, bcc.insurance_category_id, "+
		"  bcc.include_in_claim_calc,COALESCE(bcc.tax_amt, 0) as tax_amt "+
		"   FROM bill b "+
		"	JOIN bill_claim bcl ON(b.bill_no = bcl.bill_no) " +
		"	JOIN bill_charge_claim bcc ON(bcl.bill_no = bcc.bill_no AND bcl.claim_id = bcc.claim_id) " +
		"	JOIN bill_charge bc ON(bc.charge_id = bcc.charge_id) "+
		" 	WHERE bcl.visit_id = ? AND bcl.plan_id = ?  AND " +
		"	bcc.charge_head NOT IN('PHCMED','PHMED','PHRET','PHCRET','INVRET') AND bc.status != 'X'  AND b.total_amount >= 0" +
		"	UNION ALL "+
		"	SELECT bc.charge_id || '-' || ssd.sale_item_id as charge_id, scd.claim_id, scd.sponsor_id, COALESCE(scd.ref_insurance_claim_amount,0) as insurance_claim_amt, "+
		"	0 as copay_ded_adj, 0 as max_copay_adj, 0 as sponsor_limit_adj, 0 as copay_perc_adj, scd.insurance_category_id, "+
		"	scd.include_in_claim_calc, COALESCE(scd.tax_amt, 0) as tax_amt "+
		"   FROM bill b "+
		"	JOIN bill_claim bcl ON(b.bill_no = bcl.bill_no) "+
		"	JOIN store_sales_main ssm ON(bcl.bill_no = ssm.bill_no AND ssm.type = 'S') "+
		"	JOIN bill_charge bc ON(ssm.charge_id = bc.charge_id) "+
		"	JOIN store_sales_details ssd ON(ssd.sale_id = ssm.sale_id) "+
		"	JOIN sales_claim_details scd ON(scd.sale_item_id = ssd.sale_item_id AND bcl.claim_id = scd.claim_id) "+
		"	WHERE bcl.visit_id = ? AND bcl.plan_id = ?  AND bc.status != 'X'  AND b.total_amount >= 0 "+
		"	) AS foo  ";

  public List<BasicDynaBean> getVisitBillChargeClaims(String visitId, int planId,
      Boolean includeFollowUpVisits, String followUpVisitIds) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      String query = includeFollowUpVisits ? GET_MAIN_AND_FOLLOWUP_VISIT_BILL_CHARGE_CLAIM
          : GET_VISIT_BILL_CHARGE_CLAIM;
      if (includeFollowUpVisits) {
        query = query.replaceAll("##", followUpVisitIds);
      }
      ps = con.prepareStatement(query);
      if (includeFollowUpVisits) {
        ps.setInt(1, planId);
        ps.setInt(2, planId);
      } else {
        ps.setString(1, visitId);
        ps.setInt(2, planId);
        ps.setString(3, visitId);
        ps.setInt(4, planId);
      }

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

	private static final String IS_CHARGE_CLAIM_LOCKED = "SELECT is_claim_locked FROM bill_charge WHERE charge_id= ? ";

	public Boolean isChargeItemLocked(String chargeId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		Boolean isChargeItemLocked = false;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(IS_CHARGE_CLAIM_LOCKED);
			ps.setString(1, chargeId);
			BasicDynaBean chargeBean = DataBaseUtil.queryToDynaBean(ps);
			if(null != chargeBean){
				isChargeItemLocked = (Boolean)chargeBean.get("is_claim_locked");
			}
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

		return isChargeItemLocked;
	}

	private static final String GET_BIll_ADJUSTMENT_ALERTS = "SELECT * " +
			" FROM bill_adjustment_alerts baa " +
			" JOIN patient_insurance_plans pip ON(pip.patient_id = baa.visit_id AND pip.plan_id = baa.plan_id) "+
			" JOIN tpa_master tpa ON(tpa.tpa_id = pip.sponsor_id) "+
			" LEFT JOIN item_insurance_categories iic ON(baa.category_id=iic.insurance_category_id)" +
			" WHERE baa.visit_id = ? " ;


	public List<BasicDynaBean> getBillAdjustmentAlerts(Connection con, String visitId) throws SQLException{

		try(PreparedStatement ps = con.prepareStatement(GET_BIll_ADJUSTMENT_ALERTS);){
			ps.setString(1,  visitId);
			return DataBaseUtil.queryToDynaList(ps);
		}
	}
	
	private static final String GET_CREDIT_NOTE_DETAILS = " SELECT bcn.bill_no, SUM(b.total_amount) as total_amount,"+ 
		" SUM(b.total_claim) as total_claim, SUM(b.total_amount-b.total_claim) as total_pat_amt "+ 
		" FROM bill_credit_notes bcn "+ 
		" JOIN bill b ON(bcn.credit_note_bill_no = b.bill_no) "+  
		" WHERE bcn.bill_no = ? and not b.status='X' GROUP BY bcn.bill_no "; 

	public BasicDynaBean getCreditNoteDetails(String billNo) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_CREDIT_NOTE_DETAILS);
			ps.setString(1, billNo);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public BasicDynaBean getCreditNoteDetails(Connection con, String billNo) throws SQLException{
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(GET_CREDIT_NOTE_DETAILS);
			ps.setString(1, billNo);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			if(null != ps) ps.close();
		}
	}

	private static final String GET_PATIENT_CHARGE_AMT = "select bc.orig_charge_id,sum(bc.amount) as tot_charge_amt" +
		" From bill_credit_notes bcn join bill_charge bc on(bcn.credit_note_bill_no = bc.bill_no AND bc.insurance_claim_amount = 0) " + 
		 " where bcn.bill_no=? GROUP BY bc.orig_charge_id  "; 

	/*
	 * Return List of sum of all patient credit note for each item.
	 */
	public  List<BasicDynaBean> getPatientChargeAmount(String billNo) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_PATIENT_CHARGE_AMT);
			ps.setString(1, billNo);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String GET_SPONSOR_CHARGE_AMT = "select bc.orig_charge_id,sum(bcc.insurance_claim_amt) as tot_charge_amt  From bill_credit_notes bcn " + 
			" join bill_charge bc on(bcn.credit_note_bill_no = bc.bill_no) " +
			" join bill_claim bcl on(bcl.bill_no = bcn.credit_note_bill_no AND bcl.priority = ?) " +
			" join bill_charge_claim bcc on(bcc.claim_id = bcl.claim_id AND bcc.bill_no = bcn.credit_note_bill_no AND bc.charge_id = bcc.charge_id) " +
			" where bcn.bill_no=? GROUP BY bc.orig_charge_id" ;
	
	/*
	 * Return list of sum of all sponsor (primary, secondary) credit note for each item.
	 * Priority =1 means primary Sponsor
	 * Priority =2 means Secondary Sponsor
	 */
	public  List<BasicDynaBean> getSponsorChargeAmount(String billNo, Integer priority) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_SPONSOR_CHARGE_AMT);
			ps.setInt(1, priority);
			ps.setString(2, billNo);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String GET_INSURANCE_RECV_AMT = "SELECT bcc.charge_id, " +
				" CASE WHEN bcc.claim_recd_total IS NULL THEN 0 ELSE bcc.claim_recd_total END as claim_recieved " +
				" from bill b " +
				" join bill_claim bcl on(b.bill_no = bcl.bill_no AND bcl.priority = ?) " + 
				" join bill_charge bc on(bc.bill_no = b.bill_no) " +
				" join bill_charge_claim bcc on(bcc.claim_id = bcl.claim_id and bcl.bill_no = bcc.bill_no and bc.charge_id = bcc.charge_id) " + 
				" where b.bill_no=? ";
	
	public List<BasicDynaBean> getInsuranceRecievedAmt(String billNo, Integer priority) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_INSURANCE_RECV_AMT);
			ps.setInt(1, priority);
			ps.setString(2, billNo);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String GET_SPONSOR_TYPE = "SELECT sum(insurance_claim_amt) FROM bill_claim b " +
	        "LEFT JOIN bill_charge_claim blc ON (b.bill_no = blc.bill_no AND b.claim_id = blc.claim_id) " +
		"WHERE b.bill_no = ? AND priority = ?";
	

	public BasicDynaBean getSponsorType(String billNo ,Integer priority) throws SQLException{
	    Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_SPONSOR_TYPE);
			ps.setString(1, billNo);
			ps.setInt(2, priority);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_CREDIT_NOTES_LIST = " SELECT bcn.*,b.* "+ 
			" FROM bill_credit_notes bcn "+ 
			" JOIN bill b ON(bcn.credit_note_bill_no = b.bill_no) "+  
			" WHERE bcn.bill_no = ? AND b.status!='X' "; 
	
	public List<BasicDynaBean> getListOfCreditNotesOfBill(String billNo) throws SQLException{
		//Connection con = null;
		PreparedStatement ps = null;
		try{
			//con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_CREDIT_NOTES_LIST);
			ps.setString(1, billNo);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}
	
	private static final String GET_BILL_TOTALS = "SELECT sum(total_amount) AS amount, sum(total_claim) AS sponsor_amount, "+
		" sum(total_amount+(b.total_tax-b.total_claim_tax)  - total_claim) AS patient_amt, sum(total_receipts) as total_receipts, "+ 
		" sum(b.total_amount+(b.total_tax-b.total_claim_tax) - " +
		" b.primary_total_claim-b.secondary_total_claim - total_receipts - deposit_set_off - points_redeemed_amt) AS patient_due, "+
		" sum(b.total_claim - b.primary_total_sponsor_receipts - b.secondary_total_sponsor_receipts - claim_recd_amount) AS sponsor_due "+
		" FROM bill b "+
		" WHERE visit_id=? ";
	

	public BasicDynaBean getTotalBillAmounts(String visitId) throws SQLException{
		return DataBaseUtil.queryToDynaBean(GET_BILL_TOTALS,visitId);
	}
	
	private static final String GET_VISIT_BILL_TOTALS = " SELECT  sum(amount) as amount, sum(sponsor_amount) as sponsor_amount, sum(total_receipts) as total_receipts , "+
			" sum(patient_amt) as patient_amt, sum(patient_due) as patient_due, sum(sponsor_due) as sponsor_due " +
			" FROM ( "+
				" SELECT sum(total_amount+total_tax) AS amount, sum(total_claim+total_claim_tax) AS sponsor_amount, "+
				" sum(total_amount + total_tax - total_claim - total_claim_tax) AS patient_amt, sum(total_receipts) as total_receipts,"+ 
				" sum(b.total_amount + total_tax - total_claim - total_claim_tax - total_receipts - deposit_set_off - points_redeemed_amt) AS patient_due, "+
				" sum(b.total_claim + total_claim_tax - b.primary_total_sponsor_receipts - b.secondary_total_sponsor_receipts - claim_recd_amount) AS sponsor_due "+
				" FROM bill b "+
				" WHERE b.is_tpa = true and visit_id=? "+
			
			" UNION ALL " +
	
				" SELECT sum(total_amount+total_tax) as amount ,0 as sponsor_amount, "+
				" sum(total_amount + total_tax) AS patient_amt, sum(total_receipts) as total_receipts,"+ 
				" sum(b.total_amount + total_tax - total_receipts - deposit_set_off - points_redeemed_amt) AS patient_due, "+
				" 0 AS sponsor_due "+
				" FROM bill b "+
				" WHERE b.is_tpa = false and visit_id=? "+
			" ) AS foo";



	public BasicDynaBean getVisitTotalBillAmounts(String visitId) throws SQLException{
		return DataBaseUtil.queryToDynaBean(GET_VISIT_BILL_TOTALS,new Object[]{visitId, visitId} );
	}
	
	private static final String GET_RATE_PLAN_DISCOUNT = "select sum(overall_discount_amt) from bill_charge where overall_discount_auth=-1 and bill_no=?";

	public static BigDecimal getRatePlanTotalDiscount(String billNo)
			throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_RATE_PLAN_DISCOUNT);
			ps.setString(1, billNo);
			return DataBaseUtil.getBigDecimalValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public static final String GET_PAYMENT_MODE_AMOUNT = " SELECT sum(r.amount) AS amount" 
									+ " FROM receipts r "
									+ " JOIN bill_receipts br ON r.receipt_id = br.receipt_no "
									+ " JOIN bill b ON br.bill_no = b.bill_no"
									+ " JOIN payment_mode_master pm ON (r.payment_mode_id = pm.mode_id)"
									+ " WHERE b.visit_id =? AND pm.mode_id =? ";
	public static String getpaymentmodeamount(String visit_id,
			String mode_id) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		String amount = null ;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_PAYMENT_MODE_AMOUNT);
			ps.setString(1, visit_id);
			ps.setInt(2,Integer.parseInt(mode_id));
			amount = DataBaseUtil.getStringValueFromDb(ps);

		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return amount;
	}

	public List<BasicDynaBean> getItemSubgroupCodes(String itemId,
			String chargeGroup) throws SQLException{
		// TODO Auto-generated method stub
		List<BasicDynaBean> itemSubGroupCodes = new ArrayList<BasicDynaBean>();
		
		if(chargeGroup.equals("SNP")){
			itemSubGroupCodes = new ServiceMasterDAO().getServiceItemSubGroupTaxDetails(itemId);
		}
		
		return itemSubGroupCodes;
	}

	public static final String GET_REPORT_IDS_FOR_BILL= "SELECT report_id from "+
										    " (SELECT distinct tp.report_id"+
											" FROM bill b"+
											" JOIN bill_charge bc on (bc.bill_no = b.bill_no)"+
											" JOIN bill_activity_charge bac on (bac.charge_id = bc.charge_id and activity_code='DIA')"+
											" JOIN tests_prescribed tp on (tp.prescribed_id = bac.activity_id::integer and tp.coll_prescribed_id is null and tp.mr_no is not null)"+
											" WHERE tp.conducted = 'S' AND b.bill_no = ? ) as foo"+
											" WHERE not exists (select 1 from tests_prescribed join diagnostics using(test_id)"+
											" WHERE report_id =foo.report_id  and isconfidential = 'true')";
	public static List<String> getSignoffReportIdsforBill(String billNo) throws SQLException {
		PreparedStatement ps = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_REPORT_IDS_FOR_BILL);
			ps.setString(1, billNo);
			return DataBaseUtil.queryToOnlyArrayList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public static String GET_ALL_ACTIVE_BILLS_FOR_VISIT = " select bill_no, is_tpa, bill_rate_plan_id, dyna_package_id,"
	    + " bill_type, mod_time, dyna_package_charge from bill where visit_id = ? "
			+ " and status = 'A' and restriction_type='N' ORDER BY open_date desc";
	
	public static List<BasicDynaBean> getAllActiveBillsNew(String visitId) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_ALL_ACTIVE_BILLS_FOR_VISIT, new Object[]{visitId}); 
	}	
	
	private static final String GET_VISIT_CASH_BILLS ="SELECT * FROM bill b WHERE b.is_tpa=false AND b.visit_id = ? ";
  
  @SuppressWarnings("unchecked")
  public static List<BasicDynaBean> getVisitCashbills(String visitId) throws SQLException{
    // TODO Auto-generated method stub
    return DataBaseUtil.queryToDynaList(GET_VISIT_CASH_BILLS, visitId);
  }

  public static String getNextWriteOffId(String writeOffType) throws SQLException {
    String seq = null;
    String typeNum = null;

    if (writeOffType.equalsIgnoreCase("P")) {
      seq = "patient_writeoff_receipt_sequence";
      typeNum = "write_off_patient";
    } else if (writeOffType.equalsIgnoreCase("S")) {
      seq = "sponsor_writeoff_receipt_sequence";
      typeNum = "write_off_sponsor";
    }

    return AutoIncrementId.getSequenceId(seq, typeNum);
  }
  
	public static String GET_BILLS_FOR_API = "SELECT "
			+ " pr.mr_no, pr.patient_id as visit_id, cm.center_name, pr.center_id, b.bill_no,"
			+ " CASE WHEN b.bill_type='P' THEN 'BILL_NOW' ELSE 'BILL_LATER' END  AS bill_type, b.opened_by, "
			+ " CASE WHEN b.status = 'A' THEN 'OPEN' WHEN b.status = 'F' THEN 'FINALIZED' WHEN b.status = 'C' THEN 'CLOSED' ELSE 'CANCELLED' END  AS bill_status, "
			+ " b.is_tpa AS insurance_linked_bill, "
			+ " CASE WHEN b.payment_status = 'P' THEN 'PAID' ELSE 'UNPAID' END  AS payment_status, "
			+ " (b.total_amount + b.total_discount) AS bill_amount, "
			+ " b.total_discount, b.total_tax, b.total_amount as net_amount, "
			+ " (b.total_amount + b.total_tax - b.total_claim - b.total_claim_tax) AS patient_amount, "
			+ " (b.total_tax - b.total_claim_tax ) AS patient_tax_amt, b.points_redeemed_amt, "
			+ " b.total_receipts as total_patient_payments, b.deposit_set_off, "
			+ " (b.total_amount + b.total_tax - b.total_receipts - b.total_claim - b.total_claim_tax  - b.deposit_set_off - b.points_redeemed_amt) AS patient_due_amount, "
			+ " to_char(b.open_date AT TIME ZONE (SELECT current_setting('TIMEZONE')) AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS open_date, "
			+ " to_char(b.finalized_date AT TIME ZONE (SELECT current_setting('TIMEZONE')) AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS finalized_date, "
			+ " d.dept_name AS treating_department,  "
			+ " to_char(pr.discharge_date AT TIME ZONE (SELECT current_setting('TIMEZONE')) AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS discharge_date,  "
			+ " pr.discharge_time, to_char(pr.discharge_finalized_date AT TIME ZONE (SELECT current_setting('TIMEZONE')) AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS discharge_finalized_date, "
			+ " pr.discharge_finalized_time "
			+ " FROM bill b "
			+ " JOIN patient_registration pr ON (pr.patient_id = b.visit_id) "
			+ " JOIN hospital_center_master cm ON (cm.center_id=pr.center_id) "
			+ " LEFT JOIN department d ON d.dept_id = pr.admitted_dept "
			+ " WHERE (@ BETWEEN ? AND ?)";

	public static String GET_RECEIPTS_BY_BILL = "SELECT r.bill_no, r.receipt_no, r.amount, c.counter_no AS counter, "
			+ " r.username as collected_by, p.payment_mode, "
			+ " CASE WHEN r.payment_type = 'R' THEN 'RECEIPT' WHEN r.payment_type = 'F' THEN 'REFUND' WHEN r.payment_type = 'S' THEN 'RECEIPT' END AS receipt_type, "
			+ " r.payment_type = 'S' as paid_by_sponsor, "
			+ " CASE WHEN r.recpt_type = 'A' THEN 'ADVANCE' ELSE 'SETTLEMENT' END AS receipt_subtype, "
			+ " to_char(r.display_date AT TIME ZONE (SELECT current_setting('TIMEZONE')) AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as receipt_date, "
			+ " r.payment_mode_id, r.payment_type, r.card_number, r.bank_name, r.card_number, r.bank_name, bm.bank_id "
			+ " FROM bill_receipts r "
			+ " JOIN payment_mode_master p ON (p.mode_id = r.payment_mode_id) "
			+ " JOIN counters c ON (c.counter_id = r.counter) "
			+ " LEFT JOIN bank_master bm ON bm.bank_name = r.bank_name "
			+ " WHERE r.bill_no in (@)";

	public static String GET_CHARGES_BY_BILL = "SELECT bc.bill_no, bc.charge_id, "
			+ " to_char(bc.posted_date AT TIME ZONE (SELECT current_setting('TIMEZONE')) AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as posted_date,"
			+ " cg.chargegroup_name as charge_group, ch.chargehead_name AS charge_head, "
			+ " bc.act_description AS description, bc.act_remarks as details, bc.act_rate as rate, "
			+ " bc.act_quantity as quantity, bc.amount, bc.discount, COALESCE(bc.tax_amt, 0) as tax_amount, "
			+ " bc.doctor_amount AS doctor_net_amount, d.doctor_name AS conducting_doctor, "
			+ " (COALESCE(bc.amount,0) + COALESCE(bc.tax_amt,0) - COALESCE(bc.doctor_amount,0) - COALESCE(bc.prescribing_dr_amount,0) - COALESCE(bc.referal_amount,0) + COALESCE(bc.hosp_discount_amt,0) ) AS hospital_amount "
			+ " FROM bill_charge bc "
			+ " JOIN chargegroup_constants cg ON (cg.chargegroup_id = bc.charge_group) "
			+ " JOIN chargehead_constants ch ON (ch.chargehead_id = bc.charge_head) "
			+ " LEFT JOIN doctors d ON d.doctor_id = bc.payee_doctor_id "
			+ " WHERE bc.status != 'X' AND bc.bill_no in (@)";

	public static List<Map<String, Object>> getBills(Connection con, Object fromTime, 
				Object toTime, String mrNo, Integer centerId, long page, Boolean useFinalizedDate) throws SQLException {
		Map<String, List<Object>> receipts = new HashMap<String, List<Object>>();
		Map<String, List<Object>> charges = new HashMap<String, List<Object>>();
		String billQuery = GET_BILLS_FOR_API.replace("@", useFinalizedDate ? "b.finalized_date" : "b.open_date");
		List<Object> args = new ArrayList<Object>();
		List<String> billNos = new ArrayList<String>();
		if (page < 1) {
			page = 1;
		}
		args.add(fromTime);
		args.add(toTime);
		if (mrNo !=null && !mrNo.isEmpty()) {
			billQuery += " AND pr.mr_no = ?";
			args.add(mrNo);
		}
		if (centerId !=null) {
			billQuery += " AND pr.center_id = ?";
			args.add(centerId);
		}
		billQuery += " ORDER BY " + (useFinalizedDate ? "b.finalized_date" : "b.open_date");
		billQuery += " LIMIT 100 OFFSET ?";
		args.add((page - 1) * 100);
		List<Map<String,Object>> billsList = ConversionUtils.copyListDynaBeansToMap(
			DataBaseUtil.queryToDynaList(con, billQuery, args.toArray()));	
		if (billsList.isEmpty()) {
			return billsList;
		}
		for (Map<String,Object> bill : billsList) {
			billNos.add((String) bill.get("bill_no"));
		}
        String[] placeHolderArr = new String[billNos.size()];
        Arrays.fill(placeHolderArr, "?");
        String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
		List<Map<String,Object>> receiptList = ConversionUtils.copyListDynaBeansToMap(
			DataBaseUtil.queryToDynaList(con, GET_RECEIPTS_BY_BILL.replace("@", placeHolders), billNos.toArray()));	
		List<Map<String,Object>> chargeList = ConversionUtils.copyListDynaBeansToMap(
			DataBaseUtil.queryToDynaList(con, GET_CHARGES_BY_BILL.replace("@", placeHolders), billNos.toArray()));
		// Transform Simple list of receipts to List of receipts grouped by bill_no
		for (Map<String,Object> receipt : receiptList) {
			String thisBill = (String) receipt.get("bill_no");
			if (!receipts.containsKey(thisBill)) {
				receipts.put(thisBill, new ArrayList<Object>());
			}
			receipts.get(thisBill).add(receipt);
		}
		// Transform Simple list of charges to List of charges grouped by bill_no
		for (Map<String,Object> charge : chargeList) {
			String thisBill = (String) charge.get("bill_no");
			if (!charges.containsKey(thisBill)) {
				charges.put(thisBill, new ArrayList<Object>());
			}
			charges.get(thisBill).add(charge);
		}
		// Inject charges anf receipts list into individual bill map
		List<Map<String,Object>> bills = new ArrayList<Map<String,Object>>();
		for (Map<String,Object> bill : billsList) {
			String billNo = (String) bill.get("bill_no");
			Map<String,Object> billMap = new HashMap<String, Object>();
			billMap.putAll(bill); 
			billMap.put("receipts", receipts.containsKey(billNo) ? receipts.get(billNo) : new ArrayList<Object>());
			billMap.put("charges", charges.containsKey(billNo) ? charges.get(billNo) : new ArrayList<Object>());
			bills.add(billMap);
		}		
		return bills;
	}

	private static final String UPDATE_BILL_CHARGE = "UPDATE bill_charge SET is_system_discount = 'Y' WHERE bill_no = ?";

  public static boolean updateIsSystemDiscountToYes(String billNo) throws SQLException {
    Connection connection = DataBaseUtil.getConnection();
    PreparedStatement updateStatement = null;
    try {
      updateStatement = connection.prepareStatement(UPDATE_BILL_CHARGE);
      updateStatement.setString(1, billNo);
      return updateStatement.executeUpdate() > 0;
    } finally {
      DataBaseUtil.closeConnections(connection, updateStatement);
    }
  }
  
  private static final String GET_CONSULTING_DOCTORS_LIST = "SELECT d.doctor_name, "
      + " d.doctor_license_number, bcc.insurance_claim_amt "
      + " FROM bill b "
      + " JOIN bill_claim bcl ON(b.bill_no = bcl.bill_no AND bcl.priority = 1) "
      + " JOIN bill_charge_claim bcc ON(bcl.bill_no = bcc.bill_no AND bcc.claim_id = bcl.claim_id)"
      + " JOIN bill_charge bc ON(bc.charge_id = bcc.charge_id AND bc.charge_group = 'DOC') "
      + " JOIN doctors d ON(d.doctor_id = bc.act_description_id)"
      + " WHERE b.visit_id = ? ";

  public List<BasicDynaBean> getConsultingDoctorsList(String patientId) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_CONSULTING_DOCTORS_LIST, new Object[] { patientId });
  }

  private static final String GET_ORDERED_OPERATION_LIST = "SELECT act_description as operation_name, "
      + " posted_date as order_date, act_rate_plan_item_code as ope_rateplan_code "
      + " FROM bill_charge bc "
      + " JOIN bill b ON(bc.bill_no = b.bill_no) "
      + " WHERE b.visit_id = ? AND bc.charge_head = 'SACOPE' AND bc.charge_group = 'OPE' ";

  public List<BasicDynaBean> getOrderedOperationList(String patientId) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_ORDERED_OPERATION_LIST, new Object[] { patientId });
  }
  
  private static final String GET_MULTI_VISIT_PKG_DETAILS  = " SELECT patp.mr_no, pp.patient_id, mbv.bill_no, pp.package_id, pp.pat_package_id, "
    + " patp.status, p.package_name,  "
    + " p.description, p.package_code "
    + " FROM bill b JOIN LATERAL ( "
    + " SELECT orders.*"
    + " FROM (SELECT bc.*, p.package_id as pack_id  "
    + " FROM services_prescribed sp  "
    + " JOIN bill_charge bc ON(bc.order_number = sp.common_order_id) "
    + " JOIN package_prescribed pp ON (sp.package_ref = pp.prescription_id) "
    + " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
    + " where b.bill_no = bc.bill_no AND sp.patient_id = ? "
    + " UNION ALL "
    + " SELECT bc.*,p.package_id as pack_id  "
    + " FROM tests_prescribed tp "
    + " JOIN bill_charge bc ON(bc.order_number = tp.common_order_id) "
    + " JOIN package_prescribed pp ON (tp.package_ref = pp.prescription_id) "
    + " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
    + " where b.bill_no = bc.bill_no AND tp.pat_id = ?"
    + " UNION ALL "
    + " SELECT bcc.*,p.package_id as pack_id  "
    + " FROM doctor_consultation dc  "
    + " JOIN bill_charge bcc ON(bcc.order_number = dc.common_order_id) "
    + " JOIN package_prescribed pp ON (dc.package_ref = pp.prescription_id) "
    + " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
    + " LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text AND bac.activity_code='DOC' "
    + " LEFT JOIN bill_charge bc ON (bc.charge_id = bac.charge_id) "
    + " where b.bill_no = bc.bill_no AND dc.patient_id = ?"
    + " UNION ALL "
    + " SELECT bc.*, p.package_id as pack_id  "
    + " FROM other_services_prescribed osp  "
    + " JOIN bill_charge bc ON (bc.order_number = osp.common_order_id) "
    + " JOIN package_prescribed pp ON (osp.package_ref = pp.prescription_id) "
    + " JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true) "
    + " where b.bill_no = bc.bill_no AND osp.patient_id = ?"
    + " UNION ALL "
    + " SELECT bc.*, p.package_id as pack_id  "
    + " FROM bill_charge bc  "
    + " JOIN packages p ON (p.package_id = bc.package_id AND p.multi_visit_package = true) "
    + " where b.bill_no = bc.bill_no AND b.visit_id = ? AND bc.charge_group='BED'"
    + " ) as orders) as mbv ON (mbv.bill_no = b.bill_no) "
    + " LEFT JOIN package_prescribed pp ON (mbv.order_number = pp.common_order_id "
    + " and mbv.pack_id = pp.package_id) "
    + " LEFT JOIN patient_packages patp ON (pp.pat_package_id = patp.pat_package_id) "
    + " JOIN packages p ON (mbv.pack_id = p.package_id) where b.bill_no =? ";

  public BasicDynaBean getMultivisitPatientPackageDetails(String billNo,String visitId) throws SQLException {
    try(Connection con = DataBaseUtil.getReadOnlyConnection();
	PreparedStatement ps = con.prepareStatement(GET_MULTI_VISIT_PKG_DETAILS);) {
      ps.setString(1, visitId);
      ps.setString(2, visitId);
	  ps.setString(3, visitId);
	  ps.setString(4, visitId);
	  ps.setString(5, visitId);
	  ps.setString(6, billNo);
      return DataBaseUtil.queryToDynaBean(ps);
    }
  }
  
  private static final String GET_CLOSED_FINALIZED_HAVING_INVITE = "select b.bill_no as bill_no "
      + "FROM bill b LEFT JOIN bill_charge c on b.bill_no = c.bill_no "
      + "where b.visit_id= ? and b.status in ('C','F') and c.charge_head = ? ";

  public static List<String> getClosedAndFinalizedBillHavingChargeHead(String visitId,String chargeHead) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    try {
        con = DataBaseUtil.getReadOnlyConnection();
        ps = con.prepareStatement(GET_CLOSED_FINALIZED_HAVING_INVITE);
        ps.setString(1, visitId);
        ps.setString(2, chargeHead);
        return DataBaseUtil.queryToOnlyArrayList(ps);
    } finally{
        DataBaseUtil.closeConnections(con, ps);
    }
  }
  
  private static final String GET_TESTS_NOT_COMPLETED = "SELECT 'Doctor' AS type,"
      + " dcd.doctor_name AS item_name, dc.status" 
      + " FROM doctor_consultation dc"
      + " JOIN doctors dcd ON (dcd.doctor_id = dc.doctor_name and"
      + " cancel_status is null and dc.status <> 'C')"
      + " LEFT JOIN doctors pd ON pd.doctor_id = dc.presc_doctor_id"
      + " LEFT JOIN consultation_types ct ON (ct.consultation_type_id::text = dc.head)"
      + " LEFT JOIN chargehead_constants chc ON (chc.chargehead_id = dc.ot_doc_role)" 
      + " LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text "
      + " AND bac.activity_code='DOC'"
      + " LEFT JOIN bill_charge bc USING (charge_id)"
      + " JOIN bill b on (b.bill_no = bc.bill_no and visit_type <> 'i')"
      + " where b.bill_no = ?";

  public static List getPendingConsultationForBill(Connection con, String billNo) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_TESTS_NOT_COMPLETED);
      ps.setString(1, billNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }
  
  private static final String GET_ACTIVE_VISIT = "SELECT b.bill_no, b.visit_id, b.visit_type FROM"
      + " bill b  WHERE  b.status='A' AND b.restriction_type='N' AND b.visit_id=?  "
      + " AND is_primary_bill = 'Y'";

  public static BasicDynaBean getActiveBill(String visitId) throws SQLException {
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(GET_ACTIVE_VISIT);) {
      ps.setString(1, visitId);
      return DataBaseUtil.queryToDynaBean(ps);
    }
  }
}
