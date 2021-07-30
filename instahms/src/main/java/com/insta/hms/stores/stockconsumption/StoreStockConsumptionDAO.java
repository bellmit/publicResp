package com.insta.hms.stores.stockconsumption;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.stores.stockconsumption.StoreStockConsumptionBO.StockConsumptionDetails;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class StoreStockConsumptionDAO.
 *
 * @author mithun.saha
 */
public class StoreStockConsumptionDAO extends GenericDAO {

  /**
   * Instantiates a new store stock consumption DAO.
   */
  public StoreStockConsumptionDAO() {
    super("general_reagent_usage_main");
  }

  /** The store consumption list fields. */
  private static String STORE_CONSUMPTION_LIST_FIELDS = " SELECT * ";

  /** The store consumption list count. */
  private static String STORE_CONSUMPTION_LIST_COUNT = " SELECT count(*) ";

  /** The store consumption list tables. */
  private static String STORE_CONSUMPTION_LIST_TABLES = " FROM general_reagent_usage_main grum "
      + " JOIN stores st ON(st.dept_id = grum.store_id)";

  /**
   * Gets the store consumption details list.
   *
   * @param map          the map
   * @param pagingParams the paging params
   * @return the store consumption details list
   * @throws Exception      the exception
   * @throws ParseException the parse exception
   */
  public PagedList getStoreConsumptionDetailsList(Map map, Map pagingParams)
      throws Exception, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, STORE_CONSUMPTION_LIST_FIELDS,
          STORE_CONSUMPTION_LIST_COUNT, STORE_CONSUMPTION_LIST_TABLES, pagingParams);

      qb.addFilterFromParamMap(map);
      qb.addSecondarySort("consumption_id");
      qb.build();

      PagedList pagedList = qb.getMappedPagedList();
      return pagedList;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The store stock fields. */
  private static String STORE_STOCK_FIELDS = "SELECT  * ";

  /** The store stock count. */
  private static String STORE_STOCK_COUNT = " SELECT count(*) ";

  /** The store stock tables. */
  private static String STORE_STOCK_TABLES = " FROM ( SELECT medicine_name as item_name,"
      + "medicine_name,sum(qty) AS stock_qty,sum(qty) as qty, "
      + " ssd.item_batch_id,sibd.batch_no,sid.med_category_id,scm.category,"
      + " sid.service_sub_group_id,ssg.service_sub_group_name,bin,"
      + " ssg.service_group_id,sg.service_group_name,sid.manf_name as manf_code,"
      + " mm.manf_name,ssd.dept_id,sid.cust_item_code " + " FROM store_item_details sid "
      + " LEFT JOIN store_stock_details ssd ON(sid.medicine_id = ssd.medicine_id) "
      + " LEFT JOIN store_item_batch_details sibd ON(sibd.item_batch_id = ssd.item_batch_id) "
      + " LEFT JOIN store_category_master scm ON(sid.med_category_id = scm.category_id) "
      + " LEFT JOIN service_sub_groups ssg ON "
      + " (ssg.service_sub_group_id = sid.service_sub_group_id) "
      + " LEFT JOIN service_groups sg ON(sg.service_group_id=ssg.service_group_id) "
      + " LEFT JOIN manf_master mm ON(mm.manf_code = sid.manf_name) "
      + " group by medicine_name,ssd.item_batch_id,sibd.batch_no,sid.med_category_id,"
      + " scm.category,sid.service_sub_group_id,"
      + " ssg.service_sub_group_name,bin,ssg.service_group_id,sg.service_group_name,"
      + " sid.manf_name,mm.manf_name,ssd.dept_id,sid.cust_item_code) AS foo";

  /**
   * Gets the store stock details.
   *
   * @param map           the map
   * @param pagingParams  the paging params
   * @param consumptionId the consumption id
   * @return the store stock details
   * @throws Exception      the exception
   * @throws ParseException the parse exception
   */
  public PagedList getStoreStockDetails(Map map, Map pagingParams, String consumptionId)
      throws Exception, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      List<Integer> medicineBatchesList = null;
      if (pagingParams.get(ConversionUtils.LISTING.SORTCOL) != null
          && pagingParams.get(ConversionUtils.LISTING.SORTCOL).equals("item_name")) {
        pagingParams.put(ConversionUtils.LISTING.SORTCOL, "medicine_name");
      }

      if (pagingParams.get(ConversionUtils.LISTING.SORTCOL) != null
          && pagingParams.get(ConversionUtils.LISTING.SORTCOL).equals("store_qty")) {
        pagingParams.put(ConversionUtils.LISTING.SORTCOL, "qty");
      }

      SearchQueryBuilder qb = new SearchQueryBuilder(con, STORE_STOCK_FIELDS, STORE_STOCK_COUNT,
          STORE_STOCK_TABLES, pagingParams);

      qb.addFilterFromParamMap(map);
      qb.addSecondarySort("item_batch_id");
      qb.addFilter(SearchQueryBuilder.NUMERIC, "qty", ">", new BigDecimal(0));

      if (consumptionId != null) {
        medicineBatchesList = new ArrayList<Integer>();
        for (BasicDynaBean bean : getMedcineIdsBatches(consumptionId)) {
          medicineBatchesList.add((Integer) bean.get("item_batch_id"));
        }
        if (medicineBatchesList != null && medicineBatchesList.size() > 0) {
          qb.addFilter(SearchQueryBuilder.INTEGER, "item_batch_id", "NOT IN", medicineBatchesList);
        }
      }

      qb.build();

      PagedList pagedList = qb.getMappedPagedList();
      return pagedList;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The stock consumption fields. */
  private static String STOCK_CONSUMPTION_FIELDS = " SELECT *  ";

  /** The stock consumption count. */
  private static String STOCK_CONSUMPTION_COUNT = " SELECT count(*) ";

  /** The stock consumption tables. */
  private static String STOCK_CONSUMPTION_TABLES = " FROM (SELECT sid.medicine_name"
      + " as item_name,grum.consumption_id, "
      + " sid.medicine_name,grud.item_batch_id,grud.reagent_detail_id,"
      + "sibd.batch_no,grud.stock_qty,qty,qty as store_qty,sid.cust_item_code "
      + " FROM general_reagent_usage_main grum "
      + " JOIN general_reagent_usage_details grud ON(grud.consumption_id = grum.consumption_id) "
      + " LEFT JOIN store_item_batch_details sibd ON(sibd.item_batch_id = grud.item_batch_id) "
      + " JOIN store_item_details sid ON(sibd.medicine_id = sid.medicine_id)) as foo ";

  /**
   * Gets the stock consumption details.
   *
   * @param map           the map
   * @param pagingParams  the paging params
   * @param consumptionId the consumption id
   * @return the stock consumption details
   * @throws Exception      the exception
   * @throws ParseException the parse exception
   */
  public PagedList getStockConsumptionDetails(Map map, Map pagingParams, String consumptionId)
      throws Exception, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      if (pagingParams.get(ConversionUtils.LISTING.SORTCOL) != null
          && pagingParams.get(ConversionUtils.LISTING.SORTCOL).equals("medicine_name")) {
        pagingParams.put(ConversionUtils.LISTING.SORTCOL, "item_name");
      }

      if (pagingParams.get(ConversionUtils.LISTING.SORTCOL) != null
          && pagingParams.get(ConversionUtils.LISTING.SORTCOL).equals("qty")) {
        pagingParams.put(ConversionUtils.LISTING.SORTCOL, "store_qty");
      }

      SearchQueryBuilder qb = new SearchQueryBuilder(con, STOCK_CONSUMPTION_FIELDS,
          STOCK_CONSUMPTION_COUNT, STOCK_CONSUMPTION_TABLES, pagingParams);

      qb.addFilter(SearchQueryBuilder.STRING, "consumption_id", "=", consumptionId);
      qb.addSecondarySort("reagent_detail_id");
      qb.build();

      PagedList pagedList = qb.getMappedPagedList();
      return pagedList;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant GET_MEDICINE_IDS. */
  public static final String GET_MEDICINE_IDS = "SELECT item_batch_id"
      + " FROM general_reagent_usage_details " + " WHERE consumption_id = ?";

  /**
   * Gets the medcine ids batches.
   *
   * @param consumptionId the consumption id
   * @return the medcine ids batches
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getMedcineIdsBatches(String consumptionId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_MEDICINE_IDS);
      ps.setString(1, consumptionId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_MEDICINE_BATCHES. */
  public static final String GET_MEDICINE_BATCHES = "SELECT batch_no FROM"
      + " general_reagent_usage_details " + " WHERE consumption_id = ?";

  /**
   * Gets the medcine batches.
   *
   * @param consumptionId the consumption id
   * @return the medcine batches
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getMedcineBatches(String consumptionId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_MEDICINE_BATCHES);
      ps.setString(1, consumptionId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant INSERT_STOCK_CONSUMPTION. */
  public static final String INSERT_STOCK_CONSUMPTION = "INSERT INTO general_reagent_usage_details"
      + "(reagent_detail_id,consumption_id,item_batch_id,stock_qty,qty) " + " values (?,?,?,?,?)";

  /**
   * Insert stock consumptions.
   *
   * @param con  the con
   * @param list the list
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean insertStockConsumptions(Connection con, List<StockConsumptionDetails> list)
      throws SQLException {
    PreparedStatement ps = con.prepareStatement(INSERT_STOCK_CONSUMPTION);
    boolean success = true;
    for (StockConsumptionDetails scd : list) {
      ps.setInt(1, scd.getConsumptionDetialsId());
      ps.setString(2, scd.getConsumptionId());
      ps.setInt(3, scd.getItemBatchid());
      ps.setBigDecimal(4, scd.getStockQty());
      ps.setBigDecimal(5, scd.getQty());
      ps.addBatch();
    }

    int[] results = ps.executeBatch();
    ps.close();

    for (int p = 0; p < results.length; p++) {
      if (results[p] <= 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  /** The Constant GET_STORE_STOCKS_CURRENT_QTY. */
  public static final String GET_STORE_STOCKS_CURRENT_QTY = "SELECT sum(qty) as qty"
      + " FROM store_stock_details "
      + " WHERE item_batch_id = ? AND dept_id = ?  group by item_batch_id";

  /**
   * Gets the stock items current qty.
   *
   * @param itemBatchid the item batchid
   * @param deptId      the dept id
   * @return the stock items current qty
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getStockItemsCurrentQty(int itemBatchid, int deptId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_STORE_STOCKS_CURRENT_QTY);
      ps.setInt(1, itemBatchid);
      ps.setInt(2, deptId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
