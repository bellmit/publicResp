package com.insta.hms.outpatient;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The Class PrescribeDAO.
 *
 * @author krishna.t
 */
public class PrescribeDAO {

  /** The Constant PHARMA_MEDICINE_PRESCRIPTIONS. */
  private static final String PHARMA_MEDICINE_PRESCRIPTIONS =
      " SELECT" + " sum(pmp.issued_qty) as issued_qty,"
          + " sum(pmp.medicine_quantity)::integer as medicine_quantity, "
          + " pmp.medicine_id, pp.consultation_id, pmp.pbm_presc_id, "
          + " min(pmp.frequency) as frequency, min(pmp.medicine_remarks) as medicine_remarks,"
          + " min(pmp.strength) as strength, "
          + " min(pmp.duration) as duration,min(pmp.route_of_admin) as route_of_admin ,"
          + " min(pmp.duration_units) as duration_units, "
          + " sid.medicine_name as pres_medicine_name, pp.special_instr ,"
          + " pmp.op_medicine_pres_id as erx_activity_id, pbmp.erx_reference_no, "
          + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks, "
          + " pmp.is_discharge_medication"
          + " FROM patient_prescription pp" + " JOIN patient_medicine_prescriptions pmp"
          + " ON (pp.patient_presc_id=pmp.op_medicine_pres_id)"
          + "   JOIN store_item_details sid ON (sid.medicine_id=pmp.medicine_id) "
          + " LEFT JOIN pbm_prescription pbmp ON (pbmp.pbm_presc_id = pmp.pbm_presc_id) "
          + " WHERE #filter#=? and pp.status IN ('P', 'PA') "
          + " GROUP BY pmp.medicine_id, pp.consultation_id, sid.medicine_name,"
          + " pmp.pbm_presc_id, pp.special_instr , pmp.op_medicine_pres_id, pbmp.erx_reference_no,"
          + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks,"
          + " pmp.is_discharge_medication";

