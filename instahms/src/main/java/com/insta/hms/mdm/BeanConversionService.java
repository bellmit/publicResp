package com.insta.hms.mdm;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * The Interface BeanConversionService for conversion from request parameters to beans.
 */
public interface BeanConversionService {

  public Map<String, List<BasicDynaBean>> toBeanList(Map<String, String[]> requestParams,
      BasicDynaBean type);

  /**
   * To bean.
   *
   * @param requestParams
   *          the request params
   * @return the basic dyna bean
   */
  public BasicDynaBean toBean(Map<String, String[]> requestParams);

  /**
   * To bean.
   *
   * @param requestParams
   *          the request params
   * @param fileMap
   *          the file map
   * @return the basic dyna bean
   */
  public BasicDynaBean toBean(Map<String, String[]> requestParams,
      Map<String, MultipartFile> fileMap);
}
