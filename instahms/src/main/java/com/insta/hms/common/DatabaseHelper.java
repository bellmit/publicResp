package com.insta.hms.common;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.utils.EnvironmentUtil;

import org.apache.commons.beanutils.BasicDynaBean;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.StringUtils;

import java.io.Writer;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

// TODO: clean-up
/**
 * The Utility Class DatabaseHelper which contains the methods used for operations on the database.
 * 
 * @author tanmay.k
 */
public class DatabaseHelper {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(DatabaseHelper.class);

  /** The jdbc template. */
  private static JdbcTemplate jdbcTemplate;

  /** The named jdbc template. */
  private static NamedParameterJdbcTemplate namedJdbcTemplate;

  /** The transaction manager. */
  private static DataSourceTransactionManager txManager;

  /** The Constant GENERATE_NEXT_ID_QUERY. */
  private static final String GENERATE_NEXT_ID_QUERY = " SELECT generate_id(?)";

  /** The Constant GENERATE_NEXT_SEQUENCE_QUERY. */
  private static final String GENERATE_NEXT_SEQUENCE_QUERY = " SELECT nextval(?)";

  /** The Constant GET_COLUMNS_QUERY. */
  private static String GET_COLUMNS_QUERY = "SELECT column_name,data_type FROM "
      + " information_schema.columns WHERE table_schema=(select current_schema()) AND "
      + " table_name = ? AND column_name NOT LIKE '%_obsolete' AND column_name NOT LIKE "
      + " 'obsolete_%'";

  /** The Constant ALL_SCHEMAS_QUERY. */
  private static final String ALL_SCHEMAS_QUERY = "SELECT nspname as schema FROM "
      + " pg_catalog.pg_namespace WHERE nspname NOT LIKE 'pg_%' AND nspname NOT LIKE '%_temp%' "
      + " AND nspname NOT IN ('public', 'information_schema', 'extensions')";

  /**
   * Sets the data source. The data source is injected from the spring-servlet.xml file.
   *
   * @param dataSource the new data source
   */
  public static void setDataSource(DataSource dataSource) {
    jdbcTemplate = new JdbcTemplate(dataSource);
    namedJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
  }

  /**
   * Sets the transaction manager.
   *
   * @param transactionManager the new transaction manager
   */
  public static void setTransactionManager(DataSourceTransactionManager transactionManager) {
    txManager = transactionManager;
  }

  /**
   * Initialize DB connection.
   */
  /*
   * This function is essentially the boilerplate code required to query the database. This can be
   * invoked from either the application's context or from scheduled jobs. If from scheduled jobs,
   * the session from RequestContext would be null and the schema name would be retrievable
   * 
   * TODO - Refactor to have one call for setting schema and timeout by creating a method
   * getSchemaName which has the session as an input parameter and returns the appropriate schema
   * name. Need more clarity/understanding on the use case.
   */
  public static void initializeDBConnection() {
    initializeDBConnection(EnvironmentUtil.getDatabaseQueryTimeout());
  }

  /**
   * Initialize DB connection.
   *
   * @param timeout the timeout
   */
  public static void initializeDBConnection(Integer timeout) {
    HttpSession session = RequestContext.getSession();
    if (null != session) {
      String schemaName = (String) session.getAttribute("sesHospitalId");
      if (null != schemaName && !(schemaName.isEmpty())) {
        setSchemaAndQueryTimeout(schemaName, timeout);
      } else {
        // TODO - Internationalization?
        session.setAttribute("sesErr", "Session Expired, Please Login Again");
        logger.warn("session value for Hospital Id Expired");
      }
    } else {
      String[] connectionDetails = RequestContext.getConnectionDetails();
      if (null != connectionDetails) {
        String databaseName = connectionDetails[1];
        String schemaName = connectionDetails[2];

        if (null == databaseName || "".equals(databaseName)) {
          if (null != schemaName && !(schemaName.isEmpty())) {
            setSchemaAndQueryTimeout(schemaName, timeout);
          } else {
            logger.error("Schema Name from RequestContext returned null or empty string.");
          }
        } else {
          logger.error("No any DB support for external databse connection");
        }
      }
    }
  }

