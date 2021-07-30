package com.insta.hms.core.inventory.stockmgmt;

import au.com.bytecode.opencsv.CSVReader;
import com.bob.hms.common.Constants;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.BusinessService;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.inventory.StoresHelper;
import com.insta.hms.core.inventory.stocks.StockFifoService;
import com.insta.hms.core.inventory.stocks.StoreGRNDetailsRepository;
import com.insta.hms.core.inventory.stocks.StoreGRNMainRepository;
import com.insta.hms.core.inventory.stocks.StoreGrnTaxDetailsService;
import com.insta.hms.core.inventory.stocks.StoreInvoiceRepository;
import com.insta.hms.core.inventory.stocks.StoreStockDetailsRepository;
import com.insta.hms.core.inventory.taxation.PurchaseTaxCalculatorSupport;
import com.insta.hms.core.patient.registration.PatientInsurancePlansRepository;
import com.insta.hms.mdm.integration.CsvImportable;
import com.insta.hms.mdm.integration.item.StoreItemDetailIntegrationService;
import com.insta.hms.mdm.integration.taxsubgroups.TaxSubGroupsIntegrationService;
import com.insta.hms.mdm.item.StoreItemDetailsRepository;
import com.insta.hms.mdm.organization.OrganizationRepository;
import com.insta.hms.mdm.storeitembatchdetails.StoreItemBatchDetailsService;
import com.insta.hms.mdm.storeitemlotdetails.StoreItemLotDetailsService;
import com.insta.hms.mdm.stores.StoreService;
import com.insta.hms.mdm.supplier.SupplierService;
import com.insta.hms.mdm.taxgroups.TaxGroupService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
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
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class StockService.
 */

/**
 * @author irshadmohammed.
 *
 */
@Service
public class StockService extends BusinessService implements CsvImportable {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(StockService.class);

  @LazyAutowired
  private StoreGRNMainRepository mainRepo;

  @LazyAutowired
  private StoreGRNDetailsRepository storeGRNDetailsRepository;

  @LazyAutowired
  private StoreInvoiceRepository storeInvoiceRepository;

  @LazyAutowired
  private PurchaseTaxCalculatorSupport purchaseTaxCalculatorSupport;

  @LazyAutowired
  private SupplierService supplierService;

  @LazyAutowired
  private StockRepository stockRepository;

  @LazyAutowired
  private StoreItemBatchDetailsService storeItemBatchDetailsService;

  @LazyAutowired
  private TaxGroupService taxGroupService;

  @LazyAutowired
  private StoreGrnTaxDetailsService storeGrnTaxDetailsService;

  @LazyAutowired
  private StoreStockDetailsRepository storeStockDetailsRepository;

  @LazyAutowired
  private StoreItemLotDetailsService storeItemLotDetailsService;

  @LazyAutowired
  private StockFifoService stockFifoService;

  @LazyAutowired
  private StoreService storeService;

  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;
  /**
   * Organization Repository.
   */
  @LazyAutowired
  private OrganizationRepository organizationRepository;

  @LazyAutowired
  private PatientInsurancePlansRepository patientInsurancePlansRepository;
  @LazyAutowired
  private StoreItemDetailsRepository storeItemDetailsRepository;

  /** The missing headers key. */
  private static final String MISSINGHEADERSKEY = "exception.csv.missing.headers";

  /** The non printable headers key. */
  private static final String NONPRINTABLEHEADERSKEY = "exception.csv.non.printable.characters";

  /** The non comma delimiter key. */
  private static final String NONCOMMADELIMITERKEY = "exception.csv.non.comma.seperators";

  private static final String UNKNOWNHEADERKEY = "exception.csv.unknown.header";

  private static final String[] COLUMNS = new String[] { "integration_grn_id", "store_name",
      "supplier_name", "grn_date", "invoice_date", "invoice_number", "consignment_stock",
      "grn_qty_unit", "purpose_of_purchase", "item_id", "item_batch_no", "qty", "bonus_qty", "mrp",
      "cost_price", "exp_dt", "tax_sub_groups", "tax_amount", "due_date", "bill_discount",
      "item_discount" };

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
  
  static {
    DATE_FORMAT.setLenient(false);
  }
  
  @LazyAutowired
  private MessageUtil messageUtil;

  @LazyAutowired
  private StoreItemDetailIntegrationService storeItemDetailsIntegrationService;

  @LazyAutowired
  private TaxSubGroupsIntegrationService taxSubgroupIntegrationService;

  /**
   * Gets the CP details.
   *
   * @param storeId     the store id
   * @param itemBatchId the item batch id
   * @return the CP details
   */
  public BasicDynaBean getCPDetails(int storeId, int itemBatchId) {
    return stockRepository.getCPDetails(storeId, itemBatchId);
  }

  /**
   * Gets the batch sorted lot details.
   *
   * @param storeId     the store id
   * @param itemBatchId the item batch id
   * @return the batch sorted lot details
   */
  public List<BasicDynaBean> getBatchSortedLotDetails(int storeId, int itemBatchId) {
    return stockRepository.getBatchSortedLotDetails(storeId, itemBatchId);
  }

  /**
   * Gets the selling price.
   *
   * @param itemBatchId the item batch id
   * @return the selling price
   */
  public BasicDynaBean getSellingPrice(int itemBatchId) {
    return stockRepository.getSellingPrice(itemBatchId);
  }

