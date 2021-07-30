/**
 *
 */
package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.FlashScope;
import com.insta.hms.diagnosticmodule.prescribetest.DiagnoDAOImpl;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna
 *
 */
public class TestReconductionAction extends DispatchAction {

	public ActionForward getReconductTestListScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		String activeStatus = null;
		BasicDynaBean patientDetailsBean = null;
		Map map = request.getParameterMap();

		String patientId = request.getParameter("patientid");
		String mrno = request.getParameter("mrno");
		String orgid = null;
		String presId[] = (String[])map.get("prescId");
		String reportId[] = (String[])map.get("reportId");

		if (patientId !=null && !patientId.equals("")) {
			patientDetailsBean = VisitDetailsDAO.getPatientVisitDetailsBean(patientId);
			if (patientDetailsBean != null) {
				if (patientDetailsBean.get("visit_status").toString().equals("I")) {
					request.setAttribute("info", "Patient is not active.");
					patientId = null;
				}
			} else {
				FlashScope flash = FlashScope.getScope(request);
				flash.put("error", "No Patient with Id:"+patientId);
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("showTestListRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
		} else if ((mrno!=null) && !mrno.equals("")) {
			List visits = VisitDetailsDAO.getPatientVisits(mrno, true);
			if (visits == null || visits.size() == 0) {
				request.setAttribute("error", "Invalid MR Number or patient does not active visits.");
			}
		}

		if (patientDetailsBean != null) {
			patientId = (String)patientDetailsBean.get("patient_id");
			activeStatus = (String)patientDetailsBean.get("visit_status");
			orgid = (String)patientDetailsBean.get("org_id");
			BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
			request.setAttribute("diagGenericPref", diagGenericPref);
			request.setAttribute("activeStatus", activeStatus);
			request.setAttribute("visit_type",(String)patientDetailsBean.get("visit_type"));
		}

		request.setAttribute("patientid",  patientId);
		request.setAttribute("patient",patientDetailsBean);
		if(orgid == null || orgid.equals(""))orgid = "ORG0001";

		String module = mapping.getProperty("category");
		request.setAttribute("module", module);

        if (patientDetailsBean != null){
        	request.setAttribute("presTestList",
        			new DiagnoDAOImpl().getCurrentVisitPrevTestDetailsList(
        					(String)patientDetailsBean.get("patient_id"), module,presId,reportId));
        }

		request.setAttribute("orgidforitem", orgid);
		request.setAttribute("category", module);
		return mapping.findForward("showTestList");
	}

	public ActionForward saveReconductionDetails(ActionMapping mapping,ActionForm form,
			 HttpServletRequest request,HttpServletResponse response) throws Exception {
		boolean status = true;
		Connection con = null;
		String mrno = request.getParameter("mrno");
		String patientid = request.getParameter("patientid");
		String category = request.getParameter("category");
		int commonOrderId = Integer.parseInt(DataBaseUtil.getValue("common_order_seq", "N", ""));

		String msg = null;
		FlashScope flash = FlashScope.getScope(request);

		try {
			Map requestMap = request.getParameterMap();
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			status &= LaboratoryDAO.saveReconductionDetails(con,requestMap,commonOrderId);

			if (status){
				msg ="Saved successfully..";
			}
			else msg = "Transaction failed..";

		} finally {
			if (con!=null) {
				if (status) con.commit();
				else con.rollback();
				con.close();
			}
		}

		ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("showTestListRedirect"));
		redirect.addParameter("mrno", mrno);
		redirect.addParameter("patientid", patientid);
		redirect.addParameter("category", category);
		flash.put("msg", msg);
		redirect.addParameter(FlashScope.FLASH_KEY,flash.key());
		return redirect;
	}

}
