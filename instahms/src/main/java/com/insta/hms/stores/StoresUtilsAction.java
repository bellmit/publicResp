package com.insta.hms.stores;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StoresUtilsAction extends DispatchAction {

  /*
   * Return the item master timestamp as text
   */
  @IgnoreConfidentialFilters
  public ActionForward getItemMasterTimestamp(ActionMapping m, ActionForm f, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, java.io.IOException {

    int ts = PharmacymasterDAO.getItemMasterTimestamp();
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.setHeader("Expires", "0");
    res.getWriter().write("" + ts);
    res.flushBuffer();
    return null;
  }

  /*
   * Return the store stock master timestamp as text
   */
  @IgnoreConfidentialFilters
  public ActionForward getStoreStockTimestamp(ActionMapping m, ActionForm f, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, java.io.IOException {

    String storeIdStr = req.getParameter("storeId");
    int storeId = Integer.parseInt(storeIdStr);
    int ts = MedicineStockDAO.getStoreStockTimestamp(storeId);
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.setHeader("Expires", "0");
    res.getWriter().write("" + ts);
    res.flushBuffer();
    return null;
  }

}
