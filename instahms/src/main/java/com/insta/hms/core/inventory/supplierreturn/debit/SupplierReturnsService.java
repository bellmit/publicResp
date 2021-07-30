package com.insta.hms.core.inventory.supplierreturn.debit;

import au.com.bytecode.opencsv.CSVReader;

import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AutoIdGenerator;
import com.insta.hms.common.BusinessService;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.inventory.StoresHelper;
import com.insta.hms.core.inventory.stockmgmt.StockService;
import com.insta.hms.core.inventory.stocks.StockFifoService;
import com.insta.hms.core.inventory.stocks.StoreGRNDetailsRepository;
import com.insta.hms.core.inventory.stocks.StoreGRNMainRepository;
import com.insta.hms.core.inventory.stocks.StoreGrnTaxDetailsService;
import com.insta.hms.core.inventory.stocks.StoreStockDetailsService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.mdm.integration.CsvImportable;
import com.insta.hms.mdm.integration.item.StoreItemDetailIntegrationService;
import com.insta.hms.mdm.integration.taxsubgroups.TaxSubGroupsIntegrationService;
import com.insta.hms.mdm.storeitembatchdetails.StoreItemBatchDetailsService;
import com.insta.hms.mdm.stores.StoreService;
import com.insta.hms.mdm.supplier.SupplierService;
import com.insta.hms.mdm.taxgroups.TaxGroupService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
 * Supplier Returns Service.
 * @author anandpatel
 *
 */
@Service
public class SupplierReturnsService extends BusinessService implements CsvImportable {

  /** The logger. */
  private static final Logger logger = LoggerFactory.getLogger(SupplierReturnsService.class);

  @LazyAutowired
  private StoreGRNMainRepository storeGrnMainRepository;
  
  @LazyAutowired
  private StoreGRNDetailsRepository storeGrnDetailsRepository;
  
  @LazyAutowired
  private StoreService storeService;
  
  @LazyAutowired
  private StoreItemBatchDetailsService storeItemBatchDetailsService;
  
  @LazyAutowired
  private SupplierService supplierService;
  @LazyAutowired
  private StoreItemDetailIntegrationService storeItemDetailsIntegrationService;

  @LazyAutowired
  private TaxSubGroupsIntegrationService taxSubgroupIntegrationService;
  
  @LazyAutowired
  private StoreGrnTaxDetailsService storeGrnTaxDetailsService;
  
  @LazyAutowired
  private TaxGroupService taxGroupService;
  
  @LazyAutowired
  private StockService stockService;
  
  @LazyAutowired
  private StockFifoService stockFifoService;
  
  @LazyAutowired
  private StoreStockDetailsService storeStockDetailsService;
  
  @LazyAutowired
  private StoreDebitNoteService storeDebitNoteService;
  
  /** The missing headers key. */
  private static final String MISSING_HEADERS_KEY = "exception.csv.missing.headers";

  /** The non printable headers key. */
  private static final String NON_PRINTABLE_HEADERS_KEY = "exception.csv.non.printable.characters";

  /** The non comma delimiter key. */
  private static final String NON_COMMA_DELIMITER_KEY = "exception.csv.non.comma.seperators";

  private static final String UNKNOWN_HEADER_KEY = "exception.csv.unknown.header";
  private static final String STOCK_NOT_AVLBLE = "exception.inventory.issues.stock.not.available";
  
