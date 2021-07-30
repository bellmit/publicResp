package com.insta.hms.stores;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/*
 * Returns a list of items that are in stock, grouped by stores. Parameters accepted are:
 *  includeZeroStock = Y/N (default N)
 *  retailable = empty/Y/N
 *  billable = empty/Y/N
 *  issueType = C, CR, CRPL etc. containing issueTypes to be included, or empty for all
 *  includeConsignment = Y/N (default N)
 *  includeUnapproved = Y/N (default N)
 */
public class MedicineCacheAction extends Action {

  /**
   * Execut method of MedicineCacheAction.
   */
  @IgnoreConfidentialFilters
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException, SQLException {

    HttpSession session = RequestContext.getSession();

    if (null != request.getHeader("If-Modified-Since")) {
      // the browser is just requesting the same thing, only if modified.
      // Since we encode the timestamp in the request, if we get a request,
      // it CANNOT have been modified. So, just say 304 not modified without checking.
      response.setStatus(304);
      return null;
    }

    String includeZeroStr = request.getParameter("includeZeroStock");
    boolean includeZero = false;
    if (includeZeroStr != null && includeZeroStr.equalsIgnoreCase("Y")) {
      includeZero = true;
    }

    String retailableStr = request.getParameter("retailable");
    Boolean retailable = null; // default: no filter
    if (retailableStr != null && !retailableStr.equals("")) {
      retailable = retailableStr.equalsIgnoreCase("y");
    }

    String billableStr = request.getParameter("billable");
    Boolean billable = null; // default: no filter
    if (billableStr != null && !billableStr.equals("")) {
      billable = billableStr.equalsIgnoreCase("y");
    }

    String issueTypeStr = request.getParameter("issueType");
    String[] issueTypes = null; // default: no filter
    if (issueTypeStr != null && !issueTypeStr.equals("")) {
      issueTypes = issueTypeStr.split("");
    }

    String includeConsignmentStr = request.getParameter("includeConsignmentStock");
    boolean includeConsignment = false;
    if (includeConsignmentStr != null && includeConsignmentStr.equalsIgnoreCase("Y")) {
      includeConsignment = true;
    }

    String includeUnapprovedStr = request.getParameter("includeUnapprovedStock");
    boolean includeUnapproved = false;
    if (includeUnapprovedStr != null && includeUnapprovedStr.equalsIgnoreCase("Y")) {
      includeUnapproved = true;
    }

    String onlySalesStoresStr = request.getParameter("onlySalesStores");
    boolean onlySalesStores = false;
    if (onlySalesStoresStr != null && onlySalesStoresStr.equalsIgnoreCase("Y")) {
      onlySalesStores = true;
    }

    // Last-Modified is required, cache-control is good to have to enable caching
    response.setHeader("Last-modified", "Thu, 1 Jan 2009 00:00:00 GMT");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "public; max-age=360000");
    response.setContentType("text/javascript");

    java.io.Writer writer = response.getWriter();

    writer.write("var jMedicineNames = ");

    String storeIdStr = request.getParameter("storeId");
    String healthAuthority = null;
    Integer centerId = null;
    if (storeIdStr == null || storeIdStr.equals("")) {
      centerId = (Integer) session.getAttribute("centerId");
      healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
      // fetch all stores data.
      MedicineStockDAO.writeMedicineNamesInStock(writer, includeZero, retailable, billable,
          issueTypes, includeConsignment, includeUnapproved, onlySalesStores, healthAuthority);

    } else {
      // fetch single store data
      int storeId = Integer.parseInt(storeIdStr);
      BasicDynaBean storeDetails = StoreDAO.findByStore(storeId);
      centerId = (Integer) storeDetails.get("center_id");
      healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
      String grnNo = request.getParameter("grnNo");
      // TODO: implement this.
      if (grnNo != null && grnNo != "") {
        MedicineStockDAO.writeMedicineNamesInStock(writer, includeZero, retailable, billable,
            issueTypes, includeConsignment, includeUnapproved, storeId, healthAuthority, grnNo);
      } else {
        MedicineStockDAO.writeMedicineNamesInStock(writer, includeZero, retailable, billable,
            issueTypes, includeConsignment, includeUnapproved, storeId, healthAuthority);
      } 
    }
    writer.write(";");
    return null;
  }

}
