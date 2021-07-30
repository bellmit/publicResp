package com.insta.hms.mdm.ordersets;

import com.bob.hms.common.DateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.adminmaster.packagemaster.PackageChargeDAO;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.order.master.OrderService;
import com.insta.hms.core.inventory.stockmgmt.StockService;
import com.insta.hms.jobs.MasterChargesCronSchedulerDetailsRepository;
import com.insta.hms.mdm.bedtypes.BedTypeService;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;
import com.insta.hms.mdm.organization.OrganizationService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class PackageChargesService.
 *
 * @author manika.singh
 * @since 15/04/19
 */
@Service
public class PackageChargesService {

  /** The repository. */
  @LazyAutowired
  private PackageChargesJpaRepository repository;

  /** The package content charges service. */
  @LazyAutowired
  private PackageContentChargesService packageContentChargesService;

  /** The package content charges service. */
  @LazyAutowired
  private PackageService packageService;

  /** The organization service. */
  @LazyAutowired
  private OrganizationService organizationService;

  /** The bed type service. */
  @LazyAutowired
  private BedTypeService bedTypeService;

  /** The package charges repository. */
  @LazyAutowired
  private PackageChargesRepository pkgChargesRepo;
  
  /** The package charges repository. */
  @LazyAutowired
  private PackageContentChargesJpaRepository pkgContChargesJpaRepository;

  @LazyAutowired
  private OrderService orderService;

  @LazyAutowired
  private PackageContentService packageContentService;

  @LazyAutowired
  private ObjectMapper objectMapper;

  @LazyAutowired
  private PackageChargeDAO packageChargeDAO;

  @LazyAutowired
  private PackageOrgDetailsService packageOrgDetailsService;

  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private StockService stockService;

  @LazyAutowired
  private MasterChargesCronSchedulerDetailsRepository masterChargesCronSchedulerDetailsRepository;

  @LazyAutowired
  MessageUtil messageUtil;

  @LazyAutowired
  private ChargeHeadsService chargeHeadsService;

  /** The chargeHead Ids for which the charge head name must be appended to,
   * in the activity description. */
  private static List<String> ACTIVITY_DESCRIPTION_CHARGE_HEADS = Arrays.asList("BBED", "BICU",
          "NCBED", "NCICU", "SUOPE", "TCOPE", "ANAOPE", "SACOPE", "DDBED", "DDICU", "PCBED",
          "PCICU");

  /**
   * Save.
   *
   * @param model the model
   * @return the package charges model
   */
  public PackageChargesModel save(PackageChargesModel model) {
    return this.repository.save(model);
  }

  /**
   * Save List of models.
   * @param packageChargesModels list of models
   * @return saved list of models
   */
  public List<PackageChargesModel> save(List<PackageChargesModel> packageChargesModels) {
    return this.repository.save(packageChargesModels);
  }

  /**
   * Returns validation object if job is running.
   * @param packageId The packageId
   * @return Returns validation object
   */
  public Map<String, String> getNullableValidations(int packageId) {
    List<String> entityIdList = new ArrayList<>();
    entityIdList.add(Integer.toString(packageId));
    entityIdList.add(Integer.toString(packageId) + "-updatePackageContents");
    List<String> entityList = new ArrayList<>();
    entityList.add(Integer.toString(packageId));
    entityList.add("NewPackage");
    entityList.add("updatePackageContents");
    entityList.add("EditPackageRate");
    Map<String, Object> chargesCronFilterMap = new HashMap<>();
    chargesCronFilterMap.put("entity_id", entityIdList);
    chargesCronFilterMap.put("entity", entityList);
    List<BasicDynaBean> chargeCronBeans = masterChargesCronSchedulerDetailsRepository
        .findByCriteria(chargesCronFilterMap);
    Map<String, String> chargeJobInProgressObject = null;
    for (BasicDynaBean chargeCronBean : chargeCronBeans) {
      if (((String)chargeCronBean.get("status")).equals("P")) {
        chargeJobInProgressObject = new HashMap<>();
        String message = messageUtil
            .getMessage("ui.message.package.charges.job.is.still.running", null);
        chargeJobInProgressObject.put("messageType", "warning");
        chargeJobInProgressObject.put("message", message);
      }
    }
    return chargeJobInProgressObject;
  }

