package com.insta.hms.wardactivities.defineipcareteam;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class IPCareAction.
 *
 * @author sonam
 */
public class IPCareAction extends DispatchAction {
  
  /** The log. */
  static Logger log = LoggerFactory.getLogger(IPCareAction.class);
  
  /** The ipcare DAO. */
  IPCareDAO ipcareDAO = new IPCareDAO();
  
  private static final GenericDAO visitCareTeamDAO = new GenericDAO("visit_care_team");

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   */
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, ServletException {

    String patientId = request.getParameter("visit_id");
    JSONSerializer js = new JSONSerializer().exclude(".class");
    List doctors = Collections.EMPTY_LIST;
    if (patientId != null) {
      request.setAttribute("visitcarelist", ipcareDAO.getVisitCareDetails(patientId));
      doctors = ConversionUtils.copyListDynaBeansToMap(DoctorMasterDAO.getDoctorsAndDepts());
    }
    BasicDynaBean activePatientBean = new VisitDetailsDAO().findByKey("patient_id", patientId);
    String admitDoc = (String) activePatientBean.get("doctor");
    request.setAttribute("admitingDocId", admitDoc);
    request.setAttribute("doctors", js.deepSerialize(doctors));
    request.setAttribute("patient_id", patientId);
    return mapping.findForward("list");
  }

  /**
   * Save.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    String patientId = request.getParameter("patient_id");
    HttpSession session = request.getSession(false);
    String userName = (String) session.getAttribute("userId");
    String[] careId = request.getParameterValues("h_doctor_id");
    String[] isAdded = request.getParameterValues("h_isadded");
    String[] isdeleted = request.getParameterValues("h_delItem");
    FlashScope flash = FlashScope.getScope(request);
    LinkedHashMap<String, Object> keys = new LinkedHashMap<String, Object>();
    Connection con = null;
    Boolean success = false;
    Boolean flag = false;
    List<BasicDynaBean> careBean = (List<BasicDynaBean>) ipcareDAO.findAllByKey("patient_id",
        patientId);
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      for (int i = 0; i < careId.length; i++) {
        Boolean check = false;
        if (isAdded[i].equalsIgnoreCase("true")) {
          for (int k = 0; k < careBean.size(); k++) {
            BasicDynaBean careTeamBean = (BasicDynaBean) careBean.get(k);
            String oldCareId = (String) careTeamBean.get("care_doctor_id");
            if (oldCareId.equals(careId[i])) {
              String newCareId = careId[i];
              ipcareDAO.updateCareDaetaild(con, patientId, oldCareId, newCareId, userName);
              flag = true;
              check = true;
              success = true;
              break;
            }
          }
          if (!check) {
            BasicDynaBean visitCareBean = visitCareTeamDAO.getBean();
            visitCareBean.set("patient_id", patientId);
            visitCareBean.set("care_doctor_id", careId[i]);
            visitCareBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
            visitCareBean.set("username", userName);
            success = visitCareTeamDAO.insert(con, visitCareBean);
            if (!success) {
              break;
            }
            flag = true;
          }
        } else {
          if (isdeleted[i].equalsIgnoreCase("true")) {
            keys.put("patient_id", patientId);
            keys.put("care_doctor_id", careId[i]);
            success = visitCareTeamDAO.delete(con, keys);
            if (!success) {
              break;
            }
            flag = true;
          }
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    if (!success && flag) {
      flash.put("error", "Transaction Failed");
    }
    redirect.addParameter("visit_id", patientId);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

}
