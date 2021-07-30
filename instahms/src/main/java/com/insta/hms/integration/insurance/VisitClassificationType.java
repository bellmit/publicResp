package com.insta.hms.integration.insurance;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Enum VisitClassificationType.
 */
public enum VisitClassificationType {

  /** The medical tourist. */
  MEDICAL_TOURIST("M", "Medical tourist", "MedicalTourismSelfPay"),
  /** The selfpay patient. */
  SELFPAY_PATIENT("S", "Self-pay patient", "SelfPay"),
  /** The other. */
  OTHER("O", "Other non-insurance patient", "ProFormaPayer");

  /** The key. */
  private String key;

  /** The label. */
  private String label;

  /** The xml tag. */
  private String xmlTag;

  /**
   * Instantiates a new visit classification type.
   *
   * @param key the key
   * @param label the label
   * @param xmlTag the xml tag
   */
  private VisitClassificationType(String key, String label, String xmlTag) {
    this.key = key;
    this.label = label;
    this.xmlTag = xmlTag;
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
   * Sets the value.
   *
   * @param key the new value
   */
  public void setValue(String key) {
    this.key = key;
  }

  /**
   * Gets the label.
   *
   * @return the label
   */
  public String getLabel() {
    return label;
  }

  public static String getLabel(String key) {
    return enumMap.get(key);
  }


  /**
   * Sets the label.
   *
   * @param label the new label
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /** The Constant enumMap. */
  public static final Map<String, String> enumMap = new HashMap<String, String>();

  static {
    for (VisitClassificationType r : EnumSet.allOf(VisitClassificationType.class)) {
      enumMap.put(r.getKey(), r.getLabel());
    }
  }

  /**
   * Enum map to list.
   *
   * @return the list
   */
  public static List enumMapToList() {
    List result = new ArrayList<Map<String, Object>>();
    for (VisitClassificationType r : EnumSet.allOf(VisitClassificationType.class)) {
      Map map = new HashMap<>();
      map.put("key", r.getKey());
      map.put("value", r.getLabel());
      map.put("id", r);
      result.add(map);
    }
    return result;
  }

  /**
   * Takes a key and returns matching XMLTag. If invalid key is presented, returns null.
   *
   * @param key the key
   * @return the xml tag
   */
  public static String getXmlTag(String key) {
    for (VisitClassificationType r : EnumSet.allOf(VisitClassificationType.class)) {
      if (r.getKey().equals(key)) {
        return r.xmlTag;
      }
    }
    return null;
  }

  /**
   * Sets the xml tag.
   *
   * @param xmlTag the new xml tag
   */
  public void setXmlTag(String xmlTag) {
    this.xmlTag = xmlTag;
  }

}
