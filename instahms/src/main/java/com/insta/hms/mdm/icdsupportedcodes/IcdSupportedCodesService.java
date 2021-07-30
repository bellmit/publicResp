package com.insta.hms.mdm.icdsupportedcodes;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.codetypeclassification.CodeTypeClassificationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class IcdSupportedCodesService.
 */
@Service
public class IcdSupportedCodesService extends MasterService {

  /** The code type classification service. */
  @LazyAutowired CodeTypeClassificationService codeTypeClassificationService;
  
  /** The icd supported code types repo. */
  @LazyAutowired private IcdSupportedCodeTypesRepository icdSupportedCodeTypesRepo;
  
  /** The icd supported code types validator. */
  @LazyAutowired private IcdSupportedCodeTypesValidator icdSupportedCodeTypesValidator;

  /**
   * Instantiates a new icd supported codes service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public IcdSupportedCodesService(
      IcdSupportedCodesRepository repo, IcdSupportedCodesValidator validator) {
    super(repo, validator);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> refData = new HashMap<String, List<BasicDynaBean>>();
    refData.put("codeTypeClassificationListJson", codeTypeClassificationService.listAll());
    return refData;
  }

  /**
   * Gets the diag code of code type list.
   *
   * @param searchInput the search input
   * @param codeType the code type
   * @return the diag code of code type list
   */
  public List<BasicDynaBean> getDiagCodeOfCodeTypeList(String searchInput, String codeType) {

    icdSupportedCodeTypesValidator.validateDiagCodeParameters(searchInput, codeType);
    return icdSupportedCodeTypesRepo.getDiagCodeOfCodeTypeList(searchInput, codeType);
  }

  /**
   * Gets the mrd supported codes.
   *
   * @return the mrd supported codes
   */
  public Map<String, Object> getMrdSupportedCodes() {
    Map<String, Object> response = new HashMap<>();
    response.put("mrdsupportedcodes",
        ConversionUtils.listBeanToListMap(icdSupportedCodeTypesRepo.listAll()));
    return response;
  }
}
