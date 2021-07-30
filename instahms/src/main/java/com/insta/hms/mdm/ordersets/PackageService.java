package com.insta.hms.mdm.ordersets;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.order.master.OrderService;
import com.insta.hms.core.inventory.stockmgmt.StockService;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.jobs.JobService;
import com.insta.hms.jobs.MasterChargesCronSchedulerDetailsRepository;
import com.insta.hms.master.CommonChargesMaster.CommonChargesDAO;
import com.insta.hms.mdm.bedtypes.BedTypeService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;
import com.insta.hms.mdm.codetype.CodeTypeService;
import com.insta.hms.mdm.commoncharges.CommonChargesRepository;
import com.insta.hms.mdm.insuranceitemcategories.InsuranceItemCategoryService;
import com.insta.hms.mdm.itemgroups.ItemGroupsService;
import com.insta.hms.mdm.organization.OrganizationService;
import com.insta.hms.mdm.taxgroups.TaxGroupService;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class PackageService.
 *
 * @author manika.singh
 * @since 02/04/19
 */
@Service
public class PackageService extends GenericPackagesService {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(PackageService.class);
  public static final String ACTION_UPDATE_PACKAGE = "updatePackage";
  public static final String ACTION_UPDATE_PACKAGE_CONTENTS = "updatePackageContents";
  public static final String ACTION_NEW_PACKAGE = "newPackage";

  /**
   * The item groups service.
   */
  @LazyAutowired
  private ItemGroupsService itemGroupsService;

  /**
   * The bed type service.
   */
  @LazyAutowired
  private BedTypeService bedTypeService;

  /**
   * The tax sub group service.
   */
  @LazyAutowired
  private TaxSubGroupService taxSubGroupService;

  /**
   * The tax group service.
   */
  @LazyAutowired
  private TaxGroupService taxGroupService;

  /**
   * The package jpa repository.
   */
  @LazyAutowired
  private PackageJpaRepository packageJpaRepository;

  /**
   * The package content jpa repository.
   */
  @LazyAutowired
  private PackageContentService packageContentService;

  /**
   * The package model.
   */
  @LazyAutowired
  private PackageContentsModel packageContentsModel;

  /**
   * The packages insurance category jpa repository.
   */
  @LazyAutowired
  private PackagesInsuranceCategoryService packagesInsuranceCategoryService;

  /**
   * The item category service.
   */
  @LazyAutowired
  private InsuranceItemCategoryService itemCategoryService;

  /**
   * The session service.
   */
  @LazyAutowired
  private SessionService sessionService;

  /**
   * The code type service.
   */
  @LazyAutowired
  private CodeTypeService codeTypeService;

  /**
   * The package charges service.
   */
  @LazyAutowired
  private PackageChargesService packageChargesService;

  /**
   * The center package applicability service.
   */
  @LazyAutowired
  private CenterPackageApplicabilityService centerPackageApplicabilityService;

  /** The department package applicability service. */
  @LazyAutowired
  private DepartmentPackageApplicabilityService departmentPackageApplicabilityService;

  /**
   * The organization service.
   */
  @LazyAutowired
  private OrganizationService organizationService;

  /**
   * The package org details service.
   */
  @LazyAutowired
  private PackageOrgDetailsService packageOrgDetailsService;

  /** The package category master service. */
  @LazyAutowired
  private PackageCategoryMasterService packageCategoryMasterService;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The package content charges service. */
  @LazyAutowired
  private PackageContentChargesService packageContentChargesService;

  /** The package sponsor service. */
  @LazyAutowired
  private PackageSponsorService packageSponsorService;

  /** The package item sub group tax service. */
  @LazyAutowired
  private PackageItemSubGroupTaxService packageItemSubGroupTaxService;

  /** The order service. */
  @LazyAutowired
  private OrderService orderService;

  /** The common charges repository. */
  @LazyAutowired
  private CommonChargesRepository commonChargesRepository;

  /** The package plan service. */
  @LazyAutowired
  private PackagePlanService packagePlanService;

  /** The stock service. */
  @LazyAutowired
  private StockService stockService;

  /** The job service. */
  @LazyAutowired
  private JobService jobService;

  @LazyAutowired
  private MasterChargesCronSchedulerDetailsRepository masterChargesCronSchedulerDetailsRepository;

