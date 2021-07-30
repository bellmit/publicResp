package com.insta.hms.core.clinical.dischargesummary;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author anup vishwas.
 *
 */

@Repository
public class DischargeHeaderRepository extends GenericRepository {

  public DischargeHeaderRepository() {
    super("dis_header");
  }

  private static final String GET_DOC_FORM =
      " SELECT dh.username FROM dis_header dh WHERE dh.docid = ?";

  /**
   * Gets doc form.
   * @param docId the doc id
   * @return the bean
   */
  public BasicDynaBean getDocForm(int docId) {
    List list = DatabaseHelper.queryToDynaList(GET_DOC_FORM, new Object[] {docId});
    if (list.size() > 0) {
      return (BasicDynaBean) list.get(0);
    }
    return null;
  }

  private static final String FOLLOWUP_DISCHARGE_REPORT_QUERY =
      "SELECT dh.patient_id, FUD.FOLLOWUP_ID, "
          + " TO_CHAR(FOLLOWUP_DATE,'DD-MM-YYYY') AS FOLLOWUP_DATE, FUD.FOLLOWUP_DOCTOR_ID, "
          + " DOC.DOCTOR_NAME as followup_doctorname, FUD.FOLLOWUP_REMARKS, "
          + " COALESCE(fh.form_title,fh.form_caption) as form_title, "
          + " doc1.doctor_name as discharge_doctor, "
          + " doc1.specialization as discharge_doctor_specialization "
          + " FROM dis_header dh left outer "
          + " JOIN FOLLOW_UP_DETAILS FUD  on (dh.patient_id = fud.patient_id) "
          + " left outer JOIN DOCTORS DOC ON DOC.doctor_id = FUD.FOLLOWUP_DOCTOR_ID "
          + " left outer join form_header fh on (dh.form_id = fh.form_id) "
          + " left outer join patient_registration dis on (dh.patient_id = dis.patient_id) "
          + " left outer join doctors doc1 on (dis.DISCHARGE_DOCTOR_ID = doc1.doctor_id) "
          + " WHERE dh.PATIENT_ID=? and dh.docid = ? ORDER BY FUD.FOLLOWUP_DATE ASC";

  /**
   * Gets follow up and discharge details.
   * 
   * @param patientId the visit id
   * @param docId the doc id
   * @return list of beans
   */
  public List<BasicDynaBean> getFollowupAndDischargeDetails(String patientId, int docId) {

    return DatabaseHelper.queryToDynaList(FOLLOWUP_DISCHARGE_REPORT_QUERY,
        new Object[] {patientId, docId});
  }

}
