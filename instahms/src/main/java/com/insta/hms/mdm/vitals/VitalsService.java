package com.insta.hms.mdm.vitals;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.mdm.MasterDetailsService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.departments.DepartmentService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The Class VitalsService. */
@Service("vitalsService")
public class VitalsService extends MasterDetailsService {

  /** The Constant CENTER_ID. */
  private static final String CENTER_ID = "center_id";

  /** The Constant CENTERS. */
  private static final String CENTERS = "centers";

  /** The Constant DEFAULT. */
  private static final String DEFAULT = "default";

  /** The Constant DEPARTMENT_ID. */
  private static final String DEPARTMENT_ID = "dept_id";

  /** The Constant DEPARTMENTS. */
  private static final String DEPARTMENTS = "departments";

  /** The Constant MANDATORY. */
  private static final String MANDATORY = "mandatory";

  /** The Constant PARAMETER_APPLICABILITIES. */
  private static final String PARAMETER_APPLICABILITIES = "parameter_applicabilities";

  /** The Constant VITAL_PARAMETER_ID. */
  private static final String VITAL_PARAMETER_ID = "param_id";

  /** The Constant VITAL_PARAMETER_ORDER. */
  private static final String VITAL_PARAMETER_ORDER = "param_order";

  /** The Constant VITAL_DEFAULT_ID. */
  private static final String VITAL_DEFAULT_ID = "vital_default_id";

  /** The Constant VITAL_TYPE. */
  private static final String VITAL_TYPE = "visit_type";
  

  /** The department service. */
  @LazyAutowired
  DepartmentService departmentService;

  /** The vital applicability repository. */
  @LazyAutowired
  VitalApplicabilityRepository vitalApplicabilityRepository;

  /** The vital validator. */
  @LazyAutowired
  VitalValidator vitalValidator;
  
  @LazyAutowired
  CenterService centerService;
  
  @LazyAutowired
  SessionService sessionService;
  
  @LazyAutowired
  GenericPreferencesService genPrefService;

  /**
   * Instantiates a new vitals service.
   *
   * @param parameterRepository
   *          the parameter repository
   * @param vitalValidator
   *          the vital validator
   * @param d1
   *          the d 1
   */
  public VitalsService(VitalParameterRepository parameterRepository, VitalValidator vitalValidator,
      VitalReferenceRangesRepository d1) {
    super(parameterRepository, vitalValidator, d1);
  }

  /**
   * Gets the departmentwise applicability.
   *
   * @param departmentId the department id
   * @param visitType the visit type
   * @return the departmentwise applicability
   */
  public Map<String, Object> getDepartmentwiseApplicability(String departmentId, String visitType) {
    Integer userCenterId = (Integer) sessionService.getSessionAttributes().get("centerId");
    int maxCenterIncDefault = (Integer) genPrefService.getAllPreferences()
        .get("max_centers_inc_default");
    List<BasicDynaBean> departmentVitalApplicabilities = vitalApplicabilityRepository
        .getVitalApplicabilityForDepartment(departmentId,visitType,
            userCenterId,maxCenterIncDefault);

    Integer currentCenterId = null;
    Map<String, Object> centerListItem = null;
    List<Map<String, Object>> centerVitalParameterApplicabilities = null;
    List<Map<String, Object>> centers = new ArrayList<>();

    for (BasicDynaBean vitalApplicability : departmentVitalApplicabilities) {

      if (!vitalApplicability.get(CENTER_ID).equals(currentCenterId)) {
        if (currentCenterId != null) {
          centerListItem.put(CENTER_ID, currentCenterId);
          centerListItem.put(PARAMETER_APPLICABILITIES, centerVitalParameterApplicabilities);
          centers.add(centerListItem);
        }
        currentCenterId = (Integer) vitalApplicability.get(CENTER_ID);
        centerListItem = new HashMap<>();
        centerVitalParameterApplicabilities = new ArrayList<>();
      }

      Map<String, Object> centerVitalParameterApplicability = new HashMap<>();
      centerVitalParameterApplicability.put(DEFAULT, "Y");
      centerVitalParameterApplicability.put(MANDATORY, vitalApplicability.get(MANDATORY));
      centerVitalParameterApplicability.put(VITAL_PARAMETER_ID,
          vitalApplicability.get(VITAL_PARAMETER_ID));
      centerVitalParameterApplicability.put(VITAL_DEFAULT_ID,
          vitalApplicability.get(VITAL_DEFAULT_ID));
      centerVitalParameterApplicabilities.add(centerVitalParameterApplicability);
    }
    if (currentCenterId != null) {
      centerListItem.put(CENTER_ID, currentCenterId);
      centerListItem.put(PARAMETER_APPLICABILITIES, centerVitalParameterApplicabilities);
      centers.add(centerListItem);
    }
    Map<String, Object> response = new HashMap<>();
    response.put(DEPARTMENT_ID, departmentId);
    response.put(CENTERS, centers);
    return response;
  }


