package com.insta.hms.integration.insurance.submission;

import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class SelfPaySubmissionRepository.
 */
@Repository
public class SelfPaySubmissionRepository extends MasterRepository<String> {

  /**
   * Instantiates a new self pay submission repository.
   */
  public SelfPaySubmissionRepository() {
    super("selfpay_submission_batch", "selfpay_batch_id", null, 
        new String[] { "selfpay_batch_id" });
  }

  /** The get selfpay bills. */
  private static final String GET_SELFPAY_BILLS = "SELECT b.bill_no, pr.visit_type, "
      + "b.account_group, pd.government_identifier "
      + "FROM bill b "
      + "JOIN patient_registration pr ON (b.visit_id = pr.patient_id) "
      + "JOIN patient_details pd ON (pr.mr_no = pd.mr_no) "
      + "LEFT JOIN tpa_master tm ON (tm.tpa_id = pr.primary_sponsor_id) "
      + "LEFT JOIN sponsor_type st ON (tm.sponsor_type_id = st.sponsor_type_id) "
      + "WHERE (b.is_tpa = FALSE OR st.is_selfpay_sponsor = TRUE) "
      + "AND b.selfpay_batch_id = 0 ";

  /**
   * Search bills for submission.
   *
   * @param filter the filter
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> searchBillsForSubmission(Map filter) throws SQLException {
    StringBuilder query = new StringBuilder();
    query.append(GET_SELFPAY_BILLS);

    Date[] dischargeDate = (Date[]) filter.get("discharge_date_time");
    Date[] regDateTime = (Date[]) filter.get("reg_date_time");
    Integer accountGroup = (Integer) filter.get("account_group");

    MapSqlParameterSource params = new MapSqlParameterSource();
    // add registration date filters
    if (regDateTime != null && regDateTime.length > 0 && regDateTime[0] != null
        && regDateTime[1] != null) {
      query.append(" AND pr.reg_date >= (:registration_date_start) ");
      query.append(" AND pr.reg_date <= (:registration_date_end) ");
      params.addValue("registration_date_start", regDateTime[0]);
      params.addValue("registration_date_end", regDateTime[1]);
    }
    // add discharge date filters
    if (dischargeDate != null && dischargeDate.length > 0 && dischargeDate[0] != null
        && dischargeDate[1] != null) {
      query.append(" AND pr.discharge_date >= (:discharge_date_start) ");
      query.append(" AND pr.discharge_date <= (:discharge_date_end) ");
      params.addValue("discharge_date_start", dischargeDate[0]);
      params.addValue("discharge_date_end", dischargeDate[1]);
    }
    if (accountGroup != null) {
      query.append(" AND b.account_group = (:accountGroup) ");
      params.addValue("accountGroup", accountGroup);
    }
    Integer centerId = (Integer) filter.get("center_id");
    if (centerId != null) {
      query.append(" AND pr.center_id = (:centerId) ");
      params.addValue("centerId", centerId);
    }
    String visitType = (String) filter.get("visit_type");
    if (visitType != null && ("i".equals(visitType) || "o".equals(visitType))) {
      query.append(" AND pr.visit_type = (:visitType) ");
      params.addValue("visitType", visitType);
    }
    String[] codificationStatus = (String[]) filter.get("codification_status");
    if (codificationStatus != null && codificationStatus.length > 0
        && !codificationStatus[0].isEmpty()) {
      query.append(" AND pr.codification_status IN (:codification_status) ");
      params.addValue("codification_status", Arrays.asList(codificationStatus));
    }

    String[] departmentIds = (String[]) filter.get("dept_id");
    if (departmentIds != null && departmentIds.length > 0 && !departmentIds[0].isEmpty()) {
      query.append(" AND pr.dept_name IN (:dept_name) ");
      params.addValue("dept_name", Arrays.asList(departmentIds));
    }

    Boolean ignoreOpenBills = (Boolean) filter.get("ignore_open_bills");
    if (ignoreOpenBills != null && ignoreOpenBills) {
      query.append("AND pr.patient_id NOT IN ( "
          + "        SELECT pr.patient_id FROM patient_registration pr "
          + "          JOIN bill b ON (b.visit_id = pr.patient_id) "
          + "        WHERE b.status = 'A' ");

      // add below filters to subquery to make it faster
      if (regDateTime != null && regDateTime.length == 2 && regDateTime[0] != null
          && regDateTime[1] != null) {
        query.append(" AND pr.reg_date >= (:registration_date_start) ");
        query.append(" AND pr.reg_date <= (:registration_date_end) ");
        params.addValue("registration_date_start", regDateTime[0]);
        params.addValue("registration_date_end", regDateTime[1]);
      } else if (dischargeDate != null && dischargeDate.length == 2 && dischargeDate[0] != null
          && dischargeDate[1] != null) {
        query.append(" AND pr.discharge_date >= (:discharge_date_start) ");
        query.append(" AND pr.discharge_date <= (:discharge_date_end) ");
        params.addValue("discharge_date_start", dischargeDate[0]);
        params.addValue("discharge_date_end", dischargeDate[1]);
      }
      // closing bracket after adding filters
      query.append(" ) ");
    }

    return DatabaseHelper.queryToDynaList(query.toString(), params);
  }

  /** The get selfpay bill details new. */
  private static final String GET_SELFPAY_BILL_DETAILS_NEW = "SELECT  "
      + "       ssb.selfpay_batch_id, "
      + "        b.bill_no, "
      + "        b.status, "
      + "       ppd.member_id, "
      + "       CASE "
      + "             WHEN (b.is_tpa = FALSE) THEN 'SELFPAY' "
      + "             WHEN (st.is_selfpay_sponsor = TRUE) THEN 'ProFormaPayer' "
      + "       END AS payer_id, "
      + "       ppd.policy_number, "
      + "       pip.patient_policy_id, "
      + "        st.is_selfpay_sponsor, "
      + "       pip.plan_id, "
      + "       pr.category_id, "
      + "       pip.priority, "
      + "       pip.sponsor_id, "
      + "       hit.health_authority AS receiver_id, "
      + "        tm.tpa_name, "
      + "        pip.insurance_co, "
      + "        icm.insurance_co_name, "
      + "         agm.account_group_id, "
      + "         agm.account_group_name, "
      + "         CASE "
      + "             WHEN ssb.account_group_id != 0 THEN agm.account_group_service_reg_no "
      + "             ELSE hcm.hospital_center_service_reg_no "
      + "         END AS provider_id, "
      + "         CASE "
      + "             WHEN pd.government_identifier IS NULL "
      + "                  OR pd.government_identifier = '' THEN COALESCE(gim.identifier_type, '') "
      + "             ELSE pd.government_identifier "
      + "         END AS emirates_id_number, "
      + "         SUM(total_amount) AS gross, "
      + "         SUM(total_amount-(total_claim + total_claim_return)) AS patient_share, "
      + "         SUM(total_claim + total_claim_return) AS net, "
      + "         SUM(insurance_deduction) AS deduction, "
      + "         pip.use_drg, "
      + "         pip.use_perdiem, "
      + "         agm.account_group_service_reg_no AS facility_id, "
      + "         pr.encounter_type, "
      + "         pr.encounter_start_type, "
      + "         pr.encounter_end_type, "
      + "         pr.mr_no AS patient_id, "
      + "         pr.classification, "    
      + "         b.visit_id, "
      + "         b.is_tpa, "
      + "         pr.primary_sponsor_id, "
      + "         pr.visit_type, "
      + "         trim(COALESCE(pip.prior_auth_id, '')) AS prior_auth_id, "
      + "         to_char((pr.reg_date||' '||pr.reg_time) :: TIMESTAMP WITHOUT TIME ZONE, "
      + "         'dd/MM/yyyy hh24:mi') AS start_date, "
      + "         to_char((pr.discharge_date||' '||pr.discharge_time) "
      + "             :: TIMESTAMP WITHOUT TIME ZONE, "
      + "         'dd/MM/yyyy hh24:mi') AS end_date, "
      + "         to_char(CURRENT_TIMESTAMP::TIMESTAMP, 'dd-MM-yyyy hh24:mi') AS todays_date, "
      + "         icm.insurance_co_id, "
      + "         etc.encounter_type_desc, "
      + "         est.code_desc AS encounter_start_type_desc, "
      + "         eet.code_desc AS encounter_end_type_desc, "
      + "         tsrc.transfer_hospital_service_regn_no AS source_service_regn_no, "
      + "         tdest.transfer_hospital_service_regn_no AS destination_service_regn_no, "
      + "         pd.patient_name, "
      + "         pd.middle_name, "
      + "         pd.last_name, "
      + "         pd.patient_gender, "
      + "         pd.salutation, "
      + "         pd.custom_list1_value AS nationality, "
      + "         to_char(coalesce(pd.expected_dob, pd.dateofbirth), 'dd/MM/yyyy') "
      + "             AS date_of_birth, "
      + "         icam.category_name AS package_name, "
      + "         CASE "
      + "             WHEN ppd.policy_validity_start IS NOT NULL "
      + "                 THEN to_char(ppd.policy_validity_start, 'dd/MM/yyyy') "
      + "             ELSE '' "
      + "         END AS policy_validity_start, "
      + "         CASE "
      + "             WHEN ppd.policy_validity_end IS NOT NULL "
      + "                 THEN to_char(ppd.policy_validity_end, 'dd/MM/yyyy') "
      + "             ELSE '' "
      + "         END AS policy_validity_end, "
      + "         pdd.doc_id, "
      + "         COALESCE(pdoc.content_type, '') AS card_type, "
      + "         COALESCE(pdoc.original_extension, '') AS card_ext, "
      + "         pdd.doc_name AS card_comment, "
      + "         ssb.center_id AS batch_center_id, "
      + "         ipm.require_pbm_authorization "
      + "FROM bill b "
      + "JOIN selfpay_submission_batch ssb ON (ssb.selfpay_batch_id = b.selfpay_batch_id) "
      + "LEFT JOIN account_group_master agm ON (agm.account_group_id = ssb.account_group_id) "
      + "LEFT JOIN hospital_center_master hcm ON (hcm.center_id = ssb.center_id) "
      + "JOIN patient_registration pr ON (pr.patient_id = b.visit_id) "
      + "LEFT JOIN patient_insurance_plans pip ON (pr.patient_id = pip.patient_id) "
      + "JOIN patient_details pd ON (pd.mr_no = pr.mr_no) "
      + "LEFT JOIN tpa_master tm ON (tm.tpa_id = pip.sponsor_id) "
      + " "
      + "LEFT JOIN sponsor_type st ON (st.sponsor_type_id = tm.sponsor_type_id) "
      + "LEFT JOIN ha_tpa_code hit ON(hit.tpa_id = tm.tpa_id "
      + "                             AND hit.health_authority = 'HAAD') "
      + "LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = pip.insurance_co) "
      + " "
      + "LEFT JOIN ha_ins_company_code hic ON(hic.insurance_co_id=icm.insurance_co_id "
      + "                                     AND hic.health_authority = 'HAAD') "
      + "LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = pip.plan_id) "
      + "LEFT JOIN insurance_category_master icam ON (icam.category_id = pr.category_id) "
      + "LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id) "
      + "LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pip.patient_policy_id) "
      + "LEFT JOIN plan_docs_details pdd ON ppd.patient_policy_id = pdd.patient_policy_id "
      + "LEFT JOIN patient_documents pdoc ON (pdoc.doc_id = pdd.doc_id) "
      + "LEFT JOIN encounter_type_codes etc ON (etc.encounter_type_id = encounter_type) "
      + "LEFT JOIN encounter_start_types est ON (est.code = pr.encounter_start_type::text) "
      + "LEFT JOIN encounter_end_types eet ON (eet.code = pr.encounter_end_type::text) "
      + "LEFT JOIN transfer_hospitals tsrc ON (tsrc.transfer_hospital_id = pr.transfer_source) "
      + "LEFT JOIN transfer_hospitals tdest "
      + "    ON (tdest.transfer_hospital_id = pr.transfer_destination) "
      + "WHERE ssb.selfpay_batch_id = (:selfpay_batch_id) " 
      + "GROUP BY  " 
      + "         hit.health_authority, "
      + "         b.is_tpa, " + "         b.bill_no, " + "         st.sponsor_type_id, "
      + "         ssb.selfpay_batch_id, " + "         ppd.member_id, "
      + "         ppd.policy_number, " + "         hit.tpa_code, " + "         tm.tpa_name, "
      + "         pr.primary_sponsor_id, " + "         hic.insurance_co_code, "
      + "         icm.insurance_co_name, " + "         ssb.account_group_id, "
      + "         agm.account_group_id, " + "         agm.account_group_name, "
      + "         agm.account_group_service_reg_no, " + "         hcm.center_id, "
      + "         hcm.center_name, " + "         hcm.hospital_center_service_reg_no, "
      + "         pr.encounter_type, " + "         pip.use_drg, " + "         pip.use_perdiem, "
      + "         pip.patient_policy_id, " + "         pr.mr_no, " + "         b.visit_id, "
      + "         pr.op_type, " + "         pip.plan_id, " + "         pr.category_id, "
      + "         pip.priority, " + "         pip.sponsor_id, " + "         pip.insurance_co, "
      + "         pd.patient_name, " + "         pd.middle_name, " + "         pd.last_name, "
      + "         pd.patient_gender, " + "         pd.salutation, "
      + "         pd.custom_list1_value, " + "         pd.expected_dob, "
      + "         pd.dateofbirth, " + "         pr.patient_id, " + "         pr.visit_type, "
      + "         pip.prior_auth_id, " + "         pr.reg_date, " + "         pr.reg_time, "
      + "         pr.discharge_date, " + "         pr.discharge_time, "
      + "         pr.encounter_start_type, " + "         pr.encounter_end_type, "
      + "         tsrc.transfer_hospital_service_regn_no, "
      + "         tdest.transfer_hospital_service_regn_no, "
      + "         pd.government_identifier, " + "         gim.identifier_type, "
      + "         icm.insurance_co_id, " + "         etc.encounter_type_desc, "
      + "         est.code_desc, " + "         b.status, " + "         eet.code_desc, "
      + "         ipm.plan_name, " + "         icam.category_name, "
      + "         ppd.policy_validity_start, " + "         ppd.policy_validity_end, "
      + "         pdd.doc_id, " + "         pdoc.content_type, "
      + "         pdoc.original_extension, " + "         pdd.doc_name, "
      + "         ssb.center_id, " + "         ipm.require_pbm_authorization ";

