package com.insta.hms.integration.scm.inventory;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.RequestContext;
import com.insta.hms.batchjob.CsvExportJob;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.billing.BillActivityChargeService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.billing.BillChargeTaxService;
import com.insta.hms.core.inventory.sales.StoreSalesTaxRepository;
import com.insta.hms.jobs.JobService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.item.StoreItemDetailsService;
import com.insta.hms.mdm.storeitembatchdetails.StoreItemBatchDetailsRepository;
import com.insta.hms.mdm.stores.StoreService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.testng.log4testng.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class ScmOutBoundInvService.
 *
 * <p>SCM Adapter for {@link CsvExportJob} for sales, issues, sales returns, issue returns, stock
 * transfers, stock consumptions and indents (non-supplier transactions).
 */
@Service
public class ScmOutBoundInvService {

  /**
   * The logger.
   */
  static Logger logger = Logger.getLogger(ScmOutBoundInvService.class);

  /**
   * The Constant SOURCE.
   */
  private static final String SOURCE = "INSTA HMS";

  private SimpleDateFormat expDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
  private SimpleDateFormat transDateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

  /**
   * The redis template.
   */
  @LazyAutowired
  private RedisTemplate<String, Object> redisTemplate;

  /**
   * The store item service.
   */
  @LazyAutowired
  private StoreItemDetailsService storeItemService;

  /**
   * The store service.
   */
  @LazyAutowired
  private StoreService storeService;

  /**
   * The center service.
   */
  @LazyAutowired
  private CenterService centerService;

  /**
   * The store sales tax repo.
   */
  @LazyAutowired
  private StoreSalesTaxRepository storeSalesTaxRepo;

  /**
   * The bill activity charge.
   */
  @LazyAutowired
  private BillActivityChargeService billActivityCharge;

  /**
   * The bill charge tax.
   */
  @LazyAutowired
  private BillChargeTaxService billChargeTax;

  /**
   * The bill charge service.
   */
  @LazyAutowired
  private BillChargeService billChargeService;

  /**
   * The stbd repo.
   */
  @LazyAutowired
  private StoreItemBatchDetailsRepository stbdRepo;

  /**
   * The job service.
   */
  @LazyAutowired
  private JobService jobService;

  /**
   * The Enum TransactionType.
   */
  enum TransactionType {

    /**
     * The sale.
     */
    SALE,
    /**
     * The sale return.
     */
    SALE_RETURN,
    /**
     * The issue.
     */
    ISSUE,
    /**
     * The issue return.
     */
    ISSUE_RETURN,
    /**
     * The stock transfer.
     */
    STOCK_TRANSFER,
    /**
     * The stock adjust.
     */
    STOCK_ADJUST,
    /**
     * The stock consume.
     */
    STOCK_CONSUME,
    /**
     * The stock transfer indent.
     */
    TRANSFER_INDENT;
  }

