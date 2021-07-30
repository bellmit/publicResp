/**
 *
 */
package com.insta.hms.vitalForm;

import java.sql.Timestamp;

/**
 * @author krishna
 *
 */
public class VitalsReadings {
	private int paramId;
	private String paramValue;
	private String paramUOM;
	private String paramLabel;
	private Timestamp dateTime;
	private String colorCode;
	private String paramRemarks;



	public String getColorCode() {
		return colorCode;
	}
	public void setColorCode(String colorCode) {
		this.colorCode = colorCode;
	}
	public void setParamId(int paramId) {
		this.paramId = paramId;
	}
	public int getParamId() {
		return paramId;
	}
	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}
	public String getParamValue() {
		return paramValue;
	}
	public void setParamUOM(String paramUOM) {
		this.paramUOM = paramUOM;
	}
	public String getParamUOM() {
		return paramUOM;
	}
	public void setParamLabel(String paramLabel) {
		this.paramLabel = paramLabel;
	}
	public String getParamLabel() {
		return paramLabel;
	}
	
	public String getParamRemarks() {
		return paramRemarks;
	}
	
	public void setParamRemarks(String paramRemarks) {
		this.paramRemarks = paramRemarks;
	}
}
