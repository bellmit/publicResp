package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.usermanager.UserDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class StoresAssetApprovalAction.
 */
public class StoresAssetApprovalAction extends BaseAction {

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, SQLException, Exception {
    Map map = getParameterMap(req);

    HttpSession session = req.getSession(false);
    Map actMap = (Map) session.getAttribute("actionRightsMap");
    String storeAccess = (String) session.getAttribute("multiStoreAccess");
    String userDefaultDeptId = (String) session.getAttribute("pharmacyStoreId");
    String deptId = req.getParameter("dept_id");
    int roleId = (Integer) session.getAttribute("roleId");
    deptId = (deptId == null || deptId.equals(""))
        ? (userDefaultDeptId == null || userDefaultDeptId.equals(""))
            ? (roleId == 1 || roleId == 2 ? "0" : null)
            : userDefaultDeptId
        : deptId;
    if (deptId != null && !deptId.equals("")) {
      BasicDynaBean dept = new GenericDAO("stores").findByKey("dept_id", Integer.parseInt(deptId));
      String deptName = dept.get("dept_name").toString();
      req.setAttribute("dept_id", deptId);
      req.setAttribute("dept_name", deptId);
      map.put("dept_id", new String[] { deptId });
      map.put("dept_id@type", new String[] { "integer" });
      map.put("dept_id@cast", new String[] { "y" });
    }
    if (deptId != null && deptId.equals("")) {
      req.setAttribute("dept_id", deptId);
      req.setAttribute("dept_id@type", "integer");
      req.setAttribute("dept_id@cast", "y");
    }
    PagedList list = EditStockDetailsDAO.searchList(map, ConversionUtils.getListingParameter(map));
    req.setAttribute("pagedList", list);
    req.setAttribute("genPrefs", GenericPreferencesDAO.getPrefsBean().getMap());
    return mapping.findForward("list");

  }

  /**
   * Group update.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward groupUpdate(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

    Map<String, String[]> itemsMap = request.getParameterMap();
    String[] approve = itemsMap.get("_happrove");
    String[] identifier = itemsMap.get("_identifier");
    String[] itemId = itemsMap.get("_itemId");
    String selectAll = request.getParameter("_All");
    String selectAllItems = request.getParameter("_AllItems");
    String[] store = itemsMap.get("_dept_id");
    Connection con = null;
    boolean status = true;
    EditStockDetailsDAO dao = null;
    HttpSession session = request.getSession();
    String username = (String) session.getAttribute("userid");
    FlashScope flash = FlashScope.getScope(request);
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      dao = new EditStockDetailsDAO();
      int count = 0;
      for (int i = 0; i < approve.length; i++) {
        if (approve[i].equalsIgnoreCase("true")) {
          count = count + 1;
        }
      }
      String[] updatedIden = new String[count];
      String[] updatedId = new String[count];

      for (int i = 0, j = 0; i < approve.length; i++) {
        if (approve[i].equalsIgnoreCase("true")) {
          updatedId[j] = itemId[i];
          updatedIden[j] = identifier[i];
          j++;
        }
      }
      if (status) {
        status = dao.updateApproval(con, updatedId, updatedIden, store[0], username);
      }
      if (selectAllItems != null && status) {
        status = dao.updateApproveAll(con, store[0], username);
      }
    } catch (Exception ex) {
      status = false;
      con.rollback();
    } finally {
      if (status) {
        con.commit();
        if (con != null) {
          con.close();
        }
        flash.put("success", "Item Details Updated Successfully...");
      } else {
        con.rollback();
        if (con != null) {
          con.close();
        }
        flash.put("msg", "Transaction Failure...");
      }
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    redirect.addParameter("sortOrder", "medicine_name");
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * for item details pop-up.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the item details
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws ServletException the servlet exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getItemDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, SQLException, ParseException, ServletException {

    int itemId = Integer.parseInt(request.getParameter("itemId"));
    String identifier = request.getParameter("identifier");
    int storeId = Integer.parseInt(request.getParameter("storeId"));
    BasicDynaBean centerBean = UserDAO.getCenterId(storeId);
    int centerId = (Integer) centerBean.get("center_id");
    List centerStores = new StockEntryAction().getAllStores(centerId);
    List<BasicDynaBean> itemlist = EditStockDetailsDAO.getItemDetails(itemId, identifier,
        centerStores);

    JSONSerializer js = new JSONSerializer().exclude("class");
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(itemlist)));
    response.flushBuffer();

    return null;

  }

}
