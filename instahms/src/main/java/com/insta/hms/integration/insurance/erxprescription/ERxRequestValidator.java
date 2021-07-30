package com.insta.hms.integration.insurance.erxprescription;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsService;
import com.insta.hms.core.patient.registration.RegistrationPreferencesService;
import com.insta.hms.erxprescription.EPrescription;
import com.insta.hms.erxprescription.EPrescriptionActivity;
import com.insta.hms.erxprescription.EPrescriptionDiagnosis;
import com.insta.hms.erxprescription.EPrescriptionPatient;
import com.insta.hms.erxprescription.ERxPrescription;
import com.insta.hms.erxprescription.ERxPrescriptionHeader;
import com.insta.hms.erxprescription.ERxRequest;
import com.insta.hms.eservice.EValidator;
import com.insta.hms.integration.insurance.pbm.PBMPrescriptionsService;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesService;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyService;
import com.insta.hms.mdm.tpas.TpaService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class ERxRequestValidator.
 */
@Component
@Scope("prototype")
public class ERxRequestValidator extends EValidator<ERxRequest> {

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The registration preferences service. */
  @LazyAutowired
  private RegistrationPreferencesService registrationPreferencesService;

  /** The pbm prescriptions service. */
  @LazyAutowired
  private PBMPrescriptionsService pbmPrescriptionsService;

  /** The prescriptions service. */
  @LazyAutowired
  private PrescriptionsService prescriptionsService;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The health authority preferences service. */
  @LazyAutowired
  private HealthAuthorityPreferencesService healthAuthorityPreferencesService;

  /** The insurance company service. */
  @LazyAutowired
  private InsuranceCompanyService insuranceCompanyService;

