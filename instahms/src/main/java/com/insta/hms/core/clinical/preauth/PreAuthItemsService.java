package com.insta.hms.core.clinical.preauth;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.clinical.eauthorization.EAuthPrescriptionActivitiesRepository;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;
import com.insta.hms.eauthorization.EAuthApprovalsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Service
public class PreAuthItemsService {
  @LazyAutowired
  private EAuthPrescriptionActivitiesRepository prescriptionActivitiesRepo;

  @LazyAutowired
  private PatientRegistrationRepository patientRegRepo;

  @LazyAutowired
  private BillChargeService billChargeService;

  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  private static Logger logger = LoggerFactory.getLogger(PreAuthItemsService.class);

  public enum PreAuthItemType {

    DIAGNOSIS("DIA", "testPreAuthItems", "previous_preauth_test_items",
        "previous_test_prescriptions"), OPERATION("OPE", "operationsPreAuthItems",
            "previous_preauth_operation_items", "previous_operation_prescriptions"), SERVICE("SER",
                "servicePreAuthItems", "previous_preauth_service_items",
                "previous_service_prescriptions"), CONSULTATION("DOC", "consultationPreAuthItems",
                    "previous_preauth_consultation_items", "previous_consultation_prescriptions");

    private String preAuthItemTypeCode;
    private String responseMapKey;
    private String registrationResponseMapKey;
    private String pendingPrescResponseKey;

    private static Map<String, PreAuthItemType> preAuthItemCodeTypeMap =
        new HashMap<String, PreAuthItemType>();
    static {
      for (PreAuthItemType itemType : PreAuthItemType.values()) {
        preAuthItemCodeTypeMap.put(itemType.getItemTypeCode(), itemType);
      }
    }

    private PreAuthItemType(String preAuthItemTypeCode, String responseMapKey,
        String regResponseKey, String pendingPrescResponseKey) {
      this.preAuthItemTypeCode = preAuthItemTypeCode;
      this.responseMapKey = responseMapKey;
      this.registrationResponseMapKey = regResponseKey;
      this.pendingPrescResponseKey = pendingPrescResponseKey;
    }

    public String getItemTypeCode() {
      return this.preAuthItemTypeCode;
    }

    public static PreAuthItemType getItemTypeFromCode(String itemTypeCode) {
      return preAuthItemCodeTypeMap.get(itemTypeCode);
    }

    public String getResponseMapKey() {
      return responseMapKey;
    }

    public String getRegResponseMapKey() {
      return registrationResponseMapKey;
    }

    public String getPendingPrescResponseKey() {
      return pendingPrescResponseKey;
    }

    /**
     * Get init map.
     * 
     * @return map
     */
    public static Map<PreAuthItemType, List<BasicDynaBean>> getInitMap() {
      Map<PreAuthItemType, List<BasicDynaBean>> initMap = new HashMap<>();
      for (PreAuthItemType preAuthItemType : PreAuthItemType.values()) {
        initMap.put(preAuthItemType, new ArrayList<BasicDynaBean>());
      }
      return initMap;
    }

    /**
     * Convert pre auth item to string map.
     * 
     * @param <T> the map
     * @param preAuthItemTypeMap map
     * @return map
     */
    public static <T> Map<String, T> convertPreAuthItemTypeMapToStringMap(
        Map<PreAuthItemType, T> preAuthItemTypeMap) {
      Map<String, T> convertedMap = new HashMap<>();
      for (Map.Entry<PreAuthItemType, T> itemTypeBeanMap : preAuthItemTypeMap.entrySet()) {
        convertedMap.put(itemTypeBeanMap.getKey().getResponseMapKey(), itemTypeBeanMap.getValue());
      }
      return convertedMap;
    }
  }

