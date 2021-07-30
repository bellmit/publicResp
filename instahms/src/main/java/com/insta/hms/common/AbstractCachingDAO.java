package com.insta.hms.common;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.utils.EnvironmentUtil;
import com.insta.hms.redis.RedisMessagePublisher;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * This is a DAO with inbuilt cache. This class is to be used with extreme caution.
 * Please consult me before making any changes to this class.
 * All public get API provided by GenericDAO have been backed by cache. So any subsequent call to
 * get the same data would request in a cache hit and no DB query would be executed.
 * All public write API of GenericDAO result in complete cache invalidation.
 * 
 * 
 * @author utpal.lotlikar
 * 
 */
public abstract class AbstractCachingDAO extends GenericDAO {

  static {
    CacheManager.create();
  }

  /** The log. */
  private Logger log = LoggerFactory.getLogger(AbstractCachingDAO.class);

  /**
   * Instantiates a new abstract caching DAO.
   *
   * @param tablename the tablename
   */
  public AbstractCachingDAO(String tablename) {
    super(tablename);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.GenericDAO#insert(java.sql.Connection,
   * org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public boolean insert(Connection con, BasicDynaBean bean) throws SQLException, IOException {
    boolean success = super.insert(con, bean);
    if (success && cachingEnabled()) {
      if (log.isDebugEnabled()) {
        log.debug(getTable() + ": Cache invalidated as a result of insert operation.");
      }
      invalidateCacheRegion();
    }
    return success;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.GenericDAO#insertAll(java.sql.Connection, java.util.List)
   */
  @Override
  public boolean insertAll(Connection con, List<BasicDynaBean> records)
      throws SQLException, IOException {
    boolean success = super.insertAll(con, records);
    if (success && cachingEnabled()) {
      if (log.isDebugEnabled()) {
        log.debug(getTable() + ": Cache invalidated as a result of insertAll operation.");
      }
      invalidateCacheRegion();
    }
    return success;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.GenericDAO#update(java.sql.Connection, java.lang.String,
   * java.util.Map, java.util.Map)
   */
  @SuppressWarnings("unchecked")
  @Override
  public int update(Connection con, String table, Map columndata, Map keys)
      throws SQLException, IOException {
    int count = super.update(con, table, columndata, keys);
    if (count > 0 && cachingEnabled()) {
      if (log.isDebugEnabled()) {
        log.debug(getTable() + ": Cache invalidated as a result of update operation.");
      }
      invalidateCacheRegion();
    }
    return count;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.GenericDAO#delete(java.sql.Connection, java.lang.String,
   * java.lang.Object)
   */
  @Override
  public boolean delete(Connection con, String keycolumn, Object identifier) throws SQLException {
    boolean success = super.delete(con, keycolumn, identifier);
    if (success && cachingEnabled()) {
      if (log.isDebugEnabled()) {
        log.debug(getTable() + ": Cache invalidated as a result of delete operation.");
      }
      invalidateCacheRegion();
    }
    return success;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.GenericDAO#getRecord()
   */
  @Override
  public BasicDynaBean getRecord() throws SQLException {
    BasicDynaBean cachedBean = null;
    if (cachingEnabled()) {
      Element element = getCache().get("getRecord");
      if (element != null) {
        cachedBean = (BasicDynaBean) element.getObjectValue();
      }
    }

    if (cachedBean == null) {
      cachedBean = super.getRecord();
      if (cachingEnabled()) {
        getCache().put(new Element("getRecord", cachedBean));
        if (log.isDebugEnabled()) {
          log.debug(getTable() + ": Adding data to cache for getRecord.");
        }
      }
    } else {
      if (log.isDebugEnabled()) {
        log.debug(getTable() + ": Cache hit for getRecord.");
      }
    }

    return cachedBean;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.GenericDAO#findByKey(java.sql.Connection, java.lang.String,
   * java.lang.Object)
   */
  @Override
  public BasicDynaBean findByKey(Connection con, String keycolumn, Object identifier)
      throws SQLException {
    BasicDynaBean cachedBean = null;
    if (cachingEnabled()) {
      FilterKey key = new FilterKey(keycolumn, identifier);
      Element element = getCache().get(key);
      if (element != null) {
        cachedBean = (BasicDynaBean) element.getObjectValue();
      }
    }

    if (cachedBean == null) {
      cachedBean = super.findByKey(con, keycolumn, identifier);
      if (cachingEnabled()) {
        if (log.isDebugEnabled()) {
          log.debug(getTable() + ": Adding data to cache for findByKey.");
        }
        FilterKey key = new FilterKey(keycolumn, identifier);
        getCache().put(new Element(key, cachedBean));
      }
    } else {
      if (log.isDebugEnabled()) {
        log.debug(getTable() + ": Cache hit for findByKey.");
      }
    }
    return cachedBean;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.GenericDAO#findByKey(java.lang.String, java.lang.Object)
   */
  @Override
  public BasicDynaBean findByKey(String keycolumn, Object identifier) throws SQLException {
    BasicDynaBean cachedBean = null;
    if (cachingEnabled()) {
      FilterKey key = new FilterKey(keycolumn, identifier);
      Element element = getCache().get(key);
      if (element != null) {
        cachedBean = (BasicDynaBean) element.getObjectValue();
      }
    }

    if (cachedBean == null) {
      cachedBean = super.findByKey(keycolumn, identifier);
      if (cachingEnabled()) {
        if (log.isDebugEnabled()) {
          log.debug(getTable() + ": Adding data to cache for findByKey.");
        }
        FilterKey key = new FilterKey(keycolumn, identifier);
        getCache().put(new Element(key, cachedBean));
      }
    } else {
      if (log.isDebugEnabled()) {
        log.debug(getTable() + ": Cache hit for findByKey.");
      }
    }
    return cachedBean;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.GenericDAO#listAll(java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<BasicDynaBean> listAll(String sortColumn) throws SQLException {
    return listAll(Collections.EMPTY_LIST, null, null, sortColumn);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.GenericDAO#listAll(java.util.List, java.lang.String,
   * java.lang.Object, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<BasicDynaBean> listAll(List<String> columns, String filterBy, Object filterValue,
      String sortColumn) throws SQLException {
    List<BasicDynaBean> cachedBeans = null;

    if (cachingEnabled()) {
      ListAllKey key = new ListAllKey(columns, filterBy, filterValue, sortColumn);
      log.debug(getTable() + ": key requested " + key.hashCode());
      Element element = getCache().get(key);
      if (element != null) {
        cachedBeans = (List<BasicDynaBean>) element.getObjectValue();
      }
    }

    if (cachedBeans == null) {
      cachedBeans = super.listAll(columns, filterBy, filterValue, sortColumn);
      if (cachingEnabled()) {
        if (log.isDebugEnabled()) {
          log.debug(getTable() + ": Adding data to cache for listAll.");
        }
        ListAllKey key = new ListAllKey(columns, filterBy, filterValue, sortColumn);
        getCache().put(new Element(key, cachedBeans));
      }

    } else {
      if (log.isDebugEnabled()) {
        log.debug(getTable() + ": Cache hit for listAll.");
      }
    }

    return cachedBeans;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.GenericDAO#list(java.lang.String, boolean, int, int)
   */
  @Override
  public PagedList list(String sortField, boolean sortReverse, int pageSize, int pageNum)
      throws SQLException {
    PagedList cachedList = null;
    if (cachingEnabled()) {
      PageListKey key = new PageListKey(sortField, sortReverse, pageSize, pageNum);
      Element element = getCache().get(key);
      if (element != null) {
        cachedList = (PagedList) element.getObjectValue();
      }
    }

    if (cachedList == null) {
      cachedList = super.list(sortField, sortReverse, pageSize, pageNum);
      if (cachingEnabled()) {
        if (log.isDebugEnabled()) {
          log.debug(getTable() + ": Adding data to cache for list.");
        }
        PageListKey key = new PageListKey(sortField, sortReverse, pageSize, pageNum);
        getCache().put(new Element(key, cachedList));
      }
    } else {
      if (log.isDebugEnabled()) {
        log.debug(getTable() + ": Cache hit for list.");
      }

    }

    return cachedList;
  }

  /**
   * Gets the cache.
   *
   * @return the cache
   */
  protected Cache getCache() {
    return getCache(cacheRegion());
  }

  /**
   * Gets the cache.
   *
   * @param region the region
   * @return the cache
   */
  protected Cache getCache(String region) {
    if (region == null) {
      return null; // caching is disabled
    }
    Cache daoCache = CacheManager.getInstance().getCache(region);
    if (daoCache == null) {
      daoCache = newCache(region);
      CacheManager.getInstance().addCache(daoCache);
    }
    return daoCache;
  }

  /**
   * Caching enabled.
   *
   * @return true, if successful
   */
  protected boolean cachingEnabled() {
    return getCache() != null;
  }

  /**
   * Clear cache.
   */
  public void clearCache() {
    if (cachingEnabled()) {
      log.info(cacheRegion() + ": Cache got cleared.");
      getCache().removeAll();
    }
  }

  /**
   * Clears cache on the basis of a cache region.
   *
   * @param cacheRegion the cache region
   */
  public void clearCache(String cacheRegion) {
    getCache(cacheRegion).removeAll();
  }

  /**
   * Invalidates cache region. TODO - Right now, it does a double invalidation.
   */
  public void invalidateCacheRegion() {
    clearCache();
    if (EnvironmentUtil.isDistributed()) {
      ApplicationContextProvider.getApplicationContext().getBean(RedisMessagePublisher.class)
          .notifyCacheInvalidation(cacheRegion());
    }
  }

  /**
   * Child DAO would be expected to return a cache more suited to their data.
   *
   * @param region the region
   * @return the cache
   */
  protected abstract Cache newCache(String region);

  /**
   * Cache region.
   *
   * @return the string
   */
  protected String cacheRegion() {
    String hospitalId = null;
    HttpSession session = RequestContext.getSession();
    if (session != null) {
      hospitalId = (String) session.getAttribute("sesHospitalId");
    }

    if (hospitalId == null) {
      return null;
    } else {
      return hospitalId + '@' + getTable();
    }
  }

  /**
   * The Class PageListKey.
   */
  @SuppressWarnings("serial")
  class PageListKey implements Serializable {

    /** The sort field. */
    private String sortField;

    /** The sort reverse. */
    private boolean sortReverse;

    /** The page size. */
    private int pageSize;

    /** The page num. */
    private int pageNum;

    /**
     * Instantiates a new page list key.
     *
     * @param sortField   the sort field
     * @param sortReverse the sort reverse
     * @param pageSize    the page size
     * @param pageNum     the page num
     */
    public PageListKey(String sortField, boolean sortReverse, int pageSize, int pageNum) {
      this.sortField = sortField;
      this.sortReverse = sortReverse;
      this.pageSize = pageSize;
      this.pageNum = pageNum;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof PageListKey) {
        PageListKey key = (PageListKey) obj;
        if (((sortField == null && key.sortField == null)
            || (sortField != null && sortField.equals(key.sortField)))
            && (sortReverse == key.sortReverse) && (pageSize == key.pageSize)
            && (pageNum == key.pageNum)) {
          return true;
        }
      }

      return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      int code = 31;
      if (sortField != null) {
        code += sortField.hashCode();
      }
      code = sortReverse ? code++ : code--;
      code += pageSize;
      code += pageNum;

      return code;
    }

  }

  /**
   * The Class FilterKey.
   */
  @SuppressWarnings("serial")
  class FilterKey implements Serializable {

    /** The filter. */
    private String filter;

    /** The value. */
    private Object value;

    /**
     * Instantiates a new filter key.
     *
     * @param filter the filter
     * @param value  the value
     */
    public FilterKey(String filter, Object value) {
      this.filter = filter;
      this.value = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof FilterKey) {
        FilterKey key = (FilterKey) obj;
        if (filterEquals(key.filter) && valueEquals(key.value)) {
          return true;
        }
      }

      return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      int code = 31;
      if (filter != null) {
        code += filter.hashCode();
      }
      if (value != null) {
        code += value.hashCode();
      }

      return code;
    }

    /**
     * Filter equals.
     *
     * @param key the key
     * @return true, if successful
     */
    private boolean filterEquals(String key) {
      if (filter == null && key == null) {
        return true;
      } else if ((filter != null) && filter.equals(key)) {
        return true;
      }

      return false;
    }

    /**
     * Value equals.
     *
     * @param key the key
     * @return true, if successful
     */
    private boolean valueEquals(Object key) {
      if (value == null && key == null) {
        return true;
      } else if ((value != null) && value.equals(key)) {
        return true;
      }

      return false;
    }
  }

  /**
   * The Class ListAllKey.
   */
  @SuppressWarnings("serial")
  class ListAllKey implements Serializable {

    /** The columns. */
    private List<String> columns;

    /** The filter. */
    private FilterKey filter;

    /** The sort column. */
    private String sortColumn;

    /**
     * Instantiates a new list all key.
     *
     * @param columns     the columns
     * @param filterBy    the filter by
     * @param filterValue the filter value
     * @param sortColumn  the sort column
     */
    public ListAllKey(List<String> columns, String filterBy, Object filterValue,
        String sortColumn) {
      this.columns = columns;
      this.filter = new FilterKey(filterBy, filterValue);
      this.sortColumn = sortColumn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ListAllKey) {
        ListAllKey key = (ListAllKey) obj;
        if ((((columns == null) && (key.columns == null)) || columns.equals(key.columns))
            && filter.equals(key.filter) && equalSortColumnField(key)) {
          return true;
        }
      }

      return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      int code = 31;
      if (columns != null) {
        for (String s : columns) {
          code += s.hashCode();
        }
      }
      code += filter.hashCode();
      if (sortColumn != null) {
        code += sortColumn.hashCode();
      }

      return code;
    }

    /**
     * Equal sort column field.
     *
     * @param key the key
     * @return true, if successful
     */
    private boolean equalSortColumnField(ListAllKey key) {
      if (sortColumn == null && key.sortColumn == null) {
        return true;
      } else if ((sortColumn != null) && sortColumn.equals(key.sortColumn)) {
        return true;
      }

      return false;
    }
  }
}