  /**
   * Find PackageCharge by packageId and orgId.
   * @param packageId package identifier
   * @param orgId organization identifier
   * @return PackageChargesDTO
   */
  public PackageChargesDTO findByPackageIdAndOrgId(int packageId, String orgId) {

    PackageChargesDTO response = new PackageChargesDTO();
    BasicDynaBean orgDetails = this.organizationService.getOrgdetailsDynaBean(orgId);
    response.setOrgName((String) orgDetails.get("org_name"));
    response.setPackageId(packageId);
    response.setOrgId(orgId);
    response.setPackageCharges(getPackageCharges(packageId, orgId));
    response.setPackageContentCharges(getPackageContentCharges(packageId, orgId));
    response.setDerivedRatePlans(getDerivedDetails(orgId,
        packageId));
    PackOrgDetailsModel packOrgDetails =
        this.packageOrgDetailsService.findByPackOrgDetailsIdSequence(packageId, orgId);
    response.setCodeType(packOrgDetails.getCodeType());
    response.setRatePlanCode(packOrgDetails.getItemCode());
    response.setRatePlanApplicability(packageOrgDetailsService.findOrgIdsByPackageId(packageId));
    return response;
  }

  public List<DerivedRatePlansDTO> getDerivedDetails(String rateSheetId, int packageId) {
    List<Object[]> derivedRatePlans = repository.findDerivedDetails(rateSheetId, packageId);
    return convertDerivedPlan(derivedRatePlans);
  }

  private List<DerivedRatePlansDTO> convertDerivedPlan(List<Object[]> derivedRatePlans) {
    List<DerivedRatePlansDTO> result = new ArrayList<>();

    for (Object[] ratePlan : derivedRatePlans) {
      DerivedRatePlansDTO derivedRatePlansDTO = new DerivedRatePlansDTO();
      derivedRatePlansDTO.setOrgId((String) ratePlan[0]);
      derivedRatePlansDTO.setOrgName((String) ratePlan[1]);
      derivedRatePlansDTO.setDiscormarkup((String) ratePlan[2]);
      derivedRatePlansDTO.setRateVariationPercent(Math.abs((int)ratePlan[3]));
      derivedRatePlansDTO.setRoundOffAmount((Integer) ratePlan[4]);
      derivedRatePlansDTO.setApplicable((Boolean) ratePlan[5]);
      derivedRatePlansDTO.setPackageId((Integer) ratePlan[6]);
      derivedRatePlansDTO.setBaseRateSheetId((String) ratePlan[7]);
      derivedRatePlansDTO.setIsOverride((String) ratePlan[8]);
      result.add(derivedRatePlansDTO);
    }

    return result;
  }

  /**
   * Update PackageCharges.
   * @return updated package charge
   */
  @Transactional(value = "jpaTransactionManager", rollbackFor = Exception.class)
  public PackageChargesDTO update(PackageChargesDTO packageChargesDTO) {
    updatePackageChargesList(packageChargesDTO.getPackageCharges(),
            packageChargesDTO.getPackageId());
    updatePackOrgDetails(packageChargesDTO);
    updateDerivedRatePlanApplicable(packageChargesDTO.getDerivedRatePlans());
    this.packageContentChargesService
        .updatePackageContentCharge(packageChargesDTO.getPackageContentCharges(),
            packageChargesDTO.getPackageId(), packageChargesDTO.getOrgId());
    return packageChargesDTO;
  }

  private void updatePackageChargesList(List<PackageChargesVO> packageChargesVOS,
                                        Integer packageId) {
    for (PackageChargesVO packageChargesVO : packageChargesVOS) {
      PackageChargesModel packageCharges =
          this.repository.findOne(packageChargesVO.getPackageChargeId());
      if (packageCharges != null) {
        packageCharges.setDiscount(packageChargesVO.getDiscount());
        packageCharges.setCharge(packageChargesVO.getCharge());
        packageCharges.setIsOverride(packageChargesVO.getIsOverride());
        packageCharges.setModifiedAt(DateUtil.getCurrentTimestamp());
        packageCharges.setModifiedBy((String) sessionService.getSessionAttributes().get(
            "userId"));
        this.repository.save(packageCharges);
      } else {
        packageCharges = new PackageChargesModel();
        packageCharges.setPackageId(packageId);
        packageCharges.setBedType(packageChargesVO.getBedType());
        packageCharges.setOrgId(packageChargesVO.getOrgId());
        packageCharges.setDiscount(packageChargesVO.getDiscount());
        packageCharges.setCharge(packageChargesVO.getCharge());
        packageCharges.setIsOverride(packageChargesVO.getIsOverride());
        packageCharges.setModifiedAt(DateUtil.getCurrentTimestamp());
        packageCharges.setModifiedBy((String) sessionService.getSessionAttributes().get(
                "userId"));
        packageCharges.setCreatedAt(DateUtil.getCurrentTimestamp());
        packageCharges.setCreatedBy((String) sessionService.getSessionAttributes().get(
                "userId"));
        this.repository.save(packageCharges);
      }
    }
  }

