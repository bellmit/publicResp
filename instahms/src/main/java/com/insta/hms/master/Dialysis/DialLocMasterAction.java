package com.insta.hms.master.Dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class DialLocMasterAction extends DispatchAction {

	DialLocationMasterDAO dao = new DialLocationMasterDAO();
	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws IOException, ServletException, Exception {

		Map requestParams = request.getParameterMap();
		PagedList pagedList = dao.getDialLocationsList(ConversionUtils.getListingParameter(requestParams), requestParams);
		request.setAttribute("pagedList", pagedList);
		request.setAttribute("centers", new CenterMasterDAO().getAllCentersExceptSuper());

		return mapping.findForward("list");

	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws IOException, ServletException, Exception {

		Map params = request.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		ActionRedirect redirect = null;
		FlashScope flash = FlashScope.getScope(request);
		DialLocationMasterDAO dao = new DialLocationMasterDAO();
		BasicDynaBean bean = dao.getBean();

		ConversionUtils.copyToDynaBean(params, bean, errors);
		if(errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("location_name", request.getParameter("location_name"));
			if(exists == null) {
				bean.set("location_id", dao.getNextSequence());
				boolean success = dao.insert(con, bean);
				if(success) {
					con.commit();
					redirect = new ActionRedirect(mapping.findForward("showRedirect"));
					flash.success("Location name Inserted successfully");
					redirect.addParameter("location_id", bean.get("location_id"));
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					DataBaseUtil.closeConnections(con, null);
					return redirect;
				}else {
					flash.error("Failed to add Location..");

				}
			}else {
				flash.error("Location name already exists..");

			}
		}else {
			flash.error("Incorrectly formatted values supplied");

		}
		redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}


	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws IOException, ServletException, Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		FlashScope flash = FlashScope.getScope(request);
		Map map = request.getParameterMap();

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		DialLocationMasterDAO dao = new DialLocationMasterDAO();
		BasicDynaBean bean = dao.getBean();
		List errors = new ArrayList();

		Object key = request.getParameter("location_id");
		Map<String, Integer> keys = new HashMap<String, Integer>();
		keys.put("location_id", Integer.parseInt(key.toString()));

		ConversionUtils.copyToDynaBean(map, bean, errors);
		if(errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("location_name", bean.get("location_name"));
		if(exists != null && !key.equals(exists.get("location_id").toString())) {
				flash.error("Location name already exists");
			}
			else {
				int success = dao.update(con, bean.getMap(), keys);
				if(success == 1) {
					con.commit();
					flash.success("Details Updated successfully");
				}
				else {
					con.rollback();
					flash.error("Failed to update location details..");
				}
			}
		}
		else {
			flash.error("Incorrectly formatted details supplied");
		}

		redirect.addParameter("location_id", bean.get("location_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}

	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws IOException, ServletException, Exception {

		request.setAttribute("centers", new CenterMasterDAO().getAllCentersExceptSuper());
		return mapping.findForward("addshow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		BasicDynaBean bean = dao.getRecord(Integer.parseInt(request.getParameter("location_id")));
		request.setAttribute("bean", bean);
		request.setAttribute("locationsLists", js.serialize(DialLocationMasterDAO.getAvalDialLocations()));
		request.setAttribute("centers", new CenterMasterDAO().getAllCentersExceptSuper());

		return mapping.findForward("addshow");
	}

}