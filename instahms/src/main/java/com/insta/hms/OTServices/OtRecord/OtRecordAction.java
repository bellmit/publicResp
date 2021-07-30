package com.insta.hms.OTServices.OtRecord;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.OTServices.OperationDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PushService;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.multiuser.MultiUserService;
import com.insta.hms.core.clinical.multiuser.MultiUserRedisRepository;
import com.insta.hms.instaforms.AbstractInstaForms;
import com.insta.hms.instaforms.OTForms;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CommonPrintTemplates.PrintTemplate;
import com.insta.hms.master.CommonPrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.FormComponents.FormComponentsDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.ImageMarkers.ImageMarkerDAO;
import com.insta.hms.master.PhraseSuggestionsMaster.PhraseSuggestionsMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.RegularExpression.RegularExpressionDAO;
import com.insta.hms.master.Sections.SectionsDAO;
import com.insta.hms.master.outpatient.SystemGeneratedSectionsDAO;
import com.insta.hms.master.sectionrolerights.SectionRoleRightsDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.outpatient.SecondaryComplaintDAO;

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
 * @author nikunj.s
 *
 */
public class OtRecordAction extends DispatchAction {
  
	static Logger log = LoggerFactory.getLogger(OtRecordAction.class);
	SecondaryComplaintDAO scomplaintDao = new SecondaryComplaintDAO();
	OtRecordDAO otDAO = new OtRecordDAO();
	VisitDetailsDAO visitDAO = new VisitDetailsDAO();
	FormComponentsDAO consCompDao = new FormComponentsDAO();
	SectionsDAO phyFormDesc = new SectionsDAO();
	OperationDetailsDAO opDetDAO = new OperationDetailsDAO();
	GenericDAO opProcedureDAO = new GenericDAO("operation_procedures");
	private String screenLockRedisKey = "screen_id:ot_record;screenlock";
    private String screenId = "ot_record";
	
	@IgnoreConfidentialFilters
	public ActionForward getOperationsList(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
		String opDetailsId = request.getParameter("operation_details_id");
		String patientId = request.getParameter("visit_id");
		OTForms formDAO = new OTForms();
		request.setAttribute("forms", formDAO.getFormNames(Integer.parseInt(opDetailsId), patientId));
		return mapping.findForward("otrecordlist");
	}

