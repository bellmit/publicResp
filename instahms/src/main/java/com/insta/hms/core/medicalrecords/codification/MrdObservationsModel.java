package com.insta.hms.core.medicalrecords.codification;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The Class MrdObservationsModel.
 */
@Entity
@Table(name = "mrd_observations")
public class MrdObservationsModel implements java.io.Serializable {

  /** The observation id. */
  private int observationId;

  /** The charge id. */
  private String chargeId;

  /** The observation type. */
  private String observationType;

  /** The code. */
  private String code;

  /** The value. */
  private String value;

  /** The value type. */
  private String valueType;

  /** The value editable. */
  private String valueEditable;

  /** The sponsor id. */
  private String sponsorId;

  /** The document id. */
  private Integer documentId = 0;

  /**
   * Instantiates a new mrd observations model.
   */
  public MrdObservationsModel() {
  }

  /**
   * Instantiates a new mrd observations model.
   *
   * @param observationId
   *          the observation id
   */
  public MrdObservationsModel(int observationId) {
    this.observationId = observationId;
  }

  /**
   * Instantiates a new mrd observations model.
   *
   * @param observationId
   *          the observation id
   * @param chargeId
   *          the charge id
   * @param observationType
   *          the observation type
   * @param code
   *          the code
   * @param value
   *          the value
   * @param valueType
   *          the value type
   * @param valueEditable
   *          the value editable
   * @param sponsorId
   *          the sponsor id
   */
  public MrdObservationsModel(int observationId, String chargeId,
      String observationType, String code, String value, String valueType,
      String valueEditable, String sponsorId) {
    this.observationId = observationId;
    this.chargeId = chargeId;
    this.observationType = observationType;
    this.code = code;
    this.value = value;
    this.valueType = valueType;
    this.valueEditable = valueEditable;
    this.sponsorId = sponsorId;
  }

  /**
   * Gets the observation id.
   *
   * @return the observation id
   */
  @Id
  @Column(name = "observation_id", unique = true, nullable = false)
  public int getObservationId() {
    return this.observationId;
  }

  /**
   * Sets the observation id.
   *
   * @param observationId
   *          the new observation id
   */
  public void setObservationId(int observationId) {
    this.observationId = observationId;
  }

  /**
   * Gets the charge id.
   *
   * @return the charge id
   */
  @Column(name = "charge_id", length = 15)
  public String getChargeId() {
    return this.chargeId;
  }

  /**
   * Sets the charge id.
   *
   * @param chargeId
   *          the new charge id
   */
  public void setChargeId(String chargeId) {
    this.chargeId = chargeId;
  }

  /**
   * Gets the observation type.
   *
   * @return the observation type
   */
  @Column(name = "observation_type", length = 50)
  public String getObservationType() {
    return this.observationType;
  }

  /**
   * Sets the observation type.
   *
   * @param observationType
   *          the new observation type
   */
  public void setObservationType(String observationType) {
    this.observationType = observationType;
  }

  /**
   * Gets the code.
   *
   * @return the code
   */
  @Column(name = "code", length = 50)
  public String getCode() {
    return this.code;
  }

  /**
   * Sets the code.
   *
   * @param code
   *          the new code
   */
  public void setCode(String code) {
    this.code = code;
  }

  /**
   * Gets the value.
   *
   * @return the value
   */
  @Column(name = "value")
  public String getValue() {
    return this.value;
  }

  /**
   * Sets the value.
   *
   * @param value
   *          the new value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Gets the value type.
   *
   * @return the value type
   */
  @Column(name = "value_type")
  public String getValueType() {
    return this.valueType;
  }

  /**
   * Sets the value type.
   *
   * @param valueType
   *          the new value type
   */
  public void setValueType(String valueType) {
    this.valueType = valueType;
  }

  /**
   * Gets the value editable.
   *
   * @return the value editable
   */
  @Column(name = "value_editable", length = 1)
  public String getValueEditable() {
    return this.valueEditable;
  }

  /**
   * Sets the value editable.
   *
   * @param valueEditable
   *          the new value editable
   */
  public void setValueEditable(String valueEditable) {
    this.valueEditable = valueEditable;
  }

  /**
   * Gets the sponsor id.
   *
   * @return the sponsor id
   */
  @Column(name = "sponsor_id")
  public String getSponsorId() {
    return this.sponsorId;
  }

  /**
   * Sets the sponsor id.
   *
   * @param sponsorId
   *          the new sponsor id
   */
  public void setSponsorId(String sponsorId) {
    this.sponsorId = sponsorId;
  }

  /**
   * Gets the document id.
   *
   * @return the document id
   */
  public Integer getDocumentId() {
    return documentId;
  }

  /**
   * Sets the document id.
   *
   * @param documentId
   *          the new document id
   */
  public void setDocumentId(Integer documentId) {
    if (documentId == null) {
      this.documentId = 0;
    } else {
      this.documentId = documentId;
    }
  }

}
