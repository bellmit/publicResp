/**
 *
 */

package com.insta.hms.eauthorization;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityDTO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDTO;
import com.insta.hms.master.ServiceMaster.ServiceMasterDAO;
import com.insta.hms.pbmauthorization.PBMPrescriptionHelper;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * The Class EAuthRequestValidator.
 *
 * @author lakshmi
 */
public class EAuthRequestValidator {

  /**
   * The pbmhelper.
   */
  PBMPrescriptionHelper pbmhelper = new PBMPrescriptionHelper();

  /**
   * The preauth presc dao.
   */
  EAuthPrescriptionDAO preauthPrescDao = new EAuthPrescriptionDAO();

  /**
   * Validate E auth request.
   *
   * @param errorsMap            the errors map
   * @param shafafiyaEAuthActive the shafafiya E auth active
   * @param healthAuthority      the health authority
   * @param preauthBean          the preauth bean
   * @param eauthRequest         the e auth request
   * @throws Exception the exception
   */
  public void validateEAuthRequest(Map<String, StringBuilder> errorsMap,
                                   String shafafiyaEAuthActive, String healthAuthority,
                                   BasicDynaBean preauthBean, EAuthRequest eauthRequest)
      throws Exception {

    HttpSession session = RequestContext.getSession();
    String path = RequestContext.getHttpRequest().getContextPath();
    String testingMemberId = ""; // "04274222"

    Integer userCenterId = RequestContext.getCenterId();
    userCenterId = userCenterId == null ? 0 : userCenterId;
    BasicDynaBean centerBean = new CenterMasterDAO().findByKey("center_id",
        userCenterId);
    if (centerBean != null && !healthAuthority.equals("DHA")) {
      String shafafiyaPreauthTestMemberId = centerBean
          .get("shafafiya_preauth_test_member_id") != null
          ? ((String) centerBean
          .get("shafafiya_preauth_test_member_id"))
          .trim()
          : "";

      testingMemberId = shafafiyaPreauthTestMemberId;
    }

    boolean modEclaim = (Boolean) session.getAttribute("mod_eclaim");

    RegistrationPreferencesDTO regPref = RegistrationPreferencesDAO
        .getRegistrationPreferences();

    String govenmtIdLabel = regPref.getGovernment_identifier_label() != null
        ? regPref.getGovernment_identifier_label()
        : "Emirates ID";
    String govenmtIdTypeLabel = regPref
        .getGovernment_identifier_label() != null
        ? regPref.getGovernment_identifier_type_label()
        : "Emirates ID Type";

    String encTypePref = regPref.getEncntr_type_reqd() != null
        ? regPref.getEncntr_type_reqd()
        : "RQ";

    StringBuilder attachmentErr = new StringBuilder(
        "<br/> ATTACHMENT ERROR: Prior Auth Prescription has attachment which could not be "
            + "attached in XML. <br/>"
            + "Please check the attachments for Prior Auth Prescription : <br/> ");

    StringBuilder actQtyErr = new StringBuilder(
        "<br/> ACTIVITY QUANTITY ERROR: Activity Qty should not be zero. <br/>");

    StringBuilder noCliniciansErr = new StringBuilder(
        "<br/> NO CLINICIANS ERROR: Clinician should not be empty for patient. <br/>");

    StringBuilder clinicianIdErr = new StringBuilder(
        "<br/> CLINICIAN ERROR: Clinician without/invalid clinician Id. <br/>");

    StringBuilder encountersErr = new StringBuilder(
        "<br/> ENCOUNTERS ERROR: Invalid/No encounter types. <br/>");

    StringBuilder diagnosisCodesErr = new StringBuilder(
        "<br/> DIAGNOSIS ERROR: Invalid/No diagnosis codes. <br/>");

    StringBuilder accumedDiagnosisCodeTypeErr = new StringBuilder(
        "<br/> DIAGNOSIS CODE TYPE ERROR: Invalid diagnosis code type. Allowed:(ICD9 / ICD10)"
            + "<br/>");

    StringBuilder haadDiagnosisCodeTypeErr = new StringBuilder(
        "<br/> DIAGNOSIS CODE TYPE ERROR: Invalid diagnosis code type. Allowed:(ICD)<br/>");

    StringBuilder noActivitiesErr = new StringBuilder(
        "<br/> ZERO ACTIVITIES ERROR: Prior Auth Prescription has no activities. <br/>");

    StringBuilder codesErr = new StringBuilder(
        "<br/> CODES ERROR: Invalid/No activity codes. <br/>");

    StringBuilder toothCodesErr = new StringBuilder(
        "<br/> TOOTH NUMBER ERROR: Invalid/No tooth number observation(s).<br/>");

    StringBuilder toothNoCodesErr = new StringBuilder(
        "<br/> TOOTH NUMBER CODES ERROR: Invalid/No tooth number observation code(s).<br/>");

    StringBuilder toothNoUniversalCodeErr = new StringBuilder(
        "<br/> TOOTH NUMBER NO UNIVERSAL CODE ERROR: Invalid/No universal code.<br/>");

    StringBuilder diagnosticsCodesErr = new StringBuilder(
        "<br/> DIAG CODES ERROR: Invalid/No observation code.<br/>");

    StringBuilder govtIdNoErr = new StringBuilder(
        "<br/> EMIRATES ID ERROR: Prior Auth Prescription without "
            + govenmtIdLabel + " (or) " + govenmtIdTypeLabel
            + ".<br/>");

    StringBuilder receiverErr = new StringBuilder(
        "<br/> RECEIVER ERROR: Prior Auth Prescription does not contain receiver/tpa code. <br/>");

    StringBuilder noCompanyErr = new StringBuilder(
        "<br/> COMPANY ERROR: Prior Auth Prescription does not contain insurance company. <br/>");

    StringBuilder payerErr = new StringBuilder(
        "<br/> PAYER ERROR: Prior Auth Prescription does not contain payer/company code. <br/>");

    StringBuilder testingMemberErr = new StringBuilder(
        "<br/> TESTING MEMBER ERROR: Shafafiya Prior Auth Web service is not set to active mode, "
            + "cannot use live data. <br/>"
            + "For testing purpose, the Member Id for the patient needs to be: <b> "
            + testingMemberId + " </b> <br/> ");

    if (shafafiyaEAuthActive.equals("N") && testingMemberId.equals("")
        && !healthAuthority.equals("DHA")) {
      errorsMap.put("TESTING EMPTY MEMBER ERROR:", new StringBuilder(
          "<br/> TESTING EMPTY MEMBER ERROR: Shafafiya Prior Auth Web service is not set to "
              + " active mode, cannot use live data. <br/>"
              + "Prior Auth Prescription Request cannot be sent. Test Member Id cannot be null."));
    }

    HealthAuthorityDTO healthAuthorityPref = HealthAuthorityPreferencesDAO
        .getHealthAuthorityPreferences(healthAuthority);
    String eclaimXMLSchema = healthAuthorityPref.getHealth_authority();
    eclaimXMLSchema = eclaimXMLSchema != null ? eclaimXMLSchema : "HAAD";
    String defaultDiagnosisCodeType = healthAuthorityPref
        .getDiagnosis_code_type();
    defaultDiagnosisCodeType = defaultDiagnosisCodeType != null
        ? defaultDiagnosisCodeType
        : "ICD";

    List<BasicDynaBean> diagnosis = null;
    Map<String, List> observationsMap = new HashMap<String, List>();
    List<String> observationNamesList = new ArrayList<String>();

    try {

      int preauthPrescId = (Integer) preauthBean.get("preauth_presc_id");
      String preauthPrescIdStr = preauthPrescId + "";
      String mrNo = (String) preauthBean.get("mr_no");
      String patientId = (String) preauthBean.get("patient_id");
      String memberId = (String) preauthBean.get("member_id");
      String doctorId = (String) preauthBean.get("doctor_id");

      String doctorType = (String) preauthBean.get("doctor_type");

      if (shafafiyaEAuthActive.equals("N")
          && !memberId.trim().equals(testingMemberId.trim())
          && !healthAuthority.equals("DHA")) {
        errorsMap.put("TESTING MEMBER ERROR:",
            testingMemberErr.append(pbmhelper.urlString(path,
                "insurance", patientId, null)));
        testingMemberErr.append(" , ");
      }

      String receiverId = preauthBean.get("receiver_id") != null
          ? (String) preauthBean.get("receiver_id")
          : null;
      String tpaId = preauthBean.get("tpa_id") != null
          ? (String) preauthBean.get("tpa_id")
          : null;
      String tpaName = preauthBean.get("tpa_name") != null
          ? (String) preauthBean.get("tpa_name")
          : null;

      if (receiverId == null || receiverId.trim().equals("")) {
        errorsMap.put("RECEIVER ERROR:", receiverErr.append(pbmhelper
            .urlString(path, "sponsor", tpaId, tpaName)));
        receiverErr.append(" , ");
      }

      String payerId = preauthBean.get("payer_id") != null
          ? (String) preauthBean.get("payer_id")
          : null;
      String insuranceCoId = preauthBean.get("insurance_co_id") != null
          ? (String) preauthBean.get("insurance_co_id")
          : null;
      String insuranceCoName = preauthBean
          .get("insurance_co_name") != null
          ? (String) preauthBean.get("insurance_co_name")
          : null;

      if (insuranceCoId == null || insuranceCoId.trim().equals("")) {
        errorsMap.put("COMPANY ERROR:", noCompanyErr.append(pbmhelper
            .urlString(path, "preauth", preauthPrescIdStr, null)));
        noCompanyErr.append(" , ");

      } else if (payerId == null || payerId.trim().equals("")) {
        errorsMap.put("PAYER ERROR:",
            payerErr.append(pbmhelper.urlString(path, "company",
                insuranceCoId, insuranceCoName)));
        payerErr.append(" , ");
      }

      eauthRequest.setEauthbean(preauthBean);

      String isResubmit = preauthBean.get("is_resubmit") != null
          ? ((String) preauthBean.get("is_resubmit")).toString()
          : "N";
      if (isResubmit != null && isResubmit.equals("Y")) {
        Map attachmentMap = preauthPrescDao
            .getAttachment(preauthPrescId);
        InputStream file = (InputStream) attachmentMap.get("Content");
        if (file != null) {
          String attachment = pbmhelper.convertToBase64Binary(file);
          if (attachment != null) {
            eauthRequest.setAttachment(attachment);
          } else {
            errorsMap.put("ATTACHMENT ERROR:",
                attachmentErr.append(
                    pbmhelper.urlString(path, "attachment",
                        preauthPrescIdStr, null)));
            attachmentErr.append(" , ");
          }
        }
      }

      String emiratesIdNumber = preauthBean
          .get("emirates_id_number") != null
          ? (String) preauthBean.get("emirates_id_number")
          : null;
      if (emiratesIdNumber == null || emiratesIdNumber.equals("")) {
        errorsMap.put("EMIRATES ID ERROR:", govtIdNoErr.append(pbmhelper
            .urlString(path, "pre-registration", mrNo, null)));
        govtIdNoErr.append("  ,  ");
      }

      String encounterType = preauthBean.get("encounter_type") != null
          ? ((Integer) preauthBean.get("encounter_type")).toString()
          : null;
      String visitType = (String) preauthBean.get("visit_type");
      if (((visitType.equals("i") && encTypePref.equals("IP"))
          || (visitType.equals("o") && encTypePref.equals("OP"))
          || encTypePref.equals("RQ"))
          && (encounterType == null || encounterType.equals("0"))) {
        errorsMap.put("ENCOUNTERS ERROR:", encountersErr.append(", "));
      }

      diagnosis = preauthPrescDao.findAllDiagnosis(patientId);
      eauthRequest.setDiagnosis(diagnosis);

      if (diagnosis == null || diagnosis.size() == 0) {
        errorsMap.put("DIAGNOSIS ERROR:",
            diagnosisCodesErr.append(", "));

      } else {
        for (BasicDynaBean diag : diagnosis) {
          String codeType = (String) diag.get("code_type");
          String icdCode = (String) diag.get("icd_code");

          if (icdCode == null || icdCode.equals("")) {
            errorsMap.put("DIAGNOSIS ERROR:",
                diagnosisCodesErr.append(", "));
          }

          if (modEclaim) {
            // For HAAD -- Diagnosis code types is ICD.
            if (!codeType
                .equalsIgnoreCase(defaultDiagnosisCodeType)) {
              errorsMap.put("DIAGNOSIS CODE TYPE ERROR:",
                  haadDiagnosisCodeTypeErr.append(" , "));
            }
          }
        }
      }

      String doctorName = (String) preauthBean.get("doctor");
      String doctorLicenseNumber = (String) preauthBean
          .get("doctor_license_number");
      if (doctorName == null || doctorName.trim().equals("")) {

        errorsMap.put("NO CLINICIANS ERROR:",
            noCliniciansErr.append(pbmhelper.urlString(path,
                "patient", patientId, null)));
        noCliniciansErr.append(" , ");

      } else if (doctorLicenseNumber == null
          || doctorLicenseNumber.trim().equals("")) {

        if (doctorType.equals("Doctor")) {
          errorsMap.put("CLINICIAN ERROR:",
              clinicianIdErr.append(pbmhelper.urlString(path,
                  "doctor", doctorId, doctorName)));
        } else {
          errorsMap.put("CLINICIAN ERROR:",
              clinicianIdErr.append(pbmhelper.urlString(path,
                  "referral", doctorId, doctorName)));
        }
        clinicianIdErr.append(" , ");
      }

      List<BasicDynaBean> preauthActivities = new ArrayList<
          BasicDynaBean>();

      String resubmissionType = preauthBean.get("resubmit_type") != null
          ? ((String) preauthBean.get("resubmit_type")).toString()
          : null;
      if (isResubmit.equals("Y")
          && resubmissionType.equalsIgnoreCase("internal complaint")) {
        preauthActivities = preauthPrescDao
            .findEAuthActivities(preauthPrescId, true);
      } else {
        preauthActivities = preauthPrescDao
            .findEAuthActivities(preauthPrescId, false);
      }

      // Check for codes for each activity
      for (BasicDynaBean item : preauthActivities) {
        String actCode = (String) item.get("act_code");
        String preauthActName = (String) item.get("preauth_act_name");
        String preauthActItemId = (String) item
            .get("preauth_act_item_id");
        String actType = (String) item.get("preauth_act_type");
        /* Date prescribed_date = (Date)item.get("prescribed_date"); */
        Timestamp prescribedDate = (Timestamp) item
            .get("prescribed_date");
        int actQty = item.get("act_qty") != null
            ? (Integer) item.get("act_qty")
            : 0;

        String preauthActType = "";
        if (actType.equals("SER")) {
          preauthActType = "Service";
        } else if (actType.equals("DIA")) {
          preauthActType = "Investigation";
        } else if (actType.equals("OPE")) {
          preauthActType = "Operation";
        } else if (actType.equals("DOC")) {
          preauthActType = "Doctor";
        }

        if (actQty == 0) {
          errorsMap.put("ACTIVITY QUANTITY ERROR:",
              actQtyErr.append(pbmhelper.urlString(path,
                  "preauth", preauthPrescIdStr, null)));
          actQtyErr.append("<br/>Prior Auth ID :" + preauthPrescIdStr
              + "Activity : ( " + preauthActName
              + ", Prescribed Date: " + prescribedDate
              + " ), <br/> ");
        }

        if (actCode == null || actCode.trim().equals("")) {
          errorsMap.put("CODES ERROR:", codesErr);
          codesErr.append(preauthActType + ": ( " + preauthActName
              + ", Prescribed Date: " + prescribedDate
              + " ), <br/> ");
        }

        Integer preauthActId = (Integer) item.get("preauth_act_id");
        List<BasicDynaBean> observations = preauthPrescDao.getEAuthActObservations(
            preauthActId, eclaimXMLSchema);

        List<BasicDynaBean> docUploads = preauthPrescDao
            .getPreAuthDocumentLists(preauthActId);

        observations.addAll(docUploads);

        /* Check for tooth number if required if dental service */
        if (preauthActType.equalsIgnoreCase("SER")) {
          BasicDynaBean service = new ServiceMasterDAO()
              .findByKey("service_id", preauthActItemId);
          if (service != null
              && service.get("tooth_num_required").equals("Y")) {
            if (observations == null || observations.size() == 0) {
              errorsMap.put("TOOTH NUMBER ERROR:", toothCodesErr);
              toothCodesErr.append(preauthActType + ": ( "
                  + preauthActName + ", Prescribed Date: "
                  + prescribedDate + " ), <br/> ");
            }
            int numOfDentalCodes = 0;
            if (observations.size() > 0) {
              for (BasicDynaBean observation : observations) {
                String resultCode = (String) observation
                    .get("code");
                String resultType = (String) observation
                    .get("obs_type");
                BasicDynaBean haadBean = new GenericDAO(
                    "mrd_supported_code_types").findByKey(
                    "code_type", resultType);
                Integer haadCode = haadBean == null ? null
                    : (Integer) haadBean.get("haad_code");
                if (haadCode != null
                    && haadCode.intValue() == 16) {
                  ++numOfDentalCodes;
                }
                if (resultCode == null
                    || resultCode.equals("")) {
                  errorsMap.put("TOOTH NUMBER CODES ERROR:",
                      toothNoCodesErr);
                  toothNoCodesErr.append(preauthActType
                      + ": ( " + preauthActName
                      + ", Prescribed Date: "
                      + prescribedDate + " ), <br/> ");
                }
              }
            }
            if (numOfDentalCodes == 0) {
              errorsMap.put(
                  "TOOTH NUMBER NO UNIVERSAL CODE ERROR:",
                  toothNoUniversalCodeErr);
              toothNoUniversalCodeErr.append(
                  preauthActType + ": ( " + preauthActName
                      + ", Prescribed Date: "
                      + prescribedDate + " ), <br/> ");
            }
          }
        }

        if (observations != null && observations.size() > 0) {

          /*
           * Check for codes for each observation if lab test i.e
           * LTDIA
           */
          if (preauthActType.equals("DIA")) {
            for (BasicDynaBean observation : observations) {
              String resultCode = (String) observation
                  .get("code");
              String resultValue = (String) observation
                  .get("value");

              if ((resultCode != null && !resultCode.equals(""))
                  && (resultValue == null
                  || resultValue.equals(""))) {

                errorsMap.put("DIAG CODES ERROR:",
                    diagnosticsCodesErr);
                diagnosticsCodesErr.append(preauthActType
                    + ": ( " + preauthActName
                    + ", Prescribed Date: "
                    + prescribedDate + " ), <br/> ");

              }
            }
          }

          observationsMap.put(preauthActId + "", observations);
        } // Observations
      }

      eauthRequest.setActivities(preauthActivities);
      eauthRequest.setObservationsMap(observationsMap);

    } catch (Exception exception) {
      throw exception;
    }
  }
}
