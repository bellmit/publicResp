package com.insta.hms.core.clinical.eauthorization;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Class EAuthPrescriptionActivitiesRepository.
 *
 * @author teja
 */
@Repository
public class EAuthPrescriptionActivitiesRepository extends GenericRepository {

  /**
   * Instantiates a new e auth prescription activities repository.
   */
  public EAuthPrescriptionActivitiesRepository() {
    super("preauth_prescription_activities");
  }

  /** The Constant GET_ACTIVITIES_FOR_CONS_AND_INSURANE. */
  private static final String GET_ACTIVITIES_FOR_CONS_AND_INSURANE = " SELECT prpa.* "
      + " FROM preauth_prescription_activities prpa " + "JOIN preauth_prescription prp "
      + " ON (prp.preauth_presc_id = prpa.preauth_presc_id AND prp.preauth_payer_id=?) "
      + " WHERE prpa.consultation_id=? ";

  /**
   * Gets the activities.
   *
   * @param insuranceCompanyId
   *          the insurance company id
   * @param consId
   *          the cons id
   * @return the activities
   */
  public List<BasicDynaBean> getActivities(final String insuranceCompanyId, final Integer consId) {
    return DatabaseHelper.queryToDynaList(GET_ACTIVITIES_FOR_CONS_AND_INSURANE, new Object[] {
        insuranceCompanyId, consId });
  }
  
  private static final String GET_ACTIVITIES_BY_ID = "SELECT * "
      + "FROM preauth_prescription_activities "
      + "WHERE preauth_act_id IN (:preauthActIds)";
  
