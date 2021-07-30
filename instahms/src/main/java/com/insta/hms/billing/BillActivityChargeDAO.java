package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

/*
 * Notes about the bill_activity_charge table and its usage:
 *
 * This table stores the association of a charge item and the activities that the charge
 * is connected to. Since activity IDs can be repeated across different kinds of activities,
 * we use the acvitity_code (used to be called charge_head in the table) as a way to
 * combine with the ID to get a unique activity.
 *
 * Cardinality
 * ===========
 * One charge can be associated with multiple activities, which is possible in the case of
 * packages. But, one activity can be associated with only one main charge. Thus,
 * activity_code+activity_id is the primary key for this table.
 *
 *             -> activity1  (DIA 1)
 *   charge --|-> activity2  (SER 1)
 *             -> activity3  (OPE 1)
 *
 * For quick access to whether a charge has one or more activities associated with it,
 * bill_charge.hasactivity is set to true whenever there are activities for that charge.
 *
 * Multiple charges for an activity
 * ================================
 * When multiple charges result from one activity, the information is NOT stored within this
 * table. Instead, bill_charge table itself stores the information in the form of charge_ref.
 *
 * bill_charge:
 *   charge_id  desc         ref_charge  hasactivity
 *   ----------------------------------------------
 *   CH0001     Bed Charge   null        t
 *   CHOOO4     Luxury Tax   CH0001      t    --> subsidiary charge for CH0001, single activity
 *
 * This makes it a bit hard to get all the charges associated with an activity, but it is rarely
 * required. Revenue reports should use bill_charge, and this includes "Activity" revenue
 * reports such as Lab Revenue Report. This is in any case true if we have packages, since it
 * is incorrect to show revenue for a lab test conducted as part of a package as the whole package
 * charge.
 *
 * Conduction
 * ==========
 * Every activity that has a separate conduction and/or conducting doctor payment should preferably
 * be a unique activity. Thus, the surgeon activity and the anaesthetist activity in an operation should
 * not be combined: there needs to be a separate entry in doctor_consultation for the anaesthetist.
 * (On further consideration wrt intensivist payments, it appears this rule is not 100% OK. We need
 * to have a subsidary charge hold the payee_doctor_id, since it is duty_doctor_charge. It also doesn't
 * make sense to make the DDBED the main charge. TODO.)
 *
 * Charge Head
 * ===========
 * The old charge_head column has been renamed to payment_charge_head, as that is what it is meant
 * for: when processing payment, this charge head decides which payment rule will match the activity.
 *
 * Cancellation
 * ============
 * Cancellation of an activity will retain the link to the bill_activity_charge and the bill_charge
 * (as well as references). hasactivity will continue to be true for all the associated charges, but
 * all of the charges will be marked as 'X'.
 *
 * This is so that in case we allow 'un-cancellation' in future, we have a reference to the original
 * charge. Also, charges resulting from orders should always be modified only from order.
 *
 * Common queries
 * ==============
 * Here are some common tasks and the correct way to do it:
 *
 * (a) Find the bill associated with the activity:
 *     Use the activity's activity_code and the activity ID to search in bill_activity_charge,
 *     join bill_charge on charge_id, and then bill on bill_no.
 *
 *     Note: it is incorrect to use bac.activity_code = bc.charge_head in any join.
 *
 * (b) Find all activities associated with a charge:
 *     Start from bill_charge, join bill_activity_charge, LEFT join all activity tables.
 *     (not recommended).
 *
 *     If you know that the charge is not a package, then, you can assume that there is only
 *     one activity for that charge, so you can directly join bac on charge_id.
 *
 * (c) Get total of all charges for an activity:
 *     Join with bill_activity_charge with the activity_code + activity_id to get the charge_id,
 *     use a subquery to sum the amount from bill_charge where charge_id = ? OR charge_ref = ?.
 *
 * (d) Cancel a charge:
 *     This should be prevented if hasactivity is true. If not, then, cancel the charge itself
 *     and cancel all charges which have charge_ref = cancelled charge's charge_id.
 *
 *     Note: subsidiary charges can be deleted independently, unless they are also associated
 *     with an activity.
 *
 * (e) Cancel an activity:
 *     This should be prevented if the activity is partially or fully conducted. Cancel the
 *     activity itself, and then cancel the charge associated with the activity as per (d)
 *     above, so that all related charges are also cancelled.
 *
 */
public class BillActivityChargeDAO {

	Connection con = null;

	public BillActivityChargeDAO(Connection con) {
		this.con = con;
	}

