package com.insta.hms.mdm;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.exception.HMSException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class MasterDetailsService.
 */
public class MasterDetailsService extends MasterService {

  /** The detail repositories. */
  private List<MasterRepository<?>> detailRepositories = new ArrayList<MasterRepository<?>>();

  /**
   * Instantiates a new master details service.
   *
   * @param repository
   *          the repository
   * @param validator
   *          the validator
   * @param detailsRepo
   *          the details repo
   */
  public MasterDetailsService(MasterRepository<?> repository, MasterValidator validator,
      MasterRepository<?>... detailsRepo) {
    super(repository, validator);
    List<MasterRepository<?>> repos = Arrays.asList(detailsRepo);
    detailRepositories.addAll(repos);
  }

  /** The session service. */
  @LazyAutowired
  SessionService sessionService;

  /**
   * Find details by PK.
   *
   * @param params
   *          the params
   * @return the map
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map<String, List<Map>> findDetailsByPk(Map params) {
    Map<String, List<Map>> listbean = new HashMap<String, List<Map>>();
    if (null != detailRepositories) {
      for (MasterRepository<?> d : detailRepositories) {
        String tableName = d.getBeanName();

        String mainPk = getRepository().getKeyColumn();
        String mainPkValue = ((String[]) params.get(mainPk))[0];
        Map<String, Object> filterMap = new HashMap<String, Object>();
        filterMap.put(mainPk, ConvertUtils.convert(mainPkValue, getRepository().getPkType()));
        List<BasicDynaBean> temp = d.listAll(null, filterMap, d.getSortColumn());
        listbean.put("list_" + tableName, ConversionUtils.listBeanToListMap(temp));
      }
    }
    return listbean;
  }

  /**
   * Insert details.
   *
   * @param parent
   *          the parent
   * @param detailListMap
   *          the detail list map
   * @return the int
   */
  @Transactional
  public int insertDetails(BasicDynaBean parent, Map<String, List<BasicDynaBean>> detailListMap) {
    Integer res = super.insert(parent);
    /*
     * if(res != 0) { // TODO : throws exception for transaction roll back.If there is no any
     * transaction error and res is less then 0 }
     */
    String parentKeyCol = getRepository().getKeyColumn();
    Object parentKeyVal = parent.get(parentKeyCol);
    for (MasterRepository<?> repo : detailRepositories) {
      String beanName = repo.getBeanName();
      List<BasicDynaBean> beanList = detailListMap.get(beanName + "_inserted");
      for (BasicDynaBean bean : beanList) {
        bean.set(parentKeyCol, parentKeyVal);
        String detailKeyColumn = repo.getKeyColumn();
        Object nextId = null;
        if (null != detailKeyColumn && repo.supportsAutoId()) {
          nextId = repo.getNextId();
          bean.set(detailKeyColumn, nextId);
          repo.insert(bean);
        }
      }
    }
    return res;
  }

  /**
   * Insert details map.
   *
   * @param parentBean
   *          the parent bean
   * @param detailMapBean
   *          the detail map bean
   */
  @Transactional(rollbackFor = Exception.class)
  public void insertDetailsMap(BasicDynaBean parentBean,
      Map<String, Map<String, Map<String, BasicDynaBean>>> detailMapBean) {

    String userId = (String) sessionService.getSessionAttributes().get("userId");

    parentBean.set("created_by", userId);
    super.insert(parentBean);

    String parentKeyCol = getRepository().getKeyColumn();
    Object parentKeyVal = parentBean.get(parentKeyCol);

    /* Setting the parentKey for detailBeans */
    setBeansForiegnKey(parentBean, detailMapBean);

    /* Validate All the details Table Beans */
    ((MasterDetailsValidator) validator).validateDetailTables(detailMapBean);

    for (MasterRepository<?> repo : detailRepositories) {
      String beanName = repo.getBeanName();
      String detailKeyColumn = repo.getKeyColumn();
      Map<String, BasicDynaBean> insertBeansMap = detailMapBean.get(beanName).get("insert");
      List<BasicDynaBean> insertBeanList = new ArrayList<BasicDynaBean>();
      for (Map.Entry<String, BasicDynaBean> insertMapEntry : insertBeansMap.entrySet()) {
        BasicDynaBean bean = insertMapEntry.getValue();
        bean.set(parentKeyCol, parentKeyVal);
        if (null != detailKeyColumn && repo.supportsAutoId()) {
          Object nextId = repo.getNextId();
          bean.set(detailKeyColumn, nextId);
          bean.set("created_by", userId);
          insertBeanList.add(bean);
        }
      }
      repo.batchInsert(insertBeanList);
    }
  }

