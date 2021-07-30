package com.insta.hms.eservice;

import com.insta.hms.util.MapWrapper;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The Class EResponseProcessor.
 */
public abstract class EResponseProcessor {

  static Logger log = LoggerFactory.getLogger(EResponseProcessor.class);

  /**
   * The default buffer size.
   */
  public static Integer DEFAULT_BUFFER_SIZE = 4 * 1024;

  /**
   * Process.
   *
   * @param response the response
   * @param os       the os
   * @return the e result
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public abstract EResult process(EResponse response, OutputStream os) throws IOException;

  /**
   * Read byte stream.
   *
   * @param is the is
   * @return the byte[]
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private static byte[] readByteStream(InputStream is) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    int read = 0;
    while ((read = is.read(buffer)) > 0) {
      baos.write(buffer);
    }
    return baos.toByteArray();
  }

  /**
   * The Class Base64Decoder.
   */
  public static class Base64Decoder extends EResponseProcessor {

    /* (non-Javadoc)
     * @see com.insta.hms.eservice.EResponseProcessor#process(com.insta.hms.eservice.EResponse,
     * java.io.OutputStream)
     */
    @Override
    public EResult process(EResponse response, OutputStream os) throws IOException {
      InputStream is = response.getInputStream();
      byte[] buffer = readByteStream(is);
      byte[] decoded = Base64.decodeBase64(buffer);
      os.write(decoded, 0, decoded.length);
      return null;
    }
  }

  /**
   * The Class Base64Encoder.
   */
  public static class Base64Encoder extends EResponseProcessor {

    /* (non-Javadoc)
     * @see com.insta.hms.eservice.EResponseProcessor#process(com.insta.hms.eservice.EResponse,
     * java.io.OutputStream)
     */
    @Override
    public EResult process(EResponse response, OutputStream out) throws IOException {
      InputStream is = response.getInputStream();
      byte[] buffer = readByteStream(is);
      byte[] encodedContent = Base64.encodeBase64(buffer);
      out.write(encodedContent);
      return null;
    }
  }

  /**
   * The Class ZipStreamProcessor.
   */
  public static class ZipStreamProcessor extends EResponseProcessor {

    /* (non-Javadoc)
     * @see com.insta.hms.eservice.EResponseProcessor#process(com.insta.hms.eservice.EResponse,
     * java.io.OutputStream)
     */
    @Override
    public EResult process(EResponse response, OutputStream out) throws IOException {
      ZipInputStream zis = null;
      ZipEntry ze = null;
      zis = new ZipInputStream(response.getInputStream());
      if ((ze = zis.getNextEntry()) != null) { // we support only one file right now
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int size = 0;
        while ((size = zis.read(buffer)) != -1) {
          out.write(buffer, 0, size);
        }
      }
      return null;
    }
  }

  /**
   * The Class CsvStreamProcessor.
   */
  public static class CsvStreamProcessor extends EResponseProcessor {

    /* (non-Javadoc)
     * @see com.insta.hms.eservice.EResponseProcessor#process(com.insta.hms.eservice.EResponse,
     * java.io.OutputStream)
     */
    @Override
    public EResult process(EResponse response, OutputStream out) throws IOException {
      InputStream is = response.getInputStream();
      byte[] buffer = readByteStream(is);
      out.write(buffer);
      return null;
    }
  }

  /**
   * The Class XmlStreamProcessor.
   */
  public static class XmlStreamProcessor extends EResponseProcessor {

    /**
     * The parser.
     */
    private EResultParser parser = null;

    /**
     * The result out.
     */
    private EResult resultOut = null;

    /**
     * Instantiates a new xml stream processor.
     *
     * @param parser the parser
     */
    public XmlStreamProcessor(EResultParser parser) {
      this.parser = parser;
    }

    /**
     * Process.
     *
     * @param response the response
     * @return the e result
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public EResult process(EResponse response) throws IOException {
      return process(response, null, true);
    }

    /**
     * Process.
     *
     * @param response     the response
     * @param addXmlProlog the add xml prolog
     * @return the e result
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public EResult process(EResponse response, boolean addXmlProlog) throws IOException {
      return process(response, null, addXmlProlog);
    }

    /* (non-Javadoc)
     * @see com.insta.hms.eservice.EResponseProcessor#process(com.insta.hms.eservice.EResponse,
     * java.io.OutputStream)
     */
    @Override
    public EResult process(EResponse response, OutputStream out) throws IOException {
      return process(response, out, true);
    }

    /**
     * Process.
     *
     * @param response     the response
     * @param out          the out
     * @param addXmlProlog the add xml prolog
     * @return the e result
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private EResult process(EResponse response, OutputStream out, boolean addXmlProlog)
        throws IOException {
      String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
      StringBuilder builder = (addXmlProlog) ? new StringBuilder(xmlHeader).append("\n") :
             new StringBuilder();
      InputStreamReader reader = new InputStreamReader(response.getInputStream());
      BufferedReader br = new BufferedReader(reader);
      String line = null;
      String utf8BOM = "\uFEFF";
      while ((line = br.readLine()) != null) {
        if (line.startsWith(utf8BOM)) {
          line = line.substring(1);
        }
        builder.append(line);
      }

      log.debug(builder.toString());
      try {
        resultOut = parser.parse(builder.toString());
      } catch (SAXException se) {
        throw new IOException(se);
      }
      return resultOut;
    }
  }

  /**
   * The Class SimpleParameterProcessor.
   */
  public static class SimpleParameterProcessor extends EResponseProcessor {

    /**
     * The keys.
     */
    private Object[] keys;

    /**
     * Instantiates a new simple parameter processor.
     *
     * @param keys the keys
     */
    public SimpleParameterProcessor(Object[] keys) {
      super();
      this.keys = keys;
    }

    /**
     * Process.
     *
     * @param response the response
     * @return the e result
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public EResult process(EResponse response) throws IOException {
      return process(response, null);
    }

    /* (non-Javadoc)
     * @see com.insta.hms.eservice.EResponseProcessor#process(com.insta.hms.eservice.EResponse,
     * java.io.OutputStream)
     */
    @Override
    public EResult process(EResponse response, OutputStream os) throws IOException {
      EResultMapper result = new EResultMapper();
      log.debug("SimpleParameterProcessor - processing: keys " + keys + " resultParams: "
          + response.getResultParams());
      result.load(keys, response.getResultParams());
      return result;
    }

    /**
     * The Class EResultMapper.
     */
    // class used only by SimpleParameterProcessor
    private static class EResultMapper extends MapWrapper implements EResult {

      /**
       * Instantiates a new e result mapper.
       */
      public EResultMapper() {
        super();
      }

      /**
       * Load.
       *
       * @param keys   the keys
       * @param values the values
       */
      public void load(Object[] keys, Object[] values) {
        if (null == keys || keys.length == 0) {
          return;
        }
        for (int i = 0; i < keys.length; i++) {
          log.debug("Loading key :" + keys[i] + " value:" + values[i]);
          put(keys[i], values.length > i ? values[i] : null);
        }
      }
    }
  }
}
