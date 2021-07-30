package com.insta.hms.core.scheduler.appointmentplanner;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class AppointmentPlannerDetailsRepository.
 */
@Repository
public class AppointmentPlannerDetailsRepository extends GenericRepository {

  /** The Constant TABLE_NAME. */
  private static final String TABLE_NAME = "patient_appointment_plan_details";

  /**
   * Instantiates a new appointment planner details repository.
   */
  public AppointmentPlannerDetailsRepository() {
    super(TABLE_NAME);
  }

  /** The get patient plan details. */
  private final String getPatientPlanDetails = "select pap.plan_id, pap.plan_name, pap.mr_no,"
      + " pap.presc_doc_id, pdoc.doctor_name as presc_doc_name, pap.plan_appointment_status, "
      + " papd.plan_details_id, papd.plan_visit_date, papd.appointment_category,"
      + " papd.consultation_reason_id, papd.doc_dept_id, "
      + " papd.primary_resource_id, papd.secondary_resource_id, sdoc.doctor_name "
      + " as secondary_resource_name, papd.appointment_id, papd.plan_details_status, "
      + " ap.scheduler_visit_type, ap.vip_status, ap.appointment_status, "
      + " ap.appointment_time::date AS appointment_date, "
      + " to_char(ap.appointment_time,'hh24:mi')::time AS plan_visit_time, "
      + " ap.duration, apit.resource_id, apit.resource_type, ctm.complaint_type as "
      + " consultation_reason_name, ctm.duration as complaint_type_duration, "
      + " CASE WHEN apit.resource_type ='EQID' THEN (select equipment_name from "
      + " test_equipment_master where eq_id::text=apit.resource_id )  "
      + " ELSE (SELECT generic_resource_name FROM generic_resource_type grt "
      + " JOIN generic_resource_master grm ON(grm.generic_resource_type_id = "
      + " grt.generic_resource_type_id) AND "
      + " grm.generic_resource_id::text = apit.resource_id WHERE "
      + " ap.appointment_id = apit.appointment_id AND "
      + " grt.scheduler_resource_type = apit.resource_type) END AS resource_name "
      + " FROM patient_appointment_plan pap JOIN patient_appointment_plan_details papd "
      + " ON (pap.plan_id=papd.plan_id) LEFT JOIN scheduler_appointments ap "
      + " ON(papd.appointment_id=ap.appointment_id) "
      + " LEFT JOIN scheduler_appointment_items apit ON(apit.appointment_id = ap.appointment_id"
      + " and CASE WHEN papd.appointment_category ='DOC' THEN apit.resource_type!='OPDOC' END) "
      + " LEFT JOIN doctors pdoc ON (pap.presc_doc_id = pdoc.doctor_id) LEFT JOIN doctors sdoc "
      + " ON (papd.secondary_resource_id = sdoc.doctor_id) "
      + "   JOIN complaint_type_master ctm ON (papd.consultation_reason_id=ctm.complaint_type_id) "
      + "   where pap.plan_id = ? order by papd.plan_details_id asc";

  /**
   * Gets the plan details.
   *
   * @param planId the plan id
   * @return the plan details
   */
  public List<BasicDynaBean> getPlanDetails(Integer planId) {
    String query = getPatientPlanDetails;
    List<Object> queryParams = new ArrayList<>();
    queryParams.add(0, planId);
    return DatabaseHelper.queryToDynaList(query, queryParams.toArray());
  }

}
