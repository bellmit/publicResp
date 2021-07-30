package com.bob.hms.changepassword;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChangePasswordImplDao implements ChangePasswordDaoInterface {

  /**
   * Change Password implementation.
   */
  public String changePassword(ChangePasswordForm sef) {

    Connection con = null;
    String target = "success";
    try {
      Logger.log("New Password is ------------------" + sef.getPwd());
      String updatePassword = "UPDATE U_USER SET EMP_PASSWORD=?,ENCRYPT_ALGO=? , "
          + "PASSWORD_CHANGE_DATE=now() WHERE EMP_USERNAME=? AND EMP_PASSWORD=? ";
      Logger.log("Query---" + updatePassword);

      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      try (PreparedStatement stmt = con.prepareStatement(updatePassword)) {
        stmt.setString(1, sef.getPwd());
        stmt.setString(2, "BCRYPT");
        stmt.setString(3, sef.getUid());
        stmt.setString(4, sef.getOldpwd());
        int res = stmt.executeUpdate();
        if (res > 0) {
          con.commit();
        } else {
          con.rollback();
          target = "failure";
        }
      }
    } catch (BatchUpdateException batchUpdateException) {
      batchUpdateException.printStackTrace();
      Logger.logException("Exception Occured in PartPaymentsImplDAO", batchUpdateException);
      target = "failure";
    } catch (Exception exception) {
      exception.printStackTrace();
      Logger.logException("Exception Occured in PartPaymentsImplDAO", exception);
      target = "failure";
    } finally {
      try {
        if (!con.isClosed()) {
          con.close();
        }
      } catch (SQLException sqlException) {
        Logger.logException("Exception Occured in PartPaymentsImplDAO", sqlException);
      }
    }
    Logger.log("TARGET====" + target);
    return target;
  }
}
