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
import com.insta.hms.usermanager.UserDAO;
import com.insta.hms.usermanager.UserDashBoardDAO;
import com.lowagie.text.DocumentException;
import flexjson.JSONSerializer;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URLDecoder;
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

public class StoresUserIndentApprovalAction extends BaseAction {
  
  private static final GenericDAO storeIndentMainDAO = new GenericDAO("store_indent_main");
  private static final GenericDAO storesDAO = new GenericDAO("stores");
  
  @SuppressWarnings("unchecked")
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {

    int centerId = (Integer) req.getSession().getAttribute("centerId");
    boolean multiCentered = GenericPreferencesDAO.getGenericPreferences()
        .getMax_centers_inc_default() > 1;
    Map<Object, Object> map = this.getParameterMap(req);
    PagedList pagedList = new PagedList();
    String deptFrom = req.getParameter("dept_from");
    String indentStore = req.getParameter("indent_store");
    indentStore = (indentStore == null || indentStore.isEmpty()) ? null : indentStore;
    deptFrom = (deptFrom == null || deptFrom.isEmpty()) ? null : deptFrom;
    String userName = (String) req.getSession(false).getAttribute("userid");
    String indentApprovalBy = (String) GenericPreferencesDAO.getAllPrefs()
        .get("indent_approval_by");
    String indentStores = null;
    String requestStores = null;
    String isUserHavingSuperStore = "N";
    int roleId = (Integer) req.getSession(false).getAttribute("roleId");

    indentStores = getIndentStores(userName, indentStore, indentApprovalBy);
    requestStores = getRequestingStores(userName, deptFrom, indentApprovalBy);

    isUserHavingSuperStore = (isSuperStoreUser(indentStores)) ? "Y" : "N";
    if (roleId == 1 || roleId == 2) {
      pagedList = StoresIndentDAO.searchUserIndentList(map,
          ConversionUtils.getListingParameter(map), null);
    } else {
      if (indentApprovalBy.equals("I")) {
        if (indentStores != null) {
          pagedList = StoresIndentDAO.searchUserIndentList(map,
              ConversionUtils.getListingParameter(map), indentStores);
        }
      } else if (indentApprovalBy.equals("R")) {
        pagedList = StoresIndentDAO.searchUserIndentList(map,
            ConversionUtils.getListingParameter(map), null, null, indentApprovalBy);
      }
    }

    req.setAttribute("pagedList", pagedList);
    List allUserNames = UserDashBoardDAO.getAllUserNames();
    req.setAttribute("userNameList", new JSONSerializer().serialize(allUserNames));
    req.setAttribute("multiCentered",
        GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1);
    req.setAttribute("centers", CenterMasterDAO.getAllCentersExceptSuper());
    req.setAttribute("indentApprovalBy", indentApprovalBy);
    req.setAttribute("wards", BedMasterDAO.getAllWardNames(centerId, multiCentered));
    req.setAttribute("isUserHavingSuperStoreJson",
        new JSONSerializer().exclude("class").serialize(isUserHavingSuperStore));
    if (GenericPreferencesDAO.getGenericPreferences().getAllow_cross_center_indents().equals("N")) {
      req.setAttribute("storesList", getStoresList(req));
      req.setAttribute("indent_store", indentStore);
    }
    return am.findForward("list");
  }
  /*
   * To get user stores
   */

  public String getUserStores(String userName) throws IOException, ServletException, Exception {
    BasicDynaBean usreStoresBean = null;
    String userStores = null;
    usreStoresBean = UserDAO.getRecord(userName);
    if (usreStoresBean != null) {
      userStores = (String) usreStoresBean.get("multi_store");
    }
    return userStores;
  }

  public boolean isSuperStoreUser(String userStores) {
    boolean isUserHavingSuperStore = false;
    String[] userStoresArr = null;
    if (userStores != null) {
      if (userStores.contains(",")) {
        userStoresArr = userStores.split(",");
      } else {
        userStoresArr = new String[] { userStores };
      }
      for (int i = 0; i < userStoresArr.length; i++) {
        String userSuperStore = DataBaseUtil
            .getStringValueFromDb("SELECT  is_super_store from stores where dept_id = "
                + Integer.parseInt(userStoresArr[i]) + " AND status = 'A'");
        if (userSuperStore != null && userSuperStore.equals("Y")) {
          isUserHavingSuperStore = true;
          break;
        }
      }
    }

    return isUserHavingSuperStore;
  }

