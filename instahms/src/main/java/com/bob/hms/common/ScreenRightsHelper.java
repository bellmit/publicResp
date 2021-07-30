/**
 *
 */

package com.bob.hms.common;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.MenuConfig;
import com.insta.hms.common.MenuGroup;
import com.insta.hms.common.MenuItem;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.UrlUtil;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class ScreenRightsHelper.
 *
 * @author krishna.t
 */
public class ScreenRightsHelper {

  static Logger logger = LoggerFactory.getLogger(ScreenRightsHelper.class);

  /*
   * Get screens authorized, groups authorized and screens available information.
   *
   * This does not assume that all groups/screens are listed in the rights related tables. When a
   * row is not present, it defaults it to N (for groups) or to the parent group's rights (for
   * screens)
   *
   * Note that a group's rights should not be used directly to determine if the group link is
   * available in a menu. (Groups really don't have rights, since there is no functionality
   * associated with a group). A group's link is available to a user in a menu as long as any screen
   * in the group is available.
   *
   */
  private static final String getScreenRightsQuery = "SELECT screen_id, rights"
      + " FROM screen_rights WHERE role_id=?";

  private static final String getActionRightsQuery = "SELECT action, rights"
      + " FROM action_rights WHERE role_id=?";

  private static final String getUrlRightsQuery = "SELECT action_id, rights"
      + " FROM url_action_rights WHERE role_id=?";

  private static final String[] reportActions = { "fin_fav_rpt_dashbd", "rep_rev_dashboard",
      //"rep_revenueaccrual", "rep_revenue_builder", "revenue_accrual_report_builder",
      "rep_collection_detailed_builder", "rep_collection_allocation_builder",
      "payment_details_builder", "payments_paid_builder", "rep_accounting",
      "rep_payment_consolidated_builder", "ip_outstand_report", "rep_revenue_doctor",
      "bill_fav_rpt_dashbd", "rep_bill_builder", "rep_bill_adj_builder", "rep_bill_charge_builder",
      "rep_bill_charge_adj_builder", "rep_patient_dues_builder", "billing_daybook",
      "daybook_builder", "deposit_builder", "deposit_receipts_builder", "rep_payment_insurance",
      "rep_trans_rev", "rep_rate_variation", "rep_bill_status_change", "rep_reward_points",
      "sale_fav_rpt_dashbd", "ph_sale_bill_reports", "ph_sale_item_rep_builder",
      "rep_pharmacy_pharmacy_dues_report", "pharmacy_doc_wise_sales_report", "st_hdrug_sales",
      "rep_store_sales_vat_report", "pres_lead_time_builder", "rep_markup_rates_report",
      "stock_consumption_rep_builder", "st_in_out_reg_blder", "sales_abc_blder",
      "patient_indent_item_rep_builder", "rep_karnataka_sales_tax", "proc_fav_rpt_dashbd",
      "ph_invoice_rep_builder", "ph_purchase_rep_builder", "rep_pharmacy_purchase_vat_report",
      "rep_pharmacy_purchase_items_vat_report", "rep_pharmacy_purchase_vat_abated_report",
      "stores_supp_ret_builder", "ph_pend_rep_report", "ph_supplier_payments",
      "rep_pharmacy_bonus_items", "po_items_builder", "po_pending_item_builder", "po_builder",
      "rep_store_purchasesummary", "rep_store_purchase_details", "store_mg_fav_rpt_dashbd",
      "store_item_master_builder", "ph_indvl_stock_builder", "ph_itemwise_stock_builder",
      "indent_flow_builder", "indent_item_builder", "ph_stocktransfer_rep_builder",
      "ph_stock_adj_rep_builder", "ph_stock_movement", "rep_pharmacy_stock_chk_point",
      "rep_pharmacy_stock_catg_chk_point", "rep_pharmacy_surplus_stock", "rep_stock_ledger",
      "rep_pharmacy_chkpoint_difference", "rep_pharmacy_tax", "reagent_consumables_builder",
      "diag_fav_rpt_dashbd", "diag_builder", "diag_rev_builder", "rep_incoming_test_details",
      "rep_outgoing_test_details", "rep_progressive_test_details", "tests_presc_ordered_report",
      "rep_test_equipment_details_builder", "sample_rejections_builder",
      "diag_internal_lab_builder", "rep_patient_packages_builder", "dial_fav_rpt_dashbd",
      "dialysis_builder", "rep_dialysis_frequency", "rep_clinical_outcome",
      "intra_dialysis_builder", "clini_fav_rpt_dashbd", "lab_test_result_builder",
      "rep_clinical_lab", "rep_icd_codification", "rep_codes", "rep_coder_claim_reveiws",
      "donor_details_builder", "ivf_treatment_cycle_builder", "serv_fav_rpt_dashbd",
      "service_builder", "services_presc_ordered_report", "consul_fav_rpt_dashbd",
      "rep_doc_consul_builder", "rep_presc_det_builder", "sch_fav_rpt_dashbd",
      "Scheduler_Doctors_Appointment", "Scheduler_Tests_Appointment",
      "Scheduler_Services_Appointment", "Scheduler_Surgery_Appointment", "Doctor_Appointment",
      "patientfeedback_rep", "ot_fav_rpt_dashbd", "ot_schedule_rep_builder", "bedu_fav_rpt_dashbd",
      "bed_occu_rep_builder", "bed_ward_wise_occu_rep_builder", "patient_bed_occ_builder",
      "ip_bed_utilization", "luxury_tax_rep_builder", "dental_trtment_report_builder",
      "dental_supplies_report_builder", "service_subtask_builder",
      "patient_activities_report_builder", "doctor_order_report_builder", "mas_fav_rpt_dashbd",
      "rep_rate_master_print", "custom_rpt_list", "custom_rpt_add", "fav_rpt_dashbd",
      "pat_fav_rpt_dashbd", "patient_details_builder", "rep_visit_details_builder",
      "rep_vital_measurement_builder", "rep_patientstats_table_builder",
      "rep_patientstats_trend_builder", "rep_patientvisitstats_trend_builder",
      "rep_patient_admitdischarge_trend_builder", "rep_ip_list_print", "rep_ipstats",
      "rep_ipopstats", "birth_details", "bed_dur_occu_rep_builder", "patient_duplicate_report",
      "followup_details", "maint_schedule_rep_builder", "maint_activity_rep_builder",
      "complaints_rep_builder", "contract_report_builder", "license_report_builder",
      "ins_fav_rpt_dashbd", "rep_insurance_claim_builder", "rep_insurance_claim_det_builder",
      "rep_insurance_claim_batch_builder", "pbm_prescription_builder",
      "pbm_prescription_item_builder", "diet_fav_rpt_dashbd", "dietary_daily_meals_report",
      "mrd_fav_rpt_dashbd", "case_file_details_builder", "statistics", 
      "patient_pending_prescription" };

