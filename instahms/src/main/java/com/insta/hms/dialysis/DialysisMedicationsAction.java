package com.insta.hms.dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.RecurrenceDailyMaster.RecurrenceDailyMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class DialysisMedicationsAction.
 */
public class DialysisMedicationsAction extends DispatchAction {

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, Exception {

    String mrNo = request.getParameter("mr_no");
    if (mrNo != null && !mrNo.equals("")) {
      Map patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo);
      if (patmap == null) {
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error", mrNo + " doesn't exists.");
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
      String visitId = request.getParameter("filtervisitId");
      String filterType = request.getParameter("filterType");
      if (filterType == null || filterType.equals("")) {
        filterType = "patient";
      }
      request.setAttribute("treatmentChart",
          DialysisMedicationsDAO.gettreatmentCharts(mrNo, filterType, visitId));
      request.setAttribute("mrNo", mrNo);
      BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
      request.setAttribute("genericPrefs", genericPrefs.getMap());
      String prescriptionUsesStores = (String) genericPrefs.get("prescription_uses_stores");
      Map filterMap = new HashMap<>();
      filterMap.put("status", "A");
      filterMap.put("medication_type", "M");
      request.setAttribute("frequencies",
          new RecurrenceDailyMasterDAO().listAll(null, filterMap,null));
      request.setAttribute("visitsList", VisitDetailsDAO.getAllVisitsAndDoctors(mrNo));
      request.setAttribute("filterType", filterType);
      request.setAttribute("filtervisitId", visitId);
    }

    return mapping.findForward("show");
  }

  /**
   * Save treatment chart.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public ActionForward saveTreatmentChart(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

    String userName = (String) request.getSession(false).getAttribute("userid");
    String mrNo = request.getParameter("mr_no");
    String filterType = request.getParameter("prescriptionTO");
    String filterVisitId = request.getParameter("visitId");
    GenericDAO treatmentChartDAO = new GenericDAO("treatment_chart");
    GenericDAO mmDao = new GenericDAO("prescribed_medicines_master");
    Connection con = null;

    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("mr_no", mrNo);
    redirect.addParameter("filtervisitId", filterVisitId);
    redirect.addParameter("filterType", filterType);
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      BasicDynaBean beanforOrderID = DialysisMedicationsDAO.getOrderId(mrNo);
      String prescriptionTO = request.getParameter("prescriptionTO");
      String visitId = request.getParameter("visitId");
      String[] prescIds = request.getParameterValues("s_prescription_id");
      String[] itemNames = request.getParameterValues("s_item_name");
      String[] itemIds = request.getParameterValues("s_item_id");
      String[] itemTypes = request.getParameterValues("s_itemType");
      String[] medicineDosages = request.getParameterValues("s_medicine_dosage");
      String[] itemRemarks = request.getParameterValues("s_item_remarks");
      String[] itemMasters = request.getParameterValues("s_item_master");
      String[] frequencyTypes = request.getParameterValues("s_freq_type");
      String[] recurrenceDailyIds = request.getParameterValues("s_recurrence_daily_id");
      String[] discontinued = request.getParameterValues("s_discontinued");
      String[] isPackage = request.getParameterValues("s_ispackage");
      String[] routeOfAdmins = request.getParameterValues("s_route_id");
      String[] days = request.getParameterValues("s_days");
      String[] userDate = request.getParameterValues("s_prescribed_date");
      String[] edited = request.getParameterValues("s_edited");
      String[] deleted = request.getParameterValues("s_delItem");
      String[] itemStrengths = request.getParameterValues("s_item_strength");
      String[] itemFormIds = request.getParameterValues("s_item_form_id");

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
            if (itemMasters[i].equals("")) {
              if (itemTypes[i].equals("Medicine")) {
                BasicDynaBean presMedMasterBean = mmDao.getBean();
                presMedMasterBean.set("medicine_name", itemNames[i]);
                presMedMasterBean.set("status", "A");

                if (!mmDao.insert(con, presMedMasterBean)) {
                  flash.put("error", "Transaction Failure");
                  return redirect;
                }

              }
            }
            itemPrescriptionId = treatmentChartDAO.getNextSequence();
            treatmentChart.set("prescription_id", itemPrescriptionId);
            treatmentChart.set("mr_no", mrNo);
            if (prescriptionTO.equals("visit")) {
              treatmentChart.set("visit_id", visitId);
            }
            if (beanforOrderID != null && beanforOrderID.get("prescription_id") != null
                && !beanforOrderID.equals("")) {
              treatmentChart.set("order_id", beanforOrderID.get("prescription_id"));
            }
            if (!treatmentChartDAO.insert(con, treatmentChart)) {
              flash.put("error", "Transaction Failure");
              return redirect;
            }
          } else if (deleted[i].equals("false") && edited[i].equals("true")) {
            itemPrescriptionId = Integer.parseInt(prescIds[i]);
            Map keys = new HashMap();
            keys.put("prescription_id", itemPrescriptionId);
            if (treatmentChartDAO.update(con, treatmentChart.getMap(), keys) == 0) {
              flash.put("error", "Transaction Failure");
              return redirect;
            }
          } else if (deleted[i].equals("true")) {
            itemPrescriptionId = Integer.parseInt(prescIds[i]);
            if (!treatmentChartDAO.delete(con, "prescription_id", itemPrescriptionId)) {
              flash.put("error", "Transaction Failure");
              return redirect;
            }
          }

        }
        con.commit();
      }

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    flash.put("success", "Prescription added successfully");
    return redirect;
  }

}