package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class StockLedgerDAO {

	static Logger logger = LoggerFactory.getLogger(StockLedgerDAO.class);
	
    private static final GenericDAO storeItemDetailsDAO = new GenericDAO("store_item_details");

	private static final String QUERY_FEILDS =
		"SELECT store_id, medicine_id, batch_no, identification, txn_date, txn_ref, txn_type, " +
		" details,user_name, bonus_qty, qty, "+
		" total_qty, mrp, l.package_uom, l.issue_base_unit, m.issue_units ";

	private static final String QUERY_COUNT = "SELECT COUNT(medicine_id)";

	private static final String QUERY_TABLES =
		" FROM stores_stock_ledger l " +
		"  JOIN store_item_details m using (medicine_id) "+
		"  JOIN store_category_master c on m.med_category_id = c.category_id ";

	public static PagedList getPurchases( Map filter, Map listing )
		throws SQLException, ParseException {

		Connection con = DataBaseUtil.getReadOnlyConnection();

		String sortField = (String)listing.get(LISTING.SORTCOL);
		boolean sortReverse = (Boolean)listing.get(LISTING.SORTASC);
		int pageSize = (Integer)listing.get(LISTING.PAGESIZE);
		int pageNum = (Integer)listing.get(LISTING.PAGENUM);

		SearchQueryBuilder qb = new SearchQueryBuilder(con,
				QUERY_FEILDS, QUERY_COUNT, QUERY_TABLES,null,
				sortField, sortReverse, pageSize, pageNum);

		BasicDynaBean bean = storeItemDetailsDAO.findByKey("medicine_name", filter.get("medicineName"));
		int medicineId = -1;
		if (bean != null ) medicineId = ((Integer)bean.get("medicine_id")).intValue();
		qb.addFilter(qb.INTEGER, "medicine_id", "=", medicineId);
		qb.addFilter(qb.INTEGER, "store_id", "=", Integer.parseInt((String)filter.get("store_id")));
		qb.addFilter(qb.DATE,   "date_trunc('day', txn_date)", ">=", filter.get("fromDate"));
		qb.addFilter(qb.DATE,   "date_trunc('day', txn_date)", "<=", filter.get("toDate"));
		qb.addFilter(qb.TIMESTAMP, "txn_date", ">=", filter.get("fromChkDate"));
		qb.addFilter(qb.TIMESTAMP, "txn_date", "<=", filter.get("toChkDate"));
		qb.addFilter(qb.STRING, "batch_no", "=", filter.get("batchno"));

		qb.addSecondarySort("txn_date");
		qb.build();
		PagedList l = qb.getMappedPagedList();
		qb.close();
		con.close();
		return l;
	}


	private static final String CURRENT_CLOSING_STOCK_QYERY = "SELECT m.medicine_name, sibd.batch_no, " +
 			"qpmsd.qty, sibd.mrp, pmsd.package_uom, pmsd.stock_pkg_size, m.issue_units,m.cust_item_code " +
 		"FROM  store_stock_details pmsd " +
 		" JOIN ( SELECT sum(qty) as qty,item_batch_id " +
 		"			FROM store_stock_details  " +
 		"				WHERE dept_id = ? " +
 		"		 GROUP BY item_batch_id )  qpmsd USING(item_batch_id)" +
 		"JOIN store_item_batch_details sibd USING(item_batch_id)" +
 		"JOIN store_item_details m ON (m.medicine_id = pmsd.medicine_id) " +
 		"WHERE pmsd.medicine_id = ? and dept_id = ?";


	private static final String  OPENING_STOCK_QUERY = "SELECT m.medicine_name,sibd.batch_no, qpcd.qty, " +
			" sibd.mrp, m.issue_units, pmsd.package_uom, pmsd.stock_pkg_size,m.cust_item_code " +
			" FROM store_checkpoint_main pcm " +
			" JOIN store_checkpoint_details pcd using(checkpoint_id) " +
			" JOIN ( SELECT sum(qty) as qty,batch_no,medicine_id,checkpoint_id FROM store_checkpoint_details " +
			"			WHERE store_id = ? GROUP BY medicine_id,batch_no,checkpoint_id ) as qpcd " +
			"		ON( qpcd.medicine_id = pcd.medicine_id AND pcd.batch_no =  qpcd.batch_no AND pcd.checkpoint_id = qpcd.checkpoint_id) " +
			" JOIN store_item_details m ON(m.medicine_id = pcd.medicine_id) " +
			" JOIN store_stock_details pmsd ON (pmsd.medicine_id = pcd.medicine_id AND pmsd.batch_no = pcd.batch_no)" +
			" JOIN ( SELECT sum(qty) as qty,item_batch_id FROM store_stock_details  WHERE dept_id = ? GROUP BY item_batch_id ) as qpmsd " +
			"	USING (item_batch_id)" +
			" JOIN store_item_batch_details sibd USING(item_batch_id)" +
			" WHERE  pcd.checkpoint_id = ? and pcd.medicine_id = ? and store_id = ?";


	private static final String  CHECK_POINT_CLOSING_STOCK_QUERY = "SELECT m.medicine_name,sibd.batch_no,qpmsd.qty, " +
			"sibd.mrp, pmsd.package_uom, pmsd.stock_pkg_size, m.issue_units,m.cust_item_code " +
			"FROM store_checkpoint_main pcm JOIN store_checkpoint_details pcd using(checkpoint_id) " +
			"JOIN store_item_details m using(medicine_id) " +
			"JOIN store_stock_details pmsd ON (pmsd.medicine_id = pcd.medicine_id AND pmsd.batch_no = pcd.batch_no)" +
			"JOIN ( SELECT sum(qty) as qty,item_batch_id FROM store_stock_details  WHERE dept_id = ? GROUP BY item_batch_id ) qpmsd " +
			"	USING(item_batch_id)" +
			"JOIN store_item_batch_details sibd USING(item_batch_id)" +
			"WHERE  pcd.checkpoint_id = ? and pcd.medicine_id = ? and store_id = ? ";
	
	private static final String CURRENT_TRANSIT_STOCK_QYERY = "SELECT m.medicine_name, sibd.batch_no, " +
 			"qpmsd.transit_qty, sibd.mrp, pmsd.package_uom, pmsd.stock_pkg_size, m.issue_units,m.cust_item_code " +
 		"FROM  store_stock_details pmsd " +
 		" JOIN ( SELECT sum(qty_in_transit) as transit_qty,item_batch_id " +
 		"			FROM store_stock_details  " +
 		"				WHERE dept_id = ? " +
 		"		 GROUP BY item_batch_id )  qpmsd USING(item_batch_id)" +
 		"JOIN store_item_batch_details sibd USING(item_batch_id)" +
 		"JOIN store_item_details m ON (m.medicine_id = pmsd.medicine_id) " +
 		"WHERE pmsd.medicine_id = ? and dept_id = ?";

	private static final String  CHECK_POINT_TRANSIT_STOCK_QUERY = "SELECT m.medicine_name,sibd.batch_no,qpmsd.transit_qty, " +
			"sibd.mrp, pmsd.package_uom, pmsd.stock_pkg_size, m.issue_units,m.cust_item_code " +
			"FROM store_checkpoint_main pcm JOIN store_checkpoint_details pcd using(checkpoint_id) " +
			"JOIN store_item_details m using(medicine_id) " +
			"JOIN store_stock_details pmsd ON (pmsd.medicine_id = pcd.medicine_id AND pmsd.batch_no = pcd.batch_no)" +
			"JOIN ( SELECT sum(qty_in_transit) as transit_qty,item_batch_id FROM store_stock_details  WHERE dept_id = ? GROUP BY item_batch_id ) qpmsd " +
			"	USING(item_batch_id)" +
			"JOIN store_item_batch_details sibd USING(item_batch_id)" +
			"WHERE  pcd.checkpoint_id = ? and pcd.medicine_id = ? and store_id = ? ";
	
	private static final String CURRENT_REJECTION_STOCK_QYERY = "SELECT m.medicine_name, sibd.batch_no, " +
 			"qpmsd.qty_in_rejection, sibd.mrp, pmsd.package_uom, pmsd.stock_pkg_size, m.issue_units,m.cust_item_code " +
 		"FROM  store_stock_details pmsd " +
 		" JOIN ( SELECT sum(qty_in_rejection) as qty_in_rejection,item_batch_id " +
 		"			FROM store_stock_details  " +
 		"				WHERE dept_id = ? " +
 		"		 GROUP BY item_batch_id )  qpmsd USING(item_batch_id)" +
 		"JOIN store_item_batch_details sibd USING(item_batch_id)" +
 		"JOIN store_item_details m ON (m.medicine_id = pmsd.medicine_id) " +
 		"WHERE pmsd.medicine_id = ? and dept_id = ?";

	private static final String  CHECK_POINT_REJECTION_STOCK_QUERY = "SELECT m.medicine_name,sibd.batch_no,qpmsd.qty_in_rejection, " +
			"sibd.mrp, pmsd.package_uom, pmsd.stock_pkg_size, m.issue_units,m.cust_item_code " +
			"FROM store_checkpoint_main pcm JOIN store_checkpoint_details pcd using(checkpoint_id) " +
			"JOIN store_item_details m using(medicine_id) " +
			"JOIN store_stock_details pmsd ON (pmsd.medicine_id = pcd.medicine_id AND pmsd.batch_no = pcd.batch_no)" +
			"JOIN ( SELECT sum(qty_in_rejection) as qty_in_rejection,item_batch_id FROM store_stock_details  WHERE dept_id = ? GROUP BY item_batch_id ) qpmsd " +
			"	USING(item_batch_id)" +
			"JOIN store_item_batch_details sibd USING(item_batch_id)" +
			"WHERE  pcd.checkpoint_id = ? and pcd.medicine_id = ? and store_id = ? ";


	public static List getOpenAndClose( Map filter, String openOrclose ) throws SQLException, ParseException {

		List l = null;

		try (Connection con = DataBaseUtil.getReadOnlyConnection()) {
          BasicDynaBean bean =
              storeItemDetailsDAO.findByKey("medicine_name", filter.get("medicineName"));
			int medicineId = -1;
			String psQuey = "";
			if (bean != null) {
				medicineId = ((Integer) bean.get("medicine_id")).intValue();
			}
			if (openOrclose.equals("O")) {
				psQuey = OPENING_STOCK_QUERY
						+ " GROUP BY store_id,medicine_name,sibd.batch_no,m.issue_base_unit, sibd"
						+ ".mrp, "
						+
						"m.issue_units, pmsd.package_uom, pmsd.stock_pkg_size,qpcd.qty,m"
						+ ".cust_item_code";
				if (!filter.get("batchno").equals("")) {
					psQuey = OPENING_STOCK_QUERY + "AND pcd.batch_no = ? "
							+ "GROUP BY store_id,medicine_name,sibd.batch_no,m.issue_base_unit, "
							+ "sibd.mrp, "
							+
							"m.issue_units, pmsd.package_uom, pmsd.stock_pkg_size,qpcd.qty,m"
							+ ".cust_item_code";
				}
				try (PreparedStatement ps = con.prepareStatement(psQuey)) {
					ps.setInt(1, Integer.parseInt((String) filter.get("store_id")));
					ps.setInt(2, Integer.parseInt((String) filter.get("store_id")));
					ps.setInt(3, Integer.parseInt((String) filter.get("fromCheckPt")));
					ps.setInt(4, medicineId);
					ps.setInt(5, Integer.parseInt((String) filter.get("store_id")));
					if (!filter.get("batchno").equals("")) {
						ps.setString(6, (String) filter.get("batchno"));
					}
					l = DataBaseUtil.queryToDynaList(ps);
				}
			} else if (openOrclose.equals("C")) {
				psQuey = CHECK_POINT_CLOSING_STOCK_QUERY
						+ "GROUP BY store_id,medicine_name,sibd.batch_no, m.issue_base_unit, " +
						"sibd.mrp, pmsd.package_uom, pmsd.stock_pkg_size, m.issue_units,qpmsd.qty,"
						+ "m.cust_item_code";
				if (!filter.get("batchno").equals("")) {
					psQuey = CHECK_POINT_CLOSING_STOCK_QUERY + " AND pcd.batch_no = ?"
							+ "GROUP BY store_id,medicine_name,sibd.batch_no, " +
							"m.issue_base_unit, sibd.mrp, pmsd.package_uom, pmsd.stock_pkg_size, m"
							+ ".issue_units,qpmsd.qty,m.cust_item_code";
				}
				try (PreparedStatement ps = con.prepareStatement(psQuey)) {
					ps.setInt(1, Integer.parseInt((String) filter.get("store_id")));
					ps.setInt(2, Integer.parseInt((String) filter.get("toCheckPt")));
					ps.setInt(3, medicineId);
					ps.setInt(4, Integer.parseInt((String) filter.get("store_id")));
					if (!filter.get("batchno").equals("")) {
						ps.setString(5, (String) filter.get("batchno"));
					}
					l = DataBaseUtil.queryToDynaList(ps);
				}
			} else if (openOrclose.equals("T")) {
				psQuey = CHECK_POINT_TRANSIT_STOCK_QUERY
						+ "GROUP BY store_id,medicine_name,sibd.batch_no, m.issue_base_unit, " +
						"sibd.mrp, pmsd.package_uom, pmsd.stock_pkg_size, m.issue_units,qpmsd"
						+ ".transit_qty,m.cust_item_code";
				if (!filter.get("batchno").equals("")) {
					psQuey = CHECK_POINT_TRANSIT_STOCK_QUERY + " AND pcd.batch_no = ?"
							+ "GROUP BY store_id,medicine_name,sibd.batch_no, " +
							"m.issue_base_unit, sibd.mrp, pmsd.package_uom, pmsd.stock_pkg_size, m"
							+ ".issue_units,qpmsd.transit_qty,m.cust_item_code";
				}
				try (PreparedStatement ps = con.prepareStatement(psQuey)) {
					ps.setInt(1, Integer.parseInt((String) filter.get("store_id")));
					ps.setInt(2, Integer.parseInt((String) filter.get("toCheckPt")));
					ps.setInt(3, medicineId);
					ps.setInt(4, Integer.parseInt((String) filter.get("store_id")));
					if (!filter.get("batchno").equals("")) {
						ps.setString(5, (String) filter.get("batchno"));
					}
					l = DataBaseUtil.queryToDynaList(ps);
				}
			} else if (openOrclose.equals("TM")) {
				psQuey = CURRENT_TRANSIT_STOCK_QYERY
						+ "GROUP BY dept_id,medicine_name,sibd.batch_no,m.issue_base_unit, " +
						"sibd.mrp, pmsd.package_uom, pmsd.stock_pkg_size, m.issue_units,qpmsd"
						+ ".transit_qty,m.cust_item_code";
				if (filter.get("batchno") != null && !filter.get("batchno").equals("")) {
					psQuey = CURRENT_TRANSIT_STOCK_QYERY + "AND pmsd.batch_no = ?"
							+ "GROUP BY dept_id,medicine_name,sibd.batch_no, " +
							"m.issue_base_unit, sibd.mrp, pmsd.package_uom, pmsd.stock_pkg_size, m"
							+ ".issue_units,qpmsd.transit_qty,m.cust_item_code";
				}
				try (PreparedStatement ps = con.prepareStatement(psQuey)) {
					ps.setInt(1, Integer.parseInt((String) filter.get("store_id")));
					ps.setInt(2, medicineId);
					ps.setInt(3, Integer.parseInt((String) filter.get("store_id")));
					if (filter.get("batchno") != null && !filter.get("batchno").equals("")) {
						ps.setString(4, (String) filter.get("batchno"));
					}
					l = DataBaseUtil.queryToDynaList(ps);
				}
				//Added to support qty_in_rejection
			} else if (openOrclose.equals("RJ")) {
				psQuey = CHECK_POINT_REJECTION_STOCK_QUERY
						+ "GROUP BY store_id,medicine_name,sibd.batch_no, m.issue_base_unit, " +
						"sibd.mrp, pmsd.package_uom, pmsd.stock_pkg_size, m.issue_units,qpmsd"
						+ ".qty_in_rejection,m.cust_item_code";
				if (!filter.get("batchno").equals("")) {
					psQuey = CHECK_POINT_REJECTION_STOCK_QUERY + " AND pcd.batch_no = ?"
							+ "GROUP BY store_id,medicine_name,sibd.batch_no, " +
							"m.issue_base_unit, sibd.mrp, pmsd.package_uom, pmsd.stock_pkg_size, m"
							+ ".issue_units,qpmsd.qty_in_rejection,m.cust_item_code";
				}
				try (PreparedStatement ps = con.prepareStatement(psQuey)) {
					ps.setInt(1, Integer.parseInt((String) filter.get("store_id")));
					ps.setInt(2, Integer.parseInt((String) filter.get("toCheckPt")));
					ps.setInt(3, medicineId);
					ps.setInt(4, Integer.parseInt((String) filter.get("store_id")));
					if (!filter.get("batchno").equals("")) {
						ps.setString(5, (String) filter.get("batchno"));
					}
					l = DataBaseUtil.queryToDynaList(ps);
				}
			} else if (openOrclose.equals("RJM")) {

				psQuey = CURRENT_REJECTION_STOCK_QYERY
						+ "GROUP BY dept_id,medicine_name,sibd.batch_no,m.issue_base_unit, " +
						"sibd.mrp, pmsd.package_uom, pmsd.stock_pkg_size, m.issue_units,qpmsd"
						+ ".qty_in_rejection,m.cust_item_code";
				if (filter.get("batchno") != null && !filter.get("batchno").equals("")) {
					psQuey = CURRENT_REJECTION_STOCK_QYERY + "AND pmsd.batch_no = ?"
							+ "GROUP BY dept_id,medicine_name,sibd.batch_no, " +
							"m.issue_base_unit, sibd.mrp, pmsd.package_uom, pmsd.stock_pkg_size, m"
							+ ".issue_units,qpmsd.qty_in_rejection,m.cust_item_code";
				}
				try (PreparedStatement ps = con.prepareStatement(psQuey)) {
					ps.setInt(1, Integer.parseInt((String) filter.get("store_id")));
					ps.setInt(2, medicineId);
					ps.setInt(3, Integer.parseInt((String) filter.get("store_id")));
					if (filter.get("batchno") != null && !filter.get("batchno").equals("")) {
						ps.setString(4, (String) filter.get("batchno"));
					}
					l = DataBaseUtil.queryToDynaList(ps);
				}
			} else {

				psQuey = CURRENT_CLOSING_STOCK_QYERY
						+ "GROUP BY dept_id,medicine_name,sibd.batch_no,m.issue_base_unit, " +
						"sibd.mrp, pmsd.package_uom, pmsd.stock_pkg_size, m.issue_units,qpmsd.qty,"
						+ "m.cust_item_code";
				if (filter.get("batchno") != null && !filter.get("batchno").equals("")) {
					psQuey = CURRENT_CLOSING_STOCK_QYERY + "AND pmsd.batch_no = ?"
							+ "GROUP BY dept_id,medicine_name,sibd.batch_no, " +
							"m.issue_base_unit, sibd.mrp, pmsd.package_uom, pmsd.stock_pkg_size, m"
							+ ".issue_units,qpmsd.qty,m.cust_item_code";
				}
				try (PreparedStatement ps = con.prepareStatement(psQuey)) {
					ps.setInt(1, Integer.parseInt((String) filter.get("store_id")));
					ps.setInt(2, medicineId);
					ps.setInt(3, Integer.parseInt((String) filter.get("store_id")));
					if (filter.get("batchno") != null && !filter.get("batchno").equals("")) {
						ps.setString(4, (String) filter.get("batchno"));
					}
					l = DataBaseUtil.queryToDynaList(ps);
				}
			}
			return l;
		}
	}




}
