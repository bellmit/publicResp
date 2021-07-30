package com.insta.hms.common;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

/**
 * The Class StdReportParams.
 */
public class StdReportParams implements Serializable {
         
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The log. */
  static Logger log = LoggerFactory.getLogger(StdReportParams.class);

  /** The type. */
  private String type; // list/sum/trend

  /** The list groups. */
  private ArrayList<String> listGroups; // grouping fields for list type

  /** The header fields. */
  private List<List<String>> headerFields; // additional fields to show in each group header

  /** The display fields. */
  private ArrayList<String> displayFields; // fields to display/aggregate

  /** The sum group vert. */
  private String sumGroupVert; // Vertical grouping for sum/trend types

  /** The sum group vert sub. */
  private String sumGroupVertSub; // Vertical sub-grouping for sum/trend types

  /** The sum group horiz. */
  private String sumGroupHoriz; // Horizontal grouping for sum type

  /** The trend type. */
  private String trendType; // month/week/day for trend type of report

  /** The filter where clause. */
  private String filterWhereClause; // Filter where-clause parsed from the UI.

  /** The filter inner where clause. */
  private List<String> filterInnerWhereClause; // Filter inner-where-clause parsed from the UI.

  /** The pre filter. */
  private String preFilter;

  /** The filter values. */
  private ArrayList filterValues; // Filter values to be set in the where clause.

  /** The pre filter values. */
  private ArrayList preFilterValues;

  /** The center name list. */
  private List<String> centerNameList;

  /** The date range. */
  private String dateRange; // Standard ranges: pd, td, pm, tm etc.

  /** The from date. */
  private java.sql.Date fromDate; // Date range converted to actual dates

  /** The to date. */
  private java.sql.Date toDate; //

  /** The base font size. */
  private int baseFontSize = 10; // base font size to use (0-25)

  /** The selected date field. */
  private String selectedDateField; // The main date field

  /** The filter description. */
  private String filterDescription = ""; // user readable description of filter

  /** The print title. */
  private String printTitle; // user-defined title, to be printed on the report

  /** The custom order 1. */
  private String customOrder1;

  /** The custom order 2. */
  private String customOrder2;

  /** The sort 1. */
  private String sort1;

  /** The sort 2. */
  private String sort2;

  /** The hosp name and addrs. */
  private String hospNameAndAddrs;

  /** The hosp name and addrs header. */
  private String hospNameAndAddrsHeader;

  /** The date reqd. */
  private Boolean dateReqd;

  /** The page nm reqd. */
  private Boolean pageNmReqd;

  /** The user name. */
  private String userName = "";

  /** The center name. */
  private String centerName = null;

  /** The group numbering. */
  private Boolean groupNumbering;

  /** The skip repeated values. */
  private boolean skipRepeatedValues;

  /** The null zero. */
  private boolean nullZero;// option to replace null values with 0;

  /** The home page redirect. */
  private boolean homePageRedirect = false;

  /** The param map. */
  private Map<String, String[]> paramMap;

  /**
   * Gets the type.
   *
   * @return the type
   */
  /*
   * Accessors
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the type.
   *
   * @param val the new type
   */
  public void setType(String val) {
    type = val;
  }

  /**
   * Gets the list groups.
   *
   * @return the list groups
   */
  public ArrayList<String> getListGroups() {
    return listGroups;
  }

  /**
   * Sets the list groups.
   *
   * @param val the new list groups
   */
  public void setListGroups(ArrayList<String> val) {
    listGroups = val;
  }

  /**
   * Gets the header fields.
   *
   * @return the header fields
   */
  public List<List<String>> getHeaderFields() {
    return headerFields;
  }

  /**
   * Sets the header fields.
   *
   * @param val the new header fields
   */
  public void setHeaderFields(List<List<String>> val) {
    headerFields = val;
  }

  /**
   * Gets the display fields.
   *
   * @return the display fields
   */
  public List<String> getDisplayFields() {
    return displayFields;
  }

  /**
   * Sets the display fields.
   *
   * @param val the new display fields
   */
  public void setDisplayFields(ArrayList<String> val) {
    displayFields = val;
  }

