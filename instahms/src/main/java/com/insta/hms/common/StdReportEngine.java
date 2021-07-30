package com.insta.hms.common;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.ResultSetDynaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class StdReportEngine.
 */
public abstract class StdReportEngine {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(StdReportEngine.class);

  /** The desc. */
  protected StdReportDesc desc;

  /** The params. */
  protected StdReportParams params;

  /** The con. */
  protected Connection con;

  /** The ps. */
  protected PreparedStatement ps;

  /** The rs. */
  protected ResultSet rs;

  /** The columns. */
  protected List<Column> columns;

  /** The value column index. */
  protected HashMap<String, Integer> valueColumnIndex;

  /**
   * The Enum Align.
   */
  public static enum Align {
    LEFT, RIGHT, CENTER
  }

  /**
   * The Class Column.
   */
  protected static class Column {

    /** The header. */
    public String header; // display name

    /** The width. */
    public float width;

    /** The align. */
    public Align align;

    /** The char width. */
    public int charWidth;

    /**
     * Instantiates a new column.
     *
     * @param header the header
     * @param width  the width
     * @param align  the align
     */
    public Column(String header, float width, Align align) {
      this.header = header;
      this.width = width;
      this.align = align;
    }

    /**
     * Instantiates a new column.
     *
     * @param field the field
     */
    public Column(StdReportDesc.Field field) {
      this.header = field.getDisplayName();
      this.width = field.getWidth();
      this.align = field.getDataType().equals("numeric") || field.getDataType().equals("integer")
          ? Align.RIGHT
          : Align.LEFT;
    }
  }

  /**
   * Instantiates a new std report engine.
   *
   * @param desc   the desc
   * @param params the params
   */
  public StdReportEngine(StdReportDesc desc, StdReportParams params) {
    this.desc = desc;
    this.params = params;
    columns = new ArrayList<Column>();
    valueColumnIndex = new HashMap<String, Integer>();
  }

  /**
   * Close.
   *
   * @throws SQLException the SQL exception
   */
  public void close() throws SQLException {
    Statement st = rs == null ? null : rs.getStatement();
    DataBaseUtil.closeConnections(con, st, rs);
  }

  /**
   * Write report.
   *
   * @throws Exception the exception
   */
  public void writeReport() throws Exception {
    try {
      if (params.getType().equals("list")) {
        writeListReport();
      } else if (params.getType().equals("sum") || params.getType().equals("trend")
          || params.getType().equals("vtrend")) {
        writeSumReport();
      }
    } finally {
      close();
    }
  }

