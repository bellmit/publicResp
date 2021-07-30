package com.insta.hms.common;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/*
 * Simple DTO for storing printable page (jrxml specific) options
 * like margins etc.
 */

/**
 * The Class PrintPageOptions.
 */
public class PrintPageOptions implements Serializable {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(PrintPageOptions.class);

  /** The top margin. */
  public int topMargin;

  /** The bottom margin. */
  public int bottomMargin;

  /** The left margin. */
  public int leftMargin;

  /** The right margin. */
  public int rightMargin;

  /** The char width. */
  public int charWidth;

  /** The page height. */
  public int pageHeight;

  /** The text mode. */
  public String textMode;

  /** The continuous feed. */
  public String continuousFeed;

  /** The Constant PAGE_WIDTH_DEFAULT. */
  public static final int PAGE_WIDTH_DEFAULT = 515;

  /** The page width. */
  public int pageWidth = PAGE_WIDTH_DEFAULT;

  /** The font name. */
  public String fontName;

  /** The font size. */
  public int fontSize;

  /** The orientation. */
  public String orientation;

  /** The text mode columns. */
  public int textModeColumns;

  /** The text mode trailing lines. */
  public int textModeTrailingLines;

  /** The repeat patient info. */
  public boolean repeatPatientInfo;

  /**
   * Instantiates a new prints the page options.
   *
   * @param topMargin      the top margin
   * @param bottomMargin   the bottom margin
   * @param pageHeight     the page height
   * @param continuousFeed the continuous feed
   */
  public PrintPageOptions(int topMargin, int bottomMargin, int pageHeight, String continuousFeed) {
    this.topMargin = topMargin;
    this.bottomMargin = bottomMargin;
    this.pageHeight = pageHeight;
    this.continuousFeed = continuousFeed;
    pageWidth = -1;
    leftMargin = -1;
    rightMargin = -1;
    orientation = "L";
  }

  /**
   * Instantiates a new prints the page options.
   *
   * @param topMargin         the top margin
   * @param bottomMargin      the bottom margin
   * @param pageHeight        the page height
   * @param continuousFeed    the continuous feed
   * @param repeatPatientInfo the repeat patient info
   */
  public PrintPageOptions(int topMargin, int bottomMargin, int pageHeight, String continuousFeed,
      boolean repeatPatientInfo) {
    this(topMargin, bottomMargin, pageHeight, continuousFeed);
    this.repeatPatientInfo = repeatPatientInfo;
  }

  /**
   * Instantiates a new prints the page options.
   *
   * @param topMargin      the top margin
   * @param bottomMargin   the bottom margin
   * @param pageHeight     the page height
   * @param leftMargin     the left margin
   * @param charWidth      the char width
   * @param rightMargin    the right margin
   * @param textMode       the text mode
   * @param continuousFeed the continuous feed
   * @param orientation    the orientation
   */
  public PrintPageOptions(int topMargin, int bottomMargin, int pageHeight, int leftMargin,
      int charWidth, int rightMargin, String textMode, String continuousFeed, String orientation) {
    this.topMargin = topMargin;
    this.bottomMargin = bottomMargin;
    this.pageHeight = pageHeight;
    this.continuousFeed = continuousFeed;
    pageWidth = PAGE_WIDTH_DEFAULT + leftMargin;
    this.leftMargin = leftMargin;
    this.textMode = textMode;
    this.charWidth = charWidth;
    this.rightMargin = rightMargin;
    this.orientation = orientation;
  }

  /**
   * Instantiates a new prints the page options.
   *
   * @param topMargin         the top margin
   * @param bottomMargin      the bottom margin
   * @param pageHeight        the page height
   * @param leftMargin        the left margin
   * @param charWidth         the char width
   * @param rightMargin       the right margin
   * @param textMode          the text mode
   * @param continuousFeed    the continuous feed
   * @param orientation       the orientation
   * @param repeatPatientInfo the repeat patient info
   */
  public PrintPageOptions(int topMargin, int bottomMargin, int pageHeight, int leftMargin,
      int charWidth, int rightMargin, String textMode, String continuousFeed, String orientation,
      Boolean repeatPatientInfo) {
    this(topMargin, bottomMargin, pageHeight, leftMargin, charWidth, rightMargin, textMode,
        continuousFeed, orientation);
    this.repeatPatientInfo = repeatPatientInfo;
  }

  /**
   * Instantiates a new prints the page options.
   *
   * @param topMargin      the top margin
   * @param bottomMargin   the bottom margin
   * @param pageHeight     the page height
   * @param leftMargin     the left margin
   * @param charWidth      the char width
   * @param textMode       the text mode
   * @param continuousFeed the continuous feed
   * @param orientation    the orientation
   */
  public PrintPageOptions(int topMargin, int bottomMargin, int pageHeight, int leftMargin,
      int charWidth, String textMode, String continuousFeed, String orientation) {
    this.topMargin = topMargin;
    this.bottomMargin = bottomMargin;
    this.pageHeight = pageHeight;
    this.continuousFeed = continuousFeed;
    this.leftMargin = leftMargin;
    this.textMode = textMode;
    this.charWidth = charWidth;
    rightMargin = -1;
    pageWidth = -1;
    this.orientation = orientation;
  }

