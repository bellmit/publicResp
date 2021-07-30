package com.insta.hms.mdm;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.SearchQueryAssembler;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class MasterService.
 */
@SuppressWarnings("rawtypes")
public abstract class MasterService implements BeanConversionService {

  /** The repository. */
  protected MasterRepository<?> repository;
  
  /** The validator. */
  protected MasterValidator validator;
  
  /** The log. */
  Logger log = LoggerFactory.getLogger(MasterService.class);

  /**
   * Instantiates a new master service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public MasterService(MasterRepository<?> repository, MasterValidator validator) {
    this.repository = repository;
    this.validator = validator;
  }

  /**
   * Search.
   *
   * @param params the params
   * @return the paged list
   */
  public PagedList search(Map params) {
    return this.search(params, ConversionUtils.getListingParameter(params));
  }

  /**
   * Search.
   *
   * @param params the params
   * @param listingParams the listing params
   * @return the paged list
   */
  public PagedList search(Map params, Map<LISTING, Object> listingParams) {
    return this.search(params, listingParams, false);
  }

  /**
   * Search.
   *
   * @param params the params
   * @param listingParams the listing params
   * @param filterByLoggedInCenter the filter by logged in center
   * @return the paged list
   */
  public PagedList search(Map params, Map<LISTING, Object> listingParams,
      boolean filterByLoggedInCenter) {

    SearchQueryAssembler qb = getSearchQueryAssembler(params, listingParams);
    Integer loggedInCenter = getLoggedInCenter();

    if (null != qb) {
      if (filterByLoggedInCenter && !loggedInCenter.equals(0)) {
        qb.addFilter(SearchQueryAssembler.INTEGER, "center_id", "=", loggedInCenter);
      }
      qb.build();
      return qb.getMappedPagedList();
    }
    return null;
  }

  /**
   * Gets the logged in center.
   *
   * @return the logged in center
   */
  private Integer getLoggedInCenter() {
    return RequestContext.getCenterId();
  }

  /**
   * Gets the search query assembler.
   *
   * @param params the params
   * @param listingParams the listing params
   * @return the search query assembler
   */
  protected SearchQueryAssembler getSearchQueryAssembler(Map params,
      Map<LISTING, Object> listingParams) {

    SearchQuery query = repository.getSearchQuery();
    if (null == query) {
      return null;
    }

    SearchQueryAssembler qb = new SearchQueryAssembler(query.getFieldList(), query.getCountQuery(),
        query.getSelectTables(), listingParams);
    if (null != params) {
      qb.addFilterFromParamMap(params);
    }

    String secondarySortCol = query.getSecondarySortColumn();
    if (null != secondarySortCol) {
      qb.addSecondarySort(secondarySortCol);
    }
    return qb;
  }

  /**
   * Find by pk.
   *
   * @param params the params
   * @return the basic dyna bean
   */
  public BasicDynaBean findByPk(Map params) {
    return findByPk(params, false);
  }

  /**
   * Find by pk.
   *
   * @param params the params
   * @param includeDetails the include details
   * @return the basic dyna bean
   */
  public BasicDynaBean findByPk(Map params, boolean includeDetails) {
    return repository.findByPk(params, includeDetails);
  }

  /**
   * Find by unique name.
   *
   * @param name the name
   * @param column the column
   * @return the basic dyna bean
   */
  public BasicDynaBean findByUniqueName(String name, String column) {
    BasicDynaBean bean = null;
    if (null != column && null != name) {
      bean = repository.findByKey(column, name);
    }
    return bean;
  }

  /**
   * Insert.
   *
   * @param bean the bean
   * @return the integer
   */
  @Transactional(rollbackFor = Exception.class)
  public Integer insert(BasicDynaBean bean) {
    validator.validateInsert(bean);
    String keyColumn = repository.getKeyColumn();
    Object nextId = null;
    if (null != keyColumn && repository.supportsAutoId()) {
      nextId = repository.getNextId();
      bean.set(keyColumn, nextId);
    }
    return repository.insert(bean);
  }

