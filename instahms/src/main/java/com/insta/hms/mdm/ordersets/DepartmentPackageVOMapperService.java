package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.AbstractViewObjectMapper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.departments.DepartmentService;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentPackageVOMapperService extends
    AbstractViewObjectMapper<DeptPackageApplicabilityModel, DeptPackageApplicabilityVO> {

  @LazyAutowired
  private DepartmentService departmentService;

  @Override
  public DeptPackageApplicabilityVO convertModelToViewObject(
      DeptPackageApplicabilityModel modelObj) {
    DeptPackageApplicabilityVO deptPackageApplicabilityVO = new DeptPackageApplicabilityVO();
    if ("*".equals(modelObj.getDeptId())) {
      deptPackageApplicabilityVO.setDeptName("All Departments");
    } else {
      Map departmentDetails = departmentService.getDepartmentDetails(modelObj.getDeptId());
      if (departmentDetails != null) {
        deptPackageApplicabilityVO.setDeptName((String) departmentDetails.get("dept_name"));
      }
    }
    deptPackageApplicabilityVO.setDeptId(modelObj.getDeptId());
    return deptPackageApplicabilityVO;
  }

  @Override
  public DeptPackageApplicabilityModel convertViewObjectToModel(
      DeptPackageApplicabilityVO viewObj) {
    DeptPackageApplicabilityModel deptPackageApplicabilityModel =
        new DeptPackageApplicabilityModel();
    deptPackageApplicabilityModel.setDeptId(viewObj.getDeptId());
    return deptPackageApplicabilityModel;
  }

}
