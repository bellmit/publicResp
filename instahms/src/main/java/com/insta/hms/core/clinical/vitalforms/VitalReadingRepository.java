package com.insta.hms.core.clinical.vitalforms;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.core.clinical.forms.FormParameter;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class VitalReadingRepository.
 *
 * @author anup vishwas
 */
@Repository
public class VitalReadingRepository extends GenericRepository {

  /**
   * Instantiates a new vital reading repository.
   */
  public VitalReadingRepository() {
    super("vital_reading");
  }

  /** The Constant GET_VITAL_READINGS_FOR_VISIT. */
  public static final String GET_VITAL_READINGS_FOR_VISIT =
      " SELECT  vpm.param_label, vpm.param_uom, "
          + " vv.vital_reading_id, vr.param_id, vr.param_value, date_time, user_name "
          + " FROM vital_reading vr JOIN visit_vitals vv USING(vital_reading_id) "
          + " JOIN vital_parameter_master vpm USING (param_id) "
          + " WHERE vv.patient_id=? ";

  /**
   * Gets the vital readings.
   *
   * @param patientId the patient id
   * @param paramContainer the param container
   * @return the vital readings
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public List<BasicDynaBean> getVitalReadings(String patientId, String paramContainer) {
    paramContainer = paramContainer == null ? "" : paramContainer;
    List queryFilterParams = new ArrayList();
    String query = GET_VITAL_READINGS_FOR_VISIT;
    queryFilterParams.add(patientId);
    if (!paramContainer.equals("")) {
      query += " AND param_container=?";
      queryFilterParams.add(paramContainer);
    }
    query += " order by vital_reading_id, param_id asc";
    return DatabaseHelper.queryToDynaList(query, queryFilterParams.toArray());
  }

  /** The Constant GET_VITALS. */
  private static final String GET_VITALS = "SELECT section_id, finalized, "
      + " psd.section_detail_id, foo.patient_id, foo.vital_status, foo.finalized_by,"
      + " foo.finalized_date_time, foo.vital_reading_id, foo.date_time, foo.user_name,"
      + " foo.param_id, foo.param_value,"
      + " param_remarks, foo.param_label, foo.param_uom, "
      + " foo.param_order, '' as reference_range_color_code " 
      + " FROM patient_section_details psd  " 
      + " JOIN patient_section_forms psf ON (psd.mr_no=? and psd.patient_id=? "
      + "  and psd.item_type=? and #filter#=? and section_id=-4 "
      + "  and psf.form_type=? and psd.section_detail_id=psf.section_detail_id)" 
      + " LEFT JOIN "
      + "  (SELECT vv.patient_id, vv.vital_status, vv.finalized_by, vv.finalized_date_time,"
      + "   vr.vital_reading_id, vv.date_time, vv.user_name, vr.param_id, vr.param_value,"
      + "   vr.param_remarks, vpm.param_label, vpm.param_uom, vpm.param_order "
      + " FROM visit_vitals vv "
      + " JOIN vital_reading vr ON (vr.vital_reading_id = vv.vital_reading_id)"
      + " LEFT JOIN vital_parameter_master vpm ON (vpm.param_id = vr.param_id)"
      + " WHERE vv.patient_id=? and vv.status = 'A' and vr.status='A' "
      // not in condition on vital_parameter_master added to fix bug -> HMS-36813
      // below not in condition will not include the Intake/Output values in vitals section
      + " AND vpm.param_container NOT IN ('I', 'O') AND vpm.param_status='A')"
      + " AS foo ON (psd.patient_id=foo.patient_id) "
      + "ORDER BY foo.date_time desc, foo.param_id, foo.param_order";

  /**
   * Gets the vitals.
   *
   * @param parameter the parameter
   * @return the vitals
   */
  public List<BasicDynaBean> getVitals(FormParameter parameter) {
    return DatabaseHelper.queryToDynaList(
        GET_VITALS.replace("#filter#", "psd." + parameter.getFormFieldName()),
        new Object[] {parameter.getMrNo(), parameter.getPatientId(), parameter.getItemType(),
            parameter.getId(), parameter.getFormType(), parameter.getPatientId()});
  }

  /** The Constant GET_ACTIVE_VITALS. */
  private static final String GET_ACTIVE_VITALS = " SELECT -4 as section_id, 'N' as finalized, "
      + " 0 as section_detail_id, vv.patient_id, vv.vital_status,finalized_by,finalized_date_time,"
      + " vr.vital_reading_id, date_time, vv.user_name, vr.param_id, param_value, param_remarks,"
      + " vpm.param_label, vpm.param_uom, vpm.param_order, '' as reference_range_color_code"
      + " FROM visit_vitals vv "
      + " JOIN vital_reading vr ON (vr.vital_reading_id = vv.vital_reading_id) "
      + " JOIN vital_parameter_master vpm ON (vpm.param_id = vr.param_id) "
      + " WHERE vv.patient_id = ? AND vv.status = 'A' AND vr.status='A' "
      + " AND vpm.param_container NOT IN ('I', 'O') "
      + " AND vpm.param_status='A' ORDER BY vv.date_time desc, vr.param_id, vpm.param_order";