	public ActionForward list(ActionMapping mapping, ActionForm from, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {

		String userName = (String)request.getSession(false).getAttribute("userid");
		String patientId = request.getParameter("visit_id");
		int opProcId = Integer.parseInt(request.getParameter("operation_proc_id"));
		request.setAttribute("operation_bean", OtRecordDAO.getOperation(opProcId));
		Integer patientCenterId = null;
		BasicDynaBean bean = visitDAO.findByKey("patient_id", patientId);
		if(bean == null) {
			// user entered visit id is not a valid patient id.
			FlashScope flash = FlashScope.getScope(request);
			flash.put("error", "No Patient with Id:"+patientId);
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		} else {
			patientCenterId = (Integer)bean.get("center_id");
		}
		MultiUserRedisRepository redisRepo = ApplicationContextProvider
            .getBean(MultiUserRedisRepository.class);
        boolean isScreenLocked = false;
        String screenLockedByUser = null;
        Map<String, Object> redisData =redisRepo.getHashKeyData(screenLockRedisKey, patientId);
        if(redisData != null) {
          isScreenLocked = true;
          screenLockedByUser = (String) redisData.get("user_id");
          if (screenLockedByUser.equals(userName)) {
            MultiUserService multiUserService = ApplicationContextProvider.getBean(MultiUserService.class);
            Map<String, Object> deleteLockResp = multiUserService.removeScreenLock(screenId, patientId);
            isScreenLocked = (boolean) deleteLockResp.get("is_screen_locked");
            screenLockedByUser=null;
          }
        }
		JSONSerializer js = new JSONSerializer().exclude("class");
		AbstractInstaForms formDAO = new OTForms();
		BasicDynaBean otform = formDAO.getComponents(request.getParameterMap());
		request.setAttribute("form", otform);
		request.setAttribute("insta_form_json", js.serialize(otform.getMap()));
		request.setAttribute("group_patient_sections",
				new FormComponentsDAO().findByKey("id", otform.get("form_id")).get("group_patient_sections"));
		request.setAttribute("section_rights", new SectionRoleRightsDAO().getAllSectionsRights(
				(Integer) request.getSession().getAttribute("roleId")));
		
		List<BasicDynaBean> sectionsDefList = new SectionsDAO().getSections((String) otform.get("sections"));
		request.setAttribute("sectionsDefMap", ConversionUtils.listBeanToMapBean(sectionsDefList, "section_id"));
		
		PatientSectionDetailsDAO psd = new PatientSectionDetailsDAO();
		request.setAttribute("section_finalize_status", 
				ConversionUtils.listBeanToMapMap(psd.getSections((String) bean.get("mr_no"), patientId, opProcId, 0, 
						(Integer) otform.get("form_id")), "section_id"));
		
		// markers of fields from all sections.
		List<BasicDynaBean> imageMarkers = new ImageMarkerDAO().getMarkers((String) otform.get("sections"));
		request.setAttribute("sectionsImageMarkers", ConversionUtils.listBeanToMapListBean(imageMarkers, "section_id"));
		
		List<BasicDynaBean> sectionsList = new SectionsDAO().listAll();
		request.setAttribute("insta_sections", sectionsList);
		request.setAttribute("insta_sections_json", js.serialize(ConversionUtils.copyListDynaBeansToMap(sectionsList)));

		GenericDAO userDAO = new GenericDAO("u_user");
		BasicDynaBean userbean = userDAO.findByKey("emp_username", userName);

		String doctor_dept = null;
        if (userbean != null) {
        	// doctor_dept is for getting phrases for chief complaints in IP and OT records based on logged in doctor
        	String doctor_id = (String)userbean.get("doctor_id");
        	if (doctor_id != null) {
        		GenericDAO doctorDAO = new GenericDAO("doctors");
        		BasicDynaBean doctorbean = doctorDAO.findByKey("doctor_id", doctor_id );
        		if (doctorbean != null) {
        			doctor_dept = (String)doctorbean.get("dept_id");
        			request.setAttribute("doctor_dept", doctorbean.get("dept_id"));
        		}
        	}
        }
        String phrase_dept = null;
        if(doctor_dept == null || doctor_dept.equals(""))
        	phrase_dept = (String) bean.get("dept_name");
        else
        	phrase_dept = doctor_dept ;

        BasicDynaBean consultBean = otDAO.findComplaint(patientId);
		if(consultBean != null) {
			request.setAttribute("consultation_bean", consultBean.getMap());
		}
		request.setAttribute("secondary_complaints", scomplaintDao.getSecondaryComplaints(patientId));
		request.setAttribute("diagnosis_details", MRDDiagnosisDAO.getAllDiagnosisDetails(patientId));
		request.setAttribute("allergies", formDAO.getAllergies((String) bean.get("mr_no"), patientId,
				opProcId, 0, (Integer) otform.get("form_id")));
		request.setAttribute("actionId", mapping.getProperty("action_id"));
		java.util.List<BasicDynaBean> phrase_suggestions = PhraseSuggestionsMasterDAO.getPhraseSuggestionsDynaList();
		request.setAttribute("phrase_suggestions_json", js.deepSerialize(
				ConversionUtils.listBeanToMapListMap(phrase_suggestions, "phrase_suggestions_category_id")));

		java.util.List<BasicDynaBean> phrase_suggestions_by_dept = PhraseSuggestionsMasterDAO.getPhraseSuggestionsByDeptDynaList(phrase_dept);
		request.setAttribute("phrase_suggestions_by_dept_json", js.deepSerialize(
				ConversionUtils.listBeanToMapListMap(phrase_suggestions_by_dept, "phrase_suggestions_category_id")));

		request.setAttribute("sys_generated_forms", js.deepSerialize(ConversionUtils.listBeanToListMap(
				new SystemGeneratedSectionsDAO().listAll())));

		request.setAttribute("sys_generated_section", ConversionUtils.listBeanToListMap(new SystemGeneratedSectionsDAO().listAll()));

		request.setAttribute("printTemplate", PrintTemplatesDAO.getTemplateNames(PrintTemplate.OtRecord.getType()));
		String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(patientCenterId);
		request.setAttribute("defaultDiagnosisCodeType", HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthority).getDiagnosis_code_type());

		String templateName = request.getParameter("printTemplate");
		String printerId = request.getParameter("printerId");
		if (templateName == null || templateName.equals(""))
			templateName = "BUILTIN_HTML";
		request.setAttribute("templateName", templateName);
		if (printerId == null || printerId.equals(""))
			request.setAttribute("printerDef", PrintConfigurationsDAO.getPatientDefaultPrintPrefs().get("printer_id"));
		else
			request.setAttribute("printerDef", printerId);
		
		HashMap<Integer ,String> regExpPatternMap = RegularExpressionDAO.getRegPatternWithExpression("E");
		request.setAttribute("regExpMapDesc", RegularExpressionDAO.getRegPatternWithExpression("D"));
		request.setAttribute("regExpPatternMap", js.serialize(regExpPatternMap));
		request.setAttribute("isScreenLocked", isScreenLocked);
		request.setAttribute("loginHandle", (String)RequestContext.getSession().getAttribute("login_handle"));
		request.setAttribute("screenLockedByUser", screenLockedByUser);
		return mapping.findForward("otrecord");

	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response ) throws SQLException ,IOException,Exception {

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		FlashScope flash = FlashScope.getScope(request);
		String patientId = request.getParameter("patient_id");
		BasicDynaBean regBean = visitDAO.findByKey("patient_id", patientId);
		String mrno = (String) regBean.get("mr_no");
		String opProcId = request.getParameter("operation_proc_id");
		Map params = request.getParameterMap();
		ArrayList errors = new ArrayList();

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean allSuccess = false;

		String error = null;
        try {
          // saving the optional components allergies, vitals, consultation notes, and physician forms
          error = new OTForms().save(con, params);
          if (error == null) {
            allSuccess = true;
            MultiUserRedisRepository redisRepo =
                ApplicationContextProvider.getBean(MultiUserRedisRepository.class);
            if (redisRepo.getHashKeyData(screenLockRedisKey, patientId) != null) {
              MultiUserService multiUserSvc =
                  ApplicationContextProvider.getBean(MultiUserService.class);
              multiUserSvc.removeScreenLock(screenId, patientId);
            } else {
              String userName = (String)request.getSession(false).getAttribute("userid");
              PushService pushSvc = (PushService) ApplicationContextProvider.getBean(PushService.class);
              Map<String, Object> payload = new HashMap<>();
              payload.put("message",
                  "The content on this page can now be modified. Please refresh the screen.");
              payload.put("user_id", userName);
              pushSvc.push("/topic/actionscreen/lock/ot_record/"+patientId, payload);
            }
          }
        } finally {
          DataBaseUtil.commitClose(con, allSuccess);
        }

		if (!errors.isEmpty()) {
			flash.put("error", "Some values had invalid format");
		} else if (!allSuccess) {
			flash.put("error", error == null ? "Transaction failed" : error);
		} else {
			Boolean isPrint = new Boolean(request.getParameter("printOtRecord"));
			if (isPrint) {
				ActionRedirect printRedirect = new ActionRedirect(mapping.findForward("printRedirect"));
				printRedirect.addParameter("patient_id", patientId);
				printRedirect.addParameter("mr_no", mrno);
				BasicDynaBean bean = OtRecordDAO.getOperation(Integer.parseInt(opProcId));
				printRedirect.addParameter("operation_proc_id", opProcId);
				printRedirect.addParameter("operation_details_id", bean.get("operation_details_id"));
				printRedirect.addParameter("printerId", request.getParameter("printerId"));
				printRedirect.addParameter("printTemplate", request.getParameter("printTemplate"));

				List<String> printURLs = new ArrayList<String>();
				printURLs.add(request.getContextPath() + printRedirect.getPath());
				request.getSession(false).setAttribute("printURLs", printURLs);
			}
			flash.put("success", "OT record saved successfully..");
		}

		redirect.addParameter("visit_id", request.getParameter("patient_id"));
		redirect.addParameter("operation_proc_id", opProcId);
		redirect.addParameter("printerId", request.getParameter("printerId"));
		redirect.addParameter("printTemplate", request.getParameter("printTemplate"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}
