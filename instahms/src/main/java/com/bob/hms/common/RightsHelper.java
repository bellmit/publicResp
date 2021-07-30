package com.bob.hms.common;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;

import org.apache.commons.beanutils.DynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class RightsHelper.
 */
public class RightsHelper {

  static Logger logger = LoggerFactory.getLogger(RightsHelper.class);

  private static final String URL_RIGHTS = "SELECT action_id, rights"
      + " FROM url_action_rights WHERE role_id=?";

  /**
   * Gets the url rights map.
   *
   * @param roleId            the role id
   * @param privilegedActions the privileged actions
   * @param actionUrlMap      the action url map
   * @return the url rights map
   */
  public static HashMap getUrlRightsMap(int roleId, List privilegedActions, Map actionUrlMap) {
    /*
     * Every URL Action must have an entry in urlRightsMap. Set it based on the following priority:
     * 1. If screen is privileged, N unless role is 1 2. If superuser, (roleId <=2), then A 3.
     * Whatever is there in the DB
     */
    Map urlRightsDbMap = ConversionUtils.listBeanToMapBean(
        DatabaseHelper.queryToDynaList(URL_RIGHTS, new Object[] { roleId }), "action_id");
    HashMap<String, Object> urlRightsMap = new HashMap();
    if (actionUrlMap != null) {
      for (Iterator uai = actionUrlMap.keySet().iterator(); uai.hasNext();) {
        String urlAction = (String) uai.next();
        logger.debug("Processing urlAction: " + urlAction);

        if (privilegedActions.contains(urlAction)) {
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
      }
    }
    return urlRightsMap;
  }

}
