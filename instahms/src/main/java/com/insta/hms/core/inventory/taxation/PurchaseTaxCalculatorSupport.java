package com.insta.hms.core.inventory.taxation;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.taxation.TaxCalculator;
import com.insta.hms.common.taxation.TaxContext;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.item.StoreItemDetailsService;
import com.insta.hms.mdm.stores.StoreService;
import com.insta.hms.mdm.supplier.SupplierService;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PurchaseTaxCalculatorSupport extends TaxCalculatorSupport {

  private static final Map<String, String> TAX_FIELDS_ALIAS_MAP = new HashMap<String, String>();

  static {
    TAX_FIELDS_ALIAS_MAP.put("qty", "billed_qty");
    TAX_FIELDS_ALIAS_MAP.put("tax_basis", "tax_type");
    TAX_FIELDS_ALIAS_MAP.put("qty_uom", "grn_qty_unit");
    TAX_FIELDS_ALIAS_MAP.put("bonus_qty", "bonus_qty");
    TAX_FIELDS_ALIAS_MAP.put("pkg_size", "grn_pkg_size");
  }

  @LazyAutowired
  private StoreService storeService;

  @LazyAutowired
  private CenterService centerService;

  @LazyAutowired
  private SupplierService supplierService;

  @Autowired
  public void setTaxCalculators(List<TaxCalculator> calculators) {
    super.setTaxCalculators(calculators);
  }

  @Autowired
  public void setStoreItemDetailsService(StoreItemDetailsService storeItemDetailsService) {
    super.setStoreItemDetailsService(storeItemDetailsService);
  }

  @Autowired
  public void setItemSubGroupService(TaxSubGroupService itemSubGroupService) {
    super.setItemSubGroupService(itemSubGroupService);
  }

  public PurchaseTaxCalculatorSupport() {
    super(TAX_FIELDS_ALIAS_MAP);
  }

  protected TaxContext getTaxContext(BasicDynaBean grnMain, BasicDynaBean invoiceBean,
      List<BasicDynaBean> subGroups) {
    TaxContext taxContext = new TaxContext();

    if (grnMain.get("store_id") != null) {
      int storeId = (Integer) grnMain.get("store_id");

      Map<String, Object> storeMap = new HashMap<String, Object>();
      storeMap.put("dept_id", storeId);

      BasicDynaBean storeBean = storeService.findByPk(storeMap);

      taxContext
          .setCenterBean(centerService.getCenterDetails((Integer) storeBean.get("center_id")));
    }

    if (invoiceBean.get("supplier_id") != null) {
      String supplierCode = (String) invoiceBean.get("supplier_id");

      Map<String, Object> supplierCodeMap = new HashMap<String, Object>();
      supplierCodeMap.put("supplier_code", supplierCode);

      taxContext.setSupplierBean(supplierService.findByPk(supplierCodeMap));
    }

    taxContext.setSubgroups(subGroups);
    return taxContext;
  }

}
