package com.insta.hms.core.inventory.procurement;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.taxation.ItemTaxDetails;
import com.insta.hms.common.taxation.TaxCalculator;
import com.insta.hms.common.taxation.TaxContext;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.item.StoreItemDetailsService;
import com.insta.hms.mdm.storeitemrates.taxsubgroup.StoreTariffItemSubgroupService;
import com.insta.hms.mdm.stores.StoreService;
import com.insta.hms.mdm.supplier.SupplierService;
import com.insta.hms.mdm.taxgroups.TaxGroupService;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author irshadmohammed
 *
 */
@Service
public class PurchaseOrderService {
	
	static Logger logger = LoggerFactory.getLogger(PurchaseOrderService.class);

	Map<String, TaxCalculator> calculators = new HashMap<String, TaxCalculator>();
	
	/** The PO main repository. */
	@LazyAutowired PurchaseOrderMainRepository  purchaseOrderMainRepository;
	
	/** The PO details repository. */
	@LazyAutowired PurchaseOrderDetailsRepository  purchaseOrderDetailsRepository;
	
	/** The PO tax details repository. */
	@LazyAutowired PurchaseOrderTaxDetailsRepository purchaseOrderTaxDetailsRepository;
	
	/** The GenericPreferences details service. */
	@LazyAutowired GenericPreferencesService genericPreferencesService;
	
/*	
	@LazyAutowired
	private PurchaseTaxCalculator purchaseTaxCalculator;
*/	
	@LazyAutowired
	private StoreService storeService;
	
	@LazyAutowired
	private CenterService centerService;
	
	@LazyAutowired
	private SupplierService supplierService;
	
	@LazyAutowired
	private StoreItemDetailsService storeItemDetailsService;
	
	@LazyAutowired
  TaxGroupService itemGroupService;

  @LazyAutowired
  TaxSubGroupService itemSubGroupService;
  
  @LazyAutowired 
  StoreTariffItemSubgroupService storeTariffItemSubgroupService;
	
	private static final Map<String, String> PO_DETAIL_FIELD_ALIAS_MAP = new HashMap<String, String>();
	private static final Map<String, String> REORDER_PO_DETAIL_FIELD_ALIAS_MAP = new HashMap<String, String>();
	private static final Map<String, String> PO_MAIN_FIELD_ALIAS_MAP = new HashMap<String, String>();
	
