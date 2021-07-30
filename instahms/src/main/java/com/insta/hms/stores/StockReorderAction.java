/**
 *
 */
package com.insta.hms.stores;

import au.com.bytecode.opencsv.CSVWriter;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.QueryBuilder;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.usermanager.UserDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class StockReorderAction  extends DispatchAction{


	static Logger logger = LoggerFactory.getLogger(StockReorderAction.class);
	
    private static final GenericDAO storesDAO = new GenericDAO("stores");

	static int pageSize = 100;

	@IgnoreConfidentialFilters
	public Object getReorderList(HttpServletRequest req, Boolean getPagedList)throws IOException, SQLException, Exception,ParseException {
		StockReorderDAO reorderDAO = new StockReorderDAO();
		int pageNumber = getPageNumber(req);
		Map filterMap = getFiltersFromParamMap(req);
		Object reorderList = null;
		String reorderCriteria = req.getParameter("criteria");

		req.setAttribute("decimalsAllowed",
				new GenericPreferencesDAO().getGenericPreferences().getAllowdecimalsforqty());

		if (reorderCriteria == null || reorderCriteria.equals("")) {
			reorderCriteria = "cons";
		}
		if (reorderCriteria.equalsIgnoreCase("cons")) {
			// get consumption days and order days
			String conspdays = req.getParameter("cons_days");
			if (conspdays == null || conspdays.equals(""))
				conspdays = "1";
			Integer consumptionDays = Integer.parseInt(conspdays);

			String orderdays = req.getParameter("to_order_days");
			if (orderdays == null || orderdays.equals("")) {
				orderdays = "0";
			}
			Integer orderDays = Integer.parseInt(orderdays);
			// passed to consumption handle
			if(getPagedList) {
				reorderList = reorderDAO.getConsumptionBasedReorder(consumptionDays, orderDays, filterMap,
						pageNumber);
			} else {
				reorderList = reorderDAO.getConsumptionBasedReorderForCSV(consumptionDays, orderDays, filterMap);
			}
		} else if (reorderCriteria.equalsIgnoreCase("indent")) {

			String indentType = req.getParameter("indent_type");
			String indentField = "";
			if (indentType.equals("indent_purchase")) {
				indentField = "purchase_indent";
			} else {
				indentField = "all_indent";
			}
			String indentno = req.getParameter(indentField);
			if (indentno == null || indentno.equals("")) {
				indentno = "0";
			}
			Integer indentNo = Integer.parseInt(indentno);
			// indent handler
			if(getPagedList) {
				reorderList = reorderDAO.getIndentBasedReorder(indentType.equals("indent_purchase") ? "purchase"
						: "all", indentNo, filterMap, pageNumber);
			} else {
				reorderList = reorderDAO.getIndentBasedReorderForCSV(indentType.equals("indent_purchase") ? "purchase"
						: "all", indentNo, filterMap);
			}
		} else if (reorderCriteria.equalsIgnoreCase("pending_indent")) {
			String indentage = req.getParameter("pending_indent_age");
			if (indentage == null || indentage.equals("")) {
				indentage = "0";
			}
			Integer indentAge = Integer.parseInt(indentage);
			// indent age handler
			if(getPagedList) {
				reorderList = reorderDAO.getPendingIndentAgeBasedReorder(indentAge, filterMap, pageNumber);
			} else {
				reorderList = reorderDAO.getPendingIndentAgeBasedReorderForCSV(indentAge, filterMap);
			}
		} else if (reorderCriteria.equalsIgnoreCase("reorder")) {

			String reorderLevel = req.getParameter("reorder_level");
			// handle reorder
			if(getPagedList) {
				reorderList = reorderDAO.getReorderLevelBasedReorder(reorderLevel, filterMap, pageNumber);
			} else {
				reorderList = reorderDAO.getReorderLevelBasedReorderForCSV(reorderLevel, filterMap);
			}
		}
		
		return reorderList;
	}

	@IgnoreConfidentialFilters
	public ActionForward list(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
			throws IOException, SQLException, Exception {
		JSONSerializer js = new JSONSerializer().exclude("class");
		PagedList reorderList = (PagedList)getReorderList(req, true);
		int centerId = (Integer)req.getSession().getAttribute("centerId");
		
		req.setAttribute("listAllcentersforAPo", StockReorderDAO.listAllcentersforAPo(centerId));
		req.setAttribute("allStoresJSON",
				js.serialize(ConversionUtils.listBeanToListMap(storesDAO.listAll())));
		req.setAttribute("decimalsAllowed",
				new GenericPreferencesDAO().getGenericPreferences().getAllowdecimalsforqty());
		if(req.getParameter("dept_id")!= null && !req.getParameter("dept_id").equals("")){
			req.setAttribute("dept_name", DataBaseUtil
					.getStringValueFromDb("select dept_name from stores where dept_id=?", Integer.parseInt(req.getParameter("dept_id"))));
		}
		req.setAttribute("pagedList", reorderList);
		return getForward("stockreorder");
	}

	@IgnoreConfidentialFilters
	public ActionForward stockReorder(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws IOException, SQLException, Exception {
		HttpSession session = req.getSession(false);
		String dept_id = (String) session.getAttribute("pharmacyStoreId");
		int centerId = (Integer)req.getSession().getAttribute("centerId");
		req.setAttribute("listAllcentersforAPo", StockReorderDAO.listAllcentersforAPo(centerId));
		if (dept_id != null && !dept_id.equals("")) {
			BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(dept_id));
			String dept_name = dept.get("dept_name").toString();
			req.setAttribute("dept_id", dept_id);
			req.setAttribute("dept_name", dept_name);

			req.setAttribute("isSuperStore", dept.get("is_super_store"));
			req.setAttribute("default_store", "Yes");
		}
		if (dept_id != null && dept_id.equals("")) {
			req.setAttribute("dept_id", dept_id);
			req.setAttribute("dept_name", req.getParameter("dept_name"));
			req.setAttribute("default_store", "No");
		}
		req.setAttribute("pageNumber", "0");
		req.setAttribute("pagecount", "0");
		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("allStoresJSON",
				js.serialize(ConversionUtils.listBeanToListMap(storesDAO.listAll())));
		req.setAttribute("decimalsAllowed",
				new GenericPreferencesDAO().getGenericPreferences().getAllowdecimalsforqty());
		return getForward("stockreorder");
	}

	public int getPageNumber(HttpServletRequest req) {
		int pageNum = 0;
		String pageval = req.getParameter("pageNum");
		if (pageval == null || pageval.equals("")) {
			pageval = "0";
			req.setAttribute("pageNumber", pageNum);
		} else {
			pageval = pageval.replaceAll("'", " ");
			pageval = pageval.trim();
			req.setAttribute("pageNumber", pageNum);
			int y = Integer.parseInt(pageval) - 1;
			y = y * pageSize;
			pageNum = y;
		}
		return pageNum;
	}

	public Map getFiltersFromParamMap(HttpServletRequest req) throws ParseException{
		HashMap<String, Map> filterMap = new HashMap<String, Map>();
		HashMap<String, Object> typeValueMap = new HashMap<String, Object>();
		String typeKey = "type";
		String valueKey = "value";
		String salesQty = req.getParameter("sales_quantity");
		if (salesQty != null && !salesQty.equals("")) {
			typeValueMap.put(typeKey, new Integer[] { QueryBuilder.INTEGER });
			typeValueMap.put(valueKey, Integer.parseInt(salesQty));
			filterMap.put("salesQty", typeValueMap);
			typeValueMap = new HashMap<String, Object>();
		}

		String salesDays = req.getParameter("sale_days");
		if (salesDays != null && !salesDays.equals("")) {
			typeValueMap.put(typeKey, new Integer[] { QueryBuilder.INTEGER });
			typeValueMap.put(valueKey, Integer.parseInt(salesDays));
			filterMap.put("salesDays", typeValueMap);
			typeValueMap = new HashMap<String, Object>();
		}

		String serviceGroup = req.getParameter("service_group_id");
		if (serviceGroup != null && !serviceGroup.equals("")) {
			typeValueMap.put(typeKey, new Integer[] { QueryBuilder.INTEGER });
			typeValueMap.put(valueKey, Integer.parseInt(serviceGroup));
			filterMap.put("serviceGroup", typeValueMap);
			typeValueMap = new HashMap<String, Object>();
		}

		String serviceSubGroup = req.getParameter("service_sub_group_id");
		if (serviceSubGroup != null && !serviceSubGroup.equals("")) {
			typeValueMap.put(typeKey, new Integer[] { QueryBuilder.INTEGER });
			typeValueMap.put(valueKey, Integer.parseInt(serviceSubGroup));
			filterMap.put("serviceSubGroup", typeValueMap);
			typeValueMap = new HashMap<String, Object>();
		}

		String excludePO = req.getParameter("exclude_poitem");
		if (excludePO != null && !excludePO.equals("")) {
			typeValueMap.put(typeKey, new Integer[] { QueryBuilder.STRING });
			typeValueMap.put(valueKey, "Y");
			filterMap.put("excludePO", typeValueMap);
			typeValueMap = new HashMap<String, Object>();
		}

		String preferredSupplier = req.getParameter("preferred_supplier");
		if (preferredSupplier != null && !preferredSupplier.equals("")) {
			typeValueMap.put(typeKey, new Integer[] { QueryBuilder.STRING });
			typeValueMap.put(valueKey, preferredSupplier);
			filterMap.put("preferredSupplier", typeValueMap);
			typeValueMap = new HashMap<String, Object>();
		}
		String purchaseDate = req.getParameter("purchase_flag_date_dt");
		if (purchaseDate != null && !purchaseDate.equals("")) {
			typeValueMap.put(typeKey, new Integer[] { QueryBuilder.DATE });
			typeValueMap.put(valueKey, DateUtil.parseDate(purchaseDate));
			filterMap.put("purchaseDate", typeValueMap);
			typeValueMap = new HashMap<String, Object>();
		}

		List stores = getParamAsList(req.getParameterMap(), "selected_stores");
		if (stores != null && !stores.isEmpty()) {
			Integer[] typeArray = new Integer[stores.size()];
			for (int i = 0; i < stores.size(); i++) {
				typeArray[i] = QueryBuilder.INTEGER;
				String storesString = (String) stores.get(i);
				stores.set(i, Integer.parseInt(storesString));
			}
			typeValueMap.put(typeKey, typeArray);
			typeValueMap.put(valueKey, stores);
			filterMap.put("stores", typeValueMap);
			typeValueMap = new HashMap<String, Object>();
		}

		return filterMap;
	}

	public List<String> getParamAsList(Map request, String param) {
		return ConversionUtils.getParamAsList(request, param);
	}

	private ActionForward getForward(String forward) {
		StringBuilder path = new StringBuilder("/pages/stores/StockReorder/");

		if (forward.equals("stockreorder")) {
			path.append("StockReorder.jsp");
		}
		return new ActionForward(path.toString());
	}

	/**
	 * for item details pop-up
	 */
	@IgnoreConfidentialFilters
	public  ActionForward getItemDetails(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws IOException, SQLException, ParseException, ServletException{

		int itemId = Integer.parseInt(request.getParameter("itemId"));
		String storesList = request.getParameter("storeId");
		List<Integer> storeIds = new ArrayList<Integer>();
		String[] stores = storesList.split(",");
		for (String store : stores) {
		   storeIds.add(Integer.valueOf(store));
	    }
		List allStores = getAllStoresOfCenter(storeIds);
		List<BasicDynaBean> itemlist = StockReorderDAO.getItemDetails(itemId, allStores);

		JSONSerializer js = new JSONSerializer().exclude("class");
		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(itemlist)));
		response.flushBuffer();

		return null;

	}
	/** This method takes the stores list and for each store it first find the center id and then for this center
	   it will get all the stores. 
	*/
	
	public List getAllStoresOfCenter(List<Integer> storesList) throws ServletException, SQLException, IOException{
		List centerStores = new ArrayList<Integer>();
		Set<Integer> centerIds = new HashSet<Integer>();
		
		for(int i = 0; i < storesList.size(); i++){
			BasicDynaBean centerIdBean = UserDAO.getCenterId(storesList.get(i));
			int centerId = (Integer)centerIdBean.get("center_id");
			centerIds.add(centerId);
		}
		Iterator<Integer> iterator = centerIds.iterator(); 
		while (iterator.hasNext()){
		   centerStores.addAll(new StockEntryAction().getAllStores(iterator.next()));
		}
			
		return centerStores;
	}

	@IgnoreConfidentialFilters
	public ActionForward getHelpPage(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) {
		return m.findForward("help");
	}

	protected static final String[] reorderHeaderColumns = {"Item","Qty Available","Danger Level","Minimum Level","Reorder Level",
		"Indent Pending", "Flagged", "PO Raised", "Consumption",
		"Preferred Supplier","Order Qty", "Order Package", "Cost Price", "Stock Qty Below Min Level", "Order Qty Above Max Level"};

	public static List getHeaderNamesForCSV() {
		return Arrays.asList(reorderHeaderColumns) ;
	}

	public List<BasicDynaBean> getReorderListForCSV(HttpServletRequest req)
		throws IOException, SQLException, Exception {
		JSONSerializer js = new JSONSerializer().exclude("class");
		List<BasicDynaBean> reorderList = (List)getReorderList(req, false);
		return reorderList;
	}

	@IgnoreConfidentialFilters
	public ActionForward exportReorderDetailsInCSV(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, java.io.IOException, Exception {
		String[] reorderHeaders = (String[]) (getHeaderNamesForCSV()).toArray(new String[0]);
		List<BasicDynaBean> reorderList = getReorderListForCSV(req);
		List<BasicDynaBean> itemLatestCPList = new StockReorderDAO().getLatestCostPrice(reorderList);
		Map mapofMedicineItsCPbean = ConversionUtils.listBeanToMapBean(itemLatestCPList, "medicine_id");
		String decimalsAllowed = new GenericPreferencesDAO().getGenericPreferences().getAllowdecimalsforqty();
		res.setHeader("Content-type","application/csv");
		res.setHeader("Content-disposition","attachment; filename=StockReorder.csv");
		res.setHeader("Readonly","true");
		CSVWriter writer = new CSVWriter(res.getWriter(),CSVWriter.DEFAULT_SEPARATOR);
		writer.writeNext(reorderHeaders);
		writer.flush();
		int outerIndex =0;
		Connection con = null;
		BigDecimal itemLatestCP = null;
		for ( BasicDynaBean b: reorderList ) {
			List<String> csvValueList = new ArrayList<String>();
			csvValueList.add(String.valueOf(b.get("medicine_name")));
			csvValueList.add(String.valueOf(b.get("availableqty")== null? "0":b.get("availableqty")));
			csvValueList.add(String.valueOf(b.get("danger_level")== null? "0":b.get("danger_level")));
			csvValueList.add(String.valueOf(b.get("min_level")== null? "0":b.get("min_level")));
			csvValueList.add(String.valueOf(b.get("reorder_level")== null? "0":b.get("reorder_level")));
			csvValueList.add(String.valueOf(b.get("indentqty")== null? "0":b.get("indentqty")));
			csvValueList.add(String.valueOf(b.get("flaggedqty")== null? "0":b.get("flaggedqty")));
			csvValueList.add(String.valueOf(b.get("poqty")== null? "0":b.get("poqty")));
			csvValueList.add(String.valueOf(b.get("consumedqty")== null? "NA":b.get("consumedqty")));
			csvValueList.add(String.valueOf(b.get("pref_supplier_name")== null? "": b.get("pref_supplier_name")));
			if(decimalsAllowed.equalsIgnoreCase("Y")) {
				csvValueList.add(String.valueOf(b.get("ord_qty")));
				csvValueList.add(String.valueOf((((BigDecimal)b.get("ord_qty")).divide((BigDecimal)b.get("pkg_size"), BigDecimal.ROUND_HALF_UP)).setScale(0, BigDecimal.ROUND_CEILING).toString()));
			} else {
				csvValueList.add(String.valueOf(((BigDecimal)b.get("ord_qty")).setScale(0, BigDecimal.ROUND_CEILING)));
				BigDecimal ordQty = ((BigDecimal)b.get("ord_qty")).setScale(0, BigDecimal.ROUND_CEILING);
				csvValueList.add(String.valueOf( ((ordQty).divide((BigDecimal)b.get("pkg_size"),5,BigDecimal.ROUND_HALF_UP)).setScale(0, BigDecimal.ROUND_CEILING).toString()));
			}

			if ( mapofMedicineItsCPbean != null && mapofMedicineItsCPbean.get(b.get("item_id")) != null){
				itemLatestCP = (BigDecimal)((BasicDynaBean)mapofMedicineItsCPbean.get(b.get("item_id"))).get("cost_price");
			}
			csvValueList.add(String.valueOf(itemLatestCP));
			csvValueList.add(String.valueOf(b.get("below_min_level").equals("Y")? "Yes": "No"));
			csvValueList.add(String.valueOf(b.get("above_max_level").equals("Y")? "Yes": "No"));
			String[] csvValues = null;
			csvValues = csvValueList.toArray(new String[csvValueList.size()]);
			writer.writeNext(csvValues);
			writer.flush();
			outerIndex++;
		}
		writer.flush();
		writer.close();
		return null;
	}
	
	
}