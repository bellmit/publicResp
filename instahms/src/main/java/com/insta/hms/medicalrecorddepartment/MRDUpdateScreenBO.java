package com.insta.hms.medicalrecorddepartment;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillChargeClaimDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.billing.DRGCalculator;
import com.insta.hms.billing.DiscountPlanBO;
import com.insta.hms.billing.DrgUpdateDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.dischargesummary.DischargeSummaryDAOImpl;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.insurance.SponsorDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.PerDiemCodes.PerDiemCodesDAO;
import com.insta.hms.orders.OrderBO;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class MRDUpdateScreenBO.
 */
public class MRDUpdateScreenBO {

  /** The log. */
  Logger log = LoggerFactory.getLogger(MRDUpdateScreenBO.class);
  
  private static final GenericDAO mrdSupportedCodesDAO = new GenericDAO("mrd_supported_codes");
  private static final GenericDAO doctorConsultationDAO = new GenericDAO("doctor_consultation");
  private static final GenericDAO patientRegistrationDAO = new GenericDAO("patient_registration");

  /**
   * Instantiates a new MRD update screen BO.
   */
  public MRDUpdateScreenBO() {
  }

  /** The Constant MRD_DIAGNOSIS_QUERY. */
  public static final String MRD_DIAGNOSIS_QUERY = "SELECT visit_id, icd_code, "
      + " description as code_desc, present_on_admission, year_of_onset, "
      + " (SELECT code_desc FROM  (SELECT code,code_type,code_desc "
      + "   FROM mrd_codes_master where code_type!='Drug' AND code_type!='Encounter Start' "
      + "   AND code_type!='Encounter End' AND code_type!='Encounter Type'  ) as mcm "
      + "WHERE (mcm.code=mc.icd_code AND mcm.code_type=mc.code_type ) LIMIT 1 ) AS master_desc, "
      + " diag_type, mc.code_type, mc.id, mc.sent_for_approval FROM mrd_diagnosis mc "
      + " WHERE visit_id = ? ";

  /**
   * Gets the mrd diagnosis list.
   *
   * @param visitId
   *          the visit id
   * @param codeType
   *          the code type
   * @return the mrd diagnosis list
   * @throws SQLException
   *           the SQL exception
   */
  public List getMrdDiagnosisList(String visitId, String codeType) throws SQLException {
    try (Connection con = DataBaseUtil.getConnection()) {
      /*
       * first check if the diagnosis has been copied to hospital_claim_diagnosis(coder edited
       * diagnosis) if not copied, then bring diagnosis from mrd_diagnosis table
       */
      List<BasicDynaBean> result = getMrdCoderDiagnosis(con, visitId, codeType);
      if (result != null && !result.isEmpty()) {
        return result;
      } else {
        return getMrdDiagnosis(con, visitId, codeType);
      }
    }
  }

  /** The Constant PREV_MRD_DIAGNOSIS_QUERY. */
  public static final String PREV_MRD_DIAGNOSIS_QUERY = "SELECT mc.visit_id, icd_code, "
      + " description as code_desc,  year_of_onset, present_on_admission, "
      + "(SELECT code_desc FROM (SELECT code,code_type,code_desc "
      + "   FROM mrd_codes_master where code_type!='Drug' AND code_type!='Encounter Start' "
      + "   AND code_type!='Encounter End' AND code_type!='Encounter Type' ) as mcm "
      + " WHERE (mcm.code=mc.icd_code AND mcm.code_type=mc.code_type ) LIMIT 1 ) AS master_desc, "
      + " diag_type, mc.code_type, mc.id, mc.sent_for_approval FROM mrd_diagnosis mc JOIN "
      + "(SELECT main_visit_id AS visit_id FROM patient_registration pr WHERE pr.patient_id = ? ) "
      + "AS foo ON (foo.visit_id = mc.visit_id) ORDER BY diag_type ";

  public static final String MRD_CODER_DIAGNOSIS_QUERY = "SELECT hcd.visit_id, icd_code, "
      + " description as code_desc, year_of_onset, present_on_admission, "
      + "(SELECT code_desc FROM (SELECT code,code_type,code_desc "
      + "   FROM mrd_codes_master where code_type!='Drug' AND code_type!='Encounter Start' "
      + "   AND code_type!='Encounter End' AND code_type!='Encounter Type' ) as mcm "
      + " WHERE (mcm.code=hcd.icd_code AND mcm.code_type=hcd.code_type ) LIMIT 1 ) AS master_desc, "
      + " diag_type, hcd.code_type, hcd.id FROM hospital_claim_diagnosis hcd "
      + "WHERE hcd.visit_id = ? ORDER BY diag_type ";

