package com.insta.hms.core.clinical.order.master;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Return the rate plan and bill no to which order item need to saved.
 * <p>
 * If cash patient, then rate plan would be visit rate plan and bill would be oldest bill. If no
 * bill is there then "new" bill will be selected for cash patient.
 * 
 * If item is rate plan independent return the first bill if no bill is there return newInsurance in
 * case of insurance otherwise new.
 * 
 * If Insurance Patient, first check item item is covered as part of visit rate plan. if yes, then
 * return the bill based on order specified below
 * 
 * if not covered by visit rate plan, then return the cash bill based on order specified otherwise
 * new.
 * 
 * All bills are sorted like: first preference is given to insurance Bill. secondly bill later bill
 * third oldest bill first.
 * 
 * order be like: tpa(true) -> BL, BN, tpa(false) -> BL, BN , newInsurance, new
 * 
 * Special case: Doctor, Doctor is independent of rateplan but consultation type depends on rate
 * plan. So while ordering doctor, we consider rateplan list of consultation types.
 * </p>
 */
@Component
public class OrderBillSelection {

  /** The Constant ORG_ID. */
  private static final String ORG_ID = "org_id";

  /** The Constant BILL_NO. */
  private static final String BILL_NO = "bill_no";

  /** The Constant NON_RATE_PLAN_ITEM. */
  private static final String NON_RATE_PLAN_ITEM = "non_rate_plan_item";

  /** The Constant PACKAGE_ID. */
  private static final String PACKAGE_ID = "package_id";

  /** The Constant NEW_INSURANCE. */
  private static final String NEW_INSURANCE = "newInsurance";

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The center pref service. */
  @LazyAutowired
  private CenterPreferencesService centerPrefService;

  /** The patient registration repository. */
  @LazyAutowired
  private PatientRegistrationRepository patientRegistrationRepository;

  /** The order service. */
  @LazyAutowired
  private OrderService orderService;

  /** The order repository. */
  @LazyAutowired
  private OrderRepository orderRepository;

