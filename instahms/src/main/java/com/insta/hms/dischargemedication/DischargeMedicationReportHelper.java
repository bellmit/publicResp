package com.insta.hms.dischargemedication;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplate;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.mdm.doctors.DoctorRepository;
import com.lowagie.text.DocumentException;

import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

// TODO: Auto-generated Javadoc
/**
 * The Class DischargeMedicationReportHelper.
 *
 * @author krishna
 */
public class DischargeMedicationReportHelper {

  /** The log. */
  public static final Logger log = LoggerFactory.getLogger(DischargeMedicationReportHelper.class);

  /** The print template DAO. */
  PrintTemplatesDAO printTemplateDAO = new PrintTemplatesDAO();

  /** The ph template DAO. */
  PatientHeaderTemplateDAO phTemplateDAO = new PatientHeaderTemplateDAO();
  
  private DoctorRepository doctorRepository = new DoctorRepository();

  /**
   * Gets the discharge medication sheet.
   *
   * @param patientId the patient id
   * @return the discharge medication sheet
   * @throws SQLException the SQL exception
   * @throws DocumentException the document exception
   * @throws TemplateException the template exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException the transformer exception
   * @throws ParseException the parse exception
   */
  public String getDischargeMedicationSheet(String patientId)
      throws SQLException, DocumentException, TemplateException, IOException,
      XPathExpressionException, TransformerException, ParseException {

    List<BasicDynaBean> dischargeMedicationDetails = null;
    Map params = new HashMap();

    BasicDynaBean bean = DischargeMedicationDAO.getDischargeMedicationByUser(patientId);
    if (bean != null) {
      params.put("medicationDoctorName", bean.get("doctor_name"));
      params.put("licenceNo", bean.get("doctor_license_number"));
      params.put("medicationDoctorId", bean.get("doctor_id"));
    }
    int patientCenterId = VisitDetailsDAO.getCenterId(patientId);
    String helathAuthority = CenterMasterDAO.getHealthAuthorityForCenter(patientCenterId);
    BasicDynaBean visitBean = VisitDetailsDAO.getPatientVisitDetailsBean(patientId);
    String visitType = (String) visitBean.get("visit_type");
    String orgId = (String) visitBean.get("org_id");
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    Boolean useStoreItems = genericPrefs.get("prescription_uses_stores").equals("Y");

    // Discharge Medication Details
    dischargeMedicationDetails = DischargeMedicationDAO.getDischargeMedicationDetails(patientId,
        helathAuthority, useStoreItems, visitType, orgId);
    params.put("medicationDetails", dischargeMedicationDetails);
   
    List<String> doctorIds = new ArrayList<>();
    HashMap<String, List<BasicDynaBean>> medicationDetailsMap = new HashMap<>();
    if (dischargeMedicationDetails != null && !dischargeMedicationDetails.isEmpty()) {
      for (BasicDynaBean dischargeMedication : dischargeMedicationDetails) {
        String doctorId = (String) dischargeMedication.get("doctor_id");
        if (StringUtils.isEmpty(doctorId)) {
          doctorId = "--";
        }
        if (!medicationDetailsMap.containsKey(doctorId)) {
          doctorIds.add(doctorId);
          List<BasicDynaBean> dischargeMedications = new ArrayList<>();
          dischargeMedications.add(dischargeMedication);
          medicationDetailsMap.put(doctorId, dischargeMedications);
        } else {
          medicationDetailsMap.get(doctorId).add(dischargeMedication);
        }
      }
    }

    params.put("medicationDetailsMap", medicationDetailsMap);

    HashMap<String, BasicDynaBean> doctorMap = new HashMap<>();
    if (medicationDetailsMap != null && !medicationDetailsMap.isEmpty()) {
      List<BasicDynaBean> doctorsList = doctorRepository.getDoctorDepartmentInfo(doctorIds);
      if (doctorsList != null && !doctorsList.isEmpty()) {
        for (BasicDynaBean doctor : doctorsList) {
          doctorMap.put((String) doctor.get("doctor_id"), doctor);
        }
      }
    }

    params.put("doctorMap", doctorMap);

    // Patient Allergy Details
    List<BasicDynaBean> allergyDetails = null;
    allergyDetails = DischargeMedicationDAO.getallergyDetails(patientId);
    params.put("allergyDetails", allergyDetails);

    // Patient Diagnosis Details
    List<BasicDynaBean> diagnosisDetails = null;
    diagnosisDetails = DischargeMedicationDAO.getdiagnosisDetails(patientId);
    params.put("diagnosisDetails", diagnosisDetails);

    // Patient Weight from Vitals
    BasicDynaBean vitalWeight = null;
    vitalWeight = DischargeMedicationDAO.getVitalsWeight(patientId);
    if (vitalWeight != null && !"".equals(vitalWeight.get("weight"))) {
      params.put("weight", vitalWeight.get("weight"));
    }

    Map modulesActivatedMap =
        ((Preferences) RequestContext.getSession().getAttribute("preferences"))
            .getModulesActivatedMap();
    Map patientDetails = new HashMap();
    GenericDocumentsFields.copyPatientDetails(patientDetails, null, patientId, false);
    params.put("visitdetails", patientDetails);
    params.put("modules_activated", modulesActivatedMap);

    PrintTemplate template = PrintTemplate.Discharge_Medication;
    String templateContent = printTemplateDAO.getCustomizedTemplate(template);

    FtlReportGenerator ftlGenTemplate = null;
    if (templateContent == null || templateContent.equals("")) {
      ftlGenTemplate = new FtlReportGenerator(template.getFtlName());
    } else {
      StringReader reader = new StringReader(templateContent);
      ftlGenTemplate = new FtlReportGenerator(template.getFtlName(), reader);
    }

    String printTemplateType = PatientHeaderTemplate.Discharge_Medication.getType();
    Integer pheaderTemplateId =
        (Integer) printTemplateDAO.getPatientHeaderTemplateId(PrintTemplate.Discharge_Medication);
    String patientHeader = phTemplateDAO.getPatientHeader(pheaderTemplateId, printTemplateType);
    FtlReportGenerator ftlGen =
        new FtlReportGenerator("PatientHeader", new StringReader(patientHeader));

    Map ftlParams = new HashMap();
    ftlParams.put("visitdetails", VisitDetailsDAO.getPatientVisitDetailsMap(patientId));
    StringWriter hwriter = new StringWriter();
    try {
      ftlGen.setReportParams(ftlParams);
      ftlGen.process(hwriter);
    } catch (TemplateException te) {
      throw te;
    }

    StringWriter writer = new StringWriter();
    try {
      ftlGenTemplate.setReportParams(params);
      ftlGenTemplate.process(writer);
    } finally {
      log.debug(
          "Exception raised while processing the patient header for patient Id : " + patientId);
    }

    StringBuilder printContent = new StringBuilder();
    printContent.append(hwriter.toString()).append(writer.toString());

    return printContent.toString();
  }
}
