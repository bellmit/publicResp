package com.insta.hms.mdm.ordersets;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author manika.singh
 * @since 10/04/19
 */
public interface PackageJpaRepository extends JpaRepository<PackagesModel, Integer> {

  PackagesModel findByPackageName(String packageName);

  List<PackagesModel> findByPackageIdIn(List<Integer> packageId);

  List<PackagesModel> findByPackageCodeEquals(String packageCode);
}