  /**
   * Sets the schema and query timeout.
   *
   * @param schemaName the schema name
   * @param timeout    the query timeout
   */
  public static void setSchemaAndQueryTimeout(String schemaName, Integer timeout) {
    TenantAwareProxy dataSource = (TenantAwareProxy) jdbcTemplate.getDataSource();
    dataSource.setTimeout(timeout * 1000);
  }

  /*
   * These functions return a List of BasicDynaBeans as the result set for the query to be executed.
   * Uses RowToBasicDynaBeanMapper to do the transformation from ResultSet to List of BasicDynaBean.
   */

  /**
   * Query to dyna list.
   *
   * @param query the query
   * @return the List of BasicDynaBeans
   */
  public static List<BasicDynaBean> queryToDynaList(String query) {
    return queryToDynaList(query, EnvironmentUtil.getDatabaseQueryTimeout());
  }

  /**
   * Query to dyna list.
   *
   * @param query   the query
   * @param timeout the query timeout
   * @return the List of BasicDynaBeans
   */
  public static List<BasicDynaBean> queryToDynaList(String query, Integer timeout) {
    initializeDBConnection(timeout);
    return jdbcTemplate.query(query, new RowToBasicDynaBeanMapper());
  }

  /**
   * Query to dyna list.
   *
   * @param query  the query
   * @param values the arguements of the query
   * @return the List of BasicDynaBeans
   */
  public static List<BasicDynaBean> queryToDynaList(String query, Object... values) {
    return queryToDynaList(query, EnvironmentUtil.getDatabaseQueryTimeout(), values);
  }

  /**
   * Query to dyna list.
   *
   * @param query   the query
   * @param timeout the timeout
   * @param values  the arguments of the query
   * @return the List of BasicDynaBeans
   */
  public static List<BasicDynaBean> queryToDynaList(String query, Integer timeout,
      Object... values) {
    initializeDBConnection(timeout);
    return jdbcTemplate.query(query, values, new RowToBasicDynaBeanMapper());
  }

  /**
   * Query to dyna list using NamedJDBCTemplate.
   *
   * @param query      the query
   * @param parameters the parameters replacing the named placeholders in the query
   * @return the List of BasicDynaBean
   */
  public static List<BasicDynaBean> queryToDynaList(String query,
      MapSqlParameterSource parameters) {
    initializeDBConnection();
    return namedJdbcTemplate.query(query, parameters, new RowToBasicDynaBeanMapper());
  }

  /**
   * Query to dyna list with case.
   *
   * @param query  the query
   * @param values the values
   * @return the list
   */
  public static List<BasicDynaBean> queryToDynaListWithCase(String query, Object... values) {
    return queryToDynaListWithCase(query, EnvironmentUtil.getDatabaseQueryTimeout(), values);
  }

  /**
   * Query to dyna list with case.
   *
   * @param query   the query
   * @param timeout the timeout
   * @param values  the values
   * @return the list
   */
  // TODO - Clean this up.
  public static List<BasicDynaBean> queryToDynaListWithCase(String query, Integer timeout,
      Object... values) {
    initializeDBConnection(timeout);
    return jdbcTemplate.query(query, values, new CaseSensitiveRowToBasicDynaBeanMapper());
  }

  /*
   * These functions returns a DynaBean for the row queried.
   *
   * 
   * /** Query to dyna bean.
   *
   * @param query the query
   * 
   * @return the BasicDynaBean
   */

  /**
   * Query to dyna bean.
   *
   * @param query the query
   * @return the basic dyna bean
   */
  public static BasicDynaBean queryToDynaBean(String query) {
    return queryToDynaBean(query, EnvironmentUtil.getDatabaseQueryTimeout());
  }

  /**
   * Query to dyna bean.
   *
   * @param query   the query
   * @param timeout the query timeout
   * @return the BasicDynaBean
   */
  public static BasicDynaBean queryToDynaBean(String query, Integer timeout) {
    initializeDBConnection(timeout);
    List<BasicDynaBean> dynaList = queryToDynaList(query);

    if (dynaList != null && !dynaList.isEmpty()) {
      return dynaList.get(0);
    }
    return null;
  }

