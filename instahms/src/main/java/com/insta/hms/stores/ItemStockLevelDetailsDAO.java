package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class ItemStockLevelDetailsDAO.
 *
 * @author irshad.mohamad
 */
public class ItemStockLevelDetailsDAO {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(ItemStockLevelDetailsDAO.class);

  /** The item stock level fields. */
  private static final String ITEM_STOCK_LEVEL_FIELDS = " SELECT * FROM (";

  /** The item stock level count. */
  private static final String ITEM_STOCK_LEVEL_COUNT = " SELECT count(*) FROM ( ";

  /** The item stock level exist qty. */
  private static final String ITEM_STOCK_LEVEL_EXIST_QTY = " SELECT foos.med_id as medicine_id,"
      + " foos.med_category_id,foos.med_name as medicine_name ,foos.category, foos.manf_name,"
      + " foos.bin, foos.generic_name, foos.department_id as store_id, foos.item_barcode_id,"
      + " foos.package_uom, sum(foos.availableqty) as availableqty, foos.package_type,"
      + " srl.min_level , srl.max_level, srl.reorder_level, srl.danger_level,transit FROM ("
      + " SELECT sid.medicine_id as med_id,sid.med_category_id, sid.medicine_name as med_name,"
      + " scm.category, mm.manf_name, COALESCE(isld.bin,sid.bin) as bin, "
      + " gn.generic_name, ssd.dept_id as department_id , sid.item_barcode_id,sid.package_uom,"
      + " sum(ssd.qty) as availableqty,sid.package_type,sum(qty_in_transit) as transit,"
      + " sid.cust_item_code "
      +

      " From store_item_details AS sid JOIN store_stock_details AS ssd "
      + " ON sid.medicine_id = ssd.medicine_id "
      + " LEFT JOIN item_store_level_details  isld ON isld.medicine_id = ssd.medicine_id AND "
      + " isld.dept_id = ssd.dept_id LEFT JOIN manf_master AS mm ON sid.manf_name = mm.manf_code "
      + " LEFT JOIN store_category_master AS scm ON sid.med_category_id = scm.category_id "
      + " LEFT JOIN generic_name AS gn ON sid.generic_name = gn.generic_code "
      + " GROUP BY sid.medicine_name, mm.manf_name, ssd.dept_id, gn.generic_name, "
      + " scm.category,COALESCE(isld.bin,sid.bin), sid.item_barcode_id,sid.package_uom,"
      + " sid.medicine_id, sid.med_category_id,sid.package_type,sid.cust_item_code) as foos";

  /** The item stock level join condition. */
  private static final String ITEM_STOCK_LEVEL_JOIN_CONDITION = " LEFT JOIN store_reorder_levels AS srl "
      + " ON med_id = srl.medicine_id AND srl.dept_id = department_id "
      + " LEFT JOIN stores AS strs " + " ON  foos.department_id = strs.dept_id "
      + " GROUP BY med_name, med_id, department_id, dept_name, srl.min_level , srl.max_level,"
      + " srl.reorder_level, srl.danger_level, category, manf_name,bin,generic_name,"
      + " item_barcode_id, package_uom, med_category_id,package_type,transit";

  private static final String ITEM_STOCK_LEVEL_QUERY = ITEM_STOCK_LEVEL_FIELDS
      + ITEM_STOCK_LEVEL_EXIST_QTY + ITEM_STOCK_LEVEL_JOIN_CONDITION + ") foozz ";
  
  private static final String WHERE = "WHERE";
  
  /**
   * Gets the item stock level details list.
   *
   * @param map the map
   * @param pagingParams the paging params
   * @param centerId the center id
   * @return the item stock level details list
   * @throws Exception the exception
   * @throws ParseException the parse exception
   */
  public PagedList getItemStockLevelDetailsList(Map map, Map pagingParams, int centerId)
      throws Exception, ParseException {

    Connection con = null;
    StringBuilder itemStockLevelQuery =  new StringBuilder(ITEM_STOCK_LEVEL_QUERY);

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      Map paramMap = new HashMap(map);

      if (map.get("qty") != null) {
        String[] value = (String[]) paramMap.get("qty");
        for (String filter : value) {
          if (filter.equals("e")) {
            if (itemStockLevelQuery.toString().contains(WHERE)) {
              itemStockLevelQuery.append(" AND availableqty = 0");
            } else {
              itemStockLevelQuery.append(" WHERE availableqty = 0");
            }
          }

          if (filter.equals("g")) {
            if (itemStockLevelQuery.toString().contains(WHERE)) {
              itemStockLevelQuery.append(" AND availableqty > 0");
            } else {
              itemStockLevelQuery.append(" WHERE availableqty > 0");
            }
          }

          if (filter.equals("l")) {
            if (itemStockLevelQuery.toString().contains(WHERE)) {
              itemStockLevelQuery.append(" AND availableqty < 0");
            } else {
              itemStockLevelQuery.append(" WHERE availableqty < 0");
            }
          }
        }
        paramMap.remove("qty");
      }

      // Type of stock reorder level
      if (map.get("qtycond") != null) {
        String[] value = (String[]) paramMap.get("qtycond");
        for (String filter : value) {
          // Below Reorder Level is selected
          if (filter.equals("brl")) {
            if (itemStockLevelQuery.toString().contains(WHERE)) {
              itemStockLevelQuery.append(" AND availableqty < reorder_level");
            } else {
              itemStockLevelQuery.append(" WHERE availableqty < reorder_level");
            }

          }
          // Below Danger Level is selected
          if (filter.equals("bdl")) {
            if (itemStockLevelQuery.toString().contains(WHERE)) {
              itemStockLevelQuery.append(" AND availableqty < danger_level");
            } else {
              itemStockLevelQuery.append(" WHERE availableqty < danger_level");
            }
          }
          // Below Minimum Level is selected
          if (filter.equals("bml")) {
            if (itemStockLevelQuery.toString().contains(WHERE)) {
              itemStockLevelQuery.append(" AND availableqty < min_level");
            } else {
              itemStockLevelQuery.append(" WHERE availableqty < min_level");
            }
          }
          // Above Maximum Level is selected
          if (filter.equals("aml")) {
            if (itemStockLevelQuery.toString().contains(WHERE)) {
              itemStockLevelQuery.append(" AND availableqty > max_level");
            } else {
              itemStockLevelQuery.append(" WHERE availableqty > max_level");
            }
          }
        }
        paramMap.remove("qtycond");
      }
      itemStockLevelQuery.append(") foo");
      SearchQueryBuilder qb = new SearchQueryBuilder(con, ITEM_STOCK_LEVEL_FIELDS,
          ITEM_STOCK_LEVEL_COUNT, itemStockLevelQuery.toString(), pagingParams);
      qb.addFilterFromParamMap(paramMap);
      qb.addSecondarySort("medicine_id");
      qb.build();

      return qb.getMappedPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

}