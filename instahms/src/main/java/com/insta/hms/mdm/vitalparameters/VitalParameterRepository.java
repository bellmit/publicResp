package com.insta.hms.mdm.vitalparameters;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class VitalParameterRepository.
 *
 * @author anup vishwas
 */

@Repository("parameterRepository")
public class VitalParameterRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new vital parameter repository.
   */
  public VitalParameterRepository() {
    super("vital_parameter_master", "param_id", "param_label");
  }

  /** The Constant GET_VITAL_PARAMS. */
  private static final String GET_VITAL_PARAMS = " SELECT param_id, param_label, param_order,"
      + " param_uom, mandatory_in_tx," + " expr_for_calc_result, system_vital "
      + " FROM vital_parameter_master "
      + " WHERE param_status='A' AND param_container='V' AND (visit_type=? OR visit_type is null)"
      + " ORDER BY param_order ASC";

  /**
   * Gets the active vital params.
   *
   * @param visitType
   *          the visit type
   * @return the active vital params
   */
  public List getActiveVitalParams(String visitType) {

    return DatabaseHelper.queryToDynaList(GET_VITAL_PARAMS, visitType);
  }

  /** The Constant GET_ALL_PARAMS. */
  private static final String GET_ALL_PARAMS = " SELECT param_id, param_label, param_order,"
      + " param_uom, param_container, mandatory_in_tx, visit_type, expr_for_calc_result,"
      + " param_status, system_vital "
      + " FROM vital_parameter_master ";

  /**
   * Gets the all params.
   *
   * @param container
   *          the container
   * @param visit
   *          the visit
   * @return the all params
   */
  public List getAllParams(String container, String visit) {
    container = container == null ? "" : container;
    visit = visit == null ? "" : visit;
    String query = GET_ALL_PARAMS;
    if (!visit.equals("") && !container.equals("V")) {
      query += " WHERE param_container IN ('I','O') AND (visit_type = ? OR visit_type is null) ";
    } else {
      query += " WHERE param_container = 'V' AND (visit_type = ? OR visit_type is null) ";
    }
    query += " ORDER BY param_container DESC, param_order ASC";
    return DatabaseHelper.queryToDynaList(query, visit);

  }

  /** The Constant GET_ALL_VITAL_PARAMS. */
  private static final String GET_ALL_VITAL_PARAMS = " SELECT vpm.param_id, vpm.param_label,"
      + " vpm.param_order, param_uom, param_container, "
      + " COALESCE(vdd.mandatory, vpm.mandatory_in_tx) as mandatory_in_tx,"
      + " COALESCE(vdd.visit_type, vpm.visit_type) as visit_type, expr_for_calc_result,"
      + " param_status, system_vital "
      + " FROM vital_parameter_master vpm "
      + " LEFT JOIN vitals_default_details vdd on (vpm.param_id = vdd.param_id "
      + " AND vdd.dept_id =? AND vdd.visit_type=? AND vdd.center_id =? )"
      + " ORDER BY param_container DESC, param_order ASC ";


  /**
   * Gets the all params.
   *
   * @param visit the visit
   * @param centerId the center id
   * @param deptId the dept id
   * @return the all params
   */
  public List getAllParams(String visit, int centerId, String deptId) {
    return DatabaseHelper.queryToDynaList(GET_ALL_VITAL_PARAMS, deptId, visit, centerId);

  }

  /** The Constant exactresultrange. */
  private static final String exactresultrange = " SELECT * FROM vital_reference_range_master"
      + " where param_id = ? AND ( range_for_all = 'N'  AND "
      + " ( (min_patient_age IS NULL OR min_patient_age*(CASE WHEN age_unit = 'Y'"
      + " THEN 365.25 ELSE 1 end) <= "
      + " ( SELECT (current_date - COALESCE(dateofbirth, expected_dob))::integer "
      + " FROM patient_details WHERE mr_no = ? ) ) AND"
      + " (max_patient_age IS NULL OR (  SELECT (current_date -"
      + " COALESCE(dateofbirth, expected_dob))::integer "
      + " FROM patient_details WHERE mr_no = ? ) <= "
      + " max_patient_age*(CASE WHEN age_unit = 'Y' THEN 365.25 ELSE 1 end) )) "
      + " AND (patient_gender = ? OR patient_gender = 'N')) ORDER BY priority LIMIT 1 ";

  /** The result all. */
  private static String result_all = " SELECT * FROM vital_reference_range_master "
      + " where param_id = ? AND range_for_all = 'Y'  ";

  /** The empty bean. */
  private static String EMPTY_BEAN = "SELECT vm.param_id, 0 as range_id, 'N' as range_for_all,"
      + " 'N' as patient_gender, "
      + " null as max_patient_age, null as min_patient_age, null as age_unit, null as priority, "
      + " null as min_normal_value, null as max_normal_value, null as min_critical_value,"
      + " null as max_critical_value,"
      + " null as min_improbable_value, null as max_improbable_value, null as reference_range_txt"
      + " FROM vital_parameter_master vm" + " WHERE vm.param_id = ? ";

  /**
   * Gets the reference range.
   *
   * @param mrNo
   *          the mr no
   * @param patientGender
   *          the patient gender
   * @param paramId
   *          the param id
   * @return the reference range
   */
  public BasicDynaBean getReferenceRange(String mrNo, String patientGender, int paramId) {

    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(exactresultrange, new Object[] { paramId,
        mrNo, mrNo, patientGender });
    if (bean == null) {
      bean = DatabaseHelper.queryToDynaBean(result_all, new Object[] { paramId });
    }
    if (bean == null) {
      bean = DatabaseHelper.queryToDynaBean(EMPTY_BEAN, new Object[] { paramId });
    }
    return bean;
  }

  /** The Constant REFERENCE_RANGE_LIST. */
  private static final String REFERENCE_RANGE_LIST = "SELECT param_id FROM "
      + "vital_reference_range_master GROUP BY param_id";

  /**
   * Gets the reference range list.
   *
   * @return the reference range list
   */
  public List<BasicDynaBean> getReferenceRangeList() {

    return DatabaseHelper.queryToDynaList(REFERENCE_RANGE_LIST);

  }

  /** The get all vitals. */
  private static String GET_ALL_VITALS = " select param_id,param_label,param_order,param_status"
      + " from vital_parameter_master ";

  /**
   * Gets the all vitals.
   *
   * @return the all vitals
   */
  public static List getAllVitals() {
    return DatabaseHelper.queryToDynaList(GET_ALL_VITALS);

  }
  
  /** The get vital details by patient id. */
  private static String GET_VITAL_DETAILS_BY_PATIENT_ID =
      "select textcat_commacat(vpm.param_label||' : '|| ' '|| "
          + " COALESCE(vr.param_value,'-')||' '|| COALESCE(vpm.param_uom,'')|| ' '||"
          + " COALESCE(vr.param_remarks)) as vital_details " //
          + " FROM visit_vitals vd "
          + " JOIN vital_reading vr ON (vd.vital_reading_id=vr.vital_reading_id)"
          + " JOIN vital_parameter_master vpm ON(vr.param_id=vpm.param_id)"
          + " WHERE  vd.patient_id=? GROUP by vr.vital_reading_id";
  
  /**
   * Gets the vital details entered in the consultation screen by patient id.
   *
   * @param patientId the patient id
   * @return the vital details by patient id
   */
  public List<BasicDynaBean> getVitalDetailsByPatientId(String patientId) {
    return DatabaseHelper.queryToDynaList(GET_VITAL_DETAILS_BY_PATIENT_ID, patientId);
  }

  private static final String GET_UNIQUE_VITALS_FOR_PATIENT =
      " SELECT param_id, param_label, param_order," + " param_uom, mandatory_in_tx,"
          + " expr_for_calc_result, system_vital " + " FROM vital_parameter_master "
          + " JOIN (Select DISTINCT param_id " + "   from visit_vitals "
          + "   join vital_reading using (vital_reading_id) "
          + "   where patient_id = ?) AS foo using (param_id) "
          + " WHERE param_container NOT IN ('I', 'O') AND param_status='A' "
          + " ORDER BY param_order ASC";

  
  /**
   * Gets unique vital details entered in the consultation screen by patient id.
   *
   * @param patientId the patient id
   * @return the vital details by patient id
   */
  public List<BasicDynaBean> getUniqueVitalsforPatient(String patientId) {
    return DatabaseHelper.queryToDynaList(GET_UNIQUE_VITALS_FOR_PATIENT, patientId);
  }

}
