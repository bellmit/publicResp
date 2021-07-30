package com.insta.hms.integration.insurance.erxprescription;

import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsService;
import com.insta.hms.core.clinical.diagnosisdetails.DiagnosisDetailsService;
import com.insta.hms.core.clinical.vitalforms.VitalReadingService;
import com.insta.hms.erxprescription.EPrescription;
import com.insta.hms.erxprescription.EPrescriptionActivity;
import com.insta.hms.erxprescription.EPrescriptionDiagnosis;
import com.insta.hms.erxprescription.EPrescriptionEncounter;
import com.insta.hms.erxprescription.EPrescriptionPatient;
import com.insta.hms.erxprescription.ERxPrescription;
import com.insta.hms.erxprescription.ERxPrescriptionHeader;
import com.insta.hms.erxprescription.ERxRequest;
import com.insta.hms.eservice.ERequestXMLGenerator;
import com.insta.hms.integration.insurance.pbm.PBMPrescriptionsService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.medicinedosage.MedicineDosageService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

// TODO: Auto-generated Javadoc
/**
 * The Class ERxRequestGenerator.
 */
@Component
@Scope("prototype")
public class ERxRequestGenerator
    extends ERequestXMLGenerator<ERxRequest, ERxRequestValidator> {

  /** The bean factory. */
  @Autowired
  private BeanFactory beanFactory;

  /** The pbm prescriptions service. */
  @LazyAutowired
  private PBMPrescriptionsService pbmPrescriptionsService;

  /** The prescriptions service. */
  @LazyAutowired
  private PrescriptionsService prescriptionsService;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The vital reading service. */
  @LazyAutowired
  private VitalReadingService vitalReadingService;

  /** The diagnosis details service. */
  @LazyAutowired
  private DiagnosisDetailsService diagnosisDetailsService;

  /** The medicine dosage service. */
  @LazyAutowired
  private MedicineDosageService medicineDosageService;

  /** The log. */
  private Logger log = LoggerFactory.getLogger(this.getClass());

  /** The Constant ERX_BODY_TEMPLATE. */
  private static final String ERX_BODY_TEMPLATE = "ERxRequestBody.ftl";

  /** The Constant ERX_HEADER_TEMPLATE. */
  private static final String ERX_HEADER_TEMPLATE = "ERxRequestHeader.ftl";

  /** The Constant ERX_FOOTER_TEMPLATE. */
  private static final String ERX_FOOTER_TEMPLATE = "ERxRequestFooter.ftl";

  /** The test mode. */
  private boolean testMode = false; // = "testing while in testing mode"

  /**
   * Instantiates a new e rx request generator.
   */
  public ERxRequestGenerator() {
    this(false); // by default production mode
  }

  /**
   * Instantiates a new e rx request generator.
   *
   * @param testMode the test mode
   */
  public ERxRequestGenerator(boolean testMode) {
    super(ERX_BODY_TEMPLATE, ERX_HEADER_TEMPLATE, ERX_FOOTER_TEMPLATE);
    this.testMode = testMode;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.eservice.ERequestXMLGenerator#getRequest(java.lang.String, java.lang.String)
   */
  @Override
  public ERxRequest getRequest(Object requestId, String requestType) {
    return createNewERxRequest(requestId, requestType);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.eservice.ERequestXMLGenerator#getRequestValidator()
   */
  @Override
  public ERxRequestValidator getRequestValidator() {
    return beanFactory.getBean(ERxRequestValidator.class);
  }

  /**
   * Creates a ERxRequest based on the requestId. Fetches all the relevant data from the database
   * for a given requestId and requestType and populates a map-like object. The object will be used
   * in the FTL to finally generate an XML. Note : This method should not do any data validation,
   * that should be done only in the validator. Also it should not do any updates to the existing
   * data. That should be done before or after the XML generation, as appropriate.
   *
   * @param consIdStr the cons id str
   * @param requestType the request type
   * @return the erx request
   */
  private ERxRequest createNewERxRequest(Object consId, String requestType) {

    ERxPrescription erxPresc = new ERxPrescription();
    try {
      int pbmPrescId = prescriptionsService.getErxConsPBMId(consId);
      erxPresc.setRequestId(consId);

      // Get erx cons bean.
      BasicDynaBean erxConsBean =
          pbmPrescriptionsService.getConsErxDetails(pbmPrescId);
      Boolean isSelfpaySponsor =
          (Boolean) erxConsBean.get("is_selfpay_sponsor") == null ? false
              : (Boolean) erxConsBean.get("is_selfpay_sponsor");
      String doctorId = (String) erxConsBean.get("doctor_name");
      String tpaId = (String) erxConsBean.get("primary_sponsor_id");
      String insuCompId = (String) erxConsBean.get("primary_insurance_co");
      Integer centerId = (Integer) erxConsBean.get("erx_center_id");
      String healthAuthority =
          (String) centerService.findByKey(centerId).get("health_authority");
      BasicDynaBean erxheaderBean =
          genericPreferencesService.getERxHeaderFields(pbmPrescId, doctorId,
              tpaId, insuCompId, healthAuthority);

      // check if the erx is a for a selfpay patient
      Boolean isSelfpay = (isSelfpaySponsor || tpaId == null);

      ERxPrescriptionHeader header = new ERxPrescriptionHeader();
      EPrescription prescription = new EPrescription();
      header.setSenderID((String) erxheaderBean.get("provider_id"));
      // set the following fields if claim is a selfpay claim
      if (isSelfpay) {
        header.setReceiverID("DHA");
        prescription.setPayerId("SelfPay");
      } else {
        header.setReceiverID((String) erxheaderBean.get("receiver_id"));
        prescription.setPayerId((String) erxheaderBean.get("payer_id"));
      }
      header.setRecordCount((Integer) erxheaderBean.get("erx_record_count"));
      header.setTransactionDate((String) erxheaderBean.get("transaction_date"));
      header.setDispositionFlag(testMode ? "TEST" : "PRODUCTION");

      erxPresc.setHeader(header);

      // PrescriptionPayerId IS Insurance License i.e Receiver Id or SelfPay (cash patient)

      prescription.setId((String) erxConsBean.get("erx_presc_id"));
      prescription.setType(requestType);
      prescription
          .setClinician((String) erxheaderBean.get("doctor_license_number"));
      EPrescriptionPatient patient = new EPrescriptionPatient();
      String patientId = (String) erxConsBean.get("patient_id");
      BasicDynaBean weightVitalBean =
          vitalReadingService.getVisitVitalWeightBean(patientId);

      // Set weight
      int weight = 0;
      if (weightVitalBean != null
          && (Integer) weightVitalBean.get("param_id") != 0) {
        System.out.println(weightVitalBean.get("param_value"));
        if (SearchQueryBuilder
            .isInteger((String) weightVitalBean.get("param_value"))) {
          weight = new Integer((String) weightVitalBean.get("param_value"));
        } else if (SearchQueryBuilder
            .isFloat((String) weightVitalBean.get("param_value"))) {
          weight = Math
              .round(new Float((String) weightVitalBean.get("param_value")));
        }
      }
      if (!isSelfpay) {
        patient.setMemberId((String) erxConsBean.get("member_id"));
      } else {
        // set mrNo as memberId for selfpay patient
        patient.setMemberId((String) erxConsBean.get("mr_no"));
      }
      patient
          .setEmiratesIDNumber((String) erxConsBean.get("emirates_id_number"));
      patient.setDateOfBirth((String) erxConsBean.get("dob"));
      patient.setWeight(weight);
      patient.setEmail((String) erxConsBean.get("email_id"));

      prescription.setPatient(patient);

      EPrescriptionEncounter encounter = new EPrescriptionEncounter();
      encounter.setType(erxConsBean.get("encounter_type") != null
          ? (Integer) erxConsBean.get("encounter_type")
          : 0);
      encounter.setFacilityID((String) erxheaderBean.get("provider_id"));

      prescription.setEncounter(encounter);

      List<BasicDynaBean> prescDiagnosis =
          diagnosisDetailsService.findAllDiagnosis(patientId);

      for (BasicDynaBean diagbean : prescDiagnosis) {
        EPrescriptionDiagnosis diagnosis = new EPrescriptionDiagnosis();
        diagnosis.setCode((String) diagbean.get("icd_code"));
        diagnosis.setType((String) diagbean.get("diag_type"));
        diagnosis.setDiagnosis_type((String) diagbean.get("diagnosis_type"));
        prescription.addDiagnosis(diagnosis);
      }

      if (!requestType.equals("eRxCancellation")) {
        // Get prescribed medicines list.
        List<BasicDynaBean> prescMedList = prescriptionsService
            .getErxPrescribedActivities(pbmPrescId, healthAuthority);

        for (BasicDynaBean erxmed : prescMedList) {
          EPrescriptionActivity activity = new EPrescriptionActivity();
          List<BasicDynaBean> observations = null;
          BigDecimal refills = BigDecimal.ZERO;

          activity.setMedicineID(erxmed.get("medicine_id") != null
              ? (Integer) erxmed.get("medicine_id")
              : 0);
          activity.setActivityName((erxmed.get("medicine_name") != null
              && !((String) erxmed.get("medicine_name")).equals(""))
                  ? (String) erxmed.get("medicine_name")
                  : (String) erxmed.get("generic_name"));
          activity.setActivityCode((erxmed.get("item_code") != null
              && !((String) erxmed.get("item_code")).equals(""))
                  ? (String) erxmed.get("item_code")
                  : (String) erxmed.get("generic_code"));

          // TODO: Code and Code type for drugs when prescriptions by generics
          String haadCode = (erxmed != null && erxmed.get("haad_code") != null)
              ? erxmed.get("haad_code").toString()
              : null;
          activity.setActivityType(haadCode);

          activity.setActivityID((String) erxmed.get("activity_id"));
          activity.setActivityStart(
              (String) erxmed.get("activity_prescribed_date"));
          activity
              .setDuration(new BigDecimal((Integer) erxmed.get("duration")));
          activity.setInstructions((String) erxmed.get("medicine_remarks"));
          activity.setQuantity(
              new BigDecimal((Integer) erxmed.get("medicine_quantity")));
          activity.setRefills(refills);
          activity.setRouteOfAdminName((String) erxmed.get("route_name"));
          activity.setRouteOfAdminId((Integer) erxmed.get("route_id"));
          activity.setRoutOfAdmin(erxmed.get("route_code") != null
              ? (String) erxmed.get("route_code")
              : null);

          if (null != erxmed.get("granular_units")
              && erxmed.get("granular_units").equals("Y")) {
            String frequency = erxmed.get("frequency") != null
                ? (String) erxmed.get("frequency")
                : null;
            if (frequency != null && !frequency.trim().equals("")) {
              BasicDynaBean freqBean = medicineDosageService
                  .findByUniqueName(frequency, "dosage_name");

              if (null != freqBean) {
                // Set frequency details
                String freqType = freqBean.get("frequency_type") != null
                    ? (String) freqBean.get("frequency_type")
                    : ""; // per Hour, per Day, per Week
                BigDecimal freqValue = freqBean.get("frequency_value") != null
                    ? new BigDecimal((Integer) freqBean.get("frequency_value"))
                    : BigDecimal.ZERO;
                // BigDecimal freqUnit = freqBean.get("per_day_qty") != null ?
                // (BigDecimal)freqBean.get("per_day_qty") : BigDecimal.ONE;

                String strength = erxmed.get("strength") != null
                    ? (String) erxmed.get("strength")
                    : "";
                Scanner scanner = new Scanner(strength);
                BigDecimal freqUnit;
                if (scanner.hasNextBigDecimal()) {
                  freqUnit = scanner.nextBigDecimal();
                } else {
                  freqUnit = BigDecimal.ZERO;
                }
                // BigDecimal freqUnit = ERxPrescriptionDAO.getStrengthBigDecimal(strength);

                EPrescriptionActivity.Frequency actFreq =
                    new EPrescriptionActivity().new Frequency();
                actFreq.setType(freqType);
                actFreq.setValue(freqValue);
                actFreq.setUnit(freqUnit);
                actFreq.setValueType(frequency);
                activity.setFrequency(actFreq);
              }
            }
          }

          // If required by customer, can include observations bug # 40305
          /*
           * BasicDynaBean consChargeBean = DoctorConsultationDAO.getConsultationCharge(consId);
           * observations =
           * claimsubdao.findAllObservations((String)consChargeBean.get("charge_id"),centerId);
           * 
           * EPrescriptionActivityObservation actObs = null;
           * 
           * for (BasicDynaBean obs : observations) { actObs = new
           * EPrescriptionActivityObservation(); actObs.setCode((String)obs.get("code"));
           * actObs.setType((String)obs.get("type")); actObs.setValue((String)obs.get("value"));
           * actObs.setValueType((String)obs.get("value_type"));
           * 
           * activity.observations.add(actObs); }
           */

          prescription.addActivity(activity);
        }
      }
      erxPresc.setPrescription(prescription);

    } catch (Exception exception) {
      log.error("Error while creating eRx request : ", exception);
    }

    return erxPresc;
  }
}
