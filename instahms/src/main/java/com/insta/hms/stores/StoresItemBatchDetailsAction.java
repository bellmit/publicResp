package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author mithun.saha
 *
 */

public class StoresItemBatchDetailsAction extends DispatchAction{
  
	static Logger logger = LoggerFactory.getLogger(StoresItemBatchDetailsAction.class);
	private static StoresItemBatchDetailsDAO sdao = new StoresItemBatchDetailsDAO();
	private static JSONSerializer js = new JSONSerializer().exclude("class");
	
    private static final GenericDAO genericPreferencesDAO = new GenericDAO("generic_preferences");
    private static final GenericDAO uUserDAO = new GenericDAO("u_user");

	@IgnoreConfidentialFilters
	public ActionForward list(ActionMapping map,ActionForm form,
				HttpServletRequest req,HttpServletResponse res) throws ServletException,Exception {
		BasicDynaBean hosp = genericPreferencesDAO.getRecord();
        if(hosp.get("stock_entry_agnst_do") != null)
            req.setAttribute("stock_entry_agnst_do", hosp.get("stock_entry_agnst_do"));
		
        req.setAttribute("userCenterId",uUserDAO.findByKey("emp_username", req.getSession(false).getAttribute("userId")).get("center_id"));
		return map.findForward("list");
	}

	@IgnoreConfidentialFilters
	public ActionForward searchList(ActionMapping map,ActionForm form,
				HttpServletRequest req,HttpServletResponse res) throws ServletException,Exception {
		PagedList pagedList = sdao.getStoreConsumptionDetailsList(req.getParameterMap(), ConversionUtils.getListingParameter(req.getParameterMap()));
		BasicDynaBean hosp = genericPreferencesDAO.getRecord();
        if(hosp.get("stock_entry_agnst_do") != null)
            req.setAttribute("stock_entry_agnst_do", hosp.get("stock_entry_agnst_do"));
		
        req.setAttribute("userCenterId",uUserDAO.findByKey("emp_username", req.getSession(false).getAttribute("userId")).get("center_id"));req.setAttribute("pagedList", pagedList);
		req.setAttribute("itemsList", js.serialize(ConversionUtils.listBeanToListMap(sdao.getItemDetails())));
		return map.findForward("list");
	}

	@IgnoreConfidentialFilters
	public ActionForward show(ActionMapping map,ActionForm form,
			HttpServletRequest req,HttpServletResponse res) throws ServletException,Exception {

		String itemBatchId = req.getParameter("item_batch_id");
		String storeId = req.getParameter("dept_id");
		req.setAttribute("userCenterId",uUserDAO.findByKey("emp_username", req.getSession(false).getAttribute("userId")).get("center_id"));		
		BasicDynaBean itemBatchBean = sdao.getItemBatchDetails(Integer.parseInt(itemBatchId),Integer.parseInt(storeId));
		req.setAttribute("itemBatchBean", itemBatchBean);
		return map.findForward("show");
	}

	public ActionForward updateItemBatchDetails(ActionMapping map,ActionForm form,
			HttpServletRequest req,HttpServletResponse res) throws ServletException,Exception {
		String itemBatchId = req.getParameter("item_batch_id");
		String storeId = req.getParameter("dept_id");
		BasicDynaBean itemBatchBean = sdao.getBean();
		FlashScope flash = FlashScope.getScope(req);
		List errors = new ArrayList();
		Connection con = null;
		boolean success = false;
		HttpSession session=req.getSession(false);
		String username = (String) session.getAttribute("userid");
		ActionRedirect redirect = new ActionRedirect(map.findForward("showRedirect"));
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ConversionUtils.copyToDynaBean(req.getParameterMap(), itemBatchBean, errors);
			itemBatchBean.set("username", username);
			int key = Integer.parseInt(itemBatchId);
			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("item_batch_id", key);
			if(errors.isEmpty()) {
				success = sdao.update(con, itemBatchBean.getMap(), keys) > 0;
				success = true;
				flash.success("Store Item Batch details updated successfully..");
			} else {
				flash.error("Incorrectly formatted values supplied");
				success = false;
			}
			redirect.addParameter("item_batch_id", itemBatchId);
			redirect.addParameter("dept_id", storeId);
		} finally {
			DataBaseUtil.commitClose(con, success);
		}
		return redirect;
	}
	
	@IgnoreConfidentialFilters
	public ActionForward insertBarCode(ActionMapping map,ActionForm form,
			HttpServletRequest req,HttpServletResponse res) throws ServletException,Exception {
		
		ActionRedirect redirect = new ActionRedirect(map.findForward("barcodePrintRedirect"));
		String itemId = req.getParameter("itemId");
		String noOfPrints = req.getParameter("noOfPrints");
		String item_batch_id = req.getParameter("item_batch_id");
		GenericDAO dao = new GenericDAO("store_item_details");
		List<BasicDynaBean> store_item_details_list = dao.findAllByKey("medicine_id", Integer.parseInt(itemId));
		BasicDynaBean bean = (BasicDynaBean) store_item_details_list.get(0);
		Object obj = bean.get("item_barcode_id");
		if (obj == null || obj.equals("")) {
			Connection con = DataBaseUtil.getConnection();
			String barcode = StockEntryDAO.getBarCode();
			Map columnMap = new HashMap();
			columnMap.put("item_barcode_id ", barcode);
			Map keyMap = new HashMap();
			keyMap.put("medicine_id", Integer.parseInt(itemId));
			DataBaseUtil.dynaUpdate(con, "store_item_details", columnMap, keyMap);
			DataBaseUtil.closeConnections(con, null);
		}
		redirect.addParameter("barcodeType", req.getParameter("barcodeType"));
		redirect.addParameter("itemId", itemId);
		if(null == noOfPrints || "".equals(noOfPrints)){
			redirect.addParameter("noOfPrints", "1");
		}else{
			redirect.addParameter("noOfPrints", noOfPrints);
		}
		
		if(null == item_batch_id || "".equals(item_batch_id)){
			redirect.addParameter("item_batch_id", "");
		}else{
			redirect.addParameter("item_batch_id", item_batch_id);
		}
			
		return redirect;
	}
}