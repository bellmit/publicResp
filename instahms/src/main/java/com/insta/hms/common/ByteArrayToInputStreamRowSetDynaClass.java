package com.insta.hms.common;

import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.RowSetDynaClass;

import java.io.InputStream;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * The Class ByteArrayToInputStreamRowSetDynaClass wrapper around RowSetDynaClass which is done to
 * convert bytea columns to InputStream since loading bytea in memory is resource intensive.
 * 
 * @author tanmay.k
 */
public class ByteArrayToInputStreamRowSetDynaClass extends RowSetDynaClass {

  /**
   * Instantiates a new byte array to input stream row set dyna class.
   *
   * @param resultSet the result set
   * @throws SQLException the SQL exception
   */
  public ByteArrayToInputStreamRowSetDynaClass(ResultSet resultSet) throws SQLException {
    super(resultSet);
  }

  /**
   * Instantiates a new byte array to input stream row set dyna class.
   *
   * @param resultSet the result set
   * @param lowerCase the lower case
   * @throws SQLException the SQL exception
   */
  public ByteArrayToInputStreamRowSetDynaClass(ResultSet resultSet, boolean lowerCase)
      throws SQLException {
    super(resultSet, lowerCase);
  }

  /**
   * Instantiates a new byte array to input stream row set dyna class.
   *
   * @param resultSet the result set
   * @param lowerCase the lower case
   * @param limit     the limit
   * @throws SQLException the SQL exception
   */
  public ByteArrayToInputStreamRowSetDynaClass(ResultSet resultSet, boolean lowerCase, int limit)
      throws SQLException {
    super(resultSet, lowerCase, limit);
  }

  /**
   * Instantiates a new byte array to input stream row set dyna class.
   *
   * @param resultSet the result set
   * @param limit     the limit
   * @throws SQLException the SQL exception
   */
  public ByteArrayToInputStreamRowSetDynaClass(ResultSet resultSet, int limit) throws SQLException {
    super(resultSet, limit);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.beanutils.JDBCDynaClass#createDynaProperty(java.sql.ResultSetMetaData,
   * int)
   */
  @Override
  protected DynaProperty createDynaProperty(ResultSetMetaData metadata, int integer)
      throws SQLException {
    DynaProperty property = super.createDynaProperty(metadata, integer);
    if (byte[].class == property.getType()) {
      property = new DynaProperty(property.getName(), InputStream.class);
    }
    return property;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.beanutils.JDBCDynaClass#getObject(java.sql.ResultSet, java.lang.String)
   */
  @Override
  protected Object getObject(ResultSet resultSet, String name) throws SQLException {
    DynaProperty property = getDynaProperty(name);
    if (property == null) {
      throw new IllegalArgumentException("Invalid name '" + name + "'");
    }
    String columnName = getColumnName(name);
    Class type = property.getType();

    if (type.equals(Date.class)) {
      return resultSet.getDate(columnName);
    }

    if (type.equals(Timestamp.class)) {
      return resultSet.getTimestamp(columnName);
    }

    if (type.equals(Time.class)) {
      return resultSet.getTime(columnName);
    }

    if (type.equals(InputStream.class)) {
      return resultSet.getBinaryStream(columnName);
    }

    return resultSet.getObject(columnName);
  }
}
