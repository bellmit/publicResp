package com.insta.hms.common;

import com.bob.hms.common.DateUtil;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class ConversionUtils. Data conversion utilities, like BeanUtils or ConvertUtils
 */
public class ConversionUtils {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(ConversionUtils.class);

  /**
   * The Enum LISTING.
   */
  public static enum LISTING {

    /** The sortcol. */
    SORTCOL,
    /** The sortasc. */
    SORTASC,
    /** The pagesize. */
    PAGESIZE,
    /** The pagenum. */
    PAGENUM
  }

  /**
   * Flattens a request.getParameterMap() map. Flatten => convert all arrays of a single element
   * into object of that element.
   *
   * @param in the in
   * @return the map
   */
  public static Map flatten(Map in) {
    HashMap out = new HashMap();
    for (Map.Entry e : (Collection<Map.Entry>) in.entrySet()) {
      Object[] values = (Object[]) e.getValue();
      if (values.length == 1) {
        out.put(e.getKey(), values[0]);
      } else {
        out.put(e.getKey(), values);
      }
    }
    return out;
  }

  /**
   * Copy string fields.
   *
   * @param from        the from
   * @param to          the to
   * @param fieldNames  the field names
   * @param errorFields the error fields
   */
  public static void copyStringFields(Map from, Map to, String[] fieldNames, List errorFields) {
    for (String f : fieldNames) {
      String[] values = (String[]) from.get(f);
      if (values != null) {
        String value = values[0];
        if (value != null) {
          to.put(f, value);
        }
      }
    }
  }

  /**
   * Copy numeric fields.
   *
   * @param from        the from
   * @param to          the to
   * @param fieldNames  the field names
   * @param errorFields the error fields
   */
  public static void copyNumericFields(Map from, Map to, String[] fieldNames, List errorFields) {
    for (String f : fieldNames) {
      String[] values = (String[]) from.get(f);
      if (values != null) {
        String value = values[0];
        if (value != null) {
          try {
            BigDecimal bd = (BigDecimal) ConvertUtils.convert(value, BigDecimal.class);
            to.put(f, bd);
          } catch (ConversionException exception) {
            if (errorFields != null) {
              errorFields.add(f);
            }
          }
        }
      }
    }
  }

  /**
   * Copy integer fields.
   *
   * @param from        the from
   * @param to          the to
   * @param fieldNames  the field names
   * @param errorFields the error fields
   */
  public static void copyIntegerFields(Map from, Map to, String[] fieldNames, List errorFields) {
    for (String field : fieldNames) {
      String[] values = (String[]) from.get(field);
      if (values != null) {
        String value = values[0];
        if (value != null) {
          try {
            Integer integer = (Integer) ConvertUtils.convert(value, Integer.class);
            to.put(field, integer);
          } catch (ConversionException exception) {
            if (errorFields != null) {
              errorFields.add(field);
            }
          }
        }
      }
    }
  }

  /**
   * Convert a string-based set of fields to Boolean objects. defaultValue is assigned in case the
   * value is not found in the 'from' map.
   *
   * @param from         the from
   * @param to           the to
   * @param fieldNames   the field names
   * @param errorFields  the error fields
   * @param defaultValue the default value
   */
  public static void copyBooleanFields(Map from, Map to, String[] fieldNames, List errorFields,
      boolean defaultValue) {

    for (String f : fieldNames) {
      String[] values = (String[]) from.get(f);
      if (values != null && values[0] != null) {
        try {
          Boolean bool = (Boolean) ConvertUtils.convert(values[0], Boolean.class);
          to.put(f, bool);
        } catch (ConversionException exception) {
          to.put(f, new Boolean(defaultValue));
        }
      } else {
        to.put(f, new Boolean(defaultValue));
      }
    }
  }

  /**
   * Set one string value string to a bean: useful if names are different in bean and request
   * parameter map.
   *
   * @param bean            the bean
   * @param name            the name
   * @param value           the value
   * @param nullEmptyString the null empty string
   */
  public static void setDynaProperty(DynaBean bean, String name, String value,
      boolean nullEmptyString) {
    DynaProperty property = bean.getDynaClass().getDynaProperty(name);
    if (value == null) {
      return;
    }
    try {
      if (value.equals("")) {
        if (property.getType() == java.lang.String.class) {
          bean.set(name, nullEmptyString ? null : "");
        } else {
          bean.set(name, null);
        }
      }
    } catch (ConversionException exception) {
      log.error("Conversion error. " + name + "=" + value + " could not be converted to "
          + property.getType(), exception);
    }

    bean.set(name, ConvertUtils.convert(value, property.getType()));
  }

  /**
   * Sets the dyna property.
   *
   * @param bean  the bean
   * @param name  the name
   * @param value the value
   */
  public static void setDynaProperty(DynaBean bean, String name, String value) {
    ConversionUtils.setDynaProperty(bean, name, value, true);
  }

  /**
   * Copy to dyna bean.
   *
   * @param from               the from
   * @param bean               the bean
   * @param errorFields        the error fields
   * @param nullifyEmptyString the nullify empty string
   * @param fieldPrefix        the field prefix
   */
  public static void copyToDynaBean(Map from, DynaBean bean, List errorFields,
      boolean nullifyEmptyString, String fieldPrefix) {
    copyToDynaBean(from, bean, errorFields, nullifyEmptyString, fieldPrefix, null);
  }

  /**
   * Copy to dyna bean. original method: does not convert empty strings to null, retains them as
   * empty strings.
   *
   * @param from        the from
   * @param bean        the bean
   * @param errorFields the error fields
   */
  public static void copyToDynaBean(Map from, DynaBean bean, List errorFields) {
    copyToDynaBean(from, bean, errorFields, false, null);
  }

  /**
   * Copy to dyna bean.
   *
   * @param from the from
   * @param bean the bean
   */
  public static void copyToDynaBean(Map from, DynaBean bean) {
    copyToDynaBean(from, bean, null, true, null);
  }

  /**
   * Copy to dyna bean.
   *
   * @param from               the from
   * @param bean               the bean
   * @param errorFields        the error fields
   * @param nullifyEmptyString the nullify empty string
   */
  public static void copyToDynaBean(Map from, DynaBean bean, List errorFields,
      boolean nullifyEmptyString) {
    copyToDynaBean(from, bean, errorFields, nullifyEmptyString, null);
  }

  /**
   * Copy to dyna bean.
   *
   * @param from   the from
   * @param bean   the bean
   * @param prefix the prefix
   */
  public static void copyToDynaBean(Map from, DynaBean bean, String prefix) {
    copyToDynaBean(from, bean, null, true, prefix);
  }

  /**
   * Copy the request parameter map to a bean, by looking at the dyna properties of the bean and
   * setting those values, if found, from the request. If nullifyEmptyStrings is true, then, empty
   * strings are set as null (which is usually what we want).
   *
   * @param from               the from
   * @param bean               the bean
   * @param errorFields        the error fields
   * @param nullifyEmptyString the nullify empty string
   * @param fieldPrefix        the field prefix
   * @param aliasMap           the alias map
   */
  public static void copyToDynaBean(Map from, DynaBean bean, List errorFields,
      boolean nullifyEmptyString, String fieldPrefix, Map<String, String> aliasMap) {

    DynaProperty[] dynaProperties = bean.getDynaClass().getDynaProperties();
    String imageContentType = null;
    for (DynaProperty property : dynaProperties) {
      String propertyName = property.getName();
      String alias = null;
      if (null != aliasMap) {
        alias = aliasMap.get(propertyName);
      }
      String fieldname = (null != fieldPrefix ? fieldPrefix : "")
          + (null != alias ? alias : propertyName);
      String propName = (null != fieldPrefix ? fieldPrefix : "") + propertyName;
      Object fieldValue = from.containsKey(fieldname) ? from.get(fieldname) : from.get(propName);
      Object[] fieldValueArray = null != fieldValue && !(fieldValue.getClass().isArray())
          ? new Object[] { fieldValue }
          : (Object[]) fieldValue;

      try {
        if (fieldValueArray != null && fieldValueArray[0] != null) {
          if (fieldValueArray[0].equals("")) {
            if (property.getType() == java.lang.String.class) {
              bean.set(propertyName, nullifyEmptyString ? null : "");
            } else {
              bean.set(propertyName, null);
            }
            continue;
          }

          if (property.getType().equals(InputStream.class)
              && fieldValueArray[0] instanceof MultipartFile) {
            MultipartFile file = (MultipartFile) fieldValueArray[0];
            imageContentType = file.getContentType();
            if (!file.isEmpty()) {
              bean.set(propertyName, file.getInputStream());
            }

          } else {
            bean.set(propertyName, ConvertUtils.convert(fieldValueArray[0], property.getType()));
          }

        } else if (property.getType() == java.sql.Timestamp.class) {
          /*
           * Special handling for date fields, which come in two parts: <fieldname>_dt
           * <fieldname>_tm
           */

          String[] datePart = (String[]) from.get(fieldname + "_dt");
          String[] timePart = (String[]) from.get(fieldname + "_tm");
          if (datePart != null && datePart[0] != null && timePart != null && timePart[0] != null) {
            bean.set(propertyName, DateUtil.parseTimestamp(datePart[0], timePart[0]));
          }
        }
      } catch (ConversionException exception) {
        log.error("Conversion error. " + fieldname + "=" + fieldValueArray[0]
            + " could not be converted to " + property.getType(), exception);
        if (errorFields != null) {
          errorFields.add(fieldname);
        }
      } catch (ParseException exception) {
        log.error("Conversion error. " + fieldname + "=" + fieldValueArray[0]
            + " could not be converted to " + property.getType(), exception);
        if (errorFields != null) {
          errorFields.add(fieldname);
        }
      } catch (IOException exception) {
        log.error("Conversion error. " + fieldname + "=" + fieldValueArray[0] + " Irretrievable "
            + property.getType(), exception);
        if (errorFields != null) {
          errorFields.add(fieldname);
        }
      }
    }

    // Hack for setting content-type for Spring handled images
    if (null != imageContentType && null != bean.getDynaClass().getDynaProperty("content_type")) {
      bean.set("content_type", imageContentType);
    }
  }

