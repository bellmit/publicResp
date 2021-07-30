package com.bob.hms.changepassword;

public class ChangePasswordDaoFactory {

  private static ChangePasswordDaoInterface dao = null;

  /**
   * Gets the change password DAO.
   *
   * @return the change password DAO
   */
  public static ChangePasswordDaoInterface getChangePasswordDao() {

    if (dao == null) {
      dao = new ChangePasswordImplDao();
    }
    return dao;
  }
}
