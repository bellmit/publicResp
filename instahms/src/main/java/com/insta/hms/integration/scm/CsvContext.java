package com.insta.hms.integration.scm;

import java.io.Serializable;

/**
 * The interface Csv context.
 */
public abstract class CsvContext implements Serializable {
  /**
   * Get columns string [ ].
   *
   * @return the string [ ]
   */
  public abstract String[] getColumns();

  public abstract String getEntityName();

  //TODO use this for job sync
  Object getMonitor() {
    return this.getClass();
  }
}
