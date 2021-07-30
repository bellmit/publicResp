package com.insta.hms.mdm.ordersets;

import com.google.common.collect.Iterables;
import com.insta.hms.common.BaseJPAService;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author manika.singh
 * @since 19/04/19
 */
@Service
public class PackageContentService
    extends BaseJPAService<PackageContentJpaRepository, PackageContentsModel, Integer> {
  @LazyAutowired
  public PackageContentService(PackageContentJpaRepository repository) {
    super(repository);
  }

  public List<Integer> findAllByPackageIdCustom(Integer packageId) {
    return this.repository.findAllByPackageIdCustom(packageId);
  }

  public List<PackageContentsModel> findByPackageId(Integer packageId) {
    return this.repository.findAllByPackageId(packageId);
  }

  public List<PackageContentsModel> findAllByPackageId(Integer packageId) {
    return this.repository.findAllByPackageId(packageId);
  }

  public int getNextSequence() {
    GenericRepository genericRepository = new GenericRepository("package_contents");
    return genericRepository.getNextSequence();
  }

  /**
   * Delete package contents by package contents.
   *
   * @param packageContentIds the package content ids
   * @return the int
   */
  public int deletePackageContentByPackageContentId(Iterable<Integer> packageContentIds) {
    if (!Iterables.isEmpty(packageContentIds)) {
      return this.repository.deletePackageContentByPackageContentId(packageContentIds);
    } else {
      return 0;
    }
  }

  public PackageContentsModel findById(Integer packageContentId) {
    return this.repository.findOne(packageContentId);
  }

  public void delete(PackageContentsModel packageContentsModel) {
    this.repository.delete(packageContentsModel);
  }
}
