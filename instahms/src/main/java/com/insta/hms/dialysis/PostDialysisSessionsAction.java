package com.insta.hms.dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.dialysisadequacy.DialysisAdequacyDAO;
import com.insta.hms.master.Dialysis.DialAccessTypesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class PostDialysisSessionsAction.
 */
public class PostDialysisSessionsAction extends DispatchAction {
  
  /** The js. */
  JSONSerializer js = new JSONSerializer();
  
  private static final GenericDAO uUserDAO = new GenericDAO("u_user");

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  /*
   * show: returns the "edit" screen for showing/editing an existing Post Dialysis Session.
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException {
    return addShow(mapping, form, req, res);
  }

  /**
   * Adds the show.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  /*
   * Common method for add/show. Returns an add/show screen.
   */
  private ActionForward addShow(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException {

    DialysisSessionsDao dao = new DialysisSessionsDao();
    String orderId = req.getParameter("order_id");

    BasicDynaBean postSesDetailsList = null;
    postSesDetailsList = dao.findByKey("order_id", Integer.parseInt(orderId));
    BasicDynaBean consumablesBean = dao.getConsumableDetails(Integer.parseInt(orderId));
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    String prescriptionUsesStores = (String) genericPrefs.get("prescription_uses_stores");
    int visitCenter = dao.getVisitCenter(Integer.parseInt(orderId));

    req.setAttribute("logedin_user", req.getSession(false).getAttribute("userid"));
    req.setAttribute("postSesDetails", postSesDetailsList.getMap());
    DialAccessTypesDAO accesstypedao = new DialAccessTypesDAO();
    req.setAttribute("AccessTypeDetailsJson", js.exclude("class").serialize(
        ConversionUtils.copyListDynaBeansToMap(accesstypedao.findAllByKey("status", "A"))));
    req.setAttribute("ConsumablesDetails", consumablesBean);
    String mrno = req.getParameter("mr_no");
    req.setAttribute("mr_no", mrno);
    req.setAttribute("order_id", orderId);
    req.setAttribute("isFinalized", dao.checkFinalized(Integer.parseInt(orderId)));
    req.setAttribute("staff", uUserDAO.findAllByKey("emp_status", "A"));
    req.setAttribute("clinicalStaff",
        dao.clinicalStaffList("dialysis_post_sessions", visitCenter));
    req.setAttribute("administeredDrugs",
        ConversionUtils.copyListDynaBeansToMap(dao.getDrugList(Integer.parseInt(orderId))));
    req.setAttribute("genericPrefs", genericPrefs.getMap());
    req.setAttribute("treatmentChart",
        ConversionUtils.copyListDynaBeansToMap(dao.getPostDrugList(Integer.parseInt(orderId))));
    req.setAttribute("issuedToPatient",
        ConversionUtils.copyListDynaBeansToMap(dao.getIssuedToPatienList(mrno)));
    req.setAttribute("routes",
        ConversionUtils.copyListDynaBeansToMap(new GenericDAO("medicine_route").listAll()));
    req.setAttribute("visit_center", visitCenter);
    Map<String, Object> filter = new HashMap<>();
    filter.put("emp_status", "A");
    filter.put("center_id", visitCenter);
    req.setAttribute("users", uUserDAO.listAll(null, filter, "emp_username"));
    return mapping.findForward("addshow");

  }

  /**
   * update: POST method to update an existing Post Dialysis Session.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, IOException, ParseException {

    FlashScope flash = FlashScope.getScope(req);
    DialysisAdequacyDAO adequacyDao = new DialysisAdequacyDAO();
    Map<String, Map<String, Object>> valuesMap = new HashMap<>();

    // GenericDAO drugDAO = new GenericDAO("dialysis_patient_drugs");

    String[] drugIds = req.getParameterValues("dialysis_patient_drug_id");

    // error redirect: to the same page; from the referer header.
    ActionRedirect errRedirect = new ActionRedirect(
        req.getHeader("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    errRedirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    // success redirect: back to the dashboard or wherever "add" was called from
    ActionRedirect redirect;
    if (req.getParameter("Referer") != null) {
      redirect = new ActionRedirect(
          req.getParameter("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    } else {
      redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    ArrayList errors = new ArrayList();
    DialysisSessionsDao dao = new DialysisSessionsDao();
    BasicDynaBean bean = dao.getBean();
    ConversionUtils.copyToDynaBean(req.getParameterMap(), bean, errors);
    String orderId = req.getParameter("order_id");
    String username = (String) req.getSession(false).getAttribute("userid");
    // String[] expiryDate = req.getParameterValues("expiry_date");

    Connection con = null;
    boolean allSuccess = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      // Update the Post Dialysis Session
      boolean success = (1 == dao.update(con, bean.getMap(), "order_id",
          Integer.parseInt(orderId)));

      if (!success) {
        flash.put("error", "Transaction Failure");
        return errRedirect;
      }
      BasicDynaBean sessionBean = dao.findByKey("order_id", Integer.parseInt(orderId));
      sessionBean.set("fin_real_wt", bean.get("fin_real_wt"));
      valuesMap = adequacyDao.getCalculatedKtvandUrr(con, sessionBean.getMap(),
          req.getParameter("mr_no"), username, null);
      String mrNo = req.getParameter("mr_no");
      success = adequacyDao.saveKtvAndUrrValues(con, valuesMap, mrNo, username);

      if (!success) {
        flash.put("error", "Transaction fail to insert adequcy details");
        return errRedirect;
      }

      String userName = (String) req.getSession(false).getAttribute("userid");
      GenericDAO drugChartDAO = new GenericDAO("drugs_administered");
      String[] prescIds = req.getParameterValues("s_prescription_id");
      String[] itemNames = req.getParameterValues("s_item_name");
      String[] itemIds = req.getParameterValues("s_item_id");
      String[] medicineDosages = req.getParameterValues("s_medicine_dosage");
      String[] itemRemarks = req.getParameterValues("s_item_remarks");
      String[] routeOfAdmins = req.getParameterValues("s_route_id");
      String[] quantity = req.getParameterValues("s_quantity");
      String[] expiryDate = req.getParameterValues("s_expdate");
      String[] staff = req.getParameterValues("s_staff");
      String[] doctorId = req.getParameterValues("s_doctor");
      String[] batchNos = req.getParameterValues("s_batch_no");
      String[] delItems = req.getParameterValues("s_delItem");
      String[] editItems = req.getParameterValues("s_edited");

      if (prescIds != null) {
        for (int i = 0; i < prescIds.length - 1; i++) {
          
          int itemPrescriptionId = 0;

          BasicDynaBean drugChart = drugChartDAO.getBean();
          drugChart.set("user_name", userName);
          drugChart.set("mr_no", mrNo);
          drugChart.set("order_id", Integer.parseInt(orderId));
          drugChart.set("medicine_name", itemNames[i]);
          if (itemIds[i] != null && !itemIds[i].equals("")) {
            drugChart.set("medicine_id", Integer.parseInt(itemIds[i]));
          }
          if (!routeOfAdmins[i].equals("")) {
            drugChart.set("route_of_admin", Integer.parseInt(routeOfAdmins[i]));
          }
          drugChart.set("dosage", medicineDosages[i]);
          drugChart.set("remarks", itemRemarks[i]);
          BigDecimal medQuantity = BigDecimal.ZERO;
          if (quantity[i] != null && !quantity[i].equals("")) {
            medQuantity = new BigDecimal(quantity[i]);
          }
          drugChart.set("quantity", medQuantity);
          drugChart.set("expiry_date", expiryDate[i]);
          drugChart.set("staff", staff[i]);
          drugChart.set("doctor_id", doctorId[i]);
          drugChart.set("batch_no", batchNos[i]);
          drugChart.set("prescribed_date", new java.sql.Timestamp(new java.util.Date().getTime()));
          String prescId = prescIds[i];
          if (prescId.equals("_")) {
            int drugChartID = drugChartDAO.getNextSequence();
            drugChart.set("drug_administered_id", drugChartID);
            if (!drugChartDAO.insert(con, drugChart)) {
              flash.put("error", "Transaction Failure");
              return errRedirect;
            }
          } else if (delItems[i].equals("false") && editItems[i].equals("true")) {
            itemPrescriptionId = Integer.parseInt(prescIds[i]);
            Map keys = new HashMap();
            keys.put("drug_administered_id", itemPrescriptionId);
            if (drugChartDAO.update(con, drugChart.getMap(), keys) == 0) {
              flash.put("error", "Transaction Failure");
              return errRedirect;
            }
          } else if (delItems[i].equals("true")) {
            itemPrescriptionId = Integer.parseInt(prescIds[i]);
            if (!drugChartDAO.delete(con, "drug_administered_id", itemPrescriptionId)) {
              flash.put("error", "Transaction Failure");
              return errRedirect;
            }

          }

        }
      }

      allSuccess = true;
      flash.put("success", "Post Dialysis Session updated successfully");

    } finally {
      DataBaseUtil.commitClose(con, allSuccess);
    }
    redirect.addParameter("mr_no", req.getParameter("mr_no"));
    return errRedirect;
  }

  /**
   * Fill from bill.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward fillFromBill(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    String mrNo = request.getParameter("mr_no");
    JSONSerializer js = new JSONSerializer();
    List<BasicDynaBean> beanList = DialysisSessionsDao.getIssuedToPatienList(mrNo);
    List<Map> mapList = ConversionUtils.listBeanToListMap(beanList);
    response.setContentType("application/json");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter().write(js.serialize(mapList));

    return null;
  }
}