  /**
   * Gets the package charges.
   *
   * @param charge    charge
   * @param orgId     the org id
   * @param packageId the package id
   * @param bedType   the bed type
   */
  public void savePackageCharges(BigDecimal charge, String orgId, int packageId, String bedType) {
    PackageChargesModel packageCharges =
        this.repository.findByPackageIdAndOrgIdAndBedType(packageId, orgId, bedType);
    BigDecimal discount = BigDecimal.ZERO;
    if (packageCharges != null && packageCharges.getCharge() != null) {
      charge = charge.add(packageCharges.getCharge());
    } else {
      packageCharges = new PackageChargesModel();
    }
    if (packageCharges != null && packageCharges.getDiscount() != null) {
      discount = discount.add(packageCharges.getDiscount());
    }
    packageCharges.setDiscount(discount);
    packageCharges.setCharge(charge);
    packageCharges.setIsOverride("N");
    packageCharges.setCreatedAt(DateUtil.getCurrentTimestamp());
    packageCharges.setCreatedBy("InstaAdmin");
    packageCharges.setBedType(bedType);
    packageCharges.setOrgId(orgId);
    packageCharges.setPackageId(packageId);

    this.repository.save(packageCharges);
  }

  /**
   * Gets the package charges.
   *
   * @param deletedPackageContents package content to be deleted.
   * @param packageId              the package id
   * @param orgIdList              the org id list
   */
  public void reCalculateAndSavePackageChargeAfterDelete(
      List<PackageContentsModel> deletedPackageContents,
      Integer packageId, List<String> orgIdList) {
    List<PackageChargesModel> packageChargesList = new ArrayList<PackageChargesModel>();
    for (String orgId : orgIdList) {
      for (PackageContentsModel deletedPackageContent : deletedPackageContents) {
        Integer packageContentId = deletedPackageContent.getPackageContentId();
        List<PackageContentCharges> packageContentList = pkgContChargesJpaRepository
            .findAllByPackageIdAndOrgIdSortedByBedType(packageId, orgId);
        for (PackageContentCharges packageContent : packageContentList) {
          if (!(packageContent.getPackageContentId() == deletedPackageContent.getPackageContentId()
              && packageContent.getOrgId().equals(orgId))) {
            continue;
          }
          PackageChargesModel packageCharges =
              this.repository.findByPackageIdAndOrgIdAndBedType(packageId,
                  orgId, packageContent.getBedType());
          BigDecimal charge = packageCharges.getCharge();
          if (!(packageContent.getCharge() == null)) {
            charge = charge.subtract(packageContent.getCharge());
          }
          packageCharges.setCharge(charge);
          packageCharges.setModifiedAt(DateUtil.getCurrentTimestamp());
          packageCharges.setModifiedBy("InstaAdmin");
          packageChargesList.add(packageCharges);
        }
      }
    }
    this.repository.save(packageChargesList);
  }

  private List<PackageChargesVO> getPackageCharges(int packageId, String orgId) {
    Sort.Order order = new Sort.Order(Sort.Direction.ASC, "bedType").ignoreCase();

    List<PackageChargesModel> packageChargesList =
        this.repository.findByPackageIdAndOrgId(packageId,
            orgId, new Sort(order));
    List<PackageChargesVO> packageChargesVOS = new ArrayList<>();
    for (PackageChargesModel packageCharges : packageChargesList) {
      PackageChargesVO packageChargesVO = this.objectMapper.convertValue(packageCharges,
          PackageChargesVO.class);
      if ("GENERAL".equals(packageChargesVO.getBedType())) {
        packageChargesVOS.add(0, packageChargesVO);
        continue;
      }
      packageChargesVOS.add(packageChargesVO);
    }
    return packageChargesVOS;
  }

  /**
   * Gets the package charges.
   *
   * @param packageId the package id
   * @param orgId the org id
   * @param bedType the bed type
   * @return the package charges
   */
  public BasicDynaBean getPackageCharges(Integer packageId, String orgId, String bedType) {
    Map<String, Object> filter = new HashMap<>();
    filter.put("package_id", packageId);
    filter.put("bed_type", bedType);
    filter.put("org_id", orgId);

    return pkgChargesRepo.findByKey(filter);
  }

