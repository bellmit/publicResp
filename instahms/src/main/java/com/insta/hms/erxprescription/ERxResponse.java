package com.insta.hms.erxprescription;

import com.insta.hms.eservice.EResponse;

public class ERxResponse extends EResponse {

  public ERxResponse(int responseCode, String errorMessage, Object content) {
    this(responseCode, errorMessage, content, new Object[] {});
  }

  public ERxResponse(int responseCode, String errorMessage, Object content, Object[] extraParams) {
    super(responseCode, errorMessage, content, extraParams);
  }
}
