package com.insta.hms.core.scheduler.appointmentplanner;

import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class AppointmentPlannerRepository.
 */
@Repository
public class AppointmentPlannerRepository extends GenericRepository {

  /** The Constant TABLE_NAME. */
  private static final String TABLE_NAME = "patient_appointment_plan";

  /**
   * Instantiates a new appointment planner repository.
   */
  public AppointmentPlannerRepository() {
    super(TABLE_NAME);
  }

  /**
   * Gets the plan names by patient.
   *
   * @param mrNo the mr no
   * @param centerId the center id
   * @return the plan names by patient
   */
  public List<BasicDynaBean> getPlanNamesByPatient(String mrNo, Integer centerId) {
    List<String> columns = new ArrayList<>();
    columns.add("plan_id");
    columns.add("plan_name");
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("mr_no", mrNo);
    filterMap.put("center_id", centerId);
    filterMap.put("plan_status", "A");
    return listAll(columns, filterMap, "plan_id");
  }
}
