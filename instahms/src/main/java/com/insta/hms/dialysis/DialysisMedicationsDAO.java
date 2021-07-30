package com.insta.hms.dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class DialysisMedicationsDAO.
 */
public class DialysisMedicationsDAO {

  /** The Constant ALL_GET_TREATMENTS_DETAILS. */
  private static final String ALL_GET_TREATMENTS_DETAILS = " SELECT prescription_id, tc.mr_no,"
      + " prescribed_date, type, item_id, days, tc.order_id, "
      + " case when tc.type='O' or tc.type='I' or tc.type='N' then item_name when tc.type='M' "
      + "then sid.medicine_name "
      + " end as item_name, medicine_dosage, tc.remarks, freq_type, tc.recurrence_daily_id, "
      + " tc.mod_time, tc.username, discontinued, display_name,"
      + " case when tc.type='O' or tc.type='N' then 'op' else 'item_master' end as master,"
      + " g.generic_name, g.generic_code, cum.consumption_uom, tc.ispackage, mr.route_id,"
      + " mr.route_name, sid.cons_uom_id,"
      + " tc.item_strength, tc.item_form_id, if.item_form_name" + " FROM treatment_chart tc "
      + "   JOIN patient_details pd ON (pd.mr_no = tc.mr_no "
      + "AND patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no))"
      + " LEFT JOIN store_item_details sid ON (tc.type='M' AND tc.item_id=sid.medicine_id::text) "
      + " LEFT JOIN item_form_master if ON (tc.item_form_id=if.item_form_id) "
      + " LEFT JOIN medicine_route mr ON (tc.route_of_admin=mr.route_id) "
      + " LEFT JOIN generic_name g ON (sid.generic_name = g.generic_code) "
      + " LEFT JOIN all_tests_pkgs_view atpv ON (tc.type='T' AND tc.item_id=atpv.test_id)"
      + " LEFT JOIN recurrence_daily_master rdm USING (recurrence_daily_id)"
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = sid.cons_uom_id)"
      + " WHERE tc.mr_no = ? ";

  /** The Constant GET_TREATMENTS_DETAILS. */
  private static final String GET_TREATMENTS_DETAILS = " SELECT prescription_id, tc.mr_no,"
      + " prescribed_date, type, item_id, days, "
      + " case when tc.type='O' or tc.type='I' or tc.type='N' then item_name when tc.type='M' "
      + "then sid.medicine_name "
      + " end as item_name, medicine_dosage, tc.remarks, freq_type, tc.recurrence_daily_id, "
      + " tc.mod_time, tc.username, discontinued, display_name,"
      + " case when tc.type='O' or tc.type='N' then 'op' else 'item_master' end as master,"
      + " g.generic_name, g.generic_code, cum.consumption_uom, tc.ispackage, mr.route_id,"
      + " mr.route_name, sid.cons_uom_id,"
      + " tc.item_strength, tc.item_form_id, if.item_form_name" + " FROM treatment_chart tc "
      + "   JOIN patient_details pd ON (pd.mr_no = tc.mr_no "
      + " AND patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no))"
      + " LEFT JOIN store_item_details sid ON (tc.type='M' AND tc.item_id=sid.medicine_id::text) "
      + " LEFT JOIN item_form_master if ON (tc.item_form_id=if.item_form_id) "
      + " LEFT JOIN medicine_route mr ON (tc.route_of_admin=mr.route_id) "
      + " LEFT JOIN generic_name g ON (sid.generic_name = g.generic_code) "
      + " LEFT JOIN all_tests_pkgs_view atpv ON (tc.type='T' AND tc.item_id=atpv.test_id)"
      + " LEFT JOIN recurrence_daily_master rdm USING (recurrence_daily_id)"
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = sid.cons_uom_id)"
      + " WHERE tc.mr_no = ? AND (tc.visit_id = '' OR tc.visit_id is null) ";

  /** The Constant GET_TREATMENTS_DETAILS_TO_VISIT. */
  private static final String GET_TREATMENTS_DETAILS_TO_VISIT = " SELECT prescription_id,"
      + "  tc.mr_no, prescribed_date, type, item_id, days, "
      + " case when tc.type='O' or tc.type='I' or tc.type='N' then item_name when tc.type='M'"
      + "  then sid.medicine_name "
      + " end as item_name, medicine_dosage, tc.remarks, freq_type, tc.recurrence_daily_id, "
      + " tc.mod_time, tc.username, discontinued, display_name,"
      + " case when tc.type='O' or tc.type='N' then 'op' else 'item_master' end as master,"
      + " g.generic_name, g.generic_code, cum.consumption_uom, tc.ispackage, mr.route_id,"
      + "  mr.route_name, sid.cons_uom_id,"
      + " tc.item_strength, tc.item_form_id, if.item_form_name" + " FROM treatment_chart tc "
      + "   JOIN patient_details pd ON (pd.mr_no = tc.mr_no "
      + " AND patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no))"
      + " LEFT JOIN store_item_details sid ON (tc.type='M' AND tc.item_id=sid.medicine_id::text) "
      + " LEFT JOIN item_form_master if ON (tc.item_form_id=if.item_form_id) "
      + " LEFT JOIN medicine_route mr ON (tc.route_of_admin=mr.route_id) "
      + " LEFT JOIN generic_name g ON (sid.generic_name = g.generic_code) "
      + " LEFT JOIN all_tests_pkgs_view atpv ON (tc.type='T' AND tc.item_id=atpv.test_id)"
      + " LEFT JOIN recurrence_daily_master rdm USING (recurrence_daily_id)"
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = sid.cons_uom_id)"
      + " WHERE tc.visit_id = ?";

  /**
   * Gets the treatment charts.
   *
   * @param mrNo the mr no
   * @param filterType the filter type
   * @param visitId the visit id
   * @return the treatment charts
   * @throws SQLException the SQL exception
   */
  public static List gettreatmentCharts(String mrNo, String filterType, String visitId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      if (filterType == null) {
        ps = con.prepareStatement(ALL_GET_TREATMENTS_DETAILS);
        ps.setString(1, mrNo);
      } else if (filterType.equals("patient")) {
        ps = con.prepareStatement(GET_TREATMENTS_DETAILS);
        ps.setString(1, mrNo);
      } else {
        ps = con.prepareStatement(GET_TREATMENTS_DETAILS_TO_VISIT);
        ps.setString(1, visitId);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the prescriptin details.
   *
   * @param mrNo the mr no
   * @param fromDate the from date
   * @param toDate the to date
   * @return the prescriptin details
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public static List<BasicDynaBean> getPrescriptinDetails(String mrNo, String fromDate,
      String toDate) throws SQLException, ParseException {

    StringBuilder prescriptionDetails = new StringBuilder(ALL_GET_TREATMENTS_DETAILS);
    Connection con = null;
    PreparedStatement pstmt = null;
    prescriptionDetails.append("AND prescribed_date::date >= ? AND prescribed_date::date <= ?");
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(prescriptionDetails.toString());
      pstmt.setString(1, mrNo);
      pstmt.setDate(2, DateUtil.parseDate(fromDate));
      pstmt.setDate(3, DateUtil.parseDate(toDate));
      return DataBaseUtil.queryToDynaList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /**
   * Gets the prescriptin for session.
   *
   * @param mrNo the mr no
   * @param orderId the order id
   * @return the prescriptin for session
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getPrescriptinForSession(String mrNo, int orderId)
      throws SQLException {

    StringBuilder prescriptionDetails = new StringBuilder(ALL_GET_TREATMENTS_DETAILS);
    Connection con = null;
    PreparedStatement pstmt = null;
    prescriptionDetails.append("AND tc.order_id=?");
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(prescriptionDetails.toString());
      pstmt.setString(1, mrNo);
      pstmt.setInt(2, orderId);

      return DataBaseUtil.queryToDynaList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /** The Constant ORDER_ID_FOR_MRNO. */
  private static final String ORDER_ID_FOR_MRNO = "SELECT prescription_id"
      + " FROM services_prescribed"
      + " WHERE mr_no=? AND specialization = 'D' AND conducted != 'X' order by presc_date"
      + "  DESC limit 1";

  /**
   * Gets the order id.
   *
   * @param mrNo the mr no
   * @return the order id
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getOrderId(String mrNo) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getConnection();
      pstmt = con.prepareStatement(ORDER_ID_FOR_MRNO);
      pstmt.setString(1, mrNo);
      return DataBaseUtil.queryToDynaBean(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

}