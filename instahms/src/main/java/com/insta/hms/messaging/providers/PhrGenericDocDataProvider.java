package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhrGenericDocDataProvider extends QueryDataProvider {
  static Logger logger = LoggerFactory
      .getLogger(PhrGenericDocDataProvider.class);
  private static String THIS_NAME = "PHR Document Upload Finalization ";
  private static final String selectFields = "SELECT * ";
  private static final String selectCount = "SELECT COUNT(*)";
  private static final String fromTables = "FROM (select pgd.doc_id,"
      + " dt.doc_type_name as record_type, pgd.doc_name, to_char(pgd.doc_date,'YYYY-MM-DD')"
      + " as doc_date, pgd.patient_id as visit_id, "
      + " to_char((pr.reg_date + pr.reg_time) AT TIME ZONE (SELECT  current_setting('TIMEZONE'))"
      + " AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as visit_date,"
      + " sm.salutation||' '||pd.patient_name||' '||coalesce(pd.middle_name,'')||' '||"
      + "coalesce(pd.last_name,'')  as patient_full_name, "
      + " to_char(pdm.mod_time AT TIME ZONE (SELECT  current_setting('TIMEZONE')) "
      + "AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS mod_time, "
      + " pd.patient_phone as patient_phone, "
      + " pd.patient_gender, to_char(pd.dateofbirth,'YYYY-MM-DD') AS patient_dateofbirth,"
      + " pd.email_id as patient_email, "
      + " get_patient_age(pd.dateofbirth, pd.expected_dob) AS patient_age,"
      + " get_patient_age_in(pd.dateofbirth, pd.expected_dob) AS patient_age_unit, "
      + " pd.mr_no,(select current_schema()) as group_id,hcm.center_id, hcm.center_name,"
      + "hcm.center_code,hcm.center_contact_phone AS center_phone, "
      + " hcm.center_address,c.city_name as center_city, s.state_name as center_state,"
      + " hcmd.center_name as default_center_name,hcmd.center_code as default_center_code, "
      + " hcmd.center_contact_phone AS default_center_phone,hcmd.center_address as"
      + " default_center_address,dc.city_name as default_center_city,"
      + " ds.state_name as default_center_state, "
      + " ref.ref_id, ref.ref_name, ref.ref_email_id as referral_email,"
      + " d.doctor_id as presc_doc_id, d.doctor_name as presc_doc_name, d.specialization,"
      + " pgd.doc_name as message_attachment_name, "
      + " 'phr_doc_upload' as category, pdc.content_type,"
      + " pdc.doc_content_bytea as _message_attachment, pd.mr_no as receipient_id__,"
      + " 'PATIENT' as receipient_type__ , (select hospital_name from generic_preferences)"
      + " as hospital_name  " + " from patient_general_docs pgd "
      + " JOIN patient_documents pdc using(doc_id) "
      + " JOIN doc_type dt on(dt.doc_type_id = pdc.doc_type) "
      + " LEFT JOIN patient_registration pr on(pgd.patient_id=pr.patient_id) "
      + " JOIN patient_details pd ON(pgd.mr_no = pd.mr_no) "
      + " LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id) "
      + " JOIN patient_demographics_mod pdm ON(pd.mr_no = pdm.mr_no) "
      + " LEFT JOIN hospital_center_master hcm  ON(hcm.center_id = pr.center_id) "
      + " LEFT JOIN city c ON (c.city_id=hcm.city_id) "
      + " LEFT JOIN state_master s ON (c.state_id=s.state_id) "
      + " LEFT JOIN hospital_center_master hcmd ON(hcmd.center_id = 0) "
      + " LEFT JOIN city dc ON (dc.city_id=hcmd.city_id) "
      + " LEFT JOIN state_master ds ON (dc.state_id=ds.state_id) "
      + " LEFT JOIN (SELECT doctor_id AS ref_id, doctor_name AS ref_name,"
      + "doctor_mail_id as ref_email_id FROM doctors "
      + " UNION SELECT referal_no AS ref_id, referal_name AS ref_name,"
      + "referal_doctor_email as ref_email_id FROM referral ORDER BY ref_name )"
      + " as ref ON(ref.ref_id=pr.reference_docto_id) "
      + " LEFT JOIN doctors d on(d.doctor_id = pr.doctor) "
      + " WHERE pdc.doc_status = 'F' order by pgd.doc_date ) as foo ";

  public PhrGenericDocDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null);
  }

  @Override
  public List<Map> getMessageDataList(MessageContext ctx) throws SQLException, ParseException {
    Map eventData = ctx.getEventData();

    if (null != eventData) {
      Map filter = new HashMap();
      if (null != eventData.get("doc_id")) {
        String[] reportId = (String[]) eventData.get("doc_id");
        filter.put("doc_id", reportId);
        filter.put("doc_id@type", new String[] { "integer" });
        filter.put("doc_id@cast", new String[] { "y" });
        filter.put("doc_id@op", new String[] { "in" });
      }
      addCriteriaFilter(filter);
    }

    List<Map> messageDataList = super.getMessageDataList(ctx);

    return messageDataList;
  }
}
