/**
 *
 */
package com.insta.hms.pbmauthorization;

import java.math.BigDecimal;
import java.util.ArrayList;


/**
 * @author lakshmi
 *
 */
public class PriorAuthAuthorization {

	private String authorizationResult;
	private String authorizationID;
	private String authorizationIDPayer;
	private String denialCode;
	private String start;
	private String end;
	private BigDecimal limit;
	private String comments;

	private ArrayList<PriorAuthorizationActivity> activities;

	public PriorAuthAuthorization(){
		activities = new ArrayList<PriorAuthorizationActivity>();
	}
	public ArrayList<PriorAuthorizationActivity> getActivities() {
		return activities;
	}
	public void setActivities(ArrayList<PriorAuthorizationActivity> activities) {
		this.activities = activities;
	}
	public void addActivity(PriorAuthorizationActivity activity){
		activities.add(activity);
	}
	public String getAuthorizationID() {
		return authorizationID;
	}
	public void setAuthorizationID(String authorizationID) {
		this.authorizationID = authorizationID;
	}
	public String getAuthorizationIDPayer() {
		return authorizationIDPayer;
	}
	public void setAuthorizationIDPayer(String authorizationIDPayer) {
		this.authorizationIDPayer = authorizationIDPayer;
	}
	public String getAuthorizationResult() {
		return authorizationResult;
	}
	public void setAuthorizationResult(String authorizationResult) {
		this.authorizationResult = authorizationResult;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getDenialCode() {
		return denialCode;
	}
	public void setDenialCode(String denialCode) {
		this.denialCode = denialCode;
	}
	public String getEnd() {
		return end;
	}
	public void setEnd(String end) {
		this.end = end;
	}
	public BigDecimal getLimit() {
		return limit;
	}
	public void setLimit(BigDecimal limit) {
		this.limit = limit;
	}
	public String getStart() {
		return start;
	}
	public void setStart(String start) {
		this.start = start;
	}
}
