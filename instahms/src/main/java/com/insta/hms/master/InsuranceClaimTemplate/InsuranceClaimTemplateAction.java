package com.insta.hms.master.InsuranceClaimTemplate;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

/**
 * @author pragna.p
 *
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InsuranceClaimTemplateAction extends DispatchAction{

	GenericDAO insclaimtemp = new GenericDAO("insurance_claim_template");

	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException, ParseException{
		Map params = req.getParameterMap();
		Map listingParams = ConversionUtils.getListingParameter(params);
		req.setAttribute("templateList", insclaimtemp.search(params, listingParams, "claim_template_id"));
		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException{
		String realpath = 	getServlet().getServletContext().getRealPath("");

		FileInputStream fis = new FileInputStream(
					new File(realpath+"/WEB-INF/templates/InsuranceClaimTemplate.ftl"));
		String templateContent = new String(DataBaseUtil.readInputStream(fis));
		req.setAttribute("template_content", templateContent);
		return m.findForward("addshow");
	}
	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)throws SQLException, IOException{
		Map paramMap = req.getParameterMap();
		List errors = new ArrayList();

		BasicDynaBean bean = insclaimtemp.getBean();
		ConversionUtils.copyToDynaBean(paramMap, bean, errors);
		String error = null;
		String msg = null;
		if (errors.isEmpty()){
			BasicDynaBean exists = insclaimtemp.findByKey("template_name", bean.get("template_name"));
			System.out.println(exists);
			Connection con  = null;
			try{
				con = DataBaseUtil.getReadOnlyConnection();
				con.setAutoCommit(false);
				if (exists == null) {
					bean.set("claim_template_id",insclaimtemp.getNextSequence());

					if (insclaimtemp.insert(con, bean))
						msg = "Template saved successfully..";
					else error = "Failed to save the Template..";
				}else {
					error = "Template Name already exists...";
				}
			}finally {
				DataBaseUtil.commitClose(con, (msg != null));
			}
		}else {
			error =  "Incorrectly formatted details supplied..";
		}

		FlashScope flash = FlashScope.getScope(req);
		flash.put("success", msg);
		flash.put("error", error);
		ActionRedirect redirect = null;
		if (error != null) {
			redirect = new ActionRedirect(m.findForward("addRedirect"));
		}else {
			redirect = new ActionRedirect(m.findForward("showRedirect"));
			if (bean.get("template_type").equals("P"))
				redirect.addParameter("_editorMode", "tinyMCE");
			else
				redirect.addParameter("_editorMode", "text");
			redirect.addParameter("claim_template_id", bean.get("claim_template_id"));
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}
	public ActionForward delete(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)throws SQLException, IOException{
		String[] claimTemplate = req.getParameterValues("deleteClaimTemplate");
		String msg = null;
		String error = null;
		boolean success = true;
		Connection con = null;
			try	{
				con = DataBaseUtil.getReadOnlyConnection();
				con.setAutoCommit(false);
				if (claimTemplate != null) {
					for (String claim_template_id :  claimTemplate) {
						if (insclaimtemp.delete(con, "claim_template_id",Integer.parseInt(claim_template_id))){
						}else{
							success = false;
							break;
						}//end inner if
					 }//end for

					if(success){
						msg = ((claimTemplate.length >1)	?" Claim Templates ":"Claim Template") +
							" Deleted Successfully ";
					}else{
						error = "Failed to Delete Claim Templates..";
					}
			 }//end outer if
			}finally{
				DataBaseUtil.commitClose(con, (msg!=null));
			}//end finally

		FlashScope flash = FlashScope.getScope(req);
		flash.put("success", msg);
		flash.put("error", error);
		ActionRedirect redirect = null;
		redirect = new ActionRedirect(m.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}
	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException{
		BasicDynaBean bean = insclaimtemp.findByKey("claim_template_id", Integer.parseInt(req.getParameter("claim_template_id")));

		req.setAttribute("template_name", bean.get("template_name"));
		req.setAttribute("template_type", bean.get("template_type"));
		req.setAttribute("status", bean.get("status"));
		req.setAttribute("template_content", bean.get("template_content"));
		req.setAttribute("claim_template_id", bean.get("claim_template_id"));

		return m.findForward("addshow");
	}

	public ActionForward defaultshow(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException{

		String templateType = req.getParameter("templateType");
		String ftl = null;

		if(templateType.equals("P")){
			ftl = "/WEB-INF/templates/InsuranceClaimTemplate.ftl";
		} else{
			ftl =  "/WEB-INF/templates/ClaimTemplateRTF.ftl";
		}

		String realpath = 	getServlet().getServletContext().getRealPath("");
		FileInputStream fis = new FileInputStream(
				new File(realpath+ftl));
		String templateContent = new String(DataBaseUtil.readInputStream(fis));
		req.setAttribute("template_content", templateContent);
		req.setAttribute("template_type", templateType);

		return m.findForward("addshow");
	}
	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)throws SQLException, IOException{

		Map paramMap = req.getParameterMap();
		List errors = new ArrayList();
		ActionRedirect redirect = null;

		String editorMode = req.getParameter("_editorMode");
		BasicDynaBean bean = insclaimtemp.getBean();
		ConversionUtils.copyToDynaBean(paramMap, bean, errors);

		String msg = null;
		String error = null;
		Map<String, Object> keys = new HashMap<String, Object>();
		keys.put("claim_template_id", Integer.parseInt(bean.get("claim_template_id").toString()));

		if (errors.isEmpty()) {
			BasicDynaBean exists = insclaimtemp.findByKey("template_name", bean.get("template_name"));
			if (exists != null && !bean.get("claim_template_id").equals(exists.get("claim_template_id"))) {

				error = "Template Name already exists...";
			} else {

				Connection con = null;
				try{
					con = DataBaseUtil.getReadOnlyConnection();
					con.setAutoCommit(false);
					int count = insclaimtemp.update(con, bean.getMap(), keys);

					if (count >0) msg = "Template updated successfully..";
					else error = "Failed to update the Template..";
				}finally{
					DataBaseUtil.commitClose(con, (msg != null));
				}
			}
		}else {
			error = "Incorrectly formatted details supplied..";
		}

		FlashScope flash = FlashScope.getScope(req);
		flash.put("success", msg);
		flash.put("error", error);

		redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("_editorMode", editorMode);
		redirect.addParameter("claim_template_id", bean.get("claim_template_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}
}
