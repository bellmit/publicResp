package com.insta.hms.integration;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.integration.patientsync.PatientRecord;
import com.insta.hms.integration.patientsyncresponse.PatientRecordResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

import javax.servlet.http.HttpServletResponse;

/**
 * The Class SyncExternalPatientController.
 */
@RestController
@RequestMapping("/syncexternalpatient")
public class SyncExternalPatientController extends BaseRestController {

  /** The insta integration service. */
  @LazyAutowired
  private InstaIntegrationService instaIntegrationService;

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(SyncExternalPatientController.class);

  /**
   * Sync external patient.
   *
   * @param patientRecord the patient record
   * @param response the response
   * @return the patient record response
   * @throws ParseException the parse exception
   */
  @RequestMapping(value = "/preregister", method = RequestMethod.POST,
      produces = MediaType.APPLICATION_XML_VALUE, consumes = MediaType.APPLICATION_XML_VALUE)
  public PatientRecordResponse syncExternalPatient(@RequestBody PatientRecord patientRecord,
      HttpServletResponse response) throws ParseException {
    logger.info("Syncexternalpatient API");
    PatientRecordResponse patientResponse = instaIntegrationService
        .syncExternalPatient(patientRecord, response);

    return patientResponse;
  }
}
