package com.insta.hms.core.clinical.forms;

/**
 * The Class FormParameter.
 *
 * @author krishnat
 */
public class FormParameter {

  /** The mr no. */
  private String mrNo;

  /** The patient id. */
  private String patientId;

  /** The id. */
  private Object id;

  /** The item type. */
  private String itemType;

  /** The form type. */
  private String formType;

  /** The form field name. */
  private String formFieldName;

  /**
   * Instantiates a new form parameter.
   *
   * @param formType the form type
   * @param itemType the item type
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param id the id
   * @param formFieldName the form field name
   */
  public FormParameter(String formType, String itemType, String mrNo, String patientId, Object id,
      String formFieldName) {
    this.formType = formType;
    this.itemType = itemType;
    this.mrNo = mrNo;
    this.patientId = patientId;
    this.id = id;
    this.formFieldName = formFieldName;
  }

  /**
   * Gets the mr no.
   *
   * @return the mr no
   */
  public String getMrNo() {
    return this.mrNo;
  }

  /**
   * Gets the patient id.
   *
   * @return the patient id
   */
  public String getPatientId() {
    return this.patientId;
  }

  /**
   * Gets the form type.
   *
   * @return the form type
   */
  public String getFormType() {
    return this.formType;
  }

  /**
   * Gets the item type.
   *
   * @return the item type
   */
  public String getItemType() {
    return this.itemType;
  }

  /**
   * Gets the form field name.
   *
   * @return the form field name
   */
  public String getFormFieldName() {
    return formFieldName;
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public Object getId() {
    return this.id;
  }

}