  /**
   * Copy the request Model map to a bean, by looking at the dyna properties of the bean and setting
   * those values, if found, from the request. If nullifyEmptyStrings is true, then, empty strings
   * are set as null (which is usually what we want).
   *
   * @param from               the from
   * @param bean               the bean
   * @param errorFields        the error fields
   * @param nullifyEmptyString the nullify empty string
   * @param fieldPrefix        the field prefix
   */
  public static void copyJsonToDynaBean(Map from, DynaBean bean, List errorFields,
      boolean nullifyEmptyString, String fieldPrefix) {

    DynaProperty[] dynaProperties = bean.getDynaClass().getDynaProperties();
    for (DynaProperty property : dynaProperties) {
      String propertyName = property.getName();
      String fieldname = fieldPrefix == null ? propertyName : fieldPrefix + propertyName;
      Object object = (Object) from.get(fieldname);

      try {
        if (from.containsKey(fieldname)) {
          if (object == null || "".equals(object)) {
            if (property.getType() == java.lang.String.class) {
              bean.set(propertyName, nullifyEmptyString ? null : "");
            } else {
              bean.set(propertyName, null);
            }
            continue;
          }

          bean.set(propertyName, ConvertUtils.convert(object, property.getType()));
        } else if (property.getType() == java.sql.Timestamp.class) {
          /*
           * Special handling for date fields, which come in two parts: <fieldname>_dt
           * <fieldname>_tm
           */

          String[] datePart = (String[]) from.get(fieldname + "_dt");
          String[] timePart = (String[]) from.get(fieldname + "_tm");
          if (datePart != null && datePart[0] != null && timePart != null && timePart[0] != null) {
            bean.set(propertyName, DateUtil.parseTimestamp(datePart[0], timePart[0]));
          }
        }
      } catch (ConversionException exception) {
        log.error("Conversion error. " + fieldname + "=" + object + " could not be converted to "
            + property.getType(), exception);
        if (errorFields != null) {
          errorFields.add(fieldname);
        }
      } catch (ParseException except) {
        log.error("Conversion error. " + fieldname + "=" + object + " could not be converted to "
            + property.getType(), except);
        if (errorFields != null) {
          errorFields.add(fieldname);
        }
      }
    }
  }

  /**
   * Copy json to dyna bean.
   *
   * @param from               the from
   * @param bean               the bean
   * @param errorFields        the error fields
   * @param nullifyEmptyString the nullify empty string
   */
  public static void copyJsonToDynaBean(Map from, DynaBean bean, List errorFields,
      boolean nullifyEmptyString) {
    copyJsonToDynaBean(from, bean, errorFields, nullifyEmptyString, null);
  }

  /*
   * Copy the indexed request parameter map to a bean, by looking at the dyna properties of the bean
   * and setting those values, if found, from the request. Used when converting an request parameter
   * arrays (many rows of objects) into a list of beans. For example, for (int i=0; i<numRows; i++)
   * { Dynabean b = dao.getBean(); copyIndexToDynaBean(params, i, b, errors); beanList.add(b); }
   */

  /**
   * Copy index to dyna bean.
   *
   * @param from        the from
   * @param index       the index
   * @param bean        the bean
   * @param errorFields the error fields
   * @param ignoreEmpty the ignore empty
   */
  public static void copyIndexToDynaBean(Map from, int index, DynaBean bean, List errorFields,
      boolean ignoreEmpty) {
    copyIndexToDynaBean(from, index, bean, errorFields, ignoreEmpty, null);
  }

  /**
   * Copy index to dyna bean.
   *
   * @param from        the from
   * @param index       the index
   * @param bean        the bean
   * @param errorFields the error fields
   * @param ignoreEmpty the ignore empty
   * @param aliasMap    the alias map
   */
  public static void copyIndexToDynaBean(Map from, int index, DynaBean bean, List errorFields,
      boolean ignoreEmpty, Map<String, String> aliasMap) {

    DynaProperty[] dynaProperties = bean.getDynaClass().getDynaProperties();

    for (DynaProperty property : dynaProperties) {
      String fieldname = property.getName();
      String alias = property.getName(); // set the alias to bean properties to start with
      // If an alias is specified, get the alias
      if (null != aliasMap && aliasMap.containsKey(fieldname)) {
        alias = aliasMap.get(fieldname);
      }
      // Fetch the alias, if that turns out to be null, fallback to bean property name
      Object[] object = (from.containsKey(alias)) ? (Object[]) from.get(alias)
          : (Object[]) from.get(fieldname);

      if (object != null && object.length > index && object[index] != null) {
        if (object[index].toString().equals("") && property.getType() != java.lang.String.class) {
          if (!ignoreEmpty) {
            bean.set(fieldname, null);
          }
          continue;
        }
        try {
          bean.set(fieldname, ConvertUtils.convert(object[index], property.getType()));
        } catch (ConversionException exception) {
          log.error("Conversion error. " + fieldname + "=" + object[index] + " at " + index
              + " could not be converted to " + property.getType(), exception);
          if (errorFields != null) {
            errorFields.add(fieldname);
          }
        }
      }
    }
  }

  /**
   * Original method sets null for empty string which will over wride column default value.
   *
   * @param from        the from
   * @param index       the index
   * @param bean        the bean
   * @param errorFields the error fields
   */
  public static void copyIndexToDynaBean(Map from, int index, DynaBean bean, List errorFields) {
    copyIndexToDynaBean(from, index, bean, errorFields, false, null);
  }

  /**
   * Copy index to dyna bean.
   *
   * @param from  the from
   * @param index the index
   * @param bean  the bean
   */
  public static void copyIndexToDynaBean(Map from, int index, DynaBean bean) {
    copyIndexToDynaBean(from, index, bean, null, true, null);
  }

  /**
   * Copy index to dyna bean prefixed. Same as above, but takes a prefix for the UI param which is
   * removed when looking for the bean param. This can be used to create "namespaces" in the UI, to
   * distinguish different kinds of objects, which could potentially have same field names. For
   * example, if we need a set of tests as well as a set of services, but presc_date is same in
   * both, we can use test.* and service.* as the field names, and still automatically convert the
   * params to a bean.
   *
   * @param from        the from
   * @param index       the index
   * @param bean        the bean
   * @param errorFields the error fields
   * @param prefix      the prefix
   */
  public static void copyIndexToDynaBeanPrefixed(Map from, int index, DynaBean bean,
      List errorFields, String prefix) {

    DynaProperty[] dynaProperties = bean.getDynaClass().getDynaProperties();

    for (DynaProperty property : dynaProperties) {
      String fieldname = property.getName();
      Object[] object = (Object[]) from.get(prefix + fieldname);

      if (object != null && object.length > index && object[index] != null) {
        if (object[index].toString().equals("") && property.getType() != java.lang.String.class) {
          bean.set(fieldname, null);
          continue;
        }
        try {
          bean.set(fieldname, ConvertUtils.convert(object[index], property.getType()));
        } catch (ConversionException exception) {
          log.error("Conversion error (Prefix: " + prefix + "). " + fieldname + "=" + object[index]
              + " at " + index + " could not be converted to " + property.getType(), exception);
          if (errorFields != null) {
            errorFields.add(fieldname);
          }
        }
      }
    }
  }