  /**
   * Gets the bill details.
   *
   * @param selfpayBatchId the selfpay batch id
   * @return the bill details
   */
  public List<BasicDynaBean> getBillDetails(Integer selfpayBatchId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("selfpay_batch_id", selfpayBatchId);
    return DatabaseHelper.queryToDynaList(GET_SELFPAY_BILL_DETAILS_NEW,
        params);
  }

  /** The get haad account xml header. */
  private static String GET_HAAD_ACCOUNT_XML_HEADER = "SELECT account_group_id, "
      + "account_group_name, "
      + " account_group_service_reg_no AS provider_id, '' AS eclaim_xml_schema, "
      + " 'HAAD' as receiver_id, "
      + " to_char(current_timestamp::timestamp, 'dd/MM/yyyy hh24:mi') AS todays_date, "
      + " 0 as claims_count, 'Y' AS testing, '' AS from_date, '' AS to_date, '' AS operation "
      + " FROM account_group_master WHERE account_group_id = "
      + " (SELECT account_group_id FROM selfpay_submission_batch "
      + " WHERE selfpay_batch_id = ?) ";

  /** The get haad hospital center xml header. */
  private static String GET_HAAD_HOSPITAL_CENTER_XML_HEADER = "SELECT center_id, center_name, "
      + " hospital_center_service_reg_no AS provider_id, '' AS eclaim_xml_schema, "
      + " 'HAAD' as receiver_id, "
      + " to_char(current_timestamp::timestamp, 'dd/MM/yyyy hh24:mi') AS todays_date, "
      + " 0 as claims_count, 'Y' AS testing, '' AS from_date, '' AS to_date, '' AS operation "
      + " FROM hospital_center_master WHERE center_id = "
      + " (SELECT center_id FROM selfpay_submission_batch WHERE selfpay_batch_id= ?) ";


