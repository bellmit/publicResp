package com.insta.hms.common;

import com.bob.hms.common.DateUtil;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * The Class QueryAssembler.
 */
public class QueryAssembler {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(QueryBuilder.class);

  /** The where clause. */
  protected StringBuilder whereClause;

  /** The init where. */
  protected String initWhere;

  /** The field types. */
  protected ArrayList fieldTypes;

  /** The field values. */
  protected ArrayList fieldValues;

  /** The description. */
  protected StringBuilder description = new StringBuilder();

  /** The append. */
  // transient variables for temp storage
  protected boolean append;

  /** The Constant INTEGER. */
  public static final int INTEGER = 1;

  /** The Constant STRING. */
  public static final int STRING = 2;

  /** The Constant TEXT. */
  public static final int TEXT = 2; // alias for string

  /** The Constant NUMERIC. */
  public static final int NUMERIC = 3;

  /** The Constant DATE. */
  public static final int DATE = 4;

  /** The Constant TIMESTAMP. */
  public static final int TIMESTAMP = 5;

  /** The Constant TIME. */
  public static final int TIME = 6;

  /** The Constant BOOLEAN. */
  public static final int BOOLEAN = 7;

  /**
   * Instantiates a new query assembler.
   *
   * @param initWhere the init where
   */
  public QueryAssembler(String initWhere) {

    this.initWhere = initWhere;

    if (null == initWhere) {
      this.append = false;
    } else {
      this.append = true;
    }

    whereClause = new StringBuilder();
    fieldTypes = new ArrayList();
    fieldValues = new ArrayList();
  }

  /**
   * Instantiates a new query assembler.
   */
  public QueryAssembler() {
    this(null);
  }

  /**
   * Gets the field types.
   *
   * @return the field types
   */
  public ArrayList getfieldTypes() {
    return fieldTypes;
  }

  /**
   * Gets the field values.
   *
   * @return the field values
   */
  public ArrayList getfieldValues() {
    return fieldValues;
  }
  
  /** 
   * Sets the field values.
   */
  public void setfieldValues(ArrayList values) {
    fieldValues.addAll(values);
  }
 
  /**
   * Gets the where clause.
   *
   * @return the where clause
   */
  public String getWhereClause() {
    return whereClause.toString();
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description.toString();
  }

  /**
   * Gets the quoted where clause. Replaces all ?s with the actual values quoted, so that the WHERE
   * clause can be appended in toto to a query without having to prepare the statement and doing
   * setObject() etc. To be used only for supplying the params to Jasper reports (because we can't
   * supply arbitrary number of parameters to jasper). Otherwise, always prefer the
   * PreparedStatement version.
   * 
   * @return the quoted where clause
   */
  public String getQuotedWhereClause() {
    StringBuilder whereCopy = new StringBuilder(whereClause);
    int index = whereCopy.indexOf("?");
    int paramIndex = 0;
    DateUtil dateUtil = new DateUtil();

    while (index != -1) {
      int type = (Integer) fieldTypes.get(paramIndex);
      Object value = fieldValues.get(paramIndex);
      String valueStr = null;

      switch (type) {
        case STRING:
          valueStr = quoteValue(value.toString());
          break;
        case INTEGER:
          valueStr = ((Integer) value).toString();
          break;
        case NUMERIC:
          valueStr = ((BigDecimal) value).toString();
          break;
        case DATE:
          valueStr = quoteValue(dateUtil.getSqlDateFormatter().format((java.sql.Date) value));
          break;
        case TIME:
          valueStr = quoteValue(dateUtil.getSqlTimeFormatter().format((java.sql.Time) value));
          break;
        case TIMESTAMP:
          valueStr = quoteValue(dateUtil.getSqlTimeStampFormatter().format((java.sql.Time) value));
          break;
        case BOOLEAN:
          valueStr = ((Boolean) value) ? "true" : "false";
          break;
        default:
          valueStr = null;
      }

      whereCopy.replace(index, index + 1, valueStr);
      paramIndex++;
    }
    return whereCopy.toString();
  }

