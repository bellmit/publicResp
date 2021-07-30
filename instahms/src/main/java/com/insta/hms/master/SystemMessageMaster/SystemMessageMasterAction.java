package com.insta.hms.master.SystemMessageMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;

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

public class SystemMessageMasterAction extends DispatchAction {

	public ActionForward list(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws IOException, ServletException, Exception {

		SystemMessagesDAO dao = new SystemMessagesDAO();
		List beanList = dao.listAll(null, "system_type", "User", null);
		req.setAttribute("beanList", beanList);

		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws IOException, ServletException, Exception {
		return m.findForward("addshow");
	}

	@SuppressWarnings("unchecked")
	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		SystemMessagesDAO dao = new SystemMessagesDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("addRedirect"));

		try {
			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.findByKey("messages", bean.get("messages"));
				if (exists == null) {
					bean.set("message_id", dao.getNextSequence());
					boolean success = dao.insert(con, bean);
					if (success) {
						con.commit();
						flash.success("Message inserted successfully..");
						redirect = new ActionRedirect(m.findForward("showRedirect"));
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						redirect.addParameter("message_id", bean.get("message_id"));
						return redirect;
					} else {
						con.rollback();
						flash.error("Failed to add  Message..");
					}
				} else {
					flash.error("Message already exists..");
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}

		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException, Exception {

 		SystemMessagesDAO dao = new SystemMessagesDAO();
		String messageId = req.getParameter("message_id");
		int message_id = Integer.parseInt(messageId);
		BasicDynaBean bean = dao.findByKey("message_id", message_id);
		req.setAttribute("bean", bean);

		return m.findForward("addshow");
	}

	@SuppressWarnings("unchecked")
	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		Map params = req.getParameterMap();
		List errors = new ArrayList();

		SystemMessagesDAO dao = new SystemMessagesDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		String messageId = (String) req.getParameter("message_id");
		int message_id = Integer.parseInt(messageId);
		Object key = message_id;
		Map<String, Integer> keys = new HashMap<String, Integer>();
		keys.put("message_id", new Integer(message_id));

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		try
		{
		if (errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("messages", bean.get("messages"));
			if (exists != null && !key.equals(exists.get("message_id"))) {
				flash.error("Message already exists..");
			}
			else {
				int success = dao.update(con, bean.getMap(), keys);
				if (success > 0) {
					con.commit();
					flash.success("Message details updated successfully..");
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					redirect.addParameter("message_id", req.getParameter("message_id"));
					return redirect;
				} else {
					con.rollback();
					flash.error("Failed to update Message master details..");
				}
			}
		}
		else {
			flash.error("Incorrectly formatted values supplied");
		}
		} finally
		{
			DataBaseUtil.closeConnections(con,null);
		}
		redirect.addParameter("message_id", req.getParameter("message_id"));
		return redirect;
	}

	public ActionForward delete (ActionMapping m,ActionForm f,
			HttpServletRequest req,HttpServletResponse res) throws IOException, ServletException, Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		SystemMessagesDAO dao = new SystemMessagesDAO();
		String messageId = (String) req.getParameter("message_id");
		int message_id = Integer.parseInt(messageId);
		try {
			boolean status = dao.delete(con, "message_id", message_id);
			if(status){
				con.commit();
				FlashScope flash = FlashScope.getScope(req);
				flash.success("Message deleted successfully..");
				ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
			else {
				con.rollback();
				FlashScope flash = FlashScope.getScope(req);
				flash.error("Failed to delete message.");
				ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
		}
		finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}


}
