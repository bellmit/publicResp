package com.insta.hms.mdm.departments;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.departmenttypes.DepartmentTypeService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DepartmentService.
 */
@Service
public class DepartmentService extends MasterService {

  /** The logger. */
  Logger logger = LoggerFactory.getLogger(DepartmentService.class);

  /** The department type service. */
  @LazyAutowired
  private DepartmentTypeService departmentTypeService;

  /** The department repository. */
  private DepartmentRepository departmentRepository;

  /**
   * Instantiates a new department service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public DepartmentService(DepartmentRepository repo, DepartmentValidator validator) {
    super(repo, validator);
    this.departmentRepository = repo;
  }

  /**
   * Gets the adds the edit page data.
   *
   * @param requestParams the request params
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map<String, String[]> requestParams) {
    Map<String, List<BasicDynaBean>> referenceMap = new HashMap<>();
    List<BasicDynaBean> departmenttypes = departmentTypeService.lookup(true);
    referenceMap.put("departmenttypes", departmenttypes);
    referenceMap.put("departments", lookup(false));
    return referenceMap;
  }

  /**
   * Gets the list page data.
   *
   * @param requestParams the request params
   * @return the list page data
   */
  public Map<String, List<BasicDynaBean>> getListPageData(Map<String, String[]> requestParams) {
    Map<String, List<BasicDynaBean>> referenceMap = new HashMap<>();
    List<BasicDynaBean> departmenttypes = departmentTypeService.lookup(true);
    referenceMap.put("departmenttypes", departmenttypes);
    referenceMap.put("departments", lookup(false));
    return referenceMap;
  }

  /**
   * Gets the department details.
   *
   * @param deptId the dept id
   * @return the department details
   */
  public Map<String, Object> getDepartmentDetails(String deptId) {
    BasicDynaBean deptBean = departmentRepository.findByKey("dept_id", deptId);
    if (deptBean == null) {
      logger.error("No record exists for deptId :" + deptId);
    }
    return (deptBean != null) ? deptBean.getMap() : null;
  }

  /**
   * Gets the non clinical departments.
   *
   * @return the non clinical departments
   */
  public List<BasicDynaBean> getNonClinicalDepartments() {
    return departmentRepository.getNonClinicalDepartments();
  }

  /**
   * List all.
   *
   * @param columns the columns
   * @param filterBy the filter by
   * @param filterValue the filter value
   * @param sortColumn the sort column
   * @return the list
   */
  public List<BasicDynaBean> listAll(List<String> columns, String filterBy, Object filterValue,
      String sortColumn) {
    return departmentRepository.listAll(columns, filterBy, filterValue, sortColumn);

  }

  /**
   * Gets the all departments data.
   *
   * @param sendOnlyActiveData the send only active data
   * @return the all departments data
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getAllDepartmentsData(boolean sendOnlyActiveData) {
    return ConversionUtils.listBeanToListMap(departmentRepository
        .getAllDepartments(sendOnlyActiveData));
  }

  /**
   * Gets the departments for prescription.
   *
   * @param searchQuery the search query
   * @param itemLimit the item limit
   * @return the departments for prescription
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getDepartmentsForPrescription(String searchQuery,
      Integer itemLimit) {
    return ConversionUtils.listBeanToListMap(departmentRepository.getDepartmentsForPrescription(
        searchQuery, itemLimit));
  }

}
