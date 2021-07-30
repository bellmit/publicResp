package com.insta.instaapi.patient.registration;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/** General Registartion DAO. */
public class GeneralRegistrationDAO extends GenericDAO {
  public GeneralRegistrationDAO() {
    super("");
  }

  public static final String GET_CITY_DETAILS =
      "select c.city_id,c.city_name,c.state_id  from city c join state_master sm "
          + " on(c.state_id=sm.state_id) join  country_master cm "
          + "on(cm.country_id=sm.country_id) where "
          + " sm.status='A' and c.status='A' and cm.status='A' ";

  /**
   * Get City Details.
   *
   * @param con connection object.
   * @return retuens city details
   * @throws SQLException may throw Sql Exception
   */
  public static List<BasicDynaBean> getCityDetails(Connection con) throws SQLException {

    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_CITY_DETAILS);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      com.insta.instaapi.common.DbUtil.closeConnections(null, ps);
    }
  }

  public static final String GET_STATE_DETAILS =
      "select sm.state_id,sm.state_name,sm.country_id from state_master sm "
          + " join country_master cm on(sm.country_id=cm.country_id) where "
          + "cm.status='A' and sm.status='A'";

  /**
   * Gives the state Details.
   *
   * @param con connection object
   * @return returns list of basicDynaBeans
   * @throws SQLException may throw SQl Exception
   */
  public static List<BasicDynaBean> getStateDetails(Connection con) throws SQLException {

    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_STATE_DETAILS);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      com.insta.instaapi.common.DbUtil.closeConnections(null, ps);
    }
  }
 
  public static final String GET_COUNTRY_DETAILS = 
      "select country_id,country_name,country_code from country_master WHERE status='A'";

  /**
   * Gives the country Details.
   *
   * @param con
   *          connection object
   * @return returns list of basicDynaBeans
   * @throws SQLException
   *           may throw SQl Exception
   */
  public static List<BasicDynaBean> getCountryDetails(Connection con) throws SQLException {

    try (PreparedStatement ps = con.prepareStatement(GET_COUNTRY_DETAILS)) {
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  public static final String GET_GOVT_IDENTIFIER_DETAILS =
      "select identifier_id,remarks as description "
      + " from govt_identifier_master WHERE status='A'";

  /**
   * Gives the Govt. IDentifier Master Details.
   *
   * @param con
   *          connection object
   * @return returns list of basicDynaBeans
   * @throws SQLException
   *           may throw SQl Exception
   */
  public static List<BasicDynaBean> getGovtIdentifierDetails(Connection con) throws SQLException {

    try (PreparedStatement ps = con.prepareStatement(GET_GOVT_IDENTIFIER_DETAILS)) {
      return DataBaseUtil.queryToDynaList(ps);
    }
  }


  public static final String GET_PATIENTCATEGORY_DETAILS =
      "select pm.category_id,pm.category_name,pm.center_id from "
          + " patient_category_master pm join hospital_center_master hm "
          + "on(pm.center_id=hm.center_id) where pm.status='A' "
          + " and hm.status='A'";

  /**
   * Gives the patient category details.
   *
   * @param con connection object.
   * @return returns list of BasicDynaBeans
   * @throws SQLException may throw SQL Exception
   */
  public static List<BasicDynaBean> getPatientCategoryDetails(Connection con) throws SQLException {

    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_PATIENTCATEGORY_DETAILS);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      com.insta.instaapi.common.DbUtil.closeConnections(null, ps);
    }
  }

  /**
   * Check for name, age & gender already exists for any patient.
   *
   * @param con connection Object
   * @param patDetailsBean patient details bean
   * @return returns true or false
   * @throws SQLException may throw SQL Exception
   * @throws ParseException may throw Parse Exception
   */
  public static boolean checkDetailsExist(Connection con, BasicDynaBean patDetailsBean)
      throws SQLException, ParseException {
    // Connection con = null;
    PreparedStatement ps = null;
    try {

      String dob = patDetailsBean.get("remarks").toString();
      StringBuilder q1 = new StringBuilder();
      q1.append(
          " SELECT mr_no, patient_name, middle_name, last_name, dateofbirth, expected_dob, "
              + " to_char(dateofbirth,'dd-MM-yyyy')::text AS dob, "
              + " to_char(expected_dob,'dd-MM-yyyy')::text AS eob, "
              + " get_patient_age(dateofbirth, expected_dob)::text AS age,  "
              + " get_patient_age_in(dateofbirth, expected_dob)::text AS agein, "
              + " CASE WHEN patient_gender='M' THEN 'Male' WHEN "
              + " patient_gender='F' THEN 'Female' ELSE 'Other' END AS patient_gender,"
              + " patient_phone, patient_address, patient_city, city_name, patient_area"
              + " FROM patient_details "
              + " JOIN city ON (city_id = patient_city) "
              + " WHERE patient_name ILIKE ? AND middle_name ILIKE ? AND last_name ILIKE ? "
              + " AND patient_gender = ? ");

      if (null == dob || ("").equals(dob)) {
        q1.append(" and  (select get_patient_age(null, expected_dob)) = ? ");
      } else {
        q1.append(" and dateofbirth = ? ");
      }
      if (null != patDetailsBean.get("patient_phone").toString()
          && (!(patDetailsBean.get("patient_phone").toString().equals("")))) {
        /*q1.append(" UNION "
        + " SELECT mr_no, patient_name, middle_name, last_name, dateofbirth, expected_dob, "
        + " to_char(dateofbirth,'dd-MM-yyyy')::text AS dob, "
        + "to_char(expected_dob,'dd-MM-yyyy')::text AS eob,"
        + " get_patient_age(dateofbirth, expected_dob)::text AS age,  "
        + " get_patient_age_in(dateofbirth, expected_dob)::text AS agein, "
        + " CASE WHEN patient_gender='M' THEN 'Male' WHEN "
        + " patient_gender='F' THEN 'Female' ELSE 'Other' END AS patient_gender,"
        + " patient_phone, patient_address, patient_city, city_name, patient_area "
        + " FROM patient_details  "
        + " JOIN city ON (city_id = patient_city) "
        + " WHERE patient_phone = ? ");*/
        q1.append(" and patient_phone = ? ");
      }
      q1.append(" order by mr_no desc  limit 1  ");

      int count = 1;
      // con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(q1.toString());
      ps.setString(count++, patDetailsBean.get("patient_name").toString());
      ps.setString(count++, patDetailsBean.get("middle_name").toString());
      ps.setString(count++, patDetailsBean.get("last_name").toString());
      ps.setString(count++, patDetailsBean.get("patient_gender").toString());

      if (null == dob || ("").equals(dob)) {
        ps.setInt(count++, Integer.parseInt(patDetailsBean.get("patient_care_oftext").toString()));
      } else {

        java.sql.Date dateOfBirth = null;
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

        if (!patDetailsBean.get("remarks").toString().equalsIgnoreCase("")) {
          dateOfBirth =
              new java.sql.Date((sf.parse((patDetailsBean.get("remarks").toString()))).getTime());
        }

        ps.setDate(count++, dateOfBirth);
      }

      if (null != patDetailsBean.get("patient_phone").toString()
          && (!(patDetailsBean.get("patient_phone").toString().equals("")))) {
        ps.setString(count++, patDetailsBean.get("patient_phone").toString());
      }
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list != null && list.size() > 0) {
        return true;
      } else {
        return false;
      }

    } finally {
      com.insta.instaapi.common.DbUtil.closeConnections(null, ps);
    }
  }
}