	static {
		PO_DETAIL_FIELD_ALIAS_MAP.put("cost_price", "cost_price_display");
		PO_DETAIL_FIELD_ALIAS_MAP.put("mrp", "mrp_display");
		PO_DETAIL_FIELD_ALIAS_MAP.put("qty_req", "qty_req_display");
		PO_DETAIL_FIELD_ALIAS_MAP.put("bonus_qty_req", "bonus_qty_req_display");
		PO_DETAIL_FIELD_ALIAS_MAP.put("po_pkg_size", "po_pkg_size");
		PO_DETAIL_FIELD_ALIAS_MAP.put("vat_type", "vat_type");
		PO_DETAIL_FIELD_ALIAS_MAP.put("discount_per", "discount_per");

		REORDER_PO_DETAIL_FIELD_ALIAS_MAP.put("qty_req", "qty");
		//REORDER_PO_DETAIL_FIELD_ALIAS_MAP.put("bonus_qty_req", "bonus_qty_req_display");
		REORDER_PO_DETAIL_FIELD_ALIAS_MAP.put("po_pkg_size", "issue_base_unit");
		REORDER_PO_DETAIL_FIELD_ALIAS_MAP.put("vat_type", "tax_type");
		//REORDER_PO_DETAIL_FIELD_ALIAS_MAP.put("discount_per", "discount");

		PO_MAIN_FIELD_ALIAS_MAP.put("store_id", "store_id_hid");
		PO_MAIN_FIELD_ALIAS_MAP.put("supplier_id", "supplier_code_hid");
		PO_MAIN_FIELD_ALIAS_MAP.put("po_qty_unit", "store_package_uom");
	}

	
	/**
	 * This method is used to get the tax details for direct po flow.
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	
	@Autowired
	public void setTaxCalculators(List<TaxCalculator> calculators) {
		for (TaxCalculator calculator : calculators) {
			if (calculator instanceof PurchaseTaxCalculator) {
				String[] supportedGroups = ((PurchaseTaxCalculator)calculator).getSupportedGroups();
				for (String group : supportedGroups) {
					this.calculators.put(group, calculator);
				}

			}
		}
	}
	
	public List<Map<String, Object>> getTaxDetails(BasicDynaBean poMain, List<BasicDynaBean> poDetailList) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
		if (null == poDetailList) {
			return resultList;
		}
		for (BasicDynaBean poDetail : poDetailList) {
			Map<String, Object> itemMap = getTaxDetails(poMain, poDetail);
			resultList.add(itemMap);
		}
		return resultList;
	}
	
	public Map<String, Object> getTaxDetails(BasicDynaBean poMain, BasicDynaBean poDetails) throws Exception {
		List<Map<Integer, Object>> allTaxesList = new ArrayList<Map<Integer, Object>>();
		Map<String, Object> result = new HashMap<String, Object>();
		List<BasicDynaBean> subGroups = new ArrayList<>();
		if(null != poDetails.get("medicine_id")) {
			//Get list subgroups
		  BasicDynaBean storeBean = storeService.findByStore((int)poMain.get("store_id"));
      Object storeRatePlanId = storeBean.get("store_rate_plan_id");
      if (null != storeRatePlanId) {
        BasicDynaBean storeTariffBean = storeTariffItemSubgroupService
            .findByKey((Integer) poDetails.get("medicine_id"), (Integer) storeRatePlanId);
        if (null != storeTariffBean) {
          subGroups = storeItemDetailsService.getStoreTariffSubgroups((Integer) poDetails.get("medicine_id"), (int) storeRatePlanId);
        } else {
          subGroups = storeItemDetailsService.getSubgroups((Integer) poDetails.get("medicine_id"));
        }
      } else {
        subGroups = storeItemDetailsService.getSubgroups((Integer)poDetails.get("medicine_id"));
      }
			ItemTaxDetails itemTaxDetails = getTaxBean(poDetails, poMain);
			//Get tax Context
			TaxContext taxContext = getTaxContext(poMain, subGroups);
			for (BasicDynaBean subGroup : subGroups) {
				TaxCalculator calculator = getTaxCalculator((String)subGroup.get("group_code"));
				if (null != calculator) {
					itemTaxDetails.setSugbroupId((Integer)subGroup.get("item_subgroup_id"));
					Map<Integer, Object> taxMap = calculator.calculateTaxes(itemTaxDetails, taxContext);
				 	allTaxesList.add(taxMap);
				}
			}
			result.put("medicine_id", poDetails.get("medicine_id"));
			result.put("net_amount", itemTaxDetails.getNetAmount());
			result.put("discount_amount", itemTaxDetails.getDiscount());
			result.put("adj_price", null!=itemTaxDetails.getAdjPrice()?itemTaxDetails.getAdjPrice():itemTaxDetails.getMrp());
			result.put("tax_details", allTaxesList);
		}
		
		return result;
	}
	
  public Map<String, Object> onChangeTaxDetails(BasicDynaBean poMain, BasicDynaBean poDetails,
      Integer[] subGroupOverrides) throws Exception {
    List<Map<Integer, Object>> allTaxesList = new ArrayList<>();
    Map<String, Object> result = new HashMap<>();
    if (null != poDetails.get("medicine_id")) {
      // Get list subgroups
      List<BasicDynaBean> subGroups = new ArrayList<>();
      if (null != subGroupOverrides && subGroupOverrides.length > 0) {
        Map<String, Object[]> filter = new HashMap<>();
        filter.put("item_subgroup_id", subGroupOverrides);
        subGroups = itemSubGroupService.getSubGroups(filter);
      }

      ItemTaxDetails itemTaxDetails = getTaxBean(poDetails, poMain);
      // Get tax Context
      TaxContext taxContext = getTaxContext(poMain, subGroups);
      for (BasicDynaBean subGroup : subGroups) {
        TaxCalculator calculator = getTaxCalculator((String) subGroup.get("group_code"));
        if (null != calculator) {
          itemTaxDetails.setSugbroupId((Integer) subGroup.get("item_subgroup_id"));
          Map<Integer, Object> taxMap = calculator.calculateTaxes(itemTaxDetails, taxContext);
          allTaxesList.add(taxMap);
        }
      }
      result.put("medicine_id", poDetails.get("medicine_id"));
      result.put("net_amount", itemTaxDetails.getNetAmount());
      result.put("discount_amount", itemTaxDetails.getDiscount());
      result.put("adj_price", null != itemTaxDetails.getAdjPrice() ? itemTaxDetails.getAdjPrice()
          : itemTaxDetails.getMrp());
      result.put("tax_details", allTaxesList);
    }

    return result;
  }
	
	public TaxCalculator getTaxCalculator(String groupCode) {
		if (null == groupCode || null == calculators || calculators.isEmpty()) {
			return null;
		}
		return calculators.get(groupCode.trim().toUpperCase());
	}

	/*
	@Transactional(rollbackFor = Exception.class)
	public boolean copyPO(HttpServletRequest req, ModelAndView modelAndView) throws Exception {
		String username = (String) req.getSession(false).getAttribute("userid");
		boolean success = false;
		String origPoNo = req.getParameter("po_no");
		int storeId = Integer.parseInt(req.getParameter("store_id"));
		BasicDynaBean genPrefs = genericPreferencesService.getAllPreferences();
		BigDecimal poapplimit = (BigDecimal)genPrefs.get("po_approval_reqd_more_than_amt");
		int allowDecimalForQty = (Integer)genPrefs.get("after_decimal_digits");
		BasicDynaBean poBean = purchaseOrderMainRepository.findByKey("po_no", origPoNo);
		String orgSupplierId = (String)poBean.get("supplier_id");
		String newSupplierId = req.getParameterMap().get("supplier_id")[0];
		
		//after copy po, PO is like a new PO.No need to carry forward Amend related info of existing PO.  
		eraseAmendDetailsForCopyPO(poBean);
		poBean.set("amended_reason","");
		poBean.set("approved_time", null);
		poBean.set("validated_by", "");
		poBean.set("validated_time", null);
		String newPoNo = PurchaseOrderMainRepository.getNextId("" + storeId);
		ConversionUtils.copyToDynaBean(req.getParameterMap(), poBean, null, true);
		// following not to be copied
		poBean.set("po_no", newPoNo);
		poBean.set("actual_po_date", DateUtil.getCurrentTimestamp());
		poBean.set("user_id", username);

		success = purchaseOrderMainRepository.insert(poBean) > 0;
		PurchaseOrderMainRepository.copyPoDetail(origPoNo, newPoNo, storeId, allowDecimalForQty);
		
		if(orgSupplierId.equals(newSupplierId)) {
			//If both supplier are same then copy the same taxes.
			PurchaseOrderMainRepository.copyPoTaxDetail(origPoNo, newPoNo);
		} else {
			//If both supplier are different then generate new taxes.
			List<BasicDynaBean> poDetailsBeans = getPODetails(newPoNo);
			
			TaxContext taxContext = new TaxContext();
			
			Map<String, Object> storeMap = new HashMap<String, Object>();
			storeMap.put("dept_id", storeId);
			
			BasicDynaBean storeBean = storeService.findByPK(storeMap);
			
			taxContext.setCenterBean(centerService.getCenterDetails((Integer)storeBean.get("center_id")));
			
			if(null != newSupplierId && !newSupplierId.isEmpty()) {
				Map<String, Object> supplierCodeMap = new HashMap<String, Object>();
				supplierCodeMap.put("supplier_code", newSupplierId);
				
				taxContext.setSupplierBean(supplierService.findByPK(supplierCodeMap));
			}
			StoresHelper storesHelper = new StoresHelper();
			Iterator<BasicDynaBean> poDetailsBeansIterator = poDetailsBeans.iterator();
			while(poDetailsBeansIterator.hasNext()) {
				BasicDynaBean poDetails = poDetailsBeansIterator.next();
				// Used to get the Item sub group codes.
				List<BasicDynaBean> subGroupCodes = itemService.getSubgroups((Integer)poDetails.get("medicine_id"));
				List<BasicDynaBean> taxRates = purchaseTaxCalculator.getTaxRates(subGroupCodes, taxContext);
				BigDecimal totalTaxRate = BigDecimal.ZERO;
				BigDecimal mrp = (BigDecimal)poDetails.get("mrp");
				Iterator<BasicDynaBean> taxRatesIterator = taxRates.iterator();
				while(taxRatesIterator.hasNext()) {
					BasicDynaBean taxRateBean = taxRatesIterator.next();
					BigDecimal taxRate = (BigDecimal)taxRateBean.get("tax_rate");
					totalTaxRate = totalTaxRate.add(taxRate);
				}
				BigDecimal adjMrp = mrp.divide(BigDecimal.ONE.add((totalTaxRate.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_DOWN))), 2, RoundingMode.HALF_DOWN);
				BigDecimal qty = (BigDecimal)poDetails.get("qty_req");
				BigDecimal pkgSize = (BigDecimal)poDetails.get("po_pkg_size");
				ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
				itemTaxDetails.setCostPrice((BigDecimal)poDetails.get("cost_price"));
				itemTaxDetails.setMrp((BigDecimal)poDetails.get("mrp"));
				itemTaxDetails.setQty(qty.divide(pkgSize));
				itemTaxDetails.setBonusQty((BigDecimal)poDetails.get("bonus_qty_req"));
				itemTaxDetails.setPkgSize(pkgSize);
				itemTaxDetails.setDiscount((BigDecimal)poDetails.get("discount"));
				itemTaxDetails.setTaxBasis((String)poDetails.get("vat_type"));
				itemTaxDetails.setAdjMrp(adjMrp);
				
				Map<String, Object> itemMap = new HashMap<String, Object>();
				//Purchase tax amount.
				// TODO : Use the refactored version of calculateTaxes
				// storesHelper.getTaxDetails(purchaseTaxCalculator.calculateTaxes(itemTaxDetails, subGroupCodes, taxContext), subGroupCodes, itemMap);
				success = purchaseOrderTaxDetailsRepository.updateTaxDetails(itemMap, newPoNo, (Integer)poDetails.get("medicine_id"), poDetails);
				if(!success)
					break;
			}
			modelAndView.getModelMap().addAttribute("poNo", newPoNo);
			// TODO: Should be able to achieve this with ValidationException - review
			// if(!success)
				//throw new DataManipulationException();
		}
		
		
		// 
		//  In case selected store has different store tariff
		//  vat,vat_rate,vat_type can be different recalculating po_total
		// 
		List<BasicDynaBean> copiedPODetails = purchaseOrderDetailsRepository.listAll(null, "po_no", newPoNo);
		BigDecimal poTotal = BigDecimal.ZERO;
		for(BasicDynaBean poItemBean : copiedPODetails) {
			poTotal = poTotal.add((BigDecimal)poItemBean.get("med_total"));
		}
		poBean.set("po_total", poTotal);
		
		//status to be set based on po approval limit
		if ( ((BigDecimal)poBean.get("po_total")).compareTo(poapplimit) > 0) {
			poBean.set("status", "O");
			poBean.set("approver_remarks", "");
			poBean.set("approved_by", null);//approved by is not valid for an open PO
		} else {
			poBean.set("status", "A");
			poBean.set("approver_remarks", "Automatic PO Approval");
		}
		Map<String, Object> keyMap = new HashMap<String, Object>();
		keyMap.put("po_no", newPoNo);
		success = purchaseOrderMainRepository.update(poBean, keyMap) > 0;
		
		//update all po items status to main status
		success = purchaseOrderDetailsRepository.updatePOItemsStatus(newPoNo, (String)poBean.get("status"));
		modelAndView.addObject("poNo", newPoNo);
		return success;
	}
	*/
	/**
	 * Just a helper method to cleanup the amendment details.
	 * 
	 * @param poBean
	 */
	/*
	void eraseAmendDetailsForCopyPO(BasicDynaBean poBean){
		poBean.set("amendment_time", null);
		poBean.set("amendment_validated_time",null);
		poBean.set("amendment_approved_time", null);
		poBean.set("amended_by","");
		poBean.set("amendment_validated_by","");
		poBean.set("amendment_approved_by","");
		poBean.set("amendment_validator_remarks","");
		poBean.set("amendment_approver_remarks","");
	}
	*/
	/*
	public List<BasicDynaBean> getAllSuppliers(int centerId) {
		return supplierService.getAllSuppliers(centerId);
	}
	*/
	/*
	public BasicDynaBean getStoreDetails(int storeId) {
		Map<String, Object> filterMap = new HashMap<String, Object>();
		filterMap.put("dept_id", storeId);
		return storeService.findByPK(filterMap);
	}
	
	public BasicDynaBean getPOMain(String poNo) {
		Map<String, Object> filterMap = new HashMap<String, Object>();
		filterMap.put("po_no", poNo);
		return purchaseOrderMainRepository.findByKey(filterMap);
	}
	
	public List<BasicDynaBean> getPODetails(String poNo) {
		return purchaseOrderDetailsRepository.listAll(null, "po_no", poNo);
	}
	
	public BasicDynaBean getSuplierDetails(String supplierId) {
		Map<String, Object> filterMap = new HashMap<String, Object>();
		filterMap.put("supplier_code", supplierId);
		return supplierService.findByPK(filterMap);
	}
	*/
	/**
	 * This method is used to get the context of tax calculator.
	 * 
	 * @param storeIdStr
	 * @param supplierId
	 * @return
	 */
	protected TaxContext getTaxContext(BasicDynaBean poMainBean, List<BasicDynaBean> subGroups) {
		TaxContext taxContext = new TaxContext();
		
		if(poMainBean.get("store_id") != null) {
			int storeId = (Integer)poMainBean.get("store_id");
			
			Map<String, Object> storeMap = new HashMap<String, Object>();
			storeMap.put("dept_id", storeId);
			
			BasicDynaBean storeBean = storeService.findByPk(storeMap);
			
			taxContext.setCenterBean(centerService.getCenterDetails((Integer)storeBean.get("center_id")));
		}
		
		if(poMainBean.get("supplier_id") != null) {
			String supplierCode = (String)poMainBean.get("supplier_id");
			
			Map<String, Object> supplierCodeMap = new HashMap<String, Object>();
			supplierCodeMap.put("supplier_code", supplierCode);
			
			taxContext.setSupplierBean(supplierService.findByPk(supplierCodeMap));
		}
		
		taxContext.setSubgroups(subGroups);
		return taxContext;
	}
	
