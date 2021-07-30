package com.insta.hms.messaging.providers;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PdfUtils;
import com.insta.hms.diagnosticmodule.common.DiagReportGenerator;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryBO;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.insta.hms.imageretriever.DiagImageRetriever;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.messaging.MessageContext;


import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DiagReportDataProvider.
 */
public class DiagReportDataProvider extends QueryDataProvider {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(DiagReportDataProvider.class);

  private static LaboratoryDAO dao = new LaboratoryDAO();


  /** The this name. */
  private static String THIS_NAME = "Report Ready Patients ";

  /** The Constant selectFields. */
  private static final String selectFields = "SELECT * ";

  /** The Constant selectCount. */
  private static final String selectCount = "SELECT COUNT(*)";

  /** The from tables. */
  private String fromTables = "FROM (SELECT tvr.report_id as key,"
      + " tvr.report_name as diag_report_name, tvr.report_id as entity_id, "
      + " '' as _report_content,tvr.report_name as message_attachment_name, "
      + " '' as _message_attachment,to_char(tvr.report_date, 'DD-MM-YYYY') as report_date,"
      + "to_char(tvr.report_date, 'DD-Mon-YYYY') as signed_off_date,"
      + "to_char(tvr.report_date, 'HH24:MI:SS') as report_time, "
      + "pd.mr_no as receipient_id__, 'PATIENT' as receipient_type__,"
      + "to_char(tvr.report_date, 'HH12:MI AM') as report_time_12hr, "
      + "date(tvr.report_date)::text as report_date_yyyy_mm_dd, "
      + "to_char(tvr.report_date, 'yyyy-MM-dd') as report_date_search__,  "
      + "coalesce(sm.salutation,'') as salutation_name, "
      + "coalesce(CONCAT(pd.patient_name,' ',pd.middle_name,' ',pd.last_name)"
      + ",ih.hospital_name) as receipient_name, pd.government_identifier as government_id,"
      + "coalesce(pd.email_id,ih.email_id) as recipient_email, "
      + "coalesce(pr.patient_id,isr.incoming_visit_id) as user_name, "
      + "pr.docs_download_passcode::text as pass_word,pr.visit_type as visit_type, "
      + "pd.patient_phone as recipient_mobile, pd.mr_no as mr_no,tvr.user_name, "
      + "coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('S','B') then 'Y' else 'N' end , CASE WHEN "
      + "(cf.receive_communication in ('S','B') OR cf.receive_communication is null) "
      + " then 'Y' else 'N' end) as send_sms, "
      + " coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('E','B') then 'Y' else 'N' end ,CASE WHEN "
      + "(cf.receive_communication in ('E','B')  OR cf.receive_communication is null)"
      + " then 'Y' else 'N' end) as send_email,pcpref.message_group_name,"
      + "hcm.center_name,hcm.center_code,hcm.center_contact_phone AS center_phone,"
      + "hcm.center_address, d.doctor_name,"
      + "d.doctor_mail_id as prescribing_doctor_email,ref.ref_email_id as referral_email,"
      + "tvr.category , "
      + " (select message_footer from message_types where message_type_id='sms_report_ready')"
      + " as message_footer, " + " case when cf.lang_code is not null then cf.lang_code else"
      + " (select contact_pref_lang_code from generic_preferences) end as lang_code "
      + " FROM test_visit_reports tvr "
      + " LEFT JOIN ( SELECT (SELECT report_id  FROM tests_prescribed"
      + " WHERE report_id = tppresc.report_id LIMIT 1 ) as report_id,"
      + "     ( SELECT pres_doctor  FROM tests_prescribed WHERE"
      + "         report_id = tppresc.report_id LIMIT 1 ) as pres_doctor "
      + "     FROM ( SELECT DISTINCT report_id,pres_doctor FROM tests_prescribed"
      + " where pres_doctor IS NOT NULL AND report_id in #) as tppresc )"
      + " as tp USING(report_id)"
      + " LEFT JOIN patient_registration pr ON(tvr.patient_id = pr.patient_id)"
      + " LEFT JOIN incoming_sample_registration isr ON(tvr.patient_id=isr.incoming_visit_id)"
      + " LEFT JOIN patient_details pd ON(COALESCE(pr.mr_no, isr.mr_no) = pd.mr_no)"
      + " LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)"
      + " LEFT JOIN incoming_hospitals ih ON(isr.orig_lab_name=ih.hospital_id)"
      + " LEFT JOIN contact_preferences cf ON(pd.mr_no = cf.mr_no) "
      + " left join patient_communication_preferences pcpref on (pd.mr_no = pcpref.mr_no and"
      + " pcpref.message_group_name='@')"
      + " LEFT JOIN hospital_center_master hcm "
      + " ON(hcm.center_id = coalesce(pr.center_id,isr.center_id))"
      + " LEFT JOIN doctors d ON (tp.pres_doctor=d.doctor_id) "
      + " LEFT JOIN (SELECT doctor_id AS ref_id, doctor_name AS"
      + " ref_name,doctor_mail_id as ref_email_id" + " FROM doctors"
      + " UNION SELECT referal_no AS ref_id, referal_name AS ref_name,"
      + "referal_doctor_email as ref_email_id" + " FROM referral ORDER BY ref_name"
      + " ) as ref ON(ref.ref_id=coalesce(pr.reference_docto_id,isr.referring_doctor))"
      + " WHERE report_id in # AND tvr.signed_off = 'Y' order by tvr.report_date ) as foo ";

