package com.insta.hms.integration.insurance;


/**
 * The Class ClaimDocument.
 */
public class ClaimDocument {

  /** The content. */
  private byte[] content;

  /** The file name. */
  private String fileName;

  /**
   * Gets the content.
   *
   * @return the content
   */
  public byte[] getContent() {
    return content;
  }

  /**
   * Sets the content.
   *
   * @param content the new content
   */
  public void setContent(byte[] content) {
    this.content = content;
  }

  /**
   * Gets the file name.
   *
   * @return the file name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Sets the file name.
   *
   * @param fileName the new file name
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }


}