  /**
   * Gets the sale map.
   *
   * @param saleMain    the sale main
   * @param saleDetails the sale details
   * @param visitId     the visit id
   * @param mrNo        the mr no
   * @param centerId    the center id
   * @param billFlag    the bill flag
   * @return the sale map
   */
  public Map<String, Object> getSaleMap(BasicDynaBean saleMain, BasicDynaBean saleDetails,
                                        String visitId, String mrNo, Integer centerId,
                                        Boolean billFlag) {

    Map<String, Object> jobData = new HashMap<>();
    jobData.put("TRANSACTION_ID", saleMain.get("bill_no"));
    Date transDate = (Date) saleMain.get("sale_date");
    jobData.put("TRANSACTION_DATE", transDateFormat.format(transDate));
    jobData.put("TRANSACTION_DATE_TIME", saleMain.get("date_time"));
    jobData.put("USER_NAME", saleMain.get("username"));
    jobData.put("SOURCE_APPLICATION", SOURCE);
    jobData.put("TRANSACTION_ITEM_ID", saleDetails.get("sale_item_id"));
    jobData.put("txn_item_id", saleDetails.get("sale_item_id"));

    if (saleMain.get("store_id") != null) {
      BasicDynaBean storeBean = storeService.findByStore((Integer) saleMain.get("store_id"));
      jobData.put("SOURCE_STORE_NAME", storeBean.get("dept_name"));
      jobData.put("SOURCE_STORE_CODE", saleMain.get("store_id"));
    }
    if (centerId != null && centerId != 0) {
      BasicDynaBean centerBean = centerService.findByKey(centerId);
      jobData.put("SOURCE_CENTER_NAME", centerBean.get("center_name"));
      jobData.put("SOURCE_CENTER_CODE", centerId);
    }
    jobData.put("MRN", mrNo);
    jobData.put("VISIT_ID", visitId);

    Map<String, Object> itemParams = new HashMap<>();
    itemParams.put("medicine_id", saleDetails.get("medicine_id"));
    BasicDynaBean itemDetails = storeItemService.findByPk(itemParams);
    jobData.put("ITEM_NAME", itemDetails.get("medicine_name"));
    jobData.put("ITEM_INTERFACE_CODE", itemDetails.get("cust_item_code"));
    jobData.put("PKG_SIZE", saleDetails.get("package_unit"));
    jobData.put("BATCH_NUMBER", saleDetails.get("batch_no"));
    Date expDate = (Date) saleDetails.get("expiry_date");
    jobData.put("BATCH_EXPIRY_DATE", expDateFormat.format(expDate));
    jobData.put("MRP", saleDetails.get("orig_rate"));
    jobData.put("ITEM_COST", saleDetails.get("cost_value"));
    jobData.put("TRANSACTION_QUANTITY", saleDetails.get("quantity"));
    jobData.put("DISCOUNT", saleDetails.get("disc"));
    jobData.put("DISCOUNT_PERCENT", saleDetails.get("discount_per"));
    jobData.put("AMOUNT", saleDetails.get("amount"));
    jobData.put("TRANSACTION_UOM", saleDetails.get("sale_unit"));

    if (billFlag) {
      jobData.put("BILL_DISCOUNT", saleMain.get("discount"));
      jobData.put("ROUND_OFF", saleMain.get("round_off"));
    }

    return jobData;
  }

  /**
   * Gets the issue map.
   *
   * @param issueMain    the issue main
   * @param issueDetails the issue details
   * @param patientBean  the patient bean
   * @param billNo       the bill no
   * @param centerId     the center id
   * @param billCharge   the bill charge
   * @return the issue map
   */
  public Map<String, Object> getIssueMap(BasicDynaBean issueMain, BasicDynaBean issueDetails,
                                         Map patientBean, String billNo, Integer centerId,
                                         BasicDynaBean billCharge) {
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("TRANSACTION_ID", billNo);
    jobData.put("SOURCE_APPLICATION", SOURCE);
    Date transDate = (Date) issueMain.get("date_time");
    jobData.put("TRANSACTION_DATE", transDateFormat.format(transDate));
    jobData.put("TRANSACTION_DATE_TIME", issueMain.get("date_time"));
    jobData.put("USER_NAME", issueMain.get("username"));
    jobData.put("TRANSACTION_ITEM_ID", issueDetails.get("item_issue_no"));
    jobData.put("txn_item_id", issueDetails.get("item_issue_no"));

    if (issueMain.get("dept_from") != null) {
      BasicDynaBean storeBean = storeService.findByStore((Integer) issueMain.get("dept_from"));
      jobData.put("SOURCE_STORE_NAME", storeBean.get("dept_name"));
      jobData.put("SOURCE_STORE_CODE", issueMain.get("dept_from"));
    }

    if (centerId != null && centerId != -1) {
      BasicDynaBean centerBean = centerService.findByKey(centerId);
      jobData.put("SOURCE_CENTER_NAME", centerBean.get("center_name"));
      jobData.put("SOURCE_CENTER_CODE", centerId);
    }

    jobData.put("MRN", patientBean.get("mr_no"));
    jobData.put("VISIT_ID", patientBean.get("patient_id"));

    Map<String, Object> itemParams = new HashMap<>();
    itemParams.put("medicine_id", issueDetails.get("medicine_id"));
    BasicDynaBean stBean = stbdRepo.findByKey("item_batch_id",
        issueDetails.get("item_batch_id"));
    BasicDynaBean itemDetails = storeItemService.findByPk(itemParams);
    jobData.put("ITEM_NAME", itemDetails.get("medicine_name"));
    jobData.put("ITEM_INTERFACE_CODE", itemDetails.get("cust_item_code"));
    jobData.put("PKG_SIZE", issueDetails.get("pkg_size"));
    jobData.put("BATCH_NUMBER", issueDetails.get("batch_no"));
    Date expDate = (Date) stBean.get("exp_dt");
    jobData.put("BATCH_EXPIRY_DATE", expDateFormat.format(expDate));
    jobData.put("MRP", issueDetails.get("pkg_mrp"));
    jobData.put("ITEM_COST", issueDetails.get("cost_value"));
    jobData.put("TRANSACTION_QUANTITY", issueDetails.get("qty"));
    jobData.put("DISCOUNT", billCharge.get("discount"));
    jobData.put("AMOUNT", billCharge.get("amount"));
    jobData.put("TRANSACTION_UOM", issueDetails.get("item_unit"));

    return jobData;
  }

