/**
 *
 */
package com.insta.hms.master.DiagnosisStatus;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;

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
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna
 *
 */
public class DiagnosisStatusAction extends DispatchAction {

	DiagnosisStatusDAO dao = new DiagnosisStatusDAO();

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ServletException, ParseException  {
		request.setAttribute("pagedList", dao.search(request.getParameterMap()));
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ServletException  {
		return mapping.findForward("addshow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ServletException  {
		request.setAttribute("bean", dao.findByKey("diagnosis_status_id",
				Integer.parseInt(request.getParameter("diagnosis_status_id"))));
		return mapping.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ServletException  {
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		Map params = request.getParameterMap();
		List errors = new ArrayList();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		String error = null;
		FlashScope flash = FlashScope.getScope(request);
		if (errors.isEmpty()) {
			Connection con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			try {
			if (dao.findByKey(con, "diagnosis_status_name", bean.get("diagnosis_status_name")) != null) {
				flash.error("Diagnosis Status Name '"+bean.get("diagnosi_status_name")+"' already exists");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
			int statusId = dao.getNextSequence();
			bean.set("diagnosis_status_id", statusId);
			if (!dao.insert(con, bean)) {
				error = "Failed to insert the Diagnosis status details..";
			}
			} finally {
				DataBaseUtil.commitClose(con, error == null);
			}
		} else {
			error = "Incorrectly formatted results supplied..";
		}
		flash.error(error);
		redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		redirect.addParameter("diagnosis_status_id", bean.get("diagnosis_status_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ServletException  {

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		Map params = request.getParameterMap();
		List errors = new ArrayList();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		String error = null;
		FlashScope flash = FlashScope.getScope(request);
		if (errors.isEmpty()) {
			Connection con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			try {
			if (dao.statusExists(con, (Integer) bean.get("diagnosis_status_id"), (String) bean.get("diagnosis_status_name"))) {
				flash.error("Diagnosis Status Name '"+bean.get("diagnosi_status_name")+"' already exists");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
			if (dao.update(con, bean.getMap(), "diagnosis_status_id", bean.get("diagnosis_status_id")) == 0) {
				error = "Failed to update the Diagnosis status details..";
			}
			} finally {
				DataBaseUtil.commitClose(con, error == null);
			}
		} else {
			error = "Incorrectly formatted results supplied..";
		}
		flash.error(error);
		redirect.addParameter("diagnosis_status_id", bean.get("diagnosis_status_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}
}
