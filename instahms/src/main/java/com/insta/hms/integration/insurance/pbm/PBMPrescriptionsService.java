package com.insta.hms.integration.insurance.pbm;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.BusinessService;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.clinical.consultation.prescriptions.MedicineItemService;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PBMPrescriptionsService.
 */
@Service
public class PBMPrescriptionsService extends BusinessService {

  /** The log. */
  Logger log = LoggerFactory.getLogger(PBMPrescriptionsService.class);

  /** The pbm prescriptions repo. */
  @LazyAutowired
  private PBMPrescriptionsRepository pbmPrescriptionsRepo;

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /** The insurance company service. */
  @LazyAutowired
  private InsuranceCompanyService insuranceCompanyService;

  /** The prescriptions service. */
  @LazyAutowired
  private PrescriptionsService prescriptionsService;

  /** The medicine item service. */
  @LazyAutowired
  private MedicineItemService medicineItemService;

  /**
   * This method creates PBM entry for consultation.
   *
   * @param consId the Consultation Id
   * @return the PBM record Bean
   */
  public BasicDynaBean insert(Object consId) {
    BasicDynaBean bean = pbmPrescriptionsRepo.getBean();
    bean.set("pbm_presc_id", pbmPrescriptionsRepo.getNextSequence());
    bean.set((consId instanceof String) ? "erx_visit_id" : "erx_consultation_id", consId);
    return pbmPrescriptionsRepo.insert(bean) == 1 ? bean : null;
  }

  /**
   * This method updates the PBM entry.
   *
   * @param fields fields to be updated
   * @param keys keys for filtering the entries to update
   * @return success
   */
  public boolean update(Map<String, Object> fields, Map<String, Object> keys) {
    BasicDynaBean bean = pbmPrescriptionsRepo.getBean();
    ConversionUtils.copyToDynaBean(fields, bean);
    return pbmPrescriptionsRepo.update(bean, keys) == 1;
  }

  /**
   * Gets the cons erx details.
   *
   * @param pbmPrescId the pbm presc id
   * @return the cons erx details
   */
  public BasicDynaBean getConsErxDetails(int pbmPrescId) {
    return pbmPrescriptionsRepo.getConsErxDetails(pbmPrescId);
  }

  /**
   * Find by key.
   *
   * @param filterMap the filter map
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(Map<String, Object> filterMap) {
    return pbmPrescriptionsRepo.findByKey(filterMap);
  }

  /**
   * Saves the ERx Request Details.
   *
   * @param pbmPrescId PBM Prescription ID
   * @param userid User ID
   * @param requestType ERx Request Type
   * @param userAction User-Action
   * @param centerId Center ID
   * @param patientId Patient ID
   * @return If Update affected any rows.
   */
  public boolean saveErxRequestDetails(int pbmPrescId, String userid, String requestType,
      String userAction, Integer centerId, String patientId) {
    BasicDynaBean genPrefs = genericPreferencesService.getAllPreferences();
    BasicDynaBean pbmPresBean = pbmPrescriptionsRepo.findByKey("pbm_presc_id", pbmPrescId);

    Timestamp erxRequestDate = DateUtil.getCurrentTimestamp();
    if ((userAction != null && userAction.equals("View"))
        && null != pbmPresBean.get("erx_request_date")) {
      erxRequestDate = (Timestamp) pbmPresBean.get("erx_request_date");
    }
    String erxRequestType = requestType;
    if ((userAction != null && userAction.equals("View"))
        && null != pbmPresBean.get("erx_request_type")) {
      erxRequestType = (String) pbmPresBean.get("erx_request_type");
    }

    Integer erxCenterId = centerId;
    if (null != pbmPresBean.get("erx_center_id")) {
      erxCenterId = (Integer) pbmPresBean.get("erx_center_id");
    }

    String facilityId = null;

    BasicDynaBean centerbean = centerService.findByKey(centerId);
    if (erxCenterId != null && erxCenterId != 0) {
      facilityId = centerbean.get("hospital_center_service_reg_no") != null
          ? (String) centerbean.get("hospital_center_service_reg_no")
          : "";
    } else {
      facilityId = genPrefs.get("hospital_service_regn_no") != null
          ? (String) genPrefs.get("hospital_service_regn_no")
          : "";
    }

    String payerId = null;

    String healthAuthority = (String) centerbean.get("health_authority");
    String primaryCompanyId =
        (String) registrationService.findByKey(patientId).get("primary_insurance_co");
    BasicDynaBean insubean =
        insuranceCompanyService.getInsuranceCompanyCode(healthAuthority, primaryCompanyId);
    if (null != insubean) {
      payerId =
          insubean.get("insurance_co_code") != null ? (String) insubean.get("insurance_co_code")
              : "@" + (String) insubean.get("insurance_co_name");
    }
    String erxPrescId = null;

    // Generate ERx Presc. ID
    String timeFormatStr =
        DataBaseUtil.getStringValueFromDb("SELECT to_char(now(), 'yyyymmddhh24miss')");
    if (payerId == null || payerId.isEmpty()) {
      erxPrescId = facilityId + "-Selfpay-" + timeFormatStr;
    } else {
      erxPrescId = facilityId + "-" + payerId + "-" + timeFormatStr;
    }

    pbmPresBean.set("erx_request_type", erxRequestType);
    pbmPresBean.set("erx_request_date", erxRequestDate);
    pbmPresBean.set("erx_center_id", erxCenterId);

    // File Name Format Example :
    // FACILITYID-PAYERID-UniqueNumber-PBMPRESCID.xml :
    // DHA-F-0046895-INS017-20130212172328-12.xml
    String fileName = erxPrescId + "-" + pbmPrescId + ".xml";
    if (!StringUtils.equals(requestType, "eRxCancellation") && (
        StringUtils.isEmpty((String) pbmPresBean.get("erx_reference_no")) || StringUtils
            .isEmpty((String) pbmPresBean.get("erx_presc_id")))) {
      pbmPresBean.set("erx_file_name", fileName);
      pbmPresBean.set("erx_presc_id", erxPrescId);
      pbmPresBean.set("erx_request_by", userid);
      // pbmPresBean.set("erx_auth_id_payer", "");
    }

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("pbm_presc_id", pbmPrescId);
    return pbmPrescriptionsRepo.update(pbmPresBean, keys) > 0;
  }

