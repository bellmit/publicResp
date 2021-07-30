package com.insta.hms.api.services;

import com.insta.hms.api.repositories.AccountingHeadsListRepository;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * The Class AccountingHeadsListService.
 */
@Service
public class AccountingHeadsListService {

  /** The accounting heads list repository. */
  @Autowired
  private AccountingHeadsListRepository accountingHeadsListRepository;

  /**
   * Gets the accounting heads names.
   *
   * @param source
   *          the source
   * @param filterParams
   *          the filter params
   * @return the accounting heads names
   */
  @SuppressWarnings("unchecked")
  public List<BasicDynaBean> getAccountingHeadsNames(String source,
      Map<String, String[]> filterParams) {
    Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(filterParams);
    if (!filterParams.containsKey("pageSize") && !filterParams.containsKey("page_size")) {
      listingParams.put(LISTING.PAGESIZE, 20);
    }
    listingParams.put(LISTING.SORTCOL, "account_head_name");
    PagedList resultPagedList = getAccountingHeadsPagedList(source, listingParams);
    return resultPagedList.getDtoList();
  }

  /**
   * Gets the accounting heads paged list.
   *
   * @param source
   *          the source
   * @param listingParams
   *          the listing params
   * @return the accounting heads paged list
   */
  private PagedList getAccountingHeadsPagedList(String source, Map<LISTING, Object> listingParams) {
    SearchQuery query = new SearchQuery(getAccountingHeadsQuery(source));
    if (query.getSelectTables() == null) {
      return new PagedList();
    }
    SearchQueryAssembler qb = new SearchQueryAssembler(query.getFieldList(), query.getCountQuery(),
        query.getSelectTables(), listingParams);
    qb.build();
    return qb.getDynaPagedList();
  }

  /**
   * Gets the accounting heads query.
   *
   * @param source
   *          the source
   * @return the accounting heads query
   */
  private String getAccountingHeadsQuery(String source) {
    String accountHeadsQuery = null;
    switch (source) {
      case "bill_account_heads":
        accountHeadsQuery = accountingHeadsListRepository.getBillAccountHeadsNamesQuery();
        break;
      case "tpa_master":
        accountHeadsQuery = accountingHeadsListRepository.getTpaNamesQuery();
        break;
      case "payment_mode_master":
        accountHeadsQuery = accountingHeadsListRepository.getPaymentModeNamesQuery();
        break;
      case "doctors":
        accountHeadsQuery = accountingHeadsListRepository.getDoctorNamesQuery();
        break;
      case "referral":
        accountHeadsQuery = accountingHeadsListRepository.getReferralNamesQuery();
        break;
      case "supplier_master":
        accountHeadsQuery = accountingHeadsListRepository.getSupplierNamesQuery();
        break;
      case "payments_details":
        accountHeadsQuery = accountingHeadsListRepository.getPaymentsDetailsNamesQuery();
        break;
      case "payments":
        accountHeadsQuery = accountingHeadsListRepository.getPaymentsNamesQuery();
        break;
      case "outhouse_master":
        accountHeadsQuery = accountingHeadsListRepository.getOuthouseNamesQuery();
        break;
      default:
        break;
    }
    return accountHeadsQuery;
  }

}
