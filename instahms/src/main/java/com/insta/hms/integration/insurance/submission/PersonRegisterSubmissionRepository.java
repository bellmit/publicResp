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
 * The Class PersonRegisterSubmissionRepository.
 */
@Repository
public class PersonRegisterSubmissionRepository extends MasterRepository<String> {

  /**
   * Instantiates a new self pay submission repository.
   */
  public PersonRegisterSubmissionRepository() {
    super("person_register_submission_batch", "personregister_batch_id", null,
        new String[] { "personregister_batch_id" });
  }

  /** The get selfpay bills. */
  private static final String GET_SELFPAY_PERSON_REGISTER = "SELECT DISTINCT(pr.mr_no) "
      // + "b.account_group, pd.government_identifier "
      + "FROM patient_registration pr " + "JOIN bill b ON (b.visit_id = pr.patient_id) "
      // + "JOIN patient_details pd ON (pr.mr_no = pd.mr_no) "
      + "LEFT JOIN tpa_master tm ON (tm.tpa_id = pr.primary_sponsor_id) "
      + "LEFT JOIN sponsor_type st ON (tm.sponsor_type_id = st.sponsor_type_id) "
      + "WHERE (b.is_tpa = FALSE OR st.is_selfpay_sponsor = TRUE) ";
  // + "AND b.selfpay_batch_id = 0 ";

  /**
   * Search Person Register for submission.
   *
   * @param filter the filter
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> searchMrNosForSubmission(Map filter) throws SQLException {
    StringBuilder query = new StringBuilder();
    query.append(GET_SELFPAY_PERSON_REGISTER);
    
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

    return DatabaseHelper.queryToDynaList(query.toString(), params);
  }

  /** The get Mr no. */
  private static final String GET_MRNO_PERSON_REGISTER_DETAILS = "SELECT prsbd.mr_no "
      + "FROM person_register_submission_batch_details prsbd "
      + "JOIN person_register_submission_batch prsb ON "
      + "(prsb.personregister_batch_id = prsbd.personregister_batch_id) "
      + "WHERE prsbd.mr_no = :mrNo AND prsb.center_id= :centerId  "
      + "AND prsb.account_group_id=:accountGroup";

  /**
   * Check whether the patient is already part of batch ever before.
   * 
   * @param mrNo         the patient id
   * @param centerId     the center id
   * @param accountGroup the account group
   * @return the list
   * @throws SQLException the SQL Exception
   */
  public List<BasicDynaBean> isPatientAlreadyRegistered(Object mrNo, Integer centerId,
      Integer accountGroup) throws SQLException {
    StringBuilder query = new StringBuilder();
    query.append(GET_MRNO_PERSON_REGISTER_DETAILS);
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("mrNo", mrNo);
    params.addValue("centerId", centerId);
    params.addValue("accountGroup", accountGroup);

    return DatabaseHelper.queryToDynaList(query.toString(), params);
  }

  private static final String GET_PERSON_REGISTER_DETAILS_NEW = "Select "
      + "         pd.patient_name, pd.mr_no AS patient_id, " + "         pd.middle_name, "
      + "         pd.last_name, pd.patient_phone," + "         pd.patient_gender, "
      + "         coalesce(cs.label,cmn.country_name) AS nationality, "
      + "         pd.other_identification_doc_value as unified_number,"
      + "         cs.short_code AS nationality_code, "
      + "         css.short_code AS emirate_of_residence_code, "
      + "         csc.short_code AS city_code, "
      + "         c.city_name AS patient_city, " + "         cm.country_name AS country, "
      + "         to_char(coalesce(pd.expected_dob, pd.dateofbirth), 'dd/MM/yyyy') "
      + "             AS date_of_birth,pd.passport_no," + "         CASE "
      + "             WHEN pd.government_identifier IS NULL "
      + "                  OR pd.government_identifier = '' THEN COALESCE(gim.identifier_type, '') "
      + "             ELSE pd.government_identifier " + "         END AS emirates_id_number"
      + "         from patient_details pd  join "
      + "         person_register_submission_batch_details prsbd on pd.mr_no = prsbd.mr_no "
      + "         LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id) "
      + "         LEFT JOIN country_master cmn ON cmn.country_id = pd.nationality_id "
      + "         LEFT JOIN city c ON c.city_id = pd.patient_city "
      + "         LEFT JOIN country_master cm ON cm.country_id = pd.country "
      + "         LEFT JOIN state_master sm ON sm.state_id = pd.patient_state "
      + "         LEFT JOIN code_sets cs ON cs.entity_id = cmn.id AND "
      + "         cs.code_system_category_id = 6 AND cs.code_system_id IN "
      + "         (SELECT id from code_systems where code_systems.label = :health_authority)"
      + "         LEFT JOIN code_sets css ON css.entity_id = sm.id AND "
      + "         css.code_system_category_id = 7 AND css.code_system_id IN "
      + "         (SELECT id from code_systems where code_systems.label = :health_authority)"
      + "         LEFT JOIN code_sets csc ON csc.entity_id = c.id AND "
      + "         csc.code_system_category_id = 12 AND csc.code_system_id IN "
      + "         (SELECT id from code_systems where code_systems.label = :health_authority)"
      + "         where prsbd.personregister_batch_id = :batch_id ";

