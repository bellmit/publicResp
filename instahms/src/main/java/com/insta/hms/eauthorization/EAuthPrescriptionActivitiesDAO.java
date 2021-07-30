/**
 *
 */

package com.insta.hms.eauthorization;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.medicalrecorddepartment.MRDUpdateScreenDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class EAuthPrescriptionActivitiesDAO.
 *
 * @author lakshmi
 */
public class EAuthPrescriptionActivitiesDAO extends GenericDAO {

  /**
   * The logger.
   */
  static Logger logger = LoggerFactory
      .getLogger(EAuthPrescriptionActivitiesDAO.class);

  /**
   * The e auth presc obs DAO.
   */
  GenericDAO eauthPrescObsDAO = new GenericDAO(
      "preauth_activities_observations");

  /**
   * The eauthdao.
   */
  EAuthPrescriptionDAO eauthdao = new EAuthPrescriptionDAO();

  /**
   * Instantiates a new e auth prescription activities DAO.
   */
  public EAuthPrescriptionActivitiesDAO() {
    super("preauth_prescription_activities");
  }

  /**
   * Copy pre auth presc attributes.
   *
   * @param visitId            the visit id
   * @param username           the username
   * @param itemType           the item type
   * @param itemBean           the item bean
   * @param preAuthBean        the pre auth bean
   * @param itemPrescriptionId the item prescription id
   * @param preauthPrescId     the preauth presc id
   * @param consultationId     the consultation id
   * @throws SQLException the SQL exception
   */
  private void copyPreAuthPrescAttributes(String visitId, String username,
                                          String itemType, BasicDynaBean itemBean,
                                          BasicDynaBean preAuthBean,
                                          int itemPrescriptionId, int preauthPrescId,
                                          Integer consultationId)
      throws SQLException {

    String preauthActType = "";
    String preauthActItemId = "";
    String preauthItemRemarks = "";
    String toothUnvNumber = null;
    String toothFdiNumber = null;
    int docConsType = 0;
    int patientPresId = 0;
    int quantity = 1;
    Timestamp actDueDate = null;

    if (itemType.equals("Inv.")) {
      preauthActType = "DIA";
      preauthItemRemarks = (String) itemBean.get("test_remarks");
      preauthActItemId = (String) itemBean.get("test_id");
      actDueDate = (Timestamp) itemBean.get("activity_due_date");

    } else if (itemType.equals("Service")) {
      preauthActType = "SER";
      preauthItemRemarks = (String) itemBean.get("service_remarks");
      preauthActItemId = (String) itemBean.get("service_id");

      toothUnvNumber = (String) itemBean.get("tooth_unv_number");
      toothFdiNumber = (String) itemBean.get("tooth_fdi_number");
      quantity = (Integer) itemBean.get("qty");
      actDueDate = (Timestamp) itemBean.get("activity_due_date");

    } else if (itemType.equals("Operation")) {
      preauthActType = "OPE";
      preauthItemRemarks = (String) itemBean.get("remarks");
      preauthActItemId = (String) itemBean.get("operation_id");

    } else if (itemType.equals("Doctor")) {
      preauthActType = "DOC";
      preauthItemRemarks = (String) itemBean.get("cons_remarks");
      preauthActItemId = (String) itemBean.get("doctor_id");
      docConsType = preAuthBean.get("doc_cons_type") != null
          ? (Integer) preAuthBean.get("doc_cons_type")
          : docConsType;
    }

    patientPresId = itemPrescriptionId;

    preauthPrescId = preAuthBean.get("preauth_presc_id") != null
        ? (Integer) preAuthBean.get("preauth_presc_id")
        : preauthPrescId;
    int preauthActId = preAuthBean.get("preauth_act_id") != null
        ? (Integer) preAuthBean.get("preauth_act_id")
        : getNextSequence();

    /*
     * Date prescribedDate = DateUtil.getCurrentDate(); prescribedDate =
     * preAuthBean.get("prescribed_date") != null ?
     * (Date)preAuthBean.get("prescribed_date") : prescribedDate;
     */
    Timestamp prescribedDate = DateUtil.getCurrentTimestamp();
    prescribedDate = preAuthBean.get("prescribed_date") != null
        ? (Timestamp) preAuthBean.get("prescribed_date")
        : prescribedDate;

    preAuthBean.set("preauth_act_id", preauthActId);
    preAuthBean.set("consultation_id", consultationId);
    preAuthBean.set("patient_pres_id", patientPresId);
    preAuthBean.set("preauth_act_type", preauthActType);
    preAuthBean.set("preauth_act_item_id", preauthActItemId);
    preAuthBean.set("prescribed_date", prescribedDate);
    preAuthBean.set("act_qty", quantity);

    preAuthBean.set("act_code_type", preAuthBean.get("act_code_type"));
    preAuthBean.set("act_code", preAuthBean.get("act_code"));
    preAuthBean.set("activity_due_date", actDueDate);

    preAuthBean.set("doc_cons_type", docConsType);

    preAuthBean.set("tooth_unv_number", toothUnvNumber);
    preAuthBean.set("tooth_fdi_number", toothFdiNumber);

    preAuthBean.set("mod_time",
        new java.sql.Timestamp(new java.util.Date().getTime()));
    preAuthBean.set("username", username);

    preAuthBean.set("preauth_act_item_remarks", preauthItemRemarks);
    preAuthBean.set("visit_id", visitId);

    String preauthRequired = itemBean.get("preauth_required") != null
        ? (String) itemBean.get("preauth_required")
        : "N";
    preAuthBean.set("preauth_required", preauthRequired);
    preAuthBean.set("preauth_presc_id", preauthPrescId);

    String status = preAuthBean.get("status") != null
        ? (String) preAuthBean.get("status")
        : "A";
    preAuthBean.set("status", status);
    String preauthActStatus = preAuthBean.get("preauth_act_status") != null
        ? (String) preAuthBean.get("preauth_act_status")
        : "O";
    preAuthBean.set("preauth_act_status", preauthActStatus);
    String preauthID = preAuthBean.get("preauth_id") != null
        ? (String) preAuthBean.get("preauth_id")
        : null;
    preAuthBean.set("preauth_id", preauthID);
    int preauthMode = preAuthBean.get("preauth_mode") != null
        ? (Integer) preAuthBean.get("preauth_mode")
        : 0;
    preAuthBean.set("preauth_mode", preauthMode);

    String denialCode = preAuthBean.get("denial_code") != null
        ? (String) preAuthBean.get("denial_code")
        : null;
    preAuthBean.set("denial_code", denialCode);

    String denialRemarks = preAuthBean.get("denial_remarks") != null
        ? (String) preAuthBean.get("denial_remarks")
        : null;
    preAuthBean.set("denial_remarks", denialRemarks);
  }

