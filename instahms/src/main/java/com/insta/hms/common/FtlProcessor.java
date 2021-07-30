package com.insta.hms.common;

import com.insta.hms.common.ftl.FtlMethods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * The Class FtlProcessor.
 */
@Component
public class FtlProcessor {

  static Logger logger = LoggerFactory.getLogger(FtlProcessor.class);

  /**
   * Process.
   *
   * @param expression the expression
   * @param params     the params
   * @return the string
   */
  public String process(String expression, Map<String, Object> params) {
    return process(expression, params, true);
  }
  
  /**
   * Process.
   *
   * @param expression             the expression
   * @param params                 the params
   * @param useDefaultNumberFormat the use default number format
   * @return the string
   */
  public String process(String expression, Map<String, Object> params,
      boolean useDefaultNumberFormat) {
    StringWriter writer = new StringWriter();
    String value = null;
    if (useDefaultNumberFormat) {
      expression = "<#setting number_format=\"##.##\">\n" + expression;
    }
    try {
      Template template = new Template("expression", new StringReader(expression),
          new Configuration());
      params.put("maskSensitiveData", 
          new FtlMethods.FtlMethodMaskingSensitiveData());
      template.process(params, writer);
    } catch (TemplateException | ArithmeticException exception) {
      logger.error("", exception);
      return null;
    } catch (Exception exception) {
      logger.error("", exception);
      return null;
    }
    value = writer.toString().trim();
    return value;
  }

}
