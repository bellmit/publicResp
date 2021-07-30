package com.insta.hms.integration.insurance.submission;

import com.insta.hms.common.AppInit;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class XmlGeneratorHelper.
 */
public class XmlGeneratorHelper {

  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(XmlGeneratorHelper.class);

  /**
   * Adds the claim header.
   *
   * @param stream the stream
   * @param ftlMap the ftl map
   * @param healthAuthority the health authority
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void addClaimHeader(OutputStream stream, Map ftlMap, String healthAuthority)
      throws IOException, TemplateException {
    Template template = AppInit.getFmConfig().getTemplate(
        "/Eclaim/" + healthAuthority.toLowerCase() + "/EclaimHeader.ftl");
    writeToStream(template, stream, ftlMap);
  }

  /**
   * Adds the claim header.
   *
   * @param stream the stream
   * @param ftlMap the ftl map
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void addClaimHeader(OutputStream stream, Map ftlMap)
      throws IOException, TemplateException {
    Template template = AppInit.getFmConfig().getTemplate("/Eclaim/EclaimHeader.ftl");
    writeToStream(template, stream, ftlMap);
  }

  /**
   * Adds the claim body.
   *
   * @param stream the stream
   * @param ftlMap the ftl map
   * @param template the template
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void addClaimBody(OutputStream stream, Map ftlMap, String template) throws IOException,
      TemplateException {
    Template temp = AppInit.getFmConfig().getTemplate(template);
    writeToStream(temp, stream, ftlMap);
  }

  /**
   * Adds the claim body.
   *
   * @param stream the stream
   * @param ftlMap the ftl map
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void addClaimBody(OutputStream stream, Map ftlMap) throws IOException, TemplateException {
    addClaimBody(stream, ftlMap, "/Eclaim/EclaimBody.ftl");
  }

  /**
   * Adds the claim footer.
   *
   * @param stream the stream
   * @param ftlMap the ftl map
   * @param healthAuthority the health authority
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void addClaimFooter(OutputStream stream, Map ftlMap, String healthAuthority)
      throws IOException, TemplateException {
    Template template = AppInit.getFmConfig().getTemplate(
        "/Eclaim/" + healthAuthority.toLowerCase() + "/EclaimFooter.ftl");
    writeToStream(template, stream, ftlMap);
  }

  /**
   * Adds the claim footer.
   *
   * @param stream the stream
   * @param ftlMap the ftl map
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void addClaimFooter(OutputStream stream, Map ftlMap) 
      throws IOException, TemplateException {
    Template template = AppInit.getFmConfig().getTemplate("/Eclaim/EclaimFooter.ftl");
    writeToStream(template, stream, ftlMap);
  }

  /**
   * Write to stream.
   *
   * @param template the t
   * @param stream the stream
   * @param ftlMap the ftl map
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void writeToStream(Template template, OutputStream stream, Map ftlMap) throws IOException,
      TemplateException {
    if (template == null) {
      return;
    }
    StringWriter stringWriter = new StringWriter();
    template.process(ftlMap, stringWriter);
    stream.write(stringWriter.toString().getBytes());
    stream.flush();
  }

  /**
   * Adds the accumed claim header.
   *
   * @param stream the stream
   * @param ftlMap the ftl map
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void addAccumedClaimHeader(OutputStream stream, Map ftlMap) throws IOException,
      TemplateException {
    Template template = AppInit.getFmConfig().getTemplate("/Accumed/AccumedEclaimHeader.ftl");
    writeToStream(template, stream, ftlMap);
  }

  /**
   * Adds the accumed claim body.
   *
   * @param stream the stream
   * @param ftlMap the ftl map
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void addAccumedClaimBody(OutputStream stream, Map ftlMap) throws IOException,
      TemplateException {
    Template template = AppInit.getFmConfig().getTemplate("/Accumed/AccumedEclaimBody.ftl");
    writeToStream(template, stream, ftlMap);
  }

  /**
   * Adds the accumed claim footer.
   *
   * @param stream the stream
   * @param ftlMap the ftl map
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void addAccumedClaimFooter(OutputStream stream, Map ftlMap) throws IOException,
      TemplateException {
    Template template = AppInit.getFmConfig().getTemplate("/Accumed/AccumedEclaimFooter.ftl");
    writeToStream(template, stream, ftlMap);
  }
}
