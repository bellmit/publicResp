package com.insta.hms.insurance;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class RemittanceForm extends ActionForm {
  private FormFile remittance_metadata;

  public FormFile getRemittance_metadata() {
    return remittance_metadata;
  }

  public void setRemittance_metadata(FormFile remittance_metadata) {
    this.remittance_metadata = remittance_metadata;
  }

}
