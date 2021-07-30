package com.insta.hms.core.clinical.antenatal;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.core.clinical.forms.FormParameter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class AntenatalRepository.
 *
 * @author anupvishwas
 */

@Repository
public class AntenatalRepository extends GenericRepository {

  /**
   * Instantiates a new antenatal repository.
   */
  public AntenatalRepository() {
    super(ANTENATAL_TABLE_NAME);
  }

  public static final String ANTENATAL_TABLE_NAME = "antenatal";

  public static final String ANTENATAL_ID = "antenatal_id";

  /** The Constant GET_ALL_ACTIVE_ANTENATAL. */
  private static final String GET_ALL_ACTIVE_ANTENATAL = "SELECT al.antenatal_id,"
      + " al.visit_date, al.gestation_age, al.height_fundus, al.presentation,"
      + " al.rel_pp_brim, al.foetal_heart, al.urine, al.weight, al.prescription_summary,"
      + " al.next_visit_date, al.username, al.systolic_bp, al.diastolic_bp,al.movement,"
      + " al.position,"
      + " d.doctor_id, d.doctor_name, 0 as section_detail_id, psd.section_id, psd.finalized, "
      + " am.antenatal_main_id, am.lmp, am.edd, am.final_edd, am.close_pregnancy,"
      + " coalesce(am.pregnancy_result, 'Delivery') as pregnancy_result,"
      + " am.pregnancy_result_date, am.number_of_birth, am.pregnancy_count, am.remarks"
      + " FROM antenatal_main am"
      + " JOIN antenatal al ON (al.antenatal_main_id = am.antenatal_main_id)"
      + " JOIN patient_section_details psd ON (am.section_detail_id = psd.section_detail_id)"
      + " LEFT JOIN doctors d ON (al.doctor_id = d.doctor_id)"
      + " WHERE psd.mr_no = ? AND psd.section_id = -14 AND psd.section_status = 'A'"
      + " ORDER BY am.pregnancy_count, al.visit_date, al.antenatal_id asc";

  /**
   * Gets the all active antenatal.
   *
   * @param mrNo
   *          the mr no
   * @return the all active antenatal
   */
  public List<BasicDynaBean> getAllActiveAntenatal(String mrNo) {

    return DatabaseHelper.queryToDynaList(GET_ALL_ACTIVE_ANTENATAL, mrNo);
  }

  /** The Constant GET_ALL_ANTENATAL. */
  private static final String GET_ALL_ANTENATAL = "SELECT al.antenatal_id,"
      + " al.visit_date, al.gestation_age, al.height_fundus, al.presentation,"
      + " al.rel_pp_brim, al.foetal_heart, al.urine, al.weight, al.prescription_summary,"
      + " al.next_visit_date, al.username, al.systolic_bp, al.diastolic_bp,al.movement,"
      + " al.position, am.antenatal_main_id, am.lmp, am.edd, am.final_edd, am.close_pregnancy,"
      + " coalesce(am.pregnancy_result, 'Delivery') as pregnancy_result,"
      + " am.pregnancy_result_date, am.number_of_birth, am.pregnancy_count, am.remarks,"
      + " d.doctor_id, d.doctor_name, psd.section_detail_id, psd.section_id, psd.finalized "
      + " FROM patient_section_details psd"
      + " JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id)"
      + " LEFT JOIN antenatal_main am ON (am.section_detail_id = psd.section_detail_id)"
      + " JOIN antenatal al ON (al.antenatal_main_id = am.antenatal_main_id)"
      + " LEFT JOIN doctors d ON (d.doctor_id = al.doctor_id)"
      + " WHERE psd.patient_id = ? AND #filter#=? AND item_type=?"
      + " AND psd.section_id = -14 AND psf.form_type=?"
      + " ORDER BY am.pregnancy_count, al.visit_date, al.antenatal_id asc";

  /**
   * Gets the all antenatal.
   *
   * @param parameter
   *          the parameter
   * @return the all antenatal
   */
  public List<BasicDynaBean> getAllAntenatal(FormParameter parameter) {

    return DatabaseHelper.queryToDynaList(
        GET_ALL_ANTENATAL.replace("#filter#", parameter.getFormFieldName()),
        parameter.getPatientId(), parameter.getId(), parameter.getItemType(),
        parameter.getFormType());
  }

  /** The Constant GET_ALL_ANTENATAL_DETAILS. */
  private static final String GET_ALL_ANTENATAL_DETAILS = " SELECT al.antenatal_id, "
      + " al.visit_date , al.gestation_age, al.height_fundus, al.presentation, al.rel_pp_brim, "
      + " al.foetal_heart, al.urine, al.weight, al.prescription_summary, al.doctor_id, "
      + " al.next_visit_date, al.mod_time, al.username, al.systolic_bp, al.diastolic_bp, "
      + " al.movement, al.position, al.antenatal_main_id, "
      + " alm.section_detail_id, alm.lmp, alm.edd, alm.final_edd, "
      + " coalesce(alm.pregnancy_result, 'Delivery') as pregnancy_result, "
      + " alm.pregnancy_result_date, alm.number_of_birth, alm.pregnancy_count, alm.remarks, "
      + " alm.created_by, alm.modified_by, alm.modified_at, "
      + " alm.pregnancy_count::text as pregnancy_count_key, " + " d.doctor_name, "
      + " psd.mr_no, psd.finalized, psd.finalized_user, usr.temp_username "
      + " FROM antenatal_main alm "
      + " JOIN antenatal al ON (alm.antenatal_main_id = al.antenatal_main_id) "
      + "   LEFT JOIN doctors d ON (al.doctor_id=d.doctor_id)"
      + "   JOIN patient_section_details psd ON (alm.section_detail_id = psd.section_detail_id) "
      + " JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id) "
      + "   LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
      + "   WHERE psd.mr_no = ? AND psd.patient_id = ? AND coalesce(psd.section_item_id, 0)=? "
      + " AND coalesce(psd.generic_form_id, 0)=? AND item_type=? AND psd.section_id = -14 "
      + "   AND psf.form_id=? " + " ORDER BY al.visit_date";

  /**
   * Gets the all antenatal details.
   *
   * @param mrNo
   *          the mr no
   * @param patientId
   *          the patient id
   * @param itemId
   *          the item id
   * @param genericFormId
   *          the generic form id
   * @param itemType
   *          the item type
   * @param formId
   *          the form id
   * @return the all antenatal details
   */
  public List<BasicDynaBean> getAllAntenatalDetails(String mrNo, String patientId, int itemId,
      int genericFormId, String itemType, int formId) {
    Object[] tableField = new Object[] { mrNo, patientId, itemId, genericFormId, itemType, formId };
    return DatabaseHelper.queryToDynaList(GET_ALL_ANTENATAL_DETAILS, tableField);

  }

}