  /**
   * Gets the medicine names in stock.
   *
   * @param writer             the writer
   * @param includeZeroStock   the include zero stock
   * @param retailable         the retailable
   * @param billable           the billable
   * @param issueType          the issue type
   * @param includeConsignment the include consignment
   * @param includeUnapproved  the include unapproved
   * @param onlySalesStores    the only sales stores
   * @param singleStore        the single store
   * @param storeId            the store id
   * @param healthAuthority    the health authority
   * @param grnNo              the grn no
   * @param medicineNameFilterText the medicine name filter text
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void getMedicineNamesInStock(Writer writer, boolean includeZeroStock,
      Boolean retailable, Boolean billable, String[] issueType, boolean includeConsignment,
      boolean includeUnapproved, boolean onlySalesStores, boolean singleStore, int storeId,
      String healthAuthority, String grnNo, String medicineNameFilterText)
      throws SQLException, IOException {
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("is_sales_store", "Y");
    List<BasicDynaBean> saleStoresList = storeService.lookup(true, filterMap);
    List<BasicDynaBean> storesList = storeService.lookup(true);
    stockRepository.getMedicineNamesInStock(writer, includeZeroStock, retailable, billable,
        issueType, includeConsignment, includeUnapproved, onlySalesStores, singleStore,
        storeId, healthAuthority, grnNo, saleStoresList, storesList, medicineNameFilterText);
  }

  /**
   * Gets the medicine names in stock.
   *
   * @param includeZeroStock the include zero stock
   * @param retailable the retailable
   * @param billable the billable
   * @param issueType the issue type
   * @param includeConsignment the include consignment
   * @param includeUnapproved the include unapproved
   * @param onlySalesStores the only sales stores
   * @param singleStore the single store
   * @param storeId the store id
   * @param healthAuthority the health authority
   * @param grnNo the grn no
   * @param medicineNameFilterText the medicine name filter text
   * @return the medicine names in stock
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public List<BasicDynaBean> getMedicineNamesInStock(boolean includeZeroStock,
      Boolean retailable, Boolean billable, String[] issueType, boolean includeConsignment,
      boolean includeUnapproved, boolean onlySalesStores, boolean singleStore, int storeId,
      String healthAuthority, String grnNo, String medicineNameFilterText)
      throws SQLException, IOException {
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("is_sales_store", "Y");
    List<BasicDynaBean> saleStoresList = storeService.lookup(true, filterMap);
    List<BasicDynaBean> storesList = storeService.lookup(true);
    return stockRepository.getMedicineNamesInStock(includeZeroStock, retailable, billable,
        issueType, includeConsignment, includeUnapproved, onlySalesStores, singleStore,
        storeId, healthAuthority, grnNo, saleStoresList, storesList, medicineNameFilterText);
  }


  /**
   * Gets the medicine names in stock.
   *
   * @param writer the writer
   * @param includeZeroStock the include zero stock
   * @param retailable the retailable
   * @param billable the billable
   * @param issueType the issue type
   * @param includeConsignment the include consignment
   * @param includeUnapproved the include unapproved
   * @param onlySalesStores the only sales stores
   * @param healthAuthority the health authority
   * @param medicineNameFilterText the medicine name filter text
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void getMedicineNamesInStock(Writer writer, boolean includeZeroStock,
      Boolean retailable, Boolean billable, String[] issueType, boolean includeConsignment,
      boolean includeUnapproved, boolean onlySalesStores, String healthAuthority,
      String medicineNameFilterText) throws SQLException, IOException {
    getMedicineNamesInStock(writer, includeZeroStock, retailable, billable, issueType,
        includeConsignment, includeUnapproved, onlySalesStores, false, 0, healthAuthority,
        null, medicineNameFilterText);
  }

  /**
   * Gets the medicine names in stock.
   *
   * @param includeZeroStock the include zero stock
   * @param retailable the retailable
   * @param billable the billable
   * @param issueType the issue type
   * @param includeConsignment the include consignment
   * @param includeUnapproved the include unapproved
   * @param onlySalesStores the only sales stores
   * @param healthAuthority the health authority
   * @param medicineNameFilterText the medicine name filter text
   * @return the medicine names in stock
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public List<BasicDynaBean> getMedicineNamesInStock(boolean includeZeroStock,
      Boolean retailable, Boolean billable, String[] issueType, boolean includeConsignment,
      boolean includeUnapproved, boolean onlySalesStores, String healthAuthority,
      String medicineNameFilterText) throws SQLException, IOException {
    return getMedicineNamesInStock(includeZeroStock, retailable, billable, issueType,
        includeConsignment, includeUnapproved, onlySalesStores, false, 0, healthAuthority,
        null, medicineNameFilterText);
  }

  /**
   * Gets the medicine names in stock.
   *
   * @param writer the writer
   * @param includeZeroStock the include zero stock
   * @param retailable the retailable
   * @param billable the billable
   * @param issueType the issue type
   * @param includeConsignment the include consignment
   * @param includeUnapproved the include unapproved
   * @param storeId the store id
   * @param healthAuthority the health authority
   * @param grnNo the grn no
   * @param medicineNameFilterText the medicine name filter text
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void getMedicineNamesInStock(Writer writer, boolean includeZeroStock,
      Boolean retailable, Boolean billable, String[] issueType, boolean includeConsignment,
      boolean includeUnapproved, int storeId, String healthAuthority, String grnNo,
      String medicineNameFilterText) throws SQLException, IOException {
    getMedicineNamesInStock(writer, includeZeroStock, retailable, billable, issueType,
        includeConsignment, includeUnapproved, false, true, storeId, healthAuthority, grnNo,
        medicineNameFilterText);
  }

  /**
   * Gets the medicine names in stock.
   *
   * @param includeZeroStock the include zero stock
   * @param retailable the retailable
   * @param billable the billable
   * @param issueType the issue type
   * @param includeConsignment the include consignment
   * @param includeUnapproved the include unapproved
   * @param storeId the store id
   * @param healthAuthority the health authority
   * @param grnNo the grn no
   * @param medicineNameFilterText the medicine name filter text
   * @return the medicine names in stock
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public List<BasicDynaBean> getMedicineNamesInStock(boolean includeZeroStock,
      Boolean retailable, Boolean billable, String[] issueType, boolean includeConsignment,
      boolean includeUnapproved, int storeId, String healthAuthority, String grnNo,
      String medicineNameFilterText) throws SQLException, IOException {
    return getMedicineNamesInStock(includeZeroStock, retailable, billable, issueType,
        includeConsignment, includeUnapproved, false, true, storeId, healthAuthority, grnNo,
        medicineNameFilterText);
  }

  /**
   * Gets the medicine names in stock.
   *
   * @param writer the writer
   * @param includeZeroStock the include zero stock
   * @param retailable the retailable
   * @param billable the billable
   * @param issueType the issue type
   * @param includeConsignment the include consignment
   * @param includeUnapproved the include unapproved
   * @param storeId the store id
   * @param healthAuthority the health authority
   * @param medicineNameFilterText the medicine name filter text
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void getMedicineNamesInStock(Writer writer, boolean includeZeroStock,
      Boolean retailable, Boolean billable, String[] issueType, boolean includeConsignment,
      boolean includeUnapproved, int storeId, String healthAuthority,
      String medicineNameFilterText) throws SQLException, IOException {
    getMedicineNamesInStock(writer, includeZeroStock, retailable, billable, issueType,
        includeConsignment, includeUnapproved, false, true, storeId, healthAuthority, null,
        medicineNameFilterText);
  }

  /**
   * Gets the medicine names in stock.
   *
   * @param includeZeroStock the include zero stock
   * @param retailable the retailable
   * @param billable the billable
   * @param issueType the issue type
   * @param includeConsignment the include consignment
   * @param includeUnapproved the include unapproved
   * @param storeId the store id
   * @param healthAuthority the health authority
   * @param medicineNameFilterText the medicine name filter text
   * @return the medicine names in stock
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public List<BasicDynaBean> getMedicineNamesInStock(boolean includeZeroStock,
      Boolean retailable, Boolean billable, String[] issueType, boolean includeConsignment,
      boolean includeUnapproved, int storeId, String healthAuthority,
      String medicineNameFilterText) throws SQLException, IOException {
    return getMedicineNamesInStock(includeZeroStock, retailable, billable, issueType,
        includeConsignment, includeUnapproved, false, true, storeId, healthAuthority, null,
        medicineNameFilterText);
  }

  /**
   * Get batch details of an item.
   *
   * @param storeId the store id
   * @param medicineId the medicine id
   * @return the item details
   */
  public List<BasicDynaBean> getItemDetails(int storeId, int medicineId) {
    BasicDynaBean genericPreferanceBean = genericPreferencesService.getAllPreferences();
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("dept_id", storeId);
    BasicDynaBean storeBean = storeService.findByPk(params);
    return stockRepository.getItemDetails(storeBean, medicineId, genericPreferanceBean);
  }

