package com.insta.hms.mdm.ordersets;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author manika.singh
 * @since 10/04/19
 */
@Repository
public interface PackageContentJpaRepository extends JpaRepository<PackageContentsModel, Integer> {

  @Query(value = "select package_content_id, package_id, activity_id, activity_type, activity_qty,"
      + " activity_qty_uom, activity_remarks, doctor_id,dept_id, display_order,modified_by,"
      + " modified_at,created_by,created_at,consultation_type_id,charge_head,conduction_gap,"
      + " conduction_gap_unit,parent_pack_ob_id,operation_id,bed_id,panel_id,"
      + " visit_qty_limit, case when (activity_type = 'Operation' OR activity_type = 'Bed' OR "
      + " activity_type = 'ICU' OR panel_id is not null) "
      + " then coalesce(content_id_ref, package_content_id) else null end"
      + " as content_id_ref from package_contents  "
      + " where package_id = ?1 order by display_order, package_content_id", nativeQuery = true)
  List<PackageContentsModel> findAllByPackageId(Integer packageId);

  @Query(value = "Select pc.package_content_id from package_contents pc WHERE pc.package_id = ?1",
      nativeQuery = true)
  List<Integer> findAllByPackageIdCustom(Integer packageId);
  
  /**
   * Delete package content by package contents.
   *
   * @param packageContentIds the package content ids
   * @return the int
   */
  @Modifying
  @Query(value = "DELETE FROM package_contents where "
      + " package_content_id IN (:packageContentIds)", nativeQuery = true)
  int deletePackageContentByPackageContentId(
      @Param("packageContentIds") Iterable<Integer> packageContentIds);
  
}
