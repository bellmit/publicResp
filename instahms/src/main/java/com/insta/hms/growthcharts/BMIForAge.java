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
 * The Class BMIForAge.
 *
 * @author nikunj.s
 */
public class BMIForAge extends AbstractGrowthCharts {

  /** The chart range. */
  static String chartRange = "2-20";

  /** The chart type. */
  static String chartType = null;

  /** The growth chart desc bean. */
  private static BasicDynaBean growthChartDescBean = null;

  /**
   * Instantiates a new BMI for age.
   *
   * @param chartType the chart type
   * @param growthChartDescBean the growth chart desc bean
   */
  public BMIForAge(String chartType, BasicDynaBean growthChartDescBean) {
    this(growthChartDescBean, chartRange, chartType);
    this.chartType = chartType;
    this.growthChartDescBean = growthChartDescBean;
    this.chartRange = (String) growthChartDescBean.get("chart_range");
  }

  /**
   * Instantiates a new BMI for age.
   *
   * @param growthChartDescBean the growth chart desc bean
   * @param chartRange the chart range
   * @param chartType the chart type
   */
  public BMIForAge(BasicDynaBean growthChartDescBean, String chartRange, String chartType) {
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
          referenceDataSet.addSeries(refSeries);
        }

        patientDataSet = new XYSeriesCollection();
        List<BasicDynaBean> patientDataList =
            GrowthChartsDAO.getPatientDataSet(mrNo, gender, chartType, "Year", chartRange);
        final XYSeries patSeries = new XYSeries(true);

        for (BasicDynaBean patientData : patientDataList) {
          patSeries.add(
              Double.parseDouble(patientData.get("age_in_months").toString()),
              null != patientData.get("bmi") ? Double
                  .parseDouble(patientData.get("bmi").toString()) : Double.parseDouble("0"));
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
        ChartFactory.createXYLineChart(header, "Age (Years)", "", null, PlotOrientation.VERTICAL,
            false, true, false);
    chart.setBackgroundPaint(Color.WHITE);

    final XYPlot plot = (XYPlot) chart.getXYPlot();

    final NumberAxis axis1 = (NumberAxis) plot.getDomainAxis();

    Integer xaxisStart = (Integer) growthChartDescBean.get("x_axis_start");
    Integer xaxisEnd = (Integer) growthChartDescBean.get("x_axis_end");
    axis1.setAutoRangeIncludesZero(true); // X axis
    axis1.setRange(xaxisStart, xaxisEnd);
    axis1.setStandardTickUnits(NumberAxis.createStandardTickUnits());
    axis1.setMinorTickCount(4);
    axis1.setMinorTickMarksVisible(true);
    axis1.setTickMarkOutsideLength(8.5f);
    axis1.setTickLabelFont(new Font("Tahoma", Font.BOLD, 6));
    axis1.setNumberFormatOverride(new NumberFormat() {

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

    axis1.setTickUnit(new NumberTickUnit(12));


    final NumberAxis axis3 = (NumberAxis) plot.getRangeAxis(); // Y axis
    axis3.setLabel("BMI (kg/m*m)");
    axis3.setAutoRangeIncludesZero(false);
    Integer yaxisStart = (Integer) growthChartDescBean.get("y_axis_start");
    Integer yaxisEnd = (Integer) growthChartDescBean.get("y_axis_end");
    axis3.setRange(yaxisStart, yaxisEnd);
    axis3.setStandardTickUnits(NumberAxis.createStandardTickUnits());
    axis3.setTickUnit(new NumberTickUnit(1));
    axis3.setTickLabelFont(new Font("Tahoma", Font.BOLD, 12));

    plot.setRangeAxis(1, axis3);
    plot.setRangeGridlinesVisible(true);
    plot.setDomainMinorGridlinesVisible(true);
    plot.setDomainMinorGridlinePaint(Color.BLACK);
    Stroke st = new BasicStroke(1.0f);
    plot.setDomainGridlineStroke(st);
    plot.setRangeGridlineStroke(st);
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