  /**
   * Gets the item discount amounts.
   *
   * @param paramMap    the paramters map
   * @param discountMap the discount map
   * @return the item discount amounts
   */
  public BigDecimal getItemDiscountAmounts(Map<String, Object> paramMap,
      Map<String, Object> discountMap) {

    BigDecimal finalDiscount = BigDecimal.ZERO;
    BigDecimal discountAmt = BigDecimal.ZERO;

    if (null != paramMap.get("visit_id") && null != paramMap.get("medicine_id")
        && null != paramMap.get("item_batch_id") && null != paramMap.get("qty")) {

      int medicineId = (Integer) paramMap.get("medicine_id");
      BigDecimal pkgSize = (BigDecimal) paramMap.get("pkg_size");
      BigDecimal discountPer = (BigDecimal) paramMap.get("discount_per");
      BigDecimal rate = (BigDecimal) paramMap.get("rate");
      BigDecimal qty = (BigDecimal) paramMap.get("qty");
      String changeType = (String) paramMap.get("change_type");
      String billNo = (String) paramMap.get("bill_no");
      String chargehead = (String) paramMap.get("chargehead");

      // Change type is 'D' means discount is changed so ignore the master discount and used
      // user
      // entered discount.
      if (changeType != null && !changeType.isEmpty() && (changeType.equals("D")
          || changeType.equals("A") || changeType.equals("T") || changeType.equals("Q"))) {
        finalDiscount = discountPer;
      } else {
        BigDecimal ratePlanDisc = BigDecimal.ZERO;
        BigDecimal itemCategoryDisc = BigDecimal.ZERO;
        Integer insuranceCategoryId = null;
        Integer itemCategoryId = null;

        BasicDynaBean itemCategoryBean =
            storeItemDetailsRepository.getItemCategoryDiscountDetails(medicineId);
        if (itemCategoryBean != null) {
          insuranceCategoryId = (Integer) itemCategoryBean.get("insurance_category_id");
          itemCategoryDisc = itemCategoryBean.get("discount") != null
              ? (BigDecimal) itemCategoryBean.get("discount")
              : BigDecimal.ZERO;
          itemCategoryId = (Integer) itemCategoryBean.get("med_category_id");
        }

        List<BasicDynaBean> discPlanDetailsList =
            patientInsurancePlansRepository.getDiscountPlanDetails(billNo);
        BasicDynaBean ratePlanBean = organizationRepository.getRatePlanDetail(billNo);
        Iterator<BasicDynaBean> iterator = discPlanDetailsList.iterator();
        while (iterator.hasNext()) {
          BasicDynaBean discBean = iterator.next();
          if (discBean.get("applicable_type") != null
              && discBean.get("applicable_type").equals("N")
              && discBean.get("discount_plan_id") != null && insuranceCategoryId != null
              && insuranceCategoryId
                  .equals(Integer.valueOf((String) discBean.get("applicable_to_id")))) {
            if (discBean.get("discount_value") != null) {
              finalDiscount = (BigDecimal) discBean.get("discount_value");
              break;
            }
          }

          if (discBean.get("applicable_type") != null
              && discBean.get("applicable_type").equals("C")
              && discBean.get("discount_plan_id") != null && chargehead != null
              && chargehead.equals((String) discBean.get("applicable_to_id"))) {
            if (discBean.get("discount_value") != null) {
              finalDiscount = (BigDecimal) discBean.get("discount_value");
              break;
            }
          }
          if (discBean.get("applicable_type") != null && discBean.get("applicable_type").equals("S")
              && discBean.get("discount_plan_id") != null && itemCategoryId != null
              && itemCategoryId
                  .equals(Integer.valueOf((String) discBean.get("applicable_to_id")))) {
            if (discBean.get("discount_value") != null) {
              finalDiscount = (BigDecimal) discBean.get("discount_value");
              break;
            }
          }

        }

        if (finalDiscount.equals(BigDecimal.ZERO)) {
          if (ratePlanBean != null
              && ratePlanBean.get("pharmacy_discount_percentage") != null) {
            ratePlanDisc = (BigDecimal) ratePlanBean.get("pharmacy_discount_percentage");
          }
          finalDiscount = ratePlanDisc.add(itemCategoryDisc);
        }
      }

      // Discount Amount Calculation.
      if (!finalDiscount.equals(BigDecimal.ZERO)) {
        BigDecimal unitMrp = ConversionUtils.divideHighPrecision(rate, pkgSize);
        BigDecimal netAmt = qty.multiply(unitMrp);
        discountAmt = (netAmt.multiply(finalDiscount)).divide(new BigDecimal(100));
      }

      discountMap.put("discount_per", finalDiscount);
      discountMap.put("discount_amt", ConversionUtils.setScale(discountAmt));
    }
    return finalDiscount;
  }

