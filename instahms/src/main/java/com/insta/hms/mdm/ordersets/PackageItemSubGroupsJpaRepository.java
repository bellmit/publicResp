package com.insta.hms.mdm.ordersets;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * The Interface PackageItemSubGroupsJpaRepository.
 */
public interface PackageItemSubGroupsJpaRepository
    extends JpaRepository<PackageItemSubGroupsModel, Integer> {
  
  /**
   * Find by package id.
   *
   * @param packageId the package id
   * @return the list
   */
  public List<PackageItemSubGroupsModel> findByPackageId(Integer packageId);
}