  /**
   * Update.
   *
   * @param bean the bean
   * @return the integer
   */
  @Transactional(rollbackFor = Exception.class)
  public Integer update(BasicDynaBean bean) {
    Map<String, Object> keys = new HashMap<String, Object>();
    validator.validateUpdate(bean);
    String keyColumn = repository.getKeyColumn();
    if (null != keyColumn) {
      keys.put(keyColumn, bean.get(keyColumn));
    }
    return repository.update(bean, keys);
  }

  /**
   * Soft deletes if status field present. Override to hard delete.
   *
   * @param bean
   *          the bean
   * @return the integer
   */
  @Transactional
  public Integer delete(BasicDynaBean bean) {
    return delete(bean, false);
  }

  /**
   * Delete.
   *
   * @param bean
   *          the bean
   * @param hardDelete
   *          HardDeletes if hardDelete = true
   * @return the integer
   */
  @Transactional
  public Integer delete(BasicDynaBean bean, Boolean hardDelete) {
    String statusField = repository.getStatusField();
    if (hardDelete) {
      String keyColumn = repository.getKeyColumn();
      if (null != keyColumn && !keyColumn.isEmpty()) {
        return repository.delete(keyColumn, bean.get(keyColumn));
      }
    }

    if (null != statusField && !statusField.isEmpty()) {
      bean.set(statusField, "I");
      return update(bean);
    }

    log.info("The bean with id:" + bean.get(repository.getKeyColumn())
        + " hasn't been inactived as it has no status field");
    return 0;// if hardDelete is false and there is no status field; do
    // nothing.
  }

  /**
   * Batch delete.The child class will have to create the endpoint the controller for batch delete
   *
   * @param ids          the ids array of primary keys
   * @param hardDelete          hardDeletes if hardDelete = true
   * @return true, if successful
   */
  @Transactional
  public boolean batchDelete(String[] ids, Boolean hardDelete) {
    boolean success = false;
    String statusField = repository.getStatusField();
    if (hardDelete) {
      List<Object> values = new ArrayList<Object>(Arrays.asList(ids));
      int[] updates = repository.batchDelete(repository.getKeyColumn(), values,
          repository.getSqlType());
      success = true;
      for (int update : updates) {
        if (update < 1) {
          return false;
        }
      }
      return success;
    }
    if ((null != statusField && !statusField.isEmpty())) {
      if (null == repository.batchSoftDelete(ids)) {
        ;
      }
      return false;
    }
    return false;// if no statusField and hardDelete is false
  }

  /**
   * Autocomplete on the first uniqueNameColumn defined in the repository.
   *
   * @param match          the match
   * @param parameters          the parameters
   * @return the list
   */
  public List<BasicDynaBean> autocomplete(String match, Map<String, String[]> parameters) {
    return autocomplete(repository.getUniqueNameColumn()[0], match, true, parameters);
  }

  /**
   * Autocomplete.
   *
   * @param matchField the match field
   * @param likeValue the like value
   * @param activeOnly the active only
   * @param parameters the parameters
   * @return the list
   */
  public List<BasicDynaBean> autocomplete(String matchField, String likeValue, boolean activeOnly,
      Map<String, String[]> parameters) {

    // check for contains
    boolean contains = false;
    String paramContains = null;
    if (null != parameters.get("contains")) {
      paramContains = parameters.get("contains")[0].trim();
    }
    if (null != paramContains && paramContains.equalsIgnoreCase("true")) {
      contains = true;
    }

    final String lookupQuery = repository.getLookupQuery();

    SearchQueryAssembler qb = null;
    // qb = new SearchQueryAssembler(lookupQuery, null, null,
    // listingParams);
    qb = getLookupQueryAssembler(lookupQuery, parameters);
    addFilterForLookUp(qb, likeValue, matchField, contains, parameters);
    if (null != repository.getStatusField() && activeOnly) {
      qb.addFilter(QueryAssembler.STRING, repository.getStatusField(), "=",
          repository.getActiveStatus());
    }
    qb.build();
    PagedList pagedList = qb.getDynaPagedList();
    List<BasicDynaBean> resultList = pagedList.getDtoList();
    return resultList;
  }

