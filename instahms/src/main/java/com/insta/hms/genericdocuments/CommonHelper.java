/**
 *
 */

package com.insta.hms.genericdocuments;

import freemarker.template.TemplateException;
import jlibs.core.util.regex.TemplateMatcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class CommonHelper.
 *
 * @author krishna.t
 */
public class CommonHelper {

  // static PatientHeaderTemplateDAO phTemplateDao = new PatientHeaderTemplateDAO();

  /**
   * Replace tags.
   *
   * @param instream the instream
   * @param outstream the outstream
   * @param fields the fields
   * @param isRtf the is rtf
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void replaceTags(InputStream instream, OutputStream outstream,
      Map<String, String> fields, boolean isRtf) throws IOException {

    /*
     * in RTF, { and } are escaped in the actual file, even though they appear normal in the
     * document, since these are special characters for RTF. So, we need to look for the escaped
     * version if the content is rtf, else, look for it as is.
     */
    TemplateMatcher matcher =
        isRtf ? new TemplateMatcher("$\\{", "\\}") : new TemplateMatcher("${", "}");

    matcher.replace(new InputStreamReader(instream), new OutputStreamWriter(outstream), fields);
  }

  /**
   * Replace tags.
   *
   * @param content the content
   * @param fields the fields
   * @param isRtf the is rtf
   * @return the string
   */
  public static String replaceTags(String content, Map<String, String> fields, boolean isRtf) {
    TemplateMatcher matcher =
        isRtf ? new TemplateMatcher("$\\{", "\\}") : new TemplateMatcher("${", "}");
    return matcher.replace(content, fields);
  }

  /**
   * Gets the value from map.
   *
   * @param params the params
   * @param key the key
   * @param index the index
   * @return the value from map
   */
  public static String getValueFromMap(Map params, String key, int index) {
    Object[] object = (Object[]) params.get(key);
    if (object != null && object[index] != null && !object[index].equals("")) {
      return object[index].toString();
    }
    return null;
  }

  /**
   * Gets the value from map.
   *
   * @param params the params
   * @param key the key
   * @return the value from map
   */
  public static String getValueFromMap(Map params, String key) {
    Object[] object = (Object[]) params.get(key);
    if (object != null && object[0] != null) {
      return (String) object[0];
    }
    return null;
  }

  /**
   * Adds the rich text title.
   *
   * @param docContent the doc content
   * @param title the title
   * @param pheaderTemplateId the pheader template id
   * @param visitId the visit id
   * @param mrNo the mr no
   * @return the string
   * @throws TemplateException the template exception
   */
  public static String addRichTextTitle(String docContent, String title,
      Integer pheaderTemplateId, String visitId, String mrNo) throws TemplateException {

    StringBuilder html = new StringBuilder("");

    html.append("<div align='center'>" + title + "</div>");
    html.append(docContent);

    return html.toString();
  }

}