  /**
   * Copy json to dyna bean prefixed. Same as above, but takes a prefix for the UI param which is
   * removed when looking for the bean param. This can be used to create "namespaces" in the UI, to
   * distinguish different kinds of objects, which could potentially have same field names. For
   * example, if we need a set of tests as well as a set of services, but presc_date is same in
   * both, we can use test.* and service.* as the field names, and still automatically convert the
   * params to a bean.
   *
   * @param from        the from
   * @param bean        the bean
   * @param errorFields the error fields
   * @param prefix      the prefix
   */
  public static void copyJsonToDynaBeanPrefixed(Map from, DynaBean bean, List errorFields,
      String prefix) {

    DynaProperty[] dynaProperties = bean.getDynaClass().getDynaProperties();

    for (DynaProperty property : dynaProperties) {
      String fieldname = property.getName();
      Object fieldValue = from.get(prefix + fieldname);

      if (fieldValue != null) {
        if (fieldValue.toString().equals("") && property.getType() != java.lang.String.class) {
          bean.set(fieldname, null);
          continue;
        }
        try {
          bean.set(fieldname, ConvertUtils.convert(fieldValue, property.getType()));
        } catch (ConversionException exception) {
          log.error("Conversion error (Prefix: " + prefix + "). " + fieldname + "=" + fieldValue
              + " could not be converted to " + property.getType(), exception);
          if (errorFields != null) {
            errorFields.add(fieldname);
          }
        }
      }
    }
  }

  /**
   * Convert a list of beans to a list of Maps.
   *
   * @param dynabeans the dynabeans
   * @return the list
   */
  public static List copyListDynaBeansToMap(List dynabeans) {
    List maplist = new ArrayList<Map<String, Object>>();
    if (dynabeans == null) {
      return maplist;
    }
    for (Object object : dynabeans) {
      BasicDynaBean bean = (BasicDynaBean) object;
      maplist.add(bean.getMap());
    }
    return maplist;
  }

  /**
   * Copy list dyna beans to linked map.
   *
   * @param dynabeans the dynabeans
   * @return the list
   */
  public static List copyListDynaBeansToLinkedMap(List dynabeans) {
    List maplist = new ArrayList<Map<String, Object>>();
    if (dynabeans == null) {
      return maplist;
    }
    for (Object object : dynabeans) {
      BasicDynaBean bean = (BasicDynaBean) object;
      LinkedHashMap<String, Object> beanMap = new LinkedHashMap<String, Object>(bean.getMap());
      maplist.add(beanMap);
    }
    return maplist;
  }

  /**
   * Gets the listing parameter.
   *
   * @param request the request
   * @return the listing parameter
   */
  public static Map<LISTING, Object> getListingParameter(Map request) {
    Map<LISTING, Object> listing = new HashMap<LISTING, Object>();
    String[] params = (String[]) request.get("sortOrder");
    if (params == null) {
      params = (String[]) request.get("sort_order");
    }
    if (params != null) {
      listing.put(LISTING.SORTCOL, params[0]);
    }

    params = (String[]) request.get("sortReverse");
    if (params == null) {
      params = (String[]) request.get("sort_reverse");
    }
    Boolean sort = false;
    if (params != null) {
      try {
        sort = new Boolean(params[0]);
      } catch (NumberFormatException exception) {
        // ignored
      }
    }
    listing.put(LISTING.SORTASC, sort);

    params = (String[]) request.get("pageSize");
    if (params == null) {
      params = (String[]) request.get("page_size");
    }
    int size = 20;
    if (params != null) {
      try {
        size = new Integer(params[0]);
      } catch (NumberFormatException exception) {
        // ignored
      }
    }
    listing.put(LISTING.PAGESIZE, size);

    params = (String[]) request.get("pageNum");
    if (params == null) {
      params = (String[]) request.get("page_num");
    }
    int num = 1;
    if (params != null) {
      try {
        num = Integer.parseInt(params[0]);
      } catch (NumberFormatException except) {
        // ignored
      }
    }
    listing.put(LISTING.PAGENUM, num);

    return listing;
  }

  /**
   * Get a parameter values set as a list, ignoring empty strings.
   *
   * @param request the request
   * @param param   the param
   * @return the param as list
   */
  public static List<String> getParamAsList(Map request, String param) {
    String[] values = (String[]) request.get(param);
    if (values == null) {
      return null;
    }

    List list = new ArrayList();
    for (int i = 0; i < values.length; i++) {
      if (!values[i].equals("")) {
        list.add(values[i]);
      }
    }
    if (list.size() > 0) {
      return list;
    }
    return null;
  }

  /**
   * Get a parameter values set as a list of booleans, ignoring empty strings.
   *
   * @param request the request
   * @param param   the param
   * @return the param as list boolean
   */
  public static List<Boolean> getParamAsListBoolean(Map request, String param) {
    String[] values = (String[]) request.get(param);
    if (values == null) {
      return null;
    }

    List list = new ArrayList();
    for (int i = 0; i < values.length; i++) {
      if (!values[i].equals("")) {
        list.add(new Boolean(values[i]));
      }
    }
    if (list.size() > 0) {
      return list;
    }
    return null;
  }

  /**
   * Get a parameter values set as a list of Integers, ignoring empty strings.
   *
   * @param request the request
   * @param param   the param
   * @return the param as list integer
   */
  public static List<Boolean> getParamAsListInteger(Map request, String param) {
    String[] values = (String[]) request.get(param);
    if (values == null) {
      return null;
    }

    List list = new ArrayList();
    for (int i = 0; i < values.length; i++) {
      if (!values[i].equals("")) {
        list.add(new Integer(values[i]));
      }
    }
    if (list.size() > 0) {
      return list;
    }
    return null;
  }

  /**
   * Convert a list of DynaBeans to a list of Maps.
   *
   * @param dynabeans the dynabeans
   * @return the list
   */
  public static List listBeanToListMap(List dynabeans) {
    return copyListDynaBeansToMap(dynabeans);
  }

  /**
   * Converts a list of DynaBeans (rows) into a HashMap based on the value of one column, which is
   * the identifier of the row. Useful for looking up the row of a known ID. The underlying Map is a
   * LinkedHashMap, so the ordering of the keySet() is retained based on the order of the rowset
   * Example ResultSet: col1 col2 value ---------------------- A 1 valA1 B 2 valA2 C 1 valB1
   * Output Map: A => row1Bean B => row2Bean C => row3Bean
   *
   * @param dynaBeans the dyna beans
   * @param col       the col
   * @return the map
   */
  public static Map listBeanToMapBean(List dynaBeans, String col) {
    HashMap rowSetMap = new LinkedHashMap();

    for (BasicDynaBean bean : (List<BasicDynaBean>) dynaBeans) {
      rowSetMap.put(bean.get(col), bean);
    }
    return rowSetMap;
  }

  /**
   * List map to map map.
   *
   * @param listMap the list map
   * @param col     the col
   * @return the map
   */
  public static Map listMapToMapMap(List<Map> listMap, String col) {
    HashMap rowSetMap = new LinkedHashMap();

    for (Map map : listMap) {
      rowSetMap.put(map.get(col).toString(), map);
    }
    return rowSetMap;
  }

  /**
   * List bean to map map. Same as above, but the result is a map-map, so that in JSPs, you don't
   * have to use bean.map[colname], you can address it by bean[colname]. Note that this is useful
   * only for JSPs. FTLs anyway seem to be able to handle the bean directly using bean[colname].
   *
   * @param dynaBeans the dyna beans
   * @param col       the col
   * @return the map
   */
  public static Map listBeanToMapMap(List dynaBeans, String col) {
    HashMap rowSetMap = new LinkedHashMap();

    for (BasicDynaBean bean : (List<BasicDynaBean>) dynaBeans) {
      rowSetMap.put(bean.get(col), bean.getMap());
    }
    return rowSetMap;
  }

