package com.insta.hms.csvutils;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * The Class TableDataHandler.
 */
public class TableDataHandler {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(TableDataHandler.class);

  /** The table name. */
  private String tableName;
  
  /** The sequence name. */
  private String sequenceName;
  
  /** The auto incr name. */
  private String autoIncrName;
  
  /** The id val as string. */
  private boolean idValAsString;
  
  /** The keys. */
  private String[] keys;
  
  /** The fields. */
  private String[] fields;
  
  /** The all fields. */
  private List<String> allFields = new ArrayList<String>();
  
  /** The masters. */
  private String[][] masters;
  
  /** The filters. */
  private String[] filters;
  
  /** The master field map. */
  private HashMap<String, Master> masterFieldMap = new HashMap<String, Master>();
  
  /** The master name field map. */
  private HashMap<String, Master> masterNameFieldMap = new HashMap<String, Master>();
  
  /** The master filter check. */
  private HashMap<String, Object> masterFilterCheck = new HashMap<>();
  
  /** The master lookup map. */
  private HashMap<String, Lookup> masterLookupMap = new HashMap<>();

  /** The alias to name. */
  private HashMap<String, String> aliasToName = new HashMap<>();
  
  /** The name to alias. */
  private HashMap<String, String> nameToAlias = new HashMap<>();
  
  /** The type map. */
  private HashMap<String, Class> typeMap = new HashMap<String, Class>();

  /**
   * The Class Master.
   */
  public static class Master {
    
    /** The field. */
    public String field;
    
    /** The ref table. */
    public String refTable;
    
    /** The id field. */
    public String idField;
    
    /** The name field. */
    public String nameField;

    /**
     * Instantiates a new master.
     *
     * @param field the field
     * @param refTable the ref table
     * @param idField the id field
     * @param nameField the name field
     */
    public Master(String field, String refTable, String idField,
        String nameField) {
      this.field = field;
      this.refTable = refTable;
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
    
    /** The id field. */
    public String idField;
    
    /** The error msg. */
    public String errorMsg;
    
    /** The map. */
    public Map map;

    /**
     * Instantiates a new lookup.
     *
     * @param field the field
     * @param idField the id field
     * @param map the map
     * @param errorMsg the error msg
     */
    public Lookup(String field, String idField, Map map, String errorMsg) {
      this.field = field;
      this.idField = idField;
      this.map = map;
      this.errorMsg = errorMsg;
    }
  }

  /**
   * Instantiates a new table data handler.
   *
   * @param tableName the table name
   * @param keys the keys
   * @param fields the fields
   * @param masters the masters
   * @param filters the filters
   */
  public TableDataHandler(String tableName, String[] keys, String[] fields,
      String[][] masters, String[] filters) {
    this.tableName = tableName;
    this.keys = keys;
    this.fields = fields;
    this.masters = masters;
    this.filters = filters;
    if (fields != null) {
      allFields.addAll(Arrays.asList(keys));
      allFields.addAll(Arrays.asList(fields));
    }
    for (String[] m : masters) {
      Master ms = new Master(m[0], m[1], m[2], m[3]);
      masterFieldMap.put(ms.field, ms);
      masterNameFieldMap.put(ms.nameField, ms);
    }
  }

  /**
   * Sets the alias.
   *
   * @param name the name
   * @param alias the alias
   */
  public void setAlias(String name, String alias) {
    aliasToName.put(alias, name);
    nameToAlias.put(name, alias);
  }

  /**
   * Enforce type.
   *
   * @param name the name
   * @param typeClass the type class
   */
  public void enforceType(String name, Class typeClass) {
    typeMap.put(name, typeClass);
  }

  /**
   * Sets the master data for corr value.
   *
   * @param data the new master data for corr value
   */
  public void setMasterDataForCorrValue(Object[][] data) {
    for (Object[] d : data) {
      Lookup lookUp = new Lookup((String) d[0], (String) d[1], (Map) d[2],
          (String) d[3]);
      masterLookupMap.put(lookUp.field, lookUp);
    }
  }

  /**
   * Gets the sequence name.
   *
   * @return the sequence name
   */
  public String getSequenceName() {
    return sequenceName;
  }

  /**
   * Sets the sequence name.
   *
   * @param sequence the new sequence name
   */
  public void setSequenceName(String sequence) {
    sequenceName = sequence;
  }

  /**
   * Gets the auto incr name.
   *
   * @return the auto incr name
   */
  public String getAutoIncrName() {
    return autoIncrName;
  }

  /**
   * Sets the auto incr name.
   *
   * @param name the new auto incr name
   */
  public void setAutoIncrName(String name) {
    autoIncrName = name;
  }

  /**
   * Checks if is id val as string.
   *
   * @return true, if is id val as string
   */
  public boolean isIdValAsString() {
    return idValAsString;
  }

  /**
   * Sets the id val as string.
   *
   * @param idValAsString the new id val as string
   */
  public void setIdValAsString(boolean idValAsString) {
    this.idValAsString = idValAsString;
  }

