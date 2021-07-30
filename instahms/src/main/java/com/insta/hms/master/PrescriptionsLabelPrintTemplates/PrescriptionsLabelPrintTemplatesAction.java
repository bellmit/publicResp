package com.insta.hms.master.PrescriptionsLabelPrintTemplates;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import flexjson.JSONSerializer;

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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PrescriptionsLabelPrintTemplatesAction extends DispatchAction{
	static Logger log = LoggerFactory.getLogger(PrescriptionsLabelPrintTemplatesAction.class);
	PrescriptionsLabelPrintTemplateDAO pdao = new PrescriptionsLabelPrintTemplateDAO();

	public ActionForward list(ActionMapping map,ActionForm form,
				HttpServletRequest req,HttpServletResponse res) throws Exception,ServletException {
			req.setAttribute("templateList", pdao.listAll());
			return map.findForward("list");
	}

	public ActionForward templateMode(ActionMapping map,ActionForm form,
				HttpServletRequest req,HttpServletResponse res)throws Exception,ServletException {
		return map.findForward("add");
	}

	public ActionForward add(ActionMapping map,ActionForm form,
			HttpServletRequest req,HttpServletResponse res)throws SQLException,IOException {
		JSONSerializer js = new JSONSerializer().exclude("class");
		String realPath = getServlet().getServletContext().getRealPath("");
		String templateMode = req.getParameter("templateMode");
		FileInputStream fis = null;
		if (templateMode.equals("H")) {
			fis = new FileInputStream(new File(realPath+"/WEB-INF/templates/PrescriptionLabelTemplate.ftl"));
		} else {
			fis = new FileInputStream(new File(realPath+"/WEB-INF/templates/PrescriptionLabelPrintTextTemplate.ftl"));
		}
		String templateContent = new String(DataBaseUtil.readInputStream(fis));
		req.setAttribute("prescription_lbl_template_content", templateContent);
		req.setAttribute("prefs", PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT).getMap());
		req.setAttribute("templateNames", js.serialize(pdao.getTemplateNames()));
		return map.findForward("addshow");
	}

	public ActionForward show(ActionMapping map,ActionForm form,
			HttpServletRequest req,HttpServletResponse res)throws SQLException,IOException {
		JSONSerializer json = new JSONSerializer().exclude("class");
		BasicDynaBean bean = pdao.findByKey("template_name", req.getParameter("template_name"));
		req.setAttribute("prescription_lbl_template_content", bean.get("prescription_lbl_template_content"));
		req.setAttribute("templateMode", (bean.get("template_mode")).toString());
		req.setAttribute("template_name", bean.get("template_name"));
		req.setAttribute("reason", bean.get("reason"));
		req.setAttribute("templateNames", json.serialize(pdao.getTemplateNames()));
		return map.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form,
				HttpServletRequest req, HttpServletResponse res) throws SQLException,Exception {
		Map params = req.getParameterMap();
		BasicDynaBean bean = pdao.getBean();
		List errors = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		bean.set("user_name", req.getSession(false).getAttribute("userid"));
		bean.set("prescription_lbl_template_content", req.getParameter("prescription_lbl_template_content"));
		String error = null;
		String success = null;
		ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("showRedirect"));
		if (errors.isEmpty()) {
			BasicDynaBean exists = pdao.findByKey("template_name",bean.get("template_name"));
			Connection con =null;
				try {
						con = DataBaseUtil.getConnection();
						con.setAutoCommit(false);
						if (exists == null) {
							if (pdao.insert(con, bean)) {
								success = "Template Saved Successfully";
								redirect.addParameter("template_name", bean.get("template_name"));
							} else {
								error = "Failed To Save The Template";
							}
						} else {
							error = "Template Name Already Exists...";
						}
				} finally {
					if (success != null) {
						con.commit();
					} else {
						con.rollback();
					}
					DataBaseUtil.closeConnections(con, null);
				}
		} else {
			error = "Incorrectly Formatted Data Supplied..";
		}
		FlashScope flash = FlashScope.getScope(req);
		flash.put("success", success);
		flash.put("error", error);
		if (error != null) {
			redirect = new ActionRedirect(mapping.findForwardConfig("addRedirect"));
			redirect.addParameter("templateMode", bean.get("template_mode"));
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException {
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Object key = req.getParameter("template_name");
		BasicDynaBean bean = pdao.getBean();
		String error = null;
		String success = null;
		ConversionUtils.copyToDynaBean(params, bean, errors);
		bean.set("user_name", req.getSession(false).getAttribute("userid"));
		Map<String, Object> keys = new HashMap<String, Object>();
		keys.put("template_name", key);
		ActionRedirect redirect = new ActionRedirect(m.findForwardConfig("showRedirect"));
		if (errors != null) {
			Connection con = null;
			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				int count = pdao.update(con, bean.getMap(), keys);
				if (count > 0) {
					success = "Template Contents are Updated Successfully...";
				} else {
					error ="Failed To Update The Template...";
				}
			} finally {
				if (success!= null) {
					con.commit();
				} else {
					con.rollback();
				}
				DataBaseUtil.closeConnections(con, null);
			}
		} else {
			error = "Incorrectly Formatted data Supplied.....";
		}
		FlashScope flash = FlashScope.getScope(req);
		flash.put("success", success);
		flash.put("error", error);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("template_name", bean.get("template_name"));
		return redirect;
	}

	public ActionForward delete(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)throws SQLException, IOException{
		String[] PrescriptionPrints = req.getParameterValues("deletePrescriptionPrint");
		Map params = req.getParameterMap();
		BasicDynaBean bean = pdao.getBean();
		String success = null;
		String error = null;
		boolean suc = true;
		Connection con = null;
			try	{
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				if (PrescriptionPrints != null) {
					for (String template_name :  PrescriptionPrints) {
						if (pdao.delete(con, "template_name",template_name)){
						}else{
							suc = false;
							break;
						}//end inner if
					 }//end for

					if(suc){
						success = ((PrescriptionPrints.length >1)	?" Prescription Prints ":"Prescription Print") +
							" Deleted Successfully ";
					}else{
						error = "Failed to Delete Prescription prints..";
					}
			 }//end outer if
			}finally{
				if (success != null) con.commit();
				else con.rollback();
				DataBaseUtil.closeConnections(con, null);
			}//end finally

		FlashScope flash = FlashScope.getScope(req);
		flash.put("success", success);
		flash.put("error", error);
		ActionRedirect redirect = null;
		redirect = new ActionRedirect(m.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}
	public ActionForward resetToDefault(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException{
		String templateName = req.getParameter("template_name");
		String templateMode = req.getParameter("template_mode");
		String realPath = getServlet().getServletContext().getRealPath("");
		FileInputStream fis = null;
		if (templateMode.equals("T")){
		 fis = new FileInputStream(
				new File(realPath+"/WEB-INF/templates/PrescriptionLabelPrintTextTemplate.ftl"));
		} else{
			fis = new FileInputStream(
				new File(realPath+"/WEB-INF/templates/PrescriptionLabelTemplate.ftl"));
		}
		String templateContent = new String(DataBaseUtil.readInputStream(fis));

		BasicDynaBean bean = pdao.getBean();
		bean.set("user_name", req.getSession(false).getAttribute("userid"));
		bean.set("template_name", templateName);
		bean.set("prescription_lbl_template_content", templateContent);

		Object key = templateName;
		Map<String, Object> keys = new HashMap<String, Object>();
		keys.put("template_name", key);
		Connection con = null;
		String success = null;
		String error = null;
		try{
			con =DataBaseUtil.getConnection();
			if (pdao.update(con, bean.getMap(), keys)>0) {
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


}