  /** The order repository. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /**
   * Bill selection rate plan.
   *
   * @param params the params
   * @return the map
   * @throws ParseException the ParseException
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> billSelectionRatePlan(Map<String, Object> params)
      throws ParseException {


    String itemId = params.get("id") instanceof Integer
        ? String.valueOf(params.get("id")) : (String) params.get("id");

    String prescribedDate = (String) params.get("prescribed_date_date");
    String prescribedTime = (String) params.get("prescribed_date_time");
    DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    Date orderDateTime = formatter.parse(prescribedDate + " " + prescribedTime + ":59");
    // The bill creation time is in format dd-MM-yyyy HH:mm:ss
    // While order time is in dd-MM-yyyy HH:mm

    Integer patPackageId = -1;
    Map<String, Object> mvpContent =
        (Map<String, Object>) params.get("mvp_package_content");
    if (null != mvpContent && mvpContent.get("patient_package_id") != null
        && !mvpContent.get("patient_package_id").equals("")) {
      patPackageId = (Integer) mvpContent.get("patient_package_id");
    }

    List<Integer> planIdsStrList = (List<Integer>) params.get("plan_ids");
    boolean isInsurance = null != planIdsStrList && !planIdsStrList.isEmpty();
    Integer planId = isInsurance ? planIdsStrList.get(0) : 0;
    Boolean isDoctorExcluded = null;
    if (null != params.get("item_excluded_from_doctor")) {
      if (params.get("item_excluded_from_doctor").equals("Y")) {
        isDoctorExcluded = true;
      } else if (params.get("item_excluded_from_doctor").equals("N")) {
        isDoctorExcluded = false;
      }
    }
    String tpaId = isInsurance ? (String) params.get("tpa_id") : "0";
    String type = (String) params.get("type");
    boolean isMultiVisitPackage = (boolean) params.get("multi_visit_package");
    if (type.equals("Package") && !isMultiVisitPackage) {
      Integer packageId = (null != params.get("id")) 
          ? Integer.parseInt(params.get("id").toString()) : 0 ;
      boolean packApplicability = orderService.getPackageApplicability(packageId,
          tpaId, planId);
      isInsurance = isInsurance && packApplicability ? true : false;
    }

    String patientId = (String) params.get("patient_id");
    String visitType = (String) params.get("visit_type");
    Boolean isMultiVisitPaackage = (Boolean) params.get("multi_visit_package");

    List<BasicDynaBean> billDetails = null;
    if (!isMultiVisitPaackage) {
      billDetails = orderService.getUnpaidBillsForVisit(patientId, visitType, null, "N",
          orderDateTime);
    } else {
      billDetails = orderService.getMvpUnpaidBillsForVisit(patientId, patPackageId, orderDateTime);
    }
    if (billDetails != null) {
      Collections.sort(billDetails, new OrderBillSelectionComparator());
    }

    Map<String, Object> ratePlanForItem = null;
    List<String> entity = (List<String>) params.get("entity");
    if (entity != null && entity.get(0).equals("Doctor")) {
      List<String> consultationOrgIds = (List<String>) params.get("consultation_org_ids");
      ratePlanForItem = new HashMap<>();
      ratePlanForItem.put(NON_RATE_PLAN_ITEM, "N");
      ratePlanForItem.put("rate_plan", consultationOrgIds);
    } else {
      ratePlanForItem = getRatePlanListMap(entity, itemId);

    }

    String visitRatePlan = (String) (patientRegistrationRepository.getVisitRatePlan(patientId,
        visitType)).get(ORG_ID);

    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");

    String nonInsuranceRatePlan = (String) (centerPrefService.getCenterPreferences(centerId))
        .get("pref_rate_plan_for_non_insured_bill");

    Map exsistingBillDetails = new HashMap();

    Map<String, Object> selectedBillRatePlanMap = new HashMap<>();

    getPreviousBillDetails(selectedBillRatePlanMap, nonInsuranceRatePlan, visitRatePlan,
        billDetails, ratePlanForItem, exsistingBillDetails);
    
    boolean allowBillNowInsurance = "Y".equals(
        genericPreferencesService.getPreferences().get("allow_bill_now_insurance"));
    
    if (!isInsurance || ratePlanForItem.get(NON_RATE_PLAN_ITEM).equals("Y")) {
      selectedBillRatePlanMap.put(ORG_ID, visitRatePlan);
      if ((billDetails == null || billDetails.isEmpty()) && !isInsurance) {
        selectedBillRatePlanMap.put(BILL_NO, "new");
        selectedBillRatePlanMap.put("is_tpa", false);
      } else if ((billDetails == null || billDetails.isEmpty()) && isInsurance) {
        if (allowBillNowInsurance) {
          selectedBillRatePlanMap.put(BILL_NO, NEW_INSURANCE);
          selectedBillRatePlanMap.put("is_tpa", true);
        } else {
          selectedBillRatePlanMap.put(BILL_NO, "new");
          selectedBillRatePlanMap.put("is_tpa", false);
        }
      } else {
        if (!isInsurance) {
          if (isMultiVisitPaackage) {
            for (BasicDynaBean bill : billDetails) {
              if (params.get("package_id") != null && !(Boolean) bill.get("is_tpa")
                  && params.get("package_id").equals(bill.get("package_id"))) {
                selectedBillRatePlanMap.put(BILL_NO, bill.get(BILL_NO));
                selectedBillRatePlanMap.put("is_tpa", bill.get("is_tpa"));
                return selectedBillRatePlanMap;
              }
            }
          }
          for (BasicDynaBean bill : billDetails) {
            if (!(Boolean) bill.get("is_tpa")) {
              selectedBillRatePlanMap.put(BILL_NO, bill.get(BILL_NO));
              selectedBillRatePlanMap.put("is_tpa", (Boolean) bill.get("is_tpa"));
              return selectedBillRatePlanMap;
            } else {
              selectedBillRatePlanMap.put(BILL_NO, "new");
              selectedBillRatePlanMap.put("is_tpa", (Boolean) billDetails.get(0).get("is_tpa"));
            }
          }
        } else {
          for (BasicDynaBean bill : billDetails) {
            if ((Boolean) bill.get("is_tpa")) {
              selectedBillRatePlanMap.put(BILL_NO, bill.get(BILL_NO));
              selectedBillRatePlanMap.put("is_tpa", (Boolean) bill.get("is_tpa"));
              return selectedBillRatePlanMap;
            }
          }
          if (allowBillNowInsurance) {
            selectedBillRatePlanMap.put(BILL_NO, NEW_INSURANCE);
            selectedBillRatePlanMap.put("is_tpa", true);
          } else {
            selectedBillRatePlanMap.put(BILL_NO, "new");
            selectedBillRatePlanMap.put("is_tpa", false);
          }
        }
      }
      return selectedBillRatePlanMap;
    }
    
    boolean isVisitRatePlanApplicable = ((List<String>) ratePlanForItem.get("rate_plan"))
        .contains(visitRatePlan);

    boolean isCenterRatePlanApplicable = ((List<String>) ratePlanForItem.get("rate_plan"))
        .contains(nonInsuranceRatePlan);

    if (isVisitRatePlanApplicable) {
      selectedBillRatePlanMap.put(ORG_ID, visitRatePlan);
      if (billDetails == null || billDetails.isEmpty()) {
        if (allowBillNowInsurance) {
          if (isDoctorExcluded == null || !isDoctorExcluded) {
            selectedBillRatePlanMap.put(BILL_NO, NEW_INSURANCE);
            selectedBillRatePlanMap.put("is_tpa", true);
          } else {
            selectedBillRatePlanMap.put(BILL_NO, "new");
            selectedBillRatePlanMap.put("is_tpa", true);
          }

        } else {
          if (isCenterRatePlanApplicable) {
            selectedBillRatePlanMap.put(BILL_NO, "new");
            selectedBillRatePlanMap.put("is_tpa", false);
          } else {
            throw new ValidationException("exception.new.bill.right.denied");
          }
        }
        return selectedBillRatePlanMap;
      } else {
        if (isMultiVisitPaackage) {
          for (BasicDynaBean bill : billDetails) {
            if (bill.get("bill_rate_plan_id").equals(visitRatePlan)
                && params.get("package_id") != null) {
              if (((Boolean) bill.get("is_tpa") && (isDoctorExcluded == null || !isDoctorExcluded))
                  && ((List<String>) ratePlanForItem.get("rate_plan")).contains(visitRatePlan)) {
                if (params.get("package_id").equals(bill.get("package_id"))) {
                  selectedBillRatePlanMap.put(BILL_NO, bill.get(BILL_NO));

                  selectedBillRatePlanMap.put("is_tpa", bill.get("is_tpa"));
                  return selectedBillRatePlanMap;
                }
              }
            }
          }
        }
        for (BasicDynaBean bill : billDetails) {
          if (bill.get("bill_rate_plan_id").equals(visitRatePlan)) {

            if (((Boolean) bill.get("is_tpa") && (isDoctorExcluded == null || !isDoctorExcluded))
                && ((List<String>) ratePlanForItem.get("rate_plan")).contains(visitRatePlan)) {
              selectedBillRatePlanMap.put(BILL_NO, bill.get(BILL_NO));

              selectedBillRatePlanMap.put("is_tpa", bill.get("is_tpa"));
              return selectedBillRatePlanMap;
            }
          }
        }
        if (allowBillNowInsurance && (isDoctorExcluded == null || !isDoctorExcluded)) {
          selectedBillRatePlanMap.put(BILL_NO, NEW_INSURANCE);
          selectedBillRatePlanMap.put("is_tpa", true);
        } else {
          if (isCenterRatePlanApplicable) {
            for (BasicDynaBean bill : billDetails) {
              if (!((Boolean) bill.get("is_tpa"))
                  && bill.get("bill_rate_plan_id").equals(nonInsuranceRatePlan)) {
                selectedBillRatePlanMap.put(BILL_NO, bill.get(BILL_NO));
                selectedBillRatePlanMap.put("is_tpa", (Boolean) bill.get("is_tpa"));
                return selectedBillRatePlanMap;
              }
            }
            selectedBillRatePlanMap.put(BILL_NO, "new");
            selectedBillRatePlanMap.put("is_tpa", false);
          } else {
            throw new ValidationException("exception.new.bill.right.denied");
          }
        }
        return selectedBillRatePlanMap;
      }
    } else {
      selectedBillRatePlanMap.put(ORG_ID, nonInsuranceRatePlan);
      if (billDetails == null || billDetails.isEmpty()) {
        selectedBillRatePlanMap.put(BILL_NO, "new");
        selectedBillRatePlanMap.put("is_tpa", false);
        return selectedBillRatePlanMap;
      } else {
        for (BasicDynaBean bill : billDetails) {
          if (!((Boolean) bill.get("is_tpa"))
              && bill.get("bill_rate_plan_id").equals(nonInsuranceRatePlan)) {
            selectedBillRatePlanMap.put(BILL_NO, bill.get(BILL_NO));
            selectedBillRatePlanMap.put("is_tpa", (Boolean) bill.get("is_tpa"));
            return selectedBillRatePlanMap;
          }
        }
        selectedBillRatePlanMap.put(BILL_NO, "new");
        selectedBillRatePlanMap.put("is_tpa", false);
        return selectedBillRatePlanMap;
      }
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void getPreviousBillDetails(Map<String, Object> selectedBillRatePlanMap,
      String nonInsuranceRatePlan, String visitRatePlan, List<BasicDynaBean> billDetails,
      Map<String, Object> ratePlanForItem, Map exsistingBillDetails) {
    /**
     * If User does not have access right to create new bills below logic is followed to select a
     * Bill:
     * 
     * If Item is rate plan Independent Item, then we select tpa bill first, if not cash bill is
     * returned. If item is rate plan dependent item, then, we check if item is visitrateplan
     * applicable, if so, then we check for any existing tpa bills and return the same, else we pick
     * cash bill. Suppose item is not visit rate plan applicable but has center rate plan
     * applicability, then we check for existing tpa bills and return the same, else we pick cash
     * bill.
     * 
     * If none of the cases matches and user does not have access right to create bill, then it
     * throws error saying user cannot create bills.
     */
    if (ratePlanForItem.get(NON_RATE_PLAN_ITEM).equals("Y")) {
      for (BasicDynaBean bill : billDetails) {
        if ((boolean) bill.get("is_tpa")) {
          exsistingBillDetails.put(BILL_NO, bill.get(BILL_NO));
          exsistingBillDetails.put("is_tpa", (Boolean) bill.get("is_tpa"));
          break;
        } else {
          exsistingBillDetails.put(BILL_NO, bill.get(BILL_NO));
          exsistingBillDetails.put("is_tpa", (Boolean) bill.get("is_tpa"));
        }
      }
    } else {

      boolean isVisitRatePlanApplicable = ((List<String>) ratePlanForItem.get("rate_plan"))
          .contains(visitRatePlan);

      boolean isCenterRatePlanApplicable = ((List<String>) ratePlanForItem.get("rate_plan"))
          .contains(nonInsuranceRatePlan);

      if (isVisitRatePlanApplicable) {
        for (BasicDynaBean bill : billDetails) {
          if (bill.get("bill_rate_plan_id").equals(visitRatePlan)
              && ((List<String>) ratePlanForItem.get("rate_plan")).contains(visitRatePlan)) {
            if ((Boolean) bill.get("is_tpa")) {
              exsistingBillDetails.put(BILL_NO, bill.get(BILL_NO));
              exsistingBillDetails.put("is_tpa", bill.get("is_tpa"));
              exsistingBillDetails.put(ORG_ID, visitRatePlan);
              break;
            } else {
              exsistingBillDetails.put(BILL_NO, bill.get(BILL_NO));
              exsistingBillDetails.put("is_tpa", bill.get("is_tpa"));
              exsistingBillDetails.put(ORG_ID, visitRatePlan);
            }
          }
        }
      }
      if (isCenterRatePlanApplicable && !exsistingBillDetails.containsKey(BILL_NO)) {
        for (BasicDynaBean bill : billDetails) {
          if (bill.get("bill_rate_plan_id").equals(nonInsuranceRatePlan)) {
            if ((Boolean) bill.get("is_tpa")) {
              exsistingBillDetails.put(BILL_NO, bill.get(BILL_NO));
              exsistingBillDetails.put("is_tpa", (Boolean) bill.get("is_tpa"));
              exsistingBillDetails.put(ORG_ID, nonInsuranceRatePlan);
              break;
            } else {
              exsistingBillDetails.put(BILL_NO, bill.get(BILL_NO));
              exsistingBillDetails.put("is_tpa", (Boolean) bill.get("is_tpa"));
              exsistingBillDetails.put(ORG_ID, nonInsuranceRatePlan);
            }
          }
        }
      }
    }