  /**
   * Quote value.
   *
   * @param value the value
   * @return the string
   */
  public static String quoteValue(String value) {
    value = value.replaceAll("'", "''");
    return "'" + value + "'";
  }

  /**
   * Append expression. appends the expression query to where clause , adds list of values to
   * fieldValues and adds list of types to fieldTypes .
   *
   * @param expr   the expr
   * @param types  the types
   * @param values the values
   */
  public void appendExpression(String expr, ArrayList types, ArrayList values) {
    if (!append) {
      if (whereClause == null) {
        whereClause = new StringBuilder();
      }
      whereClause.append(" WHERE " + expr);
      append = true;
    } else {
      whereClause.append(" AND " + expr);
    }
    fieldValues.addAll(values);
    fieldTypes.addAll(types);
  }

  /**
   * Append to query. appends the expression query to where clause.
   * 
   * @param expr the expr
   */
  public void appendToQuery(String expr) {
    if (!append) {
      if (whereClause == null) {
        whereClause = new StringBuilder();
      }
      whereClause.append(" WHERE " + expr);
      append = true;
    } else {
      whereClause.append(" AND " + expr);
    }
  }

  /**
   * Adds the init value. Add initial parameter values that are not part of the filter specs.
   * 
   * @param type  the type
   * @param value the value
   */
  public void addInitValue(int type, Object value) {
    // ignore null values as well as empty string values
    if (value == null) {
      return;
    }
    if ((type == STRING) && value.equals("")) {
      return;
    }

    fieldTypes.add(type);
    fieldValues.add(value);
  }

  /**
   * Adds the where field op value. Method to construct a WHERE clause on the fly, depending on the
   * supplied field name, operator and value(s). Returns whether the next call should be an AND or,
   * start with WHERE, same meaning as what is passed in as append.
   * Takes in a single operand: suitable for =, !=, <, > etc.
   *
   * @param append the append
   * @param where  the where
   * @param field  the field
   * @param op     the op
   * @param value  the value
   * @return true, if successful
   */
  public static boolean addWhereFieldOpValue(boolean append, StringBuilder where, String field,
      String op, Object value) {

    if (value == null) {
      // since we are not appending anything, next call uses whatever
      // this call should have used.
      return append;
    }

    where.append(append ? " AND " : " WHERE ");

    where.append('(');
    where.append(field).append(" ").append(op).append(" ").append('?');
    where.append(')');

    // since we appended something, next call must use and.
    return true;
  }

  /**
   * Adds the where field op value. No operand: suitable for IS NULL and IS NOT NULL
   *
   * @param append the append
   * @param where  the where
   * @param field  the field
   * @param op     the op
   * @return true, if successful
   */
  public static boolean addWhereFieldOpValue(boolean append, StringBuilder where, String field,
      String op) {

    where.append(append ? " AND " : " WHERE ");

    where.append('(');
    where.append(field).append(" ").append(op).append(" ");
    where.append(')');

    return true;
  }

  /**
   * Adds the where field op value. Two operands: suitable for BETWEEN
   *
   * @param append the append
   * @param where  the where
   * @param field  the field
   * @param op     the op
   * @param value1 the value 1
   * @param value2 the value 2
   * @return true, if successful
   */
  public static boolean addWhereFieldOpValue(boolean append, StringBuilder where, String field,
      String op, Object value1, Object value2) {

    if ((value1 == null) || (value2 == null)) {
      return append;
    }

    where.append(append ? " AND " : " WHERE ");

    where.append('(');
    where.append(field).append(" " + op + " ").append('?').append(" ").append("AND").append(" ")
        .append('?').append(" ");
    where.append(')');

    return true;
  }

