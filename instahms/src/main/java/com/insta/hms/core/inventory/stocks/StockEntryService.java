package com.insta.hms.core.inventory.stocks;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.BusinessService;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.inventory.stockmgmt.StockRepository;
import com.insta.hms.core.inventory.taxation.PurchaseTaxCalculatorSupport;
import com.insta.hms.mdm.storeitembatchdetails.StoreItemBatchDetailsService;
import com.insta.hms.mdm.storeitemlotdetails.StoreItemLotDetailsService;
import com.insta.hms.mdm.stores.StoreService;
import com.insta.hms.mdm.supplier.SupplierService;
import com.insta.hms.mdm.taxgroups.TaxGroupService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import flexjson.JSONSerializer;

@Service
public class StockEntryService extends BusinessService {

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

  private static final Logger logger = LoggerFactory.getLogger(StockEntryService.class);
  private static final Map<String, String> GRN_DETAIL_FIELD_ALIAS_MAP = new HashMap<>();
  private static final Map<String, String> GRN_MAIN_FIELD_ALIAS_MAP = new HashMap<>();
  private static final Map<String, String> INOVICE_FIELD_ALIAS_MAP = new HashMap<>();

  static {
    GRN_DETAIL_FIELD_ALIAS_MAP.put("cost_price", "cost_price_display");
    GRN_DETAIL_FIELD_ALIAS_MAP.put("mrp", "mrp_display");
    GRN_DETAIL_FIELD_ALIAS_MAP.put("billed_qty", "billed_qty_display");
    GRN_DETAIL_FIELD_ALIAS_MAP.put("bonus_qty", "bonus_qty_display");
    GRN_DETAIL_FIELD_ALIAS_MAP.put("grn_pkg_size", "grn_pkg_size");
    GRN_DETAIL_FIELD_ALIAS_MAP.put("tax_type", "tax_type");
    GRN_DETAIL_FIELD_ALIAS_MAP.put("discount", "discount");
    GRN_DETAIL_FIELD_ALIAS_MAP.put("scheme_discount", "scheme_discount");

    GRN_MAIN_FIELD_ALIAS_MAP.put("store_id", "store_id_hid");
    GRN_MAIN_FIELD_ALIAS_MAP.put("grn_qty_unit", "store_package_uom");

    INOVICE_FIELD_ALIAS_MAP.put("supplier_id", "supplier_code_hid");
  }

  public BasicDynaBean toGRNMainBean(Map<String, String[]> reqMap) {
    BasicDynaBean grnMain = mainRepo.getBean();
    ConversionUtils.copyToDynaBean(reqMap, grnMain, null, true, null, GRN_MAIN_FIELD_ALIAS_MAP);
    return grnMain;
  }

  public BasicDynaBean toGRNDetailsBean(Map<String, String[]> reqMap) {
    BasicDynaBean grnDetails = storeGRNDetailsRepository.getBean();
    ConversionUtils.copyToDynaBean(reqMap, grnDetails, null, true, null,
        GRN_DETAIL_FIELD_ALIAS_MAP);
    return grnDetails;
  }

  public BasicDynaBean toInvoiceBean(Map<String, String[]> reqMap) {
    BasicDynaBean invoiceBean = storeInvoiceRepository.getBean();
    ConversionUtils.copyToDynaBean(reqMap, invoiceBean, null, true, null, INOVICE_FIELD_ALIAS_MAP);
    return invoiceBean;
  }

  public Map<String, Object> getTaxDetails(BasicDynaBean grnMain, BasicDynaBean grnDetails,
      BasicDynaBean invoiceDetails) throws Exception {
    return purchaseTaxCalculatorSupport.getTaxDetails(grnMain, grnDetails, invoiceDetails, null);
  }
  