  /**
   * Gets the issue returns map.
   *
   * @param issueMain    the issue main
   * @param issueDetails the issue details
   * @param patientBean  the patient bean
   * @param billNo       the bill no
   * @param centerId     the center id
   * @param billCharge   the bill charge
   * @return the issue returns map
   */
  public Map<String, Object> getIssueReturnsMap(BasicDynaBean issueMain, BasicDynaBean issueDetails,
                                                Map patientBean, String billNo, Integer centerId,
                                                BasicDynaBean billCharge) {
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("TRANSACTION_ID", billNo);
    jobData.put("SOURCE_APPLICATION", SOURCE);
    Date transDate = (Date) issueMain.get("date_time");
    jobData.put("TRANSACTION_DATE", transDateFormat.format(transDate));
    jobData.put("TRANSACTION_DATE_TIME", issueMain.get("date_time"));
    jobData.put("USER_NAME", issueMain.get("username"));
    jobData.put("TRANSACTION_ITEM_ID", issueDetails.get("item_return_no"));
    jobData.put("txn_item_id", issueDetails.get("item_return_no"));

    if (issueMain.get("dept_to") != null) {
      BasicDynaBean storeBean = storeService.findByStore((Integer) issueMain.get("dept_to"));
      jobData.put("SOURCE_STORE_NAME", storeBean.get("dept_name"));
      jobData.put("SOURCE_STORE_CODE", issueMain.get("dept_to"));
    }

    if (centerId != null && centerId != -1) {
      BasicDynaBean centerBean = centerService.findByKey(centerId);
      jobData.put("SOURCE_CENTER_NAME", centerBean.get("center_name"));
      jobData.put("SOURCE_CENTER_CODE", centerId);
    }

    jobData.put("MRN", patientBean.get("mr_no"));
    jobData.put("VISIT_ID", patientBean.get("patient_id"));

    Map<String, Object> itemParams = new HashMap<>();
    itemParams.put("medicine_id", issueDetails.get("medicine_id"));
    BasicDynaBean stBean = stbdRepo.findByKey("item_batch_id",
        issueDetails.get("item_batch_id"));
    BasicDynaBean itemDetails = storeItemService.findByPk(itemParams);
    jobData.put("ITEM_NAME", itemDetails.get("medicine_name"));
    jobData.put("ITEM_INTERFACE_CODE", itemDetails.get("cust_item_code"));
    jobData.put("PKG_SIZE", issueDetails.get("rtn_pkg_size"));
    jobData.put("BATCH_NUMBER", issueDetails.get("batch_no"));
    Date expDate = (Date) stBean.get("exp_dt");
    jobData.put("BATCH_EXPIRY_DATE", expDateFormat.format(expDate));
    jobData.put("ITEM_COST", issueDetails.get("cost_value"));
    jobData.put("TRANSACTION_QUANTITY", issueDetails.get("qty"));
    jobData.put("DISCOUNT", billCharge.get("discount"));
    jobData.put("AMOUNT", billCharge.get("amount"));
    jobData.put("TRANSACTION_UOM", issueDetails.get("item_unit"));

    return jobData;
  }

