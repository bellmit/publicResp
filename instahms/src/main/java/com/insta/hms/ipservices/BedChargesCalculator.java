package com.insta.hms.ipservices;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.orders.OrderBO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class BedChargesCalculator.
 *
 * @author sirisha.rachkonda
 */
public class BedChargesCalculator extends GenericJob {
  
  /** The Constant log. */
  static final Logger log = LoggerFactory.getLogger(BedChargesCalculator.class);

  /** (non-Javadoc)
   * @see org.springframework.scheduling.quartz
   * .QuartzJobBean#executeInternal(org.quartz.JobExecutionContext)
   **/
  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    String schema = (String) jobContext.getJobDetail().getJobDataMap().get("schema");
    BasicDynaBean ipBedBean = null;
    GenericDAO ipbedDAO = new GenericDAO("ip_bed_details");
    IPBedDAO ipbedDetailsDao = new IPBedDAO();
    OrderBO order = new OrderBO();
    Connection con = null;
    boolean success = true;
    RequestContext.setConnectionDetails(new String[] { null, null, schema });
    Set<String> patientIdsUpdated = new HashSet<String>();

    try {
      con = DataBaseUtil.getConnection();
      List<BasicDynaBean> ipBedDetails = ipbedDetailsDao.getIpBedDetails(con);
      List<String> billNos = new ArrayList<String>();
      int idx = 0;
      for (BasicDynaBean ipBedDetail : ipBedDetails) {
        idx++;
        if (!billNos.contains((String) ipBedDetail.get("bill_no"))) {
          billNos.add((String) ipBedDetail.get("bill_no"));
        }

        ipBedBean = ipbedDAO.findByKey(con, "admit_id", ipBedDetail.get("admit_id"));
        ipBedBean.set("updated_date", DateUtil.getCurrentTimestamp());
        ipBedBean.set("end_date", DateUtil.getCurrentTimestamp());

        String patientId = (String) ipBedDetail.get("patient_id");
        order
            .setBillInfo(con, patientId, (String) ipBedDetail.get("bill_no"), false, "auto_update");

        // update ip_bed_details with the current date as end_date
        List<BasicDynaBean> vistBeds = IPBedDAO.getVisitBeds(con, patientId, "O");
        if (vistBeds.size() > 0) {
          Map keys = new HashMap();
          for (BasicDynaBean visitBedBean : vistBeds) {
            keys = new HashMap();
            keys.put("end_date", DateUtil.getCurrentTimestamp());
            ipbedDAO.update(con, keys, "admit_id", visitBedBean.get("admit_id"));
          }
        }

        if (patientIdsUpdated.add(patientId)) {
          log.info("Bed Charges updating for " + patientId);
          try {
            success &= order.recalculateBedCharges(con, patientId) == null;
          } catch (Exception exp) {
            log.error("Failed to update Bed Charges for: " + patientId, exp);
          }
        }

        // Update sponsor amounts for a batch of 50 bills.
        if (idx == 50) {
          for (String billNo : billNos) {
            try {
              if (billNo != null && !billNo.equals("")) {
                BillDAO.resetTotalsOrReProcess(billNo);
              }
            } catch (Exception exp) {
              log.error("Failed to reset sponsor claim totals after bed charges update, "
                  + "for bill no: "
                  + (String) ipBedDetail.get("bill_no"));
            }
          }
          billNos = new ArrayList<String>();
          idx -= 50;
        }
      }

      // Update sponsor amounts for last batch of bills.
      for (String billNo : billNos) {
        try {
          if (billNo != null && !billNo.equals("")) {
            BillDAO.resetTotalsOrReProcess(billNo);
          }
        } catch (Exception exp) {
          log.error("Failed to reset sponsor claim totals after bed charges update, for bill no: "
              + billNo);
          throw new JobExecutionException(exp.getMessage());
        }
      }

    } catch (Exception exp) {
      log.error("Failed to update bed charges ", exp);
      throw new JobExecutionException(exp.getMessage());
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }
}
