package com.insta.hms.mdm.packageuom;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.BeanConversionService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Not extending with Master Service because of composite primaryKey (package_uom, issue_uom).
 */

@Service
public class PackageUomService implements BeanConversionService {

  @LazyAutowired
  private PackageUomRepository packageUomRepository;

  public List<BasicDynaBean> listAll() {
    return packageUomRepository.listAll();
  }

  public List<BasicDynaBean> listDistinct() {
    return packageUomRepository.getDistinctPackageIsse();
  }

  public List<BasicDynaBean> listDistinctIssueUom() {
    return packageUomRepository.getDistinctIssueUom();
  }

  @Override
  public BasicDynaBean toBean(Map<String, String[]> requestParams) {
    return null;
  }

  @Override
  public BasicDynaBean toBean(Map<String, String[]> requestParams,
      Map<String, MultipartFile> fileMap) {
    return null;
  }

  @Override
  public Map<String, List<BasicDynaBean>> toBeanList(Map<String, String[]> requestParams,
      BasicDynaBean type) {
    return null;
  }

  /**
   * Gets the package size.
   *
   * @param issueUom the issue uom
   * @param packageUom the package uom
   * @return the package size
   */
  public BigDecimal getPackageSize(String issueUom, String packageUom) {
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("package_uom", packageUom);
    filterMap.put("issue_uom", issueUom);
    BasicDynaBean bean = packageUomRepository.findByKey(filterMap);
    if (bean != null) {
      return (BigDecimal) bean.get("package_size");
    }
    return null;
  }

  public List<BasicDynaBean> listDistinctPackageUom() {
    
    return packageUomRepository.listDistinctPackageUom();
  }

}
