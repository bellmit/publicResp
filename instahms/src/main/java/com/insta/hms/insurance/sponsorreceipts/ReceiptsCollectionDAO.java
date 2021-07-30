package com.insta.hms.insurance.sponsorreceipts;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;

/**
 * The Class ReceiptsCollectionDAO.
 */
public class ReceiptsCollectionDAO extends GenericDAO {

  /** The logger. */
  static Logger logger =
      LoggerFactory.getLogger(ReceiptsCollectionDAO.class);

  /**
   * Instantiates a new receipts collection DAO.
   */
  public ReceiptsCollectionDAO() {
    super("receipts_collection");
  }

  /** The Constant SERVER_REQ_FIELDS. */
  private static final String SERVER_REQ_FIELDS = "SELECT *";

  /** The Constant SERVER_REQ_COUNT. */
  private static final String SERVER_REQ_COUNT = "SELECT count(*)";

  /** The Constant SERVER_REQ_FROM_TABLES. */
  private static final String SERVER_REQ_FROM_TABLES =
      " FROM (select rc.collection_id, tm.tpa_name, rc.username,"
          + " count(rc.receipt_id) as no_of_receipts," + " SUM(r.amount) as amount, "
          + "date(MAX(r.display_date)) as receipt_date, rc.status"
          + " FROM receipts_collection rc LEFT JOIN "
          + " receipts r on (r.receipt_id = rc.receipt_id)"
          + " LEFT JOIN tpa_master tm on (tm.tpa_id = r.tpa_id)"
          + " GROUP BY rc.collection_id, rc.status,tm.tpa_name,rc.username) AS foo";

  /**
   * Gets the receipt collection list.
   *
   * @param requestParams the request params
   * @param listingParameter the listing parameter
   * @return the receipt collection list
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   */
  public PagedList getReceiptCollectionList(Map requestParams,
      Map<LISTING, Object> listingParameter) throws ParseException, SQLException {

    Connection con = null;
    SearchQueryBuilder qb = null;
    try {
      con = DataBaseUtil.getConnection();
      qb = new SearchQueryBuilder(con, SERVER_REQ_FIELDS, SERVER_REQ_COUNT,
          SERVER_REQ_FROM_TABLES, listingParameter);
      qb.addFilterFromParamMap(requestParams);
      qb.addSecondarySort("collection_id");
      qb.build();

      return qb.getMappedPagedList();
    } finally {
      qb.close();
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant REQ_FIELDS. */
  private static final String REQ_FIELDS = "SELECT *";

  /** The Constant REQ_COUNT. */
  private static final String REQ_COUNT = "SELECT count(*)";

  /** The Constant REQ_FROM_TABLES. */
  private static final String REQ_FROM_TABLES =
      " FROM (select p.mr_no,p.patient_name,to_char(date(b.open_date),'YYYY-MM-DD') as visit_date,"
          + " to_char(date(r.display_date),'YYYY-MM-DD') as receipt_date ,"
          + " r.amount,r.tpa_id,rc.collection_id" + " FROM receipts_collection rc "
          + " LEFT JOIN receipts r on (r.receipt_id = rc.receipt_id)"
          + " LEFT JOIN bill_receipts br on (br.receipt_no = rc.receipt_id)"
          + " LEFT JOIN bill b on (br.bill_no = b.bill_no)"
          + " JOIN patient_registration pr on (b.visit_id = pr.patient_id) "
          + " JOIN patient_details p ON "
          + " (p.mr_no = pr.mr_no AND patient_confidentiality_check(p.patient_group,p.mr_no) )"
          + " ) AS foo";

  /**
   * Gets the receipt collection.
   *
   * @param collectionId the collection id
   * @param listingParameter the listing parameter
   * @return the receipt collection
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   */
  public PagedList getReceiptCollection(Integer collectionId,
      Map<LISTING, Object> listingParameter) throws ParseException, SQLException {
    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder qb = null;
    try {
      String sortOrder = "collection_id";
      Integer pageSize = (Integer) listingParameter.get(LISTING.PAGESIZE);
      Integer pageNum = (Integer) listingParameter.get(LISTING.PAGENUM);
      String pkgid = Integer.toString(collectionId);

      if (pkgid == null || pkgid.equals("")) {
        return new PagedList(new ArrayList(), 0, pageSize, pageNum);
      }

      qb = new SearchQueryBuilder(con, REQ_FIELDS, REQ_COUNT, REQ_FROM_TABLES, null, sortOrder,
          true, pageSize, pageNum);
      qb.addFilter(SearchQueryBuilder.INTEGER, "collection_id", "=", collectionId);
      qb.build();

      return qb.getMappedPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, null);
      if (qb != null) {
        qb.close();
      }
    }

  }

  /** The Constant GET_PATIENTDUE. */
  private static final String GET_PATIENTDUE =
      "SELECT total_amount-total_claim-total_receipts-"
          + "deposit_set_off-points_redeemed_amt " + " FROM bill WHERE bill_no=? ";

  /**
   * Gets the patient due.
   *
   * @param billNo the bill no
   * @return the patient due
   * @throws SQLException the SQL exception
   */
  public BigDecimal getPatientDue(String billNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PATIENTDUE);
      ps.setString(1, billNo);
      return DataBaseUtil.getBigDecimalValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_BILL_SPONSORDUE. */
  private static final String GET_BILL_SPONSORDUE =
      "SELECT total_claim-primary_total_sponsor_receipts-"
          + "secondary_total_sponsor_receipts-claim_recd_amount"
          + " FROM bill WHERE bill_no=? ";

  /**
   * Gets the sponsor due.
   *
   * @param billNo the bill no
   * @return the sponsor due
   * @throws SQLException the SQL exception
   */
  public BigDecimal getSponsorDue(String billNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_BILL_SPONSORDUE);
      ps.setString(1, billNo);
      return DataBaseUtil.getBigDecimalValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
