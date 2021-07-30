package com.insta.hms.insurance.patientsponsorsapproval;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.insurance.DavitaSponsorDAO;
import com.insta.hms.master.MasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class SponsorApprovalDAO.
 *
 * @author prasanna.kumar
 */
public class SponsorApprovalDAO extends MasterDAO {

  /** The pat reg dao. */
  GenericDAO patRegDao = new GenericDAO("patient_registration");

  /** The dav spon dao. */
  DavitaSponsorDAO davSponDao = new DavitaSponsorDAO();

  /**
   * Instantiates a new sponsor approval DAO.
   */
  public SponsorApprovalDAO() {
    super("patient_sponsor_approvals", "sponsor_approval_id");
  }

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(SponsorApprovalDAO.class);


  /** The Constant LIST_QUERY_FIELDS. */
  private static final String LIST_QUERY_FIELDS = " SELECT *  ";

  /** The Constant COUNT_QUERY. */
  private static final String COUNT_QUERY = " SELECT count(approval_no) ";

  /** The Constant LIST_QUERY_TABLES. */
  private static final String LIST_QUERY_TABLES =
      " FROM ( SELECT " + " psa.mr_no,psa.sponsor_approval_id,psa.sponsor_id,psa.approval_no,"
          + " psa.validity_start,psa.validity_end,psa.approval_status,psa.status, "
          + " (case when psa.status = 'A' AND psa.approval_status='Y' THEN 'A' "
          + " when psa.status = 'A' AND psa.approval_status='N' THEN 'P' "
          + " when psa.status = 'I' then 'I' " + " when psa.status = 'C' then 'C' "
          + " end) as combine_flag_status, " + " tm.tpa_name,tm.tpa_id, "
          + " get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name) "
          + " as patient_name ,approved_by " + " FROM patient_sponsor_approvals psa "
          + " JOIN patient_details pd ON (pd.mr_no = psa.mr_no)"
          + " LEFT JOIN tpa_master tm ON (psa.sponsor_id=tm.tpa_id)) as list ";

  /**
   * Search.
   * @param filter the filter
   * @param listingParams the listing params
   */
  public PagedList search(Map filter, Map<LISTING, Object> listingParams)
      throws SQLException, ParseException {
    String secondarySort = getIdColumnName();
    return super.search(LIST_QUERY_FIELDS, COUNT_QUERY, LIST_QUERY_TABLES, filter,
        listingParams, secondarySort);
  }

  /** The Constant GET_ALL_SPONSOR_APPROVALS. */
  private static final String GET_ALL_SPONSOR_APPROVALS =
      " SELECT psa.sponsor_approval_id,psa.sponsor_id, "
          + " psa.approval_no,psa.validity_start,psa.validity_end,psa.primary_center_id,"
          + " psa.priority,psa.status,tm.tpa_name " + " FROM patient_sponsor_approvals psa "
          + " JOIN tpa_master tm ON (tm.tpa_id=psa.sponsor_id) WHERE mr_no=? ORDER BY priority ";

  /**
   * Gets the sponsor approvals.
   *
   * @param mrNo the mr no
   * @return the sponsor approvals
   * @throws SQLException the SQL exception
   */
  public List getSponsorApprovals(String mrNo) throws SQLException {
    PreparedStatement ps = null;
    List sponsorApprovals = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();;
      ps = con.prepareStatement(GET_ALL_SPONSOR_APPROVALS);
      ps.setString(1, mrNo);
      sponsorApprovals = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return sponsorApprovals;
  }

  /** The Constant GET_PRIMARY_CENTER. */
  private static final String GET_PRIMARY_CENTER =
      " SELECT primary_center_id " + " FROM patient_sponsor_approvals WHERE mr_no=? "
          + "ORDER BY sponsor_approval_id DESC LIMIT 1 ";

  /**
   * Gets the primary center.
   *
   * @param mrNo the mr no
   * @return the primary center
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getPrimaryCenter(String mrNo) throws SQLException {
    PreparedStatement ps = null;
    BasicDynaBean primaryCenter = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();;
      ps = con.prepareStatement(GET_PRIMARY_CENTER);
      ps.setString(1, mrNo);
      primaryCenter = DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return primaryCenter;
  }

  /** The Constant GET_RATE_PLAN_LIST. */
  private static final String GET_RATE_PLAN_LIST =
      " SELECT od.org_name,psa.org_id,od.status " + " FROM patient_sponsor_approvals psa "
          + " JOIN organization_details od ON(psa.org_id = od.org_id) "
          + " WHERE psa.sponsor_approval_id = ? " + " UNION ALL "
          + " SELECT org_name,org_id,status from organization_details "
          + " WHERE status='A' AND ( (has_date_validity AND "
          + " current_date BETWEEN valid_from_date AND valid_to_date ) OR"
          + " (NOT has_date_validity)) " + " ORDER BY org_name ";

