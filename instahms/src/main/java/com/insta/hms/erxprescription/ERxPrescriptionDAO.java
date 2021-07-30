/**
 *
 */
package com.insta.hms.erxprescription;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.erxprescription.erxauthorization.PriorAuthAuthorization;
import com.insta.hms.erxprescription.erxauthorization.PriorAuthorization;
import com.insta.hms.erxprescription.erxauthorization.PriorAuthorizationActivity;
import com.insta.hms.erxprescription.erxauthorization.PriorAuthorizationHeader;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.pbmauthorization.PBMPrescriptionsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author lakshmi
 *
 */
public class ERxPrescriptionDAO {

  static Logger log = LoggerFactory.getLogger(ERxPrescriptionDAO.class);

  PBMPrescriptionsDAO pbmprescdao = new PBMPrescriptionsDAO();
  GenericDAO patMedPrescDAO = new GenericDAO("patient_medicine_prescriptions");

  public static final String GET_LATEST_CONS_ERX_DETAILS = " SELECT pp.erx_consultation_id, pp.erx_presc_id, pp.erx_request_type, pp.erx_request_date "
      + " FROM pbm_prescription pp "
      + " JOIN patient_medicine_prescriptions pmp ON (pmp.pbm_presc_id = pp.pbm_presc_id) "
      + " WHERE pp.erx_consultation_id = ? "
      + "	AND pp.erx_presc_id IS NOT NULL AND pp.erx_reference_no IS NOT NULL LIMIT 1 ";