  /**
   * Gets the all active pre auth approved items.
   *
   * @param visitId the visit id
   * @return the all active pre auth approved items
   */
  public Map<PreAuthItemType, List<BasicDynaBean>> getAllActivePreAuthApprovedItems(
      String visitId) {
    BasicDynaBean patientInfo = patientRegRepo.getPatientInfo(visitId);
    String mrNo = (String) patientInfo.get("mr_no");
    Integer centerId = (Integer) patientInfo.get("center_id");
    String primarySponsorId = (String) patientInfo.get("pri_sponsor_id");
    return getActivePreAuthApprovedItems(mrNo, centerId, primarySponsorId);
  }

  /**
   * Gets the visit active pre auth approved items.
   *
   * @param visitId the visit id
   * @return the visit active pre auth approved items
   */
  public Map<String, Map<PreAuthItemType, List<BasicDynaBean>>> getVisitActivePreAuthApprovedItems(
      String visitId) {
    BasicDynaBean patientInfo = patientRegRepo.getPatientInfo(visitId);
    String mrNo = (String) patientInfo.get("mr_no");
    Integer centerId = (Integer) patientInfo.get("center_id");
    String primarySponsorId = (String) patientInfo.get("pri_sponsor_id");
    return getVisitPreAuthApprovedItems(mrNo, centerId, primarySponsorId);
  }

  private static final String UPDATE_PREAUTH_ITEM_QUANTITY_BE_CHARGEID =
      "UPDATE preauth_prescription_activities SET rem_qty=rem_qty-(?) ";

  private static final String UPDATE_PREAUTH_ITEM_QUANTITY_BE_CHARGEID_WHERE_CLAUSE =
      "where preauth_act_id in " + " (select preauth_act_id from bill_charge where charge_id=?)";


  private static final String UPDATE_PREAUTH_ITEM_QUANTITY_BY_ID =
      "UPDATE " + " preauth_prescription_activities SET rem_qty=rem_qty-(?) ";

  private static final String UPDATE_PREAUTH_ITEM_QUANTITY_BY_ID_WHERE_CLAUSE =
      " WHERE preauth_act_id = ? ";

  private boolean isValidPreAuthQuantityUpdate(String chargeId, int itemQuantity) {
    return itemQuantity > 0 && StringUtils.isNotBlank(chargeId);
  }

  /**
   * Update pre auth item qty.
   * 
   * @param chargeId the string
   * @param itemQuantity the int
   * @param cancel boolean value
   * @param noPrescription boolean value
   * @param preAuthActStatus string
   */
  public void updatePreAuthItemQuantity(String chargeId, int itemQuantity, boolean cancel,
      boolean noPrescription, String preAuthActStatus) {
    boolean isValidUpdate = isValidPreAuthQuantityUpdate(chargeId, itemQuantity);
    if (isValidUpdate) {
      Object[] queryParams = null;
      int itemQuantityToUpdate = itemQuantity;
      String preauthUpdateQuery = "";
      String preauthApprovedQtyUpdate = "";
      if ("C".equals(preAuthActStatus)) {
        preauthApprovedQtyUpdate = ",rem_approved_qty=rem_approved_qty-(?)";
      }
      if (cancel) {
        itemQuantityToUpdate *= -1;
      } else {
        preauthUpdateQuery = " AND rem_qty > 0 ";
      }
      if (noPrescription) {
        preauthUpdateQuery += " AND patient_pres_id = 0 ";
      }
      if ("C".equals(preAuthActStatus)) {
        queryParams = new Object[] {itemQuantityToUpdate, itemQuantityToUpdate, chargeId};
        DatabaseHelper.update(
            UPDATE_PREAUTH_ITEM_QUANTITY_BE_CHARGEID + preauthApprovedQtyUpdate
                + UPDATE_PREAUTH_ITEM_QUANTITY_BE_CHARGEID_WHERE_CLAUSE + preauthUpdateQuery,
            queryParams);
      } else {
        queryParams = new Object[] {itemQuantityToUpdate, chargeId};
        DatabaseHelper.update(
            UPDATE_PREAUTH_ITEM_QUANTITY_BE_CHARGEID
                + UPDATE_PREAUTH_ITEM_QUANTITY_BE_CHARGEID_WHERE_CLAUSE + preauthUpdateQuery,
            queryParams);
      }
    }
  }

