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

public class ToothSurfaceMaterialMasterAction extends DispatchAction{

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ParseException {
		Map map = request.getParameterMap();
		Map listingParams = ConversionUtils.getListingParameter(map);
		ToothSurfaceMaterialMasterDAO  dao = new ToothSurfaceMaterialMasterDAO();
		PagedList list = dao.getMaterialList(map, listingParams);
		request.setAttribute("pagedList", list);
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ParseException {
		return mapping.findForward("addshow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ParseException {
		String materialID = request.getParameter("material_id");
		ToothSurfaceMaterialMasterDAO dao = new ToothSurfaceMaterialMasterDAO();
		BasicDynaBean bean = dao.findByKey("material_id", Integer.parseInt(materialID));
		request.setAttribute("bean", bean);
		return mapping.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ParseException {
		Map params = request.getParameterMap();
		ToothSurfaceMaterialMasterDAO dao = new ToothSurfaceMaterialMasterDAO();
		BasicDynaBean bean = dao.getBean();
		Connection con = null;
		ArrayList errors = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		String error = null;
		ActionRedirect redirect = null;
		Boolean success = false;
		int materialID = dao.getNextSequence();
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			bean.set("material_id",materialID);
			if(!dao.exist("material_name", bean.get("material_name")))
				success = dao.insert(con, bean);
			else
				error = "Material Name "+bean.get("material_name")+" already exists";
		}finally{
			DataBaseUtil.commitClose(con, success);
		}

		redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		FlashScope flash = FlashScope.getScope(request);
		flash.error(error);
		if(error == null){
			redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("material_id", materialID);
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws SQLException, IOException, ParseException {
		ActionRedirect redirect = null;
		Map params = request.getParameterMap();
		ToothSurfaceMaterialMasterDAO dao = new ToothSurfaceMaterialMasterDAO();
		BasicDynaBean bean = dao.getBean();
		ArrayList errors = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		String error = null;
		String materialID = request.getParameter("material_id");
		Connection con = null;
		Boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map<String,Object> keys = new HashMap<String, Object>();
			keys.put("material_id", Integer.parseInt(materialID));
			if(dao.exists(bean.get("material_name").toString(),Integer.parseInt(materialID))) {
				error = "Material Name "+bean.get("material_name") +" already exists" ;
			} else {
				int j = dao.update(con, bean.getMap(), keys);
				if(j>0) success = true;
			}
		}finally {
			DataBaseUtil.commitClose(con, success);
		}
		FlashScope flash = FlashScope.getScope(request);
		flash.error(error);
		redirect = new ActionRedirect(mapping.findForwardConfig("showRedirect"));
		redirect.addParameter("material_id", materialID);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}