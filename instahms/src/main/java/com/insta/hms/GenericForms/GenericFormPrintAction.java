/**
 *
 */
package com.insta.hms.GenericForms;

import com.bob.hms.common.APIUtility;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.PreferencesDao;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.imageretriever.DoctorConsultImageRetriever;
import com.insta.hms.instaforms.AbstractInstaForms;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;
import com.insta.hms.master.CommonPrintTemplates.PrintTemplate;
import com.insta.hms.master.CommonPrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.FormComponents.FormComponentsDAO;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplate;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.Sections.SectionsDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.outpatient.AllergiesDAO;
import com.insta.hms.outpatient.AntenatalDAO;
import com.insta.hms.outpatient.HealthMaintenanceDAO;
import com.insta.hms.outpatient.ObstetricRecordDAO;
import com.insta.hms.outpatient.PreAnaesthestheticDAO;
import com.insta.hms.outpatient.PregnancyHistoryDAO;
import com.insta.hms.outpatient.SecondaryComplaintDAO;
import com.insta.hms.vitalForm.genericVitalFormDAO;
import com.insta.hms.vitalparameter.VitalMasterDAO;
import com.insta.instaapi.common.JsonProcessor;
import com.insta.instaapi.common.ServletContextUtil;

import flexjson.JSONSerializer;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna
 *
 */
public class GenericFormPrintAction extends DispatchAction {

	static Logger log = LoggerFactory.getLogger(GenericFormPrintAction.class);
	private static final JSONSerializer js = JsonProcessor.getJSONParser();

	public ActionForward print(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, Exception {
		Map params = new HashMap(request.getParameterMap());
		
		String reqHandlerKey = request.getParameter("request_handler_key");
    String error =
        APIUtility.setConnectionDetails(servlet.getServletContext(),
            reqHandlerKey);
    if (error != null) {
      APIUtility.setInvalidLoginError(response, error);
      return null;
    }
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    Map<String,Object> sessionParameters = null;
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map<String,Object>) sessionMap.get(reqHandlerKey);
    }
 
    
		int genericFormId = Integer.parseInt(request.getParameter("generic_form_id"));
		int insta_form_id = Integer.parseInt(request.getParameter("insta_form_id"));
		
		 BasicDynaBean fcBean = new FormComponentsDAO().findByKey("id", insta_form_id);
		int printTemplateId = 0;
		if (null!= fcBean) {
			printTemplateId = (Integer) ((null == fcBean.get("print_template_id")) ? 0 : fcBean.get("print_template_id"));
		}
		
		AbstractInstaForms formDAO = AbstractInstaForms.getInstance("Form_Gen");
		PatientSectionDetailsDAO psdDAO = new PatientSectionDetailsDAO();

		BasicDynaBean bean = psdDAO.findByKey("generic_form_id", genericFormId);
		String patientId = (String) bean.get("patient_id");

		PrintTemplate printTemplate = PrintTemplate.InstaGenericForm;
		String temp_username = "", finalized_user = "";
		boolean isSameFinalizedUser = false;
		int count = 0;
		
		List<BasicDynaBean> genFormList = psdDAO.getGenericFormList(patientId, genericFormId, "GEN");
		if (genFormList != null && genFormList.size()>0){
			finalized_user = null != (String) genFormList.get(0).get("finalized_user") ? (String) genFormList.get(0).get("finalized_user") :"";
			temp_username = null != (String) genFormList.get(0).get("temp_username") ? (String) genFormList.get(0).get("temp_username") :"";
			for (BasicDynaBean bdBean : genFormList) {
				if(finalized_user.equalsIgnoreCase(null != (String)bdBean.get("finalized_user") ? (String)bdBean.get("finalized_user") : "") ){
					count++;
				}
			}
		}
		isSameFinalizedUser = genFormList == null || genFormList.size() == count; 
		
		BasicDynaBean pbean = PrintTemplatesDAO.getTemplateContent(printTemplateId);
		Template t = null;
		String templateMode = null;
		if (pbean == null) {
			t = AppInit.getFmConfig().getTemplate(printTemplate.getHmFtlName()+".ftl");
			templateMode = "H";
		} else {
			
			String templateContent =(String) pbean.get("template_content");
			templateMode = (String)pbean.get("template_mode");
			StringReader reader = new StringReader(templateContent);
			t = new Template("GenericFormPrintTemplate.ftl", reader, AppInit.getFmConfig());
		}
		
		PatientHeaderTemplateDAO phTemplateDao = new PatientHeaderTemplateDAO();
		String patientHeader = (String) phTemplateDao.getPatientHeader(
				pbean == null ? null : (Integer) pbean.get("pheader_template_id"), PatientHeaderTemplate.INSTA_GENERIC_FORM.getType());
		StringReader reader = new StringReader(patientHeader);
		Template pt = new Template("PatientHeader.ftl", reader, AppInit.getFmConfig());

		Map pDetails = new HashMap();
		pDetails.put("temp_username", temp_username);
		pDetails.put("finalized_user", finalized_user);
		pDetails.put("isSameFinalizedUser", isSameFinalizedUser);
		Map modulesActivatedMap = new HashMap();
		GenericDocumentsFields.copyPatientDetails(pDetails, null, patientId,false);
		
