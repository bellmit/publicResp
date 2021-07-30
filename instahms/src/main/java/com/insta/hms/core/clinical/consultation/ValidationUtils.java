package com.insta.hms.core.clinical.consultation;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.List;
import java.util.Map;

/**
 * @author teja.
 *
 */

public class ValidationUtils {

  /**
   * Is valid key.
   * @param table the table
   * @param keyValue the object
   * @param tableColumnName the string
   * @return boolean value
   */
  public static boolean isKeyValid(String table, Object keyValue, String tableColumnName) {
    String query = "Select " + tableColumnName + " From " + table + " Where " + tableColumnName
        + "=?";
    return !(DatabaseHelper.queryToDynaBean(query, new Object[] { keyValue }) == null);

  }

  public static BasicDynaBean isKeyValid(String table, Map<String, Object> identifiers) {
    GenericRepository repo = new GenericRepository(table);
    return repo.findByKey(identifiers);
  }

  /**
   * Validate error map.
   * @param errorMap the ValidationErrorMap
   * @param conversionErros list of string
   * @return ValidationErrorMap
   */
  public static ValidationErrorMap copyCoversionErrors(ValidationErrorMap errorMap,
      List<String> conversionErros) {
    for (String attr : conversionErros) {
      errorMap.addError(attr, "exception.Invalid.data");
    }
    return errorMap;

  }

  /**
   * Checks is duplicate data.
   * @param table the string
   * @param tableColNameMap the map
   * @param keyValue the objects
   * @return boolean value
   */
  public static boolean isDuplicateData(String table, Map<Integer, String> tableColNameMap,
      Object... keyValue) {
    StringBuilder selectedColumns = new StringBuilder("SELECT ");
    StringBuilder whereColumns = new StringBuilder(" WHERE ");
    for (int i = 0; i < tableColNameMap.size(); i++) {
      selectedColumns.append(tableColNameMap.get(i));
      whereColumns.append(tableColNameMap.get(i) + "=?");
      if (!(i == tableColNameMap.size() - 1)) {
        selectedColumns.append(",");
        whereColumns.append(" AND ");
      }
    }
    return !(DatabaseHelper.queryToDynaBean(
        selectedColumns.toString() + " FROM " + table + whereColumns.toString(), keyValue) == null);

  }

}
