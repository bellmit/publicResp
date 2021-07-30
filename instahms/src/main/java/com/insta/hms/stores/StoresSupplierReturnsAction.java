/**
 *
 */
package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.core.inventory.StoresHelper;
import com.insta.hms.integration.scm.grn.SupplierReturnsCsvAdapter;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.StoreMaster.StoreMasterDAO;
import com.insta.hms.usermanager.UserDAO;
import flexjson.JSONSerializer;
import freemarker.template.Template;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author fazil.md
 *
 */
public class StoresSupplierReturnsAction extends DispatchAction {

    static Logger log = LoggerFactory.getLogger(StoresSupplierReturnsAction.class);
	static final SupplierReturnsCsvAdapter CSV_ADAPTER =
			ApplicationContextProvider.getApplicationContext()
					.getBean(SupplierReturnsCsvAdapter.class);
	
    private static final GenericDAO storeGrnTaxDetailsDAO = new GenericDAO("store_grn_tax_details");
    private static final GenericDAO storeGatePassDAO = new GenericDAO("store_gatepass");
    private static final GenericDAO storeSupplierReturnsMainDAO =
        new GenericDAO("store_supplier_returns_main");
    
    private static final GenericDAO storesDAO = new GenericDAO("stores");
    private static final GenericDAO storeSupplierReturnsDAO =
        new GenericDAO("store_supplier_returns");
    private static final GenericDAO storeGrnMainDAO = new GenericDAO("store_grn_main");

