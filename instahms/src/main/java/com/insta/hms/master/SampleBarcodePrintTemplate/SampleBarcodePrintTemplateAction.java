package com.insta.hms.master.SampleBarcodePrintTemplate;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.File;
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


public class SampleBarcodePrintTemplateAction extends DispatchAction {
	private SampleBarcodePrintTemplateDAO dao = new SampleBarcodePrintTemplateDAO();
	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		request.setAttribute("sampleTemplates", dao.listAll());
		return mapping.findForward("list");
	}
	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException{
		JSONSerializer json = new JSONSerializer().exclude("class");
		SampleBarcodePrintTemplateDAO dao = new SampleBarcodePrintTemplateDAO();
		String realpath = 	getServlet().getServletContext().getRealPath("");
		FileInputStream fis = null;
		fis = new FileInputStream(
				new File(realpath+"/WEB-INF/templates/SampleCollectionBarCodeTextTemplate.ftl"));
		String templateContent = new String(DataBaseUtil.readInputStream(fis));
		req.setAttribute("print_template_content", templateContent);
		req.setAttribute("templateNames", json.serialize(dao.listAll()));
		return m.findForward("addshow");

	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)throws SQLException, IOException{
		SampleBarcodePrintTemplateDAO dao = new SampleBarcodePrintTemplateDAO();
		Map paramMap = req.getParameterMap();
		BasicDynaBean bean = dao.getBean();
		List errors = new ArrayList();
		ConversionUtils.copyToDynaBean(paramMap, bean, errors);
		bean.set("user_name", req.getSession(false).getAttribute("userid"));
		String error = null;
		String success = null;
		ActionRedirect redirect = redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("template_name", bean.get("template_name"));
		if (errors.isEmpty()){
			BasicDynaBean exists = dao.findByKey("template_name", bean.get("template_name"));
			Connection con  = null;
			try{
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				if (exists == null) {
					if (dao.insert(con, bean))
					{
						success = "Template saved successfully..";
						redirect.addParameter("template_name",bean.get("template_name"));
					}
					else error = "Failed to save the Template..";
				}else {
					error = "Template Name already exists...";
				}
			}finally {
				if (success != null) con.commit();
				else con.rollback();
				DataBaseUtil.closeConnections(con, null);
			}
		}else {
			error =  "Incorrectly formatted details supplied..";
		}

		FlashScope flash = FlashScope.getScope(req);
		flash.put("success", success);
		flash.put("error", error);
		if (error != null) {
			redirect = new ActionRedirect(m.findForward("addRedirect"));
			redirect.addParameter("templateMode", req.getParameter("template_mode"));
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse resonse) throws ServletException, IOException, IllegalArgumentException,
			SQLException {
		String template_name = request.getParameter("template_name");
		Boolean customized = new Boolean(request.getParameter("customized"));
		String templateContent = null;
		BasicDynaBean template = dao.findByKey("template_name", template_name);
		if (customized) {
			templateContent = (String) template.get("print_template_content");
		} else {
			String ftlPath = getServlet().getServletContext().getRealPath("/WEB-INF/templates");
			FileInputStream stream = new FileInputStream(ftlPath + "/" + "SampleCollectionBarCodeTextTemplate.ftl");
			templateContent = new String(DataBaseUtil.readInputStream(stream));
		}
		request.setAttribute("template", template);
		request.setAttribute("print_template_content", templateContent);


		return mapping.findForward("addshow");
	}
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {

		Map params = request.getParameterMap();
		String template_name = request.getParameter("template_name");
		BasicDynaBean bean = dao.getBean();
		List errors = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		bean.set("user_name", request.getSession(false).getAttribute("userid"));

		Boolean resetToDefault = new Boolean(request.getParameter("resetToDefault"));
		if (resetToDefault)
			bean.set("print_template_content", "");
		ActionRedirect redirect =new ActionRedirect(mapping.findForward("showRedirect"));
		redirect.addParameter("template_name", template_name);

		String error = null;
		String msg = null;
		if (errors.isEmpty()) {
			Connection con = null;
			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				if (dao.update(con, bean.getMap(), "template_name", template_name) > 0)
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
		redirect.addParameter("customized", request.getParameter("customized"));

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws ServletException, IOException, SQLException {

		String[] deleteSamplePrintsList = request.getParameterValues("deleteSamplePrintList");
		SampleBarcodePrintTemplateDAO dao = new SampleBarcodePrintTemplateDAO();
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		FlashScope flash = FlashScope.getScope(request);
		String message = null;
		Connection con = null;
		boolean success = true;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if (deleteSamplePrintsList != null) {
				for (String templateName : deleteSamplePrintsList) {
					if (!dao.delete(con, "template_name", templateName)) {
						success = false;
						break;
					}
				}
				if (success) {
					message = ((deleteSamplePrintsList.length >1)	?" Sample Bar Code Prints":"Sample Bar Code Print") +
					" Deleted Successfully ";
				} else {
					message = "Failed to Delete Sample Bar Code Prints..";
				}

			}
		} finally {
			if (message != null && success) {
				con.commit();
				flash.put("success", message);
			}
			else if (message != null) {
				con.rollback();
				flash.put("error", message);
			}
			DataBaseUtil.closeConnections(con, null);
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}