  /**
   * Gets the sum group vert.
   *
   * @return the sum group vert
   */
  public String getSumGroupVert() {
    return sumGroupVert;
  }

  /**
   * Sets the sum group vert.
   *
   * @param val the new sum group vert
   */
  public void setSumGroupVert(String val) {
    sumGroupVert = val;
  }

  /**
   * Gets the sum group vert sub.
   *
   * @return the sum group vert sub
   */
  public String getSumGroupVertSub() {
    return sumGroupVertSub;
  }

  /**
   * Sets the sum group vert sub.
   *
   * @param val the new sum group vert sub
   */
  public void setSumGroupVertSub(String val) {
    sumGroupVertSub = val;
  }

  /**
   * Gets the sum group horiz.
   *
   * @return the sum group horiz
   */
  public String getSumGroupHoriz() {
    return sumGroupHoriz;
  }

  /**
   * Sets the sum group horiz.
   *
   * @param val the new sum group horiz
   */
  public void setSumGroupHoriz(String val) {
    sumGroupHoriz = val;
  }

  /**
   * Gets the trend type.
   *
   * @return the trend type
   */
  public String getTrendType() {
    return trendType;
  }

  /**
   * Sets the trend type.
   *
   * @param val the new trend type
   */
  public void setTrendType(String val) {
    trendType = val;
  }

  /**
   * Gets the filter clause.
   *
   * @return the filter clause
   */
  public String getFilterClause() {
    return filterWhereClause;
  }

  /**
   * Sets the filter clause.
   *
   * @param val the new filter clause
   */
  public void setFilterClause(String val) {
    filterWhereClause = val;
  }
  
  /**
   * Gets the inner filter clause.
   *
   * @return the inner filter clause
   */
  public List<String> getInnerFilterClause() {
    return filterInnerWhereClause;
  }

  /**
   * Sets the inner filter clause.
   *
   * @param val the new inner filter clause
   */
  public void setInnerFilterClause(List<String> val) {
    filterInnerWhereClause.addAll(val);
  }

  /**
   * Gets the filter values.
   *
   * @return the filter values
   */
  public ArrayList getFilterValues() {
    return filterValues;
  }

  /**
   * Sets the filter values.
   *
   * @param val the new filter values
   */
  public void setFilterValues(ArrayList val) {
    filterValues = val;
  }

  /**
   * Gets the pre filter values.
   *
   * @return the pre filter values
   */
  public ArrayList getPreFilterValues() {
    return preFilterValues;
  }

  /**
   * Sets the pre filter values.
   *
   * @param val the new pre filter values
   */
  public void setPreFilterValues(ArrayList val) {
    preFilterValues = val;
  }

  /**
   * Gets the date range.
   *
   * @return the date range
   */
  public String getDateRange() {
    return dateRange;
  }

  /**
   * Sets the date range.
   *
   * @param val the new date range
   */
  public void setDateRange(String val) {
    dateRange = val;
  }

  /**
   * Gets the from date.
   *
   * @return the from date
   */
  public java.sql.Date getFromDate() {
    return fromDate;
  }

  /**
   * Sets the from date.
   *
   * @param val the new from date
   */
  public void setFromDate(java.sql.Date val) {
    fromDate = val;
  }

  /**
   * Gets the to date.
   *
   * @return the to date
   */
  public java.sql.Date getToDate() {
    return toDate;
  }

  /**
   * Sets the to date.
   *
   * @param val the new to date
   */
  public void setToDate(java.sql.Date val) {
    toDate = val;
  }

  /**
   * Gets the base font size.
   *
   * @return the base font size
   */
  public int getBaseFontSize() {
    return baseFontSize;
  }

  /**
   * Sets the base font size.
   *
   * @param val the new base font size
   */
  public void setBaseFontSize(int val) {
    baseFontSize = val;
  }

  /**
   * Gets the selected date field.
   *
   * @return the selected date field
   */
  public String getSelectedDateField() {
    return selectedDateField;
  }

  /**
   * Sets the selected date field.
   *
   * @param val the new selected date field
   */
  public void setSelectedDateField(String val) {
    selectedDateField = val;
  }

