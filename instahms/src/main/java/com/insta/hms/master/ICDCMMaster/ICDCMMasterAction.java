/**
 *
 */
package com.insta.hms.master.ICDCMMaster;

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
public class ICDCMMasterAction extends DispatchAction {

	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map requestParams = req.getParameterMap();
		GenericDAO dao = new GenericDAO("mrd_icdcodes_cm");
		PagedList pagedList = dao.search(requestParams, ConversionUtils.getListingParameter(requestParams),
					"icd_code");
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

		GenericDAO dao = new GenericDAO("mrd_codes_master");
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;

		if (errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("code", bean.get("code"));
			 if(exists == null) {
				boolean success = dao.insert(con, bean);
				if (success) {
					con.commit();
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					flash.success("ICD Code details inserted successfully..");
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					redirect.addParameter("icd_code", bean.get("code"));
					DataBaseUtil.closeConnections(con, null);
					return redirect;
				} else {
					con.rollback();
					flash.error("Failed to add ICD Code...");
				}
			} else {
				flash.error("ICD Code already exists...");
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

		ICDCMMasterDAO dao = new ICDCMMasterDAO();
		BasicDynaBean bean = dao.findByKey("icd_code", req.getParameter("icd_code"));
		req.setAttribute("bean", bean);
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		Map params = req.getParameterMap();
		List errors = new ArrayList();

		ICDCMMasterDAO dao = new ICDCMMasterDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		Object key = req.getParameter("old_icd_code");
		Map<String, String> keys = new HashMap<String, String>();
		keys.put("icd_code", key.toString());
		FlashScope flash = FlashScope.getScope(req);

		if (errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("icd_code", bean.get("icd_code"));
			if (exists != null && !key.equals(exists.get("icd_code"))) {
				flash.error(req.getParameter("icd_code")+" ICD Code already exists..");
			}
			else {
				int success = dao.update(con, bean.getMap(), keys);

				if (success > 0) {
					con.commit();
					flash.success("ICD Code details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update ICD Code details..");
				}
			}
		}
		else {
			flash.error("Incorrectly formatted values supplied");
		}
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("icd_code", bean.get("icd_code"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}
}
