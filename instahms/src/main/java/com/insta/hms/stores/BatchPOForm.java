/**
 *
 */
package com.insta.hms.stores;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

/**
 * @author krishna
 *
 */
public class BatchPOForm extends ActionForm {

  public FormFile csv_file;
  public String deptId;
  public FormFile getCsv_file() {
    return csv_file;
  }
  public void setCsv_file(FormFile csv_file) {
    this.csv_file = csv_file;
  }
  public String getDeptId() {
    return deptId;
  }
  public void setDeptId(String deptId) {
    this.deptId = deptId;
  }

}
