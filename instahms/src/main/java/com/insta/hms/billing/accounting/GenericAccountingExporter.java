package com.insta.hms.billing.accounting;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.master.Accounting.vouchertemplates.VoucherTemplateDAO;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericAccountingExporter.
 */
public abstract class GenericAccountingExporter extends AccountingExporter {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(GenericAccountingExporter.class);

  /** The export voucher type. */
  private String exportVoucherType = null;

  /** The voucher num field. */
  private String voucherNumField = null;

  /** The entity name. */
  private String entityName = null;

  /** The voucher type field. */
  private String voucherTypeField = null;

  /** The voucher date field. */
  private String voucherDateField = null;

  /** The atleast one voucher exported. */
  private boolean atleastOneVoucherExported = false;

  /** The template map. */
  private Map<String, Template> templateMap = new HashMap<String, Template>();

  /** The transaction type. */
  private String transactionType = null;

  /**
   * Instantiates a new generic accounting exporter.
   *
   * @param exportVoucherType
   *          the export voucher type
   * @param format
   *          the format
   */
  public GenericAccountingExporter(String exportVoucherType, String format) {
    super(format);
    this.exportVoucherType = exportVoucherType;
  }

  /**
   * Instantiates a new generic accounting exporter.
   *
   * @param exportVoucherType
   *          the export voucher type
   * @param voucherNumField
   *          the voucher num field
   * @param voucherDateField
   *          the voucher date field
   * @param entityName
   *          the entity name
   * @param voucherTypeField
   *          the voucher type field
   * @param transactionType
   *          the transaction type
   * @param format
   *          the format
   */
  public GenericAccountingExporter(String exportVoucherType, String voucherNumField,
      String voucherDateField, String entityName, String voucherTypeField, String transactionType,
      String format) {
    super(format);
    this.exportVoucherType = exportVoucherType;
    this.voucherNumField = voucherNumField;
    this.entityName = entityName;
    this.voucherTypeField = voucherTypeField;
    this.transactionType = transactionType;
    this.voucherDateField = voucherDateField;
  }

  /**
   * Gets the export voucher type.
   *
   * @return the export voucher type
   */
  public String getExportVoucherType() {
    return exportVoucherType;
  }

  /**
   * Export.
   *
   * @param centerId
   *          the center id
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
   * @param targetExportNo
   *          the target export no
   * @param sourceExportNo
   *          the source export no
   * @param stream
   *          the stream
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
  public boolean export(int centerId, Timestamp fromDate, Timestamp toDate, Integer accountGroup,
      java.sql.Date voucherDate, Map debitSummary, Map creditSummary,
      java.sql.Date voucherFromDate, java.sql.Date voucherToDate, int targetExportNo,
      int sourceExportNo, OutputStream stream) throws SQLException, IOException, TemplateException,
      ClassNotFoundException, AccountingException {
    setParameters(fromDate, toDate, accountGroup, voucherDate, voucherFromDate, voucherToDate,
        targetExportNo, sourceExportNo);
    helper = new AccountingHelper(voucherDate, debitSummary, creditSummary, voucherFromDate,
        voucherToDate, saveAgnstExportNo);
    List voucherList = getSourceVoucherList(sourceExportNo, getExportVoucherType(),
        getVoucherIdClass());
    export(centerId, voucherList, stream);
    return atleastOneVoucherExported;
  }

  /**
   * Export.
   *
   * @param centerId
   *          the center id
   * @param voucherList
   *          the voucher list
   * @param stream
   *          the stream
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws ClassNotFoundException
   *           the class not found exception
   */
  protected void export(int centerId, List voucherList, OutputStream stream) throws SQLException,
      IOException, TemplateException, ClassNotFoundException {

    List<BasicDynaBean> transactions = getTransactions(centerId, voucherList);

    logger.info("Exporting Vouchers : " + transactions.size());
    logger.info("Processing vouchers started @ :" + new Date());

    String voucherNoField = getVoucherNumberField();
    String previousVoucherNo = "";
    BasicDynaBean prevBean = null;

    VoucherProcessor aggregator = null;

    int index = 1;
    for (BasicDynaBean record : transactions) {
      String voucherNo = record.get(voucherNoField).toString();

      // Once we hit a new voucher no - it means the old one is complete
      // So we send the previous voucher to the XML
      if (!previousVoucherNo.equalsIgnoreCase(voucherNo)) {
        if (!previousVoucherNo.equals("") && prevBean != null) {
          sendVoucher(aggregator, prevBean.getMap(), stream);
        }
        // Start a new aggregator for the new voucher so that aggregation can start afresh.
        aggregator = getVoucherProcessor(voucherNo);
      }

      processTransaction(record.getMap(), voucherNoField, aggregator);
      // if it is a last voucher, then process and send the voucher.
      if (transactions.size() == index) {
        sendVoucher(aggregator, record.getMap(), stream);
      }

      // setup the previous vocuher for comparison in the next loop
      previousVoucherNo = voucherNo;
      prevBean = record;
      index++;

    }
    logger.info("Processing vouchers ended @ :" + new Date());
  }
  
