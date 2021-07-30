/* $Id$ */

package com.insta.hms.extension.accounting.zoho.books.model;

/**
 * This class is used to make an object for email templates.
 */

public class Emailtemplate {

  /** The selected. */
  private boolean selected = false;

  /** The name. */
  private String name = "";

  /** The email template id. */
  private String emailTemplateId = "";

  /**
   * set is selected.
   * 
   * @param selected
   *          Whether the email template is selected or not.
   */

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  /**
   * get is selected.
   * 
   * @return Returns true if the email template is selected else return false.
   */

  public boolean isSelected() {
    return this.selected;
  }

  /**
   * set the name.
   * 
   * @param name
   *          Name of the email template.
   */

  public void setName(String name) {
    this.name = name;
  }

  /**
   * get the name.
   * 
   * @return Returns the email template name.
   */

  public String getName() {
    return this.name;
  }

  /**
   * set the email template id.
   * 
   * @param emailTemplateId
   *          ID of the email template.
   */

  public void setEmailTemplateId(String emailTemplateId) {
    this.emailTemplateId = emailTemplateId;
  }

  /**
   * get the email template id.
   * 
   * @return Returns the email template id.
   */

  public String getEmailTemplateId() {
    return this.emailTemplateId;
  }
}
