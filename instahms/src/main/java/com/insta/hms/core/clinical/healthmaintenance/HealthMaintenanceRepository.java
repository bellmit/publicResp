package com.insta.hms.core.clinical.healthmaintenance;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class HealthMaintenanceRepository.
 *
 * @author sonam
 */
@Repository
public class HealthMaintenanceRepository extends GenericRepository {

  /**
   * Instantiates a new health maintenance repository.
   */
  public HealthMaintenanceRepository() {
    super("patient_health_maintenance");
  }

  /** The Constant GET_HEALTH_MAINT. */
  private static final String GET_HEALTH_MAINT = " SELECT phm.health_maint_id, phm.doctor_id, "
      + " phm.activity, phm.remarks, phm.status, phm.recorded_date, phm.due_by, "
      + " phm.username, psd.finalized, psd.section_detail_id, psd.section_id, " + " d.doctor_name "
      + " FROM patient_section_details psd "
      + " JOIN patient_section_forms psf ON (psf.section_detail_id = psd.section_detail_id)"
      + " LEFT JOIN patient_health_maintenance phm "
      + " ON (phm.section_detail_id = psd.section_detail_id)"
      + " LEFT JOIN doctors d ON (phm.doctor_id = d.doctor_id)"
      + " LEFT JOIN u_user usr on (psd.finalized_user = usr.emp_username) "
      + " WHERE psd.mr_no = ? AND psd.patient_id = ?  AND #filter#=? "
      + " AND psd.item_type= ? AND psf.form_type= ?  AND psd.section_id = -15 "
      + " ORDER BY phm.recorded_date";

  /** The Constant GET_ACTIVE_HEALTH_MAINT. */
  private static final String GET_ACTIVE_HEALTH_MAINT = " SELECT phm.health_maint_id, "
      + " phm.doctor_id, "
      + " phm.activity, phm.remarks, phm.status, phm.recorded_date, phm.due_by, phm.username, "
      + " psd.finalized, " + " d.doctor_name " + " FROM patient_health_maintenance phm "
      + " LEFT JOIN doctors d ON (phm.doctor_id=d.doctor_id)"
      + " JOIN patient_section_details psd ON (phm.section_detail_id = psd.section_detail_id) "
      + " LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
      + " WHERE psd.mr_no = ? AND psd.section_id = -15 AND psd.section_status = 'A'"
      + " ORDER BY phm.recorded_date ";

  /**
   * Gets the health maint records.
   *
   * @param mrNo
   *          the mr no
   * @param patientId
   *          the patient id
   * @param id
   *          the id
   * @param itemType
   *          the item type
   * @param formType
   *          the form type
   * @param formKeyfield
   *          the form keyfield
   * @return the health maint records
   */
  public List<BasicDynaBean> getHealthMaintRecords(String mrNo, String patientId, int id,
      String itemType, String formType, String formKeyfield) {
    Object[] parameter = new Object[] { mrNo, patientId, id, itemType, formType };
    return DatabaseHelper.queryToDynaList(GET_HEALTH_MAINT.replace("#filter#", formKeyfield),
        parameter);
  }

  /**
   * Gets the all active health maint.
   *
   * @param mrNo
   *          the mr no
   * @return the all active health maint
   */
  public List<BasicDynaBean> getAllActiveHealthMaint(String mrNo) {
    return DatabaseHelper.queryToDynaList(GET_ACTIVE_HEALTH_MAINT, mrNo);

  }

  /** The Constant GET_ALL_HEALTH_MAINTENANCE. */
  // this used in discharge summary
  private static final String GET_ALL_HEALTH_MAINTENANCE = " SELECT phm.*, "
      + "   d.doctor_name, psd.mr_no, psd.finalized, psd.finalized_user, usr.temp_username "
      + " FROM patient_health_maintenance phm "
      + " LEFT JOIN doctors d ON (phm.doctor_id=d.doctor_id)"
      + " JOIN patient_section_details psd "
      + " ON (phm.section_detail_id = psd.section_detail_id) "
      + " JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id) "
      + " LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
      + " WHERE psd.mr_no = ? AND psd.patient_id = ? AND coalesce(psd.section_item_id, 0)=? "
      + " AND coalesce(psd.generic_form_id, 0)=? AND item_type=? AND psd.section_id = -15 "
      + " AND psf.form_id=? " + " ORDER BY phm.recorded_date";

  /**
   * Gets the all health maintenance.
   *
   * @param mrNo
   *          the mr no
   * @param patientId
   *          the patient id
   * @param itemId
   *          the item id
   * @param genericFormId
   *          the generic form id
   * @param formId
   *          the form id
   * @param itemType
   *          the item type
   * @return the all health maintenance
   */
  public List<BasicDynaBean> getAllHealthMaintenance(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) {
    Object[] parameter = new Object[] { mrNo, patientId, itemId, genericFormId, itemType, formId };
    return DatabaseHelper.queryToDynaList(GET_ALL_HEALTH_MAINTENANCE, parameter);

  }

}
