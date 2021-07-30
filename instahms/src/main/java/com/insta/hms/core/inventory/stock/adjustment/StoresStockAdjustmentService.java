package com.insta.hms.core.inventory.stock.adjustment;

import au.com.bytecode.opencsv.CSVReader;

import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BusinessService;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.core.inventory.stocks.StockFifoService;
import com.insta.hms.core.inventory.stocks.StoreStockDetailsService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.integration.scm.inventory.ScmOutBoundInvService;
import com.insta.hms.mdm.integration.CsvImportable;
import com.insta.hms.mdm.integration.item.StoreItemDetailIntegrationService;
import com.insta.hms.mdm.stockadjustmentreason.StockAdjustmentReasonService;
import com.insta.hms.mdm.storeitembatchdetails.StoreItemBatchDetailsService;
import com.insta.hms.mdm.stores.StoreService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Stores Stock Adjustment Service.
 * @author anandpatel
 *
 */
@Service
public class StoresStockAdjustmentService extends BusinessService implements CsvImportable {

  /** The logger. */
  private static final Logger logger = LoggerFactory.getLogger(StoresStockAdjustmentService.class);

  @LazyAutowired
  private StoreAdjustmentMainRepository storeAdjMainRepository;
  @LazyAutowired
  private StoreAdjustmentDetailsRepository storeAdjDetailsRepository; 
  @LazyAutowired
  private StoreService storeService;
  @LazyAutowired
  private StockAdjustmentReasonService stockAdjustmentReasonService;
  @LazyAutowired
  private StoreItemBatchDetailsService storeItemBatchDetailsService;
  @LazyAutowired
  private ScmOutBoundInvService scmOutService;
  @LazyAutowired
  private StoreItemDetailIntegrationService storeItemDetailsIntegrationService;  
  @LazyAutowired
  private StockFifoService stockFifoService;
  @LazyAutowired
  private StoreStockDetailsService storeStockDetailsService;
  @LazyAutowired
  private SecurityService securityService;
  
  
  /** The missing headers key. */
  private static final String MISSING_HEADERS_KEY = "exception.csv.missing.headers";

  /** The non printable headers key. */
  private static final String NON_PRINTABLE_HEADERS_KEY = "exception.csv.non.printable.characters";

  /** The non comma delimiter key. */
  private static final String NON_COMMA_DELIMITER_KEY = "exception.csv.non.comma.seperators";

  private static final String UNKNOWN_HEADER_KEY = "exception.csv.unknown.header";
  private static final String STOCK_NOT_AVlBLE = "exception.inventory.issues.stock.not.available";

  private static final String MEDICINE_ID = "medicine_id";
  private static final String STORE_ID = "store_id";
  private static final String INTEGRATION_ADJ_ID = "integration_adj_id";
  private static final String EXT_TRANSC_ID = "external_transaction_id";
  private static final String ADJUSTMENT_REASON = "adjustment_reason";
  private static final String ADJ_DATE = "adj_date";
  private static final String DATE_TIME = "date_time";
  private static final String ITEM_BATCH_ID = "item_batch_id";
  private static final String STATUS = "status";
  private static final String ITEM_ID = "item_id";
  private static final String ITEM_BATCH_NO = "item_batch_no";
  private static final String BATCH_ID = "batch_id";
  private static final String QUANTITY = "qty";
  private static final String ADJ_TYPE = "type";
  private static final String BATCH_NO = "batch_no";
  private static final String ITEM_LOT_ID = "item_lot_id";
  private static final String USER_NAME = "username";
  private static final String ADJ_REMARK = "adj_remark";
  private static final String DESCRIPTION = "description";

  private static final String[] COLUMNS = new String[] { INTEGRATION_ADJ_ID, EXT_TRANSC_ID,
      "store_name", "adj_reason", ADJ_DATE, ITEM_ID, ITEM_BATCH_NO, "adj_type", "adj_qty",
      ADJ_REMARK };
  
  private final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");


