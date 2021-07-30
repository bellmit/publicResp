package com.insta.hms.mdm.bulk;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.util.MultiValueMap;

public class CsvRowContext {
  Integer lineNumber;
  String[] headers;
  String[] row;
  BasicDynaBean bean;
  MultiValueMap<Object, Object> warnings;

  /**
   * Instantiates a new csv row context.
   *
   * @param lineNumber the line number
   * @param headers the csv headers
   * @param row the row
   * @param bean the bean
   * @param warnings the warnings
   */
  public CsvRowContext(Integer lineNumber, String[] headers, String[] row, BasicDynaBean bean,
      MultiValueMap<Object, Object> warnings) {
    this.lineNumber = lineNumber;
    this.headers = headers;
    this.row = row;
    this.bean = bean;
    this.warnings = warnings;
  }

  public Integer getLineNumber() {
    return lineNumber;
  }

  public String[] getHeaders() {
    return headers;
  }

  public String[] getRow() {
    return row;
  }

  public BasicDynaBean getBean() {
    return bean;
  }

  public MultiValueMap<Object, Object> getWarnings() {
    return warnings;
  }

}
