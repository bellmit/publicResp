package com.insta.hms.master.BarcodePrintTemplate;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;

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


public class BarcodePrintTemplateAction  extends DispatchAction{


	private BarcodePrintTemplateDAO dao = new BarcodePrintTemplateDAO();
	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		request.setAttribute("printTemplates", dao.listAll("template_type"));
		return mapping.findForward("list");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse resonse) throws ServletException, IOException, IllegalArgumentException,
			SQLException {
		String template_type = request.getParameter("template_type");
		if (template_type == null || template_type.equals(""))
			throw new IllegalArgumentException("Print Template Type is null");

		PrintTemplate pTemplate = null;
		for (PrintTemplate template: PrintTemplate.values()) {
			if (template.getType().equals(template_type))
				pTemplate = template;
		}

		if (pTemplate == null)
			throw new IllegalArgumentException("Print Template Type does not exists : "+template_type);

		Boolean customized = new Boolean(request.getParameter("customized"));
		String templateContent = null;
		if (customized) {
			BasicDynaBean template = dao.findByKey("template_type", pTemplate.getType());
			templateContent = (String) template.get("print_template_content");
		} else {
			String ftlPath = getServlet().getServletContext().getRealPath("/WEB-INF/templates");
			FileInputStream stream = new FileInputStream(ftlPath + "/" + pTemplate.getFtlName() + ".ftl");
			templateContent = new String(DataBaseUtil.readInputStream(stream));
		}
		BasicDynaBean bean = dao.findByKey("template_type", template_type);
		request.setAttribute("reason", bean.get("reason").toString());
		request.setAttribute("print_template_content", templateContent);
		request.setAttribute("prefs", PrintConfigurationsDAO.getPageOptions(pTemplate.getPrintType()).getMap());


		return mapping.findForward("addshow");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		String template_type = request.getParameter("template_type");
		if (template_type == null || template_type.equals(""))
			throw new IllegalArgumentException("Print Template Type is null");

		Map params = request.getParameterMap();
		BasicDynaBean bean = dao.getBean();
		List errors = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		bean.set("user_name", request.getSession(false).getAttribute("userid"));

		Boolean resetToDefault = new Boolean(request.getParameter("resetToDefault"));
		if (resetToDefault)
			bean.set("print_template_content", "");
		ActionRedirect redirect =new ActionRedirect(mapping.findForward("showRedirect"));

		String error = null;
		String msg = null;
		if (errors.isEmpty()) {
			Connection con = null;
			try {
				con = DataBaseUtil.getConnection();
				if (dao.update(con, bean.getMap(), "template_type", template_type) == 1)
					msg = "Template saved successfully..";
				else
					error = "Failed to save the Template..";
			} finally {
				if (msg != null) {
					con.commit();
				} else {
					con.rollback();
				}
				DataBaseUtil.closeConnections(con, null);
			}
		} else {
			error = "Incorrectly formatted details supplied..";
		}
		FlashScope flash = FlashScope.getScope(request);
		flash.put("success", msg);
		flash.put("error", error);
		/*if (msg != null) {
			if (resetToDefault)
				flash.put("success", "Template reset to default successfully..");

			redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		} else {
			redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("title", request.getParameter("title"));
		}*/
		redirect.addParameter("template_type", template_type);
		redirect.addParameter("customized", request.getParameter("customized"));

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}



}