  /**
   * Same as above, but works on a numeric column in the bean, adds a _total attribute.
   *
   * @param dynaBeans the dyna beans
   * @param col       the col
   * @param valueCol  the value col
   * @return the map
   */
  public static Map listBeanToMapNumeric(List dynaBeans, String col, String valueCol) {
    HashMap rowSetMap = new LinkedHashMap();

    for (BasicDynaBean bean : (List<BasicDynaBean>) dynaBeans) {
      Object value = bean.get(valueCol);

      rowSetMap.put(bean.get(col), value);

      Object total = rowSetMap.get(TOTAL);
      rowSetMap.put(TOTAL, addNumber(total, value));
    }
    return rowSetMap;
  }

  /**
   * List bean to map list bean. Converts a list of DynaBeans into a HashMap of Lists based on the
   * value of one column, where the given column is NOT a unique identifier, instead, is a reference
   * to multiple rows. Thus, the value of the HashMap is a list of rows rather than a single row as
   * in the previous method
   * Example Rowset: key col1 col2 ----------------- A v1 v2 A v3 v4 B v5 v6
   * Output Map: A => [row1bean, row2Bean] B => [row3Bean]
   * Useful for hierarchical data like roles -> users, and we want to iterate like this: for each
   * role for each user
   * (the keySet() of the returned map can be used to get the list of roles.)
   *
   * @param beans      the beans
   * @param columnName the column name
   * @return the hash map
   */
  public static HashMap listBeanToMapListBean(List beans, String columnName) {
    HashMap rowMap = new LinkedHashMap();
    Iterator it = beans.iterator();
    while (it.hasNext()) {
      DynaBean row = (DynaBean) it.next();
      Object colName = row.get(columnName);
      List list = (List) rowMap.get(colName);
      if (list == null) {
        list = new ArrayList();
        rowMap.put(colName, list);
      }
      list.add(row);
    }
    return rowMap;
  }

  /**
   * List bean to map list map. Same as above, but the result is a map instead of a bean for ease of
   * use in JSPs.
   * 
   * @param beans      the beans
   * @param columnName the column name
   * @return the hash map
   */
  public static HashMap listBeanToMapListMap(List beans, String columnName) {
    HashMap rowMap = new LinkedHashMap();
    Iterator it = beans.iterator();
    while (it.hasNext()) {
      BasicDynaBean row = (BasicDynaBean) it.next();
      Object colName = row.get(columnName);
      List list = (List) rowMap.get(colName);
      if (list == null) {
        list = new ArrayList();
        rowMap.put(colName, list);
      }
      list.add(row.getMap());
    }
    return rowMap;
  }

  /**
   * List map to map list map. Same as above, but the input is list maps instead of list of
   * BasicDynaBeans.
   *
   * @param maps       the maps
   * @param columnName the column name
   * @return the hash map
   */
  public static HashMap listMapToMapListMap(List<Map> maps, String columnName) {
    HashMap rowMap = new LinkedHashMap();
    Iterator it = maps.iterator();
    while (it.hasNext()) {
      Map row = (Map) it.next();
      Object colName = row.get(columnName);
      List list = (List) rowMap.get(colName);
      if (list == null) {
        list = new ArrayList();
        rowMap.put(colName, list);
      }
      list.add(row);
    }
    return rowMap;
  }

  /**
   * List bean to map list list bean. 3-level hierarchy grouping of results. (eg,
   * visit->reports->tests) Example: key1 key2 value(s) ------------------------------- A 1 row1 A 1
   * row2 A 2 row3 B 1 row4 B 2 row4 B 2 row4
   * Output is a map of list of list of beans like this: A => [ [row1bean,row2bean], [row3bean] ] B
   * => [ [row4bean], [row5bean,row6bean] ]
   * In JSP, the iteration can be like this: c:forEach var="outer" items="mainList" <%-- iterate
   * over output, A,B --%> c:forEach var="inner" items="outer" <%-- iterate over 1,2 --%> c:forEach
   * var="leaf" items="inner" <%-- iterate over row1bean, row2bean --%>
   *
   * @param beans    the beans
   * @param colName1 the col name 1
   * @param colName2 the col name 2
   * @return the map
   */
  public static Map listBeanToMapListListBean(List<BasicDynaBean> beans, String colName1,
      String colName2) {
    /*
     * outerMap => innerList -> records
     */
    Map<Object, List<List>> outerMap = new LinkedHashMap<Object, List<List>>();
    /*
     * trackingMap keeps a map of outerVal+innerVal to the innerList, this is temp to keep track of
     * which inner list to add to, when the records don't come in a sorted order trackingMap =>
     * innerMap => records
     */
    Map<Object, Map<Object, List>> trackingMap = new HashMap<Object, Map<Object, List>>();

    for (BasicDynaBean bean : beans) {
      Object outerVal = (Object) bean.get(colName1); // A
      Object innerVal = (Object) bean.get(colName2); // 1

      List<List> innerList = outerMap.get(outerVal); // get outerMap.A
      if (innerList == null) {
        innerList = new ArrayList<List>(); // first time, allocate new list
        outerMap.put(outerVal, innerList); // outerMap.A = []
      }

      Map<Object, List> innerMap = trackingMap.get(outerVal); // get trackingMap.A
      if (innerMap == null) {
        innerMap = new HashMap<Object, List>();
        trackingMap.put(outerVal, innerMap); // trackingMap.A = {}
      }

      List<BasicDynaBean> records = innerMap.get(innerVal); // get trackingMap.A.1
      if (records == null) {
        records = new ArrayList<BasicDynaBean>();
        innerMap.put(innerVal, records); // trackingMap.A.1 = []
        innerList.add(records); // outerMap.A[0] = []
      }

      /*
       * trackingMap.A.1[0] = bean, same as outerMap.A[0][0] = bean
       */
      records.add(bean);
    }
    return outerMap;
  }

  /**
   * List bean to map map bean. Convert a dynalist to a two-D Map (like a cross-tab), where the
   * primary key in the result set is two columns.
   * Note: - The outer as well as inner Maps' order is retained based on the result set order. by
   * using a LinkedHashMap as the underlying map. - All possible values need not exist in the
   * result. - It is assumed that col1 and col2 values cannot be null in the dynaBeans.
   * Example ResultSet: col1 col2 value ---------------------- A 1 valA1 A 2 valA2 B 1 valB1
   * Output Map: A => {1 => row1Bean, 2 => row2Bean} B => {1 => row3Bean}
   * CrossTab: 1 2 A valA1 valA2 B valB1 valB2
   * In the JSP, you can access the contents of any cell directly, for example,
   * ${result['A']['1'].map.value} will give 'valA1'.
   *
   * @param dynaBeans the dyna beans
   * @param col1      the col 1
   * @param col2      the col 2
   * @return the map
   */
  public static Map listBeanToMapMapBean(List dynaBeans, String col1, String col2) {
    HashMap outer = new LinkedHashMap();
    for (BasicDynaBean bean : (List<BasicDynaBean>) dynaBeans) {
      Object col1Val = bean.get(col1);
      Object col2Val = bean.get(col2);

      Map inner = (Map) outer.get(col1Val);

      if (inner == null) {
        inner = new LinkedHashMap();
        outer.put(col1Val, inner);
      }

      inner.put(col2Val, bean);
    }
    return outer;
  }

  /**
   * List map to map map bean.
   *
   * @param mapBeans the map beans
   * @param col1     the col 1
   * @param col2     the col 2
   * @return the map
   */
  public static Map listMapToMapMapBean(List mapBeans, String col1, String col2) {
    HashMap outer = new LinkedHashMap();
    for (Map mapbean : (List<Map>) mapBeans) {
      Object col1Val = mapbean.get(col1);
      Object col2Val = mapbean.get(col2);

      Map inner = (Map) outer.get(col1Val);
      if (inner == null) {
        inner = new LinkedHashMap();
        outer.put(col1Val, inner);
      }
      inner.put(col2Val, mapbean);
    }

    return outer;
  }

  /**
   * List bean to map map map.
   *
   * @param dynaBeans the dyna beans
   * @param col1      the col 1
   * @param col2      the col 2
   * @return the map
   */
  public static Map listBeanToMapMapMap(List dynaBeans, String col1, String col2) {
    HashMap outer = new LinkedHashMap();
    for (BasicDynaBean bean : (List<BasicDynaBean>) dynaBeans) {
      Object col1Val = bean.get(col1);
      Object col2Val = bean.get(col2);

      Map inner = (Map) outer.get(col1Val);

      if (inner == null) {
        inner = new LinkedHashMap();
        outer.put(col1Val, inner);
      }

      inner.put(col2Val, bean.getMap());
    }
    return outer;
  }

