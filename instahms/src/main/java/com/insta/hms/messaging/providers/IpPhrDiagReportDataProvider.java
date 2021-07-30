package com.insta.hms.messaging.providers;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PdfUtils;
import com.insta.hms.diagnosticmodule.common.DiagReportGenerator;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryBO;
import com.insta.hms.imageretriever.DiagImageRetriever;
import com.insta.hms.messaging.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IpPhrDiagReportDataProvider extends QueryDataProvider {
  static Logger logger = LoggerFactory
      .getLogger(IpPhrDiagReportDataProvider.class);
  private static String THIS_NAME = "Report Ready Patients ";
  private static final String selectFields = "SELECT * ";
  private static final String selectCount = "SELECT COUNT(*)";
  private String fromTables = "FROM (SELECT tvr.report_id as key, tvr.report_name as "
      + "diag_report_name, tvr.report_id as entity_id, " + " '' as _report_content,tvr"
      + ".report_name as message_attachment_name, " + " '' as _message_attachment,to_char(tvr"
      + ".report_date, 'DD-MM-YYYY') as report_date, " + "to_char(tvr.report_date, "
      + "'HH24:MI:SS') as report_time, " + "pd.mr_no as receipient_id__, 'PATIENT' as "
      + "receipient_type__," + "to_char(tvr.report_date, 'HH12:MI AM') as report_time_12hr, "
      + "to_char(tvr.report_date  AT TIME ZONE (SELECT  current_setting('TIMEZONE')) AT TIME"
      + " ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as report_date_yyyy_mm_dd , "
      + "to_char(tvr.report_date, 'yyyy-MM-dd') as report_date_search__,  " + " coalesce"
      + "(to_char((pr.reg_date + pr.reg_time) AT TIME ZONE (SELECT  current_setting"
      + "('TIMEZONE')) AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"'), " + " to_char"
      + "(isr.date AT TIME ZONE (SELECT  current_setting('TIMEZONE')) AT TIME ZONE 'UTC', "
      + "'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"')) as visit_date, " + "coalesce(sm.salutation,'') as "
      + "salutation_name, coalesce(pd.patient_name,ih.hospital_name) as receipient_name, "
      + "coalesce(pd.email_id,ih.email_id) as patient_email, " + "(select tp.pat_id from "
      + "tests_prescribed tp where tp.report_id = tvr.report_id and tp.coll_prescribed_id IS "
      + "NULL  LIMIT 1) as user_name, pr.docs_download_passcode::text as pass_word, " + "pd"
      + ".patient_phone as recipient_mobile, pd.mr_no as mr_no,tvr.user_name, " + "sm"
      + ".salutation||' '||pd.patient_name||' '||coalesce(pd.middle_name,'')||' '||coalesce(pd"
      + ".last_name,'')  as patient_full_name, " + "to_char(pd.dateofbirth,'YYYY-MM-DD') AS "
      + "recipient_dateofbirth, pd.patient_gender as recipient_gender, get_patient_age(pd"
      + ".dateofbirth, pd.expected_dob) AS recipient_age, get_patient_age_in(pd.dateofbirth, "
      + "pd.expected_dob) AS recipient_age_unit, " + "to_char(pdm.mod_time AT TIME ZONE "
      + "(SELECT  current_setting('TIMEZONE')) AT TIME ZONE 'UTC', "
      + "'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS mod_time, " + "hcm.center_id, hcm.center_name,"
      + "hcm.center_code,hcm.center_contact_phone AS center_phone,hcm.center_address,c"
      + ".city_name as center_city, s.state_name as center_state, " + "hcmd.center_name as "
      + "default_center_name,hcmd.center_code as default_center_code,hcmd.center_contact_phone"
      + " AS default_center_phone,hcmd.center_address as default_center_address,dc.city_name "
      + "as default_center_city, ds.state_name as default_center_state, " + "d.doctor_mail_id "
      + "as prescribing_doctor_email,d.doctor_id, d.doctor_name,d.specialization,ref.ref_id, "
      + "ref.ref_name, ref.ref_email_id as referral_email,tvr.category, " + " (select "
      + "current_schema()) as group_id, (select hospital_name from generic_preferences) as "
      + "hospital_name " + " FROM test_visit_reports tvr " + " LEFT JOIN ( SELECT DISTINCT"
      + "(SELECT report_id  FROM tests_prescribed WHERE report_id = tppresc.report_id LIMIT 1 "
      + ") as report_id," + " ( SELECT pres_doctor  FROM tests_prescribed WHERE "
      + "report_id = tppresc.report_id LIMIT 1 ) as pres_doctor " + " FROM ( SELECT "
      + "DISTINCT report_id,pres_doctor FROM tests_prescribed where pres_doctor IS NOT NULL "
      + "AND report_id in # ) as tppresc ) as tp USING(report_id)" + " LEFT JOIN "
      + "patient_registration pr ON(pr.patient_id = (select pat_id from tests_prescribed where"
      + " report_id = tvr.report_id and coll_prescribed_id is null limit 1) )" + " LEFT JOIN "
      + "incoming_sample_registration isr ON(tvr.patient_id=isr.incoming_visit_id)" + " INNER "
      + "JOIN patient_details pd ON(COALESCE(pr.mr_no, isr.mr_no) = pd.mr_no)" + " INNER JOIN "
      + "patient_demographics_mod pdm ON(pd.mr_no = pdm.mr_no) " + " LEFT JOIN "
      + "salutation_master sm ON (pd.salutation = sm.salutation_id)" + " LEFT JOIN "
      + "incoming_hospitals ih ON(isr.orig_lab_name=ih.hospital_id)" + " LEFT JOIN "
      + "hospital_center_master hcm  ON(hcm.center_id = (select pr.center_id " + " FROM  "
      + "tests_prescribed tp " + " JOIN patient_registration pr on tp.pat_id = pr.patient_id "
      + " WHERE tp.coll_prescribed_id IS NULL AND tp.report_id = tvr.report_id LIMIT 1))"
      + " LEFT JOIN city c ON (c.city_id=hcm.city_id) " + " LEFT JOIN state_master s ON (c"
      + ".state_id=s.state_id) " + " LEFT JOIN hospital_center_master hcmd ON(hcmd.center_id ="
      + " 0)" + " LEFT JOIN city dc ON (dc.city_id=hcmd.city_id) " + " LEFT JOIN state_master "
      + "ds ON (dc.state_id=ds.state_id) " + " LEFT JOIN doctors d ON (tp.pres_doctor=d"
      + ".doctor_id) " + " LEFT JOIN (SELECT doctor_id AS ref_id, doctor_name AS ref_name,"
      + "doctor_mail_id as ref_email_id" + " FROM doctors" + " "
      + "UNION SELECT referal_no AS ref_id, referal_name AS ref_name,referal_doctor_email as "
      + "ref_email_id" + " FROM referral ORDER BY ref_name" + " ) as ref ON(ref"
      + ".ref_id=coalesce(pr.reference_docto_id,isr.referring_doctor)) " + " WHERE report_id "
      + "in # AND tvr.signed_off = 'Y' AND pr.patient_discharge_status='D' AND pr"
      + ".visit_type='i' " + " order by report_date_yyyy_mm_dd ) as foo ";

  public IpPhrDiagReportDataProvider() {
    super(THIS_NAME);
  }

  @Override
  public List<Map> getMessageDataList(MessageContext ctx) throws SQLException, ParseException {
    Map eventData = ctx.getEventData();
    String[] reportId = null;
    // This will be available only if the message type is sms_report_ready
    if (null != eventData && null != eventData.get("handoverreadyreports")) {
      reportId = (String[]) eventData.get("handoverreadyreports");
      String reportIdStr = Arrays.toString(reportId);
      reportIdStr = reportIdStr.replace("[", "").replace("]", "");
      fromTables = fromTables.replace("#", "(" + reportIdStr + ")");
    }
    setQueryParams(selectFields, selectCount, fromTables, null);

    // This will be available only if the message type is email_diag_report

    Map configParams = ctx.getConfigParams();

    if (null != configParams) {
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

    List<Map> messageDataList = super.getMessageDataList(ctx);
    List<Map> messageDataListWithReportContent = new ArrayList<Map>();

    // test_visit_reports.report_data is no more an available column.Where ever report is
    // required
    // should generate afresh
    try {
      for (int i = 0; i < messageDataList.size(); i++) {
        Map item = new HashMap<Object, Object>();
        item.putAll(messageDataList.get(i));
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