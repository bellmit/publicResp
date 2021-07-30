package com.insta.hms.extension.billing;

import com.insta.hms.common.BusinessRepository;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.StringUtil;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class AccountingRepository.
 */
@Repository
public class AccountingRepository extends BusinessRepository {

  /** The default search query. */
  private static final String DEFAULT_SEARCH_QUERY = " FROM (SELECT hai.*, "
      + " coalesce(hai.account_group, 1) as d_account_group from hms_accounting_info hai ) AS FOO";

  /**
   * Instantiates a new accounting repository.
   */
  public AccountingRepository() {
    super("hms_accounting_info");
  }

  /**
   * Gets the search query.
   *
   * @return the search query
   */
  public SearchQuery getSearchQuery() {
    SearchQuery qry = new SearchQuery(DEFAULT_SEARCH_QUERY);
    qry.setSecondarySortColumn("voucher_type");
    return qry;
  }

  /** The Constant SUMMARY_CREDIT_QUERY_FIELDS. */
  private static final String SUMMARY_CREDIT_QUERY_FIELDS = "SELECT voucher_type, credit_account, "
      + " sum(gross_amount) as gross_amount, sum(net_amount) as net_amount ";

  /** The Constant SUMMARY_CREDIT_QUERY_TABLES. */
  private static final String SUMMARY_CREDIT_QUERY_TABLES = "FROM "
      + " (SELECT voucher_date, voucher_type, account_group, "
      + " coalesce(account_group, 1) as d_account_group, "
      + " center_id, update_status, credit_account, "
      + " coalesce(gross_amount, 0) as gross_amount, " + " coalesce(net_amount, 0) as net_amount "
      + " FROM hms_accounting_info ) " + " AS FOO ";

  /** The Constant SUMMARY_CREDIT_GROUPBY_LIST. */
  private static final String SUMMARY_CREDIT_GROUPBY_LIST = " voucher_type, credit_account ";

  /**
   * Gets the summary credits.
   *
   * @param filter
   *          the filter
   * @return the summary credits
   */
  public List<BasicDynaBean> getSummaryCredits(Map<String, String[]> filter) {
    SearchQueryAssembler builder = new SearchQueryAssembler(SUMMARY_CREDIT_QUERY_FIELDS, null,
        SUMMARY_CREDIT_QUERY_TABLES, null, SUMMARY_CREDIT_GROUPBY_LIST, "voucher_type", false, 
        2000,1);
    if (null != filter) {
      builder.addFilterFromParamMap(filter);
    }
    builder.build();
    PagedList pagedList = builder.getDynaPagedList();
    if (null != pagedList) {
      return pagedList.getDtoList();
    }
    return null;
  }

  /** The Constant SUMMARY_DEBIT_QUERY_FIELDS. */
  private static final String SUMMARY_DEBIT_QUERY_FIELDS = "SELECT voucher_type, debit_account, "
      + " sum(gross_amount) as gross_amount, " + " sum(net_amount) as net_amount ";

  /** The Constant SUMMARY_DEBIT_QUERY_TABLES. */
  private static final String SUMMARY_DEBIT_QUERY_TABLES = "FROM "
      + " (SELECT voucher_date, voucher_type, account_group, "
      + " coalesce(account_group, 1) as d_account_group, "
      + " center_id, update_status, debit_account, "
      + " coalesce(gross_amount, 0) as gross_amount, " + " coalesce(net_amount, 0) as net_amount "
      + " FROM hms_accounting_info ) " + " AS FOO ";

  /** The Constant SUMMARY_DEBIT_GROUPBY_LIST. */
  private static final String SUMMARY_DEBIT_GROUPBY_LIST = " voucher_type, debit_account ";

  /**
   * Gets the summary debits.
   *
   * @param filter
   *          the filter
   * @return the summary debits
   */
  public List<BasicDynaBean> getSummaryDebits(Map<String, String[]> filter) {
    SearchQueryAssembler builder = new SearchQueryAssembler(SUMMARY_DEBIT_QUERY_FIELDS, null,
        SUMMARY_DEBIT_QUERY_TABLES, null, SUMMARY_DEBIT_GROUPBY_LIST, "voucher_type", false, 2000,
        1);

    if (null != filter) {
      builder.addFilterFromParamMap(filter);
    }

    builder.build();
    PagedList pagedList = builder.getDynaPagedList();
    if (null != pagedList) {
      List<BasicDynaBean> debits = pagedList.getDtoList();
      return debits;
    }
    return null;
  }

  /** The Constant UPDATE_EXPORT_STATUS. */
  private static final String UPDATE_EXPORT_STATUS = "UPDATE hms_accounting_info hai "
      + " SET update_status = vl.status "
      + " FROM accounting_export_voucher as vl "
      + " WHERE vl.guid = hai.guid AND vl.export_id = ? ";

  /**
   * Update export status.
   *
   * @param exportId
   *          the export id
   * @return true, if successful
   */
  public boolean updateExportStatus(Integer exportId) {
    int updateCount = 0;

    if (null != exportId) {
      updateCount = DatabaseHelper.update(UPDATE_EXPORT_STATUS, new Object[] { exportId });
    }

    return updateCount > 0;
  }

  /** The Constant VOUCHERS_BY_GUID. */
  private static final String VOUCHERS_BY_GUID = "SELECT * from hms_accounting_info "
      + " WHERE guid = ANY (VALUES ('#valueList')) ORDER BY voucher_date, voucher_type, "
      + " voucher_no";

  /**
   * Gets the by guid.
   *
   * @param guids
   *          the guids
   * @return the by guid
   */
  public List<BasicDynaBean> getByGuid(String[] guids) {
    if (null != guids && guids.length > 0) {
      // array[] = {"ABC", "XYZ"} will be converted to "('ABC'),('XYZ')"
      String guidList = StringUtil.join(guids, "'),('"); 
      // TODO : Use prepared statement
      String qry = VOUCHERS_BY_GUID.replace("#valueList", guidList);
      return DatabaseHelper.queryToDynaList(qry);
    }
    return null;
  }

  /** The Constant EXPORT_STATUS_QUERY. */
  public static final String EXPORT_STATUS_QUERY = "SELECT aej.export_id, aej.status, "
      + " COUNT(aej.journal_id) as journal_count"
      + " FROM accounting_export_journal aej "
      + " JOIN accounting_export_log ael ON ael.export_id = aej.export_id"
      + " WHERE ael.export_id = ? " + " GROUP BY aej.export_id, aej.status";

  /**
   * Gets the export status.
   *
   * @param exportId
   *          the export id
   * @return the export status
   */
  public List<BasicDynaBean> getExportStatus(Integer exportId) {
    return DatabaseHelper.queryToDynaList(EXPORT_STATUS_QUERY, new Object[] { exportId });
  }
}
