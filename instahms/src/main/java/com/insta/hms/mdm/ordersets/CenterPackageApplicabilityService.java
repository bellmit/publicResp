package com.insta.hms.mdm.ordersets;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseJPAService;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.centers.CenterService;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

/**
 * The Class CenterPackageApplicabilityService.
 *
 * @author manika.singh
 * @since 23/04/19
 */
@Service
public class CenterPackageApplicabilityService extends
    BaseJPAService<CenterPackageApplicabilityJpaRepository,
      CenterPackageApplicabilityModel, Integer> {

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The object mapper service. */
  @LazyAutowired
  private CenterPackageVOMapperService objectMapperService;

  /**
   * Instantiates a new center package applicability service.
   *
   * @param repository the repository
   */
  @LazyAutowired
  public CenterPackageApplicabilityService(
      CenterPackageApplicabilityJpaRepository repository) {
    super(repository);
  }

  /**
   * Find by package id.
   *
   * @param packageId the package id
   * @return the list
   */
  public List<CenterPackageApplicabilityModel> findByPackageId(Integer packageId) {
    return this.repository.findByPackageId(packageId);
  }

  /**
   * Save or update center applicability.
   *
   * @param packageId the package id
   * @param centerPackageApplicability the center package applicability
   */
  public void saveOrUpdateCenterApplicability(int packageId,
      List<CenterPackageApplicabilityVO> centerPackageApplicability) {
    List<CenterPackageApplicabilityModel> existingCenterApplicabilities =
        this.repository.findByPackageId(packageId);
    Timestamp currentTime = DataBaseUtil.getDateandTime();
    String currentUser = (String) sessionService.getSessionAttributes().get("userId");
    boolean isUpdate = CollectionUtils.isNotEmpty(existingCenterApplicabilities);
    List<CenterPackageApplicabilityModel> newCenterApplicabilities =
        objectMapperService.convertViewObjectsToModels(centerPackageApplicability);
    for (CenterPackageApplicabilityModel centerPackageAppModel : newCenterApplicabilities) {
      if (isUpdate) {
        centerPackageAppModel.setModifiedAt(currentTime);
        centerPackageAppModel.setModifiedBy(currentUser);
      }
      centerPackageAppModel.setPackageId(packageId);
    }
    saveOrUpdate(existingCenterApplicabilities, newCenterApplicabilities, true);
  }

  /**
   * Find all VO by package id.
   *
   * @param packageId the package id
   * @return the list
   */
  public List<CenterPackageApplicabilityVO> findAllVOByPackageId(int packageId) {
    List<CenterPackageApplicabilityModel> centerPackageApplicabilityModels =
        this.repository.findByPackageId(packageId);
    return objectMapperService.convertModelsToViewObjects(centerPackageApplicabilityModels);
  }

}
