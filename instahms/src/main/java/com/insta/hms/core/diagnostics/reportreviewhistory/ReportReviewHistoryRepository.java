package com.insta.hms.core.diagnostics.reportreviewhistory;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReportReviewHistoryRepository extends GenericRepository {

  public ReportReviewHistoryRepository() {
    super("report_review_history");
  }

  private static final String GET_REPORT_REVIEW_HISTORY =
      "SELECT reviewed_date, remarks, reviewed_by FROM report_review_history "
      + " WHERE entity_id=? AND is_test_doc=? "
      + " ORDER BY reviewed_date DESC";

  public List<?> getReportReviewHistory(int reportId, String isTestDoc) {
    return ConversionUtils.copyListDynaBeansToMap(DatabaseHelper
        .queryToDynaList(GET_REPORT_REVIEW_HISTORY, new Object[] {reportId, isTestDoc}));
  }
}