	private static final String INSERT_BILL_ACT_CHG =
		" INSERT INTO bill_activity_charge " +
		" (charge_id, activity_code, payment_charge_head, activity_id, charge_group, " +
		" act_description_id, doctor_id, activity_conducted, conducted_datetime) " +
		" VALUES(?,?,?,?,?,?,?,?,?)";

	/*
	 * Insert a list of elements
	 */
    public boolean insertBillActivityCharge(List list) throws SQLException{
     
      PreparedStatement ps = null;
      boolean success = true;
  
      try{
    		if (list.isEmpty())
    			return true;
    		
    		ps = con.prepareStatement(INSERT_BILL_ACT_CHG);
    		Iterator iterator = list.iterator();
    		while (iterator.hasNext()) {
    			BillActivityCharge bacdto = (BillActivityCharge)iterator.next();
    			setInsertChargeParams(bacdto,ps);
    			ps.addBatch();
    		}
    		int results[] = ps.executeBatch();
    		for (int p = 0; p < results.length; p++) {
    			if (results[p] <= 0) {
    				success = false;
    				break;
    			}
    		}
      }finally{
        if(null != ps){
          ps.close();
        }
      }
    	return success;
    }

    /*
	 * Insert one element
	 */
    public boolean insertBillActivityCharge(BillActivityCharge bacdto) throws SQLException {
		PreparedStatement ps = con.prepareStatement(INSERT_BILL_ACT_CHG);
		setInsertChargeParams(bacdto, ps);
		int count = ps.executeUpdate();
		ps.close();
		return (count == 1);
	}

	public boolean insertBillActivityCharge(String chargeId, String activityCode, String paymentChargeHead,
			String activityId, String activityDescId, String doctorId, String activityConducted,
			Timestamp conductedDateTime) throws SQLException {

		BillActivityCharge bac = new BillActivityCharge(chargeId, activityCode, paymentChargeHead,
				activityId, activityDescId, doctorId, activityConducted, conductedDateTime);
		return insertBillActivityCharge(bac);
	}

	private void setInsertChargeParams(BillActivityCharge bacdto, PreparedStatement ps) throws SQLException {
		int i = 1;
		ps.setString(i++, bacdto.getChargeId());
		ps.setString(i++, bacdto.getActivityCode());
		ps.setString(i++, bacdto.getPaymentChargeHead());
		ps.setString(i++, bacdto.getActivityId());
		ps.setString(i++, bacdto.getChargeGroup());
		ps.setString(i++, bacdto.getActDescriptionId());
		ps.setString(i++, bacdto.getDoctorId());
		ps.setString(i++, bacdto.getActivityConducted());
		ps.setTimestamp(i++, bacdto.getConductedDateTime());
	}

	private static final String DELETE_ACTIVITY =
		" DELETE from bill_activity_charge WHERE activity_id=? AND activity_code=? ";

	public boolean deleteActivity(BillActivityCharge dto) throws SQLException {
		deleteActivity(dto.getActivityCode(),dto.getActivityId());
		return true;
	}

