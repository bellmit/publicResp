package com.insta.hms.core.billing;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/receiptAmountAllocation")
public class AllocationController extends BaseRestController {

  @LazyAutowired
  AllocationService allocationService;

  @GetMapping("/allocate")
  public ResponseEntity<List<BillChargeReceiptAllocationModel>> allocate(
      @RequestParam String billNo) {
    return new ResponseEntity<>(allocationService.allocate(billNo), HttpStatus.OK);
  }

  @IgnoreConfidentialFilters
  @GetMapping("/allocateAllOpenBills")
  public ResponseEntity<List<String>> allocateAllOpenBills() {
    return new ResponseEntity<>(allocationService.allocateAllOpenBills(), HttpStatus.OK);
  }

  @IgnoreConfidentialFilters
  @GetMapping("/allocateAllOpenBillsForDavita")
  public ResponseEntity<List<String>> allocateAllOpenBillsForDavita(
      @RequestParam(value = "from") String startDate,
      @RequestParam(value = "to") String endDate) {
    return new ResponseEntity<>(
        allocationService.allocateAllOpenBillsForDavita(startDate, endDate), HttpStatus.OK);
  }
}