package com.insta.hms.core.clinical.operationdetails;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Class OperationDetailsRepository.
 *
 * @author anup vishwas
 */
@Repository
public class OperationDetailsRepository extends GenericRepository {

  /**
   * Instantiates a new operation details repository.
   */
  public OperationDetailsRepository() {
    super("operation_details");
  }

  /** The Constant GET_OPERATION_DETAILS_FOR_FTL. */
  private static final String GET_OPERATION_DETAILS_FOR_FTL = " SELECT od.mr_no, od.patient_id,"
      + " od.wheel_in_time, od.wheel_out_time, od.surgery_start, od.surgery_end, od.specimen, "
      + " od.conduction_remarks, od.operation_status, od.prescribing_doctor, od.charge_type, "
      + " od.order_remarks, od.cancel_reason, od.added_to_bill, tm.theatre_name, "
      + " d.doctor_name as prescribed_by, op.operation_proc_id, op.operation_id, "
      + " om.operation_name, om.dept_id "
      + " FROM operation_details od "
      + " LEFT JOIN  operation_procedures op ON(op.operation_details_id = od.operation_details_id "
      + " AND op.oper_priority = 'P') "
      + " LEFT JOIN operation_master om ON(om.op_id=op.operation_id) "
      + " LEFT JOIN theatre_master tm ON(tm.theatre_id=od.theatre_id) "
      + " LEFT JOIN doctors d ON(d.doctor_id=od.prescribing_doctor) "
      + " WHERE od.operation_details_id = ? ";

  /**
   * Gets the operation details for FTL.
   *
   * @param opDetailsId
   *          the op details id
   * @return the operation details for FTL
   */
  public BasicDynaBean getOperationDetailsForFTL(Integer opDetailsId) {

    return DatabaseHelper.queryToDynaBean(GET_OPERATION_DETAILS_FOR_FTL,
        new Object[] { opDetailsId });
  }

  /** The Constant GET_OPERATION_TEAM_FOR_FTL. */
  private static final String GET_OPERATION_TEAM_FOR_FTL = "SELECT d.doctor_name,"
      + " ot.operation_speciality "
      + " FROM operation_details od  "
      + " JOIN operation_team ot ON(ot.operation_details_id = od.operation_details_id) "
      + " JOIN doctors d ON(d.doctor_id=ot.resource_id) "
      + " WHERE od.operation_details_id = ? ORDER BY operation_team_id ";

  /**
   * Gets the operation team.
   *
   * @param opDetailsId
   *          the op details id
   * @return the operation team
   */
  public List<BasicDynaBean> getOperationTeam(Integer opDetailsId) {

    return DatabaseHelper.queryToDynaList(GET_OPERATION_TEAM_FOR_FTL, new Object[] { opDetailsId });
  }

  /** The Constant GET_SURGERY_LIST_FOR_FTL. */
  private static final String GET_SURGERY_LIST_FOR_FTL = " SELECT om.operation_name, op.modifier, "
      + " od.operation_status, op.oper_priority"
      + " FROM operation_details od "
      + " JOIN operation_procedures op ON(op.operation_details_id=od.operation_details_id "
      + " AND oper_priority IN ('P','S')) "
      + " LEFT JOIN operation_master om ON(om.op_id=op.operation_id) "
      + " WHERE od.operation_details_id = ? "
      + " ORDER BY CASE op.oper_priority WHEN 'P' THEN 1 ELSE 2 END ";

  /**
   * Gets the surgery list for FTL.
   *
   * @param opDetailsId
   *          the op details id
   * @return the surgery list for FTL
   */
  public List<BasicDynaBean> getSurgeryListForFTL(Integer opDetailsId) {

    return DatabaseHelper.queryToDynaList(GET_SURGERY_LIST_FOR_FTL, new Object[] { opDetailsId });
  }

