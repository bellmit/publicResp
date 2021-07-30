package com.insta.hms.core.clinical.order.master;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.clinical.order.doctoritems.DoctorOrderItemValidator;
import com.insta.hms.core.clinical.order.serviceitems.ServiceOrderItemValidator;
import com.insta.hms.core.clinical.order.testitems.TestOrderItemValidator;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService;
import com.insta.hms.core.patient.registration.PatientRegistrationService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Component
public class OrderValidator {

  @LazyAutowired
  private TestOrderItemValidator testOrderItemValidator;

  @LazyAutowired
  private ServiceOrderItemValidator serviceOrderItemValidator;

  @LazyAutowired
  private BillService billService;

  @LazyAutowired
  private PatientRegistrationService patientRegistrationService;

  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  @LazyAutowired
  private PreAuthItemsService preAuthItemService;

  @LazyAutowired
  private DoctorOrderItemValidator doctorOrderItemValidator;
  
  /** The log. */
  private static Logger log = LoggerFactory.getLogger(OrderValidator.class);
  
  @LazyAutowired
  private SecurityService securityService;

  /**
   * validate conducting doctor.
   * 
   * @param orderParams          the orderParams
   * @param listValidationErrors the listValidationErrors
   * @return boolean
   */
  public boolean validateConductingDoctor(Map<String, List<Object>> orderParams,
      List<ValidationErrorMap> listValidationErrors) {

    Boolean status = false;
    Object[] testsOrderParams = (orderParams.get("tests") != null
          ? orderParams.get("tests") : new ArrayList<>()).toArray();
    if (testsOrderParams != null && testsOrderParams.length != 0) {
      ValidationErrorMap validationErrors = new ValidationErrorMap();
      Boolean check = testOrderItemValidator.validateConductingDoctor(testsOrderParams,
          validationErrors);
      if (check) {
        listValidationErrors.add(validationErrors);
        status = true;
      }
    }

    Object[] servicesOrderParams = (orderParams.get("services") != null
          ? orderParams.get("services") : new ArrayList<>()).toArray();
    if (servicesOrderParams != null && servicesOrderParams.length != 0) {
      ValidationErrorMap validationErrors = new ValidationErrorMap();
      Boolean check = serviceOrderItemValidator.validateConductingDoctor(servicesOrderParams,
          validationErrors);
      if (check) {
        listValidationErrors.add(validationErrors);
        status = true;
      }
    }

    return status;
  }

  /**
   * validate new order bill status.
   * 
   * @param params the params
   * @return boolean
   */
  public boolean validateNewOrderBillStatus(ModelMap params) {

    Map<String, Map<String, List<Object>>> orderedItems =
        (Map<String, Map<String, List<Object>>>) params.get("ordered_items");

    for (Entry<String, Map<String, List<Object>>> billEntry : orderedItems.entrySet()) {
      String billNo = billEntry.getKey();
      if (!billNo.equals("new") && !billNo.equals("newInsurance")) {
        for (Entry<String, List<Object>> itemType : billEntry.getValue().entrySet()) {
          for (Object itemObj : itemType.getValue()) {
            Map<String, Object> item = (Map<String, Object>) itemObj;
            boolean isNewOrder = "Y".equals(item.get("new"));
            if (isNewOrder) {
              BasicDynaBean billBean = billService.findByKey(billNo);
              String billStatus = (String) billBean.get("status");
              if (!"A".equals(billStatus)) {
                throw new ValidationException("exception.new.order.bill.closed");
              }
              String paidStatus = (String) billBean.get("payment_status");
              if (!"U".equals(paidStatus)) {
                throw new ValidationException("exception.new.order.bill.paid");
              }

            }
          }
        }
      }
      if (!billNo.equals("new")) {
        for (Entry<String, List<Object>> itemType : billEntry.getValue().entrySet()) {
          for (Object itemObj : itemType.getValue()) {
            BasicDynaBean billBean = null;
            Map<String, Object> item = (Map<String, Object>) itemObj;
            if ("Y".equals(item.get("new")) && !billNo.equals("newInsurance")) {
              billBean = billService.findByKey(billNo);
            }
            if (("A".equals(item.get("prior_auth_id_req")) || preAuthItemService
                    .isDoctorPrescribedForPreAuth(MapUtils.getInteger(item, "doc_presc_id")))
                && ((billBean != null && (Boolean) billBean.get("is_tpa"))
                    || billNo.equals("newInsurance"))
                && (StringUtils.isBlank((String) item.get("prior_auth_id"))
                    || (item.get("prior_auth_mode_id") == null)
                    || ((Integer) item.get("prior_auth_mode_id") == 0))) {
              if ("A".equals(item.get("prior_auth_id_req"))) {
                throw new ValidationException("exception.new.order.preauth.mandatory",
                    new String[] { (String) item.get("item_name") });
              } else {
                throw new ValidationException("exception.new.order.preauth.required",
                    new String[] { (String) item.get("item_name") });
              }
            }
          }
        }
      }
    }
    return true;
  }

