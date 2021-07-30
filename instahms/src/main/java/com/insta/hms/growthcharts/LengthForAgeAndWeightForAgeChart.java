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
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc

/**
 * The Class LengthForAgeAndWeightForAgeChart.
 *
 * @author nikunj.s
 */
public class LengthForAgeAndWeightForAgeChart extends AbstractGrowthCharts {

  /** The growth chart desc DAO. */
  GenericDAO growthChartDescDAO = new GenericDAO("growth_chart_descriptions");

  /** The chart range. */
  static String chartRange = "0-2";

  /** The chart type. */
  static String chartType = null;

  /** The growth chart desc bean. */
  private static BasicDynaBean growthChartDescBean = null;


  /**
   * Instantiates a new length for age and weight for age chart.
   *
   * @param chartType the chart type
   * @param growthChartDescBean the growth chart desc bean
   */
  public LengthForAgeAndWeightForAgeChart(String chartType, BasicDynaBean growthChartDescBean) {
    this(growthChartDescBean, chartRange, chartType);
    this.chartType = chartType;
    this.growthChartDescBean = growthChartDescBean;
    this.chartRange = (String) growthChartDescBean.get("chart_range");
  }

  /**
   * Instantiates a new length for age and weight for age chart.
   *
   * @param growthChartDescBean the growth chart desc bean
   * @param chartRange the chart range
   * @param chartType the chart type
   */
  public LengthForAgeAndWeightForAgeChart(BasicDynaBean growthChartDescBean, String chartRange,
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

    XYSeriesCollection lengthReferenceDataSet = null;
    XYSeriesCollection lengthPatientDataSet = null;
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
          if (chartType.equalsIgnoreCase("L")) {
            lengthReferenceDataSet = new XYSeriesCollection();
            List<BasicDynaBean> referenceDataList =
                GrowthChartsDAO.getReferenceDataSet(mrNo, gender, chartType, "Month", chartRange);

            for (String per : percentailList) {
              final XYSeries refSeries = new XYSeries(true);
              for (BasicDynaBean referenceData : referenceDataList) {
                if (referenceData == null || referenceData.get(per) == null) {
                  continue;
                }
                refSeries.add(Double.parseDouble(referenceData.get("month").toString()),
                    Double.parseDouble(referenceData.get(per).toString()));
              }
              lengthReferenceDataSet.addSeries(refSeries);
            }

            lengthPatientDataSet = new XYSeriesCollection();
            List<BasicDynaBean> patientDataList =
                GrowthChartsDAO.getPatientDataSet(mrNo, gender, chartType, "Month", chartRange);
            final XYSeries patSeries = new XYSeries(true);

            for (BasicDynaBean patientData : patientDataList) {
              patSeries.add(
                  Double.parseDouble(patientData.get("age_in_months").toString()),
                  null != patientData.get("height") ? Double.parseDouble(patientData.get("height")
                      .toString()) : Double.parseDouble("0"));
            }
            lengthPatientDataSet.addSeries(patSeries);
          } else if (chartType.equalsIgnoreCase("WA")) {
            weightReferenceDataSet = new XYSeriesCollection();
            List<BasicDynaBean> referenceDataList =
                GrowthChartsDAO.getReferenceDataSet(mrNo, gender, chartType, "Month", chartRange);

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
                GrowthChartsDAO.getPatientDataSet(mrNo, gender, chartType, "Month", chartRange);
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
          createChart(patientDetails, lengthReferenceDataSet, lengthPatientDataSet,
              weightReferenceDataSet, weightPatientDataSet);
    }
    return chart;
  }

