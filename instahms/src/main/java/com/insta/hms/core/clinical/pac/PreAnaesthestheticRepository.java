package com.insta.hms.core.clinical.pac;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class PreAnaesthestheticRepository.
 *
 * @author teja
 */
@Repository
public class PreAnaesthestheticRepository extends GenericRepository {

  /**
   * Instantiates a new pre anaesthesthetic repository.
   */
  public PreAnaesthestheticRepository() {
    super("patient_pac");
  }

  /** The Constant ALL_PAC_RECORDS. */
  private static final String ALL_PAC_RECORDS =
      " SELECT ppac.patient_pac_id, ppac.doctor_id, ppac.status, ppac.patient_pac_remarks,"
          + " ppac.pac_date, ppac.pac_validity, ppac.username, d.doctor_name,"
          + " psd.section_detail_id, psd.section_id, psd.finalized "
          + "FROM patient_section_details psd "
          + "LEFT JOIN patient_pac ppac ON (ppac.section_detail_id = psd.section_detail_id) "
          + "LEFT JOIN doctors d ON (ppac.doctor_id=d.doctor_id) "
          + "LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
          + "WHERE psd.mr_no = ? AND psd.patient_id = ? AND #filter#=? AND item_type=?"
          + " AND psd.section_id = -16 " + "ORDER BY ppac.pac_validity desc";

  /**
   * Gets the pre anaesthestheticdetails.
   *
   * @param mrNo the mr no
   * @param visitId the visit id
   * @param id the id
   * @param itemType the item type
   * @param formKeyField the form key field
   * @return the pre anaesthestheticdetails
   */
  public List<BasicDynaBean> getPreAnaesthestheticdetails(String mrNo, String visitId, Object id,
      String itemType, String formKeyField) {
    return DatabaseHelper.queryToDynaList(
        ALL_PAC_RECORDS.replace("#filter#", "psd." + formKeyField),
        new Object[] {mrNo, visitId, id, itemType});
  }

  /** The Constant ALL_ACTIVE_RECORDS. */
  private static final String ALL_ACTIVE_RECORDS = " SELECT "
      + " ppac.patient_pac_id as null, ppac.doctor_id, ppac.status, ppac.patient_pac_remarks,"
      + " ppac.pac_date, ppac.pac_validity, ppac.username, d.doctor_name "
      + "FROM patient_pac ppac " + "LEFT JOIN doctors d ON (ppac.doctor_id=d.doctor_id) "
      + "JOIN patient_section_details psd ON (ppac.section_detail_id = psd.section_detail_id) "
      + "LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
      + "WHERE psd.mr_no = ? AND psd.section_id = -16 AND psd.section_status = 'A' "
      + "ORDER BY ppac.pac_validity desc ";

  /**
   * Gets the active pre anaesthestheticdetails.
   *
   * @param mrNo the mr no
   * @return the active pre anaesthestheticdetails
   */
  public List<BasicDynaBean> getActivePreAnaesthestheticdetails(String mrNo) {
    return DatabaseHelper.queryToDynaList(ALL_ACTIVE_RECORDS, new Object[] {mrNo});
  }

  /** The Constant GET_ALL_PAC_RECORDS. */
  private static final String GET_ALL_PAC_RECORDS =
      " SELECT ppac.*, d.doctor_name, psd.mr_no, psd.finalized, psd.finalized_user,"
          + " usr.emp_username, usr.temp_username FROM patient_pac ppac "
          + " LEFT JOIN doctors d ON (ppac.doctor_id=d.doctor_id)"
          + " JOIN patient_section_details psd ON (ppac.section_detail_id = psd.section_detail_id)"
          + " JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id) "
          + "   LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
          + " WHERE psd.mr_no = ? AND psd.patient_id = ? AND coalesce(psd.section_item_id, 0)=? "
          + " AND coalesce(psd.generic_form_id, 0) = ? AND item_type=? AND psd.section_id = -16"
          + " AND psf.form_id=? ORDER BY ppac.pac_validity desc";

  /**
   * Gets the all PAC records.
   *
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param itemId the item id
   * @param genericFormId the generic form id
   * @param formId the form id
   * @param itemType the item type
   * @return the all PAC records
   */
  public List<BasicDynaBean> getAllPACRecords(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) {
    return DatabaseHelper.queryToDynaList(GET_ALL_PAC_RECORDS,
        new Object[] {mrNo, patientId, itemId, genericFormId, itemType, formId});
  }
}