  private static MessageUtil messageUtil = ApplicationContextProvider.getBean(MessageUtil.class);

  /**
   * Sets the screen rights.
   *
   * @param con    the con
   * @param roleId the role id
   * @throws SQLException the SQL exception
   */
  public static void setScreenRights(Connection con, int roleId) throws SQLException {
    ArrayList groupRights = null;
    ArrayList actionRights = null;
    Map urlRightsDbMap = null;

    // for roleId == 1 or 2 (InstaAdmin or su), we assume full access, no need to query the DB
    if ((roleId != 1) && (roleId != 2)) {
      try (PreparedStatement stmt = con.prepareStatement(getActionRightsQuery)) {
        stmt.setInt(1, roleId);
        actionRights = DataBaseUtil.queryToArrayList(stmt);
      }
      try (PreparedStatement stmt = con.prepareStatement(getUrlRightsQuery)) {
        stmt.setInt(1, roleId);
        urlRightsDbMap = ConversionUtils.listBeanToMapBean(DataBaseUtil.queryToDynaList(stmt),
            "action_id");
      }
    }
    HttpServletRequest request = RequestContext.getHttpRequest();
    HttpSession session = request.getSession(false);
    Preferences prefs = (Preferences) session.getAttribute("preferences");
    Map actionUrlMap = (Map) session.getServletContext().getAttribute("actionUrlMap");
    List privilegedActions = (List) session.getServletContext().getAttribute("privilegedActions");
    Map screenActionMap = (Map) session.getServletContext().getAttribute("screenActionMap");
    Map actionScreenMap = (Map) session.getServletContext().getAttribute("actionScreenMap");

    HashMap actionRightsMap = new HashMap(); // User actions this user has access to
    HashMap urlRightsMap = new HashMap(); // URL actions that the user has access to
    HashMap menuAvlblMap = new HashMap(); // Menus (not menu items) that are available to the user
    HashMap groupAvlblMap = new HashMap(); // groups available: used in role screen.
    /*
     * Every URL Action must have an entry in urlRightsMap. Set it based on the following priority:
     * 1. If module is not available, then N 2. If screen is privileged, N unless role is 1 3. If
     * superuser, (roleId <=2), then A 4. Whatever is there in the DB
     */
    ScreenConfig screenConfig = (ScreenConfig) session.getServletContext()
        .getAttribute("screenConfig");
    if (actionUrlMap != null) {
      boolean reportDashboardAdded = false;
      List<String> reportActionsList = Arrays.asList(reportActions);
      for (Iterator uai = actionUrlMap.keySet().iterator(); uai.hasNext();) {
        String urlAction = (String) uai.next();
        if (urlAction.equals("report_job_status")) {
          continue;
        }
        logger.debug("Processing urlAction: " + urlAction);

        // an actionUrl does not have a module of its own. Its module is derived
        // only from the screen that it belongs to. Get the screen and then the module.
        String screenId = (String) actionScreenMap.get(urlAction);
        if (screenId == null) {
          screenId = urlAction;
        }
        String module = screenId.equals("passthru") ? "mod_basic"
            : screenConfig.getScreen(screenId).getModule();

        if (module == null) {
          // assume not available
          urlRightsMap.put(urlAction, "N");

        } else if (!prefs.getModulesActivatedMap().containsKey(module)
            && !module.equals("mod_basic")) {
          // module not available
          urlRightsMap.put(urlAction, "N");

        } else if (privilegedActions.contains(urlAction)) {
          // available only for InstaAdmin role
          urlRightsMap.put(urlAction, (roleId == 1) ? "A" : "N");

        } else if (roleId <= 2) {
          // superuser: A
          urlRightsMap.put(urlAction, "A");

        } else {
          // normal user: get from the DB
          DynaBean bean = (DynaBean) urlRightsDbMap.get(urlAction);
          urlRightsMap.put(urlAction, bean == null ? "N" : bean.get("rights"));
        }

        if (reportActionsList.contains(urlAction) && !reportDashboardAdded) {
          urlRightsMap.put("report_job_status", "A");
          reportDashboardAdded = true;
        }
      }
    }

    // User Action rights must all be specified in the DB, absence is considered no access.
    if ((roleId != 1) && (roleId != 2)) {
      for (Iterator g = actionRights.iterator(); g.hasNext();) {
        Hashtable row = (Hashtable) g.next();
        actionRightsMap.put(row.get("ACTION"), row.get("RIGHTS"));
      }
    }

    for (Iterator g = screenConfig.getScreenGroupList().iterator(); g.hasNext();) {
      ScreenGroup grp = (ScreenGroup) g.next();
      // initialize group availability as "N"
      groupAvlblMap.put(grp.getId(), "N");
      for (Iterator s = grp.getScreenList().iterator(); s.hasNext();) {
        Screen scr = (Screen) s.next();
        // check if the module for the screen is activated.
        String module = scr.getModule();
        String moduleActive = "N";

        if (null != module
            && (module.equals("mod_basic") || prefs.getModulesActivatedMap().containsKey(module))) {
          moduleActive = "Y";
        }

        List screenActionList = null;
        if (screenActionMap != null) {
          screenActionList = (List) screenActionMap.get(scr.getId());
        }

        // if there is no entry for this screen in the screen-action map, treat screen id as action
        // id
        // else get the first action for the screen - all actions for a screen have the same rights.
        String refId = (null == screenActionList) ? scr.getId() : (String) screenActionList.get(0);
        String rights = roleId <= 2 ? "Y" : "N";

        // lookup the reference id in the urlrights map.
        rights = (String) urlRightsMap.get(refId);

        String scrRights = (null == rights || moduleActive.equals("N")) ? "N" : rights;

        if (!scrRights.equals("N")) {
          groupAvlblMap.put(grp.getId(), "Y");
          break;
        }
      }
    }
    MenuConfig menuConfig = (MenuConfig) session.getServletContext().getAttribute("menuConfig");
    for (Iterator g = menuConfig.getMenuGroups().values().iterator(); g.hasNext();) {
      com.insta.hms.common.MenuGroup grp = (com.insta.hms.common.MenuGroup) g.next();
      getMenuAvailability(grp.getId(), prefs.getModulesActivatedMap(), urlRightsMap, menuAvlblMap);
    }
    /*
     * Finally, set these maps in the session context, so it is available for all JSPs, especially
     * GroupMenu.jsp and Home.jsp
     */
    session.setAttribute("actionRightsMap", actionRightsMap);
    session.setAttribute("urlRightsMap", urlRightsMap);
    session.setAttribute("menuAvlblMap", menuAvlblMap);
    session.setAttribute("groupAvlblMap", groupAvlblMap);
    List<Map<String, Object>> menuItemsList = new ArrayList<>(); // List of menuurls accessible for
    // the current user; Used by react
    // app

    menuItemsList = getMenuItemsList(urlRightsMap, menuAvlblMap);
    session.setAttribute("menuItemsList", menuItemsList);
    List<String> reportsUrlList = new ArrayList<String>();
    List<String> settingsUrlList = new ArrayList<String>();
    List<String> menuUrls = getMenuUrls(menuItemsList, reportsUrlList, settingsUrlList);
    session.setAttribute("menuUrlsList", menuUrls);
    session.setAttribute("reportsUrlList", reportsUrlList);
    session.setAttribute("settingsUrlList", settingsUrlList);
  }