  /**
   * Creates the chart.
   *
   * @param patientDetails the patient details
   * @param lengthReferenceDataSet the length reference data set
   * @param lengthPatientDataSet the length patient data set
   * @param weightReferenceDataSet the weight reference data set
   * @param weightPatientDataSet the weight patient data set
   * @return the j free chart
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  private static JFreeChart createChart(BasicDynaBean patientDetails,
      XYDataset lengthReferenceDataSet, XYDataset lengthPatientDataSet,
      XYDataset weightReferenceDataSet, XYDataset weightPatientDataSet) throws IOException,
      SQLException, ParseException {

    Integer secaxisStart = (Integer) growthChartDescBean.get("second_axis_start");
    Integer secaxisEnd = (Integer) growthChartDescBean.get("second_axis_end");
    // create subplot 1...
    final NumberAxis laxis2 = new NumberAxis("Length (cm)"); // Y axis
    laxis2.setAutoRangeIncludesZero(false);
    laxis2.setRange(secaxisStart, secaxisEnd);
    laxis2.setStandardTickUnits(NumberAxis.createStandardTickUnits());
    laxis2.setTickUnit(new NumberTickUnit(5));
    laxis2.setTickLabelFont(new Font("Tahoma", Font.BOLD, 12));

    XYLineAndShapeRenderer patientLineRenderer = rendererPatientLine(lengthPatientDataSet);
    XYLineAndShapeRenderer referanceLineRenderer = rendererReferanceLines(lengthReferenceDataSet);
    final XYPlot plot1 = new XYPlot(lengthReferenceDataSet, null, laxis2, referanceLineRenderer);
    plot1.setDataset(1, lengthPatientDataSet);
    plot1.setRenderer(1, patientLineRenderer);
    plot1.setRangeAxis(1, laxis2);
    Stroke st = new BasicStroke(1.0f);
    plot1.setRangeGridlinesVisible(true);
    plot1.setBackgroundPaint(Color.WHITE);
    plot1.setDomainGridlinesVisible(true);
    plot1.setDomainGridlinePaint(Color.BLUE);
    plot1.setDomainMinorGridlinePaint(Color.BLACK);
    plot1.setRangeGridlinePaint(Color.BLUE);
    plot1.setDomainGridlineStroke(st);
    plot1.setRangeGridlineStroke(st);
    plot1.setDomainGridlineStroke(st);
    plot1.setDomainMinorGridlinesVisible(true);
    plot1.setRangeGridlinePaint(Color.BLUE);
    plot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT, true);

    // create subplot 2...
    final NumberAxis waxis2 = new NumberAxis("Weight (Kg)"); // Y axis
    Integer yaxisStart = (Integer) growthChartDescBean.get("y_axis_start");
    Integer yaxisEnd = (Integer) growthChartDescBean.get("y_axis_end");
    waxis2.setAutoRangeIncludesZero(false);
    waxis2.setRange(yaxisStart, yaxisEnd);
    waxis2.setStandardTickUnits(NumberAxis.createStandardTickUnits());
    waxis2.setTickUnit(new NumberTickUnit(1));
    waxis2.setTickLabelFont(new Font("Tahoma", Font.BOLD, 12));

    XYLineAndShapeRenderer wpatientLineRenderer = rendererPatientLine(weightPatientDataSet);
    XYLineAndShapeRenderer wreferanceLineRenderer = rendererReferanceLines(weightReferenceDataSet);
    final XYPlot plot2 = new XYPlot(weightReferenceDataSet, null, waxis2, wreferanceLineRenderer);
    plot2.setDataset(1, weightPatientDataSet);
    plot2.setRenderer(1, wpatientLineRenderer);
    plot2.setRangeAxis(1, waxis2);
    plot2.setRangeGridlinesVisible(true);
    plot2.setBackgroundPaint(Color.WHITE);
    plot2.setDomainGridlinesVisible(true);
    plot2.setDomainGridlinePaint(Color.BLUE);
    plot2.setDomainMinorGridlinePaint(Color.BLACK);
    plot2.setRangeGridlinePaint(Color.BLUE);
    plot2.setDomainGridlineStroke(st);
    plot2.setRangeGridlineStroke(st);
    plot2.setDomainGridlineStroke(st);
    plot2.setDomainMinorGridlinesVisible(true);
    plot2.setRangeGridlinePaint(Color.BLUE);
    plot2.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT, true);

    // parent plot...
    final NumberAxis axis1 = new NumberAxis("Age (Months)");// X-Axis
    Integer xaxisStart = (Integer) growthChartDescBean.get("x_axis_start");
    Integer xaxisEnd = (Integer) growthChartDescBean.get("x_axis_end");
    axis1.setAutoRangeIncludesZero(false);
    axis1.setRange(xaxisStart, xaxisEnd);
    axis1.setStandardTickUnits(NumberAxis.createStandardTickUnits());
    axis1.setTickUnit(new NumberTickUnit(1));
    axis1.setMinorTickCount(4);
    axis1.setMinorTickMarksVisible(true);
    axis1.setTickLabelFont(new Font("Tahoma", Font.BOLD, 6));

    final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(axis1);
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
