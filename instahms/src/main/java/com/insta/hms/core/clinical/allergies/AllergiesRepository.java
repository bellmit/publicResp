package com.insta.hms.core.clinical.allergies;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;


// TODO: Auto-generated Javadoc
/**
 * Allergies repo.
 * 
 * @author krishnat
 *
 */
@Repository
public class AllergiesRepository extends GenericRepository {

  /**
   * Instantiates a new allergies repository.
   */
  public AllergiesRepository() {
    super("patient_allergies");
  }

  /** The Constant GET_ACTIVE_ALLERGIES. */
  private static final String GET_ACTIVE_ALLERGIES =
      "SELECT pa.allergy_id, pa.reaction, "
          + " pa.onset_date,pa.severity, pa.status, pa.username, pa.created_at," 
          + " COALESCE(am.allergen_description,gn.generic_name) as allergy,"
          + " case when pa.allergy_type_id is null then 'N' else atm.allergy_type_code END"
          + " as allergy_type, atm.allergy_type_name, pa.allergen_code_id,"
          + " pa.allergy_type_id, psd.finalized "
          + " FROM patient_allergies pa "
          + " JOIN patient_section_details psd ON (pa.section_detail_id = psd.section_detail_id)"
          + " LEFT JOIN allergen_master am ON (pa.allergen_code_id=am.allergen_code_id)"
          + " LEFT JOIN allergy_type_master atm ON (atm.allergy_type_id = pa.allergy_type_id)"
          + " LEFT JOIN generic_name gn ON (pa.allergen_code_id = gn.allergen_code_id)"
          + " WHERE psd.mr_no = ? AND psd.section_id = -2 AND psd.section_status = 'A' "
          + " ORDER BY pa.status, atm.allergy_type_code";


  /** The Constant GET_ALLERGIES_RECORD. */
  private static final String GET_ALLERGIES_RECORD =
      "SELECT pa.allergy_id, pa.reaction, "
          + " pa.onset_date, pa.severity, pa.status, pa.username, pa.created_at, "
          + " COALESCE(am.allergen_description,gn.generic_name) as allergy,"
          + " case when pa.allergy_type_id is null and pa.allergy_id is not null"
          + " then 'N' else atm.allergy_type_code END as allergy_type, pa.allergen_code_id,"
          + " pa.allergy_type_id, atm.allergy_type_name,"
          + " psd.finalized, psd.section_detail_id, psd.section_id "
          + " FROM patient_section_details psd "
          + " JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id) "
          + " LEFT JOIN patient_allergies pa ON (pa.section_detail_id = psd.section_detail_id) "
          + " LEFT JOIN allergen_master am ON"
          + " (pa.allergen_code_id=am.allergen_code_id and pa.allergy_id is not null)"
          + " LEFT JOIN allergy_type_master atm ON"
          + " (atm.allergy_type_id = pa.allergy_type_id and pa.allergy_id is not null)"
          + " LEFT JOIN generic_name gn ON"
          + " (pa.allergen_code_id = gn.allergen_code_id and pa.allergy_id is not null)"
          + " LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
          + " WHERE psd.mr_no= ? AND psd.patient_id= ? AND #filter#=? "
          + " AND psd.item_type= ? AND psf.form_type= ? AND section_id=-2 "
          + " ORDER BY status, atm.allergy_type_code";

  /**
   * Gets the allergies.
   *
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param id the id
   * @param itemType the item type
   * @param formType the form type
   * @param formKeyField the form key field
   * @return the allergies
   */
  public List<BasicDynaBean> getAllergies(String mrNo, String patientId, Object id, String itemType,
      String formType, String formKeyField) {
    // There will be no section data carry forward in other form
    return DatabaseHelper.queryToDynaList(GET_ALLERGIES_RECORD.replace("#filter#", formKeyField),
        new Object[] {mrNo, patientId, id, itemType, formType});
  }

