package com.insta.hms.master.ClinicalVaccinationsMaster;

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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author mithun.saha
 *
 */


public class ClinicalVaccinationsMasterAction extends DispatchAction{

	static Logger logger = LoggerFactory.getLogger(ClinicalVaccinationsMasterAction.class);
	JSONSerializer json = new JSONSerializer().exclude("class");
	ClinicalVaccinationsMasterDAO dao = new ClinicalVaccinationsMasterDAO();

	public ActionForward list(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,HttpServletResponse response)
	throws SQLException, Exception {
		PagedList pl = null;
		Map requestParams = request.getParameterMap();
		pl = dao.getVaccinationDetails(requestParams, ConversionUtils.getListingParameter(request.getParameterMap()));
		request.setAttribute("pagedList", pl);
		request.setAttribute("vaccinationTypesAndIds", json.serialize(ConversionUtils.copyListDynaBeansToMap(dao.listAll())));
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,HttpServletResponse response)
	throws SQLException, Exception {
		request.setAttribute("vaccinationTypesAndIds", json.serialize(ConversionUtils.copyListDynaBeansToMap(dao.listAll())));
		return mapping.findForward("addshow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,HttpServletResponse response)
	throws SQLException, Exception {

		BasicDynaBean bean = null;
		String vaccinationId = request.getParameter("vaccination_type_id");
		if (vaccinationId != null && !vaccinationId.equals("")) {
			bean = dao.getVaccinationBean(Integer.parseInt(vaccinationId));
		}
		request.setAttribute("bean", bean);
		request.setAttribute("vaccinationTypesAndIds", json.serialize(ConversionUtils.copyListDynaBeansToMap(dao.listAll())));
		return mapping.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,HttpServletResponse response)
	throws SQLException, Exception {
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
				boolean exists = dao.exist("vaccination_type", (String)bean.get("vaccination_type"));
				if (exists) {
					error = "Vaccination Type name already exists.....";
				} else {
					int vaccinationId = dao.getNextSequence();
					bean.set("vaccination_type_id", vaccinationId);
					success = dao.insert(con, bean);
					if (!success) {
						error = "Fail to add Vaccination Types Master....";
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
			redirect = new ActionRedirect(mapping.findForward("addRedirect"));
			flash.error(error);

		}else {
			redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("vaccination_type_id", bean.get("vaccination_type_id"));
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
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

			String key = req.getParameter("vaccination_type_id");

			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("vaccination_type_id",  Integer.parseInt(key));
			FlashScope flash = FlashScope.getScope(req);

			if (errors.isEmpty()) {
				int success = dao.update(con, bean.getMap(), keys);
				if (success > 0) {
					con.commit();
					flash.success("Vaccination Type Master details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update Vaccination Type Master..");
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
			ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("vaccination_type_id", bean.get("vaccination_type_id"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}


}
