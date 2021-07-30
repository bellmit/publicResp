package com.insta.hms.outpatient;

import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.DiagnosisCodeFavourites.MRDCodesMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.RecurrenceDailyMaster.RecurrenceDailyMasterDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.wardactivities.PatientActivitiesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class PrescriptionBO.
 */
public class PrescriptionBO {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(PrescriptionBO.class);

  /** The visit dao. */
  VisitDetailsDAO visitDao = new VisitDetailsDAO();

  /** The presc DAO. */
  PatientPrescriptionDAO prescDAO = new PatientPrescriptionDAO();

  /** The activity dao. */
  PatientActivitiesDAO activityDao = new PatientActivitiesDAO();

  /** The operation pres. */
  GenericDAO operationPres = new GenericDAO("patient_operation_prescriptions");

  /** The medi pres. */
  GenericDAO mediPres = new GenericDAO("patient_medicine_prescriptions");

  /** The inv pres. */
  GenericDAO invPres = new GenericDAO("patient_test_prescriptions");

  /** The ser pres. */
  GenericDAO serPres = new GenericDAO("patient_service_prescriptions");

  /** The doc pres. */
  GenericDAO docPres = new GenericDAO("patient_consultation_prescriptions");

  /** The non billable pres. */
  GenericDAO nonBillablePres = new GenericDAO("patient_other_prescriptions");

  /** The other medi pres. */
  GenericDAO otherMediPres = new GenericDAO("patient_other_medicine_prescriptions");

  /** The generic prefs dao. */
  GenericPreferencesDAO genericPrefsDao = new GenericPreferencesDAO();

