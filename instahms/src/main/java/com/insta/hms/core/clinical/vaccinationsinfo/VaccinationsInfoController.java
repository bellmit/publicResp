package com.insta.hms.core.clinical.vaccinationsinfo;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/vaccinationsinfo")
public class VaccinationsInfoController extends BaseRestController {

  @LazyAutowired
  VaccinationsInfoService vaccinationsInfoService;

  @IgnoreConfidentialFilters
  @GetMapping(value = "/searchItems")
  public ResponseEntity<Map<String, Object>> searchItems(@RequestParam(
      value = "query") String searchQuery) throws Exception {
    return new ResponseEntity<>(vaccinationsInfoService.searchMedicineItems(searchQuery),
        HttpStatus.OK);
  }

  @IgnoreConfidentialFilters
  @GetMapping(value = "/itemRoutes")
  public ResponseEntity<Map<String, Object>> getItemRoutes(@RequestParam(
      value = "medicine_id") Integer medicineId) throws Exception {
    return new ResponseEntity<>(vaccinationsInfoService.getItemRoutes(medicineId), HttpStatus.OK);
  }

  @GetMapping(value = "/itemBatchDetails")
  public ResponseEntity<Map<String, Object>> getItemBatchDetails(
      @RequestParam(value = "mr_no") String mrNo,
      @RequestParam(value = "medicine_id") Integer medicineId)
      throws Exception {
    return new ResponseEntity<>(vaccinationsInfoService.getPatientBatchDetailsForSelectedItem(mrNo,
        medicineId), HttpStatus.OK);
  }

  @IgnoreConfidentialFilters
  @GetMapping(value = "/vaccineCategoryList")
  public ResponseEntity<Map<String, Object>> getVaccineCategoryList(@RequestParam(
      value = "vaccine_id") Integer vaccineId) throws Exception {
    return new ResponseEntity<>(vaccinationsInfoService.getVaccineCategoryListForSelectedVaccine(
        vaccineId), HttpStatus.OK);
  }

}