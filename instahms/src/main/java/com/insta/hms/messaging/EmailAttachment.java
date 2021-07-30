package com.insta.hms.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The Class EmailAttachment.
 */
public class EmailAttachment implements javax.activation.DataSource {
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(EmailAttachment.class);
  
  /** The name. */
  // TODO : Compare this with EmailDataSource implementation and iron out the differences
  private String name;
  
  /** The content type. */
  private String contentType;
  
  /** The bytes. */
  private byte[] bytes;

  /**
   * Instantiates a new email attachment.
   *
   * @param name the name
   * @param contentType the content type
   * @param bytes the bytes
   */
  public EmailAttachment(String name, String contentType, byte[] bytes) {
    this.name = name;
    this.contentType = contentType;
    this.bytes = bytes;
  }

  /**
   * Instantiates a new email attachment.
   *
   * @param name the name
   * @param contentType the content type
   * @param is the is
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public EmailAttachment(String name, String contentType, InputStream is) throws IOException {
    this.name = name;
    this.contentType = contentType;

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    byte[] readbytes = new byte[4096];

    while (is.read(readbytes) > 0) {
      os.write(readbytes);
    }
    os.flush();
    bytes = os.toByteArray();
    os.close();
  }

  /* (non-Javadoc)
   * @see javax.activation.DataSource#getContentType()
   */
  public String getContentType() {
    return contentType;
  }

  /* (non-Javadoc)
   * @see javax.activation.DataSource#getInputStream()
   */
  public InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream(bytes);
  }

  /* (non-Javadoc)
   * @see javax.activation.DataSource#getName()
   */
  public String getName() {
    return name;
  }

  /* (non-Javadoc)
   * @see javax.activation.DataSource#getOutputStream()
   */
  public OutputStream getOutputStream() throws IOException {
    return null;
  }

}
