package com.insta.hms.mdm.codesets;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.HMSException;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CodeSetsService extends MasterService {

  @Autowired
  private CodeSetsRepository repository;
  
  @LazyAutowired
  private CodeSystemsRepository codeSystemsRepository;
  
  @LazyAutowired
  private CodeSystemCategoriesRepository codeSystemCategoriesRepository;
  
  @LazyAutowired
  private BeanFactory beanfactory;
  
  public static final Map<String, Object> CODE_SET_CACHED_MAP = new HashMap<String, Object>();
  
  public CodeSetsService(CodeSetsRepository repository,
      CodeSetsValidator validator) {
    super(repository, validator);
  }
  
  @SuppressWarnings("rawtypes")
  public PagedList search(Map params) {
    return repository.getCodeSets(params);
  }
  
  @SuppressWarnings("rawtypes")
  public PagedList searchForDownload(Map params) {
    return repository.getCodeSetsForDownload(params);
  }
  
  /**
   * Get Code System category Label.
   * 
   * @param codeSystemCategoryId the id
   * @return the string
   */
  public String getCodeSystemCategoryLabel(int codeSystemCategoryId) {
    BasicDynaBean bean = repository.getCodeSystemCategoryLabel(codeSystemCategoryId);
    if (bean != null) {
      return bean.get("label").toString();
    }
    return "";
  }
  
  /**
   * Get Code System Label.
   * 
   * @param codeSystemId the id
   * @return the string
   */
  public String getCodeSystemLabel(int codeSystemId) {
    BasicDynaBean bean = repository.getCodeSystemLabel(codeSystemId);
    if (bean != null) {
      return bean.get("label").toString();
    }
    return "";
  }

  /**
   * Get code sets for message creation.
   * 
   * @return map
   */
  public Map<String, Object> getCodeSets() {
    List<BasicDynaBean> codeSystemsList = codeSystemsRepository.listAll();
    List<BasicDynaBean> codeSystemsCategoryList = codeSystemCategoriesRepository.listAll();

    Map<String, Object> codeSetMap = new HashMap<>();
    codeSetMap.put("code",
        getCodeSetSeperatlyCodeSystemWise(codeSystemsList, codeSystemsCategoryList, "short_code"));
    codeSetMap.put("code_desc",
        getCodeSetSeperatlyCodeSystemWise(codeSystemsList, codeSystemsCategoryList, "label"));
    return codeSetMap;
  }

  private Map<String, Object> getSpecificCodeSetColumn(int codeSystemId, int codeSystemsCategoryId,
      String columnName) {
    Map<String, Object> params = new HashMap<>();
    params.put("code_system_category_id", codeSystemsCategoryId);
    params.put("code_system_id", codeSystemId);
    List<BasicDynaBean> codeSetList = repository.findByCriteria(params);
    Map<String, Object> codeSet = new HashMap<>();
    for (BasicDynaBean bean : codeSetList) {
      if (!StringUtils.isEmpty(bean.get(columnName))) {
        codeSet.put(String.valueOf(bean.get("entity_id")), bean.get(columnName));
        if (null != bean.get("is_default") && (boolean) bean.get("is_default")) {
          codeSet.put("default", bean.get(columnName));
        }
      }
    }
    return codeSet;
  }

  private Map<String, Object> getCodeSetSeperatlyCodeSystemCategoryWise(int codeSystemId,
      List<BasicDynaBean> codeSystemsCategoryList, String columnName) {
    Map<String, Object> codeSet = new HashMap<>();
    Map<String, Object> cset;
    String label;
    for (BasicDynaBean bean : codeSystemsCategoryList) {
      label = (int) bean.get("id") == 6 ? "nationality" : String.valueOf(bean.get("table_name"));
      cset = getSpecificCodeSetColumn(codeSystemId, (int) bean.get("id"), columnName);
      if (!cset.isEmpty()) {
        codeSet.put(label.toLowerCase(), cset);
      }
    }
    return codeSet;
  }

  private Map<String, Object> getCodeSetSeperatlyCodeSystemWise(List<BasicDynaBean> codeSystemsList,
      List<BasicDynaBean> codeSystemsCategoryList, String columnName) {
    Map<String, Object> codeSet = new HashMap<>();
    Map<String, Object> cset;
    for (BasicDynaBean bean : codeSystemsList) {
      cset = getCodeSetSeperatlyCodeSystemCategoryWise((int) bean.get("id"),
          codeSystemsCategoryList, columnName);
      if (!cset.isEmpty()) {
        codeSet.put(String.valueOf(bean.get("label")).toLowerCase(), cset);
      }

    }
    return codeSet;
  }

  /**
   * Save Code Sets.
   * 
   * @param paramMap the map
   */
  public void saveCodeSets(Map<String, String[]> paramMap) {
    String[] code = paramMap.get("code");
    String[] codeDescription = paramMap.get("code_description");
    String[] entityId = paramMap.get("entity_id");
    String[] codeSetId = paramMap.get("code_set_id");
    String[] codeSystemCategoryId = paramMap.get("code_system_category_id");
    String[] codeSystemId = paramMap.get("code_system_id");
    int length = code.length;

    BasicDynaBean codeSetBean = null;
    List<BasicDynaBean> insertCodeSetBeanList = new ArrayList<>();
    List<BasicDynaBean> updateCodeSetBeanList = new ArrayList<>();
    List<Object> updateKeys = new ArrayList<>();
    Map<String, Object> updateKeysMap = new HashMap<>();
    for (int count = 0; count < length; count++) {
      codeSetBean = repository.getBean();
      codeSetBean.set("code_system_category_id", Integer.parseInt(codeSystemCategoryId[count]));
      codeSetBean.set("code_system_id", Integer.parseInt(codeSystemId[count]));
      codeSetBean.set("entity_id", Integer.parseInt(entityId[count]));
      codeSetBean.set("label", codeDescription[count]);
      codeSetBean.set("short_code", code[count]);
      if (StringUtils.isEmpty(codeSetId[count])) {
        // inserts new code sets record if code and code desc is not empty
        if (!StringUtils.isEmpty(codeDescription[count]) && !StringUtils.isEmpty(code[count])) {
          codeSetBean.set("id", repository.getNextId());
          insertCodeSetBeanList.add(codeSetBean);
        }
      } else {
        // updates existing code sets record
        updateKeys.add(Integer.parseInt(codeSetId[count]));
        updateCodeSetBeanList.add(codeSetBean);
      }
    }
    if (!insertCodeSetBeanList.isEmpty()) {
      repository.batchInsert(insertCodeSetBeanList);
    }
    if (!updateCodeSetBeanList.isEmpty() && !updateKeys.isEmpty()) {
      updateKeysMap.put("id", updateKeys);
      repository.batchUpdate(updateCodeSetBeanList, updateKeysMap);
    }
    CODE_SET_CACHED_MAP.put(RequestContext.getSchema(), null);
  }
  
  /**
   * Import bulk code sets.
   * 
   * @param mapList the list of map
   */
  @Transactional(rollbackFor = Exception.class)
  public void importBulkCodeSets(List<Map<String, Object>> mapList) {
    List<BasicDynaBean> codeSetBeanListInsert = new ArrayList<>();
    List<BasicDynaBean> codeSetBeanListUpdate = new ArrayList<>();
    List<Object> updateKeys = new ArrayList<>();
    Map<String, Object> updateKeysMap = new HashMap<>();
    List<String> errors = new ArrayList<>();
    
    Map<String, Object> codeSetMap;
    BasicDynaBean tempBean = null;
    BasicDynaBean codeSystemCategoryBean = null;
    BasicDynaBean codeSetBean = null;
    Map<String,GenericRepository> beanMap = new HashMap<>();
    for (Map<String, Object> map : mapList) {
      codeSetMap = new HashMap<>();
      codeSetBean = repository.getBean();
      
      // code system category id
      if (!StringUtils.isEmpty(map.get("master_name"))) {
        codeSystemCategoryBean =
            codeSystemCategoriesRepository.findByKey("label", map.get("master_name"));
        if (codeSystemCategoryBean != null) {
          codeSetMap.put("code_system_category_id", codeSystemCategoryBean.get("id"));
        } else {
          errors.add("Invalid master name :" + map.get("master_name"));
        }
        if (beanMap.get(map.get("master_name")) == null) {
          beanMap.put((String) map.get("master_name"),
              new GenericRepository((String) codeSystemCategoryBean.get("table_name")));
        }
      }
      
      // code system id
      if (!StringUtils.isEmpty(map.get("code_system"))) {
        tempBean = codeSystemsRepository.findByKey("label",map.get("code_system"));
        if (tempBean != null) {
          codeSetMap.put("code_system_id",tempBean.get("id"));
        } else {
          errors.add("Invalid code system name :" + map.get("code_system"));
        }
      }
      
      // entity id
      if (!StringUtils.isEmpty(map.get("entity_name"))) {
        tempBean = beanMap.get((String) map.get("master_name"))
            .findByKey((String) codeSystemCategoryBean.get("entity_name"), map.get("entity_name"));
        if (tempBean != null) {
          codeSetMap.put("entity_id",
              tempBean.get((String) codeSystemCategoryBean.get("entity_id")));
        } else {
          errors.add("Invalid entity name :" + map.get("entity_name"));
        }
      }
      
      // code set id
      tempBean = repository.findByKey(codeSetMap);
      if (tempBean != null) {
        codeSetMap.put("label", map.get("code_description"));
        codeSetMap.put("short_code", map.get("code"));
        ConversionUtils.copyJsonToDynaBean(codeSetMap, codeSetBean, errors, true);
        updateKeys.add(tempBean.get("id"));
        codeSetBeanListUpdate.add(codeSetBean);
      } else {
        codeSetMap.put("label", map.get("code_description"));
        codeSetMap.put("short_code", map.get("code"));
        ConversionUtils.copyJsonToDynaBean(codeSetMap, codeSetBean, errors, true);
        codeSetBean.set("id", repository.getNextId());
        codeSetBeanListInsert.add(codeSetBean);
      }
    }
    if (!updateKeys.isEmpty()) {
      updateKeysMap.put("id", updateKeys);
    }
    if (errors.isEmpty()) {
      if (!codeSetBeanListInsert.isEmpty()) {
        repository.batchInsert(codeSetBeanListInsert);
      }
      if (!codeSetBeanListUpdate.isEmpty()) {
        repository.batchUpdate(codeSetBeanListUpdate, updateKeysMap);
      }
    } else {
      throw new HMSException(errors);
    }
    CODE_SET_CACHED_MAP.put(RequestContext.getSchema(), null);
  }
  
  /**
   * Search default code sets.
   * 
   * @param params the map
   * @return list
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<Map<String, Object>> searchDefaultCodeSets(Map params) {
    Map<String, Object> flattenMap = ConversionUtils.flatten(params);
    int codeSystemCategoryId = !StringUtils.isEmpty(flattenMap.get("code_system_category_id"))
        ? Integer.parseInt((String) flattenMap.get("code_system_category_id"))
        : 0;
    int codeSystemId = !StringUtils.isEmpty(flattenMap.get("code_systems_id"))
        ? Integer.parseInt((String) flattenMap.get("code_systems_id"))
        : 0;

    List<BasicDynaBean> defaultCodeSetListBean =
        repository.getDefaultCodeSets(codeSystemId, codeSystemCategoryId);
    List<Map<String, Object>> defaultCodeSetListMap = new ArrayList<>();
    Map<String, Object> map = null;
    for (BasicDynaBean bean : defaultCodeSetListBean) {
      map = new HashMap<>();
      map.putAll(bean.getMap());
      map.put("all_codes",
          getDistinctCodesets(codeSystemId, (int) bean.get("code_system_category_id")));
      defaultCodeSetListMap.add(map);
    }
    return defaultCodeSetListMap;
  }

  private List<Map<String, Object>> getDistinctCodesets(int codeSystemId,
      int codeSystemCategoryId) {
    List<BasicDynaBean> codesBeanList =
        repository.getDistinctCodesets(codeSystemId, codeSystemCategoryId);
    List<Map<String, Object>> codes = new ArrayList<>();
    Map<String, Object> map = null;
    for (BasicDynaBean bean : codesBeanList) {
      map = new HashMap<>();
      map.put("code", bean.get("short_code"));
      map.put("code_desc", bean.get("label"));
      codes.add(map);
    }
    return codes;
  }
  
  /**
   * Update default code set.
   * 
   * @param paramMap the map
   */
  @Transactional(rollbackFor = Exception.class)
  public void saveDefaultCodeSets(Map<String, String[]> paramMap) {
    String[] codeSystemCategoryId = paramMap.get("code_sys_cat_id");
    String[] codeSystemId = paramMap.get("code_sys_id");
    int length = codeSystemCategoryId.length;

    int codeSysCategoryId;
    int codeSysId;
    for (int count = 0; count < length; count++) {

      codeSysCategoryId = Integer.parseInt(codeSystemCategoryId[count]);
      codeSysId = Integer.parseInt(codeSystemId[count]);

      repository.updateDefault(codeSysId, codeSysCategoryId,
          paramMap.get(codeSysCategoryId + "_code")[0]);
    }
  }
}
