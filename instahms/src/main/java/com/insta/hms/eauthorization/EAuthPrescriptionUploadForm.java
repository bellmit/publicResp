package com.insta.hms.eauthorization;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class EAuthPrescriptionUploadForm.
 */
public class EAuthPrescriptionUploadForm extends ActionForm {

  /**
   * The s preauth act id.
   */
  String[] s_preauth_act_id;

  /**
   * The attachment extension.
   */
  String[] attachment_extension;

  /**
   * The file name.
   */
  String[] file_name;

  /**
   * The activity file upload.
   */
  private List activity_file_upload = new ArrayList();

  /**
   * Gets the activity file upload.
   *
   * @return the activity file upload
   */
  public List getActivity_file_upload() {
    return activity_file_upload;
  }

  /**
   * Sets the activity file upload.
   *
   * @param index the index
   * @param file  the file
   */
  public void setActivity_file_upload(int index, FormFile file) {

    // formfile elements doesn't come in the order mentioned in jsp,
    // we will get randomly, first we may get 10th element then 20th and
    // then 1st etc.,
    // so first add null values if the index is greater than the elements of
    // the list.
    // when we get actual indexed element, replace the null with element.

    while (index > this.activity_file_upload.size()) {
      FormFile f = null;
      this.activity_file_upload.add(f);
    }
    if (index < this.activity_file_upload.size()) {
      this.activity_file_upload.set(index, file); // replace
    } else {
      this.activity_file_upload.add(index, file);
    }
  }

  /**
   * Gets the s preauth act id.
   *
   * @return the s preauth act id
   */
  public String[] getS_preauth_act_id() {
    return s_preauth_act_id;
  }

  /**
   * Sets the s preauth act id.
   *
   * @param s_preauth_act_id the new s preauth act id
   */
  public void setS_preauth_act_id(String[] s_preauth_act_id) {
    this.s_preauth_act_id = s_preauth_act_id;
  }

  /**
   * Gets the attachment extension.
   *
   * @return the attachment extension
   */
  public String[] getAttachment_extension() {
    return attachment_extension;
  }

  /**
   * Sets the attachment extension.
   *
   * @param attachment_extension the new attachment extension
   */
  public void setAttachment_extension(String[] attachment_extension) {
    this.attachment_extension = attachment_extension;
  }

  /**
   * Gets the file name.
   *
   * @return the file name
   */
  public String[] getFile_name() {
    return file_name;
  }

  /**
   * Sets the file name.
   *
   * @param file_name the new file name
   */
  public void setFile_name(String[] file_name) {
    this.file_name = file_name;
  }

}
