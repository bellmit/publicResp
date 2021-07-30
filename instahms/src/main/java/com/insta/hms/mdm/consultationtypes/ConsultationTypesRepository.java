package com.insta.hms.mdm.consultationtypes;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The Class ConsultationTypesRepository.
 *
 * @author Anand Patel
 */
@Repository
public class ConsultationTypesRepository extends MasterRepository<Integer> {

  private StringBuilder consultationTypes =
      new StringBuilder(
          " SELECT ct.consultation_type_id, "
              + " consultation_type, status, consultation_code, patient_type, "
              + " doctor_charge_type, service_sub_group_id, ct.username, ct.mod_time,"
              + " charge_head,insurance_category_id,visit_consultation_type "
              + " FROM consultation_types ct "
              + " JOIN consultation_org_details co"
              + " on(co.consultation_type_id = ct.consultation_type_id and co.applicable) ");

  public ConsultationTypesRepository() {
    super("consultation_types", "consultation_type_id");
  }

  /**
   * Gets the consultation types.
   *
   * @return the consultation types
   */
  public List<BasicDynaBean> getConsultationTypes(String visitType) {
    return DatabaseHelper.queryToDynaList(
        "SELECT consultation_type,consultation_type_id"
            + " FROM consultation_types"
            + " WHERE patient_type= ? AND status='A' ", visitType);
  }

  private static final String GET_CONSULTATION_TYPES_SELECT =
      " SELECT ct.consultation_type_id, "
          + " consultation_type, status, consultation_code, patient_type, "
          + " doctor_charge_type, service_sub_group_id, ct.username, ct.mod_time,"
          + " charge_head,insurance_category_id,visit_consultation_type,duration, "
          + " textcat_commacat(co.org_id) as org_ids "
          + " FROM consultation_types ct "
          + " JOIN consultation_org_details co"
          + " on(co.consultation_type_id = ct.consultation_type_id and co.applicable) ";

  private static final String CONSULTATION_CODE_TYPE_JOIN =
      " JOIN (SELECT trim(regexp_split_to_table(consultation_code_types, ',')) as code_type"
          + " FROM  health_authority_preferences "
          + " WHERE health_authority=?) as foo ON (foo.code_type = co.code_type) ";

  private static final String PRACTITIONAR_TYPE_JOIN =
      " JOIN practitioner_type_consultation_mapping ptcm "
          + " ON (ct.consultation_type_id = ptcm.consultation_type_id "
          + " AND ptcm.practitioner_type_id =";

  private static final String GET_CONSULTATION_TYPES_WHERE =
      " WHERE (patient_type=? OR patient_type=?) and status='A' and co.org_id IN ";

  private static final String GET_CONSULTATION_TYPES_ORDER_BY =
      " ORDER BY lower(consultation_type) ";

  private static final String GET_CONSULTATION_TYPES_GROUP_BY = " GROUP BY "
      + " ct.consultation_type_id, consultation_type, status, consultation_code, "
      + " patient_type, doctor_charge_type, service_sub_group_id, ct.username, ct.mod_time, "
      + " charge_head, insurance_category_id, visit_consultation_type";


  /**
   * Gets the consultation types.
   *
   * @param patientType1          the patient type 1
   * @param patientType2          the patient type 2
   * @param orgIds                the org id
   * @param healthAuthority       the health authority
   * @param consultationCodeTypes the consultation code types
   * @return the consultation types
   */
  public List<BasicDynaBean> getConsultationTypes(String patientType1, String patientType2,
      List<String> orgIds, String healthAuthority, String consultationCodeTypes) {

    if (orgIds == null || orgIds.isEmpty()) {
      return Collections.emptyList();
    }

    StringBuilder consultationTypesQuery = new StringBuilder(GET_CONSULTATION_TYPES_SELECT);

    if (consultationCodeTypes != null && !consultationCodeTypes.equals("")) {
      consultationTypesQuery.append(CONSULTATION_CODE_TYPE_JOIN);
    }

    consultationTypesQuery.append(GET_CONSULTATION_TYPES_WHERE);

    String[] placeholdersArr = new String[orgIds.size()];
    Arrays.fill(placeholdersArr, "?");
    consultationTypesQuery.append("( ")
        .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");

    consultationTypesQuery.append(GET_CONSULTATION_TYPES_GROUP_BY);
    consultationTypesQuery.append(GET_CONSULTATION_TYPES_ORDER_BY);

    List<Object> params;
    if (consultationCodeTypes != null && !consultationCodeTypes.equals("")) {
      params = new ArrayList<Object>(Arrays.asList(healthAuthority, patientType1, patientType2));
    } else {
      params = new ArrayList<Object>(Arrays.asList(patientType1, patientType2));
    }
    params.addAll(orgIds);
    return DatabaseHelper.queryToDynaList(consultationTypesQuery.toString(), params.toArray());
  }

