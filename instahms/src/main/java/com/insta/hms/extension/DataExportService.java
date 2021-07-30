package com.insta.hms.extension;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Interface DataExportService.
 *
 * @author anupama Marker interface for all data export to external applications.
 */

public interface DataExportService {

  /**
   * Export.
   *
   * @param recordMap the record map
   * @param centerId the center id
   * @param accountGroupId the account group id
   * @return the integer
   * @throws Exception the exception
   */
  Integer export(Map<String, Map<String, Object>> recordMap, Integer centerId,
      Integer accountGroupId) throws Exception;

  /**
   * Supports target.
   *
   * @param target the target
   * @return true, if successful
   */
  boolean supportsTarget(String target);

  /**
   * Sets the export event handler.
   *
   * @param handler the new export event handler
   */
  void setExportEventHandler(DataExportEventHandler handler);
}
