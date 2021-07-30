package com.insta.hms.master.SalutationMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;

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

public class SalutationMasterAction extends DispatchAction {

	static Logger logger =  LoggerFactory.getLogger(SalutationMasterAction.class);


	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

			SalutationMasterDAO dao = new SalutationMasterDAO();
			Map  map= req.getParameterMap();
			PagedList pagedList = dao.search(map, ConversionUtils.getListingParameter(req.getParameterMap()),
						"salutation_id");
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
		Connection con = null;

		SalutationMasterDAO dao = new SalutationMasterDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.findByKey("salutation", bean.get("salutation"));
				if (exists == null) {
					bean.set("salutation_id", dao.getNextSalutationId());
					boolean success = dao.insert(con, bean);
					if (success) {
						con.commit();
						flash.success("Salutation master details inserted successfully..");
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						redirect.addParameter("salutation_id", bean.get("salutation_id"));
						return redirect;
					} else {
						con.rollback();
						flash.error("Failed to add  Salutation..");
					}
				} else {
					flash.error("Salutation name already exists..");
					redirect = new ActionRedirect(m.findForward("addRedirect"));
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
			redirect = new ActionRedirect(m.findForward("addRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		}finally {
			DataBaseUtil.closeConnections(con, null);
		}
		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		SalutationMasterDAO dao = new SalutationMasterDAO();
		BasicDynaBean bean = dao.findByKey("salutation_id", req.getParameter("salutation_id"));
		req.setAttribute("bean", bean);
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		Map params = req.getParameterMap();
		List errors = new ArrayList();

		SalutationMasterDAO dao = new SalutationMasterDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			Object key = req.getParameter("salutation_id");
			Map<String, String> keys = new HashMap<String, String>();
			keys.put("salutation_id", key.toString());
			FlashScope flash = FlashScope.getScope(req);

			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.findByKey("salutation", bean.get("salutation"));
				if (exists != null && !key.equals(exists.get("salutation_id"))) {
					flash.error("Salutation name already exists..");
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					redirect.addParameter("salutation_id", req.getParameter("salutation_id"));
					return redirect;
				}
				else {
					int success = dao.update(con, bean.getMap(), keys);

					if (success > 0) {
						con.commit();
						flash.success("Salutation master details updated successfully..");
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						redirect.addParameter("salutation_id", req.getParameter("salutation_id"));
						return redirect;
					} else {
						con.rollback();
						req.setAttribute("error", "Failed to update Salutation master details..");
					}
				}
			}
			else {
				req.setAttribute("error", "Incorrectly formatted values supplied");
			}
			redirect.addParameter("salutation_id", req.getParameter("salutation_id"));
		}finally {
			DataBaseUtil.closeConnections(con, null);
		}
		return redirect;
	}

}
