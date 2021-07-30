/**
 *
 */

package com.insta.hms.initialassessment;

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
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.Sections.SectionsDAO;
import com.insta.hms.outpatient.AllergiesDAO;
import com.insta.hms.outpatient.AntenatalDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.outpatient.PhysicianFormValuesDAO;
import com.insta.hms.vitalForm.genericVitalFormDAO;
import com.insta.hms.vitalparameter.VitalMasterDAO;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

// TODO: Auto-generated Javadoc
/**
 * The Class InitialAssessmentFtlHelper.
 *
 * @author krishna
 */
public class InitialAssessmentFtlHelper {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(InitialAssessmentFtlHelper.class);

  /** The ph template dao. */
  private PatientHeaderTemplateDAO phTemplateDao = new PatientHeaderTemplateDAO();

  /** The physician form values DAO. */
  PhysicianFormValuesDAO physicianFormValuesDAO = new PhysicianFormValuesDAO();

  /** The vm DAO. */
  VitalMasterDAO vmDAO = new VitalMasterDAO();

  /** The cfg. */
  private Configuration cfg = null;

  /**
   * Instantiates a new initial assessment ftl helper.
   */
  public InitialAssessmentFtlHelper() {
    cfg = AppInit.getFmConfig();
  }

  /**
   * The Enum return_type.
   */
  public enum ReturnType {
    /** The pdf. */
    PDF,
    /** The pdf bytes. */
    PDF_BYTES,
    /** The text bytes. */
    TEXT_BYTES,
    /** The html. */
    HTML
  }

  /**
   * Instantiates a new initial assessment ftl helper.
   *
   * @param cfg the cfg
   */
  public InitialAssessmentFtlHelper(Configuration cfg) {
    this.cfg = cfg;
  }

