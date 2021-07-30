package com.insta.hms.growthcharts;

import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.GenericDAO;
import org.apache.commons.beanutils.BasicDynaBean;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc

/**
 * The Class StatureForAgeAndWeightForAge20Chart.
 *
 * @author nikunj.s
 */
public class StatureForAgeAndWeightForAge20Chart extends AbstractGrowthCharts {

  /** The chart range. */
  static String chartRange = "2-20";

  /** The chart type. */
  static String chartType = null;

  /** The growth chart desc bean. */
  private static BasicDynaBean growthChartDescBean = null;

  /**
   * Instantiates a new stature for age and weight for age 20 chart.
   *
   * @param chartType the chart type
   * @param growthChartDescBean the growth chart desc bean
   */
  public StatureForAgeAndWeightForAge20Chart(String chartType, BasicDynaBean growthChartDescBean) {
    this(growthChartDescBean, chartRange, chartType);
    this.chartType = chartType;
    this.growthChartDescBean = growthChartDescBean;
    this.chartRange = (String) growthChartDescBean.get("chart_range");
  }

  /**
   * Instantiates a new stature for age and weight for age 20 chart.
   *
   * @param growthChartDescBean the growth chart desc bean
   * @param chartRange the chart range
   * @param chartType the chart type
   */
  public StatureForAgeAndWeightForAge20Chart(BasicDynaBean growthChartDescBean, String chartRange,
      String chartType) {
    super(growthChartDescBean, chartRange, chartType);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.GrowthCharts.AbstractGrowthCharts#generateChart(java.lang.String)
   */
  @Override
  public JFreeChart generateChart(String mrNo) throws IOException, SQLException, ParseException {

    XYSeriesCollection statureReferenceDataSet = null;
    XYSeriesCollection staturePatientDataSet = null;
    XYSeriesCollection weightReferenceDataSet = null;
    XYSeriesCollection weightPatientDataSet = null;
    JFreeChart chart = null;

    if (null != mrNo && !mrNo.equals("")) {

      BasicDynaBean patientDetails = PatientDetailsDAO.getPatientGeneralDetailsBean(mrNo);
      String gender = (String) patientDetails.get("patient_gender");
      String[] chartTypes = chartType.split(",");

      for (String chartType : chartTypes) {
        if (null != chartType && !chartType.equals("")) {
          List<String> labelList =
              Arrays.asList(((String) growthChartDescBean.get("standard_deviation")).split(","));
          List<String> percentailList = new ArrayList<String>(percentail);
          for (String per : percentail) {
            if (percentailList.size() > labelList.size()) {
              percentailList.remove(percentailList.size() - 1);
            } else {
              break;
            }
          }
          if (chartType.equalsIgnoreCase("S")) {
            statureReferenceDataSet = new XYSeriesCollection();
            List<BasicDynaBean> referenceDataList =
                GrowthChartsDAO.getReferenceDataSet(mrNo, gender, chartType, "Year", chartRange);

            for (String per : percentailList) {
              final XYSeries refSeries = new XYSeries(true);
              for (BasicDynaBean referenceData : referenceDataList) {
                refSeries.add(Double.parseDouble(referenceData.get("month").toString()),
                    Double.parseDouble(referenceData.get(per).toString()));
              }
              statureReferenceDataSet.addSeries(refSeries);
            }

            staturePatientDataSet = new XYSeriesCollection();
            List<BasicDynaBean> patientDataList =
                GrowthChartsDAO.getPatientDataSet(mrNo, gender, chartType, "Year", chartRange);
            final XYSeries patSeries = new XYSeries(true);

            for (BasicDynaBean patientData : patientDataList) {
              patSeries.add(
                  Double.parseDouble(patientData.get("age_in_months").toString()),
                  null != patientData.get("height") ? Double.parseDouble(patientData.get("height")
                      .toString()) : Double.parseDouble("0"));
            }
            staturePatientDataSet.addSeries(patSeries);
          } else if (chartType.equalsIgnoreCase("WA")) {
            weightReferenceDataSet = new XYSeriesCollection();
            List<BasicDynaBean> referenceDataList =
                GrowthChartsDAO.getReferenceDataSet(mrNo, gender, chartType, "Year", chartRange);

            for (String per : percentailList) {
              final XYSeries refSeries = new XYSeries(true);
              for (BasicDynaBean referenceData : referenceDataList) {
                if (referenceData == null || referenceData.get(per) == null) {
                  continue;
                }
                refSeries.add(Double.parseDouble(referenceData.get("month").toString()),
                    Double.parseDouble(referenceData.get(per).toString()));
              }
              weightReferenceDataSet.addSeries(refSeries);
            }

            weightPatientDataSet = new XYSeriesCollection();
            List<BasicDynaBean> patientDataList =
                GrowthChartsDAO.getPatientDataSet(mrNo, gender, chartType, "Year", chartRange);
            final XYSeries patSeries = new XYSeries(true);

            for (BasicDynaBean patientData : patientDataList) {
              patSeries.add(
                  Double.parseDouble(patientData.get("age_in_months").toString()),
                  null != patientData.get("weight") ? Double.parseDouble(patientData.get("weight")
                      .toString()) : Double.parseDouble("0"));
            }
            weightPatientDataSet.addSeries(patSeries);
          }
        }
      }
      chart =
          createChart(patientDetails, statureReferenceDataSet, staturePatientDataSet,
              weightReferenceDataSet, weightPatientDataSet);
    }
    return chart;
  }

  /**
   * Creates the chart.
   *
   * @param patientDetails the patient details
   * @param statureReferenceDataSet the stature reference data set
   * @param staturePatientDataSet the stature patient data set
   * @param weightReferenceDataSet the weight reference data set
   * @param weightPatientDataSet the weight patient data set
   * @return the j free chart
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  private static JFreeChart createChart(BasicDynaBean patientDetails,
      XYDataset statureReferenceDataSet, XYDataset staturePatientDataSet,
      XYDataset weightReferenceDataSet, XYDataset weightPatientDataSet) throws IOException,
      SQLException, ParseException {

    Integer secaxisStart = (Integer) growthChartDescBean.get("second_axis_start");
    Integer secaxisEnd = (Integer) growthChartDescBean.get("second_axis_end");
    // create subplot 1...
    final NumberAxis saxisY = new NumberAxis("Stature (cm)"); // Y axis
    saxisY.setAutoRangeIncludesZero(false);
    saxisY.setRange(secaxisStart, secaxisEnd);
    saxisY.setStandardTickUnits(NumberAxis.createStandardTickUnits());
    saxisY.setTickUnit(new NumberTickUnit(5));
    saxisY.setTickLabelFont(new Font("Tahoma", Font.BOLD, 12));

    XYLineAndShapeRenderer patientLineRenderer = rendererPatientLine(staturePatientDataSet);
    XYLineAndShapeRenderer referanceLineRenderer = rendererReferanceLines(statureReferenceDataSet);
    final XYPlot plot1 = new XYPlot(statureReferenceDataSet, null, saxisY, referanceLineRenderer);
    plot1.setDataset(1, staturePatientDataSet);
    plot1.setRenderer(1, patientLineRenderer);
    plot1.setRangeAxis(1, saxisY);
    plot1.setRangeGridlinesVisible(true);
    plot1.setBackgroundPaint(Color.WHITE);
    plot1.setDomainGridlinesVisible(true);
    plot1.setDomainGridlinePaint(Color.BLUE);
    plot1.setDomainMinorGridlinePaint(Color.BLACK);
    Stroke st = new BasicStroke(1.0f);
    plot1.setRangeGridlineStroke(st);
    plot1.setDomainGridlineStroke(st);
    plot1.setDomainMinorGridlinesVisible(true);
    plot1.setRangeGridlinePaint(Color.BLUE);
    plot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT, true);

    // create subplot 2...
    final NumberAxis waxisY = new NumberAxis("Weight (Kg)"); // Y axis
    Integer yaxisStart = (Integer) growthChartDescBean.get("y_axis_start");
    Integer yaxisEnd = (Integer) growthChartDescBean.get("y_axis_end");
    waxisY.setAutoRangeIncludesZero(false);
    waxisY.setRange(yaxisStart, yaxisEnd);
    waxisY.setStandardTickUnits(NumberAxis.createStandardTickUnits());
    waxisY.setTickUnit(new NumberTickUnit(5));
    waxisY.setTickLabelFont(new Font("Tahoma", Font.BOLD, 12));

    XYLineAndShapeRenderer wpatientLineRenderer = rendererPatientLine(weightPatientDataSet);
    XYLineAndShapeRenderer wreferanceLineRenderer = rendererReferanceLines(weightReferenceDataSet);
    final XYPlot plot2 = new XYPlot(weightReferenceDataSet, null, waxisY, wreferanceLineRenderer);
    plot2.setDataset(1, weightPatientDataSet);
    plot2.setRenderer(1, wpatientLineRenderer);
    plot2.setRangeAxis(1, waxisY);
    plot2.setRangeGridlinesVisible(true);
    plot2.setBackgroundPaint(Color.WHITE);
    plot2.setDomainGridlinesVisible(true);
    plot2.setDomainGridlinePaint(Color.BLUE);
    plot2.setDomainMinorGridlinePaint(Color.BLACK);
    plot2.setRangeGridlinePaint(Color.BLUE);
    plot2.setDomainGridlineStroke(st);
    plot2.setRangeGridlineStroke(st);

    plot2.setDomainMinorGridlinesVisible(true);
    plot2.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT, true);

    // parent plot...
    final NumberAxis axisX = new NumberAxis("Age (Years)");// X-Axis
    Integer xaxisStart = (Integer) growthChartDescBean.get("x_axis_start");
    Integer xaxisEnd = (Integer) growthChartDescBean.get("x_axis_end");
    axisX.setAutoRangeIncludesZero(false);
    axisX.setRange(xaxisStart, xaxisEnd);
    axisX.setStandardTickUnits(NumberAxis.createStandardTickUnits());
    axisX.setMinorTickCount(4);
    axisX.setMinorTickMarksVisible(true);
    axisX.setTickMarkOutsideLength(8.5f);
    axisX.setNumberFormatOverride(new NumberFormat() {

      @Override
      public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
        BigDecimal bd = new BigDecimal(number / 12);
        return new StringBuffer(bd.toPlainString());
      }

      @Override
      public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
        BigDecimal bd = new BigDecimal(number / 12);
        return new StringBuffer(bd.toPlainString());
      }

      @Override
      public Number parse(String source, ParsePosition parsePosition) {
        return null;
      }

    });
    axisX.setTickUnit(new NumberTickUnit(12));
    axisX.setTickLabelFont(new Font("Tahoma", Font.BOLD, 6));

    final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(axisX);
    // plot.setDomainMinorGridlinePaint(Color.BLACK);
    // plot.setDomainMinorGridlinesVisible(true);
    // plot.setDomainGridlineStroke(s);

    plot.setGap(20.0);

    // add the subplots...
    plot.add(plot1, 1);
    plot.add(plot2, 1);
    plot.setOrientation(PlotOrientation.VERTICAL);

    // return a new chart containing the overlaid plot...
    Map keys = new HashMap<>();
    keys.put("chart_type", chartType);
    keys.put("gender", patientDetails.get("patient_gender"));
    BasicDynaBean growthChartDetailsBean = new GenericDAO("growth_chart_details").findByKey(keys);
    String title = (String) growthChartDetailsBean.get("chart_title");
    String[] str = title.split("MALE");
    String header = "";
    if (str.length > 1) {
      header = str[0] + "MALE \n" + str[1] + " " + patientDetails.get("full_name");
    } else if (str.length > 0) {
      header = str[0] + "MALE \n " + patientDetails.get("full_name");
    }
    JFreeChart chart = null;
    chart = new JFreeChart(header, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
    chart.setBackgroundPaint(Color.WHITE);

    return chart;
  }

}
