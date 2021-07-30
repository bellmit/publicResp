package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.NumberToWordFormat;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.core.inventory.StoresHelper;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.FixedAssetMaster.FixedAssetMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.PackageUOM.PackageUOMDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.SupplierContractsItem.SupplierContractsItemRateDAO;
import com.insta.hms.usermanager.UserDAO;
import com.lowagie.text.DocumentException;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class StockEntryAction extends BaseAction {

	static Logger log = LoggerFactory.getLogger(StockEntryAction.class);
	private static GenericDAO grnPrintTemplateDao = new GenericDAO("grn_print_template");
	private static final GenericDAO storesDAO = new GenericDAO("stores");

	/*
	 * Stock Entry Screen: same screen is called from multiple places:
	 *  Direct Stock Entry (from main menu)
	 *  From PO -> doing stock entry for the PO
	 *  Edit GRN -> add to an existing GRN and/or change status
	 */
  @IgnoreConfidentialFilters
  public ActionForward getScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    JSONSerializer js = new JSONSerializer().exclude("class");

    // last sequence for identified (serial number) stock
    String countQuery = "SELECT last_value FROM store_seq";
    String seqCount = DataBaseUtil.getStringValueFromDb(countQuery);
    request.setAttribute("seqCount", seqCount);
    int userCenterId = (int) new GenericDAO("u_user")
        .findByKey("emp_username", request.getSession(false).getAttribute("userId"))
        .get("center_id");
    request.setAttribute("userCenterId", userCenterId);

    int centerId = (Integer) request.getSession(false).getAttribute("centerId");
    String healthAuthorityForCenter = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
    String healthAuthority =
        HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthorityForCenter)
            .getHealth_authority();
    String grnNum = request.getParameter("grNo");
    String poNum = request.getParameter("poNo");

    if (grnNum != null && !grnNum.equals("")) {
      // edit GRN
      BasicDynaBean grnMain = StockEntryDAO.getGrnDetails(grnNum);
      request.setAttribute("inv", grnMain);

      List<BasicDynaBean> grnItems = StockEntryDAO.getGrnItems(grnNum, healthAuthority);
      request
          .setAttribute("grnItemsJSON", js.serialize(ConversionUtils.listBeanToListMap(grnItems)));

      List<BasicDynaBean> batches = StockEntryDAO.getItemsInGrnBatches(grnNum);
      Map itemBatches = ConversionUtils.listBeanToMapListMap(batches, "medicine_id");
      request.setAttribute("grnItemBatchesJSON", js.deepSerialize(itemBatches));

      request.setAttribute("grn", request.getParameter("grNo"));
      poNum = (String) grnMain.get("po_no");
      request.setAttribute("editGRN", true);

      // need for checking invoice number on edit
      DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
      java.util.Date grnDate  = df.parse((String) grnMain.get("invoice_date"));
      java.sql.Date[] dt = DateUtil.getCustomDateFinYearsDateRange(grnDate);
      
      List invoiceNos = StockEntryDAO.getInvoiceNos((String) grnMain.get("supplier_id"), dt[0].toString(), dt[1].toString());
		      request.setAttribute("suppInvoicesJSON", js.serialize(
          ConversionUtils.listBeanToListMap(invoiceNos)));
      List<BasicDynaBean> grnTaxDetails = StockEntryDAO.getGRNTaxDetails(grnNum);
      request.setAttribute("grn_tax_details",
          js.serialize(ConversionUtils.listBeanToListMap(grnTaxDetails)));

    } else {
      request.setAttribute("editGRN", false);
    }

    if (poNum != null && !poNum.equals("")) {
      request.setAttribute("ponum", poNum);
      request.setAttribute("poStoreId", request.getParameter("poStoreId"));
    }
    request.setAttribute("centerId", centerId);

    request.setAttribute("genPrefs", GenericPreferencesDAO.getPrefsBean().getMap());

    request.setAttribute("master_timestamp", PharmacymasterDAO.getItemMasterTimestamp());

    GenericDAO defaultControlType = new GenericDAO("store_item_controltype");
    List<BasicDynaBean> sugGroupList = PurchaseOrderDAO.getAllSubGroups();
    List<BasicDynaBean> groupList = PurchaseOrderDAO.getAllGroups();

    request.setAttribute("printPref",
        PrintConfigurationsDAO.getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_STORE));
    request.setAttribute("subGroupListJSON",
        js.serialize(ConversionUtils.listBeanToListMap(sugGroupList)));
    request.setAttribute("groupList", ConversionUtils.listBeanToListMap(groupList));
    request
        .setAttribute("groupListJSON", js.serialize(ConversionUtils.listBeanToListMap(groupList)));
    request.setAttribute("packageUOMList", js.serialize(ConversionUtils.listBeanToListMap(
        new PackageUOMDAO().listAll("package_uom"))));
    request.setAttribute("grn_count", (String) request.getParameter("grn_count"));
    if (userCenterId > 0) {
      request.setAttribute("storesListJSON",
          js.serialize(ConversionUtils.listBeanToListMap(storesDAO.listAll(null,"center_id",userCenterId))));
    } else {
      request.setAttribute("storesListJSON",
          js.serialize(ConversionUtils.listBeanToListMap(storesDAO.listAll())));
    }
    request.setAttribute("listAllcentersforAGRN",
        js.deepSerialize(PurchaseOrderDAO.listAllcentersforAPo(centerId)));
    request.setAttribute("grnPrintTemplates", ConversionUtils.listBeanToListMap(grnPrintTemplateDao
        .listAll(Arrays.asList("template_id", "template_name"))));
    return mapping.findForward("stockEntry");
  }

  /*
   * Get the items and details that are in a PO as JSON: used to load the PO
   * into the stock entry screen.
   */
  @IgnoreConfidentialFilters
  public ActionForward getPOItems(ActionMapping mappinf, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    JSONSerializer js = new JSONSerializer().exclude("class");
    HashMap result = new HashMap();

    int storeId = Integer.parseInt(req.getParameter("store"));
    String poNo = req.getParameter("po_no");
    String supplierId = req.getParameter("supplierId");
    BasicDynaBean centerBean = UserDAO.getCenterId(storeId);
    int centerId = (Integer) centerBean.get("center_id");
    String healthAuthorityForCenter = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
    String healthAuthority =
        HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthorityForCenter)
            .getHealth_authority();

    List items = StockEntryDAO.getPOItems(poNo, healthAuthority);
    List itemTaxDetails = PurchaseOrderDAO.getPOTaxDetails(poNo);

		/*SupplierContractsItemRateDAO supContractItemDao = new SupplierContractsItemRateDAO();
		List maplist = new ArrayList();
		if(items != null){
			for (Object object : items) {
					BasicDynaBean bean = (BasicDynaBean)object;
					Map<String, Object> mapBean = new HashMap<String, Object>(bean.getMap());
					BasicDynaBean supplierRateContractBean = supContractItemDao.getSupplierItemRateValue(mapBean.get("medicine_id"),supplierId,centerId);
					if(supplierRateContractBean != null) {
						mapBean.put("max_cost_price", supplierRateContractBean.get("supplier_rate").toString());
						mapBean.put("supplier_rate_validation", "true");
					}
					maplist.add(mapBean);
			}
		}*/

    result.put("poItems", ConversionUtils.listBeanToListMap(items));
    result.put("poTaxDetails", ConversionUtils.listBeanToListMap(itemTaxDetails));

    List batches = StockEntryDAO.getItemsInPoBatches(poNo);
    Map itemBatches = ConversionUtils.listBeanToMapListMap(batches, "medicine_id");
    result.put("poItemBatches", itemBatches);

    res.setContentType("application/x-json");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.setHeader("Expires", "0");
    res.getWriter().write(js.deepSerialize(result));
    res.flushBuffer();
    return null;
  }

  @IgnoreConfidentialFilters
  public ActionForward getItemDetails(ActionMapping am, ActionForm af,
      HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException, SQLException, ParseException {

    JSONSerializer js = new JSONSerializer().exclude("class");
    int medicineId = Integer.parseInt(req.getParameter("medicineId"));
    int storeId = Integer.parseInt(req.getParameter("store"));
    String supplierId = req.getParameter("supplierId");
    BasicDynaBean centerBean = UserDAO.getCenterId(storeId);
    int centerId = (Integer) centerBean.get("center_id");
    List centerStores = new StockEntryAction().getAllStores(centerId);
    String healthAuthorityForCenter = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
    String healthAuthority =
        HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthorityForCenter)
            .getHealth_authority();


    BasicDynaBean item = StockEntryDAO
        .getItemDetails(medicineId, storeId, supplierId, centerStores, healthAuthority);
    Map resultMap = new HashMap(item.getMap());

    // item batches
    List<BasicDynaBean> batches = StockEntryDAO.getItemBatchesDetails(storeId, medicineId);
    resultMap.put("batches", ConversionUtils.listBeanToListMap(batches));

    SupplierContractsItemRateDAO supContractItemDao = new SupplierContractsItemRateDAO();
    List<BasicDynaBean> suppRateList =
        supContractItemDao.getSupplierItemRateValue(medicineId, supplierId, centerId);
    if (item != null) {
      if (suppRateList != null && suppRateList.size() > 0) {

        for (BasicDynaBean supplierRate : suppRateList) {
          Object cp = supplierRate.get("discount");
          if (cp != null) {
            resultMap.put("cost_price", cp);
            break;
          } else {
            continue;
          }
        }
        for (BasicDynaBean supplierRate : suppRateList) {
          Object dis = supplierRate.get("discount");
			if (dis != null) {
				resultMap.put("discount", new BigDecimal(dis.toString()));
				break;
			} else {
				continue;
			}
        }
        for (BasicDynaBean supplierRate : suppRateList) {
          Object mrp = supplierRate.get("mrp");
			if (mrp != null) {
				resultMap.put("mrp", new BigDecimal(mrp.toString()));
				break;
			} else {
				continue;
			}
        }
        resultMap.put("supplier_rate_validation", "true");
      }
    }

    res.setContentType("application/x-json");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.setHeader("Expires", "0");
    res.getWriter().write(js.deepSerialize(resultMap));
    res.flushBuffer();
    return null;
  }

  @IgnoreConfidentialFilters
  public ActionForward getPurchaseDetails(ActionMapping am, ActionForm af,
      HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException, SQLException {

    JSONSerializer js = new JSONSerializer().exclude("class");
    String itemIdStr = req.getParameter("itemId");
    int itemId = Integer.parseInt(itemIdStr);

    int storeId = Integer.parseInt(req.getParameter("storeId"));
    BasicDynaBean centerBean = UserDAO.getCenterId(storeId);
    int centerId = (Integer) centerBean.get("center_id");
    List centerStores = getAllStores(centerId);

    List<BasicDynaBean> purdet = StockEntryDAO.getPurchaseDetails(itemId, centerStores);
    String purchase = js.serialize(ConversionUtils.listBeanToListMap(purdet));
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.setHeader("Expires", "0");
    res.setContentType("text/plain");
    res.getWriter().write(purchase);
    res.flushBuffer();
    return null;
  }

  /* To get all stores of a center
   */
  @IgnoreConfidentialFilters
  public List getAllStores(int centerId) throws IOException, ServletException, SQLException {
    List<BasicDynaBean> centerStoresBean = null;
    List<Integer> storeList = new ArrayList<Integer>();

    centerStoresBean = UserDAO.getAllStoreOfCenter(centerId);
    if (centerStoresBean != null) {
      for (BasicDynaBean bean : centerStoresBean) {
        storeList.add((Integer) bean.get("dept_id"));
      }
    }
    return storeList;
  }

  private void createInvoiceGrn(Connection con, Map requestParams,
      BasicDynaBean invBean, BasicDynaBean grnBean, String username, String cashPurchase)
      throws Exception {

    String mainCST = getParameter(requestParams, "main_cst_rate");
    ConversionUtils.copyToDynaBean(requestParams, invBean);
    ConversionUtils.copyToDynaBean(requestParams, grnBean);
    // some parameters are prefixed with inv_ to avoid conflict with grn_details
    ConversionUtils.copyToDynaBean(requestParams, invBean, "inv_");

    String poNo = (String) invBean.get("po_no");

    invBean.set("date_time", DateUtil.getCurrentTimestamp());
    invBean.set("cst_rate", BigDecimal.ZERO);
    int suppInvId = DataBaseUtil.getNextSequence("invoice_seq");
    invBean.set("supplier_invoice_id", suppInvId);

    int storeId = (Integer) grnBean.get("store_id");
    BasicDynaBean storebean = new GenericDAO("stores").findByKey("dept_id", storeId);
    int storeacGroup = (Integer) storebean.get("account_group");
    invBean.set("account_group", storeacGroup);
    String tcsPer = getParameter(requestParams, "inv_tcs_per");
    String tcsType = getParameter(requestParams, "tcs_type");
    if(null != tcsType && tcsType.equalsIgnoreCase("P")) {
      invBean.set("tcs_per", new BigDecimal(tcsPer));
    }

    new GenericDAO("store_invoice").insert(con, invBean);

    grnBean.set("grn_date", DateUtil.getCurrentTimestamp());
    grnBean.set("user_name", username);
    grnBean.set("supplier_invoice_id", suppInvId);

    String gNo = StockEntryDAO.getNextId("" + storeId, cashPurchase);
    grnBean.set("grn_no", gNo);

    new GenericDAO("store_grn_main").insert(con, grnBean);
  }

  @IgnoreConfidentialFilters
  public ActionForward insertInvoiceStock(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse response) throws Exception {

    Map paramMap = getParameterMap(req);
    String grnNo = getParameter(paramMap, "grn_no");
    String action = getParameter(paramMap, "saveAction");

    ActionRedirect redirect = null;
    String msg = "Stock Insertion Failed";
    HttpSession session = req.getSession(false);
    String username = (String) session.getAttribute("userid");
    Connection con = null;
    boolean success = false;
    boolean newGrn = false;
    ArrayList<String> printUrls = new ArrayList<String>();

    String vatORcst = getParameter(paramMap, "tax_name");
	  if (vatORcst == null) {
		  vatORcst = "VAT";
	  }
    String storeIdStr = getParameter(paramMap, "store_id");
    int storeId = Integer.parseInt(storeIdStr);
    String supplierId = getParameter(paramMap, "supplier_id");
    String supplierName = StoresDBTablesUtil.suppIdToName(supplierId);
    String poNo = getParameter(paramMap, "po_no");
    String cashPurchase = getParameter(paramMap, "cash_purchase");    // spl handling for checkbox
    String paymentRemarks = getParameter(paramMap, "payment_remarks");
    String mainCST = getParameter(paramMap, "main_cst_rate");
    String errorMsg = null;

    String[] grnmed = (String[]) paramMap.get("grnmed");
    String[] medicine_id = (String[]) paramMap.get("medicine_id");
    String[] batch_no = (String[]) paramMap.get("batch_no");
    String[] mrp_array = (String[]) paramMap.get("mrp");
    String[] cstRate = (String[]) paramMap.get("cst_rate");
    String[] consignment_stock = (String[]) paramMap.get("consignment_stock");
    String[] billed_qty = (String[]) paramMap.get("billed_qty");
    String[] bonus_qty = (String[]) paramMap.get("bonus_qty");
    int elelen = medicine_id.length - 1;
    BigDecimal[] medTotal = StoresDBTablesUtil.copyStringArrayTOBigdecimal(elelen,
        paramMap.get("med_total"));
    BigDecimal[] bonusTax = StoresDBTablesUtil.copyStringArrayTOBigdecimal(elelen,
        paramMap.get("bonus_tax"));
    BigDecimal totalQty = BigDecimal.ZERO;
    BigDecimal totalBonusQty = BigDecimal.ZERO;


    GenericDAO gmDao = new GenericDAO("store_grn_main");
    GenericDAO invDao = new GenericDAO("store_invoice");
    GenericDAO gdDao = new GenericDAO("store_grn_details");
    GenericDAO stockDao = new GenericDAO("store_stock_details");
    GenericDAO itemBatchDAO = new GenericDAO("store_item_batch_details");
    GenericDAO itemLotDAO = new GenericDAO("store_item_lot_details");
    GenericDAO itemMasterDAO = new GenericDAO("store_item_details");
    GenericDAO storeGrnTaxDAO = new GenericDAO("store_grn_tax_details");
    GenericDAO storePODAO = new GenericDAO("store_po");


    String[] identifierType = (String[]) paramMap.get("identification");
    String barCodePref = GenericPreferencesDAO.getGenericPreferences().getBarcodeForItem();

    tryBlock:
    try {
      /* Validation on item quantity */
      if (poNo != null && !poNo.equals("")) {
        List<BasicDynaBean> existingGrnBean =
            gmDao.findAllByKey("po_no", poNo);
        if (existingGrnBean != null && !existingGrnBean.isEmpty()) {
          List<BasicDynaBean> poItems = storePODAO.findAllByKey("po_no", poNo);
          for (BasicDynaBean poItem : poItems) {
            totalQty = BigDecimal.ZERO;
            totalBonusQty = BigDecimal.ZERO;
            for (int i = 0; i < billed_qty.length - 1; i++) {
              if (medicine_id[i].equals(poItem.get("medicine_id").toString())) {
                totalQty = new BigDecimal(billed_qty[i]);
                totalBonusQty = new BigDecimal(bonus_qty[i]);
                break;
              }
            }
            for (BasicDynaBean existingGrn : existingGrnBean) {
              if (!existingGrn.get("grn_no").toString().equals(grnNo)) {

                HashMap<String, Object> parms = new HashMap<String, Object>();
                parms.put("grn_no", existingGrn.get("grn_no").toString());
                parms.put("medicine_id", (Integer) poItem.get("medicine_id"));

                BasicDynaBean grnDetail = gdDao.findByKey(parms);
                if (grnDetail != null) {
                  totalQty = totalQty.add((BigDecimal) grnDetail.get("billed_qty"));
                  totalBonusQty = totalBonusQty.add((BigDecimal) grnDetail.get("bonus_qty"));
                }
              }
            }
            if (totalQty.compareTo((BigDecimal) poItem.get("qty_req")) > 0
                || totalBonusQty.compareTo((BigDecimal) poItem.get("bonus_qty_req")) > 0) {
              errorMsg = "item quantity is not matching ";
              log.info("item quantity is not matching for the following po_no: " +
                  poNo);
              return displayErrorMessage(errorMsg, req, mapping, redirect);
            }
          }
        } else {
          /* this block will be executed if the po_no has no grn_no associated */
          List<BasicDynaBean> poItems = storePODAO.findAllByKey("po_no", poNo);
          for (BasicDynaBean poItem : poItems) {
            for (int i = 0; i < billed_qty.length - 1; i++) {
              if (medicine_id[i].equals(poItem.get("medicine_id").toString())) {
                totalQty = new BigDecimal(billed_qty[i]);
                totalBonusQty = new BigDecimal(bonus_qty[i]);
                if (totalQty.compareTo((BigDecimal) poItem.get("qty_req")) > 0
                    || totalBonusQty.compareTo((BigDecimal) poItem.get("bonus_qty_req")) > 0) {
                  errorMsg = "item quantity is not matching ";
                  log.info("item quantity is not matching for the following po_no: " +
                      poNo);
                  return displayErrorMessage(errorMsg, req, mapping, redirect);
                }
              }
            }
          }
        }
      }
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      log.debug("Number of items being entered in stock: " + elelen);
      BasicDynaBean invBean = null;
      BasicDynaBean grnBean = null;

      // main tables: store_inovice and store_grn_main, create or update
      if (grnNo.equals("")) {
        invBean = invDao.getBean();
        grnBean = gmDao.getBean();
        createInvoiceGrn(con, paramMap, invBean, grnBean, username, cashPurchase);
        grnNo = (String) grnBean.get("grn_no");
        newGrn = true;

      } else {
        grnBean = gmDao.findByKey("grn_no", grnNo);
        int suppInvId = (Integer) grnBean.get("supplier_invoice_id");
        invBean = StockEntryDAO.getInvDetails(suppInvId);

        if (action.equals("reopen") || action.equals("close")) {
          int c = StockEntryDAO.updateInvoiceStatus(con, suppInvId,
              action.equals("reopen") ? "O" : "C", paymentRemarks);
          assert (c == 1) : "Invoice update status failed with row count: " + c;
          success = true;
          msg = "GRN " + action + " successful.";
          break tryBlock;
        }

        // invoice to be updated with new values
        ConversionUtils.copyToDynaBean(paramMap, invBean);
        ConversionUtils.copyToDynaBean(paramMap, invBean, "inv_");
        invBean.set("date_time", DateUtil.getCurrentTimestamp());
        //if ( mainCST != null)
        invBean.set("cst_rate", BigDecimal.ZERO);
        if (cashPurchase == null) {
          invBean.set("cash_purchase", "N");
        }
        String tcsPer = getParameter(paramMap, "inv_tcs_per");
        String tcsType = getParameter(paramMap, "tcs_type");
        if(null != tcsType && tcsType.equalsIgnoreCase("P")) {
          invBean.set("tcs_per", new BigDecimal(tcsPer));
        }

        String[] columns = {"invoice_date", "invoice_no", "due_date", "po_reference",
            "discount", "round_off", "status", "discount_type", "discount_per", "date_time",
            "other_charges", "remarks", "cess_tax_rate", "cess_tax_amt", "debit_amt",
            "cash_purchase", "payment_remarks", "cst_rate", "company_name", "means_of_transport",
            "consignment_no","consignment_date","transportation_charges", 
			"tcs_type", "tcs_per", "tcs_amount"};
        int c = invDao.updateWithName(con, columns, invBean.getMap(), "supplier_invoice_id");
        assert (c == 1) : "Invoice update failed with row count: " + c;

        // GRN can be updated with grn_qty_unit alone
        ConversionUtils.copyToDynaBean(paramMap, grnBean);
        String[] grnColumns = {"invoice_date", "grn_qty_unit", "purpose_of_purchase"};
        c = gmDao.updateWithName(con, grnColumns, grnBean.getMap(), "grn_no");
        assert (c == 1) : "Invoice update failed with row count: " + c;
      }

      // invoice copy upload
      String filename = getParameter(paramMap, "fileName");
      String del = getParameter(paramMap, "deleteUploadedInvoice");

      if ((filename != null && !filename.equals("")) || (del != null && del.equals("Y"))) {
        HashMap map = new HashMap<String, Object>();

        if (del != null && del.equals("Y")) {
          map.put("invoice_file_name", null);
          map.put("supplier_invoice_attachment", null);
          map.put("invoice_contenttype", null);

        } else {
          // filename is not null
          Object[] fileContent = (Object[]) paramMap.get("invoiceAttachment");
          map.put("invoice_file_name", filename);
          map.put("supplier_invoice_attachment", fileContent[0]);
          map.put("invoice_contenttype", getParameter(paramMap, "content_type"));
        }

        int c = invDao.update(con, map, "supplier_invoice_id", invBean.get("supplier_invoice_id"));

        assert (c == 1) : "Invoice file upload/delete failed with row count: " + c;
      }

      List<BasicDynaBean> groupList = PurchaseOrderDAO.getAllGroups();
      StoresHelper storeHelper = new StoresHelper();
      Map indentMap = new HashMap();
      if (!poNo.equals("")) {
        List<BasicDynaBean> indentList = StoresIndentDAO.getIndentNos(poNo);
        indentMap = ConversionUtils.listBeanToMapListBean(indentList, "medicine_id");
      }
      // detail table and stock related tables (stock, item-batch and lot)
      for (int i = 0; i < elelen; i++) {
        String vatType = "C";

        BasicDynaBean gdBean = gdDao.getBean();
        ConversionUtils.copyIndexToDynaBean(paramMap, i, gdBean);

        gdBean.set("grn_no", grnNo);
        gdBean.set("total_qty", ((BigDecimal) gdBean.get("billed_qty")).
            add((BigDecimal) gdBean.get("bonus_qty")));
        gdBean.set("issue_qty", new BigDecimal("0"));
        gdBean.set("outgoing_tax_rate", gdBean.get("tax_rate"));

        BasicDynaBean existingGdBean = null;

        int medicineId = Integer.parseInt(medicine_id[i]);
        String batchNo = batch_no[i];

        HashMap<String, Object> itemBatchKeys = new HashMap<String, Object>();
        itemBatchKeys.put("batch_no", batchNo);
        itemBatchKeys.put("medicine_id", medicineId);

        BasicDynaBean existingItemBatchBean = itemBatchDAO.findByKey(con, itemBatchKeys);
        int itemBatchId;

        BigDecimal mrp = new BigDecimal(mrp_array[i]);
        if (existingItemBatchBean == null) {
          // if batch is new,an insert into store_item_batch_details is required
          itemBatchId = itemBatchDAO.getNextSequence();
          BasicDynaBean newItemBatchBean = itemBatchDAO.getBean();
          newItemBatchBean.set("item_batch_id", itemBatchId);
          newItemBatchBean.set("batch_no", gdBean.get("batch_no"));
          newItemBatchBean.set("medicine_id", gdBean.get("medicine_id"));
          newItemBatchBean.set("exp_dt", gdBean.get("exp_dt"));
          newItemBatchBean.set("mrp", mrp);
          newItemBatchBean.set("username", username);
          itemBatchDAO.insert(con, newItemBatchBean);

        } else {
          itemBatchId = (Integer) existingItemBatchBean.get("item_batch_id");
          // update the MRP if it is different (can happen if MRP is set in PO)
          BigDecimal existingMrp = (BigDecimal) existingItemBatchBean.get("mrp");
          if (mrp.compareTo(existingMrp) != 0) {
            existingItemBatchBean.set("mrp", mrp);
            existingItemBatchBean.set("username", username);
            itemBatchDAO.updateWithName(con, new String[] {"mrp"}, existingItemBatchBean.getMap(),
                "item_batch_id");
          }
        }

        if (!grnmed[i].equalsIgnoreCase("Y")) {
          // insert grn items
          gdBean.set("item_batch_id", itemBatchId);
          gdDao.insert(con, gdBean);

          for (int j = 0; j < groupList.size(); j++) {
            BasicDynaBean groupBean = groupList.get(j);
            BasicDynaBean taxBean = storeGrnTaxDAO.getBean();
            storeHelper
                .setTaxDetails(paramMap, i, (Integer) groupBean.get("item_group_id"), taxBean);
            taxBean.set("medicine_id", gdBean.get("medicine_id"));
            taxBean.set("grn_no", gdBean.get("grn_no"));
            taxBean.set("item_batch_id", itemBatchId);

            Map<String, Object> taxMap = new HashMap<String, Object>();
            taxMap.put("medicine_id", gdBean.get("medicine_id"));
            taxMap.put("grn_no", gdBean.get("grn_no"));
            taxMap.put("item_batch_id", itemBatchId);
            taxMap.put("item_subgroup_id", taxBean.get("item_subgroup_id"));
            if (storeGrnTaxDAO.findByKey(taxMap) != null) {
              Map keysMap = new HashMap();
              keysMap.put("medicine_id", gdBean.get("medicine_id"));
              keysMap.put("grn_no", gdBean.get("grn_no"));
              keysMap.put("item_batch_id", itemBatchId);
              keysMap.put("item_subgroup_id", taxBean.get("item_subgroup_id"));
              storeGrnTaxDAO.update(con, taxBean.getMap(), keysMap);
            } else {
              int oldTaxSubgroupId = storeHelper.getOldTaxSubgroup(paramMap, i,
                  (Integer) groupBean.get("item_group_id"));
              LinkedHashMap<String, Object> identifiers = new LinkedHashMap<>();
              identifiers.put("medicine_id", gdBean.get("medicine_id"));
              identifiers.put("grn_no", gdBean.get("grn_no"));
              identifiers.put("item_batch_id", itemBatchId);
              identifiers.put("item_subgroup_id", oldTaxSubgroupId);
              storeGrnTaxDAO.delete(con, identifiers);
              if (taxBean.get("item_subgroup_id") != null && taxBean.get("tax_amt") != null) {
                storeGrnTaxDAO.insert(con, taxBean);
              }
            }
          }

        } else {
          // update grn items: keep the original copy for comparison to increment stock.
          existingGdBean = StockEntryDAO.getRow(grnNo, medicineId, batchNo);

          String[] columns = {"cost_price", "discount", "scheme_discount", "tax_rate",
              "tax_type", "tax", "billed_qty", "bonus_qty", "total_qty", "item_ced_per",
              "item_ced", "outgoing_tax_rate", "bonus_tax"};

          String[] keys = {"grn_no", "medicine_id", "batch_no"};

          int c = gdDao.updateWithNames(con, columns, gdBean.getMap(), keys);
          for (int j = 0; j < groupList.size(); j++) {
            BasicDynaBean groupBean = groupList.get(j);

            BasicDynaBean taxBean = storeGrnTaxDAO.getBean();
            taxBean.set("medicine_id", gdBean.get("medicine_id"));
            taxBean.set("grn_no", gdBean.get("grn_no"));
            taxBean.set("item_batch_id", itemBatchId);
            storeHelper
                .setTaxDetails(paramMap, i, (Integer) groupBean.get("item_group_id"), taxBean);

            Map<String, Object> taxMap = new HashMap<String, Object>();
            taxMap.put("medicine_id", gdBean.get("medicine_id"));
            taxMap.put("grn_no", gdBean.get("grn_no"));
            taxMap.put("item_batch_id", itemBatchId);
            taxMap.put("item_subgroup_id", taxBean.get("item_subgroup_id"));

            if (storeGrnTaxDAO.findByKey(taxMap) != null) {
              Map keysMap = new HashMap();
              keysMap.put("medicine_id", gdBean.get("medicine_id"));
              keysMap.put("grn_no", gdBean.get("grn_no"));
              keysMap.put("item_batch_id", itemBatchId);
              keysMap.put("item_subgroup_id", taxBean.get("item_subgroup_id"));
              storeGrnTaxDAO.update(con, taxBean.getMap(), keysMap);
            } else {
              int oldTaxSubgroupId = storeHelper.getOldTaxSubgroup(paramMap, i,
                  (Integer) groupBean.get("item_group_id"));
              LinkedHashMap<String, Object> identifiers = new LinkedHashMap<>();
              identifiers.put("medicine_id", gdBean.get("medicine_id"));
              identifiers.put("grn_no", gdBean.get("grn_no"));
              identifiers.put("item_batch_id", itemBatchId);
              identifiers.put("item_subgroup_id", oldTaxSubgroupId);
              storeGrnTaxDAO.delete(con, identifiers);
              if (taxBean.get("item_subgroup_id") != null && taxBean.get("tax_amt") != null) {
                storeGrnTaxDAO.insert(con, taxBean);
              }
            }


          }
          assert (c == 1) : "GRN Details update failed with row count: " + c;
        }


        /*
         * Do all the store stock stuff now.
         */
        totalQty = (BigDecimal) gdBean.get("total_qty");
        BigDecimal pkgSize = (BigDecimal) gdBean.get("grn_pkg_size");

        BasicDynaBean stockBean = stockDao.getBean();
        ConversionUtils.copyIndexToDynaBean(paramMap, i, stockBean);
        BasicDynaBean itemLotBean = itemLotDAO.getBean();
        ConversionUtils.copyIndexToDynaBean(paramMap, i, itemLotBean);

        // common for all types of stock change
        stockBean.set("consignment_stock",
            consignment_stock[0].equals("true") || consignment_stock[0].equals("t"));
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
            ((BigDecimal) stockBean.get("tax")).compareTo((BigDecimal) gdBean.get("bonus_tax")) >
                0 ?
                ((BigDecimal) stockBean.get("tax")).subtract((BigDecimal) gdBean.get("bonus_tax")) :
                ((BigDecimal) gdBean.get("bonus_tax")).subtract((BigDecimal) stockBean.get("tax")));

        // bonus stock, bonus shd be a seperate stock with package_cp almost 0 (only bonus_tax)
        BigDecimal bonusStockQty = (BigDecimal) gdBean.get("bonus_qty");
        BigDecimal bonusCostValue = (BigDecimal) gdBean.get("bonus_tax");
        BigDecimal billedStockQty = (BigDecimal) gdBean.get("billed_qty");
        BigDecimal billedCostValue = medTotal[i].subtract(bonusTax[i]);

        // if no billed qty, allocate all amount to bonus (can be different from bonusTax
        // in case there is discount given).
        if (billedStockQty.compareTo(BigDecimal.ZERO) == 0) {
          bonusCostValue = medTotal[i];
        }

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
            billedCostValue = medTotal[i].subtract(existingTotalCostValue);
            bonusCostValue = BigDecimal.ZERO;
          } else if (bonusStockQty.compareTo(BigDecimal.ZERO) > 0) {
            // all extra cost to bonus qty
            bonusCostValue = medTotal[i].subtract(existingTotalCostValue);
            billedCostValue = BigDecimal.ZERO;
          }
        }

        String stockSource = existingGdBean != null ? "E" : "S";
        BigDecimal totalCostValue = BigDecimal.ZERO;
        BigDecimal cost = BigDecimal.ZERO;
        BasicDynaBean itemBean =
            itemMasterDAO.findByKey(con, "medicine_id", gdBean.get("medicine_id"));

        if (bonusStockQty.compareTo(BigDecimal.ZERO) > 0) {
          BasicDynaBean bonusStock = stockDao.getBean();
          ConversionUtils.copyIndexToDynaBean(paramMap, i, bonusStock);

          //common for all types of stock change
          bonusStock.set("consignment_stock",
              consignment_stock[0].equals("true") || consignment_stock[0].equals("t"));
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

          int itemLotId = itemLotDAO.getNextSequence();
          BasicDynaBean bonusLot = itemLotBean;
          bonusLot.set("item_lot_id", itemLotId);
          bonusLot.set("package_cp", pkgCp);
          bonusLot.set("grn_no", grnNo);
          bonusLot.set("item_batch_id", itemBatchId);
          itemLotBean.set("lot_source", stockSource);
          itemLotBean.set("purchase_type", "B");
          itemLotDAO.insert(con, bonusLot);

          bonusStock.set("package_cp", pkgCp);
          bonusStock.set("item_lot_id", itemLotId);
          bonusStock.set("item_batch_id", itemBatchId);
          bonusStock.set("tax", gdBean.get("bonus_tax"));
          stockDao.insert(con, bonusStock);
          totalCostValue = totalCostValue.add((BigDecimal) gdBean.get("bonus_tax"));

        }

        if (billedStockQty.compareTo(BigDecimal.ZERO) > 0) {
          stockBean.set("qty", billedStockQty);
          // package_cp = cost_value*pkgSize/qty (high precision due to bug 30643).
          BigDecimal pkgCp = ConversionUtils.divideHighPrecision(billedCostValue.multiply(pkgSize),
              billedStockQty);

          int itemLotId = itemLotDAO.getNextSequence();
          itemLotBean.set("package_cp", pkgCp);
          itemLotBean.set("grn_no", grnNo);
          itemLotBean.set("item_batch_id", itemBatchId);
          itemLotBean.set("item_lot_id", itemLotId);
          itemLotBean.set("lot_source", stockSource);
          itemLotBean.set("purchase_type", "S");
          itemLotDAO.insert(con, itemLotBean);

          cost = billedStockQty.multiply(pkgCp);
          cost = ConversionUtils
              .divideHighPrecision(cost, (BigDecimal) itemBean.get("issue_base_unit"));
          totalCostValue = totalCostValue.add(cost);

          stockBean.set("item_batch_id", itemBatchId);
          stockBean.set("item_lot_id", itemLotId);
          stockBean.set("package_cp", pkgCp);
          stockDao.insert(con, stockBean);

        }

        Map<String, Object> updateKeys = new HashMap<String, Object>();
        updateKeys.put("grn_no", grnNo);
        updateKeys.put("item_batch_id", itemBatchId);
        //cost value of the grn
        grnBean = gdDao.findByKey(con, updateKeys);
        grnBean.set("cost_value", grnBean.get("cost_value") != null ?
            ((BigDecimal) grnBean.get("cost_value")).add(totalCostValue) : totalCostValue);

        gdDao.update(con, grnBean.getMap(), updateKeys);
        // Fixed Asset Table Insertion

        boolean isCategoryAssetTracking =
            StockEntryDAO.isCategoryAssetTracking((Integer) itemBean.get("med_category_id"));
        FixedAssetMasterDAO assetDao = new FixedAssetMasterDAO();
        BasicDynaBean assetBean = assetDao.getBean();
        HashMap<String, Object> fixedAssetKeys = new HashMap<String, Object>();
        fixedAssetKeys.put("asset_serial_no", batchNo);
        fixedAssetKeys.put("asset_id", medicineId);
        BasicDynaBean existingFixedAssetBean = assetDao.findByKey(con, fixedAssetKeys);

        if (isCategoryAssetTracking) {
          if (existingFixedAssetBean == null) {
            assetBean.set("asset_id", gdBean.get("medicine_id"));
            assetBean
                .set("installation_date", java.sql.Date.valueOf(DataBaseUtil.getCurrentDate()));
            assetBean.set("asset_dept", storeId);
            assetBean.set("asset_serial_no", gdBean.get("batch_no"));
            assetBean.set("asset_status", "A");
            assetDao.insert(con, assetBean);
          }
        }

        // reducing poqty if item comes from PO: TODO: should this be done only for pomed or all?
        if (!poNo.equals("")) {
          BigDecimal billedQty = (BigDecimal) gdBean.get("billed_qty");
          BigDecimal bonusQty = (BigDecimal) gdBean.get("bonus_qty");
          if (grnmed[i].equalsIgnoreCase("Y") && existingGdBean != null) {
            billedQty = billedQty.subtract((BigDecimal) existingGdBean.get("billed_qty"));
            bonusQty = bonusQty.subtract((BigDecimal) existingGdBean.get("bonus_qty"));
          }
          PurchaseOrderDAO.updateReceivedQty(con, poNo, medicineId, billedQty, bonusQty);
          // error check not required, 0 rows updated is valid.

          // updating indent status if indent type is 'Request for new medicine' ...
          StockEntryDAO dao = new StockEntryDAO(con);
          if (null != indentMap.get(medicineId)) {
            dao.updateIndentItems((List<BasicDynaBean>) indentMap.get(medicineId), medicineId, poNo,
                bonusQty.add(billedQty), username);
            // error check not required, 0 rows updated is valid.
          }
        }
      }    // end for each item

      int count = Integer.parseInt(getParameter(paramMap, "countSeq"));
      StoresDBTablesUtil.updateCount(con, count);

      // updating PO status if all qty_req-qty_received <= 0
		if (!poNo.equals("")) {
			PurchaseOrderDAO.closePoIfAllReceived(con, poNo);
		}
      // error check not required, 0 rows updated is valid.

      success = true;
      msg = "GRN " + grnNo + " generated successfully";
      if (getParameter(paramMap, "_printAfterSave").equals("Y")) {
        ActionRedirect url = new ActionRedirect(mapping.findForward("grnPrintRedirect"));
        url.addParameter("grNo", grnNo);
        url.addParameter("printerType", getParameter(paramMap, "printType"));
        url.addParameter("doAllowStatus", getParameter(paramMap, "doAllowStatus"));
        url.addParameter("doSchemaAllowStatus", getParameter(paramMap, "doSchemaAllowStatus"));
        url.addParameter("grnPrintTemplate", getParameter(paramMap, "grnPrintTemplate"));
        printUrls.add(req.getContextPath() + url.getPath());
      }
      if (barCodePref.equals("Y")) {
        ActionRedirect url = new ActionRedirect(mapping.findForward("grnBarcodeRedirect"));
        url.addParameter("grnno", grnNo);
        url.addParameter("barcodeType", "Item");
        printUrls.add(req.getContextPath() + url.getPath());
      }
		if (printUrls.size() > 0) {
			session.setAttribute("printURLs", printUrls);
		}

    } finally {
      DataBaseUtil.commitClose(con, success);

      //update stock timestamp
      StockFIFODAO stockFIFODAO = new StockFIFODAO();
      stockFIFODAO.updateStockTimeStamp();
      stockFIFODAO.updateStoresStockTimeStamp(storeId);
    }

    redirect = new ActionRedirect(mapping.findForward("stockEntryRedirect"));
    redirect.addParameter("grNo", grnNo);
    if (newGrn) {
      FlashScope flash = FlashScope.getScope(req);
      flash.info(msg);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    }
    return redirect;
  }

  @IgnoreConfidentialFilters
  public ActionForward getBatchs(ActionMapping am, ActionForm af,
      HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException, SQLException {
    int storeNum = -1;
    int medicineIdNum = -1;
    String storeId = req.getParameter("storeId");
    JSONSerializer js = new JSONSerializer().exclude("class");
    String medicineName = req.getParameter("medicineName");
    String medicineId = MedicineStockDAO.medicineNameToId(medicineName);
    if ((medicineId != null) && (medicineId.trim().length() > 0)) {
      medicineIdNum = Integer.parseInt(medicineId);
    }
    if ((storeId != null) && (storeId.trim().length() > 0)) {
      storeNum = Integer.parseInt(storeId);
    }
    String[] batchNos = StockEntryDAO.getBatches(medicineIdNum, storeNum);
    String batchnos = js.serialize(batchNos);
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.setHeader("Expires", "0");
    res.getWriter().write(batchnos);
    res.flushBuffer();
    return null;
  }

  /* displays the message on stock-entry-screen */
  private ActionForward displayErrorMessage(String errorMsg, HttpServletRequest req,
      ActionMapping mapping, ActionRedirect redirect) {
    FlashScope flash = FlashScope.getScope(req);
    redirect = new ActionRedirect(mapping.findForward("stockEntryRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    flash.info(errorMsg);
    return redirect;
  }

  /* Below method to generate GRN print*/
  @IgnoreConfidentialFilters
  public ActionForward generateGRNprint(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res)
      throws DocumentException, Exception {

    Connection con = null;
    FtlReportGenerator fGen = null;
    Map params = new HashMap();
    String grNo = req.getParameter("grNo");
    String doAllowStatus = "false";
    String doSchemaAllowStatus = "false";

    if (grNo != null) {
      GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();

      if (req.getParameter("doAllowStatus") == null &&
          req.getParameter("doSchemaAllowStatus") == null) {
        if (dto.getStock_entry_agnst_do().equalsIgnoreCase("y")) {
          doSchemaAllowStatus = "true";
			if ((Integer) new GenericDAO("u_user")
					.findByKey("emp_username", req.getSession(false).getAttribute("userid"))
					.get("center_id") != 0) {
				doAllowStatus = "true";
			}
        }
      } else {
        doAllowStatus = req.getParameter("doAllowStatus");
        doSchemaAllowStatus = req.getParameter("doSchemaAllowStatus");
      }

      List<BasicDynaBean> grnItemList = StockEntryDAO.getGRNitemsList(grNo);

      /** Taxation Detaills */
      List<BasicDynaBean> grnTaxDetails = StockEntryDAO.getTaxDetails(grNo);
      params.put("grnTaxDetails", grnTaxDetails);

      String printerId = req.getParameter("printerType");

      BasicDynaBean printprefs = null;
		if (printerId != null) {
			printprefs = PrintConfigurationsDAO.getPageOptions(
					PrintConfigurationsDAO.PRINT_TYPE_STORE, Integer.parseInt(printerId));
		}
		if (printprefs == null) {
			printprefs =
					PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_STORE);
		}
      params.put("items", grnItemList);
      params.put("hospital_tin", dto.getHospitalTin());
      params.put("hospital_pan", dto.getHospitalPan());
      params.put("hospital_service_regn_no", dto.getHospitalServiceRegnNo());
      BasicDynaBean invBean = StockEntryDAO.getInvoiceRow(
          Integer.parseInt(grnItemList.get(0).get("supplier_invoice_id").toString()));
      params.put("invoice_rnd_off", invBean.get("round_off"));
      params.put("discount_type", invBean.get("discount_type"));
      params.put("discount", invBean.get("discount"));
      params.put("discount_per", invBean.get("discount_per"));
      params.put("other_charges", invBean.get("other_charges"));
      params.put("transportation_charges", invBean.get("transportation_charges"));
      params.put("debit_amt", invBean.get("debit_amt"));
      params.put("remarks", invBean.get("remarks"));
      params.put("cashpurchase", invBean.get("cash_purchase"));
      params.put("due_date", invBean.get("due_date"));
      params.put("NumberToStringConversion", NumberToWordFormat.wordFormat());
      params.put("doAllowStatus", doAllowStatus);
      params.put("doSchemaAllowStatus", doSchemaAllowStatus);
      params.put("tcs_amount", invBean.get("tcs_amount"));

      HashMap<String, BigDecimal> vatDetails = new HashMap<String, BigDecimal>();
      for (BasicDynaBean b : grnItemList) {
        String rate = b.get("tax_rate").toString();
        BigDecimal taxAmt = (BigDecimal) b.get("tax");
        BigDecimal totalTax = vatDetails.get(rate);
        if (totalTax == null) {
          vatDetails.put(rate, taxAmt);
        } else {
          vatDetails.put(rate, taxAmt.add(totalTax));
        }
      }
      params.put("vatDetails", vatDetails);
      PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
      String templateContent = printtemplatedao.getCustomizedTemplate(PrintTemplate.Grn_print);
      String templateName = req.getParameter("grnPrintTemplate");
      String templateMode = null;


      if (templateName == null || templateName.equals("") || templateName.equals("BUILTIN_HTML")) {
        fGen = new FtlReportGenerator("GrnPrint");
        templateMode = "H";
      } else {
        String templateCodeQuery =
            "SELECT grn_template_content, template_mode FROM grn_print_template " +
                " WHERE template_name=?";
        List printTemplateList = DataBaseUtil.queryToDynaList(templateCodeQuery, templateName);
        for (Object obj : printTemplateList) {
          BasicDynaBean templateBean = (BasicDynaBean) obj;
          templateContent = (String) templateBean.get("grn_template_content");
          log.debug("templateContent=" + templateContent);
          templateMode = (String) templateBean.get("template_mode");
        }

        if (printTemplateList != null && printTemplateList.size() == 0) {
	  //in case all print templates are deleted and pref still hold old value it is equal to built in template
          fGen = new FtlReportGenerator("GrnPrint");
        } else {

          StringReader reader = new StringReader(templateContent);
          fGen = new FtlReportGenerator("CustomTemplate", reader);
        }
      }

      String printContent = fGen.getPlainText(params);
      HtmlConverter hc = new HtmlConverter();
      if (printprefs.get("print_mode").equals("P")) {
        OutputStream os = res.getOutputStream();
        res.setContentType("application/pdf");
        hc.writePdf(os, printContent, "GrnPrint", printprefs, false, false, true, true, true,
            false);
        return null;
      } else {
        String textReport = null;
        textReport = new String(hc.getText(printContent, "GrnPrintText", printprefs, true, true));
        req.setAttribute("textReport", textReport);
        req.setAttribute("textColumns", printprefs.get("text_mode_column"));
        req.setAttribute("printerType", "DMP");
        return am.findForward("textPrintApplet");
      }
    }
    return null;
  }

  @IgnoreConfidentialFilters
  public ActionForward getInvoiceNosOfSupplier(ActionMapping am, ActionForm af,
      HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException, SQLException {
    String supplierId = req.getParameter("supplier_id");
    String fromDate = req.getParameter("from_date");
    String toDate = req.getParameter("to_date");
    JSONSerializer js = new JSONSerializer().exclude("class");
    List invoiceNos = StockEntryDAO.getInvoiceNos(supplierId, fromDate, toDate);
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.setHeader("Expires", "0");
    res.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(invoiceNos)));
    res.flushBuffer();
    return null;
  }

  @IgnoreConfidentialFilters
  public ActionForward getUploadedInvoiceCopy(ActionMapping m, ActionForm f,
      HttpServletRequest req, HttpServletResponse resp) throws Exception {

    String id = req.getParameter("suppinvid");
    String fileName = "";
    String contentType = "";
    if (id == null) {
      return m.findForward("error");
    }

    Map<String, Object> uploadMap = StockEntryDAO.getUploadedDocInfo(Integer.parseInt(id));

    if (uploadMap.isEmpty()) {
      return m.findForward("error");
    }

    fileName = (String) uploadMap.get("filename");
    contentType = (String) uploadMap.get("contenttype");
    resp.setContentType(contentType);
    if (!fileName.equals("")) {
      resp.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
    }

    OutputStream os = resp.getOutputStream();
    InputStream s = (InputStream) uploadMap.get("uploadfile");
    if (s != null) {
      byte[] bytes = new byte[4096];
      int len = 0;
      while ((len = s.read(bytes)) > 0) {
        os.write(bytes, 0, len);
      }
    }

    os.flush();
    if (s != null) {
      s.close();
    }
    return null;
  }

  @IgnoreConfidentialFilters
  public ActionForward getPOsForStore(ActionMapping m, ActionForm f,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    JSONSerializer js = new JSONSerializer().exclude("class");
    HashMap<String, List<?>> result = new HashMap();
    Integer storeId = null;
    if (req.getParameter("store") != null && !req.getParameter("store").equals("")) {
      storeId = Integer.parseInt(req.getParameter("store"));
    }

    List<BasicDynaBean> polist = null;
    if (storeId != null) {
      polist = PurchaseOrderDAO.getApprovedPOsForStore(storeId);
    }

    result.put("polist", ConversionUtils.listBeanToListMap(polist));
    res.setContentType("text/javascript");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.setHeader("Expires", "0");
    res.getWriter().write(js.deepSerialize(result));
    res.flushBuffer();

    return null;
  }

}
