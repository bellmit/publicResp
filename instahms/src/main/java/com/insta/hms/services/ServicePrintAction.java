package com.insta.hms.services;

import com.bob.hms.common.APIUtility;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.PreferencesDao;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.imageretriever.DoctorConsultImageRetriever;
import com.insta.hms.instaforms.AbstractInstaForms;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;
import com.insta.hms.master.FormComponents.FormComponentsDAO;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplate;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.Sections.SectionsDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.outpatient.AllergiesDAO;
import com.insta.hms.outpatient.PhysicianFormValuesDAO;
import com.insta.hms.outpatient.SecondaryComplaintDAO;
import com.insta.hms.vitalForm.genericVitalFormDAO;
import com.insta.hms.vitalparameter.VitalMasterDAO;
import com.insta.instaapi.common.JsonProcessor;
import com.insta.instaapi.common.ServletContextUtil;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

// TODO: Auto-generated Javadoc
/**
 * The Class ServicePrintAction.
 *
 * @author amol.b
 */
public class ServicePrintAction extends DispatchAction {

  /** The cons comp dao. */
  FormComponentsDAO consCompDao = new FormComponentsDAO();

  /** The pfv DAO. */
  PhysicianFormValuesDAO pfvDAO = new PhysicianFormValuesDAO();

  /** The gdao. */
  GenericDAO gdao = new GenericDAO("patient_registration");

  /** The ph template DAO. */
  PatientHeaderTemplateDAO phTemplateDAO = new PatientHeaderTemplateDAO();
  
  /** The vm DAO. */
  VitalMasterDAO vmDAO = new VitalMasterDAO();
  
  /** The pt DAO. */
  com.insta.hms.master.PrintTemplates.PrintTemplatesDAO ptDAO = 
      new com.insta.hms.master.PrintTemplates.PrintTemplatesDAO();

  private static final JSONSerializer js = JsonProcessor.getJSONParser();

  /**
   * Prints the.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   * @throws DocumentException the document exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException the transformer exception
   * @throws TemplateException the template exception
   * @throws Exception the exception
   */
  public ActionForward print(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response)
          throws SQLException, ServletException, IOException, ParseException, 
          DocumentException,
          XPathExpressionException, TransformerException, TemplateException, Exception {

    String reqHandlerKey = request.getParameter("request_handler_key");
    
    String error = APIUtility.setConnectionDetails(servlet.getServletContext(),
        reqHandlerKey);
    if (error != null) {
      APIUtility.setInvalidLoginError(response, error);
      return null;
    }
   
    String printerIdStr = request.getParameter("printerId");
    BasicDynaBean prefs = null;
    int printerId = 0;
    String printTemplateType = null;
    printTemplateType = PatientHeaderTemplate.Ser.getType();
    if ((printerIdStr != null) && !printerIdStr.equals("")) {
      printerId = Integer.parseInt(printerIdStr);
    }
    prefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_SERVICE,
        printerId);
    String prescribedId = request.getParameter("prescription_id");
    BasicDynaBean serviceBean = ServicesDAO.getServiceDetails(Integer.parseInt(prescribedId));
    String serviceId = (String) serviceBean.get("service_id");

