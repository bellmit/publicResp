/**
 *
 */
package com.insta.hms.pbmauthorization;

/**
 * @author lakshmi
 *
 */
public class PriorAuthorization {

	private PriorAuthorizationHeader header;
	private PriorAuthAuthorization authorization;

	public PriorAuthorizationHeader getHeader() {
		return header;
	}
	public void setHeader(PriorAuthorizationHeader header) {
		this.header = header;
	}
	public PriorAuthAuthorization getAuthorization() {
		return authorization;
	}
	public void setAuthorization(PriorAuthAuthorization authorization) {
		this.authorization = authorization;
	}
}
