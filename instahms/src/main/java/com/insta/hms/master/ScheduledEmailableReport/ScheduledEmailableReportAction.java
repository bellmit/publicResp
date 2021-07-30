/**
 *
 */
package com.insta.hms.master.ScheduledEmailableReport;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.scheduledreport.BuiltinEmailReportProvider;
import com.insta.hms.scheduledreport.CustomEmailReportProvider;
import com.insta.hms.scheduledreport.Event;
import com.insta.hms.scheduledreport.FavouriteEmailReportProvider;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna.t
 *
 */
public class ScheduledEmailableReportAction extends BaseAction {

	ScheduledEmailableReportDAO dao = new ScheduledEmailableReportDAO();

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		List list = dao.getReports();
		request.setAttribute("configuredReportsList", ConversionUtils.listBeanToListMap(list));

		JSONSerializer js = new JSONSerializer();
		BuiltinEmailReportProvider builtin = new BuiltinEmailReportProvider();
		// list of all builtin reports as JSON
		List builtinReports = builtin.listAvailableReports();
		request.setAttribute("builtinReportsJSON", js.deepSerialize(builtinReports));
		// a map of report_id => report_name for translation to names on load
		request.setAttribute("builtinReportMap",
				ConversionUtils.listMapToMapMap(builtinReports, "report_id"));

		CustomEmailReportProvider custom = new CustomEmailReportProvider();
		List customReports = custom.listAvailableReports();
		request.setAttribute("customReportsJSON", js.deepSerialize(customReports));
		request.setAttribute("customReportMap", ConversionUtils.listMapToMapMap(customReports, "report_id"));

		FavouriteEmailReportProvider favourite = new FavouriteEmailReportProvider();
		List favouriteReports = favourite.listAvailableReports();
		request.setAttribute("favouriteReportsJSON", js.deepSerialize(favouriteReports));
		request.setAttribute("favouriteReportMap",
				ConversionUtils.listMapToMapMap(favouriteReports, "report_id"));

		request.setAttribute("eventsJSON", js.deepSerialize(Event.ALL.getEventMap()));

		return mapping.findForward("list");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, FileUploadException,
			SQLException {
		Map params = getParameterMap(request);
		String[] docIds = request.getParameterValues("doc_id");
		String[] deleteReports = request.getParameterValues("delete");
		String[] reportIds = request.getParameterValues("report_id");

		List<BasicDynaBean> updateReportsList = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> insertReportsList = new ArrayList<BasicDynaBean>();
		List<Integer> deleteReportsList = new ArrayList<Integer>();

		List errorFields = new ArrayList();

		for (int i=0; i< docIds.length; i++) {
			String docId = docIds[i];
			String delete = deleteReports[i];
			if (docId.equals("") && delete.equals("N") && !reportIds[i].equals("")) {
				// inserts
				BasicDynaBean bean = dao.getBean();
				ConversionUtils.copyIndexToDynaBean(params, i, bean, errorFields);
				insertReportsList.add(bean);

			}
			if (!docId.equals("") && delete.equals("N")) {
				// updates
				BasicDynaBean bean = dao.getBean();
				ConversionUtils.copyIndexToDynaBean(params, i, bean, errorFields);
				updateReportsList.add(bean);

			}
			if (!docId.equals("") && delete.equals("Y")) {
				// deletes
				deleteReportsList.add(Integer.parseInt(docId));
			}
		}

		boolean successFlag = false;
		String error = null;
		String success = null;
		Connection con = null;

		try {
			txn: {
				if (!errorFields.isEmpty()) {
					error = "Incorrectly formatted details supplied..";
					break txn;
				} else {
					con = DataBaseUtil.getConnection();
					con.setAutoCommit(false);

					for (BasicDynaBean bean: insertReportsList) {
						bean.set("doc_id", dao.getNextSequence());
						if (!dao.insert(con, bean)) {
							error = "ScheduledEmailableReportDAO insert error...";
							break txn;
						}
					}

					for (BasicDynaBean bean: updateReportsList) {
						if (bean!=null && !bean.getMap().isEmpty() && dao.update(con, bean.getMap(), "doc_id", bean.get("doc_id")) != 1) {
							error = "ScheduledEmailableReportDAO update error...";
							break txn;
						}
					}
					for (int doc_id: deleteReportsList) {
						if (!dao.delete(con, "doc_id", doc_id)){
							error = "ScheduledEmailableReportDAO delete error...";
							break txn;
						}
					}

					successFlag = true;
				}
			}
		} finally {
			DataBaseUtil.commitClose(con, successFlag);
			if (successFlag) success = "Transaction Successful..";
			else error = "Transaction Failure..\n"+error==null?"":error;
		}
		FlashScope flash = FlashScope.getScope(request);
		flash.put("success", success);
		flash.put("error", error);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

}
