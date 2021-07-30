package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.BaseJPAService;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.itemtaxuploaddownloads.ServiceItemSubGroupService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The Class PackageItemSubGroupTaxService.
 */
@Service
public class PackageItemSubGroupTaxService extends
    BaseJPAService<PackageItemSubGroupsJpaRepository, PackageItemSubGroupsModel, Integer> {

  /** The object mapper service. */
  @LazyAutowired
  private ItemSubGroupPackageVOMapperService objectMapperService;

  /**
   * Instantiates a new package item sub group tax service.
   *
   * @param repository the repository
   */
  @LazyAutowired
  public PackageItemSubGroupTaxService(PackageItemSubGroupsJpaRepository repository) {
    super(repository);
  }

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(PackageItemSubGroupTaxService.class);

  /** The package item sub groups jpa repository. */
  @LazyAutowired
  private PackageItemSubGroupsJpaRepository packageItemSubGroupsJpaRepository;

  /**
   * Save all.
   *
   * @param packageTaxSubGroupList the package tax sub group list
   * @return the list
   */
  public List<PackageItemSubGroupsModel> saveAll(
      List<PackageItemSubGroupsModel> packageTaxSubGroupList) {
    return this.packageItemSubGroupsJpaRepository.save(packageTaxSubGroupList);
  }

  /**
   * Save all.
   *
   * @param packageTaxSubGroup the package tax sub group
   * @return the package item sub groups model
   */
  public PackageItemSubGroupsModel saveAll(PackageItemSubGroupsModel packageTaxSubGroup) {
    return this.packageItemSubGroupsJpaRepository.save(packageTaxSubGroup);
  }


  /**
   * Find all VO by package id.
   *
   * @param packageId the package id
   * @return the list
   */
  public List<PackageItemSubGroupsVO> findAllVOByPackageId(Integer packageId) {
    List<PackageItemSubGroupsModel> itemSubGroupsModels = findAllByPackageId(packageId);
    return objectMapperService.convertModelsToViewObjects(itemSubGroupsModels);

  }

  /**
   * Find all by package id.
   *
   * @param packageId the package id
   * @return the list
   */
  public List<PackageItemSubGroupsModel> findAllByPackageId(Integer packageId) {
    return this.packageItemSubGroupsJpaRepository.findByPackageId(packageId);
  }

  /**
   * Delete all.
   *
   * @param itemSubGroupsModels the item sub groups models
   */
  public void deleteAll(List<PackageItemSubGroupsModel> itemSubGroupsModels) {
    this.packageItemSubGroupsJpaRepository.delete(itemSubGroupsModels);
    this.packageItemSubGroupsJpaRepository.flush();
  }

  /**
   * Save or update package tax applicability.
   *
   * @param packageId the package id
   * @param packageTaxApplicability the package tax applicability
   */
  public void saveOrUpdatePackageTaxApplicability(int packageId,
      List<PackageItemSubGroupsVO> packageTaxApplicability) {
    List<PackageItemSubGroupsModel> newTaxApplicabilities =
        objectMapperService.convertViewObjectsToModels(packageTaxApplicability);
    List<PackageItemSubGroupsModel> existingTaxApplicabilities =
        this.packageItemSubGroupsJpaRepository.findByPackageId(packageId);
    for (PackageItemSubGroupsModel newTaxApplicability : newTaxApplicabilities) {
      newTaxApplicability.setPackageId(packageId);
    }
    saveOrUpdate(existingTaxApplicabilities, newTaxApplicabilities, true);
  }

}