  public String getIndentStores(String userName, String indentStore, String indentApprovalBy)
      throws IOException, ServletException, Exception {
    String indentStores = null;
    if (null != indentStore) {
      return indentStore; // 1 store, return the same
    }

    indentStores = getUserStores(userName); // Approval by Indent store user, return all user stores

    return indentStores; // will be comma separated list of user stores if approval by "I", null if
                         // approval by "R"
  }

  public String getRequestingStores(String userName, String requestingStrore,
      String indentApprovalBy) throws IOException, ServletException, Exception {
    String deptFrom = null;
    if (null != requestingStrore) {
      return requestingStrore; // 1 store, return the same
    }

    deptFrom = getUserStores(userName); // Approval by Requesting store user, return all user stores

    return deptFrom; // will be comma separated list of user stores if approval by "R", null if
                     // approval by "I"
  }
  /* Get the center specific stores list */

  public List getStoresList(HttpServletRequest req)
      throws IOException, ServletException, Exception {
    int centerId = (Integer) req.getSession().getAttribute("centerId");
    List storeList = null;
    if (GenericPreferencesDAO.getGenericPreferences().getAllow_cross_center_indents().equals("N")) {
      HashMap filterMap = new HashMap();
      filterMap.put("is_super_store", "Y");
      filterMap.put("status", "A");
      if (centerId != 0) {
        filterMap.put("center_id", centerId);
      }
      List columns = new ArrayList();
      columns.add("dept_name");
      columns.add("dept_id");
      storeList = storesDAO.listAll(columns, filterMap, "dept_name");
    }
    return storeList;
  }
  @IgnoreConfidentialFilters
  public ActionForward show(ActionMapping m, ActionForm f, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {
    int indent_no = Integer.parseInt(req.getParameter("indent_no"));
    BasicDynaBean indentdetails = StoresIndentDAO.getIndentApprovalRejectDetails(indent_no);
    req.setAttribute("indentdetails", indentdetails);
    List<BasicDynaBean> indentlist = null;
    int store_id = 0;
    store_id = Integer.parseInt(indentdetails.get("indent_store").toString());
    indentlist = StoresIndentDAO.getIndentItemDetails(indent_no, store_id);
    if (indentdetails.get("indent_type").equals("S"))
      req.setAttribute("deptFrom", StoresIndentDAO
          .getStoreName(Integer.parseInt(indentdetails.get("dept_from").toString())));

    HttpSession session = req.getSession(false);
    String dept_id = (String) session.getAttribute("pharmacyStoreId");
    if (dept_id != null && !dept_id.equals("")) {
      BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(dept_id));
      String dept_name = dept.get("dept_name").toString();
      req.setAttribute("dept_id", dept_id);
      req.setAttribute("dept_name", dept_name);
    }
    if (dept_id != null && dept_id.equals("")) {
      req.setAttribute("dept_id", dept_id);
    }

    req.setAttribute("indentlist", indentlist);
    String storeName = StoresIndentDAO
        .getStoreName(Integer.parseInt(indentdetails.get("indent_store").toString()));
    req.setAttribute("storeName", storeName);
    String indentApprovalBy = (String) GenericPreferencesDAO.getAllPrefs()
        .get("indent_approval_by");
    req.setAttribute("indentApprovalBy", indentApprovalBy);
    return m.findForward("addshow");
  }

