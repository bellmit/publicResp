package com.insta.hms.billing.accounting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.accountinglog.XmlImportExportLogDAO;
import com.insta.hms.master.Accounting.AccountingPrefsDAO;
import com.insta.hms.master.Accounting.PartyAccountNamesDAO;
import com.insta.hms.master.Accounting.SpecialAccountNamesDAO;
import com.insta.hms.master.Accounting.VoucherTypesDAO;
import com.insta.hms.master.DepartmentMaster.DepartmentMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import freemarker.template.TemplateException;

import jlibs.core.util.regex.TemplateMatcher;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class AccountingExporter.
 *
 * @author krishna
 */
public class AccountingExporter {

  /** The ac prefs DAO. */
  private static AccountingPrefsDAO acPrefsDAO = new AccountingPrefsDAO();

  /** The v types DAO. */
  private static VoucherTypesDAO vTypesDAO = new VoucherTypesDAO();

  /** The party names DAO. */
  private static PartyAccountNamesDAO partyNamesDAO = new PartyAccountNamesDAO();

  /** The spl ac names DAO. */
  private static SpecialAccountNamesDAO splAcNamesDAO = new SpecialAccountNamesDAO();

  /** The import export log dao. */
  protected static XmlImportExportLogDAO importExportLogDao = new XmlImportExportLogDAO();

  /** The dept dao. */
  private static DepartmentMasterDAO deptDao = new DepartmentMasterDAO();

  /** The log. */
  static Logger log = LoggerFactory.getLogger(AccountingExporter.class);

  /** The ac prefs. */
  BasicDynaBean acPrefs = null;

  /** The v types. */
  BasicDynaBean voucherTypes = null;

  /** The party names. */
  BasicDynaBean partyNames = null;

  /** The spl ac names. */
  BasicDynaBean splAcNames = null;

  /** The helper. */
  AccountingHelper helper = null;

  /** The from date. */
  java.sql.Timestamp fromDate = null;

  /** The to date. */
  java.sql.Timestamp toDate = null;

  /** The stream. */
  OutputStream stream = null;

  /** The format. */
  String format = null;

  /** The account group. */
  Integer accountGroup = null;

  /** The voucher from date. */
  java.sql.Date voucherFromDate = null;

  /** The voucher to date. */
  java.sql.Date voucherToDate = null;

  /** The voucher date. */
  java.sql.Date voucherDate = null;

  /** The save agnst export no. */
  int saveAgnstExportNo = 0;

  /** The fetch agnst export no. */
  int fetchAgnstExportNo = 0;

  /** The incoming pat cost center code. */
  String incomingPatCostCenterCode = "";

  /** The pharmacy cost center code. */
  String pharmacyCostCenterCode = "";

  /** The outside pat cost center code. */
  String outsidePatCostCenterCode = "";

  /** The gen prefs. */
  BasicDynaBean genPrefs = null;

  /** The debit summary. */
  private Map debitSummary = null;

  /** The credit summary. */
  private Map creditSummary = null;

  /**
   * Instantiates a new accounting exporter.
   *
   * @param format
   *          the format
   */
  public AccountingExporter(String format) {
    this.format = format;
  }

  /**
   * Instantiates a new accounting exporter.
   *
   * @param fromDate
   *          the from date
   * @param toDate
   *          the to date
   * @param accountGroup
   *          the account group
   * @param format
   *          the format
   * @param saveAgnstExportNo
   *          the save agnst export no
   * @param voucherDate
   *          the voucher date
   * @throws SQLException
   *           the SQL exception
   */
  public AccountingExporter(java.sql.Timestamp fromDate, java.sql.Timestamp toDate,
      Integer accountGroup, String format, int saveAgnstExportNo, java.sql.Date voucherDate)
      throws SQLException {

    this(fromDate, toDate, accountGroup, voucherDate, null, null, null, null, format,
        saveAgnstExportNo, 0);
  }

  /**
   * Instantiates a new accounting exporter.
   *
   * @param accountGroup
   *          the account group
   * @param saveAgnstExportNo
   *          the save agnst export no
   * @param fetchAgnstExportNo
   *          the fetch agnst export no
   * @param format
   *          the format
   * @param voucherDate
   *          the voucher date
   * @throws SQLException
   *           the SQL exception
   */
  public AccountingExporter(int accountGroup, int saveAgnstExportNo, int fetchAgnstExportNo,
      String format, java.sql.Date voucherDate) throws SQLException {
    this(null, null, accountGroup, voucherDate, null, null, null, null, format, saveAgnstExportNo,
        fetchAgnstExportNo);
  }

