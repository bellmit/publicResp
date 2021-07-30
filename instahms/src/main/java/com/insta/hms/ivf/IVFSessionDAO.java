package com.insta.hms.ivf;


import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class IVFSessionDAO.
 */
public class IVFSessionDAO extends GenericDAO {

  /**
   * Instantiates a new IVF session DAO.
   */
  public IVFSessionDAO() {
    super("ivf_cycle");
  }

  /** The ivf cycle query fields. */
  private static String IVF_CYCLE_QUERY_FIELDS = "SELECT * ";

  /** The ivf cycle query count. */
  private static String IVF_CYCLE_QUERY_COUNT = "select count(ivf_cycle_id) ";

  /** The ivf cycle query tables. */
  private static String IVF_CYCLE_QUERY_TABLES = "FROM "
      + " (SELECT ivf_cycle_id,ivf.mr_no,ivf.start_date,ivf.end_date, "
      + " ivf.patient_id, "
      + " get_patient_full_name(sm.salutation,pd.patient_name, "
      + " pd.middle_name,pd.last_name) as patient_name, "
      + " ivf.cycle_status "
      + " FROM ivf_cycle ivf "
      + " JOIN patient_details pd ON pd.mr_no = ivf.mr_no"
      + "   AND (pd.mr_no in (SELECT mr_no from user_mrno_association"
      + "   where emp_username = current_setting('application.username')"
      + "   OR current_setting('application.username') = '_system') or pd.patient_group in "
      + "   (SELECT ufa.confidentiality_grp_id as patient_group "
      + "     FROM user_confidentiality_association ufa JOIN confidentiality_grp_master cgm"
      + "     ON (cgm.confidentiality_grp_id = ufa.confidentiality_grp_id)"
      + "     where emp_username = current_setting('application.username')"
      + "     AND ufa.status = 'A' and cgm.status = 'A' UNION SELECT 0))"
      + " JOIN salutation_master sm on(sm.salutation_id=pd.salutation)) AS foo";
  
  /**
   * Gets the IVF session details.
   *
   * @param filter
   *          the filter
   * @param listing
   *          the listing
   * @return the IVF session details
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public static PagedList getIVFSessionDetails(Map filter, 
      Map listing) throws SQLException,ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();

    SearchQueryBuilder qb = new SearchQueryBuilder(con, IVF_CYCLE_QUERY_FIELDS,
        IVF_CYCLE_QUERY_COUNT, IVF_CYCLE_QUERY_TABLES, listing);

    qb.addFilterFromParamMap(filter);

    qb.build();

    PagedList ivfList = qb.getMappedPagedList();

    qb.close();
    con.close();

    return ivfList;
  }

  /** The ivf pre cycle details. */
  private static String IVF_PRE_CYCLE_DETAILS = "SELECT * "
      + " FROM ivf_cycle "
      + " WHERE ivf_cycle_id=?";

  /**
   * Gets the IVF pre cycle details.
   *
   * @param ivfCycleID
   *          the ivf cycle ID
   * @return the IVF pre cycle details
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public BasicDynaBean getIVFPreCycleDetails(int ivfCycleID) throws SQLException, ParseException {
    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean preCycleBean = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(IVF_PRE_CYCLE_DETAILS);
      ps.setInt(1, ivfCycleID);
      List preCycleList = DataBaseUtil.queryToDynaList(ps);
      if (preCycleList.size() > 0) {
        preCycleBean = (BasicDynaBean) preCycleList.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return preCycleBean;
  }

  /** The ivf daily treatment details. */
  private static String IVF_DAILY_TREATMENT_DETAILS = "SELECT dd.ivf_cycle_daily_id,"
      + " ivf_cycle_id, treatment_date,treatment_days_from_start,endometrium_thickness, "
      + " final_remarks,ovary_position, follicles_count,follicles_size,fsh_value, "
      + " p4_value,e2_value,lh_value,uterus_remarks, tsh_value,dp.prescription_id,"
      + " tc.item_id AS medicine_id,medicine_name,dd.doctor,doc.doctor_name "
      + " FROM ivf_daily_details dd "
      + " LEFT JOIN ivf_daily_follicles df on(df.ivf_cycle_daily_id = dd.ivf_cycle_daily_id) "
      + " LEFT JOIN ivf_daily_hormone_results dhr "
      + " on(dhr.ivf_cycle_daily_id = dd.ivf_cycle_daily_id) "
      + " LEFT JOIN ivf_daily_prescription dp "
      + " on(dp.ivf_cycle_daily_id = dd.ivf_cycle_daily_id) "
      + " LEFT JOIN treatment_chart tc ON (tc.prescription_id = dp.prescription_id) "
      + " LEFT JOIN store_item_details sitd on(sitd.medicine_id::text = tc.item_id) "
      + " LEFT JOIN doctors doc ON (doc.doctor_id = dd.doctor) "
      + " WHERE dd.ivf_cycle_daily_id=?";