	/**
	 * This method is used to set the tax details and return it as a bean.
	 * 
	 * 
	 * @return
	 */
	protected ItemTaxDetails getTaxBean(BasicDynaBean poDetails, BasicDynaBean poMain) {
		BigDecimal costPrice = BigDecimal.ZERO;
		BigDecimal mrp = BigDecimal.ZERO;
		BigDecimal qty = BigDecimal.ZERO;
		BigDecimal bonusQty = BigDecimal.ZERO;
		BigDecimal pkgSize = BigDecimal.ZERO;
		BigDecimal discountPer = BigDecimal.ZERO;
		BigDecimal discount = BigDecimal.ZERO;
		BigDecimal adjMrp = BigDecimal.ZERO;
		String taxBasis = "MB";
		String quantityUOM = "P";

		if(poDetails.get("cost_price") != null) {
			costPrice = (BigDecimal)poDetails.get("cost_price");
		}
		if(poDetails.get("mrp") != null) {
			mrp = (BigDecimal)poDetails.get("mrp");
		}
		if(poDetails.get("qty_req") != null) {
			qty = (BigDecimal)poDetails.get("qty_req");
		}
		if(poDetails.get("bonus_qty_req") != null) {
			bonusQty = (BigDecimal)poDetails.get("bonus_qty_req");
		}
		if(poDetails.get("po_pkg_size") != null) {
			pkgSize = (BigDecimal)poDetails.get("po_pkg_size");
		}
		if(poDetails.get("discount_per") != null) {
			discountPer = (BigDecimal)poDetails.get("discount_per");
		}
		if(poDetails.get("discount") != null) {
			discount = (BigDecimal)poDetails.get("discount");
		}
		if(poDetails.get("vat_type") != null) {
			taxBasis = (String)poDetails.get("vat_type");
		}
		if(poMain.get("po_qty_unit") != null) {
			quantityUOM = (String)poMain.get("po_qty_unit");
		}
		
		ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
		itemTaxDetails.setCostPrice(costPrice);
		itemTaxDetails.setMrp(mrp);
		itemTaxDetails.setQty(qty);
		itemTaxDetails.setBonusQty(bonusQty);
		itemTaxDetails.setPkgSize(pkgSize);
		if(poDetails.get("discount") != null)
			itemTaxDetails.setDiscount(discount);
		else
			itemTaxDetails.setDiscountPercent(discountPer);
		itemTaxDetails.setTaxBasis(taxBasis);
		itemTaxDetails.setAdjMrp(adjMrp);
		itemTaxDetails.setQtyUom(quantityUOM);
		return itemTaxDetails;
	}
	
