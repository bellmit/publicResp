package com.insta.hms.extension.billing;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.BusinessController;

import org.springframework.web.servlet.ModelAndView;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class AccountingBaseController.
 */
public class AccountingBaseController extends BusinessController {

  /** The Constant REFERENCE_DATA_SECTION_KEYS. */
  private static final String[] REFERENCE_DATA_SECTION_KEYS = new String[] { "voucher_types",
      "account_groups" };

  /** The Constant DATA_SECTION_TEMPLATE. */
  private static final Map<String, String[]> DATA_SECTION_TEMPLATE = 
      new HashMap<String, String[]>();

  /** The Constant SEARCH_RESULT_KEYS. */
  private static final String[] SEARCH_RESULT_KEYS = new String[] { "summary", "result" };

  /** The Constant EXPORT_RESULT_KEYS. */
  private static final String[] EXPORT_RESULT_KEYS = new String[] { "message" };

  static {
    DATA_SECTION_TEMPLATE.put("search", SEARCH_RESULT_KEYS);
    DATA_SECTION_TEMPLATE.put("export", EXPORT_RESULT_KEYS);
  }

  /**
   * The Class AccountingModelAndView.
   */
  public class AccountingModelAndView extends ModelAndView {

    /** The action. */
    private String action = null;

    /**
     * Gets the action.
     *
     * @return the action
     */
    public String getAction() {
      return this.action;
    }

    /**
     * Instantiates a new accounting model and view.
     *
     * @param action
     *          the action
     * @param viewName
     *          the view name
     */
    public AccountingModelAndView(String action, String viewName) {
      super(viewName);
      this.action = action;
    }

    /**
     * Sets the data.
     *
     * @param action
     *          the action
     * @param data
     *          the data
     */
    public void setData(String action, Object... data) {
      if (null != data) {
        for (int i = 0; i < data.length; i++) {
          String[] dataSectionKeys = getDataSectionKeys(action);
          addObject(dataSectionKeys[i], data[i]);
        }
      }
    }

    /**
     * Gets the data section keys.
     *
     * @param action
     *          the action
     * @return the data section keys
     */
    private String[] getDataSectionKeys(String action) {
      return DATA_SECTION_TEMPLATE.get(action);
    }

    /**
     * Sets the reference data.
     *
     * @param action
     *          the action
     * @param referenceData
     *          the reference data
     */
    public void setReferenceData(String action, List<Object> referenceData) {
      Map<String, Object> refMap = new HashMap<String, Object>();
      if (null != referenceData) {
        for (int i = 0; i < referenceData.size(); i++) {
          refMap.put(REFERENCE_DATA_SECTION_KEYS[i], referenceData.get(i));
        }
        addObject("referenceData", refMap);
      }
    }
  }

  /**
   * Preprocess params.
   *
   * @param reqMap
   *          the req map
   * @return the map
   */
  protected Map<String, String[]> preprocessParams(Map<String, String[]> reqMap) {

    Map<String, String[]> map = new LinkedHashMap<String, String[]>();
    map.putAll(reqMap);
    String[] accounts = map.get("_account_id");

    if (null != accounts && accounts.length > 0 && null != accounts[0] && !accounts[0].isEmpty()) {
      if (accounts[0].startsWith("A")) {
        map.put("d_account_group", new String[] { accounts[0].substring(1).trim() });
      }
      if (accounts[0].startsWith("C")) {
        map.put("center_id", new String[] { accounts[0].substring(1).trim() });
        // to ensure that pharmacy bills belonging to a different account group does not end up
        // along with
        // other vouchers for the same center.
        map.put("d_account_group", new String[] { "1" });
      }
    }

    String[] vdates = map.get("voucher_date");
    // Note : It is very funny how the servlet gives us a read-only parameter map (only with respect
    // to keys)
    // but allows us to override the values in the map by reference. So if we do not clone the array
    // here, the original
    // parameter value is overwritten and shows up as such in the UI when the page is reloaded with
    // results.
    // Not sure, if this is a bug in apache or spring... but cloning the array so that integrity of
    // the request parameter map is
    // maintained.
    String[] dates = null;
    if (null != vdates) {
      dates = vdates.clone();
    }
    // TODO : This qualifies to go into DateUtil as getNextZeroHours()
    if (null != dates && dates.length > 1 && !dates[1].isEmpty()) { // to date is specified
      java.sql.Timestamp toDate;
      try {
        toDate = new DateUtil().parseTheTimestamp(dates[1]);
      } catch (ParseException ex) {
        toDate = new Timestamp(new Date().getTime());
      }
      java.sql.Timestamp nextDay = DateUtil.addDays(toDate, 1);
      // Assumption here is that the incoming parameter for toDate does not carry any time value,
      // but only date value
      dates[1] = DateUtil.formatTimestamp(nextDay);
      map.put("voucher_date", dates);
    }

    return map;
  }

  /**
   * Gets the view.
   *
   * @param action
   *          the action
   * @return the view
   */
  protected AccountingModelAndView getView(String action) {
    AccountingModelAndView view = null;
    if ("search".equalsIgnoreCase(action)) {
      view = new AccountingModelAndView(action, "/pages/integration/accounting/journal");
    }
    if ("export".equalsIgnoreCase(action)) {
      view = new AccountingModelAndView(action, "redirect:/billing/accounting/search.htm");
    }
    return view;
  }

  // TODO : Ideally this should go into a ZOHO specific class since it uses BooksException class

  /**
   * Creates the view.
   *
   * @param action
   *          the action
   * @param data
   *          the data
   * @param referenceData
   *          the reference data
   * @return the accounting model and view
   */
  protected AccountingModelAndView createView(String action, Object[] data,
      List<Object> referenceData) {
    AccountingModelAndView view = getView(action);
    view.setData(action, data);
    view.setReferenceData(action, referenceData);
    return view;
  }

}
