package com.insta.instaapi.common;

import com.bob.hms.common.RequestContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.naming.InitialContext;
import javax.sql.DataSource;

public class DbUtil {

  static Logger logger = LoggerFactory.getLogger(DbUtil.class);

  /**
   * Get connection to database.
   * 
   * @param schemaName schema to be set for connection
   * @return Database connection
   */
  public static Connection getConnection(String schemaName) {
    return getConnection(30, schemaName);
  }

  /**
   * Get connection to database.
   * 
   * @param schemaName schema to be set for connection
   * @param timeout    timeout in ms for connection
   * @return Database connection
   */
  public static Connection getConnection(int timeout, String schemaName) {
    Connection con = null;
    PreparedStatement preStmt = null;

    if (schemaName != null && !schemaName.isEmpty()) {
      try {
        InitialContext ctx = new InitialContext();
        DataSource pl = (DataSource) ctx.lookup("java:comp/env/postgres");
        con = pl.getConnection();
        con.setAutoCommit(true);

        // not being able to use stmt.setString in set search_path: do quote removal
        // ourselves and concatenate
        /*
         * preStmt = con.prepareStatement("set search_path = ?"); preStmt.setString(1,
         * strHospitalId);
         */
        String initString = "set search_path = '" + schemaName.replaceAll("[^a-zA-Z0-9_]", "")
            + "'";
        if (timeout != 0) {
          int timeoutMilli = timeout * 1000;
          initString = initString + "; set statement_timeout = " + timeoutMilli;
        }

        preStmt = con.prepareStatement(initString);
        boolean res = preStmt.execute();
      } catch (SQLException ex) {
        closeConnections(con, preStmt);
        con = null;
        String strError = ex.getMessage().toString();
        String str1 = new String("ERROR: schema \"" + schemaName + "\" does not exist").toString();
        logger.error("Unable to get connection/set search path: " + ex);

      } catch (Exception ex) {
        closeConnections(con, null);
        con = null;
        logger.error("Could not get connection: ", ex);

      } finally {
        closeConnections(null, preStmt);
      }

    }
    RequestContext.addConnection(con);
    return con;
  }

  /**
   * Close connection objects.
   * 
   * @param con  Database Connection
   * @param stmt Statement Object
   */
  public static void closeConnections(Connection con, Statement stmt) {

    try {
      if (stmt != null) {
        stmt.close();
      }
      if (con != null) {
        if (!con.isClosed()) {
          con.close();
        }
      }
    } catch (SQLException se) {
      logger.debug(se.toString());
    }
  }

  /**
   * Close connection objects.
   * 
   * @param con  Database Connection
   * @param stmt Statement Object
   * @param rs   Result set Object
   */
  public static void closeConnections(Connection con, Statement stmt, ResultSet rs) {
    try {
      if (rs != null) {
        rs.close();
      }
      if (stmt != null) {
        stmt.close();
      }
      if (con != null) {
        if (!con.isClosed()) {
          con.close();
        }
      }
    } catch (SQLException se) {
      logger.debug(se.toString());
    }
  }

  /**
   * Close connection objects.
   * 
   * @param con Database Connection
   * @param ps  Prepared statement Object
   */
  public static void closeConnections(Connection con, PreparedStatement ps) {

    try {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        if (!con.isClosed()) {
          con.close();
        }
      }
    } catch (SQLException se) {
      logger.debug(se.toString());
    }
  }

  /**
   * Close connection objects.
   * 
   * @param con     Database Connection
   * @param success rollback or commit
   */
  public static void commitClose(Connection con, boolean success) throws SQLException {
    if (con != null) {
      if (success) {
        con.commit();
      } else {
        con.rollback();
      }
      con.close();
    }
  }

  public static final String GEN_ID = " SELECT generate_id(?)";

  /**
   * Get next pattern ID.
   * 
   * @param con       Database Connection
   * @param patternId Pattern ID
   */
  public static String getNextPatternId(Connection con, String patternId) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GEN_ID);
      ps.setString(1, patternId);
      return com.bob.hms.common.DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DbUtil.closeConnections(null, ps);
    }
  }

  private static String GET_SEQUENCE_ID = "SELECT nextval(?), prefix, pattern FROM unique_number "
      + " WHERE type_number = ?";

  /**
   * Get sequence ID.
   * 
   * @param con          Database Connection
   * @param sequenceName sequence name
   * @param typeNumber   type number
   */
  public static String getSequenceId(Connection con, String sequenceName, String typeNumber)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;

    int nextNumber = 0;
    String prefix = typeNumber;
    String pattern = "000000";
    String newId = null;

    try {
      stmt = con.prepareStatement(GET_SEQUENCE_ID);
      stmt.setString(1, sequenceName);
      stmt.setString(2, typeNumber);

      rs = stmt.executeQuery();
      if (rs.next()) {
        nextNumber = rs.getInt(1);
        prefix = rs.getString(2);
        pattern = rs.getString(3);
      }

      DecimalFormat decFmt = (DecimalFormat) NumberFormat.getInstance();
      decFmt.applyPattern(prefix + pattern);
      newId = decFmt.format(nextNumber);

      logger.info(
          "Generated sequence ID for " + sequenceName + ", type " + typeNumber + " = " + newId);

    } finally {
      DbUtil.closeConnections(null, stmt, rs);
    }
    return newId;
  }

  /**
   * Get next sequence ID.
   * 
   * @param con   Database Connection
   * @param table table name
   */
  public static int getNextSequence(Connection con, String table) throws SQLException {
    String query = "SELECT nextval(?)";
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(query);
      ps.setString(1, table + "_seq");
      return com.bob.hms.common.DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      com.insta.instaapi.common.DbUtil.closeConnections(null, ps);
    }
  }

  private static final String GET_DATE_TIME = "SELECT * FROM LOCALTIMESTAMP(0)";

  /**
   * Get current date and time from database.
   * 
   * @param con Database connection
   * @return current date and time from database
   */
  public static Timestamp getDateandTime(Connection con) {
    Timestamp getdateandTime = null;

    try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(GET_DATE_TIME)) {
      if (rs.next()) {
        getdateandTime = rs.getTimestamp(1);
      }
    } catch (Exception ex) {
      logger.error(ex.getMessage());
    }
    return getdateandTime;
  }
}
