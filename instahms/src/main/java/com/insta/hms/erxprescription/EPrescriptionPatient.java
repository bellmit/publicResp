/**
 *
 */
package com.insta.hms.erxprescription;

/**
 * @author lakshmi
 *
 */
public class EPrescriptionPatient {

  private String memberId;
  private String emiratesIDNumber;
  private String dateOfBirth; // Date form dd/mm/yyyy
  private int weight; // The patient's weight in kilograms (Kg)
  private String email;

  public String getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(String dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getEmiratesIDNumber() {
    return emiratesIDNumber;
  }

  public void setEmiratesIDNumber(String emiratesIDNumber) {
    this.emiratesIDNumber = emiratesIDNumber;
  }

  public String getMemberId() {
    return memberId;
  }

  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }
}
