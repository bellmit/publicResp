package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.core.billing.AllocationRepository;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.core.billing.DepositType;
import com.insta.hms.core.fa.AccountingJobScheduler;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/*
 * Contains data access methods for storing/retrieving Receipts and similar objects,
 * for tables bill_receipts and bill_refund
 */
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ReceiptRelatedDAO {

  static Logger logger = LoggerFactory.getLogger(ReceiptRelatedDAO.class);

  private AllocationService allocationService = (AllocationService) ApplicationContextProvider
      .getApplicationContext().getBean("allocationService");
  private AccountingJobScheduler accountingJobScheduler = ApplicationContextProvider
      .getBean(AccountingJobScheduler.class);

  private Connection con = null;

  public ReceiptRelatedDAO(Connection con) {
    this.con = con;
  }

  /*
   * Receipt number generation methods: this is based on a set of preferences on how the type
   * controls the prefix and sequence to use.
   */
  private static final String RECEIPT_SEQ_PREFS = " SELECT pattern_id::text FROM hosp_receipt_seq_prefs "
      + " WHERE priority = (SELECT min(priority) FROM hosp_receipt_seq_prefs "
      + "  WHERE (bill_type = ? or bill_type ='*') AND "
      + "        (visit_type = ? or visit_type = '*') AND "
      + "        (restriction_type = ? or restriction_type = '*') AND "
      + "        (payment_type = ?) AND (center_id = ? OR center_id = 0)) ";

  public static final String getReceiptNoPattern(String billType, String visitType,
      String restrictionType, String paymentType, int centerId) throws SQLException {

    Connection con = null;
    PreparedStatement stmt = null;

    try {
      con = DataBaseUtil.getConnection();
      stmt = con.prepareStatement(RECEIPT_SEQ_PREFS);
      stmt.setString(1, billType);
      stmt.setString(2, visitType);
      stmt.setString(3, restrictionType);
      stmt.setString(4, paymentType);
      stmt.setInt(5, centerId);

      List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(stmt);
      BasicDynaBean b = l.get(0);
      return (String) b.get("pattern_id");
    } finally {
      DataBaseUtil.closeConnections(con, stmt);
    }
  }

  public static final String getNextReceiptNo(String billType, String visitType,
      String restrictionType, String paymentType, int centerId) throws SQLException {

    String patternId = getReceiptNoPattern(billType, visitType, restrictionType, paymentType,
        centerId);
    return DataBaseUtil.getNextPatternId(patternId);
  }

  private static final String GET_BILL_RECEIPTS_REFUNDS = "SELECT r.receipt_id, br.receipt_no, "
      + " r.receipt_type, br.bill_no, r.amount, r.display_date, r.counter, c.counter_type, "
      + " r.payment_mode_id, pm.payment_mode, r.card_type_id, cm.card_type, r.bank_name, "
      + " r.reference_no, r.remarks, br.username, r.bank_batch_no,  r.card_auth_code, "
      + " r.card_holder_name, r.mob_number, r.totp, "
      + " r.currency_id, r.exchange_rate, r.exchange_date, r.tpa_id, r.is_settlement,"
      + " r.currency_amt, fc.currency, r.card_number, r.card_exp_date as card_expdate, "
      + " sponsor_index,r.credit_card_commission_amount, r.credit_card_commission_percentage  "
      + " FROM receipts r JOIN bill_receipts br ON (r.receipt_id = br.receipt_no)"
      + "   JOIN counters c ON (c.counter_id = r.counter) "
      + "   JOIN payment_mode_master pm ON (pm.mode_id = r.payment_mode_id) "
      + "   LEFT JOIN card_type_master cm ON (cm.card_type_id = r.card_type_id) "
      + "   LEFT JOIN foreign_currency fc ON (fc.currency_id = r.currency_id)"
      + " WHERE bill_no=? AND r.receipt_type=? ";

  public List getBillReceipts(String billNo, String paymentType) throws SQLException {

    ArrayList list = new ArrayList();

    try (PreparedStatement ps = con.prepareStatement(GET_BILL_RECEIPTS_REFUNDS);) {
      ps.setString(1, billNo);
      ps.setString(2, paymentType);
      try (ResultSet rs = ps.executeQuery();) {
        while (rs.next()) {
          Receipt rec = new Receipt();
          populateReceipt(rec, rs);
          list.add(rec);
        }
      }
    }
    return list;
  }

  private static void populateReceipt(Receipt rec, ResultSet rs) throws SQLException {
    rec.setReceiptNo(rs.getString("receipt_no"));
    rec.setBillNo(rs.getString("bill_no"));
    rec.setAmount(rs.getBigDecimal("amount"));
    rec.setReceiptDate(rs.getTimestamp("display_date"));
    rec.setCounter(rs.getString("counter"));
    rec.setPaymentModeId(rs.getInt("payment_mode_id"));
    rec.setPaymentMode(rs.getString("payment_mode"));
    rec.setCardTypeId(rs.getInt("card_type_id"));
    rec.setCardType(rs.getString("card_type"));
    rec.setReferenceNo(rs.getString("reference_no"));
    rec.setBankName(rs.getString("bank_name"));
    rec.setUsername(rs.getString("username"));
    rec.setRemarks(rs.getString("remarks"));
    String tpaId = rs.getString("tpa_id");
    String paymentType = "R";
    String receiptType = rs.getString("receipt_type");
    if (null != tpaId && !tpaId.isEmpty()) {
      paymentType = "S";
    }
    if (receiptType.equals("F")) {
      paymentType = "F";
    }
    rec.setPaymentType(paymentType);
    rec.setTpaId(tpaId);
    rec.setIsSettlement(Boolean.parseBoolean(rs.getString("is_settlement")));
    rec.setReceiptType(receiptType);
    rec.setCounterType(rs.getString("counter_type"));
    rec.setBankBatchNo(rs.getString("bank_batch_no"));
    rec.setCardAuthCode(rs.getString("card_auth_code"));
    rec.setCardHolderName(rs.getString("card_holder_name"));
    rec.setCurrencyId(rs.getInt("currency_id"));
    rec.setExchangeRate(rs.getBigDecimal("exchange_rate"));
    rec.setExchangeDateTime(rs.getTimestamp("exchange_date"));
    rec.setCurrencyAmt(rs.getBigDecimal("currency_amt"));
    rec.setCurrency(rs.getString("currency"));
    rec.setCardNumber(rs.getString("card_number"));
    rec.setCardExpDate(rs.getDate("card_expdate"));
    rec.setSponsorIndex(rs.getString("sponsor_index"));
    rec.setCommissionAmount(rs.getBigDecimal("credit_card_commission_amount"));
    rec.setCommissionPercentage(rs.getBigDecimal("credit_card_commission_percentage"));
    rec.setMobNumber(rs.getString("mob_number"));
    rec.setTotp(rs.getString("totp"));
  }

  public static final String INSERT_BILL_RECEIPT = "INSERT INTO bill_receipts( "
      + " receipt_no, bill_no, display_date, username, sponsor_index, sponsor_id) "
      + " VALUES (?,?,?,?,?,?)";

   boolean createBillReceipt(Receipt receiptDtoObject) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(INSERT_BILL_RECEIPT);) {
      int i = 1;
      ps.setString(i++, receiptDtoObject.getReceiptNo());
      ps.setString(i++, receiptDtoObject.getBillNo());
      ps.setTimestamp(i++, receiptDtoObject.getReceiptDate());
      ps.setString(i++, receiptDtoObject.getUsername());
      ps.setString(i++, receiptDtoObject.getSponsorIndex());
      ps.setString(i, receiptDtoObject.getSponsorId());
  
      int result = ps.executeUpdate();
      if (result != 1) {
        logger.error("Error updating Bill Receipts/Refunds ");
        return false;
      }
    }

    Map<String, Map<String, String>> receiptUsageMap = new HashMap<>();
    Map<String, String> entityMap = new HashMap<>();
    entityMap.put(receiptDtoObject.getBillNo(), BillConstants.Restrictions.BILL_NO);
    receiptUsageMap.put(receiptDtoObject.getReceiptNo(), entityMap);
    return createReceiptUsage(receiptUsageMap);
  }

  public static final String INSERT_RECEIPT_USAGE = "INSERT INTO receipt_usage( "
      + " receipt_id, entity_id, entity_type) VALUES (?,?,?)";

  public boolean createReceiptUsage(Map<String, Map<String, String>> receiptUsageMap)
      throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(INSERT_RECEIPT_USAGE)) {

      if (!CollectionUtils.isEmpty(receiptUsageMap)) {
        for (Entry<String, Map<String, String>> entry : receiptUsageMap.entrySet()) {
          if (!CollectionUtils.isEmpty(entry.getValue())) {
            for (Entry<String, String> entityMap : entry.getValue().entrySet()) {
              int i = 1;
              ps.setString(i++, entry.getKey());
              ps.setString(i++, entityMap.getKey());
              ps.setString(i++, entityMap.getValue());
  
              int result = ps.executeUpdate();
              if (result != 1) {
                logger.error("Error updating Receipts Usage ");
                return false;
              }
            }
          }
        }
      }
    }
    return true;
  }

  public static final String GET_PATIENT_PACKAGE_ID = "SELECT pat_package_id FROM patient_packages WHERE mr_no = ? AND package_id = ? AND status = 'P'";

  public Integer getPatientPackageId(String mrNo, Integer packageId) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_PATIENT_PACKAGE_ID);) {
      int i = 1;
      ps.setString(i++, mrNo);
      ps.setInt(i++, packageId);
  
      Integer patPackageId = null;
      try (ResultSet rs = ps.executeQuery();) {
        while (rs.next()) {
          patPackageId = rs.getInt("pat_package_id");
        }
      }
      return patPackageId;
    }
  }

  public static final String GET_SETOFF_AMOUNT = "SELECT COALESCE(sum(b.allocated_amount),0) as allt_amt FROM bill_receipts b "
      + " JOIN receipts r ON (b.receipt_no = r.receipt_id) "
      + " WHERE r.mr_no = ? and r.is_deposit = true";

  public BigDecimal getSetoffAmount(String mrNo) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_SETOFF_AMOUNT);) {
      ps.setString(1, mrNo);
  
      BigDecimal allocatedAmount = BigDecimal.ZERO;
      try (ResultSet rs = ps.executeQuery();) {
        while (rs.next()) {
          allocatedAmount = rs.getBigDecimal("allt_amt");
        }
      }
      return allocatedAmount;
    }
  }
  
  public static final String GET_BILL_SETOFF_AMOUNT = "SELECT COALESCE(sum(b.allocated_amount),0) as total_setoff FROM bill_receipts b "
      + " JOIN receipts r ON (b.receipt_no = r.receipt_id) "
      + " WHERE b.bill_no = ? and r.is_deposit = true";
  
  public BigDecimal getBillSetoff(String billNo) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_BILL_SETOFF_AMOUNT);) {
      ps.setString(1, billNo);
  
      BigDecimal totalSetoff = BigDecimal.ZERO;
      try (ResultSet rs = ps.executeQuery();) {
        while (rs.next()) {
          totalSetoff = rs.getBigDecimal("total_setoff");
        }
      }
      return totalSetoff;
    }
  }

  public static final String GET_TOTAL_DEPOSIT = "SELECT COALESCE(sum(amount), 0) as amt FROM receipts "
      + " WHERE mr_no = ? and is_deposit = true AND realized='Y'";

  public BigDecimal getTotalDeposit(String mrNo) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_TOTAL_DEPOSIT);) {
      ps.setString(1, mrNo);
  
      BigDecimal totalDeposit = BigDecimal.ZERO;
      try (ResultSet rs = ps.executeQuery();) {
        while (rs.next()) {
          totalDeposit = rs.getBigDecimal("amt");
        }
      }
      return totalDeposit;
    }
  }

  public static final String CHECK_FOR_MR_NO_EXISTANCE = "SELECT * FROM deposit_setoff_total WHERE mr_no = ? ";

  public static final String UPDATE_DEPOSIT_SETOFF_TOTAL = "UPDATE deposit_setoff_total SET hosp_total_deposits = ?, "
      + " hosp_total_setoffs = ?, hosp_total_balance = ?, hosp_total_tax_amount =?, hosp_total_setoffs_tax_amount  =? "
      + " WHERE mr_no=?";

  public static final String INSERT_DEPOSIT_SETOFF_TOTAL = "INSERT INTO deposit_setoff_total( "
      + " mr_no, hosp_total_deposits, hosp_total_setoffs, hosp_total_balance, hosp_total_tax_amount, hosp_total_setoffs_tax_amount) "
      + " VALUES (?,?,?,?,?,?)";

  public boolean createDepositSetoffTotal(String mrNo, BigDecimal balance, BigDecimal setOffAmount, 
       BigDecimal totalDepositTaxAmount, BigDecimal totalTaxSetOffAmount)
      throws SQLException {

    try (PreparedStatement ps = con.prepareStatement(CHECK_FOR_MR_NO_EXISTANCE);) {
      ps.setString(1, mrNo);
  
      try (ResultSet rs = ps.executeQuery();) {
        if (!rs.next()) {
          try (PreparedStatement prepSt = con.prepareStatement(INSERT_DEPOSIT_SETOFF_TOTAL);) {
            int i = 1;
            prepSt.setString(i++, mrNo);
            prepSt.setBigDecimal(i++, balance);
            prepSt.setBigDecimal(i++, setOffAmount);
            prepSt.setBigDecimal(i++, balance.subtract(setOffAmount));
            prepSt.setBigDecimal(i++, totalDepositTaxAmount);
            prepSt.setBigDecimal(i, totalTaxSetOffAmount);
    
            int result = prepSt.executeUpdate();
            if (result != 1) {
              logger.error("Error Inserting new row in Deposit Setoff Total");
              return false;
            }
          }
        } else {
          try (PreparedStatement prepSt = con.prepareStatement(UPDATE_DEPOSIT_SETOFF_TOTAL);) {
            int i = 1;
            prepSt.setBigDecimal(i++, balance);
            prepSt.setBigDecimal(i++, setOffAmount);
            prepSt.setBigDecimal(i++, balance.subtract(setOffAmount));
            prepSt.setBigDecimal(i++, totalDepositTaxAmount);
            prepSt.setBigDecimal(i++, totalTaxSetOffAmount);
            prepSt.setString(i, mrNo);
      
            int result = prepSt.executeUpdate();
            if (result != 1) {
              logger.error("Error updating Deposit Setoff Total");
              return false;
            }
          }
        }
      }
    }
    return true;
  }

  public boolean createReceipt(Receipt receiptDtoObject) throws SQLException {
    int paymentModeId = receiptDtoObject.getPaymentModeId();
    String billNo = receiptDtoObject.getBillNo();
    String receiptType = receiptDtoObject.getReceiptType();
    if (BillConstants.depositSetoffPaymentModes.contains(paymentModeId)) {
      // In this case a receipt should not be created. we should only add the amount to bill
      // receipt's allocated amount.
      String depositType = "";
      switch (paymentModeId) {
        case -7:
          depositType = DepositType.IP;
          break;
        case -8:
          depositType = DepositType.PACKAGE;
          break;
        default:
          depositType = DepositType.GENERAL;
      }
      BigDecimal amount = receiptDtoObject.getAmount();
      // Commit previous changes so that the bill table is not locked by struts.
      con.commit();
      if ("F".equals(receiptType)) {
        // If the receipt type if F the amount will be negative but we need to send a positive amount to be reduced.
        allocationService.reduceDepositSetoffAmount(billNo, depositType, amount.negate());
      } else {
        allocationService.splitDepositAmountToSetOff(billNo, depositType, amount);
      }
      allocationService.updateBillTotal(billNo);
      return true;
    }

    boolean toReturn = createReceiptEntry(receiptDtoObject) && createBillReceipt(receiptDtoObject);
    // Have to commit so that the receipt is available to hibernate.
    con.commit(); 
    
    Map<String, Object> receiptData = new HashMap<>();
    receiptData.put("receiptId", receiptDtoObject.getReceiptNo());
    receiptData.put("reversalsOnly", Boolean.FALSE);
    accountingJobScheduler.scheduleAccountingForReceipt(receiptData);

    allocationService.updateBillTotal(billNo);
    return toReturn;
  }

  private static final String INSERT_RECEIPT = "INSERT INTO receipts( "
      + " receipt_id, receipt_type, is_settlement, is_deposit, realized, mr_no,center_id, payer_name, payer_mobile_number, points_redeemed, "
      + " amount, display_date, counter, payment_mode_id , card_type_id, "
      + " bank_name, reference_no, created_by, modified_by, remarks, tds_amount, paid_by, bank_batch_no, "
      + " card_auth_code, card_holder_name, currency_id, exchange_rate, exchange_date, "
      + " currency_amt, card_number, card_exp_date, credit_card_commission_percentage,"
      + " credit_card_commission_amount, tpa_id, mob_number, totp, edc_imei, unallocated_amount, total_tax_rate, store_retail_customer_id, incoming_visit_id, payment_transaction_id) "
      + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

  public boolean createReceiptEntry(Receipt receiptDtoObject) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(INSERT_RECEIPT);) {
      int i = 1;
      ps.setString(i++, receiptDtoObject.getReceiptNo());

      String receiptType = receiptDtoObject.getReceiptType();
      BigDecimal amount = receiptDtoObject.getAmount();
      BigDecimal tdsAmount = receiptDtoObject.getTdsAmt();

      String newReceiptType = "R";
      if ("F".equals(receiptType)) {
        newReceiptType = "F";
      }
      if ("W".equals(receiptType)) {
        newReceiptType = "W";
      }
      ps.setString(i++, newReceiptType);

      ps.setBoolean(i++, receiptDtoObject.getisSettlement());
      ps.setBoolean(i++, receiptDtoObject.getIsDeposit());
      ps.setString(i++, receiptDtoObject.getRealized());
      ps.setString(i++, receiptDtoObject.getMrno());
      ps.setInt(i++, receiptDtoObject.getCenterId());
      ps.setString(i++, receiptDtoObject.getPayerName());
      ps.setString(i++, receiptDtoObject.getPayerMobileNumber());
      ps.setInt(i++, receiptDtoObject.getPointsRedeemed());

      ps.setBigDecimal(i++, amount);
      ps.setTimestamp(i++, receiptDtoObject.getReceiptDate());
      ps.setString(i++, receiptDtoObject.getCounter());
      ps.setInt(i++, receiptDtoObject.getPaymentModeId());
      ps.setInt(i++, receiptDtoObject.getCardTypeId());
      ps.setString(i++, receiptDtoObject.getBankName());
      ps.setString(i++, receiptDtoObject.getReferenceNo());
      ps.setString(i++, receiptDtoObject.getUsername());
      ps.setString(i++, receiptDtoObject.getUsername());
      ps.setString(i++, receiptDtoObject.getRemarks());
      ps.setBigDecimal(i++, tdsAmount);
      ps.setString(i++, receiptDtoObject.getPaidBy());
      ps.setString(i++, receiptDtoObject.getBankBatchNo());
      ps.setString(i++, receiptDtoObject.getCardAuthCode());
      ps.setString(i++, receiptDtoObject.getCardHolderName());
      ps.setInt(i++, receiptDtoObject.getCurrencyId());
      ps.setBigDecimal(i++, receiptDtoObject.getExchangeRate());
      ps.setTimestamp(i++, receiptDtoObject.getExchangeDateTime());
      ps.setBigDecimal(i++, receiptDtoObject.getCurrencyAmt());
      ps.setString(i++, receiptDtoObject.getCardNumber());
      ps.setDate(i++, receiptDtoObject.getCardExpDate());
      ps.setBigDecimal(i++, receiptDtoObject.getCommissionPercentage());
      ps.setBigDecimal(i++, receiptDtoObject.getCommissionAmount());
      ps.setString(i++, receiptDtoObject.getSponsorId());
      ps.setString(i++, receiptDtoObject.getMobNumber());
      ps.setString(i++, receiptDtoObject.getTotp());
      ps.setString(i++, receiptDtoObject.getEdcIMEI());

      BigDecimal unallocatedAmount = receiptDtoObject.getAmount().add(receiptDtoObject.getTdsAmt());

      ps.setBigDecimal(i++, unallocatedAmount);
      ps.setBigDecimal(i++, receiptDtoObject.getTotalTax());
      ps.setString(i++, receiptDtoObject.getStoreRetailCustomerId());
      ps.setString(i++, receiptDtoObject.getIncomingVisitId());
      if (receiptDtoObject.getPaymentTransactionId() == null) {
    	    ps.setNull(i, java.sql.Types.INTEGER);
      }
      else {
    	    ps.setInt(i, receiptDtoObject.getPaymentTransactionId());
      }
      int result = ps.executeUpdate();
      if (result != 1) {
        logger.error("Error updating Bill Receipts/Refunds ");
        return false;
      }
    }
    return true;
  }

  public static final String RECEIPTS_EXT_QUERY_FIELDS = "SELECT payment_type, recpt_type, receipt_no, bill_no, amount, display_date, counter, counter_type, "
      + " payment_mode, bank_name, reference_no, username, remarks, payment_mode_id, "
      + " visit_id, visit_type, bill_type, status, " + " mr_no, patient_full_name, dob, "
      + " patient_gender,  customer_name, incoming_patient_name, "
      + " counter_type, sponsor_index , is_credit_note, is_settlement, tpa_id, is_deposit, deposit_available_for, package_id, allocated_amount";

  public static final String RECEIPTS_EXT_QUERY_COUNT = "SELECT count(receipt_no)";

  public static final String RECEIPTS_EXT_QUERY_TABLES = " " + " FROM bill_receipts_view ";

  public static PagedList searchReceiptsExtended(Map filter, Map listing)
      throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();

    SearchQueryBuilder qb = new SearchQueryBuilder(con, RECEIPTS_EXT_QUERY_FIELDS,
        RECEIPTS_EXT_QUERY_COUNT, RECEIPTS_EXT_QUERY_TABLES, listing);

    qb.addFilterFromParamMap(filter);

    qb.build();

    PagedList l = qb.getMappedPagedList();

    qb.close();
    con.close();

    return l;
  }

  public static final String BILL_RECEIPTS = " SELECT br.receipt_no, br.bill_no, r.amount,"
      + " br.display_date, b.status  ";

  public static final String BILL_RECEIPT_QUERY_TABLES = " FROM receipts r JOIN bill_receipts br ON r.receipt_id = br.receipt_no "
      + " JOIN bill b on (b.bill_no = br.bill_no) ";

  public static final String Update_Receipt_Amt = "UPDATE receipts SET amount=?, modified_at=current_timestamp WHERE receipt_id=?";

  public boolean updateBillReceipts(List updateChargeList) throws SQLException {

    List billReceiptList = new ArrayList();
    List billNumList = new ArrayList();
    Iterator it = updateChargeList.iterator();
    while (it.hasNext()) {
      ChargeDTO cDtoObj = (ChargeDTO) it.next();
      billNumList.add(cDtoObj.getBillNo());
    }

    SearchQueryBuilder qb = new SearchQueryBuilder(con, BILL_RECEIPTS, null,
        BILL_RECEIPT_QUERY_TABLES, null, "br.receipt_no", true, 0, 0);
    qb.addFilter(qb.STRING, "br.bill_no", "IN", billNumList);
    qb.build();

    try (PreparedStatement psData = qb.getDataStatement();) {
      try (ResultSet rsData = psData.executeQuery();) {
        while (rsData.next()) {
          Receipt recObj = new Receipt();
          recObj.setReceiptNo(rsData.getString("receipt_no"));
          recObj.setBillNo(rsData.getString("bill_no"));
          recObj.setAmount(rsData.getBigDecimal("amount"));
          recObj.setBillStatus(rsData.getString("status"));
          billReceiptList.add(recObj);
        }
      }
    }

    String bNum;
    BigDecimal billAmt = new BigDecimal(0);
    BigDecimal recAmt;
    boolean sufficientDebits = false;

    try(PreparedStatement ps = con.prepareStatement(Update_Receipt_Amt);) {
      for (int i = 0; i < updateChargeList.size(); i++) {
        ChargeDTO billObj = (ChargeDTO) updateChargeList.get(i);
        bNum = billObj.getBillNo();
        billAmt = billObj.getAmount().subtract(billObj.getActualAmount());
        sufficientDebits = false;
        for (int j = 0; j < billReceiptList.size(); j++) {
          Receipt recObj = (Receipt) billReceiptList.get(j);
          if (recObj.getBillNo().equalsIgnoreCase(bNum)) {
            recAmt = recObj.getAmount();
            if (recObj.getBillStatus().equalsIgnoreCase("C")) {
              if (billObj.getAmount().compareTo(billObj.getActualAmount()) > 0) {
                setUpdateReceiptParams(ps, recObj.getReceiptNo(), billAmt.add(recObj.getAmount()));
                sufficientDebits = true;
                break;
              } else if (billObj.getAmount().compareTo(
                  billObj.getActualAmount()) < 0) {
                if (billAmt.add(recAmt).compareTo(new BigDecimal(10)) > 0) {
                  setUpdateReceiptParams(ps, recObj.getReceiptNo(), billAmt.add(recObj.getAmount()));
                  sufficientDebits = true;
                  break;
                } else {
                  setUpdateReceiptParams(ps, recObj .getReceiptNo(), new BigDecimal(10));
                  billAmt = billAmt.add(recAmt.subtract(new BigDecimal(10)));
                }
              } else {
                sufficientDebits = true;
              }
            } else {
              sufficientDebits = true;
            }
          }
        }
      }
      if (sufficientDebits) {
        int[] result = ps.executeBatch();
        return DataBaseUtil.checkBatchUpdates(result);
      } else {
        return false;
      }
    }
  }

  private void setUpdateReceiptParams(PreparedStatement ps, String receiptNo, BigDecimal amount)
      throws SQLException {
    ps.setBigDecimal(1, amount);
    ps.setString(2, receiptNo);
    ps.addBatch();
  }

  /*
   * The following is the detailed listing (no summary) of all receipts/claims between the given
   * dates. Used in Tally Export.
   */

  private static final String ALL_RECEIPTS_FOR_AG = " SELECT rpt.*, "
      + "   CASE WHEN rpt.is_settlement THEN 'S' ELSE 'A' END AS recpt_type, rpt.tds_amount AS tds_amt, "
      + "   CASE WHEN rpt.receipt_type = 'R' AND rpt.tpa_id IS NULL THEN 'R' WHEN rpt.receipt_type = 'R' AND rpt.tpa_id IS NOT NULL THEN 'S'"
      + "        WHEN rpt.receipt_type = 'F' THEN 'F' END AS payment_type,  "
      + "   date(rpt.display_date) as voucher_date, "
      + "		r.bill_no, b.bill_type, b.visit_type, b.status, b.finalized_date, b.is_tpa, "
      + " 	b.no_of_receipts, pm.payment_mode, cm.card_type, "
      + "  	coalesce(pd.patient_name, isr.patient_name, prc.customer_name) as patient_name, "
      + "		get_patient_full_name(sm.salutation, coalesce(pd.patient_name, isr.patient_name, prc.customer_name), "
      + "		pd.middle_name, pd.last_name) AS patient_full_name, spl_account_name as payment_mode_account, "
      + "		last_name, sm.salutation, (case when sponsor_index='P' then tm.tpa_name else stpa.tpa_name end) as tpa_name, "
      + " 	c.counter_id, c.counter_no as counter_name, bank_batch_no, b.restriction_type,"
      + " 	rpt.bank_name as bank, pm.ref_required, pm.bank_required, hcm.center_code, pr.mr_no, r.receipt_no, r.bill_no "
      + "     FROM receipts rpt " + "     JOIN bill_receipts r ON (rpt.receipt_id = r.receipt_no)"
      + "  	JOIN bill b USING (bill_no) "
      + "  	LEFT JOIN patient_registration pr ON(pr.patient_id = b.visit_id) "
      + "  	LEFT JOIN patient_details pd ON (pd.mr_no=pr.mr_no) "
      + "  	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id=b.visit_id) "
      + "		LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id "
      + "  	LEFT JOIN salutation_master sm ON (sm.salutation_id=pd.salutation) "
      + "		LEFT JOIN tpa_master tm ON (pr.primary_sponsor_id = tm.tpa_id)"
      + "		LEFT JOIN tpa_master stpa ON (pr.secondary_sponsor_id=stpa.tpa_id) "
      + "     JOIN payment_mode_master pm ON (pm.mode_id = rpt.payment_mode_id) "
      + "     LEFT JOIN card_type_master cm ON (cm.card_type_id = rpt.card_type_id) "
      + "		JOIN counters c ON (rpt.counter=c.counter_id and collection_counter='Y')"
      + "		JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id)";

  public static List getAllReceiptsForAccountGroup(Connection con, java.sql.Timestamp fromDate,
      java.sql.Timestamp toDate, Integer accountGroup, int centerId, boolean IP, List receiptNos)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      String query = ALL_RECEIPTS_FOR_AG;

      StringBuilder where = new StringBuilder();
      if (IP) {
        where.append(" WHERE b.visit_type = 'i'");
      } else {
        where.append(" WHERE b.visit_type != 'i'");
      }

      if (fromDate != null && toDate != null) {
        where.append(" AND r.mod_time BETWEEN ? AND ? AND b.account_group=? ");
        if (centerId != 0) {
          where.append(" AND c.center_id=? ");
        }
      } else {
        if (receiptNos == null || receiptNos.isEmpty()) {
          return Collections.emptyList();
        }

        DataBaseUtil.addWhereFieldInList(where, "r.receipt_no", receiptNos);
      }
      ps = con.prepareStatement(query + where.toString());

      if (fromDate != null && toDate != null) {
        int i = 1;
        ps.setTimestamp(i++, fromDate);
        ps.setTimestamp(i++, toDate);
        ps.setInt(i++, accountGroup);
        if (centerId != 0) {
          ps.setInt(i++, centerId);
        }
      } else {
        Iterator it = receiptNos.iterator();
        int i = 1;
        while (it.hasNext()) {
          ps.setString(i++, (String) ((Map) it.next()).get("voucher_no"));
        }
      }

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  public static List getAllReceipts(Connection con, java.sql.Timestamp fromDate,
      java.sql.Timestamp toDate, Integer accountGroup, int centerId, List receiptNos)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      String query = ALL_RECEIPTS_FOR_AG;

      StringBuilder where = new StringBuilder();

      if (fromDate != null && toDate != null) {
        where.append(" WHERE r.mod_time BETWEEN ? AND ? AND b.account_group=? ");
        if (centerId != 0) {
          where.append(" AND c.center_id=? ");
        }
      } else {
        if (receiptNos == null || receiptNos.isEmpty()) {
          return Collections.emptyList();
        }

        DataBaseUtil.addWhereFieldInList(where, "r.receipt_no", receiptNos);
      }
      ps = con.prepareStatement(query + where.toString());

      if (fromDate != null && toDate != null) {
        int i = 1;
        ps.setTimestamp(i++, fromDate);
        ps.setTimestamp(i++, toDate);
        ps.setInt(i++, accountGroup);
        if (centerId != 0) {
          ps.setInt(i++, centerId);
        }
      } else {
        Iterator it = receiptNos.iterator();
        int i = 1;
        while (it.hasNext()) {
          ps.setString(i++, (String) ((Map) it.next()).get("voucher_no"));
        }
      }

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  public static List getAllReceiptsForAccountGroup(Connection con, java.sql.Timestamp fromDate,
      java.sql.Timestamp toDate, Integer accountGroup, int centerId, List receiptNos)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      String query = ALL_RECEIPTS_FOR_AG;

      StringBuilder where = new StringBuilder();

      if (fromDate != null && toDate != null) {
        where.append(" AND r.mod_time BETWEEN ? AND ? AND b.account_group=? ");
        if (centerId != 0) {
          where.append(" AND c.center_id=? ");
        }
      } else {
        if (receiptNos == null || receiptNos.isEmpty()) {
          return Collections.emptyList();
        }

        DataBaseUtil.addWhereFieldInList(where, "r.receipt_no", receiptNos);
      }
      ps = con.prepareStatement(query + where.toString());

      if (fromDate != null && toDate != null) {
        int i = 1;
        ps.setTimestamp(i++, fromDate);
        ps.setTimestamp(i++, toDate);
        ps.setInt(i++, accountGroup);
        if (centerId != 0) {
          ps.setInt(i++, centerId);
        }
      } else {
        Iterator it = receiptNos.iterator();
        int i = 1;
        while (it.hasNext()) {
          ps.setString(i++, (String) ((Map) it.next()).get("voucher_no"));
        }
      }

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  private static final String NO_OF_RECEIPTS_PER_BILL = ""
      + "SELECT receipt_no, payment_mode, remarks from bill_receipts where bill_no=?";

  public static List getBillReceiptsAndRefunds(String billNo) throws SQLException {
    return DataBaseUtil.queryToDynaList(NO_OF_RECEIPTS_PER_BILL, billNo);
  }

  /*
   * Gets a bill-wise total of receipts and claims, for Settled and Closed bills, whose modification
   * date/time is within the given range.
   */
  private static final String BILL_RECEIPTS_TOTAL = "SELECT bill_no, sum(amount) as amount FROM bill_receipts "
      + "  JOIN bill USING (bill_no) "
      + " WHERE bill.status IN ('S','C') AND bill.mod_time BETWEEN ? AND ? " + " GROUP BY bill_no ";

  public static List getBillReceiptsTotal(Connection con, java.sql.Timestamp fromDate,
      java.sql.Timestamp toDate) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(BILL_RECEIPTS_TOTAL);
      ps.setTimestamp(1, fromDate);
      ps.setTimestamp(2, toDate);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /*
   * CFD report query: all cash flow grouped by main type (Bill Receipts vs. Payments) and
   * payment_mode TODO: make a cash_flow_view that this and other cash-flow based queries like
   * collection report can share.
   */

  // The bill receipts and consolidated receipts are filtered by center only.
  private static final String RECEIPTS_GROUPED = " SELECT main_type, payment_mode, sum(amt) as amount "
      + " FROM ( SELECT 'Receipts' AS main_type, rpt.payment_mode_id, pm.payment_mode, "
      + "    rpt.card_type_id, cm.card_type, rpt.amount AS amt, rpt.display_date, cag.account_group_id, cs.center_id AS center_id "
      + "   FROM receipts rpt JOIN bill_receipts r ON (rpt.receipt_id = r.receipt_no AND NOT is_deposit) "
      + "    JOIN counter_associated_accountgroup_view cag ON (cag.counter_id = rpt.counter) "
      + "    JOIN payment_mode_master pm ON (pm.mode_id = rpt.payment_mode_id) "
      + "    LEFT JOIN card_type_master cm ON (cm.card_type_id = rpt.card_type_id) "
      + "    JOIN counters cs ON (cs.counter_id = rpt.counter) UNION ALL "
      + "   SELECT 'Receipts' AS main_type, d.payment_mode_id, pm.payment_mode, "
      + "    d.card_type_id, cm.card_type, d.amount, d.display_date, cag.account_group_id, cs.center_id AS center_id "
      + "   FROM receipts d "
      + "    JOIN counter_associated_accountgroup_view cag ON (cag.counter_id = d.counter AND d.is_deposit) "
      + "    JOIN payment_mode_master pm ON (pm.mode_id = d.payment_mode_id) "
      + "    LEFT JOIN card_type_master cm ON (cm.card_type_id = d.card_type_id) "
      + "    JOIN counters cs ON (cs.counter_id = d.counter) UNION ALL "
      + "   SELECT 'Payments' AS main_type, p.payment_mode_id, pm.payment_mode, "
      + "    p.card_type_id, cm.card_type,"
      + "    (0 - p.amount), p.date, cag.account_group_id , cs.center_id AS center_id"
      + "   FROM payments p "
      + "    JOIN counter_associated_accountgroup_view cag ON (cag.counter_id = p.counter) "
      + "    JOIN payment_mode_master pm ON (pm.mode_id = p.payment_mode_id) "
      + "    LEFT JOIN card_type_master cm ON (cm.card_type_id = p.card_type_id) "
      + "    JOIN counters cs ON (cs.counter_id = p.counter) UNION ALL "
      + "   SELECT 'Receipts' AS main_type, r.payment_mode_id, pm.payment_mode, "
      + "    r.card_type_id, cm.card_type, r.amount, r.display_date, cag.account_group_id, cs.center_id "
      + "   FROM bill_sponsor_receipts r "
      + "    JOIN counter_associated_accountgroup_view cag ON (cag.counter_id = r.counter) "
      + "    JOIN payment_mode_master pm ON (pm.mode_id = r.payment_mode_id) "
      + "    LEFT JOIN card_type_master cm ON (cm.card_type_id = r.card_type_id) "
      + "    JOIN counters cs ON (cs.counter_id = r.counter) UNION ALL "
      + "   SELECT 'Receipts' AS main_type, r.payment_mode_id, pm.payment_mode, "
      + "    r.card_type_id, cm.card_type, r.amount, r.display_date, cag.account_group_id, cs.center_id "
      + "   FROM insurance_claim_receipt r "
      + "    JOIN counter_associated_accountgroup_view cag ON (cag.counter_id = r.counter) "
      + "    JOIN payment_mode_master pm ON (pm.mode_id = r.payment_mode_id) "
      + "    LEFT JOIN card_type_master cm ON (cm.card_type_id = r.card_type_id) "
      + "    JOIN counters cs ON (cs.counter_id = r.counter)) as cashflow "
      + " WHERE (date(display_date) between ? and ?) "
      + "  AND (?=0 OR coalesce(account_group_id,1)=?) AND ((?=0 OR center_id=?))"
      + " GROUP BY main_type, payment_mode ";

  public static final List<BasicDynaBean> getReceiptsGrouped(Date from, Date to, int accountGroup,
      int centerId) throws SQLException {
    return DataBaseUtil.queryToDynaList(RECEIPTS_GROUPED,
        new Object[] { from, to, accountGroup, accountGroup, centerId, centerId });
  }

  /*
   * Bill Later receipts sum and count grouped by types: for CFD
   */
  public static final String CREDIT_RECEIPTS_GROUPED = "   SELECT CASE WHEN rpt.receipt_type='R' AND rpt.is_settlement ='t' AND rpt.tpa_id IS null THEN 'RS' "
      + "             when rpt.receipt_type='R' AND rpt.is_settlement ='f' AND rpt.tpa_id IS null THEN 'RA' "
      + "             WHEN rpt.receipt_type='R' AND rpt.tpa_id IS NOT null THEN 'S' "
      + "             WHEN rpt.receipt_type='F' THEN 'F' " + "             END AS r_type,"
      + "  sum(rpt.amount) as sum, count(*) as count "
      + " FROM receipts rpt JOIN bill_receipts r ON (rpt.receipt_id = r.receipt_no) "
      + "   JOIN bill b ON (b.bill_no = r.bill_no) "
      + "   JOIN counter_associated_accountgroup_view cag ON (cag.counter_id = rpt.counter) "
      + "  WHERE (b.bill_type = 'C' OR is_tpa) "
      + "   AND NOT rpt.is_deposit AND date(rpt.display_date) BETWEEN ? AND ? AND (?=0 OR coalesce(cag.account_group_id,1)=?) "
      + " GROUP BY r_type ";

  public static final String CENTER_CREDIT_RECEIPTS_GROUPED = "   SELECT CASE WHEN rpt.receipt_type='R' AND rpt.is_settlement ='t' AND rpt.tpa_id IS null THEN 'RS' "
      + "             when rpt.receipt_type='R' AND rpt.is_settlement ='f' AND rpt.tpa_id IS null THEN 'RA' "
      + "             WHEN rpt.receipt_type='R' AND rpt.tpa_id IS NOT null THEN 'S' "
      + "             WHEN rpt.receipt_type='F' THEN 'F' END AS r_type,"
      + "  sum(amount) as sum, count(*) as count "
      + " FROM receipts rpt JOIN bill_receipts r ON (rpt.receipt_id = r.receipt_no) "
      + "   JOIN bill b ON (b.bill_no = r.bill_no) "
      + "   JOIN counter_associated_accountgroup_view cag ON (cag.counter_id = rpt.counter)"
      + "     JOIN counters cs ON (cs.counter_id = rpt.counter)"
      + "  WHERE (b.bill_type = 'C' OR is_tpa) AND cs.center_id = ? "
      + "   AND NOT rpt.is_deposit AND date(rpt.display_date) BETWEEN ? AND ? AND (?=0 OR coalesce(cag.account_group_id,1)=?) "
      + " GROUP BY r_type ";

  public static List<BasicDynaBean> getCreditReceiptsGrouped(java.sql.Date from, java.sql.Date to,
      int accountGroup, int centerId) throws SQLException {
    if (centerId == 0) {
      return DataBaseUtil.queryToDynaList(CREDIT_RECEIPTS_GROUPED,
          new Object[] { from, to, accountGroup, accountGroup });
    } else {
      return DataBaseUtil.queryToDynaList(CENTER_CREDIT_RECEIPTS_GROUPED,
          new Object[] { centerId, from, to, accountGroup, accountGroup });
    }
  }

  private static final String CONSOLIDATED_CLAIM_RECEIPTS = "SELECT SUM(amount) AS sum, COUNT(receipt_no) AS count "
      + " FROM insurance_claim_receipt r "
      + "   JOIN counter_associated_accountgroup_view cag ON (cag.counter_id = r.counter) "
      + " WHERE date(r.display_date) BETWEEN ? AND ? AND (?=0 OR coalesce(cag.account_group_id,1)=?) ";

  private static final String CENTER_CONSOLIDATED_CLAIM_RECEIPTS = "SELECT SUM(amount) AS sum, COUNT(receipt_no) AS count "
      + " FROM insurance_claim_receipt r "
      + " JOIN counter_associated_accountgroup_view cag ON (cag.counter_id = r.counter) "
      + " JOIN u_user  u ON u.emp_username = r.username "
      + " WHERE u.center_id =? AND date(r.display_date) BETWEEN ? AND ? AND (?=0 OR coalesce(cag.account_group_id,1)=?) ";

  public static BasicDynaBean getConsolidatedClaimReceipts(java.sql.Date from, java.sql.Date to,
      int accountGroup, int centerId) throws SQLException {
    if (centerId == 0) {
      return DataBaseUtil.queryToDynaBean(CONSOLIDATED_CLAIM_RECEIPTS,
          new Object[] { from, to, accountGroup, accountGroup });
    } else {
      return DataBaseUtil.queryToDynaBean(CENTER_CONSOLIDATED_CLAIM_RECEIPTS,
          new Object[] { centerId, from, to, accountGroup, accountGroup });
    }
  }

  /*
   * Extra receipts within the given date range, for bill now bills not finalized within the given
   * date range (thus, revenue is included but not receipts). The count is the number of such
   * receipts.
   */
  private static final String GET_BILL_NOW_EXTRA_RECEIPTS = "SELECT count(r.receipt_no) as count, coalesce(sum(rpt.amount),0) as amount "
      + " FROM receipts rpt JOIN  bill_receipts r ON (rpt.receipt_id = r.receipt_no) "
      + "  JOIN bill b ON (b.bill_no = r.bill_no) "
      + " JOIN counters cs ON (cs.counter_id = rpt.counter) "
      + " WHERE b.bill_type != 'C' AND (NOT b.is_tpa) "
      + "  AND (date(rpt.display_date) BETWEEN ? AND ?) "
      + "  AND (NOT(date(b.finalized_date) BETWEEN ? AND ?) OR (b.status != 'C') "
      + "     OR (b.finalized_date IS NULL) ) AND NOT rpt.is_deposit "
      + "  AND (?=0 OR b.account_group=?) AND (?=0 OR cs.center_id=?)";

  public static BasicDynaBean getBillNowExtraReceipts(java.sql.Date from, java.sql.Date to,
      int accountGroup, int centerId) throws SQLException {
    return DataBaseUtil.queryToDynaBean(GET_BILL_NOW_EXTRA_RECEIPTS,
        new Object[] { from, to, from, to, accountGroup, accountGroup, centerId, centerId });
  }

  /*
   * Excess collection for bills finalized within the given date range, where amount collected
   * within the given period is more than billed amount. The count is the number of such bills where
   * there is excess collection.
   */
  public static final String GET_BILL_NOW_EXCESS = " SELECT count(b.bill_no) as count, "
      + "   coalesce(sum(rt.total_receipts),0) + coalesce(sum(b.deposit_set_off),0) "
      + "    - coalesce(sum(b.total_amount),0) AS amount FROM bill b LEFT JOIN "
      + "     (SELECT bill_no, sum(amount) as total_receipts FROM receipts r JOIN bill_receipts br ON r.receipt_id = br.receipt_no "
      + "      WHERE date(r.display_date) BETWEEN ? AND ? AND NOT r.is_deposit GROUP BY bill_no) as rt "
      + "   ON (rt.bill_no = b.bill_no) "
      + " LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id "
      + " LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id "
      + " LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = b.visit_id"
      + " WHERE b.bill_type != 'C' AND (NOT is_tpa) AND b.status = 'C' "
      + "   AND coalesce(rt.total_receipts,0) != coalesce(b.total_amount,0) "
      + "   AND date(b.finalized_date) BETWEEN ? AND ? "
      + "   AND (?=0 OR b.account_group=?)  AND (?=0 OR COALESCE(pr.center_id,isr.center_id, prc.center_id)=?)";

  public static BasicDynaBean getBillNowExcessCollection(java.sql.Date from, java.sql.Date to,
      int accountGroup, int centerId) throws SQLException {
    return DataBaseUtil.queryToDynaBean(GET_BILL_NOW_EXCESS,
        new Object[] { from, to, from, to, accountGroup, accountGroup, centerId, centerId });
  }

  private static String UPDATE_RECEIPT_DATE = "UPDATE bill_receipts SET display_date = ? WHERE bill_no = ? ";

  public boolean updateBillReceipts(String billNo, java.util.Date displayDate) throws SQLException {
    boolean status = false;
    try (PreparedStatement ps = con.prepareStatement(UPDATE_RECEIPT_DATE);) {
      ps.setTimestamp(1, new Timestamp((displayDate).getTime()));
      ps.setString(2, billNo);
      status = ps.execute();
    }
    return status;
  }

  private static String UPDATE_BILL_RECEIPT_TOTALS = " UPDATE bill SET "
      + " primary_total_sponsor_receipts = 0, "
      + " primary_no_of_sponsor_receipts = 0, last_sponsor_receipt_no = null, "
      + " secondary_total_sponsor_receipts = 0, secondary_no_of_sponsor = 0, "
      + " total_receipts = 0, no_of_receipts = 0, last_receipt_no = null " + " WHERE bill_no = ? ";

  public boolean updateBillReceiptTotals(String billNo) throws SQLException {
    boolean status = false;
    try (PreparedStatement ps = con.prepareStatement(UPDATE_BILL_RECEIPT_TOTALS);) {
      ps.setString(1, billNo);
      status = ps.execute();
    }
    return status;
  }

  private static final String UPDATE_RECEIPT_BILL_NO = "UPDATE bill_receipts SET bill_no=? WHERE bill_no=?";
  
  private static final String UPDATE_RECEIPT_USAGE_BILL_NO = "UPDATE receipt_usage SET entity_id=? WHERE entity_type='bill_no' AND entity_id=?";

  public void updateReceiptBillNo(String origBillNo, String newBillNo) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(UPDATE_RECEIPT_BILL_NO);) {
      ps.setString(1, newBillNo);
      ps.setString(2, origBillNo);
      ps.execute();
    }
    // If the bill receipt is updated, update bill_no in receipt_usage table also.
    try (PreparedStatement ps = con.prepareStatement(UPDATE_RECEIPT_USAGE_BILL_NO);) {
      ps.setString(1, newBillNo);
      ps.setString(2, origBillNo);
      ps.execute();
    }
  }

  private static final String RECEIPTS_TABLE = "SELECT r.*, br.* , CASE WHEN r.tpa_id IS NOT NULL THEN 'S' ELSE r.receipt_type END AS payment_type, c.counter_type,"
      + " pm.payment_mode AS payment_mode_name,cm.card_type,"
      + " (CASE WHEN r.payment_mode_id = -1 THEN 'C' "
      + "	   WHEN  r.payment_mode_id = 1  THEN 'R' " + "	   WHEN  r.payment_mode_id = 2  THEN 'B' "
      + "	   WHEN  r.payment_mode_id = 3  THEN 'Q' "
      + "	   WHEN  r.payment_mode_id = 4  THEN 'D' ELSE 'U' END) AS payment_mode,"
      + "  fc.currency, CASE WHEN (r.is_settlement='t' OR r.is_deposit) "
      + "  THEN 'S' ELSE 'A' END AS recpt_type "
      + " FROM receipts r " + " JOIN bill_receipts br on (br.receipt_no=r.receipt_id) "
      + " JOIN counters c on (r.counter=c.counter_id) "
      + " JOIN payment_mode_master pm ON (pm.mode_id = r.payment_mode_id) "
      + " LEFT JOIN card_type_master cm ON (cm.card_type_id = r.card_type_id) "
      + " LEFT JOIN foreign_currency fc ON (fc.currency_id = r.currency_id) ";

  public static List getReceptRefundList(String billNo) throws SQLException {
    return DataBaseUtil.queryToDynaList(RECEIPTS_TABLE + " WHERE br.bill_no= ? ", billNo);
  }

  public static List getReceptRefundListForBills(String billNos) throws SQLException {
    return DataBaseUtil.queryToDynaList(RECEIPTS_TABLE + " WHERE bill_no IN ( ? ) ", billNos);
  }

  private static final String GET_NEW_RECEIPT_DETAILS = "SELECT r.receipt_type, r.receipt_id, "
      + " r.receipt_id AS receipt_no, br.bill_no, r.amount, r.display_date,"
      + " r.modified_at, r.counter, r.payment_mode_id, pm.payment_mode AS payment_mode_name, "
      + " r.card_type_id, cm.card_type,"
      + " r.bank_name, r.reference_no, r.created_by, r.remarks, "
      + " b.total_receipts, b.visit_id, r.tds_amount,r.tds_amount as tds_amt, r.paid_by, "
      + " COALESCE(b.primary_total_sponsor_receipts, 0), "
      + " COALESCE(b.secondary_total_sponsor_receipts,0) AS total_sponsor_receipts, "
      + " (CASE WHEN r.payment_mode_id = -1 THEN 'C' "
      + "    WHEN  r.payment_mode_id = 1  THEN 'R' " + "    WHEN  r.payment_mode_id = 2  THEN 'B' "
      + "    WHEN  r.payment_mode_id = 3  THEN 'Q' "
      + "    WHEN  r.payment_mode_id = 4  THEN 'D' ELSE 'U' END) AS payment_mode,"
      + "  r.exchange_date, r.exchange_rate, r.currency_amt, r.currency_id, fc.currency, "
      + "  r.card_number, r.card_holder_name, r.card_exp_date, r.card_auth_code, r.bank_batch_no, "
      + "  r.credit_card_commission_amount, r.credit_card_commission_percentage, r.is_settlement, "
      + "  CASE WHEN r.is_settlement='t' THEN 'S' ELSE 'A' END AS recpt_type, "
      + "  CASE WHEN r.tpa_id IS NOT NULL THEN 'S' ELSE r.receipt_type END AS payment_type, r.created_by as username"
      + " FROM receipts r" + " LEFT JOIN bill_receipts br ON (r.receipt_id = br.receipt_no)"
      + " LEFT JOIN bill b USING (bill_no) "
      + " JOIN payment_mode_master pm ON (pm.mode_id = r.payment_mode_id) "
      + " LEFT JOIN card_type_master cm ON (cm.card_type_id = r.card_type_id) "
      + " LEFT JOIN foreign_currency fc ON (fc.currency_id = r.currency_id) "
      + " WHERE receipt_id = ?";

  public static BasicDynaBean getReceiptDetails(String receiptNo, String paymentType)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_NEW_RECEIPT_DETAILS);
      ps.setString(1, receiptNo);
      List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(ps);
      if (l != null && !l.isEmpty()) {
        return l.get(0);
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static String GET_BILL_RECEIPT_DETAILS_FOR_EDIT_RECIPT = " SELECT receipt_no,amount,counter, "
      + " payment_mode, remarks,card_type,bank_name,reference_no,display_date,payment_type,recpt_type, "
      + " bill_no, tds_amt, display_date, mod_time,  payment_mode_id, card_type_id, username, mob_number, totp, "
      + " counter_type, paid_by, bank_batch_no, card_auth_code,card_holder_name, currency_id, "
      + " exchange_rate, exchange_date, currency_amt, currency, card_expdate, card_number, mr_no, "
      + " patient_full_name, counter_no as counter_name, sponsor_index,visit_id "
      + " FROM bill_receipts_view " + " WHERE receipt_no = ? ";

  private static String GET_DEPOSIT_RECEIPT_DETAILS_FOR_EDIT_RECIPT = " SELECT receipt_no,amount,counter, "
      + " payment_mode, remarks,card_type,bank_name,reference_no,display_date,payment_type, "
      + " tds_amt, display_date, mod_time,  payment_mode_id, card_type_id, username, mob_number, totp, "
      + " counter_type, paid_by, bank_batch_no, card_auth_code,card_holder_name, currency_id, "
      + " exchange_rate, exchange_date, currency_amt, currency, card_expdate, card_number, mr_no, "
      + " patient_full_name, counter_no as counter_name,center_name " + " FROM deposits_receipts_view "
      + " WHERE receipt_no = ? ";

  public static BasicDynaBean getReceiptDetails(String receiptNo, boolean isbillReceipt)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (isbillReceipt) {
        ps = con.prepareStatement(GET_BILL_RECEIPT_DETAILS_FOR_EDIT_RECIPT);
      } else {
        ps = con.prepareStatement(GET_DEPOSIT_RECEIPT_DETAILS_FOR_EDIT_RECIPT);
      }
      ps.setString(1, receiptNo);
      List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(ps);
      if (l != null && l.size() > 0) {
        return l.get(0);
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
  
  private static final String CENTER_ID = " AND center_id = ? "; 
      
  public static BasicDynaBean getReceiptDetailsCenterwise(String receiptNo, int centerId, boolean isbillReceipt)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (isbillReceipt) {
        ps = con.prepareStatement(GET_BILL_RECEIPT_DETAILS_FOR_EDIT_RECIPT+CENTER_ID);
      } else {
        ps = con.prepareStatement(GET_DEPOSIT_RECEIPT_DETAILS_FOR_EDIT_RECIPT+CENTER_ID);
      }
      ps.setString(1, receiptNo);
      ps.setInt(2, centerId);
      List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(ps);
      if (l != null && l.size() > 0) {
        return l.get(0);
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String GET_ALL_DEPOSITS = "SELECT * FROM receipts "
      + " WHERE mr_no = ? and is_deposit = true AND realized='Y'";
  
  private static final String GET_REFUND_DEPOSITS_TAX = "select sum(COALESCE(tax_amount,0)) as tax_amount from receipt_refund_reference where "
      + " refund_receipt_id = ?;";
  
  public BigDecimal getTotalDepositTaxAmount(Connection con, String mrNo) throws SQLException {
    List<BasicDynaBean> listOfDeposits = DataBaseUtil.queryToDynaList(con,GET_ALL_DEPOSITS, mrNo);
    BigDecimal totalTaxAmt = new BigDecimal(0.00);
    BigDecimal returnTotalTaxAmt = new BigDecimal(0.00);
    for(BasicDynaBean bean : listOfDeposits){
      BigDecimal amount = (BigDecimal) bean.get("amount");
      if(amount.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal tax = (BigDecimal) bean.get("total_tax_rate");
        totalTaxAmt = totalTaxAmt.add(getTaxAggAmount(amount, tax));
      } else{
        BasicDynaBean refundTaxAmountBean = DataBaseUtil.queryToDynaBean(con,GET_REFUND_DEPOSITS_TAX,(String)bean.get("receipt_id"));
        BigDecimal tax = (BigDecimal) refundTaxAmountBean.get("tax_amount");
        if (refundTaxAmountBean != null && tax != null) {
          returnTotalTaxAmt = returnTotalTaxAmt.add(tax);
        }
      }
    }
    return totalTaxAmt.subtract(returnTotalTaxAmt);
  }
  
  public static final String GET_ALL_SETOFF_AMOUNT = "SELECT * FROM bill_receipts b "
      + " JOIN receipts r ON (b.receipt_no = r.receipt_id) "
      + " WHERE r.mr_no = ? and r.is_deposit = true";

  public BigDecimal getTotalTaxSetOffAmount(Connection con, String mrNo) throws SQLException {
    List<BasicDynaBean> listOfSetOff = DataBaseUtil.queryToDynaList(con,GET_ALL_SETOFF_AMOUNT, mrNo);
    BigDecimal totalSetoffTaxAmt = new BigDecimal(0.00);
    for(BasicDynaBean bean : listOfSetOff){
      BigDecimal amount = (BigDecimal) bean.get("allocated_amount");
      BigDecimal tax = (BigDecimal) bean.get("total_tax_rate");
      totalSetoffTaxAmt = totalSetoffTaxAmt.add(getTaxAggAmount(amount, tax));
    }
    return totalSetoffTaxAmt;
  }
  
  protected BigDecimal getTaxAggAmount(BigDecimal amount,
      BigDecimal aggregateTaxPer) {
    BigDecimal taxAmt = BigDecimal.ZERO;

    BigDecimal denomi = aggregateTaxPer.divide(new BigDecimal("100"));
    denomi = (BigDecimal.ONE).add(denomi);

    taxAmt = ConversionUtils.divideHighPrecision(amount, denomi);

    return ConversionUtils.setScale(amount.subtract(taxAmt),true);
  }
}
