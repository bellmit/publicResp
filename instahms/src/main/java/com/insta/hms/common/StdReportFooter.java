package com.insta.hms.common;

import com.bob.hms.common.DateUtil;
import com.lowagie.text.Document;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * The Class StdReportFooter.
 */
public class StdReportFooter extends PdfPageEventHelper {

  /** The total. */
  protected PdfTemplate total;

  /** The date. */
  protected PdfTemplate date;

  /** The user name templt. */
  protected PdfTemplate userNameTemplt;

  /** The hosp name templt. */
  protected PdfTemplate hospNameTemplt;

  /** The user name. */
  protected String userName;

  /** The is user name reqd. */
  protected Boolean isUserNameReqd = true;

  /** The Hosp name and addrs. */
  protected String hospNameAndAddrs;

  /** The is hosp name and addrs reqd. */
  protected Boolean isHospNameAndAddrsReqd = true;

  /** The is date reqd. */
  protected Boolean isDateReqd = true;

  /** The is page nm reqd. */
  protected Boolean isPageNmReqd = true;

  /**
   * Instantiates a new std report footer.
   *
   * @param username         the user name
   * @param hospNameAndAddrs the hosp name and addrs
   * @param isDateReqd       the is date reqd
   * @param isPageNmReqd     the is page nm reqd
   */
  public StdReportFooter(String username, String hospNameAndAddrs, Boolean isDateReqd,
      Boolean isPageNmReqd) {
    userName = username;
    if (username != null && !username.equals("")) {
      isUserNameReqd = true;
    } else {
      isUserNameReqd = false;
    }
    if (hospNameAndAddrs != null && !hospNameAndAddrs.equals("")) {
      this.hospNameAndAddrs = hospNameAndAddrs;
      isHospNameAndAddrsReqd = true;
    } else {
      isHospNameAndAddrsReqd = false;
    }
    this.isDateReqd = isDateReqd != null ? isDateReqd : true;
    this.isPageNmReqd = isPageNmReqd != null ? isPageNmReqd : true;
  }

  /** The PdfTemplate that contains the total number of pages. */

  /** The font that will be used. */
  protected BaseFont helv;

