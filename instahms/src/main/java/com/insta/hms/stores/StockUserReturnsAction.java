package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.integration.scm.inventory.ScmOutBoundInvService;
import com.insta.hms.modules.ModulesDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class StockUserReturnsAction extends DispatchAction{
  
  private static ModulesDAO modulesDao = new ModulesDAO();

  private static final DateUtil dateUtil = new DateUtil();
  
	public static final String stock_issued_users = "select distinct(user_issue_no),date_time from stock_issue_main join stock_issue_details using(user_issue_no) where issued_to=? and dept_from = ? and qty != return_qty";

	public static final String stock_user_issued_items = "select * from stock_issue_details ";
	
	private static final GenericDAO storeItemDetailsDAO = new GenericDAO("store_item_details");
	private static final GenericDAO storeHospUserDAO = new GenericDAO("store_hosp_user");
    private static final GenericDAO storeIssueReturnsMainDAO =
        new GenericDAO("store_issue_returns_main");
    private static final GenericDAO storeIssueReturnsDetailsDAO =
        new GenericDAO("store_issue_returns_details");
    private static final GenericDAO storeCategoryMasterDAO =
        new GenericDAO("store_category_master");


	public static final String ISSUED_ITEMS_ON_DATE = "SELECT DISTINCT(sibd.batch_no),u.item_issue_no, u.user_issue_no,ui.date_time, " +
			"i.medicine_name,i.medicine_id,i.cust_item_code,u.qty,u.return_qty, u.qty-u.return_qty AS returnqty, sibd.exp_dt, sibd.mrp, " +
			"COALESCE(ROUND(COALESCE(bc.act_rate,ROUND(u.amount/u.qty, 2)) * u.pkg_size, 2),0) as amount, " +
			"COALESCE(bc.act_rate,ROUND(u.amount/u.qty, 2),0) as unit_rate, " +
			"c.identification,c.billable,(CASE WHEN c.issue_type='P' THEN 'Permanent' WHEN c.issue_type='L'  " +
			" THEN 'Reusable' ELSE 'Consumable' END) AS issue_type,  i.issue_base_unit as issue_units, " +
			" COALESCE(ROUND(sibd.mrp/i.issue_base_unit, 2),0) as unit_mrp, COALESCE(bc.amount,0) as  pkg_mrp, " +
			" i.package_uom, i.issue_units as issue_uom, u.item_unit, COALESCE(bc.discount/u.qty,0) AS discount,u.item_batch_id   "+
	        " FROM stock_issue_main ui  JOIN stock_issue_details u USING(user_issue_no) " +
	        " JOIN  store_item_details i USING(medicine_id)  " +
	        " JOIN store_item_batch_details sibd USING(item_batch_id) " +
	        " LEFT JOIN store_item_issue_rates siir ON (siir.medicine_id = i.medicine_id) " +
		    " LEFT JOIN bill_activity_charge bac ON (u.item_issue_no::varchar = bac.activity_id " +
			   " AND activity_code = 'PHI' AND payment_charge_head = 'INVITE') " +
		    " LEFT JOIN bill_charge bc  On bc.charge_id = bac.charge_id" +
	        " JOIN store_category_master c ON c.category_id=i.med_category_id   ";

	public static final String ISSUE_ITEMS_ON_DATE_GROUP_BY =
		" GROUP BY sibd.batch_no,u.item_issue_no, u.user_issue_no,ui.date_time, " +
		" i.medicine_name,i.medicine_id,i.cust_item_code,u.qty,u.return_qty, sibd.exp_dt, sibd.mrp, " +
		" bc.act_rate,u.amount,u.pkg_size, " +
		" c.identification,c.billable,c.issue_type," +
		" i.issue_base_unit,sibd.mrp,i.issue_base_unit,bc.amount, " +
		" i.package_uom, i.issue_units , u.item_unit, bc.discount,u.item_batch_id ";

	public static final String ISSUED_ITEMS_WITH_PAT_AMTS_ON_DATE = "SELECT DISTINCT(sibd.batch_no),u.item_issue_no, u.user_issue_no," +
			"ui.date_time, i.medicine_name,i.medicine_id,i.cust_item_code,u.qty,u.return_qty, u.qty-u.return_qty AS returnqty, sibd.exp_dt, sibd.mrp, " +
			"COALESCE(ROUND(COALESCE(bc.act_rate,ROUND(u.amount/u.qty, 2)) * u.pkg_size, 2),0) as amount, COALESCE(bc.act_rate,ROUND(u.amount/u.qty, 2),0) as unit_rate, c.identification,c.billable," +
			"(CASE WHEN c.issue_type='P' THEN 'Permanent' WHEN c.issue_type='L'  " +
			" THEN 'Reusable' ELSE 'Consumable' END) AS issue_type,  i.issue_base_unit as issue_units, " +
			"COALESCE(ROUND(sibd.mrp/i.issue_base_unit, 2),0) as unit_mrp, COALESCE(bc.amount,0) as pkg_mrp,"+
			" CASE WHEN insurance_payable = 'Y' THEN ipd.patient_amount ELSE 0 END AS patient_amount," +
			" CASE WHEN insurance_payable = 'Y' THEN ipd.patient_amount_per_category ELSE 0 END AS patient_amount_per_category,  " +
			" CASE WHEN insurance_payable = 'Y' THEN ipd.patient_percent ELSE 100 END AS patient_percent, " +
			" CASE WHEN insurance_payable = 'Y' THEN ipd.patient_amount_cap ELSE null END AS patient_amount_cap, " +
			" u.insurance_claim_amt , i.package_uom, i.issue_units as issue_uom, " +
			" u.item_unit, COALESCE(bc.discount/u.qty,0) AS discount,u.item_batch_id  "+
		    " FROM stock_issue_main ui  " +
		    " JOIN stock_issue_details u USING(user_issue_no) " +
		    " JOIN  store_item_details i USING(medicine_id)  " +
		    " JOIN store_item_batch_details sibd USING(item_batch_id) " +
		    " LEFT JOIN store_item_issue_rates siir ON (siir.medicine_id = u.medicine_id) " +
		    " LEFT  JOIN bill_activity_charge bac ON (u.item_issue_no::varchar = bac.activity_id " +
			" AND activity_code = 'PHI' AND payment_charge_head = 'INVITE') " +
		    " LEFT JOIN bill_charge bc  On bc.charge_id = bac.charge_id" +
		    " JOIN store_category_master c ON c.category_id=i.med_category_id   " +
		    " LEFT JOIN patient_registration pr on (patient_id = issued_to) " +
		    "  JOIN item_insurance_categories iic ON (iic.insurance_category_id = i.insurance_category_id) "+
		    " LEFT OUTER JOIN insurance_plan_details ipd on (ipd.insurance_category_id = i.insurance_category_id and ipd.patient_type = pr.visit_type ) ";


	private static final String ISSUED_ITEMS_WITH_PAT_AMTS_ON_DATE_GROUP_BY =
			" GROUP BY sibd.batch_no,u.item_issue_no, u.user_issue_no," +
			" ui.date_time, i.medicine_name,i.medicine_id,i.cust_item_code,u.qty,u.return_qty,sibd.exp_dt, sibd.mrp, " +
			" bc.act_rate,u.amount,u.qty,u.pkg_size,bc.act_rate, c.identification,c.billable," +
			" c.issue_type, i.issue_base_unit, " +
			" sibd.mrp,i.issue_base_unit,bc.amount,"+
			"  ipd.patient_amount,ipd.patient_amount_per_category,ipd.patient_percent, " +
			" ipd.patient_amount_cap,u.insurance_claim_amt , i.package_uom, i.issue_units, " +
			" u.item_unit, bc.discount,u.item_batch_id  ";

	private static final String ITEM_DETAIL_LIST_QUERY =
			"SELECT distinct sibd.BATCH_NO,S.MEDICINE_ID,C.identification,sibd.mrp,I.tax_rate,I.MEDICINE_NAME,I.cust_item_code, "+
			"(CASE WHEN C.ISSUE_TYPE='C'  THEN 'CONSUMABLE' WHEN C.ISSUE_TYPE='L'  THEN 'REUSABLE' "+
			"WHEN C.ISSUE_TYPE='P'  THEN 'PERMANENT' END ) AS ISSUE_TYPE ,"+
			"S.DEPT_ID,COALESCE(sibd.EXP_DT,null) AS EXP_DT, "+
			"C.billable,S.QTY,I.PACKAGE_TYPE AS PKG_TYPE,i.issue_base_unit as pkg_size " +
			" FROM STORE_STOCK_DETAILS S "+
			" JOIN store_item_batch_details sibd USING(item_batch_id) " +
			"JOIN store_item_details I ON (I.MEDICINE_ID = S.medicine_id) "+
			"JOIN store_category_master C ON(C.CATEGORY_ID =I.MED_CATEGORY_ID) "+
			"WHERE S.ASSET_APPROVED='Y'  ";

	   private static final String PATIENT_DEPARTMENT_QUERY = "SELECT pr.dept_name from bill b JOIN patient_registration pr  "+
	    "ON (b.visit_id = pr.patient_id) ";

	static Logger log = LoggerFactory.getLogger(StockUserReturnsAction.class);
	static JSONSerializer js = new JSONSerializer().exclude("class");

	private static ScmOutBoundInvService scmOutService = ApplicationContextProvider
      .getBean(ScmOutBoundInvService.class);



	@IgnoreConfidentialFilters
	public ActionForward show(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String[] user = null;
		String msg = req.getParameter("message");
		String type = req.getParameter("type");
		if (msg != null) {
			req.setAttribute("message",msg);
		}else{
			req.setAttribute("message","0");
		}

		String msg1 = req.getParameter("message1");

        if (msg1 != null) {
            req.setAttribute("info",msg1);
        }

		HttpSession session = req.getSession(false);
		String store_id = (String) session.getAttribute("pharmacyStoreId");
		int roleId = (Integer) session.getAttribute("roleId");
		if(store_id != null && !store_id.equals("")) {
			req.setAttribute("store_id", store_id);
			BasicDynaBean store = new GenericDAO("stores").findByKey("dept_id", Integer.parseInt(store_id));
			String store_name = store.get("dept_name").toString();
            req.setAttribute("store_name", store_name);
		}
		if(store_id != null && store_id.equals("")) {
			req.setAttribute("store_id", store_id );
		}
		if(roleId == 1 || roleId == 2) {
			if(store_id!=null && !store_id.equals(""))
				req.setAttribute("store_id", store_id);
				else
				req.setAttribute("store_id", 0);
		}

		Map items = req.getParameterMap();
		if (items.get("mrno") != null){
			user = (String[])items.get("mrno");
			req.setAttribute("user_type", "Patient");
			req.setAttribute("enabled", "Patient");
			if (user != null){

		        List<BasicDynaBean> allCreditBills = BillDAO.getPatientAllCreditBills(user[0], "N");

		        String activeCreditBillNo = "";
		        String billStatus = "";

		        if (allCreditBills != null && allCreditBills.size() > 0) {
		        	for (BasicDynaBean bean : allCreditBills) {
		        		activeCreditBillNo = (String)bean.get("bill_no");
		        		billStatus = (String)bean.get("status");
		        		if (!"".equals(billStatus)) {
		        		  break; 
		        		}
					}
		        }
		        req.setAttribute("activeCreditBillNo", activeCreditBillNo);
		        req.setAttribute("billStatus", billStatus);
			}
		} else if (items.get("Hospital_field") != null){
			 user = (String[])items.get("Hospital_field");
			 req.setAttribute("user_type", "Hospital");
			 req.setAttribute("hospuserlist", js.serialize(ConversionUtils.copyListDynaBeansToMap(storeHospUserDAO.listAll(null,"status","A","hosp_user_name"))));
		} else if (type != null && type.equalsIgnoreCase("patient")){
			req.setAttribute("user_type", "Patient");
			req.setAttribute("enabled", "Patient");
		} else if (type != null && type.equalsIgnoreCase("hospital")){
			req.setAttribute("user_type", "Hospital");
			req.setAttribute("hospuserlist", js.serialize(ConversionUtils.copyListDynaBeansToMap(storeHospUserDAO.listAll(null,"status","A","hosp_user_name"))));

		}

		req.setAttribute("display", "block");
		req.setAttribute("search", "visible");

		String username = (String)session.getAttribute("userid");
        BasicDynaBean uBean = new GenericDAO("u_user").findByKey("emp_username", username);
        if (uBean != null)
        	req.setAttribute("isSharedLogIn", uBean.get("is_shared_login"));

		String[] store = (String[])items.get("store");
		ArrayList<String> issued_nos = new ArrayList<String>();
		ArrayList<String> issued_dates = new ArrayList<String>();


		try{
			if(user == null)return am.findForward("addshow");
			if(store == null)return am.findForward("addshow");
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(stock_issued_users);
			ps.setString(1, user[0]);
			ps.setInt(2, Integer.parseInt(store[0]));
			rs = ps.executeQuery();
			while(rs.next()){
				issued_nos.add(rs.getString(1));
				issued_dates.add(rs.getString(2));
			}
		req.setAttribute("issue_nos", issued_nos);
		req.setAttribute("issue_dates", issued_dates);
		req.setAttribute("user", user[0]);
		req.setAttribute("store", store[0]);

		}finally{
      DataBaseUtil.closeConnections(con, ps);
		  if (rs != null) {
		    rs.close();
		  }
		}
		return am.findForward("addshow");
	}


public ActionForward getIssueDetails(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		Timestamp date = null;
    if (!req.getParameter("date_time").isEmpty()) {
      date = new Timestamp(
          dateUtil.getSqlTimeStampFormatterSecs().parse(req.getParameter("date_time")).getTime());
    }
		String user_issue_no = req.getParameter("user_issue_no");
		String planId = req.getParameter("plan_id");
		try{
			con = DataBaseUtil.getConnection();
			if(!user_issue_no.equalsIgnoreCase("no")){
				if(date == null){
					if ((null == planId) || (planId.equals("0"))){
						ps = con.prepareStatement(ISSUED_ITEMS_ON_DATE+" where user_issue_no = ? and u.qty != u.return_qty"+ISSUE_ITEMS_ON_DATE_GROUP_BY);
						String issue_no = (String)req.getParameter("user_issue_no");
						if (((issue_no) != null) && (issue_no.length() > 0)){
							ps.setInt(1, Integer.parseInt(req.getParameter("user_issue_no")));
						}
					} else{
						ps = con.prepareStatement(ISSUED_ITEMS_WITH_PAT_AMTS_ON_DATE+" where user_issue_no = ? and u.qty != u.return_qty and ipd.plan_id = ? "+ISSUED_ITEMS_WITH_PAT_AMTS_ON_DATE_GROUP_BY);
						String issue_no = (String)req.getParameter("user_issue_no");
						if (((issue_no) != null) && (issue_no.length() > 0)){
							ps.setInt(1, Integer.parseInt(req.getParameter("user_issue_no")));
						}
						ps.setInt(2, Integer.parseInt(planId));
					}
				}else{
					if ((null == planId) ||  (planId.equals("0"))){
						ps = con.prepareStatement(ISSUED_ITEMS_ON_DATE + " where user_issue_no = ? and ui.date_time=? and u.qty != u.return_qty" + ISSUE_ITEMS_ON_DATE_GROUP_BY);
						ps.setInt(1, Integer.parseInt(req.getParameter("user_issue_no")));
						ps.setTimestamp(2, date);
					} else{
						ps = con.prepareStatement(ISSUED_ITEMS_WITH_PAT_AMTS_ON_DATE+" where user_issue_no = ? and ui.date_time=? and u.qty != u.return_qty and ipd.plan_id = ? "+ISSUED_ITEMS_WITH_PAT_AMTS_ON_DATE_GROUP_BY);
						ps.setInt(1, Integer.parseInt(req.getParameter("user_issue_no")));
						ps.setInt(2,  Integer.parseInt(planId));
						ps.setTimestamp(3, date);
					}
				}
			}else{
				if ((null == planId) ||  (planId.equals("0"))){
					ps = con.prepareStatement(ISSUED_ITEMS_ON_DATE+" where  ui.date_time=? and u.qty != u.return_qty"+ISSUE_ITEMS_ON_DATE_GROUP_BY);
					ps.setTimestamp(1, date);
				} else{
					ps = con.prepareStatement(ISSUED_ITEMS_WITH_PAT_AMTS_ON_DATE+" where  ui.date_time=? and u.qty != u.return_qty and ipd.plan_id = ? "+ISSUED_ITEMS_WITH_PAT_AMTS_ON_DATE_GROUP_BY);
					ps.setTimestamp(1, date);
					ps.setInt(2, Integer.parseInt(planId));
				}
			}
			res.setContentType("text/plain");
			res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
			res.getWriter().write(js.serialize(DataBaseUtil.queryToArrayList(ps)));
			res.flushBuffer();
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return null;
}

public ActionForward getIssueIds(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		String user = null;
		String store = null;
		user = req.getParameter("user").toString();
		store = req.getParameter("store").toString();
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(stock_issued_users);
			ps.setString(1, user);
			ps.setInt(2, Integer.parseInt(store));
			res.setContentType("text/plain");
			res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
			res.getWriter().write(js.serialize(DataBaseUtil.queryToArrayList(ps)));
			res.flushBuffer();
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		return null;
}
public ActionForward create(ActionMapping am, ActionForm af,
		HttpServletRequest req, HttpServletResponse res) throws Exception{
		Map returnmap = req.getParameterMap();
		String[] item_id = (String[])returnmap.get("item_id");
		String[] item_identifier = (String[])returnmap.get("item_identifier");
		String store_id = (String)(((String[])returnmap.get("store"))[0]);
		String[] qty = (String[])returnmap.get("returned_qty");
		String[] user = (String[])returnmap.get("mrno");
		if(user == null) user = (String[])returnmap.get("return_from");
		String reference="";
		if(returnmap.get("reason") != null)
		reference =(String)((String[])returnmap.get("reason"))[0];
		String[] issue_type = (String[])returnmap.get("issue_type");
		String[] returnissueType = (String[])returnmap.get("issueType");
		String[] user_issue_nos = (String[])returnmap.get("user_issue_nos");
		String[] returnitem = (String[])returnmap.get("returnitem");
		String[] item_issue_no = (String[])returnmap.get("item_issue_no");
		String[] patInsClaimAmt = (String[]) returnmap.get("patientInsClaimHid");
		String[] itemUnit = (String[]) returnmap.get("itemUnit");
        String[] pkgSize = (String[]) returnmap.get("issue_units");
        String[] discounts = (String[]) returnmap.get("issueDiscHid");
        String[] rates = (String[]) returnmap.get("unitMrpHid");
        String[] itemBatchId = (String[]) returnmap.get("item_batch_id");
		String planId = "";
		String visitType = "";

		Boolean isUserReturn = new Boolean(req.getParameter("is_user_returns"));
		HttpSession session = req.getSession(false);
		String isShared = req.getParameter("isSharedLogIn");
    	String userName = isShared == null ? (String)session.getAttribute("userid") : isShared.equals("Y") ?
    			req.getParameter("authUser") : (String)session.getAttribute("userid");
		if (null != returnmap.get("planId")  && !(returnmap.get("planId").equals("0"))){
			planId = (String)((String[])returnmap.get("planId"))[0];
		}
		if (null != returnmap.get("visitType")){
			visitType = (String)((String[])returnmap.get("visitType"))[0];
		}
		Integer loggedInCenterId = (Integer) req.getSession().getAttribute("centerId");
		String msg = null;
        String billable = null;
        String issueType = null;
        String[] bill_no = (String[]) returnmap.get("bill_no");
        String chargeId = null;
        String gropName = ChargeDTO.CG_RETURNS;
		String headName = ChargeDTO.CH_INVENTORY_RETURNS;
		int itemIdNum = 0;
		int storeIdNum = -1;
		String itemIdentifier = "";
		Connection con = null;
		BasicDynaBean stockreturn = null;
		BasicDynaBean stockreturnmain = null;
		boolean sucess = true;
		boolean allSuccesss = false;
    BasicDynaBean module = modulesDao.findByKey("module_id", "mod_scm");
		Integer newReturnId =  Integer.parseInt((new StockUserIssueDAO(con).getSequenceId("store_issue_returns_sequence")));
		List<Map<String, Object>> cacheIssueTxns = new ArrayList<>();
		ActionRedirect redirect = null;
		if (returnmap.get("mrno") != null)
			redirect = new ActionRedirect("StockPatientReturn.do");
		else
			redirect = new ActionRedirect("StockUserReturn.do");
		try{
			con = DataBaseUtil.getConnection();con.setAutoCommit(false);int k = 0;
			ChargeDTO chargeDTO = null;
	        ChargeDAO chargeDAO = new ChargeDAO(con);
			for(int i = 0;i<item_id.length;i++){
				//itemreturnid = new GenericDAO("store_issue_returns_details").getNextSequence();
				boolean returnedrow = true;
				Integer itemReturnId = null;
				if(returnitem != null){
					for(int j = 0;j<returnitem.length;j++){
						if(Integer.parseInt(returnitem[j]) == i)returnedrow = false;
					}
				}
				if ((item_id[i] != null) && (item_id[i].length() > 0)){
					itemIdNum = Integer.parseInt(item_id[i]);
				}
				if ((item_identifier[i] != null) && (item_identifier[i].length() > 0)){
					itemIdentifier = item_identifier[i];
				}
				if ((itemIdNum > 0) && (itemIdentifier != null && !itemIdentifier.equals(""))){
					BigDecimal rate = null;
					BigDecimal discount = null;
					BigDecimal quantity =  null;
					BigDecimal amount = null;
					quantity = new BigDecimal(qty[i]== null|| qty[i].isEmpty()?"0":qty[i]);
					if (bill_no[0] != null && !bill_no[0].isEmpty() && !bill_no[0].equals("C")) {
						rate = new BigDecimal(rates[i]);
						discount = new BigDecimal(discounts[i]);
						amount = (rate.multiply(quantity)).subtract(discount.multiply(quantity));
					} else {
						rate = BigDecimal.ZERO;
						discount =  BigDecimal.ZERO;
						amount =  BigDecimal.ZERO;
					}

					if(!returnedrow) {
						//set bean of inventory_stock_user_return table
						stockreturn = storeIssueReturnsDetailsDAO.getBean();
						stockreturn.set("user_return_no", new BigDecimal(newReturnId));
						itemReturnId = storeIssueReturnsDetailsDAO.getNextSequence();
						stockreturn.set("item_return_no", itemReturnId);
						stockreturn.set("medicine_id",Integer.parseInt(item_id[i]) );
						stockreturn.set("batch_no", item_identifier[i]);
						stockreturn.set("qty", quantity);
						stockreturn.set("amount", amount);
						stockreturn.set("discount", discount);
						stockreturn.set("item_unit", itemUnit[i]);
						stockreturn.set("rtn_pkg_size", (pkgSize[i] != null && !pkgSize[i].equals("")) ? new BigDecimal(pkgSize[i]) : BigDecimal.ONE);
						stockreturn.set("item_batch_id", Integer.parseInt(itemBatchId[i]));

						stockreturnmain = storeIssueReturnsMainDAO.getBean();
						if ((store_id != null) && store_id.length() > 0){
							storeIdNum = Integer.parseInt(store_id);
						}
		//					set bean of inventory_stock_user_return_main table
						stockreturnmain.set("user_return_no", new BigDecimal(newReturnId));
						stockreturnmain.set("date_time", DataBaseUtil.getDateandTime());

						stockreturnmain.set("dept_to", storeIdNum);

						stockreturnmain.set("returned_by", user[0]);
						if (reference != null && !reference.equals("")){
							stockreturnmain.set("reference", reference);
						}

						stockreturnmain.set("username", userName);

						stockreturnmain.set("user_issue_no", new BigDecimal(user_issue_nos[i]));
						if(sucess && k == 0){
							if(storeIssueReturnsMainDAO.insert(con, stockreturnmain)){
								sucess = true; k = 1;
							}else sucess = false;
						}

						StockFIFODAO stockFIFODAO = new StockFIFODAO();
						StockIssueDetailsDAO stockIssueDAO = new StockIssueDetailsDAO();
						Map statusMap = null;

						if(sucess && !returnedrow){


								statusMap = stockFIFODAO.addStock(con,Integer.parseInt(store_id),Integer.parseInt(item_issue_no[i]),"U",
										new BigDecimal(qty[i]),user[0],"UserReturns",null,"UR",itemReturnId);

								if(!(Boolean)statusMap.get("transaction_lot_exists")){//this is true if issue happened before fifo
									sucess &= stockFIFODAO.addToEarlierStock(con, Integer.parseInt(itemBatchId[i]), Integer.parseInt(store_id),new BigDecimal(qty[i]));
								}
								sucess &= (Boolean)statusMap.get("status");

								//set cost value
								stockreturn.set("cost_value", ((BigDecimal)statusMap.get("costValue")));
								sucess &= storeIssueReturnsDetailsDAO.insert(con, stockreturn);


								if(sucess){
									sucess &= new StockUserIssueDAO(con).updateStockUserIssue(user_issue_nos[i], item_id[i], Integer.parseInt(itemBatchId[i]), Float.parseFloat(qty[i]));
									if(sucess){
										String item_name = null;
										ArrayList itemdetails =  DataBaseUtil.queryToArrayList(ITEM_DETAIL_LIST_QUERY+" and S.batch_no=? and S.medicine_id=?", new Object[]{item_identifier[i], Integer.parseInt(item_id[i])});
				                        for(int l = 0;l<itemdetails.size();l++){
				                        	Hashtable table = (Hashtable)itemdetails.get(l);
				                        	billable = table.get("BILLABLE").toString();
				                        	issueType = table.get("ISSUE_TYPE").toString();
				                        	item_name = table.get("MEDICINE_NAME").toString();
				                        }
				                        if(!bill_no[0].isEmpty() && !issueType.equalsIgnoreCase("REUSABLE")){
		                                	if(billable.equalsIgnoreCase("t")){

		                                		BillDAO billDao = new BillDAO(con);
		                                		String billNo = bill_no[0];
		                                		Bill bill = billDao.getBill(billNo);
		                            			String billStatus = bill.getStatus();
		                                		if ( billStatus != null && !billStatus.equals("A") ) {
		                                			sucess = false;
		                                			redirect.addParameter("message1","Bill status is not open: cannot return items");
		                                		}else {

			                                		List<ChargeDTO> actChargeList = new ArrayList<ChargeDTO>();
			                                		List<ChargeDTO> updateChargeList = new ArrayList<ChargeDTO>();
			                                		//Get dept name from bill charges, bill and join with patient registration bill
			                                    	String dept_name = DataBaseUtil.getStringValueFromDb(PATIENT_DEPARTMENT_QUERY+" WHERE b.bill_no = ?", billNo);
			                                    	//set mrp to charge dto
			    	                                chargeId = chargeDAO.getNextChargeId();
			    	                                BasicDynaBean cBean = null;
			    	                                boolean claimable = true;
			    	                                BasicDynaBean itemBean = null;

			    	                                itemBean = storeItemDetailsDAO.findByKey("medicine_id", Integer.parseInt(item_id[i]));
			    	                                if (itemBean != null)
			    	                                	cBean = storeCategoryMasterDAO.findByKey("category_id",  itemBean.get("med_category_id"));
			    	                                if (cBean != null)
			    	                                	claimable = (Boolean)cBean.get("claimable");
					                                int insuranceCategoryId = 0;
					                                BigDecimal claimAmt = BigDecimal.ZERO;
					                                if(itemBean != null ){
					                                	insuranceCategoryId = Integer.parseInt(itemBean.get("insurance_category_id").toString());
					                                	if ((null != planId) && !(planId.equals("0"))) {
					                                		   if (patInsClaimAmt != null)
					                                				claimAmt = new BigDecimal(patInsClaimAmt[i]);
						                           		} else {
					                           			   if (claimable){
					                           				   //there is TPA, but no plan..entire amount is claimable
					                           				   claimAmt = amount;
					                           			   }
						                           		}
					                                }

					                                visitType = bill.getVisitType();
			                                        String patientId = bill.getVisitId();

			                                        int account_group = bill.getAccount_group();
			                                        if (account_group == 0) account_group = Bill.BILL_DEFAULT_ACCOUNT_GROUP;


	                                                BasicDynaBean masterItemBean = new PharmacymasterDAO().findByKey("medicine_id", new Integer(item_id[i]));
	                                                int subGroupId = (Integer) masterItemBean.get("service_sub_group_id");

			    	                                chargeDTO = new ChargeDTO(gropName,headName,
															rate, new BigDecimal(qty[i]).negate(),
															ConversionUtils.setScale(discount.multiply(quantity).negate()),
															"",
													item_id[i], item_name, null, false, 0, subGroupId,insuranceCategoryId, visitType, patientId, null);

													chargeDTO.setBillNo(bill_no[0]);
													chargeDTO.setChargeId(chargeId);
			    	                				chargeDTO.setActivityDetails("PHI", itemReturnId, "Y", null);
			    	                				chargeDTO.setUsername((String) req.getSession(false).getAttribute("userid"));
			    	                				chargeDTO.setActRemarks(reference);
			    	                				chargeDTO.setInsuranceClaimAmount(claimAmt.negate());
			    	                				actChargeList.add(chargeDTO);

			    	                				BasicDynaBean issueDetailBean = stockIssueDAO.getIssueDetailBean(con,Integer.parseInt(user_issue_nos[i]), Integer.parseInt(item_id[i]), item_identifier[i]);
			    	                				// Fetch the original item and set the qty, amount and claim amt.
			    	                				BasicDynaBean issuebean = StockUserIssueDAO.getIssueItemCharge(issueDetailBean.get("item_issue_no").toString());
			    	                				StockUserReturnDAO.setIssueItemsForReturns(chargeDTO.getActQuantity(),chargeDTO.getAmount(),chargeDTO.getInsuranceClaimAmount(), updateChargeList, issuebean);

			    	                				if(chargeDAO.insertCharges(actChargeList)) {
			    	                				  sucess = true;
		    	                          if (module != null &&
		    	                              ((String)module.get("activation_status")).equals("Y")) {
    			    	                				  Map<String, Object> visitMap = new HashMap<>();
    			    	                         visitMap.put("patient_id", bill.getVisitId());
    			    	                         visitMap.put("mr_no", null);
    			    	                				  cacheIssueTransactions(cacheIssueTxns, stockreturnmain, stockreturn, visitMap,
    			    	                              billNo, loggedInCenterId, chargeDTO);
			    	                        }
			    	                				}
			    	                				else sucess = false;

			    	                				if(!sucess)
			    	                					break;

				    	                			if(chargeDAO.updateSaleCharges(updateChargeList)) sucess = true;
				    	                			else sucess = false;

				    	                			if(!sucess)
			    	                					break;
			                                	}
		                                	}
				                        }
									}
								}
							}
						}
					}
				}

			allSuccesss = sucess;

			if(allSuccesss){
				msg = "Successfully Returned Items";
				req.setAttribute("msg", msg);
				redirect.addParameter("message", newReturnId);
				con.commit();

        if (!cacheIssueTxns.isEmpty() && module != null &&
            ((String)module.get("activation_status")).equals("Y")) {
          scmOutService.scheduleIssueReturnTxns(cacheIssueTxns);
        }

				//update stock timestamp
    			StockFIFODAO stockFIFODAO = new StockFIFODAO();
    			stockFIFODAO.updateStockTimeStamp();
    			stockFIFODAO.updateStoresStockTimeStamp(Integer.parseInt(store_id));

				if (bill_no[0] != null && !bill_no[0].equals("")) {
					BillDAO.resetTotalsOrReProcess(bill_no[0]);
				}

			}else {
				msg = "Issue Returns Failed";
				req.setAttribute("msg", msg);
				redirect.addParameter("message", null);
				con.rollback();
			}

			if (returnmap.get("mrno") != null){
				redirect.addParameter("_method", "show");
				redirect.addParameter("type", "patient");
				redirect.addParameter("mrno", user[0]);
				redirect.addParameter("store", store_id);
				redirect.addParameter("issueType", returnissueType[0]);
				//forwardUrl = "StockPatientReturn.do?_method=show&message="+newReturnId+"&type=patient&mrno="+user[0]+"&store="+store_id;
			} else{
				redirect.addParameter("_method", "show");
				redirect.addParameter("type", "hospital");
				redirect.addParameter("Hospital_field", user[0]);
				redirect.addParameter("store", store_id);
				redirect.addParameter("issueType", returnissueType[0]);
				// forwardUrl = "StockUserReturn.do?_method=show&message="+newReturnId+"&type=hospital&Hospital_field="+user[0]+"&store="+store_id;
			}

			//res.sendRedirect(forwardUrl);
			return redirect;
		}finally{
			if(con != null)con.close();
			am.findForward("addshow");
		}
}

public ActionForward getPatientDetailsJSON(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse res)
    throws IOException, SQLException, ParseException {

    res.setContentType("application/x-json");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    String visitId = request.getParameter("visit_id");
    BasicDynaBean pddet = VisitDetailsDAO.getPatientVisitDetailsBean(visitId);

    res.getWriter().write(js.serialize(pddet.getMap()));
    res.flushBuffer();

    return null;
}

public ActionForward isSponsorBill(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse res)
		throws IOException, SQLException, ParseException {
		String billNo = request.getParameter("billNo");
		String medId = request.getParameter("medId");
		Boolean isInsurance = false;
		boolean claimable = false;
		BasicDynaBean itemBean = storeItemDetailsDAO.findByKey("medicine_id", Integer.parseInt(medId));
		if (itemBean != null){
			BasicDynaBean cBean = storeCategoryMasterDAO.findByKey("category_id",  itemBean.get("med_category_id"));
			if (cBean != null)
				claimable = (Boolean)cBean.get("claimable");
		}
		if (BillDAO.checkIfsponsorBill(billNo) && claimable){
			isInsurance = true;
		}
		String resp = js.serialize(isInsurance);
		res.setContentType("text/plain");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		res.getWriter().write(resp);
        res.flushBuffer();
        return null;
}

private void cacheIssueTransactions(List<Map<String,Object>> cacheIssueTxns,
    BasicDynaBean issueMain, BasicDynaBean issueDetails, Map patient,
    String billNo, Integer centerId, ChargeDTO charge) throws SQLException {
  GenericDAO billChargeDAO = new GenericDAO("bill_charge");
  BasicDynaBean chargeBean = billChargeDAO.getBean();
  chargeBean.set("amount", charge.getAmount());
  chargeBean.set("discount", charge.getDiscount());
  Map<String, Object> data = scmOutService.getIssueReturnsMap(issueMain, issueDetails, patient,
      billNo, centerId, chargeBean);
  if(!data.isEmpty()) {
    cacheIssueTxns.add(data);
  }
}


}