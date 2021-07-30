/**
 *
 */
package com.insta.hms.master.PatientHeaderTemplate;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna.t
 *
 */
public class PatientHeaderTemplateAction extends DispatchAction {

	PatientHeaderTemplateDAO dao = new PatientHeaderTemplateDAO();
	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		request.setAttribute("phTemplates", dao.listAll());
		request.setAttribute("pHeaderTypes", PatientHeaderTemplate.values());
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {

		String template_type = request.getParameter("type");
		if (template_type == null || template_type.equals(""))
			throw new IllegalArgumentException("Patient Header Template Type is null");

		PatientHeaderTemplate pTemplate = null;
		for (PatientHeaderTemplate template: PatientHeaderTemplate.values()) {
			if (template.getType().equals(template_type))
				pTemplate = template;
		}

		if (pTemplate == null)
			throw new IllegalArgumentException("Print Template Type does not exists : "+template_type);

		String ftlPath = getServlet().getServletContext().getRealPath("/WEB-INF/templates/PatientHeaders");
		FileInputStream stream = new FileInputStream(ftlPath + "/" + pTemplate.getFtlName() + ".ftl");
		String templateContent = new String(DataBaseUtil.readInputStream(stream));
		request.setAttribute("templateContent", templateContent);

		if (new Boolean(request.getParameter("resetToDefault")))
			request.setAttribute("success", "Template Reset to Default successfully..");

		request.setAttribute("title", pTemplate.getTitle());
		request.setAttribute("prefs", PrintConfigurationsDAO.getPageOptions(pTemplate.getPrintType()).getMap());
		return mapping.findForward("addshow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		String template_type = request.getParameter("type");
		if (template_type == null || template_type.equals(""))
			throw new IllegalArgumentException("Patient Header Template Type is null");

		PatientHeaderTemplate pTemplate = null;
		for (PatientHeaderTemplate template: PatientHeaderTemplate.values()) {
			if (template.getType().equals(template_type))
				pTemplate = template;
		}

		if (pTemplate == null)
			throw new IllegalArgumentException("Print Template Type does not exists : "+template_type);

		String templateId = request.getParameter("template_id");
		String templateContent = "";
		BasicDynaBean template = null;
		if (templateId == null || templateId.equals("")) {
			String ftlPath = getServlet().getServletContext().getRealPath("/WEB-INF/templates/PatientHeaders");
			FileInputStream stream = new FileInputStream(ftlPath + "/" + pTemplate.getFtlName() + ".ftl");
			templateContent = new String(DataBaseUtil.readInputStream(stream));
		} else {
			template = dao.findByKey("template_id", Integer.parseInt(templateId));
			templateContent = (String) template.get("template_content");
		}
		request.setAttribute("phTemplateDetails", template);
		request.setAttribute("title", pTemplate.getTitle());
		request.setAttribute("prefs", PrintConfigurationsDAO.getPageOptions(pTemplate.getPrintType()).getMap());

		return mapping.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		String type = request.getParameter("type");
		if (type == null || type.equals(""))
			throw new IllegalArgumentException("Patient Header Template Type is null");

		Map params = request.getParameterMap();
		BasicDynaBean bean = dao.getBean();
		List errors = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		bean.set("user_name", request.getSession(false).getAttribute("userid"));
		bean.set("template_id", dao.getNextSequence());
		String error = null;
		String success = null;
		ActionRedirect redirect = null;
		if (errors.isEmpty()) {
			Connection con = null;
			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				if (dao.insert(con, bean))
					success = "Patient Header Template saved successfully..";
				else
					error = "Failed to save the Patient Header Template..";
			} finally {
				if (success != null) {
					con.commit();
					redirect = new ActionRedirect(mapping.findForward("showRedirect"));
					redirect.addParameter("type", type);
					redirect.addParameter("template_id", bean.get("template_id"));
				} else {
					redirect = new ActionRedirect(mapping.findForward("addRedirect"));
					con.rollback();
				}
				DataBaseUtil.closeConnections(con, null);
			}
		} else {
			error = "Incorrectly formatted details supplied..";
		}
		FlashScope flash = FlashScope.getScope(request);
		flash.put("success", success);
		flash.put("error", error);

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		String templateId = request.getParameter("template_id");
		String template_type = request.getParameter("type");
		if (templateId == null || templateId.equals(""))
			throw new IllegalArgumentException("Patient Header Template is null");

		Map params = request.getParameterMap();
		BasicDynaBean bean = dao.getBean();

		List errors = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		bean.set("user_name", request.getSession(false).getAttribute("userid"));
		if (new Boolean(request.getParameter("resetToDefault"))) {
			PatientHeaderTemplate pTemplate = null;
			for (PatientHeaderTemplate template: PatientHeaderTemplate.values()) {
				if (template.getType().equals(template_type))
					pTemplate = template;
			}

			String ftlPath = getServlet().getServletContext().getRealPath("/WEB-INF/templates/PatientHeaders");
			FileInputStream stream = new FileInputStream(ftlPath + "/" + pTemplate.getFtlName() + ".ftl");
			String templateContent = new String(DataBaseUtil.readInputStream(stream));
			bean.set("template_content", templateContent);
		}
		String error = null;
		String success = null;
		ActionRedirect redirect = null;
		if (errors.isEmpty()) {
			Connection con = null;
			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				if (dao.update(con, bean.getMap(), "template_id", Integer.parseInt(templateId)) == 1)
					success = "Template saved successfully..";
				else
					error = "Failed to save the Template..";
			} finally {
				if (success != null) {
					con.commit();
				} else {
					con.rollback();
				}
				DataBaseUtil.closeConnections(con, null);
			}
		} else {
			error = "Incorrectly formatted details supplied..";
		}
		redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		redirect.addParameter("template_id", templateId);
		redirect.addParameter("type", bean.get("type"));
		FlashScope flash = FlashScope.getScope(request);
		flash.put("success", success);
		flash.put("error", error);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
}
