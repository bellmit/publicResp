/**
 *
 */
package com.insta.hms.pbmauthorization;

import com.insta.hms.eservice.EResponse;

/**
 * @author lakshmi
 *
 */
public class PBMAuthResponse extends EResponse {

	public PBMAuthResponse(int responseCode, String errorMessage, Object content) {
		super(responseCode, errorMessage, content);
	}

	public PBMAuthResponse(int responseCode, String errorMessage, Object content, Object[] extraParams) {
		super(responseCode, errorMessage, content, extraParams);
	}

	public boolean isEmptyTransactions() {
		return responseCode == 2;
	}
}
