package com.insta.hms.core.inventory.sales;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.inventory.InventoryController;
import com.insta.hms.core.inventory.URLRoute;
import com.insta.hms.mdm.item.StoreItemDetailsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jfree.util.Log;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Used for Sales Related Operations.
 * 
 * @author irshadmohammed
 *
 */
@RestController
@RequestMapping(URLRoute.SALES)
public class SalesController extends InventoryController {
	static Logger logger = LoggerFactory.getLogger(SalesController.class);
	
	@LazyAutowired
	private SalesService salesService;

	@LazyAutowired
	private StoreItemDetailsService itemService;

	@IgnoreConfidentialFilters
	@RequestMapping(value = URLRoute.GET_SELLING_PRICE, method = RequestMethod.GET)
	public BigDecimal getSellingPrice(HttpServletRequest request) throws Exception {
		BigDecimal sellingPrice = BigDecimal.ZERO;
		int visitStoreRatePlanId = Integer.parseInt(request.getParameter("visitStoreRatePlanId") != null ? request.getParameter("visitStoreRatePlanId") : "-1");
		int itemBatchId = Integer.parseInt(request.getParameter("itemBatchId") != null ? request.getParameter("itemBatchId") : "-1");
		int storeId = Integer.parseInt(request.getParameter("storeId"));
		BigDecimal qty = new BigDecimal(request.getParameter("qty") != null ? request.getParameter("qty") : "0");
		double mrp = Double.parseDouble(request.getParameter("mrp")!=null ? request.getParameter("mrp") : "0");
		int medicineId = Integer.parseInt(request.getParameter("medicine_id"));
		boolean isVisitStoreRatePlanId = new Boolean(request.getParameter("is_visit_store_rate_plan_id") != null ? request.getParameter("is_visit_store_rate_plan_id") : "false");
		int centerId = (Integer) request.getSession(false).getAttribute("centerId");
		sellingPrice = salesService.getSellingPrice(itemBatchId, qty, storeId, centerId, visitStoreRatePlanId, mrp, medicineId, isVisitStoreRatePlanId);
		return sellingPrice;
	}

	@RequestMapping(value = URLRoute.GET_TAX_DETAILS, method = RequestMethod.POST)
	public /*List<Map<Integer, Object>>*/ Map<String, Object> getTaxDetails(HttpServletRequest request, HttpServletResponse response) 
			throws Exception {
		// List<Map<Integer, Object>> responseMap = new ArrayList<Map<Integer, Object>>();
		Map<String, Object> responseMap = new HashMap<String, Object>();
		Map<String, String[]> reqMap = new HashMap<String, String[]>();
		reqMap.putAll(request.getParameterMap());
		if(reqMap.get("item_subgroup_id")!=null) {
			String itemSubgroupId = reqMap.get("item_subgroup_id")[0];
			String[] itemSubGroupIds = null;
			if(!itemSubgroupId.trim().isEmpty() && itemSubgroupId.contains(",")){
				itemSubGroupIds = itemSubgroupId.split("\\,");
				reqMap.put("item_subgroup_id", itemSubGroupIds);
				String[] medicineIds = new String[itemSubGroupIds.length];
				for(int i=0; i<itemSubGroupIds.length; i++) {
					medicineIds[i] = reqMap.get("medicine_id")[0];
				}
				reqMap.put("medicine_id", medicineIds);
			}
			
		} else {
			reqMap.put("item_subgroup_id", new String[0]);
		}
		BasicDynaBean salesDetailsBean = salesService.toDetailsBean(reqMap);
		BasicDynaBean billBean = salesService.toBillBean(reqMap);
		BasicDynaBean patientBean = salesService.toPatientBean(reqMap);
		BasicDynaBean tpaBean = salesService.toTPABean(reqMap);
		BasicDynaBean visitBean = salesService.toVisitBean(reqMap);
		
		BasicDynaBean parent = itemService.toBean(reqMap);
		Map<String, List<BasicDynaBean>> mappedSubgroups = itemService.toBeanList(reqMap, parent);
		List<BasicDynaBean> subGroups = null;
		if (null != mappedSubgroups) {
			subGroups = mappedSubgroups.get("store_item_sub_groups_updated");
			if (null == subGroups || subGroups.size() <= 0) {
				subGroups = mappedSubgroups.get("store_item_sub_groups_inserted");
			} if (null == subGroups || subGroups.size() <= 0) {
				subGroups = null;
				Log.debug("Sub group override not available... falling back to master");
			}
		}
		responseMap = salesService.getTaxDetails(salesDetailsBean, subGroups, billBean, patientBean, tpaBean, visitBean);
		return responseMap;
	}