  /**
   * Insert E auth.
   *
   * @param con                the con
   * @param itemPrescriptionId the item prescription id
   * @param patientId          the patient id
   * @param userName           the user name
   * @param itemType           the item type
   * @param itemBean           the item bean
   * @param preauthPrescId     the preauth presc id
   * @param consultationId     the consultation id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean insertEAuth(Connection con, int itemPrescriptionId,
                             String patientId, String userName, String itemType,
                             BasicDynaBean itemBean, int preauthPrescId, Integer consultationId)
      throws SQLException, IOException {
    BasicDynaBean preAuthBean = getBean();
    copyPreAuthPrescAttributes(patientId, userName, itemType, itemBean,
        preAuthBean, itemPrescriptionId, preauthPrescId,
        consultationId);

    String itemId = (String) preAuthBean.get("preauth_act_item_id");
    BigDecimal itemQty = new BigDecimal(
        (Integer) preAuthBean.get("act_qty"));
    String chargeType = ((Integer) preAuthBean.get("doc_cons_type"))
        .toString();

    String preauthItemType = (String) preAuthBean.get("preauth_act_type");
    ChargeDTO itemCharge = eauthdao.getItemRateCodeCharge(preauthItemType,
        itemId, itemQty, patientId, chargeType);
    preAuthBean.set("act_code", itemCharge.getActRatePlanItemCode());
    preAuthBean.set("act_code_type", itemCharge.getCodeType());

    preAuthBean.set("amount", itemCharge.getAmount());
    preAuthBean.set("rate", itemCharge.getActRate());
    preAuthBean.set("discount", itemCharge.getDiscount());
    preAuthBean.set("claim_net_amount",
        itemCharge.getInsuranceClaimAmount());
    preAuthBean.set("patient_share", (itemCharge.getAmount()
        .subtract(itemCharge.getInsuranceClaimAmount())));

    boolean success = insert(con, preAuthBean);

    if (success && itemType != null && itemType.equals("Service")) {
      boolean isObservationActivity = false;
      String toothNumbers = "";
      String toothNumberingSystem = (String) GenericPreferencesDAO
          .getAllPrefs().get("tooth_numbering_system");
      String toothNumberUNV = (String) itemBean.get("tooth_unv_number");
      String toothNumberFDI = (String) itemBean.get("tooth_fdi_number");
      if (toothNumberingSystem.equals("U") && toothNumberUNV != null
          && toothNumberUNV.length() > 0) {
        isObservationActivity = true;
        toothNumbers = toothNumberUNV;
      } else if (toothNumberingSystem.equals("F")
          && toothNumberUNV != null && toothNumberFDI.length() > 0) {
        isObservationActivity = true;
        toothNumbers = toothNumberFDI;
      }
      if (isObservationActivity) {
        success = insertOrUpdateEAuthActivityObservations(con, itemBean,
            preAuthBean, toothNumbers, "insert");
      }
    }

    return success;
  }

  /**
   * Insert or update E auth activity observations.
   *
   * @param con            the con
   * @param itemBean       the item bean
   * @param preAuthBean    the pre auth bean
   * @param toothNumbers   the tooth numbers
   * @param insertOrUpdate the insert or update
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean insertOrUpdateEAuthActivityObservations(Connection con,
                                                         BasicDynaBean itemBean,
                                                         BasicDynaBean preAuthBean,
                                                         String toothNumbers, String insertOrUpdate)
      throws SQLException, IOException {
    GenericDAO preAuthActivitiesObsevationDAO = new GenericDAO(
        "preauth_activities_observations");
    BasicDynaBean preAuthActivitiesObsevationBean = null;
    boolean success = false;
    if (toothNumbers != null) {
      String[] toothNumberArr = toothNumbers.split(",");
      for (String toothNumber : toothNumberArr) {
        preAuthActivitiesObsevationBean = preAuthActivitiesObsevationDAO
            .getBean();
        preAuthActivitiesObsevationBean.set("preauth_act_id",
            (Integer) preAuthBean.get("preauth_act_id"));
        preAuthActivitiesObsevationBean.set("obs_type",
            ("Universal Dental"));
        preAuthActivitiesObsevationBean.set("code", toothNumber);
        preAuthActivitiesObsevationBean.set("value", "");
        preAuthActivitiesObsevationBean.set("value_type", "");

        if (insertOrUpdate.equals("insert")) {
          success = preAuthActivitiesObsevationDAO.insert(con,
              preAuthActivitiesObsevationBean);
        } else {
          if ((preAuthActivitiesObsevationDAO.findByKey(con,
              "preauth_act_id", (Integer) preAuthBean
                  .get("preauth_act_id"))) != null) {
            ;
          }
          preAuthActivitiesObsevationDAO.delete(con, "preauth_act_id",
              (Integer) preAuthBean.get("preauth_act_id"));

          success = insertOrUpdateEAuthActivityObservations(con,
              itemBean, preAuthBean, toothNumbers, "insert");
        }
      }
    }

    return success;
  }

  /**
   * Update E auth.
   *
   * @param con                the con
   * @param itemPrescriptionId the item prescription id
   * @param patientId          the patient id
   * @param userName           the user name
   * @param itemType           the item type
   * @param itemBean           the item bean
   * @param preauthPrescId     the preauth presc id
   * @return the int
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public int updateEAuth(Connection con, int itemPrescriptionId,
                         String patientId, String userName, String itemType,
                         BasicDynaBean itemBean, int preauthPrescId)
      throws SQLException, IOException {
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("patient_pres_id", itemPrescriptionId);
    keys.put("preauth_presc_id", preauthPrescId);
    BasicDynaBean preAuthBean = findByKey(keys);
    int prescId = (Integer) preAuthBean.get("preauth_presc_id");
    BasicDynaBean preauthBean = eauthdao
        .getPreauthPrescriptionBean(prescId);
    String preAuthStatus = (String) preauthBean.get("preauth_status");

    // If Pre-Auth is Open i.e not Sent then update item details.
    if (preAuthStatus != null && !preAuthStatus.equals("O")) {
      return 1;
    }

    copyPreAuthPrescAttributes(patientId, userName, itemType, itemBean,
        preAuthBean, itemPrescriptionId, preauthPrescId,
        (Integer) preAuthBean.get("consultation_id"));

    String itemId = (String) preAuthBean.get("preauth_act_item_id");
    BigDecimal itemQty = new BigDecimal(
        (Integer) preAuthBean.get("act_qty"));
    String preauthItemType = (String) preAuthBean.get("preauth_act_type");
    String chargeType = ((Integer) preAuthBean.get("doc_cons_type"))
        .toString();

    ChargeDTO itemCharge = eauthdao.getItemRateCodeCharge(preauthItemType,
        itemId, itemQty, patientId, chargeType);
    preAuthBean.set("act_code", itemCharge.getActRatePlanItemCode());
    preAuthBean.set("act_code_type", itemCharge.getCodeType());

    preAuthBean.set("amount", itemCharge.getAmount());
    preAuthBean.set("rate", itemCharge.getActRate());
    preAuthBean.set("discount", itemCharge.getDiscount());
    preAuthBean.set("claim_net_amount",
        itemCharge.getInsuranceClaimAmount());
    boolean success = updateWithName(con, preAuthBean.getMap(),
        "preauth_act_id") > 0;

    if (success && itemType != null && itemType.equals("Service")) {
      boolean isObservationActivity = false;
      String toothNumbers = "";
      String toothNumberingSystem = (String) GenericPreferencesDAO
          .getAllPrefs().get("tooth_numbering_system");
      String toothNumberUNV = (String) itemBean.get("tooth_unv_number");
      String toothNumberFDI = (String) itemBean.get("tooth_fdi_number");
      if (toothNumberingSystem.equals("U") && toothNumberUNV != null
          && toothNumberUNV.length() > 0) {
        isObservationActivity = true;
        toothNumbers = toothNumberUNV;
      } else if (toothNumberingSystem.equals("F")
          && toothNumberUNV != null && toothNumberFDI.length() > 0) {
        isObservationActivity = true;
        toothNumbers = toothNumberFDI;
      }
      if (isObservationActivity) {
        success = insertOrUpdateEAuthActivityObservations(con, itemBean,
            preAuthBean, toothNumbers, "update");
      }
    }

    return success ? 1 : 0;
  }

  /**
   * The Constant UPDATE_PREAUTH_PRESCRIPTION_ACTIVITIES_STATUS.
   */
  public static final String UPDATE_PREAUTH_PRESCRIPTION_ACTIVITIES_STATUS = " UPDATE "
      + "preauth_prescription_activities  SET status = ?, mod_time = ?, username = ? WHERE "
      + "patient_pres_id = ? ";

