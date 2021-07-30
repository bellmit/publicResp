package com.insta.hms.jobs;

import com.bob.hms.common.RequestContext;

import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * This class is an anchor for all job classes, other then SQLUpdateJob classes This is more like to
 * write common logic for all jobs.
 *
 * @author yashwant
 */
public abstract class GenericJob extends QuartzJobBean {

  /** This is required for setting schema to run the job in particular schema. */
  private String schema;

  /**
   * Gets the schema.
   *
   * @return the schema
   */
  public String getSchema() {
    return schema;
  }

  /**
   * Sets the schema.
   *
   * @param schema the new schema
   */
  public void setSchema(String schema) {
    this.schema = schema;
  }

  /**
   * Sets the job connection details.
   */
  protected void setJobConnectionDetails() {
    RequestContext.setConnectionDetails(new String[] { null, "", getSchema(), "_system", "", "" });
  }
}
