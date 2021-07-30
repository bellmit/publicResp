package com.insta.hms.mdm.dentalsupplies;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.dentalsupplieritemrates.DentalSupplierItemRateService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class DentalSuppliesService extends MasterService {

  @LazyAutowired
  DentalSupplierItemRateService dentalSupplierItemRateService;

  public DentalSuppliesService(DentalSuppliesRepository dentalSuppliesRepository,
      DentalSuppliesValidator dentalSuppliesValidator) {
    super(dentalSuppliesRepository, dentalSuppliesValidator);
  }

  /**
   * Method is use to get List page data.
   * 
   * @return Return type is Map
   */
  public Map<String, List<BasicDynaBean>> getListPageData() {
    Map<String, List<BasicDynaBean>> map = new HashMap<>();
    map.put("itemNames", lookup(false));
    return map;
  }

  /**
   * Method is use to get Add/Edit pate data.
   * 
   * @return Return type is Map
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> map = new HashMap<>();
    map.put("itemNames", lookup(false));
    map.put("supplierItemsRateList", dentalSupplierItemRateService.lookup(true));
    return map;
  }
}
