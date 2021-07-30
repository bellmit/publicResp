package com.insta.hms.ivf;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.RecurrenceDailyMaster.RecurrenceDailyMasterDAO;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator.Fitness;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class DailyTreatmentAction.
 */
public class DailyTreatmentAction extends DispatchAction {
  
  private static final GenericDAO ivfDailyDetailsDAO = new GenericDAO("ivf_daily_details");
  private static final GenericDAO ivfDailyFolliclesDAO = new GenericDAO("ivf_daily_follicles");
  private static final GenericDAO ivfDailyHormoneResults =
      new GenericDAO("ivf_daily_hormone_results");
  
  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ParseException, IOException {
    String ivfCycleID = request.getParameter("ivf_cycle_id");
    IVFSessionDAO dao = new IVFSessionDAO();
    List<BasicDynaBean> follicleBean = ivfDailyFolliclesDAO.listAll();
    String dailyTreatCompleted = null;

    if (null != dao.findByKey("ivf_cycle_id", Integer.parseInt(ivfCycleID)).get("cycle_status")) {
      dailyTreatCompleted = dao.findByKey("ivf_cycle_id", Integer.parseInt(ivfCycleID))
          .get("cycle_status").toString();
    }
    request.setAttribute("follicleList", follicleBean);
    request.setAttribute("prescriptionList", dao.getPrescriptionList(Integer.parseInt(ivfCycleID)));
    List<BasicDynaBean> dailyTreatmentbean = dao
        .getDailyTreatmentList(Integer.parseInt(ivfCycleID));
    request.setAttribute("dailyTreatmentlist", dailyTreatmentbean);
    request.setAttribute("dailyTreatCompleted", dailyTreatCompleted);
    return mapping.findForward("list");
  }

