package com.insta.hms.forms;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PatientFormDetailsRepository extends GenericRepository {

  public PatientFormDetailsRepository() {
    super("patient_form_details");
  }

  private static final String LIST_FORMS_BY_VISIT =
      "SELECT pfd.created_date, fc.form_name AS doc_name, pfd.mod_time AS mod_date,"
          + " pfd.user_name AS username, 'F' AS item_type, fc.doc_type, "
          + " dt.doc_type_name, fc.id AS form_id, ur.role_name, pfd.form_status AS save_status,"
          + " pfd.form_detail_id AS generic_form_id,"
          + " COALESCE(pfd.created_by, pfd.user_name) as created_by"
          + " FROM patient_form_details pfd"
          + " JOIN patient_registration pr USING (patient_id)"
          + " JOIN form_components fc ON (fc.id=pfd.form_master_id)"
          + " JOIN doc_type dt ON (dt.doc_type_id = fc.doc_type)"
          + " JOIN doc_type_category_mapping dtcm ON (dt.doc_type_id = dtcm.doc_type_id)"
          + " JOIN u_user uu ON (uu.emp_username = pfd.user_name)"
          + " JOIN u_role ur USING (role_id)"
          + " WHERE pr.patient_id=? AND pfd.form_type=? AND dtcm.doc_type_category_id = ? "
          + " AND pfd.form_status <> 'N' "
          + " ORDER BY created_date DESC";

  public List<BasicDynaBean> listPatientForms(String visitId, String formType, String docCategory) {
    List<BasicDynaBean> resultList = DatabaseHelper.queryToDynaList(LIST_FORMS_BY_VISIT,
        new Object[] {visitId, formType, docCategory});
    if (resultList == null) {
      return Collections.emptyList();
    }
    return resultList;
  }
  
  private static final String GEN_FORM_SUMMARY = "SELECT fc.form_name, pfd.mod_time, pfd.revision_number"
      + " FROM patient_form_details pfd" 
      + " JOIN form_components fc ON (fc.id=pfd.form_master_id) "
      + " WHERE pfd.form_detail_id = ? AND pfd.form_type='Form_Gen'";

  public BasicDynaBean getGenericFormSummary(int genFormId) {
    BasicDynaBean genFormSummary =
        DatabaseHelper.queryToDynaBean(GEN_FORM_SUMMARY, new Object[] {genFormId});
    return genFormSummary;
  }
  
  public int getGenericFormIdNextVal() {
    return DatabaseHelper.getInteger("select nextval('generic_insta_form_seq')");
  }
  
  public BasicDynaBean findByFormId(int formDetailId, String formType) {
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("form_detail_id", formDetailId);
    filterMap.put("form_type", formType);
    return findByKey(filterMap);
  }
  public BasicDynaBean findByPatientId(String patientId, String formType) {
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("patient_id", patientId);
    filterMap.put("form_type", formType);
    return findByKey(filterMap);
  }

  /** query to get form information to populate segment data. */
  private static final String GET_FORM_SEGMENT_INFORMATION =
      "SELECT d.doctor_name, user_name, pfd.mod_time, pfd.patient_id, "
          + "pfd.mr_no, pfd.is_reopened" + "FROM patient_form_details pfd"
          + "JOIN patient_registration pr using (patient_id)"
          + "JOIN doctors d ON (d.doctor_id=pr.doctor)"
          + "WHERE form_detail_id=? AND form_type=? AND form_status='F'";

  /**
   * Get form segment information for OT, Generic Form.
   * @param formDetailId form identifier (operation_procedure_id, generic_form_id )
   * @param formType (Form_OT, Form_Gen)
   * @return
   */
  public BasicDynaBean getFormSegmentInformation(int formDetailId, String formType) {
    return DatabaseHelper
        .queryToDynaBean(GET_FORM_SEGMENT_INFORMATION, new Object[] {formDetailId, formType});
  }
}
