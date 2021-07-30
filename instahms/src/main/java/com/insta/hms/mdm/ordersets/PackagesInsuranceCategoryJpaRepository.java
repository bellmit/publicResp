package com.insta.hms.mdm.ordersets;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author manika.singh
 * @since 10/04/19
 */
public interface PackagesInsuranceCategoryJpaRepository
    extends JpaRepository<PackagesInsuranceCategoryMapping, Integer> {

  List<PackagesInsuranceCategoryMapping> findAllByPackageId(Integer packageId) ;
}