  /**
   * Write list report.
   *
   * @throws Exception the exception
   */
  public void writeListReport() throws Exception {
    con = DataBaseUtil.getConnection(60, true);
    rs = getListResultSet(con, desc, params);

    // initialize the columns and validate
    List<String> fields = params.getDisplayFields();
    for (String fieldName : fields) {
      if (fieldName.equals("_sl") || fieldName.equals("_gsl")) {
        columns.add(new Column("Sl No.", (float) 40, Align.RIGHT));
      } else {
        StdReportDesc.Field field = desc.getField(fieldName);
        if (field == null) {
          throw new IllegalArgumentException("Not a valid field identifier: " + fieldName);
        }
        columns.add(new Column(field));
      }
    }

    // start the document etc.
    initialize();

    // add the title
    String fromDate = null;
    String toDate = null;
    if (params.getSelectedDateField() != null && !params.getSelectedDateField().equals("")) {
      fromDate = DateUtil.formatDate(params.getFromDate());
      toDate = DateUtil.formatDate(params.getToDate());
    }

    if (params.getHospNameAndAddrsHeader() != null
        && !params.getHospNameAndAddrsHeader().equals("")) {
      writeTitle(params.getPrintTitle(), params.getHospNameAndAddrsHeader(), fromDate, toDate,
          params.getFilterDescription().equals("") ? null
              : "Filtered on: " + params.getFilterDescription());
    } else {
      writeTitle(params.getPrintTitle(), fromDate, toDate,
          params.getFilterDescription().equals("") ? null
              : "Filtered on: " + params.getFilterDescription());
    }

    // create the main table for adding all rows
    writeMainHeader(columns);

    List<String> groups = params.getListGroups();
    Object[] oldGroupVals = new Object[groups.size()];
    for (int i = 0; i < oldGroupVals.length; i++) {
      oldGroupVals[i] = null;
    }

    // Initialize a map of sum values for each field. Need an array, one for
    // each group level
    Map<String, Object>[] groupTotals = new HashMap[groups.size()];
    Map<String, Object> grandTotals = new HashMap();
    resetGroupTotals(groupTotals, 0);

    for (String field : fields) {
      grandTotals.put(field, null);
    }
    boolean hasAggregates = false;

    // get the rows, and write every row.
    ResultSetDynaClass rsdc = new ResultSetDynaClass(rs, false);
    Iterator rows = rsdc.iterator();
    boolean firstRow = true;
    int addedRows = 0;
    int serialIndex = 1;
    int groupSerialIndex = 1;
    int groupHeaderIndex = 1;
    HashMap<String, Object> prvsValues = new HashMap<String, Object>();

    while (rows.hasNext()) {
      DynaBean row = (DynaBean) rows.next();

      int changeLevel = firstRow ? 0 : getGroupChangeLevel(row, groups, oldGroupVals);
      boolean groupChange = (changeLevel < groups.size());

      // write totals for prvs groups: inner groups first
      if (!firstRow) {
        for (int i = groups.size() - 1; i >= changeLevel; i--) {
          String formattedValue = formatValue(oldGroupVals[i], groups.get(i), true);
          String titleStr = formattedValue + " Total:";
          if (hasAggregates) {
            writeListTotals(titleStr, groupTotals[i], i);
          }
          groupSerialIndex = 1;
        }
      }
      resetGroupTotals(groupTotals, changeLevel);

      // Write new headers for all changed groups
      for (int i = changeLevel; i < groups.size(); i++) {
        String group = groups.get(i);

        List<String> headerNames = new ArrayList();
        List<String> headerValues = new ArrayList();

        String numbering = "";
        if (params.getGroupNumbering() && i == groups.size() - 1) {
          numbering += groupHeaderIndex;
        }

        headerNames.add(desc.getField(group).getDisplayName());
        headerValues.add(formatValue(row.get(group), group, true));

        List<String> addnlFields = params.getHeaderFields().get(i);
        for (String afield : addnlFields) {
          headerNames.add(desc.getField(afield).getDisplayName());
          headerValues.add(formatValue(row.get(afield), afield, true));
        }

        writeGroupHeader(numbering, headerNames, headerValues, i);

        if (i == groups.size() - 1) {
          ++groupHeaderIndex;
        }
      }

      // save the current val as the new groupVal
      for (int i = 0; i < oldGroupVals.length; i++) {
        oldGroupVals[i] = row.get(groups.get(i));
      }

      // write the fields
      List<String> valueList = new ArrayList<String>();

      boolean stopSkip = false;
      for (String fieldName : fields) {
        if (fieldName.equals("_sl")) {
          Object value = serialIndex;
          valueList.add(value.toString());
          serialIndex++;
        } else if (fieldName.equals("_gsl")) {
          Object value = groupSerialIndex;
          valueList.add(value.toString());
          groupSerialIndex++;

        } else {
          Object value = row.get(fieldName);
          boolean skip = false;
          if (params.isSkipRepeatedValues() && !firstRow && !groupChange && !stopSkip) {
            StdReportDesc.Field field = desc.getField(fieldName);
            if (field != null && !field.getDataType().equalsIgnoreCase("numeric")) {
              Object prvsValue = prvsValues.get(fieldName);
              if (prvsValue == null && value == null) {
                skip = true;
              } else if (prvsValue != null && value != null && prvsValue.equals(value)) {
                skip = true;
              }
            }
          }
          stopSkip = !skip; // stop skipping at the first non-repeating column
          prvsValues.put(fieldName, value);
          String formattedValue = skip ? "" : formatValue(value, fieldName);
          valueList.add(formattedValue);
        }
      }
      writeListRow(valueList);

      // add to totals
      for (String field : fields) {
        if (field.equals("_sl") || field.equals("_gsl")) {
          continue;
        }

        for (int i = 0; i < groups.size(); i++) {
          aggregateRow(groupTotals[i], row, field);
        }

        if (aggregateRow(grandTotals, row, field)) {
          hasAggregates = true;
        }
      }

      firstRow = false;
    }

    // write residual totals: all of them
    for (int i = groups.size() - 1; i >= 0; i--) {
      String formattedValue = formatValue(oldGroupVals[i], groups.get(i), true);
      String titleStr = formattedValue + " Total:";
      if (hasAggregates) {
        writeListTotals(titleStr, groupTotals[i], i);
        groupSerialIndex = 1;
      }
    }

    // write Grand total
    if (hasAggregates) {
      writeListTotals("Grand Total:", grandTotals, -1);
    }

    cleanup();
  }

  /**
   * Write list totals.
   *
   * @param title       the title
   * @param groupTotals the group totals
   * @param indentLevel the indent level
   * @throws Exception the exception
   */
  protected void writeListTotals(String title, Map<String, Object> groupTotals, int indentLevel)
      throws Exception {

    List<String> valueList = new ArrayList<String>();

    boolean titleAdded = false;
    int titleColSpan = 0;

    for (String field : params.getDisplayFields()) {
      StdReportDesc.Field fieldDesc = desc.getField(field);

      if (field.equals("_sl") || field.equals("_gsl") || fieldDesc.getAggFunction() == null) {
        // not an aggregate field: before title, just skip, otherwise
        // put blank
        if (!titleAdded) {
          titleColSpan++;
        } else {
          valueList.add("");
        }

      } else {
        Object aggValue = aggRetrieve(fieldDesc.getAggFunction(), groupTotals.get(field));
        valueList.add(formatValue(aggValue, field));
        titleAdded = true;
      }
    }

    // now write the list of aggregates out
    writeListGroupTotals(title, titleColSpan, valueList);
  }

  /**
   * Reset group totals.
   *
   * @param groupTotals the group totals
   * @param changeLevel the change level
   */
  protected void resetGroupTotals(Map<String, Object>[] groupTotals, int changeLevel) {
    for (int i = changeLevel; i < params.getListGroups().size(); i++) {
      groupTotals[i] = new HashMap();
      resetTotals(groupTotals[i], params.getDisplayFields());
    }
  }

  /**
   * Reset totals.
   *
   * @param totals the totals
   * @param fields the fields
   */
  protected void resetTotals(Map<String, Object> totals, List<String> fields) {
    for (String field : fields) {
      totals.put(field, null);
    }
  }

