package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.QueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoreItemStock {

	static Logger logger = LoggerFactory.getLogger(StoreItemStock.class);

	private static final String GET_ITEMS_IN_STOCK_STORE_WISE =
		"SELECT DISTINCT itd.item_name " +
		" FROM store_stock_details imsd " +
		" JOIN store_item_batch_details sibd ON(sibd.item_batch_id = imsd.item_batch_id)" +
		" JOIN store_item_details itd USING(medicine_id) where imsd.dept_id=? and itd.status='A' and qty_avbl>0 ";

	private  static final String  GET_STORE_LIST="SELECT dept_id from stores";

	public static HashMap getItemNamesInStock() throws SQLException {
		ResultSet rs=null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;

		try (PreparedStatement ps11 = con.prepareStatement(GET_ITEMS_IN_STOCK_STORE_WISE)) {
			HashMap storeWiseMedMap = new HashMap();
			
			ps = con.prepareStatement(GET_STORE_LIST);
			rs=ps.executeQuery();

			while (rs.next()){
			  ps11.setInt(1, rs.getInt("DEPT_ID"));
				storeWiseMedMap.put(String.valueOf(rs.getInt("DEPT_ID")), DataBaseUtil.queryToArrayList1(ps11))	;
			}
			return storeWiseMedMap;
		} finally {
		  DataBaseUtil.closeConnections(con, ps);
      if(rs!=null) {
        rs.close();
      }
		}
	}

	private final static String SERV_NO_OF_REAGENTS_REQUIRED_QUERY = "select count(*) from service_consumables where service_id= ? and status='A'";
	private final static String SERV_REAGENTS_REQUIRED_QUERY = "SELECT consumable_id,quantity_needed from service_consumables where service_id=? and status='A'";
	private final static String DIAG_NO_OF_REAGENTS_REQUIRED_QUERY = "SELECT COUNT(*) FROM diagnostics_reagents WHERE test_id = ? and status='A'";
	private final static String DIAG_REAGENTS_REQUIRED_QUERY = "SELECT reagent_id,quantity_needed FROM diagnostics_reagents WHERE test_id=? and status='A'";

	private final static String OT_NO_OF_REAGENTS_REQUIRED_QUERY = "SELECT COUNT(*) FROM ot_consumables WHERE operation_id = ? and status='A'";
	private final static String OT_REAGENTS_REQUIRED_QUERY = "SELECT consumable_id,qty_needed FROM ot_consumables WHERE operation_id=? and status='A'";

	private final static String NO_OF_REAGENTS_AVBL_IN_STOCK = "SELECT COUNT(distinct ssd.item_batch_id) FROM store_stock_details ssd " +
					" JOIN store_item_batch_details sibd ON(sibd.item_batch_id = ssd.item_batch_id) " +
					" where sibd.medicine_id = ? and ssd.dept_id = ? and asset_approved='Y' ";
	private final static String REAGENTS_AVBL_IN_STOCK = " SELECT  distinct sibd.item_batch_id,sibd.batch_no,sum(ssd.qty) as qty FROM store_stock_details ssd  " +
					" JOIN store_item_batch_details sibd ON(sibd.item_batch_id = ssd.item_batch_id)  ";


	private final static String REAGENTS_AVBL_IN_STOCK_FOR_CONSUMABLES =
		" SELECT sum(qty) as qty FROM store_stock_details ssd "+
		" JOIN store_item_batch_details sibd on(sibd.item_batch_id = ssd.item_batch_id) "+
		" WHERE exp_dt >= current_date AND ssd.medicine_id=? AND ssd.dept_id=? and asset_approved='Y'";

	private final static String GET_ITEM_CAT_IDENTIFICATION = "select identification from store_category_master "
	    +" join store_item_details on med_category_id=category_id where medicine_id=? ";

	private final static String GET_ITEM_BATCH_ID = " SELECT sibd.item_batch_id " +
			" FROM store_item_batch_details sibd WHERE sibd.medicine_id = ? AND sibd.batch_no =? ";

	public static boolean updateReagents( Connection con, String id ,int prescId,
			 String userName,int storeId,List reagentsRequired,int reagent_usage_seq, String module) throws SQLException, IOException, ParseException{
		
		 return  updateReagents(con,id ,prescId, userName,storeId, reagentsRequired, reagent_usage_seq,  module, new StringBuilder());
	}
	
	public static boolean updateReagents( Connection con, String id ,int prescId,
			 String userName,int storeId,List reagentsRequired,int reagent_usage_seq, String module, StringBuilder flashmsg)
		throws SQLException, IOException,ParseException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement psmt = null;
		PreparedStatement identifiersPs = null;
		ResultSet identifiersRs = null;
		boolean status = !module.equals("services");
		boolean serialInsufficentQty = false;
		String reagentsCountQuery = "";
		String reagentsQuery = "";int reagentSeq = 0;
		boolean entryRequired = false;
		String reagentType = module.equals("services") ? "S":module.equals("diagnostics") ? "D" : "O";
		GenericPreferencesDTO pref = GenericPreferencesDAO.getGenericPreferences();

		BasicDynaBean bean = null;
		BasicDynaBean reagentBean = null;
		Map<String, Object> keys = new HashMap<String, Object>();
		Map statusMap = null;

		if ( module.equals("services")) {       // for services
			reagentsCountQuery = SERV_NO_OF_REAGENTS_REQUIRED_QUERY;
			reagentsQuery = SERV_REAGENTS_REQUIRED_QUERY;
		} else if(module.equals("OT")){   // for OT
			reagentsCountQuery = OT_NO_OF_REAGENTS_REQUIRED_QUERY;
			reagentsQuery = OT_REAGENTS_REQUIRED_QUERY;
		} else{          // for Diag Tests
			reagentsCountQuery = DIAG_NO_OF_REAGENTS_REQUIRED_QUERY;
			reagentsQuery = DIAG_REAGENTS_REQUIRED_QUERY;
		}
			PreparedStatement cPs = con.prepareStatement(reagentsCountQuery);
			cPs.setString(1, id);
			int noOfReagentsReq = Integer.parseInt(DataBaseUtil.getStringValueFromDb(cPs));

			Integer[] reagentRequired = new Integer[reagentsRequired != null?reagentsRequired.size():noOfReagentsReq];
			BigDecimal[] qtyRequired = new BigDecimal[reagentsRequired != null?reagentsRequired.size():noOfReagentsReq];
			BigDecimal[] actualQty = new BigDecimal[reagentsRequired != null?reagentsRequired.size():noOfReagentsReq];
			/*int[] reagentRequired = new int[reagentsRequired.size()];
			BigDecimal[] qtyRequired = new BigDecimal[reagentsRequired.size()];
			BigDecimal[] actualQty = new BigDecimal[reagentsRequired.size()];
*/
			ps = con.prepareStatement( reagentsQuery );
			ps.setString(1, id);
			rs = ps.executeQuery();
			int j = 0;
			if(reagentsRequired == null){
				while (rs.next()){
					reagentRequired[j] = rs.getInt(1);
					qtyRequired[j] = rs.getBigDecimal(2);
					actualQty[j] = rs.getBigDecimal(2);
					j ++;
				}
			}
			rs.close();
			ps.close();
			int noOfReagents = j;
			if(reagentsRequired != null){
				for(int i = 0;i<reagentsRequired.size();i++){
					noOfReagents = reagentsRequired.size();
					DynaBean regaents = (DynaBean)reagentsRequired.get(i);
					reagentRequired[i] = (Integer)regaents.get("item_id");
					qtyRequired[i] = (BigDecimal)regaents.get("redusing_qty");
					actualQty[i] = (BigDecimal)regaents.get("qty");
				}
			}
			boolean AllowconsumableStockNegative = true;
			if(module.equals("services")) {
			  PreparedStatement pst = null;
			  try {
  				for(int r=0; r<noOfReagents; r++) {
  				  pst = con.prepareStatement(REAGENTS_AVBL_IN_STOCK_FOR_CONSUMABLES);
  					pst.setInt(1, reagentRequired[r]);
  					pst.setInt(2, storeId);
  					BasicDynaBean qtyBean = DataBaseUtil.queryToDynaBean(pst);
  
  					if(qtyRequired[r].compareTo(BigDecimal.ZERO)==0) {
  						AllowconsumableStockNegative = true;
  					} else {
  						if(pref.getConsumableStockNegative().equals("N"))
  							AllowconsumableStockNegative = !( qtyBean == null || null == qtyBean.get("qty") || ((BigDecimal)qtyBean.get("qty")).compareTo(qtyRequired[r]) <= -1 );
  						 else
  							if(qtyBean== null || null==qtyBean.get("qty")) AllowconsumableStockNegative = false;
  					}
  				}
			  } finally {
			    if (pst != null) {
			      pst.close();
			    }
			  }
			}


			StringBuilder query = new StringBuilder(REAGENTS_AVBL_IN_STOCK);
			List<Integer> reagentRequiredList = Arrays.asList(reagentRequired);
			QueryBuilder.addWhereFieldOpValue(false, query, "sibd.medicine_id", "IN", reagentRequiredList);
			PreparedStatement pstmt = con.prepareStatement(query +" AND ssd.dept_id=? and asset_approved='Y'  GROUP BY sibd.item_batch_id,sibd.batch_no ORDER BY sibd.item_batch_id");

			int index = 1;

			for (int reagentId : reagentRequiredList) {
				pstmt.setInt(index, reagentId);
				index++;
			}
			pstmt.setInt(index++, storeId);
			BasicDynaBean isExistsReagent = DataBaseUtil.queryToDynaBean(pstmt);
			if (isExistsReagent != null) {
				entryRequired = true;
			}
			
			GenericDAO storeReagentUsageMainDAO = new GenericDAO("store_reagent_usage_main");

			if(reagent_usage_seq == 0){
				reagentSeq =  storeReagentUsageMainDAO.getNextSequence();
				if ( noOfReagentsReq > 0 && entryRequired ) {
					bean = storeReagentUsageMainDAO.getBean();
					bean.set("store_id",storeId);
					java.util.Date d = new java.util.Date();
					java.sql.Timestamp dt =new java.sql.Timestamp(d.getTime());
					bean.set("date_time", dt);
					bean.set("consumer_id",id);
					bean.set("user_name",userName);
					bean.set("ref_no", prescId);
					bean.set("reagent_usage_seq", reagentSeq);
					bean.set("reagent_type", reagentType);
					status = storeReagentUsageMainDAO.insert(con, bean);
				}
			}else {
				reagentSeq = reagent_usage_seq;
			}

			String itemIdentification = "";
			GenericDAO adjDAo = new GenericDAO("store_reagent_usage_details");
			PreparedStatement identPs = null ;
			if(entryRequired) {
			  try {
  				for ( int reagent=0; reagent < noOfReagents; reagent++ ) {
  					cPs = con.prepareStatement(NO_OF_REAGENTS_AVBL_IN_STOCK);
  					cPs.setInt(1, reagentRequired[reagent] );
  					cPs.setInt(2, storeId);
  					int noOfItemIdents = Integer.parseInt(DataBaseUtil.getStringValueFromDb(cPs));
  					String identifierPsQry = "";
  					if(pref.getConsumableStockNegative().equals("N"))
  					  identifierPsQry = REAGENTS_AVBL_IN_STOCK +" WHERE sibd.medicine_id=? AND ssd.dept_id=? and asset_approved='Y' AND exp_dt >= current_date and ssd.qty > 0 GROUP BY sibd.item_batch_id,sibd.batch_no ORDER BY item_batch_id";
  					else
  					  identifierPsQry = REAGENTS_AVBL_IN_STOCK +" WHERE sibd.medicine_id=? AND ssd.dept_id=? and asset_approved='Y' GROUP BY sibd.item_batch_id,sibd.batch_no ORDER BY item_batch_id";
  					try (PreparedStatement identifiersPsq = con.prepareStatement(identifierPsQry)) {
    					identifiersPsq.setInt(1, reagentRequired[reagent]);
    					identifiersPsq.setInt(2, storeId);
    					identifiersRs = identifiersPsq.executeQuery();
	  					identPs = con.prepareStatement(GET_ITEM_CAT_IDENTIFICATION);
	  					identPs.setInt(1, reagentRequired[reagent] );
	  					itemIdentification = DataBaseUtil.getStringValueFromDb(identPs);
	  
	  					BigDecimal requiredQty = null;
	  					BigDecimal reduceQty = new BigDecimal(0);
	  					int identifierCount = 1;
	  
	  					requiredQty = qtyRequired[reagent];
	  					if(requiredQty.compareTo(BigDecimal.ZERO)==0) status = true;
	  					while (identifiersRs.next()) {
	  						serialInsufficentQty = false;
	  						reduceQty = new BigDecimal(0);
	  						BigDecimal qtyAvblInStock = identifiersRs.getBigDecimal("qty");
	  						String identifier = identifiersRs.getString("batch_no");
	  						BigDecimal remainQtyInStock = qtyAvblInStock.subtract(requiredQty);
	  						int remQtyCheck = remainQtyInStock.compareTo(BigDecimal.ZERO);
	  						if (noOfItemIdents == identifierCount) {
	  							if (remainQtyInStock.floatValue() > 0)reduceQty = requiredQty;
	  							else {
	  								if (itemIdentification.equalsIgnoreCase("B")) reduceQty = requiredQty;
	  								else {
	  									if (qtyAvblInStock.floatValue() > 0) reduceQty = qtyAvblInStock;
	  									else reduceQty = new BigDecimal("0");
	  									serialInsufficentQty = true;
	  									if (qtyAvblInStock.floatValue() < requiredQty.floatValue())
	  										logger.error("@  "+module+" :"+id+",Conduction time Item Id :"+reagentRequired[reagent]+" has insufficient stock in "+module+" STORE ");
	  								}
	  							}
	  						}else if (!(remQtyCheck > -2 &&  remQtyCheck < 0)){
	  							reduceQty = requiredQty;
	  						}else if (remQtyCheck > 0 && remQtyCheck < 2){
	  							reduceQty = qtyAvblInStock;
	  						}
	  						identifierCount++;
	  
	  						int itemBatchId = identifiersRs.getInt("item_batch_id");
	  
	  						if(reagent_usage_seq == 0){
	  							reagentBean = adjDAo.getBean();
	  							int reagentUsageDetailsId = adjDAo.getNextSequence();
	  							reagentBean.set("reagent_usage_det_id", reagentUsageDetailsId);
	  							reagentBean.set("item_batch_id", itemBatchId);
	  							reagentBean.set("obsolete_medicine_id",reagentRequired[reagent]);
	  							reagentBean.set("obsolete_batch_no",identifier);
	  							reagentBean.set("qty",itemIdentification.equalsIgnoreCase("B") ? reduceQty : BigDecimal.ONE);
	  							reagentBean.set("ref_no",prescId);
	  							reagentBean.set("reagent_usage_seq", reagentSeq);
	  
	  							//if(status)
	  								statusMap = new StockFIFODAO().reduceStock(con, storeId,itemBatchId,
	  										module.equals("services") ? "SC":module.equals("diagnostics") ?"TC":"OC",reduceQty, null, userName,
	  										module.equals("services") ? "Services":module.equals("diagnostics") ?"Tests":"Operation"+" Consumable Usage",
	  										reagentUsageDetailsId,pref.getConsumableStockNegative().equals("Y"));
	  
	  							if(statusMap != null) {
	  								status = (Boolean)statusMap.get("status");
	  								if(!status  && pref.getConsumableStockNegative().equals("N") ){
	  									if(flashmsg != null){
	  										flashmsg.append(statusMap.get("medicine_name")+"<br>");
	  									}
	  								
	  								}
	  								reagentBean.set("cost_value", (BigDecimal)statusMap.get("costValue"));
	  							}
	  							int redQtyCheck = reduceQty.compareTo(BigDecimal.ZERO);
	  							if ((status && (redQtyCheck > 0 && redQtyCheck < 2)) || (status && serialInsufficentQty)) {
	  								status = adjDAo.insert(con, reagentBean);
	  							}
	  
	  						}else{
	  							keys.put("reagent_usage_seq", reagentSeq);
	  							keys.put("ref_no", prescId);
	  							for(int i = 0;i<reagentsRequired.size();i++){
	  								DynaBean regaents = (DynaBean)reagentsRequired.get(i);
	  								keys.put("obsolete_medicine_id", (Integer)regaents.get("item_id"));
	  								reagentBean = adjDAo.getBean();
	  								reagentBean.set("qty",(BigDecimal)regaents.get("qty"));
	  								status = adjDAo.update(con, reagentBean.getMap(), keys) > 0;
	  							}
	  						}
	  						reagentBean = null;
	  
	  						requiredQty = requiredQty.subtract(reduceQty);
	  						if (requiredQty.compareTo(BigDecimal.ZERO) == 0 )
	  							break;
	  					}
	  					if(identifierCount == 1){
	  						if(flashmsg != null &&  requiredQty.compareTo(BigDecimal.ZERO) != 0){
	  						  try (PreparedStatement psmt1 = con.prepareStatement("select medicine_name from store_item_details where medicine_id=?");) {
	  						    psmt1.setInt(1, reagentRequired[reagent] );
	                  flashmsg.append(DataBaseUtil.getStringValueFromDb(psmt1)+"<br>");
	  						  }
	  						  
	  						}
	  					}
	  					
	  					identifiersRs.close();
	  				}
  				}
  			} finally {
  			  if (cPs != null) {
  			    cPs.close();
          }
  			  if (identifiersPs != null) {
  			    identifiersPs.close();
  			  }
  			  if (identPs != null) {
  			    identPs.close();
          }
  			  if (psmt != null) {
  			    psmt.close();
  			  }
  			}
			} else {
			  return true;
			}
				
		return status;
	}
	public static boolean updateStock(Connection con,int itemBatchId,BigDecimal qty,int storeId,String module)throws SQLException{
		   PreparedStatement ps = null;
		   boolean status = false;
		   try{
			   ps = con.prepareStatement("UPDATE store_stock_details SET QTY=QTY-?,CHANGE_SOURCE=? WHERE DEPT_ID=? AND ITEM_BATCH_ID=?");
			   ps.setBigDecimal(1,qty);
			   ps.setString(2, module+" Consumable usage");
			   ps.setInt(3, storeId);
			   ps.setInt(4, itemBatchId);
			   if(ps.executeUpdate() > 0)status = true;
		   }finally{
			   if(ps != null)ps.close();
		   }
		   return status;
	   }

	private static String  GET_REAGENTS_OR_CONSUMABLES = "select sibd.medicine_id,ref_no,store_id,consumer_id,date_time,user_name,irm.reagent_usage_seq" +
			",i.medicine_name,'true' as status,qty from  store_reagent_usage_main  irm" +
			" join store_reagent_usage_details srud using(ref_no) " +
			" JOIN store_item_batch_details sibd ON(sibd.item_batch_id = srud.item_batch_id)" +
			" join store_item_details i using(medicine_id) where ref_no = ? and store_id=?" +
			" group by sibd.medicine_id,ref_no,store_id,consumer_id,date_time,user_name,irm.reagent_usage_seq,i.medicine_name,qty ";

	public List  getReagentsorConsumablesUsed(int refno,String store_id)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_REAGENTS_OR_CONSUMABLES);
			ps.setInt(1,refno);
			ps.setString(2, store_id);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			if(ps != null)ps.close();
			if(con != null)con.close();
		}

	}


}