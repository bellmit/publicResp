/**
 *
 */

package com.insta.hms.genericdocuments;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.instaforms.AbstractInstaForms;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericDocumentsFields.
 *
 * @author krishna.t
 */
public class GenericDocumentsFields {

  /** The log. */
  static Logger log = LoggerFactory
      .getLogger(GenericDocumentsFields.class);

  /**
   * copies standard fields: hosp header, footer, curdate to map.
   *
   * @param to the to
   * @param underscore the underscore
   * @throws SQLException the SQL exception
   */
  public static void copyStandardFields(Map<String, String> to, boolean underscore)
      throws SQLException {

    if (to == null) {
      return;
    }
    BasicDynaBean prefs = PrintConfigurationsDAO.getPatientDefaultPrintPrefs();
    convertAndCopy(prefs.getMap(), to, underscore);

    copyCurrentDateAndTime(to, underscore);
  }

  /**
   * copies the patient and/or visit details map.
   *
   * @param to the to
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param underscore the underscore
   * @throws SQLException the SQL exception
   */
  public static void copyPatientDetails(Map<String, String> to, String mrNo, String patientId,
      boolean underscore) throws SQLException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      copyPatientDetails(con, to, mrNo, patientId, underscore);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Copy patient details.
   *
   * @param con the con
   * @param to the to
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param underscore the underscore
   * @throws SQLException the SQL exception
   */
  public static void copyPatientDetails(Connection con, Map<String, String> to, String mrNo,
      String patientId, boolean underscore) throws SQLException {
    if (to == null) {
      return;
    }
    if ((mrNo == null && patientId == null)) {
      return;
    }

    Map patientDetails = null;
    if (patientId != null && !patientId.equals("")) {
      patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap(con, patientId);
    } else {
      patientDetails = PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo);
    }

    convertAndCopy(patientDetails, to, underscore);

    // some derived fields from patient
    if (patientDetails == null) {
      return;
    }

    StringBuilder fullName = new StringBuilder();

    String salutation = (String) patientDetails.get("salutation");
    salutation = salutation == null ? "" : salutation;
    fullName.append(salutation);

    String firstName = (String) patientDetails.get("patient_name");
    firstName = firstName == null ? "" : firstName;
    if (!firstName.equals("")) {
      fullName.append(" ").append(firstName);
    }

    String middleName = (String) patientDetails.get("middle_name");
    middleName = middleName == null ? "" : middleName;
    if (!middleName.equals("")) {
      fullName.append(" ").append(middleName);
    }

    String lastName = (String) patientDetails.get("last_name");
    lastName = lastName == null ? "" : lastName;
    if (!lastName.equals("")) {
      fullName.append(" ").append(lastName);
    }

