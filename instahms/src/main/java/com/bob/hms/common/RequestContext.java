package com.bob.hms.common;

import com.insta.hms.common.InputValidator;

import org.apache.commons.lang.LocaleUtils;
import org.owasp.esapi.errors.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * The Class RequestContext.
 */
public class RequestContext {
  private static final Logger log = LoggerFactory.getLogger(RequestContext.class);

  private static final String AUTOCLOSE_DB_CONNECTIONS_KEY = "autoclose.db.connections";

  /** The max schema name length. */
  private static final Integer SCHEMA_NAME_MAX_LENGTH = 25;

  // ThreadLocal( ) is associated with the current thread.
  private static ThreadLocal localRequest = new ThreadLocal();

  private static final ThreadLocal<List<Connection>> localConnectionList = 
      new ThreadLocal<List<Connection>>() {
    @Override
    protected List<Connection> initialValue() {
      return new ArrayList<Connection>();
    }
  };

  /**
   * Sets the request.
   *
   * @param request the new request
   */
  // Set the request in the current thread.
  public static void setRequest(ServletRequest request) {
    localRequest.set(request);
  }

  /**
   * Gets the request.
   *
   * @return the request
   */
  // Get the request from the current thread.
  public static ServletRequest getRequest() {
    Object request = localRequest.get();
    if (request instanceof ServletRequest) {
      return (ServletRequest) request;
    }
    return null;
  }

  /**
   * Gets the http request.
   *
   * @return the http request
   */
  // If it is an HttpServletRequest, get the request from the current thread.
  public static HttpServletRequest getHttpRequest() {
    Object request = localRequest.get();
    if (request instanceof HttpServletRequest) {
      return (HttpServletRequest) request;
    }
    return null;
  }

  /**
   * Get the session from the HttpRequest in the current thread.
   *
   * @return the session
   */
  public static HttpSession getSession() {
    if (getHttpRequest() == null) {
      return null;
    }
    return getHttpRequest().getSession(true);
  }

  /**
   * When there is no request (running from command line, not from tomcat) we still need a request
   * context to store the schema. In this case, the schema can be saved directly here. See
   * DataBaseUtil.java for how this is used.
   *
   * @param schema the new connection details
   */
  public static void setConnectionDetails(String[] schema) {
    localRequest.set(schema);
  }

  /**
   * Gets the connection details.
   *
   * @return the connection details
   */
  public static String[] getConnectionDetails() {
    Object threadObj = localRequest.get();
    if (threadObj instanceof String[]) {
      return (String[]) threadObj;
    }
    return null;
  }

  /**
   * Gets the schema.
   *
   * @return the schema
   */
  public static String getSchema() {
    Object threadObj = localRequest.get();
    if (threadObj instanceof HttpServletRequest) {
      HttpSession session = ((HttpServletRequest) threadObj).getSession(true);
      return (String) session.getAttribute("sesHospitalId");

    } else if (threadObj instanceof String[]) {
      // this contains connection details as an array.
      String[] details = (String[]) threadObj;
      if (details.length >= 3) {
        return details[2];
      }
    }
    return null;
  }

  /**
   * Gets the user name.
   *
   * @return the user name
   */
  public static String getUserName() {
    Object threadObj = localRequest.get();
    if (threadObj instanceof HttpServletRequest) {
      HttpSession session = ((HttpServletRequest) threadObj).getSession(true);
      return (String) session.getAttribute("userId");

    } else if (threadObj instanceof String[]) {
      String[] details = (String[]) threadObj;
      if (details.length >= 4) {
        return details[3];
      }
    }
    return null;
  }

  /**
   * Gets the center id.
   *
   * @return the center id
   */
  public static Integer getCenterId() {
    Object threadObj = localRequest.get();
    if (threadObj instanceof HttpServletRequest) {
      HttpSession session = ((HttpServletRequest) threadObj).getSession(true);
      return (Integer) session.getAttribute("centerId");

    } else if (threadObj instanceof String[]) {
      String[] details = (String[]) threadObj;
      if (details.length >= 5  && !details[4].isEmpty()) {
        return Integer.parseInt(details[4]);
      }
    }
    return null;
  }

  /**
   * Gets the center name.
   *
   * @return the center name
   */
  public static String getCenterName() {
    Object threadObj = localRequest.get();
    if (threadObj instanceof HttpServletRequest) {
      HttpSession session = ((HttpServletRequest) threadObj).getSession(true);
      return (String) session.getAttribute("centerName");

    } else if (threadObj instanceof String[]) {
      String[] details = (String[]) threadObj;
      if (details.length >= 6) {
        return details[5];
      }
    }
    return null;
  }

