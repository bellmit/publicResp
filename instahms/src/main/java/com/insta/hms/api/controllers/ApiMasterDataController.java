package com.insta.hms.api.controllers;

import com.insta.hms.api.models.APIMasterDataResponse;
import com.insta.hms.api.models.AccountingHeadsResponse;
import com.insta.hms.api.models.AccountingHeadsResponse.AccountingHead;
import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.masterdata.MasterdataService;
import com.insta.hms.core.masterdata.MasterdataService.ReturnCode;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.accountingheads.AccountingHeadsService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class APIMasterDataController.
 */
@Controller
@RequestMapping("/api/masterdata")
public class ApiMasterDataController extends BaseRestController {

  /** The masterdata service. */
  @LazyAutowired
  private MasterdataService masterdataService;

  /** The accounting head service. */
  @LazyAutowired
  AccountingHeadsService accountingHeadService;

  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;

  /**
   * Gets the scheduler master data.
   *
   * @param request  the request
   * @param response the response
   * @return the scheduler master data
   */
  @RequestMapping(value = "", method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  public ResponseEntity getSchedulerMasterData(HttpServletRequest request,
      HttpServletResponse response) {
    Map<String, Object> masterData = null;
    try {
      masterData = masterdataService.getMasterData(request.getParameterMap());
      masterData.put("ReturnCode", MasterdataService.ReturnCode.SUCCESS);
    } catch (ValidationException ve) {
      masterData = new HashMap<String, Object>();
      masterData.put("ReturnCode", MasterdataService.ReturnCode.FAILED);
    }
    APIMasterDataResponse masterDataResponse = new APIMasterDataResponse(masterData);
    return new ResponseEntity<>(masterData,getHTTPStatus(masterDataResponse));
  }

  /**
   * Gets the HTTP status.
   *
   * @param masterDataResponse the master data response
   * @return the HTTP status
   */
  public static HttpStatus getHTTPStatus(APIMasterDataResponse masterDataResponse) {
    Map<String, ReturnCode> returnCodeMap = ReturnCode.getReturnCodeMap();
    if (StringUtils.isNotBlank(masterDataResponse.return_code)) {
      ReturnCode returnCode = returnCodeMap.get(masterDataResponse.return_code);
      if (returnCode != null) {
        return HttpStatus.valueOf(returnCode.getHTTPResponseStatus());
      }
    }
    return HttpStatus.OK;
  }

  /**
   * Gets the accounting heads.
   *
   * @param request  the request
   * @param response the response
   * @return the accounting heads
   */
  @RequestMapping(value = "/accountingheads", method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
  public ResponseEntity<AccountingHeadsResponse> getAccountingHeads(HttpServletRequest request,
      HttpServletResponse response) {
    AccountingHeadsResponse accountingHeadsResponse = new AccountingHeadsResponse();
    try {
      Map<String, String[]> paramMap = accountingHeadService
          .preprocessParams(request.getParameterMap());
      List accountingHeadsList = accountingHeadService.search(paramMap).getDtoList();
      ;
      Iterator<Map<String, Object>> iterator = accountingHeadsList.iterator();
      while (iterator.hasNext()) {
        Map<String, Object> listItem = iterator.next();
        AccountingHead accountingHead = new AccountingHead(listItem);
        accountingHeadsResponse.addAccountingHead(accountingHead);
      }
      accountingHeadsResponse.postProcess();
    } catch (Exception ex) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return new ResponseEntity<>(accountingHeadsResponse, HttpStatus.OK);
  }
}
