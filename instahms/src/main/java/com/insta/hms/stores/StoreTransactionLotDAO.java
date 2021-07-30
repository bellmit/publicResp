package com.insta.hms.stores;

import com.insta.hms.common.GenericDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreTransactionLotDAO extends GenericDAO{

	static Logger logger = LoggerFactory.getLogger(StockFIFODAO.class);

	public StoreTransactionLotDAO() {
		super("store_transaction_lot_details");
	}

	public static final String STOCK_TRANSFER = "T";
	//detail unique id of respective transaction sales,issues,transfer etc.,
	public static final String FILTER_COLUMN_1 = "transaction_id";
	public static final String FILTER_COLUMN_2 = "transaction_type";



}