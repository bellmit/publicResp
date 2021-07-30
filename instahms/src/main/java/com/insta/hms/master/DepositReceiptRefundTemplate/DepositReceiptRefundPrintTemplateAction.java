package com.insta.hms.master.DepositReceiptRefundTemplate;

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


public class DepositReceiptRefundPrintTemplateAction extends DispatchAction {

	DepositReceiptRefundPrintTemplateDAO dao = new DepositReceiptRefundPrintTemplateDAO();

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws ServletException, IOException, Exception{

		List templateList = dao.getTemplateList();
		request.setAttribute("templateList", templateList);

		return mapping.findForward("list");
	}

	public ActionForward selectMode(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws ServletException, IOException {

		return mapping.findForward("add");
	}

	public ActionForward addShow(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws ServletException, IOException {

		return null;
	}

	public ActionForward add(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException{

		JSONSerializer json = new JSONSerializer().exclude("class");
		String realpath = 	getServlet().getServletContext().getRealPath("");
		String templateMode = request.getParameter("template_mode");
		FileInputStream fis = null;
		if (templateMode.equals("H")){
			fis = new FileInputStream(
					new File(realpath+"/WEB-INF/templates/DepositReceiptRefundPrint.ftl"));
		}else{
			fis = new FileInputStream(
					new File(realpath+"/WEB-INF/templates/DepositReceiptRefundTextPrint.ftl"));
		}
		String templateContent = new String(DataBaseUtil.readInputStream(fis));
		request.setAttribute("template_content", templateContent);
		request.setAttribute("template_mode", templateMode);
		request.setAttribute("prefs", PrintConfigurationsDAO.getPageOptions
				(PrintConfigurationsDAO.PRINT_TYPE_BILL).getMap());
		request.setAttribute("templateNames", json.serialize(dao.getTemplateNames()));
		return mapping.findForward("addshow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException{
		JSONSerializer json = new JSONSerializer().exclude("class");
		BasicDynaBean bean = dao.findByKey("template_name", request.getParameter("template_name"));
		request.setAttribute("template_content", bean.get("template_content"));
		request.setAttribute("template_name", bean.get("template_name"));
		request.setAttribute("template_mode", bean.get("template_mode"));
		request.setAttribute("reason", bean.get("reason"));
		request.setAttribute("templateBean", bean);
		request.setAttribute("templateNames", json.serialize(dao.getTemplateNames()));
		return mapping.findForward("addshow");
	}

	public ActionForward insertOrUpdate(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws ServletException, IOException, SQLException{

		Connection con = null;
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		String method = request.getParameter("method");
		BasicDynaBean bean = dao.getBean();

		boolean success = true;
		Map params = request.getParameterMap();
		List errorFields = new ArrayList();

		FlashScope flash = FlashScope.getScope(request);
		String templateName = request.getParameter("template_name");

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			con.setAutoCommit(false);
			ConversionUtils.copyToDynaBean(params, bean, errorFields);
			bean.set("user_name", request.getSession(false).getAttribute("userid"));

			if (method.equalsIgnoreCase("insert")) {

				if (errorFields.isEmpty()) {
					success = dao.insert(con, bean);
					if (success)
						flash.put("success", "Details Inserted Successfully");
					else flash.put("error", "Transaction Failed while Inserting");
				}

			} else {
				if (errorFields.isEmpty()) {
					Map keys = new HashMap();
					keys.put("template_name", templateName);
					success = dao.update(con, bean.getMap(), keys) > 0;
					if (success)
						flash.put("success", "Details Updated Successfully");
					else flash.put("error", "Transaction Failed while Updation");
				}

			}
		} finally {
			DataBaseUtil.commitClose(con, success);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			redirect.addParameter("template_name", bean.get("template_name"));
		}

		return redirect;
	}

	public ActionForward delete(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)throws SQLException, IOException{
		String[] printTemplates = req.getParameterValues("deletePrintTemplate");
		String success = null;
		String error = null;
		boolean suc = true;
		Connection con = null;
			try	{
				con = DataBaseUtil.getReadOnlyConnection();
				con.setAutoCommit(false);
				if (printTemplates != null) {
					for (String template_name :  printTemplates) {
						if (dao.delete(con, "template_name",template_name)){
						}else{
							suc = false;
							break;
						}//end inner if
					 }//end for

					if(suc){
						success = ((printTemplates.length >1)	?" Receipt/Refund Prints ":"Receipt/Refund Print") +
							" Deleted Successfully ";
					}else{
						error = "Failed to Delete Receipt/Refund prints..";
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
		//req.setAttribute("billDeposits", new BigDecimal(0));
		FileInputStream fis = null;
		if (templateMode.equals("T")){
		 fis = new FileInputStream(
				new File(realPath+"/WEB-INF/templates/DepositReceiptRefundTextPrint.ftl"));
		}else{
		 fis = new FileInputStream(
				new File(realPath+"/WEB-INF/templates/DepositReceiptRefundPrint.ftl"));
		}
		String templateContent = new String(DataBaseUtil.readInputStream(fis));

		BasicDynaBean bean = dao.getBean();
		bean.set("user_name", req.getSession(false).getAttribute("userid"));
		bean.set("template_name", templateName);
		bean.set("template_content", templateContent);

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
				error = "Failed to Reset the custome template to Default..";
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