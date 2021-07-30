package com.insta.hms.master.DiagTemplate;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DiagTemplateAction extends DispatchAction {

	/*
	 * List objects available
	 */
	public ActionForward list(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse resp)
		throws IOException, ServletException, Exception {

		DiagTemplateDAO dao = new DiagTemplateDAO();
		JSONSerializer js = new JSONSerializer();
		Map listingParams = ConversionUtils.getListingParameter(req.getParameterMap());
		String templateName = req.getParameter("templateName");
		String templateDesc = req.getParameter("templateDesc");

		PagedList pagedList = dao.list(req.getParameterMap(), listingParams);
		req.setAttribute("pagedList", pagedList);
        req.setAttribute("testNames", js.serialize(dao.getTestsNames()));
        req.setAttribute("diagDepartmentNames", js.serialize(dao.getDiagDepartmentNames()));
		return m.findForward("list");
	}

	/*
	 * Show an existing object for viewing/editing
	 */
	public ActionForward show(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse resp)
		throws IOException, ServletException, Exception {

		DiagTemplateDAO dao = new DiagTemplateDAO();
		BasicDynaBean bean = (BasicDynaBean) dao.findByKey("testformat_id",
				req.getParameter("testformat_id"));
		BasicDynaBean printPref = PrintConfigurationsDAO.getDiagDefaultPrintPrefs();
		req.setAttribute("bean", bean.getMap());
		req.setAttribute("prefs", printPref.getMap());
		return m.findForward("addshow");
	}

	/*
	 * Show the screen for adding a new object
	 */
	public ActionForward add(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse resp)
		throws IOException, ServletException, Exception {

		BasicDynaBean printPref = PrintConfigurationsDAO.getDiagDefaultPrintPrefs();
		req.setAttribute("prefs", printPref.getMap());
		return m.findForward("addshow");
	}

	/*
	 * POST: update an existing object
	 */
	public ActionForward update(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse resp)
		throws IOException, ServletException, Exception {

		FlashScope flash = FlashScope.getScope(req);

		Map params = req.getParameterMap();
		List errors = new ArrayList();

		DiagTemplateDAO dao = new DiagTemplateDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		String formatId = req.getParameter("testformat_id");

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
			ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("testformat_id", formatId);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

		Connection con = null;
		int success = 0;
		String error = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			// todo: genericDAO method for single key/value (string and int)
			Map<String, String> keys = new HashMap<String, String>();
			keys.put("testformat_id", formatId);
			success = dao.update(con, bean.getMap(), keys);

		} catch (SQLException e) {
			if (DataBaseUtil.isDuplicateViolation(e)) {
				error = "Duplicate Report Name: another report called "
					+ req.getParameter("format_name") + " already exists";
			} else {
				throw (e);
			}

		} finally {
			DataBaseUtil.commitClose(con, (success>0));
		}

		ActionRedirect redirect = null;
		if (success > 0) {
			flash.put("success", "Report Template updated successfully.");

		} else {
			if (error == null)
				error = "Failed to update Report Template: unknown error";
			flash.put("error", error);

		}

		redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("testformat_id", formatId);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward create(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse resp)
		throws IOException, ServletException, Exception {

		FlashScope flash = FlashScope.getScope(req);

		DiagTemplateDAO dao = new DiagTemplateDAO();
		BasicDynaBean bean = dao.getBean();

		List errors = new ArrayList();
		ConversionUtils.copyToDynaBean(req.getParameterMap(), bean, errors);

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
			ActionRedirect redirect = new ActionRedirect(m.findForward("addRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

		Connection con = null;
		boolean success = false;
		String error = null;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			bean.set("testformat_id", dao.getNextId());
			success = dao.insert(con, bean);

		} catch (SQLException e) {
			if (DataBaseUtil.isDuplicateViolation(e))
				error = "Duplicate Report Name: " + req.getParameter("format_name") + " already exists";
			else
				throw (e);
		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		ActionRedirect redirect = null;
		if (success) {
			flash.put("success", "Report Template created successfully.");
			redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("testformat_id", bean.get("testformat_id"));
		} else {
			if (error == null)
				error = "Failed to create Report Template: unknown error";
			flash.put("error", error);
			redirect = new ActionRedirect(m.findForward("addRedirect"));
		}

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}