  /**
   * Delete E auth.
   *
   * @param con                the con
   * @param itemPrescriptionId the item prescription id
   * @param userName           the user name
   * @return the int
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public int deleteEAuth(Connection con, int itemPrescriptionId,
                         String userName) throws SQLException, IOException {
    int rows = 0;
    try (PreparedStatement ps = con.prepareStatement(
        UPDATE_PREAUTH_PRESCRIPTION_ACTIVITIES_STATUS)) {

      ps.setString(1, "X");
      ps.setTimestamp(2, new java.sql.Timestamp(new java.util.Date().getTime()));
      ps.setString(3, userName);
      ps.setInt(4, itemPrescriptionId);
      rows = ps.executeUpdate();
    } finally {
      DataBaseUtil.closeConnections(null, null);
    }
    return rows;
  }

  /**
   * The Constant GET_PRESENTING_COMPLAINT.
   */
  public static final String GET_PRESENTING_COMPLAINT = " SELECT pao.* "
      + " FROM preauth_activities_observations pao WHERE pao.preauth_act_id = ? "
      + " AND pao.code= ? AND pao.obs_type='Text' AND pao.value_type='Text' "
      + " ORDER BY act_obs_id LIMIT 1 ";

  /**
   * Gets the complaint obs value.
   *
   * @param con       the con
   * @param patientId the patient id
   * @return the complaint obs value
   * @throws SQLException the SQL exception
   */
  public String getComplaintObsValue(Connection con, String patientId)
      throws SQLException {
    StringBuilder complaintVal = new StringBuilder();

    String chiefComplaint = new MRDUpdateScreenDAO().getChiefComplaint(con,
        patientId);
    complaintVal.append(
        chiefComplaint != null && !chiefComplaint.trim().equals("")
            ? "Chief Complaint :- " + chiefComplaint
            : "");

    String secComplaints = new MRDUpdateScreenDAO()
        .getSecondaryComplaints(con, patientId);
    complaintVal.append(
        secComplaints != null && !secComplaints.trim().equals("")
            ? " Other Complaints :- " + secComplaints
            : "");

    return complaintVal.toString().trim();
  }

