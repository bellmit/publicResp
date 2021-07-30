package com.insta.hms.integration.salucro;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.integration.paymentgateway.TransactionRequirements;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class SalucroService.
 */
@Service
public class SalucroService {

  /** The log. */
  private static Logger log = LoggerFactory.getLogger(SalucroService.class);

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The salucro role repository. */
  @LazyAutowired
  private SalucroRoleRepository salucroRoleRepository;

  /** The salucro location repository. */
  @LazyAutowired
  private SalucroLocationRepository salucroLocationRepository;

  /** The salucro role repository. */
  @LazyAutowired
  private SalucroPaymentRepository salucroPaymentRepository;

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;
  
  /**
   * Gets the salucro report details.
   *
   * @return the salucro report details
   * @throws Exception exception
   */
  public Map<String,String> getSalucroReportDetails() throws Exception {
    ArrayList<String> locationId = new ArrayList<String>();
    ArrayList<String> roles = new ArrayList<String>();
    Map<String, Object> configMap = new HashMap<>();
    Integer centerId = RequestContext.getCenterId() != null
              ? RequestContext.getCenterId() : 0;
    String username = sessionService.getSessionAttributes().get("userId").toString();
    configMap.put(SalucroConstants.USERNAME, username);
    configMap.put(SalucroConstants.USERID, username);
    List<BasicDynaBean> salucroRoleBean = salucroRoleRepository
        .getSalucroRoleDetailsList(username,centerId, null);
    for (BasicDynaBean role :salucroRoleBean) {
      roles.add(role.get("role").toString());
    }
    configMap.put(SalucroConstants.ROLES, roles);
    List<BasicDynaBean> salucroLocationBean = salucroLocationRepository
        .getSalucroLocationDetailsList(null, centerId);
    for (BasicDynaBean location : salucroLocationBean) {
      locationId.add(location.get("id").toString());
    }
    configMap.put(SalucroConstants.LOCATION_IDS, locationId);
    return SalucroUtil.getSalucroReport(configMap);
  }

  /**
   * Gets the salucro transaction details.
   *
   * @return the salucro report details
   * @throws Exception exception
   */
  public Map<String,String> getSalucroTransactionDetails(HttpServletRequest paramMap)
        throws Exception {
    ArrayList<String> locationId = new ArrayList<String>();
    ArrayList<String> roles = new ArrayList<String>();
    Map<String, Object> configMap = new HashMap<>();
    Integer centerId = RequestContext.getCenterId() != null
              ? RequestContext.getCenterId() : 0;
    String username = sessionService.getSessionAttributes().get("userId").toString();
    configMap.put(SalucroConstants.USERNAME, username);
    configMap.put(SalucroConstants.USERID, username);
    List<BasicDynaBean> salucroRoleBean = salucroRoleRepository
        .getSalucroRoleDetailsList(username,centerId, null);
    for (BasicDynaBean role :salucroRoleBean) {
      roles.add(role.get("role").toString());
    }
    configMap.put(SalucroConstants.ROLES, roles);
    List<BasicDynaBean> salucroLocationBean = salucroLocationRepository
        .getSalucroLocationDetailsList(null, centerId);
    for (BasicDynaBean location : salucroLocationBean) {
      locationId.add(location.get("id").toString());
    }
    String paymentId = paramMap.getParameter("paymentId") != null
            ? (String) paramMap.getParameter("paymentId") : null;
    String lineItemId = paramMap.getParameter("lineItemId") != null
            ?  (String) paramMap.getParameter("lineItemId") : null;
    String transactionId = paramMap.getParameter("transactionId") != null
             ? (String) paramMap.getParameter("transactionId") : null;
    configMap.put(SalucroConstants.LOCATION_IDS, locationId);
    configMap.put(SalucroConstants.TRANSACTION_ID,transactionId);
    configMap.put(SalucroConstants.PAYMENT_ID,paymentId);
    configMap.put(SalucroConstants.LINE_ITEM_ID,lineItemId);
    return SalucroUtil.getSalucroTransactions(configMap);
  }

