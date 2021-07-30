package com.insta.hms.ivf;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class IVFPreCycleAction.
 */
public class IVFPreCycleAction extends DispatchAction {
  
  private static final GenericDAO ivfCycleAllergiesDAO = new GenericDAO("ivf_cycle_allergies");

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
    BasicDynaBean preCycleDetails = dao.getIVFPreCycleDetails(Integer.parseInt(ivfCycleID));

    List<BasicDynaBean> allergiesbean = ivfCycleAllergiesDAO.findAllByKey("ivf_cycle_id",
        Integer.parseInt(ivfCycleID));

    request.setAttribute("preCycDetails", preCycleDetails.getMap());
    request.setAttribute("allergieslist",
        js.serialize(ConversionUtils.listBeanToListMap(allergiesbean)));
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
    String mrNo = request.getParameter("mr_no");
    String patientID = request.getParameter("patient_id");
    String precycleCompleted = request.getParameter("precycleCompleted");
    String testDate = request.getParameter("test_date");
    String testTime = request.getParameter("test_time");
    String userName = (String) request.getSession(false).getAttribute("userid");
    String[] allergyIds = request.getParameterValues("allergy_id");
    BasicDynaBean allergiesBean = ivfCycleAllergiesDAO.getBean();
    List<BasicDynaBean> existingAllergiesbean = ivfCycleAllergiesDAO.findAllByKey("ivf_cycle_id",
        Integer.parseInt(ivfCycleID));
    String[] exisitingallergies = new String[existingAllergiesbean.size()];
    for (int i = 0; i < existingAllergiesbean.size(); i++) {
      exisitingallergies[i] = existingAllergiesbean.get(i).get("allergy_id").toString();
    }
    IVFSessionDAO dao = new IVFSessionDAO();
    BasicDynaBean ivfcyclebean = dao.getBean();
    Map requestParams = request.getParameterMap();
    List errors = new ArrayList();
    Boolean success = false;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      ConversionUtils.copyToDynaBean(requestParams, ivfcyclebean, errors);
      ivfcyclebean.set("test_datetime", DateUtil.parseTimestamp(testDate, testTime));
      ivfcyclebean.set("mod_time", DateUtil.getCurrentTimestamp());
      ivfcyclebean.set("username", userName);
      if (null != precycleCompleted && precycleCompleted.equalsIgnoreCase("on")) {
        ivfcyclebean.set("cycle_status", "P");
      }
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("ivf_cycle_id", Integer.parseInt(ivfCycleID));
      success = dao.update(con, ivfcyclebean.getMap(), keys) > 0;
      if (allergyIds != null) {
        for (int k = 0; k < allergyIds.length; k++) {
          if (allergyIds[k] != null && !"".equals(allergyIds[k])) {
            int exist = 0;
            for (int l = 0; l < exisitingallergies.length; l++) {
              if (exisitingallergies[l].equals(allergyIds[k])) {
                exist = 1;
              }
            }
            if (exist == 0) {
              allergiesBean.set("ivf_cycle_id", Integer.parseInt(ivfCycleID));
              allergiesBean.set("allergy_id", Integer.parseInt(allergyIds[k]));
              ivfCycleAllergiesDAO.insert(con, allergiesBean);
            }
          }
        }
      }
      for (int m = 0; m < exisitingallergies.length; m++) {
        Boolean isAllergySelected = false;
        if (allergyIds != null) {
          for (int n = 0; n < allergyIds.length; n++) {
            if (exisitingallergies[m].equals(allergyIds[n])) {
              isAllergySelected = true;
            }
          }
        }
        if (!isAllergySelected) {
          LinkedHashMap<String, Object> allergykeys = new LinkedHashMap<String, Object>();
          allergykeys.put("ivf_cycle_id", Integer.parseInt(ivfCycleID));
          allergykeys.put("allergy_id", Integer.parseInt(exisitingallergies[m]));
          ivfCycleAllergiesDAO.delete(con, allergykeys);
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect;
    if (null != precycleCompleted && precycleCompleted.equalsIgnoreCase("on")) {
      redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    } else {
      redirect = new ActionRedirect(mapping.findForward("showRedirect"));
      redirect.addParameter("ivf_cycle_id", ivfCycleID);
      redirect.addParameter("mr_no", mrNo);
      redirect.addParameter("patient_id", patientID);
    }
    return redirect;
  }
}