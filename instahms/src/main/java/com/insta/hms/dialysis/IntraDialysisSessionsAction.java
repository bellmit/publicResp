package com.insta.hms.dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.dialysisadequacy.DialysisAdequacyDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


// TODO: Auto-generated Javadoc
/**
 * The Class IntraDialysisSessionsAction.
 */
public class IntraDialysisSessionsAction extends DispatchAction {

  
  private static final GenericDAO dialysisSessionParametersDAO =
      new GenericDAO("dialysis_session_parameters");

  private static final GenericDAO dialysisSessionIncidentsDAO =
      new GenericDAO("dialysis_session_incidents");

  private static final GenericDAO dialysisSessionNotesDAO =
      new GenericDAO("dialysis_session_notes");

  private static final GenericDAO dialysisSessionsDao = new GenericDAO("dialysis_session");
  
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
   * show: returns the "edit" screen for showing/editing an existing Intra Dialysis Session.
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
    List<BasicDynaBean> intraSesDetailsList = new ArrayList<>();

    intraSesDetailsList = dao.getIntraDialysisDetails(Integer.parseInt(orderId));

    BasicDynaBean bean = dao.getMachineStatusDetails(Integer.parseInt(orderId));
    BasicDynaBean sessionDetails = dao.getSessionDetails(Integer.parseInt(orderId));

    req.setAttribute("intraSesDetails", intraSesDetailsList);
    req.setAttribute("machineStatus", (bean != null ? bean.getMap() : null));
    req.setAttribute("sessionDetails", (sessionDetails != null ? sessionDetails.getMap() : null));
    req.setAttribute("logedin_user", req.getSession(false).getAttribute("userid"));
    List incidentsList =
        dialysisSessionIncidentsDAO.findAllByKey("order_id", Integer.parseInt(orderId));
    List sessionNtsList =
        dialysisSessionNotesDAO.findAllByKey("order_id", Integer.parseInt(orderId));
    JSONSerializer js = new JSONSerializer();
    if (incidentsList.size() > 0) {
      req.setAttribute("incidents",
          js.serialize(ConversionUtils.copyListDynaBeansToMap(incidentsList)));
    } else {
      req.setAttribute("incidents", js.exclude("class").serialize(null));
    }

