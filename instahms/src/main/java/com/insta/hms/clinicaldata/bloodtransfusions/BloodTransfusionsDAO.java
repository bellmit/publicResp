package com.insta.hms.clinicaldata.bloodtransfusions;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * The Class BloodTransfusionsDAO.
 */
public class BloodTransfusionsDAO extends GenericDAO {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(BloodTransfusionsDAO.class);

  /**
   * Instantiates a new blood transfusions DAO.
   */
  public BloodTransfusionsDAO() {
    super("clinical_blood_transfusions");
  }

  /** The clinical blood transfusions fields. */
  private static String CLINICAL_BLOOD_TRANSFUSIONS_FIELDS = " SELECT *  ";

  /** The clinical blood transfusions count. */
  private static String CLINICAL_BLOOD_TRANSFUSIONS_COUNT = " SELECT count(*) ";

  /** The clinical blood transfusions tables. */
  private static String CLINICAL_BLOOD_TRANSFUSIONS_TABLES = "FROM (SELECT cbt.*,"
      + " get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) "
      + " as patient_name,center_id,"
      + " (SELECT count(transfusion_id) from clinical_transfusion_details ctd "
      + "  WHERE cbt.transfusion_id = ctd.transfusion_id) AS transfusion_count "
      + " FROM clinical_blood_transfusions cbt" + " JOIN patient_details pd ON(cbt.mr_no=pd.mr_no "
      + " AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) ))"
      + " LEFT JOIN patient_registration pr "
      + " ON(pr.patient_id = coalesce(pd.visit_id,pd.previous_visit_id))"
      + " LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation) " + " ) as foo ";

  /**
   * Gets the blood transfusions.
   *
   * @param map
   *          the map
   * @param pagingParams
   *          the paging params
   * @param centerId
   *          the center id
   * @return the blood transfusions
   * @throws Exception
   *           the exception
   * @throws ParseException
   *           the parse exception
   */
  public PagedList getBloodTransfusions(Map map, Map pagingParams, int centerId)
      throws Exception, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, CLINICAL_BLOOD_TRANSFUSIONS_FIELDS,
          CLINICAL_BLOOD_TRANSFUSIONS_COUNT, CLINICAL_BLOOD_TRANSFUSIONS_TABLES, pagingParams);

      qb.addFilterFromParamMap(map);
      if (!(centerId == 0
          && (Integer) (GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")) > 1)) {
        qb.addFilter(qb.INTEGER, "center_id", "=", centerId);
      }
      qb.addSecondarySort("mod_time", true);
      qb.build();

      return qb.getMappedPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The clinical blood transfusions details. */
  private static String CLINICAL_BLOOD_TRANSFUSIONS_DETAILS = " SELECT cbt.*,ctd.* "
      + " FROM clinical_blood_transfusions cbt "
      + " JOIN clinical_transfusion_details ctd ON(cbt.transfusion_id=ctd.transfusion_id)"
      + " WHERE mr_no = ? AND cbt.transfusion_id = ? order by data_as_of_date";

  /**
   * Gets the blood transfusion bean.
   *
   * @param mrNo
   *          the mr no
   * @param tranfusionId
   *          the tranfusion id
   * @return the blood transfusion bean
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getBloodTransfusionBean(String mrNo, int tranfusionId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(CLINICAL_BLOOD_TRANSFUSIONS_DETAILS);
      ps.setString(1, mrNo);
      ps.setInt(2, tranfusionId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The clinical blood transfusions. */
  private static String CLINICAL_BLOOD_TRANSFUSIONS = "SELECT cbt.*,ctd.* "
      + " FROM clinical_blood_transfusions cbt "
      + " JOIN clinical_transfusion_details ctd ON(cbt.transfusion_id=ctd.transfusion_id)"
      + " WHERE mr_no = ? order by data_as_of_date";

  /**
   * Gets the blood transfusion bean.
   *
   * @param mrNo
   *          the mr no
   * @return the blood transfusion bean
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getBloodTransfusionBean(String mrNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(CLINICAL_BLOOD_TRANSFUSIONS);
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
