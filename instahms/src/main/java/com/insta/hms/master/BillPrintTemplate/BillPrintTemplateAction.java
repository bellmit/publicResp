package com.insta.hms.master.BillPrintTemplate;

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
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BillPrintTemplateAction extends DispatchAction {

	static Logger log = LoggerFactory.getLogger(BillPrintTemplateAction.class);

	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException{
		BillPrintTemplateDAO dao = new BillPrintTemplateDAO();
		req.setAttribute("templateList", dao.getBillTemplateList());
		return m.findForward("list");
	}

	public ActionForward templateMode(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {
		return m.findForward("add");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException{
		JSONSerializer json = new JSONSerializer().exclude("class");
		BillPrintTemplateDAO dao = new BillPrintTemplateDAO();
		String realpath = 	getServlet().getServletContext().getRealPath("");
		req.setAttribute("billDeposits", new BigDecimal(0));
		String templateMode = req.getParameter("templateMode");
		FileInputStream fis = null;
		if (templateMode.equals("H")){
			fis = new FileInputStream(
					new File(realpath+"/WEB-INF/templates/BillPrintTemplate.ftl"));
		}else{
			fis = new FileInputStream(
					new File(realpath+"/WEB-INF/templates/BillPrintTextTemplate.ftl"));
		}
		req.setAttribute("billDeposits", new BigDecimal(0));
		String templateContent = new String(DataBaseUtil.readInputStream(fis));
		req.setAttribute("bill_template_content", templateContent);
		req.setAttribute("prefs", PrintConfigurationsDAO.getPageOptions
				(PrintConfigurationsDAO.PRINT_TYPE_BILL).getMap());
		req.setAttribute("templateNames", json.serialize(dao.getTemplateNames()));
		return m.findForward("addshow");

	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException{
		JSONSerializer json = new JSONSerializer().exclude("class");
		BillPrintTemplateDAO dao = new BillPrintTemplateDAO();
		BasicDynaBean bean = dao.findByKey("template_name", req.getParameter("template_name"));
		req.setAttribute("bill_template_content", bean.get("bill_template_content"));
		req.setAttribute("template_name", bean.get("template_name"));
		req.setAttribute("template_mode", bean.get("template_mode"));
		req.setAttribute("download_content_type", bean.get("download_content_type"));
		req.setAttribute("download_extn", bean.get("download_extn"));
		req.setAttribute("reason", bean.get("reason"));
		req.setAttribute("templateNames", json.serialize(dao.getTemplateNames()));
		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)throws SQLException, IOException{
		BillPrintTemplateDAO dao = new BillPrintTemplateDAO();
		Map paramMap = req.getParameterMap();
		BasicDynaBean bean = dao.getBean();
		List errors = new ArrayList();
		ConversionUtils.copyToDynaBean(paramMap, bean, errors);
		bean.set("user_name", req.getSession(false).getAttribute("userid"));
		String error = null;
		String success = null;
		ActionRedirect redirect = redirect = new ActionRedirect(m.findForward("showRedirect"));
		if (errors.isEmpty()){
			BasicDynaBean exists = dao.findByKey("template_name", bean.get("template_name"));
			Connection con  = null;
			try{
				con = DataBaseUtil.getReadOnlyConnection();
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

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)throws SQLException, IOException{
		Map params = req.getParameterMap();
		BillPrintTemplateDAO billDao = new BillPrintTemplateDAO();
		Object key = req.getParameter("template_name");
		BasicDynaBean bean = billDao.getBean();
		List errors = new ArrayList();
		String success = null;
		String error = null;
		ConversionUtils.copyToDynaBean(params, bean, errors);
		bean.set("user_name", req.getSession(false).getAttribute("userid"));
		Map<String, Object> keys = new HashMap<String, Object>();
		keys.put("template_name", key);
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		if (errors.isEmpty()) {
			Connection con = null;
			try{
				con = DataBaseUtil.getReadOnlyConnection();
				con.setAutoCommit(false);
				int count = billDao.update(con, bean.getMap(), keys);
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

		FlashScope flash = FlashScope.getScope(req);
		flash.put("success", success);
		flash.put("error", error);

		//if (error != null){
			//redirect = new ActionRedirect(m.findForward("showRedirect"));
			//redirect.addParameter("template_name", key);
		//}else {
			//redirect = new ActionRedirect(m.findForward("showRedirect"));
		//}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("template_name", req.getParameter("template_name"));

		return redirect;
	}

	public ActionForward delete(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)throws SQLException, IOException{
		String[] billPrints = req.getParameterValues("deleteBillPrint");
		Map params = req.getParameterMap();
		BillPrintTemplateDAO billDao = new BillPrintTemplateDAO();
		BasicDynaBean bean = billDao.getBean();
		String success = null;
		String error = null;
		boolean suc = true;
		Connection con = null;
			try	{
				con = DataBaseUtil.getReadOnlyConnection();
				con.setAutoCommit(false);
				if (billPrints != null) {
					for (String template_name :  billPrints) {
						if (billDao.delete(con, "template_name",template_name)){
						}else{
							suc = false;
							break;
						}//end inner if
					 }//end for

					if(suc){
						success = ((billPrints.length >1)	?" Bill Prints ":"Bill Print") +
							" Deleted Successfully ";
					}else{
						error = "Failed to Delete Bill prints..";
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
		BillPrintTemplateDAO dao = new BillPrintTemplateDAO();
		String realPath = getServlet().getServletContext().getRealPath("");
		req.setAttribute("billDeposits", new BigDecimal(0));
		FileInputStream fis = null;
		if (templateMode.equals("T")){
		 fis = new FileInputStream(
				new File(realPath+"/WEB-INF/templates/BillPrintTextTemplate.ftl"));
		}else{
		 fis = new FileInputStream(
				new File(realPath+"/WEB-INF/templates/BillPrintTemplate.ftl"));
		}
		String templateContent = new String(DataBaseUtil.readInputStream(fis));

		BasicDynaBean bean = dao.getBean();
		bean.set("template_name", templateName);
		bean.set("bill_template_content", templateContent);
		bean.set("user_name", req.getSession(false).getAttribute("userid"));

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


}


