package com.insta.hms.core.clinical.complaints;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.core.clinical.forms.FormParameter;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Complaints repo.
 * 
 * @author anupvishwas
 *
 */

@Repository
public class ComplaintsRepository extends GenericRepository {

  public ComplaintsRepository() {
    super("secondary_complaints");
  }

  private static final String GET_COMPLAINTS_LIST =
      "SELECT -1 as section_id, " + " coalesce(psd.section_detail_id, 0) as section_detail_id, "
          + " case when coalesce(pr.complaint, '') = '' then null else "
          + " pr.complaint end as complaint, 0 as row_id, 'CC' as type "
          + " FROM patient_registration pr"
          + "  JOIN patient_section_details psd ON (psd.patient_id = pr.patient_id)"
          + "  JOIN patient_section_forms psf ON (psd.patient_id= ? and psd.item_type=? and "
          + "   #fitler_pri#=? and section_id=-1 and psf.form_type=? and "
          + "   psd.section_detail_id=psf.section_detail_id) " + " UNION "
          + " SELECT psd.section_id, psd.section_detail_id, sc.complaint, sc.row_id, 'SC' as type "
          + " FROM patient_section_details psd "
          + "  JOIN patient_section_forms psf ON (psd.patient_id= ? and psd.item_type=? "
          + "   and section_id=-1 and psf.form_type=? and "
          + "   #fitler_sec#=? and psd.section_detail_id=psf.section_detail_id) "
          + "  JOIN secondary_complaints sc ON (sc.visit_id = psd.patient_id) "
          + " ORDER BY row_id ";

  /**
   * Gets the complaint list.
   *
   * @param parameter the parameter
   * @return the complaint list
   */
  public List<BasicDynaBean> getComplaintList(FormParameter parameter) {
    return DatabaseHelper.queryToDynaList(
        GET_COMPLAINTS_LIST.replace("#fitler_pri#", "psd." + parameter.getFormFieldName())
            .replace("#fitler_sec#", "psd." + parameter.getFormFieldName()),
        parameter.getPatientId(), parameter.getItemType(), parameter.getId(),
        parameter.getFormType(), parameter.getPatientId(), parameter.getItemType(),
        parameter.getFormType(), parameter.getId());
  }

  private static final String GET_ALL_ACTIVE_COMPLAINTS_LIST =
      "SELECT -1 as section_id, " + " 0 as section_detail_id, "
          + " 0 as revision_number, "
          + " case when coalesce(pr.complaint, '') = '' then null else "
          + " pr.complaint end as complaint, 0 as row_id, 'CC' as type "
          + " FROM patient_registration pr"
          + " WHERE pr.patient_id= ?"
          + "  UNION "
          + " SELECT -1 as section_id, 0 as section_detail_id, 0 as revision_number,"
          + " sc.complaint, sc.row_id, 'SC' as type"
          + " FROM secondary_complaints sc"
          + " WHERE sc.visit_id= ? ORDER BY row_id ";

  /**
   * Gets the all active complaint list.
   *
   * @param patientId the patient id
   * @return the all active complaint list
   */
  public List<BasicDynaBean> getAllActiveComplaintList(String patientId) {
    return DatabaseHelper.queryToDynaList(GET_ALL_ACTIVE_COMPLAINTS_LIST, patientId, patientId);
  }

  private static String GET_CHIEF_COMPLAINT =
      " SELECT pr.complaint FROM patient_registration pr WHERE pr.patient_id = ? ";

  /**
   * Gets the chief complaint.
   *
   * @param patientId the patient id
   * @return the chief complaint
   */
  public String getChiefComplaint(String patientId) {
    return DatabaseHelper.getString(GET_CHIEF_COMPLAINT, patientId);
  }

  private static String GET_SECONDARY_COMPLAINTS = " SELECT "
      + "textcat_linecat(sc.complaint) AS sec_complaint FROM secondary_complaints sc "
      + "  WHERE sc.visit_id = ? ";

  public String getSecondaryComplaints(String patientId) {
    return DatabaseHelper.getString(GET_SECONDARY_COMPLAINTS, patientId);
  }

}