  /**
   * Update diangois details.
   *
   * @param con                  the con
   * @param diagnosisId          the diagnosis id
   * @param diagnosisCode        the diagnosis code
   * @param diagnosisDesc        the diagnosis desc
   * @param diagnosisYearOfOnset the diagnosis year of onset
   * @param statusId             the status id
   * @param remarks              the remarks
   * @param doctor               the doctor
   * @param datetime             the datetime
   * @param delete               the delete
   * @param edited               the edited
   * @param patientId            the patient id
   * @param diagType             the diag type
   * @param userName             the user name
   * @param admissionRequestId   the admission request id
   * @param diagFavourite        the diag favourite
   * @param patBean              the pat bean
   * @param admissionReqBean     the admission req bean
   * @param codeType             the code type
   * @return true, if successful
   * @throws SQLException   the SQL exception
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public boolean updateDiangoisDetails(Connection con, String diagnosisId, String diagnosisCode,
      String diagnosisDesc, String diagnosisYearOfOnset, String statusId, String remarks,
      String doctor, String datetime, boolean delete, boolean edited, String patientId,
      String diagType, String userName, Integer admissionRequestId, String diagFavourite,
      BasicDynaBean patBean, BasicDynaBean admissionReqBean, String codeType)
      throws SQLException, IOException, ParseException {
    MRDDiagnosisDAO diagnosisDAO = new MRDDiagnosisDAO();
    Integer diagnosisStatusId = statusId == null || statusId.equals("") ? null
        : Integer.parseInt(statusId);
    Integer yearOfOnSet = diagnosisYearOfOnset == null || diagnosisYearOfOnset.equals("") ? null
        : Integer.parseInt(diagnosisYearOfOnset);

    if (diagnosisId.equals("_")) {
      if (!diagnosisCode.equals("") || !diagnosisDesc.equals("")) {
        BasicDynaBean diagnosisbean = diagnosisDAO.getBean();
        diagnosisbean.set("id", new BigDecimal(diagnosisDAO.getNextSequence()));

        diagnosisbean.set("code_type", codeType);
        diagnosisbean.set("icd_code", diagnosisCode);
        diagnosisbean.set("description", diagnosisDesc);
        diagnosisbean.set("year_of_onset", yearOfOnSet);
        diagnosisbean.set("diag_type", diagType);
        diagnosisbean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
        diagnosisbean.set("username", userName);
        diagnosisbean.set("visit_id", patientId);
        diagnosisbean.set("diagnosis_status_id", diagnosisStatusId);
        diagnosisbean.set("remarks", remarks);
        diagnosisbean.set("doctor_id", doctor);
        diagnosisbean.set("diagnosis_datetime", DateUtil.parseTimestamp(datetime));
        diagnosisbean.set("adm_request_id", admissionRequestId);
        if (!diagnosisDAO.insert(con, diagnosisbean)) {
          return false;
        }
        if (diagFavourite.equals("Y")
            && !insertDiagnosisCodeFavourite(codeType, diagnosisCode, doctor)) {
          return false;
        }
      }

    } else {
      if (delete) {
        if (MRDDiagnosisDAO.updateUserName(con, Integer.parseInt(diagnosisId), userName)
            && !diagnosisDAO.delete(con, "id", new BigDecimal(diagnosisId))) {
          return false;
        }
      } else if (edited) {
        BasicDynaBean diagnosisbean = diagnosisDAO.getBean();
        diagnosisbean.set("diag_type", diagType);
        diagnosisbean.set("icd_code", diagnosisCode);
        diagnosisbean.set("description", diagnosisDesc);
        diagnosisbean.set("year_of_onset", yearOfOnSet);
        diagnosisbean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
        diagnosisbean.set("username", userName);
        diagnosisbean.set("diagnosis_status_id", diagnosisStatusId);
        diagnosisbean.set("remarks", remarks);
        diagnosisbean.set("doctor_id", doctor);
        diagnosisbean.set("diagnosis_datetime", DateUtil.parseTimestamp(datetime));

        HashMap keys = new HashMap();
        keys.put("id", new BigDecimal(diagnosisId));
        if (diagnosisDAO.update(con, diagnosisbean.getMap(), keys) == 0) {
          return false;
        }
        if (diagFavourite.equals("Y")
            && !insertDiagnosisCodeFavourite(codeType, diagnosisCode, doctor)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Save ip prescriptions.
   *
   * @param con                the con
   * @param admissionRequestId the admission request id
   * @param prescriptions      the prescriptions
   * @param params             the params
   * @param patientId          the patient id
   * @param userName           the user name
   * @param hidDeleted         the hid deleted
   * @param isSharedLogIn      the is shared log in
   * @param hidEdited          the hid edited
   * @return the string
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException    Signals that an I/O exception has occurred.
   */
  public String saveIpPrescriptions(Connection con, Integer admissionRequestId,
      String[] prescriptions, Map params, String patientId, String userName, String[] hidDeleted,
      String isSharedLogIn, String[] hidEdited) throws SQLException, ParseException, IOException {

    Map<String, String> presTypes = new HashMap<>();
    presTypes.put("M", "Medicine");
    presTypes.put("I", "Inv.");
    presTypes.put("S", "Service");
    presTypes.put("C", "Doctor");
    presTypes.put("O", "NonBillable");
    presTypes.put("OPE", "Operation");
    String error = null;
    if (prescriptions != null) {
      txn: {
        List errorFields = new ArrayList();
        for (int i = 0; i < prescriptions.length - 1; i++) {
          BasicDynaBean bean = prescDAO.getBean();
          ConversionUtils.copyIndexToDynaBeanPrefixed(params, i, bean, errorFields, "h_");
          bean.set("prescribed_date", ConvertUtils
              .convert(((Object[]) params.get("h_prescription_date"))[i], Timestamp.class));
          bean.set("visit_id", patientId);
          bean.set("username", userName);
          bean.set("adm_request_id", admissionRequestId);
          if (params.get("mr_no") != null) {
            String mrNo = (String) ((Object[]) params.get("mr_no"))[0];
            bean.set("mr_no", mrNo);
          }
          Boolean delete = new Boolean(hidDeleted[i]);
          if (errorFields.isEmpty()) {
            int prescriptionId = Integer
                .parseInt((String) ((Object[]) params.get("h_prescription_id"))[i]);
            bean.set("patient_presc_id", prescriptionId);
            String prescType = (String) bean.get("presc_type");
            if (prescriptionId > 0) {
              if (delete) {
                if (admissionRequestId == null
                    && activityDao.completedActivitiesExists(con, prescriptionId, prescType)) {
                  error = "Prescription cannot be deleted."
                      + " There are completed activities associated with it.";
                  break txn;
                }
                if (!prescDAO.delete(con, "patient_presc_id", prescriptionId)) {
                  error = "Failed to delete the prescriptions..";
                  break txn;
                }
                // delete the incomplete activities if exist.
                if (admissionRequestId == null
                    && activityDao.getPendingActivity(con, prescriptionId, prescType) != null) {
                  if (!activityDao.deleteIncompleteActivity(con, prescriptionId, prescType)) {
                    error = "Failed to delete the incomplete activity for the prescription..";
                    break txn;
                  }
                }
              } else if (new Boolean(hidEdited[i])) {
                if (admissionRequestId == null && bean.get("discontinued").equals("Y")
                    && activityDao.getPendingActivity(con, prescriptionId, prescType) != null) {
                  if (!activityDao.cancelActivity(con, prescriptionId, prescType)) {
                    error = "Failed to cancel the activity..";
                    break txn;
                  }
                }
                BasicDynaBean durBean = prescDAO.findByKey(con, "patient_presc_id", prescriptionId);
                // if any of the column in frequency and duration is modified, then cancel all
                // the incomplete activities, and new activity according to the new user input
                // (based on current frequency and duration values)
                if (admissionRequestId == null && isFreqNDurationModified(durBean, bean)) {
                  if (!activityDao.cancelActivity(con, prescriptionId, prescType)
                      && activityDao.getPendingActivity(con, prescriptionId, prescType) != null) {
                    error = "Failed to cancel the activity..";
                    break txn;
                  }
                  if (admissionRequestId == null
                      && !insertActivity(con, bean, patientId, userName)) {
                    error = "Failed to insert the activity..";
                    break txn;
                  }
                }
                bean.set("presc_type", presTypes.get(prescType));
                if (!(prescDAO.update(con, bean.getMap(), "patient_presc_id", prescriptionId) == 1
                    && savePrescriptionDetails(con, bean, i, params, userName, "update"))) {
                  error = "Failed to update the prescriptions..";
                  break txn;
                }
              }
            } else {
              if (delete) {
                continue;
              } else {
                int itemPrescriptionId = prescDAO.getNextSequence();
                bean.set("patient_presc_id", itemPrescriptionId);
                bean.set("presc_type", presTypes.get(prescType));
                if (!(prescDAO.insert(con, bean)
                    && savePrescriptionDetails(con, bean, i, params, userName, "insert"))) {
                  error = "Failed to insert prescriptions..";
                  break txn;
                }
                if (admissionRequestId == null && !insertActivity(con, bean, patientId, userName)) {
                  error = "Failed to insert the activity";
                  break txn;
                }
              }
            }
          } else {
            error = "Incorrectly formatted values supplied..";
            break txn;
          }
        }
      }
    }
    return error;
  }

