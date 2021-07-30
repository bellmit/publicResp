package com.insta.hms.vitalForm;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.usermanager.User;
import com.insta.hms.usermanager.UserDAO;
import com.insta.hms.vitalparameter.VitalMasterDAO;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;



public class genericVitalFormAction extends BaseAction {

	static Logger log = LoggerFactory.getLogger(genericVitalFormAction.class);

	static VitalMasterDAO vmDAO = new VitalMasterDAO();
	static VisitVitalsDAO vvDAO = new VisitVitalsDAO();
	static UserDAO userDao = new UserDAO();
	static DoctorConsultationDAO consultDao = new DoctorConsultationDAO();

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, ServletException, IOException, ParseException {
		JSONSerializer js = new JSONSerializer().exclude("class");
		String visit = null;
		String mrNo = null;
		String deptId = null;
		String patientId = request.getParameter("patient_id");
		String paramType = mapping.getProperty("param_type");
		BasicDynaBean patBean = new GenericDAO("patient_registration").findByKey("patient_id",patientId);
		Map patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap(patientId);
		int centerId = RequestContext.getCenterId();
		if(patBean != null) {		  
		  deptId = (String) patBean.get("dept_name");
			visit =  ((String)patBean.get("visit_type")).toUpperCase();
			mrNo = (String) patBean.get("mr_no");
		}
		if(paramType.equals("V")) {
		  request.setAttribute("all_fields", vmDAO.getAllVitalParams(deptId,centerId,visit));
		} else {
		  request.setAttribute("all_fields", vmDAO.getAllParams(paramType,visit));
		}
		
		request.setAttribute("vital_readings", vvDAO.getVitals(request.getParameter("patient_id"),
				request.getParameter("vifromDate"), request.getParameter("vitoDate"), paramType));
		// check the readings of the patient irrespective of date filter.
		request.setAttribute("vital_reading_exists", vvDAO.readingsExist(request.getParameter("patient_id"), paramType));
		request.setAttribute("latest_vital_reading_json", 
				js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
						vvDAO.getLatestVitalsForVisit(mrNo, patientId, visit))));
		
		request.setAttribute("height_weight_params", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
				vvDAO.getHeightAndWeight(mrNo, patientId))));
		String consId = request.getParameter("consultation_id");
		User user = userDao.getUser((String) request.getSession(false).getAttribute("userid"));
		if (consId != null && !consId.equals("")) {
			BasicDynaBean consultBean = consultDao.findConsultationExt(Integer.parseInt(consId));
			request.setAttribute("consultation_bean", consultBean.getMap());
		}
		if(user != null) {
			request.setAttribute("isSharedLogIn", user.getSharedLogin());
			request.setAttribute("roleId", user.getRoleId());
		}
		BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
		String consultationEditAcrossDoctorsOp = (String) ApplicationContextProvider.getBean(ClinicalPreferencesService.class).getClinicalPreferences().get("op_consultation_edit_across_doctors");
		request.setAttribute("doctor_logged_in", user == null ? "" : user.getDoctorId());
		request.setAttribute("actionId", mapping.getProperty("action_id"));
		request.setAttribute("paramType", paramType);
		request.setAttribute("referenceList", genericVitalFormDAO.getReferenceRange(patientDetails));
		request.setAttribute("prefColorCodes", genericPrefs);
		request.setAttribute("consultationEditAcrossDoctorsOp", consultationEditAcrossDoctorsOp);
		return mapping.findForward("addshow");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, ParseException, SQLException,
			Exception {
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		String patientId = request.getParameter("patient_id");
		HttpSession session = request.getSession(false);
		String userName = (String) session.getAttribute("userId");
		String remoteAddr = request.getRemoteAddr();
		String actionId = (String)mapping.getProperty("action_id");
		String consultationIdStr = request.getParameter("consultation_id");
		String vifromDate = request.getParameter("vifromDate");
		String vitoDate = request.getParameter("vitoDate");
		FlashScope flash = FlashScope.getScope(request);
		String error = null;

		boolean flag = true;
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		try {
			if(request.getParameter("isSharedLogIn").equals("Y")){
	        	userName = request.getParameter("authUser");
	        }
			flag = new VitalsBO().updateVitals(con, request.getParameterMap(), userName);
		} finally {
			DataBaseUtil.commitClose(con, flag);
		}

		if (flag) {
			if (new Boolean(request.getParameter("printVitals"))) {
				ActionRedirect printRedirect = new ActionRedirect(mapping.findForward("printRedirect"));
				printRedirect.addParameter("visitId", patientId);

				List<String> printURLs = new ArrayList<String>();
				printURLs.add(request.getContextPath() + printRedirect.getPath());
				request.getSession(false).setAttribute("printURLs", printURLs);
			}
		} else {
			flash.put("error", "Transaction Failed");
		}
		flash.put("error", error);
		redirect.addParameter("patient_id", patientId);
		redirect.addParameter("consultation_id", consultationIdStr);
		redirect.addParameter("vifromDate", vifromDate);
		redirect.addParameter("vitoDate", vitoDate);
		return redirect;
	}

	public ActionForward generateReport(ActionMapping mapping,ActionForm form,HttpServletRequest request,
				HttpServletResponse response) throws SQLException, DocumentException, TemplateException,
				IOException, XPathExpressionException, TransformerException{

		 	String visitId = request.getParameter("visitId");
		 	String paramType = mapping.getProperty("param_type");
		 	String printerIdStr = request.getParameter("printerId");
		 	int printerId = 0;
		 	if (printerIdStr != null && !printerIdStr.equals("")) {
		 		printerId = Integer.parseInt(printerIdStr);
		 	}
		 	BasicDynaBean pref =  PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT, printerId);
			String printMode = "P";
			if (pref.get("print_mode") != null) {
				printMode = (String) pref.get("print_mode");
			}

			GenericVitalFormFtlHelper ftlHelper = new GenericVitalFormFtlHelper(AppInit.getFmConfig());

			if (printMode.equals("P")) {
				response.setContentType("application/pdf");
				OutputStream os = response.getOutputStream();
				if (paramType != null && paramType.equals("V"))
					ftlHelper.getVitalParameterReport(visitId, paramType,  GenericVitalFormFtlHelper.return_type.PDF, pref, os);
				else
					ftlHelper.getIntakeOutputParameterReport(visitId, paramType,  GenericVitalFormFtlHelper.return_type.PDF, pref, os);
				os.close();
			} else {
				String textReport = null;
				if(paramType != null && paramType.equals("V")) {
					textReport = new String(ftlHelper.getVitalParameterReport(visitId,
						paramType, GenericVitalFormFtlHelper.return_type.TEXT_BYTES, pref, null));
				} else {
					textReport = new String(ftlHelper.getIntakeOutputParameterReport(visitId,
							paramType, GenericVitalFormFtlHelper.return_type.TEXT_BYTES, pref, null));
				}
				request.setAttribute("textReport", textReport);
				request.setAttribute("textColumns", pref.get("text_mode_column"));
				request.setAttribute("printerType", "DMP");
				return mapping.findForward("textPrintApplet");
			}
			return null;
	}
}
