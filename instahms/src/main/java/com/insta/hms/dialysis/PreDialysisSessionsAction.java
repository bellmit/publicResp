package com.insta.hms.dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.Dialysis.DialAccessTypesDAO;
import com.insta.hms.master.Dialysis.DialLocationMasterDAO;
import com.insta.hms.master.Dialysis.DialysisMachineMasterDAO;
import com.insta.hms.usermanager.PasswordEncoder;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class PreDialysisSessionsAction.
 */
public class PreDialysisSessionsAction extends DispatchAction {
  
  private static final GenericDAO uUserDAO = new GenericDAO("u_user");
  private static final GenericDAO dialysisPrepValuesDAO = new GenericDAO("dialysis_prep_values");


  /**
   * add: returns the "add" screen for adding a new Pre Dialysis Session.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException {
    return addShow(mapping, form, req, res);
  }

  /**
   * show: returns the "edit" screen for showing/editing an existing Pre Dialysis Session.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException {
    return addShow(mapping, form, req, res);
  }

  /**
   * Common method for add/show. Returns an add/show screen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  private ActionForward addShow(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException {
    String mrno = req.getParameter("mr_no");
    String servicePrescId = req.getParameter("prescriptionId");
    String method = req.getParameter("_method");
    DialysisSessionsDao dao = new DialysisSessionsDao();
    int activePrescriptionId = 0;
    String orderId = req.getParameter("order_id");
    BasicDynaBean preSesDetailsList = null;
    String prevSessionNotes = null;
    JSONSerializer js = new JSONSerializer();

    if (method.equals("show")) {
      preSesDetailsList = dao.getPrescAndDialysisDetails(Integer.parseInt(orderId));

      BasicDynaBean prevSesNotes = dao.getPrevSessionNotes(Integer.parseInt(orderId), mrno);

      if (prevSesNotes != null && prevSesNotes.get("post_session_notes") != null
          && !(prevSesNotes.get("post_session_notes").toString().equals(""))) {
        prevSessionNotes = (String) prevSesNotes.get("post_session_notes");
      }

      if (prevSesNotes != null && prevSesNotes.get("fin_real_wt") != null
          && !(prevSesNotes.get("fin_real_wt").toString().equals(""))) {
        req.setAttribute("previousSesWT", prevSesNotes.get("fin_real_wt"));
      } else {
        req.setAttribute("previousSesWT", null);
      }

      req.setAttribute("method", "update");
      req.setAttribute("order_id", orderId);
      activePrescriptionId = Integer.parseInt(req.getParameter("dialysisprescId"));
      req.setAttribute("isFinalized", dao.checkFinalized(Integer.parseInt(orderId)));
      req.setAttribute("prePrepDialysisList",
          dao.getPreDialysiPrepDetails(Integer.parseInt(orderId)));
    } else {
      // check for active Prescription
      BasicDynaBean prescBean = new GenericDAO("dialysis_prescriptions").findByKey("mr_no", mrno);

      if (prescBean == null) {
        req.setAttribute("mr_no", mrno);
        return mapping.findForward("noprescriptions");

      } else if (prescBean != null && !prescBean.get("status").toString().equalsIgnoreCase("A")) {
        req.setAttribute("mr_no", mrno);
        return mapping.findForward("prescription");
      }
      activePrescriptionId = Integer.parseInt(prescBean.get("dialysis_presc_id").toString());
      preSesDetailsList = dao.getDialysisSessionDetails(activePrescriptionId);
      req.setAttribute("previousSesWT", preSesDetailsList.get("fin_real_wt"));
      prevSessionNotes = (String) preSesDetailsList.get("post_session_notes");

      req.setAttribute("logedin_user", req.getSession(false).getAttribute("userid"));

      req.setAttribute("order_id", servicePrescId);
      orderId = servicePrescId;
      req.setAttribute("method", "create");
      req.setAttribute("prePrepDialysisList", dao.getPreDialysiPrepDetails());
    }
    DialysisMachineMasterDAO daoMachine = new DialysisMachineMasterDAO();

    req.setAttribute("preSesDetails", preSesDetailsList.getMap());
    req.setAttribute("machineMasterDetailsJson", js.exclude("class")
        .serialize(ConversionUtils.copyListDynaBeansToMap(daoMachine.findAllByKey("status", "A"))));
    DialAccessTypesDAO accesstypedao = new DialAccessTypesDAO();
    req.setAttribute("AccessTypeDetailsJson", js.exclude("class").serialize(
        ConversionUtils.copyListDynaBeansToMap(accesstypedao.findAllByKey("status", "A"))));
    req.setAttribute("mr_no", mrno);
    req.setAttribute("prescription_id", activePrescriptionId);
    req.setAttribute("staff", uUserDAO.findAllByKey("emp_status", "A"));
    req.setAttribute("attendantJson", js.exclude("class").serialize(ConversionUtils
        .copyListDynaBeansToMap(uUserDAO.findAllByKey("emp_status", "A"))));
    req.setAttribute("accessTypes", DialysisSessionsDao.getAccessTypes(mrno));
    ArrayList vaccinations = DialysisSessionsDao.getVaccinationList(mrno);
    ArrayList labResults = DialysisSessionsDao.getLabResultsList(mrno);
    req.setAttribute("vaccinations", vaccinations);
    req.setAttribute("jsVaccinations", js.serialize(vaccinations));
    req.setAttribute("prev_session_notes", prevSessionNotes);
    req.setAttribute("labResults", labResults);
    req.setAttribute("jsLabResults", js.serialize(labResults));

    // locations, machines and staff are retrieved for the visit center
    Integer visitCenter = null;
    String visitCenterStr = req.getParameter("visit_center");
    visitCenterStr = visitCenterStr == null ? "" : visitCenterStr;
    if (visitCenterStr.equals("") && (orderId != null && !orderId.equals(""))) {
      visitCenter = new DialysisSessionsDao().getVisitCenter(Integer.parseInt(orderId));
    }

    if (!visitCenterStr.equals("")) {
      visitCenter = Integer.parseInt(visitCenterStr);
    }

    req.setAttribute("clinicalStaff", dao.clinicalStaffList("dialysis_pre_sessions", visitCenter));
    req.setAttribute("locations", DialLocationMasterDAO.getAvalDialLocations(visitCenter));
    req.setAttribute("machines",
        DialysisMachineMasterDAO.getNotAssignedMachines(visitCenter, orderId));
    return mapping.findForward("addshow");

  }

  /**
   * create: POST method to create a new Pre Dialysis Session.
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
  public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, IOException, ParseException {
    FlashScope flash = FlashScope.getScope(req);


    BasicDynaBean dpvBean = null;
    String[] paramIds = req.getParameterValues("prep_param_id");
    String[] paramValues = req.getParameterValues("prep_param_value");

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

    String status = req.getParameter("status");
    int orderId = (Integer) bean.get("order_id");
    int machineId = 0;
    if (bean.get("machine_id") != null) {
      machineId = (Integer) bean.get("machine_id");
    }
    String originalMachineId = req.getParameter("original_machine_id");

    Connection con = null;
    boolean allSuccess = false;

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      List allowStatuses = Arrays.asList("I", "F", "C");
      if (allowStatuses.contains(status)) {
        bean.set("start_time", DateUtil.getCurrentTimestamp());
      }

      if (status.equals("I") || status.equals("P")) {
        // Assing Machine Status
        // check if machine is already assigned for any other patient
        BasicDynaBean machinebean = DialysisMachinesStatusDAO.checkForAlreadyAssigned(machineId);

        if (machinebean != null && !machinebean.get("assigned_order_id").toString().equals("")
            && !machinebean.get("assigned_order_id").toString()
                .equals(bean.get("order_id").toString())) {
          flash.put("error", "Selected Machine is already Assigned");
          return errRedirect;
        }

        DialysisMachinesStatusDAO.assignMachine(con, machineId, orderId);
      }

      boolean success = dao.insert(con, bean);

      if (paramIds != null && paramValues != null) {
        for (int i = 0; i < paramIds.length; i++) {
          if (!paramIds[i].equals("")) {
            dpvBean = dialysisPrepValuesDAO.getBean();
            ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, dpvBean, errors);
            dpvBean.set("prep_param_id", Integer.parseInt(paramIds[i]));
            dpvBean.set("prep_param_value", paramValues[i]);
            dpvBean.set("order_id", orderId);
            if (success) {
              success = dialysisPrepValuesDAO.insert(con, dpvBean);
            }
          }
        }

      }

      if (!success) {
        flash.put("error", "Transaction Failure");
        return errRedirect;
      }

      if (machineId != 0) {
        DialysisMachinesStatusDAO.assignMachine(con, orderId, machineId);
      }

      allSuccess = true;
      flash.put("success", "Pre Dialysis Session Added Successfully");

    } finally {
      DataBaseUtil.commitClose(con, allSuccess);
      redirect.addParameter("mr_no", req.getParameter("mr_no"));
    }

    return redirect;
  }

  /**
   * update: POST method to update an existing Pre Dialysis Session.
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

    BasicDynaBean dpvBean = null;
    String[] paramIds = req.getParameterValues("prep_param_id");
    String[] paramValues = req.getParameterValues("prep_param_value");

    String originalStatus = req.getParameter("originalStatus");

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
    DialysisSessionsDao dao = new DialysisSessionsDao();
    BasicDynaBean bean = dao.getBean();
    ArrayList errors = new ArrayList();
    ConversionUtils.copyToDynaBean(req.getParameterMap(), bean, errors);

    String heparintype = req.getParameter("heparin_type");
    if (heparintype.equals("h")) {
      bean.set("low_heparin_initial_dose", null);
      bean.set("low_heparin_intrim_dose", null);
    }

    int orderId = (Integer) bean.get("order_id");
    int machineId = 0;
    String originalMachineId = req.getParameter("original_machine_id");

    if (bean.get("machine_id") != null) {
      machineId = (Integer) bean.get("machine_id");
    }

    Connection con = null;
    boolean allSuccess = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      String presentStatus = bean.get("status").toString();
      List allowStatuses = Arrays.asList("I", "P", "F", "C");

      if ((originalStatus.equals("P") || originalStatus.equals("O"))
          && allowStatuses.contains(presentStatus)) {
        bean.set("start_time", DateUtil.getCurrentTimestamp());
      }
      if (((bean.get("status").toString().equals("I")) || bean.get("status").toString().equals("P"))
          && bean.get("machine_id") != null) {

        // Assing Machine Status
        // check if machine is already assigned for any other patient
        BasicDynaBean machinebean = DialysisMachinesStatusDAO.checkForAlreadyAssigned(machineId);

        if (machinebean != null && !machinebean.get("assigned_order_id").toString().equals("")
            && !machinebean.get("assigned_order_id").toString()
                .equals(bean.get("order_id").toString())) {
          flash.put("error", "Selected Machine is already Assigned");
          return errRedirect;
        }

        if (originalMachineId != null && !originalMachineId.equals("")
            && !originalMachineId.equals(bean.get("machine_id").toString())) {
          DialysisMachinesStatusDAO.unassignMachine(con, Integer.parseInt(originalMachineId));
          DialysisMachinesStatusDAO.assignMachine(con, machineId, orderId);
        } else {
          DialysisMachinesStatusDAO.assignMachine(con, machineId, orderId);
        }
      }
      if ((bean.get("status").toString().equals("F") || bean.get("status").toString().equals("C"))
          && originalMachineId != null && !originalMachineId.equals("")) {
        DialysisMachinesStatusDAO.unassignMachine(con,
            Integer.parseInt(bean.get("machine_id") == null ? originalMachineId
                : bean.get("machine_id").toString()));
      }

      boolean success = (1 == dao.update(con, bean.getMap(), "order_id", orderId));

      if (paramIds != null && paramValues != null) {
        for (int i = 0; i < paramIds.length; i++) {
          if (!paramIds[i].equals("")) {
            dpvBean = dialysisPrepValuesDAO.getBean();
            ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, dpvBean, errors);
            dpvBean.set("prep_param_value", paramValues[i]);
            if (success) {
              success = (1 == dao.updateDilaysisPrepDetails(con, paramValues[i],
                  Integer.parseInt(paramIds[i]), orderId));
            }
          }
        }

      }

      if (!success) {
        flash.put("error", "Transaction Failure");
        return errRedirect;
      }

      allSuccess = true;
      flash.put("success", "Pre Dialysis Session updated successfully");

    } finally {
      DataBaseUtil.commitClose(con, allSuccess);
    }
    redirect.addParameter("mr_no", req.getParameter("mr_no"));
    return redirect;
  }

  /**
   * Show dialysis.
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
  public ActionForward showDialysis(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, IOException, ParseException {
    String mrNo = req.getParameter("mr_no");
    DialysisSessionsDao dao = new DialysisSessionsDao();
    ActionRedirect redirect;
    List<BasicDynaBean> preDialysisList = dao.getPrescAndDialysisDetailsList(mrNo);
    if (preDialysisList != null && preDialysisList.size() > 1) {
      redirect = new ActionRedirect(mapping.findForward("currentDialysisListRedirect"));
    } else {
      if (preDialysisList != null && preDialysisList.size() > 0) {
        BasicDynaBean prescBean = preDialysisList.get(0);
        redirect = new ActionRedirect(mapping.findForward("getSession"));
        redirect.addParameter("mr_no", mrNo);
        redirect.addParameter("order_id", prescBean.get("order_id"));
        redirect.addParameter("prescriptionId", prescBean.get("dialysis_presc_id"));
        redirect.addParameter("visit_center", prescBean.get("center_id"));
        return redirect;
      } else {
        req.setAttribute("mr_no", mrNo);
        return mapping.findForward("noprescriptions");
      }
    }
    return redirect;
  }

  /**
   * Secondary password check.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws NoSuchAlgorithmException the no such algorithm exception
   */
  public ActionForward secondaryPasswordCheck(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, ServletException, IOException, NoSuchAlgorithmException {

    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    String password = request.getParameter("password");
    String user = request.getParameter("user");
    BasicDynaBean bean = uUserDAO.findByKey("emp_username", user);
    String existingPassword = (String) bean.get("emp_password");
    String result = "false";
    if (bean != null) {
      result = PasswordEncoder.matches(password, existingPassword, bean).toString();
    }

    response.getWriter().write(result);
    response.flushBuffer();
    return null;
  }

}
