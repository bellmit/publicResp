/**
 *
 */
package com.insta.hms.pbmauthorization;

/**
 * @author lakshmi
 *
 */
public class PriorActivityObservation {

	private String type;
	private String code;
	private String value;
	private String valueType;

	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getValueType() {
		return valueType;
	}
	public void setValueType(String valueType) {
		this.valueType = valueType;
	}
}
