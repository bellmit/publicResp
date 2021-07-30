package com.insta.hms.outpatient;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class ToothImageDetails.
 *
 * @author krishna
 */
public class ToothImageDetails {

  /**
   * Instantiates a new tooth image details.
   */
  public ToothImageDetails() {

  }

  /** The teeth. */
  private Map<String, Tooth> teeth = new HashMap<>();

  /**
   * Gets the teeth.
   *
   * @return the teeth
   */
  public Map<String, Tooth> getTeeth() {
    return teeth;
  }

  /**
   * Sets the teeth.
   *
   * @param teeth the teeth
   */
  public void setTeeth(Map<String, Tooth> teeth) {
    this.teeth = teeth;
  }

  /**
   * The Class Tooth.
   */
  public static class Tooth {

    /** The tooth type. */
    private String tooth_type;

    /** The tooth part. */
    private Map<String, ToothPart> toothPart = new HashMap<String, ToothPart>();

    /** The tooth number FDI. */
    private int toothNumberFDI;

    /**
     * Instantiates a new tooth.
     */
    public Tooth() {

    }

    /**
     * Sets the tooth number FDI.
     *
     * @param toothNumberFDI the new tooth number FDI
     */
    public void setToothNumberFDI(int toothNumberFDI) {
      this.toothNumberFDI = toothNumberFDI;
    }

    /**
     * Gets the tooth number FDI.
     *
     * @return the tooth number FDI
     */
    public int getToothNumberFDI() {
      return toothNumberFDI;
    }

    /**
     * Gets the tooth part.
     *
     * @return the tooth part
     */
    public Map<String, ToothPart> getToothPart() {
      return this.toothPart;
    }

    /**
     * Sets the tooth part.
     *
     * @param toothPart the tooth part
     */
    public void setToothPart(Map<String, ToothPart> toothPart) {
      this.toothPart = toothPart;
    }

    /**
     * Gets the tooth type.
     *
     * @return the tooth type
     */
    public String getTooth_type() {
      return this.tooth_type;
    }

    /**
     * Sets the tooth type.
     *
     * @param tooth_type the new tooth type
     */
    public void setTooth_type(String tooth_type) {
      this.tooth_type = tooth_type;
    }
  }

  /**
   * The Class ToothPart.
   */
  public static class ToothPart {

    /** The image name. */
    private String image_name;

    /** The pos x. */
    private float pos_x;

    /** The pos y. */
    private float pos_y;

    /** The width. */
    private float width;

    /** The ht. */
    private float ht;

    /**
     * Instantiates a new tooth part.
     */
    public ToothPart() {

    }

    /**
     * Gets the image name.
     *
     * @return the image name
     */
    public String getImage_name() {
      return this.image_name;
    }

    /**
     * Gets the ht.
     *
     * @return the ht
     */
    public float getHt() {
      return ht;
    }

    /**
     * Sets the ht.
     *
     * @param ht the new ht
     */
    public void setHt(float ht) {
      this.ht = ht;
    }

    /**
     * Gets the pos x.
     *
     * @return the pos x
     */
    public float getPos_x() {
      return pos_x;
    }

    /**
     * Sets the pos x.
     *
     * @param pos_x the new pos x
     */
    public void setPos_x(float pos_x) {
      this.pos_x = pos_x;
    }

    /**
     * Gets the pos y.
     *
     * @return the pos y
     */
    public float getPos_y() {
      return pos_y;
    }

    /**
     * Sets the pos y.
     *
     * @param pos_y the new pos y
     */
    public void setPos_y(float pos_y) {
      this.pos_y = pos_y;
    }

    /**
     * Gets the width.
     *
     * @return the width
     */
    public float getWidth() {
      return width;
    }

    /**
     * Sets the width.
     *
     * @param width the new width
     */
    public void setWidth(float width) {
      this.width = width;
    }

    /**
     * Sets the image name.
     *
     * @param image_name the new image name
     */
    public void setImage_name(String image_name) {
      this.image_name = image_name;
    }
  }
}
