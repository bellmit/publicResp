package com.insta.hms.adminmasters;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

// TODO: Auto-generated Javadoc
/**
 * The Class OpFormEditForm.
 */
public class OpFormEditForm extends ActionForm {

  /** The pdf content. */
  private FormFile pdfContent;

  /**
   * Gets the pdf content.
   *
   * @return the pdf content
   */
  public FormFile getPdfContent() {
    return pdfContent;
  }

  /**
   * Sets the pdf content.
   *
   * @param v the new pdf content
   */
  public void setPdfContent(FormFile v) {
    pdfContent = v;
  }

}
