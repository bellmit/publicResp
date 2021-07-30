package com.insta.hms.api.controllers;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.HMSErrorResponse;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.extension.billing.AccountingBaseController;
import com.insta.hms.extension.billing.AccountingEventsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequestMapping("/api")
public class AccountingEventsController extends AccountingBaseController {

  @LazyAutowired
  AccountingEventsService service;

  @LazyAutowired
  MessageUtil messageUtil;

  private static final String RETURN_CODE_SUCCESS = "2001";
  private static final String RETURN_MSG_SUCCESS = "Success";
  private static final String RETURN_CODE_NODATA = "2004";
  private static final String RETURN_MSG_NODATA = "No matching records found";
  private static final String RETURN_CODE_INVALID_DATA = "1021";

  /**
   * Search.
   *
   * @param model    the model
   * @param req      the req
   * @param response the response
   * @return the response entity
   */
  @RequestMapping(value = { "/accounting/events/list" }, method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> search(Model model, HttpServletRequest req,
      HttpServletResponse response) {
    Map<String, String[]> reqMap = req.getParameterMap();
    List list = new ArrayList();
    if (null != reqMap && reqMap.size() > 0) {
      Map<String, String[]> paramMap = preprocessParams(reqMap);
      PagedList pagedList = service.search(paramMap);
      list = ConversionUtils.listBeanToListMap(pagedList.getDtoList());
    }
    AccountingModelAndView view = createView("search", new Object[] { null, list }, null);
    return prepareSuccessResponse(view, (null != list && list.size() > 0));
  }

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
    }

    String[] fromDates = reqMap.get("from_date");
    String[] toDates = reqMap.get("to_date");
    // Note : It is very funny how the servlet gives us a read-only parameter map (only with respect
    // to keys)
    // but allows us to override the values in the map by reference. So if we do not clone the array
    // here, the original
    // parameter value is overwritten and shows up as such in the UI when the page is reloaded with
    // results.
    // Not sure, if this is a bug in apache or spring... but cloning the array so that integrity of
    // the request parameter map is
    // maintained.
    String[] dates = null;
    if (null != fromDates && fromDates.length > 0 && !fromDates[0].isEmpty()) {
      if (null == dates) {
        dates = new String[2];
      }
      dates[0] = fromDates[0];
    } else {
      throw new ValidationException("exception.single.field.required.placeholder", 
          new String[] {"from_date"});
    }
    // TODO : This qualifies to go into DateUtil as getNextZeroHours()
    if (null != toDates && toDates.length > 0 && !toDates[0].isEmpty()) { // to date is specified
      java.sql.Timestamp toDate;
      try {
        toDate = new DateUtil().parseTheTimestamp(toDates[0]);
      } catch (ParseException ex) {
        toDate = new Timestamp(new Date().getTime());
      }
      java.sql.Timestamp nextDay = DateUtil.addDays(toDate, 1);

      if (null == dates) {
        dates = new String[2];
      }
      // Assumption here is that the incoming parameter for toDate does not carry any time value,
      // but only date value
      dates[1] = DateUtil.formatTimestamp(nextDay);
    }

    if (null != dates) {
      map.put("event_date_time@type", new String[] { "date" });
      map.put("event_date_time@op", new String[] { "ge,lt" });
      map.put("event_date_time", dates);
    }

    String[] etypes = reqMap.get("event_type");

    if (null != etypes) {
      map.put("_event_type", etypes);
    }

    return map;
  }

  protected List<Object> getReferenceData(String action, Map<String, String[]> reqMap) {
    List<Object> referenceDataList = new ArrayList<Object>();
    Map<String, String> eventTypeMap = service.getEventTypes();
    List<BasicDynaBean> accountGroups = service.getAccountGroups();
    List accountGroupMap = ConversionUtils.listBeanToListMap(accountGroups);
    referenceDataList.add(eventTypeMap);
    referenceDataList.add(accountGroupMap);
    return referenceDataList;
  }

  private Map<String, Object> prepareErrorMesageMap(HMSException ex) {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    responseMap.put("return_code", RETURN_CODE_INVALID_DATA);
    responseMap.put("return_message", messageUtil.getMessage(ex.getMessageKey(), ex.getParams()));
    return responseMap;
  }

  @ExceptionHandler(HMSException.class)
  public ResponseEntity<Map<String, Object>> handleValidationException(
      HMSException validationException, HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> responseMap = prepareErrorMesageMap(validationException);
    return new ResponseEntity<Map<String, Object>>(responseMap, validationException.getStatus());
  }

  /**
   * Handle validation exception.
   *
   * @param exception the exception
   * @param request   the request
   * @param response  the response
   * @return the response entity
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleValidationException(Exception exception,
      HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    HMSErrorResponse error = new HMSErrorResponse(exception.toString());
    responseMap.put("return_message", error.getDisplayMessage());
    responseMap.put("return_code", error.getStatus());
    return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.INTERNAL_SERVER_ERROR);
  }

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
