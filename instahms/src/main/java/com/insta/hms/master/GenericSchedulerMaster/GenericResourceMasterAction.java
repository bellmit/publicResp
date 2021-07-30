
package com.insta.hms.master.GenericSchedulerMaster;

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

/**
 * @author mithun.saha
 *
 */

public class GenericResourceMasterAction extends DispatchAction{
	static Logger logger = LoggerFactory.getLogger(GenericResourceMasterAction.class);
	JSONSerializer js = new JSONSerializer().exclude("class");
	CenterMasterDAO centerDao = new CenterMasterDAO();
	GenericResourceMasterDAO dao = new GenericResourceMasterDAO();

	public ActionForward list(ActionMapping map,ActionForm fm,
							HttpServletRequest req,HttpServletResponse res)
	throws ServletException,Exception {

		Map params = req.getParameterMap();
		int userCenterId = (Integer)req.getSession(false).getAttribute("centerId");
		PagedList pagedlist =
			dao.getGenericResources(
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
		request.setAttribute("genericResourceNames", js.serialize(resourceNames));
		request.setAttribute("centers", centerDao.getAllCentersExceptSuper());
		return map.findForward("addshow");
	}

	public ActionForward create(ActionMapping map,ActionForm fm,
			HttpServletRequest req,HttpServletResponse res)
	throws ServletException,Exception {

		ActionRedirect redirect = null;
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = null;
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		boolean success = false;
		String error = null;
		FlashScope flash = FlashScope.getScope(req);

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if (errors.isEmpty()) {
				boolean exists = dao.exist("generic_resource_name",(String)bean.get("generic_resource_name"));
				if (exists) {
					error = "Generic Resource name already exists.....";
				} else {
					int resourceId = dao.getNextSequence();
					bean.set("generic_resource_id", resourceId);
					if(req.getParameter("schedule") != null && !req.getParameter("schedule").isEmpty()) {
						bean.set("schedule", true);
					} else {
						bean.set("schedule", false);
					}
					/*if(req.getParameter("overbook") != null && !req.getParameter("overbook").isEmpty()) {
						bean.set("overbook", true);
					} else {
						bean.set("overbook", false);
					}*/
					success = dao.insert(con, bean);

					if (success) {
						flash.success("Generic Resources Master details inserted successfully...");
						redirect = new ActionRedirect(map.findForward("showRedirect"));
						redirect.addParameter("generic_resource_id", (Integer)bean.get("generic_resource_id"));
					} else {
						error = "Fail to add Generic Resources master....";
						flash.error(error);
					}
				}
			} else {
				error = "Incorrectly formatted values supplied..";
			}
		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		if (error != null) {
			redirect = new ActionRedirect(map.findForward("addRedirect"));
		}

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest request, HttpServletResponse responce)
	throws IOException, ServletException, Exception {

		BasicDynaBean bean = dao.getRecord(Integer.parseInt(request.getParameter("generic_resource_id")));
		request.setAttribute("bean", bean);
		ArrayList<String>  resourceNames = (ArrayList<String>)dao.getAllResourceNames();
		request.setAttribute("resourceNames", js.serialize(resourceNames));
		request.setAttribute("centers", centerDao.getAllCentersExceptSuper());
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest request, HttpServletResponse responce)
	throws IOException, ServletException, Exception {

		Connection con = null;
		Map params = request.getParameterMap();
		List errors = new ArrayList();
		boolean success = false;

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		if (request.getParameter("schedule") == null ||request.getParameter("schedule").equals("")) {
			bean.set("schedule", false);
		}
		/*if (request.getParameter("overbook") == null ||request.getParameter("overbook").equals("")) {
			bean.set("overbook", false);
		}*/

		Object key = request.getParameter("generic_resource_id");

		Map<String, Integer> keys = new HashMap<String, Integer>();
		keys.put("generic_resource_id", Integer.parseInt(key.toString()));
		FlashScope flash = FlashScope.getScope(request);

		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);//begin transaction

			if (errors.isEmpty()) {
				success = dao.update(con, bean.getMap(), keys) > 0;

				if (!success)
					flash.error("Failed to update Generic Resources Master details..");
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("generic_resource_id", Integer.parseInt(key.toString()));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
}

