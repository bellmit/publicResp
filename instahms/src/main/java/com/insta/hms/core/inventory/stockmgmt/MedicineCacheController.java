package com.insta.hms.core.inventory.stockmgmt;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.inventory.InventoryController;
import com.insta.hms.core.inventory.URLRoute;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.stores.StoreDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This class is used to get available medicines of store and cache it.
 * 
 * @author irshadmohammed
 *
 */
@Controller
@RequestMapping(URLRoute.STOCK_DETAILS)
public class MedicineCacheController extends InventoryController {

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The stock service. */
  @LazyAutowired
  private StockService stockService;

  /**
   * Gets the items list.
   *
   * @param request  the request
   * @param response the response
   * @return the items list
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException     the SQL exception
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = URLRoute.GET_STOCK_IN_STORE, method = RequestMethod.GET)
  public String getItemsList(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {
    HttpSession session = RequestContext.getSession();
    if (null != request.getHeader("If-Modified-Since")) {
      // the browser is just requesting the same thing, only if modified.
      // Since we encode the timestamp in the request, if we get a request,
      // it CANNOT have been modified. So, just say 304 not modified without checking.
      response.setStatus(304);
      return null;
    }
    boolean includeZero = getIncludeZeroStockParam(request);

    Boolean retailable = getIsRetailiableStockParam(request);

    Boolean billable = getIsBillableParam(request);

    String[] issueTypes = getIssueTypesParam(request);

    boolean includeConsignment = getIsIncludeConsignmentParam(request);

    boolean includeUnapproved = getIsIncludeUnApprovedParam(request);

    boolean onlySalesStores = getIsOnlySalesStoresParam(request);

    String medicineNameFilterText = StringUtils.trimToNull(request.getParameter("filterText"));

    // Last-Modified is required, cache-control is good to have to enable caching
    response.setHeader("Last-modified", "Thu, 1 Jan 2009 00:00:00 GMT");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "public; max-age=360000");
    response.setContentType("text/javascript; charset=UTF-8");

    java.io.Writer writer = response.getWriter();
    writer.write("var jMedicineNames = ");

    String storeIdStr = request.getParameter("storeId");
    String healthAuthority = null;
    Integer centerId = null;

    if (storeIdStr == null || storeIdStr.equals("")) {
      centerId = (Integer) session.getAttribute("centerId");
      BasicDynaBean centerBean = centerService.findByKey(centerId);
      healthAuthority = (String) centerBean.get("health_authority");
      // fetch all stores data.
      stockService.getMedicineNamesInStock(writer, includeZero, retailable, billable,
          issueTypes, includeConsignment, includeUnapproved, onlySalesStores, healthAuthority,
          medicineNameFilterText);

    } else {
      // fetch single store data
      int storeId = Integer.parseInt(storeIdStr);
      BasicDynaBean storeDetails = StoreDAO.findByStore(storeId);
      centerId = (Integer) storeDetails.get("center_id");
      healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
      String grnNo = request.getParameter("grnNo");
      if (grnNo != null && grnNo != "") {
        stockService.getMedicineNamesInStock(writer, includeZero, retailable, billable,
            issueTypes, includeConsignment, includeUnapproved, storeId, healthAuthority, grnNo,
            medicineNameFilterText);
      } else {
        stockService.getMedicineNamesInStock(writer, includeZero, retailable, billable,
            issueTypes, includeConsignment, includeUnapproved, storeId, healthAuthority,
            medicineNameFilterText);
      }
    }
    writer.write(";");
    return null;
  }

  /**
   * Gets the checks if is only sales stores param.
   *
   * @param request the request
   * @return the checks if is only sales stores param
   */
  private boolean getIsOnlySalesStoresParam(HttpServletRequest request) {
    String onlySalesStoresStr = request.getParameter("onlySalesStores");
    boolean onlySalesStores = false;
    if (onlySalesStoresStr != null && onlySalesStoresStr.equalsIgnoreCase("Y")) {
      onlySalesStores = true;
    }
    return onlySalesStores;
  }

  /**
   * Gets the checks if is include un approved param.
   *
   * @param request the request
   * @return the checks if is include un approved param
   */
  private boolean getIsIncludeUnApprovedParam(HttpServletRequest request) {
    String includeUnapprovedStr = request.getParameter("includeUnapprovedStock");
    boolean includeUnapproved = false;
    if (includeUnapprovedStr != null && includeUnapprovedStr.equalsIgnoreCase("Y")) {
      includeUnapproved = true;
    }
    return includeUnapproved;
  }

  /**
   * Gets the checks if is include consignment param.
   *
   * @param request the request
   * @return the checks if is include consignment param
   */
  private boolean getIsIncludeConsignmentParam(HttpServletRequest request) {
    String includeConsignmentStr = request.getParameter("includeConsignmentStock");
    boolean includeConsignment = false;
    if (includeConsignmentStr != null && includeConsignmentStr.equalsIgnoreCase("Y")) {
      includeConsignment = true;
    }
    return includeConsignment;
  }

