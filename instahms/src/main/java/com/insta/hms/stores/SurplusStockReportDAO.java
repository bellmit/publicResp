package com.insta.hms.stores;

import au.com.bytecode.opencsv.CSVWriter;
import com.bob.hms.common.DataBaseUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The Class SurplusStockReportDAO.
 */
public class SurplusStockReportDAO {

  /** The Constant GET_NON_MOVING_MEDICINES. */
  private static final String GET_NON_MOVING_MEDICINES = "select item_name,sum(sold_qty)"
      + " as sold_qty,  sum(available_qty) as available_qty, Round(avg(value_cp),2)as value_cp,"
      + "Round(avg(value_mrp),2) as value_mrp  from ( " + "select medicine_name as item_name, "
      + " 0 as sold_qty,stock_qty as available_qty, "
      + " (unit_cp*stock_qty) AS value_cp, (unit_mrp*stock_qty) AS value_mrp "
      + " from store_item_in_stock_view  pmsv " + " JOIN stores s USING (dept_id) "
      + " where medicine_id not in " + " (select medicine_id from store_sales_details_view "
      + " JOIN stores s USING  (dept_id) " + " where sales_date between ? and  ? ) "
      + " and s.dept_id::character varying = ? " + " ORDER BY dept_name "
      + " ) as foo  group by item_name ";

  /** The Constant GET_SLOW_MOVING_MEDICINES. */
  private static final String GET_SLOW_MOVING_MEDICINES = "select * from (select item_name,"
      + "sum(sold_qty) as sold_qty,sum(available_qty) as available_qty ,  case when "
      + "sum(sold_qty)=0 THEN sum(value_cp) else Round(avg(value_cp), 2) end as value_cp, "
      + " case when sum(sold_qty)=0 THEN sum(value_mrp) else Round(avg(value_mrp),2)end"
      + " as value_mrp  from ( " + " select medicine_name as item_name, "
      + " sum(sold_qty) as sold_qty,stock_qty as available_qty,"
      + " (unit_cp*sum(sold_qty)) AS value_cp, (unit_mrp*sum(sold_qty)) AS value_mrp "
      + " from store_sales_details_view pmsv "
      + " JOIN store_item_in_stock_view sv USING (medicine_id,batch_no,dept_id) "
      + " JOIN stores s USING (dept_id) " + " where  sales_date between  ? and  ? "
      + " and s.dept_id::character varying = ? "
      + " group by medicine_id,medicine_name,stock_qty,s.dept_name,pmsv.batch_no,sv.unit_cp,"
      + " sv.unit_mrp  ORDER BY s.dept_name "
      + " ) as foo group by item_name) as foo1 group by  item_name, sold_qty, available_qty,"
      + "  value_cp, value_mrp having sold_qty  < ? ::integer";

  /** The Constant GET_FAST_MOVING_MEDICINES. */
  private static final String GET_FAST_MOVING_MEDICINES = " select * from (select item_name,"
      + " sum(sold_qty) as sold_qty,sum(available_qty) as available_qty ,  case when "
      + " sum(sold_qty)=0 THEN sum(value_cp) else Round(avg(value_cp), 2) end as value_cp, "
      + " case when sum(sold_qty)=0 THEN sum(value_mrp) else Round(avg(value_mrp),2)end"
      + " as value_mrp  from ( " + " select medicine_name as item_name, "
      + " sum(sold_qty) as sold_qty,stock_qty as available_qty,"
      + " (unit_cp*sum(sold_qty)) AS value_cp, (unit_mrp*sum(sold_qty)) AS value_mrp "
      + " from store_sales_details_view pmsv "
      + " JOIN store_item_in_stock_view sv USING (medicine_id,batch_no,dept_id) "
      + " JOIN stores s USING (dept_id) " + " where  sales_date between  ? and ? "
      + " and s.dept_id::character varying = ? "
      + " group by s.dept_name,medicine_id,medicine_name,stock_qty,pmsv.batch_no,sv.unit_cp,"
      + " sv.unit_mrp ORDER BY s.dept_name  ) as foo group by item_name) as foo1  group by  "
      + " item_name, sold_qty, available_qty,  value_cp, value_mrp having sold_qty  "
      + " > ? ::integer";

