package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.QueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class SalesClaimDetailsDAO extends GenericDAO {

  public SalesClaimDetailsDAO() {
    super("sales_claim_details");
  }

  private String visit_sold_items_claim_details = " SELECT ssd.sale_item_id,scd.insurance_claim_amt,scd.org_insurance_claim_amount,scd.claim_status,scd.claim_id,scd.tax_amt FROM store_sales_main ssm "
      + " JOIN store_sales_details ssd USING(sale_id) "
      + " JOIN sales_claim_details scd USING(sale_item_id) " + " JOIN bill b USING(bill_no) ";

  private String where_visit_sold_items_claim_details = "WHERE b.visit_id = ?  AND store_id = ? "
      + "ORDER BY sales_item_plan_claim_id";

  private String where_sale_sold_items_claim_details = " WHERE ssm.sale_id = ? "
      + " ORDER BY sales_item_plan_claim_id";

  /**
   * Gives visit id and store id this method list out item claim amount for no of plans of the visit
   * sales.
   * 
   * @param visitId
   * @param storeId
   * @return
   * @throws SQLException
   */
  public List<BasicDynaBean> getVisitSoldItemsClaimDetails(String visitId, int storeId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con
          .prepareStatement(visit_sold_items_claim_details + where_visit_sold_items_claim_details);
      ps.setString(1, visitId);
      ps.setInt(2, storeId);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gives saleid this method list out item claim amount for no of plans of the visit sales.
   * 
   * @param visitId
   * @param storeId
   * @return
   * @throws SQLException
   */
  public List<BasicDynaBean> getVisitSoldItemsClaimDetailsOfASale(String saleId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con
          .prepareStatement(visit_sold_items_claim_details + where_sale_sold_items_claim_details);
      ps.setString(1, saleId);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private String sales_claim_details = " SELECT sum(insurance_claim_amt) as total_insurance_claim_amt,claim_id,charge_id,bill_no,priority FROM "
      + "	(SELECT scd.insurance_claim_amt,scd.claim_id,ssm.charge_id,ssm.bill_no,scd.sale_item_id,bcl.priority "
      + "	FROM sales_claim_details scd " + "	JOIN store_sales_details ssd USING(sale_item_id) "
      + "	JOIN store_sales_main ssm USING(sale_id)  "
      + "   JOIN bill_claim bcl ON(bcl.bill_no = ssm.bill_no AND bcl.claim_id = scd.claim_id) ORDER BY bcl.priority ) as foo";

  public List<BasicDynaBean> getSalesClaimDetails(Connection con, String[] saleItemIds)
      throws SQLException {

    PreparedStatement ps = null;
    try {

      StringBuilder query = new StringBuilder(sales_claim_details);
      List<String> saleItemIdList = Arrays.asList(saleItemIds);
      QueryBuilder.addWhereFieldOpValue(false, query, "sale_item_id", "IN", saleItemIdList);
      ps = con.prepareStatement(query
          .append(" GROUP BY claim_id,charge_id,bill_no,priority ORDER BY priority").toString());

      int index = 1;

      for (String saleItemId : saleItemIdList) {
        ps.setInt(index, Integer.parseInt(saleItemId));
        index++;
      }

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }

  }

  private String sales_bill_claim_details = " SELECT scd.* " + "	from sales_claim_details scd "
      + "	JOIN store_sales_details ssd USING(sale_item_id) "
      + "	JOIN store_sales_main ssm USING(sale_id)  ";

  public List<BasicDynaBean> getSalesClaimDetails(Connection con, String billNo)
      throws SQLException {

    PreparedStatement ps = null;
    try {

      StringBuilder query = new StringBuilder(sales_bill_claim_details);
      ps = con.prepareStatement(query.append("  WHERE ssm.bill_no = ? ").toString());
      ps.setString(1, billNo);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }

  }

  public List<BasicDynaBean> getSalesClaimDetails(Connection con, String billNo, String claimId)
      throws SQLException {

    PreparedStatement ps = null;
    try {

      StringBuilder query = new StringBuilder(sales_bill_claim_details);
      ps = con
          .prepareStatement(query.append("  WHERE ssm.bill_no = ? AND claim_id = ? ").toString());
      ps.setString(1, billNo);
      ps.setString(2, claimId);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }

  }

  public boolean updateSalesClaimId(Connection con, List<BasicDynaBean> salesClaimDetails,
      String newClaimId) throws SQLException, IOException {

    boolean result = true;
    for (BasicDynaBean salesClaimBean : salesClaimDetails) {
      salesClaimBean.set("claim_id", newClaimId);

      result &= (update(con, salesClaimBean.getMap(), "sales_item_plan_claim_id",
          (Integer) salesClaimBean.get("sales_item_plan_claim_id"))) > 0;

      if (!result)
        break;

    }

    return result;
  }

  private String get_tax_amt = "select sum(tax_amt) as tax_amt, sum(insurance_claim_amt) as insurance_claim_amt from sales_claim_details where sale_item_id = ?";

  public BasicDynaBean getTaxAmt(Integer saleItemId) {
    return DatabaseHelper.queryToDynaBean(get_tax_amt, new Object[] { saleItemId });
  }

	private static final String UPDATE_CLAIMID_IN_SALES_CLAIM_DETAILS = " UPDATE "
			+ " sales_claim_details scd SET claim_id = ?, sponsor_id = ? "
			+ " FROM  store_sales_details ssd "
			+ " JOIN  store_sales_main ssm ON(ssm.sale_id = ssd.sale_id) "
			+ " WHERE scd.sale_item_id = ssd.sale_item_id AND "
			+ " ssm.bill_no = ? AND scd.claim_id = ? ";

	public int updateSalesClaimOnEditIns(String billNo, String sponsorId,
											 String oldClaimId, String newClaimId) {
		return DatabaseHelper.update(UPDATE_CLAIMID_IN_SALES_CLAIM_DETAILS,
				new Object[]{newClaimId, sponsorId, billNo, oldClaimId});
	}

	private static final String UPDATE_CLAIM_AMT = " UPDATE "
			+ " sales_claim_details scd SET insurance_claim_amt = 0.00, return_insurance_claim_amt = 0.00, "
			+ " ref_insurance_claim_amount = 0.00"
			+ " FROM  store_sales_details ssd "
			+ " WHERE scd.sale_item_id = ssd.sale_item_id AND "
			+ " ssd.sale_id = ? ";

	public boolean updateSalesClaimAmt(String saleId) throws SQLException {
		return (DatabaseHelper.update(UPDATE_CLAIM_AMT, new Object[]{saleId})) > 0;
	}
}
