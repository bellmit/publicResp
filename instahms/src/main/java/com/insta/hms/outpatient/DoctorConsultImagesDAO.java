package com.insta.hms.outpatient;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * The Class DoctorConsultImagesDAO.
 *
 * @author krishna.t
 */
public class DoctorConsultImagesDAO extends GenericDAO {

  /**
   * Instantiates a new doctor consult images DAO.
   */
  public DoctorConsultImagesDAO() {
    super("doctor_consult_images");
  }

  /**
   * Gets the image.
   *
   * @param imageId the image id
   * @return the image
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getImage(Object imageId) throws SQLException {
    BasicDynaBean bean = getBean();
    loadByteaRecords(bean, "image_id", imageId);
    return bean;
  }

  /** The Constant IMAGES_HISTORY. */
  private static final String IMAGES_HISTORY = " SELECT dc.consultation_id,"
      + " patient_id, visited_date, content_type, datetime, image_id "
      + " FROM doctor_consult_images dci" + " JOIN doctor_consultation dc using (consultation_id)"
      + " WHERE consultation_id IN " + "  (SELECT consultation_id FROM doctor_consultation dc "
      + "     JOIN patient_registration pr using (patient_id) "
      + " WHERE dc.mr_no=? AND pr.visit_type=? and doctor_name = ? AND consultation_id < ?"
      + " ORDER BY consultation_id desc) order by consultation_id desc";

  /**
   * Gets the images history.
   *
   * @param mrNo           the mr no
   * @param visitType      the visit type
   * @param doctorName     the doctor name
   * @param consultationId the consultation id
   * @return the images history
   * @throws SQLException the SQL exception
   */
  public static List getImagesHistory(String mrNo, String visitType, String doctorName,
      int consultationId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(IMAGES_HISTORY);
      ps.setString(1, mrNo);
      ps.setString(2, visitType);
      ps.setString(3, doctorName);
      ps.setInt(4, consultationId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
