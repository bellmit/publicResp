package com.insta.hms.core.clinical.order.testvisitreports;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class TestVisitReportsRepository.
 */
@Repository
public class TestVisitReportsRepository extends GenericRepository {

  /**
   * Instantiates a new test visit reports repository.
   */
  public TestVisitReportsRepository() {
    super("test_visit_reports");
  }
  
  private static final String TEST_DETAILS = "SELECT tvr.patient_id AS visit_id,"
      + " tp.prescribed_id AS presc_id, dd.category"
      + " FROM test_visit_reports tvr"
      + " LEFT JOIN tests_prescribed tp ON (tvr.report_id = tp.report_id)"
      + " LEFT JOIN diagnostics d ON (d.test_id = tp.test_id)"
      + " LEFT JOIN diagnostics_departments dd ON (dd.ddept_id = d.ddept_id)"
      + " WHERE conduction_type = 'i'";
  
  /**
   * Get sign off details.
   * 
   * @param reportIds the id
   * @return list
   */
  public List<BasicDynaBean> getTestDetails(String[] reportIds) {
    StringBuilder reportQueryBuilder = new StringBuilder(TEST_DETAILS);
    List<Object> queryParams = new ArrayList<>();
    if (reportIds != null) {
      reportQueryBuilder.append(" AND tvr.report_id IN (");
      boolean first = true;
      for (String reportId : reportIds) {
        if (!first) {
          reportQueryBuilder.append(",");
        }
        first = false;
        reportQueryBuilder.append('?');
        queryParams.add(Integer.parseInt(reportId));
      }
      reportQueryBuilder.append(")");
    }
    return DatabaseHelper.queryToDynaList(reportQueryBuilder.toString(), queryParams.toArray());
  }
}
