package com.insta.hms.master.Dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

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
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class DialMachineMasterAction extends DispatchAction {

	public ActionForward list(ActionMapping mapping, ActionForm form,
				HttpServletRequest request, HttpServletResponse response)throws IOException, ServletException, Exception {

		DialysisMachineMasterDAO dao = new DialysisMachineMasterDAO();

		Map map = request.getParameterMap();

		PagedList pagedList = dao.getDialMachineMasterList(map, ConversionUtils.getListingParameter(request.getParameterMap()));
		request.setAttribute("locations", DialLocationMasterDAO.getAvalDialLocations());
		request.setAttribute("pagedList", pagedList);

		return mapping.findForward("list");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException, Exception {

	FlashScope flash = FlashScope.getScope(request);
	DialysisMachineMasterDAO dao = new DialysisMachineMasterDAO();
	ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));

	BasicDynaBean bean = dao.getBean();
	ArrayList errorFields = new ArrayList();
	Map params = request.getParameterMap();
	ConversionUtils.copyToDynaBean(params, bean, errorFields);

	String status = request.getParameter("status");
	String machineName = request.getParameter("machine_name");
	Connection con = DataBaseUtil.getConnection();
	con.setAutoCommit(false);

	if (errorFields.isEmpty()) {
		BasicDynaBean exists = dao.findByKey("machine_name", machineName);

		BasicDynaBean genPrefs = GenericPreferencesDAO.getAllPrefs();
		Object max_dial_machine_count = genPrefs.get("max_dial_machine_count");
		String machine_count = DialysisMachineMasterDAO.getMachineCount();
		int dial_machine_count = Integer.parseInt(machine_count);

		if(null!=max_dial_machine_count && !"".equals(max_dial_machine_count)) {
			int maxdialmachinecount = (Integer) max_dial_machine_count;
			if(dial_machine_count < maxdialmachinecount ) {
				if (exists == null) {
					bean.set("machine_id", dao.getNextSequence());
					boolean success = dao.insert(con, bean);
					if(success) {
						con.commit();
						flash.success("Details Inserted successfully");
						redirect = new ActionRedirect(mapping.findForward("showRedirect"));
						redirect.addParameter("machine_id", bean.get("machine_id"));
						con.close();
						return redirect;
					} else {
						flash.error("Failed to insert details");
					}
				} else {
					flash.error("Mchine name already exists");
				}
			}else{

				flash.error("Max number of dialysis machines reached");
			}
	} else{
		if (exists == null) {
			bean.set("machine_id", dao.getNextSequence());
			boolean success = dao.insert(con, bean);
			if(success) {
				con.commit();
				flash.success("Details Inserted successfully");
				redirect = new ActionRedirect(mapping.findForward("showRedirect"));
				redirect.addParameter("machine_id", bean.get("machine_id"));
				con.close();
				return redirect;
			} else {
				flash.error("Failed to insert details");
			}
		} else {
			flash.error("Mchine name already exists");
		}
	}

	} else {
		flash.error("Incorrectly formatted details supplied");
	}

	redirect = new ActionRedirect(mapping.findForwardConfig("addRedirect"));
	redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
	con.close();
	return redirect;
}

	public ActionForward update(ActionMapping mapping, ActionForm form,
				HttpServletRequest request, HttpServletResponse response)throws IOException, ServletException, Exception {

		FlashScope flash = FlashScope.getScope(request);
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("showRedirect"));

		Map params = request.getParameterMap();
		DialysisMachineMasterDAO dao = new DialysisMachineMasterDAO();
		BasicDynaBean bean = dao.getBean();
		ArrayList errorFields = new ArrayList();

		Object key = request.getParameter("machine_id");
		Map<String, Integer> keys = new HashMap<String, Integer>();
		keys.put("machine_id", Integer.parseInt(key.toString()));
		String status = request.getParameter("status");

		ConversionUtils.copyToDynaBean(params, bean, errorFields);
		if(errorFields.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("machine_name", bean.get("machine_name"));
			if(exists != null && !key.equals(exists.get("machine_id").toString())) {
				flash.error("Machine name already exists");

			} else {
				int success = dao.update(con, bean.getMap(), keys);
				if (success == 1) {
					con.commit();
					flash.success("Details updated successfully");
				} else {
					con.rollback();
					flash.error("Failed to update");
				}
			}

		} else {
			flash.error("Incorrectly formatted values supplied");
		}

		con.close();
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("machine_id", request.getParameter("machine_id"));
		return redirect;
	}

	public ActionForward add(ActionMapping mapping, ActionForm form,
				HttpServletRequest request, HttpServletResponse response)throws IOException, ServletException, Exception {

		request.setAttribute("locations", DialLocationMasterDAO.getAvalDialLocations());
		return mapping.findForward("addshow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form,
				HttpServletRequest request, HttpServletResponse response)throws IOException, ServletException, Exception {

		DialysisMachineMasterDAO dao = new DialysisMachineMasterDAO();
		BasicDynaBean bean = dao.findByKey("machine_id",
					Integer.parseInt(request.getParameter("machine_id")));
		request.setAttribute("bean", bean);
		request.setAttribute("locations", DialLocationMasterDAO.getAvalDialLocations());
		return mapping.findForward("addshow");
	}


}
