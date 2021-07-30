package com.insta.hms.integration.insurance.accumed;

import org.apache.commons.lang3.StringUtils;

/**
 * The Class AccumedXmlValues.
 */
public class AccumedXmlValues {

  /**
   * The Enum AttachmentType.
   */
  public enum AttachmentType {

    /** The pdf. */
    PDF("PDF"),
    /** The ms word. */
    MS_WORD("MS Word"),
    /** The tiff. */
    TIFF("TIFF"),
    /** The jpeg. */
    JPEG("JPEG"),
    /** The png. */
    PNG("PNG"),
    /** The gif. */
    GIF("GIF"),
    /** The rtf. */
    RTF("RTF"),
    /** The txt. */
    TXT("TXT");

    /** The attachment type value. */
    private String attachmentTypeValue;

    /**
     * Instantiates a new attachment type.
     *
     * @param attachmentType the attachment type
     */
    private AttachmentType(String attachmentType) {
      this.attachmentTypeValue = attachmentType;
    }

    /**
     * Gets the attachment type string.
     *
     * @return the attachment type string
     */
    private String getAttachmentTypeString() {
      return this.attachmentTypeValue;
    }

    /**
     * Gets the attachment type string.
     *
     * @param contentType the content type
     * @return the attachment type string
     */
    public static String getAttachmentTypeXmlValue(String contentType) {
      for (AttachmentType attachmentType : AttachmentType.values()) {
        if (StringUtils.containsIgnoreCase(contentType,
            attachmentType.getAttachmentTypeString())) {
          return attachmentType.getAttachmentTypeString();
        }
      }
      return null;
    }
  }

  /**
   * The Enum AccumedXmlTagName.
   */
  public enum AccumedXmlTagName {

    /** The chief complaint. */
    CHIEF_COMPLAINT("ChiefComplaint"),

    /** The main symptoms. */
    MAIN_SYMPTOMS("MainSymptoms"),

    /** The physical exam. */
    PHYSICAL_EXAM("PhysicalExam"),

    /** The past history. */
    PAST_HISTORY("PastHistory"),

    /** The vital signs. */
    VITAL_SIGNS("VitalSigns");

    /** The xml tag name. */
    private String xmlTagName;

    /**
     * Instantiates a new accumed xml tag name.
     *
     * @param xmlTagName the xml tag name
     */
    private AccumedXmlTagName(String xmlTagName) {
      this.xmlTagName = xmlTagName;
    }

    /**
     * Gets the tag name.
     *
     * @return the tag name
     */
    public String getTagName() {
      return this.xmlTagName;
    }
  }


  /**
   * The Enum ObservationType.
   */
  public enum ObservationType {

    /** The file. */
    FILE("File"),

    /** The cpt. */
    CPT("CPT"),

    /** The Text. */
    TEXT("Text");
    
    /** The observation type. */
    private String observationType;

    /**
     * Instantiates a new observation type.
     *
     * @param observationType the observation type
     */
    private ObservationType(String observationType) {
      this.observationType = observationType;

    }

    /**
     * Gets the observation type.
     *
     * @return the observation type
     */
    public String getObservationType() {
      return this.observationType;
    }
  }

}
