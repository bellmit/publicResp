package com.insta.hms.core.inventory.stock.transfer;

import au.com.bytecode.opencsv.CSVReader;

import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BusinessService;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.core.inventory.stocks.StockFifoService;
import com.insta.hms.core.inventory.stocks.StoreStockDetailsService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.integration.scm.inventory.ScmOutBoundInvService;
import com.insta.hms.mdm.integration.CsvImportable;
import com.insta.hms.mdm.integration.item.StoreItemDetailIntegrationService;
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
 * Stores Stock Transfer Service.
 * @author anandpatel
 *
 */
@Service
public class StoresStockTransferService extends BusinessService implements CsvImportable {

  /** The logger. */
  private static final Logger logger = LoggerFactory.getLogger(StoresStockTransferService.class);

  @LazyAutowired
  private StoreService storeService;
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
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  @LazyAutowired
  private StoreTransferMainRepository storeTransferMainRepository;
  @LazyAutowired
  private StoreTransferDetailsRepository storeTransferDetailsRepository;

  /** The missing headers key. */
  private static final String MISSING_HEADERS_KEY = "exception.csv.missing.headers";

  /** The non printable headers key. */
  private static final String NON_PRINTABLE_HEADERS_KEY = "exception.csv.non.printable.characters";

  /** The non comma delimiter key. */
  private static final String NON_COMMA_DELIMITER_KEY = "exception.csv.non.comma.seperators";

  private static final String UNKNOWN_HEADER_KEY = "exception.csv.unknown.header";

  private static final String STOCK_NOT_AVLBLE = "exception.inventory.issues.stock.not.available";

  private static final String INTEGRATION_TRANSFER_ID = "integration_transfer_id";
  private static final String EXT_TRANSC_ID = "external_transaction_id";
  private static final String STORE_FROM = "store_from";
  private static final String STORE_TO = "store_to";
  private static final String DEPT_ID = "dept_id";
  private static final String TRANSFER_DATE = "transfer_date";
  private static final String DATE_TIME = "date_time";
  private static final String DISALLOW_EXPIRED = "disallow_expired";
  private static final String ITEM_ID = "item_id";
  private static final String MEDICINE_ID = "medicine_id";
  private static final String MEDICINE_NAME = "medicine_name";
  private static final String ITEM_BATCH_ID = "item_batch_id";
  private static final String TRN_PKG_SIZE = "trn_pkg_size";
  private static final String ITEM_UNIT = "item_unit";
  private static final String ITEM_BATCH_NO = "item_batch_no";
  private static final String BATCH_ID = "batch_id";
  private static final String QUANTITY = "qty";
  private static final String BATCH_NO = "batch_no";
  private static final String ITEM_TRANSFER_DESC = "item_transfer_description";
  private static final String DESCRIPTION = "description";
  private static final String STATUS = "status";
  private static final String USER_NAME = "username";

  private static final String[] COLUMNS = new String[] { INTEGRATION_TRANSFER_ID, EXT_TRANSC_ID,
      STORE_FROM, STORE_TO, "transfer_reason", TRANSFER_DATE, DISALLOW_EXPIRED, ITEM_ID,
      ITEM_BATCH_NO, "transfer_qty", ITEM_TRANSFER_DESC };

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
    Map<String, Map> stockTransfers = new LinkedHashMap<>();
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
      Map<String, Object> transfer = new HashMap<>();
      transfer.put(USER_NAME, Constants.API_USERNAME);
      if (row.length != headers.length) {
        continue;
      }

