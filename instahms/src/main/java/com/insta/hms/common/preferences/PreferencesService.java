package com.insta.hms.common.preferences;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.mdm.BeanConversionService;
import com.insta.hms.mdm.MasterValidator;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * the service class for single-record table.
 *
 * @author aditya.b
 */
public class PreferencesService implements BeanConversionService {

  /** The repository. */
  PreferencesRepository repository;
  
  /** The validator. */
  protected MasterValidator validator;

  /**
   * Instantiates a new preferences service.
   *
   * @param preferencesRepository the preferences repository
   * @param masterValidator the master validator
   */
  public PreferencesService(PreferencesRepository preferencesRepository, 
      MasterValidator masterValidator) {
    this.repository = preferencesRepository;
    this.validator = masterValidator;
  }

  /**
   * Gets the preferences.
   *
   * @return all preferences record if view query is null; otherwise returns the query result
   */
  public BasicDynaBean getPreferences() {
    return repository.getRecord(repository.getViewQuery());
  }

  /**
   * Gets all the columns of the preferences.
   *
   * @return the all preferences
   */
  public BasicDynaBean getAllPreferences() {
    return repository.getRecord();
  }

  /**
   * Update preferences.
   *
   * @param bean
   *          the bean
   * @return number of rows affected
   */
  @Transactional(rollbackFor = Exception.class)
  public Integer update(BasicDynaBean bean) {
    // validation call
    return repository.update(bean, null);
  }

  /**
   * Convert request parameters to BasicDynaBean.
   *
   * @param requestParams the request params
   * @return bean of parameters
   */
  @Override
  public BasicDynaBean toBean(Map<String, String[]> requestParams) {
    return toBean(requestParams, null);
  }

  /**
   * Convert request parameters to BasicDynaBean.
   *
   * @param requestParams the request params
   * @param fileMap the file map
   * @return bean of parameters
   */
  @Override
  public BasicDynaBean toBean(Map<String, String[]> requestParams,
      Map<String, MultipartFile> fileMap) {
    List<String> errorFields = new ArrayList<>();
    Map<String, Object> multipartRequestParameters = new HashMap<String, Object>(requestParams);
    if (null != fileMap && !(fileMap.isEmpty())) {
      multipartRequestParameters.putAll(fileMap);
    }

    BasicDynaBean bean = repository.getBean();
    ConversionUtils.copyToDynaBean(multipartRequestParameters, bean, errorFields);
    if (!errorFields.isEmpty()) {
      throw new ConversionException(errorFields); 
    }
    return bean;
  }

  /**
   * Gets the repository.
   *
   * @return repository instance
   */
  protected PreferencesRepository getRepository() {
    return repository;
  }

  @Override
  public Map<String, List<BasicDynaBean>> toBeanList(Map<String, String[]> requestParams,
      BasicDynaBean type) {
    return null;
  }

}
