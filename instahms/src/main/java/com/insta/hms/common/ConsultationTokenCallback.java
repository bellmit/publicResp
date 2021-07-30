package com.insta.hms.common;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class ConsultationTokenCallback implements PreparedStatementCallback<Integer> {
  private String doctorID = null;

  public ConsultationTokenCallback(String doctorID) {
    this.doctorID = doctorID;
  }

  @Override
  public Integer doInPreparedStatement(PreparedStatement preparedStatement)
      throws SQLException, DataAccessException {
    preparedStatement.setObject(1, doctorID);
    ResultSet resultSet = preparedStatement.executeQuery();

    Integer token = null;
    while (resultSet.next()) {
      token = resultSet.getInt(1);
    }
    return token;
  }

}