    if (sessionNtsList.size() > 0) {
      req.setAttribute("sessionNotes",
          js.serialize(ConversionUtils.copyListDynaBeansToMap(sessionNtsList)));
    } else {
      req.setAttribute("sessionNotes",
          js.serialize(ConversionUtils.copyListDynaBeansToMap(sessionNtsList)));
    }
    String mrno = req.getParameter("mr_no");
    req.setAttribute("mr_no", mrno);
    req.setAttribute("order_id", orderId);
    req.setAttribute("visit_center", dao.getVisitCenter(Integer.parseInt(orderId)));
    req.setAttribute("postPrepDialysisList",
        dao.getPostDialysiPrepDetails(Integer.parseInt(orderId)));
    req.setAttribute("postPrepDialysisListWithoutOrderId", dao.getPostDialysiPrepDetails());
    return mapping.findForward("addshow");

  }

  /**
   * create: POST method to create a new Intra Dialysis Session.
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

    BasicDynaBean bean = dialysisSessionParametersDAO.getBean();
    ActionRedirect redirect = new ActionRedirect("IntraDialysisSessions.do?_method=show");
    String actionToDo = req.getParameter("actionToDo");
    String userName = (String) req.getSession(false).getAttribute("userid");

    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    ArrayList errors = new ArrayList();

    int index = Integer.parseInt(req.getParameter("machineDetailsIndex"));
    ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), index - 1, bean, errors);

    bean.set("order_id", Integer.parseInt(req.getParameter("order_id")));
    bean.set("obs_time", DataBaseUtil.parseTimestamp(req.getParameter("currentMachineTime")));
    bean.set("finalized", "N");
    bean.set("saved_by_flag", "U");
    bean.set("obs_type", null);

    Connection con = null;
    boolean allSuccess = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      bean.set("observation_id", dialysisSessionParametersDAO.getNextSequence());
      boolean success = dialysisSessionParametersDAO.insert(con, bean);
      if (!success) {
        flash.put("error", "Transaction Failure");
        return redirect;
      }

      DialysisAdequacyDAO adequacyDao = new DialysisAdequacyDAO();
      Map<String, Map<String, Object>> valuesMap = new HashMap<>();
      BasicDynaBean sessionBean = dialysisSessionsDao.findByKey("order_id",
          Integer.parseInt(req.getParameter("order_id")));

      valuesMap = adequacyDao.getCalculatedKtvandUrr(con, sessionBean.getMap(),
          req.getParameter("mr_no"), userName, null);
      success = adequacyDao.saveKtvAndUrrValues(con, valuesMap, req.getParameter("mr_no"),
          userName);
      if (!success) {
        flash.put("error", "Failed to save adequcy values");
        return redirect;
      }

      allSuccess = true;
      flash.put("success", "Intra Dialysis Session Updated Successfully");
    } finally {
      DataBaseUtil.commitClose(con, allSuccess);
      redirect.addParameter("mr_no", req.getParameter("mr_no"));
      redirect.addParameter("order_id", req.getParameter("order_id"));
    }
    return redirect;
  }

  /**
   * update: POST method to update an existing Intra Dialysis Session.
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
    DialysisSessionsDao sesdao = new DialysisSessionsDao();
    String[] paramIds = req.getParameterValues("prep_param_id");
    String[] paramValues = req.getParameterValues("prep_param_value");
    String completedStatus = req.getParameter("completion_status");
    ArrayList errors = new ArrayList();
    GenericDAO dpvDao = new GenericDAO("dialysis_prep_values");
    BasicDynaBean dpvBean = null;

    BasicDynaBean bean = null;
    ActionRedirect redirect = new ActionRedirect("IntraDialysisSessions.do?_method=show");
    ActionRedirect listRedirect = new ActionRedirect(mapping.findForward("listRedirect"));

    String[] observationId = req.getParameterValues("observation_id");
    String[] discard = req.getParameterValues("discard_obs_id");
    String[] dbFinalized = req.getParameterValues("db_finalized");
    List<String> dbFinalizedList = new ArrayList<>();
    int len = 0;
    String originalStatus = req.getParameter("originalStatus");
    String[] discardIncidents = req.getParameterValues("discard_ids");

    if (observationId != null) {
      len = observationId.length - 1;
    }
    if (dbFinalized != null) {
      dbFinalizedList = Arrays.asList(dbFinalized);
    }

    Connection con = null;
    boolean allSuccess = false;
    ArrayList errorFields = new ArrayList();
    String userId = req.getSession(false).getAttribute("userid").toString();
    String[] incidentTime = req.getParameterValues("incident_time");
    String[] sessionTime = req.getParameterValues("session_time");
    String[] discardSession = req.getParameterValues("discard_sess_ids");

    String status = req.getParameter("status");

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      if (status != null && !(status.equals(""))) {
        boolean updateStatus = DialysisSessionsDao.updateStatus(con, status, originalStatus,
            completedStatus, Integer.parseInt(req.getParameter("order_id")));

        if (!updateStatus) {
          flash.put("error", "Transaction Failure");
          return redirect;
        }
      }
      // Unassign Machine status
      if (status != null && !(status.equals("")) && (status.equals("F") || status.equals("C"))) {
        BasicDynaBean sesbean = sesdao.findByKey("order_id",
            Integer.parseInt(req.getParameter("order_id")));
        DialysisMachinesStatusDAO.unassignMachine(con,
            Integer.parseInt(sesbean.get("machine_id").toString()));
      }
      for (int j = 0; j < incidentTime.length - 1; j++) {
        BasicDynaBean incidentBean = dialysisSessionIncidentsDAO.getBean();
        ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), j, incidentBean, errorFields);
        incidentBean.set("order_id", Integer.parseInt(req.getParameter("order_id")));

        if (incidentBean.get("incident_id") == null) {
          if (discardIncidents.length - 1 <= incidentTime.length - 1
              && !discardIncidents[j].equals("New")) {
            incidentBean.set("incident_id", dialysisSessionIncidentsDAO.getNextSequence());
            boolean success = dialysisSessionIncidentsDAO.insert(con, incidentBean);
            if (!success) {
              flash.put("error", "Transaction Failure");
              return redirect;
            }
          }
        } else {
          if (discardIncidents.length - 1 <= incidentTime.length - 1
              && discardIncidents[j].equals(incidentBean.get("incident_id").toString())) {
            boolean success = dialysisSessionIncidentsDAO.delete(con, "incident_id",
                Integer.parseInt(incidentBean.get("incident_id").toString()));
            if (!success) {
              flash.put("error", "Transaction Failure");
              return redirect;
            }
          } else {
            boolean success = (1 == dialysisSessionIncidentsDAO.update(con, incidentBean.getMap(),
                "incident_id", Integer.parseInt(incidentBean.get("incident_id").toString())));
            if (!success) {
              flash.put("error", "Transaction Failure");
              return redirect;
            }
          }
        }
      }
      for (int j = 0; j < sessionTime.length - 1; j++) {
        BasicDynaBean sessionNtsBean = dialysisSessionNotesDAO.getBean();
        ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), j, sessionNtsBean, errorFields);
        sessionNtsBean.set("order_id", Integer.parseInt(req.getParameter("order_id")));

        if (sessionNtsBean.get("session_notes_id") == null) {
          if (discardSession.length - 1 <= sessionTime.length - 1
              && !discardSession[j].equals("New")) {
            sessionNtsBean.set("session_notes_id", dialysisSessionNotesDAO.getNextSequence());
            boolean success = dialysisSessionNotesDAO.insert(con, sessionNtsBean);
            if (!success) {
              flash.put("error", "Transaction Failure");
              return redirect;
            }
          }
        } else {
          if (discardSession.length - 1 <= sessionTime.length - 1
              && discardSession[j].equals(sessionNtsBean.get("session_notes_id").toString())) {
            boolean success = dialysisSessionNotesDAO.delete(con, "session_notes_id",
                Integer.parseInt(sessionNtsBean.get("session_notes_id").toString()));
            if (!success) {
              flash.put("error", "Transaction Failure");
              return redirect;
            }
          } else {
            boolean success = (1 == dialysisSessionNotesDAO.update(con, sessionNtsBean.getMap(),
                "session_notes_id",
                Integer.parseInt(sessionNtsBean.get("session_notes_id").toString())));
            if (!success) {
              flash.put("error", "Transaction Failure");
              return redirect;
            }
          }
        }
      }
      for (int i = 0; i < len; i++) {
        bean = dialysisSessionParametersDAO.getBean();
        ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, bean, errorFields);

        if (observationId[i].startsWith("-") && !observationId[i].equals(discard[i])) {
          bean.set("observation_id", dialysisSessionParametersDAO.getNextSequence());
          bean.set("order_id", Integer.parseInt(req.getParameter("order_id")));
          bean.set("finalized", "N");
          bean.set("saved_by_flag", "U");
          bean.set("obs_type", null);
          boolean success = dialysisSessionParametersDAO.insert(con, bean);
          if (!success) {
            flash.put("put", "Transaction Failure");
            return redirect;
          }

        } else {
          if (discard.length <= observationId.length && discard.length - 1 >= i
              && !observationId[i].startsWith("-") && discard[i].equals(observationId[i])) {
            // Delete Observation
            boolean success = dialysisSessionParametersDAO.delete(con, "observation_id",
                Integer.parseInt(bean.get("observation_id").toString()));
            if (!success) {
              flash.put("error", "Transaction Failure");
              return redirect;
            }
          } else {
            // Update the Post Dialysis Session
            if (!dbFinalizedList.contains(observationId[i]) && bean.get("observation_id") != null
                && !observationId[i].startsWith("-")) {
              if (bean.get("finalized").equals("Y")) {
                bean.set("finalized_by", userId);
                bean.set("finalized_time", DateUtil.getCurrentTimestamp());
              }
              boolean success = (1 == dialysisSessionParametersDAO.update(con, bean.getMap(),
                  "observation_id", Integer.parseInt(bean.get("observation_id").toString())));
              if (!success) {
                flash.put("error", "Transaction Failure");
                return redirect;
              }
            }
          }
        }
      }

      DialysisAdequacyDAO adequacyDao = new DialysisAdequacyDAO();
      Map<String, Map<String, Object>> valuesMap = new HashMap<>();
      BasicDynaBean sessionBean = dialysisSessionsDao.findByKey("order_id",
          Integer.parseInt(req.getParameter("order_id")));

      valuesMap = adequacyDao.getCalculatedKtvandUrr(con, sessionBean.getMap(),
          req.getParameter("mr_no"), userId, null);
      boolean success = adequacyDao.saveKtvAndUrrValues(con, valuesMap, req.getParameter("mr_no"),
          userId);
      if (!success) {
        flash.put("error", "Failed to save adequcy values");
        return redirect;
      }

      String orderId = req.getParameter("order_id");
      BasicDynaBean existsBaen = new DialysisSessionsDao()
          .isPostRecordsExist(Integer.parseInt(orderId));
      if (existsBaen != null) {
        if (paramIds != null && paramValues != null) {
          for (int i = 0; i < paramIds.length; i++) {
            if (!paramIds[i].equals("")) {
              dpvBean = dpvDao.getBean();
              ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, dpvBean, errors);
              dpvBean.set("prep_param_value", paramValues[i]);
              allSuccess = (1 == new DialysisSessionsDao().updateDilaysisPrepDetails(con,
                  paramValues[i], Integer.parseInt(paramIds[i]), Integer.parseInt(orderId)));
            }
          }

        }
      } else {
        if (paramIds != null && paramValues != null) {
          for (int i = 0; i < paramIds.length; i++) {
            if (!paramIds[i].equals("")) {
              dpvBean = dpvDao.getBean();
              ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, dpvBean, errors);
              dpvBean.set("prep_param_id", Integer.parseInt(paramIds[i]));
              dpvBean.set("prep_param_value", paramValues[i]);
              dpvBean.set("order_id", Integer.parseInt(orderId));
              allSuccess = dpvDao.insert(con, dpvBean);
            }
          }

        }
      }
      allSuccess = true;
      flash.put("success", "Intra Dialysis Session updated successfully");
    } finally {
      DataBaseUtil.commitClose(con, allSuccess);
      redirect.addParameter("mr_no", req.getParameter("mr_no"));
      redirect.addParameter("order_id", req.getParameter("order_id"));
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }
}