  /**
   * Update details.
   *
   * @param parentBean
   *          the parent bean
   * @param detailListMapBean
   *          the detail list map bean
   * @return the int[]
   */
  @Transactional
  public int[] updateDetails(BasicDynaBean parentBean,
      Map<String, List<BasicDynaBean>> detailListMapBean) {

    int[] status = null;
    String parentKeyCol = getRepository().getKeyColumn();
    Map<String, Object> mainKeys = new HashMap<String, Object>();
    mainKeys.put(parentKeyCol, parentBean.get(parentKeyCol));
    Integer res = getRepository().update(parentBean, mainKeys);
    /*
     * if(res != 0) { // TODO : throws exception for transaction roll back.If there is no any
     * transaction error and res is less then 0 }
     */
    for (MasterRepository<?> repo : detailRepositories) {
      String beanName = repo.getBeanName();

      /* Update the bean */
      List<BasicDynaBean> beanListUpdated = detailListMapBean.get(beanName + "_updated");

      for (BasicDynaBean bean : beanListUpdated) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(repo.getKeyColumn(), bean.get(repo.getKeyColumn()));
        repo.update(bean, keys);
      }

      /* Delete the bean */
      List<BasicDynaBean> beanListDeleted = detailListMapBean.get(beanName + "_deleted");
      for (BasicDynaBean bean : beanListDeleted) {
        repo.delete(repo.getKeyColumn(), bean.get(repo.getKeyColumn()));
      }

      /* Insert the bean */
      List<BasicDynaBean> beanListInserted = detailListMapBean.get(beanName + "_inserted");
      Object parentKeyVal = parentBean.get(parentKeyCol);
      for (BasicDynaBean bean : beanListInserted) {
        bean.set(parentKeyCol, parentKeyVal);
        String detailKeyColumn = repo.getKeyColumn();
        Object nextId = null;
        if (null != detailKeyColumn && repo.supportsAutoId()) {
          nextId = repo.getNextId();
          bean.set(detailKeyColumn, nextId);
        }
      }
      repo.batchInsert(beanListInserted);
    }

