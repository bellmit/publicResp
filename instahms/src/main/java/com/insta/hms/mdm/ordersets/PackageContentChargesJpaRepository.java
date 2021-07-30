package com.insta.hms.mdm.ordersets;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * The Interface PackageContentChargesJpaRepository.
 *
 * @author manika.singh
 * @since 15/04/19
 */
public interface PackageContentChargesJpaRepository
    extends JpaRepository<PackageContentCharges, Integer> {

  /**
   * Find by package content id and org id.
   *
   * @param packageContentId the package content id
   * @param orgId the org id
   * @param sort the sort
   * @return the list
   */
  List<PackageContentCharges> findByPackageContentIdAndOrgId(int packageContentId,
      String orgId, Sort sort);

  /**
   * Save default package content charge by package id.
   *
   * @param packageId the package id
   * @param orgIds the org ids
   * @param bedTypes the bed types
   * @return the int
   */
  @Modifying
  @Query(value = "INSERT into package_content_charges(content_charge_id, package_content_id,"
      + " org_id, bed_type, charge, discount, is_override, created_by, created_at,"
      + " modified_by, modified_at)"
      + " SELECT nextval('package_content_charges_seq'), pc.package_content_id "
      + " as package_content_id, od.org_id as org_id , bt.bed_type_name AS bed_type, 0,0, 'N',"
      + " 'InstaAdmin' as created_by, now() as created_at, NULL as modified_by,"
      + " NULL as modified_at FROM organization_details od, bed_types bt, package_contents pc "
      + " WHERE od.org_id IN :orgIds AND bed_type_name IN :bedTypes "
      + " AND pc.package_id =:packageId AND bt.billing_bed_type='Y' " + " AND bt.status = 'A'",
      nativeQuery = true)
  int saveDefaultPackageContentChargeByPackageId(@Param("packageId") Integer packageId,
      @Param("orgIds") List<String> orgIds, @Param("bedTypes") List<String> bedTypes);

  /**
   * Find by package content id and bed type and org id.
   *
   * @param packageContentId the package content id
   * @param bedType the bed type
   * @param orgId the org id
   * @return the package content charges
   */
  PackageContentCharges findByPackageContentIdAndBedTypeAndOrgId(Integer packageContentId,
      String bedType, String orgId);

  /**
   * Find by package content id.
   *
   * @param packageContentId the package content id
   * @return the list
   */
  List<PackageContentCharges> findByPackageContentId(Integer packageContentId);

  /**
   * Find by package content id in.
   *
   * @param ids the ids
   * @return the list
   */
  List<PackageContentCharges> findByPackageContentIdIn(Iterable<Integer> ids);

  /**
   * Delete package content charge by package contents.
   *
   * @param packageContentIds the package content ids
   * @return the int
   */
  @Modifying
  @Query(value = "DELETE FROM package_content_charges where "
      + " package_content_id IN (:packageContentIds)", nativeQuery = true)
  int deletePackageContentChargeByPackageContents(
      @Param("packageContentIds") Iterable<Integer> packageContentIds);


  /**
   * Save default package content charge by package content id.
   *
   * @param packageContentId the package content id
   * @return the int
   */
  @Modifying
  @Query(value = "INSERT into package_content_charges(content_charge_id, package_content_id,"
      + "      org_id, bed_type, charge, discount, is_override, created_by, created_at,"
      + "      modified_by, modified_at)"
      + "      SELECT nextval('package_content_charges_seq'), pc.package_content_id "
      + "      as package_content_id, od.org_id as org_id , bt.bed_type_name AS bed_type, "
      + " 0,0, 'N',"
      + "      'InstaAdmin' as created_by, now() as created_at, NULL as modified_by,"
      + "      NULL as modified_at FROM organization_details od, bed_types bt, package_contents pc "
      + "      WHERE pc.package_content_id=:packageContentId AND bt.billing_bed_type='Y' "
      + "      AND bt.status = 'A'", nativeQuery = true)
  int saveDefaultPackageContentChargeByPackageContentId(
      @Param("packageContentId") Integer packageContentId);

  /**
   * Find all by package id and org id sorted by bed type.
   *
   * @param packageId the package id
   * @param orgId the org id
   * @return the list
   */
  @Query(value = "SELECT pcc.* FROM package_content_charges pcc JOIN package_contents pc"
      + " using (package_content_id) where pc.package_id =? AND pcc.org_id=? ORDER "
      + " BY pc.display_order, pc.package_content_id, CASE WHEN"
      + " bed_type= 'GENERAL' then 0 else 1 end, lower(bed_type)", nativeQuery = true)
  List<PackageContentCharges> findAllByPackageIdAndOrgIdSortedByBedType(int packageId,
      String orgId);

  /**
   * Update package charge by package id.
   *
   * @param packageId the package id
   * @return the int
   */
  @Modifying
  @Query(value = "UPDATE package_charges pch SET charge = foo.charge FROM "
      + " (SELECT sum(pcc.charge) as charge, package_id, org_id, bed_type FROM "
      + " package_content_charges pcc JOIN package_contents pc ON "
      + " (pc.package_content_id=pcc.package_content_id) WHERE package_id=:packageId "
      + " GROUP BY package_id, org_id, bed_type) AS foo"
      + " WHERE foo.package_id = pch.package_id and foo.org_id = pch.org_id and foo.bed_type ="
      + " pch.bed_type;", nativeQuery = true)
  int updatePackageChargeByPackageId(@Param("packageId") Integer packageId);

  @Modifying
  @Query(value = "DELETE from package_content_charges "
         + " WHERE package_content_id IN (SELECT package_content_id from package_contents "
         + "   WHERE package_id=?1) AND org_id IN (?2) ", nativeQuery = true)
  void removePackageContentChargesByPackageIdAndOrgIdIn(Integer packageId, List<String> orgIds);

  @Query(value = "SELECT pcc.* from package_content_charges pcc "
          + " JOIN package_contents pc ON pcc.package_content_id = pc.package_content_id "
          + " WHERE pc.package_content_id = ?1 AND pcc.bed_type = ?2 AND org_id = ?3",
          nativeQuery = true)
  PackageContentCharges findPackageContentActivityChargesByPackageContentId(
      Integer packageContentId,
      String bedType, String orgId);
}
