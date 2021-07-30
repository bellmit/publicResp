package com.insta.hms.mdm.ordersets;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author manika.singh
 * @since 25/04/19
 */
public interface PackageCategoryMasterJpaRepository extends
    JpaRepository<PackageCategoryMasterModel, Integer> {
  List<PackageCategoryMasterModel> findByStatus(String status);
}
