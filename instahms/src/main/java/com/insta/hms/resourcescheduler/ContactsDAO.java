package com.insta.hms.resourcescheduler;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

public class ContactsDAO extends GenericDAO{

  public ContactsDAO() {
    super("contact_details");
  }
  
  private static final String CHECK_IF_CONTACT_EXISTS = 
      "select * from contact_details where CASE WHEN (middle_name IS NULL OR middle_name = '')"
          + " THEN trim(CONCAT(trim(lower(COALESCE(patient_name,''))),' ',trim(lower(COALESCE(last_name,'')))))"
          + " ELSE trim(CONCAT(trim(lower(COALESCE(patient_name,''))),' ',trim(lower(COALESCE(middle_name,''))),' ',trim(lower(COALESCE(last_name,''))))) "
          + " END = ?"
          + " AND patient_contact = ?";
  
  public static Integer getContactIdIfContactExists(BasicDynaBean bean) {
    String query = CHECK_IF_CONTACT_EXISTS;
    String fullName = (String)bean.get("patient_name");
    String patientContact = (String)bean.get("patient_contact");
    BasicDynaBean contactBean = DatabaseHelper.queryToDynaBean(query, new Object[]{fullName.trim().toLowerCase(),patientContact});
    if (contactBean != null) {
      return (Integer) contactBean.get("contact_id");
    }
    return null;
  }
}
