/**
 *
 */
package com.insta.hms.TriageForm;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.instaforms.AbstractInstaForms;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;
import com.insta.hms.instaforms.TriageForms;
import com.insta.hms.master.FormComponents.FormComponentsDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.ImageMarkers.ImageMarkerDAO;
import com.insta.hms.master.PhraseSuggestionsMaster.PhraseSuggestionsMasterDAO;
import com.insta.hms.master.RegularExpression.RegularExpressionDAO;
import com.insta.hms.master.Sections.SectionsDAO;
import com.insta.hms.master.outpatient.SystemGeneratedSectionsDAO;
import com.insta.hms.master.sectionrolerights.SectionRoleRightsDAO;
import com.insta.hms.outpatient.AllergiesDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.outpatient.SecondaryComplaintDAO;
import com.insta.hms.usermanager.User;
import com.insta.hms.usermanager.UserDAO;
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

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna
 *
 */
public class TriageFormAction extends DispatchAction {
	VitalMasterDAO vmDAO = new VitalMasterDAO();
	VisitVitalsDAO vvDAO = new VisitVitalsDAO();
	DoctorConsultationDAO consultDao = new DoctorConsultationDAO();
	PatientDetailsDAO patDetDao = new PatientDetailsDAO();
	SecondaryComplaintDAO scomplaintDao = new SecondaryComplaintDAO();
	UserDAO userDao = new UserDAO();
	AllergiesDAO allerDao = new AllergiesDAO();

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException, ParseException {
		
		
		String consIdStr = request.getParameter("consultation_id");
		String mrNo = request.getParameter("mr_no");
		int consId = Integer.parseInt(consIdStr);
		
		Map modulesActivatedMap = ((Preferences) RequestContext.getSession().getAttribute("preferences")).getModulesActivatedMap();
		String mod_newcons_status = (String)modulesActivatedMap.get("mod_newcons");
		if (null != mod_newcons_status && mod_newcons_status.equals("Y")) {
			if (null == mrNo || mrNo.equals("")) {
				BasicDynaBean consult_bean = consultDao.findByKey("consultation_id" ,consId);
				mrNo = (String) consult_bean.get("mr_no");
			}
			ActionRedirect redirect = new ActionRedirect("/triage/index.htm#/filter/default/patient/"+ URLEncoder.encode(mrNo, "UTF-8") +"/triage/"+ consId +"?retain_route_params=true");
			return redirect;
		}
		
		JSONSerializer js = new JSONSerializer().exclude("class");
		BasicDynaBean consultBean = consultDao.findConsultationExt(consId);
		request.setAttribute("consultation_bean", consultBean.getMap());
		request.setAttribute("all_fields", vmDAO.getActiveVitalParams("O"));
		List readingList = vvDAO.getVitals(request.getParameter("patient_id"),
				null, null, "V");
		request.setAttribute("vital_readings", readingList);
		// vital reading exists is defined using above list, anyhow no date filters applied.
		request.setAttribute("vital_reading_exists", !readingList.isEmpty());
		request.setAttribute("latest_vital_reading_json", 
				js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
						vvDAO.getLatestVitals((String) consultBean.get("mr_no"), (String) consultBean.get("patient_id")))));
		request.setAttribute("height_weight_params", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
				vvDAO.getHeightAndWeight((String) consultBean.get("mr_no"), (String) consultBean.get("patient_id")))));	
		String patientId = request.getParameter("patient_id");
		Map patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap(patientId);
		//BasicDynaBean patDetBean = patDetDao.getBean();
		request.setAttribute("patient_details", PatientDetailsDAO.getPatientGeneralDetailsMap((String) consultBean.get("mr_no")));
		BasicDynaBean consBean = (BasicDynaBean) new VisitDetailsDAO().getAllVisitsAndDoctorsByPatientId(patientId);
		request.setAttribute("antenatal_bean_doctor_id", consBean.get("doctor"));
		request.setAttribute("antenatal_bean_doctor_name", consBean.get("doctor_name"));
		
		AbstractInstaForms formDAO = new TriageForms();
		BasicDynaBean triageform = formDAO.getComponents(request.getParameterMap());
		request.setAttribute("form", triageform);
		request.setAttribute("insta_form_json", js.serialize(triageform.getMap()));
		
		List<BasicDynaBean> sectionsDefList = new SectionsDAO().getSections((String) triageform.get("sections"));
		request.setAttribute("sectionsDefMap", ConversionUtils.listBeanToMapBean(sectionsDefList, "section_id"));
		
		PatientSectionDetailsDAO psd = new PatientSectionDetailsDAO();
		request.setAttribute("section_finalize_status", 
				ConversionUtils.listBeanToMapMap(psd.getSections((String)consultBean.get("mr_no"), patientId, consId, 0, 
						(Integer) triageform.get("form_id")), "section_id"));

		List<BasicDynaBean> sectionsList = new SectionsDAO().listAll();
		request.setAttribute("insta_sections", sectionsList);
		request.setAttribute("insta_sections_json", js.serialize(ConversionUtils.copyListDynaBeansToMap(sectionsList)));
		request.setAttribute("group_patient_sections",
				new FormComponentsDAO().findByKey("id", triageform.get("form_id")).get("group_patient_sections"));
		request.setAttribute("section_rights", new SectionRoleRightsDAO().getAllSectionsRights(
				(Integer) request.getSession().getAttribute("roleId")));

		request.setAttribute("allergies", formDAO.getAllergies((String)consultBean.get("mr_no"),
				(String) consultBean.get("patient_id"), consId, 0, (Integer) triageform.get("form_id")));
		request.setAttribute("pregnancyhistories",
				formDAO.getPregnancyHistories((String)consultBean.get("mr_no"), (String) consultBean.get("patient_id"),  consId, 0, (Integer) triageform.get("form_id")));
		request.setAttribute("antenatalinfo",
				formDAO.getAntenatalRecords((String)consultBean.get("mr_no"), (String) consultBean.get("patient_id"),  consId, 0, (Integer) triageform.get("form_id")));	
		request.setAttribute("pregnancyhistoriesBean",
				ConversionUtils.listBeanToListMap(formDAO.getObstetricrecords((String)consultBean.get("mr_no"), patientId, consId, 0, (Integer) triageform.get("form_id"))));
		List systemGeneratedSectionList = ConversionUtils.listBeanToListMap(new SystemGeneratedSectionsDAO().listAll());
		request.setAttribute("sys_generated_forms", js.deepSerialize(systemGeneratedSectionList));
		request.setAttribute("sys_generated_section", systemGeneratedSectionList);

		request.setAttribute("secondary_complaints", scomplaintDao.getSecondaryComplaints((String) consultBean.get("patient_id")));

		User user = userDao.getUser((String) request.getSession(false).getAttribute("userid"));
		request.setAttribute("doctor_logged_in", user == null ? "" : user.getDoctorId());
		request.setAttribute("referenceList", genericVitalFormDAO.getReferenceRange(patientDetails));
		request.setAttribute("prefColorCodes", GenericPreferencesDAO.getAllPrefs());
		BasicDynaBean clinicalPreferences = ApplicationContextProvider.getApplicationContext().getBean(ClinicalPreferencesService.class).getClinicalPreferences();
		request.setAttribute("clinicalPrefs", clinicalPreferences.getMap());
		request.setAttribute("paramType", "V");


		java.util.List<BasicDynaBean> phrase_suggestions = PhraseSuggestionsMasterDAO.getPhraseSuggestionsDynaList();
		request.setAttribute("phrase_suggestions_json", js.deepSerialize(
				ConversionUtils.listBeanToMapListMap(phrase_suggestions, "phrase_suggestions_category_id")));

		java.util.List<BasicDynaBean> phrase_suggestions_by_dept = PhraseSuggestionsMasterDAO.getPhraseSuggestionsByDeptDynaList((String)consultBean.get("dept_id"));
		request.setAttribute("phrase_suggestions_by_dept_json", js.deepSerialize(
				ConversionUtils.listBeanToMapListMap(phrase_suggestions_by_dept, "phrase_suggestions_category_id")));
		
		// markers of fields from all sections.
		List<BasicDynaBean> imageMarkers = new ImageMarkerDAO().getMarkers((String) triageform.get("sections"));
		request.setAttribute("sectionsImageMarkers", ConversionUtils.listBeanToMapListBean(imageMarkers, "section_id"));
		
		HashMap<Integer ,String> regExpPatternMap = RegularExpressionDAO.getRegPatternWithExpression("E");
		request.setAttribute("regExpMapDesc", RegularExpressionDAO.getRegPatternWithExpression("D"));
		request.setAttribute("regExpPatternMap", js.serialize(regExpPatternMap));
		
		return mapping.findForward("addshow");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException, ParseException,
			Exception {
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		String patientId = request.getParameter("patient_id");
		String consultationIdStr = request.getParameter("consultation_id");
		Map params = request.getParameterMap();

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean allSuccess = false;
		String error = null;

		try {
			txn: {
				// save the sections like allergies, vitals, and insta sections.
				error = new TriageForms().save(con, params);

				// update the doctor consultation(for nurse assessment, emergency-category, immunization status)
				BasicDynaBean consultBean = consultDao.getBean();
				consultBean.set("immunization_status_upto_date", request.getParameter("immunization_status_upto_date"));
				consultBean.set("emergency_category", request.getParameter("emergency_category"));
				String triageDone = request.getParameter("triage_done");
				triageDone = (triageDone == null || triageDone.equals("")) ? "N" : triageDone;
				consultBean.set("triage_done", triageDone);

				if (consultDao.update(con, consultBean.getMap(), "consultation_id", Integer.parseInt(consultationIdStr)) <=0 )
					break txn;

				allSuccess = true;
			}
		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}
		FlashScope flash = FlashScope.getScope(request);

		if (allSuccess) {
			if (new Boolean(request.getParameter("printTriage"))) {
				ActionRedirect printRedirect = new ActionRedirect(mapping.findForward("printRedirect"));
				printRedirect.addParameter("visitId", patientId);
				printRedirect.addParameter("consultation_id", consultationIdStr);

				List<String> printURLs = new ArrayList<String>();
				printURLs.add(request.getContextPath() + printRedirect.getPath());
				request.getSession(false).setAttribute("printURLs", printURLs);
			}
		} else {
			flash.put("error", error == null ? "Transaction Failed" : error);
		}
		redirect.addParameter("patient_id", patientId);
		redirect.addParameter("consultation_id", consultationIdStr);

		return redirect;
	}

}