  /**
   * validate visit.
   * 
   * @param visitParams the visitParams
   */
  public void validateVisit(Map<String, Object> visitParams) {
    String visitId = (String) visitParams.get("visit_id");
    boolean isActive = "A"
        .equals(patientRegistrationService.findByKey("patient_id", visitId).get("status"));
    if (!isActive) {
      throw new ValidationException("exception.visit.inactive");
    }
  }

  /**
   * set prescribed date and doctor.
   * 
   * @param orderItems the orderItems
   * @return map
   */
  public Map<String, List<Object>> setPrescribedDateAndDoctor(
      Map<String, Map<String, List<Map<String, Object>>>> orderItems) {
    Map<String, List<Object>> orderedItemsObject = new HashMap<String, List<Object>>();
    for (Entry<String, Map<String, List<Map<String, Object>>>> billEntry : orderItems.entrySet()) {
      String billType = billEntry.getKey();
      for (Entry<String, List<Map<String, Object>>> itemTypeEntry:
              billEntry.getValue().entrySet()) {
        String orderItemType = itemTypeEntry.getKey();
        List<Object> orderItemList = new ArrayList<Object>();
        for (Map<String, Object> item : itemTypeEntry.getValue()) {
          if (item.get(orderItemType + "_prescribed_date") == null
                  || item.get(orderItemType + "_prescribed_date") == "") {
            DateUtil dateUtil = new DateUtil();
            item.put(orderItemType + "_prescribed_date", dateUtil.getCurrentTimestamp());
          }
          if (item.get(orderItemType + "_prescribed_doctor_id") != null
                  && item.get(orderItemType + "_prescribed_doctor_id") != "") {
            item.put("prescribed_doctor_id", item.get(orderItemType + "_prescribed_doctor_id"));
          }
          orderItemList.add(item);
        }
        orderedItemsObject.put(orderItemType, orderItemList);
      }
    }
    return orderedItemsObject;
  }

  /**
   * validate prescribing doctor.
   * 
   * @param requestBody the requestBody
   */
  @SuppressWarnings("unchecked")
  public void validatePrescDoctor(Map<String, Object> requestBody) {
    String visitType = (String) ((Map<String, Object>) requestBody.get("visit")).get("visit_type");
    if (!"Y".equals(genericPreferencesService.getAllPreferences().get("op_one_presc_doc"))
        || "i".equals(visitType)) {
      return;
    }
    Map<String, Map<String, List<Map<String, Object>>>> orderedItems =
        (Map<String, Map<String, List<Map<String, Object>>>>) requestBody.get("ordered_items");
    Set<String> prescDoctorSet = new HashSet<>();
    if (null != requestBody.get("previous_presc_doctors")) {
      List<String> previousPrescDoctors = (List<String>) requestBody.get("previous_presc_doctors");
      if (previousPrescDoctors.size() > 0) {
        prescDoctorSet.addAll(previousPrescDoctors);
      }
    }

    for (Entry<String, Map<String, List<Map<String, Object>>>> billEntry :
            orderedItems.entrySet()) {
      for (Entry<String, List<Map<String, Object>>> itemType: billEntry.getValue().entrySet()) {
        for (Map<String, Object> item : itemType.getValue()) {
          if (null != item.get("prescribed_doctor_id")
                  && !"".equals(item.get("prescribed_doctor_id"))) {
            prescDoctorSet.add((String) item.get("prescribed_doctor_id"));
          }
          if (prescDoctorSet.size() > 1) {
            throw new ValidationException("js.order.common.one.prescribingdoctor.required");
          }
        }
      }
    }
  }

  /**
   * validate Pre Auth Items.
   * 
   * @param requestBody the requestBody
   */
  public void validatePreAuthItems(ModelMap requestBody) {
    Map<String, Map<String, List<Object>>> orderedItems =
        (Map<String, Map<String, List<Object>>>) requestBody.get("ordered_items");
    Map<Integer, Integer> preAuthItemsQuantityMap = new HashMap<>();
    Map<Integer, Integer> prescIdQuantityMap = new HashMap<>();
    for (Entry<String, Map<String, List<Object>>> billEntry : orderedItems.entrySet()) {
      for (Entry<String, List<Object>> itemType : billEntry.getValue().entrySet()) {
        for (Object itemObj : itemType.getValue()) {
          Map<String, Object> item = (Map<String, Object>) itemObj;
          Integer preAuthItemId = MapUtils.getInteger(item, "prior_auth_item_id");
          Integer quantity = MapUtils.getInteger(item, "quantity");
          Integer prescId = MapUtils.getInteger(item, "doc_presc_id");
          if (preAuthItemId != null && quantity != null) {
            preAuthItemsQuantityMap.put(preAuthItemId, quantity);
          }
          if (prescId != null && prescId != 0 && quantity != null) {
            prescIdQuantityMap.put(prescId, quantity);
          }
        }
      }
    }
    if (!preAuthItemService.isValidPreAuthItemQuantities(preAuthItemsQuantityMap)) {
      throw new ValidationException("exception.new.order.preauth.quantity");
    }

    if (!preAuthItemService.isValidPrescriptionItems(prescIdQuantityMap)) {
      throw new ValidationException("exception.new.order.pendingpresc.consumed");
    }
  }

