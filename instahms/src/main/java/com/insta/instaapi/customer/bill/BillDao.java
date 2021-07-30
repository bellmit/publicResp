package com.insta.instaapi.customer.bill;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillDao extends GenericDAO {

  static Logger logger = LoggerFactory.getLogger(BillDao.class);

  private Connection con = null;

  public BillDao(Connection con) {
    super("bill");
    this.con = con;
  }

  public static String GET_BILLS_FOR_API_QUERY = "SELECT "
      + " pr.mr_no, pr.patient_id as visit_id, cm.center_name, pr.center_id, b.bill_no,"
      + " CASE WHEN b.bill_type='P' THEN 'BILL_NOW' ELSE 'BILL_LATER' END  AS bill_type,"
      + " b.opened_by, CASE WHEN b.status = 'A' THEN 'OPEN' WHEN b.status = 'F' THEN 'FINALIZED' "
      + "WHEN b.status = 'C' THEN 'CLOSED' ELSE 'CANCELLED' END  AS bill_status, "
      + " b.is_tpa AS insurance_linked_bill, "
      + " CASE WHEN b.payment_status = 'P' THEN 'PAID' ELSE 'UNPAID' END  AS payment_status, "
      + " (b.total_amount + b.total_discount) AS bill_amount, "
      + " b.total_discount, b.total_tax, b.total_amount + b.total_tax as net_amount, "
      + " (b.total_amount - b.total_claim) AS patient_amount, "
      + " (b.total_tax - b.total_claim_tax ) AS patient_tax_amt, b.points_redeemed_amt, "
      + " b.total_receipts as total_patient_payments, b.deposit_set_off, "
      + "(b.total_amount + b.total_tax - b.total_receipts - b.total_claim - "
      + "b.total_claim_tax  - b.deposit_set_off - b.points_redeemed_amt) AS patient_due_amount, "
      + " to_char(b.open_date AT TIME ZONE (SELECT current_setting('TIMEZONE')) "
      + "AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as open_date, "
      + " to_char(b.finalized_date AT TIME ZONE (SELECT current_setting('TIMEZONE')) "
      + "AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as finalized_date, "
      + " to_char(b.last_finalized_at AT TIME ZONE (SELECT current_setting('TIMEZONE')) "
      + "AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as last_finalized_at, "
      + " d.dept_name as treating_department, "
      + " to_char(pr.discharge_date AT TIME ZONE (SELECT current_setting('TIMEZONE')) "
      + "AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS discharge_date,  "
      + " pr.discharge_time, to_char(pr.discharge_finalized_date AT TIME ZONE ("
      + "SELECT current_setting('TIMEZONE')) AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"')"
      + " AS discharge_finalized_date, " + " pr.discharge_finalized_time   " + " FROM bill b "
      + " JOIN patient_registration pr ON (pr.patient_id = b.visit_id) "
      + " JOIN hospital_center_master cm ON (cm.center_id=pr.center_id) "
      + " LEFT JOIN department d ON d.dept_id = pr.admitted_dept " + " WHERE (@ BETWEEN ? AND ?)";

  public static String GET_RECEIPTS_BY_BILL_QUERY = "SELECT br.bill_no, r.receipt_id, "
      + "r.amount, c.counter_no AS counter, " + " r.created_by as collected_by, p.payment_mode, "
      + " CASE WHEN r.receipt_type = 'R' AND tpa_id IS NULL THEN 'RECEIPT' "
      + "WHEN r.receipt_type = 'F' AND tpa_id IS NULL THEN 'REFUND' WHEN r.receipt_type = 'R' "
      + "AND tpa_id IS NOT NULL THEN 'SPONSOR RECEIPT' END AS receipt_type, "
      + " CASE WHEN r.tpa_id IS NOT NULL THEN 't' ELSE 'f' END  as paid_by_sponsor, "
      + " CASE WHEN r.is_settlement='t' THEN 'ADVANCE' ELSE 'SETTLEMENT' END AS receipt_subtype, "
      + " to_char(r.display_date AT TIME ZONE (SELECT current_setting('TIMEZONE')) "
      + "AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as receipt_date, "
      + " r.payment_mode_id, CASE WHEN r.receipt_type = 'R' AND tpa_id IS NULL THEN 'RECEIPT' "
      + "WHEN r.receipt_type = 'F' AND tpa_id IS NULL THEN 'REFUND' WHEN r.receipt_type = 'R' "
      + "AND tpa_id IS NOT NULL THEN 'R' END AS payment_type, "
      + " r.card_number,r.bank_name,r.card_number, r.bank_name, bm.bank_id  "
      + " FROM receipts r JOIN bill_receipts br ON (r.receipt_id = br.receipt_no) "
      + " JOIN payment_mode_master p ON (p.mode_id = r.payment_mode_id) "
      + " JOIN counters c ON (c.counter_id = r.counter) "
      + " LEFT JOIN bank_master bm ON bm.bank_name=r.bank_name" + " WHERE br.bill_no in (@)";

  public static String GET_CHARGES_BY_BILL_QUERY = "SELECT bc.bill_no, bc.charge_id, "
      + " to_char(bc.posted_date AT TIME ZONE (SELECT current_setting('TIMEZONE')) "
      + "AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as posted_date,"
      + " cg.chargegroup_name as charge_group, ch.chargehead_name AS charge_head, "
      + " bc.act_description_id AS activity_id, bc.act_description AS description, "
      + " bc.act_remarks as details, bc.act_rate as rate, "
      + " bc.act_quantity as quantity, bc.amount, bc.discount, bc.tax_amt as tax_amount, "
      + " bc.doctor_amount AS doctor_net_amount, d.doctor_name AS conducting_doctor, "
      + " (COALESCE(bc.amount,0) + COALESCE(bc.tax_amt,0) - COALESCE(bc.doctor_amount,0) - "
      + "COALESCE(bc.prescribing_dr_amount,0) - COALESCE(bc.referal_amount,0) + "
      + "COALESCE(bc.hosp_discount_amt,0) ) AS hospital_amount  " + " FROM bill_charge bc "
      + " JOIN chargegroup_constants cg ON (cg.chargegroup_id = bc.charge_group) "
      + " JOIN chargehead_constants ch ON (ch.chargehead_id = bc.charge_head) "
      + " LEFT JOIN doctors d ON d.doctor_id = bc.payee_doctor_id  "
      + " WHERE bc.status != 'X' AND bc.bill_no in (@)";

  /**
   * Gets the bills.
   *
   * @param con the con
   * @param fromTime the from time
   * @param toTime the to time
   * @param mrNo the mr no
   * @param centerId the center id
   * @param page the page
   * @param useFinalizedDate the use finalized date
   * @return the bills
   * @throws SQLException the SQL exception
   */
  public static List<Map<String, Object>> getBills(Connection con, Object fromTime, Object toTime,
      String mrNo, Integer centerId, long page, Boolean useFinalizedDate) throws SQLException {
    Map<String, List<Object>> receipts = new HashMap<String, List<Object>>();
    Map<String, List<Object>> charges = new HashMap<String, List<Object>>();
    String billQuery = GET_BILLS_FOR_API_QUERY.replace("@",
        useFinalizedDate ? "b.finalized_date" : "b.open_date");
    List<Object> args = new ArrayList<Object>();
    List<String> billNos = new ArrayList<String>();
    if (page < 1) {
      page = 1;
    }
    args.add(fromTime);
    args.add(toTime);
    if (mrNo != null && !mrNo.isEmpty()) {
      billQuery += " AND pr.mr_no = ?";
      args.add(mrNo);
    }
    if (centerId != null) {
      billQuery += " AND pr.center_id = ?";
      args.add(centerId);
    }
    billQuery += " ORDER BY " + (useFinalizedDate ? "b.finalized_date" : "b.open_date");
    billQuery += " LIMIT 100 OFFSET ?";
    args.add((page - 1) * 100);
    List<Map<String, Object>> billsList = ConversionUtils
        .copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(con, billQuery, args.toArray()));
    if (billsList.isEmpty()) {
      return billsList;
    }
    for (Map<String, Object> bill : billsList) {
      billNos.add((String) bill.get("bill_no"));
    }
    String[] placeHolderArr = new String[billNos.size()];
    Arrays.fill(placeHolderArr, "?");
    String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
    List<Map<String, Object>> receiptList = ConversionUtils
        .copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(con,
            GET_RECEIPTS_BY_BILL_QUERY.replace("@", placeHolders), billNos.toArray()));
    List<Map<String, Object>> chargeList = ConversionUtils
        .copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(con,
            GET_CHARGES_BY_BILL_QUERY.replace("@", placeHolders), billNos.toArray()));
    // Transform Simple list of receipts to List of receipts grouped by bill_no
    for (Map<String, Object> receipt : receiptList) {
      String thisBill = (String) receipt.get("bill_no");
      if (!receipts.containsKey(thisBill)) {
        receipts.put(thisBill, new ArrayList<Object>());
      }
      receipts.get(thisBill).add(receipt);
    }
    // Transform Simple list of charges to List of charges grouped by bill_no
    for (Map<String, Object> charge : chargeList) {
      String thisBill = (String) charge.get("bill_no");
      if (!charges.containsKey(thisBill)) {
        charges.put(thisBill, new ArrayList<Object>());
      }
      charges.get(thisBill).add(charge);
    }
    // Inject charges anf receipts list into individual bill map
    List<Map<String, Object>> bills = new ArrayList<Map<String, Object>>();
    for (Map<String, Object> bill : billsList) {
      String billNo = (String) bill.get("bill_no");
      Map<String, Object> billMap = new HashMap<String, Object>();
      billMap.putAll(bill);
      billMap.put("receipts",
          receipts.containsKey(billNo) ? receipts.get(billNo) : new ArrayList<Object>());
      billMap.put("charges",
          charges.containsKey(billNo) ? charges.get(billNo) : new ArrayList<Object>());
      bills.add(billMap);
    }
    return bills;
  }
}