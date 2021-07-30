package com.insta.hms.mdm.vitalparameter.referenceranges;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.vitalparameters.VitalParameterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ReferenceRangesService.
 *
 * @author sonam
 */
@Service
public class ReferenceRangesService extends MasterService {
  
  /** The vital parameter service. */
  @LazyAutowired
  VitalParameterService vitalParameterService;
  
  /** The patient details service. */
  @LazyAutowired
  PatientDetailsService patientDetailsService;

  /** The reference ranges repository. */
  @LazyAutowired
  private ReferenceRangesRepository referenceRangesRepository;

  /**
   * Instantiates a new reference ranges service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public ReferenceRangesService(ReferenceRangesRepository repo,
      ReferenceRangesValidator validator) {
    super(repo, validator);
    this.referenceRangesRepository = repo;
  }

  /**
   * Gets the viatl bean.
   *
   * @param params the params
   * @return the viatl bean
   */
  public BasicDynaBean getViatlBean(Map params) {
    return vitalParameterService.findByPk(params);
  }

  /**
   * Gets the result ranges.
   *
   * @param params the params
   * @return the result ranges
   */
  public List<BasicDynaBean> getResultRanges(Map params) {
    Map<String, Object> filterMap = new HashMap<String, Object>();
    List<String> columns = new ArrayList<String>();
    String paramId = ((String[]) params.get("param_id"))[0];
    Integer paramIdInt = Integer.parseInt(paramId);
    filterMap.put("param_id", paramIdInt);
    return referenceRangesRepository.listAll(columns, filterMap, "priority");
  }

  /**
   * Update reference range.
   *
   * @param params the params
   * @return true, if successful
   */
  public boolean updateReferenceRange(Map params) {
    return referenceRangesRepository.updateReferenceRange(params);

  }

  /**
   * Gets the reference range list.
   *
   * @param vitals the vitals
   * @param mrNo the mr no
   * @return the reference range list
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getReferenceRangeList(List<BasicDynaBean> vitals, String mrNo) {
    List<Map<String, Object>> refRange = new ArrayList<Map<String, Object>>();
    Map<String, Object> applicableRefRangeMap = applicableReferenceRange(mrNo);
    Map allExactReferenceRangeParamIdMap = (Map) applicableRefRangeMap
        .get("allExactReferenceRangeParamIdMap");
    Map refRangeForAllParamIdMap = (Map) applicableRefRangeMap.get("refRangeForAllParamIdMap");

    for (int i = 0; i < vitals.size(); i++) {
      BasicDynaBean vitalBean = vitals.get(i);
      int paramId = (int) vitalBean.get("param_id");
      BasicDynaBean refRangeBean = null;
      Map<String, Object> map = new HashMap<>();
      List<BasicDynaBean> referenceRangeBeanList = 
          (List<BasicDynaBean>) allExactReferenceRangeParamIdMap
          .get(paramId);
      if (referenceRangeBeanList != null && !referenceRangeBeanList.isEmpty()) {
        refRangeBean = referenceRangeBeanList.get(0);
      } else {
        List<BasicDynaBean> refRangeForAllBeanList = (List<BasicDynaBean>) refRangeForAllParamIdMap
            .get(paramId);
        if (refRangeForAllBeanList != null && !refRangeForAllBeanList.isEmpty()) {
          refRangeBean = refRangeForAllBeanList.get(0);
        }
      }
      map.put("param_id", vitalBean.get("param_id"));
      map.put("param_order", vitalBean.get("param_order"));
      map.put("param_container", vitalBean.get("param_container"));
      map.put("param_label", vitalBean.get("param_label"));
      map.put("param_uom", vitalBean.get("param_uom"));
      map.put("mandatory_in_tx", vitalBean.get("mandatory_in_tx"));
      map.put("visit_type", vitalBean.get("visit_type"));
      map.put("param_status", vitalBean.get("param_status"));
      if (null != refRangeBean) {
        map.put("range_id",  refRangeBean.get("range_id"));
        map.put("patient_gender",  refRangeBean.get("patient_gender"));
        map.put("min_patient_age",  refRangeBean.get("min_patient_age"));
        map.put("max_patient_age",  refRangeBean.get("max_patient_age"));
        map.put("range_for_all",  refRangeBean.get("range_for_all"));
        map.put("age_unit",  refRangeBean.get("age_unit"));
        map.put("min_normal_value",  refRangeBean.get("min_normal_value"));
        map.put("max_normal_value", refRangeBean.get("max_normal_value"));
        map.put("max_improbable_value", refRangeBean.get("max_improbable_value"));
        map.put("min_improbable_value", refRangeBean.get("min_improbable_value"));
        map.put("max_critical_value", refRangeBean.get("max_critical_value"));
        map.put("min_critical_value", refRangeBean.get("min_critical_value"));
        map.put("reference_range_txt", refRangeBean.get("reference_range_txt")); 
      }
      refRange.add(map);
    }

    return refRange;
  }

  /**
   * Sets the applicable reference range.
   *
   * @param vitals the vitals
   * @param mrNo the mr no
   * @return the list
   */
  public List<BasicDynaBean> setApplicableReferenceRange(List<BasicDynaBean> vitals, String mrNo) {
    Map<String, Object> applicableRefRangeMap = applicableReferenceRange(mrNo);
    Map allExactReferenceRangeParamIdMap = (Map) applicableRefRangeMap
        .get("allExactReferenceRangeParamIdMap");
    Map refRangeForAllParamIdMap = (Map) applicableRefRangeMap.get("refRangeForAllParamIdMap");
    List<BasicDynaBean> list = new ArrayList<BasicDynaBean>();

    for (int i = 0; i < vitals.size(); i++) {
      BasicDynaBean vitalBean = vitals.get(i);
      int paramId = (int) vitalBean.get("param_id");
      BasicDynaBean refRangeBean = null;
      List<BasicDynaBean> referenceRangeBeanList = 
          (List<BasicDynaBean>) allExactReferenceRangeParamIdMap
          .get(paramId);
      if (referenceRangeBeanList != null && !referenceRangeBeanList.isEmpty()) {
        refRangeBean = referenceRangeBeanList.get(0);
      } else {
        List<BasicDynaBean> refRangeForAllBeanList = (List<BasicDynaBean>) refRangeForAllParamIdMap
            .get(paramId);
        if (refRangeForAllBeanList != null && !refRangeForAllBeanList.isEmpty()) {
          refRangeBean = refRangeForAllBeanList.get(0);
        }
      }
      String refRangeColorCode = getApplicableColorCode(refRangeBean,
          (String) vitalBean.get("param_value"));
      vitalBean.set("reference_range_color_code", refRangeColorCode);
      list.add(vitalBean);
    }

    return list;
  }

