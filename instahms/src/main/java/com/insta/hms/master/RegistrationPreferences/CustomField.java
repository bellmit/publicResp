/**
 *
 */
package com.insta.hms.master.RegistrationPreferences;

/**
 * @author krishna
 *
 */
public class CustomField {

	public String label;
	public String required;
	public String show; // values are primary and secondary,depending on we will show this field in registration screen(directly in Reg screen or in more dialog).
	public String display;
	public String showInClinical; // show this field in patient demography in clinical screens.
	public String showInOther; // show this field in patient demography in other screen(which are not clinical)
	public String value;
	public String txColumnName; // custom field column name in transaction table(ex: column name in patient_details table)

	public void setLabel(String label) {
		this.label = label;
	}
	public String getLabel() {
		return label;
	}
	public void setRequired(String required) {
		this.required = required;
	}
	public String getRequired() {
		return required;
	}
	public void setDisplay(String display) {
		this.display = display;
	}
	public String getDisplay() {
		return display;
	}

	public void setValue(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
	public String getShowInClinical() {
		return showInClinical;
	}
	public void setShowInClinical(String showInClinical) {
		this.showInClinical = showInClinical;
	}
	public String getShowInOther() {
		return showInOther;
	}
	public void setShowInOther(String showInOther) {
		this.showInOther = showInOther;
	}
	public String getTxColumnName() {
		return txColumnName;
	}
	public void setTxColumnName(String txColumnName) {
		this.txColumnName = txColumnName;
	}
	public String getShow() {
		return show;
	}
	public void setShow(String show) {
		this.show = show;
	}


}