	public void deleteActivity(String activityCode, String activityId) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(DELETE_ACTIVITY);
			ps.setString(1, activityId);
			ps.setString(2, activityCode);
			ps.executeUpdate();
		} finally {
			if (ps !=null) ps.close();
		}
	}

	/*
	 * Update activity_conducted alone: used when directly signing off, without
	 * specifying doctor who and when conducted.
	 */
	public static final String UPDATE_ACTIVITY_CONDUCTED =
		" UPDATE bill_activity_charge SET activity_conducted=? " +
		" WHERE activity_code=? AND activity_id=? ";

	public static final String UPDATE_CHARGE_ACTIVITY_CONDUCTED =
		" UPDATE bill_charge bc SET activity_conducted=? " +
		" FROM bill_activity_charge bac " +
		" WHERE bac.activity_code=? AND bac.activity_id=? " +
		"  AND bc.charge_id = bac.charge_id AND bc.charge_head != 'PKGPKG' ";

	public static void updateActivityConducted(Connection con, String activityCode, String activityId,
			String conductionStatus) throws SQLException {

		try(PreparedStatement ps = con.prepareStatement(UPDATE_ACTIVITY_CONDUCTED)) {			
			ps.setString(1, conductionStatus);
			ps.setString(2, activityCode);
			ps.setString(3, activityId);
			ps.executeUpdate();
		}
		// also update bill_charge, same details provided it is not a package charge.
		try(PreparedStatement ps = con.prepareStatement(UPDATE_CHARGE_ACTIVITY_CONDUCTED)){
			ps.setString(1, conductionStatus);
			ps.setString(2, activityCode);
			ps.setString(3, activityId);

			ps.executeUpdate();
		}	
	}

	/*
	 * Update activity_conducted and the doctor_id of who conducted
	 */
	public static final String UPDATE_ACTIVITY_DETAILS =
		" UPDATE bill_activity_charge SET doctor_id=?, activity_conducted=?, conducted_datetime=?, username=? " +
		" WHERE activity_code=? AND activity_id=? ";

	public static final String UPDATE_CHARGE_ACTIVITY_DETAILS =
		" UPDATE bill_charge bc SET payee_doctor_id=?, activity_conducted=?, conducted_datetime=?, username=? " +
		" FROM bill_activity_charge bac " +
		" WHERE bac.activity_code=? AND bac.activity_id=? " +
		"  AND bc.charge_id = bac.charge_id AND bc.charge_head != 'PKGPKG' ";

	public static boolean updateActivityDetails(Connection con, String activityCode, String activityId,
			String doctorId, String conductionStatus, Timestamp conductedDateTime, String userName) throws SQLException {
	  int i=1;
		try(PreparedStatement ps = con.prepareStatement(UPDATE_ACTIVITY_DETAILS)){ 		
  		ps.setString(i++, doctorId);
  		ps.setString(i++, conductionStatus);
  		ps.setTimestamp(i++, conductedDateTime);
  		ps.setString(i++, userName);
  		ps.setString(i++, activityCode);
  		ps.setString(i++, activityId);
  		ps.executeUpdate();
		}
		// also update bill_charge, same details provided it is not a package charge.
		i=1;
		try(PreparedStatement ps = con.prepareStatement(UPDATE_CHARGE_ACTIVITY_DETAILS)){
  		ps.setString(i++, doctorId);
  		ps.setString(i++, conductionStatus);
  		ps.setTimestamp(i++, conductedDateTime);
  		ps.setString(i++, userName);
  		ps.setString(i++, activityCode);
  		ps.setString(i++, activityId);
  		ps.executeUpdate();
		}
		return true;
	}

	public static final String UPDATE_ACTIVITY_PAYOUT =
		" UPDATE bill_activity_charge set doctor_amount=? " +
		" WHERE activity_code=? AND activity_id=?";

	public static boolean updateActivityPayout(Connection con, String activityCode, String activityId,
			BigDecimal docAmount)
		throws SQLException {

		PreparedStatement ps = null;
		int count = 0;
		try {
			ps = con.prepareStatement(UPDATE_ACTIVITY_PAYOUT);

			ps.setBigDecimal(1, docAmount);
			ps.setString(2, activityCode);
			ps.setString(3, activityId);

			count = ps.executeUpdate();
		} finally {
		  if(null != ps){
		    ps.close();
		  }
		}
		return (count == 1);
	}

	private static final String UPDATE_ACTIVITY_PAYMENT_DETAILS =
		" UPDATE bill_activity_charge set doctor_amount=?, doctor_payment_id=? " +
		" WHERE activity_code=? AND activity_id=?";

	public static boolean updateActivityPaymentDetails(Connection con, String activityCode, String activityId,
			BigDecimal docAmount, String paymentId) throws SQLException {

		boolean success = false;
		try(PreparedStatement ps = con.prepareStatement(UPDATE_ACTIVITY_PAYMENT_DETAILS)){
  		ps.setBigDecimal(1, docAmount);
  		ps.setString(2, paymentId);
  		ps.setString(3, activityCode);
  		ps.setString(4, activityId);
  
  		success = ps.executeUpdate() > 0;
		}
		return success;
	}

	private static final String REMOVE_DOC_PAYMENT_ID =
		" UPDATE bill_activity_charge SET doctor_payment_id=null " +
		" WHERE activity_code=? AND activity_id=?";

	public static void removeDoctorPaymentId(Connection con, String activityCode, String activityId)
		throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(REMOVE_DOC_PAYMENT_ID)){
  
  		ps.setString(1, activityCode);
  		ps.setString(2, activityId);
  
  		ps.executeUpdate();
		}
	}

	public static final String UPDATE_ACTIVITY_DOCTOR =
		" UPDATE bill_activity_charge set doctor_id=? " +
		" WHERE activity_code=? AND activity_id=?";

	public static boolean updateActivityDoctor(Connection con, String activityCode, int activityID,
			String docId)
		throws SQLException {

		PreparedStatement ps = null;
		int count = 0;
		try {
			ps = con.prepareStatement(UPDATE_ACTIVITY_PAYOUT);
			ps.setString(1, docId);
			ps.setString(2, activityCode);
			ps.setString(3, String.valueOf(activityID));

			count = ps.executeUpdate();
		} finally {
		  if(null != ps){
		    ps.close();
		  }
		}
		return (count == 1);
	}

	/*
	 * Returns the charge ID given activity_code + activity_id combination. If
	 * the combination is not found returns null.
	 */
	public static final String GET_ACTIVITY_CHARGE =
		" SELECT charge_id FROM bill_activity_charge WHERE activity_code=? AND activity_id=? ORDER BY charge_id";

	public ChargeDTO getCharge(String activityCode, String activityId) throws SQLException {

	  String chargeId = null;
    ChargeDTO charge = null;
		try(PreparedStatement stmt = con.prepareStatement(GET_ACTIVITY_CHARGE)){
		stmt.setString(1, activityCode);
		stmt.setString(2, activityId);		
  		try(ResultSet rs = stmt.executeQuery()){
    		if (rs.next()) {
    			chargeId = rs.getString(1);
    			charge = new ChargeDAO(con).getCharge(chargeId);
    		}
  		}
		}
		return charge;
	}

	public ChargeDTO getCharge(String activityCode, int activityId) throws SQLException {
		return getCharge(activityCode, String.valueOf(activityId));
	}

	/*
	 * Static functions returning the chargeID alone
	 */
	public static String getChargeId(String activityCode, String activityId) throws SQLException {		
		try(Connection con = DataBaseUtil.getReadOnlyConnection();
		    PreparedStatement stmt = con.prepareStatement(GET_ACTIVITY_CHARGE)){
			stmt.setString(1, activityCode);
			stmt.setString(2, activityId);
			String chargeId = null;
			try(ResultSet rs = stmt.executeQuery()){ 			
  			if (rs.next()) {
  				chargeId = rs.getString(1);
  			}
			}
			return chargeId;
		} 
	}

	public static final String GET_ACTIVE_ACTIVITY_CHARGE =
		" SELECT charge_id FROM bill_activity_charge " +
		" JOIN bill_charge USING(charge_id) " +
		" WHERE activity_code=? AND activity_id=? AND status != 'X' AND charge_ref IS NULL " +
		" ORDER BY charge_id";


	/**
	 * Returns active activity chargeid
	 * @param activityCode
	 * @param activityId
	 * @return
	 * @throws SQLException
	 */
	public static String getActiveChargeId(Connection con,String activityCode, String activityId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(GET_ACTIVE_ACTIVITY_CHARGE);
			ps.setString(1, activityCode);
			ps.setString(2, activityId);
			rs = ps.executeQuery();

			String chargeId = null;

			if (rs.next()) {
				chargeId = rs.getString(1);
			}
			return chargeId;
		} finally {
			DataBaseUtil.closeConnections(null, ps,rs);
		}
	}

	public static String getChargeId(String activityCode, int activityId) throws SQLException {
		return getChargeId(activityCode, String.valueOf(activityId));
	}

	/*
	 * Return a list of all bill_activity_charge records for the given charge id.
	 */
	public static final String CHARGE_ACTIVITIES =
		" SELECT bac.*, " +
		"  cd.payment_category::text as con_doc_category, cd.payment_eligible as con_doc_eligible " +
		" FROM bill_activity_charge bac " +
		"  LEFT JOIN doctors cd ON (cd.doctor_id = bac.doctor_id) " +
		" WHERE charge_id=? ";

	public static List<BasicDynaBean> getChargeActivities(Connection con, String chargeId)
		throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(CHARGE_ACTIVITIES);
			ps.setString(1, chargeId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
		  if(null != ps){
		    ps.close();
		  }
		}
	}

	/*
	 * Update the MLC activity ID (= visitId) for OP-IP conversion
	 */
	private static final String UPDATE_MLC_ACTIVITY_ID =
		" UPDATE bill_activity_charge bac SET activity_id=? " +
		" FROM bill_charge bc " +
		" WHERE activity_code='MLREG' AND bc.bill_no=?" +
		"  AND bc.charge_id=bac.charge_id ";

	public void updateActivityId(String billNo, String newActivityId) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(UPDATE_MLC_ACTIVITY_ID)){
  		int i=1;
  		ps.setString(i++, newActivityId);
  		ps.setString(i++, billNo);
  		ps.executeUpdate();
		}	
	}

	private static final String UPDATE_BILL_ACTIVITY_CHARGE =
			" UPDATE bill_activity_charge bac SET " +
			"  charge_id = ? , activity_code=?, payment_charge_head=?,  charge_group=?, " +
			" act_description_id=?, doctor_id=?, activity_conducted=?, conducted_datetime=?  " +
			" WHERE activity_id=? " ;

	public void updateBillActivityCharge(BillActivityCharge bacdto)  throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(UPDATE_BILL_ACTIVITY_CHARGE)){
  		int i = 1;
  		ps.setString(i++, bacdto.getChargeId());
  		ps.setString(i++, bacdto.getActivityCode());
  		ps.setString(i++, bacdto.getPaymentChargeHead());
  		ps.setString(i++, bacdto.getChargeGroup());
  		ps.setString(i++, bacdto.getActDescriptionId());
  		ps.setString(i++, bacdto.getDoctorId());
  		ps.setString(i++, bacdto.getActivityConducted());
  		ps.setTimestamp(i++, bacdto.getConductedDateTime());
  		ps.setString(i++, bacdto.getActivityId());
  		ps.executeUpdate();
		}
	}

	/*
	 * Returns the activity details given charge id
	 */
	public static final String GET_ACTIVITY_DETAILS =
		" SELECT charge_id, activity_id, payment_charge_head, charge_group, act_description_id, " +
		" doctor_id, doctor_amount, activity_conducted, " +
		" username, activity_code, conducted_datetime FROM bill_activity_charge WHERE charge_id=? ";

	public BillActivityCharge getActivity(String chargeId) throws SQLException {

	  BillActivityCharge activity = null;
		try(PreparedStatement stmt = con.prepareStatement(GET_ACTIVITY_DETAILS)){
  		stmt.setString(1, chargeId);
  		try(ResultSet rs = stmt.executeQuery()){
    		if (rs.next()) {
    			activity = new BillActivityCharge();
    			populateBillActivityChargeDTO(activity, rs);
    		}
  		}
		}
		return activity;
	}


	/*
	 * Returns the package activity entry given charge id and activity code
	 */
	public static final String GET_ACTIVITY_PKG_DETAILS =
		" SELECT charge_id, activity_id, payment_charge_head, charge_group, act_description_id, " +
		" doctor_id, doctor_amount, activity_conducted, " +
		" username, activity_code, conducted_datetime FROM bill_activity_charge WHERE charge_id=? " +
		" AND activity_code = ?";

	public BillActivityCharge getPkgActivity(String chargeId, String activityCode) throws SQLException {

		BillActivityCharge activity = null;
		try(PreparedStatement stmt = con.prepareStatement(GET_ACTIVITY_PKG_DETAILS)){
			stmt.setString(1, chargeId);
			stmt.setString(2, activityCode);
			try(ResultSet rs = stmt.executeQuery()){
				if (rs.next()) {
					activity = new BillActivityCharge();
					populateBillActivityChargeDTO(activity, rs);
				}
			}
		}
		return activity;
	}

	private void populateBillActivityChargeDTO(BillActivityCharge activity, ResultSet rs) throws SQLException {
		activity.setChargeId(rs.getString("charge_id"));
		activity.setActivityId(rs.getString("activity_id"));
		activity.setPaymentChargeHead(rs.getString("payment_charge_head"));
		activity.setChargeGroup(rs.getString("charge_group"));
		activity.setActDescriptionId(rs.getString("act_description_id"));
		activity.setDoctorId(rs.getString("doctor_id"));
		activity.setActivityConducted(rs.getString("activity_conducted"));
		activity.setActivityCode(rs.getString("activity_code"));
		activity.setConductedDateTime(rs.getTimestamp("conducted_datetime"));
	}

	/**
	 * Updates the activity id of existing with passed activityid.
	 * Useful while recodnucting test which need an updated activity_id for codification.
	 * @param oldActivityId
	 * @param activityCode
	 * @param newActivityId
	 * @return
	 */
	public boolean updateActivityId(String oldActivityId ,String activityCode,String newActivityId)
	throws SQLException{
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(
					"UPDATE bill_activity_charge SET activity_id = ?" +
					"	 WHERE activity_id = ? AND activity_code = ?");
			ps.setString(1, newActivityId);
			ps.setString(2, oldActivityId);
			ps.setString(3, activityCode);

			return ps.executeUpdate() > 0;
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}

	}

	private static String UPDATE_ACT_CHARGE = "UPDATE bill_activity_charge SET charge_id=? " +
		" WHERE activity_id=? AND activity_code=? ";
	public int updateActivityCharge(String activityId, String activityCode, String chargeId)
		throws SQLException {
		return DataBaseUtil.executeQuery(con, UPDATE_ACT_CHARGE, new Object[]{chargeId, activityId, activityCode});
	}

}
