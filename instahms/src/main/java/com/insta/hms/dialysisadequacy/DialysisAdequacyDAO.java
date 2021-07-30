package com.insta.hms.dialysisadequacy;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class DialysisAdequacyDAO.
 */
public class DialysisAdequacyDAO extends GenericDAO {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(DialysisAdequacyDAO.class);

  /**
   * Instantiates a new dialysis adequacy DAO.
   */
  public DialysisAdequacyDAO() {
    super("clinical_dial_adeq_values");
  }

  /** The dialysis adequacy fields. */
  private static String DIALYSIS_ADEQUACY_FIELDS = " SELECT *  ";

  /** The dialysis adequacy count. */
  private static String DIALYSIS_ADEQUACY_COUNT = " SELECT count(*) ";

  /** The dialysis adequacy tables. */
  private static String DIALYSIS_ADEQUACY_TABLES = "FROM (SELECT distinct(cdav.mr_no),"
      + "cdav.mod_time,cdav.user_name,"
      + " get_patient_full_name(sm.salutation, pd.patient_name,"
      + " pd.middle_name, pd.last_name) as patient_name"
      + " FROM clinical_dial_adeq_values cdav " + " JOIN patient_details pd"
      + " ON(cdav.mr_no=pd.mr_no)"
      + " LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)"
      + " ) as foo ";

