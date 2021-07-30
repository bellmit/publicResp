package com.insta.hms.ceed;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.messaging.InstaIntegrationDao;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 *  all database activities related to ceed integration are defined in this class
 */
public class CeedDAO {

  static Logger logger = LoggerFactory.getLogger(CeedDAO.class);

  private static final String GET_CLAIM_SEQ_NEXT_VAL = "SELECT"
      + " nextval('ceed_integration_claim_sequence')";
  
  private static final GenericDAO ceedIntegrationMainDAO = new GenericDAO("ceed_integration_main");
  private static final GenericDAO ceedIntegrationDetailsDAO =
      new GenericDAO("ceed_integration_details");
  
  /**
   * Gets next claim id from ceed_integration_claim_sequence.
   * 
   * @return Claim ID
   * @throws SQLException the SQL exception
   */
  public static int getNextClaimId() throws SQLException {
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(GET_CLAIM_SEQ_NEXT_VAL);) {
      return DataBaseUtil.getIntValueFromDb(ps);
    }
  }

  private static final String GET_PERSON_DETAILS = " SELECT"
      + " get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name,"
      + " pd.last_name) AS patname, "
      + "   pd.dateofbirth, CASE WHEN "
      + "pd.patient_gender is null || patient_gender = '' then 'U'"
      + " else pd.patient_gender end as patient_gender"
      + " FROM patient_details pd "
      + "   LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id) "
      + " WHERE pd.mr_no = ? ";

  /**
   * Get patient details.
   * 
   * @param mrNo MR number of patient
   * @return bean containing patient details
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getPersonDetails(String mrNo) throws SQLException {
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(GET_PERSON_DETAILS);) {
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaBean(ps);
    }
  }

  private static final String GET_ALL_DIAGNOSES = "SELECT "
      + " mrdd.icd_code, mrdd.code_type, mrdd.diag_type,"
      + " msc.code_type_classification FROM mrd_diagnosis mrdd "
      + "LEFT JOIN mrd_supported_codes msc"
      + " ON mrdd.code_type = msc.code_type"
      + " WHERE mrdd.visit_id = ?";

  /**
   * Get list of all diagnoses.
   * 
   * @param visitId Visit Identifier
   * @return List of bean, where each bean contains diagnosis details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAllDiagnoses(String visitId) throws SQLException {
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(GET_ALL_DIAGNOSES);) {
      ps.setString(1, visitId);
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  private static final String GET_OPERATION_ACTIVITIES = "SELECT "
      + " pp.patient_presc_id, ood.code_type,"
      + " ood.item_code, pp.prescribed_date, 1 AS qty, "
      + "msc.code_type_classification "
      + "FROM patient_prescription pp "
      + "JOIN patient_operation_prescriptions pop ON"
      + " pp.patient_presc_id=pop.prescription_id "
      + "JOIN doctor_consultation dc ON"
      + " dc.consultation_id = pp.consultation_id "
      + "JOIN patient_registration pr ON pr.patient_id=dc.patient_id "
      + "JOIN operation_org_details ood ON ood.org_id=pr.org_id AND"
      + " pop.operation_id=ood.operation_id "
      + " LEFT JOIN mrd_supported_codes msc ON ood.code_type = msc.code_type "
      + " AND msc.code_category = 'Treatment' "
      + "WHERE pp.consultation_id = ?";

  /**
   * Get list of operation activities for a specific consultation.
   * 
   * @param consultationId Consultation ID
   * @return List of bean, where each bean contains operation activities details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getOperationActivities(String consultationId)
      throws SQLException {
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(GET_OPERATION_ACTIVITIES);) {
      ps.setInt(1, Integer.parseInt(consultationId));
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  private static final String GET_SERVICE_ACTIVITIES = "SELECT"
      + " pp.patient_presc_id, sod.code_type, sod.item_code,"
      + " pp.prescribed_date, psp.qty, msc.code_type_classification "
      + "FROM patient_prescription pp "
      + "JOIN patient_service_prescriptions psp"
      + " ON pp.patient_presc_id=psp.op_service_pres_id "
      + "JOIN doctor_consultation dc"
      + " ON dc.consultation_id = pp.consultation_id "
      + "JOIN patient_registration pr ON pr.patient_id=dc.patient_id "
      + "JOIN service_org_details sod ON sod.org_id=pr.org_id"
      + " AND psp.service_id = sod.service_id "
      + " LEFT JOIN mrd_supported_codes msc"
      + " ON sod.code_type = msc.code_type "
      + " AND msc.code_category = 'Treatment' "
      + "WHERE pp.consultation_id = ?";

  /**
   * Get list of services activities for a specific consultation.
   * 
   * @param consultationId Consultation ID
   * @return List of bean, where each bean contains services activities details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getServiceActivities(String consultationId)
      throws SQLException {
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(GET_SERVICE_ACTIVITIES);) {
      ps.setInt(1, Integer.parseInt(consultationId));
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  private static final String GET_TEST_ACTIVITIES = "SELECT"
      + " pp.patient_presc_id, tod.code_type, tod.item_code,"
      + " pp.prescribed_date, 1 AS qty, msc.code_type_classification "
      + "FROM patient_prescription pp "
      + "JOIN patient_test_prescriptions ptp ON"
      + " pp.patient_presc_id=ptp.op_test_pres_id "
      + "JOIN doctor_consultation dc ON"
      + " dc.consultation_id = pp.consultation_id "
      + "JOIN patient_registration pr ON pr.patient_id=dc.patient_id "
      + "JOIN test_org_details tod ON tod.org_id=pr.org_id "
      + "AND ptp.test_id=tod.test_id "
      + "LEFT JOIN mrd_supported_codes msc"
      + " ON msc.code_type = tod.code_type "
      + " AND msc.code_category = 'Treatment' "
      + "WHERE pp.consultation_id = ?";

  /**
   * Get list of test activities for a specific consultation.
   * 
   * @param consultationId Consultation ID
   * @return List of bean, where each bean contains test activities details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getTestActivities(String consultationId) throws SQLException {
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(GET_TEST_ACTIVITIES);) {
      ps.setInt(1, Integer.parseInt(consultationId));
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  private static final String GET_MEDICINE_ACTIVITIES = "SELECT"
      + " pp.patient_presc_id, hict.code_type,"
      + " sic.item_code, pp.prescribed_date,"
      + " pmp.issued_qty AS qty, msc.code_type_classification FROM patient_prescription pp "
      + "JOIN patient_medicine_prescriptions pmp ON pp.patient_presc_id=pmp.op_medicine_pres_id "
      + "LEFT JOIN (select distinct code_type,medicine_id FROM ha_item_code_type hic"
      + " where hic.health_authority=?) as hict on (hict.medicine_id=pmp.medicine_id)"
      + "LEFT JOIN store_item_codes sic ON pmp.medicine_id=sic.medicine_id"
      + " AND hict.code_type=sic.code_type"
      + " LEFT JOIN mrd_supported_codes msc ON msc.code_type = hict.code_type "
      + " AND msc.code_category = 'Drug'"
      + " WHERE consultation_id = ? AND pmp.medicine_id IS NOT NULL";

  /**
   * Get list of medicine activities for a specific consultation.
   * 
   * @param consultationId  Consultation ID
   * @param healthAuthority Health Authority
   * @return List of bean, where each bean contains medicine activities details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getMedicineActivities(String consultationId,
      String healthAuthority) throws SQLException {
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(GET_MEDICINE_ACTIVITIES);) {
      ps.setString(1, healthAuthority);
      ps.setInt(2, Integer.parseInt(consultationId));
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  private static final String GET_TREATMENT_CODES_QUERY = "SELECT "
      + " bc.charge_id AS patient_presc_id, bc.code_type,"
      + " bc.act_rate_plan_item_code AS item_code,"
      + " bc.posted_date AS prescribed_date, bc.qty_included AS qty,"
      + " msc.code_type_classification"
      + " FROM bill_charge bc"
      + " JOIN bill b ON b.bill_no = bc.bill_no"
      + " JOIN chargehead_constants cc ON (cc.chargehead_id = bc.charge_head) "
      + " LEFT JOIN mrd_supported_codes msc ON msc.code_type = bc.code_type"
      + " AND msc.code_category = 'Treatment'"
      + " WHERE cc.codification_supported != 'N' AND b.visit_id = ? "
      + " AND bc.charge_head NOT IN ('PHCMED','PHMED','PHRET','PHCRET')  "
      + " AND bc.charge_group!='RET' AND bc.charge_group!='DOC' AND bc.status!='X' ";

  /**
   * Get list of treatment codes for a visit.
   * 
   * @param visitId         Visit ID
   * @return List of bean, where each bean contains treatment codes for a visit
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getTreatmentCodesList(String visitId) throws SQLException {
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(GET_TREATMENT_CODES_QUERY);) {
      ps.setString(1, visitId);
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  /**
   * Get bean with ceed credentials (username and password).
   * 
   * @param integrationName integration name for ceed
   * @return a bean with ceed credentials (username and password)
   * @throws SQLException the SQL Exception
   */
  public static BasicDynaBean getCredentialsBean(String integrationName) throws SQLException {
    InstaIntegrationDao idao = new InstaIntegrationDao();
    return idao.findByKey("integration_name", integrationName);
  }

  public static final String GET_RESPONSE_DETAILS = "SELECT "
      + " activity_id, claim_edit_rank, claim_edit_response_comments"
      + " FROM ceed_integration_details cid "
      + " JOIN ceed_integration_main cim using (claim_id) "
      + " WHERE consultation_id=? and cim.status='A' ";

  /**
   * Get list of response entries in database for a specific consultation.
   * 
   * @param consultationId Consultation ID
   * @return List of bean, where each bean contains response entries
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getResponseDetails(int consultationId) throws SQLException {
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(GET_RESPONSE_DETAILS);) {
      ps.setInt(1, consultationId);
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  public static final String GET_RESPONSE_DETAILS_VISIT = "SELECT"
      + " charge_id, claim_edit_rank, claim_edit_response_comments"
      + " FROM ceed_integration_details cid "
      + " JOIN ceed_integration_main cim using (claim_id) "
      + " WHERE visit_id=? and cim.status='A' ";

  /**
   * Get list of response entries in database for a specific consultation.
   * 
   * @param visitId Visit ID
   * @return List of bean, where each bean contains response entries
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getMrdResponseDetails(String visitId) throws SQLException {
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(GET_RESPONSE_DETAILS_VISIT);) {
      ps.setString(1, visitId);
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  /**
   * insert request details.
   * @param con Database Connection
   * @param claimId Claim ID
   * @param consultationId Consultation ID
   * @param visitId Visit ID
   * @param status the status
   * @param serviceType service type
   * @return returns true or false representing success of operation
   * @throws SQLException the SQL exception
   * @throws IOException the IO Exception
   */
  public static boolean insertRequestDetailsInCeedMain(Connection con, int claimId,
      int consultationId, String visitId, char status, char serviceType)
      throws SQLException, IOException {

    // update all earlier claims with same consultation id as inactive
    Map values = new HashMap();
    values.put("status", "I");

    Map keys = new HashMap();
    keys.put("consultation_id", consultationId);

    if (ceedIntegrationMainDAO.findByKey(con, "consultation_id", consultationId) != null
        && ceedIntegrationMainDAO.update(con, values, keys) == 0) {
      return false;
    }

    BasicDynaBean bean = ceedIntegrationMainDAO.getBean();

    bean.set("claim_id", claimId);
    bean.set("consultation_id", consultationId);
    bean.set("status", String.valueOf(status));
    bean.set("service_type", String.valueOf(serviceType));

    return ceedIntegrationMainDAO.insert(con, bean);
  }

  /**
   * insert mrd request details.
   * @param con Database Connection
   * @param claimId Claim ID
   * @param visitId Visit ID
   * @param status the status
   * @param serviceType service type
   * @return returns true or false representing success of operation
   * @throws SQLException the SQL exception
   * @throws IOException the IO Exception
   */
  public static boolean insertMrdRequestDetailsInCeedMain(Connection con, int claimId,
      String visitId, char status, char serviceType) throws SQLException, IOException {

    // update all earlier claims with same consultation id as inactive
    Map<String, Object> values = new HashMap<>();
    values.put("status", "I");

    Map<String, Object> keys = new HashMap<>();
    keys.put("visit_id", visitId);

    if (ceedIntegrationMainDAO.findByKey(con, "visit_id", visitId) != null
        && ceedIntegrationMainDAO.update(con, values, keys) == 0) {
      return false;
    }

    BasicDynaBean bean = ceedIntegrationMainDAO.getBean();

    bean.set("claim_id", claimId);
    bean.set("visit_id", visitId);
    bean.set("status", String.valueOf(status));
    bean.set("service_type", String.valueOf(serviceType));

    return ceedIntegrationMainDAO.insert(con, bean);
  }

  /**
   * Create CEED activity details.
   * @param con Database Connection
   * @param claimId Claim ID
   * @param activityId Activity ID
   * @param claimEditRefNum Claim Reference Number
   * @param claimEditId Claim Edit ID
   * @param claimEditSubType Claim Edit Subtype
   * @param claimEditCode Claim Edit Code
   * @param claimEditRank Claim Edit Rank
   * @param claimEditResponseComments Claim Edit Response Comments
   * @return returns true or false representing success of operation
   * @throws SQLException the SQL exception
   * @throws IOException the IO Exception
   */
  public static boolean insertCeedIntegrationDetails(Connection con, Integer claimId,
      Integer activityId, String claimEditRefNum, Integer claimEditId, String claimEditSubType,
      String claimEditCode, String claimEditRank, String claimEditResponseComments)
      throws SQLException, IOException {

    BasicDynaBean bean = ceedIntegrationDetailsDAO.getBean();

    bean.set("claim_id", claimId);
    bean.set("activity_id", activityId);
    bean.set("claim_ref_num", claimEditRefNum);
    bean.set("claim_edit_id", claimEditId);
    bean.set("claim_edit_sub_type", claimEditSubType);
    bean.set("claim_edit_code", claimEditCode);
    bean.set("claim_edit_rank", claimEditRank);
    bean.set("claim_edit_response_comments", claimEditResponseComments);

    return ceedIntegrationDetailsDAO.insert(con, bean);
  }

  /**
   * Update CEED activity details.
   * @param con Database Connection
   * @param claimId Claim ID
   * @param activityId Activity ID
   * @param claimEditRefNum Claim Reference Number
   * @param claimEditId Claim Edit ID
   * @param claimEditSubType Claim Edit Subtype
   * @param claimEditCode Claim Edit Code
   * @param claimEditRank Claim Edit Rank
   * @param claimEditResponseComments Claim Edit Response Comments
   * @return returns true or false representing success of operation
   * @throws SQLException the SQL exception
   * @throws IOException the IO Exception
   */
  public static boolean updateCeedIntegrationDetails(Connection con, Integer claimId,
      Integer activityId, String claimEditRefNum, Integer claimEditId, String claimEditSubType,
      String claimEditCode, String claimEditRank, String claimEditResponseComments)
      throws SQLException, IOException {
    Map<String, Object> values = new HashMap<>();
    values.put("claim_ref_num", claimEditRefNum);
    values.put("claim_edit_id", claimEditId);
    values.put("claim_edit_sub_type", claimEditSubType);
    values.put("claim_edit_code", claimEditCode);
    values.put("claim_edit_rank", claimEditRank);
    values.put("claim_edit_response_comments", claimEditResponseComments);

    Map<String, Object> keys = new HashMap<>();
    keys.put("claim_id", claimId);
    keys.put("activity_id", activityId);

    return ceedIntegrationDetailsDAO.update(con, values, keys) > 0;
  }

  /**
   * Create MRD CEED activity details.
   * @param con Database Connection
   * @param claimId Claim ID
   * @param claimEditRefNum Claim Reference Number
   * @param claimEditId Claim Edit ID
   * @param claimEditSubType Claim Edit Subtype
   * @param claimEditCode Claim Edit Code
   * @param claimEditRank Claim Edit Rank
   * @param claimEditResponseComments Claim Edit Response Comments
   * @return returns true or false representing success of operation
   * @throws SQLException the SQL exception
   * @throws IOException the IO Exception
   */
  public static boolean insertMrdCeedIntegrationDetails(Connection con, Integer claimId,
      String chargeId, String claimEditRefNum, Integer claimEditId, String claimEditSubType,
      String claimEditCode, String claimEditRank, String claimEditResponseComments)
      throws SQLException, IOException {

    BasicDynaBean bean = ceedIntegrationDetailsDAO.getBean();

    bean.set("claim_id", claimId);
    bean.set("charge_id", chargeId);
    bean.set("claim_ref_num", claimEditRefNum);
    bean.set("claim_edit_id", claimEditId);
    bean.set("claim_edit_sub_type", claimEditSubType);
    bean.set("claim_edit_code", claimEditCode);
    bean.set("claim_edit_rank", claimEditRank);
    bean.set("claim_edit_response_comments", claimEditResponseComments);

    return ceedIntegrationDetailsDAO.insert(con, bean);
  }

  /**
   * Update MRD CEED activity details.
   * @param con Database Connection
   * @param claimId Claim ID
   * @param claimEditRefNum Claim Reference Number
   * @param claimEditId Claim Edit ID
   * @param claimEditSubType Claim Edit Subtype
   * @param claimEditCode Claim Edit Code
   * @param claimEditRank Claim Edit Rank
   * @param claimEditResponseComments Claim Edit Response Comments
   * @return returns true or false representing success of operation
   * @throws SQLException the SQL exception
   * @throws IOException the IO Exception
   */
  public static boolean updateMrdCeedIntegrationDetails(Connection con, Integer claimId,
      String chargeId, String claimEditRefNum, Integer claimEditId, String claimEditSubType,
      String claimEditCode, String claimEditRank, String claimEditResponseComments)
      throws SQLException, IOException {

    Map<String, Object> values = new HashMap<>();
    values.put("claim_ref_num", claimEditRefNum);
    values.put("claim_edit_id", claimEditId);
    values.put("claim_edit_sub_type", claimEditSubType);
    values.put("claim_edit_code", claimEditCode);
    values.put("claim_edit_rank", claimEditRank);
    values.put("claim_edit_response_comments", claimEditResponseComments);

    Map<String, Object> keys = new HashMap<>();
    keys.put("claim_id", claimId);
    keys.put("charge_id", chargeId);

    return ceedIntegrationDetailsDAO.update(con, values, keys) > 0;
  }

  /**
   * Update Request Timestamp.
   * @param con Database Connection
   * @param claimId Claim ID
   * @return returns true or false representing success of operation
   * @throws SQLException the SQL exception
   * @throws IOException the IO Exception
   */
  public static boolean updateRequestTime(Connection con, int claimId)
      throws SQLException, IOException {
    Map<String, Object>  values = new HashMap<>();
    values.put("request_datetime", DateUtil.getCurrentTimestamp());

    Map<String, Object>  keys = new HashMap<>();
    keys.put("claim_id", claimId);

    return ceedIntegrationMainDAO.update(con, values, keys) > 0;
  }

  /**
   * Update Response Information Message.
   * @param con Database Connection
   * @param claimId Claim ID
   * @param infoMessage Response Information Message
   * @return returns true or false representing success of operation
   * @throws SQLException the SQL exception
   * @throws IOException the IO Exception
   */
  public static boolean updateResponseInfo(Connection con, int claimId, String infoMessage)
      throws SQLException, IOException {
    Map<String, Object> values = new HashMap<>();
    values.put("info_message", infoMessage);
    values.put("response_datetime", DateUtil.getCurrentTimestamp());

    Map<String, Object> keys = new HashMap<>();
    keys.put("claim_id", claimId);

    return ceedIntegrationMainDAO.update(con, values, keys) > 0;
  }

  private static final String GET_ACTIVE_CLAIM = "SELECT * FROM ceed_integration_main"
      + " WHERE consultation_id = ?"
      + " AND status = ?";

  /**
   * checks if ceed check is done for a specific consultation or not.
   * @param consultationId Consultation ID
   * @return Bean containing details of ceed data.
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean checkIfCeedCheckDone(int consultationId) throws SQLException {
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(GET_ACTIVE_CLAIM);) {
      ps.setInt(1, consultationId);
      ps.setString(2, "A");
      return DataBaseUtil.queryToDynaBean(ps);
    }
  }

  private static final String GET_ACTIVE_CLAIM_VISIT = "SELECT * FROM ceed_integration_main"
      + " WHERE visit_id = ? AND status = ?";

  /**
   * checks if ceed check is done for a specific visit or not.
   * @param visitId Visit ID
   * @return Bean containing details of ceed data.
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean checkIfMrdCeedCheckDone(String visitId) throws SQLException {
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(GET_ACTIVE_CLAIM_VISIT);) {
      ps.setString(1, visitId);
      ps.setString(2, "A");
      return DataBaseUtil.queryToDynaBean(ps);
    }
  }

  private static final String GET_CONSID_LIST_CLAIM_STATUS = "SELECT"
      + " dc.consultation_id, temp.status"
      + " FROM ((SELECT consultation_id FROM doctor_consultation WHERE consultation_id IN "
      + "(#) AS dc LEFT JOIN (SELECT * FROM ceed_integration_main "
      + "WHERE status = ?) AS temp ON dc.consultation_id = temp.consultation_id)";

  /**
   * checks if ceed check is done for a list of consultation ids.
   * @param consIdList List of consultation IDs
   * @return List of Bean containing details of ceed data.
   * @throws SQLException the SQL Exception
   */
  public static List<BasicDynaBean> checkIfCeedCheckDoneForList(List<Integer> consIdList)
      throws SQLException {
    String[] valuesArr = new String[consIdList.size()];
    Arrays.fill(valuesArr, "?");
    String placeholder = StringUtils.arrayToCommaDelimitedString(valuesArr);
    List<Object> params = new ArrayList<>();
    params.addAll(consIdList);
    params.add("A");
    return DataBaseUtil.queryToDynaList(
        GET_CONSID_LIST_CLAIM_STATUS.replaceAll("#", placeholder), params.toArray());
  }

  private static final String GET_ENCOUNTER_TYPE = "SELECT"
      + " encounter_type, encounter_start_type, encounter_end_type"
      + " FROM patient_registration"
      + " WHERE patient_id = ?";

  /**
   * Get encounter type for a visit.
   * @param visitId Visit ID
   * @return Bean containing details of encounter.
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getEncounterDetails(String visitId) throws SQLException {
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(GET_ENCOUNTER_TYPE)) {
      ps.setString(1, visitId);
      List<BasicDynaBean> rs = DataBaseUtil.queryToDynaList(ps);
      BasicDynaBean bean = (BasicDynaBean) rs.iterator().next();

      return bean;
    }
  }

  private static final String GET_OBSERVATIONS = "SELECT"
      + " bc.charge_id, mo.observation_id, mo.observation_type,"
      + " mo.code, mo.value, mo.value_type  FROM bill b "
      + "JOIN bill_charge bc ON b.bill_no = bc.bill_no "
      + "JOIN mrd_observations mo ON mo.charge_id = bc.charge_id "
      + "WHERE b.visit_id = ?";

  /**
   * Get Observations for a visit.
   * @param visitId Visit ID
   * @return List of Observations
   * @throws SQLException the SQL Exception
   */
  public static List<BasicDynaBean> getObservations(String visitId) throws SQLException {
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(GET_OBSERVATIONS)) {
      ps.setString(1, visitId);
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

}
