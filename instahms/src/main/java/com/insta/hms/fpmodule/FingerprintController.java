package com.insta.hms.fpmodule;

import com.bob.hms.common.MimeTypeDetector;
import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.registration.RegistrationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * API controllers for fingerprint module.
 * 
 */
@RestController
@RequestMapping("/fpModule")
public class FingerprintController extends BaseRestController {

  @LazyAutowired
  private RegistrationService registrationService;

  @LazyAutowired
  private FingerprintService fingerprintService;

  /**
   * Get details of fingers registered for MrNo.
   * 
   * @param mrNo MR Number
   * @return All registered fingers for the mrNo
   * @throws SQLException the SQL exception
   */
  @GetMapping("/getFinger")
  public ResponseEntity getFinger(@RequestParam("mr_no") String mrNo) throws SQLException {
    return new ResponseEntity<List<String>>(fingerprintService.getFingerByMrNo(mrNo),
        HttpStatus.OK);
  }

  /**
   * Compare the sent fingerprint with the saved fingerprint of the patient.
   * 
   * @param mrNo    MR Number
   * @param file    The PNG to be compared
   * @param purpose purpose of verification
   * @param finger  which finger to compare to
   * @param visitId Visit ID
   * @param token   Authentication token
   * @return If the fingerprint image sent matched the one which was registered
   * @throws SQLException the SQL exception
   * @throws IOException   Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/compare", method = RequestMethod.POST, 
      consumes = "multipart/form-data", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity compareFingerprints(@RequestParam("mr_no") String mrNo,
      @RequestParam("file") MultipartFile file, @RequestParam("purpose") String purpose,
      @RequestParam("finger") String finger, @RequestParam("visit_id") String visitId,
      @RequestParam("token") String token) throws SQLException, IOException {

    Map<String, Boolean> result = new HashMap<String, Boolean>();
    result.put("verified",
        fingerprintService.compareFingerprint(mrNo, file, purpose, finger, visitId, token));

    return new ResponseEntity<Map<String, Boolean>>(result, HttpStatus.OK);
  }

  /**
   * Controller to add fingerprints for the given MR number.
   * 
   * @param mrNo   MR Number
   * @param file   The PNG to be stored for future verification.
   * @param finger Finger name
   * @return Http status 201 if successful
   * @throws SQLException the SQL exception
   * @throws IOException   Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/addFingerprint", method = RequestMethod.POST,
      consumes = "multipart/form-data", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity addFingerprint(@RequestParam("mr_no") String mrNo,
      @RequestParam("file") MultipartFile file, @RequestParam("finger") String finger)
      throws SQLException, IOException {

    byte[] fp = null;
    if (!file.isEmpty()) {
      fp = file.getBytes();
    }
    boolean done = fingerprintService.addFingerprint(mrNo, fp, finger);

    return new ResponseEntity<Boolean>(HttpStatus.CREATED);
  }

  /**
   * Delete fingerprints of the patient.
   * 
   * @param mrNo   MR Number
   * @param finger the finger
   * @return Http status 200 if successful
   * @throws SQLException the SQL exception
   */
  @GetMapping("/deleteFingerprint")
  public ResponseEntity deleteFingerprint(@RequestParam("mr_no") String mrNo,
      @RequestParam("finger") String finger) throws SQLException {
    boolean result = fingerprintService.deleteFingerprintByMrNo(mrNo, finger);
    if (result) {
      return new ResponseEntity<Boolean>(HttpStatus.OK);
    }
    return new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND);
  }

  /**
   * Get all possible purposes for verification.
   * 
   * @return Purposes to be shown in verification screen
   * @throws SQLException the SQL exception
   */
  @GetMapping("/getAllPurpose")
  public ResponseEntity getAllPurpose() throws SQLException {
    List<String> purposes = fingerprintService.getAllPurpose();

    return new ResponseEntity<List<String>>(purposes, HttpStatus.OK);
  }

  /**
   * Get the authentication token for verification for the given MR number.
   * 
   * @param mrNo MR Number
   * @return Authentication token
   * @throws SQLException the SQL exception
   * @throws IOException   Signals that an I/O exception has occurred.
   */
  @GetMapping("/compare")
  public ResponseEntity getTokenForCompareFingerprints(@RequestParam("mr_no") String mrNo)
      throws SQLException, IOException {

    return new ResponseEntity<Map<String, String>>(
        fingerprintService.createTokenForCompareFingerprints(mrNo), HttpStatus.OK);
  }

  /**
   * Api to get fingerprint image of the patient in PNG format.
   * 
   * @param mrNo     MR Number
   * @param response HttpServletResponse
   * @throws IOException   Signals that an I/O exception has occurred.
   */
  @GetMapping(value = "/getFingerprintImage")
  public void getFingerprintImage(@RequestParam("mr_no") String mrNo, HttpServletResponse response)
      throws IOException {

    InputStream image = fingerprintService.getFingerprintImage(mrNo);

    String contentType = MimeTypeDetector.getMimeUtil().getMimeTypes(image).toString();
    response.addHeader("Content-Type", contentType);

    OutputStream responseStream = null;
    try {
      responseStream = response.getOutputStream();
      StreamUtils.copy(image, responseStream);
    } finally {
      if (null != responseStream) {
        responseStream.close();
      }
    }
  }

  /**
   * Gets the active op vists.
   *
   * @param mrNo MR Number
   * @return the list of active op vists
   */
  @GetMapping("/getOpVisits")
  public Map<String, Object> getActiveOpVists(@RequestParam("mr_no") String mrNo) {
    Map<String, Object> visits = new HashMap<String, Object>();
    // getting only active op visits.
    boolean activeOnly = true;
    visits.put("visits", registrationService.getPatientVisits(mrNo, "o", activeOnly, false));
    return visits;
  }
}
