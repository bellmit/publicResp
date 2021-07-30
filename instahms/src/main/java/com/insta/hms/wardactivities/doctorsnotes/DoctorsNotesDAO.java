package com.insta.hms.wardactivities.doctorsnotes;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.billing.BillChargeClaimDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.billing.DiscountPlanBO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emr.EMRInterface;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.orders.OrderBO;

import org.apache.commons.beanutils.BasicDynaBean;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

/**
 * The Class DoctorsNotesDAO.
 *
 * @author Nikunj
 */

public class DoctorsNotesDAO extends GenericDAO {

  /**
   * Instantiates a new doctors notes DAO.
   */
  public DoctorsNotesDAO() {
    super("ip_doctor_notes");
  }

  /** The Constant GET_NOTE_NUM. */
  public static final String GET_NOTE_NUM = "select max(note_num)"
      + " from ip_doctor_notes where patient_id=?";

  /**
   * Gets the note num.
   *
   * @param patientId the patient id
   * @return the note num
   * @throws SQLException the SQL exception
   */
  public int getNoteNum(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_NOTE_NUM);
      ps.setString(1, patientId);
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /**
   * Gets the billable notes for day.
   *
   * @param patientId the patient id
   * @param doctorId the doctor id
   * @param date the date
   * @return the billable notes for day
   * @throws SQLException the SQL exception
   */
  public static Integer getBillableNotesForDay(String patientId, String doctorId, Date date)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      if (null != date) {
        ps = con
            .prepareStatement("SELECT count(*) from ip_doctor_notes WHERE billable_consultation='Y'"
                + " AND creation_datetime::date = date(?) AND doctor_id= ? AND  patient_id= ? ");
        ps.setDate(1, date);
        ps.setString(2, doctorId);
        ps.setString(3, patientId);
      }
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_DOCTORS_NOTES. */
  public static final String GET_DOCTORS_NOTES = "SELECT idn.note_id, idn.patient_id, idn.note_num,"
      + " idn.notes, idn.billable_consultation, idn.consultation_type_id,"
      + " idn.creation_datetime, idn.doctor_id, doc.doctor_name, idn.mod_time, idn.mod_user,"
      + " idn.finalized, idn.highlighted, idn.charge_id "
      + " From ip_doctor_notes idn"
      + " LEFT JOIN doctors doc ON (idn.doctor_id=doc.doctor_id) "
      + " WHERE patient_id=? ORDER BY creation_datetime";

  /**
   * Gets the doctors notes.
   *
   * @param patientId the patient id
   * @return the doctors notes
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getDoctorsNotes(String patientId) throws SQLException {

    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_DOCTORS_NOTES);
      pstmt.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /** The Constant GET_DOCTORS_NOTES_FIELDS. */
  public static final String GET_DOCTORS_NOTES_FIELDS = " SELECT idn.note_id, idn.patient_id,"
      + " idn.note_num, regexp_replace(notes, E'[\\r\\n]+', ' ', 'g') as notes,"
      + " idn.billable_consultation, idn.consultation_type_id,idn.creation_datetime,"
      + " idn.doctor_id, doc.doctor_name, idn.mod_time,"
      + " idn.mod_user, idn.finalized, idn.highlighted, idn.charge_id ";
  
  /** The Constant COUNT. */
  private static final String COUNT = "SELECT count(*) ";
  
  /** The Constant TABLES. */
  private static final String TABLES = " From ip_doctor_notes idn"
      + " LEFT JOIN doctors doc ON (idn.doctor_id=doc.doctor_id) ";

