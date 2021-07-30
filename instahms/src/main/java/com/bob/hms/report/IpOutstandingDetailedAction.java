package com.bob.hms.report;

import au.com.bytecode.opencsv.CSVWriter;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * IP Detailed Outstanding CSV Export Actions.
 */
public class IpOutstandingDetailedAction extends DispatchAction {

  private static final String CSV_EXPORT_QUERY = "SELECT mr_no, patient_name, bill_no, bed_name,"
      + " total_amt - pharmacy_amt AS hospital_amt, hospital_advance,"
      + "  total_amt - pharmacy_amt - hospital_advance AS hospital_due, pharmacy_amt,"
      + " pharmacy_advance, pharmacy_amt-pharmacy_advance AS pharmacy_due, deposit_set_off,"
      + " CASE WHEN tpa_status  = 'I' THEN 'No' ELSE 'Yes' END AS tpa_status"
      + " FROM ("
      + "   SELECT pr.patient_id, b.bill_no, pr.mr_no, pr.status, wn.ward_no, wn.ward_name,"
      + "     pr.bed_type, bn.bed_name, hcm.center_id,"
      + "     (SELECT bed_name FROM ip_bed_details JOIN bed_names using(bed_id) "
      + "        WHERE is_bystander AND  b.visit_id = patient_id AND bed_state = 'O' LIMIT 1) "
      + "     as bystander_bed_name, b.deposit_set_off, get_patient_name("
      + "       pd.salutation, pd.patient_name, pd.middle_name, pd.last_name) as patient_name,"
      + "     CASE WHEN pr.primary_sponsor_id IS NULL or pr.primary_sponsor_id = '' THEN 'I' "
      + "       ELSE 'A' END AS tpa_status,  "
      + "     (b.total_amount+b.total_tax) - (b.total_claim+b.total_claim_tax) AS total_amt,"
      + "     (SELECT "
      + "       CASE WHEN "
      + "         pr.primary_sponsor_id IS NOT NULL and pr.primary_sponsor_id != '' "
      + "       THEN "
      + "         coalesce(sum((amount+tax_amt)-(insurance_claim_amount+sponsor_tax_amt)),0.00)"
      + "       ELSE "
      + "         coalesce(sum(amount+tax_amt),0.00) "
      + "       END "
      + "      FROM bill_charge bc WHERE "
      + "        bc.bill_no=b.bill_no AND charge_head IN ('PHMED','PHRET','PHCMED','PHCRET') "
      + "     AND bc.status !='X') AS pharmacy_amt,  "
      + "     (SELECT coalesce(sum(brh.amount), 0.00) "
      + "       FROM receipts brh "
      + "       JOIN bill_receipts brep ON brh.receipt_id = brep.receipt_no AND NOT brh.is_deposit "
      + "       JOIN counters ch ON (brh.counter = ch.counter_id) "
      + "       WHERE brep.bill_no = b.bill_no AND brh.receipt_type IN ('R','F') "
      + "         AND ch.counter_type != 'P'  "
      + "     ) AS hospital_advance,"
      + "     (SELECT coalesce(sum(brp.amount), 0.00)  "
      + "       FROM receipts brp "
      + "       JOIN bill_receipts brr ON brp.receipt_id = brr.receipt_no AND NOT brp.is_deposit "
      + "       JOIN counters cp ON (brp.counter = cp.counter_id) "
      + "       WHERE brr.bill_no = b.bill_no AND brp.receipt_type IN ('R','F') "
      + "        AND cp.counter_type = 'P' "
      + "     ) AS pharmacy_advance, "
      + "     pr.primary_sponsor_id, pr.center_id AS cen_id,hcm.center_name "
      + "   FROM bill b "
      + "   LEFT JOIN patient_registration pr ON (pr.patient_id=b.visit_id) "
      + "   LEFT JOIN patient_details pd on (pd.mr_no=pr.mr_no) "
      + "   LEFT JOIN admission a ON (a.patient_id=pr.patient_id) "
      + "   LEFT JOIN bed_names bn ON (bn.bed_id=a.bed_id) "
      + "   LEFT JOIN ward_names wn ON (wn.ward_no=bn.ward_no) "
      + "   LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id) "
      + "   WHERE b.status IN ('A','F') AND payment_status = 'U' AND bill_type IN ('P','C') "
      + " AND b.visit_type='i') as qry @ ORDER BY ward_name, bill_no";
  
