/**
 *
 */

package com.insta.hms.eauthorization;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.adminmasters.services.MasterServicesDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.bob.hms.otmasters.opemaster.OperationMasterDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.core.clinical.diagnosisdetails.HospitalClaimDiagnosisRepository;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.StoresItemMaster.StoresItemDAO;
import com.insta.hms.orders.OrderBO;
import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class EAuthPrescriptionDAO.
 *
 * @author lakshmi
 */
public class EAuthPrescriptionDAO extends GenericDAO {

  /**
   * The logger.
   */
  static Logger logger = LoggerFactory.getLogger(EAuthPrescriptionDAO.class);
  
  private static StoresItemDAO itemDAO = new StoresItemDAO();

  /**
   * The preauth req app DAO.
   */
  GenericDAO preauthReqAppDAO = new GenericDAO(
      "preauth_request_approval_details");

  /**
   * Instantiates a new e auth prescription DAO.
   */
  public EAuthPrescriptionDAO() {
    super("preauth_prescription");
  }

  /**
   * The Constant SELECT_EAUTH_PRESCRIPTION_FIELDS.
   */
  private static final String SELECT_EAUTH_PRESCRIPTION_FIELDS = "SELECT * ";

  /**
   * The Constant SELECT_EAUTH_PRESCRIPTION_COUNT.
   */
  private static final String SELECT_EAUTH_PRESCRIPTION_COUNT = " SELECT count(*) ";

  /**
   * The Constant SELECT_EAUTH_PRESCRIPTION_TABLES.
   */
  private static final String SELECT_EAUTH_PRESCRIPTION_TABLES = " FROM (SELECT "
      + " sm.salutation || ' ' || patient_name || case when coalesce(middle_name, '') = '' "
      + " then '' else (' ' || middle_name) end || case when coalesce(last_name, '') = '' then '' "
      + " else (' ' || last_name) end as patname, "
      + " get_patient_age(dateofbirth, " + " expected_dob) as age,"
      + " get_patient_age_in(dateofbirth, expected_dob) as age_in,pd.patient_phone,pd"
      + ".patient_gender, "
      + " pr.mr_no, pr.patient_id, pr.op_type, pr.status as patstatus, pr.center_id, "
      + " CASE WHEN (pr.op_type != 'O') THEN d.doctor_name ELSE ref.referal_name END AS doctor,"
      + " CASE WHEN (pr.visit_type = 'i' OR pr.op_type = 'O') THEN (pr.reg_date + pr.reg_time) "
      + "ELSE dc.visited_date END AS visited_date, "
      + " grp.consultation_id, pr.visit_type, (pr.reg_date + pr.reg_time) AS reg_date_time, "
      + " prmp.preauth_presc_id, prmp.preauth_status, prmp.resubmit_type, "
      + " icm.insurance_co_id AS primary_insurance_co_id, icms.insurance_co_id AS "
      + "secondary_insurance_co_id, "
      + " icm.insurance_co_name AS primary_insurance_co_name, icms.insurance_co_name AS "
      + "secondary_insurance_co_name, "
      + " tp.tpa_id AS primary_tpa_id, tps.tpa_id AS secondary_tpa_id, "
      + " tp.tpa_name AS primary_tpa_name, tps.tpa_name AS secondary_tpa_name, "
      + " pm.plan_id AS primary_plan_id, pms.plan_id AS secondary_plan_id, "
      + " pm.plan_name AS primary_plan_name, pms.plan_name AS secondary_plan_name, "
      + " pip.plan_type_id :: text AS primary_category_id , pips.plan_type_id :: text AS "
      + " secondary_category_id , "
      + " cat.category_name AS primary_category_name, cats.category_name AS "
      + " secondary_category_name, "
      + " pip.priority AS priority, tp.pre_auth_mode as primary_pre_auth_mode, tps.pre_auth_mode "
      + " as secondary_pre_auth_mode "
      + " FROM (SELECT preauth_presc_id, consultation_id, visit_id "
      + " FROM preauth_prescription_activities WHERE status != 'X' "
      + " GROUP by preauth_presc_id, consultation_id, visit_id ) as grp"
      + " JOIN patient_registration pr ON (grp.visit_id = pr.patient_id)"
      + " JOIN patient_details pd ON (pr.mr_no = pd.mr_no) "
      + " JOIN preauth_prescription prmp ON (grp.preauth_presc_id = prmp.preauth_presc_id ) "
      + " LEFT JOIN doctor_consultation dc ON (dc.consultation_id = grp.consultation_id) "
      + " LEFT JOIN doctors d ON (d.doctor_id = pr.doctor) "
      + " LEFT JOIN (SELECT referal_name,referal_no FROM referral UNION "
      + " SELECT doctor_name,doctor_id FROM doctors ) AS ref ON (ref.referal_no = "
      + " pr.reference_docto_id) "
      + " LEFT JOIN patient_insurance_plans pip ON (prmp.preauth_payer_id = pip.insurance_co AND "
      + " pip.patient_id = pr.patient_id) "
      + " LEFT JOIN patient_insurance_plans pips ON (prmp.preauth_payer_id = pips.insurance_co "
      + "AND pips.patient_id = pr.patient_id) "
      + " LEFT JOIN insurance_plan_main pm ON (pm.plan_id = pip.plan_id) "
      + " LEFT JOIN insurance_plan_main pms ON (pms.plan_id = pips.plan_id) "
      + " LEFT JOIN tpa_master tp ON (tp.tpa_id = pip.sponsor_id)  "
      + " LEFT JOIN tpa_master tps ON (tps.tpa_id = pips.sponsor_id) "
      + " LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = pip.insurance_co) "
      + " LEFT JOIN insurance_company_master icms ON (icms.insurance_co_id = pips.insurance_co)  "
      + " LEFT JOIN insurance_category_master cat ON (cat.category_id = pip.plan_type_id) "
      + " LEFT JOIN insurance_category_master cats ON (cats.category_id = pips.plan_type_id) "
      + " LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)  "
      + " WHERE ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )) as list ";

