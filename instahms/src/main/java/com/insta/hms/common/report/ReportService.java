package com.insta.hms.common.report;

import com.bob.hms.common.Constants;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.StdReportDesc;
import com.insta.hms.common.StdReportDescJsonProvider;
import com.insta.hms.common.StdReportDescProvider;
import com.insta.hms.common.StdReportDescXmlProvider;
import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


@Service
public class ReportService {
  private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

  @LazyAutowired
  private CustomReportsRepository customReportsRepository;

  private static final ResultSetExtractor<List<String>> FIRST_VALUE_EXTRACTOR =
      new ResultSetExtractor<List<String>>() {
        @Override
        public List<String> extractData(ResultSet resultSet)
            throws SQLException {
          List<String> result = new ArrayList<>();
          while (resultSet.next()) {
            result.add(resultSet.getString(1));
          }
          return result;
        }
      };

  /**
   * Gets report desc.
   *
   * @param reportDescName   the report desc name
   * @param descProviderName the desc provider name
   * @return the report desc
   * @throws Exception the exception
   */
  public StdReportDesc getReportDesc(String reportDescName, String descProviderName)
      throws Exception {
    StdReportDescProvider provider = null;
    if (StringUtils.isEmpty(descProviderName) && reportDescName.contains(".srxml")) {
      // use the srxml digester to get the report desc
      provider = new StdReportDescXmlProvider();
    } else if (StringUtils.isEmpty(descProviderName) && reportDescName.contains(".srjs")) {
      provider = new StdReportDescJsonProvider();
    } else {
      provider = (StdReportDescProvider) Class.forName(descProviderName).newInstance();
    }
    return provider.getReportDesc(reportDescName);
  }

  /**
   * Gets custom report desc.
   *
   * @param reportName the report name
   * @return the custom report desc
   * @throws UnsupportedEncodingException the unsupported encoding exception
   * @throws SQLException                 the sql exception
   */
  public StdReportDesc getCustomReportDesc(String reportName)
      throws UnsupportedEncodingException, SQLException {

    Pattern pat = Pattern.compile("\\.srjs");
    String[] splitSrx = pat.split(reportName);
    reportName = splitSrx[0];
    DynaBean report = null;
    StdReportDesc newdesc = null;


    report = customReportsRepository.findByKey(Constants.REPORT_NAME, reportName);
    String reportMetadata = report.get(Constants.REPORT_METADATA).toString();
    StdReportDescJsonProvider newp = new StdReportDescJsonProvider();
    newdesc = newp.getReportDescForString(reportMetadata);
    return newdesc;
  }

  List<String> getFilterValues(String reportName, String providerName, String fieldName,
      Boolean isCustom,
      Integer limit)
      throws Exception {

    StdReportDesc reportDesc;
    if (Boolean.TRUE.equals(isCustom)) {
      reportDesc = getCustomReportDesc(reportName);
    } else {
      reportDesc = getReportDesc(reportName, providerName);
    }
    StdReportDesc.Field field = reportDesc.getField(fieldName);
    if (StringUtils.isBlank(field.getAllowedValuesQuery())) {
      logger.error("No query found for {} : {}}", reportName, fieldName);
      return Collections.emptyList();
    }
    StringBuilder query = new StringBuilder(field.getAllowedValuesQuery());
    Object[] queryParams = null;
    if (limit != null && !limit.equals(0)) {
      query.append(" limit ?");
      queryParams = new Object[] {limit};
    }
    return DatabaseHelper.queryWithCustomMapper(query.toString(), queryParams, FIRST_VALUE_EXTRACTOR
    );

  }


}