  /**
   * Gets the menu urls as list to be used by the confidentiality filter and sets reportUrlList and
   * settingsUrlList.
   *
   * @param menuItemsList   the menu items list
   * @param reportsUrlList  the reports url list
   * @param settingsUrlList the settings url list
   * @return the menu urls
   */
  private static List<String> getMenuUrls(List<Map<String, Object>> menuItemsList,
      List<String> reportsUrlList, List<String> settingsUrlList) {
    List<String> menuUrls = new ArrayList<>();
    List<Map<String, Object>> mainItemsList = menuItemsList;
    for (Map<String, Object> menuItem : mainItemsList) {
      List<Map<String, Object>> itemsList = (List<Map<String, Object>>) menuItem.get("itemsList");
      for (Map<String, Object> itemList : itemsList) {
        String linkUrl = null;
        if (itemList.get("linkUrl") == null) {
          List<Map<String, String>> subItemsList = (List<Map<String, String>>) itemList
              .get("subItemsList");
          if (menuItem.get("labelName").equals("REPORTS")) {
            for (Map<String, String> subItemList : subItemsList) {
              reportsUrlList.add(subItemList.get("linkUrl"));
            }
          } else if (menuItem.get("labelName").equals("Settings")) {
            for (Map<String, String> subItemList : subItemsList) {
              settingsUrlList.add(subItemList.get("linkUrl"));
            }
          }
        } else {
          linkUrl = (String) itemList.get("linkUrl");
          menuUrls.add(linkUrl);
        }
      }
    }
    return menuUrls;
  }