  /**
   * Search E auth prescription list.
   *
   * @param filter the filter
   * @param listing the listing
   * @return the paged list
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList searchEAuthPrescriptionList(Map filter, Map listing)
      throws SQLException, ParseException {

    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      Map<Object, Object> temp = new HashMap<Object, Object>();
      temp.putAll(filter);

      String insuranceCoId = null;
      String[] categoryIds = null;
      String[] preAuthModes = null;
      String tpaId = null;
      String planId = null;

      if (temp.containsKey("insurance_co_id")
          && ((String[]) temp.get("insurance_co_id"))[0] != null) {
        insuranceCoId = ((String[]) temp.get("insurance_co_id"))[0];
        temp.remove("insurance_co_id");
      }
      if (temp.containsKey("category_id")
          && (((String[]) temp.get("category_id")) != null
          && !((String[]) temp.get("category_id"))[0]
          .isEmpty())) {
        categoryIds = ((String[]) temp.get("category_id"));
        temp.remove("category_id");
      }

      if (temp.containsKey("pre_auth_mode")
          && (((String[]) temp.get("pre_auth_mode")) != null
          && !((String[]) temp.get("pre_auth_mode"))[0]
          .isEmpty())) {
        preAuthModes = ((String[]) temp.get("pre_auth_mode"));
        temp.remove("pre_auth_mode");
      }

      if (temp.containsKey("tpa_id")
          && ((String[]) temp.get("tpa_id"))[0] != null) {
        tpaId = ((String[]) temp.get("tpa_id"))[0];
        temp.remove("tpa_id");
      }
      if (filter.containsKey("plan_id")
          && ((String[]) temp.get("plan_id"))[0] != null) {
        planId = ((String[]) temp.get("plan_id"))[0];
        temp.remove("plan_id");
      }

      SearchQueryBuilder qb = new SearchQueryBuilder(con,
          SELECT_EAUTH_PRESCRIPTION_FIELDS,
          SELECT_EAUTH_PRESCRIPTION_COUNT,
          SELECT_EAUTH_PRESCRIPTION_TABLES, listing);

      qb.addFilterFromParamMap(temp);
      if (insuranceCoId != null && !insuranceCoId.equals("")) {
        qb.appendToQuery("( primary_insurance_co_id = '" + insuranceCoId
            + "' OR " + "secondary_insurance_co_id = '"
            + insuranceCoId + "' )");
      }
      if (categoryIds != null) {
        String catStr = null;
        Boolean first = true;
        for (String categoryId : categoryIds) {
          if (categoryId == null || categoryId.equals("")) {
            continue;
          }
          if (first) {
            catStr = "'" + categoryId + "'";
          } else {
            catStr += ", '" + categoryId + "'";
          }
          first = false;
        }
        qb.appendToQuery(" ( primary_category_id IN (" + catStr
            + ") OR secondary_category_id IN (" + catStr + ") )");
      }

      if (preAuthModes != null) {
        String preAuthStr = null;
        Boolean first = true;
        for (String preauthMode : preAuthModes) {
          if (preauthMode == null || preauthMode.equals("")) {
            continue;
          }
          if (first) {
            preAuthStr = "'" + preauthMode + "'";
          } else {
            preAuthStr += ", '" + preauthMode + "'";
          }
          first = false;
        }
        qb.appendToQuery(" ( primary_pre_auth_mode IN (" + preAuthStr
            + ") OR " + "secondary_pre_auth_mode IN (" + preAuthStr
            + ") )");
      }

      if (tpaId != null && !tpaId.equals("")) {
        qb.appendToQuery(" ( primary_tpa_id = '" + tpaId
            + "' OR secondary_tpa_id = '" + tpaId + "' )");
      }
      if (planId != null && !planId.equals("")) {
        qb.appendToQuery("( primary_plan_id = '" + planId
            + "' OR  secondary_plan_id = '" + planId + "' )");
      }

      int centerId = RequestContext.getCenterId();
      if (centerId != 0) {
        qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=",
            centerId);
      }
      qb.addSecondarySort("preauth_presc_id", true);
      qb.build();

      PagedList list = qb.getMappedPagedList();

      qb.close();
      con.close();

      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * The get eauth prescription.
   */
  private static final String GET_EAUTH_PRESCRIPTION = " SELECT "
      + " pr.mr_no, pr.patient_id, pr.visit_type, pr.op_type, pr.complaint, "
      + " CASE WHEN pd.government_identifier IS NULL OR pd.government_identifier = '' THEN "
      + " COALESCE(gim.identifier_type,'') ELSE  pd.government_identifier END AS "
      + "emirates_id_number, " + " ppd.member_id, ppd.policy_number, "
      + " COALESCE (hta.tpa_code, '@'||tm.tpa_name) AS " + "receiver_id, "
      + " COALESCE (hic.insurance_co_code,'@'||icm.insurance_co_name) AS payer_id, "
      + " prad.center_id as preauth_center_id, prad.start_date as validity_start_date,"
      + " prad.end_date as validity_end_date, preauth_enc_end_datetime, "
      + " pr.reg_date, pr.reg_time, "
      + " to_char((pr.reg_date||' '||pr.reg_time) :: timestamp without time zone, 'dd/MM/yyyy "
      + "hh24:mi') as start_date, "
      + " to_char(preauth_enc_end_datetime::timestamp, 'dd/MM/yyyy hh24:mi') AS end_date, "
      + " hcm.hospital_center_service_reg_no AS provider_id, "
      + " COALESCE(pprqresub.approval_status, pprr.approval_status,"
      + " pprq.approval_status, prad.approval_status) AS "
      + "approval_status, "
      + " pr.encounter_type, etc.encounter_type_desc, pr.status as patstatus, pr.center_id, "
      + " CASE WHEN (pr.op_type != 'O') THEN d.doctor_license_number ELSE ref"
      + ".doctor_license_number END AS doctor_license_number, "
      + " CASE WHEN (pr.op_type != 'O') THEN d.doctor_name ELSE ref.referal_name END AS doctor, "
      + " CASE WHEN (pr.op_type != 'O') THEN d.doctor_id ELSE ref.referal_no END AS doctor_id, "
      + " CASE WHEN (pr.op_type != 'O') THEN 'Doctor' ELSE ref.doctor_type END AS doctor_type, "
      + " CASE WHEN (pr.visit_type = 'i' OR pr.op_type = 'O') THEN (pr.reg_date + pr.reg_time) "
      + "ELSE dc.visited_date END AS visited_date, "
      + " CASE WHEN (pr.visit_type = 'i' OR pr.op_type = 'O') THEN to_char(pr.reg_date, "
      + "'dd/MM/yyyy') "
      + " ELSE to_char(dc.visited_date::date, 'dd/MM/yyyy') END AS date_ordered,"
      + " grp.consultation_id, prmp.resubmit_request_id_with_correction, "
      + "prmp.preauth_resubmit_request_id, "
      + " prmp.preauth_presc_id, prmp.preauth_status, prmp.preauth_request_id, prmp.comments, "
      + " prmp.resubmit_type, prad.is_resubmit, prad.preauth_request_type, prad.file_name,"
      + " COALESCE(pprqresub.preauth_id_payer,pprr.preauth_id_payer, "
      + " pprq.preauth_id_payer, prad.preauth_id_payer) AS "
      + "preauth_id_payer, "
      + " prad.preauth_receiver_id, prad.preauth_sender_id, "
      + " pip.insurance_co AS insurance_co_id, pip.sponsor_id AS tpa_id , "
      + " pip.plan_id, pr.category_id :: text, icm.insurance_co_name, tm.tpa_name, pm.plan_name, "
      + "cat.category_name," + " pr.org_id, od.org_name, pip.priority, "
      + " COALESCE(pprqresub.approval_comments,pprr.approval_comments, "
      + " pprq.approval_comments,prad.approval_comments) as "
      + "approval_comments "
      + " FROM (SELECT preauth_presc_id, consultation_id, visit_id "
      + " FROM preauth_prescription_activities GROUP by preauth_presc_id, consultation_id, "
      + " visit_id ) as grp "
      + " JOIN patient_registration pr ON (grp.visit_id = pr.patient_id) "
      + " JOIN patient_details pd ON (pr.mr_no = pd.mr_no) "
      + " JOIN preauth_prescription prmp ON (grp.preauth_presc_id = prmp.preauth_presc_id ) "
      + " LEFT JOIN doctor_consultation dc ON (dc.consultation_id = grp.consultation_id) "
      + " LEFT JOIN doctors d ON (d.doctor_id = pr.doctor) "
      + " LEFT JOIN ( SELECT 'Referal' AS doctor_type,referal_no, referal_name, clinician_id AS "
      + "doctor_license_number"
      + " FROM referral UNION SELECT 'Doctor' AS doctor_type,doctor_id, doctor_name, "
      + "doctor_license_number FROM doctors) AS ref"
      + " ON (ref.referal_no = pr.reference_docto_id)"
      + " LEFT JOIN patient_insurance_plans pip ON (pr.patient_id = pip.patient_id) "
      + " LEFT JOIN insurance_plan_main pm ON (pm.plan_id = pip.plan_id) "
      + " LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pip.patient_policy_id) "
      + " LEFT JOIN tpa_master tm ON (tm.tpa_id = pip.sponsor_id) "
      + " LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = pip.insurance_co ) "
      + " LEFT JOIN insurance_category_master cat ON (cat.category_id = pr.category_id) "
      + " LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id) "
      + " LEFT JOIN encounter_type_codes etc ON (etc.encounter_type_id = encounter_type) "
      + " LEFT JOIN preauth_request_approval_details prad on (prad.preauth_request_id ="
      + " prmp.preauth_request_id) "
      + " LEFT JOIN preauth_prescription_request pprq on (pprq.preauth_request_id = "
      + "prmp.preauth_request_id) "
      + " LEFT JOIN preauth_prescription_request pprqresub on (pprqresub.preauth_request_id = "
      + "prmp.preauth_resubmit_request_id) "
      + " LEFT JOIN hospital_center_master hcm ON (hcm.center_id = prad.center_id) "
      + " LEFT JOIN organization_details od ON (pr.org_id = od.org_id)"
      + " LEFT JOIN ha_tpa_code hta ON(hta.tpa_id=tm.tpa_id AND hta.health_authority = hcm"
      + ".health_authority) "
      + " LEFT JOIN ha_ins_company_code hic ON(hic.insurance_co_id=icm.insurance_co_id AND hic"
      + ".health_authority = hcm.health_authority) "
      + " LEFT JOIN LATERAL(SELECT preauth_id_payer,approval_comments,approval_status,"
      + " preauth_presc_id FROM preauth_prescription_request WHERE preauth_presc_req_id "
      + " IN(SELECT MAX(preauth_presc_req_id) FROM preauth_prescription_request "
      + " WHERE file_id IS NOT NULL and file_id!='' and preauth_presc_id=?)) pprr "
      + " on pprr.preauth_presc_id=pprq.preauth_presc_id "
      + " WHERE ";

