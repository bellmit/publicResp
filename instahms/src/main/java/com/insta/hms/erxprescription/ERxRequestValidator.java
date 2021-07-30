package com.insta.hms.erxprescription;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.eservice.EValidator;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDTO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.HttpSession;

public class ERxRequestValidator extends EValidator<ERxRequest> {
  static Logger log = LoggerFactory.getLogger(ERxRequestValidator.class);

  ERxPrescriptionDAO erxdao = new ERxPrescriptionDAO();

  private static final String ERX_CONSULTATION_ERROR = "ERx Consultation";
  private static final String EMPTY_ERX_CONSULTATION_MSG = "Invalid or No Consultation Id";

  private static final String ERX_PBM_PRESCRIPTION_ID_ERROR = "ERx PBM Presc";
  private static final String EMPTY_ERX_PBM_PRESCRIPTION_ID_MSG = "Invalid or No PBM Presc. Id for Consultation";

  private static final String ERX_HEALTH_AUTHORITY_ERROR = "ERx Health Authority";
  private static final String EMPTY_ERX_HEALTH_AUTHORITY_MSG = "Invalid or No ERx Health Authority for Center";

  private static final String SENDER_ID_ERROR = "Sender ID";
  private static final String EMPTY_SENDER_ID_MSG = "Sender ID is null. Service Reg. No. is required";

  // private static final String SPONSOR_ID_ERROR = "Patient Sponsor";
  // private static final String EMPTY_SPONSOR_ID_MSG = "Patient has no Sponsor";

  // private static final String RECEIVER_ID_ERROR = "Receiver ID";
  // private static final String EMPTY_RECEIVER_ID_MSG = "Receiver ID is null. TPA code is
  // Required";

  private static final String INSURANCE_COMPANY_ID_ERROR = "Patient Insurance Company";
  private static final String EMPTY_INSURANCE_COMPANY_ID_MSG = "Patient has no Insurance Company";

  private static final String RECEIVER_ID_ERROR = "Receiver ID";
  private static final String EMPTY_RECEIVER_ID_MSG = "Receiver ID is null. Company code is Required";

  // private static final String ERX_PRESC_ID_ERROR = "ERx Prescription ID";
  // private static final String EMPTY_ERX_PRESC_ID_MSG = "ERx Prescription ID is null";

  private static final String ERX_CLINICIAN_ID_ERROR = "ERx Clinician ID";
  private static final String EMPTY_ERX_CLINICIAN_ID_MSG = "ERx Clinician ID is null. Doctor license no. is required";

  private static final String MEMBER_ID_ERROR = "Member ID";
  private static final String EMPTY_MEMBER_ID_MSG = "Member ID should not be null";

  private static final String EMIRATES_ID_ERROR = "Emirates ID";
  // private static final String EMPTY_EMIRATES_ID_MSG = "Emirates ID should not be null";

  private static final String PATIENT_WEIGHT_ERROR = "Patient Weight";
  private static final String EMPTY_PATIENT_WEIGHT_MSG = "Invalid/No Patient Weight";

  private static final String ENCOUNTER_TYPE_ERROR = "Encounter Type";
  private static final String EMPTY_ENCOUNTER_TYPE_MSG = "Encounter Type should not be null";

  private static final String DIAGNOSIS_TYPE_ERROR = "Diagnosis Type";
  private static final String EMPTY_DIAGNOSIS_TYPE_MSG = "Diagnosis Type should not be null";

  private static final String DIAGNOSIS_CODE_ERROR = "Diagnosis Code";
  private static final String EMPTY_DIAGNOSIS_CODE_MSG = "Diagnosis Code should not be null";

  private static final String ACTIVITY_TYPE_ERROR = "Activity Type";
  private static final String EMPTY_ACTIVITY_TYPE_MSG = "Activity/Drug Type should not be null w.r.t Center Health Authority";

  private static final String ACTIVITY_CODE_ERROR = "Activity Code";
  private static final String EMPTY_ACTIVITY_CODE_MSG = "Drug/Generic Code should not be null w.r.t Center Health Authority";

  private static final String ROUTE_CODE_ERROR = "Route Code";
  private static final String EMPTY_ROUTE_CODE_MSG = "Route Code should not be null/empty";

  private static final String FREQUENCY_TYPE_ERROR = "Frequency Type";
  private static final String EMPTY_FREQUENCY_TYPE_MSG = "Frequency Type should not be null/empty";

  private static final String FREQUENCY_VALUE_ERROR = "Frequency Value";
  private static final String EMPTY_FREQUENCY_VALUE_MSG = "Frequency Value should not be null/empty";

  /**
   * Method to validate the request / response object
   */