  /**
   * Instantiates a new accounting exporter.
   *
   * @param fromDate
   *          the from date
   * @param toDate
   *          the to date
   * @param accountGroup
   *          the account group
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
   * @param format
   *          the format
   * @param saveAgnstExportNo
   *          the save agnst export no
   * @param fetchAgnstExportNo
   *          the fetch agnst export no
   * @throws SQLException
   *           the SQL exception
   */
  public AccountingExporter(java.sql.Timestamp fromDate, java.sql.Timestamp toDate,
      Integer accountGroup, java.sql.Date voucherDate, Map debitSummary, Map creditSummary,
      java.sql.Date voucherFromDate, java.sql.Date voucherToDate, String format,
      int saveAgnstExportNo, int fetchAgnstExportNo) throws SQLException {

    this.fromDate = fromDate;
    this.toDate = toDate;
    this.format = format;
    this.accountGroup = accountGroup;
    this.voucherDate = voucherDate;
    this.voucherFromDate = voucherFromDate;
    this.voucherToDate = voucherToDate;
    this.saveAgnstExportNo = saveAgnstExportNo;
    this.fetchAgnstExportNo = fetchAgnstExportNo;
    this.debitSummary = debitSummary;
    this.creditSummary = creditSummary;
    helper = new AccountingHelper(voucherDate, debitSummary, creditSummary, voucherFromDate,
        voucherToDate, saveAgnstExportNo);
  }

  /**
   * Export details.
   *
   * @param exportItems
   *          the export items
   * @param stream
   *          the stream
   * @param centerId
   *          the center id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws ClassNotFoundException
   *           the class not found exception
   * @throws AccountingException
   *           the accounting exception
   */
  public boolean exportDetails(String[] exportItems, OutputStream stream, int centerId)
      throws SQLException, IOException, TemplateException, ClassNotFoundException,
      AccountingException {
    this.stream = stream;
    loadPreferences();
    List list = importExportLogDao.getVouchers(fetchAgnstExportNo);
    boolean atleastOneVoucherExported = false;
    boolean flag = false;
    for (String exportItem : exportItems) {
      if (exportItem.equals("") || exportItem.equals("Bills")) {

        flag = new AccountingHospitalBillsExporter(format).export(centerId, fromDate, toDate,
            accountGroup, voucherDate, debitSummary, creditSummary, voucherFromDate, voucherToDate,
            saveAgnstExportNo, fetchAgnstExportNo, stream);
        if (!atleastOneVoucherExported) {
          atleastOneVoucherExported = flag;
        }

        flag = new AccountingPharmacyBillsExporter(format).export(centerId, fromDate, toDate,
            accountGroup, voucherDate, debitSummary, creditSummary, voucherFromDate, voucherToDate,
            saveAgnstExportNo, fetchAgnstExportNo, stream);
        if (!atleastOneVoucherExported) {
          atleastOneVoucherExported = flag;
        }
      }
      if (exportItem.equals("") || exportItem.equals("Receipts")) {
        flag = new AccountingReceiptsExporter(format).export(centerId, fromDate, toDate,
            accountGroup, voucherDate, debitSummary, creditSummary, voucherFromDate, voucherToDate,
            saveAgnstExportNo, fetchAgnstExportNo, stream);
        if (!atleastOneVoucherExported) {
          atleastOneVoucherExported = flag;
        }
      }

      if (exportItem.equals("") || exportItem.equals("Deposits")) {
        flag = new AccountingDepositsExporter(format).export(centerId, fromDate, toDate,
            accountGroup, voucherDate, debitSummary, creditSummary, voucherFromDate, voucherToDate,
            saveAgnstExportNo, fetchAgnstExportNo, stream);
      }
      if (!atleastOneVoucherExported) {
        atleastOneVoucherExported = flag;
      }

      if (exportItem.equals("") || exportItem.equals("Consolidated Sponsor Receipts")) {
        flag = new AccountingConsolidatedSponsorReceiptsExporter(format).export(centerId, fromDate,
            toDate, accountGroup, voucherDate, debitSummary, creditSummary, voucherFromDate,
            voucherToDate, saveAgnstExportNo, fetchAgnstExportNo, stream);
      }
      if (!atleastOneVoucherExported) {
        atleastOneVoucherExported = flag;
      }

      if (exportItem.equals("") || exportItem.equals("Payment Vouchers")) {
        flag = new AccountingPaymentsExporter(format).export(centerId, fromDate, toDate,
            accountGroup, voucherDate, debitSummary, creditSummary, voucherFromDate, voucherToDate,
            saveAgnstExportNo, fetchAgnstExportNo, stream);
      }
      if (!atleastOneVoucherExported) {
        atleastOneVoucherExported = flag;
      }

      if (exportItem.equals("") || exportItem.equals("Payments Due")) {
        flag = new AccountingPaymentDuesExporter(format).export(centerId, fromDate, toDate,
            accountGroup, voucherDate, debitSummary, creditSummary, voucherFromDate, voucherToDate,
            saveAgnstExportNo, fetchAgnstExportNo, stream);
      }
      if (!atleastOneVoucherExported) {
        atleastOneVoucherExported = flag;
      }

      if (exportItem.equals("") || exportItem.equals("Stores Invoices")) {
        flag = new AccountingStoreInvoicesExporter(format).export(centerId, fromDate, toDate,
            accountGroup, voucherDate, debitSummary, creditSummary, voucherFromDate, voucherToDate,
            saveAgnstExportNo, fetchAgnstExportNo, stream);
      }
      if (!atleastOneVoucherExported) {
        atleastOneVoucherExported = flag;
      }

      if (exportItem.equals("") || exportItem.equals("Stores Returns with Debit")) {
        // stuffAllStoresReturnsWithDebit("STORES_RETURNS_WITH_DEBIT", centerId,
        // importExportLogDao.getVouchers(list, "STORES_RETURNS_WITH_DEBIT", String.class));
        flag = new AccountingStoreReturnsExporter(format).export(centerId, fromDate, toDate,
            accountGroup, voucherDate, debitSummary, creditSummary, voucherFromDate, voucherToDate,
            saveAgnstExportNo, fetchAgnstExportNo, stream);
      }
      if (!atleastOneVoucherExported) {
        atleastOneVoucherExported = flag;
      }

      if (exportItem.equals("") || exportItem.equals("Stores Consignment Stock Issued")) {
        flag = new AccountingConsignmentIssuesExporter(format).export(centerId, fromDate, toDate,
            accountGroup, voucherDate, debitSummary, creditSummary, voucherFromDate, voucherToDate,
            saveAgnstExportNo, fetchAgnstExportNo, stream);
      }
      if (!atleastOneVoucherExported) {
        atleastOneVoucherExported = flag;
      }

      if (exportItem.equals("") || exportItem.equals("Stores Consignment Stock Returns")) {
        flag = new AccountingConsignmentReturnsExporter(format).export(centerId, fromDate, toDate,
            accountGroup, voucherDate, debitSummary, creditSummary, voucherFromDate, voucherToDate,
            saveAgnstExportNo, fetchAgnstExportNo, stream);
      }
      if (!atleastOneVoucherExported) {
        atleastOneVoucherExported = flag;
      }
    }

    return atleastOneVoucherExported;
  }