  /**
   * Gets the e auth presc.
   *
   * @param preauthPrescId the preauth presc id
   * @param insuranceCoId  the insurance co id
   * @return the e auth presc
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getEAuthPresc(int preauthPrescId, String insuranceCoId) throws SQLException {

    return getEAuthPresc(preauthPrescId, insuranceCoId, true);
  }
  
  /**
   * Gets the e auth presc.
   *
   * @param preauthPrescId the preauth presc id
   * @param insuranceCoId  the insurance co id
   * @return the e auth presc
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getEAuthPresc(int preauthPrescId, String insuranceCoId, 
        boolean checkConfidentiality) throws SQLException {

    return DataBaseUtil.queryToDynaBean(
        GET_EAUTH_PRESCRIPTION + (checkConfidentiality
          ? "( patient_confidentiality_check(pd.patient_group,pd.mr_no) ) AND " : "" )
        + " prmp.preauth_presc_id = ? AND pip.insurance_co = ? ",
        new Object[] {preauthPrescId, preauthPrescId, insuranceCoId});
  }

  /**
   * The get patient prescription.
   */
  private static final String GET_PATIENT_PRESCRIPTION = " SELECT "
      + " pr.mr_no, pr.patient_id, pr.visit_type, pr.op_type, pr.complaint, "
      + " CASE WHEN pd.government_identifier IS NULL OR pd.government_identifier = '' THEN "
      + " COALESCE(gim.identifier_type,'') ELSE  pd.government_identifier END AS "
      + "emirates_id_number, " + " ppd.member_id, ppd.policy_number, "
      + " COALESCE (hta.tpa_code, '@'||tm.tpa_name) AS " + "receiver_id, "
      + " COALESCE (hic.insurance_co_code,'@'||icm.insurance_co_name) AS payer_id,"
      + " pr.center_id AS preauth_center_id, '' AS provider_id, pr.encounter_type, etc"
      + ".encounter_type_desc,"
      + " pr.status as patstatus, pr.center_id, null as preauth_enc_end_datetime,"
      + " pr.reg_date, pr.reg_time, '' AS validity_start_date, '' AS validity_end_date, "
      + " 0 AS preauth_presc_id, '' AS preauth_status,  "
      + " NULL AS preauth_request_id, '' AS " + "comments,"
      + " '' AS resubmit_type, 'N' AS is_resubmit, "
      + " pip.insurance_co AS insurance_co_id, pip.sponsor_id AS tpa_id , "
      + " pip.plan_id, pr.category_id :: text,  "
      + " icm.insurance_co_name, tm.tpa_name, pm.plan_name, cat.category_name, "
      + " pr.org_id,  "
      + " CASE WHEN (pr.op_type != 'O') THEN d.doctor_license_number ELSE ref"
      + ".doctor_license_number END AS doctor_license_number,"
      + " CASE WHEN (pr.op_type != 'O') THEN d.doctor_name ELSE ref.referal_name END AS doctor,"
      + " CASE WHEN (pr.op_type != 'O') THEN d.doctor_id ELSE ref.referal_no END AS doctor_id,"
      + " CASE WHEN (pr.op_type != 'O') THEN 'Doctor' ELSE ref.doctor_type END AS doctor_type,"
      + " CASE WHEN (pr.visit_type = 'i' OR pr.op_type = 'O') THEN (pr.reg_date + pr.reg_time) "
      + "ELSE dc.visited_date END AS visited_date,"
      + " CASE WHEN (pr.visit_type = 'i' OR pr.op_type = 'O') THEN to_char(pr.reg_date, "
      + "'dd/MM/yyyy') "
      + " ELSE to_char(dc.visited_date::date, 'dd/MM/yyyy') END AS date_ordered,"
      + " consultation_id, pip.priority "
      + " FROM patient_registration pr "
      + " LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no) "
      + " LEFT JOIN doctor_consultation dc ON (dc.patient_id = pr.patient_id AND dc.doctor_name ="
      + " pr.doctor) "
      + " LEFT JOIN ( SELECT 'Referal' AS doctor_type,referal_no, referal_name, clinician_id AS "
      + "doctor_license_number FROM referral UNION"
      + " SELECT 'Doctor' AS doctor_type,doctor_id, doctor_name, doctor_license_number FROM"
      + " doctors ) AS ref ON (ref.referal_no = pr.reference_docto_id) "
      + " LEFT JOIN doctors d on (dc.doctor_name = d.doctor_id) "
      + " LEFT JOIN patient_insurance_plans pip ON (pr.patient_id = pip.patient_id) "
      + " LEFT JOIN tpa_master tm ON (tm.tpa_id = pip.sponsor_id)"
      + " LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = pip.insurance_co)"
      + " LEFT JOIN insurance_category_master cat ON (cat.category_id = pr.category_id)"
      + " LEFT JOIN insurance_plan_main pm ON (pm.plan_id = pip.plan_id)"
      + " LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id) "
      + " LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pip.patient_policy_id) "
      + " LEFT JOIN encounter_type_codes etc ON (etc.encounter_type_id = encounter_type) "
      + " LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id) "
      + " LEFT JOIN ha_tpa_code hta ON(hta.tpa_id=tm.tpa_id AND hta.health_authority = hcm"
      + ".health_authority)"
      + " LEFT JOIN ha_ins_company_code hic ON(hic.insurance_co_id=icm.insurance_co_id AND hic"
      + ".health_authority = hcm.health_authority)";

  /**
   * Gets the e auth patient.
   *
   * @param patientId   the patient id
   * @param insuranceCo the insurance co
   * @param priority    the priority
   * @return the e auth patient
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getEAuthPatient(String patientId, String insuranceCo, int priority)
      throws SQLException {
    return DataBaseUtil.queryToDynaBean(GET_PATIENT_PRESCRIPTION + " WHERE pr.patient_id = ? "
       + " AND icm.insurance_co_id = ? AND priority = ? ORDER BY priority",
        new Object[] {patientId, insuranceCo, priority});
  }

  /**
   * The Constant GET_EAUTH_PRESCRIPTION_ACTIVITIES.
   */
  private static final String GET_EAUTH_PRESCRIPTION_ACTIVITIES = " SELECT "
      + " CASE WHEN prp.preauth_act_type = 'DIA' THEN tm.test_name "
      + " WHEN prp.preauth_act_type = 'SER' THEN sm.service_name "
      + " WHEN prp.preauth_act_type = 'OPE' THEN COALESCE(om.operation_name,"
      + " dc.doctor_name,thm.theatre_name) "
      + " WHEN prp.preauth_act_type = 'DOC' THEN dc.doctor_name "
      + " WHEN prp.preauth_act_type = 'ITE' THEN sid.medicine_name END AS preauth_act_name,"
      + " CASE WHEN prp.preauth_act_type = 'DIA' THEN tm.prior_auth_required "
      + " WHEN prp.preauth_act_type = 'SER' THEN sm.prior_auth_required "
      + " WHEN prp.preauth_act_type = 'OPE' THEN om.prior_auth_required "
      + " WHEN prp.preauth_act_type = 'DOC' THEN 'A' "
      + " WHEN prp.preauth_act_type = 'ITE' THEN sid.prior_auth_required END AS preauth_req_master,"
      + " 'item_master' as master, prp.ispackage, prp.status, prp.added_to_bill,"
      + " dd.category as test_category, prmp.preauth_status, prp.preauth_act_item_id, "
      + " prp.preauth_act_id, prp.consultation_id, prp.patient_pres_id,"
      + " prp.preauth_act_type, prp.prescribed_date, prp.act_qty, prp.act_code_type,"
      + " prp.patient_share, prp.claim_net_amount, prp.claim_net_approved_amount, prp.preauth_id, "
      + " prp.act_code, prp.tooth_unv_number, prp.tooth_fdi_number, sm.tooth_num_required, "
      + " prp.activity_due_date, prp.doc_cons_type,"
      + " prp.preauth_act_item_remarks, prp.preauth_required, prp.preauth_presc_id, "
      + " prp.preauth_act_status, prp.preauth_id, prp.preauth_mode, pam.prior_auth_mode_name, "
      + " prp.denial_code, prp.denial_remarks, prp.patient_share,"
      + " prp.amount, prp.claim_net_amount, prp.claim_net_approved_amount, prp.rate, prp.discount,"
      + " to_char(prp.prescribed_date::timestamp, 'dd/MM/yyyy hh24:mi') AS "
      + "activity_prescribed_date,"
      + " msct.haad_code, idc.status AS denial_code_status, idc.type AS denial_code_type, "
      + " ppad.file_name,  length(ppad.attachment) as attachment_size, "
      + " COALESCE((SELECT code_desc FROM (SELECT code,code_type,code_desc "
      + " FROM " + "mrd_codes_master) AS mcm "
      + " WHERE (mcm.code=prp.act_code AND mcm.code_type=prp.act_code_type ) LIMIT 1 ), '') AS "
      + "code_master_desc " + " , prp.rem_qty,prp.approved_qty,prp.rem_approved_qty "
      + " FROM preauth_prescription_activities prp "
      + " JOIN preauth_prescription prmp ON (prmp.preauth_presc_id = prp.preauth_presc_id)"
      + " JOIN patient_registration pr ON (pr.patient_id = prp.visit_id)"
      + " JOIN hospital_center_master hcm ON (pr.center_id = hcm.center_id)"
      + " JOIN health_authority_preferences hap ON (hap.health_authority=hcm.health_authority)"
      + " LEFT JOIN services sm ON (sm.service_id = prp.preauth_act_item_id)"
      + " LEFT JOIN diagnostics tm ON (tm.test_id = prp.preauth_act_item_id)"
      + " LEFT JOIN diagnostics_departments dd ON (dd.ddept_id=tm.ddept_id)"
      + " LEFT JOIN operation_master om ON (om.op_id=prp.preauth_act_item_id)"
      + " LEFT JOIN doctors dc ON (dc.doctor_id=prp.preauth_act_item_id)"
      + " LEFT JOIN store_item_details sid ON (sid.medicine_id::text = prp.preauth_act_item_id)"
      + " LEFT JOIN theatre_master thm ON thm.theatre_id=prp.preauth_act_item_id "
      + " LEFT JOIN mrd_supported_code_types msct ON (msct.code_type = prp.act_code_type)"
      + " LEFT JOIN insurance_denial_codes idc ON (idc.denial_code = prp.denial_code)"
      + " LEFT JOIN prior_auth_modes pam ON (pam.prior_auth_mode_id = prp.preauth_mode)"
      + " LEFT JOIN preauth_prescription_activities_docs ppad ON(ppad.preauth_act_id ="
      + " prp.preauth_act_id) WHERE prmp.preauth_presc_id=? AND prp.status != 'X' ";