    if (underscore) {
      to.put("_patient_full_name", fullName.toString());
      to.put("_mr_no_barcode", "*" + patientDetails.get("mr_no") + "*");
    } else {
      to.put("patient_full_name", fullName.toString());
      to.put("mr_no_barcode", "*" + patientDetails.get("mr_no") + "*");
    }

  }

  /**
   * Copy patient details.
   *
   * @param patientDetails the patient details
   * @param to the to
   * @param underscore the underscore
   * @throws SQLException the SQL exception
   */
  public static void copyPatientDetails(Map patientDetails, Map<String, String> to,
      boolean underscore) throws SQLException {
    if (to == null) {
      return;
    }

    convertAndCopy(patientDetails, to, underscore);

    // some derived fields from patient
    if (patientDetails == null) {
      return;
    }

    StringBuilder fullName = new StringBuilder();

    String salutation = (String) patientDetails.get("salutation");
    salutation = salutation == null ? "" : salutation;
    fullName.append(salutation);

    String firstName = (String) patientDetails.get("patient_name");
    firstName = firstName == null ? "" : firstName;
    if (!firstName.equals("")) {
      fullName.append(" ").append(firstName);
    }

    String middleName = (String) patientDetails.get("middle_name");
    middleName = middleName == null ? "" : middleName;
    if (!middleName.equals("")) {
      fullName.append(" ").append(middleName);
    }

    String lastName = (String) patientDetails.get("last_name");
    lastName = lastName == null ? "" : lastName;
    if (!lastName.equals("")) {
      fullName.append(" ").append(lastName);
    }

    if (underscore) {
      to.put("_patient_full_name", fullName.toString());
      to.put("_mr_no_barcode", "*" + patientDetails.get("mr_no") + "*");
    } else {
      to.put("patient_full_name", fullName.toString());
      to.put("mr_no_barcode", "*" + patientDetails.get("mr_no") + "*");
    }

  }

  /**
   * Copy patient diag and cpt code details.
   *
   * @param to the to
   * @param patientId the patient id
   * @param underscore the underscore
   * @throws SQLException the SQL exception
   */
  public static void copyPatientDiagAndCptCodeDetails(Map<String, String> to, String patientId,
      boolean underscore) throws SQLException {
    if (to == null) {
      return;
    }
    if ((patientId == null)) {
      return;
    }

    String patientIcdCodes = null;
    String patientCptCodes = null;
    String patientPrescribedCptCodes = null;
    String patPrescribedMedicines = null;
    String patPrescribedMedAndDrugCode = null;
    String patPrescribedMedDrugCode = null;
    if (patientId != null && !patientId.equals("")) {
      patientIcdCodes = VisitDetailsDAO.getPatientIcdCodes(patientId);
      patientCptCodes = VisitDetailsDAO.getPatientCptCodes(patientId);
      patientPrescribedCptCodes = VisitDetailsDAO.getOpPatientPrescribedCptCodes(patientId);
      BasicDynaBean patPrescribedBean = VisitDetailsDAO.getOpPatientPrescribedMedicines(patientId);
      if (null != patPrescribedBean) {
        patPrescribedMedicines = (String) patPrescribedBean.get("prescribed_medicines");
        patPrescribedMedAndDrugCode = (String)
          patPrescribedBean.get("prescribed_medicines_with_drug_code");
        patPrescribedMedDrugCode = (String) patPrescribedBean.get("drug_codes");
      }
    }

    if (underscore) {
      to.put("_patient_icd_codes", patientIcdCodes);
      to.put("_patient_cpt_codes", patientCptCodes);
      to.put("_patient_prescribed_cpt_codes", patientPrescribedCptCodes);
      to.put("_patient_prescribed_medicines", patPrescribedMedicines);
      to.put("_patient_prescribed_med_and_drug_codes", patPrescribedMedAndDrugCode);
      to.put("_patient_prescribed_med_drug_codes", patPrescribedMedDrugCode);
    } else {
      to.put("patient_icd_codes", patientIcdCodes);
      to.put("patient_cpt_codes", patientCptCodes);
      to.put("patient_prescribed_cpt_codes", patientPrescribedCptCodes);
      to.put("patient_prescribed_medicines", patPrescribedMedicines);
      to.put("patient_prescribed_med_and_drug_codes", patPrescribedMedAndDrugCode);
      to.put("patient_prescribed_med_drug_codes", patPrescribedMedDrugCode);
    }
  }

  /**
   * Copy patient section detail map.
   *
   * @param to the to
   * @param patientId the patient id
   * @param underscore the underscore
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  // Copy all the section data visit wise if only one consultation exist checked eclaim module
  // enable
  public static void copyPatientSectionDetailMap(Map<String, String> to, String patientId,
      boolean underscore) throws SQLException, IOException, ParseException {
    if (to == null) {
      return;
    }
    if ((patientId == null)) {
      return;
    }
    PatientSectionDetailsDAO psdDAO = new PatientSectionDetailsDAO();
    AbstractInstaForms formDAO = AbstractInstaForms.getInstance("Form_CONS");
    String itemType = (String) formDAO.getKeys().get("item_type");
    int consultId;
    Map params = new HashMap();
    BasicDynaBean consultBean = DoctorConsultationDAO.getVisitWiseSingleConsultation(patientId);
    if (consultBean != null) {
      consultId = (Integer) consultBean.get("consultation_id");
      params.put("consultation_id", new String[] {consultId + ""});
      BasicDynaBean compBean = formDAO.getComponents(params);
      List<BasicDynaBean> consValues =
          psdDAO.getAllSectionDetails((String) consultBean.get("mr_no"),
              (String) consultBean.get("patient_id"), consultId, 0,
              (Integer) compBean.get("form_id"), itemType);

      Map<Object, List<List>> map =
          ConversionUtils.listBeanToMapListListBean(consValues, "section_title", "field_id");
      Iterator<Map.Entry<Object, List<List>>> entries = map.entrySet().iterator();
      while (entries.hasNext()) {
        Map.Entry<Object, List<List>> entry = entries.next();
        String section = (String) entry.getKey();
        section = section.replaceAll("[-+.^:,&]", "").replace(" ", "_");
        StringBuilder sectiondetails = new StringBuilder();
        for (List<BasicDynaBean> list : entry.getValue()) {
          BasicDynaBean bean = list.get(0);
          String fieldType;
          String fieldName;
          String optionValue = "";
          String fieldValue = "";
          String optionRemarks = "";
          fieldType = (String) bean.get("field_type");
          fieldName = (String) bean.get("field_name");
          if (bean.get("option_value") != null) {
            optionValue = (String) bean.get("option_value");
          }
          if (bean.get("option_remarks") != null) {
            optionRemarks = (String) bean.get("option_remarks");
          }

          if (fieldType.equals("dropdown")) {
            Integer optionId = (Integer) bean.get("option_id");
            if (optionId == -1) {
              if (!optionRemarks.equals("")) {
                fieldValue = fieldName + ":" + "Others-" + optionRemarks + ",";
              } else {
                fieldValue = fieldName + ":" + "Others" + ",";
              }
            } else {
              if (optionId == 0) {
                fieldValue = fieldName + ":" + "Normal" + ",";
              } else {
                fieldValue = fieldName + ":" + optionValue + ",";
              }
            }
            sectiondetails = sectiondetails.append(fieldValue);
          } else if (fieldType.equals("checkbox")) {
            Integer optionId = (Integer) bean.get("option_id");
            if (optionId == -1) {
              if (!optionRemarks.equals("")) {
                fieldValue = fieldName + ":" + "Others-" + optionRemarks + ",";
              } else {
                fieldValue = fieldName + ":" + "Others" + ",";
              }
            } else if (optionId == 0) {
              if (!optionRemarks.equals("")) {
                fieldValue = fieldName + ":" + "Normal-" + optionRemarks + ",";
              } else {
                fieldValue = fieldName + ":" + "Normal" + ",";
              }
            } else {
              if (!optionValue.equals("") && !optionRemarks.equals("")) {
                fieldValue = fieldName + ":" + optionValue + "-" + optionRemarks + ",";
              } else {
                fieldValue = fieldName + ":" + optionValue + ",";
              }
            }
            sectiondetails = sectiondetails.append(fieldValue);
          } else if (fieldType.equals("text")) {
            fieldValue = fieldName + ":" + optionRemarks + ",";
            sectiondetails = sectiondetails.append(fieldValue);
          } else if (fieldType.equals("wide text")) {
            fieldValue = fieldName + ":" + optionRemarks + ",";
            sectiondetails = sectiondetails.append(fieldValue);
          } else if (fieldType.equals("date")) {
            Date optionValueDate = (Date) bean.get("date_time");
            String date = DateUtil.formatDate(optionValueDate);
            fieldValue = fieldName + ":" + date + ",";
            sectiondetails = sectiondetails.append(fieldValue);
          } else if (fieldType.equals("datetime")) {
            Timestamp optionValueDate = (Timestamp) bean.get("date_time");
            String dateTimeStr = DateUtil.formatTimestamp(optionValueDate);
            fieldValue = fieldName + ":" + dateTimeStr + ",";
            sectiondetails = sectiondetails.append(fieldValue);
          } 
        }
        if (underscore) {
          to.put("_" + section, sectiondetails.toString());
        } else {
          to.put(section, sectiondetails.toString());
        }
      }
    }
  }

  /**
   * copies the current date and time as strings to map.
   *
   * @param to the to
   * @param underscore the underscore
   */
  public static void copyCurrentDateAndTime(Map<String, String> to, boolean underscore) {
    if (to == null) {
      return;
    }
    String date = DataBaseUtil.dateFormatter.format(new Date());
    String time = DataBaseUtil.timeFormatter.format(new Timestamp(new Date().getTime()));
    if (underscore) {
      to.put("_current_date", date);
      to.put("_current_time", time);
    } else {
      to.put("current_date", date);
      to.put("current_time", time);
    }
  }

  /**
   * converts the all values in 'from' Map to strings and copies to 'to' map. 'from' can contain
   * objects, which will be converted to strings based on the type of th object. Also, underscore
   * prefix is handled here.
   *
   * @param from the from
   * @param to the to
   * @param underscore the underscore
   */
  public static void convertAndCopy(Map from, Map<String, String> to, boolean underscore) {
    if (to == null || from == null) {
      return;
    }

    for (Map.Entry e : (Collection<Map.Entry>) from.entrySet()) {
      String key = (String) e.getKey();
      if (e.getValue() != null) {
        Object value = e.getValue();
        String valString = null;
        if (value instanceof java.sql.Date) {
          valString = DataBaseUtil.dateFormatter.format((java.sql.Date) value);
        } else if (value instanceof java.sql.Time) {
          valString = DataBaseUtil.timeFormatter.format((java.sql.Time) value);
        } else if (value instanceof java.sql.Timestamp) {
          valString = DataBaseUtil.timeStampFormatter.format((java.sql.Timestamp) value);
        } else {
          valString = e.getValue().toString();
        }
        if (underscore) {
          to.put("_" + key, valString);
        } else {
          to.put(key, valString);
        }
        log.debug("convertAndCopy: " + key + "=" + valString);
      } else {
        if (underscore) {
          to.put("_" + key, "");
        } else {
          to.put(key, "");
        }
        log.debug("convertAndCopy: " + key + "=" + "");
      }
    }
  }

  /**
   * Get the Temp UserName as strings to map.
   *
   * @param to the to
   * @param tempUserName the tempUserName
   * @param userName the userName
   * @param underscore the underscore
   */
  public static void copyUserName(Map<String, String> to, String tempUserName, String userName,
      boolean underscore) throws SQLException {
    if (to == null || userName == null) {
      return;
    }
    if (underscore) {
      to.put("_username", userName);
      to.put("_temp_username", tempUserName);
    } else {
      to.put("username", userName);
      to.put("temp_username", tempUserName);
    }
  }

}
