package com.insta.hms.insurance;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

// TODO: Auto-generated Javadoc
/**
 * The Class EmailAttachmentDataSource.
 */
public class EmailAttachmentDataSource implements javax.activation.DataSource {

  /** The data. */
  private byte[] data;

  /** The content type. */
  private String contentType;

  /** The name. */
  private String name;

  /** The Tomail ids. */
  private String[] tomailIds;

  /** The C cmail ids. */
  private String[] ccmailIds;

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

  /*
   * (non-Javadoc)
   * 
   * @see javax.activation.DataSource#getInputStream()
   */
  public InputStream getInputStream() {
    return new ByteArrayInputStream(data);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.activation.DataSource#getOutputStream()
   */
  public OutputStream getOutputStream() {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.activation.DataSource#getContentType()
   */
  public String getContentType() {
    return contentType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.activation.DataSource#getName()
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the c cmail ids.
   *
   * @return the c cmail ids
   */
  public String[] getCCmailIds() {
    return ccmailIds;
  }

  /**
   * Sets the c cmail ids.
   *
   * @param cmailIds the new c cmail ids
   */
  public void setCCmailIds(String[] cmailIds) {
    ccmailIds = cmailIds;
  }

  /**
   * Gets the tomail ids.
   *
   * @return the tomail ids
   */
  public String[] getTomailIds() {
    return tomailIds;
  }

  /**
   * Sets the tomail ids.
   *
   * @param tomailIds the new tomail ids
   */
  public void setTomailIds(String[] tomailIds) {
    this.tomailIds = tomailIds;
  }
}
