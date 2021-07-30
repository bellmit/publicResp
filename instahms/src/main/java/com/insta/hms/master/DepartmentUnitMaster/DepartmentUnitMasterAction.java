/**
 *
 */
package com.insta.hms.master.DepartmentUnitMaster;

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

/**
 * @author lakshmi.p
 *
 */
public class DepartmentUnitMasterAction extends DispatchAction {

	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map requestParams = req.getParameterMap();

		PagedList pagedList = DepartmentUnitMasterDAO.searchDeptUnitList(requestParams,
					ConversionUtils.getListingParameter(requestParams));
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

		DepartmentUnitMasterDAO dao = new DepartmentUnitMasterDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;

		if (errors.isEmpty()) {
			boolean exists = dao.isDeptUnitExists(req.getParameter("dept_id"),req.getParameter("unit_name"));
			 if(!exists) {
				bean.set("unit_id", DataBaseUtil.getIntValueFromDb("SELECT nextval('dept_unit_master_seq')"));
				boolean success = dao.insert(con, bean);
				if (success) {
					con.commit();
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					flash.success("Department Unit details inserted successfully..");
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					redirect.addParameter("unit_id", bean.get("unit_id"));
					DataBaseUtil.closeConnections(con, null);
					return redirect;
				} else {
					con.rollback();
					flash.error("Failed to add unit...");
				}
			} else {
				flash.error(req.getParameter("unit_name") + " Department Unit already exists...");
			}
		} else {
			flash.error("Incorrectly formatted values supplied");
		}

		redirect = new ActionRedirect(m.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		DepartmentUnitMasterDAO dao = new DepartmentUnitMasterDAO();
		BasicDynaBean bean = dao.findByKey("unit_id", new Integer(req.getParameter("unit_id")));
		req.setAttribute("bean", bean);
		req.setAttribute("deptUnitsList", js.serialize(dao.getUnitNamesAndIds()));
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		Map params = req.getParameterMap();
		List errors = new ArrayList();

		DepartmentUnitMasterDAO dao = new DepartmentUnitMasterDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		Map keys = new HashMap();
		keys.put("unit_id", new Integer(bean.get("unit_id").toString()));
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));

		if (errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("unit_id", bean.get("unit_id"));
			if (exists != null
				&& (!dao.isDeptUnitExists(req.getParameter("dept_id"),req.getParameter("unit_name"))
				|| exists.get("unit_name").equals(req.getParameter("unit_name")))) {
					int success = dao.update(con, bean.getMap(), keys);
					if (success > 0) {
						con.commit();
						flash.success("Unit details updated successfully..");

					} else {
						con.rollback();
						flash.error("Failed to update unit details..");
					}
			}else {
				flash.error(req.getParameter("unit_name") + " Deparment Unit already exists...");
			}
		}
		else {
			flash.error("Incorrectly formatted values supplied");
		}
		redirect.addParameter("unit_id", bean.get("unit_id") );
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}
}
