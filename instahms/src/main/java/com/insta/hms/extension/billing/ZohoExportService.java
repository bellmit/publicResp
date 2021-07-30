package com.insta.hms.extension.billing;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.extension.accounting.zoho.books.api.ChartOfAccountsApi;
import com.insta.hms.extension.accounting.zoho.books.api.JournalsApi;
import com.insta.hms.extension.accounting.zoho.books.exception.BooksException;
import com.insta.hms.extension.accounting.zoho.books.model.ChartOfAccount;
import com.insta.hms.extension.accounting.zoho.books.model.ChartOfAccountList;
import com.insta.hms.extension.accounting.zoho.books.model.Journal;
import com.insta.hms.extension.accounting.zoho.books.model.LineItem;
import com.insta.hms.extension.accounting.zoho.books.service.ZohoBooks;
import com.insta.hms.integration.InstaIntegrationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class ZohoExportService.
 */
@Service
public class ZohoExportService extends AccountingExportService<ZohoBooks, ChartOfAccount, Journal> {

  /** The account names repo. */
  @LazyAutowired
  ExportAccountNameRepository accountNamesRepo;

  /** The journal log repo. */
  @LazyAutowired
  AccountingExportLogRepository journalLogRepo;

  /** The integration service. */
  @LazyAutowired
  InstaIntegrationService integrationService;

  /** The Constant TARGETS. */
  private static final String[] TARGETS = new String[] { "zohobooks" };

  /** The Constant STATUS_SUCCESS. */
  private static final String STATUS_SUCCESS = "Success";

  /** The Constant STATUS_FAILED. */
  private static final String STATUS_FAILED = "Failed";

  /**
   * Instantiates a new zoho export service.
   */
  public ZohoExportService() {
    super(TARGETS);
  }

  /**
   * Sets the event handler.
   *
   * @param handler
   *          the new event handler
   */
  @Autowired
  public void setEventHandler(AccountingExportEventHandler handler) {
    super.setExportEventHandler(handler);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.extension.billing.AccountingExportService#initialize(java.lang.String,
   * java.lang.Integer, java.lang.Integer)
   */
  @Override
  protected ZohoBooks initialize(String target, Integer centerId, Integer accountGroupId)
      throws Exception {
    // Integer centerId = RequestContext.getCenterId();
    BasicDynaBean config = integrationService.getCenterIntegrationDetails(target, centerId,
        accountGroupId);

    if (null == config) {
      throw new BooksException(-1, "Incorrect / missing accounting export configuration");
    }

    ZohoBooks service = null;
    service = new ZohoBooks();
    /* Pick the center specific configuration */
    String secret = (String) config.get("establishment_key"); // application_secret");
    String applicationId = (String) config.get("merchant_id"); // "application_id");

    /* Fall back on the main table configuration, if the center configuration is not found */
    if (null == secret || null == applicationId) {
      secret = (String) config.get("application_secret"); // application_secret");
      applicationId = (String) config.get("application_id"); // "application_id");
    }
    service.initialize(secret, applicationId);
    return service;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.extension.billing.AccountingExportService#syncAccountNames(java.lang.Object)
   */
  protected List<ChartOfAccount> syncAccountNames(ZohoBooks service) throws Exception {
    ChartOfAccountsApi accountsApi = service.getChartOfAccountsApi();
    HashMap<String, Object> hashMap = new HashMap<String, Object>();
    hashMap.put("filter_by", "AccountType.All");
    hashMap.put("sort_column", "account_type");
    ChartOfAccountList accountList = null;
    accountList = accountsApi.getChartOfAccounts(hashMap);
    for (ChartOfAccount account : accountList) {
      if (null == accountNamesRepo.findByKey("account_id", account.getAccountId())) {
        BasicDynaBean bean = accountNamesRepo.getBean();
        bean.set("account_id", account.getAccountId());
        bean.set("account_name", account.getAccountName());
        bean.set("account_type", account.getAccountType());
        bean.set("description", account.getDescription());
        accountNamesRepo.insert(bean);
      }
    }
    return accountList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.extension.billing.AccountingExportService#postVoucher(java.lang.Object,
   * java.util.List, java.util.Map, java.util.Map, java.util.Map)
   */
  @Override
  protected Journal postVoucher(ZohoBooks service, List<ChartOfAccount> accountList,
      Map<String, Object> beanMap, Map<String, BigDecimal> credit, Map<String, BigDecimal> debit)
          throws Exception {
    Journal zohoJournal = null;
    Journal newJournal = null;
    if (null != beanMap && !beanMap.isEmpty()) {
      List<LineItem> lineItems = createLineItems(accountList, beanMap, credit, debit);

      if (null != lineItems && lineItems.size() > 0) {
        zohoJournal = new Journal();
        String journalDate = new SimpleDateFormat("yyyy-MM-dd")
            .format((Date) beanMap.get("voucher_date"));
        zohoJournal.setJournalDate(journalDate);
        zohoJournal.setReferenceNumber((String) beanMap.get("voucher_no"));
        zohoJournal.setNotes((String) beanMap.get("remarks"));
        zohoJournal.setLineItems(lineItems);
        JournalsApi journalsApi = service.getJournalsApi();
        newJournal = journalsApi.create(zohoJournal);
      }
    }
    return newJournal;
  }

  /**
   * Creates the line items.
   *
   * @param accountList
   *          the account list
   * @param beanMap
   *          the bean map
   * @param credit
   *          the credit
   * @param debit
   *          the debit
   * @return the list
   */
  private List<LineItem> createLineItems(List<ChartOfAccount> accountList,
      Map<String, Object> beanMap, Map<String, BigDecimal> credit, Map<String, BigDecimal> debit) {
    List<LineItem> lineItems = new ArrayList<LineItem>();
    for (String crAccount : credit.keySet()) {
      LineItem lineItem = new LineItem();
      lineItem.setAccountId(getAccountId(accountList, crAccount));
      lineItem.setAmount(credit.get(crAccount).doubleValue());
      lineItem.setDebitOrCredit("credit");
      lineItems.add(lineItem);
    }
    for (String drAccount : debit.keySet()) {
      LineItem lineItem = new LineItem();
      lineItem.setAccountId(getAccountId(accountList, drAccount));
      lineItem.setAmount(debit.get(drAccount).doubleValue());
      lineItem.setDebitOrCredit("debit");
      lineItems.add(lineItem);
    }
    return lineItems;
  }

  /**
   * Gets the account id.
   *
   * @param accountList
   *          the account list
   * @param accountName
   *          the account name
   * @return the account id
   */
  private String getAccountId(List<ChartOfAccount> accountList, String accountName) {
    if (null != accountName && null != accountList && accountList.size() > 0) {
      for (ChartOfAccount account : accountList) {
        String name = account.getAccountName();
        if (null != name && name.equalsIgnoreCase(accountName.trim())) {
          return account.getAccountId();
        }
      }
    }
    return null;
  }

}
