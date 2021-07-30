package com.insta.hms.messaging.providers;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.genericdocuments.DocumentPrintConfigurationsDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrescriptionsPrintTemplates.PrescriptionsTemplateDAO;
import com.insta.hms.messaging.MessageContext;
import com.insta.hms.outpatient.OPPrescriptionFtlHelper;
import org.apache.commons.beanutils.BasicDynaBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrescriptionEmailDataProvider extends QueryDataProvider {
  static Logger logger = LoggerFactory.getLogger(PatientDueDataProvider.class);
  private static String THIS_NAME = "Patient Prescription";
  private static final String selectFields = "SELECT * ";
  private static final String selectCount = "SELECT COUNT(*)";
  /** The from tables. */
  String fromTables = "FROM (select cons.consultation_id as key, "
      + "cons.patient_id as visit_id, cons.mr_no as "
      + "receipient_id__, cons.mr_no, '' as _report_content, "
      + " 'PATIENT' as receipient_type__,  sm.salutation as "
      + "salutation_name, pd.patient_name as recipient_name, pd.email_id as recipient_email, "
      + "pd.patient_phone as recipient_mobile, pr.docs_download_passcode::text as pass_word,"
      + "coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('S','B') then 'Y' else 'N' end , CASE WHEN "
      + "(cf.receive_communication in ('S','B') OR cf.receive_communication is null) "
      + " then 'Y' else 'N' end) as send_sms, "
      + " coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('E','B') then 'Y' else 'N' end ,CASE WHEN "
      + "(cf.receive_communication in ('E','B')  OR cf.receive_communication is null)"
      + " then 'Y' else 'N' end) as send_email,pcpref.message_group_name, "
      + " 'Prescription-'||to_char(cons.start_datetime,'DD-MM-YYYY')"
      + " as message_attachment_name ,to_char(cons.visited_date AT TIME ZONE (SELECT  "
      + "current_setting('TIMEZONE')) AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') "
      + "as visit_date,  to_char(cons.consultation_mod_time AT TIME ZONE (SELECT  "
      + "current_setting('TIMEZONE')) AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') "
      + "AS prescription_date, hcm.center_id, hcm.center_name,hcm.center_code, hcm "
      + ".center_contact_phone AS center_phone, "
      + "cons.doctor_name, d.doctor_mail_id as prescribing_doctor_email,"
      + "pr.mr_no as user_name, hcm.center_address,"
      + "(select hospital_name from generic_preferences) as" + " hospital_name , cons.visit_mode, "
      + " case when cf.lang_code is not null then cf.lang_code else "
      + "(select contact_pref_lang_code from generic_preferences) end as lang_code "
      + "from doctor_consultation cons LEFT JOIN patient_registration pr "
      + "on(cons.patient_id=pr.patient_id) JOIN patient_details pd ON(pr.mr_no = pd.mr_no)"
      + "LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)"
      + "LEFT JOIN hospital_center_master hcm  ON( pr.center_id = hcm.center_id )"
      + "LEFT JOIN patient_communication_preferences pcpref on (pd.mr_no = pcpref.mr_no and"
      + " pcpref.message_group_name='#') " + " LEFT JOIN contact_preferences cf ON(pd.mr_no = "
      + "cf.mr_no) LEFT JOIN doctors d ON(cons.doctor_name = d.doctor_id) "
      + "where cons.consultation_id= &) as foo";

  public PrescriptionEmailDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null);
  }

  /** The tokens. */
  String[] tokens = new String[] { "salutation_name", "recipient_name", "recipient_email",
      "recipient_mobile", "center_name", "center_code", "center_phone", "mr_no", "lang_code",
      "_report_content", "message_attachment_name", "visit_mode", "visit_date",
      "prescribing_doctor_email", "visit_id", "user_name", "center_address","pass_word"};

  @Override
  public List<String> getTokens() throws SQLException {
    List<String> tokenList =  new ArrayList<String>();
    tokenList =  Arrays.asList(tokens);
    Collections.sort(tokenList);
    return tokenList;

  }

  @Override
  public List<Map> getMessageDataList(MessageContext ctx) throws SQLException, ParseException {
    
    GenericDAO messageTypesDao = new GenericDAO("message_types");
    BasicDynaBean messageTypeBean = messageTypesDao.findByKey("message_type_id",
        (String) ctx.getMessageType().get("message_type_id"));
    String messageGroupName = (String) messageTypeBean.get("message_group_name");
    fromTables = fromTables.replace("#", messageGroupName);
    fromTables = fromTables.replace("&",  String.valueOf(ctx.getEventData().get("consultationId")));
    setQueryParams(selectFields, selectCount, fromTables, null);
    
    Map eventData = ctx.getEventData();
   
    List<Map> messageDataList = super.getMessageDataList(ctx);
    List<Map> messageDataListWithReportContent = new ArrayList<Map>();

    String consIdStr = String.valueOf((int) eventData.get("consultationId"));
    consIdStr = consIdStr.substring(consIdStr.lastIndexOf('_') + 1).trim();
    int centerId = (int) eventData.get("centerId");
    String schema = (String) eventData.get("schema");
    RequestContext.setConnectionDetails(new String[] { null, null, schema,
        (String) eventData.get("userName"), String.valueOf(centerId) });
    try {
      // if template is Web Based Prescription Template with blank content, copy
      // the content from
      // WebPrescription.ftl
      String templateContent = null;
      String templateName = (String) GenericPreferencesDAO.getAllPrefs()
          .get("default_prescription_web_template");
      Integer printerId = (Integer) GenericPreferencesDAO.getAllPrefs()
          .get("default_prescription_web_printer");
      if (templateName.equals("Web Based Prescription Template")) {
        BasicDynaBean pbean = PrescriptionsTemplateDAO.getTemplateContent(templateName);
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
            String realPath = (String) eventData.get("path");
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
      BasicDynaBean prefs = null;
      prefs = DocumentPrintConfigurationsDAO.getPrescriptionPrintPreferences(templateName,
          printerId);
      OPPrescriptionFtlHelper ftlHelper = new OPPrescriptionFtlHelper();
      String textReport = "";
      byte[] pdfbytes = ftlHelper.getConsultationFtlReport(consId, templateName,
          OPPrescriptionFtlHelper.ReturnType.PDF_BYTES, prefs, true, null,
          (String) eventData.get("userName"), OPPrescriptionFtlHelper.DefaultType.PRESCRIPTION);
      if (pdfbytes != null) {
        textReport = new String(pdfbytes);
      }
      Map<String, Object> prescriptionData = new HashMap<>();
      prescriptionData.putAll(messageDataList.get(0));
      prescriptionData.put("_report_content", textReport);
      prescriptionData.put("_message_attachment", textReport);
      prescriptionData.put("_report_content_byte", pdfbytes);
      prescriptionData.put("message_attachment_name", "Consultation_" + consId);
      prescriptionData.put("consultation_id", consIdStr);
      prescriptionData.put("printtype", String.valueOf(printerId));
      prescriptionData.put("category", "Prescription");
      ArrayList abcdd = (ArrayList) eventData.get("messageTo");
      String emailIds = "";
      for (Object s : abcdd.toArray()) {
        emailIds += s;
      }
      prescriptionData.put("recipient_email", emailIds);

      messageDataListWithReportContent.add(prescriptionData);

    } catch (Exception exception) {
      logger.debug("Exception in PatientPrescriptionSharing" + exception.getMessage());
    }
    return messageDataListWithReportContent;
  }
}

