package com.insta.hms.billing.accounting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.Accounting.AccountingPrefsDAO;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @author krishna.t
 *
 */
public class AccountingHelper {

  static Logger log = LoggerFactory.getLogger(AccountingHelper.class);

  java.sql.Date voucherDate = null;
  Map debitSummary = null;
  Map creditSummary = null;
  java.sql.Date voucherFromDate = null;
  java.sql.Date voucherToDate = null;
  boolean atleastOneVoucherExported = false;
  int saveAgnstExportNo = 0;
  BasicDynaBean acPrefs = null;

  AccountingPrefsDAO acPrefsDAO = new AccountingPrefsDAO();

  // private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
  // private SimpleDateFormat instaStyleFormatter = new SimpleDateFormat("dd-MM-yyyy");

  private static final Map<String, String> exporterMap = new HashMap<>();
  private static final Map<String, String> formatterTemplates = new HashMap<>();
  private static final Map<String, AccountingVoucherFormatter> formatters = new HashMap<>();

  static {
    exporterMap.put("IPBills", "com.insta.hms.billing.accounting.AccountingBillsExporter");
    exporterMap.put("OPandOthersBills", "com.insta.hms.billing.accounting.AccountingBillsExporter");
    exporterMap.put("IPReceipts", "com.insta.hms.billing.accounting.AccountingReceiptsExporter");
    exporterMap.put("OPandOthersReceipts",
        "com.insta.hms.billing.accounting.AccountingReceiptsExporter");
    exporterMap.put("Deposits", "com.insta.hms.billing.accounting.AccountingDepositsExporter");
    exporterMap.put("Consolidated Sponsor Receipts",
        "com.insta.hms.billing.accounting.AccountingSponsorReceiptsExporter");
    exporterMap.put("Payment Vouchers",
        "com.insta.hms.billing.accounting.AccountingPaymentsExporter");
    exporterMap.put("Payments Due",
        "com.insta.hms.billing.accounting.AccountingPaymentDuesExporter");
    exporterMap.put("Stores Invoices",
        "com.insta.hms.billing.accounting.AccountingStoreInvoicesExporter");
    exporterMap.put("Stores Returns with Debit",
        "com.insta.hms.billing.accounting.AccountingStoreReturnsExporter");
    exporterMap.put("Stores Consignment Stock Issued",
        "com.insta.hms.billing.accounting.AccountingConsignmentIssuesExporter");
    exporterMap.put("Stores Consignment Stock Returns",
        "com.insta.hms.billing.accounting.AccountingConsignmentReturnsExporter");

  }

  static {
    formatterTemplates.put("tallyxml", "TallyXmlVoucher.ftl");
    formatterTemplates.put("details", "voucher.ftl");
  }

  static {
    formatters.put("tallyxml", new VoucherXmlFormatter("tallyxml"));
    formatters.put("details", new VoucherXmlFormatter("details"));
  }

