package com.insta.hms.mdm.ordersets;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * The Interface DepartmentPackageApplicabilityJpaRepository.
 */
public interface DepartmentPackageApplicabilityJpaRepository
    extends JpaRepository<DeptPackageApplicabilityModel, Integer> {
  
  /**
   * Find by package id.
   *
   * @param packageId the package id
   * @return the list
   */
  public List<DeptPackageApplicabilityModel> findByPackageId(Integer packageId);
  
}
