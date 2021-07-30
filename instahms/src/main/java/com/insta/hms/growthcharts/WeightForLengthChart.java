package com.insta.hms.growthcharts;

import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.GenericDAO;
import org.apache.commons.beanutils.BasicDynaBean;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.Color;
import java.awt.Font;
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
 * The Class WeightForLengthChart.
 *
 * @author nikunj.s
 */
public class WeightForLengthChart extends AbstractGrowthCharts {

  /** The chart range. */
  static String chartRange = "0-2";

  /** The chart type. */
  static String chartType = null;

  /** The growth chart desc bean. */
  private static BasicDynaBean growthChartDescBean = null;

  /**
   * Instantiates a new weight for length chart.
   *
   * @param chartType the chart type
   * @param growthChartDescBean the growth chart desc bean
   */
  public WeightForLengthChart(String chartType, BasicDynaBean growthChartDescBean) {
    this(growthChartDescBean, chartRange, chartType);
    this.chartType = chartType;
    this.growthChartDescBean = growthChartDescBean;
    this.chartRange = (String) growthChartDescBean.get("chart_range");
  }

  /**
   * Instantiates a new weight for length chart.
   *
   * @param growthChartDescBean the growth chart desc bean
   * @param chartRange the chart range
   * @param chartType the chart type
   */
  public WeightForLengthChart(BasicDynaBean growthChartDescBean, 
      String chartRange, String chartType) {
    super(growthChartDescBean, chartRange, chartType);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.GrowthCharts.AbstractGrowthCharts#generateChart(java.lang.String)
   */
  @Override
  public JFreeChart generateChart(String mrNo) throws IOException, SQLException, ParseException {

    XYSeriesCollection referenceDataSet = null;
    XYSeriesCollection patientDataSet = null;
    JFreeChart chart = null;

    if (null != mrNo && !mrNo.equals("")) {

      BasicDynaBean patientDetails = PatientDetailsDAO.getPatientGeneralDetailsBean(mrNo);
      String gender = (String) patientDetails.get("patient_gender");

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
        referenceDataSet = new XYSeriesCollection();
        List<BasicDynaBean> referenceDataList =
            GrowthChartsDAO.getReferenceDataSet(mrNo, gender, chartType, "Month", chartRange);

        for (String per : percentailList) {
          final XYSeries refSeries = new XYSeries(true);
          for (BasicDynaBean referenceData : referenceDataList) {
            if (referenceData == null || referenceData.get(per) == null) {
              continue;
            }
            refSeries.add(Double.parseDouble(referenceData.get("length").toString()),
                Double.parseDouble(referenceData.get(per).toString()));
          }
          referenceDataSet.addSeries(refSeries);
        }

        patientDataSet = new XYSeriesCollection();
        List<BasicDynaBean> patientDataList =
            GrowthChartsDAO.getPatientDataSet(mrNo, gender, chartType, "Month", chartRange);
        final XYSeries patSeries = new XYSeries(true);

        for (BasicDynaBean patientData : patientDataList) {
          patSeries.add(
              (null != patientData.get("height") ? Double.parseDouble(patientData.get("height")
                  .toString()) : Double.parseDouble("0")),
              (null != patientData.get("weight") ? Double.parseDouble(patientData.get("weight")
                  .toString()) : Double.parseDouble("0")));
        }
        patientDataSet.addSeries(patSeries);

        chart = createChart(patientDetails, referenceDataSet, patientDataSet);
      }
    }
    return chart;
  }

  /**
   * Creates the chart.
   *
   * @param patientDetails the patient details
   * @param referenceDataSet the reference data set
   * @param patientDataSet the patient data set
   * @return the j free chart
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  private static JFreeChart createChart(BasicDynaBean patientDetails, XYDataset referenceDataSet,
      XYDataset patientDataSet) throws IOException, SQLException, ParseException {
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
    chart =
        ChartFactory.createXYLineChart(header, "Length (cm)", "", null, PlotOrientation.VERTICAL,
            false, true, false);
    chart.setBackgroundPaint(Color.WHITE);

    final XYPlot plot = (XYPlot) chart.getXYPlot();

    final NumberAxis axis1 = (NumberAxis) plot.getDomainAxis(); // X axis
    Integer xaxisStart = (Integer) growthChartDescBean.get("x_axis_start");
    Integer xaxisEnd = (Integer) growthChartDescBean.get("x_axis_end");
    axis1.setAutoRangeIncludesZero(true);
    axis1.setRange(xaxisStart, xaxisEnd);
    axis1.setStandardTickUnits(NumberAxis.createStandardTickUnits());
    axis1.setTickUnit(new NumberTickUnit(5));
    axis1.setTickLabelFont(new Font("Tahoma", Font.BOLD, 6));

    final NumberAxis axis2 = (NumberAxis) plot.getRangeAxis(); // Y axis
    axis2.setLabel("Weight (cm)");
    axis2.setAutoRangeIncludesZero(false);
    Integer yaxisStart = (Integer) growthChartDescBean.get("y_axis_start");
    Integer yaxisEnd = (Integer) growthChartDescBean.get("y_axis_end");
    axis2.setRange(yaxisStart, yaxisEnd);
    axis2.setStandardTickUnits(NumberAxis.createStandardTickUnits());
    axis2.setTickUnit(new NumberTickUnit(1));
    axis2.setTickLabelFont(new Font("Tahoma", Font.BOLD, 12));

    plot.setDomainAxis(0, axis1);
    plot.setRangeAxis(1, axis2);
    plot.setRangeGridlinesVisible(true);
    plot.setBackgroundPaint(Color.WHITE);
    plot.setDomainGridlinesVisible(true);
    plot.setDomainGridlinePaint(Color.BLUE);
    plot.setRangeGridlinePaint(Color.BLUE);
    plot.setDataset(0, referenceDataSet);
    plot.setDataset(1, patientDataSet);

    XYLineAndShapeRenderer referanceLineRenderer = rendererReferanceLines(referenceDataSet);
    plot.setRenderer(0, referanceLineRenderer);

    XYLineAndShapeRenderer patientLineRenderer = rendererPatientLine(patientDataSet);
    plot.setRenderer(1, patientLineRenderer);

    return chart;
  }

}
