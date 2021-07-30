/**
 *
 */

package com.insta.hms.eauthorization;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class EAuthRequestsDAO.
 *
 * @author lakshmi
 */
public class EAuthRequestsDAO {

  /**
   * The preauth prescdao.
   */
  EAuthPrescriptionDAO preauthPrescdao = new EAuthPrescriptionDAO();

  /**
   * The preauth req DAO.
   */
  GenericDAO preauthReqDAO = new GenericDAO("preauth_prescription_request");

  /**
   * The preauth req app DAO.
   */
  GenericDAO preauthReqAppDAO = new GenericDAO(
      "preauth_request_approval_details");

  public String getGeneratedEAuthRequestId() throws SQLException {
    return DataBaseUtil.getNextPatternId("preauth_request_approval_details");
  }

  /**
   * Save E auth request details.
   *
   * @param preauthPrescId  the preauth presc id
   * @param preauthCenterId the preauth center id
   * @param insuranceCoId   the insurance co id
   * @param userid          the userid
   * @param requestType     the request type
   * @param userAction      the user action
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean saveEAuthRequestDetails(int preauthPrescId,
                                         Integer preauthCenterId, String insuranceCoId,
                                         String userid,
                                         String requestType, String userAction)
      throws SQLException, IOException {

    boolean success = true;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      BasicDynaBean preauthPresBean = preauthPrescdao
          .getPreauthPrescriptionBean(preauthPrescId);

      String serviceRegNo = null;

      if (preauthCenterId != null) {
        BasicDynaBean centerbean = new CenterMasterDAO()
            .findByKey("center_id", preauthCenterId);
        serviceRegNo = centerbean
            .get("hospital_center_service_reg_no") != null
            ? (String) centerbean
            .get("hospital_center_service_reg_no")
            : "";
      }

      String healthAuthority = CenterMasterDAO
          .getHealthAuthorityForCenter(preauthCenterId);

      String receiverId = null;
      String patientId = DataBaseUtil.getStringValueFromDb("SELECT visit_id "
          + " FROM preauth_prescription_activities WHERE preauth_presc_id = ? LIMIT 1 ",
          preauthPrescId);

      String sponsorId = DataBaseUtil.getStringValueFromDb("SELECT sponsor_id "
          + " FROM patient_insurance_plans WHERE patient_id = ? AND insurance_co = ? ",
          new Object[] {patientId, insuranceCoId});

      BasicDynaBean tpabean = DataBaseUtil.queryToDynaBean(
          "SELECT htc.tpa_code, tm.tpa_name FROM tpa_master tm "
              + " LEFT JOIN ha_tpa_code htc ON(htc.tpa_id=tm.tpa_id AND health_authority = ?) "
              + " WHERE tm.tpa_id = ? ",
          new Object[] {healthAuthority, sponsorId});
      receiverId = tpabean.get("tpa_code") != null ? (String) tpabean.get("tpa_code") :
          "@" + (String) tpabean.get("tpa_name");

      String preauthRequestId = null;
      String preauthPrescriptionRequestId = null;

      // Generate Request Id if Prior Auth prescription has No Request Id.
      if (preauthPresBean.get("preauth_request_id") != null
          && !preauthPresBean.get("preauth_request_id").equals("")) {

        preauthRequestId = (String) preauthPresBean
            .get("preauth_request_id");
      } else {

        // REQUESTID format : SERVICEREGNO-EAUTHREQUESTID-YYYYMMDDHH24MISS
        String timeFormatStr = DataBaseUtil.getStringValueFromDb("SELECT to_char(now(), "
            + "'yyyymmddhh24miss')");
        preauthRequestId = getGeneratedEAuthRequestId();
        preauthRequestId = serviceRegNo + "-" + preauthRequestId + "-" + timeFormatStr;
      }

      String preauthResubmitRequestId = preauthPresBean
          .get("preauth_resubmit_request_id") != null
          ? (String) preauthPresBean
          .get("preauth_resubmit_request_id")
          : null;

      preauthPrescriptionRequestId = (preauthResubmitRequestId != null
          && !preauthResubmitRequestId.equals(""))
          ? preauthResubmitRequestId
          : preauthRequestId;
      String isResubmit = (preauthPrescriptionRequestId.equals(preauthRequestId))
          ? "N"
          : "Y";

      // File Name Format Example : REQUESTID-EAUTHPRESCID.xml :
      // MF2222-EPR000115-20140227112844-66.xml
      String fileName = preauthPrescriptionRequestId + "-"
          + preauthPrescId + ".xml";

      BasicDynaBean preauthReqAppBean = preauthReqAppDAO.getBean();
      preauthReqAppBean.set("preauth_request_id", preauthRequestId);
      preauthReqAppBean.set("preauth_request_type", requestType);
      preauthReqAppBean.set("request_date",
          DateUtil.getCurrentTimestamp());

      BasicDynaBean existingReqAppBean = preauthReqAppDAO
          .findByKey("preauth_request_id", preauthRequestId);

      if (existingReqAppBean == null || !userAction.equals("View")) {
        preauthReqAppBean.set("request_by", userid);
        preauthReqAppBean.set("file_name", fileName);
        preauthReqAppBean.set("file_id", "");
        preauthReqAppBean.set("is_resubmit", isResubmit);
        preauthReqAppBean.set("center_id", preauthCenterId);
        preauthReqAppBean.set("preauth_sender_id", serviceRegNo);
        preauthReqAppBean.set("preauth_receiver_id", receiverId);
      }

      if (existingReqAppBean == null) {
        if (!preauthReqAppDAO.insert(con, preauthReqAppBean)) {
          success = false;
          return success;
        }
      } else if (!userAction.equals("View")) {
        int count = preauthReqAppDAO.updateWithName(con,
            preauthReqAppBean.getMap(), "preauth_request_id");
        success = (count > 0);
        if (!success) {
          return success;
        }
      }

      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("preauth_presc_id", preauthPrescId);
      keys.put("preauth_request_id", preauthPrescriptionRequestId);
      BasicDynaBean preauthPrescReq = preauthReqDAO.findByKey(keys);

      if (preauthPrescReq == null) {
        preauthPrescReq = preauthReqDAO.getBean();
        preauthPrescReq.set("preauth_presc_id", preauthPrescId);
        preauthPrescReq.set("preauth_request_id",
            preauthPrescriptionRequestId);
        preauthPrescReq.set("mod_time", DateUtil.getCurrentTimestamp());
        preauthPrescReq.set("username", userid);
        preauthPrescReq.set("preauth_presc_req_id", DataBaseUtil
            .getNextSequence("preauth_prescription_request_seq"));

        if (!preauthReqDAO.insert(con, preauthPrescReq)) {
          success = false;
          return success;
        }
      }

      BasicDynaBean preauthPrescBean = preauthPrescdao.getBean();
      preauthPrescBean.set("preauth_presc_id", preauthPrescId);
      preauthPrescBean.set("preauth_request_id", preauthRequestId);
      int count = preauthPrescdao.updateWithName(con,
          preauthPrescBean.getMap(), "preauth_presc_id");
      success = success && (count > 0);

    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  /**
   * Gets the e auth request type.
   *
   * @param preauthPrescId the preauth presc id
   * @return the e auth request type
   * @throws SQLException the SQL exception
   */
  public String getEAuthRequestType(int preauthPrescId) throws SQLException {

    List<String> columns = new ArrayList<String>();
    columns.add("preauth_request_id");

    Map<String, Object> key = new HashMap<String, Object>();
    key.put("preauth_presc_id", preauthPrescId);
    BasicDynaBean preauthPresBean = preauthPrescdao.findByKey(columns, key);

    String preauthRequestId = preauthPresBean
        .get("preauth_request_id") != null
        ? (String) preauthPresBean.get("preauth_request_id")
        : null;

    BasicDynaBean reqAppBean = preauthReqAppDAO
        .findByKey("preauth_request_id", preauthRequestId);
    String requestType = (reqAppBean != null
        && reqAppBean.get("preauth_request_type") != null)
        ? (String) reqAppBean.get("preauth_request_type")
        : null;

    return (requestType == null || requestType.trim().equals(""))
        ? "Authorization"
        : requestType;
  }
  

  /** The Constant DOWNLOAD_ALL_XML. */
  private static final String DOWNLOAD_ALL_XML = " SELECT DISTINCT file_id "
        + "FROM preauth_prescription_request "
        + " WHERE preauth_presc_id = ? AND file_id IS NOT NULL AND file_id !='' ";

  /**
   * Get the file id of EauthResponse File.
   * 
   * @param preauthPrescId   the preauth presc id
   * @param preauthRequestId the preauth request id
   * @param downloadKey      it shows the number of file id to download
   * @return the list of file id
   * @throws SQLException the SQLException
  */

  public List<BasicDynaBean> getFileIds(int preauthPrescId, String preauthRequestId,
      String downloadKey) throws SQLException {
    String query = "";
    if (downloadKey.equalsIgnoreCase("all")) {
      query = DOWNLOAD_ALL_XML;
      return DataBaseUtil.queryToDynaList(query.toString(), preauthPrescId);
    } 
    return Collections.EMPTY_LIST;
  }
}
