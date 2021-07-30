package com.insta.hms.common;

import com.bob.hms.common.AutoIncrementId;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

// TODO: Auto-generated Javadoc
/**
 * The Class AutoIdGenerator for generating incremental IDs. Port of {@link AutoIncrementId}.
 * 
 * @author tanmay.k
 */
public class AutoIdGenerator {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(AutoIdGenerator.class);

  /** The Constant GET_ID_DETAILS_QUERY. */
  private static final String GET_ID_DETAILS_QUERY = "select start_number, prefix, pattern from "
      + " unique_number where type_number= ? ";

  /** The get last id query. */
  private static String GET_LAST_ID_QUERY = "select %s from %s where %s SIMILAR TO ? ORDER BY "
      + " TO_NUMBER(SUBSTRING(%s FROM %d FOR 10), '999999') DESC LIMIT 1";

  /** The get sequence id query. */
  private static String GET_SEQUENCE_ID_QUERY = "SELECT nextval(?), prefix, pattern FROM "
      + " unique_number WHERE type_number = ?";

  /** The Constant PREFIX_FIELD. */
  private static final String PREFIX_FIELD = "prefix";

  /** The Constant PATTERN_FIELD. */
  private static final String PATTERN_FIELD = "pattern";

  /** The Constant NEXTVAL_FIELD. */
  private static final String NEXTVAL_FIELD = "nextval";

  /**
   * Gets the new id.
   *
   * @param id    the id
   * @param table the table
   * @param type  the type
   * @return the new id TODO - Check usage of Insesitive Scroll and Concur read only
   */
  public static String getNewId(String id, String table, String type) {
    BasicDynaBean details = DatabaseHelper.queryToDynaBean(GET_ID_DETAILS_QUERY, type);
    Integer startNumber = ((BigDecimal) details.get("start_number")).intValueExact();
    String prefix = (String) details.get(PREFIX_FIELD);
    String pattern = (String) details.get(PATTERN_FIELD);

    Integer prefixLength = prefix.length();
    Integer idNumericPortion;
    logger.debug("startNo=" + startNumber + " prefix=" + prefix + " Pattern=" + pattern);

    GET_LAST_ID_QUERY = String.format(GET_LAST_ID_QUERY, id, table, id, id, prefixLength + 1);
    String lastId = DatabaseHelper.getString(GET_LAST_ID_QUERY, prefix + "[0-9]%");

    if (lastId.equals("")) {
      idNumericPortion = startNumber;
    } else {
      idNumericPortion = Integer.parseInt(lastId.substring(prefixLength)) + 1;
    }

    return prependPrefix(prefix, pattern, idNumericPortion);
  }

  /**
   * Gets the sequence id.
   *
   * @param sequenceName the sequence name
   * @param typeNumber   the type number
   * @return the sequence id
   */
  public static String getSequenceId(String sequenceName, String typeNumber) {
    BasicDynaBean details = DatabaseHelper.queryToDynaBean(GET_SEQUENCE_ID_QUERY, sequenceName,
        typeNumber);
    String prefix = details.get(PREFIX_FIELD) != null ? (String) details.get(PREFIX_FIELD)
        : typeNumber;
    String pattern = details.get(PATTERN_FIELD) != null ? (String) details.get(PATTERN_FIELD)
        : "000000";
    Long nextId = details.get(NEXTVAL_FIELD) != null ? (Long) details.get(NEXTVAL_FIELD) : 0;
    return prependPrefix(prefix, pattern, nextId);
  }

  /**
   * Prepend prefix to the numeric portion of nextId.
   *
   * @param prefix  the prefix
   * @param pattern the pattern
   * @param number  the number
   * @return the prefixed id from number
   */
  public static String prependPrefix(String prefix, String pattern, Object number) {
    DecimalFormat format = (DecimalFormat) NumberFormat.getInstance();
    format.applyPattern(prefix + pattern);
    return format.format(number);
  }

}
