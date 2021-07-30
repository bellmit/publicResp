package com.insta.hms.master.PharmacyRetailDoctor;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import flexjson.JSONSerializer;

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

public class PharmacyRetailDoctorAction extends DispatchAction{
  
  private static final GenericDAO storeRetailDoctorDAO = new GenericDAO("store_retail_doctor");

	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		PharmacyRetailDoctorDAO dao = new PharmacyRetailDoctorDAO();
		Map requestParams = req.getParameterMap();
		PagedList pagedList = dao.search(requestParams,
					ConversionUtils.getListingParameter(req.getParameterMap()), "doctor_id");
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
		String error = null;

		BasicDynaBean bean = storeRetailDoctorDAO.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;
		try {
			if (errors.isEmpty()) {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				BasicDynaBean exists = storeRetailDoctorDAO.findByKey("doctor_name", bean.get("doctor_name"));
				if (exists == null) {
					bean.set("doctor_id", storeRetailDoctorDAO.getNextSequence());
					boolean success = storeRetailDoctorDAO.insert(con, bean);
					if (success) {
						con.commit();
						redirect = new ActionRedirect(m.findForward("showRedirect"));
						flash.success("Retail Doctor master details inserted successfully..");
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						redirect.addParameter("doctor_id", bean.get("doctor_id"));
						return redirect;
					} else {
						con.rollback();
						error =  "Failed to add  Doctor..";
					}
				} else {
					error =  "Doctor name already exists..";
				}
			} else {
				error = "Incorrectly formatted values supplied";
			}
			flash.error(error);
			redirect = new ActionRedirect(m.findForward("addRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		PharmacyRetailDoctorDAO cdao = new PharmacyRetailDoctorDAO();
		Object doctorId = req.getAttribute("doctor_id");
		if (doctorId == null)  doctorId = req.getParameter("doctor_id");
		BasicDynaBean bean = storeRetailDoctorDAO.findByKey("doctor_id",Integer.parseInt(doctorId.toString()));
		req.setAttribute("bean", bean);
		req.setAttribute("doctorsLists", js.serialize(cdao.getPharmacyRetailDoctorsNamesAndIds()));

		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		String error = null;

		BasicDynaBean bean = storeRetailDoctorDAO.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		Object key = ((Object[])params.get("doctor_id"))[0];
		Map<String, Integer> keys = new HashMap<String, Integer>();
		keys.put("doctor_id", Integer.parseInt(key.toString()));
		FlashScope flash = FlashScope.getScope(req);
		try {
			if (errors.isEmpty()) {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				DynaBean exists = storeRetailDoctorDAO.findByKey("doctor_name", bean.get("doctor_name"));
				if (exists != null && !key.equals(exists.get("doctor_id").toString())) {
					flash.error("Doctor name already exists..");
				}
				else {
					int success = storeRetailDoctorDAO.update(con, bean.getMap(), keys);

					if (success > 0) {
						con.commit();
						flash.success("Doctor master details updated successfully..");
					} else {
						con.rollback();
						flash.error("Failed to update Doctor master details..");
					}
				}
			}
			else {
	             flash.error("Incorrectly formatted values supplied");
			}
			ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("doctor_id", bean.get("doctor_id"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}



}
