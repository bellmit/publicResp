package com.insta.hms.master.PatientCategory;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;


public class PatientCategoryDAO extends GenericDAO{

	public PatientCategoryDAO(){
		super("patient_category_master");
	}

	public int getNextId() throws SQLException {
		int categoryId = getNextSequence();
		return categoryId;
	}


	private static final String GET_CATEGORY_MASTER__INC_SUPER_DENTER = "SELECT * from patient_category_master WHERE status='A' AND " +
			" center_id in (?,0) order by category_name";

	public static List<BasicDynaBean> getAllCategoriesIncSuperCenter(int centerId) throws SQLException {

		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		List<BasicDynaBean> categoryList = null;
		try {
			ps = con.prepareStatement(GET_CATEGORY_MASTER__INC_SUPER_DENTER);
			ps.setInt(1, centerId);
			categoryList = DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		return categoryList;

	}

	private static final String GET_CATEGORY_MASTER_DETAILS = "SELECT * from patient_category_master WHERE " +
		" center_id =? order by category_name";

	public static List<BasicDynaBean> getAllCategories(int centerId) throws SQLException {
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		List<BasicDynaBean> categoryList = null;
		try {
			ps = con.prepareStatement(GET_CATEGORY_MASTER_DETAILS);
			ps.setInt(1, centerId);
			categoryList = DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		return categoryList;

	}



	private static final String GET_ALL_CATEGORIES =
		"	SELECT * FROM patient_category_master WHERE status='A' ORDER BY category_name ";
	public static List<BasicDynaBean> getAllCategories() throws SQLException {
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		List<BasicDynaBean> categoryMasterDetails = null;
		try {
			ps = con.prepareStatement(GET_ALL_CATEGORIES);
			categoryMasterDetails = DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		return categoryMasterDetails;

	}



    private static final String IP_ALLOWED_SPONSORS_QUERY =
        "SELECT t.tpa_id, t.tpa_name FROM tpa_master t  "
            + "JOIN (select * from patient_category_master where category_id=?) p "
            + "ON p.ip_allowed_sponsors='*' " + "OR p.ip_allowed_sponsors LIKE t.tpa_id "
            + "OR p.ip_allowed_sponsors LIKE t.tpa_id || ',%' "
            + "OR p.ip_allowed_sponsors LIKE '%,' || t.tpa_id || ',%' "
            + "OR p.ip_allowed_sponsors LIKE '%,' || t.tpa_id " + "WHERE t.status='A';";

    private static final String OP_ALLOWED_SPONSORS_QUERY =
        "SELECT t.tpa_id, t.tpa_name FROM tpa_master t  "
            + "JOIN (select * from patient_category_master where category_id=?) p "
            + "ON p.op_allowed_sponsors='*' " + "OR p.op_allowed_sponsors LIKE t.tpa_id "
            + "OR p.op_allowed_sponsors LIKE t.tpa_id || ',%' "
            + "OR p.op_allowed_sponsors LIKE '%,' || t.tpa_id || ',%' "
            + "OR p.op_allowed_sponsors LIKE '%,' || t.tpa_id " + "WHERE t.status='A';";

	public static List getAllowedSponsors(int categoryId, String visitType) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			if(visitType.equals("i"))
				ps = con.prepareStatement(IP_ALLOWED_SPONSORS_QUERY);
			else
				ps = con.prepareStatement(OP_ALLOWED_SPONSORS_QUERY);
			ps.setInt(1, categoryId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

    private static final String IP_ALLOWED_INSURANCE_COMPANIES_QUERY =
        "SELECT ic.insurance_co_id, ic.insurance_co_name, ic.status, ic.insurance_rules_doc_name"
            + " FROM insurance_company_master ic "
            + "JOIN (select * from patient_category_master where category_id=?) p "
            + "ON p.ip_allowed_insurance_co_ids='*' "
            + "OR p.ip_allowed_insurance_co_ids LIKE ic.insurance_co_id "
            + "OR p.ip_allowed_insurance_co_ids LIKE ic.insurance_co_id || ',%' "
            + "OR p.ip_allowed_insurance_co_ids LIKE '%,' || ic.insurance_co_id || ',%'  "
            + "OR p.ip_allowed_insurance_co_ids LIKE '%,' || ic.insurance_co_id WHERE ic.status='A';";
 
    private static final String OP_ALLOWED_INSURANCE_COMPANIES_QUERY =
        "SELECT ic.insurance_co_id, ic.insurance_co_name, ic.status, ic.insurance_rules_doc_name"
            + " FROM insurance_company_master ic "
            + "JOIN (select * from patient_category_master where category_id=?) p "
            + "ON p.op_allowed_insurance_co_ids='*' "
            + "OR p.op_allowed_insurance_co_ids LIKE ic.insurance_co_id "
            + "OR p.op_allowed_insurance_co_ids LIKE ic.insurance_co_id || ',%' "
            + "OR p.op_allowed_insurance_co_ids LIKE '%,' || ic.insurance_co_id || ',%'  "
            + "OR p.op_allowed_insurance_co_ids LIKE '%,' || ic.insurance_co_id WHERE ic.status='A';";
  
	public static List<BasicDynaBean> getAllowedInsCompanies(int categoryId, String visitType) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			if(visitType.equals("i"))
				ps = con.prepareStatement(IP_ALLOWED_INSURANCE_COMPANIES_QUERY);
			else
				ps = con.prepareStatement(OP_ALLOWED_INSURANCE_COMPANIES_QUERY);
			ps.setInt(1, categoryId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

    private static final String IP_ALLOWED_RATE_PLANS_QUERY =
        "SELECT  o.org_id, o.org_name " + "FROM organization_details o  "
            + "JOIN (select * from patient_category_master where category_id=?) p  "
            + "ON (p.ip_allowed_rate_plans='*') OR p.ip_allowed_rate_plans LIKE o.org_id  "
            + "OR p.ip_allowed_rate_plans LIKE o.org_id || ',%'  "
            + "OR p.ip_allowed_rate_plans LIKE '%,' || o.org_id || ',%' "
            + "OR p.ip_allowed_rate_plans LIKE '%,' || o.org_id  "
            + "WHERE o.status = 'A' AND ( (o.has_date_validity "
            + "AND current_date BETWEEN o.valid_from_date AND o.valid_to_date ) "
            + "OR (NOT o.has_date_validity));";
    
    private static final String OP_ALLOWED_RATE_PLANS_QUERY =
        "SELECT  o.org_id, o.org_name " + "FROM organization_details o  "
            + "JOIN (select * from patient_category_master where category_id=?) p  "
            + "ON (p.op_allowed_rate_plans='*') OR p.op_allowed_rate_plans LIKE o.org_id  "
            + "OR p.op_allowed_rate_plans LIKE o.org_id || ',%'  "
            + "OR p.op_allowed_rate_plans LIKE '%,' || o.org_id || ',%' "
            + "OR p.op_allowed_rate_plans LIKE '%,' || o.org_id  "
            + "WHERE o.status = 'A' AND ( (o.has_date_validity "
            + "AND current_date BETWEEN o.valid_from_date AND o.valid_to_date ) "
            + "OR (NOT o.has_date_validity));";

		public static List getAllowedRatePlans(int categoryId, String visitType) throws SQLException {
			Connection con = DataBaseUtil.getConnection();
			PreparedStatement ps = null;
			try {
				if(visitType.equals("i"))
					ps = con.prepareStatement(IP_ALLOWED_RATE_PLANS_QUERY);
				else
					ps = con.prepareStatement(OP_ALLOWED_RATE_PLANS_QUERY);
				ps.setInt(1, categoryId);
				return DataBaseUtil.queryToDynaList(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}


	private static final String IP_DEFAULT_SPONSOR =
		" SELECT tpa.tpa_id, tpa.tpa_name FROM tpa_master tpa " +
		"	WHERE tpa_id IN " +
		"		(SELECT primary_ip_sponsor_id " +
		"				FROM patient_category_master WHERE category_id=?) and status='A'";

	private static final String OP_DEFAULT_SPONSOR =
		" SELECT tpa.tpa_id, tpa.tpa_name FROM tpa_master tpa " +
		"	WHERE tpa_id IN " +
		"		(SELECT primary_op_sponsor_id " +
		"				FROM patient_category_master WHERE category_id=?) and status='A'";

	public static BasicDynaBean getDefaultSponsor(int categoryId, String visitType) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			if(visitType.equals("i"))
				ps = con.prepareStatement(IP_DEFAULT_SPONSOR);
			else
				ps = con.prepareStatement(OP_DEFAULT_SPONSOR);
			ps.setInt(1, categoryId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String IP_DEFAULT_COMPANY =
		" SELECT ic.insurance_co_id, ic.insurance_co_id FROM insurance_company_master ic " +
		"	WHERE insurance_co_id IN " +
		"		(SELECT primary_ip_insurance_co_id " +
		"				FROM patient_category_master WHERE category_id=?) and status='A'";

	private static final String OP_DEFAULT_COMPANY =
		" SELECT ic.insurance_co_id, ic.insurance_co_id FROM insurance_company_master ic " +
		"	WHERE insurance_co_id IN " +
		"		(SELECT primary_op_insurance_co_id " +
		"				FROM patient_category_master WHERE category_id=?) and status='A'";

	public static BasicDynaBean getDefaultInsCompany(int categoryId, String visitType) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			if(visitType.equals("i"))
				ps = con.prepareStatement(IP_DEFAULT_COMPANY);
			else
				ps = con.prepareStatement(OP_DEFAULT_COMPANY);
			ps.setInt(1, categoryId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String IP_DEFAULT_RATE_PLAN =
		" SELECT org.org_id, org.org_name FROM organization_details org " +
		"	WHERE org_id IN " +
		"		(SELECT ip_rate_plan_id " +
		"				FROM patient_category_master WHERE category_id=?) and status='A' ";

	private static final String OP_DEFAULT_RATE_PLAN =
		" SELECT org.org_id, org.org_name FROM organization_details org " +
		"	WHERE org_id IN " +
		"		(SELECT op_rate_plan_id " +
		"				FROM patient_category_master WHERE category_id=?) and status='A' ";

	public static BasicDynaBean getDefaultRatePlan(int categoryId, String visitType) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			if(visitType.equals("i"))
				ps = con.prepareStatement(IP_DEFAULT_RATE_PLAN);
			else
				ps = con.prepareStatement(OP_DEFAULT_RATE_PLAN);
			ps.setInt(1, categoryId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String SEARCH_FIELDS = " SELECT * ";
	private static final String SEARCH_COUNT = " SELECT count(*) ";

	private static final String SEARCH_TABLE =
			" FROM ( SELECT pc.category_id," +
			" pc.category_name, pc.status, " +
			" pc.ip_rate_plan_id,pc.op_rate_plan_id, " +
			" pc.primary_ip_sponsor_id, pc.primary_op_sponsor_id," +
			" pc.primary_ip_insurance_co_id, pc.primary_op_insurance_co_id," +
			" pc.ip_allowed_rate_plans,pc.op_allowed_rate_plans, " +
			" pc.ip_allowed_sponsors,pc.op_allowed_sponsors, " +
			" pc.ip_allowed_insurance_co_ids,pc.op_allowed_insurance_co_ids, " +
			" pc.registration_charge_applicable, pc.case_file_required, " +
			" itpa.tpa_name AS ip_tpa_name, otpa.tpa_name AS op_tpa_name, " +
			" iorg.org_name AS ip_org_name, oorg.org_name AS op_org_name," +
			" iicm.insurance_co_name AS ip_insurance_co_name, iocm.insurance_co_name AS op_insurance_co_name, " +
			" hcm.center_name, pc.center_id,pc.code,pc.seperate_num_seq " +
			" FROM patient_category_master pc " +
			" LEFT JOIN tpa_master itpa ON (pc.primary_ip_sponsor_id=itpa.tpa_id) " +
			" LEFT JOIN organization_details iorg ON (pc.ip_rate_plan_id = iorg.org_id)" +
			" LEFT JOIN insurance_company_master iicm ON (pc.primary_ip_insurance_co_id = iicm.insurance_co_id) " +
			" LEFT JOIN tpa_master otpa ON (pc.primary_op_sponsor_id=otpa.tpa_id) " +
			" LEFT JOIN organization_details oorg ON (pc.op_rate_plan_id = oorg.org_id)" +
			" LEFT JOIN insurance_company_master iocm ON (pc.primary_op_insurance_co_id = iocm.insurance_co_id) " +
			" JOIN hospital_center_master hcm ON (pc.center_id=hcm.center_id) " +
			" ) AS foo ";

	public PagedList search(Map requestParams, Map listing) throws SQLException, ParseException {

		Connection con = DataBaseUtil.getReadOnlyConnection();

		SearchQueryBuilder qb = new SearchQueryBuilder(con, SEARCH_FIELDS, SEARCH_COUNT, SEARCH_TABLE,
				listing);

		qb.addFilterFromParamMap(requestParams);
		qb.addSecondarySort("category_id");

		qb.build();
		PagedList l = qb.getMappedPagedList();
		qb.close();
		DataBaseUtil.closeConnections(con, null);
		return l;
	}
}