  /**
   * Gets the role id.
   *
   * @return the role id
   */
  public static Integer getRoleId() {
    Object threadObj = localRequest.get();
    if (threadObj instanceof HttpServletRequest) {
      HttpSession session = ((HttpServletRequest) threadObj).getSession(true);
      return (Integer) session.getAttribute("roleId");

    } else if (threadObj instanceof String[]) {
      String[] details = (String[]) threadObj;
      if (details.length > 6 && !details[6].isEmpty()) {
        return Integer.parseInt(details[6]);
      }
    }
    return null;
  }

  /**
   * Gets the locale.
   *
   * @return the locale
   */
  public static Locale getLocale() {
    Object threadObj = localRequest.get();
    if (threadObj instanceof HttpServletRequest) {
      HttpSession session = ((HttpServletRequest) threadObj).getSession(true);
      return RequestContext.getRequest().getLocale();

    } else if (threadObj instanceof String[]) {
      String[] details = (String[]) threadObj;
      if (details.length >= 7) {
        return LocaleUtils.toLocale(details[7]);
      }
    }
    return null;
  }

  /**
   * Gets the database port.
   *
   * @return the database port
   */
  public static Integer getDatabasePort() {
    Object threadObj = localRequest.get();
    if (threadObj instanceof String[]) {
      String[] details = (String[]) threadObj;
      if (details.length > 8) {
        return Integer.parseInt(details[8]);
      }
    }
    return 5432;
  }

  /**
   * Add a connection to a tracking list of connections for automatic cleanup of abandoned
   * connections. The list is a thead local, which means that the connections are tracked per
   * thread.
   * 
   * @param con - Connection to be tracked
   * @return int - No of connections in the list after this addition.
   */
  public static int addConnection(Connection con) {
    List<Connection> connectionList = localConnectionList.get();
    if (null != con && !connectionList.contains(con)) {
      connectionList.add(con);
      return connectionList.size();
    }
    return 0;
  }

  /**
   * Closes all the connections that were created by the calling thread and added to the tracking
   * list, if they are not correctly closed by the method / action that created it. All the
   * connections will be removed from the tracking list as well, irrespective of whether the close
   * is successful or not. This method will be called by RequestContextFilter when request
   * processing is complete. Typically this method should not be called by other classes in the
   * application, this method is __NOT__ a substitute for connection.close()
   */

  public static void cleanupConnections() {
    List<Connection> connectionList = localConnectionList.get();
    if (isAutoCloseEnabled()) {
      for (Connection con : connectionList) {
        try {
          if (!con.isClosed()) {
            HttpServletRequest req = getHttpRequest();
            log.error("Abandoned Connection detected while processing the request :"
                + ((null != req) ? (req.getRequestURI() + "?" + req.getQueryString()) : ""));
            con.close();
          }
        } catch (SQLException sqle) {
          log.error("Error cleaning up the connections" + sqle.getMessage());
        }
      }
    }
    connectionList.clear();
    localConnectionList.remove();
    localRequest.remove();
    return;
  }

  /**
   * Checks if is auto close enabled.
   *
   * @return the boolean
   */
  private static Boolean isAutoCloseEnabled() {
    // Boolean closeConnections = true;
    // MessageResources resource =
    // MessageResources.getMessageResources("java.resources.application");
    //
    // if (null != resource) {
    // String autoClose = resource.getMessage(AUTOCLOSE_DB_CONNECTIONS_KEY);
    // if (null != autoClose) {
    // closeConnections = Boolean.valueOf(autoClose);
    // log.debug("Loading autoclose setting from resource file :" + autoClose);
    // }
    // }
    // log.debug("Autoclose setting : " + closeConnections);
    // return closeConnections;
    return true;
  }

  /**
   * Gets the sanitized schema name.
   *
   * @return the sanitized schema name
   */
  public static String getSanitizedSchemaName() {

    String sanitizedSchema = RequestContext.getSchema();
    if (sanitizedSchema == null) {
      return sanitizedSchema;
    }
    try {
      sanitizedSchema = InputValidator.getSafeSchemaString("hospital", sanitizedSchema.trim(),
          SCHEMA_NAME_MAX_LENGTH, false);
    } catch (ValidationException exception) {
      log.error(exception.getMessage());
    }
    return sanitizedSchema;
  }

}