  /**
   * List bean to map map list map. 3-level hierarchy grouping of results. Example: key1 key2
   * value(s) ------------------------------- A 1 row1 A 1 row2 A 2 row3 B 1 row4 B 2 row4 B 2 row4
   * Output is a map of map of list of map like this: A => [ 1 => [row1map,row2map], 2 => [row3map]
   * ] B => [ 1 => [row4map], 2=> [row5map,row6map] ]
   *
   * @param dynaBeans the dyna beans
   * @param col1      the col 1
   * @param col2      the col 2
   * @return the map
   */
  public static Map listBeanToMapMapListMap(List dynaBeans, String col1, String col2) {
    HashMap outer = new LinkedHashMap();
    for (BasicDynaBean bean : (List<BasicDynaBean>) dynaBeans) {
      Object col1Val = bean.get(col1);
      Object col2Val = bean.get(col2);

      Map inner = (Map) outer.get(col1Val);
      if (inner == null) {
        inner = new LinkedHashMap();
        outer.put(col1Val, inner);
      }

      List inner1 = (List) inner.get(col2Val);
      if (inner1 == null) {
        inner1 = new ArrayList();
        inner.put(col2Val, inner1);
      }
      inner1.add(bean.getMap());
    }
    return outer;
  }

  /** The Constant TOTAL. */
  /*
   * Same as listBeanToMapMapBean, but the "cell" value is assumed to be a number, so that we can do
   * some totalling etc. and give additional totals in the crosstab output. Now, the cell value is
   * accessed directly like this:
   *
   * ${result['A']['1']}
   *
   * The row and column totals are added as special values, can be accessed like this:
   *
   * ${result['A']._total} ${result._total['1']}
   */
  public static final String TOTAL = "_total";

  /**
   * List bean to map map numeric.
   *
   * @param dynaBeans the dyna beans
   * @param col1      the col 1
   * @param col2      the col 2
   * @param valueCol  the value col
   * @return the map
   */
  public static Map listBeanToMapMapNumeric(List dynaBeans, String col1, String col2,
      String valueCol) {

    HashMap outer = new LinkedHashMap();

    for (BasicDynaBean bean : (List<BasicDynaBean>) dynaBeans) {
      Object col1Val = bean.get(col1);
      Object col2Val = bean.get(col2);

      Map inner = (Map) outer.get(col1Val);

      if (inner == null) {
        inner = new LinkedHashMap();
        outer.put(col1Val, inner);
      }

      Object value = bean.get(valueCol);
      inner.put(col2Val, value);

      // total: column
      Map colTotalMap = (Map) outer.get(TOTAL);
      if (colTotalMap == null) {
        colTotalMap = new HashMap();
        outer.put(TOTAL, colTotalMap);
      }

      Object colTotal = colTotalMap.get(col2Val);
      colTotalMap.put(col2Val, addNumber(colTotal, value));

      // total: row
      Object rowTotal = inner.get(TOTAL);
      inner.put(TOTAL, addNumber(rowTotal, value));

      // total: grand
      Object grandTotal = colTotalMap.get(TOTAL);
      colTotalMap.put(TOTAL, addNumber(grandTotal, value));
    }

    return outer;
  }

  /**
   * List bean to map map total numeric. The "cell" value is assumed to be a number (BigDecimal or
   * Integer), it's use to add upper cell values with current cell value when loop goes on so that
   * we can do some totalling etc. and give additional totals in the crosstab output. Assume first
   * row cell value is one. Assume second row cell value is one. Now, the cell value is accessed
   * directly like this:
   * ${result['A']['1']} this is first row of cell ${result['B']['2']} this is second row of cell
   * The row and column totals are added as special values, can be accessed like this:
   * ${result['A']._total} ${result._total['1']}
   *
   * @param dynaBeans the dyna beans
   * @param col1      the col 1
   * @param col2      the col 2
   * @param valueCol  the value col
   * @return the map
   */
  public static Map listBeanToMapMapTotalNumeric(List dynaBeans, String col1, String col2,
      String valueCol) {

    HashMap outer = new LinkedHashMap();
    Object innerValue = null;
    String ctotal = "_ctotal";

    for (BasicDynaBean bean : (List<BasicDynaBean>) dynaBeans) {
      Object col1Val = bean.get(col1);
      Object col2Val = bean.get(col2);

      Map inner = (Map) outer.get(col1Val);

      if (inner == null) {
        inner = new LinkedHashMap();
        outer.put(col1Val, inner);
        innerValue = null;
      }

      Object value = bean.get(valueCol);
      inner.put(col2Val, addNumber(inner.get(TOTAL), value));

      // total: column
      Map colTotalMap = (Map) outer.get(TOTAL);
      if (colTotalMap == null) {
        colTotalMap = new HashMap();
        outer.put(TOTAL, colTotalMap);
      }

      Object colTotal = colTotalMap.get(col2Val);
      colTotalMap.put(col2Val, addNumber(colTotal, addNumber(inner.get(TOTAL), value)));

      // total: row
      Object rowTotal = inner.get(TOTAL);
      inner.put(TOTAL, addNumber(rowTotal, value));
      innerValue = addNumber(innerValue, inner.get(col2Val));
      inner.put(ctotal, innerValue);

      // total: grand
      Object grandTotal = colTotalMap.get(TOTAL);
      Object innerTotal = inner.get(TOTAL);
      colTotalMap.put(TOTAL, addNumber(grandTotal, addNumber(innerTotal, value)));

    }

    return outer;
  }

  /**
   * List bean to map map map numeric. 3 level deep map consolidation/totalling
   *
   * @param dynaBeans the dyna beans
   * @param col1      the col 1
   * @param col2      the col 2
   * @param col3      the col 3
   * @param valueCol  the value col
   * @return the map
   */
  public static Map listBeanToMapMapMapNumeric(List dynaBeans, String col1, String col2,
      String col3, String valueCol) {

    Map map = new HashMap();

    for (BasicDynaBean bean : (List<BasicDynaBean>) dynaBeans) {
      Object col1Val = bean.get(col1);
      Object col2Val = bean.get(col2);
      Object col3Val = bean.get(col3);

      Object value = bean.get(valueCol);

      log.debug(
          "Map3 numeric, processing: " + col1Val + "." + col2Val + "." + col3Val + "=" + value);

      // set the value: map[col1val][col2val][col3val] = value
      setMapValue(map, col1Val, col2Val, col3Val, value);

      // add to each of the totals: all possible combinations!
      addMapValue(map, col1Val, col2Val, TOTAL, value);
      addMapValue(map, col1Val, TOTAL, col3Val, value);
      addMapValue(map, TOTAL, col2Val, col3Val, value);

      addMapValue(map, TOTAL, TOTAL, col3Val, value);
      addMapValue(map, TOTAL, col2Val, TOTAL, value);
      addMapValue(map, col1Val, TOTAL, TOTAL, value);

      addMapValue(map, TOTAL, TOTAL, TOTAL, value);
    }
    return map;
  }

  /**
   * Group by column. TODO: same as listBeanToMapListBean, except this expects the results to be
   * sorted in the group by order.
   *
   * @param beans   the beans
   * @param colName the col name
   * @return the map
   */
  public static Map groupByColumn(List<BasicDynaBean> beans, String colName) {
    Map<Object, List> map = new LinkedHashMap<Object, List>();
    List<BasicDynaBean> records = null;
    for (BasicDynaBean bean : beans) {
      Object key = (Object) bean.get(colName);
      if (!map.containsKey(key)) {
        records = new ArrayList<BasicDynaBean>();
        map.put(key, records);
      }
      records.add(bean);
    }
    return map;
  }