  /**
   * Save response.
   *
   * @param resultObj the result obj
   * @return true, if successful
   */
  public boolean saveResponse(Map<String, Object> resultObj) {
    BasicDynaBean pbmPrescBean = pbmPrescriptionsRepo.getBean();
    pbmPrescBean.set("pbm_presc_id", resultObj.get("pbm_presc_id"));
    String erxReferenceNo = (null != resultObj && null != resultObj.get("eRxReferenceNo"))
        ? resultObj.get("eRxReferenceNo").toString()
        : "";
    pbmPrescBean.set("erx_reference_no", erxReferenceNo);

    log.debug("Saving ERx Response params: Presc. Id: " + resultObj.get("pbm_presc_id")
        + " eRxReferenceNo: " + resultObj.get("eRxReferenceNo"));
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("pbm_presc_id", pbmPrescBean.get("pbm_presc_id"));
    return pbmPrescriptionsRepo.update(pbmPrescBean, keys) > 0;
  }

  /**
   * Save cancel response.
   *
   * @param consId the cons id
   * @param pbmPrescId the pbm presc id
   * @return true, if successful
   */
  public boolean saveCancelResponse(Object consId, int pbmPrescId) {
    BasicDynaBean pbmPrescBean = pbmPrescriptionsRepo.getBean();
    pbmPrescBean.set("pbm_presc_id", pbmPrescId);
    pbmPrescBean.set("status", "C");// Close PBM
    pbmPrescBean.set("erx_reference_no", null);
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("pbm_presc_id", pbmPrescBean.get("pbm_presc_id"));
    boolean success = pbmPrescriptionsRepo.update(pbmPrescBean, keys) > 0;

    if (success) {
      success &= medicineItemService.deAttachPbmFromERX(consId);
    }
    return success;
  }

  /**
   * Gets the latest cons erx bean.
   *
   * @param consId the cons id
   * @return the latest cons erx bean
   */
  public BasicDynaBean getLatestConsErxBean(Object consId) {
    return pbmPrescriptionsRepo.getLatestConsErxBean(consId);
  }

  /**
   * Gets the mr nos by PBM prescription id.
   *
   * @param pbmPrescIds the pbm presc ids
   * @return the mr nos by PBM prescription id
   */
  public List<String> getMrNosByPBMPrescriptionId(List<String> pbmPrescIds) {

    List<Integer> prescIdList = new ArrayList<>();
    for (String prescId : pbmPrescIds) {
      prescIdList.add(Integer.valueOf(prescId));
    }

    List<BasicDynaBean> pbmBeanList = pbmPrescriptionsRepo.getMrNosByPBMPrescriptionId(prescIdList);
    List<String> mrNoList = new ArrayList<>();
    for (BasicDynaBean pbmBean : pbmBeanList) {
      if (StringUtils.isNotBlank((String) pbmBean.get("mr_no"))) {
        mrNoList.add((String) pbmBean.get("mr_no"));
      }
    }
    return mrNoList;
  }

  /**
   * Checks if is PBM presc id valid.
   *
   * @param pbmPrescId the pbm presc id
   * @return the boolean
   */
  public Boolean isPBMPrescIdValid(String pbmPrescId) {
    Integer integerPbmPrescId;
    try {
      integerPbmPrescId = Integer.parseInt(pbmPrescId);
    } catch (NumberFormatException exception) {
      log.error("Unable to parse:" + pbmPrescId, exception);
      return false;
    }
    return pbmPrescriptionsRepo.exist("pbm_presc_id", integerPbmPrescId);
  }
}
