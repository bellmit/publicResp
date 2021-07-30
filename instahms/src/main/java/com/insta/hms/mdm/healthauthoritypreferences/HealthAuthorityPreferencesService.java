package com.insta.hms.mdm.healthauthoritypreferences;

import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.mrdcodesupport.MrdCodeSupportService;
import com.insta.hms.mdm.organization.OrganizationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The Class HealthAuthorityPreferencesService. */
@Service
public class HealthAuthorityPreferencesService extends MasterService {

  /** The organization service. */
  @LazyAutowired private OrganizationService organizationService;

  /** The mrd code support service. */
  @LazyAutowired private MrdCodeSupportService mrdCodeSupportService;

  /** The consultation types service. */
  @LazyAutowired private ConsultationTypesService consultationTypesService;
  
  /** The health auth pref repo. */
  @LazyAutowired private HealthAuthorityPreferencesRepository healthAuthPrefRepo;

  /**
   * Instantiates a new health authority preferences service.
   *
   * @param repo the r
   * @param validator the v
   */
  public HealthAuthorityPreferencesService(
      HealthAuthorityPreferencesRepository repo, HealthAuthorityPreferencesValidator validator) {
    super(repo, validator);
  }

  /**
   * Gets the list page data.
   *
   * @param requestParams the request params
   * @return the list page data
   */
  public Map<String, List<BasicDynaBean>> getListPageData(Map<String, String[]> requestParams) {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();

    List<BasicDynaBean> baseRatePlan = organizationService.getAllBaseRatePlanList();
    map.put("baseRatePlanList", baseRatePlan);

    List<BasicDynaBean> defaultDiagnosisCodeType =
        mrdCodeSupportService.getDefaultDiagnosisCodeType();
    map.put("defaultDiagnosisCodeType", defaultDiagnosisCodeType);

    List<BasicDynaBean> drugCodeType = mrdCodeSupportService.getDrugCodeType();
    map.put("drugCodeType", drugCodeType);

    List<BasicDynaBean> consultationCodeType = mrdCodeSupportService.getConsultationsCodeType();
    map.put("consultationCodeType", consultationCodeType);

    List<BasicDynaBean> consultationTypes = consultationTypesService.getConsultationTypes();
    map.put("consultationTypes", consultationTypes);

    return map;
  }

  /**
   * This method is used to convert request map to bean.
   * 
   * @param requestParams Map
   * @param fileMap Map
   * @return BasicDynaBean
   */
  @Override
  public BasicDynaBean toBean(
      Map<String, String[]> requestParams, Map<String, MultipartFile> fileMap) {
    BasicDynaBean bean = super.toBean(requestParams, fileMap);
    String[] codeTypes = requestParams.get("drug_code_type");
    String[] consultCodeTypes = requestParams.get("consultation_code_types");
    String codeTypeString = StringUtil.join(codeTypes, ",");
    String consultCodeTypeString = StringUtil.join(consultCodeTypes, ",");
    bean.set("drug_code_type", codeTypeString);
    bean.set("consultation_code_types", consultCodeTypeString);
    return bean;
  }

  /**
   * List bycenter id.
   *
   * @param centerId the center id
   * @return the basic dyna bean
   */
  public BasicDynaBean listBycenterId(Integer centerId) {
    centerId = centerId == null ? -1 : centerId;
    return ((HealthAuthorityPreferencesRepository) this.getRepository()).listBycenterId(centerId);
  }
  
  /**
   * Gets the center health auth base rate plan.
   *
   * @return the center health auth base rate plan
   */
  public String getCenterHealthAuthBaseRatePlan() {
    return healthAuthPrefRepo.getCenterHealthAuthBaseRatePlan();
  }
  
  public BasicDynaBean findByKey(String healthAuthority) {
    return healthAuthPrefRepo.findByKey("health_authority", healthAuthority);
  }
  
  /**
   * Is Prescribe By Generics.
   * 
   * @param centerId the center id
   * @return boolean
   */
  public boolean isPrescribeByGenerics(Integer centerId) {
    String prescByGenericPreference =
        healthAuthPrefRepo.getPrescribeByGenericsPrefernceByCenterId(centerId);
    return "Y".equals(prescByGenericPreference) ? true : false;
  }
}