  /**
   * Instantiates a new prints the page options.
   *
   * @param topMargin         the top margin
   * @param bottomMargin      the bottom margin
   * @param pageHeight        the page height
   * @param leftMargin        the left margin
   * @param charWidth         the char width
   * @param textMode          the text mode
   * @param continuousFeed    the continuous feed
   * @param orientation       the orientation
   * @param repeatPatientInfo the repeat patient info
   */
  public PrintPageOptions(int topMargin, int bottomMargin, int pageHeight, int leftMargin,
      int charWidth, String textMode, String continuousFeed, String orientation,
      Boolean repeatPatientInfo) {
    this(topMargin, bottomMargin, pageHeight, leftMargin, charWidth, textMode, continuousFeed,
        orientation);
    this.repeatPatientInfo = repeatPatientInfo;
  }

  /**
   * Instantiates a new prints the page options.
   *
   * @param topMargin    the top margin
   * @param bottomMargin the bottom margin
   * @param rightMargin  the right margin
   * @param leftMargin   the left margin
   * @param pageWidth    the page width
   * @param fontName     the font name
   * @param fontSize     the font size
   */
  public PrintPageOptions(int topMargin, int bottomMargin, int rightMargin, int leftMargin,
      int pageWidth, String fontName, int fontSize) {
    this.topMargin = topMargin;
    this.bottomMargin = bottomMargin;
    this.rightMargin = rightMargin;
    this.leftMargin = leftMargin;
    this.pageWidth = pageWidth;
    this.fontName = fontName;
    this.fontSize = fontSize;
  }

  /**
   * Instantiates a new prints the page options.
   *
   * @param topMargin         the top margin
   * @param bottomMargin      the bottom margin
   * @param rightMargin       the right margin
   * @param leftMargin        the left margin
   * @param pageWidth         the page width
   * @param fontName          the font name
   * @param fontSize          the font size
   * @param repeatPatientInfo the repeat patient info
   */
  public PrintPageOptions(int topMargin, int bottomMargin, int rightMargin, int leftMargin,
      int pageWidth, String fontName, int fontSize, Boolean repeatPatientInfo) {
    this(topMargin, bottomMargin, rightMargin, leftMargin, pageWidth, fontName, fontSize);
    this.repeatPatientInfo = repeatPatientInfo;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString() The string representation is used only for Jasper: so include
   * only variables that will affect Jasper prints.
   */
  @Override
  public String toString() {
    String str = "";
    if (continuousFeed.equals("Y")) {
      str = "cont";
    } else {
      str = "top" + topMargin + "_bottom" + bottomMargin + "_height" + pageHeight;
    }
    if (leftMargin != -1) {
      str += "_left" + leftMargin;
    }
    if (rightMargin != -1) {
      str += "_right" + rightMargin;
    }
    if (pageWidth != -1) {
      str += "_width" + pageWidth;
    }
    if (repeatPatientInfo) {
      str += "_repeatPatientInfo";
    }
    return str;
  }

  /**
   * Instantiates a new prints the page options. Construct page options based on the values of
   * printer_definition table
   *
   * @param bean the bean
   */
  public PrintPageOptions(BasicDynaBean bean) {
    topMargin = (Integer) bean.get("top_margin");
    bottomMargin = (Integer) bean.get("bottom_margin");
    leftMargin = (Integer) bean.get("left_margin");

    continuousFeed = (String) bean.get("continuous_feed");
    pageHeight = (Integer) bean.get("page_height");
    fontName = (String) bean.get("font_name");
    fontSize = (Integer) bean.get("font_size");
    orientation = (String) bean.get("orientation");

    textMode = (String) bean.get("print_mode");
    textModeColumns = (Integer) bean.get("text_mode_column");
    textModeTrailingLines = (Integer) bean.get("text_mode_extra_lines");
    repeatPatientInfo = bean.get("repeat_patient_info") != null
        && bean.get("repeat_patient_info").equals("Y");

    // For text mode, it is useful to have no right margin, and the page
    // width to be equal to the body width + left margin. Here we are assuming that the
    // body width in the jasper is 515, which is true for bills.
    if (textMode.equals("T")) {
      rightMargin = 0;
      pageWidth = 515 + leftMargin;
      logger.debug("textModeColumns: " + textModeColumns);
      double charWidthDbl = Math.ceil((515.00 + leftMargin) / textModeColumns);
      logger.debug("charwidthdbl: " + charWidthDbl);
      charWidth = (int) charWidthDbl;
      logger.debug("charwidth: " + charWidth);
    } else {
      rightMargin = -1;
      pageWidth = -1;
    }
  }
}
