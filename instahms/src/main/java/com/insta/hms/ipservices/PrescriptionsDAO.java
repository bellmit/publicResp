package com.insta.hms.ipservices;

import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class PrescriptionsDAO.
 */
public class PrescriptionsDAO {
  
  /**
   * Saves tests prescribed for an In patient.
   *
   * @param con the con
   * @param testDto the test dto
   * @param prescriptionid the prescriptionid
   * @param commonOrderId the common order id
   * @param conductedStatus the conducted status
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean saveTests(Connection con, PrescribedTestDTO testDto, int prescriptionid,
      int commonOrderId, String conductedStatus) throws SQLException {
    try (PreparedStatement ps = con
        .prepareStatement("INSERT INTO "
            + " tests_prescribed(mr_no, pat_id,test_id,  "
            + " pres_date,pres_time, pres_doctor, conducted,  "
            + " priority, sflag, prescribed_id, user_name,remarks,labno,"
            + " prescription_type,common_order_id)VALUES "
            + " (?, ?, ?,   CURRENT_DATE,?, ? , ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

      ps.setString(1, testDto.getMrno());
      ps.setString(2, testDto.getPatientid());
      ps.setString(3, testDto.getTestId());
      ps.setTimestamp(4, testDto.getPresdate());
      ps.setString(5, testDto.getDoctor());
      ps.setString(6, conductedStatus);
      ps.setString(7, "R");
      ps.setString(8, testDto.getIsSampleCollected());
      ps.setInt(9, prescriptionid);
      ps.setString(10, testDto.getUserName());
      ps.setString(11, testDto.getTestremark());
      ps.setString(12, testDto.getLabno());
      ps.setString(13, AddTestDAOImpl.getTestPrescriptionType(testDto.getTestId()));
      ps.setInt(14, commonOrderId);

      return ps.executeUpdate() != 0;
    }

  }

  /**
   * Saves services prescribed for an In Patients.
   *
   * @param con the con
   * @param sdto the sdto
   * @param prescriptionid the prescriptionid
   * @param commonOrderId the common order id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean saveServices(Connection con, PrescribedServicesDTO sdto, int prescriptionid,
      int commonOrderId) throws SQLException {
    try (PreparedStatement ps = con
        .prepareStatement("INSERT INTO "
            + " SERVICES_PRESCRIBED(MR_NO, PATIENT_ID, SERVICE_ID, PRESC_DATE, "
            + " DOCTOR_ID, CONDUCTED, CONDUCTEDDATE,  QUANTITY, PRESCRIPTION_ID, "
            + " USER_NAME,COMMENTS,OPERATION_REF,COMMON_ORDER_ID,SPECIALIZATION) "
            + " VALUES(?,?,?,?,?,?,?,?::numeric,?,?,?,?,?,?)")) {
      ps.setString(1, sdto.getMrno());
      ps.setString(2, sdto.getPatientid());
      ps.setString(3, sdto.getServiceId());
      ps.setTimestamp(4, sdto.getPresdate());
      ps.setString(5, sdto.getDoctor());
      ps.setString(6, sdto.getConducted());
      ps.setTimestamp(7, sdto.getPresdate());
      ps.setString(8, sdto.getNoofdays());
      ps.setInt(9, prescriptionid);
      ps.setString(10, "admin");
      ps.setString(11, sdto.getServiceremark());
      ps.setString(12, sdto.getHoperationid());
      ps.setInt(13, commonOrderId);
      ps.setString(14, sdto.getSpecialization());
      return ps.executeUpdate() != 0;
    }
  }

  /**
   * Save equipments.
   *
   * @param con the con
   * @param edto the edto
   * @param nextPrescribedId the next prescribed id
   * @param commonOrderId the common order id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean saveEquipments(Connection con, EquipmentsDTO edto, int nextPrescribedId,
      int commonOrderId) throws SQLException {
    try (PreparedStatement ps = con
        .prepareStatement("INSERT INTO "
            + " PATIENT_BED_EQIPMENTCHARGES(MR_NO,PATIENT_ID,EQIP_ID,DEPT_ID,"
            + " DURATION,DATE,BED_PRESCRIBED_EQUIP_ID,REMARKS,"
            + " DOCTOR_ID,OPERATION_REF,COMMON_ORDER_ID"
            + " ) VALUES(?,?,?,?,?,?,?,?,?,?,?)")) {
      ps.setString(1, edto.getMrno());
      ps.setString(2, edto.getPatientid());
      ps.setString(3, edto.getEquipmentId());
      ps.setString(4, edto.getEquipmentdepartment());
      ps.setInt(5, edto.getEquipmentduration());
      ps.setTimestamp(6, edto.getPresdate());
      ps.setInt(7, nextPrescribedId);
      ps.setString(8, edto.getEquipmentremark());
      ps.setString(9, edto.getDoctor());
      ps.setString(10, edto.getHoperationid());
      ps.setInt(11, commonOrderId);
      return ps.executeUpdate() > 0;
    }
  }

  /**
   * Save visits.
   *
   * @param con the con
   * @param vdto the vdto
   * @param presriptionid the presriptionid
   * @param commonOrderId the common order id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean saveVisits(Connection con, DoctorVisitDTO vdto, int presriptionid,
      int commonOrderId) throws SQLException {

    try (PreparedStatement ps = con
        .prepareStatement("INSERT INTO "
            + " DOCTOR_CONSULTATION( MR_NO,PATIENT_ID,DOCTOR_NAME,PRESC_DATE,CONSULTATION_ID,"
            + " REMARKS, OT_DOC_ROLE,HEAD,consultation_token,common_order_id, visited_date)"
            + " VALUES(?,?,?,?,?,?,?,?,?,?,?)")) {
      ps.setString(1, vdto.getMrno());
      ps.setString(2, vdto.getPatientid());
      ps.setString(3, vdto.getDoctorname());
      ps.setTimestamp(4, vdto.getVisitingdate());
      ps.setInt(5, presriptionid);
      ps.setString(6, vdto.getVisitremarks());
      ps.setString(7, vdto.getOtdocrole());
      ps.setString(8, vdto.getHead());
      ps.setInt(9, vdto.getConsultationToken());
      ps.setInt(10, commonOrderId);
      ps.setTimestamp(11, vdto.getVisitingdate());
      return ps.executeUpdate() != 0;
    }
  }

  /**
   * Save medicine.
   *
   * @param con the con
   * @param mediDto the medi Dto
   * @param id the id
   * @param commonOrderId the common order id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean saveMedicine(Connection con, MedicineDTO mediDto, int id, int commonOrderId)
      throws SQLException {
    String medId;
    try (PreparedStatement ps = con
        .prepareStatement("SELECT MEDICINE_ID FROM store_item_details WHERE MEDICINE_NAME=?")) {
      ps.setString(1, mediDto.getMedicine());
      medId = DataBaseUtil.getStringValueFromDb(ps);
    }
    // String id = DataBaseUtil.getValue("ip_medicine_sequence", "Y", "IPMEDICINE");
    try (PreparedStatement ps = con
        .prepareStatement("insert into Doctor_Prescription "
            + " (mr_no, patient_id, doctor_id, medicine_id, dosage, "
            + " prescribed_qty,no_days, prescribed_date, prescribed_time, "
            + " medicine_name, med_prescription_id,remarks,operation_ref,common_order_id) "
            + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
      ps.setString(1, mediDto.getMrno());
      ps.setString(2, mediDto.getPatientid());
      ps.setString(3, mediDto.getDoctor());
      ps.setString(4, medId);
      ps.setString(5, mediDto.getMedicinedosage());
      ps.setInt(6, Integer.parseInt(mediDto.getMedquantity()));
      ps.setInt(7, Integer.parseInt(mediDto.getMednoofdays()));
      ps.setTimestamp(8, mediDto.getPresdate());
      ps.setTimestamp(9, mediDto.getPresdate());
      ps.setString(10, mediDto.getMedicine());
      ps.setInt(11, id);
      ps.setString(12, mediDto.getMedremarks());
      ps.setString(13, mediDto.getHoperationid());
      ps.setInt(14, commonOrderId);
      return ps.executeUpdate() != 0;
    }
  }

  /**
   * Save other services.
   *
   * @param con the con
   * @param odto the odto
   * @param presriptionid the presriptionid
   * @param commonOrderId the common order id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean saveOtherServices(Connection con, OtherServicesDTO odto, 
      int presriptionid,
      int commonOrderId) throws SQLException {
    try (PreparedStatement ps = con
        .prepareStatement("INSERT INTO "
            + " other_services_prescribed(mr_no, patient_id, service_group, "
            + " service_name, prescribed_id,quantity,remarks,pres_time, "
            + " doctor_id,operation_ref,common_order_id)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)")) {
      ps.setString(1, odto.getMrno());
      ps.setString(2, odto.getPatientid());
      ps.setString(3, odto.getOtherserviceGroup());
      ps.setString(4, odto.getOtherservice());
      ps.setInt(5, presriptionid);
      ps.setInt(6, Integer.parseInt(odto.getOtherserviceqty()));
      ps.setString(7, odto.getOtherserviceremarks());
      ps.setTimestamp(8, odto.getPresdate());
      ps.setString(9, odto.getDoctor());
      ps.setString(10, odto.getHoperationid());
      ps.setInt(11, commonOrderId);
      return ps.executeUpdate() != 0;
    }
  }

  /**
   * Gets the bill number.
   *
   * @param con the con
   * @param visitid the visitid
   * @return the bill number
   * @throws SQLException the SQL exception
   */
  public String getBillNumber(Connection con, String visitid) throws SQLException {
    try (PreparedStatement ps = con
        .prepareStatement("select * from bill  where visit_id=? and status='A'")) {
      ps.setString(1, visitid);
      return DataBaseUtil.getStringValueFromDb(ps);
    }
  }

