package com.insta.hms.master.DentalCharting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;

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

public class CrownStatusMasterAction extends DispatchAction{

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ParseException {
		CrownStatusMasterDAO  dao = new CrownStatusMasterDAO();
		Map params = request.getParameterMap();
		PagedList list = dao.getCrownStatusdetails(params,ConversionUtils.getListingParameter(params));
		request.setAttribute("pagedList", list);
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ParseException {
		return mapping.findForward("addshow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ParseException {
		String crownStatusID = request.getParameter("crown_status_id");
		CrownStatusMasterDAO dao = new CrownStatusMasterDAO();
		BasicDynaBean bean = dao.findByKey("crown_status_id", Integer.parseInt(crownStatusID));
		request.setAttribute("bean", bean);
		return mapping.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, ParseException, IOException {
		Map params = request.getParameterMap();
		CrownStatusMasterDAO dao = new CrownStatusMasterDAO();
		BasicDynaBean bean = dao.getBean();
		ArrayList errors = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		Connection con = null;
		Boolean success = false;
		String error = null;
		int crownStatusID = dao.getNextSequence();
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			bean.set("crown_status_id", crownStatusID);
			if(!dao.exist("crown_status_desc", bean.get("crown_status_desc")))
				success = dao.insert(con, bean);
			else
				error = "Crown Status Desc "+bean.get("crown_status_desc")+" already exist.";
		}finally {
			DataBaseUtil.commitClose(con, success);
		}
		FlashScope flash = FlashScope.getScope(request);
		flash.error(error);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		if(error == null) {
			redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("crown_status_id", crownStatusID);
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, ParseException, IOException {
		String crownStatusID = request.getParameter("crown_status_id");
		Map params = request.getParameterMap();
		CrownStatusMasterDAO dao = new CrownStatusMasterDAO();
		BasicDynaBean bean = dao.getBean();
		ArrayList errors = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		Connection con = null;
		String error = null;
		Boolean success = false;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map<String,Object> keys = new HashMap<String,Object>();
			keys.put("crown_status_id", Integer.parseInt(crownStatusID));
			if(dao.exists(bean.get("crown_status_desc").toString(),Integer.parseInt(crownStatusID))){
				error = "Crown Status "+bean.get("crown_status_desc")+" already exist";
			}else {
				int j = dao.update(con, bean.getMap(), keys);
				if(j>0) success = true;
			}

		}finally {
			DataBaseUtil.commitClose(con, success);
		}
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(request);
		flash.error(error);
		redirect.addParameter("crown_status_id", crownStatusID);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
}