  /**
   * Adds the filter for look up.
   *
   * @param qb the qb
   * @param likeValue the like value
   * @param matchField the match field
   * @param contains the contains
   * @param parameters the parameters
   */
  public void addFilterForLookUp(SearchQueryAssembler qb, String likeValue, String matchField,
      boolean contains, Map<String, String[]> parameters) {
    if (!likeValue.trim().isEmpty()) {
      String filterText = likeValue.trim() + "%";
      if (contains) {
        filterText = "%" + likeValue.trim() + "%";
      }
      qb.addFilter(QueryAssembler.STRING, matchField, "ILIKE", filterText);
    }
  }

  /**
   * Gets the lookup query assembler.
   *
   * @param lookupQuery the lookup query
   * @param parameters the parameters
   * @return the lookup query assembler
   */
  public SearchQueryAssembler getLookupQueryAssembler(String lookupQuery,
      Map<String, String[]> parameters) {
    SearchQueryAssembler qb = null;
    qb = new SearchQueryAssembler(lookupQuery, null, null,
        ConversionUtils.getListingParameter(parameters));
    return qb;
  }

  /**
   * Lookup.
   *
   * @param activeOnly the active only
   * @return the list
   */
  // TODO - Document.Please
  public List<BasicDynaBean> lookup(boolean activeOnly) {
    return repository.lookup(activeOnly);
  }

  /**
   * Lookup.
   *
   * @param activeOnly the active only
   * @param filterMap the filter map
   * @return the list
   */
  public List<BasicDynaBean> lookup(boolean activeOnly, Map<String, Object> filterMap) {
    return repository.lookup(activeOnly, filterMap);
  }

  /**
   * Filter.
   *
   * @param parameters request parameters
   * @param match string to match
   * @return the paged list
   */
  public PagedList filter(Map<String, String[]> parameters, String match) {
    // check for contains
    boolean contains = false;
    String paramContains = null;
    if (null != parameters.get("contains")) {
      paramContains = parameters.get("contains")[0].trim();
    }
    if (null != paramContains && paramContains.equalsIgnoreCase("true")) {
      contains = true;
    }

    SearchQuery query = repository.getSearchQuery();
    if (null == query) {
      return null;
    }

    SearchQueryAssembler qb = new SearchQueryAssembler(query.getFieldList(), query.getCountQuery(),
        query.getSelectTables(), ConversionUtils.getListingParameter(parameters));

    addFilterForLookUp(qb, match, repository.getUniqueNameColumn()[0], contains, parameters);
    qb.build();
    return qb.getMappedPagedList();

  }

  @Override
  public Map<String, List<BasicDynaBean>> toBeanList(Map<String, String[]> requestParams,
      BasicDynaBean type) {
    return null;
  }
  
  @Override
  public BasicDynaBean toBean(Map<String, String[]> requestParams) {
    return toBean(requestParams, null);
  }

  @Override
  public BasicDynaBean toBean(Map<String, String[]> requestParams,
      Map<String, MultipartFile> fileMap) {
    List<String> errorFields = new ArrayList<String>();
    Map<String, Object> multipartRequestParameters = new HashMap<String, Object>(requestParams);
    if (null != fileMap && !(fileMap.isEmpty())) {
      multipartRequestParameters.putAll(fileMap);
    }
    BasicDynaBean bean = repository.getBean();
    ConversionUtils.copyToDynaBean(multipartRequestParameters, bean, errorFields);
    return bean;
  }

  /**
   * To bean.
   *
   * @param requestBody the request body
   * @return the basic dyna bean
   */
  public BasicDynaBean toBean(ModelMap requestBody) {
    BasicDynaBean bean = repository.getBean();
    List<String> errorFields = new ArrayList<String>();
    ConversionUtils.copyJsonToDynaBean(requestBody, bean, errorFields, true);
    return bean;
  }

  /**
   * Gets the repository.
   *
   * @return the repository
   */
  protected MasterRepository getRepository() {
    return repository;
  }
}