  /**
   * Gets the test charge.
   *
   * @param con the con
   * @param bed the bed
   * @param orgid the orgid
   * @param testid the testid
   * @return the test charge
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getTestCharge(Connection con, String bed, 
      String orgid, String testid)
      throws SQLException {
    try (PreparedStatement psTest = con
        .prepareStatement("select charge,item_code "
            + " from diagnostic_charges dc,"
            + " test_org_details tod "
            + " where  dc.org_name=tod.org_id and dc.test_id=tod.test_id "
            + " and tod.applicable and dc.test_id=? "
            + " and bed_type=? and org_name=? and priority='R'")) {
      psTest.setString(1, testid);
      psTest.setString(2, bed);
      psTest.setString(3, orgid);
      List list = null;
      BasicDynaBean chargefromdb = null;
      list = DataBaseUtil.queryToDynaList(psTest);
      if (list != null && list.size() > 0) {
        chargefromdb = (BasicDynaBean) list.get(0);
      }
      return chargefromdb;
    }
  }

  /**
   * Gets the service charge.
   *
   * @param con the con
   * @param bedno the bedno
   * @param orgid the orgid
   * @param serviceid the serviceid
   * @return the service charge
   * @throws SQLException the SQL exception
   */
  public float getServiceCharge(Connection con, String bedno, String orgid, String serviceid)
      throws SQLException {
    try (PreparedStatement ps = con
        .prepareStatement("select bed_type from bed_names where bed_name=?")) {
      ps.setString(1, bedno);
      try (PreparedStatement psSer = con
          .prepareStatement("select unit_charge "
              + " from service_master_charges "
              + " where service_id=? and bed_type=? and org_id=?")) {
        psSer.setString(1, serviceid);
        psSer.setString(2, DataBaseUtil.getStringValueFromDb(ps));
        psSer.setString(3, orgid);
        String charge = DataBaseUtil.getStringValueFromDb(psSer);
        return Float.parseFloat(charge);
      }
    }
  }

