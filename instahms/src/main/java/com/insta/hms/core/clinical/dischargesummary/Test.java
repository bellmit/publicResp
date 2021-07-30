package com.insta.hms.core.clinical.dischargesummary;

import java.util.ArrayList;
import java.util.List;

/**
 * @author anup vishwas.
 *
 */
public class Test {

  private int prescribedId;
  private String testName;
  private String notes;
  private String format;
  private List testValues = new ArrayList();

  /**
   * Constructr.
   * 
   * @param prescribedId the int
   * @param testName the string
   * @param notes the string
   * @param format the string
   */
  public Test(int prescribedId, String testName, String notes, String format) {
    this.prescribedId = prescribedId;
    this.testName = testName;
    this.notes = notes;
    this.format = format;
  }

  public List getTestValues() {
    return testValues;
  }

  public String getTestName() {
    return testName;
  }

  public int getPrescribedId() {
    return prescribedId;
  }

  public String getNotes() {
    return notes;
  }

  public String getFormat() {
    return format;
  }

}
