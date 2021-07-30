package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class StoresStockAdjustDAO{

static Logger logger = LoggerFactory.getLogger(StoresStockAdjustDAO.class);

private static final String ITEMS_FROM_A_STORE =
		" SELECT pm.dept_id, pm.medicine_id, sibd.batch_no,sibd.item_batch_id, icm.category," +
		"	 icm.category_id, pmd.medicine_name,pmd.cust_item_code, pm.qty, pm.qty_in_use, " +
		"	 pm.qty_maint,pm.qty_retired, pm.qty_lost,pmd.item_barcode_id, " +
		"	 pm.qty_unknown,pm.consignment_stock, pmd.issue_units, pmd.issue_units, " +
		"	 to_char(sibd.exp_dt, 'dd-mm-yyyy') as exp_dt,icm.identification," +
		"	 pm.package_cp,pm.item_lot_id " +
		" FROM (SELECT medicine_id,sum(qty) as qty,item_batch_id,batch_no," +
		"			sum(qty_in_use) as qty_in_use,sum(qty_maint) as qty_maint, " +
		" 			sum(qty_retired) as qty_retired,sum(qty_lost) as qty_lost,sum(qty_kit) as qty_kit, " +
        " 			sum(qty_unknown) as qty_unknown,consignment_stock," +
        "			asset_approved,dept_id,min(tax) as tax ,package_cp,item_lot_id," +
        "			lot_source,purchase_type,lot_time " +
		" 		FROM store_stock_details  GROUP BY batch_no,item_batch_id," +
		"			 medicine_id,consignment_stock,asset_approved,dept_id,package_cp,item_lot_id " +
		"  		ORDER BY medicine_id) pm" +
		" JOIN store_item_batch_details sibd USING(item_batch_id)" +
		" JOIN store_item_details pmd ON ( pmd.medicine_id = pm.medicine_id ) " +
		" JOIN store_category_master icm ON ( icm.category_id = pmd.med_category_id )" +
		" WHERE pm.medicine_id = pmd.medicine_id AND pmd.med_category_id = icm.category_id AND pm.asset_approved = 'Y' " +
		" AND dept_id = ? AND pmd.medicine_id = ?";
public List getItemsFromStore(String storeId, String itemId) throws SQLException, Exception {
		Connection con = null;
		PreparedStatement ps = null;
		List itemList = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(ITEMS_FROM_A_STORE);
			int itemidLocal = -1;
			int deptIdNum = -1;
			if ((storeId != null) && (storeId.length() > 0)){
				deptIdNum = Integer.parseInt(storeId);
			}
			if (itemId != null && !itemId.equals(""))
				itemidLocal = Integer.parseInt(itemId);
			ps.setInt(1, deptIdNum);
			ps.setInt(2, itemidLocal);
			itemList = DataBaseUtil.queryToArrayList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return itemList;
	}

private static final String ITEMS_FOR_REJ_INDENT =
	" SELECT pm.dept_id, pm.medicine_id, sibd.batch_no, icm.category," +
	"	 icm.category_id, pmd.medicine_name,pmd.cust_item_code, pm.qty, pm.qty_in_use, " +
	"	 pm.qty_maint,pm.qty_retired, pm.qty_lost, " +
	"	 pm.qty_unknown,pst.indent_no, pmd.issue_base_unit as issue_units," +
	" 	 CASE " +
	"		WHEN pm.qty_in_transit > pst.qty_rejected THEN pst.qty_rejected " +
	"		WHEN pm.qty_in_transit = pst.qty_rejected THEN pm.qty_in_transit "+
	" 	 	WHEN pm.qty_in_transit < pst.qty_rejected THEN pm.qty_in_transit " +
	"	 END as qty_rejected,pm.consignment_stock, pmd.issue_units, to_char(sibd.exp_dt, 'dd-mm-yyyy') as exp_dt " +
	" FROM store_stock_details pm " +
	" JOIN store_item_batch_details sibd USING(item_batch_id) " +
	" JOIN store_item_details pmd using (medicine_id) "+
	" JOIN store_stock_transfer_view pst ON pm.medicine_id = pst.medicine_id and pm.batch_no = pst.batch_no and pm.dept_id = pst.store_from "+
	" JOIN store_category_master icm ON pmd.med_category_id = icm.category_id " +
	" 		AND pst.transfer_no IN " +
	"			( select max(transfer_no) FROM store_stock_transfer_view pstv where pstv.medicine_id = "+
	" 					pst.medicine_id AND pstv.batch_no = pst.batch_no " +
	"					AND pstv.store_from = pst.store_from AND pstv.indent_no = pst.indent_no ) ";


public List getItemsListFromStore(String store, List itemId, List batchno, List indentno) throws SQLException, Exception {
	Connection con = null;
	PreparedStatement ps = null;
	List itemList = null;
	try{
		con = DataBaseUtil.getConnection();
		int storeIdNum = -5;
		if (store!=null && (!store.equals(""))){

			storeIdNum = Integer.parseInt(store);

		}
		if (storeIdNum > -5){
			StringBuilder where = new StringBuilder();

			DataBaseUtil.addWhereFieldInList(where, "pst.batch_no", batchno);
			DataBaseUtil.addWhereFieldInList(where, "pst.medicine_id", itemId);
			DataBaseUtil.addWhereFieldInList(where, "pst.indent_no", indentno);

			StringBuilder query = new StringBuilder(ITEMS_FOR_REJ_INDENT);
			query.append(where);
			query.append(" AND dept_id=?  AND pm.asset_approved = 'Y' and qty_in_transit > 0 " );
			ps =con.prepareStatement(query.toString());

			int i = 1;

			if (batchno != null) {
				Iterator it = batchno.iterator();
				while (it.hasNext()) {
					ps.setString(i, (String) it.next());
					i++;
				}
			}
			if (itemId != null) {
				Iterator it = itemId.iterator();
				while (it.hasNext()) {
					int medIdNum = -1;
					String medId = (String)it.next();
					if ((medId != null) && (medId.trim().length() > 0)){
						medIdNum = Integer.parseInt(medId);
					}
					if (medIdNum > 0){
						ps.setInt(i, medIdNum);
					}
					i++;
				}
			}

			if (indentno != null) {
				Iterator it = indentno.iterator();
				while (it.hasNext()) {
					int indentNum = Integer.parseInt((String)it.next());

					ps.setInt(i, indentNum);

					i++;
				}
			}

			ps.setInt(i, storeIdNum);
			itemList = DataBaseUtil.queryToArrayList(ps);
		} else{
			logger.error("No Department Selected");
		}
	}finally{
		DataBaseUtil.closeConnections(con, ps);
	}
	return itemList;
}



private static final String CATS_FROM_A_STORE = "SELECT distinct icm.category_id, icm.category, pm.dept_id,icm.identification " +
		" FROM store_stock_details pm,store_item_details pd,store_category_master icm " +
		" WHERE pm.medicine_id = pd.medicine_id and pd.med_category_id = icm.category_id AND pm.asset_approved = 'Y' AND dept_id=? and icm.status='A'";
public List getCategoriesFromStore(String storeId) throws SQLException, Exception {
		Connection con = null;
		PreparedStatement ps = null;
		List itemList = null;
		int deptIdNum = -1;
		try{
		con = DataBaseUtil.getConnection();
		ps = con.prepareStatement(CATS_FROM_A_STORE);
		if (storeId != null && !storeId.equals(""))
			deptIdNum = Integer.parseInt(storeId);
		ps.setInt(1, deptIdNum);
		itemList = DataBaseUtil.queryToArrayList(ps);
		}finally{
		DataBaseUtil.closeConnections(con, ps);
		}
		return itemList;
}

private static final String ITEM_FROM_A_STORE =
	    "SELECT distinct icm.category_id, icm.category, pd.medicine_id, pd.medicine_name,pd.cust_item_code, pm.dept_id,pd.item_barcode_id, " +
	    " CASE WHEN pd.cust_item_code IS NOT NULL AND  TRIM(pd.cust_item_code) != ''  THEN pd.medicine_name||' - '||pd.cust_item_code ELSE pd.medicine_name END as cust_item_code_with_name"+
		" FROM store_stock_details pm,store_item_details pd,store_category_master icm " +
		" WHERE pm.medicine_id = pd.medicine_id and pd.med_category_id = icm.category_id AND pm.asset_approved = 'Y' " +
		" AND (pd.status = 'A' OR (pd.status='I' AND pm.qty != 0))" +
		" AND dept_id=? order by medicine_name";
public List getItemFromStore(String storeId) throws SQLException, Exception {
		Connection con = null;
		PreparedStatement ps = null;
		List itemList = null;
		int deptIdNum = -1;
		try{
		con = DataBaseUtil.getConnection();
		ps = con.prepareStatement(ITEM_FROM_A_STORE);
		if (storeId != null && !storeId.equals(""))
			deptIdNum = Integer.parseInt(storeId);
		ps.setInt(1, deptIdNum);
		itemList = DataBaseUtil.queryToDynaList(ps);
		}finally{
		DataBaseUtil.closeConnections(con, ps);
		}
		return itemList;
}
private static final String ITEM_FROM_A_STORE_WITHOUT_APPROVAL = "SELECT distinct icm.category_id, pm.medicine_id, pd.medicine_name,pd.issue_qty,pd.issue_units,pd.cust_item_code, " +
		" CASE WHEN pd.cust_item_code IS NOT NULL AND  TRIM(pd.cust_item_code) != ''  THEN pd.medicine_name||' - '||pd.cust_item_code ELSE pd.medicine_name END as cust_item_code_with_name "+
		" FROM store_item_details pd join store_stock_details pm using(nedicine_id) " +
        " join store_category_master icm on pd.med_category_id = icm.category_id AND  icm.issue_type='C' where pm.dept_id=? ";
public List getItemFromStoreWithoutApproval(String storeId) throws SQLException, Exception {
		Connection con = null;
		PreparedStatement ps = null;
		List itemList = null;
		int deptIdNum = -1;
		try{
		con = DataBaseUtil.getConnection();
		ps = con.prepareStatement(ITEM_FROM_A_STORE_WITHOUT_APPROVAL);
		if (storeId != null && !storeId.equals(""))
			deptIdNum = Integer.parseInt(storeId);
		ps.setInt(1, deptIdNum);
		itemList = DataBaseUtil.queryToArrayList(ps);
		}finally{
		DataBaseUtil.closeConnections(con, ps);
		}
		return itemList;
}

private static String UPDATE_FIELDS1= "UPDATE store_stock_details SET ";
private static StringBuilder UPDATE_WHERE_CONDITION = new StringBuilder(" WHERE dept_id = ? AND item_lot_id = ?");

  public boolean update(Connection con, List<StockAdjustDTO> stockList, String username)
      throws SQLException {

    int rows = 0;
    StringBuilder update_fields = null;
    boolean success = true;
    for (StockAdjustDTO listItem : stockList) {
      update_fields = new StringBuilder(UPDATE_FIELDS1);
      if (listItem.getStockStatus().equalsIgnoreCase("A")) {
        update_fields.append("qty = qty + ? , ");
        update_fields.append("remarks_avbl = ?,");
      } else if (listItem.getStockStatus().equalsIgnoreCase("M")) {
        update_fields.append("qty_maint = qty_maint + ?, ");
        update_fields.append("remarks_maint = ?,");
      } else if (listItem.getStockStatus().equalsIgnoreCase("L")) {
        update_fields.append("qty_lost = qty_lost + ?, ");
        update_fields.append("remarks_lost = ?,");
      } else if (listItem.getStockStatus().equalsIgnoreCase("U")) {
        update_fields.append("qty_unknown = qty_unknown + ?,");
        update_fields.append("remarks_unknown = ?,");
      } else if (listItem.getStockStatus().equalsIgnoreCase("R")) {
        update_fields.append("qty_retired = qty_retired + ?,");
        update_fields.append("remarks_retired = ?,");

      } else if (listItem.getStockStatus().equalsIgnoreCase("T")) {
        update_fields.append("qty_in_transit = qty_in_transit + ?,");
        update_fields.append("remarks_transit = ?,");

      }
      update_fields.append("username = ?,");
      update_fields.append("change_source = ?");
      update_fields.append(UPDATE_WHERE_CONDITION);
      try (PreparedStatement ps = con.prepareStatement(update_fields.toString());) {
        BigDecimal qty = null;
        if (listItem.getQty() != null && !listItem.getQty().equals("")) {
          if (listItem.getIncType().equalsIgnoreCase("A"))
            qty = new BigDecimal(listItem.getQty());
          else
            qty = BigDecimal.ZERO.subtract(new BigDecimal(listItem.getQty()));
        } else {
          qty = new BigDecimal(0);
        }
        ps.setBigDecimal(1, qty);
        ps.setString(2, listItem.getRemarks());
        ps.setString(3, username);
        ps.setString(4, "StockAdjust");
        ps.setInt(5, listItem.getStoreId());
        ps.setInt(6, listItem.getItemLotId());
        rows = ps.executeUpdate();
      }
      if (rows <= 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  public boolean updateRejIndentAdjust(Connection con, List<StockAdjustDTO> stockList,
      String username, String[] indentno) throws SQLException, Exception {

    int rows = 0;
    StringBuilder update_fields = null;
    boolean success = true;
    for (StockAdjustDTO listItem : stockList) {
      update_fields = new StringBuilder(UPDATE_FIELDS1);
      if (listItem.getStockStatus().equalsIgnoreCase("A")) {
        update_fields.append("qty = qty + ? , ");
        update_fields.append("remarks_avbl = ?,");
      } else if (listItem.getStockStatus().equalsIgnoreCase("M")) {
        update_fields.append("qty_maint = qty_maint + ?, ");
        update_fields.append("remarks_maint = ?,");
      } else if (listItem.getStockStatus().equalsIgnoreCase("L")) {
        update_fields.append("qty_lost = qty_lost + ?, ");
        update_fields.append("remarks_lost = ?,");
      } else if (listItem.getStockStatus().equalsIgnoreCase("U")) {
        update_fields.append("qty_unknown = qty_unknown + ?,");
        update_fields.append("remarks_unknown = ?,");
      } else if (listItem.getStockStatus().equalsIgnoreCase("R")) {
        update_fields.append("qty_retired = qty_retired + ?,");
        update_fields.append("remarks_retired = ?,");

      }
      update_fields.append("qty_in_transit = qty_in_transit - ?,");
      update_fields.append("username = ?,");
      update_fields.append("change_source = ?");
      update_fields.append(UPDATE_WHERE_CONDITION);
      try (PreparedStatement ps = con.prepareStatement(update_fields.toString());) {
        BigDecimal qty = null;
        if (listItem.getQty() != null && !listItem.getQty().equals("")) {
          if (listItem.getIncType().equalsIgnoreCase("A"))
            qty = new BigDecimal(listItem.getQty());
          else
            qty = BigDecimal.ZERO.subtract(new BigDecimal(listItem.getQty()));
        } else {
          qty = new BigDecimal(0);
        }
        ps.setBigDecimal(1, qty);
        ps.setString(2, listItem.getRemarks());
        ps.setBigDecimal(3, qty);
        ps.setString(4, username);
        ps.setString(5, "StockAdjust");
        ps.setInt(6, listItem.getStoreId());
        ps.setInt(7, listItem.getItemId());
        ps.setInt(8, listItem.getItemBatchId());
        rows = ps.executeUpdate();
      }
      if (rows <= 0) {
        success = false;
        break;
      }
    }
    return success;
  }

	public List getItemsFromStore(String storeId, String itemId,String identifier) throws SQLException, Exception {
		Connection con = null;
		PreparedStatement ps = null;
		List itemList = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(ITEMS_FROM_A_STORE + " and pm.batch_no=?" );
			int id = -1;
			int deptIdNum = -1;
			if (itemId != null && !itemId.equals(""))
				id = Integer.parseInt(itemId);
			if (storeId != null && !storeId.equals(""))
				deptIdNum = Integer.parseInt(storeId);
			ps.setInt(1, deptIdNum);
			ps.setInt(2, id);
			ps.setString(3,identifier);
			itemList = DataBaseUtil.queryToArrayList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return itemList;
	}


	protected final static String INDENT_TRANSFER_ITEMS = "SELECT si.indent_no, si.medicine_id ,st.transfer_no,"
	      +" si.status, st.qty, CASE WHEN sd.qty_in_transit > st.qty_rejected THEN st.qty_rejected "
	      +" WHEN sd.qty_in_transit = st.qty_rejected THEN st.qty_rejected "
		  +" WHEN sd.qty_in_transit < st.qty_rejected THEN sd.qty_in_transit END as qty_rejected, st.qty_recd, "
		  +" st.store_from, st.batch_no, cat.identification "
	      +" FROM store_indent_details si"
	      +" JOIN store_stock_transfer_view st using (indent_no,medicine_id)"
	      +" JOIN store_stock_details sd ON st.medicine_id = sd.medicine_id and st.item_batch_id = sd.item_batch_id and st.store_from = sd.dept_id "
	      +" LEFT JOIN store_item_details sm ON sd.medicine_id = sm.medicine_id JOIN store_category_master cat on sm.med_category_id = cat.category_id"
	      +" WHERE st.indent_no = ? and st.medicine_id = ? and st.item_batch_id = ? and st.store_from = ? and st.qty_rejected = ?";

	public BasicDynaBean updateRejectStock(String indentno,String itemid,String batchno,BigDecimal adjustedqty,int storeid) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		BasicDynaBean stock_transfer = null;
		boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			stock_transfer = DataBaseUtil.queryToDynaBean(INDENT_TRANSFER_ITEMS, new Object[]{Integer.parseInt(indentno), Integer.parseInt(itemid), batchno, storeid, adjustedqty});
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return stock_transfer;
	}


}