  /**
   * Gets the source voucher list.
   *
   * @param sourceExportNo
   *          the source export no
   * @param exportVoucherType
   *          the export voucher type
   * @param clazz
   *          the clazz
   * @return the source voucher list
   * @throws SQLException
   *           the SQL exception
   */
  protected List getSourceVoucherList(int sourceExportNo, String exportVoucherType, Class clazz)
      throws SQLException {
    List list = importExportLogDao.getVouchers(sourceExportNo);
    importExportLogDao.getVouchers(list, exportVoucherType, clazz);
    return null;
  }

  /**
   * Sets the parameters.
   *
   * @param fromDate
   *          the from date
   * @param toDate
   *          the to date
   * @param accountGroup
   *          the account group
   * @param voucherDate
   *          the voucher date
   * @param voucherFromDate
   *          the voucher from date
   * @param voucherToDate
   *          the voucher to date
   * @param targetExportNo
   *          the target export no
   * @param sourceExportNo
   *          the source export no
   * @throws SQLException
   *           the SQL exception
   */
  protected void setParameters(Timestamp fromDate, Timestamp toDate, Integer accountGroup,
      java.sql.Date voucherDate, java.sql.Date voucherFromDate, java.sql.Date voucherToDate,
      int targetExportNo, int sourceExportNo) throws SQLException {
    this.fromDate = fromDate;
    this.toDate = toDate;
    this.accountGroup = accountGroup;
    this.voucherFromDate = voucherFromDate;
    this.voucherToDate = voucherToDate;
    this.saveAgnstExportNo = targetExportNo;
    this.fetchAgnstExportNo = sourceExportNo;
    this.voucherDate = voucherDate;
    loadPreferences();
  }

  /**
   * Gets the template.
   *
   * @param transactionType
   *          the transaction type
   * @return the template
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  protected Template getTemplate(String transactionType) throws IOException, SQLException {
    Template templateObj = templateMap.get(transactionType);
    if (null == templateObj) {
      String templateContent = new VoucherTemplateDAO().getCustomizedTemplate(transactionType);
      if (templateContent == null || templateContent.equals("")) {
        String file = "accounting/" + transactionType + "_voucher.ftl";
        templateObj = AppInit.getFmConfig().getTemplate(file);
      } else {
        StringReader reader = new StringReader(templateContent);
        templateObj = new Template("Accounting_" + transactionType + "_Template.ftl", reader,
            AppInit.getFmConfig());
      }
      templateMap.put(transactionType, templateObj);
    }
    return templateObj;
  }

  /**
   * Process transaction.
   *
   * @param record
   *          the record
   * @param voucherNumField
   *          the voucher num field
   * @param aggregator
   *          the aggregator
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws SQLException
   *           the SQL exception
   */
  protected void processTransaction(Map record, String voucherNumField, VoucherProcessor aggregator)
      throws IOException, TemplateException, SQLException {
    String transactionType = getTransactionType(record);
    getVoucherEntry(transactionType, record, voucherNumField, aggregator);
    return;
  }

