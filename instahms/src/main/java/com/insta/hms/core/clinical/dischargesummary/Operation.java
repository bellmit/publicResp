package com.insta.hms.core.clinical.dischargesummary;

import java.util.ArrayList;
import java.util.List;

/**
 * @author anup vishwas.
 *
 */

public class Operation {

  private String operationName;
  private String notes;
  private String richTextContent;
  private List hvfValues = new ArrayList();
  private int prescribedId;
  private String format;

  public Operation(String operationName, int prescribedId) {
    this.operationName = operationName;
    this.prescribedId = prescribedId;
  }

  public String getOperationName() {
    return operationName;
  }

  public int getPrescribedId() {
    return prescribedId;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getFormat() {
    return format;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getNotes() {
    return notes;
  }

  public String getRichTextContent() {
    return richTextContent;
  }

  public void setRichTextContent(String richTextContent) {
    this.richTextContent = richTextContent;
  }

  public List getHvfValues() {
    return hvfValues;
  }

}
