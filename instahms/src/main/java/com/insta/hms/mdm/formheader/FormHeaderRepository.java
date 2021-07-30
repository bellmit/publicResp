package com.insta.hms.mdm.formheader;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

/**
 * @author anup vishwas.
 *
 */

@Repository
public class FormHeaderRepository extends GenericRepository {

  public FormHeaderRepository() {
    super("form_header");
  }

  private static final String HEADER_DETAIL = "select "
      + " case when (fh.form_title is null or fh.form_title = '') then fh.form_caption else "
      + " form_title end as form_title "
      + " from form_header fh "
      + " join dis_header dh on dh.form_id=fh.form_id " 
      + " where dh.docid=? and dh.patient_id=?";

  public BasicDynaBean getFormHeaderDetail(int docId, String patientId) {

    return DatabaseHelper.queryToDynaBean(HEADER_DETAIL, new Object[] { docId, patientId });
  }
}
