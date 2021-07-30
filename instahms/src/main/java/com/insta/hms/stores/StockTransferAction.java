package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.NumberToWordFormat;
import com.insta.hms.cssd.SurgeryKitAvailabilityDao;
import com.insta.hms.integration.scm.inventory.ScmOutBoundInvService;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.modules.ModulesDAO;
import com.lowagie.text.DocumentException;
import flexjson.JSONSerializer;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

public class StockTransferAction extends BaseAction{

	static Logger log = LoggerFactory.getLogger(StockTransferAction.class);
	static JSONSerializer js = new JSONSerializer().exclude("class");
    private static final GenericDAO storeTransferDetailsDAO = new GenericDAO("store_transfer_details");
    private static final GenericDAO storeTransferMainDAO = new GenericDAO("store_transfer_main");

	private static final String ITEM_DETAIL_LIST_QUERY = "SELECT i.medicine_id,c.identification,i.medicine_name,i.cust_item_code," +
				" (CASE WHEN c.issue_type='C'  THEN 'CONSUMABLE' WHEN c.issue_type='L'  THEN 'REUSABLE' " +
				" WHEN C.ISSUE_TYPE='P'  THEN 'PERMANENT' WHEN C.iSSUE_type='R' THEN 'RETAIL' END )AS issue_type,i.item_barcode_id," +
				" COALESCE(i.issue_units,'') AS issue_units, sibd.batch_no, sibd.exp_dt, i.package_type, s.qty, s.consignment_stock, " +
				"i.issue_base_unit,s.dept_id,sibd.mrp, i.package_uom,sibd.item_batch_id " +
			    " FROM " +
			    " ( SELECT medicine_id,sum(qty) as qty,item_batch_id,batch_no,sum(qty_in_use) as qty_in_use,sum(qty_maint) as qty_maint, " +
			    "	 sum(qty_retired) as qty_retired,sum(qty_lost) as qty_lost,sum(qty_kit) as qty_kit, " +
	            " 	sum(qty_unknown) as qty_unknown,consignment_stock,asset_approved,dept_id,min(tax) as tax " +
	            " 	FROM store_stock_details  GROUP BY batch_no,item_batch_id,medicine_id,consignment_stock,asset_approved,dept_id " +
	            "   ORDER BY medicine_id" +
	            ") as s " +
			    " JOIN store_item_batch_details sibd USING(item_batch_id) " +
			    " JOIN store_item_details i ON (i.medicine_id = s.medicine_id) " +
			    " JOIN store_category_master c ON(c.category_id =i.med_category_id) ";
	private static final String ITEM_DETAIL_LIST_QUERY_WHERE = " WHERE s.qty>0 AND s.asset_approved='Y'";

	private static ScmOutBoundInvService scmOutService = ApplicationContextProvider
      .getBean(ScmOutBoundInvService.class);