  /**
   * Gets the filter description.
   *
   * @return the filter description
   */
  public String getFilterDescription() {
    return filterDescription;
  }

  /**
   * Sets the filter description.
   *
   * @param val the new filter description
   */
  public void setFilterDescription(String val) {
    filterDescription = val;
  }

  /**
   * Gets the custom order 1.
   *
   * @return the custom order 1
   */
  public String getCustomOrder1() {
    return customOrder1;
  }

  /**
   * Sets the custom order 1.
   *
   * @param val the new custom order 1
   */
  public void setCustomOrder1(String val) {
    customOrder1 = val;
  }

  /**
   * Gets the custom order 2.
   *
   * @return the custom order 2
   */
  public String getCustomOrder2() {
    return customOrder2;
  }

  /**
   * Sets the custom order 2.
   *
   * @param val the new custom order 2
   */
  public void setCustomOrder2(String val) {
    customOrder2 = val;
  }

  /**
   * Gets the sort 1.
   *
   * @return the sort 1
   */
  public String getSort1() {
    return sort1;
  }

  /**
   * Sets the sort 1.
   *
   * @param val the new sort 1
   */
  public void setSort1(String val) {
    sort1 = val;
  }

  /**
   * Gets the sort 2.
   *
   * @return the sort 2
   */
  public String getSort2() {
    return sort2;
  }

  /**
   * Sets the sort 2.
   *
   * @param val the new sort 2
   */
  public void setSort2(String val) {
    sort2 = val;
  }

  /**
   * Gets the date reqd.
   *
   * @return the date reqd
   */
  public Boolean getDateReqd() {
    return dateReqd;
  }

  /**
   * Sets the date reqd.
   *
   * @param val the new date reqd
   */
  public void setDateReqd(Boolean val) {
    this.dateReqd = val;
  }

  /**
   * Gets the hosp name and addrs.
   *
   * @return the hosp name and addrs
   */
  public String getHospNameAndAddrs() {
    return hospNameAndAddrs;
  }

  /**
   * Sets the hosp name and addrs.
   *
   * @param hospNameAndAddrs the new hosp name and addrs
   */
  public void setHospNameAndAddrs(String hospNameAndAddrs) {
    this.hospNameAndAddrs = hospNameAndAddrs;
  }

  /**
   * Gets the hosp name and addrs header.
   *
   * @return the hosp name and addrs header
   */
  public String getHospNameAndAddrsHeader() {
    return hospNameAndAddrsHeader;
  }

  /**
   * Sets the hosp name and addrs header.
   *
   * @param hospNameAndAddrsHeader the new hosp name and addrs header
   */
  public void setHospNameAndAddrsHeader(String hospNameAndAddrsHeader) {
    this.hospNameAndAddrsHeader = hospNameAndAddrsHeader;
  }

  /**
   * Gets the page nm reqd.
   *
   * @return the page nm reqd
   */
  public Boolean getPageNmReqd() {
    return pageNmReqd;
  }

  /**
   * Sets the page nm reqd.
   *
   * @param pageNmReqd the new page nm reqd
   */
  public void setPageNmReqd(Boolean pageNmReqd) {
    this.pageNmReqd = pageNmReqd;
  }

  /**
   * Gets the user name.
   *
   * @return the user name
   */
  public String getUserName() {
    return userName;
  }

  /**
   * Sets the user name.
   *
   * @param val the new user name
   */
  public void setUserName(String val) {
    userName = val;
  }

  /**
   * Gets the prints the title.
   *
   * @return the prints the title
   */
  public String getPrintTitle() {
    return printTitle;
  }

  /**
   * Sets the prints the title.
   *
   * @param printTitle the new prints the title
   */
  public void setPrintTitle(String printTitle) {
    this.printTitle = printTitle;
  }

  /**
   * Gets the group numbering.
   *
   * @return the group numbering
   */
  public Boolean getGroupNumbering() {
    return groupNumbering;
  }

  /**
   * Sets the group numbering.
   *
   * @param groupNumbering the new group numbering
   */
  public void setGroupNumbering(Boolean groupNumbering) {
    this.groupNumbering = groupNumbering;
  }

