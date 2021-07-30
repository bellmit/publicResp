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

public class ToothRootStatusMasterAction extends DispatchAction{

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ParseException {
		Map map = request.getParameterMap();
		ToothRootStatusMasterDAO dao = new ToothRootStatusMasterDAO();
		PagedList list = dao.getToothRootStatusDetails(map,ConversionUtils.getListingParameter(map));
		request.setAttribute("pagedList",list);
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ParseException {
		return mapping.findForward("addshow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ParseException {
		String rootStatusID = request.getParameter("root_status_id");
		ToothRootStatusMasterDAO dao = new ToothRootStatusMasterDAO();
		BasicDynaBean bean = dao.findByKey("root_status_id", Integer.parseInt(rootStatusID));
		request.setAttribute("bean", bean);
		return mapping.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ParseException{
		Map params = request.getParameterMap();
		ToothRootStatusMasterDAO dao = new ToothRootStatusMasterDAO();
		BasicDynaBean bean = dao.getBean();
		ArrayList errors = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		Connection con = null;
		Boolean success = false;
		int rootStatusID = dao.getNextSequence();
		String error = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			bean.set("root_status_id",rootStatusID);
			if(!dao.exist("root_status_desc",bean.get("root_status_desc")))
				success = dao.insert(con, bean);
			else
				error = "Root status desc "+bean.get("root_status_desc")+" already exist.";
		}finally {
			DataBaseUtil.commitClose(con, success);
		}
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		FlashScope flash = FlashScope.getScope(request);
		flash.error(error);
		if(error==null) {
			redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("root_status_id", rootStatusID);
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ParseException {
		String rootStatusID = request.getParameter("root_status_id");
		Map params = request.getParameterMap();
		ToothRootStatusMasterDAO dao = new ToothRootStatusMasterDAO();
		BasicDynaBean bean = dao.getBean();
		ArrayList errors = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		Connection con = null;
		Boolean success = false;
		String error = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map<String,Object> keys = new HashMap<String,Object>();
			keys.put("root_status_id", Integer.parseInt(rootStatusID));
			if(!dao.exists(Integer.parseInt(rootStatusID),bean.get("root_status_desc").toString())){
				int j = dao.update(con,bean.getMap(),keys);
				if(j>0) success = true;
			}else {
				error = "Root status desc "+bean.get("root_status_desc")+" already exist";
			}
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(request);
		flash.error(error);
		redirect.addParameter("root_status_id", rootStatusID);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}