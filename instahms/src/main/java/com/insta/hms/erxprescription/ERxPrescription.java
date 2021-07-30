/**
 *
 */
package com.insta.hms.erxprescription;

import com.insta.hms.eservice.EResult;

/**
 * @author lakshmi
 *
 */
public class ERxPrescription extends ERxRequest implements EResult {

  // System generated id: consultation_id or Visit Id since ERx request is from consultation or IPEMR
  private Object requestId;

  public Object getRequestId() {
    return requestId;
  }

  public void setRequestId(Object requestId) {
    this.requestId = requestId;
  }

  private Object header;
  private EPrescription prescription;

  public Object getHeader() {
    return header;
  }

  public void setHeader(Object header) {
    this.header = header;
  }

  public EPrescription getPrescription() {
    return prescription;
  }

  public void setPrescription(EPrescription prescription) {
    this.prescription = prescription;
  }
}
