package com.insta.hms.mdm.storeretailcustomers;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The StoreRetailCustomers Repository.
 * 
 * @author tejaschaudhari
 *
 */
@Repository
public class StoreRetailCustomersRepository extends GenericRepository {

  public StoreRetailCustomersRepository() {
    super("store_retail_customers");
  }

  private static final String GET_STORE_RETAIL_CUSTOMERS = "SELECT * FROM "
      + "store_retail_customers WHERE customer_id IN (:customerid)";

  /**
   * Get Store Retail Customers Details.
   * @param retailCustomerId The list of retail customer Ids.
   * @return List
   */
  public List<BasicDynaBean> storeRetailCustomersDetails(List<String> retailCustomerId) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("customerid", retailCustomerId);
    List<BasicDynaBean> listDetails = DatabaseHelper.queryToDynaList(
        GET_STORE_RETAIL_CUSTOMERS, parameters);
    return listDetails;
  }

}
