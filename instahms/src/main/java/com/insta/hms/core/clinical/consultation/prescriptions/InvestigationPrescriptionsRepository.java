package com.insta.hms.core.clinical.consultation.prescriptions;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

/**
 * @author teja.
 *
 */
@Repository
public class InvestigationPrescriptionsRepository extends GenericRepository {

  public InvestigationPrescriptionsRepository() {
    super("patient_test_prescriptions");
  }

  private String presTests = "SELECT d.test_name AS item_name, d.diag_code AS order_code,"
      + " d.test_id AS item_id, false AS is_package, "
      + " 'Inv.' AS item_type, d.prior_auth_required, '' AS type, "
      + " COALESCE(cat.insurance_category_id,0) AS insurance_category_id, "
      + " COALESCE(cat.insurance_category_name,'') AS insurance_category_name, "
      + " COALESCE(cat.category_payable,'N') AS category_payable, dd.category,"
      + " dc.charge, dc.discount, tod.item_code AS item_rate_plan_cod, d.result_validity_period,"
      + " d.result_validity_period_units, (select max(tc.conducted_date) from tests_prescribed tp "
      + "   JOIN tests_conducted tc using (prescribed_id) "
      + " where tp.test_id=d.test_id and tp.conducted='S' "
      + "   and tp.mr_no=?) as last_conduction_date_time,"
      + " 'N' as order_auth_required, tod.applicable, "
      + " d.mandate_clinical_info,d.clinical_justification, "
      + " false AS is_panel " + "FROM diagnostics d "
      + "JOIN diagnostic_charges dc ON (d.test_id=dc.test_id AND dc.bed_type=? AND dc.org_name=?) "
      + "JOIN diagnostics_departments dd ON (dd.ddept_id=d.ddept_id) "
      + "JOIN test_org_details tod ON (tod.test_id=d.test_id AND tod.org_id=?) "
      + "LEFT JOIN LATERAL (SELECT dtic.diagnostic_test_id AS test_id, "
      + "      dtic.insurance_category_id, "
      + "      iic.insurance_category_name, ipd.category_payable "
      + "  FROM diagnostic_test_insurance_category_mapping dtic "
      + "  JOIN item_insurance_categories iic "
      + "       ON(dtic.insurance_category_id = iic.insurance_category_id) "
      + "  JOIN insurance_plan_details ipd "
      + "       ON(ipd.insurance_category_id = iic.insurance_category_id "
      + "        AND ipd.patient_type = ? AND ipd.plan_id=?) "
      + "      WHERE d.test_id = dtic.diagnostic_test_id "
      + "      ORDER BY iic.priority LIMIT 1) as cat ON(cat.test_id = d.test_id) "
      + "WHERE (not ? or d.is_prescribable) AND d.status='A' AND (d.test_name ilike ? "
      + " OR d.test_name ilike ? OR d.diag_code ilike ?) "
      + "UNION "
      + "SELECT pm.package_name AS item_name, pm.package_code AS order_code, "
      + " pm.package_id::text AS item_id,"
      + " pm.type = 'P' AS is_package, 'Inv.' AS item_type, pm.prior_auth_required, pm.type, "
      + " COALESCE(cat.insurance_category_id,0) AS insurance_category_id,"
      + " COALESCE(cat.insurance_category_name,'') AS insurance_category_name, "
      + " COALESCE(cat.category_payable,'N') AS category_payable, "
      + " '' AS category, coalesce(pc.charge, 0) as charge,"
      + " coalesce(pc.discount, 0) as discount, pod.item_code AS item_rate_plan_code, "
      + " 0 AS result_validity_period, "
      + " '' AS result_validity_period_units, null as last_conduction_date_time, "
      + " 'N' as order_auth_required, pod.applicable,'N'as mandate_clinical_info , "
      + " '' as clinical_justification, "
      + " false AS is_panel FROM packages pm "
      + " LEFT JOIN package_charges pc "
      + "    ON (pm.package_id=pc.package_id AND pc.bed_type=? AND pc.org_id=?) "
      + " LEFT JOIN pack_org_details pod ON (pod.package_id=pm.package_id AND pod.org_id=?) "
      + " JOIN center_package_applicability pcm ON (pcm.package_id=pm.package_id AND pcm.status='A'"
      + "   AND (pcm.center_id=? or pcm.center_id=-1)) "
      + " JOIN package_sponsor_master psm ON (psm.pack_id=pm.package_id AND psm.status='A'"
      + "    AND (psm.tpa_id=? OR psm.tpa_id = '-1' OR psm.tpa_id = '0')) "
      + " LEFT JOIN LATERAL (SELECT pic.package_id AS package_id, pic.insurance_category_id,"
      + "     iic.insurance_category_name, ipd.category_payable "
      + "     FROM packages_insurance_category_mapping pic "
      + " JOIN item_insurance_categories iic "
      + "      ON(pic.insurance_category_id = iic.insurance_category_id) "
      + " JOIN insurance_plan_details ipd "
      + "      ON(ipd.insurance_category_id = iic.insurance_category_id "
      + "       AND ipd.patient_type = ? AND ipd.plan_id=?) "
      + "     WHERE pm.package_id = pic.package_id "
      + "     ORDER BY iic.priority LIMIT 1) as cat "
      + " ON(cat.package_id = pm.package_id) "
      + " JOIN package_plan_master ppm ON (ppm.pack_id = pm.package_id and ppm.status='A' "
      + " AND (ppm.plan_id=? OR ppm.plan_id = '-1' OR ppm.plan_id = 0)) "
      + " JOIN dept_package_applicability dpa ON (dpa.dept_id IN ('*', ?) "
      + "    AND dpa.package_id::integer=pm.package_id::integer) "
      + " WHERE  NOT pm.multi_visit_package AND pm.package_category_id in(-3,-2) "
      + " AND pm.approval_status='A' AND pm.status='A'"
      + " AND (pm.package_name ilike ? OR pm.package_name ilike ? OR pm.package_code ilike ?)"
      + "  AND pm.gender_applicability IN (?, '*') AND "
      + " pm.visit_applicability IN (?, '*') AND "
      + " (pm.min_age <= ? OR pm.min_age IS NULL) AND "
      + " (pm.max_age >= ? OR pm.max_age IS NULL) AND "
      + " (pm.age_unit = ? OR pm.age_unit IS NULL) AND "
      + " (pm.valid_from is NULL OR (pm.valid_from <= ?)) "
      + " AND (pm.valid_till is NULL OR (pm.valid_till >= ?))"
      // To fetch panel order sets
      + " UNION ALL Select " + " p.package_name AS item_name, p.package_code AS order_code,"
      + " p.package_id::text AS item_id, false AS is_package, "
      + " 'Inv.' AS item_type, 'N' as prior_auth_required,"
      + " '' AS type, 0 AS insurance_category_id, "
      + " '' AS insurance_category_name, 'N' AS category_payable, "
      + " '' AS category, 0 AS charge, 0 AS discount, "
      + " '' AS item_rate_plan_code, 0 AS result_validity_period, "
      + " '' AS result_validity_period_units, null as last_conduction_date_time, "
      + " 'N' as order_auth_required, null AS applicable,"
      + " 'N' as mandate_clinical_info , ''as clinical_justification, " + " true AS is_panel "
      + " From packages p JOIN center_package_applicability cpa ON (cpa.center_id IN (-1, ?) "
      + " AND cpa.package_id=p.package_id) "
      + "JOIN dept_package_applicability dpa ON (dpa.dept_id IN ('*', ?) "
      + " AND dpa.package_id=p.package_id) "
      + "WHERE p.package_category_id = -2 AND p.status='A' AND p.type='O'"
      + " AND p.gender_applicability IN (?, '*') "
      + " AND p.visit_applicability IN (?, '*') AND (p.valid_from is NULL OR (p.valid_from < ?)) "
      + " AND (p.valid_till is NULL OR (p.valid_till > ?))"
      + " AND (p.package_name ilike ? OR p.package_name ilike ? OR p.package_code ilike ?)"
      + "LIMIT ? ";