  /**
   * Gets the medicine stock with pat amts in dept.
   *
   * @param medicineIds the medicine ids
   * @param deptId the dept id
   * @param planId the plan id
   * @param visitType the visit type
   * @param includeZeroStock the include zero stock
   * @param visitStoreRatePlanId the visit store rate plan id
   * @param healthAuthority the health authority
   * @return the medicine stock with pat amts in dept
   */
  public List<BasicDynaBean> getMedicineStockWithPatAmtsInDept(List<Integer> medicineIds,
      int deptId, int planId, String visitType, boolean includeZeroStock,
      int visitStoreRatePlanId, String healthAuthority) {

    if (medicineIds == null || medicineIds.isEmpty()) {
      throw new IllegalArgumentException("Stock query requires at least one item");
    }
    BasicDynaBean storeBean = storeService.findByStore(deptId);
    int storeRatePlanId = (storeBean.get("store_rate_plan_id") == null ? 0
        : (Integer) storeBean.get("store_rate_plan_id"));
    boolean useBatchMRP = ((String) storeBean.get("use_batch_mrp")).equals("Y");
    return stockRepository.getMedicineStockWithPatAmtsInDept(medicineIds, deptId, planId,
        visitType, includeZeroStock, visitStoreRatePlanId, healthAuthority, storeRatePlanId,
        useBatchMRP);
  }

  /**
   * Gets the order kit medicine stock with pat amts in dept.
   *
   * @param medicineIds the medicine ids
   * @param deptId the dept id
   * @param planId the plan id
   * @param visitType the visit type
   * @param includeZeroStock the include zero stock
   * @param visitStoreRatePlanId the visit store rate plan id
   * @param healthAuthority the health authority
   * @return the order kit medicine stock with pat amts in dept
   */
  public List<BasicDynaBean> getOrderKitMedicineStockWithPatAmtsInDept(
      List<Integer> medicineIds, int deptId, int planId, String visitType,
      boolean includeZeroStock, int visitStoreRatePlanId, String healthAuthority) {

    if (medicineIds == null || medicineIds.size() == 0) {
      throw new IllegalArgumentException("Stock query requires at least one item");
    }
    BasicDynaBean storeBean = storeService.findByStore(deptId);
    int storeRatePlanId = (storeBean.get("store_rate_plan_id") == null ? 0
        : (Integer) storeBean.get("store_rate_plan_id"));
    boolean useBatchMRP = ((String) storeBean.get("use_batch_mrp")).equals("Y");

    return stockRepository.getOrderKitMedicineStockWithPatAmtsInDept(medicineIds, deptId,
        planId, visitType, includeZeroStock, visitStoreRatePlanId, healthAuthority,
        storeRatePlanId, useBatchMRP);

  }

  /**
   * Gets the issued item list.
   *
   * @param issueNo the issue no
   * @return the issued item list
   */
  public List<BasicDynaBean> getIssuedItemList(Integer issueNo) {
    return stockRepository.getIssuedItemList(issueNo);
  }

  /**
   * Gets the package medicine stock in store.
   *
   * @param medicineIds the medicine ids
   * @param storeId the store id
   * @param planId the plan id
   * @param visitType the visit type
   * @param healthAuthority the health authority
   * @return the package medicine stock in store
   */
  public List<BasicDynaBean> getPackageMedicineStockInStore(List<Integer> medicineIds,
      int storeId, int planId, String visitType, String healthAuthority) {

    return stockRepository.getPackageMedicineStockInStore(medicineIds, storeId, planId,
        visitType, healthAuthority);
  }

