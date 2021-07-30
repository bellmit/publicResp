package com.insta.hms.master.SupplierRateContract;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.utils.JsonUtility;
import com.insta.hms.master.SupplierContractsItem.SupplierContractsItemRateDAO;
import com.insta.hms.stores.PharmacymasterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 
 * @author irshad
 *
 */
public class SupplierRateContractMasterAction extends BaseAction {
	
	SupplierRateContractDAO supplierRateContractdao = new SupplierRateContractDAO();
    private static final GenericDAO storeSupplierContractsDAO = new GenericDAO("store_supplier_contracts");

	
	public ActionForward list(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{
		
		JSONSerializer js = new JSONSerializer().exclude("class");
		
		List<BasicDynaBean> contractLists = supplierRateContractdao.getContractNames();

		request.setAttribute("lookupListMap", js.deepSerialize(ConversionUtils.listBeanToListMap(contractLists)));
		PagedList pagedList = supplierRateContractdao.getSupplierContractItemDetails(request.getParameterMap(),ConversionUtils.getListingParameter(request.getParameterMap()));
		request.setAttribute("pagedList", pagedList);
		List<BasicDynaBean> supplierlist = (List<BasicDynaBean>) supplierRateContractdao.getSupplierList();
		request.setAttribute("supplierlist", supplierlist);
		
		return m.findForward("list");
	}
	
	public ActionForward addshow(ActionMapping m,ActionForm f,
			HttpServletRequest req,HttpServletResponse res) throws Exception{

		JSONSerializer js = new JSONSerializer().exclude("class");
		int centerId = (Integer)req.getSession().getAttribute("centerId");
		String supplier_rate_contract_id = req.getParameter("supplier_rate_contract_id");
		String supplierCode = req.getParameter("supplier_code");
		List<String> columnList = new ArrayList<String>();
		columnList.add("supplier_rate_contract_name");
		List<BasicDynaBean> supplierRateContractNameList = storeSupplierContractsDAO.listAll(columnList,"supplier_rate_contract_name");
		if (supplier_rate_contract_id != null && !supplier_rate_contract_id.equals("") && Integer.valueOf(supplier_rate_contract_id) > -1) {
			BasicDynaBean sscbean = null;
			if(null != supplierCode && supplierCode.equals("-1")){
				 sscbean = SupplierRateContractDAO.getSupplierRateContractDetailsForDefaultSupplier(supplier_rate_contract_id);
			}else{
				 sscbean = SupplierRateContractDAO.getSupplierRateContractDetails(supplier_rate_contract_id);
			}
			//BasicDynaBean sscbean = SupplierRateContractDAO.getSupplierRateContractDetails(supplier_rate_contract_id);
			req.setAttribute("sscbean", sscbean);
			req.setAttribute("supplier_code", sscbean.get("supplier_code"));
			req.setAttribute("supplier_rate_contract_id", supplier_rate_contract_id);
			req.setAttribute("centerId", centerId);
			req.setAttribute("op_mode", "edit");
			
			req.setAttribute("listAllcentersforAPo", js.deepSerialize(SupplierRateContractDAO.listAllcentersforAPo(0)));
		} else {
			req.setAttribute("op_mode", "add");
			List suppliers = SupplierRateContractDAO.listAllcentersforAPoActive(centerId);
			Hashtable defaultSupplier = new Hashtable();
			defaultSupplier.put("SUPPLIER_CODE", "-1");
			defaultSupplier.put("SUPPLIER_NAME_WITH_CITY", "DEFAULT SUPPLIER");
			suppliers.add(defaultSupplier);
			req.setAttribute("listAllcentersforAPo", js.deepSerialize(suppliers));
			//req.setAttribute("listAllcentersforAPo", js.deepSerialize(SupplierRateContractDAO.listAllcentersforAPoActive(centerId)));
		}
    
		req.setAttribute("supplierRateContractNameList", JsonUtility.toJson(ConversionUtils.listBeanToListMap(supplierRateContractNameList)));
    
		req.setAttribute("master_timestamp", PharmacymasterDAO.getItemMasterTimestamp());
		return m.findForward("addshow");
	}
	
	public ActionForward addScreen(ActionMapping m,ActionForm f,
			HttpServletRequest req,HttpServletResponse res) throws Exception{

		JSONSerializer js = new JSONSerializer().exclude("class");
		int centerId = (Integer)req.getSession().getAttribute("centerId");
		String supplier_rate_contract_id = req.getParameter("supplier_rate_contract_id");
		String supplierCode = req.getParameter("supplier_code");
		List<String> columnList = new ArrayList<String>();
		columnList.add("supplier_rate_contract_name");
		List<BasicDynaBean> supplierRateContractNameList = storeSupplierContractsDAO.listAll(columnList,"supplier_rate_contract_name");
		if (supplier_rate_contract_id != null && !supplier_rate_contract_id.equals("") && Integer.valueOf(supplier_rate_contract_id) > -1) {
			BasicDynaBean sscbean = null;
			if(null != supplierCode && supplierCode.equals("-1")){
				 sscbean = SupplierRateContractDAO.getSupplierRateContractDetailsForDefaultSupplier(supplier_rate_contract_id);
			}else{
				 sscbean = SupplierRateContractDAO.getSupplierRateContractDetails(supplier_rate_contract_id);
			}
			//BasicDynaBean sscbean = SupplierRateContractDAO.getSupplierRateContractDetails(supplier_rate_contract_id);
			req.setAttribute("sscbean", sscbean);
			req.setAttribute("supplier_code", sscbean.get("supplier_code"));
			req.setAttribute("supplier_rate_contract_id", supplier_rate_contract_id);
			req.setAttribute("centerId", centerId);
			req.setAttribute("op_mode", "edit");
			
			req.setAttribute("listAllcentersforAPo", js.deepSerialize(SupplierRateContractDAO.listAllcentersforAPo(0)));
		} else {
			req.setAttribute("op_mode", "add");
			List suppliers = SupplierRateContractDAO.listAllcentersforAPoActive(centerId);
			Hashtable defaultSupplier = new Hashtable();
			defaultSupplier.put("SUPPLIER_CODE", "-1");
			defaultSupplier.put("SUPPLIER_NAME_WITH_CITY", "DEFAULT SUPPLIER");
			suppliers.add(defaultSupplier);
			req.setAttribute("listAllcentersforAPo", js.deepSerialize(suppliers));
			//req.setAttribute("listAllcentersforAPo", js.deepSerialize(SupplierRateContractDAO.listAllcentersforAPoActive(centerId)));
		}
		req.setAttribute("mode", "additem");
		req.setAttribute("supplierRateContractNameList", JsonUtility.toJson(ConversionUtils.listBeanToListMap(supplierRateContractNameList)));
		req.setAttribute("master_timestamp", PharmacymasterDAO.getItemMasterTimestamp());
		return m.findForward("addshow");
	}
	
	public ActionForward saveSupplierRateContract(ActionMapping mapping, ActionForm f,
			HttpServletRequest req,HttpServletResponse res) throws Exception{
		
		SupplierRateContractForm uploadForm = (SupplierRateContractForm) f;
		String supplier_contract_name = uploadForm.getSupplier_contract_name();
		String supplier_code = uploadForm.getSupplier_id();
		String validity_start_date = uploadForm.getValidity_start_date();
		String validity_end_date = uploadForm.getValidity_end_date();
		String status = uploadForm.getStatus();
		String supplier_rate_contract_id = uploadForm.getSupplier_rate_contract_id();
		if(null == supplier_code || supplier_code.equals("")){
			supplier_code = "-1";
		}
		
		ActionRedirect redirect = null;
		HttpSession session = req.getSession(false);
		String username = (String) session.getAttribute("userid");
		Connection con = null;
		boolean dbStatus = false;
		BasicDynaBean sscMainDAO =null;
		FlashScope flash = FlashScope.getScope(req);
		int sscNO = -1;
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		BasicDynaBean existSupplierRateContractMainBean = null;
	    try {
	    	redirect = new ActionRedirect(mapping.findForward("showRedirect"));
	    	con = DataBaseUtil.getConnection();
	    	sscMainDAO = storeSupplierContractsDAO.getBean();
	    	
	    	if ( supplier_rate_contract_id != null && !supplier_rate_contract_id.equals("") && Integer.valueOf(supplier_rate_contract_id) > -1 ){
              existSupplierRateContractMainBean = storeSupplierContractsDAO.findByKey(con,
                  "supplier_rate_contract_id", Integer.valueOf(supplier_rate_contract_id));
				if(existSupplierRateContractMainBean != null) {
					sscNO = Integer.valueOf(supplier_rate_contract_id);
				}
	    	}
	    	
	    	
	    	String[] medicine_id = (String[]) req.getParameterValues("medicine_id");
	    	String[] mrp = (String[]) req.getParameterValues("mrp");
	    	String[] discount = (String[]) req.getParameterValues("discount");
	    	String[] rate = (String[]) req.getParameterValues("supplier_rate");
	    	String[] margin = (String[]) req.getParameterValues("margin");
	    	String[] marginType = (String[]) req.getParameterValues("margin_type");
	    	
			int elelen = medicine_id == null ? 0 : medicine_id.length-1;
			GenericDAO itemRatesDAO = new GenericDAO("store_supplier_contracts_item_rates");
			Map<String, Object> findKeys = new HashMap<String, Object>();
			
			for (int i=0; i<elelen; i++) {
				
				redirect = new ActionRedirect(mapping.findForward("showAddRedirect"));
				
				BasicDynaBean rateBean = itemRatesDAO.getBean();
				rateBean.set("supplier_rate_contract_id", Integer.parseInt(supplier_rate_contract_id));
				rateBean.set("medicine_id", Integer.parseInt(medicine_id[i]) );
				rateBean.set("supplier_rate",  new BigDecimal(rate[i]));
				rateBean.set("discount", discount[i] != null && !discount[i].isEmpty() ? new BigDecimal(discount[i]) : null );
				rateBean.set("mrp", mrp[i] != null && !mrp[i].isEmpty() ? new BigDecimal(mrp[i]) : null);
				rateBean.set("margin", margin[i] != null && !margin[i].isEmpty() ? new BigDecimal(margin[i]) : null);
				rateBean.set("margin_type", marginType[i] != null && !marginType[i].isEmpty() ? marginType[i] : null);
				
				findKeys.put("supplier_rate_contract_id", Integer.parseInt(supplier_rate_contract_id));
				findKeys.put("medicine_id", Integer.parseInt(medicine_id[i]) );
				
				if ( itemRatesDAO.findByKey(con, findKeys) != null ){
					itemRatesDAO.update(con, rateBean.getMap(),findKeys);
				}else{
					itemRatesDAO.insert(con, rateBean);
				}
				flash.info("Added items successfully");
			}
			
			if ( elelen == 0 ) {//no supplier update on add item
			if(supplier_rate_contract_id != null && !supplier_rate_contract_id.equals("") && Integer.valueOf(supplier_rate_contract_id) > -1) {
				if(existSupplierRateContractMainBean != null) {
					sscMainDAO.set("supplier_rate_contract_id", Integer.valueOf(supplier_rate_contract_id));
					sscMainDAO.set("supplier_rate_contract_name", supplier_contract_name);
					sscMainDAO.set("supplier_code", supplier_code);
					sscMainDAO.set("validity_start_date", new java.sql.Timestamp(sdf.parse(validity_start_date).getTime()));
					sscMainDAO.set("validity_end", new java.sql.Timestamp(sdf.parse(validity_end_date).getTime()));
					sscMainDAO.set("status", status);
					if(status.equalsIgnoreCase("A")) {
						ArrayList centerList = SupplierRateContractDAO.getCenterForSupplierRateContract(supplier_rate_contract_id);
						if(centerList.size() > 0) {
							ArrayList existSupplierRateContractList =  SupplierRateContractDAO.getSupplierRateContract(centerList, supplier_code, new java.sql.Timestamp(sdf.parse(validity_start_date).getTime()), new java.sql.Timestamp(sdf.parse(validity_end_date).getTime()), supplier_rate_contract_id);
							if(existSupplierRateContractList.size() > 0 && !existSupplierRateContractList.contains(supplier_contract_name)) {
								String activeSupplierRateContractName = (String)existSupplierRateContractList.get(0);
								if(!activeSupplierRateContractName.equalsIgnoreCase(supplier_contract_name)) {
									flash.error(activeSupplierRateContractName+" is active for selected centers");
								} else {
									flash.error((String)existSupplierRateContractList.get(1)+" is active for selected centers");
								}
								
								redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
								redirect.addParameter("supplier_rate_contract_id", sscNO);
								return redirect;
							} else if(existSupplierRateContractList.size() > 1 && existSupplierRateContractList.contains(supplier_contract_name)) {
								String activeSupplierRateContractName = (String)existSupplierRateContractList.get(0);
								if(!activeSupplierRateContractName.equalsIgnoreCase(supplier_contract_name)) {
									flash.error(activeSupplierRateContractName+" is active for selected centers");
								} else {
									flash.error((String)existSupplierRateContractList.get(1)+" is active for selected centers");
								}
								
								redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
								redirect.addParameter("supplier_rate_contract_id", sscNO);
								return redirect;
							}
						}						
					}					
					int linesUpdated = storeSupplierContractsDAO.update(con, sscMainDAO.getMap(), "supplier_rate_contract_id", Integer.valueOf(supplier_rate_contract_id));
					if( linesUpdated > 0 && uploadForm.getUploadFile() != null ) {
						dbStatus = true;
						if(uploadForm.getUploadFile().getInputStream().available() > 0) {
							InputStreamReader isReader = new InputStreamReader(uploadForm.getUploadFile().getInputStream());
							StringBuilder infoMsg = new StringBuilder();
							String error = getDataHandler(null).importTable(isReader, infoMsg, supplier_contract_name);
							if(error != null) {
								flash.error(error);
							} else {
								flash.info("Supplier Rate Contract: "+supplier_contract_name+" Updated Successfully \r\n"+infoMsg);
							}
						} else {
							flash.info("Supplier Rate Contract: "+supplier_contract_name+" Updated Successfully");
						}						
					} else {
						flash.error("Unable to Update Supplier Rate Contract: "+supplier_contract_name);
					}
										
				} else {
					flash.error("Unable to Update Supplier Rate Contract: "+supplier_contract_name);
				}
			} else {
				sscNO = storeSupplierContractsDAO.getNextSequence();
				sscMainDAO.set("supplier_rate_contract_id", sscNO);
				sscMainDAO.set("supplier_rate_contract_name", supplier_contract_name);
				sscMainDAO.set("supplier_code", supplier_code);
				sscMainDAO.set("validity_start_date", new java.sql.Timestamp(sdf.parse(validity_start_date).getTime()));
				sscMainDAO.set("validity_end", new java.sql.Timestamp(sdf.parse(validity_end_date).getTime()));
				sscMainDAO.set("status", status);
				if(status.equalsIgnoreCase("A")) {
					List<String> centerIdList = new ArrayList<String>();
					centerIdList.add("0");
					List existSupplierRateContractList =  SupplierRateContractDAO.getSupplierRateContract(centerIdList, supplier_code, new java.sql.Timestamp(sdf.parse(validity_start_date).getTime()), new java.sql.Timestamp(sdf.parse(validity_end_date).getTime()), null);
					if(existSupplierRateContractList.size() > 0) {
						flash.error(existSupplierRateContractList.get(0)+" is active for selected centers");
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						redirect.addParameter("supplier_rate_contract_id", -1);
						return redirect;
					}
				}
				
				dbStatus = storeSupplierContractsDAO.insert(con, sscMainDAO);
				if(dbStatus) {
					GenericDAO sscenterGenericDAO = new GenericDAO("store_supplier_contracts_center_applicability");
					BasicDynaBean sscenterBean = sscenterGenericDAO.getBean();
					/*sscenterBean.set("supplier_rate_contract_center_id", sscenterGenericDAO.getNextSequence());*/
					sscenterBean.set("supplier_rate_contract_id", sscNO);
					sscenterBean.set("center_id", 0);
					sscenterBean.set("status", "A");
					dbStatus = sscenterGenericDAO.insert(con, sscenterBean);
					if(dbStatus) {
						if(uploadForm.getUploadFile().getInputStream().available() > 0) {
							InputStreamReader isReader = new InputStreamReader(uploadForm.getUploadFile().getInputStream());
							StringBuilder infoMsg = new StringBuilder();
							String error = getDataHandler(null).importTable(isReader, infoMsg, supplier_contract_name);
							if(error != null) {
								flash.error(error);
							} else {
								flash.info("Supplier Rate Contract: "+supplier_contract_name+" Generated Successfully \r\n"+infoMsg);
							}
						} else {
							flash.info("Supplier Rate Contract: "+supplier_contract_name+" Generated Successfully");
						}						
					} else {
						flash.error("Unable to Add Supplier Rate Contract: "+supplier_contract_name);
					}
					
				} else {
					flash.error("Unable to Add Supplier Rate Contract: "+supplier_contract_name);
				}
			
			}	
			
	    }
			
	    } finally {
			DataBaseUtil.closeConnections(con, null);
		}		
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("supplier_rate_contract_id", sscNO);
		redirect.addParameter("supplier_rate_contract_id@type", "integer");
		redirect.addParameter("status", "A");
		if ( !redirect.getName().equals("listRedirect") ){
			if(null!= supplier_code && supplier_code.equals("-1")){
				redirect.addParameter("supplier_code", -1);
			}else{
				redirect.addParameter("supplier_code", supplier_code);
			}
		}
		return redirect;
	}
	
	public ActionForward exportMaster(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		String supplier_rate_contract_id = req.getParameter("supplier_rate_contract_id");
		getDataHandler(supplier_rate_contract_id).exportTable(res);
		return null;
	}

	private static SupplierRateDataHandler masterHandler = null;

	protected SupplierRateDataHandler getDataHandler(String supplier_rate_contract_id) {
		String supplierRateContractWhere = "";
		if(supplier_rate_contract_id != null && !supplier_rate_contract_id.equals("")) {
			supplierRateContractWhere = " store_supplier_contracts_item_rates.supplier_rate_contract_id ="+Integer.valueOf(supplier_rate_contract_id);
		} else {
			supplierRateContractWhere = " store_supplier_contracts_item_rates.supplier_rate_contract_id =-1";
		}
		masterHandler = new SupplierRateDataHandler(
				"store_supplier_contracts_item_rates",		// table name
				new String[]{"supplier_rate_contract_id","medicine_id"},	// keys
				new String[]{"supplier_rate","discount","mrp", "margin", "margin_type"},
				new String[][]{
					// our field        ref table        ref table id field  ref table name field
					{"medicine_id", "store_item_details", "medicine_id", "medicine_name"},
					{"supplier_rate_contract_id", "store_supplier_contracts", "supplier_rate_contract_id", "supplier_rate_contract_name"}
				},
				new String[]{supplierRateContractWhere});
		

		return masterHandler;
	}
	
	public ActionForward getExistingItemStatus(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		JSONSerializer js = new JSONSerializer().exclude("class");
		String rateContractId = req.getParameter("contractId");
		String medicineIdStr = req.getParameter("medicineId");
		int itemId = Integer.parseInt(medicineIdStr);
		int contractId = Integer.parseInt(rateContractId);
		
		
		BasicDynaBean itemRateDetails = new SupplierContractsItemRateDAO().getSupplierItemDetails(contractId, itemId);
		String status = js.serialize(itemRateDetails == null ? "notexist" : "exist");
		res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.setHeader("Expires", "0");
		res.setContentType("text/plain");
        res.getWriter().write(status);
        res.flushBuffer();
		return null;
	}
}
