package com.insta.hms.hospitalizationinformation;

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


// TODO: Auto-generated Javadoc
/**
 * The Class HospitalizationInformationDAO.
 *
 * @author mithun.saha
 */

public class HospitalizationInformationDAO extends GenericDAO {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(HospitalizationInformationDAO.class);

  /**
   * Instantiates a new hospitalization information DAO.
   */
  public HospitalizationInformationDAO() {
    super("clinical_hospitalization");
  }

  /** The clinical hospitalization information fields. */
  private static String CLINICAL_HOSPITALIZATION_INFORMATION_FIELDS = " SELECT *  ";

  /** The clinical hospitalization information count. */
  private static String CLINICAL_HOSPITALIZATION_INFORMATION_COUNT = " SELECT count(*) ";

  /** The clinical hospitalization information tables. */
  private static String CLINICAL_HOSPITALIZATION_INFORMATION_TABLES =
      "FROM (SELECT ch.*,"
          + " get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name)"
          + " as patient_name,pr.center_id,"
          + " (SELECT count(hospitalization_id ) from clinical_hospitalization_details chd "
          + "  WHERE ch.hospitalization_id = chd.hospitalization_id) AS hospitalization_count "
          + " FROM clinical_hospitalization ch"
          + " JOIN patient_details pd ON(ch.mr_no=pd.mr_no "
          + " AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )) "
          + " LEFT JOIN patient_registration pr ON"
          + " (pr.patient_id = coalesce(pd.visit_id,pd.previous_visit_id))"
          + " LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)" + " )"
          + " as foo ";

  /**
   * Gets the hospitalization informations.
   *
   * @param map the map
   * @param pagingParams the paging params
   * @param centerId the center id
   * @return the hospitalization informations
   * @throws Exception the exception
   * @throws ParseException the parse exception
   */
  public PagedList getHospitalizationInformations(Map map, Map pagingParams, int centerId)
      throws Exception, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb =
          new SearchQueryBuilder(con, CLINICAL_HOSPITALIZATION_INFORMATION_FIELDS,
              CLINICAL_HOSPITALIZATION_INFORMATION_COUNT,
              CLINICAL_HOSPITALIZATION_INFORMATION_TABLES, pagingParams);

      qb.addFilterFromParamMap(map);
      if (centerId == 0
          && (Integer) (GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")) > 1) {
        // Do Nothing
      } else {
        qb.addFilter(qb.INTEGER, "center_id", "=", centerId);
      }
      qb.addSecondarySort("mod_time", true);
      qb.build();

      PagedList list = qb.getMappedPagedList();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The clinical hospitalization information details. */
  private static String CLINICAL_HOSPITALIZATION_INFORMATION_DETAILS =
      " SELECT ch.*,chd.*,chr.*  "
          + " FROM clinical_hospitalization ch "
          + " JOIN clinical_hospitalization_details chd ON"
          + " (ch.hospitalization_id=chd.hospitalization_id)"
          + " JOIN clinical_hospitalization_reasons chr ON(chr.reason_id = chd.reason_id)"
          + " WHERE mr_no = ? AND ch.hospitalization_id = ? order by admission_date";

  /**
   * Gets the hospitalization bean.
   *
   * @param mrNo the mr no
   * @param hospId the hosp id
   * @return the hospitalization bean
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getHospitalizationBean(String mrNo, int hospId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(CLINICAL_HOSPITALIZATION_INFORMATION_DETAILS);
      ps.setString(1, mrNo);
      ps.setInt(2, hospId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The clinical hospitalization information. */
  private static String CLINICAL_HOSPITALIZATION_INFORMATION =
      " SELECT ch.*,chd.*,chr.*  "
          + " FROM clinical_hospitalization ch "
          + " JOIN clinical_hospitalization_details chd ON"
          + " (ch.hospitalization_id=chd.hospitalization_id)"
          + " JOIN clinical_hospitalization_reasons chr ON(chr.reason_id = chd.reason_id)"
          + " WHERE mr_no = ? order by admission_date";


  /**
   * Gets the hospitalization bean.
   *
   * @param mrNo the mr no
   * @return the hospitalization bean
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getHospitalizationBean(String mrNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(CLINICAL_HOSPITALIZATION_INFORMATION);
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