  /**
   * Group by column. Three-level grouping, results in Map => Map => List, eg A => 1 => [bean, bean
   * ...]
   *
   * @param beans    the beans
   * @param colName1 the col name 1
   * @param colName2 the col name 2
   * @return the map
   */
  public static Map groupByColumn(List<BasicDynaBean> beans, String colName1, String colName2) {
    Map<Object, Map<Object, List>> map = new LinkedHashMap<Object, Map<Object, List>>();
    List<BasicDynaBean> records = null;
    Map<Object, List> innerColMap = null;
    for (BasicDynaBean bean : beans) {
      Object key = (Object) bean.get(colName1);
      Object colName2Val = (Object) bean.get(colName2);

      if (!map.containsKey(key)) {
        if (map.get(key) == null) {
          innerColMap = new LinkedHashMap<Object, List>();
        } else {
          innerColMap = (LinkedHashMap<Object, List>) map.get(key);
        }

        records = new ArrayList<BasicDynaBean>();
        innerColMap.put(colName2Val, records);

        map.put(key, innerColMap);
      } else {
        if (!innerColMap.containsKey(colName2Val)) {
          records = new ArrayList<BasicDynaBean>();
          innerColMap.put(colName2Val, records);
        } else {
          records = (ArrayList<BasicDynaBean>) innerColMap.get(colName2Val);
        }
      }
      records.add(bean);
    }
    return map;
  }

  /**
   * Sets the map value. Sets a nested map value, if there is no map along the hierarchy, creates
   * the map as required. Equivalent to m[id1][id2] = value;
   *
   * @param map   the m
   * @param id1   the id 1
   * @param id2   the id 2
   * @param value the value
   */
  private static void setMapValue(Map map, Object id1, Object id2, Object value) {
    log.debug("Set map value2: " + id1 + "." + id2 + "=" + value);
    Map val1 = (Map) map.get(id1);
    if (val1 == null) {
      val1 = new HashMap();
      map.put(id1, val1);
    }
    val1.put(id2, value);
  }

  /**
   * Sets the map value. Sets a nested map value, if there is no map along the hierarchy, creates
   * the map as required. Equivalent to m[id1][id2][id3] = value;
   *
   * @param map   the m
   * @param id1   the id 1
   * @param id2   the id 2
   * @param id3   the id 3
   * @param value the value
   */
  private static void setMapValue(Map map, Object id1, Object id2, Object id3, Object value) {
    log.debug("Set map value3: " + id1 + "." + id2 + "." + id3 + "=" + value);
    Map val1 = (Map) map.get(id1);
    if (val1 == null) {
      val1 = new HashMap();
      map.put(id1, val1);
    }
    Map val2 = (Map) val1.get(id2);
    if (val2 == null) {
      val2 = new HashMap();
      val1.put(id2, val2);
    }
    val2.put(id3, value);
  }

  /**
   * Gets the map value. Gets a nested map value, if there is no map along the hierarchy, returns
   * null Equivalent to m[id1][id2]
   *
   * @param map the m
   * @param id1 the id 1
   * @param id2 the id 2
   * @return the map value
   */
  public static Object getMapValue(Map map, Object id1, Object id2) {
    log.debug("Get map value2: " + id1 + "." + id2);
    Map val1 = (Map) map.get(id1);
    if (val1 == null) {
      return null;
    }
    Object value = val1.get(id2);
    log.debug(" .. returning " + value);
    return value;
  }

  /**
   * Gets the map value. Gets a nested map value, if there is no map along the hierarchy, returns
   * null Equivalent to m[id1][id2][id3]
   *
   * @param map the m
   * @param id1 the id 1
   * @param id2 the id 2
   * @param id3 the id 3
   * @return the map value
   */
  public static Object getMapValue(Map map, Object id1, Object id2, Object id3) {
    log.debug("Get map value3: " + id1 + "." + id2 + "." + id3);
    Map val1 = (Map) map.get(id1);
    if (val1 == null) {
      return null;
    }
    log.debug("val2: " + val1.get(id2));
    Map val2 = (Map) val1.get(id2);
    if (val2 == null) {
      return null;
    }
    Object value = val2.get(id3);
    log.debug(" .. returning " + value);
    return value;
  }

  /**
   * Adds the map value. Adds a number to an existing map value. Null considered as 0. Equivalent to
   * m[id1][id2] += value.
   *
   * @param map   the m
   * @param id1   the id 1
   * @param id2   the id 2
   * @param value the value
   */
  public static void addMapValue(Map map, Object id1, Object id2, Object value) {
    log.debug("Adding value2: " + id1 + "." + id2 + "+=" + value);
    Object curNumber = getMapValue(map, id1, id2);
    Object newNumber = addNumber(curNumber, value);
    setMapValue(map, id1, id2, newNumber);
  }

  /**
   * Adds the map value. Adds a number to an existing map value. Null considered as 0. Equivalent to
   * m[id1][id2][id3] += value.
   *
   * @param map   the m
   * @param id1   the id 1
   * @param id2   the id 2
   * @param id3   the id 3
   * @param value the value
   */
  public static void addMapValue(Map map, Object id1, Object id2, Object id3, Object value) {
    log.debug("Adding value3: " + id1 + "." + id2 + "." + id3 + "+=" + value);
    Object curNumber = getMapValue(map, id1, id2, id3);
    Object newNumber = addNumber(curNumber, value);
    setMapValue(map, id1, id2, id3, newNumber);
  }

  /**
   * Adds the number. Adds two numbers, the number is an Object, can be of type BigDecimal, Integer,
   * Float or Double. The first argument can be null, so that the return is a copy of the second
   * argument.
   *
   * @param number the number
   * @param toAdd  the to add
   * @return the object
   */
  public static Object addNumber(Object number, Object toAdd) {

    if (toAdd == null) {
      return number;
    }

    if (toAdd instanceof BigDecimal) {
      if (number == null) {
        number = BigDecimal.ZERO;
      }
      return ((BigDecimal) number).add((BigDecimal) toAdd);

    } else if (toAdd instanceof Long) {
      if (number == null) {
        return new Long(((Long) toAdd).longValue());
      } else {
        return new Long(((Long) number).longValue() + ((Long) toAdd).longValue());
      }

    } else if (toAdd instanceof Integer) {
      if (number == null) {
        return new Integer(((Integer) toAdd).intValue());
      } else {
        return new Integer(((Integer) number).intValue() + ((Integer) toAdd).intValue());
      }

    } else if (toAdd instanceof Float) {
      if (number == null) {
        return new Float(((Float) toAdd).floatValue());
      } else {
        return new Float(((Float) number).floatValue() + ((Float) toAdd).floatValue());
      }

    } else if (toAdd instanceof Double) {
      if (number == null) {
        return new Double(((Double) toAdd).doubleValue());
      } else {
        return new Double(((Double) number).doubleValue() + ((Double) toAdd).doubleValue());
      }
    } else {
      log.warn("Unknown number type in addNumber: " + toAdd.getClass());
      return null;
    }
  }

  /**
   * Consolidate map map. consolidate a map of values/totals so that only maxCount (including one
   * extra for "others" are retained and the rest are summed up into an "others" item.
   *
   * @param map      the m
   * @param items    the items
   * @param maxCount the max count
   */
  public static void consolidateMapMap(Map map, List items, int maxCount) {
    int len = items.size();
    for (int i = len - 1; i >= maxCount - 1; i--) {
      Object item = items.get(i);
      Map innerMap = (Map) map.get(item);
      for (Object o : innerMap.keySet()) {
        addMapValue(map, "_others", o, innerMap.get(o));
      }
    }
  }

  /**
   * The Class ByTotal. Use for sorting a resultMap.
   */
  public static class ByTotal implements java.util.Comparator {

    /** The result map. */
    Map resultMap;

    /**
     * Instantiates a new by total.
     *
     * @param map the m
     */
    public ByTotal(Map map) {
      this.resultMap = map;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Object first, Object second) {
      if (first == null && second == null) {
        return 0;
      }
      if (first == null) {
        return -1;
      }
      if (second == null) {
        return 1;
      }
      Comparable firstTotal = (Comparable) ((HashMap) resultMap.get(first)).get("_total");
      Comparable secondTotal = (Comparable) ((HashMap) resultMap.get(second)).get("_total");
      return -firstTotal.compareTo(secondTotal);
    }
  }

  /**
   * The Class ByTotalTotal.
   */
  public static class ByTotalTotal implements java.util.Comparator {

    /** The result map. */
    Map resultMap;

    /**
     * Instantiates a new by total total.
     *
     * @param map the m
     */
    public ByTotalTotal(Map map) {
      this.resultMap = map;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Object first, Object second) {
      if (first == null && second == null) {
        return 0;
      }
      if (first == null) {
        return -1;
      }
      if (second == null) {
        return 1;
      }
      Comparable firstTotal = (Comparable) ((HashMap) ((HashMap) resultMap.get(first))
          .get("_total")).get("_total");
      Comparable secondTotal = (Comparable) ((HashMap) ((HashMap) resultMap.get(second))
          .get("_total")).get("_total");
      return -firstTotal.compareTo(secondTotal);
    }
  }

