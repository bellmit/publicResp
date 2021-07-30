package com.bob.hms.common;

import com.insta.hms.common.InputValidator;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.utils.EnvironmentUtil;
import com.insta.hms.exception.HMSException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import flexjson.JSONSerializer;

import net.sf.jasperreports.engine.JRException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 * The Class DataBaseUtil.
 */
public class DataBaseUtil {

  private static Logger logger = LoggerFactory.getLogger(DataBaseUtil.class);
  private static int schemaNameLen = 25;
  private static final String JNDI_DEFAULT_DATASOURCE = "java:comp/env/postgres";
  private static final String JNDI_READ_REPLICA = "java:comp/env/postgres_read";

  /**
   * Gets the connection.
   *
   * @return the connection
   */
  /*
   * Purpose of this method is to get the connetion and maintain Database pooling. Specifying a
   * timeout (in seconds) causes the query to be aborted (as well as resources freed up in the
   * database) after the query executes for that many seconds. Specifying 0 for the timout (eg, from
   * reports) will never timeout using this mechanism, instead, will timeout based on primrise.conf
   * setting.
   */
  public static Connection getConnection() {
    return getConnection(EnvironmentUtil.getDatabaseQueryTimeout());
  }

  /**
   * Gets the connection.
   *
   * @param readOnly the read only
   * @return the connection
   */
  public static Connection getConnection(boolean readOnly) {
    return getConnection(EnvironmentUtil.getDatabaseQueryTimeout(), readOnly);
  }

  /**
   * Gets the connection.
   *
   * @param timeout the timeout
   * @return the connection
   */
  public static Connection getConnection(int timeout) {
    return getConnection(timeout, false);
  }

  /**
   * Gets the connection.
   *
   * @param timeout  the timeout
   * @param readOnly the read only
   * @return the connection
   */
  @SuppressFBWarnings(value = "HARD_CODE_PASSWORD",
      justification = "To be refactored once we move to application user implementation")
  public static Connection getConnection(int timeout, boolean readOnly) {
    Connection con = null;
    PreparedStatement preStmt = null;
    String strErrorMsg = "";
    HttpSession session = RequestContext.getSession();

    if (session != null) {
      String strHospitalId = (String) session.getAttribute("sesHospitalId");

      try {
        if ((strHospitalId != null) && (!strHospitalId.equals(""))) {
          DataSource pl = getDataSource(readOnly);
          if (pl == null) {
            throw new HMSException("Unable to get datasource");
          }
          con = pl.getConnection();
          con.setAutoCommit(true);
          String strSafeHospitalId = InputValidator.getSafeSchemaString("hospital",
              strHospitalId.trim(), schemaNameLen, false);

          // not being able to use stmt.setString in set search_path: do quote removal
          // ourselves and concatenate
          /*
           * preStmt = con.prepareStatement("set search_path = ?"); preStmt.setString(1,
           * strHospitalId);
           */
          String initString = "set search_path = '" + strSafeHospitalId + "'" + ", 'extensions'";
          if (timeout != 0) {
            int timeoutMilli = timeout * 1000;
            initString = initString + "; set statement_timeout = " + timeoutMilli;
          }
          String username = RequestContext.getUserName();
          if (!StringUtil.isNullOrEmpty(username)) {
            initString = initString + "; set application.username = '" + username + "';";
          }
          preStmt = con.prepareStatement(initString);
          boolean res = preStmt.execute();
        } else {
          con = null;
          strErrorMsg = "Session Expired, Please Login Again";
          session.setAttribute("sesErr", strErrorMsg);
          logger.warn("session value for Hospital Id Expired");
        }

      } catch (SQLException exception) {
        closeConnections(con, preStmt);
        con = null;
        String strError = exception.getMessage().toString();
        String str1 = new String("ERROR: schema \"" + strHospitalId + "\" does not exist")
            .toString();
        if (strError.equals(str1)) {
          strErrorMsg = "Hospital " + strHospitalId + " does not exist";
          session.setAttribute("sesErr", strErrorMsg);
        }
        logger.error("Unable to get connection/set search path: " + exception);

      } catch (Exception exception) {
        closeConnections(con, null);
        con = null;
        logger.error("Could not get connection: ", exception);

      } finally {
        closeConnections(null, preStmt);
      }
    } else {
      // maybe this is not running within tomcat, see if we have a schema set in the RC directly
      String[] dbSchema = RequestContext.getConnectionDetails();
      if (dbSchema == null) {
        logger.error("Could not get schema or session");
        return null;
      }

      String host = dbSchema[0];
      String database = dbSchema[1];
      String schema = dbSchema[2];
      String dbPort = RequestContext.getDatabasePort().toString();
      if (database != null && !database.equals("")) {
        try {
          // we get a connection directly from the driver: this will not use a connection pool
          Class.forName("org.postgresql.Driver");
          Properties props = new Properties();
          props.put("user", "postgres");
          props.put("password", "");
          con = DriverManager.getConnection(
              "jdbc:postgresql://" + host + ":" + dbPort + "/" + database, props);
          String strSafeSchema = InputValidator.getSafeSchemaString("hospital", schema.trim(),
              schemaNameLen, false);
          String initString = "set search_path = '" + strSafeSchema + "'" + ", 'extensions'";
          if (timeout != 0) {
            int timeoutMilli = timeout * 1000;
            initString = initString + "; set statement_timeout = " + timeoutMilli;
          }
          preStmt = con.prepareStatement(initString);
          boolean res = preStmt.execute();

        } catch (Exception exception) {

          closeConnections(con, null);
          con = null;
          // no logger available, must print to console
          System.out.println("Could not get connection: " + exception.getMessage());
          exception.printStackTrace();

        } finally {
          closeConnections(null, preStmt);
        }
      } else {
        // which is executed from scheduled jobs.
        try {
          DataSource pl = getDataSource(readOnly);
          if (pl == null) {
            throw new HMSException("Unable to get datasource");
          }
          con = pl.getConnection();
          con.setAutoCommit(true);
          if (schema != null && !schema.equals("")) {
            String strSafeSchema = InputValidator.getSafeSchemaString("hospital", schema.trim(),
                schemaNameLen, false);
            String initString = "set search_path = '" + strSafeSchema + "'" + ", 'extensions'";
            if (timeout != 0) {
              int timeoutMilli = timeout * 1000;
              initString = initString + "; set statement_timeout = " + timeoutMilli;
            }
            preStmt = con.prepareStatement(initString);
            boolean res = preStmt.execute();
          }
        } catch (Exception exception) {
          logger.error("Could not get connection: ", exception);
        } finally {
          closeConnections(null, preStmt);
        }
      }
    }
    RequestContext.addConnection(con);
    return con;
  }

  /**
   * Gets the data source.
   *
   * @param readOnly the read only
   * @return the data source
   */
  private static DataSource getDataSource(boolean readOnly) {
    InitialContext context;
    DataSource dataSource = null;

    if (readOnly) {
      try {
        context = new InitialContext();
        dataSource = (DataSource) context.lookup(JNDI_READ_REPLICA);
        logger.debug("Getting connection from read-only-replica.");
      } catch (NamingException exception) {
        logger.debug("Could not get read-only connection: " + exception);
        dataSource = getDataSource(false);
      }
    } else {
      try {
        context = new InitialContext();
        dataSource = (DataSource) context.lookup(JNDI_DEFAULT_DATASOURCE);
      } catch (NamingException exception) {
        logger.error("Could not resolve data source: " + exception);
      }
    }
    return dataSource;
  }