  /**
   * Gets the centerwise applicability.
   *
   * @param centerId the center id
   * @param visitType the visit type
   * @return the centerwise applicability
   */
  public Map<String, Object> getCenterwiseApplicability(int centerId, String visitType) {
    List<BasicDynaBean> centerVitalApplicabilities = vitalApplicabilityRepository
        .getVitalApplicabilityForCenter(centerId,visitType);

    String currentDepartmentId = null;
    Map<String, Object> departmentListItem = null;
    List<Map<String, Object>> departmentVitalParameterApplicabilities = null;
    List<Map<String, Object>> departments = new ArrayList<>();

    for (BasicDynaBean vitalApplicability : centerVitalApplicabilities) {

      if (!vitalApplicability.get(DEPARTMENT_ID).equals(currentDepartmentId)) {
        if (currentDepartmentId != null) {
          departmentListItem.put(DEPARTMENT_ID, currentDepartmentId);
          departmentListItem.put(PARAMETER_APPLICABILITIES,
              departmentVitalParameterApplicabilities);
          departments.add(departmentListItem);
        }
        currentDepartmentId = (String) vitalApplicability.get(DEPARTMENT_ID);
        departmentListItem = new HashMap<>();
        departmentVitalParameterApplicabilities = new ArrayList<>();
      }

      Map<String, Object> centerVitalParameterApplicability = new HashMap<>();
      centerVitalParameterApplicability.put(DEFAULT, "Y");
      centerVitalParameterApplicability.put(MANDATORY, vitalApplicability.get(MANDATORY));
      centerVitalParameterApplicability.put(VITAL_PARAMETER_ID,
          vitalApplicability.get(VITAL_PARAMETER_ID));
      centerVitalParameterApplicability.put(VITAL_DEFAULT_ID,
          vitalApplicability.get(VITAL_DEFAULT_ID));
      departmentVitalParameterApplicabilities.add(centerVitalParameterApplicability);
    }
    if (currentDepartmentId != null) {
      departmentListItem.put(DEPARTMENT_ID, currentDepartmentId);
      departmentListItem.put(PARAMETER_APPLICABILITIES, departmentVitalParameterApplicabilities);
      departments.add(departmentListItem);
    }
    Map<String, Object> response = new HashMap<>();
    response.put(CENTER_ID, centerId);
    response.put(DEPARTMENTS, departments);
    return response;
  }

  /**
   * List all.
   *
   * @return the list
   */
  public List<BasicDynaBean> listAll() {
    return getRepository().listAll(null, "param_container", "V", VITAL_PARAMETER_ORDER);
  }

  /**
   * For checking duplicate order and vital.
   *
   * @param filterValues
   *          the filter values
   * @return the list
   */
  public List<BasicDynaBean> filterVitalParamOrOrder(Object[] filterValues) {
    return ((VitalParameterRepository) getRepository()).filterVitalParamOrOrder(filterValues);
  }

