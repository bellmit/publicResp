package com.insta.hms.billing.paymentdetails;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.Receipt;
import com.insta.hms.billing.ReceiptRelatedDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.insurance.sponsorreceipts.ReceiptsCollectionDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PaymentModes.PaymentModeMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractPaymentDetails.
 *
 * @author lakshmi.p
 */
public abstract class AbstractPaymentDetails {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(AbstractPaymentDetails.class);

  /** The Constant BILL_PAYMENT. */
  public static final int BILL_PAYMENT = 1;

  /** The Constant PHARMACY_PAYMENT. */
  public static final int PHARMACY_PAYMENT = 2;

  /** The Constant INCOMING_PAYMENT. */
  public static final int INCOMING_PAYMENT = 3;

  /**
   * Generate print receipt urls.
   *
   * @param receiptList
   *          the receipt list
   * @param printParamMap
   *          the print param map
   * @return the list
   * @throws SQLException
   *           the SQL exception
   */
  public abstract List generatePrintReceiptUrls(List<Receipt> receiptList, Map printParamMap)
      throws SQLException;

  /**
   * Process receipt params.
   *
   * @param requestParams
   *          the request params
   * @return the list
   * @throws ParseException
   *           the parse exception
   * @throws SQLException
   *           the SQL exception
   */
  public List<Receipt> processReceiptParams(Map requestParams) throws ParseException, SQLException {

    List<Receipt> receiptList = null;

    Object[] paymentTypeObj = getParam(requestParams, STRING, "paymentType");
    Object[] paymentModeIdObj = getParam(requestParams, INTEGER, "paymentModeId");
    Object[] totPayingAmtObj = getParam(requestParams, NUMERIC, "totPayingAmt");
    Object[] currencyIdObj = getParam(requestParams, INTEGER, "currencyId");
    Object[] currencyAmtObj = getParam(requestParams, NUMERIC, "currencyAmt");
    Object[] exchangeRateObj = getParam(requestParams, NUMERIC, "exchangeRate");
    Object[] exchangeDateTimeObj = getParam(requestParams, STRING, "exchangeDateTime");
    Object[] paidByObj = getParam(requestParams, STRING, "paidBy");
    Object[] tdsAmtObj = getParam(requestParams, NUMERIC, "tdsAmt");

    Object[] allUserRemarksObj = getParam(requestParams, STRING, "allUserRemarks");
    Object[] paymentRemarksObj = getParam(requestParams, STRING, "paymentRemarks");

    Object[] cardTypeIdObj = getParam(requestParams, INTEGER, "cardTypeId");
    Object[] bankNameObj = getParam(requestParams, STRING, "bankName");
    Object[] refNumberObj = getParam(requestParams, STRING, "refNumber");
    Object[] bankBatchNoObj = getParam(requestParams, STRING, "bankBatchNo");
    Object[] cardAuthCodeObj = getParam(requestParams, STRING, "cardAuthCode");
    Object[] cardHolderNameObj = getParam(requestParams, STRING, "cardHolderName");
    Object[] cardNumberObj = getParam(requestParams, STRING, "cardNumber");
    Object[] cardExpDateObj = getParam(requestParams, STRING, "cardExpDate");
    Object[] packageIdsObj = getParam(requestParams, STRING, "mvPackageId");
    Object[] rewardPointsRedeemedObj = getParam(requestParams, STRING, "rewardPointsRedeemed");
    Object[] applicableToIpObj = getParam(requestParams, STRING, "applicableToIp");
    Object[] depositAvailableForObj = getParam(requestParams, STRING, "depositAvailableFor");
    Object[] commissionPerObj = getParam(requestParams, NUMERIC, "commissionPer");
    Object[] commissionAmtObj = getParam(requestParams, NUMERIC, "commissionAmt");
    Object[] mobNumberObj = getParam(requestParams, STRING, "mobNumber");
    Object[] totpObj = getParam(requestParams, STRING, "totp");
    Object[] edcMachineObj = getParam(requestParams, STRING, "edcMachine");
    Object[] paymentTransactionObj = getParam(requestParams, STRING, "paymentTransactionId");

    String[] paymentType = paymentTypeObj != null ? (String[]) paymentTypeObj : null;
    Integer[] paymentModeId = paymentModeIdObj != null ? (Integer[]) paymentModeIdObj : null;

    BigDecimal[] totPayingAmt = totPayingAmtObj != null ? (BigDecimal[]) totPayingAmtObj : null;
    Integer[] currencyId = currencyIdObj != null ? (Integer[]) currencyIdObj : null;
    BigDecimal[] currencyAmt = currencyAmtObj != null ? (BigDecimal[]) currencyAmtObj : null;
    BigDecimal[] exchangeRate = exchangeRateObj != null ? (BigDecimal[]) exchangeRateObj : null;
    String[] exchangeDateTime = exchangeDateTimeObj != null ? (String[]) exchangeDateTimeObj : null;
    String[] paidBy = paidByObj != null ? (String[]) paidByObj : null;
    BigDecimal[] tdsAmt = tdsAmtObj != null ? (BigDecimal[]) tdsAmtObj : null;

    Integer[] cardTypeId = cardTypeIdObj != null ? (Integer[]) cardTypeIdObj : null;
    String[] bankName = bankNameObj != null ? (String[]) bankNameObj : null;
    String[] refNumber = refNumberObj != null ? (String[]) refNumberObj : null;
    String[] bankBatchNo = bankBatchNoObj != null ? (String[]) bankBatchNoObj : null;
    String[] cardAuthCode = cardAuthCodeObj != null ? (String[]) cardAuthCodeObj : null;
    String[] cardHolderName = cardHolderNameObj != null ? (String[]) cardHolderNameObj : null;
    String[] cardNumber = cardNumberObj != null ? (String[]) cardNumberObj : null;
    String[] cardExpDate = cardExpDateObj != null ? (String[]) cardExpDateObj : null;
    String[] mobNumber = mobNumberObj != null ? (String[]) mobNumberObj : null;
    String[] totp = totpObj != null ? (String[]) totpObj : null;
    String[] edcMachine = edcMachineObj != null ? (String[]) edcMachineObj : null;

    String[] allUserRemarks = allUserRemarksObj != null ? (String[]) allUserRemarksObj : null;
    String[] paymentRemarks = paymentRemarksObj != null ? (String[]) paymentRemarksObj : null;
    String[] paymentTransactionId = paymentTransactionObj != null
        ? (String[]) paymentTransactionObj : null;
    String[] packageIds = packageIdsObj != null ? (String[]) packageIdsObj : null;
    String[] rewardPointsRedeemed = rewardPointsRedeemedObj != null
        ? (String[]) rewardPointsRedeemedObj
        : null;
    String[] applicableToIps = applicableToIpObj != null ? (String[]) applicableToIpObj : null;
    String[] depositAvailableFor = depositAvailableForObj != null
        ? (String[]) depositAvailableForObj
        : null;

    BigDecimal[] commissionAmt = commissionAmtObj != null ? (BigDecimal[]) commissionAmtObj : null;
    BigDecimal[] commissionPer = commissionPerObj != null ? (BigDecimal[]) commissionPerObj : null;

    String billNo = requestParams.get("billNo") != null
        ? ((String[]) requestParams.get("billNo"))[0]
        : null;
    String payDate = requestParams.get("payDate") != null
        ? ((String[]) requestParams.get("payDate"))[0]
        : null;
    String payTime = requestParams.get("payTime") != null
        ? ((String[]) requestParams.get("payTime"))[0]
        : null;
    String counterId = requestParams.get("counterId") != null
        ? ((String[]) requestParams.get("counterId"))[0]
        : null;

    // setting primarySponsor and secondarySponsor for to set tpa_id in receipts table
    String primarySponsor = requestParams.get("primarySponsor") != null
        ? ((String[]) requestParams.get("primarySponsor"))[0]
        : null;
    String secondarySponsor = requestParams.get("secondarySponsor") != null
        ? ((String[]) requestParams.get("secondarySponsor"))[0]
        : null;

    HttpSession session = RequestContext.getSession();
    String userid = (String) session.getAttribute("userid");

    // If no payments or no counter, receipts are not generated.
    if (paymentType != null && (counterId != null && !counterId.equals(""))) {
      receiptList = new ArrayList<Receipt>();
    } else {
      return null;
    }

    int jval = 0;
    int kval = 0;
    int nval = 0;
    int tval = 0;
    int rval = 0;
    int pval = 0;
    int uval = 0;
    int vval = 0;
    int wval = 0;
    int xval = 0;

    for (int i = 0; i < paymentType.length; i++) {
      if (paymentType[i] == null) {
        continue;
      }
      if (paymentType[i].equals("")) {
        continue;
      }
      if (totPayingAmt[i] == null) {
        continue;
      }
      if (totPayingAmt[i].compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }

      Receipt receipt = new Receipt();

      if (paymentType[i].equalsIgnoreCase(Receipt.PATIENT_ADVANCE)
          || paymentType[i].equalsIgnoreCase(Receipt.PATIENT_SETTLEMENT)) {

        receipt.setReceiptType("R");
        if (paymentType[i].equalsIgnoreCase(Receipt.PATIENT_ADVANCE)) {
          receipt.setIsSettlement(false);
        } else {
          receipt.setIsSettlement(true);
        }
        receipt.setPaymentType("R");
        receipt.setAmount(totPayingAmt[i]);

        if (commissionAmt != null && commissionPer != null
            && commissionPer[i].compareTo(BigDecimal.ZERO) > 0) {
          receipt.setCommissionAmount(commissionAmt[i]);
          receipt.setCommissionPercentage(commissionPer[i]);
        }

        if (currencyAmt != null && currencyId != null && currencyId[i] > 0
            && currencyAmt[i].compareTo(BigDecimal.ZERO) > 0) {
          receipt.setCurrencyId(currencyId[i]);
          receipt.setCurrencyAmt(currencyAmt[i]);
          receipt.setExchangeRate(exchangeRate[i]);
          receipt.setExchangeDateTime(DateUtil.parseTimestamp(exchangeDateTime[i]));
        }
        
      } else if (paymentType[i].equalsIgnoreCase(Receipt.REFUND)) {
        receipt.setReceiptType("F");
        receipt.setIsSettlement(true);
        receipt.setPaymentType("F");
        receipt.setAmount(totPayingAmt[i].negate());

        if (commissionAmt != null && commissionPer != null
            && commissionPer[i].compareTo(BigDecimal.ZERO) > 0) {
          receipt.setCommissionAmount(commissionAmt[i]);
          receipt.setCommissionPercentage(commissionPer[i]);
        }

        if (currencyAmt != null && currencyId != null && currencyId[i] > 0
            && currencyAmt[i].compareTo(BigDecimal.ZERO) > 0) {
          receipt.setCurrencyId(currencyId[i]);
          receipt.setCurrencyAmt(currencyAmt[i].negate());
          receipt.setExchangeRate(exchangeRate[i]);
          receipt.setExchangeDateTime(DateUtil.parseTimestamp(exchangeDateTime[i]));
        }

      } else if (paymentType[i].equalsIgnoreCase(Receipt.PRIMARY_SPONSOR_ADVANCE)
          || paymentType[i].equalsIgnoreCase(Receipt.PRIMARY_SPONSOR_SETTLEMENT)
          || paymentType[i].equalsIgnoreCase(Receipt.SECONDARY_SPONSOR_ADVANCE)
          || paymentType[i].equalsIgnoreCase(Receipt.SECONDARY_SPONSOR_SETTLEMENT)) {

        if (paymentType[i].equalsIgnoreCase(Receipt.PRIMARY_SPONSOR_ADVANCE)) {
          receipt.setReceiptType("R");
          receipt.setTpaId(primarySponsor);
          receipt.setIsSettlement(false);
          receipt.setSponsorIndex("P");
          receipt.setSponsorId(primarySponsor);

        } else if (paymentType[i].equalsIgnoreCase(Receipt.PRIMARY_SPONSOR_SETTLEMENT)) {
          receipt.setReceiptType("R");
          receipt.setTpaId(primarySponsor);
          receipt.setIsSettlement(true);
          receipt.setSponsorIndex("P");
          receipt.setSponsorId(primarySponsor);

        } else if (paymentType[i].equalsIgnoreCase(Receipt.SECONDARY_SPONSOR_ADVANCE)) {
          receipt.setReceiptType("R");
          receipt.setTpaId(secondarySponsor);
          receipt.setIsSettlement(false);
          receipt.setSponsorIndex("S");
          receipt.setSponsorId(secondarySponsor);

        } else if (paymentType[i].equalsIgnoreCase(Receipt.SECONDARY_SPONSOR_SETTLEMENT)) {
          receipt.setReceiptType("R");
          receipt.setTpaId(secondarySponsor);
          receipt.setIsSettlement(true);
          receipt.setSponsorIndex("S");
          receipt.setSponsorId(secondarySponsor);
        }

        receipt.setPaymentType("S");
        receipt.setAmount(totPayingAmt[i]);

        if (tdsAmt != null && tdsAmt[i] != null) {
          receipt.setTdsAmt(tdsAmt[i]);
        }
        if (paidBy != null && paidBy[i] != null) {
          receipt.setPaidBy(paidBy[i]);
        }

        if (currencyAmt != null && currencyId[i] > 0
            && currencyAmt[i].compareTo(BigDecimal.ZERO) > 0) {
          receipt.setCurrencyId(currencyId[i]);
          receipt.setCurrencyAmt(currencyAmt[i]);
          receipt.setExchangeRate(exchangeRate[i]);
          receipt.setExchangeDateTime(DateUtil.parseTimestamp(exchangeDateTime[i]));
        }
      }

      receipt.setBillNo(billNo);

      receipt.setReceiptDate(DateUtil.parseTimestamp(payDate, payTime));

      if (receipt.getReceiptDate() == null) {
        receipt.setReceiptDate(DateUtil.getCurrentTimestamp());
      }

      int modeId = paymentModeId[i];
      BasicDynaBean payBean = new PaymentModeMasterDAO().findByKey("mode_id", modeId);
      if (modeId == -9) {
        int rewardPointsRedeemedTmp = (rewardPointsRedeemed[i] != null
            && !rewardPointsRedeemed[i].equals("")) ? Integer.parseInt(rewardPointsRedeemed[i]) : 0;
        receipt.setPointsRedeemed(rewardPointsRedeemedTmp);
      }

      if (payBean != null) {
        if (payBean.get("bank_required") != null
            && ((String) payBean.get("bank_required")).equals("Y")) {
          receipt.setBankName(bankName[jval]);
          jval++;
        }
        if ((payBean.get("ref_required") != null
            && ((String) payBean.get("ref_required")).equals("Y"))
            || ((Integer) payBean.get("mode_id") == -2)) {
          if (null != refNumber && null != refNumber[kval]) {
            receipt.setReferenceNo(refNumber[kval]);
            kval++;
          }
        }
        if (payBean.get("card_type_required") != null
            && ((String) payBean.get("card_type_required")).equals("Y")) {
          receipt.setCardTypeId(cardTypeId[nval]);
          nval++;
        }
        if (payBean.get("bank_batch_required") != null
            && ((String) payBean.get("bank_batch_required")).equals("Y")) {
          receipt.setBankBatchNo(bankBatchNo[tval]);
          tval++;
        }
        if (payBean.get("card_auth_required") != null
            && ((String) payBean.get("card_auth_required")).equals("Y")) {
          receipt.setCardAuthCode(cardAuthCode[rval]);
          rval++;
        }
        if (payBean.get("card_holder_required") != null
            && ((String) payBean.get("card_holder_required")).equals("Y")) {
          receipt.setCardHolderName(cardHolderName[pval]);
          pval++;
        }
        if (payBean.get("card_number_required") != null
            && ((String) payBean.get("card_number_required")).equals("Y")) {
          receipt.setCardNumber(cardNumber[uval]);
          uval++;
        }
        if (payBean.get("card_expdate_required") != null
            && ((String) payBean.get("card_expdate_required")).equals("Y")) {
          if (null != cardExpDate && !cardExpDate[vval].equals("")) {
            String dateSeparator = cardExpDate[vval].contains("/") ? "/" : "-";
            String[] arr = cardExpDate[vval].split(dateSeparator);
            int year = DateUtil.convertTwoDigitYear(Integer.parseInt(arr[1]), "future");
            receipt.setCardExpDate(
                DateUtil.parseDate(arr[0] + dateSeparator + year, dateSeparator, "short"));
            vval++;
          }
        }
        if (payBean.get("mobile_number_required") != null
            && ((String) payBean.get("mobile_number_required")).equals("Y")) {
          receipt.setMobNumber(mobNumber[wval]);
          wval++;
        }
        if (payBean.get("totp_required") != null
            && ((String) payBean.get("totp_required")).equals("Y")) {
          receipt.setTotp(totp[xval]);
          xval++;
        }
      }
      if (null != edcMachine && null != edcMachine[i]) {
        receipt.setEdcIMEI(edcMachine[i]);
      }
      receipt.setPaymentModeId(paymentModeId[i]);
      receipt.setCounter(counterId);

      receipt.setUsername(userid);
      receipt.setRemarks(paymentRemarks[i]);
      if (paymentTransactionId != null) {
        receipt.setPaymentTransactionId(
              (paymentTransactionId[i] != null && !paymentTransactionId[i].equals(""))
              ? Integer.parseInt(paymentTransactionId[i]) : null);
      }
      if (packageIds != null) {
        receipt.setPackageId(
            (packageIds[i] != null && !packageIds[i].equals("")) ? Integer.parseInt(packageIds[i])
                : null);
      }
      if (applicableToIps != null) {
        receipt.setApplicableToIp(
            (applicableToIps[i] != null && !applicableToIps[i].equals("")) ? applicableToIps[i]
                : null);
      }
      if (depositAvailableFor != null) {
        receipt.setApplicableToIp(
            (depositAvailableFor[i] != null && !depositAvailableFor[i].equals(""))
                ? depositAvailableFor[i]
                : null);
      }
      receiptList.add(receipt);
    }

    return receiptList;
  }