  private List<PackageContentChargesVO> getPackageContentCharges(int packageId, String orgId) {

    List<PackageContentCharges> packageContentChargesList =
        this.packageContentChargesService.findAllByPackageIdAndOrgIdSortedByBedType(packageId,
            orgId);
    List<PackageContentChargesVO> packageContentChargesVOList = new ArrayList<>();
    Set<String> orderableItemActivityIds = new HashSet<>();
    Set<Integer> inventoryActivityIds = new HashSet<>();
    Set<Integer> panelPackageIds = new HashSet<>();
    Set<String> chargeHeadIds = new HashSet<>();
    for (PackageContentCharges packageContentCharges : packageContentChargesList) {
      PackageContentChargesVO packageContentChargesVO =
          this.objectMapper.convertValue(packageContentCharges, PackageContentChargesVO.class);
      PackageContentsModel packageContent =
          this.packageContentService.findById(packageContentChargesVO.getPackageContentId());
      packageContentChargesVO.setActivityId(packageContent.getActivityId());
      packageContentChargesVO.setPanelId(packageContent.getPanelId());
      packageContentChargesVO.setChargeHead(packageContent.getChargeHead());
      packageContentChargesVO.setContentQuantity(packageContent.getActivityQty());
      if (packageContent.getActivityType().equals("Inventory")) {
        inventoryActivityIds.add(Integer.valueOf(packageContent.getActivityId()));
      } else {
        orderableItemActivityIds.add(packageContent.getActivityId());
      }
      if (null != packageContent.getPanelId()) {
        panelPackageIds.add(packageContent.getPanelId());
      }
      packageContentChargesVOList.add(packageContentChargesVO);
      chargeHeadIds.add(packageContent.getChargeHead());
    }
    if (CollectionUtils.isNotEmpty(packageContentChargesVOList)) {
      packageContentChargesVOList = setActivityDescription(
          packageContentChargesVOList, orderableItemActivityIds, inventoryActivityIds,
          panelPackageIds, chargeHeadIds);
    }
    return packageContentChargesVOList;
  }

  /**
   * Update derived rate plan package charges.
   *
   * @param packageId  the package id
   * @param orgId      the org id
   * @param modifiedBy the modified by
   */
  @Transactional(value = "jpaTransactionManager", rollbackFor = Exception.class)
  public void updateDerivedRatePlanPackageCharges(Integer packageId, String orgId,
                                                  String modifiedBy) {

    List<DerivedRatePlansDTO> derivedDetails = this.getDerivedDetails(orgId, packageId);
    List<BasicDynaBean> bedTypeList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(derivedDetails)) {
      bedTypeList = this.bedTypeService.getAllBedTypes();
    }

