package com.insta.hms.growthcharts;

import com.bob.hms.common.DataBaseUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

// TODO: Auto-generated Javadoc

/**
 * The Class GrowthChartsDAO.
 *
 * @author nikunj.s
 */
public class GrowthChartsDAO {

  /** The Constant REFERENCE_DATA_FOR_CHART. */
  public static final String REFERENCE_DATA_FOR_CHART =
      " SELECT #, per_3, per_5, per_10, per_25, per_50, "
      + " per_75, per_90, per_95, per_97 FROM growth_chart_reference_data ";

  /**
   * Gets the reference data set.
   *
   * @param mrNO the mr NO
   * @param gender the gender
   * @param chartType the chart type
   * @param chartIn the chart in
   * @param chartRange the chart range
   * @return the reference data set
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static List getReferenceDataSet(String mrNO, String gender, String chartType,
      String chartIn, String chartRange) throws SQLException, IOException {

    Connection con = null;
    PreparedStatement ps = null;
    String monthRange = "0 and 24";
    String yearRange = "2 AND 20";
    if (chartRange.equals("0-5")) {
      monthRange = "0 and 60";
      yearRange = "0 and 5";
    }
    String query = REFERENCE_DATA_FOR_CHART;
    try {
      con = DataBaseUtil.getConnection();

      if (chartIn.equals("Month")) {
        if (chartType.equalsIgnoreCase("L") || chartType.equalsIgnoreCase("WA")
            || chartType.equalsIgnoreCase("HC")) {
          query = query.replace("#", "month");
          query +=
              " WHERE chart_type = ? AND sex = ? AND  month between " + monthRange
                  + " ORDER BY month ";
        } else if (chartType.equalsIgnoreCase("WL")) {
          query = query.replace("#", "length");
          query += " WHERE chart_type = ? AND sex = ? ORDER BY length ";
        }
      } else if (chartIn.equals("Year")) {
        if (chartType.equalsIgnoreCase("S") || chartType.equalsIgnoreCase("WA")
            || chartType.equalsIgnoreCase("BMI")) {
          query = query.replace("#", "DISTINCT ON (month) month");

          query +=
              " WHERE chart_type = ? AND sex = ? AND year between " + yearRange
                  + " ORDER BY month ";

        } else if (chartType.equalsIgnoreCase("WS")) {
          query = query.replace("#", "stature");
          query += " WHERE chart_type = ? AND sex = ? ORDER BY stature ";
        }
      }

      ps = con.prepareStatement(query);
      ps.setString(1, chartType);
      ps.setString(2, gender);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }


  /** The Constant PATIENT_DATA_FOR_CHART. */
  public static final String PATIENT_DATA_FOR_CHART =
      " SELECT # FROM patient_growth_chart_data_view ";

  /**
   * Gets the patient data set.
   *
   * @param mrNo the mr no
   * @param gender the gender
   * @param chartType the chart type
   * @param chartIn the chart in
   * @param chartRange the chart range
   * @return the patient data set
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static List getPatientDataSet(String mrNo, String gender, String chartType,
      String chartIn, String chartRange)
      throws SQLException, IOException {

    Connection con = null;
    PreparedStatement ps = null;
    String query = PATIENT_DATA_FOR_CHART;
    String monthRange = "0 and 24";
    String yearRange = "2 AND 20";
    if (chartRange.equals("0-5")) {
      monthRange = "0 and 60";
      yearRange = "0 and 5";
    }
    try {
      con = DataBaseUtil.getConnection();

      if (chartIn.equals("Month")) {
        if (chartType.equalsIgnoreCase("L")) {
          query = query.replace("#", " age_in_months, height ");
        } else if (chartType.equalsIgnoreCase("WA")) {
          query = query.replace("#", " age_in_months, weight ");
        } else if (chartType.equalsIgnoreCase("HC")) {
          query = query.replace("#", " age_in_months, head_circumference ");
        } else if (chartType.equalsIgnoreCase("WL")) {
          query = query.replace("#", " height, weight ");
        }
        query +=
            " WHERE mr_no = ? AND age_in_months between " + monthRange + " ORDER BY age_in_months ";
      } else if (chartIn.equals("Year")) {
        if (chartType.equalsIgnoreCase("S")) {
          query = query.replace("#", " age_in_months, height ");
        } else if (chartType.equalsIgnoreCase("WA")) {
          query = query.replace("#", " age_in_months, weight ");
        } else if (chartType.equalsIgnoreCase("BMI")) {
          query = query.replace("#", " age_in_months, bmi ");
        } else if (chartType.equalsIgnoreCase("WS")) {
          query = query.replace("#", " age_in_years as age_in_years, height, weight ");
        }
        query +=
            " WHERE mr_no = ? AND age_in_years between " + yearRange + " ORDER BY age_in_years ";
      }

      ps = con.prepareStatement(query);
      ps.setString(1, mrNo);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