  public ActionForward update(ActionMapping m, ActionForm f, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException, Exception {

    StoresIndentDAO dao = new StoresIndentDAO();

    HttpSession session = req.getSession();
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    Map<String, String[]> params = req.getParameterMap();

    Map<String, Object> fields = new HashMap<String, Object>();
    Map<String, Object> keys = new HashMap<String, Object>();

    String[] item_name = params.get("medicine_name");
    String[] itemstatus = params.get("status");
    String[] qty = params.get("item_qty");
    for (int i = 0; i < item_name.length; i++) {
      item_name[i] = URLDecoder.decode(item_name[i], "UTF-8");
    }

    int indent_no = Integer.parseInt(req.getParameter("indentNo"));
    String msg = "Approval/Rejection Updation Failed for Indent No: " + indent_no;

    java.sql.Timestamp approval_time = DataBaseUtil.getDateandTime();
    String userid = (String) session.getAttribute("userid");
    String status = req.getParameter("status_main");
    fields.put("approved_date", approval_time);
    fields.put("approved_by", userid);
    fields.put("expected_date", DateUtil.parseTimestamp(req.getParameter("expected_date"),
        req.getParameter("expected_time")));
    if (status.equals("A")) {
      fields.put("approver_remarks", req.getParameter("remarks"));
    } else if (status.equals("R")) {
      fields.put("closure_reasons", req.getParameter("remarks"));
    }

    keys.put("indent_no", indent_no);
    boolean success = false;
    String[] itemid = req.getParameterValues("medicine_id");
    Map<String, Object> ikeys = null;
    int update = 0;
    int rejectedItems = 0;
    for (int i = 0; i < itemid.length; i++) {
      BasicDynaBean bean = dao.getBean();
      if (status.equals("R")) {
        bean.set("status", "R");
      } else {
        bean.set("approved_by", userid);
        bean.set("approved_time", approval_time);
        bean.set("status", itemstatus[i]);
        if (itemstatus[i].equals("R")) {
          rejectedItems++;
        }
      }
      bean.set("qty", new BigDecimal(qty[i]));
      ikeys = new HashMap<String, Object>();
      ikeys.put("indent_no", indent_no);
      ikeys.put("medicine_name", item_name[i]);
      update = update + dao.update(con, bean.getMap(), ikeys);
    }
    if (update == itemid.length)
      success = true;

    if (rejectedItems == itemid.length) {
      fields.put("status", "R");
    } else {
      fields.put("status", status);
    }

    if (success)
      success = storeIndentMainDAO.update(con, fields, keys) > 0;

    if (success) {
      msg = "Approval/Rejection Updation Successfully done for Indent No: " + indent_no;
    } else {
      msg = "Approval/Rejection Updation failed for Indent No: " + indent_no;
    }

    DataBaseUtil.commitClose(con, success);
    FlashScope flash = FlashScope.getScope(req);
    flash.put(success ? "success" : "error", msg);
    ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("status", "O");
    redirect.addParameter("sortOrder", "indent_no");
    redirect.addParameter("sortReverse", true);
    return redirect;
  }

  @IgnoreConfidentialFilters
  public ActionForward generateReport(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
      TemplateException, XPathExpressionException, DocumentException, TransformerException {

    Template t = null;
    HashMap params = new HashMap();
    int store_id = 0;
    int indent_no = Integer.parseInt(request.getParameter("indent_no"));
    BasicDynaBean indentdetails = StoresIndentDAO.getIndentApprovalRejectDetails(indent_no);
    List<BasicDynaBean> indentlist = null;
    store_id = Integer.parseInt(indentdetails.get("indent_store").toString());
    BasicDynaBean indentBean = storeIndentMainDAO.findByKey("indent_no",
        indent_no);
    String deptFrom = (indentBean != null && ((String) indentBean.get("indent_type")).equals("S"))
        ? (String) indentBean.get("dept_from")
        : "-999";
    indentlist = StoresIndentDAO.getIndentItemDetailsForProcess(indent_no, store_id, "print",
        Integer.parseInt(deptFrom));

    request.setAttribute("indentdetails", indentdetails);
    List l = StoresIndentDAO.getItemIdentifierDetails(indent_no, store_id);

    String indenttype = (String) indentdetails.get("indent_type");
    if (indenttype.equals("S")) {
      String storeName = StoresIndentDAO
          .getStoreName(Integer.parseInt(indentdetails.get("dept_from").toString()));
      params.put("storeName", storeName);
    }

    params.put("indentdetails", indentdetails);
    params.put("indentlist", indentlist);
    params.put("identifierList", l);

    BasicDynaBean printprefs = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_STORE);

    PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();

    String templateContent = printtemplatedao
        .getCustomizedTemplate(PrintTemplate.approve_indent_print);
    if (templateContent == null || templateContent.equals("")) {
      t = AppInit.getFmConfig()
          .getTemplate(PrintTemplate.approve_indent_print.getFtlName() + ".ftl");
    } else {
      StringReader reader = new StringReader(templateContent);
      t = new Template("ApproveIndentPrint.ftl", reader, AppInit.getFmConfig());
    }

    StringWriter writer = new StringWriter();
    t.process(params, writer);
    String printContent = writer.toString();
    HtmlConverter hc = new HtmlConverter();
    if (printprefs.get("print_mode").equals("P")) {
      OutputStream os = response.getOutputStream();
      response.setContentType("application/pdf");
      hc.writePdf(os, printContent, "ApproveIndentPrint", printprefs, false, false, true, true,
          true, false);
    } else {
      String textReport = null;
      textReport = new String(
          hc.getText(printContent, "approveindentText", printprefs, true, true));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", printprefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");
    }

    return null;
  }

}
