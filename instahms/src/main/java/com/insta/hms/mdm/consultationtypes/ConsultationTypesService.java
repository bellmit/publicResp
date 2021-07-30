package com.insta.hms.mdm.consultationtypes;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class ConsultationTypesService.
 */
@Service
public class ConsultationTypesService extends MasterService {

  // private ConsultationTypesRepository consultationTypesRepository;

  /** The health auth pref service. */
  @LazyAutowired private HealthAuthorityPreferencesService healthAuthPrefService;

  /**
   * Instantiates a new consultation types service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public ConsultationTypesService(
      ConsultationTypesRepository repo, ConsultationTypesValidator validator) {
    super(repo, validator);
    // consultationTypesRepository = r;
  }

  public List<BasicDynaBean> getAllConsultationTypes(List<String> columns) {
    return getRepository().listAll(columns, "status", "A");
  }

  public List<BasicDynaBean> getAllOpConsultationTypes(List<String> columns, Map filterMap) {
    return getRepository().listAll(columns, filterMap, null);
  }

  /**
   * Gets the consultation types.
   *
   * @return the consultation types
   */
  public List<BasicDynaBean> getConsultationTypes() {
    return getConsultationTypes("o");
  }
  
  public List<BasicDynaBean> getConsultationTypes(String visitType) {
    return ((ConsultationTypesRepository) getRepository()).getConsultationTypes(visitType);
  }

  /**
   * Gets the consultation types.
   *
   * @param patientType the patient type
   * @param orgId the org id
   * @param healthAuthority the health authority
   * @return the consultation types
   */
  public List<BasicDynaBean> getConsultationTypes(String patientType, String orgId,
      String healthAuthority) {
    List<String> orgIds = new ArrayList<>();
    orgIds.add(orgId);
    return getConsultationTypes(patientType, "", orgIds, healthAuthority);
  }

  /**
   * Gets the consultation types.
   *
   * @param patientType the patient type
   * @param orgId the org id
   * @param healthAuthority the health authority
   * @param practitionertypeId the practitioner type id
   * @return the consultation types
   */
  public List<BasicDynaBean> getConsultationTypes(String patientType, String orgId,
      String healthAuthority, Integer practitionertypeId) {
    List<String> orgIds = new ArrayList<>();
    orgIds.add(orgId);
    return getConsultationTypes(patientType, "", orgIds, healthAuthority, practitionertypeId);
  }

  /**
   * Gets the consultation types.
   *
   * @param patientType the patient type
   * @param orgIds the org ids
   * @param healthAuthority the health authority
   * @return the consultation types
   */
  public List<BasicDynaBean> getConsultationTypes(String patientType, List<String> orgIds,
      String healthAuthority) {
    return getConsultationTypes(patientType, "", orgIds, healthAuthority);
  }

  /**
   * Gets the consultation types.
   *
   * @param patientType the patient type
   * @param orgIds the org ids
   * @param healthAuthority the health authority
   * @return the consultation types
   */
  public List<BasicDynaBean> getConsultationTypes(String patientType, List<String> orgIds,
      String healthAuthority, Integer practitionertypeId) {
    return getConsultationTypes(patientType, "", orgIds, healthAuthority, practitionertypeId);
  }

  /**
   * Gets the consultation types.
   *
   * @param patientType1 the patient type 1
   * @param patientType2 the patient type 2
   * @param orgId the org id
   * @param healthAuthority the health authority
   * @return the consultation types
   */
  public List<BasicDynaBean> getConsultationTypes(String patientType1, String patientType2,
      String orgId, String healthAuthority) {
    List<String> orgIds = new ArrayList<>();
    orgIds.add(orgId);
    return getConsultationTypes(patientType1, patientType2, orgIds, healthAuthority);
  }

  /**
   * Gets the consultation types.
   *
   * @param patientType1
   *          the patient type 1
   * @param patientType2
   *          the patient type 2
   * @param orgIds
   *          the org ids
   * @param healthAuthority
   *          the health authority
   * @return the consultation types
   */
  public List<BasicDynaBean> getConsultationTypes(String patientType1, String patientType2,
      List<String> orgIds, String healthAuthority) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("health_authority", healthAuthority);
    BasicDynaBean healthPref = healthAuthPrefService.findByPk(params);
    String consultationCodeTypes = healthPref == null ? null
        : (String) healthPref.get("consultation_code_types");
    return ((ConsultationTypesRepository) this.getRepository()).getConsultationTypes(patientType1,
        patientType2, orgIds, healthAuthority, consultationCodeTypes);
  }

  /**
   * Gets the consultation types.
   *
   * @param patientType1
   *          the patient type 1
   * @param patientType2
   *          the patient type 2
   * @param orgIds
   *          the org ids
   * @param healthAuthority
   *          the health authority
   * @param practitionertypeId 
   *          the practitioner type id
   * @return the consultation types
   */
  public List<BasicDynaBean> getConsultationTypes(String patientType1, String patientType2,
      List<String> orgIds, String healthAuthority, Integer practitionertypeId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("health_authority", healthAuthority);
    BasicDynaBean healthPref = healthAuthPrefService.findByPk(params);
    String consultationCodeTypes = healthPref == null ? null
        : (String) healthPref.get("consultation_code_types");
    return ((ConsultationTypesRepository) this.getRepository()).getConsultationTypes(patientType1,
        patientType2, orgIds, healthAuthority, consultationCodeTypes, practitionertypeId);
  }

  /**
   * Gets the consultation type item sub group tax details.
   *
   * @param consultationId the consultation id
   * @return the consultation type item sub group tax details
   */
  public List<BasicDynaBean> getConsultationTypeItemSubGroupTaxDetails(int consultationId) {
    return ((ConsultationTypesRepository) getRepository())
        .getConsultationTypeItemSubGroupTaxDetails(consultationId);
  }

  /**
   * Find by key.
   *
   * @param consultationTypeId the consultation type id
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(int consultationTypeId) {
    // TODO Auto-generated method stub
    return ((ConsultationTypesRepository) getRepository())
        .findByKey("consultation_type_id", consultationTypeId);
  }

  public BasicDynaBean getOrgDetails(String consultationTypeId, String orgId) {
    return ((ConsultationTypesRepository) getRepository())
        .getConsultationTypeOrgDetails(Integer.parseInt(consultationTypeId), orgId);
  }
  //  public List<BasicDynaBean> getConsultationTypes(Map<String, String[]> paramsMap) {
  //    List<BasicDynaBean> consulationTypes =
  // consultationTypesRepository.getConsulationTypes(healthPref,
  //        healthAuthority, patientType1, patientType2, orgId);
  //    return null;
  //
  //  }
}
