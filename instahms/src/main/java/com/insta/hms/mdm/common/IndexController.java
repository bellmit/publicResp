package com.insta.hms.mdm.common;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseController;
import com.insta.hms.common.MenuConfig;
import com.insta.hms.common.MenuGroup;
import com.insta.hms.common.MenuItem;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.UrlUtil;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.exception.AccessDeniedException;
import com.insta.hms.master.URLRoute;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

/**
 * The Class IndexController used to load the JSP Page with React code. Generic for each master.
 *
 * @author tanmay.k
 */
@Controller
@RequestMapping(URLRoute.MASTER_INDEX_URL)
public class IndexController extends BaseController {

  @LazyAutowired
  private SecurityService securityService;

  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private MessageUtil messageUtil;

  private MenuConfig menuConfig;

  /**
   * Instantiates a new index controller.
   *
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SAXException
   *           the SAX exception
   */
  public IndexController() throws IOException, SAXException {
    super();
    WebApplicationContext context =
        (WebApplicationContext) ApplicationContextProvider.getApplicationContext();
    // Context is null when running testng.
    if (context != null) {
      ServletContext servletContext = context.getServletContext();
      Digester digester =
          DigesterLoader.createDigester(servletContext.getResource("/WEB-INF/menu-digester.xml"));
      this.menuConfig =
          (MenuConfig) digester.parse(servletContext.getResource("/WEB-INF/menu.xml"));
    }

  }

  /**
   * Gets the index page.
   *
   * @return the index page
   */
  @IgnoreConfidentialFilters
  @GetMapping("/master")
  public ModelAndView getMasterIndexPage() {
    return renderMasterUi("Master", "bundle", "frame");
  }

  /**
   * Gets the application settings page.
   *
   * @return the application settings page
   */
  @IgnoreConfidentialFilters
  @GetMapping("/applicationsettings")
  public ModelAndView getApplicationSettingsIndexPage() {
    Map<String, Object> sessionAttributes = (Map<String, Object>) sessionService
        .getSessionAttributes(new String[] { "menuAvlblMap" });
    Map<String, Object> menuAvlblMap = (Map<String, Object>) sessionAttributes.get("menuAvlblMap");
    Boolean hasSettingsAccess = menuAvlblMap.containsKey("grp_settings")
        && ((String) menuAvlblMap.get("grp_settings")).equals("Y");
    if (!hasSettingsAccess) {
      throw new AccessDeniedException("exception.access.denied");
    }
    return renderMasterUi("Settings", "applicationSettings");
  }

  /**
   * Gets the generic preferences page.
   *
   * @return the generic preferences page
   */
  @IgnoreConfidentialFilters
  @GetMapping("genericpreferences")
  public ModelAndView getGenericPreferencesIndexPage() {
    String title = messageUtil.getMessage("patient.genericpreference.addshow.title");
    return renderMasterUi(title, "preference", "frame");
  }

  /**
   * Gets the integrations index page.
   *
   * @return the integrations index page
   */
  @IgnoreConfidentialFilters
  @GetMapping("/integrations")
  public ModelAndView getIntegrationsIndexPage() {
    return renderMasterUi("Practo Book", "bookApp", "frame");
  }

  /**
   * Gets the settings.
   *
   * @return the settings
   */
  @IgnoreConfidentialFilters
  @SuppressWarnings({ "unchecked" })
  @GetMapping(value = "/settings")
  public Map<String, Object> getSettings() {
    Map<String, Object> sessionAttributes =
        (Map<String, Object>)
            sessionService.getSessionAttributes(new String[] {"menuAvlblMap", "urlRightsMap"});
    Map<String, Object> urlRightsMap = (Map<String, Object>) sessionAttributes.get("urlRightsMap");
    Map<String, Object> menuAvlblMap = (Map<String, Object>) sessionAttributes.get("menuAvlblMap");
    Map menuGroups = this.menuConfig.getMenuGroups();

    List<Map<String, Object>> mainItemsList = new ArrayList<>();

    for (Object menuId : this.menuConfig.getTopMenu()) {
      MenuGroup menuGroup = (MenuGroup) menuGroups.get(menuId);
      if (menuGroup != null && "Y".equals(menuAvlblMap.get(menuGroup.getId()))) {
        List<MenuGroup> menuSubGroups = (List<MenuGroup>) menuGroup.getSubGroups();
        Map<String, Object> nav = new HashMap<>();
        nav.put("labelName", messageUtil.getMessage(menuGroup.getName()));
        nav.put("id", menuGroup.getId());
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

    Map<String, Object> responseMap = new HashMap<String, Object>();
    responseMap.put("mainItemsList", mainItemsList);
    return responseMap;
  }
  
  private Map<String, Object> getItem(MenuItem menuItem) {
    String actionUrl =
        UrlUtil.buildURL(
            menuItem.getActionId(),
            null,
            menuItem.getUrlParams(),
            menuItem.getHashFragment(),
            null);
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
}
