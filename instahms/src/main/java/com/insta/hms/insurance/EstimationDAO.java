package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Class EstimationDAO.
 *
 * @author lakshmi.p
 */
public class EstimationDAO {

  /**
   * Gets the bed types.
   *
   * @return the bed types
   * @throws SQLException the SQL exception
   */
  public static List getBedTypes() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List bedList = null;
    String query = "select distinct bed_type from bed_details bd where bd.bed_status = 'A' union "
        + "select distinct intensive_bed_type as bed_type from icu_bed_charges ibc"
        + " where ibc.bed_status = 'A' order by bed_type";
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(query);
      bedList = DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }
    return bedList;
  }

  /**
   * Gets the bed ward charges.
   *
   * @param orgid the orgid
   * @return the bed ward charges
   * @throws SQLException the SQL exception
   */
  public List getBedWardCharges(String orgid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List list = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con
          .prepareStatement("select * from bed_details where organization=? and bed_status='A'");
      ps.setString(1, orgid);
      list = DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }
    return list;
  }

  /** The Constant GET_ICU_WARD_QUERY. */
  /*
   * Retrieve a list of ICU bedcharges
   */
  private static final String GET_ICU_WARD_QUERY = "SELECT "
      + " ic.intensive_bed_type, ic.bed_charge, ic.hourly_charge, "
      + "  ic.nursing_charge, ic.duty_charge, ic.maintainance_charge, ic.luxary_tax "
      + " FROM bed_names bn "
      + "  JOIN icu_bed_charges ic ON (bn.bed_type = ic.intensive_bed_type) "
      + "  JOIN ward_names w ON (bn.ward_no=w.ward_no) "
      + " WHERE ic.bed_status='A' AND w.status='A' ";

  /** The Constant GET_ICU_WARD_CHARGES. */
  private static final String GET_ICU_WARD_CHARGES = GET_ICU_WARD_QUERY
      + " AND ic.bed_type=? AND ic.organization=? " + " UNION " + GET_ICU_WARD_QUERY
      + " AND organization='ORG0001' AND ic.bed_type='GENERAL' "
      + " AND NOT EXISTS (SELECT bed_type FROM icu_bed_charges "
      + " WHERE intensive_bed_type=bn.bed_type AND bed_type=? AND organization=?)";

  /**
   * Gets the ICU ward charges.
   *
   * @param bedType the bed type
   * @param orgid   the orgid
   * @return the ICU ward charges
   * @throws SQLException the SQL exception
   */
  public List getIcuWardCharges(String bedType, String orgid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List list = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_ICU_WARD_CHARGES);
      ps.setString(1, bedType);
      ps.setString(2, orgid);
      ps.setString(3, bedType);
      ps.setString(4, orgid);
      list = DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }
    return list;
  }

  /**
   * Gets the insurance patient details.
   *
   * @param insuranceID the insurance ID
   * @return the insurance patient details
   * @throws SQLException the SQL exception
   */
  public static EstimatePatientDetails getInsurancePatientDetails(String insuranceID)
      throws SQLException {
    String patientVisitDetailsQuery = " SELECT  pd.mr_no,sm.salutation,pd.patient_name,"
        + " pd.last_name,pd.patient_gender,pd.patient_phone,"
        + " pd.oldmrno,get_patient_age(pd.dateofbirth, pd.expected_dob) as PATIENT_AGE,"
        + " get_patient_age_in(pd.dateofbirth, pd.expected_dob) as PATIENT_AGE_IN, "
        + " pra.patient_id, pra.reg_date, "
        + " pra.reg_time,pra.doctor,dr.doctor_name,pra.dept_name AS dept_id,"
        + " (CASE WHEN pra.visit_type IS NULL THEN 'o' ELSE pra.visit_type END) as visit_type,"
        + " dep.dept_name,COALESCE(eh.organization_id,pra.org_id) as organization_id,od.org_name,"
        + " COALESCE(eh.bed_type,pra.bed_type) as bed_type,ic.insurance_id,"
        + " pra.center_id FROM patient_details pd "
        + " LEFT JOIN patient_registration pra ON (pd.mr_no = pra.mr_no AND pra.status='A')"
        + " LEFT OUTER JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)"
        + " LEFT OUTER JOIN department dep ON (pra.dept_name = dep.dept_id)"
        + " LEFT OUTER JOIN doctors dr on (pra.doctor = dr.doctor_id) "
        + " LEFT OUTER JOIN insurance_case ic ON (ic.mr_no = pd.mr_no) "
        + " LEFT OUTER JOIN insurance_estimate ie on ie.insurance_id = ic.insurance_id "
        + " LEFT OUTER JOIN estimate_header eh ON (eh.estimate_id = ie.estimate_id)"
        + " LEFT OUTER JOIN organization_details od on "
        + " (COALESCE(eh.organization_id,pra.org_id) = od.org_id)  where ic.insurance_id=?";
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(patientVisitDetailsQuery);) {
      ps.setInt(1, Integer.parseInt(insuranceID));
      try (ResultSet rs = ps.executeQuery()) {
        EstimatePatientDetails pd = null;
        if (rs.next()) {
          pd = new EstimatePatientDetails();
          populatePatientDetails(pd, rs);
          pd.setInsurance_id(rs.getString("insurance_id"));
        }
        return pd;
      }
    }
  }

  /**
   * Populate patient details.
   *
   * @param pd the pd
   * @param rs the rs
   * @throws SQLException the SQL exception
   */
  private static void populatePatientDetails(EstimatePatientDetails pd, ResultSet rs)
      throws SQLException {

    pd.setMrNo(rs.getString("mr_no"));
    pd.setTitle(rs.getString("salutation"));
    pd.setFirstName(rs.getString("patient_name"));
    pd.setLastName(rs.getString("last_name"));
    pd.setAge(rs.getString("PATIENT_AGE"));
    pd.setAgeIn(rs.getString("PATIENT_AGE_IN"));
    pd.setGender(rs.getString("patient_gender"));
    pd.setPhone(rs.getString("patient_phone"));
    pd.setOldMrno(rs.getString("oldmrno"));
    pd.setPatientId(rs.getString("patient_id"));
    pd.setDoctorId(rs.getString("doctor"));
    pd.setDoctorName(rs.getString("doctor_name"));
    pd.setDepartmentId(rs.getString("dept_id"));
    pd.setDepartmentName(rs.getString("dept_name"));
    pd.setOrganizationId(rs.getString("organization_id"));
    pd.setOrganizationName(rs.getString("org_name"));
    pd.setBedType(rs.getString("bed_type"));
    pd.setRegDate(rs.getDate("reg_date"));
    pd.setRegTime(rs.getTime("reg_time"));
    pd.setPatientType(rs.getString("visit_type"));
    pd.setCenterId(rs.getInt("center_id"));

  }

  /** The Constant INSURANCE_ESTIMATE_ID_QUERY. */
  private static final String INSURANCE_ESTIMATE_ID_QUERY = "SELECT estimate_id "
      + " from insurance_estimate where insurance_id=?";

  /** The Constant BILL_ESTIMATE_ID_QUERY. */
  private static final String BILL_ESTIMATE_ID_QUERY = "SELECT estimate_id "
      + " from bill where bill_no=?";

  /**
   * Gets the estimate ID.
   *
   * @param id       the id
   * @param moduleId the module id
   * @return the estimate ID
   * @throws SQLException the SQL exception
   */
  public String getEstimateID(String id, String moduleId) throws SQLException {
    String estID = null;
    if (moduleId.equals("mod_billing")) {
      try (PreparedStatement ps = con.prepareStatement(BILL_ESTIMATE_ID_QUERY);) {
        ps.setString(1, id);
        try (ResultSet rs = ps.executeQuery();) {
          if (rs.next()) {
            estID = rs.getString(1);
          }
        }
      }
    }
    if (moduleId.equals("mod_insurance")) {
      try (PreparedStatement ps = con.prepareStatement(INSURANCE_ESTIMATE_ID_QUERY);) {
        ps.setInt(1, Integer.parseInt(id));
        try (ResultSet rs = ps.executeQuery();) {
          if (rs.next()) {
            estID = rs.getString(1);
          }
        }
      }
    }
    return estID;
  }

  /** The Constant CHARGE_QUERY. */
  private static final String CHARGE_QUERY = "select "
      + " distinct insurance_estimate.*,chargegroup_name,chargehead_name "
      + " from insurance_estimate JOIN chargehead_constants ON "
      + " (insurance_estimate.charge_head = chargehead_constants.chargehead_id)"
      + " JOIN chargegroup_constants ON "
      + " (insurance_estimate.charge_group_id = chargegroup_constants.chargegroup_id) "
      + " where estimate_id=? order by charge_id";

  /**
   * Gets the charge list.
   *
   * @param estimateID the estimate ID
   * @return the charge list
   * @throws SQLException the SQL exception
   */
  // get a charge list given a single query
  public List getChargeList(String estimateID) throws SQLException {
    ArrayList list = new ArrayList();
    try (PreparedStatement stmt = con.prepareStatement(CHARGE_QUERY);) {
      stmt.setString(1, estimateID);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          EstimationDTO estimate = new EstimationDTO();
          populateEstimationDTO(estimate, rs);
          list.add(estimate);
        }
      }
    }
    return list;
  }

  /**
   * Populate estimation DTO.
   *
   * @param estimate the estimate
   * @param rs       the rs
   * @throws SQLException the SQL exception
   */
  private void populateEstimationDTO(EstimationDTO estimate, ResultSet rs) throws SQLException {
    estimate.setActDescription(rs.getString("activity_desc"));
    estimate.setActQuantity(rs.getBigDecimal("qty"));
    estimate.setActRate(rs.getBigDecimal("rate"));
    estimate.setActRemarks(rs.getString("remarks"));
    estimate.setAmount(rs.getBigDecimal("amt"));
    estimate.setApprovedAmt(rs.getBigDecimal("approvedamt"));
    estimate.setChargeGroup(rs.getString("charge_group_id"));
    estimate.setChargeGroupName(rs.getString("chargegroup_name"));
    estimate.setChargeHead(rs.getString("charge_head"));
    estimate.setChargeHeadName(rs.getString("chargehead_name"));
    estimate.setChargeID(rs.getString("charge_id"));
    estimate.setChargeRef(rs.getString("charge_ref"));
    estimate.setDiscount(rs.getBigDecimal("discount"));
    estimate.setEstimateID(rs.getString("estimate_id"));
    estimate.setUpdatedDate(rs.getDate("updated_date"));
  }

  /** The con. */
  private Connection con = null;

  /**
   * Instantiates a new estimation DAO.
   */
  public EstimationDAO() {
  }

  /**
   * Instantiates a new estimation DAO.
   *
   * @param con the con
   */
  public EstimationDAO(Connection con) {
    // Always construct a DAO by passing in the connection.
    this.con = con;
  }

  /** The Constant UPDATE_CHARGE. */
  public static final String UPDATE_CHARGE = "UPDATE insurance_estimate "
      + " SET remarks=?, rate=?, qty=?, discount=?, amt=?,"
      + " approvedamt=?, updated_date=current_timestamp::timestamp WHERE charge_id=?";

  /**
   * Update charge amounts list.
   *
   * @param estimateID the estimate ID
   * @param list       the list
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updateChargeAmountsList(String estimateID, List list) throws SQLException {
    boolean success = true;
    try (PreparedStatement ps = con.prepareStatement(UPDATE_CHARGE);) {
      Iterator iterator = list.iterator();
      while (iterator.hasNext()) {
        EstimationDTO estimation = (EstimationDTO) iterator.next();
        setUpdateChargeAmountParams(estimation, ps);
        ps.addBatch();
      }
      int[] results = ps.executeBatch();
      for (int p = 0; p < results.length; p++) {
        if (results[p] <= 0) {
          success = false;
          break;
        }
      }
    }
    return success;
  }

  /**
   * Sets the update charge amount params.
   *
   * @param estimation the estimation
   * @param ps         the ps
   * @throws SQLException the SQL exception
   */
  private void setUpdateChargeAmountParams(EstimationDTO estimation, PreparedStatement ps)
      throws SQLException {
    int index = 1;
    ps.setString(index++, estimation.getActRemarks());
    ps.setBigDecimal(index++, estimation.getActRate());
    ps.setBigDecimal(index++, estimation.getActQuantity());
    ps.setBigDecimal(index++, estimation.getDiscount());
    ps.setBigDecimal(index++, estimation.getAmount());
    ps.setBigDecimal(index++, new BigDecimal(0));
    // primary key

    // TODO:: need to insert int value of charge id
    String chargeId = estimation.getChargeID();
    ps.setString(index++, chargeId);
  }

  /** The Constant ESTIMATE_HEAD_INSERT. */
  public static final String ESTIMATE_HEAD_INSERT = "INSERT into "
      + " estimate_header(estimate_id,user_id,organization_id,bed_type,updated_date) "
      + "values(?,?,?,?,to_timestamp(to_char(current_timestamp,'YYYY-MM-DD HH:MI:SS'),"
      + "'YYYY-MM-DD HH:MI:SS')::timestamp)";

  /**
   * Insert estimate head.
   *
   * @param estmateID the estmate ID
   * @param orgId     the org id
   * @param bedType   the bed type
   * @param user      the user
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean insertEstimateHead(int estmateID, String orgId, String bedType, String user)
      throws SQLException {
    String estID = estmateID + "";
    try (PreparedStatement ps = con.prepareStatement(ESTIMATE_HEAD_INSERT);) {
      ps.setString(1, estID);
      ps.setString(2, user);
      ps.setString(3, orgId);
      ps.setString(4, bedType);
      int updatedRows = ps.executeUpdate();
      return updatedRows > 0;
    }
  }

  /** The Constant ESTIMATE_HEAD_UPDATE. */
  public static final String ESTIMATE_HEAD_UPDATE = "UPDATE estimate_header set user_id=?"
      + ",updated_date=to_timestamp(to_char(current_timestamp,'YYYY-MM-DD HH:MI:SS'),"
      + "'YYYY-MM-DD HH:MI:SS')::timestamp where estimate_id=?";

  /**
   * Update estimate head.
   *
   * @param estmateID the estmate ID
   * @param user      the user
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updateEstimateHead(String estmateID, String user) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(ESTIMATE_HEAD_UPDATE);) {
      ps.setString(1, user);
      ps.setString(2, estmateID);
      int rowsUpdated = ps.executeUpdate();
      return rowsUpdated > 0;
    }
  }

  /** The Constant INSURANCE_ESTIMATEID. */
  public static final String INSURANCE_ESTIMATEID = "UPDATE "
      + " insurance_case set estimate_id=? where insurance_id=?";

  /** The Constant BILL_ESTIMATEID. */
  public static final String BILL_ESTIMATEID = "UPDATE bill set estimate_id=? where bill_no=?";

  /**
   * Update insurance estimate ID.
   *
   * @param estmateID   the estmate ID
   * @param insuranceID the insurance ID
   * @param billNo      the bill no
   * @param moduleId    the module id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updateInsuranceEstimateID(int estmateID, String insuranceID, String billNo,
      String moduleId) throws SQLException {
    int rowsUpdated = 0;
    if (moduleId.equals("mod_insurance")) {
      try (PreparedStatement ps = con.prepareStatement(INSURANCE_ESTIMATEID);) {
        ps.setInt(1, estmateID);
        ps.setInt(2, Integer.parseInt(insuranceID));
        rowsUpdated = ps.executeUpdate();
      }
    }
    if (moduleId.equals("mod_billing")) {
      try (PreparedStatement ps = con.prepareStatement(BILL_ESTIMATEID);) {
        ps.setInt(1, estmateID);
        ps.setString(2, billNo);
        rowsUpdated = ps.executeUpdate();
      }
    }
    return rowsUpdated > 0;
  }

  /** The Constant INSURANCE. */
  public static final String INSURANCE = "UPDATE insurance_case set "
      + "est_mod_date=to_timestamp(to_char(current_timestamp,'YYYY-MM-DD HH:MI:SS'),"
      + "'YYYY-MM-DD HH:MI:SS')::timestamp"
      + ",est_transaction_type=?,estimate_amount=? where insurance_id=?";

  /**
   * Update insurance.
   *
   * @param est the est
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updateInsurance(Estimate est) throws SQLException {
    String insuranceID = est.getInsuranceId();
    String billNo = est.getBillNo();
    String moduleId = est.getModuleId();
    BigDecimal totAmt = est.getTotalAmt();
    int updatedRows = 0;
    if (moduleId.equals("mod_insurance")) {
      try (PreparedStatement ps = con.prepareStatement(INSURANCE);) {
        ps.setString(1, "S");
        ps.setBigDecimal(2, totAmt);
        ps.setInt(3, Integer.parseInt(insuranceID));
        updatedRows = ps.executeUpdate();
      }
    }
    return updatedRows > 0;
  }

  /** The Constant CHARGEID_SEQUENCE. */
  private static final String CHARGEID_SEQUENCE = "SELECT nextval('estimate_chargeid_sequence')";

  /**
   * Gets the charge ID sequence.
   *
   * @return the charge ID sequence
   * @throws SQLException the SQL exception
   */
  public String getChargeIDSequence() throws SQLException {
    String chrgSeq = null;
    try (PreparedStatement ps = con.prepareStatement(CHARGEID_SEQUENCE);) {
      try (ResultSet rs = ps.executeQuery();) {
        rs.next();
        chrgSeq = rs.getString(1);
      }
    }
    return chrgSeq;
  }

  /** The Constant ESTIMATE_SEQUENCE. */
  private static final String ESTIMATE_SEQUENCE = "SELECT nextval('estimate_id_sequence')";

  /**
   * Gets the estimate sequence.
   *
   * @return the estimate sequence
   * @throws SQLException the SQL exception
   */
  public int getEstimateSequence() throws SQLException {
    int estSeq = 0;
    try (PreparedStatement ps = con.prepareStatement(ESTIMATE_SEQUENCE);) {
      try (ResultSet rs = ps.executeQuery();) {
        rs.next();
        estSeq = rs.getInt(1);
      }
    }
    return estSeq;
  }

  /** The Constant INSERT_CHARGE. */
  private static final String INSERT_CHARGE = "INSERT INTO "
      + " insurance_estimate(charge_id, charge_group_id,"
      + " charge_head, activity_desc,remarks, rate, qty, discount, amt, approvedamt,"
      + " updated_date,charge_ref,estimate_id,insurance_id) "
      + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,current_timestamp::timestamp,?,?,?)";

  /**
   * Insert charges.
   *
   * @param est the est
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean insertCharges(Estimate est) throws SQLException {
    boolean success = true;
    String estimateID = est.getEstimateID();
    String insuranceID = est.getInsuranceId();
    String billNo = est.getBillNo();
    String moduleId = est.getModuleId();
    List list = est.getInsertEstimationChargeList();
    String bedType = est.getBedType();
    String organizationId = est.getOrgId();
    String loginUser = est.getUser();
    int estmateSeq = 0;
    if (estimateID == null || estimateID.equals("")) {
      estmateSeq = getEstimateSequence();
    } else {
      estmateSeq = Integer.parseInt(estimateID);
    }
    try (PreparedStatement ps = con.prepareStatement(INSERT_CHARGE);) {
      Iterator iterator = list.iterator();
      while (iterator.hasNext()) {
        EstimationDTO estimation = (EstimationDTO) iterator.next();
        ps.setInt(12, estmateSeq);
        ps.setInt(13, Integer.parseInt(insuranceID));
        setInsertChargeParams(estimation, ps);
        ps.addBatch();
      }
      int[] results = ps.executeBatch();
      boolean estimateHeader = false;
      if (est.getHeaderFlag()) {
        estimateHeader = updateEstimateHead(estimateID, loginUser);
      } else {
        estimateHeader = insertEstimateHead(estmateSeq, organizationId, bedType, loginUser);
      }
      for (int p = 0; p < results.length; p++) {
        if (results[p] <= 0) {
          success = false;
          break;
        }
      }
      if (!estimateHeader) {
        success = false;
      }
    }
    return success;
  }

  /** The new charg ID. */
  String newChargID = null;

  /**
   * Sets the insert charge params.
   *
   * @param estimation the estimation
   * @param ps         the ps
   * @throws SQLException the SQL exception
   */
  private void setInsertChargeParams(EstimationDTO estimation, PreparedStatement ps)
      throws SQLException {
    String chargeID = getChargeIDSequence();
    String chargeRef = estimation.getChargeRef();
    if (chargeRef.equals("_")) {
      estimation.setChargeRef("");
      newChargID = chargeID;
    } else {
      estimation.setChargeRef(newChargID);
    }
    ps.setString(1, chargeID);
    ps.setString(2, estimation.getChargeGroup());
    ps.setString(3, estimation.getChargeHead());
    ps.setString(4, estimation.getActDescription());
    ps.setString(5, estimation.getActRemarks());
    ps.setBigDecimal(6, estimation.getActRate());
    ps.setBigDecimal(7, estimation.getActQuantity());
    ps.setBigDecimal(8, estimation.getDiscount());
    ps.setBigDecimal(9, estimation.getAmount());
    ps.setBigDecimal(10, new BigDecimal(0));
    ps.setString(11, estimation.getChargeRef());
  }

  /** The Constant DELETE_CHARGE. */
  private static final String DELETE_CHARGE = "DELETE FROM insurance_estimate WHERE charge_id=?";

  /**
   * Delete charges.
   *
   * @param chargeIdList the charge id list
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean deleteCharges(List chargeIdList) throws SQLException {
    boolean success = true;
    try (PreparedStatement ps = con.prepareStatement(DELETE_CHARGE);) {
      EstimationDTO dto = null;
      Iterator iterator = chargeIdList.iterator();
      while (iterator.hasNext()) {
        dto = (EstimationDTO) iterator.next();
        ps.setString(1, dto.getChargeID());
        ps.addBatch();
      }
      int[] results = ps.executeBatch();
      for (int p = 0; p < results.length; p++) {
        if (results[p] <= 0) {
          success = false;
          break;
        }
      }
    }
    return success;
  }

  /** The Constant GET_CHARGE_GROUPS. */
  /*
   * Query the masters for charge heads and charge groups
   */
  private static final String GET_CHARGE_GROUPS = "SELECT "
      + " chargegroup_id, chargegroup_name,ip_applicable,op_applicable,"
      + " associated_module, display_order,dependent_module "
      + " FROM chargegroup_constants ORDER BY display_order ";

  /**
   * Gets the charge group const names.
   *
   * @return the charge group const names
   * @throws SQLException the SQL exception
   */
  public List getChargeGroupConstNames() throws SQLException {
    PreparedStatement ps = con.prepareStatement(GET_CHARGE_GROUPS);
    List list = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    return list;
  }

  /** The Constant GET_CHARGE_HEADS. */
  private static final String GET_CHARGE_HEADS = "SELECT "
      + " chargegroup_id, chargehead_id, chargehead_name,ip_applicable,op_applicable,"
      + " associated_module, display_order,ordereable,dependent_module,insurance_payable "
      + " FROM chargehead_constants ORDER BY display_order ";

  /**
   * Gets the charge head const names.
   *
   * @return the charge head const names
   * @throws SQLException the SQL exception
   */
  public List getChargeHeadConstNames() throws SQLException {
    PreparedStatement ps = con.prepareStatement(GET_CHARGE_HEADS);
    List list = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    return list;
  }

  /** The Constant getPatientVisitDetailsQuery. */
  private static final String getPatientVisitDetailsQuery = "SELECT "
      + " eh.estimate_id,eh.organization_id,"
      + " od.org_name,eh.bed_type from estimate_header eh"
      + " LEFT OUTER JOIN insurance_estimate ie ON (ie.estimate_id = eh.estimate_id)"
      + " LEFT OUTER JOIN organization_details od ON (od.org_id = eh.organization_id)"
      + " where ie.insurance_id = ? ";

  /**
   * Gets the patient visit details.
   *
   * @param insuranceID the insurance ID
   * @return the patient visit details
   * @throws SQLException the SQL exception
   */
  public static EstimatePatientDetails getPatientVisitDetails(String insuranceID)
      throws SQLException {
    EstimatePatientDetails pd = null;
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(getPatientVisitDetailsQuery);) {
      ps.setInt(1, Integer.parseInt(insuranceID));
      try (ResultSet rs = ps.executeQuery();) {
        if (rs.next()) {
          pd = new EstimatePatientDetails();
          pd.setEstimate_id(rs.getString("estimate_id"));
          pd.setOrganizationId(rs.getString("organization_id"));
          pd.setOrganizationName(rs.getString("org_name"));
          pd.setBedType(rs.getString("bed_type"));
        }
      }
    }
    return pd;
  }

  /** The Constant GET_BILL_NUMBER. */
  private static final String GET_BILL_NUMBER = "Select "
      + " pr.mr_no, b.visit_id, b.bill_no "
      + " from bill b  "
      + " join patient_registration pr  "
      + "on (b.visit_id = pr.patient_id)  join insurance_case ic "
      + " on (ic.mr_no = pr.mr_no) where ic.insurance_id=?  and b.bill_type = 'C' "
      + " and b.restriction_type='N' ";

  /**
   * Gets the bill no.
   *
   * @param insuranceId the insurance id
   * @return the bill no
   * @throws SQLException the SQL exception
   */
  public static String getBillNo(String insuranceId) throws SQLException {
    String billNo = null;
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement stmt = con.prepareStatement(GET_BILL_NUMBER);) {
      stmt.setInt(1, Integer.parseInt(insuranceId));
      try (ResultSet rs = stmt.executeQuery();) {
        if (rs.next()) {
          billNo = rs.getString("bill_no");
        }
      }
    }
    return billNo;
  }

}