  protected String getPaymentModeAccount(String splAccountName, Map voucher) {
    TemplateMatcher matcher = new TemplateMatcher("${", "}");
    String retVal;
    try {
      retVal = matcher.replace(splAccountName, voucher);
    } catch (IllegalArgumentException exception) {
      log.error("Could not replace tokens in account: ", exception);
      retVal = splAccountName;
    }
    return retVal;
  }

  /**
   * Gets the pref for payments due.
   *
   * @return the pref for payments due
   */
  protected Map<String, String> getPrefForPaymentsDue() {
    Map<String, String> paymentTypes = new HashMap<String, String>();
    paymentTypes.put("D", "doctor");
    paymentTypes.put("P", "prescribingdoctor");
    paymentTypes.put("F", "referral");
    paymentTypes.put("R", "referral");
    paymentTypes.put("C", "misc");
    paymentTypes.put("O", "outhouse");

    Map<String, String> map = new HashMap<String, String>();
    for (Map.Entry<String, String> entry : paymentTypes.entrySet()) {
      String colNameStartsWith = entry.getValue();
      String paymentType = entry.getKey();
      String indivAccountsStr = (String) partyNames.get(colNameStartsWith + "_individual_accounts");
      boolean indivAccounts = ((String) indivAccountsStr).equals("Y");
      String ipPrefix = "";
      String ipSuffix = "";
      String opPrefix = "";
      String opSuffix = "";
      String commonAccount = "";
      String prefix = "";
      String suffix = "";
      if (indivAccounts) {
        if (paymentType.equals("D") || paymentType.equals("P") || paymentType.equals("F")
            || paymentType.equals("R")) {
          ipPrefix = (String) partyNames.get(colNameStartsWith + "_ip_ac_prefix");
          ipSuffix = (String) partyNames.get(colNameStartsWith + "_ip_ac_suffix");

          opPrefix = (String) partyNames.get(colNameStartsWith + "_op_ac_prefix");
          opSuffix = (String) partyNames.get(colNameStartsWith + "_op_ac_suffix");
        } else {
          prefix = (String) partyNames.get(colNameStartsWith + "_ac_prefix");
          suffix = (String) partyNames.get(colNameStartsWith + "_ac_suffix");
        }
      } else {
        commonAccount = (String) partyNames.get(colNameStartsWith + "_ac_name");
      }
      map.put(paymentType + "_indiv_accounts", indivAccounts + "");
      map.put(paymentType + "_ip_prefix", ipPrefix);
      map.put(paymentType + "_ip_suffix", ipSuffix);
      map.put(paymentType + "_op_prefix", opPrefix);
      map.put(paymentType + "_op_suffix", opSuffix);
      map.put(paymentType + "_prefix", prefix);
      map.put(paymentType + "_suffix", suffix);
      map.put(paymentType + "_common_account", commonAccount);
    }
    map.put("D_special_account", (String) splAcNames.get("doctor_payments_exp_ac_name"));
    map.put("P_special_account", (String) splAcNames
        .get("prescribing_doctor_payments_exp_ac_name"));
    map.put("R_special_account", (String) splAcNames.get("referral_payments_exp_act_name"));
    map.put("F_special_account", (String) splAcNames.get("referral_payments_exp_act_name"));
    map.put("C_special_account", (String) splAcNames.get("misc_payments_ac_name"));
    map.put("O_special_account", (String) splAcNames.get("outhouse_payments_exp_act_name"));
    return map;
  }

