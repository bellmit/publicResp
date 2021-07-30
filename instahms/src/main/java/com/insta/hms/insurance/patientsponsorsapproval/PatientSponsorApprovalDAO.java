package com.insta.hms.insurance.patientsponsorsapproval;

import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.MasterDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

/**
 * The Class PatientSponsorApprovalDAO.
 *
 * @author prasanna.kumar
 */
public class PatientSponsorApprovalDAO extends MasterDAO {

  /**
   * Instantiates a new patient sponsor approval DAO.
   */
  public PatientSponsorApprovalDAO() {
    super("patient_sponsor_approvals", "sponsor_approval_id");
  }

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(PatientSponsorApprovalDAO.class);

  /** The Constant LIST_QUERY_FIELDS. */
  private static final String LIST_QUERY_FIELDS = " SELECT *  ";

  /** The Constant COUNT_QUERY. */
  private static final String COUNT_QUERY = " SELECT count(approval_no) ";

  /** The Constant LIST_QUERY_TABLES. */
  private static final String LIST_QUERY_TABLES = " FROM ( SELECT "
      + " psa.mr_no,psa.sponsor_approval_id,psa.sponsor_id,psa.approval_no,"
      + " psa.validity_start,psa.validity_end, psa.status, "
      + " (case when psa.status = 'A' then 'Active' "
      + " when psa.status = 'X' then 'Cancelled' " + " when psa.status = 'C' then 'Closed' "
      + " end) as psa_status,tm.tpa_name,tm.tpa_id, "
      + " get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name) "
      + " as patient_name " + " FROM patient_sponsor_approvals psa "
      + " JOIN patient_details pd ON (pd.mr_no = psa.mr_no)"
      + " LEFT JOIN tpa_master tm ON (psa.sponsor_id=tm.tpa_id)) as list ";

  /**
   *  Search.
   *
   * @param filter the filter
   * @param listingParams the listing params
   * @return the paged list
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public PagedList search(Map filter, Map<LISTING, Object> listingParams)
      throws SQLException, ParseException {
    String secondarySort = getIdColumnName();
    return super.search(LIST_QUERY_FIELDS, COUNT_QUERY, LIST_QUERY_TABLES, filter,
        listingParams, secondarySort);
  }

}