      for (int i = 0; i < row.length; i++) {
        if (ignoreColumn[i]) {
          continue;
        }
        switch (headers[i]) {
          case INTEGRATION_TRANSFER_ID:
            transfer.put(INTEGRATION_TRANSFER_ID, row[i]);
            break;
          case EXT_TRANSC_ID:
            transfer.put(EXT_TRANSC_ID, row[i]);
            break;
          case STORE_FROM:
            BasicDynaBean fromStoreBean = storeService.findByUniqueName(row[i], "dept_name");
            if (fromStoreBean == null) {
              addWarning(warnings, lineNumber, "Invalid from_store name");
              rowFailed = true;
              break;
            }
            transfer.put(STORE_FROM, (int) fromStoreBean.get(DEPT_ID));
            break;
          case STORE_TO:
            BasicDynaBean toStoreBean = storeService.findByUniqueName(row[i], "dept_name");
            if (toStoreBean == null) {
              addWarning(warnings, lineNumber, "Invalid to_store name");
              rowFailed = true;
              break;
            }
            transfer.put(STORE_TO, (int) toStoreBean.get(DEPT_ID));
            break;
          case "transfer_reason":
            if ("".equals(row[i])) {
              addWarning(warnings, lineNumber, "transfer_reason is mandatory");
              rowFailed = true;
              break;
            }
            transfer.put("reason", row[i]);
            break;
          case TRANSFER_DATE:
            try {
              Date transferDate = dateFormat.parse(row[i]);
              transfer.put(DATE_TIME, transferDate);
            } catch (ParseException ex) {
              logger.debug("Failed to parse transfer date", ex);
              addWarning(warnings, lineNumber, "Invalid transfer date " + row[i]);
              rowFailed = true;
            }
            break;
          case DISALLOW_EXPIRED:
            transfer.put(DISALLOW_EXPIRED, row[i]);
            break;
          case ITEM_ID:
            BasicDynaBean itemBean = storeItemDetailsIntegrationService.findByIntegrationId(row[i]);
            if (itemBean == null) {
              transfer.put(ITEM_ID, row[i]);
              addWarning(warnings, lineNumber, "Invalid item id ");
              rowFailed = true;
            } else {
              transfer.put(ITEM_ID, row[i]);
              transfer.put(MEDICINE_ID, itemBean.get(MEDICINE_ID));
              transfer.put(MEDICINE_NAME, itemBean.get(MEDICINE_NAME));
              transfer.put(TRN_PKG_SIZE, itemBean.get("issue_base_unit"));
            }
            break;
          case ITEM_BATCH_NO:
            transfer.put(BATCH_ID, row[i]);
            if (row[i].isEmpty()) {
              transfer.put(ITEM_BATCH_NO, "---");
            } else {
              transfer.put(ITEM_BATCH_NO, row[i]);
            }
            break;
          case "uom_type":
            if ("".equals(row[i]) || !"p".equalsIgnoreCase(row[i])) {
              transfer.put(ITEM_UNIT, "I");
            } else {
              transfer.put(ITEM_UNIT, "P");
            }
            break;
          case "transfer_qty":
            try {
              BigDecimal qty = new BigDecimal(row[i]);
              transfer.put(QUANTITY, qty);
              if (qty.compareTo(BigDecimal.ZERO) == 0) {
                logger.debug("Please provide transfer_qty non zero value");
                addWarning(warnings, lineNumber,
                    "Please provide transfer_qty non zero value " + row[i]);
                rowFailed = true;
              }
            } catch (NumberFormatException nfe) {
              logger.debug("Failed to parse transfer_qty ", nfe);
              addWarning(warnings, lineNumber, "Invalid value for transfer_qty " + row[i]);
              rowFailed = true;
            }
            break;
          case ITEM_TRANSFER_DESC:
            transfer.put(DESCRIPTION, row[i]);
            break;
          default:
            transfer.put(headers[i], row[i]);
            break;
        }
      }
      if (checkDuplicate(transfer, duplicateItemMap)) {
        logger.debug("Duplicate entry for item having item_id : " + transfer.get(ITEM_ID)
            + ", item_batch_no : " + transfer.get(BATCH_ID) + " for integration_transfer_id : "
            + transfer.get(INTEGRATION_TRANSFER_ID));
        addWarning(warnings, lineNumber,
            "Duplicate entry for item having item_id : " + transfer.get(ITEM_ID)
                + ", item_batch_no : " + transfer.get(BATCH_ID) + " for integration_transfer_id : "
                + transfer.get(INTEGRATION_TRANSFER_ID));
        rowFailed = true;
      }

