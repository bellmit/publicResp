/**
 *
 */
package com.insta.hms.master.CommonPrintTemplates;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.master.FormComponents.FormComponentsDAO;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
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

/**
 * @author krishna
 *
 */
public class PrintTemplatesAction extends DispatchAction {
	static Logger log = LoggerFactory.getLogger(PrintTemplatesAction.class);
	PrintTemplatesDAO pdao = new PrintTemplatesDAO();
	PatientHeaderTemplateDAO phTemplateDao = new PatientHeaderTemplateDAO();
	
	public ActionForward list(ActionMapping map,ActionForm form,
			HttpServletRequest req,HttpServletResponse res) throws Exception,ServletException {
		req.setAttribute("template_types", PrintTemplate.values());
		req.setAttribute("templateList", pdao.search(req.getParameterMap()));
		return map.findForward("list");
	}

	public ActionForward templateMode(ActionMapping map,ActionForm form,
				HttpServletRequest req,HttpServletResponse res)throws Exception,ServletException {
		req.setAttribute("template_types", PrintTemplate.values());
		return map.findForward("add");
	}

	public ActionForward add(ActionMapping map,ActionForm form,
			HttpServletRequest req,HttpServletResponse res)throws SQLException,IOException {
		JSONSerializer js = new JSONSerializer().exclude("class");
		String realPath = getServlet().getServletContext().getRealPath("");
		String templateMode = req.getParameter("templateMode");

		String template_type = req.getParameter("template_type");
		if (template_type == null || template_type.equals(""))
			throw new IllegalArgumentException("Print Template Type is null");

		PrintTemplate pTemplate = null;
		for (PrintTemplate template: PrintTemplate.values()) {
			if (template.getType().equals(template_type))
				pTemplate = template;
		}

		if (pTemplate == null)
			throw new IllegalArgumentException("Print Template Type does not exists : "+template_type);
		
		String templateType = null;
		if ("InstaGenericForm".equalsIgnoreCase(template_type)){
			templateType = "INSTA_GENERIC_FORM";
		}
		req.setAttribute("phTemplates", phTemplateDao.getTemplates(templateType, "A"));


		FileInputStream fis = null;
		if (templateMode.equals("H")) {
			fis = new FileInputStream(new File(realPath+"/WEB-INF/templates/" +pTemplate.getHmFtlName()+".ftl"));
		} else {
			fis = new FileInputStream(new File(realPath+"/WEB-INF/templates/"+pTemplate.getTmFtlName()+".ftl"));
		}
		String templateContent = new String(DataBaseUtil.readInputStream(fis));
		req.setAttribute("template_content", templateContent);
		req.setAttribute("prefs", PrintConfigurationsDAO.getPageOptions(pTemplate.printType).getMap());
		req.setAttribute("templateNames", js.serialize(pdao.getTemplateNames(pTemplate.type)));
		req.setAttribute("templateMode", templateMode);
		req.setAttribute("template_type", pTemplate.type);
		return map.findForward("addshow");
	}

