package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.AbstractViewObjectMapper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.ordersets.SponsorPackageApplicabilityVO.SponsorApplicabilityType;
import com.insta.hms.mdm.tpas.TpaService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class PackageSponsorMasterVOMappingService.
 */
@Service
public class PackageSponsorMasterVOMappingService
    extends AbstractViewObjectMapper<PackageSponsorMasterModel, PackageSponsorMasterVO> {

  private static final Character STATUS_ACTIVE = 'A';

  /** The tpa service. */
  @LazyAutowired
  private TpaService tpaService;

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.common.AbstractViewObjectMapper#convertModelToViewObject(java.lang.Object)
   */
  @Override
  public PackageSponsorMasterVO convertModelToViewObject(PackageSponsorMasterModel modelObj)
      throws Exception {
    if (modelObj != null) {
      PackageSponsorMasterVO packageSponsorMasterVO = new PackageSponsorMasterVO();
      packageSponsorMasterVO.setTpaId(modelObj.getTpaId());
      if (!("-1".equals(modelObj.getTpaId()) || "0".equals(modelObj.getTpaId()))) {
        BasicDynaBean tpaDetails = tpaService.getDetails(modelObj.getTpaId());
        String tpaName = (String) tpaDetails.get("tpa_name");
        packageSponsorMasterVO.setTpaName(tpaName);
      }
      return packageSponsorMasterVO;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.common.AbstractViewObjectMapper#convertViewObjectToModel(java.lang.Object)
   */
  @Override
  public PackageSponsorMasterModel convertViewObjectToModel(PackageSponsorMasterVO viewObj)
      throws Exception {
    if (viewObj != null) {
      PackageSponsorMasterModel packageSponsorMasterModel = new PackageSponsorMasterModel();
      packageSponsorMasterModel.setStatus(STATUS_ACTIVE);
      packageSponsorMasterModel.setTpaId(viewObj.getTpaId());
      packageSponsorMasterModel.setPackId(viewObj.getPackageId());
      return packageSponsorMasterModel;
    }
    return null;
  }

  /**
   * Construct sponsor package applicability VO.
   *
   * @param sponsorMasterModelVOObjects the sponsor master model VO objects
   * @return the sponsor package applicability VO
   */
  public SponsorPackageApplicabilityVO constructSponsorPackageApplicabilityVO(
      List<PackageSponsorMasterVO> sponsorMasterModelVOObjects) {
    SponsorPackageApplicabilityVO sponsorPackageApplicabilityVO =
        new SponsorPackageApplicabilityVO();
    sponsorPackageApplicabilityVO.setSponsorApplicabilityType(
        SponsorApplicabilityType.SOME_SPONSORS.getApplicabilityType());

    for (PackageSponsorMasterVO packageSponsorMasterVO : sponsorMasterModelVOObjects) {
      if (packageSponsorMasterVO.getTpaId().equals("-1")) {
        // if any tpa id of a package is -1, then it is applicable to all sponsors.
        return returnAllSponsorApplicabilityVO();
      } else if (packageSponsorMasterVO.getTpaId().equals("0")) {
        sponsorPackageApplicabilityVO.setSponsorApplicabilityType(
            SponsorApplicabilityType.NO_SPONSORS.getApplicabilityType());
      }
    }
    sponsorPackageApplicabilityVO.setSponsorList(sponsorMasterModelVOObjects);
    return sponsorPackageApplicabilityVO;
  }

  /**
   * Return all sponsor applicability VO.
   *
   * @return the sponsor package applicability VO
   */
  private SponsorPackageApplicabilityVO returnAllSponsorApplicabilityVO() {
    SponsorPackageApplicabilityVO sponsorPackageApplicabilityVO =
        new SponsorPackageApplicabilityVO();
    sponsorPackageApplicabilityVO.setSponsorApplicabilityType(
        SponsorApplicabilityType.ALL_SPONSORS.getApplicabilityType());
    sponsorPackageApplicabilityVO.setSponsorList(new ArrayList<PackageSponsorMasterVO>());
    return sponsorPackageApplicabilityVO;
  }

}
