package com.insta.hms.master.HVFPrintTemplate;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HVFPrintTemplateAction extends DispatchAction {

	static Logger log = LoggerFactory.getLogger(HVFPrintTemplateAction.class);

	public ActionForward list(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse responce) throws SQLException, IOException{
		request.setAttribute("templateList", HVFPrintTemplateDAO.gethvfTemplateList());
		return mapping.findForward("list");
	}

	public ActionForward templateMode(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse responce) throws Exception {
		return mapping.findForward("add");
	}

	public ActionForward add(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse responce) throws SQLException, IOException{
		String realpath = 	getServlet().getServletContext().getRealPath("");
		String templateMode = request.getParameter("templateMode");
		FileInputStream fis = null;
		if (templateMode.equals("H")){
			fis = new FileInputStream(
					new File(realpath+"/WEB-INF/templates/PatientHVFDocumentPrint.ftl"));
		}else{
			fis = new FileInputStream(
					new File(realpath+"/WEB-INF/templates/PatientHVFDocumentTextPrint.ftl"));
		}
		String templateContent = new String(DataBaseUtil.readInputStream(fis));
		request.setAttribute("hvf_template_content", templateContent);
		request.setAttribute("prefs", PrintConfigurationsDAO.getPageOptions
				(PrintConfigurationsDAO.PRINT_TYPE_BILL).getMap());
		return mapping.findForward("addshow");

	}

	public ActionForward show(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse responce) throws SQLException, IOException{
		HVFPrintTemplateDAO dao = new HVFPrintTemplateDAO();
		BasicDynaBean bean = dao.findByKey("template_name", request.getParameter("template_name"));
		request.setAttribute("hvf_template_content", bean.get("hvf_template_content"));
		request.setAttribute("template_name", bean.get("template_name"));
		request.setAttribute("template_mode", bean.get("template_mode"));
		request.setAttribute("reason", bean.get("reason"));
		return mapping.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse responce)throws SQLException, IOException{
		HVFPrintTemplateDAO dao = new HVFPrintTemplateDAO();
		Map paramMap = request.getParameterMap();
		BasicDynaBean bean = dao.getBean();
		List errors = new ArrayList();
		ConversionUtils.copyToDynaBean(paramMap, bean, errors);
		bean.set("user_name", request.getSession(false).getAttribute("userid"));
		String error = null;
		String success = null;
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
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

		FlashScope flash = FlashScope.getScope(request);
		flash.put("success", success);
		flash.put("error", error);
		if (error != null) {
			redirect = new ActionRedirect(mapping.findForward("addRedirect"));
			redirect.addParameter("templateMode", request.getParameter("template_mode"));
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse responce)throws SQLException, IOException{
		Map params = request.getParameterMap();
		HVFPrintTemplateDAO dao = new HVFPrintTemplateDAO();
		Object key = request.getParameter("template_name");
		BasicDynaBean bean = dao.getBean();
		List errors = new ArrayList();
		String success = null;
		String error = null;
		ConversionUtils.copyToDynaBean(params, bean, errors);
		bean.set("user_name", request.getSession(false).getAttribute("userid"));
		Map<String, Object> keys = new HashMap<String, Object>();
		keys.put("template_name", key);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		if (errors.isEmpty()) {
			Connection con = null;
			try{
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				int count = dao.update(con, bean.getMap(), keys);
				if (count >0)
				{
					success = "Template updated successfully..";
				}
				else error = "Failed to update the Template..";
			}finally{
				if (success != null) con.commit();
				else con.rollback();
				DataBaseUtil.closeConnections(con, null);
			}
		}else {
			error = "Incorrectly formatted details supplied..";
		}

		FlashScope flash = FlashScope.getScope(request);
		flash.put("success", success);
		flash.put("error", error);

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("template_name", request.getParameter("template_name"));

		return redirect;
	}

	public ActionForward resetToDefault(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException{
		String templateName = req.getParameter("template_name");
		String templateMode = req.getParameter("template_mode");
		HVFPrintTemplateDAO dao = new HVFPrintTemplateDAO();
		String realPath = getServlet().getServletContext().getRealPath("");
		FileInputStream fis = null;
		if (templateMode.equals("T")){
		 fis = new FileInputStream(
				new File(realPath+"/WEB-INF/templates/PatientHVFDocumentPrint.ftl"));
		}else{
		 fis = new FileInputStream(
				new File(realPath+"/WEB-INF/templates/PatientHVFDocumentPrint.ftl"));
		}
		String templateContent = new String(DataBaseUtil.readInputStream(fis));

		BasicDynaBean bean = dao.getBean();
		bean.set("user_name", req.getSession(false).getAttribute("userid"));
		bean.set("template_name", templateName);
		bean.set("hvf_template_content", templateContent);

		Object key = templateName;
		Map<String, Object> keys = new HashMap<String, Object>();
		keys.put("template_name", key);
		Connection con = null;
		String success = null;
		String error = null;
		try{
			con =DataBaseUtil.getConnection();
			if (dao.update(con, bean.getMap(), keys)>0) {
				success = " Template Reset to Default successfully.. ";
			}else{
				error = "Failed to Reset the custom template to Default..";
			}
		}finally{
			DataBaseUtil.closeConnections(con, null);
		}

		FlashScope flash  = FlashScope.getScope(req);
		flash.put("success", success);
		flash.put("error", error);
		ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward delete(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)throws SQLException, IOException{
		String[] hvfTemplate = req.getParameterValues("deleteHVF");
		HVFPrintTemplateDAO dao = new HVFPrintTemplateDAO();
		String success = null;
		String error = null;
		boolean suc = true;
		Connection con = null;
			try	{
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				if (hvfTemplate != null) {
					for (String template_name :  hvfTemplate) {
						if (dao.delete(con, "template_name",template_name)){
						}else{
							suc = false;
							break;
						}
					 }

					if(suc){
						success = ((hvfTemplate.length >1)	?" HVF Prints ":"HVF Print") +
							" Deleted Successfully ";
					}else{
						error = "Failed to Delete HVF prints..";
					}
			 }
			}finally{
				if (success != null) con.commit();
				else con.rollback();
				DataBaseUtil.closeConnections(con, null);
			}

		FlashScope flash = FlashScope.getScope(req);
		flash.put("success", success);
		flash.put("error", error);
		ActionRedirect redirect = null;
		redirect = new ActionRedirect(m.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}
}