    selectedBillRatePlanMap.put("exsisting_bill_details", exsistingBillDetails);
  }

  /**
   * Get Rate Plan details for selected item.
   *
   * @param entity the entity
   * @param itemId the item id
   * @return the rate plan list map
   */
  private Map<String, Object> getRatePlanListMap(List<String> entity, String itemId) {
    Map<String, Object> getRatePlanList = new HashMap<>();
    boolean nonRatePlanItem = checkRatePlanIndependentItem(entity);
    if (!nonRatePlanItem) {
      List<BasicDynaBean> ratePlanListsForItem = getRatePlanForItem(itemId, entity);
      List<String> ratePlanList = new ArrayList<>();
      for (BasicDynaBean ratePlanForItem : ratePlanListsForItem) {
        ratePlanList.add((String) ratePlanForItem.get(ORG_ID));
      }
      getRatePlanList.put("rate_plan", ratePlanList);
      getRatePlanList.put(NON_RATE_PLAN_ITEM, "N");
    } else {
      getRatePlanList.put(NON_RATE_PLAN_ITEM, "Y");
    }
    return getRatePlanList;
  }

  /** The rate plan independent items. */
  private List<String> ratePlanIndependentItems = new ArrayList<>(
      Arrays.asList("Doctor", "Meal", "Equipment", "Bed", "ICU", "Other Charge", "Direct Charge"));

  /**
   * To check if item doesn't need rate plan to order.
   *
   * @param entityList the entity list
   * @return true, if successful
   */
  private boolean checkRatePlanIndependentItem(List<String> entityList) {
    for (String entity : entityList) {
      if (ratePlanIndependentItems.contains(entity)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return the list of rate plan for a given item_id.
   *
   * @param itemId the item id
   * @param entity the entity
   * @return the rate plan for item
   */
  private List<BasicDynaBean> getRatePlanForItem(String itemId, List<String> entity) {
    return orderRepository.getRatePlanForItem(itemId, entity);
  }

}
