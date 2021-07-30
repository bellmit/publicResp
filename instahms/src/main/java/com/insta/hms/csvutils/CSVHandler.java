package com.insta.hms.csvutils;

import au.com.bytecode.opencsv.CSVReader;
import com.insta.hms.common.AutoIdGenerator;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.exception.InvalidFileFormatException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class CSVHandler.
 * 
 * @author tanmay.k
 */
public class CSVHandler {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(CSVHandler.class);

  /** The table name. */
  private String tableName;

  /** The sequence name. */
  private String sequenceName;

  /** The auto increment id name. */
  private String autoIncrementIdName;

  /** The id string value. */
  private boolean isIdString;

  /** The keys. */
  private String[] keys;

  /** The fields. */
  private String[] fields;

  /** The filters. */
  private String[] filters;

  /** The all fields. */
  private List<String> allFields = new ArrayList<String>();

  /** The masters. */
  private String[][] masters;

  /** The master field map. */
  private HashMap<String, Master> masterFieldMap = new HashMap<>();

  /** The master name field map. */
  private HashMap<String, Master> masterNameFieldMap = new HashMap<>();

  /** The master filter check. */
  private HashMap<String, Object> masterFilterCheck = new HashMap<>();

  /** The master lookup map. */
  private HashMap<String, Lookup> masterLookupMap = new HashMap<>();

  /** The alias to name. */
  private HashMap<String, String> aliasToName = new HashMap<>();

  /** The name to alias. */
  private HashMap<String, String> nameToAlias = new HashMap<>();

  /** The type map. */
  private HashMap<String, Class> typeMap = new HashMap<>();