  /**
   * This method generates a list of all menu urls to be used by the react app and confidentiality
   * filter.
   *
   * @param urlRightsMap the url rights map
   * @param menuAvlblMap the menu avlbl map
   * @return List of map of urls in the format:
   * 
   *         <pre>
   * [
   *    {
   *       itemsList: [ {
   *         "linkUrl":"placeholder_rurl",
   *         "hash":"placeholder_hash",
   *         "query":"queryParam=queryValue",
   *         "labelName":"name_lable",
   *         "type":"type_of_item"
   *      },
   *      .
   *      .
   *      ]
   *      "labelName":"Name of Label"
   *    }
   *    .
   *    .
   * ]
   *         </pre>
   */
  @SuppressWarnings("rawtypes")
  private static List<Map<String, Object>> getMenuItemsList(Map<String, Object> urlRightsMap,
      Map<String, Object> menuAvlblMap) {
    WebApplicationContext context = (WebApplicationContext) ApplicationContextProvider
        .getApplicationContext();
    MenuConfig menuConfig = null;
    if (context != null) {
      ServletContext servletContext = context.getServletContext();
      Digester digester;
      try {
        digester = DigesterLoader
            .createDigester(servletContext.getResource("/WEB-INF/menu-digester.xml"));
        menuConfig = (MenuConfig) digester.parse(servletContext.getResource("/WEB-INF/menu.xml"));
      } catch (IOException | SAXException exception) {
        logger.error("Exception encountered while trying to build menuURL");
      }
    }
    List<Map<String, Object>> mainItemsList = new ArrayList<Map<String, Object>>();
    Map menuGroups = menuConfig.getMenuGroups();

    for (Object menuId : menuConfig.getTopMenu()) {
      MenuGroup menuGroup = (MenuGroup) menuGroups.get(menuId);
      if (menuGroup != null && "Y".equals(menuAvlblMap.get(menuGroup.getId()))) {
        List<MenuGroup> menuSubGroups = (List<MenuGroup>) menuGroup.getSubGroups();
        Map<String, Object> nav = new HashMap<>();
        nav.put("labelName", messageUtil.getMessage(menuGroup.getName()));
        List<Map<String, Object>> itemsList = new ArrayList<>();
        if ((Integer) menuGroup.getSubGroupCount() == 0) {
          for (MenuItem menuItem : (List<MenuItem>) menuGroup.getMenuItems()) {
            if ("A".equals(urlRightsMap.get(menuItem.getActionId()))) {
              itemsList.add(getItem(menuItem));
            }
          }
        } else if ((Integer) menuGroup.getSubGroupCount() > 0) {

          for (MenuGroup item : menuSubGroups) {
            MenuGroup menuSubGroup = (MenuGroup) menuGroups.get(item.getId());
            if (menuSubGroup != null && "Y".equals(menuAvlblMap.get(menuSubGroup.getId()))) {
              Map<String, Object> subNav = new HashMap<>();
              subNav.put("labelName", messageUtil.getMessage(menuSubGroup.getName()));
              List<Map<String, Object>> subItemsList = new ArrayList<>();
              for (MenuItem menuItem : (List<MenuItem>) menuSubGroup.getMenuItems()) {
                if ("A".equals(urlRightsMap.get(menuItem.getActionId()))) {
                  subItemsList.add(getItem(menuItem));
                }
              }
              subNav.put("subItemsList", subItemsList);
              itemsList.add(subNav);
            }
          }
        }
        nav.put("itemsList", itemsList);
        mainItemsList.add(nav);
      }
    }
    return mainItemsList;
  }

