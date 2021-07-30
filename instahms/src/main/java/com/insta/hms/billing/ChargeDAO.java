package com.insta.hms.billing;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.ServiceMaster.ServiceMasterDAO;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ChargeDAO {

	static Logger logger = LoggerFactory.getLogger(ChargeDAO.class);
	final GenericDAO billChargeTranasactionDao = new GenericDAO("bill_charge_transaction");
	final ServiceMasterDAO serviceMasterDao = new ServiceMasterDAO();
	final BillDAO billDao = new BillDAO();
	final GenericDAO mrdObsDao = new GenericDAO("mrd_observations");

	private Connection con = null;

	public ChargeDAO(Connection con) {
		// Always construct a DAO by passing in the connection.
		this.con = con;
	}

	public String getNextChargeId() throws SQLException {
		return AutoIncrementId.getSequenceId("chargeid_sequence", "CHARGEID");
	}

	/*
	 * This is the common charge query that is used by multiple getXxx methods
	 * after adding one or more WHERE clauses.
	 */
	private static final String CHARGE_QUERY =
		  " SELECT coalesce(bill_charge.insurance_claim_amount,0) as insurance_claim_amount, "
		+ "  bill_charge.charge_id, bill_charge.bill_no, bill_charge.charge_group, bill_charge.charge_head, act_department_id, COALESCE(act_description,'') as act_description, "
		+ "  act_remarks, act_rate, act_unit, act_quantity, bill_charge.amount, bill_charge.discount, bill_charge.discount_reason, is_system_discount, "
		+ "  charge_ref, paid_amount, bill_charge.posted_date, bill_charge.status, bill_charge.username, bill_charge.mod_time, bill_charge.approval_id, orig_rate, "
		+ "  package_unit, doctor_amount, doc_payment_id, ref_payment_id, oh_payment_id, act_description_id, "
		+ "  hasactivity, insurance_claim_amount, return_qty, bill_charge.return_insurance_claim_amt, return_amt, "
		+ "  payee_doctor_id, referal_amount, out_house_amount, "
		+ "  prescribing_dr_id, prescribing_dr_amount, prescribing_dr_payment_id, overall_discount_auth, "
		+ "  overall_discount_amt, discount_auth_dr, dr_discount_amt, discount_auth_pres_dr, pres_dr_discount_amt,"
		+ "  discount_auth_ref, ref_discount_amt, discount_auth_hosp, hosp_discount_amt, activity_conducted,"
		+ "  bill_charge.account_group, act_item_code, act_rate_plan_item_code, bill_charge.code_type, bill_charge.allow_discount, order_number,"
		+ "  bill_charge.charge_ref, chargegroup_name, chargehead_name, dept_name, prd.doctor_name as prescribing_dr_name, "
		+ "  dac.disc_auth_name AS discount_auth_dr_name, dap.disc_auth_name AS discount_auth_pres_dr_name,"
		+ "  dar.disc_auth_name AS discount_auth_ref_name, dah.disc_auth_name AS discount_auth_hosp_name,"
		+ "  daov.disc_auth_name AS overall_discount_auth_name, insurance_payable, "
		+ "  claim_service_tax_applicable,bill_charge.service_sub_group_id,ss.service_group_id,"
		+ "  ss.service_sub_group_name,sg.service_group_name, conducting_doc_mandatory, "
		+ "  CASE WHEN (bill_charge.charge_head = 'MARPKG') THEN 'N' "
		+ "  WHEN bill.dyna_package_id != 0  AND bill_charge.charge_head in('INVITE','INVRET') AND (coalesce(bill_charge.amount_included,0) = 0) "
		+ "  AND (coalesce(bill_charge.qty_included,0) = 0) AND bill_charge.dyna_package_excluded IS NOT NULL THEN bill_charge.dyna_package_excluded "
		+ "  	  WHEN (bill.dyna_package_id = 0 OR (coalesce(bill_charge.qty_included,0) = coalesce(act_quantity,0))) THEN 'N' "
		+ "       WHEN (coalesce(bill_charge.amount_included,0) = 0) AND (coalesce(bill_charge.qty_included,0) = 0 AND (coalesce(amount,0) = 0 )) THEN 'Y' "
		+ "		  WHEN ((coalesce(bill_charge.amount_included,0) = coalesce(amount+bill_charge.tax_amt,0)) AND (coalesce(bill_charge.qty_included,0) = 0)) THEN 'N' "
		+ "  	  WHEN (coalesce(bill_charge.amount_included,0) = 0) AND (coalesce(bill_charge.qty_included,0) = 0) THEN 'Y' ELSE 'P' END AS charge_excluded, "
		+ "  bill_charge.amount_included, qty_included, package_finalized, "
		+ "  bill_charge.consultation_type_id, consultation_type, user_remarks, "
		+ "  bill_charge.insurance_category_id,bill_charge.prior_auth_id, bill_charge.prior_auth_mode_id, bill_charge.first_of_category, "
		+ "  op_id, bill_charge.from_date, bill_charge.to_date, tdv.dept_name, item_remarks, "
		+ "  bill_charge.allow_rate_increase, bill_charge.allow_rate_decrease, "
		+ "  bill_charge.claim_status, bill_charge.claim_recd_total, bill_charge.redeemed_points, "
		+ "  ss.eligible_to_redeem_points, ss.redemption_cap_percent, service_charge_applicable,"
		+ "  bill_charge.orig_insurance_claim_amount , bill.visit_id, bill.visit_type, is_tpa, "
		+ "  pbccl.insurance_claim_amt as pri_claim_amt, sbccl.insurance_claim_amt as sec_claim_amt, "
		+ "	 pbccl.tax_amt as pri_tax_amt, sbccl.tax_amt as sec_tax_amt, "
		+ "	 pbccl.prior_auth_id as pri_prior_auth_id, sbccl.prior_auth_id as sec_prior_auth_id, "
		+ "  pbccl.prior_auth_mode_id as pri_prior_auth_mode, sbccl.prior_auth_mode_id as sec_prior_auth_mode, "
		+ "  pbcl.plan_id as pri_plan_id, sbcl.plan_id as sec_plan_id, is_claim_locked, pbccl.include_in_claim_calc as pri_include_in_claim, "
		+ "	 sbccl.include_in_claim_calc as sec_include_in_claim, bill_charge.tax_amt, "
		+ "  bill_charge.sponsor_tax_amt, bill_charge.return_tax_amt, bill_charge.original_tax_amt, bill_charge.package_id, "
		+ "  packages.bill_display_type, bill.bill_rate_plan_id, bill_charge.dyna_package_excluded, ssm.round_off as sale_bill_level_roundoff "
		+ "  FROM bill_charge  "
		+ "  JOIN bill ON (bill.bill_no = bill_charge.bill_no) "
		+ "  LEFT JOIN store_sales_main ssm ON(bill_charge.charge_id = ssm.charge_id) "
		+ "  LEFT JOIN bill_claim pbcl ON(bill_charge.bill_no = pbcl.bill_no and pbcl.priority = 1) "
		+ "  LEFT JOIN bill_claim sbcl ON(bill_charge.bill_no = sbcl.bill_no and sbcl.priority = 2) "
		+ "  LEFT JOIN bill_charge_claim pbccl ON(bill_charge.charge_id = pbccl.charge_id and pbcl.claim_id = pbccl.claim_id) "
		+ "  LEFT JOIN bill_charge_claim sbccl ON(bill_charge.charge_id = sbccl.charge_id and sbcl.claim_id = sbccl.claim_id) "		
		+ "  LEFT JOIN service_sub_groups ss using(service_sub_group_id) "
		+ "  LEFT JOIN service_groups sg using(service_group_id) "
		+ "  LEFT OUTER JOIN treating_departments_view tdv ON (bill_charge.act_department_id=tdv.dept_id) "
		+ "  JOIN chargehead_constants ON (bill_charge.charge_head = chargehead_constants.chargehead_id) "
		+ "  JOIN chargegroup_constants ON (bill_charge.charge_group = chargegroup_constants.chargegroup_id)"
		+ "  LEFT OUTER JOIN doctors prd ON (prd.doctor_id = bill_charge.prescribing_dr_id) "
		+ "  LEFT OUTER JOIN discount_authorizer dac ON (bill_charge.discount_auth_dr=dac.disc_auth_id)"
		+ "  LEFT OUTER JOIN discount_authorizer dap ON (bill_charge.discount_auth_pres_dr=dap.disc_auth_id)"
		+ "  LEFT OUTER JOIN discount_authorizer dar ON (bill_charge.discount_auth_ref = dar.disc_auth_id)"
		+ "  LEFT OUTER JOIN discount_authorizer dah ON (bill_charge.discount_auth_hosp = dah.disc_auth_id)"
		+ "  LEFT OUTER JOIN discount_authorizer daov ON (bill_charge.overall_discount_auth=daov.disc_auth_id)"
		+ "  LEFT OUTER JOIN consultation_types ct ON (bill_charge.consultation_type_id = ct.consultation_type_id) "
		+ "  LEFT JOIN packages ON (bill_charge.package_id = packages.package_id)" ;

	/*
	 * Returns a single charge DTO based on the given charge ID. If the charge
	 * ID is not there in the database, returns null
	 */
	public static final String GET_CHARGE = CHARGE_QUERY
			+ " WHERE bill_charge.charge_id=?";

	public ChargeDTO getCharge(String chargeId) throws SQLException {
	  ChargeDTO charge = null;
	  try(PreparedStatement stmt = con.prepareStatement(GET_CHARGE);){
  		stmt.setString(1, chargeId);
  		try(ResultSet rs = stmt.executeQuery();){
  		    if (rs.next()) { 		
  		      charge = new ChargeDTO();
  		      populateChargeDTO(charge, rs);
  		    }
  		}
	  }
		return charge;
	}

	private static final String GET_MAX_CHARGE_ID = "SELECT max(charge_id) as charge_id FROM bill_charge bc " +
			" JOIN bill b USING(bill_no) WHERE bill_no=? ";
	public static String getChargeId(Connection con, String billNo) throws SQLException {	
		try(PreparedStatement ps = con.prepareStatement(GET_MAX_CHARGE_ID);) {
			ps.setString(1, billNo);
			try(ResultSet rs = ps.executeQuery();){
			  if (rs.next())
			    return rs.getString("charge_id");
			}
		}
		return null;
	}

	/*
	 * Returns all charges applicable for the given bill number
	 */
	public static final String GET_BILL_CHARGES = CHARGE_QUERY
			+ " WHERE bill_charge.bill_no=? "
			+ " ORDER BY chargegroup_constants.display_order, chargehead_constants.display_order, "
			+ " bill_charge.posted_date, bill_charge.charge_id ";

	public List<ChargeDTO> getBillCharges(String billNo) throws SQLException {
		return getChargeList(GET_BILL_CHARGES, billNo);
	}



	public static final String GET_VISIT_CHARGES = CHARGE_QUERY
      + " WHERE bill.visit_id=? "
      + " ORDER BY chargegroup_constants.display_order, chargehead_constants.display_order, "
      + " bill_charge.posted_date ";
	public List<ChargeDTO> getVisitCharges(String visitId) throws SQLException{
    return getChargeList(GET_VISIT_CHARGES, visitId);
	}

	public List<BasicDynaBean> getBillChargesDynaList(String billNo) throws SQLException {
		try(PreparedStatement stmt = con.prepareStatement(GET_BILL_CHARGES)){
		  stmt.setString(1, billNo);
		  return DataBaseUtil.queryToDynaList(stmt);
		}
	}

	/*
	 * Returns all charges approved in the given approvalId
	 */
	public static final String GET_APPROVED_CHARGES = CHARGE_QUERY + " WHERE bill_charge.approval_id=?";

	public List getApprovedCharges(String approvalId) throws SQLException {
		return getChargeList(GET_APPROVED_CHARGES, approvalId);
	}

	/*
	 * Returns the min/max dates for the charges in the given list. minDate and
	 * maxDate are output parameters. If chargeIdList is null, then gets the
	 * min/max of the entire set of charges.
	 */
	private static final String GET_MIN_MAX_DATE = "SELECT min(posted_date)::TIMESTAMP WITHOUT TIME ZONE "
			+ ", max(posted_date) ::TIMESTAMP WITHOUT TIME ZONE "
			+ " FROM bill_charge ";

	public boolean getMinMaxPostedDate(List chargeIdList, Date minDate,
			Date maxDate) throws SQLException {

		StringBuilder where = new StringBuilder();
		//DataBaseUtil.addWhereFieldInList(where, "charge_id", chargeIdList);
		String[] placeHolderArr = new String[chargeIdList.size()];
    Arrays.fill(placeHolderArr, "?");
    String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
    where.append("WHERE charge_id in ( " + placeHolders  + ")");
		StringBuilder query = new StringBuilder(GET_MIN_MAX_DATE);
		query.append(where);

		try(PreparedStatement ps = con.prepareStatement(query.toString());){
  		int i = 1;
  		if (chargeIdList != null) {
  			Iterator it = chargeIdList.iterator();
  			while (it.hasNext()) {
  				ps.setString(i, (String) it.next());
  				i++;
  			}
  		}		
  		try(ResultSet rs = ps.executeQuery();) {
    		if (rs.next()) {
    			if (minDate != null) {
    				minDate.setTime(rs.getDate(1).getTime());
    			}
    			if (maxDate != null) {
    				maxDate.setTime(rs.getDate(2).getTime());
    			}
    			return true;
    		}
  		}
  		return false;
		}
	}

	private static final String CHARGE_EXT_QUERY_FIELDS = "SELECT * ";

	private static final String CHARGE_EXT_QUERY_COUNT = "SELECT count(charge_id) ";

	private static final String CHARGE_EXT_QUERY_TABLES =
		" FROM (SELECT bc.*, cgc.chargegroup_name, chc.chargehead_name, d.dept_name, "
		+ " b.bill_type, b.status as bill_status, b.visit_id,prc.customer_id, b.finalized_date, "
		+ " b.visit_type, pr.mr_no, pd.patient_name, pd.middle_name, pd.last_name,prc.customer_name, "
		+ " doc.doctor_name, pr.ward_name, dac.disc_auth_name AS discount_auth_dr_name, COALESCE(pd.patient_group,0) as patient_group,"
		+ " dap.disc_auth_name AS discount_auth_pres_dr_name, "
		+ " dar.disc_auth_name AS discount_auth_ref_name,"
		+ " dah.disc_auth_name AS discount_auth_hosp_name,"
		+ " daov.disc_auth_name AS overall_discount_auth_name, "
		+ " CASE WHEN pr.primary_sponsor_id IS NULL THEN 'N' WHEN pr.primary_sponsor_id = '' THEN 'N' ELSE 'Y' "
		+ " END AS insurance_payable "
		+ "FROM bill_charge bc "
		+ " LEFT OUTER JOIN treating_departments_view d on (bc.act_department_id = d.dept_id)"
		+ " JOIN chargehead_constants chc ON (bc.charge_head = chc.chargehead_id) "
		+ " JOIN chargegroup_constants cgc ON (bc.charge_group = cgc.chargegroup_id) "
		+ " JOIN bill b ON (bc.bill_no = b.bill_no) "
		+ " LEFT JOIN patient_registration pr ON (b.visit_id = pr.patient_id) "
		+ " LEFT OUTER JOIN doctors doc ON (pr.doctor = doc.doctor_id) "
		+ " LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no) "
		+ " LEFT JOIN store_retail_customers prc ON  (prc.customer_id=b.visit_id) "
		+ " LEFT OUTER JOIN discount_authorizer dac ON (bc.discount_auth_dr = dac.disc_auth_id)"
		+ " LEFT OUTER JOIN discount_authorizer dap ON (bc.discount_auth_pres_dr = dap.disc_auth_id)"
		+ " LEFT OUTER JOIN discount_authorizer dar ON (bc.discount_auth_ref = dar.disc_auth_id)"
		+ " LEFT OUTER JOIN discount_authorizer dah ON (bc.discount_auth_hosp = dah.disc_auth_id)"
		+ " LEFT OUTER JOIN discount_authorizer daov ON (bc.overall_discount_auth = daov.disc_auth_id)"
		+ " ) AS foo";

	private static final String WHERE_COND = " WHERE coalesce(approval_id,'')='' ";

	public static PagedList searchChargesExtended(Map filters, Map pagingParams)
		throws SQLException, FileNotFoundException, ParseException {
	  Connection con = DataBaseUtil.getConnection();
	  SearchQueryBuilder qb;
	  PagedList list ;
	  try {
	  qb = new SearchQueryBuilder(con, CHARGE_EXT_QUERY_FIELDS,
  				CHARGE_EXT_QUERY_COUNT, CHARGE_EXT_QUERY_TABLES, WHERE_COND, pagingParams);
  
  		qb.addFilterFromParamMap(filters);
  		qb.appendToQuery("patient_confidentiality_check(foo.patient_group,foo.mr_no)");
  		qb.build();
		 list =  qb.getMappedPagedList();
	  } finally {
	    DataBaseUtil.closeConnections(con, null);
	  }
		return list;
	}

	/*
	 * Query the masters for charge heads and charge groups
	 */
	private static final String GET_CHARGE_GROUPS = "SELECT "
			+ " chargegroup_id, chargegroup_name,ip_applicable,op_applicable,"
			+ " associated_module, display_order,dependent_module "
			+ " FROM chargegroup_constants  ";

	public List getChargeGroupConstNames() throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(GET_CHARGE_GROUPS
				+ " ORDER BY display_order ");){
		  return DataBaseUtil.queryToArrayList(ps);
		}
	}

	public List getChargeGroupConstNames(boolean ordereable)
			throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(GET_CHARGE_GROUPS
				+ " where ordereable= ? ORDER BY display_order");){
		  ps.setBoolean(1, ordereable);
		  return DataBaseUtil.queryToArrayList(ps);
		}
	}

	public static List getAllChargeGroups() throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_CHARGE_GROUPS);
	}

	private static final String GET_CHARGE_HEADS = "SELECT "
		+ " chargegroup_id, chargehead_id, chargehead_name,ip_applicable,op_applicable, "
		+ " associated_module, display_order, ordereable, dependent_module, insurance_payable, "
		+ " claim_service_tax_applicable, insurance_category_id, service_charge_applicable, "
    + " service_sub_group_id "
		+ " FROM chargehead_constants ORDER BY display_order ";

	private static final String GET_CHARGE_HEAD_NAMES = "SELECT "
		+ " distinct on(chargehead_name) chargehead_name, display_order "
		+ " FROM chargehead_constants ";

	public List getChargeHeadConstNames() throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(GET_CHARGE_HEADS);){
		  return DataBaseUtil.queryToArrayList(ps);
		}
	}

	public static List getAllChargeHeads() throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_CHARGE_HEAD_NAMES);
	}

	/*
	 * Update a charge, based on the charge_id in the DTO
	 */
	public static final String UPDATE_CHARGE = "UPDATE bill_charge SET "
		+ "charge_id=?, bill_no=?, charge_group=?, charge_head=?, "
		+ " act_department_id=?, act_description_id=?, act_description=?, act_remarks=?, act_rate=?, "
		+ " act_unit=?, act_quantity=?, amount=?, discount=?, discount_reason=?, charge_ref=?, "
		+ " act_item_code=?, act_rate_plan_item_code=?, code_type=?, posted_date=?, status=?, mod_time=?, "
		+ " username=? ,insurance_claim_amount = ?, conducting_doc_mandatory=?, tax_amt= ? "
		+ "WHERE charge_id=?";

	public void updateCharge(String chargeId, ChargeDTO charge)
			throws SQLException {

  		try(PreparedStatement stmt = con.prepareStatement(UPDATE_CHARGE);){
    		int i = 1;
    		stmt.setString(i++, charge.getChargeId());
    		stmt.setString(i++, charge.getBillNo());
    		stmt.setString(i++, charge.getChargeGroup());
    		stmt.setString(i++, charge.getChargeHead());
    
    		stmt.setString(i++, charge.getActDepartmentId());
    		stmt.setString(i++, charge.getActDescriptionId());
    		stmt.setString(i++, charge.getActDescription());
    		stmt.setString(i++, charge.getActRemarks());
    		stmt.setBigDecimal(i++, charge.getActRate());
    
    		stmt.setString(i++, charge.getActUnit());
    		stmt.setBigDecimal(i++, charge.getActQuantity());
    		stmt.setBigDecimal(i++, charge.getAmount());
    		stmt.setBigDecimal(i++, charge.getDiscount());
    		stmt.setString(i++, charge.getDiscountReason());
    
    		stmt.setString(i++, charge.getChargeRef());
    		stmt.setString(i++, charge.getActItemCode());
    		stmt.setString(i++, charge.getActRatePlanItemCode());
    		stmt.setString(i++, charge.getCodeType());
    
    		stmt.setTimestamp(i++, new Timestamp(charge.getPostedDate().getTime()));
    		stmt.setString(i++, charge.getStatus());
    		stmt.setTimestamp(i++, new Timestamp(charge.getModTime().getTime()));
    		stmt.setString(i++, charge.getUsername());
    		stmt.setBigDecimal(i++, charge.getInsuranceClaimAmount());
    		stmt.setString(i++, charge.getConducting_doc_mandatory());
    		stmt.setBigDecimal(i++, charge.getTaxAmt());

    
    		// primary key
    		stmt.setString(i++, chargeId);
    
    		int result = stmt.executeUpdate();
    		if (result == 0) {
    			logger.error("Error updating charge " + chargeId);
    		}
  		}	
	}

	public static final String UPDATE_ANAESTHESIA_CHARGES = "UPDATE bill_charge SET act_remarks = ?,act_rate=?,amount=?, discount=?, act_quantity = ?," +
			"	from_date =?, to_date=?, username = ?, mod_time=? WHERE charge_id = ?";

	public void updateAnaethesiaCharges(String chargeId, ChargeDTO charge) throws SQLException {
		try(PreparedStatement stmt = con.prepareStatement(UPDATE_ANAESTHESIA_CHARGES);){
  		String username = RequestContext.getUserName();
  		int i = 1;
  		stmt.setString(i++, charge.getActRemarks());
  		stmt.setBigDecimal(i++, charge.getActRate());
  		stmt.setBigDecimal(i++, charge.getAmount());
  		stmt.setBigDecimal(i++, charge.getDiscount());
  		stmt.setBigDecimal(i++, charge.getActQuantity());
  		stmt.setTimestamp(i++,charge.getFrom_date());
  		stmt.setTimestamp(i++,charge.getTo_date());
  		stmt.setString(i++, username);
  		stmt.setTimestamp(i++, DateUtil.getCurrentTimestamp());
  		stmt.setString(i++, chargeId);
  		int result = stmt.executeUpdate();
  		if (result == 0) {
  			logger.error("Error updating charge " + chargeId);
  		}
		}
	}

	private static final String UPDATE_BILL_MODIFIED = "UPDATE bill SET app_modified='Y' "
			+ " WHERE bill_no in ";

	private static final String SELECT_BILLS_SUBQUERY = "SELECT DISTINCT(bill_no) from bill_charge ";

	public boolean setBillModified(List modifiedChargeIdList)
			throws SQLException {
		StringBuilder query = new StringBuilder(UPDATE_BILL_MODIFIED);
		StringBuilder subQuery = new StringBuilder(SELECT_BILLS_SUBQUERY);
		StringBuilder where = new StringBuilder();
		//DataBaseUtil.addWhereFieldInList(where, "charge_id", modifiedChargeIdList);
		String[] placeHolderArr = new String[modifiedChargeIdList.size()];
    Arrays.fill(placeHolderArr, "?");
    String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
    where.append("WHERE charge_id in ( " + placeHolders  + ")");
		subQuery.append(where);

		query.append('(').append(subQuery).append(')');
		logger.debug("setBillModified query: " + query.toString());
		/*try(PreparedStatement stmt = con.prepareStatement(query.toString());){
  		Iterator it = modifiedChargeIdList.iterator();
  		int i = 1;
  		String chId;
  		while (it.hasNext()) {
  			chId = ((String) it.next()).trim();
  			stmt.setString(i++, chId);
  		}
  		int count = stmt.executeUpdate();
  		return (count >= 1);
		}*/
		try(Connection con = DataBaseUtil.getConnection();){
		  int count = DataBaseUtil.executeQuery(con, query.toString(), modifiedChargeIdList.toArray());
		  return (count >= 1);
		}
		
	}

	private static final String UPDATE_BILL_NO = "UPDATE bill_charge SET bill_no=? "
			+ " WHERE bill_no=?";

	public void updateBillNo(String origBillNo, String newBillNo)
			throws SQLException {
		try(PreparedStatement stmt = con.prepareStatement(UPDATE_BILL_NO);){
		    stmt.setString(1, newBillNo);
		    stmt.setString(2, origBillNo);
		    stmt.execute();
	  }
	}

	/*
	 * Update a charge (amount fields only)
	 */
	public boolean updateChargeAmounts(ChargeDTO charge) throws SQLException {
		try(PreparedStatement stmt = con.prepareStatement(UPDATE_CHARGE_AMOUNTS);){
		  setUpdateChargeAmountParams(charge, stmt);
		  int result = stmt.executeUpdate();
		  return result != 0;
		}		
	}

	/*
	 * Update a list of charges (amount fields only)
	 */
	public boolean updateChargeAmountsList(List list) throws SQLException {	 
		try(PreparedStatement ps = con.prepareStatement(UPDATE_CHARGE_AMOUNTS);){  	
		  boolean success = true;
  		Iterator iterator = list.iterator();
  		while (iterator.hasNext()) {
  			ChargeDTO chargeDTO = (ChargeDTO) iterator.next();
  			setUpdateChargeAmountParams(chargeDTO, ps);
  			ps.addBatch();
  		}
  
  		int results[] = ps.executeBatch();
  		for (int p = 0; p < results.length; p++) {
  			if (results[p] <= 0) {
  				success = false;
  				logger.error("Error updating charge amount: " + p);
  				break;
  			}
  		}
  		return success;
		}
	}

	public boolean updateChargeRefsList(List list) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(UPDATE_CHARGE_REF);){
		  boolean success = true;
  		Iterator iterator = list.iterator();
  		while (iterator.hasNext()) {
  			success &= updateChargeRef((ChargeDTO) iterator.next());
  			if ( !success ) break;
  		}
  		return success;
		}	
	}

	private static final String UPDATE_SALE_CHARGE =
		" UPDATE bill_charge SET return_insurance_claim_amt = return_insurance_claim_amt + ?, " +
		" return_amt= return_amt + ?, return_qty= return_qty + ?, discount = discount- ? , "
		+ "overall_discount_amt = overall_discount_amt - ? ,amount = amount + ? WHERE charge_id = ?";

	public boolean updateSaleCharges(List<ChargeDTO> saleIdChargesToUpdate) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(UPDATE_SALE_CHARGE);){
  		Iterator iterator = saleIdChargesToUpdate.iterator();
  		while (iterator.hasNext()) {
  			ChargeDTO charge = (ChargeDTO)iterator.next();
  
  			int i = 1;
  
  			ps.setBigDecimal(i++, charge.getReturnInsuranceClaimAmt());
  			ps.setBigDecimal(i++, charge.getReturnAmt());
  			ps.setBigDecimal(i++, charge.getReturnQty());
  			ps.setBigDecimal(i++, charge.getDiscount());;
  			ps.setBigDecimal(i++, charge.getDiscount());
  			ps.setBigDecimal(i++, charge.getDiscount());
  
  			ps.setString(i++, charge.getChargeId());
  			int k = ps.executeUpdate();
  
  			if (k == 0)
  				return false;
  		}
		}
		return true;
	}
	
	
	 private static final String UPDATE_SALE_CHARGE_WITH_TAX =
	     " UPDATE bill_charge SET return_insurance_claim_amt = return_insurance_claim_amt + ?, " +
	     " return_amt= return_amt + ?, return_qty= return_qty + ?, discount = discount- ? , "
	     + "overall_discount_amt = overall_discount_amt - ? ,amount = amount + ?,"
	     + " return_tax_amt = return_tax_amt + ?,"
	     + " return_original_tax_amt = return_original_tax_amt + ?,dyna_package_excluded = NULL "
	     + " WHERE charge_id = ?";

	   public boolean updateSaleChargesWithTax(List<ChargeDTO> saleIdChargesToUpdate) throws SQLException {
	     try(PreparedStatement ps = con.prepareStatement(UPDATE_SALE_CHARGE_WITH_TAX);){
  	     Iterator iterator = saleIdChargesToUpdate.iterator();
  	     while (iterator.hasNext()) {
  	       ChargeDTO charge = (ChargeDTO)iterator.next();
  
  	       int i = 1;
  
  	       ps.setBigDecimal(i++, charge.getReturnInsuranceClaimAmt());
  	       ps.setBigDecimal(i++, charge.getReturnAmt());
  	       ps.setBigDecimal(i++, charge.getReturnQty());
  	       ps.setBigDecimal(i++, charge.getDiscount());
  	       ps.setBigDecimal(i++, charge.getDiscount());
  	       ps.setBigDecimal(i++, charge.getDiscount());
  	       ps.setBigDecimal(i++, charge.getReturnTaxAmt());
  	       ps.setBigDecimal(i++, charge.getReturnOriginalTaxAmt());
  	       
  	       ps.setString(i++, charge.getChargeId());
  	       int k = ps.executeUpdate();
  
  	       if (k == 0)
  	         return false;
  	     }
	     }
	     return true;
	   }

	   private static final String UPDATE_RETURN_AMOUNTS =
	       "UPDATE bill_charge SET"
	       + " return_amt=?, return_tax_amt=?, return_original_tax_amt=?,"
	       + " return_qty = ?, amount = ?, discount = ?"
	       + " WHERE charge_id = ?";
	   public boolean updateReturnAmounts(List<ChargeDTO> charges) throws SQLException{
       try(PreparedStatement ps = con.prepareStatement(UPDATE_RETURN_AMOUNTS);){
  	     for(ChargeDTO charge : charges){
  	       int i = 1;
  	       ps.setBigDecimal(i++, charge.getReturnAmt());
  	       ps.setBigDecimal(i++, charge.getReturnTaxAmt());
  	       ps.setBigDecimal(i++, charge.getReturnOriginalTaxAmt());
  	       ps.setBigDecimal(i++, charge.getReturnQty());
  	       ps.setBigDecimal(i++, charge.getAmount());
  	       ps.setBigDecimal(i++, charge.getDiscount());
  	       ps.setString(i, charge.getChargeId());
  	       int updatedRows = ps.executeUpdate();
  	       if(updatedRows == 0){
  	         return false;
  	       }
  	     }
       }
	     return true;
	   }


	private static final String RECALC_SALE_RETURN_AMTS =
		" UPDATE bill_charge bc SET " +
		"  return_amt = (SELECT sum(return_amt-return_tax_amt) FROM store_sales_details WHERE sale_id= " +
		"    (SELECT sale_id FROM store_sales_main WHERE charge_id=bc.charge_id)), " +
		"  return_qty = (SELECT sum(return_qty) FROM store_sales_details WHERE sale_id= " +
		"    (SELECT sale_id FROM store_sales_main WHERE charge_id=bc.charge_id)), " +
		"  insurance_claim_amount = (SELECT sum(insurance_claim_amt) " +
		"    FROM store_sales_details WHERE sale_id= " +
		"    (SELECT sale_id FROM store_sales_main WHERE charge_id=bc.charge_id)), " +
		"  return_tax_amt = (SELECT sum(return_tax_amt) " +
		"    FROM store_sales_details WHERE sale_id= " +
		"    (SELECT sale_id FROM store_sales_main WHERE charge_id=bc.charge_id)), " +
		"  return_original_tax_amt = (SELECT sum(return_original_tax_amt) " +
		"    FROM store_sales_details WHERE sale_id= " +
		"    (SELECT sale_id FROM store_sales_main WHERE charge_id=bc.charge_id)) " +
		" WHERE charge_id=?";

	/*
	 * Recalculate and update the return totals in the charge based on the corresponding
	 * sale in store_sales_details
	 */
	public void recalcSaleReturnAmounts(String chargeId) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(RECALC_SALE_RETURN_AMTS);){
		  ps.setString(1, chargeId);
		  ps.executeUpdate();
		}
	}

	private static final String UPDATE_SALE_CHARGE_RET_INS_AMT =
		" UPDATE bill_charge SET return_insurance_claim_amt = return_insurance_claim_amt + ? " +
		" WHERE charge_id = ?";

	public boolean updateSaleChargesReturnInsAmt(List<ChargeDTO> saleIdChargesToUpdate) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(UPDATE_SALE_CHARGE_RET_INS_AMT);){
  		if (ps == null) return false;
  
  		Iterator iterator = saleIdChargesToUpdate.iterator();
  		while (iterator.hasNext()) {
  			ChargeDTO charge = (ChargeDTO)iterator.next();
  
  			int i = 1;
  
  			ps.setBigDecimal(i++, charge.getReturnInsuranceClaimAmt());
  
  			ps.setString(i++, charge.getChargeId());
  			int k = ps.executeUpdate();
  
  			if (k == 0)
  				return false;
  		}
		}
		return true;
	}

	public static final String CANCEL_CHARGE = "UPDATE bill_charge " +
		" SET status='X', act_quantity=0, discount=0, amount=0, insurance_claim_amount=0, mod_time = NOW(), " +
		"  act_remarks = replace(act_remarks, 'Occupied', 'Cancelled')" +
		" WHERE charge_id=?";

	public static final String CANCEL_CHARGE_REFS = "UPDATE bill_charge " +
		" SET status='X', act_quantity=0, discount=0, amount=0, insurance_claim_amount=0, mod_time = NOW(), " +
		"  act_remarks = replace(act_remarks, 'Occupied', 'Cancelled')" +
		" WHERE charge_ref=?";

	/**
	 * This method will change the status of a charge to 'X' which means
	 * cancelled charge. If cancelRefs is true, it will also cancel charges whose charge_ref is
	 * this charge. This is mainly called when cancelling an order.
	 */
	public static boolean cancelCharge(Connection con, String chargeid, boolean cancelRefs)
			throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(CANCEL_CHARGE);){
  		ps.setString(1, chargeid);
  		int result = ps.executeUpdate();
  		if (result == 0) {
  			return false;
  		}	
		}
		if (cancelRefs) {
		  try(PreparedStatement ps = con.prepareStatement(CANCEL_CHARGE_REFS);){			
		    ps.setString(1, chargeid);
		    ps.executeUpdate();
		  }
		}
		return true;
	}

	public boolean cancelCharge(Connection con, String chargeid) throws SQLException {
		return cancelCharge(con, chargeid, false);
	}


	public static final String CANCEL_BILL_CHARGE_CLAIM = "UPDATE bill_charge_claim "+
		" SET insurance_claim_amt=0 WHERE charge_id=? ";

	public boolean cancelBillChargeClaim(Connection con, String chargeId)throws SQLException {
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(CANCEL_BILL_CHARGE_CLAIM);
			ps.setString(1, chargeId);
			int result = ps.executeUpdate();
			if(result == 0) return false;
		}finally{
			if(ps != null) ps.close();
		}
		return true;
	}

	public static final String GET_ASSOCIATED_CHARGES = "SELECT charge_id FROM bill_charge WHERE charge_ref = ?";

	public List<BasicDynaBean> getAssociatedCharges(Connection con, String chargeId)throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_ASSOCIATED_CHARGES);
			ps.setString(1, chargeId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			if(ps != null) ps.close();
		}
	}

	/*
	 * Approve a list of charges, the list contains charge IDs
	 */
	private static final String APPROVE_CHARGE = "UPDATE bill_charge SET approval_id=? "
			+ " WHERE charge_id=?";

	public boolean approveCharges(List chargeIdList, String approvalId)
			throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(APPROVE_CHARGE);){ 
		  boolean success = true;
  		Iterator iterator = chargeIdList.iterator();
  		while (iterator.hasNext()) {
  			ps.setString(1, approvalId);
  			ps.setString(2, (String) iterator.next());
  			ps.addBatch();
  		}
  
  		int[] results = ps.executeBatch();
  		for (int p = 0; p < results.length; p++) {
  			if (results[p] <= 0) {
  				success = false;
  				logger.error("Error approving charge amount: " + p);
  				break;
  			}
  		}
  		return success;
		}
	}

	/*
	 * Insert one charge
	 */
	public boolean insertCharge(ChargeDTO charge) throws SQLException {
	  
		try(PreparedStatement ps = con.prepareStatement(INSERT_CHARGE);){
  		BillingHelper billingHelper = new BillingHelper();
  		String revenueDepartmentId = billingHelper.getRevenueDepartmentFromCharge(con, charge);
  		charge.setRevenueDepartmentId(revenueDepartmentId);
  		billingHelper.setBillChargeBillingGroup(charge);
  		setInsertChargeParams(charge, ps);
  		int count = ps.executeUpdate();
  		return (count == 1);
		}		
	}

	// todo: move this to BAC dao
	private static final String INSERT_ACTIVITY_FOR_MLC = "INSERT INTO bill_activity_charge "
		+ " (charge_id, activity_id, activity_code)"
		+ " values(?,?,?);";

	public boolean insertActivity(String chargeId, String activityId,
			String chargeHead) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(INSERT_ACTIVITY_FOR_MLC);){
  		ps.setString(1, chargeId);
  		ps.setString(2, activityId);
  		ps.setString(3, chargeHead);
  		int count = ps.executeUpdate();
  		return (count == 1);
		}		
	}

	/*
	 * Insert a list of charges
	 */
	public boolean insertCharges(List list) throws SQLException, IOException {

		if (list.isEmpty())
			return true;
		
		try(PreparedStatement ps = con.prepareStatement(INSERT_CHARGE);){
		  boolean success = true;  
	    ArrayList activityList = new ArrayList();
  		Iterator iterator = list.iterator();
  		BillingHelper billingHelper = new BillingHelper();
  		String revenueDepartmentId = billingHelper.getRevenueDepartmentFromCharge(con, (ChargeDTO) list.get(0));
  		while (iterator.hasNext()) {
  			ChargeDTO chargeDTO = (ChargeDTO) iterator.next();
  			chargeDTO.setRevenueDepartmentId(revenueDepartmentId);
  			billingHelper.setBillChargeBillingGroup(chargeDTO);
  			setZeroClaimAmountConfig(chargeDTO);
  			setInsertChargeParams(chargeDTO, ps);
			if (chargeDTO.getActivityId() != 0 && chargeDTO.getActivityCode() != null
					&& (chargeDTO.getPackageId() == null || chargeDTO.getPackageId().equals("") 
					|| "PKGPKG".equals(chargeDTO.getChargeHead()) || "INVRET".equals(chargeDTO.getChargeHead()))) {
  				activityList.add(new BillActivityCharge(
  						chargeDTO.getChargeId(), chargeDTO.getActivityCode(), chargeDTO.getChargeHead(),
  						String.valueOf(chargeDTO.getActivityId()), chargeDTO.getActDescriptionId(),
  						chargeDTO.getPayeeDoctorId(), chargeDTO.getActivityConducted(),
  						chargeDTO.getConductedDateTime()!= null
  							?new java.sql.Timestamp(chargeDTO.getConductedDateTime().getTime())
  							:null));
  			}
  			ps.addBatch();
  		}
  		int[] results = ps.executeBatch();
  		for (int p = 0; p < results.length; p++) {
  			if (results[p] <= 0) {
  				success = false;
  				break;
  			}
  		}
  		
  		insertBillChargeTransaction(con, list, null);
		
  		if (success) {
  			// collect a list of activities from the charges and insert those also.
  			BillActivityChargeDAO acDao = new BillActivityChargeDAO(con);
  			acDao.insertBillActivityCharge(activityList);
  		}
  		return success;
		}
	}

	private static final String DELETE_CHARGE = "DELETE FROM bill_charge WHERE charge_id=?";

	/*
	 * Delete a charge
	 */
	public boolean deleteCharge(String chargeId) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(DELETE_CHARGE);){
		  ps.setString(1, chargeId);
		  int count = ps.executeUpdate();
		  return (count == 1);
		}
		
	}

	/*
	 * Delete a list of charges
	 */
	public boolean deleteCharges(List chargeIdList) throws SQLException {	 
		try(PreparedStatement ps = con.prepareStatement(DELETE_CHARGE);){
		  boolean success = true;
  		Iterator iterator = chargeIdList.iterator();
  		while (iterator.hasNext()) {
  			ps.setString(1, (String) iterator.next());
  			ps.addBatch();
  		}
  		int[] results = ps.executeBatch();
  		for (int p = 0; p < results.length; p++) {
  			if (results[p] <= 0) {
  				success = false;
  				break;
  			}
  		}
  		return success;
		}	
	}

	// get a charge list given a single query
	public List<ChargeDTO> getChargeList(PreparedStatement stmt) throws SQLException {
		try(ResultSet rs = stmt.executeQuery();){
		  ArrayList list = new ArrayList();
		  while (rs.next()) {
  			ChargeDTO charge = new ChargeDTO();
  			populateChargeDTO(charge, rs);
  			list.add(charge);
  		}
		  return list;
		}		
	}

	public List<ChargeDTO> getChargeList(String query, String value) throws SQLException {
		try(PreparedStatement stmt = con.prepareStatement(query);){
		  stmt.setString(1, value);
		  List list = getChargeList(stmt);
		  return list;
		}
	}

	public List getChargeList(String query, String value1, String value2) throws SQLException {
		try(PreparedStatement stmt = con.prepareStatement(query);){
  		stmt.setString(1, value1);
  		stmt.setString(2, value2);
  		List list = getChargeList(stmt);
  		return list;
		}	
	}


	private static final String GET_CHARGES_IN_DATE_RANGE= " SELECT charge_id from bill_charge bc " +
			" JOIN bill b using (bill_no) " +
			" WHERE b.visit_id = ? AND bc.posted_date >= ? AND bc.posted_date <= ? AND bc.status != 'X' ";

	public List<String> getChargesByPostedDateRange(String visitId,Timestamp fromDateTime,Timestamp toDateTime)
									throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(GET_CHARGES_IN_DATE_RANGE);){
  		ps.setString(1, visitId);
  		ps.setTimestamp(2, fromDateTime);
  		ps.setTimestamp(3, toDateTime);
  		List<String> list = DataBaseUtil.queryToStringList(ps);
  		if(list == null)
  			list = new ArrayList<String>();
  		return list;
		}
		
	}

	public static List getChargeDetailsBean(String billNo) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_BILL_CHARGES, billNo);
	}

	private void populateChargeDTO(ChargeDTO charge, ResultSet rs)
			throws SQLException {
		charge.setChargeId(rs.getString("charge_id"));
		charge.setBillNo(rs.getString("bill_no"));
		charge.setChargeGroup(rs.getString("charge_group"));
		charge.setChargeHead(rs.getString("charge_head"));
		charge.setActDepartmentId(rs.getString("act_department_id"));
		charge.setActDescriptionId(rs.getString("act_description_id"));
		charge.setActDescription(rs.getString("act_description"));
		charge.setActRemarks(rs.getString("act_remarks"));
		charge.setActRate(rs.getBigDecimal("act_rate"));
		charge.setActUnit(rs.getString("act_unit"));
		charge.setActQuantity(rs.getBigDecimal("act_quantity"));
		charge.setAmount(rs.getBigDecimal("amount"));
		charge.setDiscount(rs.getBigDecimal("discount"));
		charge.setTaxAmt(rs.getBigDecimal("tax_amt"));
		charge.setOriginalTaxAmt(rs.getBigDecimal("original_tax_amt"));
		charge.setSponsorTaxAmt(rs.getBigDecimal("sponsor_tax_amt"));
		charge.setReturnTaxAmt(rs.getBigDecimal("return_tax_amt"));
		charge.setDiscountReason(rs.getString("discount_reason"));
		charge.setIsSystemDiscount(rs.getString("is_system_discount"));
		charge.setChargeRef(rs.getString("charge_ref"));
		charge.setPostedDate(rs.getTimestamp("posted_date"));
		charge.setChargeRef(rs.getString("charge_ref"));
		charge.setModTime(rs.getTimestamp("mod_time"));
		charge.setStatus(rs.getString("status"));
		charge.setUsername(rs.getString("username"));
		charge.setApprovalId(rs.getString("approval_id"));
		charge.setActDepartmentName(rs.getString("dept_name"));
		charge.setChargeGroupName(rs.getString("chargegroup_name"));
		charge.setChargeHeadName(rs.getString("chargehead_name"));
		charge.setOriginalRate(rs.getBigDecimal("orig_rate"));
		charge.setPackageUnit(rs.getBigDecimal("package_unit"));
		charge.setDoctorAmount(rs.getBigDecimal("doctor_amount"));
		charge.setDocPaymentId(rs.getString("doc_payment_id"));
		charge.setHasActivity(rs.getBoolean("hasactivity"));
		charge.setActivityConducted(rs.getString("activity_conducted"));
		charge.setInsuranceClaimAmount(rs.getBigDecimal("insurance_claim_amount"));
		charge.setOrigInsuranceClaimAmount(rs.getBigDecimal("orig_insurance_claim_amount"));
		charge.setReturnQty(rs.getBigDecimal("return_qty"));
		charge.setReturnAmt(rs.getBigDecimal("return_amt"));
		charge.setReturnInsuranceClaimAmt(rs.getBigDecimal("return_insurance_claim_amt"));
		charge.setInsurancePayable(rs.getString("insurance_payable"));
		charge.setInsuranceClaimTaxable(rs.getString("claim_service_tax_applicable"));
		charge.setPayeeDoctorId(rs.getString("payee_doctor_id"));
		charge.setReferalAmount(rs.getBigDecimal("referal_amount"));
		charge.setBillDisplayType(rs.getString("bill_display_type"));

		charge.setDiscount_auth_dr(rs.getInt("discount_auth_dr"));
		charge.setDiscount_auth_dr_name(rs.getString("discount_auth_dr_name"));
		charge.setDr_discount_amt(rs.getBigDecimal("dr_discount_amt"));

		charge.setDiscount_auth_pres_dr(rs.getInt("discount_auth_pres_dr"));
		charge.setDiscount_auth_pres_dr_name(rs.getString("discount_auth_pres_dr_name"));
		charge.setPres_dr_discount_amt(rs.getBigDecimal("pres_dr_discount_amt"));

		charge.setDiscount_auth_hosp(rs.getInt("discount_auth_hosp"));
		charge.setDiscount_auth_hosp_name(rs.getString("discount_auth_hosp_name"));
		charge.setHosp_discount_amt(rs.getBigDecimal("hosp_discount_amt"));

		charge.setDiscount_auth_ref(rs.getInt("discount_auth_ref"));
		charge.setDiscount_auth_ref_name(rs.getString("discount_auth_ref_name"));
		charge.setRef_discount_amt(rs.getBigDecimal("ref_discount_amt"));

		charge.setOverall_discount_auth(rs.getInt("overall_discount_auth"));
		charge.setOverall_discount_auth_name(rs.getString("overall_discount_auth_name"));
		charge.setOverall_discount_amt(rs.getBigDecimal("overall_discount_amt"));

		charge.setActItemCode(rs.getString("act_item_code"));
		charge.setActRatePlanItemCode(rs.getString("act_rate_plan_item_code"));
		charge.setCodeType(rs.getString("code_type"));

		charge.setAllowDiscount(rs.getBoolean("allow_discount"));
		charge.setServiceSubGroupId(rs.getInt("service_sub_group_id"));
		charge.setServiceGroupId(rs.getInt("service_group_id"));
		charge.setServiceGroupName(rs.getString("service_group_name"));
		charge.setServiceSubGroupName(rs.getString("service_sub_group_name"));

		if (rs.getObject("order_number") != null)
			charge.setOrderNumber((Integer)rs.getObject("order_number"));

		charge.setConducting_doc_mandatory(rs.getString("conducting_doc_mandatory"));

		charge.setUserRemarks(rs.getString("user_remarks"));
		charge.setInsuranceCategoryId(rs.getInt("insurance_category_id"));
		charge.setFirstOfCategory(rs.getBoolean("first_of_category"));
		charge.setPreAuthId(rs.getString("prior_auth_id"));
		charge.setPreAuthModeId(rs.getInt("prior_auth_mode_id"));
		charge.setOp_id(rs.getString("op_id"));
		charge.setTo_date(rs.getTimestamp("to_date"));
		charge.setFrom_date(rs.getTimestamp("from_date"));
		charge.setConsultation_type_id(rs.getInt("consultation_type_id"));
		charge.setItemRemarks(rs.getString("item_remarks"));
		charge.setAllowRateIncrease(rs.getBoolean("allow_rate_increase"));
		charge.setAllowRateDecrease(rs.getBoolean("allow_rate_decrease"));
		charge.setInsuranceBill(rs.getBoolean("is_tpa"));
		charge.setClaimRecdAmount(rs.getBigDecimal("claim_recd_total"));
		charge.setClaimStatus(rs.getString("claim_status"));
		charge.setRedeemed_points(rs.getInt("redeemed_points"));
		charge.setEligible_to_redeem_points(rs.getString("eligible_to_redeem_points"));
		if (rs.getObject("redemption_cap_percent") != null)
			charge.setRedemption_cap_percent(rs.getBigDecimal("redemption_cap_percent"));

		charge.setChargeExcluded(rs.getString("charge_excluded"));
		charge.setAmount_included(rs.getBigDecimal("amount_included"));
		charge.setQty_included(rs.getBigDecimal("qty_included"));
		charge.setPackageFinalized(rs.getString("package_finalized"));

		
		charge.setServiceChrgApplicable(rs.getString("service_charge_applicable"));
		int noOfPlans = 0;
		if(rs.getInt("pri_plan_id") != 0) noOfPlans++;
		if(rs.getInt("sec_plan_id") != 0) noOfPlans++;

		BigDecimal[] claimAmounts = new BigDecimal[noOfPlans];
		BigDecimal[] sponsorTaxAmounts = new BigDecimal[noOfPlans];
		String[] preAuthIds = new String[noOfPlans];
		Integer[] preAuthModeIds = new Integer[noOfPlans];
		String[] includeInClaimCalc = new String[noOfPlans];

		if(noOfPlans > 0){
			claimAmounts[0] = rs.getBigDecimal("pri_claim_amt");
			sponsorTaxAmounts[0] = rs.getBigDecimal("pri_tax_amt");
			preAuthIds[0] = rs.getString("pri_prior_auth_id");
			preAuthModeIds[0] = rs.getInt("pri_prior_auth_mode");
			if(rs.getBoolean("pri_include_in_claim"))
				includeInClaimCalc[0] = "Y";
			else
				includeInClaimCalc[0] = "N";
			if(noOfPlans == 2){
				claimAmounts[1] = rs.getBigDecimal("sec_claim_amt");
				sponsorTaxAmounts[1] = rs.getBigDecimal("sec_tax_amt");
				preAuthIds[1] = rs.getString("sec_prior_auth_id");
				preAuthModeIds[1] = rs.getInt("sec_prior_auth_mode");
				if(rs.getBoolean("sec_include_in_claim"))
					includeInClaimCalc[1] = "Y";
				else
					includeInClaimCalc[1] = "N";
			}
		}

		if(noOfPlans <= 0){
			claimAmounts = new BigDecimal[1];
			sponsorTaxAmounts = new BigDecimal[1];
			preAuthIds = new String[1];
			preAuthModeIds = new Integer[1];
			includeInClaimCalc = new String[1];
			claimAmounts[0] = rs.getBigDecimal("insurance_claim_amount");
			sponsorTaxAmounts[0] = rs.getBigDecimal("sponsor_tax_amt");
			preAuthIds[0] = (String)rs.getString("prior_auth_id");
			preAuthModeIds[0] = (Integer)rs.getInt("prior_auth_mode_id");
			includeInClaimCalc[0] = "Y";
		}

		charge.setClaimAmounts(claimAmounts);
		charge.setSponsorTaxAmounts(sponsorTaxAmounts);
		charge.setIncludeInClaimCalc(includeInClaimCalc);
		charge.setPreAuthIds(preAuthIds);
		charge.setPreAuthModeIds(preAuthModeIds);

		charge.setIsClaimLocked(rs.getBoolean("is_claim_locked"));
		if (rs.getInt("package_id") > 0) {
		  charge.setPackageId(rs.getInt("package_id"));
		}
		
		if(rs.getString("visit_type") != null && !(rs.getString("visit_type")).equals(""))
			charge.setVisitType(rs.getString("visit_type"));
		
    if (!charge.getChargeHead().equals("PHCMED") && !charge.getChargeHead().equals("PHCRET")
        && !charge.getStatus().equals("X")) {
      List<BasicDynaBean> billChargeTaxes = new BillChargeTaxDAO().getItemSubgroupCodes(con,
          charge.getChargeId());
      charge.setBillChargeTaxes(billChargeTaxes);
    }		
	}


	private static final String GET_BILLCHARGE_CLAIM_DETAILS= " SELECT bcc.* "+
		" FROM bill_claim bc "+
		" JOIN bill_charge_claim bcc ON(bcc.bill_no = bc.bill_no and bcc.claim_id = bc.claim_id) "+
		" WHERE charge_id = ? and priority = ?";

	private BasicDynaBean getBillChargeClaimDetails(String chargeId, int priority)throws SQLException {
		try(Connection con = DataBaseUtil.getReadOnlyConnection();
		    PreparedStatement ps = con.prepareStatement(GET_BILLCHARGE_CLAIM_DETAILS);){
			ps.setString(1, chargeId);
			ps.setInt(2, priority);
			return DataBaseUtil.queryToDynaBean(ps);
		}
	}

	private void populateChargeDTOExtended(ChargeDTO charge, ResultSet rs)
			throws SQLException {
		populateChargeDTO(charge, rs);
		charge.setBillType(rs.getString("bill_type"));
		charge.setBillStatus(rs.getString("bill_status"));
		charge.setBillFinalizedDate(rs.getDate("finalized_date"));
		charge.setVisitId(rs.getString("visit_id"));
		charge.setVisitType(rs.getString("visit_type"));
		charge.setPatientName(rs.getString("patient_name"));
		charge.setPatientLastName(rs.getString("last_name"));
		charge.setMrNo(rs.getString("mr_no"));
		charge.setWardName(rs.getString("ward_name"));
		charge.setDoctorName(rs.getString("doctor_name"));
		charge.setCustomerName(rs.getString("customer_name"));
		charge.setFirstOfCategory(rs.getBoolean("first_of_category"));
	}

	private static final String INSERT_CHARGE = "INSERT INTO bill_charge "
		+ "(charge_id, bill_no, charge_group, charge_head, act_department_id, act_description_id, "
		+ " act_description, act_remarks, act_rate, act_unit, act_quantity, amount, discount, "
		+ " discount_reason, charge_ref, posted_date, status, username, mod_time, "
		+ " approval_id, orig_rate, package_unit, doctor_amount, hasactivity, insurance_claim_amount, "
		+ "	payee_doctor_id, referal_amount,prescribing_dr_amount,prescribing_dr_id,"
		+ " discount_auth_dr,dr_discount_amt,discount_auth_pres_dr,pres_dr_discount_amt,"
		+ " discount_auth_ref,ref_discount_amt,discount_auth_hosp,hosp_discount_amt, "
		+ " overall_discount_auth, overall_discount_amt, account_group, "
		+ " act_item_code, act_rate_plan_item_code, order_number, allow_discount, "
		+ " activity_conducted, conducted_datetime, service_sub_group_id, code_type, "
		+ " conducting_doc_mandatory, consultation_type_id, user_remarks, "
		+ " insurance_category_id,prior_auth_id,prior_auth_mode_id, first_of_category, op_id, "
		+ " from_date, to_date, item_remarks,allow_rate_increase,allow_rate_decrease,redeemed_points,"
		+ " amount_included, qty_included, package_finalized,surgery_anesthesia_details_id, is_claim_locked, "
		+ " tax_amt, allow_zero_claim, billing_group_id, revenue_department_id, is_system_discount,"
		+ " package_id,panel_id,submission_batch_type) "
		+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
		+ "  ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private void setInsertChargeParams(ChargeDTO charge, PreparedStatement ps)
			throws SQLException {
		int i = 1;
		ps.setString(i++, charge.getChargeId());
		ps.setString(i++, charge.getBillNo());
		ps.setString(i++, charge.getChargeGroup());
		ps.setString(i++, charge.getChargeHead());
		ps.setString(i++, charge.getActDepartmentId());
		ps.setString(i++, charge.getActDescriptionId());
		ps.setString(i++, charge.getActDescription());
		ps.setString(i++, charge.getActRemarks());
		ps.setBigDecimal(i++, charge.getActRate());
		ps.setString(i++, charge.getActUnit());
		ps.setBigDecimal(i++, charge.getActQuantity());
		ps.setBigDecimal(i++, charge.getAmount());
		ps.setBigDecimal(i++, charge.getDiscount());
		ps.setString(i++, charge.getDiscountReason());
		ps.setString(i++, charge.getChargeRef());
		ps.setTimestamp(i++, new Timestamp(charge.getPostedDate().getTime()));
		ps.setString(i++, charge.getStatus());
		ps.setString(i++, charge.getUsername());
		ps.setTimestamp(i++, new Timestamp(charge.getModTime().getTime()));
		ps.setString(i++, charge.getApprovalId());
		ps.setBigDecimal(i++, charge.getOriginalRate());
		ps.setBigDecimal(i++, charge.getPackageUnit());
		ps.setBigDecimal(i++, charge.getDoctorAmount());
		ps.setBoolean(i++, charge.getHasActivity());

		if(null != charge.getInsuranceClaimAmount())
			ps.setBigDecimal(i++, charge.getInsuranceClaimAmount());
		else
			ps.setBigDecimal(i++, BigDecimal.ZERO);

		ps.setString(i++, charge.getPayeeDoctorId());
		ps.setBigDecimal(i++, charge.getReferalAmount());
		ps.setBigDecimal(i++, charge.getPrescribingDrAmount());
		ps.setString(i++, charge.getPrescribingDrId());
		ps.setInt(i++, charge.getDiscount_auth_dr());
		ps.setBigDecimal(i++, charge.getDr_discount_amt());
		ps.setInt(i++, charge.getDiscount_auth_pres_dr());
		ps.setBigDecimal(i++, charge.getPres_dr_discount_amt());
		ps.setInt(i++, charge.getDiscount_auth_ref());
		ps.setBigDecimal(i++, charge.getRef_discount_amt());
		ps.setInt(i++, charge.getDiscount_auth_hosp());
		ps.setBigDecimal(i++, charge.getHosp_discount_amt());
		ps.setInt(i++, charge.getOverall_discount_auth());
		ps.setBigDecimal(i++, charge.getOverall_discount_amt());
		ps.setInt(i++, charge.getAccount_group() > 0 ? charge.getAccount_group() : 1);
		ps.setString(i++, charge.getActItemCode());
		ps.setString(i++, charge.getActRatePlanItemCode());
		ps.setObject(i++, charge.getOrderNumber());
		ps.setBoolean(i++, charge.isAllowDiscount());
		ps.setString(i++, charge.getActivityConducted());
		if (charge.getConductedDateTime() != null)
			ps.setTimestamp(i++, new Timestamp(charge.getConductedDateTime().getTime()));
		else
			ps.setTimestamp(i++, null);
		ps.setInt(i++, charge.getServiceSubGroupId());
		ps.setString(i++, charge.getCodeType());
		ps.setString(i++, charge.getConducting_doc_mandatory());
		ps.setInt(i++, charge.getConsultation_type_id());
		ps.setString(i++, charge.getUserRemarks());
		ps.setInt(i++, charge.getInsuranceCategoryId());
		ps.setString(i++, charge.getPreAuthId());
		if(charge.getPreAuthModeId()== null)
			ps.setNull(i++, java.sql.Types.INTEGER);
		else
			ps.setInt(i++,charge.getPreAuthModeId());
		ps.setBoolean(i++, charge.getFirstOfCategory());
		ps.setString(i++, charge.getOp_id());
		ps.setTimestamp(i++, charge.getFrom_date());
		ps.setTimestamp(i++, charge.getTo_date());
		ps.setString(i++, charge.getItemRemarks());
		ps.setBoolean(i++, charge.isAllowRateIncrease());
		ps.setBoolean(i++, charge.isAllowRateDecrease());
		ps.setInt(i++, charge.getRedeemed_points());
		ps.setBigDecimal(i++, charge.getAmount_included() == null ? BigDecimal.ZERO : charge.getAmount_included());
		ps.setBigDecimal(i++, charge.getQty_included() == null ? BigDecimal.ZERO : charge.getQty_included());
		ps.setString(i++, charge.getPackageFinalized());
		ps.setObject(i++, charge.getSurgeryAnesthesiaDetailsId());
		ps.setBoolean(i++, charge.getIsClaimLocked() == null ? false : charge.getIsClaimLocked());
		ps.setBigDecimal(i++, charge.getTaxAmt() == null ? BigDecimal.ZERO : charge.getTaxAmt());
		ps.setBoolean(i++, charge.isAllowZeroClaim());
		if (charge.getBillingGroupId() != null) {
			ps.setInt(i++, charge.getBillingGroupId());
		} else {
			ps.setNull(i++, java.sql.Types.INTEGER);
		}
		ps.setString(i++, charge.getRevenueDepartmentId());
		ps.setString(i++, charge.getIsSystemDiscount());
		if (charge.getPackageId() != null) {
		  ps.setInt(i++, charge.getPackageId());
		} else {
		  ps.setNull(i++, java.sql.Types.INTEGER);
		}
		if (charge.getPanelId() != null) {
			  ps.setInt(i++, charge.getPanelId());
		} else {
			  ps.setNull(i++, java.sql.Types.INTEGER);
		}
		ps.setString(i++, charge.getSubmissionBatchType());
	}

	private static final String UPDATE_CHARGE_AMOUNTS = "UPDATE bill_charge SET "
			+ "  act_remarks=?, act_rate=?, act_quantity=?, act_item_code=?, act_rate_plan_item_code=?,"
			+ "  amount=?, discount=?, discount_reason=?,is_system_discount=?, "
			+ "  status=?, posted_date=?, mod_time=?, username=?,  "
			+ "  discount_auth_dr=?, dr_discount_amt=?, discount_auth_pres_dr=?,"
			+ "  pres_dr_discount_amt=?, discount_auth_ref=?, ref_discount_amt=?, discount_auth_hosp=?, "
			+ "  hosp_discount_amt=?, overall_discount_auth=?, overall_discount_amt=?, payee_doctor_id=?, "
			+ "  conducting_doc_mandatory=?, amount_included=?, qty_included=?, package_finalized = ?, "
			+ "	 user_remarks=?, insurance_category_id=?,"
			+ "  prior_auth_id = ?, prior_auth_mode_id=?,  act_unit = ?, first_of_category = ?,act_description = ?,"
			+ "  act_description_id = ? , item_remarks = ?, redeemed_points = ?, is_claim_locked = ?, code_type = ?, "
			+ "  dyna_package_excluded = ?, activity_conducted = ? "
			+ " WHERE charge_id=?";

	private void setUpdateChargeAmountParams(ChargeDTO charge,
			PreparedStatement ps) throws SQLException {
		int i = 1;
		ps.setString(i++, charge.getActRemarks());
		ps.setBigDecimal(i++, charge.getActRate());
		ps.setBigDecimal(i++, charge.getActQuantity());
		ps.setString(i++, charge.getActItemCode());
		ps.setString(i++, charge.getActRatePlanItemCode());

		ps.setBigDecimal(i++, charge.getAmount());
		ps.setBigDecimal(i++, charge.getDiscount());
		ps.setString(i++, charge.getDiscountReason());
		ps.setString(i++, charge.getIsSystemDiscount());
		
		ps.setString(i++, charge.getStatus());
		ps.setTimestamp(i++, new Timestamp(charge.getPostedDate().getTime()));
		ps.setTimestamp(i++, DateUtil.getCurrentTimestamp());
		String uName = charge.getUsername();
		ps.setString(i++, uName);

		ps.setInt(i++, charge.getDiscount_auth_dr());
		ps.setBigDecimal(i++, charge.getDr_discount_amt());
		ps.setInt(i++, charge.getDiscount_auth_pres_dr());
		ps.setBigDecimal(i++, charge.getPres_dr_discount_amt());
		ps.setInt(i++, charge.getDiscount_auth_ref());
		ps.setBigDecimal(i++, charge.getRef_discount_amt());
		ps.setInt(i++, charge.getDiscount_auth_hosp());
		ps.setBigDecimal(i++, charge.getHosp_discount_amt());
		ps.setInt(i++, charge.getOverall_discount_auth());
		ps.setBigDecimal(i++, charge.getOverall_discount_amt());
		ps.setString(i++, charge.getPayeeDoctorId());
		ps.setString(i++, charge.getConducting_doc_mandatory());
		ps.setBigDecimal(i++, charge.getAmount_included());
		ps.setBigDecimal(i++, charge.getQty_included());
		ps.setString(i++, charge.getPackageFinalized());
		ps.setString(i++, charge.getUserRemarks());
		ps.setInt(i++, charge.getInsuranceCategoryId());
		ps.setString(i++, charge.getPreAuthId());
		ps.setInt(i++, charge.getPreAuthModeId()== null? 1 : charge.getPreAuthModeId());
		ps.setString(i++, charge.getActUnit());
		boolean foc = charge.getFirstOfCategory();
		ps.setBoolean(i++, foc);
		ps.setString(i++, charge.getActDescription());
		ps.setString(i++, charge.getActDescriptionId());
		ps.setString(i++, charge.getItemRemarks());
		ps.setInt(i++, charge.getRedeemed_points());
		ps.setBoolean(i++, charge.getIsClaimLocked() == null ? false : charge.getIsClaimLocked());
		ps.setString(i++, charge.getCodeType());
		ps.setString(i++, charge.getDynaPackageExcluded());
		ps.setString(i++, charge.getActivityConducted() == null ? "Y" : charge.getActivityConducted());
		// primary key
		String cId = charge.getChargeId();
		ps.setString(i++, cId);

	}

	private static final String UPDATE_EXPORT_STATUS = "UPDATE BILL_APPROVALS SET "
			+ "  EXPORTED = 'Y' ";

	public boolean updateExportStatus(List approvalIds) throws SQLException {
		StringBuilder query = new StringBuilder(UPDATE_EXPORT_STATUS);
		StringBuilder where = new StringBuilder();
		//DataBaseUtil.addWhereFieldInList(where, "approval_id", approvalIds);
		String[] placeHolderArr = new String[approvalIds.size()];
    Arrays.fill(placeHolderArr, "?");
    String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
    where.append("WHERE approval_id in ( " + placeHolders  + ")");
		query.append(where);
		/*try(PreparedStatement ps = con.prepareStatement(query.toString());){
  		int i = 1;
  		Iterator itr = approvalIds.iterator();
  		while (itr.hasNext()) {
  			ps.setString(i++, (String) itr.next());
  		}
      int count = ps.executeUpdate();	
  		if (count == approvalIds.size()) {
  			return true;
  		} else {
  			return false;
  		}
		}*/
		
		int count = DataBaseUtil.executeQuery(con, query.toString(), approvalIds.toArray());
		if (count == approvalIds.size()) {
      return true;
    } else {
      return false;
    }
	}

	private static final String UPDATE_CHARGE_AMOUNT = "UPDATE bill_charge SET "
			+ " act_quantity=?, amount=?, mod_time=?, username=? ,act_remarks=? , doctor_amount=? "
			+ " ,act_rate =?, act_unit = ? ,act_description = ?, insurance_claim_amount=?"
			+ " ,discount = ? , overall_discount_auth = ?, payee_doctor_id = ? , activity_conducted = ? "
			+ " WHERE charge_id=?";

	public static boolean updateChargeAmount(Connection con, ChargeDTO cdto)
			throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(UPDATE_CHARGE_AMOUNT);){
  		ps.setBigDecimal(1, cdto.getActQuantity());
  		ps.setBigDecimal(2, cdto.getAmount());
  		ps.setTimestamp(3, new Timestamp(cdto.getModTime().getTime()));
  		ps.setString(4, cdto.getUsername());
  		ps.setString(5, cdto.getActRemarks());
  		ps.setBigDecimal(6, cdto.getDoctorAmount());
  		ps.setBigDecimal(7, cdto.getActRate());
  		ps.setString(8, cdto.getActUnit());
  		ps.setString(9, cdto.getActDescription());
  		ps.setBigDecimal(10, cdto.getInsuranceClaimAmount());
  		ps.setBigDecimal(11, cdto.getDiscount());
  		ps.setInt(12, cdto.getOverall_discount_auth());
  		ps.setString(13, cdto.getPayeeDoctorId());
  		ps.setString(14, cdto.getActivityConducted());
  		ps.setString(15, cdto.getChargeId());
  		int result = ps.executeUpdate();
  		return result != 0;
		}
	}


	/**
	 * update doctor paymentid for corresponding charge id. This indicates that the doctor payment
	 * has been confirmed and cannot be modified further.
	 */
	public static final String UPDATE_DOC_PAYMENT_DETAILS =
		" UPDATE bill_charge SET doctor_amount=?, doc_payment_id=? " +
		" WHERE charge_id=?   ";

	public static boolean updateDoctorPaymentDetails(Connection con, String chargeId,
			BigDecimal doctorAmount, String paymentId) throws SQLException {

		boolean success = false;
		try(PreparedStatement ps = con.prepareStatement(UPDATE_DOC_PAYMENT_DETAILS);){
  		ps.setBigDecimal(1, doctorAmount);
  		ps.setString(2, paymentId);
  		ps.setString(3, chargeId);
  
  		success = ps.executeUpdate() > 0;
  		return success;
		}
	}

	private static final String REMOVE_DOC_PAYMENT_ID =
		" UPDATE bill_charge SET doc_payment_id=null " +
		" WHERE charge_id=? ";

	public static void removeDoctorPaymentId(Connection con, String chargeId) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(REMOVE_DOC_PAYMENT_ID);){
		  ps.setString(1, chargeId);
		  ps.executeUpdate();
		}
	}

	public static final String UPDATE_DOCTOR_PAYMENT =
		"UPDATE bill_charge SET doctor_amount=? "
		+ " WHERE charge_id=? ";

	public static void updateDrChargeAmount(Connection con, String chargeId, BigDecimal docAmount)
		throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(UPDATE_DOCTOR_PAYMENT);){
  		ps.setBigDecimal(1, docAmount);
  		ps.setString(2, chargeId);
  		ps.executeUpdate();
		}
	}

	public static final String UPDATE_REFDOC_PAYMENT_DETAILS =
		" UPDATE bill_charge SET referal_amount=?, ref_payment_id=? " +
		" WHERE charge_id=? ";

	public boolean updateRefDocPaymentDetails(Connection con, String chargeId, BigDecimal amount,
			String paymentId) throws SQLException {

		boolean success = false;
		try(PreparedStatement ps = con.prepareStatement(UPDATE_REFDOC_PAYMENT_DETAILS);){
  		int i = 1;
  		ps.setBigDecimal(i++, amount);
  		ps.setString(i++, paymentId);
  		ps.setString(i++, chargeId);
  		success = ps.executeUpdate() > 0;
  		return success;
		}
	}

	private static final String REMOVE_REFDOC_PAYMENT_ID =
		" UPDATE bill_charge SET ref_payment_id=null " +
		" WHERE charge_id=? ";

	public static void removeRefDocPaymentId(Connection con, String chargeId) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(REMOVE_REFDOC_PAYMENT_ID);){
		  ps.setString(1, chargeId);
		  ps.executeUpdate();
		}
	}

	public static final String UPDATE_REFERRER_CHARGE =
		" UPDATE bill_charge set referal_amount=? " +
		" WHERE charge_id=? ";

	public static void updateRefChargeAmount(Connection con, String chargeId, BigDecimal docAmount)
		throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(UPDATE_REFERRER_CHARGE);){
  		ps.setBigDecimal(1, docAmount);
  		ps.setString(2, chargeId);
  		ps.executeUpdate();
		}
	}

	public static final String UPDATE_PRESC_PAYMENT_DETAILS =
		" UPDATE bill_charge SET prescribing_dr_amount=?, prescribing_dr_payment_id=? " +
		" WHERE charge_id=? ";

	public boolean updatePrescPaymentDetails(Connection con, String chargeId, BigDecimal amount,
			String paymentId) throws SQLException {

		boolean success = false;
		try(PreparedStatement ps = con.prepareStatement(UPDATE_PRESC_PAYMENT_DETAILS);){
  		int i = 1;
  		ps.setBigDecimal(i++, amount);
  		ps.setString(i++, paymentId);
  		ps.setString(i++, chargeId);
  		success = ps.executeUpdate() > 0;
  		return success;
	  }
	}

	private static final String REMOVE_PRESC_PAYMENT_ID =
		" UPDATE bill_charge SET prescribing_dr_payment_id=null " +
		" WHERE charge_id=? ";

	public static void removePrescPaymentId(Connection con, String chargeId) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(REMOVE_PRESC_PAYMENT_ID);){
		  ps.setString(1, chargeId);
		  ps.executeUpdate();
		}
	}

	public static final String UPDATE_PRESCRBING_DR_AMOUNT =
		" UPDATE bill_charge SET prescribing_dr_amount=? " +
		" WHERE charge_id=? ";

	public static void updatePresChargeAmount(Connection con, String chargeId, BigDecimal amount)
		throws SQLException {

		try(PreparedStatement ps = con.prepareStatement(UPDATE_PRESCRBING_DR_AMOUNT);){
  		ps.setBigDecimal(1, amount);
  		ps.setString(2, chargeId);
  		ps.executeUpdate();
	  }
	}

	public static final String UPDATE_OH_PAYMENT_DETAILS =
		" UPDATE bill_charge SET out_house_amount=?, oh_payment_id=? " +
		" WHERE charge_id=? ";

	public void updateOhPaymentDetails(Connection con, String chargeId, BigDecimal amount,
			String paymentId) throws SQLException {

		try(PreparedStatement ps = con.prepareStatement(UPDATE_OH_PAYMENT_DETAILS);){
  		int i = 1;
  		ps.setBigDecimal(i++, amount);
  		ps.setString(i++, paymentId);
  		ps.setString(i++, chargeId);
  		ps.executeUpdate();
		}
	}

	private static final String REMOVE_OH_PAYMENT_ID =
		" UPDATE bill_charge SET oh_payment_id=null " +
		" WHERE charge_id=? ";

	public static void removeOhPaymentId(Connection con, String chargeId) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(REMOVE_OH_PAYMENT_ID);){
		  ps.setString(1, chargeId);
		  ps.executeUpdate();
		}
	}

	private static final String UPDATE_OUT_HOUSE_CHARGE =
		"UPDATE bill_charge set out_house_amount = ? "
		+ " where charge_id=? ";

	public static void updateOuthouseCharge(Connection con, String chargeId, BigDecimal ohAmount)
		throws SQLException {
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(UPDATE_OUT_HOUSE_CHARGE);;
			ps.setBigDecimal(1, ohAmount);
			ps.setString(2, chargeId);
			ps.executeUpdate();
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public static final String UPDATE_PAYOUT =
		" UPDATE bill_charge SET doctor_amount=?, prescribing_dr_amount=?, referal_amount=? " +
		" WHERE charge_id=? ";

	public static boolean updatePayout(Connection con, String chargeId,
			BigDecimal docAmount, BigDecimal prescribedDrAmount, BigDecimal referralAmount)
		throws SQLException {	  
		int count = 0;
		try(PreparedStatement ps = con.prepareStatement(UPDATE_PAYOUT)) {
			ps.setBigDecimal(1, docAmount);
			ps.setBigDecimal(2, prescribedDrAmount);
			ps.setBigDecimal(3, referralAmount);
			ps.setString(4, chargeId);
			count = ps.executeUpdate();
		 }
		if (count >= 1)
			return true;
		else
			return false;
	}

	/*
	 * Prepaid charges grouped: used in CFD: Financial Consolidated Dashboard
	 * (collections)
	 */
	public static final String WANTED_CHARGE_HEADS = "('OPDOC', 'LTDIA', 'RTDIA', 'PHMED')";

	public static final String PREPAID_CHARGES = "SELECT "
		+ "  charge_type, sum(activities) as activities, "
		+ "  count(DISTINCT b.bill_no) as bills, sum(amount) as amount"
		+ " FROM bill b JOIN LATERAL ("
		+ " SELECT " + 
		"    CASE WHEN charge_head = 'PHRET' THEN 'PHMED'    "
		+ "       WHEN charge_head = 'ROPDOC' THEN 'OPDOC'    "
		+ "       WHEN charge_head IN "+WANTED_CHARGE_HEADS+" THEN charge_head     "
		+ "   ELSE 'O' END as charge_type,   "
		+ "   sum(act_quantity) as activities, "
		+ "   sum(amount) as amount "
		+ " FROM bill_charge WHERE bill_no = b.bill_no and status != 'X' GROUP BY charge_head"
		+ ") AS bc ON TRUE " +
		"  LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id AND NOT is_tpa) " +
		"  LEFT JOIN store_retail_customers prc ON prc.customer_id = pr.patient_id " +
		"  LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = pr.patient_id " +
		 " WHERE b.bill_type != 'C' AND b.status = 'C' AND " +
		 "   date(b.finalized_date) BETWEEN ? AND ? " +
		 "   AND (?=0 OR b.account_group=?) AND (?=0 OR COALESCE(pr.center_id,prc.center_id,isr.center_id)=?)"+
		 " GROUP BY charge_type ";

	public static List getPrepaidChargesGrouped(java.sql.Date from, java.sql.Date to, int accountGroup, int centerId)
		throws SQLException {
		return DataBaseUtil.queryToDynaList(PREPAID_CHARGES, new Object[]{from, to, accountGroup, accountGroup, centerId, centerId});
	}

	/*
	 * Includes all bills, even open ones. If passing in finalized_date, the
	 * check will fail for bills without finalized dates, so it is an automatic
	 * filter. posted_date is mandatory, so that check cannot fail when using
	 * posted_date.
	 */
	private static final String CHARGES_CGROUP_DESC =
		" SELECT c_group, " +
		"   CASE " +
		"    WHEN b.is_tpa THEN 's' WHEN b.visit_type = 'i' THEN 'i' WHEN b.visit_type = 'o' AND pr.op_type = 'O' THEN 'r' WHEN b.visit_type = 'o' AND pr.op_type != 'O' THEN 'o' " +
		"   END AS v_type, " +
		"   descr," +
		"   count(bc.charge_count) AS count," +
		"  sum(bc.amount) AS amount" +
		" FROM bill b JOIN LATERAL (" +
		"    SELECT" +
		"      bill_no," +
		"      COALESCE(display_title, 'Others') AS c_group," +
		"      COUNT(charge_id) AS charge_count," +
		"      sum(amount + tax_amt) AS amount," +
		"      account_group," +
		"      bc.charge_head,"+ 
		"      date(posted_date) AS posted_date," +
		"      COALESCE(act_description, '') AS descr" +
		"    FROM" +
		"      bill_charge bc" +
		"      LEFT JOIN bill_dashboard_heads bdh ON (bdh.charge_head = bc.charge_head)" +
		"    WHERE" +
		"      status != 'X'" +
		"      AND bill_no = b.bill_no" +
		"    GROUP BY" +
		"      bill_no," +
		"      c_group," +
		"      account_group," +
		"      bc.charge_head,"+ 
		"      posted_date, " +
		"      descr) bc ON TRUE" +
		"   JOIN patient_registration pr ON (b.visit_id = pr.patient_id) " +
		"  LEFT JOIN store_retail_customers prc ON prc.customer_id = pr.patient_id " +
		"  LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = pr.patient_id " +
		" WHERE   b.status != 'X' AND " +
		"   ('#' = 'posted_date' OR b.status != 'A') AND (date(#) BETWEEN ? AND ?) " +
		"   AND (?=0 OR b.account_group=?) AND (? = 0 OR COALESCE(pr.center_id,prc.center_id,isr.center_id) = ? )" +
		" GROUP BY c_group, descr, v_type ";

	public static List<BasicDynaBean> getChargesGroupedByCGroupDescr(Date from, Date to, String dateField,
			int accountGroup, int centerId) throws SQLException {
		dateField = DataBaseUtil.quoteIdent(dateField);
		String query = CHARGES_CGROUP_DESC.replace("#", dateField);
		return DataBaseUtil.queryToDynaList(query,
				new Object[]{from, to, accountGroup, accountGroup, centerId, centerId});
	}

	public static final String SET_INSURANCE_AMOUNTS = "UPDATE bill_charge " +
		" SET insurance_claim_amount=?, return_insurance_claim_amt=?, first_of_category=?, " +
		" orig_insurance_claim_amount=? WHERE charge_id=? ";

	public boolean setInsuranceAmounts(List<ChargeDTO> updateClaimList)
			throws SQLException {
		if (updateClaimList.isEmpty()) {
			return true;
		} else {
			try(PreparedStatement ps = con.prepareStatement(SET_INSURANCE_AMOUNTS);){
  			Iterator<ChargeDTO> it = updateClaimList.iterator(); 
  			while (it.hasNext()) {
  				ChargeDTO cdto = it.next();
  				int i = 1;
  				ps.setBigDecimal(i++, cdto.getInsuranceClaimAmount());
  				ps.setBigDecimal(i++, cdto.getReturnInsuranceClaimAmt());
  				ps.setBoolean(i++, cdto.getFirstOfCategory());
  				ps.setBigDecimal(i++, cdto.getOrigInsuranceClaimAmount());
  				ps.setString(i++, cdto.getChargeId());
  				ps.addBatch();
  			}
  			int[] result = ps.executeBatch();
  			return DataBaseUtil.checkBatchUpdates(result);
			}
		}
	}

	private static final String HOSPITAL_CHARGE_HEADS_FIELDS =
		" SELECT b.bill_no, b.finalized_date, date(finalized_date) as voucher_date, b.is_tpa, tm.tpa_name as primary_sponsor_name, " +
		"	stpa.tpa_name as secondary_sponsor_name, " +
		"	case when sbah.account_head_name is null then bah.account_head_name else sbah.account_head_name end as account_head_name, " +
		"	max(b.insurance_deduction) as insurance_deduction, sum(bc.amount) AS amount, sum(bc.discount) as item_discount, " +
		"	b.primary_total_claim, b.secondary_total_claim, b.total_receipts, coalesce(b.deposit_set_off, 0) as deposit_set_off, " +
		"	coalesce(b.points_redeemed_amt, 0) as points_redeemed_amt, b.bill_type, min(b.visit_type) AS visit_type, " +
		"	min(b.restriction_type) AS restriction_type, COALESCE(pd.patient_name, isr.patient_name) AS patient_name, " +
		"	pd.last_name, sm.salutation, " +
		"	get_patient_full_name(sm.salutation, COALESCE(pd.patient_name, isr.patient_name), pd.middle_name, pd.last_name) AS patient_full_name, " +
		"	0 AS final_tax, 0 AS discount, 0 AS round_off, " +
		"	'HOSP'::text AS sale_id, -1 AS vat_rate, 'H'::text AS type, b.mod_time, b.account_group, " +
		"	'HOSP'::text as inter_comp_acc_group, -1 as inter_comp_account_group_id, 'Hospital Item'::text as charge_item_type, " +
		"	0 as med_category_id, ''::text as sales_cat_vat_account_prefix, ''::text as sales_store_vat_account_prefix, " +
		"	hcm.center_code, " +
		"	min(pr.op_type) as op_type, min(d.cost_center_code) as dept_center_code, " +
		"	hcm.center_id as visit_center_id, min(pr.mr_no) as mr_no " +
		" FROM bill b " +
		" 	JOIN bill_charge bc USING(bill_no) " +
		"	LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = b.visit_id " +
		"	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id " +
		"	LEFT JOIN tpa_master tm ON tm.tpa_id = pr.primary_sponsor_id " +
		"	LEFT JOIN tpa_master stpa ON stpa.tpa_id::text = pr.secondary_sponsor_id::text " +
		"	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id or hcm.center_id=isr.center_id) " + // this has to be left join for retail credit bills we can add round offs.
		"	LEFT JOIN department d ON (d.dept_id=pr.dept_name) " +
		"	LEFT JOIN patient_details pd ON pd.mr_no = pr.mr_no " +
		"	JOIN chargehead_constants cc ON (cc.chargehead_id=bc.charge_head)" +
		"	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id) " +
	 	// left join required on sub groups, for ex: round off's there will be no service sub group.
		"	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=bc.service_sub_group_id) " +
		"	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id) " +
		"	LEFT JOIN salutation_master sm ON sm.salutation_id = pd.salutation " +
		" WHERE b.status in ('S', 'C', 'F') AND bc.status != 'X' AND bc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET') ";
	private static final String HOSPITAL_CHARGE_HEADS_GROUP_FIELDS =
		" GROUP BY b.bill_no, b.account_group, b.is_tpa, tm.tpa_name, stpa.tpa_name, b.finalized_date, " +
		"	case when sbah.account_head_name is null then bah.account_head_name else sbah.account_head_name end, " +
		"	b.deposit_set_off, b.points_redeemed_amt, " +
	 	"	b.bill_type, pd.patient_name, isr.patient_name, pd.last_name, sm.salutation, pd.middle_name, " +
	 	"	b.mod_time, b.primary_total_claim, b.secondary_total_claim, b.total_receipts, " +
		"	hcm.center_code, hcm.center_id ";

	private static final String SALES_CHARGE_HEADS_FIELDS =
		" SELECT b.bill_no, b.finalized_date, date(finalized_date) as voucher_date, b.is_tpa, tm.tpa_name as primary_sponsor_name, stpa.tpa_name as secondary_sponsor_name, " +
	 	"	case when sbah.account_head_name is null then bah.account_head_name else sbah.account_head_name end as account_head_name, " +
	 	"	max(b.insurance_deduction) as insurance_deduction, " +
	 	"	sum(pms.amount) AS amount, sum(pms.disc) as item_discount, b.primary_total_claim, b.secondary_total_claim, " +
	 	"	b.total_receipts, coalesce(b.deposit_set_off, 0) as deposit_set_off, coalesce(b.points_redeemed_amt, 0) as points_redeemed_amt, " +
	 	"	b.bill_type, min(b.visit_type) AS visit_type, min(b.restriction_type) AS restriction_type, " +
		"	COALESCE(pd.patient_name, prc.customer_name) AS patient_name, pd.last_name, sm.salutation, " +
		"	get_patient_full_name(sm.salutation, COALESCE(pd.patient_name, prc.customer_name), pd.middle_name, pd.last_name) AS patient_full_name, " +
		"	sum(pms.tax) AS final_tax, 0 as discount, 0 as round_off, " +
		"	pmsm.sale_id, pms.tax_rate AS vat_rate, pmsm.type, b.mod_time, b.account_group, gm.inter_comp_acc_name, " +
		"	gm.account_group_id as inter_comp_account_group_id, 'Pharmacy Credit Item'::text as charge_item_type, " +
		"	scm.category_id, scm.sales_cat_vat_account_prefix, gd.sales_store_vat_account_prefix, " +
		"	hcm.center_code, " +
		"	min(pr.op_type) as op_type, min(d.cost_center_code) as dept_center_code, " +
		"	hcm.center_id as visit_center_id, min(pr.mr_no) as mr_no " +
		" FROM store_sales_main pmsm " +
	 	"	JOIN bill b ON b.bill_no = pmsm.bill_no " +
	    "	JOIN chargehead_constants cc ON (cc.chargehead_id=(case when b.bill_type='P' and pmsm.type='S' then 'PHMED' " +
		"		when b.bill_type='P' and pmsm.type='R' then 'PHRET' " +
		"		when b.bill_type='C' and pmsm.type='S' then 'PHCMED' " +
		"		when b.bill_type='C' and pmsm.type='R' then 'PHCRET' " +
		"		else 'HOSPITAL_OR_ISSUE_ITEM' end)) " +
		"	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id) " +
		"	JOIN store_sales_details pms ON pmsm.sale_id = pms.sale_id " +
		"	JOIN store_item_details sid ON pms.medicine_id=sid.medicine_id " +
		"	JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=sid.service_sub_group_id) " +
		"	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id) " +
		"	JOIN store_category_master scm ON scm.category_id=sid.med_category_id " +
		"	JOIN stores gd ON pmsm.store_id=gd.dept_id " +
		"	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id " +
		"	LEFT JOIN patient_details pd ON pd.mr_no = pr.mr_no " +
		"	LEFT JOIN salutation_master sm ON sm.salutation_id = pd.salutation " +
		"	LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id " +
		"	LEFT JOIN tpa_master tm ON tm.tpa_id = pr.primary_sponsor_id " +
		"	LEFT JOIN tpa_master stpa ON stpa.tpa_id::text = pr.secondary_sponsor_id::text " +
		"	LEFT JOIN hospital_center_master hcm ON (hcm.center_id=pr.center_id or hcm.center_id=prc.center_id) " +
		"	LEFT JOIN department d ON (d.dept_id=pr.dept_name) " +
		"	JOIN account_group_master gm ON gm.account_group_id=gd.account_group " + //-- (dont move this to the top joins: performance issue)
		" WHERE b.status in ('S', 'C', 'F')	" ;
	private static final String SALES_CHARGE_HEADS_GROUP_FIELDS =
		" GROUP BY b.bill_no, b.account_group, b.is_tpa, tm.tpa_name, stpa.tpa_name, b.finalized_date, " +
	 	"	case when sbah.account_head_name is null then bah.account_head_name else sbah.account_head_name end, " +
	 	"	pms.tax_rate, pmsm.discount, b.deposit_set_off, b.points_redeemed_amt, " +
	 	"	pmsm.round_off, pmsm.sale_id, gm.inter_comp_acc_name, inter_comp_account_group_id, pmsm.type, " +
	 	"	pd.patient_name, prc.customer_name, pd.last_name, sm.salutation, pd.middle_name, " +
	 	"	b.bill_type, b.mod_time, b.primary_total_claim, b.secondary_total_claim, b.total_receipts, scm.category_id, " +
	 	"	scm.sales_cat_vat_account_prefix, gd.sales_store_vat_account_prefix, " +
		"	hcm.center_code, hcm.center_id ";

	private static final String SALE_BILL_WISE_FIELDS =
		" SELECT b.bill_no, b.finalized_date, date(finalized_date) as voucher_date, b.is_tpa, tm.tpa_name as primary_sponsor_name, stpa.tpa_name as secondary_sponsor_name, " +
	 	"	'' as account_head_name, 0 as insurance_deduction, " +
	 	"	0 AS amount, 0 as item_discount, b.primary_total_claim, b.secondary_total_claim, " +
	 	"	b.total_receipts, coalesce(b.deposit_set_off, 0) as deposit_set_off, coalesce(b.points_redeemed_amt, 0) as points_redeemed_amt, " +
	 	"	b.bill_type, b.visit_type, b.restriction_type, " +
		"	COALESCE(pd.patient_name, prc.customer_name) AS patient_name, pd.last_name, sm.salutation, " +
		"	get_patient_full_name(sm.salutation, COALESCE(pd.patient_name, prc.customer_name), pd.middle_name, pd.last_name) AS patient_full_name, " +
		"	0 AS final_tax, pmsm.discount, pmsm.round_off, pmsm.sale_id, 0 AS vat_rate, pmsm.type, b.mod_time, " +
		"	b.account_group, gm.inter_comp_acc_name, " +
		"	gm.account_group_id as inter_comp_account_group_id, 'Pharmacy Credit Item'::text as charge_item_type, " +
		"	0 as category_id, '' as sales_cat_vat_account_prefix, '' as sales_store_vat_account_prefix, " +
		"	hcm.center_code, pr.op_type, d.cost_center_code, " +
		"	hcm.center_id as visit_center_id, pr.mr_no " +
		" FROM store_sales_main pmsm " +
	 	"	JOIN bill b ON b.bill_no = pmsm.bill_no " +
	    "	JOIN chargehead_constants cc ON (cc.chargehead_id=(case when b.bill_type='P' and pmsm.type='S' then 'PHMED' " +
		"		when b.bill_type='P' and pmsm.type='R' then 'PHRET' " +
		"		when b.bill_type='C' and pmsm.type='S' then 'PHCMED' " +
		"		when b.bill_type='C' and pmsm.type='R' then 'PHCRET' " +
		"		else 'HOSPITAL_OR_ISSUE_ITEM' end)) " +
		"	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id) " +
		"	JOIN store_sales_details pms ON pmsm.sale_id = pms.sale_id " +
		"	JOIN store_item_details sid ON pms.medicine_id=sid.medicine_id " +
		"	JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=sid.service_sub_group_id) " +
		"	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id) " +
		"	JOIN store_category_master scm ON scm.category_id=sid.med_category_id " +
		"	JOIN stores gd ON pmsm.store_id=gd.dept_id " +
		"	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id " +
		"	LEFT JOIN patient_details pd ON pd.mr_no = pr.mr_no " +
		"	LEFT JOIN salutation_master sm ON sm.salutation_id = pd.salutation " +
		"	LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id " +
		"	LEFT JOIN tpa_master tm ON tm.tpa_id = pr.primary_sponsor_id " +
		"	LEFT JOIN tpa_master stpa ON stpa.tpa_id::text = pr.secondary_sponsor_id::text " +
		"	LEFT JOIN hospital_center_master hcm ON (hcm.center_id=pr.center_id or hcm.center_id=prc.center_id) " +
		"	LEFT JOIN department d ON (d.dept_id=pr.dept_name) " +
		"	JOIN account_group_master gm ON gm.account_group_id=gd.account_group " + //-- (dont move this to the top joins: performance issue)
		" WHERE b.status in ('S', 'C', 'F')	" ;
	private static final String SALE_BILL_WISE_GROUP_FIELDS =
		" GROUP BY b.bill_no, b.account_group, b.is_tpa, tm.tpa_name, stpa.tpa_name, b.finalized_date, " +
	 	"	b.deposit_set_off, b.points_redeemed_amt, " +
	 	"	pmsm.sale_id, pmsm.discount, pmsm.round_off, gm.inter_comp_acc_name, inter_comp_account_group_id, pmsm.type, " +
	 	"	pd.patient_name, prc.customer_name, pd.last_name, sm.salutation, pd.middle_name, " +
	 	"	b.bill_type, b.mod_time, b.primary_total_claim, b.secondary_total_claim, b.total_receipts, " +
		"	hcm.center_code, hcm.center_id, b.visit_type, b.restriction_type, pr.op_type, pr.mr_no, d.cost_center_code ";

	private static final String ISSUE_CHARGE_HEADS_FIELDS =
		" SELECT b.bill_no, b.finalized_date, date(finalized_date) as voucher_date, b.is_tpa, tm.tpa_name as primary_sponsor_name, stpa.tpa_name as secondary_sponsor_name, " +
	 	"	case when sbah.account_head_name is null then bah.account_head_name else sbah.account_head_name end as account_head_name, " +
	 	"	max(b.insurance_deduction) as insurance_deduction, " +
	 	"	sum(bc.amount) AS amount, sum(bc.discount) as item_discount, b.primary_total_claim, b.secondary_total_claim, " +
	 	"	b.total_receipts, coalesce(b.deposit_set_off, 0) as deposit_set_off, coalesce(b.points_redeemed_amt, 0) as points_redeemed_amt, " +
	 	"	b.bill_type, min(b.visit_type) AS visit_type, min(b.restriction_type) AS restriction_type, " +
	 	"	pd.patient_name AS patient_name, pd.last_name, sm.salutation, " +
		"	get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_full_name, " +
		"	round(sum(((isu.amount*isu.qty*isu.vat)/100)),2) AS final_tax, " +
		"	0 as discount, 0 as round_off, isum.user_issue_no::text, isu.vat AS vat_rate, 'ISSUE'::text as type, " +
		"	b.mod_time, b.account_group, gm.inter_comp_acc_name, gm.account_group_id as inter_comp_account_group_id, " +
		"	'Store Issue Credit Item'::text as charge_item_type, scm.category_id, scm.sales_cat_vat_account_prefix, " +
		"	s.sales_store_vat_account_prefix,  hcm.center_code, " +
		"	min(pr.op_type) as op_type, min(d.cost_center_code) as dept_center_code, " +
		"	hcm.center_id as visit_center_id, min(pr.mr_no) as mr_no " +
		" FROM stock_issue_details isu " +
	 	"	JOIN bill_activity_charge bac ON isu.item_issue_no::text = bac.activity_id AND bac.activity_code = ('PHI') " +
		"	JOIN bill_charge bc ON bc.charge_id = bac.charge_id " +
	    "	JOIN chargehead_constants cc ON (cc.chargehead_id=bc.charge_head) " +
		"	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id) " +
		"	JOIN bill b ON b.bill_no = bc.bill_no " +
		"	JOIN patient_registration pr ON pr.patient_id = b.visit_id " +
		"	LEFT JOIN department d ON (d.dept_id=pr.dept_name) " +
		"	LEFT JOIN tpa_master tm ON tm.tpa_id = pr.primary_sponsor_id " +
		"	LEFT JOIN tpa_master stpa ON stpa.tpa_id::text = pr.secondary_sponsor_id::text " +
		"	JOIN patient_details pd ON pd.mr_no = pr.mr_no " +
		"	LEFT JOIN salutation_master sm ON sm.salutation_id = pd.salutation " +
		"	JOIN stock_issue_main isum ON isu.user_issue_no = isum.user_issue_no " +
		"	JOIN stores s ON s.dept_id=isum.dept_from " +
		"	JOIN account_group_master gm ON gm.account_group_id=bc.account_group " +
		"	JOIN store_item_details sid ON sid.medicine_id=isu.medicine_id " +
		"	JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=sid.service_sub_group_id) " +
		"	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id) " +
		"	JOIN store_category_master scm ON scm.category_id=sid.med_category_id " +
		"	JOIN hospital_center_master hcm ON (hcm.center_id=pr.center_id)	" +
		" WHERE b.status in ('S', 'C', 'F') AND bc.status != 'X' and bc.charge_head = 'INVITE' ";
	private static final String ISSUE_CHARGE_HEADS_GROUP_FIELDS =
		" GROUP BY b.bill_no, b.account_group, b.is_tpa, tm.tpa_name, stpa.tpa_name, b.finalized_date, " +
	 	"	case when sbah.account_head_name is null then bah.account_head_name else sbah.account_head_name end, " +
	 	"	isu.vat, b.deposit_set_off, b.points_redeemed_amt, " +
	 	" 	isum.user_issue_no, gm.inter_comp_acc_name, inter_comp_account_group_id, " +
	 	" 	pd.patient_name,pd.last_name, sm.salutation, pd.middle_name,  " +
	 	" 	b.bill_type, b.mod_time, b.primary_total_claim, b.secondary_total_claim, b.total_receipts, " +
	 	" 	scm.category_id, scm.sales_cat_vat_account_prefix, s.sales_store_vat_account_prefix, " +
	 	"	hcm.center_code, hcm.center_id";

	private static final String ISSUE_RETURNS_CHARGE_HEADS_FIELDS =
		// issue returns should be added to hospital bills hence grouped by visit center code
		" SELECT b.bill_no, b.finalized_date, date(finalized_date) as voucher_date, b.is_tpa, tm.tpa_name as primary_sponsor_name, stpa.tpa_name as secondary_sponsor_name, " +
		"	case when sbah.account_head_name is null then bah.account_head_name else sbah.account_head_name end as account_head_name, " +
		"	max(b.insurance_deduction) as insurance_deduction, " +
		"	sum(bc.amount) AS amount, sum(bc.discount) as item_discount, b.primary_total_claim, b.secondary_total_claim, " +
		" 	b.total_receipts, coalesce(b.deposit_set_off, 0) as deposit_set_off, coalesce(b.points_redeemed_amt, 0) as points_redeemed_amt, " +
		"	b.bill_type, min(b.visit_type) AS visit_type, min(b.restriction_type) AS restriction_type, " +
		"	pd.patient_name AS patient_name, pd.last_name, sm.salutation, " +
		"	get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_full_name, " +
		"	0 AS final_tax, 0 as discount, 0 as round_off, " +
		"	sirm.user_return_no::text, 0 AS vat_rate, 'ISSUE_RETURN'::text as type, b.mod_time, b.account_group, gm.inter_comp_acc_name, " +
		"	gm.account_group_id as inter_comp_account_group_id, 'Store Return Credit Item'::text as charge_item_type, " +
		"	scm.category_id, scm.sales_cat_vat_account_prefix, s.sales_store_vat_account_prefix, " +
		"	hcm.center_code, min(pr.op_type) as op_type, min(d.cost_center_code) as dept_center_code,  " +
		"	hcm.center_id as visit_center_id, min(pr.mr_no) as mr_no " +
		" FROM store_issue_returns_details sird " +
		"	JOIN bill_activity_charge bac ON sird.item_return_no::text = bac.activity_id AND bac.activity_code = ('PHI') " +
		"	JOIN bill_charge bc ON bc.charge_id = bac.charge_id " +
		"	JOIN chargehead_constants cc ON (cc.chargehead_id=bc.charge_head) " +
		"	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id) " +
		"	JOIN bill b ON b.bill_no = bc.bill_no " +
		"	JOIN patient_registration pr ON pr.patient_id = b.visit_id " +
		"	LEFT JOIN department d ON (d.dept_id=pr.dept_name) " +
		"	JOIN hospital_center_master hcm ON pr.center_id=hcm.center_id " +
		"	LEFT JOIN tpa_master tm ON tm.tpa_id = pr.primary_sponsor_id " +
		"	LEFT JOIN tpa_master stpa ON stpa.tpa_id::text = pr.secondary_sponsor_id::text " +
		"	JOIN patient_details pd ON pd.mr_no = pr.mr_no " +
		"	LEFT JOIN salutation_master sm ON sm.salutation_id = pd.salutation " +
		"	JOIN store_issue_returns_main sirm ON sirm.user_return_no = sird.user_return_no " +
		"	JOIN stores s ON s.dept_id=sirm.dept_to " +
		"	JOIN account_group_master gm ON gm.account_group_id=bc.account_group " +
		"	JOIN store_item_details sid ON sid.medicine_id=sird.medicine_id " +
		"	JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=sid.service_sub_group_id) " +
		"	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id) " +
		"	JOIN store_category_master scm ON scm.category_id=sid.med_category_id " +
		" WHERE b.status in ('S', 'C', 'F') AND bc.status != 'X' and bc.charge_head = 'INVRET' " ;
	private static final String ISSUE_RETURNS_CHARGE_HEADS_GROUP_FIELDS =
		" GROUP BY b.bill_no, b.account_group, b.is_tpa, tm.tpa_name, stpa.tpa_name, b.finalized_date, " +
		"	case when sbah.account_head_name is null then bah.account_head_name else sbah.account_head_name end, " +
		"	b.deposit_set_off, b.points_redeemed_amt, " +
		" 	sirm.user_return_no, gm.inter_comp_acc_name, inter_comp_account_group_id, " +
		" 	pd.patient_name,pd.last_name, sm.salutation, pd.middle_name, " +
		" 	b.bill_type, b.mod_time, b.primary_total_claim, b.secondary_total_claim, b.total_receipts, " +
		" 	scm.category_id, scm.sales_cat_vat_account_prefix, s.sales_store_vat_account_prefix, " +
		"	hcm.center_code, hcm.center_id" ;


	/*
	 * Gives a bill-by-bill summary based on account heads. This is for export
	 * to Tally.
	 */
	public static Object[] getHospitalBillSummary(Connection con, java.sql.Timestamp fromDate,
			java.sql.Timestamp toDate, Integer accountGroup, int centerId, boolean IP, List bills) throws SQLException {
		StringBuilder where = new StringBuilder();
		if (IP) {
			where.append(" AND b.visit_type='i' AND bill_type in ('P', 'C') AND restriction_type='N'");
		} else {
			// retail patients belongs to pharmacy bill.
			where.append(" AND b.visit_type not in ('i', 'r') AND bill_type in ('P', 'C') AND restriction_type in ('N', 'T') ");
		}
		if (fromDate != null && toDate != null) {
			where.append(" AND b.account_group=? AND b.mod_time BETWEEN ? AND ? " );
			if (centerId != 0)
				where.append(" AND hcm.center_id=? ");
		} else {
			if (bills == null || bills.isEmpty()) return new Object[]{};

			DataBaseUtil.addWhereFieldInList(where, "b.bill_no", bills, true);
		}
		StringBuilder query = new StringBuilder();
		query.append(HOSPITAL_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(HOSPITAL_CHARGE_HEADS_GROUP_FIELDS);
		query.append(" UNION ALL ");
		query.append(SALES_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(SALES_CHARGE_HEADS_GROUP_FIELDS);
		query.append(" UNION ALL ");
		query.append(ISSUE_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(ISSUE_CHARGE_HEADS_GROUP_FIELDS);
		query.append(" UNION ALL ");
		query.append(ISSUE_RETURNS_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(ISSUE_RETURNS_CHARGE_HEADS_GROUP_FIELDS);

		try(PreparedStatement ps = con.prepareStatement(query.toString() + " ORDER BY bill_no, account_head_name");){
  
  		if (fromDate != null && toDate != null) {
  			int index = 1;
  			for (int i=0; i<4; i++) {
  				ps.setInt(index++, accountGroup);
  				ps.setTimestamp(index++, fromDate);
  				ps.setTimestamp(index++, toDate);
  				if (centerId != 0)
  					ps.setInt(index++, centerId);
  			}
  		} else {
  			Iterator it =  bills.iterator();
  			int i = 1;
  			for (int j=0; j<4; j++) {
  				while (it.hasNext()) {
  					ps.setString(i++, (String) ((Map) it.next()).get("voucher_no"));
  				}
  			}
  		}
  		try(ResultSet rs =  ps.executeQuery();){
  		  return new Object[]{ps, rs};
  		}
		}
	}

	public static Object[] getHospitalBillSummary(Connection con, java.sql.Timestamp fromDate,
			java.sql.Timestamp toDate, Integer accountGroup, int centerId, List bills) throws SQLException, ClassNotFoundException {
		StringBuilder where = new StringBuilder();
		where.append(" AND bill_type in ('P', 'C') AND restriction_type in ('N', 'T') ");
		if (fromDate != null && toDate != null) {
			where.append(" AND b.account_group=? AND b.mod_time BETWEEN ? AND ? " );
			if (centerId != 0)
				where.append(" AND hcm.center_id=? ");
		} else {
			if (bills == null || bills.isEmpty()) return new Object[]{};

			//DataBaseUtil.addWhereFieldInList(where, "b.bill_no", bills, true);
			String[] placeHolderArr = new String[bills.size()];
      Arrays.fill(placeHolderArr, "?");
      String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
      where.append("AND b.bill_no in ( " + placeHolders  + ")");
		}
		StringBuilder query = new StringBuilder();
		query.append(HOSPITAL_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(HOSPITAL_CHARGE_HEADS_GROUP_FIELDS);
		query.append(" UNION ALL ");
		query.append(SALES_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(SALES_CHARGE_HEADS_GROUP_FIELDS);
		query.append(" UNION ALL ");
		query.append(SALE_BILL_WISE_FIELDS);
		query.append(where.toString());
		query.append(SALE_BILL_WISE_GROUP_FIELDS);
		query.append(" UNION ALL ");
		query.append(ISSUE_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(ISSUE_CHARGE_HEADS_GROUP_FIELDS);
		query.append(" UNION ALL ");
		query.append(ISSUE_RETURNS_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(ISSUE_RETURNS_CHARGE_HEADS_GROUP_FIELDS);

		try (PreparedStatement ps = con.prepareStatement(query.toString() + " ORDER BY bill_no, account_head_name");){ 
  		if (fromDate != null && toDate != null) {
  			int index = 1;
  			for (int i=0; i<5; i++) {
  				// iterate five unions
  				ps.setInt(index++, accountGroup);
  				ps.setTimestamp(index++, fromDate);
  				ps.setTimestamp(index++, toDate);
  				if (centerId != 0)
  					ps.setInt(index++, centerId);
  			}
  		} else {
  			Iterator it =  bills.iterator();
  			int i = 1;
  			for (int j=0; j<5; j++) {
  				while (it.hasNext()) {
  					ps.setString(i++, (String) ((Map) it.next()).get("voucher_no"));
  				}
  			}
  		}
      try (ResultSet rs = ps.executeQuery();) {
        DynaBeanBuilder builder = DynaBeanBuilder.getDynaBeanBuilder(rs);
        List<BasicDynaBean> recordsList = new ArrayList<>();
        while (rs.next()) {
          BasicDynaBean record = builder.build();
          record = DynaBeanBuilder.loadBean(rs, record);
          recordsList.add(record);
        }
        return new Object[] { ps, rs, recordsList };
      }
		}
	}

	public static Object[] getPharmacyBillNowSummary(Connection con, java.sql.Timestamp fromDate,
			java.sql.Timestamp toDate, Integer accountGroup, int centerId, boolean IP, List bills) throws SQLException {
		StringBuilder where = new StringBuilder();

		if (IP) {
			//pharmacy bill now bills(IP)
			where.append(" AND b.visit_type='i' AND bill_type = 'P' AND restriction_type = 'P' ");
		} else {
			// pharmacy bill now bills(OP), retail bill now bills, pharmacy return bills
			where.append(" AND b.visit_type!='i' AND bill_type='P' AND restriction_type='P' ");
		}
		if (fromDate != null && toDate != null) {
			where.append(" AND b.account_group=? AND b.mod_time BETWEEN ? AND ? ");
			if (centerId != 0)
				where.append(" AND hcm.center_id=?");
		} else {
			if (bills == null || bills.isEmpty()) {
			  return new Object[]{};
			}
			String[] placeHolderArr = new String[bills.size()];
      Arrays.fill(placeHolderArr, "?");
      String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
      where.append("AND b.bill_no in ( " + placeHolders  + ")");
			//DataBaseUtil.addWhereFieldInList(where, "b.bill_no", bills, true);
		}

		StringBuilder query = new StringBuilder();
		query.append(HOSPITAL_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(HOSPITAL_CHARGE_HEADS_GROUP_FIELDS);
		query.append(" UNION ALL ");
		query.append(SALES_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(SALES_CHARGE_HEADS_GROUP_FIELDS);
		query.append(" UNION ALL ");
		query.append(ISSUE_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(ISSUE_CHARGE_HEADS_GROUP_FIELDS);
		query.append(" UNION ALL ");
		query.append(ISSUE_RETURNS_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(ISSUE_RETURNS_CHARGE_HEADS_GROUP_FIELDS);

		try(PreparedStatement ps = con.prepareStatement(query.toString() + " ORDER BY bill_no, account_head_name");){
  		if (fromDate != null && toDate != null) {
  			int index = 1;
  			for (int i=0; i<4; i++) {
  				ps.setInt(index++, accountGroup);
  				ps.setTimestamp(index++, fromDate);
  				ps.setTimestamp(index++, toDate);
  				if (centerId != 0)
  					ps.setInt(index++, centerId);
  			}
  		} else {
  			Iterator it =  bills.iterator();
  			int i = 1;
  			for (int j=0; j<4; j++) {
  				while (it.hasNext()) {
  					ps.setString(i++, (String) ((Map) it.next()).get("voucher_no"));
  				}
  			}
  		}
  		try(ResultSet rs =  ps.executeQuery();){
  		  return new Object[]{ps, rs};
  		} 
		}
	}

	public static Object[] getPharmacyCreditBillSummary(Connection con, java.sql.Timestamp fromDate,
			java.sql.Timestamp toDate, Integer accountGroup, int centerId, boolean IP, List bills) throws SQLException {
		StringBuilder where = new StringBuilder();

		if (IP) {
			where.append(" AND b.visit_type='i' AND bill_type='C' and restriction_type='P' ");
		} else {
			where.append(" AND b.visit_type!='i' AND bill_type='C' and restriction_type='P' ");
		}
		if (fromDate != null && toDate != null) {
			where.append(" AND b.account_group=? AND b.mod_time BETWEEN ? AND ? ");
			if (centerId != 0)
				where.append(" AND hcm.center_id=? ");
		} else {
			if (bills == null || bills.isEmpty()){
			  return new Object[]{};
			}
			String[] placeHolderArr = new String[bills.size()];
      Arrays.fill(placeHolderArr, "?");
      String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
      where.append("AND b.bill_no in ( " + placeHolders  + ")");
			//DataBaseUtil.addWhereFieldInList(where, "b.bill_no", bills, true);
		}
		StringBuilder query = new StringBuilder();
		query.append(HOSPITAL_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(HOSPITAL_CHARGE_HEADS_GROUP_FIELDS);
		query.append(" UNION ALL ");
		query.append(SALES_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(SALES_CHARGE_HEADS_GROUP_FIELDS);
		query.append(" UNION ALL ");
		query.append(ISSUE_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(ISSUE_CHARGE_HEADS_GROUP_FIELDS);
		query.append(" UNION ALL ");
		query.append(ISSUE_RETURNS_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(ISSUE_RETURNS_CHARGE_HEADS_GROUP_FIELDS);
		
		try(PreparedStatement ps = con.prepareStatement(query.toString() + " ORDER BY bill_no, account_head_name");){
    		if (fromDate != null && toDate != null) {
    			int index = 1;
    			for (int i=0; i<4; i++) {
    				ps.setInt(index++, accountGroup);
    				ps.setTimestamp(index++, fromDate);
    				ps.setTimestamp(index++, toDate);
    				if (centerId != 0)
    					ps.setInt(index++, centerId);
    			}
    		} else {
    			Iterator it =  bills.iterator();
    			int i = 1;
    			for (int j=0; j<4; j++) {
    				while (it.hasNext()) {
    					ps.setString(i++, (String) ((Map) it.next()).get("voucher_no"));
    				}
    			}
    		}	
    		try(ResultSet rs =  ps.executeQuery();) {
    		  return new Object[]{ps, rs};
    		}
		  }
	}

	public static Object[] getPharmacyBillSummary(Connection con, java.sql.Timestamp fromDate,
			java.sql.Timestamp toDate, Integer accountGroup, int centerId, List bills) throws SQLException, ClassNotFoundException {
		StringBuilder where = new StringBuilder();

		where.append(" AND restriction_type='P' AND bill_type IN ('C', 'P') ");
		if (fromDate != null && toDate != null) {
			where.append(" AND b.account_group=? AND b.mod_time BETWEEN ? AND ? ");
			if (centerId != 0)
				where.append(" AND hcm.center_id=? ");
		} else {
			if (bills == null || bills.isEmpty()) return new Object[]{};

			DataBaseUtil.addWhereFieldInList(where, "b.bill_no", bills, true);
		}
		StringBuilder query = new StringBuilder();
		query.append(HOSPITAL_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(HOSPITAL_CHARGE_HEADS_GROUP_FIELDS);
		query.append(" UNION ALL ");
		query.append(SALES_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(SALES_CHARGE_HEADS_GROUP_FIELDS);
		query.append(" UNION ALL ");
		query.append(SALE_BILL_WISE_FIELDS);
		query.append(where.toString());
		query.append(SALE_BILL_WISE_GROUP_FIELDS);
		query.append(" UNION ALL ");
		query.append(ISSUE_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(ISSUE_CHARGE_HEADS_GROUP_FIELDS);
		query.append(" UNION ALL ");
		query.append(ISSUE_RETURNS_CHARGE_HEADS_FIELDS);
		query.append(where.toString());
		query.append(ISSUE_RETURNS_CHARGE_HEADS_GROUP_FIELDS);
		try(PreparedStatement ps = con.prepareStatement(query.toString() + " ORDER BY bill_no, account_head_name");){  
  		if (fromDate != null && toDate != null) {
  			int index = 1;
  			for (int i=0; i<5; i++) {
  				// iterate five unions
  				ps.setInt(index++, accountGroup);
  				ps.setTimestamp(index++, fromDate);
  				ps.setTimestamp(index++, toDate);
  				if (centerId != 0)
  					ps.setInt(index++, centerId);
  			}
  		} else {
  			Iterator it =  bills.iterator();
  			int i = 1;
  			for (int j=0; j<5; j++) {
  				while (it.hasNext()) {
  					ps.setString(i++, (String) ((Map) it.next()).get("voucher_no"));
  				}
  			}
  		}  		
      try (ResultSet rs = ps.executeQuery();) {
        DynaBeanBuilder builder = DynaBeanBuilder.getDynaBeanBuilder(rs);
        List<BasicDynaBean> recordsList = new ArrayList<>();
        while (rs.next()) {
          BasicDynaBean record = builder.build();
          record = DynaBeanBuilder.loadBean(rs, record);
          recordsList.add(record);
        }
        return new Object[] { ps, rs, recordsList };
      }
		}
	}



	/*private static final String HOSPITAL_CREDIT_BILL_CHARGE_ITEMS =
		" SELECT * FROM bill_amounts_account_head_view " +
		" WHERE bill_type='C' AND restriction_type='N' AND charge_item_type != 'Hospital Item' " +
		" 	AND account_group!=inter_comp_account_group_id ";
	public static List getHospitalCreditBillsChargeItems(Connection con, java.sql.Timestamp fromDate,
			java.sql.Timestamp toDate, Integer accountGroup, List bills) throws SQLException {
			StringBuilder where = new StringBuilder();
			if (fromDate != null && toDate != null) {
				where.append(" AND mod_time BETWEEN ? AND ? AND account_group=?");
			} else {
				if (bills == null || bills.isEmpty()) return Collections.EMPTY_LIST;

				DataBaseUtil.addWhereFieldInList(where, "bill_no", bills, true);
			}
			try(PreparedStatement ps = con.prepareStatement(HOSPITAL_CREDIT_BILL_CHARGE_ITEMS + where.toString() +
					" ORDER BY bill_no, inter_comp_account_group_id, sale_id");){
    			if (fromDate != null && toDate != null) {
    				ps.setTimestamp(1, fromDate);
    				ps.setTimestamp(2, toDate);
    				ps.setInt(3, accountGroup);
    			} else {
    				Iterator it =  bills.iterator();
    				int i = 1;
    				while (it.hasNext()) {
    					ps.setString(i++, (String) ((Map) it.next()).get("voucher_no"));
    				}
    			}
    			return DataBaseUtil.queryToDynaList(ps);
			 }
	}*/

	/*private static final String OTHER_ACC_GROUP_ITEMS_IN_HOSP_CREDITBILL =
		" SELECT * FROM bill_amounts_account_head_view " +
		" WHERE bill_type='C' AND restriction_type='N' AND account_group!=inter_comp_account_group_id " +
		" 	AND charge_item_type != 'Hospital Item' " ;
	public static List getSelectedAccountGroupItemsInHospCreditBil(Connection con, java.sql.Timestamp fromDate,
			java.sql.Timestamp toDate, Integer interComAccountGroup, List bills) throws SQLException {
			StringBuilder where = new StringBuilder();
			if (fromDate != null && toDate != null) {
				where.append(" AND mod_time BETWEEN ? AND ? AND inter_comp_account_group_id=?");
			} else {
				if (bills == null || bills.isEmpty()) return Collections.EMPTY_LIST;

				DataBaseUtil.addWhereFieldInList(where, "bill_no", bills, true);
			}
			try(PreparedStatement ps = con.prepareStatement(OTHER_ACC_GROUP_ITEMS_IN_HOSP_CREDITBILL + where.toString() +
					" ORDER BY bill_no, inter_comp_account_group_id, sale_id");){
    			if (fromDate != null && toDate != null) {
    				ps.setTimestamp(1, fromDate);
    				ps.setTimestamp(2, toDate);
    				ps.setInt(3, interComAccountGroup);
    			} else {
    				Iterator it =  bills.iterator();
    				int i = 1;
    				while (it.hasNext()) {
    					ps.setString(i++, (String) ((Map) it.next()).get("voucher_no"));
    				}
    			}
    			return DataBaseUtil.queryToDynaList(ps);
			}
	}*/

	/*
	 * payment related updations
	 */

	// For Package Payments
	public static final String PACKAGE_BILL_CHARGES =
		" SELECT b.*, bc.charge_head AS billcharge_chargehead, bc.amount, "
		+ " bc.prescribing_dr_id, bc.dr_discount_amt, bc.discount, bac.* ,"
		+ " bc.*, pr.bed_type, pr.ward_id, pr.ward_name, pr.mlc_status, "
		+ " pr.patient_id, pr.reference_docto_id  "
		+ "FROM  bill_charge bc "
		+ " JOIN bill_activity_charge bac ON (bac.charge_id = bc.charge_id)"
		+ " JOIN bill b USING (bill_no) "
		+ " LEFT JOIN patient_registration pr ON (pr.patient_id=b.visit_id) "
		+ " WHERE bc.charge_id=?  ";

	public static List<BasicDynaBean> getPackageActivities(Connection con, String chargeId)
		throws SQLException {
		List chargeItems = null;
		try(PreparedStatement ps = con.prepareStatement(PACKAGE_BILL_CHARGES);) {
			ps.setString(1, chargeId);
			chargeItems = DataBaseUtil.queryToDynaList(ps);
		}
		return chargeItems;
	}

	public static final String CHARGE_PAYMENT_DETAILS =
		" SELECT bc.*, bac.activity_conducted,pr.center_id::text, " +
		"  pr.patient_id, pr.org_id, pr.reference_docto_id, "  +
		"  cd.payment_category::text as con_doc_category, cd.payment_eligible as con_doc_eligible, " +
		"  pd.payment_category::text as pres_doc_category, pd.payment_eligible as pres_doc_eligible, " +
		"  COALESCE(rd.payment_category::text, rr.payment_category::text) AS ref_doc_category, " +
		"  COALESCE(rd.payment_eligible, rr.payment_eligible) AS ref_doc_eligible " +
		" FROM bill_charge bc " +
		"  JOIN bill b USING(bill_no) " +
		"  LEFT JOIN bill_activity_charge bac USING(charge_id) " +
		"  LEFT JOIN patient_registration pr on pr.patient_id=b.visit_id " +
		"  LEFT JOIN incoming_sample_registration isr ON  (isr.billno = b.bill_no) "+
		"  LEFT JOIN doctors cd ON (cd.doctor_id = bc.payee_doctor_id) " +
		"  LEFT JOIN doctors pd ON (pd.doctor_id = bc.prescribing_dr_id) " +
		"  LEFT JOIN doctors rd ON (rd.doctor_id = pr.reference_docto_id OR  "+
		"  rd.doctor_id= isr.referring_doctor) " +
		"  LEFT JOIN referral rr ON (rr.referal_no = pr.reference_docto_id OR  " +
		"  rr.referal_no = isr.referring_doctor ) " +
		" WHERE bc.charge_id=? ";

	public static BasicDynaBean getChargePaymentDetails(Connection con, String chargeId) throws SQLException {
		try (PreparedStatement ps = con.prepareStatement(CHARGE_PAYMENT_DETAILS);){
			ps.setString(1, chargeId);
			return DataBaseUtil.queryToDynaBean(ps);
		}
	}

	// get doctorid using testid from bill_charge table for test conducting
	// doctor
	public static final String BILL_CHARGEID =
		"SELECT charge_id, charge_group, b.status  "
		+ " FROM bill_charge bc join bill b using (bill_no) where bill_no= ? and payee_doctor_id is null ";

	public static List<BasicDynaBean> getBillChargeId(String billNo)
			throws SQLException {
		List chargeIds = null;
		try(Connection con = DataBaseUtil.getReadOnlyConnection();
		    PreparedStatement ps = con.prepareStatement(BILL_CHARGEID);){
			ps.setString(1, billNo);
			chargeIds = DataBaseUtil.queryToDynaList(ps);
		}
		return chargeIds;
	}

	public static final String GET_CHARGE_GROUP =
		" SELECT bc.charge_group,bc.hasactivity, coalesce(tp.conducted,sp.conducted) as conducted  " +
		" FROM bill_charge bc " +
		"  LEFT JOIN bill_activity_charge bac ON (bac.charge_id = bc.charge_id)" +
		"  JOIN bill b USING(bill_no) " +
		"  LEFT JOIN tests_prescribed tp ON (bc.act_description_id=tp.test_id)  AND (b.visit_id=tp.pat_id) AND (tp.prescribed_id::varchar = bac.activity_id)" +
		"  LEFT JOIN services_prescribed sp on (sp.patient_id=b.visit_id) AND (sp.service_id=bc.act_description_id)  AND (sp.prescription_id::varchar = bac.activity_id) " +
		"  WHERE bc.charge_id=? ";

	public static List getChargeGroup(String chargeId) throws SQLException {
		try(Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(GET_CHARGE_GROUP);){
			ps.setString(1, chargeId);
			return DataBaseUtil.queryToDynaList(ps);
		}
	}


	private static final String TEST_DOCTOR =
		" SELECT tc.test_id, tc.conducted_by " +
		" FROM tests_conducted tc " +
		" WHERE prescribed_id::varchar=? ";

	public static BasicDynaBean getTestDoctorDetails(Connection con, String prescribedId)
			throws SQLException {
	  try(PreparedStatement ps = con.prepareStatement(TEST_DOCTOR);){
			ps.setString(1, prescribedId);
			List<BasicDynaBean> testList = DataBaseUtil.queryToDynaList(ps);
			if (testList.size() > 0)
				return testList.get(0);
			return null;
		}
	}

	public static final String DOCTORS = " SELECT * from doctors where doctor_id= ? ";

	public static List<BasicDynaBean> getDoctorsDetails(String doctorId)
			throws SQLException {

		List docList = null;
		try(Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(DOCTORS);){
			ps.setString(1, doctorId);
			docList = DataBaseUtil.queryToDynaList(ps);
		}
		return docList;
	}

	public static final String SERVICE_DOCTOR_ID = "SELECT doctor from patient_registration where patient_id=? ";

	public static String getServiceDoctorId(String visitId) throws SQLException {
		try(Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(SERVICE_DOCTOR_ID);){
			ps.setString(1, visitId);
			return DataBaseUtil.getStringValueFromDb(ps);
		}
	}


	private static final String PATIENT_COUNT_GROUPED = "SELECT COALESCE(#, '(None)') as # ,CASE WHEN patient_gender IS NULL THEN 'O' ELSE patient_gender END AS patient_gender,"
			+ " count(patient_id)::numeric as count, "
			+ " visit_type_name||revisit as visit_type "
			+ " from patient_visit_details_ext_view where reg_date between ? and ? @ group by "
			+ " #, patient_gender,visit_type_name,revisit ";

	public static List<BasicDynaBean> getPatientCountGrouped(Connection con,
			Date from, Date to, String groupBy, String centerClause) throws SQLException {
		groupBy = DataBaseUtil.quoteIdent(groupBy);
		String query = PATIENT_COUNT_GROUPED.replace("#", groupBy);
		query = query.replace("@", centerClause);
		return DataBaseUtil.queryToDynaListDates(con, query, from, to);
	}

	private static final String PATIENT_COUNT_TREND = "  SELECT   COALESCE(#, '(None)') as #, patient_gender, "
			+ " to_char(date(date_trunc('%',reg_date)),'yyyy-MM-dd') as period"
			+ ", count(patient_id)::numeric as count  FROM patient_visit_details_ext_view  "
			+ " WHERE date(reg_date) BETWEEN ? AND ? @ GROUP BY #, period,patient_gender";

	public static List<BasicDynaBean> getPatientCountTrend(Connection con,
			Date from, Date to, String trend, String groupBy, String centerClause)
			throws SQLException {

		groupBy = DataBaseUtil.quoteIdent(groupBy);
		if (!trend.equals("month") && !trend.equals("week")
				&& !trend.equals("day")) {
			return null;
		}
		String query = PATIENT_COUNT_TREND.replace("%", trend);
		query = query.replace("#", groupBy);
		query = query.replace("@", centerClause);
		return DataBaseUtil.queryToDynaListDates(query, from, to);
	}

	private static final String PATIENT_VISIT_COUNT_TREND = "  SELECT   COALESCE(#, '(None)') as #, revisit, "
			+ " to_char(date(date_trunc('%',reg_date)),'yyyy-MM-dd') as period"
			+ ", count(patient_id)::numeric as count  FROM patient_visit_details_ext_view  "
			+ " WHERE date(reg_date) BETWEEN ? AND ? @ GROUP BY #, period,revisit";

	public static List<BasicDynaBean> getPatientVisitCountTrend(Connection con,
			Date from, Date to, String trend, String groupBy, String centerClause)
			throws SQLException {
		groupBy = DataBaseUtil.quoteIdent(groupBy);
		if (!trend.equals("month") && !trend.equals("week")
				&& !trend.equals("day")) {
			return null;
		}
		String query = PATIENT_VISIT_COUNT_TREND.replace("%", trend);
		query = query.replace("#", groupBy);
		query = query.replace("@", centerClause);
		return DataBaseUtil.queryToDynaListDates(query, from, to);
	}

	private static final String PATIENT_ADMIT_DISCHARGE_TREND = " select COALESCE(#, '(None)') as #,"
		+ " to_char(date(date_trunc('%',start_date)),'yyyy-MM-dd') as period,"
		+ " 'AD' as state ,"
		+ " count(start_date) from patient_visit_details_ext_view where date(start_date) "
		+ " between ? and ? @ "
		+ " group by #,date_trunc('%',start_date) "
		+ " union "
		+ " select COALESCE(#, '(None)') as #,to_char(date(date_trunc('%',discharge_date)),'yyyy-MM-dd') as period ,"
		+ " 'DI' as state,  "
		+ " count(discharge_date) from patient_visit_details_ext_view where date(discharge_date) "
		+ " between ? and ? @ and patient_id in (select patient_id from patient_registration where discharge_flag='D') "
		+ " group by #,date_trunc('%',discharge_date) ;";

	public static List<BasicDynaBean> getPatientAdmitDischargeCountTrend(
			Connection con, Date from, Date to, String trend, String groupBy, String centerClause)
			throws SQLException {
		groupBy = DataBaseUtil.quoteIdent(groupBy);
		if (!trend.equals("month") && !trend.equals("week")
				&& !trend.equals("day")) {
			return null;
		}
		String query = PATIENT_ADMIT_DISCHARGE_TREND.replace("%", trend);
		query = query.replace("#", groupBy);
		query = query.replace("@", centerClause);
		try(PreparedStatement ps = con.prepareStatement(query);){
  		ps.setDate(1, from);
  		ps.setDate(2, to);
  		ps.setDate(3, from);
  		ps.setDate(4, to);
  		return DataBaseUtil.queryToDynaListDates(ps);
		}
	}

	private static final String UPDATE_HAS_ACTIVITY =
		"UPDATE bill_charge SET hasactivity=? WHERE charge_id=?";
	private static final String UPDATE_HAS_ACTIVITY_REFS =
		"UPDATE bill_charge SET hasactivity=? WHERE charge_ref=?";

	public boolean updateHasActivityStatus(String chargeId, boolean hasActivity, boolean alsoRefs)
			throws SQLException {
		boolean status = false;
		try(PreparedStatement ps = con.prepareStatement(UPDATE_HAS_ACTIVITY);){
  		ps.setBoolean(1, hasActivity);
  		ps.setString(2, chargeId);
  		status = ps.executeUpdate() > 0;
		}
		if (status && alsoRefs) {
			try(PreparedStatement ps = con.prepareStatement(UPDATE_HAS_ACTIVITY_REFS);){
  			ps.setBoolean(1, hasActivity);
  			ps.setString(2, chargeId);
  			ps.executeUpdate();
			}
		}
		return status;
	}

	public boolean updateHasActivityStatus(String chargeId, boolean hasActivity)
			throws SQLException {
		return updateHasActivityStatus(chargeId, hasActivity, false);
	}

	private static final String ALL_PAYMENTS_DUE =
		" SELECT coalesce(bill_no, payment_id)||to_char(mod_time, 'yyMMdd') as voucher_no, bill_no, amount, " +
		"	payee_name, payment_type, name, posted_date, " +
		"	date(posted_date) as voucher_date, payment_id, account_group, " +
		"	account_head_name, center_code, dept_center_code, visit_type, op_type, mod_time, " +
		"	salutation, patient_name, middle_name, last_name, full_name, charge_head, chargehead_name " +
		" FROM 	all_payments_due_view ";
	public static List<BasicDynaBean> getAllPaymentsDue(Connection con, java.sql.Timestamp fromDate,
			java.sql.Timestamp toDate, Integer accountGroup, int centerId, List billNos) throws SQLException {
		PreparedStatement ps = null;
		try {
			StringBuilder where = new StringBuilder();
			if (fromDate != null && toDate != null) {
				where.append(" WHERE mod_time BETWEEN ? AND ? AND account_group=? ");
				if (centerId != 0)
					where.append(" AND expense_center_id=? ");
			} else {
				if (billNos == null || billNos.isEmpty()) {
				  return Collections.EMPTY_LIST;
				} 
				String[] placeHolderArr = new String[billNos.size()];
        Arrays.fill(placeHolderArr, "?");
        String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
        where.append("WHERE coalesce(bill_no, payment_id)||to_char(mod_time, 'yyMMdd') in ( " + placeHolders  + ")");

				//DataBaseUtil.addWhereFieldInList(where, "coalesce(bill_no, payment_id)||to_char(mod_time, 'yyMMdd')", billNos);
			}
			List<Object> values = new ArrayList<Object>();
			//ps = con.prepareStatement(ALL_PAYMENTS_DUE + where.toString() + " ORDER BY bill_no, date(mod_time) ");
			if (fromDate != null && toDate != null) {
				//ps.setTimestamp(1, fromDate);
				//ps.setTimestamp(2, toDate);
				//ps.setInt(3, accountGroup);
			  values.add(fromDate);
			  values.add(toDate);
			  values.add(accountGroup);
				if (centerId != 0) {
					//ps.setInt(4, centerId);
				  values.add(centerId);
				}	
			} else {
				/*Iterator it =  billNos.iterator();
				int i = 1;
				while (it.hasNext()) {
					ps.setString(i++, (String) ((Map) it.next()).get("voucher_no"));
				}*/
			  values.addAll(billNos);
			}
			return DataBaseUtil.queryToDynaList(ALL_PAYMENTS_DUE + where.toString() + 
			      " ORDER BY bill_no, date(mod_time) ", values.toArray());

		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String ALL_PAYMENT_VOUCHERS =
			" SELECT voucher_no, amount, tds_amount, reference_no, " +
			"	payee_name, payment_type, payment_mode, card_type, name, mod_time, date, date(date) as voucher_date," +
			"	voucher_category, counter, bank, spl_account_name, ref_required, bank_required, center_code, " +
			"	tax_amount, round_off " +
			" FROM all_payment_vouchers_view ";

	public static List<BasicDynaBean> getAllPaymentVouchers(Connection con, java.sql.Timestamp fromDate,
			java.sql.Timestamp toDate, Integer accountGroup, int centerId, List voucherNos) throws SQLException {
		PreparedStatement ps = null;
		try {
			StringBuilder where = new StringBuilder();
			if (fromDate != null && toDate != null) {
				where.append(" WHERE mod_time BETWEEN ? AND ? AND COALESCE(account_group_id, 1)=? ");
				if (centerId != 0)
					where.append(" AND center_id=? ");
			} else {
				if (voucherNos == null || voucherNos.isEmpty()) {
				   return Collections.EMPTY_LIST;
				}
				String[] placeHolderArr = new String[voucherNos.size()];
        Arrays.fill(placeHolderArr, "?");
        String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
        where.append("WHERE voucher_no in ( " + placeHolders  + ")");
				//DataBaseUtil.addWhereFieldInList(where, "voucher_no", voucherNos);
			}
			//ps = con.prepareStatement(ALL_PAYMENT_VOUCHERS + where.toString() + " ORDER BY voucher_category");
			List<Object> values = new ArrayList<Object>();
			if (fromDate != null && toDate != null) {
				//ps.setTimestamp(1, fromDate);
				//ps.setTimestamp(2, toDate);
				//ps.setInt(3, accountGroup);
			  values.add(fromDate);
        values.add(toDate);
        values.add(accountGroup);
        if (centerId != 0) {
          //ps.setInt(4, centerId);
          values.add(centerId);
        } 
			} else {
			  /*Iterator it =  billNos.iterator();
        int i = 1;
        while (it.hasNext()) {
          ps.setString(i++, (String) ((Map) it.next()).get("voucher_no"));
        }*/
        values.addAll(voucherNos);
			}
			return DataBaseUtil.queryToDynaList(ALL_PAYMENT_VOUCHERS + where.toString() + 
			    " ORDER BY voucher_category", values.toArray());
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String SUPPLIER_TRANSFERS =
		" SELECT voucher_no, sum(pd.amount) AS amount, p.payee_name, pd.payment_type, sm.supplier_name, " +
		" 	voucher_category, date(date) as voucher_date, pd.account_group, gm.inter_comp_acc_name, gm.account_group_name " +
		" FROM payments p " +
		" 	JOIN payments_details pd USING (voucher_no) " +
		" 	JOIN supplier_master sm ON sm.supplier_code=p.payee_name " +
		"	JOIN account_group_master gm ON gm.account_group_id=pd.account_group " +
		"	JOIN counter_associated_accountgroup_view cav ON (p.counter=cav.counter_id " +
		"		AND COALESCE(cav.account_group_id,1) != pd.account_group) " +
		" WHERE pd.payment_type='S'  ";

	private static final String SUPPLIER_TRANSFERS_GROUPBY =
		" GROUP BY voucher_no, p.payee_name, pd.payment_type, account_group, sm.supplier_name, date, " +
		"	voucher_category, inter_comp_acc_name, gm.account_group_name ";

	public static List<BasicDynaBean> getSupAmtsReceivableFromAccGroups(Connection con, java.sql.Timestamp fromDate,
			java.sql.Timestamp toDate, Integer accountGroup, List voucherNos) throws SQLException {
		PreparedStatement ps = null;
		try {
			StringBuilder where = new StringBuilder();
			if (fromDate != null && toDate != null) {
				where.append(" AND mod_time BETWEEN ? AND ? AND coalesce(cav.account_group_id,1)=? ");
			} else {
				if (voucherNos == null || voucherNos.isEmpty()) {
				  return Collections.EMPTY_LIST;
				}
				String[] placeHolderArr = new String[voucherNos.size()];
        Arrays.fill(placeHolderArr, "?");
        String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
        where.append("AND voucher_no in ( " + placeHolders  + ")");
        
				//DataBaseUtil.addWhereFieldInList(where, "voucher_no", voucherNos, true);
			}
			//ps = con.prepareStatement(SUPPLIER_TRANSFERS + where.toString() + SUPPLIER_TRANSFERS_GROUPBY +
					//" ORDER BY voucher_no");
			List<Object> values = new ArrayList<Object>();		
			if (fromDate != null && toDate != null) {
			  //ps.setTimestamp(1, fromDate);
        //ps.setTimestamp(2, toDate);
        //ps.setInt(3, accountGroup);
        values.add(fromDate);
        values.add(toDate);
        values.add(accountGroup);
			} else {
			  /*Iterator it =  billNos.iterator();
        int i = 1;
        while (it.hasNext()) {
          ps.setString(i++, (String) ((Map) it.next()).get("voucher_no"));
        }*/
        values.addAll(voucherNos);
			}
			return DataBaseUtil.queryToDynaList(SUPPLIER_TRANSFERS + where.toString() + 
			      SUPPLIER_TRANSFERS_GROUPBY +" ORDER BY voucher_no" , values.toArray());
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String SUPPLIER_AMTS_FOR_AG =
		" SELECT voucher_no, sum(pd.amount) AS amount, p.payee_name, pd.payment_type, sm.supplier_name, " +
		" 	voucher_category, date(date) as voucher_date, account_group as counter_acc_grp_id, " +
		"	inter_comp_acc_name as counter_acc_grp_inter_co_acc_name, " +
		"	gm.account_group_name as counter_acc_grp_name" +
		" FROM payments p " +
		" 	JOIN payments_details pd USING (voucher_no) " +
		" 	JOIN supplier_master sm ON sm.supplier_code=p.payee_name " +
		"	JOIN counter_associated_accountgroup_view cav ON p.counter=cav.counter_id " +
		"	JOIN account_group_master gm ON (gm.account_group_id=coalesce(cav.account_group_id, 1) " +
		"		AND coalesce(cav.account_group_id, 1)!=pd.account_group) " +
		" WHERE pd.payment_type='S' " ;

	private static final String SUPPLIER_AMTS_FOR_AG_GROUP_BY =
		" GROUP BY voucher_no, p.payee_name, pd.payment_type, account_group, sm.supplier_name, date, " +
		"	voucher_category, inter_comp_acc_name, gm.account_group_name ";

	public static List<BasicDynaBean> getSupplierAmtPaidForAccountGroup(Connection con,
			java.sql.Timestamp fromDate, java.sql.Timestamp toDate, Integer accountGroup,
			List voucherNos) throws SQLException {
		PreparedStatement ps = null;
		try {
			StringBuilder where = new StringBuilder();
			if (fromDate != null && toDate != null) {
				where.append(" AND mod_time BETWEEN ? AND ? AND pd.account_group=? ");
			} else {
				if (voucherNos == null || voucherNos.isEmpty()) {
				  return Collections.EMPTY_LIST;
				}
				String[] placeHolderArr = new String[voucherNos.size()];
        Arrays.fill(placeHolderArr, "?");
        String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
        where.append("AND voucher_no in ( " + placeHolders  + ")");
				//DataBaseUtil.addWhereFieldInList(where, "voucher_no", voucherNos, true);
			}
			//ps = con.prepareStatement(SUPPLIER_AMTS_FOR_AG + where.toString() + SUPPLIER_AMTS_FOR_AG_GROUP_BY
				//	+ " ORDER BY voucher_no");
			List<Object> values = new ArrayList<Object>();   
			if (fromDate != null && toDate != null) {
				//ps.setTimestamp(1, fromDate);
        //ps.setTimestamp(2, toDate);
        //ps.setInt(3, accountGroup);
        values.add(fromDate);
        values.add(toDate);
        values.add(accountGroup);
			} else {
			  /*Iterator it =  billNos.iterator();
        int i = 1;
        while (it.hasNext()) {
          ps.setString(i++, (String) ((Map) it.next()).get("voucher_no"));
        }*/
        values.addAll(voucherNos);
			}
			return DataBaseUtil.queryToDynaList(SUPPLIER_AMTS_FOR_AG + where.toString() 
			    + SUPPLIER_AMTS_FOR_AG_GROUP_BY
          + " ORDER BY voucher_no", values.toArray());

		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public static final String GET_OH_CHARGE = "SELECT charge "+
		" FROM diag_outsource_detail dod "+
		" JOIN outsource_sample_details osd ON (osd.test_id = dod.test_id AND dod.outsource_dest_id = osd.outsource_dest_id) "+
		" JOIN bill_activity_charge bac on bac.activity_id=(prescribed_id:: character varying) "+
		" JOIN diag_outsource_master dom on (dom.outsource_dest_id = dod.outsource_dest_id) "+
		" and dom.status='A' WHERE charge_id= ? AND dod.source_center_id = ?";

	public static BigDecimal getOuthouseCharge(Connection con, String chargeId, int centerID)
			throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(GET_OH_CHARGE);) {
			ps.setString(1, chargeId);
			ps.setInt(2, centerID);
			return DataBaseUtil.getBigDecimalValueFromDb(ps);
		}
	}
	
	public static final String GET_OUTHOUSE_CHARGE = 
			"SELECT charge FROM diag_outsource_detail  WHERE source_center_id = ? AND outsource_dest_id = ? "
			+ "	AND test_id = ?";
	public static BigDecimal getOuthouseCharge(Connection con, int sourceCenterId, int outsourceDestId, 
			String testId) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(GET_OUTHOUSE_CHARGE);) {
			ps.setInt(1, sourceCenterId);
			ps.setInt(2,outsourceDestId);
			ps.setString(3, testId);
			return DataBaseUtil.getBigDecimalValueFromDb(ps);
		}
	}

	private static final String SERVICES_CONDUCTING_DOCTOR =
		" SELECT sp.service_id, sp.conductedby " +
		" FROM services_prescribed sp " +
		" WHERE prescription_id::varchar=? ";

	public static BasicDynaBean getServiceDoctorDetails(Connection con,
			String serviceId, String chargeId,String prescribedId) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(SERVICES_CONDUCTING_DOCTOR); ){
			ps.setString(1, prescribedId);
			List<BasicDynaBean> serviceList = DataBaseUtil.queryToDynaList(ps);
			if (serviceList.size() > 0)
				return serviceList.get(0);
			return null;
		}
	}

	private static final String UPDATE_ACTIVITY =
		" UPDATE bill_charge SET hasactivity='f',status=? WHERE charge_id=? ";

	public boolean updateActivity(String chargeId, String status) throws SQLException {
		boolean success = false;
		try(PreparedStatement ps = con.prepareStatement(UPDATE_ACTIVITY);){
  		ps.setString(1, status);
  		ps.setString(2, chargeId);
  		success = ps.executeUpdate() > 0;
		}
		return success;
	}

	/*
	 * Update the prescribing doctor to the new one. If updateRefs is true, then, also updates
	 * the referenced charges with the same prescribing doctor.
	 */
	public static final String UPDATE_PRESCRIBING_DR =
		" UPDATE bill_charge SET prescribing_dr_id=? WHERE charge_id=?";
	public static final String UPDATE_PRESCRIBING_DR_REFS =
		" UPDATE bill_charge SET prescribing_dr_id=? WHERE charge_ref=?";

	public void updatePrescribingDoctor(String chargeId, String docId, boolean updateRefs)
		throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(UPDATE_PRESCRIBING_DR);){
  		ps.setString(1, docId);
  		ps.setString(2, chargeId);
  		ps.executeUpdate();
		}
		if (updateRefs) {
		  try(PreparedStatement ps = con.prepareStatement(UPDATE_PRESCRIBING_DR_REFS);){
  			ps.setString(1, docId);
  			ps.setString(2, chargeId);
  			ps.executeUpdate();
		  }
		}
	}

	private static final String GET_CHARGEHEAD_INSURANCE_PAYABLE = "SELECT insurance_payable FROM chargehead_constants WHERE chargehead_id=? ";

	public String getChargeInsurancePayable(String chargeHead)
			throws SQLException {
		try(Connection con = DataBaseUtil.getConnection();
		    PreparedStatement ps = con.prepareStatement(GET_CHARGEHEAD_INSURANCE_PAYABLE);) {		
			  ps.setString(1, chargeHead);
			  try(ResultSet rs = ps.executeQuery();){
  			  if (rs.next()) {
  				return rs.getString("insurance_payable");
  			  } else
  				return null;
			  }
		}
	}

	public static final String TOTAL_BILL_NOW_BILLS = "SELECT COUNT(*) FROM ( SELECT b.*, COALESCE(pr.center_id,isr.center_id, prc.center_id) AS center_id " +
			"  FROM bill b " +
			"  LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id " +
			"  LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id " +
			"  LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = b.visit_id " +
			"  WHERE bill_type='P' AND b.status='A' AND (0=? OR account_group=?) ) AS foo WHERE (0=? OR center_id=?) ";

	static int getTotalBillNowBills(int accountGroup, int centerId) throws SQLException {
		try(Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(TOTAL_BILL_NOW_BILLS);){
			ps.setInt(1, accountGroup);
			ps.setInt(2, accountGroup);
			ps.setInt(3, centerId);
			ps.setInt(4, centerId);
			return DataBaseUtil.getIntValueFromDb(ps);
		}
	}

	public static final String TOTAL_BILL_LATER_BILLS = "SELECT COUNT(*) FROM ( SELECT b.*, COALESCE(pr.center_id,isr.center_id, prc.center_id) AS center_id " +
			" FROM  bill b " +
			"  LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id " +
			"  LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id " +
			"  LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = b.visit_id " +
		    " WHERE bill_type='C'AND b.status='A' AND (0=? OR account_group=?) ) AS foo WHERE (0=? OR center_id=?) ";

	static int getTotalBillLaterBills(int accountGroup, int centerId) throws SQLException {
		try(Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(TOTAL_BILL_LATER_BILLS);){
			ps.setInt(1, accountGroup);
			ps.setInt(2, accountGroup);
			ps.setInt(3, centerId);
			ps.setInt(4, centerId);
			return DataBaseUtil.getIntValueFromDb(ps);
		}
	}

	public static final String CANCELLED_BILLS = "SELECT COUNT(*) FROM ( SELECT b.*, COALESCE(pr.center_id,isr.center_id, prc.center_id) AS center_id " +
		" FROM bill b "+
		"  LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id " +
		"  LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id " +
		"  LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = b.visit_id " +
		" WHERE b.status='X' AND (b.mod_time::date BETWEEN ? AND ?) AND (0=? OR account_group=?)  ) AS foo WHERE  (0=? OR center_id=?)";

	static int getCancelledBillsCount(java.sql.Date from, java.sql.Date to, int accountGroup, int centerId)
		throws SQLException {
		try(Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(CANCELLED_BILLS);){
			ps.setDate(1, from);
			ps.setDate(2, to);
			ps.setInt(3, accountGroup);
			ps.setInt(4, accountGroup);
			ps.setInt(5, centerId);
			ps.setInt(6, centerId);
			return DataBaseUtil.getIntValueFromDb(ps);
		}
	}

	public static final String CLOSED_BILLS = "SELECT COUNT(*) FROM ( SELECT b.*, COALESCE(pr.center_id,isr.center_id, prc.center_id) AS center_id " +
		" FROM bill b " +
		"  LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id " +
		"  LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id " +
		"  LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = b.visit_id " +
		 " WHERE b.status='C' AND (closed_date::date BETWEEN ? AND ?) AND (0=? OR account_group=?)  ) AS foo WHERE (0=? OR center_id=?)";

	static int getClosedBillsCount(java.sql.Date from, java.sql.Date to, int accountGroup, int centerId)
		throws SQLException {
		try(Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(CLOSED_BILLS);){
			ps.setDate(1, from);
			ps.setDate(2, to);
			ps.setInt(3, accountGroup);
			ps.setInt(4, accountGroup);
			ps.setInt(5, centerId);
			ps.setInt(6, centerId);
			return DataBaseUtil.getIntValueFromDb(ps);
		}
	}

	/*
	 * Preferences for top N for each type of charge head
	 */
	public static final String GET_ALL_FROM_BILL_DASHBOARD_TOP_N = "SELECT charge_group, display_count FROM bill_dashboard_topN ";

	static List getAllFromBillDashboardTopN() throws SQLException {
		List l;
		try(Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(GET_ALL_FROM_BILL_DASHBOARD_TOP_N);
        ResultSet rs = ps.executeQuery();) {
			RowSetDynaClass rsd = new RowSetDynaClass(rs);
			l = rsd.getRows();
		}
		return l;
	}


	public static final String GET_CHARGE_REFERENCES = "SELECT * FROM BILL_CHARGE WHERE CHARGE_REF=?";

	public static List<Map> getChargeReferencesArrayList(Connection con, String chargeid)
			throws SQLException {
		List charge_ids = null;
		try (PreparedStatement ps = con.prepareStatement(GET_CHARGE_REFERENCES);){
			ps.setString(1, chargeid);
			charge_ids = DataBaseUtil.queryToArrayList(ps);
		} 
		return charge_ids;
	}

	public static final String GET_CHARGE_REFERENCE_IDS =
		"SELECT charge_id FROM bill_charge WHERE charge_ref=?";

	public static List<String> getChargeReferenceIds(Connection con, String chargeid) throws SQLException {
		List<String> chargeIds = null;
		try(PreparedStatement ps = con.prepareStatement(GET_CHARGE_REFERENCES);) {
			ps.setString(1, chargeid);
			chargeIds = DataBaseUtil.queryToStringList(ps);
		}
		return chargeIds;
	}

	public static final String GET_CHARGE_REFS = CHARGE_QUERY + " WHERE charge_ref=? ";

	public List getChargeReferences(String chargeId) throws SQLException {
		return getChargeList(GET_CHARGE_REFS, chargeId);
	}

	public static final String GET_CHARGE_AND_REFS = CHARGE_QUERY +
		" WHERE bill_charge.charge_id=? OR charge_ref=? AND bill_charge.status != 'X' " +
		" ORDER BY (charge_ref is null) desc, bill_charge.charge_id ";

	public List<ChargeDTO> getChargeAndRefs(String chargeId) throws SQLException {
		return getChargeList(GET_CHARGE_AND_REFS, chargeId, chargeId);
	}

	public static final String BILL_BED_CHARGES = "SELECT * FROM bill_charge WHERE bill_no = ? " +
			" AND (charge_group = ? OR charge_head = 'LTAX') AND act_description_id = ? ";

	public List<BasicDynaBean> getChargesUsingChargeGroupHeadActivityId(String originalBillNo,
				String chargeGrp, String activityId) throws SQLException {

		 return DataBaseUtil.queryToDynaList(BILL_BED_CHARGES,
				 new Object[] {originalBillNo, chargeGrp, activityId});
	}

	private static final String UPDATE_RATE_PLAN_CODE =
		"UPDATE bill_charge SET act_rate_plan_item_code=? WHERE charge_id=?";

	public void updateRatePlanCode(String chargeId, String code) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(UPDATE_RATE_PLAN_CODE);){
  		ps.setString(1, code);
  		ps.setString(2, chargeId);
  		ps.executeUpdate();
	  }
	}


	private static final String PACKAGES_FOR_BILL = "WITH RECURSIVE charge_closure AS ("
			+ " SELECT bc.charge_id package_charge_id,bc.*,"
			+ " pbccl.insurance_claim_amt as pri_claim_amt, "
			+ " sbccl.insurance_claim_amt as sec_claim_amt,"
			+ " pbccl.tax_amt as pri_tax_amt, sbccl.tax_amt as sec_tax_amt "
			+ " FROM bill_charge bc"
			+ " LEFT JOIN bill_claim pbcl ON(bc.bill_no = pbcl.bill_no and pbcl.priority = 1) "
			+ " LEFT JOIN bill_claim sbcl ON(bc.bill_no = sbcl.bill_no and sbcl.priority = 2) "
			+ " LEFT JOIN bill_charge_claim pbccl ON(bc.charge_id = pbccl.charge_id "
			+ " and pbcl.claim_id = pbccl.claim_id)"
			+ " LEFT JOIN bill_charge_claim sbccl ON(bc.charge_id = sbccl.charge_id "
			+ " and sbcl.claim_id = sbccl.claim_id) "
			+ " WHERE bc.bill_no = ? and bc.charge_head = 'PKGPKG' "
			+ " UNION "
			+ " SELECT cp.package_charge_id, bc_internal.*,"
			+ " pbccl.insurance_claim_amt as pri_claim_amt, "
			+ " sbccl.insurance_claim_amt as sec_claim_amt,"
			+ " pbccl.tax_amt as pri_tax_amt, sbccl.tax_amt as sec_tax_amt"
			+ " FROM "
			+ " bill_charge bc_internal"
			+ " LEFT JOIN bill_claim pbcl ON(bc_internal.bill_no = pbcl.bill_no "
			+ " and pbcl.priority = 1) "
			+ " LEFT JOIN bill_claim sbcl ON(bc_internal.bill_no = sbcl.bill_no "
			+ " and sbcl.priority = 2) "
			+ " LEFT JOIN bill_charge_claim pbccl ON(bc_internal.charge_id = pbccl.charge_id "
			+ " and pbcl.claim_id = pbccl.claim_id) "
			+ " LEFT JOIN bill_charge_claim sbccl ON(bc_internal.charge_id = sbccl.charge_id "
			+ " and sbcl.claim_id = sbccl.claim_id) "
			+ " INNER JOIN charge_closure cp ON cp.charge_id = bc_internal.charge_ref"
			+ " ) select * from charge_closure";
			
	
	/*
	 * This is the common charge query that is used by multiple getXxx methods
	 * after adding one or more WHERE clauses.
	 */
	private static final String CHARGE_QUERY_PRINT =
		  " SELECT coalesce(bill_charge.insurance_claim_amount,0) as insurance_claim_amount, "
		+ "  bill_charge.charge_id, bill_charge.bill_no, bill_charge.charge_group, bill_charge.charge_head, act_department_id, COALESCE(act_description,'') as act_description, "
		+ "  act_remarks, coalesce(cpc.act_rate, bill_charge.act_rate) act_rate, act_unit, coalesce(cpc.act_quantity, bill_charge.act_quantity) act_quantity, (coalesce(cpc.amount, bill_charge.amount)+coalesce(cpc.tax_amt, bill_charge.tax_amt)) as amount, coalesce(cpc.discount, bill_charge.discount) discount, discount_reason, "
		+ "  charge_ref, coalesce(cpc.paid_amount, bill_charge.paid_amount), posted_date, bill_charge.status, bill_charge.username, bill_charge.mod_time, bill_charge.approval_id, coalesce(cpc.orig_rate, bill_charge.orig_rate), "
		+ "  package_unit, coalesce(cpc.doctor_amount, bill_charge.doctor_amount), doc_payment_id, ref_payment_id, oh_payment_id, act_description_id, "
		+ "  hasactivity, coalesce(cpc.insurance_claim_amount, bill_charge.insurance_claim_amount), return_qty, coalesce(cpc.return_insurance_claim_amt,bill_charge.return_insurance_claim_amt), coalesce(cpc.return_amt, bill_charge.return_amt), "
		+ "  payee_doctor_id, coalesce(cpc.referal_amount, bill_charge.referal_amount), coalesce(cpc.referal_amount, bill_charge.out_house_amount), "
		+ "  prescribing_dr_id, coalesce(cpc.prescribing_dr_amount, bill_charge.prescribing_dr_amount), prescribing_dr_payment_id, overall_discount_auth, "
		+ "  coalesce(cpc.overall_discount_amt, bill_charge.overall_discount_amt), discount_auth_dr, coalesce(cpc.dr_discount_amt, bill_charge.dr_discount_amt), discount_auth_pres_dr, coalesce(cpc.pres_dr_discount_amt, bill_charge.pres_dr_discount_amt),"
		+ "  discount_auth_ref, coalesce(cpc.ref_discount_amt, bill_charge.ref_discount_amt), discount_auth_hosp, coalesce(cpc.hosp_discount_amt, bill_charge.hosp_discount_amt), activity_conducted,"
		+ "  bill_charge.account_group, act_item_code, act_rate_plan_item_code, bill_charge.code_type, allow_discount, order_number,"
		+ "  chargegroup_name, chargehead_name, dept_name, prd.doctor_name as prescribing_dr_name, "
		+ "  dac.disc_auth_name AS discount_auth_dr_name, dap.disc_auth_name AS discount_auth_pres_dr_name,"
		+ "  dar.disc_auth_name AS discount_auth_ref_name, dah.disc_auth_name AS discount_auth_hosp_name,"
		+ "  daov.disc_auth_name AS overall_discount_auth_name, insurance_payable, "
		+ "  claim_service_tax_applicable,bill_charge.service_sub_group_id,ss.service_group_id,"
		+ "  ss.service_sub_group_name,sg.service_group_name, conducting_doc_mandatory, "
		+ "  CASE WHEN (bill_charge.charge_head = 'MARPKG') THEN 'N' "
		+ "  WHEN bill.dyna_package_id != 0  AND bill_charge.charge_head in('INVITE','INVRET') AND (coalesce(bill_charge.amount_included,0) = 0) "
		+ "  AND (coalesce(bill_charge.qty_included,0) = 0) AND bill_charge.dyna_package_excluded IS NOT NULL THEN bill_charge.dyna_package_excluded "
		+ "  	  WHEN (bill.dyna_package_id = 0 OR (coalesce(bill_charge.qty_included,0) = coalesce(bill_charge.act_quantity,0))) THEN 'N' "
		+ "       WHEN (coalesce(bill_charge.amount_included,0) = 0) AND (coalesce(bill_charge.qty_included,0) = 0 AND (coalesce(bill_charge.amount,0) = 0 )) THEN 'Y' "
		+ "		  WHEN ((coalesce(bill_charge.amount_included,0) = coalesce(bill_charge.amount+bill_charge.tax_amt,0)) AND (coalesce(bill_charge.qty_included,0) = 0)) THEN 'N' "
		+ "  	  WHEN (coalesce(bill_charge.amount_included,0) = 0) AND (coalesce(bill_charge.qty_included,0) = 0) THEN 'Y' ELSE 'P' END AS charge_excluded, "
		+ "  bill_charge.amount_included, qty_included, package_finalized, "
		+ "  bill_charge.consultation_type_id, consultation_type, user_remarks, "
		+ "  bill_charge.insurance_category_id,bill_charge.prior_auth_id, bill_charge.prior_auth_mode_id, bill_charge.first_of_category, "
		+ "  op_id, bill_charge.from_date, bill_charge.to_date, tdv.dept_name, item_remarks, "
		+ "  bill_charge.allow_rate_increase, bill_charge.allow_rate_decrease, "
		+ "  bill_charge.claim_status, coalesce(cpc.claim_recd_total, bill_charge.claim_recd_total), bill_charge.redeemed_points, "
		+ "  ss.eligible_to_redeem_points, ss.redemption_cap_percent, service_charge_applicable,"
		+ "  coalesce(cpc.orig_insurance_claim_amount, bill_charge.orig_insurance_claim_amount), bill.visit_id, bill.visit_type, is_tpa, "
		+ "  coalesce(cpc.pri_claim_amt,pbccl.insurance_claim_amt) as pri_claim_amt, "
		+ "  coalesce(cpc.sec_claim_amt,sbccl.insurance_claim_amt) as sec_claim_amt, "
		+ "	 coalesce(cpc.pri_tax_amt,pbccl.tax_amt) as pri_tax_amt, coalesce(cpc.sec_tax_amt,sbccl.tax_amt) as sec_tax_amt, "
		+ "	 pbccl.prior_auth_id as pri_prior_auth_id, sbccl.prior_auth_id as sec_prior_auth_id, "
		+ "  pbccl.prior_auth_mode_id as pri_prior_auth_mode, sbccl.prior_auth_mode_id as sec_prior_auth_mode, "
		+ "  pbcl.plan_id as pri_plan_id, sbcl.plan_id as sec_plan_id, is_claim_locked, pbccl.include_in_claim_calc as pri_include_in_claim, "
		+ "	 sbccl.include_in_claim_calc as sec_include_in_claim, coalesce(cpc.tax_amt,bill_charge.tax_amt) AS tax_amt, "
		+ "  coalesce(cpc.sponsor_tax_amt,bill_charge.sponsor_tax_amt) AS sponsor_tax_amt, "
		+ "  coalesce(cpc.return_tax_amt,bill_charge.return_tax_amt) AS return_tax_amt,"
		+ "  coalesce(cpc.original_tax_amt,bill_charge.original_tax_amt) AS original_tax_amt,"
		+ "  bill_charge.billing_group_id, ig.item_group_name as billing_group_name,bill.bill_rate_plan_id "
		+ "  FROM bill_charge  "
		+ "  LEFT JOIN bill_claim pbcl ON(bill_charge.bill_no = pbcl.bill_no and pbcl.priority = 1) "
		+ "  LEFT JOIN bill_claim sbcl ON(bill_charge.bill_no = sbcl.bill_no and sbcl.priority = 2) "
		+ "  LEFT JOIN bill_charge_claim pbccl ON(bill_charge.charge_id = pbccl.charge_id and pbcl.claim_id = pbccl.claim_id) "
		+ "  LEFT JOIN bill_charge_claim sbccl ON(bill_charge.charge_id = sbccl.charge_id and sbcl.claim_id = sbccl.claim_id) "
		+ "	 JOIN bill ON (bill.bill_no = bill_charge.bill_no) "
		+ "  LEFT JOIN service_sub_groups ss using(service_sub_group_id) "
		+ "  LEFT JOIN service_groups sg using(service_group_id) "
		+ "  LEFT OUTER JOIN treating_departments_view tdv ON (bill_charge.act_department_id=tdv.dept_id) "
		+ "  JOIN chargehead_constants ON (bill_charge.charge_head = chargehead_constants.chargehead_id) "
		+ "  JOIN chargegroup_constants ON (bill_charge.charge_group = chargegroup_constants.chargegroup_id)"
		+ "  LEFT OUTER JOIN doctors prd ON (prd.doctor_id = bill_charge.prescribing_dr_id) "
		+ "  LEFT OUTER JOIN discount_authorizer dac ON (bill_charge.discount_auth_dr=dac.disc_auth_id)"
		+ "  LEFT OUTER JOIN discount_authorizer dap ON (bill_charge.discount_auth_pres_dr=dap.disc_auth_id)"
		+ "  LEFT OUTER JOIN discount_authorizer dar ON (bill_charge.discount_auth_ref = dar.disc_auth_id)"
		+ "  LEFT OUTER JOIN discount_authorizer dah ON (bill_charge.discount_auth_hosp = dah.disc_auth_id)"
		+ "  LEFT OUTER JOIN discount_authorizer daov ON (bill_charge.overall_discount_auth=daov.disc_auth_id)"
		+ "  LEFT OUTER JOIN consultation_types ct ON (bill_charge.consultation_type_id = ct.consultation_type_id) "
		+ "  LEFT JOIN item_groups ig ON ig.item_group_id = bill_charge.billing_group_id  and item_group_type_id='BILLGRP' "
	  	+ "  LEFT JOIN ( "
				  + "select"
				  + "		package_charge_id, "
				  + "       sum(amount + discount)  act_rate,"
				  + "       1 as act_quantity,"
				  + "       sum(amount) amount,"
				  + "       sum(discount) discount,"
				  + "       sum(paid_amount) paid_amount,"
				  + "       sum(orig_rate * act_quantity) orig_rate,"
				  + "       sum(doctor_amount) doctor_amount,"
				  + "       sum(insurance_claim_amount) insurance_claim_amount,"
				  + "       sum(referal_amount) referal_amount,"
				  + "       sum(out_house_amount) out_house_amount,"
				  + "       sum(prescribing_dr_amount) prescribing_dr_amount,"
				  + "       sum(overall_discount_amt) overall_discount_amt,"
				  + "       sum(dr_discount_amt) dr_discount_amt,"
				  + "       sum(pres_dr_discount_amt) pres_dr_discount_amt,"
				  + "       sum(ref_discount_amt) ref_discount_amt,"
				  + "       sum(hosp_discount_amt) hosp_discount_amt,"
				  + "       sum(claim_recd_total) claim_recd_total,"
				  + "        sum(return_insurance_claim_amt) return_insurance_claim_amt,"
				  + "       sum(return_amt) return_amt,"
				  + "        sum(amount_included) amount_included,"
				  + "       sum(orig_insurance_claim_amount) orig_insurance_claim_amount,"
				  + "       sum(tax_amt) tax_amt,"
				  + "       sum(sponsor_tax_amt) sponsor_tax_amt,"
				  + "       sum(return_tax_amt) return_tax_amt,"
				  + "       sum(original_tax_amt) original_tax_amt,"
				  + "       sum(return_original_tax_amt) return_original_tax_amt,"
				  + "       sum(pri_claim_amt) AS pri_claim_amt,"
				  + "       sum(sec_claim_amt) AS sec_claim_amt,"
				  + "       sum(pri_tax_amt) AS pri_tax_amt,"
				  + "       sum(sec_tax_amt) AS sec_tax_amt"
				  + " from ("
				  + 	PACKAGES_FOR_BILL
				  + ") pcc group by package_charge_id"
				  + ") as  cpc ON bill_charge.charge_head = 'PKGPKG' AND cpc.package_charge_id = bill_charge.charge_id" ;

	public static final String GET_BILL_PRINT_CHARGES = CHARGE_QUERY_PRINT
			+ " WHERE bill_charge.bill_no=? AND bill_charge.status != 'X' AND (bill_charge.act_rate !=0  OR bill_charge.discount !=0 OR "
			+ " bill_charge.charge_head = 'PKGPKG'"
			+ ") "
			+ " AND ((bill_charge.charge_group = 'PKG' AND (bill_charge.charge_head ='PKGPKG' OR bill_charge.charge_head = 'MARPKG')) OR (bill_charge.charge_group != 'PKG')  )"
			+ " ORDER BY chargegroup_constants.display_order, chargehead_constants.display_order, "
			+ " bill_charge.posted_date  ";

	public static List getPrintChargeDetailsBean(String billNo) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_BILL_PRINT_CHARGES, billNo, billNo);
	}

	public static List<BasicDynaBean> getPackageCharges(String billNo) throws SQLException {
		return DataBaseUtil.queryToDynaList(PACKAGE_BILL_CHARGES, billNo);
	}

	public static final String GET_BILL_STATUS = "SELECT b.status " +
		" FROM bill_charge bc " +
		"  JOIN bill b USING (bill_no) " +
		" WHERE bc.charge_id=?";

	public final String getBillStatus(String chargeId) throws SQLException {
		String status = null;
		try(PreparedStatement ps = con.prepareStatement(GET_BILL_STATUS);) {
  		ps.setString(1, chargeId);	
  		try(ResultSet rs = ps.executeQuery();){
  		  if (rs.next())
  		    return rs.getString(1);
  		}
  		return status;
		}
	}

	public static final String PKG_COMPONENTS_DETAILS = "SELECT pc.package_content_id as pack_ob_id, "+
			" p.package_id::text as package_id, activity_id, " +
			" charge_head, (CASE WHEN strpos(charge_head,'asd') > 0 THEN 'Doctor' ELSE "+
			" coalesce(test.test_name,s.service_name, doc.doctor_name, d.dept_name, "+
			" em.equipment_name,om.operation_name,"+
			"sid.medicine_name, oi.item_name) END) AS activity_description, " +
			" activity_remarks, pc.activity_qty_uom as activity_units, activity_qty, activity_type, " +
			" pcc.charge AS activity_charge, package_name, type, p.status, description, type," +
			" allow_discount, operation_id, p.service_sub_group_id "+
			" FROM package_contents pc JOIN packages p ON p.package_id = pc.package_id"+
			" LEFT JOIN doctors doc ON (pc.doctor_id = doc.doctor_id AND pc.activity_type = 'Doctor') "+
			" LEFT JOIN services s ON (pc.activity_id=s.service_id AND pc.activity_type = 'Service') "+
			" LEFT JOIN diagnostics test ON (test.test_id=pc.activity_id AND "+
			" (pc.activity_type = 'Laboratory' OR pc.activity_type = 'Radiology')) "+
			" LEFT JOIN equipment_master em ON (em.eq_id = pc.activity_id AND "+
			" pc.activity_type = 'Equipment') "+ 
			" LEFT JOIN department d ON (pc.dept_id = d.dept_id AND pc.activity_type = 'Department')"+
			"  LEFT JOIN operation_master om ON (om.op_id = pc.activity_id AND pc.activity_type ="+
		    " 'Operation') LEFT JOIN store_item_details sid ON ("+
		    " sid.medicine_id::character varying = pc.activity_id "+
		    "  AND pc.activity_type ='Inventory')" +
		    " LEFT JOIN orderable_item oi ON (oi.entity_id =pc.activity_id AND oi.entity=pc.activity_type)"+
			" JOIN package_content_charges pcc ON pcc.package_content_id = pc.package_content_id ";

	public static List<BasicDynaBean> getPackageComponentsList(List<Integer> pkgIdList, 
			List<String> ratePlanList, List<String> bedTypeList) throws SQLException {
		Connection con  = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		List pkgCompList = null;
		
		if ( pkgIdList == null || pkgIdList.size() == 0 ){
		  return new ArrayList<BasicDynaBean>();//return empty list
		}
		try{

			StringBuilder where = new StringBuilder();
			//DataBaseUtil.addWhereFieldInList(where, "package_id", pkgIdList);
			String[] placeHolderArr = new String[pkgIdList.size()];
			String[] ratePlanplaceHolderArr = new String[ratePlanList.size()];
			String[] bedTypeplaceHolderArr = new String[bedTypeList.size()];
      Arrays.fill(placeHolderArr, "?");
      Arrays.fill(ratePlanplaceHolderArr, "?");
      Arrays.fill(bedTypeplaceHolderArr, "?");
      List<Object> otherParams = new ArrayList<Object>();
      String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
      String ratePlanPlaceHolders = StringUtils.arrayToCommaDelimitedString(ratePlanplaceHolderArr);
      String bedTypePlaceHolders = StringUtils.arrayToCommaDelimitedString(bedTypeplaceHolderArr);
      where.append("WHERE p.package_id in ( " + placeHolders  + ")");
      where.append("AND pcc.org_id in ( " + ratePlanPlaceHolders  + ")");
      where.append("AND pcc.bed_type in ( " + bedTypePlaceHolders  + ")");
      otherParams.addAll(pkgIdList);
      otherParams.addAll(ratePlanList);
      otherParams.addAll(bedTypeList);

			StringBuilder query = new StringBuilder(PKG_COMPONENTS_DETAILS);
			query.append(where);

			/*ps = con.prepareStatement(query.toString());

			int index = 1;
			for(int i=0;i<pkgIdList.size();i++,index++){
				  ps.setInt(index, new Integer((String)pkgIdList.get(i)));
			}
			pkgCompList = DataBaseUtil.queryToDynaList(ps);
      */
			pkgCompList = DataBaseUtil.queryToDynaList(query.toString(), otherParams.toArray());

		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return pkgCompList;
	}

	private static final String UPDATE_PACKAGE_MARGIN_CHARGE = "UPDATE bill_charge SET act_description=?,"
			+ " act_description_id=?,insurance_claim_amount = ?  " +
			" WHERE charge_id=? ";

	public boolean updatePackageMarginCharge(List updateBillChargeList) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(UPDATE_PACKAGE_MARGIN_CHARGE);){
  		boolean success = true;
  		Iterator iterator = updateBillChargeList.iterator();
  		while (iterator.hasNext()) {
  			ChargeDTO chargeDTO = (ChargeDTO) iterator.next();
  			String chargeHead = chargeDTO.getChargeHead();
  			String cId = chargeDTO.getChargeId();
  			if (chargeHead.equals("MARPKG")) {
  				ps.setString(1, chargeDTO.getActDescription());
  				ps.setString(2, chargeDTO.getActDescriptionId());
  				ps.setBigDecimal(3, chargeDTO.getInsuranceClaimAmount());
  				ps.setString(4, cId);
  
  				success = ps.executeUpdate() > 0;
  				break;
  			}
  		}
  		return success;
		}
	}

	private static final String UPDATE_DR_CONSULTATION_ID= "UPDATE bill_charge SET consultation_type_id=?, charge_head=?, code_type=?, allow_zero_claim=? " +
	" WHERE charge_id=? ";

	private static final String UPDATE_ACTIVITY_DR_CHARGE_HEAD= "UPDATE bill_activity_charge SET payment_charge_head=? " +
	" WHERE charge_id=? ";

	public boolean updateChargeConsultationIdAndHead(List updateBillChargeList, String codeType,boolean allowZeroClaim) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(UPDATE_DR_CONSULTATION_ID);){
  		boolean success = true;
  		Iterator iterator = updateBillChargeList.iterator();
  		while (iterator.hasNext()) {
  			ChargeDTO chargeDTO = (ChargeDTO) iterator.next();
  			String chargeHead = chargeDTO.getChargeHead();
  			String cId = chargeDTO.getChargeId();
  			ps.setInt(1, chargeDTO.getConsultation_type_id());
  			ps.setString(2, chargeHead);
  			ps.setString(3, codeType);
  			ps.setBoolean(4, allowZeroClaim);
  			ps.setString(5, cId);
  			ps.executeUpdate();
  			try(PreparedStatement ps1 = con.prepareStatement(UPDATE_ACTIVITY_DR_CHARGE_HEAD);){
  			  ps1.setString(1, chargeHead);
  			  ps1.setString(2, cId);
  			  success = ps1.executeUpdate() > 0;
  			}
  		}
  		return success;
		}
	}

	private static final String BED_CHARGES =
		"  SELECT *  FROM bill_charge bc  " +
		"  JOIN bill_activity_charge bac ON (bac.charge_id = bc.charge_id AND bac.activity_code = 'BED')" +
		"  JOIN bill b on(bc.bill_no = b.bill_no)  " +
		"  JOIN ip_bed_details ipb ON (ipb.admit_id::text = bac.activity_id  or ipb.ref_admit_id::text = bac.activity_id ) " +
		"  WHERE ipb.bed_state = 'O' AND bc.act_unit = 'Days' AND bc.status = 'A' AND b.status = 'A'";

	public List<BasicDynaBean > getUpdateBedCharges()throws SQLException{
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(BED_CHARGES);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String UPDATE_ACTIVITY_DETAILS =
		" UPDATE bill_charge set activity_conducted = ? " +
		" WHERE charge_id = ? ";

	/*
	 * update activity conducted status of passed ChargeDTO
	 */
	public boolean updateActivityDetails( ChargeDTO chargeDTO ) throws SQLException{
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(UPDATE_ACTIVITY_DETAILS);
			ps.setString(1, chargeDTO.getActivityConducted());
			ps.setString(2, chargeDTO.getChargeId());

			return (ps.executeUpdate() > 0 ) ;
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String GET_DRG_CODE =
		" SELECT b.bill_no AS drg_bill_no," +
		" bc.charge_id AS drg_charge_id," +
		" bc.act_rate_plan_item_code AS drg_code, " +
		" COALESCE((SELECT code_desc from getItemCodesForCodeType('IR-DRG', b.visit_type) mcm " +
		" WHERE (bc.act_rate_plan_item_code::text = mcm.code)  LIMIT 1), '') AS drg_description " +
		" FROM bill b " +
		" LEFT JOIN bill_charge bc ON (bc.charge_head = 'BPDRG' AND bc.status != 'X' AND bc.bill_no = b.bill_no) " +
		" WHERE b.bill_type = 'C' AND b.is_primary_bill = 'Y' AND b.status != 'X' AND b.is_tpa AND b.bill_no = ?  " ;

	public static Map getBillDRGCode(Connection con, String billNo) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_DRG_CODE);
			ps.setString(1, billNo);
			List l = DataBaseUtil.queryToDynaList(ps);
			if ( l != null && l.size() > 0 )
				return ((BasicDynaBean)l.get(0)).getMap();
			else
				return null;
		}finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public static Map getBillDRGCode(String billNo) throws SQLException {
		Connection con = null;
    	try {
    		con = DataBaseUtil.getReadOnlyConnection();
    		return getBillDRGCode(con, billNo);
    	}finally {
    		DataBaseUtil.closeConnections(con, null);
    	}
	}
	
	// Below method is used for migrated finalized DRG bills
	
	private static final String GET_DRG_MARGIN_CODE =
			" SELECT b.bill_no AS drg_bill_no," +
			" bc.charge_id AS drg_charge_id," +
			" bc.act_rate_plan_item_code AS drg_code, " +
			" COALESCE((SELECT code_desc from getItemCodesForCodeType('IR-DRG', b.visit_type) mcm " +
			" WHERE (bc.act_rate_plan_item_code::text = mcm.code)  LIMIT 1), '') AS drg_description " +
			" FROM bill b " +
			" LEFT JOIN bill_charge bc ON (bc.charge_head = 'MARDRG' AND bc.status != 'X' AND bc.bill_no = b.bill_no) " +
			" WHERE b.bill_type = 'C' AND b.is_primary_bill = 'Y' AND b.status != 'X' AND b.is_tpa AND b.bill_no = ?  " ;

	public static Map getBillDRGMarginCode(Connection con, String billNo) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_DRG_MARGIN_CODE);
			ps.setString(1, billNo);
			List l = DataBaseUtil.queryToDynaList(ps);
			if ( l != null && l.size() > 0 )
				return ((BasicDynaBean)l.get(0)).getMap();
			else
				return null;
		}finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}
	
	public static Map getBillDRGMarginCode(String billNo) throws SQLException {
		Connection con = null;
    	try {
    		con = DataBaseUtil.getReadOnlyConnection();
    		return getBillDRGMarginCode(con, billNo);
    	}finally {
    		DataBaseUtil.closeConnections(con, null);
    	}
	}
	

	private static final String GET_PERDIEM_CODE =
		" SELECT b.bill_no AS perdiem_bill_no," +
		" bc.charge_id AS perdiem_charge_id," +
		" bc.act_rate_plan_item_code AS per_diem_code " +
		" FROM bill b " +
		" LEFT JOIN bill_charge bc ON (bc.charge_head = 'MARPDM' " +
		"			AND bc.bill_no = b.bill_no) " +
		" WHERE b.bill_no = ?  " ;

	public static Map getBillPerdiemCharge(Connection con, String billNo) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_PERDIEM_CODE);
			ps.setString(1, billNo);
			List l = DataBaseUtil.queryToDynaList(ps);
			if ( l != null && l.size() > 0 )
				return ((BasicDynaBean)l.get(0)).getMap();
			else
				return null;
		}finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public static Map getBillPerdiemCharge(String billNo) throws SQLException {
		Connection con = null;
    	try {
    		con = DataBaseUtil.getReadOnlyConnection();
    		return getBillPerdiemCharge(con, billNo);
    	}finally {
    		DataBaseUtil.closeConnections(con, null);
    	}
	}

	public static final String CHARGE_HEAD_CHARGE = "SELECT charge_id FROM bill_charge " +
		" WHERE bill_no =? AND charge_head = ? ";

	public static String getChargeUsingChargeHead(Connection con ,String billNo, String chargeHead) throws SQLException {
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(CHARGE_HEAD_CHARGE);
			ps.setString(1, billNo);
			ps.setString(2, chargeHead);
			List<BasicDynaBean> chList = DataBaseUtil.queryToDynaList(ps);
			if (chList != null && chList.size() > 0) {
				return (String)((BasicDynaBean)chList.get(0)).get("charge_id");
			} else {
			return null;
			}
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String UPDATE_CHARGE_REF =
		"UPDATE bill_charge set charge_ref = ?,status = 'A' WHERE charge_id = ? ";

	public boolean updateChargeRef(ChargeDTO chargeDto)
	throws SQLException{
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(UPDATE_CHARGE_REF);
			ps.setString(1, chargeDto.getChargeRef());
			ps.setString(2, chargeDto.getChargeId());

			return (ps.executeUpdate() > 0 ) ;
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}

	}

	public static String getAccountHead(String chargeHeadId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT account_head_name FROM bill_account_heads bah JOIN chargehead_constants cc " +
					" ON (cc.account_head_id=bah.account_head_id) WHERE chargehead_id=?");
			ps.setString(1, chargeHeadId);
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getString("account_head_name");
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return "";
	}

	private static final String BILL_CHARGE_EXCLUDED = "SELECT * FROM bill_charge " +
	" WHERE bill_no =? AND status != 'X' AND charge_excluded = 'Y' ";

	public static boolean isPkgProcessedOldWay(String billNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(BILL_CHARGE_EXCLUDED);
			ps.setString(1, billNo);
			List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(ps);
			if (list != null && list.size() > 0)
				return true;
			return false;
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public static boolean isPkgProcessedOldWay(Connection con, String billNo) throws SQLException {
    PreparedStatement ps = null;
    try{
      ps = con.prepareStatement(BILL_CHARGE_EXCLUDED);
      ps.setString(1, billNo);
      List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(ps);
      if (list != null && list.size() > 0)
        return true;
      return false;
    }finally{
      DataBaseUtil.closeConnections(null, ps);
    }
  }
	
	public static final String CANCEL_CHARGE_UPDATE_AUDIT_LOG = "UPDATE bill_charge " +
			" SET status='X', discount=0, amount=0, insurance_claim_amount=0, tax_amt=0, sponsor_tax_amt=0," +
			"  act_remarks = replace(act_remarks, 'Occupied', 'Cancelled'), username = ?, mod_time = NOW() " +
			" WHERE charge_id=?";

		public static final String CANCEL_CHARGE_REFS_UPDATE_AUDIT_LOG = "UPDATE bill_charge " +
			" SET status='X', discount=0, amount=0, insurance_claim_amount=0, tax_amt=0, " +
			"  act_remarks = replace(act_remarks, 'Occupied', 'Cancelled'), username = ?, mod_time = NOW() " +
			" WHERE charge_ref=?";

		/**
		 * This method will change the status of a charge to 'X' which means
		 * cancelled charge. If cancelRefs is true, it will also cancel charges whose charge_ref is
		 * this charge. This is mainly called when cancelling an order.
		 */
		public static boolean cancelChargeUpdateAuditLog(Connection con, String chargeid, boolean cancelRefs, String username)
				throws SQLException {
			try(PreparedStatement ps = con.prepareStatement(CANCEL_CHARGE_UPDATE_AUDIT_LOG);){
  			ps.setString(1,username);
  			ps.setString(2, chargeid);
  			int result = ps.executeUpdate();
  			if (result == 0)
  				return false;
			}
			if (cancelRefs) {
				try(PreparedStatement ps = con.prepareStatement(CANCEL_CHARGE_REFS_UPDATE_AUDIT_LOG);){
  				ps.setString(1,username);
  				ps.setString(2, chargeid);
  				ps.executeUpdate();
				}
			}
			return true;
		}
		
		private void setZeroClaimAmountConfig(ChargeDTO chargeDTO) throws SQLException{
			String chargeGroup=chargeDTO.getChargeGroup();
			String allowZeroClaimfor = "";
			String actId= chargeDTO.getActDescriptionId();
			BasicDynaBean result = null;
			
			if (chargeGroup.endsWith("OPE")) {
				String chrHead= chargeDTO.getChargeHead();
				if("TCOPE".equalsIgnoreCase(chrHead) || "TCAOPE".equalsIgnoreCase(chrHead) ){
					//Theater Charge
					if(null != actId){
						result = new GenericDAO("theatre_master").findByKey("theatre_id", actId);
						if(null != result){
							allowZeroClaimfor=(String)result.get("allow_zero_claim_amount");
							if(chargeDTO.getVisitType().equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor)){
								chargeDTO.setAllowZeroClaim(true);
							}
						}
					}
				}else if("ANATOPE".equalsIgnoreCase(chrHead)){
					//Anesthesia type Charge
					if(null != actId){
						result = new GenericDAO("anesthesia_type_master").findByKey("anesthesia_type_id", actId);
						if(null != result){
							allowZeroClaimfor=(String)result.get("allow_zero_claim_amount");
							if(chargeDTO.getVisitType().equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor)){
								chargeDTO.setAllowZeroClaim(true);
							}
						}
					}
				}else if("ANAOPE".equalsIgnoreCase(chrHead) || "SUOPE".equalsIgnoreCase(chrHead) || "SACOPE".equalsIgnoreCase(chrHead)){
					String opId = chargeDTO.getOp_id();
					//itemBean = new GenericDAO("operation_master").findByKey("op_id", chargeBean.get("act_description_id"));
					if(null != opId){
						result = new GenericDAO("operation_master").findByKey("op_id", opId);
						if(null != result){
							allowZeroClaimfor=(String)result.get("allow_zero_claim_amount");
							if(chargeDTO.getVisitType().equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor)){
								chargeDTO.setAllowZeroClaim(true);
							}
						}
					}
				}
			}else if (chargeGroup.endsWith("DIA")) {
				String test_id = chargeDTO.getActDescriptionId();
				//itemBean = new GenericDAO("diagnostics").findByKey("test_id", chargeBean.get("act_description_id"));
				if(null != test_id){
					result = new GenericDAO("diagnostics").findByKey("test_id", test_id);
					if(null != result){
						allowZeroClaimfor=(String)result.get("allow_zero_claim_amount");
						if(chargeDTO.getVisitType().equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor)){
							chargeDTO.setAllowZeroClaim(true);
						}
					}
				}
			}else if (chargeGroup.endsWith("SNP")) {
				String service_id = chargeDTO.getActDescriptionId();
				//itemBean = new GenericDAO("services").findByKey("service_id", chargeBean.get("act_description_id"));
				if(null != service_id){
					result = new GenericDAO("services").findByKey("service_id", service_id);
					if(null != result){
						allowZeroClaimfor=(String)result.get("allow_zero_claim_amount");
						if(chargeDTO.getVisitType().equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor)){
							chargeDTO.setAllowZeroClaim(true);
						}
					}
				}
			}else if (chargeGroup.endsWith("BED")) {
				//String service_id = chargeDTO.getActDescriptionId();
				//itemBean = new GenericDAO("services").findByKey("service_id", chargeBean.get("act_description_id"));
				String bed_type = chargeDTO.getBedType();
				if(null != bed_type){
					result = new GenericDAO("bed_types").findByKey("bed_type_name", bed_type);
					if(null != result){
						allowZeroClaimfor=(String)result.get("allow_zero_claim_amount");
						if(chargeDTO.getVisitType().equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor)){
							chargeDTO.setAllowZeroClaim(true);
						}
					}
				}
			}else if (chargeGroup.endsWith("DOC")) {
				//String service_id = chargeDTO.getActDescriptionId();
				//itemBean = new GenericDAO("services").findByKey("service_id", chargeBean.get("act_description_id"));
				int conTypeId = chargeDTO.getConsultation_type_id();
				if(0 != conTypeId){
					result = new GenericDAO("consultation_types").findByKey("consultation_type_id", conTypeId);
					if(null != result){
						allowZeroClaimfor=(String)result.get("allow_zero_claim_amount");
						if(chargeDTO.getVisitType().equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor)){
							chargeDTO.setAllowZeroClaim(true);
						}
					}
				}
			}
		}

  private static final String UPDATE_CHARGE_TAX = "UPDATE bill_charge SET tax_amt = ?, original_tax_amt = ? WHERE charge_id = ?";

  public boolean updateChargeTax(ChargeDTO charge) throws SQLException {

    try(PreparedStatement statement = con.prepareStatement(UPDATE_CHARGE_TAX);){
      statement.setBigDecimal(1, charge.getTaxAmt());
      statement.setBigDecimal(2, charge.getOriginalTaxAmt());
      statement.setString(3, charge.getChargeId());
  
      return statement.executeUpdate() > 0;
    }
  }


		public void updateExclusionFields(String chargeId, ChargeDTO charge) throws SQLException {

			Map<String, Object> fields = new HashMap<>();
			Map<String, Object> keys = new HashMap<>();
			fields.put("item_excluded_from_doctor", charge.getItemExcludedFromDoctor());
			fields.put("item_excluded_from_doctor_remarks", charge.getDoctorExclusionRemarks());
			keys.put("charge_id", chargeId);

			 DataBaseUtil.dynaUpdate(con,"bill_charge", fields, keys);
		}

    private static final String INV_ISSUED_STATIC_PACKAGES = "SELECT DISTINCT bc.bill_no, "
        + " bc.charge_ref,p.package_id,p.package_name,pp.pat_package_id FROM bill_charge bc JOIN "
        + " patient_package_content_consumed ppcc ON(ppcc.bill_charge_id=bc.charge_id "
        + " and ppcc.item_type ='Inventory' and ppcc.quantity>0) JOIN "
        + " patient_package_contents ppc ON(ppc.patient_package_content_id="
        + " ppcc.patient_package_content_id) JOIN patient_packages pp ON(ppc.patient_package_id="
        + " pp.pat_package_id) JOIN packages p ON(pp.package_id=p.package_id) WHERE "
        + " bc.charge_head='INVITE' AND pp.status IN('P','C') AND p.multi_visit_package = false ";

    public List<BasicDynaBean> getInvStaticPackagesIssued(List<Bill> activeBills) {
      String[] billNos = new String[activeBills.size()];
      String[] placeHolderArr = new String[activeBills.size()];
      Arrays.fill(placeHolderArr, "?");
      String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
      StringBuilder query = new StringBuilder(INV_ISSUED_STATIC_PACKAGES);
      query.append(" AND bc.bill_no IN (" + placeHolders +")");
      for (int i=0; i < billNos.length; i++) {
        billNos[i] = activeBills.get(i).getBillNo();
      }

      List<BasicDynaBean> pkgList = new ArrayList<>();
      try {
        pkgList = DataBaseUtil.queryToDynaList(query.toString(), billNos);
      } catch (SQLException ex) {
        logger.error("Error fetching data for INV_ISSUED_STATIC_PACKAGES :", ex);
      }

      return pkgList;

    }

	public boolean insertBillChargeTransaction(Connection con, List<ChargeDTO> charges, String billNo)
			throws SQLException, IOException {
		boolean success = false;

		if (null == charges) {
			return false;
		}
		try {
			for (ChargeDTO charge : charges) {
			  BasicDynaBean billChargeTransactionBean = billChargeTranasactionDao.getBean();
			  billChargeTransactionBean.set("bill_charge_id", charge.getChargeId());
			  billChargeTransactionBean.set("cash_rate", charge.getCashRate());
			  if ("SNP".equals(charge.getChargeGroup()) && billNo != null) {
			    BasicDynaBean billBean = (BasicDynaBean) billDao.getBillBean(billNo);
			    String orgId = (String) billBean.get("bill_rate_plan_id");
					BasicDynaBean specialCodeBean = (BasicDynaBean) serviceMasterDao
							.getServiceDetails(charge.getActDescriptionId(), orgId);
					String splServiceCode = (String) specialCodeBean.get("special_service_code");
					String splServiceContract = (String) specialCodeBean.get("special_service_contract_name");
					if ((splServiceCode != null && !splServiceCode.isEmpty())
							|| (splServiceContract != null && !splServiceContract.isEmpty())) {
						billChargeTransactionBean.set("transaction_id", billChargeTranasactionDao.getNextSequence());
						billChargeTransactionBean.set("special_service_code", splServiceCode);
						billChargeTransactionBean.set("special_service_contract_name", splServiceContract);
					}
				}
			  success = billChargeTranasactionDao.insert(con, billChargeTransactionBean);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return success;
	}


	public void updateBillChargeTransaction(Connection con, List<ChargeDTO> charges, String billNo)
			throws SQLException, IOException {

		List<ChargeDTO> newSplServCode = new ArrayList<>();
		BasicDynaBean billBean = (BasicDynaBean) billDao.getBillBean(billNo);
		String orgId = (String) billBean.get("bill_rate_plan_id");
		if (null == charges) {
			return;
		}
		try {
			for (ChargeDTO charge : charges) {
				if ("SNP".equals(charge.getChargeGroup())) {
					BasicDynaBean specialCodeBean = (BasicDynaBean) serviceMasterDao
							.getServiceDetails(charge.getActDescriptionId(), orgId);
					String splServiceCode = (String) specialCodeBean.get("special_service_code");
					String splServiceContract = (String) specialCodeBean.get("special_service_contract_name");
					BasicDynaBean splServUpdateBean = billChargeTranasactionDao.findByKey(con, "bill_charge_id",
							charge.getChargeId());
					if ((splServiceCode != null && !splServiceCode.isEmpty())
							|| (splServiceContract != null && !splServiceContract.isEmpty())) {
						if (splServUpdateBean != null) {
							splServUpdateBean.set("special_service_code", splServiceCode);
							splServUpdateBean.set("special_service_contract_name", splServiceContract);
							billChargeTranasactionDao.update(con, splServUpdateBean.getMap(), "bill_charge_id",
									charge.getChargeId());
						} else {
							newSplServCode.add(charge);
						}
					} else if (splServUpdateBean != null) {
						billChargeTranasactionDao.delete(con, "bill_charge_id", charge.getChargeId());

					}
				}
			}
			insertBillChargeTransaction(con, newSplServCode, billNo);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void insertSpecialServiceObservation(Connection con, List<ChargeDTO> chargesList, String billNo)
			throws Exception {
		if (org.apache.commons.lang.StringUtils.isBlank(billNo)) {
			return;
		}
		BasicDynaBean billBean = billDao.getBillBean(billNo);
		String orgId = (String) billBean.get("bill_rate_plan_id");
		for (ChargeDTO charge : chargesList) {
			if ("SNP".equals(charge.getChargeGroup())) {

				String serviceId = charge.getActDescriptionId();
				String chargeId = charge.getChargeId();
				String splServiceCode = null;
				BasicDynaBean specialCodeBean = (BasicDynaBean) serviceMasterDao.getServiceDetails(serviceId, orgId);
				if (specialCodeBean != null) {
					splServiceCode = (String) specialCodeBean.get("special_service_code");
				}
				if (splServiceCode != null && !splServiceCode.isEmpty()) {

					BasicDynaBean obsBean = mrdObsDao.getBean();
					obsBean.set("charge_id", chargeId);
					obsBean.set("observation_type", "Grouping");
					obsBean.set("code", "PackageID");
					obsBean.set("value", splServiceCode);
					obsBean.set("value_type", "Other");
					obsBean.set("value_editable", "N");
					mrdObsDao.insert(con, obsBean);
				}
			}
		}
	}

	public void updateSpecialServiceObservation(Connection con, List<ChargeDTO> chargesList,
												String billNo) throws Exception {
		Map<String, Object> filter = new HashMap<>();
		List<ChargeDTO> newObs = new ArrayList<>();
		BasicDynaBean billBean = billDao.getBillBean(billNo);
		String orgId = (String) billBean.get("bill_rate_plan_id");
		for (ChargeDTO charge : chargesList) {
			if ("SNP".equals(charge.getChargeGroup())) {
				String serviceId = charge.getActDescriptionId();
				String chargeId = charge.getChargeId();
				filter.put("charge_id", chargeId);
				BasicDynaBean obsBean = mrdObsDao.findByKey(filter);
				BasicDynaBean specialCodeBean = serviceMasterDao
						.getServiceDetails(serviceId, orgId);
				String specialCode = (String) specialCodeBean.get("special_service_code");

				if (org.apache.commons.lang.StringUtils.isNotBlank(specialCode)) {
					if (obsBean != null) {
						obsBean.set("value", specialCode);
						mrdObsDao.update(con, obsBean.getMap(), filter);
					} else {
						newObs.add(charge);
					}
				} else if (obsBean != null) {
					mrdObsDao.delete(con, "observation_id", obsBean.get("observation_id"));

				}
			}
		}
		insertSpecialServiceObservation(con, newObs, billNo);
	}

}