  /**
   * Instantiates a new CSV handler.
   *
   * @param tableName
   *          the table name
   * @param keys
   *          the keys
   * @param fields
   *          the fields
   * @param masters
   *          the masters
   * @param filters
   *          the filters
   */
  public CSVHandler(String tableName, String[] keys, String[] fields,
      String[][] masters, String[] filters) {
    this.tableName = tableName;
    this.keys = keys;
    this.fields = fields;
    this.filters = filters;
    this.masters = masters;

    if (fields != null) {
      allFields.addAll(Arrays.asList(keys));
      allFields.addAll(Arrays.asList(fields));
    }

    for (String[] master : masters) {
      Master masterObject = new Master(master[0], master[1], master[2],
          master[3]);
      masterFieldMap.put(masterObject.field, masterObject);
      masterNameFieldMap.put(masterObject.nameField, masterObject);
    }
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
   * Sets the master data.
   *
   * @param data
   *          the new master data
   */
  public void setMasterData(Object[][] data) {
    for (Object[] values : data) {
      Lookup lookup = new Lookup((String) values[0], (String) values[1],
          (Map) values[2], (String) values[3]);
      masterLookupMap.put(lookup.field, lookup);
    }
  }

  /**
   * Gets the sequence name.
   *
   * @return the sequence name
   */
  public String getSequenceName() {
    return this.sequenceName;
  }

  /**
   * Sets the sequence name.
   *
   * @param sequenceName
   *          the new sequence name
   */
  public void setSequenceName(String sequenceName) {
    this.sequenceName = sequenceName;
  }

  /**
   * Gets the auto increment id name.
   *
   * @return the auto increment id name
   */
  public String getAutoIncrementIdName() {
    return this.autoIncrementIdName;
  }

  /**
   * Sets the auto increment id name.
   *
   * @param autoIncrementIdName
   *          the new auto increment id name
   */
  public void setAutoIncrementIdName(String autoIncrementIdName) {
    this.autoIncrementIdName = autoIncrementIdName;
  }

  /**
   * Checks if is id string.
   *
   * @return true, if is id string
   */
  public boolean isIdString() {
    return isIdString;
  }

  /**
   * Sets the id string.
   *
   * @param isIdString the new id string
   */
  public void setIdString(boolean isIdString) {
    this.isIdString = isIdString;
  }

  /**
   * Gets the real field name.
   *
   * @param fieldName
   *          the field name
   * @return the real field name
   */
  /*
   * Fetch the real field name (as in the main table) given the spreadsheet
   * name. This could be: (a) aliased: the spreadsheet name is an alias for the
   * real field in the DB (b) master: the spreadsheet name is the field name of
   * the "name" field in the master When the two are combined (eg, org_id ->
   * (master) org_name -> (alias) rate_plan, we only use the alias (org_id ->
   * rate_plan)
   */
  private String getRealFieldName(String fieldName) {
    String realName = aliasToName.get(fieldName);
    if (realName != null) {
      return realName;
    }

    Master ms = masterNameFieldMap.get(fieldName);
    if (ms != null) {
      return ms.field;
    }

    return fieldName;
  }

  /**
   * Gets the master filter check.
   *
   * @return the master filter check
   */
  public HashMap<String, Object> getMasterFilterCheck() {
    return masterFilterCheck;
  }

  /**
   * Sets the master filter check.
   *
   * @param masterFilterCheck
   *          the master filter check
   */
  public void setMasterFilterCheck(HashMap<String, Object> masterFilterCheck) {
    this.masterFilterCheck = masterFilterCheck;
  }

  /**
   * Adds the warning.
   *
   * @param warnings
   *          the warnings
   * @param line
   *          the line
   * @param msg
   *          the msg
   */
  private void addWarning(StringBuilder warnings, int line, String msg) {
    if (line > 0) {
      warnings.append("Line ").append(line).append(": ");
    } else {
      warnings.append("Error in header: ");
    }
    warnings.append(msg).append("<br>");
    logger.warn("Line " + line + ": " + msg);
  }

  /**
   * Gets the master data.
   *
   * @return the master data
   */
  private Map<String, Map<String, String>> getMasterData() {
    Map<String, Map<String, String>> masterData = new HashMap<String, Map<String, String>>();
    for (Master master : masterFieldMap.values()) {
      List<BasicDynaBean> beans = new GenericRepository(master.referencedTable)
          .listAll(
              Arrays.asList(new String[] { master.idField, master.nameField }));
      Map<String, String> beanMap = new HashMap<>();
      for (BasicDynaBean bean : beans) {
        String name = (String) bean.get(master.nameField);
        if (name != null) {
          beanMap.put(name.toLowerCase(), bean.get(master.idField).toString());
        }
      }
      masterData.put(master.field, beanMap);
    }
    return masterData;
  }

  /**
   * Import table.
   *
   * @param streamReader
   *          the stream reader
   * @param feedback
   *          the feedback
   * @return the string
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public String importTable(InputStreamReader streamReader,
      StringBuilder feedback) {

    CSVReader csvReader = new CSVReader(streamReader);
    String[] headers;
    try {
      headers = csvReader.readNext();
    } catch (IOException e1) {
      throw new InvalidFileFormatException(e1);
    }
    StringBuilder warnings = new StringBuilder();
    Integer lineNumber = 1;
    Integer countOfLinesWithWarnings = 0;
    Integer countOfWarningsinHeader = 0;
    Integer insertionCount = 0;
    Integer updationCount = 0;
    boolean[] columnsToBeIgnored = new boolean[headers.length];

    /*
     * TODO - Validation being done here. Should be ported to generic validator
     */

    if (headers.length < 1) {
      return "exception.csv.missing.headers";
    }

    if (!headers[0].matches("\\p{Print}*")) {
      return "exception.csv.non.printable.characters";
    }

    if (headers.length == 1) {
      return "exception.csv.non.comma.seperators";
    }

    GenericRepository repository = new GenericRepository(this.tableName);
    BasicDynaBean bean = null;

    for (Integer index = 0; index < headers.length; index++) {
      String fieldName = getRealFieldName(headers[index].trim());
      if (!allFields.contains(fieldName)) {
        addWarning(warnings, 0,
            "Unknown property in header (ignoring data in column): "
                + headers[index]);
        columnsToBeIgnored[index] = true;
        countOfWarningsinHeader++;
      } else {
        columnsToBeIgnored[index] = false;
      }

      headers[index] = fieldName;
    }

    Map<String, Map<String, String>> masterData = getMasterData();
    String[] line = null;

    try {
      while ((line = csvReader.readNext()) != null) {
        lineNumber++;
        logger.debug("Processing line: " + lineNumber);
        bean = repository.getBean();
        boolean hasWarnings = false;
        Integer countOfNonEmptyColumns = 0;

        for (Integer index = 0; index < headers.length
            && index < line.length; index++) {
          if (columnsToBeIgnored[index]) {
            continue;
          }

          String fieldName = headers[index];
          logger.debug("Processing field: " + fieldName);
          String value = line[index].trim();

          if ((value != null) && !value.isEmpty()) {
            Map<String, String> masterValuesMap = masterData.get(fieldName);
            if (masterValuesMap != null) {
              String valueId = masterValuesMap.get(value.toLowerCase());
              if (valueId == null) {
                addWarning(warnings, lineNumber, "No master value found for "
                    + value + " (" + fieldName + ")");
                hasWarnings = true;
                continue;
              }

              String filterCheckName = (String) masterFilterCheck
                  .get(fieldName);
              if (filterCheckName != null && !filterCheckName.equals(value)) {
                addWarning(warnings, lineNumber, "supported value is " + value
                    + " for " + " (" + fieldName + ")");
                hasWarnings = true;
                continue;
              }
              value = valueId;
            }

            DynaProperty property = null;
            try {
              property = bean.getDynaClass().getDynaProperty(fieldName);
              Class enforcedType = typeMap.get(fieldName);
              if (enforcedType != null) {
                if (ConvertUtils.convert(value, enforcedType) == null) {
                  addWarning(warnings, lineNumber,
                      "Conversion error: " + value + " could not converted to "
                          + (enforcedType == BigDecimal.class ? " Number "
                              : enforcedType.getSimpleName())
                          + " for " + fieldName);
                  hasWarnings = true;
                  continue;
                }
              }
              logger.debug("Property type is " + property.getType()
                  + ", value: " + value);
              bean.set(fieldName,
                  ConvertUtils.convert(value, property.getType()));
            } catch (ConversionException exp) {
              addWarning(warnings, lineNumber,
                  "Conversion error: " + value + " could not be converted to "
                      + property.getType() + " for " + fieldName);
              hasWarnings = true;
              logger.error("Conversion error: ", exp);
            }
            countOfNonEmptyColumns++;
          } else {
            bean.set(fieldName, null);
          }
        }

        if (masterLookupMap != null && !masterLookupMap.isEmpty()) {
          for (DynaProperty property : bean.getDynaClass()
              .getDynaProperties()) {
            if (masterLookupMap.get(property.getName()) != null) {
              Lookup lookup = masterLookupMap.get(property.getName());
              Map lookupMap = lookup.map;
              List list = (List) lookupMap
                  .get(bean.get(lookup.referencedField));
              if (list == null || !list.contains(bean.get(lookup.field))) {
                hasWarnings = true;
                addWarning(warnings, lineNumber, lookup.errorMessage);
              }
            }
          }
        }

        if (hasWarnings) {
          countOfLinesWithWarnings++;
          continue;
        }

        if (countOfNonEmptyColumns == 0) {
          continue;
        }

        HashMap<String, Object> keyValueMap = new HashMap<>();
        boolean allKeysGiven = true;
        for (String key : keys) {
          keyValueMap.put(key, bean.get(key));
          if (bean.get(key) == null) {
            allKeysGiven = false;
          }
        }

        try {
          if (keys.length == 1) {
            // single key: update if key given, else insert.
            if (allKeysGiven) {
              int rows = repository.update(bean, keyValueMap);
              if (rows != 0) {
                updationCount++;
              } else {
                repository.insert(bean);
                insertionCount++;
              }
            } else {
              if (sequenceName != null && autoIncrementIdName != null) {
                bean.set(keys[0], AutoIdGenerator.getSequenceId(sequenceName,
                    autoIncrementIdName));
              } else if (sequenceName != null) {
                // idValAsString is set to true, when we have an
                // id(primary key) coloumn of the table is character
                // varying type in the table design
                bean.set(keys[0],
                    isIdString
                        ? ((Integer) DatabaseHelper
                            .getNextSequence(sequenceName)).toString()
                        : DatabaseHelper.getNextSequence(sequenceName));
              } else if (autoIncrementIdName != null) {
                bean.set(keys[0], AutoIdGenerator.getNewId(keys[0], tableName,
                    autoIncrementIdName));
              }
              repository.insert(bean);
              insertionCount++;
            }

          } else {
            if (allKeysGiven) {
              // try an update. If it fails, do an insert.
              int rows = repository.update(bean, keyValueMap);
              if (rows != 0) {
                updationCount++;
              } else {
                repository.insert(bean);
                insertionCount++;
              }
            } else {
              // we don't allow inserts for multiple keys by
              // autogeneration
              addWarning(warnings, lineNumber, "Key fields not given");
              countOfLinesWithWarnings++;
            }
          }
        } catch (DuplicateKeyException exp) {
          addWarning(warnings, lineNumber, "Duplicate record found");
          logger.error("Duplicate record found.");
          countOfLinesWithWarnings++;
        } catch (DataAccessException exp) {
          addWarning(warnings, lineNumber,
              "Unknown error: " + exp.getMostSpecificCause().getMessage());
          logger.error("Error uploading csv line", exp.getCause());
          countOfLinesWithWarnings++;
        }
      }
    } catch (IOException exp) {
      throw new InvalidFileFormatException(exp);
    }

    feedback.append("Processed lines: ").append(lineNumber - 1).append("<br>");
    if (insertionCount > 0) {
      feedback.append("New rows inserted: ").append(insertionCount)
          .append("<br>");
    }
    feedback.append("Existing records updated: ").append(updationCount)
        .append("<br>");

    if (countOfLinesWithWarnings > 0 || countOfWarningsinHeader > 0) {
      if (countOfLinesWithWarnings > 0) {
        feedback.append("Lines with errors: ").append(countOfLinesWithWarnings);
      }
      if (countOfWarningsinHeader > 0) {
        feedback.append("Headers with errors: ")
            .append(countOfWarningsinHeader);
      }
      feedback.append("<br>");
      feedback.append("<hr>");
      feedback.append(warnings);
    }
    logger.info("{}", feedback);
    return null;

  }

