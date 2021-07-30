package com.insta.hms.common;

import flexjson.JSONSerializer;

import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

final class ResultSetToJsonStreamExtractor implements ResultSetExtractor<Writer> {

  Writer writer = null;
  JSONSerializer js = new JSONSerializer().exclude("class");

  public ResultSetToJsonStreamExtractor(Writer stream) {
    this.writer = stream;
  }

  @Override
  public Writer extractData(ResultSet rs) throws SQLException {
    ResultSetMetaData rsMetaData = rs.getMetaData();
    try {
      writer.write("[");
      if (rs != null) {
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
      }
      writer.write("]");
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
    return writer;
  }

}