  /**
   * Schedule sale txns.
   *
   * @param txnList   the txn list
   * @param isReturns the is returns
   */
  public void scheduleSaleTxns(List<Map<String, Object>> txnList, Boolean isReturns) {
    for (Map<String, Object> txn : txnList) {
      StringBuilder taxCode = new StringBuilder();
      StringBuilder taxAmount = new StringBuilder();
      List<BasicDynaBean> taxBeans = storeSalesTaxRepo
          .getItemSubgroupCodes((Integer) txn.get("txn_item_id"));
      Iterator<BasicDynaBean> taxBeanIterator = taxBeans.iterator();
      while (taxBeanIterator.hasNext()) {
        BasicDynaBean taxBean = taxBeanIterator.next();
        if (taxBean.get("integration_subgroup_id") != null) {
          taxCode.append(taxBean.get("integration_subgroup_id").toString());
          if (taxBeanIterator.hasNext()) {
            taxCode.append(",");
          }
        }
        taxAmount.append(taxBean.get("tax_amt").toString());
        if (taxBeanIterator.hasNext()) {
          taxAmount.append(",");
        }
      }
      txn.put("TAX_CODE", taxCode.toString());
      txn.put("TAX_AMOUNT", taxAmount.toString());
      if (isReturns) {
        txn.put("TRANSACTION_TYPE", TransactionType.SALE_RETURN);
      } else {
        txn.put("TRANSACTION_TYPE", TransactionType.SALE);
      }
    }
    if (isReturns) {
      scheduleTxnExport(txnList, TransactionType.SALE_RETURN);
    } else {
      scheduleTxnExport(txnList, TransactionType.SALE);
    }
  }

  /**
   * Schedule issue txns.
   *
   * @param txnList the txn list
   */
  public void scheduleIssueTxns(List<Map<String, Object>> txnList) {
    for (Map<String, Object> txn : txnList) {
      Map<String, Object> activityParams = new HashMap<>();
      activityParams.put("activity_id", txn.get("txn_item_id").toString());
      BasicDynaBean activityCharge = billActivityCharge.findByKey(activityParams);
      StringBuilder taxCode = new StringBuilder();
      StringBuilder taxAmount = new StringBuilder();
      if (activityCharge != null) {
        List<BasicDynaBean> taxBeans = billChargeTax
            .getItemSubgroupCodes(activityCharge.get("charge_id").toString());
        Iterator<BasicDynaBean> taxBeansIterator = taxBeans.iterator();
        while (taxBeansIterator.hasNext()) {
          BasicDynaBean taxBean = taxBeansIterator.next();

          if (taxBean.get("integration_subgroup_id") != null) {
            taxCode.append(taxBean.get("integration_subgroup_id").toString());
            if (taxBeansIterator.hasNext()) {
              taxCode.append(",");
            }
          }

          taxAmount.append(taxBean.get("tax_amount").toString());
          if (taxBeansIterator.hasNext()) {
            taxAmount.append(",");
          }
        }
      }
      txn.put("TAX_AMOUNT", taxAmount.toString());
      txn.put("TAX_CODE", taxCode.toString());
      txn.put("TRANSACTION_TYPE", TransactionType.ISSUE);
    }
    scheduleTxnExport(txnList, TransactionType.ISSUE);
  }

