/**
 *
 */

package com.bob.hms.common;

import eu.medsea.mimeutil.MimeUtil2;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Collection;

/**
 * The Class MimeTypeDetector.
 *
 * @author krishna
 */
public class MimeTypeDetector {

  /**
   * Instantiates a new mime type detector.
   */
  private MimeTypeDetector() {

  }

  private static MimeUtil2 mimeUtil = null;

  /**
   * Gets the mime util.
   *
   * @return the mime util
   */
  public static MimeUtil2 getMimeUtil() {
    if (mimeUtil == null) {
      mimeUtil = new MimeUtil2();
      mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
    }
    return mimeUtil;
  }

  /**
   * Gets the mime types.
   *
   * @param is the is
   * @return the mime types
   */
  public static Collection getMimeTypes(InputStream is) {
    BufferedInputStream stream = new BufferedInputStream(is);
    return getMimeUtil().getMimeTypes(stream);
  }

}