  /**
   * Gets the pharma medicine prescriptions.
   *
   * @param consultationId the consultation id
   * @param modErx the mod erx
   * @return the pharma medicine prescriptions
   * @throws SQLException the SQL exception
   */
  public static List getPharmaMedicinePrescriptions(Object consultationId, Boolean modErx)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    boolean isInPatient = (consultationId instanceof String);
    String query = (PHARMA_MEDICINE_PRESCRIPTIONS).replace("#filter#",
        isInPatient ? "pmp.is_discharge_medication = true and pp.visit_id" : "pp.consultation_id");
    PreparedStatement ps = con.prepareStatement(query);
    try {
      if (isInPatient) {
        ps.setString(1, (String) consultationId);
      } else {
        ps.setInt(1, (Integer) consultationId);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant PHARMA_GENERIC_PRESCRIPTIONS. */
  private static final String PHARMA_GENERIC_PRESCRIPTIONS =
      " SELECT pmp.*," + " g.generic_name, pp.consultation_id, pp.special_instr"
          + " FROM patient_prescription pp " + " JOIN patient_medicine_prescriptions pmp"
          + " ON (pp.patient_presc_id=pmp.op_medicine_pres_id) "
          + "   JOIN generic_name g ON (pmp.generic_code=g.generic_code) "
          + " WHERE pp.consultation_id=?";

  /**
   * Gets the pharma generic prescriptions.
   *
   * @param consultationId the consultation id
   * @return the pharma generic prescriptions
   * @throws SQLException the SQL exception
   */
  public static List getPharmaGenericPrescriptions(int consultationId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(PHARMA_GENERIC_PRESCRIPTIONS);
      ps.setInt(1, consultationId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant PHARMA_PBM_MEDICINE_PRESCRIPTION. */
  private static final String PHARMA_PBM_MEDICINE_PRESCRIPTION =
      " SELECT pmp.issued_qty," + " pmp.medicine_quantity, pmp.pbm_medicine_pres_id,"
          + "   pmp.frequency , pmp.medicine_remarks, pmp.strength, pmp.duration,"
          + " pmp.route_of_admin, pmp.duration_units, "
          + " pmp.medicine_id, pmp.consultation_id, pmp.pbm_presc_id, pmp.rate, pmp.discount,"
          + " pmp.amount, pmp.user_unit,"
          + " pmp.pbm_status, sid.issue_units, sid.package_uom, pmp.claim_net_amount,"
          + " pmp.claim_net_approved_amount, "
          + "   sid.medicine_name as pres_medicine_name, pmp.special_instr, pamtd.net, "
          + " '' as erx_activity_id, '' as erx_reference_no, "
          + " false as item_excluded_from_doctor, '' as item_excluded_from_doctor_remarks"
          + " FROM pbm_medicine_prescriptions pmp "
          + "   JOIN store_item_details sid ON (sid.medicine_id=pmp.medicine_id) "
          + " JOIN pbm_prescription pbmp ON (pmp.pbm_presc_id = pbmp.pbm_presc_id ) "
          + " LEFT JOIN pbm_approval_amount_details pamtd on "
          + " (pamtd.pbm_request_id = pbmp.pbm_request_id and"
          + " pamtd.pbm_medicine_pres_id = pmp.pbm_medicine_pres_id) "
          + " WHERE pmp.pbm_presc_id=? and pmp.issued NOT IN ('Y', 'C') ";

  /**
   * Gets the PBM medicine prescription.
   *
   * @param pbmPrescId the pbm presc id
   * @return the PBM medicine prescription
   * @throws SQLException the SQL exception
   */
  public static List getPBMMedicinePrescription(int pbmPrescId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(PHARMA_PBM_MEDICINE_PRESCRIPTION);
      ps.setInt(1, pbmPrescId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant UPDATE_ADD_TO_BILL_IN_TEST. */
  private static final String UPDATE_ADD_TO_BILL_IN_TEST =
      " UPDATE patient_prescription" + " set status=?, username=?" + " WHERE patient_presc_id=?";

  /**
   * Update test prescription.
   *
   * @param con the con
   * @param status the status
   * @param presId the pres id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateTestPrescription(Connection con, String status, Integer presId)
      throws SQLException {
    PreparedStatement ps = con.prepareStatement(UPDATE_ADD_TO_BILL_IN_TEST);
    try {
      ps.setString(1, status);
      ps.setString(2, RequestContext.getUserName());
      ps.setInt(3, presId);

      int count = ps.executeUpdate();
      if (count > 0) {
        return true;
      }
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
    return false;
  }

  /** The Constant UPDATE_ADD_TO_BILL_IN_SERVICE. */
  private static final String UPDATE_ADD_TO_BILL_IN_SERVICE =
      " UPDATE patient_prescription" + " set status=?, username=?" + " WHERE patient_presc_id=?";

  /**
   * Update service prescription.
   *
   * @param con the con
   * @param status the status
   * @param presId the pres id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateServicePrescription(Connection con, String status, int presId)
      throws SQLException {
    PreparedStatement ps = con.prepareStatement(UPDATE_ADD_TO_BILL_IN_SERVICE);
    try {
      ps.setString(1, status);
      ps.setString(2, RequestContext.getUserName());
      ps.setInt(3, presId);

      int count = ps.executeUpdate();
      if (count > 0) {
        return true;
      }
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
    return false;
  }

  /** The Constant UPDATE_OP_PRESCRIPTION. */
  private static final String UPDATE_OP_PRESCRIPTION = "UPDATE patient_medicine_prescriptions"
      + " SET mod_time=LOCALTIMESTAMP(0), issued_qty=issued_qty+?, username=? ";

  /** The Constant UPDATE_PRESCRIPTION_STATUS_ALL. */
  private static final String UPDATE_PRESCRIPTION_STATUS_ALL = " UPDATE patient_prescription"
      + " SET status='O', username=?" + " from patient_medicine_prescriptions pmp "
      + " WHERE consultation_id=? and patient_presc_id=op_medicine_pres_id"
      + " AND COALESCE(pbm_presc_id, 0) = ? ";

  private static final String UPDATE_IP_DISCHARGE_MEDICATION_STATUS_ALL =
      " UPDATE patient_prescription" + " SET status='O', username=?"
          + " from patient_medicine_prescriptions pmp "
          + " WHERE pmp.visit_id=? AND is_discharge_medication = true AND"
          + " patient_presc_id=op_medicine_pres_id" + " AND COALESCE(pbm_presc_id, 0) = ? ";

  /** The Constant UPDATE_PBM_MEDICINE_PRESCRIPTION. */
  private static final String UPDATE_PBM_MEDICINE_PRESCRIPTION =
      "UPDATE" + " pbm_medicine_prescriptions SET issued=?,"
          + " mod_time=LOCALTIMESTAMP(0), issued_qty=issued_qty+? ";

  /** The Constant UPDATE_PBM_MEDICINE_STATUS_ALL. */
  private static final String UPDATE_PBM_MEDICINE_STATUS_ALL =
      " UPDATE" + " pbm_medicine_prescriptions SET issued='Y'"
          + " WHERE consultation_id=? and COALESCE(pbm_presc_id, 0) = ? ";

  private static final String UPDATE_PBM_IP_DISCHARGE_MEDICATION_STATUS_ALL =
      " UPDATE" + " pbm_medicine_prescriptions SET issued='Y'"
          + " WHERE visit_id=? and COALESCE(pbm_presc_id, 0) = ? ";

  /**
   * Update status.
   *
   * @param con the con
   * @param medAndQuantityMap the med and quantity map
   * @param consIds the cons ids
   * @param medDispOpt the med disp opt
   * @param saleId the sale id
   * @param prescriptionsByGenerics the prescriptions by generics
   * @param pbmPrescId the pbm presc id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static boolean updateStatus(Connection con, Map<String, BigDecimal> medAndQuantityMap,
      String[] consIds, String[] medDispOpt, String saleId, boolean prescriptionsByGenerics,
      int pbmPrescId, String visitType, String visitId, List<Integer> docPrescIdList)
      throws SQLException, IOException {

    boolean update = updateMedicinesStatus(con, medAndQuantityMap, consIds, medDispOpt, saleId,
        prescriptionsByGenerics, pbmPrescId, visitType, visitId, docPrescIdList);
    update = update && updatePBMMedicinesStatus(con, medAndQuantityMap, consIds, medDispOpt, saleId,
        prescriptionsByGenerics, pbmPrescId, visitType, visitId);

    return update;
  }

  /**
   * Update PBM medicines status.
   *
   * @param con the con
   * @param medAndQuantityMap the med and quantity map
   * @param consIds the cons ids
   * @param medDispOpt the med disp opt
   * @param saleId the sale id
   * @param prescriptionsByGenerics the prescriptions by generics
   * @param pbmPrescId the pbm presc id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static boolean updatePBMMedicinesStatus(Connection con,
      Map<String, BigDecimal> medAndQuantityMap, String[] consIds, String[] medDispOpt,
      String saleId, boolean prescriptionsByGenerics, int pbmPrescId, String visitType,
      String visitId) throws SQLException, IOException {
    GenericDAO pbmMedSalesDAO = new GenericDAO("pbm_medicine_sales");
    PreparedStatement ps = null;
    PreparedStatement ps1 = null;
    PreparedStatement ps2 = null;
    boolean isIPDischargeMedication = visitType.equals("i");
    boolean success = true;
    try {
      String keyCol = prescriptionsByGenerics ? "generic_code" : "medicine_id";

      String medquery = " SELECT pbm_medicine_pres_id, medicine_id,"
          + " issued_qty, coalesce(medicine_quantity, 1) as medicine_quantity, "
          + " consultation_id, initial_sale_id " + " FROM pbm_medicine_prescriptions pmp ";

      String ipDischargeMedicationQuery = " SELECT pbm_medicine_pres_id, medicine_id,"
          + " issued_qty, coalesce(medicine_quantity, 1) as medicine_quantity, "
          + " visit_id, initial_sale_id " + " FROM pbm_medicine_prescriptions pmp WHERE"
          + " pmp.#filter#=? AND COALESCE(pbm_presc_id, 0) = ?"
          + " AND issued not in ('Y', 'C')";

      for (Map.Entry<String, BigDecimal> entry : medAndQuantityMap.entrySet()) {
        BigDecimal qty = entry.getValue();
        String key = entry.getKey();
        String id = "";
        boolean first = true;
        if (!isIPDischargeMedication) {
          List<Integer> consIdList = new ArrayList<Integer>();
          for (int j = 0; j < consIds.length; j++) {
            if (!consIds[j].equals("")) {
              consIdList.add(Integer.parseInt(consIds[j]));
            }
          }

          StringBuilder whereCond = new StringBuilder();
          DataBaseUtil.addWhereFieldInList(whereCond, "consultation_id", consIdList);
          whereCond.append(" AND " + DataBaseUtil.quoteIdent(keyCol) + " = ? and"
              + " COALESCE(pbm_presc_id, 0) = ? and issued not in ('Y', 'C')"
              + " order by consultation_id");

          ps = con.prepareStatement(medquery + whereCond.toString());

          int psindex = 1;
          for (Integer consId : consIdList) {
            ps.setInt(psindex++, consId);
          }
          if (prescriptionsByGenerics) {
            ps.setString(psindex++, key);
          } else {
            ps.setInt(psindex++, Integer.parseInt(key));
          }
          ps.setInt(psindex++, pbmPrescId);
        } else {
          String ipQuery = ipDischargeMedicationQuery.replace("#filter#",
              prescriptionsByGenerics ? "generic_code" : "medicine_id");
          ps = con.prepareStatement(ipQuery);
          if (prescriptionsByGenerics) {
            ps.setString(1, key);
          } else {
            ps.setInt(1, Integer.parseInt(key));
          }
          ps.setInt(2, pbmPrescId);
        }

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
          BigDecimal issuedQty = rs.getBigDecimal("issued_qty");
          BigDecimal prescQty = rs.getBigDecimal("medicine_quantity");
          BigDecimal pendingQty = prescQty.subtract(issuedQty);
          String status = "";
          BigDecimal dispensedQty = BigDecimal.ZERO;
          if (qty.compareTo(pendingQty) >= 0) {
            // if ordered qty greater than or equal to the pending quantity and is the last record
            // then dump all the qty to the last consultation row
            dispensedQty = (rs.isLast()) ? qty : pendingQty;
            qty = qty.subtract(pendingQty);
            status = "Y";
          } else {
            dispensedQty = qty;
            String dispenseStatus = "";
            if (!isIPDischargeMedication) {
              Integer consultationId = rs.getInt("consultation_id");
              for (int k = 0; k < consIds.length; k++) {
                if (consIds[k].equals(consultationId + "")) {
                  dispenseStatus = medDispOpt[k];
                }
              }
            } else {
              dispenseStatus = medDispOpt[0];
            }
            if (dispenseStatus.equals("all")) {
              status = "Y";
            } else if (dispenseStatus.equals("partiall")) {
              status = qty.compareTo(BigDecimal.ZERO) == 1 ? "Y" : "N";
            } else if (dispenseStatus.equals("full")) {
              status = qty.compareTo(BigDecimal.ZERO) == 0 ? "N" : "P";
            }
          }

          String query = UPDATE_PBM_MEDICINE_PRESCRIPTION;
          String initialSaleId = rs.getString("initial_sale_id");
          if (initialSaleId == null || initialSaleId.equals("")) {
            query = query + " ,initial_sale_id=?,final_sale_id=? ";
          } else {
            query = query + " ,final_sale_id=? ";
          }
          if (isIPDischargeMedication) {
            query += " WHERE visit_id=? AND pbm_medicine_pres_id = ?";
          } else {
            query += " WHERE consultation_id=? AND pbm_medicine_pres_id = ?";
          }

          int index = 1;
          ps1 = con.prepareStatement(query);
          ps1.setString(index++, status);
          ps1.setBigDecimal(index++, dispensedQty);

          if (initialSaleId == null || initialSaleId.equals("")) {
            ps1.setString(index++, saleId);
            ps1.setString(index++, saleId);
          } else {
            ps1.setString(index++, saleId);
          }

          if (isIPDischargeMedication) {
            ps1.setString(index++, visitId);
          } else {
            int consultationId = rs.getInt("consultation_id");
            ps1.setInt(index++, consultationId);
          }

          Integer prescribedId = rs.getInt("pbm_medicine_pres_id");
          ps1.setInt(index++, prescribedId);


          if (ps1.executeUpdate() == 0) {
            success = false;
            break;
          }

          insertIntopbmMedicineSales(con, pbmMedSalesDAO, saleId, pbmPrescId, prescribedId);
        }
      }

      String allPrescQuery = isIPDischargeMedication ? UPDATE_PBM_IP_DISCHARGE_MEDICATION_STATUS_ALL
          : UPDATE_PBM_MEDICINE_STATUS_ALL;

      if (isIPDischargeMedication) {
        if (medDispOpt[0].equalsIgnoreCase("all")) {
          ps2 = con.prepareStatement(allPrescQuery);
          ps2.setString(1, visitId);
          ps2.setInt(2, pbmPrescId);
          ps2.executeUpdate();
          ps2.close();
        }
      } else {
        for (int i = 0; i < consIds.length; i++) {
          if (consIds[i].equals("")) {
            continue;
          }

          if (medDispOpt[i].equalsIgnoreCase("all")) {
            // close all prescriptions whether they were issued or not. This is required
            // the medicine may not appear in the list of sold items or unavailable items
            // if the user has explicitly deleted the item.
            ps = con.prepareStatement(allPrescQuery);
            ps.setInt(1, Integer.parseInt(consIds[i]));
            ps.setInt(2, pbmPrescId);
            ps.executeUpdate();
            ps.close();
          }
        }
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (ps1 != null) {
        ps1.close();
      }
      if (ps2 != null) {
        ps2.close();
      }
    }
    return success;
  }

  /**
   * Insert intopbm medicine sales.
   *
   * @param con the con
   * @param pbmMedSalesDAO the pbm med sales DAO
   * @param saleId the sale id
   * @param pbmPrescId the pbm presc id
   * @param prescribedId the prescribed id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static boolean insertIntopbmMedicineSales(Connection con, GenericDAO pbmMedSalesDAO,
      String saleId, int pbmPrescId, Integer prescribedId) throws SQLException, IOException {
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("pbm_presc_id", pbmPrescId);
    keys.put("pbm_medicine_pres_id", prescribedId);
    keys.put("sale_id", saleId);
    boolean success = false;
    BasicDynaBean pbmMedicineSalesbean = pbmMedSalesDAO.findByKey(con, keys);
    if (pbmMedicineSalesbean == null) {
      pbmMedicineSalesbean = pbmMedSalesDAO.getBean();
      pbmMedicineSalesbean.set("sale_id", saleId);
      pbmMedicineSalesbean.set("pbm_medicine_pres_id", prescribedId);
      pbmMedicineSalesbean.set("pbm_presc_id", pbmPrescId);
      success = pbmMedSalesDAO.insert(con, pbmMedicineSalesbean);
    }
    return success;
  }

  /**
   * Update medicines status.
   *
   * @param con the con
   * @param medAndQuantityMap the med and quantity map
   * @param consIds the cons ids
   * @param medDispOpt the med disp opt
   * @param saleId the sale id
   * @param prescriptionsByGenerics the prescriptions by generics
   * @param pbmPrescId the pbm presc id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateMedicinesStatus(Connection con,
      Map<String, BigDecimal> medAndQuantityMap, String[] consIds, String[] medDispOpt,
      String saleId, boolean prescriptionsByGenerics, int pbmPrescId, String visitType,
      String visitId, List<Integer> docPrescIdList) throws SQLException {

    PreparedStatement ps = null;
    PreparedStatement ps1 = null;
    PreparedStatement ps2 = null;
    boolean success = true;
    boolean isIPDischargeMedication = visitType.equals("i");
    try {
      String keyCol = prescriptionsByGenerics ? "generic_code" : "medicine_id";
      String medquery = " SELECT op_medicine_pres_id, medicine_id, issued_qty,"
          + " coalesce(medicine_quantity, 1) as medicine_quantity, "
          + " consultation_id, initial_sale_id " + " FROM patient_medicine_prescriptions pmp "
          + " JOIN patient_prescription pp ON (pp.patient_presc_id=pmp.op_medicine_pres_id)";

      // To update IP Discharge Medication prescriptions using visit id HMS-33076
      String ipDischargeMedicationQuery = " SELECT op_medicine_pres_id, medicine_id, issued_qty,"
          + " coalesce(medicine_quantity, 1) as medicine_quantity,"
          + " pmp.visit_id, initial_sale_id " + " FROM patient_medicine_prescriptions pmp"
          + " JOIN patient_prescription pp ON (pp.patient_presc_id=pmp.op_medicine_pres_id)"
          + " WHERE pmp.visit_id = ? and pmp.is_discharge_medication=true AND"
          + " pmp.#filter#=? AND COALESCE(pbm_presc_id, 0) = ? AND" + " pp.status in ('P', 'PA')";

      for (Map.Entry<String, BigDecimal> entry : medAndQuantityMap.entrySet()) {
        BigDecimal qty = entry.getValue();
        String key = entry.getKey();

        if (!isIPDischargeMedication) {
          List<Integer> consIdList = new ArrayList<Integer>();
          for (int j = 0; j < consIds.length; j++) {
            if (!consIds[j].equals("")) {
              consIdList.add(Integer.parseInt(consIds[j]));
            }
          }

          StringBuilder whereCond = new StringBuilder();
          DataBaseUtil.addWhereFieldInList(whereCond, "consultation_id", consIdList);
          whereCond.append(" AND " + DataBaseUtil.quoteIdent(keyCol)
              + " = ? AND COALESCE(pbm_presc_id, 0) = ? and"
              + " status in ('P', 'PA') order by consultation_id");

          ps = con.prepareStatement(medquery + whereCond.toString());

          int psIndex = 1;
          for (Integer consId : consIdList) {
            ps.setInt(psIndex++, consId);
          }
          if (prescriptionsByGenerics) {
            ps.setString(psIndex++, key);
          } else {
            ps.setInt(psIndex++, Integer.parseInt(key));
          }
          ps.setInt(psIndex++, pbmPrescId);
        } else {
          String ipQuery = ipDischargeMedicationQuery.replace("#filter#",
              prescriptionsByGenerics ? "generic_code" : "medicine_id");
          ps = con.prepareStatement(ipQuery);
          ps.setString(1, visitId);
          if (prescriptionsByGenerics) {
            ps.setString(2, key);
          } else {
            ps.setInt(2, Integer.parseInt(key));
          }
          ps.setInt(3, pbmPrescId);
        }

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
          BigDecimal issuedQty = rs.getBigDecimal("issued_qty");
          BigDecimal prescQty = rs.getBigDecimal("medicine_quantity");
          BigDecimal pendingQty = prescQty.subtract(issuedQty);
          String status = "";
          BigDecimal dispensedQty = BigDecimal.ZERO;
          if (qty.compareTo(pendingQty) >= 0) {
            // if ordered qty greater than or equal to the pending quantity and is the last record
            // then dump all the qty to the last consultation row
            dispensedQty = (rs.isLast()) ? qty : pendingQty;
            qty = qty.subtract(pendingQty);
            status = "O";
          } else {
            dispensedQty = qty;
            String dispenseStatus = "";
            if (!isIPDischargeMedication) {
              Integer consultationId = rs.getInt("consultation_id");
              for (int k = 0; k < consIds.length; k++) {
                if (consIds[k].equals(consultationId + "")) {
                  dispenseStatus = medDispOpt[k];
                }
              }
            } else {
              dispenseStatus = medDispOpt[0];
            }
            if (dispenseStatus.equals("all")) {
              status = "O";
            } else if (dispenseStatus.equals("partiall")) {
              // 'O' is ordered, 'P' is in progress
              status = qty.compareTo(BigDecimal.ZERO) == 1 ? "O" : "P";
            } else if (dispenseStatus.equals("full")) {
              // 'PA' is Partially Ordered.
              status = qty.compareTo(BigDecimal.ZERO) == 0 ? "N" : "PA";
            }
          }

          ps1 = con.prepareStatement("UPDATE patient_prescription SET status=?, username=? WHERE "
              + " patient_presc_id=?");
          ps1.setString(1, status);
          ps1.setString(2, RequestContext.getUserName());

          Integer prescribedId = rs.getInt("op_medicine_pres_id");
          docPrescIdList.add(prescribedId);
          
          ps1.setInt(3, prescribedId);
          if (ps1.executeUpdate() == 0) {
            success = false;
            break;
          }
          ps1.close();

          String query = UPDATE_OP_PRESCRIPTION;
          String initialSaleId = rs.getString("initial_sale_id");
          if (initialSaleId == null || initialSaleId.equals("")) {
            query = query + " ,initial_sale_id=?,final_sale_id=? ";
          } else {
            query = query + " ,final_sale_id=? ";
          }

          int index = 1;
          ps1 = con.prepareStatement(query + " WHERE op_medicine_pres_id = ?");
          ps1.setBigDecimal(index++, dispensedQty);
          ps1.setString(index++, RequestContext.getUserName());
          if (initialSaleId == null || initialSaleId.equals("")) {
            ps1.setString(index++, saleId);
            ps1.setString(index++, saleId);
          } else {
            ps1.setString(index++, saleId);
          }
          ps1.setInt(index++, prescribedId);
          if (ps1.executeUpdate() == 0) {
            success = false;
            break;
          }

        }
      }

      String allPrescQuery = isIPDischargeMedication ? UPDATE_IP_DISCHARGE_MEDICATION_STATUS_ALL
          : UPDATE_PRESCRIPTION_STATUS_ALL;

      if (isIPDischargeMedication) {
        if (medDispOpt[0].equalsIgnoreCase("all")) {
          ps2 = con.prepareStatement(allPrescQuery);
          ps2.setString(1, RequestContext.getUserName());
          ps2.setString(2, visitId);
          ps2.setInt(3, pbmPrescId);
          ps2.executeUpdate();
          ps2.close();
        }
      } else {
        for (int i = 0; i < consIds.length; i++) {
          if (consIds[i].equals("")) {
            continue;
          }

          if (medDispOpt[i].equalsIgnoreCase("all")) {
            // close all prescriptions whether they were issued or not. This is required
            // the medicine may not appear in the list of sold items or unavailable items
            // if the user has explicitly deleted the item.
            ps = con.prepareStatement(allPrescQuery);
            ps.setString(1, RequestContext.getUserName());
            ps.setInt(2, Integer.parseInt(consIds[i]));
            ps.setInt(3, pbmPrescId);
            ps.executeUpdate();
            ps.close();
          }
        }
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (ps1 != null) {
        ps1.close();
      }
      if (ps2 != null) {
        ps2.close();
      }
    }
    return success;
  }

  /**
   * Close all.
   *
   * @param consultationIds the consultation ids
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean closeAll(String[] consultationIds) throws SQLException {
    if (consultationIds == null || consultationIds.length == 0) {
      return true;
    }

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    PreparedStatement ps = null;
    boolean success = true;
    try {
      for (String id : consultationIds) {
        Pattern pattern = Pattern.compile("[0-9]+");
        pattern = Pattern.compile("\\d+");
        if (pattern.matcher(id).matches()) {
          ps = con.prepareStatement(" UPDATE patient_prescription"
              + " SET status='O', username=? from patient_medicine_prescriptions pmp "
              + " WHERE consultation_id=? and patient_presc_id=op_medicine_pres_id");

          ps.setString(1, RequestContext.getUserName());
          ps.setInt(2, Integer.parseInt(id));
          if (ps.executeUpdate() > 0) {
            success = true;
          } else {
            success = false;
            break;
          }
        } else {
          // Close IP discharge Medication
          ps = con.prepareStatement(" UPDATE patient_prescription"
              + " SET status='O', username=? from patient_medicine_prescriptions pmp "
              + " WHERE visit_id=? and patient_presc_id=op_medicine_pres_id"
              + " and is_discharge_medication = true");

          ps.setString(1, RequestContext.getUserName());
          ps.setInt(2, Integer.parseInt(id));
          if (ps.executeUpdate() > 0) {
            success = true;
          } else {
            success = false;
            break;
          }

        }
      }

    } finally {
      DataBaseUtil.commitClose(con, success);
      if (ps != null) {
        ps.close();
      }
    }
    return success;
  }
}
