package com.insta.hms.core.billing;

import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author anandpatel
 *
 */
@Service
public class NewBillService {

  @LazyAutowired
  private BillService billService;
  private BillModel bill;

  /**
   * create bill now or bill later and cash or insurance bill.
   * @param isInsurance the isInsurance
   * @param visitId the visitId
   * @param billType the billType
   * @return map
   */
  public Map<String, Object> createBill(boolean isInsurance, String visitId, String billType) {
    return billService.createBill(isInsurance, visitId, billType);

  }

  /**
   * get all visit Details for a patient on basis of visit type.
   * @param mrNo the mrNo
   * @param visitType the visitType
   * @return map
   */
  public Map<String, Object> getAllVisitDetails(String mrNo, String visitType) {
    return billService.getAllVisitDetails(mrNo, visitType);
  }

}