  /**
   * Checks if is skip repeated values.
   *
   * @return true, if is skip repeated values
   */
  public boolean isSkipRepeatedValues() {
    return skipRepeatedValues;
  }

  /**
   * Sets the skip repeated values.
   *
   * @param val the new skip repeated values
   */
  public void setSkipRepeatedValues(boolean val) {
    skipRepeatedValues = val;
  }

  /**
   * Gets the pre filter.
   *
   * @return the pre filter
   */
  public String getPreFilter() {
    return preFilter;
  }

  /**
   * Sets the pre filter.
   *
   * @param val the new pre filter
   */
  public void setPreFilter(String val) {
    this.preFilter = val;
  }

  /**
   * Instantiates a new std report params.
   *
   * @param type the type
   */
  public StdReportParams(String type) {
    this.type = type;
    listGroups = new ArrayList<String>();
    headerFields = new ArrayList<List<String>>();
    displayFields = new ArrayList<String>();
    filterWhereClause = new String();
    filterInnerWhereClause = new ArrayList();
    filterValues = new ArrayList();
    preFilter = new String();

    if (type.equals("trend")) {
      sumGroupHoriz = "_period";
    }
    if (type.equals("vtrend")) {
      sumGroupVert = "_period";
    }
  }

  /**
   * Instantiates a new std report params.
   */
  public StdReportParams() {
  }