  /**
   * Gets the adequacy.
   *
   * @param map the map
   * @param pagingParams the paging params
   * @return the adequacy
   * @throws Exception the exception
   * @throws ParseException the parse exception
   */
  public PagedList getAdequacy(Map map, Map pagingParams) throws Exception, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, DIALYSIS_ADEQUACY_FIELDS,
          DIALYSIS_ADEQUACY_COUNT, DIALYSIS_ADEQUACY_TABLES, pagingParams);

      qb.addFilterFromParamMap(map);
      qb.addSecondarySort("mod_time", true);
      qb.build();

      PagedList list = qb.getMappedPagedList();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The dialysis adquacy details. */
  private static String DIALYSIS_ADQUACY_DETAILS = " select dp.mr_no,dp.dialysis_presc_id,"
      + " ds.fin_ktv,ds.fin_urr,ds.start_time::date::text as observation_date "
      + " from dialysis_prescriptions dp "
      + " left join dialysis_session ds on(ds.prescription_id=dp.dialysis_presc_id) "
      + " WHERE dp.mr_no = ? order by  observation_date desc";

  /**
   * Gets the adequacy bean.
   *
   * @param mrNo the mr no
   * @return the adequacy bean
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getAdequacyBean(String mrNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(DIALYSIS_ADQUACY_DETAILS);
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The dialysis adquecy details by adequacy date. */
  private static String DIALYSIS_ADQUECY_DETAILS_BY_ADEQUACY_DATE = " select dp.mr_no,"
      + " dp.dialysis_presc_id,ds.fin_ktv,"
      + " ds.fin_urr,ds.start_time::date::text as observation_date "
      + " from dialysis_prescriptions dp "
      + " left join dialysis_session ds on(ds.prescription_id=dp.dialysis_presc_id) "
      + " WHERE dp.mr_no = ? AND ds.start_time= ? order by  ds.start_time desc";

  /**
   * Gets the adequacy bean.
   *
   * @param mrNo the mr no
   * @param adequacyDate the adequacy date
   * @return the adequacy bean
   * @throws Exception the exception
   */
  public BasicDynaBean getAdequacyBean(String mrNo, String adequacyDate) throws Exception {
    Connection con = null;
    java.util.Date date = DateUtil.parseDate(adequacyDate);
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(DIALYSIS_ADQUECY_DETAILS_BY_ADEQUACY_DATE);
      ps.setString(1, mrNo);
      ps.setDate(2, new java.sql.Date(date.getTime()));
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The clinical dialysis session dates fields. */
  private static String CLINICAL_DIALYSIS_SESSION_DATES_FIELDS = " SELECT *  ";

  /** The clinical dialysis session dates count. */
  private static String CLINICAL_DIALYSIS_SESSION_DATES_COUNT = " SELECT count(*) ";

  /** The clinical dialysis session tables. */
  private static String CLINICAL_DIALYSIS_SESSION_TABLES = " FROM "
      + " (SELECT  cdav.values_as_of_date, cdav.urr, cdav.ktv, cdav.mr_no "
      + " FROM clinical_dial_adeq_values cdav "
      + " JOIN patient_details pd ON (pd.mr_no = cdav.mr_no AND "
      + " patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no))"
      + " WHERE cdav.ktv is not null OR cdav.urr is not null"
      + " ORDER BY values_as_of_date desc) AS foo ";

  /**
   * Gets the clinical dialysis dates.
   *
   * @param map the map
   * @param pagingParams the paging params
   * @param mrno the mrno
   * @return the clinical dialysis dates
   * @throws Exception the exception
   * @throws ParseException the parse exception
   */
  public PagedList getClinicalDialysisDates(Map map, Map pagingParams, String mrno)
      throws Exception, ParseException {
    Connection con = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, CLINICAL_DIALYSIS_SESSION_DATES_FIELDS,
          CLINICAL_DIALYSIS_SESSION_DATES_COUNT, CLINICAL_DIALYSIS_SESSION_TABLES, pagingParams);
      qb.addFilter(SearchQueryBuilder.STRING, "mr_no", "=", mrno);
      qb.build();

      PagedList list = qb.getMappedPagedList();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the calculated ktvand urr.
   *
   * @param con the con
   * @param map the map
   * @param mrNo the mr no
   * @param username the username
   * @param labrecordId the labrecord id
   * @return the calculated ktvand urr
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Map<String, Map<String, Object>> getCalculatedKtvandUrr(Connection con, Map map,
      String mrNo, String username, Integer labrecordId)
      throws SQLException, ParseException, IOException {

    Map<String, Map<String, Object>> valuesMap = new HashMap<String, Map<String, Object>>();
    java.sql.Date date = null;
    BigDecimal realWeight = null;
    int orderId = -1;

    if (map != null) {
      if (map.get("start_time") != null && !map.get("start_time").equals("")) {
        date = new java.sql.Date(((java.sql.Timestamp) map.get("start_time")).getTime());
      }

      if (map.get("fin_real_wt") != null && !map.get("fin_real_wt").equals("")) {
        realWeight = (BigDecimal) map.get("fin_real_wt");
      }

      if (map.get("order_id") != null && !map.get("order_id").equals("")) {
        orderId = (Integer) map.get("order_id");
      }
    }

    if (labrecordId == null) {
      labrecordId = getLabRecordId(mrNo, date, con);
    }
    if (labrecordId != 0) {
      Map valMap = returnValues(labrecordId, realWeight, orderId, con);
      valuesMap.put(date.toString(), valMap);
    }

    return valuesMap;
  }

  /** The Constant GET_LAB_RECORDED_ID. */
  public static final String GET_LAB_RECORDED_ID = "SELECT clinical_lab_recorded_id FROM"
      + " clinical_lab_recorded clr"
      + " WHERE clr.mrno = ? AND values_as_of_date = ?::date ORDER BY"
      + "  clinical_lab_recorded_id DESC LIMIT 1";

  /**
   * Gets the lab record id.
   *
   * @param mrNo the mr no
   * @param date the date
   * @param con the con
   * @return the lab record id
   * @throws SQLException the SQL exception
   */
  private Integer getLabRecordId(String mrNo, java.sql.Date date, Connection con)
      throws SQLException {

    PreparedStatement pstmt = null;
    pstmt = con.prepareStatement(GET_LAB_RECORDED_ID);
    pstmt.setString(1, mrNo);
    pstmt.setDate(2, date);
    return DataBaseUtil.getIntValueFromDb(pstmt);

  }

  /** The Constant GET_RESULT_VALUES. */
  public static final String GET_RESULT_VALUES = "SELECT trm.resultlabel, clv.test_value FROM"
      + " clinical_lab_values clv" + " JOIN test_results_master trm USING (resultlabel_id)"
      + " WHERE clv.clinical_lab_recorded_id = ?";

  /** The possible values for prebun. */
  private List<String> possibleValuesForPrebun = Arrays.asList("PRE BUN", "PRE-BUN", "pre bun",
      "pre-bun");
  
  /** The possible values for postbun. */
  private List<String> possibleValuesForPostbun = Arrays.asList("POST BUN", "POST-BUN", "post bun",
      "post-bun");

  /**
   * Return values.
   *
   * @param labRecordId the lab record id
   * @param weight the weight
   * @param orderId the order id
   * @param con the con
   * @return the map
   * @throws SQLException the SQL exception
   */
  public Map<String, Object> returnValues(int labRecordId, BigDecimal weight, int orderId,
      Connection con) throws SQLException {
    PreparedStatement pstmt = null;
    BigDecimal preBun = null;
    BigDecimal postBun = null;
    BigDecimal urr = null;
    Map<String, Object> valueMap = new HashMap<String, Object>();
    valueMap.put("URR", null);
    valueMap.put("KTV", null);

    try {
      pstmt = con.prepareStatement(GET_RESULT_VALUES);
      pstmt.setInt(1, labRecordId);
      List list = DataBaseUtil.queryToArrayList(pstmt);

      Iterator it = list.iterator();
      while (it.hasNext()) {
        Hashtable table = (java.util.Hashtable) it.next();

        String key = (String) table.get("RESULTLABEL");
        if (key.equalsIgnoreCase("pre bun") || possibleValuesForPrebun.contains(key)) {
          if (table.get("TEST_VALUE") != null && !table.get("TEST_VALUE").equals("")) {
            try {
              preBun = new BigDecimal(((String) table.get("TEST_VALUE")).trim());
            } catch (NumberFormatException ex) {
              preBun = null;
            }
          }
        } else if (key.equalsIgnoreCase("post bun") || possibleValuesForPostbun.contains(key)) {
          if (table.get("TEST_VALUE") != null && !table.get("TEST_VALUE").equals("")) {
            String postBunStr = ((String) table.get("TEST_VALUE")).trim();
            try {
              postBun = new BigDecimal(((String) table.get("TEST_VALUE")).trim());
            } catch (NumberFormatException ex) {
              postBun = null;
            }
          }
        }

        if (preBun != null && postBun != null) {
          break;
        }
      }

      if (preBun != null && postBun != null) {
        try {
          urr = ((preBun.subtract(postBun)).multiply(new BigDecimal(100))).divide(preBun, 2,
              BigDecimal.ROUND_HALF_UP);
          valueMap.put("URR", urr);
        } catch (ArithmeticException ex) {
          valueMap.put("URR", null);
        }
      }

      BigDecimal ufr = null;
      if (orderId != -1) {
        ufr = getUltrafiltrateVolume(con, orderId);
      }
      if (weight == null || weight.equals("") || ufr == null || preBun == null || postBun == null) {
        return valueMap;
      }
      try {
        BigDecimal ratio = postBun.divide(preBun, 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal logInput = ratio.subtract(new BigDecimal(0.03));
        BigDecimal logValue = new BigDecimal(Math.log(logInput.doubleValue()));
        BigDecimal minusOne = new BigDecimal(-1);
        BigDecimal threedotFive = new BigDecimal(3.5);
        BigDecimal four = new BigDecimal(4);
        BigDecimal firstPart = minusOne.multiply(logValue);
        BigDecimal ratioval = threedotFive.multiply(ratio);
        BigDecimal secondPart = four.subtract(ratioval);
        BigDecimal thirdPart = ufr.divide(weight, 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal secondandthirdValue = secondPart.multiply(thirdPart);
        BigDecimal ktv = firstPart.add(secondandthirdValue);

        valueMap.put("KTV", ktv.setScale(2, BigDecimal.ROUND_HALF_UP));
      } catch (Exception ex) {
        valueMap.put("KTV", null);
      }
      return valueMap;

    } finally {
      logger.info("reached finally");
    }
  }

  /** The Constant GET_UFR. */
  private static final String GET_UFR = "SELECT uf_rate" + " FROM dialysis_session_parameters dp "
      + " WHERE dp.order_id = ? AND obs_type = 'L' LIMIT 1";

  /**
   * Gets the ultrafiltrate volume.
   *
   * @param con the con
   * @param orderId the order id
   * @return the ultrafiltrate volume
   * @throws SQLException the SQL exception
   */
  private BigDecimal getUltrafiltrateVolume(Connection con, int orderId) throws SQLException {
    PreparedStatement pstmt = null;
    BigDecimal ufr = null;
    pstmt = con.prepareStatement(GET_UFR);
    pstmt.setInt(1, orderId);
    ResultSet rs = pstmt.executeQuery();
    if (rs != null && rs.next()) {
      ufr = rs.getBigDecimal(1);
    } else {
      BasicDynaBean bean = new GenericDAO("dialysis_session_parameters").findByKey(con, "order_id",
          orderId);// we have to use same connection
      if (bean != null && bean.get("uf_rate") != null && !bean.get("uf_rate").equals("")) {
        ufr = (BigDecimal) bean.get("uf_rate");
      }
    }
    return ufr;

  }

  /** The Constant FIND_ADEQUACY_BEAN. */
  private static final String FIND_ADEQUACY_BEAN = "SELECT * FROM clinical_dial_adeq_values "
      + " WHERE mr_no = ? AND values_as_of_date = ?";

  /**
   * Checks if is adequacy bean exists.
   *
   * @param mrNo the mr no
   * @param date the date
   * @return the basic dyna bean
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   */
  private BasicDynaBean isAdequacyBeanExists(String mrNo, java.sql.Date date)
      throws ParseException, SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(FIND_ADEQUACY_BEAN);
      pstmt.setString(1, mrNo);
      pstmt.setDate(2, date);

      return DataBaseUtil.queryToDynaBean(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }

  }

  /**
   * Save ktv and urr values.
   *
   * @param con the con
   * @param valuesMap the values map
   * @param mrNo the mr no
   * @param userName the user name
   * @return true, if successful
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean saveKtvAndUrrValues(Connection con, Map<String, Map<String, Object>> valuesMap,
      String mrNo, String userName) throws ParseException, SQLException, IOException {

    Iterator<String> keyIterator = valuesMap.keySet().iterator();
    DialysisAdequacyDAO dao = new DialysisAdequacyDAO();
    BasicDynaBean adequacyBean = dao.getBean();
    Map<String, Object> keys = new HashMap<String, Object>();
    SimpleDateFormat simpleDateformat = new SimpleDateFormat("yyyy-MM-dd");

    boolean success = true;

    while (keyIterator.hasNext()) {
      String date = keyIterator.next();
      java.sql.Date parsedDate = new java.sql.Date(simpleDateformat.parse(date).getTime());
      BasicDynaBean bean = isAdequacyBeanExists(mrNo, parsedDate);
      Map<String, Object> computedValuesMap = valuesMap.get(date);

      if (bean == null
          && (computedValuesMap.get("URR") != null || computedValuesMap.get("KTV") != null)) {
        adequacyBean.set("mod_time", DataBaseUtil.getDateandTime());
        adequacyBean.set("mr_no", mrNo);
        adequacyBean.set("values_as_of_date", parsedDate);
        adequacyBean.set("urr", computedValuesMap.get("URR"));
        adequacyBean.set("ktv", computedValuesMap.get("KTV"));
        adequacyBean.set("user_name", userName);

        success = dao.insert(con, adequacyBean);
        if (!success) {
          break;
        }

      } else {
        if (bean != null && ((bean.get("urr") == null ? computedValuesMap.get("URR") != null
            : !bean.get("urr").equals(computedValuesMap.get("URR")))
            || (bean.get("ktv") == null ? computedValuesMap.get("KTV") != null
                : !bean.get("ktv").equals(computedValuesMap.get("KTV"))))) {
          keys.put("values_as_of_date", bean.get("values_as_of_date"));
          keys.put("mr_no", bean.get("mr_no"));

          bean.set("mod_time", DataBaseUtil.getDateandTime());
          bean.set("urr", computedValuesMap.get("URR"));
          bean.set("ktv", computedValuesMap.get("KTV"));
          bean.set("user_name", userName);

          success = dao.update(con, bean.getMap(), keys) > 0;
          if (!success) {
            break;
          }
        }
      }
    }

    return success;
  }

}