  private String getTestsLastConductionDate = "SELECT "
      + " d.test_id AS item_id, false AS is_package, 'Inv.' AS item_type,"
      + " d.prior_auth_required, d.result_validity_period, d.result_validity_period_units,"
      + " (select max(tc.conducted_date) from tests_prescribed tp"
      + "   JOIN tests_conducted tc using (prescribed_id) "
      + " where tp.test_id=d.test_id and tp.conducted='S' "
      + "   and tp.mr_no=:mr_no) as last_conduction_date_time, " + " 'N' as order_auth_required "
      + "FROM diagnostics d " + "WHERE d.status='A' AND (d.test_id IN (:id_list))";

  /**
   * Get tests for prescription.
   * @param mrNo the string
   * @param bedType the string
   * @param orgId the string
   * @param patientType the string
   * @param insPlanId the int
   * @param centerId the int
   * @param tpaId the string
   * @param searchQuery the string
   * @param deptId the string
   * @param gender the string
   * @param isPrescribalbe the boolean
   * @param itemLimit the int
   * @return list of BasicDynaBean
   */
  public List<BasicDynaBean> getTestsForPrescription(String mrNo, String bedType, String orgId,
      String patientType, Integer insPlanId, Integer centerId, String tpaId, String searchQuery,
      String deptId, Integer age, String ageIn, String gender, 
      Boolean isPrescribalbe, Integer itemLimit) {
    Boolean diaPakage = !patientType.equals("i");
    return DatabaseHelper.queryToDynaList(presTests, mrNo, bedType, orgId, orgId, patientType,
        insPlanId, isPrescribalbe, searchQuery + "%", "% " + searchQuery + "%", searchQuery + "%",
        bedType, orgId, orgId, centerId, tpaId, patientType, insPlanId, insPlanId,deptId,
        searchQuery + "%", "% " + searchQuery + "%", searchQuery + "%", gender,patientType,
        age, age, ageIn,
        DateUtil.getCurrentDate(), DateUtil.getCurrentDate(), centerId, deptId, gender, patientType,
        DateUtil.getCurrentDate(), DateUtil.getCurrentDate(), searchQuery + "%",
        "% " + searchQuery + "%", searchQuery + "%",
        itemLimit);
  }