  /**
   * Agg accumulate. Aggregating functions: this is very similar to postgres CREATE AGGREGATE. We
   * have two functions: one to accumulate, and one to get the final value. When accumulating, we
   * pass in the state, a value and an optional weight. When retrieving the final value, we pass in
   * the state and get back the final value.
   *
   * @param func   the func
   * @param state  the state
   * @param value  the value
   * @param weight the weight
   * @return the object
   */
  protected Object aggAccumulate(String func, Object state, Object value, Object weight) {

    if (func.equals("sum")) {
      return ConversionUtils.addNumber(state, value);

    } else if (func.equals("min")) {
      // todo: need conversionUtils functions for min/max
      return (((BigDecimal) state).compareTo((BigDecimal) value) > 0) ? value : state;

    } else if (func.equals("max")) {
      return (((BigDecimal) state).compareTo((BigDecimal) value) < 0) ? value : state;

    } else {
      BigDecimal[] avg = (state == null) ? new BigDecimal[2] : (BigDecimal[]) state;
      ;

      if (func.equals("avg")) {
        avg[0] = (BigDecimal) ConversionUtils.addNumber(avg[0], value);
        avg[1] = (BigDecimal) ConversionUtils.addNumber(avg[1], BigDecimal.ONE);

      } else if (func.equals("wavg")) {
        avg[0] = (BigDecimal) ConversionUtils.addNumber(avg[0],
            ((BigDecimal) weight).multiply((BigDecimal) value));
        avg[1] = (BigDecimal) ConversionUtils.addNumber(avg[1], weight);
      }
      return avg;
    }
  }

  /**
   * Agg accumulate. Sometimes we need to add two states themselves (when summing up totals into
   * grand-totals, for example)
   *
   * @param func       the func
   * @param state      the state
   * @param stateToAdd the state to add
   * @return the object
   */
  protected Object aggAccumulate(String func, Object state, Object stateToAdd) {

    if (func.equals("avg") || func.equals("wavg")) {
      BigDecimal[] curVal = (state == null) ? new BigDecimal[2] : (BigDecimal[]) state;
      ;
      BigDecimal[] addVal = (BigDecimal[]) stateToAdd;
      BigDecimal[] result = new BigDecimal[2];

      result[0] = (BigDecimal) ConversionUtils.addNumber(curVal[0], addVal[0]);
      result[1] = (BigDecimal) ConversionUtils.addNumber(curVal[1], addVal[1]);

      return result;

    } else {
      return ConversionUtils.addNumber(state, stateToAdd);
    }
  }

  /**
   * Agg retrieve.
   *
   * @param func  the func
   * @param state the state
   * @return the object
   */
  protected Object aggRetrieve(String func, Object state) {

    if (func.equals("sum") || func.equals("min") || func.equals("max")) {
      return state;

    } else if (func.equals("avg") || func.equals("wavg")) {
      if (state != null) {
        BigDecimal totalValue = ((BigDecimal[]) state)[0];
        BigDecimal totalWeight = ((BigDecimal[]) state)[1];
        return totalValue.divide(totalWeight, BigDecimal.ROUND_HALF_UP);
      }
    }
    return null;
  }

  /**
   * Aggregate row. Used by list reports to handle aggregates: the totals are stored in a map based
   * on the field name.
   *
   * @param totals the totals
   * @param row    the row
   * @param field  the field
   * @return true, if successful
   */
  protected boolean aggregateRow(Map<String, Object> totals, DynaBean row, String field) {

    StdReportDesc.Field fieldDesc = desc.getField(field);
    String aggFunction = fieldDesc.getAggFunction();
    if (aggFunction == null) {
      return false;
    }

    Object value = row.get(field);
    if (value == null) {
      return false;
    }

    Object state = totals.get(field);
    if (aggFunction.equals("wavg")) {
      // need secondary field
      Object weight = row.get(fieldDesc.getAggWeight());
      totals.put(field, aggAccumulate(aggFunction, state, value, weight));

    } else {
      totals.put(field, aggAccumulate(aggFunction, state, value, null));
    }

    return true;
  }

  /**
   * Format agg.
   *
   * @param func      the func
   * @param state     the state
   * @param fieldName the field name
   * @return the string
   */
  public String formatAgg(String func, Object state, String fieldName) {
    return formatValue(aggRetrieve(func, state), fieldName);
  }

  /**
   * Format value.
   *
   * @param value     the value
   * @param fieldName the field name
   * @return the string
   */
  public String formatValue(Object value, String fieldName) {
    return formatValue(value, fieldName, false);
  }

  /**
   * Format value.
   *
   * @param value          the value
   * @param fieldName      the field name
   * @param useNoneForNull the use none for null
   * @return the string
   */
  public String formatValue(Object value, String fieldName, boolean useNoneForNull) {

    DateUtil dateUtil = new DateUtil();

    if (value == null) {
      return useNoneForNull ? "(None)" : (params.isNullZero() ? "0.00" : "");
    }

    if ("".equals(value.toString().trim()) && useNoneForNull) {
      return "(None)";
    }

    String dtype = "";
    StdReportDesc.Field field = desc.getField(fieldName);

    if (field == null) {
      // pseudo fields such as _count
      if (fieldName.equals("_count")) {
        dtype = "integer";
      } else {
        dtype = "";
      }
    } else {
      dtype = field.getDataType();
    }

    if (dtype.equalsIgnoreCase("date")) {
      return dateUtil.getDateFormatter().format(value);
    }

    if (dtype.equalsIgnoreCase("time")) {
      return dateUtil.getTimeFormatter().format(value);
    }

    if (dtype.equalsIgnoreCase("timestamp")) {
      return dateUtil.getTimeStampFormatterSecs().format(value);
    }
    if (dtype.equalsIgnoreCase("datewithoutdays")) {
      return dateUtil.getDateFormatterWithoutDays().format(value);
    }
    if (dtype.equalsIgnoreCase("timestampnosecs")) {
      return dateUtil.getTimeStampFormatter().format(value);
    }

    if (dtype.equalsIgnoreCase("numeric")) {
      String decimalType = field.getDecimalType();
      if (decimalType == null) {
        // try and guess.
        if (QueryBuilder.hasWord(fieldName, "qty") || QueryBuilder.hasWord(fieldName, "quantity")
            || QueryBuilder.hasWord(fieldName, "level")) {
          decimalType = "quantity";
        } else if (QueryBuilder.hasWord(fieldName, "amt")
            || QueryBuilder.hasWord(fieldName, "amount")
            || QueryBuilder.hasWord(fieldName, "rate")) {
          decimalType = "amount";
        } else if (QueryBuilder.hasWord(fieldName, "per")
            || QueryBuilder.hasWord(fieldName, "percent")) {
          decimalType = "percent";
        } else if (QueryBuilder.hasWord(fieldName, "num")
            || QueryBuilder.hasWord(fieldName, "count")) {
          decimalType = "integer";
        }
      }

      if (decimalType == null) {
        return value.toString(); // use the native type based on the db data type
      } else if (decimalType.equals("amount")) {
        return (ConversionUtils.setScale((BigDecimal) value)).toString();
      } else if (decimalType.equals("quantity") || decimalType.equals("percent")) {
        return (((BigDecimal) value).setScale(2, BigDecimal.ROUND_HALF_UP)).toString();
      } else if (decimalType.equals("integer")) {
        if (value instanceof Integer || value instanceof Long) {
          return value.toString();
        } else if (value instanceof BigDecimal) {
          return (((BigDecimal) value).setScale(0, BigDecimal.ROUND_HALF_UP)).toString();
        }
      } else {
        int numDecimals = Integer.parseInt(decimalType);
        return (((BigDecimal) value).setScale(numDecimals, BigDecimal.ROUND_HALF_UP)).toString();
      }
    }

    if (dtype.equalsIgnoreCase("integer")) {
      return value.toString();
    }

    return value.toString();
  }