  private static final String GET_AC_PREFS = "SELECT pharmacy_separate_entity, inter_co_vouchers,"
      + " all_centers_same_comp_name, tally_guid_prefix, separate_acc_for_out_vat,"
      + " separate_purcharse_acc_for_vat, separate_acc_for_in_vat, separate_sales_acc_for_vat,"
      + " pharmacy_sales_acc_include_vat, single_acc_for_item_and_bill_discounts,"
      + " ip_income_acc_prefix, ip_income_acc_suffix, op_income_acc_prefix, op_income_acc_suffix,"
      + " others_income_acc_prefix, others_income_acc_suffix, bill_reference, cost_center_basis,"
      + " CASE WHEN (cost_center_basis = 'Dept Based' "
      + "  AND phar_sales_to_hosp_patient = 'Pharmacy Dept') THEN pharmacy_income_dept ELSE '' END"
      + " income_dept_pharmacy, CASE cost_center_basis WHEN 'Dept Based' THEN"
      + " incoming_test_income_dept ELSE '' END income_dept_incoming_test, CASE cost_center_basis"
      + " WHEN 'Dept Based' THEN outside_pat_income_dept ELSE '' END income_dept_osp"
      + " FROM hosp_accounting_prefs LIMIT 1";

  private static final String GET_PARTY_NAMES = "select * from hosp_party_account_names limit 1";

  private static final String GET_SPLAC_NAMES = "SELECT"
      + " counter_receipts_ac_name AS counter_receipts_ac_name,"
      + " counter_receipts_ac_name_ip AS counter_receipts_ip,"
      + " counter_receipts_ac_name_op AS counter_receipts_op,"
      + " counter_receipts_ac_name_others AS counter_receipts_others,"
      + " tds_receipt_ac_name AS tds_receipts, tds_payment_ac_name AS tds_payments,"
      + " pharma_claim_ac_name AS pharma_claims, pharma_receipts_ac_name AS pharma_receipts,"
      + " pharma_refunds_ac_name AS pharma_refunds, pharma_preturns_ac_name AS pharma_returns,"
      + " pharm_sales_round_off_ac_name AS pharma_sales_round_off,"
      + " pharm_sales_disc_ac_name AS pharma_sales_discounts,"
      + " pharm_inv_disc_ac_name AS pharma_inv_discounts,"
      + " pharm_inv_round_off_ac_name AS pharma_inv_round_off,"
      + " pharm_inv_other_charges_ac_name AS pharma_inv_other_charges,"
      + " pharmacy_cess_ac_name AS pharma_cess, inv_purchase_ac_name AS inv_purchase,"
      + " inv_preturns_ac_name AS inv_returns, doctor_payments_exp_ac_name AS exp_doctor_payments,"
      + " referral_payments_exp_act_name AS exp_referral_payments,"
      + " prescribing_doctor_payments_exp_ac_name AS exp_prescribing_doctor_payments,"
      + " outhouse_payments_exp_act_name AS exp_outhouse_payments,"
      + " transfer_expenses AS exp_transfers, misc_payments_ac_name AS exp_misc_payments,"
      + " outgoing_vat_ac_name AS outgoing_vat, incoming_vat_ac_name AS incoming_vat,"
      + " incoming_cst_ac_name AS incoming_cst, outgoing_ced_ac_name AS outgoing_ced,"
      + " claims_ac_name AS hospital_claims, hospital_transfer_act_name AS hospital_transfers,"
      + " writeoff_ac_name AS writeoff, patient_deposit_ac_name AS patient_deposits,"
      + " patient_points_ac_name AS patient_points FROM hosp_special_account_names LIMIT 1";

  /**
   * Gets the voucher entry.
   *
   * @param transactionType the transaction type
   * @param data            the data
   * @param voucherNumField the voucher num field
   * @param aggregator      the aggregator
   * @return the voucher entry
   * @throws IOException       Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws SQLException      the SQL exception
   */
  private void getVoucherEntry(String transactionType, Map data, String voucherNumField,
      VoucherProcessor aggregator) throws IOException, TemplateException, SQLException {
    Template templateObj = getTemplate(transactionType); // + "_voucher.ftl");
    if (null != templateObj) {
      Map dataMap = new HashMap();
      dataMap.put("acprefs", DataBaseUtil.queryToDynaBean(GET_AC_PREFS, new Object[] {}));
      dataMap.put("partynames", DataBaseUtil.queryToDynaBean(GET_PARTY_NAMES, new Object[] {}));
      dataMap.put("specialnames", DataBaseUtil.queryToDynaBean(GET_SPLAC_NAMES, new Object[] {}));
      dataMap.put("record", data);
      dataMap.put("voucher_num_field", voucherNumField);
      dataMap.put("voucher", aggregator);
      dataMap.put("ledgerEntry", aggregator.getLedgerEntryProcessor());
      dataMap.put("settings", getSettingsMap());
      dataMap.put("depts", getDeptsMap());
      Writer writer = new StringWriter();
      templateObj.process(dataMap, writer);
    }

    return;

  }