  /**
   * Schedule issue return txns.
   *
   * @param txnList the txn list
   */
  public void scheduleIssueReturnTxns(List<Map<String, Object>> txnList) {
    for (Map<String, Object> txn : txnList) {
      Map<String, Object> activityParams = new HashMap<>();
      activityParams.put("activity_id", txn.get("txn_item_id").toString());
      BasicDynaBean activityCharge = billActivityCharge.findByKey(activityParams);
      StringBuilder taxCode = new StringBuilder();
      StringBuilder taxAmount = new StringBuilder();
      if (activityCharge != null) {
        List<BasicDynaBean> taxBeans = billChargeTax
            .getItemSubgroupCodes(activityCharge.get("charge_id").toString());
        Iterator<BasicDynaBean> taxBeansIterator = taxBeans.iterator();
        while (taxBeansIterator.hasNext()) {
          BasicDynaBean taxBean = taxBeansIterator.next();
          if (taxBean.get("integration_subgroup_id") != null) {
            taxCode.append(taxBean.get("integration_subgroup_id").toString());
            if (taxBeansIterator.hasNext()) {
              taxCode.append(",");
            }
          }
          taxAmount.append(taxBean.get("tax_amount").toString());
          if (taxBeansIterator.hasNext()) {
            taxAmount.append(",");
          }
        }
      }
      txn.put("TAX_AMOUNT", taxAmount.toString());
      txn.put("TAX_CODE", taxCode.toString());
      txn.put("TRANSACTION_TYPE", TransactionType.ISSUE_RETURN);
    }
    scheduleTxnExport(txnList, TransactionType.ISSUE_RETURN);
  }

  /**
   * Schedule stock transfer txns.
   *
   * @param txnList the txn list
   */
  public void scheduleStockTransferTxns(List<Map<String, Object>> txnList) {
    for (Map<String, Object> txn : txnList) {
      txn.put("TRANSACTION_TYPE", TransactionType.STOCK_TRANSFER);
    }
    scheduleTxnExport(txnList, TransactionType.STOCK_TRANSFER);
  }

  /**
   * Schedule stock transfer indent approval.
   *
   * @param txnList the txn list
   */
  public void scheduleStockTransferIndentTxns(List<Map<String, Object>> txnList) {
    for (Map<String, Object> txn : txnList) {
      txn.put("TRANSACTION_TYPE", TransactionType.TRANSFER_INDENT);
    }
    scheduleTxnExport(txnList, TransactionType.TRANSFER_INDENT);
  }

  /**
   * Schedule stock adj txns.
   *
   * @param txnList the txn list
   */
  public void scheduleStockAdjTxns(List<Map<String, Object>> txnList) {
    for (Map<String, Object> txn : txnList) {
      txn.put("TRANSACTION_TYPE", TransactionType.STOCK_ADJUST);
    }
    scheduleTxnExport(txnList, TransactionType.STOCK_ADJUST);
  }

  /**
   * Schedule txn export.
   *
   * @param transactions the transactions
   * @param type         the type
   */
  public void scheduleTxnExport(List<Map<String, Object>> transactions, TransactionType type) {
    Map<String, Object> jobData = new HashMap<String, Object>();
    Date date = new Date();
    jobData.put("schema", RequestContext.getSchema());
    jobData.put("eventId", type.toString() + "_" + date.getTime());
    jobData.put("eventData", transactions);
    jobData.put("ttl", 10);

    jobService
        .scheduleImmediate(buildJob((String) jobData.get("eventId"), CsvExportJob.class, jobData));
  }