  /*
   * (non-Javadoc)
   * 
   * @see com.lowagie.text.pdf.PdfPageEventHelper#onOpenDocument( com.lowagie.text.pdf.PdfWriter,
   * com.lowagie.text.Document)
   */
  @Override
  public void onOpenDocument(PdfWriter writer, Document document) {
    total = writer.getDirectContent().createTemplate(100, 100);
    total.setBoundingBox(new Rectangle(-20, -20, 100, 100));
    total.getBoundingBox().setBorder(Rectangle.BOX);
    date = writer.getDirectContent().createTemplate(100, 100);
    date.setBoundingBox(new Rectangle(-20, -20, 100, 100));
    date.getBoundingBox().setBorder(Rectangle.BOX);
    hospNameTemplt = writer.getDirectContent().createTemplate(150, 100);
    hospNameTemplt.setBoundingBox(new Rectangle(-20, -20, 150, 100));
    userNameTemplt = writer.getDirectContent().createTemplate(150, 100);
    userNameTemplt.setBoundingBox(new Rectangle(-20, -20, 150, 150));
    userNameTemplt.getBoundingBox().setBorder(Rectangle.BOX);

    try {
      helv = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
    } catch (Exception exception) {
      throw new ExceptionConverter(exception);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.lowagie.text.pdf.PdfPageEventHelper#onEndPage( com.lowagie.text.pdf.PdfWriter,
   * com.lowagie.text.Document)
   */
  @Override
  public void onEndPage(PdfWriter writer, Document document) {

    String text = isPageNmReqd ? "Page " + writer.getPageNumber() + " of " : "";
    float textBase = document.bottom() - 20;
    float textSize = helv.getWidthPoint(text, 8);

    PdfContentByte cb = writer.getDirectContent();
    cb.saveState();
    cb.beginText();
    cb.setFontAndSize(helv, 8);
    float adjust = helv.getWidthPoint("0", 8);
    cb.setTextMatrix(document.right() - textSize - adjust, textBase);
    cb.showText(text);
    cb.endText();
    if (isPageNmReqd) {
      cb.addTemplate(total, document.right() - adjust, textBase);
    }
    cb.restoreState();

    PdfContentByte cb1 = writer.getDirectContent();
    cb1.saveState();
    cb1.beginText();
    cb1.setFontAndSize(helv, 8);
    cb1.setTextMatrix(document.left(), textBase);
    String dt = isDateReqd ? DateUtil.currentDate("dd-MM-yyyy HH:mm:ss") : "";
    cb1.showText(dt);
    cb1.endText();
    cb1.addTemplate(date, document.left() + textSize, textBase);
    cb1.restoreState();
    float docWidth = document.getPageSize().getWidth();
    float widthAdjust = docWidth > 595 ? 250 : 150;
    if (isUserNameReqd) {
      String text2 = " Report  generated  by: " + userName;
      float textSize2 = helv.getWidthPoint(text2, 8);
      PdfContentByte cb2 = writer.getDirectContent();
      cb2.saveState();
      cb2.beginText();
      cb2.setFontAndSize(helv, 8);
      float adjust2 = helv.getWidthPoint("0", 8);
      cb2.setTextMatrix(document.left() + textSize2, textBase);
      cb2.showText(text2);
      cb2.endText();
      cb2.addTemplate(userNameTemplt, document.left() + textSize2, textBase);
      cb2.restoreState();
    }

    if (isHospNameAndAddrsReqd) {
      String text2 = hospNameAndAddrs;
      int textlengths = text2.length();
      float textSize2 = helv.getWidthPoint(text2, 8);
      widthAdjust = textSize2 > 100 && (widthAdjust + textSize2) < 500 && docWidth <= 595
          ? 100 + textSize2
          : widthAdjust;
      PdfContentByte cb3 = writer.getDirectContent();
      cb3.saveState();
      cb3.beginText();
      cb3.setFontAndSize(helv, 8);
      float adjust2 = helv.getWidthPoint("0", 8);
      if (document.right() - textSize - textSize2 - (adjust2 * 2) > 350 && docWidth <= 595) {
        cb3.setTextMatrix(document.left() + textSize2 + widthAdjust, textBase);
        cb3.showText(text2);
        cb3.endText();
        cb3.addTemplate(hospNameTemplt, document.left() + textSize2, textBase);
        cb3.restoreState();
      } else if (document.right() - textSize - textSize2 - (adjust2 * 2) < 200 && docWidth <= 595) {
        String spltTxt1 = text2.substring(0, text2.length() / 2);
        String spltTxt2 = text2.substring((text2.length() / 2), text2.length());
        float text1Size2 = helv.getWidthPoint(spltTxt1, 8);
        float text2Size2 = helv.getWidthPoint(spltTxt2, 8);
        cb3.setTextMatrix(document.right() - textSize - text2Size2 - (adjust2 * 2), textBase);
        cb3.showText(spltTxt1);
        cb3.setTextMatrix(document.right() - textSize - text2Size2 - (adjust2 * 2), textBase - 8);
        cb3.showText(spltTxt2);
        cb3.endText();
        cb3.addTemplate(hospNameTemplt, document.right() - textSize - text1Size2 - (adjust2 * 2),
            textBase);
        cb3.restoreState();
      } else {
        cb3.setTextMatrix(document.right() - textSize - textSize2 - (adjust2 * 2), textBase);
        cb3.showText(text2);
        cb3.endText();
        cb3.addTemplate(hospNameTemplt, document.right() - textSize - textSize2 - (adjust2 * 2),
            textBase);
        cb3.restoreState();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.lowagie.text.pdf.PdfPageEventHelper#onCloseDocument( com.lowagie.text.pdf.PdfWriter,
   * com.lowagie.text.Document)
   */
  @Override
  public void onCloseDocument(PdfWriter writer, Document document) {
    total.beginText();
    total.setFontAndSize(helv, 8);
    total.setTextMatrix(0, 0);
    total.showText(String.valueOf(writer.getPageNumber() - 1));
    total.endText();
  }
}