  /**
   * Gets the bill details.
   *
   * @param personRegisterBatchId the person register batch id
   * @return the bill details
   */
  public List<BasicDynaBean> getPatientDetails(String personRegisterBatchId,
      String healthAuthority) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("batch_id", personRegisterBatchId);
    params.addValue("health_authority", healthAuthority);
    return DatabaseHelper.queryToDynaList(GET_PERSON_REGISTER_DETAILS_NEW, params);
  }

  /** The get haad account xml header. */
  private static String GET_HAAD_ACCOUNT_XML_HEADER = "SELECT account_group_id, "
      + "account_group_name, "
      + " account_group_service_reg_no AS provider_id, '' AS eclaim_xml_schema, "
      + " 'HAAD' as receiver_id, "
      + " to_char(current_timestamp::timestamp, 'dd/MM/yyyy hh24:mi') AS todays_date, "
      + " 0 as claims_count, 'Y' AS testing, '' AS from_date, '' AS to_date, '' AS operation "
      + " FROM account_group_master WHERE account_group_id = "
      + " (SELECT account_group_id FROM person_register_submission_batch "
      + " WHERE personregister_batch_id = ?) ";

  /** The get haad hospital center xml header. */
  private static String GET_HAAD_HOSPITAL_CENTER_XML_HEADER = "SELECT center_id, center_name, "
      + " hospital_center_service_reg_no AS provider_id, '' AS eclaim_xml_schema, "
      + " 'HAAD' as receiver_id, "
      + " to_char(current_timestamp::timestamp, 'dd/MM/yyyy hh24:mi') AS todays_date, "
      + " 0 as claims_count, 'Y' AS testing, '' AS from_date, '' AS to_date, '' AS operation "
      + " FROM hospital_center_master WHERE center_id = "
      + " (SELECT center_id FROM person_register_submission_batch "
      + "WHERE personregister_batch_id= ?) ";

  /**
   * Gets the haad xml header fields.
   *
   * @param submissionBean the submission bean
   * @return the haad xml header fields
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getHaadXmlHeaderFields(BasicDynaBean submissionBean) {
    if (submissionBean != null) {
      String submissionBatchId = (String) submissionBean.get("personregister_batch_id");
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
      + "prsb.personregister_batch_id as batch_id , prsb.status,prsb.visit_type, "
      + " prsb.created_at, prsb.submission_date, agm.account_group_service_reg_no, "
      + "prsb.processing_status, "
      + " hc.hospital_center_service_reg_no, prsb.account_group_id, agm.account_group_name, "
      + "prsb.center_id, hc.center_name " + " FROM person_register_submission_batch prsb "
      + " LEFT JOIN account_group_master agm ON (agm.account_group_id = prsb.account_group_id)"
      + " LEFT JOIN hospital_center_master hc ON(hc.center_id = prsb.center_id) ) AS foo ";

  /**
   * List submission details.
   *
   * @param parameters        the parameters
   * @param listingParameters the listing parameters
   * @param accGrpFilter      the acc grp filter
   * @param centerFilter      the center filter
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
