package com.insta.hms.mdm.opvisittyperuleapplicability;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.centers.CenterRepository;
import com.insta.hms.mdm.departments.DepartmentRepository;
import com.insta.hms.mdm.doctors.DoctorCenterMasterRepository;
import com.insta.hms.mdm.doctors.DoctorRepository;
import com.insta.hms.mdm.opvisittyperules.OpVisitTypeRulesRepository;
import com.insta.hms.mdm.tpas.TpaRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class OpVisitTypeRuleApplicabilityService extends MasterService {

  /** The Constant SPONSOR_CASH. */
  public static final String SPONSOR_CASH = "$";

  /** The Constant DOCTOR_NOT_APPLICABLE. */
  public static final String DOCTOR_NOT_APPLICABLE = "#";
  
  /** The Constant DOCTOR_ALL. */
  public static final String DOCTOR_ALL = "*";

  /** The op visit type rule applicability repository. */
  @LazyAutowired
  OpVisitTypeRuleApplicabilityRepository opVisitTypeRuleApplicabilityRepository;

  /** The op visit type applicability comparator. */
  @LazyAutowired
  OpVisitTypeApplicabilityComparator opVisitTypeApplicabilityComparator;

  /* The op visit type rule details repository */
  @LazyAutowired
  private CenterRepository centerRepository;

  /* The tpa master repository */
  @LazyAutowired
  private TpaRepository tpaRepository;

  /* The department repository */
  @LazyAutowired
  private DepartmentRepository departmentRepository;

  /* The doctor repository */
  @LazyAutowired
  private DoctorRepository doctorRepository;
  
  /* The doctor center master repository */
  @LazyAutowired
  private DoctorCenterMasterRepository doctorCenterMasterRepository;

  /* The Visit Type Rules repository */
  @LazyAutowired
  private OpVisitTypeRulesRepository opVisitTypeRulesRepo;

  /**
   * Instantiates a new op visit type rule applicability service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public OpVisitTypeRuleApplicabilityService(OpVisitTypeRuleApplicabilityRepository repository,
      OpVisitTypeRuleApplicabilityValidator validator) {
    super(repository, validator);
  }

  /**
   * 
   * @param centerId
   *          the center id
   * @param tpaId
   *          the tpa id
   * @param deptId
   *          the dept id
   * @param doctorId
   *          the doctor id
   * @return the applicable rule, with decreasing priority from center, sponsor, department, doctor
   *         in that order.
   */
  public BasicDynaBean getApplicableRule(Integer centerId, String tpaId, String deptId,
      String doctorId) {
    
    if (StringUtils.isEmpty(tpaId)) {
      tpaId = SPONSOR_CASH;
    }
    
    List<BasicDynaBean> rules = opVisitTypeRuleApplicabilityRepository.getApplicableRules(centerId,
        tpaId, deptId, doctorId);
    try {
      return Collections.max(rules, opVisitTypeApplicabilityComparator);
    } catch (NoSuchElementException ex) {
      return null;
    }

  }

  /**
   * Gets the
   *
   * @return the adds the edit page data.
   */
  public Map<String, List<BasicDynaBean>> getAllComponents() {
    Map<String, List<BasicDynaBean>> map = new HashMap<>();
    Map<String, Object> criteriaMap = new HashMap<>();
    criteriaMap.put("status", "A");
    List<BasicDynaBean> beans = centerRepository.findByCriteria(criteriaMap);
    map.put("centers", beans);
    List<BasicDynaBean> tpaBeans = tpaRepository.findByCriteria(criteriaMap);
    map.put("tpa", tpaBeans);
    List<BasicDynaBean> deptBeans = departmentRepository.findByCriteria(criteriaMap);
    map.put("departments", deptBeans);
    List<BasicDynaBean> doctorBeans = doctorRepository.findByCriteria(criteriaMap);
    map.put("doctors", doctorBeans);
    List<BasicDynaBean> centerDoctors = doctorCenterMasterRepository.findByCriteria(criteriaMap);
    map.put("centerDoctors", centerDoctors);    
    List<BasicDynaBean> rulesBean = opVisitTypeRulesRepo.findByCriteria(criteriaMap);
    map.put("rules", rulesBean);
    List<BasicDynaBean> applicabilities = opVisitTypeRuleApplicabilityRepository
        .getApplicableRulesList();
    map.put("applicabilities", applicabilities);
    return map;
  }

  /**
   * Save applicability.
   *
   * @param paramMap the param map
   * @return the map
   */
  public Map<String, String> saveApplicability(Map<String, Object> paramMap) {
    Map<String, String> returnMap = new HashMap<>();
    BasicDynaBean applicabiltyBean = opVisitTypeRuleApplicabilityRepository.getBean();
    ConversionUtils.copyToDynaBean(paramMap, applicabiltyBean);
    Integer rowsInserted = opVisitTypeRuleApplicabilityRepository.insert(applicabiltyBean);
    if (rowsInserted > 0) {
      returnMap.put("status", "success");
    } else {
      returnMap.put("status", "fail");
    }
    return returnMap;
  }

  /**
   * Delete applicability.
   *
   * @param params the params
   * @return the map
   */
  public Map<String, String> deleteApplicability(Map<String, Object> params) {
    Map<String, String> returnMap = new HashMap<>();
    Map<String, Object> paramMap = new HashMap<>();
    if (!params.containsKey("rule_applicability_id")
        || !StringUtils.isNoneEmpty(params.get("rule_applicability_id").toString())) {
      returnMap.put("status", "fail");
      return returnMap;
    }
    paramMap.put("rule_applicability_id",
        Integer.parseInt(params.get("rule_applicability_id").toString()));
    Integer rowsDeleted = opVisitTypeRuleApplicabilityRepository.delete(paramMap);
    if (rowsDeleted > 0) {
      returnMap.put("status", "success");
    } else {
      returnMap.put("status", "fail");
    }
    return returnMap;
  }

}