  /**
  * Do salucro refund.
  *
  * @param transactionReq the transaction req
  * @param paramMap the param map
  * @return the map
  * @throws Exception exception
  */
  public Map<String,String> doSalucroRefund(
      TransactionRequirements transactionReq, Map<String, String[]> paramMap) throws Exception {
    ArrayList<String> locationId = new ArrayList<String>();
    ArrayList<String> roles = new ArrayList<String>();
    Map<String, Object> configMap = new HashMap<>();
    String billNo = paramMap.get("billNo")[0];
    BasicDynaBean transactionRoleBean = salucroPaymentRepository
         .getTransactionId(billNo);
    if ( transactionRoleBean == null ) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.transaction.not.found");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    configMap.put(SalucroConstants.TRANSACTION_ID, transactionRoleBean.get("transaction_id"));
    Integer centerId = RequestContext.getCenterId() != null
            ? RequestContext.getCenterId() : 0;
    String username = sessionService.getSessionAttributes().get("userId").toString();
    configMap.put(SalucroConstants.USERNAME, username);
    String counterId = paramMap.get("counter_id")[0];
    List<BasicDynaBean> salucroRoleBean = salucroRoleRepository
        .getSalucroRoleDetailsList(username, centerId, counterId);
    for (BasicDynaBean role :salucroRoleBean) {
      roles.add(role.get("role").toString());
    }
    configMap.put(SalucroConstants.ROLES, roles);
    List<BasicDynaBean> salucroLocationBean = salucroLocationRepository
        .getSalucroLocationDetailsList(counterId,centerId);
    for (BasicDynaBean location :salucroLocationBean) {
      locationId.add(location.get("id").toString());
    }
    configMap.put(SalucroConstants.LOCATION_IDS, locationId);
    return SalucroUtil.doSalucroRefund(configMap);
  }

  /**
   * Do salucro settlement.
   *
   * @param transactionReq the transaction req
   * @param parameterMap the parameter map
   * @return the map
   * @throws Exception exception
   */
  public Map<String,String> doSalucroSettlement(
      TransactionRequirements transactionReq, Map<String, String[]> parameterMap) throws Exception {
    ArrayList<String> locationId = new ArrayList<String>();
    ArrayList<String> roles = new ArrayList<String>();
    Map<String, Object> configMap = new HashMap<>();
    String username = sessionService.getSessionAttributes().get("userId").toString();
    Integer centerId = RequestContext.getCenterId() != null
            ? RequestContext.getCenterId() : 0;
    configMap.put(SalucroConstants.USERNAME, username);
    configMap.put(SalucroConstants.USERID, username);
    String counterId = parameterMap.get("counter_id")[0];
    List<BasicDynaBean> salucroRoleBean = salucroRoleRepository
        .getSalucroRoleDetailsList(username,centerId, counterId);
    for (BasicDynaBean role :salucroRoleBean) {
      roles.add(role.get("role").toString());
    }
    configMap.put(SalucroConstants.ROLES, roles);
    Map<String, Object> accountsObject = new HashMap<String, Object>();
    List<BasicDynaBean> salucroLocationBean = salucroLocationRepository
        .getSalucroLocationDetailsList(counterId,centerId);
    for (BasicDynaBean location :salucroLocationBean) {
      locationId.add(location.get("id").toString());
      accountsObject.put(SalucroConstants.LOCATION_ID, location.get("id").toString());
    }
    configMap.put(SalucroConstants.LOCATION_IDS, locationId);
    String patientName = parameterMap.get("patientName") != null
         && !StringUtils.isEmpty(parameterMap.get("patientName")[0])
         ? parameterMap.get("patientName")[0] : null;
    if (patientName != null) {
      accountsObject.put(SalucroConstants.FIRST_NAME, patientName);
    }
      
    String emailId = parameterMap.get("email") != null
         && !StringUtils.isEmpty(parameterMap.get("email")[0])
         ? parameterMap.get("email")[0] : null;
    if (emailId != null) {
      accountsObject.put(SalucroConstants.EMAIL, emailId);
    }
      
    String accountNo = parameterMap.get("billNo") != null
         && !StringUtils.isEmpty(parameterMap.get("billNo")[0])
         ? parameterMap.get("billNo")[0] : null;
    accountsObject.put(SalucroConstants.ACCOUNT_NUMBER, accountNo);

    Double amount = parameterMap.get("pay") != null
         && !StringUtils.isEmpty(parameterMap.get("pay")[0])
         ? Double.parseDouble(parameterMap.get("pay")[0])  : null;
    accountsObject.put(SalucroConstants.AMOUNT, amount);
      
    String patientPhone = parameterMap.get("phone") != null 
         && !StringUtils.isEmpty(parameterMap.get("phone")[0])
         ? parameterMap.get("phone")[0] : null;
    accountsObject.put(SalucroConstants.PHONE, patientPhone);
    List<Object> accounts = new ArrayList<Object>();
    accounts.add(accountsObject);
    configMap.put("accounts", accounts);
    return SalucroUtil.doSalucroPayment(configMap);
  }