  /**
   * Gets the haad xml header fields.
   *
   * @param submissionBean the submission bean
   * @return the haad xml header fields
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getHaadXmlHeaderFields(BasicDynaBean submissionBean) {
    if (submissionBean != null) {
      Integer submissionBatchId = (Integer) submissionBean.get("selfpay_batch_id");
      if (submissionBean.get("account_group_id") != null
          && ((Integer) submissionBean.get("account_group_id")).intValue() != 0) {
        return DatabaseHelper.queryToDynaBean(GET_HAAD_ACCOUNT_XML_HEADER,
            new Object[] { submissionBatchId });
      } else if (submissionBean.get("center_id") != null) {
        return DatabaseHelper.queryToDynaBean(GET_HAAD_HOSPITAL_CENTER_XML_HEADER,
            new Object[] { submissionBatchId });
      }
    }
    return null;
  }

  /** The selfpay submission query. */
  private static String SELFPAY_SUBMISSION_QUERY = "";

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterRepository#getSearchQuery()
   */
  @Override
  public SearchQuery getSearchQuery() {
    String selfpaySubmissionTables = "FROM " + SELFPAY_SUBMISSION_QUERY;
    return new SearchQuery(selfpaySubmissionTables);
  }

  /** The Constant SEARCH_FIELDS. */
  private static final String SEARCH_FIELDS = " SELECT *  ";