	public BasicDynaBean toDetailsBean(Map<String, String[]> reqMap) {
		
		BasicDynaBean poBean = purchaseOrderDetailsRepository.getBean();
		ConversionUtils.copyToDynaBean(reqMap, poBean, null, true, null, PO_DETAIL_FIELD_ALIAS_MAP);
		/*
		if(StringUtil.isNotEmpty(reqMap.get("cost_price_display"))) {
			field = new BigDecimal(reqMap.get("cost_price_display"));
			poBean.set("cost_price", field);
		}
		if(StringUtil.isNotEmpty(reqMap.get("mrp_display"))) {
			field = new BigDecimal(reqMap.get("mrp_display"));
			poBean.set("mrp", field);
		}
		if(StringUtil.isNotEmpty(reqMap.get("qty_req_display"))) {
			field = new BigDecimal(reqMap.get("qty_req_display"));
			poBean.set("qty_req", field);
		}
		if(StringUtil.isNotEmpty(reqMap.get("qty"))) {
			field = new BigDecimal(reqMap.get("qty"));
			poBean.set("qty_req", field);
		}
		if(StringUtil.isNotEmpty(reqMap.get("bonus_qty_req_display"))) {
			field = new BigDecimal(reqMap.get("bonus_qty_req_display"));
			poBean.set("bonus_qty_req", field);
		}
		if(StringUtil.isNotEmpty(reqMap.get("issue_base_unit"))) {
			field = new BigDecimal(reqMap.get("issue_base_unit"));
			poBean.set("po_pkg_size", field);
		}
		if(StringUtil.isNotEmpty(reqMap.get("tax_type"))) {
			fieldStr = reqMap.get("tax_type");
			poBean.set("vat_type", fieldStr);
		} */
		return poBean;
	}
	