  /**
   * Gets the list result set. Return a result set based on report description and report params
   *
   * @param con    the con
   * @param desc   the desc
   * @param params the params
   * @return the list result set
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ResultSet getListResultSet(Connection con, StdReportDesc desc, StdReportParams params)
      throws SQLException, Exception {
    String queryStr = desc.getListResultQuery(params);
    log.debug("Final query string: " + queryStr);
    con.setAutoCommit(false); // required for setFetchSize to work
    PreparedStatement ps = con.prepareStatement(queryStr);
    ps.setFetchSize(1000); // fetch only 1000 rows at a time, otherwise,
    // gets the entire data
    int index = 1;
    List queryParams = desc.getQueryParams();
    for (Object param : queryParams) {
      if (param != null) {
        ps.setObject(index++, param);
      }
    }
    ResultSet rs = ps.executeQuery();
    return rs;
  }

  /**
   * Gets the sum result set.
   *
   * @param con    the con
   * @param desc   the desc
   * @param params the params
   * @return the sum result set
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ResultSet getSumResultSet(Connection con, StdReportDesc desc, StdReportParams params)
      throws SQLException, Exception {
    String queryStr = desc.getSummaryResultQuery(params);
    log.debug("Final query string: " + queryStr);
    con.setAutoCommit(false);
    PreparedStatement ps = con.prepareStatement(queryStr);
    ps.setFetchSize(1000); // fetch only 100 rows at a time
    int index = 1;
    List queryParams = desc.getQueryParams();
    for (Object param : queryParams) {
      if (param != null) {
        ps.setObject(index++, param);
      }
    }
    ResultSet rs = ps.executeQuery();
    return rs;
  }

  /**
   * Gets the group change level. Get the level of group change. 0 means all groups have changed, N
   * means no change.
   *
   * @param row     the row
   * @param groups  the groups
   * @param oldVals the old vals
   * @return the group change level
   */
  public static int getGroupChangeLevel(DynaBean row, List<String> groups, Object[] oldVals) {
    // for inner levels, any outer level change triggers a change, so check
    // from topmost
    // level downwards, till we reach a change. This is the level of change.
    int index;
    for (index = 0; index < groups.size(); index++) {
      String oldVal = oldVals[index] != null ? oldVals[index].toString() : null;
      Object newValObj = row.get(groups.get(index));
      String newVal = newValObj != null ? newValObj.toString() : null;
      if ((oldVal == null) && (newVal == null)) {
        continue;
      }
      if ((oldVal == null) && (newVal != null)) {
        break;
      }
      if ((oldVal != null) && (newVal == null)) {
        break;
      }
      if (!oldVal.equals(newVal)) {
        break;
      }
    }
    return index;
  }