  @Override
  public String importCsv(InputStreamReader csvStreamReader,
      Map<String, MultiValueMap<Object, Object>> feedback) throws IOException {
    MultiValueMap<Object, Object> warnings = new LinkedMultiValueMap<>();

    CSVReader csvReader = new CSVReader(csvStreamReader);
    String[] headers = csvReader.readNext();

    if (headers.length < 1) {
      return MISSING_HEADERS_KEY;
    }

    if (!headers[0].matches("\\p{Print}*")) {
      return NON_PRINTABLE_HEADERS_KEY;
    }

    if (headers.length == 1) {
      return NON_COMMA_DELIMITER_KEY;
    }

    boolean[] ignoreColumn = new boolean[headers.length];
    Integer lineNumber = 0;
    Map<String, Map> adjustments = new LinkedHashMap<>();
    for (int i = 0; i < headers.length; i++) {
      if (ArrayUtils.contains(COLUMNS, headers[i])) {
        ignoreColumn[i] = false;

      } else {
        ignoreColumn[i] = true;
        addWarning(warnings, lineNumber, UNKNOWN_HEADER_KEY);
      }
    }
    lineNumber++;
    String[] row;
    Map<String, Boolean> duplicateItemMap = new HashMap<>();
    while (null != (row = csvReader.readNext())) {
      lineNumber++;
      boolean rowFailed = false;
      Map<String, Object> adjustment = new HashMap<>();
      adjustment.put(USER_NAME, Constants.API_USERNAME);
      if (row.length != headers.length) {
        continue;
      }

      for (int i = 0; i < row.length; i++) {
        if (ignoreColumn[i]) {
          continue;
        }
        switch (headers[i]) {
          case INTEGRATION_ADJ_ID:
            adjustment.put(INTEGRATION_ADJ_ID, row[i]);
            break;
          case EXT_TRANSC_ID:
            adjustment.put(EXT_TRANSC_ID, row[i]);
            break;
          case "store_name":
            BasicDynaBean storeBean = storeService.findByUniqueName(row[i], "dept_name");
            if (storeBean == null) {
              addWarning(warnings, lineNumber, "Invalid store name");
              rowFailed = true;
              break;
            }
            adjustment.put(STORE_ID, storeBean.get("dept_id").toString());
            adjustment.put("store_name", row[i]);
            break;
          case "adj_reason":
            Map<String, Object> filterMap = new HashMap<>();
            filterMap.put(ADJUSTMENT_REASON, row[i]);
            filterMap.put(STATUS, "A");
            BasicDynaBean reasonBean = stockAdjustmentReasonService.findByKey(filterMap);
            if (reasonBean == null) {
              filterMap.put(ADJUSTMENT_REASON, "others");
              reasonBean = stockAdjustmentReasonService.findByKey(filterMap);
              if (reasonBean == null) {
                addWarning(warnings, lineNumber, "Invalid Reason");
                rowFailed = true;
                break;
              } else {
                adjustment.put("reason", reasonBean.get(ADJUSTMENT_REASON));
              }
            } else {
              adjustment.put("reason", reasonBean.get(ADJUSTMENT_REASON));
            }
            break;
          case ADJ_DATE:
            try {
              Date adjDate = dateFormat.parse(row[i]);
              adjustment.put(DATE_TIME, adjDate);
            } catch (ParseException ex) {
              logger.debug("Failed to parse date", ex);
              addWarning(warnings, lineNumber, "Invalid adj_date date " + row[i]);
              rowFailed = true;
            }
            break;
          case ITEM_ID:
            BasicDynaBean itemBean = storeItemDetailsIntegrationService.findByIntegrationId(row[i]);
            if (itemBean == null) {
              adjustment.put(ITEM_ID, row[i]);
              addWarning(warnings, lineNumber, "Invalid item id ");
              rowFailed = true;
            } else {
              adjustment.put(ITEM_ID, row[i]);
              adjustment.put(MEDICINE_ID, itemBean.get(MEDICINE_ID).toString());
            }
            break;
          case ITEM_BATCH_NO:
            adjustment.put(BATCH_ID, row[i]);
            if (row[i].isEmpty()) {
              adjustment.put(ITEM_BATCH_NO, "---");
            } else {
              adjustment.put(ITEM_BATCH_NO, row[i]);   
            }
            break;
          case "adj_type":
            if ("R".equalsIgnoreCase(row[i]) || "A".equalsIgnoreCase(row[i])) {
              adjustment.put(ADJ_TYPE, row[i]);
            } else {
              logger.debug("valid value for adj_type is A or R");
              addWarning(warnings, lineNumber, "Invalid value for adj_type " + row[i]);
              rowFailed = true;
            }
            break;
          case "adj_qty":
            try {
              BigDecimal qty = new BigDecimal(row[i]);
              adjustment.put(QUANTITY, qty);
              if (qty.compareTo(BigDecimal.ZERO) == 0) {
                logger.debug("Please provide adj_qty non zero value");
                addWarning(warnings, lineNumber, "Please provide adj_qty non zero value " + row[i]);
                rowFailed = true;
              }
            } catch (NumberFormatException nfe) {
              logger.debug("Failed to parse adj_qty ", nfe);
              addWarning(warnings, lineNumber, "Invalid value for adj_qty " + row[i]);
              rowFailed = true;
            }
            break;
          default:
            adjustment.put(headers[i], row[i]);
            break;
        }
      }
      if (checkDuplicate(adjustment, duplicateItemMap)) {
        logger.debug("Duplicate adjustment  entry for item having item_id : "
            + adjustment.get(ITEM_ID) + ", item_batch_no : " + adjustment.get(BATCH_ID)
            + " for integration_adj_id : " + adjustment.get(INTEGRATION_ADJ_ID));
        addWarning(warnings, lineNumber,
            "Duplicate adjustment  entry for item having item_id : " + adjustment.get(ITEM_ID)
                + ", item_batch_no : " + adjustment.get(BATCH_ID) + " for integration_adj_id : "
                + adjustment.get(INTEGRATION_ADJ_ID));
        rowFailed = true;
      }
      if (!rowFailed) {
        try {
          if (adjustments.keySet().contains(adjustment.get(INTEGRATION_ADJ_ID))) {
            Map existingGrn = adjustments.get(adjustment.get(INTEGRATION_ADJ_ID));
            ((List<Map<String, Object>>) existingGrn.get("medicines"))
                .addAll(getMedicine(adjustment, lineNumber));
          } else {
            List<Map> medicines = new ArrayList<>();
            medicines.addAll(getMedicine(adjustment, lineNumber));
            adjustment.put("medicines", medicines);
            adjustments.put((String) adjustment.get(INTEGRATION_ADJ_ID), adjustment);
          }

        } catch (HMSException ex) {
          addWarning(warnings, lineNumber, ex.getMessage());
        }
      }
    }
    for (String grnNo : adjustments.keySet()) {
      create(adjustments.get(grnNo), warnings);
    }

    feedback.put("warnings", warnings);
    return null;
  }
  
