package com.insta.hms.exception;

import org.springframework.http.HttpStatus;

/**
 * The Class FileStoreIOException is used to throw exceptions from the file
 * store like minio or S3 while trying to retrieve or store a document.
 */
public class FileStoreIOException extends HMSException {

  private static final HttpStatus defaultStatus = HttpStatus.INTERNAL_SERVER_ERROR;
  private static final String defaultKey = "exception.file.store.io.exception";

  public FileStoreIOException(String messageKey, String[] params) {
    super(defaultStatus, messageKey, params);
  }

  public FileStoreIOException(String[] params) {
    this(defaultKey, params);
  }

  public FileStoreIOException(String messageKey) {
    this(messageKey, null);
  }

  public FileStoreIOException(Throwable cause) {
    super(cause);
  }

  public FileStoreIOException() {
    this(defaultKey, null);
  }

  private static final long serialVersionUID = 1L;

}

