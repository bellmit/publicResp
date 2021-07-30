package com.insta.hms.core.clinical.consultation.prescriptions;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PendingPrescriptionsRemarksRepository extends GenericRepository {

  public PendingPrescriptionsRemarksRepository() {
    super("pending_prescription_details");
  }

  private static final String GET_LATEST_REMARKS_QUERY = "SELECT "
      + " ppd.remark AS remark, ppd.modified_at AS modified_date_time, "
      + " u.temp_username AS "
      + " remark_added_by FROM pending_prescription_details ppd "
      + " LEFT JOIN u_user u ON u.emp_username= ppd.modified_by "
      + " WHERE ppd.pending_prescription_id = ? AND  "
      + " (ppd.remark != null OR ppd.remark != '') AND NOT ppd.system_log "
      + " ORDER BY ppd.modified_at DESC LIMIT 1 ";

  public BasicDynaBean getLatestRemarks(Object pendingPrescId) {
    return DatabaseHelper.queryToDynaBean(GET_LATEST_REMARKS_QUERY, pendingPrescId);
  }

  private static final String REMARKS_QUERY = "SELECT ppd.remark, "
      + " COALESCE(u.temp_username,hrm.hosp_role_name) AS assigned_to, "
      + " ppd.assigned_to_user_id AS assigned_user_id, "
      + " ppd.assigned_to_role_id AS assigned_role_id, "
      + " ppd.modified_at AS modified_date_time, ppd.modified_by AS remark_added_by, "
      + " u2.temp_username AS created_by, hrm.hosp_role_name AS assigned_role_name "
      + " FROM pending_prescription_details ppd "
      + " LEFT JOIN u_user AS u on u.emp_username = ppd.assigned_to_user_id "
      + " LEFT JOIN u_user AS u2 on u2.emp_username = ppd.modified_by "
      + " LEFT JOIN hospital_roles_master hrm ON hrm.hosp_role_id = ppd.assigned_to_role_id "
      + " WHERE ppd.pending_prescription_id = ? ORDER BY modified_at DESC ";

  public List<BasicDynaBean> getRemarks(Object pendingPrescId) {
    return DatabaseHelper.queryToDynaList(REMARKS_QUERY, pendingPrescId);
  }
}
