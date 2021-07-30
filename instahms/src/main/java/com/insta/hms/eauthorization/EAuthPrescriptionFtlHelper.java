package com.insta.hms.eauthorization;

import com.bob.hms.common.APIUtility;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.lowagie.text.DocumentException;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;



/**
 * The Class EAuthPrescriptionFtlHelper.
 */
public class EAuthPrescriptionFtlHelper {

  /**
   * The log.
   */
  static Logger log = LoggerFactory
      .getLogger(EAuthPrescriptionFtlHelper.class);

  /**
   * The e auth presc DAO.
   */
  EAuthPrescriptionDAO eauthPrescDAO = new EAuthPrescriptionDAO();

  /**
   * The ph template dao.
   */
  private PatientHeaderTemplateDAO phTemplateDao = new PatientHeaderTemplateDAO();

  /**
   * The cfg.
   */
  private Configuration cfg = null;

  /**
   * Instantiates a new e auth prescription ftl helper.
   */
  public EAuthPrescriptionFtlHelper() {
    cfg = AppInit.getFmConfig();
  }

  /**
   * Instantiates a new e auth prescription ftl helper.
   *
   * @param cfg the cfg
   */
  public EAuthPrescriptionFtlHelper(Configuration cfg) {
    this.cfg = cfg;
  }

  /**
   * The Enum return_type.
   */
  public enum ReturnType {

    /**
     * The pdf.
     */
    PDF,
    /**
     * The pdf bytes.
     */
    PDF_BYTES,
    /**
     * The text bytes.
     */
    TEXT_BYTES,
    /**
     * The html.
     */
    HTML
  }

  ;

  /**
   * Gets the prior auth prescription ftl report.
   *
   * @param prescId   the presc id
   * @param consultId the consult id
   * @param enumType  the enum type
   * @param prefs     the prefs
   * @param os        the os
   * @param userName  the user name
   * @param patId     the pat id
   * @return the prior auth prescription ftl report
   * @throws SQLException             the SQL exception
   * @throws DocumentException        the document exception
   * @throws TemplateException        the template exception
   * @throws IOException              Signals that an I/O exception has
   *                                  occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   * @throws ParseException           the parse exception
   */
  public byte[] getPriorAuthPrescriptionFtlReport(int prescId, int consultId,
      ReturnType enumType, BasicDynaBean prefs, OutputStream os,
      String userName, String patId) throws SQLException, DocumentException,
      TemplateException, IOException, XPathExpressionException, TransformerException,
      ParseException {
    return getPriorAuthPrescriptionFtlReport(prescId, consultId, enumType,
        prefs, os, userName, patId, null);

  }

  /**
   * Gets the prior auth prescription ftl report.
   *
   * @param prescId       the presc id
   * @param consultId     the consult id
   * @param enumType      the enum type
   * @param prefs         the prefs
   * @param os            the os
   * @param userName      the user name
   * @param patId         the pat id
   * @param patientDetMap the patient det map
   * @return the prior auth prescription ftl report
   * @throws SQLException             the SQL exception
   * @throws DocumentException        the document exception
   * @throws TemplateException        the template exception
   * @throws IOException              Signals that an I/O exception has
   *                                  occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   * @throws ParseException           the parse exception
   */
  public byte[] getPriorAuthPrescriptionFtlReport(int prescId, int consultId,
                                                  ReturnType enumType, BasicDynaBean prefs,
                                                  OutputStream os,
                                                  String userName, String patId, Map patientDetMap)
      throws SQLException, DocumentException, TemplateException,
      IOException, XPathExpressionException, TransformerException,
      ParseException {

    byte[] bytes = null;

    BasicDynaBean consultBean = DoctorConsultationDAO
        .getConsultDetails(consultId);
    Map patientDetails = new HashMap();
    if (patientDetMap == null || patientDetMap.isEmpty()) {
      GenericDocumentsFields.copyPatientDetails(patientDetails, null, patId,
          false);
    } else {
      GenericDocumentsFields.convertAndCopy(patientDetMap, patientDetails,
          false);
    }

    Map ftlParamMap = new HashMap();
    ftlParamMap.put("consultation_bean", consultBean);

    // Add some patient registration fields
    BasicDynaBean patientRegistration = eauthPrescDAO
        .getEAuthPatientRegistration(patId);
    patientDetails.putAll(patientRegistration.getMap());
    ftlParamMap.put("visitdetails", patientDetails);
    String patientId;
    if (consultBean != null) {
      patientId = (String) consultBean.get("patient_id");
    } else {
      patientId = patId;
    }
    ftlParamMap.put("diagnosis_details",
        MRDDiagnosisDAO.getAllDiagnosisDetails(patientId));

    Map modulesActivatedMap = APIUtility.getPreferences()
        .getModulesActivatedMap();
    List<BasicDynaBean> prescActivities = eauthPrescDAO
        .getEAuthPrescriptionActivities(prescId);
    BasicDynaBean prescRequest = eauthPrescDAO
        .getEAuthPrescriptionRequest(prescId);

    ftlParamMap.put("preAuthPrescActivities", prescActivities);
    ftlParamMap.put("preAuthServices", EAuthPrescriptionDAO
        .getPreAuthPrescribedItems(prescActivities, "SER"));
    ftlParamMap.put("preAuthInv", EAuthPrescriptionDAO
        .getPreAuthPrescribedItems(prescActivities, "DIA"));
    ftlParamMap.put("preAuthOperation", EAuthPrescriptionDAO
        .getPreAuthPrescribedItems(prescActivities, "OPE"));
    ftlParamMap.put("preAuthDoctor", EAuthPrescriptionDAO
        .getPreAuthPrescribedItems(prescActivities, "DOC"));
    ftlParamMap.put("totalActAmount",
        eauthPrescDAO.getPriorAuthPrescTotalsCharge(prescActivities));
    ftlParamMap.put("modules_activated", modulesActivatedMap);
    ftlParamMap.put("preAuthPrescRequest", prescRequest);

    PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
    PrintTemplate printTemplate = null;
    printTemplate = PrintTemplate.PriorAuthPrescription;
    String templateContent = printtemplatedao
        .getCustomizedTemplate(printTemplate);
    Template template = null;
    if (templateContent == null || templateContent.equals("")) {
      template = cfg.getTemplate(printTemplate.getFtlName() + ".ftl");
    } else {
      StringReader reader = new StringReader(templateContent);
      template = new Template("PreauthPrescriptionPrintTemplate.ftl", reader,
          AppInit.getFmConfig());
    }

    StringWriter writer = new StringWriter();
    template.process(ftlParamMap, writer);
    HtmlConverter hc = new HtmlConverter();
    StringBuilder printContent = new StringBuilder();
    printContent.append(writer.toString());
    Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info"))
        .equals("Y");
    if (enumType.equals(ReturnType.PDF)) {
      hc.writePdf(os, printContent.toString(), "Prior Auth Prescription",
          prefs, false, repeatPHeader, true, true, true, false);
      os.close();
    } else if (enumType.equals(ReturnType.PDF_BYTES)) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      hc.writePdf(stream, printContent.toString(),
          "Prior Auth Prescription", prefs, false, repeatPHeader,
          true, true, true, false);
      bytes = stream.toByteArray();
      stream.close();

    } else if (enumType.equals(ReturnType.TEXT_BYTES)) {
      bytes = hc.getText(printContent.toString(),
          "Prior Auth Prescription", prefs, true, true);

    } else if (enumType.equals(ReturnType.HTML)) {
      return printContent.toString().getBytes();
    }
    return bytes;

  }

}
