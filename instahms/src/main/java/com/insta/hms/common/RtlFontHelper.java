package com.insta.hms.common;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.pdf.ITextFSFont;
import org.xhtmlrenderer.pdf.ITextFontResolver.FontDescription;
import org.xhtmlrenderer.pdf.TrueTypeUtil;
import org.xhtmlrenderer.render.FSFont;

import java.io.IOException;

/**
 * The Class RtlFontHelper.
 */
public class RtlFontHelper {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(RtlFontHelper.class);

  /** The Constant ARABIC_CODEPAGE_INDICATOR. */
  private static final String ARABIC_CODEPAGE_INDICATOR = "arabic";

  /** The Constant defaultFontPath. */
  // TODO : This needs to come from the RtlProcessor class
  private static final String defaultFontPath = 
      "/usr/share/fonts/truetype/unicodefonts/arialunicodems.ttf";

  /**
   * Gets the applicable font.
   *
   * @param style   the style
   * @param context the c
   * @param dpp     the dpp
   * @return the applicable font
   */
  public static FSFont getApplicableFont(CalculatedStyle style, CssContext context, float dpp) {
    FSFont font = style.getFSFont(context);
    return getApplicableFont(font, dpp);
  }

  /**
   * Gets the applicable font.
   *
   * @param font the f
   * @param dpp  the dpp
   * @return the applicable font
   */
  private static FSFont getApplicableFont(FSFont font, float dpp) {

    FontDescription fd = null;
    FontSpecification fs = getDefaultFontSpec(dpp);

    if (null == font) {
      return createDefaultFont(fs.fontStyle, fs.fontWeight, fs.size);
    }

    if (font instanceof ITextFSFont) {
      ITextFSFont itextFont = (ITextFSFont) font;
      fd = itextFont.getFontDescription();

      if (null != fd) {
        BaseFont baseFont = fd.getFont();
        if (null != baseFont && isArabicCodePageSupported(baseFont.getCodePagesSupported())) {
          return font;
        }
      }
    }
    return createDefaultFont(fs.fontStyle, fs.fontWeight, fs.size);
  }

  /**
   * Checks if is arabic code page supported.
   *
   * @param codePagesSupported the code pages supported
   * @return true, if is arabic code page supported
   */
  private static boolean isArabicCodePageSupported(String[] codePagesSupported) {

    if (null == codePagesSupported || codePagesSupported.length == 0) {
      return false;
    }

    for (String codePage : codePagesSupported) {
      if (codePage.toLowerCase().contains(ARABIC_CODEPAGE_INDICATOR)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the default font spec.
   *
   * @param dpp the dpp
   * @return the default font spec
   */
  public static FontSpecification getDefaultFontSpec(float dpp) {
    FontSpecification fs = new FontSpecification();
    fs.families = new String[] { "Arial Unicode MS" };
    fs.fontStyle = IdentValue.NORMAL;
    fs.fontWeight = IdentValue.NORMAL;
    fs.size = 10.0f * dpp;
    return fs;
  }

  /**
   * Creates the default font.
   *
   * @param style  the style
   * @param weight the weight
   * @param size   the size
   * @return the FS font
   */
  public static FSFont createDefaultFont(IdentValue style, IdentValue weight, float size) {
    FontDescription fd = null;
    FSFont font = null;
    fd = getDefaultFontDescr(style, weight);
    if (null != fd) {
      font = new ITextFSFont(fd, size);
    }
    return font;
  }

  /**
   * Gets the default font descr.
   *
   * @param style  the style
   * @param weight the weight
   * @return the default font descr
   */
  private static FontDescription getDefaultFontDescr(IdentValue style, IdentValue weight) {
    BaseFont font = null;
    try {
      font = BaseFont.createFont(defaultFontPath, BaseFont.IDENTITY_H, true);
    } catch (DocumentException de) {
      log.error("Document Error while creating the default font : " + de.getMessage());
      return null;
    } catch (IOException ioe) {
      log.error("I/O Error while creating the default font : " + ioe.getMessage());
      return null;
    }

    // int fontWeight = (int) weight.asFloat();
    FontDescription descr = new FontDescription(font); // , style, fontWeight);

    try {
      TrueTypeUtil.populateDescription(defaultFontPath, font, descr);
    } catch (Exception exception) {
      // IOException, NoSuchFieldException, IllegalAccessException, DocumentException
      log.error("Error while extracting the font description :" + exception.getMessage());
    }

    return descr;
  }

}
