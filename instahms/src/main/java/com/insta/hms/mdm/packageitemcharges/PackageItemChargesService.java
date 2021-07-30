package com.insta.hms.mdm.packageitemcharges;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Composite Primary Key.
 *
 * @author ritolia.
 */
@Service
public class PackageItemChargesService extends BaseController {

  @LazyAutowired
  private PackageItemChargesRepository packageItemChargesRepository;

  /**
   * @param packageId.
   * @param itemBean.
   * @param itemId.
   * @param bedType.
   * @param orgId.
   * @param itemType.
   * @param qty.
   * @return BigDecimal
   */
  public BigDecimal getMultiVisitPackageItemCharge(String packageId, BasicDynaBean itemBean,
      String itemId, String bedType, String orgId, String itemType, BigDecimal qty) {

    return packageItemChargesRepository.getMultiVisitPackageItemCharge(packageId, itemBean, itemId,
        bedType, orgId, itemType, qty);
  }

  public BasicDynaBean findBykey(Map<String, Object> filterMap) {
    return packageItemChargesRepository.findByKey(filterMap);
  }

  public BasicDynaBean getMultiVisitPackage(Integer packageId, Integer packObId, String bedType,
      String orgId) {
    return packageItemChargesRepository.getMultiVisitPackage(packageId, packObId, bedType, orgId);
  }
}