  /** The Constant SEARCH_COUNT. */
  private static final String SEARCH_COUNT = " SELECT count(*) ";

  /** The Constant SEARCH_TABLE. */
  private static final String SEARCH_TABLE = " FROM ( SELECT "
      + "ssb.selfpay_batch_id::text as batch_id, "
      + "ssb.selfpay_status as status, ssb.visit_type, "
      + " ssb.created_at, ssb.submission_date, agm.account_group_service_reg_no, "
      + "ssb.processing_status, "
      + " hc.hospital_center_service_reg_no, ssb.account_group_id, agm.account_group_name, "
      + "ssb.center_id, hc.center_name "
      + " FROM selfpay_submission_batch ssb "
      + " LEFT JOIN account_group_master agm ON (agm.account_group_id = ssb.account_group_id)"
      + " LEFT JOIN hospital_center_master hc ON(hc.center_id = ssb.center_id) ) AS foo ";

  /**
   * List submission details.
   *
   * @param parameters the parameters
   * @param listingParameters the listing parameters
   * @param accGrpFilter the acc grp filter
   * @param centerFilter the center filter
   * @return the paged list
   */
  public PagedList listSubmissionDetails(Map<String, String[]> parameters,
      Map<LISTING, Object> listingParameters, List<String> accGrpFilter, 
      List<String> centerFilter) {
    SearchQueryAssembler qb = null;

    qb = new SearchQueryAssembler(SEARCH_FIELDS, SEARCH_COUNT, SEARCH_TABLE, listingParameters);
    qb.addFilterFromParamMap(parameters);
    String[] accountGroup = accGrpFilter.toArray(new String[accGrpFilter.size()]);
    String[] centerId = centerFilter.toArray(new String[centerFilter.size()]);
    String query = "";

    if (centerId != null && centerId.length > 0) {
      query = query + " center_id IN (";
      for (int i = 0; i < centerId.length; i++) {
        query += centerId[0];
        if (i < centerId.length - 1) {
          query += ",";
        }
      }
      query += ") ";
    }

    if ((centerId != null && centerId.length > 0)
        && (accountGroup != null && accountGroup.length > 0)) {
      query += " OR ";
    }

    if (accountGroup != null && accountGroup.length > 0) {
      query = query + " account_group_id IN (";
      for (int i = 0; i < accountGroup.length; i++) {
        query += accountGroup[i];
        if (i < accountGroup.length - 1) {
          query += ",";
        }
      }
      query += ") ";
    }

    if (!query.equals("")) {
      qb.appendToQuery(" ( " + query + " ) ");
    }
    qb.build();

    return qb.getMappedPagedList();
  }
}