  private List<Map<String, Object>> getMedicine(Map<String, Object> adjustment, int lineNumber) {
    List<Map<String, Object>> itemList = new ArrayList<>();

    HashMap<String, Object> itemBatchKeys = new HashMap<>();

    int medicineId = Integer.parseInt((String) adjustment.get(MEDICINE_ID));
    int storeId = Integer.parseInt((String) adjustment.get(STORE_ID));
    String batchNo = (String) adjustment.get(ITEM_BATCH_NO);
    String batchNoFromCsv = (String) adjustment.get(BATCH_ID);
    itemBatchKeys.put(BATCH_NO, batchNo);
    itemBatchKeys.put(MEDICINE_ID, medicineId);
    BasicDynaBean batchDetailsBean = storeItemBatchDetailsService.findByKey(itemBatchKeys);

    if (batchDetailsBean == null) {
      throw new HMSException("exception.csv.item.batch.error",
          new String[] { (String) adjustment.get(ITEM_ID), batchNoFromCsv });
    }

    int itemBatchId = (int) batchDetailsBean.get(ITEM_BATCH_ID);
    BigDecimal quantity = (BigDecimal) adjustment.get(QUANTITY);
    Boolean qtyAvailable = true;
    if ("R".equalsIgnoreCase((String) adjustment.get(ADJ_TYPE))) {
      qtyAvailable = storeStockDetailsService.isQuantityAvailable(storeId, medicineId, itemBatchId,
          quantity);
    }
    if (!qtyAvailable) {
      throw new HMSException(STOCK_NOT_AVlBLE,
          new String[] { (String) adjustment.get(ITEM_ID) });
    }

    List<BasicDynaBean> batchList = storeStockDetailsService.getItemQtyForBatch(medicineId, storeId,
        batchNo);
    if (batchList.isEmpty()) {
      throw new HMSException("exception.csv.store.item.batch.error",
          new String[] { (String) adjustment.get(ITEM_ID), batchNoFromCsv,
              (String) adjustment.get("store_name") });
    }
    
    if ("R".equalsIgnoreCase((String) adjustment.get(ADJ_TYPE))) {
      for (BasicDynaBean bean : batchList) {
        Map<String, Object> medicine = new HashMap<>();
        medicine.put(MEDICINE_ID, medicineId);
        medicine.put(ITEM_ID, adjustment.get(ITEM_ID));
        medicine.put(ADJ_TYPE, adjustment.get(ADJ_TYPE));
        medicine.put(DESCRIPTION, adjustment.get(DESCRIPTION));
        medicine.put(BATCH_NO, batchNo);
        medicine.put("line_no", lineNumber);
        medicine.put(ITEM_LOT_ID, bean.get(ITEM_LOT_ID));
        medicine.put(ITEM_BATCH_ID, itemBatchId);
        BigDecimal availableQty = (BigDecimal) bean.get("qty");

        if (availableQty.compareTo(quantity) <= 0) {
          medicine.put(QUANTITY, availableQty);
        } else {
          medicine.put(QUANTITY, quantity);
        }
        formDescriptionMsg(medicine);
        itemList.add(medicine);
        quantity = quantity.subtract(availableQty);
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
          break;
        }
      }
    } else {
      BasicDynaBean bean = batchList.get(0);
      Map<String, Object> medicine = new HashMap<>();
      medicine.put(MEDICINE_ID, medicineId);
      medicine.put(ITEM_ID, adjustment.get(ITEM_ID));
      medicine.put(ADJ_TYPE, adjustment.get(ADJ_TYPE));
      medicine.put(DESCRIPTION, adjustment.get(DESCRIPTION));
      medicine.put(BATCH_NO, batchNo);
      medicine.put("line_no", lineNumber);
      medicine.put(ITEM_LOT_ID, bean.get(ITEM_LOT_ID));
      medicine.put(ITEM_BATCH_ID, itemBatchId);
      medicine.put(QUANTITY, quantity);
      formDescriptionMsg(medicine);
      itemList.add(medicine);
    }
    if (itemList.isEmpty()) {
      throw new HMSException("exception.csv.item.batch.error",
          new String[] { (String) adjustment.get(ITEM_ID), batchNoFromCsv });
    }

