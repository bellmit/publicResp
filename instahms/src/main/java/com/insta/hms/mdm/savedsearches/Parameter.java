package com.insta.hms.mdm.savedsearches;

/**
 * The Class Parameter.
 *
 * @author krishnat
 */
public class Parameter {

  /** The key. */
  private String key;

  /** The op. */
  private String op;

  /** The sql type. */
  private String sqlType;

  /** The cast. */
  private String cast;

  /** The multiple. */
  private Boolean multiple;

  /**
   * Instantiates a new parameter.
   *
   * @param key the key
   */
  public Parameter(String key) {
    this(key, null, null, null, false);
  }

  /**
   * Instantiates a new parameter.
   *
   * @param key the key
   * @param op the op
   * @param sqlType the sql type
   * @param cast the cast
   * @param multiple the multiple
   */
  public Parameter(String key, String op, String sqlType, String cast, Boolean multiple) {
    this.key = key;
    this.op = op;
    this.sqlType = sqlType;
    this.cast = cast;
    this.setMultiple(multiple);
  }

  /**
   * Gets the key.
   *
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * Sets the key.
   *
   * @param key the new key
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * Gets the op.
   *
   * @return the op
   */
  public String getOp() {
    return op;
  }

  /**
   * Sets the op.
   *
   * @param op the new op
   */
  public void setOp(String op) {
    this.op = op;
  }

  /**
   * Gets the sql type.
   *
   * @return the sql type
   */
  public String getSqlType() {
    return sqlType;
  }

  /**
   * Sets the sql type.
   *
   * @param sqlType the new sql type
   */
  public void setSqlType(String sqlType) {
    this.sqlType = sqlType;
  }

  /**
   * Gets the cast.
   *
   * @return the cast
   */
  public String getCast() {
    return cast;
  }

  /**
   * Sets the cast.
   *
   * @param cast the new cast
   */
  public void setCast(String cast) {
    this.cast = cast;
  }

  /**
   * Checks if is multiple.
   *
   * @return the boolean
   */
  public Boolean isMultiple() {
    return multiple;
  }

  /**
   * Sets the multiple.
   *
   * @param multiple the new multiple
   */
  public void setMultiple(Boolean multiple) {
    this.multiple = multiple;
  }
}
