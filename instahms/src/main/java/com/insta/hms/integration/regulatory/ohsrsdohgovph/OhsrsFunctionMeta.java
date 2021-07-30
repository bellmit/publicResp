package com.insta.hms.integration.regulatory.ohsrsdohgovph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OhsrsFunctionMeta {

  private String key;
  private String label;
  private String representation;
  private String uploadable;
  private String groupBy;
  private List<OhsrsFunctionFieldMeta> fields;
  
  public static String REPRESENTATION_SIMPLE = "simple";
  public static String REPRESENTATION_TABLE = "table";
  public static String UPLOADABLE_ALWAYS = "always";
  public static String UPLOADABLE_NEVER = "never";
  public static String UPLOADABLE_FIRST_YEAR = "firstyear";
  
  public String getRepresentation() {
    return representation;
  }
  
  public void setRepresentation(String representation) {
    this.representation = representation;
  }

  public String getUploadable() {
    return uploadable;
  }
  
  public void setUploadable(String uploadable) {
    this.uploadable = uploadable;
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
  
  public String getGroupBy() {
    return groupBy;
  }
  
  public void setGroupBy(String groupBy) {
    this.groupBy = groupBy;
  }
  
  public void setFields(List<OhsrsFunctionFieldMeta> list) {
    this.fields = list;
  }
  
  public List<OhsrsFunctionFieldMeta> getFields() {
    return fields;
  }
  
  /**
   * Get fields as map where each key represents field key.
   * @return Map of fields
   */
  public Map<String, OhsrsFunctionFieldMeta> getFieldsAsMap() {
    Map<String, OhsrsFunctionFieldMeta> map = new HashMap<>();
    for (OhsrsFunctionFieldMeta field : fields) {
      map.put(field.getKey(), field);
    }
    return map;
  }
}
