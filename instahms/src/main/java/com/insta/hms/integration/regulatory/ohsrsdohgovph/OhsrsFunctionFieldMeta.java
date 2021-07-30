package com.insta.hms.integration.regulatory.ohsrsdohgovph;

public class OhsrsFunctionFieldMeta {

  private String key;
  private String label;
  private String dataType;
  
  public static final String DATATYPE_AMOUNT = "amount";
  public static final String DATATYPE_STRING = "string";
  public static final String DATATYPE_LOOKUP = "string_lookup";
  public static final String DATATYPE_INTEGER = "integer";
  public static final String DATATYPE_DATE = "date";
  
  /**
   * Constructor.
   * @param key      Key for field
   * @param label    Label for field
   * @param dataType Data type of field.
   */
  public OhsrsFunctionFieldMeta(String key, String label, String dataType) {
    this.setKey(key);
    this.setLabel(label);
    this.setDataType(dataType);
  }
  
  public String getKey() {
    return key;
  }
  
  public void setKey(String key) {
    this.key = key;
  }

  public String getLabel() {
    return label;
  }
  
  public void setLabel(String label) {
    this.label = label;
  }

  public String getDataType() {
    return dataType;
  }
  
  public void setDataType(String dataType) {
    this.dataType = dataType;
  }
}
