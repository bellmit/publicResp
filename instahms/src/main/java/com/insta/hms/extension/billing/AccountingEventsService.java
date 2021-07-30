package com.insta.hms.extension.billing;

import com.insta.hms.common.BusinessService;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.SearchQuery;
import com.insta.hms.mdm.accounting.AccountingGroupService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class AccountingEventsService.
 */
@Service
public class AccountingEventsService extends BusinessService {

  /** The accounting group service. */
  @LazyAutowired
  private AccountingGroupService accountingGroupService;

  /** The session service. */
  @LazyAutowired
  SessionService sessionService;

  /** The Constant ACCOUNTING_EVENT_TYPE_MAP. */
  private static final Map<String, String> ACCOUNTING_EVENT_TYPE_MAP = 
      new HashMap<String, String>();

  static {
    ACCOUNTING_EVENT_TYPE_MAP.put("FINALIZE_BILL", "FINALIZED");
    ACCOUNTING_EVENT_TYPE_MAP.put("CLOSE_BILL", "CLOSED");
    ACCOUNTING_EVENT_TYPE_MAP.put("SEND_CLAIM", "CLAIM SENT");
  }

  /** The Constant BILL_FINALIZATION_QUERY. */
  private static final String BILL_FINALIZATION_QUERY = "FROM (SELECT 'BILL'::text as entity_name,"
      + " bill_no as entity_id, "
      + " finalized_date as event_date_time, 'FINALIZED'::text as event_name "
      + " FROM bill) AS FOO ";

  /** The Constant BILL_CLOSURE_QUERY. */
  private static final String BILL_CLOSURE_QUERY = "FROM (SELECT 'BILL'::text as entity_name, "
      + " coalesce(bill.account_group, 1) as d_account_group, "
      + " bill_no as entity_id "
      + " closed_date as event_date_time, 'FINALIZED'::text as event_name " + " FROM bill) AS FOO ";

  /** The Constant CLAIM_SENT_QUERY. */
  private static final String CLAIM_SENT_QUERY = "FROM (SELECT 'BILL'::text as entity_name, "
      + " coalesce(bill.account_group, 1) as d_account_group, "
      + " bill_no as entity_id "
      + " finalized_date as event_date_time, 'FINALIZED'::text as event_name "
      + " FROM bill) AS FOO ";

  /**
   * Search.
   *
   * @param filterParams
   *          the filter params
   * @return the paged list
   */
  public PagedList search(Map<String, String[]> filterParams) {
    Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(filterParams);
    listingParams.put(LISTING.PAGESIZE, 2000);
    return this.search(filterParams, listingParams);
  }

  /**
   * Search.
   *
   * @param filterParams
   *          the filter params
   * @param listingParameter
   *          the listing parameter
   * @return the paged list
   */
  private PagedList search(Map<String, String[]> filterParams,
      Map<LISTING, Object> listingParameter) {
    SearchQueryAssembler qb = getSearchQueryAssembler(filterParams, listingParameter);
    PagedList list = null;
    if (null != qb) {
      qb.build();
      list = qb.getDynaPagedList();
    }
    return list;
  }

  /**
   * Gets the search query assembler.
   *
   * @param params
   *          the params
   * @param listingParams
   *          the listing params
   * @return the search query assembler
   */
  protected SearchQueryAssembler getSearchQueryAssembler(Map params,
      Map<LISTING, Object> listingParams) {

    SearchQuery query = getSearchQuery(params);
    if (null == query) {
      return null;
    }
    SearchQueryAssembler qb = new SearchQueryAssembler(query.getFieldList(), query.getCountQuery(),
        query.getSelectTables(), listingParams);
    if (null != params) {
      qb.addFilterFromParamMap(params);
    }

    String secondarySortCol = query.getSecondarySortColumn();
    if (null != secondarySortCol) {
      qb.addSecondarySort(secondarySortCol);
    }
    return qb;
  }

  /**
   * Gets the search query.
   *
   * @param params
   *          the params
   * @return the search query
   */
  private SearchQuery getSearchQuery(Map params) {
    SearchQuery searchQry = null;
    String qry = BILL_FINALIZATION_QUERY;
    String[] eventType = (String[]) params.get("_event_type");
    if (null != eventType && eventType.length > 0 && !eventType[0].trim().isEmpty()) {
      String type = eventType[0];
      if ("FINALIZE_BILL".equals(type)) {
        qry = BILL_FINALIZATION_QUERY;
      } else if ("CLOSE_BILL".equals(type)) {
        qry = BILL_CLOSURE_QUERY;
      } else if ("SEND_CLAIM".equals(type)) {
        qry = CLAIM_SENT_QUERY;
      }
    }
    searchQry = new SearchQuery(qry);
    searchQry.setSecondarySortColumn("event_name");
    return searchQry;
  }

  /**
   * Gets the event types.
   *
   * @return the event types
   */
  public Map<String, String> getEventTypes() {
    return Collections.unmodifiableMap(ACCOUNTING_EVENT_TYPE_MAP);
  }

  /**
   * Gets the account groups.
   *
   * @return the account groups
   */
  public List<BasicDynaBean> getAccountGroups() {
    Map httpSession = sessionService.getSessionAttributes();
    Integer centerId = (null != httpSession) ? (Integer) httpSession.get("centerId") : null;
    return accountingGroupService.getAccountingGroups(centerId);
  }
}