  /** The job service. */
  @LazyAutowired
  private ChargeHeadsService chargeHeadsService;

  /** The chargeHead Ids for which the charge head name must be appended to,
   * in the activity description. */
  private static List<String> ACTIVITY_DESCRIPTION_CHARGE_HEADS = Arrays.asList("BBED", "BICU",
          "NCBED", "NCICU", "SUOPE", "TCOPE", "ANAOPE", "SACOPE", "DDBED", "DDICU", "PCBED",
          "PCICU","ANATOPE");

  /**
   * Instantiates a new package service.
   *
   * @param repo the repo
   * @param validator the validator
   * @param d1 the d 1
   * @param d2 the d 2
   * @param d3 the d 3
   * @param d4 the d 4
   */
  public PackageService(PackagesRepository repo, OrderSetsValidator validator,
      PackageContentsRepository d1, CenterPackageApplicabilityRepository d2,
      DeptPackageApplicabilityRepository d3, TpaPackageApplicabilityRepository d4) {
    super(repo, validator, "P", d1, d2, d3, d4);

  }

  /**
   * Gets the tax group details.
   *
   * @return the tax group details
   */
  public List<Map<String, Object>> getTaxGroupDetails() {
    List<BasicDynaBean> itemGroups = taxGroupService.getAllItemGroup();
    List<Map<String, Object>> taxGroupDetails = new ArrayList<>();
    for (BasicDynaBean item : itemGroups) {
      Map<String, Object> taxGroups = new HashMap<>();
      taxGroups.put("item_group_id", (Integer) item.get("item_group_id"));
      taxGroups.put("item_group_name", (String) item.get("item_group_name"));
      taxGroupDetails.add(taxGroups);
    }
    return taxGroupDetails;
  }


