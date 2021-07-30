package com.insta.hms.core.billing;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.bob.hms.common.DateUtil;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillConstants;
import com.insta.hms.billing.Receipt;
import com.insta.hms.billing.paymentdetails.AbstractPaymentDetails;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.discharge.DischargeService;
import com.insta.hms.core.fa.AccountingJobScheduler;
import com.insta.hms.core.medicalrecords.MRDCaseFileIndentRepository;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.extension.payments.PaymentProcessor;
import com.insta.hms.integration.insurance.remittance.RemittanceService;

import freemarker.template.TemplateException;

@Service
public class ReceiptService {

  private static Logger log = LoggerFactory.getLogger(ReceiptService.class);

  @LazyAutowired
  private BillService billService;

  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private BillChargeService billChargeService;

  @LazyAutowired
  private BillReceiptRepository billReceiptRepo;

  @LazyAutowired
  private ReceiptRepository receiptRepository;

  @LazyAutowired
  private RegistrationService regService;

  @LazyAutowired
  private DischargeService dischargeService;

  @LazyAutowired
  private PaymentProcessor paymentProcessor;

  @LazyAutowired
  private MRDCaseFileIndentRepository mrdCaseFileRepo;

  @LazyAutowired
  private PatientInsurancePlansService patInsPlansService;

  @LazyAutowired
  private BillClaimService billClaimService;

  @LazyAutowired
  private RemittanceService remittanceService;

  @LazyAutowired
  private Receipt receiptService;

  @LazyAutowired
  private AllocationService allocationService;

  @LazyAutowired
  private ReceiptHibernateRepository receiptHibernateRepository;

  @Autowired
  private AccountingJobScheduler accountingJobScheduler;

  @LazyAutowired
  private GenericPreferencesService genPrefService;
  
  /**
   * @param bill
   * @param visitDetailsParams
   * @param doc_eandm_codification_required
   * @param visitDetailsBean
   * @param map
   * @return
   * @throws SQLException
   */
  @SuppressWarnings({ "rawtypes" })
  public Map createReceipt(BasicDynaBean bill, Map<String, Object> visitDetailsParams,
      boolean doc_eandm_codification_required, BasicDynaBean visitDetailsBean,
      Map<String, Object> map) throws SQLException {

    String userName = (String) bill.get("username");
    AbstractPaymentDetails bpImpl = AbstractPaymentDetails
        .getReceiptImpl(AbstractPaymentDetails.BILL_PAYMENT);
    String billStatus = (String) bill.get("status");
    String paymentStatus = BillService.BILL_PAYMENT_PAID;
    String billNo = (String) bill.get("bill_no");
    BasicDynaBean billBean = billService.findByKey("bill_no", billNo);
    boolean hasConsultation = false;
    String patAmt = (String) getValue("patientAmt", visitDetailsParams, true);
    BigDecimal patientAmount = (patAmt != null && !patAmt.trim().isEmpty()) ? new BigDecimal(patAmt)
        : BigDecimal.ZERO;
    boolean success = false;

    billStatus = setBillNewStatus(bill, doc_eandm_codification_required, billStatus, billNo,
        hasConsultation);

    success = dischargeRelatedUpdate(bill, userName, success, visitDetailsBean);

    if (((BigDecimal) billBean.get("total_amount")).compareTo(BigDecimal.ZERO) > 0) {
      boolean isAmountZero = false;
      List<Receipt> receiptList = new ArrayList<Receipt>();
      ArrayList<BasicDynaBean> receiptBeanList = new ArrayList<BasicDynaBean>();

      String mrNo = (String) visitDetailsBean.get("mr_no");
      isAmountZero = createReceiptObjectList(billBean, visitDetailsParams, userName, billNo,
          mrNo, patientAmount, isAmountZero, receiptList);

      if (!isAmountZero) {
        success = insertReceipt(receiptList, bill);
        
        if (success) {
            if (!((Boolean) bill.get("is_tpa")
                && bill.get("bill_type").equals(BillService.BILL_TYPE_PREPAID))) {
              billStatus = Bill.BILL_STATUS_CLOSED;
              paymentStatus = BillService.BILL_PAYMENT_PAID;
            }
          generatePrintURLs(map, bpImpl, receiptList, visitDetailsParams);
        }
      }
    }
    java.sql.Timestamp finalizedDate = DateUtil.getCurrentTimestamp();
    updatePayments(bill, billStatus, paymentStatus, "Y", finalizedDate, userName, false, false,
        false, (String) visitDetailsBean.get("mr_no"));
    return map;
  }