  /**
   * Gets the active vitals.
   *
   * @param parameter the parameter
   * @return the active vitals
   */
  public List<BasicDynaBean> getActiveVitals(FormParameter parameter) {
    return DatabaseHelper.queryToDynaList(GET_ACTIVE_VITALS,
        new Object[] {parameter.getPatientId()});
  }

  /** The Constant GET_DEFAULT_APPLICABLE_VITAL. */
  private static final String GET_DEFAULT_APPLICABLE_VITAL =
      " SELECT vpm.param_id, vpm.param_label, vpm.param_uom, '' as param_value,'' as param_remarks,"
      + " vpm.param_order, vdd.mandatory as mandatory_in_tx "
      + " FROM vitals_default_details vdd "
      + " JOIN vital_parameter_master vpm on (vpm.param_id = vdd.param_id)"
      + " WHERE vpm.param_status = 'A' AND vdd.center_id = ? AND vdd.dept_id = ? "
      + "   AND vdd.visit_type = ? ORDER BY param_order ";

  /**
   * Gets the default applicable vitals.
   *
   * @param centerId the center id
   * @param deptId the dept id
   * @param visitType the visit type
   * @return the default applicable vitals
   */
  public List<BasicDynaBean> getDefaultApplicableVitals(int centerId, String deptId,
      String visitType) {
    return DatabaseHelper.queryToDynaList(GET_DEFAULT_APPLICABLE_VITAL,
        new Object[] {centerId, deptId, visitType.toUpperCase()});
  }

  /** The Constant GET_LATEST_READING_FOR_PATIENT. */
  public static final String GET_LATEST_READING_FOR_PATIENT =
      " SELECT vpm.param_label, vpm.param_uom, vr.param_id, vr.param_value, param_remarks "
      + " FROM vital_reading vr JOIN visit_vitals vv USING(vital_reading_id) "
      + " JOIN vital_parameter_master vpm USING (param_id) "
      + " WHERE vpm.param_status='A' and vital_reading_id=(SELECT vital_reading_id "
      + "   FROM visit_vitals vv where vv.patient_id in (SELECT patient_id "
      + "   FROM patient_registration pr where pr.mr_no=? and pr.patient_id!=? "
      + " and pr.visit_type='o' and reg_date+reg_time >= (localtimestamp(0) - interval '24 hours')"
      + "   ORDER BY reg_date+reg_time desc limit 1) and vv.status = 'A' and vr.status='A' "
      + " ORDER BY vv.date_time desc limit 1) ORDER BY vpm.param_order ASC";

  /**
   * Gets the latest vitals.
   *
   * @param mrNo the mr no
   * @param patientId the patient id
   * @return the latest vitals
   */
  public List<BasicDynaBean> getLatestVitals(String mrNo, String patientId) {
    return DatabaseHelper.queryToDynaList(GET_LATEST_READING_FOR_PATIENT,
        new Object[] {mrNo, patientId});
  }

  /** The Constant GET_HEIGHT_AND_WEIGHT. */
  public static final String GET_HEIGHT_AND_WEIGHT =
      " SELECT vpm.param_label, vpm.param_uom, vr.param_id, vr.param_value, '' as param_remarks "
      + " FROM visit_vitals vv JOIN patient_registration pr using (patient_id) "
      + " JOIN vital_reading vr using (vital_reading_id) "
      + " JOIN vital_parameter_master vpm on (vr.param_id=vpm.param_id) "
      + " WHERE pr.mr_no=? and reg_date+reg_time <= (select reg_date+reg_time from "
      + "   patient_registration where patient_id=?) and vv.status = 'A' and vr.status='A' "
      + " and vpm.param_id in (5, 6) "
      + " order by pr.reg_date+pr.reg_time desc, vv.date_time desc limit 2 ";

  /**
   * Gets the height and weight.
   *
   * @param mrNo the mr no
   * @param patientId the patient id
   * @return the height and weight
   */
  public List<BasicDynaBean> getHeightAndWeight(String mrNo, String patientId) {
    return DatabaseHelper.queryToDynaList(GET_HEIGHT_AND_WEIGHT, new Object[] {mrNo, patientId});
  }
}