  /**
   * Adds the presenting complaint.
   *
   * @param con          the con
   * @param patientId    the patient id
   * @param preauthActId the preauth act id
   * @param actCode      the act code
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean addPresentingComplaint(Connection con, String patientId,
                                        int preauthActId, String actCode) throws SQLException,
      IOException {

    boolean success = true;

    if (actCode == null || actCode.trim().equals("")) {
      return success;
    }

    String complaintVal = getComplaintObsValue(con, patientId);
    BasicDynaBean existingbean = DataBaseUtil.queryToDynaBean(GET_PRESENTING_COMPLAINT,
        new Object[] {preauthActId, actCode});

    if (existingbean == null && !complaintVal.equals("")) {
      success = insertPresentingComplaint(con, preauthActId, complaintVal,
          actCode);
    }

    return success;
  }

  /**
   * Insert presenting complaint.
   *
   * @param con          the con
   * @param preauthActId the preauth act id
   * @param complaintVal the complaint val
   * @param actCode      the act code
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean insertPresentingComplaint(Connection con, int preauthActId,
                                           String complaintVal, String actCode)
      throws SQLException, IOException {
    BasicDynaBean eauthObsbean = eauthPrescObsDAO.getBean();

    eauthObsbean.set("preauth_act_id", preauthActId);
    eauthObsbean.set("obs_type", "Text");
    eauthObsbean.set("code", actCode);
    eauthObsbean.set("value", complaintVal);
    eauthObsbean.set("value_type", "Text");
    return eauthPrescObsDAO.insert(con, eauthObsbean);
  }

  /**
   * Mark item as sent.
   *
   * @param preauthPrescBean the preauth presc bean
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean markItemAsSent(BasicDynaBean preauthPrescBean) throws SQLException, IOException {
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean flag = true;
    try {
      BasicDynaBean bean = getBean();
      bean.set("preauth_act_status", "S");

      Map keys = new HashMap();
      keys.put("preauth_presc_id",
          preauthPrescBean.get("preauth_presc_id"));
      keys.put("preauth_required", "Y");

      String isResubmit = preauthPrescBean.get("is_resubmit") != null
          ? ((String) preauthPrescBean.get("is_resubmit")).toString()
          : "N";
      String resubmissionType = preauthPrescBean.get("resubmit_type") != null
          ? ((String) preauthPrescBean.get("resubmit_type")).toString() : null;

      if (isResubmit.equals("Y") && (("internal complaint").equalsIgnoreCase(resubmissionType)
          || ("reconciliation").equalsIgnoreCase(resubmissionType))) {
        keys.put("preauth_act_status", "D");
      }

      flag = update(con, bean.getMap(), keys) != 0;

    } finally {
      DataBaseUtil.commitClose(con, flag);
    }
    return flag;
  }
}
