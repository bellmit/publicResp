package com.insta.hms.orders;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class OrderDAO.
 */
public class OrderDAO {

  /** The Constant ALL_ORDERS_QUERY. */
  /*
   * Get a list of all orders.
   */
  private static final String ALL_ORDERS_QUERY = " SELECT * FROM (SELECT pov.*,null AS package_id,"
      + " COALESCE(pm.multi_visit_package,false) "
      + " AS multi_visit_package,null as pat_package_id" + " FROM patient_orders_view pov "
      + " LEFT JOIN packages pm ON(pm.package_id::text = pov.item_id)) AS foo"
      + " WHERE operation_ref IS NULL AND package_ref IS NULL AND multi_visit_package = false "
      + "  AND patient_id=? " + "  ORDER BY pres_timestamp,order_id";

  /**
   * Gets the all orders.
   *
   * @param patientId the patient id
   * @return the all orders
   * @throws SQLException the SQL exception
   */
  public List getAllOrders(String patientId) throws SQLException {
    return DataBaseUtil.queryToDynaList(ALL_ORDERS_QUERY, patientId);
  }

  /*
   * Get a list of all orders for multivisitpackage separately.
   */

  /** The all orders query for multi visit package. */
  private String allOrdersQueryForMultiVisitPackage = " SELECT pov.*,"
      + " COALESCE(pm.multi_visit_package,false) AS multi_visit_package,"
      + " pm.package_id,pp.pat_package_id FROM patient_orders_view pov"
      + " LEFT JOIN package_prescribed pp ON(pp.prescription_id = pov.package_ref) "
      + " LEFT JOIN packages pm ON(pm.package_id = pp.package_id)"
      + " WHERE operation_ref IS NULL AND package_ref IS NOT NULL "
      + " AND pm.multi_visit_package = true AND pov.patient_id=? # "
      + "  ORDER BY pres_timestamp,order_id";

  /**
   * Gets the all orders for multi visit package.
   *
   * @param patientId the patient id
   * @param filter    the filter
   * @return the all orders for multi visit package
   * @throws SQLException the SQL exception
   */
  public List getAllOrdersForMultiVisitPackage(String patientId, String filter)
      throws SQLException {
    if (filter != null && !filter.isEmpty()) {
      allOrdersQueryForMultiVisitPackage = allOrdersQueryForMultiVisitPackage.replace("#",
          " AND pov.type=?");
      return DataBaseUtil.queryToDynaList(allOrdersQueryForMultiVisitPackage, patientId, filter);
    } else {
      allOrdersQueryForMultiVisitPackage = allOrdersQueryForMultiVisitPackage.replace("#", "");
      return DataBaseUtil.queryToDynaList(allOrdersQueryForMultiVisitPackage, patientId);
    }
  }

  /** The Constant FILTERED_ORDERS_QUERY. */
  /*
   * Get a list of all orders, filtered by type.
   */
  private static final String FILTERED_ORDERS_QUERY = " SELECT *,null AS package_id,"
      + " false AS multi_visit_package,null as pat_package_id FROM patient_orders_view "
      + " WHERE operation_ref IS NULL  AND package_ref IS NULL AND patient_id=? AND type=? "
      + "  ORDER BY pres_timestamp";

  /**
   * Gets the filtered orders.
   *
   * @param patientId the patient id
   * @param filter    the filter
   * @return the filtered orders
   * @throws SQLException the SQL exception
   */
  public List getFilteredOrders(String patientId, String filter) throws SQLException {
    return DataBaseUtil.queryToDynaList(FILTERED_ORDERS_QUERY, new Object[] { patientId, filter });
  }

  /** The Constant ORDERS_BY_TYPE_QUERY. */
  private static final String ORDERS_BY_TYPE_QUERY = " SELECT order_id, common_order_id, type,"
      + " sub_type, sub_type_name, activity_code, "
      + " item_id, item_name, pres_doctor_id, to_char(pres_timestamp AT TIME ZONE "
      + "(SELECT  current_setting('TIMEZONE')) AT TIME ZONE 'UTC',"
      + " 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as pres_timestamp , "
      + " pres_doctor_name, package_ref FROM patient_orders_view "
      + " WHERE patient_id=? AND type=? ORDER BY pres_timestamp";

