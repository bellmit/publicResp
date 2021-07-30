package com.insta.hms.mdm.bulk;

import java.util.Map;

/**
 * The Class BulkDataLookupEntity for maintaining lookups in Bulk Data operations.
 * 
 * @author tanmay.k
 */
public class BulkDataLookupEntity {

  /** The field. */
  private String field;

  /** The referenced id field. */
  private String referencedIdField;

  /** The error message. */
  private String errorMessage;

  /** The map. */
  private Map<?, ?> map;

  /**
   * Instantiates a new bulk data lookup entity.
   *
   * @param field
   *          the field
   * @param idField
   *          the referenced id field
   * @param map
   *          the map
   * @param errorMessage
   *          the error message
   */
  public BulkDataLookupEntity(String field, String idField, Map<?, ?> map, String errorMessage) {
    this.field = field;
    this.referencedIdField = idField;
    this.map = map;
    this.errorMessage = errorMessage;
  }

  /**
   * Gets the field.
   *
   * @return the field
   */
  public String getField() {
    return field;
  }

  /**
   * Gets the referenced id field.
   *
   * @return the referenced id field
   */
  public String getReferencedIdField() {
    return referencedIdField;
  }

  /**
   * Gets the error message.
   *
   * @return the error message
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Gets the map.
   *
   * @return the map
   */
  public Map<?, ?> getMap() {
    return map;
  }
}
