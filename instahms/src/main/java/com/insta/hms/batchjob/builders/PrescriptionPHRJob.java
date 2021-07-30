package com.insta.hms.batchjob.builders;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.genericdocuments.DocumentPrintConfigurationsDAO;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrescriptionsPrintTemplates.PrescriptionsTemplateDAO;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.outpatient.OPPrescriptionFtlHelper;
import com.insta.hms.outpatient.PatientPrescriptionDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class PrescriptionPHRJob extends GenericJob {
  private static Logger logger = LoggerFactory.getLogger(PrescriptionPHRJob.class);
  private String params;
  private String path;

  public String getParams() {
    return params;
  }

  public void setParams(String params) {
    this.params = params;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    String schema = getSchema();
    String userName = "InstaAdmin";
    String parameters = getParams();
    String[] param = parameters.split(";");
    String consIdStr = param[0];
    consIdStr = consIdStr.substring(consIdStr.lastIndexOf('_') + 1).trim();
    int centerId = Integer.parseInt(param[1]);
    RequestContext.setConnectionDetails(new String[] { null, null, schema, "_system", param[1] });
    try {
      // if template is Web Based Prescription Template with blank content, copy the content from
      // WebPrescription.ftl
      String templateContent = null;
      String templateName =
          (String) GenericPreferencesDAO.getAllPrefs().get("default_prescription_web_template");
      if (templateName.equals("Web Based Prescription Template")) {
        BasicDynaBean pbean = PrescriptionsTemplateDAO.getTemplateContent(templateName);
        if (pbean == null) {
          return;
        }
        templateContent = (String) pbean.get("prescription_template_content");
        GenericDAO templateDao = new GenericDAO("prescription_print_template");
        if (templateContent == null || (templateContent).trim().isEmpty()) {
          String builtinTemplateName = "WebPrescription";
          Connection con2 = null;
          int success = 0;
          FileInputStream fis = null;
          try {
            BasicDynaBean bean = templateDao.getBean();
            con2 = DataBaseUtil.getConnection();
            con2.setAutoCommit(false);
            bean.set("template_name", templateName);
            String realPath = getPath();
            fis = new FileInputStream(
                new File(realPath + "/WEB-INF/templates/" + builtinTemplateName + ".ftl"));
            bean.set("prescription_template_content",
                new String(DataBaseUtil.readInputStream(fis)));
            Map<String, String> additionalDetailsKeys = new HashMap<>();
            additionalDetailsKeys.put("template_name", templateName);
            success = templateDao.update(con2, bean.getMap(), additionalDetailsKeys);
          } finally {
            DataBaseUtil.commitClose(con2, success > 0);
          }
        }
      }
      int consId = Integer.parseInt(consIdStr);
      PatientPrescriptionDAO patPrescDAO = new PatientPrescriptionDAO();
      BasicDynaBean consultBean = patPrescDAO.findByKey("consultation_id", consId);
      if (consultBean != null) {
        OutputStream webos = null;
        String webPrinterId = String
            .valueOf(GenericPreferencesDAO.getAllPrefs().get("default_prescription_web_printer"));
        String webTemplateName =
            (String) GenericPreferencesDAO.getAllPrefs().get("default_prescription_web_template");
        OPPrescriptionFtlHelper.DefaultType templateType =
            OPPrescriptionFtlHelper.DefaultType.PRESCRIPTION;
        BasicDynaBean webPrefs = DocumentPrintConfigurationsDAO.getAllPrintPreferences(
            "prescription_" + webTemplateName, centerId, Integer.parseInt(webPrinterId));
        OPPrescriptionFtlHelper ftlHelper = new OPPrescriptionFtlHelper();
        String textReport = "";
        byte[] pdfbytes = ftlHelper.getConsultationFtlReport(consId, webTemplateName,
            OPPrescriptionFtlHelper.ReturnType.PDF_BYTES, webPrefs, true, webos, userName,
            templateType);
        if (pdfbytes != null) {
          textReport = new String(pdfbytes);
        }
        Map<String, Object> prescriptionData = new HashMap<>();
        prescriptionData.put("_report_content", textReport);
        prescriptionData.put("_message_attachment", textReport);
        prescriptionData.put("_report_content_byte", pdfbytes);
        prescriptionData.put("message_attachment_name", "Consultation_" + consId);
        prescriptionData.put("consultation_id", consIdStr);
        prescriptionData.put("printtype", webPrinterId);
        prescriptionData.put("category", "Prescription");
        MessageManager mgr = new MessageManager();
        mgr.processEvent("prescription_saved", prescriptionData, true);
      }
    } catch (Exception exception) {
      logger.debug("Exception in PrescriptionPHRJob" + exception.getMessage());
    }
  }

}