      if (!rowFailed) {
        try {
          if (stockTransfers.keySet().contains(transfer.get(INTEGRATION_TRANSFER_ID))) {
            Map existingGrn = stockTransfers.get(transfer.get(INTEGRATION_TRANSFER_ID));
            ((List<Map<String, Object>>) existingGrn.get("medicines"))
                .add(getMedicine(transfer, lineNumber));
          } else {
            List<Map> medicines = new ArrayList<>();
            medicines.add(getMedicine(transfer, lineNumber));
            transfer.put("medicines", medicines);
            stockTransfers.put((String) transfer.get(INTEGRATION_TRANSFER_ID), transfer);
          }

        } catch (HMSException ex) {
          addWarning(warnings, lineNumber, ex.getMessage());
        }
      }
    }
    for (String transferId : stockTransfers.keySet()) {
      create(stockTransfers.get(transferId), warnings);
    }

    feedback.put("warnings", warnings);
    return null;
  }

  private Map<String, Object> getMedicine(Map<String, Object> transfer, int lineNumber) {

    HashMap<String, Object> itemBatchKeys = new HashMap<>();
    int medicineId = (int) transfer.get(MEDICINE_ID);
    String batchNo = (String) transfer.get(ITEM_BATCH_NO);
    String batchNoFromCsv = (String) transfer.get(BATCH_ID);
    itemBatchKeys.put(BATCH_NO, batchNo);
    itemBatchKeys.put(MEDICINE_ID, medicineId);
    BasicDynaBean batchDetailsBean = storeItemBatchDetailsService.findByKey(itemBatchKeys);
    if (batchDetailsBean == null) {
      throw new HMSException("exception.csv.item.batch.error",
          new String[] { (String) transfer.get(ITEM_ID), batchNoFromCsv });
    }
    
    if ("Y".equalsIgnoreCase((String) transfer.get(DISALLOW_EXPIRED))
        && storeItemBatchDetailsService.isItemExpired(batchNo, medicineId)) {
      throw new HMSException("js.stores.mgmt.itemsavailable.pastexpirydate",
          new String[] { (String) transfer.get(ITEM_ID), batchNoFromCsv });
    }
    BigDecimal quantity = (BigDecimal) transfer.get(QUANTITY);
    String uomType = (String) transfer.get("uom_type");
    BigDecimal pkgSize = (BigDecimal) transfer.get(TRN_PKG_SIZE);
    if ("p".equalsIgnoreCase(uomType)) {
      quantity = quantity.multiply(pkgSize);
    }
    int storeId = (int) transfer.get(STORE_FROM);
    BigDecimal availableQtyInStock = storeStockDetailsService
        .getAvailableItemCountForBatchAndStore(medicineId, storeId, batchNo);

    if (null == availableQtyInStock || availableQtyInStock.compareTo(quantity) < 0) {
      throw new HMSException(STOCK_NOT_AVLBLE,
          new String[] { (String) transfer.get(ITEM_ID), batchNoFromCsv });
    }

    Map<String, Object> medicine = new HashMap<>();
    medicine.put(MEDICINE_ID, medicineId);
    medicine.put(MEDICINE_NAME, transfer.get(MEDICINE_NAME));
    medicine.put(ITEM_ID, transfer.get(ITEM_ID));
    medicine.put(DESCRIPTION, transfer.get(DESCRIPTION));
    medicine.put(QUANTITY, quantity);
    medicine.put(BATCH_NO, batchNo);
    medicine.put(ITEM_UNIT, "P");
    medicine.put("line_no", lineNumber);
    medicine.put(ITEM_BATCH_ID, (Integer) batchDetailsBean.get(ITEM_BATCH_ID));
    medicine.put(TRN_PKG_SIZE, pkgSize);
    return medicine;
  }

  private void addWarning(MultiValueMap<Object, Object> warnings, Integer lineNumber,
      String message) {
    warnings.add(lineNumber, message);
  }

  /**
   * stock transfer transaction method.
   * 
   * @param params
   *          the params
   * @param warnings
   *          the warnings
   */
  @Transactional
  public void create(Map<String, Object> params, MultiValueMap<Object, Object> warnings) {
    boolean success = true;
    try {
      int newTransferId = 0;
      int newTransferDetailId = 0;
      List<Map<String, Object>> cacheTransferTxns = new ArrayList<>();
      String appointmentId = (String) params.get("appointment_id");
      String userName = (String) params.get(USER_NAME);
      BasicDynaBean prefs = genericPreferencesService.getAllPreferences();
      int deptFrom = (int) params.get(STORE_FROM);
      int deptTo = (int) params.get(STORE_TO);
      List<String> module = securityService.getActivatedModules();
      boolean modScmActive = false;
      if (module.contains("mod_scm")) {
        modScmActive = true;
      }

      List<Map<String, Object>> medicines = (List<Map<String, Object>>) params.get("medicines");
      BasicDynaBean stockTransferBean = null;
      if (!medicines.isEmpty()) {
        stockTransferBean = storeTransferMainRepository.getBean();
        ConversionUtils.copyToDynaBean(params, stockTransferBean);
        newTransferId = DataBaseUtil.getNextSequence("stock_transfer_seq");
        stockTransferBean.set("transfer_no", newTransferId);
        stockTransferBean.set("appointment_id", appointmentId != null && !appointmentId.isEmpty()
            ? Integer.parseInt(appointmentId) : null);
        if (null == stockTransferBean.get(DATE_TIME)) {
          Date date = new Date();
          java.sql.Timestamp dt = new java.sql.Timestamp(date.getTime());
          stockTransferBean.set(DATE_TIME, dt);
        }
        success = storeTransferMainRepository.insert(stockTransferBean) > 0;
      }
      int lineNo = 1;
      for (Map<String, Object> medicine : medicines) {
        BigDecimal transferredQty = (BigDecimal) medicine.get(QUANTITY);
        Map<String, Object> statusMap = null;
        try {
          // stock transfer details
          lineNo = (int) medicine.get("line_no");
          BasicDynaBean stockTransferDetailsBean = storeTransferDetailsRepository.getBean();
          ConversionUtils.copyToDynaBean(medicine, stockTransferDetailsBean);
          stockTransferDetailsBean.set("transfer_no", newTransferId);
          newTransferDetailId = DataBaseUtil.getNextSequence("store_transfer_details_seq");
          stockTransferDetailsBean.set("transfer_detail_no", newTransferDetailId);

          /**
           * 1.Reduce stock by FIFO from FROM store. 2.insert into transfer details 3.Add stock to
           *
           */
          if (success) {
            int medicineId = (int) medicine.get(MEDICINE_ID);
            int itemBatchId = (int) medicine.get(ITEM_BATCH_ID);
            HashMap<String, Object> stockKeys = new HashMap<>();
            stockKeys.put(ITEM_BATCH_ID, itemBatchId);
            stockKeys.put(DEPT_ID, deptTo);
            BasicDynaBean stockBean = storeStockDetailsService.findByKey(stockKeys);

            BigDecimal qty = (BigDecimal)medicine.get(QUANTITY);
            if (stockBean != null) {
              statusMap = stockFifoService.reduceStock(deptFrom, itemBatchId, "T", qty, null,
                  userName, "StockTransfer", newTransferDetailId);
              if (!(Boolean) statusMap.get(STATUS)) {
                transferredQty = transferredQty.subtract((BigDecimal) statusMap.get("left_qty"));
                throw new HMSException(STOCK_NOT_AVLBLE,
                    new String[] { (String) medicine.get(ITEM_ID) });
              }

              statusMap = stockFifoService.addStock(deptTo, newTransferDetailId, "T", qty, userName,
                  "StockTransfer", deptFrom);
              success &= (Boolean) statusMap.get(STATUS);
              stockTransferDetailsBean.set("cost_value", statusMap.get("costValue"));

              if (success && ((String) (prefs.get("show_central_excise_duty"))).equals("Y")) {
                success = storeStockDetailsService.updateStock(deptTo, deptFrom,
                    (String) medicine.get(BATCH_NO), medicineId);
              }
            } else {
              statusMap = stockFifoService.transferStock(medicineId, itemBatchId, deptFrom, deptTo,
                  qty, userName, newTransferDetailId, "N");
              stockTransferDetailsBean.set("cost_value", statusMap.get("costValue"));
              if (!(Boolean) statusMap.get(STATUS) && statusMap.get("statusReason") != null) {
                transferredQty = transferredQty.subtract((BigDecimal) statusMap.get("left_qty"));
                throw new HMSException(STOCK_NOT_AVLBLE,
                    new String[] { (String) medicine.get(ITEM_ID) });
              }
            }
          }

          // transfer details
          stockTransferDetailsBean.set("qty", transferredQty);

          success = storeTransferDetailsRepository.insert(stockTransferDetailsBean) > 0;
          if (!success) {
            break;
          }
          
          // Enable this code and chek for some key to identify the scm inbound entry or
          // application entry then cache for out bound

          // if (modScmActive && success) {
          // cacheStockTransfer(stockTransferBean, stockTransferDetailsBean, cacheTransferTxns);
          // }

        } catch (HMSException ex) {
          addWarning(warnings, lineNo, ex.getMessage());
        } catch (RuntimeException re) {
          addWarning(warnings, lineNo, "Exception ocuured while transaction ," + re.getMessage());
        }
      }

      if (success) {
        if (!cacheTransferTxns.isEmpty() && modScmActive) {
          scmOutService.scheduleStockTransferTxns(cacheTransferTxns);
        }
        // update stock timestamp
        stockFifoService.updateStockTimeStamp();
        stockFifoService.updateStoresStockTimeStamp(deptTo);
      }

    } catch (SQLException ex) {
      addWarning(warnings, 2, "stock_transfer_seq is not cretaed ," + ex.getMessage());
    }

  }

  private boolean checkDuplicate(Map<String, Object> transfer,
      Map<String, Boolean> duplicateItemMap) {
    String integrationId = (String) transfer.get(INTEGRATION_TRANSFER_ID);
    String itemId = (String) transfer.get(ITEM_ID);
    String batchNo = (String) transfer.get(ITEM_BATCH_NO);
    if (duplicateItemMap.keySet()
        .contains(integrationId.trim() + "_" + itemId.trim() + "_" + batchNo.trim())) {
      return true;
    } else {
      duplicateItemMap.put(integrationId.trim() + "_" + itemId.trim() + "_" + batchNo.trim(),
          Boolean.TRUE);
      return false;
    }
  }

  private void cacheStockTransfer(BasicDynaBean transferMain, BasicDynaBean transferDetails,
      List<Map<String, Object>> cacheTransferTxns) {
    Map<String, Object> data = scmOutService.getStockTransferMap(transferMain, transferDetails);
    if (!data.isEmpty()) {
      cacheTransferTxns.add(data);
    }

  }

}
