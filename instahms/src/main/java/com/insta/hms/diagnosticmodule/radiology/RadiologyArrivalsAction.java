/**
 *
 */
package com.insta.hms.diagnosticmodule.radiology;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryBO;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.insta.hms.diagnosticmodule.prescribetest.DiagnoDAOImpl;
import com.insta.hms.diagnosticsmasters.outhousemaster.OutHouseMasterDAO;
import com.insta.hms.master.DiagnosticDepartmentMaster.DiagnosticDepartmentMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.InComingHospitals.InComingHospitalDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import flexjson.JSONSerializer;

/**
 * @author krishna
 *
 */
public class RadiologyArrivalsAction extends DispatchAction {
  
  private static final GenericDAO testsPrescribedDAO = new GenericDAO("tests_prescribed");

  @IgnoreConfidentialFilters
	public ActionForward getRadiologyArrivalScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException, Exception {

		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();

		HttpSession session = request.getSession(false);
		String userId = (String) session.getAttribute("userid");
		String userDept = new DiagnosticDepartmentMasterDAO().getUserDepartment(userId);
		Map params = new HashMap(request.getParameterMap());
		Object[] department = (Object[]) params.get("ddept_id");
		LaboratoryBO bo = new LaboratoryBO();

		String deptId = "";
		if (department == null || department[0] == null ) {
			if (userDept != null && !userDept.equals(""))
				deptId = userDept;
		} else {
			deptId = (String) department[0];
		}
		params.put("ddept_id", new String[]{deptId});
		params.put("category", new String[]{"DEP_RAD"});
		int centerId = (Integer) request.getSession(false).getAttribute("centerId");
		if (centerId != 0) {
			params.put("center_id", new String[]{centerId+""});
			params.put("center_id@type", new String[]{"integer"});
		}
		String[] conductionStatus = (String[]) params.get("conducted");
		if (conductionStatus == null || conductionStatus[0] == null || conductionStatus[0].equals("")) {
			params.put("conducted", new String[]{"N", "NRN", "MA"});
		}

		String date_range = request.getParameter("date_range");
		String week_start_date = null;
		if (date_range != null && date_range.equals("week")) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	        Calendar cal = Calendar.getInstance();
	        cal.add(Calendar.DATE, -7);
	        Date openDt = cal.getTime();
	        week_start_date = dateFormat.format(openDt);

			params.put("pres_date", new String[]{week_start_date, ""});
			params.put("pres_date@op", new String[]{"ge,le"});
			params.put("pres_date@cast", new String[]{"y"});
			params.remove("date_range");
		}

		Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(params);
		PagedList list = LaboratoryBO.unfinishedTestsList(params, listingParams, diagGenericPref,centerId, null);
		List<BasicDynaBean> incomingHospitals = new InComingHospitalDAO().listAll(
				 Arrays.asList(new String[] {"hospital_id","hospital_name"}), "status", "A", "hospital_name");
		List outHouses = OutHouseMasterDAO.getAllOutSources();
		JSONSerializer js = new JSONSerializer().exclude("class");

		request.setAttribute("islabNoReq", (String)diagGenericPref.get("autogenerate_labno"));
		request.setAttribute("pagedList", list);
		request.setAttribute("diagGenericPref", diagGenericPref);
		request.setAttribute("inHouses", js.serialize(ConversionUtils.copyListDynaBeansToMap(incomingHospitals)));
		request.setAttribute("outHouses", js.serialize(outHouses));
		request.setAttribute("category", "DEP_RAD");
		request.setAttribute("module", "DEP_RAD");
		request.setAttribute("userDept", userDept);
		request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
		request.setAttribute("directBillingPrefs", ConversionUtils.listBeanToMapBean(
				new GenericDAO("hosp_direct_bill_prefs").listAll(),"item_type"));
		request.setAttribute("test_timestamp", new DiagnoDAOImpl().getCountFromDiagTimeStamp());
		List l2 = bo.getLabtechnicions("DEP_RAD", centerId);
		request.setAttribute("doctors", l2);

		ActionForward forward = new ActionForward(mapping.findForward("arrivalsScreen").getPath());
		// when ever user uses a pagination pres_date should not append again.
		if (date_range != null && date_range.equals("week") && request.getParameter("pres_date") == null) {
			addParameter("pres_date", week_start_date, forward);
	    }

		return forward;

	}

	public void addParameter(String key, String value, ActionForward forward) {
        StringBuffer sb = new StringBuffer(forward.getPath());
        if (key == null || key.length() < 1)
            return ;
        if (forward.getPath().indexOf('?') == -1)
            sb.append('?');
        else
            sb.append('&');
        sb.append(key + "=" + value);
        forward.setPath(sb.toString());
    }

	public ActionForward setConductingDoctor(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException, Exception {
		String[] prescribedIDS = request.getParameterValues("completeCheck");
		String doctorID = request.getParameter("conducting_doctor");
		if (doctorID != null && doctorID.equals(""))
			doctorID = null;
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean flag = true;

		if (prescribedIDS != null) {
			try {
				for (int i=0; i<prescribedIDS.length; i++) {
					LaboratoryDAO.updateActivityDetails(con, "DIA", prescribedIDS[i], doctorID);
				}
			} catch(Exception e) {
				flag = false;
				throw e;
			} finally {
				DataBaseUtil.commitClose(con, flag);
			}
		}

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer").
				replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	public ActionForward setModalityArrived(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException, Exception {
		String[] prescribedIds = request.getParameterValues("completeCheck");
		String doctorID = request.getParameter("conducting_doctor");
		if (doctorID != null && doctorID.equals(""))
			doctorID = null;
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean flag = true;
		if (prescribedIds != null) {
			flag = false;
			try {
				txn : {
					for (int i=0; i<prescribedIds.length; i++) {
						BasicDynaBean bean = testsPrescribedDAO.getBean();
						bean.set("conducted", "MA");
						int prescId = Integer.parseInt(prescribedIds[i]);
						if (testsPrescribedDAO.update(con, bean.getMap(), "prescribed_id", prescId) == 0)
							break txn;
					}
					flag = true;
				}
			} finally {
				DataBaseUtil.commitClose(con, flag);
			}
		}

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer").
				replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		if (!flag) {
			flash.put("error", "Failed to mark as Patient Arrived..");
		} else {
			flash.put("success", "Succefully marked as Patient Arrived..");
		}
		return redirect;
	}

	public ActionForward conductionCompleted(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException, Exception {

		String[] prescribedIds = request.getParameterValues("completeCheck");
		String doctorID = request.getParameter("conducting_doctor");
		if (doctorID != null && doctorID.equals(""))
			doctorID = null;
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean flag = true;
		if (prescribedIds != null) {
			flag = false;
			try {
				txn : {
					for (int i=0; i<prescribedIds.length; i++) {
						BasicDynaBean bean = testsPrescribedDAO.getBean();
						bean.set("conducted", "CC");
						int prescId = Integer.parseInt(prescribedIds[i]);
						if (testsPrescribedDAO.update(con, bean.getMap(), "prescribed_id", prescId) == 0)
							break txn;
					}
					flag = true;
				}
			} finally {
				DataBaseUtil.commitClose(con, flag);
			}
		}

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer").
				replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		if (!flag) {
			flash.put("error", "Failed to mark as Conduction Completed..");
		} else {
			flash.put("success", "Succefully marked as Conduction Completed..");
		}
		return redirect;
	}

}