  /**
   * Gets the sum column details. Get the column details for a summary output
   *
   * @return the sum column details
   * @throws SQLException the SQL exception
   */
  protected void getSumColumnDetails() throws SQLException {
    int dataWidth = 0;

    for (String dispField : params.getDisplayFields()) {
      int width = 0;
      if (dispField.equals("_count")) {
        width = 40;
      } else {
        StdReportDesc.Field field = desc.getField(dispField);
        if (field == null) {
          throw new IllegalArgumentException("Not a valid field identifier: " + dispField);
        }
        width = field.getWidth();
      }
      dataWidth = (width > dataWidth) ? width : dataWidth;
    }

    String vg = params.getSumGroupVert();
    String hg = params.getSumGroupHoriz();
    String vgSub = params.getSumGroupVertSub();

    /*
     * V1 level is mandatory and cannot be data.
     */
    if (vg == null) {
      throw new IllegalArgumentException("Vertical Group is mandatory");
    }

    StdReportDesc.Field field = null;
    if (vg.equals("_period")) {
      columns.add(new Column("Period", (float) 60, Align.LEFT));
    } else {
      field = desc.getField(vg);
      if (field == null) {
        throw new IllegalArgumentException(
            "Not a valid field identifier for Vertical group: " + vg);
      }
      columns.add(new Column(field.getDisplayName(), (float) field.getWidth(), Align.LEFT));
    }

    /*
     * Ensure data field is supplied when multiple display fields are there
     */
    if (params.getDisplayFields().size() > 1) {
      if ((vgSub == null || !vgSub.equals("_data")) && (hg == null || !hg.equals("_data"))) {
        throw new IllegalArgumentException("Horizontal or Vertical Sub must be for Summary Fields");
      }
    }

    /*
     * Process the vertical sub group
     */
    if (params.getSumGroupVertSub() != null) {
      if (params.getSumGroupVertSub().equals("_data")) {
        columns.add(new Column("Data", (float) dataWidth, Align.LEFT));
      } else {
        field = desc.getField(params.getSumGroupVertSub());
        if (field == null) {
          throw new IllegalArgumentException("Not a valid field identifier for Vertical Sub group: "
              + params.getSumGroupVertSub());
        }
        columns.add(new Column(field.getDisplayName(), (float) field.getWidth(), Align.LEFT));
      }
    }

    /*
     * Process the vertical sub group
     */
    if (params.getSumGroupHoriz() != null) {
      if (params.getSumGroupHoriz().equals("_data")) {
        // iterate through the display fields, and add them
        for (String fieldName : params.getDisplayFields()) {
          if (fieldName.equals("_count")) {
            columns.add(new Column("Count", (float) 40, Align.RIGHT));
          } else {
            field = desc.getField(fieldName);
            columns.add(new Column(field.getDisplayName(), (float) field.getWidth(), Align.RIGHT));
          }
        }
      } else {
        // one column for each possible VALUE of the data
        // also save a mapping to the name and the order of display
        String hgName = params.getSumGroupHoriz();
        field = desc.getField(hgName);
        if (!params.getType().equals("trend") && field == null) {
          throw new IllegalArgumentException(
              "Not a valid field identifier for Horizontal group: " + params.getSumGroupHoriz());
        }
        int width = 0;
        if (params.getType().equals("trend")) {
          // allowed values are calculated from the trend and date
          // range
          width = dataWidth;
          List<String> dates = DateUtil.getDatesInRange(params.getFromDate(), params.getToDate(),
              params.getTrendType());
          for (String date : dates) {
            columns.add(new Column(date, width, Align.RIGHT));
            valueColumnIndex.put(date, columns.size() - 1);
          }

        } else if ((field.getAllowedValues() != null) && (field.getAllowedValues().size() > 0)) {
          log.debug("Horizontal field has fixed values: " + hgName);
          width = dataWidth > field.getWidth() ? dataWidth : field.getWidth();
          for (String value : field.getAllowedValues()) {
            columns.add(new Column(value, width, Align.RIGHT));
            valueColumnIndex.put(value, columns.size() - 1);
          }
          if (field.getAllowNull()) {
            columns.add(new Column("(None)", width, Align.RIGHT));
            valueColumnIndex.put("(None)", columns.size() - 1);
          }

        } else if (field.getAllowedValuesQuery() != null) {
          log.debug("Horizontal field has fixed values query: " + hgName);
          width = field.getWidth();
          List<String> values = DataBaseUtil.queryToArrayList1(field.getAllowedValuesQuery());
          if (values.size() > 50) {
            // todo: return error or throw exception: should not
            // allow more than N
            // values in the horizontal group, to conserve memory.
          }
          for (String value : values) {
            String displayValue = formatValue(value, params.getSumGroupHoriz(), true);
            columns.add(new Column(displayValue, width, Align.RIGHT));
            valueColumnIndex.put(displayValue, columns.size() - 1);
          }
          if (field.getAllowNull()) {
            columns.add(new Column("(None)", width, Align.RIGHT));
            valueColumnIndex.put("(None)", columns.size() - 1);
          }
        } else {
          throw new IllegalArgumentException(
              "Horizontal group does not have fixed number of values: " + field);
        }

        // add a column for the total
        columns.add(new Column("Total", width, Align.RIGHT));
      }
    } else {
      // need one column for the single data
      if (params.getDisplayFields().size() > 1) {
        // this kind of column cannot have a name
        columns.add(new Column("Value", dataWidth, Align.RIGHT));
      } else {
        String fname = params.getDisplayFields().get(0);
        if (fname.equals("_count")) {
          columns.add(new Column("Count", dataWidth, Align.RIGHT));
        } else {
          columns.add(new Column(desc.getField(fname)));
        }
      }
    }
  }

