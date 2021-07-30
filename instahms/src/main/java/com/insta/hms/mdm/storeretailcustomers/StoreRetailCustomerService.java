package com.insta.hms.mdm.storeretailcustomers;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class StoreRetailCustomerService.
 * 
 * @author tejaschaudhari
 *
 */
@Service
public class StoreRetailCustomerService {

  @LazyAutowired
  private StoreRetailCustomersRepository storeRetailCustRepo;

  /**
   * Get store retail customers details.
   * @param customerId The customer Id.
   * @return List.
   */
  public List<BasicDynaBean> getRetailCustomerDetails(List<String> customerId) {
    return storeRetailCustRepo.storeRetailCustomersDetails(customerId);
  }
}
