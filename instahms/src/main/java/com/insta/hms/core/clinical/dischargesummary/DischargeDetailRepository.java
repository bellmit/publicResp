package com.insta.hms.core.clinical.dischargesummary;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author anup vishwas.
 *
 */

@Repository
public class DischargeDetailRepository extends GenericRepository {

  public DischargeDetailRepository() {
    super("dis_detail");
  }

  private static final String GET_FORM_FIELD_VALUES =
      " SELECT f.field_id, f.caption, dd.field_value, "
          + " f.no_of_lines, dh.username, fh.form_caption as template_name "
          + " FROM dis_detail dd " + " JOIN dis_header dh on (dh.docid=dd.doc_id) "
          + " JOIN fields f ON (f.field_id = dd.field_id) "
          + " JOIN form_header fh ON (fh.form_id = dh.form_id)"
          + " WHERE dd.doc_id = ? ORDER BY f.displayorder";

  public List<BasicDynaBean> getFormFieldsValues(int docId) {

    return DatabaseHelper.queryToDynaList(GET_FORM_FIELD_VALUES, new Object[] {docId});
  }

}
