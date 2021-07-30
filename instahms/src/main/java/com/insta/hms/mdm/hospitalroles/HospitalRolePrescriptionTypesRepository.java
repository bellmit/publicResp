package com.insta.hms.mdm.hospitalroles;

import com.insta.hms.common.DatabaseHelper;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class HospitalRolePrescriptionTypesRepository {

  private static final String INSERT_QUERY = "INSERT INTO hospital_role_prescription_types "
      + "(hosp_role_id, prescription_type) VALUES (?, ?)";

  /**
   * Insert.
   *
   * @param hospRoleId
   *          the hosp role id
   * @param prescriptionTypes
   *          the prescription types
   * @return the int[]
   */
  public int[] insert(int hospRoleId, List<String> prescriptionTypes) {
    if (prescriptionTypes == null || prescriptionTypes.isEmpty()) {
      return new int[0];
    }
    List<Object[]> queryParams = new ArrayList<>();
    for (String rxType : prescriptionTypes) {
      queryParams.add(new Object[] { hospRoleId, rxType });
    }
    return DatabaseHelper.batchInsert(INSERT_QUERY, queryParams);
  }

  private static final String GET_LIST = "SELECT prescription_type "
      + "FROM hospital_role_prescription_types " + "WHERE hosp_role_id = ?";

  /**
   * Get Prescription Types.
   * 
   * @param hospRoleId
   *          hopspital role id
   * @return the list
   */
  public List<String> getPrescriptionTypes(int hospRoleId) {
    List<BasicDynaBean> rxTypeDynaList = DatabaseHelper.queryToDynaList(GET_LIST,
        (Object) hospRoleId);
    List<String> rxTypes = new ArrayList<>();
    for (BasicDynaBean rxTypeBean : rxTypeDynaList) {
      rxTypes.add((String) rxTypeBean.get("prescription_type"));
    }
    return rxTypes;
  }

  private static final String DELETE_RX_TYPES = "DELETE FROM hospital_role_prescription_types "
      + "WHERE hosp_role_id = ?";

  /**
   * Delete mapped prescriptions types.
   * 
   * @param hospRoleId
   *          hospital role id
   * @return the int
   */
  public int deleteMappedPrescriptionTypes(int hospRoleId) {
    return DatabaseHelper.delete(DELETE_RX_TYPES, (Object) hospRoleId);
  }
}
