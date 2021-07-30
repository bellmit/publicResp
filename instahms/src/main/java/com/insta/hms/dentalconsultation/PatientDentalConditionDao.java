package com.insta.hms.dentalconsultation;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientDentalConditionDao extends GenericDAO {

  PatDentalConditionDetailsDao condetailsDao = new PatDentalConditionDetailsDao();

  public PatientDentalConditionDao() {
    super("patient_dental_condition_main");
  }

  /**
   * Gets the marker image details.
   *
   * @param mrNo the mr no
   * @return the marker image details
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public List<BasicDynaBean> getMarkerImageDetails(String mrNo) throws SQLException, IOException {
    BasicDynaBean patbean = PatientDetailsDAO.getPatientGeneralDetailsBean(mrNo);
    int age = (Integer) patbean.get("age");
    String ageIn = (String) patbean.get("agein");
    boolean adult = false;
    if (ageIn.equals("Y") && age >= 12) {
      adult = true;
    }

    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement(" SELECT details.*, main.mr_no, crown_status_desc, root_status_desc, "
              + "surface_status_name, option_name, material_name "
              + "FROM patient_dental_condition_main main "
              + "JOIN patient_dental_condition_details details "
              + "USING (dental_condition_id) "
              + "LEFT JOIN crown_status_master crown ON (details.tooth_part='crown' "
              + "and details.status_id=crown.crown_status_id) "
              + "LEFT JOIN tooth_root_status root ON (details.tooth_part='root' "
              + "and details.status_id=root.root_status_id) "
              + "LEFT JOIN tooth_surface_material_master material "
              + "ON (details.tooth_part IN ('left', 'right', 'center', 'top', 'bottom') "
              + "AND details.material_id=material.material_id)"
              + "LEFT JOIN tooth_surface_option_master option "
              + "ON (details.tooth_part IN ('left', 'right', 'center', 'top', 'bottom') "
              + "AND details.option_id=option.option_id)"
              + "LEFT JOIN tooth_surface_status_master condition "
              + "ON (details.tooth_part IN ('left', 'right', 'center', 'top', 'bottom') "
              + "AND details.status_id=condition.surface_status_id)"
              + " WHERE mr_no=? and patient_type=?");
      ps.setString(1, mrNo);
      ps.setString(2, adult ? "A" : "C");
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Record pat dental condition.
   *
   * @param con the con
   * @param params the params
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public String recordPatDentalCondition(Connection con, Map params)
      throws SQLException, IOException {

    String mrNo = ConversionUtils.getParamValue(params, "mr_no", "0");
    final String[] teeth = (String[]) params.get("dc_unv_number");
    BasicDynaBean patbean = PatientDetailsDAO.getPatientGeneralDetailsBean(mrNo);
    int age = (Integer) patbean.get("age");
    String ageIn = (String) patbean.get("agein");
    boolean adult = false;
    if (ageIn.equals("Y") && age >= 12) {
      adult = true;
    }

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("mr_no", mrNo);
    keys.put("patient_type", adult ? "A" : "C");
    BasicDynaBean mainRecord = findByKey(con, (List) null, keys);
    int dentalConditionId = 0;

    if (mainRecord == null) {
      BasicDynaBean bean = getBean();
      dentalConditionId = getNextSequence();
      bean.set("dental_condition_id", dentalConditionId);
      bean.set("mr_no", mrNo);
      bean.set("patient_type", adult ? "A" : "C");
      if (!insert(con, bean)) {
        return "Failed to insert the Patient dental condition main details..";
      }
    } else {
      dentalConditionId = (Integer) mainRecord.get("dental_condition_id");
      if (condetailsDao.findByKey(con, "dental_condition_id", dentalConditionId) != null) {
        condetailsDao.delete(con, "dental_condition_id", dentalConditionId);
      }

    }

    List errorFields = new ArrayList();
    List statuses = new ArrayList();
    for (int i = 0; i < teeth.length - 1; i++) {
      BasicDynaBean bean = condetailsDao.getBean();
      ConversionUtils.copyIndexToDynaBeanPrefixed(params, i, bean, errorFields, "dc_");
      bean.set("dental_condition_id", dentalConditionId);
      statuses.add(bean);
    }
    if (!errorFields.isEmpty()) {
      return "Incorrectly formatted values supplied in dental chart...";
    }
    if (!statuses.isEmpty()) {
      if (!condetailsDao.insertAll(con, statuses)) {
        return "Failed to save the dental condition..";
      }
    }
    return null;
  }

}