  /**
   * Gets the service id.
   *
   * @param con the con
   * @return the service id
   * @throws SQLException the SQL exception
   */
  public int getServiceId(Connection con) throws SQLException {
    PreparedStatement ps = con.prepareStatement("select nextval('service_prescribed')");
    int id = DataBaseUtil.getIntValueFromDb(ps);
    ps.close();
    return id;

  }

  /**
   * Gets the other service charge.
   *
   * @param con the con
   * @param servicegroup the servicegroup
   * @param service the service
   * @param qty the qty
   * @return the other service charge
   * @throws SQLException the SQL exception
   */
  public float getOtherServiceCharge(Connection con, 
      String servicegroup, String service, String qty)
      throws SQLException {
    String query = "";
    if (servicegroup.equalsIgnoreCase("OCOTC")) {

      query = "SELECT CHARGE FROM OTHER_CHARGE_MASTER WHERE CHARGE_NAME=?";
    } else if (servicegroup.equalsIgnoreCase("CONOTC")) {
      query = "SELECT CHARGE FROM CONSUMABLES_MASTER WHERE CHARGE_NAME=?";
    } else {
      query = "SELECT CHARGE FROM IMPLANTS_MASTER WHERE CHARGE_NAME=?";
    }
    try (PreparedStatement ps = con.prepareStatement(query)) {
      ps.setString(1, service);
      int charge = DataBaseUtil.getIntValueFromDb(ps);
      return (charge) * (Float.parseFloat(qty));
    }
  }

