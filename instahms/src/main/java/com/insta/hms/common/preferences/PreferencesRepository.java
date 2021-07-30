package com.insta.hms.common.preferences;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;

/**
 * the repository for single-record tables.
 *
 * @author aditya.b
 */
public class PreferencesRepository extends GenericRepository {

  /**
   * Instantiates a new preferences repository.
   *
   * @param table the table
   */
  public PreferencesRepository(String table) {
    super(table);
  }

  /**
   * Override to write custom select queries.
   *
   * @return the view query
   */
  public String getViewQuery() {
    return null;
  }

  /**
   * gets all the fields if the view query is null otherwise executes view query.
   *
   * @param viewQuery the view query
   * @return the record
   */
  public BasicDynaBean getRecord(String viewQuery) {
    if (null != viewQuery && !viewQuery.isEmpty()) {
      return DatabaseHelper.queryToDynaBean(viewQuery);
    }
    return super.getRecord();
  }

  @Override
  public BasicDynaBean getRecord() {
    return getRecord(null);
  }
}