  /**
   * Gets the daily treatment details.
   *
   * @param ivfCycleDailyID
   *          the ivf cycle daily ID
   * @return the daily treatment details
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public BasicDynaBean getDailyTreatmentDetails(int ivfCycleDailyID) throws SQLException,
      ParseException {
    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean bean = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(IVF_DAILY_TREATMENT_DETAILS);
      ps.setInt(1, ivfCycleDailyID);
      List dailyTrtList = DataBaseUtil.queryToDynaList(ps);
      if (dailyTrtList.size() > 0) {
        bean = (BasicDynaBean) dailyTrtList.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return bean;
  }

  /** The Constant IVF_DAILY_TREATMENT_LIST. */
  private static final String IVF_DAILY_TREATMENT_LIST = "select dd.ivf_cycle_id,"
      + " dd.ivf_cycle_daily_id,"
      + " treatment_date,treatment_days_from_start,endometrium_thickness, "
      + " final_remarks,uterus_remarks,fsh_value,p4_value,e2_value,"
      + " lh_value,tsh_value,dd.doctor,doc.doctor_name "
      + " from ivf_daily_details dd "
      + " left join ivf_daily_hormone_results dhr "
      + " on(dhr.ivf_cycle_daily_id = dd.ivf_cycle_daily_id) "
      + " left join doctors doc on(doc.doctor_id = dd.doctor) "
      + " where dd.ivf_cycle_id = ? order by treatment_days_from_start ";

  /**
   * Gets the daily treatment list.
   *
   * @param ivfCycleID
   *          the ivf cycle ID
   * @return the daily treatment list
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public List<BasicDynaBean> getDailyTreatmentList(int ivfCycleID) throws SQLException,
      ParseException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(IVF_DAILY_TREATMENT_LIST);
      ps.setInt(1, ivfCycleID);
      List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(ps);
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The ivf daily treatment prescription. */
  private static String IVF_DAILY_TREATMENT_PRESCRIPTION = "SELECT ivf_cycle_daily_id, tc.item_id, "
      + " medicine_name, idp.prescription_id "
      + " FROM ivf_daily_prescription idp "
      + " LEFT JOIN treatment_chart tc ON(tc.prescription_id = idp.prescription_id) "
      + " LEFT JOIN store_item_details sitd ON (tc.item_id = sitd.medicine_id::text) "
      + " WHERE ivf_cycle_daily_id=?";

  /**
   * Gets the daily treatment prescription.
   *
   * @param ivfCycleDailyID
   *          the ivf cycle daily ID
   * @return the daily treatment prescription
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public List<BasicDynaBean> getDailyTreatmentPrescription(int ivfCycleDailyID)
      throws SQLException, ParseException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(IVF_DAILY_TREATMENT_PRESCRIPTION);
      ps.setInt(1, ivfCycleDailyID);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant IVF_HISTORY_QUERY_FIELDS. */
  private static final String IVF_HISTORY_QUERY_FIELDS = "SELECT *";

