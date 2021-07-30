package com.insta.hms.orders;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class ConsultationTypesDAO.
 */
public class ConsultationTypesDAO extends GenericDAO {

  /**
   * Instantiates a new consultation types DAO.
   */
  public ConsultationTypesDAO() {
    super("consultation_types");
  }

  /**
   * Gets the consultation types.
   *
   * @param patientType1    the patient type 1
   * @param patientType2    the patient type 2
   * @param orgId           the org id
   * @param healthAuthority the health authority
   * @return the consultation types
   * @throws SQLException the SQL exception
   */
  public static List getConsultationTypes(String patientType1, String patientType2, String orgId,
      String healthAuthority) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;

    BasicDynaBean healthPref = new HealthAuthorityPreferencesDAO().findByKey("health_authority",
        healthAuthority);
    try {
      StringBuilder consultationTypes = new StringBuilder(" SELECT ct.consultation_type_id, "
          + " consultation_type, status, consultation_code, patient_type, "
          + " doctor_charge_type, service_sub_group_id, ct.username, ct.mod_time,"
          + " charge_head,insurance_category_id,visit_consultation_type "
          + " FROM consultation_types ct " + " JOIN consultation_org_details co on "
          + " (co.consultation_type_id = ct.consultation_type_id and co.applicable) ");

      if (healthPref != null && healthPref.get("consultation_code_types") != null
          && !healthPref.get("consultation_code_types").equals("")) {
        consultationTypes.append(
            " JOIN (SELECT trim(regexp_split_to_table(consultation_code_types, ',')) as code_type"
                + " FROM  health_authority_preferences "
                + " WHERE health_authority=?) as foo ON (foo.code_type = co.code_type) ");
      }

      consultationTypes
          .append(" WHERE (patient_type=? OR patient_type=?) and status='A' and co.org_id = ? ");
      ps = con.prepareStatement(consultationTypes.toString());
      if (healthPref != null && healthPref.get("consultation_code_types") != null
          && !healthPref.get("consultation_code_types").equals("")) {
        ps.setString(1, healthAuthority);
        ps.setString(2, patientType1);
        ps.setString(3, patientType2);
        ps.setString(4, orgId);
      } else {
        ps.setString(1, patientType1);
        ps.setString(2, patientType2);
        ps.setString(3, orgId);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the consultation types.
   *
   * @param patientType     the patient type
   * @param orgId           the org id
   * @param healthAuthority the health authority
   * @return the consultation types
   * @throws SQLException the SQL exception
   */
  public static List getConsultationTypes(String patientType, String orgId, String healthAuthority)
      throws SQLException {
    return getConsultationTypes(patientType, "", orgId, healthAuthority);
  }

}
