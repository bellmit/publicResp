package com.insta.hms.payments;

import au.com.bytecode.opencsv.CSVWriter;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class PaymentsDAO {

	static Logger logger = LoggerFactory.getLogger(PaymentsDAO.class);

	Connection con = null;
	GenericDAO payeeDAO = new GenericDAO("payee_names_view_for_voucher");
	private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");


	public PaymentsDAO(Connection con){
		this.con = con;
	}
	public PaymentsDAO(){}


	public static final String PAYEE_NAMES = " (CASE WHEN  payment_type in('D','P','R') THEN "+
		" COALESCE((SELECT doctor_name FROM doctors WHERE  doctor_id=payee_name)," +
		" (SELECT referal_name FROM referral WHERE referal_no=payee_name))  "+
		" WHEN payment_type= 'F' THEN (SELECT referal_name FROM referral WHERE referal_no=payee_name) "+
		" WHEN payment_type='O' THEN " +
		" (SELECT COALESCE(om.oh_name, hcm.center_name) AS oh_name " +
		"  FROM  diag_outsource_master dom" +
		"  LEFT JOIN outhouse_master om ON (om.oh_id = dom.outsource_dest)" +
		"  LEFT JOIN hospital_center_master hcm ON (hcm.center_id::text = dom.outsource_dest) " +
		"  where dom.outsource_dest=payee_name) " +
		" WHEN payment_type='S' THEN (SELECT supplier_name FROM  supplier_master where supplier_code=payee_name) "+
		" ELSE payee_name  END ) as payee_name ";

	public static final String ACTIVE_PAYEES_FIELDS = "SELECT "+ PAYEE_NAMES +
		" ,payee_name as payeeid, pd.payment_type ";

	public static final String ACTIVE_PAYEES_TABLES =" FROM  payments_details pd ";

	public static final String ACTIVE_PAYEES_FILTER = " where voucher_no is null "; //and pd.amount >0 ";

	public static final String ACTIVE_PAYEE_SUB_TABLES = " FROM (SELECT DISTINCT ON (payee_name) payee_name,"+
		" pd.payment_type "+ ACTIVE_PAYEES_TABLES + ACTIVE_PAYEES_FILTER + ") as pd ";


	public static final String GET_VOUCHER_DETAILS = "SELECT  CASE WHEN payment_type='C' THEN "+
		" 'Cash Voucher' WHEN payment_type ='D' THEN 'Doctor Payment' WHEN payment_type in ('R','F') "+
		" THEN  'Referral Doctor Payment' WHEN payment_type='O' THEN 'Out Test Payments' WHEN "+
		" payment_type='S' THEN 'Supplier Payments' WHEN payment_type ='P' THEN  "+
		" 'Prescribing Doctor Payment' END as payment_type, p.voucher_no,p.date, p.type, voucher_category, "+
		PAYEE_NAMES +",p.tax_amount,p.tds_amount, p.bank,p.reference_no,(0-pd.amount) as amount,p.remarks, "+
		" pd.description,pd.category, p.payment_mode_id, p.card_type_id, pm.payment_mode, cm.card_type, "+
		" p.payment_type as paymenttype "+
		" FROM  payments p JOIN (SELECT description, category, amount, voucher_no "+
					" FROM  payments_details WHERE voucher_no=? ) as pd ON (pd.voucher_no=p.voucher_no ) "+
		" JOIN payment_mode_master pm ON (pm.mode_id = p.payment_mode_id) " +
		" LEFT JOIN card_type_master cm ON (cm.card_type_id = p.card_type_id) " +
		" where p.voucher_no=? ";



	public static final String PAYMENT_DETAILS_FIELDS = ACTIVE_PAYEES_FIELDS + " ,amount, "+
		" CASE WHEN pd.payment_type='C' THEN 'Cash Voucher' WHEN payment_type='D' THEN 'Doctor' "+
		" WHEN pd.payment_type='P' THEN 'Prescribing Doctor'  "+
		" WHEN pd.payment_type in ('R','F') THEN 'Referral Doctor' "+
		" WHEN pd.payment_type='O' THEN 'Outgoing Tests' " +
		" WHEN pd.payment_type='S' THEN 'Supplier Payments' END as paymentType, "+
		" posted_date ,description, category, payment_id, hcm.center_name ";

	public static final String PAYMENT_DETAILS_TABLES = "FROM  payments_details pd " ;


	public static final String PAYMENT_COUNT = "SELECT count(payment_id) ";

	public static final String PAYMENT_DETAILS_GROUP = " payee_name, pd.payment_type, posted_date, "+
		" description,category, payment_id, amount ";


	private static final String CENTER_WISE_REFERRAL_DOCTORS =
		" SELECT ref.referal_no as doctor_id,ref.referal_name as  doctor_name, 'referral' as doctype "+
		" FROM referral ref "+
		" JOIN referral_center_master rcm on (ref.referal_no = rcm.referal_no) "+
		" WHERE ref.referal_no in  (select reference_docto_id from  patient_registration) "+
			" and (rcm.center_id = 0 or rcm.center_id = ? )  "+
		" UNION  "+
		" SELECT ref.referal_no as doctor_id,ref.referal_name as  doctor_name, 'referral' as doctype "+
		" FROM referral ref "+
		" JOIN referral_center_master rcm on (ref.referal_no = rcm.referal_no) "+
		" WHERE ref.referal_no in (SELECT referring_doctor FROM incoming_sample_registration) "+
			" and (rcm.center_id = 0 or rcm.center_id = ? )  "+
		" UNION SELECT doc.doctor_id, doc.doctor_name,  'doctor' as doctype  "+
		" FROM doctors doc " +
		" JOIN doctor_center_master dcm ON(doc.doctor_id = dcm.doctor_id) "+
		" WHERE doc.doctor_id in (SELECT reference_docto_id from patient_registration) "+
			" and (dcm.center_id = 0 or dcm.center_id = ? ) "+
		" UNION "+
		" SELECT doc.doctor_id, doc.doctor_name,  'doctor' as doctype  "+
		" FROM doctors doc " +
		" JOIN doctor_center_master dcm ON(doc.doctor_id = dcm.doctor_id) "+
		" WHERE doc.doctor_id in (SELECT referring_doctor FROM incoming_sample_registration) "+
			" and (dcm.center_id = 0 or dcm.center_id = ? ) "+
		" order by doctor_name " ;


	private static final String ALL_REFERRAL_DOCTORS=
		" SELECT referal_no as doctor_id,referal_name as  doctor_name, 'referral' as doctype "+
		" FROM referral ref WHERE referal_no in  (select reference_docto_id from  patient_registration) "+
		" UNION  "+
		" SELECT referal_no as doctor_id,referal_name as  doctor_name, 'referral' as doctype "+
		" FROM referral ref WHERE referal_no in (SELECT referring_doctor FROM incoming_sample_registration)"+
		" UNION SELECT doctor_id, doctor_name,  'doctor' as doctype  "+
		" FROM doctors doc WHERE doctor_id in (SELECT reference_docto_id from patient_registration) "+
		" UNION "+
		" SELECT doctor_id, doctor_name,  'doctor' as doctype  "+
		" FROM doctors WHERE doctor_id in (SELECT referring_doctor FROM incoming_sample_registration) "+
		" order by doctor_name " ;

	// INSERTING PAYMENTS DETAILS

	/**
	 *  Creating new payment id using sequence
	 */

	public static String getPaymentId() throws SQLException{
		String paymentId =null;
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			con = DataBaseUtil.getConnection();
			stmt = con.prepareStatement("SELECT GENERATE_ID('DOCTOR_PAYMENTS')");
			rs = stmt.executeQuery();
			if (rs.next()) {
				paymentId=rs.getString(1);
			}
		} finally {
			DataBaseUtil.closeConnections(con, stmt, rs);
		}
		return paymentId;
	}

	public static final String INSERT_PAYMENT_DETAILS = "INSERT INTO payments_details "+
		"(payment_id, payment_type, voucher_no, amount, description, category, posted_date, "+
		" username, payee_name, charge_id, account_head,activity_id,account_group, expense_center_id) "+
		" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

	/*
	 * Inserting paymentDetails into payments_details table;
	 */


	public static final String IS_CHARGE_EXISTS =" SELECT payment_id FROM payments_details where charge_id=? AND activity_id = ? ";

	public  static boolean isPaymentPosted(Connection con, String chargeId,String activityId)throws SQLException{
		try (PreparedStatement ps = con.prepareStatement(IS_CHARGE_EXISTS);){
			ps.setString(1, chargeId);
			ps.setString(2, activityId);
			return (DataBaseUtil.getStringValueFromDb(ps)!= null);
		}
	}

	public static boolean insertPaymentDetails(Connection con, ArrayList<PaymentDetailsDTO> payDetails)throws SQLException{
		PreparedStatement ps = null;
		boolean success = false;
		String payId = null; //For Supplier Payments
		int countRecords = 0;
		try{
			ps = con.prepareStatement(INSERT_PAYMENT_DETAILS);
			if(ps == null) return false;
			Iterator<PaymentDetailsDTO> it = payDetails.iterator();
			while(it.hasNext()){
				PaymentDetailsDTO pd = it.next();
				int i=1;
				if (!isPaymentPosted(con, pd.getChargeId(),pd.getPkgActivityId())){

					payId = getPaymentId();
					pd.setPaymentId(payId);

					ps.setString(i++,payId);
					ps.setString(i++,pd.getPaymentType());
					ps.setString(i++,pd.getVoucherNo());
					ps.setBigDecimal(i++,pd.getAmount());
					ps.setString(i++,pd.getDescription());
					ps.setString(i++,pd.getCategory());
					ps.setTimestamp(i++, new Timestamp(pd.getPostedDate().getTime()));
					ps.setString(i++,pd.getUsername());
					ps.setString(i++,pd.getPayeeName());
					ps.setString(i++,pd.getChargeId());
					ps.setInt(i++, pd.getAccountHead());
					ps.setString(i++, pd.getPkgActivityId());
					/*
					 * If Payment Type is Supplier and it not a direct payment
					 * then we are inserting account group of store .
					 * In all other cases it is Hospital only (i.e. 1 only)
					 */
					if (pd.getPaymentType().equals("S") && pd.getAccountGroup() != null && !(pd.getAccountGroup().equals("")))
						ps.setInt(i++, Integer.parseInt(pd.getAccountGroup()));
					else
						ps.setInt(i++, 1);
					ps.setInt(i++, pd.getCenterId());

					ps.addBatch();
					/*
					 * If Supplier payments then we getting payment id
					 * to update in the all required tables
					 */
					if (pd.getPaymentType().equals("S"))
						pd.setPaymentId(payId);

				}else{
					success = false;
				}
			}
			int[] updates = ps.executeBatch();
			success = DataBaseUtil.checkBatchUpdates(updates);

		}finally {
			ps.close();
		}
		return success;
	}


									// CREATING VOUCHER

	
	private static final String VOUCHER_NUMBER_SEQUENCE_PATTERN =
			" SELECT pattern_id " +
			" FROM (SELECT min(priority) as priority,  pattern_id FROM hosp_voucher_seq_prefs " +
			" WHERE (center_id=? or center_id= 0) " +
			" GROUP BY pattern_id ORDER BY priority limit 1) as foo";
	/*
	 * Creating new Voucher id using sequence
	 */
	
	public String getVoucherNo(Integer centerId) throws SQLException {

			BasicDynaBean b = DataBaseUtil.queryToDynaBean(VOUCHER_NUMBER_SEQUENCE_PATTERN, centerId);
			String patternId = (String) b.get("pattern_id");
			return DataBaseUtil.getNextPatternId(patternId);
	}

	public String getReversalVoucherNo()throws SQLException{
		return AutoIncrementId.getSequenceId("reversal_voucher_sequence","REVERSAL_VOUCHERID");
	}



	public static final String INSERT_PAYMENTS = "INSERT INTO payments "+
		"(voucher_no,type,amount,tax_type,tax_amount,date,counter,username,payment_mode_id,card_type_id,bank,reference_no "+
		" ,payee_name,tds_amount,remarks,voucher_category, payment_type, round_off) "+
		" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " ;

	/*
	 * Inserting payments into Payments Table
	 */

	public boolean insertPayments(PaymentsDTO payment)throws SQLException{
		boolean success = false;
		try (PreparedStatement ps = con.prepareStatement(INSERT_PAYMENTS);){
			int i =1;
			ps.setString(i++,payment.getVoucherNo());
			ps.setString(i++,payment.getType());
			ps.setBigDecimal(i++,payment.getAmount());
			ps.setString(i++,payment.getTaxType());
			ps.setBigDecimal(i++,payment.getTaxAmount());
			ps.setTimestamp(i++,payment.getPaymentDtTime());
			ps.setString(i++,payment.getCounter());
			ps.setString(i++,payment.getUsername());
			ps.setInt(i++,payment.getPaymentModeId());
			ps.setInt(i++,payment.getCardTypeId());
			ps.setString(i++,payment.getBank());
			ps.setString(i++,payment.getReferenceNo());
			ps.setString(i++,payment.getPayeeName());
			ps.setBigDecimal(i++,payment.getTdsAmount());
			ps.setString(i++,payment.getRemarks());
			if (payment.getVoucherCategory() == null || payment.getVoucherCategory().equals("")){
				 ps.setString(i++, "P");
			}else{
				ps.setString(i++, payment.getVoucherCategory());
			}
			ps.setString(i++, payment.getPaymentType());
			ps.setBigDecimal(i++, payment.getRoundOff());
			success =  ( ps.executeUpdate() > 0 );
		}
		return success ;
	}

									//UPDATING PAYMENT DETAILS

	/*
	 * Updating voucherNo in  payment_details table for the created vouchers
	 */

  private static final String UPDATE_PAYMENT_DETAILS = "UPDATE payments_details pd set voucher_no= ?";
  private static final String UPDATE_PAYMENT_DETAILS_VISIT_JOIN = " FROM bill_charge bc"
      + " JOIN bill bac ON (bc.bill_no = bac.bill_no)";
  private static final String UPDATE_PAYMENT_VISIT_CLAUSE = " AND bc.charge_id = pd.charge_id"
      + " AND visit_type = ?";
  private static final String UPDATE_PAYMENT_CENTER_CLAUSE = " AND expense_center_id = ?";
  private static final String UPDATE_PAYMENT_DETAILS_FILTER = "WHERE pd.payee_name=? AND pd.payment_type=? AND pd.voucher_no IS NULL ";

	public boolean updatePaymentDetails(PaymentsDTO payment, String payType, String screen, String voucherType)
			throws SQLException {
		boolean success = false;
		String query = UPDATE_PAYMENT_DETAILS;
		int centerId = RequestContext.getCenterId();
		int paramIndex = 1;
		if (voucherType.equals("ip") || voucherType.equals("op")) {
			query += UPDATE_PAYMENT_DETAILS_VISIT_JOIN;
		}
		query += UPDATE_PAYMENT_DETAILS_FILTER;
		if (voucherType.equals("ip") || voucherType.equals("op")) {
			query += UPDATE_PAYMENT_VISIT_CLAUSE;
		}
		if (centerId != 0) {
			query += UPDATE_PAYMENT_CENTER_CLAUSE;
		}
		try (PreparedStatement ps = con.prepareStatement(query)) {
			ps.setString(paramIndex++, payment.getVoucherNo());
			ps.setString(paramIndex++, payment.getPayeeName());
			ps.setString(paramIndex++, payType);
			if (voucherType.equals("ip") || voucherType.equals("op")) {
				ps.setString(paramIndex++, voucherType.substring(0, 1));
			}
			if (centerId != 0) {
				ps.setInt(paramIndex++, centerId);
			}
			success = (ps.executeUpdate() > 0);
		}
		return success;
	}

	/*
	 * Updating the paid date and status of invoice once the payments are made
	 */
	public static final String UPDATE_STORE_INVOICE = " UPDATE store_invoice pi SET paid_date = ? ,payment_remarks= ?,status = 'C'  " +
			" FROM  payments_details pd " +
			" WHERE (pd.payment_id = pi.payment_id ) AND pd.voucher_no = ? ";

	public static final String UPDATE_PHARMACY_DEBIT = " UPDATE store_debit_note pdn  SET status = 'C' " +
			" FROM payments_details pd " +
			" WHERE (pd.payment_id = pdn.payment_id) AND pd.voucher_no = ? ";

	public boolean updateInvoice (PaymentsDTO payment,String payType) throws SQLException{
		boolean success = false;
    try (PreparedStatement ps = con.prepareStatement(UPDATE_STORE_INVOICE)){
      ps.setDate(1,  payment.getDate());
      ps.setString(2, payment.getRemarks());
      ps.setString(3,payment.getVoucherNo());
      success = (ps.executeUpdate() >= 0);
    }
    try (PreparedStatement ps = con.prepareStatement(UPDATE_PHARMACY_DEBIT)){
      ps.setString(1, payment.getVoucherNo());
      success = (ps.executeUpdate() >= 0);
    }
		return success;
	}



	/*
	 * Fetching payee amount from payments details table based on payee name
	 */

	private static final String GET_PAYEE_AMOUNT_QUERY = "SELECT sum(pd.amount) as amount from payments_details pd";
	private static final String GET_PAYEE_AMOUNT_VISIT_JOIN = " JOIN bill_charge bc ON (bc.charge_id = pd.charge_id)"
	    + " JOIN bill bac ON (bc.bill_no = bac.bill_no)";

  private static final String GET_PAYEE_AMOUNT_FILTER = " WHERE pd.payee_name=? AND pd.payment_type=? AND pd.voucher_no is null";
  private static final String GET_PAYEE_AMOUNT_VISIT_FILTER = " AND visit_type = ?";
  private static final String GET_PAYEE_AMOUNT_CENTER_FILTER = " AND expense_center_id = ?";

	public BigDecimal getPayeeAmount(String payeename,String payType,String screen, String voucherType)throws SQLException{
		BigDecimal amount = new BigDecimal(0);
		String query = GET_PAYEE_AMOUNT_QUERY;
		if (voucherType.equals("ip") || voucherType.equals("op")) {
		  query += GET_PAYEE_AMOUNT_VISIT_JOIN;
		}
		query += GET_PAYEE_AMOUNT_FILTER;
		if (voucherType.equals("ip") || voucherType.equals("op")) {
		  query += GET_PAYEE_AMOUNT_VISIT_FILTER;
		}
    int centerId = RequestContext.getCenterId();
		if (centerId != 0) {
		  query += GET_PAYEE_AMOUNT_CENTER_FILTER;
		}
    int paramIndex = 1;
		try (Connection con = DataBaseUtil.getReadOnlyConnection();
		    PreparedStatement ps = con.prepareStatement(query);) {
      ps.setString(paramIndex++, payeename);
      ps.setString(paramIndex++, payType);
      if (voucherType.equals("ip") || voucherType.equals("op")) {
        ps.setString(paramIndex++, voucherType.substring(0, 1));
      }
      if (centerId != 0) {
        ps.setInt(paramIndex++, centerId);
      }
      try (ResultSet rs = ps.executeQuery()) {
        if(rs.next()){
          amount = rs.getBigDecimal("amount");
        }        
      }
		}
		return amount;
	}


	/**************************************ALL PAYEES LIST FOR DASHBOARD************************************/
	public static final String ALL_PAYEE_NAMES =
	" SELECT misc_payee_name AS payee_name, misc_payee_name AS payee_id FROM misc_payees "+
	" UNION SELECT doctor_name AS payee_name, doctor_id AS payee_id FROM doctors  WHERE status='A' "+
	" UNION SELECT referal_name AS payee_name, referal_no AS payee_id FROM referral WHERE status='A' "+
	" UNION SELECT oh_name AS payee_name, oh_id AS payee_id FROM outhouse_master WHERE status='A' "+
	" UNION SELECT supplier_name AS payee_name, supplier_code AS payee_id FROM supplier_master WHERE status='A' ";


	/***************************************PAYMENT VOUCHERS QUERIES***************************************/


	public static final String GET_PAYMENT_VOUCHERS = "SELECT p.voucher_no,p.type,(0-p.amount)as amount, "+
		" p.tax_type,p.tax_amount,p.date,p.counter,p.username,p.reference_no,p.tds_amount, "+
		PAYEE_NAMES +" ,p.payment_mode_id, p.card_type_id, pm.payment_mode, cm.card_type, p.bank ,p.payment_type,"+
		" p.payee_name as payeeId, p.voucher_category, c.center_id " ;

  public static final String ALL_PAYMENT_TABLES = " FROM payments p "
      + " JOIN payment_mode_master pm ON (pm.mode_id = p.payment_mode_id) "
      + " JOIN counters c ON (p.counter = c.counter_id) "
      + " LEFT JOIN card_type_master cm ON (cm.card_type_id = p.card_type_id) ";