  /**
   * Update pre auth item qty.
   * 
   * @param preauthActId the int
   * @param itemQuantity the int
   * @param cancel boolean value
   * @param noPrescription boolean value
   * @param preAuthActStatus string
   */
  public void updatePreAuthItemQuantity(int preauthActId, int itemQuantity, boolean cancel,
      boolean noPrescription, String preAuthActStatus) {
    Object[] queryParams = null;
    String preauthUpdateQuery = "";
    String preauthApprovedQtyUpdate = "";
    if ("C".equals(preAuthActStatus)) {
      preauthApprovedQtyUpdate = ",rem_approved_qty=rem_approved_qty-(?)";
    }
    if (cancel) {
      itemQuantity *= -1;
    } else {
      preauthUpdateQuery = " AND rem_qty > 0";
    }
    if (noPrescription) {
      preauthUpdateQuery += " AND patient_pres_id = 0 ";
    }
    if ("C".equals(preAuthActStatus)) {
      queryParams = new Object[] {itemQuantity, itemQuantity, preauthActId};
      DatabaseHelper.update(UPDATE_PREAUTH_ITEM_QUANTITY_BY_ID + preauthApprovedQtyUpdate
          + UPDATE_PREAUTH_ITEM_QUANTITY_BY_ID_WHERE_CLAUSE + preauthUpdateQuery, queryParams);
    } else {
      queryParams = new Object[] {itemQuantity, preauthActId};
      DatabaseHelper.update(UPDATE_PREAUTH_ITEM_QUANTITY_BY_ID
          + UPDATE_PREAUTH_ITEM_QUANTITY_BY_ID_WHERE_CLAUSE + preauthUpdateQuery, queryParams);
    }
  }

  /**
   * Update pre auth item.
   * 
   * @param chargesList the list
   * @param isCancel the boolean
   * @param preAuthActStatus string
   */
  public void updatePreAuthItemQuantity(List<BasicDynaBean> chargesList, boolean isCancel,
      String preAuthActStatus) {
    for (BasicDynaBean charge : chargesList) {
      Integer quantity = ((BigDecimal) charge.get("act_quantity")).intValue();
      String chargeId = (String) charge.get("charge_id");
      Integer preAuthItemId = (Integer) charge.get("preauth_act_id");
      if (quantity == null || quantity <= 0 || StringUtils.isBlank(chargeId)
          || preAuthItemId == null) {
        continue;
      }
      if (isCancel) {
        updatePreAuthItemQuantity(chargeId, quantity, isCancel, true, preAuthActStatus);
      } else {
        updatePreAuthItemQuantity(preAuthItemId, quantity, isCancel, false, preAuthActStatus);
      }
    }
  }

  /**
   * If the patient with the visit has any preauth approved item for the particular center and TPA
   * conditions(optional,ignored if null)
   * Map&lt;PreAuthItemTypeCode,List&lt;PreAuthItem&gt;&gt;<br/>
   * <b>Empty Collection</b> otherwise.
   * 
   * @param mrNo the string
   * @param centerId the int
   * @param primarySponsorId the string
   */

  public Map<PreAuthItemType, List<BasicDynaBean>> getActivePreAuthApprovedItems(
      @Nonnull String mrNo, @Nullable Integer centerId, @Nullable String primarySponsorId) {
    List<BasicDynaBean> preAuthApprovedItems =
        prescriptionActivitiesRepo.getActivePreAuthApprovedItems(mrNo, centerId, primarySponsorId);
    Map<PreAuthItemType, List<BasicDynaBean>> preAuthApprovedItemMap = PreAuthItemType.getInitMap();
    if (CollectionUtils.isNotEmpty(preAuthApprovedItems)) {
      for (BasicDynaBean preAuthApprovedItem : preAuthApprovedItems) {
        PreAuthItemType preAuthItemType = PreAuthItemType
            .getItemTypeFromCode((String) preAuthApprovedItem.get("preauth_item_type"));
        if (preAuthItemType != null) {
          preAuthApprovedItemMap.get(preAuthItemType).add(preAuthApprovedItem);
        }
      }
    }
    return preAuthApprovedItemMap;
  }

