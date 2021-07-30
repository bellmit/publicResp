package com.insta.hms.Registration;

import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.bob.hms.diag.ohsampleregistration.OhSampleRegistrationDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.RandomGeneration;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.integration.hl7.message.v23.ADTService;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CenterPreferences.CenterPreferencesDAO;
import com.insta.hms.master.GovtIdentifierMaster.GovtIdentifierMasterDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.OtherIdentifierMaster.OtherIdentificationDocumentTypesDAO;
import com.insta.hms.master.PatientCategory.PatientCategoryDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.SalutationMaster.SalutationMasterDAO;
import com.insta.hms.mdm.confidentialitygrpmaster.ConfidentialityGroupService;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.messaging.MessageUtil;
import com.insta.hms.usermanager.Role;
import com.insta.hms.util.hijricalender.UmmalquraCalendar;
import com.insta.hms.util.hijricalender.UmmalquraGregorianConverter;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.upload.FormFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class GeneralRegistrationAction extends DispatchAction {
  private static GenericDAO patientDetailsDao = new GenericDAO("patient_details");
  private static GenericDAO hospitalCanterMasterDao = new GenericDAO("hospital_center_master");
  private static GenericDAO mrdCaseFileAttributesDao = new GenericDAO("mrd_casefile_attributes");
  private static GenericDAO countryMasterDao = new GenericDAO ("country_master");
  private static GenericDAO stateMasterDao = new GenericDAO("state_master");
  private static GenericDAO cityDao = new GenericDAO("city");
  private static GenericDAO contactPreferenceDao = new GenericDAO("contact_preferences");
  private static CenterMasterDAO centerMasterDAO = new CenterMasterDAO();
  private static OtherIdentificationDocumentTypesDAO otherIdDocTypesDAO = new OtherIdentificationDocumentTypesDAO();
  private static ConfidentialityGroupService confidentialityService = ApplicationContextProvider.getBean(ConfidentialityGroupService.class);
  private static Logger log = LoggerFactory.getLogger(GeneralRegistrationAction.class);
  private static InterfaceEventMappingService interfaceEventMappingService =
	      ApplicationContextProvider.getBean(InterfaceEventMappingService.class);
  
    @IgnoreConfidentialFilters
	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		String mrno = req.getParameter("mrno");

		Map actionRightsMap = (Map)(req.getSession().getAttribute("actionRightsMap"));
		String allowNewReg = (String)actionRightsMap.get("allow_new_registration");
		int roleId = (Integer)req.getSession().getAttribute("roleId");
		
		String allowMarkDuplicate = (roleId != 1 && roleId != 2) ? (String)actionRightsMap.get("mark_patient_duplicate") : "A";

		if ((mrno == null || mrno.equals("")) && (allowNewReg != null && !allowNewReg.equals("A")) && roleId != 1 && roleId != 2) {
    		String error = "You are not authorized to register new patients.";
    		req.setAttribute("error", error);
			return m.findForward("reportErrors");
    	}

		req.setAttribute("allowMarkDuplicate", allowMarkDuplicate);
		req.setAttribute("visitId", req.getParameter("visitId"));
		req.setAttribute("preferredLanguages", RegistrationPreferencesDAO.getPreferredLanguages(
		    (String) req.getAttribute("language")));
		req.setAttribute("currentLanguagePreference", PatientDetailsDAO.getContactPreference(mrno));
		String commMode = PatientDetailsDAO.getCommunicationMode(mrno);
		req.setAttribute("communicationMode", commMode);
		if (mrno != null && !mrno.equals("")) {

			BasicDynaBean patientbean = PatientDetailsDAO.getPatientGeneralDetailsBean(mrno);
			if (patientbean != null) {
				req.setAttribute("mrChecked", "checked");
				Date d = (Date) patientbean.get("dateofbirth");
				if (d != null) {
					req.setAttribute("dateofbirth", d.getTime());
				}
				Date expectedDate = (Date) patientbean.get("expected_dob");
				if (expectedDate != null) {
					Map map = DateUtil.getAgeForDate(expectedDate.toString(), "yyyy-MM-dd");
					Object age = (map.get("age") != null) ? map.get("age").toString() : "";
					req.setAttribute("pat_age", age);
					Object ageIn = (map.get("ageIn") != null) ? map.get("ageIn").toString() : "";
					req.setAttribute("patient_agein", ageIn);
				}

				Boolean portalAccess = (Boolean)patientbean.get("portal_access");
				if ((new Boolean(true)).equals(portalAccess)) {
					req.setAttribute("portalPatAccess", "Y");
				} else {
					req.setAttribute("portalPatAccess", "N");
				}

				Boolean mobileAccess = (Boolean)patientbean.get("mobile_access");
				if ((new Boolean(true)).equals(mobileAccess)) {
					req.setAttribute("mobilePatAccess", "Y");
				} else {
					req.setAttribute("mobilePatAccess", "N");
				}

				req.setAttribute("patient", patientbean.getMap());

				if (patientbean.get("visit_id")!=null)
					req.setAttribute("patId", patientbean.get("visit_id").toString());
			} else {
				req.setAttribute("error", "Failed to find patient details for " + mrno);
				req.setAttribute("newChecked", "checked");
			}
		}
		else {
			req.setAttribute("newChecked", "checked");
			req.setAttribute("portalPatAccess", "N");
			req.setAttribute("mobilePatAccess", "N");
			HttpSession session = (HttpSession)req.getSession(false);
			Preferences pref = (Preferences)session.getAttribute("preferences");
	    if ( (pref!=null) && (pref.getModulesActivatedMap() != null) ) {
	      String modMobile = (String) pref.getModulesActivatedMap().get("mod_mobile");
	          if (modMobile != null && modMobile.equals("Y")) {
	            req.setAttribute("mobilePatAccess", "Y");
	          }
	      }
		}

		setAttributes(req);
		req.setAttribute("countryList", PhoneNumberUtil.getAllCountries());
		String countryCode = centerMasterDAO.getCountryCode(RequestContext.getCenterId());
		if(StringUtil.isNullOrEmpty(countryCode)){
		  countryCode = centerMasterDAO.getCountryCode(0);
		}

    req.setAttribute("patientConfidentialityCategories", new JSONSerializer().serialize(
        ConversionUtils.listBeanToListMap(
            confidentialityService.getUserConfidentialityGroups((String) req.getSession().getAttribute("userid")))));
		req.setAttribute("defaultCountryCode", countryCode);
		return m.findForward("show");
	}

	 @IgnoreConfidentialFilters
	public ActionForward forward(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		return m.findForward("regsuccess");
	}

	public ActionForward insertOrUpdate(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		ActionRedirect forward = new ActionRedirect(m.findForward("getregsuccess"));
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		String mrNo=null;
		String mr_no = req.getParameter("mr_no");
		String originalMrNo = req.getParameter("original_mr_no");
		boolean newPatient = StringUtil.isNullOrEmpty(mr_no);
		boolean duplicatePatient = (originalMrNo != null);

		HttpSession session = (HttpSession)req.getSession(false);
		String userName = (String)(session.getAttribute("userid"));
		GeneralRegistrationForm genForm = (GeneralRegistrationForm)f;
		PatientDetailsDAO dao = new PatientDetailsDAO();
		GenericDAO pddao = patientDetailsDao;
		BasicDynaBean patDetailsBean = pddao.getBean();
		pddao.loadByteaRecords(patDetailsBean, "mr_no", mr_no);
		int defaultCategory = 1;
		int existingAge = 0;
		String existingAgeIn = null;
		BasicDynaBean bean = dao.getBean();
		Preferences pref = (Preferences)session.getAttribute("preferences");
		String modMobile= "Y";
		if ( (pref!=null) && (pref.getModulesActivatedMap() != null) ) {
			modMobile = (String) pref.getModulesActivatedMap().get("mod_mobile");
	        if (modMobile == null || modMobile.equals("")) {
	        	modMobile = "N";
	        }
	    }

//		 when reg_name contains four parts we  r appending the 3rd part to 2nd part and saving in DB
    	String middle_name = null;
    	String middle_name1 = getValue("middle_name", params);
    	String middle_name2 = getValue("middle_name2", params);
    	if (middle_name2 != null && !middle_name2.equals("..MiddleName2..") && !middle_name2.equals(""))
    		middle_name = middle_name1+" "+middle_name2;
    	else
    		middle_name = middle_name1;
    	Object[] middleName_obj = new Object[1];
    	middleName_obj[0] = middle_name;
    	params.put("middle_name", middleName_obj);

		ConversionUtils.copyToDynaBean(params, bean, errors, true);
		String firstName = req.getParameter("patient_name");
		String middleName;
		if (middle_name2 != null && !middle_name2.equals("..MiddleName2..") && !middle_name2.equals(""))
			middleName = req.getParameter("middle_name")+" "+ ( req.getParameter("middle_name2") == null ? "" : req.getParameter("middle_name2") );
		else
			middleName = req.getParameter("middle_name");
		String lastName = req.getParameter("last_name");
		String contactPrefLangCode = getValue("preferredLanguage", params);
		String emailCommPref = getValue("modeOfCommEmail", params);
		String smsCommPref = getValue("modeOfCommSms", params);
		String communication = "B";
		if(emailCommPref.equals("on") || smsCommPref.equals("on")) {
			if(!emailCommPref.equals("on")) {
				communication = "S";
			}
			if(!smsCommPref.equals("on")) {
				communication = "E";
			}
		} else {
			communication = "N";
		}
		// patient name is set explicitly just to insert empty string when it is not entered.
		// above copytoDynaBean nullifys all the empty strings. we dont want to nullify the patient name fields.
		bean.set("patient_name", firstName);
		bean.set("middle_name", middleName);
		bean.set("last_name", lastName);

		bean.set("user_name", userName);
		bean.set("original_mr_no", originalMrNo);

		String patientCareOfText = String.valueOf(bean.get("patient_care_oftext"));
		String patientContact = (String) bean.get("patient_phone");
		String defaultCode = centerMasterDAO.getCountryCode(RequestContext.getCenterId());

		List<String> splitCountryCodeAndText = PhoneNumberUtil.getCountryCodeAndNationalPart(
				patientContact, null);
		if (splitCountryCodeAndText != null && !splitCountryCodeAndText.isEmpty()
				&& !splitCountryCodeAndText.get(0).isEmpty()) {
			bean.set("patient_phone_country_code", "+"+splitCountryCodeAndText.get(0));
			bean.set("patient_phone", "+" + splitCountryCodeAndText.get(0)
					+ splitCountryCodeAndText.get(1));
		} else if (defaultCode != null) {
			bean.set("patient_phone_country_code", "+" + defaultCode);
			if (patientContact != null && !patientContact.equals("") && !patientContact.startsWith("+")) {
				bean.set("patient_phone", "+" + defaultCode + patientContact);
			}
		}
		splitCountryCodeAndText = PhoneNumberUtil.getCountryCodeAndNationalPart(
				patientCareOfText, null);
		if (splitCountryCodeAndText != null && !splitCountryCodeAndText.isEmpty()
				&& !splitCountryCodeAndText.get(0).isEmpty()) {
			bean.set("patient_care_oftext_country_code", "+" + splitCountryCodeAndText.get(0));
			bean.set("patient_care_oftext", "+" + splitCountryCodeAndText.get(0)
					+ splitCountryCodeAndText.get(1));
		} else if (defaultCode != null) {
			bean.set("patient_care_oftext_country_code", "+" + defaultCode);
			if (patientCareOfText != null && !patientCareOfText.equals("") && !patientCareOfText.startsWith("+")) {
				bean.set("patient_care_oftext", "+" + defaultCode + patientCareOfText);
			}
		}

		if (genForm.getPatientPhoto()!=null && genForm.getPatientPhoto().getFileSize() > 0) {
			FormFile ff = genForm.getPatientPhoto();
			bean.set("patient_photo", ff.getInputStream());
		}

		bean.set("patient_state",req.getParameter("patient_state"));
		bean.set("patient_city",req.getParameter("patient_city"));

		if(bean.get("patient_address") == null) {
			bean.set("patient_address", "");
		}

		if ("".equals(req.getParameter("patient_area"))) {
			bean.set("patient_area", null);
		}
		
		if(!StringUtil.isNullOrEmpty(mr_no)){
			PatientDetailsDAO.updateContactPreference(mr_no,contactPrefLangCode, communication);			
		}
		String patAccess = req.getParameter("portalPatAccess");
		bean.set("portal_access", ("Y".equals(patAccess)));
		String mobilePatAccess = req.getParameter("mobilePatAccess");
		bean.set("mobile_access", ("Y".equals(mobilePatAccess)));
		if((Boolean)bean.get("mobile_access") && patDetailsBean != null
				&& (null == patDetailsBean.get("mobile_password") || patDetailsBean.get("mobile_password").equals(""))) {
			bean.set("mobile_password", RandomGeneration.randomGeneratedPassword(6));
		}
		try {
			if (!mr_no.equals("") && // registration completed, send SMS to admiitting and referal doctors
				   MessageUtil.allowMessageNotification(req,"scheduler_message_send")) { // Message notification allowed
				log.debug("Admission successful : Sending SMS to admitting doctor and referal doctor");
				  MessageManager mgr = new MessageManager();
	              Map patientData = new HashMap();
	              patientData.put("receipient_id__", bean.get("mr_no"));
	              patientData.put("mr_no", bean.get("mr_no"));
	              patientData.put("patient_phone", bean.get("patient_phone") != null ?  bean.get("patient_phone").toString().trim() : null);
	              patientData.put("receipient_type__", "PATIENT");
	              patientData.put("patient_name", bean.get("patient_name"));
	              patientData.put("recipient_mobile",bean.get("patient_phone") != null ?  bean.get("patient_phone").toString().trim() : null);
	              patientData.put("recipient_email", bean.get("email_id"));
	              patientData.put("lang_code", PatientDetailsDAO.getContactPreference((String)bean.get("mr_no")));
	              mgr.processEvent("edit_patient_appointment_reminder", patientData);
			}
    	} catch (Exception e) {
    		log.error("Error while trying to send SMS : " + e.getMessage());
    		// let the other things continue normally
    	}

		if(req.getParameter("category_expiry_date")!=null){
			bean.set("category_expiry_date", DataBaseUtil.parseDate(((Object[]) params.get("category_expiry_date"))[0].toString()));
		}
		if (mr_no != null && !mr_no.equals("")) {
			Boolean mrnoexists = patientDetailsDao.exist("mr_no", mr_no, false);
			if (mrnoexists) {
				BasicDynaBean patientbean = PatientDetailsDAO.getPatientGeneralDetailsBean(mr_no);
				defaultCategory = (Integer)(patientbean.get("patient_category_id"));
				existingAge = (patientbean.get("age") != null) ? (Integer)patientbean.get("age") : existingAge;
	    		existingAgeIn = (patientbean.get("agein") != null) ? (String)patientbean.get("agein") : existingAgeIn;
			}
		}

		String dateOfBirth = req.getParameter("dateOfBirth");
		java.sql.Date expectedDob = null;

		String ageIn = req.getParameter("ageIn");
		if (dateOfBirth.trim().equals("") && !ageIn.equals("D")) {
			int age = Integer.parseInt(req.getParameter("age"));
			java.util.Date date = (java.util.Date)DateUtil.getExpectedDate(age, ageIn, true, true);
			expectedDob = new java.sql.Date(date.getTime());

			bean.set("dateofbirth", null);
			if (existingAge != age || !existingAgeIn.equals(ageIn))
				bean.set("expected_dob", expectedDob);

		} else {
			java.sql.Date dob;
			if (dateOfBirth.trim().equals("")) {
				int age = Integer.parseInt(req.getParameter("age"));
				java.util.Date date = (java.util.Date)DateUtil.getExpectedDate(age, ageIn, true, true);
				dob = new java.sql.Date(date.getTime());
			} else {
				dob = Date.valueOf(dateOfBirth);
			}
			bean.set("dateofbirth", dob);
			bean.set("expected_dob", null);
		}

		if (bean.get("dateofbirth") == null && bean.get("expected_dob") == null)
			bean.set("expected_dob", expectedDob);

		if (req.getParameter("patient_category_id")!=null){
			if (req.getParameter("patient_category_id").isEmpty()) {
				bean.set("patient_category_id", defaultCategory);
			} else {
				int patientCategoryId = Integer.parseInt(req.getParameter("patient_category_id"));
				bean.set("patient_category_id", patientCategoryId);
			}
		}else{
			bean.set("patient_category_id", defaultCategory);
		}

		/* Bug 20326, 20398 */
    	String caseFileRequired = "N";
    	String caseFileNo = req.getParameter("casefileNo");

    	BasicDynaBean categoryDetBean = new PatientCategoryDAO().findByKey("category_id", (Integer)bean.get("patient_category_id"));
    	if (categoryDetBean != null && categoryDetBean.get("case_file_required") != null && !categoryDetBean.get("case_file_required").equals(""))
    		caseFileRequired = categoryDetBean.get("case_file_required").toString();

		if (caseFileRequired.equals("Y")) {
			if (req.getParameter("oldRegAutoGenerate")!=null && req.getParameter("oldRegAutoGenerate").equals("Y")) {
				caseFileNo = DataBaseUtil.getNextPatternId("CASENO");
				bean.set("casefile_no", caseFileNo);
			}else if (caseFileNo != null && !caseFileNo.equals("")) {
				bean.set("casefile_no", caseFileNo);
			}
		}

		if(req.getParameter("oldmrno")!=null && !req.getParameter("oldmrno").equals("")) {
			bean.set("oldmrno", req.getParameter("oldmrno"));
		}

		RegistrationBO registerbo = new RegistrationBO();
		String password = RandomGeneration.randomGeneratedPassword(6);

		if (!"".equals(mr_no)) {
			redirect.addParameter("mrno", mr_no);
			Connection con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			try {
				FlashScope flash = FlashScope.getScope(req);
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				if (errors.isEmpty()) {
					boolean success = registerbo.updatePatient(con, bean, password);

					if (success) {
						if((req.getParameter("oldRegAutoGenerate")!=null && req.getParameter("oldRegAutoGenerate").equals("Y"))
								|| (req.getParameter("casefileNo")!=null && !req.getParameter("casefileNo").equals("")) ) {
							Boolean exists = mrdCaseFileAttributesDao.exist("mr_no", mr_no);
							if(!exists) {
					    		BasicDynaBean mrdfile = mrdCaseFileAttributesDao.getBean();
					    		mrdfile.set("mr_no", mr_no);
					    		mrdfile.set("file_status", "A");
					    		mrdfile.set("case_status", "A");
					    		mrdfile.set("created_date", DateUtil.getCurrentDate());
					    		success = mrdCaseFileAttributesDao.insert(con, mrdfile);
							}
				    	}
					}

					if (success) {
						con.commit();
						
            ADTService adtService = (ADTService) ApplicationContextProvider.getApplicationContext()
                .getBean("adtService");
            Map<String, Object> adtData = new HashMap<>();
            if (bean.get("original_mr_no") != null
                && !((String) bean.get("original_mr_no")).isEmpty()) {
              adtData.put(Constants.MR_NO, (String) bean.get("original_mr_no"));
              adtData.put("old_mr_no", bean.get(Constants.MR_NO));
              adtService.createAndSendADTMessage("ADT_18", adtData);
            } else {
              adtData.put("patient_id", (String) bean.get("visit_id"));
              adtData.put(Constants.MR_NO, (String) bean.get(Constants.MR_NO));
              adtService.createAndSendADTMessage("ADT_08", adtData);
            }
            if (newPatient) {
              interfaceEventMappingService.preRegistrationEvent((String) bean.get(Constants.MR_NO));
            } else {
              if (duplicatePatient) {
                interfaceEventMappingService.mergePatientsEvent((String) bean.get("original_mr_no"), (String) bean.get(Constants.MR_NO));
              } else {
            	interfaceEventMappingService.editPatientEvent((String) bean.get(Constants.MR_NO));
              }
            }
            
						//update the modified patient details to the incoming patient in case of internal lab test.
						OhSampleRegistrationDAO.updateModifiedPatientDetailsForIncomingPatient(mr_no);
						Object object = (bean.getMap()).get("portal_access");
						Object mobileAccess = (bean.getMap()).get("mobile_access");
						if((Boolean)mobileAccess && 
						    MessageUtil.allowMessageNotification(req, "general_message_send")){
						  Map patientData = new HashMap();
						  patientData.put("mr_no", mr_no);
						  if(req.getParameter("visitId") != null) {
						    patientData.put("patient_id",req.getParameter("visitId"));
						  }
						  MessageManager mgr = new MessageManager();
						  mgr.processEvent("enable_mobile_access", patientData);
						}
						if (new Boolean(true).equals(object)) {
							//send out activation mail to patient
							try {
								registerbo.sendActivationMail(bean, password);
								flash.info("Patient details for "+ mr_no + " updated successfully. Activation mail send to user.");
							}
							catch (Exception e) {
								log.error("Error while sending email for " + mr_no, e);
								flash.info("Patient details for "+ mr_no + " updated successfully. Activation mail could not be send to user.");
							}
						}
						else {
							flash.success("Patient details for "+ mr_no + " updated successfully.");
						}
						redirect.addParameter("mr_no", mr_no);

					} else {
						con.rollback();
						flash.error("Failed to update patient details for " + mr_no);
					}
				}
				else {
					flash.error("Invalid values specified for patient with " + mr_no);
				}
			}
			finally {
				DataBaseUtil.closeConnections(con, null);
			}
		}
		else {
			Connection con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			try  {
				FlashScope flash = FlashScope.getScope(req);
				forward.addParameter(FlashScope.FLASH_KEY, flash.key());
				if (errors.isEmpty()) {
					boolean success = registerbo.registerPatient(con, bean, password);
					if (success) {
						if((req.getParameter("oldRegAutoGenerate")!=null && req.getParameter("oldRegAutoGenerate").equals("Y"))
								|| (req.getParameter("casefileNo")!=null && !req.getParameter("casefileNo").equals("")) ) {
							Boolean exists = mrdCaseFileAttributesDao.exist("mr_no", bean.get("mr_no").toString());
							if(!exists) {
					    		BasicDynaBean mrdfile = mrdCaseFileAttributesDao.getBean();
					    		mrdfile.set("mr_no", bean.get("mr_no").toString());
					    		mrdfile.set("file_status", "A");
					    		mrdfile.set("case_status", "A");
					    		mrdfile.set("created_date",DateUtil.getCurrentDate());
					    		
					    		BasicDynaBean contactPreferenceBean = contactPreferenceDao.getBean();
					    		contactPreferenceBean.set("mr_no", bean.get("mr_no").toString());
					    		contactPreferenceBean.set("lang_code", contactPrefLangCode);
					    		
					    		success = mrdCaseFileAttributesDao.insert(con, mrdfile) &&
					    				contactPreferenceDao.insert(con,contactPreferenceBean);
					    		
							}
				    	}
					}
					if (success) {
						con.commit();
						interfaceEventMappingService.preRegistrationEvent((String) bean.get("mr_no"));
						
					// Pre-registration
			      ADTService adtService = (ADTService) ApplicationContextProvider.getApplicationContext()
			          .getBean("adtService");
			      Map<String, Object> adtData = new HashMap<String, Object>();
			      adtData.put("patient_id", (String) bean.get("visit_id"));
			      adtData.put("mr_no", (String) bean.get("mr_no"));
			      adtService.createAndSendADTMessage("ADT_08", adtData);
			      
						mrNo = bean.get("mr_no").toString();
						BasicDynaBean patientbean = PatientDetailsDAO .getPatientGeneralDetailsBean(mrNo);
						forward.addParameter("mr_no", mrNo);
						forward.addParameter("full_name", patientbean.get("full_name").toString());

						Object object = (bean.getMap()).get("portal_access");
						Object mobileAccess = (bean.getMap()).get("mobile_access");
						if ((Boolean) mobileAccess 
						    && MessageUtil.allowMessageNotification(req, "general_message_send")) {
						  Map patientData = new HashMap();
						  patientData.put("mr_no", mrNo);
						  if(req.getParameter("visitId") != null) {
						    patientData.put("patient_id",req.getParameter("visitId"));
						  }
						  MessageManager mgr = new MessageManager();
						  mgr.processEvent("enable_mobile_access", patientData);
						  }
						if (new Boolean(true).equals(object)) {
							//send out activation mail to patient
							try {
								registerbo.sendActivationMail(bean, password);
								flash.success("Patient with "+ mrNo + " added successfully. Activation mail send to user.");
							}
							catch (Exception e) {
								log.error("Error while sending email for " + mrNo, e);
								flash.success("Patient with "+ mrNo + " added successfully. Activation mail could not be send to user.");
							}
						}
						else {
							flash.success("Patient with "+ mrNo + " added successfully.");
						}
					}
					else {
						con.rollback();
						flash.error("Failed to add new patient.");
					}
				}
				else {
					flash.error("Invalid values specified for patient");
				}
			}
			finally {
				DataBaseUtil.closeConnections(con, null);
			}

		}

		if(null != req.getParameter("visitId") && !req.getParameter("visitId").equals("")) {
			FlashScope flash = FlashScope.getScope(req);
			redirect = new ActionRedirect("/pages/registration/editvisitdetails.do");
			redirect.addParameter("_method", "getPatientVisitDetails");
			redirect.addParameter("ps_status", "active");
			redirect.addParameter("patient_id", req.getParameter("visitId"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		}

		if (!"".equals(mr_no)){
			return redirect;

		}else{
			return forward;
		}

	}

	private void setAttributes(HttpServletRequest req) throws SQLException {
		JSONSerializer js = new JSONSerializer().exclude("class");
		int centerId = (Integer)req.getSession().getAttribute("centerId");
		PatientDetailsDAO pdao = new PatientDetailsDAO();

		req.setAttribute("regPref", RegistrationPreferencesDAO.getRegistrationPreferences());
		req.setAttribute("regPrefJSON", js.serialize(RegistrationPreferencesDAO.getRegistrationPreferences()));
		req.setAttribute("healthAuthoPrefJSON", js.serialize
				(HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(CenterMasterDAO.getHealthAuthorityForCenter(centerId))));
		req.setAttribute("salutationQueryJson",js.serialize(ConversionUtils.listBeanToListMap(SalutationMasterDAO.getSalutationIdName())));
        req.setAttribute("categoryWiseRateplans", js.serialize(ConversionUtils.listBeanToListMap(
        		PatientCategoryDAO.getAllCategoriesIncSuperCenter(centerId))));
        BasicDynaBean centerPrefs = new CenterPreferencesDAO().getCenterPreferences(centerId);
		req.setAttribute("centerPrefs", centerPrefs);


		BasicDynaBean centerBean = hospitalCanterMasterDao.findByKey("center_id", centerId);
		String cityId=null, stateId=null, countryId=null, cityName=null, stateName=null, countryName=null, districtId = null, districtName = null;
		if(!((String) centerBean.get("city_id")).equals(""))
		{
			cityId = (String) centerBean.get("city_id");
			stateId = (String) centerBean.get("state_id");
			countryId = (String) centerBean.get("country_id");
			cityName = (String) (cityDao.findByKey("city_id", cityId)).get("city_name");
			stateName = (String) (stateMasterDao.findByKey("state_id", stateId )).get("state_name");
			countryName = (String) (countryMasterDao.findByKey("country_id", countryId)).get("country_name");
			BasicDynaBean districtBean = pdao.getDistrictDetails(cityId, stateId);
			if(districtBean != null) {
				districtId = (String)districtBean.get("district_id");
				districtName = (String)districtBean.get("district_name");
			}
  		}

		req.setAttribute("defaultCity", cityId );
		req.setAttribute("defaultDistrict", districtId );
		req.setAttribute("defaultState", stateId );
		req.setAttribute("defaultCountry", countryId );
		req.setAttribute("defaultCityName", cityName );
		req.setAttribute("defaultDistrictName", districtName );
		req.setAttribute("defaultStateName", stateName );
		req.setAttribute("defaultCountryName", countryName );

        HashMap actionRightsMap = new HashMap();
		Object roleID=null;
		Role role=new Role();
		actionRightsMap= (HashMap) req.getSession(false).getAttribute("actionRightsMap");
		roleID=  req.getSession(false).getAttribute("roleId");
		String actionRightStatus=(String) actionRightsMap.get(role.EDIT_PATIENT_FIRST_NAME);

		if (actionRightStatus==null)
			actionRightStatus="N";

		if (actionRightStatus.equalsIgnoreCase("A")|| roleID.equals(1)|| roleID.equals(2))
			req.setAttribute("editActionRights", "Y");
	    else
	    	req.setAttribute("editActionRights", "N");

		Map<String,List> customListMap = new HashMap<String,List>();
		for(int i=1;i<=9;i++) {
			List customList= new GenericDAO("custom_list"+i+"_master").listAll(null, "status", "A", "custom_value");
			customListMap.put("custom_list"+i+"_master",customList);
		}
		req.setAttribute("customListMap", customListMap);
		req.setAttribute("govtIdentifierTypes", new GovtIdentifierMasterDAO().listAll(null, "status", "A"));
		req.setAttribute("govtIdentifierTypesJSON", js.serialize(ConversionUtils.copyListDynaBeansToMap(new GovtIdentifierMasterDAO().listAll(null, "status", "A"))));
	    List<BasicDynaBean> otherIdentifier = otherIdDocTypesDAO.listAll(null, "status", "A");
	    req.setAttribute("otherIdentifierTypes", otherIdentifier);
	    req.setAttribute("otherIdentifierTypesJSON",
	        js.serialize(ConversionUtils.copyListDynaBeansToMap(otherIdentifier)));
	}

    public ActionForward viewPatientPhoto(ActionMapping mapping, ActionForm af, HttpServletRequest req,
    		HttpServletResponse res) throws SQLException, IOException {

    	String mrno = req.getParameter("mrno");
		if (mrno != null) {
			res.setContentType("image/png");
			OutputStream os = res.getOutputStream();
			InputStream s = PatientDetailsDAO.getPatientPhoto(mrno);

			if (s != null) {
				byte[] bytes = new byte[1024];
				int len = 0;
				while ( (len = s.read(bytes)) > 0) {
					os.write(bytes, 0, len);
				}
				os.flush();
				s.close();
				return null;
			} else {
				return mapping.findForward("error");
			}
		} else {
			return mapping.findForward("error");
		}
	}

    public ActionForward viewInsuranceCardImage(ActionMapping mapping, ActionForm af, HttpServletRequest req,
    		HttpServletResponse res) throws SQLException, IOException {

    	String sponsorType = "I";
    	return viewCardImage(mapping, req, res, sponsorType);
    }

    public ActionForward viewCorporateCardImage(ActionMapping mapping, ActionForm af, HttpServletRequest req,
    		HttpServletResponse res) throws SQLException, IOException {

    	String sponsorType = "C";
    	return viewCardImage(mapping, req, res, sponsorType);
    }

    public ActionForward viewNationalCardImage(ActionMapping mapping, ActionForm af, HttpServletRequest req,
    		HttpServletResponse res) throws SQLException, IOException {

    	String sponsorType = "N";
    	return viewCardImage(mapping, req, res, sponsorType);
    }


    public ActionForward viewCardImageForVisit(ActionMapping mapping, ActionForm af, HttpServletRequest req,
    		HttpServletResponse res) throws SQLException, IOException {

    	String sponsorType = req.getParameter("sponsorType");
    	String sponsorIndex = req.getParameter("sponsorIndex");
    	String visitId = req.getParameter("visitId");

		OutputStream os = res.getOutputStream();
		Map cardMap	= sponsorIndex.equals("P")? PatientDetailsDAO.getCurrentPatientCardImageMap(visitId, sponsorType)
				: PatientDetailsDAO.getCurrentPatientSecCardImageMap(visitId, sponsorType);

		InputStream s = cardMap != null ? (InputStream)cardMap.get("CONTENT") : null;
       	String contentType = cardMap != null ? (String)cardMap.get("CONTENT_TYPE") : "image/png";
		res.setContentType(contentType);

		if (s != null) {
			byte[] bytes = new byte[1024];
			int len = 0;
			while ( (len = s.read(bytes)) > 0) {
				os.write(bytes, 0, len);
			}
			os.flush();
			s.close();
			return null;
		} else {
			return mapping.findForward("error");
		}

    }

    public ActionForward viewCardImage(ActionMapping mapping, HttpServletRequest req,
    		HttpServletResponse res, String sponsorType) throws SQLException, IOException {
    	String visitId = req.getParameter("visitId");
		   visitId = visitId == null || visitId.equals("") ? req.getParameter("patientId"): visitId ;
		   visitId = visitId == null  || visitId.equals("")? req.getParameter("patient_id"): visitId ;

	 	int docId = req.getParameter("doc_id")== null || req.getParameter("doc_id").equals("")? 0
					: Integer.parseInt(req.getParameter("doc_id"));
    	if(docId != 0 ){
			OutputStream os = res.getOutputStream();

			Map cardMap	= PatientDetailsDAO.getPatientCardImageMap(docId, sponsorType);
			InputStream s = cardMap != null ? (InputStream)cardMap.get("CONTENT") : null;
	       	String contentType = cardMap != null ? (String)cardMap.get("CONTENT_TYPE") : "image/png";
			res.setContentType(contentType);

			if (s != null) {
				byte[] bytes = new byte[1024];
				int len = 0;
				while ( (len = s.read(bytes)) > 0) {
					os.write(bytes, 0, len);
				}
				os.flush();
				s.close();
				return null;
			} else {
				return mapping.findForward("error");
			}

    	} else if (visitId != null) {
			OutputStream os = res.getOutputStream();

			Map cardMap	= PatientDetailsDAO.getCurrentPatientCardImageMap(visitId, sponsorType);
			InputStream s = cardMap != null ? (InputStream)cardMap.get("CONTENT") : null;
	       	String contentType = cardMap != null ? (String)cardMap.get("CONTENT_TYPE") : "image/png";
			res.setContentType(contentType);

			if (s != null) {
				byte[] bytes = new byte[1024];
				int len = 0;
				while ( (len = s.read(bytes)) > 0) {
					os.write(bytes, 0, len);
				}
				os.flush();
				s.close();
				return null;
			} else {
				return mapping.findForward("error");
			}
		} else {
			return mapping.findForward("error");
		}
	}

    public ActionForward viewInsCardImageForVisit(ActionMapping mapping, ActionForm af, HttpServletRequest req,
    		HttpServletResponse res) throws SQLException, IOException {

    	String sponsorType = req.getParameter("sponsorType").trim();
    	String sponsorIndex = req.getParameter("sponsorIndex");
    	String visitId = req.getParameter("visitId");
    	String policyIDstr = req.getParameter("patient_policy_id");
    	Integer policyID = (policyIDstr == null || policyIDstr.equals("")) ? 0 : Integer.parseInt(policyIDstr);

		OutputStream os = res.getOutputStream();
		Map cardMap	= sponsorIndex.equals("P")? PatientDetailsDAO.getCurrentPatientInsCardImageMap(visitId, sponsorType, policyID)
				: PatientDetailsDAO.getCurrentPatientInsSecCardImageMap(visitId, sponsorType, policyID);

		InputStream s = cardMap != null ? (InputStream)cardMap.get("CONTENT") : null;
       	String contentType = cardMap != null ? (String)cardMap.get("CONTENT_TYPE") : "image/png";
		res.setContentType(contentType);

		if (s != null) {
			byte[] bytes = new byte[1024];
			int len = 0;
			while ( (len = s.read(bytes)) > 0) {
				os.write(bytes, 0, len);
			}
			os.flush();
			s.close();
			return null;
		} else {
			return mapping.findForward("error");
		}

    }

    private static String getValue(String key, Map params) {
		Object[] obj = (Object[]) params.get(key);
		if (obj != null && obj[0] != null) {
			return obj[0].toString();
		}
		return "";
    }
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
		result.put("month", hDateInfo[1]);
		result.put("year", hDateInfo[0]);
		response.getWriter().write(js.serialize(result));

		response.flushBuffer();
	    return null;
	}


    public ActionForward getHijriToGregorian(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws IOException,Exception{



    	JSONSerializer js = new JSONSerializer();
		String day = request.getParameter("dobDay");
		String month = request.getParameter("dobMonth");
		String year = request.getParameter("dobYear");

		UmmalquraCalendar cal = new UmmalquraCalendar(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
		java.util.Date date = cal.getTime();

		response.setContentType("application/x-json");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		String dateStr = DateUtil.formatDate(date);
		String[] dateArr = dateStr.split("-");
		Map result = new HashMap();
		result.put("day", dateArr[0]);
		result.put("month", dateArr[1]);
		result.put("year", dateArr[2]);
		response.getWriter().write(js.serialize(result));

		response.flushBuffer();
	    return null;
	}

}