  /**
   * Export table.
   *
   * @param fileName the file name
   * @return the list
   */
  public List<BasicDynaBean> exportTable(String fileName) {
    String exportQuery = getExportQuery();
    return DatabaseHelper.queryToDynaList(exportQuery);
  }

  /**
   * Gets the export query.
   *
   * @return the export query
   */
  public String getExportQuery() {
    StringBuilder query = new StringBuilder();
    query.append("SELECT");
    if (fields == null) {
      query.append(" *");
    } else {
      boolean first = true;
      for (String f : allFields) {
        query.append(first ? " " : ", ");
        Master ms = masterFieldMap.get(f);
        if (ms != null) {
          query.append(ms.referencedTable).append(".").append(ms.nameField);
        } else {
          query.append(tableName).append(".").append(f);
        }
        String alias = nameToAlias.get(f);
        if (alias != null) {
          query.append(" AS ").append(alias);
        }
        first = false;
      }
    }
    query.append("\nFROM ").append(tableName);
    for (Master ms : masterFieldMap.values()) {
      query.append("\n LEFT JOIN ").append(ms.referencedTable).append(" ON ")
          .append(tableName).append(".").append(ms.field).append(" = ")
          .append(ms.referencedTable).append(".").append(ms.idField);
    }

    if (filters != null) {
      boolean first = true;
      for (String filter : filters) {
        query.append(first ? "\nWHERE " : " AND ");
        query.append(filter);
        first = false;
      }
    }

    // order by the primary keys in the given order.
    query.append("\nORDER BY");
    boolean first = true;
    for (String k : keys) {
      query.append(first ? " " : ", ");
      query.append(tableName).append(".").append(k);
      first = false;
    }
    return query.toString();
  }

