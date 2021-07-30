package com.insta.hms.stores;

import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.dischargemedication.DischargeMedicationDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.outpatient.PrescribeDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PharmacyPrescriptionListAction extends BaseAction {

	static Logger log = LoggerFactory.getLogger(PharmacyPrescriptionListAction.class);

	@IgnoreConfidentialFilters
	public  ActionForward getList(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{
		Map<Object,Object> map= getParameterMap(request);
		
		String date_range = request.getParameter("date_range");
		String wkdate = null;
		if (date_range != null && date_range.equals("week")) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	        Calendar cal = Calendar.getInstance();
	        cal.add(Calendar.DATE, -7);
	        Date openDt = cal.getTime();
	        wkdate = dateFormat.format(openDt);

			map.put("visited_date", new String[]{wkdate, ""});
			map.put("visited_date@op", new String[]{"ge,le"});
			map.put("visited_date@cast", new String[]{"y"});
			map.remove("date_range");
		}
		
		PagedList list = PharmacyPrescriptionListDAO.searchPrescriptionList(map, ConversionUtils.getListingParameter(map));
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
		
		Integer printerId = (Integer) printpref.get("printer_id");
		request.setAttribute("printerId", printerId);
		request.setAttribute("pagedList", list);
		
		ActionForward forward = new ActionForward(mapping.findForward("list").getPath());
		// when ever user uses a pagination open_date should not append again.
		if (date_range != null && date_range.equals("week") && request.getParameter("visited_date") == null) {
			addParameter("visited_date", wkdate, forward);
	    }
		return forward;
		
	}

	public  ActionForward getSalesScreen(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

		String mr_no = request.getParameter("mr_no");
		String visit_id = request.getParameter("visit_id");
		String patstatus = request.getParameter("patstatus");
		String pbm_presc_id = request.getParameter("pbm_presc_id");
		boolean modEclaimErx = (Boolean)request.getSession(false).getAttribute("mod_eclaim_erx");
		ActionRedirect redirect = new ActionRedirect("/pages/stores/MedicineSales.do?method=getSalesScreen&phStore=0&ps_status=active");
		redirect.addParameter("visit_id", visit_id);
		redirect.addParameter("comingFromPrescList","Y");
		redirect.addParameter("patstatus",patstatus);
		if (modEclaimErx && null != pbm_presc_id) {
			redirect.addParameter("erx_pbm_presc_id", pbm_presc_id);
		}
		return redirect;
	}

	public ActionForward closePrescription(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		FlashScope flash = FlashScope.getScope(request);
		String[] consultationIds = request.getParameterValues("_closePrescription");
		boolean prescriptionStatus = PrescribeDAO.closeAll(consultationIds);
		//boolean dischargeMedicationStatus = DischargeMedicationDAO.closeAll(consultationIds);
		flash.put("error", (prescriptionStatus) ? null : "Failed to close the prescriptions.");
		ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer").
				replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}
}