package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PharmacyItemsCreditBillDAO {

  private PharmacyItemsCreditBillDAO() {
    // TODO Auto-generated constructor stub
  }

  public static final String GET_CREDIT_BILLFIELDS = " SELECT *";

  private static final String GET_CREDIT_BILLFIELDS_COUNT = "SELECT count(*) ";

  private static final String GET_CREDIT_BILLFIELDS_TABLES = "FROM (select distinct bill_no,b.visit_id,mr_no,"
      + " get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name) as patient_name,b.status "
      + " from bill b" + " join bill_charge bc using(bill_no)"
      + " JOIN patient_registration pr ON (pr.patient_id = b.visit_id)"
      + " JOIN patient_details pd using (mr_no) " + " where bc.charge_head in ('PHCMED','PHCRET') "
      + " AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) ) "
      + " order by bill_no) AS FOO";

  public static PagedList getCreditBillList(Map filter, Map listing)
      throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();

    SearchQueryBuilder qb = new SearchQueryBuilder(con, GET_CREDIT_BILLFIELDS,
        GET_CREDIT_BILLFIELDS_COUNT, GET_CREDIT_BILLFIELDS_TABLES, listing);

    qb.addFilterFromParamMap(filter);
    qb.addSecondarySort("bill_no");
    qb.build();

    PagedList l = qb.getMappedPagedList();

    qb.close();
    con.close();

    return l;
  }

  private static final String GET_BILL_DETAILS = " select bill_no, posted_date, chargehead_name, "
      + "act_description, act_remarks,act_rate, act_quantity, discount, (amount+tax_amt) as amount, (insurance_claim_amount+sponsor_tax_amt) as insurance_claim_amount "
      + "FROM bill_charge " + "JOIN chargehead_constants on chargehead_id = charge_head "
      + "WHERE charge_head in ('PHCMED','PHCRET') and bill_no=?";

  public static List<BasicDynaBean> getItemsList(String billNo) throws SQLException {
    List<BasicDynaBean> salesList = null;
    Connection c = null;
    PreparedStatement ps = null;
    try {
      c = DataBaseUtil.getReadOnlyConnection();
      ps = c.prepareStatement(GET_BILL_DETAILS);
      ps.setString(1, billNo);
      salesList = DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (c != null) {c.close();}
    }
    return salesList;
  }

  private static final String GET_BILL_DETAILS_FOR_HOSP = " select bill_no, posted_date,act_description,act_remarks,act_rate,"
      + " act_quantity,discount,amount" + " from bill_charge where bill_no=?";

  public static List<BasicDynaBean> getItemsListForHosp(String billNo) throws SQLException {
    List<BasicDynaBean> salesList = null;
    Connection c = null;
    PreparedStatement ps = null;
    try {
      c = DataBaseUtil.getReadOnlyConnection();
      ps = c.prepareStatement(GET_BILL_DETAILS_FOR_HOSP);
      ps.setString(1, billNo);
      salesList = DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (c != null) {c.close();}
    }
    return salesList;
  }

  private static final String GET_PATIENT_VISIT_DETAILS = " select pr.patient_id as customer_id,(patient_name || last_name) as customer_name, "
      + " patient_phone as phone_no from patient_registration pr join patient_details pd using (mr_no) where pr.patient_id=?";

  public static BasicDynaBean getPatientVisitDetailsBean(String patientId)
      throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_PATIENT_VISIT_DETAILS);
      ps.setString(1, patientId);

      List list = DataBaseUtil.queryToDynaList(ps);
      if (!list.isEmpty()) {
        return (BasicDynaBean) list.get(0);
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }
  public static boolean updateDepositSetOffInBill(Connection con, String billNo,
      String depositSetOff) throws SQLException {
    boolean success = true;
    PreparedStatement ps = null;
    try {
      BillDAO bdao = new BillDAO(con);
      Bill billObj = bdao.getBill(billNo);
      ps = con.prepareStatement("SELECT COALESCE(deposit_set_off,0) FROM bill WHERE bill_no=?");
      ps.setString(1, billNo);
      BigDecimal prevDeposits = DataBaseUtil.getBigDecimalValueFromDb(ps);
      if (depositSetOff != null && !depositSetOff.trim().equalsIgnoreCase("")) {
        billObj.setDepositSetOff(new BigDecimal(depositSetOff).add(prevDeposits));
      }
      billObj.setModTime(new Date());
      bdao.updateBill(billObj);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
    return success;
  }
}
