package com.insta.hms.mdm.codetype;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.mdm.MasterService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CodeTypeService extends MasterService {

  public CodeTypeService(CodeTypeRepository repository, CodeTypeValidator validator) {
    super(repository, validator);
  }

  /**
   * Get list of Code Type.
   *
   * @return response map
   */
  public Map<String, Object> getCodeTypeList() {
    List<BasicDynaBean> result = ((CodeTypeRepository) getRepository())
        .getCodeTypeList();
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("code_type_list", ConversionUtils.listBeanToListMap(result));
    return responseMap;
  }

  /**
   * Get code details by code type.
   *
   * @param searchInput search text
   * @param codeType code type
   * @return response map
   */
  public Map<String, Object> getCodeDetailsByCodeType(String searchInput, String codeType)
      throws Exception {
    List<BasicDynaBean> result = ((CodeTypeRepository) getRepository())
        .getCodeDetailsByCodeType(searchInput, codeType);
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("code_detail_list", ConversionUtils.listBeanToListMap(result));
    return responseMap;
  }

  /**
   * Get Get code types by code category.
   *
   * @param codeCategory category of code
   * @return code type list
   */
  public List<BasicDynaBean> getCodeTypesByCodeCategory(String codeCategory)
      throws Exception {
    List<BasicDynaBean> result = ((CodeTypeRepository) getRepository())
        .getCodeTypesByCodeCategory(codeCategory);
    return result;
  }

}