  /** The Constant IVF_HISTORY_SUMMARY_COUNT. */
  private static final String IVF_HISTORY_SUMMARY_COUNT = "SELECT COUNT(ivf_cycle_id)";

  /** The Constant IVF_HISTORY_TABLES. */
  private static final String IVF_HISTORY_TABLES = "FROM "
      + " ( SELECT SUM(oocyte_number) AS tot_oocyte,ivf_cycle_id, mr_no, "
      + " start_date, protocol,gndtropin_dose, "
      + " fertilization_rate_number, patient_id,peak_e FROM  "
      + " (SELECT ic.ivf_cycle_id, ic.mr_no, start_date, "
      + " CASE WHEN protocol='I' THEN 'Ibg' "
      + " WHEN protocol='A' THEN 'antag' "
      + " WHEN protocol='M' THEN 'MF' "
      + " WHEN protocol='U' THEN 'UL' "
      + " WHEN protocol='S' THEN 'SBG' END AS protocol, "
      + " gndtropin_dose, fertilization_rate_number, "
      + " ic.patient_id, max(e2_value) as peak_e, oocyte_number "
      + " FROM ivf_cycle ic "
      + " LEFT JOIN patient_registration pr "
      + " on (pr.mr_no = ic.mr_no and pr.patient_id = ic.patient_id) "
      + " LEFT JOIN ivf_daily_details id on (id.ivf_cycle_id = ic.ivf_cycle_id) "
      + " LEFT JOIN ivf_daily_hormone_results ihr "
      + " on (ihr.ivf_cycle_daily_id = id.ivf_cycle_daily_id) "
      + " LEFT JOIN ivf_compl_oocyte_assess ico on (ico.ivf_cycle_id = ic.ivf_cycle_id) "
      + " GROUP BY ic.ivf_cycle_id, ic.mr_no, start_date, protocol,gndtropin_dose, "
      + " fertilization_rate_number, ic.patient_id, oocyte_number "
      + " ORDER BY ivf_cycle_id ) AS foo"
      + " GROUP BY ivf_cycle_id, mr_no, start_date, protocol,gndtropin_dose, "
      + " fertilization_rate_number, patient_id,peak_e ) AS foo1 ";

