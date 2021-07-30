package com.insta.hms.Registration;


import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.orders.ConsultationTypesDAO;
import com.insta.hms.orders.OrderDAO;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.editvisitdetails.EditVisitDetailsDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.IPPreferences.IPPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDTO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.medicalrecorddepartment.MRDUpdateScreenBO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.resourcescheduler.ResourceDAO;
import com.insta.hms.util.hijricalender.UmmalquraCalendar;
import com.insta.hms.util.hijricalender.UmmalquraGregorianConverter;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RegistrationUtilsAction extends DispatchAction {

	static Logger log = LoggerFactory.getLogger(RegistrationUtilsAction.class);

	/*
     * Utility method to get the ward names with the selected bed type and also no. of free beds in the ward.
     */

	@IgnoreConfidentialFilters
    public ActionForward getWardnamesForBedType(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws IOException,Exception{

    	JSONSerializer js = new JSONSerializer();
		String bedType = request.getParameter("selectedbedtype");
		int centerId = (Integer)request.getSession().getAttribute("centerId");
		boolean multiCentered = GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1;

		List<BasicDynaBean> wardNameList = new BedMasterDAO().getWardsForBedType(bedType,centerId,multiCentered);

		response.setContentType("application/x-json");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(js.serialize(ConversionUtils.listBeanToListMap(wardNameList)));
		response.flushBuffer();
        return null;
    }

	@IgnoreConfidentialFilters
	 public ActionForward getBednamesForWard(ActionMapping mapping, ActionForm form,
				HttpServletRequest request, HttpServletResponse response)throws IOException,Exception{

		JSONSerializer js = new JSONSerializer();
		String ward = request.getParameter("selectedward");
		String bedType = request.getParameter("selectedbedtype");

		List<BasicDynaBean> bedNameList = BedMasterDAO.getBednamesForWard(ward, bedType);

		response.setContentType("application/x-json");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(js.serialize(ConversionUtils.listBeanToListMap(bedNameList)));
		response.flushBuffer();
        return null;
    }

	@IgnoreConfidentialFilters
	public ActionForward getBedTypesForWard(ActionMapping mapping, ActionForm form,
				HttpServletRequest request, HttpServletResponse response)throws IOException,Exception{

    	JSONSerializer js = new JSONSerializer();
		String ward = request.getParameter("selectedward");

		List<BasicDynaBean> bedTypesList = new BedMasterDAO().getBedTypesForWard( ward );

		response.setContentType("application/x-json");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(js.serialize(ConversionUtils.listBeanToListMap(bedTypesList)));
		response.flushBuffer();
        return null;
    }
	
	@IgnoreConfidentialFilters
	public ActionForward getBedChargesJson(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws IOException,Exception{

		JSONSerializer js = new JSONSerializer();
		
		String actionId = request.getParameter("screenid");
		String organization = request.getParameter("organization");
		String bedtype = request.getParameter("bedtype");
		
		List<BasicDynaBean> bedChargesList = new ArrayList<BasicDynaBean>();
		if (actionId.equals("ip_registration")) {
        	if ( new IPPreferencesDAO().getPreferences().get("allocate_bed_at_reg").equals("Y") ) {
        		bedChargesList = new BedMasterDAO().getAllBedDetails(organization , bedtype);
        	} else {
        		bedChargesList = new BedMasterDAO().getBillingBedDetails(organization , bedtype);
        	}
		}
		
		response.setContentType("application/json");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(js.serialize(ConversionUtils.listBeanToListMap(bedChargesList)));
		response.flushBuffer();
	    return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward getDeptUnit(ActionMapping mapping, ActionForm form, HttpServletRequest request,
    		HttpServletResponse response) throws IOException, ServletException, SQLException {

        JSONSerializer js = new JSONSerializer();
        String dept_id = request.getParameter("dept_id");
        response.setContentType("text/plain");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        String unit_id = DataBaseUtil.getStringValueFromDb("SELECT getUnit(?)", dept_id);
        response.getWriter().write(js.serialize(unit_id));
        response.flushBuffer();
    	return null;
    }

	@IgnoreConfidentialFilters
	public ActionForward ajaxCheckValidity(ActionMapping mapping, ActionForm form,HttpServletRequest request,
            HttpServletResponse response) throws SQLException,IOException,ParseException {

		JSONSerializer js = new JSONSerializer();
        String isValid = "false";
        String type = request.getParameter("type");
    	String ratePlanOrSponsor = request.getParameter("id");

    	Date currentDate = DateUtil.getCurrentDate();
    	if (type != null && !type.equals("")) {
    		if (type.equals("RatePlan")) {
    			isValid = EditVisitDetailsDAO.checkRatePlanValidity(ratePlanOrSponsor, currentDate);
    		}
    		if (type.equals("Sponsor")) {
    			isValid = EditVisitDetailsDAO.checkSponsorValidity(ratePlanOrSponsor, currentDate);
    		}
    	}
        response.setContentType("text/plain");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        response.getWriter().write(js.serialize(isValid));
        response.flushBuffer();
    	return null;
    }

	public ActionForward ajaxBillAndPaymentStatus(ActionMapping mapping, ActionForm form,HttpServletRequest request,
            HttpServletResponse response) throws SQLException,IOException,ParseException {

		JSONSerializer js = new JSONSerializer();
    	String visitId = request.getParameter("patient_id");
    	String visitType = request.getParameter("visit_type");

    	response.setContentType("application/x-json");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    	BasicDynaBean billAndPaymentDetails = EditVisitDetailsDAO.getBillAndPaymentStatus(visitId, visitType);

    	if (billAndPaymentDetails == null)
    		response.getWriter().write(js.serialize(null));
    	else {
    		response.getWriter().write(js.serialize(billAndPaymentDetails.getMap()));
    	}
        response.flushBuffer();
    	return null;

	}

	@IgnoreConfidentialFilters
	public ActionForward checkPatientDetailsExists(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws SQLException, ParseException, IOException {

    	 PatientDetailsDAO dao = new PatientDetailsDAO();
    	 BasicDynaBean patDetailsBean = dao.getBean();

	     patDetailsBean.set("patient_name", request.getParameter("firstName"));
	     patDetailsBean.set("middle_name", request.getParameter("middleName"));
	     patDetailsBean.set("last_name", request.getParameter("lastName"));
	     patDetailsBean.set("patient_gender", request.getParameter("gender"));
	     patDetailsBean.set("patient_care_oftext", request.getParameter("age"));
	     patDetailsBean.set("remarks", request.getParameter("dob"));
	     patDetailsBean.set("patient_phone", request.getParameter("phno"));
	     patDetailsBean.set("government_identifier", request.getParameter("govt_identifier"));

	     JSONSerializer js = new JSONSerializer().exclude("class");

 		 response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
 		 response.setContentType("application/x-json");
 		 List<BasicDynaBean> existingPatients = dao.checkDetailsExist(patDetailsBean);
 		 if (existingPatients != null && existingPatients.size() > 0)
 			 response.getWriter().write(js.serialize(existingPatients.get(0).getMap()));
 		 else
 			 response.getWriter().write(js.serialize(null));

 		 return null;
    }


	@IgnoreConfidentialFilters
    public ActionForward getPatientDetailsJSON(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse res)
        throws IOException, SQLException, ParseException, Exception {
        res.setContentType("application/x-json");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        String mrno = request.getParameter("mrno");
        String patient_id = request.getParameter("patient_id");
        String reg_screen_id = request.getParameter("reg_screen_id");

        Preferences pref = (Preferences)request.getSession(false).getAttribute("preferences");
		String modAdvancedPackagesActive = "Y";

		if ( (pref!=null) && (pref.getModulesActivatedMap() != null) ) {
			modAdvancedPackagesActive = (String) pref.getModulesActivatedMap().get("mod_adv_packages");
	        if (modAdvancedPackagesActive == null || modAdvancedPackagesActive.equals("")) {
	        	modAdvancedPackagesActive = "N";
	        }
	    }

        String last_visit_id = null;
        int loggedInCenterId = RequestContext.getCenterId();
        String lastVisitInThisCenter = "";
        if (patient_id == null || patient_id.trim().equals("")) {
        	if (reg_screen_id != null && reg_screen_id.equals("new_op_registration")) {
        		lastVisitInThisCenter = VisitDetailsDAO.getPatientLatestVisitId(mrno, null, "o", loggedInCenterId);
        		last_visit_id = VisitDetailsDAO.getPatientLatestVisitId(mrno, null, "o");
        		if (lastVisitInThisCenter == null || lastVisitInThisCenter.equals(""))
        			lastVisitInThisCenter = VisitDetailsDAO.getPatientLatestVisitId(mrno, null, "i", loggedInCenterId);
        		if (last_visit_id == null || last_visit_id.equals(""))
        			last_visit_id = VisitDetailsDAO.getPatientLatestVisitId(mrno, null, "i");
        	} else {
        		lastVisitInThisCenter = VisitDetailsDAO.getPatientLatestVisitId(mrno, null, null, loggedInCenterId);
        		last_visit_id = VisitDetailsDAO.getPatientLatestVisitId(mrno, null, null);
        	}
        } else {
        	lastVisitInThisCenter = patient_id;
        	last_visit_id = patient_id;
        }

        // Default allow multiple active visits is No.
        RegistrationPreferencesDTO regPrefs = RegistrationPreferencesDAO.getRegistrationPreferences();
        String allowMultipleActiveVisits = (regPrefs.getAllow_multiple_active_visits() != null
        										&& !regPrefs.getAllow_multiple_active_visits().equals(""))
        									? regPrefs.getAllow_multiple_active_visits() : "N";

        if (allowMultipleActiveVisits.equals("N")) {
	        List<BasicDynaBean> activeVisits = VisitDetailsDAO.getPatientVisits(mrno, true);
	        if (activeVisits != null && activeVisits.size() > 0) {
	        	for (BasicDynaBean patient : activeVisits) {
	        		last_visit_id = (String)patient.get("patient_id");
	        		lastVisitInThisCenter = (String) patient.get("patient_id");
	        		break;
				}
	        }
        }

		String consStatus = null;
		Date recentGenRegChargePostedDate = null;

		// Readonly Map.
		Map pd = null;

		if (last_visit_id != null) {
			pd = VisitDetailsDAO.getPatientVisitDetailsMap(last_visit_id);
		}else {
			pd = PatientDetailsDAO.getPatientGeneralDetailsMap(mrno);
		}
		
		BasicDynaBean familyBillTotal = null;
		OrderDAO oDao = new OrderDAO();
		List<BasicDynaBean> followUpDocVisits = null;
		List<BasicDynaBean> policyNos = null;
		List<BasicDynaBean> allPolicyNos =null;
		List<BasicDynaBean> corporateIds = null;
		List<BasicDynaBean> nationalIds = null;
		List<BasicDynaBean> previousDocVisits = null;
		List<BasicDynaBean> patientMultiVisitPacakgeDetails = null;
		List<BasicDynaBean> patientMultiVisitPacakgeConsumedDetails = null;
		List<BasicDynaBean> patientMultiVisitPacakgeComponentDetails = null;
		List<BasicDynaBean> multiVisitPackageIds = null;
		List<String> previousEpisodeVisitIds = new ArrayList<String>();

		List<BasicDynaBean> previousTestPrescriptions = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> previousServicePrescriptions = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> previousConsultationPrescriptions = new ArrayList<BasicDynaBean>();
		BasicDynaBean pVisitBean = null;
		VisitDetailsDAO visitDetDao = new VisitDetailsDAO();

		BasicDynaBean patientLastIpVisit = null;
		BasicDynaBean activeVisit = null;
        if (pd != null) {
        	
        	activeVisit = visitDetDao.getVisitInAnotherCenter((String) pd.get("mr_no"));
        	if (last_visit_id != null && !last_visit_id.equals("")) {


        		pVisitBean = visitDetDao.findByKey("patient_id", last_visit_id);

	        	if (pd.get("doctor")!= null && !pd.get("doctor").equals("")) {
	        		int consultationId = new DoctorConsultationDAO().getAdmittingDoctorConsultationId(last_visit_id);
	    			BasicDynaBean consultationBean = new DoctorConsultationDAO().findByKey("consultation_id", consultationId);
	    			if (consultationBean!= null && consultationBean.get("cancel_status")!=null)
	    				consStatus = consultationBean.get("cancel_status").toString();
	    		}

	        	BasicDynaBean regDates = VisitDetailsDAO.getPreviousRegDateChargeAccepted(mrno);
	        	recentGenRegChargePostedDate = (Date)regDates.get("visit_reg_date");

				// Patient doctor visits according to base doctor visits
				previousDocVisits = VisitDetailsDAO.getPatientPreviousVisits(mrno, (String)pd.get("doctor"));
	 			followUpDocVisits = DoctorConsultationDAO.getPatientFollowUpDoctorVisits(mrno);
	 			patientLastIpVisit = VisitDetailsDAO.getPatientLastIpVisitDischargeDoctor(mrno);
				policyNos = RegistrationBO.getPatientPlansDetails(mrno, last_visit_id);
				allPolicyNos= RegistrationBO.getAllPatientPlansDetailsUsingMRNO(mrno);
				corporateIds = RegistrationBO.getPatientCorporateDetails(mrno);
				nationalIds = RegistrationBO.getPatientNationalDetails(mrno);

				List<BasicDynaBean> episodePreviousVisitDetails = null;
				if (pVisitBean != null) {
					episodePreviousVisitDetails = VisitDetailsDAO.getPreviousEpisodeVisitDetails((String)pVisitBean.get("main_visit_id"));
				}

				if (episodePreviousVisitDetails != null && episodePreviousVisitDetails.size() > 0) {
					for (BasicDynaBean eb : episodePreviousVisitDetails) {
						previousEpisodeVisitIds.add((String)eb.get("patient_id"));
					}
				}

				if (previousEpisodeVisitIds != null && previousEpisodeVisitIds.size() > 0) {
					for (String visitId : previousEpisodeVisitIds) {
						List<BasicDynaBean> ltest = oDao.getPatientTestPrescriptions(visitId);

						if (ltest != null && ltest.size() > 0) {
							previousTestPrescriptions.addAll(ltest);
						}

						List<BasicDynaBean> lserv = oDao.getPatientServicePrescriptions(visitId);
						if (lserv != null && lserv.size() > 0) {
							previousServicePrescriptions.addAll(lserv);
						}

						List<BasicDynaBean> lcons = oDao.getPatientConsultationPrescriptions(visitId);
						if (lcons != null && lcons.size() > 0) {
							previousConsultationPrescriptions.addAll(lcons);
						}
					}
				}
			}

        	if (pd.get("family_id") != null && !pd.get("family_id").equals(""))
    			familyBillTotal = VisitDetailsDAO.getPatientFamilyBillsTotal((String)pd.get("family_id"));
        }
        JSONSerializer js = new JSONSerializer().exclude("class").exclude("dynaClass");
    // needed to block creating new IP visit if an active IP visit exists already.
		String activeIPVisitId = null;
		List columns = new ArrayList();
		Map identifiers = new HashMap();
		columns.add("patient_id");
		identifiers.put("visit_type", "i");
		identifiers.put("mr_no", mrno);
		identifiers.put("status", "A");
		List<BasicDynaBean> visits = new GenericDAO("patient_registration").listAll(columns, identifiers, null);
		for(BasicDynaBean basicDynaBean : visits) {
			if (basicDynaBean.get("patient_id") != null) {
				activeIPVisitId = (String) basicDynaBean.get("patient_id");
			}
		}
		HashMap patientInfo = new HashMap();
		patientInfo.put("pVisitBean", pVisitBean != null ? pVisitBean.getMap() : null);
		patientInfo.put("patient", pd);
		patientInfo.put("lastVisitId", last_visit_id);
		patientInfo.put("lastVisitIdInThisCenter", lastVisitInThisCenter);
		patientInfo.put("recentGenRegChargePostedDate", recentGenRegChargePostedDate);
		patientInfo.put("consultation_status", consStatus);
		patientInfo.put("previousDocVisits", ConversionUtils.listBeanToListMap(previousDocVisits));
		patientInfo.put("followUpDocVisits", ConversionUtils.listBeanToListMap(followUpDocVisits));
		patientInfo.put("policyNos", ConversionUtils.listBeanToListMap(policyNos));
		patientInfo.put("allPolicyNos", ConversionUtils.listBeanToListMap(allPolicyNos));
		patientInfo.put("corporateIds", ConversionUtils.listBeanToListMap(corporateIds));
		patientInfo.put("nationalIds", ConversionUtils.listBeanToListMap(nationalIds));
		patientInfo.put("contact_pref_lang_code", PatientDetailsDAO.getContactPreference(mrno));
		String communicationMode = PatientDetailsDAO.getCommunicationMode(mrno);
		patientInfo.put("send_sms" ,(communicationMode.equals("S") || communicationMode.equals("B")) ? "Y" :"N");
		patientInfo.put("send_email" ,(communicationMode.equals("E") || communicationMode.equals("B")) ? "Y" :"N");
		patientInfo.put("active_visit_in_another_center", activeVisit == null ? null : activeVisit.getMap());
	  patientInfo.put("activeIPVisitId", activeIPVisitId);

		if (patientLastIpVisit != null)
			patientInfo.put("patientLastIpVisit", patientLastIpVisit.getMap());
		else
			patientInfo.put("patientLastIpVisit", null);

		patientInfo.put("previousTestPrescriptions", ConversionUtils.copyListDynaBeansToMap(previousTestPrescriptions));
		patientInfo.put("previousServicePrescriptions", ConversionUtils.copyListDynaBeansToMap(previousServicePrescriptions));
		patientInfo.put("previousConsultationPrescriptions", ConversionUtils.copyListDynaBeansToMap(previousConsultationPrescriptions));
		List l = VisitDetailsDAO.getAllPrevVisitBillWiseDues(mrno);
		patientInfo.put("previousVisitDues", l.size() > 0 ? l : null);

		BigDecimal billsApprovalTotal = null;
		if (last_visit_id != null)
			billsApprovalTotal = BillDAO.getBillApprovalAmountsTotal(last_visit_id);

		patientInfo.put("billsApprovalTotal", billsApprovalTotal);
		patientInfo.put("patientFamilyBillsTotal",  familyBillTotal != null ?  familyBillTotal.getMap() : null);

		if(modAdvancedPackagesActive.equals("Y") && mrno != null && !mrno.isEmpty()) {
			multiVisitPackageIds = PackageDAO.getMultiVisitPackageIdS(mrno);
			if(multiVisitPackageIds != null && multiVisitPackageIds.size() > 0) {
				patientInfo.put("mvPackageIds", ConversionUtils.listBeanToListMap(multiVisitPackageIds));
				patientMultiVisitPacakgeComponentDetails = new ArrayList<BasicDynaBean>();
				patientMultiVisitPacakgeConsumedDetails = new ArrayList<BasicDynaBean>();
				for(int i=0;i<multiVisitPackageIds.size();i++) {
					BasicDynaBean packages = multiVisitPackageIds.get(i);
					Integer packageId = (Integer)packages.get("package_id");
					patientMultiVisitPacakgeComponentDetails.addAll(PackageDAO.getPackageComponents(packageId));
					patientMultiVisitPacakgeConsumedDetails.addAll(PackageDAO.getMultiVisitPackageComponentQuantityDetails(packageId, mrno));
				}
			} else {
				patientInfo.put("mvPackageIds", null);
			}
		}

		if(patientMultiVisitPacakgeComponentDetails != null && patientMultiVisitPacakgeComponentDetails.size() > 0) {
			patientInfo.put("patientMultiVisitPacakgeComponentDetails", ConversionUtils.listBeanToListMap(patientMultiVisitPacakgeComponentDetails));
			patientInfo.put("patientMultiVisitPacakgeConsumedDetails", ConversionUtils.listBeanToListMap(patientMultiVisitPacakgeConsumedDetails));
		} else {
			patientInfo.put("patientMultiVisitPacakgeComponentDetails", null);
			patientInfo.put("patientMultiVisitPacakgeConsumedDetails", null);
		}

		List<BasicDynaBean> apptList = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> patientAppointmentDetails = ResourceDAO.getPatientTodaysAppointments(mrno);
		if(patientAppointmentDetails != null && patientAppointmentDetails.size() > 0)
			apptList.addAll(patientAppointmentDetails);
		if (apptList != null && apptList.size() > 0) {
			patientInfo.put("patientTodaysAppointmnts", ConversionUtils.listBeanToListMap(apptList));
		}
		
		//get the patient deposit details
		BigDecimal depositAmount = RegistrationBO.getAvailableGeneralAndIpDeposit(mrno);
		patientInfo.put("patDepositAmount", depositAmount);
		
        res.getWriter().write(js.deepSerialize(patientInfo));
        res.flushBuffer();
        return null;
    }

    public ActionForward getPatientDoctorVisits(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws IOException,Exception{

		JSONSerializer js = new JSONSerializer();
		String mrNo = request.getParameter("mrNo");
		String doctor = request.getParameter("doctor");
		String opType = request.getParameter("opType");

		List<BasicDynaBean> previousDocVisits = null;

		if (opType != null && !opType.trim().equals(""))
			previousDocVisits = VisitDetailsDAO.getPatientPreviousVisits(mrNo, doctor);
		else
			previousDocVisits = VisitDetailsDAO.getPatientPreviousMainVisits(mrNo, doctor, opType);

		response.setContentType("application/x-json");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(js.serialize(ConversionUtils.listBeanToListMap(previousDocVisits)));
		response.flushBuffer();
	    return null;
	}


    @IgnoreConfidentialFilters
    public ActionForward getGregorianToHijri(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws IOException,Exception{


		JSONSerializer js = new JSONSerializer();
		String day = request.getParameter("dobDay");
		String month = request.getParameter("dobMonth");
		String year = request.getParameter("dobYear");

		Calendar cal = new GregorianCalendar(Integer.parseInt(year), Integer.parseInt(month)-1, Integer.parseInt(day));
		int[] hDateInfo = UmmalquraGregorianConverter.toHijri(cal.getTime());




		response.setContentType("application/x-json");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		Map result = new HashMap();
		result.put("day", hDateInfo[2]);
		result.put("month", hDateInfo[1]+1);
		result.put("year", hDateInfo[0]);
		response.getWriter().write(js.serialize(result));

		response.flushBuffer();
	    return null;
	}

    @IgnoreConfidentialFilters
    public ActionForward getHijriToGregorian(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws IOException,Exception{

    	JSONSerializer js = new JSONSerializer();
    	response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		String day = request.getParameter("dobDay");
		String month = request.getParameter("dobMonth");
		String year = request.getParameter("dobYear");

		Map result = new HashMap();
		try {
			UmmalquraCalendar cal = new UmmalquraCalendar(Integer.parseInt(year), Integer.parseInt(month)-1, Integer.parseInt(day));
			java.util.Date date = cal.getTime();

			response.setContentType("application/x-json");
			response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
			String dateStr = DateUtil.formatDate(date);
			String[] dateArr = dateStr.split("-");

			result.put("day", dateArr[0]);
			result.put("month", dateArr[1]);
			result.put("year", dateArr[2]);
			result.put("error", "");

		} catch (ArrayIndexOutOfBoundsException exception) {
			// The following Western-Islamic calendar converter is based on the Umm al-Qura calendar and is valid from 1356 AH
			// (14 March 1937 CE) to 1500 AH (16 November 2077 CE). Outside this interval, the converter will give erroneous results.
			result.put("error", "Date is out of range.");
		}
		response.getWriter().write(js.serialize(result));

		response.flushBuffer();
	    return null;
	}

    public ActionForward getVisitConsultations(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws IOException,Exception{

		JSONSerializer js = new JSONSerializer();
		String visitId = request.getParameter("visitId");

		List<BasicDynaBean> docConsultations = DoctorConsultationDAO.getVisitConsultations(visitId);

		response.setContentType("application/x-json");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(js.serialize(ConversionUtils.listBeanToListMap(docConsultations)));
		response.flushBuffer();
	    return null;
	}

    public ActionForward getPatientPreviousMainVisits(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws IOException,Exception{

		JSONSerializer js = new JSONSerializer();
		String mrNo = request.getParameter("mrNo");
		String doctor = request.getParameter("doctor");
		List<BasicDynaBean> previousMainVisits = VisitDetailsDAO.getPatientPreviousMainVisits(mrNo, doctor);

		response.setContentType("application/x-json");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(js.serialize(ConversionUtils.listBeanToListMap(previousMainVisits)));
		response.flushBuffer();
	    return null;
	}

    public ActionForward photoSize(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse res)
        throws IOException, SQLException, ParseException {

        res.setContentType("application/x-json");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

        String mrno = request.getParameter("mrno");
        PatientDetailsDAO pdao = new PatientDetailsDAO();
        int photosize = pdao.getPhotoSize(mrno);

        JSONSerializer js = new JSONSerializer().exclude("class");
        res.getWriter().write(js.serialize(photosize));
        res.flushBuffer();

        return null;
    }
   
    
    //returns the image in base64 string
    public ActionForward photo(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse res)
        throws IOException, SQLException, ParseException {

        res.setContentType("application/x-json");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

        String mrno = request.getParameter("mrno");
        String photo = "";
        if(PatientDetailsDAO.getPatientPhoto(mrno) != null) {
	        photo = new String(Base64.encodeBase64(DataBaseUtil.readInputStream(PatientDetailsDAO.getPatientPhoto(mrno)))) ;
        }
	    JSONSerializer js = new JSONSerializer().exclude("class");
	    res.getWriter().write(js.serialize(photo));
	    res.flushBuffer();

        return null;
    }
    

    public ActionForward checkForDuplicateMemberId(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException, ParseException {

	    String memberId = req.getParameter("member_id");
	    String mrNo = req.getParameter("mr_no");
	    Integer planId = Integer.parseInt(req.getParameter("planId"));
	    String responseText = "false";

	    if(memberId!= null)  memberId.trim();

	    boolean exists = VisitDetailsDAO.checkFOrDUplicateMemberId(mrNo, memberId, planId );
	    if (exists)
	    	responseText = "true";

	    JSONSerializer js = new JSONSerializer();
	    res.setContentType("text/plain");
	    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
	    res.getWriter().write(js.serialize(responseText));
	    res.flushBuffer();

	    return null;
	}


    public ActionForward checkForDuplicateCorporateMemberId(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException, ParseException {

	    String memberId = req.getParameter("member_id");
	    String mrNo = req.getParameter("mr_no");
	    String sponsorId = req.getParameter("sponsor_id");
	    String responseText = "false";

	    if(memberId!= null)  memberId.trim();

	    boolean exists = VisitDetailsDAO.checkFOrDUplicateCorpMemberId(mrNo, memberId, sponsorId);
	    if (exists)
	    	responseText = "true";

	    JSONSerializer js = new JSONSerializer();
	    res.setContentType("text/plain");
	    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
	    res.getWriter().write(js.serialize(responseText));
	    res.flushBuffer();

	    return null;
	}

    public ActionForward checkForDuplicateNationalMemberId(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException, ParseException {

	    String memberId = req.getParameter("member_id");
	    String mrNo = req.getParameter("mr_no");
	    String sponsorId = req.getParameter("sponsor_id");
	    String responseText = "false";

	    if(memberId!= null)  memberId.trim();

	    boolean exists = VisitDetailsDAO.checkFOrDUplicateNationalMemberId(mrNo, memberId, sponsorId);
	    if (exists)
	    	responseText = "true";

	    JSONSerializer js = new JSONSerializer();
	    res.setContentType("text/plain");
	    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
	    res.getWriter().write(js.serialize(responseText));
	    res.flushBuffer();

	    return null;
	}

    @IgnoreConfidentialFilters
    public ActionForward getDoctorCharge(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws IOException,SQLException {

    	JSONSerializer js = new JSONSerializer().exclude("class");

		String doctor = request.getParameter("doctor");
		String orgid = request.getParameter("orgid");
		BasicDynaBean doctorchargebean = DoctorMasterDAO.getDoctorOPChargeDetails(doctor, orgid);
		response.setContentType("application/x-json");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		if(doctorchargebean != null) response.getWriter().write(js.serialize(doctorchargebean.getMap()));
		else response.getWriter().write(js.serialize(doctorchargebean));
		response.flushBuffer();
        return null;
    }

    @IgnoreConfidentialFilters
    public ActionForward checkOldMrno(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException, ParseException {

	    String oldno = req.getParameter("oldno");
	    String responseText = "false";

	    PatientDetailsDAO dao = new PatientDetailsDAO();
	    boolean exists = dao.exist("oldmrno", oldno);
	    if (exists)
	    	responseText = "true";

	    JSONSerializer js = new JSONSerializer();
	    res.setContentType("text/plain");
	    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
	    res.getWriter().write(js.serialize(responseText));
	    res.flushBuffer();

	    return null;
	}

    @IgnoreConfidentialFilters
	public ActionForward checkCaseFileNo(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException, ParseException {

	    String casefileno = req.getParameter("casefileno");
	    String responseText = "false";

	    PatientDetailsDAO dao = new PatientDetailsDAO();
	    boolean exists = dao.exist("casefile_no", casefileno);
	    if (exists)
	    	responseText = "true";

	    JSONSerializer js = new JSONSerializer();
	    res.setContentType("text/plain");
	    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
	    res.getWriter().write(js.serialize(responseText));
	    res.flushBuffer();

	    return null;
	}
    
    @IgnoreConfidentialFilters
	public ActionForward getConsultationTypesForRateplan(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws IOException, SQLException{
		String orgId = request.getParameter("orgId");

		Integer centerId = RequestContext.getCenterId();
		String healthAuthorityForCenter = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
		String healthAuthority = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthorityForCenter).getHealth_authority();
		JSONSerializer js = new JSONSerializer();
		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(js.serialize(ConversionUtils.listBeanToListMap(
				ConsultationTypesDAO.getConsultationTypes("o", orgId,healthAuthority))));
		response.flushBuffer();

	    return null;

	}

    @IgnoreConfidentialFilters
	 public ActionForward getICUStatus(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws IOException,Exception{

		String bedType = request.getParameter("bed_type");

		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write( new JSONSerializer().exclude("class").serialize(
				BedMasterDAO.isIcuBedType(bedType)?"Y":"N"));
		response.flushBuffer();
        return null;
    }

    @IgnoreConfidentialFilters
	 public ActionForward getDiagnosisCodes(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response) throws ServletException, IOException, SQLException {
		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		JSONSerializer js = new JSONSerializer().exclude("class");
		String query = request.getParameter("query");
		String defaultDiagnosisCodeType = request.getParameter("defaultDiagnosisCodeType");
		List l = new MRDUpdateScreenBO().getCodesListOfCodeType(query, defaultDiagnosisCodeType);

		HashMap retVal = new HashMap();
		retVal.put("result", ConversionUtils.copyListDynaBeansToMap(l));

		response.getWriter().write(js.deepSerialize(retVal));
		response.flushBuffer();


		return null;
	}

    @IgnoreConfidentialFilters
	public ActionForward getDiagCodeFavourites(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		JSONSerializer js = new JSONSerializer().exclude("class");
		String query = request.getParameter("query");
		String defaultDiagnosisCodeType = request.getParameter("defaultDiagnosisCodeType");
		String doctorId = request.getParameter("doctorId");
		List l = new MRDUpdateScreenBO().getDiagCodeFavourites(query, doctorId, defaultDiagnosisCodeType);

		HashMap retVal = new HashMap();
		retVal.put("result", ConversionUtils.copyListDynaBeansToMap(l));

		response.getWriter().write(js.deepSerialize(retVal));
		response.flushBuffer();

		return null;
	}

	public ActionForward getPendingConsultations(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response) throws ServletException, IOException, SQLException,Exception {

		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		JSONSerializer js = new JSONSerializer().exclude("class");
		String patientId = request.getParameter("patientId");
		response.getWriter().write(js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
			 new OrderDAO().getPatientConsultationPrescriptions(patientId))));
		response.flushBuffer();
		return null;
	}

	@IgnoreConfidentialFilters
	 public ActionForward isRatePlanApplicable(ActionMapping mapping, ActionForm form,
		 	HttpServletRequest request, HttpServletResponse res) throws Exception {
		 String category = request.getParameter("category");
		 String orgId = request.getParameter("orgId");
		 String scheduleId = request.getParameter("schedule_id");
		 String responseContent = "";
		 if (scheduleId != null && !scheduleId.equals(""))
			 responseContent = ResourceDAO.isRatePlanApplicable(category, orgId, scheduleId);
		 res.setContentType("application/json");
		 res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		 res.getWriter().write(responseContent);
		 res.flushBuffer();
		 return null;
	 }

	 public ActionForward getPreviousDiagnosisDetails(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
			 ServletException {
		 JSONSerializer js = new JSONSerializer().exclude("class");
		 String mrNo = request.getParameter("mr_no");
		 mrNo = mrNo == null ? "" : mrNo;
		 String patientId = request.getParameter("patient_id");
		 patientId = patientId == null ? "" : patientId;

		 String pageNumStr = request.getParameter("pageNum");
		 int pageNum = 1;
		 if (pageNumStr != null && !pageNumStr.equals(""))
			 pageNum = Integer.parseInt(pageNumStr);

		 PagedList pagedList = null;
		 if (!mrNo.equals("") && !patientId.equals("")) {
			 pagedList = MRDDiagnosisDAO.getPreviousDiagnosisDetails(mrNo, patientId, pageNum);
		 }

		 response.setContentType("application/json");
		 response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		 js.deepSerialize(pagedList, response.getWriter());
		 return null;
	 }

	 public ActionForward checkForPatientMemberId(ActionMapping m, ActionForm f,
				HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException, ParseException {

	    String memberId = req.getParameter("member_id");
	    String companyId = req.getParameter("companyId");
	    String mrNo = req.getParameter("mrno");
	    String responseText = "false";

	    boolean exists = new PatientInsurancePlanDAO().checkDupliMemberID(memberId, companyId, mrNo);
	    if (exists)
	    	responseText = "true";

	    JSONSerializer js = new JSONSerializer();
	    res.setContentType("text/plain");
	    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
	    res.getWriter().write(js.serialize(responseText));
	    res.flushBuffer();

	    return null;
	}

    public ActionForward getBabyDOBAndMemberIdValidityDetails(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException, ParseException {

    	VisitDetailsDAO vdao = new VisitDetailsDAO();
	    String mrNo = req.getParameter("mr_no");
	    String visitId = req.getParameter("visit_id");
	    String sponsorType = req.getParameter("sponsor_type");
	    String parentId = null;
	    BasicDynaBean parentDetails = null;
	    String parentMrNo = null;
	    HashMap babyInfo = null;

	    List<String> columns;
	    Map<String, Object> identifiers;
	    BasicDynaBean govtIdentifierBean;
	    String memberId = null;

	    int centerId = 0;
	    BasicDynaBean babyVisitInfo = vdao.findByKey("patient_id",visitId);
	    BasicDynaBean babyDetails = PatientDetailsDAO.getBabyDateOfBirtsAndSalutationDetails(mrNo);
	    if(babyDetails != null)
	    	parentId = (String)babyDetails.get("parent_id");
	    if(babyVisitInfo != null) {
	    	centerId = (Integer)babyVisitInfo.get("center_id");
	    	parentDetails = vdao.findByKey("patient_id", parentId);
	    	if(parentDetails != null)
	    		parentMrNo = (String)parentDetails.get("mr_no");
	    }

	    if (null != parentMrNo && !parentMrNo.equals("")) {
	    	babyInfo = new HashMap();
		    BasicDynaBean healthAuthPrefs = HealthAuthorityPreferencesDAO.getAllHealthAuthorityPrefs(CenterMasterDAO.getHealthAuthorityForCenter(centerId));
		    if (sponsorType != null)
		    	memberId = VisitDetailsDAO.getPatientMemberId(parentMrNo, sponsorType);

		    columns = new ArrayList<String>();
		    identifiers = new HashMap<String, Object>();
		    columns.add("government_identifier");
		    identifiers.put("mr_no", parentMrNo);
		    govtIdentifierBean = new GenericDAO("patient_details").findByKey(columns, identifiers);

		    babyInfo.put("babyDetails", babyDetails.getMap());
		    babyInfo.put("babyVisitInfo",babyVisitInfo.getMap());
		    babyInfo.put("helathAuthPrefs", healthAuthPrefs.getMap());
		    babyInfo.put("member_id", memberId);
		    babyInfo.put("govtIdentifier", govtIdentifierBean != null ? govtIdentifierBean.get("government_identifier") : "");
		}

	    JSONSerializer js = new JSONSerializer();
	    res.setContentType("text/plain");
	    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
	    res.getWriter().write(js.deepSerialize(babyInfo));
	    res.flushBuffer();

	    return null;
	}

    @IgnoreConfidentialFilters
    public ActionForward isUniqueGovtID(ActionMapping mapping, ActionForm form, HttpServletRequest request,
    		HttpServletResponse response)throws SQLException, IOException {

    	String govtIdentifier = request.getParameter("govt_identifier");
    	String mrNo = request.getParameter("mr_no");
    	boolean isGovtIDexists = PatientDetailsDAO.isUniqueGovtID(govtIdentifier, mrNo);
    	JSONSerializer js = new JSONSerializer().exclude("class");

    	response.setContentType("text/plain");
    	response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    	response.getWriter().write(js.serialize(isGovtIDexists));
    	response.flushBuffer();

    	return null;
    }

}
