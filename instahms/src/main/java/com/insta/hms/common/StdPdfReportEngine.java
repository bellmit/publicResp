package com.insta.hms.common;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The Class StdPdfReportEngine.
 */
public class StdPdfReportEngine extends StdReportEngine {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(StdReportEngine.class);

  /** The document. */
  protected Document document;

  /** The writer. */
  protected PdfWriter writer;

  /** The main table. */
  protected PdfPTable mainTable;

  /** The os. */
  protected OutputStream os;

  /** The added rows. */
  protected int addedRows;

  /** The size. */
  protected int size;

  /** The Constant numSizes. */
  private static final int numSizes = 25; // 0 - 24

  /** The Constant DEFAULT_FONT_FILE. */
  private static final String DEFAULT_FONT_FILE = 
      "/usr/share/fonts/truetype/unicodefonts/arialunicodems.ttf";

  /** The content font. */
  public static Font[] contentFont = new Font[numSizes];

  /** The main header font. */
  public static Font[] mainHeaderFont = new Font[numSizes];

  /** The group header font. */
  public static Font[] groupHeaderFont = new Font[numSizes];

  /** The totals font. */
  public static Font[] totalsFont = new Font[numSizes];

  /** The title font. */
  public static Font[] titleFont = new Font[numSizes];

  /** The sub title font. */
  public static Font[] subTitleFont = new Font[numSizes];

  /** The date title font. */
  public static Font[] dateTitleFont = new Font[numSizes];

  /** The description font. */
  public static Font[] descriptionFont = new Font[numSizes];

  /** The Constant RTL_CHARS_PATTERN. */
  private static final Pattern RTL_CHARS_PATTERN = Pattern.compile(
      ".*[\\u0590-\\u05ff\\u0600-\\u06ff\\u0750–\\u077f\\u08a0–\\u08ff].*", // Pattern rtl, ""
      Pattern.UNICODE_CHARACTER_CLASS);

  static {
    BaseFont font = null;
    try {
      font = BaseFont.createFont(DEFAULT_FONT_FILE, BaseFont.IDENTITY_H, true);
    } catch (DocumentException de) {
      log.warn("Could not create a font : " + de.getMessage());
    } catch (IOException ioe) {
      log.warn("Could not create font from file : " + DEFAULT_FONT_FILE + " : " + ioe.getMessage());
    }
    for (int i = 1; i < numSizes; i++) {
      int base = i;
      contentFont[i] = (null != font) ? new Font(font, base, Font.NORMAL)
          : new Font(Font.HELVETICA, base, Font.NORMAL); // 10 normal
      descriptionFont[i] = contentFont[i]; // 10 normal
      mainHeaderFont[i] = new Font(Font.HELVETICA, base, Font.BOLD); // 10 bold
      totalsFont[i] = groupHeaderFont[i] = mainHeaderFont[i]; // 10 bold
      titleFont[i] = new Font(Font.HELVETICA, base + 8, Font.BOLD); // 18 bold
      subTitleFont[i] = new Font(Font.HELVETICA, base + 2, Font.BOLD); // 12 bold
      // 8 normal
      dateTitleFont[i] = new Font(Font.HELVETICA, base - 2 < 7 ? 7 : base - 2, Font.NORMAL);
    }
  }

  /**
   * Instantiates a new std pdf report engine.
   *
   * @param desc   the desc
   * @param params the params
   * @param os     the os
   */
  public StdPdfReportEngine(StdReportDesc desc, StdReportParams params, OutputStream os) {
    super(desc, params);
    this.os = os;
    addedRows = 0;
    this.size = params.getBaseFontSize();
  }