  /**
   * Gets the consultation types.
   *
   * @param patientType1          the patient type 1
   * @param patientType2          the patient type 2
   * @param orgIds                the org id
   * @param healthAuthority       the health authority
   * @param consultationCodeTypes the consultation code types
   * @param practitionerTypeId    the practitioner type id
   * @return the consultation types
   */
  public List<BasicDynaBean> getConsultationTypes(String patientType1, String patientType2,
      List<String> orgIds, String healthAuthority, String consultationCodeTypes,
      Integer practitionerTypeId) {
    StringBuilder consultationTypesQuery = new StringBuilder(GET_CONSULTATION_TYPES_SELECT);

    if (consultationCodeTypes != null && !consultationCodeTypes.equals("")) {
      consultationTypesQuery.append(CONSULTATION_CODE_TYPE_JOIN);
    }

    if (null != practitionerTypeId) {
      consultationTypesQuery.append(PRACTITIONAR_TYPE_JOIN)
          .append(practitionerTypeId.toString()).append(")");
    }
    consultationTypesQuery.append(GET_CONSULTATION_TYPES_WHERE);
    List<Object> params;
    if (consultationCodeTypes != null && !consultationCodeTypes.equals("")) {
      params = new ArrayList<Object>(Arrays.asList(healthAuthority, patientType1, patientType2));
    } else {
      params = new ArrayList<Object>(Arrays.asList(patientType1, patientType2));
    }
    if (orgIds == null || orgIds.isEmpty()) {
      return Collections.emptyList();
    }
    String[] placeholdersArr = new String[orgIds.size()];
    Arrays.fill(placeholdersArr, "?");
    consultationTypesQuery.append("(")
        .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");

    consultationTypesQuery.append(GET_CONSULTATION_TYPES_GROUP_BY);
    consultationTypesQuery.append(GET_CONSULTATION_TYPES_ORDER_BY);

    params.addAll(orgIds);
    return DatabaseHelper.queryToDynaList(consultationTypesQuery.toString(), params.toArray());
  }

  /**
   * Gets the consulation types.
   *
   * @param healthPref      the health pref
   * @param healthAuthority the health authority
   * @param patientType1    the patient type 1
   * @param patientType2    the patient type 2
   * @param orgId           the org id
   * @return the consulation types
   */
  public List<BasicDynaBean> getConsulationTypes(
      BasicDynaBean healthPref,
      String healthAuthority,
      String patientType1,
      String patientType2,
      String orgId) {
    if (healthPref != null
        && healthPref.get("consultation_code_types") != null
        && !healthPref.get("consultation_code_types").equals("")) {
      consultationTypes.append(
          " JOIN (SELECT trim(regexp_split_to_table(consultation_code_types, ',')) as code_type"
              + " FROM  health_authority_preferences "
              + " WHERE health_authority=?) as foo ON (foo.code_type = co.code_type) ");
    }

    consultationTypes.append(
        " WHERE (patient_type=? OR patient_type=?) and status='A' and co.org_id = ? ");
    String conString = consultationTypes.toString();

    if (healthPref != null
        && healthPref.get("consultation_code_types") != null
        && !healthPref.get("consultation_code_types").equals("")) {
      return DatabaseHelper.queryToDynaList(
          conString, healthAuthority, patientType1, patientType2, orgId);
    } else {
      return DatabaseHelper.queryToDynaList(conString, patientType1, patientType2, orgId);
    }
  }

  private static final String GET_CONSULTATION_TYPE_ITEM_SUB_GROUP_TAX_DETAILS =
      "SELECT isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
          + " FROM consultation_item_sub_groups cisg "
          + " JOIN item_sub_groups isg ON(cisg.item_subgroup_id = isg.item_subgroup_id) "
          + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
          + " WHERE cisg.consultation_type_id = ? ";

  public List<BasicDynaBean> getConsultationTypeItemSubGroupTaxDetails(int consultationId) {
    return DatabaseHelper.queryToDynaList(
        GET_CONSULTATION_TYPE_ITEM_SUB_GROUP_TAX_DETAILS, new Object[] {consultationId});
  }

  private static final String DOCTOR_CHARGE_TYPE =
      "SELECT doctor_charge_type from consultation_types "
          + "WHERE consultation_type_id = ?";

  /**
   * Gets the consultation types.
   *
   * @param consultationTypeId the consultation Id
   * @return doctor_charge types
   */
  public static String getDoctorChargeType(Integer consultationTypeId) {
    if (consultationTypeId == null) {
      return null;
    }
    return DatabaseHelper.getString(DOCTOR_CHARGE_TYPE, new Object[] {consultationTypeId});

  }

  private static final String GET_ORG_DETAILS =
      "select * from consultation_org_details where consultation_type_id = ? and org_id = ?";

  public BasicDynaBean getConsultationTypeOrgDetails(Integer consultationTypeId, String orgId) {
    return DatabaseHelper
        .queryToDynaBean(GET_ORG_DETAILS, new Object[] {consultationTypeId, orgId});
  }

}