  /**
   * Insert stock.
   *
   * @param params the params
   */
  @Transactional
  public void insertStock(Map<String, Object> params) {

    BasicDynaBean genericPreferences = genericPreferencesService.getAllPreferences();
    String integrationGrnId = (String) params.get("integration_grn_id");
    String grnNo = (String) params.get("grn_no");
    String username = (String) params.get("username");
    Integer storeId = Integer.parseInt((String) params.get("store_id"));
    String supplierId = (String) params.get("supplier_id");
    BasicDynaBean supplier = supplierService
        .findByPk(Collections.singletonMap("supplier_code", supplierId));
    String supplierName = (String) supplier.get("supplier_name");
    String cashPurchase = (String) params.get("cash_purchase");
    List<Map<String, Object>> medicines = (List<Map<String, Object>>) params.get("medicines");

    logger.debug("Number of items being entered in stock: " + medicines.size());
    BasicDynaBean invBean = null;
    BasicDynaBean grnBean = null;

    // main tables: store_inovice and store_grn_main, create or update

    BasicDynaBean existingGrnBean = null;
    if (StringUtils.isNotEmpty(integrationGrnId)) {
      existingGrnBean = mainRepo.findByKey("integration_grn_id", integrationGrnId); 
      if (existingGrnBean != null) {
        grnNo = (String) existingGrnBean.get("grn_no");
        logger.debug("existing int id for " + integrationGrnId + " " + grnNo);
      }
    }

    if (StringUtils.isEmpty(grnNo)) {
      invBean = storeInvoiceRepository.getBean();
      grnBean = mainRepo.getBean();
      createInvoiceGrn(params, invBean, grnBean);
      grnNo = (String) grnBean.get("grn_no");
      logger.debug("new grn created for " + integrationGrnId + " " + " new grnno " + grnNo);
      grnBean.set("integration_grn_id", integrationGrnId);
    } else {
      grnBean = mainRepo.findByKey("grn_no", grnNo);
      int suppInvId = (Integer) grnBean.get("supplier_invoice_id");
      invBean = stockRepository.getInvDetails(suppInvId);

      // invoice to be updated with new values
      ConversionUtils.copyToDynaBean(params, invBean);
      ConversionUtils.copyToDynaBean(params, invBean, "inv_");
      invBean.set("date_time", DateUtil.getCurrentTimestamp());
      invBean.set("cst_rate", BigDecimal.ZERO);
      if (cashPurchase == null) {
        invBean.set("cash_purchase", "N");
      }

      String[] columns = { "invoice_date", "invoice_no", "due_date", "po_reference", "discount",
          "round_off", "status", "discount_type", "discount_per", "date_time", "other_charges",
          "remarks", "cess_tax_rate", "cess_tax_amt", "debit_amt", "cash_purchase",
          "payment_remarks", "cst_rate", "company_name", "means_of_transport", "consignment_no",
          "consignment_date", "transportation_charges" };

      BasicDynaBean updatedInvoiceBean = storeInvoiceRepository.findByKey("supplier_invoice_id",
          invBean.get("supplier_invoice_id"));

      for (String column : columns) {
        updatedInvoiceBean.set(column, invBean.get(column));
      }

      int count = storeInvoiceRepository.update(updatedInvoiceBean,
          Collections.singletonMap("supplier_invoice_id", invBean.get("supplier_invoice_id")));
      assert (count == 1) : "Invoice update failed with row count: " + count;

      // GRN can be updated with grn_qty_unit alone
      ConversionUtils.copyToDynaBean(params, grnBean);
      String[] grnColumns = { "invoice_date", "grn_qty_unit", "purpose_of_purchase" };
      count = mainRepo.update(grnBean, Collections.singletonMap("grn_no", grnBean.get("grn_no")));
      assert (count == 1) : "Invoice update failed with row count: " + count;
    }

    List<BasicDynaBean> groupList = taxGroupService.getTaxItemGroups();
    StoresHelper storeHelper = new StoresHelper();
    // detail table and stock related tables (stock, item-batch and lot)
    for (Map<String, Object> medicine : medicines) {
      String vatType = "C";

      BasicDynaBean gdBean = storeGRNDetailsRepository.getBean();
      ConversionUtils.copyToDynaBean(medicine, gdBean);
      gdBean.set("grn_no", grnNo);
      gdBean.set("total_qty",
          ((BigDecimal) gdBean.get("billed_qty")).add((BigDecimal) gdBean.get("bonus_qty")));
      gdBean.set("issue_qty", new BigDecimal("0"));
      gdBean.set("outgoing_tax_rate", gdBean.get("tax_rate"));

      int medicineId = Integer.parseInt((String) medicine.get("medicine_id"));
      String batchNo = (String) medicine.get("batch_no");

      HashMap<String, Object> itemBatchKeys = new HashMap<String, Object>();
      itemBatchKeys.put("batch_no", batchNo);
      itemBatchKeys.put("medicine_id", medicineId);

      BasicDynaBean existingItemBatchBean = storeItemBatchDetailsService.findByKey(itemBatchKeys);
      int itemBatchId;

      BigDecimal mrp = new BigDecimal((String) medicine.get("mrp"));
      if (existingItemBatchBean == null) {
        // if batch is new,an insert into store_item_batch_details is required
        BasicDynaBean newItemBatchBean = storeItemBatchDetailsService.getBean();
        newItemBatchBean.set("batch_no", gdBean.get("batch_no"));
        newItemBatchBean.set("medicine_id", gdBean.get("medicine_id"));
        newItemBatchBean.set("exp_dt", gdBean.get("exp_dt"));
        newItemBatchBean.set("mrp", mrp);
        newItemBatchBean.set("username", username);
        storeItemBatchDetailsService.insert(newItemBatchBean);
        itemBatchId = (int) newItemBatchBean.get("item_batch_id");

      } else {
        itemBatchId = (Integer) existingItemBatchBean.get("item_batch_id");
        // update the MRP if it is different (can happen if MRP is set in PO)
        BigDecimal existingMrp = (BigDecimal) existingItemBatchBean.get("mrp");
        if (mrp.compareTo(existingMrp) != 0) {
          existingItemBatchBean.set("mrp", mrp);
          existingItemBatchBean.set("username", username);
          storeItemBatchDetailsService.update(existingItemBatchBean);
        }
      }

      if (null != existingGrnBean) {
        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("grn_no", (String) existingGrnBean.get("grn_no"));
        filterMap.put("medicine_id", medicineId);
        filterMap.put("batch_no", batchNo);
        BasicDynaBean storeGrnDetailsBean = storeGRNDetailsRepository.findByKey(filterMap);
        if (null != storeGrnDetailsBean) {
          medicine.put("grn_med", "Y");
        }
      }
      
      if (!((String) medicine.get("grn_med")).equalsIgnoreCase("Y")) {
        // insert grn items
        gdBean.set("item_batch_id", itemBatchId);
        storeGRNDetailsRepository.insert(gdBean);

      } else {
        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("grn_no", grnNo);
        filterMap.put("medicine_id", medicineId);
        filterMap.put("batch_no", batchNo);
        BasicDynaBean storeGrnDetailsBean = storeGRNDetailsRepository.findByKey(filterMap);
        String[] columns = { "cost_price", "discount", "scheme_discount", "tax_rate", "tax_type",
            "tax", "billed_qty", "bonus_qty", "total_qty", "item_ced_per", "item_ced",
            "outgoing_tax_rate", "bonus_tax" };

        for (String column : columns) {
          storeGrnDetailsBean.set(column, gdBean.get(column));
        }
        storeGRNDetailsRepository.update(storeGrnDetailsBean, filterMap);
      }
      Map keys = new HashMap();
      keys.put("medicine_id", gdBean.get("medicine_id"));
      keys.put("grn_no", gdBean.get("grn_no"));
      keys.put("item_batch_id", itemBatchId);
      storeGrnTaxDetailsService.delete(keys);
      for (int j = 0; j < groupList.size(); j++) {
        BasicDynaBean groupBean = groupList.get(j);
        BasicDynaBean taxBean = storeGrnTaxDetailsService.getBean();
        storeHelper.setTaxDetails(medicine, (Integer) groupBean.get("item_group_id"), taxBean);
        taxBean.set("medicine_id", gdBean.get("medicine_id"));
        taxBean.set("grn_no", gdBean.get("grn_no"));
        taxBean.set("item_batch_id", itemBatchId);

        Map<String, Object> taxMap = new HashMap<String, Object>();
        taxMap.put("medicine_id", gdBean.get("medicine_id"));
        taxMap.put("grn_no", gdBean.get("grn_no"));
        taxMap.put("item_batch_id", itemBatchId);
        taxMap.put("item_subgroup_id", taxBean.get("item_subgroup_id"));
        if (storeGrnTaxDetailsService.findByKey(taxMap) != null) {
          Map keysMap = new HashMap();
          keysMap.put("medicine_id", gdBean.get("medicine_id"));
          keysMap.put("grn_no", gdBean.get("grn_no"));
          keysMap.put("item_batch_id", itemBatchId);
          keysMap.put("item_subgroup_id", taxBean.get("item_subgroup_id"));
          storeGrnTaxDetailsService.update(taxBean, keysMap);
        } else {
          if (taxBean.get("item_subgroup_id") != null && taxBean.get("tax_amt") != null) {
            storeGrnTaxDetailsService.insert(taxBean);
          }
        }

      }
      /*
       * Do all the store stock stuff now.
       */
      BigDecimal totalQty = (BigDecimal) gdBean.get("total_qty");
      BigDecimal pkgSize = (BigDecimal) gdBean.get("grn_pkg_size");

      BasicDynaBean stockBean = storeStockDetailsRepository.getBean();
      ConversionUtils.copyToDynaBean(medicine, stockBean);
      BasicDynaBean itemLotBean = storeItemLotDetailsService.getBean();
      ConversionUtils.copyToDynaBean(medicine, itemLotBean);

      // common for all types of stock change
      stockBean.set("stock_time", DateUtil.getCurrentTimestamp());
      stockBean.set("username", username);
      stockBean.set("change_source", "StockEntry");
      stockBean.set("received_date", DateUtil.getCurrentDate());
      stockBean.set("last_cp_grn", grnNo);
      stockBean.set("item_ced_amt", BigDecimal.ZERO);

      // Set the new batch attributes and insert
      stockBean.set("dept_id", storeId);
      stockBean.set("package_sp", gdBean.get("adj_mrp"));
      stockBean.set("stock_pkg_size", gdBean.get("grn_pkg_size"));
      stockBean.set("package_uom", gdBean.get("grn_package_uom"));

      stockBean.set("item_supplier_name", supplierName);
      stockBean.set("item_supplier_code", supplierId);
      stockBean.set("item_invoice_no", invBean.get("invoice_no"));
      stockBean.set("item_grn_no", grnNo);
      stockBean.set("max_cp_grn", grnNo);
      stockBean.set("tax",
          ((BigDecimal) stockBean.get("tax")).compareTo((BigDecimal) gdBean.get("bonus_tax")) > 0
              ? ((BigDecimal) stockBean.get("tax")).subtract((BigDecimal) gdBean.get("bonus_tax"))
              : ((BigDecimal) gdBean.get("bonus_tax")).subtract((BigDecimal) stockBean.get("tax")));

      // bonus stock, bonus shd be a seperate stock with package_cp almost 0 (only bonus_tax)
      BigDecimal bonusStockQty = (BigDecimal) gdBean.get("bonus_qty");
      BigDecimal bonusCostValue = (BigDecimal) gdBean.get("bonus_tax");
      BigDecimal billedStockQty = (BigDecimal) gdBean.get("billed_qty");
      BigDecimal billedCostValue = (new BigDecimal((String) medicine.get("med_total")))
          .subtract(new BigDecimal((String) medicine.get("bonus_tax")));

      // if no billed qty, allocate all amount to bonus (can be different from bonusTax
      // in case there is discount given).
      if (billedStockQty.compareTo(BigDecimal.ZERO) == 0) {
        bonusCostValue = (BigDecimal) medicine.get("med_total");
      }

      BasicDynaBean existingGdBean = null;

      if (existingGdBean != null) {
        // Calculate incremental quantity
        billedStockQty = billedStockQty.subtract((BigDecimal) existingGdBean.get("billed_qty"));
        bonusStockQty = bonusStockQty.subtract((BigDecimal) existingGdBean.get("bonus_qty"));

        BigDecimal existingBonusCostValue = (BigDecimal) existingGdBean.get("bonus_tax");
        BigDecimal existingTotalCostValue = ((BigDecimal) existingGdBean.get("med_total"));
        BigDecimal existingBilledCostValue = existingTotalCostValue
            .subtract(existingBonusCostValue);

        // differential cost value has to be allocated based on which qty increased
        if (billedStockQty.compareTo(BigDecimal.ZERO) > 0
            && bonusStockQty.compareTo(BigDecimal.ZERO) > 0) {
          // both quantities increased, allocate billed to billed and bonus to bonus
          billedCostValue = billedCostValue.subtract(existingBilledCostValue);
          bonusCostValue = bonusCostValue.subtract(existingBonusCostValue);
        } else if (billedStockQty.compareTo(BigDecimal.ZERO) > 0) {
          // all extra cost to billed qty
          billedCostValue = ((BigDecimal) medicine.get("med_total"))
              .subtract(existingTotalCostValue);
          bonusCostValue = BigDecimal.ZERO;
        } else if (bonusStockQty.compareTo(BigDecimal.ZERO) > 0) {
          // all extra cost to bonus qty
          bonusCostValue = ((BigDecimal) medicine.get("med_total"))
              .subtract(existingTotalCostValue);
          billedCostValue = BigDecimal.ZERO;
        }
      }

      String stockSource = existingGdBean != null ? "E" : "S";

      if (bonusStockQty.compareTo(BigDecimal.ZERO) > 0) {
        BasicDynaBean bonusStock = storeStockDetailsRepository.getBean();
        ConversionUtils.copyToDynaBean(medicine, bonusStock);

        // common for all types of stock change
        bonusStock.set("stock_time", DateUtil.getCurrentTimestamp());
        bonusStock.set("username", username);
        bonusStock.set("change_source", "StockEntry");
        bonusStock.set("received_date", DateUtil.getCurrentDate());
        bonusStock.set("last_cp_grn", grnNo);
        bonusStock.set("item_ced_amt", gdBean.get("item_ced"));

        // Set the new batch attributes and insert
        bonusStock.set("dept_id", storeId);
        bonusStock.set("package_sp", gdBean.get("adj_mrp"));
        bonusStock.set("stock_pkg_size", gdBean.get("grn_pkg_size"));
        bonusStock.set("package_uom", gdBean.get("grn_package_uom"));

        bonusStock.set("item_supplier_name", supplierName);
        bonusStock.set("item_supplier_code", supplierId);
        bonusStock.set("item_invoice_no", invBean.get("invoice_no"));
        bonusStock.set("item_grn_no", grnNo);
        bonusStock.set("max_cp_grn", grnNo);
        bonusStock.set("qty", bonusStockQty);
        bonusStock.set("tax", gdBean.get("bonus_tax"));
        BigDecimal pkgCp = ConversionUtils.divideHighPrecision(bonusCostValue.multiply(pkgSize),
            bonusStockQty);

        BasicDynaBean bonusLot = itemLotBean;
        bonusLot.set("package_cp", pkgCp);
        bonusLot.set("grn_no", grnNo);
        bonusLot.set("item_batch_id", itemBatchId);
        itemLotBean.set("lot_source", stockSource);
        itemLotBean.set("purchase_type", "B");
        storeItemLotDetailsService.insert(bonusLot);

        bonusStock.set("package_cp", pkgCp);
        bonusStock.set("item_lot_id", bonusLot.get("item_lot_id"));
        bonusStock.set("item_batch_id", itemBatchId);
        bonusStock.set("tax", gdBean.get("bonus_tax"));
        storeStockDetailsRepository.insert(bonusStock);

      }

      if (billedStockQty.compareTo(BigDecimal.ZERO) > 0) {
        stockBean.set("qty", billedStockQty);
        // package_cp = cost_value*pkgSize/qty (high precision due to bug 30643).
        BigDecimal pkgCp = ConversionUtils.divideHighPrecision(billedCostValue.multiply(pkgSize),
            billedStockQty);

        itemLotBean.set("package_cp", pkgCp);
        itemLotBean.set("grn_no", grnNo);
        itemLotBean.set("item_batch_id", itemBatchId);
        itemLotBean.set("lot_source", stockSource);
        itemLotBean.set("purchase_type", "S");
        storeItemLotDetailsService.insert(itemLotBean);
        
        stockBean.set("item_batch_id", itemBatchId);
        stockBean.set("item_lot_id", itemLotBean.get("item_lot_id"));
        stockBean.set("package_cp", pkgCp);
        storeStockDetailsRepository.insert(stockBean);
      }

    }

    stockFifoService.updateStockTimeStamp();
    stockFifoService.updateStoresStockTimeStamp(storeId);

  }