  /**
   * Gets the patient orders by type.
   *
   * @param con       the con
   * @param patientId the patient id
   * @param type      the type
   * @return the patient orders by type
   * @throws SQLException the SQL exception
   */
  public List getPatientOrdersByType(Connection con, String patientId, String type)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(con, ORDERS_BY_TYPE_QUERY,
        new Object[] { patientId, type });
  }

  /** The Constant FILTERED_ORDERS_PLUS_DIAG_PKG_QUERY. */
  /*
   * Get a list of all orders, filtered by type, and include diag packages
   */
  private static final String FILTERED_ORDERS_PLUS_DIAG_PKG_QUERY = " SELECT *,null AS package_id,"
      + " false AS multi_visit_package,null as pat_package_id "
      + "FROM patient_orders_view " + " WHERE operation_ref IS NULL  AND package_ref IS NULL "
      + "  AND patient_id=? AND (type=? OR (type='Package')) "
      + "  ORDER BY pres_timestamp";

  /**
   * Gets the filtered orders and diag packages.
   *
   * @param patientId the patient id
   * @param filter    the filter
   * @return the filtered orders and diag packages
   * @throws SQLException the SQL exception
   */
  public List getFilteredOrdersAndDiagPackages(String patientId, String filter)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(FILTERED_ORDERS_PLUS_DIAG_PKG_QUERY,
        new Object[] { patientId, filter });
  }


  /** The Constant FILTERED_ORDERS_PLUS_RADIOLOGY_PKG_QUERY. */
  /*
   * Get a list of all orders, filtered by type, and include Radiology packages
   */
  private static final String FILTERED_ORDERS_PLUS_RADIO_PKG_QUERY = " SELECT *,null AS package_id,"
      + " false AS multi_visit_package,null as pat_package_id "
      + " FROM patient_orders_view " 
      + " WHERE (package_category_id != -2 OR package_category_id is null)"
      + " AND operation_ref IS NULL  AND package_ref IS NULL "
      + " AND patient_id=? AND (type=? OR (type='Package'))  "
      + " ORDER BY pres_timestamp";

  /**
   * Gets the filtered orders and Radiology packages.
   *
   * @param patientId the patient id
   * @param filter    the filter
   * @return the filtered orders and diag packages
   * @throws SQLException the SQL exception
   */
  public List getFilteredOrdersAndRadioPackages(String patientId, String filter)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(FILTERED_ORDERS_PLUS_RADIO_PKG_QUERY,
        new Object[] { patientId, filter });
  }

  /** The Constant TEST_PRESCRIPTIONS. */
  /*
   * Get various kinds of prescriptions.
   */
  private static final String TEST_PRESCRIPTIONS = " SELECT dc.patient_id, "
      + " tp.op_test_pres_id as pres_id, d.doctor_id, d.doctor_name, "
      + "   atp.test_name as name, tp.test_remarks as remarks, tp.test_id, pp.pri_pre_auth_no, "
      + " pp.pri_pre_auth_mode_id, pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id  "
      + " FROM patient_prescription pp  "
      + " JOIN patient_test_prescriptions tp ON (pp.patient_presc_id=tp.op_test_pres_id)  "
      + "   JOIN doctor_consultation dc ON (dc.consultation_id = pp.consultation_id)  "
      + "   LEFT JOIN doctors d ON (dc.doctor_name = d.doctor_id)  "
      + "   JOIN all_tests_pkgs_view atp ON (tp.test_id = atp.test_id) "
      + " WHERE pp.status='P' AND coalesce(pp.consultation_id, 0)!=0 AND dc.patient_id=? "

      + " UNION ALL "

      + " SELECT pp.visit_id as patient_id, "
      + " tp.op_test_pres_id as pres_id, null as doctor_id, null as doctor_name, "
      + "   atp.test_name as name, tp.test_remarks as remarks, tp.test_id, pp.pri_pre_auth_no, "
      + " pp.pri_pre_auth_mode_id, pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id  "
      + " FROM patient_prescription pp  "
      + " JOIN patient_test_prescriptions tp ON (pp.patient_presc_id=tp.op_test_pres_id) "
      + "   JOIN all_tests_pkgs_view atp ON (tp.test_id = atp.test_id)  "
      + " WHERE pp.status='P' AND coalesce(pp.consultation_id, 0)=0 "
      + " AND pp.visit_id=?  order by pres_id";

  /**
   * Gets the patient test prescriptions.
   *
   * @param patientId the patient id
   * @return the patient test prescriptions
   * @throws SQLException the SQL exception
   */
  public List getPatientTestPrescriptions(String patientId) throws SQLException {
    return DataBaseUtil.queryToDynaList(TEST_PRESCRIPTIONS, patientId, patientId);
  }

  /** The Constant TEST_PRESCRIPTIONS_BY_IDS. */
  private static final String TEST_PRESCRIPTIONS_BY_IDS = " SELECT"
      + " coalesce(pp.visit_id, dc.patient_id) as patient_id,"
      + " tp.op_test_pres_id as pres_id, d.doctor_id, d.doctor_name, "
      + "   atp.test_name as name, tp.test_remarks as remarks, tp.test_id, pp.pri_pre_auth_no, "
      + " pp.pri_pre_auth_mode_id, pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id "
      + " FROM patient_prescription pp "
      + " LEFT JOIN doctor_consultation dc ON (dc.consultation_id=pp.consultation_id) "
      + " JOIN patient_test_prescriptions tp ON (pp.patient_presc_id=tp.op_test_pres_id) "
      + "   LEFT JOIN doctors d ON (dc.doctor_name = d.doctor_id) "
      + "   JOIN all_tests_pkgs_view atp ON (tp.test_id = atp.test_id) " + " WHERE pp.status='P' ";

  /**
   * Gets the patient test prescriptions.
   *
   * @param patientPrescIds the patient presc ids
   * @return the patient test prescriptions
   * @throws SQLException the SQL exception
   */
  public List getPatientTestPrescriptions(String[] patientPrescIds) throws SQLException {
    StringBuilder builder = new StringBuilder(TEST_PRESCRIPTIONS_BY_IDS);
    // DataBaseUtil.addWhereFieldInList(builder, "patient_presc_id",
    // Arrays.asList(patientPrescIds));
    String[] placeHolderArr = new String[patientPrescIds.length];
    Arrays.fill(placeHolderArr, "?");
    String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
    builder.append("AND patient_presc_id in ( " + placeHolders + ")");
    builder.append(" order by patient_presc_id ");

    // Connection con = DataBaseUtil.getConnection();
    // PreparedStatement ps = null;
    List<Integer> values = new ArrayList<Integer>();
    try {
      // ps = con.prepareStatement(builder.toString());
      // int i=1;
      if (patientPrescIds != null) {
        for (String val : patientPrescIds) {
          values.add(Integer.parseInt(val));
          // ps.setInt(i++, Integer.parseInt(val));
        }
      }
      return DataBaseUtil.queryToDynaList(builder.toString(), values.toArray());
      // return DataBaseUtil.queryToDynaList(ps);
    } finally {
      // DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant OPERATION_PRESCRIPTIONSBY_IDS. */
  private static final String OPERATION_PRESCRIPTIONSBY_IDS = " SELECT operation_id,"
      + " prescription_id as pres_id, "
      + " coalesce(pp.visit_id, dc.patient_id) as patient_id, "
      + " operation_name, pp.pri_pre_auth_no, "
      + " pp.pri_pre_auth_mode_id, pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id "
      + " FROM patient_prescription pp"
      + " LEFT JOIN doctor_consultation dc ON (dc.consultation_id=pp.consultation_id) "
      + " JOIN patient_operation_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id) "
      + " JOIN operation_master op ON (op.op_id=pop.operation_id)" + " WHERE pp.status='P' ";

  /**
   * Gets the operation prescriptions.
   *
   * @param patientPrescIds the patient presc ids
   * @return the operation prescriptions
   * @throws SQLException the SQL exception
   */
  public List getOperationPrescriptions(String[] patientPrescIds) throws SQLException {
    StringBuilder builder = new StringBuilder(OPERATION_PRESCRIPTIONSBY_IDS);
    // DataBaseUtil.addWhereFieldInList(builder, "patient_presc_id",
    // Arrays.asList(patientPrescIds));
    String[] placeHolderArr = new String[patientPrescIds.length];
    Arrays.fill(placeHolderArr, "?");
    String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
    builder.append("AND patient_presc_id in ( " + placeHolders + ")");
    builder.append(" order by patient_presc_id ");
    // Connection con = DataBaseUtil.getConnection();
    // PreparedStatement ps = null;
    List<Integer> values = new ArrayList<Integer>();
    try {
      // ps = con.prepareStatement(builder.toString());
      // int i=1;
      if (patientPrescIds != null) {
        for (String val : patientPrescIds) {
          values.add(Integer.parseInt(val));
          // ps.setInt(i++, Integer.parseInt(val));
        }
      }
      return DataBaseUtil.queryToDynaList(builder.toString(), values.toArray());
      // return DataBaseUtil.queryToDynaList(ps);
    } finally {
      // DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant OPERATION_PRESCRIPTIONS. */
  private static final String OPERATION_PRESCRIPTIONS = " SELECT dc.patient_id,  "
      + " operation_id, prescription_id as pres_id, operation_name, pp.pri_pre_auth_no, "
      + " pp.pri_pre_auth_mode_id, pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id  "
      + " FROM patient_prescription pp "
      + " JOIN patient_operation_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id) "
      + "  JOIN doctor_consultation dc ON (dc.consultation_id=pp.consultation_id) "
      + " JOIN operation_master op ON (op.op_id=pop.operation_id) "
      + " WHERE pp.status='P' and coalesce(pp.consultation_id, 0)!=0 and dc.patient_id=? "
      + " UNION ALL " + " SELECT pp.visit_id as patient_id, "
      + " operation_id, prescription_id as pres_id, operation_name, pp.pri_pre_auth_no, "
      + " pp.pri_pre_auth_mode_id, pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id  "
      + " FROM patient_prescription pp "
      + " JOIN patient_operation_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id) "
      + " JOIN operation_master op ON (op.op_id=pop.operation_id) "
      + " WHERE pp.status='P' and coalesce(pp.consultation_id, 0)=0 "
      + " and pp.visit_id=? order by pres_id ";

  /**
   * Gets the operation prescriptions.
   *
   * @param patientId the patient id
   * @return the operation prescriptions
   * @throws SQLException the SQL exception
   */
  public List getOperationPrescriptions(String patientId) throws SQLException {
    return DataBaseUtil.queryToDynaList(OPERATION_PRESCRIPTIONS, patientId, patientId);
  }

  /** The Constant SERVICE_PRESCRIPTIONS. */
  private static final String SERVICE_PRESCRIPTIONS = " SELECT dc.patient_id, "
      + "  sp.op_service_pres_id as pres_id, d.doctor_id, d.doctor_name, "
      + "   s.service_name as name, sp.service_remarks as remarks, "
      + " sp.service_id, sp.tooth_unv_number, "
      + "  sp.tooth_fdi_number, s.tooth_num_required, sp.qty as service_qty, pp.pri_pre_auth_no,  "
      + "  pp.pri_pre_auth_mode_id, pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id  "
      + " FROM patient_prescription pp "
      + "  JOIN patient_service_prescriptions sp ON (pp.patient_presc_id=sp.op_service_pres_id) "
      + " JOIN doctor_consultation dc ON (dc.consultation_id = pp.consultation_id)  "
      + " LEFT JOIN doctors d ON (dc.doctor_name = d.doctor_id)  "
      + " JOIN services s ON (sp.service_id = s.service_id)  "
      + " WHERE pp.status='P' AND coalesce(pp.consultation_id, 0)!=0 AND dc.patient_id=? "

      + " UNION ALL "

      + " SELECT pp.visit_id as patient_id, "
      + " sp.op_service_pres_id as pres_id, null as doctor_id, null as doctor_name, "
      + "    s.service_name as name, sp.service_remarks as remarks, "
      + " sp.service_id, sp.tooth_unv_number, "
      + " sp.tooth_fdi_number, s.tooth_num_required, sp.qty as service_qty, pp.pri_pre_auth_no,  "
      + " pp.pri_pre_auth_mode_id, pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id  "
      + " FROM patient_prescription pp "
      + " JOIN patient_service_prescriptions sp ON (pp.patient_presc_id=sp.op_service_pres_id) "
      + " JOIN services s ON (sp.service_id = s.service_id)  "
      + " WHERE pp.status='P' AND coalesce(pp.consultation_id, 0)=0 "
      + " AND pp.visit_id=?  order by pres_id ";

  /**
   * Gets the patient service prescriptions.
   *
   * @param patientId the patient id
   * @return the patient service prescriptions
   * @throws SQLException the SQL exception
   */
  public List getPatientServicePrescriptions(String patientId) throws SQLException {
    return DataBaseUtil.queryToDynaList(SERVICE_PRESCRIPTIONS, patientId, patientId);
  }

  /** The Constant SERVICE_PRESCRIPTIONS_BY_IDS. */
  private static final String SERVICE_PRESCRIPTIONS_BY_IDS = " SELECT"
      + " coalesce(pp.visit_id, dc.patient_id) as patient_id, "
      + " sp.op_service_pres_id as pres_id, d.doctor_id, d.doctor_name, "
      + "   s.service_name as name, sp.service_remarks as remarks, "
      + " sp.service_id, sp.tooth_unv_number, "
      + " sp.tooth_fdi_number, s.tooth_num_required, sp.qty as service_qty, pp.pri_pre_auth_no, "
      + " pp.pri_pre_auth_mode_id, pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id "
      + " FROM patient_prescription pp"
      + "  LEFT JOIN doctor_consultation dc ON (dc.consultation_id=pp.consultation_id)"
      + " JOIN patient_service_prescriptions sp ON (pp.patient_presc_id=sp.op_service_pres_id)"
      + "   LEFT JOIN doctors d ON (dc.doctor_name = d.doctor_id) "
      + "   JOIN services s ON (sp.service_id = s.service_id) " + " WHERE pp.status='P' ";

  /**
   * Gets the patient service prescriptions.
   *
   * @param patientPrescIds the patient presc ids
   * @return the patient service prescriptions
   * @throws SQLException the SQL exception
   */
  public List getPatientServicePrescriptions(String[] patientPrescIds) throws SQLException {
    StringBuilder builder = new StringBuilder(SERVICE_PRESCRIPTIONS_BY_IDS);
    // DataBaseUtil.addWhereFieldInList(builder, "patient_presc_id",
    // Arrays.asList(patientPrescIds));
    String[] placeHolderArr = new String[patientPrescIds.length];
    Arrays.fill(placeHolderArr, "?");
    String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
    builder.append("AND patient_presc_id in ( " + placeHolders + ")");
    builder.append(" order by patient_presc_id ");
    // Connection con = DataBaseUtil.getConnection();
    // PreparedStatement ps = null;
    List<Integer> values = new ArrayList<Integer>();
    try {
      // ps = con.prepareStatement(builder.toString());
      // int i=1;
      if (patientPrescIds != null) {
        for (String val : patientPrescIds) {
          values.add(Integer.parseInt(val));
          // ps.setInt(i++, Integer.parseInt(val));
        }
      }
      return DataBaseUtil.queryToDynaList(builder.toString(), values.toArray());
      // return DataBaseUtil.queryToDynaList(ps);
    } finally {
      // DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant DIET_PRESCRIPTIONS. */
  private static final String DIET_PRESCRIPTIONS = " SELECT dp.visit_id as patient_id,"
      + " diet_pres_id as pres_id, d.doctor_id, d.doctor_name, "
      + "   dp.meal_name as name, dp.special_instructions as remarks, "
      + "   dp.meal_date, dp.meal_timing, dp.meal_time " + " FROM patient_diet_prescriptions dp "
      + "   JOIN diet_master dm USING (meal_name) "
      + "   JOIN doctors d ON (dp.prescribed_by = d.doctor_id) "
      + " WHERE added_to_bill = 'f' AND dp.visit_id=? order by diet_pres_id";

  /**
   * Gets the patient diet prescriptions.
   *
   * @param patientId the patient id
   * @return the patient diet prescriptions
   * @throws SQLException the SQL exception
   */
  public List getPatientDietPrescriptions(String patientId) throws SQLException {
    return DataBaseUtil.queryToDynaList(DIET_PRESCRIPTIONS, patientId);
  }

  /** The Constant CONSULTATION_PRESCRIPTIONS_BY_IDS. */
  private static final String CONSULTATION_PRESCRIPTIONS_BY_IDS = " SELECT"
      + " coalesce(pp.visit_id, dc.patient_id) as patient_id, "
      + " cp.prescription_id AS pres_id," + " dt.doctor_id, dt.doctor_name, d.doctor_name AS name, "
      + " cp.doctor_id as cross_cons_doctor_id, "
      + " case when pr.visit_type='o' then -1 when visit_type='i' "
      + " then -3 end as head, cons_remarks as remarks,"
      + "  pp.consultation_id, pp.prescribed_date as prescription_date, "
      + " pp.status as added_to_bill, cp.username,"
      + "  ct.consultation_type, dp.dept_name, pp.pri_pre_auth_no, pp.pri_pre_auth_mode_id, "
      + " pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id" + " FROM patient_prescription pp"
      + "  LEFT JOIN doctor_consultation dc ON (dc.consultation_id=pp.consultation_id)"
      + " JOIN patient_consultation_prescriptions cp ON (pp.patient_presc_id=cp.prescription_id) "
      + " JOIN patient_registration pr ON (pr.patient_id=dc.patient_id) "
      + "  JOIN doctors d ON (cp.doctor_id = d.doctor_id) "
      + "   JOIN department dp ON(d.dept_id = dp.dept_id) "
      + "  LEFT JOIN doctors dt ON (dc.doctor_name = dt.doctor_id)  "
      + "  LEFT JOIN consultation_types ct ON (dc.head = ct.consultation_type_id::text)"
      + " WHERE pp.status='P' ";

  /**
   * Gets the patient consultation prescriptions.
   *
   * @param patientPrescIds the patient presc ids
   * @return the patient consultation prescriptions
   * @throws SQLException the SQL exception
   */
  public List getPatientConsultationPrescriptions(String[] patientPrescIds) throws SQLException {
    StringBuilder builder = new StringBuilder(CONSULTATION_PRESCRIPTIONS_BY_IDS);
    // DataBaseUtil.addWhereFieldInList(builder, "patient_presc_id",
    // Arrays.asList(patientPrescIds));
    String[] placeHolderArr = new String[patientPrescIds.length];
    Arrays.fill(placeHolderArr, "?");
    String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
    builder.append("AND patient_presc_id in ( " + placeHolders + ")");
    builder.append(" order by patient_presc_id ");

    // Connection con = DataBaseUtil.getConnection();
    // PreparedStatement ps = null;
    List<Integer> values = new ArrayList<Integer>();
    try {
      // ps = con.prepareStatement(builder.toString());
      // int i=1;
      if (patientPrescIds != null) {
        for (String val : patientPrescIds) {
          values.add(Integer.parseInt(val));
          // ps.setInt(i++, Integer.parseInt(val));
        }
      }
      return DataBaseUtil.queryToDynaList(builder.toString(), values.toArray());
    } finally {
      // DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant CONSULTATION_PRESCRIPTIONS. */
  private static final String CONSULTATION_PRESCRIPTIONS = " SELECT dc.patient_id,"
      + " cp.prescription_id AS pres_id, "
      + " dt.doctor_id, dt.doctor_name, d.doctor_name AS name, "
      + " cp.doctor_id as cross_cons_doctor_id, "
      + " case when pr.visit_type='o' then -1 when visit_type='i' "
      + " then -3 end as head, cons_remarks as remarks, "
      + "  pp.consultation_id, pp.prescribed_date as prescription_date, "
      + " pp.status as added_to_bill, cp.username, "
      + "  ct.consultation_type, dp.dept_name, pp.pri_pre_auth_no, "
      + " pp.pri_pre_auth_mode_id, pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id "
      + " FROM patient_prescription pp "
      + " JOIN patient_consultation_prescriptions cp ON (pp.patient_presc_id=cp.prescription_id) "
      + "  JOIN doctor_consultation dc USING (consultation_id)  "
      + " JOIN patient_registration pr ON (pr.patient_id=dc.patient_id) "
      + "  JOIN doctors d ON (cp.doctor_id = d.doctor_id)  "
      + "    JOIN department dp ON(d.dept_id = dp.dept_id)  "
      + "  LEFT JOIN doctors dt ON (dc.doctor_name = dt.doctor_id) "
      + "  LEFT JOIN consultation_types ct ON (dc.head = ct.consultation_type_id::text) "
      + " WHERE pp.status='P' AND coalesce(pp.consultation_id, 0)!=0 AND dc.patient_id=? "
      + " UNION ALL " + " SELECT pp.visit_id as patient_id, cp.prescription_id AS pres_id, "
      + " null as doctor_id, null as doctor_name, d.doctor_name AS name, "
      + " cp.doctor_id as cross_cons_doctor_id, "
      + " case when pr.visit_type='o' then -1 when visit_type='i' "
      + " then -3 end as head, cons_remarks as remarks, "
      + "  pp.consultation_id, pp.prescribed_date as prescription_date, "
      + " pp.status as added_to_bill, cp.username, "
      + "  ct.consultation_type, dp.dept_name, pp.pri_pre_auth_no,  "
      + " pp.pri_pre_auth_mode_id, pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id "
      + " FROM patient_prescription pp "
      + " JOIN patient_consultation_prescriptions cp ON (pp.patient_presc_id=cp.prescription_id) "
      + " JOIN patient_registration pr ON (pr.patient_id=pp.visit_id)  "
      + "  JOIN doctors d ON (cp.doctor_id = d.doctor_id)  "
      + "    JOIN department dp ON(d.dept_id = dp.dept_id)  "
      + "  LEFT JOIN consultation_types ct ON "
      + "  ((case when pr.visit_type='o' then -1 when visit_type='i' "
      + " then -3 end)::text = ct.consultation_type_id::text) "
      + " WHERE pp.status='P' AND coalesce(pp.consultation_id, 0)=0 "
      + " AND pp.visit_id=?  order by pres_id ";

  /**
   * Gets the patient consultation prescriptions.
   *
   * @param patientId the patient id
   * @return the patient consultation prescriptions
   * @throws SQLException the SQL exception
   */
  public List getPatientConsultationPrescriptions(String patientId) throws SQLException {
    return DataBaseUtil.queryToDynaList(CONSULTATION_PRESCRIPTIONS, patientId, patientId);
  }

  /** The Constant PRESCRIBED_OPERATIONS. */
  private static final String PRESCRIBED_OPERATIONS = " SELECT  "
      + "  bps.prescribed_id AS id, bps.operation_name as operation_id, om.operation_name as name, "
      + "  sdoc.doctor_name AS surgeon_name, adoc.doctor_name AS anaesthetist_name, "
      + "  tm.theatre_name AS theatre, start_datetime, end_datetime, "
      + "  bps.status, bps.finalization_status, b.status AS bill_status, "
      + " bps.remarks, consultant_doctor, "
      + "  doc.doctor_name,common_order_id,prescribed_date,foo.secondary_operations "
      + " FROM bed_operation_schedule bps "
      + "  JOIN operation_master om ON (om.op_id = bps.operation_name) "
      + "  LEFT JOIN  theatre_master tm ON (theatre_id = bps.theatre_name)  "
      + "  LEFT OUTER JOIN doctors sdoc ON (sdoc.doctor_id = bps.surgeon) "
      + "  LEFT OUTER JOIN doctors adoc ON (adoc.doctor_id = bps.anaesthetist) "
      + "  LEFT OUTER JOIN doctors doc ON (doc.doctor_id = bps.consultant_doctor) "
      + "  LEFT JOIN bill_activity_charge bac ON (bac.activity_id=bps.prescribed_id::text "
      + "    AND bac.activity_code='OPE') " + "  LEFT JOIN bill_charge bc USING (charge_id) "
      + "  LEFT JOIN bill b USING (bill_no) " + " LEFT JOIN "
      + " (select textcat_commacat(opm.operation_name) as secondary_operations,bos.prescribed_id"
      + "  FROM bed_operation_secondary bos "
      + "  JOIN operation_master opm ON(opm.op_id = bos.operation_id) "
      + "  group by bos.prescribed_id) as foo " + "  ON(foo.prescribed_id = bps.prescribed_id) ";

  /** The Constant GET_PRESCRIBED_OPERATIONS. */
  private static final String GET_PRESCRIBED_OPERATIONS = PRESCRIBED_OPERATIONS
      + " WHERE bps.patient_id=? AND package_ref IS NULL ORDER BY bps.operation_name";

  /**
   * Gets the operations.
   *
   * @param patientId the patient id
   * @return the operations
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getOperations(String patientId) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_PRESCRIBED_OPERATIONS, patientId);
  }

  /** The Constant GET_PACKAGE_REF_OPERATION. */
  private static final String GET_PACKAGE_REF_OPERATION = PRESCRIBED_OPERATIONS
      + "WHERE bps.patient_id=? AND package_ref=? ";

  /**
   * Gets the package ref operation.
   *
   * @param patientId the patient id
   * @param prescId   the presc id
   * @return the package ref operation
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getPackageRefOperation(String patientId, int prescId)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_PACKAGE_REF_OPERATION,
        new Object[] { patientId, prescId });
  }

  /** The Constant ALL_ORDERS_COMMON_QUERY. */
  /*
   * Get a list of all orders with the given common_order_id, excluding operations, package ref and
   * operation ref orders.
   */
  private static final String ALL_ORDERS_COMMON_QUERY = " SELECT p.*,"
      + " (bc.amount) as pack_amount FROM patient_orders_view p"
      + " LEFT JOIN LATERAL (select order_number,sum(amount) as amount"
      + " FROM bill_charge bc WHERE bc.order_number=p.common_order_id AND bc.charge_group='PKG' "
      + " GROUP by order_number) bc ON bc.order_number=p.common_order_id"
      + " WHERE  package_ref IS NULL AND operation_ref IS NULL "
      + " AND patient_id=? AND common_order_id=?  AND status != 'X'" + " ORDER BY pres_timestamp";

  /**
   * Gets the all orders by common order id.
   *
   * @param patientId     the patient id
   * @param commonOrderId the common order id
   * @return the all orders by common order id
   * @throws SQLException the SQL exception
   */
  public List getAllOrdersByCommonOrderId(String patientId, int commonOrderId) throws SQLException {
    return DataBaseUtil.queryToDynaList(ALL_ORDERS_COMMON_QUERY,
        new Object[] { patientId, commonOrderId });
  }

  /** The Constant OPERATIONS_COMMON_QUERY. */
  /*
   * Get a list of operations ordered by common order id
   */
  private static final String OPERATIONS_COMMON_QUERY = PRESCRIBED_OPERATIONS
      + "WHERE patient_id=? AND common_order_id = ? AND bps.status != 'X' ";

  /**
   * Gets the operations by common order id.
   *
   * @param patientId     the patient id
   * @param commonOrderId the common order id
   * @return the operations by common order id
   * @throws SQLException the SQL exception
   */
  public List getOperationsByCommonOrderId(String patientId, int commonOrderId)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(OPERATIONS_COMMON_QUERY,
        new Object[] { patientId, commonOrderId });
  }

  /** The Constant OPERATION_REFS_COMMON_QUERY. */
  /*
   * Get a list of normal orders referenced by operation as well as part of the given common order
   * Id. Note that this can include ref-orders of operations which have a different common order id.
   */
  private static final String OPERATION_REFS_COMMON_QUERY = "SELECT * FROM patient_orders_view "
      + " WHERE patient_id=? AND operation_ref IS NOT NULL AND common_order_id=? AND status != 'X' "
      + " ORDER BY pres_timestamp ";

  /**
   * Gets the operation ref orders by common order id.
   *
   * @param patientId     the patient id
   * @param commonOrderId the common order id
   * @return the operation ref orders by common order id
   * @throws SQLException the SQL exception
   */
  public List getOperationRefOrdersByCommonOrderId(String patientId, int commonOrderId)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(OPERATION_REFS_COMMON_QUERY,
        new Object[] { patientId, commonOrderId });
  }

  /** The Constant OPERATION_ORDERS. */
  /*
   * Get a list of sub-orders referenced by the given operation order.
   */
  private static final String OPERATION_ORDERS = "SELECT * FROM patient_orders_view "
      + "WHERE patient_id=? AND operation_ref=? ORDER BY pres_timestamp ";

  /**
   * Gets the operation ref orders.
   *
   * @param visitId the visit id
   * @param opId    the op id
   * @return the operation ref orders
   * @throws SQLException the SQL exception
   */
  // although operation_ref is all we need, using visitId (which has an index)
  // makes it faster.
  public static List<BasicDynaBean> getOperationRefOrders(String visitId, int opId)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(OPERATION_ORDERS, new Object[] { visitId, opId });
  }

  /** The Constant PACKAGE_ORDERS. */
  /*
   * Get a list of sub-orders referenced by the given package order.
   */
  private static final String PACKAGE_ORDERS = "SELECT * FROM patient_orders_view "
      + "WHERE patient_id=? AND package_ref=? ORDER BY pres_timestamp ";

  /**
   * Gets the package ref orders.
   *
   * @param visitId the visit id
   * @param prescId the presc id
   * @return the package ref orders
   * @throws SQLException the SQL exception
   */
  // although operation_ref is all we need, using visitId (which has an index)
  // makes it faster.
  public static List<BasicDynaBean> getPackageRefOrders(String visitId, int prescId)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(PACKAGE_ORDERS, new Object[] { visitId, prescId });
  }

  /** The Constant PACKAGE_REF_ORDERS. */
  private static final String PACKAGE_REF_ORDERS = "SELECT * FROM patient_orders_view "
      + "WHERE patient_id=? AND common_order_id=? "
      + " AND package_ref IS NOT NULL ORDER BY pres_timestamp ";

  /**
   * Gets the package ref orders by common order id.
   *
   * @param visitId       the visit id
   * @param commonOrderId the common order id
   * @return the package ref orders by common order id
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getPackageRefOrdersByCommonOrderId(String visitId,
      int commonOrderId) throws SQLException {
    return DataBaseUtil.queryToDynaList(PACKAGE_REF_ORDERS,
        new Object[] { visitId, commonOrderId });
  }

  /** The Constant BILL_BEDS_NOT_FINALIZED. */
  public static final String BILL_BEDS_NOT_FINALIZED = "SELECT"
      + " ipb.bed_id, ipb.bed_state, ipb.status, "
      + " ipb.start_date, ipb.end_date, ipb.admit_id, wn.ward_name, bn.bed_name, "
      + " (CASE WHEN bn.bed_type IN (SELECT distinct intensive_bed_type FROM icu_bed_charges) THEN "
      + "   'ICU' ELSE bn.bed_type END ) AS type_of_bed, pr.bed_type,"
      + " ipb.is_bystander,ipb.charged_bed_type  " + " FROM ip_bed_details ipb "
      + " JOIN bill b ON (b.visit_id = ipb.patient_id)"
      + " JOIN bill_charge bc ON (bc.bill_no = b.bill_no)" + " JOIN bill_activity_charge bac ON "
      + "  (bac.charge_id = bc.charge_id " + "   AND  ( bac.activity_id = ipb.admit_id::text "
      + " OR bac.activity_id = ipb.ref_admit_id::text ) " + "   AND bac.activity_code = 'BED')"
      + "  JOIN bed_names bn ON (bn.bed_id = ipb.bed_id)"
      + "  JOIN ward_names wn ON (wn.ward_no = bn.ward_no)"
      + "  JOIN patient_registration pr ON (pr.patient_id = ipb.patient_id)"
      + " WHERE ipb.patient_id = ? AND ipb.bed_state != 'F' "
      + " AND ipb.status IN ('A','C','R') AND  b.bill_no = ? "
      + " ORDER BY ipb.start_date, ipb.end_date";

  /**
   * Gets the bill bed details not finalized.
   *
   * @param visitId the visit id
   * @param billNo  the bill no
   * @return the bill bed details not finalized
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getBillBedDetailsNotFinalized(String visitId, String billNo)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> lis = new ArrayList<BasicDynaBean>();
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(BILL_BEDS_NOT_FINALIZED);
      ps.setString(1, visitId);
      ps.setString(2, billNo);
      lis = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return lis;
  }

  /** The Constant BILL_EQUIPMENTS_NOT_FINALIZED. */
  public static final String BILL_EQUIPMENTS_NOT_FINALIZED = "SELECT ep.* "
      + " FROM equipment_prescribed ep" + "   JOIN bill b ON (b.visit_id = ep.patient_id)"
      + "   JOIN bill_charge bc ON (bc.bill_no = b.bill_no)"
      + "   JOIN bill_activity_charge bac ON "
      + "     (bac.charge_id = bc.charge_id AND  bac.activity_id = ep.prescribed_id::text"
      + "  AND bac.activity_code = 'EQU')"
      + " WHERE ep.patient_id=? AND ep.operation_ref IS NULL AND ep.package_ref IS NULL "
      + "   AND ep.finalization_status = 'N' AND (CASE WHEN ep.cancel_status = 'C' "
      + " THEN 'X' ELSE 'U' END) != 'X' " + "   AND b.bill_no=? ORDER BY ep.date";

  /**
   * Gets the bill equipment details not finalized.
   *
   * @param visitId the visit id
   * @param billNo  the bill no
   * @return the bill equipment details not finalized
   * @throws SQLException the SQL exception
   */
  // although bill_no is all we need, using visitId (which has an index) makes it
  // faster.
  public List<BasicDynaBean> getBillEquipmentDetailsNotFinalized(String visitId, String billNo)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> lis = new ArrayList<BasicDynaBean>();
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(BILL_EQUIPMENTS_NOT_FINALIZED);
      ps.setString(1, visitId);
      ps.setString(2, billNo);
      lis = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return lis;
  }

  /**
   * Checks if is bill equipment details finalized.
   *
   * @param visitId the visit id
   * @param billNo  the bill no
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String isBillEquipmentDetailsFinalized(String visitId, String billNo)
      throws SQLException {
    List lis = DataBaseUtil.queryToDynaList(BILL_EQUIPMENTS_NOT_FINALIZED,
        new Object[] { visitId, billNo });
    if (lis != null && !lis.isEmpty()) {
      return "Not Finalized";
    } else {
      return "Finalized";
    }
  }

  /**
   * Checks if is bill equipment details finalized.
   *
   * @param con     Connection
   * @param visitId the visit id
   * @param billNo  the bill no
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String isBillEquipmentDetailsFinalized(Connection con, String visitId,
      String billNo) throws SQLException {
    List lis = DataBaseUtil.queryToDynaList(con, BILL_EQUIPMENTS_NOT_FINALIZED,
        new Object[] { visitId, billNo });
    if (lis != null && !lis.isEmpty()) {
      return "Not Finalized";
    } else {
      return "Finalized";
    }
  }

  /**
   * Checks if is bill bed details finalized.
   *
   * @param visitId the visit id
   * @param billNo  the bill no
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String isBillBedDetailsFinalized(String visitId, String billNo)
      throws SQLException {
    List lis = DataBaseUtil.queryToDynaList(BILL_BEDS_NOT_FINALIZED,
        new Object[] { visitId, billNo });
    if (lis != null && !lis.isEmpty()) {
      return "Not Finalized";
    } else {
      return "Finalized";
    }
  }

  /**
   * Is Bill bed details finalized.
   * @param con database connection
   * @param visitId visit ID
   * @param billNo bill number
   * @return Status as string
   * @throws SQLException the SQL exception
   */
  public static String isBillBedDetailsFinalized(Connection con, String visitId, String billNo)
      throws SQLException {
    List lis = DataBaseUtil.queryToDynaList(con, BILL_BEDS_NOT_FINALIZED,
        new Object[] { visitId, billNo });
    if (lis != null && !lis.isEmpty()) {
      return "Not Finalized";
    } else {
      return "Finalized";
    }
  }

  /**
   * Gets the equipment details not finalized.
   *
   * @param visitId the visit id
   * @return the equipment details not finalized
   * @throws SQLException the SQL exception
   */
  public static String getEquipmentDetailsNotFinalized(String visitId) throws SQLException {
    OrderDAO orDao = new OrderDAO();

    List<BasicDynaBean> equipList = orDao.getFilteredOrders(visitId, "Equipment");

    List<BasicDynaBean> equipOrders = new ArrayList<BasicDynaBean>();
    if (!equipList.isEmpty()) {
      for (BasicDynaBean equip : equipList) {
        if (equip.get("status") != null && !equip.get("status").equals("X")
            && equip.get("finalization_status") != null
            && equip.get("finalization_status").equals("N")) {
          equipOrders.add(equip);
        }
      }
    }
    if (!equipOrders.isEmpty()) {
      return "Not Finalized";
    } else {
      return "Finalized";
    }
  }

  /** The Constant UPDATE_PACKAGE_VISIT_ID. */
  /*
   * Update the visit ID of a package prescribed, restricted to the bill number which is being moved
   * from one visit to another. Used in OP IP conversion.
   */
  public static final String UPDATE_PACKAGE_VISIT_ID = "UPDATE package_prescribed p"
      + " SET patient_id=? "
      + "FROM bill_activity_charge bac, bill_charge bc "
      + "WHERE (bac.activity_id = p.prescription_id::text AND activity_code = 'PKG') "
      + "  AND (bac.charge_id = bc.charge_id) " + "  AND bill_no=?";

  /**
   * Update package visit id.
   *
   * @param con      the con
   * @param billNo   the bill no
   * @param newVisit the new visit
   * @throws SQLException the SQL exception
   */
  public static void updatePackageVisitId(Connection con, String billNo, String newVisit)
      throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(UPDATE_PACKAGE_VISIT_ID)) {
      ps.setString(1, newVisit);
      ps.setString(2, billNo);
      ps.executeUpdate();
    }
  }

  /** The Constant UPDATE_OTHER_SERVICES_VISIT_ID. */
  /*
   * Update the visit ID of other services prescribed, restricted to the bill number which is being
   * moved from one visit to another. Used in OP IP conversion.
   */
  public static final String UPDATE_OTHER_SERVICES_VISIT_ID = "UPDATE other_services_prescribed o"
      + " SET patient_id=? "
      + "FROM bill_activity_charge bac, bill_charge bc "
      + "WHERE (bac.activity_id = o.prescribed_id::text AND activity_code = 'OTC') "
      + "  AND (bac.charge_id = bc.charge_id) " + "  AND bill_no=?";

  /**
   * Update other services visit id.
   *
   * @param con      the con
   * @param billNo   the bill no
   * @param newVisit the new visit
   * @throws SQLException the SQL exception
   */
  public static void updateOtherServicesVisitId(Connection con, String billNo, String newVisit)
      throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(UPDATE_OTHER_SERVICES_VISIT_ID)) {
      ps.setString(1, newVisit);
      ps.setString(2, billNo);
      ps.executeUpdate();
    }
  }

  /** The Constant UPDATE_EQP_VISIT_ID. */
  /*
   * Update the visit ID of equipment prescribed, restricted to the bill number which is being moved
   * from one visit to another. Used in OP IP conversion.
   */
  public static final String UPDATE_EQP_VISIT_ID = "UPDATE equipment_prescribed eq"
      + " SET patient_id=? "
      + "FROM bill_activity_charge bac, bill_charge bc "
      + "WHERE (bac.activity_id = eq.prescribed_id::text AND activity_code = 'EQU') "
      + "  AND (bac.charge_id = bc.charge_id) " + "  AND bill_no=?";

  /**
   * Update equipment visit id.
   *
   * @param con      the con
   * @param billNo   the bill no
   * @param newVisit the new visit
   * @throws SQLException the SQL exception
   */
  public static void updateEquipmentVisitId(Connection con, String billNo, String newVisit)
      throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(UPDATE_EQP_VISIT_ID)) {
      ps.setString(1, newVisit);
      ps.setString(2, billNo);
      ps.executeUpdate();
    }
  }

  /** The Constant UPDATE_OP_VISIT_ID. */
  public static final String UPDATE_OP_VISIT_ID = "UPDATE bed_operation_schedule bos"
      + " SET patient_id=? "
      + "FROM bill_activity_charge bac, bill_charge bc "
      + "WHERE (bac.activity_id = bos.prescribed_id::text AND activity_code = 'OPE') "
      + "  AND (bac.charge_id = bc.charge_id) " + "  AND bill_no=?";

  /**
   * Update operation visit id.
   *
   * @param con      the con
   * @param billNo   the bill no
   * @param newVisit the new visit
   * @throws SQLException the SQL exception
   */
  public static void updateOperationVisitId(Connection con, String billNo, String newVisit)
      throws SQLException {
    DataBaseUtil.executeQuery(con, UPDATE_OP_VISIT_ID, newVisit, billNo);
  }

  /** The Constant CONSULTATION_CHARGES. */
  public static final String CONSULTATION_CHARGES = " SELECT cc.*, cod.item_code, cod.code_type "
      + " FROM consultation_charges cc " + "  JOIN consultation_org_details cod "
      + " ON (cod.consultation_type_id = cc.consultation_type_id "
      + "    AND cod.org_id = cc.org_id) "
      + " WHERE cc.consultation_type_id = ? AND cc.bed_type = ? AND cc.org_id = ? ";

  /**
   * Gets the consultation charges.
   *
   * @param consultationId the consultation id
   * @param bedType        the bed type
   * @param ratePlan       the rate plan
   * @return the consultation charges
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getConsultationCharges(int consultationId, String bedType, String ratePlan)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(CONSULTATION_CHARGES);
      ps.setInt(1, consultationId);
      ps.setString(2, bedType);
      ps.setString(3, ratePlan);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant DOC_CONSULTATION_TYPES. */
  public static final String DOC_CONSULTATION_TYPES = " SELECT * FROM consultation_types ";

  /**
   * Gets the consultation types.
   *
   * @param visitType     the visit type
   * @param includeOtDocs the include ot docs
   * @return the consultation types
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getConsultationTypes(String visitType, boolean includeOtDocs)
      throws SQLException { // not used in Registration anymore
    Connection con = null; // can be removed if not used anywhere else
    PreparedStatement ps = null;
    StringBuilder whereCondition = new StringBuilder();
    try {
      con = DataBaseUtil.getConnection();
      if (visitType.equals("i") && includeOtDocs) {
        whereCondition.append(" WHERE patient_type IN('i','ot') ");
      } else if (visitType.equals("i")) {
        whereCondition.append(" WHERE patient_type IN('i') ");
      } else {
        whereCondition.append(" WHERE patient_type IN ('o') ");
      }
      whereCondition.append(" AND status = 'A' ");
      if (visitType.equals("A")) {
        ps = con.prepareStatement(DOC_CONSULTATION_TYPES_OP + " WHERE status = 'A' ");
      } else {
        ps = con.prepareStatement(DOC_CONSULTATION_TYPES_OP + whereCondition.toString());
      }
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the consultation types.
   *
   * @param con     database connection
   * @param visitType     the visit type
   * @param includeOtDocs the include ot docs
   * @return the consultation types
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getConsultationTypes(Connection con, String visitType,
      boolean includeOtDocs) throws SQLException { // can be removed if not used anywhere else
    PreparedStatement ps = null;
    StringBuilder whereCondition = new StringBuilder();
    try {
      if (visitType.equals("i") && includeOtDocs) {
        whereCondition.append(" WHERE patient_type IN('i','ot') ");
      } else if (visitType.equals("i")) {
        whereCondition.append(" WHERE patient_type IN('i') ");
      } else {
        whereCondition.append(" WHERE patient_type IN ('o') ");
      }
      whereCondition.append(" AND status = 'A' ");
      if (visitType.equals("A")) {
        ps = con.prepareStatement(DOC_CONSULTATION_TYPES_OP + " WHERE status = 'A' ");
      } else {
        ps = con.prepareStatement(DOC_CONSULTATION_TYPES_OP + whereCondition.toString());
      }
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }

  }

  /** The Constant DOC_CONSULTATION_TYPES_OP. */
  public static final String DOC_CONSULTATION_TYPES_OP = " SELECT visit_consultation_type,"
      + "consultation_type,status,consultation_type_id,"
      + " patient_type FROM consultation_types ";

  /**
   * Gets the consultation types op.
   *
   * @param visitType     the visit type
   * @param includeOtDocs the include ot docs
   * @return the consultation types op
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getConsultationTypesOp(String visitType, boolean includeOtDocs)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    StringBuilder whereCondition = new StringBuilder();
    try {
      con = DataBaseUtil.getConnection();
      if (visitType.equals("i") && includeOtDocs) {
        whereCondition.append(" WHERE patient_type IN('i','ot') ");
      } else if (visitType.equals("i")) {
        whereCondition.append(" WHERE patient_type IN('i') ");
      } else {
        whereCondition.append(" WHERE patient_type IN ('o') ");
      }
      whereCondition.append(" AND status = 'A' ");
      if (visitType.equals("A")) {
        ps = con.prepareStatement(DOC_CONSULTATION_TYPES_OP + " WHERE status = 'A' ");
      } else {
        ps = con.prepareStatement(DOC_CONSULTATION_TYPES_OP + whereCondition.toString());
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /**
   * The Class SystemConsultations.
   */
  private class SystemConsultations {

    /** The Constant OP_CONSULTATION. */
    static final int OP_CONSULTATION = -1;

    /** The Constant OP_REVISIT_CONSULTATION. */
    static final int OP_REVISIT_CONSULTATION = -2;

    /** The Constant IP_DOCTOR_VISIT. */
    static final int IP_DOCTOR_VISIT = -3;

    /** The Constant FOLLOW_UP_CONSULTATION. */
    static final int FOLLOW_UP_CONSULTATION = -4;
  }

  /** The Constant ENCOUNTER_CONSULTATIONS. */
  private static final String ENCOUNTER_CONSULTATIONS = " SELECT * FROM doctor_consultation  "
      + "  JOIN patient_registration pr using(patient_id)"
      + "  JOIN consultation_types on(consultation_type_id::text = head) "
      + " WHERE visit_consultation_type NOT IN (?,?) AND pr.main_visit_id=? "
      + "  AND cancel_status IS NULL ";

  /**
   * Consultations encounter wise.
   *
   * @param patientId the patient id
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> consultationsEncounterWise(String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(ENCOUNTER_CONSULTATIONS);
      ps.setInt(1, SystemConsultations.OP_REVISIT_CONSULTATION);
      ps.setInt(2, SystemConsultations.FOLLOW_UP_CONSULTATION);
      ps.setString(3, patientId);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant FOR_PATIENT_SERVICE. */
  private static final String FOR_PATIENT_SERVICE = "UPDATE patient_prescription"
      + " SET status = 'P', username=? WHERE patient_presc_id = ?";

  /** The Constant FOR_PATIENT_TESTS. */
  private static final String FOR_PATIENT_TESTS = "UPDATE patient_prescription"
      + " SET status = 'P', username=? WHERE patient_presc_id = ?";

  /** The Constant FOR_PATIENT_CONSULTATION. */
  private static final String FOR_PATIENT_CONSULTATION = "UPDATE patient_prescription"
      + " SET status = 'P', username=? WHERE patient_presc_id = ?";

  /** The Constant FOR_SERVICE. */
  private static final String FOR_SERVICE = "UPDATE services_prescribed"
      + " SET doc_presc_id = null WHERE prescription_id = ?";

  /** The Constant FOR_TESTS. */
  private static final String FOR_TESTS = "UPDATE tests_prescribed"
      + " SET doc_presc_id = null WHERE prescribed_id = ?";

  /** The Constant FOR_CONSULTATION. */
  private static final String FOR_CONSULTATION = "UPDATE doctor_consultation"
      + " SET doc_presc_id = null WHERE consultation_id = ?";

  /**
   * Update cancel status to patient.
   *
   * @param con  the con
   * @param type the type
   * @param id   the id
   * @throws SQLException the SQL exception
   */
  public static void updateCancelStatusToPatient(Connection con, String type, int id)
      throws SQLException {

    if (type.equalsIgnoreCase("DIA")) {
      updatePatientTests(con, id);
    } else if (type.equalsIgnoreCase("SER")) {
      updatePatientService(con, id);
    } else if (type.equalsIgnoreCase("DOC")) {
      updatePatientConsultation(con, id);
    }

  }

  /**
   * Update patient consultation.
   *
   * @param con the con
   * @param id  the id
   * @throws SQLException the SQL exception
   */
  private static void updatePatientConsultation(Connection con, int id) throws SQLException {
    BasicDynaBean bean = new GenericDAO("doctor_consultation").findByKey(con, "consultation_id",
        id);
    if (bean != null && bean.get("doc_presc_id") != null
        && !bean.get("doc_presc_id").toString().equals("")) {
      try (PreparedStatement pstmt = con.prepareStatement(FOR_PATIENT_CONSULTATION)) {
        pstmt.setString(1, RequestContext.getUserName());
        pstmt.setInt(2, (Integer) bean.get("doc_presc_id"));
        pstmt.executeUpdate();
      }
      try (PreparedStatement pstmt1 = con.prepareStatement(FOR_CONSULTATION)) {
        pstmt1.setInt(1, id);
        pstmt1.executeUpdate();
      }
    }
  }

  /**
   * Update patient service.
   *
   * @param con the con
   * @param id  the id
   * @throws SQLException the SQL exception
   */
  private static void updatePatientService(Connection con, int id) throws SQLException {
    BasicDynaBean bean = new GenericDAO("services_prescribed").findByKey(con, "prescription_id",
        id);
    if (bean != null && bean.get("doc_presc_id") != null
        && !bean.get("doc_presc_id").toString().equals("")) {
      try (PreparedStatement pstmt = con.prepareStatement(FOR_PATIENT_SERVICE)) {
        pstmt.setString(1, RequestContext.getUserName());
        pstmt.setInt(2, (Integer) bean.get("doc_presc_id"));
        pstmt.executeUpdate();
      }
      try (PreparedStatement pstmt1 = con.prepareStatement(FOR_SERVICE)) {
        pstmt1.setInt(1, id);
        pstmt1.executeUpdate();
      }
    }
  }

  /**
   * Update patient tests.
   *
   * @param con the con
   * @param id  the id
   * @throws SQLException the SQL exception
   */
  private static void updatePatientTests(Connection con, int id) throws SQLException {
    BasicDynaBean bean = new GenericDAO("tests_prescribed").findByKey(con, "prescribed_id", id);
    if (bean != null && bean.get("doc_presc_id") != null
        && !bean.get("doc_presc_id").toString().equals("")) {
      try (PreparedStatement pstmt = con.prepareStatement(FOR_PATIENT_TESTS)) {
        pstmt.setString(1, RequestContext.getUserName());
        pstmt.setInt(2, (Integer) bean.get("doc_presc_id"));
        pstmt.executeUpdate();
      }
      try (PreparedStatement pstmt1 = con.prepareStatement(FOR_TESTS)) {
        pstmt1.setInt(1, id);
        pstmt1.executeUpdate();
      }
    }
  }

  /** The Constant GET_PREAUTHNO_AND_MODEIDS. */
  private static final String GET_PREAUTHNO_AND_MODEIDS = "SELECT b.visit_id,b.bill_no,"
      + " bc.charge_id,"
      + " pbc.prior_auth_id as pri_prior_auth_id, sbc.prior_auth_id as sec_prior_auth_id,"
      + " pbc.prior_auth_mode_id as pri_prior_auth_mode_id,"
      + " sbc.prior_auth_mode_id as sec_prior_auth_mode_id " + " FROM bill_charge bc "
      + " JOIN bill b ON(bc.bill_no = b.bill_no) "
      + " JOIN (SELECT bclm.bill_no,prior_auth_id,prior_auth_mode_id,charge_id "
      + " FROM bill_claim bclm " + " JOIN bill_charge_claim bcc "
      + " ON (bcc.claim_id = bclm.claim_id) where bclm.priority=1) AS pbc "
      + "  ON(b.bill_no = pbc.bill_no and bc.charge_id = pbc.charge_id) "
      + " JOIN (SELECT bclm.bill_no,prior_auth_id,prior_auth_mode_id,charge_id "
      + " FROM bill_claim bclm " + " JOIN bill_charge_claim bcc ON (bcc.claim_id = bclm.claim_id) "
      + " where bclm.priority=2) AS sbc "
      + " ON(b.bill_no = sbc.bill_no and bc.charge_id = sbc.charge_id) " + " WHERE b.visit_id = ? ";

  /**
   * Gets the pre auth no and mode ids list.
   *
   * @param visitId the visit id
   * @return the pre auth no and mode ids list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getPreAuthNoAndModeIdsList(String visitId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PREAUTHNO_AND_MODEIDS);
      ps.setString(1, visitId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_OPER_ANAESTHESIA_DETAILS. */
  private static final String GET_OPER_ANAESTHESIA_DETAILS = "SELECT bc.charge_id,sud.*,"
      + " atm.* FROM surgery_anesthesia_details sud "
      + " JOIN bill_charge bc USING (surgery_anesthesia_details_id)"
      + " JOIN anesthesia_type_master atm ON(atm.anesthesia_type_id=sud.anesthesia_type)"
      + " WHERE prescribed_id = ? order by sud.surgery_anesthesia_details_id";

  /**
   * Gets the oper anaesthesia details.
   *
   * @param prescribedId the prescribed id
   * @return the oper anaesthesia details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getOperAnaesthesiaDetails(Integer prescribedId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_OPER_ANAESTHESIA_DETAILS);
      ps.setInt(1, prescribedId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_ADVANCED_OPER_ANAESTHESIA_DETAILS. */
  private static final String GET_ADVANCED_OPER_ANAESTHESIA_DETAILS = "SELECT *"
      + " FROM surgery_anesthesia_details sud "
      + " JOIN operation_procedures op ON(op.prescribed_id=sud.prescribed_id "
      + " AND op.oper_priority = 'P') " + " JOIN operation_anaesthesia_details oad "
      + " ON(op.operation_details_id=oad.operation_details_id)"
      + " WHERE op.prescribed_id = ? order by sud.surgery_anesthesia_details_id";

  /**
   * Gets the advance oper anaesthesia details.
   *
   * @param prescribedId the prescribed id
   * @return the advance oper anaesthesia details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAdvanceOperAnaesthesiaDetails(Integer prescribedId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ADVANCED_OPER_ANAESTHESIA_DETAILS);
      ps.setInt(1, prescribedId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_PRESC_DOCTORS_FOR_ORDERED_ITEMS. */
  private static final String GET_PRESC_DOCTORS_FOR_ORDERED_ITEMS = "SELECT prescribing_dr_id "
      + "FROM bill_charge bc " + "JOIN bill b USING(bill_no)" + "WHERE b.visit_id = ?";

  /**
   * Gets the presc doctor list for ordered items.
   *
   * @param visitID the visit ID
   * @return the presc doctor list for ordered items
   * @throws SQLException the SQL exception
   */
  public static List<Map> getPrescDoctorListForOrderedItems(String visitID) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_PRESC_DOCTORS_FOR_ORDERED_ITEMS);
      pstmt.setString(1, visitID);
      return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(pstmt));
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /**
   * Gets the pending operations.
   *
   * @param patientId the patient id
   * @return the pending operations
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getPendingOperations(String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      String getPendingOperation = " SELECT bos.*, op.*" + " FROM bed_operation_schedule bos"
          + " JOIN patient_details pd ON (pd.mr_no = bos.mr_no)"
          + " JOIN operation_procedures op ON bos.prescribed_id = op.prescribed_id"
          + " WHERE bos.patient_id = ? AND bos.status = ? AND op.oper_priority = ?"
          + " AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )";
      StringBuilder query = new StringBuilder(getPendingOperation);
      ps = con.prepareStatement(query.toString());
      ps.setString(1, patientId);
      ps.setString(2, "N");
      ps.setString(3, "P");
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the pending orders.
   *
   * @param patientId the patient id
   * @return the pending orders
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getPendingOrders(String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      String getPendingOrders = " SELECT *" + " FROM patient_orders_view"
          + " WHERE patient_id=? AND (( type IN ('Laboratory', 'Radiology') "
          + " AND status IN ('N', 'R', 'RP', 'NRN'))"
          + " OR ( type = 'Service' AND status IN ('N', 'P')))";
      StringBuilder query = new StringBuilder(getPendingOrders);
      ps = con.prepareStatement(query.toString());
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
