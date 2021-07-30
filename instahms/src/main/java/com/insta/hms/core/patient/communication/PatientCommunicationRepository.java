package com.insta.hms.core.patient.communication;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PatientCommunicationRepository extends GenericRepository {

  public PatientCommunicationRepository() {
    super("patient_communication_preferences");
  }

  private static final String PATIENT_MESSAGE_SPECIFIC_PREFERENCE = "select "
      + "mt.message_group_name,message_category_name, case when message_mode = 'SMS' "
      + "then 'S' else 'E' end as message_mode, coalesce(communication_type, "
      + "(select receive_communication from contact_preferences where mr_no = ?) , 'B') "
      + "as prefered_mode from message_types mt left join (select * from "
      + "patient_communication_preferences where mr_no = ?) as pcp ON"
      + " (mt.message_group_name = pcp.message_group_name) join message_category mc ON "
      + " (mt.category_id = mc.message_category_id)  where mt.message_mode in ('SMS','EMAIL')"
      + " and mt.recipient_category='Patient' and mt.status = 'A'";

  private static final String GET_PATIENT_COMMUNICATION_PREFERENCES = "select message_group_name"
      + " from patient_communication_preferences where mr_no= ? ";

  private static final String UPDATE_PREFERENCE = "update patient_communication_preferences "
      + " set communication_type= ? where message_group_name= ? AND mr_no= ? ";

  private static final String INSERT_PREFERENCE = "insert into "
      + "patient_communication_preferences "
      + "(communication_type, message_group_name, mr_no) values (?,?,?) ";

  public List<BasicDynaBean> getPatientMessagePreference(String mrNo) {
    return DatabaseHelper.queryToDynaList(PATIENT_MESSAGE_SPECIFIC_PREFERENCE, mrNo, mrNo);
  }

  public List<BasicDynaBean> getPatientCommunicationPrefs(String mrNo) {
    return DatabaseHelper.queryToDynaList(GET_PATIENT_COMMUNICATION_PREFERENCES, mrNo);
  }

  public void batchUpdatePrefs(List<Object[]> updateParamsList) {
    DatabaseHelper.batchUpdate(UPDATE_PREFERENCE, updateParamsList);
  }

  public void batchInsertPrefs(List<Object[]> insertParamsList) {
    DatabaseHelper.batchUpdate(INSERT_PREFERENCE, insertParamsList);
  }

}
