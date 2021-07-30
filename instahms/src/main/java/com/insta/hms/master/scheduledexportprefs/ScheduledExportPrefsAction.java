/**
 *
 */
package com.insta.hms.master.scheduledexportprefs;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.accounting.AccountingExporterJobScheduling;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.FlashScope;
import com.insta.hms.master.Accounting.AccountingGroupMasterDAO;
import com.insta.hms.master.Accounting.AccountingPrefsDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.quartz.SchedulerException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna
 *
 */
public class ScheduledExportPrefsAction extends DispatchAction {

	ScheduledExportPrefsDAO dao = new ScheduledExportPrefsDAO();
	private static AccountingGroupMasterDAO accGroupDAO = new AccountingGroupMasterDAO();
	CenterMasterDAO centerDAO = new CenterMasterDAO();
	private static AccountingPrefsDAO acPrefsDAO = new AccountingPrefsDAO();

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException, ParseException {
		Map listing = ConversionUtils.getListingParameter(request.getParameterMap());
		request.setAttribute("pagedList", dao.searchScheduledJobs(request.getParameterMap(), listing));
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		request.setAttribute("acc_prefs", acPrefsDAO.getRecord());
		request.setAttribute("accGroups", accGroupDAO.getAssociatedAccountGroups());
		request.setAttribute("centers", centerDAO.getAllCentersExceptSuper());
		return mapping.findForward("addshow");
	}
	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		request.setAttribute("bean", dao.findByKey("schedule_id",
				Integer.parseInt(request.getParameter("schedule_id"))));
		request.setAttribute("acc_prefs", acPrefsDAO.getRecord());
		request.setAttribute("accGroups", accGroupDAO.getAssociatedAccountGroups());
		request.setAttribute("centers", centerDAO.getAllCentersExceptSuper());
		return mapping.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException,
			SchedulerException {
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		Map params = request.getParameterMap();
		BasicDynaBean bean = dao.getBean();
		List errorFields = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errorFields);
		String[] exportItems = request.getParameterValues("exportItems");
		StringBuilder itemsCommaSep = new StringBuilder("");
		if (exportItems != null) {
			boolean first = true;
			for (int i=0; i<exportItems.length; i++) {
				if (!exportItems[i].equals("")) {
					if (!first) {
						itemsCommaSep.append(",");
					}
					first = false;
					itemsCommaSep.append(exportItems[i]);
				}
			}
		}
		bean.set("export_items", itemsCommaSep.toString());
		String exportFor = request.getParameter("exportFor");
		int accountGroup = 1;
		int centerId = 0;
		if (exportFor.charAt(0) == 'C') {
			centerId = Integer.parseInt(exportFor.substring(1));
		} else if (exportFor.charAt(0) == 'A') {
			accountGroup = Integer.parseInt(exportFor.substring(1));
		}
		bean.set("center_id", centerId);
		bean.set("account_group", accountGroup);

		FlashScope flash = FlashScope.getScope(request);
		String error = null;

