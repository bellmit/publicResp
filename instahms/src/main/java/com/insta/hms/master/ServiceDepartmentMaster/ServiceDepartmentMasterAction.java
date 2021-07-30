package com.insta.hms.master.ServiceDepartmentMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
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

public class ServiceDepartmentMasterAction extends DispatchAction {

	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		ServiceDepartmentMasterDAO dao = new ServiceDepartmentMasterDAO();
		Map requestParams = req.getParameterMap();
		PagedList pagedList = dao.searchDeptList(requestParams, ConversionUtils.getListingParameter(requestParams));
		req.setAttribute("pagedList", pagedList);

		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		ServiceDepartmentMasterDAO dao = new ServiceDepartmentMasterDAO();
		int serv_dept_id = DataBaseUtil.getIntValueFromDb("SELECT max(serv_dept_id) FROM services_departments");
		serv_dept_id=serv_dept_id+1;
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("addRedirect"));;

		if (errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("department", bean.get("department"));
			if (exists == null) {
				bean.set("serv_dept_id", serv_dept_id);
				boolean success = dao.insert(con, bean);
				if (success) {
					con.commit();
					flash.success("Service Department master details inserted successfully..");
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					redirect.addParameter("serv_dept_id", bean.get("serv_dept_id"));
					redirect.addParameter("store_id", bean.get("store_id"));
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					DataBaseUtil.closeConnections(con, null);
					return redirect;
				} else {
					con.rollback();
					flash.error("Failed to add  Service Department..");
				}
			} else {
				flash.error("Service Department name already exists..");
			}
		} else {
			flash.error("Incorrectly formatted values supplied");
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		ServiceDepartmentMasterDAO dao = new ServiceDepartmentMasterDAO();
		BasicDynaBean bean = dao.findByKey("serv_dept_id", Integer.parseInt(req.getParameter("serv_dept_id")));
		req.setAttribute("bean", bean);
		req.setAttribute("serviceLists", js.serialize(dao.getServicesNamesAndIds()));

		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		Map params = req.getParameterMap();
		List errors = new ArrayList();

		ServiceDepartmentMasterDAO dao = new ServiceDepartmentMasterDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		Integer key = Integer.parseInt(req.getParameter("serv_dept_id"));
		Map<String, Integer> keys = new HashMap<String, Integer>();
		keys.put("serv_dept_id", key);
		FlashScope flash = FlashScope.getScope(req);

		if (errors.isEmpty()) {

			int success = dao.update(con, bean.getMap(), keys);

			if (success > 0) {
				con.commit();
				flash.success("Service Department master details updated successfully..");
			} else {
				con.rollback();
				flash.error("Failed to update Service Department master details..");
			}

		}
		else {
			flash.error("Incorrectly formatted values supplied");
		}
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("serv_dept_id", bean.get("serv_dept_id"));
		redirect.addParameter("store_id", bean.get("store_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}

}