  /**
   * Gets the stock transfer map.
   *
   * @param transferMain    the transfer main
   * @param transferDetails the transfer details
   * @return the stock transfer map
   */
  public Map<String, Object> getStockTransferMap(BasicDynaBean transferMain,
                                                 BasicDynaBean transferDetails) {
    Map<String, Object> jobData = new HashMap<String, Object>();
    jobData.put("TRANSACTION_ID", transferMain.get("transfer_no"));
    jobData.put("SOURCE_APPLICATION", SOURCE);
    Date transDate = (Date) transferMain.get("date_time");
    jobData.put("TRANSACTION_DATE", transDateFormat.format(transDate));
    jobData.put("TRANSACTION_DATE_TIME", transferMain.get("date_time"));
    jobData.put("USER_NAME", transferMain.get("username"));
    jobData.put("TRANSACTION_ITEM_ID", transferDetails.get("transfer_detail_no"));
    jobData.put("txn_item_id", transferDetails.get("transfer_detail_no"));

    if (transferMain.get("store_from") != null) {
      BasicDynaBean storeBean = storeService.findByStore((Integer) transferMain.get("store_from"));
      jobData.put("SOURCE_STORE_NAME", storeBean.get("dept_name"));
      jobData.put("SOURCE_STORE_CODE", transferMain.get("store_from"));
      Integer centerId = (Integer) storeBean.get("center_id");
      BasicDynaBean centerBean = centerService.findByKey(centerId);
      jobData.put("SOURCE_CENTER_NAME", centerBean.get("center_name"));
      jobData.put("SOURCE_CENTER_CODE", centerId);
    }

    if (transferMain.get("store_to") != null) {
      BasicDynaBean storeBean = storeService.findByStore((Integer) transferMain.get("store_to"));
      jobData.put("DESTINATION_STORE_NAME", storeBean.get("dept_name"));
      jobData.put("DESTINATION_STORE_CODE", transferMain.get("store_to"));
      Integer centerId = (Integer) storeBean.get("center_id");
      BasicDynaBean centerBean = centerService.findByKey(centerId);
      jobData.put("DESTINATION_CENTER_NAME", centerBean.get("center_name"));
      jobData.put("DESTINATION_CENTER_CODE", centerId);
    }

    Map<String, Object> itemParams = new HashMap<>();
    itemParams.put("medicine_id", transferDetails.get("medicine_id"));
    BasicDynaBean stBean = stbdRepo.findByKey("item_batch_id",
        transferDetails.get("item_batch_id"));
    BasicDynaBean itemDetails = storeItemService.findByPk(itemParams);
    jobData.put("ITEM_NAME", itemDetails.get("medicine_name"));
    jobData.put("ITEM_INTERFACE_CODE", itemDetails.get("cust_item_code"));
    jobData.put("PKG_SIZE", transferDetails.get("trn_pkg_size"));
    jobData.put("BATCH_NUMBER", stBean.get("batch_no"));
    Date expDate = (Date) stBean.get("exp_dt");
    jobData.put("BATCH_EXPIRY_DATE", expDateFormat.format(expDate));
    jobData.put("ITEM_COST", transferDetails.get("cost_value"));
    jobData.put("TRANSACTION_QUANTITY", transferDetails.get("qty"));
    jobData.put("TRANSACTION_UOM", transferDetails.get("item_unit"));

    return jobData;
  }

