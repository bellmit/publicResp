package com.insta.hms.mdm.orderkit;

import com.insta.hms.mdm.MasterDetailsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class OrderkitService.
 *
 * @author irshadmohammed
 */
@Service("orderkitService")
public class OrderkitService extends MasterDetailsService {

  /**
   * Instantiates a new orderkit service.
   *
   * @param repository
   *          the repository
   * @param validator
   *          the validator
   * @param detailsRepository
   *          the detailsRepository
   */
  public OrderkitService(OrderkitRepository repository, OrderkitValidator validator,
      OrderkitDetailsRepository detailsRepository) {
    super(repository, validator, detailsRepository);
  }

  public List<BasicDynaBean> getOrderKitItemsDetails(int orderKitId) {
    return ((OrderkitRepository) getRepository()).getOrderkitItemsDetails(orderKitId);
  }

  public List<BasicDynaBean> getNonIssuableItems(int orderKitId) {
    return ((OrderkitRepository) getRepository()).getNonIssuableItems(orderKitId);
  }

  /**
   * Gets the orderkit items stock status.
   *
   * @param deptId the dept id
   * @param orderKitId the order kit id
   * @param issueType the issue type
   * @return the orderkit items stock status
   */
  public Map<Integer, String> getOrderkitItemsStockStatus(int deptId, int orderKitId,
      String[] issueType,boolean includeZeroStock) {
    Map<Integer, String> stockStatusMap = new HashMap<>();
    List<BasicDynaBean> medicineInStockStatus = ((OrderkitRepository) getRepository())
        .getOrderKitItemsStockStatus(deptId, orderKitId, issueType, includeZeroStock);
    Iterator<BasicDynaBean> medicineInStockStatusIterator = medicineInStockStatus.iterator();
    while (medicineInStockStatusIterator.hasNext()) {
      if (!medicineInStockStatus.isEmpty()) {
        BasicDynaBean inStockBean = medicineInStockStatusIterator.next();
        BigDecimal inStockQty = (BigDecimal) inStockBean.get("in_stock_qty");
        BigDecimal qtyNeeded = (BigDecimal) inStockBean.get("qty_needed");
        int medicineId = (Integer) inStockBean.get("medicine_id");
        String lowStockStatus = inStockQty + "@" + qtyNeeded;
        stockStatusMap.put(medicineId, lowStockStatus);
      }
    }
    return stockStatusMap;
  }

  public Map<Integer, String> getOrderkitItemsStockStatus(int deptId, int orderKitId,
      String[] issueType) {
    return getOrderkitItemsStockStatus(deptId, orderKitId, issueType, true);
  }
}