  /**
   * Adds the amount cost center wise.
   *
   * @param chargeHeadMap
   *          the charge head map
   * @param amount
   *          the amount
   * @param headName
   *          the head name
   * @param costCenterCode
   *          the cost center code
   */
  protected void addAmountCostCenterWise(Map<String, Map<String, BigDecimal>> chargeHeadMap,
      BigDecimal amount, String headName, String costCenterCode) {
    if (chargeHeadMap.containsKey(headName)) {
      Map<String, BigDecimal> costCenterMap = (Map<String, BigDecimal>) chargeHeadMap.get(headName);
      if (costCenterMap.containsKey(costCenterCode)) {
        amount = amount.add((BigDecimal) costCenterMap.get(costCenterCode));
        costCenterMap.put(costCenterCode, amount);
      } else {
        costCenterMap.put(costCenterCode, amount);
      }
    } else {
      Map<String, BigDecimal> costCenterMap = new HashMap<String, BigDecimal>();
      costCenterMap.put(costCenterCode, amount);
      chargeHeadMap.put(headName, costCenterMap);
    }
  }

  /**
   * Gets the real account cost center.
   *
   * @param costCenterCode
   *          the cost center code
   * @return the real account cost center
   */
  protected String getRealAccountCostCenter(String costCenterCode) {
    String costCenterType = (String) acPrefs.get("cost_center_basis");
    if (costCenterType.equals("Center Based")) {
      return costCenterCode;
    } else if (costCenterType.equals("Dept Based")) {
      return "None";
    }
    return null;
  }

  /**
   * Gets the income expense cost center.
   *
   * @param costCenterCode
   *          the cost center code
   * @return the income expense cost center
   */
  protected String getIncomeExpenseCostCenter(String costCenterCode) {
    // we do something special only for null/empty center codes
    if ((costCenterCode != null) && !costCenterCode.equals("")) {
      return costCenterCode;
    }

    String costCenterType = (String) acPrefs.get("cost_center_basis");

    if (costCenterType.equals("Center Based")) {
      return costCenterCode;
    } else if (costCenterType.equals("Dept Based")) {
      return "None";
    }
    return null;
  }

  /**
   * Load preferences.
   *
   * @throws SQLException
   *           the SQL exception
   */
  protected void loadPreferences() throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    try {
      acPrefs = acPrefsDAO.getRecord(con);
      voucherTypes = vTypesDAO.getRecord(con);
      partyNames = partyNamesDAO.getRecord(con);
      splAcNames = splAcNamesDAO.getRecord(con);

      incomingPatCostCenterCode = (String) ((BasicDynaBean) deptDao.findByKey("dept_id",
          (String) acPrefs.get("incoming_test_income_dept"))).get("cost_center_code");
      outsidePatCostCenterCode = (String) ((BasicDynaBean) deptDao.findByKey("dept_id",
          (String) acPrefs.get("outside_pat_income_dept"))).get("cost_center_code");
      pharmacyCostCenterCode = (String) ((BasicDynaBean) deptDao.findByKey("dept_id",
          (String) acPrefs.get("pharmacy_income_dept"))).get("cost_center_code");
      genPrefs = GenericPreferencesDAO.getAllPrefs();
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

}
