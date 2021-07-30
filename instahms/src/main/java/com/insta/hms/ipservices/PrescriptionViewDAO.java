package com.insta.hms.ipservices;

import com.bob.hms.common.DataBaseUtil;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class PrescriptionViewDAO.
 */
public class PrescriptionViewDAO {

  /**
   * Gets the services prescribed.
   *
   * @param mrno
   *          the mrno
   * @param con
   *          the con
   * @return the services prescribed
   * @throws SQLException
   *           the SQL exception
   */
  public ArrayList getServicesPrescribed(String mrno, Connection con) throws SQLException {
    PreparedStatement ps = null;
    try {
      String servicesQuery = "select "
          + " s.dept_name,s.service_name,sp.quantity,sp.comments "
          + " from services s,SERVICES_PRESCRIBED sp "
          + " where s.service_id = sp.service_id and sp.mr_no = ?";
      ps = con.prepareStatement(servicesQuery);
      ps.setString(1, mrno);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /**
   * Gets the tests prescribed.
   *
   * @param mrno
   *          the mrno
   * @param con
   *          the con
   * @return the tests prescribed
   * @throws SQLException
   *           the SQL exception
   */
  public ArrayList getTestsPrescribed(String mrno, Connection con) throws SQLException {
    PreparedStatement ps = null;
    try {
      String testsPrescribedQuery = "select "
          + " dd.ddept_name,d.test_name,tp.remarks "
          + " from diagnostics_departments dd,tests_prescribed tp left outer"
          + " join diagnostics d on d.test_id=tp.test_id" + " where  tp.mr_no = ?";
      ps = con.prepareStatement(testsPrescribedQuery);
      ps.setString(1, mrno);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /**
   * Gets the bed equipment charges.
   *
   * @param mrno
   *          the mrno
   * @param con
   *          the con
   * @return the bed equipment charges
   * @throws SQLException
   *           the SQL exception
   */
  public ArrayList getBedEquipmentCharges(String mrno, Connection con) throws SQLException {
    PreparedStatement ps = null;
    try {
      String bedEquipmentQuery = "select dpt.dept_name,em.equipment_name,pbe.duration,pbe.remarks "
          + "from equipment_master em,patient_bed_eqipmentcharges pbe,department dpt "
          + "where pbe.eqip_id = em.eq_id and pbe.dept_id=dpt.dept_id and pbe.mr_no = ?";
      ps = con.prepareStatement(bedEquipmentQuery);
      ps.setString(1, mrno);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /**
   * Gets the doctor consultation charges.
   *
   * @param mrno
   *          the mrno
   * @param con
   *          the con
   * @return the doctor consultation charges
   * @throws SQLException
   *           the SQL exception
   */
  public ArrayList getDoctorConsultationCharges(String mrno, Connection con) throws SQLException {
    PreparedStatement ps = null;
    try {
      String docConsultQuery = "select "
          + " d.doctor_name,to_char(dc.visited_date,'DD-MM-YYYY') as visited_date,"
          + " to_char(dc.visited_date,'HH:MI:SS AM') as visited_time,dc.remarks "
          + " from doctors d,doctor_consultation dc "
          + " where d.doctor_id = dc.doctor_name and dc.mr_no = ?";
      ps = con.prepareStatement(docConsultQuery);
      ps.setString(1, mrno);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /**
   * Gets the other services prescribed.
   *
   * @param mrno
   *          the mrno
   * @param con
   *          the con
   * @return the other services prescribed
   * @throws SQLException
   *           the SQL exception
   */
  public ArrayList getOtherServicesPrescribed(String mrno, Connection con) throws SQLException {
    PreparedStatement ps = null;
    try {
      String otherServicesQuery = "SELECT "
          + " (case when service_group='OCOTC' then 'Other charge' "
          + " when service_group='CONOTC' then 'Consumables'"
          + " when service_group='MISOTC' then 'Miscellaneous' end) as service_group, "
          + " service_name,quantity,remarks"
          + " FROM other_services_prescribed where mr_no= ?";
      ps = con.prepareStatement(otherServicesQuery);
      ps.setString(1, mrno);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /**
   * Gets the medicines prescribed.
   *
   * @param mrno
   *          the mrno
   * @param con
   *          the con
   * @return the medicines prescribed
   * @throws SQLException
   *           the SQL exception
   */
  public ArrayList getMedicinesPrescribed(String mrno, Connection con) throws SQLException {
    PreparedStatement ps = null;
    try {
      String medicinesPrescibedQuery = " SELECT "
          + " d.doctor_name,medicine_name, dosage, prescribed_qty,"
          + " no_days, prescribed_date,to_char(prescribed_time,'HH:MI:SS AM') "
          + " as prescribed_time,remarks "
          + " FROM doctor_prescription dp,"
          + " doctors d where dp.doctor_id=d.doctor_id and dp.mr_no=?";

      ps = con.prepareStatement(medicinesPrescibedQuery);
      ps.setString(1, mrno);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /**
   * Gets the opetaions.
   *
   * @param mrno
   *          the mrno
   * @param patientId
   *          the patient id
   * @return the opetaions
   * @throws SQLException
   *           the SQL exception
   */
  public ArrayList getOpetaions(String mrno, String patientId) throws SQLException {
    ArrayList list = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      String prescibedQueryOp = " select  (DOC.DOCTOR_NAME) AS primarysurgeon,"
          + " (DOCTOR.DOCTOR_NAME) AS PRIMARYANAESTHETIST,tm.theatre_name as theatre ,"
          + " bps.operation_name as opid,to_char(operation_time,'hh24:mi') as starttime,"
          + " to_char(expected_end_time,'hh24:mi') as endtime,*,"
          + " to_char(bps.start_date,'DD-MM-YYYY') as operation_date,om.operation_name as name,"
          + " bps.prescribed_id as id,bps.status,"
          + " (case when bps.status ='X' then 'checked' else '' end) as check,"
          + " (case when bps.status ='X' then 'disabled' when bps.status ='C' "
          + " then 'disabled'  end) as dis  from "
          + " operation_master om ,BED_OPERATION_SCHEDULE bps "
          + " left join  theatre_master tm on theatre_id= bps.theatre_name  "
          + " left outer join DOCTORS DOC on DOC.DOCTOR_ID=BPS.SURGEON "
          + " left outer join DOCTORS DOCTOR on DOCTOR.DOCTOR_ID=BPS.anaesthetist  "
          + " where  om.op_id = bps.operation_name "
          + " and  mr_no = ? and patient_id = ? order by bps.operation_name";
      ps = con.prepareStatement(prescibedQueryOp);
      ps.setString(1, mrno);
      ps.setString(2, patientId);
      list = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /**
   * Gets the opetaions print.
   *
   * @param mrno
   *          the mrno
   * @param patientId
   *          the patient id
   * @param orderId
   *          the order id
   * @return the opetaions print
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getOpetaionsPrint(String mrno, String patientId, String orderId)
      throws SQLException {
    List list = null;
    Connection con = null;
    PreparedStatement ps = null;
    int idx = 1;
    try {
      con = DataBaseUtil.getConnection();
      String prescibedQueryOp = " SELECT (DOC.DOCTOR_NAME) AS primarysurgeon, "
          + " (DOCTOR.DOCTOR_NAME) AS PRIMARYANAESTHETIST,"
          + " tm.theatre_name as theatre ,bps.operation_name as opid,"
          + " TO_CHAR(start_datetime,'hh24:mi') AS starttime,"
          + " to_char(end_datetime,'hh24:mi') AS endtime,*,"
          + " TO_CHAR(bps.start_datetime,'DD-MM-YYYY') AS operation_date, "
          + " om.operation_name as name,bps.prescribed_id as id,bps.status as prescribed_status,"
          + " (case when bps.status ='X' then 'checked' else '' end) as check,"
          + " (case when bps.status ='X' then 'disabled' end) as dis  from "
          + " operation_master om , BED_OPERATION_SCHEDULE bps  "
          + " left join  theatre_master tm on theatre_id= bps.theatre_name  "
          + " left outer join DOCTORS DOC on DOC.DOCTOR_ID=BPS.SURGEON "
          + " left outer join DOCTORS DOCTOR on DOCTOR.DOCTOR_ID=BPS.anaesthetist  "
          + " where  om.op_id = bps.operation_name "
          + " and  mr_no = ? and patient_id = ?  and bps.status != 'X' and package_ref is null ";
      if (orderId != null) {
        prescibedQueryOp = prescibedQueryOp + " and common_order_id = ?";
      }
      ps = con.prepareStatement(prescibedQueryOp);
      ps.setString(idx++, mrno);
      ps.setString(idx++, patientId);
      if (orderId != null) {
        ps.setInt(idx++, Integer.parseInt(orderId));
      }
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /** The prescibed query op. */
  private String prescibedQueryOp = "SELECT * from operation_prescriptions_view";

  /**
   * Gets the opetaion prescriptions print.
   *
   * @param orderId
   *          the order id
   * @return the opetaion prescriptions print
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getOpetaionPrescriptionsPrint(int orderId) throws SQLException {
    List list = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(prescibedQueryOp + " WHERE  Common_order_id = ?");
      ps.setInt(1, orderId);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /**
   * Gets the opetaions completed.
   *
   * @param patientId
   *          the patient id
   * @return the opetaions completed
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getOpetaionsCompleted(String patientId) throws SQLException {
    List list = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      String prescibedQueryOp = " SELECT " + " doc.doctor_name AS primarysurgeon, "
          + " doctor.doctor_name AS primaryanaesthetist, "
          + " tm.theatre_name as theatre, bps.operation_name as opid, "
          + " to_char(start_datetime,'hh24:mi') as starttime, "
          + " to_char(end_datetime,'hh24:mi') as endtime, "
          + " to_char(bps.start_datetime,'DD-MM-YYYY') as operation_date, "
          + " to_char(bps.end_datetime,'DD-MM-YYYY') as operation_end_date, "
          + " om.operation_name as name, "
          + " bps.prescribed_id, bps.status, od.doc_id, bps.remarks, "
          + " (CASE WHEN dat.doc_format IS NULL THEN 'doc_fileupload' "
          + " ELSE dat.doc_format END) AS doc_format, "
          + " (case when bps.status = 'X' then 'checked' else '' end) as check, "
          + " (case when bps.status = 'X' then 'disabled' "
          + " when bps.status ='C' then 'disabled' end) as dis "
          + " FROM operation_master om, bed_operation_schedule bps "
          + " LEFT JOIN  theatre_master tm ON theatre_id= bps.theatre_name "
          + " LEFT OUTER JOIN doctors doc ON doc.doctor_id=bps.surgeon "
          + " LEFT OUTER JOIN doctors doctor ON doctor.doctor_id=bps.anaesthetist "
          + " LEFT OUTER JOIN operation_documents od ON (bps.prescribed_id=od.prescription_id) "
          + " LEFT OUTER JOIN patient_documents pd ON (od.doc_id=pd.doc_id) "
          + " LEFT OUTER JOIN doc_all_templates_view dat "
          + " ON (pd.template_id=dat.template_id AND pd.doc_format=dat.doc_format) "
          + " WHERE  om.op_id = bps.operation_name AND bps.status ='C' " + " AND patient_id = ? "
          + " ORDER BY bps.operation_name";
      ps = con.prepareStatement(prescibedQueryOp);
      ps.setString(1, patientId);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /**
   * Gets the opetaion prescriptions completed.
   *
   * @param patientId
   *          the patient id
   * @return the opetaion prescriptions completed
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getOpetaionPrescriptionsCompleted(String patientId)
      throws SQLException {
    List list = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement("SELECT * " + " FROM operation_prescriptions_view "
          + " WHERE patient_id = ? " + " AND (status != 'C' OR status IS NULL) "
          + " ORDER BY mr_no,prescribed_time");
      ps.setString(1, patientId);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

}
