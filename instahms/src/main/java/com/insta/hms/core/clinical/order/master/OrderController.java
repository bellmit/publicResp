package com.insta.hms.core.clinical.order.master;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.URLRoute;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService.PreAuthItemType;
import com.insta.hms.exception.HMSException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class OrderController extends BaseRestController {

  static final Logger logger = LoggerFactory.getLogger(OrderController.class);

  @Autowired
  private OrderService orderService;

  @LazyAutowired
  private OrderValidator orderValidator;

  @LazyAutowired
  private PreAuthItemsService preAuthItemsService;

  private String flowType;

  public OrderController(String flowType) {
    super();
    this.flowType = flowType;
  }

  /**
   * get Order Index Page.
   * 
   * @return ModelAndView
   */
  @IgnoreConfidentialFilters
  @GetMapping(URLRoute.VIEW_INDEX_URL)
  public ModelAndView getOrderIndexPage() {
    String bundle = "v12";
    if (this.flowType == "ipFlow") {
      bundle = "ipFlow";
    }
    return renderFlowUi("Order", bundle, "withFlow", this.flowType, "order", false);
  }
  
  /**
   * get Details.
   * 
   * @param request   the request
   * @param response  the response
   * @param visitId   the visitId
   * @param visitType the visitType
   * @param pharmacy  the pharmacy
   * @return map
   */
  @GetMapping(value = "/getdetails")
  public Map<String, Object> getDetails(HttpServletRequest request, HttpServletResponse response,
      @RequestParam(value = "visit_id") String visitId,
      @RequestParam(required = false, value = "visit_type") String visitType,
      @RequestParam(required = false, value = "pharmacy") String pharmacy) {

    if (!"".equals(visitId)) {
      return orderService.getDetails(visitId, visitType, pharmacy);
    } else {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
  }

  /**
   * bill Update.
   * 
   * @param request     the request
   * @param response    the response
   * @param requestBody the requestBody
   * @return map
   * @throws SQLException   the SQLException
   * @throws ParseException the ParseException
   */
  @RequestMapping(value = "/billupdate", method = RequestMethod.POST, consumes = "application/json")
  public Map<String, Object> billUpdate(HttpServletRequest request, HttpServletResponse response,
      @RequestBody ModelMap requestBody) throws SQLException, ParseException {
    Map<String, Object> map;
    if (requestBody.get("bill").equals("new")) {
      map = orderService.createBill("Y".equals((String) requestBody.get("is_insurance")),
          (Map<String, Object>) requestBody);
    } else {
      map = orderService.cancel(requestBody);
    }
    return map;
  }

  /**
   * update Orders.
   * 
   * @param requestBody the requestBody
   * @return map
   * @throws ParseException            the ParseException
   * @throws IOException               the IOException
   * @throws InvocationTargetException the InvocationTargetException
   * @throws NoSuchMethodException     the NoSuchMethodException
   * @throws IllegalAccessException    the IllegalAccessException
   * @throws SQLException              the SQLException
   */
  @SuppressWarnings("unchecked")
  @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = "application/json")
  public Map<String, Object> updateOrders(@RequestBody ModelMap requestBody)
      throws ParseException, IOException, InvocationTargetException, NoSuchMethodException,
      IllegalAccessException, SQLException {
    if (requestBody == null || requestBody.get("visit") == null
        || requestBody.get("mr_no") == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    orderValidator.validateNewOrderBillStatus(requestBody);
    List<String> previousPrescDoctors = new ArrayList<String>();
    Map<String, Object> visitParams = (Map<String, Object>) requestBody.get("visit");
    if (null != visitParams.get("doctor_id")) {
      previousPrescDoctors.add((String) visitParams.get("doctor_id"));
    }
    requestBody.put("previous_presc_doctors", previousPrescDoctors);
    orderValidator.validatePrescDoctor((Map<String, Object>) requestBody);
    orderValidator.validatePreAuthItems(requestBody);
    orderValidator.validateVisit(visitParams);
    orderValidator.validateOrderingDate(requestBody);

    orderService.orderItems(requestBody, null);
    String visitId = (String) visitParams.get("visit_id");
    orderService.recalculateSponsorAmount(visitId);
    String visitType = (String) visitParams.get("visit_type");
    String mrNo = (String) requestBody.get("mr_no");
    List<String> itemTypes = orderService.getItemTypes(requestBody);
    // Create preauth prescriptions if limit exceeded.
    orderService.initiatePreAuthLimitCheck(visitId);
    return orderService.updateOrderResponse(visitId, visitType, mrNo, itemTypes);
  }

  /**
   * Return allVisitInformation (active/Inactive visit with order, all active Visit) As well as
   * returns allergies information of the patient.
   *
   * @param mrNo the mrNo
   * @return map
   */
  @GetMapping(value = "/getvisitdetails")
  public Map<String, Object> getVisitDetails(
      @RequestParam(required = true, value = "mr_no") String mrNo,
      @RequestParam(defaultValue = "o", value = "visit_type") String visitType) {
    return orderService.getVisitDetails(mrNo, visitType);
  }

  /**
   * Will return the list of all orders for a specific visit id passed.
   * 
   * @param visitId the visitId
   * @return map
   */
  @GetMapping(value = "/getOrders")
  public Map<String, Object> getOrders(
      @RequestParam(required = true, value = "visit_id") String visitId) {
    return orderService.getOrders(visitId, flowType, null);
  }

  /**
   * get Pre Auth Approved Items.
   * 
   * @param mrNo             the mrNo
   * @param centerId         the centerId
   * @param primarySponsorId the primarySponsorId
   * @return map
   */
  @GetMapping(value = "/getPreAuthApprovedItems")
  public Map<String, Object> getPreAuthApprovedItems(
      @RequestParam(required = true, value = "mr_no") String mrNo,
      @RequestParam(required = true, value = "center_id") Integer centerId,
      @RequestParam(required = true, value = "primary_sponsor_id") String primarySponsorId) {
    Map<String, Object> preAuthApprovedItemsMap = new HashMap<>();
    for (Map.Entry<PreAuthItemType, List<BasicDynaBean>> itemTypeBeanMap : preAuthItemsService
        .getActivePreAuthApprovedItems(mrNo, centerId, primarySponsorId).entrySet()) {
      preAuthApprovedItemsMap.put(itemTypeBeanMap.getKey().getResponseMapKey(),
          ConversionUtils.listBeanToListMap(itemTypeBeanMap.getValue()));
    }
    return preAuthApprovedItemsMap;
  }

  /**
   * Gets the common order ids.
   *
   * @param visitId the visit id
   * @return the common order ids
   */
  @GetMapping(value = "/getcommonorderids")
  public Map<String, Object> getCommonOrderIds(@RequestParam(name = "visit_id") String visitId) {
    Map<String, Object> returnMap = new HashMap<>();
    returnMap.put("common_order_ids", orderService.getCommonOrderIds(visitId));
    return returnMap;
  }

  /**
   * Gets the additional docs.
   *
   * @param prescribedId the prescribed id
   * @return the additional docs
   */
  @GetMapping(value = "/getadditionaldocs")
  public Map<String, Object> getAdditionalDocs(
      @RequestParam(name = "prescribed_id") Integer prescribedId) {
    Map<String, Object> returnMap = new HashMap<>();
    returnMap.put("documents",
        ConversionUtils.listBeanToListMap(orderService.getAdditionalDocs(prescribedId)));
    return returnMap;
  }

  /**
   * Marks patient package as discontinued while updating the remark.
   *
   * @param requestBody the request
   */
  @RequestMapping(value = "/discontinuepackage", method = RequestMethod.POST,
      consumes = "application/json")
  public void discontinuePackage(@RequestBody ModelMap requestBody) {
    Integer patientPackageId = (Integer) requestBody.get("patientPackageId");
    String discontinueRemark = (String) requestBody.get("discontinueRemark");
    this.orderService.discontinuePackage(patientPackageId, discontinueRemark);
  }

}