  /** The Constant GET_OPERATION_DETAILS_BY_PROCID. */
  public static final String GET_OPERATION_DETAILS_BY_PROCID = " SELECT ops.operation_id, "
      + " ops.operation_proc_id, om.dept_id, ops.operation_details_id, om.operation_name "
      + " FROM operation_details ods "
      + " JOIN operation_procedures ops ON (ods.operation_details_id=ops.operation_details_id) "
      + " JOIN operation_master om ON (om.op_id=ops.operation_id) "
      + " WHERE ops.operation_proc_id = ? ";

  /**
   * Gets the op details by proc id.
   *
   * @param opProcId
   *          the op proc id
   * @return the op details by proc id
   */
  public BasicDynaBean getOpDetailsByProcId(int opProcId) {

    return DatabaseHelper.queryToDynaBean(GET_OPERATION_DETAILS_BY_PROCID,
        new Object[] { opProcId });
  }

  /** The Constant GET_OPERATIONS_DETAILS. */
  public static final String GET_OPERATIONS_DETAILS = " SELECT ops.operation_id,"
      + " ops.operation_proc_id, om.dept_id, om.operation_name "
      + " FROM operation_details ods "
      + " JOIN operation_procedures ops ON (ods.operation_details_id=ops.operation_details_id) "
      + " JOIN operation_master om ON (om.op_id=ops.operation_id) "
      + " WHERE ods.patient_id = ? AND ods.operation_details_id = ? "
      + " order by ops.operation_proc_id";

  /**
   * Gets the operations.
   *
   * @param patientId
   *          the patient id
   * @param opDetailsId
   *          the op details id
   * @return the operations
   */
  public List getOperations(String patientId, int opDetailsId) {

    return DatabaseHelper.queryToDynaList(GET_OPERATIONS_DETAILS, patientId, opDetailsId);
  }

  /** The Constant GET_PRIMARY_PROCEDURES_BY_PRESCRIBED_ID. */
  private static final String GET_PRIMARY_PROCEDURES_BY_PRESCRIBED_ID = "SELECT * "
      + " FROM operation_details od LEFT JOIN operation_procedures op "
      + "    ON(op.operation_details_id=od.operation_details_id AND oper_priority = 'P')"
      + " LEFT JOIN operation_master om ON(om.op_id=op.operation_id)"
      + " WHERE op.prescribed_id = ?";

  /**
   * Gets the primary operation details by prescribed id.
   *
   * @param prescribedId
   *          the prescribed id
   * @return the primary operation details by prescribed id
   */
  public BasicDynaBean getPrimaryOperationDetailsByPrescribedId(Integer prescribedId) {
    return DatabaseHelper.queryToDynaBean(GET_PRIMARY_PROCEDURES_BY_PRESCRIBED_ID,
        new Object[] { prescribedId });
  }

  /** The Constant UPDATE_CANCEL_ADV_SURGERY. */
  private static final String UPDATE_CANCEL_ADV_SURGERY = " UPDATE operation_details "
      + " SET operation_status = 'X' WHERE operation_details_id IN "
      + " (select operation_details_id from operation_procedures where oper_priority = 'P' ";

  /**
   * Cancel advanced OT surgery.
   *
   * @param prescribedIdList
   *          the prescribed id list
   * @return the int
   */
  public int cancelAdvancedOTSurgery(List<Integer> prescribedIdList) {
    StringBuilder query = new StringBuilder(UPDATE_CANCEL_ADV_SURGERY);

    List<Object> params = new ArrayList<>();

    if (prescribedIdList != null && !prescribedIdList.isEmpty()) {
      String[] placeholdersArr = new String[prescribedIdList.size()];
      Arrays.fill(placeholdersArr, "?");
      query.append("AND prescribed_id IN ( ")
          .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(" ) )");
      params.addAll(prescribedIdList);
      return DatabaseHelper.update(query.toString(), params.toArray());
    }

    return 0;
  }

}
