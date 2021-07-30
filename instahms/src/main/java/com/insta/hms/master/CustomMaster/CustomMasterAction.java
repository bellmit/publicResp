/**
 *
 */
package com.insta.hms.master.CustomMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;

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
public class CustomMasterAction extends DispatchAction {

	GenericDAO dao = new GenericDAO("");

	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		setCustomMaster(m, req);

		Map map=req.getParameterMap();
		PagedList pagedList = dao.search(map, ConversionUtils.getListingParameter(req.getParameterMap()), "custom_value");
		req.setAttribute("pagedList", pagedList);
		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		setCustomMaster(m, req);
		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		setCustomMaster(m, req);

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));

		try {
			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.findByKey("custom_value", bean.get("custom_value"));
				if (exists == null) {
					boolean success = dao.insert(con, bean);
					if (success) {
						con.commit();
						flash.success("Details inserted successfully..");
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						redirect.addParameter("custom_value", bean.get("custom_value"));
						return redirect;
					} else {
						con.rollback();
						flash.error("Failed to add  details..");
					}
				} else {
					flash.error("Custom name already exists..");
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
		redirect = new ActionRedirect(m.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		setCustomMaster(m, req);

		BasicDynaBean bean = dao.findByKey("custom_value", req.getParameter("custom_value"));
		req.setAttribute("bean", bean);
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		setCustomMaster(m, req);

		Connection con = null;
		Map params = req.getParameterMap();
		List errors = new ArrayList();

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);

			Object key = req.getParameter("old_custom_value");
			Map<String, String> keys = new HashMap<String, String>();
			keys.put("custom_value", key.toString());
			FlashScope flash = FlashScope.getScope(req);
			ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));

			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.findByKey("custom_value", bean.get("custom_value"));
				if (exists != null && !key.equals(exists.get("custom_value"))) {
					flash.error("Custom name already exists..");
				}
				else {
					int success = dao.update(con, bean.getMap(), keys);

					if (success > 0) {
						con.commit();
						flash.success("Details updated successfully..");
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						redirect.addParameter("custom_value", bean.get("custom_value"));
						return redirect;
					} else {
						con.rollback();
						flash.error("Failed to update details..");
					}
				}
			}
			else {
				flash.error("Incorrectly formatted values supplied");
			}
			DataBaseUtil.closeConnections(con, null);
			redirect.addParameter("custom_value", req.getParameter("custom_value"));
			return redirect;

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private void setCustomMaster(ActionMapping m, HttpServletRequest req) {
		String customMasterNumber = m.getProperty("custom_list");
		String customMasterType	  = m.getProperty("custom_type");
		int customNo = new Integer(customMasterNumber);
		if (customMasterType != null) {
			if (customMasterType.equals("P")) {
				String table = "custom_list"+customNo+"_master";
				GenericDAO customdao = new GenericDAO(table);
				dao = customdao;
				req.setAttribute("custom_list", customMasterNumber);
				req.setAttribute("custom_type", customMasterType);

			}else if (customMasterType.equals("V")) {
				String table = "custom_visit_list"+customNo+"_master";
				GenericDAO customdao = new GenericDAO(table);
				dao = customdao;
				req.setAttribute("custom_list", customMasterNumber);
				req.setAttribute("custom_type", customMasterType);
			}
		}
	}
}