  /**
   * Gets the visit pre auth approved items.
   *
   * @param mrNo the mr no
   * @param centerId the center id
   * @param primarySponsorId the primary sponsor id
   * @return the visit pre auth approved items
   */
  public Map<String, Map<PreAuthItemType, List<BasicDynaBean>>> getVisitPreAuthApprovedItems(
      @Nonnull String mrNo, @Nullable Integer centerId, @Nullable String primarySponsorId) {
    List<BasicDynaBean> preAuthApprovedItems =
        prescriptionActivitiesRepo.getActivePreAuthApprovedItems(mrNo, centerId, primarySponsorId);
    Map<String, Map<PreAuthItemType, List<BasicDynaBean>>> returnMap = new HashMap<>();
    Map<PreAuthItemType, List<BasicDynaBean>> preAuthApprovedItemMap = PreAuthItemType.getInitMap();
    if (CollectionUtils.isNotEmpty(preAuthApprovedItems)) {
      for (BasicDynaBean preAuthApprovedItem : preAuthApprovedItems) {
        PreAuthItemType preAuthItemType = PreAuthItemType
            .getItemTypeFromCode((String) preAuthApprovedItem.get("preauth_item_type"));
        if (preAuthItemType != null) {
          preAuthApprovedItemMap.get(preAuthItemType).add(preAuthApprovedItem);
        }
        returnMap.put(preAuthApprovedItem.get("patient_id").toString(), preAuthApprovedItemMap);
      }
    }
    return returnMap;
  }

  /**
   * Is valid pre auth item qty.
   * 
   * @param preAuthItemIdQuantityMap the map
   * @return boolean value
   */
  public boolean isValidPreAuthItemQuantities(Map<Integer, Integer> preAuthItemIdQuantityMap) {
    boolean isValid = true;
    if (MapUtils.isNotEmpty(preAuthItemIdQuantityMap)) {
      Map<String, Object> preAuthItemIdFilter = new HashMap<>();
      preAuthItemIdFilter.put("preauth_act_id", new ArrayList<>(preAuthItemIdQuantityMap.keySet()));
      List<BasicDynaBean> preAuthItems =
          prescriptionActivitiesRepo.findByCriteria(preAuthItemIdFilter);
      for (BasicDynaBean preAuthItem : preAuthItems) {
        Integer remainingQty = (Integer) preAuthItem.get("rem_qty");
        Integer orderedQty =
            preAuthItemIdQuantityMap.get((Integer) preAuthItem.get("preauth_act_id"));
        if (remainingQty != null && orderedQty != null && remainingQty < orderedQty) {
          isValid = false;
          break;
        }
      }
    }
    return isValid;

  }

  /**
   * Get prior auth item.
   * 
   * @param docPrescId the int
   * @return int value
   */
  public Integer getPriorAuthItemIdFromPrescId(Integer docPrescId) {
    if (docPrescId != null) {
      BasicDynaBean preAuthActivity =
          prescriptionActivitiesRepo.findByKey("patient_pres_id", docPrescId);
      if (preAuthActivity != null) {
        return (Integer) preAuthActivity.get("preauth_act_id");
      }
    }
    return null;
  }