  /**
   * Write sum report.
   *
   * @throws Exception the exception
   */
  public void writeSumReport() throws Exception {
    // start the document etc.
    getSumColumnDetails();
    initialize();

    con = DataBaseUtil.getConnection(600, true);
    rs = getSumResultSet(con, desc, params);

    // add the title
    String fromDate = null;
    String toDate = null;
    if (params.getSelectedDateField() != null && !params.getSelectedDateField().equals("")) {
      fromDate = DateUtil.formatDate(params.getFromDate());
      toDate = DateUtil.formatDate(params.getToDate());
    }

    if (params.getHospNameAndAddrsHeader() != null
        && !params.getHospNameAndAddrsHeader().equals("")) {
      writeTitle(params.getPrintTitle(), params.getHospNameAndAddrsHeader(), fromDate, toDate,
          params.getFilterDescription().equals("") ? null
              : "Filtered on: " + params.getFilterDescription());
    } else {
      writeTitle(params.getPrintTitle(), fromDate, toDate,
          params.getFilterDescription().equals("") ? null
              : "Filtered on: " + params.getFilterDescription());
    }

    // create the main table for adding all rows
    writeMainHeader(columns, true);

    // get the rows, and write every row.
    ResultSetDynaClass rsdc = new ResultSetDynaClass(rs, false);
    Iterator rows = rsdc.iterator();

    String vg = params.getSumGroupVert();
    String vgSub = params.getSumGroupVertSub();
    String hg = params.getSumGroupHoriz();

    int numData = params.getDisplayFields().size();
    String[] sumFieldNames = new String[numData];
    String[] sumFieldDisplayNames = new String[numData];
    String[] sumFieldAggFunctions = new String[numData];
    String[] sumFieldAggWeights = new String[numData];

    for (int d = 0; d < numData; d++) {
      sumFieldNames[d] = params.getDisplayFields().get(d);
      sumFieldDisplayNames[d] = desc.getFieldDisplayName(sumFieldNames[d]);
      if (!sumFieldNames[d].equals("_count")) {
        StdReportDesc.Field field = desc.getField(sumFieldNames[d]);
        sumFieldAggFunctions[d] = field.getAggFunction();
        sumFieldAggWeights[d] = field.getAggWeight();
      } else {
        sumFieldAggFunctions[d] = "sum";
      }
    }

    if ((hg != null) && !hg.equals("_data")) { // F*F, hg is a field (F-F,
      // FDF and FFF)

      // all groups are fields: every db row is only one cell: collect
      // till we have all
      // columns and data fields and then send it out. Need to watch out
      // for missing columns.
      boolean isVertSubField = (vgSub != null) && !vgSub.equals("_data");
      boolean isVertSubData = (vgSub != null) && vgSub.equals("_data");
      int firstFieldColumn = (vgSub == null) ? 1 : 2;
      int rowTotalColumn = columns.size() - 1;

      String[][] valueList = new String[numData][columns.size()];
      Object[] rowTotals = new Object[numData];
      Object[] subTotals = new Object[columns.size()]; // first 1 or 2
      // are unused
      Object[][] grandTotals = new Object[numData][columns.size()]; // first
      // 1 or
      // 2
      // are
      // unused

      // initialize
      for (int d = 0; d < numData; d++) {
        for (int i = firstFieldColumn; i < columns.size(); i++) {
          if (params.isNullZero()) {
            valueList[d][i] = "0.00";
          } else {
            valueList[d][i] = "";
          }
        }
      }

      if (isVertSubData) {
        for (int d = 0; d < numData; d++) {
          valueList[d][1] = desc.getFieldDisplayName(params.getDisplayFields().get(d));
        }
      }

      String vgValue = null;
      String vgValuePrvs = null;
      String vgSubValue = null;
      String vgSubValuePrvs = null;
      boolean firstRow = true;

      // iterate over each db row
      while (rows.hasNext()) {
        DynaBean bean = (DynaBean) rows.next();

        vgValue = formatValue(bean.get(vg), vg, true);
        if (vgValuePrvs == null) {
          vgValuePrvs = vgValue;
          valueList[0][0] = vgValue;
          for (int d = 1; d < numData; d++) {
            if (params.isSkipRepeatedValues()) {
              valueList[d][0] = "";
            } else {
              valueList[d][0] = vgValue;
            }
          }
        }
        if (isVertSubField) {
          vgSubValue = formatValue(bean.get(vgSub), vgSub, true);
          if (vgSubValuePrvs == null) {
            vgSubValuePrvs = vgSubValue;
            valueList[0][1] = vgSubValue;
          }
        }

        // if the vgValue or vgSubValue differ, then the previous row is
        // complete
        if (!vgValue.equals(vgValuePrvs)
            || (isVertSubField && !vgSubValuePrvs.equals(vgSubValue))) {
          log.debug("Writing out row for: " + vgValue + ", " + vgSubValue);
          for (int d = 0; d < numData; d++) {
            valueList[d][rowTotalColumn] = formatAgg(sumFieldAggFunctions[d], rowTotals[d],
                sumFieldNames[d]);
            writeSumRow(Arrays.asList(valueList[d]), !valueList[d][0].isEmpty(), false, true);
          }

          if (isVertSubField && !vgValue.equals(vgValuePrvs)) {
            if (params.isSkipRepeatedValues()) {
              valueList[0][0] = "";
            } else {
              valueList[0][0] = vgValuePrvs;
            }
            valueList[0][1] = "Total";
            Object rowTotal = null;
            String func = sumFieldAggFunctions[0];
            for (int col = 2; col < columns.size(); col++) {
              valueList[0][col] = formatAgg(func, subTotals[col], sumFieldNames[0]);
              if (subTotals[col] != null) {
                rowTotal = aggAccumulate(func, rowTotal, subTotals[col]);
              }
              subTotals[col] = null; // reset for the next
              // iteration
            }
            valueList[0][rowTotalColumn] = formatAgg(func, rowTotal, sumFieldNames[0]);
            writeSumRow(Arrays.asList(valueList[0]), false, true, false, true);
          }

          // initialize the next row: set vgValue and vgSubValue,
          // clear data fields
          valueList[0][0] = vgValue;
          if (isVertSubField) {
            if (vgValue.equals(vgValuePrvs) && params.isSkipRepeatedValues()) {
              valueList[0][0] = "";
            } else {
              valueList[0][0] = vgValue;
            }
            valueList[0][1] = vgSubValue; // vg sub value is
            // always written
          } else {
            valueList[0][0] = vgValue; // only for the first data
            // row
            if (!params.isSkipRepeatedValues()) {
              for (int d = 0; d < numData; d++) {
                valueList[d][0] = vgValue;
              }
            }
          }
          for (int d = 0; d < numData; d++) {
            rowTotals[d] = null;
            for (int i = firstFieldColumn; i < columns.size(); i++) {
              if (params.isNullZero()) {
                valueList[d][i] = "0.00";
              } else {
                valueList[d][i] = "";
              }
            }
          }
          vgValuePrvs = vgValue;
          vgSubValuePrvs = vgSubValue;
          firstRow = false;
        }

        /*
         * Normal processing: save the value in the valueList, save totals
         */
        /* Bug 40265 */
        String fieldValue = formatValue(bean.get(hg), hg, true);
        log.debug("Field value is: " + fieldValue + ", valueColumnIndex is: " + valueColumnIndex);
        Integer col = valueColumnIndex.get(fieldValue);
        if (col == null) {
          String msg = "Horizontal column value not found for " + hg + ": " + fieldValue;
          throw new IllegalArgumentException(msg);
        }

        for (int d = 0; d < numData; d++) {
          String dataName = params.getDisplayFields().get(d);
          Object data = bean.get(dataName);
          String dataValue = formatValue(data, sumFieldNames[d]);
          log.debug("Adding: [" + vgValue + ", " + vgSubValue + "] " + dataName + " at " + col
              + ": " + dataValue);
          valueList[d][col] = dataValue;

          // add to rowTotals in the last column and also to the grand
          // totals
          String func = sumFieldAggFunctions[d];
          Object weight = func.equals("wavg") ? bean.get(sumFieldAggWeights[d]) : null;
          rowTotals[d] = aggAccumulate(func, rowTotals[d], data, weight);
          grandTotals[d][col] = aggAccumulate(func, grandTotals[d][col], data, weight);
          if (isVertSubField) {
            subTotals[col] = aggAccumulate(func, subTotals[col], data, weight);
          }
          // todo: we should collect vgSub grand totals as well. But
          // we don't know the
          // number of vgSub values, and if the user chooses something
          // that can be
          // large in number, we will end up consuming a lot of
          // memory. Maybe we
          // can collect up to a limit, and if it goes beyond that, we
          // can print
          // error message in the report itself.
        }
      }

      // write out last table row after all db rows are done.
      for (int d = 0; d < numData; d++) {
        valueList[d][rowTotalColumn] = formatAgg(sumFieldAggFunctions[d], rowTotals[d],
            sumFieldNames[d]);
        writeSumRow(Arrays.asList(valueList[d]),
            !(valueList[d][0] == null || valueList[d][0].isEmpty()),
            !isVertSubField && (d == numData - 1), true);
      }

      if (isVertSubField) {
        // write out the last sub-total row.
        if (params.isSkipRepeatedValues()) {
          valueList[0][0] = "";
        } else {
          valueList[0][0] = vgValue;
        }
        valueList[0][1] = "Total";
        Object rowTotal = null;
        String func = sumFieldAggFunctions[0];
        for (int col = 2; col < columns.size(); col++) {
          valueList[0][col] = formatAgg(func, subTotals[col], sumFieldNames[0]);
          if (subTotals[col] != null) {
            rowTotal = aggAccumulate(func, rowTotal, subTotals[col]);
          }
          subTotals[col] = null; // reset for the next iteration
        }
        valueList[0][rowTotalColumn] = formatAgg(func, rowTotal, sumFieldNames[0]);
        writeSumRow(Arrays.asList(valueList[0]), false, true, false, true);

      } else {
        // write out the grand total row(s)
        valueList[0][0] = "Total";
        for (int d = 0; d < numData; d++) {
          if (!params.isSkipRepeatedValues()) {
            valueList[d][0] = "Total";
          }
          rowTotals[d] = null;
          for (int i = firstFieldColumn; i < columns.size(); i++) {
            valueList[d][i] = "";
          }
        }
        for (int d = 0; d < numData; d++) {
          String func = sumFieldAggFunctions[d];
          for (int i = firstFieldColumn; i < columns.size() - 1; i++) {
            valueList[d][i] = formatAgg(func, grandTotals[d][i], sumFieldNames[d]);
            if (grandTotals[d][i] != null) {
              rowTotals[d] = aggAccumulate(func, rowTotals[d], grandTotals[d][i]);
            }
          }
          valueList[d][rowTotalColumn] = formatAgg(func, rowTotals[d], sumFieldNames[d]);
          writeSumRow(Arrays.asList(valueList[d]), (d == 0), (d == numData - 1), false, true);
        }
      }

    } else if ((vgSub != null) && vgSub.equals("_data")) { // FD_ (vgSub is
      // data)
      List<String> valueList = new ArrayList<String>();
      Object[] totals = new Object[numData];
      while (rows.hasNext()) {
        DynaBean bean = (DynaBean) rows.next();
        for (int d = 0; d < numData; d++) {
          Object data = bean.get(sumFieldNames[d]);
          if (d == 0) {
            valueList.add(formatValue(bean.get(vg), vg, true));
          } else {
            valueList.add("");
          }
          valueList.add(sumFieldDisplayNames[d]);
          valueList.add(formatValue(data, sumFieldNames[d]));
          writeSumRow(valueList, (d == 0), false);
          valueList.clear();

          String func = sumFieldAggFunctions[d];
          Object weight = func.equals("wavg") ? bean.get(sumFieldAggWeights[d]) : null;
          totals[d] = aggAccumulate(func, totals[d], data, weight);
        }
      }

      // write totals
      for (int d = 0; d < numData; d++) {
        valueList.clear();
        valueList.add((d == 0) ? "Total" : "");
        valueList.add(sumFieldDisplayNames[d]);
        valueList.add(formatAgg(sumFieldAggFunctions[d], totals[d], sumFieldNames[d]));
        writeSumRow(valueList, (d == 0), (d == numData - 1), false, true);
      }

    } else { // FFD, FF-, F-D, F-- (all the others, D/- is horizontal)

      String vgValuePrvs = null;
      List<String> valueList = new ArrayList<String>();
      Object[] totals = new Object[numData];
      Object[] grandTotals = new Object[numData];

      while (rows.hasNext()) {
        DynaBean bean = (DynaBean) rows.next();

        String vgValue = formatValue(bean.get(vg), vg, true);
        if (vgSub != null) { // FF* (second V exists), we can group
          // V1
          if ((vgValuePrvs == null) || !vgValuePrvs.equals(vgValue)) {
            // write out sub totals and then start the new row group
            if (vgValuePrvs != null) {
              valueList.add("");
              valueList.add("Total");
              for (int d = 0; d < numData; d++) {
                valueList.add(formatAgg(sumFieldAggFunctions[d], totals[d], sumFieldNames[d]));
                totals[d] = null;
              }
              writeSumRow(valueList, false, false, true, true);
              valueList.clear();
            }
            valueList.add(vgValue);
          } else {
            valueList.add("");
          }
          vgValuePrvs = vgValue;

          valueList.add(formatValue(bean.get(vgSub), vgSub, true));

        } else {
          valueList.add(vgValue);
        }

        // we get a complete table row in every db row
        for (int d = 0; d < numData; d++) {
          Object data = bean.get(sumFieldNames[d]);
          valueList.add(formatValue(data, sumFieldNames[d]));
          if (data != null) {
            String func = sumFieldAggFunctions[d];
            Object weight = func.equals("wavg") ? bean.get(sumFieldAggWeights[d]) : null;
            totals[d] = aggAccumulate(func, totals[d], data, weight);
            grandTotals[d] = aggAccumulate(func, grandTotals[d], data, weight);
          }
        }

        writeSumRow(valueList, !valueList.get(0).equals(""), false);
        valueList.clear();
      }

      if (vgSub == null) {
        // F-- and F-D: write grand totals
        valueList.add("Total");
        for (int d = 0; d < numData; d++) {
          valueList.add(formatAgg(sumFieldAggFunctions[d], totals[d], sumFieldNames[d]));
        }
        writeSumRow(valueList, true, true, true, true);

      } else {
        // FF- and FFD: last sub-total is yet to be written out: write
        // it now
        valueList.clear();
        valueList.add("");
        valueList.add("Total");
        for (int d = 0; d < numData; d++) {
          valueList.add(formatAgg(sumFieldAggFunctions[d], totals[d], sumFieldNames[d]));
        }
        writeSumRow(valueList, false, true, true, true);

        // write out the grand totals.
        valueList.clear();
        valueList.add("");
        valueList.add("Grand Total");
        for (int d = 0; d < numData; d++) {
          valueList.add(formatAgg(sumFieldAggFunctions[d], grandTotals[d], sumFieldNames[d]));
        }
        writeSumRow(valueList, true, true, true, true);
      }
    }

    cleanup();
  }