	public ActionForward show(ActionMapping map,ActionForm form,
			HttpServletRequest req,HttpServletResponse res)throws SQLException,IOException {
		String template_type = req.getParameter("template_type");
		String templateType = null;
		if (template_type == null || template_type.equals(""))
			throw new IllegalArgumentException("Print Template Type is null");

		PrintTemplate pTemplate = null;
		for (PrintTemplate template: PrintTemplate.values()) {
			if (template.getType().equals(template_type))
				pTemplate = template;
		}

		if (pTemplate == null)
			throw new IllegalArgumentException("Print Template Type does not exists : "+template_type);
		
		if ("InstaGenericForm".equalsIgnoreCase(template_type)){
			templateType = "INSTA_GENERIC_FORM";
		}
		req.setAttribute("phTemplates", phTemplateDao.getTemplates(templateType, "A"));

		JSONSerializer json = new JSONSerializer().exclude("class");
		BasicDynaBean bean = pdao.findByKey("template_name", req.getParameter("template_name"));
		req.setAttribute("template_content", bean.get("template_content"));
		req.setAttribute("templateMode", (bean.get("template_mode")).toString());
		req.setAttribute("template_name", bean.get("template_name"));
		req.setAttribute("reason", bean.get("reason"));
		req.setAttribute("template_type", bean.get("template_type"));
		req.setAttribute("templateNames", json.serialize(pdao.getTemplateNames((String) bean.get("template_type"))));
		req.setAttribute("phTemplateId", (Integer) bean.get("pheader_template_id"));
		req.setAttribute("print_template_id", (Integer) bean.get("print_template_id"));
		req.setAttribute("templateMode", (String) bean.get("template_mode"));
		
		return map.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form,
				HttpServletRequest req, HttpServletResponse res) throws SQLException,Exception {
		Map params = req.getParameterMap();
		BasicDynaBean bean = pdao.getBean();
		List errors = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		bean.set("user_name", req.getSession(false).getAttribute("userid"));
		String error = null;
		String success = null;
		ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("showRedirect"));
		if (errors.isEmpty()) {
			BasicDynaBean exists = pdao.findByKey("template_name", bean.get("template_name"));
			Connection con =null;
				try {
					con = DataBaseUtil.getReadOnlyConnection();
					con.setAutoCommit(false);
					if (exists == null) {
						bean.set("print_template_id", DataBaseUtil.getNextSequence("common_print_templates_seq"));
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
		redirect.addParameter("template_type", bean.get("template_type"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException {
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		String key = req.getParameter("print_template_id");
		BasicDynaBean bean = pdao.getBean();
		String error = null;
		String success = null;
		ConversionUtils.copyToDynaBean(params, bean, errors);
		bean.set("user_name", req.getSession(false).getAttribute("userid"));
		Map<String, Object> keys = new HashMap<String, Object>();
		keys.put("print_template_id", Integer.parseInt(key));
		ActionRedirect redirect = new ActionRedirect(m.findForwardConfig("showRedirect"));
		if (errors != null) {
			Connection con = null;
			try {
				con = DataBaseUtil.getReadOnlyConnection();
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
		redirect.addParameter("template_type", bean.get("template_type"));
		return redirect;
	}

	public ActionForward delete(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)throws SQLException, IOException{
		String[] deletePrintTemplates = req.getParameterValues("deletePrintTemplate");
		Map params = req.getParameterMap();
		BasicDynaBean bean = pdao.getBean();
		String success = null;
		String error = null;
		boolean suc = true;
		Connection con = null;
		BasicDynaBean fcBean = null;
			try	{
				con = DataBaseUtil.getReadOnlyConnection();
				con.setAutoCommit(false);
				if (deletePrintTemplates != null) {
					for (String print_template_id :  deletePrintTemplates) {
						fcBean = (BasicDynaBean) new FormComponentsDAO().findByKey("print_template_id", Integer.parseInt(print_template_id));
						if (null == fcBean) {
							if (pdao.delete(con, "print_template_id", Integer.parseInt(print_template_id))) {
							} else {
								suc = false;
								break;
							}//end inner if
						}
					 }//end for
					if (null!= fcBean) {
						error = "Template linked with the form can not be delete...";
					}

					if (suc) {
						success = ((deletePrintTemplates.length >1)	? "Print Templates ":"Print Template ") +
							" Deleted Successfully ";
					} else {
						error = "Failed to Delete Print Templates..";
					}
			 }//end outer if
			} finally {
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
		String template_type = req.getParameter("template_type");
		String templateMode = req.getParameter("template_mode");
		String templateName = req.getParameter("dbtemplate_name");

		PrintTemplate pTemplate = null;
		for (PrintTemplate temp : PrintTemplate.values()) {
			if (temp.getType().equals(template_type)) {
				pTemplate = temp;
			}
		}
		FileInputStream fis = null;
		String realPath = getServlet().getServletContext().getRealPath("");
		if (templateMode.equals("T")){
			fis = new FileInputStream(
				new File(realPath+"/WEB-INF/templates/"+pTemplate.tmFtlName+".ftl"));
		} else{
			fis = new FileInputStream(
				new File(realPath+"/WEB-INF/templates/"+pTemplate.hmFtlName+".ftl"));
		}
		String templateContent = new String(DataBaseUtil.readInputStream(fis));

		BasicDynaBean bean = pdao.getBean();
		bean.set("user_name", req.getSession(false).getAttribute("userid"));
		bean.set("template_name", templateName);
		bean.set("template_content", templateContent);

		ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
		if (templateName.equals("")) {
			// template is in add mode no need to update.
			return redirect;
		}

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

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
	
	public ActionForward getTemplateId(ActionMapping actionmapping, ActionForm actionform,	
			HttpServletRequest req, HttpServletResponse res) throws Exception {

	    String printTemplateId = req.getParameter("printTemplateId");
	    Integer printTempId = null;
	   
		BasicDynaBean bean = (BasicDynaBean) new FormComponentsDAO().findByKey("print_template_id", 
				Integer.parseInt(printTemplateId));
		if (null!=bean) {
			printTempId = (Integer) bean.get("print_template_id");
		}

	    res.setContentType("text/plain");
	    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
	    
	    if(null!=printTempId){
	    	//res.getWriter().write(printTempId);
	    	JSONSerializer js = new JSONSerializer().exclude("class");
	        res.getWriter().write(js.deepSerialize(printTempId));
	        res.flushBuffer();
	    }else{
	    	res.getWriter().write("");
	    }
	    res.flushBuffer();

	    return null;
	}
}
