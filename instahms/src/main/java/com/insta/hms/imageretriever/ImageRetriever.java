package com.insta.hms.imageretriever;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

// TODO: Auto-generated Javadoc
/**
 * The Interface ImageRetriever.
 */
public interface ImageRetriever {
  
  /**
   * Retrieve.
   *
   * @param url the url
   * @return the input stream
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * 
   */
  /*
   * /**
   * @author krishna
   *  Simple interface used by HtmlConverter: we need a callback that the converter
   *  can call to retrieve images in any "<img src=" kind of tag. Based on the URL,
   *  the retriever will fetch an InputStream.
   */
  InputStream retrieve(String url) throws IOException, SQLException;

}
