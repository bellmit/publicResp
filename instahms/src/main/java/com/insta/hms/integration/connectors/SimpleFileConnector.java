package com.insta.hms.integration.connectors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SimpleFileConnector extends FileConnector {

  private String url = null;

  @Override
  public boolean open(String url) throws IOException {
    this.url = url;
    return true;
  }

  @Override
  public void close() throws IOException {
  }

  @Override
  public OutputStream getOutputStream() {
    OutputStream os = null;
    try {
      os = new FileOutputStream(new File(url), true); // Should be opened only in append mode
    } catch (FileNotFoundException ex) {
      ex.printStackTrace();
    }
    return os;
  }
}