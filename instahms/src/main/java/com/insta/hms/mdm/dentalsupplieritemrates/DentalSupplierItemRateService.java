package com.insta.hms.mdm.dentalsupplieritemrates;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.dentalsuppliers.DentalSupplierService;
import com.insta.hms.mdm.dentalsupplies.DentalSuppliesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DetailSupplierItemRateService.
 * 
 * @author krishna
 *
 */
@Service
public class DentalSupplierItemRateService extends MasterService {

  @LazyAutowired
  DentalSupplierService dentalSupplierService;
  @LazyAutowired
  DentalSuppliesService dentalSuppliesService;

  public DentalSupplierItemRateService(
      DentalSupplierItemRateRepository dentalSupplierItemRateRepository,
      DentalSupplierItemRateValidator dentalSupplierItemRateValidator) {
    super(dentalSupplierItemRateRepository, dentalSupplierItemRateValidator);
  }

  /**
   * Method to get the List page data.
   * @param requestParams Method Parameter
   * @return Return type is Map
   */
  public Map<String, List<BasicDynaBean>> getListPageData(Map<String, String[]> requestParams) {
    Map<String, List<BasicDynaBean>> map = new HashMap<>();

    List<BasicDynaBean> suppliers = dentalSupplierService.lookup(false);
    List<BasicDynaBean> items = dentalSuppliesService.lookup(false);

    map.put("itemList", suppliers);
    map.put("supplierList", items);
    return map;
  }


  /**
   * Method to get the page data.
   * @param params is the Parameter of type Map.
   * @return Return type is Map.
   */
  @SuppressWarnings("rawtypes")
  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map params) {
    Map<String, List<BasicDynaBean>> referenceMap = new HashMap<>();

    List<BasicDynaBean> suppliers = dentalSupplierService.lookup(true);
    List<BasicDynaBean> items = dentalSuppliesService.lookup(true);
    List<BasicDynaBean> rateList = lookup(false);

    referenceMap.put("itemList", suppliers);
    referenceMap.put("supplierList", items);
    referenceMap.put("dentalSupplierItemRateList", rateList);

    return referenceMap;
  }
}
