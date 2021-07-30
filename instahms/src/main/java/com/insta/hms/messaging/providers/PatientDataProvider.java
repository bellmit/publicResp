package com.insta.hms.messaging.providers;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.messaging.MessageContext;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientDataProvider extends QueryDataProvider {

  static Logger logger = LoggerFactory
      .getLogger(PatientDataProvider.class);

  private static final String THIS_NAME = "Patient_Visits";
  private static final String selectFields = "SELECT * ";
  private static final String selectCount = "SELECT COUNT(*)";
  private String fromTables = " from (SELECT p.mr_no as mr_no, p.patient_id as key,"
      + " p.patient_id as visit_id,  "
      + "p.mr_no as receipient_id__ , 'PATIENT' as receipient_type__, "
      + "p.age::text as patient_age, " + "salutation_name, p.patient_name as recipient_name, "
      + "CASE WHEN p.email_id = '' THEN NULL ELSE p.email_id END as recipient_email, "
      + "CASE WHEN p.patient_phone = '' THEN NULL ELSE p.patient_phone END as recipient_mobile, "
      + "coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('S','B') then 'Y' else 'N' end , CASE WHEN "
      + "(cf.receive_communication in ('S','B') OR cf.receive_communication is null) "
      + " then 'Y' else 'N' end) as send_sms, "
      + " coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('E','B') then 'Y' else 'N' end ,CASE WHEN "
      + "(cf.receive_communication in ('E','B')  OR cf.receive_communication is null)"
      + " then 'Y' else 'N' end) as send_email,pcpref.message_group_name,"
      + " coalesce(cf.promotional_consent,'N') as promotional_consent, "
      + "coalesce(p.patient_phone, p.patient_phone2, '') as patient_phone, "
      + "p.patient_city as patient_city, p.patient_state as patient_state, "
      + "p.country as patient_country, ct.city_name as patient_city_name, "
      + "p.custom_list4_value AS custom_list4, "
      + "date(p.visit_reg_date)::text as patient_visit_date, "
      + "to_char(p.visit_reg_date, 'DD-MM-YYYY') as patient_visit_date_dd_mm_yyyy, "
      + "p.visit_type as patient_visit_type, "
      + "p.status as patient_status, p.doctor_name as patient_doctor, "
      + "p.complaint as patient_complaint, p.dept_name as patient_dept, "
      + "p.patient_full_name as patient_full_name, "
      + "date_part('day', p.dateofbirth)::text as patient_birth_day, "
      + "date_part('month', p.dateofbirth)::text as patient_birth_month, "
      + "p.dept_id as dept_id__, p.doctor as doctor__, "
      + "p.patient_area as patient_area,mobile_password,"
      + "case when mobile_access=true then 'Y' else 'N'end as mobile_access, "
      + "p.center_id::text as center_id,  "
      + " coalesce(hcm.center_name,'') as center_name, coalesce(hcm.center_contact_phone,'')"
      + " AS center_phone,coalesce(hcm.center_address,'') as center_address "
      + "from all_visits_view p " + "LEFT OUTER JOIN city ct ON p.patient_city = ct.city_id "
      + " LEFT JOIN hospital_center_master hcm  ON(hcm.center_id = p.center_id) "
      + "LEFT JOIN contact_preferences cf ON(p.mr_no = cf.mr_no) "
      + "left join patient_communication_preferences pcpref on (p.mr_no = pcpref.mr_no and"
      + " pcpref.message_group_name='#')"
      + ") as foo";

  public PatientDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null);
  }

  @Override
  public List<Map> getMessageDataList(MessageContext ctx) throws SQLException, ParseException {
    GenericDAO messageTypesDao = new GenericDAO("message_types");
    BasicDynaBean messageTypeBean = messageTypesDao.findByKey("message_type_id",
        (String) ctx.getMessageType().get("message_type_id"));
    String messageGroupName = (String) messageTypeBean.get("message_group_name");
    fromTables = fromTables.replace("#", messageGroupName);
    setQueryParams(selectFields, selectCount, fromTables, null);
    
    GenericDAO messageCategoryDao = new GenericDAO("message_category");
    BasicDynaBean messageCategoryBean = messageCategoryDao.findByKey("message_category_id",
        (Integer) messageTypeBean.get("category_id"));
    String messageCategory = (String) messageCategoryBean.get("message_category_name");
    if (messageCategory != null 
        && (messageCategory.equals("Custom Promotional")
        || messageCategory.equals("Promotional"))) {
      Map promotionalFilter = new HashMap();
      promotionalFilter.put("promotional_consent", new String[] { "Y" });
      addCriteriaFilter(promotionalFilter);
    }
    
    String messageMode = (String) messageTypeBean.get("message_mode");
    String mode = messageMode.equalsIgnoreCase("sms") ? "sms" : "email";
    Map modeFilter = new HashMap();
    modeFilter.put("send_" + mode, new String[] { "Y" });
    modeFilter.put("send_" + mode + "@type", new String[] { "text" });
    addCriteriaFilter(modeFilter);
    
    Map eventData = ctx.getEventData();
    Integer centerId = RequestContext.getCenterId();
    if (null != eventData) { // this is sent when the search filter is applied.
      addCriteriaFilter(eventData);
    }
    // if (centerID != 0 &&
    // GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
    Map map = new HashMap();
    map.put("center_id", new String[] { centerId.toString() });
    addCriteriaFilter(map);
    // }
    return super.getMessageDataList(ctx);
  }
}
