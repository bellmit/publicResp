package com.insta.hms.core.inventory.taxation;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.taxation.ItemTaxDetails;
import com.insta.hms.common.taxation.TaxCalculator;
import com.insta.hms.common.taxation.TaxContext;
import com.insta.hms.core.inventory.procurement.PurchaseTaxCalculator;
import com.insta.hms.mdm.item.StoreItemDetailsService;
import com.insta.hms.mdm.storeitemrates.taxsubgroup.StoreTariffItemSubgroupService;
import com.insta.hms.mdm.stores.StoreService;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupService;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class TaxCalculatorSupport.
 */
public abstract class TaxCalculatorSupport {

  /** The calculators. */
  private Map<String, TaxCalculator> calculators = new HashMap<String, TaxCalculator>();

  /** The field alias. */
  private Map<String, String> fieldAlias = new HashMap<String, String>();

  /** The item service. */
  private StoreItemDetailsService itemService;

  /** The item sub group service. */
  private TaxSubGroupService itemSubGroupService;
  
  @LazyAutowired
  private StoreService storeService;
  
  @LazyAutowired StoreTariffItemSubgroupService storeTariffItemSubgroupService;

  /**
   * Instantiates a new tax calculator support.
   *
   * @param fieldAlias the field alias
   */
  public TaxCalculatorSupport(Map<String, String> fieldAlias) {
    this.fieldAlias = fieldAlias;
  }

  /**
   * Sets the tax calculators.
   *
   * @param calculators the new tax calculators
   */
  public void setTaxCalculators(List<TaxCalculator> calculators) {
    for (TaxCalculator calculator : calculators) {
      if (calculator instanceof PurchaseTaxCalculator) {
        String[] supportedGroups = ((PurchaseTaxCalculator) calculator).getSupportedGroups();
        for (String group : supportedGroups) {
          this.calculators.put(group, calculator);
        }
      }
    }
  }

  /**
   * Sets the store item details service.
   *
   * @param itemService the new store item details service
   */
  public void setStoreItemDetailsService(StoreItemDetailsService itemService) {
    this.itemService = itemService;
  }

  /**
   * Sets the item sub group service.
   *
   * @param itemSubGroupService the new item sub group service
   */
  public void setItemSubGroupService(TaxSubGroupService itemSubGroupService) {
    this.itemSubGroupService = itemSubGroupService;
  }

  /**
   * Gets the tax details.
   *
   * @param mainBean the main bean
   * @param detailBean the detail bean
   * @param invoiceBean the invoice bean
   * @param subGroupOverrides the sub group overrides
   * @return the tax details
   * @throws Exception the exception
   */
  public Map<String, Object> getTaxDetails(BasicDynaBean mainBean, BasicDynaBean detailBean,
      BasicDynaBean invoiceBean, Integer[] subGroupOverrides) throws Exception {
    List<Map<Integer, Object>> allTaxesList = new ArrayList<Map<Integer, Object>>();
    Map<String, Object> result = new HashMap<String, Object>();
    if (null != detailBean.get("medicine_id")) {
      // Get list subgroups
      List<BasicDynaBean> subGroups = new ArrayList<BasicDynaBean>();
      // TODO: This is surely not done. Clean it up when it is time.
      if (null != subGroupOverrides && subGroupOverrides.length > 0) {
        Map<String, Object[]> filter = new HashMap<String, Object[]>();
        filter.put("item_subgroup_id", subGroupOverrides);
        subGroups = itemSubGroupService.getSubGroups(filter);
      } else {
        BasicDynaBean storeBean = storeService.findByStore((int)mainBean.get("store_id"));
        Object storeRatePlanId = storeBean.get("store_rate_plan_id");
        if (null != storeRatePlanId) {
          BasicDynaBean storeTariffBean = storeTariffItemSubgroupService
              .findByKey((Integer) detailBean.get("medicine_id"), (Integer) storeRatePlanId);
          if (null != storeTariffBean) {
            subGroups = itemService.getStoreTariffSubgroups((Integer) detailBean.get("medicine_id"),
                (int) storeRatePlanId);
          } else {
            subGroups = itemService.getSubgroups((Integer) detailBean.get("medicine_id"));
          }
        } else {
          subGroups = itemService.getSubgroups((Integer) detailBean.get("medicine_id"));
        }
        
      }
      ItemTaxDetails itemTaxDetails = getTaxBean(mainBean, detailBean);
      // Get tax Context
      TaxContext taxContext = getTaxContext(mainBean, invoiceBean, subGroups);
      for (BasicDynaBean subGroup : subGroups) {
        TaxCalculator calculator = getTaxCalculator((String) subGroup.get("group_code"));
        if (null != calculator) {
          itemTaxDetails.setSugbroupId((Integer) subGroup.get("item_subgroup_id"));
          Map<Integer, Object> taxMap = calculator.calculateTaxes(itemTaxDetails, taxContext);
          allTaxesList.add(taxMap);
        }
      }
      result.put("medicine_id", detailBean.get("medicine_id"));
      result.put("net_amount", itemTaxDetails.getNetAmount());
      result.put("discount_amount", itemTaxDetails.getDiscount());
      result.put("adj_price", null != itemTaxDetails.getAdjPrice() ? itemTaxDetails.getAdjPrice()
          : itemTaxDetails.getMrp());
      result.put("tax_details", allTaxesList);
    }

    return result;
  }
  
  
  /**
   * Gets the tax details on change of tax-sub group.
   * used in stock entry screen for removing the mapped tax for an item.
   *
   * @param mainBean the main bean
   * @param detailBean the detail bean
   * @param invoiceBean the invoice bean
   * @param subGroupOverrides the sub group overrides
   * @return the tax details
   * @throws Exception the exception
   */
  public Map<String, Object> onChangeTaxDetails(BasicDynaBean mainBean, BasicDynaBean detailBean,
      BasicDynaBean invoiceBean, Integer[] subGroupOverrides) throws Exception {
    List<Map<Integer, Object>> allTaxesList = new ArrayList<>();
    Map<String, Object> result = new HashMap<>();
    if (null != detailBean.get("medicine_id")) {
      // Get list subgroups
      List<BasicDynaBean> subGroups = new ArrayList<>();
      if (null != subGroupOverrides && subGroupOverrides.length > 0) {
        Map<String, Object[]> filter = new HashMap<>();
        filter.put("item_subgroup_id", subGroupOverrides);
        subGroups = itemSubGroupService.getSubGroups(filter);
      }
      ItemTaxDetails itemTaxDetails = getTaxBean(mainBean, detailBean);
      // Get tax Context
      TaxContext taxContext = getTaxContext(mainBean, invoiceBean, subGroups);
      for (BasicDynaBean subGroup : subGroups) {
        TaxCalculator calculator = getTaxCalculator((String) subGroup.get("group_code"));
        if (null != calculator) {
          itemTaxDetails.setSugbroupId((Integer) subGroup.get("item_subgroup_id"));
          Map<Integer, Object> taxMap = calculator.calculateTaxes(itemTaxDetails, taxContext);
          allTaxesList.add(taxMap);
        }
      }
      result.put("medicine_id", detailBean.get("medicine_id"));
      result.put("net_amount", itemTaxDetails.getNetAmount());
      result.put("discount_amount", itemTaxDetails.getDiscount());
      result.put("adj_price", null != itemTaxDetails.getAdjPrice() ? itemTaxDetails.getAdjPrice()
          : itemTaxDetails.getMrp());
      result.put("tax_details", allTaxesList);
    }

    return result;
  }