  /**
   * Instantiates a new std report params.
   *
   * @param paramMap the param map
   * @param desc     the desc
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public StdReportParams(Map<String, String[]> paramMap, StdReportDesc desc)
      throws java.sql.SQLException, java.text.ParseException {

    this.paramMap = paramMap;

    listGroups = new ArrayList<>();
    headerFields = new ArrayList<List<String>>();
    displayFields = new ArrayList<>();
    filterWhereClause = new String();
    filterInnerWhereClause = new ArrayList<>();
    preFilter = new String();
    filterValues = new ArrayList();

    type = getParameter("reportType", "list");
    if (type.equals("trend")) {
      sumGroupHoriz = "_period";
    }

    customOrder1 = getParameter("customOrder1");
    customOrder2 = getParameter("customOrder2");
    sort1 = getParameter("sort1");
    sort2 = getParameter("sort2");

    HttpSession session = RequestContext.getSession();
    if (session != null) {
      Integer reportingCenterId = null;
      // session will be null if called from email reports.
      if (getParameter("userNameNeeded", "Y").equals("Y")) {
        userName = (String) session.getAttribute("userid");
        reportingCenterId = (Integer) new GenericDAO("u_user")
            .findByKey("emp_username", userName).get("report_center_id");        
      }
      if (null == reportingCenterId) {
        if ((Integer) session.getAttribute("centerId") != 0) {
          centerNameList = new ArrayList<String>();
          centerName = (String) session.getAttribute("centerName");
          centerNameList.add(centerName);
        }
      } else {
        centerNameList = CenterMasterDAO.getCentersForGroup(reportingCenterId);
      }
    }

    dateReqd = Boolean.valueOf(getParameter("dt_needed", "true"));
    pageNmReqd = Boolean.valueOf(getParameter("pgn_needed", "true"));
    if (type.equals("list")) {
      skipRepeatedValues = Boolean.valueOf(getParameter("skip_repeated_values", "false"));
    } else {
      skipRepeatedValues = true;
    }
    BasicDynaBean bean = GenericPreferencesDAO.getAllPrefs();
    String nameAndAddrsString = "";
    String separatorString;
    if (bean.get("hospital_name") == null || bean.get("hospital_name").equals("")) {
      nameAndAddrsString += "";
      separatorString = "";
    } else {
      nameAndAddrsString += bean.get("hospital_name").toString().trim();
      separatorString = ", ";
    }
    if (!(bean.get("hospital_address") == null || bean.get("hospital_address").equals(""))) {
      nameAndAddrsString += separatorString + bean.get("hospital_address");
    }
    if (Boolean.valueOf(getParameter("hsp_needed", "false"))) {
      hospNameAndAddrs = nameAndAddrsString;
    }

    if (Boolean.valueOf(getParameter("hsp_needed_h", "false"))) {
      hospNameAndAddrsHeader = nameAndAddrsString;
    }

    groupNumbering = Boolean.valueOf(getParameter("grpn_needed", "false"));
    printTitle = getParameter("print_title", desc.getTitle());
    baseFontSize = Integer.parseInt(getParameter("baseFontSize", "10"));
    homePageRedirect = Boolean.valueOf(getParameter("home_page_redirect", "false"));

    selectedDateField = getParameter("dateFieldSelection");
    if (selectedDateField != null
        && (selectedDateField.equals("") || selectedDateField.equalsIgnoreCase("none"))) {
      selectedDateField = null;
    }

    dateRange = getParameter("selDateRange");

    if (dateRange.equalsIgnoreCase("cstm")) {
      fromDate = DateUtil.parseDate(getParameter("fromDate"));
      toDate = DateUtil.parseDate(getParameter("toDate"));
    } else {
      java.sql.Date[] dateArray = DateUtil.getDateRange(dateRange);
      fromDate = dateArray[0];
      toDate = dateArray[1];
    }

    if (type.equals("list")) {
      addDisplayFields(getParameterValues("listFields"));
      addListGroups(getParameterValues("listGroups"));
      moveDisplayFieldsToHeaders(getParameterValues("listHeaderNumFields"));

    } else if (type.equals("sum") || type.equals("trend") || type.equals("vtrend")) {
      addDisplayFields(getParameterValues("sumFields"));

      sumGroupVert = getParameter(type.equals("sum") ? "sumGroupVert" : "trendGroupVert");
      sumGroupVertSub = getParameter(type.equals("sum") ? "sumGroupVertSub"
          : type.equals("vtrend") ? "vtrendGroupVertSub" : "trendGroupVertSub");

      if (type.equals("vtrend")) {
        sumGroupVert = "_period";
      }

      if (type.equals("sum")) {
        sumGroupHoriz = getParameter("sumGroupHoriz");
        trendType = getParameter("trendType");
      } else if (type.equals("vtrend")) {
        sumGroupHoriz = getParameter("vtrendGroupHoriz");
        trendType = getParameter("vtrendType");
      } else {
        trendType = getParameter("trendType");
      }
    }

    // Parse and form the filter where-clause
    // passing list for center names (filter) to support user reporting center group
    ReportQueryBuilder rb = new ReportQueryBuilder();
    rb.addFilterFromParamMap(paramMap, desc.getFields());
    List<Map<String, String>> fieldsWithExpList = desc.getFieldsWithExpression();

    log.debug("User's center is: " + centerName);
    if (centerNameList != null && !centerNameList.isEmpty()
        && desc.getFields().get("center_name") != null) {
      log.debug("Applying center auto filter");
      rb.addFilter(rb.STRING, "center_name", "in", centerNameList);
      if (centerName != null) {
        rb.appendDescription(desc.getFields().get("center_name").getDisplayName(), "=", centerName);
      }
    }
    
    String selectDateFieldExp = null;
    
    List<String> innerwhereClauseList = new ArrayList<>();
    if ((desc.getFields().get(selectedDateField) != null)
        && (!desc.getSkipDateFilter())) {
      for (Map<String, String> fieldsWithExp : fieldsWithExpList) {
        StringBuilder innerWhereClause = new StringBuilder();
        selectDateFieldExp = (fieldsWithExp.containsKey(selectedDateField)) 
            ? fieldsWithExp.get(selectedDateField) : null;
        if (selectDateFieldExp != null) {
          if (fromDate != null) {
            innerWhereClause.append(" ((").append(selectDateFieldExp).append(")::date >= '")
              .append(fromDate).append("') ");
          }
          if (toDate != null) {
            if (innerWhereClause != null) {
              innerWhereClause.append(" AND ");
            }
            innerWhereClause.append(" ((").append(selectDateFieldExp).append(")::date <= '")
              .append(toDate).append("') ");
          }
        }
        innerwhereClauseList.add(innerWhereClause.toString());
      }
    }
    
    if ((desc.getFields().get(selectedDateField) != null)
        && (!desc.getSkipDateFilter())) {
      if (fromDate != null) {
        rb.addFilter(rb.DATE, selectedDateField + "::date", ">=", fromDate);
      }
      if (toDate != null) {
        rb.addFilter(rb.DATE, selectedDateField + "::date", "<=", toDate);
      }
    }
    filterInnerWhereClause.addAll(innerwhereClauseList);
    filterWhereClause = rb.getWhereClause();
    filterValues = rb.getfieldValues();

    ReportQueryBuilder rbPre = new ReportQueryBuilder();
    rbPre.addFilterFromParamMap(paramMap, desc.getFilterOnlyFields());
    if ((selectedDateField != null)
        && (desc.getFilterOnlyFields().get(selectedDateField) != null)) {
      if (fromDate != null) {
        rbPre.addFilter(rbPre.DATE, selectedDateField, ">=", fromDate);
      }
      if (toDate != null) {
        rbPre.addFilter(rbPre.DATE, selectedDateField, "<=", toDate);
      }
    }
    preFilter = rbPre.getWhereClause();
    preFilterValues = rbPre.getfieldValues();

    boolean filterDescReqd = Boolean.valueOf(getParameter("filterDesc_needed", "true"));
    if (filterDescReqd) {
      filterDescription = rb.getDescription();
      if (rbPre.getDescription().length() > 0) {
        if (filterDescription.length() > 0) {
          filterDescription = filterDescription + " AND " + rbPre.getDescription();
        } else {
          filterDescription = rbPre.getDescription();
        }
      }
    }
  }

  /**
   * Gets the parameter.
   *
   * @param param the param
   * @return the parameter
   */
  public String getParameter(String param) {
    if (this.paramMap == null) {
      return null;
    }
    String[] values = this.paramMap.get(param);
    if (values == null) {
      return null;
    }
    return values[0].equals("") ? null : values[0];
  }