  /**
   * Import table.
   *
   * @param isReader the is reader
   * @param infoMsg the info msg
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public String importTable(InputStreamReader isReader, StringBuilder infoMsg)
      throws SQLException, IOException {

    CSVReader csvReader = new CSVReader(isReader);

    String[] header = csvReader.readNext();

    if (header.length < 1) {
      return "Uploaded file does not appear to be a CSV file (no headers found)";
    }

    if (!header[0].matches("\\p{Print}*")) {
      return "Uploaded file does not appear to be a CSV file (non-printable characters found)";
    }

    if (header.length == 1) {
      return "Uploaded file appears to be using non-comma separators (maybe semi-colon or tab)";
    }

    GenericDAO dao = new GenericDAO(tableName);
    BasicDynaBean bean = null;

    StringBuilder warnings = new StringBuilder();
    int lineNum = 1;
    int lineWarnings = 0;
    int headerWarnings = 0;
    int numInserted = 0;
    int numUpdated = 0;
    boolean[] ignoreColumn = new boolean[header.length];

    for (int i = 0; i < header.length; i++) {
      String fieldName = getRealFieldName(header[i].trim());
      if (!allFields.contains(fieldName)) {
        addWarning(warnings, 0,
            "Unknown property in header (ignoring data in column): "
                + header[i]);
        ignoreColumn[i] = true;
        // continue ... we will ignore unknown errors, but show a warning.
        headerWarnings++;
      } else {
        ignoreColumn[i] = false;
      }

      header[i] = fieldName;
    }

    Map<String, Map<String, String>> masterData = getMasterData();

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(true); // we are committing after every record.
    String[] line = null;

    while ((line = csvReader.readNext()) != null) {
      lineNum++;
      logger.debug("Processing line: " + lineNum);
      bean = dao.getBean(con);
      boolean hasWarnings = false;
      int numNonEmptyColumns = 0;

      /*
       * Convert the line into one bean
       */
      for (int i = 0; i < header.length && i < line.length; i++) {
        if (ignoreColumn[i]) {
          continue;
        }
        String fieldName = header[i];
        logger.debug("Processing field: " + fieldName);
        String value = line[i].trim();

        if ((value != null) && !value.equals("")) {
          Map<String, String> masterValuesMap = masterData.get(fieldName);
          if (masterValuesMap != null) {
            String valueId = masterValuesMap.get(value.toLowerCase());
            if (valueId == null) {
              addWarning(warnings, lineNum, "No master value found for " + value
                  + " (" + fieldName + ")");
              hasWarnings = true;
              continue; // next column
            }
            String filterCheckName = (String) masterFilterCheck.get(fieldName);
            if (filterCheckName != null && !filterCheckName.equals(value)) {
              addWarning(warnings, lineNum, "supported value is " + value
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
            if (null != enforcedType && ConvertUtils.convert(value, enforcedType) == null) {
              // Convert to make sure that the type is convertible.
              // because of default setting in AppInit ConvertUtils.convert
              // returns null
              // if failed to convert.
              addWarning(warnings, lineNum, "Conversion error: " + value
                  + " could not converted to " 
                  + (enforcedType == BigDecimal.class ? " Number " : enforcedType.getSimpleName()) 
                  + " for " + fieldName);
              hasWarnings = true;
              continue;
            }
            logger.debug(
                "Property type is " + property.getType() + ", value: " + value);
            bean.set(fieldName,
                ConvertUtils.convert(value, property.getType()));
          } catch (ConversionException exp) {
            addWarning(warnings, lineNum,
                "Conversion error: " + value + " could not be converted to "
                    + property.getType() + " for " + fieldName);
            hasWarnings = true;
            logger.error("Conversion error: ", exp);
          }
          numNonEmptyColumns++;
        } else {
          // "" converted to null
          bean.set(fieldName, null);
        }
      }

      if (masterLookupMap != null && !masterLookupMap.isEmpty()) {
        for (DynaProperty property : bean.getDynaClass().getDynaProperties()) {
          if (masterLookupMap.get(property.getName()) != null) {
            Lookup lookUp = masterLookupMap.get(property.getName());
            Map map = lookUp.map;
            List list = (List) map.get(bean.get(lookUp.idField));
            if (list == null || !list.contains(bean.get(lookUp.field))) {
              hasWarnings = true;
              addWarning(warnings, lineNum, lookUp.errorMsg);
            }
          }
        }
      }

      if (hasWarnings) {
        lineWarnings++;
        continue; // next line.
      }

      if (numNonEmptyColumns == 0) {
        continue; // empty line, nothing to do here.
      }
      // Create the key value map for updates
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
            int rows = dao.update(con, bean.getMap(), keyValueMap);
            if (rows != 0) {
              numUpdated++;
            } else {
              dao.insert(con, bean);
              numInserted++;
            }
          } else {
            if (sequenceName != null && autoIncrName != null) {
              bean.set(keys[0],
                  AutoIncrementId.getSequenceId(sequenceName, autoIncrName));
            } else if (sequenceName != null) {
              // idValAsString is set to true, when we have an id(primary key)
              // coloumn of the table is character varying type in the table
              // design
              bean.set(keys[0],
                  idValAsString
                      ? ((Integer) DataBaseUtil.getNextSequence(sequenceName))
                          .toString()
                      : DataBaseUtil.getNextSequence(sequenceName));
            } else if (autoIncrName != null) {
              bean.set(keys[0], AutoIncrementId.getNewIncrUniqueId(keys[0],
                  tableName, autoIncrName));
            }
            dao.insert(con, bean);
            numInserted++;
          }

        } else {
          if (allKeysGiven) {
            // try an update. If it fails, do an insert.
            int rows = dao.update(con, bean.getMap(), keyValueMap);
            if (rows != 0) {
              numUpdated++;
            } else {
              dao.insert(con, bean);
              numInserted++;
            }
          } else {
            // we don't allow inserts for multiple keys by autogeneration
            addWarning(warnings, lineNum, "Key fields not given");
            lineWarnings++;
          }
        }
      } catch (SQLException sqle) {
        if (DataBaseUtil.isDuplicateViolation(sqle)) {
          addWarning(warnings, lineNum, "Duplicate record found");
        } else {
          addWarning(warnings, lineNum, "Unknown error: " + sqle.getMessage());
          logger.error("Error uploading csv line", sqle);
        }
        lineWarnings++;
      }

    } // end while each line

    con.close();

    infoMsg.append("Processed lines: ").append(lineNum - 1).append("<br>");
    if (numInserted > 0) {
      infoMsg.append("New rows inserted: ").append(numInserted).append("<br>");
    }
    infoMsg.append("Existing records updated: ").append(numUpdated)
        .append("<br>");

    if (lineWarnings > 0 || headerWarnings > 0) {
      if (lineWarnings > 0) {
        infoMsg.append("Lines with errors: ").append(lineWarnings);
      }
      if (headerWarnings > 0) {
        infoMsg.append("Headers with errors: ").append(headerWarnings);
      }
      infoMsg.append("<br>");
      infoMsg.append("<hr>");
      infoMsg.append(warnings);
    }
    logger.info("{}", infoMsg);
    return null;
  }

  /**
   * Export table.
   *
   * @param res the res
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void exportTable(HttpServletResponse res)
      throws SQLException, java.io.IOException {
    exportTable(res, null);
  }

  /**
   * Export table.
   *
   * @param res the res
   * @param fileName the file name
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void exportTable(HttpServletResponse res, String fileName)
      throws SQLException, java.io.IOException {
    // form the query based on the master data
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
          query.append(ms.refTable).append(".").append(ms.nameField);
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
      query.append("\n LEFT JOIN ").append(ms.refTable).append(" ON ")
          .append(tableName).append(".").append(ms.field).append(" = ")
          .append(ms.refTable).append(".").append(ms.idField);
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

    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      // run the query and stuff it into the writer.
      con = DataBaseUtil.getReadOnlyConnection();
      con.setAutoCommit(false); // required for setFetchSize to work
      ps = con.prepareStatement(query.toString());
      ps.setFetchSize(1000); // fetch only 1000 rows at a time
      rs = ps.executeQuery();

      res.setHeader("Content-type", "application/csv");
      res.setHeader("Content-disposition", "attachment; filename="
          + (null == fileName ? tableName : fileName) + ".csv");

      CSVWriter writer = new CSVWriter(res.getWriter(),
          CSVWriter.DEFAULT_SEPARATOR);
      writer.writeAll(rs, true);
      writer.flush();

    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }

  }

  /**
   * Gets the master data.
   *
   * @return the master data
   * @throws SQLException the SQL exception
   */
  private Map getMasterData() throws SQLException {
    Map masterData = new HashMap();
    for (Master ms : masterFieldMap.values()) {
      String[] listParam = new String[] { ms.idField, ms.nameField };
      List<BasicDynaBean> beans = new GenericDAO(ms.refTable)
          .listAll(Arrays.asList(listParam));
      Map beanMap = new HashMap();
      for (BasicDynaBean b : beans) {
        // name : id, eg: Bangalore : CT0001
        String name = (String) b.get(ms.nameField);
        if (name != null) { // avoid bad data in masters
          beanMap.put(name.toLowerCase(), b.get(ms.idField).toString());
        }
      }
      masterData.put(ms.field, beanMap);
    }
    return masterData;
  }

  /**
   * Adds the warning.
   *
   * @param warnings the warnings
   * @param line the line
   * @param msg the msg
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
   * @param masterFilterCheck the master filter check
   */
  public void setMasterFilterCheck(HashMap<String, Object> masterFilterCheck) {
    this.masterFilterCheck = masterFilterCheck;
  }

}
