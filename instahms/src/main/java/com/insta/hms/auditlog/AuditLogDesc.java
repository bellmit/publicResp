package com.insta.hms.auditlog;

import com.bob.hms.common.DataBaseUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AuditLogDesc {

  static Logger logger = LoggerFactory.getLogger(AuditLogDesc.class);

  private static String TYPE_STATIC_MAP = "static_map";
  private static String TYPE_TABLE_LOOKUP = "table_lookup";
  public static String[] STD_AUDIT_LOG_FIELDS =
      { "log_id", "user_name", "mod_time", "operation", "field_name", "old_value", "new_value" };
  public static String DEFAULT_DISCRIMINATOR_KEY = "__default__";

  private String auditLogTable;

  public Map<String, FieldDesc> fieldNamesMap = new LinkedHashMap<>();

  private List keyFieldNameList;

  public AuditLogDesc(String auditLogTable) {
    this.auditLogTable = auditLogTable;
  }

  /** Required. */
  public Map<String, FieldDesc> getFields() {
    return fieldNamesMap;
  }

  /**
   * Method to get a list of all fields that can be specified as filter in audit log search.
   * 
   * @return List - containing each searchable field as map of the form {name=>"", display_name=>""}
   */
  public List getSearchableFieldList() {
    return getFieldNames(true);
  }

  /**
   * Method to get a list of all fields and their display names for a given audit log base table.
   * 
   * @return List - contains each field as map entry {name=> , display_name=>}
   */
  public List getAllFieldsList() {
    return getFieldNames(false);
  }

  private static final String GET_KEY_FIELD_LIST = "SELECT column_name"
      + " FROM information_schema.columns"
      + " WHERE table_schema=(select current_schema())"
      + " AND table_name=? AND column_name not in (#)";
  
  /**
   * Method to get a list of field names for fields other than the standard audit log fields.
   * Typically these represent the key fields of the base table - bill_no for bill_audit_log, mr_no
   * for patient_details etc. There could be more than one such field in an audit log table.
   *
   * @return the key field list
   */
  public List<String> getKeyFieldList() {
    if (keyFieldNameList != null) {
      return keyFieldNameList;
    }
    List<Object> parameters = new ArrayList<>();
    parameters.add(auditLogTable);
    for (String stdAuditLogField : STD_AUDIT_LOG_FIELDS) {
      parameters.add(stdAuditLogField);
    }
    String[] placeHolderArr = new String[STD_AUDIT_LOG_FIELDS.length];
    Arrays.fill(placeHolderArr, "?");
    String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
    List<Map> resultList = DataBaseUtil.queryToArrayList(
        GET_KEY_FIELD_LIST.replace("#", placeHolders), parameters.toArray());
    List<String> columnList = new ArrayList<>();
    for (Map columns : resultList) {
      String columnName = (String) columns.get("COLUMN_NAME");
      columnList.add(columnName);
    }
    keyFieldNameList = columnList;
    return keyFieldNameList;
  }

  /** Required. */
  public Map<String, FieldDesc> getKeyFields() {
    Map<String, FieldDesc> keyFieldMap = new LinkedHashMap<>();

    List<String> keyFieldNames = getKeyFieldList();

    if (null != keyFieldNames) {
      for (String keyField : keyFieldNames) {
        keyFieldMap.put(keyField, fieldNamesMap.get(keyField));
      }
    }
    return keyFieldMap;
  }

  /**
   * Method to get a map of all fields and their display names for a given audit log base table.
   * 
   * @return Map - containing entries as {field_name=>display_name}
   */
  public Map getFieldNameMap() {
    Map<String, String> fieldMap = new LinkedHashMap<>();
    for (Map.Entry<String, FieldDesc> entry : fieldNamesMap.entrySet()) {
      String key = entry.getKey();
      FieldDesc fd = entry.getValue();
      fieldMap.put(key, fd.displayName);
    }
    return fieldMap;
  }

  /**
   * Method to get the display names for the values of a given field.
   *
   * @param fieldName
   *          - name of the field for which display names for the values are required
   * @param discriminator
   *          the discriminator
   * @param filterKeys
   *          - set of values on which the values have to be filtered, useful only for database
   *          lookup
   * @return Map - containing entries of the form {{"ID=>{ID=> , VALUE=>}}, {ID=>{ID=> , VALUE=>}}
   * @throws SQLException
   *           the SQL exception
   */
  public Map getLookupValueMap(String fieldName, String discriminator, String[] filterKeys)
      throws SQLException {
    /*
     * TODO: The conversionutils method to convert a list to a map does not seem to work in this
     * case. Needs a bit of debugging. If that works we may not need this method, this conversion
     * can be done in the calling class, rather than here.
     */
    Map valueMap = null;
    return getLookupValues(fieldName, discriminator, filterKeys);
  }

  public Map getLookupValueMap(String fieldName, String[] filterKeys) throws SQLException {
    return getLookupValueMap(fieldName, DEFAULT_DISCRIMINATOR_KEY, filterKeys);
  }

  /**
   * Method to get the display names for the set of values for a given field.
   *
   * @param fieldName
   *          - name of the field for which the display names for values are required
   * @param discriminator
   *          the discriminator
   * @param filterKeys
   *          - set of values which have to be included in the retunrned value
   * @return List - containing map of display names for values.
   * @throws SQLException
   *           the SQL exception
   */
  public List getLookupValueList(String fieldName, String discriminator, String[] filterKeys)
      throws SQLException {
    Map map = getLookupValues(fieldName, discriminator, filterKeys);
    if (null != map) {
      return new ArrayList(map.values());
    }
    return null;
  }

  public List getLookupValueList(String fieldName, String[] filterKeys) throws SQLException {
    return getLookupValueList(fieldName, DEFAULT_DISCRIMINATOR_KEY, filterKeys);
  }

  /**
   * Method to add a field descriptor.
   * 
   * @param fieldDisplayNames
   *          - a map containing field names and their display names as a map Useful for tables
   *          which have a simple set of fields without any value mapping. The fields added through
   *          this method are searchable.
   */
  public void addFields(Map<String, String> fieldDisplayNames) {
    for (Map.Entry<String, String> entry : fieldDisplayNames.entrySet()) {
      String key = entry.getKey();
      String displayName = entry.getValue();
      addField(key, displayName, true);
    }
  }

  /**
   * Method to add a field descriptor, one field at a time.
   *
   * @param fieldName
   *          - name of the field
   * @param displayName
   *          - display name of the field Same as addFields(Map&lt;String, String&gt;), but allows
   *          addition of fields one at a time, without the need to construct a map. The field is
   *          searchable by default.
   */
  public void addField(String fieldName, String displayName) {
    addField(fieldName, displayName, true);
  }

  /**
   * Method to add a field descriptor, specifying whether the field is searchable or not.
   * 
   * @param fieldName
   *          - name of the field
   * @param displayName
   *          - display name of the field
   * @param searchable
   *          - whether the field should be presented as search criteria in audit log search
   */
  public void addField(String fieldName, String displayName, boolean searchable) {
    addField(fieldName, displayName, searchable, null);
  }

  public void addField(String fieldName, String displayName, boolean searchable,
      boolean displayAsKey) {
    addField(fieldName, displayName, searchable, null, displayAsKey);
  }

  public void addField(String fieldName, String displayName, boolean searchable,
      boolean displayAsKey, String fieldType) {
    addField(fieldName, displayName, searchable, null, displayAsKey, fieldType);
  }

  /**
   * Method to add a field descriptor, specifying whether the field is searchable or not.
   * 
   * @param fieldName
   *          - name of the field
   * @param displayName
   *          - display name of the field
   * @param searchable
   *          - whether the field should be presented as search criteria in audit log search
   */
  public void addField(String fieldName, String displayName, boolean searchable,
      String lookupDiscriminator) {
    addFieldDesc(fieldName, new FieldDesc(fieldName, displayName, searchable, lookupDiscriminator));
  }

  public void addField(String fieldName, String displayName, boolean searchable,
      String lookupDiscriminator, boolean displayAsKey) {
    addFieldDesc(fieldName,
        new FieldDesc(fieldName, displayName, searchable, lookupDiscriminator, displayAsKey));
  }

  public void addField(String fieldName, String displayName, boolean searchable,
      String lookupDiscriminator, boolean displayAsKey, String fieldType) {
    addFieldDesc(fieldName, new FieldDesc(fieldName, displayName, searchable, lookupDiscriminator,
        displayAsKey, fieldType));
  }

  /**
   * Conveninece method to add values after the field has been added using a static map.
   *
   * @param fieldName
   *          the field name
   * @param valueMap
   *          the value map
   */
  public void addFieldValue(String fieldName, Map valueMap) {
    ValueDesc valueDesc = new ValueDesc(fieldName, valueMap);
    addFieldValue(fieldName, DEFAULT_DISCRIMINATOR_KEY, valueDesc);
  }

  /**
   * Conveninece method to add values after the field has been added using a table lookup.
   *
   * @param fieldName
   *          the field name
   * @param lookupTable
   *          the lookup table
   * @param lookupField
   *          the lookup field
   * @param displayField
   *          the display field
   */

  public void addFieldValue(String fieldName, String lookupTable, String lookupField,
      String displayField) {
    addFieldValue(fieldName, DEFAULT_DISCRIMINATOR_KEY, lookupTable, lookupField, displayField);
  }

  public void addFieldValue(String fieldName, String discriminator, String lookupTable,
      String lookupField, String displayField) {
    ValueDesc valueDesc = new ValueDesc(fieldName, lookupTable, lookupField, displayField);
    addFieldValue(fieldName, discriminator, valueDesc);
  }

  private void addFieldValue(String fieldName, String discriminator, ValueDesc valueDesc) {
    addValueDesc(fieldName, discriminator, valueDesc);
  }

  private Map getLookupValues(String fieldName, String discriminator, String[] filterKeys) {
    FieldDesc fd = fieldNamesMap.get(fieldName);
    return fd.getValueMap(filterKeys);
  }

  private List getFieldNames(boolean searchableOnly) {
    List<Map<String, String>> fieldList = new ArrayList();
    List<Map.Entry<String, FieldDesc>> mapEntries = new ArrayList<>(fieldNamesMap.entrySet());
    Collections.sort(mapEntries, new Comparator<Map.Entry<String, FieldDesc>>() {
      public int compare(Map.Entry<String, FieldDesc> e1, Map.Entry<String, FieldDesc> e2) {
        if ((e1 == null && e2 == null)
            || (e1 != null && e2 != null && e1.getValue() == null && e2.getValue() == null)) {
          return 0;
        }

        if (e1 == null || e1.getValue() == null) {
          return -1;
        }

        if (e2 == null || e2.getValue() == null) {
          return 1;
        }

        return e1.getValue().displayName.compareTo(e2.getValue().displayName);
      }
    });
    for (Map.Entry<String, FieldDesc> entry : mapEntries) {
      Map<String, String> fieldEntry = new HashMap<>();
      if (!searchableOnly || entry.getValue().searchable) {
        fieldEntry.put("name", entry.getKey());
        fieldEntry.put("display_name", entry.getValue().displayName);
        fieldList.add(fieldEntry);
      }
    }
    return fieldList;
  }

  private void addFieldDesc(String fieldName, FieldDesc fieldDesc) {
    fieldNamesMap.put(fieldName, fieldDesc);
  }

  private void addValueDesc(String fieldName, String discriminator, ValueDesc valueDesc) {
    if (!fieldNamesMap.containsKey(fieldName)) {
      addField(fieldName, fieldName);
    }
    FieldDesc fieldDesc = fieldNamesMap.get(fieldName);
    fieldDesc.addValueDesc(discriminator, valueDesc);
  }

  private Map listMapToMapMap(List<Map> listMap, String column) {
    Map map = new LinkedHashMap();
    if (null != listMap && !listMap.isEmpty()) {
      for (Map listEntry : listMap) {
        if (null != listEntry.get(column)) {
          map.put(listEntry.get(column), listEntry);
        }
      }
    }
    return map;
  }

  public class ValueDesc {
    private String fieldName;
    private String type;
    private Map<String, String> valueMap = new LinkedHashMap<>();
    private String lookupTable;
    private String lookupField;
    private String displayField;

    /**
     * Instantiates a new value desc.
     *
     * @param fieldName
     *          the field name
     * @param valueMap
     *          the value map
     */
    public ValueDesc(String fieldName, Map valueMap) {
      this.fieldName = fieldName;
      this.type = TYPE_STATIC_MAP;
      this.valueMap.clear();
      this.valueMap.putAll(valueMap);
    }

    /**
     * Instantiates a new value desc.
     *
     * @param fieldName
     *          the field name
     * @param lookupTable
     *          the lookup table
     * @param lookupField
     *          the lookup field
     * @param displayField
     *          the display field
     */
    public ValueDesc(String fieldName, String lookupTable, String lookupField,
        String displayField) {
      this.fieldName = fieldName;
      this.type = TYPE_TABLE_LOOKUP;
      this.lookupTable = lookupTable;
      this.lookupField = lookupField;
      this.displayField = displayField;
    }

    /**
     * Gets the query string.
     *
     * @param filterKeys
     *          the filter keys
     * @return the query string
     */
    public String getQueryString(String[] filterKeys) {
      StringBuilder sb = new StringBuilder();
      if (null == lookupField || lookupField.isEmpty() || null == displayField
          || displayField.isEmpty() || null == lookupTable || lookupTable.isEmpty()) {
        return null;
      }

      sb.append("SELECT ").append(DataBaseUtil.quoteIdent(lookupField)).append(" as id, ");
      sb.append(DataBaseUtil.quoteIdent(displayField)).append(" as value FROM ")
          .append(lookupTable);
      sb.append(" ORDER by value ");
      return sb.toString();
    }

    /**
     * Gets the value list.
     *
     * @param filterKeys
     *          the filter keys
     * @return the value list
     */
    public List<Map> getValueList(String[] filterKeys) {
      List<Map> valueList = new ArrayList<>();
      if (TYPE_STATIC_MAP.equals(this.type)) {
        Map<String, String> lookupMap = this.valueMap;
        for (Map.Entry<String, String> entry : lookupMap.entrySet()) {
          String key = entry.getKey();
          Map valueMap = new LinkedHashMap();
          valueMap.put("ID", key);
          valueMap.put("VALUE", entry.getValue());
          valueList.add(valueMap);
        }
      } else {
        String query = getQueryString(filterKeys);
        if (null != query && !query.isEmpty()) {
          valueList = DataBaseUtil.queryToArrayList(query);
        }
      }
      return valueList;
    }

    /**
     * Gets the value map.
     *
     * @param filterKeys
     *          the filter keys
     * @return the value map
     */
    public Map getValueMap(String[] filterKeys) {
      Map valueMap = null;
      List<Map> valueList = getValueList(filterKeys);
      return listMapToMapMap(valueList, "ID");
    }

  }

  public class FieldDesc {
    private String fieldName;
    private String displayName;
    private boolean searchable;
    private String lookupDiscriminator;
    private Map<String, ValueDesc> valueDescMap = new LinkedHashMap<>();
    private Map<String, Map<String, String>> valueMap;
    private boolean displayAsKey;
    private String fieldType = "Text";

    /**
     * Instantiates a new field desc.
     *
     * @param fieldName
     *          the field name
     * @param displayName
     *          the display name
     * @param searchable
     *          the searchable
     * @param lookupDiscriminator
     *          the lookup discriminator
     */
    public FieldDesc(String fieldName, String displayName, boolean searchable,
        String lookupDiscriminator) {
      this(fieldName, displayName, searchable, lookupDiscriminator, false, "Text");
    }

    public FieldDesc(String fieldName, String displayName, boolean searchable,
        String lookupDiscriminator, boolean displayAsKey) {
      this(fieldName, displayName, searchable, lookupDiscriminator, displayAsKey, "Text");
    }

    /**
     * Instantiates a new field desc.
     *
     * @param fieldName
     *          the field name
     * @param displayName
     *          the display name
     * @param searchable
     *          the searchable
     * @param lookupDiscriminator
     *          the lookup discriminator
     * @param displayAsKey
     *          the display as key
     * @param fieldType
     *          the field type
     */
    public FieldDesc(String fieldName, String displayName, boolean searchable,
        String lookupDiscriminator, boolean displayAsKey, String fieldType) {
      this.fieldName = fieldName;
      this.displayName = displayName;
      this.searchable = searchable;
      this.lookupDiscriminator = lookupDiscriminator;
      this.displayAsKey = displayAsKey;
      this.fieldType = fieldType;
    }

    public String getFieldType() {
      return this.fieldType;
    }

    public void setFieldType(String fieldType) {
      this.fieldType = fieldType;
    }

    public boolean getDisplayAsKey() {
      return displayAsKey;
    }

    public String getFieldName() {
      return fieldName;
    }

    public boolean getSearchable() {
      return searchable;
    }

    public String getLookupDiscriminator() {
      return lookupDiscriminator;
    }

    public String getDisplayName() {
      return (null == displayName || displayName.trim().length() == 0) ? fieldName : displayName;
    }

    public Map getValueMap() {
      return getValueMap(null);
    }

    /**
     * Gets the value map.
     *
     * @param filterKeys
     *          the filter keys
     * @return the value map
     */
    public Map getValueMap(String[] filterKeys) {
      if (null == valueMap) {
        if (lookupDiscriminator != null || null != valueDescMap.get(DEFAULT_DISCRIMINATOR_KEY)) {
          valueMap = new LinkedHashMap<>();
        }
        if (null == lookupDiscriminator) {
          ValueDesc desc = valueDescMap.get(DEFAULT_DISCRIMINATOR_KEY);
          if (null != desc) {
            Map descMap = desc.getValueMap(filterKeys);
            valueMap.putAll(descMap);
          }
        } else {
          for (Map.Entry<String, ValueDesc> entry : valueDescMap.entrySet()) {
            String discriminatorKey = entry.getKey();
            ValueDesc desc = entry.getValue();
            if (null != desc) {
              Map descMap = desc.getValueMap(filterKeys);
              valueMap.put(discriminatorKey, descMap);
            }
          }
        }
      }
      return valueMap;
    }

    public void addValueDesc(String discriminator, ValueDesc valueDesc) {
      this.valueDescMap.put(discriminator, valueDesc);
    }
  }
}
