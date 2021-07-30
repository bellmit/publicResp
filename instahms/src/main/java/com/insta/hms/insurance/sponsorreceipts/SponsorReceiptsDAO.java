package com.insta.hms.insurance.sponsorreceipts;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * The Class SponsorReceiptsDAO.
 */
public class SponsorReceiptsDAO extends GenericDAO {

  /** The logger. */
  static Logger logger =
      LoggerFactory.getLogger(SponsorReceiptsDAO.class);

  /**
   * Instantiates a new sponsor receipts DAO.
   */
  public SponsorReceiptsDAO() {
    super("sponsor_receipts");
  }

  /** The Constant SERVER_REQ_FIELDS. */
  /*
   * Search: returns a PagedList suitable for a dashboard type list
   */
  private static final String SERVER_REQ_FIELDS = "SELECT *";

  /** The Constant SERVER_REQ_COUNT. */
  private static final String SERVER_REQ_COUNT = "SELECT count(*)";

  /** The Constant SERVER_REQ_FROM_TABLES. */
  private static final String SERVER_REQ_FROM_TABLES =
      " FROM (select p.mr_no, p.patient_name, cpb.consolidated_bill_no,"
          + " sum(bcc.insurance_claim_amt) AS insurance_claim_amt, "
          + " reced.sponsor_received_amt, MAX(date(cpb.open_date)) AS open_date,"
          + " bcc.sponsor_id, psa.primary_center_id " + " FROM consolidated_patient_bill  cpb "
          + " JOIN bill b on (b.bill_no = cpb.bill_no) "
          + " JOIN patient_registration pr on (b.visit_id = pr.patient_id) "
          + " LEFT JOIN patient_insurance_plans pip on (pr.patient_id = pip.patient_id) "
          + " JOIN bill_charge_claim bcc on "
          + " (cpb.bill_no = bcc.bill_no AND bcc.sponsor_id = pip.sponsor_id) "
          + " JOIN sponsor_approved_charges spc on (bcc.charge_id = spc.charge_id) "
          + " JOIN patient_sponsor_approval_details psad on "
          + " (spc.sponsor_approval_detail_id = psad.sponsor_approval_detail_id) "
          + " JOIN patient_sponsor_approvals psa on"
          + " (psad.sponsor_approval_id = psa.sponsor_approval_id ) "
          + " JOIN patient_details p ON "
          + " (p.mr_no = pr.mr_no AND patient_confidentiality_check(p.patient_group,p.mr_no) )"
          + " LEFT join " + " (select sponsor_id,consolidated_bill_no,"
          + " SUM(sponsor_received_amt) as sponsor_received_amt from "
          + "consolidated_bill_receipts_view GROUP BY sponsor_id,consolidated_bill_no) "
          + " as reced ON (reced.sponsor_id = bcc.sponsor_id AND "
          + " reced.consolidated_bill_no=cpb.consolidated_bill_no) "
          + " WHERE b.status NOT IN('X','C') "
          + "GROUP BY p.mr_no, p.patient_name,cpb.consolidated_bill_no, bcc.sponsor_id,"
          + "reced.sponsor_received_amt,psa.primary_center_id) AS foo";

  /**
   * Gets the sponsor receipts list.
   *
   * @param requestParams the request params
   * @param listingParameter the listing parameter
   * @return the sponsor receipts list
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   */
  public PagedList getSponsorReceiptsList(Map requestParams,
      Map<LISTING, Object> listingParameter) throws ParseException, SQLException {

    Connection con = null;
    SearchQueryBuilder qb = null;
    listingParameter.put(LISTING.PAGESIZE, 0);
    try {
      con = DataBaseUtil.getConnection();
      String initWhere = " WHERE (insurance_claim_amt - sponsor_received_amt) > 0";
      qb = new SearchQueryBuilder(con, SERVER_REQ_FIELDS, SERVER_REQ_COUNT,
          SERVER_REQ_FROM_TABLES, initWhere, listingParameter);
      qb.addFilterFromParamMap(requestParams);
      qb.addSecondarySort("consolidated_bill_no");
      qb.build();

      return qb.getMappedPagedList();
    } finally {
      qb.close();
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant BILL_DETAILS. */
  private static final String BILL_DETAILS = " SELECT b.* FROM bill b WHERE b.bill_no IN"
      + "(SELECT bill_no FROM consolidated_patient_bill WHERE consolidated_bill_no=?)"
      + " ORDER BY b.open_date ";

  /**
   * Gets the bill details.
   *
   * @param consolidatedBillNo the consolidated bill no
   * @return the bill details
   */
  public List<BasicDynaBean> getBillDetails(String consolidatedBillNo) {
    try {
      if (consolidatedBillNo != null && !"".equals(consolidatedBillNo)) {
        return DataBaseUtil.queryToDynaList(BILL_DETAILS, consolidatedBillNo);
      }
    } catch (SQLException sqlException) {
      sqlException.printStackTrace();
    }
    return null;
  }

  /** The Constant BILL_Charge_Claim_DETAILS. */
  private static final String BILL_Charge_Claim_DETAILS =
      " SELECT b.* FROM bill_charge_claim b"
          + " WHERE b.bill_no = ? AND coalesce(b.insurance_claim_amt,0) > 0"
          + " ORDER BY b.charge_id ";

  /**
   * Gets the bill charge claim details.
   *
   * @param billNo the bill no
   * @return the bill charge claim details
   */
  public List<BasicDynaBean> getBillChargeClaimDetails(String billNo) {
    try {
      if (billNo != null && !"".equals(billNo)) {
        return DataBaseUtil.queryToDynaList(BILL_Charge_Claim_DETAILS, billNo);
      }
    } catch (SQLException sqlException) {
      sqlException.printStackTrace();
    }
    return null;
  }
}
