/**
 *
 */
package com.insta.hms.Registration;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FtlHelper;
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

/**
 * @author lakshmi.p
 *
 */
public class PatientStatsReportFtlHelper extends FtlHelper {

	private Configuration cfg;

	public PatientStatsReportFtlHelper(Configuration cfg) {
		this.cfg = cfg;
	}

	public byte[] getSummaryReport(Connection con, Map params, Object out)
		throws SQLException, IOException, TemplateException, DocumentException {

		String format = (String) params.get("format");
		String groupBy = (String) params.get("groupBy");
		java.sql.Date fromDate = (java.sql.Date) params.get("fromDate");
		java.sql.Date toDate = (java.sql.Date) params.get("toDate");
		String centerClause = (String) params.get("centerClause");

		List<BasicDynaBean> l = ChargeDAO.getPatientCountGrouped(con, fromDate, toDate, groupBy, centerClause);
		Map resultMap = ConversionUtils.listBeanToMapMapMapNumeric(l, groupBy, "visit_type", "patient_gender","count");

		List<String> categories = new ArrayList();
		categories.addAll(resultMap.keySet());
		Collections.sort(categories, new ConversionUtils.ByTotalTotal(resultMap));

		params.put("result", resultMap);
		params.put("categories", categories);
		params.put("curDateTime", DataBaseUtil.timeStampFormatter.format(new java.util.Date()) );

		Template t = cfg.getTemplate("PatientStatsSummaryReport.ftl");

		return getFtlReport(t, params, format, out);
	}

	public byte[] getTrendReport(Connection con, Map params, Object out)
		throws SQLException, IOException, TemplateException, DocumentException {

		String format = (String) params.get("format");
		String groupBy = (String) params.get("groupBy");
		String trendPeriod = (String) params.get("trendPeriod");
		java.sql.Date fromDate = (java.sql.Date) params.get("fromDate");
		java.sql.Date toDate = (java.sql.Date) params.get("toDate");
		String centerClause = (String) params.get("centerClause");

		List<BasicDynaBean> l = ChargeDAO.getPatientCountTrend(con, fromDate, toDate, trendPeriod, groupBy, centerClause);
		Map resultMap = ConversionUtils.listBeanToMapMapMapNumeric(l, groupBy, "period","patient_gender", "count");

		List<String> categories = new ArrayList();
		categories.addAll(resultMap.keySet());
		Collections.sort(categories, new ConversionUtils.ByTotalTotal(resultMap));

		List<String>periods = new ArrayList();
		Map innerTotals = (Map)resultMap.get("_total");
		if (innerTotals != null)
			periods.addAll(innerTotals.keySet());
		Collections.sort(periods);

		params.put("result", resultMap);
		params.put("categories", categories);
		params.put("periods", periods);
		params.put("curDateTime", DataBaseUtil.timeStampFormatter.format(new java.util.Date()) );

		Template t = cfg.getTemplate("PatientStatsTrendReport.ftl");

		return getFtlReport(t, params, format, out);
	}

	public byte[] getVisitTrendReport(Connection con, Map params, Object out)
		throws SQLException, IOException, TemplateException, DocumentException {

		String format = (String) params.get("format");
		String groupBy = (String) params.get("groupBy");
		String trendPeriod = (String) params.get("trendPeriod");
		java.sql.Date fromDate = (java.sql.Date) params.get("fromDate");
		java.sql.Date toDate = (java.sql.Date) params.get("toDate");
		String centerClause = (String) params.get("centerClause");

		List<BasicDynaBean> l = ChargeDAO.getPatientVisitCountTrend(con, fromDate, toDate, trendPeriod, groupBy, centerClause);
		Map resultMap = ConversionUtils.listBeanToMapMapMapNumeric(l, groupBy, "period","revisit", "count");

		List<String> categories = new ArrayList();
		categories.addAll(resultMap.keySet());
		Collections.sort(categories, new ConversionUtils.ByTotalTotal(resultMap));

		List<String>periods = new ArrayList();
		Map innerTotals = (Map)resultMap.get("_total");
		if (innerTotals != null)
			periods.addAll(innerTotals.keySet());
		Collections.sort(periods);

		params.put("result", resultMap);
		params.put("categories", categories);
		params.put("periods", periods);
		params.put("curDateTime", DataBaseUtil.timeStampFormatter.format(new java.util.Date()) );

		Template t = cfg.getTemplate("PatientVisitStatsTrendReport.ftl");

		return getFtlReport(t, params, format, out);
	}

	public byte[] getAdmitDischargeTrendReport(Connection con, Map params, Object out)
	throws SQLException, IOException, TemplateException, DocumentException {

	String format = (String) params.get("format");
	String groupBy = (String) params.get("groupBy");
	String trendPeriod = (String) params.get("trendPeriod");
	java.sql.Date fromDate = (java.sql.Date) params.get("fromDate");
	java.sql.Date toDate = (java.sql.Date) params.get("toDate");
	String centerClause = (String) params.get("centerClause");

	List<BasicDynaBean> l = ChargeDAO.getPatientAdmitDischargeCountTrend(con, fromDate, toDate, trendPeriod, groupBy, centerClause);
	Map resultMap = ConversionUtils.listBeanToMapMapMapNumeric(l, groupBy, "period","state","count");

	List<String> categories = new ArrayList();
	categories.addAll(resultMap.keySet());
	Collections.sort(categories, new ConversionUtils.ByTotalTotal(resultMap));

	List<String>periods = new ArrayList();
	Map innerTotals = (Map)resultMap.get("_total");
	if (innerTotals != null)
		periods.addAll(innerTotals.keySet());
	Collections.sort(periods);

	params.put("result", resultMap);
	params.put("categories", categories);
	params.put("periods", periods);
	params.put("curDateTime", DataBaseUtil.timeStampFormatter.format(new java.util.Date()) );

	Template t = cfg.getTemplate("PatientAdmitDischargeTrendReport.ftl");

	return getFtlReport(t, params, format, out);
}

}
