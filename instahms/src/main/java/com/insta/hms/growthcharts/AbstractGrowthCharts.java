package com.insta.hms.growthcharts;

import com.insta.hms.common.GenericDAO;
import org.apache.commons.beanutils.BasicDynaBean;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.TextAnchor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractGrowthCharts.
 *
 * @author nikunj.s
 */
public abstract class AbstractGrowthCharts {

  /** The Constant percentail. */
  public static final Set<String> percentail = new LinkedHashSet<String>() {
    {
      add("per_3");
      add("per_5");
      add("per_10");
      add("per_25");
      add("per_50");
      add("per_75");
      add("per_90");
      add("per_95");
      add("per_97");
    }
  };

  /** The chart range. */
  private static String chartRange = null;

  /** The chart type. */
  private static String chartType = null;

  /** The growth chart desc bean. */
  private static BasicDynaBean growthChartDescBean = null;

  /**
   * Instantiates a new abstract growth charts.
   *
   * @param growthChartDescBean the growth chart desc bean
   * @param chartRange the chart range
   * @param chartType the chart type
   */
  public AbstractGrowthCharts(BasicDynaBean growthChartDescBean,
      String chartRange, String chartType) {
    this.chartRange = chartRange;
    this.chartType = chartType;
    this.growthChartDescBean = growthChartDescBean;
  }

  /**
   * Generate chart.
   *
   * @param mrNo the mr no
   * @return the j free chart
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public abstract JFreeChart generateChart(String mrNo) throws IOException, SQLException,
      ParseException;

  /**
   * Gets the single instance of AbstractGrowthCharts.
   *
   * @param chartType the chart type
   * @return single instance of AbstractGrowthCharts
   * @throws SQLException the SQL exception
   */
  public static AbstractGrowthCharts getInstance(String chartType) throws SQLException {
    AbstractGrowthCharts implClass = null;
    GenericDAO growthChartDescDAO = new GenericDAO("growth_chart_descriptions");
    BasicDynaBean growthChartDescbean = null;
    if ("L,WA".equalsIgnoreCase(chartType)) { // Length for Age And Weight for Age 0 to 2 years
      growthChartDescbean = growthChartDescDAO.findByKey("chart_type", "L,WA");
      implClass = new LengthForAgeAndWeightForAgeChart(chartType, growthChartDescbean);
    } else if ("HC".equalsIgnoreCase(chartType)) { // Head Circumference for Age
      growthChartDescbean = growthChartDescDAO.findByKey("chart_type", "HC");
      implClass = new HeadCircumferenceForAgeChart(chartType, growthChartDescbean);
    } else if ("WL".equalsIgnoreCase(chartType)) { // Weight for Length
      growthChartDescbean = growthChartDescDAO.findByKey("chart_type", "WL");
      implClass = new WeightForLengthChart(chartType, growthChartDescbean);
    } else if ("S,WA".equalsIgnoreCase(chartType)) { // Weight for Age
      growthChartDescbean = growthChartDescDAO.findByKey("chart_type", "S,WA");
      implClass = new StatureForAgeAndWeightForAge20Chart(chartType, growthChartDescbean);
      // BMI = (weight(kg)/ (Stature(cm) * Stature(cm)) *10000)
    } else if ("BMI".equalsIgnoreCase(chartType)) { 
      growthChartDescbean = growthChartDescDAO.findByKey("chart_type", "BMI");
      implClass = new BMIForAge(chartType, growthChartDescbean);
    } else if ("WS".equalsIgnoreCase(chartType)) { // Weight for Stature
      growthChartDescbean = growthChartDescDAO.findByKey("chart_type", "WS");
      implClass = new WeightForStatureChart(chartType, growthChartDescbean);
    }
    return implClass;
  }

  /**
   * Renderer referance lines.
   *
   * @param referenceDataSet the reference data set
   * @return the XY line and shape renderer
   */
  public static XYLineAndShapeRenderer rendererReferanceLines(XYDataset referenceDataSet) {

    XYLineAndShapeRenderer referanceLineRenderer = new XYLineAndShapeRenderer(true, false);
    for (int i = 0; i <= 8; i++) {
      referanceLineRenderer.setSeriesItemLabelGenerator(i, new ItemLabelGenerator());
      referanceLineRenderer.setSeriesItemLabelsVisible(i, true);
      referanceLineRenderer.setSeriesStroke(i, new BasicStroke(1.0f));
      referanceLineRenderer.setSeriesPaint(i, Color.gray);
    }
    referanceLineRenderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
        ItemLabelAnchor.OUTSIDE10, TextAnchor.CENTER_LEFT));
    referanceLineRenderer.setItemLabelFont(new Font("Tahoma", Font.BOLD, 8));
    referanceLineRenderer.setItemLabelPaint(Color.blue);
    referanceLineRenderer.setBaseShapesVisible(false);
    referanceLineRenderer.setDrawOutlines(true);
    referanceLineRenderer.findRangeBounds(referenceDataSet);

    return referanceLineRenderer;
  }

  /**
   * Renderer patient line.
   *
   * @param patientDataSet the patient data set
   * @return the XY line and shape renderer
   */
  public static XYLineAndShapeRenderer rendererPatientLine(XYDataset patientDataSet) {

    XYLineAndShapeRenderer patientLineRenderer = new XYLineAndShapeRenderer(true, false);
    patientLineRenderer.setSeriesStroke(0, new BasicStroke(3.0f));
    patientLineRenderer.setSeriesPaint(0, Color.red);
    patientLineRenderer.setBaseShapesVisible(true);
    patientLineRenderer.setDrawOutlines(true);
    patientLineRenderer.findRangeBounds(patientDataSet);

    return patientLineRenderer;
  }

  /**
   * The Class ItemLabelGenerator.
   */
  public static class ItemLabelGenerator implements XYItemLabelGenerator {

    /**
     * (non-Javadoc)
     * 
     * @see org.jfree.chart.labels.XYItemLabelGenerator#generateLabel(org.jfree.data.xy.XYDataset,
     * int, int)
     */
    public String generateLabel(XYDataset arg0, int row, int column) {
      String label = null;
      // System.out.println("Row:"+row +"===="+"Column:"+column);
      List<String> labelList =
          Arrays.asList(((String) growthChartDescBean.get("standard_deviation")).split(","));
      Map<Integer, String> labelMap = new HashMap<Integer, String>();
      Integer count = 0;
      for (String s : labelList) {
        labelMap.put(count, s);
        count++;
      }
      if (row >= 0
          && column == (chartRange.equals("0-5") ? (Integer) growthChartDescBean.get("x_axis_end")
              : (chartType.equals("WS") ? 45 : ((chartType.equals("WL") ? 59 : (chartRange
                  .equals("0-2") ? 24 : 18)))))) {
        label = labelMap.get(row);
      }
      return label;
    }
  }


}