	@IgnoreConfidentialFilters
	public ActionForward show(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res) throws Exception{
		Connection con = null;
		String msg = req.getParameter("message");
		if (msg != null) {
			req.setAttribute("message",msg);
		}else{
			req.setAttribute("message","show");
		}

		String stockType = am.getProperty("stock_type");

		HttpSession session = req.getSession(false);
		String dept_id = (String)session.getAttribute("pharmacyStoreId");
		int roleId = (Integer)session.getAttribute("roleId");
		if(dept_id != null && !dept_id.equals("")){
			BasicDynaBean dept = new GenericDAO("stores").findByKey("dept_id", Integer.parseInt(dept_id));
			String dept_name = dept.get("dept_name").toString();
			req.setAttribute("dept_id", dept_id);
			req.setAttribute("dept_name", dept_name);
		}
		if(dept_id != null && dept_id.equals("")) {
			req.setAttribute("dept_id", dept_id);
		}
		if(roleId == 1 || roleId ==2) {
			if(dept_id != null && !dept_id.equals("") )
				req.setAttribute("dept_id", dept_id);
			else
				req.setAttribute("dept_id", 0);
		}
		try{
		con = DataBaseUtil.getReadOnlyConnection();
		List groupItemDetails =  null;
		String transaction = req.getParameter("_transaction");
		if(transaction != null){
			List<HashMap> items = new ArrayList();
			HashMap itemMap = null;
			String storeId = req.getParameter("dept_id");

			Map<String, String[]> params = req.getParameterMap();
			String[] selected = (String[])params.get("_selected");
			String[] medicineId = (String[])params.get("_medicine_id");
			String[] deptId = (String[])params.get("_dept_id");
			String[] batchNo = (String[]) params.get("_batchno");

			for( int i=0; i<medicineId.length; i++ ){
				if ( selected[i].equals("Y")) {
    				itemMap = new HashMap();itemMap.put("itemId", medicineId[i]);itemMap.put("itemIdentifier", batchNo[i]);
    				items.add(itemMap);
				}
			}

			BasicDynaBean storeBean = StoreDAO.findByStore(Integer.parseInt(deptId[0]));
			groupItemDetails = StockUserIssueDAO.getGroupItemDetails(ITEM_DETAIL_LIST_QUERY,deptId[0], items, (Integer)storeBean.get("store_rate_plan_id"));
			req.setAttribute("groupStoreId", deptId[0]);
			req.setAttribute("groupItemDetails",js.serialize(ConversionUtils.copyListDynaBeansToMap(groupItemDetails)));
		}else{
			req.setAttribute("groupItemDetails", js.serialize(groupItemDetails));
		}

		if ( req.getParameter("is_sterile_store") != null && req.getParameter("kit_id") != null ){//kit transfer
			req.setAttribute("kit_details_json",js.serialize(
					ConversionUtils.copyListDynaBeansToMap(new SurgeryKitAvailabilityDao().
							getKitDetals(Integer.parseInt(req.getParameter("kit_id"))))));

		}
		req.setAttribute("stock_timestamp",MedicineStockDAO.getMedicineTimestamp());
		req.setAttribute("msg", req.getParameter("msg"));
		req.setAttribute("flag", req.getParameter("flag"));
		req.setAttribute("is_sterile_store", req.getParameter("is_sterile_store"));
		req.setAttribute("ot_store", req.getParameter("ot_store"));
		req.setAttribute("to_sterile_store", stockType != null && stockType.equals("sterile"));
		List<String> orderKitDetailsList = new ArrayList<String>();
        orderKitDetailsList.add("order_kit_id");
        orderKitDetailsList.add("order_kit_name");
        req.setAttribute("orderkitJSON", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(new GenericDAO("order_kit_main").listAll(orderKitDetailsList, "status","A"))));
		return am.findForward("addshow");
		}finally{
			if(con != null)con.close();
		}
	}
	public ActionForward create(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res) throws Exception{
		Connection con = null;
		Map itemsmap = req.getParameterMap();
		String[] item_id = (String[])itemsmap.get("item_id");
		String[] item_name = (String[])itemsmap.get("item_name");
		String[] item_identifier = (String[])itemsmap.get("itemidentifier");
		String[] dept_from = (String[])itemsmap.get("from_store");
		String[] dept_to = (String[])itemsmap.get("to_store");
		String[] reference = (String[])itemsmap.get("reason");
		String[] qty = (String[])itemsmap.get("trannsfer_qty");
		String[] date = (String[])itemsmap.get("transferDate");
		String[] deleted = (String[]) itemsmap.get("hdeleted");
		String[] itemUnit = (String[]) itemsmap.get("itemUnit");
		String[] trnPkSize = (String[]) itemsmap.get("pkgSize");
		String[] itemBatchId = (String[])itemsmap.get("item_batch_id");
		String[] description = (String[])itemsmap.get("description");
		BasicDynaBean prefs = new GenericPreferencesDAO().getAllPrefs();
		String msg = "";
		BasicDynaBean stock_transfer,store_transfer_main = null;
		boolean sucess = true;
		HttpSession session = req.getSession();
		String username = (String) session.getAttribute("userid");
		ActionRedirect redirect = new ActionRedirect ("stocktransfer.do?_method=show");
		String appointment_id = getParameter(itemsmap, "appointment_id");
		FlashScope flash = FlashScope.getScope(req);

		if ( req.getParameter("kitissue").equals("true") ) {
			redirect = new ActionRedirect ("/cssd/SurgeryKitAvailability.do?_method=list");
			redirect.addParameter("appointment_date", "today");
			redirect.addParameter("sortOrder", "appointment_date");
			redirect.addParameter("sortReverse", false);
			redirect.addParameter("issue_status", "N");
		}

		if (getParameter(itemsmap,"to_sterile_store") != null && getParameter(itemsmap,"to_sterile_store").equals("true") ) {
			redirect = new ActionRedirect ("/pages/cssd/SterileStockTransfer.do?_method=show&is_sterile_store=N");

		}
		int  newtransferId  = 0, newTransferDetId = 0;
		int len = item_id.length-1;

		StockFIFODAO stockFIFODAO = new StockFIFODAO();
		GenericDAO stockDetDAO = new GenericDAO("store_stock_details");
		ArrayList<String> failedItems = new ArrayList<String>();;
		List<Map<String, Object>> cacheTransferTxns = new ArrayList<>();

		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);int k = 0;
			ModulesDAO modules = new ModulesDAO();
      BasicDynaBean module = modules.findByKey("module_id", "mod_scm");

			for(int i =0 ;i < len;i++){
				if (deleted[i].equalsIgnoreCase("false")) {
					//setting properties for inventory_stock_transfer

					boolean qtyAvailable = EditStockDetailsDAO.checkQtyAvailable(con,dept_from[0],item_id[i],Integer.parseInt(itemBatchId[i]), new BigDecimal(qty[i]));

					if(!qtyAvailable){

						con.rollback();
						redirect.addParameter("flag", false);
						redirect.addParameter("msg", "Stock not available for : "+item_name[i]+"  -  "+item_identifier[i]+"");
						return redirect;

					}

					//generating transfer number for this transfer
					if (len > 0 && i == 0)
						newtransferId = Integer.parseInt((new StockUserIssueDAO(con).getSequenceId("stock_transfer_seq")));
					stock_transfer = storeTransferDetailsDAO.getBean();
					stock_transfer.set("transfer_no", newtransferId);
					stock_transfer.set("medicine_id", Integer.parseInt(item_id[i]));
					stock_transfer.set("qty", new BigDecimal(qty[i]));
					stock_transfer.set("item_unit", itemUnit[i]);
					stock_transfer.set("trn_pkg_size", new BigDecimal(trnPkSize[i]));
					stock_transfer.set("item_batch_id", Integer.parseInt(itemBatchId[i]));
					stock_transfer.set("description", description[i]);
					BigDecimal transferredQty = new BigDecimal(qty[i]);


	//				setting properties for inventory_store_transfer_main
					store_transfer_main = storeTransferMainDAO.getBean();
					store_transfer_main.set("transfer_no", newtransferId);
					store_transfer_main.set("date_time",  DataBaseUtil.parseTimestamp(date[0]+" "+DataBaseUtil.getStringValueFromDb("select * from localtime(0)")));
					store_transfer_main.set("store_from", Integer.parseInt(dept_from[0]));
					store_transfer_main.set("store_to", Integer.parseInt(dept_to[0]));
					store_transfer_main.set("reason", reference[0]);
					store_transfer_main.set("username", req.getSession(false).getAttribute("userid"));
					store_transfer_main.set("appointment_id", appointment_id != null && !appointment_id.isEmpty() ? Integer.parseInt(appointment_id) : null);

					newTransferDetId = storeTransferDetailsDAO.getNextSequence();
					stock_transfer.set("transfer_detail_no", newTransferDetId);

					if(sucess && k == 0){
						if(storeTransferMainDAO.insert(con, store_transfer_main)){
							sucess = true;k = 1;
						}else sucess = false;
					}

					if(sucess){

						/**
						 * 1.Reduce stock by FIFO from FROM store.
						 * 2.insert into transfer details
						 * 3.Add stock to TO store
						 *
						 */

							if(sucess){

								HashMap<String, Object> stockKeys = new HashMap<String, Object>();
								stockKeys.put("item_batch_id", Integer.parseInt(itemBatchId[i]));
								stockKeys.put("dept_id", Integer.parseInt(dept_to[0]));
								//this is actual stock of that item batch,store,lot
								BasicDynaBean stockBean = stockDetDAO.findByKey(con,new ArrayList<String>(),stockKeys);
								Map statusMap = null;


								if(stockBean != null){

//									reducing stock
									statusMap = stockFIFODAO.reduceStock(con,Integer.parseInt(dept_from[0]),Integer.parseInt(itemBatchId[i]), "T",
											 new BigDecimal(qty[i]),null,(String)req.getSession(false).getAttribute("userid"),"StockTransfer",newTransferDetId);
									sucess &= (Boolean)statusMap.get("status");
									if(!(Boolean)statusMap.get("status") && statusMap.get("statusReason") != null){
										transferredQty = transferredQty.subtract((BigDecimal)statusMap.get("left_qty"));
										failedItems.add((String)statusMap.get("statusReason") + " for item <b>" +item_name[i] +"</b>, Actual quantity is "+qty[i]+ "  Transfered Qty is "+transferredQty.toString());
										sucess = true;//let next item transfer
									}

									//add stock
									statusMap = stockFIFODAO.addStock(con,Integer.parseInt(dept_to[0]),newTransferDetId,"T",
											new BigDecimal(qty[i]),(String)req.getSession(false).getAttribute("userid"),"StockTransfer",Integer.parseInt(dept_from[0]));

									sucess &= (Boolean)statusMap.get("status");
									stock_transfer.set("cost_value", statusMap.get("costValue"));

									if (sucess && ((String)(prefs.get("show_central_excise_duty"))).equals("Y")) {
										sucess = new StockUserReturnDAO(con).updateStock(Integer.parseInt(dept_to[0]),Integer.parseInt(dept_from[0]),item_identifier[i],Integer.parseInt(item_id[i]));
									}
								}else{

									//transfer to new store
									statusMap = stockFIFODAO.transferStock(con, Integer.parseInt(item_id[i]),Integer.parseInt(itemBatchId[i]),
											Integer.parseInt(dept_from[0]),Integer.parseInt(dept_to[0]),new BigDecimal(qty[i]),username,newTransferDetId,"N");

//									set cost value of reduces stock
									stock_transfer.set("cost_value", statusMap.get("costValue"));
									msg = statusMap.get("statusReason") != null ? (String)statusMap.get("statusReason") : "";
									sucess = (Boolean)statusMap.get("status");

									if(!(Boolean)statusMap.get("status") && statusMap.get("statusReason") != null){
										transferredQty = transferredQty.subtract((BigDecimal)statusMap.get("left_qty"));
										failedItems.add((String)statusMap.get("statusReason") + " for item <b>" +item_name[i] +"</B>, Actual quantity is "+qty[i]+ "  Transfered Qty is "+transferredQty.toString());
										sucess = true;//let next item transfer
									}

								}
							}

							//transfer details
							stock_transfer.set("qty", transferredQty);;
							sucess &=  storeTransferDetailsDAO.insert(con, stock_transfer);

							// Scm export of Transfer
			        if (module != null &&
			            ((String)module.get("activation_status")).equals("Y")) {
			          cacheStockTransfer(store_transfer_main, stock_transfer, cacheTransferTxns);
			        }
					}
			    }
			}
			if(sucess){
				msg = "Successfully Transferred Items";
				if( req.getParameter("kitissue").equals("false") )
					redirect.addParameter("msg", msg);
				con.commit();
        if (!cacheTransferTxns.isEmpty() && module != null &&
            ((String)module.get("activation_status")).equals("Y")) {
          scmOutService.scheduleStockTransferTxns(cacheTransferTxns);
        }
				//update stock timestamp
				stockFIFODAO.updateStockTimeStamp();
				stockFIFODAO.updateStoresStockTimeStamp(Integer.parseInt(dept_to[0]));
			}else {
				flash.put("error", msg);
				msg = msg.isEmpty() ? "Failed to Transfer  " : msg;
				redirect.addParameter("msg", msg);
				redirect.addParameter("flag", sucess);
				con.rollback();
			}
		}finally{
			if(con != null)con.close();
		}
		if(sucess && newtransferId!= 0 &&  req.getParameter("kitissue").equals("false") )
			redirect.addParameter("message", newtransferId);
		if ( req.getParameter("kitissue").equals("false") )
			redirect.addParameter("store", "0");
		String insuffInfo = "";
		for(String failedItem:failedItems){
			insuffInfo = insuffInfo.concat(failedItem).concat("</br>");
		}
		flash.put("info", insuffInfo);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;

	}

	@IgnoreConfidentialFilters
	public ActionForward getItemDetails(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res) throws Exception{
			Connection con = null;
			try{
				con = DataBaseUtil.getConnection();
				res.setContentType("text/plain");
				res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
                String itemName = req.getParameter("item_name");
                String storeId = req.getParameter("storeid");
				res.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(
						new StockUserIssueDAO(con).getItemDetails(
								ITEM_DETAIL_LIST_QUERY+ITEM_DETAIL_LIST_QUERY_WHERE+" and medicine_name=? and dept_id=? ",itemName,storeId))));
				res.flushBuffer();
		        return null;
			}finally{
				if(con != null)con.close();
			}
	}

	@IgnoreConfidentialFilters
	public ActionForward getStockTransferPrint(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws SQLException, IOException,TransformerException,
						TemplateException, DocumentException, XPathExpressionException, ParseException {

		String transferNo = request.getParameter("transfer_no");
		Template t = null;

		Map map = new HashMap();
		map.put("hospitalName", request.getSession(false).getAttribute("sesHospitalId"));
		map.put("stocktransferdetails", StockUserIssueDAO.getStockTransferDetails(Integer.parseInt(transferNo)));
		map.put("NumberToStringConversion", NumberToWordFormat.wordFormat());

		BasicDynaBean printprefs = PrintConfigurationsDAO.getPageOptions(
				PrintConfigurationsDAO.PRINT_TYPE_STORE);

		PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
		FtlReportGenerator fGen = null;

		String templateContent = printtemplatedao.getCustomizedTemplate(PrintTemplate.Stock_Transfer_print);
		if (templateContent == null || templateContent.equals("")) {
			fGen = new FtlReportGenerator("StockTransferPrint");
			//t = AppInit.getFmConfig().getTemplate(PrintTemplate.Stock_Transfer_print.getFtlName() + ".ftl");
		} else
		{
			StringReader reader = new StringReader(templateContent);
			fGen = new FtlReportGenerator("StockTransferPrint",reader);
			//t = new Template("StockTransferPrint.ftl", reader, AppInit.getFmConfig());
		}

//		StringWriter writer = new StringWriter();
//		t.process(map,writer);
		String printContent = fGen.getPlainText(map);
		//String printContent = writer.toString();
		HtmlConverter hc = new HtmlConverter();
		if (printprefs.get("print_mode").equals("P")) {
			OutputStream os = response.getOutputStream();
			response.setContentType("application/pdf");
			hc.writePdf(os, printContent, "stocktransfer", printprefs, false, false, true, true, true, false);
			}else {
				String textReport = null;
				textReport = new String(hc.getText(printContent, "stocktransferText", printprefs, true, true));
				request.setAttribute("textReport", textReport);
				request.setAttribute("textColumns", printprefs.get("text_mode_column"));
				request.setAttribute("printerType", "DMP");
				return mapping.findForward("textPrintApplet");
			}
	
		return null;
	}
	
	 /**
	  * This method is use to get orderkit items with batches.
	  *
	  */
	@IgnoreConfidentialFilters
	 public ActionForward getOrderKitItemsJSON(ActionMapping mapping, ActionForm form,
	            HttpServletRequest request, HttpServletResponse res)
	        throws Exception {

	   res.setContentType("text/javascript");
       res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
       Map<String, Object> orderKitDetailsMap = new HashMap<String, Object>();
       
       int orderKitId = Integer.parseInt(request.getParameter("order_kit_id"));
       int deptId = Integer.parseInt(request.getParameter("storeId"));
       		
       JSONSerializer js = new JSONSerializer().exclude("class");
       //to get all order kit items
       List<BasicDynaBean> orderKitItems = StockUserIssueDAO.getOrderKitItemsDetails(orderKitId);
       
       //list of available medicines in stock
       List<BasicDynaBean> availMedicineList = new ArrayList<BasicDynaBean>();
       
       //to get all items status available in store
       Map<Integer, String> orderKitItemsStatus = StockUserIssueDAO.getOrderKitItemsStockStatus(deptId, orderKitId, null);
       
       Map<String, String> summarizedOrderKitItemsStatusMap = new LinkedHashMap<String, String>();
       List<Integer> medicineList = new ArrayList<Integer>();
       
       Iterator<BasicDynaBean> medicineListIterator = orderKitItems.iterator();
       
       int unavailableMedicineCount = 0;
       
		while(medicineListIterator.hasNext()) {
			BasicDynaBean medicineBean = medicineListIterator.next();
			Integer medicineId = (Integer)medicineBean.get("medicine_id");
			BigDecimal qtyNeeded = (BigDecimal)medicineBean.get("qty_needed");
			String medicineName = (String)medicineBean.get("medicine_name");
			if(orderKitItemsStatus.containsKey(medicineId)) {
				String medicineStockStatus = orderKitItemsStatus.get(medicineId);
				String medicineStatus[] = medicineStockStatus.split("@");
				Double inStockQty = Double.parseDouble(medicineStatus[0]);
				if(inStockQty > 0)
					availMedicineList.add(medicineBean);
				if(inStockQty < qtyNeeded.doubleValue())
					unavailableMedicineCount++;
				summarizedOrderKitItemsStatusMap.put(medicineName, orderKitItemsStatus.get(medicineId));
			} else {
				summarizedOrderKitItemsStatusMap.put(medicineName, "0@"+qtyNeeded);
				unavailableMedicineCount++;
			}
			medicineList.add(medicineId);
			
		}
		
		orderKitDetailsMap.put("medBatches", ConversionUtils.listBeanToMapListMap(getOrderKitItemDetails(orderKitId, deptId), "medicine_id"));
		orderKitDetailsMap.put("order_kit_items_status", summarizedOrderKitItemsStatusMap);
		orderKitDetailsMap.put("order_kit_items", ConversionUtils.copyListDynaBeansToMap(availMedicineList));
		orderKitDetailsMap.put("total_items_status", unavailableMedicineCount+"@"+orderKitItems.size());
		res.getWriter().write(js.deepSerialize(orderKitDetailsMap));
       res.flushBuffer();
       return null;
   } 
	 
   private List<BasicDynaBean> getOrderKitItemDetails(int orderKitId, int deptId) throws SQLException {
	   String orderKitMedicinesBatchQuery = "SELECT i.medicine_id,c.identification,i.medicine_name, "
	   		+ "(CASE WHEN c.issue_type='C'  THEN 'CONSUMABLE' WHEN c.issue_type='L'  THEN 'REUSABLE'  WHEN C.ISSUE_TYPE='P'  THEN 'PERMANENT' "
	   		+ "WHEN C.iSSUE_type='R' THEN 'RETAIL' END )AS issue_type,i.item_barcode_id, COALESCE(i.issue_units,'') AS issue_units, "
	   		+ "sibd.batch_no, sibd.exp_dt, i.package_type, s.qty, s.consignment_stock, i.issue_base_unit,"
	   		+ "s.dept_id,sibd.mrp, i.package_uom,sibd.item_batch_id  "
	   		+ "FROM  ( SELECT medicine_id,sum(qty) as qty,item_batch_id,batch_no,sum(qty_in_use) as qty_in_use,sum(qty_maint) as qty_maint,"
	   		+ "	sum(qty_retired) as qty_retired,sum(qty_lost) as qty_lost,sum(qty_kit) as qty_kit,sum(qty_unknown) as qty_unknown,"
	   		+ "consignment_stock,asset_approved,dept_id,min(tax) as tax FROM store_stock_details "
	   		+ "GROUP BY batch_no,item_batch_id,medicine_id,consignment_stock,asset_approved,dept_id ORDER BY medicine_id) as s "
	   		+ " JOIN store_item_batch_details sibd USING(item_batch_id)  JOIN store_item_details i ON (i.medicine_id = s.medicine_id)  "
	   		+ " JOIN store_category_master c ON(c.category_id =i.med_category_id) JOIN order_kit_details okd ON (okd.medicine_id = s.medicine_id) "
	   		+ " WHERE s.qty>0 AND s.asset_approved='Y' and order_kit_id = ? and dept_id=? ";
	   
	   	PreparedStatement ps = null;
	   	Connection con = DataBaseUtil.getReadOnlyConnection();
		try{
			ps = con.prepareStatement(orderKitMedicinesBatchQuery);
			ps.setInt(1, orderKitId);
			ps.setInt(2, deptId);
			return DataBaseUtil.queryToDynaList(ps);
		}
		finally{
			DataBaseUtil.closeConnections(con, ps);
		}
   }

   private void cacheStockTransfer(BasicDynaBean transferMain, BasicDynaBean transferDetails,
       List<Map<String,Object>> cacheTransferTxns) {
     Map<String, Object> data = scmOutService.getStockTransferMap(transferMain, transferDetails);
     if(!data.isEmpty()) {
       cacheTransferTxns.add(data);
     }
     
   }


}