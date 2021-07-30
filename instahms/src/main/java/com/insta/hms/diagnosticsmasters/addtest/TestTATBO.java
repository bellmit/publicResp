package com.insta.hms.diagnosticsmasters.addtest;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class TestTATBO.
 */
public class TestTATBO {
  
  /** The test TATDAO. */
  private TestTATDAO testTATDAO = new TestTATDAO("diag_tat_center_master");

  /**
   * Calculate expt rpt ready time.
   *
   * @param orderTime the order time
   * @param testId the test id
   * @param centerId the center id
   * @return the timestamp
   * @throws Exception the exception
   */
  public Timestamp calculateExptRptReadyTime(Timestamp orderTime, String testId, int centerId)
      throws Exception {
    Timestamp exptRptReadyTime = null;
    Float conductionTAThours = 0f;
    Time conductionStartTime = null;
    String processingDays = "";
    BasicDynaBean basicDynaBean = null;
    // List<String> destIds = testTATDAO.getOutsourceDestIds(center_id,
    // test_id);
    // if (destIds != null && !destIds.isEmpty() && destIds.size() > 0) {
    List<BasicDynaBean> list = TestTATDAO.getTATDetailsChain(testId, centerId);

    if (list != null && !list.isEmpty() && list.size() > 0) {
      BigDecimal logisticTAThours = BigDecimal.ZERO;
      int count = 0;
      for (Iterator iterator = list.iterator(); iterator.hasNext();) {
        basicDynaBean = (BasicDynaBean) iterator.next();
        if (basicDynaBean.get("logistics_tat_hours") != null) {
          logisticTAThours = logisticTAThours
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
      calendar.add(Calendar.SECOND, (int) (Math.ceil(logisticTAThours.floatValue() * 60 * 60)));
      Timestamp reachTime = new Timestamp(calendar.getTime().getTime());
      if (basicDynaBean.get("conduction_tat_hours") != null
          && basicDynaBean.get("conduction_start_time") != null
          && basicDynaBean.get("processing_days") != null) {
        conductionTAThours = ((BigDecimal) basicDynaBean.get("conduction_tat_hours")).floatValue();
        conductionStartTime = (Time) basicDynaBean.get("conduction_start_time");
        processingDays = (String) basicDynaBean.get("processing_days");
        if (processingDays != null && !"".equals(processingDays)
            && !"XXXXXXX".equals(processingDays) && conductionStartTime != null) {
          Timestamp cndStarttime = getConductionStartDateTime(reachTime, processingDays,
              conductionStartTime);
          if (cndStarttime != null && conductionTAThours != null) {
            calendar.setTime(cndStarttime);
            calendar.add(Calendar.SECOND,
                (int) (Math.ceil(conductionTAThours.floatValue() * 60 * 60)));
            exptRptReadyTime = new Timestamp(calendar.getTime().getTime());
          }
        }
      }
      // }
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
    int inc = reachDay;
    while (true) {

      if (processingDays.charAt(inc) != 'X') {
        if (maxCount == 0 && reachTime.getTime() < conductionStrTime.getTime()) {
          nextDay = inc;
          break;
        } else if (maxCount != 0) {
          nextDay = inc;
          break;
        }
      }
      inc++;
      maxCount++;
      if (maxCount > 7) {
        break;
      }
      inc %= 7;
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
   * Gets the distinct TAT details.
   *
   * @param dtoList the dto list
   * @return the distinct TAT details
   */
  public List<BasicDynaBean> getDistinctTATDetails(List<BasicDynaBean> dtoList) {
    Map<String, String> sourceOutSourceMap = new HashMap<>();
    for (Iterator iterator = dtoList.iterator(); iterator.hasNext();) {
      BasicDynaBean basicDynaBean = (BasicDynaBean) iterator.next();

      String outsourceName = (String) basicDynaBean.get("outsource_name");
      String sourceCenterName = (String) basicDynaBean.get("center_name");
      if (sourceOutSourceMap.containsKey(sourceCenterName)) {
        if (outsourceName != null && !"".equals(outsourceName)) {
          sourceOutSourceMap.put(sourceCenterName,
              sourceOutSourceMap.get(sourceCenterName) + "," + outsourceName);
        }
      } else {
        sourceOutSourceMap.put(sourceCenterName, outsourceName);
      }
    }

    List<String> centers = new ArrayList<>();
    for (Iterator iterator = dtoList.iterator(); iterator.hasNext();) {
      BasicDynaBean basicDynaBean = (BasicDynaBean) iterator.next();

      if (!centers.contains(basicDynaBean.get("center_name"))) {
        centers.add((String) basicDynaBean.get("center_name"));
      } else {
        iterator.remove();
      }
      basicDynaBean.set("outsource_name", sourceOutSourceMap.get(basicDynaBean.get("center_name")));
    }
    return dtoList;
  }
}