  /**
   * Gets the current schema.
   *
   * @return the current schema
   */
  private String getCurrentSchema() {
    HttpSession session = RequestContext.getSession();
    String schema = null;
    if (null != session) {
      schema = (String) session.getAttribute("sesHospitalId");
    } else {
      schema = RequestContext.getSchema();
    }
    return schema;
  }

  /**
   * Gets the settings map.
   *
   * @return the settings map
   * @throws SQLException
   *           the SQL exception
   */
  protected Map getSettingsMap() throws SQLException {
    return new HashMap();
  }

  /**
   * Gets the depts map.
   *
   * @return the depts map
   * @throws SQLException
   *           the SQL exception
   */
  protected Map getDeptsMap() throws SQLException {
    return new HashMap();
  }

  /**
   * Send voucher.
   *
   * @param aggregator
   *          the aggregator
   * @param data
   *          the data
   * @param stream
   *          the stream
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws SQLException
   *           the SQL exception
   */
  protected void sendVoucher(VoucherProcessor aggregator, Map data, OutputStream stream)
      throws IOException, TemplateException, SQLException {
    Voucher voucher = null;
    if (null == aggregator || null == data) {
      logger.error("invalid parameters");
      return;
    }

    // use the id class to do the conversion
    String voucherNo = data.get(getVoucherNumberField()).toString();
    voucher = createVoucher(data);
    voucher.setNarration(aggregator.getNarration(voucherNo));

    boolean ledgersCreated = false;
    Map<String, Map<String, BigDecimal>> costCenterMap = aggregator.getCostCentersMap(voucherNo);
    for (Map.Entry<String, LedgerEntry> entry : aggregator.getLedgerEntries(voucherNo)) {
      LedgerEntry lentry = entry.getValue();
      Map billAllocations = null;
      BigDecimal amt = lentry.getAmount().setScale((Integer) genPrefs.get("after_decimal_digits"),
          RoundingMode.HALF_EVEN);

      if (amt.compareTo(BigDecimal.ZERO) != 0) {
        boolean isDebit = lentry.getType().equalsIgnoreCase("D");
        if (amt.compareTo(BigDecimal.ZERO) < 0) {
          isDebit = !isDebit; // reverse the debit credit if the amount is < 0
          amt = amt.negate();
        }
        if (lentry.getReferenceName() != null) {
          billAllocations = fillBillAllocations(lentry.getReferenceName(), lentry.getIsNewRef(),
              amt, isDebit);
        }
        List costCenterList = new ArrayList();
        if (costCenterMap.get(lentry.getAccountName()) != null) {
          for (Map.Entry<String, BigDecimal> ccEntry : costCenterMap.get(lentry.getAccountName())
              .entrySet()) {
            BigDecimal centerAmt = ccEntry.getValue().setScale(
                (Integer) genPrefs.get("after_decimal_digits"), RoundingMode.HALF_EVEN);
            if (centerAmt.compareTo(BigDecimal.ZERO) < 0) {
              centerAmt = centerAmt.negate();
            }
            costCenterList.add(costCenterAllocations(ccEntry.getKey(), centerAmt, isDebit));
          }
        }
        helper.addDebitOrCredit(getEntityName(), lentry.getLedgerType(), voucher, entry.getKey(),
            amt, isDebit, billAllocations, costCenterList);
        ledgersCreated = true;
      }
    }
    if (ledgersCreated) {
      sendVoucher(voucher, data, stream);
    }
  }

