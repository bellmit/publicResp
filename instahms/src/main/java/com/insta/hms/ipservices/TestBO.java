package com.insta.hms.ipservices;

import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.ChargeBO;
import com.insta.hms.billing.ChargeDAO;

import java.sql.Connection;

// TODO: Auto-generated Javadoc
/**
 * The Class TestBO.
 */
public class TestBO {
  
  /** The dao. */
  DashBoardDAO dao = new DashBoardDAO();

  /**
   * Cancel tests prescribed.
   *
   * @param con the con
   * @param prescribedId the prescribed id
   * @param head the head
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean cancelTestsPrescribed(Connection con, String prescribedId, String head)
      throws Exception {
    boolean status = false;
    if (new ChargeBO().isCancelable("DIA", Integer.parseInt(prescribedId))) {
      if (new TestsDAO().cancelTestsPrescribed(con, Integer.parseInt(prescribedId))) {
        String chargeid = BillActivityChargeDAO.getChargeId("DIA", Integer.parseInt(prescribedId));
        if (new ChargeDAO(con).cancelCharge(con, chargeid)) {
          status = true;
        } else if (chargeid == null) {
          status = true;
        }
      }
    } else {
      if (BillActivityChargeDAO.getChargeId("DIA", Integer.parseInt(prescribedId)) == null) {
        if (new TestsDAO().cancelTestsPrescribed(con, Integer.parseInt(prescribedId))) {
          status = true;
        }
      }
    }
    return status;
  }
}
