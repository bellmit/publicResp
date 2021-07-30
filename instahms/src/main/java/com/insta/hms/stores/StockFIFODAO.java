package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StockFIFODAO extends GenericDAO {

	static Logger logger = LoggerFactory.getLogger(StockFIFODAO.class);
	private static final GenericDAO storeStockDetailsDAO = new GenericDAO("store_stock_details");
    private static final GenericDAO storeItemLotDetailsDAO = new GenericDAO("store_item_lot_details");
    private static final GenericDAO storeTransactionLotDetailsDAO = new GenericDAO("store_transaction_lot_details");



	public StockFIFODAO() {
		super("store_stock_details");
	}

	public Map reduceStock(Connection con,int storeid,int itembatchId,String transactionType,
			BigDecimal qty, BigDecimal kitQty, String username, String changeSource, int transactionId)
		throws SQLException,IOException {
		return reduceStock(con, storeid, itembatchId, transactionType, qty, kitQty, username,
				changeSource, transactionId, false);
	}

	public Map reduceStock(Connection con,int storeid,int itembatchId,String transactionType,
			BigDecimal qty, BigDecimal kitQty, String username, String changeSource, int transactionId,
			boolean allowNegative)
	throws SQLException,IOException {
		return reduceStock(con,storeid,itembatchId,transactionType,
				qty,kitQty,username,changeSource,transactionId,
				allowNegative,null);
	}

	public Map reduceStock(Connection con,int storeid,int itembatchId,String transactionType,
			BigDecimal qty, BigDecimal kitQty, String username, String changeSource, int transactionId,
			boolean allowNegative, String purchaseType)
	throws SQLException,IOException {
		return reduceStock(con,storeid,itembatchId,transactionType,
				qty,kitQty,username,changeSource,transactionId,
				allowNegative,purchaseType,null);
	}
	/**
	 * To reduce stock on FIFO base we need item_batch_id,store_id
	 * Stock can be reduced
	 */
	public Map reduceStock(Connection con,int storeid,int itembatchId,String transactionType,
			BigDecimal qty, BigDecimal kitQty, String username, String changeSource, int transactionId,
			boolean allowNegative,String purchaseType, String grnNo)
	throws SQLException,IOException {

		HashMap<String, Integer> lotKeys = new HashMap<String, Integer>();
		lotKeys.put("dept_id", storeid);
		lotKeys.put("item_batch_id", itembatchId);
		List<BasicDynaBean> itemStock = null;
		//R.C HMS-7205.logic of fetching batches shd be similar in case of grnNo != null case as well
		if(grnNo != null){
			itemStock = purchaseType != null ?
					getBatchLotDetails(con,storeid, itembatchId,purchaseType,grnNo) : listAll(con,null, lotKeys,"item_lot_id");
		} else {
			itemStock = purchaseType != null ?
					getBatchLotDetails(con,storeid, itembatchId,purchaseType) : listAll(con,null, lotKeys,"item_lot_id");
		}
		
		String qtyKey = ( kitQty != null ? "qty_kit" : "qty" );
		BigDecimal remainingQty = ( kitQty != null ? kitQty : qty );
		BigDecimal totalCostValue = BigDecimal.ZERO;
		Map statusMap = new HashMap();
		int medicine_id = 0;
		for (int i=0; i<itemStock.size(); i++) {

			BasicDynaBean stockDetails = itemStock.get(i);

			if (remainingQty.compareTo(BigDecimal.ZERO) <= 0)
				break;

			BigDecimal stockQty = (BigDecimal)stockDetails.get("qty");
			BigDecimal transactionQty = BigDecimal.ZERO;
			medicine_id = (Integer)stockDetails.get("medicine_id");
			if (stockQty.compareTo(remainingQty) > 0) {
				// no problem -- use up all what we need.
				transactionQty = remainingQty;
			} else {
				if (allowNegative && (i == itemStock.size()-1)) {
					// last lot, and we allow negative. We must use up remaining qty here itself.
					transactionQty = remainingQty;
				} else {
					transactionQty = stockQty;
					// try next lot
				}
			}

			if (transactionQty.compareTo(BigDecimal.ZERO) <= 0) {
				// no luck in this lot. Try next one.
				continue;
			}

			remainingQty = remainingQty.subtract(transactionQty);

			// update the stock details: reduce stock.
			addQtyToStockDetails(con, (Integer)stockDetails.get("store_stock_id"),
					transactionQty.negate(), username, changeSource);

			// insert transaction details
			insertTxnLot(con, transactionId, transactionType, (Integer) stockDetails.get("item_lot_id"),
					transactionQty);

			BasicDynaBean lot = getItemLotDetails((Integer)stockDetails.get("item_lot_id"));
			// cost is the whole transaction's actual cost, so package_cp needs divide by package size.
			BigDecimal cost = transactionQty.multiply((BigDecimal)lot.get("package_cp"));
			cost = ConversionUtils.divideHighPrecision(cost, (BigDecimal)lot.get("issue_base_unit"));
			totalCostValue = totalCostValue.add(cost);
		}

		if (remainingQty.compareTo(BigDecimal.ZERO) > 0 && !allowNegative) {
			statusMap.put("status", false);
			statusMap.put("statusReason", "Insufficient Quantity ");
			statusMap.put("left_qty", remainingQty);
			statusMap.put("medicine_name",(new GenericDAO("store_item_details").listAll(null, "medicine_id", medicine_id)).get(0).get("medicine_name").toString());
		} else {
			statusMap.put("status", true);
			statusMap.put("costValue", totalCostValue);
		}

		return statusMap;
	}

	public Map addStock(Connection con, int storeId, int transactionId, String transactionType,
			BigDecimal qty, String userName, String changeSource)
	throws SQLException,IOException {
		return addStock(con, storeId, transactionId, transactionType,
				qty, userName, changeSource, null);
	}

	/**
	 * Returning back  items to stock which requires item lot id which was used while reducing the stock.
	 * Note: this works only if there is a transaction that is being reversed. For fresh stock addition,
	 * this should not be used.
	 *
	 * If itemBatchId is given, it means that new lots will be inserted instead of updating the qty
	 * of an existing lot. This is used for supplier replacements.
	 */
	
	public Map addStock(Connection con, int storeId, int transactionId, String transactionType,
			BigDecimal qty, String userName, String changeSource, Integer refStore)
		throws SQLException, IOException {
		return addStock(con, storeId, transactionId, transactionType,
				qty, userName, changeSource, refStore,null,0);
		
	}
	
	
	public Map addStock(Connection con, int storeId, int transactionId, String transactionType,
			BigDecimal qty, String userName, String changeSource, Integer refStore,
			String returntransactionType,int returnTransactionId)
		throws SQLException, IOException {

		Map statusMap = new HashMap();
		boolean status = true;
		BigDecimal totalCostValue = BigDecimal.ZERO;

		List<BasicDynaBean> usedLots = getUsedLots(con,transactionId,transactionType);

		BigDecimal transactionQty = qty;
		BigDecimal remainingQty = transactionQty;
		statusMap.put("transaction_lot_exists", (usedLots.size() > 0));

		for (BasicDynaBean usedLot : usedLots) {
			int itemLotId = (Integer)usedLot.get("item_lot_id");
			BigDecimal lotQty = (BigDecimal)usedLot.get("qty");

			if (remainingQty.compareTo(BigDecimal.ZERO) == 0)
				break;

			if (lotQty.compareTo(BigDecimal.ZERO) == 0)
				continue;

			transactionQty = (remainingQty.compareTo(lotQty) > 0 ) ? lotQty : remainingQty;

			remainingQty = remainingQty.subtract(transactionQty);

			HashMap<String, Object> stockKeys = new HashMap<String, Object>();
			stockKeys.put("item_lot_id", itemLotId);
			stockKeys.put("dept_id", storeId);
			BasicDynaBean stockBean = storeStockDetailsDAO.findByKey(con, stockKeys);

			/** possible in case transfer is happening
			 *  and same batch exissts in the to store but
			 *  not this lot.Need to insert it from ref store lot.
			 */
			if (stockBean == null && refStore != null) {
				//get stock from reference store.
				stockKeys = new HashMap<String, Object>();
				stockKeys.put("item_lot_id", itemLotId);
				stockKeys.put("dept_id", refStore);
				stockBean = storeStockDetailsDAO.findByKey(con, stockKeys);

				transferStock(con, storeId, transactionQty, "StockTransfer", userName, stockBean);

			} else if (stockBean != null) {
				// update the stock details: add stock.
				addQtyToStockDetails(con, (Integer)stockBean.get("store_stock_id"),
						transactionQty, userName, changeSource);
			}

			// insert transaction lot (negative of original transaction)
			insertTxnLot(con, transactionId, transactionType, itemLotId, transactionQty.negate());
			
			if ( returntransactionType != null ) {
				//few transaction may do not need to track return transactions
				// insert transaction lot (negative of original transaction)
				insertTxnLot(con, returnTransactionId, returntransactionType, itemLotId, transactionQty.negate());
			}

			BasicDynaBean lot = getItemLotDetails(itemLotId);
			// cost is the whole transaction's actual cost, so package_cp needs divide by package size.
			BigDecimal cost = transactionQty.multiply((BigDecimal)lot.get("package_cp"));
			cost = ConversionUtils.divideHighPrecision(cost, (BigDecimal)lot.get("issue_base_unit"));
			totalCostValue = totalCostValue.add(cost);
		}

		statusMap.put("status", status);
		statusMap.put("costValue", totalCostValue);

		return statusMap;
	}

	public Map addTransitStock(Connection con, int storeId, int transactionId, String transactionType,
			BigDecimal qty, String userName, String changeSource, Integer refStore)
		throws SQLException, IOException {

		Map statusMap = new HashMap();
		boolean status = true;
		BigDecimal totalCostValue = BigDecimal.ZERO;

		List<BasicDynaBean> usedLots = getUsedLots(con,transactionId,transactionType);

		BigDecimal transactionQty = qty;
		BigDecimal remainingQty = transactionQty;
		statusMap.put("transaction_lot_exists", (usedLots.size() > 0));

		for (BasicDynaBean usedLot : usedLots) {
			int itemLotId = (Integer)usedLot.get("item_lot_id");
			BigDecimal lotQty = (BigDecimal)usedLot.get("qty");

			if (remainingQty.compareTo(BigDecimal.ZERO) == 0)
				break;

			if (lotQty.compareTo(BigDecimal.ZERO) == 0)
				continue;

			transactionQty = (remainingQty.compareTo(lotQty) > 0 ) ? lotQty : remainingQty;

			remainingQty = remainingQty.subtract(transactionQty);

			HashMap<String, Object> stockKeys = new HashMap<String, Object>();
			stockKeys.put("item_lot_id", itemLotId);
			stockKeys.put("dept_id", storeId);
			BasicDynaBean stockBean = storeStockDetailsDAO.findByKey(con, stockKeys);

			/** possible in case transfer is happening
			 *  and same batch exissts in the to store but
			 *  not this lot.Need to insert it from ref store lot.
			 */
			if (stockBean == null && refStore != null) {
				//get stock from reference store.
				stockKeys = new HashMap<String, Object>();
				stockKeys.put("item_lot_id", itemLotId);
				stockKeys.put("dept_id", refStore);
				stockBean = storeStockDetailsDAO.findByKey(con, stockKeys);

				transferStock(con, storeId, transactionQty, "StockTransfer", userName, stockBean, true);

			} else if (stockBean != null) {
				// update the stock details: add stock.
				addQtyToStockDetails(con, (Integer)stockBean.get("store_stock_id"),
						transactionQty, userName, changeSource, true);
			}

			// insert transaction lot (negative of original transaction)
			insertTxnLot(con, transactionId, transactionType, itemLotId, transactionQty.negate());

			BasicDynaBean lot = getItemLotDetails(itemLotId);
			// cost is the whole transaction's actual cost, so package_cp needs divide by package size.
			BigDecimal cost = transactionQty.multiply((BigDecimal)lot.get("package_cp"));
			cost = ConversionUtils.divideHighPrecision(cost, (BigDecimal)lot.get("issue_base_unit"));
			totalCostValue = totalCostValue.add(cost);
		}

		statusMap.put("status", status);
		statusMap.put("costValue", totalCostValue);

		return statusMap;
	}
	/**
	 * Add to stock with a new lot. New lots will be inserted instead of updating the qty
	 * of an existing lot. This is used for supplier replacements.
	 */
	public Map addStockWithNewLot(Connection con, int storeId, int transactionId, String transactionType,
			BigDecimal qty, String userName, String changeSource, int itemBatchId, String batchNo)
		throws SQLException, IOException {

		Map statusMap = new HashMap();
		boolean status = true;
		BigDecimal totalCostValue = BigDecimal.ZERO;

		List<BasicDynaBean> usedLots = getUsedLots(con, transactionId, transactionType);

		BigDecimal transactionQty = qty;
		BigDecimal remainingQty = transactionQty;
		statusMap.put("transaction_lot_exists", (usedLots.size() > 0));

		for (BasicDynaBean usedLot : usedLots) {
			int oldLotId = (Integer)usedLot.get("item_lot_id");
			BigDecimal lotQty = (BigDecimal)usedLot.get("qty");

			if (remainingQty.compareTo(BigDecimal.ZERO) == 0)
				break;

			if (lotQty.compareTo(BigDecimal.ZERO) == 0)
				continue;

			transactionQty = (remainingQty.compareTo(lotQty) > 0 ) ? lotQty : remainingQty;

			remainingQty = remainingQty.subtract(transactionQty);

			// insert lot and get new lot id
			BasicDynaBean lot = getItemLotDetails(oldLotId);
			BigDecimal packageCP = (BigDecimal) lot.get("package_cp");
			int newLotId = storeItemLotDetailsDAO.getNextSequence();
			insertLot(con, newLotId, transactionId+"", itemBatchId, packageCP, "P", "S");

			// insert into stock details a copy based on old lot id
			HashMap<String, Object> stockKeys = new HashMap<String, Object>();
			stockKeys.put("item_lot_id", oldLotId);
			stockKeys.put("dept_id", storeId);
			BasicDynaBean stockBean = storeStockDetailsDAO.findByKey(con, stockKeys);

			stockBean.set("store_stock_id", storeStockDetailsDAO.getNextSequence());
			stockBean.set("item_lot_id", newLotId);
			stockBean.set("item_batch_id", itemBatchId);
			stockBean.set("batch_no", batchNo);
			stockBean.set("qty", transactionQty);
			stockBean.set("username", userName);
			stockBean.set("change_source", changeSource);
			stockBean.set("received_date", DateUtil.getCurrentDate());
			stockBean.set("stock_time", DateUtil.getCurrentTimestamp());
			stockBean.set("item_grn_no", transactionId+"");
			storeStockDetailsDAO.insert(con, stockBean);

			// insert transaction lot (negative of original transaction)
			insertTxnLot(con, transactionId, transactionType, oldLotId, transactionQty.negate());

			// cost is the whole transaction's actual cost, so package_cp needs divide by package size.
			BigDecimal cost = transactionQty.multiply(packageCP);
			cost = ConversionUtils.divideHighPrecision(cost, (BigDecimal)lot.get("issue_base_unit"));
			totalCostValue = totalCostValue.add(cost);
		}

		statusMap.put("status", status);
		statusMap.put("costValue", totalCostValue);

		return statusMap;
	}

	public Map addNewLot(Connection con, int storeId, int transactionId, BasicDynaBean earlierLotDetails,
			BigDecimal qty, String userName, String changeSource, int itemBatchId, String batchNo)
		throws SQLException, IOException {

		Map statusMap = new HashMap();
		boolean status = true;
		BigDecimal totalCostValue = BigDecimal.ZERO;


		BigDecimal transactionQty = qty;
		BigDecimal remainingQty = transactionQty;

		// insert lot and get new lot id
		int newLotId = storeItemLotDetailsDAO.getNextSequence();
		insertLot(con, newLotId, transactionId+"", itemBatchId, (BigDecimal)earlierLotDetails.get("package_cp"), "P", "S");

		// insert into stock details a copy based on old lot id
		HashMap<String, Object> stockKeys = new HashMap<String, Object>();
		stockKeys.put("item_lot_id", (Integer)earlierLotDetails.get("item_lot_id"));
		stockKeys.put("dept_id", storeId);
		BasicDynaBean stockBean = storeStockDetailsDAO.findByKey(con, stockKeys);

		stockBean.set("store_stock_id", storeStockDetailsDAO.getNextSequence());
		stockBean.set("item_lot_id", newLotId);
		stockBean.set("item_batch_id", itemBatchId);
		stockBean.set("batch_no", batchNo);
		stockBean.set("qty", transactionQty);
		stockBean.set("username", userName);
		stockBean.set("change_source", changeSource);
		stockBean.set("received_date", DateUtil.getCurrentDate());
		stockBean.set("stock_time", DateUtil.getCurrentTimestamp());
		storeStockDetailsDAO.insert(con, stockBean);

		BasicDynaBean lot = getItemLotDetails((Integer)earlierLotDetails.get("item_lot_id"));

		// cost is the whole transaction's actual cost, so package_cp needs divide by package size.
		BigDecimal cost = transactionQty.multiply((BigDecimal)earlierLotDetails.get("package_cp"));
		cost = ConversionUtils.divideHighPrecision(cost, (BigDecimal)lot.get("issue_base_unit"));
		totalCostValue = totalCostValue.add(cost);

		statusMap.put("status", status);
		statusMap.put("costValue", totalCostValue);

		return statusMap;
	}

	/**
	 * Can can transafer stock fromStore to toStore on FIFO
	 * @return
	 */
	public Map transferStock(Connection con,int medicineId,int itemBatchId,
			int fromStore,int toStore,BigDecimal qty,String userName,int transactionId,String receiveprocess)
	throws SQLException,IOException{
		boolean status = true;

		BasicDynaBean lotTxnBean = null;
		Map statusMap = new HashMap();

		List<BasicDynaBean> fromStockList = null;
		BigDecimal transactionQty = qty;
		BigDecimal remainingQty = transactionQty;
		BigDecimal totalCostValue = BigDecimal.ZERO;

		Map<String, Object> stockKeys = new HashMap<String, Object>();
		stockKeys.put("dept_id", fromStore);
		stockKeys.put("medicine_id", medicineId);
		stockKeys.put("item_batch_id", itemBatchId);

		fromStockList = storeStockDetailsDAO.listAll(con,null,stockKeys,"item_lot_id");

		for(BasicDynaBean fromStock : fromStockList ){

			if ( (BigDecimal)fromStock.get("qty") == BigDecimal.ZERO || remainingQty == BigDecimal.ZERO )
				continue;
			transactionQty = ( remainingQty.compareTo(BigDecimal.ZERO) > 0 && remainingQty.compareTo((BigDecimal)fromStock.get("qty")) > 0 )
										? (BigDecimal)fromStock.get("qty") : remainingQty;
			remainingQty = remainingQty.subtract(transactionQty);
			Map reduceKeys = new HashMap();
			reduceKeys.put("store_stock_id", fromStock.get("store_stock_id"));

			fromStock.set("qty", ((BigDecimal)fromStock.get("qty")).subtract(transactionQty));
			status &= storeStockDetailsDAO.update(con, fromStock.getMap(),reduceKeys) > 0;//reduce from from store


			//lot details of reducing item
			lotTxnBean = storeTransactionLotDetailsDAO.getBean();
			lotTxnBean.set("transaction_id", transactionId);
			lotTxnBean.set("transaction_type", "T");
			lotTxnBean.set("item_lot_id", fromStock.get("item_lot_id"));
			lotTxnBean.set("qty", transactionQty);

			//record transaction
			status = storeTransactionLotDetailsDAO.insert(con, lotTxnBean);


			fromStock.set("dept_id", toStore);

			if (receiveprocess.equalsIgnoreCase("N")){
				fromStock.set("qty", transactionQty);
			} else{
				fromStock.set("qty", BigDecimal.ZERO);
				fromStock.set("qty_in_transit", transactionQty);
			}
			fromStock.set("change_source","StockTransfer");
			fromStock.set("username", userName);
			fromStock.set("store_stock_id", storeStockDetailsDAO.getNextSequence());

			status &= storeStockDetailsDAO.insert(con, fromStock);//transfer

			BasicDynaBean lot = getItemLotDetails((Integer)fromStock.get("item_lot_id"));
			// cost is the whole transaction's actual cost, so package_cp needs divide by package size.
			BigDecimal cost = transactionQty.multiply((BigDecimal)lot.get("package_cp"));
			cost = ConversionUtils.divideHighPrecision(cost, (BigDecimal)lot.get("issue_base_unit"));
			totalCostValue = totalCostValue.add(cost);
		}

		if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
			statusMap.put("status", false);
			statusMap.put("statusReason", "Insufficient Quantity ");
			statusMap.put("left_qty", remainingQty);
		} else {
			statusMap.put("status", true);
			statusMap.put("costValue", totalCostValue);
		}

		return statusMap;

	}

	private static final String GET_ITEM_LOT_DETAILS =
		"SELECT il.*, sid.issue_base_unit " +
		" FROM store_item_lot_details il " +
		"  JOIN store_item_batch_details ibd USING (item_batch_id) " +
		"  JOIN store_item_details sid USING (medicine_id) " +
		" WHERE item_lot_id=?";

	public BasicDynaBean getItemLotDetails(int itemLotId) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_ITEM_LOT_DETAILS, itemLotId);
	}

	private static final String GET_BATCH_LOT_DETAILS =
		"SELECT il.*, ssd.*,sid.issue_base_unit,ibd.mrp " +
		" FROM store_item_lot_details il " +
		"  JOIN store_stock_details ssd USING(item_lot_id)" +
		"  JOIN store_item_batch_details ibd ON (ssd.item_batch_id = ibd.item_batch_id) " +
		"  JOIN store_item_details sid ON (sid.medicine_id = ssd.medicine_id) " +
		" WHERE ssd.dept_id = ? AND ibd.item_batch_id = ?";

	public List<BasicDynaBean> getBatchLotDetails(int storeId,int itemBatchId) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_BATCH_LOT_DETAILS, new Object[]{storeId,itemBatchId});
	}

	private static final String GET_QTY_SUM_BATCH_LOT_DETAILS =
			"SELECT sum(qty) as avlb_qty,sid.medicine_name " +
			" FROM store_item_lot_details il " +
			"  JOIN store_stock_details ssd USING(item_lot_id)" +
			"  JOIN store_item_batch_details ibd ON (ssd.item_batch_id = ibd.item_batch_id) " +
			"  JOIN store_item_details sid ON (sid.medicine_id = ssd.medicine_id) " +
			" WHERE ssd.dept_id = ? AND ibd.item_batch_id = ? GROUP BY sid.medicine_name";

		public BasicDynaBean getBatchLotQtySum(int storeId,int itemBatchId) throws SQLException {
			return DataBaseUtil.queryToDynaBean(GET_QTY_SUM_BATCH_LOT_DETAILS, new Object[]{storeId,itemBatchId});
		}	
	private static final String GET_BATCH_LOT_DETAILS_MAX_CP =
			"SELECT max(il.package_cp) as max_package_cp " +
			" FROM store_item_lot_details il " +
			"  JOIN store_stock_details ssd USING(item_lot_id)" +
			"  JOIN store_item_batch_details ibd ON (ssd.item_batch_id = ibd.item_batch_id) " +
			"  JOIN store_item_details sid ON (sid.medicine_id = ssd.medicine_id) " +
			" WHERE ssd.dept_id = ? AND ibd.item_batch_id = ?";
	
	public BasicDynaBean getBatchSortedLotDetailsMaxCP(int storeId,int itemBatchId) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_BATCH_LOT_DETAILS_MAX_CP, new Object[]{storeId,itemBatchId});
	}
	
	public List<BasicDynaBean> getBatchSortedLotDetails(int storeId,int itemBatchId) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_BATCH_LOT_DETAILS+ " ORDER BY il.item_lot_id", new Object[]{storeId,itemBatchId});
	}
	
	public List<BasicDynaBean> getBatchLotDetails(Connection con,int storeId,int itemBatchId,String purchaseType) throws SQLException {
		return DataBaseUtil.queryToDynaList(con,GET_BATCH_LOT_DETAILS+ " AND purchase_type = ? ORDER BY il.item_lot_id ", new Object[]{storeId,itemBatchId,purchaseType});
	}
	public List<BasicDynaBean> getBatchLotDetails(Connection con,int storeId,int itemBatchId,String purchaseType, String grnNo) throws SQLException {
		return DataBaseUtil.queryToDynaList(con,GET_BATCH_LOT_DETAILS+ " AND purchase_type = ? AND grn_no = ? ORDER BY il.item_lot_id ", new Object[]{storeId,itemBatchId,purchaseType,grnNo});
	}

	private static final String STOCK_REDUCED_LOTS =
		" SELECT transaction_id, transaction_type, sum(qty) as qty, item_lot_id" +
		" FROM store_transaction_lot_details " +
		" WHERE transaction_id=? AND transaction_type=? " +
		" GROUP BY transaction_id, transaction_type, item_lot_id" +
		" ORDER BY item_lot_id DESC ";

	public List<BasicDynaBean> getUsedLots(Connection con, int txnId, String txnType)
		throws SQLException {
		return DataBaseUtil.queryToDynaList(con, STOCK_REDUCED_LOTS, new Object[]{txnId, txnType});
	}

	public boolean insertLot(Connection con, int lotId, String grnNo, int itemBatchId, BigDecimal packageCp,
			String lotSource, String purchaseType)
	throws SQLException,IOException{


		BasicDynaBean itemLotBean = storeItemLotDetailsDAO.getBean();
		itemLotBean.set("package_cp", packageCp);
		itemLotBean.set("grn_no", grnNo);
		itemLotBean.set("item_batch_id", itemBatchId);
		itemLotBean.set("item_lot_id", lotId);
		itemLotBean.set("lot_source", lotSource);
		itemLotBean.set("purchase_type", purchaseType);
		return storeItemLotDetailsDAO.insert(con, itemLotBean);
	}

	private static final String INSERT_TXN_LOT =
		" INSERT INTO store_transaction_lot_details (transaction_id, transaction_type, item_lot_id, qty) " +
		" VALUES (?, ?, ?, ?)";

	public static int insertTxnLot(Connection con, int txnId, String txnType, int itemLotId, BigDecimal qty)
		throws SQLException {
		return DataBaseUtil.executeQuery(con, INSERT_TXN_LOT, new Object[]{
			txnId, txnType, itemLotId, qty});
	}


	private static boolean transferStock(Connection con,int storeId,BigDecimal qty,String chnageSource,String userName,BasicDynaBean existStock, boolean toTransit)
	throws SQLException,IOException {


		existStock.set("dept_id", storeId);
		if (toTransit) {
			existStock.set("qty_in_transit", qty);
			existStock.set("qty", BigDecimal.ZERO);
		} else {
			existStock.set("qty_in_transit", BigDecimal.ZERO);
		existStock.set("qty", qty);
		}
		existStock.set("change_source","StockTransfer");
		existStock.set("username", userName);
		existStock.set("store_stock_id", storeStockDetailsDAO.getNextSequence());

		return storeStockDetailsDAO.insert(con, existStock);

	}

	private static boolean transferStock(Connection con,int storeId,BigDecimal qty,String chnageSource,String userName,BasicDynaBean existStock)
	throws SQLException,IOException {

		return transferStock(con, storeId, qty, chnageSource, userName, existStock, false);
	}

	private static final String ADD_QTY_TO_STOCK_DETAILS =
		" UPDATE store_stock_details SET qty=qty+?, qty_in_use=qty_in_use-?, qty_in_transit=qty_in_transit+?, " +
		"  username=?, change_source=? " +
		" WHERE store_stock_id=? ";

	private static int addQtyToStockDetails(Connection con, int stockId, BigDecimal qtyToAdd,
			String userName, String changeSource, boolean transitOnly) throws SQLException {
		if (transitOnly) {
		return DataBaseUtil.executeQuery(con, ADD_QTY_TO_STOCK_DETAILS , new Object[]{
					BigDecimal.ZERO, qtyToAdd, qtyToAdd, userName, changeSource, stockId});
		} else {
			return DataBaseUtil.executeQuery(con, ADD_QTY_TO_STOCK_DETAILS , new Object[]{
					qtyToAdd, qtyToAdd, BigDecimal.ZERO, userName, changeSource, stockId});
	}
	}

	private static int addQtyToStockDetails(Connection con, int stockId, BigDecimal qtyToAdd,
			String userName, String changeSource) throws SQLException {
		return addQtyToStockDetails(con, stockId, qtyToAdd, userName, changeSource, false);
	}

	private static final String REDUCE_TRANSIT_STOCK =
		" UPDATE store_stock_details SET qty_in_transit=qty_in_transit-? " +
		" WHERE store_stock_id=? AND qty_in_transit > 0 ";

	private static int reduceTransitStock(Connection con, int stockId, BigDecimal qty) throws SQLException {
		return DataBaseUtil.executeQuery(con, REDUCE_TRANSIT_STOCK , new Object[]{
				qty, stockId});

	}
	
	private static final String ADD_QTY_REJECTION_TO_STOCK_DETAILS =
		" UPDATE store_stock_details SET qty_in_rejection = qty_in_rejection + ? WHERE store_stock_id=? ";

	private static int addQtyRejectionToStock(Connection con, int stockId, BigDecimal qty) throws SQLException {
		return DataBaseUtil.executeQuery(con, ADD_QTY_REJECTION_TO_STOCK_DETAILS , new Object[]{qty, stockId});
	}


	/*
	 * Add/reduce stock by lot: used by stock adjustment
	 */
	public Map addStockByLot(Connection con,int storeId,int itemLotId,String transactionType
			,BigDecimal qty,String userName,String changeSource,int transactionId,String remarks)
	throws SQLException,IOException{

		Map statusMap = new HashMap();
		boolean status = true;
		BigDecimal totalCostValue = BigDecimal.ZERO;

		String[] columns = {"qty", "remarks_avbl", "username", "change_source"};

		String[] keys = {"item_lot_id","dept_id"};


		HashMap<String, Object> stockKeys = new HashMap<String, Object>();
		stockKeys.put("item_lot_id", itemLotId);
		stockKeys.put("dept_id",storeId);
		//this is actual stock of that item batch,store,lot
		BasicDynaBean stockBean = storeStockDetailsDAO.findByKey(con,new ArrayList<String>(),stockKeys);

		stockBean.set("qty", ((BigDecimal)stockBean.get("qty")).add(qty));
		stockBean.set("username", userName);
		stockBean.set("change_source",changeSource);
		stockBean.set("item_lot_id", itemLotId);
		stockBean.set("remarks_avbl", remarks);

		status = storeStockDetailsDAO.updateWithNames(con, columns, stockBean.getMap(), keys) > 0;

		BasicDynaBean lotTxnBean = null;
		lotTxnBean = storeTransactionLotDetailsDAO.getBean();
		lotTxnBean.set("transaction_id", transactionId);
		lotTxnBean.set("transaction_type", transactionType);
		lotTxnBean.set("item_lot_id", itemLotId);
		lotTxnBean.set("qty", qty.negate());

		//record transaction
		status = storeTransactionLotDetailsDAO.insert(con, lotTxnBean);

		BasicDynaBean lot = getItemLotDetails(itemLotId);
		// cost is the whole transaction's actual cost, so package_cp needs divide by package size.
		BigDecimal cost = qty.multiply((BigDecimal)lot.get("package_cp"));
		cost = ConversionUtils.divideHighPrecision(cost, (BigDecimal)lot.get("issue_base_unit"));
		totalCostValue = totalCostValue.add(cost);

		statusMap.put("status", status);
		statusMap.put("costValue", totalCostValue.negate());

		return statusMap;
	}

	public Map reduceStockByLot(Connection con,int storeid,int itemLotId,String transactionType,
			BigDecimal qty,String username,String changeSource,int transactionId,String remarks)
	throws SQLException,IOException{

		BasicDynaBean lotTxnBean = null;

		Map<String, Object> lotKeys = new HashMap<String, Object>();
		lotKeys.put("dept_id", storeid);
		lotKeys.put("item_lot_id", itemLotId);
		BasicDynaBean stockBean = storeStockDetailsDAO.findByKey(con,new ArrayList<String>(),lotKeys);

		boolean status = true;
		BigDecimal totalCostValue = BigDecimal.ZERO;
		Map statusMap = new HashMap();
		BigDecimal txnQty = ((BigDecimal)stockBean.get("qty")).subtract(qty);
		
		
		if (  txnQty.compareTo(BigDecimal.ZERO) < 0  ){
			statusMap.put("status", false);
			statusMap.put("costValue", BigDecimal.ZERO);
			statusMap.put("statusReason", "Insufficient Quantity ");
			
			return statusMap;
		}

		stockBean.set("qty", txnQty );
		stockBean.set("username", username);
		stockBean.set("change_source", changeSource);
		stockBean.set("remarks_avbl", remarks);

		//reduce stock
		status = storeStockDetailsDAO.update(con, stockBean.getMap(), "store_stock_id",(Integer)stockBean.get("store_stock_id")) > 0;

		lotTxnBean = storeTransactionLotDetailsDAO.getBean();
		lotTxnBean.set("transaction_id", transactionId);
		lotTxnBean.set("transaction_type", transactionType);
		lotTxnBean.set("item_lot_id", stockBean.get("item_lot_id"));
		lotTxnBean.set("qty", qty);

		//record transaction
		status = storeTransactionLotDetailsDAO.insert(con, lotTxnBean);

		BasicDynaBean lot = getItemLotDetails((Integer)stockBean.get("item_lot_id"));
		// cost is the whole transaction's actual cost, so package_cp needs divide by package size.
		BigDecimal cost = qty.multiply((BigDecimal)lot.get("package_cp"));
		cost = ConversionUtils.divideHighPrecision(cost, (BigDecimal)lot.get("issue_base_unit"));
		totalCostValue = totalCostValue.add(cost);

		statusMap.put("status", status);
		statusMap.put("costValue", totalCostValue);

		return statusMap;
	}

	public Map receiveTransactionLots(Connection con, int storeId, int transactionId, String transactionType,
			BigDecimal qty, String userName, String changeSource, Integer refStore) throws SQLException,IOException{


		HashMap<String, Object> transLotKeys = new HashMap<String, Object>();
		transLotKeys.put("transaction_id", transactionId);
		transLotKeys.put("transaction_type", transactionType);

		List<BasicDynaBean> transactionLotBeans = storeTransactionLotDetailsDAO.listAll(null, transLotKeys, null);
		BigDecimal transactionQty = qty;
		BigDecimal remainingQty = transactionQty;
		BigDecimal totalCostValue = BigDecimal.ZERO;
		boolean status = true;
		Map statusMap = new HashMap();

		for( BasicDynaBean lot : transactionLotBeans ){
			int itemLotId = (Integer)lot.get("item_lot_id");
			BigDecimal lotQty = (BigDecimal)lot.get("qty");

			if (remainingQty.compareTo(BigDecimal.ZERO) == 0)
				break;

			if (lotQty.compareTo(BigDecimal.ZERO) == 0 || lotQty.compareTo(BigDecimal.ZERO) < 0)
				continue;

			transactionQty = (remainingQty.compareTo(lotQty) > 0 ) ? lotQty : remainingQty;

			remainingQty = remainingQty.subtract(transactionQty);

			HashMap<String, Object> stockKeys = new HashMap<String, Object>();
			stockKeys.put("item_lot_id", itemLotId);
			stockKeys.put("dept_id", storeId);
			BasicDynaBean stockBean = storeStockDetailsDAO.findByKey(con, stockKeys);

			/** possible in case transfer is happening
			 *  and same batch exissts in the to store but
			 *  not this lot.Need to insert it from ref store lot.
			 */
			if (stockBean == null && refStore != null) {
				//get stock from reference store.
				stockKeys = new HashMap<String, Object>();
				stockKeys.put("item_lot_id", itemLotId);
				stockKeys.put("dept_id", refStore);
				stockBean = storeStockDetailsDAO.findByKey(con, stockKeys);

				transferStock(con, storeId, transactionQty, "StockTransfer", userName, stockBean);

			} else if (stockBean != null) {
				// update the stock details: add stock.
				addQtyToStockDetails(con, (Integer)stockBean.get("store_stock_id"),
						transactionQty, userName, changeSource);
				reduceTransitStock(con, (Integer)stockBean.get("store_stock_id"), transactionQty);
			}

			BasicDynaBean lotBean = getItemLotDetails(itemLotId);
			// cost is the whole transaction's actual cost, so package_cp needs divide by package size.
			BigDecimal cost = transactionQty.multiply((BigDecimal)lotBean.get("package_cp"));
			cost = ConversionUtils.divideHighPrecision(cost, (BigDecimal)lotBean.get("issue_base_unit"));
			totalCostValue = totalCostValue.add(cost);
		}

		if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
			statusMap.put("status", false);
			statusMap.put("statusReason", "Insufficient Quantity ");
			statusMap.put("left_qty", remainingQty);
		} else {
			statusMap.put("status", true);
			statusMap.put("costValue", totalCostValue);
		}

		return statusMap;

	}

	public Map rejectTransactionLots(Connection con, int indentStoreId, int transactionId, String transactionType,
			BigDecimal qty, String userName, String changeSource, Integer recvStore) throws SQLException,IOException{


		HashMap<String, Object> transLotKeys = new HashMap<String, Object>();
		transLotKeys.put("transaction_id", transactionId);
		transLotKeys.put("transaction_type", transactionType);

		List<BasicDynaBean> transactionLotBeans = storeTransactionLotDetailsDAO.listAll(null, transLotKeys, null);
		BigDecimal transactionQty = qty;
		BigDecimal remainingQty = transactionQty;
		BigDecimal totalCostValue = BigDecimal.ZERO;
		boolean status = true;
		Map statusMap = new HashMap();

		for( BasicDynaBean lot : transactionLotBeans ){
			int itemLotId = (Integer)lot.get("item_lot_id");
			BigDecimal lotQty = (BigDecimal)lot.get("qty");

			if (remainingQty.compareTo(BigDecimal.ZERO) == 0)
				break;

			if (lotQty.compareTo(BigDecimal.ZERO) == 0 || lotQty.compareTo(BigDecimal.ZERO) < 0)
				continue;

			HashMap<String, Object> stockKeys = new HashMap<String, Object>();
			stockKeys.put("item_lot_id", itemLotId);
			stockKeys.put("dept_id", indentStoreId);
			BasicDynaBean iStockBean = storeStockDetailsDAO.findByKey(con, stockKeys);

			stockKeys = new HashMap<String, Object>();
			stockKeys.put("item_lot_id", itemLotId);
			stockKeys.put("dept_id", recvStore);
			BasicDynaBean rStockBean = storeStockDetailsDAO.findByKey(con, stockKeys);
			BigDecimal transitQty = (null != rStockBean.get("qty_in_transit")) ? (BigDecimal)rStockBean.get("qty_in_transit") : BigDecimal.ZERO;

			transactionQty = (remainingQty.compareTo(lotQty) > 0 ) ? (lotQty.compareTo(transitQty) > 0 ? transitQty : lotQty) :
				(remainingQty.compareTo(transitQty) > 0 ? transitQty : remainingQty);

			if (transactionQty.compareTo(BigDecimal.ZERO) <= 0) continue;

			remainingQty = remainingQty.subtract(transactionQty);

			/** possible in case transfer is happening
			 *  and same batch exists in the to store but
			 *  not this lot.Need to insert it from ref store lot.
			 */
			if (iStockBean == null && recvStore != null) {
				//get stock from reference store.
				transferStock(con, indentStoreId, new BigDecimal(0), "StockTransfer", userName, rStockBean);

			} else if (iStockBean != null) {
				// update the stock details: add stock.
				addQtyToStockDetails(con, (Integer)iStockBean.get("store_stock_id"),
						new BigDecimal(0), userName, changeSource);

			}

			// reduce the transit stock from the recv store
			reduceTransitStock(con, (Integer)rStockBean.get("store_stock_id"), transactionQty);
			if (iStockBean != null) {
  			// add rejected qty to store stock details table.
  			addQtyRejectionToStock(con, (Integer)iStockBean.get("store_stock_id"), transactionQty);
  		}
			
			insertTxnLot(con, transactionId, "RJ", itemLotId, transactionQty);

			BasicDynaBean lotBean = getItemLotDetails(itemLotId);
			// cost is the whole transaction's actual cost, so package_cp needs divide by package size.
			BigDecimal cost = transactionQty.multiply((BigDecimal)lotBean.get("package_cp"));
			cost = ConversionUtils.divideHighPrecision(cost, (BigDecimal)lotBean.get("issue_base_unit"));
			totalCostValue = totalCostValue.add(cost);
		}

		if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
			statusMap.put("status", false);
			statusMap.put("statusReason", "Insufficient Quantity ");
			statusMap.put("left_qty", remainingQty);
		} else {
			statusMap.put("status", true);
			statusMap.put("costValue", totalCostValue);
		}

		return statusMap;

	}

	public Map receiveLots(Connection con, int storeId, int transactionId, String transactionType,
			BigDecimal qty, String userName, String changeSource, Integer refStore,int itemBatchId) throws SQLException,IOException{


		HashMap<String, Object> stockLotKeys = new HashMap<String, Object>();
		stockLotKeys.put("item_batch_id", itemBatchId);
		stockLotKeys.put("dept_id", storeId);

		List<BasicDynaBean> transactionLotBeans = storeStockDetailsDAO.listAll(con,null, stockLotKeys, null);
		BigDecimal transactionQty = qty;
		BigDecimal remainingQty = transactionQty;
		BigDecimal totalCostValue = BigDecimal.ZERO;
		boolean status = true;
		Map statusMap = new HashMap();

		for( BasicDynaBean lot : transactionLotBeans ){
			int itemLotId = (Integer)lot.get("item_lot_id");
			BigDecimal lotTransitQty = (BigDecimal)lot.get("qty_in_transit");

			if (remainingQty.compareTo(BigDecimal.ZERO) == 0)
				break;

			if (lotTransitQty.compareTo(BigDecimal.ZERO) == 0 || lotTransitQty.compareTo(BigDecimal.ZERO) < 0)
				continue;

			transactionQty = (remainingQty.compareTo(lotTransitQty) > 0 ) ? lotTransitQty : remainingQty;

			remainingQty = remainingQty.subtract(transactionQty);

			HashMap<String, Object> stockKeys = new HashMap<String, Object>();
			stockKeys.put("item_lot_id", itemLotId);
			stockKeys.put("dept_id", storeId);
			BasicDynaBean stockBean = storeStockDetailsDAO.findByKey(con, stockKeys);

			/** possible in case transfer is happening
			 *  and same batch exissts in the to store but
			 *  not this lot.Need to insert it from ref store lot.
			 */
			if (stockBean == null && refStore != null) {
				//get stock from reference store.
				stockKeys = new HashMap<String, Object>();
				stockKeys.put("item_lot_id", itemLotId);
				stockKeys.put("dept_id", refStore);
				stockBean = storeStockDetailsDAO.findByKey(con, stockKeys);

				transferStock(con, storeId, transactionQty, "StockTransfer", userName, stockBean);

			} else if (stockBean != null) {
				// update the stock details: add stock.
				addQtyToStockDetails(con, (Integer)stockBean.get("store_stock_id"),
						transactionQty, userName, changeSource);
			}

			BasicDynaBean lotBean = getItemLotDetails(itemLotId);
			// cost is the whole transaction's actual cost, so package_cp needs divide by package size.
			BigDecimal cost = transactionQty.multiply((BigDecimal)lotBean.get("package_cp"));
			cost = ConversionUtils.divideHighPrecision(cost, (BigDecimal)lotBean.get("issue_base_unit"));
			totalCostValue = totalCostValue.add(cost);
		}

		if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
			statusMap.put("status", false);
			statusMap.put("statusReason", "Insufficient Quantity ");
			statusMap.put("left_qty", remainingQty);
		} else {
			statusMap.put("status", true);
			statusMap.put("costValue", totalCostValue);
		}

		return statusMap;

	}


	public boolean addToEarlierStock(Connection con,int itemBatchId,int storeId,BigDecimal qty)
	throws SQLException,IOException{
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement("SELECT * FROM store_stock_details WHERE item_batch_id = ? AND dept_id = ? ORDER BY stock_time");
			ps.setInt(1, itemBatchId);
			ps.setInt(2, storeId);
			BasicDynaBean earlierStock = DataBaseUtil.queryToDynaBean(ps);
			earlierStock.set("qty", ((BigDecimal)earlierStock.get("qty")).add(qty));

			Map<String,Object> stockKeys = new HashMap<String,Object>();
			stockKeys.put("dept_id", storeId);
			stockKeys.put("store_stock_id", (Integer)earlierStock.get("store_stock_id"));

			return (storeStockDetailsDAO.update(con, earlierStock.getMap(),stockKeys) > 0);
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public BasicDynaBean getLotDetails(Connection con,int itemBatchId)
	throws SQLException,IOException{
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement("SELECT * FROM store_item_lot_details WHERE item_batch_id = ? ORDER BY lot_time");
			ps.setInt(1, itemBatchId);

			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public boolean updateStockTimeStamp() throws SQLException{
        Connection con = null;
        Statement s = null;
        try{
            con = DataBaseUtil.getConnection();
            s = con.createStatement();
            return (s.executeUpdate("UPDATE store_main_stock_timestamp SET medicine_timestamp = medicine_timestamp+1") > 0);
        }finally{
        	DataBaseUtil.closeConnections(con, s);
        }
    }

    public boolean updateStoresStockTimeStamp(int storeId) throws SQLException{
        Connection con = null;
        PreparedStatement ps = null;
        try{
            con = DataBaseUtil.getConnection();
            ps = con.prepareStatement("UPDATE stores SET stock_timestamp = stock_timestamp+1 WHERE dept_id = ?");
            ps.setInt(1, storeId);
            return (ps.executeUpdate() > 0);
        }finally{
        	DataBaseUtil.closeConnections(con, ps);
        }
    }

}