  /**
   * Gets the prev mrd diagnosis list.
   *
   * @param visitId
   *          the visit id
   * @param codeType
   *          the code type
   * @return the prev mrd diagnosis list
   * @throws SQLException
   *           the SQL exception
   */
  public List getPrevMrdDiagnosisList(String visitId, String codeType) throws SQLException {
    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getConnection();
    try {
      BasicDynaBean visitDetails = VisitDetailsDAO.getVisitDetails(visitId);
      String mainVisitId = null;
      if (visitDetails != null && visitDetails.get("doctor") != null) {
        List<BasicDynaBean> prevMainVisits = VisitDetailsDAO.getPatientPreviousMainVisits(
            (String) visitDetails.get("mr_no"), (String) visitDetails.get("doctor"));
        if (prevMainVisits != null && prevMainVisits.size() > 0) {
          for (BasicDynaBean mainVisit : prevMainVisits) {
            if (mainVisit.get("op_type") != null && mainVisit.get("op_type").equals("M")) {
              mainVisitId = (String) mainVisit.get("patient_id");
              break;
            }
          }
        }
      }

      if (codeType != null && !codeType.equals("")) {
        ps = con.prepareStatement(PREV_MRD_DIAGNOSIS_QUERY + "AND mc.code_type = ?");
      } else {
        ps = con.prepareStatement(PREV_MRD_DIAGNOSIS_QUERY);
      }
      int idx = 1;
      ps.setString(idx++, mainVisitId);
      if (codeType != null && !codeType.equals("")) {
        ps.setString(idx++, codeType);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The ph sales bills query. */
  private static final String PH_SALES_BILLS_QUERY = " SELECT * FROM  "
      + " (SELECT b.bill_no, b.bill_type, b.visit_id,"
      + "  case when b.bill_type = 'C' then 'C' else 'N' end as bill_bill_type, "
      + "  case when b.visit_id=prc.customer_id and b.bill_type='C'  "
      + "  and b.visit_type='r' then 'c' else b.visit_type end as visit_type, "
      + "  pmsm.sale_id, pmsm.type AS pharm_bill_type, pmsm.sale_date, "
      + "  coalesce(pmsm.doctor_name,'') as doctor_name, pr.mr_no, pr.patient_id, "
      + "  coalesce(get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name)"
      + "  ,prc.customer_name) as patient_full_name,"
      + "  pmsm.total_item_amount-pmsm.discount+pmsm.round_off as amount,"
      + "  b.visit_type AS bill_visit_type, user_remarks as remarks,pr.main_visit_id, pr.op_type  "
      + "  FROM store_sales_main pmsm JOIN bill b on(pmsm.bill_no=b.bill_no) "
      + "  JOIN bill_charge bc on(bc.charge_id = pmsm.charge_id)  "
      + "  LEFT JOIN store_retail_customers prc on(prc.customer_id=b.visit_id) "
      + "  LEFT JOIN patient_registration pr on(pr.patient_id=b.visit_id) "
      + "  LEFT JOIN patient_details pd USING (mr_no)) AS FOO WHERE patient_id = ? ";

  /**
   * Gets the sales bill url list.
   *
   * @param visitId
   *          the visit id
   * @return the sales bill url list
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public List getSalesBillUrlList(String visitId) throws SQLException, ParseException {
    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getConnection();
    try {
      ps = con.prepareStatement(PH_SALES_BILLS_QUERY);
      ps.setString(1, visitId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant MRD_DR_CONSULTATION_QUERY. */
  public static final String MRD_DR_CONSULTATION_QUERY = " SELECT  pr.mr_no, pr.patient_id "
      + " as visit_id, consultation_type, dc.consultation_id, b.bill_no, pr.op_type, "
      + " ct.consultation_type_id, (act_rate_plan_item_code) AS item_code, "
      + " COALESCE(bc.code_type) AS code_type, bc.charge_id, "
      + " doc.doctor_name, doc.doctor_id, dept.dept_name, "
      + " COALESCE(dc.description,mcm.code_desc) AS code_desc,"
      + " dc.consultation_id, dc.status AS consultation_status, b.status AS bill_status, CASE"
      + " WHEN pr.established_type = 'E' then 'Established' ELSE 'New' END AS established_type"
      + " FROM doctor_consultation dc "
      + " JOIN consultation_types ct on (ct.consultation_type_id::text=dc.head AND "
      + " (dc.ot_doc_role = '' OR dc.ot_doc_role IS NULL)) "
      + " JOIN patient_registration pr USING (patient_id) "
      + " JOIN bill_activity_charge bac ON bac.activity_code = 'DOC' "
      + "  AND bac.activity_id = dc.consultation_id::varchar "
      + " JOIN bill_charge bc ON bc.charge_id = bac.charge_id JOIN bill b using (bill_no) "
      + " JOIN consultation_org_details cod ON (cod.consultation_type_id=ct.consultation_type_id"
      + "  AND cod.org_id= b.bill_rate_plan_id) "
      + " JOIN doctors doc ON doc.doctor_id=dc.doctor_name "
      + " JOIN department dept ON dept.dept_id=doc.dept_id LEFT JOIN ( "
      + " SELECT code,code_type,code_desc FROM mrd_codes_master "
      + "   where code_type!='Drug' AND code_type!='Encounter Start' "
      + "   AND code_type!='Encounter End' AND code_type!='Encounter Type' "
      + " ) as mcm ON (mcm.code = cod.item_code AND mcm.code_type = cod.code_type) "
      + " LEFT JOIN chargehead_constants chc ON bc.charge_head= chc.chargehead_id "
      + " WHERE pr.patient_id = ?  " + " AND dc.status != 'X' AND bc.status != 'X' "
      + " AND bc.charge_group='DOC' ORDER BY dc.consultation_id ";

  /**
   * Gets the mrd dr consultation list.
   *
   * @param visitId
   *          the visit id
   * @param codeType
   *          the code type
   * @return the mrd dr consultation list
   * @throws SQLException
   *           the SQL exception
   */
  public List getMrdDrConsultationList(String visitId, String codeType) throws SQLException {
    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getConnection();
    try {
      if (codeType != null && !codeType.equals("")) {
        ps = con.prepareStatement(MRD_DR_CONSULTATION_QUERY + "AND code_type = ?");
      } else {
        ps = con.prepareStatement(MRD_DR_CONSULTATION_QUERY);
      }
      int idx = 1;
      ps.setString(idx++, visitId);
      if (codeType != null && !codeType.equals("")) {
        ps.setString(idx++, codeType);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The allowed item codes for consultn query. */
  public static final String ALLOWED_ITEM_CODES_FOR_CONSULTN_QUERY = " SELECT "
      + " cod.consultation_type_id, ct.consultation_type, cod.org_id, cod.applicable,"
      + " cod.item_code, cod.code_type, cod.username, cod.mod_time, mcm.code_type, "
      + " CASE WHEN mcm.code='' THEN NULL ELSE mcm.code END AS code, mcm.code_desc, mcm.status "
      + " FROM  consultation_org_details cod JOIN ( "
      + " SELECT code,code_type,code_desc, status FROM mrd_codes_master "
      + "   where code_type!='Drug' AND code_type!='Encounter Start' "
      + "   AND code_type!='Encounter End' AND code_type!='Encounter Type' "
      + " ) as mcm ON mcm.code= cod.item_code "
      + " JOIN bill b ON b.bill_rate_plan_id = cod.org_id  "
      + " JOIN consultation_types ct ON (ct.consultation_type_id=cod.consultation_type_id) "
      + " WHERE visit_id = ? ";

  /**
   * Gets the allowed item codes list.
   *
   * @param visitId
   *          the visit id
   * @return the allowed item codes list
   * @throws SQLException
   *           the SQL exception
   */
  public List getAllowedItemCodesList(String visitId) throws SQLException {
    return getAllowedItemCodesList(visitId, "");
  }

  /**
   * Gets the allowed item codes list.
   *
   * @param visitId
   *          the visit id
   * @param codeType
   *          the code type
   * @return the allowed item codes list
   * @throws SQLException
   *           the SQL exception
   */
  public List getAllowedItemCodesList(String visitId, String codeType) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      int index = 1;
      StringBuilder query = new StringBuilder(ALLOWED_ITEM_CODES_FOR_CONSULTN_QUERY);
      if (codeType != null && codeType.split(",").length > 1) {
        List<String> values = new ArrayList<String>(Arrays.asList(codeType.split(",")));
        DataBaseUtil.addWhereFieldInList(query, "cod.code_type", values, true);
        ps = con.prepareStatement(query.toString());
        ps.setString(index++, visitId);

        for (String codetype : values) {
          ps.setObject(index++, codetype);
        }
      } else {
        if (codeType == null || codeType.equals("")) {
          ps = con.prepareStatement(query.toString());
          ps.setString(index++, visitId);
        } else {
          query.append(" AND cod.code_type = ? ");
          ps = con.prepareStatement(query.toString());
          ps.setString(index++, visitId);
          ps.setString(index++, codeType);
        }
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The diagnosis code count. */
  public static final String DIAGNOSIS_CODE_COUNT = "select count(visit_id) as secondary_codes "
      + " from mrd_diagnosis where visit_id = ? and (diag_type = 'S' or diag_type = 'A') ";

  /**
   * Diagnosis code count.
   *
   * @param visitId
   *          the visit id
   * @return the string
   * @throws SQLException
   *           the SQL exception
   */
  public String diagnosisCodeCount(String visitId) throws SQLException {
    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getConnection();
    ResultSet rs = null;
    try {
      ps = con.prepareStatement(DIAGNOSIS_CODE_COUNT);
      ps.setString(1, visitId);
      rs = ps.executeQuery();
      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }

  }

  /** The Constant GET_TREATMENT_CODES_QUERY. */
  public static final String GET_TREATMENT_CODES_QUERY = "SELECT DISTINCT bc.bill_no, "
      + " act_rate_plan_item_code, order_number, posted_date, "
      + " CASE WHEN ((cc.chargegroup_id = 'SNP') AND (bc.conducted_datetime IS NOT NULL) AND "
      + " (ser.activity_timing_eclaim='Y')) "
      + " THEN bc.conducted_datetime "
      + " ELSE posted_date END as activity_start_datetime,"
      + " chargehead_name, bc.code_type, act_description, bc.charge_id, tdv.dept_name, "
      + " COALESCE((SELECT code_desc  FROM (SELECT code,code_type,code_desc "
      + "   FROM mrd_codes_master WHERE code_type!='Drug' AND code_type!='Encounter Start'"
      + "   AND code_type!='Encounter End' AND code_type!='Encounter Type'"
      + "   AND (code=bc.act_rate_plan_item_code AND  code_type=bc.code_type)"
      + " UNION ALL "
      + " SELECT item_code, code_type, null FROM store_item_codes sic "
      + "   WHERE item_code IS NOT NULL AND item_code!=''"
      + "   AND (item_code=bc.act_rate_plan_item_code AND  code_type=bc.code_type)"
      + " UNION ALL "
      + " SELECT code,code_type,code_desc FROM encounter_start_types "
      + "   WHERE (code=bc.act_rate_plan_item_code AND  code_type=bc.code_type)"
      + " UNION ALL "
      + " SELECT code,code_type,code_desc FROM encounter_end_types"
      + "   WHERE (code=bc.act_rate_plan_item_code AND  code_type=bc.code_type)"
      + " UNION ALL "
      + " SELECT encounter_type_id::varchar,code_type,encounter_type_desc "
      + "  FROM encounter_type_codes WHERE (encounter_type_id::varchar=bc.act_rate_plan_item_code"
      + "  AND code_type=bc.code_type)"
      + " UNION ALL  "
      + " SELECT drg_code, code_type, drg_description FROM drg_codes_master"
      + "     WHERE (drg_code=bc.act_rate_plan_item_code AND  code_type=bc.code_type)) as mcm"
      + "   LIMIT 1 ), '') AS master_desc , CASE WHEN bc.charge_group = 'PKG' THEN "
      + "   ( SELECT status FROM patient_orders_view "
      + " WHERE order_id::text = COALESCE(bac.activity_id,bacc.activity_id) "
      + " AND activity_code = COALESCE(bac.activity_code,bacc.activity_code)) "
      + "   ELSE bc.activity_conducted"
      + "   END AS  conducted_status,  CASE WHEN diag.conduction_format='V' THEN"
      + "  bc.act_description_id  ELSE NULL END AS act_des_id,"
      + " CASE WHEN ser.tooth_num_required = 'Y' THEN 'Y' ELSE 'N' END AS tooth_num_reqd, "
      + " bc.prior_auth_id ,bc.prior_auth_mode_id,bc.insurance_claim_amount,b.is_tpa, "
      + " pbc.bill_no AS primary_bill_no, pbc.prior_auth_id AS primary_auth_id, "
      + " pbc.prior_auth_mode_id AS primary_auth_mode_id, pbc.charge_id AS primary_charge_id,"
      + "  pbc.claim_id AS primary_claim_id, sbc.bill_no AS secondary_bill_no, sbc.prior_auth_id"
      + "  AS secondary_auth_id, sbc.prior_auth_mode_id AS secondary_auth_mode_id, "
      + " sbc.charge_id AS secondary_charge_id, sbc.claim_id AS secondary_claim_id, "
      + " bc.submission_batch_type, bc.charge_head as charge_head "
      + " FROM bill_charge bc LEFT JOIN bill_activity_charge bac ON "
      + "   (bac.charge_id = bc.charge_id AND bc.package_id IS NOT NULL) "
      + " LEFT JOIN bill_activity_charge bacc ON " 
      + " (bacc.charge_id = bc.charge_id and bacc.activity_code='PKG') "
      + " JOIN bill b USING (bill_no) "
      + " LEFT JOIN (SELECT bc.bill_no, bcc.prior_auth_id, bcc.prior_auth_mode_id,"
      + "  bcc.charge_id,bc.claim_id FROM patient_insurance_plans pip "
      + " JOIN bill_claim bc ON (pip.plan_id = bc.plan_id and pip.patient_id = bc.visit_id) "
      + " JOIN bill_charge_claim bcc ON (bcc.claim_id = bc.claim_id) WHERE pip.priority =1 "
      + " AND pip.patient_id = ? "
      + ") as pbc ON(pbc.bill_no = b.bill_no and pbc.charge_id= bc.charge_id)"
      + " LEFT JOIN (SELECT bc.bill_no, bcc.prior_auth_id, bcc.prior_auth_mode_id,"
      + " bcc.charge_id, bc.claim_id FROM patient_insurance_plans pip "
      + " JOIN bill_claim bc ON (pip.plan_id = bc.plan_id and pip.patient_id = bc.visit_id) "
      + " JOIN bill_charge_claim bcc ON (bcc.claim_id = bc.claim_id) WHERE pip.priority =2 "
      + " AND pip.patient_id = ? "
      + ") as sbc on(sbc.bill_no = b.bill_no and sbc.charge_id= bc.charge_id)"
      + " JOIN chargehead_constants cc ON (cc.chargehead_id = bc.charge_head) "
      + " JOIN patient_registration pr ON (pr.patient_id = b.visit_id)"
      + " JOIN hospital_center_master hcm ON (pr.center_id = hcm.center_id)"
      + " JOIN health_authority_preferences hap ON (hap.health_authority=hcm.health_authority)"
      + " LEFT JOIN treating_departments_view tdv ON (bc.act_department_id = tdv.dept_id) "
      + " LEFT JOIN diagnostics diag ON (bc.act_description_id = diag.test_id) "
      + " LEFT JOIN services ser ON (bc.charge_group = 'SNP' "
      + " AND bc.act_description_id = ser.service_id) "
      + " LEFT JOIN packages pkg using (package_id)"
      + " WHERE codification_supported != 'N' AND pr.patient_id = ? "
      + " AND cc.chargehead_id NOT IN ('PHCMED','PHMED','PHRET','PHCRET','ADJDRG')  "
      + " AND bc.charge_group!='RET' AND bc.charge_group!='DOC' AND bc.status!='X'"
      + " AND b.total_amount >= 0";

  /**
   * Gets the mrd treatment codes list.
   *
   * @param visitId
   *          the visit id
   * @param codeType
   *          the code type
   * @return the mrd treatment codes list
   * @throws SQLException
   *           the SQL exception
   */
  public List getMrdTreatmentCodesList(String visitId, String codeType) throws SQLException {
    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getConnection();
    try {
      if (codeType != null && !codeType.equals("")) {
        ps = con.prepareStatement(GET_TREATMENT_CODES_QUERY 
           + "AND bc.code_type = ? ORDER BY bc.charge_id");
      } else {
        ps = con.prepareStatement(GET_TREATMENT_CODES_QUERY + "ORDER BY bc.charge_id");
      }
      int idx = 1;
      ps.setString(idx++, visitId);
      ps.setString(idx++, visitId);
      ps.setString(idx++, visitId);
      if (codeType != null && !codeType.equals("")) {
        ps.setString(idx++, codeType);
      }

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_DIAG_CODE_FAVOURITE. */
  private static final String GET_DIAG_CODE_FAVOURITE = " SELECT mcm.code, "
      + " mcm.code ||' '||COALESCE(code_desc,'') AS icd,  "
      + " COALESCE(code_desc,'') AS code_desc, mcdm.code_type, "
      + " mcd.health_authority, mcd.is_year_of_onset_mandatory "
      + " FROM mrd_codes_doctor_master mcdm "
      + " JOIN mrd_codes_master mcm ON (mcdm.code=mcm.code AND mcdm.code_type=mcm.code_type) "
      + " LEFT JOIN mrd_codes_details mcd on(mcd.mrd_code_id = mcm.mrd_code_id) "
      + " WHERE mcdm.code_type=? AND doctor_id=? ";

  /**
   * Gets the diag code favourites.
   *
   * @param input
   *          the input
   * @param doctorId
   *          the doctor id
   * @param codeType
   *          the code type
   * @return the diag code favourites
   * @throws SQLException
   *           the SQL exception
   */
  public List getDiagCodeFavourites(String input, String doctorId, String codeType)
      throws SQLException {

    String[] searchWord = input.split(" ");
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      StringBuilder query = new StringBuilder(GET_DIAG_CODE_FAVOURITE);
      for (int index = 0; index < searchWord.length; index++) {
        query.append(" AND (mcm.code ILIKE ? OR code_desc ILIKE ? OR code_desc"
            + "  ILIKE ? OR code_desc ILIKE ?)");
      }
      query.append(" LIMIT 100");
      int psIndex = 1;
      ps = con.prepareStatement(query.toString());
      ps.setString(psIndex++, codeType);
      ps.setString(psIndex++, doctorId);
      for (int i = 0; i < searchWord.length; i++) {
        ps.setString(psIndex++, searchWord[i] + "%");
        ps.setString(psIndex++, searchWord[i] + "%");
        ps.setString(psIndex++, "%" + searchWord[i]);
        ps.setString(psIndex++, "%" + searchWord[i] + "%");
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant SEARCH_CODES_OF_TYPE_QUERY. */
  private static final String SEARCH_CODES_OF_TYPE_QUERY = "SELECT code, code ||' ' "
      + " ||COALESCE(code_desc,'') AS icd,  COALESCE(code_desc,'') AS code_desc,code_type,"
      + " mcd.health_authority, mcd.is_year_of_onset_mandatory"
      + " FROM getItemCodesForCodeType(?, ?) fn "
      + " LEFT JOIN mrd_codes_details mcd on(mcd.mrd_code_id = fn.mrd_code_id::integer) "
      + " WHERE status = 'A' AND code_type = ? ";

  /** The Constant SEARCH_MULTIPLE_CODES_OF_TYPE_QUERY. */
  private static final String SEARCH_MULTIPLE_CODES_OF_TYPE_QUERY = "SELECT code, code ||' ' "
      + "||COALESCE(code_desc,'') AS icd,  COALESCE(code_desc,'') AS code_desc,code_type, "
      + " mcd.health_authority, mcd.is_year_of_onset_mandatory "
      + " FROM getItemCodesForCodeType('*', ?) fn "
      + " LEFT JOIN mrd_codes_details mcd on(mcd.mrd_code_id = fn.mrd_code_id::integer) "
      + " WHERE status = 'A' ";

  /**
   * Gets the codes list of code type.
   *
   * @param searchInput
   *          the search input
   * @param codeType
   *          the code type
   * @return the codes list of code type
   * @throws SQLException
   *           the SQL exception
   */
  public List getCodesListOfCodeType(String searchInput, String codeType) throws SQLException {
    String patientType = "";
    return getCodesListOfCodeType(searchInput, codeType, patientType, null);
  }

  /**
   * Gets the codes list of code type.
   *
   * @param searchInput
   *          the search input
   * @param codeType
   *          the code type
   * @param patientType
   *          the patient type
   * @param dialogType
   *          the dialog type
   * @return the codes list of code type
   * @throws SQLException
   *           the SQL exception
   */
  @IgnoreConfidentialFilters
  public List getCodesListOfCodeType(String searchInput, String codeType, String patientType,
      String dialogType) throws SQLException {

    String[] searchWord = searchInput.split(" ");
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      int psIndex = 1;
      if (dialogType != null && dialogType.equals("consultation")) {
        if (codeType != null && !codeType.equals("")) {

          if (codeType.split(",").length > 1) {
            StringBuilder query = new StringBuilder(SEARCH_MULTIPLE_CODES_OF_TYPE_QUERY);
            List<BasicDynaBean> values =
                mrdSupportedCodesDAO.findAllByKey("code_category", "Consultations");
            DataBaseUtil.addWhereFieldInList(query, "code_type", values, true);
            for (int index = 0; index < searchWord.length; index++) {
              query.append(" AND (code ILIKE ? OR code_desc ILIKE ?) ");
            }
            query.append(" LIMIT 100");
            ps = con.prepareStatement(query.toString());
            ps.setString(psIndex++, patientType);

            for (BasicDynaBean bean : values) {
              ps.setObject(psIndex++, bean.get("code_type"));
            }
          } else {
            StringBuilder query = new StringBuilder(SEARCH_CODES_OF_TYPE_QUERY);
            for (int index = 0; index < searchWord.length; index++) {
              query.append(" AND (code ILIKE ? OR code_desc ILIKE ?)");
            }
            query.append(" LIMIT 100");
            ps = con.prepareStatement(query.toString());
            ps.setString(psIndex++, codeType);
            ps.setString(psIndex++, patientType);
            ps.setString(psIndex++, codeType);
          }
        } else {
          StringBuilder query = new StringBuilder(SEARCH_MULTIPLE_CODES_OF_TYPE_QUERY);
          List<BasicDynaBean> values =
              mrdSupportedCodesDAO.findAllByKey("code_category", "Consultations");
          DataBaseUtil.addWhereFieldInList(query, "code_type", values, true);
          for (int index = 0; index < searchWord.length; index++) {
            query.append(" AND (code ILIKE ? OR code_desc ILIKE ?) ");
          }
          query.append(" LIMIT 100");
          ps = con.prepareStatement(query.toString());
          ps.setString(psIndex++, patientType);

          for (BasicDynaBean bean : values) {
            ps.setObject(psIndex++, bean.get("code_type"));
          }
        }
      } else {
        StringBuilder query = new StringBuilder(SEARCH_CODES_OF_TYPE_QUERY);
        for (int k = 0; k < searchWord.length; k++) {
          query.append(" AND (code ILIKE ? OR code_desc ILIKE ?)");
        }
        query.append(" LIMIT 100");
        ps = con.prepareStatement(query.toString());
        ps.setString(psIndex++, codeType);
        ps.setString(psIndex++, patientType);
        ps.setString(psIndex++, codeType);
      }
      for (int index = 0; index < searchWord.length; index++) {
        ps.setString(psIndex++, searchWord[index] + "%");
        ps.setString(psIndex++, "%" + searchWord[index] + "%");
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_ENCOUNTER_CODES. */
  private static final String GET_ENCOUNTER_CODES = "SELECT pr.insurance_id, "
      + " ths.transfer_hospital_name AS transfer_source,"
      + " pr.transfer_source AS transfer_source_id, "
      + " thd.transfer_hospital_name AS transfer_destination, "
      + " pr.transfer_destination AS transfer_destination_id, "
      + " CASE WHEN pr.encounter_type=0 THEN null ELSE pr.encounter_type END AS "
      + " encounter_type  , pr.patient_id, "
      + " CASE WHEN pr.encounter_start_type=0 THEN null ELSE pr.encounter_start_type END "
      + " AS encounter_start_type , "
      + " CASE WHEN pr.encounter_end_type=0 THEN null ELSE pr.encounter_end_type END"
      + " AS encounter_end_type, "
      + " COALESCE((SELECT code_desc from getItemCodesForCodeType('Encounter Type') mcm1"
      + "  WHERE (pr.encounter_type::text = mcm1.code)  LIMIT 1), '') AS enc_type_desc,"
      + " COALESCE((SELECT code_desc from getItemCodesForCodeType('Encounter Start') mcm2 "
      + " WHERE (pr.encounter_start_type::text = mcm2.code)  LIMIT 1), '') as enc_start_type_desc,"
      + "COALESCE((SELECT code_desc from getItemCodesForCodeType('Encounter End') "
      + " mcm3 WHERE (pr.encounter_end_type::text = mcm3.code)  LIMIT 1), '') as enc_end_type_desc,"
      + " reg_date AS encounter_start_date,reg_time AS encounter_start_time,"
      + " CASE WHEN pr.visit_type='i' THEN discharge_date "
      + " ELSE greatest(bc_conducted.max_order_date::date, bc_posted.max_order_date::date)"
      + " END AS encounter_end_date,"
      + " CASE WHEN pr.visit_type = 'i' THEN discharge_time"
      + " ELSE CASE WHEN pr.encounter_end_date IS NOT NULL THEN pr.encounter_end_time "
      + " ELSE greatest(bc_conducted.max_order_date::time, bc_posted.max_order_date::time)"
      + " END END AS encounter_end_time,"
      + " CASE WHEN pr.encounter_end_date IS NOT NULL THEN 'Y' "
      + " ELSE 'N' END AS is_enc_end_overridden, "
      + " greatest(bc_conducted.max_order_date, bc_posted.max_order_date) "
      + " AS max_order_date "
      + " FROM patient_registration pr "
      + " LEFT JOIN transfer_hospitals ths ON ths.transfer_hospital_id=pr.transfer_source "
      + " LEFT JOIN transfer_hospitals thd ON thd.transfer_hospital_id=pr.transfer_destination "
      + " JOIN bill b ON b.visit_id=pr.patient_id "
      + " LEFT JOIN LATERAL (SELECT bill_no,max(posted_date) AS max_order_date "
      + " FROM bill_charge  "
      + " WHERE bill_charge.status <> 'X'"
      + " AND charge_head NOT IN ('PHCRET','PHRET','INVRET') AND bill_charge.bill_no = b.bill_no "
      + " AND b.restriction_type = 'N' AND b.account_group = 1 " 
      + " GROUP BY bill_no) AS bc_posted"
      + " on b.bill_no = bc_posted.bill_no LEFT JOIN LATERAL "
      + " (SELECT bill_no,max(conducted_datetime) AS max_order_date "
      + " FROM bill_charge"
      + " JOIN services ser on ser.service_id = act_description_id WHERE bill_charge.status <> 'X' "
      + " AND ser.activity_timing_eclaim=true AND charge_head IN ('SERSNP') "
      + " AND bill_charge.bill_no = b.bill_no AND b.restriction_type = 'N' AND b.account_group = 1 "
      + " GROUP BY bill_no) AS bc_conducted "
      + " on b.bill_no = bc_conducted.bill_no WHERE patient_id = ? AND b.status <> 'X'"
      + " ORDER BY encounter_end_date DESC NULLS LAST, encounter_end_time DESC NULLS LAST, " 
      + " max_order_date DESC LIMIT 1";

  /**
   * Gets the encounter codes.
   *
   * @param visitId
   *          the visit id
   * @return the encounter codes
   * @throws SQLException
   *           the SQL exception
   */
  public Map getEncounterCodes(String visitId) throws SQLException, ParseException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_ENCOUNTER_CODES);
      ps.setString(1, visitId);
      List list = DataBaseUtil.queryToDynaList(ps);
      validateEncounterDuration(list);
      if (list != null && list.size() > 0) {
        return ((BasicDynaBean) list.get(0)).getMap();
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Method to validate the encounter duration as it must not be 0.
   *
   * @param encounterList the encounter details from the DB
   * @throws ParseException while parsing the dates
   */
  public void validateEncounterDuration(List encounterList) throws ParseException {

    if (encounterList == null || encounterList.isEmpty()) {
      return;
    }
    BasicDynaBean encounterMap = (BasicDynaBean) encounterList.get(0);
    Date encStartDate = (Date) encounterMap.get("encounter_start_date");
    Time encStartTime = (Time) encounterMap.get("encounter_start_time");
    Date encEndDate = (Date) encounterMap.get("encounter_end_date");
    Time encEndTime = (Time) encounterMap.get("encounter_end_time");

    // For IP visit without any discharge date and time.
    if (encEndDate == null || encEndTime == null) {
      return;
    }

    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date encStartDateTime = dateTimeFormat
        .parse(encStartDate.toString() + " " + encStartTime.toString());
    Date encEndDateTime = dateTimeFormat.parse(encEndDate.toString() + " " + encEndTime.toString());
    long encDurationInMins = (encEndDateTime.getTime() - encStartDateTime.getTime()) / (60 * 1000);
    if (encDurationInMins == 0) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(encStartDateTime);
      cal.add(Calendar.MINUTE, 30);
      Date date = cal.getTime();
      java.sql.Date datePart = new java.sql.Date(date.getTime());
      java.sql.Time timePart = new java.sql.Time(date.getTime());
      encounterMap.set("encounter_end_date", datePart);
      encounterMap.set("encounter_end_time", timePart);
    }
  }

  /** The Constant GET_DRG_CODES. */
  private static final String GET_DRG_CODES = " SELECT b.bill_no, bc.posted_date, bc.charge_id,"
      + "  bc.act_rate_plan_item_code AS code, bc.code_type, (SELECT drg_description "
      + "    FROM drg_codes_master WHERE drg_code = bc.act_rate_plan_item_code)"
      + "  AS description, chargehead_name, act_description, act_remarks FROM bill b "
      + " JOIN bill_charge bc ON (bc.bill_no = b.bill_no AND bc.charge_head = 'MARDRG' "
      + " AND bc.status != 'X') "
      + " JOIN chargehead_constants ON (bc.charge_head = chargehead_constants.chargehead_id) "
      + " WHERE b.bill_no = ? UNION "
      + " SELECT b.bill_no, bc.posted_date, bc.charge_id, bc.act_rate_plan_item_code "
      + "AS code, bc.code_type, '' AS description, chargehead_name,  act_description, act_remarks "
      + " FROM bill b "
      + " JOIN bill_charge bc ON (bc.bill_no = b.bill_no AND bc.charge_head = 'OUTDRG' "
      + " AND bc.status != 'X') "
      + " JOIN chargehead_constants ON (bc.charge_head = chargehead_constants.chargehead_id) "
      + " WHERE b.bill_no = ? ";

  /**
   * Gets the patient DRG codes.
   *
   * @param patientId
   *          the patient id
   * @return the patient DRG codes
   * @throws SQLException
   *           the SQL exception
   */
  public List getPatientDRGCodes(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    // List<BasicDynaBean> l = null;
    try {
      Bill bill = BillDAO.getVisitCreditBill(patientId, false);
      if (bill != null) {
        ps = con.prepareStatement(GET_DRG_CODES);
        ps.setString(1, bill.getBillNo());
        ps.setString(2, bill.getBillNo());
        return DataBaseUtil.queryToDynaList(ps);
      }
      return null;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Update doctor charges for code.
   *
   * @param con
   *          the con
   * @param basicBean
   *          the b
   * @param visitId
   *          the visit id
   * @param codeType
   *          the code type
   * @param code
   *          the code
   * @param userName
   *          the user name
   * @return the string
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws Exception
   *           the exception
   */
  public String updateDoctorChargesForCode(Connection con, BasicDynaBean basicBean, String visitId,
      String codeType, String code, String userName) throws SQLException, IOException, Exception {
    ChargeDAO chargeDAO = new ChargeDAO(con);
    String activityId = basicBean.get("consultation_id").toString();
    String chargeId = BillActivityChargeDAO.getChargeId("DOC", activityId);
    ChargeDTO existingChrge = chargeDAO.getCharge(chargeId);
    // No charge updation if it is cancelled (code is also not updated).
    if (existingChrge.getStatus().equals("X")) {
      return null;
    }

    String existingBillNo = existingChrge.getBillNo();
    Bill bill = new BillDAO(con).getBill(existingBillNo);
    String orgId = bill.getBillRatePlanId();

    BasicDynaBean drConsBean = doctorConsultationDAO.findByKey("consultation_id",
        (Integer) basicBean.get("consultation_id"));
    BasicDynaBean regtrnbean = patientRegistrationDAO.findByKey("patient_id",
        (String) drConsBean.get("patient_id"));
    BasicDynaBean doctor = DoctorMasterDAO.getDoctorCharges((String) drConsBean.get("doctor_name"),
        orgId, (String) regtrnbean.get("bed_type"));
    BasicDynaBean consTypeBean = new GenericDAO("consultation_types")
        .findByKey("consultation_type_id", Integer.parseInt((String) basicBean.get("head")));

    Boolean isInsurance = regtrnbean.get("primary_sponsor_id") != null
        && !((String) regtrnbean.get("primary_sponsor_id")).isEmpty();
    boolean usePerdiem = regtrnbean.get("use_perdiem") != null
        && ((String) regtrnbean.get("use_perdiem")).equals("Y");
    String perdiemCode = regtrnbean.get("per_diem_code") != null
        ? (String) regtrnbean.get("per_diem_code")
        : null;
    String bedType = (String) regtrnbean.get("bed_type");
    BasicDynaBean perdiemBean = new PerDiemCodesDAO().getBillPerdiemCharge(orgId, bedType,
        perdiemCode);
    String includedServGrps = perdiemBean != null && (perdiemBean.get("service_groups_incl") != null
        && !perdiemBean.get("service_groups_incl").equals(""))
            ? (String) perdiemBean.get("service_groups_incl")
            : "";

    String[] servGrpIds = includedServGrps.split(",");

    int[] planIds = new PatientInsurancePlanDAO().getPlanIds(con,
        (String) drConsBean.get("patient_id"));
    List<ChargeDTO> drCharges = OrderBO.getDoctorConsCharges(doctor, consTypeBean,
        (String) regtrnbean.get("visit_type"), OrgMasterDao.getOrgdetailsDynaBean((String) orgId),
        existingChrge.getActQuantity(), isInsurance, planIds, (String) regtrnbean.get("bed_type"),
        (String) drConsBean.get("patient_id"), null);
    ChargeDTO drCharge = drCharges.get(0);
    drCharge.setChargeId(chargeId);
    drCharge.setCodeType(codeType);
    drCharge.setActRatePlanItemCode(code);
    drCharge.setPostedDate(existingChrge.getPostedDate());
    drCharge.setUsername(userName);
    drCharge.setBillNo(existingChrge.getBillNo());
    drCharge.setPreAuthId(existingChrge.getPreAuthId());
    drCharge.setPreAuthModeId(existingChrge.getPreAuthModeId());
    drCharge.setUserRemarks(existingChrge.getUserRemarks());
    drCharge.setActRemarks(existingChrge.getActRemarks());

    DiscountPlanBO discBO = new DiscountPlanBO();
    // say some details abt discount plan
    discBO.setDiscountPlanDetails(bill.getBillDiscountCategory());
    discBO.applyDiscountRule(con, drCharge);
    if (bill.getIs_tpa()) {
      if (null != planIds && planIds.length > 0) {
        setClaimAmtsByRetainingPatientAmtPaid(drCharge, existingChrge, planIds);
      } else {
        BigDecimal patientAmtPaid = existingChrge.getAmount()
            .subtract(existingChrge.getInsuranceClaimAmount());
        drCharge.setInsuranceClaimAmount(drCharge.getAmount().subtract(patientAmtPaid));
      }
    }
    drCharges.remove(0);
    drCharges.add(drCharge);

    String newCode = drCharge.getActRatePlanItemCode() == null ? ""
        : drCharge.getActRatePlanItemCode().trim();
    int serviceSubGrp = drCharge.getServiceSubGroupId() != null ? drCharge.getServiceSubGroupId()
        : 0;
    BillChargeClaimDAO billChgClaimDAO = new BillChargeClaimDAO();
    List<ChargeDTO> chargeList = new ArrayList<ChargeDTO>();
    chargeList.add(drCharge);
    String existingCode = existingChrge.getActRatePlanItemCode() == null ? ""
        : existingChrge.getActRatePlanItemCode().trim();
    int existingConsTypeId = existingChrge.getConsultation_type_id();
    int newConsTypeId = drCharge.getConsultation_type_id();
    if ((drCharge != null) && !existingCode.equals(newCode)
        || existingConsTypeId != newConsTypeId) {
      if (usePerdiem) {
        boolean included = new BillBO().isChargeServiceSubIncluded(servGrpIds, serviceSubGrp);
        if (!included) {
          chargeDAO.updateChargeAmounts(drCharge);
          billChgClaimDAO.updateBillChargeClaims(con, chargeList, visitId, bill.getBillNo(),
              planIds, false);
        }
      } else {
        chargeDAO.updateChargeAmounts(drCharge);
        billChgClaimDAO.updateBillChargeClaims(con, chargeList, visitId, bill.getBillNo(), planIds,
            false);
      }
      boolean allowZeroClaim = false;
      String visitType = (String) regtrnbean.get("visit_type");
      String allowZeroClaimAmt = (String) consTypeBean.get("allow_zero_claim_amount");
      if (visitType.equalsIgnoreCase(allowZeroClaimAmt) || "b".equals(allowZeroClaimAmt)) {
        allowZeroClaim = true;
      }
      chargeDAO.updateChargeConsultationIdAndHead(drCharges, codeType, allowZeroClaim);
    }
    return null;
  }

  /**
   * Sets the claim amts by retaining patient amt paid.
   *
   * @param drCharge
   *          the dr charge
   * @param existingChrge
   *          the existing chrge
   * @param planIds
   *          the plan ids
   */
  private void setClaimAmtsByRetainingPatientAmtPaid(ChargeDTO drCharge, ChargeDTO existingChrge,
      int[] planIds) {
    BigDecimal patAmtPaid = BigDecimal.ZERO;
    BigDecimal[] exiClaimAmounts = existingChrge.getClaimAmounts();
    BigDecimal[] claimAmounts = drCharge.getClaimAmounts();

    if (planIds.length == 1) {
      patAmtPaid = existingChrge.getAmount().subtract(exiClaimAmounts[0]);
      claimAmounts[0] = drCharge.getAmount().subtract(patAmtPaid);
    } else if (planIds.length == 2) {
      patAmtPaid = existingChrge.getAmount().subtract(exiClaimAmounts[0])
          .subtract(exiClaimAmounts[1]);
      BigDecimal newPatAmt = drCharge.getAmount().subtract(claimAmounts[0])
          .subtract(claimAmounts[1]);

      if (newPatAmt.compareTo(patAmtPaid) >= 0) {
        if (claimAmounts[0].compareTo(claimAmounts[1]) >= 0) {
          claimAmounts[0] = claimAmounts[0].add(newPatAmt.subtract(patAmtPaid));
        } else {
          claimAmounts[1] = claimAmounts[1].add(newPatAmt.subtract(patAmtPaid));
        }
      } else {
        if (claimAmounts[0].compareTo(claimAmounts[1]) >= 0) {
          claimAmounts[0] = claimAmounts[0].subtract(patAmtPaid.subtract(newPatAmt));
        } else {
          claimAmounts[1] = claimAmounts[1].subtract(patAmtPaid.subtract(newPatAmt));
        }
      }
    }
    drCharge.setInsuranceClaimAmount(claimAmounts[0]);
    drCharge.setClaimAmounts(claimAmounts);

  }

  /**
   * Save mrd.
   *
   * @param insertList
   *          the insert list
   * @param updateList
   *          the update list
   * @param deleteList
   *          the delete list
   * @param trtUpdateList
   *          the trt update list
   * @param drgUpdateList
   *          the drg update list
   * @param loincUpdateList
   *          the loinc update list
   * @param consulUpdateList
   *          the consul update list
   * @param consulCodeTypesList
   *          the consul code types list
   * @param encBean
   *          the enc bean
   * @param regBean
   *          the reg bean
   * @param patBean
   *          the pat bean
   * @param drgMap
   *          the drg map
   * @param billChgClaimList
   *          the bill chg claim list
   * @return the map
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws Exception
   *           the exception
   */
  public static Map saveMrd(List<BasicDynaBean> insertList, List<BasicDynaBean> updateList,
      List<BasicDynaBean> deleteList, List<BasicDynaBean> trtUpdateList,
      List<BasicDynaBean> drgUpdateList, List<BasicDynaBean> loincUpdateList,
      List<BasicDynaBean> consulUpdateList, List<Map> consulCodeTypesList, BasicDynaBean encBean,
      BasicDynaBean regBean, BasicDynaBean patBean, Map drgMap,
      List<BasicDynaBean> billChgClaimList, BasicDynaBean patInsPlanBean) 
      throws SQLException, IOException, Exception {
    GenericDAO dao = new GenericDAO("hospital_claim_diagnosis");
    GenericDAO trtDAO = new GenericDAO("bill_charge");
    GenericDAO encDAO = new GenericDAO("patient_registration");
    GenericDAO drugCodeDAO = new GenericDAO("store_sales_details");
    GenericDAO patDAO = new GenericDAO("patient_details");
    GenericDAO loincDAO = new GenericDAO("test_details");
    GenericDAO billChgClaimDAO = new GenericDAO("bill_charge_claim");
    GenericDAO patientInsPlanDAO = new GenericDAO("patient_insurance_plans");
    Map<String, Object> keys = new HashMap<String, Object>();
    Map<String, Object> filterKeys = new HashMap<String, Object>();

    boolean success = true;
    Connection con = null;
    String error = null;
    try {
      con = DataBaseUtil.getConnection(120);
      con.setAutoCommit(false);
      for (BasicDynaBean bean : insertList) {
        if (bean.get("icd_code") == null || bean.get("icd_code").equals("")) {
          continue;
        }
        bean.set("id", DataBaseUtil.getNextSequence("mrd_diagnosis_seq"));
        bean.set("mod_time", DataBaseUtil.getDateandTime());
        success = dao.insert(con, bean);
        if (!success) {
          break;
        }
      }

      for (BasicDynaBean bean : deleteList) {
        boolean dgSuccess = dao.delete(con, "id", bean.get("id"));
        if (!dgSuccess) {
          break;
        }
      }

      for (BasicDynaBean bean : updateList) {
        int returnValue = 0;
        if (bean.get("icd_code") == null || bean.get("icd_code").equals("")) {
          boolean dgSuccess = dao.delete(con, "id", bean.get("id"));
          returnValue = dgSuccess ? 1 : 0;
        } else {
          bean.set("mod_time", DataBaseUtil.getDateandTime());
          bean.set("mod_time", DataBaseUtil.getDateandTime());
          returnValue = dao.updateWithName(con, bean.getMap(), "id");
        }
        if (returnValue == 0) {
          success = false;
        }
        if (!success) {
          break;
        }
      }

      for (BasicDynaBean bean : trtUpdateList) {
        filterKeys.put("bill_no", bean.get("bill_no"));
        filterKeys.put("charge_id", bean.get("charge_id"));
        int updateRows = trtDAO.update(con, bean.getMap(), filterKeys);
        if (updateRows == 0) {
          success = false;
          break;
        }
      }
      if (consulUpdateList != null && consulUpdateList.size() > 0) {
        int index = 0;
        for (BasicDynaBean bean : consulUpdateList) {
          Map consulCodeMap = consulCodeTypesList.get(index++);
          String code = consulCodeMap.get("item_code") == null ? null
              : (String) consulCodeMap.get("item_code");
          String codeType = consulCodeMap.get("code_type") == null ? null
              : (String) consulCodeMap.get("code_type");
          String username = consulCodeMap.get("user_name") == null ? null
              : (String) consulCodeMap.get("user_name");
          Map consulMap = bean.getMap();
          if (consulMap.containsKey("patient_id")) {
            consulMap.remove("patient_id");
          }
          BasicDynaBean consulBean = doctorConsultationDAO.findByKey("consultation_id",
              (Integer) bean.get("consultation_id"));
          success = new MRDUpdateScreenBO().updateDoctorChargesForCode(con, bean,
              (String) consulBean.get("patient_id"), codeType, code, username) == null;
          if (doctorConsultationDAO.updateWithName(con, consulMap, "consultation_id") == 0) {
            success = false;
          }
          if (!success) {
            break;
          }
        }
      }
      for (BasicDynaBean bean : drgUpdateList) {
        if (drugCodeDAO.updateWithName(con, bean.getMap(), "sale_item_id") == 0) {
          success = false;
        }
        if (!success) {
          break;
        }
      }

      for (BasicDynaBean bean : loincUpdateList) {
        if (loincDAO.updateWithName(con, bean.getMap(), "id") == 0) {
          success = false;
        }
        if (!success) {
          break;
        }
      }

      if (null != billChgClaimList && !billChgClaimList.isEmpty() && billChgClaimList.size() > 0) {

        for (BasicDynaBean bcc : billChgClaimList) {
          keys.put("charge_id", bcc.get("charge_id"));
          keys.put("claim_id", bcc.get("claim_id"));
          int updatedRows = billChgClaimDAO.update(con, bcc.getMap(), keys);

          if (updatedRows == 0) {
            success = false;
            break;
          }
        }

      }

      if (encBean != null) {
        if (encDAO.updateWithName(con, encBean.getMap(), "patient_id") == 0) {
          success = false;
        }

      }

      if (regBean != null) {
        if (patientRegistrationDAO.updateWithName(con, regBean.getMap(), "patient_id") == 0) {
          success = false;
        }
      }

      if (patBean != null) {
        if (patDAO.updateWithName(con, patBean.getMap(), "mr_no") == 0) {
          success = false;
        }
      }
      List primarydiagnosisList = null;
      if (regBean != null) {
        primarydiagnosisList = MRDDiagnosisDAO.getPrimaryDiagnosisList(con,
          (String) regBean.get("patient_id"));
      }
      if (primarydiagnosisList != null && primarydiagnosisList.size() > 1) {
        success = false;
        error = "Duplicate Primary Diagnosis";
      }
      if (patInsPlanBean != null) {
        if (patientInsPlanDAO.updateWithName(con, patInsPlanBean.getMap(), 
            "patient_insurance_plans_id") == 0) {
          success = false;
        }
      }

    } finally {
      if (success) {
        con.commit();
      } else {
        con.rollback();
      }
      DataBaseUtil.closeConnections(con, null);

      boolean isDRGRemoved = false;

      if (success && null != drgMap) {
        if (null != drgMap.get("act_rate_plan_item_code")
            && !((String) drgMap.get("act_rate_plan_item_code")).equals("")) {
          Boolean isMarginExists = new DrgUpdateDAO()
              .isMarginDRGExists((String) drgMap.get("bill_no"));
          if (!isMarginExists) {
            new DRGCalculator().addDRG((String) drgMap.get("bill_no"),
                (String) drgMap.get("act_rate_plan_item_code"));
          }
        } else {
          new DRGCalculator().removeDRG((String) drgMap.get("bill_no"));
          new DrgUpdateDAO().lockOrUnlockSaleItems((String) drgMap.get("bill_no"), false);
          new DrgUpdateDAO().includeSaleItemsInClaim((String) drgMap.get("bill_no"));
          isDRGRemoved = true;
        }
      }

      if (regBean != null) {
        String visitId = (regBean.get("patient_id") != null) ? (String) regBean.get("patient_id")
            : null;
        if (visitId != null) {
          List<BasicDynaBean> openBills = BillDAO.getVisitAllOpenBills(null, visitId);
          if (openBills != null && openBills.size() > 0) {
            for (BasicDynaBean billbean : openBills) {
              String billNo = (String) billbean.get("bill_no");
              BillDAO.resetTotalsOrReProcess(billNo, true, false, true);
            }
          }
          new SponsorBO().recalculateSponsorAmount(visitId);
          if (isDRGRemoved) {
            new SponsorDAO().updateSalesBillCharges(visitId);
            new DrgUpdateDAO().lockOrUnlockSaleItems((String) drgMap.get("bill_no"), true);
          }
        }
      }
    }
    Map successMap = new HashMap();
    successMap.put("success", success);
    successMap.put("error", error);

    return successMap;
  }

  /** The Constant VALIDATE_CODE_TYPE_QUERY. */
  private static final String VALIDATE_CODE_TYPE_QUERY = " SELECT * FROM mrd_supported_codes "
      + " WHERE code_category=? AND code_type IN( "
      + " SELECT code_type FROM getItemCodesForCodeType (?,?) WHERE code=? ) ";

  /**
   * Validate code.
   *
   * @param category
   *          the category
   * @param codeType
   *          the code type
   * @param code
   *          the code
   * @param visitType
   *          the visit type
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean validateCode(String category, String codeType, String code,
      String visitType) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    boolean valid = true;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(VALIDATE_CODE_TYPE_QUERY);
      ps.setString(1, category);
      ps.setString(2, codeType);
      ps.setString(3, visitType);
      ps.setString(4, code);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() == 0) {
        valid = false;
      }

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return valid;
  }

  /**
   * Reopen codifiaction.
   *
   * @param bean
   *          the bean
   * @return the int
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static int reopenCodifiaction(BasicDynaBean bean) throws SQLException, IOException {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      return patientRegistrationDAO.updateWithName(con, bean.getMap(), "patient_id");
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant GET_LOINC_CODES. */
  private static final String GET_LOINC_CODES = " SELECT td.test_id,d.test_name, td.prescribed_id,"
      + " td.resultlabel, code_type, result_code, conducted_date, td.test_details_id, "
      + " CASE WHEN tp.conducted='N' THEN 'Not Conducted' "
      + " WHEN tp.conducted='C' OR tp.conducted='Y' THEN 'Conducted' "
      + " WHEN tp.conducted='Cancel' OR tp.conducted='X' THEN 'Cancelled' "
      + " WHEN tp.conducted='P' THEN 'Partially Conducted' "
      + " WHEN tp.conducted='U' THEN 'Condn. Unnecessary'  END AS  conducted_status "
      + " FROM tests_prescribed tp JOIN patient_registration pr on tp.pat_id=pr.patient_id "
      + " JOIN test_details td ON td.prescribed_id=tp.prescribed_id "
      + " JOIN tests_conducted tc ON (tc.prescribed_id=tp.prescribed_id) "
      + " JOIN diagnostics d ON td.test_id=d.test_id "
      + " WHERE pr.patient_id = ? AND coalesce(resultlabel, '') !='' "
      + "   AND tp.conducted NOT IN ('RBS','RAS') AND td.test_detail_status != 'A'  ";

  /**
   * Gets the lion inc codes.
   *
   * @param visitId
   *          the visit id
   * @param codeType
   *          the code type
   * @return the lion inc codes
   * @throws SQLException
   *           the SQL exception
   */
  public List getLionIncCodes(String visitId, String codeType) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      if (codeType != null && !codeType.equals("")) {
        ps = con.prepareStatement(GET_LOINC_CODES + " AND code_type = ?");
      } else {
        ps = con.prepareStatement(GET_LOINC_CODES);
      }
      int idx = 1;
      ps.setString(idx++, visitId);
      if (codeType != null && !codeType.equals("")) {
        ps.setString(idx++, codeType);
      }
      List list = DataBaseUtil.queryToDynaList(ps);
      return list;

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the report content string writer.
   *
   * @param patientId
   *          the patient id
   * @param format
   *          the format
   * @param map
   *          the map
   * @return the report content string writer
   * @throws Exception
   *           the exception
   */
  public static StringWriter getReportContentStringWriter(String patientId, String format, Map map)
      throws Exception {

    Map patient = VisitDetailsDAO.getPatientVisitDetailsMap(patientId);

    String[] categories = null;
    String[] coType = null;
    if (map != null) {
      categories = (String[]) map.get("category");
      coType = ((String[]) map.get("code_type"));
    }
    String codeType = coType != null ? coType[0] : null;
    List patientDiagnosis = null;
    List patientTrtCodes = null;
    List patientDrgCodes = null;
    List loincCodes = null;
    Map patientEncCodes = null;
    List patientDrConsultn = null;
    boolean includeDiagnosis = false;
    boolean includeTreatment = false;
    boolean includeEncounter = false;
    boolean includeObservations = false;
    boolean includeEandMCodes = false;
    boolean includeDRGCodes = false;

    if (categories == null || (categories.length == 1 && categories[0].equals(""))) {
      includeDiagnosis = true;
      includeTreatment = true;
      includeEncounter = true;
      includeObservations = true;
      includeEandMCodes = true;
      includeDRGCodes = patient.get("use_drg") != null
          && ((String) patient.get("use_drg")).equals("Y");
    } else if (!(categories.length == 1 && categories[0].equals(""))) {
      for (int i = 0; i < categories.length; i++) {
        if (categories[i].equals("diagnosis")) {
          includeDiagnosis = true;
        } else if (categories[i].equals("encounter")) {
          includeEncounter = true;
        } else if (categories[i].equals("treatment")) {
          includeTreatment = true;
        } else if (categories[i].equals("observations")) {
          includeObservations = true;
        } else if (categories[i].equals("eandmcodes")) {
          includeEandMCodes = true;
        } else if (categories[i].equals("drgcodes")
            && (patient.get("use_drg") != null && ((String) patient.get("use_drg")).equals("Y"))) {
          includeDRGCodes = true;
        }
      }
    }

    if (includeDiagnosis) {
      patientDiagnosis = new MRDUpdateScreenBO().getMrdDiagnosisList(patientId, codeType);
    }
    if (includeTreatment) {
      patientTrtCodes = new MRDUpdateScreenBO().getMrdTreatmentCodesList(patientId, codeType);
    }
    if (includeEncounter) {
      patientEncCodes = new MRDUpdateScreenBO().getEncounterCodes(patientId);
    }
    if (includeDRGCodes) {
      patientDrgCodes = new MRDUpdateScreenBO().getPatientDRGCodes(patientId);
    }
    if (includeObservations) {
      loincCodes = new MRDUpdateScreenBO().getLionIncCodes(patientId, codeType);
    }
    if (includeEandMCodes) {
      patientDrConsultn = new MRDUpdateScreenBO().getMrdDrConsultationList(patientId, null);
    }

    Template template = AppInit.getFmConfig().getTemplate("ICDPatientReport.ftl");
    Map ftlParams = new HashMap();
    ftlParams.put("patient", patient);
    ftlParams.put("patientDiagnosis", patientDiagnosis);
    ftlParams.put("patientEncCodes", patientEncCodes);
    ftlParams.put("patientDrConsultn", patientDrConsultn);
    ftlParams.put("patientDrgCodes",
        patientDrgCodes != null ? ConversionUtils.copyListDynaBeansToMap(patientDrgCodes) : null);
    ftlParams.put("patientTrtCodes",
        patientTrtCodes != null ? ConversionUtils.copyListDynaBeansToMap(patientTrtCodes) : null);
    ftlParams.put("patientLoincCodes",
        loincCodes != null ? ConversionUtils.copyListDynaBeansToMap(loincCodes) : null);
    ftlParams.put("format", format);
    ftlParams.put("category", categories != null ? java.util.Arrays.asList(categories) : null);
    ftlParams.put("codeType", codeType);

    BasicDynaBean dischargeDetails = new DischargeSummaryDAOImpl().getDischargeDetails(patientId);
    if (dischargeDetails != null) {
      ftlParams.put("dis", dischargeDetails.getMap());
    }

    StringWriter writer = new StringWriter();
    try {
      template.process(ftlParams, writer);
    } catch (TemplateException te) {
      throw te;
    }
    return writer;
  }

  /** The Constant OBSERVATION_CODE_TYPES. */
  private static final String OBSERVATION_CODE_TYPES = " SELECT code_type FROM mrd_supported_codes"
      + " WHERE code_category = 'Observations' ";

  /**
   * Gets the observation list codes.
   *
   * @return the observation list codes
   * @throws SQLException
   *           the SQL exception
   */
  public List getObservationListCodes() throws SQLException {
    return DataBaseUtil.simpleQueryToArrayList(OBSERVATION_CODE_TYPES);
  }

  /** The Constant MRD_OBSERVATIONS_WITHOUT_PRESENTING_COMPLAINT. */
  private static final String MRD_OBSERVATIONS_WITHOUT_PRESENTING_COMPLAINT = " SELECT "
      + " mo.charge_id, mo.observation_type, mo.code, mo.value, mo.value_type, mo.document_id,"
      + " mo.value_editable, code_desc, mo.sponsor_id FROM mrd_observations mo "
      + " LEFT JOIN mrd_codes_master mcm ON (mcm.code_type=mo.observation_type"
      + " AND mcm.code=mo.code) WHERE mo.charge_id = ? AND mo.code != 'Presenting-Complaint' "
      + " ORDER BY observation_id ";

  /** The Constant MRD_OBSERVATIONS. */
  private static final String MRD_OBSERVATIONS = " SELECT mo.charge_id, mo.observation_type, "
      + " mo.code, mo.value, mo.value_type, mo.value_editable, code_desc, "
      + " mo.sponsor_id, mo.document_id FROM mrd_observations mo "
      + " LEFT JOIN mrd_codes_master mcm ON (mcm.code_type=mo.observation_type "
      + " AND mcm.code=mo.code) WHERE mo.charge_id = ? " + " ORDER BY observation_id ";

  /**
   * Gets the mrd observations.
   *
   * @param chargeId
   *          the charge id
   * @return the mrd observations
   * @throws SQLException
   *           the SQL exception
   */
  public List getMrdObservations(String chargeId) throws SQLException {
    Integer centerId = RequestContext.getCenterId();
    String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
    String eclaimXMLSchema = HealthAuthorityPreferencesDAO
        .getHealthAuthorityPreferences(healthAuthority).getHealth_authority();
    if (!eclaimXMLSchema.equals("DHA")) {
      return DataBaseUtil.queryToDynaList(MRD_OBSERVATIONS_WITHOUT_PRESENTING_COMPLAINT, chargeId);
    } else {
      return DataBaseUtil.queryToDynaList(MRD_OBSERVATIONS, chargeId);
    }
  }

  /** The Constant TESTS_WITH_LOINC_TEST_RESULTS. */
  private static final String TESTS_WITH_LOINC_TEST_RESULTS = " SELECT * FROM test_results_master"
      + "  trm WHERE code_type = 'LOINC' AND exists "
      + " (SELECT * FROM test_results_center trc WHERE trc.resultlabel_id=trm.resultlabel_id AND"
      + "  (center_id=0 or center_id=?)) ORDER BY test_id ";

  /**
   * Gets the tests with lionc test results.
   *
   * @param centerId
   *          the center id
   * @return the tests with lionc test results
   * @throws SQLException
   *           the SQL exception
   */
  public List getTestsWithLioncTestResults(Integer centerId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(TESTS_WITH_LOINC_TEST_RESULTS);
      ps.setInt(1, centerId == null ? -1 : centerId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_MARDRG_CODE. */
  /* For getting drg code and description of migrated DRG patient */
  private static final String GET_MARDRG_CODE = " SELECT b.bill_no AS drg_bill_no, b.status AS "
      + " drg_bill_status, bc.charge_id AS drg_charge_id,"
      + " bc.act_rate_plan_item_code AS drg_code, bc.code_type AS drg_code_type,"
      + " COALESCE((SELECT code_desc from getItemCodesForCodeType('IR-DRG', b.visit_type) mcm "
      + " WHERE (bc.act_rate_plan_item_code::text = mcm.code)  LIMIT 1), '') AS drg_description "
      + " FROM bill b "
      + " LEFT JOIN bill_charge bc ON (bc.charge_head = 'MARDRG' AND bc.bill_no = b.bill_no) "
      + " WHERE b.bill_type = 'C' AND b.is_primary_bill = 'Y' AND "
      + " b.status != 'X' AND b.is_tpa AND b.visit_id=?  ";

  /**
   * Gets the MARDRG code.
   *
   * @param patientId
   *          the patient id
   * @return the MARDRG code
   * @throws SQLException
   *           the SQL exception
   */
  public Map getMARDRGCode(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_MARDRG_CODE);
      ps.setString(1, patientId);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list != null && list.size() > 0) {
        return ((BasicDynaBean) list.get(0)).getMap();
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_DRG_CODE. */
  private static final String GET_DRG_CODE = " SELECT b.bill_no AS drg_bill_no, b.status "
      + " AS drg_bill_status, bc.charge_id AS drg_charge_id,"
      + " bc.act_rate_plan_item_code AS drg_code, " + " bc.code_type AS drg_code_type,"
      + " COALESCE((SELECT code_desc from getItemCodesForCodeType('IR-DRG', b.visit_type) mcm "
      + " WHERE (bc.act_rate_plan_item_code::text = mcm.code)  LIMIT 1), '') AS drg_description "
      + " FROM bill b "
      + " LEFT JOIN bill_charge bc ON (bc.charge_head = 'BPDRG' AND bc.bill_no = b.bill_no) "
      + " WHERE b.bill_type = 'C' AND b.is_primary_bill = 'Y' AND b.status != 'X'"
      + " AND b.is_tpa AND b.visit_id=?  ";

  /**
   * Gets the DRG code.
   *
   * @param patientId
   *          the patient id
   * @return the DRG code
   * @throws SQLException
   *           the SQL exception
   */
  public Map getDRGCode(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_DRG_CODE);
      ps.setString(1, patientId);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list != null && list.size() > 0) {
        return ((BasicDynaBean) list.get(0)).getMap();
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the mrd drg codes list.
   *
   * @param patientId
   *          the patient id
   * @return the mrd drg codes list
   * @throws SQLException
   *           the SQL exception
   */
  public List getMrdDrgCodesList(String patientId) throws SQLException {
    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getConnection();
    try {
      ps = con.prepareStatement(GET_DRG_CODE);
      ps.setString(1, patientId);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_PERDIEM_CODE. */
  private static final String GET_PERDIEM_CODE = " SELECT b.bill_no AS perdiem_bill_no,"
      + "  b.status AS perdiem_bill_status, bc.charge_id AS perdiem_charge_id,"
      + " bc.act_rate_plan_item_code AS per_diem_code, " + " bc.code_type AS per_diem_code_type,"
      + " pm.per_diem_description FROM bill b "
      + " LEFT JOIN bill_charge bc ON (bc.charge_head = 'MARPDM' AND bc.bill_no = b.bill_no) "
      + " LEFT JOIN per_diem_codes_master pm ON (pm.per_diem_code = bc.act_rate_plan_item_code) "
      + " WHERE b.bill_type = 'C' AND b.is_primary_bill = 'Y' AND b.status != 'X' "
      + " AND b.is_tpa AND b.visit_id=?  ";

  /**
   * Gets the perdiem code.
   *
   * @param patientId
   *          the patient id
   * @return the perdiem code
   * @throws SQLException
   *           the SQL exception
   */
  public Map getPerdiemCode(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_PERDIEM_CODE);
      ps.setString(1, patientId);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list != null && list.size() > 0) {
        return ((BasicDynaBean) list.get(0)).getMap();
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the prev mrd diagnosis list for current visit. First brings coder entered diagnoses. If
   * not found, then brings doctor entered diagnoses.
   *
   * @param visitId
   *          the visit id
   * @param codeType
   *          the code type
   * @return the prev mrd diagnosis list for current visit
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public List getPrevMrdDiagnosisListForCurrentVisit(String visitId, String codeType)
      throws SQLException {

    try (Connection con = DataBaseUtil.getConnection()) {
      BasicDynaBean visitDetails = VisitDetailsDAO.getVisitDetailsWithMainvisitdetails(visitId);
      String mainVisitId = null;
      if (visitDetails != null && visitDetails.get("doctor") != null) {
        String datetime = visitDetails.get("mainvisit_reg_date").toString() + " "
            + visitDetails.get("mainvisit_reg_time").toString();
        BasicDynaBean prevMainVisits = VisitDetailsDAO.getPatientCurrentVisitsPreviousMainVisits(
            (String) visitDetails.get("mr_no"), datetime, (String) visitDetails.get("doctor"));
        if (prevMainVisits != null) {
          mainVisitId = (String) prevMainVisits.get("patient_id");
        }
      }

      /*
       * first check if the diagnosis has been copied to hospital_claim_diagnosis(coder edited
       * diagnosis) if not copied, then bring diagnosis from mrd_diagnosis table
       */
      List<BasicDynaBean> result = getMrdCoderDiagnosis(con, mainVisitId, codeType);
      if (result != null && !result.isEmpty()) {
        return result;
      } else {
        return getMrdDiagnosis(con, mainVisitId, codeType);
      }
    }
  }

  /**
   * Gets the mrd diagnosis.
   *
   * @param con
   *          the connection. If not provided creates a new one.
   * @param mainVisitId
   *          the main visit id
   * @param codeType
   *          the code type
   * @return the mrd diagnosis
   * @throws SQLException
   *           the SQL exception
   */
  // brings diagnosis details from mrd_diagnosis table
  public List getMrdDiagnosis(Connection con, String mainVisitId, String codeType)
      throws SQLException {
    // return diagnosis using the mrd_diagnosis table.
    PreparedStatement ps = null;
    if (con == null) {
      con = DataBaseUtil.getConnection();
    }
    try {
      if (codeType != null && !codeType.equals("")) {
        ps = con.prepareStatement(MRD_DIAGNOSIS_QUERY + "AND mc.code_type = ?");
      } else {
        ps = con.prepareStatement(MRD_DIAGNOSIS_QUERY);
      }
      int idx = 1;
      ps.setString(idx++, mainVisitId);
      if (codeType != null && !codeType.equals("")) {
        ps.setString(idx++, codeType);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
        if (!con.isClosed()) {
          DataBaseUtil.closeConnections(con, ps);
        }
      }
    }
  }

  // brings diagnosis details from mrd_diagnosis table
  private List<BasicDynaBean> getMrdCoderDiagnosis(Connection con, String visitId, String codeType)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      if (codeType != null && !codeType.equals("")) {
        ps = con.prepareStatement(MRD_CODER_DIAGNOSIS_QUERY + "AND hcd.code_type = ?");
      } else {
        ps = con.prepareStatement(MRD_CODER_DIAGNOSIS_QUERY);
      }
      int idx = 1;
      ps.setString(idx++, visitId);
      if (codeType != null && !codeType.equals("")) {
        ps.setString(idx++, codeType);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }


  private static final String TRANSFER_HOSPITAL_SEARCH_QUERY = "SELECT * from transfer_hospitals "
      + " WHERE LOWER(transfer_hospital_name) LIKE ? " 
      + " ORDER BY transfer_hospital_name LIMIT ?";

  /**
   * Gets the transfer hospitals.
   *
   * @param searchQuery
   *          the searchQuery
   * @param limit
   *          the limit
   * @return the transfer hospitals
   * @throws SQLException
   *           the SQL exception
  */

  public static List getTransferHospitals(String searchQuery ,Integer limit) throws SQLException {

    List transHosp = null;
    String lowerSearchQuery = "";
    if (searchQuery != null) {
      lowerSearchQuery = searchQuery.toLowerCase();
    }
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
      PreparedStatement ps = con.prepareStatement(TRANSFER_HOSPITAL_SEARCH_QUERY);) { 
      ps.setString(1, "%" + lowerSearchQuery + "%");
      ps.setInt(2, limit);
      transHosp = DataBaseUtil.queryToDynaList(ps);
    }
    return transHosp;
  }

}
