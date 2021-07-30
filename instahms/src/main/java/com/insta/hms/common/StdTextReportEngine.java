package com.insta.hms.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * The Class StdTextReportEngine.
 */
public class StdTextReportEngine extends StdReportEngine {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(StdTextReportEngine.class);

  /** The sw. */
  protected Writer sw;

  /** The char width. */
  protected Double charWidth; // width of one character in points.

  /** The total width. */
  protected int totalWidth;

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportEngine#initialize()
   */
  protected void initialize() {
    totalWidth = 0;
    for (Column col : columns) {
      col.charWidth = (int) Math.round(col.width / charWidth);
      totalWidth += col.charWidth + 1; // add a space after every column
    }
    totalWidth -= 1; // no space after last column
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportEngine#cleanup()
   */
  protected void cleanup() throws IOException {
    sw.flush();
  }

  /**
   * Instantiates a new std text report engine.
   *
   * @param desc   the desc
   * @param params the params
   * @param sw     the sw
   */
  public StdTextReportEngine(StdReportDesc desc, StdReportParams params, Writer sw) {
    this(desc, params, sw, 5.0);
  }

  /**
   * Instantiates a new std text report engine.
   *
   * @param desc      the desc
   * @param params    the params
   * @param sw        the sw
   * @param charWidth the char width
   */
  public StdTextReportEngine(StdReportDesc desc, StdReportParams params, Writer sw,
      Double charWidth) {
    super(desc, params);
    this.sw = sw;
    this.charWidth = charWidth;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportEngine#writeTitle(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String)
   */
  protected void writeTitle(String title, String fdate, String tdate, String filterDesc)
      throws Exception {
    dashedLine();
    centerAlign(title, totalWidth);
    newLine();
    if (filterDesc != null) {
      centerAlign(filterDesc, totalWidth);
      newLine();
    }
    if (fdate != null || tdate != null) {
      centerAlign(fdate + " to " + tdate, totalWidth);
      newLine();
    }
    dashedLine();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportEngine#writeTitle(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String, java.lang.String)
   */
  protected void writeTitle(String title, String hspNameAddrs, String fdate, String tdate,
      String filterDesc) throws Exception {
    if (hspNameAddrs != null && !hspNameAddrs.equals("")) {
      centerAlign(hspNameAddrs, totalWidth);
      newLine();
    }
    dashedLine();
    centerAlign(title, totalWidth);
    newLine();
    if (filterDesc != null) {
      centerAlign(filterDesc, totalWidth);
      newLine();
    }
    if (fdate != null || tdate != null) {
      centerAlign(fdate + " to " + tdate, totalWidth);
      newLine();
    }
    dashedLine();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportEngine#writeMainHeader(java.util.List, boolean)
   */
  protected void writeMainHeader(List<Column> columns, boolean isSummary) throws Exception {
    for (Column col : columns) {
      writeCell(col.align, col.header, col.charWidth);
      emptySpaces(1);
    }
    newLine();
    dashedLine();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportEngine#writeListGroupTotals(java.lang.String, int,
   * java.util.List)
   */
  protected void writeListGroupTotals(String title, int titleColSpan, List<String> valueList)
      throws Exception {

    dashedLine();

    int spanWidth = 0;
    for (int i = 0; i < titleColSpan; i++) {
      spanWidth += columns.get(i).charWidth + 1; // one space between columns
    }
    spanWidth--; // no space after last column

    if (titleColSpan == 0) {
      // first column is aggregatable do not print the title.
    } else {
      rightAlign(title, spanWidth);
    }
    emptySpaces(1);

    for (int i = 0; i < valueList.size(); i++) {
      Column col = columns.get(i + titleColSpan);
      String value = valueList.get(i);
      writeCell(col.align, value, col.charWidth);
      emptySpaces(1);
    }

    newLine();
    dashedLine();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportEngine#writeGroupHeader(java.lang.String , java.util.List,
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

    sw.append(header.toString());
    newLine();
    dashedLine(header.length());
    newLine();
  }

  /**
   * Write row.
   *
   * @param values the values
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected void writeRow(List<String> values) throws IOException {
    for (int i = 0; i < values.size(); i++) {
      String value = values.get(i);
      value = value == null ? "" : value;
      Column col = columns.get(i);
      writeCell(col.align, value, col.charWidth);
      emptySpaces(1);
    }
    newLine();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportEngine#writeListRow(java.util.List)
   */
  protected void writeListRow(List<String> values) throws IOException {
    writeRow(values);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportEngine#writeSumRow(java.util.List, boolean, boolean,
   * boolean, boolean)
   */
  protected void writeSumRow(List<String> values, boolean breakGroup, boolean isLastRow,
      boolean isLastColTotal, boolean isRowTotal) throws IOException {
    if (breakGroup) {
      dashedLine();
    }
    writeRow(values);
    if (isLastRow) {
      dashedLine();
    }
  }

  /**
   * Dashed line.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void dashedLine() throws IOException {
    dashedLine(totalWidth);
    newLine();
  }

  /**
   * Dashed line.
   *
   * @param width the width
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void dashedLine(int width) throws IOException {
    for (int i = 0; i < width; i++) {
      sw.append("-");
    }
  }

  /**
   * New line.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void newLine() throws IOException {
    sw.append("\n");
  }

  /**
   * Empty spaces.
   *
   * @param len the len
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void emptySpaces(int len) throws IOException {
    for (int sp = 0; sp < len; sp++) {
      sw.append(" ");
    }
  }

  /**
   * Write cell.
   *
   * @param align the align
   * @param val   the val
   * @param width the width
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void writeCell(Align align, String val, int width) throws IOException {
    if (align == Align.RIGHT) {
      rightAlign(val, width);
    } else if (align == Align.LEFT) {
      leftAlign(val, width);
    } else {
      centerAlign(val, width);
    }
  }

  /**
   * Right align.
   *
   * @param val   the val
   * @param width the width
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void rightAlign(String val, int width) throws IOException {
    int strLen = val.length();
    if (width > strLen) {
      for (int sp = 0; sp < (width - strLen); sp++) {
        sw.append(" ");
      }
      sw.append(val);
    } else if (width < strLen) {
      sw.append(val, 0, width);
    } else {
      sw.append(val);
    }
  }

  /**
   * Left align.
   *
   * @param val   the val
   * @param width the width
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void leftAlign(String val, int width) throws IOException {
    int strLen = val.length();
    if (width > strLen) {
      sw.append(val);
      for (int sp = 0; sp < (width - strLen); sp++) {
        sw.append(" ");
      }
    } else if (width < strLen) {
      sw.append(val, 0, width);
    } else {
      sw.append(val);
    }
  }

  /**
   * Center align.
   *
   * @param val   the val
   * @param width the width
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void centerAlign(String val, int width) throws IOException {
    int strLen = val.length();
    int spaceLength = width - strLen;
    if (spaceLength > 0) {
      emptySpaces(spaceLength / 2);
      sw.append(val);
      emptySpaces(spaceLength - spaceLength / 2);
    } else if (spaceLength < 0) {
      sw.append(val, 0, width);
    } else {
      sw.append(val);
    }
  }

}