  /**
   * Gets the exporter.
   *
   * @param exportItemType
   *          the export item type
   * @return the exporter
   * @throws ClassNotFoundException
   *           the class not found exception
   * @throws InstantiationException
   *           the instantiation exception
   * @throws IllegalAccessException
   *           the illegal access exception
   */
  public static final AccountingExporter getExporter(String exportItemType)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    String clazz = exporterMap.get(exportItemType);
    if (null != clazz) {
      Class exporterClass = Class.forName(clazz);
      Object exporter = exporterClass.newInstance();
      if (exporter instanceof AccountingExporter) {
        return (AccountingExporter) exporter;
      }
    }
    return null;
  }

  /**
   * Gets the exporters.
   *
   * @param exportItemTypes
   *          the export item types
   * @return the exporters
   * @throws ClassNotFoundException
   *           the class not found exception
   * @throws InstantiationException
   *           the instantiation exception
   * @throws IllegalAccessException
   *           the illegal access exception
   */
  public static final List<AccountingExporter> getExporters(String[] exportItemTypes)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    List<AccountingExporter> exporters = new ArrayList<AccountingExporter>();
    for (String itemType : exportItemTypes) {
      exporters.add(getExporter(itemType));
    }
    return exporters;
  }

  /**
   * Instantiates a new accounting helper.
   *
   * @param voucherDate
   *          the voucher date
   * @param debitSummary
   *          the debit summary
   * @param creditSummary
   *          the credit summary
   * @param voucherFromDate
   *          the voucher from date
   * @param voucherToDate
   *          the voucher to date
   * @param saveAgnstExportNo
   *          the save agnst export no
   * @throws SQLException
   *           the SQL exception
   */
  public AccountingHelper(java.sql.Date voucherDate, Map debitSummary, Map creditSummary,
      java.sql.Date voucherFromDate, java.sql.Date voucherToDate, int saveAgnstExportNo)
      throws SQLException {
    this.voucherDate = voucherDate;
    this.debitSummary = debitSummary;
    this.creditSummary = creditSummary;
    this.voucherFromDate = voucherFromDate;
    this.voucherToDate = voucherToDate;
    this.saveAgnstExportNo = saveAgnstExportNo;
    Connection con = DataBaseUtil.getConnection();
    try {
      acPrefs = acPrefsDAO.getRecord(con);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /*
   * public Voucher createVoucher(java.util.Date dt, String idStr, String vchType, String narration,
   * BasicDynaBean acPrefs) { Voucher voucher = new Voucher(); String guidPrefix = (String)
   * acPrefs.get("tally_guid_prefix"); voucher.setRemoteId(guidPrefix + idStr);
   * voucher.setVoucherType(vchType); voucher.setAction("Create"); if (voucherDate != null)
   * voucher.setDate(voucherDate); else voucher.setDate(dt);
   * voucher.setFormattedDate(instaStyleFormatter.format(voucher.getDate()));
   * voucher.setGuid(guidPrefix + idStr); voucher.setVoucherNumber(idStr);
   * voucher.setNarration(narration); voucher.setVoucherTypeName(vchType);
   * voucher.setEffectiveDate(dateFormatter.format(voucher.getDate()));
   * 
   * return voucher; }
   */

  /*
   * public Voucher createVoucher(java.util.Date dt, String idStr, String vchType) { return
   * createVoucher(dt, idStr, vchType, null, acPrefs); }
   */

  /**
   * Adds the debit.
   *
   * @param entity
   *          the entity
   * @param ledgerType
   *          the ledger type
   * @param voucher
   *          the voucher
   * @param ledgerAccount
   *          the ledger account
   * @param amount
   *          the amount
   * @param costCenterCode
   *          the cost center code
   */
  public void addDebit(String entity, String ledgerType, Voucher voucher, String ledgerAccount,
      BigDecimal amount, String costCenterCode) {
    addDebitOrCredit(entity, ledgerType, voucher, ledgerAccount, amount, true, costCenterCode);
  }

  /**
   * Adds the credit.
   *
   * @param entity
   *          the entity
   * @param ledgerType
   *          the ledger type
   * @param voucher
   *          the voucher
   * @param ledgerAccount
   *          the ledger account
   * @param amount
   *          the amount
   * @param costCenterCode
   *          the cost center code
   */
  public void addCredit(String entity, String ledgerType, Voucher voucher, String ledgerAccount,
      BigDecimal amount, String costCenterCode) {
    addDebitOrCredit(entity, ledgerType, voucher, ledgerAccount, amount, false, costCenterCode);
  }

  /**
   * Adds the debit or credit.
   *
   * @param entity
   *          the entity
   * @param ledgerTypeName
   *          the ledger type name
   * @param voucher
   *          the voucher
   * @param account
   *          the account
   * @param amt
   *          the amt
   * @param isDebit
   *          the is debit
   */
  @SuppressWarnings("unchecked")
  public void addDebitOrCredit(String entity, String ledgerTypeName, Voucher voucher,
      String account, BigDecimal amt, boolean isDebit) {
    addDebitOrCredit(entity, ledgerTypeName, voucher, account, amt, isDebit, null);
  }

  /**
   * Adds the debit or credit.
   *
   * @param entity
   *          the entity
   * @param ledgerTypeName
   *          the ledger type name
   * @param voucher
   *          the voucher
   * @param account
   *          the account
   * @param amt
   *          the amt
   * @param isDebit
   *          the is debit
   * @param costCenterCode
   *          the cost center code
   */
  @SuppressWarnings("unchecked")
  public void addDebitOrCredit(String entity, String ledgerTypeName, Voucher voucher,
      String account, BigDecimal amt, boolean isDebit, String costCenterCode) {

    addDebitOrCredit(entity, ledgerTypeName, voucher, account, amt, isDebit, null, costCenterCode);
  }

  /**
   * Adds the debit or credit.
   *
   * @param entity
   *          the entity
   * @param ledgerTypeName
   *          the ledger type name
   * @param voucher
   *          the voucher
   * @param account
   *          the account
   * @param amt
   *          the amt
   * @param isDebit
   *          the is debit
   * @param index
   *          the index
   * @param costCenterCode
   *          the cost center code
   */
  @SuppressWarnings("unchecked")
  public void addDebitOrCredit(String entity, String ledgerTypeName, Voucher voucher,
      String account, BigDecimal amt, boolean isDebit, int index, String costCenterCode) {

    if (amt.compareTo(BigDecimal.ZERO) < 0) {
      amt = amt.negate();
      isDebit = !isDebit;
    }

    List<Map> ledgerList = voucher.getLedgerList();
    HashMap map = new HashMap();
    map.put("LEDGERNAME", account);
    map.put("LEDGERTYPENAME", ledgerTypeName);
    map.put("ISDEEMEDPOSITIVE", isDebit);
    map.put("AMOUNT", (isDebit ? amt.negate() : amt).toString());
    map.put("negativeAMOUNT", (!isDebit ? amt.negate() : amt).toString());

    String costCenterBasis = (String) acPrefs.get("cost_center_basis");
    if (!costCenterBasis.equals("None") && (costCenterCode != null && !costCenterCode.equals(""))) {
      List list = new ArrayList();
      list.add(costCenterAllocations(costCenterCode, amt, isDebit));
      map.put("costCenters", list);
    }
    if (index == -1) {
      ledgerList.add(map);
    } else {
      ledgerList.add(index, map);
    }

    // amount not required to add to debit summary or credit summary for which voucher date doesn't
    // falls b/w
    // voucherfromdate and vouchertodate since they are not exported to tally.
    if (!isVoucherInDateRange(voucher)) {
      return;
    }

    if (isDebit) {
      addToDebitSummary(entity, account, amt);
    } else {
      addToCreditSummary(entity, account, amt);
    }
  }

  /**
   * Adds the debit or credit.
   *
   * @param entity
   *          the entity
   * @param ledgerTypeName
   *          the ledger type name
   * @param voucher
   *          the voucher
   * @param account
   *          the account
   * @param amt
   *          the amt
   * @param isDebit
   *          the is debit
   * @param billAllocations
   *          the bill allocations
   * @param costCenterCode
   *          the cost center code
   */
  @SuppressWarnings("unchecked")
  public void addDebitOrCredit(String entity, String ledgerTypeName, Voucher voucher,
      String account, BigDecimal amt, boolean isDebit, Map billAllocations, String costCenterCode) {
    List costCenterList = new ArrayList();
    String costCenterBasis = (String) acPrefs.get("cost_center_basis");
    if (!costCenterBasis.equals("None") && (costCenterCode != null && !costCenterCode.equals(""))) {
      costCenterList.add(costCenterAllocations(costCenterCode, amt, isDebit));
    }
    addDebitOrCredit(entity, ledgerTypeName, voucher, account, amt, isDebit, billAllocations,
        costCenterList);
  }

  /**
   * Adds the debit or credit.
   *
   * @param entity
   *          the entity
   * @param ledgerTypeName
   *          the ledger type name
   * @param voucher
   *          the voucher
   * @param account
   *          the account
   * @param amt
   *          the amt
   * @param isDebit
   *          the is debit
   * @param billAllocations
   *          the bill allocations
   * @param costCenterAllocations
   *          the cost center allocations
   */
  @SuppressWarnings("unchecked")
  public void addDebitOrCredit(String entity, String ledgerTypeName, Voucher voucher,
      String account, BigDecimal amt, boolean isDebit, Map billAllocations,
      List costCenterAllocations) {

    addDebitOrCredit(entity, ledgerTypeName, voucher, account, amt, isDebit, billAllocations,
        costCenterAllocations, -1);
  }

  /**
   * Adds the debit or credit.
   *
   * @param entity
   *          the entity
   * @param ledgerTypeName
   *          the ledger type name
   * @param voucher
   *          the voucher
   * @param account
   *          the account
   * @param amt
   *          the amt
   * @param isDebit
   *          the is debit
   * @param billAllocations
   *          the bill allocations
   * @param costCenterAllocations
   *          the cost center allocations
   * @param index
   *          the index
   */
  @SuppressWarnings("unchecked")
  public void addDebitOrCredit(String entity, String ledgerTypeName, Voucher voucher,
      String account, BigDecimal amt, boolean isDebit, Map billAllocations,
      List costCenterAllocations, int index) {

    if (amt.compareTo(BigDecimal.ZERO) < 0) {
      amt = amt.negate();
      isDebit = !isDebit;
    }

    List<Map> ledgerList = voucher.getLedgerList();
    HashMap map = new HashMap();
    map.put("LEDGERNAME", account);
    map.put("LEDGERTYPENAME", ledgerTypeName);
    map.put("ISDEEMEDPOSITIVE", isDebit);
    map.put("AMOUNT", (isDebit ? amt.negate() : amt).toString());
    map.put("negativeAMOUNT", (!isDebit ? amt.negate() : amt).toString());

    if (billAllocations != null && !billAllocations.isEmpty()) {
      map.put("billAllocations", billAllocations);
    }
    if (costCenterAllocations != null && !costCenterAllocations.isEmpty()) {
      map.put("costCenters", costCenterAllocations);
    }

    if (index == -1) {
      ledgerList.add(map);
    } else {
      ledgerList.add(index, map);
    }
    // amount not required to add to debit summary or credit summary for which voucher date doesn't
    // falls b/w
    // voucherfromdate and vouchertodate since they are not exported to tally.
    if (!isVoucherInDateRange(voucher)) {
      return;
    }
    if (isDebit) {
      addToDebitSummary(entity, account, amt);
    } else {
      addToCreditSummary(entity, account, amt);
    }
  }

  /*
   * Add data to the summary. Summary is two Maps like this: Debits: Entity1 => Account1 => amount
   * Account2 => amount Entity2 => Account1 => amount Account2 => amount ... Credits: ... (same as
   * Debits)
   */

  // TODO : Change the method signature so that the targetMap is one of the parameters
  // When calling the method, pass in debitSummary / creditSummary as a parameter
  // Only method will suffice for both debit and credit summary

  /**
   * Adds the to debit summary.
   *
   * @param entity
   *          the entity
   * @param drAccount
   *          the dr account
   * @param amount
   *          the amount
   */
  @SuppressWarnings("unchecked")
  private void addToDebitSummary(String entity, String drAccount, BigDecimal amount) {
    if (debitSummary != null) {
      Map accountsMap = (Map) debitSummary.get(entity);
      if (accountsMap == null) {
        accountsMap = new HashMap();
        debitSummary.put(entity, accountsMap);
      }
      BigDecimal amt = (BigDecimal) accountsMap.get(drAccount);
      if (amt == null) {
        accountsMap.put(drAccount, amount);
      } else {
        accountsMap.put(drAccount, amt.add(amount));
      }
    }
  }

  /**
   * Adds the to credit summary.
   *
   * @param entity
   *          the entity
   * @param crAccount
   *          the cr account
   * @param amount
   *          the amount
   */
  @SuppressWarnings("unchecked")
  private void addToCreditSummary(String entity, String crAccount, BigDecimal amount) {
    if (creditSummary != null) {
      Map accountsMap = (Map) creditSummary.get(entity);
      if (accountsMap == null) {
        accountsMap = new HashMap();
        creditSummary.put(entity, accountsMap);
      }
      BigDecimal amt = (BigDecimal) accountsMap.get(crAccount);
      if (amt == null) {
        accountsMap.put(crAccount, amount);
      } else {
        accountsMap.put(crAccount, amt.add(amount));
      }
    }
  }

  /**
   * Save export log.
   *
   * @param voucherNo
   *          the voucher no
   * @param exportVoucherType
   *          the export voucher type
   * @param saveExportNo
   *          the save export no
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static void saveExportLog(String voucherNo, String exportVoucherType, int saveExportNo)
      throws SQLException, IOException {
    Connection con = DataBaseUtil.getConnection();
    GenericDAO dao = new GenericDAO("accounting_voucher_details");
    BasicDynaBean bean = dao.getBean();
    try {
      bean.set("voucher_no", voucherNo);
      bean.set("voucher_type", exportVoucherType);
      bean.set("export_no", saveExportNo);
      if (!dao.insert(con, bean)) {
        log.error("Failed to insert voucher :" + voucherNo + " : for Export :" + saveExportNo
            + ". Hence not exporting the voucher.");
        return;
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Adds the header.
   *
   * @param stream
   *          the stream
   * @param format
   *          the format
   * @param accountingCompanyName
   *          the accounting company name
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   */
  public static void addHeader(OutputStream stream, String format, String accountingCompanyName)
      throws IOException, TemplateException {
    Template templateObj = getHeaderTemplate(format);
    if (templateObj == null) {
      return;
    }

    Map ftlMap = new HashMap();
    if (format.equals("tallyxml")) {
      ftlMap.put("accounting_company_name", accountingCompanyName);
    }

    StringWriter stringWriter = new StringWriter();
    templateObj.process(ftlMap, stringWriter);
    stream.write(stringWriter.toString().getBytes());
    stream.flush();
  }

  /**
   * Gets the header template.
   *
   * @param format
   *          the format
   * @return the header template
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private static Template getHeaderTemplate(String format) throws IOException {
    Template templateObj = null;
    if (format.equals("details")) {
      templateObj = AppInit.getFmConfig().getTemplate("TallyExportDetailsHeader.ftl");
    } else if (format.equals("tallyxml")) {
      templateObj = AppInit.getFmConfig().getTemplate("TallyXmlHeader.ftl");
    }
    return templateObj;
  }

  /**
   * Adds the footer.
   *
   * @param stream
   *          the stream
   * @param fromDate
   *          the from date
   * @param toDate
   *          the to date
   * @param useVoucherDate
   *          the use voucher date
   * @param voucherDate
   *          the voucher date
   * @param format
   *          the format
   * @param exportFor
   *          the export for
   * @param exportItems
   *          the export items
   * @param voucherFromDateStr
   *          the voucher from date str
   * @param voucherToDateStr
   *          the voucher to date str
   * @param fromTimeStr
   *          the from time str
   * @param toTimeStr
   *          the to time str
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   */
  @SuppressWarnings("unchecked")
  public static void addFooter(OutputStream stream, String fromDate, String toDate,
      String useVoucherDate, String voucherDate, String format, String exportFor,
      String[] exportItems, String voucherFromDateStr, String voucherToDateStr, String fromTimeStr,
      String toTimeStr) throws IOException, TemplateException {

    Template templateObj = getFooterTemplate(format);
    if (templateObj == null) {
      return;
    }

    Map ftlMap = new HashMap();
    if (format.equals("details")) {
      ftlMap.put("fromDate", fromDate);
      ftlMap.put("toDate", toDate);
      ftlMap.put("useVoucherDate", useVoucherDate);
      ftlMap.put("voucherDate", voucherDate);
      ftlMap.put("format", format);
      ftlMap.put("exportFor", exportFor);
      ftlMap.put("exportItems", exportItems);
      ftlMap.put("voucherFromDate", voucherFromDateStr);
      ftlMap.put("voucherToDate", voucherToDateStr);
      ftlMap.put("fromTime", fromTimeStr);
      ftlMap.put("toTime", toTimeStr);
    }
    StringWriter stringWriter = new StringWriter();
    templateObj.process(ftlMap, stringWriter);
    stream.write(stringWriter.toString().getBytes());
    stream.flush();
  }

  /**
   * Gets the footer template.
   *
   * @param format
   *          the format
   * @return the footer template
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private static Template getFooterTemplate(String format) throws IOException {
    Template templateObj = null;
    if (format.equals("details")) {
      templateObj = AppInit.getFmConfig().getTemplate("TallyExportDetailsFooter.ftl");
    }
    if (format.equals("tallyxml")) {
      templateObj = AppInit.getFmConfig().getTemplate("TallyXmlFooter.ftl");
    }
    return templateObj;
  }

  /**
   * Fill bill allocations.
   *
   * @param name
   *          the name
   * @param newRef
   *          the new ref
   * @param amount
   *          the amount
   * @param isDebit
   *          the is debit
   * @return the map
   */
  public static Map fillBillAllocations(String name, boolean newRef, BigDecimal amount,
      boolean isDebit) {
    Map map = new HashMap();
    map.put("NAME", name);
    map.put("BILLTYPE", newRef ? "New Ref" : "Agst Ref");
    map.put("AMOUNT", (isDebit ? amount.negate() : amount).toString());

    return map;
  }

  /**
   * Cost center allocations.
   *
   * @param name
   *          the name
   * @param amount
   *          the amount
   * @param isDebit
   *          the is debit
   * @return the map
   */
  public static Map costCenterAllocations(String name, BigDecimal amount, boolean isDebit) {

    Map map = new HashMap();
    map.put("NAME", name);
    map.put("AMOUNT", (isDebit ? amount.negate() : amount).toString());
    // negativeAMOUNT is used while showing export details on screen.
    // it is not used in xml.
    map.put("negativeAMOUNT", (!isDebit ? amount.negate() : amount).toString());
    return map;
  }

  /**
   * Gets the ledger type name.
   *
   * @param accountType
   *          the account type
   * @return the ledger type name
   */
  public String getLedgerTypeName(String accountType) {
    String ledgerTypeName = "";
    if (accountType.equalsIgnoreCase("Cash")) {
      ledgerTypeName = "CASH ACCOUNT";
    } else if (accountType.equalsIgnoreCase("Bank")) {
      ledgerTypeName = "BANK ACCOUNT";
    } else if (accountType.equalsIgnoreCase("C/V")) {
      ledgerTypeName = "CUSTOMER/VENDOR ACCOUNT";
    } else if (accountType.equalsIgnoreCase("I/E")) {
      ledgerTypeName = "INCOME/EXPENSES ACCOUNT";
    } else if (accountType.equalsIgnoreCase("Sales")) {
      ledgerTypeName = "SALES ACCOUNT";
    } else if (accountType.equalsIgnoreCase("Purchases")) {
      ledgerTypeName = "PURCHASES ACCOUNT";
    } else if (accountType.equalsIgnoreCase("A/L")) {
      ledgerTypeName = "ASSETS/LIABILITIES ACCOUNT";
    }
    return ledgerTypeName;
  }

  /**
   * Checks if is voucher in date range.
   *
   * @param voucher
   *          the voucher
   * @return true, if is voucher in date range
   */
  public boolean isVoucherInDateRange(Voucher voucher) {

    if (voucherFromDate != null) {
      if (voucher.getDate().getTime() >= voucherFromDate.getTime()) {
        // allow exporting voucher
      } else {
        return false;
      }
    }
    if (voucherToDate != null) {
      if (voucher.getDate().getTime() <= voucherToDate.getTime()) {
        // allow exporting voucher
      } else {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets the formatting template.
   *
   * @param format
   *          the format
   * @return the formatting template
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static Template getFormattingTemplate(String format) throws IOException {
    Template templateObj = null;
    if (null == format) {
      return null;
    }
    String template = formatterTemplates.get(format);
    if (null != template) {
      templateObj = AppInit.getFmConfig().getTemplate(template);
    }
    return templateObj;
  }

  /**
   * Gets the formatter.
   *
   * @param format
   *          the format
   * @return the formatter
   */
  public static AccountingVoucherFormatter getFormatter(String format) {
    AccountingVoucherFormatter accountingVoucherFormatter = null;
    if (null != format) {
      accountingVoucherFormatter = formatters.get(format);
    }
    return accountingVoucherFormatter;
  }

}