  /**
   * Query to dyna bean.
   *
   * @param query  the query
   * @param values the arguments of the query
   * @return the BasicDynaBean
   */
  public static BasicDynaBean queryToDynaBean(String query, Object... values) {
    return queryToDynaBean(query, EnvironmentUtil.getDatabaseQueryTimeout(), values);
  }
  
  /**
   * Query to dyna Bean using NamedJDBCTemplate.
   *
   * @param query      the query
   * @param parameters the parameters replacing the named placeholders in the query
   * @return the BasicDynaBean
   */
  public static BasicDynaBean queryToDynaBean(String query, MapSqlParameterSource parameters) {
    initializeDBConnection();
    List<BasicDynaBean> dynaList = queryToDynaList(query, parameters);

    if (dynaList != null && !dynaList.isEmpty()) {
      return dynaList.get(0);
    }
    return null;
  }

  /**
   * Query to dyna bean.
   *
   * @param query   the query
   * @param timeout the query timeout
   * @param values  the arguments of the query
   * @return the BasicDynaBean
   */
  public static BasicDynaBean queryToDynaBean(String query, Integer timeout, Object... values) {
    initializeDBConnection(timeout);
    List<BasicDynaBean> dynaList = queryToDynaList(query, values);

    if (dynaList != null && !dynaList.isEmpty()) {
      return dynaList.get(0);
    }
    return null;
  }

  /**
   * Query to json stream.
   *
   * @param query  the query
   * @param values the values
   * @param stream the stream
   */
  public static void queryToJsonStream(String query, Object[] values, Writer stream) {
    queryToJsonStream(query, EnvironmentUtil.getDatabaseQueryTimeout(), values, stream);
  }

  /**
   * Query to json stream.
   *
   * @param query   the query
   * @param timeout the timeout
   * @param values  the values
   * @param stream  the stream
   */

  public static void queryToJsonStream(String query, Integer timeout, Object[] values,
      Writer stream) {
    initializeDBConnection(timeout);
    getJBDCTemplate().setFetchSize(1000);
    ResultSetExtractor<Writer> rse = new ResultSetToJsonStreamExtractor(stream);
    queryWithCustomMapper(query, values, rse);
  }

  /**
   * Gets the next pattern id from database.
   *
   * @param patternId the pattern id
   * @return the next pattern id
   */
  public static String getNextPatternId(String patternId) {
    initializeDBConnection();
    return getString(GENERATE_NEXT_ID_QUERY, patternId);
  }

  /**
   * Gets the next sequence.
   *
   * @param tableName the table name
   * @return the next sequence
   */
  public static Integer getNextSequence(String tableName) {
    return getInteger(GENERATE_NEXT_SEQUENCE_QUERY, tableName.concat("_seq"));
  }

  /**
   * Gets the column names of the table concerned.
   *
   * @param tableName the table name
   * @return the columns of the table
   */
  public static List<BasicDynaBean> getColumns(String tableName) {
    return queryToDynaList(GET_COLUMNS_QUERY, tableName);
  }

  /**
   * Gets all the schemas.
   *
   * @return List of BasicDynaBeans with all the schemas.
   */
  public static List<BasicDynaBean> getAllSchemas() {
    return jdbcTemplate.query(ALL_SCHEMAS_QUERY, new RowToBasicDynaBeanMapper());
  }

  /**
   * Gets the single value.
   *
   * @param        <T> the generic type
   * @param query  the query
   * @param clazz  the clazz
   * @param values the values
   * @return the single value
   */
  public static <T> T getSingleValue(String query, Class<T> clazz, Object... values) {
    try {
      return jdbcTemplate.queryForObject(query, clazz, values);
    } catch (EmptyResultDataAccessException exception) {
      logger.debug("Database returned empty row for query: " + query);
      return null;
    }
  }

  /**
   * Gets the single-valued result as a string from the database.
   *
   * @param query the query
   * @return the string result
   */
  public static String getString(String query) {
    return getString(query, new Object[] {});
  }