  /**
   * Send voucher.
   *
   * @param voucher
   *          the voucher
   * @param data
   *          the data
   * @param stream
   *          the stream
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws SQLException
   *           the SQL exception
   */
  protected void sendVoucher(Voucher voucher, Map data, OutputStream stream) throws IOException,
      TemplateException, SQLException {
    if (null != voucher) {
      String voucherNumField = getVoucherNumberField();
      sendVoucher(voucher, stream, format, getExportVoucherType(), data.get(voucherNumField)
          .toString());
    }
  }

  /**
   * Send voucher.
   *
   * @param voucher
   *          the voucher
   * @param stream
   *          the stream
   * @param format
   *          the format
   * @param exportVoucherType
   *          the export voucher type
   * @param voucherNo
   *          the voucher no
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws SQLException
   *           the SQL exception
   */
  @SuppressWarnings("unchecked")
  public void sendVoucher(Voucher voucher, OutputStream stream, String format,
      String exportVoucherType, String voucherNo) throws IOException, TemplateException,
      SQLException {
    if (voucher == null) {
      return;
    }
    if (stream == null) {
      return; // no need to send voucher. it is for summary.
    }

    // exporting the voucher also depends on the voucherfromdate and vouchertodate
    // to avoid the out of date range error while exporting the voucher to the tally.
    // voucher date should be greater than or equal to the voucherfromdate(if voucherfromdate
    // exists).
    // voucher date should be less than or equal to the vouchertodate(if vouchertodate exists).
    if (!isVoucherInDateRange(voucher)) {
      return;
    }

    if (format.equals("tallyxml")) {
      if (saveAgnstExportNo != 0) {
        AccountingHelper.saveExportLog(voucherNo, exportVoucherType, saveAgnstExportNo);
      }
      atleastOneVoucherExported = true;
    }
    processVoucher(voucher, format, stream);
  }
  
  /**
   * Gets the voucher processor.
   *
   * @param voucherNo
   *          the voucher no
   * @return the voucher processor
   */
  protected VoucherProcessor getVoucherProcessor(String voucherNo) {
    return new VoucherProcessor(voucherNo);
  }

  // TODO : This method does not belong in this class. Should be moved to
  // DynaBeanBuilder along with loadBean. The two should be clubbed into
  // a single method DynaBeanBuilder.build(ResultSet rs);

  /**
   * Gets the bean.
   *
   * @param rs
   *          the rs
   * @return the bean
   * @throws SQLException
   *           the SQL exception
   * @throws ClassNotFoundException
   *           the class not found exception
   */
  protected DynaBeanBuilder getBean(ResultSet rs) throws SQLException, ClassNotFoundException {
    ResultSetMetaData rsmd = rs.getMetaData();
    DynaBeanBuilder bean = new DynaBeanBuilder();
    for (int i = 0; i < rsmd.getColumnCount(); i++) {
      bean.add(rsmd.getColumnName(i + 1), Class.forName(rsmd.getColumnClassName(i + 1)));
    }
    return bean;
  }

  /**
   * Load bean.
   *
   * @param rs
   *          the rs
   * @param record
   *          the record
   * @return the basic dyna bean
   * @throws SQLException
   *           the SQL exception
   */
  protected BasicDynaBean loadBean(ResultSet rs, BasicDynaBean record) throws SQLException {
    DynaProperty[] properties = record.getDynaClass().getDynaProperties();
    for (int i = 0; i < properties.length; i++) {
      Object colValue = rs.getObject(properties[i].getName());
      if (colValue != null) {
        record
            .set(properties[i].getName(), ConvertUtils.convert(colValue, properties[i].getType()));
      }
    }
    return record;
  }

  /**
   * Gets the entity name.
   *
   * @return the entity name
   */
  protected String getEntityName() {
    return entityName;
  }

  /**
   * Gets the voucher number field.
   *
   * @return the voucher number field
   */
  protected String getVoucherNumberField() {
    return voucherNumField;
  }

  /**
   * Gets the voucher id class.
   *
   * @return the voucher id class
   */
  // TODO : This is a bit clunky - need to do this better
  protected Class getVoucherIdClass() {
    return String.class;
  }

  // protected abstract Voucher createVoucher(Map data);

  /**
   * Gets the transaction type.
   *
   * @param voucher
   *          the voucher
   * @return the transaction type
   */
  protected String getTransactionType(Map voucher) {
    return transactionType;
  }

