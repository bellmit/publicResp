package com.insta.hms.insurance.patientsponsorsapproval;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

/**
 * The Class SponsorApprovalDocForm.
 */
public class SponsorApprovalDocForm extends ActionForm {

  /** The sponsor approval id. */
  String sponsor_approval_id;
  
  /** The attachment extension. */
  String attachment_extension;
  
  /** The file name. */
  String file_name;
  
  /** The sponsor doc. */
  FormFile sponsor_doc;


  /**
   * Gets the sponsor approval id.
   *
   * @return the sponsor approval id
   */
  public String getSponsor_approval_id() {
    return sponsor_approval_id;
  }

  /**
   * Sets the sponsor approval id.
   *
   * @param sponsor_approval_id the new sponsor approval id
   */
  public void setSponsor_approval_id(String sponsor_approval_id) {
    this.sponsor_approval_id = sponsor_approval_id;
  }

  /**
   * Gets the attachment extension.
   *
   * @return the attachment extension
   */
  public String getAttachment_extension() {
    return attachment_extension;
  }

  /**
   * Sets the attachment extension.
   *
   * @param attachment_extension the new attachment extension
   */
  public void setAttachment_extension(String attachment_extension) {
    this.attachment_extension = attachment_extension;
  }

  /**
   * Gets the file name.
   *
   * @return the file name
   */
  public String getFile_name() {
    return file_name;
  }

  /**
   * Sets the file name.
   *
   * @param file_name the new file name
   */
  public void setFile_name(String file_name) {
    this.file_name = file_name;
  }

  /**
   * Gets the sponsor doc.
   *
   * @return the sponsor doc
   */
  public FormFile getSponsor_doc() {
    return sponsor_doc;
  }

  /**
   * Sets the sponsor doc.
   *
   * @param sponsor_doc the new sponsor doc
   */
  public void setSponsor_doc(FormFile sponsor_doc) {
    this.sponsor_doc = sponsor_doc;
  }


}
