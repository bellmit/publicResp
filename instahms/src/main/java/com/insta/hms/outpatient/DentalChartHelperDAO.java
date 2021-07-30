package com.insta.hms.outpatient;

import com.insta.hms.common.AppInit;
import com.insta.hms.outpatient.ToothImageDetails.Tooth;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The Class DentalChartHelperDAO.
 *
 * @author krishna
 */
public class DentalChartHelperDAO {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(DentalChartHelperDAO.class);

  /**
   * Gets the tooth image details.
   *
   * @param adult the adult
   * @return the tooth image details
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static ToothImageDetails getToothImageDetails(boolean adult) throws IOException {

    FileReader fr = new FileReader(AppInit.getRootRealPath() + "/WEB-INF/"
        + (adult ? "ToothImageDetails_Elder.js" : "ToothImageDetails_Pediac.js"));
    ToothImageDetails desc = (ToothImageDetails) new JSONDeserializer()
        .use(null, ToothImageDetails.class).deserialize(fr);

    if (log.isDebugEnabled()) {
      log.debug("DESC: " + new JSONSerializer().prettyPrint(true).deepSerialize(desc));
    }

    return desc;
  }

  /**
   * Gets the tooth numbers.
   *
   * @param toothNumberingSystem the tooth numbering system
   * @param imageDetails         the image details
   * @return the tooth numbers
   */
  public static List getToothNumbers(String toothNumberingSystem, ToothImageDetails imageDetails) {
    List toothNumbers = new ArrayList();
    for (Map.Entry<String, Tooth> entry : imageDetails.getTeeth().entrySet()) {
      if (toothNumberingSystem.equals("U")) {
        try {
          toothNumbers.add(Integer.parseInt(entry.getKey()));
        } catch (NumberFormatException nfe) {
          toothNumbers.add(entry.getKey());
        }
      } else {
        toothNumbers.add(entry.getValue().getToothNumberFDI());
      }
    }
    Collections.sort(toothNumbers);
    return toothNumbers;

  }

  /**
   * Gets the tooth numbers for adult.
   *
   * @param toothNumberingSystem the tooth numbering system
   * @param imageDetails         the image details
   * @return the tooth numbers for adult
   */
  @SuppressWarnings("unchecked")
  public static List getToothNumbersForAdult(String toothNumberingSystem,
      ToothImageDetails imageDetails) {
    List toothNumbers = new ArrayList();
    List toothList = new ArrayList();
    for (Map.Entry<String, Tooth> entry : imageDetails.getTeeth().entrySet()) {
      if (toothNumberingSystem.equals("U")) {
        try {
          toothNumbers.add(Integer.parseInt(entry.getKey()));
        } catch (NumberFormatException nfe) {
          toothNumbers.add(entry.getKey());
        }
      } else {
        toothNumbers.add(entry.getValue().getToothNumberFDI());
      }
    }
    Collections.sort(toothNumbers);
    int toothSize = toothNumbers.size() / 2;
    if (!toothNumbers.isEmpty() && toothNumberingSystem.equals("U")) {
      toothList = toothNumbers.subList(0, toothSize);
      List toothSet = toothNumbers.subList(toothSize, toothNumbers.size());
      Collections.reverse(toothList);
      Collections.sort(toothSet);
      toothList.addAll(toothSet);
    } else {
      toothSize = toothNumbers.size() / 4;
      List adultToothNumbers = toothNumbers.subList(0, toothSize);
      Collections.reverse(adultToothNumbers);
      toothList.addAll(adultToothNumbers);
      List toothSubString1 = toothNumbers.subList(toothSize, toothSize * 2);
      Collections.sort(toothSubString1);
      toothList.addAll(toothSubString1);
      List toothSubString2 = toothNumbers.subList(toothSize * 2, toothSize * 3);
      Collections.sort(toothSubString2);
      List toothSubString3 = toothNumbers.subList(toothSize * 3, toothNumbers.size());
      Collections.reverse(toothSubString3);
      toothList.addAll(toothSubString3);
      toothList.addAll(toothSubString2);

    }

    return toothList;

  }

  /**
   * String to color.
   *
   * @param value the value
   * @return the color
   */
  public static Color stringToColor(final String value) {
    if (value == null) {
      return Color.black;
    }
    try {
      // get color by hex or octal value
      return Color.decode(value);
    } catch (NumberFormatException nfe) {
      // if we can't decode lets try to get it by name
      try {
        // try to get a color by name using reflection
        final Field f = Color.class.getField(value);
        return (Color) f.get(null);
      } catch (Exception ce) {
        // if we can't get any color return black
        return Color.black;
      }
    }
  }

  /**
   * Change color.
   *
   * @param image       the image
   * @param mask        the mask
   * @param replacement the replacement
   * @return the buffered image
   */
  public static BufferedImage changeColor(BufferedImage image, Color mask, Color replacement) {
    BufferedImage destImage = new BufferedImage(image.getWidth(), image.getHeight(),
        BufferedImage.TYPE_INT_ARGB);

    Graphics2D graphic = destImage.createGraphics();
    graphic.drawImage(image, null, 0, 0);
    graphic.dispose();

    for (int i = 0; i < destImage.getWidth(); i++) {
      for (int j = 0; j < destImage.getHeight(); j++) {
        int destRGB = destImage.getRGB(i, j);

        if (mask.getRGB() == destRGB) {
          destImage.setRGB(i, j, replacement.getRGB());
        }
      }
    }
    return destImage;
  }

  /**
   * Gets the tooth UNV number.
   *
   * @param tid       the tid
   * @param fdiNumber the fdi number
   * @return the tooth UNV number
   */
  public static String getToothUNVNumber(ToothImageDetails tid, int fdiNumber) {
    for (Map.Entry<String, Tooth> entry : tid.getTeeth().entrySet()) {
      if (fdiNumber == entry.getValue().getToothNumberFDI()) {
        return entry.getKey();
      }
    }
    return null;
  }

  /**
   * Gets the tooth FDI number.
   *
   * @param tid       the tid
   * @param unvNumber the unv number
   * @return the tooth FDI number
   */
  public static int getToothFDINumber(ToothImageDetails tid, String unvNumber) {
    for (Map.Entry<String, Tooth> entry : tid.getTeeth().entrySet()) {
      if (unvNumber.equals(entry.getKey())) {
        return entry.getValue().getToothNumberFDI();
      }
    }
    return -1;
  }

}