    @IgnoreConfidentialFilters
    public ActionForward list(ActionMapping m,ActionForm f,HttpServletRequest req,
			HttpServletResponse res) throws IOException ,SQLException, Exception{
		try {
			req.setAttribute("medicine_timestamp",MedicineStockDAO.getMedicineTimestamp());
			JSONSerializer js = new JSONSerializer().exclude("class");

			HttpSession session = req.getSession(false);
			int centerId = (Integer)req.getSession().getAttribute("centerId");
			String dept_id = (String)session.getAttribute("pharmacyStoreId");
			int roleId = (Integer)session.getAttribute("roleId");
			if(dept_id!=null && !dept_id.equals("")){
				BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(dept_id));
				String dept_name = dept.get("dept_name").toString();
				req.setAttribute("dept_id", dept_id);
				req.setAttribute("dept_name", dept_name);
			}
			if(dept_id != null && dept_id.equals("")) {
				req.setAttribute("dept_id", dept_id);
			}
			if(roleId == 1 || roleId == 2) {
				if(dept_id!=null && !dept_id.equals(""))
					req.setAttribute("dept_id", dept_id);
				else
					req.setAttribute("dept_id", 0);
			}

			String transaction = req.getParameter("_transaction");
	 		List groupMedDetails =  null;

	 		if (req.getParameter("gtpass") != null) req.setAttribute("gtpass", req.getParameter("gtpass"));
            else req.setAttribute("gtpass", false);

	 		if(transaction != null){
	 			List medIds = new ArrayList();
	 			List batchNos = new ArrayList();
	 			List qty = new ArrayList();
	 			List indentno = new ArrayList();

	 			Map<String, String[]> params = req.getParameterMap();
				String[] selected = params.get("_selected");
				String[] medicineId = params.get("_medicine_id");
				String[] batchNo =  params.get("_batchno");
				String[] deptId =  params.get("_dept_id");

				for( int i=0; i<medicineId.length; i++ ){
					if ( selected[i].equals("Y")) {
		 				medIds.add(medicineId[i]);
		 				batchNos.add(batchNo[i]);
					}
	 			}

	 			groupMedDetails = MedicineStockDAO.getGroupMedDetails("D",deptId[0],medIds, batchNos);
	 			req.setAttribute("groupDeptId", deptId[0]);
	 			//req.setAttribute("suppName", StoresDBTablesUtil.suppIdToName(req.getParameter("supplier_code")));
	 			
	 			 
	 			req.setAttribute("type", req.getParameter("type"));
	 			req.setAttribute("groupMedDetails", js.serialize(ConversionUtils.listBeanToListMap(groupMedDetails)));
	 		}else
	 			req.setAttribute("groupMedDetails",js.serialize(groupMedDetails));
	 			req.setAttribute("suppName", StockReorderDAO.listAllcentersforAPo(centerId));
	 			req.setAttribute("listAllcentersforDebit", js.deepSerialize(PurchaseOrderDAO.listAllcentersforAPo(centerId)));

		}finally {

		}
		return m.findForward("list");
	}
    /**
     * Same as list method with variation in the query that lists all the items. For this query we also passing the
     * indent_no to get indent related details as well. This method is used when you come to Supplier Returns from
     * View Rejected Transfer Indents Dashboard
     * @param m
     * @param f
     * @param req
     * @param res
     * @return
     * @throws IOException
     * @throws SQLException
     * @throws Exception
     */
    @IgnoreConfidentialFilters
    public ActionForward listForReject(ActionMapping m,ActionForm f,HttpServletRequest req,
			HttpServletResponse res) throws IOException ,SQLException, Exception{
		try {
			req.setAttribute("medicine_timestamp",MedicineStockDAO.getMedicineTimestamp());
			JSONSerializer js = new JSONSerializer().exclude("class");
			
			String transaction = req.getParameter("_transaction");
	 		List groupMedDetails =  null;
	 		if(transaction != null){
	 			List medIds = new ArrayList();
	 			List batchNos = new ArrayList();
	 			List qty = new ArrayList();
	 			List indentno = new ArrayList();
	 			List deptIds = new ArrayList();
	 			String select[] = req.getParameterValues("_select");

	 			Map<String, String[]> params = req.getParameterMap();
				String[] selected = params.get("_selected");
				String[] medicineId = params.get("_medicine_id");
				String[] batchNo =  params.get("_batchno");
				String[] deptId =  params.get("_dept_id");
				String[] rejQty =  params.get("_qty");
				String[] indent =  params.get("_indentno");

				for( int i=0; i<medicineId.length; i++ ){
					if ( selected[i].equals("Y")) {
						medIds.add(medicineId[i]);
						batchNos.add(batchNo[i]);
						qty.add(rejQty[i]);
						indentno.add(indent[i]);
						deptIds.add(deptId[i]);
					}
	 			}

	 			if (indentno != null){
	 				groupMedDetails = StoresSupplierReturnsDAO.getGroupMedDetailsForRejIndents("D", deptId[0], medIds, batchNos, qty,indentno);
	 			}

	 			req.setAttribute("groupDeptId", deptId[0]);
	 			if (!deptId.equals("")){
	 				req.setAttribute("dept_id", deptId[0]);
	 				String deptName  = StoresIndentDAO.getStoreName( Integer.parseInt(deptId[0]));
	 				req.setAttribute("dept_name",deptName);
	 			}
	 			req.setAttribute("suppName", req.getParameter("supplier_code"));
	 			req.setAttribute("type", req.getParameter("type"));
	 			req.setAttribute("groupMedDetails", js.serialize(ConversionUtils.listBeanToListMap(groupMedDetails)));
	 		}else
	 			req.setAttribute("groupMedDetails",js.serialize(groupMedDetails));

		}finally {

		}
		return m.findForward("list");
	}




    @IgnoreConfidentialFilters
    public ActionForward getSupplierReturnScreen(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res) throws SQLException {
    	int centerId = (Integer) req.getSession().getAttribute("centerId");
		String msg = req.getParameter("msg");
		if (msg != null) {
			req.setAttribute("message",msg);
		}
		JSONSerializer js = new JSONSerializer().exclude("class");
		String invoiceList=DirectStockEntryDAO.getInvoiceList();
		req.setAttribute("supplierList", DirectStockEntryDAO.getSuuplierNamesInMaster(centerId));
		String storeId = (String)req.getParameter("dept_id");
		HashMap medicineNames = MedicineStockDAO.getMedicineNamesInStockByStore(storeId);
		req.setAttribute("medicineNamesJSON", js.serialize(medicineNames));
		req.setAttribute("invoiceList", invoiceList);
		return am.findForward("getSupplierReturnsScreen");
	}

  @IgnoreConfidentialFilters
	public ActionForward getStockJSON(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException, SQLException {
		
		String medicineName = req.getParameter("medicineName");
		String deptId =(String)req.getParameter("storeId");
		String supp = StoresDBTablesUtil.suppNameToId(req.getParameter("supp"));
		String retAgtSupp = GenericPreferencesDAO.getGenericPreferences().getReturnAgainstSpecificSupplier();
		String grnNo = (String)req.getParameter("grnNo");
		int centerId = (Integer) req.getSession().getAttribute("centerId");
		String healthAuthorityForCenter = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
		String healthAuthority = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthorityForCenter).getHealth_authority();


		if ( (medicineName==null) || medicineName.equals("") ) {
			log.error("getStockJSON: Medicine name is required");
			return null;
		}

		String medicineId = MedicineStockDAO.medicineNameToId(medicineName);
		if ( (medicineId==null) || medicineId.equals("") ) {
			log.error("getStockJSON: Medicine ID could not be resolved for " + medicineName);
			return null;
		}
		ArrayList<MedicineStock> stock;
		if (retAgtSupp.equals("N") && grnNo == null)
				stock = MedicineStockDAO.getMedicineStockInDeptWithSuppRates(medicineId, deptId,supp,healthAuthority);
		else {
			if (grnNo != null && !grnNo.isEmpty()){
				stock = MedicineStockDAO.getSelSuppMedicineStockInDept(medicineId,deptId,supp,grnNo,healthAuthority);
			} else
				stock = MedicineStockDAO.getSelSuppMedicineStockInDept(medicineId,deptId,supp,null,healthAuthority);
		}
		JSONSerializer js = new JSONSerializer().exclude("class");
		String stockJSON = js.serialize(stock);

        res.setContentType("text/plain");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.getWriter().write(stockJSON);
        res.flushBuffer();
        return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward getSuppMedDetails(ActionMapping m,ActionForm f,HttpServletRequest request,
			HttpServletResponse res) throws IOException ,SQLException, Exception{
		JSONSerializer js = new JSONSerializer().exclude("class");
		String storeId = request.getParameter("store");
		String suppId = StoresDBTablesUtil.suppNameToId(request.getParameter("supp"));
		List<BasicDynaBean> list = StoresSupplierReturnsDAO.getSuppMed(suppId,storeId);
		String medDetails = js.serialize(ConversionUtils.listBeanToListMap(list));
		res.setContentType("text/plain");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.getWriter().write(medDetails);
        res.flushBuffer();
		return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward getMedDetails(ActionMapping m,ActionForm f,HttpServletRequest request,
			HttpServletResponse res) throws IOException ,SQLException, Exception{
		JSONSerializer js = new JSONSerializer().exclude("class");
		String storeId = request.getParameter("store");
		String suppId = request.getParameter("suppid");
		String invoiceNo = request.getParameter("inv");
		ArrayList<String> list = StoresSupplierReturnsDAO.getMedDet(suppId, invoiceNo, storeId);
		String medDetails = js.serialize(list);
		res.setContentType("text/plain");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.getWriter().write(medDetails);
        res.flushBuffer();
		return null;
	}

	public ActionForward makeSupplierReturns(ActionMapping am, ActionForm af,
			HttpServletRequest request, HttpServletResponse res) throws SQLException {

		HttpSession session = request.getSession(false);
		Connection con = null;
		String message = null;
		int returnNo=0;
		int storeIdNum = -1;
		int medIdNum = -1;
		int gtpassId = 0;
		boolean status = true;
		Map<String, String[]> itemsMap = request.getParameterMap();
		String[] med_id =itemsMap.get("hmedId");
		String[] med_name =  itemsMap.get("hmedName");
		String[] batchno =  itemsMap.get("itemBatchId");
		String[] qty = itemsMap.get("hretqty");
		String[] del = itemsMap.get("hdeleted");
		String[] pkgsize = itemsMap.get("hpkgsz");
		String suppName  = request.getParameter("supplierName");
		String return_type  = request.getParameter("returnType");
		String remarks  = request.getParameter("remarks");
		String storeId = request.getParameter("store");
		String gate_pass = request.getParameter("gatepass");
		if ((storeId != null) && (storeId.trim().length() > 0)){
			storeIdNum = Integer.parseInt(storeId);
		}
		String qty_selection = request.getParameter("qty_unit");
		GenericDAO returnsDao = null;
		ActionRedirect redirect = new ActionRedirect("StoresSupplierReturns.do?_method=getSupplierReturns&sortOrder=return_no&sortReverse=true");
		FlashScope flash = FlashScope.getScope(request);

		StockFIFODAO fifoDAO = new StockFIFODAO();
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BasicDynaBean bean = storeSupplierReturnsMainDAO.getBean();
			returnNo = StoresSupplierReturnsDAO.getSupplierReturnsNextId();
			bean.set("return_no", returnNo);
			Date d = new Date();
			java.sql.Timestamp dt =new java.sql.Timestamp(d.getTime());
			bean.set("date_time", dt);
			bean.set("user_name", (String)session.getAttribute("userid"));
			String supplier_id = DirectStockEntryDAO.suppNameToId(suppName);
			bean.set("supplier_id", supplier_id);
			bean.set("return_type", return_type);
			bean.set("remarks", remarks);

			bean.set("store_id", storeIdNum);
			bean.set("status", "O");   // Open by default
			bean.set("ret_qty_unit", qty_selection);

			if(gate_pass != null)
			{
				BasicDynaBean gt_bean = storeGatePassDAO.getBean();
				int gate_pass_id = StoresSupplierReturnsDAO.getStoreGatePassNextId();
				bean.set("gatepass_id", gate_pass_id);
				String gate_pass_no = "G".concat(Integer.toString(gate_pass_id));
				gt_bean.set("gatepass_id", gate_pass_id);
				gt_bean.set("gatepass_no", gate_pass_no);
				gt_bean.set("txn_type","Supplier Return");
				gt_bean.set("created_date", dt);
				gt_bean.set("dept_id", storeIdNum);
				status = storeGatePassDAO.insert(con, gt_bean);
				if(status) gtpassId = gate_pass_id;
			}

			if (status) status = storeSupplierReturnsMainDAO.insert(con, bean);

			returnsDao = storeSupplierReturnsDAO;

			List<BasicDynaBean> retbean = new ArrayList<BasicDynaBean>();
			int i = 0;
			for (Object field : med_id) {
				if (i == med_id.length -1 || ((String)field).equals("")){
					break;
				}
				if (del[i].equalsIgnoreCase("false")) {
					if ((med_id[i] != null) && (med_id[i].trim().length() > 0)){
						medIdNum = Integer.parseInt(med_id[i]);
					}
					BigDecimal returnqty = null;
					BasicDynaBean returnBean = returnsDao.getBean();
					returnBean.set("return_no", returnNo);
					if (medIdNum > 0){
						returnBean.set("medicine_id", medIdNum);
					}
					returnBean.set("item_batch_id", Integer.parseInt(batchno[i]));
	                // if Qty in (radio option):Package Units saved as it is else xxx * issue_base_unit ...
					if (qty_selection.equalsIgnoreCase("P")) {
						returnqty = (new BigDecimal(qty[i])).multiply(new BigDecimal(pkgsize[i]).setScale(2, BigDecimal.ROUND_HALF_UP));
					} else {
						returnqty = new BigDecimal(qty[i]);
					}
					returnBean.set("qty", returnqty);
					int returnDetNo = returnsDao.getNextSequence();

					//update stock on FIFO
					Map fifoStatusMap = fifoDAO.reduceStock(con, storeIdNum, Integer.parseInt(batchno[i]), "R", returnqty, null,
							(String)session.getAttribute("userid"), "SupplierReturns", returnDetNo);

					//set cost value which was calculated while reducing stock
					returnBean.set("cost_value", fifoStatusMap.get("costValue"));
					returnBean.set("return_detail_no", returnDetNo);
					retbean.add(returnBean);

					medIdNum = -1;

				}
				i = i+1;
			}

		if (status) status = storeSupplierReturnsDAO.insertAll(con, retbean);

		if (status) {
        	message=new Integer(returnNo).toString();
        	con.commit();

        	//update stock timestamp
			StockFIFODAO stockFIFODAO = new StockFIFODAO();
			stockFIFODAO.updateStockTimeStamp();
			stockFIFODAO.updateStoresStockTimeStamp(storeIdNum);

        	redirect.addParameter("_message",message);
        	redirect.addParameter("_gtpassId", gtpassId);
        	redirect.addParameter("_report", "PharmacySupplierReturnsReport");
        	flash.put("_type", "Return No");
        	redirect.addParameter("_flag", true);
		} else {
           	con.rollback();
           	log.error("Error in StoresSupplierReturnsAction transaction, rolling back");
           	redirect.addParameter("_flag", false);
           	flash.put("error","Transaction failure");
		}
		}catch (Exception e) {
			log.error(e.getMessage());
			con.rollback();
			flash.put("error","Transaction failure");
			redirect.addParameter("_flag", false);
			log.error("Error in StoresSupplierReturnsAction transaction, rolling back");
		}
		finally {
			DataBaseUtil.closeConnections(con, null);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		}


		return redirect;
	}

	private ActionForward getForward(String forward) {
		StringBuilder path = new StringBuilder("/pages/stores/StoresSupplierReturns/");

		if (forward.equals("viewsupplierreturnscreen")) {
			path.append("ViewSupplierReturns.jsp");
		} else if (forward.equals("supplierreplacementscreen")) {
			path.append("StoresSupplierReplacement.jsp");
		} else if (forward.equals("confirmscreen")) {
			path.append("StoresConfirmation.jsp");
		} else if (forward.equals("supplierdebitscreen")) {
			path.append("StoresSupplierDebit.jsp");
		} else if (forward.equals("viewsupplierreturndebitscreen")) {
			path.append("ViewSupplierReturnsDebit.jsp");
		} else if (forward.equals("viewpurchasedetailsscreen")) {
			path.append("StoresPurchaseDetails.jsp");
		}
		return new ActionForward(path.toString());
	}


	/*
	 *  view supplier return screen
	 */

	@IgnoreConfidentialFilters
	public  ActionForward getSupplierReturns(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

		int retNo = 0;
		if ( (request.getParameter("returnno") != null) && !request.getParameter("returnno").equals("") ) {
			retNo = Integer.parseInt(request.getParameter("returnno"));
		}


		java.sql.Date fromDate = DataBaseUtil.parseDate(request.getParameter("fdate"));
		java.sql.Date toDate = DataBaseUtil.parseDate(request.getParameter("tdate"));

		String supp = "All";
		if ( (request.getParameter("supplier") != null) && !request.getParameter("supplier").equals("") ) {
			supp = request.getParameter("supplier");
		}
		int pageNum = 1;
		if (request.getParameter("pageNum")!=null && !request.getParameter("pageNum").equals("")) {
			pageNum = Integer.parseInt(request.getParameter("pageNum"));
		}
		int sortOrder = 0;
		String formSort = request.getParameter("sortOrder");

		if (formSort != null) {
			if (formSort.equals("retNo")) {
				sortOrder = StoresInvoiceDetailsDAO.FIELD_SUPPLIER;
			}
		}

		String type = null;

		if (request.getParameter("typeall") == null) {
			if (request.getParameter("return") !=null && request.getParameter("replacement") !=null) {
				type = null;
			}
			else {
				if (request.getParameter("return") !=null) type = "o";
				if (request.getParameter("replacement") !=null) type = "e";
			}
		}

		List returnTypeAll = ConversionUtils.getParamAsList(request.getParameterMap(), "returntypeall");
		List statusAll = ConversionUtils.getParamAsList(request.getParameterMap(), "statusall");

		PagedList list = StoresSupplierReturnsDAO.searchSupplierReturns(retNo,supp,fromDate, toDate,
				sortOrder, new Boolean(request.getParameter("sortReverse")),returnTypeAll,statusAll,type, 20, pageNum);

		request.setAttribute("message", request.getParameter("message")!=null?request.getParameter("message"):"");

		request.setAttribute("gatepassId", request.getParameter("gatepassId")!=null?request.getParameter("gatepassId"):"");

		request.setAttribute("report", request.getParameter("report")!=null?request.getParameter("report"):"");
		request.setAttribute("type", request.getParameter("type")!=null?request.getParameter("type"):"");
		request.setAttribute("flag", request.getParameter("flag")!=null?request.getParameter("flag"):false);

		request.setAttribute("pagedList", list);

		return getForward("viewsupplierreturnscreen");
	}

	@IgnoreConfidentialFilters
	public  ActionForward getSupplierReplacementScreen(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{
		int retNo = Integer.parseInt(request.getParameter("retNo"));
		BasicDynaBean retDet  = StoresSupplierReturnsDAO.getReturnMedDet(retNo);
		List<BasicDynaBean> retMedList = StoresSupplierReturnsDAO.getReturnMedList(retNo);
		ArrayList<String> storelist = StoresSupplierReturnsDAO.getReutrnStore(retNo);
		HttpSession session = request.getSession(false);
		String dept_id = (String)session.getAttribute("pharmacyStoreId");
		if(dept_id!=null && !dept_id.equals("")){
			BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(dept_id));
			String dept_name = dept.get("dept_name").toString();
			request.setAttribute("dept_id", dept_id);
			request.setAttribute("dept_name", dept_name);
		}
		if(dept_id != null && dept_id.equals("")) {
			request.setAttribute("dept_id", dept_id);
		}
		request.setAttribute("return_medicine", retDet);
		request.setAttribute("list", retMedList);
		request.setAttribute("storeList",storelist);
		return getForward("supplierreplacementscreen");
	}

	public ActionForward makeSupplierReplacement(ActionMapping am, ActionForm af,
			HttpServletRequest request, HttpServletResponse res) throws Exception {

		HttpSession session = request.getSession(false);
		Connection con = null;PreparedStatement ps = null;ResultSet rs = null;
		String message = null;
		boolean status = true;
		int gtpassId = 0;
		ActionRedirect redirect = null;
		redirect = new ActionRedirect("StoresSupplierReturns.do?_method=getSupplierReturns&sortOrder=return_no&sortReverse=true");
		Map<String, String[]> itemsMap = request.getParameterMap();
		String[] med_id = itemsMap.get("hmedId");
		String[] newbatchno = itemsMap.get("replacingBatch");
		String[] itemBatchId =  itemsMap.get("itemBatchId");
		String[] oldbatchno = itemsMap.get("hbatchno");
		String[] qty = itemsMap.get("hreplacingqty");
		String[] del = itemsMap.get("hdeleted");
		String[] mon = itemsMap.get("mon");
		String[] year = itemsMap.get("hyear");
		String[] mrp = itemsMap.get("hmrp");
		String supplier_id  = request.getParameter("suppId");
		String return_type  = request.getParameter("returnType");
		String remarks  = request.getParameter("remarks");
		String storeId = request.getParameter("store");
		String qty_selection = request.getParameter("qty_unit");
		String gate_pass = request.getParameter("gatepass");
		String[] return_detail_no = itemsMap.get("return_detail_no");
		int storeIdNum = -1;
		int medIdNum = -1;
		int retNo = Integer.parseInt(request.getParameter("retno"));
		BigDecimal actQty = new BigDecimal(request.getParameter("actTotQty"));
		BigDecimal replacedQty = new BigDecimal(request.getParameter("repTotQty"));
		BigDecimal replacingQty = new BigDecimal(request.getParameter("totQty"));

		String userName = (String)session.getAttribute("userid");

		GenericDAO itemLotDAO = new GenericDAO("store_item_lot_details");
		GenericDAO returnDAO = storeSupplierReturnsDAO;
		GenericDAO returnMainDAO = storeSupplierReturnsMainDAO;
		GenericDAO itemBatchDAO = new GenericDAO("store_item_batch_details");
		StockFIFODAO fifoDAO = new StockFIFODAO();

		String retStatus = "P";
		int returnNo=0;
		FlashScope flash = FlashScope.getScope(request);
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			// store_supplier_returns_main inserting
			BasicDynaBean returnMainbean = returnMainDAO.getBean();
			returnNo = StoresSupplierReturnsDAO.getSupplierReturnsNextId();
			returnMainbean.set("return_no", returnNo);
			returnMainbean.set("date_time", DateUtil.getCurrentTimestamp());
			returnMainbean.set("user_name", (String)session.getAttribute("userid"));
			returnMainbean.set("supplier_id", supplier_id);
			returnMainbean.set("return_type", return_type);
			returnMainbean.set("remarks", remarks);

			if ((storeId != null) && (storeId.trim().length() > 0)){
				storeIdNum = Integer.parseInt(storeId);
			}

			returnMainbean.set("store_id", storeIdNum);
			returnMainbean.set("orig_return_no", retNo);
			returnMainbean.set("status", "C");   // Closed by default for supplier replacement
			returnMainbean.set("ret_qty_unit", qty_selection);

			if (gate_pass != null) {
				BasicDynaBean gt_bean = storeGatePassDAO.getBean();
				int gate_pass_id = StoresSupplierReturnsDAO.getStoreGatePassNextId();
				returnMainbean.set("gatepass_id", gate_pass_id);
				String gate_pass_no = "G".concat(Integer.toString(gate_pass_id));
				gt_bean.set("gatepass_id", gate_pass_id);
				gt_bean.set("gatepass_no", gate_pass_no);
				gt_bean.set("txn_type","Supplier Replacement");
				gt_bean.set("created_date", DateUtil.getCurrentDate());
				gt_bean.set("dept_id", storeIdNum);
				status = storeGatePassDAO.insert(con, gt_bean);
				if(status) gtpassId = gate_pass_id;
			}

			returnMainDAO.insert(con, returnMainbean);

			for (int j=0; j<med_id.length; j++) {
				if (!del[j].equalsIgnoreCase("true")) {
					continue;
				}
				if ((med_id[j] != null) && (med_id[j].trim().length() > 0)){
					medIdNum = Integer.parseInt(med_id[j]);
				} else {
					continue;
				}
				int returnDetailNo = Integer.parseInt(return_detail_no[j]);
				BasicDynaBean batchbean =
					StoresSupplierReturnsDAO.getStockRow(con, storeIdNum, medIdNum, newbatchno[j]);
				BigDecimal qtyToUpdate = null;

				BasicDynaBean returnBean = returnDAO.getBean();
				returnBean.set("return_no", returnNo);
				returnBean.set("medicine_id", medIdNum);
				returnBean.set("batch_no", newbatchno[j]);
				returnBean.set("item_batch_id", Integer.parseInt( itemBatchId[j] ));
				returnBean.set("qty", (new BigDecimal(qty[j])).negate());

				qtyToUpdate = new BigDecimal(qty[j]).setScale(2, BigDecimal.ROUND_HALF_UP);

				int newItemBatchId = (batchbean != null ? (Integer)batchbean.get("item_batch_id")
						: itemBatchDAO.getNextSequence() );

				if (batchbean == null) {
					BasicDynaBean newItemBatchBean = itemBatchDAO.getBean();
					// inserting new item batch details
					newItemBatchBean.set("medicine_id", medIdNum);
					newItemBatchBean.set("batch_no", newbatchno[j]);
					newItemBatchBean.set("item_batch_id", newItemBatchId);
					newItemBatchBean.set("mrp", new BigDecimal(mrp[j]));
					newItemBatchBean.set("username", userName);
					if (!year[j].equals("") && !mon[j].equals("")) {
						java.util.Date parsedDate = DateUtil.getLastDayInMonth(Integer.parseInt(mon[j]), Integer.parseInt(year[j]));
						java.sql.Date date = new java.sql.Date(parsedDate.getTime());
						newItemBatchBean.set("exp_dt", date);
					}
					itemBatchDAO.insert(con, newItemBatchBean);
				}

				Map statusMap = fifoDAO.addStockWithNewLot(con, storeIdNum, returnDetailNo, "R",
						qtyToUpdate, (String)session.getAttribute("userid"), "SupplierReplacement",
						newItemBatchId, newbatchno[j]);

				if ( !(Boolean)statusMap.get("transaction_lot_exists") ){// this is true for returns done before fifo
					if(batchbean != null) {
						if(!(Boolean)statusMap.get("transaction_lot_exists")){//this is true if sales happened before fifo
							status &= fifoDAO.addToEarlierStock(con, (Integer)batchbean.get("item_batch_id"), storeIdNum,qtyToUpdate);
						}
					} else {
						BasicDynaBean oldLotDetails = fifoDAO.getLotDetails(con,Integer.parseInt(itemBatchId[j]));
						statusMap = fifoDAO.addNewLot(con, storeIdNum,storeIdNum,oldLotDetails, qtyToUpdate, userName, "SupplierReplacement",
								newItemBatchId, newbatchno[j]);
					}
				}

				BigDecimal replaceCostValue = (BigDecimal) statusMap.get("costValue");
				returnBean.set("cost_value", replaceCostValue.negate());//cost value of replaced qty

				// updating return qty against return detail id
				BasicDynaBean returnsDetBean = returnDAO.findByKey("return_detail_no", returnDetailNo);
				returnsDetBean.set("replaced_qty",
						((BigDecimal)returnsDetBean.get("replaced_qty")).add(qtyToUpdate));

				returnDAO.update(con, returnsDetBean.getMap(), "return_detail_no", returnDetailNo);

				// insert the detail for replacement
				returnDAO.insert(con, returnBean);
			}

			if (actQty.subtract((replacedQty.add(replacingQty))).floatValue() == 0) retStatus = "R";
			Map<String, Object> keys = new HashMap<String, Object>();
			keys.put("1", retStatus);
			keys.put("2", retNo);
			if (status) status = StoresSupplierReturnsDAO.updateReturnStatus(con,keys);
			if (status) {
				message=new Integer(returnNo).toString();
				con.commit();

				//update stock timestamp
				StockFIFODAO stockFIFODAO = new StockFIFODAO();
				stockFIFODAO.updateStockTimeStamp();
				stockFIFODAO.updateStoresStockTimeStamp(storeIdNum);

				flash.put("success", "Replacement Done Successfully...");
				redirect.addParameter("_report", "PharmacySupplierReplacementReport");
				redirect.addParameter("_type", "Replacement No");
				redirect.addParameter("_flag", true);
			} else {
				flash.put("error","Transaction failure");
				con.rollback();
				log.error("Error in StoresSupplierReturnsAction transaction, rolling back");
				redirect.addParameter("_flag", false);
			}
			redirect.addParameter("_message", message);
			redirect.addParameter("_gtpassId", gtpassId);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		}
		finally {
			DataBaseUtil.commitClose(con, status);
		}
		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward getConfirmationScreen(ActionMapping am, ActionForm af,
			HttpServletRequest request, HttpServletResponse res) throws SQLException {
		String status = request.getParameter("status");
		String retno = request.getParameter("retNo");
		BasicDynaBean retDet  = StoresSupplierReturnsDAO.getReturnItemDet(Integer.parseInt(retno));
		HttpSession session = request.getSession(false);
		String dept_id = (String)session.getAttribute("pharmacyStoreId");
		if(dept_id!=null && !dept_id.equals("")){
			BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(dept_id));
			String dept_name = dept.get("dept_name").toString();
			request.setAttribute("dept_id", dept_id);
			request.setAttribute("dept_name", dept_name);
		}
		if(dept_id != null && dept_id.equals("")) {
			request.setAttribute("dept_id", dept_id);
		}
		request.setAttribute("st", status);
		request.setAttribute("retno", retno);
		request.setAttribute("retDet", retDet);
		return getForward("confirmscreen");
	}

	@IgnoreConfidentialFilters
	public ActionForward conformStatus(ActionMapping am, ActionForm af,
			HttpServletRequest request, HttpServletResponse res) throws SQLException {
		String st = request.getParameter("chgstatus");
		int retNo = Integer.parseInt(request.getParameter("retNo"));
		Connection con = null;
		String retStatus = "C";
		ActionRedirect redirect = null;
		FlashScope flash = FlashScope.getScope(request);
		try {
			redirect = new ActionRedirect("StoresSupplierReturns.do?_method=getSupplierReturns&sortOrder=return_no&sortReverse=true");
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if (st.equalsIgnoreCase("C")) retStatus = "O";
			Map<String, Object> keys = new HashMap<String, Object>();
			keys.put("1", retStatus);
			keys.put("2", retNo);
			boolean status = StoresSupplierReturnsDAO.updateReturnStatus(con,keys);
			if (status) {
				con.commit();
				flash.put("success", "Txn.No : "+retNo+" Status changed successfully...");
			}
		}catch (Exception e) {
			con.rollback();
			flash.put("error","Transaction failure");
		}
		finally{
			DataBaseUtil.closeConnections(con, null);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		}
		return redirect;
	}

	@IgnoreConfidentialFilters
	public  ActionForward getSupplierDebitScreen(ActionMapping mapping,ActionForm fm,
			HttpServletRequest req,HttpServletResponse response) throws SQLException,Exception{
		
		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("medicine_timestamp",MedicineStockDAO.getMedicineTimestamp());
		int centerId = (Integer) req.getSession().getAttribute("centerId");
		if (GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
			ArrayList<String> Supplier = DirectStockEntryDAO.getSuuplierNamesInMaster(centerId);
			req.setAttribute("supp", js.serialize(Supplier));
		} else {
			ArrayList<String> Supplier = DirectStockEntryDAO.getSuuplierNamesInMaster();
			req.setAttribute("supp", js.serialize(Supplier));
		}

		String transaction = req.getParameter("_transaction");
 		List groupMedDetails =  null;
 		HttpSession session = req.getSession(false);
		String dept_id = (String)session.getAttribute("pharmacyStoreId");
		int roleId = (Integer)session.getAttribute("roleId");
		if(dept_id!=null && !dept_id.equals("")){
			BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(dept_id));
			String dept_name = dept.get("dept_name").toString();
			req.setAttribute("dept_id", dept_id);
			req.setAttribute("dept_name", dept_name);
		}
		if(dept_id != null && dept_id.equals("")) {
			req.setAttribute("dept_id", dept_id);
		}
		if(roleId == 1 || roleId == 2) {
			if(dept_id!=null && !dept_id.equals(""))
				req.setAttribute("dept_id", dept_id);
			else
				req.setAttribute("dept_id", 0);
		}
		HashMap actionRightsMap= (HashMap) req.getSession(false).getAttribute("actionRightsMap");
		String actionRightStatus=(String) session.getAttribute("multiStoreAccess");
		if (actionRightStatus==null)
			actionRightStatus="N";

		if(actionRightStatus.equals("N") ){
			String dept = (String) req.getSession(false).getAttribute("pharmacyStoreId");
			req.setAttribute("store", dept);
		}
 		if(transaction != null){
 			List medIds = new ArrayList();
 			List batchNos = new ArrayList();



 			Map<String, String[]> params = req.getParameterMap();
			String[] selected = params.get("_selected");
			String[] medicineId = params.get("_medicine_id");
			String[] deptId = params.get("_dept_id");
			String[] batchNo =  params.get("_batchno");

			for( int i=0; i<medicineId.length; i++ ){
				if ( selected[i].equals("Y")) {
	 				medIds.add(medicineId[i]);
	 				batchNos.add(batchNo[i]);
				}
 			}

 			groupMedDetails = MedicineStockDAO.getGroupMedDetails("D",deptId[0],medIds, batchNos);
 			
 			req.setAttribute("groupDeptId", deptId[0]);
 			req.setAttribute("type", req.getParameter("type"));
 			req.setAttribute("groupMedDetails", js.serialize(ConversionUtils.listBeanToListMap(groupMedDetails)));
 			req.setAttribute("max_centers_inc_default", GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default());
 		}else
 			req.setAttribute("groupMedDetails",js.serialize(groupMedDetails));
 			//req.setAttribute("suppName", StockReorderDAO.listAllcentersforAPo(centerId));
	 		List<BasicDynaBean> sugGroupList = PurchaseOrderDAO.getAllSubGroups();
			List<BasicDynaBean> groupList = PurchaseOrderDAO.getAllGroups();
			
			req.setAttribute("subGroupListJSON", js.serialize(ConversionUtils.listBeanToListMap(sugGroupList)));
			req.setAttribute("groupList", ConversionUtils.listBeanToListMap(groupList));
			req.setAttribute("groupListJSON", js.serialize(ConversionUtils.listBeanToListMap(groupList)));
 			req.setAttribute("genPrefs", GenericPreferencesDAO.getPrefsBean().getMap());
 			req.setAttribute("listAllcentersforDebit", js.deepSerialize(PurchaseOrderDAO.listAllcentersforAPo(centerId)));

		return getForward("supplierdebitscreen");
	}

	/** THis is the same as getSupplierDebitScreen method with differences in the query and the values it works with
	 * 1. It expects that you have passed indent no and rejected qty as a req. attribute. This will only happen if this
	 * gets called from View Rejected Indents Dashboard.
	 * 2. It calls a different query that also adds indent no as an additional filter.
	 * @param mapping
	 * @param fm
	 * @param req
	 * @param response
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	@IgnoreConfidentialFilters
	public  ActionForward getSupplierDebitForReject(ActionMapping mapping,ActionForm fm,
			HttpServletRequest req,HttpServletResponse response) throws SQLException,Exception{
		int centerId = (Integer) req.getSession().getAttribute("centerId");
		req.setAttribute("medicine_timestamp",MedicineStockDAO.getMedicineTimestamp());
		ArrayList<String> Supplier = DirectStockEntryDAO.getSuuplierNamesInMaster(centerId);
		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("supp", js.serialize(Supplier));

		String transaction = req.getParameter("_transaction");
 		List groupMedDetails =  null;
 		HttpSession session = req.getSession(false);
		String dept_id = (String)session.getAttribute("pharmacyStoreId");
		if(dept_id!=null && !dept_id.equals("")){
			BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(dept_id));
			String dept_name = dept.get("dept_name").toString();
			req.setAttribute("dept_id", dept_id);
			req.setAttribute("dept_name", dept_name);
		}
		if(dept_id != null && dept_id.equals("")) {
			req.setAttribute("dept_id", dept_id);
		}
 		if(transaction != null){
 			List medIds = new ArrayList();
 			List batchNos = new ArrayList();
 			List indentno = new ArrayList();
 			List rejqty = new ArrayList();
 			List deptIds = new ArrayList();
 			Map<String, String[]> params = req.getParameterMap();
 			String[] selected = params.get("_selected");
			String[] medicineId = params.get("_medicine_id");
			String[] deptId = params.get("_dept_id");
			String[] rejQty = params.get("_qty");
			String[] indent = params.get("_indentno");
			String[] batchNo =  params.get("_batchno");
			for( int i=0; i<medicineId.length; i++ ){
				if ( selected[i].equals("Y")) {
					medIds.add(medicineId[i]);
	 				batchNos.add(batchNo[i]);
	 				rejqty.add(rejQty[i]);
	 				indentno.add(indent[i]);
	 				deptIds.add(deptId[0]);
				}

 			}

 			groupMedDetails = StoresSupplierReturnsDAO.getGroupMedDetailsForRejIndents("D",deptId[0],medIds, batchNos,rejqty,indentno);
 			req.setAttribute("suppName", req.getParameter("supplier_code"));
 			req.setAttribute("groupDeptId", deptId[0]);
 			if (!deptId.equals("")){
 				req.setAttribute("dept_id", deptId[0]);
 				String deptName  = StoresIndentDAO.getStoreName( Integer.parseInt(deptId[0]));
 				req.setAttribute("dept_name",deptName);
 			}
 			req.setAttribute("type", req.getParameter("type"));
 			req.setAttribute("groupMedDetails", js.serialize(ConversionUtils.listBeanToListMap(groupMedDetails)));
 		}else
 			req.setAttribute("groupMedDetails",js.serialize(groupMedDetails));

		return getForward("supplierdebitscreen");
	}

	/** Does the Supplier Debit related db transactions
	 * Modified by DPV on 16-09-2010 to check for 2 additional req. params indentno and qty.
	 * If indentno is available that means we have come here due to making a Supplier Debit from View Rejected Transfer Indents
	 * Dashboard. In that case, we need to update the qty_rejected in the stock_transfer table and also revert the
	 * indent status to 'S' or 'Stock Adjusted - due to Suppl. Debit
	 * --
	 * Modified by DPV on 01/03/2011 to consider the revised rates and revised discounts entered by the user. These revised
	 * rates get saved into the grn rates as the actual rates to be used. The original rates that were shown are saved into
	 * the orig_ fields on the grn
	 * --
	 * Modified by DOV on 04/03/2011 to also consider ced(central excise tax). There will again be two CED Amts(based on whether
	 * the CED rate has been applied on original rate or revised rate. As done for tax amt, the revised ced amt will go into
	 * the item_ced field while orig. ced amt will go into orig_ced_amt
	 * @param am
	 * @param af
	 * @param request
	 * @param res
	 * @return
	 * @throws SQLException
	 * @throws ParseException 
	 */

	public ActionForward makeSupplierDebit(ActionMapping am, ActionForm af,
			HttpServletRequest request, HttpServletResponse res) throws SQLException, ParseException {

		HttpSession session = request.getSession(false);
		Connection con = null;
		String message = null;
		boolean status = true;
		String[] indentno = null;
		int gtpassId = 0;
		String suppName = null;
		Map<String, String[]> itemsMap = request.getParameterMap();
		String[] med_id = itemsMap.get("hmedId");
		String[] med_name = itemsMap.get("hmedName");
		String[] batchNo = itemsMap.get("hbatchno");
		String[] itemBatchId = itemsMap.get("item_batch_id");
		String[] expdt = itemsMap.get("hexpdt");
		String[] qty = null;
		String[] bonusQty = null;
		if (itemsMap.get("hrejQty") != null){
			qty = itemsMap.get("hrejQty");
		} else{
			qty =  itemsMap.get("hretqty");
			bonusQty = itemsMap.get("hretbonusqty");
		}
		String[] del = itemsMap.get("hdeleted");
		String[] rate =  itemsMap.get("hrate");
		String[] mrp =  itemsMap.get("hmrp");
		String[] adjmrp =  itemsMap.get("hadjmrp");
		String[] itemdiscount =  itemsMap.get("hdiscamt");
		String[] itemschemediscount = itemsMap.get("hschemediscamt");
		String[] taxRate =  itemsMap.get("htaxrate");
		String[] cedAmt =  itemsMap.get("hcedamt");
		String[] tax =  itemsMap.get("hvat");
		String[] revtax =  itemsMap.get("hrevvat");
		String[] taxType = itemsMap.get("htaxtype");
		String[] pkgSize = itemsMap.get("hpkgsz");
		String[] origRateItem = itemsMap.get("hrate");
		String[] recdRateItem = itemsMap.get("hrevrate");
		String[] revDisc = itemsMap.get("hrevdisc");
		String[] revSchemeDisc =  itemsMap.get("hrevschemedisc");
		String[] pkgUom = itemsMap.get("pkg_uom");
		if (itemsMap.get("hindentno") != null){
			indentno =  itemsMap.get("hindentno");
		}

		String returnAginst = request.getParameter("returnAgainst");
		String grnNo = null;
		if (returnAginst != null && returnAginst.equals("grnReturn")) {
			suppName  = request.getParameter("supplier_name");
			grnNo = request.getParameter("grn_no");
		} else if (request.getParameter("grnReturnH") != null && request.getParameter("grnReturnH").equals("grnReturn")) {
			suppName  = request.getParameter("supplier_name");
			grnNo = request.getParameter("grn_no");
		} else
			 suppName  = request.getParameter("supplierName");
		String return_type  = request.getParameter("returnType");
		String remarks  = request.getParameter("remarks");
		String storeId = request.getParameter("store");
		String debitstatus  = request.getParameter("status");
		String discType  = request.getParameter("discType");
		String discount  = request.getParameter("discount");

		String revdiscount = request.getParameter("otherrevdisc");
		String otherCharges = request.getParameter("otherCharges");
		String otherchargesRemarks  = request.getParameter("otherDescription");
		String roundAmt = request.getParameter("roundAmt");
		String othersreason = request.getParameter("othersreason");
		String debitNoteNo = null;
		String GRNdebitNoteNo = null;
		String companyName = request.getParameter("company_name");
		String consignmentNo = request.getParameter("consignment_no");
		String meanOfTransport = request.getParameter("means_of_transport");
		Date   consignmentDate = DateUtil.parseDate(request.getParameter("consignment_date"));
		String vatORcst = request.getParameter("vatORcst") != null ? request.getParameter("vatORcst") : "VAT";
		String cstRate = request.getParameter("cstrate");
		String qty_selection = request.getParameter("qty_unit");
		String recevideDebitAmt = request.getParameter("recDebitAmt");
		FlashScope flash = FlashScope.getScope(request);
		int storeIdNum = -1;
		int medIdNum = -1;
		ArrayList<BasicDynaBean> indentList = new ArrayList<BasicDynaBean>();
		String stkDetailsGRN = "";
		String gate_pass = request.getParameter("gatepass");

		StockFIFODAO fifoDAO = new StockFIFODAO();
		List<BasicDynaBean> groupList = PurchaseOrderDAO.getAllGroups();
		StoresHelper storeHelper = new StoresHelper();

		ActionRedirect redirect = new ActionRedirect("StoresSupplierReturns.do?_method=getSupplierReturnDebits");
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			// store_debit_note table insertion
			BasicDynaBean debitbean = new GenericDAO("store_debit_note").getBean();
			debitNoteNo = StoresSupplierReturnsDAO.getNextId("store_debit_note_seq","PhDebitNote");
			debitbean.set("debit_note_no", debitNoteNo);
			String supplier_id = DirectStockEntryDAO.suppNameToId(suppName);
		    debitbean.set("supplier_id", supplier_id);
			Date d = new Date();
			java.sql.Date onlyDate = new java.sql.Date(d.getTime());
			java.sql.Timestamp dt =new java.sql.Timestamp(d.getTime());
			debitbean.set("debit_note_date", onlyDate);
			debitbean.set("date_time", dt);
			debitbean.set("round_off", new BigDecimal(roundAmt));
			debitbean.set("return_type", return_type);
			debitbean.set("discount_type", discType);
			if (discType.equalsIgnoreCase("P")) {
				debitbean.set("discount_per", new BigDecimal(discount));
				debitbean.set("discount", new BigDecimal(revdiscount));
			} else{
				debitbean.set("discount", new BigDecimal(revdiscount));
			}
			debitbean.set("other_charges_remarks", otherchargesRemarks);
			debitbean.set("other_reason", othersreason);
			debitbean.set("remarks", remarks);
			debitbean.set("other_charges", new BigDecimal(otherCharges));
			debitbean.set("received_debit_amt", new BigDecimal(recevideDebitAmt));
			if ((storeId != null) && (storeId.trim().length() > 0)){
				storeIdNum = Integer.parseInt(storeId);
			}

				debitbean.set("store_id", storeIdNum);

			debitbean.set("store_id", storeIdNum);



			debitbean.set("status", debitstatus);
			debitbean.set("tax_name", vatORcst);
			debitbean.set("cst_rate", ( cstRate != null && !cstRate.equals("") ) ? new BigDecimal(cstRate) : BigDecimal.ZERO);
			BasicDynaBean store = new StoreMasterDAO().findByKey("dept_id", storeIdNum);
			int  accountGroup = (Integer) store.get("account_group");
			debitbean.set("account_group",accountGroup);

			if(gate_pass != null)
			{
				BasicDynaBean gt_bean = storeGatePassDAO.getBean();
				int gate_pass_id = StoresSupplierReturnsDAO.getStoreGatePassNextId();
				debitbean.set("gatepass_id", gate_pass_id);
				String gate_pass_no = "G".concat(Integer.toString(gate_pass_id));
				gt_bean.set("gatepass_id", gate_pass_id);
				gt_bean.set("gatepass_no", gate_pass_no);
				gt_bean.set("txn_type","Supplier Returns with Debit Note");
				gt_bean.set("created_date", dt);
				gt_bean.set("dept_id", storeIdNum);
				status = storeGatePassDAO.insert(con, gt_bean);
				if(status) gtpassId = gate_pass_id;
			}
			
			debitbean.set("company_name", companyName);
			debitbean.set("consignment_no", consignmentNo);
			debitbean.set("means_of_transport", meanOfTransport);
			debitbean.set("consignment_date", consignmentDate);

			if (status) status = new GenericDAO("store_debit_note").insert(con, debitbean);
			//
            //store_grn_main table insertion
			BasicDynaBean grnmainbean = storeGrnMainDAO.getBean();
			GRNdebitNoteNo = StoresSupplierReturnsDAO.getNextId("store_grn_debit_note_seq","DebitGRN");
			grnmainbean.set("grn_no", GRNdebitNoteNo);
			Date d1 = new Date();
			java.sql.Timestamp dt1 =new java.sql.Timestamp(d1.getTime());
			grnmainbean.set("grn_date", dt1);

			grnmainbean.set("store_id", storeIdNum);

			grnmainbean.set("user_name", (String)session.getAttribute("userid"));
			grnmainbean.set("debit_note_no", debitNoteNo);
			grnmainbean.set("grn_qty_unit", qty_selection);
			if (status) status = storeGrnMainDAO.insert(con, grnmainbean);

			int j = 0;
			int keycount = 1;
			List<HashMap<String,Object>> updatelist = new ArrayList<HashMap<String,Object>>();
			List<BasicDynaBean> insertlist = new ArrayList<BasicDynaBean>();
			for (Object field : med_id) {
				if (j == med_id.length - 1){
					break;
				}
				if (del[j].equalsIgnoreCase("false")) {
					BigDecimal returnqty = null;
					BigDecimal returnBilledqty = null;
					BigDecimal returnBonusqty = null;

                    // if Qty in (radio option):Package Units saved as it is else xxx * issue_base_unit ...
					if (qty_selection.equalsIgnoreCase("P")) {
						returnqty = new BigDecimal(qty[j]).add(new BigDecimal(bonusQty[j])).multiply(new BigDecimal(pkgSize[j]).setScale(2, BigDecimal.ROUND_HALF_UP));
						returnBilledqty = new BigDecimal(qty[j]).multiply(new BigDecimal(pkgSize[j]).setScale(2, BigDecimal.ROUND_HALF_UP));
						returnBonusqty = new BigDecimal(bonusQty[j]).multiply(new BigDecimal(pkgSize[j]).setScale(2, BigDecimal.ROUND_HALF_UP));
					} else {
						returnqty = new BigDecimal(qty[j]).add(new BigDecimal(bonusQty[j]));
						returnBilledqty = new BigDecimal(qty[j]);
						returnBonusqty = new BigDecimal(bonusQty[j]);
					}
					/** The next few lines of code are for appending the GRN no. associated with this debit note to the commacatted column of GRNs that is saved
					 * into the stock_details table
					 */

					if ((med_id[j] != null) && (med_id[j].trim().length() > 0)){
						medIdNum = Integer.parseInt(med_id[j]);
					}

//					check for Quantity before inserting -- only for non transfer indent related returns
					boolean qtyAvailable = true;
					if ((indentno != null) && !(((String)indentno[j]).equals(""))){
						qtyAvailable = true;
					} else{
						qtyAvailable = StoresSupplierReturnsDAO.checkQtyAvailable(con,storeId,med_id[j],itemBatchId[j],returnqty);

					}

					if(!qtyAvailable){
						con.rollback();
						//redirect.addParameter("message", "Stock not available for "+med_name[j]+" and "+batchNo[j]+"");
						flash.put("error","Stock not available for "+med_name[j]+" and "+batchNo[j]+"");
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						return redirect;
					}

					//store_grn_details table insertion
					HashMap<String, Object> batchKeys = new HashMap<String, Object>();
					batchKeys.put("batch_no", batchNo[j]);
					batchKeys.put("medicine_id",medIdNum);

					GenericDAO grnDetDAO = new GenericDAO("store_grn_details");
					BasicDynaBean batchDetailsBean = new GenericDAO("store_item_batch_details").findByKey(batchKeys);
					BasicDynaBean medbean = grnDetDAO.getBean();
					medbean.set("grn_no", GRNdebitNoteNo);
					medbean.set("medicine_id", medIdNum);
					medbean.set("batch_no", batchNo[j]);
					medbean.set("item_batch_id", (Integer)batchDetailsBean.get("item_batch_id"));
					String exp_dt = expdt[j];
					if(exp_dt != null && !exp_dt.equals("")) {
						java.sql.Date parsedDate = DateUtil.parseDate(exp_dt);
						medbean.set("exp_dt", parsedDate);
					} else {
						medbean.set("exp_dt", null);
					}


					medbean.set("orig_discount", new BigDecimal("0").subtract(new BigDecimal(itemdiscount[j])));
					medbean.set("discount", new BigDecimal("0").subtract(new BigDecimal(revDisc[j])));
					medbean.set("scheme_discount", new BigDecimal(revSchemeDisc[j]).negate());
					medbean.set("orig_scheme_discount", new BigDecimal(itemschemediscount[j]).negate());
					medbean.set("tax_rate", new BigDecimal(taxRate[j]));
					medbean.set("orig_tax", new BigDecimal("0").subtract(new BigDecimal(tax[j])));
					medbean.set("tax", new BigDecimal("0").subtract(new BigDecimal(revtax[j])));
					medbean.set("tax_type", taxType[j]);
					medbean.set("outgoing_tax_rate", ( vatORcst.equals("CST") ) ? new BigDecimal(cstRate) : new BigDecimal(taxRate[j]) );
					medbean.set("grn_pkg_size",new BigDecimal(pkgSize[j]));
					medbean.set("grn_package_uom", pkgUom[j]);
					medbean.set("item_batch_id", Integer.parseInt(itemBatchId[j]));
                    // if Qty in (radio option):Package Units saved as it is else xxx * issue_base_unit ...
					if (qty_selection.equalsIgnoreCase("P")) {
						medbean.set("mrp", new BigDecimal(mrp[j]));
						medbean.set("cost_price", new BigDecimal(recdRateItem[j]));
						medbean.set("adj_mrp", new BigDecimal(adjmrp[j]));
						medbean.set("billed_qty", (new BigDecimal("0").subtract(new BigDecimal(qty[j]).multiply(new BigDecimal(pkgSize[j])).setScale(2, BigDecimal.ROUND_HALF_UP))));
						medbean.set("bonus_qty", (new BigDecimal("0").subtract(new BigDecimal(bonusQty[j]).multiply(new BigDecimal(pkgSize[j])).setScale(2, BigDecimal.ROUND_HALF_UP))));
						medbean.set("orig_debit_rate", new BigDecimal(origRateItem[j]));
						medbean.set("item_ced", new BigDecimal("0").subtract(new BigDecimal(cedAmt[j]).multiply(new BigDecimal(qty[j]).add(new BigDecimal(bonusQty[j])))).setScale(2, BigDecimal.ROUND_HALF_UP));
					} else {
						medbean.set("mrp", ConversionUtils.setScale(new BigDecimal(mrp[j])).multiply(ConversionUtils.setScale(new BigDecimal(pkgSize[j]))));
						medbean.set("cost_price", ConversionUtils.setScale(new BigDecimal(recdRateItem[j])).multiply(ConversionUtils.setScale(new BigDecimal(pkgSize[j]))));
						medbean.set("adj_mrp", ConversionUtils.setScale(new BigDecimal(adjmrp[j])).multiply(ConversionUtils.setScale(new BigDecimal(pkgSize[j]))));
						medbean.set("billed_qty", (new BigDecimal("0").subtract(new BigDecimal(qty[j]).setScale(2, BigDecimal.ROUND_HALF_UP))));
						medbean.set("bonus_qty", (new BigDecimal("0").subtract(new BigDecimal(bonusQty[j]).setScale(2, BigDecimal.ROUND_HALF_UP))));
						medbean.set("orig_debit_rate", ConversionUtils.setScale(new BigDecimal(origRateItem[j])).multiply(ConversionUtils.setScale(new BigDecimal(pkgSize[j]))));
						medbean.set("item_ced", ConversionUtils.setScale(new BigDecimal("0").subtract(new BigDecimal(cedAmt[j]).multiply(new BigDecimal(qty[j]).add(new BigDecimal(bonusQty[j]))))));
					}

					int grnDetNo = DataBaseUtil.getNextSequence("grn_item_order_seq");
					medbean.set("item_order", grnDetNo);
					Map stockStatus = new HashMap();

					if ( (new BigDecimal("0").subtract((BigDecimal)medbean.get("billed_qty"))).compareTo(BigDecimal.ZERO) > 0 ) {
						//fifo reduce billed qty
					  stockStatus = fifoDAO.reduceStock(con, storeIdNum, Integer.parseInt(itemBatchId[j]), "D",
								returnBilledqty, null, (String)session.getAttribute("userid"),
								"SupplierReturnDebit", grnDetNo, false,"S",grnNo);
					}
					
					if ( null != stockStatus.get("status") && (Boolean)stockStatus.get("status") ) {
            medbean.set("cost_value", ((BigDecimal)stockStatus.get("costValue")).negate());
          }

					if ( (new BigDecimal("0").subtract((BigDecimal)medbean.get("bonus_qty"))).compareTo(BigDecimal.ZERO) > 0 ) {
						//fifo reduce bonus qty
					  stockStatus = fifoDAO.reduceStock(con, storeIdNum, Integer.parseInt(itemBatchId[j]), "D",
								returnBonusqty, null, (String)session.getAttribute("userid"),
								"SupplierReturnDebit", grnDetNo, false,"B", grnNo);
					}
					
					
					insertlist.add(medbean);
					
					for(int k=0; k<groupList.size() ;k++) {
					 	BasicDynaBean groupBean = groupList.get(k);
					 	BasicDynaBean taxBean = storeGrnTaxDetailsDAO.getBean();
					 	storeHelper.setTaxDetails(itemsMap, j, (Integer)groupBean.get("item_group_id"), taxBean);
					 	taxBean.set("medicine_id", medbean.get("medicine_id"));
					 	taxBean.set("grn_no", medbean.get("grn_no"));
					 	taxBean.set("item_batch_id",Integer.parseInt(itemBatchId[j]));
					 	
					 	Map<String, Object> taxMap = new HashMap<String, Object>();
					 	taxMap.put("medicine_id", medbean.get("medicine_id"));
					 	taxMap.put("grn_no", medbean.get("grn_no"));
					 	taxMap.put("item_batch_id", Integer.parseInt(itemBatchId[j]));
					 	taxMap.put("item_subgroup_id", taxBean.get("item_subgroup_id"));
					 	if(storeGrnTaxDetailsDAO.findByKey(taxMap) != null) {
					 		Map keysMap = new HashMap();
					 		keysMap.put("medicine_id", medbean.get("medicine_id"));
					 		keysMap.put("grn_no", medbean.get("grn_no"));
					 		keysMap.put("item_batch_id", Integer.parseInt(itemBatchId[j]));
					 		keysMap.put("item_subgroup_id", taxBean.get("item_subgroup_id"));
					 		storeGrnTaxDetailsDAO.update(con, taxBean.getMap(), keysMap);
					 	} else {
					 		if(taxBean.get("item_subgroup_id") != null && taxBean.get("tax_amt") != null)
						 		storeGrnTaxDetailsDAO.insert(con, taxBean);
					 	}
					 	
					 }
					medIdNum = -1;

				}
				j = j+1;
			}
			if (status) status = new GenericDAO("store_grn_details").insertAll(con, insertlist);

            if (status) {
            	message= debitNoteNo;
            	flash.put("success","Supplier Debit Done Successfully...");
            	redirect.addParameter("_gtpassId", gtpassId);
            	redirect.addParameter("_message",message);
            	redirect.addParameter("_flag", true);
            	con.commit();
				if(StringUtils.isNotEmpty(debitNoteNo) && debitstatus.equals("C")){
//            		getGrnDetailsJSON(GRNdebitNoteNo);
					List<BasicDynaBean> transactions = StoresSupplierReturnsDAO.getSupplierDebitNoteDetails(debitNoteNo);
					CSV_ADAPTER.scheduleTxnExport(CSV_ADAPTER.beanToJobData(transactions), debitNoteNo);
				}

            	// update stock timestamp
    			StockFIFODAO stockFIFODAO = new StockFIFODAO();
    			stockFIFODAO.updateStockTimeStamp();
    			stockFIFODAO.updateStoresStockTimeStamp(storeIdNum);
    		} else {
    			flash.put("error","Transaction failure");
               	con.rollback();
               	redirect.addParameter("_flag", false);
               	log.error("Error @ makeSupplierDebit() in StoresSupplierReturnsAction transaction, rolling back");
    		}
    		}catch (Exception e) {
    			con.rollback();
    			flash.put("error","Transaction failure");
        		redirect.addParameter("_flag", false);
    			log.error("Error @ makeSupplierDebit() in StoresSupplierReturnsAction transaction, rolling back", e);
    		}
    		finally {
    			DataBaseUtil.closeConnections(con,null);
    			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    		}

    		return redirect;
    	}

	/*
	 *  view supplier return (Debits)screen
	 */

	@IgnoreConfidentialFilters
	public  ActionForward getSupplierReturnDebits(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

		java.sql.Date fromDate = DataBaseUtil.parseDate(request.getParameter("fdate"));
		java.sql.Date toDate = DataBaseUtil.parseDate(request.getParameter("tdate"));
		int centerId = (Integer)request.getSession().getAttribute("centerId");
		List supplierList = ConversionUtils.getParamAsList(request.getParameterMap(), "supplier");

		int pageNum = 1;
		if (request.getParameter("pageNum")!=null && !request.getParameter("pageNum").equals("")) {
			pageNum = Integer.parseInt(request.getParameter("pageNum"));
		}
		int sortOrder = 0;
		String formSort = request.getParameter("sortOrder");

		if (formSort != null) {
			if (formSort.equals("debitNo")) {
				sortOrder = StoresSupplierReturnsDAO.FIELD_DEBITNO;
			}
		}

		List returnTypeAll = ConversionUtils.getParamAsList(request.getParameterMap(), "returntypeall");
		List statusAll = ConversionUtils.getParamAsList(request.getParameterMap(), "statusall");

		String debitNote = request.getParameter("debitno");

		PagedList list = StoresSupplierReturnsDAO.searchSupplierReturnDebits(supplierList,fromDate, toDate, debitNote,
				sortOrder, new Boolean(request.getParameter("sortReverse")),returnTypeAll,statusAll, 20, pageNum);

		request.setAttribute("message", request.getParameter("message")!=null?request.getParameter("message"):"");
		request.setAttribute("flag", request.getParameter("flag")!=null?request.getParameter("flag"):false);

		request.setAttribute("gatepassId", request.getParameter("gatepassId")!=null?request.getParameter("gatepassId"):"");
		request.setAttribute("supp", StockReorderDAO.listAllcentersforAPo(centerId));
		request.setAttribute("pagedList", list);

		return getForward("viewsupplierreturndebitscreen");
	}

	@IgnoreConfidentialFilters
	public  ActionForward editSupplierReturnDebit(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{
		int centerId = (Integer) request.getSession().getAttribute("centerId");
		String storeId = request.getParameter("store");
		String debitNo = request.getParameter("debitNo");
		
		String healthAuthorityForCenter = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
		String healthAuthority = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthorityForCenter).getHealth_authority();

		List<BasicDynaBean> list = StoresSupplierReturnsDAO.getMedListOfSelDebit(debitNo,healthAuthority);
		request.setAttribute("list", list);
		BasicDynaBean debitDet  = StoresSupplierReturnsDAO.getDebitDetails(debitNo);
		ArrayList<String> Supplier = DirectStockEntryDAO.getSuuplierNamesInMaster(centerId);
		JSONSerializer js = new JSONSerializer().exclude("class");
		HttpSession session = request.getSession(false);
		String dept_id = (String)session.getAttribute("pharmacyStoreId");
		if(dept_id!=null && !dept_id.equals("")){
			BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(dept_id));
			String dept_name = dept.get("dept_name").toString();
			request.setAttribute("dept_id", dept_id);
			request.setAttribute("dept_name", dept_name);
		}
		if(dept_id != null && dept_id.equals("")) {
			request.setAttribute("dept_id", dept_id);
		}
		request.setAttribute("supp", js.serialize(Supplier));
		request.setAttribute("debit", debitDet);
		request.setAttribute("fromedit", "Y");
		request.setAttribute("debitNo", debitNo);
		request.setAttribute("qtyselection", DataBaseUtil.getStringValueFromDb("select grn_qty_unit from store_grn_main where debit_note_no=?", debitNo));
		request.setAttribute("store", storeId);
		request.setAttribute("groupMedDetails",js.serialize(null));
		BasicDynaBean suppBean = new GenericDAO("supplier_master").findByKey("supplier_code", debitDet.get("supplier_id"));
		request.setAttribute("supplier_address", suppBean != null ? suppBean.get("supplier_address") : null );
		request.setAttribute("genPrefs", GenericPreferencesDAO.getPrefsBean().getMap());
		request.setAttribute("listAllcentersforDebit", js.deepSerialize(PurchaseOrderDAO.listAllcentersforAPo(centerId)));
		List<BasicDynaBean> sugGroupList = PurchaseOrderDAO.getAllSubGroups();
		List<BasicDynaBean> groupList = PurchaseOrderDAO.getAllGroups();
		List<BasicDynaBean> debitTaxDetails = StockEntryDAO.getDebitNoteTaxDetails(debitNo);
		request.setAttribute("debit_tax_details", debitTaxDetails);
		request.setAttribute("subGroupListJSON", js.serialize(ConversionUtils.listBeanToListMap(sugGroupList)));
		request.setAttribute("groupList", ConversionUtils.listBeanToListMap(groupList));
		request.setAttribute("groupListJSON", js.serialize(ConversionUtils.listBeanToListMap(groupList)));
		return getForward("supplierdebitscreen");
	}

	public  ActionForward updateSupplierReturnDebit(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{


		String debitno = request.getParameter("debitNo");
		String return_type  = request.getParameter("returnType");
		String remarks  = request.getParameter("remarks");
		String debitstatus  = request.getParameter("status");
		String discType  = request.getParameter("discType");
		String discount  = request.getParameter("discount");
		String disc  = (String)request.getParameter("disc");

		BigDecimal discPer = new BigDecimal("0").setScale(2, BigDecimal.ROUND_HALF_UP);

		String revDiscount = request.getParameter("otherrevdisc");


		String otherCharges = request.getParameter("otherCharges");
		String otherchargesRemarks  = request.getParameter("otherDescription");
		String roundAmt = request.getParameter("roundAmt");
		String othersreason = request.getParameter("othersreason");
		String supplier = request.getParameter("supplierName");

		String supplier_id = DirectStockEntryDAO.suppNameToId(supplier);
		String receivedDebitAmt = request.getParameter("recDebitAmt");
		String qty_selection = request.getParameter("qty_unit");
		/* also get the debit note details*/
		Map<String, String[]> itemsMap = request.getParameterMap();
		String[] med_id = itemsMap.get("hmedId");
		String[] batchNo = itemsMap.get("hbatchno");
		String[] itemdiscount = itemsMap.get("hdiscamt");
		String[] itemschemediscount = itemsMap.get("hschemediscamt");
		String[] taxRate = itemsMap.get("htaxrate");
		String[] cedAmt =  itemsMap.get("hcedamt");
		String[] tax =  itemsMap.get("hvat");
		String[] revtax =  itemsMap.get("hrevvat");
		String[] taxType =  itemsMap.get("htaxtype");
		String[] pkgSize =  itemsMap.get("hpkgsz");
		String[] origRateItem =  itemsMap.get("hrate");
		String[] recdRateItem = itemsMap.get("hrevrate");
		String[] revDisc =  itemsMap.get("hrevdisc");
		String[] revSchDisc =  itemsMap.get("hrevschemedisc");
		String companyName =request.getParameter("company_name");
		String consignmentNo = request.getParameter("consignment_no");
		String meanOfTransport = request.getParameter("means_of_transport");
		Date   consignmentDate = DateUtil.parseDate(request.getParameter("consignment_date"));
		
		boolean status = false;
		Connection con = null;
		PreparedStatement ps = null;
		String message = null;
		ActionRedirect redirect = new ActionRedirect("StoresSupplierReturns.do?_method=getSupplierReturnDebits");
		FlashScope flash = FlashScope.getScope(request);
		if (disc.equals("")){
				disc = "0";
		}
		if (otherCharges.equals("")){
			otherCharges = "0";
	}
		try {

			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map<String, Object> keys = new HashMap<String, Object>();
			keys.put("1", return_type);
			keys.put("2", remarks);
			keys.put("3", othersreason);
			keys.put("4",debitstatus);
			if (discType.equalsIgnoreCase("P")) discPer = new BigDecimal(discount);
			keys.put("5",discPer);
			keys.put("6",discType);
			keys.put("7",revDiscount != null ? new BigDecimal(revDiscount) : BigDecimal.ZERO);
			keys.put("8", new BigDecimal(otherCharges));
			keys.put("9",otherchargesRemarks);
			keys.put("10", new BigDecimal(roundAmt));
			keys.put("11", supplier_id);
			keys.put("12", new BigDecimal(receivedDebitAmt) );
			keys.put("13", companyName);
			keys.put("14", consignmentNo);
			keys.put("15", meanOfTransport);
			keys.put("16", consignmentDate);
			keys.put("17",debitno);
			String UPDATE_DEBIT_DET = "UPDATE store_debit_note SET RETURN_TYPE=?,REMARKS=?,"
				 +" OTHER_REASON=?,STATUS=?,DISCOUNT_PER=?,DISCOUNT_TYPE=?,DISCOUNT=?,OTHER_CHARGES=?,"
				 +" OTHER_CHARGES_REMARKS=?,ROUND_OFF=?,DATE_TIME=localtimestamp(0),supplier_id=?, received_debit_amt=?,"
				 +" COMPANY_NAME=?, CONSIGNMENT_NO=?, MEANS_OF_TRANSPORT=?, CONSIGNMENT_DATE=?"+
				  " WHERE DEBIT_NOTE_NO=?";
			String UPDATE_GRN_DET = "UPDATE store_grn_details set orig_discount = ?, discount = ?, orig_scheme_discount = ? ,scheme_discount = ? ,orig_tax = ?, tax = ?, cost_price = ?, orig_debit_rate = ? "+
				" where grn_no = ? and medicine_id = ? and batch_no = ? ";
			status = StoresSupplierReturnsDAO.updateDebitNoteDetails(con,keys,UPDATE_DEBIT_DET);
			List<BasicDynaBean> groupList = PurchaseOrderDAO.getAllGroups();
 			StoresHelper storeHelper = new StoresHelper();
			if (status) {
				//now update the debit note details into the grn
				String grnNo = StoresSupplierReturnsDAO.getGRNFromDebitNote(debitno);
				int j = 0;
				ps = con.prepareStatement(UPDATE_GRN_DET);
				for (Object field : med_id) {
					if (j == med_id.length){
						break;
					}
					int medIdNum = 0;
					if ((med_id[j] != null) && (med_id[j].trim().length() > 0)){
						medIdNum = Integer.parseInt(med_id[j]);
					}
					String batch = batchNo[j];
					int itemBatchId = StoresSupplierReturnsDAO.getBatchId(batch);
					BigDecimal origDiscount = new BigDecimal("0").subtract(new BigDecimal(itemdiscount[j]));
					BigDecimal origSchemeDiscount = new BigDecimal("0").subtract(new BigDecimal(itemschemediscount[j]));
					BigDecimal grndisc = new BigDecimal("0").subtract(new BigDecimal(revDisc[j]));
					BigDecimal grnSchdisc = new BigDecimal("0").subtract(new BigDecimal(revSchDisc[j]));
					BigDecimal origTax = new BigDecimal("0").subtract(new BigDecimal(tax[j]));
					BigDecimal grntax = new BigDecimal("0").subtract(new BigDecimal(revtax[j]));
					BigDecimal costprice = BigDecimal.ZERO;
					BigDecimal origRate = BigDecimal.ZERO;
					if (qty_selection.equalsIgnoreCase("P")) {
						costprice = new BigDecimal(recdRateItem[j]);
						origRate = new BigDecimal(origRateItem[j]);
					} else{
						costprice = ConversionUtils.setScale(new BigDecimal(recdRateItem[j])).multiply(ConversionUtils.setScale(new BigDecimal(pkgSize[j])));
						origRate = ConversionUtils.setScale(new BigDecimal(origRateItem[j])).multiply(ConversionUtils.setScale(new BigDecimal(pkgSize[j])));
					}
					ps.setBigDecimal(1, origDiscount);
					ps.setBigDecimal(2, grndisc);
					ps.setBigDecimal(3, origSchemeDiscount);
					ps.setBigDecimal(4, grnSchdisc);
					ps.setBigDecimal(5, origTax);
					ps.setBigDecimal(6, grntax);
					ps.setBigDecimal(7, costprice);
					ps.setBigDecimal(8, origRate);
					ps.setString(9, grnNo);
					ps.setInt(10, medIdNum);
					ps.setString(11, batch);
					ps.addBatch();
					
					for(int k=0; k<groupList.size() ;k++) {
					 	BasicDynaBean groupBean = groupList.get(k);
					 	BasicDynaBean taxBean = storeGrnTaxDetailsDAO.getBean();
					 	storeHelper.setTaxDetails(itemsMap, j, (Integer)groupBean.get("item_group_id"), taxBean);
					 	taxBean.set("medicine_id", medIdNum);
					 	taxBean.set("grn_no", grnNo);
					 	taxBean.set("item_batch_id",itemBatchId);
					 	
					 	Map<String, Object> taxMap = new HashMap<String, Object>();
					 	taxMap.put("medicine_id", medIdNum);
					 	taxMap.put("grn_no", grnNo);
					 	taxMap.put("item_batch_id", itemBatchId);
					 	taxMap.put("item_subgroup_id", taxBean.get("item_subgroup_id"));
					 	if(storeGrnTaxDetailsDAO.findByKey(taxMap) != null) {
					 		Map keysMap = new HashMap();
					 		keysMap.put("medicine_id", medIdNum);
					 		keysMap.put("grn_no", grnNo);
					 		keysMap.put("item_batch_id", itemBatchId);
					 		keysMap.put("item_subgroup_id", taxBean.get("item_subgroup_id"));
					 		storeGrnTaxDetailsDAO.update(con, taxBean.getMap(), keysMap);
					 	} else {
					 		if(taxBean.get("item_subgroup_id") != null && taxBean.get("tax_amt") != null)
						 		storeGrnTaxDetailsDAO.insert(con, taxBean);
					 	}
					 	
					 }
					j++;
				}
				int update[] = ps.executeBatch();
				status = DataBaseUtil.checkBatchUpdates(update);
				if (status) {
					con.commit();
					if(StringUtils.isNotEmpty(debitno) && debitstatus.equals("C")){
//            		getGrnDetailsJSON(GRNdebitNoteNo);
						List<BasicDynaBean> transactions = StoresSupplierReturnsDAO.getSupplierDebitNoteDetails(debitno);
						CSV_ADAPTER.scheduleTxnExport(CSV_ADAPTER.beanToJobData(transactions), debitno);
					}
				}

    			} else {
    				flash.put("error","Transaction failure");
               	con.rollback();
               	log.error("Error @ updateSupplierReturnDebit() in StoresSupplierReturnsAction transaction, rolling back");
    		}
		}catch (Exception e) {
			con.rollback();
			flash.put("error","Transaction failure");
			log.error("Error @ updateSupplierReturnDebit() in StoresSupplierReturnsAction transaction, rolling back", e);
		}
		finally {
			DataBaseUtil.closeConnections(con,ps);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			redirect.addParameter("sortOrder", "debit_note_no");
			redirect.addParameter("sortReverse", "true");
		}
		return redirect;
	}


	/*
	 *  view store purchase details screen
	 */
	@IgnoreConfidentialFilters
	public  ActionForward getPhPurchaseDetails(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

		String medicine = "";
		if ( (request.getParameter("medName") != null) && !request.getParameter("medName").equals("") ) {
			medicine = request.getParameter("medName");
			medicine = MedicineStockDAO.medicineNameToId(medicine);
			if (medicine == null) medicine = "";
		}
		String store = "GDEPT0003";
		if ( (request.getParameter("store") != null) && !request.getParameter("store").equals("") ) {
			store = request.getParameter("store");
		}
		String batchNo = "";
		if ( (request.getParameter("batchno") != null) && !request.getParameter("batchno").equals("") ) {
			batchNo = request.getParameter("batchno");
		}
		String genName = "";
		if( (request.getParameter("genName") != null) && !request.getParameter("genName").equals("") ) {
			genName = request.getParameter("genName");
			genName = GenericMasterDAO.genericNameToId(genName);
			if(genName == null) genName = "";
		}

		int offsetNum = 0;
		String pageval = request.getParameter("pageNum");
		if (pageval == null) {
			pageval = "0";
			request.getSession(false).setAttribute("pageNumber", pageval);
		} else {
			pageval = pageval.replaceAll("'", " ");
			pageval = pageval.trim();
			request.getSession(false).setAttribute("pageNumber", pageval);
			int y = Integer.parseInt(pageval);
			y = y * 15;
			offsetNum = y;
			pageval = String.valueOf(y);
		}
		PagedList pagelist = null;
		int x = 0;
		if (!medicine.equals("") || !genName.equals("")) {
			x = StoresSupplierReturnsDAO.getMedicineDetailsArraySize(store, medicine, batchNo,genName);
			List list = StoresSupplierReturnsDAO.getPurchaseMedList(store,medicine,batchNo,genName,offsetNum);
			pagelist = StoresSupplierReturnsDAO.getPageList(list,x,15,offsetNum);
		}
		request.setAttribute("pagecount", x);
		request.setAttribute("offval", pageval);
		request.setAttribute("pagedList", pagelist);
		request.setAttribute("medicineId", medicine);

		return getForward("viewpurchasedetailsscreen");
	}

	@IgnoreConfidentialFilters
	public ActionRedirect setSupplierReturnRedirectValues(ActionRedirect redirect,HttpServletRequest request){

		redirect.addParameter("typeall", "on");
		redirect.addParameter("sortOrder", "retNo");
		redirect.addParameter("sortReverse", "true");
	    redirect.addParameter("statusall","O");
	    redirect.addParameter("statusall","P");
		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionRedirect setSupplierDebitRedirectValues(ActionRedirect redirect,HttpServletRequest request){

		redirect.addParameter("sortOrder", "debitNo");
		redirect.addParameter("sortReverse", "true");
		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward generateGatePassprint(ActionMapping am,ActionForm af, HttpServletRequest req,HttpServletResponse res)
	throws SQLException,Exception{

		Connection con=null;
		Template t = null;
		Map params = new HashMap();
		String return_no =req.getParameter("return_no");

		if(return_no != null){

			List<BasicDynaBean>gatePassItemList=StoresSupplierReturnsDAO.getSuppReturnItemList(return_no);
			BasicDynaBean printprefs = PrintConfigurationsDAO.getPageOptions(
					PrintConfigurationsDAO.PRINT_TYPE_STORE);
			BasicDynaBean b = gatePassItemList.get(0);

			if(b.get("orig_return_no") != null)
				params.put("type", "Replacement");
			else
				params.put("type", "Return");

			params.put("items", gatePassItemList);
			PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
			String templateContent = printtemplatedao.getCustomizedTemplate(PrintTemplate.Gate_pass_print);

			if (templateContent == null || templateContent.equals("")) {
				t = AppInit.getFmConfig().getTemplate(PrintTemplate.Gate_pass_print.getFtlName() + ".ftl");
			} else {
				StringReader reader = new StringReader(templateContent);
				t = new Template("GatePassPrint.ftl", reader, AppInit.getFmConfig());
			}
			StringWriter writer = new StringWriter();
			t.process(params,writer);
			String printContent = writer.toString();
			HtmlConverter hc = new HtmlConverter();
			if (printprefs.get("print_mode").equals("P")) {
				OutputStream os = res.getOutputStream();
				res.setContentType("application/pdf");
				hc.writePdf(os, printContent, "GatePassPrint", printprefs, false, false, true, true, true, false);
				return null;
				}else{
					String textReport = null;
					textReport = new String(hc.getText(printContent, "GatePassPrintText", printprefs, true, true));
					req.setAttribute("textReport", textReport);
					req.setAttribute("textColumns", printprefs.get("text_mode_column"));
					req.setAttribute("printerType", "DMP");
					return am.findForward("textPrintApplet");
				}
		}
		return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward generateGatePassprintForDebit(ActionMapping am,ActionForm af, HttpServletRequest req,HttpServletResponse res)
	throws SQLException,Exception{

		Template t = null;
		Map params = new HashMap();
		String debit_no =req.getParameter("debitNo");

		if(debit_no != null){

			List<BasicDynaBean>gatePassItemList=StoresSupplierReturnsDAO.getSuppDebitItemList(debit_no);
			BasicDynaBean printprefs = PrintConfigurationsDAO.getPageOptions(
					PrintConfigurationsDAO.PRINT_TYPE_STORE);
			params.put("items", gatePassItemList);
			params.put("type", "Debit");

			PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
			String templateContent = printtemplatedao.getCustomizedTemplate(PrintTemplate.Gate_pass_print);

			if (templateContent == null || templateContent.equals("")) {
				t = AppInit.getFmConfig().getTemplate(PrintTemplate.Gate_pass_print.getFtlName() + ".ftl");
			} else {
				StringReader reader = new StringReader(templateContent);
				t = new Template("GatePassPrint.ftl", reader, AppInit.getFmConfig());
			}
			StringWriter writer = new StringWriter();
			t.process(params,writer);
			String printContent = writer.toString();
			HtmlConverter hc = new HtmlConverter();
			if (printprefs.get("print_mode").equals("P")) {
				OutputStream os = res.getOutputStream();
				res.setContentType("application/pdf");
				hc.writePdf(os, printContent, "GatePassPrint", printprefs, false, false, true, true, true, false);
				return null;
				}else{
					String textReport = null;
					textReport = new String(hc.getText(printContent, "GatePassPrintText", printprefs, true, true));
					req.setAttribute("textReport", textReport);
					req.setAttribute("textColumns", printprefs.get("text_mode_column"));
					req.setAttribute("printerType", "DMP");
					return am.findForward("textPrintApplet");
				}
		}
		return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward generatePrintForItemReturnNote(ActionMapping am, ActionForm af, HttpServletRequest request, HttpServletResponse response)
	throws SQLException, Exception{

		Map params = new HashMap();
		String debit_no =request.getParameter("debitNo");
		String return_no =request.getParameter("return_no");
		String replace =  request.getParameter("replacement");
		FtlReportGenerator fGen = null;

		if(debit_no != null || return_no != null){
			List<BasicDynaBean> ItemsReturnNoteList = new ArrayList<BasicDynaBean>();
			if(debit_no != null) {
				ItemsReturnNoteList=StoresSupplierReturnsDAO.getSuppDebitItemList(debit_no);
				params.put("type", "Debit");
			}
			else {
				ItemsReturnNoteList=StoresSupplierReturnsDAO.getSuppReturnItemList(return_no);
				if(null != replace && replace.equalsIgnoreCase("true"))
					params.put("type", "Replacement");
				else
					params.put("type", "Return");
			}
			params.put("items", ItemsReturnNoteList);
			params.put("taxLabel", GenericPreferencesDAO.getPrefsBean().getMap());
			
			/** Taxation Details */
			if(debit_no != null) {
				List<BasicDynaBean> debitTaxDetails = StoresSupplierReturnsDAO.getDebitTaxDetails(debit_no);
				params.put("debitTaxDetails",  debitTaxDetails);
			}
			
			BasicDynaBean printprefs = PrintConfigurationsDAO.getPageOptions(
					PrintConfigurationsDAO.PRINT_TYPE_STORE);

			PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
			String templateContent = printtemplatedao.getCustomizedTemplate(PrintTemplate.Items_Return_Note_Print);

			if (templateContent == null || templateContent.equals("")) {
				fGen = new FtlReportGenerator("ItemsReturnNotePrint");
			} else {
				StringReader reader = new StringReader(templateContent);
				fGen = new FtlReportGenerator("ItemsReturnNotePrint",reader);
			}
			String printContent = fGen.getPlainText(params);
			HtmlConverter hc = new HtmlConverter();
			if (printprefs.get("print_mode").equals("P")) {
				OutputStream os = response.getOutputStream();
				response.setContentType("application/pdf");
				hc.writePdf(os, printContent, "ItemsReturnNotePrint", printprefs, false, false, true, true, true, false);
				return null;
				}else{
					String textReport = null;
					textReport = new String(hc.getText(printContent, "ItemsReturnNotePrintText", printprefs, true, true));
					request.setAttribute("textReport", textReport);
					request.setAttribute("textColumns", printprefs.get("text_mode_column"));
					request.setAttribute("printerType", "DMP");
					return am.findForward("textPrintApplet");
				}
		}

		return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward isSerialBatchNoAlreadyPresent(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		String medId = request.getParameter("medId");
		String batch = request.getParameter("batchNo");
		Integer medicineId = Integer.parseInt(medId);

		if( batch!= null && !batch.equals("") && medId != null) {
			boolean isSerialBatchPresent = StoresSupplierReturnsDAO.isSerialBatchNoAlreadyPresent(medicineId, batch);
			response.setContentType("text/javascript");
	        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
			if(isSerialBatchPresent) {
				response.getWriter().write("Y");
			} else {
				response.getWriter().write("N");
			}
		}
		return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward getGrnDetailsJSON(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException, SQLException {
		
		HttpSession session = req.getSession();
		String grnNo = (String)req.getParameter("grnNo");
		String deptId =(String)req.getParameter("storeId");
		
		if ( (grnNo==null) || grnNo.equals("") ) {
			log.error("getGrnDetailsJSON: GRN No is required");
			return null;
		}
		
		int centerId = (Integer)session.getAttribute("centerId");
		int roleId = (Integer)session.getAttribute("roleId");
		String userid = (String)session.getAttribute("userid");
		
		BasicDynaBean usreStoresBean = null;
		String userStores = null;
		List<Integer> storeList = new ArrayList<Integer>();
		usreStoresBean = UserDAO.getRecord(userid);
		if(usreStoresBean != null) 
			userStores = (String)usreStoresBean.get("multi_store");
		
		//String retAgtSupp = GenericPreferencesDAO.getGenericPreferences().getReturnAgainstSpecificSupplier();
		
		List<BasicDynaBean> grnBeanList = new ArrayList<BasicDynaBean>();
		BasicDynaBean grnBean = null;
		if (roleId != 1 && roleId != 2) {
			if(userStores != null ) {
				if(userStores.contains(",")) {
					String[] userStoresArr = userStores.split(",");
					for(String store : userStoresArr) {
						storeList.add(Integer.parseInt(store));
					}
				} else {
					storeList.add(Integer.parseInt(userStores));
				}
			}
			grnBean = StockEntryDAO.getGrnDetails(grnNo, storeList);
			
		} else if(centerId != 0){
			storeList = new StockEntryAction().getAllStores(centerId);
			grnBean = StockEntryDAO.getGrnDetails(grnNo, storeList);
		} else{
			grnBean = StockEntryDAO.getGrnDetails(grnNo);
		}
		if (grnBean != null)
			grnBeanList.add(grnBean);

		JSONSerializer js = new JSONSerializer().exclude("class");
		String stockJSON = js.serialize(ConversionUtils.copyListDynaBeansToMap(grnBeanList));

        res.setContentType("text/plain");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.getWriter().write(stockJSON);
        res.flushBuffer();
        return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward getItemTaxDetails(ActionMapping m,ActionForm f,HttpServletRequest req,
			HttpServletResponse res) throws IOException ,SQLException, Exception{
		JSONSerializer js = new JSONSerializer().exclude("class");
		String medicineName = req.getParameter("medicineName");
		String deptId =(String)req.getParameter("storeId");
		String supp = StoresDBTablesUtil.suppNameToId(req.getParameter("supp"));
		String grnNo = (String)req.getParameter("grnNo");
		String itemBatchId = (String)req.getParameter("item_batch_id");
		
		String medTaxDetails = "";
		
		String medicineId = MedicineStockDAO.medicineNameToId(medicineName);
		if ( (medicineId==null) || medicineId.equals("") ) {
			log.error("getItemTaxDetails: Medicine ID could not be resolved for " + medicineName);
			return null;
		}
		
		if(grnNo != null && !grnNo.isEmpty()) {
			medTaxDetails = js.serialize(ConversionUtils.listBeanToListMap(StockEntryDAO.getMedGrnTaxDetails(medicineId, itemBatchId, grnNo)));
		} else {
			BasicDynaBean lastGrnDetails = StockEntryDAO.getLatestGrnDetails(deptId, supp, medicineId, itemBatchId);
			if(lastGrnDetails != null) {
				String latestGrnNo = (String)lastGrnDetails.get("grn_no");
				medTaxDetails = js.serialize(ConversionUtils.listBeanToListMap(StockEntryDAO.getMedGrnTaxDetails(medicineId, itemBatchId, latestGrnNo)));
			}
		}
		
		
		
		res.setContentType("text/plain");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.getWriter().write(medTaxDetails);
        res.flushBuffer();
		return null;
	}

}