  /**
   * Meta data.
   *
   * @return the map
   * @throws Exception exception
   */
  public Map<String, Object> fetchSalucroDetails() throws Exception {
    Map<String, Object> resultData = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> resultMap = SalucroUtil.getSalucroInformation();
    if ( resultMap != null ) {
      if ( resultMap.get("result_status").equalsIgnoreCase("success")) {
        resultData = mapper.readValue(resultMap.get("payload"), Map.class);
      } else {
        resultData = mapper.readValue(resultMap.get("error"), Map.class);
      }
    }
    return resultData;
  }

  /**
  * Insert role mapping details.
  *
  * @param requestBody the request body
  * @return the map
  */
  public Map<String, Object> insertUserRoleMappingDetails(Map requestBody) {
    boolean result = true;
    List<BasicDynaBean> resultList = new ArrayList<>();
    HashSet<String> beanSet = new HashSet<>();
    List userList = (List) requestBody.get("user_id");
    List statusList = (List) requestBody.get("status");
    List salucroRoleList = (List) requestBody.get("salucro_user_role");
    if (userList.isEmpty() || statusList.isEmpty() || salucroRoleList.isEmpty()) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.wrong.inputs");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    for (int i = 0; i < userList.size(); i++) {
      BasicDynaBean bean = salucroRoleRepository.getBean();
      String role = salucroRoleList.get(i) != null ? (String) salucroRoleList.get(i) : null;
      bean.set("role", role );
      HashMap<String, Object> userMap = (HashMap<String, Object>) userList.get(i);
      String userName = userMap != null ? (String) userMap.get("emp_username") : null;
      Integer centerId = userMap != null ? (Integer) userMap.get("center_id") : null;
      String counterId = userMap != null ? (String) userMap.get("counter_id") : null;
      String status = statusList.get(i) != null ? (String) statusList.get(i) : "I";
      bean.set("emp_username", userName);
      bean.set("center_id", centerId);
      bean.set("counter_id", counterId);
      bean.set("status", status);
      bean.set("created_at", new java.sql.Timestamp(new java.util.Date().getTime()));
      
      // duplicate User check
      String dupString = role + "" + userName + "" + centerId + "" + counterId ;
      if (beanSet.contains(dupString)) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("error", "ui.exception.user.mapping.already.exists");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("error", ex.getErrors());
        throw new NestableValidationException(nestedException);        
      } else {
        beanSet.add(dupString);
      }
      
      String userIfExists = salucroRoleRepository.checkIfUserExists(bean.getMap(),"add");
      if ( userIfExists != null) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("error", "ui.exception.user.mapping.already.exists");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("error", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
      resultList.add(bean);
    }

