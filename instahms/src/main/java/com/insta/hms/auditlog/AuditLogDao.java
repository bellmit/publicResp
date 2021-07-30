package com.insta.hms.auditlog;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

public class AuditLogDao extends GenericDAO {
  private String type = null;

  static Logger logger = LoggerFactory.getLogger(AuditLogDao.class);

  public AuditLogDao(String auditType, String table) {
    super(table);
    this.type = auditType;
  }

  private static final String AUDIT_LOG_QUERY_FIELDS = "SELECT *";
  private static final String AUDIT_LOG_QUERY_COUNT = "SELECT COUNT(*)";

  /*
   * this is kinda redundant - does the same thing as the GenericDAO. Remove if nothing more added
   * here
   */

  protected String getType() {
    return type;
  }

  /**
   * Gets the audit log list.
   *
   * @param filterMap the filter map
   * @param listParams the list params
   * @return the audit log list
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public PagedList getAuditLogList(Map filterMap, Map listParams)
      throws SQLException, ParseException {

    String auditLogTables = " FROM " + getTable();
    Connection con = DataBaseUtil.getReadOnlyConnection();
    SearchQueryBuilder sqb = null;
    try {
      sqb = new SearchQueryBuilder(con, AUDIT_LOG_QUERY_FIELDS, AUDIT_LOG_QUERY_COUNT,
          auditLogTables, listParams);
      sqb.addFilterFromParamMap(filterMap);
      // TODO: Need to add a secondary sort on the entity key
      sqb.addSecondarySort("log_id");
      sqb.build();
      PagedList pagedList = sqb.getMappedPagedList();
      return pagedList;
    } finally {
      sqb.close();
      con.close();
    }
  }

  /**
   * For bulk data inserts or charge updates audit log trigger will be disabled ,to avoid connection
   * timeout inserting into audit log tables saying BULLK INSERT or BULK UPDATE.
   *
   * @param con the con
   * @param userName the user name
   * @param operation the operation
   * @param fieldName the field name
   * @param newValue the new value
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean logMasterChange(Connection con, String userName, String operation,
      String fieldName, String newValue) throws SQLException, IOException {
    BasicDynaBean logMasterBean = getBean(con);
    logMasterBean.set("user_name", userName);
    logMasterBean.set("operation", "BULK " + operation);
    if (operation.equals("INSERT")) {
      logMasterBean.set("field_name", fieldName);
      logMasterBean.set("old_value", "");
      logMasterBean.set("new_value", newValue);
    }
    return insert(con, logMasterBean);
  }

}