  /**
   * validate Pre Auth Items.
   * 
   * @param orderParams          the orderParams
   * @param listValidationErrors the listValidationErrors
   * @return boolean
   */
  public boolean validatePreAuthItems(Map<String, List<Object>> orderParams,
      List<ValidationErrorMap> listValidationErrors) {
    boolean isValid = true;
    Object[] testsOrderParams = (orderParams.get("tests") != null
            ? orderParams.get("tests") : new ArrayList<>()).toArray();
    if (ArrayUtils.isNotEmpty(testsOrderParams)) {
      ValidationErrorMap validationErrors = new ValidationErrorMap();
      if (!testOrderItemValidator.validatePreAuthRequirements(testsOrderParams, validationErrors)) {
        listValidationErrors.add(validationErrors);
        isValid &= false;
      }
    }
    Object[] doctorsOrderParams = (orderParams.get("doctors") != null
            ? orderParams.get("doctors") : new ArrayList<>()).toArray();
    if (ArrayUtils.isNotEmpty(doctorsOrderParams)) {
      ValidationErrorMap validationErrors = new ValidationErrorMap();
      if (!doctorOrderItemValidator.validatePreAuthRequirements(doctorsOrderParams,
          validationErrors)) {
        listValidationErrors.add(validationErrors);
        isValid &= false;
      }
    }

    Object[] servicesOrderParams = (orderParams.get("services") != null
            ? orderParams.get("services") : new ArrayList<>()).toArray();
    if (ArrayUtils.isNotEmpty(servicesOrderParams)) {
      ValidationErrorMap validationErrors = new ValidationErrorMap();
      if (!serviceOrderItemValidator.validatePreAuthRequirements(servicesOrderParams,
          validationErrors)) {
        listValidationErrors.add(validationErrors);
        isValid &= false;
      }
    }
    return isValid;
  }
  
  
  @SuppressWarnings("unchecked")
  private boolean validateOrderingDate(Timestamp regDateTime, Timestamp currentTimestamp,
      boolean allowBackDateBillActivities, Map<String, List<Object>> orderParams,
      List<ValidationErrorMap> listValidationErrors, String itemType) throws ParseException {
    
    boolean isValid = true;
    Object[] itemOrderParams =
        orderParams.get(itemType) != null ? orderParams.get(itemType).toArray()
            : ArrayUtils.EMPTY_OBJECT_ARRAY;
    
    if (itemOrderParams != null && itemOrderParams.length != 0) {
      ValidationErrorMap validationErrors = new ValidationErrorMap();

      final String ITEM_PRESC_DATE = itemType + "_prescribed_date";

      final String ITEM_ACT_DESCRIPTION = itemType + "_act_description";

      DateUtil dateUtil = new DateUtil();
      
      for (int i = 0; i < itemOrderParams.length; i++) {
        if (((Map<String, Object>) itemOrderParams[i]).get(ITEM_PRESC_DATE) == null) {
          return false;
        }

        String itemName =
            (String) ((Map<String, Object>) itemOrderParams[i]).get(ITEM_ACT_DESCRIPTION);
        Timestamp itemPrescDateTime = new Timestamp(dateUtil.getTimeStampFormatter()
            .parse(((String) (((Map<String, Object>) itemOrderParams[i]).get(ITEM_PRESC_DATE))))
            .getTime());

        List<String> messageParams = new ArrayList<>();
        messageParams.add(itemName);
        if (allowBackDateBillActivities && itemPrescDateTime.before(regDateTime)) {
          validationErrors.addError(Integer.toString(i),
              "exception.order.ordering.date.time.cannot.be.greater.than.reg.time", messageParams);
          isValid = false;
          log.info(
              "Validated Prescribing date for {} ,Prescribing date is less than registration date",
              itemName);
        } else if (!allowBackDateBillActivities && (itemPrescDateTime.before(currentTimestamp)
            || itemPrescDateTime.before(regDateTime))) {
          validationErrors.addError(Integer.toString(i),
              "exception.order.ordering.date.time.cannot.be.greater.than.current.time",
              messageParams);
          isValid = false;
          log.info("Validated Prescribing date for {} ,Prescribing date is less than current date",
              itemName);
        } else {
          log.info("{} has valid presc/ordering date and time", itemName);
        }
      }

      if (!isValid) {
        listValidationErrors.add(validationErrors);
      }
    }
    return isValid;
  }
  
