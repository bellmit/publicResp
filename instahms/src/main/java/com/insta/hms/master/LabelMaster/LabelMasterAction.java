package com.insta.hms.master.LabelMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class LabelMasterAction extends DispatchAction {
	static Logger logger = LoggerFactory.getLogger(LabelMasterAction.class);

	public LabelMasterDAO dao = new LabelMasterDAO();

	public ActionForward list(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		Map map= request.getParameterMap();
		List labelsList = LabelMasterDAO.getAllLabels();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("labelsList", js.serialize(labelsList));
		PagedList pagedList = dao.getLabelMasterDetails(map,ConversionUtils.getListingParameter(request.getParameterMap()));
		request.setAttribute("pagedList", pagedList);

		return m.findForward("list");
	}


	public ActionForward add(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		List labelsList = LabelMasterDAO.getAllLabels();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("labelsList", js.serialize(labelsList));
		return m.findForward("addshow");
	}


	public ActionForward create (ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		Map params = request.getParameterMap();
		List errors = new ArrayList();
		Connection con = null;
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		String error = null;
		boolean success = false;
		try {
    	 con = DataBaseUtil.getConnection();
	    	con.setAutoCommit(false);
			if (errors.isEmpty()) {
				boolean exists = dao.exist("label_short", ((String)(bean.get("label_short"))).trim());
				if (exists) {
					error = getResources(request).getMessage("generalmasters.labelmaster.action.message.labelname.exists");
				} else {
					bean.set("label_id", dao.getNextSequence());
					success = dao.insert(con, bean);
					if (!success) {
						error = getResources(request).getMessage("generalmasters.labelmaster.action.error.message.in.insert.details");
					}
				}
			} else {
				error = getResources(request).getMessage("generalmasters.labelmaster.action.error.message.incorrect.values");
			}
		}finally {
			DataBaseUtil.commitClose(con, success);
		}
		ActionRedirect redirect = null;
		FlashScope flash = FlashScope.getScope(request);
		if (error != null) {
			redirect = new ActionRedirect(m.findForward("addRedirect"));
			flash.error(error);

		}else {
			redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("label_id", bean.get("label_id"));
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;

	}


	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		BasicDynaBean bean = dao.findByKey("label_id", Integer.parseInt(req.getParameter("label_id")));
		req.setAttribute("bean", bean);

		List labelsList = LabelMasterDAO.getAllLabels();
		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("labelsList", js.serialize(labelsList));
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map params = req.getParameterMap();
			List errors = new ArrayList();

			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);

			Integer key = Integer.parseInt(req.getParameter("label_id"));
			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("label_id", key);
			FlashScope flash = FlashScope.getScope(req);

			if (errors.isEmpty()) {
				int success = dao.update(con, bean.getMap(), keys);
				if (success > 0) {
					con.commit();
					flash.success(getResources(req).getMessage("generalmasters.labelmaster.action.message.update.success"));
				} else {
					con.rollback();
					flash.error(getResources(req).getMessage("generalmasters.labelmaster.action.message.update.failure"));
				}
			} else {
				flash.error(getResources(req).getMessage("generalmasters.labelmaster.action.error.message.incorrect.values"));
			}
			ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("label_id", key.toString());
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

}