  /**
   * Gets the stock adj map.
   *
   * @param adjMain    the adj main
   * @param adjDetails the adj details
   * @return the stock adj map
   */
  public Map<String, Object> getStockAdjMap(BasicDynaBean adjMain, BasicDynaBean adjDetails) {
    Map<String, Object> jobData = new HashMap<String, Object>();
    jobData.put("TRANSACTION_ID", adjMain.get("adj_no").toString());
    jobData.put("SOURCE_APPLICATION", SOURCE);
    Date transDate = (Date) adjMain.get("date_time");
    jobData.put("TRANSACTION_DATE", transDateFormat.format(transDate));
    jobData.put("TRANSACTION_DATE_TIME", adjMain.get("date_time"));
    jobData.put("USER_NAME", adjMain.get("username"));
    jobData.put("TRANSACTION_ITEM_ID", adjDetails.get("adj_detail_no"));
    jobData.put("txn_item_id", adjDetails.get("adj_detail_no"));

    if (adjMain.get("store_id") != null) {
      BasicDynaBean storeBean = storeService.findByStore((Integer) adjMain.get("store_id"));
      jobData.put("SOURCE_STORE_NAME", storeBean.get("dept_name"));
      jobData.put("SOURCE_STORE_CODE", adjMain.get("store_id"));
      Integer centerId = (Integer) storeBean.get("center_id");
      BasicDynaBean centerBean = centerService.findByKey(centerId);
      jobData.put("SOURCE_CENTER_NAME", centerBean.get("center_name"));
      jobData.put("SOURCE_CENTER_CODE", centerId);
    }

    Map<String, Object> itemParams = new HashMap<>();
    itemParams.put("medicine_id", adjDetails.get("medicine_id"));
    BasicDynaBean stBean = stbdRepo.findByKey("item_batch_id",
        adjDetails.get("item_batch_id"));
    BasicDynaBean itemDetails = storeItemService.findByPk(itemParams);
    jobData.put("ITEM_NAME", itemDetails.get("medicine_name"));
    jobData.put("ITEM_INTERFACE_CODE", itemDetails.get("cust_item_code"));
    jobData.put("PKG_SIZE", itemDetails.get("issue_base_unit"));
    jobData.put("BATCH_NUMBER", stBean.get("batch_no"));
    Date expDate = (Date) stBean.get("exp_dt");
    jobData.put("BATCH_EXPIRY_DATE", expDateFormat.format(expDate));
    jobData.put("ITEM_COST", adjDetails.get("cost_value"));
    jobData.put("TRANSACTION_QUANTITY", adjDetails.get("qty"));
    jobData.put("ADJUSTMENT_TYPE", adjDetails.get("type"));
    jobData.put("TRANSACTION_UOM", "I");

    return jobData;
  }

  /**
   * Gets the stock consume map.
   *
   * @param main          the main
   * @param details       the details
   * @param consumptionId the consumption id
   * @return the stock consume map
   */
  public Map<String, Object> getStockConsumeMap(BasicDynaBean main, BasicDynaBean details,
                                                String consumptionId) {
    Map<String, Object> jobData = new HashMap<String, Object>();
    jobData.put("TRANSACTION_ID", consumptionId);
    jobData.put("SOURCE_APPLICATION", SOURCE);
    Date transDate = (Date) main.get("date_time");
    jobData.put("TRANSACTION_DATE", transDateFormat.format(transDate));
    jobData.put("TRANSACTION_DATE_TIME", main.get("date_time"));
    jobData.put("USER_NAME", main.get("user_name"));
    jobData.put("TRANSACTION_ITEM_ID", details.get("reagent_usage_det_id"));
    jobData.put("txn_item_id", consumptionId);

    if (main.get("store_id") != null) {
      BasicDynaBean storeBean = storeService.findByStore((Integer) main.get("store_id"));
      jobData.put("SOURCE_STORE_NAME", storeBean.get("dept_name"));
      jobData.put("SOURCE_STORE_CODE", main.get("store_id"));
      Integer centerId = (Integer) storeBean.get("center_id");
      BasicDynaBean centerBean = centerService.findByKey(centerId);
      jobData.put("SOURCE_CENTER_NAME", centerBean.get("center_name"));
      jobData.put("SOURCE_CENTER_CODE", centerId);
    }

    Map<String, Object> itemParams = new HashMap<>();
    int itemBatchId = (Integer) details.get("item_batch_id");
    BasicDynaBean stBean = stbdRepo.findByKey("item_batch_id", itemBatchId);

    itemParams.put("medicine_id", stBean.get("medicine_id"));
    BasicDynaBean itemDetails = storeItemService.findByPk(itemParams);
    jobData.put("ITEM_NAME", itemDetails.get("medicine_name"));
    jobData.put("ITEM_INTERFACE_CODE", itemDetails.get("cust_item_code"));
    jobData.put("PKG_SIZE", itemDetails.get("issue_base_unit"));
    jobData.put("BATCH_NUMBER", stBean.get("batch_no"));
    Date expDate = (Date) stBean.get("exp_dt");
    jobData.put("BATCH_EXPIRY_DATE", expDateFormat.format(expDate));
    jobData.put("ITEM_COST", details.get("cost_value"));
    jobData.put("TRANSACTION_QUANTITY", details.get("qty"));
    jobData.put("TRANSACTION_UOM", "I");

    return jobData;
  }