  /**
   * Gets the doctors notes.
   *
   * @param patientId the patient id
   * @param pageNumParam the page num param
   * @param pageSizeParam the page size param
   * @return the doctors notes
   * @throws SQLException the SQL exception
   */
  public static PagedList getDoctorsNotes(String patientId, String pageNumParam,
      String pageSizeParam) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    SearchQueryBuilder qb = null;
    try {
      int pageNum = 0;
      int noOfRecord = 0;
      int pageSize = 20;
      if (pageSizeParam != null && !pageSizeParam.equals("")) {
        pageSize = Integer.parseInt(pageSizeParam);
      }
      if (pageNumParam != null && !pageNumParam.equals("")) {
        pageNum = Integer.parseInt(pageNumParam);
      } else {
        ps = con.prepareStatement(COUNT + TABLES + " WHERE (idn.patient_id  = ?)");
        ps.setString(1, patientId);
        rs = ps.executeQuery();
        if (rs.next()) {
          noOfRecord = rs.getInt(1);
        }

        int mod = noOfRecord % pageSize;
        if (mod == 0) {
          pageNum = noOfRecord / pageSize;
        } else {
          pageNum = noOfRecord / pageSize + 1;
        }
      }
      qb = new SearchQueryBuilder(con, GET_DOCTORS_NOTES_FIELDS, COUNT, TABLES, null,
          "idn.creation_datetime", false, pageSize, pageNum);
      qb.addFilter(SearchQueryBuilder.STRING, "idn.patient_id", "=", patientId);
      qb.build();

      return qb.getDynaPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  /**
   * Sets the doctors charge.
   *
   * @param con the con
   * @param patientId the patient id
   * @param doctorId the doctor id
   * @param userName the user name
   * @param consultationtypeid the consultationtypeid
   * @return the map
   * @throws Exception the exception
   */
  public Map<String, Object> setDoctorsCharge(Connection con, String patientId, String doctorId,
      String userName, int consultationtypeid) throws Exception {
    PreparedStatement ps = null;
    try {
      Map<String, Object> values = new HashMap<String, Object>();
      boolean success = true;
      String billno = new BillDAO(con).getPatientCreditBillOpenOnly(patientId, true, true);
      OrderBO order = new OrderBO();
      String setBillCheck = order.setBillInfo(con, patientId, billno, false, userName);
      if (setBillCheck != null) {
        values.put("checkForInactive", setBillCheck);
        success = false;
        values.put("status", success);
        return values;
      }
      List<ChargeDTO> charges = null;
      BasicDynaBean consTypeBean = null;
      consTypeBean = OrderBO.getConsultationTypeBean(consultationtypeid);
      BasicDynaBean doctor = DoctorMasterDAO.getDoctorCharges(doctorId, order.getBillRatePlanId(),
          order.getBedType());
      order.setPlanIds(new PatientInsurancePlanDAO().getPlanIds(patientId));
      charges = OrderBO.getDoctorConsCharges(doctor, consTypeBean, "i",
          OrgMasterDao.getOrgdetailsDynaBean(order.getBillRatePlanId()), BigDecimal.ONE,
          order.isInsurance(), order.getPlanIds(), order.getBedType(), patientId, null);

      ChargeDAO chargedao = new ChargeDAO(con);
      java.sql.Timestamp postedDate = new java.sql.Timestamp(new java.util.Date().getTime());
      /*
       * Set the common attributes for all charges
       */
      BasicDynaBean bill = order.getBill();
      billno = (String) bill.get("bill_no");
      DiscountPlanBO discBO = new DiscountPlanBO();
      // say some details abt discount plan
      discBO.setDiscountPlanDetails(bill.get("discount_category_id") != null ? (Integer) bill
          .get("discount_category_id") : 0);
      for (ChargeDTO charge : charges) {
        if (postedDate.getTime() < ((java.sql.Timestamp) bill.get("open_date")).getTime()) {
          postedDate = (java.sql.Timestamp) bill.get("open_date");
        }
        charge.setOrderAttributes(chargedao.getNextChargeId(), billno, userName, "", doctorId,
            postedDate);
        if (charge.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
          charge.setOverall_discount_amt(charge.getDiscount());
          charge.setOverall_discount_auth(-1);
        }
        charge.setOrderNumber(order.getCommonOrderId());
        charge.setPreAuthId(null);
        charge.setPreAuthModeId(1);
        charge.setHasActivity(false);
        if (charge.isAllowDiscount()) {
          discBO.applyDiscountRule(con, charge);
        }
      }
      if (charges.size() == 0) {
        success = false;
        values.put("status", success);
        return values;
      }
      ps = con.prepareStatement(INSERT_CHARGE);
      Iterator iterator = charges.iterator();
      while (iterator.hasNext()) {
        ChargeDTO chargeDTO = (ChargeDTO) iterator.next();
        setInsertChargeParams(chargeDTO, ps);
        ps.addBatch();
      }
      int[] results = ps.executeBatch();
      ps.close();
      for (int p = 0; p < results.length; p++) {
        if (results[p] <= 0) {
          success = false;
          break;
        }
      }

      if (success) {
        PatientInsurancePlanDAO planDAO = new PatientInsurancePlanDAO();
        BillChargeClaimDAO billChgClmDAO = new BillChargeClaimDAO();
        int[] planIds = planDAO.getPlanIds(patientId);
        if (null != planIds && planIds.length > 0) {
          billChgClmDAO.insertBillChargeClaims(con, charges, planIds, patientId, billno);
        }
      }

      ChargeDTO chargeDTO = charges.get(0);
      values.put("status", success);
      values.put("charge_id", chargeDTO.getChargeId());
      values.put("bill_no", billno);
      return values;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /** The Constant INSERT_CHARGE. */
  private static final String INSERT_CHARGE = "INSERT INTO bill_charge "
      + "(charge_id, bill_no, charge_group, charge_head, act_department_id,"
      + " act_description_id, act_description, act_remarks, act_rate, act_unit, act_quantity,"
      + " amount, discount, discount_reason, charge_ref, posted_date, status, username, mod_time, "
      + " approval_id, orig_rate, package_unit, doctor_amount, hasactivity, insurance_claim_amount,"
      + " payee_doctor_id, referal_amount, prescribing_dr_amount, prescribing_dr_id,"
      + " discount_auth_dr,dr_discount_amt,discount_auth_pres_dr,pres_dr_discount_amt,"
      + " discount_auth_ref,ref_discount_amt,discount_auth_hosp,hosp_discount_amt, "
      + " overall_discount_auth, overall_discount_amt, account_group, "
      + " act_item_code, act_rate_plan_item_code, order_number, allow_discount, "
      + " activity_conducted, conducted_datetime, service_sub_group_id, code_type, "
      + " conducting_doc_mandatory, consultation_type_id, user_remarks, "
      + " insurance_category_id,prior_auth_id,prior_auth_mode_id, first_of_category, op_id, "
      + " from_date, to_date, item_remarks,allow_rate_increase,allow_rate_decrease,redeemed_points,"
      + " amount_included, qty_included ) "
      + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
      + "  ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

  /**
   * Sets the insert charge params.
   *
   * @param charge the charge
   * @param ps the ps
   * @throws SQLException the SQL exception
   */
  private void setInsertChargeParams(ChargeDTO charge, PreparedStatement ps) throws SQLException {
    int idx = 1;
    ps.setString(idx++, charge.getChargeId());
    ps.setString(idx++, charge.getBillNo());
    ps.setString(idx++, charge.getChargeGroup());
    ps.setString(idx++, charge.getChargeHead());
    ps.setString(idx++, charge.getActDepartmentId());
    ps.setString(idx++, charge.getActDescriptionId());
    ps.setString(idx++, charge.getActDescription());
    ps.setString(idx++, charge.getActRemarks());
    ps.setBigDecimal(idx++, charge.getActRate());
    ps.setString(idx++, charge.getActUnit());
    ps.setBigDecimal(idx++, charge.getActQuantity());
    ps.setBigDecimal(idx++, charge.getAmount());
    ps.setBigDecimal(idx++, charge.getDiscount());
    ps.setString(idx++, charge.getDiscountReason());
    ps.setString(idx++, charge.getChargeRef());
    ps.setTimestamp(idx++, new Timestamp(charge.getPostedDate().getTime()));
    ps.setString(idx++, charge.getStatus());
    ps.setString(idx++, charge.getUsername());
    ps.setTimestamp(idx++, new Timestamp(charge.getModTime().getTime()));
    ps.setString(idx++, charge.getApprovalId());
    ps.setBigDecimal(idx++, charge.getOriginalRate());
    ps.setBigDecimal(idx++, charge.getPackageUnit());
    ps.setBigDecimal(idx++, charge.getDoctorAmount());
    ps.setBoolean(idx++, charge.getHasActivity());
    ps.setBigDecimal(idx++, charge.getInsuranceClaimAmount());
    ps.setString(idx++, charge.getPayeeDoctorId());
    ps.setBigDecimal(idx++, charge.getReferalAmount());
    ps.setBigDecimal(idx++, charge.getPrescribingDrAmount());
    ps.setString(idx++, charge.getPrescribingDrId());
    ps.setInt(idx++, charge.getDiscount_auth_dr());
    ps.setBigDecimal(idx++, charge.getDr_discount_amt());
    ps.setInt(idx++, charge.getDiscount_auth_pres_dr());
    ps.setBigDecimal(idx++, charge.getPres_dr_discount_amt());
    ps.setInt(idx++, charge.getDiscount_auth_ref());
    ps.setBigDecimal(idx++, charge.getRef_discount_amt());
    ps.setInt(idx++, charge.getDiscount_auth_hosp());
    ps.setBigDecimal(idx++, charge.getHosp_discount_amt());
    ps.setInt(idx++, charge.getOverall_discount_auth());
    ps.setBigDecimal(idx++, charge.getOverall_discount_amt());
    ps.setInt(idx++, charge.getAccount_group() > 0 ? charge.getAccount_group() : 1);
    ps.setString(idx++, charge.getActItemCode());
    ps.setString(idx++, charge.getActRatePlanItemCode());
    ps.setObject(idx++, charge.getOrderNumber());
    ps.setBoolean(idx++, charge.isAllowDiscount());
    ps.setString(idx++, charge.getActivityConducted());
    if (charge.getConductedDateTime() != null) {
      ps.setTimestamp(idx++, new Timestamp(charge.getConductedDateTime().getTime()));
    } else {
      ps.setTimestamp(idx++, null);
    }
    ps.setInt(idx++, charge.getServiceSubGroupId());
    ps.setString(idx++, charge.getCodeType());
    ps.setString(idx++, charge.getConducting_doc_mandatory());
    ps.setInt(idx++, charge.getConsultation_type_id());
    ps.setString(idx++, charge.getUserRemarks());
    ps.setInt(idx++, charge.getInsuranceCategoryId());
    ps.setString(idx++, charge.getPreAuthId());
    if (charge.getPreAuthModeId() == null) {
      ps.setNull(idx++, java.sql.Types.INTEGER);
    } else {
      ps.setInt(idx++, charge.getPreAuthModeId());
    }
    ps.setBoolean(idx++, charge.getFirstOfCategory());
    ps.setString(idx++, charge.getOp_id());
    ps.setTimestamp(idx++, charge.getFrom_date());
    ps.setTimestamp(idx++, charge.getTo_date());
    ps.setString(idx++, charge.getItemRemarks());
    ps.setBoolean(idx++, charge.isAllowRateIncrease());
    ps.setBoolean(idx++, charge.isAllowRateDecrease());
    ps.setInt(idx++, charge.getRedeemed_points());
    ps.setBigDecimal(idx++,
        charge.getAmount_included() == null ? BigDecimal.ZERO : charge.getAmount_included());
    ps.setBigDecimal(idx++,
        charge.getQty_included() == null ? BigDecimal.ZERO : charge.getQty_included());
  }

  /**
   * Update billable consultation.
   *
   * @param chargeId the charge id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static boolean updateBillableConsultation(String chargeId) throws SQLException,
      ServletException, IOException {
    Connection con = null;
    boolean success = true;
    DoctorsNotesDAO drnoteDAo = new DoctorsNotesDAO();
    try {
      BasicDynaBean noteBean = drnoteDAo.findByKey("charge_id", chargeId);
      if (noteBean != null) {
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);
        Map columnMap = new HashMap();
        columnMap.put("billable_consultation", "X");
        Map keyMap = new HashMap();
        keyMap.put("charge_id", chargeId);
        success = drnoteDAo.update(con, "ip_doctor_notes", columnMap, keyMap) >= 0;
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

}