  /**
   * Gets the item.
   *
   * @param menuItem the menu item
   * @return the item
   */
  private static Map<String, Object> getItem(MenuItem menuItem) {
    String actionUrl = UrlUtil.buildURL(menuItem.getActionId(), null, menuItem.getUrlParams(),
        menuItem.getHashFragment(), null);
    Map<String, Object> item = new HashMap<String, Object>();
    item.put("labelName", messageUtil.getMessage(menuItem.getName()));
    item.put("linkUrl", actionUrl);
    item.put("hash", menuItem.getHashFragment());
    item.put("type", menuItem.getType());
    item.put("query", menuItem.getUrlParams());
    item.put("isNew", menuItem.getIsNew());
    item.put("isBeta", menuItem.getIsBeta());
    return item;
  }

  /**
   * This function traverses the menu structure recursively to set the parent node availability
   * based on child node availability. The parent node sets itself up, if any one menu item under it
   * is available or any menu subgroup under it is available.
   *
   * @param groupId          the group id
   * @param activeModulesMap the active modules map
   * @param urlRightsMap     the url rights map
   * @param menuAvlblMap     the menu avlbl map
   * @return the menu availability
   */
  public static boolean getMenuAvailability(String groupId, Map activeModulesMap, Map urlRightsMap,
      Map menuAvlblMap) {

    HttpServletRequest request = RequestContext.getHttpRequest();
    HttpSession session = request.getSession(false);

    MenuConfig mc = (MenuConfig) session.getServletContext().getAttribute("menuConfig");
    com.insta.hms.common.MenuGroup mg = (com.insta.hms.common.MenuGroup) mc.getMenuGroups()
        .get(groupId);

    // There are some superfluous cases like grp_transactions etc which do not have a
    // corresponding menu items.
    if (null == mg || mg.getId().isEmpty()) {
      return false;
    }

    if (mg.getId().equals("grp_billing")) {
      if (null != activeModulesMap.get("mod_ins_ext")
          && ((String) activeModulesMap.get("mod_ins_ext")).equals("Y")) {
        return false;
      }
    }

    menuAvlblMap.put(mg.getId(), "N");

    // Process all subgroups
    for (Iterator sg = mg.getSubGroups().iterator(); sg.hasNext();) {
      com.insta.hms.common.MenuGroup subGroup = (com.insta.hms.common.MenuGroup) sg.next();
      if (getMenuAvailability(subGroup.getId(), activeModulesMap, urlRightsMap, menuAvlblMap)) {
        menuAvlblMap.put(mg.getId(), "Y");
      }
    }

    // check if the default module is available. If specified and the module is not available,
    // disable the menu group.
    String defModule = mg.getDefaultModule();
    if (null != defModule && !defModule.isEmpty()) {
      String moduleActivated = (defModule.equals("mod_basic")) ? "Y"
          : (String) activeModulesMap.get(defModule);
      if (null == moduleActivated || "N".equals(moduleActivated)) {
        menuAvlblMap.put(mg.getId(), "N");
        return false;
      }
    }

    // At least one group is available - so return
    if ("Y".equals(menuAvlblMap.get(mg.getId()))) {
      return true;
    }

    // Check if at least one item is available.
    for (Iterator mi = mg.getMenuItems().iterator(); mi.hasNext();) {
      MenuItem menuItem = (MenuItem) mi.next();
      String actionId = menuItem.getActionId();

      if (null != actionId && "A".equals(urlRightsMap.get(actionId))) {
        // one item is avialable, so the parent is available
        menuAvlblMap.put(mg.getId(), "Y");
        break; // if we got one, we are done.
      }
    }

    return "Y".equals(menuAvlblMap.get(mg.getId()));
  }

