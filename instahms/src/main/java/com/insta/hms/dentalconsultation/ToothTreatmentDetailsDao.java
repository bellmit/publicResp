package com.insta.hms.dentalconsultation;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ToothTreatmentDetailsDao extends GenericDAO {

  public ToothTreatmentDetailsDao() {
    super("tooth_treatment_details");
  }

  private static final String TREATMENT_DETAILS = " SELECT ttd.*, s.service_name, "
      + "service_group_name, s.doc_speciality_id, pd.doctor_name as planned_by_name, "
      + "cd.doctor_name as completed_by_name, unit_charge as charge,"
      + " smc.discount, tooth_num_required, s.service_code, "
      + "s.conducting_doc_mandatory, bc.bill_no, bc.discount as disc, "
      + "bc.amount as amt, bc.act_rate as rate, bc.act_quantity as ordered_qty "
      + "FROM tooth_treatment_details ttd "
      + "LEFT JOIN bill_activity_charge bac ON ("
      + "bac.activity_id=ttd.service_prescribed_id::text AND payment_charge_head='SERSNP') "
      + "LEFT JOIN bill_charge bc USING (charge_id) "
      + "JOIN services s ON (s.service_id=ttd.service_id) "
      + "JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=s.service_sub_group_id) "
      + "JOIN service_groups USING (service_group_id) "
      + "LEFT JOIN doctors pd ON (ttd.planned_by=pd.doctor_id) "
      + "LEFT JOIN doctors cd ON (ttd.completed_by=cd.doctor_id) "
      + "LEFT OUTER JOIN service_org_details sod ON sod.org_id=ttd.org_id "
      + "AND sod.service_id=s.service_id "
      + "JOIN service_master_charges smc ON (smc.service_id=s.service_id "
      + "and smc.org_id=sod.org_id and smc.bed_type=?)"
      + "WHERE ttd.mr_no=? ORDER BY ttd.planned_date ASC";

  /**
   * Gets the treatment details.
   *
   * @param mrNo the mr no
   * @param orgId the org id
   * @param bedType the bed type
   * @return the treatment details
   * @throws SQLException the SQL exception
   */
  public List getTreatmentDetails(String mrNo, String orgId, String bedType) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(TREATMENT_DETAILS);
      // ps.setString(1, orgId); #HMS-8634
      ps.setString(1, bedType);
      ps.setString(2, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /*
   * gets the subtasks in following cases 1) saved subtasks. 2) if saved subtasks not found but in
   * master we found subtasks for the service in such case if the treatement is in planned or
   * inprogress get those sub tasks. if the treatment is in completed or cancelled dont get the
   * subtasks from master.
   */
  private static final String SERVICE_SUB_TASKS_DETAILS_FOR_PATIENT = " SELECT ttd.treatment_id, "
      + "spt.completed_by, d.doctor_name, spt.completion_time, spt.status, sst.sub_task_id, "
      + "sst.desc_short, sst.desc_long, "
      + "sst.status as mas_task_status, ttd.service_id, spt.task_presc_id "
      + "FROM tooth_treatment_details ttd "
      + "LEFT JOIN services_presc_tasks spt using (treatment_id) "
      + "LEFT JOIN service_sub_tasks sst on (case when task_presc_id is null then "
      + "(ttd.treatment_status in ('P', 'I') AND ttd.service_id=sst.service_id AND sst.status='A') "
      + "else sst.sub_task_id=spt.sub_task_id end) "
      + "JOIN services s ON (s.service_id=ttd.service_id) "
      + "LEFT JOIN doctors d ON (d.doctor_id=spt.completed_by) "
      + "WHERE ttd.mr_no=? ORDER BY ttd.planned_date ASC,sst.display_order";

  /**
   * Gets the service sub tasks for patient.
   *
   * @param mrNo the mr no
   * @return the service sub tasks for patient
   * @throws SQLException the SQL exception
   */
  public List getServiceSubTasksForPatient(String mrNo) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(SERVICE_SUB_TASKS_DETAILS_FOR_PATIENT);
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String SERVICE_SUB_TASKS_FOR_PATIENT = " SELECT ttd.treatment_id,"
      + " 'TRTMT'||ttd.treatment_id as unique_id, spt.completed_by,"
      + "d.doctor_name, spt.completion_time, spt.status, "
      + "sst.sub_task_id, sst.desc_short, sst.desc_long, "
      + "sst.status as mas_task_status, ttd.service_id, spt.task_presc_id, s.service_name "
      + "FROM tooth_treatment_details ttd "
      + "JOIN services_presc_tasks spt using (treatment_id) "
      + "JOIN service_sub_tasks sst on (sst.sub_task_id=spt.sub_task_id) "
      + "JOIN services s ON (s.service_id=ttd.service_id) "
      + "LEFT JOIN doctors d ON (d.doctor_id=spt.completed_by) "
      + "WHERE ttd.mr_no=? ORDER BY ttd.planned_date ASC,sst.display_order";

  /**
   * Gets the saved service sub tasks for patient.
   *
   * @param mrNo the mr no
   * @return the saved service sub tasks for patient
   * @throws SQLException the SQL exception
   */
  public List getSavedServiceSubTasksForPatient(String mrNo) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(SERVICE_SUB_TASKS_FOR_PATIENT);
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String SERVICE_SUB_TASKS_DETAILS = " SELECT sst.sub_task_id, "
      + "sst.desc_short, sst.desc_long, sst.status, sst.service_id "
      + " FROM service_sub_tasks sst JOIN services s ON (s.service_id=sst.service_id) "
      + " WHERE sst.service_id=? AND sst.status='A' order by sst.display_order";

  /**
   * Gets the service sub tasks.
   *
   * @param serviceId the service id
   * @return the service sub tasks
   * @throws SQLException the SQL exception
   */
  public List getServiceSubTasks(String serviceId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(SERVICE_SUB_TASKS_DETAILS);
      ps.setString(1, serviceId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String GET_CHARGE_ID = "SELECT charge_id FROM bill_activity_charge "
      + " WHERE activity_id = ? AND act_description_id = ? AND payment_charge_head = 'SERSNP' ";

  /**
   * Gets the charge id.
   *
   * @param con the con
   * @param servicePrecId the service prec id
   * @param serviceId the service id
   * @return the charge id
   * @throws SQLException the SQL exception
   */
  public String getChargeId(Connection con, Integer servicePrecId, String serviceId)
      throws SQLException {
    PreparedStatement ps = con.prepareStatement(GET_CHARGE_ID);
    ResultSet rs = null;
    try {
      ps.setString(1, servicePrecId.toString());
      ps.setString(2, serviceId);
      rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getString("charge_id");
      }
    } finally {
      DataBaseUtil.closeConnections(null, ps, rs);
    }
    return null;
  }

  private static final String OP_IP_ORG_ID = " select pcm.op_rate_plan_id,pcm.ip_rate_plan_id "
      + "from patient_category_master pcm "
      + "join  patient_details pd on (pcm.category_id=pd.patient_category_id) "
      + "where pd.mr_no  = ? and pcm.status = 'A' ";

  /**
   * Gets the op io org id.
   *
   * @param mrNo the mr no
   * @return the op io org id
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getOpIoOrgId(String mrNo) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(OP_IP_ORG_ID);
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