  private static final String ITEM_GROUP_ID = "item_group_id";
  private static final String MEDICINE_ID = "medicine_id";
  private static final String ITEM_BATCH_ID = "item_batch_id";
  private static final String INTEGRATION_GRN_ID = "integration_grn_id";
  private static final String EXT_TRANSC_ID = "external_transaction_id";
  private static final String SUPPLIER_NAME = "supplier_name";
  private static final String GRN_DATE = "grn_date";
  private static final String DUE_DATE = "due_date";
  private static final String EXP_DATE = "exp_dt";
  private static final String GRN_NO = "grn_no";
  private static final String GRN_QTY_UNIT = "grn_qty_unit";
  private static final String STATUS = "status";
  private static final String ITEM_ID = "item_id";
  private static final String ITEM_BATCH_NO = "item_batch_no";
  private static final String BATCH_ID = "batch_id";
  private static final String BONUS_QTY = "bonus_qty";
  private static final String COST_PRICE = "cost_price";
  private static final String USER_NAME = "username";
  private static final String ISSUE_BASE_UNIT = "issue_base_unit";
  private static final String TAX_RATE = "taxrate";
  private static final String TAX_AMOUNT = "taxamount";
  private static final String ITEM_SUB_GROUP_ID = "item_subgroup_id";
  private static final String BILLED_QTY = "billed_qty";
  private static final String DISCOUNT = "discount";
  private static final String ITEM_DISCOUNT = "item_discount";
  private static final String BATCH_NO = "batch_no";
  private static final String BONUS_TAX = "bonus_tax";
  private static final String GRN_PKG_SIZE = "grn_pkg_size";
  private static final String RETURN_TYPE = "return_type";

  private static final String[] COLUMNS = new String[] { INTEGRATION_GRN_ID, EXT_TRANSC_ID,
      "store_name", SUPPLIER_NAME, GRN_DATE, GRN_NO, GRN_QTY_UNIT, RETURN_TYPE, "remarks", STATUS,
      ITEM_ID, ITEM_BATCH_NO, "qty", BONUS_QTY, "mrp", COST_PRICE, EXP_DATE, "tax_sub_groups",
      "tax_amount", DUE_DATE, ITEM_DISCOUNT, "bill_discount", "other_charges",
      "other_charges_remarks" };
   
