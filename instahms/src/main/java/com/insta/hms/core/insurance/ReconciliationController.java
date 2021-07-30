package com.insta.hms.core.insurance;

import com.fasterxml.jackson.annotation.JsonView;
import com.insta.hms.common.BaseController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.billing.ReceiptConstants;
import com.insta.hms.core.billing.ReceiptModel;
import com.insta.hms.integration.insurance.remittance.ReconciliationModel;
import com.insta.hms.mdm.paymentmode.PaymentModeMasterModel;
import com.insta.hms.mdm.tpas.TpaMasterModel;
import com.insta.hms.util.ViewProfiles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(URLRoute.REMITTANCE_RECONCILIATION)
public class ReconciliationController extends BaseController {

  /**
   * The remittance service.
   */
  @Autowired
  private ReconciliationService reconciliationService;

  @IgnoreConfidentialFilters
  @GetMapping("/index")
  public ModelAndView getReconcilitaionIndexPage() {
    return renderFlowUi("Remittance Reconciliation", "insurance", "withFlow", "insuranceFlow",
        "remittanceReconciliation", false);
  }

  /**
   * Adds the sponsor receipt.
   *
   * @param json the json
   * @return the response entity
   */
  @IgnoreConfidentialFilters
  @PostMapping("/addSponsorReceipt")
  public ResponseEntity<Map<String, Object>> addSponsorReceipt(
      @RequestBody Map<String, String> json) {
    ReceiptModel sponsorReceipt = new ReceiptModel();
    sponsorReceipt.setIsSettlement(false);
    sponsorReceipt.setReceiptType(ReceiptConstants.RECEIPT);

    try {
      sponsorReceipt.setAmount(new BigDecimal(json.get("payment_amount")));

      sponsorReceipt.setTpaId(new TpaMasterModel(json.get("tpa_id")));

      sponsorReceipt.setCenterId(Integer.parseInt(json.get("center_id")));

      sponsorReceipt.setPaymentModeId(
          new PaymentModeMasterModel(Integer.parseInt(json.get("payment_mode_id"))));

      SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
      Date paymentReceivedDate;
      paymentReceivedDate = format.parse(json.get("received_date"));
      sponsorReceipt.setPaymentReceivedDate(paymentReceivedDate);

      sponsorReceipt.setRemarks(json.get("narration"));

      sponsorReceipt.setAmount(new BigDecimal(json.get("payment_amount")));

      String tdsAmountString = json.get("tds_amount");
      if (null != tdsAmountString) {
        sponsorReceipt.setTdsAmount(new BigDecimal(tdsAmountString));
      } else {
        sponsorReceipt.setTdsAmount(BigDecimal.ZERO);
      }

      String otherAmountString = json.get("other_deductions");
      if (null != otherAmountString) {
        sponsorReceipt.setOtherDeductions(new BigDecimal(otherAmountString));
      } else {
        sponsorReceipt.setOtherDeductions(BigDecimal.ZERO);
      }

      BigDecimal unallocatedAmount = sponsorReceipt.getAmount().add(sponsorReceipt.getTdsAmount())
          .add(sponsorReceipt.getOtherDeductions());
      sponsorReceipt.setUnallocatedAmount(unallocatedAmount);

      sponsorReceipt.setBankName(json.get("bank"));

      sponsorReceipt.setBankBatchNo(json.get("bank_batch_number"));

      sponsorReceipt.setReferenceNo(json.get("payment_reference_number"));
    } catch (ParseException | NumberFormatException ex) {
      Map<String, Object> map = new HashMap<>();
      map.put("error", ex.toString());
      return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }

    int res = reconciliationService.addSponsorReceipt(sponsorReceipt);
    Map<String, Object> map = new HashMap<>();
    map.put("result", res);
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  @IgnoreConfidentialFilters
  @PostMapping("/updateSponsorReceipt")
  public ResponseEntity<Map<String, Object>> updateSponsorReceipt(
      @RequestBody Map<String, String> json, @RequestParam("receipt_id") String receiptId) {
    reconciliationService.updateSponsorReceipt(receiptId, json);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Gets the remittance details.
   *
   * @param reconciliationId the reconciliation id
   * @return the remittance details
   */
  @IgnoreConfidentialFilters
  @JsonView(ViewProfiles.Public.class)
  @GetMapping("/getReconciliationDetails")
  public ResponseEntity<ReconciliationModel> getReconciliationDetails(
      @RequestParam("reconciliation_id") int reconciliationId) {
    ReconciliationModel reconciliation = reconciliationService
        .getSponsorReceiptDetails(reconciliationId);
    if (null != reconciliation) {
      return new ResponseEntity<>(reconciliation, HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
  }

  @IgnoreConfidentialFilters
  @GetMapping("/getSponsorReceiptAmountDetails")
  public ResponseEntity<Object> getSponsorReceiptAmountDetails(
      @RequestParam("reconciliation_id") int reconciliationId) {
    Object data = reconciliationService.getSponsorReceiptAmountDetails(reconciliationId);
    return new ResponseEntity<>(data, HttpStatus.OK);
  }

  @IgnoreConfidentialFilters
  @GetMapping("/getBillsOfBatches")
  public ResponseEntity<Object> getBillsOfBatch(
      @RequestParam("batch_id") List<String> submissionBatchIdlist) {
    Object bills = reconciliationService.getBillsFromBatch(submissionBatchIdlist);
    return new ResponseEntity<>(bills, HttpStatus.OK);
  }

  @IgnoreConfidentialFilters
  @GetMapping("/getBillsOfBillNo")
  public ResponseEntity<Object> getBillsOfBillNo(@RequestParam("bill_no") List<String> billNoList,
      @RequestParam(value = "tpa_id", required = false) String tpaId,
      @RequestParam(value = "center_id") Integer centerId) {
    Object bills = reconciliationService.getBillsOfBillNo(billNoList, tpaId, centerId);
    return new ResponseEntity<>(bills, HttpStatus.OK);
  }

  @IgnoreConfidentialFilters
  @GetMapping("/getBillsOfMrNo")
  public ResponseEntity<Object> getBillsOfMrNo(@RequestParam("mr_no") List<String> mrNoList,
      @RequestParam("tpa_id") String tpaId) {
    Object bills = reconciliationService.getBillsOfMrNo(mrNoList, tpaId);
    return new ResponseEntity<>(bills, HttpStatus.OK);
  }

  @IgnoreConfidentialFilters
  @GetMapping("/getBillsOfClaim")
  public ResponseEntity<Object> getBillsOfClaim(
      @RequestParam("claim_id") List<String> claimIdList) {
    Object bills = reconciliationService.getBillsOfClaim(claimIdList);
    return new ResponseEntity<>(bills, HttpStatus.OK);
  }

  @IgnoreConfidentialFilters
  @GetMapping("/getBatchesByTpa")
  public ResponseEntity<Object> getBatchesByTpa(@RequestParam("tpa_id") String tpaId,
      @RequestParam("center_id") Integer centerId) {
    Object batches = reconciliationService.getBatchesByTpa(tpaId, centerId);
    return new ResponseEntity<>(batches, HttpStatus.OK);
  }

  /**
   * Gets the bill charges.
   *
   * @param billNo  the bill no
   * @param claimId the claim id
   * @return the bill charges
   */
  @IgnoreConfidentialFilters
  @GetMapping("/getBillCharges")
  public ResponseEntity<List<Map<String, Object>>> getBillCharges(
      @RequestParam("bill_no") String billNo, @RequestParam("claim_id") String claimId) {
    List<Map<String, Object>> charges = reconciliationService.getBillCharges(billNo, claimId);
    return new ResponseEntity<>(charges, HttpStatus.OK);
  }

  /**
   * Save.
   *
   * @param parameters the parameters
   * @param isDraft    the is draft
   * @return the response entity
   */
  @IgnoreConfidentialFilters
  @PostMapping("/saveReconciliation")
  public ResponseEntity<Map<String, Object>> save(@RequestBody Map<String, Object> parameters,
      @RequestParam(value = "isDraft", required = false, defaultValue = "true") boolean isDraft) {
    int reconciliationId = (int) parameters.get("reconciliation_id");
    Map<String, Map<String, Object>> bills =
        (Map<String, Map<String, Object>>) parameters.get("bills");
    String errorMessage = ReconciliationValidator.validateRemittanceRequest(bills);
    if (!("".equals(errorMessage))) {
      Map<String, Object> resMap = new HashMap<>();
      resMap.put("error", errorMessage);
      return new ResponseEntity<>(resMap, HttpStatus.BAD_REQUEST);
    }
    Map<String, Object> response = new HashMap();
    response.put("remittance_id", reconciliationId);
    response.put("bills", reconciliationService.parseAndSave(reconciliationId, bills, isDraft));
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  /**
   * Gets list.
   *
   * @param tpaId    the tpa id
   * @param mrNo     the mr no
   * @param billNo   the bill no
   * @param claimId  the claim id
   * @param centerId the center id
   * @return the list
   */
  @IgnoreConfidentialFilters
  @GetMapping("/getList")
  public ResponseEntity<List<Map<String, Object>>> getList(@RequestParam("tpa_id") String tpaId,
      @RequestParam(value = "mr_no", required = false) String mrNo,
      @RequestParam(value = "bill_no", required = false) String billNo,
      @RequestParam(value = "claim_id", required = false) String claimId,
      @RequestParam(value = "center_id") Integer centerId
  ) {
    List<Map<String, Object>> result =
        reconciliationService.getList(tpaId, mrNo, billNo, claimId, centerId);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @IgnoreConfidentialFilters
  @GetMapping("/getDraftedDetails")
  public ResponseEntity getDraftedDetails(@RequestParam("reconciliation_id") int reconciliationId) {
    return new ResponseEntity<>(reconciliationService.getDraftedDetails(reconciliationId),
        HttpStatus.OK);
  }

  @IgnoreConfidentialFilters
  @GetMapping("/getRemittedBillDetails")
  public ResponseEntity getRemittedBillDetails(@RequestParam("reconciliation_id")
      int reconciliationId) {
    return new ResponseEntity<>(reconciliationService.getRemittedBillDetails(reconciliationId),
        HttpStatus.OK);
  }

  /**
   * Gets the bills by date.
   *
   * @param tpaId    the tpa id
   * @param fromDate the from date
   * @param toDate   the to date
   * @return the bills by date
   */
  @IgnoreConfidentialFilters
  @GetMapping("/getBillsByDate")
  public ResponseEntity<Object> getBillsByDate(@RequestParam("tpa_id") String tpaId,
      @RequestParam("center_id") Integer centerId,
      @RequestParam("from_date") @DateTimeFormat(pattern = "dd-MM-yyyy") Date fromDate,
      @RequestParam("to_date") @DateTimeFormat(pattern = "dd-MM-yyyy") Date toDate) {
    String errorMessage = ReconciliationValidator.validateDateRange(fromDate, toDate);
    if (!("".equals(errorMessage))) {
      Map<String, Object> resMap = new HashMap<>();
      resMap.put("error", errorMessage);
      return new ResponseEntity<Object>(resMap, HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(
        reconciliationService.getBillsByFinalizedDate(tpaId, centerId, fromDate, toDate),
        HttpStatus.OK);

  }

  /**
   * Gets the tpa list.
   *
   * @param tpaName  the tpa name
   * @param fromDate the from date
   * @param toDate   the to date
   * @return the tpa list
   */
  @IgnoreConfidentialFilters
  @GetMapping("/getTpaList")
  public ResponseEntity<List<Map>> getTpaList(@RequestParam("tpa_name") String tpaName,
      @RequestParam(value = "from_date", required = false)
      @DateTimeFormat(pattern = "dd-MM-yyyy") Date fromDate,
      @RequestParam(value = "to_date", required = false)
      @DateTimeFormat(pattern = "dd-MM-yyyy") Date toDate) {
    return new ResponseEntity(reconciliationService.getTpaList(tpaName, fromDate, toDate),
        HttpStatus.OK);

  }

  /**
   * Gets the payment reference list.
   *
   * @param referenceNo the reference no
   * @param tpaId       the tpa id
   * @param fromDate    the from date
   * @param toDate      the to date
   * @return the payment reference list
   */
  @IgnoreConfidentialFilters
  @GetMapping("/getPaymentReferenceList")
  public ResponseEntity<List<String>> getPaymentReferenceList(
      @RequestParam("reference_no") String referenceNo,
      @RequestParam(value = "tpa_id", required = false) String tpaId,
      @RequestParam(value = "from_date", required = false)
      @DateTimeFormat(pattern = "dd-MM-yyyy") Date fromDate,
      @RequestParam(value = "to_date", required = false)
      @DateTimeFormat(pattern = "dd-MM-yyyy") Date toDate) {
    return new ResponseEntity(
        reconciliationService.gePaymentReferences(referenceNo, tpaId, fromDate, toDate),
        HttpStatus.OK);

  }

  /**
   * Gets the sponsor receipt list.
   *
   * @param receiptId   the receipt id
   * @param referenceNo the reference no
   * @param tpaId       the tpa id
   * @param fromDate    the from date
   * @param toDate      the to date
   * @return the sponsor receipt list
   */
  @IgnoreConfidentialFilters
  @GetMapping("/getSponsorReceiptList")
  public ResponseEntity<List<Map>> getSponsorReceiptList(
      @RequestParam("receipt_id") String receiptId,
      @RequestParam(value = "reference_no", required = false) String referenceNo,
      @RequestParam(value = "tpa_id", required = false) String tpaId,
      @RequestParam(value = "from_date", required = false)
      @DateTimeFormat(pattern = "dd-MM-yyyy") Date fromDate,
      @RequestParam(value = "to_date", required = false)
      @DateTimeFormat(pattern = "dd-MM-yyyy") Date toDate) {
    return new ResponseEntity(reconciliationService.getSponsorReceiptList(receiptId, referenceNo,
        tpaId, fromDate, toDate), HttpStatus.OK);

  }

  /**
   * Gets the bills list.
   *
   * @param activityList the claim activity id
   * @return the bills list
   */

  @IgnoreConfidentialFilters
  @GetMapping("/getBillsOfActivityIds")
  public ResponseEntity<Map<String, Object>> getBillsOfActivityIds(@RequestParam("activity_id")
      List<String> activityList, @RequestParam("center_id") Integer centerId,
      @RequestParam("tpa_id") String tpaId) {
    List<Map<String, Object>> bills =
        reconciliationService.getBillsOfActivityIds(activityList, centerId, tpaId);
    Map claimCharges = reconciliationService
        .getBillCharges(bills.stream().map(bill -> (String) bill.get("bill_no")).collect(
            Collectors.toList()));

    Map<String, Object> map = new HashMap<>();
    map.put("bills", bills);
    map.put("claimCharges", claimCharges);
    return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
  }

  @IgnoreConfidentialFilters
  @PostMapping("/validateCsv")
  public List<Map<String, Object>> validateCsvData(@RequestBody Map csv,
      @RequestParam(name = "center_id") Integer centerId,
      @RequestParam(name = "tpa_id") String tpaId, @RequestParam(name = "type") String type) {
    return reconciliationService.validateCsvData((List<Map<String, Object>>) csv.get("csv_data"),
        (Map<String, String>) csv.get("key_map"), centerId, tpaId, type);
  }


}
