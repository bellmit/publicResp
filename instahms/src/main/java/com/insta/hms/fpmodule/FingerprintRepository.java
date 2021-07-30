package com.insta.hms.fpmodule;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.util.IOUtils;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FingerprintRepository extends GenericRepository {

  public FingerprintRepository() {
    super("patient_fingerprints");
  }

  private static final String FETCH_FP = "SELECT fp_data "
      + "FROM patient_fingerprints WHERE mr_no = ? and finger = ?";

  /**
   * Gets the finger print.
   *
   * @param mrNo
   *          the mr no
   * @param finger
   *          the finger
   * @return the finger print
   * @throws IOException the IOException
   */
  public byte[] getFingerPrint(String mrNo, String finger) throws IOException {
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(FETCH_FP, mrNo, finger);

    ByteArrayInputStream fingerprint = (ByteArrayInputStream) bean.get("fp_data");
    return IOUtils.toByteArray(fingerprint);
  }

  private static final String ADD_FP = "INSERT INTO patient_fingerprints "
      + "(mr_no, created_by, created_at, updated_by, updated_at, finger, fp_data, fp_thumbnail) "
      + "VALUES (?, ?, 'now', ?, 'now', ?, ?, ?)";

  /**
   * Adds the finger print.
   *
   * @param mrNo the mr no
   * @param fpData the fp data
   * @param user the user
   * @param finger the finger
   * @param fpThumbnail the fp thumbnail
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean addFingerPrint(String mrNo, byte[] fpData, String user, String finger,
      byte[] fpThumbnail) throws SQLException {

    int res = DatabaseHelper.insert(ADD_FP, mrNo, user, user, finger, fpData, fpThumbnail);
    return (res > 0);
  }

  private static final String GET_FINGERS = "SELECT finger "
      + "FROM patient_fingerprints WHERE mr_no = ?";

  /**
   * Gets the finger by mr no.
   *
   * @param mrNo the mr no
   * @return the finger by mr no
   * @throws SQLException the SQL exception
   */
  public List<String> getFingerByMrNo(String mrNo) throws SQLException {

    List<BasicDynaBean> beans = DatabaseHelper.queryToDynaList(GET_FINGERS, mrNo);
    if (beans == null || beans.isEmpty()) {
      return null;
    }
    List<String> fingers = new ArrayList<String>();
    for (BasicDynaBean bean : beans) {
      fingers.add((String) bean.get("finger"));
    }
    return fingers;

  }

  private static final String GET_FINGER_COUNT = "SELECT COUNT(finger) "
      + "FROM patient_fingerprints WHERE mr_no = ?";

  private static final int getFingerCount(String mrNo) {

    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(GET_FINGER_COUNT);
    return (int) bean.get("count");

  }

  private static final String DELETE_FINGERPRINT = "DELETE FROM patient_fingerprints "
      + "WHERE mr_no = ? and finger= ?";

  /**
   * Delete finger print by mr no.
   *
   * @param mrNo the mr no
   * @param finger the finger
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean deleteFingerPrintByMrNo(String mrNo, String finger) throws SQLException {

    int res = DatabaseHelper.delete(DELETE_FINGERPRINT, mrNo, finger);
    return (res > 0);
  }

  private static final String GET_ALL_PURPOSE = "SELECT purpose "
      + "FROM fp_log_purpose WHERE status='A' and purpose_id > 5";

  /**
   * Gets the all purpose.
   *
   * @return the all purpose
   * @throws SQLException the SQL exception
   */
  public List<String> getAllPurpose() throws SQLException {

    List<BasicDynaBean> beans = DatabaseHelper.queryToDynaList(GET_ALL_PURPOSE);
    List<String> purposes = new ArrayList<String>();
    for (BasicDynaBean bean : beans) {
      purposes.add((String) bean.get("purpose"));
    }
    return purposes;
  }

  private static final String GET_PURPOSE_ID = "SELECT purpose_id "
      + "FROM fp_log_purpose WHERE purpose= ?";

  /**
   * Gets the purpose id by purpose.
   *
   * @param purpose the purpose
   * @return the purpose id by purpose
   * @throws SQLException the SQL exception
   */
  public int getPurposeIdByPurpose(String purpose) throws SQLException {

    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(GET_PURPOSE_ID, purpose);
    return (int) bean.get("purpose_id");
  }

  private static final String FINGERPRINT_LOG = "INSERT INTO fp_logs"
      + "( mr_no, patient_id, purpose_id, finger, authorized_by, authorized_at) "
      + "VALUES ( ?, ?, ?, ?, ?, 'now')";

  /**
   * Log entry.
   *
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param purposeId the purpose id
   * @param user the user
   * @param finger the finger
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean logAuditEntry(String mrNo, String patientId, int purposeId, String user,
      String finger) throws SQLException {

    int res = DatabaseHelper.insert(FINGERPRINT_LOG, mrNo, patientId, purposeId, finger, user);
    return (res > 0);
  }

  private static final String GET_FINGERPRINT_IMAGE = "SELECT fp_thumbnail "
      + "FROM patient_fingerprints WHERE mr_no = ?";

  /**
   * Gets the photo.
   *
   * @param mrNo the mr no
   * @return the photo
   */
  public List<BasicDynaBean> getPhoto(String mrNo) {

    return DatabaseHelper.queryToDynaList(GET_FINGERPRINT_IMAGE, mrNo);

  }

  private static final String GET_THRESHOLD = "SELECT fingerprint_dp_threshold "
      + "FROM generic_preferences";

  public BasicDynaBean getThreshold() {
    return DatabaseHelper.queryToDynaBean(GET_THRESHOLD);
  }

}