  /** The Constant GET_MON_AVG_REPORT. */
  private static final String GET_MON_AVG_REPORT = " select * # from ("
      + " select medicine_id,sum(qty) as qty_consumed,dept_id,medicine_name,dept_name from ("
      + " select medicine_id,sum(qty_consumed) as qty,store_from as dept_id from "
      + " store_stk_trnsfr_view where txndate between ? and  ?  and store_from::character"
      + " varying = ? group by store_from,medicine_id  union all "
      + " select medicine_id,sum(qty) as qty,dept_id from store_stk_issue_view"
      + " where txndate between ? and  ?  and dept_id::character varying = ? "
      + " group by dept_id,medicine_id" + " union all"
      + " select medicine_id,sum(sold_qty) as qty,dept_id from store_sales_details_view"
      + " where sales_date between ? and  ?  and dept_id::character varying = ? "
      + " group by dept_id,medicine_id)as foo" + " join store_item_details using(medicine_id)"
      + " JOIN stores s USING (dept_id)" + " group by dept_id,dept_name,medicine_id,medicine_name"
      + " order by medicine_id ) as foo";

  /**
   * Gets the NS fmedicines.
   *
   * @param writer the writer
   * @param fromDate the from date
   * @param toDate the to date
   * @param storeId the store id
   * @param fsn the fsn
   * @param qty the qty
   * @param diff the diff
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void getNSFmedicines(CSVWriter writer, Date fromDate, Date toDate, String storeId,
      String fsn, String qty, String diff) throws SQLException, IOException {

    Connection con = null;
    ResultSet rs = null;
    PreparedStatement ps = null;
    String query = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (fsn.equals("n")) {
        ps = con.prepareStatement(GET_NON_MOVING_MEDICINES);
      } else if (fsn.equals("s")) {
        ps = con.prepareStatement(GET_SLOW_MOVING_MEDICINES);
      } else if (fsn.equals("f")) {
        ps = con.prepareStatement(GET_FAST_MOVING_MEDICINES);
      } else if (fsn.equals("a")) {
        query = GET_MON_AVG_REPORT;
        String extraColumn = " ,";
        if (diff != null && !diff.equals("")) {
          int days = Integer.parseInt(diff);
          // $F{qty_consumed}.divide( new BigDecimal($P{diffdays}).divide(new
          // BigDecimal("30"),2,BigDecimal.ROUND_HALF_UP), 2,BigDecimal.ROUND_HALF_UP ) :
          // $F{qty_consumed}.multiply( new BigDecimal("30").divide(new
          // BigDecimal($P{diffdays}),2,BigDecimal.ROUND_HALF_UP))
          if (days >= 30) {
            extraColumn = extraColumn + " round(qty_consumed/(round(" + days
                + "/30,2)),2) as monthlyavgqty ";
          } else {
            extraColumn = extraColumn + " round(qty_consumed * ( round(30/" + days
                + ",2)),2) as monthlyavgqty ";
          }
        }
        String finalQuery = query.replace("#", extraColumn);
        ps = con.prepareStatement(finalQuery);
      }

      int i1 = 1;
      if (null != ps) {
        ps.setDate(i1++, fromDate);
        ps.setDate(i1++, toDate);
        ps.setString(i1++, storeId);
        if (fsn.equals("s") || fsn.equals("f")) {
          ps.setString(i1++, qty);
        }
        if (fsn.equals("a")) {
          ps.setDate(i1++, fromDate);
          ps.setDate(i1++, toDate);
          ps.setString(i1++, storeId);
          ps.setDate(i1++, fromDate);
          ps.setDate(i1++, toDate);
          ps.setString(i1++, storeId);
        }

        rs = ps.executeQuery();

      }
      writer.writeAll(rs, true);
      writer.flush();
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

}