  /**
   * Gets the tax calculator.
   *
   * @param groupCode the group code
   * @return the tax calculator
   */
  public TaxCalculator getTaxCalculator(String groupCode) {
    if (null == groupCode || null == calculators || calculators.isEmpty()) {
      return null;
    }
    return calculators.get(groupCode.trim().toUpperCase());
  }

  /**
   * Gets the tax bean.
   *
   * @param mainBean the main bean
   * @param detailBean the detail bean
   * @return the tax bean
   */
  protected ItemTaxDetails getTaxBean(BasicDynaBean mainBean, BasicDynaBean detailBean) {
    Map beanMap = detailBean.getMap();
    ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
    itemTaxDetails.setCostPrice((BigDecimal) beanMap.get(getMappedKey("cost_price")));
    itemTaxDetails.setMrp((BigDecimal) beanMap.get(getMappedKey("mrp")));
    itemTaxDetails.setQty((BigDecimal) beanMap.get(getMappedKey("qty")));
    itemTaxDetails.setBonusQty((BigDecimal) beanMap.get(getMappedKey("bonus_qty")));
    itemTaxDetails.setPkgSize((BigDecimal) beanMap.get(getMappedKey("pkg_size")));
    if (beanMap.get(getMappedKey("scheme_discount")) != null
        && beanMap.get(getMappedKey("discount")) != null) {
      itemTaxDetails.setDiscount(((BigDecimal) beanMap.get(getMappedKey("discount")))
          .add((BigDecimal) beanMap.get(getMappedKey("scheme_discount"))));
    } else {
      itemTaxDetails.setDiscount((BigDecimal) beanMap.get(getMappedKey("discount")));
    }
    itemTaxDetails.setDiscountPercent((BigDecimal) beanMap.get(getMappedKey("discount_per")));
    itemTaxDetails.setTaxBasis((String) beanMap.get(getMappedKey("tax_basis")));
    itemTaxDetails.setAdjMrp((BigDecimal) beanMap.get(getMappedKey("adj_mrp")));
    Map mainBeanMap = mainBean.getMap();
    itemTaxDetails.setQtyUom((String) mainBeanMap.get(getMappedKey("qty_uom")));
    return itemTaxDetails;
  }

  /**
   * Gets the mapped key.
   *
   * @param beanField the bean field
   * @return the mapped key
   */
  private String getMappedKey(String beanField) {
    String key = fieldAlias.get(beanField);
    return (null == key) ? beanField : key;
  }

  /**
   * Gets the tax context.
   *
   * @param grnMain the grn main
   * @param invoiceBean the invoice bean
   * @param subGroups the sub groups
   * @return the tax context
   */
  protected abstract TaxContext getTaxContext(BasicDynaBean grnMain, BasicDynaBean invoiceBean,
      List<BasicDynaBean> subGroups);
}
