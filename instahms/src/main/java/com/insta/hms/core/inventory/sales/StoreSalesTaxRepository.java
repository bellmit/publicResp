package com.insta.hms.core.inventory.sales;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StoreSalesTaxRepository extends GenericRepository{

  public StoreSalesTaxRepository() {
    super("store_sales_tax_details");
    // TODO Auto-generated constructor stub
  }
  
  private static final String GET_ITEM_SUBGROUP_CODES = "SELECT sstd.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name,"+
      " sstd.tax_amt, sstd.tax_rate, isg.item_group_id, ig.group_code, isg.integration_subgroup_id  "+
      " FROM store_sales_tax_details sstd "+
      " JOIN item_sub_groups isg ON(sstd.item_subgroup_id = isg.item_subgroup_id) "+
      " JOIN item_groups ig ON(isg.item_group_id = ig.item_group_id) "+
      " WHERE sstd.sale_item_id = ? ";
  
  public List<BasicDynaBean> getItemSubgroupCodes(int saleItemId) {
    return DatabaseHelper.queryToDynaList(GET_ITEM_SUBGROUP_CODES, new Object[]{saleItemId});
  }

}
