package com.insta.hms.mdm.practitionertypes;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PractitionerTypeMappingsService {

  @LazyAutowired private PractitionerTypeMappingsRepository repository;

  @LazyAutowired private PractitionerTypeMappingsValidator validator;

  public List<BasicDynaBean> getPractitionerMappings(Integer centerId) {
    return repository.getPractitionerMappings(centerId);
  }
  
  public List<BasicDynaBean> getPractitionerMappings(Integer centerId,Integer practionerId) {
    return repository.getPractitionerMappings(centerId,practionerId);
  }

  /**
   * insert practitioner type mapping.
   * @param bean mapping bean
   * @return number of rows affected
   */
  @Transactional(rollbackFor = Exception.class)
  public Integer insert(BasicDynaBean bean) {
    validator.validateInsert(bean);
    return repository.insert(bean);
  }

  /**
   * update practitioner type mapping.
   * @param requestBody update data
   * @return number of rows affected
   */
  @Transactional(rollbackFor = Exception.class)
  public Integer update(ModelMap requestBody) {
    Map<String, Object> keys = (Map<String, Object>) requestBody.get("existing");
    BasicDynaBean bean = repository.getBean();
    ConversionUtils.copyToDynaBean((Map) requestBody.get("new"), bean);
    validator.validateUpdate(bean);
    return repository.update(bean, keys);
  }

  /**
   * create bean.
   * @param requestBody data
   * @return bean
   */
  public BasicDynaBean toBean(ModelMap requestBody) {
    BasicDynaBean bean = repository.getBean();
    List<String> errorFields = new ArrayList<String>();
    ConversionUtils.copyJsonToDynaBean(requestBody, bean, errorFields, true);
    return bean;
  }

  /**
   * return list by center id.
   * @param centerId center id
   * @return list
   */
  public List<BasicDynaBean> listByCenterId(int centerId) {
    List<String> columns = new ArrayList<String>();
    columns.add("practitioner_id");
    columns.add("consultation_type_id");
    columns.add("visit_type");
    return repository.listAll(columns, "center_id", centerId);
  }

  /**
   * return bean.
   * @return bean
   */
  public BasicDynaBean getBean() {
    return repository.getBean();
  }

  /**
   * batch update method.
   * @param beans list of beans
   * @param keys keys
   * @return int array
   */
  public int[] batchUpdate(List<BasicDynaBean> beans, Map<String, Object> keys) {
    return repository.batchUpdate(beans, keys);
  }

  /**
   * batch insert method.
   * @param beans list of beans
   * @return int array
   */
  public int[] batchInsert(List<BasicDynaBean> beans) {
    return repository.batchInsert(beans);
  }

  /**
   * batch delete method.
   * @param key key
   * @param values values
   * @return int array
   */
  public int[] batchDelete(String key, List<Object> values) {
    return repository.batchDelete(key, values);
  }
  
  public BasicDynaBean findByKey(Map<String, Object> filterMap) {
    return repository.findByKey(filterMap);
  }
}