  /** The tpa service. */
  @LazyAutowired
  private TpaService tpaService;

  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.eservice.EValidator#validate(java.lang.Object)
   */
  @Override
  public boolean validate(ERxRequest data) {
    String path = RequestContext.getHttpRequest().getContextPath();
    path = path + "/";
    ERxPrescription erxp = (ERxPrescription) data;
    ERxPrescriptionHeader header = (ERxPrescriptionHeader) erxp.getHeader();
    Object consIdStr = erxp.getRequestId();
    if (!validateNotEmpty(consIdStr, messageUtil.getMessage("exception.erx.consultation.error"),
        messageUtil.getMessage("exception.empty.erx.consultation.msg"), new Object[] {consIdStr},
        false)) {
      return false;
    }

    int pbmPrescId = prescriptionsService.getErxConsPBMId(consIdStr);
    if (!validateNotEmpty(consIdStr,
        messageUtil.getMessage("exception.erx.pbm.prescription.id.error"),
        messageUtil.getMessage("exception.empty.erx.pbm.prescription.id.msg"),
        new Object[] {pbmPrescId}, false)) {
      return false;
    }
    BasicDynaBean erxConsBean = pbmPrescriptionsService.getConsErxDetails(pbmPrescId);
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer erxCenterId = (Integer) sessionAttributes.get("centerId");
    if (null != erxConsBean.get("erx_center_id")) {
      erxCenterId = (Integer) erxConsBean.get("erx_center_id");
    }

    Boolean isSelfpaySponsor = (Boolean) erxConsBean.get("is_selfpay_sponsor");
    BasicDynaBean erxcenterBean = centerService.findByKey(erxCenterId);
    String patientId = (String) erxConsBean.get("patient_id");
    String doctorId = (String) erxConsBean.get("doctor_name");
    String tpaId = (String) erxConsBean.get("primary_sponsor_id");
    String insuCompId = (String) erxConsBean.get("primary_insurance_co");
    String healthAuthority = (String) erxcenterBean.get("health_authority");
    HttpSession session = RequestContext.getSession();
    java.util.HashMap actionUrlMap =
        (java.util.HashMap) session.getServletContext().getAttribute("actionUrlMap");
    java.util.HashMap urlRightsMap = (java.util.HashMap) session.getAttribute("urlRightsMap");

    // its a selfpay patient
    Boolean isSelfpay = (tpaId == null || isSelfpaySponsor);

    if (!isSelfpay && !validateNotEmpty(insuCompId,
        messageUtil.getMessage("exception.insurace.company.id.error"),
        messageUtil.getMessage("exception.empty.insurace.company.id.msg"),
        new Object[] {patientId, path + ((String) actionUrlMap.get("change_visit_tpa"))
            + "?_method=changeTpa&visitId=" + patientId},
        (urlRightsMap.get("change_visit_tpa").equals("A")))) {
      return false;
    }

    BasicDynaBean erxheaderBean = genericPreferencesService.getERxHeaderFields(pbmPrescId, doctorId,
        tpaId, insuCompId, healthAuthority);

    String centerName = (String) erxcenterBean.get("center_name");
    if (healthAuthority == null || healthAuthority.trim().equals("")
        || !healthAuthority.equals("DHA")) {
      if (!validateNotEmpty("", messageUtil.getMessage("exception.erx.health.authority.error"),
          messageUtil.getMessage("exception.empty.erx.health.authority.msg"),
          new Object[] {centerName,
              path + URLRoute.CENTER_PATH + "/show.htm?center_id" + erxCenterId},
          (urlRightsMap.get("mas_centers").equals("A")))) {
        return false;
      }
    }

    if (erxCenterId != null) {
      validateNotEmpty(header.getSenderID(), messageUtil.getMessage("exception.sender.id.error"),
          messageUtil.getMessage("exception.empty.sender.id.msg"),
          new Object[] {(String) erxheaderBean.get("center_name"),
              path + URLRoute.CENTER_PATH + "/show.htm?center_id" + erxCenterId},
          (urlRightsMap.get("mas_centers").equals("A")));
    } else {
      validateNotEmpty(header.getSenderID(), messageUtil.getMessage("exception.sender.id.error"),
          messageUtil.getMessage("exception.empty.sender.id.msg"),
          new Object[] {"Generic Preferences",
              path + com.insta.hms.common.URLRoute.GENERIC_PREFERENCES + ".htm#/genericPreference"},
          (urlRightsMap.get("mas_gen_pref").equals("A")));
    }

    EPrescription prescription = erxp.getPrescription();
    EPrescriptionPatient patient = prescription.getPatient();
    if (!isSelfpay) {
      String insuCompName = (String) insuranceCompanyService
          .findByUniqueName(insuCompId, "insurance_co_id").get("insurance_co_name");
      validateNotEmpty(header.getReceiverID(),
          messageUtil.getMessage("exception.receiver.id.error"),
          messageUtil.getMessage("exception.empty.receiver.id.msg"),
          new Object[] {insuCompName,
              path + ((String) actionUrlMap.get("mas_insurance_comp"))
                  + "?_method=show&insurance_co_id=" + insuCompId},
          (urlRightsMap.get("mas_insurance_comp").equals("A")));

      validateNotEmpty(patient.getMemberId(), messageUtil.getMessage("exception.member.id.error"),
          messageUtil.getMessage("exception.empty.member.id.msg"),
          new Object[] {patientId, path + ((String) actionUrlMap.get("change_visit_tpa"))
              + "?_method=changeTpa&visitId=" + patientId},
          (urlRightsMap.get("change_visit_tpa").equals("A")));
    }

    BasicDynaBean regPref = registrationPreferencesService.getRegistrationPreferences();
    String govenmtIdLabel = (String) (regPref.get("government_identifier_label") != null
        ? regPref.get("government_identifier_label")
        : "Emirates ID");
    String govenmtIdTypeLabel = (String) (regPref.get("government_identifier_label") != null
        ? regPref.get("government_identifier_type_label")
        : "Emirates ID Type");

    String mrNo = (String) erxConsBean.get("mr_no");
    validateNotEmpty(patient.getEmiratesIDNumber(),
        messageUtil.getMessage("exception.emirates.id.error"),
        govenmtIdLabel + " (or) " + govenmtIdTypeLabel + " is required",
        new Object[] {mrNo, path + ((String) actionUrlMap.get("reg_general"))
            + "?_method=show&regType=regd&mr_no=" + mrNo + "&mrno=" + mrNo},
        (urlRightsMap.get("reg_general").equals("A")));


    validateNotEmpty(prescription.getClinician(),
        messageUtil.getMessage("exception.erx.clinician.id.error"),
        messageUtil.getMessage("exception.empty.erx.clinician.id.msg"),
        new Object[] {(String) erxheaderBean.get("doctor_name"),
            path + ((String) actionUrlMap.get("mas_doctors_detail"))
                + "?_method=getDoctorDetailsScreen&mode=update&doctor_id=" + doctorId},
        (urlRightsMap.get("mas_doctors_detail").equals("A")));

    if (consIdStr instanceof Integer) {
      validateNotEmpty(patient.getWeight() == 0 ? null : patient.getWeight(),
          messageUtil.getMessage("exception.patient.weight.error"),
          messageUtil.getMessage("exception.empty.patient.weight.msg"),
          new Object[] {"Consultation", path + ((String) actionUrlMap.get("op_prescribe"))
              + "?_method=list&consultation_id=" + (int) consIdStr},
          (urlRightsMap.get("op_prescribe").equals("A")));
    } else {
      validateNotEmpty(patient.getWeight() == 0 ? null : patient.getWeight(),
          messageUtil.getMessage("exception.patient.weight.error"),
          messageUtil.getMessage("exception.empty.patient.weight.msg"),
          new Object[] {"IPEMR", path + ("ipemr/index.htm#/filter/default/patient/" + mrNo
                  + "/ipemr/visit/" + consIdStr.toString() + "?retain_route_params=true")},
          (urlRightsMap.get("visit_summary").equals("A")));
    }


    Object[] codifyParamsArr =
        new Object[] {patientId, path + ((String) actionUrlMap.get("update_mrd"))
            + "?_method=getMRDUpdateScreen&patient_id=" + patientId};

    boolean hasCodifyRights = (urlRightsMap.get("update_mrd").equals("A"));

    int encType = prescription.getEncounter().getType();
    // Encounter not mandatory
    validateNotEmpty(encType == 0 ? null : encType,
        messageUtil.getMessage("exception.encounter.type.error"),
        messageUtil.getMessage("exception.empty.encounter.type.msg"), codifyParamsArr,
        hasCodifyRights);

    List<EPrescriptionDiagnosis> eprescDiagnosis = prescription.getDiagnosis();
    for (EPrescriptionDiagnosis diag : eprescDiagnosis) {

      validateNotEmpty(diag.getCode(), messageUtil.getMessage("exception.diagnosis.code.error"),
          messageUtil.getMessage("exception.empty.diagnosis.code.msg"), codifyParamsArr,
          hasCodifyRights);

      validateNotEmpty(diag.getType(), messageUtil.getMessage("exception.diagnosis.type.error"),
          messageUtil.getMessage("exception.empty.diagnosis.type.msg"), codifyParamsArr,
          hasCodifyRights);
    }

    BasicDynaBean genericPrefs = genericPreferencesService.getAllPreferences();
    String prescriptionUsesStores = (String) genericPrefs.get("prescription_uses_stores");
    String prescByGenerics = (String) healthAuthorityPreferencesService
        .findByUniqueName(healthAuthority, "health_authority").get("prescriptions_by_generics");
    boolean isGenerics = (prescriptionUsesStores.equals("Y") && prescByGenerics.equals("Y"));

    List<EPrescriptionActivity> activities = prescription.getActivities();

    if (isGenerics) {
      for (EPrescriptionActivity activity : activities) {
        validateNotEmpty(activity.getActivityCode(),
            messageUtil.getMessage("exception.activity.code.error"),
            messageUtil.getMessage("exception.empty.activity.code.msg"),
            new Object[] {activity.getActivityName()}, false);

        validateNotEmpty(activity.getActivityType(),
            messageUtil.getMessage("exception.activity.type.error"),
            messageUtil.getMessage("exception.empty.activity.type.msg"),
            new Object[] {activity.getActivityName()}, false);

        validateNotEmpty(activity.getRoutOfAdmin() == null ? null : activity.getRoutOfAdmin(),
            messageUtil.getMessage("exception.route.code.error"),
            messageUtil.getMessage("exception.empty.route.code.msg"),
            new Object[] {"Medicine Route: " + activity.getRouteOfAdminName(),
                path + URLRoute.ROUTE_OF_ADMINISTRATION_PATH + "/show.htm?route_id="
                    + activity.getRouteOfAdminId()},
            (urlRightsMap.get("medicine_route").equals("A")));

        EPrescriptionActivity.Frequency actFreq = activity.getFrequency();
        if (actFreq != null && (actFreq.getValueType() != null)
            && !(actFreq.getValueType().equals(""))) {

          validateNotEmpty(actFreq.getType(),
              messageUtil.getMessage("exception.frequency.type.error"),
              messageUtil.getMessage("exception.empty.frequency.type.msg"),
              new Object[] {"Frequency: " + actFreq.getValueType(),
                  path + ((String) actionUrlMap.get("mas_medicine_dosage"))
                      + "?_method=show&med_dosage_name=" + actFreq.getValueType()},
              (urlRightsMap.get("mas_medicine_dosage").equals("A")));

          validateNotEmpty(
              actFreq.getValue().compareTo(BigDecimal.ZERO) == 0 ? null : actFreq.getValue(),
              messageUtil.getMessage("exception.frequency.value.error"),
              messageUtil.getMessage("exception.empty.frequency.value.msg"),
              new Object[] {"Frequency: " + actFreq.getValueType(),
                  path + ((String) actionUrlMap.get("mas_medicine_dosage"))
                      + "?_method=show&med_dosage_name=" + actFreq.getValueType()},
              (urlRightsMap.get("mas_medicine_dosage").equals("A")));
        }
      }
    } else {
      for (EPrescriptionActivity activity : activities) {
        validateNotEmpty(activity.getActivityCode(),
            messageUtil.getMessage("exception.activity.code.error"),
            messageUtil.getMessage("exception.empty.activity.code.msg"),
            new Object[] {activity.getActivityName(),
                path + ((String) actionUrlMap.get("mas_medicines"))
                    + "?_method=editItemCode&medicine_id=" + activity.getMedicineID()},
            (urlRightsMap.get("mas_medicines").equals("A")));

        validateNotEmpty(activity.getActivityType(),
            messageUtil.getMessage("exception.activity.type.error"),
            messageUtil.getMessage("exception.empty.activity.type.msg"),
            new Object[] {activity.getActivityName(),
                path + ((String) actionUrlMap.get("mas_medicines"))
                    + "?_method=editItemCode&medicine_id=" + activity.getMedicineID()},
            (urlRightsMap.get("mas_medicines").equals("A")));

        validateNotEmpty(activity.getRoutOfAdmin() == null ? null : activity.getRoutOfAdmin(),
            messageUtil.getMessage("exception.route.code.error"),
            messageUtil.getMessage("exception.empty.route.code.msg"),
            new Object[] {"Medicine Route: " + activity.getRouteOfAdminName(),
                path + URLRoute.ROUTE_OF_ADMINISTRATION_PATH + "/show.htm?route_id="
                    + activity.getRouteOfAdminId()},
            (urlRightsMap.get("medicine_route").equals("A")));

        EPrescriptionActivity.Frequency actFreq = activity.getFrequency();
        if (actFreq != null && (actFreq.getValueType() != null)
            && !(actFreq.getValueType().equals(""))) {

          validateNotEmpty(actFreq.getType(),
              messageUtil.getMessage("exception.frequency.type.error"),
              messageUtil.getMessage("exception.empty.frequency.type.msg"),
              new Object[] {"Frequency: " + actFreq.getValueType(),
                  path + ((String) actionUrlMap.get("mas_medicine_dosage"))
                      + "?_method=show&med_dosage_name=" + actFreq.getValueType()},
              (urlRightsMap.get("mas_medicine_dosage").equals("A")));

          validateNotEmpty(
              actFreq.getValue().compareTo(BigDecimal.ZERO) == 0 ? null : actFreq.getValue(),
              messageUtil.getMessage("exception.frequency.value.error"),
              messageUtil.getMessage("exception.empty.frequency.value.msg"),
              new Object[] {"Frequency: " + actFreq.getValueType(),
                  path + ((String) actionUrlMap.get("mas_medicine_dosage"))
                      + "?_method=show&med_dosage_name=" + actFreq.getValueType()},
              (urlRightsMap.get("mas_medicine_dosage").equals("A")));
        }
      }
    }
    return getErrorMap().isEmpty();
  }
}
