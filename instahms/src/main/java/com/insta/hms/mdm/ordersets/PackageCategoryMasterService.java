package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.BaseJPAService;
import com.insta.hms.common.annotations.LazyAutowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author manika.singh
 * @since 25/04/19
 */
@Service
public class PackageCategoryMasterService extends
    BaseJPAService<PackageCategoryMasterJpaRepository, PackageCategoryMasterModel, Integer> {

  @LazyAutowired
  public PackageCategoryMasterService(PackageCategoryMasterJpaRepository repository) {
    super(repository);
  }

  /**
   * Gets the active package categories.
   *
   * @return the active package categories
   */
  public List<PackageCategoryMasterModel> findActiveCategories() {
    return this.repository.findByStatus("A");
  }

}
