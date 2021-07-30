package com.insta.hms.mdm.vitalparameters;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.mdm.MasterService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class VitalParameterService.
 *
 * @author anup vishwas
 */

@Service("parameterService")
public class VitalParameterService extends MasterService {

  /** The vital repository. */
  @LazyAutowired
  VitalParameterRepository vitalRepository;

  /** The pat det service. */
  @LazyAutowired
  PatientDetailsService patDetService;

  /**
   * Instantiates a new vital parameter service.
   *
   * @param vitalParameterRepository
   *          the vital parameter repository
   * @param vitalParameterValidator
   *          the vital parameter validator
   */
  public VitalParameterService(VitalParameterRepository vitalParameterRepository,
      VitalParameterValidator vitalParameterValidator) {
    super(vitalParameterRepository, vitalParameterValidator);
  }

  /**
   * Gets the list page data.
   *
   * @return the list page data
   */
  public Map<String, List<BasicDynaBean>> getListPageData() {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
    map.put("referenceRangeList", vitalRepository.getReferenceRangeList());
    return map;
  }

  /**
   * Gets the adds the edit page data.
   *
   * @param params
   *          the params
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map params) {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
    Map<String, Object> filterMap = new HashMap<String, Object>();
    if (params.get("param_id") != null) {
      String paramId = ((String[]) params.get("param_id"))[0];
      Integer paramIdInt = Integer.parseInt(paramId);
      filterMap.put("param_id", paramIdInt);
      map.put("selectedVitaldList", vitalRepository.lookup(filterMap, null));
    }
    map.put("avlVitalList", vitalRepository.getAllVitals());

    return map;
  }

  /**
   * Gets the active vital params.
   *
   * @param visitType
   *          the visit type
   * @return the active vital params
   */
  public List getActiveVitalParams(String visitType) {

    return vitalRepository.getActiveVitalParams(visitType);
  }

  /**
   * Gets the all params.
   *
   * @param container
   *          the container
   * @param visit
   *          the visit
   * @return the all params
   */
  public List getAllParams(String container, String visit) {

    return vitalRepository.getAllParams(container, visit);
  }

  /**
   * Gets the all params.
   *
   * @param visit the visit
   * @param centerId the center id
   * @param deptId the dept id
   * @return the all params
   */
  public List getAllParams(String visit, int centerId, String deptId) {
    return vitalRepository.getAllParams(visit, centerId, deptId);
  }

  /**
   * List all.
   *
   * @return the list
   */
  public List<BasicDynaBean> listAll() {
    return vitalRepository.listAll();
  }

  /**
   * List all.
   *
   * @param columns
   *          the columns
   * @param filterMap
   *          the filter map
   * @param sortColumn
   *          the sort column
   * @return the list
   */
  public List<BasicDynaBean> listAll(List<String> columns, Map<String, Object> filterMap,
      String sortColumn) {
    return vitalRepository.listAll(columns, filterMap, sortColumn);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterService#toBean(java.util.Map, java.util.Map)
   */
  @Override
  public BasicDynaBean toBean(Map<String, String[]> requestParams,
      Map<String, MultipartFile> fileMap) {
    BasicDynaBean bean = super.toBean(requestParams, fileMap);
    String visitType = (String) bean.get("visit_type");
    visitType = visitType == null || visitType.isEmpty() ? null : visitType;
    bean.set("visit_type", visitType);
    return bean;

  }

  public List<BasicDynaBean> getUniqueVitalsforPatient(String patientId) {

    return vitalRepository.getUniqueVitalsforPatient(patientId);
  }

}
