package com.insta.hms.scheduledreport;

import java.util.List;
import java.util.Map;

/*
 * A email report provider is responsible for listing the reports available
 * for emailing, and also to generate the report when requested for. The key
 * for the report is the ID, which is unique for that provider alone. It is
 * up to the provider to use a convenient key that it can use to locate the
 * actual report.
 *
 * Known classes implementing this interface:
 *  BuiltinEmailReportProvider
 *  FavouriteEmailReportProvider
 *  CustomEmailReportProvider
 */
public interface EmailReportProviderInterface {

  public List listAvailableReports() throws Exception;

  public byte[] getReportBytes(String reportId, String format, Map params) throws Exception;

  public String getReportName(String reportId) throws Exception;
}