    return itemList;
  }

  private void addWarning(MultiValueMap<Object, Object> warnings, Integer lineNumber,
      String message) {
    warnings.add(lineNumber, message);
  }

  
  /**
   * stock adjustment transactional method.
   * @param params the params
   * @param warnings the warnings
   */
  @Transactional
  public void create(Map<String, Object> params, MultiValueMap<Object, Object> warnings) {
    List<Map<String, Object>> cacheAdjTxns = new ArrayList<>();
    List<String> module = securityService.getActivatedModules();
    boolean modScmActive = false;
    if (module.contains("mod_scm")) {
      modScmActive = true;
    }
    String storeId = (String) params.get(STORE_ID);
    int storeIdNum = -1;

    try {

      boolean success = true;
      String infomsg = "";
      int adjNo = -1;
      if ((storeId != null) && (storeId.length() > 0)) {
        storeIdNum = Integer.parseInt(storeId);
      }

      List<Map<String, Object>> medicines = (List<Map<String, Object>>) params.get("medicines");
      BasicDynaBean adjMainBean = null;
      if (!medicines.isEmpty()) {
        adjMainBean = storeAdjMainRepository.getBean();
        ConversionUtils.copyToDynaBean(params, adjMainBean);
        adjNo = DataBaseUtil.getNextSequence("stockadjust_sequence");
        adjMainBean.set("adj_no", adjNo);
        if (null == adjMainBean.get(DATE_TIME)) {
          Date date = new Date();
          java.sql.Timestamp dt = new java.sql.Timestamp(date.getTime());
          adjMainBean.set(DATE_TIME, dt);
        }
        adjMainBean.set(STORE_ID, storeIdNum);
        success = storeAdjMainRepository.insert(adjMainBean) > 0;
      }

      int lineNo = 1;
      for (Map<String, Object> medicine : medicines) {
        Map<String, Object> statusMap = null;
        try {
          // adjustment details
          BasicDynaBean adjBean = storeAdjDetailsRepository.getBean();
          ConversionUtils.copyToDynaBean(medicine, adjBean);
          lineNo = (int) medicine.get("line_no");
          int adjDetailNo = DataBaseUtil.getNextSequence("store_adj_details_seq");
          adjBean.set("adj_no", adjNo);
          adjBean.set("adj_detail_no", adjDetailNo);

          if ("A".equalsIgnoreCase((String)medicine.get(ADJ_TYPE))) {
            statusMap = stockFifoService.addStockByLot(storeIdNum,
                (int) medicine.get(ITEM_LOT_ID), "A", (BigDecimal) medicine.get("qty"),
                (String) params.get(USER_NAME), "StockAdjust", adjDetailNo,
                (String) medicine.get(ADJ_REMARK));
            success = (Boolean) statusMap.get(STATUS);
            adjBean.set("cost_value", ((BigDecimal) statusMap.get("costValue")).negate());
          } else {
            statusMap = stockFifoService.reduceStockByLot(storeIdNum,
                (int) medicine.get(ITEM_LOT_ID), "A", (BigDecimal) medicine.get("qty"),
                (String) params.get(USER_NAME), "StockAdjust", adjDetailNo,
                (String) medicine.get(ADJ_REMARK));
            success = (Boolean) statusMap.get(STATUS);
            adjBean.set("cost_value", statusMap.get("costValue"));

            if (!success) {
              infomsg = "Stock can not be decreased to negative for few items.";
              addWarning(warnings, lineNo, infomsg);
              continue;
            }
          }

          success = storeAdjDetailsRepository.insert(adjBean) > 0;
          if (!success) {
            break;
          }

          // Enable this code and chek for some key to identify the scm inbound entry or
          // application entry then cache for out bound
          // if (modScmActive && success) {
          // cacheStockAdj(adjMainBean, adjBean, cacheAdjTxns);
          // }

        } catch (HMSException ex) {
          addWarning(warnings, lineNo, ex.getMessage());
        } catch (RuntimeException re) {
          addWarning(warnings, lineNo, "Exception ocuured while transaction ," + re.getMessage());
        }
      }

    } catch (SQLException ex) {
      addWarning(warnings, 2, "stockadjust_sequence is not cretaed ," + ex.getMessage());
    } finally {
      if (!cacheAdjTxns.isEmpty() && modScmActive) {
        scmOutService.scheduleStockAdjTxns(cacheAdjTxns);
      }

      // update stock timestamp
      stockFifoService.updateStockTimeStamp();
      stockFifoService.updateStoresStockTimeStamp(storeIdNum);
    }
  }
  
  private void cacheStockAdj(BasicDynaBean adjMain, BasicDynaBean adjDetails,
      List<Map<String, Object>> cacheAdjTxns) {
    Map<String, Object> data = scmOutService.getStockAdjMap(adjMain, adjDetails);
    if (!data.isEmpty()) {
      cacheAdjTxns.add(data);
    }
  }
  
  private void formDescriptionMsg(Map<String, Object> medicine) {
    String data = "R".equalsIgnoreCase((String) medicine.get(ADJ_TYPE)) ? "Decrease" : "Increase";
    String description = data + " By " + medicine.get(QUANTITY);
    medicine.put(DESCRIPTION, description);
  }
  
  private boolean checkDuplicate(Map<String, Object> adj, Map<String, Boolean> duplicateItemMap) {
    String integrationId = (String) adj.get(INTEGRATION_ADJ_ID);
    String itemId = (String) adj.get(ITEM_ID);
    String batchNo = (String) adj.get(ITEM_BATCH_NO);
    if (duplicateItemMap.keySet()
        .contains(integrationId.trim() + "_" + itemId.trim() + "_" + batchNo.trim())) {
      return true;
    } else {
      duplicateItemMap.put(integrationId.trim() + "_" + itemId.trim() + "_" + batchNo.trim(),
          Boolean.TRUE);
      return false;
    }
  }
  
}
