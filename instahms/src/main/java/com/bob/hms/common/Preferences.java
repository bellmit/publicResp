package com.bob.hms.common;

/*
 * Simple DTO like object to store all preferences specific to the hospital.
 * This object will be stored in the session as soon as the user logs in.
 */
import java.io.Serializable;
import java.util.HashMap;

// TODO: Auto-generated Javadoc
/**
 * The Class Preferences.
 */
@SuppressWarnings("serial")
public class Preferences implements Serializable {

  private HashMap modulesActivatedMap;

  private int generalRegistrationCharge;
  private int ipRegistrationCharge;
  private int opRegistrationCharge;
  private int diagRegistrationCharge;
  private int emrRegistrationCharge;
  private int mlcRegistrationCharge;

  private int ipCredit;
  private int ipValidityDays;
  private int opValidityDays;
  private int opValidityPeriod;
  private int opConsultationValidity;
  private String opConsultationValidityType;
  private int grace;

  private int nightPm;
  private int nightAm;

  private int scda;
  private String receiptRequired;
  private boolean generalChargeCollect;
  private boolean ipOp;
  private boolean opIp;

  /*
   * private String reg_custom_field1_name; private String reg_custom_field2_name; private String
   * reg_custom_field3_name;
   */

  private String mrNoPrefix;
  private int mrNoDigits;
  private String billNoPrefix;
  private int billNoDigits;

  /**
   * Gets the modules activated map.
   *
   * @return the modules activated map
   */
  public HashMap getModulesActivatedMap() {
    return modulesActivatedMap;
  }

  /**
   * Sets the modules activated map.
   *
   * @param modulesActivated the new modules activated map
   */
  public void setModulesActivatedMap(HashMap modulesActivated) {
    modulesActivatedMap = modulesActivated;
  }

  /**
   * Gets the general registration charge.
   *
   * @return the general registration charge
   */
  public int getGeneralRegistrationCharge() {
    return generalRegistrationCharge;
  }

  /**
   * Sets the general registration charge.
   *
   * @param charge the new general registration charge
   */
  public void setGeneralRegistrationCharge(int charge) {
    generalRegistrationCharge = charge;
  }

  /**
   * Gets the ip registration charge.
   *
   * @return the ip registration charge
   */
  public int getIpRegistrationCharge() {
    return ipRegistrationCharge;
  }

  /**
   * Sets the ip registration charge.
   *
   * @param charge the new ip registration charge
   */
  public void setIpRegistrationCharge(int charge) {
    ipRegistrationCharge = charge;
  }

  /**
   * Gets the op registration charge.
   *
   * @return the op registration charge
   */
  public int getOpRegistrationCharge() {
    return opRegistrationCharge;
  }

  /**
   * Sets the op registration charge.
   *
   * @param charge the new op registration charge
   */
  public void setOpRegistrationCharge(int charge) {
    opRegistrationCharge = charge;
  }

  /**
   * Gets the diag registration charge.
   *
   * @return the diag registration charge
   */
  public int getDiagRegistrationCharge() {
    return diagRegistrationCharge;
  }

  /**
   * Sets the diag registration charge.
   *
   * @param charge the new diag registration charge
   */
  public void setDiagRegistrationCharge(int charge) {
    diagRegistrationCharge = charge;
  }

  /**
   * Gets the emr registration charge.
   *
   * @return the emr registration charge
   */
  public int getEmrRegistrationCharge() {
    return emrRegistrationCharge;
  }

  /**
   * Sets the emr registration charge.
   *
   * @param charge the new emr registration charge
   */
  public void setEmrRegistrationCharge(int charge) {
    emrRegistrationCharge = charge;
  }

  /**
   * Gets the mlc registration charge.
   *
   * @return the mlc registration charge
   */
  public int getMlcRegistrationCharge() {
    return mlcRegistrationCharge;
  }

  /**
   * Sets the mlc registration charge.
   *
   * @param charge the new mlc registration charge
   */
  public void setMlcRegistrationCharge(int charge) {
    mlcRegistrationCharge = charge;
  }

  /**
   * Gets the ip credit.
   *
   * @return the ip credit
   */
  public int getIpCredit() {
    return ipCredit;
  }

  /**
   * Sets the ip credit.
   *
   * @param charge the new ip credit
   */
  public void setIpCredit(int charge) {
    ipCredit = charge;
  }

  /**
   * Gets the ip validity days.
   *
   * @return the ip validity days
   */
  public int getIpValidityDays() {
    return ipValidityDays;
  }

  /**
   * Sets the ip validity days.
   *
   * @param charge the new ip validity days
   */
  public void setIpValidityDays(int charge) {
    ipValidityDays = charge;
  }

  /**
   * Gets the op validity days.
   *
   * @return the op validity days
   */
  public int getOpValidityDays() {
    return opValidityDays;
  }

  /**
   * Sets the op validity days.
   *
   * @param charge the new op validity days
   */
  public void setOpValidityDays(int charge) {
    opValidityDays = charge;
  }

  /**
   * Gets the op validity period.
   *
   * @return the op validity period
   */
  public int getOpValidityPeriod() {
    return opValidityPeriod;
  }

  /**
   * Sets the op validity period.
   *
   * @param charge the new op validity period
   */
  public void setOpValidityPeriod(int charge) {
    opValidityPeriod = charge;
  }

