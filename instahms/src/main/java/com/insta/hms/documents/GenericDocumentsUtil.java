package com.insta.hms.documents;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.genericdocuments.GenericDocumentsFields;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericDocumentsUtil.
 */
@Component
public class GenericDocumentsUtil {

  /** The print config repo. */
  @LazyAutowired
  private PrintConfigurationRepository printConfigRepo;

  /** The reg service. */
  @LazyAutowired
  private RegistrationService regService;

  /** The patient details service. */
  @LazyAutowired
  private PatientDetailsService patientDetailsService;

  /** The patient registration repo. */
  @LazyAutowired
  private PatientRegistrationRepository patientRegistrationRepo;

  /** The log. */
  static Logger log = LoggerFactory.getLogger(GenericDocumentsFields.class);

  /**
   * copies standard fields: hosp header, footer, curdate to map.
   *
   * @param to the to
   * @param underscore the underscore
   */
  public void copyStandardFields(Map<String, String> to, boolean underscore) {

    if (to == null) {
      return;
    }
    BasicDynaBean prefs = PrintConfigurationRepository.getPatientDefaultPrintPrefs();
    convertAndCopy(prefs.getMap(), to, underscore);

    copyCurrentDateAndTime(to, underscore);
  }

  /**
   * Copy patient details.
   *
   * @param to the to
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param underscore the underscore
   */
  @SuppressWarnings("rawtypes")
  public void copyPatientDetails(Map<String, String> to, String mrNo, String patientId,
      boolean underscore) {
    if (to == null) {
      return;
    }
    if ((mrNo == null && patientId == null)) {
      return;
    }

    Map patientDetails = null;
    BasicDynaBean patientDetailsBean = null;
    if (patientId != null && !patientId.equals("")) {
      patientDetailsBean = regService.getPatientVisitDetailsBean(patientId);
    } else {
      patientDetailsBean = patientDetailsService.getPatientDetailsDisplayBean(mrNo);
    }

    // some derived fields from patient
    if (patientDetailsBean == null) {
      return;
    } else {
      patientDetails = patientDetailsBean.getMap();
    }

    convertAndCopy(patientDetails, to, underscore);

    if (underscore) {
      to.put("_patient_full_name", patientDetails.get("full_name").toString());
      to.put("_mr_no_barcode", "*" + patientDetails.get("mr_no") + "*");
    } else {
      to.put("patient_full_name", patientDetails.get("full_name").toString());
      to.put("mr_no_barcode", "*" + patientDetails.get("mr_no") + "*");
    }

  }

  /**
   * copies the current date and time as strings to map.
   *
   * @param to the to
   * @param underscore the underscore
   */
  public void copyCurrentDateAndTime(Map<String, String> to, boolean underscore) {
    if (to == null) {
      return;
    }
    String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
    String time = new SimpleDateFormat("HH:mm").format(new Timestamp(new Date().getTime()));
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
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void convertAndCopy(Map from, Map<String, String> to, boolean underscore) {
    if (to == null || from == null) {
      return;
    }

    for (Map.Entry e : (Collection<Map.Entry>) from.entrySet()) {
      String key = (String) e.getKey();
      if (e.getValue() != null) {
        Object value = e.getValue();
        String valString = null;
        if (value instanceof java.sql.Date) {
          valString = new SimpleDateFormat("dd-MM-yyyy").format((java.sql.Date) value);
        } else if (value instanceof java.sql.Time) {
          valString = new SimpleDateFormat("HH:mm").format((java.sql.Time) value);
        } else if (value instanceof java.sql.Timestamp) {
          valString = new SimpleDateFormat("dd-MM-yyyy HH:mm").format((java.sql.Timestamp) value);
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
   * Copy patient diag and cpt code details.
   *
   * @param to the to
   * @param patientId the patient id
   * @param underscore the underscore
   */
  public void copyPatientDiagAndCptCodeDetails(Map<String, String> to, String patientId,
      boolean underscore) {
    if (to == null) {
      return;
    }
    if ((patientId == null)) {
      return;
    }

    String patientIcdCodes = null;
    String patientCptCodes = null;
    String patientPrescribedCptCodes = null;
    if (patientId != null && !patientId.equals("")) {
      patientIcdCodes = patientRegistrationRepo.getPatientIcdCodes(patientId);
      patientCptCodes = patientRegistrationRepo.getPatientCptCodes(patientId);
      patientPrescribedCptCodes = patientRegistrationRepo.getOpPatientPrescribedCptCodes(patientId);
    }

    if (underscore) {
      to.put("_patient_icd_codes", patientIcdCodes);
      to.put("_patient_cpt_codes", patientCptCodes);
      to.put("_patient_prescribed_cpt_codes", patientPrescribedCptCodes);
    } else {
      to.put("patient_icd_codes", patientIcdCodes);
      to.put("patient_cpt_codes", patientCptCodes);
      to.put("patient_prescribed_cpt_codes", patientPrescribedCptCodes);
    }
  }

}
