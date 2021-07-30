package com.insta.hms.pbmauthorization;

public class PBMResponseProcessor {

	public void processPriorAuthorization(PBMResponse response) {
		if (response.getResponseCode() >= 0) {

		} else {

			PriorAuthorizationHelper priorAuthHelper = new PriorAuthorizationHelper();
			String errorReportStr = priorAuthHelper.getErrorReportbase64String(response.getErrorReport()); // Encoded string
		}

	}

}