  /**
   * Gets the issue types param.
   *
   * @param request the request
   * @return the issue types param
   */
  private String[] getIssueTypesParam(HttpServletRequest request) {
    String issueTypeStr = request.getParameter("issueType");
    String[] issueTypes = null; // default: no filter
    if (issueTypeStr != null && !issueTypeStr.equals("")) {
      issueTypes = issueTypeStr.split("");
    }
    return issueTypes;
  }

  /**
   * Gets the checks if is billable param.
   *
   * @param request the request
   * @return the checks if is billable param
   */
  private Boolean getIsBillableParam(HttpServletRequest request) {
    String billableStr = request.getParameter("billable");
    Boolean billable = null; // default: no filter
    if (billableStr != null && !billableStr.equals("")) {
      billable = billableStr.equalsIgnoreCase("y");
    }
    return billable;
  }

  /**
   * Gets the include zero stock param.
   *
   * @param request the request
   * @return the include zero stock param
   */
  private boolean getIncludeZeroStockParam(HttpServletRequest request) {
    String includeZeroStr = request.getParameter("includeZeroStock");
    boolean includeZero = false;
    if (includeZeroStr != null && includeZeroStr.equalsIgnoreCase("Y")) {
      includeZero = true;
    }
    return includeZero;
  }

  /**
   * Lookup.
   *
   * @param request the request
   * @param response the response
   * @return the map
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = URLRoute.GET_STOCK_IN_STORE + "/lookup", method = RequestMethod.GET)
  public Map<String, Object> lookup(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    List<BasicDynaBean> medicineList = new ArrayList<>();
    HttpSession session = RequestContext.getSession();
    if (null != request.getHeader("If-Modified-Since")) {
      // the browser is just requesting the same thing, only if modified.
      // Since we encode the timestamp in the request, if we get a request,
      // it CANNOT have been modified. So, just say 304 not modified without checking.
      response.setStatus(304);
      return null;
    }
    boolean includeZero = getIncludeZeroStockParam(request);

    Boolean retailable = getIsRetailiableStockParam(request);

    Boolean billable = getIsBillableParam(request);

    String[] issueTypes = getIssueTypesParam(request);

    boolean includeConsignment = getIsIncludeConsignmentParam(request);

    boolean includeUnapproved = getIsIncludeUnApprovedParam(request);

    boolean onlySalesStores = getIsOnlySalesStoresParam(request);

    String medicineNameFilterText = StringUtils.trimToNull(request.getParameter("filterText"));
    String storeIdStr = request.getParameter("storeId");
    String healthAuthority = null;
    Integer centerId = null;

    if (storeIdStr == null || storeIdStr.equals("")) {
      centerId = (Integer) session.getAttribute("centerId");
      BasicDynaBean centerBean = centerService.findByKey(centerId);
      healthAuthority = (String) centerBean.get("health_authority");
      // fetch all stores data.
      medicineList.addAll(stockService.getMedicineNamesInStock(includeZero, retailable,
          billable, issueTypes, includeConsignment, includeUnapproved, onlySalesStores,
          healthAuthority, medicineNameFilterText));

    } else {
      // fetch single store data
      int storeId = Integer.parseInt(storeIdStr);
      BasicDynaBean storeDetails = StoreDAO.findByStore(storeId);
      centerId = (Integer) storeDetails.get("center_id");
      healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
      String grnNo = request.getParameter("grnNo");
      if (grnNo != null && grnNo != "") {
        medicineList.addAll(stockService.getMedicineNamesInStock(includeZero, retailable,
            billable, issueTypes, includeConsignment, includeUnapproved, storeId,
            healthAuthority, grnNo, medicineNameFilterText));
      } else {
        medicineList.addAll(stockService.getMedicineNamesInStock(includeZero, retailable,
            billable, issueTypes, includeConsignment, includeUnapproved, storeId,
            healthAuthority, medicineNameFilterText));
      }
    }
    medicineList = removeDuplicateMedicines(medicineList);
    Map<String, Object> getOrderableItemData = new HashMap<>();
    getOrderableItemData.put("inventory_items",
        ConversionUtils.listBeanToListMap(medicineList));
    return getOrderableItemData;
  }

  /**
   * Gets the checks if is retailiable stock param.
   *
   * @param request the request
   * @return the checks if is retailiable stock param
   */
  private Boolean getIsRetailiableStockParam(HttpServletRequest request) {
    String retailableStr = request.getParameter("retailable");
    Boolean retailable = null; // default: no filter
    if (retailableStr != null && !retailableStr.equals("")) {
      retailable = retailableStr.equalsIgnoreCase("y");
    }
    return retailable;
  }

  /**
   * Removes the duplicate medicines.
   *
   * @param medicineList the medicine list
   * @return the list
   */
  private List<BasicDynaBean> removeDuplicateMedicines(List<BasicDynaBean> medicineList) {
    if (CollectionUtils.isNotEmpty(medicineList)) {
      Map<String, BasicDynaBean> medicineIdMap =
          ConversionUtils.listBeanToMapBean(medicineList, "medicine_id");
      return new ArrayList<>(medicineIdMap.values());
    }
    return medicineList;
  }

}
