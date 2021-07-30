package com.insta.hms.core.inventory.issues;

import com.insta.hms.exception.HMSException;

public class PatientIssueException extends HMSException {

  public PatientIssueException(String messageKey) {
    super(messageKey);
  }

}