  /**
   * Gets the item group ids from tax group details.
   *
   * @param taxGroupDetails the tax group details
   * @return the item group ids from tax group details
   */
  public List<Integer> getItemGroupIdsFromTaxGroupDetails(
      List<Map<String, Object>> taxGroupDetails) {
    List<Integer> itemGroupIds = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(taxGroupDetails)) {
      for (Map<String, Object> taxGroupDetail : taxGroupDetails) {
        itemGroupIds.add((Integer) taxGroupDetail.get("item_group_id"));
      }
    }
    return itemGroupIds;
  }

  /**
   * Gets the tax sub group details.
   *
   * @param taxGroupDetails the item group ids
   * @return the tax sub group details
   */
  public List getTaxSubGroupDetails(List<Map<String, Object>> taxGroupDetails) {
    List<Integer> itemGroupIds = getItemGroupIdsFromTaxGroupDetails(taxGroupDetails);
    List taxSubGroupDetails = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(itemGroupIds)) {
      for (Integer id : itemGroupIds) {
        taxSubGroupDetails.addAll(
            ConversionUtils.listBeanToListMap(taxSubGroupService.getValidItemSubGroup(id)));
      }
    }
    return taxSubGroupDetails;
  }

  /**
   * Gets the billing groups.
   *
   * @return the billing groups
   */
  public List getBillingGroups() {
    Map<String, Object> params = new HashMap<>();
    params.put("item_group_type_id", "BILLGRP");
    List<BasicDynaBean> result = this.itemGroupsService.lookup(true, params);
    return ConversionUtils.listBeanToListMap(result);
  }

  /**
   * Gets the item insurance category.
   *
   * @return the item insurance category
   */
  public List getItemInsuranceCategory() {
    List<BasicDynaBean> itemCategoryList = itemCategoryService.getItemInsuranceCategory();
    return ConversionUtils.listBeanToListMap(itemCategoryList);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.mdm.ordersets.GenericPackagesService#metaData()
   */
  @Override
  public Map<String, Object> metaData() throws Exception {
    Map<String, Object> info = super.metaData();
    List<Map<String, Object>> taxGroupDetails = getTaxGroupDetails();
    info.put("tax_group_details", taxGroupDetails);
    info.put("tax_sub_group_details", getTaxSubGroupDetails(taxGroupDetails));
    info.put("bed_type_list",
        ConversionUtils.listBeanToListMap(bedTypeService.getAllSortedBedTypes()));
    info.put("billing_groups", getBillingGroups());
    info.put("item_insurance_categories", getItemInsuranceCategory());
    info.put("code_type_list", ConversionUtils
        .listBeanToListMap(codeTypeService.getCodeTypesByCodeCategory("Treatment")));
    info.put("hospital_centers_list",
        ConversionUtils.listBeanToListMap(centerService.getActiveCentersList()));
    info.put("package_categories", this.packageCategoryMasterService.findActiveCategories());
    info.put("rate_sheets",
        ConversionUtils.listBeanToListMap(this.organizationService.getRateSheetAndPlanList()));
    return info;
  }

  /**
   * Save.
   *
   * @param packagesDTO the packages DTO
   */
  @Transactional(value = "jpaTransactionManager", rollbackFor = Exception.class)
  public void save(PackagesDTO packagesDTO) {
    validateSaveOrUpdatePackageRequest(packagesDTO);
    String userId = (String) sessionService.getSessionAttributes().get("userId");
    packagesDTO.getPackagesModel().setCreatedBy(userId);
    packagesDTO.getPackagesModel().setCreatedAt(DateUtil.getCurrentTimestamp());
    packagesDTO.getPackagesModel().setType('P');
    PackagesModel savedPackage =
        this.packageJpaRepository.saveAndFlush(packagesDTO.getPackagesModel());
    int packageId = savedPackage.getPackageId();
    savePackageInsuranceCategoryMapping(packagesDTO.getInsuranceCategoryIds(), packageId);
    packageOrgDetailsService.saveOrUpdatePackageOrgDetails(packageId,
        packagesDTO.getRatePlanApplicability(), new ArrayList<String>());
    // saveDefaultSponsorMapping(packageId);
    packageSponsorService.saveOrUpdateSponsorMapping(packageId,
        packagesDTO.getSponsorPackageApplicability());
    packagePlanService.saveOrUpdatePlanMapping(packageId,
        packagesDTO.getInsurancePlanApplicability());
    // saveDefaultPlanMapping(packageId);
    departmentPackageApplicabilityService.saveOrUpdatePackageDepartmentApplicability(packageId,
        packagesDTO.getDepartmentPackageApplicability());
    packageItemSubGroupTaxService.saveOrUpdatePackageTaxApplicability(packageId,
        packagesDTO.getPackageTaxApplicability());
    centerPackageApplicabilityService.saveOrUpdateCenterApplicability(packageId,
        packagesDTO.getCenterPackageApplicability());
    Collections.sort(packagesDTO.getPackageContentsModels(),
        new PackageContentsModel().new CompareByRef());
    int ref = 0;
    int firstIndex = 0;
    int count = -1;
    Map<String, String> charges = null;
    for (PackageContentsModel packageContent : packagesDTO.getPackageContentsModels()) {
      count++;
      int packageContentId = packageContentService.getNextSequence();
      packageContent.setPackageContentId(packageContentId);
      if (packageContent.getContentIdRef() != null) {
        if (ref == 0) {
          ref = packageContent.getContentIdRef();
          firstIndex = count;
          packageContent.setContentIdRef(null);
        } else if (ref == packageContent.getContentIdRef()) {
          packageContent.setContentIdRef(
              (packagesDTO.getPackageContentsModels().get(firstIndex).getPackageContentId()
                  + 1));
        } else {
          ref = packageContent.getContentIdRef();
          firstIndex = count;
          packageContent.setContentIdRef(null);
        }
      }

      packageContent.setPackageId(packageId);
      packageContent.setCreatedBy(userId);
      packageContent.setCreatedAt(DateUtil.getCurrentTimestamp());
      if (packageContent.getActivityType().equals("Other Charge")) {
        if (charges == null) {
          charges = commonChargesRepository.getCommonChargeTypeMap();
        }
        packageContent.setChargeHead(charges.get(packageContent.getActivityId()));
      }
      this.packageContentService.save(packageContent, true);
    }
    scheduleSaveCharges(packageId, packagesDTO.getPackageContentsModels(),
        null, packagesDTO.getRatePlanApplicability(), ACTION_NEW_PACKAGE);
  }

  /**
   * Validate save package request.
   *
   * @param packagesDTO the packages DTO
   */
  private void validateSaveOrUpdatePackageRequest(PackagesDTO packagesDTO) {
    ValidationException validationException = null;

    if (CollectionUtils.isEmpty(packagesDTO.getPackageContentsModels())) {
      validationException =
          new ValidationException("package.validation.error.package.content.empty");
    } else if (packagesDTO.getPackagesModel().getMinAge() != null
        && packagesDTO.getPackagesModel().getMinAge() < 0) {
      validationException = new ValidationException("package.validation.error.min.age");
    } else if (new Character('Y').equals(packagesDTO.getPackagesModel().getAgeUnit())
        && packagesDTO.getPackagesModel().getMaxAge() != null
        && packagesDTO.getPackagesModel().getMaxAge() > 120) {
      validationException = new ValidationException("package.validation.error.max.age");
    } else if (packagesDTO.getPackagesModel().getValidFrom() != null
        && packagesDTO.getPackagesModel().getValidTill() != null
        && packagesDTO.getPackagesModel().getValidFrom()
            .after(packagesDTO.getPackagesModel().getValidTill())) {
      validationException = new ValidationException("package.validation.error.validity");
    }

    PackagesModel existingPackage = this.packageJpaRepository
        .findByPackageName(packagesDTO.getPackagesModel().getPackageName());

    if ((packagesDTO.getPackagesModel().getPackageId() == 0 && existingPackage != null)
        || (existingPackage != null && existingPackage.getPackageId()
        != packagesDTO.getPackagesModel().getPackageId())) {
      validationException = new ValidationException("package.validation.error.duplicate.name");
    }

    if (validationException != null) {
      throw validationException;
    }
  }

  /**
   * Save package insurance category mapping.
   *
   * @param insuranceCategoryIds the insurance category ids
   * @param packageId the package id
   * @return the list
   */
  private List<PackagesInsuranceCategoryMapping> savePackageInsuranceCategoryMapping(
      List<Integer> insuranceCategoryIds, int packageId) {

    List<PackagesInsuranceCategoryMapping> mappingList = new ArrayList<>();
    for (Integer ids : insuranceCategoryIds) {
      PackagesInsuranceCategoryMapping packagesInsuranceCategoryMapping =
          new PackagesInsuranceCategoryMapping();
      packagesInsuranceCategoryMapping.setInsuranceCategoryId(ids);
      packagesInsuranceCategoryMapping.setPackageId(packageId);
      mappingList.add(packagesInsuranceCategoryService.save(packagesInsuranceCategoryMapping));
    }
    return mappingList;
  }

  /**
   * Update package insurance category mapping.
   *
   * @param insuranceCategoryIds the insurance category ids
   * @param packageId the package id
   * @return the list
   */
  private List<PackagesInsuranceCategoryMapping> updatePackageInsuranceCategoryMapping(
      List<Integer> insuranceCategoryIds, Integer packageId) {
    List<PackagesInsuranceCategoryMapping> mappingList =
        packagesInsuranceCategoryService.findAllByPackageId(packageId);

    packagesInsuranceCategoryService.delete(mappingList);
    return savePackageInsuranceCategoryMapping(insuranceCategoryIds, packageId);
  }

  /**
   * Update package contents.
   *
   * @param newPackageContents the package contents models
   * @param packageId          the package id
   * @param orgIdList          orgId List
   */
  private void updatePackageContents(List<PackageContentsModel> newPackageContents,
      Integer packageId, List<String> orgIdList) {

    List<PackageContentsModel> existingPackageContents =
        this.packageContentService.findAllByPackageId(packageId);

    List<PackageContentsModel> deletedPackageContents =
        deleteStalePackageContents(newPackageContents, existingPackageContents);
    List<PackageContentsModel> toBeUpdatedPackageContents =
        new ArrayList<>(existingPackageContents);
    toBeUpdatedPackageContents.removeAll(deletedPackageContents);
    Timestamp currentTimeStamp = new java.sql.Timestamp(new Date().getTime());
    String userId = (String) sessionService.getSessionAttributes().get("userId");
    for (PackageContentsModel toBeUpdatedPackageContent : toBeUpdatedPackageContents) {
      int existingIndex = newPackageContents.indexOf(toBeUpdatedPackageContent);
      toBeUpdatedPackageContent
          .setActivityQty(newPackageContents.get(existingIndex).getActivityQty());
      toBeUpdatedPackageContent
          .setDisplayOrder(newPackageContents.get(existingIndex).getDisplayOrder());
      toBeUpdatedPackageContent.setModifiedBy(userId);
      toBeUpdatedPackageContent.setModifiedAt(currentTimeStamp);
    }

    this.packageContentService.saveAll(toBeUpdatedPackageContents, false);

    // Save charges for newly added package contents
    List<PackageContentsModel> toBeInsertedPackageContents =
        new ArrayList<>(newPackageContents);
    toBeInsertedPackageContents.removeAll(existingPackageContents);
    int ref = 0;
    int firstIndex = 0;
    int count = -1;
    Map<String, String> charges = null;
    for (PackageContentsModel packageContent : toBeInsertedPackageContents) {
      count++;
      if (packageContent.getPackageContentId() == null) {
        int packageContentId = packageContentService.getNextSequence();
        packageContent.setPackageContentId(packageContentId);
        if (packageContent.getContentIdRef() != null) {
          if (ref == 0) {
            ref = packageContent.getContentIdRef();
            firstIndex = count;
            packageContent.setContentIdRef(null);
          } else if (ref == packageContent.getContentIdRef()) {
            packageContent
                .setContentIdRef((toBeInsertedPackageContents
                    .get(firstIndex).getPackageContentId() + 1));
          } else {
            ref = packageContent.getContentIdRef();
            firstIndex = count;
            packageContent.setContentIdRef(null);
          }
        }
        packageContent.setPackageId(packageId);
        packageContent
            .setCreatedBy((String) sessionService.getSessionAttributes().get("userId"));
        packageContent.setCreatedAt(DateUtil.getCurrentTimestamp());
        if (packageContent.getActivityType().equals("Other Charge")) {
          if (charges == null) {
            charges = commonChargesRepository.getCommonChargeTypeMap();
          }
          packageContent.setChargeHead(charges.get(packageContent.getActivityId()));
        }
        PackageContentsModel savedContent =
            this.packageContentService.save(packageContent, true);
      }
    }
    if (!toBeInsertedPackageContents.isEmpty() || !deletedPackageContents.isEmpty()) {
      scheduleSaveCharges(packageId, toBeInsertedPackageContents, deletedPackageContents,
          orgIdList, ACTION_UPDATE_PACKAGE_CONTENTS);
    }
  }

  /**
   * Get the list of deleted package contents.
   *
   * @param newPackageContents      the new package contents
   * @param existingPackageContents the existing package contents
   * @return the list
   */
  private List<PackageContentsModel> deleteStalePackageContents(
      final List<PackageContentsModel> newPackageContents,
      final List<PackageContentsModel> existingPackageContents) {
    List<PackageContentsModel> toDeleteContents = new ArrayList<>(existingPackageContents);
    toDeleteContents.removeAll(newPackageContents);
    return toDeleteContents;
  }

  /**
   * Delete stale package contents.
   *
   * @param toDeleteContents list of PackageContentsModel
   */
  @Transactional(value = "jpaTransactionManager", rollbackFor = Exception.class)
  public void deletePackageContentsAndCharges(List<PackageContentsModel> toDeleteContents) {
    List<Integer> deletePackageContentIds = new ArrayList<>();
    for (PackageContentsModel toDeleteContent : toDeleteContents) {
      deletePackageContentIds.add(toDeleteContent.getPackageContentId());
    }
    packageContentChargesService
        .deletePackageContentChargeByPackageContents(deletePackageContentIds);
    packageContentService.deletePackageContentByPackageContentId(deletePackageContentIds);
  }

  /**
   * Update.
   *
   * @param packagesDTO the packages DTO
   */
  @Transactional(value = "jpaTransactionManager", rollbackFor = Exception.class)
  public void update(PackagesDTO packagesDTO) {
    String userId = (String) sessionService.getSessionAttributes().get("userId");
    int packageId = packagesDTO.getPackagesModel().getPackageId();
    if (packageJpaRepository.exists(packageId)) {
      validateSaveOrUpdatePackageRequest(packagesDTO);
      packagesDTO.getPackagesModel().setModifiedBy(userId);
      packagesDTO.getPackagesModel().setModifiedAt(DateUtil.getCurrentTimestamp());
      this.packageJpaRepository.save(packagesDTO.getPackagesModel());
      updatePackageContents(packagesDTO.getPackageContentsModels(), packageId,
          packagesDTO.getRatePlanApplicability());
      updatePackageInsuranceCategoryMapping(packagesDTO.getInsuranceCategoryIds(), packageId);
      centerPackageApplicabilityService.saveOrUpdateCenterApplicability(packageId,
          packagesDTO.getCenterPackageApplicability());
      departmentPackageApplicabilityService.saveOrUpdatePackageDepartmentApplicability(
          packageId, packagesDTO.getDepartmentPackageApplicability());
      packageItemSubGroupTaxService.saveOrUpdatePackageTaxApplicability(packageId,
          packagesDTO.getPackageTaxApplicability());
      packageSponsorService.saveOrUpdateSponsorMapping(packageId,
          packagesDTO.getSponsorPackageApplicability());
      packagePlanService.saveOrUpdatePlanMapping(packageId,
          packagesDTO.getInsurancePlanApplicability());
      List<String> applicableOrgIds = new ArrayList<>(packagesDTO.getRatePlanApplicability());
      List<String> existingOrgIds = packageOrgDetailsService.findOrgIdsByPackageId(packageId);
      applicableOrgIds.removeAll(existingOrgIds);
      if (applicableOrgIds.size() != 0) {
        scheduleSaveCharges(packageId, packagesDTO.getPackageContentsModels(),
                 null, applicableOrgIds, ACTION_UPDATE_PACKAGE);
      }
      existingOrgIds.removeAll(packagesDTO.getRatePlanApplicability());
      packageOrgDetailsService.saveOrUpdatePackageOrgDetails(packageId,
          applicableOrgIds, existingOrgIds);
      if (existingOrgIds.size() != 0) {
        this.packageChargesService.removeByPackageIdAndOrgIdIn(packageId, existingOrgIds);
        this.packageContentChargesService.removePackageContentChargesByPackageIdAndOrgIdIn(
            packageId, existingOrgIds);
      }
    }
  }

  /**
   * Find by id.
   *
   * @param packageId the package id
   * @return the packages DTO
   */
  public PackagesDTO findById(int packageId) {
    PackagesDTO packagesDTO = new PackagesDTO();
    PackagesModel packages = packageJpaRepository.findOne(packageId);
    if (packages != null) {
      packagesDTO.setPackagesModel(packages);
      List<PackageContentsModel> packageContents =
          packageContentService.findAllByPackageId(packageId);
      packageContents = setActivityDescription(packageContents);
      packagesDTO.setPackageContentsModels(packageContents);

      List<PackagesInsuranceCategoryMapping> itemInsuranceCategories =
          packagesInsuranceCategoryService.findAllByPackageId(packageId);

      List<Integer> itemCategories = new ArrayList<>();

      for (PackagesInsuranceCategoryMapping mapping : itemInsuranceCategories) {
        itemCategories.add(mapping.getInsuranceCategoryId());
      }
      packagesDTO.setInsuranceCategoryIds(itemCategories);
      packagesDTO.setPackageTaxApplicability(
          packageItemSubGroupTaxService.findAllVOByPackageId(packageId));

      packagesDTO.setCenterPackageApplicability(
          centerPackageApplicabilityService.findAllVOByPackageId(packageId));

      packagesDTO.setDepartmentPackageApplicability(
          departmentPackageApplicabilityService.findAllVOByPackageId(packageId));

      packagesDTO.setSponsorPackageApplicability(
          packageSponsorService.getSponsorPackageApplicabilityVO(packageId));

      packagesDTO.setInsurancePlanApplicability(
          packagePlanService.getPackagePlanApplicabilityVO(packageId));
      packagesDTO.setRatePlanApplicability(
          packageOrgDetailsService.findOrgIdsByPackageId(packageId));
    }

    return packagesDTO;
  }


  /**
   * Find contents of a package.
   *
   * @param packageId the package id
   * @return the packages DTO with packageContentsModels containing package contents
   */
  public PackagesDTO getPackageContents(int packageId) {
    PackagesDTO packagesDTO = new PackagesDTO();
    List<PackageContentsModel> packageContents =
        packageContentService.findAllByPackageId(packageId);
    setActivityDescription(packageContents);
    packagesDTO.setPackageContentsModels(packageContents);
    return packagesDTO;
  }

  /**
   * Find ChargeHead by item type.
   *
   * @param itemType itemType
   * @param id id
   * @param consultationTypeId consultationTypeId
   * @return charge head
   * @throws Exception exception
   */
  public String getChargeHeadByItemType(String itemType, String id, String consultationTypeId)
      throws Exception {
    String chargeHead;

    switch (itemType) {
      case "Service":
        chargeHead = ChargeDTO.CH_SERVICE;
        break;
      case "Equipment":
        chargeHead = ChargeDTO.CH_EQUIPMENT;
        break;
      case "Laboratory":
        chargeHead = ChargeDTO.CH_DIAG_LAB;
        break;
      case "Radiology":
        chargeHead = ChargeDTO.CH_DIAG_RAD;
        break;
      case "Meal":
        chargeHead = ChargeDTO.CH_DIETARY;
        break;
      case "Other Charges":
        BasicDynaBean otherService = new CommonChargesDAO().getCommonCharge(id);
        chargeHead = (String) otherService.get("charge_type");
        break;
      case "Direct Charges":
        chargeHead = id;
        break;
      case "Doctor":
        BasicDynaBean consultationType = new GenericDAO("consultation_types")
            .findByKey("consultation_type_id", Integer.parseInt(consultationTypeId));

        chargeHead = (String) consultationType.get("charge_head");
        break;
      case "Inventory":
        chargeHead = ChargeDTO.CH_INVENTORY_ITEM;
        break;
      default:
        chargeHead = StringUtils.EMPTY;
    }

    return chargeHead;
  }

  /**
   * Gets the active packages for bills.
   *
   * @param billNos the bill nos
   * @return the active packages for bills
   */
  public List<BasicDynaBean> getActivePackagesForBills(List<String> billNos) {
    return ((PackagesRepository) getRepository()).getActivePackagesForBills(billNos);
  }

  /**
   * Gets the package inventory details.
   *
   * @param patPkgId the patient package id
   * @param orgId the org id
   * @param bedType the bed type
   * @return the package inventory details
   */
  public List<BasicDynaBean> getPkgInvDetails(int patPkgId, String orgId, String bedType) {
    return ((PackagesRepository) getRepository()).getPkgInvDetails(patPkgId, orgId, bedType);
  }

  /**
   * Gets the pkg items.
   *
   * @param medicineIds the medicine ids
   * @return the pkg items
   */
  public List<BasicDynaBean> getPkgItems(List<Integer> medicineIds) {
    return ((PackagesRepository) getRepository()).getPkgItems(medicineIds);
  }

  /**
   * Gets the package item sub group tax details.
   *
   * @param packageId the package id
   * @return the package item sub group tax details
   */
  public List<BasicDynaBean> getPackageItemSubGroupTaxDetails(Integer packageId) {
    return ((PackagesRepository) getRepository()).getPackageItemSubGroupTaxDetails(packageId);
  }

  /**
   * Gets the multi pkg details.
   *
   * @param patPkgId the patient package id
   * @return the multi pkg details
   */
  public List<BasicDynaBean> getMultiPkgDetails(int patPkgId) {
    return ((PackagesRepository) getRepository()).getMultiPkgDetails(patPkgId);
  }

  /**
   * Gets the package by id.
   *
   * @param packageId the package id
   * @return the package by id
   */
  public BasicDynaBean getPackageById(int packageId) {
    return ((PackagesRepository) getRepository()).findByKey("package_id", packageId);
  }

  /**
   * Save default sponsor mapping.
   *
   * @param packageId the package id
   */
  private void saveDefaultSponsorMapping(Integer packageId) {
    PackageSponsorMasterModel packageSponsorMasterModel = new PackageSponsorMasterModel();
    packageSponsorMasterModel.setTpaId("-1");
    packageSponsorMasterModel.setPackId(packageId);
    packageSponsorMasterModel.setStatus('A');
    this.packageSponsorService.save(packageSponsorMasterModel, true);
  }

  // /**
  // * Save default plan mapping.
  // *
  // * @param packageId the package id
  // */
  // private void saveDefaultPlanMapping(Integer packageId) {
  // PackagePlanMasterModel packagePlanMasterModel = new PackagePlanMasterModel();
  // packagePlanMasterModel.setPackId(packageId);
  // packagePlanMasterModel.setPlanId(-1);
  // packagePlanMasterModel.setStatus('A');
  // this.packagePlanService.save(packagePlanMasterModel);
  // }

  /**
   * Sets the activity description.
   *
   * @param packageContentsModels the package contents models
   * @return the list
   */
  private List<PackageContentsModel> setActivityDescription(
      List<PackageContentsModel> packageContentsModels) {
    Set<String> chargeHeadIds = new HashSet<>();
    Set<String> orderableIds = new HashSet<>();
    Set<Integer> inventoryIds = new HashSet<>();
    for (PackageContentsModel packageContent : packageContentsModels) {
      chargeHeadIds.add(packageContent.getChargeHead());
      if (packageContent.getActivityType().equals("Inventory")) {
        inventoryIds.add(Integer.valueOf(packageContent.getActivityId()));
      } else {
        orderableIds.add(packageContent.getActivityId());
      }
    }
    List<String> activityIds = new ArrayList<>(orderableIds);
    List<Integer> inventoryIdList = new ArrayList<>(inventoryIds);
    List<String> chargeHeadIdList = new ArrayList<>(chargeHeadIds);
    Map<String, String> nameMap = this.orderService.getItemNamesByEntityIds(activityIds);
    Map<String, String> chargeHeadNamesMap = this.chargeHeadsService
            .getChargeHeadNames(chargeHeadIdList);
    nameMap.putAll(this.stockService.getMedicineNamesByMedicineIds(inventoryIdList));
    for (PackageContentsModel packageContent : packageContentsModels) {
      String name = nameMap.get(packageContent.getActivityId());
      String chargeHeadId = packageContent.getChargeHead();
      String chargeHeadName = chargeHeadNamesMap.get(chargeHeadId);
      if (StringUtils.isBlank(name)) {
        continue;
      }

      StringBuilder activityName = new StringBuilder();
      if (StringUtils.isNotBlank(chargeHeadName)
              && ACTIVITY_DESCRIPTION_CHARGE_HEADS.contains(chargeHeadId)) {
        activityName.append(chargeHeadName).append(": ");
      }
      activityName.append(name);
      if (null != packageContent.getPanelId()) {
        BasicDynaBean panelpacBean = getPackageById(packageContent.getPanelId());
        activityName.append(": " + panelpacBean.get("package_name"));
      }
      packageContent.setActivityDescription(activityName.toString());
    }
    return packageContentsModels;
  }

  /**
   * Schedule save charges.
   *
   * @param packageContentList     packageContent List
   * @param orgIdList              org Id lise
   * @param deletedPackageContents deleted package contents
   * @param packageId              the package id
   */
  private void scheduleSaveCharges(int packageId, List<PackageContentsModel> packageContentList,
      List<PackageContentsModel> deletedPackageContents, List<String> orgIdList, String action) {
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("packageId", packageId);
    jobData.put("schema", RequestContext.getSchema());
    jobData.put("orgIds", orgIdList);
    jobData.put("bedNames", null);
    jobData.put("center_id", RequestContext.getCenterId());
    jobData.put("action", action);
    jobData.put("packageContentList", packageContentList);
    jobData.put("deletedPackageContents", deletedPackageContents);
    BasicDynaBean masterJobScheduler = masterChargesCronSchedulerDetailsRepository.getBean();
    masterJobScheduler.set("status", "P");
    masterJobScheduler.set("entity", action);
    masterJobScheduler.set("entity_id", packageId + "-" + action);
    masterJobScheduler.set("charge", BigDecimal.ZERO);
    masterJobScheduler.set("discount", BigDecimal.ZERO);
    masterChargesCronSchedulerDetailsRepository.insert(masterJobScheduler);
    jobData.put("masterJobBean", masterJobScheduler);
    jobService.scheduleImmediate(buildJob("savePackageCharge-"
        + packageId, SavePackageChargesJob.class, jobData));
  }

  /**
   * Gets Package Names map.
   *
   * @param packageIds the list of package ids
   * @return the packages names map
   */
  public Map<Integer, String> getPackageNamesMap(List<Integer> packageIds) {
    if (packageIds.size() == 0) {
      return Collections.emptyMap();
    }
    List<PackagesModel> packagesModelList = this.packageJpaRepository
        .findByPackageIdIn(packageIds);
    Map<Integer, String> packageNames = new HashMap<>();
    for (PackagesModel packagesModel: packagesModelList) {
      packageNames.put(packagesModel.getPackageId(), packagesModel.getPackageName());
    }
    return packageNames;
  }

  public Boolean findByPackageCode(String packageCode) {
    return !this.packageJpaRepository
        .findByPackageCodeEquals(packageCode).isEmpty();
  }
}