  /**
   * Row added event.
   *
   * @throws DocumentException the document exception
   */
  protected void rowAddedEvent() throws DocumentException {
    addedRows++;
    if (addedRows >= 50) {
      document.add(mainTable);
      mainTable.deleteBodyRows();
      mainTable.setSkipFirstHeader(true);
      writer.flush();
      addedRows = 0;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportEngine#initialize()
   */
  protected void initialize() throws Exception {
    // Create and open the document
    Rectangle docSize = PageSize.A4;
    float totalWidth = 0;
    for (Column col : columns) {
      // scale the widths based on the base font size
      if (params.getBaseFontSize() != 10) {
        col.width = col.width * params.getBaseFontSize() / 10;
      }
      totalWidth += col.width;
    }
    if (totalWidth > 495) {
      docSize = docSize.rotate();
    }
    log.debug("Total width of fields: " + totalWidth);
    document = new Document(docSize, 50, 50, 50, 50);
    writer = PdfWriter.getInstance(document, os);
    writer.setPageEvent(new StdReportFooter(params.getUserName(), params.getHospNameAndAddrs(),
        params.getDateReqd(), params.getPageNmReqd()));
    document.open();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportEngine#cleanup()
   */
  protected void cleanup() throws Exception {
    try {
      // write any residual rows to the document
      if (document != null) {
        document.add(mainTable);
        writer.flush();
        document.close();
      }
      if (os != null) {
        os.close();
      }
    } catch (java.net.SocketException exception) {
      // ignore socket errors: these are caused by PDF plugin in Firefox.
      log.error("Client closed connection: " + exception.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportEngine#writeTitle(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String, java.lang.String)
   */
  protected void writeTitle(String title, String hospNameAddrs, String fdate, String tdate,
      String filterDesc) throws Exception {
    PdfPTable titleTable = new PdfPTable(1);
    titleTable.setWidthPercentage(100);
    if (hospNameAddrs != null && !hospNameAddrs.equals("")) {
      PdfPCell hospNameAddrsCell = new PdfPCell(new Paragraph(hospNameAddrs, dateTitleFont[size]));
      hospNameAddrsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
      hospNameAddrsCell.setBorderWidth(0);
      titleTable.addCell(hospNameAddrsCell);
    }

    PdfPCell titleCell = new PdfPCell(new Paragraph(title, titleFont[size]));
    titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);

    if ((fdate == null) && (tdate == null) && (filterDesc == null)) {
      titleCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
    } else {
      titleCell.setBorder(Rectangle.TOP);
    }
    titleTable.addCell(titleCell);

    if (filterDesc != null) {
      PdfPCell filterCell = new PdfPCell(
          new Paragraph(filterDesc.toString(), descriptionFont[size]));
      filterCell.setHorizontalAlignment(Element.ALIGN_CENTER);
      filterCell
          .setBorder((fdate == null) && (tdate == null) ? Rectangle.BOTTOM : Rectangle.NO_BORDER);
      titleTable.addCell(filterCell);
    }

    if ((fdate != null) || (tdate != null)) {
      PdfPCell dateCell = new PdfPCell(new Paragraph(fdate + " to " + tdate, dateTitleFont[size]));
      dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
      dateCell.setBorder(Rectangle.BOTTOM);
      titleTable.addCell(dateCell);
    }

    document.add(titleTable);
    Paragraph spacing = new Paragraph(" ");
    document.add(spacing);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportEngine#writeTitle(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String)
   */
  protected void writeTitle(String title, String fdate, String tdate, String filterDesc)
      throws Exception {
    PdfPTable titleTable = new PdfPTable(1);
    titleTable.setWidthPercentage(100);
    PdfPCell titleCell = new PdfPCell(new Paragraph(title, titleFont[size]));
    titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);

    if ((fdate == null) && (tdate == null) && (filterDesc == null)) {
      titleCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
    } else {
      titleCell.setBorder(Rectangle.TOP);
    }
    titleTable.addCell(titleCell);

    if (filterDesc != null) {
      PdfPCell filterCell = new PdfPCell(
          new Paragraph(filterDesc.toString(), descriptionFont[size]));
      filterCell.setHorizontalAlignment(Element.ALIGN_CENTER);
      filterCell
          .setBorder((fdate == null) && (tdate == null) ? Rectangle.BOTTOM : Rectangle.NO_BORDER);
      titleTable.addCell(filterCell);
    }

    if ((fdate != null) || (tdate != null)) {
      PdfPCell dateCell = new PdfPCell(new Paragraph(fdate + " to " + tdate, dateTitleFont[size]));
      dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
      dateCell.setBorder(Rectangle.BOTTOM);
      titleTable.addCell(dateCell);
    }

    document.add(titleTable);
    Paragraph spacing = new Paragraph(" ");
    document.add(spacing);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportEngine#writeMainHeader(java.util.List, boolean)
   */
  protected void writeMainHeader(List<Column> columns, boolean isSummary) throws Exception {
    float[] allWidths = new float[columns.size()];
    int index = 0;
    for (Column column : columns) {
      allWidths[index++] = column.width;
    }

    mainTable = new PdfPTable(columns.size());
    mainTable.setTotalWidth(allWidths);
    mainTable.setLockedWidth(true);
    mainTable.setHorizontalAlignment(Element.ALIGN_LEFT);

    for (Column col : columns) {
      PdfPCell header = new PdfPCell(new Paragraph(col.header, mainHeaderFont[size]));
      header.setHorizontalAlignment(
          col.align == Align.LEFT ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
      header
          .setBorder(isSummary ? Rectangle.TOP | Rectangle.BOTTOM | Rectangle.LEFT | Rectangle.RIGHT
              : Rectangle.TOP | Rectangle.BOTTOM);
      mainTable.addCell(header);
    }
    mainTable.setHeaderRows(1);
    mainTable.setSkipFirstHeader(false);
    rowAddedEvent();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportEngine#writeListGroupTotals(java.lang.String, int,
   * java.util.List)
   */
  protected void writeListGroupTotals(String title, int titleColSpan, List<String> valueList)
      throws Exception {

    PdfPCell totCell = new PdfPCell(new Paragraph(title, totalsFont[size]));
    totCell.setColspan(titleColSpan);
    totCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    totCell.setBorderColor(Color.darkGray);
    totCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
    mainTable.addCell(totCell);

    for (String value : valueList) {
      PdfPCell cell = new PdfPCell(new Paragraph(value, totalsFont[size]));
      cell.setHorizontalAlignment(cell.ALIGN_RIGHT);
      cell.setBorderColor(Color.darkGray);
      cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
      mainTable.addCell(cell);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportEngine#writeGroupHeader(java.lang.String, java.util.List,
   * java.util.List, int)
   */
  protected void writeGroupHeader(String numbering, List<String> headerNames,
      List<String> headerVals, int level) throws Exception {
    // one big header line with all the header names and values
    StringBuilder header = new StringBuilder();

    header.append(numbering);
    if (!numbering.equals("")) {
      header.append(". ");
    }

    for (int i = 0; i < headerNames.size(); i++) {
      if (i > 0) {
        header.append(";  ");
      }
      header.append(headerNames.get(i) + ": " + headerVals.get(i));
    }

    PdfPCell hdrCell = new PdfPCell(new Paragraph(header.toString(), groupHeaderFont[size]));
    hdrCell.setBorder(Rectangle.NO_BORDER);
    hdrCell.setColspan(params.getDisplayFields().size());
    hdrCell.setIndent(level * 3f);
    hdrCell.setPadding(3f);
    mainTable.addCell(hdrCell);
    rowAddedEvent();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportEngine#writeListRow(java.util.List)
   */
  protected void writeListRow(List<String> values) throws DocumentException {
    for (int i = 0; i < values.size(); i++) {
      String value = values.get(i);
      Column col = columns.get(i);
      PdfPCell cell = new PdfPCell(new Paragraph(value, contentFont[size]));
      if (RTL_CHARS_PATTERN.matcher(value).matches()) {
        // Set the run direction to RTL to support arabic.
        cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        // Reverse the alignment since we have set the run direction to RTL
        cell.setHorizontalAlignment(
            col.align == Align.LEFT ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
      } else {
        cell.setRunDirection(PdfWriter.RUN_DIRECTION_LTR);
        cell.setHorizontalAlignment(
            col.align == Align.RIGHT ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
      }
      cell.setBorder(Rectangle.NO_BORDER);
      mainTable.addCell(cell);
    }
    rowAddedEvent();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportEngine#writeSumRow(java.util.List, boolean, boolean,
   * boolean, boolean)
   */
  protected void writeSumRow(List<String> values, boolean isNewGroup, boolean isLastRow,
      boolean isLastColTotal, boolean isRowTotal) throws DocumentException {
    for (int i = 0; i < values.size(); i++) {
      String value = values.get(i);
      Column col = columns.get(i);
      PdfPCell cell = new PdfPCell(new Paragraph(value,
          isRowTotal || (isLastColTotal && i == values.size() - 1) ? totalsFont[size]
              : contentFont[size]));
      cell.setHorizontalAlignment(
          col.align == Align.LEFT ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
      if (i == 0) {
        if (isNewGroup && isLastRow) {
          cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM | Rectangle.LEFT);
        } else if (isLastRow) {
          cell.setBorder(Rectangle.BOTTOM | Rectangle.LEFT);
        } else if (isNewGroup) {
          cell.setBorder(Rectangle.TOP | Rectangle.LEFT);
        } else {
          cell.setBorder(Rectangle.LEFT);
        }
      } else {
        cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM | Rectangle.LEFT | Rectangle.RIGHT);
      }
      mainTable.addCell(cell);
    }
    rowAddedEvent();
  }

}