  /**
   * inv conduction date for ids.
   * @param mrNo the string
   * @param idList the list of string
   * @return list of BasicDynaBean
   */
  public List<BasicDynaBean> invConductionDateForIds(String mrNo, List<String> idList) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("mr_no", mrNo);
    parameters.addValue("id_list", idList);
    return DatabaseHelper.queryToDynaList(getTestsLastConductionDate, parameters);
  }

  private static final String GET_INVESTIGATION_DETAILS = "SELECT "
      + " dd.category AS patient_prescription_type, "
      + " d.ddept_id AS dept_id, d.test_id AS presc_item_id " + " FROM diagnostics d "
      + " LEFT JOIN diagnostics_departments dd ON dd.ddept_id=d.ddept_id "
      + " WHERE d.test_id IN (:testId) ";

  /**
   * Gets investigation type.
   * @param id the list of string
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getInvestigationType(List<String> id) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("testId", id);
    return DatabaseHelper.queryToDynaList(GET_INVESTIGATION_DETAILS, parameters);
  }

  /**
   * Gets investigation type.
   * @param id the string
   * @return basic dyna bean
   */
  public BasicDynaBean getInvestigationType(String id) {
    List<BasicDynaBean> tests = getInvestigationType(Arrays.asList(id));
    if (tests != null) {
      return tests.get(0);
    }
    return null;
  }
}