  /**
   * receiptList - List of Receipt bill - The bill to which the receipt has to be created. visitType
   * - The patient visit type (retail/ incoming / hospital)
   *
   * @param con
   *          the con
   * @param receiptList
   *          the receipt list
   * @param bill
   *          the bill
   * @param visitType
   *          the visit type
   * @param billStatus
   *          the bill status
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */

  public boolean createReceipts(Connection con, List<Receipt> receiptList, Bill bill,
      String visitType, String billStatus) throws SQLException {
    return createReceipts(con, receiptList, bill, visitType, billStatus, null, null);
  }

  /**
   * Creates the receipts.
   *
   * @param con
   *          the con
   * @param receiptList
   *          the receipt list
   * @param bill
   *          the bill
   * @param visitType
   *          the visit type
   * @param billStatus
   *          the bill status
   * @param collectionId
   *          the collection id
   * @param userName
   *          the user name
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean createReceipts(Connection con, List<Receipt> receiptList, Bill bill,
      String visitType, String billStatus, Integer collectionId, String userName)
      throws SQLException {
    ReceiptRelatedDAO rdao = new ReceiptRelatedDAO(con);

    int centerId = VisitDetailsDAO.getCenterId(con, bill.getVisitId());
    int receiptCount = 0;
    if (receiptList != null && receiptList.size() > 0) {
      for (Receipt receipt : receiptList) {

        /*
         * Note : receipt.getPaymentType() defines which receipt no to be generated where
         * payment_type is "R" = "Receipt", "F" = "Refund", "S" = "Sponsor" related receipt
         * sequences from hosp_id_patterns
         */