	public BasicDynaBean toMainBean(Map<String, String[]> reqMap) {
		BasicDynaBean poMainBean = purchaseOrderMainRepository.getBean();
		ConversionUtils.copyToDynaBean(reqMap, poMainBean, null, true, null, PO_MAIN_FIELD_ALIAS_MAP);
		/*
		if(StringUtil.isNotEmpty(reqMap.get("store_id_hid"))) {
			poMainBean.set("store_id", Integer.valueOf(reqMap.get("store_id_hid")));
		}
		if(StringUtil.isNotEmpty(reqMap.get("supplier_code_hid"))) {
			poMainBean.set("supplier_id", reqMap.get("supplier_code_hid"));
		}
		if(StringUtil.isNotEmpty(reqMap.get("supplier_code"))) {
			poMainBean.set("supplier_id", reqMap.get("supplier_code"));
		} */
		
		return poMainBean;
	}

	public List<BasicDynaBean> toDetailsBeanList(Map<String, String[]> reqMap) {
		List<BasicDynaBean> beanList = new ArrayList<BasicDynaBean>();
		String[] medicines = reqMap.get("medicine_id");
		if (null != medicines && medicines.length > 0) {
			for (int i = 0; i < medicines.length; i++) {
				BasicDynaBean bean = purchaseOrderDetailsRepository.getBean();
				ConversionUtils.copyIndexToDynaBean(reqMap, i, bean, null, false, REORDER_PO_DETAIL_FIELD_ALIAS_MAP);
				beanList.add(bean);
			}
		}
		return beanList;
	}
	