  public BasicDynaBean getLatestConsErxBean(int consId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_LATEST_CONS_ERX_DETAILS);
      ps.setInt(1, consId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static final String GET_ERX_PBM_ID = " SELECT pbm_presc_id "
      + " FROM patient_prescription pp "
      + " 	JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id) "
      + " WHERE consultation_id = ? ";

  public int getErxConsPBMId(Object consId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ERX_PBM_ID);
      ps.setInt(1, (int)consId);
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static final String GET_ERX_PBM_ID_FOR_VISIT = " SELECT pbm_presc_id "
      + " FROM patient_prescription pp "
      + "   JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id AND is_discharge_medication=true) "
      + " WHERE pp.visit_id = ? ";

  public int getErxVisitPBMId(String visitId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ERX_PBM_ID_FOR_VISIT);
      ps.setString(1, visitId);
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static final String GET_CONS_ERX_DETAILS = "SELECT pr.mr_no, pr.patient_id, pp.pbm_presc_id, pp.erx_consultation_id, pp.erx_presc_id,  "
      + "	   pp.erx_request_type, pp.erx_request_date, pp.erx_center_id, pp.erx_reference_no,  "
      + "	   st.is_selfpay_sponsor, "
      + "	   dc.doctor_name, coalesce(pip.plan_id, pr.plan_id, 0) AS plan_id, pr.primary_sponsor_id, "
      + "       pr.primary_insurance_co, " + "       CASE "
      + "           WHEN pd.government_identifier IS NULL "
      + "                OR pd.government_identifier = '' THEN COALESCE(gim.identifier_type, '') "
      + "           ELSE pd.government_identifier " + "       END AS emirates_id_number, "
      + "       ppd.member_id, " + "       ppd.policy_number, " + "       pr.encounter_type, "
      + "       etc.encounter_type_desc, "
      + "       to_char(coalesce(pd.dateofbirth, expected_dob), 'dd/MM/yyyy') AS dob, "
      + "       pd.email_id " + "FROM pbm_prescription pp "
      + "JOIN doctor_consultation dc ON (dc.consultation_id = pp.erx_consultation_id) "
      + "JOIN patient_registration pr ON (pr.patient_id = dc.patient_id) "
      + "JOIN patient_details pd ON (pd.mr_no = pr.mr_no) "
      + "LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id) "
      + "LEFT JOIN patient_insurance_plans pip ON (pr.patient_id = pip.patient_id "
      + "                                          AND pip.priority=1) "
      + "LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pip.patient_policy_id) "
      + "LEFT JOIN encounter_type_codes etc ON (etc.encounter_type_id = encounter_type) "
      + "LEFT JOIN tpa_master tm ON (tm.tpa_id = pr.primary_sponsor_id) "
      + "LEFT JOIN sponsor_type st ON (st.sponsor_type_id = tm.sponsor_type_id) "
      + "WHERE pp.pbm_presc_id = ? "
      + "AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )";

  public static final String GET_ERX_DETAILS_VISIT_ID = "SELECT pr.mr_no, pr.patient_id, pp.pbm_presc_id, pp.erx_visit_id, pp.erx_presc_id,  "
      + " pp.erx_request_type, pp.erx_request_date, pp.erx_center_id, pp.erx_reference_no,  "
      + "   st.is_selfpay_sponsor, "
      + "   pr.doctor as doctor_name, coalesce(pip.plan_id, pr.plan_id, 0) AS plan_id, "
      + " pr.primary_sponsor_id, " //
      + "   pr.primary_insurance_co, " + "       CASE "
      + "     WHEN pd.government_identifier IS NULL "
      + "     OR pd.government_identifier = '' THEN COALESCE(gim.identifier_type, '') "
      + "     ELSE pd.government_identifier " + "       END AS emirates_id_number, "
      + "       ppd.member_id, " + "       ppd.policy_number, "
      + "       pr.encounter_type, " + "       etc.encounter_type_desc, "
      + "       to_char(coalesce(pd.dateofbirth, expected_dob), 'dd/MM/yyyy') AS dob, "
      + "       pd.email_id " + "FROM pbm_prescription pp "
      + "JOIN patient_registration pr ON (pr.patient_id = pp.erx_visit_id) "
      + "JOIN patient_details pd ON (pd.mr_no = pr.mr_no) "
      + "LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id) "
      + "LEFT JOIN patient_insurance_plans pip ON  "
      + " (pr.patient_id = pip.patient_id AND pip.priority=1) "
      + "LEFT JOIN patient_policy_details ppd ON "
      + " (ppd.patient_policy_id = pip.patient_policy_id) "
      + "LEFT JOIN encounter_type_codes etc ON (etc.encounter_type_id = encounter_type) "
      + "LEFT JOIN tpa_master tm ON (tm.tpa_id = pr.primary_sponsor_id) "
      + "LEFT JOIN sponsor_type st ON (st.sponsor_type_id = tm.sponsor_type_id) "
      + "WHERE pp.pbm_presc_id = ? ";

  public BasicDynaBean getConsErxDetails(int pbmPrescId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      BasicDynaBean pbmPresBean = pbmprescdao.findByKey("pbm_presc_id", pbmPrescId);
      if(pbmPresBean != null && pbmPresBean.get("erx_visit_id") != null){
        ps = con.prepareStatement(GET_ERX_DETAILS_VISIT_ID);
      }else{
        ps = con.prepareStatement(GET_CONS_ERX_DETAILS);
      }
      ps.setInt(1, pbmPrescId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static String GET_ERX_CENTER_HEADER_DETAILS = " SELECT pbmp.erx_file_name, pbmp.pbm_presc_id, pbmp.erx_consultation_id, pbmp.erx_presc_id, hcm.center_id, hcm.center_name,"
      + " COALESCE (hcm.hospital_center_service_reg_no,gp.hospital_service_regn_no) AS provider_id, "
      + " (SELECT COALESCE (htpa.tpa_code, '@'||tpam.tpa_name) FROM tpa_master tpam "
      + " LEFT JOIN ha_tpa_code htpa on (tpam.tpa_id = htpa.tpa_id AND health_authority = ?) WHERE tpam.tpa_id = ?) "
      + " AS receiver_id, " + " (SELECT COALESCE (insurance_co_code, '@'||insurance_co_name) "
      + "	FROM insurance_company_master icm "
      + "   LEFT JOIN ha_ins_company_code hic ON(hic.insurance_co_id = icm.insurance_co_id AND health_authority = ?) WHERE icm.insurance_co_id = ?) "
      + " AS payer_id,"
      + " to_char(erx_request_date::timestamp, 'dd/MM/yyyy hh24:mi') AS transaction_date, "
      + " 1 as erx_record_count, "
      + " (SELECT doctor_license_number FROM doctors WHERE doctor_id = ?) AS doctor_license_number, "
      + " (SELECT doctor_name FROM doctors WHERE doctor_id = ?) AS doctor_name "
      + " FROM generic_preferences gp, pbm_prescription pbmp "
      + " LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pbmp.erx_center_id AND hcm.center_id != 0) "
      + " WHERE pbm_presc_id = ? ";

  public BasicDynaBean getERxHeaderFields(int pbmPrescId, String doctorId, String tpaId,
      String insuCompId, String healthAuthority) throws SQLException {
    return DataBaseUtil.queryToDynaBean(GET_ERX_CENTER_HEADER_DETAILS, new Object[] {
        healthAuthority, tpaId, healthAuthority, insuCompId, doctorId, doctorId, pbmPrescId });
  }

  private static String GET_ERX_ACTIVITIES = " SELECT sid.medicine_name, pmp.medicine_id, medicine_quantity, "
      + " 	pmp.op_medicine_pres_id, pmp.op_medicine_pres_id::text as activity_id, "
      + "	medicine_remarks, frequency, strength, duration, duration_units,"
      + "	pp.status as issued, sid.cons_uom_id, cum.consumption_uom, g.generic_name, pmp.generic_code,  "
      + "	mod_time, mr.route_id, mr.route_name, mr.route_code, "
      + "   icm.category AS category_name, mm.manf_name, mm.manf_mnemonic, "
      + "	sid.prior_auth_required, coalesce(pmp.item_form_id, 0) as item_form_id, "
      + "	pmp.item_strength, if.item_form_name, " + "	pmp.item_strength_units, su.unit_name, "
      + "	to_char(pp.prescribed_date::timestamp, 'dd/MM/yyyy hh24:mi') AS activity_prescribed_date,"
      + "   pp.prescribed_date , " + "	msct.haad_code, sic.item_code, sic.code_type, "
      + "   pmp.pbm_presc_id, pmp.erx_status," + "   pmp.erx_denial_code, pmp.erx_denial_remarks, "
      + "	idc.status AS denial_code_status, idc.type AS denial_code_type, if.granular_units "
      + " FROM patient_prescription pp"
      + "	JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id) "
      + "	JOIN pbm_prescription pbmp ON (pbmp.pbm_presc_id = pmp.pbm_presc_id) "
      + "	LEFT JOIN store_item_details sid ON (pmp.medicine_id=sid.medicine_id) "
      + "	LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id AND hict.health_authority=?) "
      + "	LEFT JOIN store_item_codes sic ON (sic.medicine_id = sid.medicine_id AND sic.code_type = hict.code_type) "
      + " 	LEFT JOIN item_form_master if ON (pmp.item_form_id=if.item_form_id) "
      + "	LEFT OUTER JOIN generic_name g ON (pmp.generic_code = g.generic_code) "
      + "	LEFT OUTER JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin) "
      + "	LEFT OUTER JOIN strength_units su ON (pmp.item_strength_units=su.unit_id) "
      + "   LEFT OUTER JOIN manf_master mm on mm.manf_code=sid.manf_name "
      + "   LEFT OUTER JOIN store_category_master icm on sid.med_category_id=icm.category_id "
      + "	LEFT JOIN mrd_supported_code_types msct ON (msct.code_type = sic.code_type) "
      + "	LEFT JOIN insurance_denial_codes idc ON (idc.denial_code = pmp.erx_denial_code) "
      + "   LEFT JOIN consumption_uom_master cum ON (sid.cons_uom_id = cum.cons_uom_id) "
      + " WHERE pbmp.pbm_presc_id=? AND send_for_erx='Y' ORDER BY pmp.op_medicine_pres_id ";

  @SuppressWarnings("unchecked")
  public List<BasicDynaBean> getErxPrescribedActivities(int pbmPrescId, String healthAuthority)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_ERX_ACTIVITIES,
        new Object[] { healthAuthority, pbmPrescId });
  }

  public boolean saveErxRequestDetails(int pbmPrescId, String userid, String requestType,
      String userAction) throws SQLException, IOException {
    boolean success = true;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      BasicDynaBean genPrefs = GenericPreferencesDAO.getAllPrefs();
      Integer centerId = RequestContext.getCenterId();

      List<String> columns = new ArrayList<String>();
      columns.add("pbm_presc_id");
      columns.add("erx_presc_id");
      columns.add("erx_center_id");
      columns.add("erx_request_date");
      columns.add("erx_request_type");
      columns.add("erx_request_by");
      columns.add("erx_file_name");
      columns.add("erx_reference_no");

      Map<String, Object> key = new HashMap<String, Object>();
      key.put("pbm_presc_id", pbmPrescId);
      BasicDynaBean pbmPresBean = pbmprescdao.findByKey(columns, key);

      Timestamp erxRequestDate = DateUtil.getCurrentTimestamp();
      if ((userAction != null && userAction.equals("View"))
          && null != pbmPresBean.get("erx_request_date"))
        erxRequestDate = (Timestamp) pbmPresBean.get("erx_request_date");

      String erxRequestType = requestType;
      if ((userAction != null && userAction.equals("View"))
          && null != pbmPresBean.get("erx_request_type"))
        erxRequestType = (String) pbmPresBean.get("erx_request_type");

      Integer erxCenterId = centerId;
      if (null != pbmPresBean.get("erx_center_id"))
        erxCenterId = (Integer) pbmPresBean.get("erx_center_id");

      String facilityId = null;

      if (erxCenterId != null && erxCenterId != 0) {
        BasicDynaBean centerbean = new CenterMasterDAO().findByKey("center_id", centerId);
        facilityId = centerbean.get("hospital_center_service_reg_no") != null
            ? (String) centerbean.get("hospital_center_service_reg_no")
            : "";
      } else {
        facilityId = genPrefs.get("hospital_service_regn_no") != null
            ? (String) genPrefs.get("hospital_service_regn_no")
            : "";
      }

      String payerId = "";
      int consultationId = DataBaseUtil.getIntValueFromDb("SELECT pp.consultation_id "
          + " FROM patient_prescription pp "
          + "	JOIN patient_medicine_prescriptions pmp on (pp.patient_presc_id=pmp.op_medicine_pres_id) WHERE pbm_presc_id = "
          + pbmPrescId + " LIMIT 1 ");

      String patientId = DataBaseUtil.getStringValueFromDb("SELECT patient_id "
          + " FROM doctor_consultation WHERE consultation_id = ? LIMIT 1", consultationId);

      String primarySponsorId = DataBaseUtil.getStringValueFromDb("SELECT primary_sponsor_id "
          + " FROM patient_registration WHERE patient_id = ?", patientId);

      String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
      String primaryCompanyId = DataBaseUtil.getStringValueFromDb("SELECT primary_insurance_co "
          + " FROM patient_registration WHERE patient_id = ?", patientId);

      BasicDynaBean insubean = DataBaseUtil.queryToDynaBean(
          "SELECT hic.insurance_co_code, icm.insurance_co_name FROM insurance_company_master icm "
              + " LEFT JOIN ha_ins_company_code hic ON(hic.insurance_co_id=icm.insurance_co_id "
              + " AND health_authority = ?) WHERE icm.insurance_co_id = ? ",
          new Object[] { healthAuthority, primaryCompanyId });

      if (null != insubean) {
        payerId = insubean.get("insurance_co_code") != null
            ? (String) insubean.get("insurance_co_code")
            : "@" + (String) insubean.get("insurance_co_name");
      }

      String erxPrescId = null;

      // Generate ERx Presc. ID
      if (pbmPresBean.get("erx_presc_id") != null && !pbmPresBean.get("erx_presc_id").equals("")
          && pbmPresBean.get("erx_reference_no") != null
          && !pbmPresBean.get("erx_reference_no").equals("")) {
        erxPrescId = (String) pbmPresBean.get("erx_presc_id");
      } else {
        String timeFormatStr = DataBaseUtil
            .getStringValueFromDb("SELECT to_char(now(), 'yyyymmddhh24miss')");
        erxPrescId = facilityId + "-" + payerId + "-" + timeFormatStr;
      }

      // File Name Format Example : FACILITYID-PAYERID-UniqueNumber-PBMPRESCID.xml :
      // DHA-F-0046895-INS017-20130212172328-12.xml
      String file_name = erxPrescId + "-" + pbmPrescId + ".xml";

      pbmPresBean.set("erx_request_type", erxRequestType);
      pbmPresBean.set("erx_request_date", erxRequestDate);
      pbmPresBean.set("erx_center_id", erxCenterId);

      if (requestType.equals("eRxCancellation")) {

      } else if (pbmPresBean.get("erx_presc_id") == null
          || pbmPresBean.get("erx_presc_id").equals("")
          || pbmPresBean.get("erx_reference_no") == null
          || pbmPresBean.get("erx_reference_no").equals("")) {
        pbmPresBean.set("erx_presc_id", erxPrescId);
        pbmPresBean.set("erx_request_by", userid);
        pbmPresBean.set("erx_file_name", file_name);
        // pbmPresBean.set("erx_auth_id_payer", "");
      }

      int i = pbmprescdao.updateWithName(con, pbmPresBean.getMap(), "pbm_presc_id");
      success = success && (i > 0);

    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  public boolean saveResponse(Map<String, Object> resultObj) throws SQLException, IOException {
    boolean success = true;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      BasicDynaBean pbmPrescBean = pbmprescdao.getBean();
      pbmPrescBean.set("pbm_presc_id", resultObj.get("pbm_presc_id"));
      String erxReferenceNo = (null != resultObj && null != resultObj.get("eRxReferenceNo"))
          ? resultObj.get("eRxReferenceNo").toString()
          : "";
      pbmPrescBean.set("erx_reference_no", erxReferenceNo);

      log.debug("Saving ERx Response params: Presc. Id: " + resultObj.get("pbm_presc_id")
          + " eRxReferenceNo: " + resultObj.get("eRxReferenceNo"));
      int i = pbmprescdao.updateWithName(con, pbmPrescBean.getMap(), "pbm_presc_id");
      success = (i > 0);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  public static final String DEATTACH_PBM_FROM_ERX = "UPDATE patient_medicine_prescriptions pmp SET pbm_presc_id = NULL from patient_prescription pp "
      + "	WHERE pmp.op_medicine_pres_id=patient_presc_id and consultation_id = ? ";

  public boolean saveCancelResponse(int consId, int pbmPrescId) throws SQLException, IOException {
    boolean success = true;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      BasicDynaBean pbmPrescBean = pbmprescdao.getBean();
      pbmPrescBean.set("pbm_presc_id", pbmPrescId);
      pbmPrescBean.set("status", "C");// Close PBM
      pbmPrescBean.set("erx_reference_no", null);
      int i = pbmprescdao.updateWithName(con, pbmPrescBean.getMap(), "pbm_presc_id");
      success = (i > 0);

      if (success) {
        i = DataBaseUtil.executeQuery(con, DEATTACH_PBM_FROM_ERX, consId);
        success = (i > 0);
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  private static final String SEARCH_ERX_PRESCRIPTION = " SELECT * FROM ( SELECT pbm_presc_id, erx_presc_id, 1 AS record_count, erx_file_name "
      + " FROM pbm_prescription pbm " + " WHERE pbm_presc_id = ? "
      + " AND erx_reference_no = ? ) AS foo ";

  public String searchERxPresc(String recdFileName, int pbmPrescId, String eRxReferenceNo,
      String recordCount) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String erxRequestId = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(SEARCH_ERX_PRESCRIPTION);
      // ps.setString(1, recdFileName); // removed this where condition
      ps.setInt(1, pbmPrescId);
      ps.setString(2, eRxReferenceNo);
      // ps.setInt(4, recordCount != null ? Integer.parseInt(recordCount) : 0); // removed this
      // where condition
      BasicDynaBean pbmbean = DataBaseUtil.queryToDynaBean(ps);
      if (pbmbean != null)
        erxRequestId = (String) pbmbean.get("erx_presc_id");
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return erxRequestId;
  }

  public boolean updateERXRequestFileId(String erxRequestId, String fileId)
      throws SQLException, IOException {
    boolean success = true;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      BasicDynaBean pbmBean = pbmprescdao.getBean();
      pbmBean.set("erx_presc_id", erxRequestId);
      pbmBean.set("erx_file_id", fileId);

      int i = pbmprescdao.updateWithName(con, pbmBean.getMap(), "erx_presc_id");
      success = (i > 0);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  public boolean updateERxApprovalDetails(PriorAuthorization desc)
      throws SQLException, IOException, ParseException {
    boolean success = true;
    boolean allSuccess = false;

    Connection con = null;

    try {
      approvalTxn: {
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);

        SimpleDateFormat timeStampFormatterSecs = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        PriorAuthorizationHeader header = desc.getHeader();
        String transactionDate = header.getTransactionDate();
        if (transactionDate.trim().length() <= 10)
          transactionDate = transactionDate + " 00:00";

        Timestamp txnDate = new java.sql.Timestamp(
            timeStampFormatterSecs.parse(transactionDate).getTime());

        PriorAuthAuthorization priorAuth = desc.getAuthorization();

        String authReqID = priorAuth.getAuthorizationID();
        String authIdPayer = priorAuth.getAuthorizationIDPayer();
        String authDenialCode = priorAuth.getDenialCode();
        String start = priorAuth.getStart();
        String end = priorAuth.getEnd();
        String authResult = priorAuth.getAuthorizationResult();
        BigDecimal limit = priorAuth.getLimit();
        String comments = priorAuth.getComments();

        ArrayList<PriorAuthorizationActivity> activities = priorAuth.getActivities();

        List<String> columns = new ArrayList<String>();
        columns.add("pbm_presc_id");
        columns.add("erx_reference_no");
        columns.add("erx_presc_id");
        columns.add("drug_count");

        Map<String, Object> field = new HashMap<String, Object>();
        field.put("erx_presc_id", authReqID);
        BasicDynaBean prescbean = pbmprescdao.findByKey(columns, field);

        int pbmPrescId = (Integer) prescbean.get("pbm_presc_id");
        int drugCount = (Integer) prescbean.get("drug_count");

        int approvedActivityCount = 0;

        /* Update erx activity details in patient_medicine_prescriptions table. */
        for (PriorAuthorizationActivity activity : activities) {

          String type = activity.getActivityType();
          String code = activity.getActivityCode();
          String activityId = activity.getActivityID();
          String activityDenialCode = activity.getActivityDenialCode();
          BigDecimal quantity = activity.getQuantity();

          int medPresId = Integer.parseInt(activityId);

          activityDenialCode = (activityDenialCode == null) ? authDenialCode : activityDenialCode;

          Map<String, Object> identifiers = new HashMap<String, Object>();
          identifiers.put("pbm_presc_id", pbmPrescId);
          identifiers.put("op_medicine_pres_id", medPresId);

          BasicDynaBean prescMedicineBean = patMedPrescDAO.findByKey(identifiers);

          Integer medQty = (Integer) prescMedicineBean.get("medicine_quantity");

          /* Any denied activity(s) then the ERx Request needs to be resent after cancellation. */
          if (activityDenialCode == null || activityDenialCode.equals("")) {
            approvedActivityCount++;
            prescMedicineBean.set("erx_approved_quantity", quantity);
            prescMedicineBean.set("erx_status", "C");

          } else {
            prescMedicineBean.set("erx_denial_code", activityDenialCode);
            prescMedicineBean.set("erx_approved_quantity", quantity);
            prescMedicineBean.set("erx_status", "D");
          }

          // Update patient_medicine_prescriptions with Denial code and approved quantity.

          int i = patMedPrescDAO.updateWithName(con, prescMedicineBean.getMap(),
              "op_medicine_pres_id");
          success = (i > 0);
          if (!success)
            break approvalTxn;

        }

        /* Mark PBM Approval as F/R: Fully Approved/Fully Rejected */
        String approvalStatus = "";
        if (approvedActivityCount == drugCount) {
          approvalStatus = "F"; // Fully Approved
        } else if (approvedActivityCount == 0) {
          approvalStatus = "R"; // Fully Rejected
        } else {
          approvalStatus = "P"; // Partially Approved
        }

        // Update PBM prescription with Authorization Denial code and approval details.
        BasicDynaBean pbean = pbmprescdao.getBean();
        pbean.set("pbm_presc_id", pbmPrescId);
        pbean.set("erx_denial_code", authDenialCode);
        pbean.set("erx_auth_id_payer", authIdPayer);
        pbean.set("erx_approval_recd_date", txnDate);
        pbean.set("erx_approval_status", approvalStatus);
        pbean.set("erx_approval_comments", comments);
        pbean.set("erx_approval_result", authResult);

        int i = pbmprescdao.updateWithName(con, pbean.getMap(), "pbm_presc_id");
        success = (i > 0);
        if (!success)
          break approvalTxn;

      } // label approvalTxn
      allSuccess = true;

    } finally {
      DataBaseUtil.commitClose(con, allSuccess);
    }

    return success;
  }

  public static BigDecimal getStrengthBigDecimal(String str) {
    try (Scanner scanner = new Scanner(str);) {
      if (scanner.hasNextBigDecimal()) {
        return scanner.nextBigDecimal();
      }
    }
    return BigDecimal.ZERO;
  }

  public boolean updateERxApprovalStatus(String erxRequestId) throws SQLException, IOException {
    boolean success = true;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      List<String> columns = new ArrayList<String>();
      columns.add("pbm_presc_id");
      columns.add("erx_reference_no");
      columns.add("erx_presc_id");
      columns.add("erx_center_id");

      Map<String, Object> field = new HashMap<String, Object>();
      field.put("erx_presc_id", erxRequestId);
      BasicDynaBean erxBean = pbmprescdao.findByKey(columns, field);

      if (erxBean != null) {
        int pbmPrescId = (Integer) erxBean.get("pbm_presc_id");
        BasicDynaBean pbean = pbmprescdao.getBean();
        pbean.set("pbm_presc_id", pbmPrescId);
        pbean.set("erx_approval_status", "F");// Fully Received

        int i = pbmprescdao.updateWithName(con, pbean.getMap(), "pbm_presc_id");
        success = (i > 0);
      }
      return success;
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
  }
}
