package com.insta.hms.mdm.icdcodes;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class IcdCodesService.
 */
@Service
public class IcdCodesService extends MasterService {

  @LazyAutowired
  private IcdCodesRepository repository;
  
  /**
   * Instantiates a new ICD codes service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public IcdCodesService(IcdCodesRepository repo, IcdCodesValidator validator) {
    super(repo, validator);
  }
  
  /**
   * Get Patient Problem List.
   * 
   * @param searchInput the search input
   * @param codeType codeType of Problems
   * @return map
   */
  public Map<String, Object> getPatientProblemList(String searchInput, String codeType) {
    Map<String, Object> patientProblems = new HashMap<>();
    List<String> codeTypeList = new ArrayList<>();
    codeTypeList.add(codeType);
    patientProblems.put("patient_problem_list", ConversionUtils
        .listBeanToListMap(repository.getPatientProblemList(searchInput, codeTypeList)));
    return patientProblems;
  }
}
