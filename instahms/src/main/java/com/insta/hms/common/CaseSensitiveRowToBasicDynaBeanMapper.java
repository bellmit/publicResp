package com.insta.hms.common;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

final class CaseSensitiveRowToBasicDynaBeanMapper
    implements ResultSetExtractor<List<BasicDynaBean>> {
  @Override
  public List<BasicDynaBean> extractData(ResultSet resultSet) throws SQLException {
    RowSetDynaClass rowSetDynaClass = new RowSetDynaClass(resultSet, false);
    return (List<BasicDynaBean>) rowSetDynaClass.getRows();
  }
}
