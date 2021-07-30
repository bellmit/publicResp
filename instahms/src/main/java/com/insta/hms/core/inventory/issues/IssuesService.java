package com.insta.hms.core.inventory.issues;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FtlProcessor;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.inventory.stockmgmt.StockRepository;
import com.insta.hms.core.inventory.stockmgmt.StockService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.mdm.item.StoreItemDetailsRepository;
import com.insta.hms.mdm.storeitembatchdetails.StoreItemBatchDetailsService;
import com.insta.hms.mdm.storeitemissuerates.StoreItemIssueRateRepository;
import com.insta.hms.mdm.storeitemrates.StoreItemRatesRepository;
import com.insta.hms.mdm.stores.StoreService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class IssuesService.
 */
@Service
public class IssuesService {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(IssuesService.class);

  /** The Stock Service. */
  @LazyAutowired
  StockService stockService;

  /** The Stock Repository. */
  @LazyAutowired
  StockRepository stockRepository;

  /** The Store Service. */
  @LazyAutowired
  StoreService storeService;

  /** The Store Item repository. */
  @LazyAutowired
  StoreItemRatesRepository storeItemRatesRepository;

  /** The store item batch details service. */
  @LazyAutowired
  StoreItemBatchDetailsService storeItemBatchDetailsService;

  /** The patient registration service. */
  @LazyAutowired
  RegistrationService patientRegistrationService;

  /** The generic preferences service. */
  @LazyAutowired
  GenericPreferencesService genericPreferencesService;

  /** The store item issue rate repository. */
  @LazyAutowired
  StoreItemIssueRateRepository storeItemIssueRateRepository;

  /** The ftl processor. */
  @LazyAutowired
  FtlProcessor ftlProcessor;

  /** The store item details repository. */
  @LazyAutowired
  StoreItemDetailsRepository storeItemDetailsRepository;

  /** The Constant MEDICINE_ID. */
  private static final String MEDICINE_ID = "medicine_id";

  /**
   * Resolve issue price.
   *
   * @param reqMap the req map
   * @return the basic dyna bean
   */
  public BasicDynaBean resolveIssuePrice(Map<String, Object> reqMap) {

    BasicDynaBean storeItemBatchBean = toItemBatchDetailsBean(reqMap);
    BasicDynaBean patientBean = toPatientRegistrationBean(reqMap);

    BigDecimal sellingPrice = null;
    BigDecimal qty = new BigDecimal(
        (reqMap.get("qty") != null ? reqMap.get("qty") : "0").toString());

    String discountStr = (String) reqMap.get("discount");
    String billNo = (String) reqMap.get("bill_no");
    BigDecimal discount = discountStr != null ? new BigDecimal(discountStr) : BigDecimal.ZERO;

    sellingPrice = getItemIssuePrice(patientBean, storeItemBatchBean,
        Integer.parseInt((String) reqMap.get("storeId")), qty, discount, billNo);

    if (sellingPrice == null) {
      sellingPrice = BigDecimal.ZERO;
    }

    storeItemBatchBean.set("mrp", sellingPrice);
    return storeItemBatchBean;
  }

  /**
   * To item batch details bean.
   *
   * @param reqMap
   *          the req map
   * @return the basic dyna bean
   */
  public BasicDynaBean toItemBatchDetailsBean(Map<String, Object> reqMap) {
    BasicDynaBean storeItemBatchDetailsBean = storeItemBatchDetailsService
        .findByPk(Collections.singletonMap("item_batch_id", reqMap.get("item_batch_id")));
    ConversionUtils.copyToDynaBean(reqMap, storeItemBatchDetailsBean, null, true);
    return storeItemBatchDetailsBean;
  }

  /**
   * To patient registration bean.
   *
   * @param reqMap
   *          the req map
   * @return the basic dyna bean
   */
  public BasicDynaBean toPatientRegistrationBean(Map<String, Object> reqMap) {
    BasicDynaBean patientBean = patientRegistrationService
        .getPatientBeanWithBedTypeValue((String) reqMap.get("patient_id"));
    ConversionUtils.copyToDynaBean(reqMap, patientBean, null, true);
    return patientBean;
  }

