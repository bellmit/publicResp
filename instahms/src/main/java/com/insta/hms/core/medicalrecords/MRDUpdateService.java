package com.insta.hms.core.medicalrecords;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.billing.BillActivityChargeService;
import com.insta.hms.core.billing.BillChargeClaimService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.clinical.order.doctoritems.DoctorOrderItemService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.organization.OrganizationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class MRDUpdateService.
 *
 * @author sonam
 */
@Service
public class MRDUpdateService {

  /** The bill activity charge service. */
  @LazyAutowired
  private BillActivityChargeService billActivityChargeService;

  /** The bill charge service. */
  @LazyAutowired
  private BillChargeService billChargeService;

  /** The bill service. */
  @LazyAutowired
  private BillService billService;

  /** The doctor consultation service. */
  @LazyAutowired
  private DoctorConsultationService doctorConsultationService;

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /** The doctor service. */
  @LazyAutowired
  private DoctorService doctorService;

  /** The consultation types service. */
  @LazyAutowired
  private ConsultationTypesService consultationTypesService;

  /** The patient ins plans service. */
  @LazyAutowired
  private PatientInsurancePlansService patientInsPlansService;

  /** The organization service. */
  @LazyAutowired
  OrganizationService organizationService;

  /** The doctor order item service. */
  @LazyAutowired
  private DoctorOrderItemService doctorOrderItemService;

  /** The bill charge claim service. */
  @LazyAutowired
  private BillChargeClaimService billChargeClaimService;

  /** The Constant CHARGE_ID. */
  private static final String CHARGE_ID = "charge_id";

  /** The Constant CONSULTATION_TYPE_ID. */
  private static final String CONSULTATION_TYPE_ID = "consultation_type_id";

  /** The Constant ACT_PLAN_CODE. */
  private static final String ACT_PLAN_CODE = "act_rate_plan_item_code";

  /** The Constant BILL_NO. */
  private static final String BILL_NO = "bill_no";

  /**
   * Update doctor charges for code.
   *
   * @param consultationBean
   *          the consultation bean
   * @param visitId
   *          the visit id
   * @param codeType
   *          the code type
   * @param code
   *          the code
   * @param errorMap
   *          the error map
   */
  public void updateDoctorChargesForCode(BasicDynaBean consultationBean,
      String visitId, String codeType, String code,
      ValidationErrorMap errorMap) {
    String activityId = consultationBean.get("consultation_id").toString();
    String chargeId = billActivityChargeService.getChargeId("DOC", activityId);
    BasicDynaBean existingchargeBean = billChargeService.getCharge(chargeId);
    String status = (String) existingchargeBean.get("status");
    if (status.equals("X")) {
      errorMap.addError(CHARGE_ID, "exception.item.charge.cancelled");
    }
    String billNo = (String) existingchargeBean.get(BILL_NO);
    BasicDynaBean bill = billService.findByKey(billNo);
    String ratePlanId = (String) bill.get("bill_rate_plan_id");
    String billStatus = (String) bill.get("status");
    if (!billStatus.equals("A")) {
      errorMap.addError("bill_status", "exception.bill.status.not.open");
    }
    String visitType = (String) bill.get("visit_type");
    BasicDynaBean doctorConsBean = doctorConsultationService
        .findByKey((Integer) consultationBean.get("consultation_id"));
    BasicDynaBean registrationBean = registrationService
        .findByKey((String) doctorConsBean.get("patient_id"));
    String bedType = (String) registrationBean.get("bed_type");
    BasicDynaBean docMasterCharge = doctorService.getDoctorCharges(
        (String) doctorConsBean.get("doctor_name"), ratePlanId, bedType);
    Map<String, Object> key = new HashMap<>();
    key.put(CONSULTATION_TYPE_ID, consultationBean.get("head"));
    BasicDynaBean consTypeBean = consultationTypesService.findByPk(key);
    Boolean isInsurance = registrationBean.get("primary_sponsor_id") != null
        && !registrationBean.get("primary_sponsor_id").equals("");

    int[] planIds = patientInsPlansService
        .getPlanIds((String) doctorConsBean.get("patient_id"));
    BasicDynaBean orgDetails = organizationService
        .getOrgdetailsDynaBean(ratePlanId);

    BigDecimal charge = BigDecimal.ZERO;
    List<BasicDynaBean> drChargesList = doctorOrderItemService
        .getDoctorConsCharges(docMasterCharge, consTypeBean, orgDetails,
            BigDecimal.ONE, isInsurance, bedType, charge, false, false, false, visitType, 
            BigDecimal.ZERO);
    BasicDynaBean drCharge = drChargesList.get(0);
    drCharge.set(CHARGE_ID, chargeId);
    drCharge.set("code_type", codeType);
    drCharge.set(ACT_PLAN_CODE, code);
    drCharge.set("posted_date", existingchargeBean.get("posted_date"));
    drCharge.set("username", consultationBean.get("username"));
    drCharge.set(BILL_NO, existingchargeBean.get(BILL_NO));
    drCharge.set("prior_auth_id", existingchargeBean.get("prior_auth_id"));
    drCharge.set("prior_auth_mode_id",
        existingchargeBean.get("prior_auth_mode_id"));

    drChargesList.remove(0);
    drChargesList.add(drCharge);

    String newCode = (String) (drCharge.get(ACT_PLAN_CODE) == null ? ""
        : drCharge.get(ACT_PLAN_CODE));
    int newConTypeId = (Integer) drCharge.get(CONSULTATION_TYPE_ID);
    String existingCode = (String) (existingchargeBean
        .get(ACT_PLAN_CODE) == null ? ""
            : existingchargeBean.get(ACT_PLAN_CODE));
    int existingConTypeId = (int) existingchargeBean.get(CONSULTATION_TYPE_ID);
    if ((existingConTypeId != newConTypeId || !existingCode.equals(newCode))
        && billStatus.equals("A")) {
      billChargeService.updateChargeAmounts(drCharge);
      updateItemChargeCliamAmount(drChargesList, planIds, visitId);
      billActivityChargeService.updateChargeConsultationType(drCharge,
          codeType);
    }

  }

  /**
   * Update item charge cliam amount.
   *
   * @param drChargesList
   *          the dr charges list
   * @param planIds
   *          the plan ids
   * @param visitId
   *          the visit id
   */
  @SuppressWarnings("unchecked")
  public void updateItemChargeCliamAmount(List<BasicDynaBean> drChargesList,
      int[] planIds, String visitId) {

    if (planIds != null && planIds.length > 0) {
      for (int i = 0; i < planIds.length; i++) {
        List<BasicDynaBean> billChargeClaims = billService
            .getVisitBillChargeClaims(visitId, planIds[i], false,
                "'".concat(visitId).concat("'"));
        Map<String, List<BasicDynaBean>> billChargeClaimMap = null;
        billChargeClaimMap = ConversionUtils
            .listBeanToMapListBean(billChargeClaims, CHARGE_ID);
        billChargeClaimService.updateConsultationChargeClaims(drChargesList,
            billChargeClaimMap);
      }

    }
  }

}
