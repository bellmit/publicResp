/**
 *
 */
package com.insta.hms.master.PBMObservations;

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
 * @author lakshmi
 *
 */
public class PBMObservationsMasterAction extends DispatchAction {

	PBMObservationsMasterDAO dao = new PBMObservationsMasterDAO();

	public ActionForward list(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse resp) throws IOException, ServletException, Exception {

		Map requestParams = req.getParameterMap();
		PagedList pagedList = dao.getPBMObservationsList(requestParams,
						ConversionUtils.getListingParameter(requestParams));
		req.setAttribute("pagedList", pagedList);

		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse resp) throws IOException, ServletException, Exception {

		List<BasicDynaBean> patMedCols = ConversionUtils.listBeanToListMap(dao.getPatientPrescriptionColumns());
		req.setAttribute("patMedCols", patMedCols);
		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse resp) throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;

		if (errors.isEmpty()) {
			String observation_name = (String)bean.get("observation_name");
			String patient_med_presc_value_column = (String)bean.get("patient_med_presc_value_column");
			BasicDynaBean exists = dao.findPBMObservation(observation_name, patient_med_presc_value_column);
			if (exists == null) {
				bean.set("id", dao.getNextSequence());
				boolean success = dao.insert(con, bean);
				if (success) {
					con.commit();
					flash.success("PBM Observation details inserted successfully..");
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					redirect.addParameter("id", bean.get("id"));
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					DataBaseUtil.closeConnections(con,null);
					return redirect;
				} else {
					con.rollback();
					flash.error("Failed to add PBM Observation.");
				}
			} else {
				flash.error("PBM Observation Name/Patient Medicine Presc. Column already exists..");
			}
		} else {
			flash.error("Incorrectly formatted values supplied");
		}
		redirect = new ActionRedirect(m.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse resp) throws IOException, ServletException, Exception {

		BasicDynaBean bean = dao.findByKey("id", new Integer(req.getParameter("id")));
		req.setAttribute("bean", bean);

		List<BasicDynaBean> patMedCols = ConversionUtils.listBeanToListMap(dao.getPatientPrescriptionColumns());
		req.setAttribute("patMedCols", patMedCols);
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		Map params = req.getParameterMap();
		List errors = new ArrayList();

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		Object key = req.getParameter("id");
		Map keys = new HashMap();
		keys.put("id", Integer.parseInt(key.toString()));
		FlashScope flash = FlashScope.getScope(req);

		if (errors.isEmpty()) {
			String observation_name = (String)bean.get("observation_name");
			String patient_med_presc_value_column = (String)bean.get("patient_med_presc_value_column");
			BasicDynaBean exists = dao.findPBMObservation(observation_name, patient_med_presc_value_column);
			if (exists != null && !key.equals(exists.get("id").toString())) {
				flash.error("PBM Observation Name/Patient Medicine Presc. Column already exists..");
			}
			else {
				int success = dao.update(con, bean.getMap(), keys);

				if (success > 0) {
					con.commit();
					flash.success("PBM Observation details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update PBM Observation details..");
				}
			}
		}
		else {
			flash.error("Incorrectly formatted values supplied");
		}
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("id" , bean.get("id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}
}