  @Override
  @Transactional
  public String importCsv(InputStreamReader csvStreamReader,
      Map<String, MultiValueMap<Object, Object>> feedback) throws IOException {
    MultiValueMap<Object, Object> warnings = new LinkedMultiValueMap<Object, Object>();

    CSVReader csvReader = new CSVReader(csvStreamReader);
    String[] headers = csvReader.readNext();

    if (headers.length < 1) {
      return MISSINGHEADERSKEY;
    }

    if (!headers[0].matches("\\p{Print}*")) {
      return NONPRINTABLEHEADERSKEY;
    }

    if (headers.length == 1) {
      return NONCOMMADELIMITERKEY;
    }

    boolean[] ignoreColumn = new boolean[headers.length];
    Integer lineNumber = 0;
    // Integer insertionCount = 0;
    // Integer updationCount = 0; //TODO

    for (int i = 0; i < headers.length; i++) {
      if (ArrayUtils.contains(COLUMNS, headers[i])) {
        ignoreColumn[i] = false;

      } else {
        ignoreColumn[i] = true;
        addWarning(warnings, lineNumber, UNKNOWNHEADERKEY, headers[i]);
      }
    }
    lineNumber++;
    Map<String, Map> grns = new LinkedHashMap<>();
    String[] row;
    while (null != (row = csvReader.readNext())) {
      lineNumber++;
      boolean rowFailed = false;
      Map<String, Object> grn = new HashMap<>();
      grn.put("username", Constants.API_USERNAME);

      if (row.length != headers.length) {
        continue;
      }

      for (int i = 0; i < row.length; i++) {
        if (ignoreColumn[i]) {
          continue;
        }
        switch (headers[i]) {
          case "integration_grn_id":
            grn.put("integration_grn_id", row[i]);
            break;
          case "store_name":
            BasicDynaBean storeBean = storeService.findByUniqueName(row[i], "dept_name");
            if (storeBean == null) {
              addWarning(warnings, lineNumber, "Invalid store name", null);
              rowFailed = true;
              break;
            }
            grn.put("store_id", storeBean.get("dept_id").toString());
            break;
          case "supplier_name":
            BasicDynaBean supplierBean = supplierService.findByUniqueName(row[i], "supplier_name");
            if (supplierBean == null) {
              addWarning(warnings, lineNumber, "Invalid supplier", null);
              rowFailed = true;
              break;
            }
            grn.put("supplier_id", supplierBean.get("supplier_code").toString());
            break;
          case "grn_date":
            try {
              Date grnDate = DATE_FORMAT.parse(row[i]);
              grn.put("grn_date", grnDate);
            } catch (ParseException ex) {
              logger.debug("Failed to parse date", ex);
              addWarning(warnings, lineNumber, "Invalid grn date " + row[i], null);
              rowFailed = true;
            }
            break;
          case "due_date":
            try {
              Date grnDate = DATE_FORMAT.parse(row[i]);
              grn.put("due_date", grnDate);
            } catch (ParseException ex) {
              logger.debug("Failed to parse date", ex);
              addWarning(warnings, lineNumber, "Invalid due date " + row[i], null);
              rowFailed = true;
            }
            break;
          case "po_number":
            grn.put("po_number", row[i]);

            break;
          case "invoice_date":
            try {
              Date invoiceDate = DATE_FORMAT.parse(row[i]);
              grn.put("invoice_date", invoiceDate);
            } catch (ParseException ex) {
              logger.debug("Failed to parse date", ex);
              addWarning(warnings, lineNumber, "Invalid invoice date " + row[i], null);
              rowFailed = true;
            }
            break;
          case "invoice_number":
            grn.put("invoice_no", row[i]);
            break;
          case "consignment_stock":
            grn.put("consignment_stock", row[i].equalsIgnoreCase("true") ? "true" : "false");
            break;
          case "grn_qty_unit":
            if (row[i].equals("P") || row[i].equals("I")) {
              grn.put("grn_qty_unit", row[i]);
            } else {
              addWarning(warnings, lineNumber, "Invalid value for grn_qty_unit " + row[i], null);
              rowFailed = true;
            }
            break;
          case "purpose_of_purchase":
            grn.put("purpose_of_purchase", row[i]);
            break;
          case "item_id":
            BasicDynaBean itemBean = storeItemDetailsIntegrationService.findByIntegrationId(row[i]);
            if (itemBean == null) {
              addWarning(warnings, lineNumber, "Invalid item id " + row[i], null);
              rowFailed = true;
            } else {
              grn.put("medicine_id", itemBean.get("medicine_id").toString());
            }
            break;
          case "item_batch_no":
            grn.put("item_batch_no", row[i]);
            break;
          case "exp_dt":
            try {
              String[] expDtParts = row[i].split("/");
              grn.put("exp_dt", DateUtil.getLastDayInMonth(
                    Integer.parseInt(expDtParts[0]), Integer.parseInt(expDtParts[1])));
            } catch (NumberFormatException ex) {
              logger.debug("Invalid expiry date", ex);
              rowFailed = true;
              addWarning(warnings, lineNumber, "Invalid expiry", null);
            }
            break;
          case "tax_sub_groups":
            int taxIndex = ArrayUtils.indexOf(headers, "tax_amount");
            String[] taxSubgroups = row[i].split(",");
            String[] taxAmounts = row[taxIndex].split(",");
            if (taxSubgroups.length != taxAmounts.length) {
              rowFailed = true;
              addWarning(warnings, lineNumber, "Length of taxSubgroups and taxes should be same",
                  null);
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
                addWarning(warnings, lineNumber, "Invalid taxSubgroup " + taxSubgroups[j], null);
                continue;
              }
              if (!NumberUtils.isParsable(taxAmounts[j])) {
                addWarning(warnings, lineNumber, "Tax is not valid " + taxAmounts[j], null);
                rowFailed = true;
                continue;
              }

              grn.put("taxrate" + taxSubgroupBean.get("item_group_id"),
                  taxSubgroupBean.get("tax_rate").toString());
              grn.put("taxamount" + taxSubgroupBean.get("item_group_id"), taxAmounts[j]);
              grn.put("taxsubgroupid" + taxSubgroupBean.get("item_group_id"),
                  taxSubgroupBean.get("item_subgroup_id").toString());

            }
            break;
          case "qty":
            grn.put("billed_qty", row[i]);
            break;
          case "bill_discount":
            grn.put("inv_discount", StringUtils.isNotEmpty(row[i]) ? row[i] : "0");
            break;
          default:
            grn.put(headers[i], row[i]);
            break;
        }
      }
      grn.put("status", "F");

      if (!rowFailed) {
        if (grns.keySet().contains(grn.get("integration_grn_id"))) {
          Map existingGrn = grns.get(grn.get("integration_grn_id"));
          ((List<Map<String, Object>>) existingGrn.get("medicines")).add(getMedicine(grn));
        } else {
          List<Map> medicines = new ArrayList<>();
          medicines.add(getMedicine(grn));
          grn.put("medicines", medicines);
          grns.put((String) grn.get("integration_grn_id"), grn);
        }
      }
    }

