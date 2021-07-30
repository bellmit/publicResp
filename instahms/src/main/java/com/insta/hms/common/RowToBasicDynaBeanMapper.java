package com.insta.hms.common;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/*
 * Mapper for transforming ResultSet into a List of BasicDynaBeans using the RowSetDynaClass.
 */
final class RowToBasicDynaBeanMapper implements ResultSetExtractor<List<BasicDynaBean>> {
  @Override
  public List<BasicDynaBean> extractData(ResultSet resultSet) throws SQLException {
    ByteArrayToInputStreamRowSetDynaClass rowSetDynaClass =
        new ByteArrayToInputStreamRowSetDynaClass(resultSet);
    return (List<BasicDynaBean>) rowSetDynaClass.getRows();
  }
}
