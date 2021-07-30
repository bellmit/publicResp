package com.insta.hms.advancedpackages;

import com.bob.hms.common.DataBaseUtil;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;


// TODO: Auto-generated Javadoc
/**
 * The Class PackageServicesDAO.
 */
public class PackageServicesDAO {

  /** The Constant SIGNED_OFF_PACKAGE_SERVICE_REPORTS. */
  public static final String SIGNED_OFF_PACKAGE_SERVICE_REPORTS =
      " SELECT sd.doc_id, sd.doc_name as report_name, "
      + " coalesce(presdoc.doctor_name, pht.tech_name) as pres_doctor_name, "
      + " dat.access_rights, sd.username as report_user_name, "
      + " (CASE WHEN dat.doc_format IS NULL THEN 'doc_fileupload' "
      + " ELSE dat.doc_format END) AS doc_format, "
      + " content_type, sd.doc_date, sp.patient_id, sp.mr_no, "
      + " coalesce(sd.signed_off, false) as signed_off, pdoc.content_type "
      + " FROM services_prescribed sp "
      + "  LEFT JOIN doctors presdoc on (sp.doctor_id=presdoc.doctor_id) "
      + "  LEFT JOIN hospital_technical pht on (sp.doctor_id=pht.tech_id) "
      + "  LEFT JOIN service_documents sd ON (sp.prescription_id=sd.prescription_id) "
      + "  LEFT JOIN patient_documents pdoc on sd.doc_id=pdoc.doc_id "
      + "  LEFT OUTER JOIN doc_all_templates_view dat ON (pdoc.template_id=dat.template_id "
      + " AND pdoc.doc_format=dat.doc_format) "
      + " WHERE coalesce(signed_off, false)=true ";

  /**
   * Gets the signed off package service reports.
   *
   * @param packageRef the package ref
   * @return the signed off package service reports
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getSignedOffPackageServiceReports(int packageRef)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(SIGNED_OFF_PACKAGE_SERVICE_REPORTS + " AND sp.package_ref=?");
      ps.setInt(1, packageRef);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