  /**
   * col valueCol ---------- A 2 B 4 A 5 c 6 A 7
   * then the result map will look like A 14 B 4 c 6 _total 24.
   *
   * @param list     the list
   * @param col      the col
   * @param valueCol the value col
   * @return the map
   */
  public static Map sumByColumn(List<BasicDynaBean> list, String col, String valueCol) {
    Map map = new LinkedHashMap();
    for (BasicDynaBean bean : list) {
      Object value = bean.get(valueCol);
      if (map.containsKey(bean.get(col))) {
        Object totalByCol = map.get(bean.get(col));
        map.put(bean.get(col), addNumber(totalByCol, value));
      } else {
        map.put(bean.get(col), bean.get(valueCol));
      }

      Object total = map.get(TOTAL);
      map.put(TOTAL, addNumber(total, value));
    }
    return map;
  }

  /**
   * Gets the parameter map. Convert a string like a URL to a parameter Map like one found in
   * request.getParameterMap.
   *
   * @param queryParams the query params
   * @return the parameter map
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  public static Map getParameterMap(String queryParams) throws UnsupportedEncodingException {

    Map<String, List> paramMapList = new HashMap();
    String[] parameterPair = queryParams.split("&");

    for (int i = 0; i < parameterPair.length; i++) {
      String paramValue = parameterPair[i];
      int eq = paramValue.indexOf('=');
      if (eq == -1) {
        continue;
      }

      String name = paramValue.substring(0, eq);
      String value = paramValue.substring(eq + 1);
      value = URLDecoder.decode(value, "UTF-8");

      List valueList = paramMapList.get(name);
      if (valueList == null) {
        valueList = new ArrayList();
        paramMapList.put(name, valueList);
      }
      valueList.add(value);
    }

    Map<String, String[]> paramMapArray = new HashMap();
    for (Map.Entry<String, List> e : paramMapList.entrySet()) {
      Object[] objArray = e.getValue().toArray();
      String[] strArray = new String[objArray.length];
      int index = 0;
      for (Object obj : objArray) {
        strArray[index++] = obj == null ? null : (String) obj;
      }
      paramMapArray.put(e.getKey(), strArray);
    }

    return paramMapArray;
  }

  /**
   * Gets the param value. Gets the first value of a request parameter map.
   *
   * @param map        the map
   * @param key        the key
   * @param defaultVal the default val
   * @return the param value
   */
  public static String getParamValue(Map<String, String[]> map, String key, String defaultVal) {
    String[] values = map.get(key);
    if (values == null) {
      return defaultVal;
    }
    if (values[0] == null) {
      return defaultVal;
    }
    if (values[0].equals("")) {
      return defaultVal;
    }
    return values[0];
  }

  /**
   * Gets the parameter.
   *
   * @param params the params
   * @param key    the key
   * @param index  the i
   * @return the parameter
   */
  public static String getParameter(Map params, String key, int index) {
    return ((String[]) params.get(key))[index];
  }

  /**
   * 28-Apr-2011: Wrapper around an existing method in BigDecimal. Sets scale to whatever is
   * specified in the preferences. If nothing is set, defaults scale to 2
   *
   * @param val              the val
   * @param useHighPrecision the use high precision
   * @return the big decimal
   */
  public static BigDecimal setScale(BigDecimal val, boolean useHighPrecision) {
    BigDecimal myVal = val;
    int numDecimals = 2;
    try {
      numDecimals = GenericPreferencesDAO.getGenericPreferences().getDecimalDigits();
    } catch (SQLException exception) {
      log.error("Unable to retrieve decimal places preferences, assuming 2: ", exception);
      // assume 2 decimal places, do nothing.
    }
    BigDecimal returnValue = myVal.setScale(numDecimals + (useHighPrecision ? 2 : 0),
        BigDecimal.ROUND_HALF_UP);
    return returnValue;
  }

  /**
   * Sets the scale.
   *
   * @param val the val
   * @return the big decimal
   */
  public static BigDecimal setScale(BigDecimal val) {
    BigDecimal myVal = val;
    int numDecimals = 2;
    try {
      numDecimals = GenericPreferencesDAO.getGenericPreferences().getDecimalDigits();
    } catch (SQLException exception) {
      log.error("Unable to retrieve decimal places preferences, assuming 2: ", exception);
      // assume 2 decimal places, do nothing.
    }
    BigDecimal returnValue = myVal.setScale(numDecimals, BigDecimal.ROUND_HALF_UP);
    return returnValue;
  }

  /**
   * Sets the scale.
   *
   * @param con the con
   * @param val the val
   * @return the big decimal
   */
  public static BigDecimal setScale(Connection con, BigDecimal val) {
    BigDecimal myVal = val;
    int numDecimals = 2;
    try {
      numDecimals = GenericPreferencesDAO.getGenericPreferences(con).getDecimalDigits();
    } catch (SQLException exception) {
      log.error("Unable to retrieve decimal places preferences, assuming 2: ", exception);
      // assume 2 decimal places, do nothing.
    }
    BigDecimal returnValue = myVal.setScale(numDecimals, BigDecimal.ROUND_HALF_UP);
    return returnValue;
  }

  /**
   * Sets the scale down.
   *
   * @param val the val
   * @return the big decimal
   */
  public static BigDecimal setScaleDown(BigDecimal val) {
    BigDecimal myVal = val;
    int numDecimals = 2;
    try {
      numDecimals = GenericPreferencesDAO.getGenericPreferences().getDecimalDigits();
    } catch (SQLException exception) {
      log.error("Unable to retrieve decimal places preferences, assuming 2: ", exception);
      // assume 2 decimal places, do nothing.
    }
    BigDecimal returnValue = myVal.setScale(numDecimals, BigDecimal.ROUND_HALF_DOWN);
    return returnValue;
  }

  /**
   * Divide.
   *
   * @param numerator   the numerator
   * @param denominator the denominator
   * @return the big decimal
   */
  public static BigDecimal divide(BigDecimal numerator, BigDecimal denominator) {
    int numDecimals = 2;
    try {
      numDecimals = GenericPreferencesDAO.getGenericPreferences().getDecimalDigits();
    } catch (SQLException exception) {
      log.error("Unable to retrieve decimal places preferences, assuming 2: ", exception);
    }
    BigDecimal returnValue = numerator.divide(denominator, numDecimals, BigDecimal.ROUND_HALF_UP);
    return returnValue;
  }

  /**
   * divide with 2 extra decimal places for a higher precision division.
   *
   * @param numerator   the numerator
   * @param denominator the denominator
   * @return the big decimal
   */
  public static BigDecimal divideHighPrecision(BigDecimal numerator, BigDecimal denominator) {
    int numDecimals = 2;
    try {
      numDecimals = GenericPreferencesDAO.getGenericPreferences().getDecimalDigits();
    } catch (SQLException exception) {
      log.error("Unable to retrieve decimal places preferences, assuming 2: ", exception);
    }
    BigDecimal returnValue = numerator.divide(denominator, numDecimals + 2,
        BigDecimal.ROUND_HALF_UP);
    return returnValue;
  }

  /**
   * 10-May-2011: Wrapper around an existing method in BigDecimal. Sets scale to whatever is
   * specified in the preferences. If nothing is set, defaults scale to 2
   *
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String setFormat() throws SQLException {
    String formatStr = "";
    int numDecimals = GenericPreferencesDAO.getGenericPreferences().getDecimalDigits();
    if (numDecimals == 2) {
      formatStr = "#0.00";
    } else if (numDecimals == 3) {
      formatStr = "#0.000";
    } else {
      formatStr = "#0.00";
    }
    return formatStr;
  }

  /**
   * Gets the decimal digits.
   *
   * @return the decimal digits
   */
  public static int getDecimalDigits() {
    int numDecimals = 2;
    try {
      numDecimals = GenericPreferencesDAO.getGenericPreferences().getDecimalDigits();
    } catch (SQLException exception) {
      log.error("Unable to retrieve decimal places preferences, assuming 2: ", exception);
    }
    return numDecimals;
  }

