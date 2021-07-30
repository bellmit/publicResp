package com.insta.hms.mdm.ordersets;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseJPAService;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.departments.DepartmentService;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

/**
 * The Class DepartmentPackageApplicabilityService.
 */
@Service
public class DepartmentPackageApplicabilityService extends
    BaseJPAService<DepartmentPackageApplicabilityJpaRepository, 
      DeptPackageApplicabilityModel, Integer> {

  /** The object mapper service. */
  @LazyAutowired
  private DepartmentPackageVOMapperService objectMapperService;

  /**
   * Instantiates a new department package applicability service.
   *
   * @param repository the repository
   */
  @LazyAutowired
  public DepartmentPackageApplicabilityService(
      DepartmentPackageApplicabilityJpaRepository repository) {
    super(repository);
  }



  /** The department service. */
  @LazyAutowired
  private DepartmentService departmentService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /**
   * Save all.
   *
   * @param model the model
   * @return the list
   */
  public List<DeptPackageApplicabilityModel> saveAll(
      List<DeptPackageApplicabilityModel> model) {
    return this.repository.save(model);
  }

  /**
   * Save.
   *
   * @param model the model
   * @return the dept package applicability model
   */
  public DeptPackageApplicabilityModel save(DeptPackageApplicabilityModel model) {
    return this.repository.save(model);
  }


  /**
   * Find all by package id.
   *
   * @param packageId the package id
   * @return the list
   */
  public List<DeptPackageApplicabilityModel> findAllByPackageId(Integer packageId) {
    return this.repository.findByPackageId(packageId);
  }

  /**
   * Delete all.
   *
   * @param deptPackageApplicabilityModels the dept package applicability models
   */
  public void deleteAll(List<DeptPackageApplicabilityModel> deptPackageApplicabilityModels) {
    this.repository.delete(deptPackageApplicabilityModels);
    this.repository.flush();
  }

  /**
   * Save or update package department applicability.
   *
   * @param packageId the package id
   * @param departmentPackageApplicability the department package applicability
   */
  public void saveOrUpdatePackageDepartmentApplicability(int packageId,
      List<DeptPackageApplicabilityVO> departmentPackageApplicability) {
    List<DeptPackageApplicabilityModel> newDeptApplicabilities =
        objectMapperService.convertViewObjectsToModels(departmentPackageApplicability);;
    List<DeptPackageApplicabilityModel> existingApplicabilities =
        findAllByPackageId(packageId);
    boolean isUpdate = CollectionUtils.isNotEmpty(existingApplicabilities);
    Timestamp currentTime = null;
    String currentUser = null;
    if (isUpdate) {
      currentTime = DataBaseUtil.getDateandTime();
      currentUser = (String) sessionService.getSessionAttributes().get("userId");
    }
    for (DeptPackageApplicabilityModel deptPackageApplicabilityModel : newDeptApplicabilities) {
      if (isUpdate) {
        deptPackageApplicabilityModel.setModifiedAt(currentTime);
        deptPackageApplicabilityModel.setModifiedBy(currentUser);
      }
      deptPackageApplicabilityModel.setPackageId(packageId);
    }
    saveOrUpdate(existingApplicabilities, newDeptApplicabilities, true);
  }

  /**
   * Find all VO by package id.
   *
   * @param packageId the package id
   * @return the list
   */
  public List<DeptPackageApplicabilityVO> findAllVOByPackageId(int packageId) {
    List<DeptPackageApplicabilityModel> deptPackageApplicabilityModels =
        this.repository.findByPackageId(packageId);
    return objectMapperService.convertModelsToViewObjects(deptPackageApplicabilityModels);
  }
}