    return status;
  }

  /**
   * Update details map.
   *
   * @param parentBean
   *          the parent bean
   * @param detailMapBean
   *          the detail map bean
   */
  @Transactional(rollbackFor = Exception.class)
  public void updateDetailsMap(BasicDynaBean parentBean,
      Map<String, Map<String, Map<String, BasicDynaBean>>> detailMapBean) {
    String parentKeyCol = getRepository().getKeyColumn();
    Object parentKeyVal = parentBean.get(parentKeyCol);
    Map<String, Object> mainKeys = new HashMap<String, Object>();
    mainKeys.put(parentKeyCol, parentKeyVal);
    BasicDynaBean existingParentBean = this.findByPk(mainKeys);
    if (existingParentBean == null) {
      throw new EntityNotFoundException(new String[] { getRepository().getBeanName(),
          getRepository().getKeyColumn(),
          parentBean.get(parentKeyCol) == null ? "" : parentBean.get(parentKeyCol).toString() });
    }

    /* Updating the Parent Table. */
    parentBean.set("modified_by", sessionService.getSessionAttributes().get("userId"));
    parentBean.set("modified_at", DateUtil.getCurrentTimestamp());
    super.update(parentBean);

    /* Setting the parentKey for detailBeans */
    setBeansForiegnKey(parentBean, detailMapBean);

    /* Validate All the details Table Beans */
    ((MasterDetailsValidator) validator).validateDetailTables(detailMapBean);

    for (MasterRepository<?> repo : detailRepositories) {
      String beanName = repo.getBeanName();
      String detailKeyColumn = repo.getKeyColumn();
      List<Object> updatedList = new ArrayList<Object>();

      /* Updating the old Beans */
      Map<String, BasicDynaBean> updateBeansMap = detailMapBean.get(beanName).get("update");
      for (Map.Entry<String, BasicDynaBean> updateMapEntry : updateBeansMap.entrySet()) {
        BasicDynaBean bean = updateMapEntry.getValue();
        bean.set("modified_by", sessionService.getSessionAttributes().get("userId"));
        bean.set("modified_at", DateUtil.getCurrentTimestamp());
        updatedList.add(bean.get(repo.getKeyColumn()));
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(repo.getKeyColumn(), bean.get(repo.getKeyColumn()));
        repo.update(bean, keys);
      }

      /*
       * Deleting the detail components which are not updated. deleting should be done before
       * insert.
       */
      Map<String, Object> parameterMap = new HashMap<String, Object>();
      parameterMap.put(parentKeyCol, parentBean.get(parentKeyCol));
      if (updatedList.size() == 0) {
        repo.delete(parameterMap);
      } else {
        /* Running a NOT IN Query if beans area updated. */
        parameterMap.put(detailKeyColumn, updatedList);
        repo.delete(parameterMap, true);
      }

      /* Inserting the new Beans */
      Map<String, BasicDynaBean> insertBeansMap = detailMapBean.get(beanName).get("insert");
      List<BasicDynaBean> insertBeanList = new ArrayList<BasicDynaBean>();
      for (Map.Entry<String, BasicDynaBean> insertMapEntry : insertBeansMap.entrySet()) {
        BasicDynaBean bean = insertMapEntry.getValue();
        bean.set(parentKeyCol, parentKeyVal);
        if (null != detailKeyColumn && repo.supportsAutoId()) {
          Object nextId = repo.getNextId();
          bean.set(detailKeyColumn, nextId);
          bean.set("created_by", sessionService.getSessionAttributes().get("userId"));
          insertBeanList.add(bean);
        }
      }
      repo.batchInsert(insertBeanList);
    }
  }

  @Override
  public Map<String, List<BasicDynaBean>> toBeanList(Map<String, String[]> requestParams,
      BasicDynaBean type) {
    List<String> errorFields = new ArrayList<String>();
    Map<String, List<BasicDynaBean>> beanListMap = new HashMap<String, List<BasicDynaBean>>();
    for (MasterRepository<?> repo : detailRepositories) {
      String childPk = repo.getKeyColumn();
      List<BasicDynaBean> insertedbeans = new ArrayList<BasicDynaBean>();
      List<BasicDynaBean> deleatedBeans = new ArrayList<BasicDynaBean>();
      List<BasicDynaBean> updatedBeans = new ArrayList<BasicDynaBean>();
      // Only 1st index will be consider as a key column
      String[] names = requestParams.get(repo.getUniqueNameColumn()[0]);
      String[] deletedItem = requestParams.get("deleted");
      if (names != null) {
        for (int i = 0; i < names.length; i++) {
          BasicDynaBean bean = repo.getBean();
          ConversionUtils.copyIndexToDynaBean(requestParams, i, bean, errorFields, true);
          if (deletedItem != null && Boolean.parseBoolean(deletedItem[i])) {
            if (bean.get(repo.getKeyColumn()) == null) {
              continue; // Empty delete checks
            }
            deleatedBeans.add(bean);
          } else if (bean.get(childPk) == null || bean.get(repo.getKeyColumn()) == "") {
            insertedbeans.add(bean);
          } else {
            updatedBeans.add(bean);
          }
        }
      }
      beanListMap.put(repo.getBeanName() + "_inserted", insertedbeans);
      beanListMap.put(repo.getBeanName() + "_deleted", deleatedBeans);
      beanListMap.put(repo.getBeanName() + "_updated", updatedBeans);
    }
    return beanListMap;
  }

  /**
   * To beans map.
   *
   * @param requestBody
   *          the request body
   * @param parentBean
   *          the parent bean
   * @return the map
   */
  @SuppressWarnings("unchecked")
  public Map<String, Map<String, Map<String, BasicDynaBean>>> toBeansMap(ModelMap requestBody,
      BasicDynaBean parentBean) {
    List<String> errorFields = new ArrayList<String>();
    Map<String, Map<String, Map<String, BasicDynaBean>>> beanListMap = 
        new HashMap<String, Map<String, Map<String, BasicDynaBean>>>();
    for (MasterRepository<?> repo : detailRepositories) {
      List<Map<String, Object>> detailsList = (List<Map<String, Object>>) requestBody
          .get(repo.getBeanName());
      String childPk = repo.getKeyColumn();
      detailsList = detailsList == null ? Collections.EMPTY_LIST : detailsList;
      String foreinKey = this.getRepository().getKeyColumn();
      Map<String, Map<String, BasicDynaBean>> repoMap = 
          new HashMap<String, Map<String, BasicDynaBean>>();
      Map<String, BasicDynaBean> insertedMap = new HashMap<String, BasicDynaBean>();
      Map<String, BasicDynaBean> updatedMap = new HashMap<String, BasicDynaBean>();
      for (int i = 0; i < detailsList.size(); i++) {
        Map<String, Object> detail = detailsList.get(i);
        if ((detail.get(foreinKey) != null && !"".equals(detail.get(foreinKey)))
            && !((detail.get(foreinKey)).equals(requestBody.get(foreinKey)))) {
          throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
        }
        BasicDynaBean bean = repo.getBean();
        ConversionUtils.copyJsonToDynaBean(detail, bean, errorFields, true);
        if (!errorFields.isEmpty()) {
          throw new ConversionException(errorFields);
        }
        if (bean.get(childPk) == null || "".equals(bean.get(childPk))) {
          insertedMap.put(String.valueOf(i), bean);
        } else {
          updatedMap.put(String.valueOf(i), bean);
        }

      }
      repoMap.put("insert", insertedMap);
      repoMap.put("update", updatedMap);
      beanListMap.put(repo.getBeanName(), repoMap);
    }
    return beanListMap;
  }

  /**
   * Sets the beans foriegn key.
   *
   * @param parentBean
   *          the parent bean
   * @param detailMapBean
   *          the detail map bean
   */
  private void setBeansForiegnKey(BasicDynaBean parentBean,
      Map<String, Map<String, Map<String, BasicDynaBean>>> detailMapBean) {
    String parentKeyCol = getRepository().getKeyColumn();
    Object parentKeyVal = parentBean.get(parentKeyCol);
    for (MasterRepository<?> repo : detailRepositories) {
      String beanName = repo.getBeanName();
      Map<String, BasicDynaBean> insertBeansMap = detailMapBean.get(beanName).get("insert");
      for (Map.Entry<String, BasicDynaBean> insertMapEntry : insertBeansMap.entrySet()) {
        BasicDynaBean bean = insertMapEntry.getValue();
        bean.set(parentKeyCol, parentKeyVal);
      }
    }
  }

  /**
   * Gets the detail repository.
   *
   * @return the detail repository
   */
  public List<MasterRepository<?>> getDetailRepository() {
    return detailRepositories;
  }

  /**
   * Show.
   *
   * @param paramId the param id
   * @return the linked hash map
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public LinkedHashMap<String, Object> show(Object paramId) {

    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put((String) getRepository().getKeyColumn(),
        ConvertUtils.convert(paramId, getRepository().getPkType()));
    addShowFilterParameter(filterMap);
    BasicDynaBean parentBean = getRepository().findByKey(filterMap);
    if (parentBean == null) {
      throw new EntityNotFoundException(new String[] { getRepository().getBeanName(),
          getRepository().getKeyColumn(), paramId + "" });
    }

    Map<String, List<Map>> detailsBeans = findDetailsByPK(paramId);
    String tableName = getDetailRepository().get(0).getBeanName();
    detailsBeans.put(tableName, detailsBeans.get("list_" + tableName));
    detailsBeans.remove("list_" + tableName);

    LinkedHashMap<String, Object> showData = new LinkedHashMap<String, Object>();
    showData.putAll(parentBean.getMap());
    showData.putAll(detailsBeans);
    return showData;
  }

  /**
   * Find details by PK.
   *
   * @param params the params
   * @return the map
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map<String, List<Map>> findDetailsByPK(Object params) {
    Map<String, List<Map>> listbean = new HashMap<String, List<Map>>();
    if (null != detailRepositories) {
      for (MasterRepository<?> d : detailRepositories) {
        String mainPK = getRepository().getKeyColumn();

        Object mainPKValue = null;
        if (params instanceof Map && ((Map) params).get(mainPK) instanceof String[]) {
          mainPKValue = ((String[]) ((Map) params).get(mainPK))[0];
        } else {
          mainPKValue = params;
        }
        Map<String, Object> filterMap = new HashMap<String, Object>();
        filterMap.put(mainPK, ConvertUtils.convert(mainPKValue, getRepository().getPkType()));
        addDetailsTableFilter(filterMap);
        String tableName = d.getBeanName();
        List<BasicDynaBean> temp = d.listAll(null, filterMap, d.getSortColumn());
        listbean.put("list_" + tableName, ConversionUtils.listBeanToListMap(temp));
      }
    }
    return listbean;
  }

  protected void addDetailsTableFilter(Map<String, Object> filterMap) {
    //
  }

  protected void addShowFilterParameter(Map<String, Object> filterMap) {
    //
  }
}
