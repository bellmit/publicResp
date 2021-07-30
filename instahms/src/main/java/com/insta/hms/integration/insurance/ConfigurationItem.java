package com.insta.hms.integration.insurance;


/**
 * The Class ConfigurationItem.
 */
public class ConfigurationItem {

  /** The name. */
  private String name;
  
  /** The type. */
  private String type;
  
  /** The label. */
  private String label;

  /**
   * Instantiates a new configuration item.
   *
   * @param name the name
   * @param type the type
   * @param label the label
   */
  ConfigurationItem(String name, String type, String label) {
    super();
    this.name = name;
    this.type = type;
    this.label = label;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Gets the label.
   *
   * @return the label
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets the label.
   *
   * @param label the new label
   */
  public void setLabel(String label) {
    this.label = label;
  }
}
