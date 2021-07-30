package com.insta.hms.api.controllers;

import com.bob.hms.common.DateUtil;
import com.insta.hms.api.models.HmsAccountingInfoXMLBinding;
import com.insta.hms.api.models.HmsAccountinngInfoList;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.HMSErrorResponse;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.extension.billing.AccountingBaseController;
import com.insta.hms.extension.billing.AccountingService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class AccountingController.
 */
@RequestMapping("/api")
public class AccountingController extends AccountingBaseController {

  /** The service. */
  @LazyAutowired
  AccountingService service;

  /** The message util. */
  @LazyAutowired
  MessageUtil messageUtil;
  
  DateUtil dateUtil = new DateUtil();

  /** The Constant RETURN_CODE_SUCCESS. */
  private static final String RETURN_CODE_SUCCESS = "2001";

  /** The Constant RETURN_MSG_SUCCESS. */
  private static final String RETURN_MSG_SUCCESS = "Success";

  /** The Constant RETURN_CODE_NODATA. */
  private static final String RETURN_CODE_NODATA = "2004";

  /** The Constant RETURN_MSG_NODATA. */
  private static final String RETURN_MSG_NODATA = "No matching records found";

  /** The Constant RETURN_CODE_INVALID_DATA. */
  private static final String RETURN_CODE_INVALID_DATA = "1021";
  
  private static final String[] DATE_OPS = new String[]{"ge,le"};

  private static final List<String> VALID_FILTERBY = Arrays.asList(new String[] {
      "bill_finalized_date", "voucher_date", "created_at", "bill_last_finalized_date"});
  /** Logger Instance. */
  private static final Logger logger = LoggerFactory.getLogger(AccountingController.class);

