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

public class ToothSurfaceStatusMasterAction extends DispatchAction{

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ParseException {
		Map map = request.getParameterMap();
		ToothSurfaceStatusMasterDAO dao = new ToothSurfaceStatusMasterDAO();
		PagedList list = dao.getToothSurfaceStatuslist(map,ConversionUtils.getListingParameter(map));
		request.setAttribute("pagedList", list);
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ParseException {
		return mapping.findForward("addshow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ParseException {
		String surfaceStatusID = request.getParameter("surface_status_id");
		ToothSurfaceStatusMasterDAO dao = new ToothSurfaceStatusMasterDAO();
		BasicDynaBean bean = dao.findByKey("surface_status_id", Integer.parseInt(surfaceStatusID));
		request.setAttribute("bean", bean);
		return mapping.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws SQLException,IOException,ParseException{
		Map params = request.getParameterMap();
		ToothSurfaceStatusMasterDAO dao = new ToothSurfaceStatusMasterDAO();
		BasicDynaBean bean = dao.getBean();
		ArrayList errors = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		Connection con = null;
		Boolean success = false;
		String error = null;
		int surfaceStatusID = dao.getNextSequence();
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			bean.set("surface_status_id", surfaceStatusID);
			if(!dao.exist("surface_status_name", bean.get("surface_status_name")))
				success = dao.insert(con, bean);
			else
				error = "Surface Status Name "+bean.get("surface_status_name")+" already exist.";
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		FlashScope flash = FlashScope.getScope(request);
		flash.error(error);
		if(error == null){
			redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("surface_status_id", surfaceStatusID);
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws SQLException,IOException,ParseException{
		String surfaceStatusID = request.getParameter("surface_status_id");
		Map params = request.getParameterMap();
		ToothSurfaceStatusMasterDAO  dao = new ToothSurfaceStatusMasterDAO();
		BasicDynaBean bean = dao.findByKey("surface_status_id",Integer.parseInt(surfaceStatusID));
		ArrayList errors = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		Boolean success=false;
		Connection con = null;
		String error = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map<String,Object> keys = new HashMap<String,Object>();
			keys.put("surface_status_id", Integer.parseInt(surfaceStatusID));
			if(!dao.exists(Integer.parseInt(surfaceStatusID),bean.get("surface_status_name").toString())){
				int j = dao.update(con, bean.getMap(), keys);
				if(j>0) success = true;
			} else {
				error = "Surface Status Name "+bean.get("surface_status_name")+" already exist.";
			}
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(request);
		flash.error(error);
		redirect.addParameter("surface_status_id", surfaceStatusID);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}