  /**
   * Initialize.
   *
   * @throws Exception the exception
   */
  protected abstract void initialize() throws Exception;

  /**
   * Cleanup.
   *
   * @throws Exception the exception
   */
  protected abstract void cleanup() throws Exception;

  /**
   * Write title.
   *
   * @param title      the title
   * @param fdate      the fdate
   * @param tdate      the tdate
   * @param filterDesc the filter desc
   * @throws Exception the exception
   */
  protected abstract void writeTitle(String title, String fdate, String tdate, String filterDesc)
      throws Exception;

  /**
   * Write title.
   *
   * @param title        the title
   * @param hspNmAndAddr the hsp nm and addr
   * @param fdate        the fdate
   * @param tdate        the tdate
   * @param filterDesc   the filter desc
   * @throws Exception the exception
   */
  protected abstract void writeTitle(String title, String hspNmAndAddr, String fdate, String tdate,
      String filterDesc) throws Exception;

  /**
   * Write main header.
   *
   * @param columns   the columns
   * @param isSummary the is summary
   * @throws Exception the exception
   */
  protected abstract void writeMainHeader(List<Column> columns, boolean isSummary) throws Exception;

  /**
   * Write main header.
   *
   * @param columns the columns
   * @throws Exception the exception
   */
  protected void writeMainHeader(List<Column> columns) throws Exception {
    writeMainHeader(columns, false);
  }