  public Map getSupportedTaxGroups() {
    String[] groupCodes = null;
    Map groupMap = new HashMap();
    Map subGroupMap = new HashMap();
    Map result = new HashMap();
    if (null != calculators && calculators.size() > 0) {
      groupCodes = calculators.keySet().toArray(new String[0]);
      if (null != groupCodes) {
        List<BasicDynaBean> groups = itemGroupService.findByGroupCodes(groupCodes);
        groupMap = ConversionUtils.listBeanToMapMap(groups, "item_group_id");
        for (BasicDynaBean group : groups) {
          Integer groupId = (Integer) group.get("item_group_id");
          Map m = new HashMap();
          m.put("item_group_id", groupId);
          List<BasicDynaBean> l = itemSubGroupService.findByCriteria(m);
          Map subgroups = (null != l && l.size() > 0)
              ? ConversionUtils.listBeanToMapListMap(l, "item_group_id") : emptyMap(groupId);
          subGroupMap.putAll(subgroups);
        }
      }
    }
    result.put("item_groups", groupMap);
    result.put("item_subgroups", subGroupMap);
    return result;
  }

  private Map emptyMap(Integer itemGroupId) {
    Map m = new HashMap();
    m.put(itemGroupId, Collections.EMPTY_MAP);
    return m;
  }
}
