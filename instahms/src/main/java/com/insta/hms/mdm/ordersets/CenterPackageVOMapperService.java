package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.AbstractViewObjectMapper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.centers.CenterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

@Service
public class CenterPackageVOMapperService extends
    AbstractViewObjectMapper<CenterPackageApplicabilityModel, CenterPackageApplicabilityVO> {

  @LazyAutowired
  private CenterService centerService;

  @Override
  public CenterPackageApplicabilityVO convertModelToViewObject(
      CenterPackageApplicabilityModel applicabilityModel) {
    CenterPackageApplicabilityVO centerPackageApplicabilityVO =
        new CenterPackageApplicabilityVO();
    centerPackageApplicabilityVO.setCenterId(applicabilityModel.getCenterId());
    if (applicabilityModel.getCenterId() != -1) {
      BasicDynaBean centerDetails =
          centerService.getCenterDetails(applicabilityModel.getCenterId());
      centerPackageApplicabilityVO.setCenterName((String) centerDetails.get("center_name"));
      centerPackageApplicabilityVO.setCityName((String) centerDetails.get("city_name"));
      centerPackageApplicabilityVO.setStateName((String) centerDetails.get("state_name"));
    } else {
      centerPackageApplicabilityVO.setCenterName("All Centers");
    }
    return centerPackageApplicabilityVO;
  }

  @Override
  public CenterPackageApplicabilityModel convertViewObjectToModel(
      CenterPackageApplicabilityVO viewObj) {
    CenterPackageApplicabilityModel centerPackageApplicabilityModel =
        new CenterPackageApplicabilityModel();
    centerPackageApplicabilityModel.setCenterId(viewObj.getCenterId());
    return centerPackageApplicabilityModel;
  }


}