        receipt.setBillNo(bill.getBillNo());
        receipt.setMrno(bill.getMrno());

        if (receipt.getPaymentType().equals("R")) {
          receipt.setReceiptPrintFormat(Receipt.RECEIPT_PRINT);
          if (bill.getBillType().equals(Bill.BILL_TYPE_PREPAID)) {
            // receipt.setReceiptType("S");
            receipt.setReceiptType("R");
            receipt.setIsSettlement(true);
            receiptCount++;
          }
        } else if (receipt.getPaymentType().equals("F")) {
          receipt.setReceiptPrintFormat(Receipt.REFUND_PRINT);

        } else if (receipt.getPaymentType().equals("S")) {
          receipt.setReceiptPrintFormat(Receipt.SPONSOR_RECEIPT_PRINT);
        }
        String receiptNo = rdao.getNextReceiptNo(bill.getBillType(), bill.getVisitType(), "N",
            receipt.getPaymentType(), centerId);
        receipt.setReceiptNo(receiptNo);
        //if bill's Visit Type is "t", Then set visit_id as receipt's Incoming Visit Id
        if (bill.getVisitType().equals("t")) {
          receipt.setIncomingVisitId(bill.getVisitId());
        }
        //if bill's Visit Type is "r", Then set visit_id as receipt's Store retail custormer Id
        if (bill.getVisitType().equals("r")) {
          receipt.setStoreRetailCustomerId(bill.getVisitId());
        }
        boolean success = rdao.createReceipt(receipt);