    String patientId = (String) serviceBean.get("patient_id");
    BasicDynaBean deptBean = gdao.findByKey("patient_id", patientId);

    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    Map<String,Object> sessionParameters = null;
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map<String,Object>) sessionMap.get(reqHandlerKey);
    }

    boolean isPatientLogin = sessionParameters != null 
        && ((boolean) sessionParameters.get("patient_login"));
    if (isPatientLogin) {
      String mrNo = (String) sessionParameters.get("customer_user_id");
      if (!((String)deptBean.get("mr_no")).equals(mrNo)) {
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

 
    String printMode = "P";
    if (prefs.get("print_mode") != null) {
      printMode = (String) prefs.get("print_mode");
    }

    // api parameter
    String logoHeader = request.getParameter("logoHeader");
    if (logoHeader != null && !logoHeader.equals("")
        && (logoHeader.equalsIgnoreCase("Y") || logoHeader.equalsIgnoreCase("L")
            || logoHeader.equalsIgnoreCase("H") || logoHeader.equalsIgnoreCase("N"))) {
      prefs.set("logo_header", logoHeader.toUpperCase());
    }

    Map patDetails = new HashMap();
    String mrNo = request.getParameter("mr_no");
    GenericDocumentsFields.copyPatientDetails(patDetails, mrNo, patientId, false);
    Map ftlParamMap = new HashMap();
    ftlParamMap.put("visitdetails", patDetails);
    
    if (reqHandlerKey != null && !reqHandlerKey.equals("")) {
      Preferences preferences = null;
      Connection con = DataBaseUtil.getConnection();
      PreferencesDao dao = new PreferencesDao(con);
      preferences = dao.getPreferences();
      Map groups = preferences.getModulesActivatedMap();
      ftlParamMap.put("modules_activated", groups);
      if (con != null) {
        con.close();
      }
    } else {
      ftlParamMap.put("modules_activated",
          ((Preferences) RequestContext.getSession().getAttribute("preferences"))
              .getModulesActivatedMap());
    }
    ftlParamMap.put("serviceDetails", serviceBean);

    PatientSectionDetailsDAO psdDAO = new PatientSectionDetailsDAO();
    AbstractInstaForms formDAO = AbstractInstaForms.getInstance("Form_Serv");
    String itemType = (String) formDAO.getKeys().get("item_type");
    Map params = new HashMap();
    params.put("prescription_id", new String[] { prescribedId + "" });
    BasicDynaBean compBean = formDAO.getComponents(params);

    List<BasicDynaBean> instaFormValues = psdDAO.getAllSectionDetails(
        (String) patDetails.get("mr_no"), patientId, Integer.parseInt(prescribedId), 0,
        (Integer) compBean.get("form_id"), itemType);
    Map<Object, List<List>> map = ConversionUtils.listBeanToMapListListBean(instaFormValues,
        "str_section_detail_id", "field_id");

    ftlParamMap.put("PhysicianForms", map);
    ftlParamMap.put("diagnosis_details", MRDDiagnosisDAO.getAllDiagnosisDetails(patientId));
    ftlParamMap.put("allergies",
        AllergiesDAO.getAllActiveAllergies((String) patDetails.get("mr_no"),
            (String) patDetails.get("patient_id"), Integer.parseInt(prescribedId), 0,
            (Integer) compBean.get("form_id"), itemType));

    ftlParamMap.put("vital_params", vmDAO.getActiveVitalParams("O"));
    ftlParamMap.put("vitals", genericVitalFormDAO.groupByReadingId(patientId, "V"));

    String compAllergies = "N";
    String compVitals = "N";
    String consultationNotes = "N";
    String compComplaint = "N";

    ftlParamMap.put("service_components", compBean);
    ftlParamMap.put("insta_sections",
        SectionsDAO.getAddedSectionMasterDetails((String) patDetails.get("mr_no"),
            (String) patDetails.get("patient_id"), Integer.parseInt(prescribedId), 0,
            (Integer) compBean.get("form_id"), itemType));

    ftlParamMap.put("secondary_complaints",
        new SecondaryComplaintDAO().getSecondaryComplaints(patientId));

    Integer pheaderTemplateId = (Integer) ptDAO.getPatientHeaderTemplateId(PrintTemplate.Ser);
    String patientHeader = phTemplateDAO.getPatientHeader(pheaderTemplateId, printTemplateType);
    FtlReportGenerator ftlGen = new FtlReportGenerator("PatientHeader", 
        new StringReader(patientHeader));
    Map ftlParams = new HashMap();
    ftlParams.put("visitdetails", patDetails);
    StringWriter writers = new StringWriter();
    try {
      ftlGen.setReportParams(ftlParams);
      ftlGen.process(writers);
    } catch (TemplateException te) {
      throw te;
    }

    PrintTemplate template = PrintTemplate.Ser;
    String templateContent = ptDAO.getCustomizedTemplate(template);
    Template temp = null;
    if (templateContent == null || templateContent.equals("")) {
      temp = AppInit.getFmConfig().getTemplate(template.getFtlName() + ".ftl");
    } else {
      StringReader reader = new StringReader(templateContent);
      temp = new Template(null, reader, AppInit.getFmConfig());
    }
    StringWriter writer = new StringWriter();
    temp.process(ftlParamMap, writer);
    StringBuilder printContent = new StringBuilder();
    printContent.append(writers.toString()).append(writer.toString());
    HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
    Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");

    if (printMode.equals("P")) {
      response.setContentType("application/pdf");
      OutputStream os = response.getOutputStream();
      hc.writePdf(os, printContent.toString(), "Services Forms Details", prefs, false,
          repeatPHeader, true, true, false, false);
      os.close();
    } else {
      String textReport = null;
      textReport = new String(
          hc.getText(writer.toString(), "Services Forms Details", prefs, true, true));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", prefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");
    }
    return null;
  }

}
