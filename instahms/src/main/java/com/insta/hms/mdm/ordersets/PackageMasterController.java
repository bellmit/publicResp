package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;
import com.insta.hms.master.packages.PlanDAO;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;
import com.insta.hms.mdm.insuranceplantypes.InsurancePlanTypeService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class PackageMasterController.
 */
@RestController
@RequestMapping(URLRoute.PACKAGES_PATH)
public class PackageMasterController extends PackageBaseController {

  /**
   * Instantiates a new package master controller.
   *
   * @param service the service
   */
  public PackageMasterController(PackageService service) {
    super(service, "packages");
  }

  /** The package service. */
  @LazyAutowired
  private PackageService packageService;

  /** The package charges service. */
  @LazyAutowired
  private PackageChargesService packageChargesService;

  /** The package content charges service. */
  @LazyAutowired
  private PackageContentChargesService packageContentChargesService;

  /** The Constant planDAO. */
  private static final PlanDAO planDAO = new PlanDAO();

  /**
   * Find by id.
   *
   * @param packageId the package id
   * @return the response entity
   */
  @RequestMapping(value = "/findById", method = RequestMethod.GET)
  public ResponseEntity<PackagesDTO> findById(@RequestParam Integer packageId) {
    PackagesDTO response = this.packageService.findById(packageId);
    if (response != null) {
      return new ResponseEntity<>(response, HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  /**
   * Find contents of a package.
   *
   * @param packageId the package id
   * @return the response entity
   */
  @RequestMapping(value = "/getpackagecontents", method = RequestMethod.GET)
  public ResponseEntity<PackagesDTO> getPackageContents(@RequestParam Integer packageId) {
    PackagesDTO response = this.packageService.getPackageContents(packageId);
    if (response != null) {
      return new ResponseEntity<>(response, HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  /**
   * Save.
   *
   * @param packagesDTO the packages DTO
   * @return the response entity
   */
  @RequestMapping(value = "/save", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<HttpStatus> save(@RequestBody PackagesDTO packagesDTO) {
    packageService.save(packagesDTO);
    return new ResponseEntity<HttpStatus>(HttpStatus.CREATED);
  }

  /**
   * Update.
   *
   * @param packagesDTO the packages DTO
   * @return the response entity
   */
  @RequestMapping(value = "/update", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<HttpStatus> update(@RequestBody PackagesDTO packagesDTO) {
    packageService.update(packagesDTO);
    return new ResponseEntity<HttpStatus>(HttpStatus.OK);
  }

  /**
   * Update Package Charges.
   * 
   * @param packageChargesDTO packageChargesRequest
   * @return updated packageCharges
   */
  @RequestMapping(value = "/charges/update", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PackageChargesDTO> updatePackageCharges(
      @RequestBody PackageChargesDTO packageChargesDTO) {
    PackageChargesDTO response = this.packageChargesService.update(packageChargesDTO);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  /**
   * Find Package charge.
   * 
   * @param packageId package identifier
   * @param orgId organization id
   * @return package charge
   */
  @RequestMapping(value = "/charges/find", method = RequestMethod.GET)
  public ResponseEntity findPackageCharges(@RequestParam int packageId,
      @RequestParam String orgId) {
    Map<String, String> validationsObj = this.packageChargesService
        .getNullableValidations(packageId);
    if (null != validationsObj) {
      return new ResponseEntity<>(
          validationsObj, null, HttpStatus.OK
      );
    }
    return new ResponseEntity<>(
        this.packageChargesService.findByPackageIdAndOrgId(packageId, orgId), HttpStatus.OK);
  }

  /**
   * Find Charge head.
   * 
   * @param itemType item type
   * @param id id
   * @param consultationTypeId consultationTypeId
   * @return charge head
   * @throws Exception exception
   */
  @RequestMapping(value = "/chargehead/find", method = RequestMethod.GET)
  public ResponseEntity<String> getChargeHead(@RequestParam String itemType,
      @RequestParam String id, @RequestParam String consultationTypeId) throws Exception {
    String chargeHead =
        this.packageService.getChargeHeadByItemType(itemType, id, consultationTypeId);
    return new ResponseEntity<>(chargeHead, HttpStatus.OK);
  }

  /**
   * Gets the eligible insurance plans for sponsors.
   *
   * @param tpaIdArray the tpa id array
   * @param filterText the filter text
   * @return the eligible insurance plans for sponsors
   * @throws Exception the exception
   */
  @GetMapping(value = "/getEligibleInsurancePlans")
  public ResponseEntity<List<Map<String, Object>>> getEligibleInsurancePlansForSponsors(
      @RequestParam String[] tpaIdArray, @RequestParam String filterText) throws Exception {
    List<Map<String, Object>> eligibleInsurancePlans = new ArrayList<>();
    if (tpaIdArray != null && "all".equalsIgnoreCase(tpaIdArray[0])) {
      tpaIdArray = null;
    }
    eligibleInsurancePlans.addAll(ConversionUtils
          .listBeanToListMap(planDAO.getEligiblePlans(tpaIdArray, filterText)));
    return new ResponseEntity<List<Map<String, Object>>>(eligibleInsurancePlans,
        HttpStatus.OK);
  }

  /**
   * Returns if there exists a package with the given package code.
   *
   * @param packageCode the packageCode
   * @return if there exists a package with the given package code
   */
  @GetMapping(value = "/findByPackageCode")
  public ResponseEntity<Boolean> findByPackageCode(@RequestParam String packageCode) {
    Boolean existsByPackageCode = this.packageService.findByPackageCode(packageCode);
    return new ResponseEntity<Boolean>(existsByPackageCode, HttpStatus.OK);
  }
}
