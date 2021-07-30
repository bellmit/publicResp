package com.insta.hms.mdm.bulk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class CSVBulkDataEntity to be used as a data access object for CSV Bulk data operations.
 * 
 * @author tanmay.k
 */
public class CsVBulkDataEntity {

  /** The keys. */
  private List<String> keys;

  /** The fields. */
  private List<String> fields;

  /** The filters. */
  private List<String> filters;

  /** The all fields. */
  private List<String> allFields = new ArrayList<String>();

  /** The masters. */
  private List<BulkDataMasterEntity> masters;

  /** The referenced masters with field as key map. */
  private Map<String, BulkDataMasterEntity> referencedMastersWithFieldAsKeyMap = 
      new HashMap<String, BulkDataMasterEntity>();

  /** The referenced masters with name as key map. */
  private Map<String, BulkDataMasterEntity> referencedMastersWithNameAsKeyMap = 
      new HashMap<String, BulkDataMasterEntity>();

  /** The master filter check. */
  private Map<String, Object> masterFilterCheck = new HashMap<String, Object>();

  /** The master lookup map. */
  private Map<String, BulkDataLookupEntity> masterLookupMap = 
      new HashMap<String, BulkDataLookupEntity>();

  /** The alias to name. */
  private Map<String, String> aliasToName = new HashMap<String, String>();

  /** The name to alias. */
  private Map<String, String> nameToAlias = new HashMap<String, String>();

  /** The type map. */
  private Map<String, Class<?>> typeMap = new HashMap<String, Class<?>>();

  /**
   * Instantiates a new CSV bulk data entity.
   *
   * @param keys
   *          the keys
   * @param fields
   *          the fields
   * @param filters
   *          the filters
   * @param masters
   *          the masters
   */
  public CsVBulkDataEntity(String[] keys, String[] fields, String[] filters,
      BulkDataMasterEntity... masters) {
    super();
    this.keys = convertToList(keys);
    this.fields = convertToList(fields);
    this.filters = convertToList(filters);
    this.masters = convertToList(masters);

    if (null != fields) {
      allFields.addAll(this.keys);
      allFields.addAll(this.fields);
    }

    for (BulkDataMasterEntity master : this.masters) {
      referencedMastersWithFieldAsKeyMap.put(master.getDisplayedField(), master);
      referencedMastersWithNameAsKeyMap.put(master.getReferencedTableNameField(), master);
    }
  }

  /**
   * Gets the real field name.
   *
   * @param fieldName
   *          the field name
   * @return the real field name
   */
  public String getRealFieldName(String fieldName) {
    String realName = aliasToName.get(fieldName);
    if (null != realName) {
      return realName;
    }

    BulkDataMasterEntity master = referencedMastersWithNameAsKeyMap.get(fieldName);
    if (null != master) {
      return master.getDisplayedField();
    }

    return fieldName;
  }

  /**
   * Enforce type.
   *
   * @param name
   *          the name
   * @param typeClass
   *          the type class
   */
  public void enforceType(String name, Class typeClass) {
    typeMap.put(name, typeClass);
  }

  /**
   * Sets the alias.
   *
   * @param name
   *          the name
   * @param alias
   *          the alias
   */
  public void setAlias(String name, String alias) {
    aliasToName.put(alias, name);
    nameToAlias.put(name, alias);
  }

  /**
   * Sets the aliases.
   *
   * @param aliases
   *          the aliases
   */
  public void setAliases(Map<String, String> aliases) {
    for (Map.Entry<String, String> entry : aliases.entrySet()) {
      String name = entry.getKey();
      String alias = entry.getValue();

      aliasToName.put(alias, name);
      nameToAlias.put(name, alias);
    }
  }

  /**
   * Sets the master filter check.
   *
   * @param masterFilterCheck
   *          the master filter check
   */
  public void setMasterFilterCheck(Map<String, Object> masterFilterCheck) {
    this.masterFilterCheck = masterFilterCheck;
  }

  /**
   * Gets the keys.
   *
   * @return the keys
   */
  public List<String> getKeys() {
    return keys;
  }

  /**
   * Gets the fields.
   *
   * @return the fields
   */
  public List<String> getFields() {
    return fields;
  }

  /**
   * Gets the filters.
   *
   * @return the filters
   */
  public List<String> getFilters() {
    return filters;
  }

  /**
   * Gets the all fields.
   *
   * @return the all fields
   */
  public List<String> getAllFields() {
    return allFields;
  }

  /**
   * Gets the masters.
   *
   * @return the masters
   */
  public List<BulkDataMasterEntity> getMasters() {
    return masters;
  }

  /**
   * Gets the referenced masters with field as key map.
   *
   * @return the referenced masters with field as key map
   */
  public Map<String, BulkDataMasterEntity> getReferencedMastersWithFieldAsKeyMap() {
    return referencedMastersWithFieldAsKeyMap;
  }

  /**
   * Gets the referenced masters with name as key map.
   *
   * @return the referenced masters with name as key map
   */
  public Map<String, BulkDataMasterEntity> getReferencedMastersWithNameAsKeyMap() {
    return referencedMastersWithNameAsKeyMap;
  }

  /**
   * Gets the master filter check.
   *
   * @return the master filter check
   */
  public Map<String, Object> getMasterFilterCheck() {
    return masterFilterCheck;
  }

  /**
   * Gets the master lookup map.
   *
   * @return the master lookup map
   */
  public Map<String, BulkDataLookupEntity> getMasterLookupMap() {
    return masterLookupMap;
  }

  /**
   * Gets the alias to name.
   *
   * @return the alias to name
   */
  public Map<String, String> getAliasToName() {
    return aliasToName;
  }

  /**
   * Gets the name to alias.
   *
   * @return the name to alias
   */
  public Map<String, String> getNameToAlias() {
    return nameToAlias;
  }

  /**
   * Gets the type map.
   *
   * @return the type map
   */
  public Map<String, Class<?>> getTypeMap() {
    return typeMap;
  }

  /**
   * Convert to list.
   *
   * @param <T>
   *          the generic type
   * @param array
   *          the array
   * @return the list
   */
  public <T> List<T> convertToList(T[] array) {
    return null == array ? new ArrayList<T>() : Arrays.asList(array);
  }

}