  /**
   * Gets the string.
   *
   * @param query  the query
   * @param values the values
   * @return the string
   */
  public static String getString(String query, Object... values) {
    initializeDBConnection();
    String result = getSingleValue(query, String.class, values);
    return null != result ? result : "";
  }
  
  /**
   * Gets the boolean.
   * 
   * @param query the query
   * @param values the values
   * @return the boolean
   */
  public static Boolean getBoolean(String query, Object... values) {
    initializeDBConnection();
    Boolean result = getSingleValue(query, Boolean.class, values);
    return null != result ? result : false;
  }

  /**
   * Gets the single-valued result as an integer from the database.
   *
   * @param query the query
   * @return the integer result
   */
  public static Integer getInteger(String query) {
    return getInteger(query, new Object[] {});
  }

  /**
   * Gets the integer.
   *
   * @param query  the query
   * @param values the values
   * @return the integer
   */
  public static Integer getInteger(String query, Object... values) {
    initializeDBConnection();
    return getSingleValue(query, Integer.class, values);
  }

  /**
   * Gets the integer using named parameter source so that array and lists can be passed to the
   * query.
   *
   * @param query           the query
   * @param parameterSource the parameter source
   * @return the integer
   */
  public static Integer getInteger(String query, SqlParameterSource parameterSource) {
    initializeDBConnection();
    return namedJdbcTemplate.queryForObject(query, parameterSource, Integer.class);
  }

  /**
   * Gets the single-valued result as an integer from the database.
   *
   * @param query the query
   * @return the long result
   */
  public static Long getLong(String query) {
    return getLong(query, new Object[] {});
  }

  /**
   * Gets the Long.
   *
   * @param query  the query
   * @param values the values
   * @return the Long
   */
  public static Long getLong(String query, Object... values) {
    initializeDBConnection();
    return getSingleValue(query, Long.class, values);
  }

  /**
   * Gets the Long using named parameter source so that array and lists can be passed to the
   * query.
   *
   * @param query           the query
   * @param parameterSource the parameter source
   * @return the Long
   */
  public static Long getLong(String query, SqlParameterSource parameterSource) {
    initializeDBConnection();
    return namedJdbcTemplate.queryForObject(query, parameterSource, Long.class);
  }

  /**
   * Gets the single-valued result as a BigDecimal from the database.
   *
   * @param query  the query
   * @param values the values
   * @return the big decimal result
   */
  public static BigDecimal getBigDecimal(String query, Object... values) {
    initializeDBConnection();
    return getSingleValue(query, BigDecimal.class, values);
  }

  /**
   * Insert operation on the database.
   *
   * @param query  the query
   * @param values the query arguments
   * @return the number of rows affected
   */
  public static Integer insert(String query, Object... values) {
    return insert(query, EnvironmentUtil.getDatabaseQueryTimeout(), values);
  }

  /**
   * Insert operation on the database.
   *
   * @param query   the query
   * @param timeout the query timeout
   * @param values  the query arguments
   * @return the number of rows affected
   */
  public static Integer insert(String query, Integer timeout, Object... values) {
    initializeDBConnection(timeout);
    return jdbcTemplate.update(query, values);
  }

  /**
   * Insert.
   *
   * @param query      the query
   * @param parameters the parameters
   * @return the integer
   */
  public static Integer insert(String query, MapSqlParameterSource parameters) {
    initializeDBConnection();
    return namedJdbcTemplate.update(query, parameters);
  }

  /**
   * Insert operation on the database.
   *
   * @param query  the query
   * @param values the query arguments
   * @return the number of rows affected
   */
  public static int[] batchInsert(String query, List<Object[]> values) {
    return batchInsert(query, EnvironmentUtil.getDatabaseQueryTimeout(), values);
  }

  /**
   * Insert operation on the database.
   *
   * @param query   the query
   * @param timeout the query timeout
   * @param values  the query arguments
   * @return the array of integers with number of rows affected for each statement
   */
  public static int[] batchInsert(String query, Integer timeout, List<Object[]> values) {
    initializeDBConnection(timeout);
    return jdbcTemplate.batchUpdate(query, values);
  }

