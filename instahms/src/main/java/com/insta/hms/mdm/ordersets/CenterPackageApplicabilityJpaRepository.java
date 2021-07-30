package com.insta.hms.mdm.ordersets;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author manika.singh
 * @since 23/04/19
 */
public interface CenterPackageApplicabilityJpaRepository
    extends JpaRepository<CenterPackageApplicabilityModel, Integer> {

  public List<CenterPackageApplicabilityModel> findByPackageId(Integer packageId);
  
}
