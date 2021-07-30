package com.insta.hms.integration.common;

import java.io.IOException;
import java.io.OutputStream;

public interface Connector {

  public abstract boolean open(String uri) throws IOException;

  public abstract void close() throws IOException;

  public abstract OutputStream getOutputStream();
}
