/**
 *
 */
package com.insta.hms.erxprescription;

/**
 * @author lakshmi
 *
 */
public class EPrescriptionDiagnosis {

  private String type;
  private String code;
  private String diagnosis_type;

  public String getDiagnosis_type() {
    return diagnosis_type;
  }

  public void setDiagnosis_type(String diagnosis_type) {
    this.diagnosis_type = diagnosis_type;
  }

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
}