  /**
   * Search.
   *
   * @param model
   *          the model
   * @param req
   *          the req
   * @param response
   *          the response
   * @return the response entity
   */
  @RequestMapping(value = { "/accounting/vouchers/list" }, method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> search(Model model, HttpServletRequest req,
      HttpServletResponse response) {
    Map<String, String[]> reqMap = req.getParameterMap();
    // Map<String, Object> summary = new HashMap<String, Object>();
    List list = new ArrayList();
    if (null != reqMap && reqMap.size() > 0) {
      Map<String, String[]> paramMap = preprocessParams(reqMap);
      PagedList pagedList = service.search(paramMap);
      list = ConversionUtils.listBeanToListMap(pagedList.getDtoList());
    }
    List<Object> referenceData = getReferenceData("search", reqMap);
    AccountingModelAndView view = createView("search", new Object[] { null, list },
        null /* referenceData */); // , summary, map);
    return prepareSuccessResponse(view, (null != list && list.size() > 0));
  }

  /**
   * Export.
   *
   * @param model
   *          the model
   * @param req
   *          the req
   * @param response
   *          the response
   * @return the hms accountinng info list
   */
  @RequestMapping(value = { "/accounting/vouchers/export" }, 
      method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
  public @ResponseBody HmsAccountinngInfoList export(Model model, HttpServletRequest req,
      HttpServletResponse response) {
    Map<String, String[]> reqMap = req.getParameterMap();
    List list = new ArrayList();
    List<HmsAccountingInfoXMLBinding> voucherList = new ArrayList<HmsAccountingInfoXMLBinding>();
    if (null != reqMap && reqMap.size() > 0) {
      Map<String, String[]> paramMap = preprocessParams(reqMap);
      PagedList pagedList = service.search(paramMap);
      list = ConversionUtils.listBeanToListMap(pagedList.getDtoList());

      Iterator<Map<String, Object>> iterator = list.iterator();
      while (iterator.hasNext()) {
        Map<String, Object> listItem = iterator.next();
        HmsAccountingInfoXMLBinding voucher = new HmsAccountingInfoXMLBinding(listItem);
        voucherList.add(voucher);
      }

    }
    HmsAccountinngInfoList accountinglist = new HmsAccountinngInfoList();
    accountinglist.setAccountingList(voucherList);
    return accountinglist;
  }

  /**
   * Update status.
   *
   * @param model
   *          the model
   * @param req
   *          the req
   * @param response
   *          the response
   * @return the response entity
   */
  @RequestMapping(value = { "/accounting/exportstatus/update" }, method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> updateStatus(Model model, HttpServletRequest req,
      HttpServletResponse response) {
    Map<String, String[]> reqMap = req.getParameterMap();
    boolean result = false;
    if (null != reqMap && reqMap.size() > 0) {
      String[] guid = reqMap.get("guid");
      String[] newStatus = reqMap.get("new_status");

      // Validate input
      if (null == guid || guid.length <= 0 || guid[0].trim().isEmpty()) {
        throw new ValidationException("exception.api.accounting.invalid.guid", new String[] {});
      }
      if (null == newStatus || newStatus.length <= 0 || newStatus[0].trim().isEmpty()) {
        throw new ValidationException("exception.api.accounting.missing.new.status",
            new String[] {});
      }

      Integer status = Integer.valueOf(newStatus[0]);

      if (status != 1) {
        throw new ValidationException("exception.api.accounting.invalid.new.status",
            new String[] { newStatus[0] });
      }

      // Finally update the status
      result = service.updateVoucherStatus(guid[0], status);
    }
    return prepareSuccessResponse(null, result);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.extension.billing.AccountingBaseController#preprocessParams(java.util.Map)
   */
  @Override
  protected Map<String, String[]> preprocessParams(Map<String, String[]> reqMap) {
    Map<String, String[]> map = new LinkedHashMap<String, String[]>();

    String[] accounts = reqMap.get("account_group_id");
    String[] centers = reqMap.get("center_id");

    String accountGroupId = null;
    String centerId = null;
    if (null != accounts && accounts.length > 0 && null != accounts[0] && !accounts[0].isEmpty()) {
      accountGroupId = accounts[0].trim();
    }

    if (null != centers && centers.length > 0 && !centers[0].isEmpty()) {
      centerId = centers[0].trim();
    }

    if (null != accountGroupId) { // account group takes precedence over center.
      map.put("d_account_group", new String[] { accountGroupId });
      map.put("d_account_group@type", new String[] { "integer" });
    } else if (null != centerId) {
      map.put("center_id", new String[] { centerId });
      map.put("center_id@type", new String[] { "integer" });
      // to ensure that pharmacy bills belonging to a different account group does not end up along
      // with
      // other vouchers for the same center.
      map.put("d_account_group", new String[] { "1" });
      map.put("d_account_group@type", new String[] { "integer" });
    } else {
      throw new ValidationException("exception.api.accounting.missing.account.group",
          new String[] {});
    }

    String[] fromDates = reqMap.get("from_date");
    String[] toDates = reqMap.get("to_date");
    // Note : It is very funny how the servlet gives us a read-only parameter map (only with respect
    // to keys) but allows us to override the values in the map by reference. So if we do not clone 
    // the array here, the original parameter value is overwritten and shows up as such in the UI 
    // when the page is reloaded with results.
    // Not sure, if this is a bug in apache or spring... but cloning the array so that integrity of
    // the request parameter map is maintained.
    String[] dates = new String[2];
    if (null != fromDates && fromDates.length > 0 && !fromDates[0].isEmpty()) {
      try {
        dates[0] = DateUtil.formatSqlTimestamp(dateUtil.parseTheTimestamp(fromDates[0]));
      } catch (ParseException exception) {
        throw new ValidationException("ui.error.date.accounting.invalid", 
            new String[] {fromDates[0], "from_date"});
      }
    } else {
      throw new ValidationException("exception.single.field.required.placeholder", 
          new String[] {"from_date"});
    }
    // TODO : This qualifies to go into DateUtil as getNextZeroHours()
    if (null != toDates && toDates.length > 0 && !toDates[0].isEmpty()) { 
      // to date is specified
      String dt = toDates[0];
      if (dt.trim().length() <= 10) {
        dt = dt + " 23:59:59.999999";
      }
      try {
        dates[1] = DateUtil.formatSqlTimestamp(dateUtil.parseTheTimestamp(dt));
      } catch (ParseException exception) {
        throw new ValidationException("ui.error.date.accounting.invalid", 
            new String[] {toDates[0], "to_date"});
      }
    }

    String[] filterByArr = reqMap.get("filter_by");
    String filterBy = filterByArr != null && filterByArr.length > 0 
        ? filterByArr[0] : "voucher_date";

    if (!VALID_FILTERBY.contains(filterBy)) {
      throw new ValidationException("ui.error.parameter.accounting.invalid", 
          new String[] {filterBy, "filter_by"});
    }

    if (null != dates) {
      map.put(filterBy + "@op", DATE_OPS);
      map.put(filterBy + "@type", new String[] { "timestamp" });
      map.put(filterBy, dates);
    }

    String[] vtypes = reqMap.get("voucher_type");
    if (null != vtypes) {
      map.put("voucher_type", vtypes);
    }

    String[] updateStatus = reqMap.get("update_status");

    if (null != updateStatus && updateStatus.length > 0 && !updateStatus[0].trim().isEmpty()) {
      map.put("update_status", new String[] { updateStatus[0] });
      map.put("update_status@type", new String[] { "integer" });
    }
    String[] pageSize = ObjectUtils.firstNonNull(reqMap.get("pageSize"), reqMap.get("page_size"));
    if (ArrayUtils.isNotEmpty(pageSize)) {
      map.put("pageSize", pageSize);
    }
    String[] pageNum = ObjectUtils.firstNonNull(reqMap.get("pageNum"), reqMap.get("page_num"));
    if (ArrayUtils.isNotEmpty(pageNum)) {
      map.put("pageNum", pageNum);
    }
    return map;
  }

  /**
   * Gets the reference data.
   *
   * @param action
   *          the action
   * @param reqMap
   *          the req map
   * @return the reference data
   */
  protected List<Object> getReferenceData(String action, Map<String, String[]> reqMap) {
    List<Object> referenceDataList = new ArrayList<Object>();
    Map<String, String> voucherTypeMap = service.getVoucherTypes();
    List<BasicDynaBean> accountGroups = service.getAccountGroups();
    List accountGroupMap = ConversionUtils.listBeanToListMap(accountGroups);
    referenceDataList.add(voucherTypeMap);
    referenceDataList.add(accountGroupMap);
    return referenceDataList;
  }

  /**
   * Handle validation exception.
   *
   * @param validationException
   *          the validation exception
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the response entity
   */
  @ExceptionHandler(HMSException.class)
  public ResponseEntity<Map<String, Object>> handleValidationException(
      HMSException validationException, HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> responseMap = prepareErrorMesageMap(validationException);
    return new ResponseEntity<Map<String, Object>>(responseMap, validationException.getStatus());
  }

  /**
   * Handle validation exception.
   *
   * @param exception
   *          the exception
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the response entity
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleValidationException(Exception exception,
      HttpServletRequest request, HttpServletResponse response) {
    logger.error("Exception while validating API Request ", exception);
    Map<String, Object> responseMap = new HashMap<String, Object>();
    HMSErrorResponse error = new HMSErrorResponse(exception.toString());
    responseMap.put("return_message", error.getDisplayMessage());
    responseMap.put("return_code", error.getStatus());
    return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Prepare error mesage map.
   *
   * @param ex
   *          the ex
   * @return the map
   */
  private Map<String, Object> prepareErrorMesageMap(HMSException ex) {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    responseMap.put("return_code", RETURN_CODE_INVALID_DATA);
    responseMap.put("return_message", messageUtil.getMessage(ex.getMessageKey(), ex.getParams()));
    return responseMap;
  }

  /**
   * Prepare success response.
   *
   * @param view
   *          the view
   * @param hasContent
   *          the has content
   * @return the response entity
   */
  private ResponseEntity<Map<String, Object>> prepareSuccessResponse(ModelAndView view,
      boolean hasContent) {
    HttpStatus status = HttpStatus.OK;
    if (null == view) {
      view = new ModelAndView();
    }
    if (hasContent) {
      view.addObject("return_code", RETURN_CODE_SUCCESS);
      view.addObject("return_message", RETURN_MSG_SUCCESS);
    } else {
      view.addObject("return_code", RETURN_CODE_NODATA);
      view.addObject("return_message", RETURN_MSG_NODATA);
    }
    return new ResponseEntity<Map<String, Object>>(view.getModel(), status);
  }
}