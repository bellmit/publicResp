package com.insta.hms.master.AllergyMaster;

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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AllergyMasterAction extends DispatchAction {
	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws SQLException, IOException,ParseException {
		Map map = request.getParameterMap();
		AllergyMasterDAO dao = new AllergyMasterDAO();
		request.setAttribute("pagedList", dao.getAllergyDeatils(map, ConversionUtils.getListingParameter(map)));
		return mapping.findForward("list");
	}
	public ActionForward add(ActionMapping mapping, ActionForm form,HttpServletRequest request,
			HttpServletResponse response)throws SQLException, IOException, ParseException {
		return mapping.findForward("addshow");
	}
	public ActionForward show(ActionMapping mapping, ActionForm form,HttpServletRequest request,
			HttpServletResponse response)throws SQLException, IOException, ParseException {
		AllergyMasterDAO dao = new AllergyMasterDAO();
		BasicDynaBean bean = dao.findByKey("allergy_id", Integer.parseInt(request.getParameter("allergy_id")));
		request.setAttribute("bean", bean);
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("allergiesList", js.serialize(dao.getAllergyNamesAndIds() ));
		return mapping.findForward("addshow");
	}
	public ActionForward create(ActionMapping mapping, ActionForm form,HttpServletRequest request,
			HttpServletResponse response)throws SQLException, IOException, ParseException {
		AllergyMasterDAO dao = new AllergyMasterDAO();
		BasicDynaBean bean = dao.getBean();
		Map params = request.getParameterMap();
		ArrayList errors = new ArrayList();
		String error = null;
		ConversionUtils.copyToDynaBean(params, bean, errors);
		Connection con = null;
		Boolean success = false;
		int allergyId;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			allergyId = dao.getNextSequence();
			bean.set("allergy_id", allergyId);
			if(!dao.exist("allergy_name", request.getParameter("allergy_name")))
				success = dao.insert(con, bean);
			else
				error = "Allergy Name: "+bean.get("allergy_name")+" already exists.";
		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		FlashScope flash = FlashScope.getScope(request);
		flash.error(error);
		if (error == null) {
			redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("allergy_id", allergyId);
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
	public ActionForward update(ActionMapping mapping, ActionForm form,HttpServletRequest request,
			HttpServletResponse response)throws SQLException, IOException, ParseException {
		AllergyMasterDAO dao = new AllergyMasterDAO();
		int allergyId = Integer.parseInt(request.getParameter("allergy_id"));
		Map params = request.getParameterMap();
		BasicDynaBean bean = dao.getBean();
		ArrayList errors = new ArrayList();
		String error = null;
		ConversionUtils.copyToDynaBean(params, bean, errors);
		Connection con = null;
		Boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map<String,Object>keys = new HashMap<String,Object>();
			keys.put("allergy_id", allergyId);
			if(!dao.exists(allergyId,bean.get("allergy_name").toString())) {
				int j = dao.update(con, bean.getMap(), keys);
				if(j>0) success=true;
			} else {
				error = "Allergy Name: "+bean.get("allergy_name")+" already exists.";
			}

		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		ActionRedirect redirect = null;
		FlashScope flash = FlashScope.getScope(request);
		flash.error(error);
		redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		redirect.addParameter("allergy_id", allergyId);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
}