		boolean isPatientLogin = sessionParameters != null 
        && ((boolean) sessionParameters.get("patient_login"));
    if (isPatientLogin) {
      String mrNo = (String) sessionParameters.get("customer_user_id");
      if (!((String)pDetails.get("mr_no")).equals(mrNo)) {
        String successMsg = "Invalid input parameters supplied for prescription_id";
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("return_code", "1021");
        errorMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(errorMap));
        response.flushBuffer();
        return null;
      }
    }
		 if (reqHandlerKey != null && !reqHandlerKey.equals("")) {
	      Preferences preferences = null;
	      Connection con = DataBaseUtil.getConnection();
	      PreferencesDao dao = new PreferencesDao(con);
	      preferences = dao.getPreferences();
	      Map groups = preferences.getModulesActivatedMap();
	      modulesActivatedMap.put("modules_activated", groups);
	      if (con != null) {
	        con.close();
	      }
	    } else {
	       modulesActivatedMap = ((Preferences) RequestContext.getSession().getAttribute("preferences")).getModulesActivatedMap();
	    }
		Map templateMap = new HashMap();
		templateMap.put("visitdetails", pDetails);
		templateMap.put("modules_activated", modulesActivatedMap);
		StringWriter pwriter = new StringWriter();

		try {
			pt.process(templateMap, pwriter);
		} catch (TemplateException te) {
			log.error("", te);
			throw te;
		}
		Map ftlParamMap = new HashMap();
		ftlParamMap.put("visitdetails", pDetails);
		ftlParamMap.put("modules_activated", modulesActivatedMap);

		BasicDynaBean compBean = formDAO.getComponents(params);
		String itemType = (String) formDAO.getKeys().get("item_type");
		List<BasicDynaBean> consValues = psdDAO.getAllSectionDetails((String) pDetails.get("mr_no"),
				patientId, 0, genericFormId, (Integer) compBean.get("form_id"), itemType);

		Map<Object, List<List>> map = ConversionUtils.listBeanToMapListListBean(consValues, "str_section_detail_id", "field_id");
		ftlParamMap.put("insta_sections_data", map);
		ftlParamMap.put("form", compBean);
		ftlParamMap.put("insta_sections", SectionsDAO.getAddedSectionMasterDetails((String) pDetails.get("mr_no"),
				patientId, 0, genericFormId, (Integer) compBean.get("form_id"), itemType));

		ftlParamMap.put("secondary_complaints", new SecondaryComplaintDAO().getSecondaryComplaints(patientId));
		ftlParamMap.put("diagnosis_details", MRDDiagnosisDAO.getAllDiagnosisDetails(patientId));
		ftlParamMap.put("allergies", AllergiesDAO.getAllActiveAllergies((String) pDetails.get("mr_no"),
				patientId, 0, genericFormId, (Integer) compBean.get("form_id"), itemType));
		ftlParamMap.put("pac_details", PreAnaesthestheticDAO.getAllPACRecords((String) pDetails.get("mr_no"),
				patientId, 0, genericFormId, (Integer) compBean.get("form_id"), itemType));
		ftlParamMap.put("health_maintenance", HealthMaintenanceDAO.getAllHealthMaintenance((String) pDetails.get("mr_no"),
				patientId, 0, genericFormId, (Integer) compBean.get("form_id"), itemType));
		ftlParamMap.put("pregnancyhistories", PregnancyHistoryDAO.getAllPregnancyDetails((String) pDetails.get("mr_no"),
				patientId, 0, genericFormId, (Integer) compBean.get("form_id"), itemType));
		ftlParamMap.put("pregnancyhistoriesBean", ObstetricRecordDAO.getAllObstetricHeadDetails((String) pDetails.get("mr_no"),
				patientId, 0, genericFormId, (Integer) compBean.get("form_id"), itemType));
		ftlParamMap.put("antenatalinfo", AntenatalDAO.getAllAntenatalDetails((String) pDetails.get("mr_no"),
				patientId, 0, genericFormId, (Integer) compBean.get("form_id"), itemType));
		
		VitalMasterDAO vmDAO = new VitalMasterDAO();
		ftlParamMap.put("vital_params", vmDAO.getUniqueVitalsforPatient(patientId));
		ftlParamMap.put("vitals", genericVitalFormDAO.groupByReadingId(patientId, "V"));


		StringWriter writer = new StringWriter();
		t.process(ftlParamMap, writer);

		String printerId = request.getParameter("printerId");
		BasicDynaBean prefs;
		if ((printerId != null) && !printerId.equals("")) {
			prefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
				Integer.parseInt(request.getParameter("printerId")));
		} else {
			// use the default printer
			prefs = PrintConfigurationsDAO.getPatientDefaultPrintPrefs();
		}


		String printMode = "P";
		String forcePdf = request.getParameter("forcePdf");
		if ( (forcePdf == null) || !forcePdf.equals("true")) {
			// use the print mode selected.
			printMode = (String) prefs.get("print_mode");
		}

		HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
		Boolean repeatPHeader = ((String)prefs.get("repeat_patient_info")).equals("Y");

		StringBuilder documentContent = new StringBuilder();
		documentContent.append(pwriter.toString());
		documentContent.append(writer.toString());

		if (printMode.equals("P")) {
			response.setContentType("application/pdf");
			OutputStream os = response.getOutputStream();
			
			if (templateMode != null && templateMode.equals("T")) {
				hc.textToPDF(documentContent.toString(), os,  prefs);
			} else {
				hc.writePdf(os, documentContent.toString(), "Patient Generic Insta Form", prefs, false, repeatPHeader, true, true,
						false, false);
			}
			os.close();

		} else {
			String textReport = null;
			if (templateMode != null && templateMode.equals("T")) {
				textReport = new String(documentContent.toString().getBytes());
			} else {
				textReport = new String(hc.getText(documentContent.toString(), "Patient Generic Insta Form",
						prefs, true, true));
			}
			request.setAttribute("textReport", textReport);
			request.setAttribute("textColumns", prefs.get("text_mode_column"));
			request.setAttribute("printerType", "DMP");
			return mapping.findForward("textPrintApplet");
		}
		return null;
	}

}