        // If any receipt creation is unsuccessful, all receipt creations are aborted.
        if (!success) {
          receiptList.clear();
          return false;
        }
        // if payment mode is redeem points(-9), INSERT REDEEM POINTS IN BILL TABLE
        if (receipt.getPaymentModeId() == -9 && receipt.getPointsRedeemed() > 0) {
          BigDecimal redemptionRate = GenericPreferencesDAO.getGenericPreferences()
              .getPoints_redemption_rate();
          redemptionRate = redemptionRate == null ? BigDecimal.ZERO : redemptionRate;

          BasicDynaBean rewardPointsBean = new GenericDAO("reward_points_status").findByKey("mr_no",
              receipt.getMrno());

          int totalPointsEarned = 0;
          int totalPointsRedeemed = 0;
          int totalOpenPointsRedeemed = 0;

          int billPointsRedeemed = 0;

          if (rewardPointsBean != null) {
            if (rewardPointsBean.get("points_earned") != null) {
              totalPointsEarned = (Integer) rewardPointsBean.get("points_earned");
            }
            if (rewardPointsBean.get("points_redeemed") != null) {
              totalPointsRedeemed = (Integer) rewardPointsBean.get("points_redeemed");
            }
            if (rewardPointsBean.get("open_points_redeemed") != null) {
              totalOpenPointsRedeemed = (Integer) rewardPointsBean.get("open_points_redeemed");
            }

            totalPointsRedeemed = totalPointsRedeemed + totalOpenPointsRedeemed;
          }

          billPointsRedeemed = bill.getRewardPointsRedeemed();

          // Remaining points after the current bill points are deducted.
          int pointsRemaining = totalPointsEarned - totalPointsRedeemed + billPointsRedeemed;

          // bill points
          int points = receipt.getPointsRedeemed();

          // Set the bill redeemed points if the remaining points > 0
          if (points != 0) {
            if (pointsRemaining > 0) {
              if (pointsRemaining - points >= 0) {
                bill.setRewardPointsRedeemed(billPointsRedeemed + points);
              } else if (points > 0) {
                bill.setRewardPointsRedeemed(billPointsRedeemed + pointsRemaining);
              }
            }
            bill.setRewardPointsRedeemedAmount(
                bill.getRewardPointsRedeemedAmount().add(receipt.getAmount()));
          } else {
            bill.setRewardPointsRedeemed(0);
            bill.setRewardPointsRedeemedAmount(BigDecimal.ZERO);
          }
          // save/update bill object to capture the redeemed points and redeemed amount
          BillDAO billDao = new BillDAO(con);
          billDao.updateBill(bill);
        }

