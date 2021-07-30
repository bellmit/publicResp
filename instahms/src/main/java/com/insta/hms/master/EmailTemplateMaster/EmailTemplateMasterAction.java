package com.insta.hms.master.EmailTemplateMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
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
 * @author kalpana.muvvala
 *
 */

public class EmailTemplateMasterAction extends DispatchAction {

	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		PagedList pagedList = EmailTemplateMasterDAO.emailTemplateList(null,ConversionUtils.getListingParameter(req.getParameterMap()));
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

		EmailTemplateMasterDAO dao = new EmailTemplateMasterDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		if (errors.isEmpty()) {
			DynaBean exists = dao.findByKey("template_name", bean.get("template_name"));
			if (exists == null) {
				bean.set("email_template_id", dao.getNextEmailTemplateId());
				boolean success = dao.insert(con, bean);
				if (success) {
					con.commit();
					FlashScope flash = FlashScope.getScope(req);
					flash.put("success", "EmailTemplate master details inserted successfully..");
					ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				} else {
					con.rollback();
					req.setAttribute("error", "Failed to add  Country..");
				}
			} else {
				req.setAttribute("error", "EmailTemplate name already exists..");
			}
		} else {
			req.setAttribute("error", "Incorrectly formatted values supplied");
		}
		return m.findForward("addshow");
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		EmailTemplateMasterDAO dao = new EmailTemplateMasterDAO();
		BasicDynaBean bean = dao.findByKey("email_template_id", req.getParameter("email_template_id"));
		BasicDynaBean ebean = new GenericDAO("email_category_master").findByKey("category_id", bean.get("email_category"));
		req.setAttribute("bean", bean);
		req.setAttribute("category_name", ebean.get("category_name"));
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(req);

		EmailTemplateMasterDAO dao = new EmailTemplateMasterDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		Object key = req.getParameter("email_template_id");
		Map<String, String> keys = new HashMap<String, String>();
		keys.put("email_template_id", key.toString());

		Connection con = null;
		boolean success = true;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.findByKey("template_name", bean.get("template_name"));
				if (exists != null && !key.equals(exists.get("email_template_id"))) {
					req.setAttribute("error", "EmailTemplate name already exists..");
				}
				else {
					success = dao.update(con, bean.getMap(), keys) > 0;

					if ( !success )
						req.setAttribute("error", "Failed to update EmailTemplate master details..");
				}
			}
			else {
				req.setAttribute("error", "Incorrectly formatted values supplied");
			}
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("email_template_id",req.getParameter("email_template_id"));
		return redirect;
	}

}