/*
		join payments_details pd "+
		" on (p.payee_name=pd.payee_name and pd.voucher_no=p.voucher_no and pd.voucher_no is not null)  ";
*/
	public static final String GET_COUNT = "SELECT Count(p.voucher_no) ";


	/***************************************PAYMENT VOUCHERS BLOCK STARTS************************************/

	public static List getAllPayeeList()throws SQLException{
		return DataBaseUtil.queryToDynaList(ALL_PAYEE_NAMES);
	}

	public static PagedList getPaymentVouchers(Map filter, Map listing) throws SQLException, ParseException{
		String PAYMENT_VOUCHER_FIELDS = GET_PAYMENT_VOUCHERS;
		String PAYMENT_VOUCHER_COUNT = GET_COUNT;
		String PAYMENT_VOUCHER_TABLES = ALL_PAYMENT_TABLES;

		return paymentVoucherSearchQuery(filter, listing, PAYMENT_VOUCHER_FIELDS, PAYMENT_VOUCHER_COUNT,
				PAYMENT_VOUCHER_TABLES);

	}

	public static PagedList paymentVoucherSearchQuery(Map filter, Map listing,
			String FIELDS, String COUNT, String TABLES) throws SQLException, ParseException{

		PagedList list = null;
		try (Connection con = DataBaseUtil.getReadOnlyConnection();) {
			
			SearchQueryBuilder qb = new SearchQueryBuilder(con,
					FIELDS, COUNT, TABLES, listing);

			qb.addFilterFromParamMap(filter);
			qb.build();

			list = qb.getDynaPagedList();

		}
		return list;
	}

	/***************************************PAYMENT VOUCHERS BLOCK ENDS**************************************/


	/*************************************PAYMENT DASHBOARD BLOCK STARTS*************************************/


	public static final String PAYEE_LIST = "SELECT * FROM payeenames_view ";

	public static List getPayeeList() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		int centerID = RequestContext.getCenterId();
		try {
			if(centerID != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1){
				String PAYEE_LIST_FOR_CENTER = PAYEE_LIST + "WHERE center_id= ? OR center_id = 0";
				ps = con.prepareStatement(PAYEE_LIST_FOR_CENTER);
				ps.setInt(1, centerID);
				}else {
					ps = con.prepareStatement(PAYEE_LIST);
				}
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static List getActivePayees()throws SQLException{
		return DataBaseUtil.queryToDynaList(ALL_PAYEE_NAMES);
	}

	public static PagedList getPaymentDetails(Map filter, Map listing)throws SQLException, ParseException {

		 String PAYMENT_DUE_FIELDS = PAYMENT_DETAILS_FIELDS ;
		 String PAYMENT_DUE_TABLES = ACTIVE_PAYEES_TABLES;
		 /*if (screen.equals("ip") || screen.equals("op"))
		 	PAYMENT_DUE_TABLES = PAYMENT_DUE_TABLES + " JOIN bill_charge bc ON (bc.charge_id = pd.charge_id)" +
		 											" JOIN bill bac ON (bc.bill_no = bac.bill_no)";
*/
		 PAYMENT_DUE_TABLES = ACTIVE_PAYEES_TABLES + "JOIN hospital_center_master hcm ON(hcm.center_id = pd.expense_center_id)";
		 String PAYMENT_DUE_COUNT =  PAYMENT_COUNT;
		 String PAYMENT_DUE_FILTERS = ACTIVE_PAYEES_FILTER;
		/* if (screen.equals("ip") || screen.equals("op"))
		 	PAYMENT_DUE_FILTERS = PAYMENT_DUE_FILTERS + "AND visit_type ="+(screen.equalsIgnoreCase("ip") ? "i" : "o");*/
	//	 String PAYMENT_DUE_GROUP = PAYMENT_DETAILS_GROUP;
		 return paymentDueSeachQueryBuilder(filter, listing , PAYMENT_DUE_FIELDS ,PAYMENT_DUE_COUNT,
				 PAYMENT_DUE_TABLES, PAYMENT_DUE_FILTERS );

	}

	public static PagedList paymentDueSeachQueryBuilder(Map filter, Map listing, String FIELDS,
			String COUNT,String TABLES, String  FILTERS )	throws SQLException,ParseException {

		Connection con = null;
		PagedList dataList = null;
		try{
			int centerId = RequestContext.getCenterId();
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb = new SearchQueryBuilder(con, FIELDS, COUNT, TABLES, FILTERS, listing);

			qb.addFilterFromParamMap(filter);
			if (centerId != 0)
				qb.addFilter(SearchQueryBuilder.INTEGER, "expense_center_id", "=", centerId);
			qb.build();

			dataList = qb.getDynaPagedList();
			qb.close();

		}finally{
			DataBaseUtil.closeConnections(con, null);
		}
		return 	dataList;
	}

	/***************************************PAYMENT DASHBOARD BLOCK ENDS*************************************/






	//	VOUCHER DETAILS FOR VIEW AND PRINT FROM PAYMENT DASHBOARD

	/*
	 *	Voucher details on voucher screen for view and print
	 */
	public static List getVoucherDetails(String voucherNo) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List  dataList = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps  = con.prepareStatement(GET_VOUCHER_DETAILS);
			ps.setString(1,voucherNo);
			ps.setString(2,voucherNo);
			dataList = DataBaseUtil.queryToDynaList(ps);
		}finally {

			DataBaseUtil.closeConnections(con, ps);
		}
		return dataList ;
	}

	/*
	 * populating payment voucher detail for view and print
	 */

	public static void populateVoucherDetails(PaymentDetails pd ,ResultSet rs)throws SQLException{
		pd.setVoucherNo(rs.getString("voucher_no"));
		pd.setPayeeName(rs.getString("payee_name"));
		pd.setPaymentModeId(rs.getInt("payment_mode_id"));
		pd.setCardTypeId(rs.getInt("card_type_id"));
		pd.setPaymentMode(rs.getString("payment_mode"));
		pd.setCardType(rs.getString("card_type"));
		pd.setBankName(rs.getString("bank"));
		pd.setRefNo(rs.getString("reference_no"));
		pd.setPaymentType(rs.getString("payment_type"));
		pd.setDate(rs.getDate("date"));
		pd.setAmount(rs.getBigDecimal("amount"));
		pd.setTaxAmount(rs.getBigDecimal("tax_amount"));
		pd.setTdsAmount(rs.getBigDecimal("tds_amount"));
		pd.setRemarks(rs.getString("remarks"));
		pd.setDescription(rs.getString("description"));
		pd.setCategory(rs.getString("category"));

	}

	/*
	 *reading payment vouchers from payments table and displaying voucher details to view
	 *
	 */

	public static final String GET_EXPORT_PAYMENT_VOUCHERS = "SELECT p.voucher_no,p.type,pd.amount, "+
		" p.tax_type,p.tax_amount,date(p.date) as date,p.counter,p.username,p.reference_no,p.tds_amount, "+
		PAYEE_NAMES +" ,p.payment_mode_id, p.card_type_id, pm.payment_mode, cm.card_type, p.bank ,p.payment_type,"+
		" p.payee_name as payeeId, p.voucher_category ,p.bank ,p.payment_type,"+
		" p.payee_name as payeeId, c.counter_no as counter_name " ;

	public static final String EXPORT_PAYMENT_TABLES = "FROM payments p " +
		" JOIN payment_mode_master pm ON (pm.mode_id = p.payment_mode_id) " +
		" LEFT JOIN card_type_master cm ON (cm.card_type_id = p.card_type_id) " +
		" JOIN "+
		" (SELECT pd.voucher_no,category, posted_date,  pd.amount  "+
		" FROM payments_details pd JOIN payments p USING(voucher_no)) as pd ON (pd.voucher_no=p.voucher_no) "+
		" JOIN counters c on c.counter_id=p.counter ";

	public static final String EXPORT_CSV_FIELDS = GET_EXPORT_PAYMENT_VOUCHERS + ",pd.category, date(pd.posted_date) as posted_date ";
	public static void exportPayments(CSVWriter writer, Map filter, String screen)
		throws SQLException,Exception{
		Connection con = null;
		PreparedStatement psData = null;
		ResultSet rsData = null;
		String EXPORT_PAYMENTS_TABLES= null;
		EXPORT_PAYMENTS_TABLES = EXPORT_PAYMENT_TABLES ;

		filter.remove("screen");
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb = new SearchQueryBuilder(con,
					EXPORT_CSV_FIELDS, GET_COUNT, EXPORT_PAYMENTS_TABLES);
			qb.addFilterFromParamMap(filter);
			qb.build();

			psData = qb.getDataStatement();

			rsData = psData.executeQuery();

			writer.writeAll(rsData,true);
			writer.flush();
		}finally{
			DataBaseUtil.closeConnections(con, psData, rsData);
		}
	}

	public static final String DATE_CAST = "to_char(date,'yyyy-mm-dd')";

	/*
	 * To Delete payments from payments_details
	 */

	public static final String DELETE_CHARGE = "delete from payments_details where payment_id = ?  AND voucher_no is null ";

	public static boolean deletePaymentItems(Connection con, ArrayList<PaymentDetailsDTO> pdDTO)throws SQLException{
		PreparedStatement ps = null;
		int[] deleted = new int[0];
		try{
			ps = con.prepareStatement(DELETE_CHARGE);
			Iterator<PaymentDetailsDTO> it = pdDTO.iterator();
			while(it.hasNext()){
				PaymentDetailsDTO pd = it.next();
				ps.setString(1,pd.getPaymentId());
				ps.addBatch();
			}
			deleted = ps.executeBatch();
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
		return deleted.length > 0	;
	}
	//
	//SUPPLIER RELATED QUERIES STATRTS HERE
	//
	/*
	 *	Supplier list
	 */

	private static final String ALL_SUPPLIERS = "SELECT supplier_code,supplier_name,cust_supplier_code, "+
			" CASE WHEN cust_supplier_code IS NOT NULL AND  TRIM(cust_supplier_code) != ''  THEN supplier_name||' - '||cust_supplier_code ELSE supplier_name END as cust_supplier_code_with_name "+
			" FROM supplier_master ORDER BY supplier_name ";

	public static ArrayList getSuppliers(){
		return DataBaseUtil.queryToArrayList(ALL_SUPPLIERS);
	}

	/**
	 * To fetch supplier code for payments by selecting suppliername on payment dashboard
	 */

	private static final String SUPPLIER_ID = "SELECT supplier_code FROM supplier_master where supplier_name=? ";

	public static String getSupplierId(String supplierName)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String supplierId= null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(SUPPLIER_ID);
			ps.setString(1,supplierName);
			rs = ps.executeQuery();
			while(rs.next()){
				supplierId = rs.getString("supplier_code");
			}
		}finally{
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return supplierId;
	}

	private static final String SUPPLIER_PAYMENT_FIELDS = "SELECT * " ;

	private static final String COUNT_QUERY = "SELECT count(*) ";

	private static final String SUPPLIER_PAYMENT_TABLE = " FROM  supplier_payments_view spv ";

	private static final String SUPPLIER_PAYMENT_WHERE = " WHERE spv.payment_id is null AND "+
		" (status in ('F','C') OR debit_note_status = 'O') ";


	private static final String SUPPLIER_PAIED_CHARGES = "SELECT * " ;

	private static final String SUPPLIER_PAIED_TABLES = " FROM   supplier_payments_view spv "+
		" JOIN payments_details pd ON pd.payment_id=spv.payment_id and pd.voucher_no is null ";

	private static final String SUPPLIER_PAID_PAYMENT_WHERE = " WHERE spv.payment_id is not null AND "+
		 " (status in ('C', 'F') OR debit_note_status = 'O') ";

	private static final String SUPPLIER_PAIED_AMOUNT = "SELECT sum(amount) as paid_amount " ;

	public static PagedList getSupplierPendingCharges(Map filter, Map listing, String type) throws SQLException,
		   ParseException {

		Connection con = null;
		PagedList list = null;
		logger.info("LISTING.PAGESIZE=="+listing.get(LISTING.PAGESIZE)+" type is =="+type);
		if (type.equals("all")){
			listing.put(LISTING.PAGESIZE, 0);
			listing.put(LISTING.PAGENUM, 0);
		}

		try{
			int centerId = RequestContext.getCenterId();
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder sqb = new SearchQueryBuilder(con,SUPPLIER_PAYMENT_FIELDS,COUNT_QUERY,
					SUPPLIER_PAYMENT_TABLE, SUPPLIER_PAYMENT_WHERE, listing);

			sqb.addFilterFromParamMap(filter);
			if (centerId != 0)
				sqb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
			sqb.build();
			list = sqb.getMappedPagedList();
		}finally {
			if (con != null) con.close();
		}
		return list;
	}

	public static PagedList getSupplierPostedCharges(Map filter, Map listing)throws SQLException,
		   ParseException{

		Connection con = null;
		PreparedStatement psData = null;
		PreparedStatement psCount = null;

		ResultSet rsData = null;
		ResultSet rsCount = null;
		PagedList list = null;
		int totalCount =0;
		int centerId = RequestContext.getCenterId();
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder sqb = new SearchQueryBuilder(con, SUPPLIER_PAIED_CHARGES, COUNT_QUERY,
					SUPPLIER_PAIED_TABLES, SUPPLIER_PAID_PAYMENT_WHERE, listing);

			sqb.addFilterFromParamMap(filter);
			if (centerId != 0)
				sqb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
			sqb.build();

			list = sqb.getMappedPagedList();

		}finally{
			if (con != null) con.close();
		}
		return list;
	}

	public static BigDecimal getSupplierPaidAmount(Map filter, Map listing )throws SQLException,
		   ParseException{
		Connection con = null;
		PreparedStatement psData = null;
		int centerId = RequestContext.getCenterId();
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb = new SearchQueryBuilder(con,SUPPLIER_PAIED_AMOUNT, COUNT_QUERY,
					SUPPLIER_PAIED_TABLES, SUPPLIER_PAID_PAYMENT_WHERE, listing );

			qb.addFilterFromParamMap(filter);
			if (centerId != 0)
				qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
			qb.build();
			psData = qb.getDataStatement();

			return DataBaseUtil.getBigDecimalValueFromDb(psData);

		}finally{
			if (psData != null) psData.close();
			if (con != null) con.close();
		}

	}

	private static final String GET_COUNTER_ID ="SELECT counter_id from counters where counter_no=? ";

	public static String getCounterId(String counterName)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String counterId = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_COUNTER_ID);
			ps.setString(1,counterName);
			rs = ps.executeQuery();
			while(rs.next()){
				counterId = rs.getString("counter_id");
			}
		}finally{
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return counterId;

	}


	//Referral doctor methods
	/*
	 * TODO : Make a common method for both screens
	 */

	/*
	 * This is used for auto complete in referral doctor screen in payments
	 */
	public static ArrayList getReferralDoctorsArrayList()throws SQLException{
		Connection con = null;
	    PreparedStatement ps = null;
	    ArrayList refDoc = null;
	    int centerID = RequestContext.getCenterId();
	    try{
	    	if(centerID == 0){
		        con = DataBaseUtil.getConnection();
		        ps = con.prepareStatement(ALL_REFERRAL_DOCTORS);
	    	}else {
	    		con = DataBaseUtil.getConnection();
	    		 ps = con.prepareStatement(CENTER_WISE_REFERRAL_DOCTORS);
	    		 ps.setInt(1, centerID);
			     ps.setInt(2, centerID);
			     ps.setInt(3, centerID);
			     ps.setInt(4, centerID);
	    	}
	        refDoc = DataBaseUtil.queryToArrayList(ps);
	    }finally{
	    	DataBaseUtil.closeConnections(con, ps);
	    }
		return refDoc;
	}

	/*
	 * This is used in payments report builder screen
	 */
	public static List<BasicDynaBean> getReferralDoctorsList()throws SQLException{
		return DataBaseUtil.queryToDynaList(ALL_REFERRAL_DOCTORS);
	}

	/* filed for get bill charges 	  */

	public static final String REFERRAL_ALL_CHARGE =
		" ( SELECT coalesce(pr.center_id, isr.center_id) AS center_id, b.bill_no, b.total_amount, b.total_claim AS actual_claim_amt, b.primary_claim_status, b.claim_recd_amount,"+
		" b.status AS bill_status, bc.charge_id, bc.act_description, bc.amount, bc.overall_discount_auth, "+
		" bc.dr_discount_amt, bc.discount_auth_dr, bc.discount_auth_ref, bc.ref_discount_amt, bc.charge_group, "+
		" bc.referal_amount, bc.discount_auth_pres_dr, bc.pres_dr_discount_amt, bc.discount_auth_hosp, "+
		" bc.hosp_discount_amt, bc.status AS bill_charge_status,chc.chargehead_name, bc.posted_date, "+
		" coalesce(pr.reference_docto_id, isr.referring_doctor) as reference_docto_id, "+
		" pr.primary_sponsor_id, coalesce(pr.mr_no, isr.incoming_visit_id) as mr_no, "+
		" CASE WHEN b.bill_type='C' THEN 'Bill Later'  ELSE 'Bill Now' END as billtype,"+
		" bc.prescribing_dr_amount, bc.doctor_amount, coalesce(b.finalized_date,isr.date) as finalized_date, bc.discount, bc.ref_payment_id, "+
		" CASE WHEN b.status='A' THEN 'Open' WHEN b.status='F' THEN  'Finalized' WHEN b.status='S' THEN "+
		" 'Settled' WHEN b.status='C' THEN 'Closed' END as billstatus, CASE WHEN b.visit_type='i' THEN "+
		" 'In Patient' WHEN b.visit_type='o'  THEN 'Out Patient' END as visittype, "+
		" coalesce(pdet.patient_name,isr.patient_name) as patient_name ,  "+
		" pdet.last_name, chc.chargehead_id, bc.overall_discount_amt,b.visit_type, "+
		" dac.disc_auth_name AS discount_auth_dr_name, dap.disc_auth_name AS discount_auth_pres_dr_name, "+
		" daov.disc_auth_name AS overall_discount_auth_name, dar.disc_auth_name AS discount_auth_ref_name, "+
		" CASE WHEN pr.primary_sponsor_id  LIKE ('TAP%') THEN 'I' ELSE 'N' END AS insurancestatus, " +
		" CASE WHEN bc.charge_group = 'PKG' THEN 'Y' ELSE 'N' END AS package_charge, tpa.tpa_name, pdet.patient_group ";

	public static final String REFERRAL_CHARGE_FIELDS = "SELECT * FROM " + REFERRAL_ALL_CHARGE ;

	public static final String REFERRAL_CHARGE_AMOUNT = "SELECT SUM(referal_amount)as amount FROM " + REFERRAL_ALL_CHARGE ;

	public static final String REFERRAL_CHARGE_TABLES = "FROM bill_charge bc JOIN bill b USING(bill_no) "+
		" JOIN chargehead_constants chc ON  chc.chargegroup_id=bc.charge_group and  "+
		" chc.chargehead_id=bc.charge_head  " +
		" LEFT JOIN patient_registration pr ON (pr.patient_id=b.visit_id) " +
		" LEFT JOIN patient_details pdet on (pdet.mr_no = pr.mr_no)   "+
		" LEFT JOIN tpa_master tpa on tpa.tpa_id = pr.primary_sponsor_id "+
		" LEFT JOIN incoming_sample_registration isr on (isr.billno=b.bill_no) " ;

	public static final String REFERRAL_DISCOUNT_CHARGE_TABLES=
		" LEFT OUTER JOIN discount_authorizer dac ON (bc.discount_auth_dr = dac.disc_auth_id) "+
		" LEFT OUTER JOIN discount_authorizer dap ON (bc.discount_auth_pres_dr = dap.disc_auth_id) "+
		" LEFT OUTER JOIN discount_authorizer dar ON (bc.discount_auth_ref = dar.disc_auth_id) "+
		" LEFT OUTER JOIN discount_authorizer dah ON (bc.discount_auth_hosp = dah.disc_auth_id) "+
		" LEFT OUTER JOIN discount_authorizer daov ON (bc.overall_discount_auth = daov.disc_auth_id) "+
		" )as payments ";


	public static final String REFERRAL_CHARGES_TABLES =  REFERRAL_CHARGE_TABLES +
		 REFERRAL_DISCOUNT_CHARGE_TABLES ;

	public static final String REFERRAL_CHARGE_COUNT = " SELECT count(*) FROM " + REFERRAL_ALL_CHARGE ;

	public static final String REFERRAL_CHARGES_WHERE = " WHERE  bill_status in ('F','S','C') AND "+
		" (bill_charge_status !='X')  AND  (referal_amount > 0 ) AND (ref_payment_id is null) ";

	//referrral doctor posted charges

	public static PagedList  getReferralDoctorCharges(Map filter, Map listing, String searchType)
		throws SQLException,  ParseException {
		Connection con = null;
		PagedList dataList = null;
		if (searchType.equals("allItems")){
			listing.put(LISTING.PAGENUM, 0);
			listing.put(LISTING.PAGESIZE, 0);
		}
		try{
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder	qb;
			qb = new SearchQueryBuilder(con, REFERRAL_CHARGE_FIELDS, REFERRAL_CHARGE_COUNT,
					REFERRAL_CHARGES_TABLES, REFERRAL_CHARGES_WHERE, listing);

			qb.addFilterFromParamMap(filter);
			int centerId = RequestContext.getCenterId();
			if (centerId != 0)
				qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
			
			qb.appendToQuery(" (patient_confidentiality_check(payments.patient_group,payments.mr_no) ) ");	

			qb.build();

			dataList = qb.getMappedPagedList();
		} finally {
			if (con != null) con.close();
		}
		return  dataList;
	}


	// referral doctor total posted amount
	public static BigDecimal getReferralDoctorPayment(Map filter, Map listing)
		throws SQLException , ParseException{

		Connection con = null;
		PreparedStatement psData = null;
		try{
			int centerId = RequestContext.getCenterId();
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder	qb;
			qb = new SearchQueryBuilder(con, REFERRAL_CHARGE_AMOUNT, null ,
					REFERRAL_CHARGES_TABLES, null, listing);

			qb.addFilterFromParamMap(filter);
			if (centerId != 0)
				qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
			qb.build();

			psData = qb.getDataStatement();
			return DataBaseUtil.getBigDecimalValueFromDb(psData);
		}finally{
			if ( psData != null) psData.close();
			if (con != null) con.close();
		}
	}

	public static final String REFERAL_PAYMENT_CHARGE_FIELDS = REFERRAL_CHARGE_FIELDS +
		" , pd.amount as refamount ";

	public static final String REFERAL_PAYMENT_CHARGE_TABLES = REFERRAL_CHARGE_TABLES +
		" JOIN payments_details pd ON (bc.ref_payment_id=pd.payment_id) AND "+
		" (pd.payee_name = pr.reference_docto_id  OR pd.payee_name=isr.referring_doctor) AND  "+
		" pd.voucher_no is null AND (bc.ref_payment_id is not  null)" +
		REFERRAL_DISCOUNT_CHARGE_TABLES ;

	// referral doctor paid charges

	public static PagedList getReferralDoctorPaymentCharges(Map filter, Map listing) throws SQLException,
		   ParseException {

		Connection con = null;
		PagedList dataList = null;
		try{
			int centerId = RequestContext.getCenterId();
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb;
			qb = new SearchQueryBuilder(con, REFERAL_PAYMENT_CHARGE_FIELDS, REFERRAL_CHARGE_COUNT,
					REFERAL_PAYMENT_CHARGE_TABLES,listing);
			qb.addFilterFromParamMap(filter);
			if (centerId != 0)
				qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
			qb.appendToQuery(" (patient_confidentiality_check(payments.patient_group,payments.mr_no) )");
			qb.build();

			dataList = qb.getMappedPagedList();
		}finally{
			if (con != null) con.close();
		}
		return  dataList;
	}

	// referral doctor paid total amount

	public static BigDecimal getReferralDoctorPaidCharges(Map filter, Map listing)
		throws SQLException, ParseException{

		Connection con = null;
		PreparedStatement psData = null;
		try{
			int centerId = RequestContext.getCenterId();
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb;
			qb = new SearchQueryBuilder(con, REFERRAL_CHARGE_AMOUNT, null,
					REFERAL_PAYMENT_CHARGE_TABLES, null, listing);
			qb.addFilterFromParamMap(filter);
			if (centerId != 0)
				qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
			qb.appendToQuery(" (patient_confidentiality_check(payments.patient_group,payments.mr_no) )");
			qb.build();

			psData = qb.getDataStatement();
			return DataBaseUtil.getBigDecimalValueFromDb(psData);

		}finally{
			if (psData != null) psData.close();
			if (con != null) con.close();
		}
	}


	public static final String UPDATE_BILL_CHARGE ="UPDATE bill_charge SET ref_payment_id = ? "+
		" where charge_id = ? ";

	public static final String DOC_PAYMENT_ID=" SELECT payment_id, charge_id from payments_details "+
		"	JOIN  bill_charge using(charge_id)  WHERE ref_payment_id IS NULL ";

	public static int updateRefPaymentId(Connection con)throws SQLException {
		int count = 0;
		try (PreparedStatement ps = con.prepareStatement(DOC_PAYMENT_ID);) {
			try (ResultSet rs  = ps.executeQuery();) {
  			if(!rs.next()){
  				count =0;
  			}else {
  				while (rs.next()){
  					try (PreparedStatement psc = con.prepareStatement(UPDATE_BILL_CHARGE);) {
    					psc.setString(1, rs.getString("payment_id"));
    					psc.setString(2, rs.getString("charge_id"));
    					count = psc.executeUpdate();
  					}
  				}
  			}
			}
		}
		return count;
	}


	public static final String OUT_HOUSE = "SELECT COALESCE(om.oh_id, hcm.center_id::text) AS oh_id," +
			" COALESCE(om.oh_name, hcm.center_name) AS oh_name,dom.outsource_dest_id,dom.outsource_dest_type " +
			" FROM diag_outsource_master dom "+
			" LEFT JOIN outhouse_master om ON (om.oh_id = dom.outsource_dest) "+
			" LEFT JOIN hospital_center_master hcm ON (hcm.center_id::text = dom.outsource_dest)";

	public static List getOutHouseList()throws SQLException{
		return DataBaseUtil.queryToArrayList(OUT_HOUSE);
	}

	public static final String OUTHOUSE_PAYMENT_CHARGES_FIELDS = "SELECT * ";

	public static final String OUTHOUSE_PAYMENT_CHARGES_FIELD_ALIAS = " FROM "+
		 " (SELECT COALESCE(pr.center_id, isr.center_id) AS center_id, bc.charge_id, bc.bill_no,  "+
		 " CASE WHEN bc.charge_group ='DIA' THEN bc.act_description "+
		 " ELSE bc.act_description || ' - ' || (select test_name from diagnostics where test_id =bac.act_description_id) "+
		 " END as act_description,  "+
		 " bc.amount, bc.status, bc.oh_payment_id, "+
		 " bc.out_house_amount, osd.outsource_dest_id , dom.outsource_dest as outhouse_id, b.finalized_date::date, b.bill_type, b.visit_type,  "+
		 " cgc.chargehead_name, COALESCE(pr.mr_no, isr.incoming_visit_id) AS mr_no, pr.primary_sponsor_id,pd.patient_group  ";
	public static final String OUTHOUSE_PAYMENT_CHARGES_TABLES =
		" FROM outsource_sample_details osd "+
		" JOIN tests_prescribed tp on (tp.prescribed_id=osd.prescribed_id) AND (tp.prescription_type in ('o', 'i')) "+
		" JOIN bill b ON(b.visit_id = tp.pat_id ) AND (b.status in('F','C','S')) "+
		" JOIN bill_charge bc on(b.bill_no = bc.bill_no)  AND (bc.status !='X') "+
		" JOIN bill_activity_charge bac  ON (bac.activity_id=osd.prescribed_id::varchar) AND "+
		" bc.charge_id=bac.charge_id AND payment_charge_head in('LTDIA','RTDIA') "+
		" JOIN tests_conducted tc on (tc.prescribed_id=tp.prescribed_id OR tc.prescribed_id = tp.curr_location_presc_id) "+
		" JOIN chargehead_constants cgc ON (cgc.chargegroup_id=bc.charge_group)  AND cgc.chargehead_id=bc.charge_head "+
		" LEFT JOIN patient_registration pr ON (pr.patient_id=b.visit_id) "+
		" LEFT JOIN patient_details pd On (pd.mr_no = pr.mr_no) " +
		" LEFT JOIN incoming_sample_registration isr ON(isr.incoming_visit_id = b.visit_id)"+
		" LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = osd.outsource_dest_id) ";
		
		//" LEFT JOIN outsource_names om ON(om.outsource_dest = dom.outsource_dest) " ;



	public static final String OUTHOUSE_PAYMENT_CHARGES_TABLE = OUTHOUSE_PAYMENT_CHARGES_FIELD_ALIAS
		+ OUTHOUSE_PAYMENT_CHARGES_TABLES ;

	public static final String OUTHOUSE_PAYMENT_CHARGES_COUNT = "SELECT count(charge_id) ";

	public static PagedList getOhPaymentCharges(Map filter, Map listing, String selectOption)
		throws SQLException,ParseException {
		Connection con = null;
		PagedList dataList = null;
		try{
			int centerId = RequestContext.getCenterId();
			con = DataBaseUtil.getReadOnlyConnection();
			if (selectOption.equals("allItems")){
				listing.put(LISTING.PAGENUM, 0);
				listing.put(LISTING.PAGESIZE, 0);
			}

			String OUT_HOUSE_CHARGE_TABLES = OUTHOUSE_PAYMENT_CHARGES_FIELD_ALIAS ;
			OUT_HOUSE_CHARGE_TABLES = OUT_HOUSE_CHARGE_TABLES.replace(" FROM", " FROM (SELECT * FROM ")+
				OUTHOUSE_PAYMENT_CHARGES_TABLES + ") AS ohpayments WHERE oh_payment_id is null ) AS ohpayments1";

			SearchQueryBuilder qb = new SearchQueryBuilder(con, OUTHOUSE_PAYMENT_CHARGES_FIELDS,
					OUTHOUSE_PAYMENT_CHARGES_COUNT, OUT_HOUSE_CHARGE_TABLES, listing);
			qb.addFilterFromParamMap(filter);
			if (centerId != 0)
				qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
			qb.appendToQuery(" (patient_confidentiality_check(ohpayments1.patient_group,ohpayments1.mr_no) )");
			qb.build();


			dataList = qb.getMappedPagedList();
		}finally{
			if (con != null) con.close();
		}
		return dataList;
	}

	public static BigDecimal outhouseAmount(Map filter, Map listing) throws SQLException,ParseException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			int centerId = RequestContext.getCenterId();
			con = DataBaseUtil.getReadOnlyConnection();
			listing.put(LISTING.PAGENUM, 0);
			listing.put(LISTING.PAGESIZE, 0);
			String OUT_HOUSE_CHARGE_TABLES =
				OUTHOUSE_PAYMENT_CHARGES_TABLES + "AND oh_payment_id is null) AS ohpayments  ";
			String OUTHOUSE_PAYMENT_CHARGES_TOTAL = "SELECT sum(out_house_amount) FROM "+
				" ( SELECT pr.mr_no ,COALESCE(pr.center_id, isr.center_id) AS center_id,"
				+ " bc.out_house_amount, finalized_date::date, dom.outsource_dest, pd.patient_group ";
			SearchQueryBuilder qb = new SearchQueryBuilder(con, OUTHOUSE_PAYMENT_CHARGES_TOTAL,
					OUTHOUSE_PAYMENT_CHARGES_COUNT, OUT_HOUSE_CHARGE_TABLES, listing);
			qb.addFilterFromParamMap(filter);
			if (centerId != 0)
				qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
			qb.appendToQuery(" (patient_confidentiality_check(ohpayments.patient_group,ohpayments.mr_no) )");
			qb.build();
			ps = qb.getDataStatement();
			return DataBaseUtil.getBigDecimalValueFromDb(ps);
		}finally{
			if (con != null) con.close();
		}
	}
	private static final String OUTHOUSE_PAYMENT_COUNT = "SELECT count(payment_id) ";

	public static PagedList getOhPostedCharges(Map filter, Map listing) throws SQLException, ParseException {

		Connection con = null;
		PagedList dataList = null;
		int count = 0;
		try{
			int centerId = RequestContext.getCenterId();
			con = DataBaseUtil.getReadOnlyConnection();
			String OUTHOUSE_POSTED_CHARGE_TABLES = OUTHOUSE_PAYMENT_CHARGES_TABLE +" ) AS ohpayments "+
			" JOIN payments_details pd ON pd.payment_id = ohpayments.oh_payment_id and pd.voucher_no is null  ";

			SearchQueryBuilder qb = new SearchQueryBuilder(con, OUTHOUSE_PAYMENT_CHARGES_FIELDS + ", pd.* ",
					OUTHOUSE_PAYMENT_COUNT, OUTHOUSE_POSTED_CHARGE_TABLES, listing);
			qb.addFilterFromParamMap(filter);
			if (centerId != 0)
				qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
			qb.build();

			dataList = qb.getMappedPagedList();
		}finally{
			if (con != null) con.close();
		}
		return dataList;
	}


	public static boolean deleteOhChargeItems(ArrayList<PaymentDetailsDTO> pdDTO)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		int deleted = 0;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(DELETE_CHARGE);
			Iterator<PaymentDetailsDTO> it = pdDTO.iterator();
			while(it.hasNext()){
				int i=1;
				PaymentDetailsDTO pd = it.next();
				ps.setString(i++,pd.getPaymentId());
				ps.addBatch();
				logger.debug("pd.getPaymentId"+pd.getPaymentId());
			}
			deleted = ps.executeUpdate();
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return deleted > 0	;
	}

	private static final String GET_MISCE_PAID_PAYEES= "SELECT distinct pd.payee_name, p.payee_name as payee_id "+
		" FROM payments p  "+
		" join payments_details pd on pd.payee_name=p.payee_name and p.voucher_no is not null  "+
		" where pd.payment_type='C'";

	public static ArrayList getMiscePaidPayeeList() throws SQLException{
		return DataBaseUtil.queryToArrayList(GET_MISCE_PAID_PAYEES);
	}

	private static final String GET_MISCE_DUE_PAYEES= "SELECT distinct payee_name FROM  payments_details" +
		" where voucher_no is null and amount >0 and payment_type = 'C'";

	public static ArrayList getMisceDuePayeeList() throws SQLException{
		return DataBaseUtil.queryToArrayList(GET_MISCE_DUE_PAYEES);
	}

	public static final String PRESCRIBED_DOCTORS =
		" SELECT doc.doctor_name, bc.prescribing_dr_id "+
		" FROM (SELECT DISTINCT prescribing_dr_id FROM bill_charge) as bc " +
		"  JOIN doctors doc ON (doc.doctor_id=bc.prescribing_dr_id) "+
		 " JOIN doctor_center_master dcm ON (doc.doctor_id=dcm.doctor_id)" ;

	public static final String PRESCRIBED_DOCTORS_CENTERWISE =
		" SELECT doc.doctor_name, bc.prescribing_dr_id "+
		" FROM (SELECT DISTINCT prescribing_dr_id FROM bill_charge) as bc " +
		"  JOIN doctors doc ON (doc.doctor_id=bc.prescribing_dr_id) "+
		"  JOIN doctor_center_master dcm ON(doc.doctor_id=dcm.doctor_id)" +
		"  where (dcm.center_id=0 OR dcm.center_id=?) and dcm.status='A' ";

	public static List<BasicDynaBean> getPrescribingDoctors() throws SQLException{

		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		int centerID = RequestContext.getCenterId();
		try {
			if(centerID != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1){
				ps = con.prepareStatement(PRESCRIBED_DOCTORS_CENTERWISE);
				ps.setInt(1, centerID);
				}else {
					ps = con.prepareStatement(PRESCRIBED_DOCTORS);
				}
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	/*****************************COMMON QUERYS FOR DOCTOR AND PRESCRIBING DOCTOR****************************/
	private static final String  DOCTOR_ALL_CHARGES =
		" (SELECT  b.total_amount,bc.*, b.*,b.status AS bill_status, bc.status AS bill_charge_status,chc.chargehead_name, "+
		" pr.mr_no, pr.primary_sponsor_id, pdet.patient_name, pdet.last_name, b.total_claim AS actual_claim_amt, "+
		" CASE WHEN b.bill_type='C' THEN 'Bill Later' ELSE 'Bill Now' END  as billtype, "+
		" CASE WHEN b.status='A' THEN 'Open' WHEN b.status='F' THEN 'Finalized' "+
		" WHEN b.status='S' THEN 'Settled' WHEN b.status='C' THEN 'Closed' END as billstatus,"+
		" CASE WHEN b.visit_type='i' THEN 'In Patient' WHEN b.visit_type='o' THEN 'Out Patient' "+
		" WHEN b.visit_type='d' THEN 'Diag Patient' END as visittype,"+
		" dac.disc_auth_name AS discount_auth_dr_name, dap.disc_auth_name AS discount_auth_pres_dr_name, "+
	    " daov.disc_auth_name AS overall_discount_auth_name, dar.disc_auth_name AS discount_auth_ref_name,"+
		" CASE WHEN pr.primary_sponsor_id is null THEN 'N' WHEN pr.primary_sponsor_id = '' THEN 'N' ELSE 'I' END AS insurancestatus," +
		" CASE WHEN bc.doctor_amount = 0.00 then bac.doctor_amount ELSE bc.doctor_amount END AS pay_doc_amt ," +
		" COALESCE(bc.payee_doctor_id,bac.doctor_id) AS pay_doc_id," +
		" CASE WHEN bc.charge_group='PKG' then 'Y' ELSE 'N' END AS package_charge,bac.activity_id," +
		" COALESCE(bc.doc_payment_id,bac.doctor_payment_id) AS  paymentid ";


	private static final String DOCTOR_CHARGES = "SELECT * FROM "+ DOCTOR_ALL_CHARGES ;

	private static final String DOCTOR_AMOUNT = "SELECT SUM(doctor_amount) as amount FROM "
		+ DOCTOR_ALL_CHARGES ;


	private static final String BILL_CHARGE_DISCOUNT_TABLES =
		" LEFT OUTER JOIN discount_authorizer dac ON (bc.discount_auth_dr = dac.disc_auth_id) "+
		" LEFT OUTER JOIN discount_authorizer dap ON (bc.discount_auth_pres_dr = dap.disc_auth_id) "+
		" LEFT OUTER JOIN discount_authorizer dar ON (bc.discount_auth_ref = dar.disc_auth_id) "+
		" LEFT OUTER JOIN discount_authorizer dah ON (bc.discount_auth_hosp = dah.disc_auth_id) "+
		" LEFT OUTER JOIN discount_authorizer daov ON (bc.overall_discount_auth = daov.disc_auth_id) "+
		" LEFT JOIN tpa_master tpa on tpa.tpa_id = pr.primary_sponsor_id ) as payments ";



	/***************************PRESCRIING DOCTOR PAYMENT *********************************************/
	//prescribing doctor details
	private static final String  DOCTOR_ALL_CHARGES_FOR_PRESCRIBING_DOC =
		" (SELECT  b.total_amount,bc.*, b.*,b.status AS bill_status,bc.status AS bill_charge_status, chc.chargehead_name, "+
		" pr.mr_no, pr.primary_sponsor_id, pdet.patient_name, pdet.last_name, b.total_claim AS actual_claim_amt, "+
		" CASE WHEN b.bill_type='C' THEN 'Bill Later' ELSE 'Bill Now' END  as billtype, "+
		" CASE WHEN b.status='A' THEN 'Open' WHEN b.status='F' THEN 'Finalized' "+
		" WHEN b.status='S' THEN 'Settled' WHEN b.status='C' THEN 'Closed' END as billstatus,"+
		" CASE WHEN b.visit_type='i' THEN 'In Patient' WHEN b.visit_type='o' THEN 'Out Patient' "+
		" WHEN b.visit_type='d' THEN 'Diag Patient' END as visittype, "+
		" dac.disc_auth_name AS discount_auth_dr_name, dap.disc_auth_name AS discount_auth_pres_dr_name, "+
	    " daov.disc_auth_name AS overall_discount_auth_name, dar.disc_auth_name AS discount_auth_ref_name,"+
		" CASE WHEN pr.primary_sponsor_id is null THEN 'N' WHEN pr.primary_sponsor_id = '' THEN 'N' ELSE 'I' END AS insurancestatus," +
		"  bc.doctor_amount  AS pay_doc_amt,bc.prescribing_dr_amount AS prescribing_amount,bc.referal_amount,bc.payee_doctor_id AS pay_doc_id," +
		" CASE WHEN bc.charge_group='PKG' then 'Y' ELSE 'N' END AS package_charge," +
		" bc.doc_payment_id AS  paymentid ,tpa.tpa_name ";

	private static final String BILL_CHARGE_TABLES_FOR_PRESCRIBING_DOC = "FROM bill_charge bc " +
	"JOIN bill b "+
	" ON (bc.bill_no=b.bill_no )   JOIN patient_registration pr ON (pr.patient_id=b.visit_id) "+
	" JOIN patient_details pdet on (pdet.mr_no = pr.mr_no) "+
	" JOIN chargehead_constants chc ON (chc.chargehead_id=bc.charge_head) AND (chc.payment_eligible ='Y') " ;

	public static Map getPrescribingDoctorCharges(Map filter, Map listing, String selectOption)
		throws SQLException, ParseException{

		String PRESC_DR_PAYMENT_FIELD = " SELECT * ";
		String PRESC_DR_PAYMENT_TABLES = " FROM pres_doctor_payments_view ";
		String PRESC_DR_PAYMENT_COUNT = " SELECT count(charge_id) ";
		String PRESC_DOCTOR_AMOUNT = " SELECT SUM(prescribing_dr_amount) ";
		String PRESC_DR_PAYMENT_WHERE = "WHERE prescribing_dr_payment_id is null AND prescribing_dr_amount>0";
		Map presDrPaymentMap = new HashMap();
		if (selectOption.equals("allItems")){
			listing.put(LISTING.PAGENUM, 0);
			listing.put(LISTING.PAGESIZE, 0);
		}

		int centerId = RequestContext.getCenterId();
		if (centerId != 0)
			PRESC_DR_PAYMENT_WHERE = PRESC_DR_PAYMENT_WHERE + " AND center_id = " +centerId;


		PagedList presDrPaymentList = prescribingDrPaymentQueryBuilder(filter, listing,
				PRESC_DR_PAYMENT_FIELD,	PRESC_DR_PAYMENT_COUNT, PRESC_DR_PAYMENT_TABLES,
				PRESC_DR_PAYMENT_WHERE);

		BigDecimal presDrPaymentAmount = prescribingDrAmount(filter, listing, PRESC_DOCTOR_AMOUNT , null,
				PRESC_DR_PAYMENT_TABLES, PRESC_DR_PAYMENT_WHERE);

		presDrPaymentMap.put("presDrPaymentList", presDrPaymentList);
		presDrPaymentMap.put("presDrPaymentAmount", presDrPaymentAmount);

		return  presDrPaymentMap;

	}

	public static Map  getPrescribingDrPaymentCharges(Map filter, Map listing) throws SQLException,
		   ParseException{
		String PAID_PRESCRIB_DR_FIELDS = " SELECT * ,pd.amount as doctor_paid_amount ";
		String PAID_PRESCRIB_DR_TABLE = " FROM pres_doctor_payments_view apv  "+
			" JOIN  payments_details pd on pd.charge_id=apv.charge_id AND  "+
			" pd.payment_id=prescribing_dr_payment_id ";
		String PAID_PRESCRIB_DR_COUNT = " SELECT COUNT (*) ";
		String PAID_PRESCRIB_DR_WHERE = " WHERE prescribing_dr_payment_id is not null  AND voucher_no is NULL ";
		String PRESC_DOCTOR_AMOUNT = " SELECT SUM (prescribing_dr_amount) ";

		int centerId = RequestContext.getCenterId();
		if (centerId != 0) {
			PAID_PRESCRIB_DR_WHERE = PAID_PRESCRIB_DR_WHERE + " AND center_id = "+ centerId;
		}
		Map presDrPaidPaymentMap = new HashMap();

		PagedList presDrPaidPaymentList = prescribingDrPaymentQueryBuilder(filter, listing,
				PAID_PRESCRIB_DR_FIELDS, PAID_PRESCRIB_DR_COUNT, PAID_PRESCRIB_DR_TABLE,
				PAID_PRESCRIB_DR_WHERE);

		BigDecimal presDrPaidPaymentAmount = prescribingDrAmount(filter, listing, PRESC_DOCTOR_AMOUNT , null,
				                 PAID_PRESCRIB_DR_TABLE, PAID_PRESCRIB_DR_WHERE);

		presDrPaidPaymentMap.put("presDrPaidPaymentList", presDrPaidPaymentList);
		presDrPaidPaymentMap.put("presDrPaidPaymentAmount", presDrPaidPaymentAmount);

		return  presDrPaidPaymentMap;
	}

	public static PagedList  prescribingDrPaymentQueryBuilder(Map filter, Map listing, String FIELD,
				String COUNT, String TABLES, String INITWHERE)throws SQLException, ParseException{

		Connection con = null;
		PagedList dataList = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, FIELD, COUNT, TABLES, INITWHERE, listing);
			qb.addFilterFromParamMap(filter);
			qb.build();

			dataList = qb.getMappedPagedList();
		}finally{
			if (con != null) con.close();
		}
		return dataList;
	}

	public static BigDecimal prescribingDrAmount(Map filter, Map listing, String FIELD,
				String COUNT, String TABLES, String INITWHERE)throws SQLException, ParseException{

		Connection con = null;
		PreparedStatement psData = null;
		BigDecimal presDrAmount = BigDecimal.ZERO;
		try{
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, FIELD, COUNT, TABLES, INITWHERE, listing);

			qb.addFilterFromParamMap(filter);
			qb.build();

			psData = qb.getDataStatement();
			presDrAmount = DataBaseUtil.getBigDecimalValueFromDb(psData);
			qb.close();
		}finally{
			DataBaseUtil.closeConnections(con, psData);
		}
		return presDrAmount;
	}

	/************************************DOCTOR CHARGES **************************************************/

	private static final String DOCTOR_COUNT = "SELECT count(charge_id) FROM " + DOCTOR_ALL_CHARGES ;
	private static final String DOCTOR_PAYMENT_FIELDS = DOCTOR_CHARGES +", pd.amount as doctor_paid_amount ";
	private static final String DOCTOR_PAID_AMOUNT  = DOCTOR_CHARGES +",pd.amount as doctor_paid_amount ";

	private static final String DOCTOR_PAYMENT_COUNT = "SELECT count(*) FROM" + DOCTOR_ALL_CHARGES;

	/******************************CONDUCTING DOCTOR PAYMENT ***************************************/

	private static final String ALL_DOCTORS="SELECT d.DOCTOR_ID,d.DOCTOR_NAME FROM DOCTORS d " +
									" LEFT JOIN doctor_center_master dcm ON (d.doctor_id = dcm.doctor_id)"+
									" WHERE  doctor_type!='REFERRAL' and d.status='A' and dcm.status ='A'  order by doctor_name ";

	private static final String ALL_DOCTORS_CENTERWISE="SELECT d.DOCTOR_ID,d.DOCTOR_NAME FROM DOCTORS d " +
				" LEFT JOIN doctor_center_master dcm ON (d.doctor_id = dcm.doctor_id)"+
				" WHERE  doctor_type!='REFERRAL' and d.status='A'  and dcm.status ='A' and (dcm.center_id=0 OR dcm.center_id=?) order by doctor_name ";

	public static ArrayList getAllDoctorsList()throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		int centerID = RequestContext.getCenterId();
		try {
			if(centerID != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1){
				ps = con.prepareStatement(ALL_DOCTORS_CENTERWISE);
				ps.setInt(1, centerID);
				}else {
					ps = con.prepareStatement(ALL_DOCTORS);
				}
			return DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	public static Map  getDoctorCharges(Map filter, Map listing, String selectOption)
		throws SQLException, ParseException{

		String CONDUCTING_DOCTOR_FIELDS = " SELECT * ";

		String CONDUCTING_DOCTOR_TABLES = " FROM conducting_doctor_payments_view ";

		String CONDUCTING_DOCTOR_COUNT = " SELECT count(charge_id) ";
		String CONDUCTING_DOCTOR_AMOUNT =" SELECT SUM(doctor_amount) ";
		String CONDUCTING_DOCTOR_WHERE = " WHERE doctor_payment_id is null AND doctor_amount>0 ";

		int centerId = RequestContext.getCenterId();
		if (centerId != 0)
			CONDUCTING_DOCTOR_WHERE = CONDUCTING_DOCTOR_WHERE + " AND center_id = "+centerId;

		Map doctorChargesMap = new HashMap();
		if (selectOption.equals("allItems")){
			listing.put(LISTING.PAGENUM, 0);
			listing.put(LISTING.PAGESIZE, 0);
		}
		PagedList doctorChargesList  =  paymentSeachQueryBuilder(filter, listing,  CONDUCTING_DOCTOR_FIELDS,
				CONDUCTING_DOCTOR_COUNT, CONDUCTING_DOCTOR_TABLES, CONDUCTING_DOCTOR_WHERE);

		BigDecimal doctorAmount = getDoctorPayment(filter, listing, CONDUCTING_DOCTOR_AMOUNT, null,
				               CONDUCTING_DOCTOR_TABLES, CONDUCTING_DOCTOR_WHERE);
		doctorChargesMap.put("doctorChargesList", doctorChargesList);
		doctorChargesMap.put("doctorAmount", doctorAmount);
		return doctorChargesMap;
	}


	/*public static Map  getDoctorPaymentCharges(Map filter, Map listing)throws SQLException, ParseException{

		String CONDUCTING_DOCTOR_PAID_FIELDS = " SELECT  cd.mr_no, cd.bill_no, cd.charge_id, "+
			" cd.package_charge, cd.bac_activity_id, cd.bac_activity_code, cd.doctor_payment_id, " +
			" cd.finalized_date, cd.chargehead_name, cd.act_description, cd.prescribing_dr_amount, "+
			" cd.amount, cd.referal_amount, cd.doctor_amount, cd.primary_sponsor_id, cd.patient_name, cd.last_name, "+
			" cd.discount, pd.amount as doctor_paid_amount ";

		String CONDUCTING_DOCTOR_PAID_TABLES = " FROM conducting_doctor_payments_view cd  "+
			" JOIN  payments_details pd on pd.payment_id=cd.doctor_payment_id ";

		String CONDUCTING_DOCTOR_PAID_COUNT = " SELECT COUNT (* ) ";

		String CONDUCTING_DOCTOR_PAID_WHERE = " WHERE voucher_no is NULL ";

		String PAID_DOCTOR_AMOUNT = " SELECT SUM (pd.amount) ";

		int centerId = RequestContext.getCenterId();
		if (centerId != 0)
			CONDUCTING_DOCTOR_PAID_WHERE = CONDUCTING_DOCTOR_PAID_WHERE + " AND center_id = "+centerId;

		Map doctorPaymentsMap = new HashMap();

		PagedList doctorPaymentPaidList  =  paymentSeachQueryBuilder(filter, listing,
				CONDUCTING_DOCTOR_PAID_FIELDS,	CONDUCTING_DOCTOR_PAID_COUNT, CONDUCTING_DOCTOR_PAID_TABLES,
				CONDUCTING_DOCTOR_PAID_WHERE);

		BigDecimal docPaidAmount = getDoctorPayment(filter, listing, PAID_DOCTOR_AMOUNT, null,
				CONDUCTING_DOCTOR_PAID_TABLES, CONDUCTING_DOCTOR_PAID_WHERE);

		doctorPaymentsMap.put("doctorPaymentPaidList", doctorPaymentPaidList);
		doctorPaymentsMap.put("docPaidAmount", docPaidAmount);
		return doctorPaymentsMap;

	}*/
	
	public static Map  getDoctorPaymentCharges(Map filter, Map listing)throws SQLException, ParseException{

		String CONDUCTING_DOCTOR_PAID_FIELDS = " SELECT  cd.mr_no, cd.bill_no, cd.charge_id, "+
			" cd.package_charge, cd.bac_activity_id, cd.bac_activity_code, cd.doctor_payment_id, " +
			" cd.finalized_date, chc.chargehead_name, cd.act_description, cd.prescribing_dr_amount, "+
			" cd.amount, cd.referal_amount, cd.doctor_amount, cd.primary_sponsor_id, cd.patient_name, cd.last_name,"+
			" cd.discount, pd.amount as doctor_paid_amount ";

		String CONDUCTING_DOCTOR_PAID_TABLES = " FROM (SELECT coalesce(pr.mr_no,isr.incoming_visit_id) as mr_no, bc.bill_no as bill_no," +
				 " bc.charge_id,'N' AS package_charge, NULL AS bac_activity_id, NULL AS bac_activity_code,bc.doc_payment_id AS doctor_payment_id,"+
				 " coalesce(b.finalized_date, isr.date) as finalized_date , bc.act_description, "+
				 " bc.prescribing_dr_amount,bc.amount, bc.referal_amount, bc.doctor_amount  AS doctor_amount,"+
				 " bc.discount ,pr.primary_sponsor_id,coalesce(pdet.patient_name,isr.patient_name) as patient_name,pdet.last_name,"+
				 " bc.charge_head ,coalesce(pr.center_id, isr.center_id) as center_id, bc.payee_doctor_id AS payee_doctor_id,b.visit_type, " +
				 " bc.charge_group,b.status as bill_status,  b.closed_date, (CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END) AS insurancestatus " +
				 " FROM bill_charge bc " +
				 " JOIN bill b ON (bc.bill_no = b.bill_no)" +
				 " LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)" +
				 " LEFT JOIN patient_details pdet ON (pdet.mr_no=pr.mr_no)" +
				 " LEFT JOIN  incoming_sample_registration isr on (isr.billno=b.bill_no)" +
				 " WHERE b.status IN ('F','C') AND bc.status <> 'X' " +
				 " UNION ALL" +
				 " SELECT   coalesce(pr.mr_no,isr.incoming_visit_id) as mr_no, bc.bill_no,   bc.charge_id,'Y'  AS package_charge," +
				 " bac.activity_id AS bac_activity_id, bac.activity_code as bac_activit_code,bac.doctor_payment_id AS doctor_payment_id," +
				 " b.finalized_date,bc.act_description,bc.prescribing_dr_amount,bc.amount,bc.referal_amount," +
				 " coalesce(bac.doctor_amount,0) AS doctor_amount," +
				 " bc.discount,pr.primary_sponsor_id,coalesce(pdet.patient_name,isr.patient_name) as patient_name,pdet.last_name, " +
				 " bc.charge_head , coalesce(pr.center_id, isr.center_id) as center_id, bc.payee_doctor_id AS payee_doctor_id, b.visit_type, " +
				 " bc.charge_group,b.status as bill_status, b.closed_date, (CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END) AS insurancestatus "+
				 " FROM bill_charge bc" +
				 " JOIN bill b ON (bc.bill_no = b.bill_no)" +
				 " LEFT JOIN bill_activity_charge bac ON (bac.charge_id = bc.charge_id)" +
				 " LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)" +
				 " LEFT JOIN patient_details pdet ON (pdet.mr_no=pr.mr_no)" +
				 " LEFT JOIN  incoming_sample_registration isr on (isr.billno=b.bill_no)" +
				 " WHERE b.status IN ('F','C') AND bc.status <> 'X' and bc.charge_group = 'PKG' ) cd  "+
				 " JOIN chargehead_constants chc ON (chc.chargehead_id = cd.charge_head)"+
				 " JOIN  payments_details pd on pd.payment_id=cd.doctor_payment_id  ";

		String CONDUCTING_DOCTOR_PAID_COUNT = " SELECT COUNT (* ) ";

		String CONDUCTING_DOCTOR_PAID_WHERE = " WHERE voucher_no is NULL ";

		String PAID_DOCTOR_AMOUNT = " SELECT SUM (pd.amount) ";

		int centerId = RequestContext.getCenterId();
		if (centerId != 0)
			CONDUCTING_DOCTOR_PAID_WHERE = CONDUCTING_DOCTOR_PAID_WHERE + " AND center_id = "+centerId;

		Map doctorPaymentsMap = new HashMap();

		PagedList doctorPaymentPaidList  =  paymentSeachQueryBuilder(filter, listing,
				CONDUCTING_DOCTOR_PAID_FIELDS,	CONDUCTING_DOCTOR_PAID_COUNT, CONDUCTING_DOCTOR_PAID_TABLES,
				CONDUCTING_DOCTOR_PAID_WHERE);

		BigDecimal docPaidAmount = getDoctorPayment(filter, listing, PAID_DOCTOR_AMOUNT, null,
				CONDUCTING_DOCTOR_PAID_TABLES, CONDUCTING_DOCTOR_PAID_WHERE);

		doctorPaymentsMap.put("doctorPaymentPaidList", doctorPaymentPaidList);
		doctorPaymentsMap.put("docPaidAmount", docPaidAmount);
		return doctorPaymentsMap;

	}

	public static final String TEST_CHARGES ="SELECT test_id as activity_id, test_name as activity_name "+
		" FROM diagnostics d JOIN diagnostics_departments dd USING(ddept_id) "+
		" WHERE d.status='A' AND dd.status='A' AND category=? ";

	public static final String SERVICES = "SELECT service_id as activity_id, service_name as activity_name "+
		"FROM services  WHERE status='A' ";

	public static List getChargeHeadValues(String category, String screenType) throws SQLException {
		try (Connection con = DataBaseUtil.getReadOnlyConnection();
		    PreparedStatement ps = con.prepareStatement(TEST_CHARGES);) {
			
			ps.setString(1, category);
			return DataBaseUtil.queryToDynaList(ps);
		}
	}

	public static List getServiceList(String screenType)throws SQLException{
		return DataBaseUtil.queryToDynaList(SERVICES);
	}

	public static PagedList paymentSeachQueryBuilder(Map filter, Map listing, String FILEDS, String COUNT,
			String TABLES, String INITWHERE) throws SQLException , ParseException{
		Connection con = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb = new SearchQueryBuilder(con, FILEDS, COUNT, TABLES,
					INITWHERE, listing);
			qb.addFilterFromParamMap(filter);

			qb.build();

			PagedList list = qb.getMappedPagedList();
			return list;


		}finally{
			if (con != null) con.close();
		}

	}

	public static BigDecimal getDoctorPayment(Map filter, Map listing, String FILEDS, String COUNT,
			String TABLES, String INITWHERE) throws SQLException, ParseException{
		Connection con = null;
		PreparedStatement psData = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb = new SearchQueryBuilder(con, FILEDS, COUNT, TABLES,
					INITWHERE,(String)listing.get(LISTING.SORTCOL),listing);

			qb.addFilterFromParamMap(filter);

			qb.build();

			psData = qb.getDataStatement();

			return DataBaseUtil.getBigDecimalValueFromDb(psData);

		}finally{
			if (psData != null) psData.close();
			if (con != null) con.close();
		}

	}

	/****************************************************************************************
	 * 						Conducting doctor payments end here							    *
	****************************************************************************************/

	/****************************************************************************************
	 *                                Payment Reversal										*
	 ****************************************************************************************/
  public static final String MISC_PAYEES = "SELECT misc_payee_name AS payee_name, misc_payee_name AS payee_id, 'C' AS payment_type FROM misc_payees ";

  public static final String DOCTOR_PAYEES = " SELECT DISTINCT doctor_name AS payee_name, d.doctor_id AS payee_id, 'D' AS payment_type FROM doctors d "
      + "   LEFT JOIN doctor_center_master dcm ON(d.doctor_id = dcm.doctor_id)"
      + "   WHERE d.status='A' and dcm.status='A' and (dcm.center_id=0 OR dcm.center_id=?)";

  public static final String PRESC_DOCTOR_PAYEES = "SELECT DISTINCT doctor_name AS payee_name, doctor_id AS payee_id, "
      + "'P' AS payment_type FROM doctors pdoc  JOIN bill_charge bc ON pdoc.doctor_id = bc.prescribing_dr_id  ";

  public static final String REFERRAL_DOCTOR_PAYEES = "SELECT DISTINCT referal_name AS payee_name, referal_no AS payee_id, "
      + "'F' AS payment_type FROM referral WHERE status='A' ";

  public static final String DOC_AS_REF_DOCTOR_PAYEES = "SELECT DISTINCT doctor_name AS payee_name, doctor_id AS payee_id,"
      + " 'R' AS payment_type FROM doctors rd JOIN patient_registration pr ON pr.reference_docto_id=rd.doctor_id  WHERE rd.status='A'  ";

  public static final String OUT_HOUSE_PAYEES = "SELECT DISTINCT oh_name as payee_name, oh_id as payee_id "
      + " ,'O' as payment_type FROM  outhouse_master where status='A' ";

  public static final String SUPPLIER_PAYEES = "SELECT DISTINCT supplier_name AS payee_name, "
      + "supplier_code AS payee_id,'S' AS payment_type FROM supplier_master WHERE status = 'A' ";
	
  public static List getPaidPayeesList(String payeeType) throws SQLException {

    if (payeeType == null || "".equals(payeeType)) {
      return new ArrayList<>();
    }
    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getConnection();
    try {
      if ("C".equals(payeeType)) {
        ps = con.prepareStatement(MISC_PAYEES);

      } else if ("D".equals(payeeType)) {
        int centerID = RequestContext.getCenterId();
        ps = con.prepareStatement(DOCTOR_PAYEES);
        ps.setInt(1, centerID);

      } else if ("P".equals(payeeType)) {
        ps = con.prepareStatement(PRESC_DOCTOR_PAYEES);

      } else if ("F".equals(payeeType)) {
        ps = con.prepareStatement(REFERRAL_DOCTOR_PAYEES);

      } else if ("R".equals(payeeType)) {
        ps = con.prepareStatement(DOC_AS_REF_DOCTOR_PAYEES);

      } else if ("O".equals(payeeType)) {
        ps = con.prepareStatement(OUT_HOUSE_PAYEES);

      } else if ("S".equals(payeeType)) {
        ps = con.prepareStatement(SUPPLIER_PAYEES);

      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

	public static final String PAYMENTS_COUNT = "SELECT SUM(pd.amount) , " +
	" CASE WHEN pd.payment_type='C' THEN "+
	" 'Cash Voucher' " +
	" WHEN pd.payment_type='P' THEN 'Prescribing Doctor Payments' "+
	" WHEN pd.payment_type='D' THEN 'Doctor Payments' WHEN pd.payment_type IN ('R','F') "+
	" THEN  'Referral Payments' WHEN pd.payment_type='O' THEN 'Out Test Payments' WHEN "+
	" pd.payment_type='S' THEN 'Supplier Payments' END AS payment_type "+
	" FROM payments_details pd  "+
	" INNER JOIN payments p "+
	" ON p.voucher_no=pd.voucher_no "+
	" WHERE p.date::date BETWEEN ? AND  ?  AND p.voucher_category='P' "+
	" GROUP BY payment_type "+
	" ORDER BY payment_type  ";
	public static List getPaymentsCount(java.sql.Date from, java.sql.Date to)
	throws SQLException {
		return  DataBaseUtil.queryToDynaListDates(
				PAYMENTS_COUNT,
				from, to);
	}

	public static final String PAYMENT_REVERSALS_COUNT = "SELECT 0::NUMERIC-SUM(pd.amount) AS sum, CASE WHEN pd.payment_type='C' THEN "+
		" 'Cash Voucher' " +
		" WHEN pd.payment_type='P' THEN 'Prescribing Doctor Payments' "+
		" WHEN pd.payment_type='D' THEN 'Doctor Payments' WHEN pd.payment_type IN ('R','F') "+
		" THEN  'Referral Doctor Payment' WHEN pd.payment_type='O' THEN 'Out Test Payments' WHEN  "+
		" pd.payment_type='S' THEN 'Supplier Payments' END AS payment_type "+
		" FROM payments_details pd "+
		" INNER JOIN payments p "+
		" ON p.voucher_no=pd.voucher_no "+
		" WHERE  p.date::date BETWEEN ? AND ?  AND p.voucher_category='R' "+
		" GROUP BY payment_type "+
		" ORDER BY payment_type ";
	public static List getPaymentReversalCount(java.sql.Date from, java.sql.Date to)
	throws SQLException {
		return  DataBaseUtil.queryToDynaListDates(
				PAYMENT_REVERSALS_COUNT,
				from, to);
	}

	/*
	 * Payments Summary (count and total) between two dates: used by CFD
	 */

	public static final String PAYMENTS_SUMMARY_TOTAL =
		" SELECT payment_type, sum(amount) AS amt, count(*) as count " +
		" FROM payments p "+
		" LEFT JOIN counters cs  ON cs.counter_id = p.counter "+
		" WHERE p.date::date BETWEEN ? AND ? " ;
	public static final String PAYMENTS_SUMMARY_GROUPBY =
		" GROUP BY payment_type ";

	public static List getPaymentsSummaryTotal(java.sql.Date from, java.sql.Date to, int centerId)
		throws SQLException {
		 String query = centerId == 0? PAYMENTS_SUMMARY_TOTAL + PAYMENTS_SUMMARY_GROUPBY :
			 PAYMENTS_SUMMARY_TOTAL+ " AND COALESCE(cs.center_id,"+ centerId +") = "+ centerId + PAYMENTS_SUMMARY_GROUPBY;
		return  DataBaseUtil.queryToDynaListDates(query, from, to);
	}

	public static final String ACCOUNT_HEADS = "SELECT * from bill_account_heads where status = 'A' order by account_head_name ";

	public static List getAccountHeads() throws SQLException{
		return DataBaseUtil.queryToDynaList(ACCOUNT_HEADS);
	}

	/*
	 * All Doctor Payments (prescribing, conducting, referral) total between finalized dates; Used by CFD
	 */
	public static final String COND_CHARGES_TOTAL_NORMAL =
		" SELECT coalesce(sum(bc.doctor_amount),0) " +
		" FROM bill_charge bc " +
		"   JOIN bill b USING (bill_no) " +
		" WHERE b.status IN ('F','S','C') AND bc.status != 'X' AND bc.charge_head != 'PKGPKG' " +
		"   AND b.finalized_date::date BETWEEN ? AND ?";

	private static final String COND_CHARGES_TOTAL_PKG =
		" SELECT coalesce(sum( bac.doctor_amount),0) " +
		" FROM bill_activity_charge bac " +
		"   JOIN bill_charge bc USING (charge_id) " +
		"   JOIN bill b using (bill_no) " +
		" WHERE b.status IN ('F','S','C') AND bc.status != 'X' AND bc.charge_head = 'PKGPKG' " +
		"   AND b.finalized_date::date BETWEEN ? AND ?";

	public static final String COND_CHARGES_TOTAL_NORMAL_CENTER =
		" SELECT coalesce(sum(bc.doctor_amount),0) " +
		" FROM bill_charge bc " +
		"   JOIN bill b USING (bill_no) " +
		 " LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id "
		+ " LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id "
		+ " LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = b.visit_id"+
		" WHERE b.status IN ('F','S','C') AND bc.status != 'X' AND bc.charge_head != 'PKGPKG' " +
		"   AND b.finalized_date::date BETWEEN ? AND ? AND COALESCE(pr.center_id,isr.center_id, prc.center_id) = ?";

	private static final String COND_CHARGES_TOTAL_PKG_CENTER =
		" SELECT coalesce(sum( bac.doctor_amount),0) " +
		" FROM bill_activity_charge bac " +
		"   JOIN bill_charge bc USING (charge_id) " +
		"   JOIN bill b using (bill_no) " +
		 " LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id "
		+ " LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id "
		+ " LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = b.visit_id"+
		" WHERE b.status IN ('F','S','C') AND bc.status != 'X' AND bc.charge_head = 'PKGPKG' " +
		"   AND b.finalized_date::date BETWEEN ? AND ? AND COALESCE(pr.center_id,isr.center_id, prc.center_id) = ? ";

	public static BigDecimal getConductingDrChargesTotal(java.sql.Date from, java.sql.Date to, int centerId)
		throws SQLException {
		if(centerId == 0) {
			BigDecimal normal = DataBaseUtil.getBigDecimalValueFromDbDates(COND_CHARGES_TOTAL_NORMAL, from, to);
			BigDecimal pkg = DataBaseUtil.getBigDecimalValueFromDbDates(COND_CHARGES_TOTAL_PKG, from, to);
			return normal.add(pkg);
		} else {
			Connection con = null;
			PreparedStatement ps = null;
			try{
				con = DataBaseUtil.getConnection();

				ps = con.prepareStatement(PRES_CHARGES_TOTALS_FOR_CENTER);
				ps.setDate(1, from);
				ps.setDate(2, to);
				ps.setInt(3,  centerId);
				BigDecimal normal = DataBaseUtil.getBigDecimalValueFromDb(ps);
				ps = con.prepareStatement(COND_CHARGES_TOTAL_PKG_CENTER);
				ps.setDate(1, from);
				ps.setDate(2, to);
				ps.setInt(3,  centerId);
				BigDecimal pkg = DataBaseUtil.getBigDecimalValueFromDb(ps);
				return normal.add(pkg);
			} finally{
				DataBaseUtil.closeConnections(con, ps);
			}
		}
	}

	public static final String PRES_CHARGES_TOTALS =
		" SELECT COALESCE(SUM(prescribing_dr_amount),0) " +
		" FROM bill_charge bc " +
		"   JOIN bill b ON (b.bill_no = bc.bill_no AND b.status IN ('F','S','C') AND bc.status != 'X' ) " +
		" WHERE b.finalized_date::date BETWEEN ? AND ?";

	public static final String PRES_CHARGES_TOTALS_FOR_CENTER =
		" SELECT COALESCE(SUM(prescribing_dr_amount),0) " +
		" FROM bill_charge bc " +
		"   JOIN bill b ON (b.bill_no = bc.bill_no AND b.status IN ('F','S','C') AND bc.status != 'X') " +
		 "  JOIN patient_registration pr ON pr.patient_id = b.visit_id "
		+ " LEFT JOIN store_retail_customers prc ON prc.customer_id = pr.patient_id "
		+ " LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = b.visit_id"+
		" WHERE b.finalized_date::date BETWEEN ? AND ? AND COALESCE(pr.center_id,isr.center_id, prc.center_id) = ? ";

	public static BigDecimal getPrescDrChargesTotal(java.sql.Date from, java.sql.Date to, int centerId)
		throws SQLException {
			if(centerId == 0) {
				return DataBaseUtil.getBigDecimalValueFromDbDates(PRES_CHARGES_TOTALS, from, to);
			} else {
				Connection con = null;
				PreparedStatement ps = null;
				try{
					con = DataBaseUtil.getConnection();

					ps = con.prepareStatement(PRES_CHARGES_TOTALS_FOR_CENTER);
					ps.setDate(1, from);
					ps.setDate(2, to);
					ps.setInt(3,  centerId);
					return DataBaseUtil.getBigDecimalValueFromDb(ps);
				} finally{
					DataBaseUtil.closeConnections(con, ps);
				}

			}
	}

	public static final String REFERRAL_CHARGES_TOTALS =
		" SELECT COALESCE(SUM(referal_amount),0) " +
		" FROM bill_charge bc " +
		"   JOIN bill b USING (bill_no) " +
		" WHERE b.status IN ('F','S','C') AND bc.status != 'X' " +
		"   AND b.finalized_date::date BETWEEN ? AND ?";

	public static final String REFERRAL_CHARGES_TOTALS_CENTER =
		" SELECT COALESCE(SUM(referal_amount),0) " +
		" FROM bill_charge bc " +
		"   JOIN bill b USING (bill_no) " +
		 " LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id "
		+ " LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id "
		+ " LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = b.visit_id"+
		" WHERE b.status IN ('F','S','C') AND bc.status != 'X' " +
		"   AND b.finalized_date::date BETWEEN ? AND ? AND COALESCE(pr.center_id,isr.center_id, prc.center_id)= ? ";

	public static BigDecimal getReferralDrChargesTotal(java.sql.Date from, java.sql.Date to, int centerId)
		throws SQLException {
		if(centerId == 0) {
			return DataBaseUtil.getBigDecimalValueFromDbDates(REFERRAL_CHARGES_TOTALS, from, to);
		} else {
			Connection con = null;
			PreparedStatement ps = null;
			try{
				con = DataBaseUtil.getConnection();

				ps = con.prepareStatement( REFERRAL_CHARGES_TOTALS_CENTER);
				ps.setDate(1, from);
				ps.setDate(2, to);
				ps.setInt(3,  centerId);
				return DataBaseUtil.getBigDecimalValueFromDb(ps);
			} finally{
				DataBaseUtil.closeConnections(con, ps);
			}

		}
	}

	public static final String OUT_HOUSE_CHARGE_TOTALS_CENTER =
		" SELECT sum(bc.out_house_amount) AS oh_amount, oh_name "+
		" FROM outsource_sample_details osd " +
		"  JOIN tests_prescribed tp ON (tp.prescribed_id=osd.prescribed_id) " +
		"  JOIN tests_conducted tc ON (tc.prescribed_id=osd.prescribed_id) " +
		"  JOIN bill_activity_charge bac ON  " +
		"    (bac.activity_id=osd.prescribed_id::varchar AND bac.activity_code = 'DIA') " +
		"  JOIN bill_charge bc ON ( bc.charge_id = bac.charge_id AND bc.out_house_amount > 0 ) " +
		"  JOIN bill b ON(b.bill_no = bc.bill_no AND finalized_date::date BETWEEN ? AND ?) " +
		"  JOIN diag_outsource_master dom ON (dom.outsource_dest_id = osd.outsource_dest_id) "+
		"  JOIN outhouse_master ON dom.outsource_dest=oh_id " +
		 " LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id "
		+ " LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id "
		+ " LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = b.visit_id"+
		" WHERE	(b.status in('F','C','S')) AND (bc.status !='X') " +
		"   AND COALESCE(pr.center_id,isr.center_id, prc.center_id)= ? " +
		" GROUP BY oh_name ";

	public static final String OUT_HOUSE_CHARGE_TOTALS =
		" SELECT sum(bc.out_house_amount) AS oh_amount, oh_name "+
		" FROM outsource_sample_details osd " +
		"  JOIN tests_prescribed tp ON (tp.prescribed_id=osd.prescribed_id) " +
		"  JOIN tests_conducted tc ON (tc.prescribed_id=osd.prescribed_id) " +
		"  JOIN bill_activity_charge bac ON  " +
		"    (bac.activity_id=osd.prescribed_id::varchar AND bac.activity_code = 'DIA') " +
		"  JOIN bill_charge bc ON (bc.charge_id = bac.charge_id AND bc.out_house_amount > 0 ) " +
		"  JOIN bill b ON(b.bill_no = bc.bill_no  AND finalized_date::date BETWEEN ? AND ? ) " +
		"  JOIN diag_outsource_master dom ON (dom.outsource_dest_id = osd.outsource_dest_id) "+
		"  JOIN outhouse_master ON dom.outsource_dest=oh_id " +
		" WHERE	(b.status in('F','C','S')) AND (bc.status !='X') " +
		" GROUP BY oh_name ";

	public static List getOutHouseChargesTotal(java.sql.Date from, java.sql.Date to, int centerId)
	throws SQLException {
		if(centerId == 0) {
			return DataBaseUtil.queryToDynaListDates(OUT_HOUSE_CHARGE_TOTALS, from, to);
		} else {
			Connection con = null;
			PreparedStatement ps = null;
			try{
				con = DataBaseUtil.getConnection();
				ps = con.prepareStatement(OUT_HOUSE_CHARGE_TOTALS_CENTER);
				ps.setDate(1, from);
				ps.setDate(2, to);
				ps.setInt(3,  centerId);
				return DataBaseUtil.queryToDynaList(ps);
			} finally{
				DataBaseUtil.closeConnections(con, ps);
			}

		}
	}

	public static BigDecimal getSuppliersAmount(Map filter, Map listing) throws SQLException, ParseException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			String[] invoiceType = (String[])filter.get("_dateType");
			String[] fromDate = (String[]) filter.get("_fromDate");
			String[] toDate = (String[]) filter.get("_toDate");
			String[] isCashPurchase = (String[]) filter.get("_cash_purchase");

			con = DataBaseUtil.getReadOnlyConnection();
			String SUPPLIER_PAYMENT_AMOUNT = "SELECT sum(final_amt) ";
			SearchQueryBuilder sqb = new SearchQueryBuilder(con, SUPPLIER_PAYMENT_AMOUNT, COUNT_QUERY,
					                    SUPPLIER_PAYMENT_TABLE, SUPPLIER_PAYMENT_WHERE, listing);

			if (invoiceType[0].equals("invoice")){
				sqb.appendToQuery("invoice_date >='"+DateUtil.parseDate(fromDate[0])+"' AND invoice_date <= '"+DateUtil.parseDate(toDate[0])+"'");
			}else if(invoiceType[0].equals("due")) {
				sqb.appendToQuery("due_date >= '"+DateUtil.parseDate(fromDate[0])+"' AND due_date <= '"+DateUtil.parseDate(toDate[0])+"'");
			}
			if (isCashPurchase[0].equals("Y"))
				sqb.appendToQuery("cash_purchase = '"+isCashPurchase[0]+"'");

			sqb.addFilterFromParamMap(filter);
            sqb.build();
			ps = sqb.getDataStatement();
			return DataBaseUtil.getBigDecimalValueFromDb(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String PAYMENT_DETAIL_VOUCHER = "SELECT *  FROM payment_voucher_details_view "+
		" WHERE voucher_no = ? ORDER BY conducted_date::date, mr_no";

	public static List getPaymentVoucherBreakup(String voucherNo)throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(PAYMENT_DETAIL_VOUCHER);
			ps.setString(1, voucherNo);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	public static final String PAYMENT_SUMMARY_VOUCHER = " SELECT pd.payment_type, pv.payee_name as name, "+
		" p.payment_mode_id, pm.payment_mode, p.card_type_id, cm.card_type, "+
		" pd.category, p.voucher_no, p.date, p.tax_amount, p.tds_amount, p.bank, p.reference_no, "+
		" sum(pd.amount) as amount,(p.amount)  as totalamount, count(p.voucher_no) as count, "+
		" voucher_category,CASE WHEN voucher_category='R' THEN 'Receipt Voucher'else 'Payment Voucher'end as"+
		" form_title,p.remarks, p.username " +
		" FROM  payments p " +
		" JOIN payments_details pd ON (pd.voucher_no=p.voucher_no and "+
		" pd.payee_name=p.payee_name) " +
		" JOIN payee_names_view_for_voucher pv on pv.payee_id = p.payee_name "+
		" JOIN payment_mode_master pm ON (pm.mode_id = p.payment_mode_id) " +
		" LEFT JOIN card_type_master cm ON (cm.card_type_id = p.card_type_id) " +
		" WHERE p.voucher_no = ? GROUP BY p.voucher_no, p.date, p.tax_amount, pd.category," +
		" p.payment_mode_id, pm.payment_mode, p.card_type_id, cm.card_type, pv.payee_name,"+
		" pd.payee_name, p.bank, p.reference_no, pd.payment_type, p.tds_amount, p.amount, voucher_category,p.remarks,p.username ";

	public static List getPaymentVoucherSummary(String voucherNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(PAYMENT_SUMMARY_VOUCHER);
			ps.setString(1, voucherNo);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	public static final String SUPPLIER_PAYMENT_VOUCHER =" SELECT voucher_no, pd.amount, pd.payee_name, "+
		" posted_date as date, COALESCE(pi.invoice_no, pdn.debit_note_no, "+
		" 'Direct Payment') as invoice_no, COALESCE(pi.supplier_id, "+
		" pdn.supplier_id) AS supplier_id,pd.payment_id,sm.supplier_name as name, (p.amount) as totalamount, "+
		" p.payment_mode_id, pm.payment_mode, p.card_type_id, cm.card_type, reference_no, "+
		" bank, coalesce(pi.invoice_date,pdn.debit_note_date,posted_date) as inv_deb_date, p.remarks, p.username "+
		" FROM payments_details pd  " +
		" JOIN payments p using (voucher_no) "+
		" LEFT JOIN store_invoice pi ON (pd.payment_id = pi.payment_id) "+
		" LEFT JOIN store_debit_note pdn ON (pdn.payment_id = pd.payment_id) "+
		" JOIN supplier_master sm ON (sm.supplier_code = pd.payee_name) "+
		" JOIN payment_mode_master pm ON (pm.mode_id = p.payment_mode_id) " +
		" LEFT JOIN card_type_master cm ON (cm.card_type_id = p.card_type_id) " +
		" WHERE voucher_no = ? ORDER BY inv_deb_date, payment_id " ;

	public static List getSupplierPaymentVoucher(String voucherNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(SUPPLIER_PAYMENT_VOUCHER);
			ps.setString(1, voucherNo);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	public static final String PAYMENT_REVERSAL_DETAILS = "SELECT pay.voucher_no, pay.posted_date as date, "+
		" (0-p.amount) as voucher_amount, pay.payee_name as name, pay.description, pay.category, "+
		" (0-pay.amount)as amount,p.tax_amount,p.tds_amount,  p.bank, p.reference_no, "+
		" p.payment_mode_id, pm.payment_mode, p.card_type_id, cm.card_type, "+
		" (CASE WHEN  pay.payment_type in ('D','P') THEN "+
		" (SELECT doctor_name FROM doctors WHERE doctor_id=pay.payee_name) WHEN pay.payment_type='R' THEN "+
		" (SELECT doctor_name FROM doctors WHERE doctor_id=pay.payee_name) WHEN pay.payment_type= 'F' THEN "+
		" (SELECT referal_name FROM referral where referal_no=pay.payee_name) WHEN pay.payment_type='O' THEN"+
		" (SELECT oh_name FROM  outhouse_master where oh_id=pay.payee_name) WHEN pay.payment_type='S' THEN "+
		" (SELECT supplier_name FROM  supplier_master where supplier_code=pay.payee_name)  "+
		" WHEN pay.payment_type='C' THEN pay.payee_name  ELSE pay.payee_name END ) as payee_name,p.remarks, p.username "+
		" FROM  payments_details pay " +
		" JOIN payments p using (voucher_no) " +
		" JOIN payment_mode_master pm ON (pm.mode_id = p.payment_mode_id) " +
		" LEFT JOIN card_type_master cm ON (cm.card_type_id = p.card_type_id) " +
		" WHERE voucher_no= ? order by posted_date";

	public static List getPaymentReversalDetails(String voucherNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(PAYMENT_REVERSAL_DETAILS);
			ps.setString(1, voucherNo);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String MISC_PAYMENT_VOUCHER_DETAILS =
		  " SELECT pd.payment_type, pv.payee_name as name, p.payment_mode_id, pm.payment_mode, p.card_type_id, " +
		  " cm.card_type, pd.category, p.voucher_no, p.date, p.tax_amount,  "+
		  " p.tds_amount, p.bank, p.reference_no, pd.description, (p.amount)  as totalamount, pd.amount, "+
		  " voucher_category, CASE WHEN voucher_category='R' THEN 'Receipt Voucher'else 'Payment Voucher' "+
		  " END AS form_title,p.remarks, p.username " +
		  " FROM  payments p " +
		  " JOIN payments_details pd ON (pd.voucher_no=p.voucher_no and pd.payee_name=p.payee_name) " +
		  " JOIN  payee_names_view_for_voucher pv on pv.payee_id = p.payee_name "+
		  " JOIN payment_mode_master pm ON (pm.mode_id = p.payment_mode_id) " +
		  " LEFT JOIN card_type_master cm ON (cm.card_type_id = p.card_type_id) " +
		  " WHERE p.voucher_no = ? order by p.date";


	public static List getMiscPaymentVoucherDetails(String voucherNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(MISC_PAYMENT_VOUCHER_DETAILS);
			ps.setString(1, voucherNo);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	public static final String PAYMENT_DESCRIPTION = "SELECT description from payments_details "+
		" WHERE voucher_no is null AND description ILIKE ?";
	
	public static List getDescription(String findString, int limit)throws SQLException {
    if ( (findString == null) || (findString.length() < 2)){
      return null;
    }
		try(Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(PAYMENT_DESCRIPTION);){
			if ((findString == null) || (findString.length() < 2)){
				return null;
			}
			ps.setString(1, "%" + findString + "%");
			return DataBaseUtil.queryToDynaList(ps);
		}
	}

	public static final String PAYMENT_CATEGORY = "SELECT category from payments_details "+
		" WHERE voucher_no is null AND category ILIKE ?" ;
	public static List getCategory(String findString, int limit)throws SQLException {
    if ( (findString == null) || (findString.length() < 2)){
      return null;
    }
		try(Connection con = DataBaseUtil.getReadOnlyConnection();
		    PreparedStatement ps = con.prepareStatement(PAYMENT_CATEGORY);){
      ps.setString(1, "%" + findString + "%");
			return DataBaseUtil.queryToDynaList(ps);
		}

	}

	private static final String GET_DOCTOR_DEPT_LIST = "SELECT d.doctor_id,d.doctor_name, dpt.dept_name, dpt.dept_id, d.doctor_license_number  " +
							" FROM doctors d " +
							" LEFT JOIN doctor_center_master dcm ON (d.doctor_id = dcm.doctor_id)"+
							" JOIN department dpt USING(dept_id) WHERE d.status = 'A' and dcm.status='A' " +
							" ORDER BY doctor_name   " ;

	private static final String GET_DOCTOR_DEPT_LIST_CENTERWISE = "SELECT d.doctor_id,d.doctor_name, dpt.dept_name, dpt.dept_id, d.doctor_license_number  " +
							" FROM doctors d " +
							" LEFT JOIN doctor_center_master dcm ON (d.doctor_id = dcm.doctor_id)"+
							" JOIN department dpt USING(dept_id) WHERE d.status = 'A' and dcm.status='A' and (dcm.center_id=0 OR dcm.center_id=?) " +
							" ORDER BY doctor_name   " ;

	/*
	* This method gets the patient doctor-dept details union of all active doctors-dept details
	*/
	public static List<BasicDynaBean> getDoctorDeptList() throws SQLException {
		PreparedStatement ps = null;
		List<BasicDynaBean> doctorDeptList = null;
		Connection con = null;
		int centerID = RequestContext.getCenterId();
		try {
				con = DataBaseUtil.getConnection();
				if(centerID != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
					ps = con.prepareStatement(GET_DOCTOR_DEPT_LIST_CENTERWISE);
					ps.setInt(1,centerID);
				} else {
					ps = con.prepareStatement(GET_DOCTOR_DEPT_LIST);
				}
				doctorDeptList = DataBaseUtil.queryToDynaList(ps);
			} catch (SQLException e) {
				logger.error("", e);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
		}
		return doctorDeptList;
	}

	public static final String PRESCRIBED_DOCTOR_DEPT_LIST =
		" SELECT doc.doctor_name, bc.prescribing_dr_id, doc.dept_id, dep.dept_name, doctor_license_number " +
		" FROM (SELECT DISTINCT prescribing_dr_id FROM bill_charge) as bc " +
		"  JOIN doctors doc ON (doc.doctor_id=bc.prescribing_dr_id) " +
		"  JOIN department dep USING(dept_id) ";

	public static final String PRESCRIBED_DOCTOR_DEPT_LIST_CENTERWISE =
		" SELECT doc.doctor_name, bc.prescribing_dr_id, doc.dept_id, dep.dept_name, doctor_license_number " +
		" FROM (SELECT DISTINCT prescribing_dr_id FROM bill_charge) as bc " +
		"  JOIN doctors doc ON (doc.doctor_id=bc.prescribing_dr_id) " +
		"  JOIN doctor_center_master dcm ON(doc.doctor_id=dcm.doctor_id)"+
		"  JOIN department dep USING(dept_id)" +
		" where (dcm.center_id=0 OR dcm.center_id=?) and dcm.status='A' ";

	public static List<BasicDynaBean> getPresDoctorDeptList() throws SQLException {
		PreparedStatement ps = null;
		List<BasicDynaBean> doctorDeptList = null;
		Connection con = null;
		int centerID = RequestContext.getCenterId();
		try {
				con = DataBaseUtil.getConnection();
				if(centerID != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
					ps = con.prepareStatement(PRESCRIBED_DOCTOR_DEPT_LIST_CENTERWISE);
					ps.setInt(1, centerID);
				}else {
					ps = con.prepareStatement(PRESCRIBED_DOCTOR_DEPT_LIST);
				}
				doctorDeptList = DataBaseUtil.queryToDynaList(ps);
			} catch (SQLException e) {
				logger.error("", e);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
		}
		return doctorDeptList;
	}

	public static boolean saveAutoPayments(ArrayList<PaymentDetailsDTO> paymentDetails)throws SQLException {

		PreparedStatement ps = null;
		boolean success = true;
		String payId = null;
		Connection con = null;

		Iterator<PaymentDetailsDTO> it = paymentDetails.iterator();
			while(it.hasNext()){
				PaymentDetailsDTO pd = it.next();
				int i=1;
				try{
					con = DataBaseUtil.getConnection();
					con.setAutoCommit(false);

					if (!isPaymentPosted(con, pd.getChargeId(),pd.getPkgActivityId())){

						ps = con.prepareStatement(INSERT_PAYMENT_DETAILS);
						payId = getPaymentId();
						pd.setPaymentId(payId);

						ps.setString(i++,payId);
						ps.setString(i++,pd.getPaymentType());
						ps.setString(i++,pd.getVoucherNo());
						ps.setBigDecimal(i++,pd.getAmount());
						ps.setString(i++,pd.getDescription());
						ps.setString(i++,pd.getCategory());
						ps.setTimestamp(i++, new Timestamp(pd.getPostedDate().getTime()));
						ps.setString(i++,pd.getUsername());
						ps.setString(i++,pd.getPayeeName());
						ps.setString(i++,pd.getChargeId());
						ps.setInt(i++, pd.getAccountHead());
						ps.setString(i++, pd.getPkgActivityId());
						ps.setInt(i++, 1);

						success = ps.executeUpdate() > 0;
						if (!success) {
							return success;
						}
						ChargeDAO cdao = new ChargeDAO(con);
						BillActivityChargeDAO bacDao = new BillActivityChargeDAO(con);

						if (pd.getPaymentType().equals("D")) {
							if (pd.getPackagCharge().equals("Y")) {
								success &= bacDao.updateActivityPaymentDetails(con, pd.getPkgActivityCode(),
										pd.getPkgActivityId(), pd.getAmount(), pd.getPaymentId());
							} else {
								success &= cdao.updateDoctorPaymentDetails(con, pd.getChargeId(),
										pd.getAmount(), pd.getPaymentId());
							}

						} else if (pd.getPaymentType().equals("R") || pd.getPaymentType().equals("F") ) {
							success &= cdao.updateRefDocPaymentDetails(con, pd.getChargeId(),
									pd.getAmount(), pd.getPaymentId());

						} else if (pd.getPaymentType().equals("P")) {
							success &= cdao.updatePrescPaymentDetails(con, pd.getChargeId(),
									pd.getAmount(), pd.getPaymentId());
						}

						if (success) {
							con.commit();
						} else {
							con.rollback();
							return success;
						}
				}
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}
		return success;
	}

	public static final String REFERRAL_ALL_CHARGE_FOR_AUTO =
		" SELECT b.bill_no, b.total_amount, b.total_claim AS actual_claim_amt, b.primary_claim_status, b.claim_recd_amount,"+
		" b.status AS bill_status, bc.charge_id, bc.act_description, bc.amount, bc.overall_discount_auth, "+
		" bc.dr_discount_amt, bc.discount_auth_dr, bc.discount_auth_ref, bc.ref_discount_amt, bc.charge_group, "+
		" bc.referal_amount, bc.discount_auth_pres_dr, bc.pres_dr_discount_amt, bc.discount_auth_hosp, "+
		" bc.hosp_discount_amt, bc.status AS bill_charge_status,chc.chargehead_name, bc.posted_date AS bc_posted_date, "+
		" coalesce(pr.reference_docto_id, isr.referring_doctor) as reference_docto_id, "+
		" pr.primary_sponsor_id, coalesce(pr.mr_no, isr.incoming_visit_id) as mr_no, "+
		" CASE WHEN b.bill_type='C' THEN 'Bill Later'  ELSE 'Bill Now' END as billtype,"+
		" bc.prescribing_dr_amount, bc.doctor_amount, coalesce(b.finalized_date,isr.date) as finalized_date, bc.discount, bc.ref_payment_id, "+
		" CASE WHEN b.status='A' THEN 'Open' WHEN b.status='F' THEN  'Finalized' WHEN b.status='S' THEN "+
		" 'Settled' WHEN b.status='C' THEN 'Closed' END as billstatus, CASE WHEN b.visit_type='i' THEN "+
		" 'In Patient' WHEN b.visit_type='o'  THEN 'Out Patient' END as visittype, "+
		" coalesce(pdet.patient_name,isr.patient_name) as patient_name ,  "+
		" pdet.last_name, chc.chargehead_id, bc.overall_discount_amt,b.visit_type, "+
		" dac.disc_auth_name AS discount_auth_dr_name, dap.disc_auth_name AS discount_auth_pres_dr_name, "+
		" daov.disc_auth_name AS overall_discount_auth_name, dar.disc_auth_name AS discount_auth_ref_name, "+
		" CASE WHEN bc.charge_group = 'PKG' THEN 'Y' ELSE 'N' END AS package_charge, tpa.tpa_name ";

	public static final String REFERRAL_CHARGE_FIELDS_FOR_AUTO =  REFERRAL_ALL_CHARGE_FOR_AUTO ;

	public static final String REFERRAL_CHARGE_TABLES_FOR_AUTO = "FROM bill_charge bc JOIN bill b USING(bill_no) "+
		" JOIN chargehead_constants chc ON  chc.chargegroup_id=bc.charge_group and  "+
		" chc.chargehead_id=bc.charge_head  " +
		" LEFT JOIN patient_registration pr ON (pr.patient_id=b.visit_id) " +
		" LEFT JOIN patient_details pdet on (pdet.mr_no = pr.mr_no)   "+
		" LEFT JOIN tpa_master tpa on tpa.tpa_id = pr.primary_sponsor_id "+
		" LEFT JOIN incoming_sample_registration isr on (isr.billno=b.bill_no) " ;

	public static final String REFERRAL_DISCOUNT_CHARGE_TABLES_FOR_AUTO =
		" LEFT OUTER JOIN discount_authorizer dac ON (bc.discount_auth_dr = dac.disc_auth_id) "+
		" LEFT OUTER JOIN discount_authorizer dap ON (bc.discount_auth_pres_dr = dap.disc_auth_id) "+
		" LEFT OUTER JOIN discount_authorizer dar ON (bc.discount_auth_ref = dar.disc_auth_id) "+
		" LEFT OUTER JOIN discount_authorizer dah ON (bc.discount_auth_hosp = dah.disc_auth_id) "+
		" LEFT OUTER JOIN discount_authorizer daov ON (bc.overall_discount_auth = daov.disc_auth_id) ";


	public static final String REFERRAL_CHARGES_TABLES_FOR_AUTO =  REFERRAL_CHARGE_TABLES_FOR_AUTO +
		REFERRAL_DISCOUNT_CHARGE_TABLES_FOR_AUTO ;

	public static final String REFERRAL_CHARGE_COUNT_FOR_AUTO = " SELECT count(*) ";

	public static final String REFERRAL_CHARGES_WHERE_FOR_AUTO = " WHERE  b.status in ('F','S','C') AND "+
		" (bc.status !='X')  AND  (referal_amount > 0 ) AND (ref_payment_id is null) ";

	//referrral doctor posted charges

	public static PagedList  getReferralDoctorChargesForAuto(Map filter, Map listing, String searchType)
		throws SQLException,  ParseException {
		Connection con = null;
		PagedList dataList = null;
		if (searchType.equals("allItems")){
			listing.put(LISTING.PAGENUM, 0);
			listing.put(LISTING.PAGESIZE, 0);
		}
		try{
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder	qb;
			qb = new SearchQueryBuilder(con, REFERRAL_CHARGE_FIELDS_FOR_AUTO, REFERRAL_CHARGE_COUNT_FOR_AUTO,
					REFERRAL_CHARGES_TABLES_FOR_AUTO, REFERRAL_CHARGES_WHERE_FOR_AUTO, listing);

			qb.addFilterFromParamMap(filter);
			qb.build();

			dataList = qb.getMappedPagedList();
		} finally {
			if (con != null) con.close();
		}
		return  dataList;
	}

	private static final String INSERT_CONDUCTING_DOCTORS_NON_PKG_PAYMENT_DETAILS = "INSERT INTO payments_details (payment_id, payment_type, amount, " +
		" description, category, posted_date,"+
		" username, payee_name, charge_id, account_head, account_group, expense_center_id)"+
		" SELECT GENERATE_ID('DOCTOR_PAYMENTS'), 'D'," + 
		" bc.doctor_amount, bc.act_description, chc.chargehead_name, localtimestamp(0), ?,"+
		" bc.payee_doctor_id, bc.charge_id, 0, 1, pr.center_id"+
		" FROM bill_charge bc"+
		" JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)"+
		" JOIN bill b ON (b.bill_no = bc.bill_no)" +
		" JOIN patient_registration pr ON(b.visit_id = pr.patient_id)"+
		" JOIN doctors doc ON(bc.payee_doctor_id = doc.doctor_id)"+
		" WHERE bc.doc_payment_id is null and doctor_amount > 0 and bc.payee_doctor_id IS NOT NULL"+
		" AND b.status != 'A' AND bc.status !='X'";

	private static final String UPDATE_BILL_CHARGE_FOR_CONDUCTING_DOCTOR = "UPDATE bill_charge bc SET doc_payment_id = pd.payment_id " +
		" FROM payments_details pd " +
		" WHERE pd.charge_id = bc.charge_id AND pd.payment_type = 'D'"+
		" AND pd.mod_time = NOW();";

	private static final String INSERT_CONDUCTING_DOCTORS_PKG_PAYMENT_DETAILS = "INSERT INTO payments_details (payment_id, payment_type, amount, description, category, posted_date,"+
		" username, payee_name, charge_id, account_head, activity_id, account_group, expense_center_id)"+
		" SELECT GENERATE_ID('DOCTOR_PAYMENTS'), 'D'," +
		" bac.doctor_amount, bc.act_description, chc.chargehead_name, localtimestamp(0), ?,"+
		" bac.doctor_id, bc.charge_id, 0, bac.activity_id, 1, pr.center_id"+
		" FROM bill_charge bc"+
		" JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)"+
		" JOIN bill_activity_charge bac ON (bac.charge_id = bc.charge_id)"+
		" JOIN bill b ON (b.bill_no = bc.bill_no)" +
		" JOIN patient_registration pr ON(b.visit_id=pr.patient_id) "+
		" JOIN doctors doc ON(bac.doctor_id = doc.doctor_id)"+
		" WHERE bac.doctor_payment_id is null and bac.doctor_amount > 0 and bac.doctor_id IS NOT NULL "+
		" AND b.status != 'A' AND bc.status != 'X'"+
		" AND bc.charge_head = 'PKGPKG'";

	private static final String UPDATE_BILL_ACTIVITY_CHARGE_FOR_CONDUCTING_DOCTOR = "UPDATE bill_activity_charge bac SET doctor_payment_id = pd.payment_id " +
		" FROM payments_details pd"+
		" WHERE pd.charge_id = bac.charge_id AND pd.activity_id = bac.activity_id AND pd.payment_type = 'D'"+
		" AND pd.mod_time  = NOW();";

	private static final String INSERT_PRESCRIBING_DOCTORS_PAYMENT_DETAILS = "INSERT INTO payments_details (payment_id, payment_type, amount, " +
		" description, category, posted_date,"+
		" username, payee_name, charge_id, account_head, account_group, expense_center_id)"+
		" SELECT GENERATE_ID('DOCTOR_PAYMENTS'), 'P'," +
		" bc.prescribing_dr_amount, bc.act_description, chc.chargehead_name, localtimestamp(0), ?,"+
		" bc.prescribing_dr_id, bc.charge_id, 0, 1, pr.center_id"+
		" FROM bill_charge bc " +
		" JOIN bill b ON(b.bill_no=bc.bill_no)" +
		" JOIN patient_registration pr ON(b.visit_id=pr.patient_id)"+
		" JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head) "+
		" JOIN doctors doc ON(bc.prescribing_dr_id = doc.doctor_id)"+
		" WHERE bc.prescribing_dr_payment_id is null and prescribing_dr_amount > 0 and bc.prescribing_dr_id IS NOT NULL "+
		" AND b.status != 'A' AND bc.status != 'X'";

	private static final String UPDATE_BILL_CHARGE_FOR_PRESCRIBING_DOCTOR = "UPDATE bill_charge bc SET prescribing_dr_payment_id = pd.payment_id " +
		" FROM payments_details pd"+
		" WHERE pd.charge_id = bc.charge_id AND pd.payment_type = 'P'"+
		" AND pd.mod_time = NOW() ;";

	private static final String INSERT_REFERRAL_DOCTORS_PAYMENT_DETAILS = "INSERT INTO payments_details (payment_id, payment_type, amount, description, category, posted_date,"+
		" username, payee_name, charge_id, account_head, account_group, expense_center_id)"+
		" SELECT GENERATE_ID('DOCTOR_PAYMENTS'), " +
		" CASE WHEN (coalesce(pr.reference_docto_id, isr.referring_doctor)) LIKE 'DOC%' "+
		" 	THEN 'R' ELSE 'F' END AS payment_type,"+
		" bc.referal_amount, bc.act_description, chc.chargehead_name, localtimestamp(0), ?,"+
		" coalesce(pr.reference_docto_id, isr.referring_doctor), bc.charge_id, 0, 1, coalesce(pr.center_id, isr.center_id)"+
		" FROM bill_charge bc"+
		" JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)"+
		" JOIN bill b ON (b.bill_no = bc.bill_no)"+
		" LEFT JOIN patient_registration pr ON (b.visit_id = pr.patient_id)"+
		" LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)"+
		" WHERE bc.ref_payment_id is null and referal_amount > 0 "+
		" AND coalesce(pr.reference_docto_id, isr.referring_doctor) IS NOT NULL "+
		" AND b.status != 'A' AND bc.status != 'X'";

	private static final String UPDATE_BILL_CHARGE_FOR_REFERRAL_DOCTOR = "UPDATE bill_charge bc SET ref_payment_id = pd.payment_id " +
		" FROM payments_details pd"+
		" WHERE pd.charge_id = bc.charge_id AND pd.payment_type IN('F','R')"+
		" AND pd.mod_time = NOW() ;";

	private static final String CURRENT_VALUE_OF_PAYMENT_SEQUENCE = "select get_generated_id('DOCTOR_PAYMENTS');";

	private static final String NEXT_VALUE_OF_PAYMENT_SEQUENCE = "SELECT GENERATE_ID('DOCTOR_PAYMENTS');";

	public String getMaxPaymentSequence(Connection con) throws SQLException{
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(CURRENT_VALUE_OF_PAYMENT_SEQUENCE);
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public String getNextPaymentSequence(Connection con) throws SQLException{
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(NEXT_VALUE_OF_PAYMENT_SEQUENCE);
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}


  public void createDynamicQuery(Connection con,String paymntInsertQuery,String whereClause,Map filterMap,PreparedStatement ps,String UserId, String to, int[] centerIds) throws Exception{
		StringBuilder sb = new StringBuilder(paymntInsertQuery);
		int count=1;
		try {
			SearchQueryBuilder sqb = new SearchQueryBuilder(con,
					paymntInsertQuery, null, null, whereClause,
					null, false, 0, 0);
			sqb.addFilterFromParamMap(filterMap);
			sb.append(sqb.getWhereClause());
			ArrayList fieldTypes = sqb.getfieldTypes();
			ArrayList fieldValues = sqb.getfieldValues();
			ps = con.prepareStatement(sb.toString());
			ps.setString(count++, UserId);

      for (int centerId : centerIds) {
        if (centerId != 0) {
          ps.setInt(count++, centerId);
        }
      }

			if (fieldTypes != null && fieldValues != null) {
				for(int i=0;i<fieldTypes.size();i++) {
					if ((Integer)fieldTypes.get(i) == 2) {
						ps.setString(count++, (String)fieldValues.get(i));
					} else if((Integer)fieldTypes.get(i) == 4) {
						ps.setDate(count++, (Date)fieldValues.get(i));
					} else if ((Integer)fieldTypes.get(i) == 7) {
						ps.setBoolean(count++,(Boolean)(fieldValues.get(i)));
					} else if ((Integer)fieldTypes.get(i) == 5) {
						ps.setTimestamp(count++,(Timestamp)(fieldValues.get(i)));
					}
				}
			}
			ps.executeUpdate();
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}

	}

	public  boolean saveConductingDoctorsAutoPayments(Connection con ,String userId,Map filterMap, String selectedPostedDate)throws Exception {
		boolean success = false;
		try {
			PreparedStatement ps = null;
			PreparedStatement ps2 = null;
			String maxPayId = getMaxPaymentSequence(con);
			int centerId = RequestContext.getCenterId();
			boolean notTodaysDate = (selectedPostedDate.equalsIgnoreCase("finalized") || selectedPostedDate.equalsIgnoreCase("closed"));

      String insertNonPackagePaymentDetailQuery = constructPaymentDetailInsertQuery(selectedPostedDate, centerId,
          notTodaysDate, INSERT_CONDUCTING_DOCTORS_NON_PKG_PAYMENT_DETAILS);
      createDynamicQuery(con, insertNonPackagePaymentDetailQuery, "", filterMap, ps,
          userId, "conductdoctor", new int[] {centerId});

			try (PreparedStatement ps1 = con.prepareStatement(UPDATE_BILL_CHARGE_FOR_CONDUCTING_DOCTOR);) {
  			ps1.executeUpdate() ;
			}

			String maxPayId1 = getMaxPaymentSequence(con);

      String insertPackagePaymentDetailQuery =
          constructPaymentDetailInsertQuery(selectedPostedDate, centerId, notTodaysDate,
              INSERT_CONDUCTING_DOCTORS_PKG_PAYMENT_DETAILS);
      createDynamicQuery(con, insertPackagePaymentDetailQuery, "", filterMap, ps2, userId,
          "conductdoctor", new int[] {centerId});

 			try (PreparedStatement ps3 = con.prepareStatement(UPDATE_BILL_ACTIVITY_CHARGE_FOR_CONDUCTING_DOCTOR);) {
  			ps3.executeUpdate();
			}
			success = true;
		} finally {
		  
		}
		return success;

	}

  /**
   * Construct payment detail insert query.
   *
   * @param selectedPostedDate the selected posted date
   * @param centerId the center id
   * @param notTodaysDate the not todays date
   * @param baseQuery the base query
   * @return the string
   */
  private String constructPaymentDetailInsertQuery(String selectedPostedDate, int centerId,
      boolean notTodaysDate, String baseQuery) {
    StringBuilder paymentDetailInsertQueryBuilder = new StringBuilder(baseQuery);
    if (centerId != 0) {
      paymentDetailInsertQueryBuilder.append(" AND pr.center_id = ?");
    }
    if (notTodaysDate) {
      String insertPaymentDetailQuery = paymentDetailInsertQueryBuilder.toString();
      insertPaymentDetailQuery = insertPaymentDetailQuery.replace("localtimestamp(0)",
          selectedPostedDate.equalsIgnoreCase("finalized") ? "b.finalized_date"
              : "b.closed_date");
      paymentDetailInsertQueryBuilder = new StringBuilder(insertPaymentDetailQuery);
    }
    return paymentDetailInsertQueryBuilder.toString();
  }

	public boolean savePrescribingDoctorsAutoPayments(Connection con,String userId,Map filterMap, String selectedPostedDate)throws Exception {

		PreparedStatement ps = null;
		boolean success = false;
		try {
			String maxPayId = getMaxPaymentSequence(con);
			int centerId = RequestContext.getCenterId();
			boolean notTodaysDate = (selectedPostedDate.equalsIgnoreCase("finalized") || selectedPostedDate.equalsIgnoreCase("closed"));

			String insertPaymentQuery = constructPaymentDetailInsertQuery(selectedPostedDate,centerId,notTodaysDate,INSERT_PRESCRIBING_DOCTORS_PAYMENT_DETAILS);
            createDynamicQuery(con, insertPaymentQuery, "", filterMap, ps, userId,
                "prescribeddoctor", new int[] {centerId});

			try (PreparedStatement ps1 = con.prepareStatement(UPDATE_BILL_CHARGE_FOR_PRESCRIBING_DOCTOR);) {
  			ps1.executeUpdate();
  			DataBaseUtil.closeConnections(null, ps1);
			}
			success=true;
		} finally{

		}
		return success;
	}

	public  boolean saveReferralDoctorsAutoPayments(Connection con,String userId,Map filterMap, String selectedPostedDate)throws Exception {

		PreparedStatement ps = null;
		boolean success = false;
		try {
			String maxPayId = getMaxPaymentSequence(con);
			int centerId = RequestContext.getCenterId();
			boolean notTodaysDate = (selectedPostedDate.equalsIgnoreCase("finalized") || selectedPostedDate.equalsIgnoreCase("closed"));
			StringBuilder paymentDetailInsertQueryBuilder = new StringBuilder(INSERT_REFERRAL_DOCTORS_PAYMENT_DETAILS);
	    if (centerId != 0) {
            paymentDetailInsertQueryBuilder.append(" AND ( pr.center_id = ? ");
	      paymentDetailInsertQueryBuilder.append(" OR isr.center_id = ? )");
	    }
	    if (notTodaysDate) {
	      String insertPaymentDetailQuery = paymentDetailInsertQueryBuilder.toString();
	      insertPaymentDetailQuery = insertPaymentDetailQuery.replace("localtimestamp(0)",
	          selectedPostedDate.equalsIgnoreCase("finalized") ? "b.finalized_date"
	              : "b.closed_date");
	      paymentDetailInsertQueryBuilder = new StringBuilder(insertPaymentDetailQuery);
	    }
	    
          createDynamicQuery(con, paymentDetailInsertQueryBuilder.toString(), "", filterMap, ps,
              userId, "referraldoctor", new int[] {centerId, centerId});
			try (PreparedStatement ps1 = con.prepareStatement(UPDATE_BILL_CHARGE_FOR_REFERRAL_DOCTOR);) {
  			ps1.executeUpdate();
  			DataBaseUtil.closeConnections(null, ps1);
			}
			success=true;
		}  finally {

		}
		return success;
	}

	public static final String GET_PAYMENT_IDFROM_STORE_INVOICE = "SELECT payment_id FROM store_invoice WHERE " +
			"invoice_no=? AND  supplier_id = ?  AND status='F' and invoice_date = ?";


	public static final String SELECT_PAYMENT_IDFROM_STORECONSIGNMENT =
		" SELECT payment_id FROM store_consignment_invoice "+
		" WHERE (grn_no, issue_id) IN ( "+
		"  SELECT grn_no, issue_id FROM 	 "+
		" store_consignment_invoice "+
		" ci  "+
		"  JOIN store_invoice si ON si.supplier_invoice_id::text= ci.supplier_invoice_id::text "+
		"  where issue_id=? AND invoice_no=? AND si.supplier_id = ? ) ";


	public static final String SELECT_PAYMENT_IDFROM_DEBITNOTE = "SELECT payment_id FROM store_debit_note WHERE " +
			"debit_note_no=? AND  supplier_id = ? ";


	public static BasicDynaBean isPaymentIdExists(String invoiceType, String consignmentStatus, Map queryParams)throws SQLException, ParseException {
		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			if (invoiceType.equals("P")) {
				if (consignmentStatus.equals("F")) {
					pstmt = con.prepareStatement(GET_PAYMENT_IDFROM_STORE_INVOICE);
					pstmt.setString(1, (String) queryParams.get("grnNo"));
					pstmt.setString(2, (String) queryParams.get("supplier"));
					pstmt.setDate(3, (Date) queryParams.get("invoice_date"));

				} else if(consignmentStatus.equals("O")) {
					pstmt = con.prepareStatement(SELECT_PAYMENT_IDFROM_STORECONSIGNMENT);
					pstmt.setInt(1, (Integer) queryParams.get("issueId"));
					pstmt.setString(2, (String) queryParams.get("grnNo"));
					pstmt.setString(3, (String) queryParams.get("supplier"));

				}

			} else if(invoiceType.equals("PD")) {
				pstmt = con.prepareStatement(SELECT_PAYMENT_IDFROM_DEBITNOTE);
				pstmt.setString(1, (String) queryParams.get("grnNo"));
				pstmt.setString(2, (String) queryParams.get("supplier"));
			}

			return DataBaseUtil.queryToDynaBean(pstmt);

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}


	public BasicDynaBean getPayersDetails(String payeeId) throws SQLException{
		return payeeDAO.findByKey("payee_id", payeeId);

	}

}