		if (errorFields.isEmpty()) {
			Connection con = DataBaseUtil.getConnection();
			try {
				if (dao.exist("schedule_name", bean.get("schedule_name"))) {
					error = "Schedule Name '"+bean.get("schedule_name")+"' already exists.";
				} else {
					bean.set("schedule_id", dao.getNextSequence());
					if (!dao.insert(con, bean))
						error = "Failed to insert Route Name..";
				}
			} catch(SQLException se) {
				if (DataBaseUtil.isDuplicateViolation(se)) {
					error = bean.get("directory") + ": directory already configured for another Export. Directory has to be different for each Prefs.";
				} else {
					throw se;
				}
			} finally {
				DataBaseUtil.closeConnections(con, null);
			}
		} else {
			error = "Incorrectly formatted values supplied..";
		}
		if (error != null) {
			flash.put("error", error);
		}
		if (error == null) {
			// remove the trigger(tally exporting job) for the existing period
			String schema = (String) request.getSession(false).getAttribute("sesHospitalId");
			AccountingExporterJobScheduling.unScheduleAccountingExportJob(schema);

			// add the trigger(tally exporting job) for the new period
			DynaBeanBuilder dynabean = new DynaBeanBuilder();
			dynabean.add("schema");
			BasicDynaBean schemaBean = dynabean.build();
			schemaBean.set("schema", schema);
			List<BasicDynaBean> l = new ArrayList<BasicDynaBean>();
			l.add(schemaBean);
			AccountingExporterJobScheduling.initializeAccountingExportJob(l);

			redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("schedule_id", bean.get("schedule_id"));
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException,
			SchedulerException {
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		Map params = request.getParameterMap();
		BasicDynaBean bean = dao.getBean();
		List errorFields = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errorFields);
		String[] exportItems = request.getParameterValues("exportItems");
		StringBuilder itemsCommaSep = new StringBuilder("");
		Integer schedule_id = Integer.parseInt(request.getParameter("schedule_id"));
		if (exportItems != null) {
			boolean first = true;
			for (int i=0; i<exportItems.length; i++) {
				if (!exportItems[i].equals("")) {
					if (!first) {
						itemsCommaSep.append(",");
					}
					first = false;
					itemsCommaSep.append(exportItems[i]);
				}
			}
		}
		bean.set("export_items", itemsCommaSep.toString());
		String exportFor = request.getParameter("exportFor");
		int accountGroup = 1;
		int centerId = 0;
		if (exportFor.charAt(0) == 'C') {
			centerId = Integer.parseInt(exportFor.substring(1));
		} else if (exportFor.charAt(0) == 'A') {
			accountGroup = Integer.parseInt(exportFor.substring(1));
		}
		bean.set("center_id", centerId);
		bean.set("account_group", accountGroup);

		FlashScope flash = FlashScope.getScope(request);
		String error = null;

		if (errorFields.isEmpty()) {
			Connection con = DataBaseUtil.getConnection();
			try {
				if (dao.exists(con, (String) bean.get("schedule_name"), (Integer) bean.get("schedule_id"))) {
					error = "Schedule Name '"+bean.get("schedule_name")+"' already exists.";
				} else {
					if (dao.update(con, bean.getMap(), "schedule_id", schedule_id) <= 0)
						error = "Failed to update Preferences..";
				}
			} catch(SQLException se) {
				if (DataBaseUtil.isDuplicateViolation(se)) {
					error = bean.get("directory") + ": directory already configured for another Export. Directory has to be different for each Prefs.";
				} else {
					throw se;
				}
			} finally {
				DataBaseUtil.closeConnections(con, null);
			}
		} else {
			error = "Incorrectly formatted values supplied..";
		}
		if (error != null) {
			flash.put("error", error);
		}
		if (error == null) {
			// remove the trigger(tally exporting job) for the existing period
			String schema = (String) request.getSession(false).getAttribute("sesHospitalId");
			AccountingExporterJobScheduling.unScheduleAccountingExportJob(schema);

			// add the trigger(tally exporting job) for the new period
			DynaBeanBuilder dynabean = new DynaBeanBuilder();
			dynabean.add("schema");
			BasicDynaBean schemaBean = dynabean.build();
			schemaBean.set("schema", schema);
			List<BasicDynaBean> l = new ArrayList<BasicDynaBean>();
			l.add(schemaBean);
			AccountingExporterJobScheduling.initializeAccountingExportJob(l);
		}
		redirect.addParameter("schedule_id", schedule_id);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException,
			SchedulerException {
		String[] prefsIdStrs = request.getParameterValues("_deletePrefs");
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		String error = null;
		try {
			if (prefsIdStrs != null) {
				for (String schedule_id : prefsIdStrs) {
					if (!dao.delete(con, "schedule_id", Integer.parseInt(schedule_id))) {
						error = "Failed to delete the Preference.";
						break;
					}
				}
			}
		} finally {
			DataBaseUtil.commitClose(con, error == null);
		}

		if (error == null) {
			//	remove the trigger(tally exporting job) for the existing period
			String schema = (String) request.getSession(false).getAttribute("sesHospitalId");
			AccountingExporterJobScheduling.unScheduleAccountingExportJob(schema);

			// add the trigger(tally exporting job) for the new period
			DynaBeanBuilder dynabean = new DynaBeanBuilder();
			dynabean.add("schema");
			BasicDynaBean schemaBean = dynabean.build();
			schemaBean.set("schema", schema);
			List<BasicDynaBean> l = new ArrayList<BasicDynaBean>();
			l.add(schemaBean);
			AccountingExporterJobScheduling.initializeAccountingExportJob(l);
		}

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer").
				replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		if (error != null) {
			flash.put("error", error);
		} else {
			flash.put("success", "deleted preference succesfully.");
		}
		return redirect;
	}

}