  private final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
  private final DateFormat expiryDateFormat = new SimpleDateFormat("MM/yy");

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
    Map<String, Map> grns = new LinkedHashMap<>();
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
    while (null != (row = csvReader.readNext())) {
      lineNumber++;
      boolean rowFailed = false;
      Map<String, Object> grn = new HashMap<>();
      grn.put(USER_NAME, Constants.API_USERNAME);
      if (row.length != headers.length) {
        continue;
      }

      for (int i = 0; i < row.length; i++) {
        if (ignoreColumn[i]) {
          continue;
        }
        switch (headers[i]) {
          case INTEGRATION_GRN_ID:
            grn.put(INTEGRATION_GRN_ID, row[i]);
            break;
          case EXT_TRANSC_ID:
            grn.put(EXT_TRANSC_ID, row[i]);
            break;
          case "store_name":
            BasicDynaBean storeBean = storeService.findByUniqueName(row[i], "dept_name");
            if (storeBean == null) {
              addWarning(warnings, lineNumber, "Invalid store name");
              rowFailed = true;
              break;
            }
            grn.put("store_id", storeBean.get("dept_id").toString());
            break;
          case SUPPLIER_NAME:
            BasicDynaBean supplierBean = supplierService.findByUniqueName(row[i], SUPPLIER_NAME);
            if (supplierBean == null) {
              addWarning(warnings, lineNumber, "Invalid supplier");
              rowFailed = true;
              break;
            }
            grn.put("supplier_id", supplierBean.get("supplier_code").toString());
            break;
          case ITEM_ID:
            BasicDynaBean itemBean = storeItemDetailsIntegrationService.findByIntegrationId(row[i]);
            if (itemBean == null) {
              addWarning(warnings, lineNumber, "Invalid item id ");
              rowFailed = true;
            } else {
              grn.put(ITEM_ID, row[i]);
              grn.put(MEDICINE_ID, (int)itemBean.get(MEDICINE_ID));
              grn.put(ISSUE_BASE_UNIT, itemBean.get(ISSUE_BASE_UNIT).toString());
            }
            break;
          case ITEM_BATCH_NO:
            grn.put(BATCH_ID, row[i]);
            if (row[i].isEmpty()) {
              grn.put(ITEM_BATCH_NO, "---");
            } else {
              grn.put(ITEM_BATCH_NO, row[i]); 
            }
            break;
          case GRN_DATE:
            try {
              Date grnDate = dateFormat.parse(row[i]);
              grn.put(GRN_DATE, grnDate);
            } catch (ParseException ex) {
              logger.debug("Failed to parse date", ex);
              addWarning(warnings, lineNumber, "Invalid grn date " + row[i]);
              rowFailed = true;
            }
            break;
  
          case DUE_DATE:
            try {
              Date grnDate = dateFormat.parse(row[i]);
              grn.put(DUE_DATE, grnDate);
            } catch (ParseException ex) {
              logger.debug("Failed to parse date", ex);
              addWarning(warnings, lineNumber, "Invalid due date " + row[i]);
              rowFailed = true;
            }
            break;
  
          case GRN_QTY_UNIT:
            if (row[i].equals("P") || row[i].equals("I")) {
              grn.put(GRN_QTY_UNIT, row[i]);
            } else {
              addWarning(warnings, lineNumber, "Invalid value for grn_qty_unit " + row[i]);
              rowFailed = true;
            }
            break;
  
          case EXP_DATE:
            try {
              grn.put(EXP_DATE, expiryDateFormat.parse(row[i]));
            } catch (ParseException ex) {
              logger.debug("Invalid expiry date", ex);
              rowFailed = true;
              addWarning(warnings, lineNumber, "Invalid expiry");
            }
            break;
          case "tax_sub_groups":
            int taxIndex = ArrayUtils.indexOf(headers, "tax_amount");
            String[] taxSubgroups = row[i].split(",");
            String[] taxAmounts = row[taxIndex].split(",");
            if (taxSubgroups.length != taxAmounts.length) {
              rowFailed = true;
              addWarning(warnings, lineNumber, "Length of taxSubgroups and taxes should be same");
              break;
            }
            if (StringUtils.isEmpty(taxSubgroups[0])) {
              continue;
            }
            for (int j = 0; j < taxSubgroups.length; j++) {
              BasicDynaBean taxSubgroupBean = taxSubgroupIntegrationService
                  .getTaxSubgroupsDetails(taxSubgroups[j]);
              if (taxSubgroupBean == null) {
                rowFailed = true;
                addWarning(warnings, lineNumber, "Invalid taxSubgroup " + taxSubgroups[j]);
                continue;
              }
              if (!NumberUtils.isParsable(taxAmounts[j])) {
                addWarning(warnings, lineNumber, "Tax is not valid " + taxAmounts[j]);
                rowFailed = true;
                continue;
              }
  
              grn.put(TAX_RATE + taxSubgroupBean.get(ITEM_GROUP_ID),
                  taxSubgroupBean.get("tax_rate").toString());
              grn.put(TAX_AMOUNT + taxSubgroupBean.get(ITEM_GROUP_ID), taxAmounts[j]);
              grn.put("taxsubgroupid" + taxSubgroupBean.get(ITEM_GROUP_ID),
                  taxSubgroupBean.get(ITEM_SUB_GROUP_ID).toString());
  
            }
            break;
          case "qty":
            grn.put(BILLED_QTY, row[i]);
            break;
          case "bill_discount":
            grn.put(DISCOUNT, row[i]);
            grn.put("discount_type", 'A');
            break;
          case STATUS:
            grn.put(STATUS, row[i].charAt(0));
            grn.put("round_off", "0");
            break;
          case RETURN_TYPE:
            if (row[i].equalsIgnoreCase("D") || row[i].equalsIgnoreCase("E")) {
              grn.put(RETURN_TYPE, row[i].equalsIgnoreCase("D") ? 'D' : 'E');
            } else {
              grn.put(RETURN_TYPE, 'O');
              grn.put("other_reason", row[i]);
            }
            break;
          default:
            grn.put(headers[i], row[i]);
            break;
        }
      }
      rowFailed = validateNumricalData(grn,warnings,lineNumber,rowFailed);
      if (!rowFailed) {
        try {
          if (grns.keySet().contains(grn.get(INTEGRATION_GRN_ID))) {
            Map existingGrn = grns.get(grn.get(INTEGRATION_GRN_ID));
            ((List<Map<String, Object>>) existingGrn.get("medicines"))
                .add(getMedicine(grn, lineNumber));
          } else {
            List<Map> medicines = new ArrayList<>();
            medicines.add(getMedicine(grn, lineNumber));
            grn.put("medicines", medicines);
            grns.put((String) grn.get(INTEGRATION_GRN_ID), grn);
          }
        } catch (HMSException ex) {
          addWarning(warnings, lineNumber, ex.getMessage());
        }
      }
    }
    for (String grnNo : grns.keySet()) {
      makeSupplierDebit(grns.get(grnNo), warnings);
    }

