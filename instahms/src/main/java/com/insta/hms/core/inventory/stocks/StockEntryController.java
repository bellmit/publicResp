package com.insta.hms.core.inventory.stocks;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.inventory.InventoryController;
import com.insta.hms.core.inventory.URLRoute;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import flexjson.JSONSerializer;

@Controller
@RequestMapping(URLRoute.STOCK)
public class StockEntryController extends InventoryController {

	@LazyAutowired
	private StockEntryService stockEntryService;
	
	private static final String ITEM_SUBGROUP_ID = "item_subgroup_id";
	
	@RequestMapping(value = URLRoute.STOCK_ENTRY_TAX, method = RequestMethod.POST)
	public Map<String, Object> getTaxDetails(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		
		Map<String, String[]> reqMap = request.getParameterMap();
		BasicDynaBean grnDetails = stockEntryService.toGRNDetailsBean(reqMap);
		BasicDynaBean grnMain = stockEntryService.toGRNMainBean(reqMap);
		BasicDynaBean invoiceDetails = stockEntryService.toInvoiceBean(reqMap);
		
		if(grnDetails.get("medicine_id") != null && grnMain.get("store_id")!= null && invoiceDetails.get("supplier_id")!=null) {
			responseMap = stockEntryService.getTaxDetails(grnMain, grnDetails, invoiceDetails);
		}
		
		return responseMap;
	}
	
	@RequestMapping(value = URLRoute.STOCK_ENTRY_TAX_CHANGE, method = RequestMethod.POST)
  public Map<String, Object> onChangeTaxDetails(HttpServletRequest request, HttpServletResponse response) throws Exception {
    Map<String, Object> responseMap = new HashMap<>();
    Map<String, String[]> reqMap = new HashMap<>();
    reqMap.putAll(request.getParameterMap());   
    BasicDynaBean grnDetails = stockEntryService.toGRNDetailsBean(reqMap);
    BasicDynaBean grnMain = stockEntryService.toGRNMainBean(reqMap);
    BasicDynaBean invoiceDetails = stockEntryService.toInvoiceBean(reqMap);
    Integer[] taxSubGrp = null;

    if (reqMap.get(ITEM_SUBGROUP_ID) != null) {
      String itemSubgroupId = reqMap.get(ITEM_SUBGROUP_ID)[0];
      String[] itemSubGroupIds = null;
      if (!itemSubgroupId.trim().isEmpty()) {
        itemSubGroupIds = itemSubgroupId.split("\\,");
        taxSubGrp = new Integer[itemSubGroupIds.length];
        for(int i =0; i <itemSubGroupIds.length ; i++) {
          taxSubGrp[i] = Integer.valueOf(itemSubGroupIds[i]);
        }
      }

    }
    
    if(grnDetails.get("medicine_id") != null && grnMain.get("store_id")!= null && invoiceDetails.get("supplier_id")!=null) {
      responseMap = stockEntryService.onChangeTaxDetails(grnMain, grnDetails, invoiceDetails, taxSubGrp);
    }
    
    return responseMap;
  }
	
	@RequestMapping(value = URLRoute.STOCK_DEBIT_TAX, method = RequestMethod.POST)
	public Map<String, Object> getDebitTaxDetails(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> responseMap = new HashMap<String, Object>();

		Map<String, String[]> reqMap = request.getParameterMap();
		BasicDynaBean grnDetails = stockEntryService.toGRNDetailsBean(reqMap);
		BasicDynaBean grnMain = stockEntryService.toGRNMainBean(reqMap);
		BasicDynaBean invoiceDetails = stockEntryService.toInvoiceBean(reqMap);

		String itemSubgroupId = null;
		//Integer itemSubgroupIds[] = new Integer[3];
		List<Integer> itemSubgroupIds = new ArrayList<Integer>();
		if(null != reqMap.get("item_subgroup_id") && reqMap.get("item_subgroup_id").length > 0) {
			itemSubgroupId = reqMap.get("item_subgroup_id")[0];
			if(!itemSubgroupId.trim().isEmpty() && itemSubgroupId.contains(",")){
				String[] subGroupIds = itemSubgroupId.split("\\,");
				for(String subGrp: subGroupIds) {
					if(subGrp != null && !subGrp.isEmpty() && !subGrp.equalsIgnoreCase("null")) {
						itemSubgroupIds.add(Integer.valueOf(subGrp));
					}
				}
			} else {
				if(!itemSubgroupId.trim().isEmpty())
					itemSubgroupIds.add(Integer.valueOf(itemSubgroupId));
			}
		}

		if(grnDetails.get("medicine_id") != null && grnMain.get("store_id")!= null && invoiceDetails.get("supplier_id")!=null) {
			Integer [] subgroupIdsArray = new Integer[itemSubgroupIds.size()];
			itemSubgroupIds.toArray(subgroupIdsArray);
			responseMap = stockEntryService.getDebitNoteTaxDetails(grnMain, grnDetails, invoiceDetails, itemSubgroupIds.size()>0 ?subgroupIdsArray:null);
		}
		
		return responseMap;
	}

	@IgnoreConfidentialFilters
	@RequestMapping(value = URLRoute.GET_GRN_ITEMS_AND_DETAILS, method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> getGrnItemsAndDetails(
			@RequestParam(value = "grn_no") String grnNo) throws Exception {
		return stockEntryService.getGrnItemsAndDetails(grnNo);
	}

}
