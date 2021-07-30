package com.insta.hms.wardactivities.visitsummaryrecord;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.imageretriever.DoctorConsultImageRetriever;
import com.insta.hms.instaforms.AbstractInstaForms;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.Sections.SectionsDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.outpatient.AllergiesDAO;
import com.insta.hms.outpatient.AntenatalDAO;
import com.insta.hms.outpatient.ObstetricRecordDAO;
import com.insta.hms.outpatient.PreAnaesthestheticDAO;
import com.insta.hms.outpatient.PregnancyHistoryDAO;
import com.insta.hms.outpatient.SecondaryComplaintDAO;

import com.lowagie.text.DocumentException;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class GenericVisitSummaryRecordsFtlHelper.
 */
public class GenericVisitSummaryRecordsFtlHelper {
  
  /** The cfg. */
  private Configuration cfg;

  /**
   * Instantiates a new generic visit summary records ftl helper.
   *
   * @param cfg the cfg
   */
  public GenericVisitSummaryRecordsFtlHelper(Configuration cfg) {
    this.cfg = cfg;
  }

  /**
   * The Enum ReturnType.
   */
  public enum ReturnType {
    
    /** The pdf. */
    PDF, 
 /** The pdf bytes. */
 PDF_BYTES, 
 /** The text bytes. */
 TEXT_BYTES
  }

  /**
   * Gets the visit summary records report.
   *
   * @param patientId the patient id
   * @param enumType the enum type
   * @param prefs the prefs
   * @param os the os
   * @return the visit summary records report
   * @throws SQLException the SQL exception
   * @throws DocumentException the document exception
   * @throws TemplateException the template exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException the transformer exception
   */
  public byte[] getVisitSummaryRecordsReport(String patientId, ReturnType enumType,
      BasicDynaBean prefs, OutputStream os) throws SQLException, DocumentException,
      TemplateException, IOException, XPathExpressionException, TransformerException {

    byte[] bytes = null;

    Map patientDetails = new HashMap();
    GenericDocumentsFields.copyPatientDetails(patientDetails, null, patientId, false);
    Map ftlParamMap = new HashMap();
    ftlParamMap.put("visitdetails", patientDetails);
    ftlParamMap.put("modules_activated",
        ((Preferences) RequestContext.getSession().getAttribute("preferences"))
            .getModulesActivatedMap());

    ftlParamMap.put("secondary_complaints",
        new SecondaryComplaintDAO().getSecondaryComplaints(patientId));
    ftlParamMap.put("diagnosis_details", MRDDiagnosisDAO.getAllDiagnosisDetails(patientId));

    AbstractInstaForms formDAO = AbstractInstaForms.getInstance("Form_IP");
    String itemType = (String) formDAO.getKeys().get("item_type");

    Map params = new HashMap();
    params.put("patient_id", new String[] { patientId });
    BasicDynaBean compBean = formDAO.getComponents(params);

    ftlParamMap.put("allergies", AllergiesDAO.getAllActiveAllergies(
        (String) patientDetails.get("mr_no"), patientId, 0, 0, (Integer) compBean.get("form_id"),
        itemType));
    ftlParamMap.put("pac_details", PreAnaesthestheticDAO.getAllPACRecords(
        (String) patientDetails.get("mr_no"), patientId, 0, 0, (Integer) compBean.get("form_id"),
        itemType));
    ftlParamMap.put("pregnancyhistories", PregnancyHistoryDAO.getAllPregnancyDetails(
        (String) patientDetails.get("mr_no"), patientId, 0, 0, (Integer) compBean.get("form_id"),
        itemType));
    ftlParamMap.put("pregnancyhistoriesBean", ObstetricRecordDAO.getAllObstetricHeadDetails(
        (String) patientDetails.get("mr_no"), patientId, 0, 0, (Integer) compBean.get("form_id"),
        itemType));
    ftlParamMap.put("antenatalinfo", AntenatalDAO.getAllAntenatalDetails(
        (String) patientDetails.get("mr_no"), patientId, 0, 0, (Integer) compBean.get("form_id"),
        itemType));
    
    PatientSectionDetailsDAO psdDAO = new PatientSectionDetailsDAO();
    ftlParamMap.put("patientNotes", psdDAO.getPatientFinalNotes(patientId));
    List<BasicDynaBean> instaFormValues = psdDAO.getAllSectionDetails(patientId, patientId, 0, 0,
        (Integer) compBean.get("form_id"), itemType);

    Map<Object, List<List>> map = ConversionUtils.listBeanToMapListListBean(instaFormValues,
        "str_section_detail_id", "field_id");
    ftlParamMap.put("PhysicianForms", map);

    ftlParamMap.put("ip_record_components", compBean.getMap());
    ftlParamMap.put("insta_sections", SectionsDAO.getAddedSectionMasterDetails(
        (String) patientDetails.get("mr_no"), patientId, 0, 0, (Integer) compBean.get("form_id"),
        itemType));

    String templateContent = new PrintTemplatesDAO()
        .getCustomizedTemplate(PrintTemplate.VisitSummaryRecord);
    Template temp = null;
    if (templateContent != null && !templateContent.equals("")) {
      StringReader reader = new StringReader(templateContent);
      temp = new Template("VisitSummaryRecordsReportTemplate.ftl", reader, AppInit.getFmConfig());
    } else {
      temp = cfg.getTemplate("VisitSummaryRecordsReport.ftl");
    }

    StringWriter writer = new StringWriter();
    temp.process(ftlParamMap, writer);
    HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
    boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equalsIgnoreCase("Y");
    if (enumType.equals(ReturnType.PDF)) {
      hc.writePdf(os, writer.toString(), "Visit Summary Record", prefs, false, repeatPHeader, true,
          true, true, false);
      os.close();

    } else if (enumType.equals(ReturnType.PDF_BYTES)) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      hc.writePdf(stream, writer.toString(), "Visit Summary Record", prefs, false, repeatPHeader,
          true, true, true, false);
      bytes = stream.toByteArray();
      stream.close();

    } else if (enumType.equals(ReturnType.TEXT_BYTES)) {
      bytes = hc.getText(writer.toString(), "Visit Summary Record", prefs, true, true);

    }
    return bytes;
  }

}
