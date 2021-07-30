package com.insta.hms.infectiondata;

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
 * The Class InfectionInformationDAO.
 */
public class InfectionInformationDAO extends GenericDAO {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(InfectionInformationDAO.class);

  /**
   * Instantiates a new infection information DAO.
   */
  public InfectionInformationDAO() {
    super("clinical_infections_recorded");
  }

  /** The infections fields. */
  private static String INFECTIONS_FIELDS = " SELECT *  ";

  /** The inefections count. */
  private static String INEFECTIONS_COUNT = " SELECT count(*) ";

  /** The infections tables. */
  private static String INFECTIONS_TABLES =
      "FROM (SELECT cir.*,"
          + " get_patient_full_name(sm.salutation, pd.patient_name,"
          + " pd.middle_name, pd.last_name) as patient_name,center_id"
          + " FROM clinical_infections_recorded cir "
          + " JOIN patient_details pd ON(cir.mr_no=pd.mr_no AND"
          + " ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )) "
          + " LEFT JOIN patient_registration pr ON"
          + " (pr.patient_id = coalesce(pd.visit_id,pd.previous_visit_id))"
          + " LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation) " + " )"
          + " as foo ";

  /**
   * Gets the infections.
   *
   * @param map the map
   * @param pagingParams the paging params
   * @param centerId the center id
   * @return the infections
   * @throws Exception the exception
   * @throws ParseException the parse exception
   */
  public PagedList getInfections(Map map, Map pagingParams, int centerId) throws Exception,
      ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb =
          new SearchQueryBuilder(con, INFECTIONS_FIELDS, INEFECTIONS_COUNT, INFECTIONS_TABLES,
              pagingParams);

      qb.addFilterFromParamMap(map);
      if (centerId == 0
          && (Integer) (GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")) > 1) {
        // nothing to do
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

  /** The clinical infection details. */
  private static String CLINICAL_INFECTION_DETAILS =
      " SELECT cir.*,ci.*,cim.*,cism.* "
          + " FROM clinical_infections_recorded cir "
          + " JOIN clinical_infections ci ON"
          + " (cir.clinical_infections_recorded_id=ci.clinical_infections_recorded_id) "
          + " LEFT JOIN clinical_infection_site_master cism ON"
          + " (ci.infection_site_id = cism.infection_site_id)"
          + " LEFT JOIN clinical_infections_master cim ON"
          + " (cim.infection_type_id = ci.infection_type_id) "
          + " WHERE mr_no = ? AND cir.clinical_infections_recorded_id = ?"
          + " order by infection_effective_date ";

  /**
   * Gets the infection bean.
   *
   * @param mrNo the mr no
   * @param infectionRecId the infection rec id
   * @return the infection bean
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getInfectionBean(String mrNo, int infectionRecId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(CLINICAL_INFECTION_DETAILS);
      ps.setString(1, mrNo);
      ps.setInt(2, infectionRecId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The clinical infections. */
  private static String CLINICAL_INFECTIONS =
      " SELECT cir.*,ci.*,cim.*,cism.* "
          + " FROM clinical_infections_recorded cir "
          + " JOIN clinical_infections ci ON"
          + " (cir.clinical_infections_recorded_id=ci.clinical_infections_recorded_id)"
          + " LEFT JOIN clinical_infections_master cim ON"
          + " (cim.infection_type_id = ci.infection_type_id) "
          + " LEFT JOIN clinical_infection_site_master cism ON "
          + " (ci.infection_site_id=cism.infection_site_id)"
          + " WHERE mr_no = ? order by infection_effective_date ";


  /**
   * Gets the infection bean.
   *
   * @param mrNo the mr no
   * @return the infection bean
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getInfectionBean(String mrNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(CLINICAL_INFECTIONS);
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The clinical antibiotics details. */
  private static String CLINICAL_ANTIBIOTICS_DETAILS = " SELECT cial.* "
      + " FROM clinical_infection_antibiotic_log cial "
      + " WHERE infection_id = ? order by antibiotic_log_id ";


  /**
   * Gets the antibiotic deatils.
   *
   * @param infectionId the infection id
   * @return the antibiotic deatils
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getAntibioticDeatils(String infectionId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(CLINICAL_ANTIBIOTICS_DETAILS);
      ps.setInt(1, Integer.parseInt(infectionId));
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The clinical antibiotics with medicine. */
  private static String CLINICAL_ANTIBIOTICS_WITH_MEDICINE =
      " SELECT cial.*,sid.medicine_name,sid.medicine_id "
          + " FROM clinical_infection_antibiotic_log cial "
          + " LEFT JOIN store_item_details sid ON(sid.medicine_id = cial.op_medicine_pres_id)"
          + " WHERE infection_id = ? order by antibiotic_log_id ";


  /**
   * Gets the antibiotics.
   *
   * @param infectionId the infection id
   * @return the antibiotics
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getAntibiotics(String infectionId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(CLINICAL_ANTIBIOTICS_WITH_MEDICINE);
      ps.setInt(1, Integer.parseInt(infectionId));
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant PHARMA_MEDICINES. */
  private static final String PHARMA_MEDICINES =
      " SELECT sid.medicine_name as item_name,"
          + " sid.medicine_id::text as item_id,g.generic_code, g.generic_name,"
          + " coalesce(sid.item_form_id, 0) as item_form_id, sid.item_strength,ifm.item_form_name"
          + "  FROM store_item_details sid"
          + "   LEFT JOIN generic_name g ON (sid.generic_name=g.generic_code)"
          + "   LEFT JOIN item_form_master ifm ON(ifm.item_form_id = sid.item_form_id)"
          + " WHERE sid.status='A'"
          + "   AND (medicine_name ilike ? OR g.generic_name ilike ? OR medicine_name ilike ?)"
          + " GROUP BY medicine_name, sid.medicine_id,g.generic_code, g.generic_name,"
          + " sid.item_form_id, sid.item_strength,ifm.item_form_name"
          + " ORDER BY medicine_name limit 100";


  /**
   * Gets the all items.
   *
   * @param findItem the find item
   * @return the all items
   * @throws SQLException the SQL exception
   */
  public static List getAllItems(String findItem) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      String query = PHARMA_MEDICINES;
      ps = con.prepareStatement(query);
      ps.setString(1, findItem + "%"); // name starts with "xx"
      ps.setString(2, findItem + "%"); // generic starts with "xx"
      ps.setString(3, "% " + findItem + "%"); // name contains " xx"

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