  /**
   * Struts Route to handle and process CSV export request for 
   * IP Detailed Outstanding Report Builder.
   * @param maping - Struts Action Mapping
   * @param form - Struts Action Form
   * @param request - Request Object
   * @param response - Response Object
   * @return Struts Response
   * @throws Exception - exception
   */
  public ActionForward getCsv(ActionMapping maping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    response.reset();
    response.setHeader("Content-type", "application/csv");
    response.setHeader("Content-disposition", "attachment; filename=report.csv");
    response.setHeader("Readonly", "true");

    String[] visitStatusAr = request.getParameterValues("visitStatusArray");
    String[] tpaStatusAr = request.getParameterValues("insuranceArray");
    List<String> visitStatusList = new ArrayList(Arrays.asList(visitStatusAr));
    if (visitStatusList != null && visitStatusList.size() == 1
        && visitStatusList.get(0).equals("")) {
      visitStatusList.remove(0);
    }
    List<String> tpaStatusList = new ArrayList(Arrays.asList(tpaStatusAr));
    if (tpaStatusList != null && tpaStatusList.size() == 1 && tpaStatusList.get(0).equals("")) {
      tpaStatusList.remove(0);
    }



    StringBuilder whereCond = new StringBuilder();
    DataBaseUtil.addWhereFieldInList(whereCond, "status", visitStatusList);
    DataBaseUtil.addWhereFieldInList(whereCond, "tpa_status", tpaStatusList);
    if (visitStatusList.size() > 0 || tpaStatusList.size() > 0) {
      whereCond.append(" AND ");
    }

    if (visitStatusList.size() == 0 || tpaStatusList.size() == 0) {
      whereCond.append(" WHERE ");
    }

    whereCond.append(" (ward_no=? OR ?='') ");
    whereCond.append(" AND (primary_sponsor_id=? OR ?='') ");
    whereCond.append(request.getParameter("centerClause"));

    String pharAmtTxt = request.getParameter("pharAmtTxt");
    if (pharAmtTxt != null && !pharAmtTxt.equals("")) {
      whereCond.append(" AND (pharmacy_amt-pharmacy_advance) > ? ");
    }

    String hospAmtTxt = request.getParameter("hospAmtTxt");
    if (hospAmtTxt != null && !hospAmtTxt.equals("")) {
      whereCond.append(" AND (total_amt-pharmacy_amt-hospital_advance) > ?");
    }

    String totAmtTxt = request.getParameter("totAmtTxt");
    if (totAmtTxt != null && !totAmtTxt.equals("")) {
      whereCond.append(" AND (total_amt-pharmacy_advance-hospital_advance-deposit_set_off) > ?");
    }

    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con
        .prepareStatement(CSV_EXPORT_QUERY.replaceAll("@", whereCond.toString()));
    int index = 1;
    for (String visitStatus : visitStatusList) {
      ps.setString(index++, visitStatus);
    }
    for (String tpaStatus : tpaStatusList) {
      ps.setString(index++, tpaStatus);
    }
    String wardName = request.getParameter("wardName");
    ps.setString(index++, wardName);
    ps.setString(index++, wardName);
    String tpaName = request.getParameter("tpaName");
    ps.setString(index++, tpaName);
    ps.setString(index++, tpaName);

    if (pharAmtTxt != null && !pharAmtTxt.equals("")) {
      ps.setBigDecimal(index++, new BigDecimal(pharAmtTxt));
    }
    if (hospAmtTxt != null && !hospAmtTxt.equals("")) {
      ps.setBigDecimal(index++, new BigDecimal(hospAmtTxt));
    }
    if (totAmtTxt != null && !totAmtTxt.equals("")) {
      ps.setBigDecimal(index++, new BigDecimal(totAmtTxt));
    }

    String[] heading = { "IP Outstanding Report", "" };
    String[] dateHeader = { "Report as of :" + DateUtil.currentDate("dd-MM-yyyy HH:mm:ss"), "" };
    CSVWriter writer = new CSVWriter(response.getWriter(), CSVWriter.DEFAULT_SEPARATOR);
    writer.writeNext(heading);
    writer.writeNext(dateHeader);
    String tpaDisplayName = request.getParameter("tpaDisplayName");    
    if (tpaDisplayName != null && !tpaDisplayName.equals("")) {
      String[] tpaDisplayNamez = { "TPA Name:" + tpaDisplayName, "" };
      writer.writeNext(tpaDisplayNamez);
    }
    ResultSet rs = ps.executeQuery();
    writer.writeAll(rs, true);
    writer.flush();
    writer.close();
    rs.close();
    ps.close();
    con.close();
    return null;

  }

}
