/**
 *
 */
package com.insta.hms.GenericForms;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.instaforms.AbstractInstaForms;
import com.insta.hms.instaforms.GenericForms;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.FormComponents.FormComponentsDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.ImageMarkers.ImageMarkerDAO;
import com.insta.hms.master.PhraseSuggestionsMaster.PhraseSuggestionsMasterDAO;
import com.insta.hms.master.RegularExpression.RegularExpressionDAO;
import com.insta.hms.master.Sections.SectionsDAO;
import com.insta.hms.master.outpatient.SystemGeneratedSectionsDAO;
import com.insta.hms.master.sectionrolerights.SectionRoleRightsDAO;
import com.insta.hms.mdm.allergy.AllergyTypeService;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.outpatient.SecondaryComplaintDAO;
import com.insta.hms.vitalForm.VisitVitalsDAO;
import com.insta.hms.vitalForm.genericVitalFormDAO;
import com.insta.hms.vitalparameter.VitalMasterDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author insta
 *
 */
public class GenericFormsAction extends DispatchAction {

	static Logger log = LoggerFactory.getLogger(GenericFormsAction.class);
    static ClinicalPreferencesService clinicalPreferencesService = ApplicationContextProvider
        .getBean(ClinicalPreferencesService.class);
    static AllergyTypeService allergyTypeService = ApplicationContextProvider.getBean(
        AllergyTypeService.class);

