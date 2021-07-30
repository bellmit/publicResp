package com.insta.hms.common;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class UploadForm extends ActionForm {

  private FormFile uploadFile;

  public FormFile getUploadFile() {
    return uploadFile;
  }

  public void setUploadFile(FormFile formFile) {
    this.uploadFile = formFile;
  }
}
