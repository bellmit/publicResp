package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.usermanager.Role;
import com.insta.hms.usermanager.User;
import com.insta.hms.usermanager.UserDAO;
import com.insta.hms.usermanager.UserDashBoardDAO;
import com.lowagie.text.DocumentException;
import flexjson.JSONSerializer;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class StoresIndentAction.
 */
public class StoresIndentAction extends BaseAction {

  /** The dao. */
  static StoresIndentDAO dao = new StoresIndentDAO();
  
  /** The mdao. */
  static GenericDAO mdao = new GenericDAO("store_indent_main");
  
  private static final GenericDAO storesDAO = new GenericDAO("stores");
  private static final GenericDAO orderKitMainDAO = new GenericDAO("order_kit_main");
  
  /** The js. */
  static JSONSerializer js = new JSONSerializer().exclude("class");

  /**
   * List.
   *
   * @param am the am
   * @param af the af
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {

    Map<Object, Object> map = getParameterMap(req);
    PagedList pagedList = null;
    HttpSession session = req.getSession(false);
    String userId = (String) session.getAttribute("userid");
    /** check if user has access right to view data of all stores. */
    HashMap actionRightsMap = new HashMap();
    Object roleID = null;
    Role role = new Role();
    actionRightsMap = (HashMap) req.getSession(false).getAttribute("actionRightsMap");
    roleID = req.getSession(false).getAttribute("roleId");
    String actionRightStatus = (String) session.getAttribute("multiStoreAccess");
    if (actionRightStatus == null) {
      actionRightStatus = "N";
    }
    int roleId = ((Integer) roleID).intValue();
    if ((actionRightStatus.equals("A")) || (roleId == 1 || (roleId == 2))) {
      /** User has access to all stores or he is in admin role..let him see all indents */
      pagedList = StoresIndentDAO.searchReqCentersIndentList(map,
          ConversionUtils.getListingParameter(map));
    } else {
      /** user can see data only that he created */
      if (userId != null && !userId.equals("")) {
        map.put("requester_name", new String[] { userId });
        pagedList = StoresIndentDAO.searchReqCentersIndentList(map,
            ConversionUtils.getListingParameter(map));
      } else {
        pagedList = new PagedList();
      }
    }
    String userid = (String) session.getAttribute("userid");
    req.setAttribute("username", userid);
    req.setAttribute("pagedList", pagedList);
    List allUserNames = UserDashBoardDAO.getAllUserNames();
    String deptId = (String) session.getAttribute("pharmacyStoreId");
    if (deptId != null && !deptId.equals("")) {
      BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(deptId));
      String deptName = dept.get("dept_name").toString();
      req.setAttribute("store_id", deptId);
      req.setAttribute("store_name", deptName);
    }

    req.setAttribute("userNameList", new JSONSerializer().serialize(allUserNames));
    req.setAttribute("stock_timestamp", MedicineStockDAO.getMedicineTimestamp());
    req.setAttribute("master_timestamp", PharmacymasterDAO.getItemMasterTimestamp());
    req.setAttribute("multiCentered",
        GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1);
    req.setAttribute("centers", CenterMasterDAO.getAllCentersExceptSuper());
    return am.findForward("list");
  }

  /**
   * Adds the.
   *
   * @param am the am
   * @param af the af
   * @param req the req
   * @param resp the resp
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward add(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {

    HttpSession session = req.getSession();
    String userid = (String) session.getAttribute("userid");
    User user = getUserStore(userid);
    if (user != null && user.getPharmacyStoreId() != null) {
      String deptid = user.getPharmacyStoreId();
      if ((deptid != null) && (deptid.length() > 0) && (!deptid.equals(""))) {
        req.setAttribute("store_id", user.getPharmacyStoreId());
        int deptIdNum = Integer.parseInt(deptid);
        String deptname = getStoreNameFromId(deptIdNum);
        req.setAttribute("store_name", deptname);
      }
      if (deptid != null && deptid.equals("")) {
        req.setAttribute("store_id", deptid);
      }
    }
    int centerId = (Integer) req.getSession().getAttribute("centerId");

    HashMap filterMap = new HashMap();
    filterMap.put("is_super_store", "Y");
    filterMap.put("status", "A");

    if (GenericPreferencesDAO.getGenericPreferences().getAllow_cross_center_indents().equals("N")
        && centerId != 0) {
      filterMap.put("center_id", centerId);
    }
    List columns = new ArrayList();
    columns.add("dept_name");
    columns.add("dept_id");

    List list = storesDAO.listAll(columns, filterMap, "dept_name");
    req.setAttribute("storesList", list);
    req.setAttribute("storesList1", list);
    boolean multiCentered = GenericPreferencesDAO.getGenericPreferences()
        .getMax_centers_inc_default() > 1;

    if (multiCentered && centerId == 0) {
      req.setAttribute("error", "Add Indent is allowed only for center users.");
    }

    req.setAttribute("wards", BedMasterDAO.getAllWardNames(centerId, multiCentered));
    req.setAttribute("multiCentered", multiCentered);
    req.setAttribute("stock_timestamp", MedicineStockDAO.getMedicineTimestamp());
    req.setAttribute("master_timestamp", PharmacymasterDAO.getItemMasterTimestamp());
    String actionId = am.getProperty("action_id");
    String indentType = actionId.equals("stores_user_indent") ? "U" : "S";
    req.setAttribute("indentType", indentType);
    req.setAttribute("actionId", actionId);
    List<String> orderKitDetailsList = new ArrayList<String>();
    orderKitDetailsList.add("order_kit_id");
    orderKitDetailsList.add("order_kit_name");
    req.setAttribute("orderkitJSON", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
        orderKitMainDAO.listAll(orderKitDetailsList, "status", "A"))));
    return am.findForward("add");
  }

  /**
   * Gets the item details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param resp the resp
   * @return the item details
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getItemDetails(ActionMapping mapping, ActionForm form, HttpServletRequest
      req, HttpServletResponse resp) throws IOException, ServletException, Exception {

    String itemName = req.getParameter("itemname");
    String storeId = req.getParameter("store_id");
    String reqStore = req.getParameter("req_store_id");
    int reqStoreId = 0;
    if (reqStore != null && !reqStore.equals("")) {
      reqStoreId = Integer.parseInt(reqStore);
    }

    Map map = null;
    List<BasicDynaBean> list = StoresIndentDAO.getItemDetails(itemName, Integer.parseInt(storeId),
        reqStoreId);
    if (list != null && list.size() > 0) {
      map = list.get(0).getMap();
    }
    resp.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    resp.setContentType("application/json");
    resp.getWriter().write(js.serialize(map));
    resp.flushBuffer();
    return null;
  }

  /**
   * Gets the all issues in dept.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param resp the resp
   * @return the all issues in dept
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getAllIssuesInDept(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException,
      Exception {

    String storeId = req.getParameter("dept_id");

    List medicineNames = StoresDBTablesUtil.getUserIssuesForDept(storeId);
    resp.setContentType("text/plain");
    resp.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    resp.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(medicineNames)));

    return null;
  }

  /**
   * Creates the.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param resp the resp
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {
    String msg = "Indent Insertion Failure...";
    List<String> errors = new ArrayList<String>();
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    Map<String, String[]> itemsMap = req.getParameterMap();
    String[] itemId = itemsMap.get("medicine_id");
    int elelen = itemId.length - 1;
    int indentno = new GenericDAO("store_indent_details").getNextSequence();

    BasicDynaBean mbean = mdao.getBean();
    mbean.set("indent_no", indentno);
    mbean.set("date_time", DataBaseUtil.getDateandTime());
    mbean.set("indent_type", req.getParameter("indent_type"));
    mbean.set("indent_store", Integer.parseInt(req.getParameter("indent_store")));
    if (req.getParameter("indent_type").equals("U")) {
      mbean.set("dept_from",
          req.getParameter("dept_ward").equals("D")
              ? req.getParameter("dept")
              : req.getParameter("ward"));
      mbean.set("location_type", req.getParameter("dept_ward"));
    } else if (req.getParameter("indent_type").equals("S")) {
      mbean.set("dept_from", req.getParameter("store_to"));
    }
    HttpSession session = req.getSession();
    mbean.set("requester_name", session.getAttribute("userid"));
    mbean.set("expected_date", DateUtil.parseTimestamp(req.getParameter("expected_date"),
        req.getParameter("expected_time")));
    mbean.set("remarks", req.getParameter("remarks"));
    mbean.set("status", req.getParameter("status"));
    mbean.set("store_user", session.getAttribute("userid"));
    // center ,where the user does the save
    mbean.set("requesting_center_id", session.getAttribute("centerId"));
    boolean mresult = mdao.insert(con, mbean);
    if (mresult) {
      List<BasicDynaBean> insertList = new ArrayList<BasicDynaBean>();

      String[] indentdelete = itemsMap.get("indentdel");
      for (int i = 0; i < elelen; i++) {
        BasicDynaBean bean = dao.getBean();
        if (indentdelete[i].equals("false")) {
          ConversionUtils.copyIndexToDynaBean(itemsMap, i, bean, errors);
          bean.set("status", "O"); // Indent Raised
          bean.set("indent_no", indentno);
          insertList.add(bean);
        }
      }

      boolean result = dao.insertAll(con, insertList);
      if (result) {
        msg = "Insertion Successful..Indent No. is : " + indentno + " ";
        DataBaseUtil.commitClose(con, result);
      }
    }
    FlashScope flash = FlashScope.getScope(req);
    flash.put("success", msg);
    flash.put("report", "true");
    flash.put("indentno", indentno);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("status", "O");
    redirect.addParameter("sortOrder", "indent_no");
    redirect.addParameter("sortReverse", true);
    return redirect;
  }

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param resp the resp
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {
    int indentNo = Integer.parseInt(req.getParameter("indent_no"));
    BasicDynaBean indentdetails = mdao.findByKey("indent_no", indentNo);
    req.setAttribute("indentdetails", indentdetails);
    req.setAttribute("indentType", indentdetails.get("indent_type"));
    List<BasicDynaBean> indentlist = null;
    int storeId = 0;
    String deptId = "";
    if (indentdetails.get("indent_type").equals("S")) {
      storeId = Integer.parseInt(indentdetails.get("dept_from").toString());
      int deptidNum = (Integer) indentdetails.get("indent_store");
      deptId = ((Integer) indentdetails.get("indent_store")).toString();
      indentlist = StoresIndentDAO.getIndentItemDetails(indentNo, deptidNum);
    } else {
      storeId = Integer.parseInt(indentdetails.get("indent_store").toString());
      indentlist = StoresIndentDAO.getIndentItemDetails(indentNo, storeId);
      deptId = ((Integer) indentdetails.get("indent_store")).toString();
    }
    req.setAttribute("indentlist", indentlist);
    if (indentdetails.get("indent_type").equals("S")) {
      String storeName = StoresIndentDAO
          .getStoreName(Integer.parseInt((String) indentdetails.get("dept_from")));
      req.setAttribute("store_name", storeName);
      req.setAttribute("store_id", storeId);
    } else {
      req.setAttribute("store_id", deptId);
    }
    HttpSession session = req.getSession();
    req.setAttribute("requester_name", session.getAttribute("userid"));

    HashMap filterMap = new HashMap();
    filterMap.put("is_super_store", "Y");
    filterMap.put("status", "A");
    int centerId = (Integer) req.getSession().getAttribute("centerId");
    if (GenericPreferencesDAO.getGenericPreferences().getAllow_cross_center_indents().equals("N")
        && centerId != 0) {
      filterMap.put("center_id", centerId);
    }
    List columns = new ArrayList();
    columns.add("dept_name");
    columns.add("dept_id");
    List list = storesDAO.listAll(columns, filterMap, "dept_name");
    req.setAttribute("storesList", list);
    req.setAttribute("storesList1", list);
    boolean multiCentered = GenericPreferencesDAO.getGenericPreferences()
        .getMax_centers_inc_default() > 1;
    req.setAttribute("wards", BedMasterDAO.getAllWardNames(centerId, multiCentered));
    req.setAttribute("stock_timestamp", MedicineStockDAO.getMedicineTimestamp());
    req.setAttribute("master_timestamp", PharmacymasterDAO.getItemMasterTimestamp());
    req.setAttribute("actionId",
        indentdetails.get("indent_type").equals("S") ? "stores_transfer_indent" : "");
    List<String> orderKitDetailsList = new ArrayList<String>();
    orderKitDetailsList.add("order_kit_id");
    orderKitDetailsList.add("order_kit_name");
    req.setAttribute("orderkitJSON", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
        orderKitMainDAO.listAll(orderKitDetailsList, "status", "A"))));
    return mapping.findForward("add");
  }

  /**
   * View.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param resp the resp
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {
    int indentNo = Integer.parseInt(req.getParameter("indent_no"));
    BasicDynaBean indentdetails = StoresIndentDAO.getIndentApprovalRejectDetails(indentNo);
    req.setAttribute("indentdetails", indentdetails);
    List<BasicDynaBean> indentlist = null;
    int storeId = 0;
    if (indentdetails.get("indent_type").equals("S")) {
      storeId = Integer.parseInt(indentdetails.get("dept_from").toString());
      indentlist = StoresIndentDAO.getIndentItemDetails(indentNo, storeId);
    } else {
      storeId = Integer.parseInt(indentdetails.get("indent_store").toString());
      indentlist = StoresIndentDAO.getIndentItemDetails(indentNo, storeId);

    }
    req.setAttribute("indentlist", indentlist);
    String storeName = StoresIndentDAO
        .getStoreName(Integer.parseInt(indentdetails.get("indent_store").toString()));
    req.setAttribute("storeName", storeName);
    return mapping.findForward("show");
  }

  /**
   * Update.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param resp the resp
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {
    HttpSession session = req.getSession();
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      List<String> errors = new ArrayList<String>();
      FlashScope flash = FlashScope.getScope(req);
      Map<String, Object> fields = new HashMap<String, Object>();
      Map<String, String[]> itemsMap = req.getParameterMap();
      String[] itemId = itemsMap.get("medicine_id");
      int elelen = itemId.length - 1;
      String indentType = req.getParameter("indenttype");
      int indentNo = Integer.parseInt(req.getParameter("indentNo"));
      String err = "Updation Failed for Indent No: " + indentNo;

      int originalIndentStore = Integer.parseInt(req.getParameter("original_indent_store"));

      int indentStore = Integer.parseInt(req.getParameter("indent_store"));
      if (originalIndentStore != indentStore) {
        dao.delete(con, "indent_no", indentNo);
      }
      fields.put("updated_date", DataBaseUtil.getDateandTime());
      if (req.getParameter("status").equals("X")) {
        BasicDynaBean mainBean = mdao.findByKey("indent_no", indentNo);
        String status = (String) mainBean.get("status");
        if (!"O".equals(status)) {
          flash.put("error", "Updation Failed for Indent No: " + indentNo
              + ", Because current indent status is not open.");
          ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
          redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
          redirect.addParameter("status", "O");
          redirect.addParameter("sortOrder", "indent_no");
          redirect.addParameter("sortReverse", true);
          return redirect;
        }
        fields.put("status", req.getParameter("status"));
        fields.put("remarks", req.getParameter("remarks"));
        fields.put("cancelled_date", DataBaseUtil.getDateandTime());
        fields.put("cancelled_by", session.getAttribute("userid"));
      } else {
        fields.put("date_time", DataBaseUtil.getDateandTime());
        fields.put("requester_name", session.getAttribute("userid"));
        fields.put("expected_date", DateUtil.parseTimestamp(req.getParameter("expected_date"),
            req.getParameter("expected_time")));
        fields.put("remarks", req.getParameter("remarks"));
        fields.put("status", req.getParameter("status"));
        fields.put("store_user", session.getAttribute("userid"));
        if (indentType != null) {
          if (indentType.equals("U")) {
            fields.put("dept_from",
                req.getParameter("dept_ward").equals("D")
                    ? req.getParameter("dept")
                    : req.getParameter("ward"));
            fields.put("location_type", req.getParameter("dept_ward"));
          } else {
            fields.put("indent_store", Integer.parseInt(req.getParameter("indent_store")));
          }
        }
      }

      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("indent_no", indentNo);

      int mresult = mdao.update(con, fields, keys);

      if (mresult > 0) {
        List<BasicDynaBean> insertList = new ArrayList<BasicDynaBean>();
        List<BasicDynaBean> updateList = new ArrayList<BasicDynaBean>();
        List<BasicDynaBean> deleteList = new ArrayList<BasicDynaBean>();

        String[] indentdelete = itemsMap.get("indentdel");
        String[] indentno = req.getParameterValues("indent_no");
        for (int i = 0; i < elelen; i++) {
          BasicDynaBean bean = dao.getBean();
          ConversionUtils.copyIndexToDynaBean(itemsMap, i, bean, errors);
          if (req.getParameter("status").equals("X")) {
            bean.set("status", "C");
          } else {
            bean.set("status", "O");
          }
          if (indentno[i].equals("")) {
            if (indentdelete[i].equals("false")) {
              bean.set("indent_no", indentNo);
              insertList.add(bean);
            }
          } else {
            if (indentdelete[i].equals("false")) {
              updateList.add(bean);
            } else {
              deleteList.add(bean);
            }
          }
        }
        boolean result = true;
        if (insertList.size() > 0) {
          result = dao.insertAll(con, insertList);
        }
        boolean delete = true;
        Map<String, Object> ikeys = null;
        int update = 0;
        if (updateList.size() > 0) {
          for (BasicDynaBean bean : updateList) {
            ikeys = new HashMap<String, Object>();
            ikeys.put("indent_no", bean.get("indent_no"));
            ikeys.put("medicine_name", bean.get("medicine_name"));
            update = update + dao.update(con, bean.getMap(), ikeys);
          }
        }
        if (deleteList.size() > 0) {
          delete = StoresIndentDAO.deleteIndent(con, deleteList);
        }

        if (result && delete && (update == updateList.size())) {
          flash.put("success", "Updation Successfully done for Indent No: " + indentNo);
          con.commit();
        } else {
          flash.put("error", err);
          con.rollback();
        }
      }
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      redirect.addParameter("status", "O");
      redirect.addParameter("sortOrder", "indent_no");
      redirect.addParameter("sortReverse", true);
      return redirect;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the user store.
   *
   * @param username the username
   * @return the user store
   * @throws SQLException the SQL exception
   */
  public static User getUserStore(String username) throws SQLException {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      User usr = new UserDAO(con).getUser(username);
      return usr;
    } finally {
      if(null != con){
        con.close();
      }
    }

  }

  /** The Constant USER_DEPT. */
  private static final String USER_DEPT = "SELECT dept_name FROM stores " + " WHERE dept_id=";

  /**
   * Gets the store name from id.
   *
   * @param storeid the storeid
   * @return the store name from id
   */
  public static String getStoreNameFromId(int storeid) {
    String qry = USER_DEPT + storeid;
    String deptname = DataBaseUtil.getStringValueFromDb(qry);
    return deptname;
  }

  /**
   * Generate report.
   *
   * @param mapping the mapping
   * @param af the af
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws XPathExpressionException the x path expression exception
   * @throws DocumentException the document exception
   * @throws TransformerException the transformer exception
   */
  @IgnoreConfidentialFilters
  public ActionForward generateReport(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
      TemplateException, XPathExpressionException, DocumentException, TransformerException {

    Connection con = null;
    Template tplt = null;
    HashMap params = new HashMap();
    int storeId = 0;
    int indentNo = Integer.parseInt(request.getParameter("indent_no"));
    BasicDynaBean indentdetails = StoresIndentDAO.getIndentApprovalRejectDetails(indentNo);
    List<BasicDynaBean> indentlist = null;
    storeId = Integer.parseInt(indentdetails.get("indent_store").toString());
    BasicDynaBean indentBean = mdao.findByKey("indent_no",
        indentNo);
    String deptFrom = (indentBean != null && ((String) indentBean.get("indent_type")).equals("S"))
        ? (String) indentBean.get("dept_from")
        : "-999";
    indentlist = StoresIndentDAO.getIndentItemDetailsForProcess(indentNo, storeId, "print",
        Integer.parseInt(deptFrom));

    request.setAttribute("indentdetails", indentdetails);

    String indenttype = (String) indentdetails.get("indent_type");
    if (indenttype.equals("S")) {
      String storeName = StoresIndentDAO
          .getStoreName(Integer.parseInt(indentdetails.get("dept_from").toString()));
      params.put("storeName", storeName);
    }

    params.put("indentdetails", indentdetails);
    params.put("indentlist", indentlist);
    List list = StoresIndentDAO.getItemIdentifierDetails(indentNo, storeId);
    params.put("identifierList", list);

    BasicDynaBean printprefs = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_STORE);

    PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();

    String templateContent = printtemplatedao
        .getCustomizedTemplate(PrintTemplate.View_Raise_Indent_print);
    if (templateContent == null || templateContent.equals("")) {
      tplt = AppInit.getFmConfig()
          .getTemplate(PrintTemplate.View_Raise_Indent_print.getFtlName() + ".ftl");
    } else {
      StringReader reader = new StringReader(templateContent);
      tplt = new Template("VieworRaiseIndentPrint.ftl", reader, AppInit.getFmConfig());
    }

    StringWriter writer = new StringWriter();
    tplt.process(params, writer);
    String printContent = writer.toString();
    HtmlConverter hc = new HtmlConverter();
    if (printprefs.get("print_mode").equals("P")) {
      OutputStream os = response.getOutputStream();
      response.setContentType("application/pdf");
      hc.writePdf(os, printContent, "vieworraiseindent", printprefs, false, false, true, true, true,
          false);
    } else {
      String textReport = null;
      textReport = new String(
          hc.getText(printContent, "vieworraiseindentText", printprefs, true, true));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", printprefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");
    }

    return null;
  }

  /**
   * This method is use to get orderkit items with batches.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param res the res
   * @return the order kit items JSON
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getOrderKitItemsJSON(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse res) throws Exception {

    res.setContentType("text/javascript");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    Map<String, Object> orderKitDetailsMap = new HashMap<String, Object>();

    int orderKitId = Integer.parseInt(request.getParameter("order_kit_id"));
    int indentStoreId = Integer.parseInt(request.getParameter("indentStoreId"));
    int reqStoreId = Integer.parseInt(request.getParameter("reqStoreId"));

    // to get all order kit items
    List<BasicDynaBean> orderKitItemsStockInfo = StoresIndentDAO.getOrderKitItemDetails(orderKitId,
        indentStoreId, reqStoreId);
    orderKitDetailsMap.put("order_kit_items",
        ConversionUtils.copyListDynaBeansToMap(orderKitItemsStockInfo));
    res.getWriter().write(js.deepSerialize(orderKitDetailsMap));
    res.flushBuffer();
    return null;
  }

}