  /**
   * Gets the transactions.
   *
   * @param centerId
   *          the center id
   * @param voucherList
   *          the voucher list
   * @return the transactions
   * @throws SQLException
   *           the SQL exception
   */
  protected abstract List<BasicDynaBean> getTransactions(int centerId, List voucherList)
      throws SQLException;

  /**
   * Gets the accounting voucher type.
   *
   * @param data
   *          the data
   * @return the accounting voucher type
   */
  protected String getAccountingVoucherType(Map data) {
    return (null != voucherTypeField) ? (String) voucherTypes.get(voucherTypeField) : null;
  }

  /**
   * Creates the voucher.
   *
   * @param dt
   *          the dt
   * @param idStr
   *          the id str
   * @param vchType
   *          the vch type
   * @return the voucher
   */
  protected Voucher createVoucher(java.util.Date dt, String idStr, String vchType) {
    return createVoucher(dt, idStr, vchType, null, acPrefs);
  }

  /**
   * Creates the voucher.
   *
   * @param dt
   *          the dt
   * @param idStr
   *          the id str
   * @param vchType
   *          the vch type
   * @param narration
   *          the narration
   * @param acPrefs
   *          the ac prefs
   * @return the voucher
   */
  protected Voucher createVoucher(java.util.Date dt, String idStr, String vchType,
      String narration, BasicDynaBean acPrefs) {
    Voucher voucher = new Voucher();
    String guidPrefix = (String) acPrefs.get("tally_guid_prefix");
    voucher.setRemoteId(guidPrefix + idStr);
    voucher.setVoucherType(vchType);
    voucher.setAction("Create");
    if (voucherDate != null) {
      voucher.setDate(voucherDate);
    } else {
      voucher.setDate(dt);
    }
    SimpleDateFormat instaStyleFormatter = new SimpleDateFormat("dd-MM-yyyy");
    voucher.setFormattedDate(instaStyleFormatter.format(voucher.getDate()));
    voucher.setGuid(guidPrefix + idStr);
    voucher.setVoucherNumber(idStr);
    voucher.setNarration(narration);
    voucher.setVoucherTypeName(vchType);
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
    voucher.setEffectiveDate(dateFormatter.format(voucher.getDate()));

    return voucher;
  }

  /**
   * Creates the voucher.
   *
   * @param data
   *          the data
   * @return the voucher
   */
  // @Override
  protected Voucher createVoucher(Map data) {
    String dtField = getVoucherDateField();
    String numField = getVoucherNumberField();
    return createVoucher((java.sql.Date) data.get(dtField), data.get(numField).toString(),
        getAccountingVoucherType(data));
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
  private Map fillBillAllocations(String name, boolean newRef, BigDecimal amount, boolean isDebit) {
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
  private Map costCenterAllocations(String name, BigDecimal amount, boolean isDebit) {

    Map map = new HashMap();
    map.put("NAME", name);
    map.put("AMOUNT", (isDebit ? amount.negate() : amount).toString());
    // negativeAMOUNT is used while showing export details on screen.
    // it is not used in xml.
    map.put("negativeAMOUNT", (!isDebit ? amount.negate() : amount).toString());
    return map;
  }
  
  /**
   * Checks if is voucher in date range.
   *
   * @param voucher
   *          the voucher
   * @return true, if is voucher in date range
   */
  private boolean isVoucherInDateRange(Voucher voucher) {

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
   * Process voucher.
   *
   * @param voucher
   *          the voucher
   * @param format
   *          the format
   * @param stream
   *          the stream
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   */
  private void processVoucher(Voucher voucher, String format, OutputStream stream)
      throws IOException, TemplateException {
    AccountingVoucherFormatter formatter = getVoucherFormatter(format);
    formatter.format(voucher, stream);
  }

  /**
   * Gets the voucher formatter.
   *
   * @param format
   *          the format
   * @return the voucher formatter
   */
  protected AccountingVoucherFormatter getVoucherFormatter(String format) {
    return AccountingHelper.getFormatter(format);
  }

  /**
   * Gets the voucher date field.
   *
   * @return the voucher date field
   */
  public String getVoucherDateField() {
    return voucherDateField;
  }
}
