package com.insta.hms.mdm.ordersets;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.google.common.collect.Iterables;
import com.insta.hms.common.BaseJPAService;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.order.master.OrderItemService;
import com.insta.hms.core.clinical.order.master.OrderService;
import com.insta.hms.jobs.JobService;
import com.insta.hms.jobs.MasterChargesCronSchedulerDetailsRepository;
import com.insta.hms.mdm.bedtypes.BedTypeService;


import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class PackageContentChargesService.
 *
 * @author manika.singh
 * @since 15/04/19
 */
@Service
public class PackageContentChargesService extends
    BaseJPAService<PackageContentChargesJpaRepository, PackageContentCharges, Integer> {

  /** The package content service. */
  @LazyAutowired
  private PackageContentService packageContentService;

  /** The bed type service. */
  @LazyAutowired
  private BedTypeService bedTypeService;

  /** The package charges service. */
  @LazyAutowired
  private PackageChargesService packageChargesService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private JobService jobService;

  @LazyAutowired
  private MasterChargesCronSchedulerDetailsRepository masterChargesCronSchedulerDetailsRepository;

  /**
   * The logger.
   */
  static Logger logger = LoggerFactory.getLogger(PackageContentChargesService.class);

  /**
   * The order service.
   */
  @LazyAutowired
  private OrderService orderService;

  /**
   * Instantiates a new base JPA service.
   *
   * @param repository the repository
   */
  @LazyAutowired
  public PackageContentChargesService(PackageContentChargesJpaRepository repository) {
    super(repository);
  }

  /**
   * Save list of package content charges.
   *
   * @param packageContentChargesList list of package content charges
   * @return saved package content charges
   */
  public List<PackageContentCharges> saveAll(
      List<PackageContentCharges> packageContentChargesList) {
    return this.repository.save(packageContentChargesList);
  }

  /**
   * Find by package content id and orgId.
   *
   * @param packageContentId packageContent identifier
   * @param orgId organization id
   * @param sort the sort
   * @return list of package content charges
   */
  public List<PackageContentCharges> findByPackageContentIdAndOrgId(int packageContentId,
      String orgId, Sort sort) {
    return this.repository.findByPackageContentIdAndOrgId(packageContentId, orgId, sort);

  }

  /**
   * Find by id.
   *
   * @param packageContentChargeId packageContentChargeId identifier
   * @return package content charge object reference
   */
  public PackageContentCharges findById(int packageContentChargeId) {
    return this.repository.findOne(packageContentChargeId);
  }

  /**
   * Save default package content charge.
   *
   * @param packageId package identifier
   * @param orgIds the org ids
   * @param bedNames the bed names
   */
  @Transactional(transactionManager = "jpaTransactionManager", rollbackFor = Exception.class)
  public void saveDefaultPackageContentCharges(Integer packageId, List<String> orgIds,
      List<String> bedNames) {
    this.repository.saveDefaultPackageContentChargeByPackageId(packageId, orgIds, bedNames);
  }

  /**
   * Save package content charge from respective masters.
   *
   * @param contentCharges content Charges
   * @param orgIds         the org ids
   */
  private void insertPackageContentCharges(List<Map<String, Object>> contentCharges,
      String orgId, int packageId) {
    List<String> bedNames = bedTypeService.getAllBedTypeNames();
    for (Map<String, Object> chargeMap : contentCharges) {
      for (String bedType : bedNames) {
        insertToPackaeContentCharges((Integer) chargeMap.get("package_content_id"), orgId, bedType,
            (BigDecimal) chargeMap.get(bedType));
      }
    }
  }

  /**
   * Save package charge by adding respective item masters charges.
   *
   * @param contentCharges content Charges
   * @param orgIds         the org ids
   * @param packageId      package identifier
   */
  private void saveMasterPackageCharges(List<Map<String, Object>> contentsCharges, String orgId,
      int packageId) {
    List<String> bedNames = bedTypeService.getAllBedTypeNames();
    for (String bedType : bedNames) {
      BigDecimal charge = BigDecimal.ZERO;
      for (Map<String, Object> contentCharge : contentsCharges) {
        charge = charge.add((BigDecimal) contentCharge.get(bedType));
      }
      //insert into the table
      this.packageChargesService.savePackageCharges(charge, orgId, packageId, bedType);
    }

  }

  /**
   * Save package content charge from respective masters.
   *
   * @param packageContentList PackageContentsModel list
   * @param orgIdList          the org ids list
   * @param packageId          package identifier
   * @param action             action to be perfomed
   */
  @Transactional(transactionManager = "jpaTransactionManager", rollbackFor = Exception.class)
  public void savePackageContentAndCharge(List<PackageContentsModel> packageContentList,
      List<String> orgIdList,
      int packageId, String action) {
    if (packageContentList.isEmpty()) {
      return;
    }
    Map<String, Object> objectMap = new HashMap<String, Object>();
    List<Map<String, Object>> contentCharges = new ArrayList<Map<String, Object>>();
    objectMap = orderService.getTypeObjectMap();
    OrderItemService chargeObject = null;
    for (String orgId : orgIdList) {
      contentCharges.clear();
      for (PackageContentsModel packageContent : packageContentList) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("package_id", packageContent.getPackageId());
        paramMap.put("quantity", packageContent.getActivityQty());
        paramMap.put("id", packageContent.getActivityId());
        if (PackageService.ACTION_UPDATE_PACKAGE.equals(action)) {
          paramMap.put("package_content_id", packageContent.getPackageContentId());
        } else {
          paramMap.put("package_content_id", packageContent.getPackageContentId() + 1);
        }
        paramMap.put("type", packageContent.getActivityType());
        paramMap.put("consultation_type_id", packageContent.getConsultationTypeId());
        paramMap.put("org_id", orgId);
        paramMap.put("charge_head", packageContent.getChargeHead());
        Map<String, Object> chargeMap = new HashMap<String, Object>();
        if (paramMap.get("type").toString().equals("Radiology")) {
          chargeObject = (OrderItemService) objectMap.get("Laboratory");
        } else {
          chargeObject = (OrderItemService) objectMap.get(packageContent
              .getActivityType());
        }
        try {
          chargeMap = orderService.getAllBedTypeChargeMap(paramMap,
              chargeObject);
          //inserting package_content_id for using it during insertion
          chargeMap.put("package_content_id", paramMap.get("package_content_id"));
        } catch (ParseException pe) {
          logger.error("errror while getting charge PackageContentChargesServices.java", pe);
        } catch (SQLException se) {
          logger.error("errror while getting charge PackageContentChargesServices.java", se);
        }
        contentCharges.add(chargeMap);
      }
      saveMasterPackageCharges(contentCharges, orgId, packageId);
      insertPackageContentCharges(contentCharges, orgId, packageId);
    }
  }

  /**
   * Save package content charge from respective masters.
   *
   * @param packageContentId package Content Id
   * @param orgId            orgId
   * @param bedType          bed type
   * @param charge           charge
   */
  private void insertToPackaeContentCharges(Integer packageContentId, String orgId,
      String bedType, BigDecimal charge) {
    PackageContentCharges packageContentCharges = new PackageContentCharges();
    packageContentCharges.setPackageContentId(packageContentId);
    packageContentCharges.setOrgId(orgId);
    packageContentCharges.setBedType(bedType);
    packageContentCharges.setDiscount(BigDecimal.ZERO);
    packageContentCharges.setCharge(charge);
    packageContentCharges.setIsOverride("N");
    packageContentCharges.setCreatedAt(DateUtil.getCurrentTimestamp());
    packageContentCharges.setCreatedBy("InstaAdmin");
    this.repository.save(packageContentCharges);

  }

  /**
   * Save PackageContentCharges.
   *
   * @param packageContentCharges object reference
   * @return saved PackageContentCharges
   */
  public PackageContentCharges save(PackageContentCharges packageContentCharges) {
    return this.repository.save(packageContentCharges);
  }

  /**
   * Update package content charges and derived rate plan package content charges.
   *
   * @param packageContentChargesVOS Value object of packgeContentCharges
   * @param packageId package identifier
   * @param orgId organization identifier
   */
  public void updatePackageContentCharge(
      List<PackageContentChargesVO> packageContentChargesVOS, Integer packageId,
      String orgId) {
    for (PackageContentChargesVO packageContentChargesVO : packageContentChargesVOS) {
      PackageContentCharges packageContentCharges =
          this.repository.findOne(packageContentChargesVO.getContentChargeId());
      if (packageContentCharges != null) {
        packageContentCharges.setDiscount(packageContentChargesVO.getDiscount());
        packageContentCharges.setCharge(packageContentChargesVO.getCharge());
        packageContentCharges.setIsOverride(packageContentChargesVO.getIsOverride());
        packageContentCharges.setModifiedAt(DateUtil.getCurrentTimestamp());
        packageContentCharges
            .setModifiedBy((String) sessionService.getSessionAttributes().get("userId"));
        this.repository.save(packageContentCharges);
      } else {
        packageContentCharges = new PackageContentCharges();
        packageContentCharges.setBedType(packageContentChargesVO.getBedType());
        packageContentCharges.setOrgId(orgId);
        packageContentCharges.setPackageContentId(packageContentChargesVO.getPackageContentId());
        packageContentCharges.setDiscount(packageContentChargesVO.getDiscount());
        packageContentCharges.setCharge(packageContentChargesVO.getCharge());
        packageContentCharges.setIsOverride(packageContentChargesVO.getIsOverride());
        packageContentCharges.setModifiedAt(DateUtil.getCurrentTimestamp());
        packageContentCharges
                .setModifiedBy((String) sessionService.getSessionAttributes().get("userId"));
        packageContentCharges.setCreatedAt(DateUtil.getCurrentTimestamp());
        packageContentCharges
                .setCreatedBy((String) sessionService.getSessionAttributes().get("userId"));
        this.repository.save(packageContentCharges);

      }
    }


    if (CollectionUtils.isNotEmpty(packageChargesService.getDerivedDetails(orgId, packageId))) {
      scheduleDerivedRatePlansJob(packageId, orgId);
    }
  }

  private void scheduleDerivedRatePlansJob(Integer packageId, String orgId) {
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("packageId", packageId);
    jobData.put("orgId", orgId);
    jobData.put("schema", RequestContext.getSchema());
    jobData.put("username", RequestContext.getUserName());
    BasicDynaBean masterJobScheduler = masterChargesCronSchedulerDetailsRepository.getBean();
    masterJobScheduler.set("status", "P");
    masterJobScheduler.set("entity", "EditPackageRate");
    masterJobScheduler.set("entity_id", String.valueOf(packageId));
    masterJobScheduler.set("charge", BigDecimal.ZERO);
    masterJobScheduler.set("discount", BigDecimal.ZERO);
    masterChargesCronSchedulerDetailsRepository.insert(masterJobScheduler);
    jobService.scheduleImmediate(
        buildJob("updateDerivedPackageRatePlan-" + packageId + "-" + orgId,
            UpdateDerivedPackageChargesJob.class, jobData));
  }

  /**
   * Update derived package content charges.
   *
   * @param packageId the package id
   * @param orgId the org id
   */
  public void updateDerivedPackageContentCharges(int packageId, String orgId, String modifiedBy) {
    List<DerivedRatePlansDTO> derivedDetails =
        this.packageChargesService.getDerivedDetails(orgId, packageId);
    List<Integer> packageContentChargesList =
        this.packageContentService.findAllByPackageIdCustom(packageId);
    List<BasicDynaBean> bedTypeList = this.bedTypeService.getAllBedTypes();
    for (DerivedRatePlansDTO derivedDetail : derivedDetails) {

      if (!("Y").equals(derivedDetail.getIsOverride())
          && Boolean.TRUE.equals(derivedDetail.getApplicable())) {

        for (BasicDynaBean bedType : bedTypeList) {
          for (Integer packageContentChargeId : packageContentChargesList) {

            PackageContentCharges regularCharge =
                this.repository.findByPackageContentIdAndBedTypeAndOrgId(
                    packageContentChargeId, (String) bedType.get("bed_type"), orgId);

            PackageContentCharges derivedCharge = this.repository
                .findByPackageContentIdAndBedTypeAndOrgId(packageContentChargeId,
                    (String) bedType.get("bed_type"), derivedDetail.getOrgId());

            if (regularCharge != null && derivedCharge != null) {
              Double charge = this.packageChargesService.calculateCharge(
                  regularCharge.getCharge().doubleValue(),
                  derivedDetail.getRateVariationPercent(), derivedDetail.getRoundOffAmount(),
                  derivedDetail.getDiscormarkup());

              Double regularDiscount = regularCharge.getDiscount() != null
                  ? regularCharge.getDiscount().doubleValue()
                  : 0;

              Double discount = this.packageChargesService.calculateCharge(regularDiscount,
                  derivedDetail.getRateVariationPercent(), derivedDetail.getRoundOffAmount(),
                  derivedDetail.getDiscormarkup());
              derivedCharge.setCharge(new BigDecimal(charge));
              derivedCharge.setDiscount(new BigDecimal(discount));
              derivedCharge.setModifiedAt(DateUtil.getCurrentTimestamp());
              derivedCharge
                  .setModifiedBy(modifiedBy);
              this.repository.save(derivedCharge);
            }
          }
        }
      }
    }
  }


  /**
   * Find by package content id.
   *
   * @param packageContentId the package content id
   * @return the list
   */
  public List<PackageContentCharges> findByPackageContentId(Integer packageContentId) {
    return this.repository.findByPackageContentId(packageContentId);
  }

  /**
   * Find by package content id in.
   *
   * @param packageContentId the package content id
   * @return the list
   */
  public List<PackageContentCharges> findByPackageContentIdIn(
      Iterable<Integer> packageContentId) {
    return this.repository.findByPackageContentIdIn(packageContentId);
  }

  /**
   * Delete package content charge by package contents.
   *
   * @param packageContentIds the package content ids
   * @return the int
   */
  public int deletePackageContentChargeByPackageContents(Iterable<Integer> packageContentIds) {
    if (!Iterables.isEmpty(packageContentIds)) {
      return this.repository.deletePackageContentChargeByPackageContents(packageContentIds);
    } else {
      return 0;
    }
  }

  /**
   * Save default package content charge.
   *
   * @param packageContentId the package content id
   */
  public void saveDefaultPackageContentCharge(Integer packageContentId) {
    this.repository.saveDefaultPackageContentChargeByPackageContentId(packageContentId);
  }

  /**
   * Find all by package id and org id sorted by bed type.
   *
   * @param packageId the package id
   * @param orgId the org id
   * @return the list
   */
  public List<PackageContentCharges> findAllByPackageIdAndOrgIdSortedByBedType(int packageId,
      String orgId) {
    return this.repository.findAllByPackageIdAndOrgIdSortedByBedType(packageId, orgId);
  }


  /**
   * Update package charge by package id.
   *
   * @param packageId the package id
   * @return the int
   */
  public int updatePackageChargeByPackageId(Integer packageId) {

    return this.repository.updatePackageChargeByPackageId(packageId);
  }

  public void removePackageContentChargesByPackageIdAndOrgIdIn(Integer packageId,
                                                               List<String> orgIds) {
    this.repository.removePackageContentChargesByPackageIdAndOrgIdIn(packageId, orgIds);
  }

  public PackageContentCharges findPackageContentActivityChargesByPackageContentId(
      Integer packageContentId,
      String bedType, String orgId) {
    return this.repository
        .findPackageContentActivityChargesByPackageContentId(packageContentId, bedType, orgId);
  }
}
