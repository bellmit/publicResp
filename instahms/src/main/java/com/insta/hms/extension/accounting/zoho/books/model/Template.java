/* $Id$ */

package com.insta.hms.extension.accounting.zoho.books.model;

/**
 * This class is used to created a template object.
 */

public class Template {

  /** The template name. */
  private String templateName = "";

  /** The template id. */
  private String templateId = "";

  /** The template type. */
  private String templateType = "";

  /**
   * set the template name.
   * 
   * @param templateName
   *          Name of the template.
   */

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  /**
   * get the template name.
   * 
   * @return Returns the name of the template.
   */

  public String getTemplateName() {
    return templateName;
  }

  /**
   * set the template id.
   * 
   * @param templateId
   *          ID of the template.
   */

  public void setTemplateId(String templateId) {
    this.templateId = templateId;
  }

  /**
   * get the template id.
   * 
   * @return Returns the ID of the template.
   */

  public String getTemplateId() {
    return templateId;
  }

  /**
   * set the template type.
   * 
   * @param templateType
   *          Type of the template.
   */

  public void setTemplateType(String templateType) {
    this.templateType = templateType;
  }

  /**
   * get the template type.
   * 
   * @return Returns the type of the template.
   */

  public String getTemplateType() {
    return templateType;
  }
}