  /**
   * Batch insert.
   *
   * @param query           the query
   * @param batchParameters the batch parameters
   * @return the int[]
   */
  public static int[] batchInsert(String query, SqlParameterSource[] batchParameters) {
    initializeDBConnection();
    return namedJdbcTemplate.batchUpdate(query, batchParameters);
  }

  /**
   * Update operation on the database.
   *
   * @param query  the query
   * @param values the query arguments
   * @return the integer
   */
  public static Integer update(String query, Object... values) {
    return update(query, EnvironmentUtil.getDatabaseQueryTimeout(), values);
  }

  /**
   * Update operation on the database when only query is specified, no other parameter.
   *
   * @param query   the query
   * @param timeout the timeout
   * @param values  the query arguments
   * @return the integer
   */
  public static Integer update(String query, Integer timeout, Object... values) {
    initializeDBConnection(timeout);
    return jdbcTemplate.update(query, values);
  }

  /**
   * Update operation on the database.
   *
   * @param query the query
   * @return the integer
   */
  public static Integer update(String query) {
    initializeDBConnection();
    return jdbcTemplate.update(query);
  }

  /**
   * Update.
   *
   * @param query      the query
   * @param parameters the parameters
   * @return the integer
   */
  public static Integer update(String query, MapSqlParameterSource parameters) {
    initializeDBConnection();
    return namedJdbcTemplate.update(query, parameters);
  }

  /**
   * Batch Update.
   *
   * @param query  the query
   * @param values the query arguments
   * @return the number of rows affected
   */
  public static int[] batchUpdate(String query, List<Object[]> values) {
    return batchUpdate(query, EnvironmentUtil.getDatabaseQueryTimeout(), values);
  }

  /**
   * Batch update.
   *
   * @param query  the query
   * @param values the values
   * @param type   the sql type
   * @return the int[]
   */
  public static int[] batchUpdate(String query, List<Object[]> values, int[] type) {
    initializeDBConnection();
    return jdbcTemplate.batchUpdate(query, values, type);
  }

  /**
   * Batch Update.
   *
   * @param query   the query
   * @param timeout the timeout
   * @param values  the query arguments
   * @return the number of rows affected
   */
  public static int[] batchUpdate(String query, Integer timeout, List<Object[]> values) {
    initializeDBConnection(timeout);
    return jdbcTemplate.batchUpdate(query, values);
  }

  /**
   * Batch Update which will run the batches by smaller units(batchSize).
   *
   * @param query     the query
   * @param batchSize the batch size
   * @param list      the l
   * @return the int[][]
   */
  public static int[][] batchUpdate(String query, final int batchSize, List<Object[]> list) {
    initializeDBConnection();
    return jdbcTemplate.batchUpdate(query, list, batchSize,
        new ParameterizedPreparedStatementSetter<Object[]>() {

          @Override
          public void setValues(PreparedStatement ps, Object[] objs) throws SQLException {
            int count = 1;
            for (Object o : objs) {
              ps.setObject(count++, o);
            }

          }
        });
  }

  /**
   * Batch update.
   *
   * @param query           the query
   * @param batchParameters the batch parameters
   * @return the int[]
   */
  public static int[] batchUpdate(String query, SqlParameterSource[] batchParameters) {
    initializeDBConnection();
    return namedJdbcTemplate.batchUpdate(query, batchParameters);
  }

  /**
   * Delete operation on the database.
   *
   * @param query the query
   * @return the integer
   */

  public static Integer delete(String query) {
    initializeDBConnection();
    return jdbcTemplate.update(query);
  }

  /**
   * Delete.
   *
   * @param query  the query
   * @param values the values
   * @return the integer
   */
  public static Integer delete(String query, Object... values) {
    return delete(query, EnvironmentUtil.getDatabaseQueryTimeout(), values);
  }

  /**
   * Delete operation on the database.
   *
   * @param query   the query
   * @param timeout the timeout
   * @param values  the query arguments
   * @return the integer
   */
  public static Integer delete(String query, Integer timeout, Object... values) {
    initializeDBConnection(timeout);
    return jdbcTemplate.update(query, values);
  }

