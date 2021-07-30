package com.bob.hms.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * The Class AutoIncrementId.
 */
public class AutoIncrementId {

  static Logger log = LoggerFactory.getLogger(AutoIncrementId.class);

  /**
   * Gets the new incr id.
   *
   * @param id    the id
   * @param table the table
   * @param type  the type
   * @return the new incr id
   * @throws SQLException the SQL exception
   */
  public static String getNewIncrId(String id, String table, String type) throws SQLException {
    String newClientId = null;
    try (Connection con = DataBaseUtil.getConnection()) {
      newClientId = AutoIncrementId.getNewIncrId(con, id, table, type);
    } catch (Exception exception) {
      log.error("", exception);
    }
    return newClientId;
  }

  private static final String GET_ID_PATTERN = "SELECT start_number, prefix, pattern FROM "
      + "unique_number WHERE type_number = ?";

  private static final String GET_ID = "SELECT ##ID## FROM ##TABLE## WHERE ##ID## "
      + " SIMILAR TO ? ORDER BY TO_NUMBER"
      + " (SUBSTRING(##ID## FROM ##PREFIXLENTOKEN## FOR 10), '999999')" + " DESC LIMIT 1";

  private static final String GET_SEQUENCE_ID = "SELECT nextval(?), prefix, pattern"
      + " FROM unique_number "
      + " WHERE type_number = ?";

  /**
   * Gets the new incr id.
   *
   * @param con   the con
   * @param id    the id
   * @param table the table
   * @param type  the type
   * @return the new incr id
   * @throws SQLException the SQL exception
   */
  public static String getNewIncrId(Connection con, String id, String table, String type)
      throws SQLException {
    String prefix = null;
    String lastId = null;
    String pattern = "0000";
    String strStartNo = null;
    int startNo = 0;
    int prefixLen = 0;
    String newClientId = null;
    log.debug("getNewIncrId called for: " + id + " " + table + " " + type);
    try (PreparedStatement ps = con.prepareStatement(GET_ID_PATTERN)) {
      ps.setString(1, type);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          strStartNo = rs.getString(1);
          prefix = rs.getString(2);
          pattern = rs.getString(3);
          startNo = Integer.parseInt(strStartNo);
          prefixLen = prefix.length();
          log.debug("startNo=" + startNo + " prefix=" + prefix + " Pattern=" + pattern);
        }
      }
    } catch (Exception exception) {
      log.error("", exception);
    }
    try (PreparedStatement ps = con.prepareStatement(
        GET_ID.replaceAll("##ID##", id).replaceAll("##TABLE##", table)
            .replaceAll("##PREFIXLENTOKEN##", String.valueOf(prefixLen + 1)),
        ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
      ps.setString(1, prefix + "[0-9]%");
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          lastId = rs.getString(1);
          log.debug("last ID: " + lastId);
        }
        int number;
        if (lastId == null) {
          number = startNo;
        } else {
          number = 1 + Integer.parseInt(lastId.substring(prefixLen));
        }
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance();
        decimalFormat.applyPattern(prefix + pattern);
        newClientId = decimalFormat.format(number);
      }

    } catch (Exception exception) {
      log.error("", exception);
    }
    log.info("ID generated for: " + id + " " + table + " " + type + " = " + newClientId);
    return newClientId;
  }

  /**
   * Gets the new incr unique id.
   *
   * @param id    the id
   * @param table the table
   * @param type  the type
   * @return the new incr unique id
   * @throws SQLException the SQL exception
   */
  /*
   * Wrapper for backward compatibility (after merging medi_numbers table to unique_number)
   */
  public static String getNewIncrUniqueId(String id, String table, String type)
      throws SQLException {
    return getNewIncrId(id, table, type);
  }

  /*
   * Incremented ID using sequences, rather than the table
   */

  /**
   * Gets the sequence id.
   *
   * @param sequenceName the sequence name
   * @param typeNumber   the type number
   * @return the sequence id
   * @throws SQLException the SQL exception
   */
  public static String getSequenceId(String sequenceName, String typeNumber) throws SQLException {
    int nextNumber = 0;
    String prefix = typeNumber;
    String pattern = "000000";
    String newId = null;

    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(GET_SEQUENCE_ID)) {
      ps.setString(1, sequenceName);
      ps.setString(2, typeNumber);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          nextNumber = rs.getInt(1);
          prefix = rs.getString(2);
          pattern = rs.getString(3);
        }
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance();
        decimalFormat.applyPattern(prefix + pattern);
        newId = decimalFormat.format(nextNumber);

        log.info(
            "Generated sequence ID for " + sequenceName + ", type " + typeNumber + " = " + newId);
      } catch (Exception exRs) {
        log.error("", exRs);
      }
    } catch (Exception exCon) {
      log.error("", exCon);
    }
    return newId;
  }

}