  /**
   * Save.
   *
   * @param saveRequestBody          the save request body
   * @return the map
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> save(Map<String, Object> saveRequestBody) {
    vitalValidator.validateVitalApplicabilitySave(saveRequestBody);
    List<BasicDynaBean> inserts = new ArrayList<>();
    List<BasicDynaBean> updates = new ArrayList<>();
    List<Integer> updateKeys = new ArrayList<>();
    List<Map<String, Object>> deletes = new ArrayList<>();
    List<Object> deleteKeys = new ArrayList<>();

    List<Map<String, Object>> departments = (List<Map<String, Object>>) saveRequestBody
        .get(DEPARTMENTS);
    String currentDepartmentId = null;
    Integer currentCenterId = null;
    List<Map<String, Object>> centers = null;
    List<Map<String, Object>> parameterApplicabilities = null;
    for (Map<String, Object> department : departments) {
      currentDepartmentId = (String) department.get(DEPARTMENT_ID);
      centers = (List<Map<String, Object>>) department.get(CENTERS);

      for (Map<String, Object> center : centers) {
        currentCenterId = (Integer) center.get(CENTER_ID);
        parameterApplicabilities = (List<Map<String, Object>>) center
            .get(PARAMETER_APPLICABILITIES);

        for (Map<String, Object> parameterApplicability : parameterApplicabilities) {
          if (parameterApplicability.containsKey(VITAL_DEFAULT_ID)) {
            if ("Y".equals(parameterApplicability.get(DEFAULT))) {
              BasicDynaBean vitalApplicabilityBean = vitalApplicabilityRepository.getBean();
              vitalApplicabilityBean.set(CENTER_ID, currentCenterId);
              vitalApplicabilityBean.set(DEPARTMENT_ID, currentDepartmentId);
              vitalApplicabilityBean.set(MANDATORY, parameterApplicability.get(MANDATORY));
              vitalApplicabilityBean.set(VITAL_PARAMETER_ID,
                  parameterApplicability.get(VITAL_PARAMETER_ID));
              vitalApplicabilityBean.set(VITAL_DEFAULT_ID,
                  parameterApplicability.get(VITAL_DEFAULT_ID));
              updates.add(vitalApplicabilityBean);
              updateKeys.add((Integer) parameterApplicability.get(VITAL_DEFAULT_ID));
            } else {
              Map<String, Object> vitalApplicabilityMap = new HashMap<>();
              vitalApplicabilityMap.put(CENTER_ID, currentCenterId);
              vitalApplicabilityMap.put(DEPARTMENT_ID, currentDepartmentId);
              vitalApplicabilityMap.put(DEFAULT, parameterApplicability.get(DEFAULT));
              vitalApplicabilityMap.put(MANDATORY, parameterApplicability.get(MANDATORY));
              vitalApplicabilityMap.put(VITAL_DEFAULT_ID, null);
              vitalApplicabilityMap.put(VITAL_PARAMETER_ID,
                  parameterApplicability.get(VITAL_PARAMETER_ID));
              deletes.add(vitalApplicabilityMap);
              deleteKeys.add((Integer) parameterApplicability.get(VITAL_DEFAULT_ID));
            }
          } else {
            BasicDynaBean vitalApplicabilityBean = vitalApplicabilityRepository.getBean();
            vitalApplicabilityBean.set(VITAL_DEFAULT_ID,
                vitalApplicabilityRepository.getNextSequence());
            vitalApplicabilityBean.set(CENTER_ID, currentCenterId);
            vitalApplicabilityBean.set(DEPARTMENT_ID, currentDepartmentId);
            vitalApplicabilityBean.set(MANDATORY, parameterApplicability.get(MANDATORY));
            vitalApplicabilityBean.set(VITAL_PARAMETER_ID,
                parameterApplicability.get(VITAL_PARAMETER_ID));
            vitalApplicabilityBean.set(VITAL_TYPE, parameterApplicability.get(VITAL_TYPE));
            inserts.add(vitalApplicabilityBean);
          }
        }
      }
    }

    Map<String, Object> updateKeyMap = new HashMap<>();
    updateKeyMap.put(VITAL_DEFAULT_ID, updateKeys);
    int[] batchUpdateResults = vitalApplicabilityRepository.batchUpdate(updates, updateKeyMap);
    int[] batchInsertResults = vitalApplicabilityRepository.batchInsert(inserts);
    int[] batchDeleteResults = vitalApplicabilityRepository.batchDelete(VITAL_DEFAULT_ID,
        deleteKeys);
    if ((batchInsertResults != null
        && !vitalApplicabilityRepository.isBatchSuccess(batchInsertResults))
        || (batchUpdateResults != null
            && !vitalApplicabilityRepository.isBatchSuccess(batchUpdateResults))
        || (batchDeleteResults != null
            && !vitalApplicabilityRepository.isBatchSuccess(batchDeleteResults))) {
      throw new HMSException("exception.error.an.occured.while.saving");
    }
    List<Map<String, Object>> responseParameterApplicabilities = new ArrayList<>();
    for (BasicDynaBean insert : inserts) {
      Map<String, Object> insertVitalApplicabilityMap = insert.getMap();
      insertVitalApplicabilityMap.put(DEFAULT, "Y");
      responseParameterApplicabilities.add(insertVitalApplicabilityMap);
    }
    for (BasicDynaBean update : updates) {
      Map<String, Object> updateVitalApplicabilityMap = update.getMap();
      updateVitalApplicabilityMap.put(DEFAULT, "Y");
      responseParameterApplicabilities.add(updateVitalApplicabilityMap);
    }
    for (Map<String, Object> delete : deletes) {
      responseParameterApplicabilities.add(delete);
    }
    Map<String, Object> response = new HashMap<>();
    response.put(PARAMETER_APPLICABILITIES, responseParameterApplicabilities);
    return response;
  }

  /**
   * Gets the departments.
   *
   * @return the departments
   */
  public Map<String, Object> getDepartments() {
    Map<String, Object> response = new HashMap<>();
    response.put("departments",
        ConversionUtils.listBeanToListMap(departmentService.getNonClinicalDepartments()));
    return response;
  }

  public Map<String, Object> getLoggedInCenters(Map<String, String[]> parameters) {
    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    return centerService.getUserCenters(parameters,centerId);
  }

}