  private boolean dischargeRelatedUpdate(BasicDynaBean bill, String userName, boolean success,
      BasicDynaBean visitDetailsBean) {
    /*
     * Discharge the patient if required
     */
    if (null != bill.get("discharge_status") && bill.get("discharge_status").equals("Y")) {
      BasicDynaBean bean = regService.getBean();
      bean.set("user_name", userName);
      bean.set("discharge_time", DateUtil.getCurrentTime());
      bean.set("discharge_date", DateUtil.getCurrentDate());
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("patient_id", (String) bill.get("visit_id"));
      regService.update(bean, keys);
      success = true;
    }
    // Not related in New Registration so not migrated.
    // else if (null!=bill.get("discharge_status") && bill.get("discharge_status").equals("D")) {
    // // update the discharge date alone
    // VisitDetailsDAO.updateDischargeDate(null,(String)bill.get("visit_id"),
    // null, null,(String)bill.get("visit_id"));
    // }

    /*
     * insert or update the financial discharge date and time
     */
    Boolean isEntryExists = dischargeService
        .checkIfPatientDischargeEntryExists((String) bill.get("visit_id"));
    if (bill.get("discharge_status").equals("Y")) {
      boolean isAllBillsFinalized = billService
          .getOkToDischargeBills((String) bill.get("visit_id"));
      if (success && isAllBillsFinalized) {
        success = dischargeService.insertOrUpdateFinancialDischargeDetails(
            (String) bill.get("visit_id"), true, userName, isEntryExists);
      }
    } else if (success) {
      success = dischargeService.insertOrUpdateFinancialDischargeDetails(
          (String) bill.get("visit_id"), false, userName, isEntryExists);
    }
    return success;
  }

