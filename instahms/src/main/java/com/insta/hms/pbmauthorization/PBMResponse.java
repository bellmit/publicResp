/**
 *
 */
package com.insta.hms.pbmauthorization;

public class PBMResponse {
	private int responseCode;
	private String errorMessage;
	private byte[] errorReport;

	public PBMResponse(int responseCode, String errorMessage, byte[] errorReport) {
		super();
		this.responseCode = responseCode;
		this.errorMessage = errorMessage;
		this.errorReport = errorReport;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public byte[] getErrorReport() {
		return errorReport;
	}

	public int getResponseCode() {
		return responseCode;
	}
}