  /**
   * The Class Master.
   */
  public static class Master {

    /** The field. */
    public String field;

    /** The referenced table. */
    public String referencedTable;

    /** The id field. */
    public String idField;

    /** The name field. */
    public String nameField;

    /**
     * Instantiates a new master.
     *
     * @param field
     *          the field
     * @param referencedTable
     *          the referenced table
     * @param idField
     *          the id field
     * @param nameField
     *          the name field
     */
    public Master(String field, String referencedTable, String idField,
        String nameField) {
      this.field = field;
      this.referencedTable = referencedTable;
      this.idField = idField;
      this.nameField = nameField;
    }
  }

  /**
   * The Class Lookup.
   */
  public static class Lookup {

    /** The field. */
    public String field;

    /** The referenced field. */
    public String referencedField;

    /** The error message. */
    public String errorMessage;

    /** The map. */
    public Map map;

    /**
     * Instantiates a new lookup.
     *
     * @param field
     *          the field
     * @param referencedField
     *          the referenced field
     * @param map
     *          the map
     * @param errorMessage
     *          the error message
     */
    public Lookup(String field, String referencedField, Map map,
        String errorMessage) {
      this.field = field;
      this.referencedField = referencedField;
      this.errorMessage = errorMessage;
      this.map = map;
    }

  }

}
