package com.insta.hms.mdm.ordersets;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author manika.singh
 * @since 13/06/19
 */
public interface PackageSponsorJpaRepository extends JpaRepository<PackageSponsorMasterModel,
    Integer> {
  
  public List<PackageSponsorMasterModel> findByPackId(Integer packageId);
  
}
