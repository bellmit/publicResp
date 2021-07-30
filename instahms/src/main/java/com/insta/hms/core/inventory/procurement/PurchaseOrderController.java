package com.insta.hms.core.inventory.procurement;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.inventory.InventoryController;
import com.insta.hms.core.inventory.URLRoute;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author irshadmohammed
 *
 */
@Controller
@RequestMapping(URLRoute.PO)
public class PurchaseOrderController extends InventoryController {
	
	static Logger logger = LoggerFactory.getLogger(PurchaseOrderController.class);
	
	private static final String ITEM_SUBGROUP_ID = "item_subgroup_id";
	
	@LazyAutowired
	private PurchaseOrderService purchaseOrderService;
	
/*	@LazyAutowired
	private MessageUtil messageUtil;
*/	
	@RequestMapping(value = URLRoute.PO_TAX, method = RequestMethod.POST)
	public Map<String, Object> getTaxDetails(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		
		Map<String, String[]> reqMap = ConversionUtils.flatten(request.getParameterMap());
		BasicDynaBean poBean = purchaseOrderService.toDetailsBean(reqMap);
		BasicDynaBean poMainBean = purchaseOrderService.toMainBean(reqMap);
		
		if(poBean.get("medicine_id") != null && poMainBean.get("store_id")!= null && poMainBean.get("supplier_id")!=null)
			responseMap = purchaseOrderService.getTaxDetails(poMainBean, poBean);
		
		return responseMap;
	}
	
  @RequestMapping(value = URLRoute.PO_TAX_CHANGE, method = RequestMethod.POST)
  public Map<String, Object> onChangeTaxDetails(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Map<String, Object> responseMap = new HashMap<String, Object>();

    Map<String, String[]> reqMap = new HashMap<>();
    reqMap.putAll(request.getParameterMap());
    BasicDynaBean poBean = purchaseOrderService.toDetailsBean(reqMap);
    BasicDynaBean poMainBean = purchaseOrderService.toMainBean(reqMap);

    Integer[] taxSubGrp = null;

    if (reqMap.get(ITEM_SUBGROUP_ID) != null) {
      String itemSubgroupId = reqMap.get(ITEM_SUBGROUP_ID)[0];
      String[] itemSubGroupIds = null;
      if (!itemSubgroupId.trim().isEmpty()) {
        itemSubGroupIds = itemSubgroupId.split("\\,");
        taxSubGrp = new Integer[itemSubGroupIds.length];
        for (int i = 0; i < itemSubGroupIds.length; i++) {
          taxSubGrp[i] = Integer.valueOf(itemSubGroupIds[i]);
        }
      }

    }

    if (poBean.get("medicine_id") != null && poMainBean.get("store_id") != null
        && poMainBean.get("supplier_id") != null)
      responseMap = purchaseOrderService.onChangeTaxDetails(poMainBean, poBean, taxSubGrp);

    return responseMap;
  }

	@RequestMapping(value = URLRoute.PO_ITEM_TAX, method = RequestMethod.POST)
	public List<Map<String, Object>> getItemTaxDetails(HttpServletRequest request, HttpServletResponse response) throws Exception {
		List<Map<String, Object>> responseList = new ArrayList<Map<String,Object>>();
		
		Map<String, String[]> reqMap = ConversionUtils.flatten(request.getParameterMap());
		List<BasicDynaBean> poDetailList = purchaseOrderService.toDetailsBeanList(request.getParameterMap());
		BasicDynaBean poMainBean = purchaseOrderService.toMainBean(reqMap);
		
//		if(poMainBean.get("store_id")!= null && poMainBean.get("supplier_id")!=null)
			responseList = purchaseOrderService.getTaxDetails(poMainBean, poDetailList);
		
		return responseList;
	}
	
  @IgnoreConfidentialFilters
  @RequestMapping(value = URLRoute.GET_SUPPORTED_TAX_GROUPS, method = RequestMethod.GET)
  public Map getSupportedTaxGroups(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    Map taxGroups = purchaseOrderService.getSupportedTaxGroups();
    return taxGroups;
  }

	/*
	@RequestMapping(value = URLRoute.GET_COPY_PO, method = RequestMethod.GET)
	public ModelAndView getCopyPOScreen(HttpServletRequest req, HttpServletResponse resp, ModelAndView modelView) throws Exception {
		HttpSession session = req.getSession(false);
		String storeId = (String) session.getAttribute("pharmacyStoreId");
        int centerId = (Integer)req.getSession().getAttribute("centerId");
        String po_no = req.getParameter("poNo");
        String storeName = null;
        
        if (storeId != null && !storeId.isEmpty()) {
        	BasicDynaBean storeBean = purchaseOrderService.getStoreDetails(Integer.parseInt(storeId));
        	storeName = (String)storeBean.get("dept_name");
		}
        BasicDynaBean poMainBean = purchaseOrderService.getPOMain(po_no);
        BasicDynaBean suppBean = purchaseOrderService.getSuplierDetails((String)poMainBean.get("supplier_id")); 
		
        JSONSerializer js = new JSONSerializer().exclude("class");
        modelView.addObject("listAllcentersforAPo", js.serialize(purchaseOrderService.getAllSuppliers(centerId)));
        modelView.addObject("store_id", storeId);
        modelView.addObject("store_name", storeName);
        modelView.addObject("pobean", poMainBean);
        modelView.addObject("supplier_address", suppBean != null ? suppBean.get("supplier_address") : null );
        modelView.setViewName(ResponseRouter.COPY_PO);
     	return modelView;
	}
	
	@RequestMapping(value = URLRoute.COPY_PO, method = RequestMethod.POST)
	public ModelAndView copyPO(HttpServletRequest req, HttpServletResponse resp, RedirectAttributes attribs, ModelAndView modelView) throws Exception {
		boolean status = purchaseOrderService.copyPO(req, modelView);
		if(status) {
			//String deletedMessage = messageUtil.getMessage("flash.po.successfully", new Object[]{modelView.getModelMap().get("poNo")});
			// attribs.addFlashAttribute("info", deletedMessage);
			//modelView.setViewName(URLRoute.REDIRECT_PO);
			String newPoNo = (String)modelView.getModelMap().get("poNo");
			redirect(resp, URLRoute.REDIRECT_PO.concat(newPoNo));
			return null;
		} else {
			String deletedMessage = messageUtil.getMessage("exception.po.unable.to.copy", null);
			attribs.addFlashAttribute("info", deletedMessage);
			modelView.addAllObjects(req.getParameterMap());
			modelView.setViewName(URLRoute.REDIRECT_COPY_PO);
		}
		return modelView;
	}
*/
}
