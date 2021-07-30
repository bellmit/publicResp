package com.insta.hms.ipservices;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// TODO: Auto-generated Javadoc
/**
 * The Class EquipmentsPrescribedDAO.
 */
public class EquipmentsPrescribedDAO {

  /** The Constant prescribedequipmentscancel. */
  public static final String prescribedequipmentscancel = "UPDATE patient_bed_eqipmentcharges "
      + "SET cancel_status='C' WHERE bed_prescribed_equip_id=?";

  /**
   * Cancel prescribed equipments.
   *
   * @param con the con
   * @param id the id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean cancelPrescribedEquipments(Connection con, int id) throws SQLException {
    boolean status = true;
    try (PreparedStatement ps = con.prepareStatement(prescribedequipmentscancel)) {
      ps.setInt(1, id);
      if (ps.executeUpdate() <= 0) {
        status = false;
      }
    }
    return status;
  }

  /** The Constant UPDATE_EQUIPMENT_PRESCRIPTION. */
  private static final String UPDATE_EQUIPMENT_PRESCRIPTION = "UPDATE patient_bed_eqipmentcharges "
      + "SET remarks = ?, doctor_id=?  WHERE bed_prescribed_equip_id = ?";

  /**
   * Update equipment prescription.
   *
   * @param con the con
   * @param prescriptionId the prescription id
   * @param remarks the remarks
   * @param docId the doc id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateEquipmentPrescription(Connection con, int prescriptionId,
      String remarks, String docId) throws SQLException {
    boolean updateStatus = true;
    try (PreparedStatement ps = con.prepareStatement(UPDATE_EQUIPMENT_PRESCRIPTION);) {
      ps.setString(1, remarks);
      ps.setString(2, docId);
      ps.setInt(3, prescriptionId);
      updateStatus = ps.executeUpdate() >= 1;
    }
    return updateStatus;
  }

}
