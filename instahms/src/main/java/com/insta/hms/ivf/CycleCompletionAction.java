package com.insta.hms.ivf;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.RecurrenceDailyMaster.RecurrenceDailyMasterDAO;

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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class CycleCompletionAction.
 */
public class CycleCompletionAction extends DispatchAction {
  
  private static final GenericDAO ivfComplOocyteAssessDAO =
      new GenericDAO("ivf_compl_oocyte_assess");
  
  private static final GenericDAO ivfCompleEmbryoInfDAO = new GenericDAO("ivf_comple_embryo_inf");
  private static final GenericDAO ivfLutealHormoneLevelsDAO =
      new GenericDAO("ivf_luteal_hormone_levels");

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
    String ivfCycleID = request.getParameter("ivf_cycle_id");
    IVFSessionDAO dao = new IVFSessionDAO();

    BasicDynaBean cycleCompBean = dao.findByKey("ivf_cycle_id", Integer.parseInt(ivfCycleID));
    List<BasicDynaBean> oocyteBean = ivfComplOocyteAssessDAO.findAllByKey(
        "ivf_cycle_id", Integer.parseInt(ivfCycleID));
    List<BasicDynaBean> embryoBean = ivfCompleEmbryoInfDAO.findAllByKey(
        "ivf_cycle_id", Integer.parseInt(ivfCycleID));