  /**
   * Sets the nested loops.
   *
   * @param con    the con
   * @param enable the enable
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean setNestedLoops(Connection con, boolean enable) throws SQLException {
    try (PreparedStatement ps = con
        .prepareStatement("SET enable_nestloop=" + (enable ? "on" : "off"))) {
      return ps.execute();
    }
  }

  private static final String ALL_SCHEMAS = "SELECT nspname as schema FROM pg_catalog.pg_namespace"
      + "  WHERE nspname NOT LIKE 'pg_%' AND nspname NOT LIKE '%_temp%' AND "
      + " nspname NOT IN ('public', 'information_schema','extensions')";

  /**
   * Gets the all schemas.
   *
   * @param con        the con
   * @param stringList the string list
   * @return the all schemas
   * @throws SQLException the SQL exception
   */
  public static List getAllSchemas(Connection con, boolean stringList) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(ALL_SCHEMAS);
      if (stringList) {
        return queryToStringList(ps);
      } else {
        return queryToDynaList(ps);
      }
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Close connections.
   *
   * @param con  the con
   * @param stmt the stmt
   */
  public static void closeConnections(Connection con, Statement stmt) {

    try {
      if (stmt != null) {
        stmt.close();
      }
      if (con != null && !con.isClosed()) {
        con.close();
      }
    } catch (SQLException se) {
      logger.debug(se.toString());
    }
  }

  /**
   * Close connections.
   *
   * @param con  the con
   * @param stmt the stmt
   * @param rs   the rs
   */
  public static void closeConnections(Connection con, Statement stmt, ResultSet rs) {
    try {
      if (rs != null) {
        rs.close();
      }
      if (stmt != null) {
        stmt.close();
      }
      if (con != null && !con.isClosed()) {
        con.close();
      }
    } catch (SQLException se) {
      logger.debug(se.toString());
    }
  }

  /**
   * Close connections.
   *
   * @param con the con
   * @param ps  the ps
   */
  public static void closeConnections(Connection con, PreparedStatement ps) {

    try {
      if (ps != null) {
        ps.close();
      }
      if (con != null && !con.isClosed()) {
        con.close();
      }
    } catch (SQLException se) {
      logger.debug(se.toString());
    }
  }

  /**
   * Close connections.
   *
   * @param con the con
   * @param ps  the ps
   * @param rs  the rs
   */
  public static void closeConnections(Connection con, PreparedStatement ps, ResultSet rs) {
    try {
      if (rs != null) {
        rs.close();
      }
      if (ps != null) {
        ps.close();
      }
      if (con != null && !con.isClosed()) {
        con.close();
      }
    } catch (SQLException se) {
      logger.debug(se.toString());
    }
  }

  /**
   * Commit close.
   *
   * @param con     the con
   * @param success the success
   * @throws SQLException the SQL exception
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

  /**
   * Check batch updates.
   *
   * @param updates the updates
   * @return true, if successful
   */
  public static boolean checkBatchUpdates(int[] updates) {
    boolean success = true;
    for (int p = 0; p < updates.length; p++) {
      if (updates[p] <= 0) {
        success = false;
        logger
            .error("Batch Update failed Operation at index " + p + ": rows updated: " + updates[p]);
        break;
      }
    }
    return success;
  }

  /**
   * Gets the read only connection.
   *
   * @return the read only connection
   */
  public static Connection getReadOnlyConnection() {
    // for now return the same old connection: later, we will change this to do
    // something special, especially, this will be poolable more easily.
    return getConnection();
  }

  /**
   * Quote ident.
   *
   * @param ident the ident
   * @return the string
   */
  /*
   * Returns a table name / column name duly quoted or cleaned up for safe use within statements
   * constructed by appending strings. For identifiers, PreparedStatement.setObject cannot be used,
   * so we need to append these within the query itself, making them vulnerable to SQL Injections
   * unless these are cleansed/escaped correctly.
   *
   * This is similar to the postgres builtin function quote_ident.
   */
  public static String quoteIdent(String ident) {
    return quoteIdent(ident, false);
  }

  /**
   * Quote ident.
   *
   * @param ident       the ident
   * @param forceQuotes the force quotes
   * @return the string
   */
  public static String quoteIdent(String ident, boolean forceQuotes) {
    boolean needsQuotes = forceQuotes;
    if (!forceQuotes) {
      /*
       * We allow alias.fieldname or fieldname to go through unquoted. alias has to start with
       * a-zA-Z_ and can contain a-zA-Z_0-9. Same for fieldname.
       */
      needsQuotes = !ident.matches("([a-zA-Z_]\\w*\\.)?[a-zA-Z_]\\w*");
    }
    if (needsQuotes) {
      // TODO: to be technically correct, we need to quote the alias and fieldname independently
      // but it is unlikely that we actually have special characters in our field names.

      // replace any double-quotes with double double-quotes
      ident = ident.replace("\"", "\"\"");
      // add double quotes around the identifier to make it safe
      ident = "\"" + ident + "\"";
    }
    return ident;
  }

  /**
   * Query to dyna list.
   *
   * @param query the query
   * @return the list
   * @throws SQLException the SQL exception
   */
  /*
   * Returns a list of BasicDynaBean objects for the given query string. For use in JSTL, the
   * getMap() method of the DynaBean is useful to access the columns, for example:
   * ${row.map.patient_id}.
   */
  public static List queryToDynaList(String query) throws SQLException {
    return queryToDynaList(query, (short) 30);
  }

