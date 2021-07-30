package com.insta.hms.core.inventory.sales;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class SalesDetailsRepository extends GenericRepository{

	public SalesDetailsRepository() {
		super("store_sales_details");
	}
	
	private static final String GET_SALE_ITEM_SUBGROUPS =
			"SELECT sctd.* FROM store_sales_details ssd "
			+ "JOIN sales_claim_tax_details sctd USING(sale_item_id) where ssd.sale_item_id = ? AND sctd.claim_id = ? ";
	
	public List<BasicDynaBean> getSaleItemSubgroups(int saleItemId, String claimId) throws SQLException {
		return DatabaseHelper.queryToDynaList(GET_SALE_ITEM_SUBGROUPS, new Object[]{saleItemId, claimId});
	}
	
	public static final String UNLOCK_VISIT_SALE_ITEMS= " UPDATE store_sales_details ssd SET is_claim_locked=false " +
			" FROM store_sales_main ssm  JOIN bill b ON(b.bill_no = ssm.bill_no) WHERE ssd.sale_id = ssm.sale_id AND " +
			" ssd.is_claim_locked=true AND b.visit_id = ? ";

	public Boolean unlockVisitSaleItems(String visitId, String billStatus) {
		// TODO Auto-generated method stub
		String query = billStatus.equals("open") ? UNLOCK_VISIT_SALE_ITEMS.concat(" AND b.status = 'A' ") : UNLOCK_VISIT_SALE_ITEMS;
		return DatabaseHelper.update(query, new Object[]{visitId}) >= 0;
	}

	public static final String LOCK_VISIT_SALE_ITEMS= " UPDATE store_sales_details ssd SET is_claim_locked=true " +
			" FROM store_sales_main ssm  JOIN bill b ON(b.bill_no = ssm.bill_no) WHERE ssd.sale_id = ssm.sale_id AND " +
			" ssd.is_claim_locked=false AND b.visit_id = ? ";
	
	public Boolean lockVisitSaleItems(String visitId, String billStatus) {
		// TODO Auto-generated method stub
		String query = billStatus.equals("open") ? LOCK_VISIT_SALE_ITEMS.concat(" AND b.status = 'A' ") : LOCK_VISIT_SALE_ITEMS;
		return DatabaseHelper.update(query, new Object[]{visitId}) >= 0;
	}
	
	private static final String GET_SALES_TAX_DETAILS = " SELECT ssm.charge_id, sstd.item_subgroup_id AS tax_sub_group_id, "+
			" MAX(sstd.tax_rate) AS tax_rate, "+
			" sum(sstd.tax_amt) AS tax_amount "+
			" FROM bill b "+
			" JOIN store_sales_main ssm ON (ssm.bill_no = b.bill_no) "+
			" JOIN bill_charge bc ON (bc.charge_id = ssm.charge_id AND bc.bill_no = ssm.bill_no) "+
			" JOIN store_sales_details ssd ON (ssd.sale_id = ssm.sale_id) "+
			" JOIN store_sales_tax_details sstd ON (sstd.sale_item_id = ssd.sale_item_id) "+
			" WHERE b.visit_id = ? AND b.status = 'A' "+
			" GROUP BY ssm.charge_id,ssm.sale_id,sstd.item_subgroup_id,ssm.type ";

	public List<BasicDynaBean> getSalesTaxDetails(String visitId) {
		// TODO Auto-generated method stub
		return DatabaseHelper.queryToDynaList(GET_SALES_TAX_DETAILS, new Object[]{visitId});
	}

	private static String UPDATE_SALES_TAX_DETAILS = "UPDATE store_sales_details SET amount = (amount - tax) + ?, tax = ? WHERE sale_item_id = ?";
	
  public Boolean updateTaxInStoreSalesDetails(BasicDynaBean taxItemBean) {
    // TODO Auto-generated method stub
    return DatabaseHelper.update(UPDATE_SALES_TAX_DETAILS, new Object[]{taxItemBean.get("tax"), 
        taxItemBean.get("tax"), taxItemBean.get("sale_item_id")}) >= 0;
  }
}