  /**
   * Gets the IVF history.
   *
   * @param filter
   *          the filter
   * @param listing
   *          the listing
   * @return the IVF history
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public static PagedList getIVFHistory(Map filter, Map listing) throws SQLException,
      ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();

    String sortField = (String) listing.get(LISTING.SORTCOL);
    boolean sortReverse = (Boolean) listing.get(LISTING.SORTASC);
    int pageSize = (Integer) listing.get(LISTING.PAGESIZE);
    int pageNum = (Integer) listing.get(LISTING.PAGENUM);

    SearchQueryBuilder qb = new SearchQueryBuilder(con, IVF_HISTORY_QUERY_FIELDS,
        IVF_HISTORY_SUMMARY_COUNT, IVF_HISTORY_TABLES, null, sortField, sortReverse, pageSize,
        pageNum);

    qb.addFilter(qb.STRING, "mr_no", "=", filter.get("mr_no"));

    qb.build();

    PagedList ivfHistList = qb.getMappedPagedList();

    qb.close();
    con.close();

    return ivfHistList;
  }

  /** The Constant GET_PRESCRIPTION_DETAILS. */
  private static final String GET_PRESCRIPTION_DETAILS = "select ivf_cycle_daily_id, "
      + " idp.prescription_id, mr_no,prescribed_date,type, "
      + " case when tc.type='O' or tc.type='I' or tc.type='N' then item_name "
      + " when tc.type='M' then sid.medicine_name end as item_name,"
      + " recurrence_daily_id,medicine_dosage,remarks,"
      + " freq_type,discontinued,tc.mod_time,tc.username,"
      + " ispackage,item_id,tc.route_of_admin,days,visit_id,"
      + " order_id,tc.item_form_id,tc.item_strength,sid.cons_uom_id,cum.consumption_uom,"
      + " g.generic_code,g.generic_name, "
      + " mr.route_id, mr.route_name,if.item_form_name, display_name, "
      + " case when tc.type='O' or tc.type='N' "
      + " then 'op' else 'item_master' end as master "
      + " from ivf_daily_prescription  idp "
      + " left join treatment_chart tc on (tc.prescription_id=idp.prescription_id) "
      + " left join store_item_details sid on (sid.medicine_id::text = tc.item_id) "
      + " LEFT JOIN generic_name g ON (sid.generic_name = g.generic_code) "
      + " LEFT JOIN medicine_route mr ON (tc.route_of_admin=mr.route_id) "
      + " LEFT JOIN item_form_master if ON (tc.item_form_id=if.item_form_id) "
      + " LEFT JOIN recurrence_daily_master rdm USING (recurrence_daily_id) "
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = sid.cons_uom_id)"
      + " where ivf_cycle_daily_id=? ";

  /**
   * Gets the prescription details.
   *
   * @param ivfCycleDailyID
   *          the ivf cycle daily ID
   * @return the prescription details
   * @throws SQLException
   *           the SQL exception
   */
  public static List getPrescriptionDetails(int ivfCycleDailyID) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PRESCRIPTION_DETAILS);
      ps.setInt(1, ivfCycleDailyID);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_PRESCRIPTION_LIST. */
  private static final String GET_PRESCRIPTION_LIST = "select dd.ivf_cycle_daily_id,"
      + " dp.prescription_id, case when tc.type='O' or tc.type='I' or tc.type='N' "
      + " then item_name when tc.type='M' then sid.medicine_name end as item_name,"
      + " medicine_dosage,display_name "
      + " from ivf_daily_details dd "
      + " left join ivf_daily_prescription dp on (dp.ivf_cycle_daily_id = dd.ivf_cycle_daily_id) "
      + " left join treatment_chart tc on (tc.prescription_id = dp.prescription_id) "
      + " left join store_item_details sid on (sid.medicine_id::text=tc.item_id) "
      + " left join recurrence_daily_master rdm using(recurrence_daily_id) "
      + " where ivf_cycle_id=? ";

  /**
   * Gets the prescription list.
   *
   * @param ivfCycleID
   *          the ivf cycle ID
   * @return the prescription list
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getPrescriptionList(int ivfCycleID) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PRESCRIPTION_LIST);
      ps.setInt(1, ivfCycleID);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_LUTEAL_PRESCRIPTION_DETAILS. */
  private static final String GET_LUTEAL_PRESCRIPTION_DETAILS = "select ivf_cycle_id, "
      + " ilp.prescription_id, mr_no,prescribed_date,type, case when tc.type='O' or "
      + " tc.type='I' or tc.type='N' then item_name "
      + " when tc.type='M' then sid.medicine_name end as item_name,"
      + " recurrence_daily_id,medicine_dosage,remarks,"
      + " freq_type,discontinued,tc.mod_time,tc.username,ispackage,item_id,"
      + " tc.route_of_admin,days,visit_id, order_id,tc.item_form_id,tc.item_strength,"
      + " sid.cons_uom_id, cum.consumption_uom,g.generic_code,g.generic_name, "
      + " mr.route_id, mr.route_name,if.item_form_name, display_name, "
      + " case when tc.type='O' or tc.type='N' then 'op' else 'item_master' end as master "
      + " from ivf_luteal_prescriptions  ilp "
      + " left join treatment_chart tc on (tc.prescription_id=ilp.prescription_id) "
      + " left join store_item_details sid on (sid.medicine_id::text = tc.item_id) "
      + " LEFT JOIN generic_name g ON (sid.generic_name = g.generic_code) "
      + " LEFT JOIN medicine_route mr ON (tc.route_of_admin=mr.route_id) "
      + " LEFT JOIN item_form_master if ON (tc.item_form_id=if.item_form_id) "
      + " LEFT JOIN recurrence_daily_master rdm USING (recurrence_daily_id) "
      + " LEFT JOIN consumption_uom_master cum ON (sid.cons_uom_id = cum.cons_uom_id)"
      + " where ivf_cycle_id=? ";

  /**
   * Gets the luteal prescription details.
   *
   * @param ivfCycleID
   *          the ivf cycle ID
   * @return the luteal prescription details
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getLutealPrescriptionDetails(int ivfCycleID) 
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_LUTEAL_PRESCRIPTION_DETAILS);
      ps.setInt(1, ivfCycleID);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant ALL_DOCTORS_LIST. */
  private static final String ALL_DOCTORS_LIST = " SELECT d.doctor_name as doctor_name, "
      + " d.doctor_id "
      + " FROM doctors d WHERE status='A'";

  /**
   * Gets the doctors list.
   *
   * @return the doctors list
   * @throws SQLException
   *           the SQL exception
   */
  public static List getDoctorsList() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(ALL_DOCTORS_LIST);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }

  /** The Constant GET_TOTAL_OOCYTES. */
  private static final String GET_TOTAL_OOCYTES = "select sum(oocyte_number) "
      + " from ivf_compl_oocyte_assess "
      + " where ivf_cycle_id=?";

  /**
   * Gets the total O ocytes.
   *
   * @param ivfCycleId
   *          the ivf cycle id
   * @return the total O ocytes
   * @throws SQLException
   *           the SQL exception
   */
  public static int getTotalOOcytes(int ivfCycleId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_TOTAL_OOCYTES);
      ps.setInt(1, ivfCycleId);
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_IVF_HISTORY_PRINT. */
  private static final String GET_IVF_HISTORY_PRINT = " SELECT SUM(oocyte_number) AS tot_oocyte,"
      + " ivf_cycle_id, mr_no, "
      + " start_date, protocol,gndtropin_dose, "
      + " fertilization_rate_number, patient_id,peak_e FROM  "
      + " (SELECT ic.ivf_cycle_id, ic.mr_no, start_date, "
      + " CASE WHEN protocol='I' THEN 'Ibg' "
      + " WHEN protocol='A' THEN 'antag' "
      + " WHEN protocol='M' THEN 'MF'  "
      + " WHEN protocol='U' THEN 'UL' "
      + " WHEN protocol='S' THEN 'SBG' END AS protocol, "
      + " gndtropin_dose, fertilization_rate_number, "
      + " ic.patient_id, max(e2_value) as peak_e, oocyte_number "
      + " FROM ivf_cycle ic  "
      + " LEFT JOIN patient_registration pr on (pr.mr_no = ic.mr_no "
      + " and pr.patient_id = ic.patient_id) "
      + " LEFT JOIN ivf_daily_details id on (id.ivf_cycle_id = ic.ivf_cycle_id) "
      + " LEFT JOIN ivf_daily_hormone_results ihr "
      + " on (ihr.ivf_cycle_daily_id = id.ivf_cycle_daily_id) "
      + " LEFT JOIN ivf_compl_oocyte_assess ico on (ico.ivf_cycle_id = ic.ivf_cycle_id) "
      + " WHERE ic.mr_no = ?  "
      + " GROUP BY ic.ivf_cycle_id, ic.mr_no, start_date, protocol,gndtropin_dose, "
      + " fertilization_rate_number, ic.patient_id, oocyte_number "
      + " ORDER BY ivf_cycle_id ) AS foo "
      + " GROUP BY ivf_cycle_id, mr_no, start_date, protocol,gndtropin_dose, "
      + " fertilization_rate_number, patient_id,peak_e ";

  /**
   * Gets the IVF history print.
   *
   * @param mrNo
   *          the mr no
   * @return the IVF history print
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getIVFHistoryPrint(String mrNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_IVF_HISTORY_PRINT);
      ps.setString(1, mrNo);
      List<BasicDynaBean> beans = DataBaseUtil.queryToDynaList(ps);
      if (beans.size() > 0) {
        return beans;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return null;
  }

}