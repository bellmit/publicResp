package com.insta.hms.messaging.providers;

import com.bob.hms.common.DateUtil;
import com.insta.hms.messaging.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhrDiagReportCancelDataProvider extends QueryDataProvider {
  static Logger logger = LoggerFactory
      .getLogger(PhrDiagReportCancelDataProvider.class);
  private static String THIS_NAME = "Report Ready Patients ";
  private static final String selectFields = "SELECT * ";
  private static final String selectCount = "SELECT COUNT(*)";
  private static final String fromTables = "FROM (SELECT tvr.report_id as key, "
      + "tvr.report_name as diag_report_name, "
      + "to_char(tvr.report_date, 'DD-MM-YYYY') as report_date, "
      + "to_char(tvr.report_date, 'HH24:MI:SS') as report_time, "
      + "date(tvr.report_date)::text as report_date_yyyy_mm_dd, "
      + "pd.mr_no as receipient_id__, 'PATIENT' as receipient_type__,"
      + "to_char(now() AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') "
      + "as cancellation_date,  "
      + "to_char((pr.reg_date + pr.reg_time) AT TIME ZONE (SELECT  current_setting('TIMEZONE')) "
      + "AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as visit_date, "
      + "coalesce(sm.salutation,'') as salutation_name, coalesce(pd.patient_name,ih.hospital_name) "
      + "as receipient_name, coalesce(pd.email_id,ih.email_id) as patient_email, "
      + "coalesce(pr.patient_id,isr.incoming_visit_id) as user_name, "
      + "pr.docs_download_passcode::text as pass_word, "
      + "pd.patient_phone as recipient_mobile, pd.mr_no as mr_no,tvr.user_name, "
      + "sm.salutation||' '||pd.patient_name||' '||coalesce(pd.middle_name,'')||' '||"
      + "coalesce(pd.last_name,'')  as patient_full_name, "
      + "to_char(pd.dateofbirth,'YYYY-MM-DD') AS recipient_dateofbirth, "
      + "pd.patient_gender as recipient_gender, get_patient_age(pd.dateofbirth, "
      + "pd.expected_dob) AS recipient_age, get_patient_age_in(pd.dateofbirth, "
      + "pd.expected_dob) AS recipient_age_unit, "
      + "to_char(pdm.mod_time AT TIME ZONE (SELECT  current_setting('TIMEZONE')) "
      + "AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS mod_time, "
      + "hcm.center_id, hcm.center_name,hcm.center_code,hcm.center_contact_phone AS center_phone,"
      + "hcm.center_address,c.city_name as center_city, s.state_name as center_state, "
      + "ref.ref_email_id as referral_email,tvr.category, "
      + "hcmd.center_name as default_center_name,hcmd.center_code as default_center_code,"
      + "hcmd.center_contact_phone AS default_center_phone,hcmd.center_address "
      + "as default_center_address,dc.city_name as default_center_city, ds.state_name "
      + "as default_center_state, " + " (select message_footer from message_types "
      + "where message_type_id='email_phr_diag_report_cancel')  as message_footer, "
      + " (select current_schema()) as group_id, (select hospital_name from generic_preferences) "
      + "as hospital_name  " + " FROM test_visit_reports tvr "
      + " LEFT JOIN patient_registration pr ON(tvr.patient_id = pr.patient_id)"
      + " LEFT JOIN incoming_sample_registration isr ON(tvr.patient_id=isr.incoming_visit_id)"
      + " LEFT JOIN patient_details pd ON(COALESCE(pr.mr_no, isr.mr_no) = pd.mr_no)"
      + " LEFT JOIN patient_demographics_mod pdm ON(pd.mr_no = pdm.mr_no) "
      + " LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)"
      + " LEFT JOIN incoming_hospitals ih ON(isr.orig_lab_name=ih.hospital_id)"
      + " LEFT JOIN hospital_center_master hcm  ON(hcm.center_id = (select pr.center_id "
      + "   FROM  tests_prescribed tp "
      + " JOIN patient_registration pr on tp.pat_id = pr.patient_id "
      + " WHERE tp.coll_prescribed_id IS NULL AND tp.report_id = tvr.report_id LIMIT 1))"
      + " LEFT JOIN city c ON (c.city_id=hcm.city_id) "
      + " LEFT JOIN state_master s ON (c.state_id=s.state_id) "
      + " LEFT JOIN hospital_center_master hcmd ON(hcmd.center_id = 0)"
      + " LEFT JOIN city dc ON (dc.city_id=hcmd.city_id) "
      + " LEFT JOIN state_master ds ON (dc.state_id=ds.state_id) "
      + " LEFT JOIN (SELECT doctor_id AS ref_id, doctor_name AS ref_name,doctor_mail_id "
      + "as ref_email_id" + "       FROM doctors"
      + "       UNION SELECT referal_no AS ref_id, referal_name AS ref_name,"
      + "       referal_doctor_email as ref_email_id" + "       FROM referral ORDER BY ref_name"
      + " ) as ref ON(ref.ref_id=coalesce(pr.reference_docto_id,isr.referring_doctor))"
      + " WHERE tvr.signed_off = 'N' AND "
      + "(CASE WHEN pr.visit_type='i' THEN pr.patient_discharge_status='D' ELSE true END) "
      + " AND  (select patient_due_amnt from diag_report_sharing_on_bill_payment "
      + "where report_id = tvr.report_id limit 1) IS NULL order by report_date_yyyy_mm_dd ) "
      + "as foo ";

  public PhrDiagReportCancelDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null);
  }

  @Override
  public List<Map> getMessageDataList(MessageContext ctx) throws SQLException, ParseException {
    Map eventData = ctx.getEventData();

    // This will be available only if the message type is sms_report_ready
    if (null != eventData) {
      Map filter = new HashMap();
      if (null != eventData.get("handoverreadyreports")) {
        String[] reportId = (String[]) eventData.get("handoverreadyreports");
        filter.put("key", reportId);
        filter.put("key@type", new String[] { "text" });
        filter.put("key@cast", new String[] { "y" });
      }
      addCriteriaFilter(filter);
    }

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

    // test_visit_reports.report_data is no more an available column.Where ever report is required
    // should generate afresh
    try {
      for (int i = 0; i < messageDataList.size(); i++) {
        Map item = new HashMap<Object, Object>();
        item.putAll(messageDataList.get(i));
        /*
         * String report = DiagReportGenerator.getReport((Integer)item.get("key"),
         * (String)item.get("user_name"), RequestContext.getHttpRequest().getContextPath(), false,
         * false); item.put("_report_content", report); item.put("_message_attachment", report);
         */
        messageDataListWithReportContent.add(item);
      }
    } catch (Exception ex) {
      logger.error("", ex);
      ex.printStackTrace();
    }
    return messageDataListWithReportContent;
  }
}