    feedback.put("warnings", warnings);
    return null;
  }

  private boolean validateNumricalData(Map<String, Object> grn,
      MultiValueMap<Object, Object> warnings, Integer lineNumber,boolean rowFailed) {
    String billQtyStr = (String) grn.get(BILLED_QTY);
    String bonusQtyStr = (String) grn.get(BONUS_QTY);
    String itemDiscStr = (String) grn.get(ITEM_DISCOUNT);
    BigDecimal billedQty;
    BigDecimal bonusQty;
    BigDecimal itemDisc;
    if ("".equals(itemDiscStr)) {
      grn.put(ITEM_DISCOUNT, 0.0);
    } else {
      try {
        itemDisc = new BigDecimal(itemDiscStr);
        grn.put(ITEM_DISCOUNT, itemDisc);
      } catch (NumberFormatException nfe) {
        logger.debug("Invalid value for item_discount", nfe);
        rowFailed = true;
        addWarning(warnings, lineNumber, "Please provide numerical value for item_discount");
      }
    }
    if ("".equals(billQtyStr) && "".equals(bonusQtyStr)) {
      rowFailed = true;
      addWarning(warnings, lineNumber, "Please provide qty or bonus_qty");
    } else {
      try {
        billedQty = new BigDecimal(billQtyStr);
        bonusQty = new BigDecimal(bonusQtyStr);
        if ((billedQty.compareTo(BigDecimal.ZERO) < 0 || bonusQty.compareTo(BigDecimal.ZERO) < 0)
            || (billedQty.compareTo(BigDecimal.ZERO) == 0
                && bonusQty.compareTo(BigDecimal.ZERO) == 0)) {
          rowFailed = true;
          addWarning(warnings, lineNumber, "Please provide qty or bonus_qty greater than zero");
        }
      } catch (NumberFormatException nfe) {
        logger.debug("Invalid value for qty or bonus_qty", nfe);
        rowFailed = true;
        addWarning(warnings, lineNumber, "Please provide numerical value for qty and bonus_qty");
      }
    }
    
    String mrpStr = (String) grn.get("mrp");
    String costStr = (String) grn.get(COST_PRICE);
    BigDecimal mrpValue;
    BigDecimal costValue;
    if ("".equals(mrpStr) && "".equals(costStr)) {
      rowFailed = true;
      addWarning(warnings, lineNumber, "Please provide mrp/cost_price values");
    } else {
      try {
        mrpValue = new BigDecimal(mrpStr);
        costValue = new BigDecimal(costStr);
        if (mrpValue.compareTo(BigDecimal.ZERO) <= 0 && costValue.compareTo(BigDecimal.ZERO) <= 0) {
          rowFailed = true;
          addWarning(warnings, lineNumber, "Please provide mrp/cost_price greater than zero");
        } else if (costValue.compareTo(mrpValue) > 0) {
          rowFailed = true;
          addWarning(warnings, lineNumber, "cost_price could not be greater than mrp");
        }
      } catch (NumberFormatException nfe) {
        logger.debug("Invalid value for mrp/cost_price", nfe);
        rowFailed = true;
        addWarning(warnings, lineNumber, "Please provide numerical value for mrp/cost_price");
      }
    }
    
    return rowFailed;
  }
  
  private Map<String, Object> getMedicine(Map<String, Object> grn, int lineNumber) {
    Map<String, Object> medicine = new HashMap<>();
    HashMap<String, Object> itemBatchKeys = new HashMap<>();
    String batchNo = (String) grn.get(ITEM_BATCH_NO);
    int medicineId = (int)grn.get(MEDICINE_ID);
    itemBatchKeys.put(BATCH_NO, batchNo);
    itemBatchKeys.put(MEDICINE_ID, medicineId);
    String batchNoFromCsv = (String) grn.get(BATCH_ID);
    BasicDynaBean batchDetailsBean = storeItemBatchDetailsService.findByKey(itemBatchKeys);  
    if (batchDetailsBean == null) {
      throw new HMSException("exception.csv.item.batch.error",
          new String[] { (String) grn.get(ITEM_ID), batchNoFromCsv });
    } else {
      medicine.put(ITEM_BATCH_ID, (int) batchDetailsBean.get(ITEM_BATCH_ID));
    }
    BigDecimal totalTaxAmount = BigDecimal.ZERO;
    BigDecimal totalTaxRate = BigDecimal.ZERO;
    medicine.put(MEDICINE_ID, grn.get(MEDICINE_ID));
    medicine.put(BATCH_NO, grn.get(ITEM_BATCH_NO));
    medicine.put("mrp", grn.get("mrp"));
    medicine.put(BONUS_TAX, grn.get(BONUS_TAX) == null ? "0" : grn.get(BONUS_TAX));
    medicine.put("grn_med", "N");
    medicine.put("line_no", lineNumber);
    medicine.put(EXP_DATE, grn.get(EXP_DATE));
    medicine.put(GRN_PKG_SIZE, grn.get(ISSUE_BASE_UNIT));
    medicine.put(COST_PRICE, grn.get(COST_PRICE));
    medicine.put("tax_type", "CB");
    medicine.put(BILLED_QTY, grn.get(BILLED_QTY));
    medicine.put(BONUS_QTY, grn.get(BONUS_QTY));
    medicine.put(DISCOUNT, grn.get(ITEM_DISCOUNT));
    medicine.put(ITEM_ID, grn.get(ITEM_ID));
    for (String key : grn.keySet()) {
      if (key.startsWith(TAX_AMOUNT) || key.startsWith(TAX_RATE)
          || key.startsWith("taxsubgroupid")) {
        if (key.startsWith(TAX_AMOUNT)) {
          totalTaxAmount = totalTaxAmount.add(new BigDecimal((String) grn.get(key)));
        }
        if (key.startsWith(TAX_RATE)) {
          totalTaxRate = totalTaxRate.add(new BigDecimal((String) grn.get(key)));
        }
        medicine.put(key, grn.get(key));
      }
    }

    medicine.put("tax", totalTaxAmount.toString());
    medicine.put("orig_tax", 0.00);
    medicine.put("tax_rate", totalTaxRate.toString());
    medicine.put("item_ced", "0");
    medicine.put("item_ced_per", "0");
    medicine.put("scheme_discount", "0");
    medicine.put("orig_scheme_discount", "0");
    return medicine;

  }

  private void addWarning(MultiValueMap<Object, Object> warnings, Integer lineNumber,
      String message) {
    warnings.add(lineNumber, message);
  }

  
  /**
   * transaction method for supplier debit note.
   * @param params the params
   * @param warnings the warnings
   */
  @Transactional
  public void makeSupplierDebit(Map<String, Object> params,
      MultiValueMap<Object, Object> warnings) {
    try {
      params.remove(INTEGRATION_GRN_ID);
      BasicDynaBean debitBean = storeDebitNoteService.getBean();
      ConversionUtils.copyToDynaBean(params, debitBean);
      Date date = new Date();
      java.sql.Date onlyDate = new java.sql.Date(date.getTime());
      java.sql.Timestamp dt = new java.sql.Timestamp(date.getTime());
      debitBean.set("debit_note_date", onlyDate);
      debitBean.set("date_time", dt);

      BasicDynaBean grnMainBean = storeGrnMainRepository.getBean();
      ConversionUtils.copyToDynaBean(params, grnMainBean);
      String grnDebitNoteNo = AutoIdGenerator.getSequenceId("store_grn_debit_note_seq", "DebitGRN");
      grnMainBean.set(GRN_NO, grnDebitNoteNo);
      grnMainBean.set("user_name", params.get(USER_NAME));

      List<BasicDynaBean> groupList = taxGroupService.getTaxItemGroups();
      int storeId = Integer.parseInt((String) params.get("store_id"));
      StoresHelper storeHelper = new StoresHelper();
      List<Map<String, Object>> medicines = (List<Map<String, Object>>) params.get("medicines");
      int lineNo = 1;
      List<BasicDynaBean> medList = new ArrayList<>();
      for (Map<String, Object> medicine : medicines) {
        try {
          lineNo = (int)medicine.get("line_no");
          BasicDynaBean medBean = storeGrnDetailsRepository.getBean();
          ConversionUtils.copyToDynaBean(medicine, medBean);
          medBean.set(GRN_NO, grnDebitNoteNo);
          medBean.set("tax", ((BigDecimal)medBean.get("tax")).negate());
          medBean.set("orig_tax", ((BigDecimal)medBean.get("orig_tax")).negate());
          BigDecimal returnqty = null;
          BigDecimal returnBilledqty = null;
          BigDecimal returnBonusqty = null;

          // if Qty in (radio option):Package Units saved as it is else xxx * issue_base_unit ...
          if ("P".equalsIgnoreCase((String) grnMainBean.get(GRN_QTY_UNIT))) {
            returnqty = new BigDecimal((String) medicine.get(BILLED_QTY))
                .add(new BigDecimal((String) medicine.get(BONUS_QTY)))
                .multiply(new BigDecimal((String) medicine.get(GRN_PKG_SIZE)).setScale(2,
                    BigDecimal.ROUND_HALF_UP));
            returnBilledqty = new BigDecimal((String) medicine.get(BILLED_QTY))
                .multiply(new BigDecimal((String) medicine.get(GRN_PKG_SIZE)).setScale(2,
                    BigDecimal.ROUND_HALF_UP));
            returnBonusqty = new BigDecimal((String) medicine.get(BONUS_QTY))
                .multiply(new BigDecimal((String) medicine.get(GRN_PKG_SIZE)).setScale(2,
                    BigDecimal.ROUND_HALF_UP));
          } else {
            returnqty = new BigDecimal((String) medicine.get(BILLED_QTY))
                .add(new BigDecimal((String) medicine.get(BONUS_QTY)));
            returnBilledqty = new BigDecimal((String) medicine.get(BILLED_QTY));
            returnBonusqty = new BigDecimal((String) medicine.get(BONUS_QTY));
          }

          String indentNo = (String) medicine.get("");
          boolean qtyAvailable = true;
          int medicineId = (int) medicine.get(MEDICINE_ID);
          int itemBatchId = (int) medicine.get(ITEM_BATCH_ID);
          if (indentNo != null && !"".equals(indentNo)) {
            qtyAvailable = true;
          } else {
            qtyAvailable = storeStockDetailsService.isQuantityAvailable(storeId, medicineId,
                itemBatchId, returnqty);
          }
          if (!qtyAvailable) {
            throw new HMSException(STOCK_NOT_AVLBLE,
                new String[] { (String) medicine.get(ITEM_ID) });
          }

          int grnDetNo = DataBaseUtil.getNextSequence("grn_item_order_seq");
          medBean.set("item_order", grnDetNo);
          BigDecimal disc = (BigDecimal) medBean.get(DISCOUNT);
          BigDecimal billedQty = (BigDecimal) medBean.get(BILLED_QTY);
          BigDecimal bonusQty = (BigDecimal) medBean.get(BONUS_QTY);
          medBean.set(DISCOUNT,disc.negate());
       
          if ("P".equalsIgnoreCase((String) grnMainBean.get(GRN_QTY_UNIT))) {
            medBean.set(BILLED_QTY,
                billedQty.negate().multiply((BigDecimal) medBean.get(GRN_PKG_SIZE)).setScale(2,
                    BigDecimal.ROUND_HALF_UP));
            medBean.set(BONUS_QTY,
                bonusQty.negate().multiply((BigDecimal) medBean.get(GRN_PKG_SIZE)).setScale(2,
                    BigDecimal.ROUND_HALF_UP));
          } else {
            medBean.set(BILLED_QTY, billedQty.negate().setScale(2, BigDecimal.ROUND_HALF_UP));
            medBean.set(BONUS_QTY, bonusQty.negate().setScale(2, BigDecimal.ROUND_HALF_UP));
          }

          if (billedQty.compareTo(BigDecimal.ZERO) > 0) {
            Map statusMap = stockFifoService.reduceStock(storeId, itemBatchId, "D", returnBilledqty,
                null, (String) params.get(USER_NAME), "SupplierReturnDebit", grnDetNo, false, "S",
                null);
            if (!(Boolean) statusMap.get(STATUS)) {
              throw new HMSException(STOCK_NOT_AVLBLE,
                  new String[] { (String) medicine.get(ITEM_ID) });
            }
          }

          if (bonusQty.compareTo(BigDecimal.ZERO) > 0) {
            Map statusMap = stockFifoService.reduceStock(storeId, itemBatchId, "D", returnBonusqty,
                null, (String) params.get(USER_NAME), "SupplierReturnDebit", grnDetNo, false, "B",
                null);
            if (!(Boolean) statusMap.get(STATUS)) {
              throw new HMSException(STOCK_NOT_AVLBLE,
                  new String[] { (String) medicine.get(ITEM_ID) });
            }
          }
      
          medList.add(medBean);
          
          Map keys = new HashMap();
          keys.put(MEDICINE_ID, medBean.get(MEDICINE_ID));
          keys.put(GRN_NO, medBean.get(GRN_NO));
          keys.put(ITEM_BATCH_ID, medBean.get(ITEM_BATCH_ID));
          storeGrnTaxDetailsService.delete(keys);
          for (int j = 0; j < groupList.size(); j++) {
            BasicDynaBean groupBean = groupList.get(j);
            BasicDynaBean taxBean = storeGrnTaxDetailsService.getBean();
            storeHelper.setTaxDetails(medicine, (Integer) groupBean.get(ITEM_GROUP_ID), taxBean);
            taxBean.set(MEDICINE_ID, medBean.get(MEDICINE_ID));
            taxBean.set(GRN_NO, medBean.get(GRN_NO));
            taxBean.set(ITEM_BATCH_ID, medBean.get(ITEM_BATCH_ID));

            Map<String, Object> taxMap = new HashMap<>();
            taxMap.put(MEDICINE_ID, medBean.get(MEDICINE_ID));
            taxMap.put(GRN_NO, medBean.get(GRN_NO));
            taxMap.put(ITEM_BATCH_ID, medBean.get(ITEM_BATCH_ID));
            taxMap.put(ITEM_SUB_GROUP_ID, taxBean.get(ITEM_SUB_GROUP_ID));
            if (storeGrnTaxDetailsService.findByKey(taxMap) != null) {
              Map keysMap = new HashMap();
              keysMap.put(MEDICINE_ID, medBean.get(MEDICINE_ID));
              keysMap.put(GRN_NO, medBean.get(GRN_NO));
              keysMap.put(ITEM_BATCH_ID, medBean.get(ITEM_BATCH_ID));
              keysMap.put(ITEM_SUB_GROUP_ID, taxBean.get(ITEM_SUB_GROUP_ID));
              storeGrnTaxDetailsService.update(taxBean, keysMap);
            } else {
              if (taxBean.get(ITEM_SUB_GROUP_ID) != null && taxBean.get("tax_amt") != null) {
                storeGrnTaxDetailsService.insert(taxBean);
              }
            }
          }
        } catch (HMSException ex) {
          addWarning(warnings, lineNo, ex.getMessage());
        }
      }
      if (!medList.isEmpty()) {
        String debitNoteNo = AutoIdGenerator.getSequenceId("store_debit_note_seq", "PhDebitNote");
        debitBean.set("debit_note_no", debitNoteNo);
        grnMainBean.set("debit_note_no", debitNoteNo);
        storeDebitNoteService.insert(debitBean);
        storeGrnMainRepository.insert(grnMainBean);
        storeGrnDetailsRepository.batchInsert(medList);
      }
      stockFifoService.updateStockTimeStamp();
      stockFifoService.updateStoresStockTimeStamp(storeId);

    } catch (SQLException ex) {
      addWarning(warnings, 2, "grn_item_order_seq is not cretaed ," + ex.getMessage());
    }
  }
}
