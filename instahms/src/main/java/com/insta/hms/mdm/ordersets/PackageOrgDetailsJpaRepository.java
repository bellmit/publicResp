package com.insta.hms.mdm.ordersets;

import com.insta.hms.model.PackOrgDetailsIdSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author manika.singh
 * @since 23/04/19
 */
public interface PackageOrgDetailsJpaRepository extends JpaRepository<PackOrgDetailsModel,
    PackOrgDetailsIdSequence> {

  PackOrgDetailsModel findById(PackOrgDetailsIdSequence packOrgDetailsIdSequence);

  @Query(value = "SELECT org_id from pack_org_details where package_id = ?1", nativeQuery = true)
  List<String> findOrgIdsByPackageId(Integer packageId);

  List<PackOrgDetailsModel> removeByIdPackageIdAndIdOrgIdIn(Integer packageId,
      List<String> orgIdList);
}