    request.setAttribute("OOCyteDetails",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(oocyteBean)));
    request.setAttribute("embryoDetails",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(embryoBean)));
    request.setAttribute("CycCompDetails", cycleCompBean.getMap());
    return mapping.findForward("show");
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

    String ivfCycleID = request.getParameter("ivf_cycle_id");
    String rhcguhcgDate = request.getParameter("rhcguhcg_date");
    String rhcguhcgTime = request.getParameter("rhcguhcg_time");

    String cycleCompleted = request.getParameter("completed");

    Map requestparams = request.getParameterMap();
    List errors = new ArrayList();
    IVFSessionDAO dao = new IVFSessionDAO();
    BasicDynaBean ivfCycleBean = dao.getBean();
    Connection con = null;
    Boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      ConversionUtils.copyToDynaBean(requestparams, ivfCycleBean, errors);
      ivfCycleBean.set("rhcg_uhcg_date", DateUtil.parseTimestamp(rhcguhcgDate, rhcguhcgTime));
      if (null != cycleCompleted && cycleCompleted.equalsIgnoreCase("on")) {
        ivfCycleBean.set("cycle_status", "C");
      }
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("ivf_cycle_id", Integer.parseInt(ivfCycleID));
      int updCnt = dao.update(con, ivfCycleBean.getMap(), keys);
      if (updCnt > 0) {
        success = true;
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = null;
    if (null != cycleCompleted && cycleCompleted.equalsIgnoreCase("on")) {
      redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    } else {
      redirect = new ActionRedirect(mapping.findForwardConfig("showRedirect"));
      redirect.addParameter("ivf_cycle_id", ivfCycleID);
      redirect.addParameter("mr_no", request.getParameter("mr_no"));
      redirect.addParameter("patient_id", request.getParameter("patient_id"));
    }
    return redirect;
  }

  /**
   * Gets the oocyte details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the oocyte details
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward getOocyteDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException,
      ParseException, IOException {
    JSONSerializer js = new JSONSerializer().exclude("class");
    String ivfCycleID = request.getParameter("ivf_cycle_id");
    IVFSessionDAO dao = new IVFSessionDAO();
    BasicDynaBean cycComplBean = dao.findByKey("ivf_cycle_id", Integer.parseInt(ivfCycleID));
    List<BasicDynaBean> ooCyteBean = ivfComplOocyteAssessDAO.findAllByKey(
        "ivf_cycle_id", Integer.parseInt(ivfCycleID));
    request.setAttribute("cycComplbean", cycComplBean.getMap());
    request.setAttribute("OOCyteDetails",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(ooCyteBean)));
    return mapping.findForward("OocyteRetrieval");
  }

  /**
   * Gets the luteal details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the luteal details
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward getLutealDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException,
      ParseException, IOException {
    JSONSerializer js = new JSONSerializer().exclude("class");
    String mrNo = request.getParameter("mr_no");
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    request.setAttribute("genericPrefs", genericPrefs.getMap());
    request.setAttribute("frequencies", 
        new RecurrenceDailyMasterDAO().listAll(null, "status", "A"));
    request.setAttribute("visitsList", VisitDetailsDAO.getAllVisitsAndDoctors(mrNo));
    IVFSessionDAO dao = new IVFSessionDAO();
    String ivfCycleID = request.getParameter("ivf_cycle_id");
    List<BasicDynaBean> lutealPrescriptionBean = dao.getLutealPrescriptionDetails(Integer
        .parseInt(ivfCycleID));
    List<BasicDynaBean> lutealharmoneBean =
        ivfLutealHormoneLevelsDAO.findAllByKey("ivf_cycle_id", Integer.parseInt(ivfCycleID));
    request.setAttribute("lutealHarmoneDetails",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(lutealharmoneBean)));
    request.setAttribute("treatmentChart", lutealPrescriptionBean);
    return mapping.findForward("lutealPhaseSupport");
  }

  /**
   * Gets the embryo freezing details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the embryo freezing details
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward getEmbryoFreezingDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException,
      ParseException, IOException {
    JSONSerializer js = new JSONSerializer().exclude("class");
    String ivfCycleID = request.getParameter("ivf_cycle_id");
    BasicDynaBean bean = new IVFSessionDAO()
        .findByKey("ivf_cycle_id", Integer.parseInt(ivfCycleID));
    List<BasicDynaBean> embryoFrozenBean = ivfCompleEmbryoInfDAO.findAllByKey(
        "ivf_cycle_id", Integer.parseInt(ivfCycleID));
    request.setAttribute("embryoFrozenDetails",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(embryoFrozenBean)));
    request.setAttribute("cycComplBean", bean.getMap());
    return mapping.findForward("embryoFreezingDetails");
  }

  /**
   * Gets the embryo transfer details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the embryo transfer details
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward getEmbryoTransferDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException,
      ParseException, IOException {
    JSONSerializer js = new JSONSerializer().exclude("class");
    String ivfCycleID = request.getParameter("ivf_cycle_id");
    BasicDynaBean bean = new IVFSessionDAO()
        .findByKey("ivf_cycle_id", Integer.parseInt(ivfCycleID));
    List<BasicDynaBean> embryoTransferBean = ivfCompleEmbryoInfDAO.findAllByKey(
        "ivf_cycle_id", Integer.parseInt(ivfCycleID));
    request.setAttribute("embryoTransferDetails",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(embryoTransferBean)));
    request.setAttribute("cycComplBean", bean.getMap());
    return mapping.findForward("embryoTransferDetails");
  }

  /**
   * Save oo cyte details.
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
  public ActionForward saveOoCyteDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException,
      ParseException, IOException {
    String ivfCycleID = request.getParameter("ivf_cycle_id");
    String[] otype = request.getParameterValues("otype");
    String[] onumber = request.getParameterValues("onumber");
    String[] odeleted = request.getParameterValues("odeleted");
    Map requestParams = request.getParameterMap();
    IVFSessionDAO dao = new IVFSessionDAO();
    BasicDynaBean bean = dao.findByKey("ivf_cycle_id", Integer.parseInt(ivfCycleID));
    ArrayList errors = null;
    ConversionUtils.copyToDynaBean(requestParams, bean, errors);
    Connection con = null;
    Boolean success = false;
    String opuDate = request.getParameter("opuDate");
    String startTime = request.getParameter("startTime");
    String endTime = request.getParameter("endTime");
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      HashMap<String, Object> keys = new HashMap<String, Object>();
      keys.put("ivf_cycle_id", Integer.parseInt(ivfCycleID));
      bean.set("opu_start_time", DateUtil.parseTimestamp(opuDate, startTime));
      bean.set("opu_end_time", DateUtil.parseTimestamp(opuDate, endTime));
      if (opuDate != null && !opuDate.equals("")) {
        bean.set(
            "opu_duration",
            DateUtil.getHours(DateUtil.parseTimestamp(opuDate, startTime),
                DateUtil.parseTimestamp(opuDate, endTime), false));
      }
      int updCnt = dao.update(con, bean.getMap(), keys);
      if (updCnt > 0) {
        success = true;
      }
      ivfComplOocyteAssessDAO.delete(con, "ivf_cycle_id", Integer.parseInt(ivfCycleID));
      BasicDynaBean obean = ivfComplOocyteAssessDAO.getBean();
      obean.set("ivf_cycle_id", Integer.parseInt(ivfCycleID));
      if (null != onumber && null != otype) {
        for (int i = 0; i < otype.length; i++) {
          if (odeleted[i].equalsIgnoreCase("false") && !onumber[i].equals("")
              && !otype[i].equals("")) {
            obean.set("oocyte_number", new BigDecimal(onumber[i]));
            obean.set("oocyte_type", otype[i]);
            success = ivfComplOocyteAssessDAO.insert(con, obean);
          }
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("OocyteRedirect"));
    redirect.addParameter("ivf_cycle_id", ivfCycleID);
    String mrNo = request.getParameter("mr_no");
    redirect.addParameter("mr_no", mrNo);
    String patientId = request.getParameter("patient_id");
    redirect.addParameter("patient_id", patientId);
    return redirect;
  }

  /**
   * Save embryo transfer details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public ActionForward saveEmbryoTransferDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
      ParseException {
    String ivfCycleID = request.getParameter("ivf_cycle_id");
    String[] etnumber = request.getParameterValues("etnumber");
    String[] etgrade = request.getParameterValues("etgrade");
    String[] tdeleted = request.getParameterValues("tdeleted");
    String[] embryoidt = request.getParameterValues("embryoidt");

    Map requestParams = request.getParameterMap();
    IVFSessionDAO dao = new IVFSessionDAO();
    BasicDynaBean bean = dao.findByKey("ivf_cycle_id", Integer.parseInt(ivfCycleID));
    ArrayList errors = null;
    ConversionUtils.copyToDynaBean(requestParams, bean, errors);

    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      HashMap<String, Object> keys = new HashMap<String, Object>();
      keys.put("ivf_cycle_id", Integer.parseInt(ivfCycleID));
      int updCnt = dao.update(con, bean.getMap(), keys);
      if (updCnt > 0) {
        success = true;
      }
      BasicDynaBean embryoBean = ivfCompleEmbryoInfDAO.getBean();
      if (etnumber != null && etgrade != null) {
        for (int k = 0; k < etnumber.length; k++) {
          if (!etnumber[k].equals("") && !etgrade[k].equals("")) {
            embryoBean.set("ivf_cycle_id", Integer.parseInt(ivfCycleID));
            embryoBean.set("embryo_op", "T");
            embryoBean.set("emb_number", Integer.parseInt(etnumber[k]));
            embryoBean.set("emb_grade", etgrade[k]);

            if (tdeleted[k].equalsIgnoreCase("false") && embryoidt[k].equals("")) {
              int embryoID = ivfCompleEmbryoInfDAO.getNextSequence();
              embryoBean.set("ivf_cycle_embryo_id", embryoID);
              success = ivfCompleEmbryoInfDAO.insert(con, embryoBean);
            }
            if (tdeleted[k].equalsIgnoreCase("false") && !embryoidt[k].equals("")) {
              embryoBean.set("ivf_cycle_embryo_id", Integer.parseInt(embryoidt[k]));
              Map<String, Object> tkeys = new HashMap<String, Object>();
              tkeys.put("ivf_cycle_embryo_id", Integer.parseInt(embryoidt[k]));
              tkeys.put("ivf_cycle_id", Integer.parseInt(ivfCycleID));
              int updCount = ivfCompleEmbryoInfDAO.update(con,
                  embryoBean.getMap(), tkeys);
              if (updCount > 0) {
                success = success || true;
              }
            }
            if (tdeleted[k].equalsIgnoreCase("true") && !embryoidt[k].equals("")) {
              embryoBean.set("ivf_cycle_embryo_id", Integer.parseInt(embryoidt[k]));
              LinkedHashMap<String, Object> tkeys = new LinkedHashMap<String, Object>();
              tkeys.put("ivf_cycle_embryo_id", Integer.parseInt(embryoidt[k]));
              tkeys.put("ivf_cycle_id", Integer.parseInt(ivfCycleID));
              success = ivfCompleEmbryoInfDAO.delete(con, tkeys);
            }
          }
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("embryoTransferRedirect"));
    redirect.addParameter("ivf_cycle_id", ivfCycleID);
    String mrNo = request.getParameter("mr_no");
    redirect.addParameter("mr_no", mrNo);
    String patientId = request.getParameter("patient_id");
    redirect.addParameter("patient_id", patientId);
    return redirect;
  }

  /**
   * Save luteal details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public ActionForward saveLutealDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
      ParseException {
    String ivfCycleID = request.getParameter("ivf_cycle_id");
    String[] betahcg = request.getParameterValues("betahcg");
    String[] prog = request.getParameterValues("prog");
    String[] e2 = request.getParameterValues("e2");
    String[] lhldeleted = request.getParameterValues("lhldeleted");
    String userName = (String) request.getSession(false).getAttribute("userid");
    String mrNo = request.getParameter("mr_no");
    String patientID = request.getParameter("patient_id");
    Map requestparams = request.getParameterMap();
    Connection con = null;
    Boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      ivfLutealHormoneLevelsDAO.delete(con, "ivf_cycle_id",
          Integer.parseInt(ivfCycleID));
      BasicDynaBean bean = ivfLutealHormoneLevelsDAO.getBean();
      bean.set("ivf_cycle_id", Integer.parseInt(ivfCycleID));
      if (null != betahcg && null != prog) {
        for (int i = 0; i < betahcg.length; i++) {
          if (lhldeleted[i].equalsIgnoreCase("false") && !e2[i].equals("")
              && !betahcg[i].equals("") && !prog[i].equals("")
              && !request.getParameter("lhdate" + i).equals("")) {
            bean.set("beta_hcg_value", new BigDecimal(betahcg[i]));
            bean.set("prog_value", new BigDecimal(prog[i]));
            bean.set("blood_test_date", DateUtil.parseDate(request.getParameter("lhdate" + i)));
            bean.set("e2_value", new BigDecimal(e2[i]));
            success = ivfLutealHormoneLevelsDAO.insert(con, bean);
          }
        }
      }
      AbstractPrescriptionDetails presc = new PrescriptionAbstractImpl();
      success = presc.savePrescriptionDetails(con, requestparams, userName, mrNo, patientID,
          Integer.parseInt(ivfCycleID), "cycleCompletion");
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("lutealRedirect"));
    redirect.addParameter("ivf_cycle_id", ivfCycleID);
    redirect.addParameter("mr_no", mrNo);
    redirect.addParameter("patient_id", patientID);
    return redirect;
  }

  /**
   * Save embryo freezing details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public ActionForward saveEmbryoFreezingDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
      ParseException {
    String ivfCycleID = request.getParameter("ivf_cycle_id");
    BasicDynaBean embryoBean = ivfCompleEmbryoInfDAO.getBean();
    String[] efstate = request.getParameterValues("efstate");
    String[] efgrade = request.getParameterValues("efgrade");
    String[] efnumber = request.getParameterValues("efnumber");
    String[] fdeleted = request.getParameterValues("fdeleted");
    String[] embryoidf = request.getParameterValues("embryoidf");
    Map requestparams = request.getParameterMap();
    BasicDynaBean bean = new IVFSessionDAO().getBean();
    ArrayList errors = null;
    ConversionUtils.copyToDynaBean(requestparams, bean, errors);
    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("ivf_cycle_id", Integer.parseInt(ivfCycleID));
      int updateCnt = new IVFSessionDAO().update(con, bean.getMap(), keys);
      if (updateCnt > 0) {
        success = true;
      }
      if (efnumber != null && efstate != null && efgrade != null) {
        for (int k = 0; k < efnumber.length; k++) {
          if (!efgrade[k].equals("") && !efnumber[k].equals("") && !efstate[k].equals("")) {
            embryoBean.set("ivf_cycle_id", Integer.parseInt(ivfCycleID));
            embryoBean.set("embryo_op", "F");
            embryoBean.set("emb_number", Integer.parseInt(efnumber[k]));
            embryoBean.set("emb_state", efstate[k]);
            embryoBean.set("emb_grade", efgrade[k]);

            if (fdeleted[k].equalsIgnoreCase("false") && embryoidf[k].equals("")) {
              int embryoID = ivfCompleEmbryoInfDAO.getNextSequence();
              embryoBean.set("ivf_cycle_embryo_id", embryoID);
              success = ivfCompleEmbryoInfDAO.insert(con, embryoBean);
            }
            if (fdeleted[k].equalsIgnoreCase("false") && !embryoidf[k].equals("")) {
              embryoBean.set("ivf_cycle_embryo_id", Integer.parseInt(embryoidf[k]));
              Map<String, Object> tkeys = new HashMap<String, Object>();
              tkeys.put("ivf_cycle_embryo_id", Integer.parseInt(embryoidf[k]));
              tkeys.put("ivf_cycle_id", Integer.parseInt(ivfCycleID));
              int comEmbryoList = ivfCompleEmbryoInfDAO.update(con,
                  embryoBean.getMap(), tkeys);
              if (comEmbryoList > 0) {
                success = true;
              }
            }
            if (fdeleted[k].equalsIgnoreCase("true") && !embryoidf[k].equals("")) {
              embryoBean.set("ivf_cycle_embryo_id", Integer.parseInt(embryoidf[k]));
              LinkedHashMap<String, Object> fkeys = new LinkedHashMap<String, Object>();
              fkeys.put("ivf_cycle_embryo_id", Integer.parseInt(embryoidf[k]));
              fkeys.put("ivf_cycle_id", Integer.parseInt(ivfCycleID));
              success = ivfCompleEmbryoInfDAO.delete(con, fkeys);
            }
          }
        }
      }

    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("embryoFreezingRedirect"));
    redirect.addParameter("ivf_cycle_id", ivfCycleID);
    String mrNo = request.getParameter("mr_no");
    String patientID = request.getParameter("patient_id");
    redirect.addParameter("mr_no", mrNo);
    redirect.addParameter("patient_id", patientID);
    return redirect;
  }

}