	public ActionForward launchScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		return mapping.findForward("genericformsscreen");
	}

	@IgnoreConfidentialFilters
	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		String patientId = request.getParameter("patient_id");
		VisitDetailsDAO visitDAO = new VisitDetailsDAO();
		if (patientId != null && !patientId.equals("")) {
			BasicDynaBean visitbean = visitDAO.findByKey("patient_id", patientId);
			if ( visitbean == null ) {
				FlashScope flash = FlashScope.getScope(request);
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
				redirect.addParameter("defaultScreen", true);
				flash.info(patientId + " doesn't exist.");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
			Map listingParams = ConversionUtils.getListingParameter(request.getParameterMap());
			listingParams.put(LISTING.SORTCOL, "form_name");
			request.setAttribute("pagedList", new GenericForms().getAddedForms(patientId, listingParams));
		}

		return mapping.findForward("genericformsscreen");
	}

	public ActionForward getChooseGenericFormScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
		String patientId = request.getParameter("patient_id");
		if (patientId != null && !patientId.equals("")) {
			VisitDetailsDAO visitDAO = new VisitDetailsDAO();
			BasicDynaBean visitbean = visitDAO.findByKey("patient_id", patientId);
			if (visitbean == null) {
				FlashScope flash = FlashScope.getScope(request);
				flash.put("error", patientId+" doesn't exists.");
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("chooseGenericFormRedirect"));
				redirect.addParameter("defaultScreen", true);
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
			JSONSerializer js = new JSONSerializer().exclude("class");
			List<BasicDynaBean> genForms = new GenericForms().getForms(patientId);
			request.setAttribute("list", genForms);
			request.setAttribute("listJson", js.serialize(ConversionUtils.copyListDynaBeansToMap(genForms)));
		}
		return mapping.findForward("getChooseGenericFormScreen");
	}

	public ActionForward addOrEditGenericForm(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		AbstractInstaForms formDAO = new GenericForms();
		BasicDynaBean genericform = formDAO.getComponents(request.getParameterMap());
		request.setAttribute("form", genericform);
		request.setAttribute("insta_form_json", js.serialize(genericform.getMap()));
		request.setAttribute("group_patient_sections",
				new FormComponentsDAO().findByKey("id", genericform.get("form_id")).get("group_patient_sections"));
		request.setAttribute("section_rights", new SectionRoleRightsDAO().getAllSectionsRights(
				(Integer) request.getSession().getAttribute("roleId")));
		
		List<BasicDynaBean> sectionsDefList = new SectionsDAO().getSections((String) genericform.get("sections"));
		request.setAttribute("sectionsDefMap", ConversionUtils.listBeanToMapBean(sectionsDefList, "section_id"));
		
		// markers of fields from all sections.
		List<BasicDynaBean> imageMarkers = new ImageMarkerDAO().getMarkers((String) genericform.get("sections"));
		request.setAttribute("sectionsImageMarkers", ConversionUtils.listBeanToMapListBean(imageMarkers, "section_id"));

		List<BasicDynaBean> sectionsList = new SectionsDAO().listAll();
		request.setAttribute("insta_sections", sectionsList);
		request.setAttribute("insta_sections_json", js.serialize(ConversionUtils.copyListDynaBeansToMap(sectionsList)));

		String patientId = request.getParameter("patient_id");
		VisitDetailsDAO visitDAO = new VisitDetailsDAO();
		SecondaryComplaintDAO scomplaintDao = new SecondaryComplaintDAO();
		
		java.util.Map  visitBean =  com.insta.hms.Registration.VisitDetailsDAO.getPatientVisitDetailsMap(patientId);
		BasicDynaBean consBean = (BasicDynaBean) visitDAO.getAllVisitsAndDoctorsByPatientId(patientId);
		if(visitBean != null) {
			request.setAttribute("consultation_bean", visitBean);
		}
		request.setAttribute("antenatal_bean_doctor_id", consBean.get("doctor"));
		request.setAttribute("antenatal_bean_doctor_name", consBean.get("doctor_name"));
		request.setAttribute("secondary_complaints", scomplaintDao.getSecondaryComplaints(patientId));
		request.setAttribute("validate_diagnosis_codification", GenericPreferencesDAO.getAllPrefs().get("validate_diagnosis_codification"));
		request.setAttribute("diagnosis_details", MRDDiagnosisDAO.getAllDiagnosisDetails(patientId));

		VitalMasterDAO vmDAO = new VitalMasterDAO();
		VisitVitalsDAO vvDAO = new VisitVitalsDAO();
		request.setAttribute("all_fields", vmDAO.getActiveVitalParams("O"));
		List readingList = vvDAO.getVitals(patientId, null, null, "V");
		request.setAttribute("vital_readings", readingList);
		// vital reading exists is defined using above list, anyhow no date filters applied.
		request.setAttribute("vital_reading_exists", !readingList.isEmpty());
		request.setAttribute("latest_vital_reading_json", 
				js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
						vvDAO.getLatestVitals((String) visitBean.get("mr_no"), patientId))));
		request.setAttribute("height_weight_params", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
				vvDAO.getHeightAndWeight((String) visitBean.get("mr_no"), patientId))));
		request.setAttribute("prefColorCodes", GenericPreferencesDAO.getAllPrefs());
		request.setAttribute("paramType", "V");
		request.setAttribute("referenceList", genericVitalFormDAO.getReferenceRange(visitBean));

		int genericFormId = 0;
		String genericFormIdStr = request.getParameter("generic_form_id");
		if (genericFormIdStr != null && !genericFormIdStr.equals(""))
			genericFormId = Integer.parseInt(genericFormIdStr);
		
		PatientSectionDetailsDAO psd = new PatientSectionDetailsDAO();
		request.setAttribute("section_finalize_status", 
				ConversionUtils.listBeanToMapMap(psd.getSections((String) visitBean.get("mr_no"), patientId, 0, genericFormId, 
						(Integer) genericform.get("form_id")), "section_id"));
		
        request.setAttribute("allergies", formDAO.getAllergies((String) visitBean.get("mr_no"),
            (String) visitBean.get("patient_id"), 0, genericFormId, (Integer) genericform.get(
                "form_id")));
        request.setAttribute("allowFreeTextInAllergies", clinicalPreferencesService
            .getClinicalPreferences().get("allow_free_text_in_allergies"));
        request.setAttribute("allergy_types", ConversionUtils.listBeanToListMap(allergyTypeService
            .listAll(null, "status", "A")));
		
		request.setAttribute("preAnaesthestheticList",
				formDAO.getPreAnaestestheticRecords((String) visitBean.get("mr_no"), patientId, 0,
						genericFormId, (Integer) genericform.get("form_id")));
		request.setAttribute("pregnancyhistories",
				formDAO.getPregnancyHistories((String)visitBean.get("mr_no"), (String) visitBean.get("patient_id"),  0, genericFormId, (Integer) genericform.get("form_id")));
		request.setAttribute("pregnancyhistoriesBean",
				ConversionUtils.listBeanToListMap(formDAO.getObstetricrecords((String)visitBean.get("mr_no"), (String) visitBean.get("patient_id"),  0,genericFormId, (Integer) genericform.get("form_id"))));
		request.setAttribute("antenatalinfo",
				formDAO.getAntenatalRecords((String)visitBean.get("mr_no"), patientId,  0, genericFormId, (Integer) genericform.get("form_id")));

		request.setAttribute("actionId", mapping.getProperty("action_id"));
		java.util.List<BasicDynaBean> phrase_suggestions = PhraseSuggestionsMasterDAO.getPhraseSuggestionsDynaList();
		request.setAttribute("phrase_suggestions_json", js.deepSerialize(
				ConversionUtils.listBeanToMapListMap(phrase_suggestions, "phrase_suggestions_category_id")));

		String userName = (String) request.getSession(false).getAttribute("userid");
		GenericDAO userDAO = new GenericDAO("u_user");
		BasicDynaBean userbean = userDAO.findByKey("emp_username", userName);
		String doctor_dept = null;

        if (userbean != null) {
        	request.setAttribute("isSharedLogIn", userbean.get("is_shared_login"));
        	request.setAttribute("roleId", userbean.get("role_id"));
        	String doctor_id = (String)userbean.get("doctor_id");
        	if (doctor_id != null) {
        		GenericDAO doctorDAO = new GenericDAO("doctors");
        		BasicDynaBean doctorbean = doctorDAO.findByKey("doctor_id", doctor_id );
        		if(doctorbean != null) {
        			doctor_dept = (String)doctorbean.get("dept_id");
        		}
        	}
        }

        String phrase_dept = null;
		if(doctor_dept == null || doctor_dept.equals(""))
        	phrase_dept = (String) visitBean.get("dept_name");
        else
        	phrase_dept = doctor_dept ;


		java.util.List<BasicDynaBean> phrase_suggestions_by_dept =
			PhraseSuggestionsMasterDAO.getPhraseSuggestionsByDeptDynaList(phrase_dept);
		request.setAttribute("phrase_suggestions_by_dept_json", js.deepSerialize(
				ConversionUtils.listBeanToMapListMap(phrase_suggestions_by_dept, "phrase_suggestions_category_id")));

		request.setAttribute("sys_generated_forms", js.deepSerialize(ConversionUtils.listBeanToListMap(
				new SystemGeneratedSectionsDAO().listAll())));

		request.setAttribute("sys_generated_section", ConversionUtils.listBeanToListMap(new SystemGeneratedSectionsDAO().listAll()));

		String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter((Integer) visitBean.get("center_id"));
		request.setAttribute("defaultDiagnosisCodeType",
				HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthority).getDiagnosis_code_type());
		
		HashMap<Integer ,String> regExpPatternMap = RegularExpressionDAO.getRegPatternWithExpression("E");
		request.setAttribute("regExpMapDesc", RegularExpressionDAO.getRegPatternWithExpression("D"));
		request.setAttribute("regExpPatternMap", js.serialize(regExpPatternMap));
		
		return mapping.findForward("addoreditgenericformscreen");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, Exception {
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addoreditformredirect"));
		AbstractInstaForms formDAO = new GenericForms();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		String error = null;
		Boolean flag = false;
		Map params = new HashMap(request.getParameterMap());
		try {
			error = formDAO.save(con, params);
			if (error == null)
				flag = true;
		} finally {
			DataBaseUtil.commitClose(con, flag);
		}
		FlashScope flash = FlashScope.getScope(request);
		if (error != null) {
			flash.error(error);
		}
		Boolean isPrint = new Boolean(request.getParameter("printGenericForm"));
		if (isPrint) {
			ActionRedirect printRedirect = new ActionRedirect(mapping.findForward("printGenericForm"));
			printRedirect.addParameter("patient_id", request.getParameter("patient_id"));
			printRedirect.addParameter("insta_form_id", request.getParameter("insta_form_id"));
			// generic form id retrieved from param map because first time when it is the form is saved,
			// generic form id is generated inside the save method
			// and stored in a parameter map.
			printRedirect.addParameter("generic_form_id", ConversionUtils.getParamValue(params, "generic_form_id", "0"));
			printRedirect.addParameter("printerId", request.getParameter("printerId"));
			printRedirect.addParameter("templateName", request.getParameter("printTemplate"));

			List<String> printURLs = new ArrayList<String>();
			printURLs.add(request.getContextPath() + printRedirect.getPath());
			request.getSession(false).setAttribute("printURLs", printURLs);
		}

		redirect.addParameter("patient_id", request.getParameter("patient_id"));
		redirect.addParameter("insta_form_id", request.getParameter("insta_form_id"));
		redirect.addParameter("generic_form_id", ConversionUtils.getParamValue(params, "generic_form_id", "0"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}


 }
