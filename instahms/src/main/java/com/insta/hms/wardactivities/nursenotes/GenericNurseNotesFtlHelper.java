package com.insta.hms.wardactivities.nursenotes;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
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
 * The Class GenericNurseNotesFtlHelper.
 *
 * @author nikunj.s
 */
public class GenericNurseNotesFtlHelper {
  
  /** The cfg. */
  private Configuration cfg;

  /**
   * Instantiates a new generic nurse notes ftl helper.
   *
   * @param cfg the cfg
   */
  public GenericNurseNotesFtlHelper(Configuration cfg) {
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
   * Gets the nurese notes report.
   *
   * @param patientId the patient id
   * @param enumType the enum type
   * @param prefs the prefs
   * @param os the os
   * @return the nurese notes report
   * @throws SQLException the SQL exception
   * @throws DocumentException the document exception
   * @throws TemplateException the template exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException the transformer exception
   */
  public byte[] getNureseNotesReport(String patientId, ReturnType enumType, BasicDynaBean prefs,
      OutputStream os) throws SQLException, DocumentException, TemplateException, IOException,
      XPathExpressionException, TransformerException {

    byte[] bytes = null;

    Map patientDetails = new HashMap();
    GenericDocumentsFields.copyPatientDetails(patientDetails, null, patientId, false);
    Map ftlParamMap = new HashMap();
    ftlParamMap.put("visitdetails", patientDetails);
    ftlParamMap.put("modules_activated",
        ((Preferences) RequestContext.getSession().getAttribute("preferences"))
            .getModulesActivatedMap());

    List<BasicDynaBean> nurseNotesList = NurseNotesDAO.getNurseNotes(patientId);
    ftlParamMap.put("NurseNotesList", nurseNotesList);

    String templateContent = new PrintTemplatesDAO()
        .getCustomizedTemplate(PrintTemplate.NurseNotes);
    Template temp = null;
    if (templateContent != null && !templateContent.equals("")) {
      StringReader reader = new StringReader(templateContent);
      temp = new Template("NurseNotesReportTemplate.ftl", reader, AppInit.getFmConfig());
    } else {
      temp = cfg.getTemplate("NurseNotesReport.ftl");
    }

    StringWriter writer = new StringWriter();
    temp.process(ftlParamMap, writer);
    HtmlConverter hc = new HtmlConverter();
    boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equalsIgnoreCase("Y");
    if (enumType.equals(ReturnType.PDF)) {
      hc.writePdf(os, writer.toString(), "Nurse's Notes", prefs, false, repeatPHeader, true, true,
          true, false);
      os.close();

    } else if (enumType.equals(ReturnType.PDF_BYTES)) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      hc.writePdf(stream, writer.toString(), "Nurse's Notes", prefs, false, repeatPHeader, true,
          true, true, false);
      bytes = stream.toByteArray();
      stream.close();

    } else if (enumType.equals(ReturnType.TEXT_BYTES)) {
      bytes = hc.getText(writer.toString(), "Nurse's Notes", prefs, true, true);

    }
    return bytes;
  }
}