  /**
   * Write group header.
   *
   * @param numbering   the numbering
   * @param headerNames the header names
   * @param headerVal   the header val
   * @param level       the level
   * @throws Exception the exception
   */
  protected abstract void writeGroupHeader(String numbering, List<String> headerNames,
      List<String> headerVal, int level) throws Exception;

  /**
   * Write list row.
   *
   * @param values the values
   * @throws Exception the exception
   */
  protected abstract void writeListRow(List<String> values) throws Exception;

  /**
   * Write list group totals.
   *
   * @param title        the title
   * @param titleColSpan the title col span
   * @param valueList    the value list
   * @throws Exception the exception
   */
  protected abstract void writeListGroupTotals(String title, int titleColSpan,
      List<String> valueList) throws Exception;

  /**
   * Write sum row.
   *
   * @param values         the values
   * @param isGroupChange  the is group change
   * @param isLastRow      the is last row
   * @param isLastColTotal the is last col total
   * @param isRowTotal     the is row total
   * @throws Exception the exception
   */
  protected abstract void writeSumRow(List<String> values, boolean isGroupChange, boolean isLastRow,
      boolean isLastColTotal, boolean isRowTotal) throws Exception;

  /**
   * Write sum row.
   *
   * @param values         the values
   * @param isNewGroup     the is new group
   * @param isLastRow      the is last row
   * @param isLastColTotal the is last col total
   * @throws Exception the exception
   */
  protected void writeSumRow(List<String> values, boolean isNewGroup, boolean isLastRow,
      boolean isLastColTotal) throws Exception {
    writeSumRow(values, isNewGroup, isLastRow, isLastColTotal, false);
  }

  /**
   * Write sum row.
   *
   * @param values     the values
   * @param isNewGroup the is new group
   * @param isLastRow  the is last row
   * @throws Exception the exception
   */
  protected void writeSumRow(List<String> values, boolean isNewGroup, boolean isLastRow)
      throws Exception {
    writeSumRow(values, isNewGroup, isLastRow, false, false);
  }

}