  /**
   * Gets the e auth prescription activities.
   *
   * @param preauthActId the preauth act id
   * @return the e auth prescription activities
   * @throws SQLException the SQL exception
   */
  public static List getEAuthPrescriptionActivities(int preauthActId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String showAllPreAuthPref = (String) GenericPreferencesDAO.getAllPrefs()
        .get("show_prior_auth_presc");
    String additionalWhereCondition = "";
    if ("P".equals(showAllPreAuthPref)) {
      additionalWhereCondition = " AND prp.preauth_required='Y' ";
    }
    String query = GET_EAUTH_PRESCRIPTION_ACTIVITIES + additionalWhereCondition
        + " ORDER BY prp.preauth_act_id";
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(query);
      ps.setInt(1, preauthActId);
      List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(ps);
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Find E auth activities.
   *
   * @param preauthPrescId the preauth presc id
   * @param isResubmission the is resubmission
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> findEAuthActivities(Integer preauthPrescId, boolean isResubmission)
      throws SQLException {
    // While Resubmission of prescription, and Resubmission type is Internal complaint then,
    // only those activites which are denied should go in Prior Request XML.
    if (isResubmission) {
      return DataBaseUtil.queryToDynaList(
          GET_EAUTH_PRESCRIPTION_ACTIVITIES + " AND prp"
              + ".preauth_required = 'Y' AND prp.preauth_act_status = 'D' ",
          preauthPrescId);
    } else {
      return DataBaseUtil
          .queryToDynaList(
              GET_EAUTH_PRESCRIPTION_ACTIVITIES + " AND prp"
                  + ".preauth_required = 'Y' ",
              preauthPrescId);
    }
  }

  /**
   * The Constant EAUTH_OBSERVATIONS.
   */
  private static final String EAUTH_OBSERVATIONS = " SELECT pao.preauth_act_id, pao.obs_type, pao"
      + ".code, pao.value, pao.value_type, code_desc "
      + " FROM preauth_activities_observations pao "
      + " LEFT JOIN mrd_codes_master mcm ON (mcm.code_type=pao.obs_type AND mcm.code=pao.code) "
      + " WHERE pao.preauth_act_id = ? " + " ORDER BY act_obs_id ";

  /**
   * Gets the e auth act observations.
   *
   * @param eauthActId      the e auth act id
   * @param eclaimXMLSchema the eclaim XML schema
   * @return the e auth act observations
   * @throws SQLException the SQL exception
   */
  public List getEAuthActObservations(int eauthActId, String eclaimXMLSchema) throws SQLException {
    /*if (!eclaimXMLSchema.equals("DHA")) {
     return DataBaseUtil.queryToDynaList(EAUTH_OBSERVATIONS_WITHOUT_PRESENTING_COMPLAINT,
      eAuthActId);
     }else {
      return DataBaseUtil.queryToDynaList(EAUTH_OBSERVATIONS, eAuthActId);
    }*/

    return DataBaseUtil.queryToDynaList(EAUTH_OBSERVATIONS, eauthActId);
  }

  /**
   * The get eauth hospital center header details.
   */
  private static String GET_EAUTH_HOSPITAL_CENTER_HEADER_DETAILS = "SELECT hcm.center_id, "
      + "center_name, "
      + " preauth_sender_id AS provider_id, preauth_receiver_id AS receiver_id, "
      + " to_char(request_date::timestamp, 'dd/MM/yyyy hh24:mi') AS transaction_date, "
      + " 1 as e_record_count, 'Y' AS testing, '' as health_authority, '' AS disposition_flag "
      + " FROM preauth_request_approval_details prad "
      + " JOIN hospital_center_master hcm ON (hcm.center_id = prad.center_id) "
      + " WHERE preauth_request_id = ? ";

  /**
   * Gets the e auth header fields.
   *
   * @param preauthRequestId the preauth request id
   * @return the e auth header fields
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getEAuthHeaderFields(String preauthRequestId)
      throws SQLException {
    BasicDynaBean preauthReqAppBean = new GenericDAO(
        "preauth_request_approval_details")
        .findByKey("preauth_request_id", preauthRequestId);

    if (preauthReqAppBean != null) {
      if (preauthReqAppBean.get("center_id") != null) {
        return DataBaseUtil.queryToDynaBean(GET_EAUTH_HOSPITAL_CENTER_HEADER_DETAILS,
            preauthRequestId);
      }
    }
    return null;
  }

  /**
   * The Constant GET_LATEST_EAUTH_PRESC_ID.
   */
  private static final String GET_LATEST_EAUTH_PRESC_ID = " SELECT prp.preauth_presc_id FROM "
      + "preauth_prescription_activities ppa "
      + " JOIN preauth_prescription prp USING(preauth_presc_id) "
      + " WHERE prp.preauth_status = 'O' AND (preauth_request_id IS NULL OR preauth_request_id = "
      + "'') "
      + " AND ppa.consultation_id = ? AND prp.preauth_payer_id = ?  ORDER BY preauth_presc_id "
      + "DESC LIMIT 1";

  /**
   * Gets the latest E auth presc id.
   *
   * @param consultationId the consultation id
   * @param insuranceCoId  the insurance co id
   * @return the latest E auth presc id
   * @throws SQLException the SQL exception
   */
  public int getLatestEAuthPrescId(int consultationId, String insuranceCoId) throws SQLException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      return getLatestEAuthPrescId(con, consultationId, insuranceCoId);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the latest E auth presc id.
   *
   * @param con             the con
   * @param consultationId the consultation id
   * @param insuranceCoId the insurance co id
   * @return the latest E auth presc id
   * @throws SQLException the SQL exception
   */
  public int getLatestEAuthPrescId(Connection con, int consultationId, String insuranceCoId)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_LATEST_EAUTH_PRESC_ID);
      ps.setInt(1, consultationId);
      ps.setString(2, insuranceCoId);
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * The Constant GET_LATEST_EAUTH_VISIT_PRESC_ID.
   */
  private static final String GET_LATEST_EAUTH_VISIT_PRESC_ID = " SELECT prp.preauth_presc_id "
      + "FROM preauth_prescription_activities ppa "
      + " JOIN preauth_prescription prp USING(preauth_presc_id) "
      + " WHERE prp.preauth_status = 'O' AND (preauth_request_id IS NULL OR preauth_request_id = "
      + "'') "
      + " AND ppa.visit_id = ? AND prp.preauth_payer_id = ? ORDER BY preauth_presc_id DESC LIMIT 1";

