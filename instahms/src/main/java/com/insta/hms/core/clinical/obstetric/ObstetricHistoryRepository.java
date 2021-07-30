package com.insta.hms.core.clinical.obstetric;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.core.clinical.forms.FormParameter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class ObstetricHistoryRepository.
 *
 * @author anupvishwas
 */

@Repository
public class ObstetricHistoryRepository extends GenericRepository {

  /**
   * Instantiates a new obstetric history repository.
   */
  public ObstetricHistoryRepository() {
    super("pregnancy_history");
  }

  /** The Constant GET_ALL_ACTIVE_OBSTETRIC_HISTORY. */
  private static final String GET_ALL_ACTIVE_OBSTETRIC_HISTORY = "SELECT ph.pregnancy_history_id,"
      + " ph.date, ph.weeks, ph.place, ph.method, ph.weight, ph.sex, ph.complications,"
      + " ph.feeding, ph.outcome, ph.username" + " FROM pregnancy_history ph"
      + " JOIN patient_section_details psd ON (ph.section_detail_id = psd.section_detail_id)"
      + " WHERE psd.mr_no = ? AND psd.section_id = -13 AND psd.section_status = 'A'"
      + " ORDER BY ph.date";

  /**
   * Gets the all active obs history.
   *
   * @param mrNo
   *          the mr no
   * @return the all active obs history
   */
  public List<BasicDynaBean> getAllActiveObsHistory(String mrNo) {
    return DatabaseHelper.queryToDynaList(GET_ALL_ACTIVE_OBSTETRIC_HISTORY, mrNo);
  }

  /** The Constant GET_OBSTETRIC_HISTORY. */
  private static final String GET_OBSTETRIC_HISTORY = "SELECT ph.pregnancy_history_id,"
      + " ph.date, ph.weeks, ph.place, ph.method, ph.weight, ph.sex, ph.complications,"
      + " ph.feeding, ph.outcome, ph.username, psd.finalized, psd.section_detail_id, psd.section_id"
      + " FROM patient_section_details psd"
      + " JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id)"
      + " LEFT JOIN pregnancy_history ph ON (ph.section_detail_id = psd.section_detail_id)"
      + " WHERE psd.patient_id = ? AND #filter#=?"
      + " AND psd.item_type= ? AND psf.form_type= ? AND section_id=-13" + " ORDER BY ph.date";

  /**
   * Gets the obs history.
   *
   * @param parameter
   *          the parameter
   * @return the obs history
   */
  public List<BasicDynaBean> getObsHistory(FormParameter parameter) {
    return DatabaseHelper.queryToDynaList(
        GET_OBSTETRIC_HISTORY.replace("#filter#", parameter.getFormFieldName()),
        parameter.getPatientId(), parameter.getId(), parameter.getItemType(),
        parameter.getFormType());
  }

  /** The Constant GET_ALL_OBSTETRIC_HISTORY. */
  private static final String GET_ALL_OBSTETRIC_HISTORY = "SELECT ph.*, "
      + "psd.mr_no, psd.finalized, psd.finalized_user, usr.temp_username "
      + " FROM pregnancy_history ph "
      + " JOIN patient_section_details psd ON (ph.section_detail_id = psd.section_detail_id) "
      + " JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id) "
      + " LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
      + " WHERE psd.mr_no = ? AND psd.patient_id = ? AND coalesce(psd.section_item_id, 0)=? "
      + " AND coalesce(psd.generic_form_id, 0)=? AND item_type=? AND psd.section_id = -13 "
      + " AND  psf.form_id=? " + " ORDER BY ph.date";

  /**
   * Gets the all pregnancy details.
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
   * @return the all pregnancy details
   */
  public List<BasicDynaBean> getAllPregnancyDetails(String mrNo, String patientId, int itemId,
      int genericFormId, String itemType, int formId) {

    return DatabaseHelper.queryToDynaList(GET_ALL_OBSTETRIC_HISTORY, mrNo, patientId, itemId,
        genericFormId, itemType, formId);
  }
}