  /**
   * is valid presc item.
   * 
   * @param prescIdQuantityMap the map
   * @return boolean value
   */
  public boolean isValidPrescriptionItems(Map<Integer, Integer> prescIdQuantityMap) {
    boolean isValid = true;
    if (MapUtils.isNotEmpty(prescIdQuantityMap)) {
      Map<String, Object> patPrescIdFilter = new HashMap<>();
      patPrescIdFilter.put("patient_pres_id", new ArrayList<>(prescIdQuantityMap.keySet()));
      List<BasicDynaBean> preAuthItems =
          prescriptionActivitiesRepo.findByCriteria(patPrescIdFilter);
      for (BasicDynaBean preAuthItem : preAuthItems) {
        Integer remainingQty = (Integer) preAuthItem.get("rem_qty");
        Integer orderedQty = prescIdQuantityMap.get((Integer) preAuthItem.get("patient_pres_id"));
        if (remainingQty != null && orderedQty != null && remainingQty < orderedQty) {
          isValid = false;
          break;
        }
      }
    }
    return isValid;
  }

  /**
   * Is doc presc for pre auth.
   * 
   * @param docPrescId the int
   * @return boolean value
   */
  public boolean isDoctorPrescribedForPreAuth(Integer docPrescId) {
    if (docPrescId != null && docPrescId != 0) {
      Map<String, Object> filterMap = new HashMap<>();
      filterMap.put("patient_pres_id", docPrescId);
      filterMap.put("preauth_required", 'Y');
      return prescriptionActivitiesRepo.findByKey(filterMap) != null;
    }
    return false;
  }

  /**
   * Filter presc.
   * 
   * @param pendingPrescriptions the lsit
   * @return list of beans
   */
  public List<BasicDynaBean> filterPrescriptions(List<BasicDynaBean> pendingPrescriptions) {
    if (CollectionUtils.isNotEmpty(pendingPrescriptions)) {
      pendingPrescriptions = filterByEAuthApproval(pendingPrescriptions);
      filterPrescriptionsByCenter(pendingPrescriptions);
    }
    return pendingPrescriptions;
  }

  /**
   * Filters prescriptions based on prescription validity and pre-auth validity Retains prescription
   * when the prescription has not expired OR prescription's pre-auth is not expired.
   * 
   * @param pendingPrescriptions the list
   * @return list of filtered pending prescriptions
   */
  public List<BasicDynaBean> filterByValidity(List<BasicDynaBean> pendingPrescriptions) {
    List<BasicDynaBean> filteredPrescriptions = new ArrayList<>();
    filterPrescriptions(pendingPrescriptions);
    BigDecimal prescriptionValidity =
        (BigDecimal) genericPreferencesService.getAllPreferences().get("prescription_validity");
    for (BasicDynaBean prescription : pendingPrescriptions) {
      Date preauthEndDate = (Date) prescription.get("preauth_end_date");
      Date prescribedDate = (Date) prescription.get("prescribed_date");
      if (prescriptionValidity == null) {
        prescriptionValidity = BigDecimal.valueOf(365);
      }
      Date expiryDate = DateUtils.addDays(prescribedDate, prescriptionValidity.intValue());
      Date today = new Date();
      if (expiryDate.after(today) || (preauthEndDate != null && preauthEndDate.after(today))
          || prescriptionValidity.compareTo(BigDecimal.ZERO) == 0) {
        filteredPrescriptions.add(prescription);
      }
    }
    return filteredPrescriptions;
  }

  private void filterPrescriptionsByCenter(List<BasicDynaBean> pendingPrescriptions) {
    for (BasicDynaBean pendingPrescription : pendingPrescriptions) {
      boolean isValid = false;
      Integer centerId = (Integer) pendingPrescription.get("center_id");
      Integer currentCenterId = (Integer) sessionService.getSessionAttributes().get("centerId");
      if (centerId != null && currentCenterId != null
          && centerId.intValue() != currentCenterId.intValue()) {
        resetPreAuthDetails(pendingPrescription);
      }
    }
  }

