package com.insta.hms.core.clinical.surgery;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.OTServices.OperationDetailsDAO;
import com.insta.hms.OTServices.OtRecord.OtRecordDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.core.prints.PrintService;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.imageretriever.DoctorConsultImageRetriever;
import com.insta.hms.instaforms.AbstractInstaForms;
import com.insta.hms.instaforms.OTForms;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;
import com.insta.hms.master.CommonPrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.outpatient.SecondaryComplaintDAO;
import freemarker.template.Template;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class OTRecordPrintService {
  
  private static final Logger logger = LoggerFactory.getLogger(OTRecordPrintService.class);
  
  private static final SecondaryComplaintDAO secondaryComplaintDAO = new SecondaryComplaintDAO();
  
  private static final OtRecordDAO otRecordDAO = new OtRecordDAO();
  
  /**
   * This function gets bytes array of report.
   *
   * @param patientId            visitId of patient
   * @param mrNo                 mr Number of patient
   * @param printerIdStr         printer Id from settings/preferences
   * @param operationProcedureId operationProcedureOd
   * @param printTemplateName    printTemplateName
   * @param returnType           what type to return PDF/PDF Bytes/Text Bytes
   * @return byteArray
   * @throws SQLException when trying to query throws exeception
   */
  public byte[] getOTRecordReport(String patientId, String mrNo, String printerIdStr,
      String operationProcedureId, String printTemplateName,
      PrintService.ReturnType returnType) throws SQLException {
    
    byte[] reportContentBytes = null;
    int opDetailsId = 0;
    
    BasicDynaBean operationBean = null;
    
    if (!StringUtils.isEmpty(operationProcedureId)) {
      operationBean = OtRecordDAO.getOperation(Integer.parseInt(operationProcedureId));
    }
    
    if (operationBean != null) {
      opDetailsId = (Integer) operationBean.get("operation_details_id");
    }
    
    int printerId = 0;
    if (!StringUtils.isEmpty(printerIdStr)) {
      printerId = Integer.parseInt(printerIdStr);
    }
    
    try {
      BasicDynaBean prefs =
          PrintConfigurationsDAO.getPageOptions(
            PrintConfigurationsDAO.PRINT_TYPE_PATIENT, printerId);
      
      String printMode = "P";
      if (prefs.get("print_mode") != null) {
        printMode = (String) prefs.get("print_mode");
      }
      Map patientDetails = new HashMap();
      GenericDocumentsFields.copyPatientDetails(patientDetails, mrNo, patientId, false);
      Map ftlParamMap = new HashMap();
      ftlParamMap.put("visitdetails", patientDetails);
      ftlParamMap.put("modules_activated",
          ((Preferences) RequestContext.getSession().getAttribute("preferences"))
            .getModulesActivatedMap());
      
      ftlParamMap
        .put("secondary_complaints", secondaryComplaintDAO.getSecondaryComplaints(patientId));
      ftlParamMap.put("diagnosis_details", MRDDiagnosisDAO.getAllDiagnosisDetails(patientId));
      
      ftlParamMap.put("surgery_details", OperationDetailsDAO.getSurgeryDetailsForFTL(opDetailsId));
      ftlParamMap.put("operation_team_details", OperationDetailsDAO.getOperationTeam(opDetailsId));
      ftlParamMap.put("operation_anaethesia_details",
          OperationDetailsDAO.getOperationAnaesthesiaDetails(opDetailsId));
      ftlParamMap.put("opeartionsList", OperationDetailsDAO.getSurgeryListForFTL(opDetailsId));
      
      List<BasicDynaBean> operationsProcIdsList = otRecordDAO.getOperations(patientId, opDetailsId);
      List<BasicDynaBean> opCompDetails = new ArrayList<>();
      Map operationWiseCompValues = new HashMap<String, Map<Object, List<List>>>();
      Map<Integer, Integer> sysCompDetails = new HashMap<>();
      Map<String, String> operationNames = new HashMap<>();
      BasicDynaBean components;
      AbstractInstaForms formDAO = new OTForms();
      PatientSectionDetailsDAO psdDAO = new PatientSectionDetailsDAO();
      String itemType = (String) formDAO.getKeys().get("item_type");
      for (BasicDynaBean operationIDS : operationsProcIdsList) {
        int operationProcId = (Integer) operationIDS.get("operation_proc_id");
  
        Map params = new HashMap();
        params.put("operation_proc_id", new String[] {operationProcId + ""});
        components = formDAO.getComponents(params);
  
        opCompDetails.add(components);
        String sectionIds = (String) components.get("sections");
        String[] sectionIdsArray = sectionIds.split(",");
        for (int i = 0; i < sectionIdsArray.length; i++) {
          int sectionId = Integer.parseInt(sectionIdsArray[i]);
          if (sectionId < 0) {
            sysCompDetails.put(sectionId, sectionId);
          }
        }
        List<BasicDynaBean> sectionValues = psdDAO
            .getAllSectionDetails((String) patientDetails.get("mr_no"),
              (String) patientDetails.get("patient_id"),
              operationProcId, 0, (Integer) components.get("form_id"), itemType);
  
        String operationName = (String) operationIDS.get("operation_name");
        Map<Object, List<List>> map = ConversionUtils
            .listBeanToMapListListBean(sectionValues, "str_section_detail_id", "field_id");
        
        operationWiseCompValues.put(operationProcId + "", map);
        operationNames.put((Integer.valueOf(operationProcId)).toString(), operationName);
      }
      
      ftlParamMap.put("ot_record_components", opCompDetails);
      ftlParamMap.put("opCompSectionValues", operationWiseCompValues);
      ftlParamMap.put("system_components", sysCompDetails);
      ftlParamMap.put("operationNames", operationNames);
      
      if (!StringUtils.isEmpty(printTemplateName)) {
        printTemplateName = "BUILTIN_HTML";
      }
      Template template = null;
      String templateMode = null;
      if (printTemplateName.equals("BUILTIN_HTML")) {
        template = Objects.requireNonNull(AppInit.getFmConfig()).getTemplate("OtRecordDetails.ftl");
        templateMode = "H";
      } else if (printTemplateName.equals("BUILTIN_TEXT")) {
        template = Objects.requireNonNull(
          AppInit.getFmConfig()).getTemplate("OtRecordDetailsText.ftl");
        templateMode = "T";
      } else {
        BasicDynaBean pbean = PrintTemplatesDAO.getTemplateContent(printTemplateName);
        if (pbean == null) {
          return null;
        }
        String templateContent = (String) pbean.get("template_content");
        templateMode = (String) pbean.get("template_mode");
        StringReader reader = new StringReader(templateContent);
        template = new Template("OtRecordPrint.ftl", reader, AppInit.getFmConfig());
      }
      StringWriter writer = new StringWriter();
      template.process(ftlParamMap, writer);
      HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
      logger.debug("OT Record  Template Content : " + writer.toString());
      boolean repeatPHeader = prefs.get("repeat_patient_info").equals("Y");
      if (printMode.equals("P")) {
        OutputStream os = new ByteArrayOutputStream();
        String formTitle = "OT Record Details";
        if (returnType.equals(PrintService.ReturnType.PDF)) {
          hc.writePdf(os, writer.toString(), formTitle, prefs, false, repeatPHeader, true, true,
              false, false);
        } else if (returnType.equals(PrintService.ReturnType.PDF_BYTES)) {
          reportContentBytes =
            hc.getPdfBytes(writer.toString(), formTitle, prefs, repeatPHeader, true, true, false,
                false);
        } else if (returnType.equals(PrintService.ReturnType.TEXT_BYTES)) {
          reportContentBytes = hc.getText(writer.toString(), formTitle, prefs, true, true);
        }
        os.close();
        
      } else {
        if (templateMode != null && templateMode.equals("T")) {
          reportContentBytes = writer.toString().getBytes();
        } else {
          reportContentBytes =
            hc.getText(writer.toString(), "OT Record Details", prefs, true, true);
        }
      }
    } catch (Exception ex) {
      logger.error("Exception in getting OT Record Report ", ex);
    }
    return reportContentBytes;
  }

  /**
   * Get OT form data byte array.
   * @param patientId the patient id.
   * @param mrNo the mr no.
   * @param printerIdStr printer id  string.
   * @param operationProcedureId operation proceduce id.
   * @param printTemplateName print template name.
   * @param returnType return type.
   * @return Base 64 encoded pdf byte array.
   */
  public byte[] getFormDataEncodedByteArray(Object patientId, Object mrNo, Object printerIdStr,
      Object operationProcedureId, Object printTemplateName,
      PrintService.ReturnType returnType) {
    try {
      return Base64.encode(
          getOTRecordReport((String) patientId, (String) mrNo, (String) printerIdStr,
              (String) operationProcedureId, (String) printTemplateName, returnType));
    } catch (SQLException sqlEx) {
      logger.error("Exception while encoding OT Form data", sqlEx);
    }
    return new byte[0];
  }
}