  /**
   * Gets the url.
   *
   * @param screenId the screen id
   * @return the url
   */
  public static String getUrl(String screenId) {
    String url = null;
    HttpServletRequest request = RequestContext.getHttpRequest();
    HttpSession session = request.getSession(false);
    java.util.HashMap urlRightsMap = (java.util.HashMap) session.getAttribute("urlRightsMap");
    java.util.HashMap actionUrlMap = (java.util.HashMap) session.getServletContext()
        .getAttribute("actionUrlMap");

    if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get(screenId).equals("A")) {
      // we have the rights.
      url = (String) actionUrlMap.get(screenId); // lookup the url for the action.
    }
    return url;
  }

  /**
   * Gets the menu item name.
   *
   * @param actionId the action id
   * @return the menu item name
   */
  public static String getMenuItemName(String actionId) {
    MenuItem menuItem = getMenuItem(actionId);
    return null != menuItem ? menuItem.getName() : null;
  }

  /**
   * Gets the menu item.
   *
   * @param actionId the action id
   * @return the menu item
   */
  public static MenuItem getMenuItem(String actionId) {
    HttpServletRequest request = RequestContext.getHttpRequest();
    HttpSession session = request.getSession(false);
    MenuConfig mc = (MenuConfig) session.getServletContext().getAttribute("menuConfig");
    Map menuGroups = mc.getMenuGroups();
    Iterator mgIterator = menuGroups.entrySet().iterator();

    // search in all groups.
    while (mgIterator.hasNext()) {
      MenuGroup mg = (MenuGroup) ((Map.Entry) mgIterator.next()).getValue();
      List<MenuItem> listMenuItems = mg.getMenuItems();
      for (MenuItem menu : listMenuItems) {
        // menu action could be null for a separator item
        if (null != menu.getActionId() && menu.getActionId().equals(actionId)) {
          return menu;
        }
      }
    }
    return null;
  }

}