  /**
   * Gets the latest E auth presc id.
   *
   * @param visitId the visit id
   * @param insuranceCoId the insurance co id
   * @return the latest E auth presc id
   * @throws SQLException the SQL exception
   */
  public int getLatestEAuthPrescId(String visitId, String insuranceCoId) throws SQLException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      return getLatestEAuthPrescId(con, visitId, insuranceCoId);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the latest E auth presc id.
   *
   * @param con the con
   * @param visitId the visit id
   * @param insuranceCoId the insurance co id
   * @return the latest E auth presc id
   * @throws SQLException the SQL exception
   */
  public int getLatestEAuthPrescId(Connection con, String visitId, String insuranceCoId)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_LATEST_EAUTH_VISIT_PRESC_ID);
      ps.setString(1, visitId);
      ps.setString(2, insuranceCoId);
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Gets the e auth presc sequence id.
   *
   * @param con                the con
   * @param userName           the user name
   * @param preauthPrescId     the preauth presc id
   * @param consId             the cons id
   * @param visitId            the visit id
   * @param insuranceCompanyId the insurance company id
   * @return the e auth presc sequence id
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public int getEAuthPrescSequenceId(Connection con, String userName,
                                     int preauthPrescId, int consId, String visitId,
                                     String insuranceCompanyId) throws SQLException, IOException {

    if (consId == 0) {
      // OutSide Patients
      if (preauthPrescId == 0) {
        preauthPrescId = new EAuthPrescriptionDAO()
            .getLatestEAuthPrescId(visitId, insuranceCompanyId);
      }

      if (preauthPrescId == 0) {
        preauthPrescId = new EAuthPrescriptionDAO()
            .getLatestEAuthPrescId(con, visitId,
                insuranceCompanyId);
      }
    } else {
      // OP Patients
      if (preauthPrescId == 0) {
        preauthPrescId = new EAuthPrescriptionDAO()
            .getLatestEAuthPrescId(consId, insuranceCompanyId);
      }

      if (preauthPrescId == 0) {
        preauthPrescId = new EAuthPrescriptionDAO()
            .getLatestEAuthPrescId(con, consId, insuranceCompanyId);
      }
    }
    if (preauthPrescId == 0) {

      preauthPrescId = getNextSequence();
      BasicDynaBean eauthPrescBean = getBean();
      eauthPrescBean.set("preauth_presc_id", preauthPrescId);
      eauthPrescBean.set("preauth_cons_id", consId);
      eauthPrescBean.set("username", userName);
      eauthPrescBean.set("preauth_status", "O");
      eauthPrescBean.set("preauth_payer_id", insuranceCompanyId);

      if (!insert(con, eauthPrescBean)) {
        return -1;
      }
    }
    return preauthPrescId;
  }

  /**
   * Gets the preauth prescription bean.
   *
   * @param preauthPrescId the preauth presc id
   * @return the preauth prescription bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getPreauthPrescriptionBean(int preauthPrescId)
      throws SQLException {
    List<String> columns = new ArrayList<String>();
    columns.add("preauth_presc_id");
    columns.add("preauth_request_id");
    columns.add("preauth_status");
    columns.add("preauth_cons_id");
    columns.add("resubmit_type");
    columns.add("resubmit_request_id_with_correction");
    columns.add("preauth_resubmit_request_id");
    columns.add("preauth_payer_id");

    Map<String, Object> key = new HashMap<String, Object>();
    key.put("preauth_presc_id", preauthPrescId);
    BasicDynaBean preauthbean = findByKey(columns, key);
    return preauthbean;
  }

  /**
   * Gets the preauth prescription bean.
   *
   * @param eauthRequestId the e auth request id
   * @return the preauth prescription bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getPreauthPrescriptionBean(String eauthRequestId)
      throws SQLException {
    List<String> columns = new ArrayList<String>();
    columns.add("preauth_presc_id");
    columns.add("preauth_request_id");
    columns.add("preauth_status");
    columns.add("preauth_cons_id");
    columns.add("resubmit_type");
    columns.add("resubmit_request_id_with_correction");
    columns.add("preauth_resubmit_request_id");
    columns.add("preauth_payer_id");

    Map<String, Object> key = new HashMap<String, Object>();
    key.put("preauth_request_id", eauthRequestId);
    BasicDynaBean preauthbean = findByKey(columns, key);
    return preauthbean;
  }

  /**
   * Gets the item rate code charge.
   *
   * @param itemType   the item type
   * @param itemId     the item id
   * @param itemQty    the item qty
   * @param visitId    the visit id
   * @param chargeType the charge type
   * @return the item rate code charge
   * @throws SQLException the SQL exception
   */
  public ChargeDTO getItemRateCodeCharge(String itemType, String itemId,
                                         BigDecimal itemQty, String visitId, String chargeType)
      throws SQLException {

    ChargeDTO itemCharge = null;

    BasicDynaBean visitBean = new VisitDetailsDAO().findByKey("patient_id",
        visitId);
    String visitType = (String) visitBean.get("visit_type");
    String bedType = (String) visitBean.get("bed_type");
    String orgId = (String) visitBean.get("org_id");
    Integer planId = (Integer) visitBean.get("plan_id");
    Integer centerId = (Integer) visitBean.get("center_id");

    boolean isInsurance = (planId != 0);

    if (itemType.equals("DIA")) {

      BasicDynaBean test = AddTestDAOImpl.getTestDetails(itemId, bedType,
          orgId);

      String testCategory = (String) test.get("category");
      String testName = (String) test.get("test_name");

      String code = (String) test.get("rate_plan_code");
      String codeType = (String) test.get("code_type");
      int serviceSubGroupId = (Integer) test.get("service_sub_group_id");
      int insuranceCategoryId = (Integer) test
          .get("insurance_category_id");
      BigDecimal testRate = (BigDecimal) test.get("charge");
      BigDecimal testDiscount = (BigDecimal) test.get("discount");

      String head = testCategory.equals("DEP_LAB") ? ChargeDTO.CH_DIAG_LAB
          : ChargeDTO.CH_DIAG_RAD;

      itemCharge = new ChargeDTO("DIA", head, testRate, itemQty,
          testDiscount.multiply(itemQty), "",
          (String) test.get("test_id"), testName,
          (String) test.get("ddept_id"), isInsurance, planId,
          serviceSubGroupId, insuranceCategoryId, visitType, visitId,
          false);

      itemCharge.setActRatePlanItemCode(code);
      itemCharge.setCodeType(codeType);
      itemCharge.setAllowRateIncrease(
          (Boolean) test.get("allow_rate_increase"));
      itemCharge.setAllowRateDecrease(
          (Boolean) test.get("allow_rate_decrease"));

    } else if (itemType.equals("SER")) {

      BasicDynaBean service = new MasterServicesDao()
          .getServiceChargeBean(itemId, bedType, orgId);

      String serviceName = (String) service.get("service_name");
      String code = (String) service.get("item_code");
      String codeType = (String) service.get("code_type");
      int serviceSubGroupId = (Integer) service
          .get("service_sub_group_id");
      int insuranceCategoryId = (Integer) service
          .get("insurance_category_id");
      BigDecimal serviceRate = (BigDecimal) service.get("unit_charge");
      BigDecimal serviceDiscount = (BigDecimal) service.get("discount");

      itemCharge = new ChargeDTO("SNP", "SERSNP", serviceRate, itemQty,
          serviceDiscount.multiply(itemQty), "",
          (String) service.get("service_id"), serviceName,
          service.get("serv_dept_id").toString(), isInsurance, planId,
          serviceSubGroupId, insuranceCategoryId, visitType, visitId,
          false);

      itemCharge.setActRatePlanItemCode(code);
      itemCharge.setCodeType(codeType);
      itemCharge.setAllowRateIncrease(
          (Boolean) service.get("allow_rate_increase"));
      itemCharge.setAllowRateDecrease(
          (Boolean) service.get("allow_rate_decrease"));

      // tooth_num_required

    } else if (itemType.equals("ITE")) {
      BasicDynaBean orgBean = OrgMasterDao.getOrgdetailsDynaBean(orgId);
      Integer storeRatePlanId = 0;
      BigDecimal pharDiscPerc = BigDecimal.ZERO;
      
      if (null != orgBean && null != orgBean.get("store_rate_plan_id")) {
        storeRatePlanId = (Integer)orgBean.get("store_rate_plan_id");
      }
      if (null != orgBean && null != orgBean.get("pharmacy_discount_percentage")) {
        pharDiscPerc = (BigDecimal)orgBean.get("pharmacy_discount_percentage");
      }
          
      String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
      BasicDynaBean invItemBean = itemDAO.getInventoryItemBean(Integer.parseInt(itemId), 
          healthAuthority);

      BigDecimal invItemCharge = BigDecimal.ZERO;
      if (storeRatePlanId != 0) {
        BasicDynaBean invItemRate = itemDAO.getInventoryItemRate(
            Integer.parseInt(itemId), storeRatePlanId);
        if (null != invItemRate && null != invItemRate.get("item_rate")) {
          invItemCharge = (BigDecimal) invItemRate.get("item_rate");
        }
      } else if (invItemBean != null && invItemBean.get("item_selling_price") != null) {
        invItemCharge = (BigDecimal) invItemBean.get("item_selling_price");
      }

      BigDecimal invItemDisc = (pharDiscPerc.multiply(itemQty.multiply(invItemCharge)))
          .divide(new BigDecimal(100));

      invItemDisc = ConversionUtils.setScale(invItemDisc);
      
      int insCategoryId = (Integer)invItemBean.get("insurance_category_id");
      String medicineName = (String)invItemBean.get("medicine_name");
      int serviceSubGroupId = (Integer)invItemBean.get("service_sub_group_id");
      itemCharge = new ChargeDTO("SNP", "SERSNP", invItemCharge, itemQty,
          invItemDisc, "",
          itemId, medicineName,
          null, isInsurance, planId,
          serviceSubGroupId, insCategoryId, visitType, visitId,
          false);

      String code = (String)invItemBean.get("item_code");
      String codeType = (String)invItemBean.get("code_type");
      itemCharge.setActRatePlanItemCode(code);
      itemCharge.setCodeType(codeType);
      itemCharge.setAllowRateIncrease(
          true);
      itemCharge.setAllowRateDecrease(
          true);
      
    } else if (itemType.equals("OPE")) {
      BasicDynaBean operBean = new OperationMasterDAO()
          .getOperationChargeBean(itemId, bedType, orgId);

      String operName = (String) operBean.get("operation_name");
      String code = (String) operBean.get("item_code");
      String codeType = (String) operBean.get("code_type");
      int serviceSubGroupId = (Integer) operBean
          .get("service_sub_group_id");
      int insuranceCategoryId = (Integer) operBean
          .get("insurance_category_id");
      BigDecimal sacRate = (BigDecimal) operBean
          .get("surg_asstance_charge");
      BigDecimal sacDiscount = (BigDecimal) operBean
          .get("surg_asst_discount");

      itemCharge = new ChargeDTO("OPE", "SACOPE", sacRate, itemQty,
          sacDiscount.multiply(itemQty), "",
          (String) operBean.get("op_id"), operName,
          (String) operBean.get("dept_id"), isInsurance, planId,
          serviceSubGroupId, insuranceCategoryId, visitType, visitId,
          false);

      itemCharge.setActRatePlanItemCode(code);
      itemCharge.setCodeType(codeType);
      itemCharge.setAllowRateIncrease(
          (Boolean) operBean.get("allow_rate_increase"));
      itemCharge.setAllowRateDecrease(
          (Boolean) operBean.get("allow_rate_decrease"));

    } else if (itemType.equals("DOC")) {

      BasicDynaBean doctor = DoctorMasterDAO.getDoctorCharges(itemId,
          orgId, bedType);
      int consultationId = Integer.parseInt(chargeType);
      BasicDynaBean consTypeBean = new GenericDAO("consultation_types")
          .findByKey("consultation_type_id", consultationId);
      String desc = (String) doctor.get("doctor_name");

      int consTypeId = consTypeBean != null
          ? (Integer) consTypeBean.get("consultation_type_id")
          : 0;
      BasicDynaBean consultationTypeCharge = OrderBO
          .getConsultationCharge(consTypeId, bedType, orgId);

      String docChargeType = consTypeBean != null
          ? (String) consTypeBean.get("doctor_charge_type")
          : null;
      String consultationChargeHead = consTypeBean != null
          ? (String) consTypeBean.get("charge_head")
          : null;

      int serviceSubGroupId = consTypeBean != null
          ? (Integer) consTypeBean.get("service_sub_group_id")
          : 0;
      int insuranceCategoryId = consTypeBean != null
          ? (Integer) consTypeBean.get("insurance_category_id")
          : 0;
      BigDecimal doctorCharge = BigDecimal.ZERO;
      BigDecimal discount = BigDecimal.ZERO;
      String code = null;
      String codeType = null;

      if (docChargeType != null) {
        doctorCharge = (BigDecimal) doctor.get(docChargeType);
        discount = (BigDecimal) doctor.get(docChargeType + "_discount");

        doctorCharge = doctorCharge
            .add((BigDecimal) consultationTypeCharge.get("charge"));
        discount = discount.add(
            (BigDecimal) consultationTypeCharge.get("discount"));
      }
      if (consultationTypeCharge != null) {
        code = consultationTypeCharge.get("item_code") != null
            ? (String) consultationTypeCharge.get("item_code")
            : null;
        codeType = consultationTypeCharge.get("code_type") != null
            ? (String) consultationTypeCharge.get("code_type")
            : null;
      }

      if (consultationChargeHead != null) {
        itemCharge = new ChargeDTO("DOC", consultationChargeHead,
            doctorCharge, itemQty, discount.multiply(itemQty), "",
            (String) doctor.get("doctor_id"), desc,
            (String) doctor.get("dept_id"), isInsurance, planId,
            serviceSubGroupId, insuranceCategoryId, visitType,
            visitId, false);
      } else {
        itemCharge = new ChargeDTO();
        itemCharge.setChargeGroup("DOC");
        itemCharge
            .setActDescriptionId((String) doctor.get("doctor_id"));
        itemCharge.setActDepartmentId((String) doctor.get("dept_id"));
      }
      itemCharge.setActRatePlanItemCode(code);
      itemCharge.setCodeType(codeType);
      itemCharge.setConsultation_type_id(consTypeId);
      if (consTypeBean != null) {
        itemCharge.setAllowRateIncrease(
            (Boolean) consTypeBean.get("allow_rate_increase"));
        itemCharge.setAllowRateDecrease(
            (Boolean) consTypeBean.get("allow_rate_decrease"));
      }
    }
    return itemCharge;
  }

  /**
   * The Constant SEARCH_EAUTH_PRESCRIPTION.
   */
  private static final String SEARCH_EAUTH_PRESCRIPTION = " SELECT * FROM (SELECT prp"
      + ".preauth_request_id, 1 AS record_count, file_name, "
      + " preauth_sender_id, preauth_receiver_id, file_id "
      + " FROM preauth_prescription prp "
      + " JOIN preauth_request_approval_details prad on (prad.preauth_request_id = prp"
      + ".preauth_request_id)" + " ) AS foo "
      + " WHERE file_name = ? AND preauth_sender_id = ? "
      + " AND preauth_receiver_id = ? AND record_count = ? ";

  /**
   * Search E auth presc.
   *
   * @param xmlfileName the xmlfile name
   * @param senderId the sender id
   * @param receiverId the receiver id
   * @param recordCount the record count
   * @return the string
   * @throws SQLException the SQL exception
   */
  public String searchEAuthPresc(String xmlfileName, String senderId, String receiverId,
                                 String recordCount) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String preauthRequestId = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(SEARCH_EAUTH_PRESCRIPTION);
      ps.setString(1, xmlfileName);
      ps.setString(2, receiverId);
      ps.setString(3, senderId);
      ps.setInt(4, recordCount != null ? Integer.parseInt(recordCount) : 0);

      BasicDynaBean preauthbean = DataBaseUtil.queryToDynaBean(ps);
      if (preauthbean != null) {
        preauthRequestId = (String) preauthbean.get("preauth_request_id");
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return preauthRequestId;
  }

  /**
   * Search E auth presc bean.
   *
   * @param xmlfileName the xmlfile name
   * @param senderId the sender id
   * @param receiverId the receiver id
   * @param recordCount the record count
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean searchEAuthPrescBean(String xmlfileName, String senderId, String receiverId,
                                            String recordCount) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(SEARCH_EAUTH_PRESCRIPTION);
      ps.setString(1, xmlfileName);
      ps.setString(2, receiverId);
      ps.setString(3, senderId);
      ps.setInt(4, recordCount != null ? Integer.parseInt(recordCount) : 0);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * The Constant SEARCH_EAUTH_PRESCRIPTION_ID.
   */
  private static final String SEARCH_EAUTH_PRESCRIPTION_ID = " SELECT * FROM "
      + " (SELECT prp.preauth_request_id, 1 AS record_count, file_name, "
      + " preauth_sender_id, preauth_receiver_id, file_id "
      + " FROM preauth_prescription prp "
      + " JOIN preauth_request_approval_details prad on (prad.preauth_request_id = prp"
      + ".preauth_request_id)" + " ) AS foo "
      + " WHERE preauth_request_id = ? AND preauth_sender_id = ? "
      + " AND preauth_receiver_id = ? AND record_count = ? ";

  /**
   * Search E auth presc by ID.
   *
   * @param preReqId the pre req id
   * @param senderId the sender id
   * @param receiverId the receiver id
   * @param recordCount the record count
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean searchEAuthPrescByID(String preReqId, String senderId, String receiverId,
                                            String recordCount) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(SEARCH_EAUTH_PRESCRIPTION_ID);
      ps.setString(1, preReqId);
      ps.setString(2, receiverId);
      ps.setString(3, senderId);
      ps.setInt(4, recordCount != null ? Integer.parseInt(recordCount) : 0);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Update E auth request file id.
   *
   * @param preauthRequestId the preauth request id
   * @param fileId the file id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean updateEAuthRequestFileId(String preauthRequestId, String fileId)
      throws SQLException, IOException {
    boolean success = true;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      BasicDynaBean preauthRequestBean = preauthReqAppDAO.getBean();
      preauthRequestBean.set("preauth_request_id", preauthRequestId);
      preauthRequestBean.set("file_id", fileId);

      int count = preauthReqAppDAO.updateWithName(con, preauthRequestBean.getMap(),
          "preauth_request_id");
      success = (count > 0);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    if (!success) {
      logger.error("Error while updating Prior Auth Request File Id for :"
          + preauthRequestId);
    }
    return success;
  }

  /**
   * The Constant GET_ATTACHMENT_SIZE.
   */
  public static final String GET_ATTACHMENT_SIZE = "SELECT length(attachment) as "
      + "attachment_size "
      + " FROM preauth_prescription WHERE preauth_presc_id = ?";

  /**
   * Gets the file size.
   *
   * @param preauthPrescId the preauth presc id
   * @return the file size
   * @throws SQLException the SQL exception
   */
  public int getFileSize(int preauthPrescId) throws SQLException {
    int size = 0;
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ATTACHMENT_SIZE);
      ps.setInt(1, preauthPrescId);
      rs = ps.executeQuery();
      while (rs.next()) {
        size = rs.getInt(1);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }

    return size;
  }

  /**
   * The Constant GET_ATTACHMENT.
   */
  private static final String GET_ATTACHMENT = " SELECT attachment,attachment_content_type FROM "
      + "preauth_prescription WHERE preauth_presc_id = ?";

  /**
   * Gets the attachment.
   *
   * @param preauthPrescId the preauth presc id
   * @return the attachment
   * @throws SQLException the SQL exception
   */
  public Map getAttachment(int preauthPrescId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ATTACHMENT);
      ps.setInt(1, preauthPrescId);
      rs = ps.executeQuery();
      if (rs.next()) {
        Map map = new HashMap();
        map.put("Content", rs.getBinaryStream(1));
        map.put("Type", rs.getString(2));
        return map;
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  /**
   * The Constant DELETE_ATTACHMENT.
   */
  private static final String DELETE_ATTACHMENT = " UPDATE preauth_prescription set "
      + "attachment='' , attachment_content_type='' WHERE preauth_presc_id=? ";

  /**
   * Delete attachment.
   *
   * @param preauthPrescId the preauth presc id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean deleteAttachment(int preauthPrescId) throws SQLException {
    boolean success = false;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(DELETE_ATTACHMENT);
      ps.setInt(1, preauthPrescId);
      int result = ps.executeUpdate();
      if (result > 0) {
        success = true;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return success;
  }

  /**
   * The Constant UPDATE_ATTACHMENT.
   */
  private static final String UPDATE_ATTACHMENT = " UPDATE preauth_prescription set attachment=?"
      + " , attachment_content_type=? WHERE preauth_presc_id=? ";

  /**
   * Update attachment.
   *
   * @param params the params
   * @param preauthPrescId the preauth presc id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean updateAttachment(Map params, int preauthPrescId) throws SQLException,
      IOException {
    boolean success = false;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(UPDATE_ATTACHMENT);
      InputStream stream = ((InputStream[]) params.get("attachment"))[0];
      ps.setBinaryStream(1, stream, stream.available());
      ps.setString(2, ((String[]) params.get("attachment_content_type"))[0]);
      ps.setInt(3, preauthPrescId);
      int result = ps.executeUpdate();
      if (result > 0) {
        success = true;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return success;
  }

  /**
   * The Constant FIND_DIAGNOSIS.
   */
  public static final String FIND_DIAGNOSIS = " SELECT (CASE WHEN diag_type = 'P' THEN "
      + "'Principal' WHEN diag_type = 'A' THEN 'Admitting' "
      + " WHEN diag_type = 'V' THEN 'Reason For Visit' "
      + " ELSE 'Secondary' END) AS diag_type, md.diag_type as diagnosis_type, "
      + " md.code_type, icd_code, code_desc FROM mrd_diagnosis md "
      + " JOIN mrd_codes_master mcm ON (mcm.code_type = md.code_type AND mcm.code = md.icd_code) "
      + " WHERE visit_id = ?";

  /**
   * Find all diagnosis.
   *
   * @param visitId the visit id
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> findAllDiagnosis(String visitId) throws SQLException {
    /*
     * first check if the diagnosis has been copied to hospital_claim_diagnosis(coder edited
     * diagnosis) if not copied, then bring diagnosis from mrd_diagnosis table
     */
    List<BasicDynaBean> result = DataBaseUtil
        .queryToDynaList(HospitalClaimDiagnosisRepository.FIND_CODER_DIAGNOSIS, visitId);
    if (result != null && !result.isEmpty()) {
      return result;
    } else {
      return DataBaseUtil.queryToDynaList(FIND_DIAGNOSIS, visitId);
    }
  }

  /**
   * Mark E auth presc sent.
   *
   * @param preauthPrescId the preauth presc id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean markEAuthPrescSent(int preauthPrescId) throws SQLException, IOException {
    boolean success = true;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      BasicDynaBean preauthBean = getBean();
      preauthBean.set("preauth_presc_id", preauthPrescId);
      preauthBean.set("preauth_status", "S");
      int count = updateWithName(con, preauthBean.getMap(), "preauth_presc_id");
      success = (count > 0);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  /**
   * Mark E auth presc closed.
   *
   * @param preauthPrescId the preauth presc id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean markEAuthPrescClosed(int preauthPrescId) throws SQLException, IOException {
    boolean success = true;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      BasicDynaBean preauthBean = getBean();
      preauthBean.set("preauth_presc_id", preauthPrescId);
      // Leave the status as Sent, no need to change as Closed for cancelled request.
      //preauthBean.set("preauth_status", "C");
      int count = updateWithName(con, preauthBean.getMap(), "preauth_presc_id");
      success = (count > 0);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  /**
   * The Constant VISIT_PLAN_SPONSORS_LIST.
   */
  public static final String VISIT_PLAN_SPONSORS_LIST = " SELECT pip.patient_id, pip.plan_id, "
      + "pip.insurance_co, pip.sponsor_id, tpa.tpa_name,tpa.sponsor_type,pip.priority,"
      + " ppd.member_id,icam.category_name AS plan_type_name, ipm.plan_name,ipm.plan_exclusions, "
      + "ipm.plan_notes, "
      + " icm.insurance_co_name,icm.insurance_rules_doc_name, od.org_name, cat.category_name "
      + " FROM patient_insurance_plans pip "
      + " JOIN patient_registration pr ON (pip.patient_id = pr.patient_id) "
      + " JOIN insurance_company_master icm ON icm.insurance_co_id = pip.insurance_co "
      + " JOIN insurance_plan_main ipm ON (pip.plan_id = ipm.plan_id) "
      + " JOIN insurance_category_master icam  ON icam.category_id=ipm.category_id "
      + " JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pip.patient_policy_id) "
      + " LEFT JOIN tpa_master tpa ON (tpa.tpa_id = pip.sponsor_id) "
      + " LEFT JOIN organization_details od ON (pr.org_id = od.org_id) "
      + " LEFT JOIN insurance_category_master cat ON (cat.category_id = pr.category_id)  "
      + " JOIN patient_details pd ON (pd.mr_no = pr.mr_no "
      + " AND patient_confidentiality_check(pd.patient_group,pd.mr_no)) "
      + " WHERE pip.patient_id = ? AND pip.plan_id IS NOT NULL order by priority";

  /**
   * Gets the visit plan sponsors details.
   *
   * @param visitId the visit id
   * @return the visit plan sponsors details
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public List<BasicDynaBean> getVisitPlanSponsorsDetails(String visitId) throws SQLException,
      IOException {
    return DataBaseUtil.queryToDynaList(VISIT_PLAN_SPONSORS_LIST, visitId);
  }

  /**
   * Clone prescription.
   *
   * @param preauthPrescId the preauth presc id
   * @return the int
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public int clonePrescription(int preauthPrescId)
      throws SQLException, IOException {
    boolean success = false;
    Connection con = null;
    int newPreauthPrescId = 0;


    txn:
    {
      try {
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);
        newPreauthPrescId = getNextSequence();
        try (PreparedStatement ps = con.prepareStatement(
            " INSERT INTO preauth_prescription(preauth_presc_id, preauth_status, "
                + "preauth_cons_id, username, preauth_payer_id) "
                + " SELECT ?, 'O', preauth_cons_id, username, preauth_payer_id FROM "
                + "preauth_prescription "
                + " WHERE preauth_presc_id=?")) {
          ps.setInt(1, newPreauthPrescId);
          ps.setInt(2, preauthPrescId);
          if (ps.executeUpdate() == 0) {
            break txn;
          }
          String updateAsCloned = "UPDATE preauth_prescription "
              + "SET is_cloned = 'Y' WHERE preauth_presc_id = ?";
          try (PreparedStatement ps2 = con.prepareStatement(updateAsCloned)) {
            ps2.setInt(1, preauthPrescId);
            if (ps2.executeUpdate() == 0) {
              break txn;
            }
          }
        }
        EAuthPrescriptionActivitiesDAO actDAO = new EAuthPrescriptionActivitiesDAO();
        List<BasicDynaBean> actRecords = actDAO.findAllByKey(con, "preauth_presc_id",
            preauthPrescId);

        String observations = "INSERT INTO preauth_activities_observations(preauth_act_id,"
            + " obs_type, code, value, value_type) "
            + " SELECT ?, obs_type, code, value, "
            + "value_type FROM "
            + "preauth_activities_observations WHERE preauth_act_id=?";
        for (BasicDynaBean record : actRecords) {
          BasicDynaBean actBean = actDAO.getBean();
          ConversionUtils.copyBeanToBean(record, actBean); // copy the
          // activities
          // to
          // the
          // new prescription.

          int newPreauthActId = actDAO.getNextSequence();
          actBean.set("preauth_presc_id", newPreauthPrescId);
          actBean.set("preauth_act_id", newPreauthActId);
          actBean.set("preauth_act_status", "O");
          actBean.set("denial_code", "");
          actBean.set("denial_remarks", "");
          actBean.set("preauth_id", "");
          actBean.set("preauth_mode", 0);

          if (!actDAO.insert(con, actBean)) {
            break txn;
          }

          try (PreparedStatement ps = con.prepareStatement(observations)) { // copy the
            // observations.
            ps.setInt(1, newPreauthActId);
            ps.setInt(2, (Integer) record.get("preauth_act_id"));
            ps.executeUpdate();
          }
        }
        success = true;
      } finally {
        DataBaseUtil.commitClose(con, success);
      }
    }
    return success ? newPreauthPrescId : 0;
  }

  /**
   * The Constant GET_ACTIVITY_ATTACHMENT.
   */
  private static final String GET_ACTIVITY_ATTACHMENT = " SELECT attachment,"
      + "attachment_content_type FROM preauth_prescription_activities_docs WHERE preauth_act_id ="
      + " ?";

  /**
   * Gets the activity attachment.
   *
   * @param preauthActivityId the preauth activity id
   * @return the activity attachment
   * @throws SQLException the SQL exception
   */
  public Map getActivityAttachment(int preauthActivityId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ACTIVITY_ATTACHMENT);
      ps.setInt(1, preauthActivityId);
      rs = ps.executeQuery();
      if (rs.next()) {
        Map map = new HashMap();
        map.put("Content", rs.getBinaryStream(1));
        map.put("Type", rs.getString(2));
        return map;
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  /**
   * The Constant GET_PREAUTH_DOCUMENT_LISTS.
   */
  private static final String GET_PREAUTH_DOCUMENT_LISTS = " SELECT ppad.preauth_act_id, "
      + "'File'::text AS obs_type, "
      + " ppad.attachment_extension as code, "
      + " encode(attachment, 'base64') as value, ppad.attachment_extension || ' '  || "
      + "'text' as "
      + "value_type, " + " ' ' as  code_desc "
      + " FROM preauth_prescription_activities_docs ppad "
      + " WHERE ppad.preauth_act_id = ? ";


  /**
   * Gets the pre auth document lists.
   *
   * @param eauthActId the e auth act id
   * @return the pre auth document lists
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getPreAuthDocumentLists(int eauthActId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PREAUTH_DOCUMENT_LISTS);
      ps.setInt(1, eauthActId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  
  /**
   * Gets the pre auth prescribed items.
   *
   * @param preAuthPrescList the pre auth presc list
   * @param itemType the item type
   * @return the pre auth prescribed items
   */
  public static List getPreAuthPrescribedItems(List<BasicDynaBean> preAuthPrescList,
                                               String itemType) {
    List itemTypeList = new ArrayList();
    if (preAuthPrescList != null && !preAuthPrescList.isEmpty()) {
      for (BasicDynaBean b : preAuthPrescList) {
        if (!b.get("status").equals('X')) {
          if (b.get("preauth_act_type").equals(itemType)) {
            itemTypeList.add(b);
          }
        }
      }
    }
    return itemTypeList;
  }

  /**
   * Gets the prior auth presc totals charge.
   *
   * @param preAuthPrescList the pre auth presc list
   * @return the prior auth presc totals charge
   */
  public static Map<String, Object> getPriorAuthPrescTotalsCharge(
      List<BasicDynaBean> preAuthPrescList) {
    Map<String, Object> totalChargeMap = new HashMap<String, Object>();

    BigDecimal totalAmount = BigDecimal.ZERO;
    BigDecimal totalDiscount = BigDecimal.ZERO;
    BigDecimal totalGrossAmount = BigDecimal.ZERO;
    BigDecimal totalPatientAmount = BigDecimal.ZERO;
    BigDecimal totalClaimNetAmount = BigDecimal.ZERO;
    BigDecimal totalApprovedNetAmount = BigDecimal.ZERO;

    if (preAuthPrescList != null && !preAuthPrescList.isEmpty()) {
      for (BasicDynaBean itemcharge : preAuthPrescList) {
        if (!itemcharge.get("status").equals('X')) {
          BigDecimal chargeAmount = (BigDecimal) itemcharge
              .get("amount");
          BigDecimal discount = (BigDecimal) itemcharge
              .get("discount");

          totalAmount = totalAmount.add(chargeAmount).add(discount);
          totalDiscount = totalDiscount.add(discount);
          totalGrossAmount = totalGrossAmount.add(chargeAmount);
          BigDecimal patientShare = (BigDecimal) itemcharge
              .get("patient_share");
          totalPatientAmount = totalPatientAmount.add(patientShare);
          BigDecimal claimNetAmount = (BigDecimal) itemcharge
              .get("claim_net_amount");
          totalClaimNetAmount = totalClaimNetAmount
              .add(claimNetAmount);
          BigDecimal claimNetApvAmt = (BigDecimal) itemcharge
              .get("claim_net_approved_amount");
          totalApprovedNetAmount = totalApprovedNetAmount
              .add(claimNetApvAmt);

        }
      }
      totalChargeMap.put("totalAmount", totalAmount);
      totalChargeMap.put("totalDisc", totalDiscount);
      totalChargeMap.put("totalGrossAmt", totalGrossAmount);
      totalChargeMap.put("totalPatientAmt", totalPatientAmount);
      totalChargeMap.put("totalClaimNetAmt", totalClaimNetAmount);
      totalChargeMap.put("totalApprovedClaimAmt", totalApprovedNetAmount);
    }

    return totalChargeMap;
  }

  /**
   * The Constant GET_EAUTH_PRESCRIPTION_REQUEST.
   */
  private static final String GET_EAUTH_PRESCRIPTION_REQUEST = "SELECT prad.approval_comments, "
      + "prad.preauth_id_payer, prad.approval_status, prad.approval_result, "
      + "prad.approval_limit, prad.approval_recd_date, prad.preauth_request_id, prad.request_date,"
      + " prad.preauth_request_type, prad.start_date, prad.end_date, prad.request_by, "
      + "prad.is_resubmit, prad.preauth_sender_id "
      + "FROM preauth_request_approval_details prad "
      + "JOIN preauth_prescription pp ON (prad.preauth_request_id = pp.preauth_request_id) "
      + "WHERE pp.preauth_presc_id = ?";

  /**
   * Gets the e auth prescription request.
   *
   * @param preauthActivityId the preauth activity id
   * @return the e auth prescription request
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getEAuthPrescriptionRequest(
      int preauthActivityId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_EAUTH_PRESCRIPTION_REQUEST);
      ps.setInt(1, preauthActivityId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * The Constant GET_ADDITIONAL_PATIENT_DETAILS.
   */
  private static final String GET_ADDITIONAL_PATIENT_DETAILS = " SELECT "
      + "    CASE WHEN pd.government_identifier IS NULL OR pd.government_identifier = '' THEN "
      + "    COALESCE(gim.remarks,'') || '(' || COALESCE(gim.identifier_type,'---') || ')' ELSE"
      + "  pd.government_identifier END AS emirates_id_number, "
      + " encounter_type_desc, eet.code_desc as encounter_end_code_desc, "
      + " est.code_desc as encounter_start_code_desc  FROM patient_details pd "
      + "    JOIN patient_registration pr ON (pr.mr_no = pd.mr_no)"
      + "    LEFT JOIN encounter_type_codes etc ON (etc.encounter_type_id = pr.encounter_type) "
      + "    LEFT JOIN encounter_end_types eet ON (eet.code::integer = pr.encounter_end_type) "
      + "    LEFT JOIN encounter_start_types est ON (est.code::integer = pr.encounter_start_type) "
      + "    LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id) "
      + "    WHERE pr.patient_id = ?";

  /**
   * Gets the e auth patient registration.
   *
   * @param patientId the patient id
   * @return the e auth patient registration
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getEAuthPatientRegistration(String patientId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ADDITIONAL_PATIENT_DETAILS);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