  /**
   * Gets the preauth prescription activities.
   *
   * @param preauthActIds the preauth act ids
   * @return the activities
   */
  public List<BasicDynaBean> getActivities(List<Integer> preauthActIds) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("preauthActIds", preauthActIds);
    return DatabaseHelper.queryToDynaList(GET_ACTIVITIES_BY_ID, parameters);
  }
  
  /** The Constant GET_ACTIVE_PREAUTH_ITEMS. */
  private static final String GET_ACTIVE_PREAUTH_ITEMS = " SELECT "
      + " DISTINCT prpa.preauth_id as preauth_number, prpa.preauth_mode,"
      + " prpa.patient_pres_id as pres_id, "
      + " prpa.preauth_act_item_id AS preauth_item_code,"
      + " prpa.preauth_act_type AS preauth_item_type, prpa.rem_qty as preauth_rem_qty, "
      + " prpa.preauth_act_id as preauth_item_id, pr.primary_sponsor_id as preauth_sponsor_id, "
      + " oi.item_name as preauth_item_name , d.doctor_id, d.doctor_name "
      + " FROM preauth_prescription_activities prpa "
      + " JOIN patient_registration pr ON (prpa.mr_no = pr.mr_no) "
      + " JOIN orderable_item oi ON (oi.entity_id = prpa.preauth_act_item_id and orderable='Y')"
      + " LEFT JOIN doctor_consultation dc ON (dc.consultation_id=prpa.consultation_id) "
      + " LEFT JOIN doctors d ON (dc.doctor_name = d.doctor_id) "
      + " LEFT JOIN preauth_prescription ppresc "
      + " ON (ppresc.preauth_presc_id = prpa.preauth_presc_id) "
      + " LEFT JOIN preauth_request_approval_details prad "
      + " ON (ppresc.preauth_request_id = prad.preauth_request_id)" + " WHERE prpa.status='A'"
      + " AND prpa.rem_qty > 0 AND prpa.preauth_act_status='C' "
      + " AND prpa.prescribed_date >= current_timestamp - interval '30 days' "
      + " AND (prad.end_date IS NULL OR prad.end_date >= current_timestamp)"
      + " AND prpa.visit_id IN (" + " SELECT patient_id from patient_registration where mr_no=?";
  
  /** The Constant GET_ACTIVE_PREAUTH_ITEMS. */
  private static final String GET_PREAUTH_ITEMS = " SELECT "
      + " DISTINCT prpa.preauth_id as preauth_number, prpa.preauth_mode,"
      + " prpa.patient_pres_id as pres_id, "
      + " prpa.preauth_act_item_id AS preauth_item_code,"
      + " prpa.preauth_act_type AS preauth_item_type, prpa.rem_qty as preauth_rem_qty, "
      + " prpa.preauth_act_id as preauth_item_id, pr.primary_sponsor_id as preauth_sponsor_id, "
      + " oi.item_name as preauth_item_name , d.doctor_id, d.doctor_name, "
      + " prpa.visit_id AS patient_id, 0 AS pres_id, "
      + " CASE WHEN prpa.preauth_act_type =  'OPE' THEN operation.operation_name "
      + " WHEN prpa.preauth_act_type =  'DIA' THEN test.test_name"
      + " WHEN prpa.preauth_act_type =  'DOC' THEN doctor.doctor_name"
      + " WHEN prpa.preauth_act_type =  'SER' THEN service.service_name "
      + " ELSE oi.item_name  END AS name, "
      + " oi.entity_id AS cross_cons_doctor_id, prpa.preauth_act_item_remarks  as remarks, "
      + " prpa.consultation_id, prpa.mod_time as prescription_date, prpa.username, "
      + " CASE WHEN prpa.added_to_bill='N' THEN 'O' ELSE 'P' END AS added_to_bill, "
      + " case when pr.visit_type='o' then -1 when pr.visit_type='i' then -3 end as head,"
      + " ct.consultation_type, dp.dept_name, null AS pri_pre_auth_no, "
      + " 0 AS pri_pre_auth_mode_id, prep.preauth_status, "
      + " NULL::TIMESTAMP AS appointment_time, '' AS appointment_status, date(prpa.mod_time) "
      + " AS start_datetime, prpa.preauth_act_status, null AS sec_pre_auth_no, "
      + " 0 AS sec_pre_auth_mode_id, 'N' AS item_excluded_from_doctor, null "
      + " AS item_excluded_from_doctor_remarks, null::DATE as preauth_end_date, pr.center_id "
      + " AS center_id, prpa.preauth_presc_id, date(prpa.mod_time) as prescribed_date,"
      + " oi.entity_id AS operation_id, "
      + " CASE WHEN prpa.preauth_act_type =  'OPE' THEN operation.operation_name ELSE "
      + " oi.item_name END as operation_name, null AS pri_pre_auth_no,"
      + " prpa.preauth_act_item_id AS service_id, null AS tooth_unv_number, "
      + " null as tooth_fdi_number, null as tooth_num_required, prpa.rem_qty AS service_qty,"
      + " prpa.preauth_act_item_id AS test_id, oi.entity as type, prpa.preauth_required, "
      + " CONCAT(COALESCE(prpa.preauth_act_id,0),'_',COALESCE(prpa.patient_pres_id,0)) "
      + " AS presc_preauth_number,prpa.approved_qty AS preauth_approved_qty, "
      + " prpa.rem_approved_qty AS preauth_rem_approved_qty "
      + " FROM preauth_prescription prep "
      + " JOIN preauth_prescription_activities prpa "
      + " ON (prpa.preauth_presc_id = prep.preauth_presc_id) "
      + " JOIN patient_registration pr ON (prpa.visit_id = pr.patient_id) "
      + " JOIN orderable_item oi ON (oi.entity_id = prpa.preauth_act_item_id and orderable='Y')"
      + " LEFT JOIN services service ON (service.service_id = prpa.preauth_act_item_id "
      + "  AND prpa.preauth_act_type =  'SER')"
      + " LEFT JOIN doctors doctor ON (prpa.preauth_act_item_id = doctor.doctor_id "
      + "  AND prpa.preauth_act_type = 'DOC')"
      + " LEFT JOIN all_tests_pkgs_view test ON (test.test_id = prpa.preauth_act_item_id "
      + "  AND prpa.preauth_act_type IN ('DIA') )"
      + " LEFT JOIN operation_master operation ON (operation.op_id=prpa.preauth_act_item_id "
      + "  AND prpa.preauth_act_type =  'OPE') "
      + " LEFT JOIN doctor_consultation dc ON (dc.consultation_id=prpa.consultation_id) "
      + " LEFT JOIN doctors d ON (dc.doctor_name = d.doctor_id) "
      + " LEFT JOIN department dp ON(d.dept_id = dp.dept_id)  "
      + " LEFT JOIN consultation_types ct ON (dc.head = ct.consultation_type_id::text) "
      + " LEFT JOIN doctors dt ON (dc.doctor_name = dt.doctor_id) "
      + " WHERE prpa.status='A'"
      + " AND prpa.rem_qty > 0 "
      + " AND prpa.prescribed_date >= "
      + "  (select current_timestamp - (prescription_validity || ' days ')::interval "
      + "    from generic_preferences) "
      + " AND prpa.patient_pres_id = 0 AND prpa.consultation_id = 0 AND pr.visit_type = 'o' "
      + " AND (pr.primary_sponsor_id is not null OR pr.secondary_sponsor_id is not null)"
      + " AND pr.mr_no = ? ";

  /**
   * Contains service,test,order,consultation preauth prescriptions for the past <b>30 days</b>
   * which are in active status depending on primarySponsorId(Optional,ignored if null) 
   * and MRNo. <br/>
   * <b>Note</b>: <i>Can have entries from other visits of same patient, with same center and
   * TPA</i>
   *
   * @param mrNo
   *          the mr no
   * @param centerId
   *          the center id
   * @param primarySponsorId
   *          the primary sponsor id
   * @return the active pre auth approved items
   */

  public List<BasicDynaBean> getActivePreAuthApprovedItems(final String mrNo,
      final Integer centerId, final String primarySponsorId) {
    StringBuilder queryBuilder = new StringBuilder(GET_PREAUTH_ITEMS);
    List args = new ArrayList();
    args.add(mrNo);
    if (StringUtils.isNotBlank(primarySponsorId)) {
      queryBuilder.append(" AND pr.primary_sponsor_id= ? ");
      args.add(primarySponsorId);
    }
    if (centerId != null) {
      queryBuilder.append(" AND pr.center_id= ? ");
      args.add(centerId);
    }
    return DatabaseHelper.queryToDynaList(queryBuilder.toString(), args.toArray());
  }
  
  /** The Constant GET_PREAUTH_ACT_ID. */
  private static final String GET_PREAUTH_ACT_ID = "SELECT preauth_act_id , "
      + " patient_pres_id, preauth_required, preauth_act_status"
      + " FROM preauth_prescription_activities WHERE patient_pres_id IN (:prescId)";
  
  /**
   * Gets the preauth act ids.
   *
   * @param patientPresIds the patient pres ids
   * @return the preauth act ids
   */
  public List<BasicDynaBean> getPreauthActIds(List<Integer> patientPresIds) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("prescId", patientPresIds);
    return DatabaseHelper.queryToDynaList(GET_PREAUTH_ACT_ID, parameters);
  }
  
  private static final String GET_ORDERED_PREAUTH_ID_OF_VISIT = "SELECT ppa.preauth_act_id "
      + "FROM bill b "
      + "JOIN bill_charge bc ON (bc.bill_no = b.bill_no) "
      + "JOIN preauth_prescription_activities ppa ON (bc.preauth_act_id = ppa.preauth_act_id)"
      + "WHERE b.visit_id=:visitId";
  
  /**
   * Gets the preauth act ids of ordered items.
   *
   * @param visitId the visit id
   * @return the preauth act ids
   */
  public List<BasicDynaBean> getPreauthActIds(String visitId) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("visitId", visitId);
    return DatabaseHelper.queryToDynaList(GET_ORDERED_PREAUTH_ID_OF_VISIT, parameters);
  }


  private static final String GET_AUTO_GENERATED_PREAUTH_IDS =
      "SELECT * FROM preauth_prescription_activities WHERE preauth_act_id in (:preAuthActIds) AND"
          + " patient_pres_id = -1";

  /**
   * Get autogenerated pre auth act ids list.
   *
   * @param preauthActIds the preauth act ids
   * @return the list
   */
  public List<BasicDynaBean> getAutogeneratedPreAuthActIds(List<Integer> preauthActIds) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    if (CollectionUtils.isEmpty(preauthActIds)) {
      return Collections.emptyList();
    }
    parameters.addValue("preAuthActIds", preauthActIds);
    return DatabaseHelper.queryToDynaList(GET_AUTO_GENERATED_PREAUTH_IDS, parameters);
  }
  
  private static final String UPDATE_PREVIOUSLY_CHECKED_PREAUTH_ACTIVITIES = "UPDATE "
      + "preauth_prescription_activities SET preauth_required='N' "
      + "WHERE preauth_presc_id IN ("//
      + " SELECT preauth_presc_id FROM preauth_prescription "//
      + " WHERE preauth_status = 'O' AND consultation_id = ?"//
      + ") AND patient_pres_id NOT IN (Select CASE WHEN coalesce(d.prior_auth_required, "
      + " om.prior_auth_required, "
      + " s.prior_auth_required, '')='A' THEN pp.patient_presc_id ELSE 0 END "
      + " FROM patient_prescription pp "
      + " LEFT JOIN patient_test_prescriptions ptp ON (pp.presc_type='Inv.' "
      + "   AND pp.patient_presc_id=ptp.op_test_pres_id) "
      + " LEFT JOIN diagnostics d ON (pp.presc_type='Inv.' AND d.test_id=ptp.test_id) "
      + " LEFT JOIN patient_operation_prescriptions pop ON (pp.presc_type='Operation' "
      + "   AND pp.patient_presc_id=pop.prescription_id) "
      + " LEFT JOIN operation_master om ON (pp.presc_type='Operation' "
      + "   AND pop.operation_id=om.op_id) "
      + " LEFT JOIN patient_service_prescriptions psp ON (pp.presc_type='Service' "
      + "   AND pp.patient_presc_id=psp.op_service_pres_id) "
      + " LEFT JOIN services s ON (pp.presc_type='Service' AND s.service_id = psp.service_id) "
      + " WHERE pp.consultation_id=?)";

  public void uncheckPreviouslyCheckedPrescriptionsForPriorAuth(int consultationId) {
    DatabaseHelper.update(UPDATE_PREVIOUSLY_CHECKED_PREAUTH_ACTIVITIES,
        new Object[] {consultationId, consultationId});
  }

}