  /**
   * Applicable reference range.
   *
   * @param mrNo the mr no
   * @return the map
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Map<String, Object> applicableReferenceRange(String mrNo) {
    List selectedCols = new ArrayList<>();
    selectedCols.add("range_id");
    selectedCols.add("patient_gender");
    selectedCols.add("min_patient_age");
    selectedCols.add("max_patient_age");
    selectedCols.add("range_for_all");
    selectedCols.add("age_unit");
    selectedCols.add("max_improbable_value");
    selectedCols.add("max_critical_value");
    selectedCols.add("max_normal_value");
    selectedCols.add("min_improbable_value");
    selectedCols.add("min_critical_value");
    selectedCols.add("min_normal_value");
    selectedCols.add("priority");
    selectedCols.add("param_id");
    selectedCols.add("reference_range_txt");

    Map filterMap = new HashMap<>();
    filterMap.put("range_for_all", "Y");

    List allExactReferenceRangeList = referenceRangesRepository.getExactReferenceRange(mrNo,
        patientDetailsService.getPatientGender(mrNo));
    List refRangeForAllList = referenceRangesRepository
        .listAll(selectedCols, filterMap, "priority");
    Map allExactReferenceRangeParamIdMap = ConversionUtils.listBeanToMapListBean(
        allExactReferenceRangeList, "param_id");
    Map refRangeForAllParamIdMap = ConversionUtils.listBeanToMapListBean(refRangeForAllList,
        "param_id");

    Map<String, Object> map = new HashMap<>();
    map.put("allExactReferenceRangeParamIdMap", allExactReferenceRangeParamIdMap);
    map.put("refRangeForAllParamIdMap", refRangeForAllParamIdMap);

    return map;
  }

  /**
   * Gets the applicable color code.
   *
   * @param refRangeBean the ref range bean
   * @param value the value
   * @return the applicable color code
   */
  public String getApplicableColorCode(BasicDynaBean refRangeBean, String value) {
    BigDecimal paramValue = null;
    if (null != value && !value.equals("")) {
      try {
        paramValue = new BigDecimal(value);
      } catch (java.lang.NumberFormatException ex) {
        return "normal_color_code";
      }
    }
    if (paramValue == null || paramValue.equals("") || refRangeBean == null) {

      return "normal_color_code";
    } else if (null != refRangeBean.get("max_critical_value")
        && !refRangeBean.get("max_critical_value").equals("")
        && paramValue.floatValue() > ((BigDecimal) refRangeBean.get("max_critical_value"))
            .floatValue()) {
      return "improbable_color_code";

    } else if (null != refRangeBean.get("max_improbable_value")
        && !refRangeBean.get("max_improbable_value").equals("")
        && paramValue.floatValue() > ((BigDecimal) refRangeBean.get("max_improbable_value"))
            .floatValue()) {
      return "critical_color_code";

    } else if (null != refRangeBean.get("max_normal_value")
        && !refRangeBean.get("max_normal_value").equals("")
        && paramValue.floatValue() > ((BigDecimal) refRangeBean.get("max_normal_value"))
            .floatValue()) {
      return "abnormal_color_code";
    } else if (null != refRangeBean.get("min_critical_value")
        && !refRangeBean.get("min_critical_value").equals("")
        && paramValue.floatValue() < ((BigDecimal) refRangeBean.get("min_critical_value"))
            .floatValue()) {
      return "improbable_color_code";

    } else if (null != refRangeBean.get("min_improbable_value")
        && !refRangeBean.get("min_improbable_value").equals("")
        && paramValue.floatValue() < ((BigDecimal) refRangeBean.get("min_improbable_value"))
            .floatValue()) {
      return "critical_color_code";

    } else if (null != refRangeBean.get("min_normal_value")
        && !refRangeBean.get("min_normal_value").equals("")
        && paramValue.floatValue() < ((BigDecimal) refRangeBean.get("min_normal_value"))
            .floatValue()) {
      return "abnormal_color_code";

    } else if ((null != refRangeBean.get("min_normal_value") && !refRangeBean.get(
        "min_normal_value").equals(""))
        || (null != refRangeBean.get("max_normal_value") && !refRangeBean.get("max_normal_value")
            .equals(""))) {

      return "normal_color_code";

    } else {
      return "normal_color_code";
    }
  }
}