  private String setBillNewStatus(BasicDynaBean bill, boolean doc_eandm_codification_required,
      String billStatus, String billNo, boolean hasConsultation) {
    // Bug # 23460
    if ((Boolean) bill.get("is_tpa")
        && bill.get("bill_type").equals(BillService.BILL_TYPE_PREPAID)) {
      List<BasicDynaBean> billCharges = billChargeService.getChargeDetailsBean(billNo);
      for (BasicDynaBean bean : billCharges) {
        if (((String) bean.get("charge_group")).equals("DOC")) {
          hasConsultation = true;
          break;
        }
      }

      if (hasConsultation && doc_eandm_codification_required)
        billStatus = Bill.BILL_STATUS_OPEN;
      else
        billStatus = Bill.BILL_STATUS_FINALIZED;
    }
    return billStatus;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void generatePrintURLs(Map<String, Object> map, AbstractPaymentDetails bpImpl,
      List<Receipt> receiptList, Map<String, Object> visitDetailsParams) throws SQLException {
    Map printParamMap = new HashMap();
    if (receiptList != null && receiptList.size() > 0) {
      printParamMap.put("printerTypeStr", getValue("printerTypeStr", visitDetailsParams, false));
      printParamMap.put("customTemplate", getValue("customTemplate", visitDetailsParams, false));
      List<String> printURLs = bpImpl.generatePrintReceiptUrls(receiptList, printParamMap);// TODO
                                                                                           // Migrate
                                                                                           // to
                                                                                           // Spring
      map.put("print_urls", printURLs);
    }
  }

  private boolean createReceiptObjectList(BasicDynaBean bill,
      Map<String, Object> visitDetailsParams, String userName, String billNo, String mrNo,
      BigDecimal patientAmount, boolean isAmountZero, List<Receipt> receiptList) {
    Receipt receiptObj = new Receipt();
    receiptObj.setReceiptType("R");
    receiptObj.setPaymentType("R");
    // Settlement receipt
    receiptObj.setIsSettlement(true);
    receiptObj.setTpaId(null);

    receiptObj.setBillNo(billNo);
    if ((Boolean) bill.get("is_tpa")) {
      receiptObj.setAmount(patientAmount);
      if (patientAmount.compareTo(BigDecimal.ZERO) == 0)
        isAmountZero = true;
    } else {
      receiptObj.setAmount(
          ((BigDecimal) bill.get("total_amount")).add((BigDecimal) bill.get("total_tax")));
      isAmountZero = false;
    }
    receiptObj.setMrno(mrNo);
    receiptObj.setReceiptDate(DateUtil.getCurrentTimestamp());
    receiptObj.setPaymentModeId(-1);
    receiptObj.setCardTypeId(0);
    receiptObj.setUsername(userName);
    receiptObj.setRemarks((String) getValue("billRemarks", visitDetailsParams, true));
    receiptObj.setCounter((String) sessionService.getSessionAttributes().get("billing_counter_id"));
    receiptObj.setReceiptPrintFormat(Receipt.RECEIPT_PRINT);
    receiptList.add(receiptObj);
    return isAmountZero;
  }

  public String updatePayments(BasicDynaBean bill, String newStatus, String paymentStatus,
      String dischargeStatus, Timestamp finalizedDate, String userId, boolean removePrimary,
      boolean paymentForceClose, boolean claimForceClose, String mrNo) {

    StringBuilder sb = new StringBuilder();
    boolean allSuccess = false;
    boolean discountProblems = false;

    /*
     * If status is changing to closed, or finalized, then update the payment amounts
     */
    if (newStatus.equals(Bill.BILL_STATUS_CLOSED) || newStatus.equals(Bill.BILL_STATUS_FINALIZED)) {
      List<BasicDynaBean> allBillCharges = billChargeService
          .getChargeDetailsBean((String) bill.get("bill_no"));
      boolean success = false;

      for (BasicDynaBean chargeBean : allBillCharges) {

        if (chargeBean.get("status").equals("X")) {
          success = true;
        } else {
          try {
            success = paymentProcessor.updateAllPayoutAmounts((String) chargeBean.get("charge_id"));
          } catch (IOException e) {
            log.error("", e);
            e.printStackTrace();
          } catch (TemplateException e) {
            log.error("", e);
            e.printStackTrace();
          }
        }
        if (!success) {
          sb.append("<br/>Discount in " + chargeBean.get("chargehead_name") + "/"
              + chargeBean.get("act_description") + " Dated: " + chargeBean.get("posted_date")
              + " is greater than one or more doctor payment amounts. ");
          discountProblems = true;
        }
      }
    }

    if (!discountProblems) {

      java.sql.Timestamp closedDate = null;
      String closedBy = null;
      String finalizedBy = (String) bill.get("finalized_by");
      boolean isTpa = (Boolean) bill.get("is_tpa");

      BasicDynaBean billAmtBean = billService.getBillAmounts((String) bill.get("bill_no"));
      if (billAmtBean != null) {
        BigDecimal billAmt = (BigDecimal) billAmtBean.get("total_amount");
        BigDecimal claimAmt = ((BigDecimal) billAmtBean.get("total_claim"));
        BigDecimal priClaimAmt = ((BigDecimal) billAmtBean.get("primary_total_claim"));
        BigDecimal secClaimAmt = ((BigDecimal) billAmtBean.get("secondary_total_claim"));
        BigDecimal dedn = (BigDecimal) billAmtBean.get("insurance_deduction");

        BigDecimal totalReceipts = (BigDecimal) billAmtBean.get("total_receipts");
        BigDecimal depositSetOff = (BigDecimal) billAmtBean.get("deposit_set_off");
        BigDecimal pointsRedeemedAmt = (BigDecimal) billAmtBean.get("points_redeemed_amt");

        BigDecimal totalSponsorReceipts = BigDecimal.ZERO;
        BigDecimal primaryTotalSponsorReceipts = billAmtBean
            .get("primary_total_sponsor_receipts") == null ? BigDecimal.ZERO
                : (BigDecimal) billAmtBean.get("primary_total_sponsor_receipts");
        BigDecimal secondaryTotalSponsorReceipts = billAmtBean
            .get("secondary_total_sponsor_receipts") == null ? BigDecimal.ZERO
                : (BigDecimal) billAmtBean.get("secondary_total_sponsor_receipts");

        totalSponsorReceipts = totalSponsorReceipts.add(primaryTotalSponsorReceipts)
            .add(secondaryTotalSponsorReceipts);
        BigDecimal totalClaimRecd = billAmtBean.get("claim_recd_amount") == null ? BigDecimal.ZERO
            : (BigDecimal) billAmtBean.get("claim_recd_amount");
        // BigDecimal claimReturnAmt = billAmtBean.get("total_claim_return") == null ?
        // BigDecimal.ZERO : (BigDecimal) billAmtBean.get("total_claim_return");
        BigDecimal claimReturnAmt = BigDecimal.ZERO;

        BigDecimal patientAmt = billAmt.subtract(priClaimAmt).subtract(secClaimAmt);
        BigDecimal patientCredits = totalReceipts.add(depositSetOff).add(pointsRedeemedAmt);
        BigDecimal insAmt = priClaimAmt.add(secClaimAmt);

        if (newStatus.equals(Bill.BILL_STATUS_CLOSED)) {

          // Payment status check
          if (!paymentForceClose) {
            if (patientAmt.compareTo(patientCredits) != 0) {
              sb.append("Bill status update failed: Bill amount (" + patientAmt
                  + ") is not equal to receipts total amount (" + patientCredits + ")"
                  + (dedn.compareTo(BigDecimal.ZERO) != 0 ? " (or) check patient deduction." : ""));
            }
          }

          // Claim status check
          if (isTpa && !claimForceClose) {

            if (insAmt.add(claimReturnAmt).compareTo(BigDecimal.ZERO) > 0) {
              if (insAmt.add(claimReturnAmt)
                  .compareTo(totalClaimRecd.add(totalSponsorReceipts)) != 0) {
                sb.append("Bill status update failed: Claim amount (" + insAmt.add(claimReturnAmt)
                    + ") is not equal to sponsor received amount ("
                    + totalClaimRecd.add(totalSponsorReceipts) + ")");
              }
            }
          }

          closedDate = com.bob.hms.common.DateUtil.getCurrentTimestamp();
          closedBy = userId;

        }
      }

      // Set the finalized by when the new status is finalized or closed
      // (And) the Bill is a Credit bill or Bill now with TPA (And) the finalized by is empty.
      Timestamp lastFinalizedAt = (Timestamp) bill.get("last_finalized_at");
      if ((newStatus.equals(Bill.BILL_STATUS_FINALIZED)
          || (newStatus.equals(Bill.BILL_STATUS_CLOSED)
              && (bill.get("bill_type").equals(Bill.BILL_TYPE_CREDIT)
                  || (bill.get("bill_type").equals(Bill.BILL_TYPE_PREPAID)
                      && (Boolean) bill.get("is_tpa")))))
          && (bill.get("finalized_by") == null || bill.get("finalized_by").equals(""))) {
        finalizedBy = userId;
      }
      Timestamp now = new Timestamp(new java.util.Date().getTime());
      if ((newStatus.equals(Bill.BILL_STATUS_FINALIZED)
          || newStatus.equals(Bill.BILL_STATUS_CLOSED))
            && !bill.get("status").equals(Bill.BILL_STATUS_FINALIZED)) {
        lastFinalizedAt = now;
      }

      if (newStatus.equals(Bill.BILL_STATUS_CANCELLED)) {
        closedBy = userId;
        if (lastFinalizedAt != null){
          lastFinalizedAt = now;
        }
      }

      Map keys = new HashMap();
      keys.put("bill_no", bill.get("bill_no"));

      BasicDynaBean billBean = billService.getBean();
      billBean.set("username", userId);
      billBean.set("status", newStatus);
      billBean.set("payment_status", paymentStatus);
      billBean.set("discharge_status", dischargeStatus);
      billBean.set("finalized_date", finalizedDate);
      billBean.set("last_finalized_at", lastFinalizedAt);
      billBean.set("finalized_by", finalizedBy);
      billBean.set("closed_date", closedDate);
      billBean.set("mod_time", now);
      billBean.set("closed_by", closedBy);

      boolean success = billService.update(billBean, keys);

      keys = new HashMap<String, Object>();
      keys.put("mr_no", mrNo);
      BasicDynaBean mrdCaseFileBean = mrdCaseFileRepo.getBean();
      mrdCaseFileBean.set("case_status", "P");

      if (dischargeStatus.equals("Y") && success)
        success = success && mrdCaseFileRepo.update(mrdCaseFileBean, keys) > 0;

      // GenericDAO claimDAO = new GenericDAO("bill_claim");
      // PatientInsurancePlanDAO insPlanDAO = new PatientInsurancePlanDAO();
      int[] planIds = patInsPlansService.getPlanIds((String) bill.get("visit_id"));

      // Update claim if all bills are closed
      List<String> claims = new ArrayList<String>();
      if (null != planIds && (Boolean) bill.get("is_tpa")) {
        for (int i = 0; i < planIds.length; i++) {
          keys = new HashMap<String, Object>();
          keys.put("plan_id", planIds[i]);
          keys.put("bill_no", bill.get("bill_no"));
          keys.put("visit_id", bill.get("visit_id"));
          BasicDynaBean bean = billClaimService.findByKey(keys);
          if (null != bean)
            claims.add((String) bean.get("claim_id"));
        }
      }

      if ((Boolean) bill.get("is_tpa")) {
        String billNo = (String) bill.get("bill_no");
        if (newStatus.equals(Bill.BILL_STATUS_CLOSED)) {
          for (String claimId : claims) {
            billClaimService.closeBillClaim(billNo, claimId);
          }
        } else {
          // if only the claim status was updated, close the claim
          BasicDynaBean b = billService.findByKey((String) bill.get("bill_no"));
          for (String claimId : claims) {
            if (billClaimService.isClaimClosed(b, claimId)) {
              billClaimService.closeBillClaim((String) bill.get("bill_no"), claimId);
            }
          }
        }
      }

      if (success)
        allSuccess = true;
    }
    return sb.toString();

  }

  //THIS METHOD IS ABSOLETE
  private List<BasicDynaBean> getBeans(List<Receipt> receiptList,
      ArrayList<BasicDynaBean> receiptBeanList, BasicDynaBean bill, String billStatus) {
    int centerId = regService.getCenterId((String) bill.get("visit_id"));
    for (int i = 0; i < receiptList.size(); i++) {
      BasicDynaBean receiptBean = receiptRepository.getBean();
      receiptBean.set("receipt_type", receiptList.get(i).getReceiptType());
      receiptBean.set("bill_no", receiptList.get(i).getBillNo());
      receiptBean.set("amount", receiptList.get(i).getAmount());
      receiptBean.set("display_date", receiptList.get(i).getReceiptDate());
      receiptBean.set("payment_mode_id", receiptList.get(i).getPaymentModeId());
      receiptBean.set("card_type_id", receiptList.get(i).getCardTypeId());
      receiptBean.set("username", receiptList.get(i).getUsername());
      receiptBean.set("remarks", receiptList.get(i).getRemarks());
      receiptBean.set("counter", receiptList.get(i).getCounter());

      String receiptNo = billReceiptRepo.getNextReceiptNo((String) bill.get("bill_type"),
          (String) bill.get("visit_type"), "N", (String) receiptList.get(i).getPaymentType(),
          centerId);
      if (receiptNo.equals("")) {
        throw new HMSException(HttpStatus.BAD_REQUEST, "exception.receipt.sequence.not.configured",
            null);
      }
      receiptList.get(i).setReceiptNo(receiptNo);
      receiptBean.set("receipt_no", receiptNo);
      receiptBeanList.add(receiptBean);
    }
    return receiptBeanList;
  }

  private boolean createReceipt(Receipt receiptDtoObject, BasicDynaBean bill) {
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
      if ("F".equals(receiptType)) {
        // If the receipt type if F the amount will be negative but we need to send a positive
        // amount to be reduced.
        allocationService.reduceDepositSetoffAmount(billNo, depositType, amount.negate());
      } else {
        allocationService.splitDepositAmountToSetOff(billNo, depositType, amount);
      }
      allocationService.updateBillTotal(billNo);
      return true;
    }
    int centerId = regService.getCenterId((String) bill.get("visit_id"));
    String receiptNo = billReceiptRepo.getNextReceiptNo((String) bill.get("bill_type"),
        (String) bill.get("visit_id"), "N", receiptDtoObject.getPaymentType(),
        centerId);
    if (receiptNo.equals("")) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.receipt.sequence.not.configured",
          null);
    }
    receiptDtoObject.setReceiptNo(receiptNo);
    createReceiptEntry(receiptDtoObject);
    createBillReceipt(receiptDtoObject);
    // Have to commit so that the receipt is availabe to hibernate.
    if ("F".equals(receiptType)) {
      //Have commented, Refund receipt reference to be called at the end of the job
      //allocationService.createReceiptRefundReference(receiptDtoObject);
    }

    Map<String, Object> receiptData = new HashMap<>();
    receiptData.put("receiptId", receiptDtoObject.getReceiptNo());
    receiptData.put("reversalsOnly", Boolean.FALSE);
    accountingJobScheduler.scheduleAccountingForReceipt(receiptData);

    allocationService.updateBillTotal(billNo);

    // Call the allocation method
    allocationService.allocate(billNo, centerId);

    return true;
  }

  private boolean createReceiptEntry(Receipt receiptDtoObject) {
    ReceiptModel receipt = new ReceiptModel(receiptDtoObject);
    Timestamp now = new Timestamp(new java.util.Date().getTime());
    receipt.setModifiedAt(now);
    receipt.setCreatedAt(now);
    receiptHibernateRepository.persist(receipt);
    receiptHibernateRepository.flush();
    return true;
  }

  private boolean createBillReceipt(Receipt receiptDtoObject) {

    BillReceiptsModel billReceipt = new BillReceiptsModel();
    billReceipt.setReceiptNo(new ReceiptModel(receiptDtoObject.getReceiptNo()));
    billReceipt.setUsername(receiptDtoObject.getUsername());
    billReceipt.setBillNo(new BillModel(receiptDtoObject.getBillNo()));
    billReceipt.setModTime(new Date());
    if (receiptDtoObject.getSponsorIndex() != null) {
      billReceipt.setSponsorIndex(receiptDtoObject.getSponsorIndex().charAt(0));
    }
    receiptHibernateRepository.persist(billReceipt);
    receiptHibernateRepository.flush();

    Map<String, Map<String, String>> receiptUsageMap = new HashMap<>();
    Map<String, String> entityMap = new HashMap<>();
    entityMap.put(receiptDtoObject.getBillNo(), BillConstants.Restrictions.BILL_NO);
    receiptUsageMap.put(receiptDtoObject.getReceiptNo(), entityMap);
    return createReceiptUsage(receiptUsageMap);
  }

  private boolean createReceiptUsage(Map<String, Map<String, String>> receiptUsageMap) {

    if (!CollectionUtils.isEmpty(receiptUsageMap)) {
      for (Entry<String, Map<String, String>> entry : receiptUsageMap.entrySet()) {
        if (!CollectionUtils.isEmpty(entry.getValue())) {
          for (Entry<String, String> entityMap : entry.getValue().entrySet()) {
            ReceiptUsageModel receiptUsage = new ReceiptUsageModel();
            ReceiptUsageIdSequence receiptUsageId = new ReceiptUsageIdSequence();
            receiptUsageId.setReceiptId(entry.getKey());
            receiptUsageId.setEntityType(entityMap.getValue());
            receiptUsageId.setEntityId(entityMap.getKey());
            receiptUsage.setId(receiptUsageId);
            receiptHibernateRepository.persist(receiptUsage);
          }
          receiptHibernateRepository.flush();
        }
      }
    }

    return true;
  }

  private boolean insertReceipt(List<Receipt> receiptList, BasicDynaBean bill) {

    if (receiptList != null && !receiptList.isEmpty()) {
      for (Receipt receipt : receiptList) {
        createReceipt(receipt, bill);
      }
      allocationService.updateBillTotal((String)bill.get("bill_no"));
    }
    return true;
  }

  @SuppressWarnings("rawtypes")
  private Object getValue(String key, Map params, boolean sendNull) {
    Object obj = params.get(key);
    if (sendNull && obj == null)
      return null;
    else if (obj != null) {
      return obj;
    }
    return "";
  }

  @SuppressWarnings("rawtypes")
  private Object getValue(String key, Map params) {
    return getValue(key, params, false);
  }

  /**
   * Find by key.
   *
   * @param receiptno
   *          the receipt no
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(String receiptno) {
    return receiptRepository.findByKey("receipt_id", receiptno);
  }

  public List<BasicDynaBean> findAllBillReceipts(String receiptNo) {
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("receipt_no", receiptNo);
    return billReceiptRepo.findByCriteria(filterMap);
  }

  public Boolean isReceiptNoValid(String receiptNumber) {
    return billReceiptRepo.exist("receipt_no", receiptNumber);
  }

	public BasicDynaBean getCashLimit(String mrNo, String visitId) {
		return receiptRepository.getCashPayments(mrNo, visitId);
	}

	public BasicDynaBean getDepositCashLimit(String mrNo) {
		return receiptRepository.getDepositCashPayments(mrNo);
	}

  public List<BasicDynaBean> getReceiptsNotInHmsAccountingInfo() {
    BasicDynaBean genPerfs = genPrefService.getPreferences();
    int relStart = (int) genPerfs.get("accounting_missing_data_scan_rel_start");
    int relEnd = (int) genPerfs.get("accounting_missing_data_scan_rel_end");
    return receiptRepository.getReceiptsNotInHmsAccountingInfo(relStart, relEnd);
  }
}
