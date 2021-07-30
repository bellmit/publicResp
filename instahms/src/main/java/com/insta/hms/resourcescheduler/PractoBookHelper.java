package com.insta.hms.resourcescheduler;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.integration.book.BookSDKUtil;
import com.insta.hms.integration.book.PatientDTO;
import com.insta.hms.master.CenterPreferences.CenterPreferencesDAO;
import com.insta.hms.master.DoctorCenterApplicability.CenterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.messaging.InstaIntegrationDao;
import com.practo.integration.sdk.AppointmentVisitEvent;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class PractoBookHelper {
	private static Logger logger = LoggerFactory.getLogger(PractoBookHelper.class);
	
    private static final GenericDAO schedulerAppointmentsDAO =
        new GenericDAO("scheduler_appointments");
	
	private PractoBookHelper() {}
	
	public static boolean isPractoAdvantageEnabled() {
		Preferences prefs = (Preferences) RequestContext.getSession().getAttribute("preferences");
		Map modules = prefs.getModulesActivatedMap();
		if (modules.containsKey(BookSDKUtil.MODULE_ID) && "Y".equals(modules.get(BookSDKUtil.MODULE_ID))) {
			return true;
		}
		return false;
	}
	
	private static boolean isBookEnabled(String doctorId, int centerId) throws SQLException {
		BasicDynaBean doctorDetails = DoctorMasterDAO.getDoctorDetails(doctorId, centerId);
		String doctorStatusOnPracto = null;
		if (doctorDetails != null && doctorDetails.get("status_on_practo") != null) {
			doctorStatusOnPracto = (String) doctorDetails.get("status_on_practo");
		}
		if (doctorStatusOnPracto != null && Arrays
				.asList(BookSDKUtil.CALENDAR_SYNC_CALLBACK_RECEIVED.toLowerCase(),
						BookSDKUtil.PRACTO_BOOK_ENABLED.toLowerCase(), BookSDKUtil.PRACTO_DOCTOR_LISTED.toLowerCase())
				.contains(doctorStatusOnPracto.toLowerCase())) {
			return true;
		}
		logger.debug(String.format(
				"Pushing the event to Practo is skipped as book is not enabled for doctorId: [%s], centerId: [%d]",
				doctorId, centerId));
		return false;
	}
	
	private static String getCenterIntegrationKey(int centerId) throws SQLException {
		BasicDynaBean centerIntegrationBean = new InstaIntegrationDao().getCenterIntegrationDetails(centerId,
				BookSDKUtil.PRACTO_BOOK_INTEGRATION);
		String centerIntegrationKey = (centerIntegrationBean != null)
				? (String) centerIntegrationBean.get("establishment_key") : null;
		if (centerIntegrationKey == null || centerIntegrationKey.trim().isEmpty()) {
			return null;
		}
		return centerIntegrationKey;
	}

	/**
	 * Notifies the Newly created / Updated appointment to Practo.
	 * 
	 * @param appointmentId
	 *            - The appointment ID which got created/updated
	 * @param isAppointmentCreated
	 *            - true : Newly created appointment , false: Updated
	 *            appointment
	 * @throws SQLException
	 */
	public static void addDoctorAppointmentsToPracto(int appointmentId, boolean isAppointmentCreated)
			throws SQLException {
		BasicDynaBean appointment = schedulerAppointmentsDAO.findByKey("appointment_id", appointmentId);
		addDoctorAppointmentsToPracto(appointment, isAppointmentCreated, null);
	}
	
	protected static void addDoctorAppointmentsToPracto(int appointmentId, boolean isAppointmentCreated, List<ResourceDTO> resourceDeleteList)
			throws SQLException {
		BasicDynaBean appointment = schedulerAppointmentsDAO.findByKey("appointment_id", appointmentId);
		addDoctorAppointmentsToPracto(appointment, isAppointmentCreated, resourceDeleteList);
	}


	protected static void addDoctorAppointmentsToPracto(BasicDynaBean appointment, boolean isAppointmentCreated, List<ResourceDTO> resourceDeleteList)
			throws SQLException {
		int appointmentId = (Integer) appointment.get("appointment_id");
		int centerId = (Integer) appointment.get("center_id");
		String establishmentKey = getCenterIntegrationKey(centerId);
		if (establishmentKey == null) {
			return;
		}
		Timestamp apptStartTime = (Timestamp) appointment.get("appointment_time");
		int apptDuration = (Integer) appointment.get("duration");
		String mrNo = (String) appointment.get("mr_no");
		String patientName = (String) appointment.get("patient_name");
		String phoneNo = (String) appointment.get("patient_contact");
		String apptStatus = (String) appointment.get("appointment_status");
		String emailId = null;
		BasicDynaBean centerPrefs = new CenterPreferencesDAO().getCenterPreferences(centerId);
		PatientDTO patient = null;
		boolean sharePatientDetails = (centerPrefs.get("share_pat_details_to_practo") != null)
				? (Boolean) centerPrefs.get("share_pat_details_to_practo") : false;
		if (sharePatientDetails) {
			// Share the Patient details to Practo
			if (mrNo != null && !mrNo.isEmpty()) {
				BasicDynaBean patDetails = new PatientDetailsDAO().findPatientByMrno(mrNo);
				emailId = (String) patDetails.get("email_id");
			}
			patient = new PatientDTO(patientName, emailId, phoneNo, mrNo);
		} else {
			patient = new PatientDTO();
		}

		if ((Integer) appointment.get("res_sch_id") == 1) {
			// Primary resource is Doctor
			String doctorId = (String) appointment.get("prim_res_id");
			String appointmentIdStr = String.valueOf(appointmentId);
			if (!isBookEnabled(doctorId, centerId)) {
				return;
			}
			addAppointmentToPracto(centerId, establishmentKey, appointmentIdStr, doctorId, apptStartTime, apptDuration,
					apptStatus, patient, isAppointmentCreated);

		} else {
			List<BasicDynaBean> apptItems = schedulerAppointmentsDAO.findAllByKey("appointment_id",
					appointmentId);
			// Secondary resources
			for (BasicDynaBean apptItem : apptItems) {
				if (((String) apptItem.get("resource_type")).contains("DOC")) {
					// secondary doctor
					String doctorId = (String) apptItem.get("resource_id");
					if (!isBookEnabled(doctorId, centerId)) {
						continue;
					}
					String appointmentIdStr = String.valueOf(appointmentId) + "-" + doctorId;
					addAppointmentToPracto(centerId,establishmentKey,appointmentIdStr,doctorId,apptStartTime,
							apptDuration,apptStatus,patient, isAppointmentCreated);

				}
			}

		}
		if (resourceDeleteList == null) {
			return;
		}
		for (ResourceDTO resourceDTO : resourceDeleteList) {
			if (resourceDTO.getResourceType().contains("DOC")) {
				String doctorId = resourceDTO.getResourceId();
				if (!isBookEnabled(doctorId, centerId)) {
					continue;
				}
				String appointmentIdStr = String.valueOf(appointmentId) + "-" + doctorId;
				
				addAppointmentToPracto(centerId,establishmentKey,appointmentIdStr,doctorId,apptStartTime,
						apptDuration,BookSDKUtil.APPOINTMENT_CANCELLED,patient, isAppointmentCreated);
				
			}

		}

	}
	
	private static void addAppointmentToPracto(int centerId, String establishmentKey, String appointmentId, String doctorId,
			Timestamp apptStartTime, int apptDuration, String apptStatus, PatientDTO patient,
			boolean isAppointmentCreated) {
		if (apptStatus.equalsIgnoreCase("Noshow")) {
			BookSDKUtil.notifyAppointmentVisitEventToPracto(establishmentKey, centerId, appointmentId,
					AppointmentVisitEvent.PATIENT_NO_SHOW);
			return;
		}
		if (apptStatus.equalsIgnoreCase("Arrived")) {
			BookSDKUtil.notifyAppointmentVisitEventToPracto(establishmentKey, centerId, appointmentId,
					AppointmentVisitEvent.PATIENT_CHECKIN);
			return;
		}
		if (apptStatus.equalsIgnoreCase("Cancel")) {
			apptStatus = BookSDKUtil.APPOINTMENT_CANCELLED;
		} else {
			apptStatus = BookSDKUtil.APPOINTMENT_CONFIRMED;
		}
		logger.info(String.format(
				"Pushing [%s] appointment to Practo. Details: [%n] centerId: [%d], appointmentId: [%s], doctorId:[%s], startTime: [%s], Duration: [%d], Appt status: [%s], Patient Details: [%s] ",
				isAppointmentCreated ? "Created" : "Updated", centerId, appointmentId, doctorId, apptStartTime,
				apptDuration, apptStatus, patient));
		if (isAppointmentCreated) {
			BookSDKUtil.addAppointment(establishmentKey, appointmentId, doctorId, apptStartTime, apptDuration,
					apptStatus, patient);
		} else {
			BookSDKUtil.updateAppointment(establishmentKey, appointmentId, apptStartTime, apptDuration, apptStatus,
					patient);
		}
	}
	
	public static void addUpdateOverridesToPracto(String doctorId, Date fromDate, Date toDate, Integer centerId)
			throws Exception {
		if (centerId == null) {
			CenterDAO doctorCenterDAO = new CenterDAO();
			List<BasicDynaBean> centersList = doctorCenterDAO.getCenters(doctorId);
			if (centersList != null) {
				for (BasicDynaBean center : centersList) {
					centerId = (Integer) center.get("center_id");
					String establishmentKey = getCenterIntegrationKey(centerId);
					if (establishmentKey == null || !isBookEnabled(doctorId, centerId)) {
						continue;
					}
					List<BasicDynaBean> overridesList = new ResourceDAO().getResourceAvailabilities("DOC", fromDate, doctorId,
							null , centerId);
					BookSDKUtil.overrideDoctorTimings(establishmentKey, centerId, doctorId, fromDate, toDate, overridesList);
				}
			}
		} else {
			String establishmentKey = getCenterIntegrationKey(centerId);
			if (establishmentKey == null || !isBookEnabled(doctorId, centerId)) {
				return;
			}
			List<BasicDynaBean> overridesList = new ResourceDAO().getResourceAvailabilities("DOC", fromDate, doctorId,
					null , centerId);
			BookSDKUtil.overrideDoctorTimings(establishmentKey, centerId, doctorId, fromDate, toDate, overridesList);
		}
	}
	
	public static void deleteOverridesOnPracto(String doctorId, Date date) throws Exception {
		CenterDAO doctorCenterDAO = new CenterDAO();
		List<BasicDynaBean> centersList = doctorCenterDAO.getCenters(doctorId);
		if (centersList != null && !centersList.isEmpty()) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int weekDayNo = cal.get(Calendar.DAY_OF_WEEK) - 1;
			int centersIncDefault = (Integer) GenericPreferencesDAO.getGenericPreferences()
					.getMax_centers_inc_default();
			for (BasicDynaBean center : centersList) {
				int centerId = (Integer) center.get("center_id");
				String establishmentKey = getCenterIntegrationKey(centerId);
				if (establishmentKey == null || !isBookEnabled(doctorId, centerId)) {
					continue;
				}
				ResourceDAO resourceDAO = new ResourceDAO();
				List<BasicDynaBean> resourceAvailabilities = resourceDAO
						.getResourceDefaultAvailabilities(doctorId, weekDayNo, "DOC", "A", centerId);
				if ((resourceAvailabilities == null || resourceAvailabilities.isEmpty())) {
					// Check if weekly availability is defined as 'not availble' for this doctor
					resourceAvailabilities = resourceDAO
							.getResourceDefaultAvailabilities(doctorId, weekDayNo, "DOC", "N", null);
					if (resourceAvailabilities == null || resourceAvailabilities.isEmpty() && centersIncDefault == 1) {
						// Weekly availability is not defined for this doctor, So
						// use the weekly availability of default Doctor consultation
						resourceAvailabilities = resourceDAO.getResourceDefaultAvailabilities("*", weekDayNo, "DOC", "A",
								centerId);
					}
				}
				BookSDKUtil.overrideDoctorTimings(establishmentKey, centerId, doctorId, date, date, resourceAvailabilities);

			}
		}
	}
	
}
