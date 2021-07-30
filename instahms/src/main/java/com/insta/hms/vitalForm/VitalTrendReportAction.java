/**
 *
 */
package com.insta.hms.vitalForm;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna.t
 *
 */
public class VitalTrendReportAction extends DispatchAction {

	static Set<String> labelKeys = new HashSet<>();
	static Set<Timestamp> datesKeys = new HashSet<>();
	static double minValue;
	static double maxValue;


	public ActionForward getScreen (ActionMapping mapping, ActionForm from,
			HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException {

		List l = genericVitalFormDAO.getVitalLabels(req.getParameter("mrno"));
		req.setAttribute("vitalLabels", l);
		return mapping.findForward("getScreen");
	}

	public ActionForward getView (ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws IOException, SQLException, ParseException {
		String[] testVlaues = (String[])((req.getParameterMap()).get("vitalValues"));
		Date fromDate = DateUtil.parseDate(req.getParameter("fromDate"));
		Date toDate = DateUtil.parseDate(req.getParameter("toDate"));
		List l = genericVitalFormDAO.getVitalValues(req.getParameter("mrno"), testVlaues, fromDate, toDate, null);
		Map map = ConversionUtils.listBeanToMapMapBean(l, "param_label", "date_time");
		Map datesMap = ConversionUtils.listBeanToMapListBean(l, "date_time");
		req.setAttribute("vitalLabels", map.keySet());
		req.setAttribute("vitalDates", datesMap.keySet());
		req.setAttribute("vitalValuesMap", map);
		return mapping.findForward("getView");
	}

	public ActionForward getChart (ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws IOException, SQLException, ParseException {

		final XYDataset dataset = createDataset(req);
        final JFreeChart chart = createChart(dataset);

        chart.setBackgroundPaint(new Color(249, 231, 236));
    	OutputStream out = res.getOutputStream();
		res.setContentType("image/jpg");
		ChartUtilities.writeChartAsJPEG (out, chart, 1200, 400);
		out.flush();
		out.close();
        return null;
	}

	private XYDataset createDataset( HttpServletRequest req) throws IOException,SQLException, ParseException{

		String[] testValues = (String[])((req.getParameterMap()).get("vitalValues"));
		Date fromDate = DateUtil.parseDate(req.getParameter("fromDate"));
		Date toDate = DateUtil.parseDate(req.getParameter("toDate"));
		List l = genericVitalFormDAO.getVitalValues(req.getParameter("mrno"), testValues, fromDate, toDate, null);
		Map map = ConversionUtils.listBeanToMapMapBean(l, "param_label", "date_time");
		Map datesMap = ConversionUtils.listBeanToMapListBean(l, "date_time");
		Pattern pattern = Pattern.compile("(\\d*,?\\d+){0,999}\\.?\\d+");
		Matcher matcher = null;
        labelKeys = map.keySet();
        datesKeys = datesMap.keySet();
        boolean first = true;
        final TimeSeriesCollection dataset = new TimeSeriesCollection();
		for (String lkey: labelKeys) {
	        final TimeSeries series = new TimeSeries(lkey,  org.jfree.data.time.Second.class);
	        for (Timestamp dkey: datesKeys) {
				BasicDynaBean bn = ( (BasicDynaBean)( ((Map)( map.get(lkey) )).get(dkey) ) );
				String sreportValue = ( bn != null ) ? (String)bn.get("param_value") : null;

				if ( sreportValue != null && !sreportValue.equals("") && dkey != null) {
					matcher = pattern.matcher(sreportValue);
					if ( matcher.matches() ) {

						sreportValue = sreportValue.replaceAll(",", "");
						Double reportValue= (sreportValue != null && !sreportValue.equals("")) ?Double.parseDouble(sreportValue) : null;
						if (reportValue != null) {
							if (first) {
								minValue = Double.parseDouble(reportValue.toString());
								maxValue = Double.parseDouble(reportValue.toString());
							}else{
								if (minValue > Double.parseDouble(reportValue.toString()) )
									minValue = Double.parseDouble(reportValue.toString());
								if (maxValue < Double.parseDouble(reportValue.toString()))
									maxValue = Double.parseDouble(reportValue.toString());
							}
							series.addOrUpdate(new Second(dkey), Double.parseDouble(reportValue.toString()));
						}
					}
				}
			}
			dataset.addSeries(series);
		}
        return dataset;
	}

	 private JFreeChart createChart(final XYDataset dataset) {

	        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
		            "Vital Trend Report Chart",      // chart title
		            "Vital Labels",                      // x axis label
		            "Report Values",                      // y axis label
		            dataset,                  // data
		            true,                     // include legend
		            true,                     // tooltips
		            false                     // urls
		        );

	        chart.setBackgroundPaint(Color.white);

	        final XYPlot plot = chart.getXYPlot();
	        plot.setBackgroundPaint(Color.lightGray);
	        plot.setDomainGridlinePaint(Color.white);
	        plot.setRangeGridlinePaint(Color.white);

	        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	        plot.setRenderer(renderer);
	        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
	        plot.setNoDataMessage("No Data Avialable");
	        DateAxis dateaxis = (DateAxis)plot.getDomainAxis();
	        dateaxis.setAutoRange(true);
	        dateaxis.setDateFormatOverride(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"));

	        return chart;
	    }

	 private LegendItemCollection createLegendItems() {
	        LegendItemCollection result = new LegendItemCollection();
	        for (Timestamp key: datesKeys){
	        	LegendItem item1 = new LegendItem(key.toString(), key.toString(), key.toString(), key.toString(),
		                new Rectangle(10, 10), new GradientPaint(0.0f, 0.0f,
		                new Color(16, 89, 172), 0.0f, 0.0f, new Color(201, 201, 244)));
		        result.add(item1);
	        }
	        return result;
	    }


}
