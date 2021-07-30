package com.insta.hms.common;

import au.com.bytecode.opencsv.CSVWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class StdCsvReportEngine extends StdReportEngine {

  static Logger log = LoggerFactory.getLogger(StdCsvReportEngine.class);

  Writer writ;
  CSVWriter writer;

  public StdCsvReportEngine(StdReportDesc desc, StdReportParams params, Writer writ) {
    super(desc, params);
    this.writ = writ;
  }

  protected void writeMainHeader(List<Column> columns, boolean isSummary) throws Exception {
    writer.writeNext(columnNamesArray(columns));
  }

  protected void initialize() throws Exception {
    writer = new CSVWriter(writ);
  }

  protected void cleanup() throws Exception {
    writer.flush();
  }

  protected void writeTitle(String title, String hspNameAddrs, String fdate, String tdate,
      String filterDesc) throws Exception {
    if (hspNameAddrs != null && !hspNameAddrs.equals("")) {
      writer.writeNext(writeNormalData(hspNameAddrs));
    }
    writer.writeNext(writeNormalData(title));
    if (filterDesc != null) {
      writer.writeNext(writeNormalData(filterDesc));
    }
    if (fdate != null || tdate != null) {
      writer.writeNext(writeNormalData(fdate + " to " + tdate));
    }
  }

  protected void writeTitle(String title, String fdate, String tdate, String filterDesc)
      throws Exception {
    writer.writeNext(writeNormalData(title));
    if (filterDesc != null) {
      writer.writeNext(writeNormalData(filterDesc));
    }
    if (fdate != null || tdate != null) {
      writer.writeNext(writeNormalData(fdate + " to " + tdate));
    }
  }

  protected void writeListRow(List<String> values) {
    writer.writeNext(values.toArray(new String[0]));
  }

  protected void writeSumRow(List<String> values, boolean isGroupChanged, boolean isLastRow,
      boolean isLastColTotal, boolean isRowTotal) {
    writer.writeNext(values.toArray(new String[0]));
  }

  protected void writeTotals(String title, Map<String, BigDecimal> groupTotals, int indentLevel)
      throws Exception {
  }

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

    String[] grpHeaders = new String[] { header.toString() };
    writer.writeNext(grpHeaders);
  }

  protected void writeListGroupTotals(String title, int titleColSpan, List<String> valueList)
      throws Exception {
    int index = 0;
    int size = titleColSpan + valueList.size();
    String[] grpTotalsList = new String[size];
    for (int j = 0; j < titleColSpan - 1; j++) {
      grpTotalsList[index] = new String("");
      index++;
    }
    if (titleColSpan != 0) { // first column is the aggregatable, hence do not print the title.
      grpTotalsList[index++] = new String(title);
    }

    for (String value : valueList) {
      grpTotalsList[index] = new String(value);
      index++;
    }
    writer.writeNext(grpTotalsList);
  }

  private String[] columnNamesArray(List<Column> columns) {
    String[] str = new String[columns.size()];
    int index = 0;
    for (Column col : columns) {
      str[index] = col.header;
      index++;
    }
    return str;
  }

  private String[] writeNormalData(String data) {
    int colSize = params.getDisplayFields().size();
    int index = 0;
    String[] titleStr = new String[colSize];
    if (colSize > 1) {
      for (int j = 0; j < colSize / 2; j++) {
        titleStr[index] = new String("");
        index++;
      }
    }
    titleStr[index] = new String(data);
    return titleStr;
  }
}