    for (String grnNo : grns.keySet()) {
      insertStock(grns.get(grnNo));
    }

    feedback.put("warnings", warnings);
    return null;
  }

  private Map<String, Object> getMedicine(Map<String, Object> grn) {
    Map<String, Object> medicine = new HashMap<>();
    BigDecimal totalTaxAmount = BigDecimal.ZERO;
    BigDecimal totalTaxRate = BigDecimal.ZERO;
    medicine.put("medicine_id", grn.get("medicine_id"));
    medicine.put("batch_no", grn.get("item_batch_no"));
    medicine.put("mrp", grn.get("mrp"));
    medicine.put("bonus_tax", grn.get("bonus_tax") == null ? "0" : grn.get("bonus_tax"));
    medicine.put("grn_med", "N");
    medicine.put("exp_dt", grn.get("exp_dt"));
    medicine.put("grn_pkg_size", "1");
    medicine.put("cost_price", grn.get("cost_price"));
    medicine.put("tax_type", "CB");
    medicine.put("billed_qty", grn.get("billed_qty"));
    medicine.put("bonus_qty", grn.get("bonus_qty"));
    medicine.put("discount", grn.get("item_discount"));

    for (String key : grn.keySet()) {
      if (key.startsWith("taxamount") || key.startsWith("taxrate")
          || key.startsWith("taxsubgroupid")) {
        if (key.startsWith("taxamount")) {
          totalTaxAmount = totalTaxAmount.add(new BigDecimal((String) grn.get(key)));
        }
        if (key.startsWith("taxrate")) {
          totalTaxRate = totalTaxRate.add(new BigDecimal((String) grn.get(key)));
        }
        medicine.put(key, grn.get(key));
      }
    }


    BigDecimal medTotal = totalTaxAmount
        .divide(new BigDecimal((String) grn.get("billed_qty")), 4, BigDecimal.ROUND_HALF_EVEN)
        .add(new BigDecimal((String) (grn.get("cost_price"))));

    medicine.put("tax", totalTaxAmount.toString());
    medicine.put("tax_rate", totalTaxRate.toString());
    medicine.put("med_total", medTotal.toString());
    medicine.put("item_ced", "0");
    medicine.put("item_ced_per", "0");
    return medicine;

  }

  private void addWarning(MultiValueMap<Object, Object> warnings, Integer lineNumber,
      String message, Object... parameters) {
    StringBuilder warning = new StringBuilder();
    warning.append(message);
    warnings.add(lineNumber, warning.toString());
  }

  private void createInvoiceGrn(Map<String, Object> params, BasicDynaBean invBean,
      BasicDynaBean grnBean) {

    ConversionUtils.copyToDynaBean(params, invBean);
    ConversionUtils.copyToDynaBean(params, grnBean);
    // some parameters are prefixed with inv_ to avoid conflict with grn_details
    ConversionUtils.copyToDynaBean(params, invBean, "inv_");

    String poNo = (String) invBean.get("po_no");

    invBean.set("date_time", DateUtil.getCurrentTimestamp());
    invBean.set("cst_rate", BigDecimal.ZERO);
    int suppInvId = storeInvoiceRepository.getNextSequence();
    invBean.set("supplier_invoice_id", suppInvId);

    int storeId = (Integer) grnBean.get("store_id");
    BasicDynaBean storebean = storeService.findByPk(Collections.singletonMap("dept_id", storeId));
    int storeacGroup = (Integer) storebean.get("account_group");
    invBean.set("account_group", storeacGroup);

    storeInvoiceRepository.insert(invBean);
    grnBean.set("grn_date", DateUtil.getCurrentTimestamp());
    grnBean.set("user_name", params.get("username"));
    grnBean.set("supplier_invoice_id", suppInvId);

    String cashPurchase = null; // TODO Handle cash purchase
    String grNo = stockRepository.getNextId("" + storeId, cashPurchase);
    grnBean.set("grn_no", grNo);

    mainRepo.insert(grnBean);
  }

  /**
   * Get medicine names by medicine ids.
   * @param medicineIds medicine ids
   * @return map of medicine ids and corresponding medicine name
   */
  public Map<String, String> getMedicineNamesByMedicineIds(List<Integer> medicineIds) {
    Map<String, String> nameMap = new HashMap<>();
    if (CollectionUtils.isNotEmpty(medicineIds)) {
      List<BasicDynaBean> medicines =
          this.stockRepository.getMedicineNamesByMedicineIds(medicineIds);
      if (CollectionUtils.isNotEmpty(medicines)) {
        for (BasicDynaBean bean : medicines) {
          nameMap.put(bean.get("medicine_id").toString(), (String) bean.get("medicine_name"));
        }
      }
    }
    return nameMap;
  }
}