  /**
   * get Stock Transfer Indent Map.
   *
   * @param indentMain    the indentMain
   * @param indentDetails the indentDetails
   * @return map
   */
  public Map<String, Object> getStockIndentMap(BasicDynaBean indentMain,
                                               BasicDynaBean indentDetails) {
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("TRANSACTION_ID", indentMain.get("indent_no"));
    jobData.put("SOURCE_APPLICATION", SOURCE);
    Date transDate = (Date) indentMain.get("date_time");
    jobData.put("TRANSACTION_DATE", transDateFormat.format(transDate));
    jobData.put("TRANSACTION_DATE_TIME", indentMain.get("date_time"));

    if (indentMain.get("indent_store") != null) {
      BasicDynaBean storeBean = storeService.findByStore((Integer) indentMain.get("indent_store"));
      jobData.put("SOURCE_STORE_NAME", storeBean.get("dept_name"));
      jobData.put("SOURCE_STORE_CODE", indentMain.get("indent_store"));
      Integer centerId = (Integer) storeBean.get("center_id");
      BasicDynaBean centerBean = centerService.findByKey(centerId);
      jobData.put("SOURCE_CENTER_NAME", centerBean.get("center_name"));
      jobData.put("SOURCE_CENTER_CODE", centerId);
    }

    if (indentMain.get("dept_from") != null) {
      BasicDynaBean storeBean = storeService
          .findByStore(Integer.valueOf((String) indentMain.get("dept_from")));
      jobData.put("DESTINATION_STORE_NAME", storeBean.get("dept_name"));
      jobData.put("DESTINATION_STORE_CODE", indentMain.get("dept_from"));
      Integer centerId = (Integer) storeBean.get("center_id");
      BasicDynaBean centerBean = centerService.findByKey(centerId);
      jobData.put("DESTINATION_CENTER_NAME", centerBean.get("center_name"));
      jobData.put("DESTINATION_CENTER_CODE", centerId);
    }

    Map<String, Object> itemParams = new HashMap<>();
    itemParams.put("medicine_id", indentDetails.get("medicine_id"));
    BasicDynaBean itemDetails = storeItemService.findByPk(itemParams);
    jobData.put("ITEM_NAME", itemDetails.get("medicine_name"));
    jobData.put("ITEM_INTERFACE_CODE", itemDetails.get("cust_item_code"));
    jobData.put("PKG_SIZE", itemDetails.get("issue_base_unit"));
    jobData.put("TRANSACTION_UOM", "I");
    jobData.put("TRANSACTION_QUANTITY", indentDetails.get("qty"));
    jobData.put("REMARKS", indentMain.get("approver_remarks"));
    return jobData;
  }

  /**
   * Schedule stock consume txns.
   *
   * @param txnList the txn list
   */
  public void scheduleStockConsumeTxns(List<Map<String, Object>> txnList) {
    for (Map<String, Object> txn : txnList) {
      txn.put("TRANSACTION_TYPE", TransactionType.STOCK_CONSUME);
    }
    scheduleTxnExport(txnList, TransactionType.STOCK_CONSUME);
  }

}
