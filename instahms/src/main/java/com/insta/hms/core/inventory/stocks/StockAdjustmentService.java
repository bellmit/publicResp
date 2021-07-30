package com.insta.hms.core.inventory.stocks;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.HMSException;
import com.insta.hms.mdm.item.StoreItemDetailsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockAdjustmentService {

  @LazyAutowired
  StockAdjustmentRepository adjustMainRepo;
  
  @LazyAutowired
  StockAdjustmentDetailRepository adjustDetailRepo;
  
  @LazyAutowired
  StockFifoService stockFifoService;
  
  @LazyAutowired
  StoreStockDetailsService stockDetailService;
  
  @LazyAutowired
  StoreItemDetailsService itemService;

  /**
   * Method to post adjustment entries, given a list of items with the
   * quantities to be adjusted.
   * 
   * @param storeId
   *          The store for which the items belong to and the adjustments have
   *          to be posted
   * @param adjustments
   *          List of {@link BasicDynaBean} containing the list of items and
   *          quantities that have to be adjusted. It is expected that all the
   *          properties of the beans in the list should match with the field
   *          names of the store_adj_details table as this method uses
   *          copyBeanToBean to convert the input beans into the data beans
   *          before persisting it to the database.
   * @param adjustmentReason
   *          String reason for adjustment, which will be set in the
   *          store_adj_main table
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Transactional(rollbackFor = Exception.class)
  public void postAdjustments(Integer storeId, List<BasicDynaBean> adjustments,
      String adjustmentReason, String refId) {
    if (null == adjustments || adjustments.isEmpty()) {
      return;
    }

    String username = RequestContext.getUserName();
    BasicDynaBean adjMain = populateMainBean(storeId, username, refId,
        adjustmentReason);
    boolean success = (adjustMainRepo.insert(adjMain)) > 0;

    List<BasicDynaBean> adjustableLots = stockDetailService
        .getAvailableLots(storeId, true, true);
    Map lotMap = ConversionUtils.listBeanToMapListBean(adjustableLots,
        "item_batch_id");
    List<BasicDynaBean> detailBeans = new ArrayList<BasicDynaBean>();
    for (BasicDynaBean adjustment : adjustments) {
      BasicDynaBean adjDetail = populateDetailBean(adjMain, adjustment);

      List<BasicDynaBean> lots = (List<BasicDynaBean>) lotMap
          .get(adjDetail.get("item_batch_id"));

      if (null == lots || lots.isEmpty()) {
        continue;
      }
      Map result = null;
      if ("R".equalsIgnoreCase((String) adjDetail.get("type"))) {
        result = adjustLots(storeId, username, adjDetail, lots);
        if ((Boolean) result.get("status")) {
          adjDetail.set("cost_value", result.get("costValue"));
          detailBeans.add(adjDetail);
        } else {
          Map keys = new HashMap<String, Object>();
          keys.put("medicine_id", adjDetail.get("medicine_id"));
          BasicDynaBean itemBean = itemService.findByPk(keys);
          String[] params = new String[] {
              (String) itemBean.get("medicine_name"),
              (String) adjDetail.get("batch_no") };
          throw new HMSException(HttpStatus.BAD_REQUEST,
              "exception.stock.take.adjustment.insufficient.quantity", params);
        }
      } else {
        BasicDynaBean lot = lots.get(lots.size() - 1); // get the last lot
        result = stockFifoService.addStockByLot(storeId,
            (Integer) lot.get("item_lot_id"), "A",
            (BigDecimal) adjDetail.get("qty"), username, "StockAdjust",
            (Integer) adjDetail.get("adj_detail_no"),
            (String) adjDetail.get("description"));
        if ((Boolean) result.get("status")) {
          adjDetail.set("cost_value", ((BigDecimal)result.get("costValue")).negate());
          detailBeans.add(adjDetail);
        }
      }
    }
    adjustDetailRepo.batchInsert(detailBeans);
  }

  private BasicDynaBean populateDetailBean(BasicDynaBean adjMainBean,
      BasicDynaBean adjustment) {
    BasicDynaBean adjDetail = adjustDetailRepo.getBean();
    Integer adjDetNo = adjustDetailRepo.getNextSequence();
    ConversionUtils.copyBeanToBean(adjustment, adjDetail);
    BigDecimal adjQty = ((BigDecimal) adjDetail.get("qty"));
    adjDetail.set("qty", adjQty.abs());
    if (adjQty.compareTo(BigDecimal.ZERO) < 0) {
      adjDetail.set("type", "R");
    } else {
      adjDetail.set("type", "A");
    }
    adjDetail.set("adj_no", adjMainBean.get("adj_no"));
    adjDetail.set("adj_detail_no", adjDetNo);
    return adjDetail;
  }

  private Map adjustLots(Integer storeId, String username,
      BasicDynaBean adjDetail, List<BasicDynaBean> lots) {

    Map status = null;
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("status", true);
    result.put("costValue", BigDecimal.ZERO);

    BigDecimal remainingQty = (BigDecimal) adjDetail.get("qty");
    for (BasicDynaBean lot : lots) {
      if (((BigDecimal)lot.get("qty")).compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      BigDecimal adjLotQty = remainingQty.min((BigDecimal) lot.get("qty"));
      status = stockFifoService.reduceStockByLot(storeId,
          (Integer) lot.get("item_lot_id"), "A", adjLotQty, username,
          "StockAdjust", (Integer) adjDetail.get("adj_detail_no"),
          (String) adjDetail.get("description"));
      if (processResult(result, status)) {
        remainingQty = remainingQty.subtract(adjLotQty);
        if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
          break;
        }
      }
    }
    if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
      result.put("status", false);
    }
    return result;
  }

  private BasicDynaBean populateMainBean(Integer storeId, String username, String refId,
      String reason) {
    Integer adjNo = adjustMainRepo.getNextSequence();
    BasicDynaBean bean = adjustMainRepo.getBean();
    bean.set("adj_no", adjNo);
    Date date = new Date();
    java.sql.Timestamp dt = new java.sql.Timestamp(date.getTime());
    bean.set("date_time", dt);
    bean.set("store_id", storeId);
    bean.set("username", username);
    bean.set("reason", reason);
    bean.set("stock_take_id", refId);
    return bean;
  }
  
  private boolean processResult(Map<String, Object> result, Map status) {
      if (null != status && status.containsKey("status")
          && (Boolean) status.get("status")) {
        result.put("status",
            ((Boolean) result.get("status") && (Boolean) status.get("status")));
  
        result.put("costValue",
            ((BigDecimal) result.get("costValue"))
                .add(null != status.get("costValue")
                    ? (BigDecimal) status.get("costValue") : BigDecimal.ZERO));
      }
      return (Boolean)status.get("status");
  }
}