  /**
   * Gets the rate plan list.
   *
   * @param sponsorApprovalId the sponsor approval id
   * @return the rate plan list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getRatePlanList(String sponsorApprovalId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_RATE_PLAN_LIST);
      ps.setInt(1, Integer.parseInt(sponsorApprovalId));
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Process previous months orders.
   *
   * @param mrNo the mr no
   * @param startDate the start date
   * @param endDate the end date
   * @return Set of bill numbers
   * @throws Exception the exception
   */
  public Set<String> processPreviousMonthsOrders(String mrNo, java.sql.Date startDate,
      java.sql.Date endDate) throws Exception {

    Connection con = null;
    boolean success = true;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      VisitDetailsDAO visitDetDao = new VisitDetailsDAO();
      Set<String> billNos =  new HashSet<>();
      List<BasicDynaBean> mainVisitsBeanList =
          visitDetDao.getAllMainVisitsOfPatient(con, mrNo, startDate, endDate);
      for (BasicDynaBean mainVisitBean : mainVisitsBeanList) {
        billNos.addAll(new DavitaSponsorDAO().recalculatePreviousVisitItems(con,
            (String) mainVisitBean.get("main_visit_id")));
        new DavitaSponsorDAO().calculate(con, mainVisitBean);
      }
      return billNos;

    } catch (Exception exception) {
      success = false;
      throw exception;
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
  }

  /**
   * Process and calculate previous month orders.
   *
   * @param mrNo the mr no
   * @param startDate the start date
   * @param endDate the end date
   * @return Map of new bill_no created
   * @throws Exception the exception
   */
  public Set<String> processAndCalculatePreviousMonthOrders(String mrNo, Date startDate,
      Date endDate) throws Exception {
    // TODO Auto-generated method stub
    // steps : get the charges of non tpa bills
    // create the corresponding new bills and insert the charges
    // insert patient insurance plans records
    // create the credit notes for old bills

    Connection con = null;
    boolean success = true;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      VisitDetailsDAO visitDetDao = new VisitDetailsDAO();
      List<BasicDynaBean> mainVisitsBeanList =
          visitDetDao.getAllMainVisitsOfPatient(con, mrNo, startDate, endDate);
      Set<String> newBillNos = new HashSet<>();
      for (BasicDynaBean mainVisitBean : mainVisitsBeanList) {
        newBillNos.addAll(davSponDao.processAndCalculatePrevMonth(con, 
            (String) mainVisitBean.get("main_visit_id")));
        // new DavitaSponsorDAO().recalculatePreviousVisitItems(con ,
        // (String)mainVisitBean.get("main_visit_id"));
        // new DavitaSponsorDAO().calculate(con , mainVisitBean);
      }
      return newBillNos;

    } catch (Exception exception) {
      success = false;
      throw exception;
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

  }

  private static final String GET_ALL_PREVIOUS_MONTH_BILLS = ""
      + " SELECT bill_no  "
      + " FROM bill b  "
      + "          join patient_registration pr on pr.patient_id = b.visit_id  "
      + " WHERE mr_no = ?  "
      + "   AND reg_date >= ?  "
      + "   AND reg_date <= ? ";
  private final AllocationService allocationService = (AllocationService) ApplicationContextProvider
      .getApplicationContext().getBean("allocationService");

  /**
   * Process previous months bill by sending it to allocation service.
   *
   * @param newBillNos set of new bill numbers
   */
  public void processAllPreviousMonthBills(Set<String> newBillNos) {
    for (String billNo : newBillNos) {
      allocationService.allocate(billNo, RequestContext.getCenterId());
    }
  }

  /**
   * getAllPreviousMonthBills.
   * 
   * @param con the connection
   * @param mrNo the mr no
   * @param startDate the start date
   * @param endDate the end date
   * @return the list of bill from the previous month for the given mr no
   * @throws SQLException the exception
   */
  public List<BasicDynaBean> getAllPreviousMonthBills(Connection con, String mrNo,
      java.sql.Date startDate, java.sql.Date endDate) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_ALL_PREVIOUS_MONTH_BILLS)) {
      ps.setString(1, mrNo);
      ps.setDate(2, startDate);
      ps.setDate(3, endDate);
      return DataBaseUtil.queryToDynaList(ps);
    }
  }


}