  /**
   * Gets the round off amount. Return the round-off amount in paise. Eg, 10.25 will return -0.25
   * and 10.75 will return 0.25.
   *
   * @param amount the amount
   * @return the round off amount
   */
  public static BigDecimal getRoundOffAmount(BigDecimal amount) {
    int numDecimals = getDecimalDigits();
    BigDecimal divisor = (numDecimals == 2) ? new BigDecimal("100") : new BigDecimal("1000");
    BigDecimal change = (amount.multiply(divisor)).remainder(divisor); // 25 or 75

    if (change.compareTo(divisor.divide(new BigDecimal("2"))) >= 0) {
      return (divisor.subtract(change)).divide(divisor); // 100 - 75
    } else {
      change = (change.compareTo(new BigDecimal("0")) < 0) 
          ? change.multiply(new BigDecimal("-1")) : change;
      return (BigDecimal.ZERO.subtract(change)).divide(divisor); // 0 -25
    }
  }

  /**
   * Copy bean to bean.
   *
   * @param from the from
   * @param to   the to
   */
  public static void copyBeanToBean(BasicDynaBean from, BasicDynaBean to) {

    Set<String> columnSet = from.getMap().keySet();
    Iterator<String> it = columnSet.iterator();
    String column = null;

    while (it.hasNext()) {
      column = it.next();
      to.set(column, from.get(column));
    }
  }

  /**
   * Copy string to map.
   *
   * @param params the params
   * @param key    the key
   * @param value  the value
   */
  public static void copyStringToMap(Map params, String key, String value) {

    if (params.containsKey(key)) {
      String[] obj = (String[]) params.get(key);
      String[] newArray = Arrays.copyOf(obj, obj.length + 1);
      newArray[obj.length] = value;
      params.put(key, newArray);

    } else {
      params.put(key, new String[] { value });
    }
  }

  /**
   * Copy object to map.
   *
   * @param params the params
   * @param key    the key
   * @param value  the value
   */
  public static void copyObjectToMap(Map params, String key, Object value) {

    if (params.containsKey(key)) {
      Object[] obj = (Object[]) params.get(key);
      Object[] newArray = Arrays.copyOf(obj, obj.length + 1);
      newArray[obj.length] = value;
      params.put(key, newArray);

    } else {
      params.put(key, new Object[] { value });
    }
  }

  /*
   * List of BasicDynaBeans to be converted to example structure List l = new ArrayList();
   * l.add("section_id"); l.add("section_detail_id"); l.add("finalized");
   * 
   * Map m = new HashMap(); List ab = new ArrayList(); ab.add("vital_id"); ab.add("param_id");
   * m.put("params", ab); l.add(m);
   * 
   * Map values = new HashMap(); List ac = new ArrayList(); ac.add("param_value");
   * ac.add("param_remarks"); values.put("values", ac); l.add(values);
   * 
   * output:
   * 
   * { "finalized": "N", "section_id": 4, "section_detail_id": 4, "params": [ { "vital_id": 0,
   * "param_id": 0 }, { "vital_id": 1, "param_id": 1 }, { "vital_id": 2, "param_id": 2 }, {
   * "vital_id": 3, "param_id": 3 }, { "vital_id": 4, "param_id": 4 } ], "values": [ {
   * "param_value": "value", "param_remarks": "remarks" }, { "param_value": "value",
   * "param_remarks": "remarks" }, { "param_value": "value", "param_remarks": "remarks" }, {
   * "param_value": "value", "param_remarks": "remarks" }, { "param_value": "value",
   * "param_remarks": "remarks" } ] }
   */

  /**
   * Convert to structured map.
   *
   * @param list       the list
   * @param objList    the l
   * @param groupBy    the group by
   * @param secGroupBy the sec group by
   * @return the map
   */
  public static Map<String, Object> convertToStructuredMap(List<BasicDynaBean> list,
      List<Object> objList, String groupBy, String secGroupBy) {
    Map params = new LinkedHashMap();
    for (BasicDynaBean bean : list) {
      Iterator it = objList.iterator();
      while (it.hasNext()) {
        convert(it, bean, params, groupBy, secGroupBy);
      }
    }
    log.debug(new JSONSerializer().prettyPrint(true).deepSerialize(params));
    return params;
  }

  /**
   * Convert to structered map.
   *
   * @param list    the list
   * @param objList the l
   * @param groupBy the group by
   * @return the map
   */
  public static Map<String, Object> convertToStructeredMap(List<BasicDynaBean> list,
      List<Object> objList, String groupBy) {
    return convertToStructuredMap(list, objList, groupBy, null);
  }

  /**
   * Convert.
   *
   * @param it         the it
   * @param map        the map
   * @param params     the params
   * @param groupBy    the group by
   * @param secGroupBy the sec group by
   */
  public static void convert(Iterator it, BasicDynaBean map, Map params, String groupBy,
      String secGroupBy) {
    Object obj = it.next();
    if (obj instanceof String) {
      String string = (String) obj;
      params.put(string, map.get(string));
    } else if (obj instanceof List) {
      List<Map> mapList = (List<Map>) params.get("records");
      if (mapList == null) {
        mapList = new ArrayList<Map>();
      }

      params.put("records", mapList);
      Map mapObj = null;
      if (groupBy != null && !groupBy.equals("")) {
        for (Map g : mapList) {
          if (g.get(groupBy) != null && g.get(groupBy).equals(map.get(groupBy))) {
            mapObj = g;
          } else if (g.get(groupBy) == null && map.get(groupBy) == null) {
            mapObj = g;
          }
        }
      }
      if (mapObj == null) {
        mapObj = new LinkedHashMap();
        mapList.add(mapObj);
      }

      int outerAllNull = 0;
      List<Object> records = (List) obj;
      for (Object s : records) {
        if (s instanceof String) {
          if (map.get((String) s) == null) {
            outerAllNull++;
          }

          mapObj.put(s, map.get((String) s));
        } else if (s instanceof List) {
          List<String> strList = (List) s;
          List rl = (List) mapObj.get("records");
          if (rl == null) {
            rl = new ArrayList();
          }
          Map inner = new HashMap();
          int allNull = 0;
          for (String st : strList) {
            if (map.get(st) == null) {
              allNull++;
            }
            inner.put(st, map.get(st));
          }
          if (allNull == strList.size()) {
            outerAllNull++;
          } else {
            rl.add(inner);
          }
          mapObj.put("records", rl);
        } else if (s instanceof Map) {
          convertMap(s, map, mapObj, secGroupBy, null);
        }
      }
      if (outerAllNull == records.size()) {
        mapList.remove(mapObj);
      }

    } else if (obj instanceof Map) {
      convertMap(obj, map, params, groupBy, secGroupBy);
    }
  }

  /**
   * Convert map.
   *
   * @param obj        the obj
   * @param map        the map
   * @param params     the params
   * @param groupBy    the group by
   * @param secGroupBy the sec group by
   */
  private static void convertMap(Object obj, BasicDynaBean map, Map params, String groupBy,
      String secGroupBy) {

    for (Map.Entry<String, List<Object>> entry : ((Map<String, List<Object>>) obj).entrySet()) {
      List<Map> mapListl = (List<Map>) params.get(entry.getKey());
      if (mapListl == null) {
        mapListl = new ArrayList();
      }
      params.put(entry.getKey(), mapListl);

      Map mapObj = null;
      if (groupBy != null && !groupBy.equals("")) {
        for (Map g : mapListl) {
          if (g.get(groupBy) != null && g.get(groupBy).equals(map.get(groupBy))) {
            mapObj = g;
          } else if (g.get(groupBy) == null && map.get(groupBy) == null) {
            mapObj = g;
          }
        }
      }
      if (mapObj == null) {
        mapObj = new LinkedHashMap();
        mapListl.add(mapObj);
      }
      int outerAllNull = 0;
      for (Object s : entry.getValue()) {
        if (s instanceof String) {
          if (map.get((String) s) == null) {
            outerAllNull++;
          }

          mapObj.put(s, map.get((String) s));
        } else if (s instanceof List) {
          List<String> strList = (List) s;
          List rl = (List) mapObj.get("records");
          if (rl == null) {
            rl = new ArrayList();
          }
          int allNull = 0;
          Map inner = new HashMap();
          for (String st : strList) {
            if (map.get(st) == null) {
              allNull++;
            }
            inner.put(st, map.get(st));
          }

          if (allNull == strList.size()) {
            outerAllNull++;
          } else {
            rl.add(inner);
          }
          mapObj.put("records", rl);
        } else if (s instanceof Map) {
          convertMap(s, map, mapObj, secGroupBy, null);
        }
      }
      if (outerAllNull == entry.getValue().size()) {
        mapListl.remove(mapObj);
      }
    }

  }

}