  /**
   * Gets the parameter.
   *
   * @param param the param
   * @param def   the def
   * @return the parameter
   */
  public String getParameter(String param, String def) {
    String value = getParameter(param);
    return value == null ? def : value;
  }

  /**
   * Gets the parameter values.
   *
   * @param param the param
   * @return the parameter values
   */
  public String[] getParameterValues(String param) {
    if (this.paramMap == null) {
      return null;
    }
    return this.paramMap.get(param);
  }

  /**
   * Adds the display field.
   *
   * @param field the field
   */
  public void addDisplayField(String field) {
    if ((field == null) || field.equals("")) {
      return;
    }
    if (displayFields == null) {
      displayFields = new ArrayList();
    }
    displayFields.add(field);
  }

  /**
   * Adds the display fields.
   *
   * @param fields the fields
   */
  public void addDisplayFields(String[] fields) {
    if (fields == null) {
      return;
    }
    for (String field : fields) {
      addDisplayField(field);
    }
  }

  /**
   * Adds the list group.
   *
   * @param field the field
   */
  public void addListGroup(String field) {
    if ((field == null) || field.equals("")) {
      return;
    }

    if (listGroups == null) {
      listGroups = new ArrayList();
    }
    listGroups.add(field);

    // ensure each level has a list to hold the extra fields in that level
    if (headerFields == null) {
      headerFields = new ArrayList();
    }
    headerFields.add(new ArrayList<String>());
  }

  /**
   * Adds the list groups.
   *
   * @param fields the fields
   */
  public void addListGroups(String[] fields) {
    if (fields == null) {
      return;
    }
    for (String field : fields) {
      addListGroup(field);
    }
  }

  /**
   * Move display fields to headers.
   *
   * @param numFieldsToMove the num fields to move
   */
  public void moveDisplayFieldsToHeaders(String[] numFieldsToMove) {
    if (numFieldsToMove == null) {
      return;
    }

    // move N fields from display to the header at each group level
    for (int level = 0; level < numFieldsToMove.length; level++) {
      String numStr = numFieldsToMove[level];
      if (numStr == null || numStr.equals("")) {
        continue;
      }

      int num = Integer.parseInt(numStr);
      List levelHeaderFields = headerFields.get(level);

      for (int i = 0; i < num; i++) {
        String field = displayFields.remove(0);
        levelHeaderFields.add(field);
      }
    }
  }