  /** The Constant QUERY8TO20. */
  public static final String QUERY8TO20 = "SELECT TO_CHAR('now'::time , 'HH24') AS HOURS";

  /**
   * Gets the IP consultation charge.
   *
   * @param doctorid the doctorid
   * @param bedtype the bedtype
   * @param orgid the orgid
   * @param time the time
   * @return the IP consultation charge
   */
  public float getIPConsultationCharge(String doctorid, String bedtype, String orgid, String time) {
    Connection con = null;
    PreparedStatement ps = null;
    String doctorcharge = "";
    try {
      con = DataBaseUtil.getConnection();

      String hoursStr = DataBaseUtil.getStringValueFromDb(QUERY8TO20);
      int presenthour = Integer.parseInt(hoursStr);
      BasicDynaBean regPrefs = new RegistrationPreferencesDAO().getRecord();
      int am = ((BigDecimal) regPrefs.get("night_am")).intValue();
      int pm = ((BigDecimal) regPrefs.get("night_pm")).intValue();

      String orgquery = "select org_id from organization_details where org_name=?";
      String generalorgid = DataBaseUtil.getStringValueFromDb(orgquery,
          Constants.getConstantValue("ORG"));

      if (presenthour >= am && presenthour < pm) {
        // day charge
        String chargequery = "select doctor_ip_charge from doctor_consultation_charge  "
            + "where doctor_name=? and bed_type=? and organization=?";
        ps = con.prepareStatement(chargequery);

        ps.setString(1, doctorid);
        ps.setString(2, bedtype);
        ps.setString(3, orgid);

        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            doctorcharge = rs.getString(1);
          } else {
            ps.setString(1, doctorid);
            ps.setString(2, Constants.getConstantValue("BEDTYPE"));
            ps.setString(3, generalorgid);
            try (ResultSet rs1 = ps.executeQuery()) {
              if (rs1.next()) {
                doctorcharge = rs1.getString(1);
              }
            }
          }
        }
      } else {
        // night charge
        String chargequery = "select night_ip_charge from doctor_consultation_charge  "
            + "where doctor_name=? and bed_type=? and organization=?";

        ps = con.prepareStatement(chargequery);

        ps.setString(1, doctorid);
        ps.setString(2, bedtype);
        ps.setString(3, orgid);

        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            doctorcharge = rs.getString(1);
          } else {
            ps.setString(1, doctorid);
            ps.setString(2, Constants.getConstantValue("BEDTYPE"));
            ps.setString(3, generalorgid);
            try (ResultSet rs1 = ps.executeQuery()) {
              while (rs1.next()) {
                doctorcharge = rs1.getString(1);
              }
            }
          }
        }
      }

    } catch (Exception exp) {
      Logger.logException("Exception occured in getIPConsultationCharge method", exp);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return Float.parseFloat(doctorcharge);
  }

  /**
   * Gets the medicine charge.
   *
   * @param con the con
   * @param medicineName the medicine name
   * @return the medicine charge
   * @throws SQLException the SQL exception
   */
  public float getMedicineCharge(Connection con, String medicineName) 
      throws SQLException {
    float charge = 0;
    try (PreparedStatement ps = con
        .prepareStatement("SELECT CHARGE "
            + " FROM COMMON_MEDICINE_CHARGE_MASTER WHERE CHARGE_NAME=?")) {
      ps.setString(1, medicineName);
      charge = Float.parseFloat(DataBaseUtil.getStringValueFromDb(ps));
    }
    return charge;
  }

  /**
   * Gets the bed type.
   *
   * @param con the con
   * @param bedname the bedname
   * @return the bed type
   * @throws SQLException the SQL exception
   */
  public String getBedType(Connection con, int bedname) 
      throws SQLException {
    String bedType = null;
    try (PreparedStatement ps = con
        .prepareStatement("select bed_type "
            + " from bed_names where bed_id=?")) {
      ps.setInt(1, bedname);
      bedType = DataBaseUtil.getStringValueFromDb(ps);
    }
    return bedType;

  }

  /**
   * Gets the operation charge.
   *
   * @param con the con
   * @param bed the bed
   * @param orgid the orgid
   * @param opeid the opeid
   * @return the operation charge
   * @throws SQLException the SQL exception
   */
  public float getOperationCharge(Connection con, String bed, 
      String orgid, String opeid)
      throws SQLException {
    try (PreparedStatement ps = con
        .prepareStatement("select "
            + " (CHARGE+SURGEON_CHARGE+ANESTHETIST_CHARGE) "
            + " from operation_charges "
            + " WHERE bed_type=(SELECT distinct "
            + " bed_type from bed_names where bed_name=?) "
            + " AND org_id=? AND op_id=?")) {
      ps.setString(1, bed);
      ps.setString(2, orgid);
      ps.setString(2, opeid);
      String charge = DataBaseUtil.getStringValueFromDb(ps);
      return Float.parseFloat(charge);
    }

  }

  /**
   * Gets the theater charge.
   *
   * @param con the con
   * @param bed the bed
   * @param orgid the orgid
   * @param theaterid the theaterid
   * @return the theater charge
   * @throws SQLException the SQL exception
   */
  public float getTheaterCharge(Connection con, String bed, 
      String orgid, String theaterid)
      throws SQLException {
    try (PreparedStatement ps = con
        .prepareStatement("select daily_charge "
            + " from THEATRE_CHARGES "
            + " WHERE bed_type=? AND org_id=? and theatre_id=?")) {
      ps.setString(1, bed);
      ps.setString(2, orgid);
      ps.setString(3, theaterid);
      String charge = DataBaseUtil.getStringValueFromDb(ps);
      return Float.parseFloat(charge);
    }
  }

  /**
   * Gets the MRD consultation details.
   *
   * @param patId the pat id
   * @return the MRD consultation details
   * @throws SQLException the SQL exception
   */
  public static HashMap getMRDConsultationDetails(String patId) 
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List arrTestDetails = null;
    List arrServicesDetails = null;
    List arrOperationDetails = null;
    HashMap<String, List> arrMRDConsultationDetails = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con
          .prepareStatement(" select diag.test_name,tp.conducted "
              + " from tests_prescribed tp,diagnostics diag "
              + " where  diag.test_id=tp.test_id  and tp.pat_id=?");
      ps.setString(1, patId);
      arrTestDetails = DataBaseUtil.queryToArrayList(ps);
      ps.close();
      ps = con
          .prepareStatement("select s.service_name,conducted "
              + " from services_prescribed sp,services s"
              + " where   s.service_id = sp.service_id and sp.patient_id=?");
      ps.setString(1, patId);
      arrServicesDetails = DataBaseUtil.queryToArrayList(ps);
      ps.close();
      ps = con
          .prepareStatement(" select "
              + " to_char(bos.start_datetime,'HH24:MI') as operation_time , "
              + " to_char(bos.end_datetime,'HH24:MI') as expected_end_time,"
              + " om.operation_name,dc.doctor_name as doctor,ot_doc_role as role "
              + " from chargehead_constants,doctor_consultation  dcc"
              + " left outer join doctors dc  on dc.doctor_id = dcc.doctor_name"
              + " left outer join bed_operation_schedule bos "
              + " on (bos.prescribed_id = dcc.operation_ref::integer "
              + " and bos.finalization_status='N')"
              + " left outer join operation_master om "
              + " on om.op_id = bos.operation_name "
              + " where chargehead_id='SUOPE' and dcc.head = 'SUOPE' "
              + " and bos.patient_id=? order by om.operation_name ");
      ps.setString(1, patId);
      arrOperationDetails = DataBaseUtil.queryToArrayList(ps);
      ps.close();
      arrMRDConsultationDetails = new HashMap<String, List>();

      arrMRDConsultationDetails.put("Tests", arrTestDetails);
      arrMRDConsultationDetails.put("Services", arrServicesDetails);
      arrMRDConsultationDetails.put("Operations", arrOperationDetails);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return arrMRDConsultationDetails;
  }

  /**
   * Edits the medicine.
   *
   * @param con the con
   * @param prescriptionid the prescriptionid
   * @param qty the qty
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean editMedicine(Connection con, int prescriptionid, int qty)
      throws SQLException {
    PreparedStatement ps = null;
    boolean status = false;
    try {
      ps = con
          .prepareStatement("UPDATE DOCTOR_PRESCRIPTION "
              + " SET PRESCRIBED_QTY=? WHERE MED_PRESCRIPTION_ID=?");
      ps.setInt(1, qty);
      ps.setInt(2, prescriptionid);
      if (ps.executeUpdate() > 0) {
        status = true;
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return status;
  }

  /**
   * Edits the services.
   *
   * @param con the con
   * @param prescriptionid the prescriptionid
   * @param qty the qty
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean editServices(Connection con, int prescriptionid, int qty)
      throws SQLException {
    PreparedStatement ps = null;
    boolean status = false;
    try {
      ps = con
          .prepareStatement("UPDATE SERVICES_PRESCRIBED SET QUANTITY=? WHERE PRESCRIPTION_ID=?");
      ps.setInt(1, qty);
      ps.setInt(2, prescriptionid);
      if (ps.executeUpdate() > 0) {
        status = true;
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return status;
  }

  /**
   * Edits the equipment.
   *
   * @param con the con
   * @param prescriptionid the prescriptionid
   * @param qty the qty
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean editEquipment(Connection con, int prescriptionid, int qty)
      throws SQLException {
    PreparedStatement ps = null;
    boolean status = false;
    try {
      ps = con
          .prepareStatement("UPDATE PATIENT_BED_EQIPMENTCHARGES "
              + " SET DURATION=? WHERE BED_PRESCRIBED_EQUIP_ID=?");
      ps.setInt(1, qty);
      ps.setInt(2, prescriptionid);
      if (ps.executeUpdate() > 0) {
        status = true;
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return status;
  }

  /**
   * Edits the otherservices.
   *
   * @param con the con
   * @param prescriptionid the prescriptionid
   * @param qty the qty
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean editOtherservices(Connection con, int prescriptionid, int qty)
      throws SQLException {
    PreparedStatement ps = null;
    boolean status = false;
    try {
      ps = con
          .prepareStatement("UPDATE OTHER_SERVICES_PRESCRIBED "
              + " SET QUANTITY=? WHERE PRESCRIBED_ID=?");
      ps.setInt(1, qty);
      ps.setInt(2, prescriptionid);
      if (ps.executeUpdate() > 0) {
        status = true;
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return status;
  }
}