  public Map<String, Object> onChangeTaxDetails(BasicDynaBean grnMain,
      BasicDynaBean grnDetails,
      BasicDynaBean invoiceDetails, Integer[] subGroupOverrides) throws Exception {
    return purchaseTaxCalculatorSupport.onChangeTaxDetails(grnMain, grnDetails, invoiceDetails,
        subGroupOverrides);
  }

  public Map<String, Object> getDebitNoteTaxDetails(BasicDynaBean grnMain, BasicDynaBean grnDetails,
      BasicDynaBean invoiceDetails, Integer[] subGroups) throws Exception {
    return purchaseTaxCalculatorSupport.getTaxDetails(grnMain, grnDetails, invoiceDetails,
        subGroups);
  }

  private void createInvoiceGrn(Map<String, Object> params, BasicDynaBean invBean,
      BasicDynaBean grnBean) {

    // String mainCST = getParameter(requestParams, "main_cst_rate"); //NOTUSED
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

    // new GenericDAO("store_invoice").insert(con, invBean);
    storeInvoiceRepository.insert(invBean);
    grnBean.set("grn_date", DateUtil.getCurrentTimestamp());
    grnBean.set("user_name", params.get("username"));
    grnBean.set("supplier_invoice_id", suppInvId);

    String cashPurchase = null; // TODO Handle cash purchase
    String gNo = stockRepository.getNextId("" + storeId, cashPurchase);
    grnBean.set("grn_no", gNo);

    // new GenericDAO("store_grn_main").insert(con, grnBean);
    mainRepo.insert(grnBean);
  }

  /**
   * Gets the grn items.
   *
   * @param grnNo           the grn no
   * @param healthAuthority the health authority
   * @return the grn items
   */
  public List<BasicDynaBean> getGrnItems(String grnNo) {
    return storeStockDetailsRepository.getGrnItems(grnNo);
  }

  /**
   * Gets the grn details.
   *
   * @param grnNo the grn no
   * @return the grn details
   */
  public BasicDynaBean getGrnDetails(String grnNo) {
    return storeStockDetailsRepository.getGrnDetails(grnNo);
  }

  public ResponseEntity<Map<String, Object>> getGrnItemsAndDetails(String grnNo) {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    if (grnNo != null) {
      JSONSerializer js = new JSONSerializer().exclude("class");
      BasicDynaBean grnDetails = getGrnDetails(grnNo);
      List<BasicDynaBean> grnItems = getGrnItems(grnNo);
      List grnItemsMaplist = ConversionUtils
          .copyListDynaBeansToMap(grnItems);

      if (grnDetails != null && CollectionUtils.isNotEmpty(grnItemsMaplist)) {

        int storeId = (int) grnDetails.get("store_id");
        Map<String, Object> stockAvailabelity = new HashMap<String, Object>();
        Iterator<Map<String, Object>> grnItemsItr = grnItemsMaplist.iterator();

        while (grnItemsItr.hasNext()) {

          Map<String, Object> item = grnItemsItr.next();
          int medicineId = (Integer) item.get("medicine_id");
          int itemBatchId = (Integer) item.get("item_batch_id");

          BigDecimal availableQty = new BigDecimal(storeStockDetailsRepository.getQuantity(storeId,
              medicineId, itemBatchId, BigDecimal.ZERO).get("qty").toString());

          BigDecimal requiredQty = new BigDecimal(item.get("bonus_qty").toString())
              .add(new BigDecimal(item.get("billed_qty").toString()));

          if (availableQty.compareTo(requiredQty) < 0) {
            stockAvailabelity.put(item.get("item_batch_id").toString(), "N");
          } else {
            stockAvailabelity.put(item.get("item_batch_id").toString(), "Y");
          }
        }
        responseMap.put("stock_availabelity", stockAvailabelity);
        responseMap.put("grn_details", grnDetails.getMap());
        responseMap.put("grn_items", js.serialize(grnItemsMaplist));
      } else {
    	  return null;
      }
    }
    return new ResponseEntity<Map<String, Object>>(responseMap,
        HttpStatus.OK);
  }

}
