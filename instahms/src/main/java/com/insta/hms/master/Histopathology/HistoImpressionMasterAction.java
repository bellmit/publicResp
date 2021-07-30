package com.insta.hms.master.Histopathology;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class HistoImpressionMasterAction extends DispatchAction {

	GenericDAO dao = new GenericDAO("histo_impression_master");
	JSONSerializer json = new JSONSerializer().exclude("class");

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
					HttpServletResponse response)throws Exception {

		Map requestParams = request.getParameterMap();
		PagedList pagedList = dao.search(requestParams, ConversionUtils.getListingParameter(requestParams),
				"impression_id");
		request.setAttribute("shortImpressionNames", json.serialize(dao.getColumnList("short_impression")));
		request.setAttribute("pagedList", pagedList);

		return mapping.findForward("list");

	}


	public ActionForward add(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws Exception{

		request.setAttribute("histoNamesAndIds", json.serialize(HistoImpressionMasterDao.getHistoNamesAndIds()));
		return mapping.findForward("addShow");
	}

	public ActionForward addFromConductionScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws Exception{

		request.setAttribute("histoNamesAndIds", json.serialize(HistoImpressionMasterDao.getHistoNamesAndIds()));
		request.setAttribute("impressionName", request.getParameter("impression_name"));
		request.setAttribute("referer", request.getHeader("Referer"));
		return mapping.findForward("addShow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws Exception {

		BasicDynaBean bean = dao.findByKey("impression_id", Integer.parseInt(request.getParameter("impression_id")));
		request.setAttribute("histoNamesAndIds", json.serialize(HistoImpressionMasterDao.getHistoNamesAndIds()));
		request.setAttribute("bean", bean);
		request.setAttribute("conductionCyto", request.getParameter("conductionCyto"));

		return mapping.findForward("addShow");
	}


	public ActionForward create (ActionMapping mapping, ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws Exception {

		ActionRedirect redirect = null;
		Map params = request.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		String cytoConduction = request.getParameter("cytoConduction");
		if (cytoConduction != null && cytoConduction.equals("Y")) {
			request.setAttribute("conductionCyto", "Y");
		}

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		String error = null;
		boolean success = false;
		try {
			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.findByKey("short_impression", (String)bean.get("short_impression"));
				if (exists != null) {
					error = "Short impression name already exists.....";
				} else {
					bean.set("impression_id", dao.getNextSequence());
					success = dao.insert(con, bean);
					if (!success) {
						error = "Fail to add Short impression master....";
					}
				}
			} else {
				error = "Incorrectly formatted values supplied..";
			}
		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		FlashScope flash = FlashScope.getScope(request);
		if (error != null) {
			redirect = new ActionRedirect(mapping.findForward("addRedirect"));
			flash.error(error);

		}else {
			redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("impression_id", bean.get("impression_id"));
			if (cytoConduction != null && cytoConduction.equals("Y")) {
				redirect.addParameter("conductionCyto", cytoConduction);
			}
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;

	}


	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws Exception {

		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map params = request.getParameterMap();
			List errors = new ArrayList();

			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			Object key = request.getParameter("impression_id");

			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("impression_id", Integer.parseInt(key.toString()));
			FlashScope flash = FlashScope.getScope(request);

			if (errors.isEmpty()) {
				int success = dao.update(con, bean.getMap(), keys);
				if (success > 0) {
					con.commit();
					flash.success("Short impression master details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update Short impression master details..");
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("impression_id", key.toString());
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}


}