package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.annotations.LazyAutowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author manika.singh
 * @since 15/05/19
 */
@Service
public class PackagesInsuranceCategoryService {

  @LazyAutowired
  private PackagesInsuranceCategoryJpaRepository repository;

  public PackagesInsuranceCategoryMapping save(PackagesInsuranceCategoryMapping model) {
    return this.repository.save(model);
  }

  public List<PackagesInsuranceCategoryMapping> findAllByPackageId(int packageId) {
    return this.repository.findAllByPackageId(packageId);
  }

  public void delete(List<PackagesInsuranceCategoryMapping>
      packagesInsuranceCategoryMappings) {
    repository.delete(packagesInsuranceCategoryMappings);
  }
}