  /**
   * Adds the where field op value. Multiple (any number) operands: suitable for IN and NOT IN
   *
   * @param append the append
   * @param where  the where
   * @param field  the field
   * @param op     the op
   * @param values the values
   * @return true, if successful
   */
  public static boolean addWhereFieldOpValue(boolean append, StringBuilder where, String field,
      String op, List values) {

    if (values == null || values.size() < 1) {
      return append;
    }

    where.append(append ? " AND " : " WHERE ");

    where.append('(');

    where.append(field).append(" " + op + " ");
    where.append('(');
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) {
        where.append(',');
      }
      where.append('?');
    }
    where.append(')');
    where.append(')');

    return true;
  }

  /**
   * Adds the filter. Add a filter one by one to the where clause: generic single value of type
   * Object, which will be typecast to a list if multiple values are expected.
   *
   * @param type  the type
   * @param field the field
   * @param sqlOp the sql op
   * @param value the value
   */
  public void addFilter(int type, String field, String sqlOp, Object value) {
    // ignore null values as well as empty string values
    if (value == null) {
      return;
    }
    if ((type == STRING) && value.equals("")) {
      return;
    }

    if (sqlOp.equalsIgnoreCase("IN") || sqlOp.equalsIgnoreCase("NOT IN")) {
      // multiple operands
      List valueList = (List) value;
      append = addWhereFieldOpValue(append, whereClause, field, sqlOp, valueList);

      for (int i = 0; i < valueList.size(); i++) {
        fieldValues.add(valueList.get(i));
        fieldTypes.add(type);
      }

      logger.debug("after " + field + " " + sqlOp + " " + ": where: " + whereClause);

    } else if (sqlOp.equalsIgnoreCase("BETWEEN")) {
      // two operands
      append = addWhereFieldOpValue(append, whereClause, field, "BETWEEN", value);
      fieldTypes.add(type);
      fieldTypes.add(type);
      Object[] vals = (Object[]) value;
      fieldValues.add(vals[0]);
      fieldValues.add(vals[1]);

      logger.debug("after " + field + " " + sqlOp + " " + value + ": where: " + whereClause);

    } else if (sqlOp.equalsIgnoreCase("IS NULL") || sqlOp.equalsIgnoreCase("IS")) {
      // no operand
      append = addWhereFieldOpValue(append, whereClause, field, "IS NULL", value);

    } else if (sqlOp.equalsIgnoreCase("IS NOT NULL")) {
      // no operand
      append = addWhereFieldOpValue(append, whereClause, field, "IS NOT NULL", value);

    } else {
      // all other cases: single operand
      append = addWhereFieldOpValue(append, whereClause, field, sqlOp, value);
      logger.debug("after " + field + " " + sqlOp + " " + value + ": where: " + whereClause);
      fieldTypes.add(type);
      /*
       * The following is only for backward compatibility for direct calls to addFilter from the DAO
       * classes. Some DAO classes expect us to add the % around the value
       */
      if ((sqlOp.equalsIgnoreCase("like") || sqlOp.equalsIgnoreCase("ilike"))
          && (((String) value).indexOf('%') == -1)) {
        value = "%" + value + "%";
      }
      fieldValues.add(value);
    }
  }

  /**
   * Adds the filter. Add a filter for operator expecting no values. Currently only IS NULL and IS
   * NOT NULL is supported.
   *
   * @param field the field
   * @param sqlOp the sql op
   */
  public void addFilter(String field, String sqlOp) {

    if (!sqlOp.equalsIgnoreCase("IS NULL") && !sqlOp.equalsIgnoreCase("IS NOT NULL")) {
      logger.warn("Only IS NULL or IS NOT NULL operators supported here, "
          + "attempting anyway with the given op: " + sqlOp);
    }
    append = addWhereFieldOpValue(append, whereClause, field, sqlOp);
  }

  /**
   * Adds the filter. Add a filter for operator expecting two values. Currently only BETWEEN is
   * supported.
   *
   * @param type   the type
   * @param field  the field
   * @param sqlOp  the sql op
   * @param value1 the value 1
   * @param value2 the value 2
   */
  public void addFilter(int type, String field, String sqlOp, Object value1, Object value2) {
    // ignore null values as well as empty string values
    if ((value1 == null) || (value2 == null)) {
      return;
    }

    if (!sqlOp.equalsIgnoreCase("BETWEEN")) {
      logger.warn(
          "Only BETWEEN operator supported here, attempting anyway with the given op: " + sqlOp);
    }

    append = addWhereFieldOpValue(append, whereClause, field, sqlOp, value1, value2);
    fieldTypes.add(type);
    fieldTypes.add(type);
    fieldValues.add(value1);
    fieldValues.add(value2);

    logger.debug(
        "after " + field + " " + sqlOp + " " + value1 + " " + value2 + ": where: " + whereClause);
  }

  /**
   * Adds the filter. Add a filter with many values, passed in as a list.
   *
   * @param type      the type
   * @param field     the field
   * @param sqlOp     the sql op
   * @param valueList the value list
   */
  public void addFilter(int type, String field, String sqlOp, List valueList) {
    // ignore null values as well as empty string values
    if (valueList == null) {
      return;
    }

    if (!sqlOp.equalsIgnoreCase("IN") && !sqlOp.equalsIgnoreCase("NOT IN")) {
      logger.warn("Only IN or NOT IN operators supported here, "
          + "attempting anyway with the given op: " + sqlOp);
    }
    append = addWhereFieldOpValue(append, whereClause, field, sqlOp, valueList);

    for (int i = 0; i < valueList.size(); i++) {
      fieldValues.add(valueList.get(i));
      fieldTypes.add(type);
    }

    logger.debug("after " + field + " " + sqlOp + " " + ": where: " + whereClause);
  }
  
  private String escapeBackSlash(String value) {
    return value.contains("\\") ? value.replace("\\", "\\\\") : value;
  }

  /**
   * Adds the filter from string. Takes in a a value as string, the data type and operator, and adds
   * this as a filter after converting to the appropriate data type and/or massaging the value to
   * put % etc.
   *
   * @param typeStr the type str
   * @param field   the field
   * @param op      the op
   * @param value   the value
   * @param doCast  the do cast
   */
  public void addFilterFromString(String typeStr, String field, String op, String value,
      boolean doCast) {

    if (value == null) {
      return;
    }

    if (value.equals("")) { // todo: "" is a valid value?
      return;
    }

    if (op.equals("null")) {
      if (value.equalsIgnoreCase("y")) {
        addFilter(doCast ? field + "::text" : field, "IS NULL");
      } else {
        addFilter(doCast ? field + "::text" : field, "IS NOT NULL");
      }

    } else if (op.equals("period")) {
      DateTime[] dateRange = DateHelper.getDateRange(value);
      addFilter(DATE, doCast ? field + "::text" : field, "BETWEEN", dateRange[0], dateRange[1]);

    } else if (op.equals("like")) {
      value = escapeBackSlash(value);
      addFilter(STRING, doCast ? field + "::text" : field, "LIKE", value);
    } else if (op.equals("ilike")) {
      value = escapeBackSlash(value);
      addFilter(STRING, doCast ? field + "::text" : field, "ILIKE", value);
    } else if (op.equals("co")) {
      value = escapeBackSlash(value);
      addFilter(STRING, doCast ? field + "::text" : field, "LIKE", "%" + value + "%");
    } else if (op.equals("ico")) {
      value = escapeBackSlash(value);
      addFilter(STRING, doCast ? field + "::text" : field, "ILIKE", "%" + value + "%");
    } else if (op.equals("sw")) {
      value = escapeBackSlash(value);
      addFilter(STRING, doCast ? field + "::text" : field, "LIKE", value + "%");
    } else if (op.equals("isw")) {
      value = escapeBackSlash(value);
      addFilter(STRING, doCast ? field + "::text" : field, "ILIKE", value + "%");
    } else if (op.equals("ew")) {
      value = escapeBackSlash(value);
      addFilter(STRING, doCast ? field + "::text" : field, "LIKE", "%" + value);
    } else if (op.equals("iew")) {
      value = escapeBackSlash(value);
      addFilter(STRING, doCast ? field + "::text" : field, "ILIKE", "%" + value);

    } else {
      // single operand varying data types
      int type = typeFromTypeString(typeStr);
      Object valueObj = parseString(type, value);
      addFilter(type, doCast ? field + getTypeCast(type) : field, getSqlOp(op), valueObj);
    }
  }

  /**
   * Adds the filter from string. Same as previous, but the value is a list (suitable for in, nin
   * and between)
   *
   * @param typeStr the type str
   * @param field   the field
   * @param op      the op
   * @param values  the values
   * @param doCast  the do cast
   */
  public void addFilterFromString(String typeStr, String field, String op, String[] values,
      boolean doCast) {

    if (values == null) {
      return;
    }

    int type = typeFromTypeString(typeStr);
    if (op.equals("betw") || op.equals("between")) {
      if (values.length != 2) {
        logger.error("Error adding filter for " + field
            + ". Operator betw requires 2 operands but found " + values.length);
        return;
      }
      boolean isValue1Empty = (values[0] == null || values[0].equals(""));
      boolean isValue2Empty = (values[1] == null || values[1].equals(""));
      if (isValue1Empty && isValue2Empty) {
        logger.error("Error adding filter for " + field + "No operands found");
        return;
      } else if (isValue1Empty && !isValue2Empty) {
        addFilter(type, doCast ? field + getTypeCast(type) : field, getSqlOp("le"),
            parseString(type, values[1]));
      } else if (!isValue1Empty && isValue2Empty) {
        addFilter(type, doCast ? field + getTypeCast(type) : field, getSqlOp("ge"),
            parseString(type, values[0]));
      } else {
        addFilter(type, doCast ? field + getTypeCast(type) : field, getSqlOp(op),
            parseString(type, values[0]), parseString(type, values[1]));
      }

    } else if (op.equals("in") || op.equals("nin")) {
      List valueObjs = new ArrayList();
      for (int i = 0; i < values.length; i++) {
        if (values[i] != null && !values[i].equals("")) {
          valueObjs.add(parseString(type, values[i]));
        }
      }
      addFilter(type, doCast ? field + getTypeCast(type) : field, op.equals("in") ? "IN" : "NOT IN",
          valueObjs);
    } else {
      logger.error("Expecting operators in, nin or betw for array values");
    }
    return;
  }

  /**
   * Append description.
   *
   * @param displayName the display name
   * @param op          the op
   * @param value       the value
   */
  public void appendDescription(String displayName, String op, String value) {
    if (description.length() > 0) {
      description.append(" AND ");
    }
    description.append(displayName).append(" ").append(getOpDisplayName(op)).append(" ")
        .append(value);
  }

  /**
   * Append description.
   *
   * @param displayName the display name
   * @param op          the op
   * @param values      the values
   */
  public void appendDescription(String displayName, String op, String[] values) {

    if (description.length() > 0) {
      description.append(" AND ");
    }

    description.append(displayName).append(" ").append(getOpDisplayName(op)).append("(");
    boolean first = true;
    for (String value : values) {
      if (!first) {
        description.append(", ");
      }
      description.append(value);
      first = false;
    }

    description.append(")");
  }

  /**
   * Parses the string.
   *
   * @param type  the type
   * @param value the value
   * @return the object
   */
  public static Object parseString(int type, String value) {
    try {
      switch (type) {
        case STRING:
          return value;
        case INTEGER:
          return (isInteger(value) ? Integer.parseInt(value) : 0);
        case NUMERIC:
          return new BigDecimal(value);
        case DATE:
          return DateUtil.parseDate(value);
        case TIMESTAMP:
          return DateUtil.parseTimestamp(value);
        case TIME:
          return DateUtil.parseTime(value);
        case BOOLEAN:
          return new Boolean(value);
        default:
          return null;
      }
    } catch (ParseException exception) {
      logger.error(exception.getMessage());
    }
    return null;
  }

  /**
   * Checks if is integer.
   *
   * @param str the str
   * @return true, if is integer
   */
  public static boolean isInteger(String str) {
    try {
      Integer.valueOf(str);
      return true;
    } catch (NumberFormatException exception) {
      return false;
    }
  }

  /**
   * Checks for word.
   *
   * @param str  the str
   * @param word the word
   * @return the boolean
   */
  public static Boolean hasWord(String str, String word) {
    if (str.equalsIgnoreCase(word)) {
      return true;
    }
    if (str.startsWith(word + "_")) {
      return true;
    }
    if (str.endsWith("_" + word)) {
      return true;
    }
    return false;
  }

  /**
   * Ends with word.
   *
   * @param string the string
   * @param word   the word
   * @return true, if successful
   */
  public static boolean endsWithWord(String string, String word) {
    return string.endsWith("_" + word);
  }

  /**
   * Starts with word.
   *
   * @param string the string
   * @param word   the word
   * @return true, if successful
   */
  public static boolean startsWithWord(String string, String word) {
    return string.startsWith(word + "_");
  }

  /**
   * Guess type.
   *
   * @param fieldName the field name
   * @return the string
   */
  public static String guessType(String fieldName) {
    if (hasWord(fieldName, "date")) {
      return "date";
    }
    if (hasWord(fieldName, "time")) {
      return "time";
    }
    if (fieldName.endsWith("_at")) {
      return "timestamp";
    }
    if (hasWord(fieldName, "qty")) {
      return "numeric";
    }
    if (hasWord(fieldName, "quantity")) {
      return "numeric";
    }
    if (hasWord(fieldName, "rate")) {
      return "numeric";
    }
    if (fieldName.endsWith("_num")) {
      return "integer";
    }
    return "string";
  }

  /**
   * Type from type string.
   *
   * @param typeString the type string
   * @return the int
   */
  public static int typeFromTypeString(String typeString) {
    if (typeString.equals("string")) {
      return STRING;
    }
    if (typeString.equals("text")) {
      return STRING;
    }
    if (typeString.equals("numeric")) {
      return NUMERIC;
    }
    if (typeString.equals("integer")) {
      return INTEGER;
    }
    if (typeString.equals("date")) {
      return DATE;
    }
    if (typeString.equals("time")) {
      return TIME;
    }
    if (typeString.equals("timestamp")) {
      return TIMESTAMP;
    }
    if (typeString.equals("boolean")) {
      return BOOLEAN;
    }
    return STRING;
  }

  /**
   * Type to type string.
   *
   * @param type the type
   * @return the string
   */
  public static String typeToTypeString(int type) {
    if (type == STRING) {
      return "string";
    }
    if (type == STRING) {
      return "text";
    }
    if (type == NUMERIC) {
      return "numeric";
    }
    if (type == INTEGER) {
      return "integer";
    }
    if (type == DATE) {
      return "date";
    }
    if (type == TIME) {
      return "time";
    }
    if (type == TIMESTAMP) {
      return "timestamp(0)";
    }
    if (type == BOOLEAN) {
      return "boolean";
    }
    return "string";
  }

  /**
   * Gets the type cast.
   *
   * @param type the type
   * @return the type cast
   */
  public static String getTypeCast(int type) {
    if (type == STRING) {
      return "::text";
    } else {
      return "::" + typeToTypeString(type);
    }
  }

  /**
   * Gets the sql op. Returns the sql operator based on the operator-token string passed; by default
   * returns "="
   *
   * @param opStr the op str
   * @return the sql op
   */
  public static String getSqlOp(String opStr) {
    String sqlOp;
    if (opStr.equals("")) {
      sqlOp = "=";
    } else if (opStr.equals("eq")) {
      sqlOp = "=";
    } else if (opStr.equals("ne")) {
      sqlOp = "!=";
    } else if (opStr.equals("lt")) {
      sqlOp = "<";
    } else if (opStr.equals("gt")) {
      sqlOp = ">";
    } else if (opStr.equals("le")) {
      sqlOp = "<=";
    } else if (opStr.equals("ge")) {
      sqlOp = ">=";
    } else if (opStr.equals("co")) {
      sqlOp = "LIKE";
    } else if (opStr.equals("ico")) {
      sqlOp = "ILIKE";
    } else if (opStr.equals("ew")) {
      sqlOp = "LIKE";
    } else if (opStr.equals("iew")) {
      sqlOp = "ILIKE";
    } else if (opStr.equals("sw")) {
      sqlOp = "LIKE";
    } else if (opStr.equals("isw")) {
      sqlOp = "ILIKE";
    } else if (opStr.equals("in")) {
      sqlOp = "IN";
    } else if (opStr.equals("nin")) {
      sqlOp = "NOT IN";
    } else if (opStr.equals("~")) {
      sqlOp = "~";
    } else if (opStr.equals("between") || opStr.equals("betw")) {
      sqlOp = "BETWEEN";
    } else {
      sqlOp = "=";
    }
    return sqlOp;
  }

  /**
   * Gets the op display name. Returns the operator display name based on the operator-token string
   * passed Suitable for displaying a description of the filter operator.
   *
   * @param opStr the op str
   * @return the op display name
   */
  public static String getOpDisplayName(String opStr) {
    String displayOp;
    if (opStr.equals("")) {
      displayOp = "equals";
    } else if (opStr.equals("eq")) {
      displayOp = "equals";
    } else if (opStr.equals("ne")) {
      displayOp = "not equals";
    } else if (opStr.equals("lt")) {
      displayOp = "less than";
    } else if (opStr.equals("gt")) {
      displayOp = "greater than";
    } else if (opStr.equals("le")) {
      displayOp = "less than or equals";
    } else if (opStr.equals("ge")) {
      displayOp = "greater than or equals";
    } else if (opStr.equals("co")) {
      displayOp = "contains (exact case)";
    } else if (opStr.equals("ico")) {
      displayOp = "contains";
    } else if (opStr.equals("ew")) {
      displayOp = "ends with (exact case)";
    } else if (opStr.equals("iew")) {
      displayOp = "ends with";
    } else if (opStr.equals("sw")) {
      displayOp = "starts with (exact case)";
    } else if (opStr.equals("isw")) {
      displayOp = "starts with";
    } else if (opStr.equals("in")) {
      displayOp = "one of";
    } else if (opStr.equals("nin")) {
      displayOp = "not one of";
    } else if (opStr.equals("between") || opStr.equals("betw")) {
      displayOp = "between";
    } else {
      displayOp = "equals";
    }
    return displayOp;
  }

  /**
   * Gets the operator tokens. Converts a comma-separated, operator-string into an Array of
   * operators; if the operator-string is null, then by default "eq" (viz. "=") is returned. Note
   * that the list of operators can itself be an array, each value being a comma separated list.
   *
   * @param opValues    the op values
   * @param valueLength the value length
   * @return the operator tokens
   */
  public String[] getOperatorTokens(String[] opValues, int valueLength) {
    if (opValues == null) {
      if (valueLength > 1) {
        return new String[] { "in" };
      } else {
        return new String[] { "eq" };
      }
    }

    List<String> operators = new ArrayList<String>();

    for (int i = 0; i < opValues.length; i++) {
      StringTokenizer st = new StringTokenizer(opValues[i], ",");
      String chkString;
      while (st.hasMoreTokens()) {
        chkString = st.nextToken();
        if (!(chkString.equals("") && chkString == null)) {
          operators.add(chkString);
        }
      }
    }
    return operators.toArray(new String[0]);
  }
}
