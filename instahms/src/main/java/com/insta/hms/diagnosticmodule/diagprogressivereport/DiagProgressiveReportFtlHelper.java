package com.insta.hms.diagnosticmodule.diagprogressivereport;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FtlHelper;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.lowagie.text.DocumentException;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class DiagProgressiveReportFtlHelper.
 */
public class DiagProgressiveReportFtlHelper extends FtlHelper {

  /** The cfg. */
  private Configuration cfg;

  /**
   * Instantiates a new diag progressive report ftl helper.
   *
   * @param cfg the cfg
   */
  public DiagProgressiveReportFtlHelper(Configuration cfg) {
    this.cfg = cfg;
  }

  /**
   * Gets the trend report.
   *
   * @param con the con
   * @param params the params
   * @param out the out
   * @return the trend report
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws DocumentException the document exception
   */
  public byte[] getTrendReport(Connection con, Map params, Object out)
      throws SQLException, IOException, TemplateException, DocumentException {

    java.sql.Date fromDate = (java.sql.Date) params.get("fromDate");
    java.sql.Date toDate = (java.sql.Date) params.get("toDate");
    Object val = null;
    List<BasicDynaBean> diagProgressiveReport = LaboratoryDAO.getDiagProgressiveDetails(fromDate,
        toDate);
    Map diagProgressiveMap = ConversionUtils.listBeanToMapMapTotalNumeric(diagProgressiveReport,
        "ddept_name", "period", "test_count");

    List<String> categories = new ArrayList();

    categories.addAll(diagProgressiveMap.keySet());
    Collections.sort(categories, new ConversionUtils.ByTotal(diagProgressiveMap));

    List<String> periods = new ArrayList();
    Map innerAmountTotals = (Map) diagProgressiveMap.get("_total");
    if (innerAmountTotals != null) {
      periods.addAll(innerAmountTotals.keySet());
    }
    Collections.sort(periods);
    Map diagProgressiveTestCountMap = ConversionUtils.listBeanToMapMapNumeric(diagProgressiveReport,
        "ddept_name", "period", "test_count");
    params.put("diagProgressiveTestCountResult", diagProgressiveTestCountMap);
    params.put("diagProgressiveResult", diagProgressiveMap);
    params.put("categories", categories);
    params.put("periods", periods);
    params.put("format", "pdf");
    Template template = cfg.getTemplate("DiagProgressiveTrendReport.ftl");
    params.put("curDateTime", DataBaseUtil.timeStampFormatter.format(new java.util.Date()));
    return getFtlReport(template, params, "pdf", out);

  }
}
