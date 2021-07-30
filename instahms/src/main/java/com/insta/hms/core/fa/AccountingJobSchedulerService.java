package com.insta.hms.core.fa;

import com.insta.hms.core.billing.BillService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class AccountingJobSchedulerService.
 */
@Service
public class AccountingJobSchedulerService {

  /** The bill service. */
  @Autowired
  private BillService billService;

  /**
   * Gets the all pharmacy bills of bill.
   *
   * @param billNo        the bill no
   * @param reversalsOnly the reversals only
   * @return the all pharmacy bills of bill
   */
  public List<Map<String, Object>> getAllPharmacyBillsOfBill(String billNo, boolean reversalsOnly) {
    List<BasicDynaBean> phBillsListBeans = billService.getAllPharmacyBillsOfBill(billNo);
    List<Map<String, Object>> phBillsList = new ArrayList<>();
    if (phBillsListBeans == null) {
      return phBillsList;
    }
    for (BasicDynaBean bean : phBillsListBeans) {
      Map<String, Object> phBill = new HashMap<>();
      phBill.put("phBillNo", (String) bean.get("sale_id"));
      phBill.put("reversalsOnly", reversalsOnly);
      phBillsList.add(phBill);
    }
    return phBillsList;
  }
}