        if (null != collectionId) {
          ReceiptsCollectionDAO rcdao = new ReceiptsCollectionDAO();
          BasicDynaBean server = rcdao.getBean();
          server.set("collection_id", collectionId);
          server.set("receipt_id", receiptNo);
          server.set("username", userName);
          server.set("status", "P");
          try {
            rcdao.insert(con, server);
          } catch (IOException exception) {
            logger.error("Unable to insert data: " + exception);
          }
        }
      }

      // If pre paid bill receipt then print bill cum receipt.
      if (receiptCount == 1) {
        for (Receipt receipt : receiptList) {
          if (receipt.getPaymentType().equals("R") && billStatus.equals(Bill.BILL_STATUS_CLOSED)
              && bill.getBillType().equals(Bill.BILL_TYPE_PREPAID) && (!bill.getIs_tpa())) {

            receipt.setReceiptPrintFormat(Receipt.BILL_CUM_RECEIPT_PRINT);
          }
        }
      }
    }

    return true;
  }

  /**
   * Total amount received via multiple payments at a time.
   *
   * @param con
   *          the con
   * @param receiptList
   *          the receipt list
   * @param totalDeposit
   *          the total deposit
   * @return the payment receipt total amount
   * @throws SQLException
   *           the SQL exception
   */

  public Map getPaymentReceiptTotalAmount(Connection con, List<Receipt> receiptList,
      BigDecimal totalDeposit) throws SQLException {

    Map payAmtMap = new HashMap();
    BigDecimal totalAmount = BigDecimal.ZERO;

    if (receiptList != null && receiptList.size() > 0) {
      for (Receipt receipt : receiptList) {
        BigDecimal payAmount = receipt.getAmount();
        totalAmount = totalAmount.add(payAmount);
      }
    }
    payAmtMap.put("totalAmount", totalAmount);
    payAmtMap.put("totalDeposit", totalDeposit);
    return payAmtMap;
  }

  /**
   * Validate user counter.
   *
   * @param type
   *          the type
   * @return true, if successful
   */
  public static boolean validateUserCounter(int type) {
    boolean hasCounter = false;
    HttpSession session = RequestContext.getSession();
    String counter = null;
    switch (type) {
      case BILL_PAYMENT:
        counter = (session.getAttribute("billingcounterId") != null)
            ? (String) session.getAttribute("billingcounterId")
            : null;
        break;

      case PHARMACY_PAYMENT:
        counter = (session.getAttribute("pharmacyCounterId") != null)
            ? (String) session.getAttribute("pharmacyCounterId")
            : null;
        break;

      case INCOMING_PAYMENT:
        counter = (session.getAttribute("billingcounterId") != null)
            ? (String) session.getAttribute("billingcounterId")
            : null;
        break;

      default:
        counter = null;
    }

    if (counter != null && !counter.trim().equals("")) {
      hasCounter = true;
    }

    return hasCounter;
  }

  /**
   * Gets the receipt impl.
   *
   * @param type
   *          the type
   * @return the receipt impl
   */
  public static AbstractPaymentDetails getReceiptImpl(int type) {

    switch (type) {
      case BILL_PAYMENT:
        return new BillPaymentDetailsImpl();

      case PHARMACY_PAYMENT:
        return new PharmacyPaymentDetailsImpl();

      case INCOMING_PAYMENT:
        return new IncomingPaymentDetailsImpl();

      default:
        return null;
    }
  }

  /** The Constant INTEGER. */
  public static final int INTEGER = 1;

  /** The Constant STRING. */
  public static final int STRING = 2;

  /** The Constant NUMERIC. */
  public static final int NUMERIC = 3;

  /**
   * Gets the param.
   *
   * @param requestParams
   *          the request params
   * @param type
   *          the type
   * @param paramName
   *          the param name
   * @return the param
   * @throws ParseException
   *           the parse exception
   */
  public static Object[] getParam(Map requestParams, int type, String paramName)
      throws ParseException {
    switch (type) {
      case STRING:
        return (requestParams.get(paramName) != null) ? (Object[]) requestParams.get(paramName)
            : null;
      case INTEGER: {
        Object[] objArr = (requestParams.get(paramName) != null)
            ? (Object[]) requestParams.get(paramName)
            : null;
        Integer[] intArr = null;
        if (objArr != null) {
          intArr = new Integer[objArr.length];
          for (int i = 0; i < intArr.length; i++) {
            intArr[i] = new Integer(
                objArr[i].toString().trim().equals("") ? "0" : objArr[i].toString());
          }
        }
        return intArr;
      }
      case NUMERIC: {
        Object[] objArr = (requestParams.get(paramName) != null)
            ? (Object[]) requestParams.get(paramName)
            : null;
        BigDecimal[] bigDecArr = null;
        if (objArr != null) {
          bigDecArr = new BigDecimal[objArr.length];
          for (int i = 0; i < bigDecArr.length; i++) {
            bigDecArr[i] = new BigDecimal(
                objArr[i].toString().trim().equals("") ? "0" : objArr[i].toString());
          }
        }
        return bigDecArr;
      }
      default:
        break;
    }
    return null;
  }
}
