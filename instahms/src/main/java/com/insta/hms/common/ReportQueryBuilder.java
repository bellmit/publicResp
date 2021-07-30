package com.insta.hms.common;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;

/**
 * The Class ReportQueryBuilder.
 */
public class ReportQueryBuilder extends QueryBuilder {

  /**
   * Instantiates a new report query builder.
   *
   * @param initWhere the init where
   */
  public ReportQueryBuilder(String initWhere) {
    super(initWhere);
  }

  /**
   * Instantiates a new report query builder.
   */
  public ReportQueryBuilder() {
    this(null);
  }

  /**
   * Adds the filter from param map. Create the WHERE clause from the parameter map. The map has
   * some information about the filter in the following format: filter.0=open_date filterOp.0=le
   * filterVal.0=19-02-2010 ... The type of each field can be found from the supplied StdReportDesc.
   * Multiple values for "in" etc can come as comma separated, or as an array. Multiple values for
   * field, op, type are not expected.
   *
   * @param map    the map
   * @param fields the fields
   * @throws ParseException the parse exception
   * @throws SQLException   the SQL exception
   */
  public void addFilterFromParamMap(Map<String, String[]> map,
      Map<String, StdReportDesc.Field> fields) throws ParseException, SQLException {

    if (map == null || map.isEmpty() || map.entrySet() == null || map.keySet() == null) {
      return;
    }

    Iterator it = map.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, String[]> pairs = (Map.Entry) it.next();
      String key = pairs.getKey().toString();

      if (!key.contains("filter.")) { // look only for "filter." fields
        continue;
      }

      String fieldName = pairs.getValue()[0];
      if (fieldName == null || fieldName.equals("")) {
        continue;
      }

      String typeStr = null;
      StdReportDesc.Field fieldDesc = null;
      fieldDesc = fields.get(fieldName);
      if (fieldDesc == null) {
        continue;
      }
      typeStr = fieldDesc.getDataType();

      String indexStr = key.substring(key.lastIndexOf(".") + 1);
      int index = Integer.parseInt(indexStr);
      String op = getParamValue(map, "filterOp." + index, "eq");
      String castStr = getParamValue(map, "filterCast." + index, "eq");

      boolean cast = (castStr != null && castStr.equalsIgnoreCase("y"));

      String[] values = map.get("filterVal." + index);
      if (values == null) {
        continue;
      }

      if (op.equals("in") || op.equals("nin") || op.equals("betw") || op.equals("between")) {
        // expect an array
        if (values.length == 0) {
          continue;
        } else if (values.length > 1) {
          // we were given multiple param values
          addFilterFromString(typeStr, fieldName, op, values, cast);
        } else { // == 1
          // we were given comma separated values: split it up
          addFilterFromString(typeStr, fieldName, op, values[0].split(","), cast);
        }
        appendDescription(fieldDesc.getDisplayName(), op, values);

      } else {
        // expect a single value: so take the first
        String value = values[0];
        if ((value == null) || value.isEmpty()) {
          continue;
        }
        addFilterFromString(typeStr, fieldName, op, value, cast);
        appendDescription(fieldDesc.getDisplayName(), op, value);
      }
    }
  }

  /**
   * Gets the param value. Gets the first value of a request parameter map.
   *
   * @param map        the map
   * @param key        the key
   * @param defaultVal the default val
   * @return the param value
   */
  public static String getParamValue(Map<String, String[]> map, String key, String defaultVal) {
    String[] values = map.get(key);
    if (values == null) {
      return defaultVal;
    }
    if (values[0] == null) {
      return defaultVal;
    }
    if (values[0].equals("")) {
      return defaultVal;
    }
    return values[0];
  }

}