  private List<BasicDynaBean> filterByEAuthApproval(List<BasicDynaBean> pendingPrescriptions) {
    List<BasicDynaBean> resultPrescriptions = new ArrayList<>();
    for (BasicDynaBean pendingPrescription : pendingPrescriptions) {
      Integer preAuthPrescId = (Integer) pendingPrescription.get("preauth_presc_id");
      if (preAuthPrescId != null) {
        BasicDynaBean approvalDetails = null;
        try {
          approvalDetails = new EAuthApprovalsDAO().getApprovalBean(preAuthPrescId);
        } catch (SQLException exception) {
          logger.error("Error while getting approval bean ", exception);
        }
        if (approvalDetails != null) {
          Timestamp endDate = (Timestamp) approvalDetails.get("end_date");
          Timestamp currentTime = new Timestamp(System.currentTimeMillis());
          if (endDate != null && currentTime.compareTo(endDate) > 0) {
            resetPreAuthDetails(pendingPrescription);
          }
        }
        Integer preAuthRemainingQuantity = (Integer) pendingPrescription.get("preauth_rem_qty");
        if (preAuthRemainingQuantity != null && preAuthRemainingQuantity < 1) {
          continue;
        }
      } else {
        Integer prescId = (Integer) pendingPrescription.get("pres_id");
        if (prescId != null) {
          BasicDynaBean preauthItem =
              prescriptionActivitiesRepo.findByKey("patient_pres_id", prescId);
          if (preauthItem != null) {
            Integer preAuthRemainingQuantity = (Integer) preauthItem.get("rem_qty");
            if (preAuthRemainingQuantity != null && preAuthRemainingQuantity < 1) {
              continue;
            }
          }
        }
      }
      resultPrescriptions.add(pendingPrescription);
    }
    return resultPrescriptions;
  }

  private void resetPreAuthDetails(BasicDynaBean pendingPrescription) {
    pendingPrescription.set("preauth_number", null);
    pendingPrescription.set("preauth_mode", null);
  }

  /**
   * Determine preauth status of charge for cancellation.
   *
   * @param chargeId the charge id
   * @param preauthActId the preauth act id
   * @return "C" for Approved and "D" for denied.
   */
  public String determinePreauthStatusOfChargeForCancellation(String chargeId, int preauthActId) {
    BasicDynaBean preauthActivity =
        prescriptionActivitiesRepo.findByKey("preauth_act_id", preauthActId);
    if ("O".equals(preauthActivity.get("preauth_act_status"))) {
      return "O";
    }
    BasicDynaBean charge = billChargeService.findByKey("charge_id", chargeId);
    BigDecimal claimAmt = (BigDecimal) charge.get("insurance_claim_amount");
    if (BigDecimal.ZERO.compareTo(claimAmt) != 0) {
      return "C";
    }
    Integer actQty = (Integer) preauthActivity.get("act_qty");
    Integer approvedQty = (Integer) preauthActivity.get("approved_qty");
    if (actQty.equals(approvedQty)) {
      return "C";
    }
    return "D";
  }

  /**
   * Determine pre auth status for presc.
   * 
   * @param itemsMap the map
   * @return string
   */
  public String determinePreauthStatusForPrescribedItem(Map<String, Object> itemsMap) {
    if ("Y".equals(itemsMap.get("send_for_prior_auth"))) {
      BigDecimal insuranceClaimAmount =
          new BigDecimal(((Number)itemsMap.get("insurance_claim_amount")).doubleValue());
      if (null == itemsMap.get("preauth_act_id") || 0 == (Integer) itemsMap.get("preauth_act_id")
          || "O".equals(itemsMap.get("preauth_act_status"))) {
        return "O";
      } else if ((Integer) itemsMap.get("preauth_act_id") > 0
          && BigDecimal.ZERO.compareTo(insuranceClaimAmount) != 0) {
        return "C";
      } else if ((Integer) itemsMap.get("preauth_act_id") > 0
          && BigDecimal.ZERO.compareTo(insuranceClaimAmount) == 0) {
        return "D";
      }
    }
    return null;
  }
}
