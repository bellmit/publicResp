package com.insta.hms.scheduledreport;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The Class EmailAttachmentDataSource.
 *
 * @author krishna.t
 */

public class EmailAttachmentDataSource implements javax.activation.DataSource {
  
  /** The data. */
  private byte[] data;
  
  /** The content type. */
  private String contentType;
  
  /** The name. */
  private String name;
  
  /** The mail ids. */
  private String[] mailIds;

  /**
   * Sets the data.
   *
   * @param data the new data
   */
  public void setData(byte[] data) {
    this.data = data;
  }

  /**
   * Sets the content type.
   *
   * @param contentType the new content type
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /* (non-Javadoc)
   * @see javax.activation.DataSource#getInputStream()
   */
  public InputStream getInputStream() {
    return new ByteArrayInputStream(data);
  }

  /* (non-Javadoc)
   * @see javax.activation.DataSource#getOutputStream()
   */
  public OutputStream getOutputStream() {
    return null;
  }

  /* (non-Javadoc)
   * @see javax.activation.DataSource#getContentType()
   */
  public String getContentType() {
    return contentType;
  }

  /* (non-Javadoc)
   * @see javax.activation.DataSource#getName()
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the mail ids.
   *
   * @return the mailIds
   */
  public String[] getMailIds() {
    return mailIds;
  }

  /**
   * Sets the mail ids.
   *
   * @param mailIds          the mailIds to set
   */
  public void setMailIds(String[] mailIds) {
    this.mailIds = mailIds;
  }

}
