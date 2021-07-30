/**
 *
 */
package com.insta.hms.Registration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.RecurrenceDailyMaster.RecurrenceDailyMasterDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.outpatient.PrescriptionBO;
import com.insta.hms.wardactivities.prescription.IPPrescriptionDAO;

import flexjson.JSONSerializer;

/**
 * @author mithun.saha
 *
 */
public class AdmissionRequestAction extends DispatchAction{
	static Logger log = LoggerFactory.getLogger(AdmissionRequestAction.class);
	IPPrescriptionDAO prescDAO = new IPPrescriptionDAO();
	AdmissionRequestDAO admreqdao = new AdmissionRequestDAO();
	GenericDAO userDAO = new GenericDAO("u_user");
	RecurrenceDailyMasterDAO rdmDAO = new RecurrenceDailyMasterDAO();

	@IgnoreConfidentialFilters
	public ActionForward getAdmissionRequestList(ActionMapping mapping,ActionForm form,
			HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException,Exception {

		Map reqParams = req.getParameterMap();
		Integer centerId = null;
		String centerIdStr = req.getParameter("_center_id");
		if(centerIdStr != null && !centerIdStr.isEmpty())
			centerId = Integer.parseInt(centerIdStr);

		PagedList pagedList = admreqdao.getPatientAdmissionRequest(reqParams,ConversionUtils.getListingParameter(req.getParameterMap()), centerId);
		req.setAttribute("PagedList", pagedList);
		req.setAttribute("centers", CenterMasterDAO.getAllCentersExceptSuper());
		return mapping.findForward("list");
	}

	@IgnoreConfidentialFilters
	public ActionForward addNewAdmissionRequest(ActionMapping mapping,ActionForm form,
			HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException,Exception {
		String mrno= (String) req.getParameter("mr_no");
		Map patmap = null;
		if (mrno != null && !mrno.equals("")) {
			patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrno);

			if (patmap == null) {
				FlashScope flash = FlashScope.getScope(req);
				String msg = getResources(req).getMessage("patient.admissionrequest.action.error.does.not.exists.message");
				flash.put("error", mrno+" "+msg+".");
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("addshowredirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
		}
		setRequestAttributes(req,patmap);
		return mapping.findForward("addshow");
	}

	@SuppressWarnings("unchecked")
  public void setRequestAttributes(HttpServletRequest req,Map patmap) throws SQLException{
		JSONSerializer js = new JSONSerializer().exclude("class");
		HttpSession session  = req.getSession(false);
		String admissionRequestId = req.getParameter("adm_request_id");
		BasicDynaBean userbean = userDAO.findByKey("emp_username", session.getAttribute("userId"));
		BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
		BasicDynaBean admissionRequestDetailsBean = null;
		String patientId = req.getParameter("patient_id");
		String chiefComplaint = null;
		List<BasicDynaBean> diagnosisDetails = new ArrayList<BasicDynaBean>();
		List doctors = Collections.EMPTY_LIST;
		if (GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1 && RequestContext.getCenterId() != 0)
			doctors =DoctorMasterDAO.getAllCenterActiveDoctors(RequestContext.getCenterId());
		else
			doctors = DoctorMasterDAO.getAllActiveDoctors();
		if(admissionRequestId != null && !admissionRequestId.isEmpty()) {
			admissionRequestDetailsBean = admreqdao.findByKey("adm_request_id", Integer.parseInt(admissionRequestId));
			req.setAttribute("admissionRequestDetails", admissionRequestDetailsBean.getMap());
		}
		Integer patientCenterId = admissionRequestDetailsBean != null ? (Integer)admissionRequestDetailsBean.get("center_id")
				: RequestContext.getCenterId();
		String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(patientCenterId);
		req.setAttribute("requestingDoctors", doctors);
		req.setAttribute("doctors", js.serialize(ConversionUtils.copyListDynaBeansToMap(doctors)));
		req.setAttribute("prescriptions", prescDAO.getPrescriptions((admissionRequestId != null &&
					!admissionRequestId.isEmpty()) ? Integer.parseInt(admissionRequestId) : null));
		if(patientId != null) {
			diagnosisDetails = MRDDiagnosisDAO.getAllDiagnosisDetails(patientId);
			chiefComplaint = (String)new VisitDetailsDAO().findByKey("patient_id", patientId).get("complaint");
		} else if (admissionRequestId != null && !admissionRequestId.isEmpty()) {
			diagnosisDetails = MRDDiagnosisDAO.getAllDiagnosisDetails(Integer.parseInt(admissionRequestId));
		}
		req.setAttribute("diagnosis_details", diagnosisDetails);
		req.setAttribute("defaultDiagnosisCodeType", HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthority).getDiagnosis_code_type());
		req.setAttribute("patmap", patmap);
		req.setAttribute("userBean", js.deepSerialize(userbean.getMap()));
		req.setAttribute("genericPrefs", genericPrefs.getMap());
		Map filterMap = new HashMap<>();
    filterMap.put("status", "A");
    filterMap.put("medication_type", "M");
		req.setAttribute("frequencies", rdmDAO.listAll(null, filterMap, "display_order"));
		req.setAttribute("chief_complaint", chiefComplaint);
		req.setAttribute("validate_diagnosis_codification", GenericPreferencesDAO.getAllPrefs().get("validate_diagnosis_codification"));
		req.setAttribute("centers", CenterMasterDAO.getAllCentersExceptSuper());
	}

	public ActionForward saveAdmissionRequestDetails(ActionMapping mapping,ActionForm form,
			HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException,Exception {
		PrescriptionBO pbo = new PrescriptionBO();
		AdmissionRequestDAO admdao = new AdmissionRequestDAO();
		FlashScope flash = FlashScope.getScope(req);
		boolean success = false;
		Connection con = null;
		String error = null;
		BasicDynaBean admReqBean = null;
		String mrNo = req.getParameter("mr_no");
		String[] prescriptions = req.getParameterValues("h_prescription_id");
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addshowredirect"));
		Map params = req.getParameterMap();
		HttpSession session = req.getSession(false);
		String userName = (String)session.getAttribute("userid");
		String[] diagnosis_ids = req.getParameterValues("diagnosis_id");
		String[] diagnosis_code = req.getParameterValues("diagnosis_code");
		String[] diagnosis_description = req.getParameterValues("diagnosis_description");
		String[] diagnosis_year_of_onset = (String[]) params.get("diagnosis_year_of_onset");
		String[] diagnosis_status_id = req.getParameterValues("diagnosis_status_id");
		String[] diagnosis_remarks = req.getParameterValues("diagnosis_remarks");
		String[] diag_type = req.getParameterValues("diagnosis_type");
		String[] diag_delete = req.getParameterValues("diagnosis_deleted");
		String[] diag_edited = req.getParameterValues("diagnosis_edited");
		String[] diag_doctor = req.getParameterValues("diagnosis_doctor_id");
		BasicDynaBean userBean = userDAO.findByKey("emp_username", session.getAttribute("userId"));
		String[] diag_datetime = req.getParameterValues("diagnosis_datetime");
		String admissionReqId = req.getParameter("adm_request_id");
		String centerId = req.getParameter("d_center_id");
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			txn: {
				List errorFields = new ArrayList();
				admReqBean = admdao.getBean();
				ConversionUtils.copyToDynaBean(params, admReqBean, errorFields);
				if (errorFields.isEmpty()) {
					if(centerId != null && !centerId.isEmpty())
						admReqBean.set("center_id", Integer.parseInt(centerId));
					else
						admReqBean.set("center_id", RequestContext.getCenterId());
					admReqBean.set("request_date", DateUtil.getCurrentTimestamp());
					admReqBean.set("user_name", userName);
					admReqBean.set("mod_time", DateUtil.getCurrentTimestamp());
					if(admissionReqId != null && admissionReqId.isEmpty()) {
						admReqBean.set("adm_request_id", admdao.getNextSequence());
						admReqBean.set("status", "P");
						admdao.insert(con, admReqBean);
					} else {
						Map<String,Object> keys = new HashMap<String, Object>();
						keys.put("adm_request_id", Integer.parseInt(admissionReqId));
						admdao.update(con, admReqBean.getMap(), keys);
					}
					
					String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter((Integer) admReqBean.get("center_id"));
					String codeType = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthority).getDiagnosis_code_type();
					if (diagnosis_ids != null) {
						for (int i=0; i<diagnosis_ids.length-1; i++) {
							if (!pbo.updateDiangoisDetails(con, diagnosis_ids[i], diagnosis_code[i],
									diagnosis_description[i], diagnosis_year_of_onset[i], diagnosis_status_id[i], diagnosis_remarks[i],
									diag_doctor[i], diag_datetime[i], new Boolean(diag_delete[i]),
									new Boolean(diag_edited[i]), null, diag_type[i], userName,(Integer)admReqBean.get("adm_request_id"), "N", 
									null, admReqBean, codeType))
								break txn;
						}
					}

					error = new PrescriptionBO().saveIpPrescriptions(con,(Integer)admReqBean.get("adm_request_id"),
							prescriptions, params, null, userName, req.getParameterValues("h_delete"),
							(String) userBean.get("is_shared_login"),req.getParameterValues("h_edited"));
					if(error != null)
						break txn;

					if(error == null)
						success = true;
				}
			}
		}finally {
			DataBaseUtil.commitClose(con, success);
		}
		flash.put("error", error);
		redirect.addParameter("mr_no", mrNo);
		redirect.addParameter("adm_request_id", admReqBean != null ? admReqBean.get("adm_request_id") : null);
		redirect.addParameter("doctor_id", req.getParameter("requesting_doc"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward getCancelAdmissionRequest(ActionMapping mapping,ActionForm form,
			HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException,Exception {

		String admissionRequestId = req.getParameter("adm_request_id");
		BasicDynaBean admissionBean = AdmissionRequestDAO.getCancelledAdmissionRequest(Integer.parseInt(admissionRequestId));
		req.setAttribute("userName", req.getSession(false).getAttribute("userid"));
		if(admissionBean != null)
			req.setAttribute("admissionBean", admissionBean.getMap());
		return mapping.findForward("getCancelScreen");
	}

	public ActionForward cancelAdmissionRequest(ActionMapping mapping,ActionForm form,
			HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException,Exception {

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("cancelRedirect"));
		FlashScope flash = FlashScope.getScope(req);
		String admissionRequestId = req.getParameter("adm_request_id");
		String cancelledBy = req.getParameter("cancelledBy");
		String cancellationRemarks = req.getParameter("cancel_remarks");
		String cancellationDate = req.getParameter("cancellation_date");
		Timestamp cancellationTimeStamp = null;
		String timeStr = DateUtil.getCurrentTime()+"";
		String error = null;
		if(cancellationDate != null) {
			String cancellationTimeStr = cancellationDate+ " "+timeStr;
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy k:mm");
	        java.util.Date date = (java.util.Date)dateFormat.parse(cancellationTimeStr);
	        cancellationTimeStamp = new java.sql.Timestamp(date.getTime());
		}
		Connection con = null;
		boolean success = true;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BasicDynaBean admissionBean = admreqdao.findByKey("adm_request_id", Integer.parseInt(admissionRequestId));
			if(admissionBean != null && admissionBean.get("status").equals("X")) {
				error = getResources(req).getMessage("patient.admissionrequest.cancel.error.msg");
			} else {
				AdmissionRequestDAO.updateCancelAdmissionAttributes(con, cancelledBy, cancellationRemarks, cancellationTimeStamp,
						Integer.parseInt(admissionRequestId));
				success = true;
			}
		} finally {
			DataBaseUtil.commitClose(con, success);
		}
		redirect.addParameter("adm_request_id", admissionRequestId);
		redirect.addParameter("mr_no", req.getParameter("mr_no"));
		flash.put("error", error);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward convertAdmissionRequestToIp(ActionMapping mapping,ActionForm form,
			HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException,Exception {

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("IpRegistrationRedirect"));
		FlashScope flash = FlashScope.getScope(req);
		String admissionRequestId = req.getParameter("adm_request_id");
		String mrNo = req.getParameter("mr_no");
		redirect.addParameter("adm_request_id", admissionRequestId);
		redirect.addParameter("mr_no", mrNo);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}
