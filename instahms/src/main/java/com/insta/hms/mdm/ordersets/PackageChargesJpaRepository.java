package com.insta.hms.mdm.ordersets;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author manika.singh
 * @since 15/04/19
 */
public interface PackageChargesJpaRepository extends JpaRepository<PackageChargesModel, Integer> {

  /**
   * Find By PackageId and OrgId.
   * @param packageId package identifier
   * @param orgId organization identifier
   * @param sort sort order
   * @return list of package charges
   */
  List<PackageChargesModel> findByPackageIdAndOrgId(Integer packageId, String orgId, Sort sort);

  @Query(value = "SELECT rp.org_id AS org_id, od.org_name AS org_name, CASE WHEN "
      + " rate_variation_percent<0 then 'Decrease By' else 'Increase By' end as discormarkup, "
      + " rate_variation_percent AS rate_variation_percent, round_off_amount AS round_off_amount,"
      + " pod.applicable"
      + " AS applicable, pod.package_id AS package_id, rp.base_rate_sheet_id AS base_rate_sheet_id,"
      + " pod.is_override AS is_override from rate_plan_parameters rp JOIN "
      + " organization_details od ON (od.org_id=rp.org_id) JOIN pack_org_details pod ON "
      + " (pod.org_id = rp.org_id) where rp.base_rate_sheet_id =?1 and package_id=?2 "
      + " and pod.base_rate_sheet_id=?1", nativeQuery = true)
  List<Object[]> findDerivedDetails(String orgId, int packageId);


  @Modifying
  @Query(value = "INSERT into package_charges(package_id, org_id, bed_type, charge, discount, "
      + " is_override, created_by, created_at, modified_by, modified_at)"
      + " SELECT :packageId as package_id, od.org_id as org_id , bt.bed_type_name AS bed_type, 0,0,"
      + " 'N', 'InstaAdmin' as created_by, now() as created_at, NULL as modified_by, "
      + " NULL as modified_at FROM organization_details od, bed_types bt "
      + " WHERE od.org_id IN :orgIds AND bed_type_name IN :bedTypes "
      + " AND bt.billing_bed_type='Y' AND bt.status = 'A'",
      nativeQuery = true)
  int saveDefaultPackageCharges(@Param("packageId") int packageId,
                                @Param("orgIds") List<String> orgIds,
                                @Param("bedTypes") List<String> bedTypes);

  PackageChargesModel findByPackageIdAndOrgIdAndBedType(Integer packageId, String orgId,
                                                        String bedType);

  List<PackageChargesModel> removeByPackageIdAndOrgIdIn(Integer packageId, List<String> orgIds);
}
