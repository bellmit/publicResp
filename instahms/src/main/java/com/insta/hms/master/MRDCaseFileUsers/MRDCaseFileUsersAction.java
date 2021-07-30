/**
 *
 */
package com.insta.hms.master.MRDCaseFileUsers;

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
public class MRDCaseFileUsersAction extends DispatchAction {


	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		MRDCaseFileUsersDAO dao = new MRDCaseFileUsersDAO();
		Map requestParams = req.getParameterMap();
		PagedList pagedList = dao.search(requestParams, ConversionUtils.getListingParameter(requestParams),
					"file_user_id");
		req.setAttribute("pagedList", pagedList);

		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		List<String>  fileusers = MRDCaseFileUsersDAO.getMRDCaseFileUsers();
		req.setAttribute("mrdCasefileUsersList", new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(fileusers)));

		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		MRDCaseFileUsersDAO dao = new MRDCaseFileUsersDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;

		if (errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("file_user_name", bean.get("file_user_name"));
			if (exists == null) {
				bean.set("file_user_id", dao.getNextSequence());
				boolean success = dao.insert(con, bean);
				if (success) {
					con.commit();
					flash.success("MRD case file user details inserted successfully..");
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					redirect.addParameter("file_user_id", bean.get("file_user_id"));
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					DataBaseUtil.closeConnections(con, null);
					return redirect;
				} else {
					con.rollback();
					flash.error("Failed to add MRD File User..");
				}
			} else {
				flash.error("MRD case file user name already exists..");
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

		MRDCaseFileUsersDAO dao = new MRDCaseFileUsersDAO();
		BasicDynaBean bean = dao.findByKey("file_user_id", new Integer(req.getParameter("file_user_id")));
		req.setAttribute("bean", bean);

		List  fileusers = MRDCaseFileUsersDAO.getMRDCaseFileUsers();
		req.setAttribute("mrdCasefileUsersList", new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(fileusers)));

		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		Map params = req.getParameterMap();
		List errors = new ArrayList();

		MRDCaseFileUsersDAO dao = new MRDCaseFileUsersDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		Object key = req.getParameter("file_user_id");
		Map keys = new HashMap();
		keys.put("file_user_id", Integer.parseInt(key.toString()));
		FlashScope flash = FlashScope.getScope(req);

		if (errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("file_user_name",bean.get("file_user_name"));
			if (exists != null && !key.equals(exists.get("file_user_id").toString())) {
				flash.error("MRD case file user name already exists..");
			}
			else {
				int success = dao.update(con, bean.getMap(), keys);

				if (success > 0) {
					con.commit();
					flash.success("MRD case file user details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update MRD case file user details..");
				}
			}
		}
		else {
			flash.error("Incorrectly formatted values supplied");
		}
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("file_user_id" , key.toString());
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}
}
