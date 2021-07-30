package com.insta.hms.common;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class DynaBeanBuilder.
 */
public class DynaBeanBuilder {

  /** The properties. */
  private List<DynaProperty> properties;

  /** The field val map. */
  private Map<String, Object> fieldValMap;

  /**
   * Instantiates a new dyna bean builder.
   */
  public DynaBeanBuilder() {
    properties = new ArrayList<DynaProperty>();
    fieldValMap = new HashMap<String, Object>();
  }

  /**
   * See also addPropertyValue().
   *
   * @param field the field
   * @return the dyna bean builder
   */

  public DynaBeanBuilder add(String field) {
    return add(field, String.class);
  }

  /**
   * See also addPropertyValue().
   *
   * @param field the field
   * @param type  the type
   * @return the dyna bean builder
   */
  public DynaBeanBuilder add(String field, Class type) {
    properties.add(new DynaProperty(field, type));
    return this;
  }

  /**
   * Builds the.
   *
   * @return the basic dyna bean
   */
  public BasicDynaBean build() {
    return build("employee");
  }

  /**
   * Builds the.
   *
   * @param name the name
   * @return the basic dyna bean
   */
  public BasicDynaBean build(String name) {
    DynaProperty[] props = properties.toArray(new DynaProperty[properties.size()]);
    BasicDynaClass dynaClass = new BasicDynaClass(name, null, props);
    InstaBean instaBean = new InstaBean(dynaClass);
    if (MapUtils.isNotEmpty(fieldValMap)) {
      for (Map.Entry<String, Object> fieldValEntry : fieldValMap.entrySet()) {
        instaBean.set(fieldValEntry.getKey(), fieldValEntry.getValue());
      }
    }
    return instaBean;
  }

  /**
   * Uses <{? extends Object}>(propertyValue) to find DynaPropertyClass and also populates
   * BasicDynaBean with values.
   *
   * @param propertyName  the property name
   * @param propertyValue the property value
   * @return the dyna bean builder
   */
  public DynaBeanBuilder addPropertyValue(String propertyName, Object propertyValue) {
    if (StringUtils.isBlank(propertyName) || null == propertyValue) {
      return this; // Do nothing if propertyName/propertyValue is invalid
    } else {
      properties.add(new DynaProperty(propertyName, propertyValue.getClass()));
      fieldValMap.put(propertyName, propertyValue);
    }
    return this;
  }

  /**
   * Gets the dyna bean builder.
   *
   * @param rs the rs
   * @return the dyna bean builder
   * @throws SQLException the SQL exception
   * @throws ClassNotFoundException the class not found exception
   */
  public static DynaBeanBuilder getDynaBeanBuilder(ResultSet rs) throws SQLException,
      ClassNotFoundException {
    ResultSetMetaData rsmd = rs.getMetaData();
    DynaBeanBuilder bean = new DynaBeanBuilder();
    for (int i = 0; i < rsmd.getColumnCount(); i++) {
      bean.add(rsmd.getColumnName(i + 1), Class.forName(rsmd.getColumnClassName(i + 1)));
    }
    return bean;
  }

  /**
   * Load bean.
   *
   * @param rs the rs
   * @param record the record
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean loadBean(ResultSet rs, BasicDynaBean record) throws SQLException {
    DynaProperty[] props = record.getDynaClass().getDynaProperties();
    for (int i = 0; i < props.length; i++) {
      Object colValue = rs.getObject(props[i].getName());
      if (colValue != null) {
        record.set(props[i].getName(), ConvertUtils.convert(colValue, props[i].getType()));
      }
    }
    return record;
  }

  /**
   * The Class InstaBean.
   */
  @SuppressWarnings("serial")
  static class InstaBean extends BasicDynaBean {

    /** The added. */
    Set<String> added = new HashSet<String>();

    /**
     * Instantiates a new insta bean.
     *
     * @param clazz the clazz
     */
    public InstaBean(DynaClass clazz) {
      super(clazz);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.beanutils.BasicDynaBean#set(java.lang.String, java.lang.Object)
     */
    public void set(String name, Object value) {
      super.set(name, value);
      added.add(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.beanutils.BasicDynaBean#set(java.lang.String, int, java.lang.Object)
     */
    public void set(String name, int index, Object value) {
      super.set(name, index, value);
      added.add(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.beanutils.BasicDynaBean#set(java.lang.String, java.lang.String,
     * java.lang.Object)
     */
    public void set(String name, String key, Object value) {
      super.set(name, key, value);
      added.add(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.beanutils.BasicDynaBean#getMap()
     */
    @SuppressWarnings("unchecked")
    public Map getMap() {
      Map values = new HashMap();
      Map original = super.getMap();
      Set<String> keys = original.keySet();

      for (String key : keys) {
        if (added.contains(key)) {
          values.put(key, original.get(key));
        }
      }

      return values;
    }
  }
}