  /**
   * Adds the.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ParseException, IOException {
    JSONSerializer js = new JSONSerializer().exclude("class");
    String doctors = js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(IVFSessionDAO
        .getDoctorsList()));
    request.setAttribute("doctorsList", doctors);
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    request.setAttribute("genericPrefs", genericPrefs.getMap());
    Map filterMap = new HashMap<>();
    filterMap.put("status", "A");
    filterMap.put("medication_type", "M");
    request.setAttribute("frequencies", 
        new RecurrenceDailyMasterDAO().listAll(null,filterMap , null));
    String mrNo = request.getParameter("mr_no");
    request.setAttribute("visitsList", VisitDetailsDAO.getAllVisitsAndDoctors(mrNo));
    request.setAttribute("follicleDetails", js.serialize(Collections.EMPTY_LIST));
    return mapping.findForward("addshow");
  }

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ParseException, IOException {
    JSONSerializer js = new JSONSerializer().exclude("class");
    String ivfCycleDailyID = request.getParameter("ivf_cycle_daily_id");
    String mrNo = request.getParameter("mr_no");
    IVFSessionDAO dao = new IVFSessionDAO();
    String doctors = js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(IVFSessionDAO
        .getDoctorsList()));
    request.setAttribute("doctorsList", doctors);
    request.setAttribute("treatmentChart",
        dao.getPrescriptionDetails(Integer.parseInt(ivfCycleDailyID)));
    request.setAttribute("mrNo", mrNo);
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    request.setAttribute("genericPrefs", genericPrefs.getMap());
    Map filterMap = new HashMap<>();
    filterMap.put("status", "A");
    filterMap.put("medication_type", "M");
    request.setAttribute("frequencies", 
        new RecurrenceDailyMasterDAO().listAll(null, filterMap, null));
    request.setAttribute("visitsList", VisitDetailsDAO.getAllVisitsAndDoctors(mrNo));

    List<BasicDynaBean> folliclesBean = ivfDailyFolliclesDAO.findAllByKey(
        "ivf_cycle_daily_id", Integer.parseInt(ivfCycleDailyID));
    request.setAttribute("follicleDetails",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(folliclesBean)));
    BasicDynaBean dailyTreatmentbean = dao.getDailyTreatmentDetails(Integer
        .parseInt(ivfCycleDailyID));
    request.setAttribute("dailyTreatDetails", dailyTreatmentbean.getMap());
    return mapping.findForward("addshow");
  }

  /**
   * Creates the.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ParseException, IOException {
    String userName = (String) request.getSession(false).getAttribute("userid");
    String ivfCycleID = request.getParameter("ivf_cycle_id");
    String mrNo = request.getParameter("mr_no");
    String patientID = request.getParameter("patient_id");
    String doctorID = request.getParameter("doctor_id");

    String[] lcount = request.getParameterValues("lcount");
    String[] lsize = request.getParameterValues("lsize");
    String[] rcount = request.getParameterValues("rcount");
    String[] rsize = request.getParameterValues("rsize");
    String[] ldeleted = request.getParameterValues("ldeleted");
    String[] rdeleted = request.getParameterValues("rdeleted");
    int ivfCycleDailyID = ivfDailyDetailsDAO.getNextSequence();

    BasicDynaBean dailyBean = ivfDailyDetailsDAO.getBean();
    Map requestparams = request.getParameterMap();
    List errors = new ArrayList();
    Connection con = null;
    Boolean success = false;
    try {
      ConversionUtils.copyToDynaBean(requestparams, dailyBean, errors);
      dailyBean.set("ivf_cycle_id", Integer.parseInt(ivfCycleID));
      dailyBean.set("ivf_cycle_daily_id", ivfCycleDailyID);
      dailyBean.set("doctor", doctorID);
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      success = ivfDailyDetailsDAO.insert(con, dailyBean);
      if (success) {
        Boolean succ = false;
        BasicDynaBean hormoneBean = ivfDailyHormoneResults.getBean();
        ConversionUtils.copyToDynaBean(requestparams, hormoneBean, errors);
        hormoneBean.set("ivf_cycle_daily_id", ivfCycleDailyID);
        succ = ivfDailyHormoneResults.insert(con, hormoneBean);
        BasicDynaBean folliclesBean = ivfDailyFolliclesDAO.getBean();
        if (lcount != null && lsize != null) {
          for (int i = 0; i < lcount.length; i++) {
            if (ldeleted[i].equalsIgnoreCase("false") && !lcount[i].equals("")
                && !lsize[i].equals("")) {
              folliclesBean.set("ivf_cycle_daily_id", ivfCycleDailyID);
              folliclesBean.set("ovary_position", "L");
              folliclesBean.set("follicles_count", Integer.parseInt(lcount[i]));
              folliclesBean.set("follicles_size", new BigDecimal(lsize[i]));
              succ = ivfDailyFolliclesDAO.insert(con, folliclesBean);
            }
          }
        }
        if (rcount != null && rsize != null && !rcount.equals("") && !rsize.equals("")) {
          for (int i = 0; i < rcount.length; i++) {
            if (rdeleted[i].equalsIgnoreCase("false") && !rcount[i].equals("")
                && !rsize[i].equals("")) {
              folliclesBean.set("ivf_cycle_daily_id", ivfCycleDailyID);
              folliclesBean.set("ovary_position", "R");
              folliclesBean.set("follicles_count", Integer.parseInt(rcount[i]));
              folliclesBean.set("follicles_size", new BigDecimal(rsize[i]));
              succ = ivfDailyFolliclesDAO.insert(con, folliclesBean);
            }
          }
        }

        AbstractPrescriptionDetails presc = new PrescriptionAbstractImpl();
        succ = presc.savePrescriptionDetails(con, requestparams, userName, mrNo, patientID,
            ivfCycleDailyID, "dailyTreatment");

        success = succ || success;
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect;
    redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    redirect.addParameter("ivf_cycle_id", ivfCycleID);
    redirect.addParameter("ivf_cycle_daily_id", ivfCycleDailyID);
    redirect.addParameter("mr_no", mrNo);
    redirect.addParameter("patient_id", patientID);
    String startDate = request.getParameter("start_date");
    redirect.addParameter("start_date", startDate);
    return redirect;
  }

  /**
   * Update.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ParseException, IOException {
    String userName = (String) request.getSession(false).getAttribute("userid");
    String ivfCycleID = request.getParameter("ivf_cycle_id");
    String ivfCycleDailyID = request.getParameter("ivf_cycle_daily_id");
    String mrNo = request.getParameter("mr_no");
    String patientID = request.getParameter("patient_id");
    String doctorID = request.getParameter("doctor_id");

    String[] lcount = request.getParameterValues("lcount");
    String[] lsize = request.getParameterValues("lsize");
    String[] rcount = request.getParameterValues("rcount");
    String[] rsize = request.getParameterValues("rsize");
    String[] ldeleted = request.getParameterValues("ldeleted");
    String[] rdeleted = request.getParameterValues("rdeleted");

    Map requsetparams = request.getParameterMap();
    List errors = new ArrayList();
    BasicDynaBean dailyBean = ivfDailyDetailsDAO.getBean();
    Connection con = null;
    Boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      ConversionUtils.copyToDynaBean(requsetparams, dailyBean, errors);
      dailyBean.set("ivf_cycle_daily_id", Integer.parseInt(ivfCycleDailyID));
      dailyBean.set("ivf_cycle_id", Integer.parseInt(ivfCycleID));
      dailyBean.set("doctor", doctorID);
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("ivf_cycle_daily_id", Integer.parseInt(ivfCycleDailyID));
      keys.put("ivf_cycle_id", Integer.parseInt(ivfCycleID));
      int updateCnt = ivfDailyDetailsDAO.update(con, dailyBean.getMap(), keys);
      if (updateCnt > 0) {
        success = true;
      }
      if (success) {
        Boolean succ = false;
        BasicDynaBean harmoneBean = ivfDailyHormoneResults.getBean();
        ConversionUtils.copyToDynaBean(requsetparams, harmoneBean, errors);
        Map<String, Object> hkeys = new HashMap<String, Object>();
        hkeys.put("ivf_cycle_daily_id", Integer.parseInt(ivfCycleDailyID));
        int updCnt = ivfDailyHormoneResults.update(con, harmoneBean.getMap(), hkeys);
        if (updCnt > 0) {
          succ = true;
        }
        success = ivfDailyFolliclesDAO.delete(con, "ivf_cycle_daily_id",
            Integer.parseInt(ivfCycleDailyID));
        BasicDynaBean folliclesBean = ivfDailyFolliclesDAO.getBean();
        if (lcount != null && lsize != null) {
          for (int l = 0; l < lcount.length; l++) {
            if (ldeleted[l].equalsIgnoreCase("false") && !lcount[l].equals("")
                && !lsize[l].equals("")) {
              folliclesBean.set("ivf_cycle_daily_id", Integer.parseInt(ivfCycleDailyID));
              folliclesBean.set("ovary_position", "L");
              folliclesBean.set("follicles_count", Integer.parseInt(lcount[l]));
              folliclesBean.set("follicles_size", new BigDecimal(lsize[l]));
              succ = ivfDailyFolliclesDAO.insert(con, folliclesBean);
            }
          }
        }
        if (rcount != null && rsize != null) {
          for (int m = 0; m < rcount.length; m++) {
            if (rdeleted[m].equalsIgnoreCase("false") && !rcount[m].equals("")
                && !rsize[m].equals("")) {
              folliclesBean.set("ivf_cycle_daily_id", Integer.parseInt(ivfCycleDailyID));
              folliclesBean.set("ovary_position", "R");
              folliclesBean.set("follicles_count", Integer.parseInt(rcount[m]));
              folliclesBean.set("follicles_size", new BigDecimal(rsize[m]));
              succ = ivfDailyFolliclesDAO.insert(con, folliclesBean);
            }
          }
        }
        AbstractPrescriptionDetails presc = new PrescriptionAbstractImpl();
        succ = presc.savePrescriptionDetails(con, requsetparams, userName, mrNo, patientID,
            Integer.parseInt(ivfCycleDailyID), "dailyTreatment");
        success = succ || success;
      }

    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = null;
    redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    redirect.addParameter("mr_no", mrNo);
    redirect.addParameter("patient_id", patientID);
    redirect.addParameter("ivf_cycle_id", ivfCycleID);
    redirect.addParameter("ivf_cycle_daily_id", ivfCycleDailyID);
    String startDate = request.getParameter("start_date");
    redirect.addParameter("start_date", startDate);
    return redirect;
  }

  /**
   * Update cycle status.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward updateCycleStatus(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException,
      ParseException, IOException {
    String ivfCycleID = request.getParameter("ivf_cycle_id");
    String dailyTreatCompleted = request.getParameter("dailytreatCompleted");
    Connection con = null;
    Boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      IVFSessionDAO dao = new IVFSessionDAO();
      BasicDynaBean bean = dao.getBean();
      Map<String, Object> keys = new HashMap<String, Object>();
      bean.set("ivf_cycle_id", Integer.parseInt(ivfCycleID));
      keys.put("ivf_cycle_id", Integer.parseInt(ivfCycleID));

      if (null != dailyTreatCompleted && dailyTreatCompleted.equalsIgnoreCase("on")) {
        bean.set("cycle_status", "D");
      }
      int updCnt = dao.update(con, bean.getMap(), keys);
      if (updCnt > 0) {
        success = true;
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = null;
    redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    return redirect;
  }
}