  /**
   * Instantiates a new diag report data provider.
   */
  public DiagReportDataProvider() {
    super(THIS_NAME);
  }

  /** The tokens. */
  String[] tokens = new String[] { "receipient_name", "recipient_email", "recipient_mobile",
      "_report_content", "category", "center_address", "center_code", "center_name", "center_phone",
      "diag_report_name", "message_attachment_name", "message_footer", "mr_no", "pass_word",
      "prescribing_doctor_email", "referral_email", "report_date", "report_date_yyyy_mm_dd",
      "report_time", "report_time_12hr", "salutation_name", "user_name", "lang_code","test_name",
      "result_name","report_value","units","reference_range","doctor_name","severity_status",
      "government_id","government_id_label","signed_off_date","visit_type"};

  /**
   * @see com.insta.hms.messaging.providers.QueryDataProvider#getTokens()
   */
  @Override
  public List<String> getTokens() throws SQLException {
    List<String> tokenList = new ArrayList<String>(Arrays.asList(tokens));
    Collections.sort(tokenList);
    return tokenList;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Map> getMessageDataList(MessageContext ctx) throws SQLException, ParseException {
    Map eventData = ctx.getEventData();
    String[] reportId = null;
    Integer reportIdInt = null;
    // This will be available only if the message type is sms_report_ready
    if (null != eventData) {
      Map filter = new HashMap();
      if (null != eventData.get("handoverreadyreports")) {
        reportId = (String[]) eventData.get("handoverreadyreports");
        String reportIdStr = Arrays.toString(reportId);
        reportIdStr = reportIdStr.replace("[", "").replace("]", "");
        reportIdInt = Integer.parseInt(reportIdStr);
        fromTables = fromTables.replace("#", "(" + reportIdStr + ")");
      }
    } else {
      fromTables = fromTables.replace("report_id in #", "true");
    }
    GenericDAO messageTypesDao = new GenericDAO("message_types");
    BasicDynaBean messageTypeBean = messageTypesDao.findByKey("message_type_id",
        (String) ctx.getMessageType().get("message_type_id"));
    String messageGroupName = (String) messageTypeBean.get("message_group_name");
    fromTables = fromTables.replace("@", messageGroupName);
    setQueryParams(selectFields, selectCount, fromTables, null);

    // This will be available only if the message type is email_diag_report

    Map configParams = ctx.getConfigParams();

    if (null != configParams && messageTypeBean.get("event_id").equals("ui_trigger")) {
      java.util.Date endDate = DateUtil.getCurrentDate();
      java.util.Date startDate = DateUtil.getCurrentDate();

      String duePeriod = (String) configParams.get("ready_period");
      if (null != duePeriod && !duePeriod.isEmpty()) {
        Integer dueDays = Integer.parseInt(duePeriod);
        if (dueDays > 0) {
          java.util.Date refDate = (java.util.Date) DateUtil.getExpectedDate(dueDays, "D", true,
              true);
          startDate = new java.sql.Date(refDate.getTime());
        }
      }

      Map dateFilter = new HashMap();
      DateUtil dateUtil = new DateUtil();
      String strStartDate = dateUtil.getSqlDateFormatter().format(startDate);
      String strEndDate = dateUtil.getSqlDateFormatter().format(endDate);
      dateFilter.put("report_date_search__", new String[] { strStartDate, strEndDate });
      dateFilter.put("report_date_search__@op", new String[] { "ge,le" });
      dateFilter.put("report_date_search__@type", new String[] { "text" });
      dateFilter.put("report_date_search__@cast", new String[] { "y" });

      logger.debug("dates :" + strStartDate + "::" + strEndDate);
      addCriteriaFilter(dateFilter);
    }
    
    String messageMode = (String) messageTypeBean.get("message_mode");
    String mode = messageMode.equalsIgnoreCase("sms") ? "sms" : "email";
    Map modeFilter = new HashMap();
    modeFilter.put("send_" + mode, new String[] { "Y" });
    modeFilter.put("send_" + mode + "@type", new String[] { "text" });
    addCriteriaFilter(modeFilter);

    List<Map> messageDataList = new ArrayList<Map>();
    List<Map> unModifiableMessageDataList = super.getMessageDataList(ctx);
    if (reportIdInt == null) {
      messageDataList = unModifiableMessageDataList;
    } else {
      List resultLabels = new ArrayList();
      List reportValues = new ArrayList();
      List units = new ArrayList();
      List referenceRanges = new ArrayList();
      List<BasicDynaBean> labResultList = LaboratoryDAO.getTestResultByReportId(reportIdInt);
      for (BasicDynaBean labResult : labResultList) {
        resultLabels.add((String) labResult.get("resultlabel"));
        reportValues.add((String) labResult.get("report_value"));
        units.add((String) labResult.get("units"));
        referenceRanges.add((String) labResult.get("reference_range"));
      }
      RegistrationPreferencesDAO registrationPref = new RegistrationPreferencesDAO();
      String govIdLabel = (String) registrationPref
          .getColumnList("government_identifier_label").get(0);
      Map labResultTokens = new HashMap();
      labResultTokens.putAll(unModifiableMessageDataList.get(0));
      labResultTokens.put("test_name", labResultList.size() != 0 ? (String) labResultList
          .get(0).get("test_name") : "");
      labResultTokens.put("result_name", resultLabels);
      labResultTokens.put("report_value", reportValues);
      labResultTokens.put("units", units);
      labResultTokens.put("severity_status", labResultList.size() != 0 ? (String) labResultList
          .get(0).get("severity_status") : "");
      labResultTokens.put("reference_range", referenceRanges);
      labResultTokens.put("government_id_label", govIdLabel);

      messageDataList.add(labResultTokens);
    }
    List<Map> messageDataListWithReportContent = new ArrayList<Map>();
    // test_visit_reports.report_data is no more an available column.Where ever report is required
    // should generate afresh
    try {
      for (int i = 0; i < messageDataList.size(); i++) {
        Map item = new HashMap<Object, Object>();
        item.putAll(messageDataList.get(i));
        if (eventData == null) {
          eventData = new HashMap<>();
          String contextPath = RequestContext.getRequest().getServletContext().getRealPath("");
          eventData.put("path", contextPath);
        }
        String report = DiagReportGenerator.getReport((Integer) item.get("key"),
            (String) item.get("user_name"), (String) eventData.get("path"), true, false);
        item.put("_report_content", report);
        HtmlConverter hc = null;
        if (configParams != null && configParams.get("pdf_encryption") != null
            && configParams.get("pdf_encryption").equals("Y")) {
          hc = new HtmlConverter(new DiagImageRetriever(),
              PdfUtils.generateUserPdfPassword((String) item.get("mr_no")));
        } else {
          hc = new HtmlConverter(new DiagImageRetriever());
        }
        byte[] reportContent = LaboratoryBO.convertReportHtmlToPdf((Integer) item.get("key"),
            report, hc);
        item.put("_message_attachment", reportContent);
        messageDataListWithReportContent.add(item);
      }
    } catch (Exception ex) {
      logger.error("", ex);
    }
    return messageDataListWithReportContent;
  }
}
