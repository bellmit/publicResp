package com.insta.hms.master.serviceresourcemaster;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ServiceResourceMasterAction extends DispatchAction{
	static Logger logger = LoggerFactory.getLogger(ServiceResourceMasterAction.class);
	JSONSerializer js = new JSONSerializer().exclude("class");
	CenterMasterDAO centerDao = new CenterMasterDAO();
	ServiceResourceMasterDAO dao = new ServiceResourceMasterDAO();

	public ActionForward list(ActionMapping map,ActionForm fm,
							HttpServletRequest req,HttpServletResponse res)
	throws ServletException,Exception {

		Map params = req.getParameterMap();
		int userCenterId = (Integer)req.getSession(false).getAttribute("centerId");
		PagedList pagedlist =
			dao.getResourcesList(
					params,ConversionUtils.getListingParameter(
							req.getParameterMap()), userCenterId);

		req.setAttribute("pagedlist", pagedlist);
		req.setAttribute("centers", centerDao.getAllCentersExceptSuper());

		return map.findForward("list");
	}

	public ActionForward add(ActionMapping map,ActionForm fm,
			HttpServletRequest request,HttpServletResponse responce)
	throws ServletException,Exception {

		ArrayList<String>  resourceNames = (ArrayList<String>)dao.getAllResourceNames();
		request.setAttribute("resourceNames", js.serialize(resourceNames));
		request.setAttribute("centers", centerDao.getAllCentersExceptSuper());
		return map.findForward("addShow");
	}

	public ActionForward create(ActionMapping map,ActionForm fm,
			HttpServletRequest req,HttpServletResponse res)
	throws ServletException,Exception {

		ActionRedirect redirect = null;
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		HttpSession session = req.getSession();

		if (req.getParameter("schedule") == null ||req.getParameter("schedule").equals("")) {
			bean.set("schedule", false);
		}
		/*if (req.getParameter("overbook_limit") == null ||req.getParameter("overbook_limit").equals("")) {
			bean.set("overbook_limit", 0);
		}*/
		String error = null;
		String success = null;

		if (errors.isEmpty()) {
			boolean exists = dao.exist("serv_resource_name",(String)bean.get("serv_resource_name"));
			if (exists) {
				error = "Resource name already exists.....";
			} else {
				int resourceId = dao.getNextSequence();
				bean.set("serv_res_id", resourceId);
				boolean sucess = dao.insert(con, bean);

				if (sucess) {
					con.commit();
					success = "Service Resources Master details inserted successfully...";
				} else {
					con.rollback();
					error = "Fail to add Service Resources master....";
				}
				con.close();
			}
		} else {
			error = "Incorrectly formatted values supplied..";
		}
		FlashScope flash = FlashScope.getScope(req);
		if (error != null) {
			redirect = new ActionRedirect(map.findForward("addRedirect"));
			flash.error(error);
		}
		if (success != null) {
			redirect = new ActionRedirect(map.findForward("showRedirect"));
			redirect.addParameter("serv_res_id", (Integer)bean.get("serv_res_id"));
			flash.success(success);
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest request, HttpServletResponse responce)
	throws IOException, ServletException, Exception {

		BasicDynaBean bean = dao.getRecord(Integer.parseInt(request.getParameter("serv_res_id")));
		request.setAttribute("bean", bean);

		ArrayList<String>  resourceNames = (ArrayList<String>)dao.getAllResourceNames();
		request.setAttribute("resourceNames", js.serialize(resourceNames));
		request.setAttribute("centers", centerDao.getAllCentersExceptSuper());

		return m.findForward("addShow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest request, HttpServletResponse responce)
	throws IOException, ServletException, Exception {

		Connection con = null;
		Map params = request.getParameterMap();
		List errors = new ArrayList();
		boolean success = true;

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		if (request.getParameter("schedule") == null ||request.getParameter("schedule").equals("")) {
			bean.set("schedule", false);
		}
		/*if (request.getParameter("overbook_limit") == null ||request.getParameter("overbook_limit").equals("")) {
			bean.set("overbook_limit", 0);
		}*/

		Object key = request.getParameter("serv_res_id");

		Map<String, Integer> keys = new HashMap<String, Integer>();
		keys.put("serv_res_id", Integer.parseInt(key.toString()));
		FlashScope flash = FlashScope.getScope(request);

		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);//begin transaction

			if (errors.isEmpty()) {
				success = dao.update(con, bean.getMap(), keys) > 0;

				if (!success)
					flash.error("Failed to update Service Resources Master details..");
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("serv_res_id", Integer.parseInt(key.toString()));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
}
