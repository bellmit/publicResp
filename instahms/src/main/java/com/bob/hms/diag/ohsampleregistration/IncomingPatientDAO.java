package com.bob.hms.diag.ohsampleregistration;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * The Class IncomingPatientDAO.
 */
public class IncomingPatientDAO {

  /** The log. */
  private static Logger log = LoggerFactory.getLogger(IncomingPatientDAO.class);

  /**
   * Find patients.
   *
   * @param findString the find string
   * @param limit      the limit
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List findPatients(String findString, int limit) throws SQLException {
    if (limit <= 0) {
      limit = 100;
    }

    Connection con = null;
    PreparedStatement ps = null;

    if ((findString == null) || (findString.length() < 2)) {
      return null;
    }

    // if the string contains a space, we consider it as a further filter on the part of the
    // string after the space.
    StringBuilder filterOn = new StringBuilder();
    String[] findStringComponents = findString.split(" ");
    StringBuilder finalQuery = new StringBuilder(FIND_INCOIMNG_PATIENT);
    try {
      con = DataBaseUtil.getConnection();
      boolean first = true;
      for (String userInput : findStringComponents) {
        if (!first) {
          filterOn.append(" AND ");
        }

        filterOn.append(" patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) "
            + "AND (isr.mr_no ILIKE ? OR isr.patient_name ILIKE ? OR incoming_visit_id LIKE ?)");
        first = false;
      }

      finalQuery.append(" WHERE ").append(filterOn).append("LIMIT ?");
      ps = con.prepareStatement(finalQuery.toString());
      int index = 1;
      for (String userInput : findStringComponents) {
        ps.setString(index++, "%" + userInput);
        ps.setString(index++, "%" + userInput + "%");
        ps.setString(index++, "%" + userInput);
      }
      ps.setInt(index++, limit);
      log.debug("{}", ps);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant FIND_INCOIMNG_PATIENT. */
  private static final String FIND_INCOIMNG_PATIENT = " SELECT isr.mr_no,incoming_visit_id,"
      + " isr.patient_name "
      + " FROM incoming_sample_registration isr"
      + " LEFT JOIN patient_details pd ON (pd.mr_no = isr.mr_no) ";

  /** The Constant GET_INCOMING_PATIENT_VISIT_DETAILS. */
  private static final String GET_INCOMING_PATIENT_VISIT_DETAILS = " SELECT *, 't' as visit_type "
      + " FROM incoming_sample_ext_view WHERE patient_id = ? ";

  /**
   * Gets the patient visit details.
   *
   * @param patientId the patient id
   * @return the patient visit details
   * @throws SQLException the SQL exception
   */
  public static Map getPatientVisitDetails(String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean bean = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_INCOMING_PATIENT_VISIT_DETAILS);
      ps.setString(1, patientId);

      List list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() > 0) {
        bean = (BasicDynaBean) list.get(0);
        boolean precise = (bean.get("dateofbirth") != null);
        if (bean.get("expected_dob") != null) {
          bean.set("age_text",
              DateUtil.getAgeText((java.sql.Date) bean.get("expected_dob"), precise));
        }
        return bean.getMap();
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_INCOMING_PATIENT_VISITS. */
  private static final String GET_INCOMING_PATIENT_VISITS = " SELECT isr.incoming_visit_id,"
      + " isr.orig_lab_name as incoming_hosp_id, "
      + "isr.patient_name, isr.patient_age, " + "isr.age_unit, isr.phone_no, isr.patient_gender, "
      + "to_char(isr.date AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') "
      + "as reg_date_time, " + "ih.hospital_name, isr.patient_other_info "
      + "FROM incoming_sample_registration isr "
      + "JOIN incoming_hospitals ih on(isr.orig_lab_name=ih.hospital_id) "
      + "WHERE date BETWEEN ? and ?";

  /**
   * Gets the incoming patient visits.
   *
   * @param fromTime       the from time
   * @param toTime         the to time
   * @param incomingHospId the incoming hosp id
   * @return the incoming patient visits
   * @throws SQLException the SQL exception
   */
  public static List getIncomingPatientVisits(Object fromTime, Object toTime, String incomingHospId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean bean = null;
    try {
      con = DataBaseUtil.getConnection();
      StringBuilder queryStr = new StringBuilder(GET_INCOMING_PATIENT_VISITS);
      if (incomingHospId != null && !incomingHospId.equals("")) {
        queryStr.append(" AND isr.orig_lab_name = ? ");
      }
      ps = con.prepareStatement(queryStr.toString());
      ps.setObject(1, fromTime);
      ps.setObject(2, toTime);
      if (incomingHospId != null && !incomingHospId.equals("")) {
        ps.setString(3, incomingHospId);
      }
      List list = DataBaseUtil.queryToArrayList(ps);
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
