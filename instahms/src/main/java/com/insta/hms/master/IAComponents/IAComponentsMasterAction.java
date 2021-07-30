/**
 *
 */
package com.insta.hms.master.IAComponents;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.master.DepartmentMaster.DepartmentMasterDAO;
import com.insta.hms.master.Sections.SectionsDAO;

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

/**
 * @author krishna
 *
 */
public class IAComponentsMasterAction extends DispatchAction {

	IAComponentsDAO dao = new IAComponentsDAO();
	SectionsDAO physFormDescDao = new SectionsDAO();
	DepartmentMasterDAO deptDao = new DepartmentMasterDAO();

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ServletException {
		request.setAttribute("componentsList", dao.getComponents());
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ServletException {
		request.setAttribute("depts", deptDao.listAll(null, "dept_name"));
		request.setAttribute("availablePhysForms", physFormDescDao.listAll(null, "status", "A"));
		return mapping.findForward("addshow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ServletException {
		Integer id = Integer.parseInt(request.getParameter("id"));

		request.setAttribute("depts", deptDao.listAll(null, "dept_name"));
		request.setAttribute("record", dao.getComponents(id));
		// get the saved physician forms for this dept and visit type.
		//request.setAttribute("selectedPhysForms", physFormDescDao.getAssessmentPhysForms(id));
		request.setAttribute("availablePhysForms", physFormDescDao.listAll(null, "status", "A"));
		return mapping.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ServletException {
		Map params = request.getParameterMap();
		List errors = new ArrayList();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		if (bean.get("vitals") == null)
			bean.set("vitals", "N");
		String forms = "";
		String[] selectedForms = request.getParameterValues("selected_forms");
		if (selectedForms != null) {
			boolean first = true;
			for (String formId : selectedForms) {
				if (!first) {
					forms += ",";
				}
				forms += formId;
				first = false;
			}
		}
		bean.set("forms", forms);

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		String error = null;
		FlashScope flash = FlashScope.getScope(request);
		if (errors.isEmpty()) {
			if (dao.exists(0, (String) bean.get("form_name"), (String) bean.get("dept_id"))) {
				error = "Record already exists for the selected department.";
				flash.put("error", error);
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
			Connection con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			boolean status = true;
			try {
				int id = dao.getNextSequence();
				bean.set("id", id);
				if (!dao.insert(con, bean)) {
					status = false;
					error = "Failed to insert the Initial Assessment components details.";
				}
			} finally {
				DataBaseUtil.commitClose(con, status);
			}
		} else {
			error = "Incorrectly formatted values supplied";
		}
		if (error != null) {
			flash.put("error", error);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}
		redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		redirect.addParameter("id", bean.get("id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;

	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ServletException {
		Map params = request.getParameterMap();
		List errors = new ArrayList();
		BasicDynaBean bean = dao.getBean();

		ConversionUtils.copyToDynaBean(params, bean, errors);
		if (bean.get("vitals") == null)
			bean.set("vitals", "N");

		String forms = "";
		String[] selectedForms = request.getParameterValues("selected_forms");
		if (selectedForms != null) {
			boolean first = true;
			for (String formId : selectedForms) {
				if (!first) {
					forms += ",";
				}
				forms += formId;
				first = false;
			}
		}
		bean.set("forms", forms);
		Map keys = new HashMap();
		Integer id = Integer.parseInt(request.getParameter("id"));
		keys.put("id", id);

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		String error = null;
		FlashScope flash = FlashScope.getScope(request);
		if (errors.isEmpty()) {
			if (dao.exists(id, (String) bean.get("form_name"), (String) bean.get("dept_id"))) {
				error = "Record already exists for the selected department.";
				flash.put("error", error);
				redirect.addParameter("id", request.getParameter("id"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
			Connection con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			boolean status = true;
			try {
				if (dao.update(con, bean.getMap(), keys) == 0) {
					status = false;
					error = "Failed to update the Initial Assessment components details.";
				}
			} finally {
				DataBaseUtil.commitClose(con, status);
			}
		} else {
			error = "Incorrectly formatted values supplied";
		}
		if (error != null) {
			flash.put("error", error);
		}
		redirect.addParameter("id", bean.get("id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}
