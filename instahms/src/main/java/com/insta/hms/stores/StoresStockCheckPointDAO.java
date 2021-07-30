package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class StoresStockCheckPointDAO.
 */
public class StoresStockCheckPointDAO {

  /** The con. */
  Connection con = null;

  /**
   * Instantiates a new stores stock check point DAO.
   *
   * @param con the con
   */
  StoresStockCheckPointDAO(Connection con) {
    this.con = con;
  }

  /** The Constant CHK_EXT_QUERY_FIELDS. */
  private static final String CHK_EXT_QUERY_FIELDS = "SELECT CHECKPOINT_ID,CHECKPOINT_NAME,"
      + "USER_NAME,CHECKPOINT_DATE,REMARKS";

  /** The Constant CHK_EXT_QUERY_COUNT. */
  private static final String CHK_EXT_QUERY_COUNT = " SELECT count(CHECKPOINT_ID) ";

  /** The Constant CHK_EXT_QUERY_TABLES. */
  private static final String CHK_EXT_QUERY_TABLES = " FROM store_checkpoint_main";

  /**
   * Searchchkpoints.
   *
   * @param filter the filter
   * @param listing the listing
   * @return the paged list
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList searchchkpoints(Map filter, Map listing)
      throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();

    SearchQueryBuilder qb = new SearchQueryBuilder(con, CHK_EXT_QUERY_FIELDS, CHK_EXT_QUERY_COUNT,
        CHK_EXT_QUERY_TABLES, listing);

    qb.addFilterFromParamMap(filter);
    qb.addSecondarySort("checkpoint_id");
    qb.build();

    PagedList list = qb.getMappedPagedList();

    qb.close();
    con.close();

    return list;
  }

  /** The Constant GETCHKPOINTS. */
  private static final String GETCHKPOINTS = "SELECT CHECKPOINT_ID,CHECKPOINT_NAME FROM"
      + " store_checkpoint_main  ORDER BY CHECKPOINT_NAME";

  /**
   * Gets the chkpoint names in master.
   *
   * @return the chkpoint names in master
   * @throws SQLException the SQL exception
   */
  public static ArrayList getChkpointNamesInMaster() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GETCHKPOINTS);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the next category id.
   *
   * @return the next category id
   * @throws SQLException the SQL exception
   */
  public int getNextCategoryId() throws SQLException {
    PreparedStatement ps = con.prepareStatement("select nextval('Pharmacy_checkpoint_sequence')");
    return DataBaseUtil.getIntValueFromDb(ps);
  }

  /**
   * Insert chkpoint detail.
   *
   * @param id the id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean insertChkpointDetail(int id) throws SQLException {
    String query = "INSERT INTO store_checkpoint_details(checkpoint_id, store_id,medicine_id,"
        + " batch_no,qty,mrp,cp) select ?, dept_id,ssd.medicine_id,sibd.batch_no,qty,"
        + " round(sibd.mrp/stock_pkg_size,2) as mrp,round(package_cp/stock_pkg_size,2)"
        + " as package_cp  from store_stock_details ssd JOIN store_item_batch_details sibd"
        + " USING(item_batch_id)";
    try (PreparedStatement ps = con.prepareStatement(query);) {
      ps.setInt(1, id);
      boolean success = false;
      int count = 0;
      count = ps.executeUpdate();
      if (count > 0) {
        success = true;
      }
      return success;
    }
  }

  /** The Constant GET_ALL_CHECKPOINTS. */
  private static final String GET_ALL_CHECKPOINTS = "SELECT CHECKPOINT_ID,CHECKPOINT_NAME,"
      + " to_char(CHECKPOINT_DATE,'dd-mm-yyyy hh24:mi:ss') as CHECKPOINT_DATE"
      + " FROM store_checkpoint_main ORDER BY checkpoint_name";

  /**
   * Gets the all checkpoints.
   *
   * @return the all checkpoints
   * @throws SQLException the SQL exception
   */
  public static ArrayList getAllCheckpoints() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_CHECKPOINTS);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_CHECKPOINT_NAMES. */
  private static final String GET_CHECKPOINT_NAMES = "SELECT * FROM store_checkpoint_main"
      + "  ORDER BY checkpoint_id DESC";

  /**
   * Gets the check point names.
   *
   * @return the check point names
   * @throws SQLException the SQL exception
   */
  public static List getCheckPointNames() throws SQLException {

    return DataBaseUtil.queryToDynaList(GET_CHECKPOINT_NAMES);
  }

}