  /**
   * Batch delete.
   *
   * @param query           the query
   * @param batchParameters the batch parameters
   * @param batchTypes      the batch types
   * @return the number of rows affected
   */
  public static int[] batchDelete(String query, List<Object[]> batchParameters, int[] batchTypes) {
    initializeDBConnection();
    return jdbcTemplate.batchUpdate(query, batchParameters, batchTypes);
  }

  /**
   * Batch delete.
   *
   * @param query           the query
   * @param batchParameters the batch parameters
   * @return the number of rows affected
   */
  public static int[] batchDelete(String query, List<Object[]> batchParameters) {
    initializeDBConnection();
    return jdbcTemplate.batchUpdate(query, batchParameters);
  }

  /**
   * Quote ident.
   *
   * @param ident the ident
   * @return the string
   */
  /*
   * TODO - Check this out. Returns a table name / column name duly quoted or cleaned up for safe
   * use within statements constructed by appending strings. For identifiers,
   * PreparedStatement.setObject cannot be used, so we need to append these within the query itself,
   * making them vulnerable to SQL Injections unless these are cleansed/escaped correctly.
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
    String stringToBeIdented = ident;
    if (!forceQuotes) {
      /*
       * We allow alias.fieldname or fieldname to go through unquoted. alias has to start with
       * a-zA-Z_ and can contain a-zA-Z_0-9. Same for fieldname.
       */
      needsQuotes = !stringToBeIdented.matches("([a-zA-Z_]\\w*\\.)?[a-zA-Z_]\\w*");
    }
    if (needsQuotes) {
      // TODO: to be technically correct, we need to quote the alias and
      // fieldname independently
      // but it is unlikely that we actually have special characters in
      // our field names.

      // replace any double-quotes with double double-quotes
      stringToBeIdented = stringToBeIdented.replace("\"", "\"\"");
      // add double quotes around the identifier to make it safe
      stringToBeIdented = "\"" + stringToBeIdented + "\"";
    }
    return stringToBeIdented;
  }

  /**
   * Start transaction.
   *
   * @param transactionName the transaction name
   * @return the transaction status
   */
  public static TransactionStatus startTransaction(String transactionName) {
    return startTransaction(transactionName, TransactionDefinition.PROPAGATION_REQUIRED);
  }

  /**
   * Start transaction.
   *
   * @param transactionName   the transaction name
   * @param propagationStatus the propagation status
   * @return the transaction status
   */
  public static TransactionStatus startTransaction(String transactionName,
      Integer propagationStatus) {
    DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
    txDefinition.setName(transactionName);
    txDefinition.setPropagationBehavior(propagationStatus);
    return txManager.getTransaction(txDefinition);

  }

  /**
   * Rollback.
   *
   * @param txStatus the tx status
   * @return true, if successful
   */
  public static boolean rollback(TransactionStatus txStatus) {
    try {
      txManager.rollback(txStatus);
    } catch (TransactionException exception) {
      logger.error("Rollback failed :" + exception);
      return false;
    }

    return true;
  }

  /**
   * Commit.
   *
   * @param txStatus the tx status
   * @return true, if successful
   */
  public static boolean commit(TransactionStatus txStatus) {
    try {
      txManager.commit(txStatus);
    } catch (TransactionException exception) {
      logger.error("Commit failed :" + exception);
      return false;
    }
    return true;
  }

  /** The Constant ALTER_TRIGGER_QUERY. */
  private static final String ALTER_TRIGGER_QUERY = "ALTER TABLE %s %s TRIGGER %s";

  /**
   * Enable trigger.
   *
   * @param triggerName the trigger name
   * @param tableName   the table name
   */
  public static void enableTrigger(String triggerName, String tableName) {
    initializeDBConnection();
    String enableQuery = String.format(ALTER_TRIGGER_QUERY, tableName, "ENABLE", triggerName);
    jdbcTemplate.execute(enableQuery);
  }

  /**
   * Disable trigger.
   *
   * @param triggerName the trigger name
   * @param tableName   the table name
   */
  public static void disableTrigger(String triggerName, String tableName) {
    initializeDBConnection();
    String disableQuery = String.format(ALTER_TRIGGER_QUERY, tableName, "DISABLE", triggerName);
    jdbcTemplate.execute(disableQuery);
  }

  /**
   * Query with custom mapper.
   *
   * @param       <T> the generic type
   * @param query the query
   * @param rse   the ResultSetExtractor
   * @return the result of type T
   */
  public static <T> T queryWithCustomMapper(String query, ResultSetExtractor<T> rse) {
    return queryWithCustomMapper(query, null, rse);
  }

  /**
   * Query with custom mapper.
   *
   * @param        <T> the generic type
   * @param query  the query
   * @param values the values
   * @param rse    the rse
   * @return the result of type T
   */
  public static <T> T queryWithCustomMapper(String query, Object[] values,
      ResultSetExtractor<T> rse) {
    initializeDBConnection();
    return jdbcTemplate.query(query, values, rse);
  }

  /** The Constant CREATE_SEQUENCE_QUERY. */
  private static final String CREATE_SEQUENCE_QUERY = "CREATE sequence %s";

  /** The Constant SEQUENCE_SUFFIX. */
  private static final String SEQUENCE_SUFFIX = "_seq";

  /** The Constant SEQUENCE_NAME_MAX_LENGTH. */
  private static final Integer SEQUENCE_NAME_MAX_LENGTH = 25;

  /** The Constant SCHEMA_QUERY. */
  private static final String SCHEMA_QUERY = "SELECT nspname as schema FROM "
      + " pg_catalog.pg_namespace WHERE nspname NOT LIKE 'pg_%' AND nspname NOT LIKE '%_temp%' "
      + " AND nspname NOT IN ('public', 'information_schema', 'extensions') AND nspname = ?";

  /**
   * Creates the sequence.
   *
   * @param sequenceName the sequence name
   * @return the boolean
   */
  public static Boolean createSequence(String sequenceName) {
    initializeDBConnection();

    if (null == sequenceName || sequenceName.isEmpty()) {
      return false;
    }

    try {
      String sequenceNameSanitized = InputValidator
          .getSafeSpecialString("sequence_name", sequenceName, SEQUENCE_NAME_MAX_LENGTH, false)
          .trim();
      String sequenceNameSuffixed = sequenceNameSanitized.toLowerCase().endsWith(SEQUENCE_SUFFIX)
          ? sequenceNameSanitized
          : sequenceNameSanitized.concat(SEQUENCE_SUFFIX);

      jdbcTemplate.execute(String.format(CREATE_SEQUENCE_QUERY, sequenceNameSuffixed));
    } catch (ValidationException exception) {
      throw new RuntimeException(exception);
    } catch (IntrusionException exception) {
      throw new RuntimeException(exception);
    }

    return true;
  }

  /**
   * Gets the JBDC template.
   *
   * @return the JBDC template
   */
  public static JdbcTemplate getJBDCTemplate() {
    initializeDBConnection();
    return jdbcTemplate;
  }
  
  /**
   * Check schema exists or not.
   * 
   * @param schema the schema to verify
   * @return the boolean
   */
  public static boolean checkSchema(String schema) {
    String dbResult = DatabaseHelper.getString(SCHEMA_QUERY, schema);
    return !dbResult.isEmpty();
  }
  
  /**
   * get an query construct for IN SQL operator.
   * @param count number of placeholder required
   * @return String for placeholder
   */
  public static String getInOperatorPlaceholder(int count) {
    String[] valuesArr = new String[count];
    Arrays.fill(valuesArr, "?");
    return "(" + StringUtils.arrayToCommaDelimitedString(valuesArr) + ")";
  }

  /**
   * Insert row and return ID.
   * @param query for the query
   * @param parameters for the parameters in the query
   * @param columnNames return value of the column
   * @return String for placeholder
   */
  public static Integer insertAndReturnRowID(String query, 
      SqlParameterSource parameters, String[] columnNames) {
    if ( query == null || parameters == null 
            || columnNames == null ) {
      return -1; 
    }
    try {
      KeyHolder keyHolder = new GeneratedKeyHolder();
      DatabaseHelper.initializeDBConnection();
      namedJdbcTemplate.update(query, parameters,keyHolder, 
              columnNames );
      return (Integer) keyHolder.getKey().intValue();
    } catch (Exception ex) { 
      return -1;
    }  
  }
}