    int[] successes = salucroRoleRepository.batchInsert(resultList);
    for (int success : successes) {
      if (success < 0) {
        result = false;
        break;
      }
    }
    if (!result) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.failed.insertion");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    Map<String, Object> finalMap = new HashMap<String, Object>();
    finalMap.put("beanList", resultList);
    return finalMap;
  }
  
  /**
  * Insert location mapping details.
  *
  * @param requestBody the request body
  * @return the map
  */
  public Map<String, Object> insertCounterLocationMappingDetails(Map requestBody) {
    boolean result = true;
    List<BasicDynaBean> resultList = new ArrayList<>(); 
    List counterList = (List) requestBody.get("counter_id");
    List statusList = (List) requestBody.get("status");
    List salucroLocationList = (List) requestBody.get("salucro_counter_location");
    String userId = sessionService.getSessionAttributes().get("userId").toString();
    if (counterList.isEmpty() || statusList.isEmpty() || salucroLocationList.isEmpty()) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.wrong.inputs");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    for (int i = 0; i < counterList.size(); i++) {
      BasicDynaBean bean = salucroLocationRepository.getBean();
      bean.set("id", salucroLocationList.get(i) != null
          ? (Integer) salucroLocationList.get(i) : null);
      HashMap<String, Object> counterMap = (HashMap<String, Object>) counterList.get(i);
      bean.set("counter_id", counterMap != null ? (String) counterMap.get("counter_id") : null);
      bean.set("center_id", counterMap != null ? (Integer) counterMap.get("center_id") : null);
      bean.set("status", statusList.get(i) != null ? (String) statusList.get(i) : "I");
      bean.set("created_at", new java.sql.Timestamp(new java.util.Date().getTime()));
      bean.set("user_id", userId);

      // duplicate Counter check
      String counterIfExists = salucroLocationRepository.checkIfCounterExists(bean.getMap(), "add");
      if ( counterIfExists != null) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("error", "ui.exception.counter.mapping.already.exists");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("error", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
      resultList.add(bean);
    }

    int[] successes = salucroLocationRepository.batchInsert(resultList);
    for (int success : successes) {
      if (success < 0) {
        result = false;
        break;
      }
    }
    if (!result) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.failed.insertion");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    Map<String, Object> finalMap = new HashMap<String, Object>();
    finalMap.put("beanList", resultList);
    return finalMap;
  }
  
  /**
   * Update user role mapping details.
   *
   * @param requestBody the request body
   * @return the map
   */
  public Map<String, Object> updateUserRoleMappingDetails(Map requestBody) {
    Long editUserId = Long.parseLong(requestBody.get("editId").toString());
    List statusList = (List) requestBody.get("status");
    List userList = (List) requestBody.get("user_id");
    List salucroRoleList = (List) requestBody.get("salucro_user_role");
    if (editUserId == null || userList.isEmpty() 
         || statusList.isEmpty() || salucroRoleList.isEmpty()) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.wrong.inputs");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    BasicDynaBean bean = salucroRoleRepository.getBean();
    HashMap<String, Object> userMap = (HashMap<String, Object>) userList.get(0);
    bean.set("center_id", userMap != null ? (Integer) userMap.get("center_id") : null);
    bean.set("emp_username", userMap != null ? (String) userMap.get("emp_username") : null);
    bean.set("counter_id", userMap != null ? (String) userMap.get("counter_id") : null);
    bean.set("salucro_role_mapping_id", editUserId);
    bean.set("role", salucroRoleList.get(0) != null
            ? (String) salucroRoleList.get(0) : null);
    bean.set("status", statusList.get(0) != null ? (String) statusList.get(0) : "I");
    bean.set("modified_at", new java.sql.Timestamp(new java.util.Date().getTime()));
    
    String userIfExists = salucroRoleRepository.checkIfUserExists(bean.getMap(),"update");
    if ( userIfExists != null) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.user.mapping.already.exists");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    boolean result = salucroRoleRepository.update(bean, editUserId) > 0;
    if (!result) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.failed.update");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    return bean.getMap();
  }

  /**
   * Update user role mapping details.
   *
   * @param requestBody the request body
   * @return the map
   */
  public Map<String, Object> updateCounterLocationMappingDetails(Map requestBody) {
    Long editCounterId = Long.parseLong(requestBody.get("editId").toString());
    List counterList = (List) requestBody.get("counter_id");
    List statusList = (List) requestBody.get("status");
    List salucroLocationList = (List) requestBody.get("salucro_counter_location");
    if (editCounterId == null || counterList.isEmpty() 
         || statusList.isEmpty() || salucroLocationList.isEmpty()) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.wrong.inputs");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    BasicDynaBean bean = salucroLocationRepository.getBean();
    HashMap<String, Object> counterMap = (HashMap<String, Object>) counterList.get(0);
    bean.set("center_id", counterMap != null ? (Integer) counterMap.get("center_id") : null);
    bean.set("counter_id", counterMap != null ? (String) counterMap.get("counter_id") : null);
    bean.set("salucro_location_mapping_id", editCounterId);
    bean.set("id", salucroLocationList.get(0) != null
            ? (Integer) salucroLocationList.get(0) : null);
    bean.set("status", statusList.get(0) != null ? (String) statusList.get(0) : "I");
    bean.set("modified_at", new java.sql.Timestamp(new java.util.Date().getTime()));
    
    String counterIfExists = salucroLocationRepository.checkIfCounterExists(bean.getMap(),"update");
    if ( counterIfExists != null) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.counter.mapping.already.exists");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("duplicate", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    boolean result = salucroLocationRepository.update(bean, editCounterId) > 0;
    if (!result) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.failed.update");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    return bean.getMap();
  }
  
  /**
   * Search User.
   *
   * @param params the params
   * @param listingParams the listing params
   * @return the paged list
   * @throws Exception exception
   */
  public PagedList searchUser(Map params, Map<LISTING, Object> listingParams) throws Exception {
    PagedList result = null;
    try {
      result = salucroRoleRepository.search(params, listingParams);
      List<Map> dtoList = new ArrayList();
      for (Map dto : (List<Map>) result.getDtoList()) {
        Map salucroMap = new HashMap(dto);
        dtoList.add(salucroMap);
      }
      result.setDtoList(dtoList);
    } catch ( Exception ex ) {
      log.error("Exception occured in method searchUser : " + ex);
      throw ex;
    }
    return result;
    
  }

  /**
   * Search Counter.
   *
   * @param params the params
   * @param listingParams the listing params
   * @return the paged list
   * @throws Exception exception
   */
  public PagedList searchCounter(Map params, Map<LISTING, Object> listingParams) throws Exception {
    PagedList result = null;
    try {
      result = salucroLocationRepository.search(params, listingParams);
      List<Map> dtoList = new ArrayList();
      for (Map dto : (List<Map>) result.getDtoList()) {
        Map salucroMap = new HashMap(dto);
        dtoList.add(salucroMap);
      }
      result.setDtoList(dtoList);
    } catch ( Exception ex ) {
      log.error("Exception occured in method searchCounter : " + ex);
      throw ex;
    }
    return result;
  }

  /**
   * Delete user to role mapping.
   *
   * @param requestBody the request body
   * @return the map
   */
  public Map<String, Object> deleteUserToRoleMapping(Map requestBody) {
    Map<String, Object> result = new HashMap<String, Object>();
    Long editUserId = Long.parseLong(requestBody.get("editId").toString());
    if (editUserId == null ) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.wrong.inputs");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    int rowDeleted = salucroRoleRepository.delete("salucro_role_mapping_id", editUserId);
    if (rowDeleted != 1 ) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.failed.delete");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    result.put("result",editUserId);
    return result;
  }

  /**
   * Delete location to counter mapping.
   *
   * @param requestBody the request body
   * @return the map
   */
  public Map<String, Object> deleteLocationToCounterMapping(Map requestBody) {
    Map<String, Object> result = new HashMap<String, Object>();
    Long editCounterId = Long.parseLong(requestBody.get("editId").toString());
    if (editCounterId == null ) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.wrong.inputs");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    
    int rowDeleted = salucroLocationRepository.delete("salucro_location_mapping_id", editCounterId);
    if (rowDeleted != 1 ) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.failed.delete");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    result.put("result",editCounterId);
    return result;
  }

  /**
   * Insert transaction to payment transaction.
   *
   * @param requestBody the request body
   * @return the map
   */
  public Map<String, Object> processTransaction(
      HttpServletRequest requestBody) {
    boolean result = true;
    boolean status = false;
    String statusCode = (String) requestBody.getParameter("status"); 
    if (statusCode.isEmpty()) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.wrong.inputs");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    if (statusCode.equalsIgnoreCase("success")) {
      status = true;
    }
    String billNo = requestBody.getParameter("billNo");
    if (billNo != null && billNo.length() > SalucroConstants.BILL_NO_length) {
      billNo = billNo.substring(0, SalucroConstants.BILL_NO_length);
    }
    String transaction = requestBody.getParameter("transaction") != null
            ? requestBody.getParameter("transaction") : "";
    Double amount = Double.parseDouble(requestBody.getParameter("amount"));
    BigDecimal bamount = BigDecimal.valueOf(amount.doubleValue());
    BasicDynaBean bean = salucroPaymentRepository.getBean();
    bean.set("mode_id", Integer.parseInt(requestBody.getParameter("mode")));
    bean.set("transaction_type", "SALUCRO_PAYMENT");
    bean.set("status", status);
    bean.set("amount", bamount);
    bean.set("status_code", statusCode );
    bean.set("status_message", statusCode );
    bean.set("created_at", new java.sql.Timestamp(new java.util.Date().getTime()));
    bean.set("transaction_id", requestBody.getParameter("transactionId"));
    bean.set("card_number", requestBody.getParameter("cardNumber"));
    bean.set("initiated_by", requestBody.getParameter("inititaedBy"));
    bean.set("bill_no", billNo);
    bean.set("response", transaction);
    bean.set("payment_mode", requestBody.getParameter("paymentType"));
    bean.set("approval_code", requestBody.getParameter("cardAuthCode"));
    int paymentTransactionId = salucroPaymentRepository.insertPaymentTransaction(bean);
    if (paymentTransactionId < 0) {
      result = false; 
    }

    if (!result) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("error", "ui.exception.failed.insertion");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("error", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    Map<String, Object> finalMap = new HashMap<String, Object>();
    finalMap.put("bean", bean);
    finalMap.put("transaction_id", paymentTransactionId);
    return finalMap;
  }

  /**
   * Fetch Transaction.
   *
   * @param params the params
   * @param listingParams the listing params
   * @return the paged list
   * @throws Exception exception
   */
  public PagedList fetchTransaction(Map params,
        Map<LISTING, Object> listingParams) throws Exception {
    PagedList result = null;
    try {
      Map<String, Object> configMap = new HashMap<>();
      String username = sessionService.getSessionAttributes().get("userId").toString();
      String roleId = sessionService.getSessionAttributes().get("roleId").toString();
      configMap.put(SalucroConstants.USERNAME, username);
      configMap.put(SalucroConstants.ROLE, roleId);
      result = salucroPaymentRepository.fetchTransaction(params, listingParams, configMap);
      List<Map> dtoList = new ArrayList();
      for (Map dto : (List<Map>) result.getDtoList()) {
        Map salucroMap = new HashMap(dto);
        dtoList.add(salucroMap);
      }
      if (result != null ) {
        result.setDtoList(dtoList);
      }
    } catch ( Exception ex ) {
      log.error("Exception occured in method fetch transaction by date : " + ex);
      throw ex;
    }
    return result;
  }
}
