package com.insta.hms.core.insurance;

import com.insta.hms.common.AppInit;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Map;

/**
 * The Class ClaimGeneratorHelper.
 *
 * @author lakshmi.p
 */
@Component
public class ClaimGeneratorHelper {

  /** The logger. */
  Logger logger = LoggerFactory.getLogger(ClaimGeneratorHelper.class);

  /**
   * Adds the claim header.
   *
   * @param stream the stream
   * @param ftlMap the ftl map
   * @param healthAuthority the health authority
   * @param isAccumed the is accumed
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void addClaimHeader(OutputStream stream, Map ftlMap, String healthAuthority,
      boolean isAccumed) throws IOException, TemplateException {
    Template template = null;
    if (isAccumed) {
      template = AppInit.getFmConfig().getTemplate("/Accumed/AccumedEclaimHeader.ftl");
    } else {
      template = AppInit.getFmConfig()
          .getTemplate("/Eclaim/" + healthAuthority.toLowerCase() + "/EclaimHeader.ftl");
    }
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
    Template template = null;
    template = AppInit.getFmConfig().getTemplate("/Eclaim/EclaimHeader.ftl");
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
  public void addClaimBody(OutputStream stream, Map ftlMap, String template)
      throws IOException, TemplateException {
    Template temp = null;
    temp = AppInit.getFmConfig().getTemplate(template);
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
  public void addClaimBody(OutputStream stream, Map ftlMap)
      throws IOException, TemplateException {
    addClaimBody(stream, ftlMap, "/Eclaim/EclaimBody.ftl");
  }

  /**
   * Adds the claim footer.
   *
   * @param stream the stream
   * @param ftlMap the ftl map
   * @param healthAuthority the health authority
   * @param isAccumed the is accumed
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void addClaimFooter(OutputStream stream, Map ftlMap, String healthAuthority,
      boolean isAccumed) throws IOException, TemplateException {
    Template template = null;
    if (isAccumed) {
      template = AppInit.getFmConfig().getTemplate("/Accumed/AccumedEclaimFooter.ftl");
    } else {
      template = AppInit.getFmConfig()
          .getTemplate("/Eclaim/" + healthAuthority.toLowerCase() + "/EclaimFooter.ftl");
    }
    writeToStream(template, stream, ftlMap);
  }

  /**
   * Adds the claim footer.
   *
   * @param stream the stream
   * @param ftlMap the ftl map
   * @param isAccumed the is accumed
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void addClaimFooter(OutputStream stream, Map ftlMap, boolean isAccumed)
      throws IOException, TemplateException {
    Template template = null;
    if (isAccumed) {
      template = AppInit.getFmConfig().getTemplate("/Accumed/AccumedEclaimFooter.ftl");
    } else {
      template = AppInit.getFmConfig().getTemplate("/Eclaim/EclaimFooter.ftl");
    }
    writeToStream(template, stream, ftlMap);
  }

  /**
   * Write to stream.
   *
   * @param template the template
   * @param stream the stream
   * @param ftlMap the ftl map
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void writeToStream(Template template, OutputStream stream, Map ftlMap)
      throws IOException, TemplateException {
    if (template == null) {
      return;
    }
    StringWriter swriter = new StringWriter();
    template.process(ftlMap, swriter);
    stream.write(swriter.toString().getBytes());
    stream.flush();
  }

}
