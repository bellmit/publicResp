/**
 * 
 */
package com.insta.hms.master.SupplierContractsItem;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.MasterAction;
import com.insta.hms.master.MasterDAO;
import com.insta.hms.usermanager.UserDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author ashokkumar
 *
 */
public class SupplierContractsItemRateMasterAction extends MasterAction {
	
    static Logger logger = LoggerFactory.getLogger(SupplierContractsItemRateMasterAction.class);

    SupplierContractsItemRateDAO suppContractItemDao = new SupplierContractsItemRateDAO();

	@Override
	public MasterDAO getMasterDao() {
		return null;
	}
	
	@Override
	public Map<String, List<BasicDynaBean>> getLookupLists() throws SQLException {
		Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
		List<BasicDynaBean> suppContractItemLists = suppContractItemDao.getItems();
		map.put("medicine_name", suppContractItemLists);		
		return map;
	}
	
	public ActionForward list(ActionMapping m, ActionForm f,
				HttpServletRequest req, HttpServletResponse resp)
				throws IOException, ServletException, Exception {
		 PagedList pagedList = suppContractItemDao.getSupplierContractItemDetails(req.getParameterMap(),ConversionUtils.getListingParameter(req.getParameterMap()));
		 List<BasicDynaBean> supplierContractList = suppContractItemDao.getSupplierContracts();
		 req.setAttribute("pagedList", pagedList);
		 req.setAttribute("supplierContractList", supplierContractList);
		 getAutoLookupLists(req);
		 return m.findForward("list");
	}
	
	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		PagedList pagedList = suppContractItemDao.getSupplierContractItemDetails(req.getParameterMap(),ConversionUtils.getListingParameter(req.getParameterMap()));
		req.setAttribute("pagedList", pagedList);
	    return m.findForward("addshow");
	}
	
	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		
		Map paramMap = new HashMap();
		Map map =req.getParameterMap();
		paramMap.putAll(map);
		paramMap.put("medicine_id@type",new String[]{"integer"});
		paramMap.put("supplier_rate_contract_id@type",new String[]{"integer"});
		PagedList pagedList = suppContractItemDao.getSupplierContractDetails(paramMap,ConversionUtils.getListingParameter(req.getParameterMap()));
		req.setAttribute("pagedList", pagedList);
	    return m.findForward("addshow");
	}
	
	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("medicine_name", (String)req.getParameter("medicine_name"));
		redirect.addParameter("supplier_code", (String)req.getParameter("supplier_code"));	 
		return redirect;
	}
	
	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(req);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		String supplier_rate_contract_id[] = req.getParameterValues("supplier_rate_contract_id");
		int medicine_id = Integer.parseInt((String)req.getParameter("medicine_id"));
		String _update_supplier_rate[] = req.getParameterValues("_update_supplier_rate");
		String _update_discount[] = req.getParameterValues("_update_discount");
		String _update_mrp[] = req.getParameterValues("_update_mrp");
		String _update_margin[] = req.getParameterValues("_update_margin");
		String _update_margin_type[] = req.getParameterValues("_update_margin_type");
		
		String supplier_rate[] = req.getParameterValues("supplier_rate");
		String discount[] = req.getParameterValues("discount");
		String mrp[] = req.getParameterValues("mrp");
		String margin[] = req.getParameterValues("margin");
		String marginType[] = req.getParameterValues("margin_type");
		Connection con = null;
		int k=0;
		try {
			con = DataBaseUtil.getConnection();
			GenericDAO storeSupplierItemRateDAO = new GenericDAO("store_supplier_contracts_item_rates");
			Map<String,Object> columndata = new HashMap<String,Object>();
			Map<String,Integer>  keys= new HashMap<String,Integer>();
			int len = supplier_rate_contract_id.length;
			for(int i=0;i<len;i++){
				if(_update_supplier_rate[i].equals("true")){
          if(!StringUtils.isEmpty(supplier_rate[i])){
            columndata.put("supplier_rate", BigDecimal.valueOf(Double.parseDouble(supplier_rate[i])));
          }else{
            columndata.put("supplier_rate", null);
          }
					keys.put("supplier_rate_contract_id", Integer.parseInt(supplier_rate_contract_id[i]));
					keys.put("medicine_id", medicine_id);
					k = storeSupplierItemRateDAO.update(con, columndata, keys);
				}
				if(_update_discount[i].equals("true")){
					if(null!= discount[i] && !discount[i].equals("")){
						columndata.put("discount", BigDecimal.valueOf(Double.parseDouble(discount[i])));
					}else{
						columndata.put("discount", null);
					}
					keys.put("supplier_rate_contract_id", Integer.parseInt(supplier_rate_contract_id[i]));
					keys.put("medicine_id", medicine_id);
					k = storeSupplierItemRateDAO.update(con, columndata, keys);
				}
				if(_update_mrp[i].equals("true")){
					if(null!= mrp[i] && !mrp[i].equals("")){
						columndata.put("mrp", BigDecimal.valueOf(Double.parseDouble(mrp[i])));
					}else{
						columndata.put("mrp", null);
					}
					keys.put("supplier_rate_contract_id", Integer.parseInt(supplier_rate_contract_id[i]));
					keys.put("medicine_id", medicine_id);
					k = storeSupplierItemRateDAO.update(con, columndata, keys);
				}
				if(_update_margin[i].equals("true")){
				  if(null!= margin[i] && !margin[i].equals("")){
				    columndata.put("margin", BigDecimal.valueOf(Double.parseDouble(margin[i])));
				  }else{
				    columndata.put("margin", null);
				  }
				  keys.put("supplier_rate_contract_id", Integer.parseInt(supplier_rate_contract_id[i]));
				  keys.put("medicine_id", medicine_id);
				  k = storeSupplierItemRateDAO.update(con, columndata, keys);
				}
				if(_update_margin_type[i].equals("true")){
				  if(null!= marginType[i] && !marginType[i].equals("")){
				    columndata.put("margin_type", marginType[i]);
				  }else{
				    columndata.put("margin_type", null);
				  }
				  keys.put("supplier_rate_contract_id", Integer.parseInt(supplier_rate_contract_id[i]));
				  keys.put("medicine_id", medicine_id);
				  k = storeSupplierItemRateDAO.update(con, columndata, keys);
				}
				
			}
		
		flash.info("Supplier Item Rates Details updated successfully...");
			
		}finally{
			DataBaseUtil.closeConnections(con, null);
		}
		
		redirect.addParameter("medicine_id", (String)req.getParameter("medicine_id"));
		redirect.addParameter("supplier_code", (String)req.getParameter("supplier_code"));
		redirect.addParameter("supplier_rate_contract_id", (String)req.getParameter("supplier_rate_contract_id"));
	    return redirect;
	}
	
	public ActionForward Redirectlist(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
	 
		ActionRedirect redirect = new ActionRedirect(m.findForward("Redirectlist"));
		redirect.addParameter("supplier_rate_contract_id", (String)req.getParameter("supplier_rate_contract_id"));	 
		redirect.addParameter("supplier_rate_contract_id@type","integer");
		redirect.addParameter("status",(String)req.getParameter("status"));
		return redirect;
	}
	
	public ActionForward getSupplierItemRateDetails(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException, SQLException, ParseException {

		JSONSerializer js = new JSONSerializer().exclude("class");
		int medicineId = Integer.parseInt(req.getParameter("medicineId"));
		String supplierId = req.getParameter("supplierId");
		//int centerId = (Integer)req.getSession().getAttribute("centerId");
		int storeId = Integer.parseInt(req.getParameter("storeId"));
		BasicDynaBean centerIdBean = UserDAO.getCenterId(storeId);
		int centerId = (Integer)centerIdBean.get("center_id");
		
		SupplierContractsItemRateDAO supContractItemDao = new SupplierContractsItemRateDAO();
		List<BasicDynaBean> beans = supContractItemDao.getSupplierItemRateValue(medicineId,supplierId, centerId);
		Map supplierContractRateMap = new HashMap();
		
		if(beans != null && beans.size() > 0){
			supplierContractRateMap.putAll(beans.get(0).getMap());
			supplierContractRateMap.put("supplier_id", supplierId);
			supplierContractRateMap.put("storeId", storeId);
		}else{
			supplierContractRateMap.put("supplier_rate", "");
			supplierContractRateMap.put("supplier_rate_validation", "false");
			supplierContractRateMap.put("supplier_id", supplierId);
			supplierContractRateMap.put("storeId", storeId);
		}
		
		res.setContentType("application/x-json");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.setHeader("Expires", "0");
        res.getWriter().write(js.deepSerialize(supplierContractRateMap));
        res.flushBuffer();
		return null;
	}

}