  /**
   * Gets the item issue price.
   *
   * @param patientBean          the patient bean
   * @param storeItemBatchDetailsBean          the store item batch details bean
   * @param storeId          the store id
   * @param qty          the qty
   * @param discount          the discount
   * @param billNo the bill no
   * @return the item issue price
   */
  public BigDecimal getItemIssuePrice(BasicDynaBean patientBean,
      BasicDynaBean storeItemBatchDetailsBean, Integer storeId, BigDecimal qty, BigDecimal discount,
      String billNo) {

    BigDecimal issueRate = BigDecimal.ZERO;
    BigDecimal expressionMrp = null;
    BigDecimal batchMrp = null;
    String expression = null;
    Integer medicineId = null;
    Integer itemBatchId = null;
    if (storeItemBatchDetailsBean != null) {
      medicineId = (Integer) storeItemBatchDetailsBean.get(MEDICINE_ID);
      itemBatchId = (Integer) storeItemBatchDetailsBean.get("item_batch_id");
      batchMrp = storeItemBatchDetailsBean.get("mrp") != null
          ? (BigDecimal) storeItemBatchDetailsBean.get("mrp") : BigDecimal.ZERO;
    }

    int centerId = (int) patientBean.get("center_id");
    Map<String, Object> results = new HashMap<>();

    BasicDynaBean itemExpressionBean = stockRepository.getSellingPriceExpr(medicineId, itemBatchId,
        billNo, storeId);
    if (itemExpressionBean != null) {

      expressionMrp = itemExpressionBean.get("mrp") != null
          ? (BigDecimal) itemExpressionBean.get("mrp") : BigDecimal.ZERO;

      if (itemExpressionBean.get("issue_rate_master_expr") != null) {

        expression = (String) itemExpressionBean.get("issue_rate_master_expr");

      } else if (itemExpressionBean.get("visit_rate_plan_expr") != null) {
        expression = (String) itemExpressionBean.get("visit_rate_plan_expr");

      } else if (itemExpressionBean.get("use_batch_mrp") != null
          && itemExpressionBean.get("use_batch_mrp").equals("Y")
          && itemExpressionBean.get("mrp") != null) {

        expression = expressionMrp.toString();

      } else if (itemExpressionBean.get("store_rate_plan_expr") != null) {

        expression = (String) itemExpressionBean.get("store_rate_plan_expr");

      } else if (itemExpressionBean.get("item_selling_price") != null) {
        expression = (String) itemExpressionBean.get("item_selling_price");
      } else {
        expression = expressionMrp.toString();
      }

      BasicDynaBean itemBean = storeItemDetailsRepository
          .findByPk(Collections.singletonMap("medicine_id", medicineId));
      if (itemBean != null && itemBean.get("item_selling_price") != null) {
        results.put("mrp", itemBean.get("item_selling_price"));
      } else {
        results.put("mrp", batchMrp);
      }
      results.put("bed_type", (String) patientBean.get("bed_type"));
      results.put("discount", discount);
      results.put("center_id", centerId);
      results.put("store_id", storeId);
      BasicDynaBean packageCPDetails = getPackageCPDetails(storeId, itemBatchId);
      if (packageCPDetails != null) {
        results.putAll(packageCPDetails.getMap());
      }
    }

    if (!StringUtils.isEmpty(expression)) {
      String expressionValueStr = ftlProcessor.process(expression, results);
      if (!StringUtils.isEmpty(expressionValueStr)) {
        issueRate = new BigDecimal(expressionValueStr);
      }
    }
    return issueRate;
  }

  /**
   * Gets the package CP details.
   *
   * @param storeId
   *          the store id
   * @param itemBatchId
   *          the item batch id
   * @return the package CP details
   */
  public BasicDynaBean getPackageCPDetails(Integer storeId, Integer itemBatchId) {
    List<BasicDynaBean> packageCpList = stockRepository.getPackageCPDetails(storeId, itemBatchId);
    if (packageCpList != null && packageCpList.size() > 0) {
      return packageCpList.get(0);
    } else {
      return null;
    }
  }
}
