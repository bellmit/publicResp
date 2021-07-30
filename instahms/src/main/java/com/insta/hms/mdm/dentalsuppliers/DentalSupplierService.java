package com.insta.hms.mdm.dentalsuppliers;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.dentalsupplieritemrates.DentalSupplierItemRateService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class is {@link DentalSupplierService}.
 * @author amolbagde
 *
 */
@Service
public class DentalSupplierService extends MasterService {

  @LazyAutowired
  DentalSupplierItemRateService dentalSupplierItemRateService;

  public DentalSupplierService(DentalSupplierRepository dentalSupplierRepository,
      DentalSupplierValidator dentalSupplierValidator) {
    super(dentalSupplierRepository, dentalSupplierValidator);
  }

  /**
   * Method used to get list page data.
   * @return Return type is Map.
   */
  public Map<String, List<BasicDynaBean>> getListPageData() {
    Map<String, List<BasicDynaBean>> map = new HashMap<>();
    map.put("suppliersList", lookup(false));
    return map;
  }

  /**
   * Method used to get Add/Edit page data.
   * @return Return type is Map.
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> map = new HashMap<>();
    map.put("suppliersList", lookup(false));
    map.put("supplierItemsRateList", dentalSupplierItemRateService.lookup(true));
    return map;
  }
}
