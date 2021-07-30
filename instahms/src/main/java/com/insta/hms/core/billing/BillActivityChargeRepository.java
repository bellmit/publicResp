package com.insta.hms.core.billing;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class BillActivityChargeRepository extends GenericRepository {

	public BillActivityChargeRepository() {
		super("bill_activity_charge");
	}
	
	private static final String CHARGE_ACTIVITIES =
			" SELECT bac.*, " +
			"  cd.payment_category::text as con_doc_category, cd.payment_eligible as con_doc_eligible " +
			" FROM bill_activity_charge bac " +
			"  LEFT JOIN doctors cd ON (cd.doctor_id = bac.doctor_id) " +
			" WHERE charge_id=? ";

	public List<BasicDynaBean> getChargeActivities(Object[] objects) {
		return DatabaseHelper.queryToDynaList(CHARGE_ACTIVITIES, objects);
	}

	private static final String DELETE_ACTIVITY = " DELETE from bill_activity_charge WHERE activity_id=? AND activity_code=? ";
	
  public int deleteActivity(String activityCode, String activityId) {
    return DatabaseHelper.delete(DELETE_ACTIVITY, new Object[] { activityId, activityCode });
  }
	
	/*
	 * Returns the charge ID given activity_code + activity_id combination. If
	 * the combination is not found returns null.
	 */
	public static final String GET_ACTIVITY_CHARGE =
		" SELECT charge_id FROM bill_activity_charge WHERE activity_code=? AND activity_id=? ";

  public String getChargeId(String activityCode, String activityId) {
    return DatabaseHelper.getString(GET_ACTIVITY_CHARGE, new Object[] { activityCode, activityId });
  }
	
  public static final String GET_ACTIVITY_CHARGE_AND_BILL = " SELECT charge_id, bill_no FROM bill_activity_charge "
      + "JOIN bill_charge USING (charge_id) WHERE activity_code=? AND activity_id=? ";

  public BasicDynaBean getChargeAndBillDetails(String activityCode, String activityId) {
    return DatabaseHelper.queryToDynaBean(GET_ACTIVITY_CHARGE_AND_BILL,
        new Object[] { activityCode, activityId });
  }
  
  public static final String GET_CHARGE = " SELECT bc.* FROM bill_activity_charge "
      + "JOIN bill_charge bc USING (charge_id) WHERE activity_code=? AND activity_id=? ";
  
  public BasicDynaBean getCharge(String activityCode, String activityId) {
    return DatabaseHelper.queryToDynaBean(GET_CHARGE,
        new Object[] { activityCode, activityId });
  }

  public static final String UPDATE_ACTIVITY_DETAILS = " UPDATE bill_activity_charge SET doctor_id=?, activity_conducted=?, conducted_datetime=? "
      + " WHERE activity_code=? AND activity_id=? ";

  public static final String UPDATE_CHARGE_ACTIVITY_DETAILS = " UPDATE bill_charge bc SET payee_doctor_id=?, activity_conducted=?, conducted_datetime=? "
      + " FROM bill_activity_charge bac WHERE bac.activity_code=? AND bac.activity_id=? "
      + "  AND bc.charge_id = bac.charge_id AND bc.charge_head != 'PKGPKG' ";

  public boolean updateActivityDetails(String activityCode, String activityId, String doctorId,
      String conductionStatus, Timestamp conductedDateTime) {
    boolean success = true;
    Object[] values = new Object[] { doctorId, conductionStatus, conductedDateTime, activityCode,
        activityId };
    success = DatabaseHelper.update(UPDATE_ACTIVITY_DETAILS, values) > 0;

    success &= DatabaseHelper.update(UPDATE_CHARGE_ACTIVITY_DETAILS, values) > 0;
    return success;
  }
	
	private static final String UPDATE_ACTIVITY_DR_CHARGE_HEAD = "UPDATE bill_activity_charge SET payment_charge_head=? "
			+ " WHERE charge_id=? ";

	public void updateConsultationTypeChargeActivity(BasicDynaBean drCharge, String codeType) {
		String chargeHead = (String) drCharge.get("charge_head");
		String chargeId = (String) drCharge.get("charge_id");
		DatabaseHelper.update(UPDATE_ACTIVITY_DR_CHARGE_HEAD, new Object[] {
				chargeHead, chargeId });
	}

	  public static final String GET_BILL_STATUS = "SELECT b.status " +
	      " FROM bill_charge bc " +
	      "  JOIN bill b USING (bill_no) " +
	      " WHERE bc.charge_id=?";
	  
	  public String getBillChargeStatus(String chargeId) {
	    return DatabaseHelper.getString(GET_BILL_STATUS, chargeId);
	  }

}
