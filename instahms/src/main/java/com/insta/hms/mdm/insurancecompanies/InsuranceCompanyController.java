package com.insta.hms.mdm.insurancecompanies;

import com.bob.hms.common.MimeTypeDetector;
import com.insta.hms.common.BaseController;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** The Class InsuranceCompanyController. */
@Controller("insuranceCompanyController")
@RequestMapping(URLRoute.INSURANCE_COMPANY_MASTER_PATH)
public class InsuranceCompanyController extends BaseController {

  /** The insurance company service. */
  @LazyAutowired private InsuranceCompanyService insuranceCompanyService;
  // TODO: This doesn't extend MasterController because this is not fully
  // migrated.It needs to extend MasterController when migrating.
  // There is no action_id in insta-master-config.xml .
  // Remove the auth passthru and replace it with correct action_id.
  // Remove this loadDoc after migrating the code.

  /**
   * Gets the insurance document.
   *
   * @param request the request
   * @param response the response
   * @return the insurance document
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/getinsurancedocrules", method = RequestMethod.GET)
  public ResponseEntity<byte[]> getInsuranceDocument(
      HttpServletRequest request, HttpServletResponse response) throws IOException {
    String insCoId = request.getParameter("insurance_co_id");
    byte[] data = insuranceCompanyService.getInsuranceRulesDocument(insCoId);

    String contentType = MimeTypeDetector.getMimeUtil().getMimeTypes(data).toString();
    response.addHeader("Content-Type", contentType);

    OutputStream responseStream = null;
    try {
      responseStream = response.getOutputStream();
      StreamUtils.copy(data, responseStream);
    } finally {
      if (null != responseStream) {
        responseStream.close();
      }
    }
    return null;
  }

  /**
   * Get active insurance company list.
   *
   * @return list of active insurance companies
   */
  @GetMapping(value = "/list")
  public ResponseEntity<Map<String, Object>> getInsuranceCompanyList() {
    Map<String, Object> responseBody = insuranceCompanyService
        .getInsuranceCompanyList();
    return new ResponseEntity<>(responseBody, HttpStatus.OK);
  }
}