  /**
   * Gets the filter fields. /* get fields that the user has chosen for filtering, including date
   * range, if any
   *
   * @return the filter fields
   */
  public List<String> getFilterFields() {
    ArrayList<String> fields = new ArrayList<String>();
    for (Map.Entry<String, String[]> e : paramMap.entrySet()) {
      String key = e.getKey();

      if (!key.contains("filter.")) { // look only for "filter." fields
        continue;
      }

      String fieldName = e.getValue()[0];
      if (fieldName == null || fieldName.equals("")) {
        continue;
      }

      fields.add(fieldName);
    }

    if (selectedDateField != null) {
      fields.add(selectedDateField);
    }

    // checking null condition on center_name_list instead centerName as we are passing list of
    // centers to filter
    if (null != centerNameList && !centerNameList.isEmpty()) {
      fields.add("center_name");
    }

    return fields;
  }

  /**
   * Gets the order fields.
   *
   * @return the order fields
   */
  public List<String> getOrderFields() {
    ArrayList<String> fields = new ArrayList<String>();
    if (customOrder1 != null) {
      fields.add(customOrder1);
    }
    if (customOrder2 != null) {
      fields.add(customOrder2);
    }
    return fields;
  }

  /**
   * Gets the sum group fields.
   *
   * @return the sum group fields
   */
  public List<String> getSumGroupFields() {
    ArrayList<String> fields = new ArrayList<String>();
    if (sumGroupVert != null) {
      fields.add(sumGroupVert);
    }
    if (sumGroupVertSub != null) {
      fields.add(sumGroupVertSub);
    }
    if (sumGroupHoriz != null) {
      fields.add(sumGroupHoriz);
    }
    return fields;
  }

  /**
   * Checks if is null zero.
   *
   * @return true, if is null zero
   */
  public boolean isNullZero() {
    return nullZero;
  }

  /**
   * Sets the null zero.
   *
   * @param nullZero the new null zero
   */
  public void setNullZero(boolean nullZero) {
    this.nullZero = nullZero;
  }

  /**
   * Valid date range for dynamic fields.
   *
   * @param paramMap the param map
   * @return the boolean
   * @throws ParseException the parse exception
   * @throws SQLException   the SQL exception
   */
  public Boolean validDateRangeForDynamicFields(Map<String, String[]> paramMap)
      throws ParseException, SQLException {

    this.paramMap = paramMap;

    String[] fields = getParameterValues("listFields");
    Boolean dyr = isDynamicFieldsReport(fields);
    if (!dyr) {
      return false;
    }

    dateRange = getParameter("selDateRange");

    if (dateRange.equalsIgnoreCase("cstm")) {
      fromDate = DateUtil.parseDate(getParameter("fromDate"));
      toDate = DateUtil.parseDate(getParameter("toDate"));
    } else {
      java.sql.Date[] dateArray = DateUtil.getDateRange(dateRange);
      fromDate = dateArray[0];
      toDate = dateArray[1];
    }
    List<String> dates = DateUtil.getDatesInRange(fromDate, toDate, "day");
    if (dates.size() > 10) {
      return true;
    }
    return false;
  }

  /**
   * Checks if is dynamic fields report.
   *
   * @param strings the strings
   * @return the boolean
   */
  private Boolean isDynamicFieldsReport(String[] strings) {
    Pattern pattern = Pattern
        .compile("bill_tax_.*|item_tax_.*|pri_bill_tax_.*|sec_bill_tax_.*|pat_bill_tax_.*");
    for (String s : strings) {
      if (pattern.matcher(s).matches()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if is home page redirect.
   *
   * @return true, if is home page redirect
   */
  public boolean isHomePageRedirect() {
    return homePageRedirect;
  }

  /**
   * Sets the home page redirect.
   *
   * @param homePageRedirect the new home page redirect
   */
  public void setHomePageRedirect(boolean homePageRedirect) {
    this.homePageRedirect = homePageRedirect;
  }
}