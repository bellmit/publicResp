package com.insta.hms.master.DischargeTypeMaster;

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

public class DischargeTypeMasterAction extends DispatchAction{
	static Logger logger = LoggerFactory.getLogger(DischargeTypeMasterAction.class);

	DischargeTypeMasterDAO dao = new DischargeTypeMasterDAO();
	JSONSerializer js = new JSONSerializer().exclude("class");
	public ActionForward list(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		List dischargeTypes = dao.getAllDischargeTypes();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("dischargeTypes", js.serialize(dischargeTypes));
		Map map= request.getParameterMap();
		PagedList pagedList = dao.getDischargeTypes(map,ConversionUtils.getListingParameter(request.getParameterMap()));
		request.setAttribute("pagedList", pagedList);

		return m.findForward("list");
	}


	public ActionForward add(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		List dischargeTypes = dao.getAllDischargeTypes();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("dischargeTypes", js.serialize(dischargeTypes));
		return m.findForward("addshow");
	}


	public ActionForward create (ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		Map params = request.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		String error = null;
		boolean success = false;
		try {
			if (errors.isEmpty()) {
				boolean exists = dao.exist("discharge_type", ((String)(bean.get("discharge_type"))).trim());
				if (exists) {
					error = "discharge type already exists.....";
				} else {
					bean.set("discharge_type_id", dao.getNextSequence());
					success = dao.insert(con, bean);
					if (!success) {
						error = "Fail to add discharge type to the master....";
					}
				}
			} else {
				error = "Incorrectly formatted values supplied..";
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
			redirect.addParameter("discharge_type_id", bean.get("discharge_type_id"));
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;

	}


	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		BasicDynaBean bean = dao.findByKey("discharge_type_id", Integer.parseInt(req.getParameter("discharge_type_id")));
		req.setAttribute("bean", bean);

		List dischargeTypes = dao.getAllDischargeTypes();
		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("dischargeTypes", js.serialize(dischargeTypes));
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

			Integer key = Integer.parseInt(req.getParameter("discharge_type_id"));
			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("discharge_type_id", key);
			FlashScope flash = FlashScope.getScope(req);

			if (errors.isEmpty()) {
				int success = dao.update(con, bean.getMap(), keys);
				if (success > 0) {
					con.commit();
					flash.success("Discharge Type master details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update Discharge Type master details..");
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
			ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("discharge_type_id", key.toString());
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}
}
