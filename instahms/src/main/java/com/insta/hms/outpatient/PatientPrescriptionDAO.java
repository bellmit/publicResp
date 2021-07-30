package com.insta.hms.outpatient;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import java.sql.SQLException;
import java.util.List;

/**
 * The Class PatientPrescriptionDAO.
 *
 * @author krishna
 */
public class PatientPrescriptionDAO extends GenericDAO {

  /**
   * Instantiates a new patient prescription DAO.
   */
  public PatientPrescriptionDAO() {
    super("patient_prescription");
  }

  private static final String GET_PRESCRIBED_MEDICINES =
      " SELECT " + " pmd.medicine_id, issue_type, identification, package_type,"
          + " coalesce((SELECT sum(qty) FROM store_stock_details ssd "
          + "   JOIN store_item_batch_details sibd USING(item_batch_id)"
          + "   WHERE dept_id = ? and ssd.medicine_id=pmp.medicine_id"
          + "     AND (exp_dt is null or sibd.exp_dt >= current_date)),0) as qty_avbl, "
          + "  issue_base_unit, issue_units, category, medicine_name, package_uom, m.manf_name,"
          + " 'M' AS presc_type " + " FROM patient_prescription pp "
          + " JOIN patient_medicine_prescriptions pmp"
          + "   ON (pmp.op_medicine_pres_id=pp.patient_presc_id"
          + " AND pmp.is_discharge_medication = false)"
          + " JOIN store_item_details pmd ON (pmp.medicine_id=pmd.medicine_id)"
          + " JOIN manf_master m ON ( pmd.manf_name = m.manf_code )"
          + " JOIN store_category_master on med_category_id=category_id "
          + " JOIN clinical_preferences on true "
          + " WHERE pp.visit_id=? AND pp.presc_type='Medicine' AND pp.discontinued='N' ";

  /**
   * Gets the presc medicines.
   *
   * @param patientId the patient id
   * @param storeId the store id
   * @return the presc medicines
   * @throws SQLException the SQL exception
   */
  public List getPrescMedicines(String patientId, String storeId) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_PRESCRIBED_MEDICINES,
        new Object[] {Integer.parseInt(storeId), patientId});
  }
}
