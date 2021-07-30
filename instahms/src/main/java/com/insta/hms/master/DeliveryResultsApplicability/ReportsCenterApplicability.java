package com.insta.hms.master.DeliveryResultsApplicability;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReportsCenterApplicability extends DispatchAction{
  
  private static final GenericDAO centerReportDelivTimesDefault =
      new GenericDAO("center_report_deliv_times_default");

	public ActionForward getScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		CenterMasterDAO dao = new CenterMasterDAO();
	
		int centerId = Integer.parseInt(request.getParameter("center_id"));
		BasicDynaBean bean = dao.findByKey("center_id", centerId);
		List reportsAvailabilities = ReportsCenterApplicabilityDAO.getReportsAvailabilities(centerId);
		request.setAttribute("reportsAvailabilitiesList", reportsAvailabilities);
		if(reportsAvailabilities != null && reportsAvailabilities.size() != 0)
			request.setAttribute("reportsAvailableMap", ConversionUtils.listBeanToMapBean(reportsAvailabilities,"day_of_week_text"));
		request.setAttribute("bean", bean);
		return mapping.findForward("reports_applicability");
	}
	
	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String error = null;
		Connection con = null;
		boolean success = true;
		String centerId = request.getParameter("center_id");
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			con.setAutoCommit(false);
			Map params = request.getParameterMap();
			BasicDynaBean reportsDeliveryBean = null;
			String [] rDelete = request.getParameterValues("r_delete");
			String [] repDelivIds = request.getParameterValues("rep_deliv_default_id");
			List errorList = new ArrayList();
			String[] deliveryTime = request.getParameterValues("delivery_time");
			
 			SimpleDateFormat timeFormatterSecs = new SimpleDateFormat("HH:mm");
 			
 			if (repDelivIds != null) {
 				for (int j=0;j<repDelivIds.length;j++) {
 					if (!repDelivIds[j].equals("_") && !repDelivIds[j].equals("")) {
 						if (rDelete[j].equals("false") && !deliveryTime[j].equals("")) {
 							reportsDeliveryBean = centerReportDelivTimesDefault.getBean();
 							ConversionUtils.copyIndexToDynaBean(params,j, reportsDeliveryBean, errorList);
 							Map<String, Integer> keys = new HashMap<String, Integer>();
 							if (deliveryTime != null && deliveryTime[j] != null && !deliveryTime[j].equals(""))
									reportsDeliveryBean.set("delivery_time", new java.sql.Time(timeFormatterSecs.parse(deliveryTime[j]).getTime()));
								else
									reportsDeliveryBean.set("delivery_time",null);
 							keys.put("rep_deliv_default_id", Integer.parseInt(repDelivIds[j]));
 							keys.put("center_id", Integer.parseInt(centerId));
 							success = centerReportDelivTimesDefault.update(con, reportsDeliveryBean.getMap(), keys) > 0;
 							if (!success)
 								break;
 						} else {
 							success = centerReportDelivTimesDefault.delete(con, "rep_deliv_default_id", Integer.parseInt(repDelivIds[j]));
 							if (!success)
 								break;
 						}
 					} else {
 						if (deliveryTime[j]!=null && !repDelivIds[j].equals("")) {
 							reportsDeliveryBean = centerReportDelivTimesDefault.getBean();
 								ConversionUtils.copyIndexToDynaBean(params, j, reportsDeliveryBean, errorList);
 								reportsDeliveryBean.set("rep_deliv_default_id", centerReportDelivTimesDefault.getNextSequence());
 								if (deliveryTime != null && deliveryTime[j] != null && !deliveryTime[j].equals(""))
 									reportsDeliveryBean.set("delivery_time", new java.sql.Time(timeFormatterSecs.parse(deliveryTime[j]).getTime()));
 								else
 									reportsDeliveryBean.set("delivery_time",null);
 								reportsDeliveryBean.set("center_id", Integer.parseInt(centerId));
 								success = centerReportDelivTimesDefault.insert(con, reportsDeliveryBean);
 						 	}
 							if (!success) {
 								success = false;
 								break;
 							}
 						}
 					}
 				}
			
		} finally {
			DataBaseUtil.commitClose(con, error == null);
		}
	
		ActionRedirect redirect = new ActionRedirect(
				mapping.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(request);

		flash.put("error", error);
		redirect.addParameter("center_id", centerId);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
}
