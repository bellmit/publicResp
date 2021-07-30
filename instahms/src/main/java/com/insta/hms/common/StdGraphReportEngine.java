package com.insta.hms.common;

import flexjson.JSONSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StdGraphReportEngine extends StdReportEngine {

  static Logger log = LoggerFactory.getLogger(StdGraphReportEngine.class);

  Writer writer;
  GraphObject graphObj;
  private boolean isFirstRow = true;
  List<String> totals;

  public StdGraphReportEngine(StdReportDesc desc, StdReportParams params, Writer writer) {
    super(desc, params);
    this.writer = writer;
  }

  @Override
  protected void initialize() throws Exception {
    graphObj = new GraphObject();
    graphObj.setTrendType(params.getTrendType());
    params.setNullZero(true);
    params.setSkipRepeatedValues(false);
    // Initialize the JSON object
    writer.append("{\"data\":{");
  }

  @Override
  protected void cleanup() throws Exception {
    JSONSerializer serializer = new JSONSerializer().exclude("class");
    if (this.totals != null) {
      writer.append(',');
      String selectedField = desc.getFieldDisplayName(params.getDisplayFields().get(0));
      writer.append(serializer.serialize("Total " + selectedField) + ":");
      writer.append(this.totals.toString());
    }
    serializer = new JSONSerializer().exclude("class").include("filter1Set")
        .include("displayFields").include("filter2Set");
    String jsonObject = serializer.serialize(graphObj);
    writer.append("},\"graphObj\":");
    writer.append(jsonObject);
    // closing the JSON object
    writer.append("}");
    writer.flush();
  }

  @Override
  protected void writeTitle(String title, String fromDate, String toDate, String filterDesc)
      throws Exception {
    if (filterDesc != null) {
      graphObj.setFilterDesc(filterDesc);
    }

    if (fromDate != null || toDate != null) {
      graphObj.setFromDate(fromDate);
      graphObj.setToDate(toDate);
    }

    List<String> fieldNames = new ArrayList<String>();
    for (int i = 0; i < params.getDisplayFields().size(); i++) {
      fieldNames.add(desc.getFieldDisplayName(params.getDisplayFields().get(i)));
    }
    graphObj.setDisplayFields(fieldNames);

    if (title != null) {
      graphObj.setTitle(title);
    }
  }

  @Override
  protected void writeTitle(String title, String hspNameAddrs, String fromDate, String toDate,
      String filterDesc) throws Exception {
    if (hspNameAddrs != null && !hspNameAddrs.equals("")) {
      graphObj.setHspNameAddrs(hspNameAddrs);
    }

    if (filterDesc != null) {
      graphObj.setFilterDesc(filterDesc);
    }

    if (fromDate != null || toDate != null) {
      graphObj.setFromDate(fromDate);
      graphObj.setToDate(toDate);
    }

    List<String> fieldNames = new ArrayList<String>();
    for (int i = 0; i < params.getDisplayFields().size(); i++) {
      fieldNames.add(desc.getFieldDisplayName(params.getDisplayFields().get(i)));
    }
    graphObj.setDisplayFields(fieldNames);

    if (title != null) {
      graphObj.setTitle(title);
    }
  }

  @Override
  protected void writeMainHeader(List<Column> columns, boolean isSummary) throws Exception {
    String vgSub = params.getSumGroupVertSub();
    graphObj.setFilterName1(columns.get(0).header);
    int startIndex = 1;
    if (vgSub != null) {
      graphObj.setFilterName2(columns.get(1).header.equals("Data") ? null : columns.get(1).header);
      startIndex = 2;
    }
    // Extracting the Trend Values, (excluding the last column Total).
    List<Column> names = columns.subList(startIndex, columns.size() - 1);
    String[] trend = new String[names.size()];
    for (int i = 0; i < names.size(); i++) {
      trend[i] = "\"" + names.get(i).header + "\"";
    }
    writer.append("\"trend\":");
    writer.append(Arrays.toString(trend));
    writer.append(",");
  }

  @Override
  protected void writeGroupHeader(String numbering, List<String> headerNames,
      List<String> headerVal, int level) throws Exception {

  }

  @Override
  protected void writeListRow(List<String> values) throws Exception {

  }

  @Override
  protected void writeListGroupTotals(String title, int titleColSpan, List<String> valueList)
      throws Exception {

  }

  @Override
  protected void writeSumRow(List<String> values, boolean isGroupChange, boolean isLastRow,
      boolean isLastColTotal, boolean isRowTotal) throws Exception {
    String jsonFormat;
    String key;

    int rowMax = 0;
    int startIndex;
    // put quotes around the title;the first field of values
    if (params.getSumGroupVertSub() == null) {
      key = (values.get(0) == null ? "" : values.get(0).trim());

      if (key != null && !key.equals("Total")) {
        graphObj.filter1Set.add(key);
      }
      String fieldName = desc.getFieldDisplayName(params.getDisplayFields().get(0));
      key = key + " " + fieldName;
      startIndex = 1;
      jsonFormat = values.subList(1, values.size()).toString();
    } else {
      if (values.get(1) != null && values.get(1).equals("Total")
          && !params.getSumGroupVertSub().equals("_data")) {
        String selectedField = desc.getFieldDisplayName(params.getDisplayFields().get(0));
        // Concat display field instead of Total
        key = (values.get(0) == null ? "" : values.get(0).trim()) + " " + selectedField;
      } else {
        key = (values.get(0) == null ? "" : values.get(0).trim()) + " "
            + (values.get(1) == null ? "" : values.get(1).trim());
      }
      startIndex = 2;
      // Create a deep copy
      List<String> newValues = new ArrayList<String>(values.subList(2, values.size()));

      // Calculating Grand Total of display field when vg and vgSub selected.
      if (!params.getSumGroupVertSub().equals("_data") && values.get(1) != null
          && values.get(1).equals("Total")) {
        if (this.totals == null) {
          this.totals = new ArrayList<String>(newValues);
        } else {
          for (int i = 0; i < newValues.size(); i++) {
            Float sum = Float.parseFloat(this.totals.get(i)) + Float.parseFloat(newValues.get(i));
            this.totals.set(i, sum.toString());
          }
        }
      }

      if (values.get(0) != null && !values.get(0).equals("Total")) {
        graphObj.filter1Set.add(values.get(0).trim());
      }
      if (values.get(1) != null && !values.get(1).equals("Total")
          && !params.getSumGroupVertSub().equals("_data")) {
        graphObj.filter2Set.add(values.get(1).trim());
      }

      jsonFormat = newValues.toString();
    }

    for (String value : values.subList(startIndex, values.size())) {
      rowMax = Math.max(rowMax, value.length());
    }
    graphObj.setMaxDigits(Math.max(graphObj.getMaxDigits(), rowMax));
    if (!this.isFirstRow) {
      writer.append(',');
    }
    this.isFirstRow = false;
    JSONSerializer serializer = new JSONSerializer().exclude("class");
    writer.append(serializer.serialize(key) + ":");
    writer.append(jsonFormat);
  }

  protected class GraphObject {
    String title;
    String fromDate;
    String toDate;
    String hospNameAddrs;
    String filterDesc;
    String trendType;
    String filterName1;
    String filterName2;
    List<String> displayFields;
    Set<String> filter1Set = new HashSet<String>();
    Set<String> filter2Set = new HashSet<String>();
    int maxDigits = 0;

    public int getMaxDigits() {
      return maxDigits;
    }

    public void setMaxDigits(int maxDigits) {
      this.maxDigits = maxDigits;
    }

    public List<String> getFilter1Set() {
      List<String> sortedList = new ArrayList<String>(this.filter1Set);
      Collections.sort(sortedList, new DateComparator());
      return sortedList;
    }

    public void setFilter1Set(Set<String> filter1Set) {
      this.filter1Set = filter1Set;
    }

    public List<String> getFilter2Set() {
      List<String> sortedList = new ArrayList<String>(this.filter2Set);
      Collections.sort(sortedList, new DateComparator());
      return sortedList;
    }

    public void setFilter2Set(Set<String> filter2Set) {
      this.filter2Set = filter2Set;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getFromDate() {
      return fromDate;
    }

    public void setFromDate(String fromDate) {
      this.fromDate = fromDate;
    }

    public String getToDate() {
      return toDate;
    }

    public void setToDate(String toDate) {
      this.toDate = toDate;
    }

    public List<String> getDisplayFields() {
      return displayFields;
    }

    public void setDisplayFields(List<String> displayFields) {
      this.displayFields = displayFields;
    }

    public String getHspNameAddrs() {
      return hospNameAddrs;
    }

    public void setHspNameAddrs(String hspNameAddrs) {
      hospNameAddrs = hspNameAddrs;
    }

    public String getFilterDesc() {
      return filterDesc;
    }

    public void setFilterDesc(String filterDesc) {
      this.filterDesc = filterDesc;
    }

    public String getTrendType() {
      return trendType;
    }

    public void setTrendType(String trendType) {
      this.trendType = trendType;
    }

    public String getFilterName1() {
      return filterName1;
    }

    public void setFilterName1(String filterName1) {
      this.filterName1 = filterName1;
    }

    public String getFilterName2() {
      return filterName2;
    }

    public void setFilterName2(String filterName2) {
      this.filterName2 = filterName2;
    }

  }

  class DateComparator implements Comparator<String> {
    DateFormat format1 = new SimpleDateFormat("dd-MM-yyyy");
    DateFormat format2 = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    public int compare(String o1, String o2) {
      try {
        if (o1 == null && o2 == null) {
          return 0;
        }
        if (o1 == null) {
          return -1;
        }
        if (o2 == null) {
          return 1;
        }
        return format2.parse(o1).compareTo(format2.parse(o2));
      } catch (ParseException exception) {
        try {
          return format1.parse(o1).compareTo(format1.parse(o2));
        } catch (ParseException ex) {
          return o1.compareTo(o2);
        }
      }
    }

  }

}