  /**
   * Query to dyna list.
   *
   * @param query   the query
   * @param timeout the timeout
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List queryToDynaList(String query, short timeout) throws SQLException {
    List resultList = null;
    try (Connection con = DataBaseUtil.getConnection(timeout)) {
      if (con != null) {
        try (PreparedStatement ps = con.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
          RowSetDynaClass rsd = new RowSetDynaClass(rs);
          resultList = rsd.getRows();
        }
      }
    }
    return resultList;
  }

  /**
   * Query to dyna list.
   *
   * @param ps the ps
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List queryToDynaList(PreparedStatement ps) throws SQLException {

    ResultSet rs = ps.executeQuery();
    RowSetDynaClass rsd = new RowSetDynaClass(rs); // Why is this function needed if we have
    // queryToDynaListWithCase
    rs.close();
    return rsd.getRows();
  }

  /**
   * Variation of queryToDynaList taking one int parameter for doing setInt in the query that is
   * passed. Query string must contain one and only one ?
   *
   * @param query the query
   * @param val   the val
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List queryToDynaList(String query, int val) throws SQLException {
    List resultList = null;
    try (Connection con = DataBaseUtil.getReadOnlyConnection()) {
      if (null != con) {
        try (PreparedStatement ps = con.prepareStatement(query)) {
          ps.setInt(1, val); /* set the passed in int val */
          try (ResultSet rs = ps.executeQuery()) {
            RowSetDynaClass rsd = new RowSetDynaClass(rs);
            resultList = rsd.getRows();
          }
        }
      }
    }
    return resultList;
  }

  /**
   * Variation of queryToDynaList taking one String parameter for doing setString in the query that
   * is passed. Query string must contain one and only one ?
   *
   * @param query the query
   * @param val   the val
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List queryToDynaList(String query, String val) throws SQLException {
    List resultList = null;
    try (Connection con = DataBaseUtil.getReadOnlyConnection()) {
      if (null != con) {
        try (PreparedStatement ps = con.prepareStatement(query)) {
          ps.setString(1, val); /* set the passed in String val */
          try (ResultSet rs = ps.executeQuery()) {
            RowSetDynaClass rsd = new RowSetDynaClass(rs);
            resultList = rsd.getRows();
          }
        }
      }
    }
    return resultList;
  }

  /**
   * Query to dyna list.
   *
   * @param query the query
   * @param val1  the val 1
   * @param val2  the val 2
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List queryToDynaList(String query, String val1, String val2) throws SQLException {
    List resultList = null;
    try (Connection con = DataBaseUtil.getReadOnlyConnection()) {
      if (null != con) {
        try (PreparedStatement ps = con.prepareStatement(query)) {
          ps.setString(1, val1); /* set the passed in String val */
          ps.setString(2, val2); /* set the passed in String val */
          try (ResultSet rs = ps.executeQuery()) {
            RowSetDynaClass rsd = new RowSetDynaClass(rs);
            resultList = rsd.getRows();
          }
        }
      }
    }
    return resultList;
  }

  /**
   * DynaList returned for query with any number of parameters, passed in as object array.
   *
   * @param query   the query
   * @param values  the values
   * @param timeout the timeout
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> queryToDynaList(String query, Object[] values, short timeout)
      throws SQLException {
    List<BasicDynaBean> listDynaBeans = null;

    try (Connection con = DataBaseUtil.getConnection()) {
      if (null != con) {
        try (PreparedStatement ps = con.prepareStatement(query)) {
          int index = 1;
          for (Object val : values) {
            ps.setObject(index++, val);
          }
          try (ResultSet rs = ps.executeQuery()) {
            RowSetDynaClass rsd = new RowSetDynaClass(rs);
            listDynaBeans = rsd.getRows();
          }
        }
      }
    }
    return listDynaBeans;
  }

  /**
   * Query to dyna list.
   *
   * @param query  the query
   * @param values the values
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> queryToDynaList(String query, Object[] values)
      throws SQLException {
    return queryToDynaList(query, values, (short) 30);
  }

  /**
   * Query to dyna list.
   *
   * @param con    the con
   * @param query  the query
   * @param values the values
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> queryToDynaList(Connection con, String query, Object[] values)
      throws SQLException {
    List<BasicDynaBean> listDynaBeans = null;
    if (con != null) {
      try (PreparedStatement ps = con.prepareStatement(query)) {
        int index = 1;
        if (values != null) {
          for (Object val : values) {
            ps.setObject(index++, val);
          }
        }
        try (ResultSet rs = ps.executeQuery()) {
          RowSetDynaClass rsd = new RowSetDynaClass(rs);
          listDynaBeans = rsd.getRows();
        }
      }
    }
    return listDynaBeans;
  }

  /**
   * Query to dyna list.
   *
   * @param con   the con
   * @param query the query
   * @param value the value
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> queryToDynaList(Connection con, String query, Object value)
      throws SQLException {
    return queryToDynaList(con, query, new Object[] { value });
  }


  public static List queryToDynaList(String query, Connection con) throws SQLException {
    return queryToDynaList(query, (short) 30);
  }

  /**
   * Query to List of dyna beans.
   * @param con connection
   * @param timeout connection time out
   * @param query the query
   * @return List of Dynabeans
   * @throws SQLException the SQL exception
   */
  public static List queryToDynaList(String query, short timeout, Connection con)
      throws SQLException {

    PreparedStatement ps = null;
    ResultSet rs = null;
    List list;
    try {
      con = DataBaseUtil.getConnection(timeout);
      ps = con.prepareStatement(query);
      rs = ps.executeQuery();
      RowSetDynaClass rsd = new RowSetDynaClass(rs);
      list = rsd.getRows();
    } finally {
      DataBaseUtil.closeConnections(null, ps, rs);
    }

    return list;
  }

  /**
   * Query to List of dyna beans.
   * @param con connection
   * @param query the query
   * @param val the value
   * @return List of Dynabeans
   * @throws SQLException the SQL exception
   */
  public static List queryToDynaList(Connection con, String query, String val) throws SQLException {

    PreparedStatement ps = null;
    ResultSet rs = null;
    List list;

    try {
      ps = con.prepareStatement(query);
      ps.setString(1, val); /* set the passed in String val */
      rs = ps.executeQuery();
      RowSetDynaClass rsd = new RowSetDynaClass(rs);
      list = rsd.getRows();
    } finally {
      DataBaseUtil.closeConnections(null, ps, rs);
    }

    return list;
  }

  /**
   * Same as above, but maintains the case of the columns in the properties.
   *
   * @param ps the ps
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List queryToDynaListWithCase(PreparedStatement ps) throws SQLException {

    ResultSet rs = ps.executeQuery();
    RowSetDynaClass rsd = new RowSetDynaClass(rs, false);
    rs.close();
    return rsd.getRows();
  }

  /**
   * TODO: remove this, it is the same as above and adds no value. Query to dyna list dates.
   *
   * @param ps the ps
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List queryToDynaListDates(PreparedStatement ps) throws SQLException {
    ResultSet rs = null;
    rs = ps.executeQuery();
    RowSetDynaClass rsd = new RowSetDynaClass(rs);
    return rsd.getRows();
  }

  /**
   * Returns a list of DynaBeans for the given query, and a from/to dates (query must have two ?s
   * for these dates).
   *
   * @param query    the query
   * @param fromDate the from date
   * @param toDate   the to date
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List queryToDynaListDates(String query, java.sql.Date fromDate,
      java.sql.Date toDate) throws SQLException {
    try (Connection con = DataBaseUtil.getReadOnlyConnection()) {
      if (con != null) {
        return queryToDynaListDates(con, query, fromDate, toDate);
      }
    }
    return null;
  }

  /**
   * Query to dyna list dates.
   *
   * @param con      the con
   * @param query    the query
   * @param fromDate the from date
   * @param toDate   the to date
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List queryToDynaListDates(Connection con, String query, java.sql.Date fromDate,
      java.sql.Date toDate) throws SQLException {
    List resultList = null;
    try (PreparedStatement ps = con.prepareStatement(query)) {
      ps.setDate(1, fromDate);
      ps.setDate(2, toDate);
      try (ResultSet rs = ps.executeQuery()) {
        RowSetDynaClass rsd = new RowSetDynaClass(rs);
        resultList = rsd.getRows();
      }
    }
    return resultList;
  }

  /**
   * DynaBean returned for query with any number of parameters, passed in as object array, returns
   * the bean of the first row, if any. Returns null if no rows found.
   *
   * @param query  the query
   * @param values the values
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean queryToDynaBean(String query, Object[] values) throws SQLException {
    return queryToDynaBean(query, values, (short) 30);
  }

  /**
   * Query to dyna bean.
   *
   * @param query   the query
   * @param values  the values
   * @param timeout the timeout
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean queryToDynaBean(String query, Object[] values, short timeout)
      throws SQLException {
    List<BasicDynaBean> listDynaBean = queryToDynaList(query, values, timeout);
    if ((listDynaBean != null) && (listDynaBean.size() > 0)) {
      return listDynaBean.get(0);
    }
    return null;
  }

  /**
   * Query to dyna bean.
   *
   * @param query the query
   * @param value the value
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean queryToDynaBean(String query, String value) throws SQLException {
    List<BasicDynaBean> listDynaBean = queryToDynaList(query, value);
    if ((listDynaBean != null) && (listDynaBean.size() > 0)) {
      return listDynaBean.get(0);
    }
    return null;
  }

  /**
   * Query to dyna bean.
   *
   * @param query the query
   * @param value the value
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean queryToDynaBean(String query, int value) throws SQLException {
    List<BasicDynaBean> listDynaBean = queryToDynaList(query, value);
    if ((listDynaBean != null) && (listDynaBean.size() > 0)) {
      return listDynaBean.get(0);
    }
    return null;
  }

  /**
   * Query to dyna bean.
   *
   * @param connection the connection
   * @param query      the query
   * @param values     the values
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean queryToDynaBean(Connection connection, String query, Object[] values)
      throws SQLException {
    List<BasicDynaBean> beans = queryToDynaList(connection, query, values);

    if ((null != beans) && !(beans.isEmpty())) {
      return beans.get(0);
    }
    return null;
  }

  /**
   * Query to dyna bean.
   *
   * @param ps the ps
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean queryToDynaBean(PreparedStatement ps) throws SQLException {
    List<BasicDynaBean> listDynaBean = queryToDynaList(ps);
    if ((listDynaBean != null) && (listDynaBean.size() > 0)) {
      return listDynaBean.get(0);
    }
    return null;
  }

  /**
   * Query to dyna bean.
   *
   * @param con database connection
   * @param query the query
   * @param value the value
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean queryToDynaBean(Connection con, String query, String value)
      throws SQLException {
    List<BasicDynaBean> list = queryToDynaList(con, query, value);
    if ((list != null) && (list.size() > 0)) {
      return list.get(0);
    }
    return null;
  }

  /**
   * Return a list of objects (the first column only), so there is no map/dynabean involved.
   *
   * @param query   the query
   * @param values  the values
   * @param timeout the timeout
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List queryToList(String query, Object[] values, short timeout) throws SQLException {
    List resultList = new ArrayList();
    try (Connection con = DataBaseUtil.getConnection(timeout)) {
      if (con != null) {
        try (PreparedStatement ps = con.prepareStatement(query)) {
          int index = 1;
          for (Object val : values) {
            ps.setObject(index++, val);
          }
          try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
              resultList.add(rs.getObject(1));
            }
          }
        }
      }
    }
    return resultList;
  }

  /**
   * Query to list.
   *
   * @param query the query
   * @param value the value
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List queryToList(String query, Object value) throws SQLException {
    return queryToList(query, new Object[] { value }, (short) 30);
  }

  /**
   * Query to list.
   *
   * @param query the query
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List queryToList(String query) throws SQLException {
    return queryToList(query, new Object[] {}, (short) 30);
  }

  /**
   * Use this for JSONizing large lists. We write each row as and when we get it from the DB,
   * minimizing the amount of memory required. The alternative would have been to get a dynaList and
   * then serialize that, which means that the entire array would have been in memory, making it
   * slightly slower as well. This writes the entire result into the writer as a JSON array, even if
   * there is only one row. Variable names etc, if required, can be set outside of this call.
   *
   * @param writer  the writer
   * @param query   the query
   * @param values  the values
   * @param timeout the timeout
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public static void queryToJson(Writer writer, String query, Object[] values, short timeout)
      throws SQLException, IOException {
    JSONSerializer js = new JSONSerializer().exclude("class");
    try (Connection con = DataBaseUtil.getConnection(timeout)) {
      if (con != null) {
        con.setAutoCommit(false); // required for setFetchSize to work
        try (PreparedStatement ps = con.prepareStatement(query)) {
          ps.setFetchSize(1000); // fetch only 1000 rows at a time, otherwise, will get the entire
          // result
          int index = 1;
          if (values != null) {
            for (Object val : values) {
              ps.setObject(index++, val);
            }
          }
          try (ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData rsMetaData = rs.getMetaData();
            writer.write("[");
            while (rs.next()) {
              Map row = new HashMap();
              for (int col = 1; col <= rsMetaData.getColumnCount(); col++) {
                row.put(rsMetaData.getColumnName(col), rs.getObject(col));
              }
              js.serialize(row, writer);
              if (!rs.isLast()) {
                writer.write(",");
              }
            }
            writer.write("]");
          }
        }
      }
    }
  }

  /**
   * Query to json.
   *
   * @param writer the writer
   * @param query  the query
   * @param values the values
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public static void queryToJson(Writer writer, String query, Object[] values)
      throws SQLException, IOException {
    queryToJson(writer, query, values, (short) 30);
  }

  /**
   * Execute query.
   *
   * @param con    the con
   * @param query  the query
   * @param values the values
   * @return the int
   * @throws SQLException the SQL exception
   */
  public static int executeQuery(Connection con, String query, Object[] values)
      throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(query)) {
      if (values != null) {
        int index = 1;
        for (Object val : values) {
          ps.setObject(index++, val);
        }
      }
      return ps.executeUpdate();
    }
  }

  /**
   * Execute query.
   *
   * @param con   the con
   * @param query the query
   * @param value the value
   * @return the int
   * @throws SQLException the SQL exception
   */
  public static int executeQuery(Connection con, String query, Object value) throws SQLException {
    return executeQuery(con, query, new Object[] { value });
  }

  /**
   * Execute query.
   *
   * @param con    the con
   * @param query  the query
   * @param value1 the value 1
   * @param value2 the value 2
   * @return the int
   * @throws SQLException the SQL exception
   */
  public static int executeQuery(Connection con, String query, Object value1, Object value2)
      throws SQLException {
    return executeQuery(con, query, new Object[] { value1, value2 });
  }

  /**
   * Converts a list of DynaBeans (rows) into a HashMap based on the value of one column, which is
   * the identifier of the row. Useful for looking up the row of a known ID.
   *
   * @param rows       the rows
   * @param columnName the column name
   * @return the hash map
   */
  public static HashMap mapDynaRowSet(List rows, String columnName) {
    HashMap rowSetMap = new HashMap();
    Iterator it = rows.iterator();
    while (it.hasNext()) {
      DynaBean row = (DynaBean) it.next();
      rowSetMap.put(row.get(columnName), row);
    }
    return rowSetMap;
  }

  /**
   * Dynamically built update statement and executed. Parameters: Connection con connection to use
   * for the update String tableName name of table to update Map fields: (String)field =>
   * (Object)value a set of field-values to be set in the table Map keys: (String)field =>
   * (Object)value a set of field-values for the WHERE clause Returns: number of rows updated.
   * 
   * @param con    the con
   * @param table  the table
   * @param fields the fields
   * @param keys   the keys
   * @return the int
   * @throws SQLException the SQL exception
   */
  public static int dynaUpdate(Connection con, String table, Map fields, Map keys)
      throws SQLException {

    StringBuilder stmt = new StringBuilder();
    stmt.append("UPDATE ").append(table).append(" SET ");

    /*
     * Append a field=?[,...] statement for every field
     */
    boolean first = true;
    Iterator fieldIt = fields.keySet().iterator();
    while (fieldIt.hasNext()) {
      String fieldName = (String) fieldIt.next();
      if (!first) {
        stmt.append(", ");
      }
      first = false;
      stmt.append(fieldName).append("=?");
    }
    if (first) {
      // No fields supplied, error out
      return 0;
    }

    /*
     * Append a WHERE clause based on the key, if any
     */
    if (keys != null) {
      stmt.append(" WHERE ");
      Iterator keyIt = keys.keySet().iterator();
      boolean keyFirst = true;
      while (keyIt.hasNext()) {
        String fieldName = (String) keyIt.next();
        if (!keyFirst) {
          stmt.append(" AND ");
        }
        keyFirst = false;
        stmt.append(fieldName).append("=?");
      }
    }

    PreparedStatement ps = null;
    int rows = 0;

    try {
      ps = con.prepareStatement(stmt.toString());
      int pos = 1;
      /*
       * Do a setXXX for every field in fields as well as every field in keys
       */
      Iterator valIt = fields.keySet().iterator();
      while (valIt.hasNext()) {
        String fieldName = (String) valIt.next();
        Object value = fields.get(fieldName);
        ps.setObject(pos++, value);
      }

      if (keys != null) {
        Iterator keyIt = keys.keySet().iterator();
        while (keyIt.hasNext()) {
          String fieldName = (String) keyIt.next();
          Object value = keys.get(fieldName);
          ps.setObject(pos++, value);
        }
      }
      rows = ps.executeUpdate();
    } finally {
      if (ps != null) {
        ps.close();
      }
    }

    return rows;
  }

  /**
   * Dynamically built INSERT statement and executed. Parameters: Connection con connection to use
   * for the update String tableName name of table to update Map fields: (String)field =>
   * (Object)value a set of field-values to be set in the table Returns: true (success) or false
   *
   * @param con    the con
   * @param table  the table
   * @param fields the fields
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean dynaInsert(Connection con, String table, Map fields) throws SQLException {

    StringBuilder stmt = new StringBuilder();
    stmt.append("INSERT INTO ").append(table).append(" (");

    /*
     * Append the field name
     */
    boolean first = true;
    Iterator fieldIt = fields.keySet().iterator();
    while (fieldIt.hasNext()) {
      String fieldName = (String) fieldIt.next();
      if (!first) {
        stmt.append(", ");
      }
      first = false;
      stmt.append(fieldName);
    }
    if (first) {
      // No fields supplied, error out
      return false;
    }

    stmt.append(") VALUES (");

    /*
     * Append a ?, for every field value
     */
    first = true;
    for (int i = 0; i < fields.size(); i++) {
      if (!first) {
        stmt.append(", ");
      }
      first = false;
      stmt.append("?");
    }

    stmt.append(")");

    PreparedStatement ps = null;
    int rows = 0;

    try {
      ps = con.prepareStatement(stmt.toString());
      int pos = 1;
      /*
       * Do a setXXX for every field in fields
       */
      Iterator valIt = fields.keySet().iterator();
      while (valIt.hasNext()) {
        String fieldName = (String) valIt.next();
        Object value = fields.get(fieldName);
        ps.setObject(pos++, value);
      }

      /*
       * Execute
       */
      rows = ps.executeUpdate();

    } finally {
      if (ps != null) {
        ps.close();
      }
    }

    return (rows == 1);
  }

  /**
   * Use to check if the SQLException is due to a duplicate key Instead of pre-checking unique
   * values, we can trap this exception and show a message This is probably non-portable across
   * other databases.
   *
   * @param exception the exception
   * @return true, if is duplicate violation
   */
  public static boolean isDuplicateViolation(SQLException exception) {

    if (exception instanceof java.sql.SQLIntegrityConstraintViolationException) {
      logger.debug("Integrity constraint violation");
      return true;
    }

    if (exception.getMessage().contains("duplicate key")) {
      logger.debug("Duplicate key in message");
      return true;
    }

    return false;
  }

  /**
   * Checks if is foreign key violation.
   *
   * @param exception the exception
   * @return true, if is foreign key violation
   */
  public static boolean isForeignKeyViolation(SQLException exception) {
    if (exception instanceof java.sql.SQLIntegrityConstraintViolationException) {
      logger.debug("Foreign Key violation");
      return true;
    }

    if (exception.getMessage().contains("foreign key")) {
      logger.debug("Foreign Key in message");
      return true;
    }

    return false;
  }

  /**
   * Checks if is report design invalid.
   *
   * @param exception the exception
   * @return true, if is report design invalid
   */
  public static boolean isReportDesignInvalid(JRException exception) {
    if (exception instanceof net.sf.jasperreports.engine.design.JRValidationException) {
      logger.debug("Jasper Exception");
      return true;
    }

    if (exception.getMessage().contains("Report design not valid")) {
      logger.debug("Report design error");
      return true;
    }
    return false;
  }

  /**
   * Checks if is timeout.
   *
   * @param exception the exception
   * @return true, if is timeout
   */
  public static boolean isTimeout(SQLException exception) {
    String state = exception.getSQLState();
    if (state != null && state.equals("57014")) { // i.e. SQL query interrupted state
      logger.debug("SQL State interrupted");
      return true;
    }
    String msg = exception.getMessage();
    if (msg != null && msg.contains("statement timeout")) {
      logger.debug("Statement Timeout in message");
      return true;
    }
    return false;
  }

  public static final String GEN_ID = " SELECT generate_id(?)";

  /**
   * Gets the next pattern id.
   *
   * @param patternId the pattern id
   * @return the next pattern id
   * @throws SQLException the SQL exception
   */
  public static String getNextPatternId(Connection connection, String patternId)
      throws SQLException {
    return getStringValueFromDb(connection, GEN_ID, patternId);
  }
  
  /**
   * Gets the next pattern id.
   *
   * @param patternId the pattern id
   * @return the next pattern id
   * @throws SQLException the SQL exception
   */
  public static String getNextPatternId(String patternId) throws SQLException {
    return getStringValueFromDb(GEN_ID, patternId);
  }

  /**
   * Query to array list.
   *
   * @param query the query
   * @return the array list
   */
  public static ArrayList queryToArrayList(String query) {
    ArrayList arrResult = null;
    try (Connection con = getConnection()) {
      if (con != null) {
        try (Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY)) {
          logger.debug("DataBaseUtils queryToArrayList The query is :=  " + query);
          try (ResultSet rs = stmt.executeQuery(query)) {
            arrResult = new ArrayList();
            Hashtable hashtable = null;
            ResultSetMetaData resultSetMetaData = rs.getMetaData();

            while (rs.next()) {
              hashtable = new Hashtable();

              for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                if (rs.getObject(i) == null) {
                  hashtable.put(resultSetMetaData.getColumnName(i).toUpperCase(), "");
                } else {
                  hashtable.put(resultSetMetaData.getColumnName(i).toUpperCase(), rs.getString(i));
                }
              }
              arrResult.add(hashtable);

            } // while
          }
        }
      }
    } catch (Exception ex) {
      logger.error("DataBaseUtils queryToArrayList Exception : ", ex);
    }
    return arrResult;
  }

  /**
   * Query to array list.
   *
   * @param query the query
   * @param parameters parameters to be passed to query
   * @return the array list
   */
  public static ArrayList queryToArrayList(String query, Object[] parameters) {
    try (Connection con = getConnection(); 
        PreparedStatement ps = con.prepareStatement(query)) {
      int idx = 1;
      for (Object parameter: parameters) {
        ps.setObject(idx++, parameter);
      }
      return queryToArrayList(ps);
    } catch (SQLException ex) {
      logger.error("DataBaseUtils queryToArrayList Exception : ", ex);
    }
    return null;
  }

  /**
   * Normal query to array list: use when a prepared statement can be constructed using
   * ps.setString() etc.
   *
   * @param stmt the stmt
   * @return the array list
   * @throws SQLException the SQL exception
   */
  public static ArrayList queryToArrayList(PreparedStatement stmt) throws SQLException {
    ArrayList arrResult = new ArrayList();
    try (ResultSet rs = stmt.executeQuery()) {
      ResultSetMetaData resultSetMetaData = rs.getMetaData();
      while (rs.next()) {
        Hashtable hashtable = new Hashtable();
        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
          if (rs.getObject(i) == null) {
            hashtable.put(resultSetMetaData.getColumnName(i).toUpperCase(), "");
          } else {
            hashtable.put(resultSetMetaData.getColumnName(i).toUpperCase(), rs.getString(i));
          }
        }
        arrResult.add(hashtable);
      }
    }
    return arrResult;
  }

  /**
   * Use the following only when the query is a static simple string, not dynamically constructed.
   *
   * @param query the query
   * @return the array list
   * @throws SQLException the SQL exception
   */
  public static ArrayList simpleQueryToArrayList(String query) throws SQLException {
    try (Connection con = DataBaseUtil.getConnection()) {
      if (con != null) {
        try (PreparedStatement ps = con.prepareStatement(query)) {
          return queryToArrayList(ps);
        }
      }
    }
    return null;
  }

  /**
   * Query to array list.
   * This returns list of values of first column of the resultset.
   * @param query the query
   * @return the array list
   */
  public static ArrayList queryToArrayList1(String query) {
    ArrayList arrResult = null;
    try (Connection con = getConnection()) {
      if (con != null) {
        try (Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY)) {
          try (ResultSet rs = stmt.executeQuery(query)) {
            arrResult = new ArrayList();
            while (rs.next()) {
              if (rs.getObject(1) == null) {
                arrResult.add(null);
              } else {
                arrResult.add(rs.getString(1));
              }
            }
          }
        }
      }
    } catch (SQLException se) {
      logger.error("", se);
    } catch (Exception ex) {
      logger.error("", ex);
    }
    return arrResult;
  }

  /**
   * Returns an array of strings (first column converted to string) for the given query.
   *
   * @param stmt the stmt
   * @return the array list
   * @throws SQLException the SQL exception
   */
  public static ArrayList queryToArrayList1(PreparedStatement stmt) throws SQLException {
    ArrayList arrResult = new ArrayList();

    try (ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        if (rs.getObject(1) == null) {
          arrResult.add(null);
        } else {
          arrResult.add(rs.getString(1));
        }
      }
    }
    return arrResult;
  }

  /**
   * Query to string list.
   *
   * @param stmt the stmt
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List<String> queryToStringList(PreparedStatement stmt) throws SQLException {
    return queryToArrayList1(stmt);
  }

  /**
   * Gets the xml content with no child.
   *
   * @param sqlQuery the sql query
   * @param id       the id
   * @return the xml content with no child
   */
  // QUERYTOARRAYLIST CLOSE
  public static String getXmlContentWithNoChild(String sqlQuery, String id) {
    StringBuffer xmlcontent = new StringBuffer();
    int len;
    char char1;
    String strName;
    String str;

    try (Connection con = getConnection()) {
      if (con != null) {
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sqlQuery)) {
          ResultSetMetaData rsmd = rs.getMetaData();
          String root = id.toLowerCase();
          xmlcontent.append("<?xml version=\"1.0\"?>");
          xmlcontent.append("<xml id=\"" + id + "\">");
          xmlcontent.append("<" + id + "s>");
          str = null;
          while (rs.next()) {
            xmlcontent.append("<" + root + " ");
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
              strName = rs.getString(i);
              if (strName == null) {
                str = "";
              } else {
                str = strName;
              }
              str = replaceSymbolsInXmlContent(str);
              /*
               * len=str.length();
               *
               *
               *
               * String x="&"; str=str.replaceAll(x,"&amp;");
               *
               * String y="'"; str= str.replaceAll(y,"&apos;");
               *
               * System.out.println("strName"+str);
               */

              xmlcontent.append("class" + i + "=\"" + str + "\" ");
            } // end of for
            xmlcontent.append("></" + root + ">");
          }
          xmlcontent.append("</" + id + "s>");
          xmlcontent.append("</xml>");
          rs.close();
          stmt.close();
        }
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    return xmlcontent.toString();
  }

  /**
   * Gets the xml content with no child.
   *
   * @param stmt the stmt
   * @param id   the id
   * @return the xml content with no child
   * @throws SQLException the SQL exception
   */
  public static String getXmlContentWithNoChild(PreparedStatement stmt, String id)
      throws SQLException {
    StringBuffer xmlcontent = new StringBuffer();
    int len;
    char char1;
    String strName;
    try (ResultSet rs = stmt.executeQuery()) {
      ResultSetMetaData rsmd = rs.getMetaData();
      String root = id.toLowerCase();
      xmlcontent.append("<?xml version=\"1.0\"?>");
      xmlcontent.append("<xml id=\"" + id + "\">");
      xmlcontent.append("<" + id + "s>");
      String str;
      str = null;
      while (rs.next()) {
        xmlcontent.append("<" + root + " ");
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
          strName = rs.getString(i);
          if (strName == null) {
            str = "";
          } else {
            str = strName;
          }
          str = replaceSymbolsInXmlContent(str);
          /*
           * len=str.length();
           *
           *
           *
           * String x="&"; str=str.replaceAll(x,"&amp;");
           *
           * String y="'"; str= str.replaceAll(y,"&apos;");
           *
           * System.out.println("strName"+str);
           */

          xmlcontent.append("class" + i + "=\"" + str + "\" ");
        } // end of for
        xmlcontent.append("></" + root + ">");
      }
      xmlcontent.append("</" + id + "s>");
      xmlcontent.append("</xml>");
      stmt.close();
    }
    return xmlcontent.toString();
  }

  /**
   * Query to only array list.
   *
   * @param query the query
   * @return the array list
   */
  // deprecated
  public static ArrayList queryToOnlyArrayList(String query) {
    ArrayList arrResult = null;
    try (Connection con = getConnection()) {
      if (con != null) {
        try (Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY)) {
          logger.debug("queryToOnlyArrayList: The query is :=  " + query);
          try (ResultSet rs = stmt.executeQuery(query)) {
            arrResult = new ArrayList();
            Hashtable hashtable = null;
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            while (rs.next()) {
              for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                if (rs.getObject(i) == null) {
                  arrResult.add("");
                } else {
                  arrResult.add(rs.getString(i));
                }

              }
            }
          }
        }
      }
    } catch (SQLException se) {
      logger.error("", se);
    } catch (Exception ex) {
      logger.error("", ex);
    }
    return arrResult;
  }

  /**
   * Query to only array list.
   *
   * @param psmt the psmt
   * @return the array list
   * @throws SQLException the SQL exception
   */
  public static ArrayList<String> queryToOnlyArrayList(PreparedStatement psmt) throws SQLException {
    ArrayList<String> al = new ArrayList<String>();
    try (ResultSet rs = psmt.executeQuery()) {
      ResultSetMetaData resultSetMetaData = rs.getMetaData();
      while (rs.next()) {
        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
          if (rs.getObject(i) == null) {
            al.add("");
          } else {
            al.add(rs.getString(i));
          }
        }
      }
    }
    return al;
  }

  /**
   * Gets the string value from DB.
   *
   * @param dbQuery the DB query
   * @return the string value from DB
   */
  public static String getStringValueFromDb(String dbQuery) {
    String value = null;
    try (Connection con = getConnection()) {
      if (con != null) {
        try (Statement st = con.createStatement()) {
          logger.debug("DBQuery is @@@@@@@@@@========> " + dbQuery);
          try (ResultSet rs = st.executeQuery(dbQuery)) {
            if (rs.next()) {
              value = rs.getString(1);
              return value;
            }
          }
        }
      }
    } catch (Exception exception) {
      logger.error("getStringValueFromDB: ", exception);
      return value;
    }
    return value;
  }

  /**
   * Gets the string value from DB.
   *
   * @param ps the ps
   * @return the string value from DB
   * @throws SQLException the SQL exception
   */
  public static String getStringValueFromDb(PreparedStatement ps) throws SQLException {
    String value = null;
    try (ResultSet rs = ps.executeQuery()) {
      if (rs.next()) {
        value = rs.getString(1);
      }
    }
    return value;
  }

  /**
   * Gets the string value from DB.
   *
   * @param query  the query
   * @param param1 the param 1
   * @return the string value from DB
   * @throws SQLException the SQL exception
   */
  public static String getStringValueFromDb(String query, Object param1) throws SQLException {
    try (Connection con = getConnection()) {
      return getStringValueFromDb(con, query, param1);
    }
  }


  /**
   * Gets the string value from DB.
   *
   * @param con the connection 
   * @param query  the query
   * @param param1 the param 1
   * @return the string value from DB
   * @throws SQLException the SQL exception
   */
  public static String getStringValueFromDb(Connection con, String query, Object param1)
      throws SQLException {
    if (con != null) {
      try (PreparedStatement ps = con.prepareStatement(query)) {
        ps.setObject(1, param1);
        return getStringValueFromDb(ps);
      }
    }
    return null;
  }

  /**
   * Gets the string value from DB.
   *
   * @param query the query
   * @param parameters parameters for query
   * @return the string value from DB
   * @throws SQLException the SQL exception
   */
  public static String getStringValueFromDb(String query, 
      Object[] parameters) throws SQLException {
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(query)) {
      int idx = 1;
      for (Object parameter : parameters) {
        ps.setObject(idx++, parameter);
      }
      return DataBaseUtil.getStringValueFromDb(ps);
    }
  }

  /**
   * Gets the date value from DB.
   *
   * @param ps the ps
   * @return the date value from DB
   * @throws SQLException the SQL exception
   */
  public static Date getDateValueFromDb(PreparedStatement ps) throws SQLException {
    Date value = null;
    try (ResultSet rs = ps.executeQuery()) {
      if (rs.next()) {
        value = rs.getDate(1);
      }
    }
    return value;
  }

  /**
   * Gets the int value from DB.
   *
   * @param ps the ps
   * @return the int value from DB
   * @throws SQLException the SQL exception
   */
  public static int getIntValueFromDb(PreparedStatement ps) throws SQLException {
    int value = 0;
    try (ResultSet rs = ps.executeQuery()) {
      if (rs.next()) {
        value = rs.getInt(1);
      }
    }
    return value;
  }

  /**
   * Gets the int value from DB.
   *
   * @param query the query
   * @return the int value from DB
   * @throws SQLException the SQL exception
   */
  public static int getIntValueFromDb(String query) throws SQLException {
    try (Connection con = DataBaseUtil.getReadOnlyConnection()) {
      if (con != null) {
        try (PreparedStatement ps = con.prepareStatement(query)) {
          return DataBaseUtil.getIntValueFromDb(ps);
        }
      }
    }
    return 0;
  }

  /**
   * Gets the int value from DB.
   *
   * @param query the query
   * @param parameters parameters for query
   * @return the int value from DB
   * @throws SQLException the SQL exception
   */
  public static int getIntValueFromDb(String query, Object[] parameters) throws SQLException {
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(query)) {
      int idx = 1;
      for (Object parameter : parameters) {
        ps.setObject(idx++, parameter);
      }
      return DataBaseUtil.getIntValueFromDb(ps);
    }
  }

  /**
   * Gets the int value from DB dates.
   *
   * @param query the query
   * @param from  the from
   * @param to    the to
   * @return the int value from DB dates
   * @throws SQLException the SQL exception
   */
  public static int getIntValueFromDbDates(String query, java.sql.Date from, java.sql.Date to)
      throws SQLException {
    try (Connection con = DataBaseUtil.getReadOnlyConnection()) {
      if (con != null) {
        try (PreparedStatement ps = con.prepareStatement(query)) {
          ps.setDate(1, from);
          ps.setDate(2, to);
          return DataBaseUtil.getIntValueFromDb(ps);
        }
      }
    }
    return 0;
  }

  /**
   * Gets the big decimal value from DB.
   *
   * @param ps the ps
   * @return the big decimal value from DB
   * @throws SQLException the SQL exception
   */
  public static BigDecimal getBigDecimalValueFromDb(PreparedStatement ps) throws SQLException {
    BigDecimal value = null;
    try (ResultSet rs = ps.executeQuery()) {
      if (rs.next()) {
        value = rs.getBigDecimal(1);
      }
      return value;
    }
  }

  /**
   * Gets the big decimal value from DB.
   *
   * @param query  the query
   * @param value1 the value 1
   * @return the big decimal value from DB
   * @throws SQLException the SQL exception
   */
  public static BigDecimal getBigDecimalValueFromDb(String query, int value1) throws SQLException {
    try (Connection con = DataBaseUtil.getReadOnlyConnection()) {
      if (con != null) {
        try (PreparedStatement ps = con.prepareStatement(query)) {
          ps.setInt(1, value1);
          return DataBaseUtil.getBigDecimalValueFromDb(ps);
        }
      }
    }
    return null;
  }

  /**
   * Gets the big decimal value from DB dates.
   *
   * @param query the query
   * @param from  the from
   * @param to    the to
   * @return the big decimal value from DB dates
   * @throws SQLException the SQL exception
   */
  public static BigDecimal getBigDecimalValueFromDbDates(String query, java.sql.Date from,
      java.sql.Date to) throws SQLException {
    try (Connection con = DataBaseUtil.getReadOnlyConnection()) {
      if (con != null) {
        try (PreparedStatement ps = con.prepareStatement(query)) {
          ps.setDate(1, from);
          ps.setDate(2, to);
          return DataBaseUtil.getBigDecimalValueFromDb(ps);
        }
      }
    }
    return null;
  }

  /**
   * Gets the next sequence.
   *
   * @param sequenceName the sequence name
   * @return the next sequence
   * @throws SQLException the SQL exception
   */
  public static int getNextSequence(String sequenceName) throws SQLException {
    try (Connection con = DataBaseUtil.getReadOnlyConnection()) {
      if (con != null) {
        try (PreparedStatement ps = con.prepareStatement("SELECT nextval(?)")) {
          ps.setString(1, sequenceName);
          return getIntValueFromDb(ps);
        }
      }
    }
    return 0;
  }

  /**
   * Gets the next sequence.
   *
   * @param con          the con
   * @param sequenceName the sequence name
   * @return the next sequence
   * @throws SQLException the SQL exception
   */
  public static int getNextSequence(Connection con, String sequenceName) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement("SELECT nextval(?)");
      ps.setString(1, sequenceName);
      return getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Replace symbols in xml content.Note: don't use this: use ResponseUtils.filter available in
   * struts instead import org.apache.struts.util.ResponseUtils; escapeXml encodeXml: use
   * ResponseUtils (see HtmlConverter)
   * 
   * @author Sumathi - 05/06/2007 Description:- To return a String by replacing & with &amp; and <
   *         with &lt; and > with &gt; and " with &quot; and ' with &apos; and.
   * @param inputText the inputText
   * @return String
   */
  public static String replaceSymbolsInXmlContent(String inputText) {
    if (inputText == null) {
      return inputText;
    }
    String resultString = "";
    resultString = inputText.replaceAll("&", "&amp;");
    resultString = resultString.replaceAll("<", "&lt;");
    resultString = resultString.replaceAll(">", "&gt;");
    resultString = resultString.replaceAll("'", "&apos;");
    resultString = resultString.replaceAll("\"", "&quot;");
    return resultString;
  }

  /**
   * Date diff.
   *
   * @param startDate the start date
   * @param endDate   the end date
   * @return the string
   */
  public static String dateDiff(String startDate, String endDate) {
    // FORMAT DD-MM-YYYY
    // startDate is the current date
    // endDate is the previous date

    int sday = Integer.parseInt(startDate.substring(0, 2));
    int smonth = Integer.parseInt(startDate.substring(3, 5));
    int syear = Integer.parseInt(startDate.substring(6, 10));

    int eday = Integer.parseInt(endDate.substring(0, 2));
    int emonth = Integer.parseInt(endDate.substring(3, 5));
    int eyear = Integer.parseInt(endDate.substring(6, 10));

    Calendar calendar1 = Calendar.getInstance();
    Calendar calendar2 = Calendar.getInstance();

    calendar1.set(syear, smonth, sday);
    calendar2.set(eyear, emonth, eday);
    long diff = calendar1.getTimeInMillis() - calendar2.getTimeInMillis();
    long diffDays = diff / (24 * 60 * 60 * 1000);
    return String.valueOf(diffDays);
  }

  /**
   * Gets the current date.
   *
   * @author Sumathi - 28/06/2008 Description:- To get Current Date
   * @return String - Current Date
   */
  public static String getCurrentDate() {
    java.util.Date date = new java.util.Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String dateString = sdf.format(date);
    return dateString;
  }

  public static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
  public static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
  public static SimpleDateFormat timeFormatterSecs = new SimpleDateFormat("HH:mm:ss");
  public static SimpleDateFormat timeStampFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
  public static SimpleDateFormat timeStampFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

  /**
   * Parse a string representing a date to return a java.sql.Date object (only the date part)
   *
   * @param dateStr the date str
   * @return the java.sql. date
   * @throws ParseException the parse exception
   */
  public static java.sql.Date parseDate(String dateStr) throws java.text.ParseException {
    if ((dateStr != null) && !dateStr.equals("")) {
      java.util.Date dt = dateFormatter.parse(dateStr);
      return new java.sql.Date(dt.getTime());
    } else {
      return null;
    }
  }

  /**
   * Parse a string representing a time to return a java.sql.Time object (only the time part)
   *
   * @param timeStr the time str
   * @return the java.sql. time
   * @throws ParseException the parse exception
   */
  public static java.sql.Time parseTime(String timeStr) throws java.text.ParseException {
    if ((timeStr != null) && !timeStr.equals("")) {
      java.util.Date dateTime = timeFormatter.parse(timeStr);
      return new java.sql.Time(dateTime.getTime());
    } else {
      return null;
    }
  }

  /**
   * Parses the timestamp.
   *
   * @param timestampStr the timestamp str
   * @return the java.sql. timestamp
   * @throws ParseException the parse exception
   */
  public static java.sql.Timestamp parseTimestamp(String timestampStr) throws ParseException {
    if ((timestampStr != null) && !timestampStr.equals("")) {
      java.util.Date dateTime = timeStampFormat.parse(timestampStr);
      return new java.sql.Timestamp(dateTime.getTime());
    } else {
      return null;
    }
  }

  /**
   * Gets the value.
   *
   * @author Fazil - 31/10/2008 Description:- To get sequence number or To get sequence number with
   *         appended string or both
   * @param seqName     the seq name
   * @param appendStaus the append staus
   * @param typeNumber  the type number
   * @return String - To get sequence number or To get sequence number with appended string or both
   */
  public static String getValue(String seqName, String appendStaus, String typeNumber) {
    String value = null;
    String getPrefix = null;
    try (Connection con = DataBaseUtil.getReadOnlyConnection()) {
      if (con != null) {
        try (PreparedStatement ps = con
            .prepareStatement("SELECT PREFIX FROM" + " UNIQUE_NUMBER WHERE TYPE_NUMBER=?")) {
          ps.setString(1, typeNumber);
          getPrefix = DataBaseUtil.getStringValueFromDb(ps);
          value = String.valueOf(DataBaseUtil.getNextSequence(seqName));
          if (appendStaus.equalsIgnoreCase("Y")) {
            value = getPrefix + value;
          }
        }
      }
    } catch (SQLException ex) {
      logger.error(ex.getMessage());
    }
    return value;
  }

  private static final String GET_TIMESTAMP = "SELECT * FROM LOCALTIMESTAMP(0)";

  /**
   * Gets the dateand time.
   *
   * @return the dateand time
   */
  public static Timestamp getDateandTime() {
    Timestamp ts = null;
    try (Connection con = DataBaseUtil.getConnection()) {
      if (con != null) {
        try (PreparedStatement ps = con.prepareStatement(GET_TIMESTAMP);
            ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            ts = rs.getTimestamp(1);
          }
        }
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    return ts;
  }

  /**
   * Methods to construct a WHERE clause on the fly, depending on the supplied field name, operator
   * (or IN) and value(s). After building the query, one needs to do setString etc. on the resulting
   * Prepared Statement constructed out of this query.
   *
   * @param query the query
   * @param field the field
   * @param op    the op
   * @param value the value
   */
  public static void addWhereFieldOpValue(StringBuilder query, String field, String op,
      Object value) {
    if (value == null) {
      return;
    }

    if (query.length() > 0) {
      query.append(" AND ");
    } else {
      query.append(" WHERE ");
    }

    query.append('(');
    query.append(field).append(" ").append(op).append(" ").append('?');
    query.append(')');
  }

  /**
   * Adds the where field in list.
   *
   * @param query  the query
   * @param field  the field
   * @param values the values
   */
  public static void addWhereFieldInList(StringBuilder query, String field, java.util.List values) {
    addWhereFieldInList(query, field, values, query.length() > 0);
  }

  /**
   * Adds the where field in list.
   *
   * @param query     the query
   * @param field     the field
   * @param values    the values
   * @param appendAnd the append and
   */
  public static void addWhereFieldInList(StringBuilder query, String field, java.util.List values,
      boolean appendAnd) {
    if ((values == null) || (values.size() == 0)) {
      return;
    }

    // if the query is empty, start a WHERE, else, use AND
    if (appendAnd) {
      query.append(" AND ");
    } else {
      query.append(" WHERE ");
    }

    query.append('(');
    query.append(field).append(" IN ");
    query.append('(');
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) {
        query.append(',');
      }
      query.append('?');
    }
    query.append(')');
    query.append(')');
  }

  /**
   * Adds the not in where field in list.
   *
   * @param query     the query
   * @param field     the field
   * @param values    the values
   * @param appendAnd the append and
   */
  public static void addNotInWhereFieldInList(StringBuilder query, String field,
      java.util.List values, boolean appendAnd) {
    if ((values == null) || (values.size() == 0)) {
      return;
    }

    // if the query is empty, start a WHERE, else, use AND
    if (appendAnd) {
      query.append(" AND ");
    } else {
      query.append(" WHERE ");
    }

    query.append('(');
    query.append(field).append(" NOT IN ");
    query.append('(');
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) {
        query.append(',');
      }
      query.append('?');
    }
    query.append(')');
    query.append(')');
  }

  /**
   * Read input stream.
   *
   * @param stream the stream
   * @return the byte[]
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static byte[] readInputStream(InputStream stream) throws IOException {
    byte[] bytes = null;
    if (stream == null) {
      return null;
    } else {
      bytes = new byte[stream.available()];
      int offset = 0;
      int numRead = 0;
      while (offset < bytes.length
          && (numRead = stream.read(bytes, offset, bytes.length - offset)) >= 0) {
        offset += numRead;
      }

      // Ensure all the bytes have been read in
      if (offset < bytes.length) {
        throw new IOException("Could not completely read.");
      }
      stream.close();
    }
    return bytes;
  }

  /**
   * Returns true if given dataset (table in db) has more records than threshold.
   *
   * @param dataset the dataset
   * @return boolean
   * @throws SQLException the SQL exception
   */
  public static Boolean isLargeDataset(String dataset) throws SQLException {
    HttpSession session = RequestContext.getSession();
    BasicDynaBean bean = null;
    if (session != null) {
      String strHospitalId = (String) session.getAttribute("sesHospitalId");
      bean = DataBaseUtil.queryToDynaBean(
          "SELECT CASE WHEN n_live_tup > 500000 then 'Y' else 'N' end as is_large_dataset"
              + " FROM pg_stat_user_tables WHERE schemaname = ? AND relname = ?",
          new Object[] { strHospitalId, dataset });
    }
    return bean != null && bean.get("is_large_dataset") != null
        && ((String) bean.get("is_large_dataset")).equals("Y");
  }



  /**
   * Query to integer array list.
   * Returns an Integer ArrayList if one column with integer values
   * is to be returned.
   *
   * @param stmt the stmt
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List<Integer> queryToIntegerArrayList(PreparedStatement stmt) throws SQLException {
    ResultSet rs = null;
    List<Integer> result = new ArrayList<>();
    rs = stmt.executeQuery();
    ResultSetMetaData rsMetaData = rs.getMetaData();

    if (rsMetaData.getColumnCount() == 1) {
      while (rs.next()) {
        result.add(rs.getInt(1));
      }
    }
    return result;
  }
}