  public boolean validate(ERxRequest data) {

    HttpSession session = RequestContext.getSession();
    java.util.HashMap urlRightsMap = (java.util.HashMap) session.getAttribute("urlRightsMap");
    java.util.HashMap actionUrlMap = (java.util.HashMap) session.getServletContext()
        .getAttribute("actionUrlMap");

    Integer centerId = RequestContext.getCenterId();
    String path = RequestContext.getHttpRequest().getContextPath();
    path = path + "/";

    ERxPrescription erxp = (ERxPrescription) data;
    ERxPrescriptionHeader header = (ERxPrescriptionHeader) erxp.getHeader();
    EPrescription prescription = erxp.getPrescription();

    Object consIdStr = erxp.getRequestId();

    try {

      BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
      RegistrationPreferencesDTO regPref = RegistrationPreferencesDAO.getRegistrationPreferences();
      String govenmtIdLabel = regPref.getGovernment_identifier_label() != null
          ? regPref.getGovernment_identifier_label()
          : "Emirates ID";
      String govenmtIdTypeLabel = regPref.getGovernment_identifier_label() != null
          ? regPref.getGovernment_identifier_type_label()
          : "Emirates ID Type";

      if (!validateNotEmpty(consIdStr, ERX_CONSULTATION_ERROR, EMPTY_ERX_CONSULTATION_MSG,
          new Object[] { consIdStr }, false))
        return false;

      //int consId = Integer.parseInt(consIdStr);
      int pbmPrescId = erxdao.getErxConsPBMId(consIdStr);

      if (!validateNotEmpty(consIdStr, ERX_PBM_PRESCRIPTION_ID_ERROR,
          EMPTY_ERX_PBM_PRESCRIPTION_ID_MSG, new Object[] { pbmPrescId }, false))
        return false;

      BasicDynaBean erxConsBean = erxdao.getConsErxDetails(pbmPrescId);

      Integer erxCenterId = centerId;
      if (null != erxConsBean.get("erx_center_id"))
        erxCenterId = (Integer) erxConsBean.get("erx_center_id");

      Boolean isSelfpaySponsor = (Boolean) erxConsBean.get("is_selfpay_sponsor");
      String mrNo = (String) erxConsBean.get("mr_no");
      String patientId = (String) erxConsBean.get("patient_id");
      String doctorId = (String) erxConsBean.get("doctor_name");
      String tpaId = (String) erxConsBean.get("primary_sponsor_id");
      String insuCompId = (String) erxConsBean.get("primary_insurance_co");
      String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(erxCenterId);

      Boolean isSelfpay = false;
      if (tpaId == null || isSelfpaySponsor) {
        // its a selfpay patient
        isSelfpay = true;
      }
      /*
       * if (!validateNotEmpty(tpaId, SPONSOR_ID_ERROR, EMPTY_SPONSOR_ID_MSG, new
       * Object[]{patientId,
       * path+((String)actionUrlMap.get("change_visit_tpa"))+"?_method=changeTpa&visitId="+
       * patientId} ,(urlRightsMap.get("change_visit_tpa").equals("A")))) return false;
       */

      if (!isSelfpay && !validateNotEmpty(insuCompId, INSURANCE_COMPANY_ID_ERROR,
          EMPTY_INSURANCE_COMPANY_ID_MSG,
          new Object[] { patientId, path + ((String) actionUrlMap.get("change_visit_tpa"))
              + "?_method=changeTpa&visitId=" + patientId },
          (urlRightsMap.get("change_visit_tpa").equals("A"))))
        return false;

      BasicDynaBean erxheaderBean = erxdao.getERxHeaderFields(pbmPrescId, doctorId, tpaId,
          insuCompId, healthAuthority);

      String centerName = (String) (new CenterMasterDAO().findByKey("center_id", erxCenterId))
          .get("center_name");
      if (healthAuthority == null || healthAuthority.trim().equals("")
          || !healthAuthority.equals("DHA")) {
        if (!validateNotEmpty("", ERX_HEALTH_AUTHORITY_ERROR, EMPTY_ERX_HEALTH_AUTHORITY_MSG,
            new Object[] { centerName, path + ((String) actionUrlMap.get("mas_centers"))
                + "?_method=show&center_id=" + erxCenterId },
            (urlRightsMap.get("mas_centers").equals("A"))))
          return false;
      }

      if (erxCenterId != null) {
        validateNotEmpty(header.getSenderID(), SENDER_ID_ERROR, EMPTY_SENDER_ID_MSG,
            new Object[] { (String) erxheaderBean.get("center_name"),
                path + ((String) actionUrlMap.get("mas_centers")) + "?_method=show&center_id="
                    + erxCenterId },
            (urlRightsMap.get("mas_centers").equals("A")));
      } else {
        validateNotEmpty(header.getSenderID(), SENDER_ID_ERROR, EMPTY_SENDER_ID_MSG,
            new Object[] { "Generic Preferences",
                path + ((String) actionUrlMap.get("mas_gen_pref"))
                    + "?method=getGenericPreferenceScreen" },
            (urlRightsMap.get("mas_gen_pref").equals("A")));
      }

      String insuCompName = DataBaseUtil.getStringValueFromDb(
          "SELECT insurance_co_name FROM insurance_company_master WHERE insurance_co_id = ? ",
          insuCompId);

      EPrescriptionPatient patient = prescription.getPatient();
      // skip these validations for selfpay patients
      if (!isSelfpay) {
        validateNotEmpty(header.getReceiverID(), RECEIVER_ID_ERROR, EMPTY_RECEIVER_ID_MSG,
            new Object[] { insuCompName,
                path + ((String) actionUrlMap.get("mas_insurance_comp"))
                    + "?_method=show&insurance_co_id=" + insuCompId },
            (urlRightsMap.get("mas_insurance_comp").equals("A")));

        validateNotEmpty(patient.getMemberId(), MEMBER_ID_ERROR, EMPTY_MEMBER_ID_MSG,
            new Object[] { patientId,
                path + ((String) actionUrlMap.get("change_visit_tpa"))
                    + "?_method=changeTpa&visitId=" + patientId },
            (urlRightsMap.get("change_visit_tpa").equals("A")));
      }
      validateNotEmpty(prescription.getClinician(), ERX_CLINICIAN_ID_ERROR,
          EMPTY_ERX_CLINICIAN_ID_MSG,
          new Object[] { (String) erxheaderBean.get("doctor_name"),
              path + ((String) actionUrlMap.get("mas_doctors_detail"))
                  + "?_method=getDoctorDetailsScreen&mode=update&doctor_id=" + doctorId },
          (urlRightsMap.get("mas_doctors_detail").equals("A")));

      validateNotEmpty(patient.getEmiratesIDNumber(), EMIRATES_ID_ERROR,
          govenmtIdLabel + " (or) " + govenmtIdTypeLabel + " is required",
          new Object[] { mrNo,
              path + ((String) actionUrlMap.get("reg_general"))
                  + "?_method=show&regType=regd&mr_no=" + mrNo + "&mrno=" + mrNo },
          (urlRightsMap.get("reg_general").equals("A")));

      validateNotEmpty(patient.getWeight() == 0 ? null : patient.getWeight(), PATIENT_WEIGHT_ERROR,
          EMPTY_PATIENT_WEIGHT_MSG,
          new Object[] { "Consultation", path + ((String) actionUrlMap.get("op_prescribe"))
              + "?_method=list&consultation_id=" + (int)consIdStr },
          (urlRightsMap.get("op_prescribe").equals("A")));

      Object[] codifyParamsArr = new Object[] { patientId,
          path + ((String) actionUrlMap.get("update_mrd"))
              + "?_method=getMRDUpdateScreen&patient_id=" + patientId };

      boolean hasCodifyRights = (urlRightsMap.get("update_mrd").equals("A"));

      int encType = prescription.getEncounter().getType();
      // Encounter not mandatory
      validateNotEmpty(encType == 0 ? null : encType, ENCOUNTER_TYPE_ERROR,
          EMPTY_ENCOUNTER_TYPE_MSG, codifyParamsArr, hasCodifyRights);

      List<EPrescriptionDiagnosis> ePrescDiagnosis = prescription.getDiagnosis();
      for (EPrescriptionDiagnosis diag : ePrescDiagnosis) {

        validateNotEmpty(diag.getCode(), DIAGNOSIS_CODE_ERROR, EMPTY_DIAGNOSIS_CODE_MSG,
            codifyParamsArr, hasCodifyRights);

        validateNotEmpty(diag.getType(), DIAGNOSIS_TYPE_ERROR, EMPTY_DIAGNOSIS_TYPE_MSG,
            codifyParamsArr, hasCodifyRights);

      }

      String prescription_uses_stores = (String) genericPrefs.get("prescription_uses_stores");
      String prescByGenerics = HealthAuthorityPreferencesDAO
          .getHealthAuthorityPreferences(healthAuthority).getPrescriptions_by_generics();
      boolean isGenerics = (prescription_uses_stores.equals("Y") && prescByGenerics.equals("Y"));

      List<EPrescriptionActivity> activities = prescription.getActivities();

      if (isGenerics) {
        for (EPrescriptionActivity activity : activities) {
          validateNotEmpty(activity.getActivityCode(), ACTIVITY_CODE_ERROR, EMPTY_ACTIVITY_CODE_MSG,
              new Object[] { activity.getActivityName() }, false);

          validateNotEmpty(activity.getActivityType(), ACTIVITY_TYPE_ERROR, EMPTY_ACTIVITY_TYPE_MSG,
              new Object[] { activity.getActivityName() }, false);

          validateNotEmpty(activity.getRoutOfAdmin() == null ? null : activity.getRoutOfAdmin(),
              ROUTE_CODE_ERROR, EMPTY_ROUTE_CODE_MSG,
              new Object[] { "Medicine Route: " + activity.getRouteOfAdminName(),
                  path + ((String) actionUrlMap.get("medicine_route")) + "?_method=show&route_id="
                      + activity.getRouteOfAdminId() },
              (urlRightsMap.get("medicine_route").equals("A")));

          EPrescriptionActivity.Frequency actFreq = activity.getFrequency();
          if (actFreq != null && (actFreq.getValueType() != null)
              && !(actFreq.getValueType().equals(""))) {

            validateNotEmpty(actFreq.getType(), FREQUENCY_TYPE_ERROR, EMPTY_FREQUENCY_TYPE_MSG,
                new Object[] { "Frequency: " + actFreq.getValueType(),
                    path + ((String) actionUrlMap.get("mas_medicine_dosage"))
                        + "?_method=show&med_dosage_name=" + actFreq.getValueType() },
                (urlRightsMap.get("mas_medicine_dosage").equals("A")));

            validateNotEmpty(
                actFreq.getValue().compareTo(BigDecimal.ZERO) == 0 ? null : actFreq.getValue(),
                FREQUENCY_VALUE_ERROR, EMPTY_FREQUENCY_VALUE_MSG,
                new Object[] { "Frequency: " + actFreq.getValueType(),
                    path + ((String) actionUrlMap.get("mas_medicine_dosage"))
                        + "?_method=show&med_dosage_name=" + actFreq.getValueType() },
                (urlRightsMap.get("mas_medicine_dosage").equals("A")));
          }
        }
      } else {
        for (EPrescriptionActivity activity : activities) {
          validateNotEmpty(activity.getActivityCode(), ACTIVITY_CODE_ERROR, EMPTY_ACTIVITY_CODE_MSG,
              new Object[] { activity.getActivityName(),
                  path + ((String) actionUrlMap.get("mas_medicines"))
                      + "?_method=editItemCode&medicine_id=" + activity.getMedicineID() },
              (urlRightsMap.get("mas_medicines").equals("A")));

          validateNotEmpty(activity.getActivityType(), ACTIVITY_TYPE_ERROR, EMPTY_ACTIVITY_TYPE_MSG,
              new Object[] { activity.getActivityName(),
                  path + ((String) actionUrlMap.get("mas_medicines"))
                      + "?_method=editItemCode&medicine_id=" + activity.getMedicineID() },
              (urlRightsMap.get("mas_medicines").equals("A")));

          validateNotEmpty(activity.getRoutOfAdmin() == null ? null : activity.getRoutOfAdmin(),
              ROUTE_CODE_ERROR, EMPTY_ROUTE_CODE_MSG,
              new Object[] { "Medicine Route: " + activity.getRouteOfAdminName(),
                  path + ((String) actionUrlMap.get("medicine_route")) + "?_method=show&route_id="
                      + activity.getRouteOfAdminId() },
              (urlRightsMap.get("medicine_route").equals("A")));

          EPrescriptionActivity.Frequency actFreq = activity.getFrequency();
          if (actFreq != null && (actFreq.getValueType() != null)
              && !(actFreq.getValueType().equals(""))) {

            validateNotEmpty(actFreq.getType(), FREQUENCY_TYPE_ERROR, EMPTY_FREQUENCY_TYPE_MSG,
                new Object[] { "Frequency: " + actFreq.getValueType(),
                    path + ((String) actionUrlMap.get("mas_medicine_dosage"))
                        + "?_method=show&med_dosage_name=" + actFreq.getValueType() },
                (urlRightsMap.get("mas_medicine_dosage").equals("A")));

            validateNotEmpty(
                actFreq.getValue().compareTo(BigDecimal.ZERO) == 0 ? null : actFreq.getValue(),
                FREQUENCY_VALUE_ERROR, EMPTY_FREQUENCY_VALUE_MSG,
                new Object[] { "Frequency: " + actFreq.getValueType(),
                    path + ((String) actionUrlMap.get("mas_medicine_dosage"))
                        + "?_method=show&med_dosage_name=" + actFreq.getValueType() },
                (urlRightsMap.get("mas_medicine_dosage").equals("A")));
          }
        }
      }

    } catch (Exception e) {
      log.error("Error while validating ERx Request data", e);
      return false;
    }
    if (!getErrorMap().isEmpty())
      return false;
    return true;
  }
}
