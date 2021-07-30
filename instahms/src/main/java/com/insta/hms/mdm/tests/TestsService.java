
package com.insta.hms.mdm.tests;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * The Class TestsService.
 */
@Service
public class TestsService extends MasterService {

  /**
   * Instantiates a new tests service.
   *
   * @param tr the tr
   * @param tv the tv
   */
  public TestsService(TestsRepository tr, TestsValidator tv) {
    super(tr, tv);
  }
  
  /**
   * Gets the test details.
   *
   * @param testId the test id
   * @param bedType the bed type
   * @param orgId the org id
   * @return the test details
   */
  public BasicDynaBean getTestDetails(String testId, String bedType, String orgId) {
    return ((TestsRepository) getRepository()).getTestDetails(testId, bedType, orgId);
  }

  /**
   * Gets the test details.
   *
   * @param testId the test id
   * @param bedType the bed type
   * @param orgId the org id
   * @param centerId the center id
   * @return the test details
   */
  public BasicDynaBean getTestDetails(String testId, String bedType, String orgId, int centerId) {
    return ((TestsRepository) getRepository()).getTestDetails(testId, bedType, orgId, centerId);
  }

  /**
   * Gets the test details.
   *
   * @param testId the test id
   * @param orgId  the org id
   * @return the test details
   */
  public List<BasicDynaBean> getAllBedTypeTestDetails(String testId, String orgId) {
    return ((TestsRepository) getRepository()).getAllBedTypeTestDetails(testId, orgId);
  }

  /**
   * Calculate expt rpt ready time.
   *
   * @param orderTime the order time
   * @param testId the test id
   * @param centerId the center id
   * @return the timestamp
   */
  public Timestamp calculateExptRptReadyTime(Timestamp orderTime, String testId, int centerId) {
    Timestamp exptRptReadyTime = null;
    Float conductionTurnAroundTimehours = 0f;
    Time conductionStartTime = null;
    String processingDays = "";
    BasicDynaBean basicDynaBean = null;
    List<BasicDynaBean> list = 
        ((TestsRepository) getRepository()).getTurnAroundTimeDetailsChain(testId, centerId);
    if (list != null && !list.isEmpty() && list.size() > 0) {
      BigDecimal logisticTurnAroundTimehours = BigDecimal.ZERO;
      int count = 0;
      for (Iterator iterator = list.iterator(); iterator.hasNext();) {
        basicDynaBean = (BasicDynaBean) iterator.next();
        if (basicDynaBean.get("logistics_tat_hours") != null) {
          logisticTurnAroundTimehours = logisticTurnAroundTimehours
              .add((BigDecimal) basicDynaBean.get("logistics_tat_hours"));
        } else if (count < (list.size() - 1)) {
          return null;
        } else {
          break;
        }
        count++;

      }
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(orderTime);
      calendar.add(Calendar.SECOND, 
          (int) (Math.ceil(logisticTurnAroundTimehours.floatValue() * 60 * 60)));
      Timestamp reachTime = new Timestamp(calendar.getTime().getTime());
      if (basicDynaBean.get("conduction_tat_hours") != null
          && basicDynaBean.get("conduction_start_time") != null
          && basicDynaBean.get("processing_days") != null) {
        conductionTurnAroundTimehours = 
            ((BigDecimal) basicDynaBean.get("conduction_tat_hours")).floatValue();
        conductionStartTime = (Time) basicDynaBean.get("conduction_start_time");
        processingDays = (String) basicDynaBean.get("processing_days");
        if (processingDays != null && !"".equals(processingDays)
            && !"XXXXXXX".equals(processingDays) && conductionStartTime != null) {
          Timestamp cndStarttime = getConductionStartDateTime(reachTime, processingDays,
              conductionStartTime);
          if (cndStarttime != null && conductionTurnAroundTimehours != null) {
            calendar.setTime(cndStarttime);
            calendar.add(Calendar.SECOND,
                (int) (Math.ceil(conductionTurnAroundTimehours.floatValue() * 60 * 60)));
            exptRptReadyTime = new Timestamp(calendar.getTime().getTime());
          }
        }
      }
    }
    return exptRptReadyTime;
  }

  /**
   * Gets the conduction start date time.
   *
   * @param reachTime the reach time
   * @param processingDays the processing days
   * @param conductionStartTime the conduction start time
   * @return the conduction start date time
   */
  public Timestamp getConductionStartDateTime(Timestamp reachTime, String processingDays,
      Time conductionStartTime) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(reachTime);
    int reachDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
    Integer nextDay = null;
    int maxCount = 0;
    Timestamp conductionStrTime = Timestamp
        .valueOf(reachTime.toString().split(" ")[0] + " " + conductionStartTime.toString());
    int day = reachDay;

    while (true) {

      if (processingDays.charAt(day) != 'X') {
        if (maxCount == 0 && reachTime.getTime() < conductionStrTime.getTime()) {
          nextDay = day;
          break;
        } else if (maxCount != 0) {
          nextDay = day;
          break;
        }
      }
      day++;
      maxCount++;
      if (maxCount > 7) {
        break;
      }
      day %= 7;
    }
    if (nextDay != null) {
      int noOfDays = maxCount;
      calendar.setTime(conductionStrTime);
      calendar.add(Calendar.DATE, noOfDays);
      conductionStrTime = new Timestamp(calendar.getTime().getTime());

    } else {
      conductionStrTime = null;
    }
    return conductionStrTime;
  }

  /**
   * Checks if is outsource test.
   *
   * @param testId the test id
   * @param centerId the center id
   * @return true, if is outsource test
   */
  public boolean isOutsourceTest(String testId, int centerId) {
    return ((TestsRepository) getRepository()).isOutsourceTest(testId, centerId);
  }

  /** The Constant UPDATE_TEST_DEPT_TOKENS. */
  private static final String UPDATE_TEST_DEPT_TOKENS = 
      "UPDATE test_dept_tokens SET token_number=coalesce(token_number, 0)+1 "
      + " WHERE dept_id=? and center_id=? RETURNING token_number";

  /**
   * Gets the token.
   *
   * @param deptId the dept id
   * @param centerId the center id
   * @return the token
   */
  public Integer getToken(String deptId, int centerId) {
    return DatabaseHelper.getInteger(UPDATE_TEST_DEPT_TOKENS, new Object[] { deptId, centerId });
  }
}
