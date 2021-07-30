package com.insta.hms.master.PlanMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AbstractCachingDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import net.sf.ehcache.Cache;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlanMasterDAO extends AbstractCachingDAO {

	public PlanMasterDAO() {
		super("insurance_plan_main");
	}

	private static String PLAN_DETAIL_FILTER_SELECT = "SELECT plan_id ";
	private static String PLAN_DETAIL_FILTER_TABLES = " FROM  insurance_plan_details ";

	private static String PLAN_MASTER_QUERY_COUNT = "SELECT COUNT(*) ";

	private static String PLAN_MASTER_QUERY_SELECT = "SELECT * ";

	private static String PLAN_MASTER_QUERY_TABLES = "FROM (SELECT plan_id,plan_name, plan_notes, plan_exclusions, "
			+ " icom.insurance_co_id, insurance_co_name, category_name, icam.category_id, ipm.username, ipm.mod_time, "
			+ " ipm.ip_applicable, ipm.op_applicable, overall_treatment_limit, "
			+ " ipm.default_rate_plan , ipm.status, ipm.is_copay_pc_on_post_discnt_amt,"
			+ " ipm.base_rate, ipm.gap_amount, ipm.marginal_percent,"
			+ " ipm.perdiem_copay_per, ipm.perdiem_copay_amount, "
			+ " ipm.op_visit_copay_limit, ipm.ip_visit_copay_limit, "
			+ " ipm.insurance_validity_start_date, ipm.insurance_validity_end_date, "
			+ " ipm.require_pbm_authorization ,iccm.center_id,iccm.status as insurance_category_center_status,"
			+ " op_plan_limit,op_episode_limit,op_visit_limit,"
			+ " ip_plan_limit,ip_visit_limit,ip_per_day_limit,"
			+ " op_visit_deductible ,ip_visit_deductible ,op_copay_percent,ip_copay_percent, limits_include_followup, sponsor_id, "
			+ " tpa_name, discount_plan_id, ipm.add_on_payment_factor FROM insurance_plan_main ipm "
			+ " LEFT JOIN insurance_company_master icom USING (insurance_co_id) "
			+ " LEFT JOIN insurance_category_master icam USING (category_id)"
			+ " LEFT JOIN insurance_category_center_master iccm ON (icam.category_id = iccm.category_id)"
			+ " LEFT JOIN tpa_master tm ON(tm.tpa_id = ipm.sponsor_id)) AS rec";

	private static String getStringFromParamObj(Object obj){
		if(obj == null || obj.equals("")) return null;
		else return ((String[])obj)[0]== null || ((String[])obj)[0].equals("")? null : ((String[])obj)[0];
	}

	public PagedList getRecords(Map map, Map listMap) throws SQLException,
			ParseException {
		Connection con = DataBaseUtil.getConnection();
		PagedList pagedList = new PagedList();
		ArrayList<Integer> planDetIdsList = new ArrayList<Integer>();
		int centerID = RequestContext.getCenterId();
		SearchQueryBuilder qb = null;
		List list = new ArrayList();

			qb = new SearchQueryBuilder(con,
				PLAN_MASTER_QUERY_SELECT, PLAN_MASTER_QUERY_COUNT,
				PLAN_MASTER_QUERY_TABLES, listMap);
		qb.addFilterFromString("string", "plan_name", "ilike", getStringFromParamObj(map.get("plan_name")), true);
		qb.addFilterFromString("string" , "insurance_co_id", "eq", getStringFromParamObj(map.get("insurance_co_id")), true);
		qb.addFilterFromString("integer", "category_name", "ilike", getStringFromParamObj(map.get("category_name")), true);
		qb.addFilterFromString("numeric", "overall_treatment_limit", getStringFromParamObj(map.get("overall_treatment_limit@op")), getStringFromParamObj(map.get("overall_treatment_limit")), true);
		qb.addFilterFromString("string", "default_rate_plan", "eq", getStringFromParamObj(map.get("default_rate_plan")), true);
		qb.addFilterFromString("string", "ip_applicable", "eq",  getStringFromParamObj(map.get("ip_applicable")), true);
		qb.addFilterFromString("string", "op_applicable", "eq", getStringFromParamObj(map.get("op_applicable")), true);
		qb.addFilterFromString("string", "status", "eq", getStringFromParamObj(map.get("status")), true);
		qb.addFilterFromString("string", "is_copay_pc_on_post_discnt_amt", "eq", getStringFromParamObj(map.get("is_copay_pc_on_post_discnt_amt")), true);
		qb.addFilterFromString("string", "require_pbm_authorization", "eq", getStringFromParamObj(map.get("require_pbm_authorization")), true);
		qb.addFilterFromString("string" , "sponsor_id", "eq", getStringFromParamObj(map.get("sponsor_id")), true);
		qb.addFilterFromString("integer", "discount_plan_id", "eq", getStringFromParamObj(map.get("discount_plan_id")), true);
		
		if(map.get("insurance_category_id")!= null && map.get("detailTypeAmt")!= null && getStringFromParamObj(map.get("detailTypeAmt"))!= null){
			SearchQueryBuilder subQb = new SearchQueryBuilder(con,
					 PLAN_DETAIL_FILTER_SELECT, PLAN_MASTER_QUERY_COUNT,
					 PLAN_DETAIL_FILTER_TABLES);
			subQb.addFilterFromString("numeric",getStringFromParamObj(map.get("detailType")),getStringFromParamObj(map.get("detailType@op")),getStringFromParamObj(map.get("detailTypeAmt")), true);
			if(map.get("insurance_category_id")!= null && getStringFromParamObj(map.get("insurance_category_id"))!=null )
			subQb.addFilterFromString("string","insurance_category_id","eq",getStringFromParamObj(map.get("insurance_category_id")), true);
			subQb.build();
			PreparedStatement psData = subQb.getDataStatement();
			ArrayList<String> planDetIdsStrList = DataBaseUtil.queryToArrayList1(psData);
			if(planDetIdsStrList!= null && !planDetIdsStrList.isEmpty()){
				for(String s : planDetIdsStrList){
					if(s!=null && !s.equals(""))
						planDetIdsList.add(Integer.parseInt(s));
				}

			}
			if(planDetIdsList.isEmpty())
				planDetIdsList.add(0);
			subQb.close();
		}
		if(centerID != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1){
			list.add(centerID);
			list.add(0);
			qb.addFilter(qb.INTEGER, "center_id", "IN", list);
			qb.addFilter(qb.STRING, "insurance_category_center_status", "=", "A");
		}
		qb.addFilter(qb.INTEGER, "plan_id", "in", planDetIdsList);
		qb.addSecondarySort("plan_id");
		qb.build();
		pagedList = qb.getMappedPagedList();
		qb.close();
		DataBaseUtil.closeConnections(con, null);
 		return pagedList;
	}

	private static final String PLAN_NAMES_AND_IDS = "SELECT plan_id, plan_name, status  FROM insurance_plan_main";

	public static List getPlanNamesAndIds() throws SQLException {
		return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil
				.queryToDynaList(PLAN_NAMES_AND_IDS));
	}

	public BasicDynaBean findPlan(String keycolumn, Object identifier)
	throws SQLException {
		return findPlan(keycolumn,identifier, "=");
	}

	public BasicDynaBean findPlan(String keycolumn, Object identifier, String operator)
			throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		StringBuilder query = new StringBuilder();
		keycolumn = DataBaseUtil.quoteIdent(keycolumn);
		query.append("SELECT * ").append(PLAN_MASTER_QUERY_TABLES).append(
				" WHERE ").append(keycolumn).append(" "+operator+" ?;");
		if(operator.equals("ilike")){
			identifier = "%"+identifier+"%";
		}
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(query.toString());
			ps.setObject(1, identifier);
			List list = DataBaseUtil.queryToDynaList(ps);
			if (list.size() > 0) {
				return (BasicDynaBean) list.get(0);
			} else {
				return null;
			}
		} finally {
			con.close();
			ps.close();
		}
	}


	private static final String VALID_PLAN_FOR_BILL = " SELECT pr.plan_id " +
			" FROM bill b  "+
			" LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id  "+
			" LEFT JOIN insurance_case ic ON pr.insurance_id= ic.insurance_id "+
			" LEFT JOIN insurance_plan_main ipm  ON ipm.plan_id= pr.plan_id "+
			" WHERE policy_validity_end>=current_date AND ipm.status='A' AND b.bill_no=? ";

	public BasicDynaBean findValidPlanForBill(String billNo) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(VALID_PLAN_FOR_BILL);
			ps.setString(1, billNo);
			List list = DataBaseUtil.queryToDynaList(ps);
			if (list.size() > 0) {
				return (BasicDynaBean) list.get(0);
			} else {
				return null;
			}
		} finally {
			con.close();
			ps.close();
		}
	}


	public static String GET_INSURANCE_CATEGORY_HEADER_NAMES =
		" SELECT "
		+ " 	insurance_category_name||'-ip-per_item_pat_amt' AS ip_per_item_amt, "
		+ " 	insurance_category_name||'-ip-per_categ_pat_amt' AS ip_per_cat_amt, "
		+ " 	insurance_category_name||'-ip-patient_percent' AS ip_pat_percent, "
		+ " 	insurance_category_name||'-ip-patient_amount_cap' AS ip_pat_cap, "
		+ " 	insurance_category_name||'-ip-per_treatment_limit' AS ip_pat_limit, "
		+ " 	insurance_category_name||'-ip-category_payable' AS ip_category_payable, "
		+ " 	insurance_category_name||'-op-per_item_pat_amt' AS op_per_item_amt, "
		+ " 	insurance_category_name||'-op-per_categ_pat_amt' AS op_per_cat_amt, "
		+ " 	insurance_category_name||'-op-patient_percent' AS op_pat_percent, "
		+ " 	insurance_category_name||'-op-patient_amount_cap' AS op_pat_cap, "
		+ " 	insurance_category_name||'-op-per_treatment_limit' AS op_pat_limit, "
		+ " 	insurance_category_name||'-op-category_payable' AS op_category_payable "
		+ " FROM item_insurance_categories "
		+ "	ORDER BY insurance_category_name ";

	public static List getInsuranceCategoryHeaderNames(){
		return DataBaseUtil.queryToOnlyArrayList(GET_INSURANCE_CATEGORY_HEADER_NAMES);
	}

	public static  String GET_INSURANCE_PLAN_DETAILS =
			"SELECT ipm.plan_id,plan_name,category_name,overall_treatment_limit,"
					+ "insurance_co_name as insurance_company,plan_notes,"
					+ "plan_exclusions,org_name AS default_rate_plan,ip_applicable,"
					+ "op_applicable,ipm.status,ipm.is_copay_pc_on_post_discnt_amt,"
					+ "ipm.base_rate,ipm.gap_amount,ipm.marginal_percent,ipm.add_on_payment_factor,"
					+ "ipm.perdiem_copay_per,ipm.perdiem_copay_amount,ipm.op_visit_copay_limit,"
					+ "ipm.ip_visit_copay_limit,ipm.require_pbm_authorization "
					+ "FROM insurance_plan_main ipm "
					+ "LEFT JOIN insurance_company_master icm ON ipm.insurance_co_id=icm.insurance_co_id "
					+ "LEFT JOIN insurance_category_master iic ON iic.category_id=ipm.category_id "
					+ "LEFT JOIN organization_details org ON ipm.default_rate_plan=org.org_id "
					+ "ORDER BY ipm.plan_id ";

	public static List getInsurancePlanForCsv() throws SQLException{
		List a = new ArrayList();
		try{
			a =  DataBaseUtil.queryToDynaList(GET_INSURANCE_PLAN_DETAILS);
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		return a;
	}

	public static  String GET_INSURANCE_PLAN_DETAILS_USING_PLAN_ID =
			"SELECT * FROM insurance_plan_details WHERE plan_id =";

	public static List getInsurancePlanDetailsForCsv(Integer planId) throws SQLException{
		List list = new ArrayList();
		try{
			list =  DataBaseUtil.queryToDynaList(GET_INSURANCE_PLAN_DETAILS_USING_PLAN_ID+planId);
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		return list;
	}
	
	public static String getQueryForCSV() throws SQLException {
		StringBuilder query = new StringBuilder();
		query.append( " SELECT plan_id, plan_name, category_name, overall_treatment_limit, insurance_co_name as insurance_company, " +
		" plan_notes, plan_exclusions, org_name AS default_rate_plan, ip_applicable, op_applicable, " +
		" ipm.status, ipm.is_copay_pc_on_post_discnt_amt, ipm.base_rate, ipm.gap_amount, ipm.marginal_percent, ipm.add_on_payment_factor," +
		" ipm.perdiem_copay_per, ipm.perdiem_copay_amount, " +
		" ipm.op_visit_copay_limit, ipm.ip_visit_copay_limit, "+
		" ipm.require_pbm_authorization " );
		List<BasicDynaBean> insuranceCategories = new GenericDAO("item_insurance_categories").listAll("insurance_category_name");

		for(BasicDynaBean b : insuranceCategories){
			query.append(", (SELECT patient_amount " +
					" FROM insurance_plan_details " +
					" WHERE plan_id = ipm.plan_id AND  patient_type='i' " +
					" AND insurance_category_id = "+b.get("insurance_category_id")+" ) AS \""+b.get("insurance_category_name")+"-ip-per_item_pat_amt\" ");

			query.append(", (SELECT patient_amount_per_category " +
					" FROM insurance_plan_details " +
					" WHERE plan_id = ipm.plan_id AND  patient_type='i' " +
					" AND insurance_category_id = "+b.get("insurance_category_id")+" ) AS \""+b.get("insurance_category_name")+"-ip-per_categ_pat_amt\" ");

			query.append(", (SELECT patient_percent " +
					" FROM insurance_plan_details " +
					" WHERE plan_id = ipm.plan_id AND  patient_type='i' " +
					" AND insurance_category_id = "+b.get("insurance_category_id")+" ) AS \""+b.get("insurance_category_name")+"-ip-patient_percent\" ");

			query.append(", (SELECT patient_amount_cap " +
					" FROM insurance_plan_details " +
					" WHERE plan_id = ipm.plan_id AND  patient_type='i' " +
					" AND insurance_category_id = "+b.get("insurance_category_id")+" ) AS \""+b.get("insurance_category_name")+"-ip-patient_amount_cap\" ");

			query.append(", (SELECT per_treatment_limit " +
					" FROM insurance_plan_details " +
					" WHERE plan_id = ipm.plan_id AND  patient_type='i' " +
					" AND insurance_category_id = "+b.get("insurance_category_id")+" ) AS \""+b.get("insurance_category_name")+"-ip-per_treatment_limit\" ");
			
			query.append(", (SELECT category_payable " +
					" FROM insurance_plan_details " +
					" WHERE plan_id = ipm.plan_id AND  patient_type='i' " +
					" AND insurance_category_id = "+b.get("insurance_category_id")+" ) AS \""+b.get("insurance_category_name")+"-ip-category_payable\" ");

			query.append(", (SELECT patient_amount " +
					" FROM insurance_plan_details " +
					" WHERE plan_id = ipm.plan_id AND  patient_type='o' " +
					" AND insurance_category_id = "+b.get("insurance_category_id")+" ) AS \""+b.get("insurance_category_name")+"-op-per_item_pat_amt\" ");

			query.append(", (SELECT patient_amount_per_category " +
					" FROM insurance_plan_details " +
					" WHERE plan_id = ipm.plan_id AND  patient_type='o' " +
					" AND insurance_category_id = "+b.get("insurance_category_id")+" ) AS \""+b.get("insurance_category_name")+"-op-per_categ_pat_amt\" ");

			query.append(", (SELECT patient_percent " +
					" FROM insurance_plan_details " +
					" WHERE plan_id = ipm.plan_id AND  patient_type='o' " +
					" AND insurance_category_id = "+b.get("insurance_category_id")+" ) AS \""+b.get("insurance_category_name")+"-op-patient_percent\" ");

			query.append(", (SELECT patient_amount_cap " +
					" FROM insurance_plan_details " +
					" WHERE plan_id = ipm.plan_id AND  patient_type='o' " +
					" AND insurance_category_id = "+b.get("insurance_category_id")+" ) AS \""+b.get("insurance_category_name")+"-op-patient_amount_cap\" ");

			query.append(", (SELECT per_treatment_limit " +
					" FROM insurance_plan_details " +
					" WHERE plan_id = ipm.plan_id AND  patient_type='o' " +
					" AND insurance_category_id = "+b.get("insurance_category_id")+" ) AS \""+b.get("insurance_category_name")+"-op-per_treatment_limit\" ");
			
			query.append(", (SELECT category_payable " +
					" FROM insurance_plan_details " +
					" WHERE plan_id = ipm.plan_id AND  patient_type='o' " +
					" AND insurance_category_id = "+b.get("insurance_category_id")+" ) AS \""+b.get("insurance_category_name")+"-op-category_payable\" ");

		}

		query.append(" FROM insurance_plan_main ipm");
		query.append(" LEFT JOIN insurance_category_master iic ON iic.category_id = ipm.category_id ");
		query.append(" LEFT JOIN insurance_company_master icm ON ipm.insurance_co_id = icm.insurance_co_id ");
		query.append(" LEFT JOIN organization_details org ON ipm.default_rate_plan=org.org_id ORDER BY plan_id");
		return query.toString();
	}
	private static final String GET_PLAN_DETAILS = "SELECT * FROM insurance_plan_main WHERE plan_id = ?";

	public static BasicDynaBean getPlanDetails(int planId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PLAN_DETAILS);
			ps.setInt(1, planId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_PLAN_COPAY_CATEGORY_DETAILS =
		" SELECT * FROM insurance_plan_details WHERE " +
		" plan_id = ? AND patient_type = ? ORDER BY insurance_category_id ";

	public List findPlanCopayDetails(int planId, String patientType) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PLAN_COPAY_CATEGORY_DETAILS);
			ps.setInt(1, planId);
			ps.setString(2, patientType);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean hasPlanVisitCopayLimit(int planId, String visitType) throws SQLException {
		Connection con = null;
		BasicDynaBean planBean = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			con.setAutoCommit(false);
			planBean = new PlanMasterDAO().findByKey(con,"plan_id", planId);
		}finally{
			DataBaseUtil.commitClose(con, true);
		}

		boolean hasPlanVisitCopayLimit = false;
		if (planBean != null) {
			if (visitType.equals("o")) {

				BigDecimal visitCoPayOP = planBean.get("op_visit_copay_limit") != null
					? (BigDecimal)planBean.get("op_visit_copay_limit") : BigDecimal.ZERO;

				hasPlanVisitCopayLimit = (visitCoPayOP.compareTo(BigDecimal.ZERO) != 0);

			}else if (visitType.equals("i")) {

				BigDecimal visitCoPayIP = planBean.get("ip_visit_copay_limit") != null
					? (BigDecimal)planBean.get("ip_visit_copay_limit") : BigDecimal.ZERO;

				hasPlanVisitCopayLimit = (visitCoPayIP.compareTo(BigDecimal.ZERO) != 0);
			}
		}
		return hasPlanVisitCopayLimit;
	}

	public boolean hasPlanVisitCopayLimit(Connection con, int planId, String visitType) throws SQLException {
    BasicDynaBean planBean = null;
    planBean = new PlanMasterDAO().findByKey(con,"plan_id", planId);
    
    boolean hasPlanVisitCopayLimit = false;
    if (planBean != null) {
      if (visitType.equals("o")) {

        BigDecimal visitCoPayOP = planBean.get("op_visit_copay_limit") != null
          ? (BigDecimal)planBean.get("op_visit_copay_limit") : BigDecimal.ZERO;

        hasPlanVisitCopayLimit = (visitCoPayOP.compareTo(BigDecimal.ZERO) != 0);

      }else if (visitType.equals("i")) {

        BigDecimal visitCoPayIP = planBean.get("ip_visit_copay_limit") != null
          ? (BigDecimal)planBean.get("ip_visit_copay_limit") : BigDecimal.ZERO;

        hasPlanVisitCopayLimit = (visitCoPayIP.compareTo(BigDecimal.ZERO) != 0);
      }
    }
    return hasPlanVisitCopayLimit;
  }
	
	private String plan_details =
		" SELECT ipm.*,ipd.*,iic.*,icm.insurance_co_id,icm.insurance_co_name,icm.insurance_co_address,icm.insurance_co_city,"+
		"  icm.insurance_co_state,icm.insurance_co_country,icm.insurance_co_phone,icm.insurance_co_email,"+
		"  icm.default_rate_plan,icm.insurance_co_code_obsolete" +
		" FROM insurance_plan_main ipm " +
		" JOIN insurance_plan_details ipd USING(plan_id) " +
		" LEFT JOIN item_insurance_categories iic ON (iic.insurance_category_id = ipd.insurance_category_id) " +
		" LEFT JOIN insurance_company_master icm ON ipm.insurance_co_id = icm.insurance_co_id ";
	private String plan_filter =
		" WHERE ipm.plan_id = ? AND ipd.patient_type = ? ";

	public List<BasicDynaBean> getInsuPlanDetails(int planId,String visitType)
	throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(plan_details.concat(plan_filter));
			ps.setInt(1, planId);
			ps.setString(2, visitType);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static String discount_plan_list =" select discount_plan_id,discount_plan_name from discount_plan_main where coalesce(validity_start,current_date)<=current_date "
									  +" and coalesce(validity_end,current_date) >= current_date and status='A' order by discount_plan_name ";
	public static List<BasicDynaBean> getDefaultDiscountPlanList()
			throws SQLException{
				Connection con = null;
				PreparedStatement ps = null;
				try{
					con = DataBaseUtil.getConnection();
					ps = con.prepareStatement(discount_plan_list);
					return DataBaseUtil.queryToDynaList(ps);
				}finally{
					DataBaseUtil.closeConnections(con, ps);
				}
			}

	@Override
	protected Cache newCache(String region) {
		return new Cache(region, 100, MemoryStoreEvictionPolicy.LRU, false, "/tmp", true, 0, 0, false, 0,
				null);
	}

  public List<BasicDynaBean> getCatIdBasedOnPlanIds(String query,
      List<String> itemIds, Set<Integer> planIds, String visitType) {
    Object[] planId = planIds.toArray();
    String[] placeHolderArr = new String[itemIds.size()];
    Arrays.fill(placeHolderArr, "?");

    List<Object> args = new ArrayList<Object>();
    args.add((int) planId[0]);
    args.add(visitType);
    args.addAll(itemIds);
    if (planId.length > 1) {
      args.add((int) planId[1]);
    } else {
      args.add(-1); // return default
    }
    args.add(visitType);
    args.addAll(itemIds);
    args.addAll(itemIds);
    String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
    String categoryQuery = query.replaceAll("#", placeHolders);
    
    return DatabaseHelper.queryToDynaList(categoryQuery, args.toArray());
  }
  
  private static final String CHECK_IS_GENERAL_CAT_EXISTS_FOR_REG_CHARGES = "SELECT * "+
      " FROM patient_insurance_plan_details ipd "+
      " WHERE ipd.visit_id = ? AND ipd.plan_id =? AND ipd.patient_type = ? AND insurance_category_id = -1 ";
      

  public Boolean checkIsGeneralCategoryExistsForRegCharges(Connection con, String visitId, int planId, String visitType) 
		  throws SQLException {
    // TODO Auto-generated method stub
	BasicDynaBean catBean  = null;
	PreparedStatement ps = null; 
	Boolean isGeneralCatExists = false;
	try{
		if(con == null){
			con = DataBaseUtil.getConnection();
		}
		ps = con.prepareStatement(CHECK_IS_GENERAL_CAT_EXISTS_FOR_REG_CHARGES);
		ps.setString(1, visitId);
		ps.setInt(2, planId);
		ps.setString(3, visitType);
		catBean =  DataBaseUtil.queryToDynaBean(ps);
		if(null != catBean && catBean.get("insurance_category_id") != null){
		      isGeneralCatExists = true;
		}
	}finally{
		if( null != ps){
			ps.close();
		}
	}  
    return isGeneralCatExists;
  }

  private static final String GET_CASE_RATE_DETAILS = "SELECT * FROM case_rate_main crm "
      + " WHERE crm.insurance_company_id = ? AND crm.network_type_id = ? AND crm.plan_id = ? "
      + " AND case_rate_number = ? AND status='A'";

  public List<BasicDynaBean> getCaseRateDetails(String searchInput, int planId, String insCompId, 
      Integer planTypeId, Integer caseRateNo) throws SQLException {
    
    Connection con = null;
    PreparedStatement ps = null;
    int psIndex = 1;
    String[] searchWord = searchInput.split(" ");
    
    StringBuilder query = new StringBuilder(GET_CASE_RATE_DETAILS);
    
    for (int index = 0; index < searchWord.length; index++) {
      query.append(" AND (code ILIKE ? OR code_description ILIKE ?) ");
    }
    
    try{
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(query.toString());
      ps.setString(psIndex++, insCompId);
      ps.setInt(psIndex++, planTypeId);
      ps.setInt(psIndex++, planId);
      ps.setInt(psIndex++, caseRateNo);
      
      for (int index = 0; index < searchWord.length; index++) {
        ps.setString(psIndex++, searchWord[index] + "%");
        ps.setString(psIndex++, "%" + searchWord[index] + "%");
      }
      
      return DataBaseUtil.queryToDynaList(ps);
    }finally{
      DataBaseUtil.closeConnections(con, ps);
    }    
  }
}
