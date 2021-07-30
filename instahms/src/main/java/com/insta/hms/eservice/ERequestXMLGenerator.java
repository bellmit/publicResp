/**
 *
 */

package com.insta.hms.eservice;

import com.insta.hms.common.AppInit;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ERequestXMLGenerator.
 *
 * @param <T> the generic type
 * @param <V> the value type
 * @author lakshmi
 */
public abstract class ERequestXMLGenerator<T extends ERequest, V extends EValidator<T>> {

  static Logger logger = LoggerFactory.getLogger(ERequestXMLGenerator.class);

  /**
   * The body template.
   */
  private String bodyTemplate = null;

  /**
   * The header template.
   */
  private String headerTemplate = null;

  /**
   * The footer template.
   */
  private String footerTemplate = null;

  /**
   * Instantiates a new e request XML generator.
   *
   * @param bodyTemplate   the body template
   * @param headerTemplate the header template
   * @param footerTemplate the footer template
   */
  public ERequestXMLGenerator(String bodyTemplate, String headerTemplate, String footerTemplate) {
    this.bodyTemplate = bodyTemplate;
    this.headerTemplate = headerTemplate;
    this.footerTemplate = footerTemplate;
  }

  /**
   * Generate request XML.
   *
   * @param requestId   the request id
   * @param requestType the request type
   * @param errorList   the error list
   * @return the string
   * @throws IOException       Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws SQLException      the SQL exception
   */
  public String generateRequestXML(Object requestId, String requestType, List<String> errorList)
      throws IOException, TemplateException, SQLException {

    T request = getRequest(requestId, requestType);
    if (null != request) {
      V validator = getRequestValidator();
      boolean valid = true;
      if (null != validator) {
        valid = validator.validate(request);
        if (!valid) {
          Map<String, String> errors = validator.getErrorMap();
          List<String> errorMessages = formatErrors(errors);
          errorList.addAll(errorMessages);
          return null;
        }
      }
      StringWriter xmlOut = new StringWriter();
      processRequest(request, xmlOut);
      return xmlOut.toString();
    }
    return null;
  }

  /**
   * Gets the request.
   *
   * @param requestId   the request id
   * @param requestType the request type
   * @return the request
   */
  public abstract T getRequest(Object requestId, String requestType);

  /**
   * Gets the request validator.
   *
   * @return the request validator
   */
  public abstract V getRequestValidator();

  /**
   * Format errors.
   *
   * @param errors the errors
   * @return the list
   */
  public List<String> formatErrors(Map<String, String> errors) {
    List<String> errorList = new ArrayList<String>();
    for (String key : errors.keySet()) {
      errorList.add(errors.get(key));
    }
    return errorList;
  }

  /**
   * Process request.
   *
   * @param request the request
   * @param writer  the writer
   * @throws TemplateException the template exception
   * @throws IOException       Signals that an I/O exception has occurred.
   */
  public void processRequest(T request, Writer writer) throws TemplateException, IOException {
    Map<String, Object> data = new HashMap<String, Object>();
    if (null != request) {
      data.put("header", request.getHeader());
      data.put("request", request);
      data.put("footer", request.getFooter());
    }
    Template thead = getHeaderTemplate();
    if (null != thead) {
      thead.process(data, writer);
    }
    Template tbody = getBodyTemplate();
    if (null != tbody) {
      tbody.process(data, writer);
    }

    Template tfoot = getFooterTemplate();
    if (null != tfoot) {
      tfoot.process(data, writer);
    }
  }

  /**
   * Gets the footer template.
   *
   * @return the footer template
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected Template getFooterTemplate() throws IOException {
    Template template = AppInit.getFmConfig().getTemplate(getTemplateFolder() + "/"
        + this.footerTemplate);
    return template;
  }

  /**
   * Gets the body template.
   *
   * @return the body template
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected Template getBodyTemplate() throws IOException {
    Template template = AppInit.getFmConfig().getTemplate(getTemplateFolder() + "/"
        + this.bodyTemplate);
    return template;
  }

  /**
   * Gets the header template.
   *
   * @return the header template
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected Template getHeaderTemplate() throws IOException {
    Template template = AppInit.getFmConfig().getTemplate(getTemplateFolder() + "/"
        + this.headerTemplate);
    return template;
  }

  // ERx TODO : This method should be overridden in the sub class. Should not be
  // ERxPrescription in the base class.
  // That way it will not be usable for other eRequests. It will always go into
  // ERxPrescription folder, even for other
  // requests which are not eRx

  /**
   * Gets the template folder.
   *
   * @return the template folder
   */
  protected String getTemplateFolder() {
    return "/ERxPrescription";
  }

}