  /**
   * Prints the assessment.
   *
   * @param consultId the consult id
   * @param prefs     the prefs
   * @param os        the os
   * @param userName  the user name
   * @param enumType  the enum type
   * @return the byte[]
   * @throws SQLException             the SQL exception
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws DocumentException        the document exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TemplateException        the template exception
   * @throws TransformerException     the transformer exception
   */
  public byte[] printAssessment(int consultId, BasicDynaBean prefs, OutputStream os,
      String userName, ReturnType enumType) throws SQLException, IOException, DocumentException,
      XPathExpressionException, TemplateException, TransformerException {
    byte[] bytes = null;
    BasicDynaBean consultBean = DoctorConsultationDAO.getConsultDetails(consultId);
    Map patientDetails = new HashMap();
    GenericDocumentsFields.copyPatientDetails(patientDetails, null,
        (String) consultBean.get("patient_id"), false);
    Map ftlParamMap = new HashMap();

    List<BasicDynaBean> vitalParams = vmDAO
        .getUniqueVitalsforPatient((String) consultBean.get("patient_id"));
    if (vitalParams == null || vitalParams.isEmpty()) {
      vitalParams = vmDAO.getActiveVitalParams("O");
    }
    ftlParamMap.put("vital_params", vitalParams);
    ftlParamMap.put("vitals",
        genericVitalFormDAO.groupByReadingId((String) consultBean.get("patient_id"), "V"));
    ftlParamMap.put("consultation_bean", consultBean);
    ftlParamMap.put("userName", userName);

    AbstractInstaForms formDAO = AbstractInstaForms.getInstance("Form_IA");
    String itemType = (String) formDAO.getKeys().get("item_type");

    Map params = new HashMap();
    params.put("consultation_id", new String[] { consultId + "" });

    BasicDynaBean compBean = formDAO.getComponents(params);

    ftlParamMap.put("allergies",
        AllergiesDAO.getAllActiveAllergies((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id"), consultId, 0, (Integer) compBean.get("form_id"),
            itemType));
    ftlParamMap.put("pregnancyhistories",
        formDAO.getPregnancyHistories((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id"), consultId, 0,
            (Integer) compBean.get("form_id")));
    ftlParamMap.put("pregnancyhistoriesBean",
        formDAO.getObstetricrecords((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id"), consultId, 0,
            (Integer) compBean.get("form_id")));

    List antenatalinfo = AntenatalDAO.getAllAntenatalDetails((String) consultBean.get("mr_no"),
        (String) consultBean.get("patient_id"), consultId, 0, (Integer) compBean.get("form_id"),
        itemType);
    Map antenatalinfoMap = null;
    antenatalinfoMap = ConversionUtils.listBeanToMapListBean(antenatalinfo, "pregnancy_count_key");
    Set antenatalKeyCounts = null;
    antenatalKeyCounts = antenatalinfoMap.keySet();
    ftlParamMap.put("antenatalinfo", antenatalinfo);
    ftlParamMap.put("antenatalKeyCounts", antenatalKeyCounts);
    ftlParamMap.put("antenatalinfoMap", antenatalinfoMap);

    PatientSectionDetailsDAO psdDao = new PatientSectionDetailsDAO();
    List<BasicDynaBean> consValues = psdDao.getAllSectionDetails((String) consultBean.get("mr_no"),
        (String) consultBean.get("patient_id"), consultId, 0, (Integer) compBean.get("form_id"),
        itemType);
    Map<Object, List<List>> map = ConversionUtils.listBeanToMapListListBean(consValues,
        "str_section_detail_id", "field_id");

    String compVitals = "N";
    Map compMap = new HashMap();
    for (String comp : ((String) compBean.get("sections")).split(",")) {
      Integer formid = Integer.parseInt(comp);
      if (formid == -4) {
        compVitals = "Y";
        compMap.put("vitals", compVitals);
      }
    }
    compMap.putAll(compBean.getMap());
    ftlParamMap.put("PhysicianForms", map);
    ftlParamMap.put("assessment_components", compMap);

    ftlParamMap.put("insta_sections",
        SectionsDAO.getAddedSectionMasterDetails((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id"), consultId, 0, (Integer) compBean.get("form_id"),
            itemType));
    Template ftlTemplate = null;
    boolean isClosed = consultBean.get("status").equals("C");

    String templateContent = new PrintTemplatesDAO()
        .getCustomizedTemplate(PrintTemplate.Initial_Assessment);
    if (templateContent == null || templateContent.equals("")) {
      ftlTemplate = cfg.getTemplate("InitialAssessmentPrint.ftl");
    } else {
      StringReader reader = new StringReader(templateContent);
      ftlTemplate = new Template("InitialAssessmentFtlPrint.ftl", reader, cfg);
    }
    StringWriter writer = new StringWriter();
    ftlTemplate.process(ftlParamMap, writer);

    String patientHeader = (String) phTemplateDao.getPatientHeader(
        new PrintTemplatesDAO().getPatientHeaderTemplateId(PrintTemplate.Initial_Assessment),
        "Assessment");
    StringReader reader = new StringReader(patientHeader);
    Template pt = new Template("PatientHeader.ftl", reader, AppInit.getFmConfig());
    Map templateMap = new HashMap();
    Map modulesActivatedMap = new HashMap();
    if (RequestContext.getSession() != null) {
      modulesActivatedMap = ((Preferences) RequestContext.getSession().getAttribute("preferences"))
          .getModulesActivatedMap();
    } else {
      Preferences preferences = null;
      Connection con = DataBaseUtil.getConnection();
      PreferencesDao dao = new PreferencesDao(con);
      preferences = dao.getPreferences();
      Map groups = preferences.getModulesActivatedMap();
      modulesActivatedMap.put("modules_activated", groups);
      if (con != null) {
        con.close();
      }
    }
    templateMap.put("visitdetails", patientDetails);
    templateMap.put("modules_activated", modulesActivatedMap);
    StringWriter pwriter = new StringWriter();

    try {
      pt.process(templateMap, pwriter);
    } catch (TemplateException te) {
      log.error("", te);
      throw te;
    }

    StringBuilder documentContent = new StringBuilder();
    documentContent.append(pwriter.toString());
    documentContent.append(writer.toString());

    HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
    Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");
    if (enumType.equals(ReturnType.PDF)) {

      hc.writePdf(os, documentContent.toString(), "OP Prescription", prefs, false, repeatPHeader,
          true, true, isClosed, false);
      os.close();

    } else if (enumType.equals(ReturnType.PDF_BYTES)) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      hc.writePdf(stream, documentContent.toString(), "OP Prescription", prefs, false,
          repeatPHeader, true, true, isClosed, false);
      bytes = stream.toByteArray();
      stream.close();

    } else if (enumType.equals(ReturnType.TEXT_BYTES)) {
      bytes = hc.getText(documentContent.toString(), "OP Prescription", prefs, true, true);
    }
    return bytes;
  }

}
