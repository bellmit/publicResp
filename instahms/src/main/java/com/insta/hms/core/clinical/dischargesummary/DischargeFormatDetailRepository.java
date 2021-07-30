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
public class DischargeFormatDetailRepository extends GenericRepository {

  public DischargeFormatDetailRepository() {
    super("discharge_format_detail");
  }

  private static final String GET_DOCUMENT_REPORT = "SELECT dfd.username, "
      + " dfd.report_file, dfd.pheader_template_id, df.template_caption as template_name "
      + " FROM discharge_format_detail dfd " + " JOIN discharge_format df using(format_id)"
      + " WHERE dfd.docid = ?";

  public List<BasicDynaBean> getDocumentReport(int docId) {

    return DatabaseHelper.queryToDynaList(GET_DOCUMENT_REPORT, new Object[] {docId});
  }

}
