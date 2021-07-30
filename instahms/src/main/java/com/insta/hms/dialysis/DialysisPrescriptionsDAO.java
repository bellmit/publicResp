package com.insta.hms.dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * The Class DialysisPrescriptionsDAO.
 */
public class DialysisPrescriptionsDAO extends GenericDAO {

  /**
   * Instantiates a new dialysis prescriptions DAO.
   */
  public DialysisPrescriptionsDAO() {
    super("dialysis_prescriptions");
  }

  /** The pres query fields. */
  private static String PRES_QUERY_FIELDS = "SELECT * ";
  
  /** The pres query count. */
  private static String PRES_QUERY_COUNT = "select count(dialysis_presc_id) ";
  
  /** The pres query tables. */
  private static String PRES_QUERY_TABLES = "FROM  (select dialysis_presc_id, dp.mr_no,presc_date,"
      + "start_date,end_date,"
      + " target_weight,case when dp.status='A' then 'Active' WHEN dp.status='P' then 'Pending' "
      + "else 'Inactive' END as status,"
      + " dt.dialyzer_type_name,d.doctor_name,target_weight, pd.patient_name, d.doctor_id "
      + " FROM" + " dialysis_prescriptions dp "
      + " LEFT JOIN dialyzer_types dt using(dialyzer_type_id)"
      + " LEFT JOIN doctors d on d.doctor_id = dp.presc_doctor"
      + " JOIN patient_details pd ON (pd.mr_no = dp.mr_no AND ( patient_confidentiality_check"
      + " (pd.patient_group,pd.mr_no) ))) AS foo";

  /**
   * Gets the all MRNO prescriptions.
   *
   * @param filter the filter
   * @param listing the listing
   * @return the all MRNO prescriptions
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList getAllMRNOPrescriptions(Map filter, Map listing)
      throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();

    SearchQueryBuilder qb = new SearchQueryBuilder(con, PRES_QUERY_FIELDS, PRES_QUERY_COUNT,
        PRES_QUERY_TABLES, listing);

    qb.addFilterFromParamMap(filter);

    qb.build();

    PagedList list = qb.getMappedPagedList();

    qb.close();
    con.close();

    return list;
  }

  /** The mrno prescriptions. */
  private static String mrno_Prescriptions = "select * from dialysis_prescriptions where mr_no=? ";

  /**
   * Gets the prescription details.
   *
   * @param mrno the mrno
   * @param prescriptionId the prescription id
   * @return the prescription details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getPrescriptionDetails(String mrno, String prescriptionId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> allPrescription = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      StringBuilder query = new StringBuilder(mrno_Prescriptions);

      if (prescriptionId != null && !(prescriptionId.equals(""))) {
        SearchQueryBuilder.addWhereFieldOpValue(true, query, "dialysis_presc_id", "=",
            prescriptionId);
      }

      ps = con.prepareStatement(query.toString());
      ps.setString(1, mrno);

      if (prescriptionId != null && !(prescriptionId.equals(""))) {
        ps.setInt(2, Integer.parseInt(prescriptionId));
      }

      allPrescription = DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return allPrescription;
  }

  /** The check for duplicate status. */
  private static final String CHECK_FOR_DUPLICATE_STATUS = "select dialysis_presc_id "
      + " from dialysis_prescriptions where mr_no = ? and status=?";

  /**
   * Check for duplicate status.
   *
   * @param con the con
   * @param mrno the mrno
   * @param status the status
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean checkForDuplicateStatus(Connection con, String mrno, String status)
      throws SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    boolean exists = false;
    try {
      ps = con.prepareStatement(CHECK_FOR_DUPLICATE_STATUS);
      ps.setString(1, mrno);
      ps.setString(2, status);
      rs = ps.executeQuery();
      if (rs.next()) {
        exists = true;
      }
    } finally {
      rs.close();
      ps.close();
    }

    return exists;
  }

  /** The Constant GET_TEMPORARY_ACCESSES. */
  private static final String GET_TEMPORARY_ACCESSES = "SELECT *,dt.access_type, ds.access_site,"
      + "  d.doctor_name FROM temporary_access_types tat "
      + "JOIN dialysis_access_types dt ON(dt.access_type_id = tat.access_type_id_t) "
      + "JOIN dialysis_access_sites ds ON(tat.access_site_t = ds.access_site_id) "
      + "LEFT JOIN doctors d ON(tat.doctor_name_t = d.doctor_id) "
      + "WHERE tat.mr_no = ? AND tat.dialysis_presc_id = ? order by temporary_access_type_id";

  /**
   * Gets the temporary accesses.
   *
   * @param mrNo the mr no
   * @param prescriptionId the prescription id
   * @return the temporary accesses
   * @throws SQLException the SQL exception
   */
  public static List<Map> getTemporaryAccesses(String mrNo, int prescriptionId)
      throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_TEMPORARY_ACCESSES);
      pstmt.setString(1, mrNo);
      pstmt.setInt(2, prescriptionId);
      List<Map> list = ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(pstmt));
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }

  }

  /** The Constant GET_PERMANENT_ACCESSES. */
  private static final String GET_PERMANENT_ACCESSES = "SELECT *,dt.access_type, ds.access_site,"
      + " d.doctor_name FROM permanent_access_types pat "
      + "JOIN dialysis_access_types dt ON(dt.access_type_id = pat.access_type_id_p) "
      + "JOIN dialysis_access_sites ds ON(pat.access_site_p = ds.access_site_id) "
      + "LEFT JOIN doctors d ON(pat.doctor_name_p = d.doctor_id) "
      + "WHERE pat.mr_no = ? AND pat.dialysis_presc_id = ? order by permanent_access_type_id";

  /**
   * Gets the permanent accesses.
   *
   * @param mrNo the mr no
   * @param prescriptionId the prescription id
   * @return the permanent accesses
   * @throws SQLException the SQL exception
   */
  public static List<Map> getPermanentAccesses(String mrNo, int prescriptionId)
      throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_PERMANENT_ACCESSES);
      pstmt.setString(1, mrNo);
      pstmt.setInt(2, prescriptionId);
      List<Map> list = ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(pstmt));
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }

  }

}
