package com.insta.hms.ivf;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractPrescriptionDetails.
 */
public abstract class AbstractPrescriptionDetails {

  /**
   * Other tx while create.
   *
   * @param con the con
   * @param presID the pres ID
   * @param ivfCycleID the ivf cycle ID
   * @param requestParams the request params
   * @param errors the errors
   * @param cycleStatus the cycle status
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public abstract boolean otherTxWhileCreate(Connection con, int presID, int ivfCycleID,
      Map requestParams, List errors, String cycleStatus) throws SQLException, IOException;

  /**
   * Other tx while delete.
   *
   * @param con the con
   * @param presID the pres ID
   * @param cycleStatus the cycle status
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public abstract boolean otherTxWhileDelete(Connection con, Object presID, String cycleStatus)
      throws SQLException, IOException;

  /**
   * Save prescription details.
   *
   * @param con the con
   * @param requestParams the request params
   * @param userName the user name
   * @param mrNo the mr no
   * @param visitId the visit id
   * @param ivfCycleDailyID the ivf cycle daily ID
   * @param cycleStatus the cycle status
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public boolean savePrescriptionDetails(Connection con, Map requestParams, String userName,
      String mrNo, String visitId, int ivfCycleDailyID, String cycleStatus) throws SQLException,
      IOException, ParseException {
    List errors = new ArrayList();
    GenericDAO treatmentChartDAO = new GenericDAO("treatment_chart");

    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      String[] prescIds = (String[]) requestParams.get("s_prescription_id");
      String[] itemNames = (String[]) requestParams.get("s_item_name");
      String[] itemIds = (String[]) requestParams.get("s_item_id");
      String[] itemTypes = (String[]) requestParams.get("s_itemType");
      String[] medicineDosages = (String[]) requestParams.get("s_medicine_dosage");
      String[] itemRemarks = (String[]) requestParams.get("s_item_remarks");
      String[] frequencyTypes = (String[]) requestParams.get("s_freq_type");
      String[] recurrenceDailyIds = (String[]) requestParams.get("s_recurrence_daily_id");
      String[] discontinued = (String[]) requestParams.get("s_discontinued");
      String[] isPackage = (String[]) requestParams.get("s_ispackage");
      String[] routeOfAdmins = (String[]) requestParams.get("s_route_id");
      String[] days = (String[]) requestParams.get("s_days");
      String[] userDate = (String[]) requestParams.get("s_prescribed_date");
      String[] edited = (String[]) requestParams.get("s_edited");
      String[] deleted = (String[]) requestParams.get("s_delItem");
      String[] itemStrengths = (String[]) requestParams.get("s_item_strength");
      String[] itemFormIds = (String[]) requestParams.get("s_item_form_id");

      if (prescIds != null) {
        for (int i = 0; i < prescIds.length - 1; i++) {
          String itemType = itemTypes[i];
          int itemPrescriptionId = 0;

          String prescriptionType = null;
          if (itemType.equals("Medicine")) {
            prescriptionType = useStoreItems.equals("Y") ? "M" : "O";
          }

          BasicDynaBean treatmentChart = treatmentChartDAO.getBean();

          treatmentChart.set("type", prescriptionType);
          if (prescriptionType.equals("O")) {
            treatmentChart.set("item_name", itemNames[i]);
          } else {
            treatmentChart.set("item_id", itemIds[i]);
          }

          treatmentChart.set("item_strength", itemStrengths[i]);
          if (!itemFormIds[i].equals("")) {
            treatmentChart.set("item_form_id", Integer.parseInt(itemFormIds[i]));
          }
          if (!routeOfAdmins[i].equals("")) {
            treatmentChart.set("route_of_admin", Integer.parseInt(routeOfAdmins[i]));
          }
          treatmentChart.set("medicine_dosage", medicineDosages[i]);
          treatmentChart.set("remarks", itemRemarks[i]);
          treatmentChart.set("freq_type", frequencyTypes[i]);
          if (days[i] != null && !days[i].equals("")) {
            treatmentChart.set("days", Integer.parseInt(days[i]));
          }
          int recurrenceDailyId = 0;
          if (!recurrenceDailyIds[i].equals("")) {
            recurrenceDailyId = Integer.parseInt(recurrenceDailyIds[i]);
          }
          treatmentChart.set("recurrence_daily_id", recurrenceDailyId);
          treatmentChart.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
          treatmentChart.set("username", userName);
          treatmentChart.set("discontinued", discontinued[i]);
          treatmentChart.set("ispackage", new Boolean(isPackage[i]));
          treatmentChart.set("prescribed_date", DateUtil.parseDate(userDate[i]));

          String prescId = prescIds[i];
          if (prescId.equals("_")) {
            itemPrescriptionId = treatmentChartDAO.getNextSequence();
            treatmentChart.set("prescription_id", itemPrescriptionId);
            treatmentChart.set("mr_no", mrNo);
            treatmentChart.set("visit_id", visitId);
            if (!treatmentChartDAO.insert(con, treatmentChart)) {
              return false;
            } else {
              if (!otherTxWhileCreate(con, itemPrescriptionId, ivfCycleDailyID, requestParams,
                  errors, cycleStatus)) {
                return false;
              }
            }
          } else if (deleted[i].equals("false") && edited[i].equals("true")) {
            itemPrescriptionId = Integer.parseInt(prescIds[i]);
            Map<String, Object> keys = new HashMap<String, Object>();
            keys.put("prescription_id", itemPrescriptionId);
            if (treatmentChartDAO.update(con, treatmentChart.getMap(), keys) == 0) {
              return false;
            }
          } else if (deleted[i].equals("true")) {
            itemPrescriptionId = Integer.parseInt(prescIds[i]);
            if (!treatmentChartDAO.delete(con, "prescription_id", itemPrescriptionId)) {
              return false;
            } else {
              if (!otherTxWhileDelete(con, itemPrescriptionId, cycleStatus)) {
                return false;
              }
            }
          }

        }
        con.commit();
      }

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return true;
  }
}