package com.insta.hms.wardactivities;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplate;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;

import com.lowagie.text.DocumentException;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * The Class PatientActivitiesBO.
 *
 * @author krishna
 */
public class PatientActivitiesBO {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(PatientActivitiesBO.class);

  /**
   * The Enum ReturnType.
   */
  /*
   * Copy the request parameter map to a bean, by looking at the dyna properties of the bean and
   * setting those values, if found, from the request. If nullifyEmptyStrings is true, then, empty
   * strings are set as null (which is usually what we want).
   */
  public enum ReturnType {
    
    /** The pdf. */
    PDF, 
 /** The pdf bytes. */
 PDF_BYTES, 
 /** The text bytes. */
 TEXT_BYTES
  }

  /** The ph template dao. */
  private PatientHeaderTemplateDAO phTemplateDao = new PatientHeaderTemplateDAO();
  
  /** The print template dao. */
  private PrintTemplatesDAO printTemplateDao = new PrintTemplatesDAO();

  /**
   * Copy to dyna bean.
   *
   * @param from the from
   * @param bean the bean
   */
  public static void copyToDynaBean(Map from, DynaBean bean) {

    DynaProperty[] dynaProperties = bean.getDynaClass().getDynaProperties();
    for (DynaProperty property : dynaProperties) {
      String fieldname = property.getName();
      Object object = (Object) from.get(fieldname);

      if (object != null && !object.equals("")) {
        try {
          bean.set(fieldname, ConvertUtils.convert(object, property.getType()));
        } catch (ConversionException exe) {
          log.error("Conversion error. " + fieldname + "=" + object + " could not be converted to "
              + property.getType(), exe);
        }
      }
    }
  }

  /**
   * Gets the ward activities report.
   *
   * @param patientId the patient id
   * @param enumType the enum type
   * @param prefs the prefs
   * @param os the os
   * @param userName the user name
   * @return the ward activities report
   * @throws SQLException the SQL exception
   * @throws DocumentException the document exception
   * @throws TemplateException the template exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException the transformer exception
   */
  public byte[] getWardActivitiesReport(String patientId, ReturnType enumType,
      BasicDynaBean prefs, OutputStream os, String userName) throws SQLException,
      DocumentException, TemplateException, IOException, XPathExpressionException,
      TransformerException {
    byte[] bytes = null;
    Map patientDetails = new HashMap();
    GenericDocumentsFields.copyPatientDetails(patientDetails, null, patientId, false);
    PrintTemplate template = PrintTemplate.Patient_Ward_Activities;
    String templateContent = printTemplateDao.getCustomizedTemplate(template);

    Template temp = null;
    if (templateContent == null || templateContent.equals("")) {
      temp = AppInit.getFmConfig().getTemplate(template.getFtlName() + ".ftl");
    } else {
      StringReader reader = new StringReader(templateContent);
      temp = new Template(null, reader, AppInit.getFmConfig());
    }

    Map ftlParamMap = new HashMap();
    ftlParamMap.put("visitdetails", patientDetails);
    ftlParamMap.put("modules_activated",
        ((Preferences) RequestContext.getSession().getAttribute("preferences"))
            .getModulesActivatedMap());

    List activitiesList = PatientActivitiesDAO.getActivities(patientId, true);
    ftlParamMap.put("patient_activities", activitiesList);
    StringWriter writer = new StringWriter();
    try {
      temp.process(ftlParamMap, writer);
    } finally {
      log.debug("Exception raised while processing the patient header for patient Id : "
          + patientId);
    }
    HtmlConverter hc = new HtmlConverter();
    String patientHeader = (String) phTemplateDao.getPatientHeader(
        (Integer) printTemplateDao.getPatientHeaderTemplateId(template),
        PatientHeaderTemplate.PatientWardActivities.getType());

    Template patientHeaderTemplate = new Template("PatientHeader.ftl", new StringReader(
        patientHeader), AppInit.getFmConfig());
    Map ftlParams = new HashMap();
    ftlParams.put("visitdetails", patientDetails);
    ftlParams.put("modules_activated",
        ((Preferences) RequestContext.getSession().getAttribute("preferences"))
            .getModulesActivatedMap());
    StringWriter patientHeaderWriter = new StringWriter();
    try {
      patientHeaderTemplate.process(ftlParams, patientHeaderWriter);
    } catch (TemplateException te) {
      log.debug("Exception raised while processing the patient header for report Id : "
          + patientId);
      throw te;
    }
    StringBuilder printContent = new StringBuilder();
    printContent.append(patientHeaderWriter.toString());
    printContent.append(writer.toString());

    Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");
    if (enumType.equals(ReturnType.PDF)) {
      hc.writePdf(os, printContent.toString(), "Patient Ward Activities", prefs, false,
          repeatPHeader, true, true, true, false);
      os.close();

    } else if (enumType.equals(ReturnType.PDF_BYTES)) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      hc.writePdf(stream, printContent.toString(), "Patient Ward Activities", prefs, false,
          repeatPHeader, true, true, true, false);
      bytes = stream.toByteArray();
      stream.close();

    } else if (enumType.equals(ReturnType.TEXT_BYTES)) {
      bytes = hc.getText(printContent.toString(), "Patient Ward Activities", prefs, true, true);

    }
    return bytes;

  }

}
