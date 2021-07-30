package com.insta.hms.common;

import com.bob.hms.common.RequestContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The Class TenantAwareProxy to set schema dynamically for a multi-tenant environment.
 * 
 * @author tanmay.k
 */
public class TenantAwareProxy extends TransactionAwareDataSourceProxy {

  /** The logger. */
  private final Logger logger = LoggerFactory.getLogger(TenantAwareProxy.class);

  /** The timeout. */
  private Integer timeout;

  /** The set schema query. */
  private static final String SET_SCHEMA_QUERY = "SET search_path to '%s';";

  /** The set timeout query. */
  private static final String SET_TIMEOUT_QUERY = "SET statement_timeout = %d;";

  /** The application user executing the query. */
  private static final String SET_APPLICATION_USER = "SET application.username = '%s';";

  /**
   * Sets the timeout.
   *
   * @param timeout the new timeout
   */
  public void setTimeout(Integer timeout) {
    this.timeout = timeout;
  }

  /*
   * TODO - Add try/catch for catching SQL exception and return a new Runtime Exception on for wrong
   * schema name. Create a new Exception. (non-Javadoc)
   * 
   * @see org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy#getConnection()
   */
  @Override
  public Connection getConnection() throws SQLException {
    Connection connection = super.getConnection();
    Statement statement = connection.createStatement();
    String query;
    String schema = RequestContext.getSanitizedSchemaName();
    String username = RequestContext.getUserName();
    if (null != schema && !(schema.isEmpty())) {
      logger.debug("Setting search path to " + schema);
      schema = schema + "', 'extensions";
      query = String.format(SET_SCHEMA_QUERY, schema);
      if (null != this.timeout && this.timeout > 0) {
        logger.debug("Setting statement timeout to " + this.timeout);
        query = query.concat(" ").concat(String.format(SET_TIMEOUT_QUERY, this.timeout));
      }
      if (!StringUtil.isNullOrEmpty(username)) {
        logger.debug("Setting application username to " + username);
        query = query.concat(" ").concat(String.format(SET_APPLICATION_USER, username));
      }
    } else {
      logger.error("Setting empty schema, lost schema in application. "
          + "Current running transaction will fail");
      query = String.format(SET_SCHEMA_QUERY, " ");
    }
    try {
      statement.execute(query);
    } finally {
      statement.close();
    }
    return connection;

  }
}