  /**
   * Gets the op consultation validity.
   *
   * @return the op consultation validity
   */
  public int getOpConsultationValidity() {
    return opConsultationValidity;
  }

  /**
   * Sets the op consultation validity.
   *
   * @param charge the new op consultation validity
   */
  public void setOpConsultationValidity(int charge) {
    opConsultationValidity = charge;
  }

  /**
   * Gets the op consultation validity type.
   *
   * @return the op consultation validity type
   */
  public String getOpConsultationValidityType() {
    return opConsultationValidityType;
  }

  /**
   * Sets the op consultation validity type.
   *
   * @param validity the new op consultation validity type
   */
  public void setOpConsultationValidityType(String validity) {
    opConsultationValidityType = validity;
  }

  /**
   * Gets the grace.
   *
   * @return the grace
   */
  public int getGrace() {
    return grace;
  }

  /**
   * Sets the grace.
   *
   * @param intGrace the new grace
   */
  public void setGrace(int intGrace) {
    grace = intGrace;
  }

  /**
   * Gets the scda.
   *
   * @return the scda
   */
  public int getScda() {
    return scda;
  }

  /**
   * Sets the scda.
   *
   * @param intscda the new scda
   */
  public void setScda(int intscda) {
    scda = intscda;
  }

  /**
   * Gets the night PM.
   *
   * @return the night PM
   */
  public int getNightPm() {
    return nightPm;
  }

  /**
   * Sets the night PM.
   *
   * @param charge the new night PM
   */
  public void setNightPm(int charge) {
    nightPm = charge;
  }

  /**
   * Gets the night AM.
   *
   * @return the night AM
   */
  public int getNightAm() {
    return nightAm;
  }

  /**
   * Sets the night AM.
   *
   * @param charge the new night AM
   */
  public void setNightAm(int charge) {
    nightAm = charge;
  }

  /**
   * Gets the receipt required.
   *
   * @return the receipt required
   */
  public String getReceiptRequired() {
    return receiptRequired;
  }

  /**
   * Sets the receipt required.
   *
   * @param receipt the new receipt required
   */
  public void setReceiptRequired(String receipt) {
    receiptRequired = receipt;
  }

  /**
   * Gets the general charge collect.
   *
   * @return the general charge collect
   */
  public boolean getGeneralChargeCollect() {
    return generalChargeCollect;
  }

  /**
   * Sets the general charge collect.
   *
   * @param charge the new general charge collect
   */
  public void setGeneralChargeCollect(boolean charge) {
    generalChargeCollect = charge;
  }

  /**
   * Gets the ip OP.
   *
   * @return the ip OP
   */
  public boolean getIpOp() {
    return ipOp;
  }

  /**
   * Sets the ip OP.
   *
   * @param isIpOp the new ip OP
   */
  public void setIpOp(boolean isIpOp) {
    ipOp = isIpOp;
  }

  /**
   * Gets the op IP.
   *
   * @return the op IP
   */
  public boolean getOpIp() {
    return opIp;
  }

  /**
   * Sets the op IP.
   *
   * @param charge the new op IP
   */
  public void setOpIp(boolean charge) {
    opIp = charge;
  }

  /**
   * Gets the mr no prefix.
   *
   * @return the mr no prefix
   */
  public String getMrNoPrefix() {
    return mrNoPrefix;
  }

  /**
   * Sets the mr no prefix.
   *
   * @param mrPrefix the new mr no prefix
   */
  public void setMrNoPrefix(String mrPrefix) {
    mrNoPrefix = mrPrefix;
  }

  /**
   * Gets the mr no digits.
   *
   * @return the mr no digits
   */
  public int getMrNoDigits() {
    return mrNoDigits;
  }

  /**
   * Sets the mr no digits.
   *
   * @param digitsMrNo the new mr no digits
   */
  public void setMrNoDigits(int digitsMrNo) {
    mrNoDigits = digitsMrNo;
  }

  /**
   * Gets the bill no prefix.
   *
   * @return the bill no prefix
   */
  public String getBillNoPrefix() {
    return billNoPrefix;
  }

  /**
   * Sets the bill no prefix.
   *
   * @param billNoPrefixString the new bill no prefix
   */
  public void setBillNoPrefix(String billNoPrefixString) {
    billNoPrefix = billNoPrefixString;
  }

  /**
   * Gets the bill no digits.
   *
   * @return the bill no digits
   */
  public int getBillNoDigits() {
    return billNoDigits;
  }

  /**
   * Sets the bill no digits.
   *
   * @param digitsBillNo the new bill no digits
   */
  public void setBillNoDigits(int digitsBillNo) {
    billNoDigits = digitsBillNo;
  }

  /*
   * public String getReg_custom_field1_name() { return reg_custom_field1_name; } public void
   * setReg_custom_field1_name(String v) { reg_custom_field1_name = charge; }
   * 
   * public String getReg_custom_field2_name() { return reg_custom_field2_name; } public void
   * setReg_custom_field2_name(String v) { reg_custom_field2_name = charge; }
   * 
   * public String getReg_custom_field3_name() { return reg_custom_field3_name; } public void
   * setReg_custom_field3_name(String v) { reg_custom_field3_name = charge; }
   */

}
