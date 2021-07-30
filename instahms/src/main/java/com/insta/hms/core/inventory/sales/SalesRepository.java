package com.insta.hms.core.inventory.sales;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SalesRepository extends GenericRepository{

	public SalesRepository() {
		super("store_sales_main");
		// TODO Auto-generated constructor stub
	}

	private static final String GET_SALE_BILL_CHARGES = "SELECT bc.* " +
			" FROM store_sales_main ssm " +
			" JOIN bill_charge_claim bc ON(bc.charge_id = ssm.charge_id) "+
			" JOIN bill b ON(b.bill_no = ssm.bill_no) " +
			" WHERE ssm.type='S' AND b.visit_id = ? AND b.status = 'A' ";

	public List<BasicDynaBean> getSaleBillCharges(String visitId) {
		// TODO Auto-generated method stub
		return DatabaseHelper.queryToDynaList(GET_SALE_BILL_CHARGES, new Object[]{visitId});
	}

	private static final String SALE_ITEM_CLAIM_AGG_TAX_DETAILS = "SELECT sctd.sale_item_id,sctd.item_subgroup_id,COALESCE(SUM(sctd.tax_amt),0) tax_amt FROM bill b "+
      " JOIN store_sales_main ssm ON (ssm.bill_no = b.bill_no) "+ 
      " JOIN bill_charge bc ON (bc.charge_id = ssm.charge_id AND bc.bill_no = b.bill_no) "+ 
      " JOIN store_sales_details ssd ON (ssd.sale_id = ssm.sale_id) "+  
      " JOIN sales_claim_details scd ON (scd.sale_item_id = ssd.sale_item_id) "+  
      " JOIN sales_claim_tax_details sctd ON (sctd.sale_item_id = scd.sale_item_id AND sctd.claim_id = scd.claim_id) "+ 
      " WHERE b.visit_id = ? AND b.status = 'A' AND sctd.adj_amt = 'Y' AND ssm.type='S' "+ 
      " GROUP BY sctd.sale_item_id,item_subgroup_id";
	
  public List<BasicDynaBean> getSaleItemClaimAggTaxDetails(String patientId) {
    // TODO Auto-generated method stub
    return DatabaseHelper.queryToDynaList(SALE_ITEM_CLAIM_AGG_TAX_DETAILS, new Object[]{patientId});
  }

  private static final String SALE_ITEM_TAX_AGG_DETAILS = "SELECT sstd.sale_item_id, COALESCE(SUM(sstd.tax_amt),0) tax FROM bill b "+
      " JOIN store_sales_main ssm ON (ssm.bill_no = b.bill_no) "+ 
      " JOIN bill_charge bc ON (bc.charge_id = ssm.charge_id AND bc.bill_no = b.bill_no) "+ 
      " JOIN store_sales_details ssd ON (ssd.sale_id = ssm.sale_id) "+  
      " JOIN store_sales_tax_details sstd ON (sstd.sale_item_id = ssd.sale_item_id) "+ 
      " WHERE b.visit_id = ? AND b.status = 'A' AND ssm.type='S' "+ 
      " GROUP BY sstd.sale_item_id";
  
  public List<BasicDynaBean> getSaleItemTaxAggDetails(String patientId) {
    // TODO Auto-generated method stub
    return DatabaseHelper.queryToDynaList(SALE_ITEM_TAX_AGG_DETAILS, new Object[]{patientId});
  }

  private static final String SALE_ITEM_TAX_DETAILS = " SELECT ssm.charge_id, sstd.item_subgroup_id AS tax_sub_group_id," +
      " MAX(sstd.tax_rate) AS tax_rate, "+
      " sum(sstd.tax_amt) AS tax_amount, sum(sstd.original_tax_amt) AS original_tax_amt "+
      " FROM bill b "+
      " JOIN store_sales_main ssm ON (ssm.bill_no = b.bill_no) "+
      " JOIN bill_charge bc ON (bc.charge_id = ssm.charge_id AND bc.bill_no = ssm.bill_no) "+
      " JOIN store_sales_details ssd ON (ssd.sale_id = ssm.sale_id) "+
      " JOIN store_sales_tax_details sstd ON (sstd.sale_item_id = ssd.sale_item_id) "+
      " WHERE b.visit_id = ? AND b.status = 'A' AND ssm.type='S' "+
      " GROUP BY ssm.charge_id,ssm.sale_id,sstd.item_subgroup_id";
  public List<BasicDynaBean> getSaleItemTaxDetails(String patientId) {
    // TODO Auto-generated method stub
    return DatabaseHelper.queryToDynaList(SALE_ITEM_TAX_DETAILS, new Object[]{patientId});
  }


}
