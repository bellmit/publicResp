package com.insta.hms.extension.billing;

import com.bob.hms.common.RequestContext;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class AccountingService.
 */
@Service
public class AccountingService extends BusinessService {

  /** The accounting repository. */
  @LazyAutowired
  AccountingRepository accountingRepository;

  /** The exporters. */
  @LazyAutowired
  List<AccountingExportService> exporters;

  /** The accounting group service. */
  @LazyAutowired
  private AccountingGroupService accountingGroupService;

  /** The session service. */
  @LazyAutowired
  SessionService sessionService;

  // This is right now a static hard-coded list of voucher types. We need to
  /** The Constant VOUCHER_TYPE_MAP. */
  // find a way of extending this
  private static final Map<String, String> VOUCHER_TYPE_MAP = new HashMap<String, String>();

  static {
    VOUCHER_TYPE_MAP.put("CSISSUE", "Consignment Issues");
    VOUCHER_TYPE_MAP.put("HOSPBILLS", "Hospital Bills");
    VOUCHER_TYPE_MAP.put("Pharmacy Discounts", "Pharmacy Discounts");
    VOUCHER_TYPE_MAP.put("INVDISC", "Supplier Discounts");
    VOUCHER_TYPE_MAP.put("INVTRANS", "Inventory Transactions");
    VOUCHER_TYPE_MAP.put("PAYMENT", "Payment");
    VOUCHER_TYPE_MAP.put("PAYMENTDUE", "Payments Due");
    VOUCHER_TYPE_MAP.put("PHBILLS", "Pharmacy Bills");
    VOUCHER_TYPE_MAP.put("PURCHASE", "Store Purchases");
    VOUCHER_TYPE_MAP.put("RECEIPT", "Receipts");
    VOUCHER_TYPE_MAP.put("STKCONSUMPTION", "Stock Consumption");
    VOUCHER_TYPE_MAP.put("STOCKTRANSFER", "Stock Transfer");
    VOUCHER_TYPE_MAP.put("STORERETURNS", "Store Returns");
    VOUCHER_TYPE_MAP.put("USERISSUE", "User / Dept Issues");
  }

  /**
   * Search.
   *
   * @param filterParams
   *          the filter params
   * @return the paged list
   */
  public PagedList search(Map<String, String[]> filterParams) {
    Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(filterParams);
    if (!filterParams.containsKey("pageSize") && !filterParams.containsKey("page_size")) {
      listingParams.put(LISTING.PAGESIZE, 2000);
    }
    listingParams.put(LISTING.SORTCOL, "created_at");
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
    return this.search(filterParams, listingParameter, false);
  }

  /**
   * Search.
   *
   * @param filterParams
   *          the filter params
   * @param listingParameter
   *          the listing parameter
   * @param filterByLoggedInCenter
   *          the filter by logged in center
   * @return the paged list
   */
  private PagedList search(Map<String, String[]> filterParams,
      Map<LISTING, Object> listingParameter, boolean filterByLoggedInCenter) {
    // Map<String, List<BasicDynaBean>> result = new HashMap<String,
    // List<BasicDynaBean>>();
    SearchQueryAssembler qb = getSearchQueryAssembler(filterParams, listingParameter);
    Integer loggedInCenter = getLoggedInCenter();
    PagedList list = null;
    if (null != qb) {
      if (filterByLoggedInCenter && !loggedInCenter.equals(0)) {
        qb.addFilter(SearchQueryAssembler.INTEGER, "center_id", "=", loggedInCenter);
      }
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

    SearchQuery query = accountingRepository.getSearchQuery();
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
      // TODO : provide for multiple secondary sort columns in
      // SearchQuery() object
      qb.addSecondarySort("guid");
    }
    return qb;
  }

  /**
   * Gets the logged in center.
   *
   * @return the logged in center
   */
  private Integer getLoggedInCenter() {
    return RequestContext.getCenterId();
  }

  /**
   * Export.
   *
   * @param reqParams
   *          the req params
   * @param target
   *          the target
   * @return the list
   * @throws Exception
   *           the exception
   */
  public List<BasicDynaBean> export(Map<String, String[]> reqParams, String target)
      throws Exception {
    AccountingExportService service = getAccountingExportService(target);
    if (null != service) {
      Integer centerId = 0;
      Integer accountGroupId = 1;
      String[] accountGroups = reqParams.get("d_account_group");
      String[] guids = reqParams.get("guid");
      String[] centerIds = reqParams.get("center_id");
      if (null != centerIds && centerIds.length > 0) {
        centerId = Integer.valueOf(centerIds[0]);
      }
      if (null != accountGroups && accountGroups.length > 0) {
        accountGroupId = Integer.valueOf(accountGroups[0]);
      }
      List<BasicDynaBean> records = accountingRepository.getByGuid(guids);
      if (null != records && records.size() > 0) {
        Map<String, Map<String, Object>> recordMap = 
            (Map<String, Map<String, Object>>) ConversionUtils.listBeanToMapMap(records, "guid");
        Integer exportId = service.export(recordMap, centerId, accountGroupId);
        if (null != exportId) {
          return updateExportStatus(exportId);
        }
      }
    }
    return null;
  }