	@IgnoreConfidentialFilters
	@RequestMapping(value = URLRoute.GET_SUPPORTED_TAX_GROUPS, method = RequestMethod.GET)
	public Map getSupportedTaxGroups(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map taxGroups = salesService.getSupportedTaxGroups();
		return taxGroups;
	}
	
	@RequestMapping(value = URLRoute.GET_BASE_TAX_DETAILS, method = RequestMethod.POST)
	public Map getBaseTaxCalculator(HttpServletRequest request, HttpServletResponse response) throws Exception {
		BasicDynaBean salesDetailsBean = salesService.toDetailsBean(request.getParameterMap());
		BasicDynaBean salesClaimDetailsBean = salesService.toClaimDetailsBean(request.getParameterMap());
		Map taxDetails = salesService.getBaseTaxDetails(salesDetailsBean, salesClaimDetailsBean);
		return taxDetails;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
  @RequestMapping(value = URLRoute.GET_ALL_ITEMS_TAX_DETAILS, method = RequestMethod.POST, consumes = "application/json")
  public Map<String, Object> getTaxDetailsPost(HttpServletRequest request, HttpServletResponse response,
      @RequestBody ModelMap requestBody) 
      throws Exception {
	  Map<String,Object> responseMap = new HashMap<String, Object>();
	  ArrayList taxItems = (ArrayList)requestBody.get("taxList");
    
	  for (int k = 0; k < taxItems.size(); k++) {
      Map<String, String[]> reqMap = (Map<String, String[]>) taxItems.get(k);
      Map<String, Object> itemMap = (Map<String, Object>) taxItems.get(k);
      if(reqMap.get("item_subgroup_id")!=null) {
        String itemSubgroupId = (String) itemMap.get("item_subgroup_id");
        String[] itemSubGroupIds = null;
        if(!itemSubgroupId.trim().isEmpty() && itemSubgroupId.contains(",")){        
          String [] vals = itemSubgroupId.split("\\,");
          itemSubGroupIds = new String[vals.length];
          for(int z =0; z< vals.length; z++){
            if(!vals[z].isEmpty()){
              itemSubGroupIds[z] = vals[z];
            }
          }
          reqMap.put("item_subgroup_id", itemSubGroupIds);
          String[] medicineIds = new String[itemSubGroupIds.length];
          for(int i=0; i<itemSubGroupIds.length; i++) {
            medicineIds[i] = (String) itemMap.get("medicine_id");
          }
          reqMap.put("medicine_id", medicineIds);
        }
        
      } else {
        reqMap.put("item_subgroup_id", new String[0]);
      }
      BasicDynaBean salesDetailsBean = salesService.toDetailsBean(reqMap);
      BasicDynaBean billBean = salesService.toBillBean(reqMap);
      BasicDynaBean patientBean = salesService.toPatientBean(reqMap);
      BasicDynaBean tpaBean = salesService.toTPABean(reqMap);
      BasicDynaBean visitBean = salesService.toVisitBean(reqMap);
      
      BasicDynaBean parent = itemService.toBean(reqMap);
      Map<String, List<BasicDynaBean>> mappedSubgroups = itemService.toBeanList(reqMap, parent);
      List<BasicDynaBean> subGroups = null;
      if (null != mappedSubgroups) {
        subGroups = mappedSubgroups.get("store_item_sub_groups_updated");
        if (null == subGroups || subGroups.size() <= 0) {
          subGroups = mappedSubgroups.get("store_item_sub_groups_inserted");
        } if (null == subGroups || subGroups.size() <= 0) {
          subGroups = null;
          Log.debug("Sub group override not available... falling back to master");
        }   
      } 
      Map<String, Object> taxMap = new HashMap<String, Object>();
      taxMap = salesService.getTaxDetails(salesDetailsBean, subGroups, billBean, patientBean, tpaBean, visitBean);
      
      if(null != salesDetailsBean) {
        responseMap.put(String.valueOf(salesDetailsBean.get("item_batch_id")), taxMap);
      }
    }  
    return responseMap;
  }
}
