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

public class PatientListDataProvider extends QueryDataProvider {

  static Logger logger = LoggerFactory
      .getLogger(PatientListDataProvider.class);

  private static final String THIS_NAME = "Patients";
  private static final String selectFields = "SELECT * ";
  private static final String selectCount = "SELECT COUNT(*)";
  private String fromTables = " from (SELECT pd.mr_no as mr_no,"
      + "pd.mr_no as key,pd.visit_id as visit_id,"
      + " coalesce(hcm.center_name,'') as center_name, "
      + "coalesce(hcm.center_contact_phone,'') AS center_phone,"
      + "coalesce(hcm.center_address,'') as center_address, "
      + "pd.mr_no as receipient_id__ ,'PATIENT' as receipient_type__,'null' as patient_age,"
      + "coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('S','B') then 'Y' else 'N' end , CASE WHEN "
      + "(cf.receive_communication in ('S','B') OR cf.receive_communication is null) "
      + " then 'Y' else 'N' end) as send_sms, "
      + " coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('E','B') then 'Y' else 'N' end ,CASE WHEN "
      + "(cf.receive_communication in ('E','B')  OR cf.receive_communication is null)"
      + " then 'Y' else 'N' end) as send_email,pcpref.message_group_name,"
      + " coalesce(cf.promotional_consent,'N') as promotional_consent, "
      + "'null' as salutation_name,pd.patient_name as patient_name,"
      + "pd.patient_name as recipient_name,pd.email_id as email_id,"
      + "CASE WHEN pd.email_id = '' THEN NULL ELSE pd.email_id END as recipient_email,"
      + "CASE WHEN pd.patient_phone = '' THEN NULL ELSE pd.patient_phone END as recipient_mobile,"
      + "coalesce(pd.patient_phone, '') as patient_phone,"
      + "pd.patient_city as patient_city, pd.patient_state as patient_state,"
      + "pd.country as country,pd.country as patient_country, pd.patient_city as patient_city_name,"
      + "pd.custom_list4_value AS custom_list4, pd.first_visit_reg_date as first_visit_reg_date, "
      + "date(pd.first_visit_reg_date)::text as patient_visit_date, "
      + "to_char(pd.first_visit_reg_date, 'DD-MM-YYYY') as patient_visit_date_dd_mm_yyyy,"
      + "'null' as patient_visit_type,'null' as patient_status, 'null' as patient_doctor,"
      + "'null' as patient_complaint,'null' as patient_dept,"
      + "pd.patient_name||' '||coalesce(pd.middle_name,'')||' '||coalesce(pd.last_name,'')"
      + "  as patient_full_name," + "date_part('day', pd.dateofbirth)::text as patient_birth_day,"
      + "date_part('month', pd.dateofbirth)::text as patient_birth_month,"
      + "'null' as dept_id__, 'null' as doctor__ , "
      + "pd.patient_area as patient_area,pd.mobile_password as mobile_password,"
      + "case when mobile_access=true then 'Y' else 'N'end as mobile_access "
      + "from patient_details pd" + " LEFT JOIN patient_registration pr "
      + " ON(pr.patient_id=coalesce(pd.visit_id,pd.previous_visit_id)) "
      + " LEFT JOIN hospital_center_master hcm  ON(hcm.center_id = pr.center_id) "
      + "LEFT JOIN contact_preferences cf ON(pd.mr_no = cf.mr_no) "
      + "left join patient_communication_preferences pcpref on (pd.mr_no = pcpref.mr_no and"
      + " pcpref.message_group_name='#')"
      + " order by pd.mr_no) as foo";

  public PatientListDataProvider() {
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

    /*
     * if (centerID != 0 &&
     * GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) { Map map =
     * new HashMap(); map.put("center_id",new String[] {centerID.toString()});
     * addCriteriaFilter(map); }
     */

    if (eventData != null && eventData.containsKey("Search")) {
      eventData.put("Search", new String[1]);
      ctx.setEventData(eventData);
      addCriteriaFilter(eventData);
    }
    return super.getMessageDataList(ctx);
  }

}