  /**
   * Save prescription details.
   *
   * @param con       the con
   * @param mainBean  the main bean
   * @param index     the index
   * @param params    the params
   * @param username  the username
   * @param operation the operation
   * @return the boolean
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  private Boolean savePrescriptionDetails(Connection con, BasicDynaBean mainBean, Integer index,
      Map params, String username, String operation) throws SQLException, IOException {
    String presType = (String) mainBean.get("presc_type");
    Boolean usesStores = genericPrefsDao.getAllPrefs().get("prescription_uses_stores").equals("Y");
    BasicDynaBean bean = null;
    String itemId = (String) ((Object[]) params.get("h_item_id"))[index];
    Timestamp modTime = DateUtil.getCurrentTimestamp();
    String itemName = (String) ((Object[]) params.get("h_item_name"))[index];
    String medDosage = (String) ((Object[]) params.get("h_med_dosage"))[index];
    String medRouteStr = (String) ((Object[]) params.get("h_med_route"))[index];
    Integer medRoute = medRouteStr != null && !"".equals(medRouteStr)
        ? Integer.parseInt(medRouteStr)
        : null;
    String medFormIdStr = (String) ((Object[]) params.get("h_med_form_id"))[index];
    Integer medFormId = medFormIdStr != null && !"".equals(medFormIdStr)
        ? Integer.parseInt(medFormIdStr)
        : null;
    String medStrength = (String) ((Object[]) params.get("h_med_strength"))[index];
    String genericCode = (String) ((Object[]) params.get("h_generic_code"))[index];
    String remarks = (String) ((Object[]) params.get("h_remarks"))[index];
    String medStrengthUnitsStr = (String) ((Object[]) params.get("h_med_strength_units"))[index];
    Integer medStrengthUnits = medStrengthUnitsStr != null && !"".equals(medStrengthUnitsStr)
        ? Integer.parseInt(medStrengthUnitsStr)
        : null;
    String adminStrength = (String) ((Object[]) params.get("h_admin_strength"))[index];
    Object prescriptionId = mainBean.get("patient_presc_id");
    if ("Medicine".equals(presType)) {
      if (usesStores) {
        bean = mediPres.getBean();
        bean.set("medicine_remarks", remarks);
        bean.set("mod_time", modTime);
        bean.set("medicine_id", Integer.parseInt(itemId));
        bean.set("route_of_admin", medRoute);
        bean.set("strength", medDosage);
        bean.set("generic_code", genericCode);
        bean.set("item_form_id", medFormId);
        bean.set("item_strength", medStrength);
        bean.set("item_strength_units", medStrengthUnits);
        bean.set("admin_strength", adminStrength);
        bean.set("username", username);
        if (operation.equals("insert")) {
          bean.set("op_medicine_pres_id", prescriptionId);
          return mediPres.insert(con, bean);
        } else {
          return mediPres.update(con, bean.getMap(), "op_medicine_pres_id", prescriptionId) == 1;
        }
      } else {
        bean = otherMediPres.getBean();
        bean.set("medicine_remarks", remarks);
        bean.set("mod_time", modTime);
        bean.set("medicine_name", itemName);
        bean.set("route_of_admin", medRoute);
        bean.set("strength", medDosage);
        bean.set("generic_code", genericCode);
        bean.set("item_form_id", medFormId);
        bean.set("item_strength", medStrength);
        bean.set("item_strength_units", medStrengthUnits);
        bean.set("admin_strength", adminStrength);
        bean.set("username", username);
        if (operation.equals("insert")) {
          bean.set("prescription_id", prescriptionId);
          return otherMediPres.insert(con, bean);
        } else {
          return otherMediPres.update(con, bean.getMap(), "prescription_id", prescriptionId) == 1;
        }
      }
    } else if ("Inv.".equals(presType)) {
      bean = invPres.getBean();
      bean.set("test_remarks", remarks);
      bean.set("mod_time", modTime);
      bean.set("test_id", itemId);
      bean.set("username", username);
      if (operation.equals("insert")) {
        bean.set("op_test_pres_id", prescriptionId);
        return invPres.insert(con, bean);
      } else {
        return invPres.update(con, bean.getMap(), "op_test_pres_id", prescriptionId) == 1;
      }
    } else if ("Service".equals(presType)) {
      bean = serPres.getBean();
      bean.set("service_remarks", remarks);
      bean.set("mod_time", modTime);
      bean.set("service_id", itemId);
      bean.set("username", username);
      bean.set("op_service_pres_id", prescriptionId);
      if (operation.equals("insert")) {
        return serPres.insert(con, bean);
      } else {
        return serPres.update(con, bean.getMap(), "op_service_pres_id", prescriptionId) == 1;
      }
    } else if ("Doctor".equals(presType)) {
      bean = docPres.getBean();
      bean.set("cons_remarks", remarks);
      bean.set("username", username);
      bean.set("mod_time", modTime);
      bean.set("doctor_id", itemId);
      bean.set("prescription_id", prescriptionId);
      if (operation.equals("insert")) {
        return docPres.insert(con, bean);
      } else {
        return docPres.update(con, bean.getMap(), "prescription_id", prescriptionId) == 1;
      }
    } else if ("NonBillable".equals(presType)) {
      bean = nonBillablePres.getBean();
      bean.set("item_name", itemName);
      bean.set("item_remarks", remarks);
      bean.set("mod_time", modTime);
      bean.set("strength", medDosage);
      bean.set("item_form_id", medFormId);
      bean.set("item_strength", medStrength);
      bean.set("item_strength_units", medStrengthUnits);
      bean.set("username", username);
      bean.set("prescription_id", prescriptionId);
      bean.set("admin_strength", adminStrength);
      if (operation.equals("insert")) {
        return nonBillablePres.insert(con, bean);
      } else {
        return nonBillablePres.update(con, bean.getMap(), "prescription_id", prescriptionId) == 1;
      }
    } else if ("Operation".equals(presType)) {
      bean = operationPres.getBean();
      bean.set("operation_id", itemId);
      bean.set("mod_time", modTime);
      bean.set("remarks", remarks);
      bean.set("preauth_required", "N");
      bean.set("username", username);
      bean.set("prescription_id", prescriptionId);
      if (operation.equals("insert")) {
        return operationPres.insert(con, bean);
      } else {
        return operationPres.update(con, bean.getMap(), "prescription_id", prescriptionId) == 1;
      }
    }
    return false;
  }

  /**
   * Checks if is freq N duration modified.
   *
   * @param durBean    the dur bean
   * @param updateBean the update bean
   * @return true, if is freq N duration modified
   */
  private boolean isFreqNDurationModified(BasicDynaBean durBean, BasicDynaBean updateBean) {
    if (isNotEqual(updateBean.get("freq_type"), durBean.get("freq_type"))) {
      return true;
    }
    if (isNotEqual(updateBean.get("recurrence_daily_id"), durBean.get("recurrence_daily_id"))) {
      return true;
    }
    if (isNotEqual(updateBean.get("repeat_interval"), durBean.get("repeat_interval"))) {
      return true;
    }
    if (updateBean.get("freq_type").equals("R") && isNotEqual(
        updateBean.get("repeat_interval_units"), durBean.get("repeat_interval_units"))) {
      return true;
    }
    if (isNotEqual(updateBean.get("start_datetime"), durBean.get("start_datetime"))) {
      return true;
    }
    if (isNotEqual(updateBean.get("end_datetime"), durBean.get("end_datetime"))) {
      return true;
    }
    if (isNotEqual(updateBean.get("no_of_occurrences"), durBean.get("no_of_occurrences"))) {
      return true;
    }
    if (isNotEqual(updateBean.get("end_on_discontinue"), durBean.get("end_on_discontinue"))) {
      return true;
    }

    return false;
  }