  /**
   * Update export status.
   *
   * @param exportId
   *          the export id
   * @return the list
   */
  private List<BasicDynaBean> updateExportStatus(Integer exportId) {
    if (accountingRepository.updateExportStatus(exportId)) {
      return accountingRepository.getExportStatus(exportId);
    }
    return null;
  }

  /**
   * Gets the accounting export service.
   *
   * @param target
   *          the target
   * @return the accounting export service
   */
  private AccountingExportService getAccountingExportService(String target) {
    for (AccountingExportService exporter : exporters) {
      if (exporter.supportsTarget(target)) {
        return exporter;
      }
    }
    return null;
  }

  /**
   * Gets the search summary.
   *
   * @param filter
   *          the filter
   * @return the search summary
   */
  public Map<String, Object> getSearchSummary(Map<String, String[]> filter) {
    List<BasicDynaBean> credits = accountingRepository.getSummaryCredits(filter);
    List<BasicDynaBean> debits = accountingRepository.getSummaryDebits(filter);
    return toSummaryMap(credits, debits);
  }

  /**
   * To summary map.
   *
   * @param credits
   *          the credits
   * @param debits
   *          the debits
   * @return the map
   */
  private Map<String, Object> toSummaryMap(List<BasicDynaBean> credits,
      List<BasicDynaBean> debits) {
    Map<String, Map<String, Map<String, Object>>> creditsMap = ConversionUtils
        .listBeanToMapMapMap(credits, "voucher_type", "credit_account");
    Map<String, Map<String, Map<String, Object>>> debitsMap = ConversionUtils
        .listBeanToMapMapMap(debits, "voucher_type", "debit_account");
    Map<String, Object> summaryMap = new HashMap<String, Object>();
    Set<String> voucherTypes = mergeKeys(creditsMap, debitsMap);
    Map<String, Map<String, Map<String, Object>>> summaryLines = null;
    for (String type : voucherTypes) {
      summaryLines = new HashMap<String, Map<String, Map<String, Object>>>();
      Map<String, Map<String, Object>> creditAccountsMap = creditsMap.get(type);
      Map<String, Map<String, Object>> debitAccountsMap = debitsMap.get(type);
      Set<String> accounts = mergeKeys(creditAccountsMap, debitAccountsMap);
      for (String account : accounts) {
        Map<String, Map<String, Object>> beanMap = new HashMap<String, Map<String, Object>>();
        beanMap.put("creditbean", creditAccountsMap.get(account));
        beanMap.put("debitbean", debitAccountsMap.get(account));
        summaryLines.put(account, beanMap);
      }
      summaryMap.put(type, summaryLines);
    }
    return summaryMap;
  }

  /**
   * Merge keys.
   *
   * @param map
   *          the map
   * @param otherMaps
   *          the other maps
   * @return the sets the
   */
  private Set<String> mergeKeys(Map<String, ?> map, Map... otherMaps) {
    Set<String> mergedKeySet = new HashSet<String>();
    if (null != map) {
      mergedKeySet.addAll(map.keySet());
      for (Map otherMap : otherMaps) {
        if (null != otherMap) {
          mergedKeySet.addAll(otherMap.keySet());
        }
      }
    }
    return mergedKeySet;
  }

  /**
   * Gets the voucher types.
   *
   * @return the voucher types
   */
  public Map<String, String> getVoucherTypes() {
    return Collections.unmodifiableMap(VOUCHER_TYPE_MAP);
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

  /**
   * Update voucher status.
   *
   * @param guid
   *          the guid
   * @param status
   *          the status
   * @return true, if successful
   */
  public boolean updateVoucherStatus(String guid, Integer status) {
    if (null != guid && null != status && !guid.trim().isEmpty() 
        && (status == -1 || status == 1)) { // only -1 and 1 are allowed
      BasicDynaBean bean = accountingRepository.getBean();
      Map keys = new HashMap();
      keys.put("guid", guid);
      bean.set("update_status", status);
      return accountingRepository.update(bean, keys) > 0;
    }
    return false;
  }
}