  /**
   * validate ordering date. This method is used to validate prescribed date for items
   * which is ordered in registration screen.
   * 
   * @param orderParams the orderParams
   * @param listValidationErrors the listValidationErrors
   * @return boolean
   */
  @SuppressWarnings("unchecked")
  public boolean validateOrderingDate(BasicDynaBean visitBean,
      Map<String, List<Object>> orderParams, List<ValidationErrorMap> listValidationErrors)
      throws ParseException {
    boolean isValid = true;
    
    String[] itemTypes = new String[] {"tests","services","doctors","packages"};
    Timestamp regDateTime = null;
    DateUtil dateUtil = new DateUtil();
    
    Calendar now = Calendar.getInstance();
    now.add(Calendar.MINUTE, -30);
    Timestamp currentTimestamp = new Timestamp(now.getTime().getTime());
    
    Map<String, String> actionRightsMap =
        (Map<String, String>) securityService.getSecurityAttributes().get("actionRightsMap");
    boolean allowBackDateBillActivities =
        "A".equals(actionRightsMap.get("allow_back_date_bill_activities"));
    
    if (null != visitBean && null != visitBean.get("reg_date")
        && null != visitBean.get("reg_time")) {
      regDateTime = new Timestamp(dateUtil.getSqlTimeStampFormatter()
          .parse(visitBean.get("reg_date") + " " + visitBean.get("reg_time")).getTime());
      for (String item : itemTypes) {
        isValid &= validateOrderingDate(regDateTime, currentTimestamp, allowBackDateBillActivities,
            orderParams, listValidationErrors, item);
      }
    }
    
    return isValid;
  }
  
  /**
   * validate prescribing date. This method is overided to validate for order screen.
   * 
   * @param requestBody the requestBody
   * @throws ParseException the exception
   */
  @SuppressWarnings("unchecked")
  public void validateOrderingDate(Map<String, Object> requestBody) throws ParseException {
    DateUtil dateUtil = new DateUtil();
    Timestamp regDateTime = null;
    Map<String, String> actionRightsMap =
        (Map<String, String>) securityService.getSecurityAttributes().get("actionRightsMap");
    boolean allowBackDateBillActivities =
        "A".equals(actionRightsMap.get("allow_back_date_bill_activities"));
    Calendar now = Calendar.getInstance();
    now.add(Calendar.MINUTE, -30);
    Timestamp currentTimestamp = new Timestamp(now.getTime().getTime());

    Map<String, Object> visitDetailsMap = (Map<String, Object>) requestBody.get("visit");

    if (null != visitDetailsMap && null != visitDetailsMap.get("reg_date")
        && null != visitDetailsMap.get("reg_time")) {
      regDateTime = new Timestamp(dateUtil.getTimeStampFormatter()
          .parse(visitDetailsMap.get("reg_date") + " " + visitDetailsMap.get("reg_time"))
          .getTime());
      Map<String, Map<String, List<Object>>> orderedItems =
          (Map<String, Map<String, List<Object>>>) requestBody.get("ordered_items");
      Set<String> orderItemSet = new HashSet<>();
      for (Entry<String, Map<String, List<Object>>> billEntry : orderedItems.entrySet()) {
        for (Entry<String, List<Object>> itemType : billEntry.getValue().entrySet()) {
          for (Object itemObj : itemType.getValue()) {
            Map<String, Object> item = (Map<String, Object>) itemObj;
            
            // ignores validating date if item is not newly added
            if (!"Y".equals(item.get("new"))) {
              continue;
            }
            
            Timestamp itemPrescDateTime = new Timestamp(dateUtil.getTimeStampFormatter()
                .parse((String) item.get("prescribed_date")).getTime());
            
            if (allowBackDateBillActivities && itemPrescDateTime.before(regDateTime)) {
              orderItemSet.add((String) item.get("item_name"));
              log.debug(
                  "Validated Prescribing date for {},Prescribing date is less than reg date",
                  item.get("item_name"));
            } else if (!allowBackDateBillActivities && (itemPrescDateTime.before(currentTimestamp)
                || itemPrescDateTime.before(regDateTime))) {

              orderItemSet.add((String) item.get("item_name"));
              log.debug(
                  "Validated Prescribing date for {} ,Prescribing date is less than current date",
                  item.get("item_name"));
            } else {
              log.info("{} has valid presc/ordering date and time", item.get("item_name"));
            }

            if (orderItemSet.size() > 1) {
              throw new ValidationException(
                "exception.order.ordering.date.time.cannot.be.greater.than.reg.time.order.screen");
            }
          }
        }
      }
    }
  }
}
