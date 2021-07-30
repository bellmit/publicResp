package com.insta.hms.mdm.ordersets;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author manika.singh
 * @since 21/06/19
 */
public interface PackagePlanRepository extends JpaRepository<PackagePlanMasterModel, Integer> {
  
  public List<PackagePlanMasterModel> findByPackId(Integer packageId);
  

}