  /**
   * Gets the all active allergies.
   *
   * @param mrNo the mr no
   * @return the all active allergies
   */
  public List<BasicDynaBean> getAllActiveAllergies(String mrNo) {
    return DatabaseHelper.queryToDynaList(GET_ACTIVE_ALLERGIES, new Object[] {mrNo});
  }

  /** The Constant GET_ALL_ACTIVE_ALLERGIES. */
  // Used for Discharge summary and prints
  private static final String GET_ALL_ACTIVE_ALLERGIES =
      "SELECT  pa.severity, pa.reaction, "
          + " pa.onset_date, pa.status, atm.allergy_type_name, "
          + " COALESCE(am.allergen_description,gn.generic_name) as allergy,"
          + " case when pa.allergy_type_id is null then 'N' else atm.allergy_type_code END"
          + " as allergy_type, pa.allergen_code_id,"
          + " pa.allergy_type_id,"
          + " psd.mr_no, psd.section_item_id, psd.patient_id, psd.finalized, psd.finalized_user, "
          + " usr.temp_username FROM patient_allergies pa"
          + " JOIN patient_section_details psd ON (pa.section_detail_id=psd.section_detail_id) "
          + " JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id) "
          + " LEFT JOIN allergen_master am ON (pa.allergen_code_id=am.allergen_code_id)"
          + " LEFT JOIN allergy_type_master atm ON (atm.allergy_type_id = pa.allergy_type_id)"
          + " LEFT JOIN generic_name gn ON (pa.allergen_code_id = gn.allergen_code_id)"
          + " LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
          + " WHERE psd.mr_no=? AND psd.patient_id=? "
          + " AND coalesce(psd.section_item_id, 0)=? AND coalesce(psd.generic_form_id, 0)=?"
          + " AND psd.item_type=? AND section_id=-2 AND pa.status='A' AND psf.form_id=?"
          + " ORDER BY status, atm.allergy_type_code";

  /**
   * Gets the all active allergies.
   *
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param itemId the item id
   * @param genericFormId the generic form id
   * @param formId the form id
   * @param itemType the item type
   * @return the all active allergies
   */
  public List<BasicDynaBean> getAllActiveAllergies(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) {

    return DatabaseHelper.queryToDynaList(GET_ALL_ACTIVE_ALLERGIES,
        new Object[] {mrNo, patientId, itemId, genericFormId, itemType, formId});
  }

  /** The Constant GET_PATIENT_ALLERGIES. */
  private static final String GET_PATIENT_ALLERGIES =
      "SELECT pa.allergy_id, pa.reaction, "
          + " COALESCE(am.allergen_description,gn.generic_name) as allergy,"
          + " case when pa.allergy_type_id is null then 'N' else atm.allergy_type_code END"
          + " as allergy_type, atm.allergy_type_name, pa.allergen_code_id,"
          + " pa.allergy_type_id,"
          + " pa.onset_date, pa.severity, pa.status FROM patient_allergies pa "
          + " JOIN patient_section_details psd ON (pa.section_detail_id = psd.section_detail_id) "
          + " LEFT JOIN allergen_master am ON (pa.allergen_code_id=am.allergen_code_id)"
          + " LEFT JOIN allergy_type_master atm ON (atm.allergy_type_id = pa.allergy_type_id)"
          + " LEFT JOIN generic_name gn ON (pa.allergen_code_id = gn.allergen_code_id)"
          + " WHERE psd.mr_no = ? AND psd.section_id = -2 AND psd.section_status = 'A' "
          + " ORDER BY pa.status, atm.allergy_type_code";

  /**
   * Gets the patient recent allergies.
   *
   * @param mrNo the mr no
   * @return the patient recent allergies
   */
  public List<BasicDynaBean> getPatientRecentAllergies(String mrNo) {
    return DatabaseHelper.queryToDynaList(GET_PATIENT_ALLERGIES, new Object[] {mrNo});

  }
}
