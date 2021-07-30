package com.insta.hms.ivf;

import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PrescriptionAbstractImpl.
 */
public class PrescriptionAbstractImpl extends AbstractPrescriptionDetails {
  
  private static final GenericDAO ivfDailyPrescriptionDAO =
      new GenericDAO("ivf_daily_prescription");
  private static final GenericDAO ivfLutealPrescriptionsDAO =
      new GenericDAO("ivf_luteal_prescriptions");

  /**
   * @see com.insta.hms.ivf.AbstractPrescriptionDetails#otherTxWhileCreate(java.sql.Connection, int,
   *      int, java.util.Map, java.util.List, java.lang.String)
   */
  public boolean otherTxWhileCreate(Connection con, int presID, int ivfCycleDailyID,
      Map requestParams, List errors, String cycleStatus) throws SQLException, IOException {
    boolean success = false;
    GenericDAO dao =
        cycleStatus.equals("dailyTreatment") ? ivfDailyPrescriptionDAO : ivfLutealPrescriptionsDAO;
    BasicDynaBean prescBean = dao.getBean();
    if (cycleStatus.equals("dailyTreatment")) {
      prescBean.set("ivf_cycle_daily_id", ivfCycleDailyID);
    } else {
      prescBean.set("ivf_cycle_id", ivfCycleDailyID);
    }

    prescBean.set("prescription_id", presID);
    success = dao.insert(con, prescBean);
    return success;
  }

  /**
   * @see com.insta.hms.IVF.AbstractPrescriptionDetails#otherTxWhileDelete(java.sql.Connection,
   *      java.lang.Object, java.lang.String)
   */
  public boolean otherTxWhileDelete(Connection con, Object presID, String cycleStatus)
      throws SQLException, IOException {
    GenericDAO dao =
        cycleStatus.equals("dailyTreatment") ? ivfDailyPrescriptionDAO : ivfLutealPrescriptionsDAO;
    return dao.delete(con, "prescription_id", presID);
  }
}