    for (DerivedRatePlansDTO derivedDetail : derivedDetails) {

      if (!("Y").equals(derivedDetail.getIsOverride())
          && Boolean.TRUE.equals(derivedDetail.getApplicable())) {
        for (BasicDynaBean bedType : bedTypeList) {
          PackageChargesModel regularCharge =
              this.repository.findByPackageIdAndOrgIdAndBedType(packageId, orgId,
                  (String) bedType.get("bed_type"));
          PackageChargesModel derivedCharges =
              this.repository.findByPackageIdAndOrgIdAndBedType(packageId, derivedDetail.getOrgId(),
                  (String) bedType.get("bed_type"));

          if (regularCharge != null && derivedCharges != null) {
            Double charge = calculateCharge(regularCharge.getCharge().doubleValue(),
                derivedDetail.getRateVariationPercent(), derivedDetail.getRoundOffAmount(),
                derivedDetail.getDiscormarkup());
            Double discount = calculateCharge(regularCharge.getDiscount().doubleValue(),
                derivedDetail.getRateVariationPercent(), derivedDetail.getRoundOffAmount(),
                derivedDetail.getDiscormarkup());
            derivedCharges.setCharge(new BigDecimal(charge));
            derivedCharges.setDiscount(new BigDecimal(discount));
            derivedCharges.setModifiedAt(DateUtil.getCurrentTimestamp());
            derivedCharges.setModifiedBy(modifiedBy);
            this.repository.saveAndFlush(derivedCharges);
          }
        }
      }
    }
    this.packageContentChargesService
        .updateDerivedPackageContentCharges(packageId, orgId, modifiedBy);
  }

  @Transactional(transactionManager = "jpaTransactionManager", rollbackFor = Exception.class)
  public void saveDefaultPackageCharges(int packageId, List<String> orgIds, List<String> bedNames) {
    this.repository.saveDefaultPackageCharges(packageId, orgIds, bedNames);
  }

  /**
   * Calculate charge.
   * @param regularCharge package charge of base rate sheet
   * @param roundOffAmount derived rate plan roundoff amount
   * @param rateVariationPercentage derived rate plan rate variation percentage
   * @return derived rate plan charge
   */
  public Double calculateCharge(Double regularCharge,Integer rateVariationPercentage,
                                Integer roundOffAmount, String discOrMarkup) {
    Integer multiplicationFactor = "Decrease By".equals(discOrMarkup) ? -1 : 1;
    Double regCharge = regularCharge;
    Double charge = regCharge + (regCharge * rateVariationPercentage * multiplicationFactor)
        / 100;
    if (roundOffAmount != 0) {
      Double roundOff = new Double(roundOffAmount) / 2;

      roundOff = charge + roundOff;
      roundOff = roundOff / roundOffAmount;
      int value = roundOff.intValue();
      charge = roundOffAmount * new Double(value);

    }
    return charge;
  }

  private void updatePackOrgDetails(PackageChargesDTO packageChargesDTO) {
    PackOrgDetailsModel packOrgDetails =
        this.packageOrgDetailsService.findByPackOrgDetailsIdSequence(
            packageChargesDTO.getPackageId(), packageChargesDTO.getOrgId());
    packOrgDetails.setCodeType(packageChargesDTO.getCodeType());
    packOrgDetails.setItemCode(packageChargesDTO.getRatePlanCode());
    if (packageChargesDTO.getIsDerivedRatePlan() != null
        && packageChargesDTO.getIsDerivedRatePlan()) {
      packOrgDetails.setIsOverride("Y");
    }
    this.packageOrgDetailsService.save(packOrgDetails, true);
  }

  private void updateDerivedRatePlanApplicable(List<DerivedRatePlansDTO> derivedRatePlans) {
    for (DerivedRatePlansDTO ratePlan : derivedRatePlans) {
      PackOrgDetailsModel packOrgDetails =
          this.packageOrgDetailsService.findByPackOrgDetailsIdSequence(
              ratePlan.getPackageId(), ratePlan.getOrgId());
      packOrgDetails.setApplicable(ratePlan.getApplicable());
      this.packageOrgDetailsService.save(packOrgDetails, true);
    }
  }

  private List<PackageContentChargesVO> setActivityDescription(
      List<PackageContentChargesVO> packageContentChargesVOList,
      Set<String> orderableIds, Set<Integer> inventoryIds, Set<Integer> panelPackageIds,
      Set<String> chargeHeadIds) {
    List<String> orderableList = new ArrayList<>(orderableIds);
    List<Integer> inventoryList = new ArrayList<>(inventoryIds);
    Map<String, String> nameMap = this.orderService.getItemNamesByEntityIds(orderableList);
    nameMap.putAll(this.stockService.getMedicineNamesByMedicineIds(inventoryList));
    Map<Integer, String> panelPackageNamesMap = this.packageService
        .getPackageNamesMap(new ArrayList<Integer>(panelPackageIds));
    Map<String, String> chargeHeadNamesMap = this.chargeHeadsService
        .getChargeHeadNames(new ArrayList<String>(chargeHeadIds));
    for (PackageContentChargesVO packageContentChargesVO : packageContentChargesVOList) {
      String name = nameMap.get(packageContentChargesVO.getActivityId());
      String chargeHeadId = packageContentChargesVO.getChargeHead();
      String chargeHeadName = chargeHeadNamesMap.get(chargeHeadId);

      StringBuilder activityName = new StringBuilder();
      if (StringUtils.isNotBlank(chargeHeadName)
              && ACTIVITY_DESCRIPTION_CHARGE_HEADS.contains(chargeHeadId)) {
        activityName.append(chargeHeadName).append(": ");
      }
      activityName.append(name);
      if (null != packageContentChargesVO.getPanelId()) {
        BasicDynaBean panelpacBean = this.packageService
            .getPackageById(packageContentChargesVO.getPanelId());
        activityName.append(": " + panelpacBean.get("package_name"));
      }
      packageContentChargesVO.setPackageContentName(activityName.toString());
    }
    return packageContentChargesVOList;
  }

  public List<PackageChargesModel> removeByPackageIdAndOrgIdIn(Integer packageId,
                                                               List<String> orgIds) {
    return this.repository.removeByPackageIdAndOrgIdIn(packageId, orgIds);
  }
}