  /**
   * Checks if is not equal.
   *
   * @param val1 the val 1
   * @param val2 the val 2
   * @return true, if is not equal
   */
  public boolean isNotEqual(Object val1, Object val2) {
    if (val1 == null && val2 != null) {
      return true;
    } else if (val1 != null && val2 == null) {
      return true;
    } else if (val1 == null && val2 == null) {
      return false;
    } else {
      if (val1 instanceof String) {
        return !val1.equals(val2);
      } else if (val1 instanceof Integer) {
        return ((Integer) val1).intValue() != ((Integer) val2).intValue();
      } else if (val1 instanceof java.sql.Timestamp) {
        return !val1.equals(val2);
      } else if (val1 instanceof BigDecimal) {
        return !val1.equals(val2);
      } else if (val1 instanceof java.sql.Date) {
        return !val1.equals(val2);
      }
      return false;
    }
  }

  /**
   * Insert activity.
   *
   * @param con       the con
   * @param bean      the bean
   * @param patientId the patient id
   * @param userName  the user name
   * @return true, if successful
   * @throws ParseException the parse exception
   * @throws SQLException   the SQL exception
   * @throws IOException    Signals that an I/O exception has occurred.
   */
  public boolean insertActivity(Connection con, BasicDynaBean bean, String patientId,
                                String userName) throws ParseException, SQLException, IOException {
    String prescType = (String) bean.get("presc_type");
    java.sql.Timestamp startDateTime = (java.sql.Timestamp) bean.get("start_datetime");
    java.sql.Timestamp duedate = null;
    String freqType = (String) bean.get("freq_type");
    if (prescType.equals("C") || freqType.equals("R")) {
      duedate = startDateTime;
    } else {
      duedate = new RecurrenceDailyMasterDAO().getNextDueDateTime((String) bean.get("freq_type"),
          (Integer) bean.get("recurrence_daily_id"), (Integer) bean.get("repeat_interval"),
          (String) bean.get("repeat_interval_units"), startDateTime);
    }
    BasicDynaBean activityBean = activityDao.getBean();
    activityBean.set("activity_id", activityDao.getNextSequence());
    activityBean.set("patient_id", patientId);
    activityBean.set("activity_type", "P");
    activityBean.set("prescription_type", bean.get("presc_type"));
    activityBean.set("prescription_id", bean.get("patient_presc_id"));
    activityBean.set("presc_doctor_id", bean.get("doctor_id"));
    activityBean.set("due_date", duedate);
    activityBean.set("activity_status", bean.get("discontinued").equals("Y") ? "X" : "P");
    activityBean.set("added_by", userName);
    activityBean.set("username", userName);
    activityBean.set("activity_num", 1);
    return activityDao.insert(con, activityBean);
  }

  /**
   * Insert diagnosis code favourite.
   *
   * @param defaultCodeType the default code type
   * @param diagnosisCode   the diagnosis code
   * @param doctorId        the doctor id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean insertDiagnosisCodeFavourite(String defaultCodeType, String diagnosisCode,
      String doctorId) throws SQLException {

    MRDCodesMasterDAO mrdCodesDAO = new MRDCodesMasterDAO();
    List<BasicDynaBean> icdFavList = new ArrayList<>();
    BasicDynaBean bean = mrdCodesDAO.getBean();

    bean.set("code_type", defaultCodeType);
    bean.set("code", diagnosisCode);
    bean.set("doctor_id", doctorId);
    if (!MRDCodesMasterDAO.existsCode(defaultCodeType, diagnosisCode, doctorId)) {
      icdFavList.add(bean);
      if (!new MRDCodesMasterDAO().insertICDFavourites(icdFavList)) {
        return false;
      }
    }
    return true;
  }
}
