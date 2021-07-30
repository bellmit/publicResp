package com.insta.hms.vaccinations;

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
 * The Class VaccinationsDAO.
 *
 * @author mithun.saha
 */

public class VaccinationsDAO extends GenericDAO {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(VaccinationsDAO.class);

  /**
   * Instantiates a new vaccinations DAO.
   */
  public VaccinationsDAO() {
    super("clinical_vaccination");
  }

  /** The vaccinations fields. */
  private static String VACCINATIONS_FIELDS = " SELECT *  ";

  /** The vaccinations count. */
  private static String VACCINATIONS_COUNT = " SELECT count(*) ";

  /** The vaccinations tables. */
  private static String VACCINATIONS_TABLES = "FROM (SELECT cv.*,"
      + " get_patient_full_name(sm.salutation, pd.patient_name, "
      + " pd.middle_name, pd.last_name) as patient_name,center_id,"
      + " (SELECT count(vaccination_id ) from clinical_vaccinations_details cvd "
      + "  WHERE cv.vaccination_id = cvd.vaccination_id) AS vaccination_count "
      + " FROM clinical_vaccination cv"
      + " JOIN patient_details pd ON(cv.mr_no=pd.mr_no "
      + " AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )) "
      + " LEFT JOIN patient_registration pr "
      + " ON(pr.patient_id = coalesce(pd.visit_id,pd.previous_visit_id))"
      + " LEFT JOIN salutation_master sm "
      + " ON (sm.salutation_id = pd.salutation) " + " ) as foo ";

  /**
   * Gets the vaccinations.
   *
   * @param map the map
   * @param pagingParams the paging params
   * @param centerId the center id
   * @return the vaccinations
   * @throws Exception the exception
   * @throws ParseException the parse exception
   */
  public PagedList getVaccinations(Map map, Map pagingParams, int centerId) throws Exception,
      ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, VACCINATIONS_FIELDS, VACCINATIONS_COUNT,
          VACCINATIONS_TABLES, pagingParams);

      qb.addFilterFromParamMap(map);
      
      if (centerId != 0
          && (Integer) (GenericPreferencesDAO
              .getAllPrefs().get("max_centers_inc_default")) > 1) {
        qb.addFilter(qb.INTEGER, "center_id", "=", centerId);
      }
      qb.addSecondarySort("mod_time", true);
      qb.build();

      PagedList vaccList = qb.getMappedPagedList();
      return vaccList;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The clinical vaccination details. */
  private static String CLINICAL_VACCINATION_DETAILS = " SELECT cv.*,cvd.*,cvnr.*,cvm.*  "
      + " FROM clinical_vaccination cv "
      + " JOIN clinical_vaccinations_details cvd ON(cv.vaccination_id=cvd.vaccination_id)"
      + " LEFT JOIN clinical_vacc_no_reason cvnr ON(cvnr.reason_id = cvd.no_reason_id) "
      + " LEFT JOIN clinical_vaccinations_master cvm "
      + " ON(cvm.vaccination_type_id=cvd.vaccination_type_id)"
      + " WHERE mr_no = ? AND cv.vaccination_id = ? order by vaccination_date";

  /**
   * Gets the vaccination bean.
   *
   * @param mrNo the mr no
   * @param vaccinId the vaccin id
   * @return the vaccination bean
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getVaccinationBean(String mrNo, int vaccinId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(CLINICAL_VACCINATION_DETAILS);
      ps.setString(1, mrNo);
      ps.setInt(2, vaccinId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The clinical vaccination. */
  private static String CLINICAL_VACCINATION = " SELECT cv.*,cvd.*,cvnr.*,cvm.*  "
      + " FROM clinical_vaccination cv "
      + " JOIN clinical_vaccinations_details cvd ON(cv.vaccination_id=cvd.vaccination_id)"
      + " LEFT JOIN clinical_vacc_no_reason cvnr ON(cvnr.reason_id = cvd.no_reason_id) "
      + " LEFT JOIN clinical_vaccinations_master cvm "
      + " ON(cvm.vaccination_type_id=cvd.vaccination_type_id)"
      + " WHERE mr_no = ? order by vaccination_date";

  /**
   * Gets the vaccination bean.
   *
   * @param mrNo the mr no
   * @return the vaccination bean
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getVaccinationBean(String mrNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(CLINICAL_VACCINATION);
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
