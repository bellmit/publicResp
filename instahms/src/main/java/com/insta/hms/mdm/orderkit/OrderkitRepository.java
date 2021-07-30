package com.insta.hms.mdm.orderkit;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class OrderkitRepository.
 *
 * @author irshadmohammed
 */
@Repository
public class OrderkitRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new orderkit repository.
   */
  public OrderkitRepository() {
    super("order_kit_main", "order_kit_id", "order_kit_name");
  }

  private static final String GET_ORDER_KIT_ITEM_DETAILS = "SELECT medicine_id, qty_needed,"
      + " medicine_name, issue_units FROM order_kit_details "
      + "JOIN store_item_details sid USING(medicine_id) where order_kit_id = ?"
      + " order by upper(medicine_name)";

  public List<BasicDynaBean> getOrderkitItemsDetails(int orderkitId) {
    return DatabaseHelper.queryToDynaList(GET_ORDER_KIT_ITEM_DETAILS, new Object[] { orderkitId });
  }

  public static final String GET_NONISSUABLE_ITEMS = "SELECT medicine_id, medicine_name"
      + " FROM order_kit_details " + " JOIN store_item_details sid USING(medicine_id) "
      + " JOIN store_category_master scm ON ( category_id = med_category_id ) "
      + "where order_kit_id = ?  AND issue_type IN ('P', 'L') ";

  public List<BasicDynaBean> getNonIssuableItems(int orderkitId) {
    return DatabaseHelper.queryToDynaList(GET_NONISSUABLE_ITEMS, new Object[] { orderkitId });
  }

  /**
   * Gets the order kit items stock status.
   *
   * @param deptId
   *          the dept id
   * @param orderKitId
   *          the order kit id
   * @param issueType
   *          the issue type
   * @return the order kit items stock status
   */
  public List<BasicDynaBean> getOrderKitItemsStockStatus(int deptId, int orderKitId,
      String[] issueType, boolean includeZeroStock) {
    StringBuilder stockStatusQuery = new StringBuilder();
    stockStatusQuery.append("select coalesce(sum(qty),0) as in_stock_qty,medicine_id,qty_needed "
        + "from store_stock_details " + "JOIN order_kit_details USING(medicine_id) "
        + "JOIN store_item_details std USING(medicine_id) "
        + "JOIN  store_category_master scm ON ( category_id = med_category_id ) "
        + "where dept_id = ? AND order_kit_id=? AND asset_approved = 'Y'");

    if (!includeZeroStock) {
      stockStatusQuery.append(" AND qty > 0 ");
    }
    
    if (issueType != null) {
      stockStatusQuery.append(" AND issue_type IN (");
      boolean first = true;
      for (String type : issueType) {
        if (!first) {
          stockStatusQuery.append(",");
        }
        first = false;
        stockStatusQuery.append('?');
      }
      stockStatusQuery.append(")");
    }
    stockStatusQuery.append(" group by medicine_id, qty_needed");

    List<Object> queryParams = new ArrayList<>();
    queryParams.add(deptId);
    queryParams.add(orderKitId);
    queryParams.addAll(Arrays.asList(issueType));
    return DatabaseHelper.queryToDynaList(stockStatusQuery.toString(), queryParams.toArray());
  }

}
