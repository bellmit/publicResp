package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.usermanager.UserDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class StoresPOApprovalAction extends BaseAction {

  static StoresPOApprovalDAO dao = new StoresPOApprovalDAO();
  static GenericDAO mdao = new GenericDAO("store_po_main");

  @SuppressWarnings("unchecked")
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {

    Map<Object, Object> map = getParameterMap(req);
    PagedList pagedList = null;
    HttpSession session = req.getSession(false);
    String dept_id = (String) session.getAttribute("pharmacyStoreId");
    String user = (String) session.getAttribute("userid");
    int centerId = RequestContext.getCenterId();
    req.setAttribute("listcentersforsuppliers", dao.listAllcentersforAPo(centerId));
    /** check if user has access right to view data of all stores */
    HashMap actionRightsMap;
    Object roleID = null;
    actionRightsMap = (HashMap) req.getSession(false).getAttribute("actionRightsMap");
    roleID = req.getSession(false).getAttribute("roleId");
    String actionRightStatus = (String) session.getAttribute("multiStoreAccess");
    if (actionRightStatus == null)
      actionRightStatus = "N";
    int roleId = ((Integer) roleID).intValue();
    if ((actionRightStatus.equals("A")) || (roleId == 1 || (roleId == 2))) {

      /** User has access to all stores or he is in admin role..let him see all */
      if ((roleId != 1) && (roleId != 2)) {
        /**
         * User has multistore access but not in Admin or InstaAdmin Role
         *
         */
        if (!map.containsKey("store_id")) {
          /** if not searching by specific store, show indents belonging to all assigned stores */
          List<Object> loggedStores = StoresDBTablesUtil
              .getLoggedUserStoreIds((String) req.getSession(false).getAttribute("userId"));
          if (!loggedStores.isEmpty()) {
            String[] loggedStoresAsParamArray = loggedStores.toArray(new String[0]);
            map.put("store_id", loggedStoresAsParamArray);
            map.put("store_id@type", new String[] { "integer" });
          }
        }
        req.setAttribute("store_id", dept_id);
      }
      pagedList = StoresPOApprovalDAO.searchIndentList(map,
          ConversionUtils.getListingParameter(map));
    } else {
      /** user can see data only pertaining to his store */
      if (dept_id != null && !dept_id.equals("")) {
        map.put("store_id", new String[] { dept_id });
        map.put("store_id@type", new String[] { "integer" });
        pagedList = StoresPOApprovalDAO.searchIndentList(map,
            ConversionUtils.getListingParameter(map));
        req.setAttribute("store_id", dept_id);
      } else {
        pagedList = new PagedList();
      }
    }

    List<String> storeUserWithAutoPOList = UserDAO.getStoreUsersWithAutoPO();
    Iterator<String> storeUserWithAutoPOIterator = storeUserWithAutoPOList.iterator();
    StringBuilder storeUserWithAutoPO = new StringBuilder();
    while (storeUserWithAutoPOIterator.hasNext()) {
      storeUserWithAutoPO.append(storeUserWithAutoPOIterator.next()).append(",");
    }

    req.setAttribute("store_usersWithAutoPO",
        storeUserWithAutoPO.length() > 0
            ? storeUserWithAutoPO.substring(0, storeUserWithAutoPO.length() - 1)
            : "");
    req.setAttribute("roleId", roleId);
    req.setAttribute("pagedList", pagedList);
    String appLimit = DataBaseUtil.getStringValueFromDb(
        "select po_approval_upto from u_user where emp_username=?", user);
    req.setAttribute("userpolimit", appLimit);
    req.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
    return am.findForward("list");
  }

  @IgnoreConfidentialFilters
  public ActionForward show(ActionMapping m, ActionForm f, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {
    String po_no = req.getParameter("po_no");
    BasicDynaBean podetails = StoresPOApprovalDAO.getPOApprovalRejectDetails(po_no);
    req.setAttribute("podetails", podetails);
    List<BasicDynaBean> polist = null;
    int store_id = 0;
    store_id = Integer.parseInt(podetails.get("store_id").toString());
    polist = StoresPOApprovalDAO.getPOItemDetails(po_no, store_id);
    req.setAttribute("polist", polist);
    String storeName = StoresIndentDAO.getStoreName(store_id);
    req.setAttribute("storeName", storeName);

    HttpSession session = req.getSession(false);
    String dept_id = (String) session.getAttribute("pharmacyStoreId");
    if (dept_id != null && !dept_id.equals("")) {
      BasicDynaBean dept = new GenericDAO("stores").findByKey("dept_id", Integer.parseInt(dept_id));
      String dept_name = dept.get("dept_name").toString();
      req.setAttribute("dept_id", dept_id);
      req.setAttribute("dept_name", dept_name);
    }
    if (dept_id != null && dept_id.equals("")) {
      req.setAttribute("dept_id", dept_id);
    }
    req.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
    return m.findForward("addshow");
  }

  public ActionForward update(ActionMapping m, ActionForm f, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {

    HttpSession session = req.getSession();
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    Map params = req.getParameterMap();
    List errors = new ArrayList();

    Map fields = new HashMap();
    Map keys = new HashMap();

    String po_no = req.getParameter("poNo");
    String msg = "Approval/Rejection Updation Failed for PO No: " + po_no;

    java.sql.Timestamp approval_time = DataBaseUtil.getDateandTime();
    String userid = (String) session.getAttribute("userid");
    String status = req.getParameter("status_main");

    if (status.equals("A")) {
      fields.put("approved_by", userid);
      fields.put("approved_time", approval_time);
      fields.put("approver_remarks", req.getParameter("remarks"));
    } else if (status.equals("FC")) {
      fields.put("closure_reasons", req.getParameter("remarks"));
    } else if (status.equals("AA")) {
      fields.put("amendment_approved_by", userid);
      fields.put("amendment_approved_time", approval_time);
      fields.put("amendment_approver_remarks", req.getParameter("remarks"));
    }
    fields.put("status", status);
    fields.put("last_modified_by", userid);
    keys.put("po_no", po_no);

    int mresult = mdao.update(con, fields, keys);

    if (mresult > 0) {

      String[] itemname = req.getParameterValues("medicine_name");
      Map ikeys = null;
      int update = 0;
      boolean success = false;
      if (itemname != null) {
        for (int i = 0; i < itemname.length; i++) {
          BasicDynaBean bean = dao.getBean();
          ConversionUtils.copyIndexToDynaBean(params, i, bean, errors);
          ikeys = new HashMap();
          ikeys.put("po_no", po_no);
          ikeys.put("medicine_id", bean.get("medicine_id"));
          if (status.equals("FC"))
            bean.set("status", status);
          update = update + dao.update(con, bean.getMap(), ikeys);
        }
      }

      if (mresult > 0 && itemname == null) {
        success = true;
        msg = "PO details Updation Successfully done for PO No: " + po_no;
        DataBaseUtil.commitClose(con, success);
      }
      if (itemname != null && update == itemname.length) {
        success = true;
        msg = "Approval/Rejection Updation Successfully done for PO No: " + po_no;
        DataBaseUtil.commitClose(con, success);
      }
    }

    if ((status.equals("A") || status.equals("AA"))
        && ((Boolean) (GenericPreferencesDAO.getAllPrefs()).get("auto_mail_po_to_sup"))) {

      // auto approved po email to the supplier
      MessageManager mgr = new MessageManager();
      Map reportData = new HashMap();
      reportData.put("po_no", new String[] { po_no });
      reportData.put("printType", req.getParameter("printType"));
      reportData.put("template_name", req.getParameter("template_name"));
      mgr.processEvent("purches_order_report", reportData);

    }

    FlashScope flash = FlashScope.getScope(req);
    flash.put("success", msg);
    ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("status", "O");
    redirect.addParameter("status", "V");
    redirect.addParameter("status", "AO");
    redirect.addParameter("status", "AV");
    redirect.addParameter("sortOrder", "po_no");
    redirect.addParameter("sortReverse", true);
    return redirect;
  }
}
