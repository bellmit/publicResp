package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class StockUserIssueDAO {

	static Logger log = LoggerFactory.getLogger(StockUserIssueDAO.class);
	Connection con = null;
	public StockUserIssueDAO(Connection con){
		this.con = con;
	}

	private static final String update_stock = "update store_stock_details set qty=qty-? ";

	private static final String update_stock_non_returnable = update_stock+", username=?, change_source=? where dept_id=? and medicine_id=? and batch_no=?";

	private static final String update_stock_returnable = update_stock+", qty_in_use=qty_in_use+?, username=?, change_source=? where dept_id=? and medicine_id=? and batch_no=?";

	public static final String update_stock_user_issue = "update stock_issue_details set return_qty=return_qty+? where user_issue_no=? and medicine_id=? and item_batch_id=?";
	
    private static final GenericDAO genericPreferencesDAO = new GenericDAO("generic_preferences");
    
	@SuppressWarnings("unchecked")
	public List<Hashtable> getItemsAndDetails(
			String listQuery,String itemName,String storeId,String visitStoreRatePlanId)
	throws SQLException{
		PreparedStatement ps = null;
		try{

			BasicDynaBean storeBean = StoreDAO.findByStore(Integer.parseInt(storeId));
			int storeRatePlanId = ( storeBean.get("store_rate_plan_id") == null ? 0 :(Integer)storeBean.get("store_rate_plan_id") );
			ps = con.prepareStatement(listQuery);
			ps.setInt(1, Integer.parseInt(visitStoreRatePlanId));
			ps.setInt(2, storeRatePlanId);
			ps.setString(3, itemName);
			ps.setInt(4, Integer.parseInt(storeId));

			return DataBaseUtil.queryToDynaList(ps);
		}
		finally{
			if(ps != null)ps.close();
		}
	}

	public List<Hashtable> getItemsAndDetails(
			String listQuery,String itemName,String storeId)
	throws SQLException{
		PreparedStatement ps = null;
		try{

			BasicDynaBean storeBean = StoreDAO.findByStore(Integer.parseInt(storeId));
			int storeRatePlanId = ( storeBean.get("store_rate_plan_id") == null ? 0 :(Integer)storeBean.get("store_rate_plan_id") );

			ps = con.prepareStatement(listQuery);
			ps.setInt(1, 0);
			ps.setInt(2, storeRatePlanId);
			ps.setInt(3, storeRatePlanId);
			ps.setString(4, itemName);
			ps.setInt(5, Integer.parseInt(storeId));

			return DataBaseUtil.queryToDynaList(ps);
		}
		finally{
			if(ps != null)ps.close();
		}
	}

	public List<Hashtable> getItemDetails(
			String listQuery,String itemName,String storeId)
	throws SQLException{
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(listQuery);
			ps.setString(1, itemName);
			ps.setInt(2, Integer.parseInt(storeId));

			return DataBaseUtil.queryToDynaList(ps);
		}
		finally{
			if(ps != null)ps.close();
		}
	}


	@SuppressWarnings("unchecked")
	public List<Hashtable> getItemsAndDetailsWithIns(
			String listQuery,String itemName,String storeId,
			String planId, String visitType,String visitStoreRatePlanId)throws SQLException{
		PreparedStatement ps = null;

		BasicDynaBean storeBean = StoreDAO.findByStore(Integer.parseInt(storeId));
		int storeRatePlanId = ( storeBean.get("store_rate_plan_id") == null ? 0 :(Integer)storeBean.get("store_rate_plan_id") );

		try{
			ps = con.prepareStatement(listQuery);
			ps.setInt(1, Integer.parseInt(visitStoreRatePlanId));
			ps.setInt(2, storeRatePlanId);
			ps.setInt(3, Integer.parseInt(planId));
			ps.setString(4, visitType);
			ps.setString(5, itemName);
			ps.setInt(6, Integer.parseInt(storeId));

			return DataBaseUtil.queryToDynaList(ps);
		}
		finally{
			if(ps != null)ps.close();
		}
	}
	public boolean checkUser(String userName)throws SQLException{
		ResultSet rs = null;
		boolean hosp_user = false;
		try (PreparedStatement ps = con.prepareStatement("SELECT HOSP_USER_NAME FROM STORE_HOSP_USER WHERE HOSP_USER_NAME = ?");) {
			
			ps.setString(1, userName);
			rs = ps.executeQuery();
			while(rs.next()){
				hosp_user = true;
			}
			return hosp_user;
		} finally {
				if(rs != null) {
				  rs.close();
				}
		}
	}
	public boolean saveUser(String userName)throws SQLException{
		PreparedStatement ps = null;
		int result = 0;
		try{
			ps = con.prepareStatement("INSERT INTO STORE_HOSP_USER VALUES(?)");
			ps.setString(1, userName);
			result = ps.executeUpdate();
			return (result != 0);
		}finally{
			if(ps != null)ps.close();
		}
	}
	public String getSequenceId(String sequence)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		String id = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("select nextval(?)");
			ps.setString(1,sequence);
			id = DataBaseUtil.getStringValueFromDb(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return id;
	}

	public boolean updateStock(List<StockIssueBean> used_qty, String username)throws SQLException{
		int[] result = new int[used_qty.size()];
			for(int i = 0;i<used_qty.size();i++){
				StockIssueBean items = used_qty.get(i);
			if(items.getIssue_type().equalsIgnoreCase("Reusable")){
			  try (PreparedStatement ps = con.prepareStatement(update_stock_returnable);) {
				
  				ps.setInt(1, items.getIssue_qty().intValue());
  				ps.setInt(2, items.getIssue_qty().intValue());
  				ps.setString(3, username);
  				ps.setString(4, "UserIssue");
  				ps.setString(5, items.getStore_id());
  				ps.setString(6, items.getItem_id());
  				ps.setString(7, items.getItem_identifier());
  				ps.addBatch();
			  }
			}else{
			  try (PreparedStatement ps = con.prepareStatement(update_stock_returnable);) {
  				ps.setInt(1, items.getIssue_qty().intValue());
  				ps.setString(2, username);
  				ps.setString(3, "UserIssue");
  				ps.setString(4, items.getStore_id());
  				ps.setString(5, items.getItem_id());
  				ps.setString(6, items.getItem_identifier());
  				ps.addBatch();
			  }
			}
			}
		return (result.length == used_qty.size());
	}
  public String getItemId(String item_name)throws SQLException{

	  PreparedStatement ps = null;
	  try{
		  ps = con.prepareStatement("SELECT medicine_id FROM store_item_details WHERE medicine_name =?");
		  ps.setString(1, item_name);
		  return DataBaseUtil.getStringValueFromDb(ps);
	  }finally{
		  if(ps != null)ps.close();
	  }
  }
  public boolean updateStockUserIssue(String user_issue_no,String item_id,int item_identifier,float qty)throws SQLException{
	  PreparedStatement ps = null;
	  int result = 0;
	  try{
		  ps = con.prepareStatement(update_stock_user_issue);
		  ps.setBigDecimal(1, (BigDecimal.valueOf(qty)).setScale(2, BigDecimal.ROUND_HALF_UP));
		  ps.setInt(2, Integer.parseInt(user_issue_no));
		  ps.setInt(3, Integer.parseInt(item_id));
		  ps.setInt(4, item_identifier);
		  result = ps.executeUpdate();
	  }catch(Exception e){
	    log.error("Exception", e);
	  }finally{
		  if(ps!=null) {
		    ps.close();
		  }
	  }
	  return (result != 0);
  }
  public static final String KIT_DETAILS = "select medicine_id,batch_no,qty,medicine_name,cust_item_code from store_kit_stock join store_item_details using(medicine_id) where kit_identifier=?";
  public List getKitDetails(String kit_identifier)throws SQLException{
	  PreparedStatement ps = null;
	  try{
		  ps = con.prepareStatement(KIT_DETAILS);
		  ps.setInt(1, Integer.parseInt(kit_identifier));
		  return DataBaseUtil.queryToArrayList(ps);
	  }finally{
		  if(ps != null)ps.close();
	  }
  }
  public boolean updateKit(String kit_identifier)throws SQLException{
	  PreparedStatement ps = null;
	  int result = 0;
	  try{
		  ps = con.prepareStatement("update store_kit_stock_main set issued='Y' where kit_identifier = '"+Integer.parseInt(kit_identifier)+"'");
		  result = ps.executeUpdate();
		  return (result > 0);
	  }finally{
		  if(ps != null)ps.close();
	  }
  }
  private static final String GET_SEL_ITEM_AVBL_QTY = "SELECT IMSD.QTYL FROM STORE_STOCK_DETAILS IMSD"
      +" WHERE IMSD.MEDICINE_ID=? AND IMSD.BATCH_NO=? AND IMSD.DEPT_ID=?";
  /**
	 * Checks wether the stock of the perticuler
	 * @param item_id
	 * @param item_identifier
	 * @param store_id
	 * @param qty
	 * @return
	 * @throws SQLException
	 */
  public boolean isIssuable(String item_id,String item_identifier,String store_id,float qty)throws SQLException{
	 PreparedStatement psnew = null;
	 boolean issue = true;
	 try{
		 psnew = con.prepareStatement(GET_SEL_ITEM_AVBL_QTY);
		psnew.setInt(1, Integer.parseInt(item_id));
		psnew.setString(2, item_identifier);
		psnew.setString(3, store_id);
		int availableqty = DataBaseUtil.getIntValueFromDb(psnew);
		String stock_negative_sale = (String)genericPreferencesDAO.getRecord().getMap().get("stock_negative_sale");
		if(availableqty <= 0 && stock_negative_sale.equalsIgnoreCase("D")){
			issue =  false;
		}
		if(availableqty <= 0 && (stock_negative_sale.equalsIgnoreCase("A") || stock_negative_sale.equalsIgnoreCase("W"))){
			issue =  true;
		}
		if(availableqty-qty < 0 && stock_negative_sale.equalsIgnoreCase("D")){
			issue = false;
		}
		if(availableqty-qty < 0 && (stock_negative_sale.equalsIgnoreCase("A") || stock_negative_sale.equalsIgnoreCase("W"))){
			issue = true;
		}
	 }finally{
		 if(psnew != null)psnew.close();
	 }
	 return issue;
  }

	private static final String KIT_NAME_TO_ID =
		"SELECT kit_id FROM store_kit_main WHERE kit_name=?";

	public static String kitNameToId(String kitName) throws SQLException {
		Connection con = null; PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(KIT_NAME_TO_ID);
			ps.setString(1, kitName);

			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_KIT_DET ="select medicine_name,category,qty,"
		+" case when billable='t' then 'Yes' else 'No' end as billable"
		+" from store_kit_main"
	    +" join store_kit_details using(kit_id)"
	    +" join store_item_details on medicine_id=kit_item_id"
	    +" join store_category_master using(category_id)"
	    +" where kit_id=?";

	public static ArrayList getKitItemDetails(String kitId) throws SQLException {
		PreparedStatement ps = null;Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
		   ps = con.prepareStatement(GET_KIT_DET);
		   ps.setString(1, kitId);
		   return DataBaseUtil.queryToArrayList(ps);
		}finally {
		 DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String UPDATE_ISSUE_QTY = "update store_grn_details set issue_qty=issue_qty+? where grn_no=? and medicine_id=? and batch_no=?";

	public boolean updateGRNQTY(String grnNo,int itemId,String identifier,BigDecimal qty)throws SQLException{
		  PreparedStatement ps = null;
		  int result = 0;
		  try{
			  ps = con.prepareStatement(UPDATE_ISSUE_QTY);
			  ps.setBigDecimal(1, qty);
			  ps.setString(2,grnNo);
			  ps.setInt(3,itemId);
			  ps.setString(4,identifier);
			  result = ps.executeUpdate();
			  return (result > 0);
		  }finally{
			  if(ps != null)ps.close();
		  }
	}

	public static List getGroupItemDetails(String Query,String storeId, List<HashMap> items,int storeRatePlanId) throws SQLException {
		PreparedStatement ps = null;Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			StringBuilder where = new StringBuilder();
			StringBuilder query = new StringBuilder(Query);
			boolean first = true;
			for (HashMap itemMap: items) {
				if (first) {
					where.append(" WHERE ( ( s.medicine_id = ? AND s.batch_no = ?) " );
					first = false;
				}
				else
					where.append(" OR (s.medicine_id = ? AND s.batch_no = ?) " );
			}
			where.append(" ) ");
			query.append(where);
			query.append(" AND s.dept_id = ? AND  qty > 0 AND s.asset_approved='Y' ");
			ps =con.prepareStatement(query.toString());

			int i = 1;
			ps.setInt(i, storeRatePlanId);
			ps.setInt(i++, storeRatePlanId);
			ps.setInt(i++, storeRatePlanId);
			for (HashMap itemMap: items) {
				Integer itemId = Integer.parseInt(itemMap.get("itemId").toString());
				ps.setInt( i++, itemId);
				ps.setString( i++, (String) itemMap.get("itemIdentifier"));
			}

			ps.setInt(i, Integer.parseInt(storeId));

			return DataBaseUtil.queryToDynaList(ps);
	    }finally {
		  DataBaseUtil.closeConnections(con, ps);
	    }
    }

	public static final String GET_ISSUE_ITEMS=   " select sim.user_issue_no, sim.username as user_name, sim.date_time::date as issue_date, s.dept_name, "+
			 " medicine_name, sibd.batch_no, sid.qty, reference as remarks, sim.gatepass_id, sim.user_type, sim.issued_to, "+
			 " coalesce(sm.salutation||' '||pd.patient_name||' '||pd.middle_name||' '||pd.last_name,'') as patient_name, "+
			 " round((sid.amount*sid.qty),2) as amount, m.issue_units, "+
			 " case when scm.billable='t' then 'true' else 'false' end as billable, "+
			 " case when issue_type='P' then 'Permanent' when issue_type='C' then 'Consumable' "+
			 " when issue_type='R'  then 'Retailable' else 'Reusable' end as issue_type, "+
			 " case when consignment_stock='t' then 'Consignment' else 'Normal' end as stocktype, sid.indent_no, "+
			 " date(indent.date_time)	as indent_date,m.cust_item_code "+
			 " from stock_issue_main  sim  "+
			 " left join stock_issue_details sid on sid.user_issue_no = sim.user_issue_no "+
			 " LEFT JOIN store_item_batch_details sibd USING(item_batch_id)" +
			 " left outer join patient_details pd on pd.mr_no=sim.issued_to  "+
			 " left join store_item_details m on m.medicine_id = sid.medicine_id "+
			 " join store_category_master scm ON (scm.category_id = m.med_category_id) "+
			 " left join stores s on s.dept_id = sim.dept_from  "+
			 " join store_stock_details ssd on (ssd.dept_id=dept_from and ssd.medicine_id=sid.medicine_id and sid.batch_no=ssd.batch_no)  "+
			 " left join patient_registration pr on (pd.mr_no=pr.mr_no and pr.status='A')  "+
			 "left join salutation_master sm on (sm.salutation_id = pd.salutation)  "+
			 " left join store_indent_main indent on (indent.indent_no= sid.indent_no) "+
			 " where sim.user_issue_no=?" ;

	public static List<BasicDynaBean> getIssuedItemList(String IssNo)
	throws SQLException {

		PreparedStatement ps = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ISSUE_ITEMS);
			ps.setInt(1, Integer.parseInt(IssNo));
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_PACKAGE_MRP_AND_CP =
		" SELECT issue_base_unit, sibd.mrp, package_cp " +
		"		FROM store_stock_details ssd " +
		"  		JOIN store_item_batch_details sibd USING(item_batch_id)" +
		"  		JOIN store_item_details sid ON(sid.medicine_id = ssd.medicine_id) " +
		" WHERE ssd.medicine_id = ? AND sibd.batch_no = ?";

	public static BasicDynaBean getPackageMrpAndCP(int medicine_id, String batch_no)
			throws SQLException, ParseException {

		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_PACKAGE_MRP_AND_CP);
			ps.setInt(1, medicine_id);
			ps.setString(2, batch_no);

			List list = DataBaseUtil.queryToDynaList(ps);
			if (list.size() > 0) {
				return (BasicDynaBean) list.get(0);
			} else {
				return null;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	// Sale charge Id for the issued item given user_issue_no, item_id, item_identifier
	private static final String GET_ISSUE_ITEM_CHARGE =
		" SELECT bc.charge_id, bc.act_description_id,bc.insurance_claim_amount,bc.amount,bc.act_quantity,bc.is_claim_locked," +
		" bc.return_qty,bc.discount/(bc.act_quantity+bc.return_qty) as discount,COALESCE(billing_group_id,0) as billing_group_id FROM bill_charge bc " +
		"  JOIN bill_activity_charge USING(charge_id) WHERE activity_code = 'PHI' AND activity_id  = ? ";

	public static BasicDynaBean getIssueItemCharge(String itemIssueNo) throws SQLException {
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_ISSUE_ITEM_CHARGE);
			ps.setString(1, itemIssueNo);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

  public static BasicDynaBean getIssueItemCharge(Connection con, String itemIssueNo)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_ISSUE_ITEM_CHARGE);
      ps.setString(1, itemIssueNo);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

	// Sale charge Id for the issued item given return charge Id
	private static final String GET_ISSUE_BILL_CHARGE_ID =
		" SELECT bc.charge_id, bc.act_description_id,bc.insurance_claim_amount,"+
		" bc.amount,bc.act_quantity,bc.return_qty,bc.discount/(bc.act_quantity+bc.return_qty) as discount " +
		" FROM bill_charge bc " +
		"  JOIN bill_activity_charge bac USING(charge_id) WHERE bac.activity_id  = " +
		"	(SELECT item_issue_no FROM stock_issue_details sid " +
		"	JOIN (SELECT user_return_no,user_issue_no,medicine_id,batch_no FROM store_issue_returns_details" +
		"	JOIN store_issue_returns_main USING(user_return_no)" +
		"	WHERE item_return_no::text = (SELECT activity_id FROM bill_activity_charge WHERE charge_id = ?)) AS foo " +
		"   	ON (sid.medicine_id = foo.medicine_id AND sid.batch_no=foo.batch_no AND sid.user_issue_no=foo.user_issue_no))::text " +
		" AND bac.activity_code = 'PHI';";

	public static BasicDynaBean getIssueCharge(String chargeId) throws SQLException {
		Connection con = DataBaseUtil.getReadOnlyConnection();
		try {
			return getIssueCharge(con, chargeId);
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static BasicDynaBean getIssueCharge(Connection con, String chargeId) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_ISSUE_BILL_CHARGE_ID);
			ps.setString(1, chargeId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String GET_STOCK_TRANSFER_DETAILS =
		"select from_store,to_store, "+
		" medicine_name,cust_item_code,batch_no,date,username,qty,reason," +
		" transfer_no, amt,issue_type, indent_no, "+
		" mrp, exp_dt, package_type,description,round(cost_price,2) as cost_price, qty_rejected,qty_recd, " +
		" bin,to_store_tin_no,to_store_pharmacy_drug_license_no,from_store_tin_no,from_store_pharmacy_drug_license_no" +
		" FROM (" +
		" SELECT sf.dept_name as from_store,st.dept_name as to_store,std.qty_rejected,std.qty_recd, "+
		" sitd.medicine_name,sitd.cust_item_code,sibd.batch_no,to_char(stm.date_time, 'DD-MM-YYYY HH:MI AM')as date,stm.username,std.qty,stm.reason," +
		" std.transfer_no, round(cost_value,2) as amt, "+
		" CASE WHEN issue_type='P' THEN 'Permanent' WHEN issue_type='C' THEN 'Consumable' WHEN issue_type='L' THEN 'Reusable' "+
		" WHEN issue_type='R' THEN 'Retail' END AS issue_type, std.indent_no, "+
		" sibd.mrp, to_char(sibd.exp_dt, 'DD-MM-YYYY')as exp_dt, sitd.package_type,std.description, COALESCE(isld.bin,sitd.bin) as bin ," +
		" (SELECT avg(package_cp) FROM store_item_lot_details where item_batch_id = std.item_batch_id AND purchase_type = 'S') as cost_price," +
		"  sf.pharmacy_tin_no as from_store_tin_no,sf.pharmacy_drug_license_no as from_store_pharmacy_drug_license_no," +
		"  st.pharmacy_tin_no as to_store_tin_no,st.pharmacy_drug_license_no as to_store_pharmacy_drug_license_no" +
		" FROM store_transfer_main stm " +
		" JOIN store_transfer_details std USING(transfer_no)" +
		" JOIN store_item_details  sitd USING(medicine_id) "+
		" LEFT JOIN item_store_level_details  isld ON isld.medicine_id = std.medicine_id AND isld.dept_id = stm.store_from " +
		" JOIN store_item_batch_details sibd ON(sibd.item_batch_id = std.item_batch_id) "+
		" JOIN stores sf ON(stm.store_from= sf.dept_id)" +
		" JOIN stores st ON(stm.store_to=st.dept_id) "+
		" JOIN store_category_master scm ON(scm.category_id=sitd.med_category_id) "+
		" WHERE std.transfer_no= ? ) as foo";

	public static List<BasicDynaBean> getStockTransferDetails(int transferNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_STOCK_TRANSFER_DETAILS);
			ps.setInt(1, transferNo);
			List <BasicDynaBean> list = DataBaseUtil.queryToDynaList(ps);
			if(list.size()>0)
				return list;
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return null;
	}
	
	private static final String GET_PATIENT_ISSUE_DETAILS =
			"select distinct sibd.batch_no,s.dept_name as from_store," +
			"sitd.medicine_name,sitd.cust_item_code," +
			"to_char(sim.date_time, 'DD-MM-YYYY HH:MI AM')as date," +
			"sim.username," +
			"case when sid.item_unit = 'I' then sid.qty else round(sid.qty/issue_pkg_size,2) end as qty," +
			"sim.reference, bc.bill_no," +
			"sid.user_issue_no," +
			"sim.user_type," +
			"sim.issued_to," +
			"pr.mr_no," +
			"coalesce(coalesce(sm.salutation,'')||' '||coalesce(pd.patient_name,'')||' '||coalesce(pd.middle_name,'')||' '||coalesce" +
			"(pd.last_name,''),'') as patient_name," +
			"coalesce(bc.amount,0) + coalesce(bct.tax_amount, 0) as amount,"
			+ "bc.discount as discount, " +
			"case when sid.item_unit = 'I' then sitd.issue_units else sitd.package_uom end as issue_units," +
			"case when scm.billable='t' then 'true' else 'false' end as billable," +
			"case when issue_type='P' then 'Permanent' when issue_type='C' then 'Consumable' when issue_type='R'  then 'Retailable' else 'Reusable' end as issue_type," +
			"spid.patient_indent_no as indent_no,sibd.exp_dt,mm.manf_name,mm.manf_code," +
			"coalesce(wn.ward_name,wnr.ward_name) as ward_name,coalesce(wn.ward_no,wn.ward_no) as ward_no,bn.bed_name, " +
			"scm.category as item_category, " + 
			"bct.tax_amount as tax," +
			"COALESCE(pbccl.tax_amt,0)  as pri_sponsor_tax_amt, "+
			"COALESCE(sbccl.tax_amt,0)  as sec_sponsor_tax_amt, "+
			"bct.tax_amount - (COALESCE (pbccl.tax_amt,0) + COALESCE(sbccl.tax_amt, 0 ) ) as patient_tax_amt, "+
			"COALESCE (pbccl.tax_amt,0) + COALESCE(sbccl.tax_amt, 0 )  as sponsor_tax_amt, "+ 
			"bct.tax_rate, "+ 
			"COALESCE(pbccl.insurance_claim_amt,0) as pri_insurance_claim_amt, "+ 
			"COALESCE(sbccl.insurance_claim_amt,0) as sec_insurance_claim_amt, "
			+ "s.pharmacy_tin_no, hcms.tin_number, ptm.tin_number as pri_tpa_tin_number, "
			+ "stm.tin_number as sec_tpa_tin_number, picm.tin_number as pri_insur_tin_number,"
			+ "sicm.tin_number as sec_insur_tin_number, s.pharmacy_drug_license_no, "
			+ "ptm.tpa_name as pri_tpa_name, stm.tpa_name as sec_tpa_name, "
			+ "picm.insurance_co_name as pri_insurance_co_name, sicm.insurance_co_name as sec_insurance_co_name, "
			+ "pipm.plan_name as pri_plan_name, sipm.plan_name as sec_plan_name, "
			+ " sic.control_type_name, "
			+ "d.doctor_name, hcms.center_address, bc.code_type as drug_code, gn.generic_name " +
			" from stock_issue_main sim " +
			" join stock_issue_details sid using(user_issue_no) " +
			" JOIN store_item_batch_details sibd USING(item_batch_id) " +
			" LEFT JOIN bill_activity_charge bac  ON sid.item_issue_no::varchar = bac.activity_id AND activity_code = 'PHI' AND payment_charge_head = 'INVITE'" +
			" LEFT JOIN bill_charge bc  On bc.charge_id = bac.charge_id "+ 
			" LEFT JOIN bill_charge_tax bct ON bc.charge_id = bct.charge_id " +
			" join  store_item_details sitd  on(sitd.medicine_id=sid.medicine_id) " + 
			" JOIN manf_master mm ON(mm.manf_code = sitd.manf_name)" +
			" join store_category_master scm ON (scm.category_id = sitd.med_category_id) " +
			" join stores s on(s.dept_id = sim.dept_from) " +
			" left join patient_registration pr on (pr.patient_id = sim.issued_to) " +
			" LEFT JOIN store_patient_indent_details spid ON (spid.item_issue_no = sid.item_issue_no AND spid.medicine_id = sid.medicine_id) " +
			" LEFT JOIN ward_names wnr ON wnr.ward_no = pr.ward_id " + 
			" left join patient_details pd on (pd.mr_no=pr.mr_no) " +
			" left join salutation_master sm on (sm.salutation_id = pd.salutation) " +
			" LEFT JOIN admission ad ON ad.patient_id = pr.patient_id " +
			" LEFT JOIN bed_names bn ON bn.bed_id = ad.bed_id " +
			" LEFT JOIN ward_names wn ON wn.ward_no = bn.ward_no "+
			" LEFT JOIN bill_claim pbcl ON (bc.bill_no = pbcl.bill_no AND pbcl.priority = 1) "+
			"LEFT JOIN bill_claim sbcl ON (bc.bill_no = sbcl.bill_no AND sbcl.priority = 2) "+
			"LEFT JOIN bill_charge_claim pbccl ON (bc.charge_id = pbccl.charge_id AND pbcl.claim_id = pbccl.claim_id ) "+
			"LEFT JOIN bill_charge_claim sbccl ON (bc.charge_id = sbccl.charge_id AND sbcl.claim_id = sbccl.claim_id) "+
      "LEFT JOIN hospital_center_master hcms ON (hcms.center_id=s.center_id) "+
      "LEFT JOIN tpa_master ptm ON (ptm.tpa_id = pr.primary_sponsor_id) "+
      "LEFT JOIN tpa_master stm ON (stm.tpa_id = pr.secondary_sponsor_id) "+
      "left join bill b on (bc.bill_no = b.bill_no)"+
      "LEFT JOIN patient_insurance_plans ppip ON (ppip.patient_id = b.visit_id AND ppip.priority = 1) "+
      "LEFT JOIN patient_insurance_plans spip ON (spip.patient_id = b.visit_id AND spip.priority = 2) "+
      "LEFT JOIN insurance_plan_main pipm ON (pipm.plan_id = ppip.plan_id) "+
      "LEFT JOIN insurance_plan_main sipm ON (sipm.plan_id = spip.plan_id) "+
      "LEFT JOIN insurance_company_master picm ON (picm.insurance_co_id = ppip.insurance_co AND ppip.priority = 1) "+
      "LEFT JOIN insurance_company_master sicm ON (sicm.insurance_co_id = spip.insurance_co AND spip.priority = 2) "+
      "LEFT JOIN store_item_controltype sic ON (sic.control_type_id = sitd.control_type_id) "+ 
      "LEFT JOIN doctors d ON ( pr.doctor = d.doctor_id) "+ 
      "LEFT JOIN store_item_details items ON (sid.medicine_id = items.medicine_id) "+ 
      "LEFT JOIN generic_name gn ON (gn.generic_code = items.generic_name) "+
      "WHERE  sid.user_issue_no=? AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) ) order by billable desc ";
	
	public static List<BasicDynaBean> getPatientIssueInfo(int issueNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_PATIENT_ISSUE_DETAILS);
			ps.setInt(1, issueNo);
			List <BasicDynaBean> list = DataBaseUtil.queryToDynaList(ps);
			if(list.size()>0)
				return list;
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return null;
	}
	
	 /*
	   To calculate the average cost price for issued item.
	  */
	  public static BigDecimal getIssueRate(int itemBatchId ,BigDecimal qty, int storeId, int centerId, String bedType, double discount) throws Exception {
		  StockFIFODAO fifoDAO = new StockFIFODAO();
		  List<BasicDynaBean> itemStock = fifoDAO.getBatchSortedLotDetails(storeId, itemBatchId);
		  BigDecimal remainingQty = qty ;
		  String expression = "";
		  BigDecimal maxPackageCP = BigDecimal.ZERO;
		  BigDecimal packageCP = BigDecimal.ZERO;
		  int medicineId = 0;
		  Map results = null;
		  BigDecimal transactionQty = BigDecimal.ZERO;
		  
		  for (int i=0; i<itemStock.size(); i++) {
			  BasicDynaBean stockDetails = itemStock.get(i);
			  if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
				  break;
			  }			  
			  results = null;
			  
			  BigDecimal stockQty = (BigDecimal)stockDetails.get("qty");
			  BigDecimal lotPackageCP = (BigDecimal)stockDetails.get("package_cp");
			  if(stockQty.longValue() > 0) {
				  packageCP = BigDecimal.valueOf(Math.max(packageCP.doubleValue(), lotPackageCP.doubleValue()))
				      .setScale(2, BigDecimal.ROUND_HALF_UP);

				  if (stockQty.compareTo(remainingQty) > 0) {
					  // no problem -- use up all what we need.
					  transactionQty = remainingQty;
				  } else {
					  transactionQty = stockQty;
				  }
				  if (transactionQty.compareTo(BigDecimal.ZERO) <= 0) {
						// no luck in this lot. Try next one.
						continue;
				  }

				  remainingQty = remainingQty.subtract(transactionQty);
				  medicineId = (Integer)stockDetails.get("medicine_id");
				  results = new HashMap(stockDetails.getMap());
			  }			  
		  }
		  
		  String stock_negative_sale = (String)genericPreferencesDAO.getRecord().getMap().get("stock_negative_sale");
		  if(medicineId == 0 && (stock_negative_sale.equalsIgnoreCase("A") || stock_negative_sale.equalsIgnoreCase("W"))){
			  if(itemStock.size() > 0) {
				  BasicDynaBean stockDetails = itemStock.get(itemStock.size()-1);
				  BigDecimal lotPackageCP = (BigDecimal)stockDetails.get("package_cp");
				  packageCP = BigDecimal.valueOf(Math.max(packageCP.doubleValue(), lotPackageCP.doubleValue()))
          .setScale(2, BigDecimal.ROUND_HALF_UP);
				  medicineId = (Integer)stockDetails.get("medicine_id");
				  results = new HashMap(stockDetails.getMap());
				 
			  }
			  		  
		  }
			
			
		  if(results != null) {
			  BasicDynaBean maxitemStock = fifoDAO.getBatchSortedLotDetailsMaxCP(storeId, itemBatchId);
			  maxPackageCP = maxitemStock != null && maxitemStock.get("max_package_cp") != null ?
					  (BigDecimal)maxitemStock.get("max_package_cp") : BigDecimal.ZERO;
					  
			  if(packageCP.doubleValue() <= 0) {
				  packageCP = maxPackageCP;
			  }
			  results.put("package_cp", packageCP);
			  results.put("center_id", centerId);
			  results.put("store_id", storeId);
			  results.put("bed_type", bedType);
			  results.put("discount", discount);
			  results.put("max_cp", maxPackageCP);
		  }
		  
		  String expr =(String) new GenericDAO("store_item_issue_rates").findByKey("medicine_id",medicineId).get("issue_rate_expr");
		  //process issue rate expression

		  StringWriter writer = new StringWriter();
		  expression = "<#setting number_format=\"##.##\">\n" + expr;
		  try{
			Template expressionTemplate = new Template("expression", new StringReader(expression),new Configuration());
			expressionTemplate.process(results, writer);
		  }
		  catch (TemplateException e) {
				log.error("", e);
				return BigDecimal.ZERO;
			}catch (ArithmeticException e) {
				log.error("", e);
				return BigDecimal.ZERO;
			}catch(Exception e){
				log.error("", e);
				return BigDecimal.ZERO;
			}
		  String exprProcessValue = "0";
		  BigDecimal issueRate  = null;
		  exprProcessValue = writer.toString().trim();
		  if(exprProcessValue != null && !exprProcessValue.isEmpty()) {
			  issueRate = new BigDecimal(exprProcessValue);
		  }
		 
		  return issueRate ;
	  }
	  
	  /**
	   * This method is used to get the order kit items based on store id and order kit id.
	   * 
	   * @param orderKitId
	   * @param deptId
	   * @return
	   * @throws SQLException
	   */
	  public static Map<Integer, String> getOrderKitItemsStockStatus(int deptId, int orderKitId, String[] issueType) throws SQLException {
		  	Connection con = null;
			PreparedStatement ps = null;
			String lowStockStatus = "";
			Map<Integer, String> stockStatusMap = new HashMap<Integer, String>();
			try {
				con = DataBaseUtil.getReadOnlyConnection();
				StringBuilder stockStatusQuery = new StringBuilder();
				stockStatusQuery.append("select coalesce(sum(qty),0) as in_stock_qty,medicine_id,qty_needed from store_stock_details "
						+ "JOIN order_kit_details USING(medicine_id) "
						+ "JOIN store_item_details std USING(medicine_id) "
						+ "JOIN  store_category_master scm ON ( category_id = med_category_id ) "
						+ "where dept_id = ? AND order_kit_id=? AND asset_approved = 'Y'");
				
				List<Object> args = new ArrayList<>();
				args.add(deptId);
				args.add(orderKitId);
				if (issueType != null) {
					stockStatusQuery.append(" AND issue_type IN (");
				    String[] placeholdersArr = new String[issueType.length];
				    Arrays.fill(placeholdersArr, "?");
				    stockStatusQuery.append(StringUtils.arrayToCommaDelimitedString(placeholdersArr));
				    stockStatusQuery.append(")");
					args.addAll(Arrays.asList(issueType));
				}
				stockStatusQuery.append(" group by medicine_id, qty_needed");
				ps = con.prepareStatement(stockStatusQuery.toString());
				ListIterator<Object> argsIterator = args.listIterator();
				while (argsIterator.hasNext()) {
					ps.setObject(argsIterator.nextIndex() + 1, argsIterator.next());
				}			
				List<BasicDynaBean> medicineInStockStatus = DataBaseUtil.queryToDynaList(ps);
				Iterator<BasicDynaBean> medicineInStockStatusIterator = medicineInStockStatus.iterator();
				while(medicineInStockStatusIterator.hasNext()) {
					if(medicineInStockStatus.size() > 0) {
						BasicDynaBean inStockBean = medicineInStockStatusIterator.next();
						BigDecimal inStockQty = (BigDecimal)inStockBean.get("in_stock_qty");
						BigDecimal qtyNeeded = (BigDecimal)inStockBean.get("qty_needed");
						int medicineId = (Integer)inStockBean.get("medicine_id");
						lowStockStatus = inStockQty+"@"+qtyNeeded;
						stockStatusMap.put(medicineId, lowStockStatus);
					}
				}
			}finally {
				DataBaseUtil.closeConnections(con, ps);
			}
			return stockStatusMap;
	  }
	  
	  /**
	   * Get a list of non issuable items.
	   * @param orderKitId
	   * @param issueType
	   * @return
	   * @throws SQLException
	   */
	  public static List<BasicDynaBean> getNonIssuableItems(int orderKitId, String[] issueType) throws SQLException {
		  	Connection con = null;
			PreparedStatement ps = null;
			try {
				con = DataBaseUtil.getReadOnlyConnection();
				String nonIssuabaleItemsQuery = "SELECT medicine_id, medicine_name FROM order_kit_details "
						+ " JOIN store_item_details sid USING(medicine_id) "
						+ " JOIN store_category_master scm ON ( category_id = med_category_id ) "
						+ "where order_kit_id = ?  AND issue_type IN ('P', 'L') ";
				
				ps = con.prepareStatement(nonIssuabaleItemsQuery);
				ps.setInt(1, orderKitId);
				List<BasicDynaBean> medicineList = DataBaseUtil.queryToDynaList(ps);
				return medicineList;
			}finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		  
	  }
	  
	  /**
	   * This method retrieve the order kit items.
	   * 
	   * @param orderKitId
	   * @return
	   * @throws SQLException
	   */
	  public static ArrayList<BasicDynaBean> getOrderKitItemsDetails(int orderKitId) throws SQLException {
		  	Connection con = null;
			PreparedStatement ps = null;
			try {
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement("SELECT medicine_id, qty_needed, medicine_name, issue_units FROM order_kit_details "
						+ "JOIN store_item_details sid USING(medicine_id) where order_kit_id = ? order by upper(medicine_name)");
				ps.setInt(1, orderKitId);
				ArrayList<BasicDynaBean> medicineList = (ArrayList<BasicDynaBean>)DataBaseUtil.queryToDynaList(ps);
				return medicineList;
			}finally {
				DataBaseUtil.closeConnections(con, ps);
			}
	  }

  private static final String GET_TAX_SUMMARY = "SELECT isg.item_subgroup_name, isgtd.tax_rate,"
      + " sum(bct.tax_amount)as tax_amt, sum(pbccl.tax_amt) as pri_sponsor_tax_amount, "
      + " SUM(sbccl.tax_amt) as sec_sponsor_tax_amt, COALESCE (sum(bct.tax_amount), 0) - "
      + "(COALESCE(sum(pbccl.tax_amt), 0) + COALESCE(sum(sbccl.tax_amt), 0)) as patient_tax_amt "
      + " FROM stock_issue_details sid "
      + " LEFT JOIN bill_activity_charge bac  ON sid.item_issue_no::varchar = bac.activity_id AND"
      + " activity_code = 'PHI' AND payment_charge_head = 'INVITE' "
      + " LEFT JOIN bill_charge bc  ON bc.charge_id = bac.charge_id "
      + " LEFT JOIN bill_charge_tax bct ON bc.charge_id = bct.charge_id "
      + " LEFT JOIN item_sub_groups isg ON bct.tax_sub_group_id = isg.item_subgroup_id "
      + " LEFT JOIN item_sub_groups_tax_details isgtd ON"
      + " isg.item_subgroup_id = isgtd.item_subgroup_id "
      + " LEFT JOIN bill_claim pbcl ON (bc.bill_no = pbcl.bill_no AND pbcl.priority = 1) "
      + " LEFT JOIN bill_claim sbcl ON (bc.bill_no = sbcl.bill_no AND sbcl.priority = 2) "
      + " LEFT JOIN bill_charge_claim pbccl ON (bc.charge_id = pbccl.charge_id AND"
      + " pbcl.claim_id = pbccl.claim_id ) "
      + " LEFT JOIN bill_charge_claim sbccl ON (bc.charge_id = sbccl.charge_id AND"
      + " sbcl.claim_id = sbccl.claim_id) "
      + " WHERE user_issue_no = ? " + " GROUP BY isg.item_subgroup_name, isgtd.tax_rate";

  public static List<BasicDynaBean> getTaxSummary(Integer issueNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_TAX_SUMMARY);
      ps.setInt(1, issueNo);
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
