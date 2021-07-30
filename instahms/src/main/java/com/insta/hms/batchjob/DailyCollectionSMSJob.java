package com.insta.hms.batchjob;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.messaging.MessageManager;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyCollectionSMSJob extends MessagingJob {

  public DailyCollectionSMSJob() {
    super("daily_collection");
  }

  private static Logger logger = LoggerFactory.getLogger(DailyCollectionSMSJob.class);

  private static final String GET_DETAILED_SUMMARY = "select "
      + " payment_mode,sum(t.amt), center_name ,center_id from"
      + " (" + " select r.payment_mode_id,r.amount as amt ,r.display_date as pdate, "
      + " hcm.center_name, hcm.center_id from receipts r"
      + " JOIN bill_receipts br ON r.receipt_id = br.receipt_no AND NOT r.is_deposit "
      + " LEFT JOIN bill b on(b.bill_no=br.bill_no)"
      + " LEFT JOIN patient_registration pr on(b.visit_id=pr.patient_id)"
      + " LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)"
      + " LEFT JOIN incoming_sample_registration insr ON (insr.incoming_visit_id = b.visit_id)"
      + " LEFT JOIN hospital_center_master hcm  ON "
      + " (hcm.center_id = coalesce(pr.center_id ,coalesce( prc.center_id,insr.center_id)))"
      + " where r.display_date::date > current_date - 2 AND r.display_date::date < current_date"
      + " and r.receipt_type != 'W' and r.receipt_id not in ("
      + "   select refund_receipt_id from receipt_refund_reference rrf "
      + "   join receipts r1 on r1.receipt_id = rrf.receipt_id AND r1.receipt_type = 'W')"
      + " union all" + " select payment_mode_id,amount as amt ,display_date as pdate ,"
      + " hcm.center_name, hcm.center_id" + " from receipts pd "
      + " LEFT JOIN counters c on(c.counter_id=pd.counter AND pd.is_deposit)"
      + " LEFT JOIN hospital_center_master hcm  ON(hcm.center_id = c.center_id)" + " where "
      + " display_date::date > current_date - 2 AND display_date::date < current_date  "
      + " union all" + " select payment_mode_id,amount as amt ,display_date as pdate, "
      + " hcm.center_name, hcm.center_id" + " FROM insurance_claim_receipt icr"
      + " JOIN counters c on c.counter_id = icr.counter"
      + " LEFT JOIN hospital_center_master hcm  ON(hcm.center_id = c.center_id)" + " where"
      + " display_date::date > current_date - 2 AND display_date::date < current_date" + " )as t"
      + " JOIN payment_mode_master pm ON (t.payment_mode_id = pm.mode_id)" + " where center_id=?"
      + " group by center_name,center_id,payment_mode";

  private static final String GET_CENTER_TOTAL_COLLECTION = "select "
      + " sum(t.amt), center_name ,center_id"
      + " from" + " (" + " select r.amount as amt, hcm.center_name, hcm.center_id"
      + " from receipts r"
      + " JOIN bill_receipts br ON r.receipt_id = br.receipt_no AND NOT r.is_deposit"
      + " LEFT JOIN bill b on(b.bill_no=br.bill_no)"
      + " LEFT JOIN patient_registration pr on(b.visit_id=pr.patient_id)"
      + " LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)"
      + " LEFT JOIN incoming_sample_registration insr ON (insr.incoming_visit_id = b.visit_id)"
      + " LEFT JOIN hospital_center_master hcm  ON "
      + " (hcm.center_id = coalesce(pr.center_id ,coalesce( prc.center_id,insr.center_id)))"
      + " where r.display_date::date > current_date - 2 AND r.display_date::date < current_date"
      + " and r.receipt_type != 'W' and r.receipt_id not in ("
      + "   select refund_receipt_id from receipt_refund_reference rrf "
      + "   join receipts r1 on r1.receipt_id = rrf.receipt_id AND r1.receipt_type = 'W')"
      + " union all" + " select amount as amt ,hcm.center_name, hcm.center_id"
      + " from receipts pd " + " LEFT JOIN counters c on(c.counter_id=pd.counter AND is_deposit)"
      + " LEFT JOIN hospital_center_master hcm  ON(hcm.center_id = c.center_id)" + " where "
      + " pd.display_date::date > current_date - 2 AND pd.display_date::date < current_date"
      + " AND is_deposit "
      + " union all" + " select amount as amt ,hcm.center_name, hcm.center_id"
      + " FROM insurance_claim_receipt icr" + " JOIN counters c on c.counter_id = icr.counter"
      + " LEFT JOIN hospital_center_master hcm  ON(hcm.center_id = c.center_id)" + " where"
      + " display_date::date > current_date - 2 AND display_date::date < current_date" + " )as t"
      + " group by center_name,center_id";

  /*
   * Select all receipts to compute daily collection except
   * 1. writeoff receipts
   * 2. writeoff refunds receipts
   */
  private static final String HOSPITAL_TOTAL_COLLECTION = "select sum(t.amt) from ("
      + " select amount as amt from receipts where"
      + " display_date::date > current_date - 2 AND display_date::date < current_date"
      + " and receipt_type != 'W' and receipt_id not in ("
      + "   select refund_receipt_id from receipt_refund_reference rrf "
      + "   join receipts r on r.receipt_id = rrf.receipt_id AND r.receipt_type = 'W')"
      + " union all select amount as amt FROM insurance_claim_receipt icr where"
      + " display_date::date > current_date - 2 AND display_date::date < current_date) as t";
      
  @Override
  protected Map getMessagingData() {
    Map<String, String> collectionData = new HashMap<>();
    try {
      String currency = GenericPreferencesDAO.getGenericPreferences().getCurrencySymbol();
      if (currency == null) {
        currency = "";
      }
      String hospitalName = GenericPreferencesDAO.getGenericPreferences().getHospitalName();
      if (hospitalName == null) {
        hospitalName = "";
      }
      StringBuilder detailedSummary = new StringBuilder("");
      StringBuilder totalCollectionSummary = new StringBuilder("");
      String hospitalTotal = "0";
      Integer totalcollectionStr = 0;
      Connection contotal = DataBaseUtil.getReadOnlyConnection();
      PreparedStatement pstotal = null;
      try {
        pstotal = contotal.prepareStatement(HOSPITAL_TOTAL_COLLECTION);
        totalcollectionStr = (DataBaseUtil.getIntValueFromDb(pstotal));

      } finally {
        DataBaseUtil.closeConnections(contotal, pstotal);
      }

      if (totalcollectionStr != null) {
        hospitalTotal = totalcollectionStr.toString();
        collectionData.put("total_collection", hospitalTotal);

        List<BasicDynaBean> totalBean = null;
        Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = null;
        try {
          ps = con.prepareStatement(GET_CENTER_TOTAL_COLLECTION);
          totalBean = (DataBaseUtil.queryToDynaList(ps));

        } finally {
          DataBaseUtil.closeConnections(con, ps);
        }
        for (int i = 0; i < totalBean.size(); i++) {
          String centerName = (String) totalBean.get(i).get("center_name");
          int centerId = (Integer) totalBean.get(i).get("center_id");
          String centerSum = totalBean.get(i).get("sum").toString();
          detailedSummary.append(centerName + "\n");
          totalCollectionSummary.append(centerName + " : " + currency + " " + centerSum + "\n");
          List<BasicDynaBean> paymentModeWiseData = null;
          Connection con2 = DataBaseUtil.getReadOnlyConnection();
          PreparedStatement ps2 = null;
          try {
            ps2 = con2.prepareStatement(GET_DETAILED_SUMMARY);
            ps2.setInt(1, centerId);
            paymentModeWiseData = (DataBaseUtil.queryToDynaList(ps2));

          } finally {
            DataBaseUtil.closeConnections(con2, ps2);
          }
          for (int j = 0; j < paymentModeWiseData.size(); j++) {
            String paymentMode = (String) paymentModeWiseData.get(j).get("payment_mode");
            String sum = paymentModeWiseData.get(j).get("sum").toString();
            detailedSummary.append(paymentMode + " : " + currency + " " + sum + "\n");
          }
          detailedSummary.append("Total : " + currency + " " + centerSum + "\n \n");
        }
      }
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, -1);
      String yestedayDate = new SimpleDateFormat("dd-MMM-yyyy").format(cal.getTime());
      collectionData.put("hospital_name", hospitalName);
      collectionData.put("currency_symbol", currency);
      collectionData.put("total_collection", hospitalTotal);
      collectionData.put("total_collection_summary", totalCollectionSummary.toString());
      collectionData.put("detailed_summary", detailedSummary.toString());
      collectionData.put("yesteday_date", yestedayDate);
    } catch (SQLException exception) {
      logger.error(exception.getMessage());
    }
    return collectionData;
  }

  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    String schema = getSchema();
    RequestContext.setConnectionDetails(new String[] { null, null, schema, "_system", "0" });
    BasicDynaBean msgBean;
    try {
      msgBean = new GenericDAO("message_types").findByKey("message_type_id",
          "sms_daily_collection");
      if (msgBean != null && msgBean.get("status") != null && msgBean.get("status").equals("A")) {
        MessageManager mgr = new MessageManager();
        String[] modules = getModuleDependencies();
        boolean moduleOk = true;
        for (String module : modules) {
          moduleOk = moduleOk && isModuleEnable(module);
        }
        if (moduleOk) {
          mgr.processEvent(getMessagingEvent(), getMessagingData(), false);
        }
      }
    } catch (SQLException exception) {
      logger.debug("SQLException in DailyCollectionSMSJob" + exception.getMessage());
    } catch (IOException exception) {
      logger.debug("IOException in DailyCollectionSMSJob" + exception.getMessage());
    } catch (ParseException exception) {
      logger.debug("ParseException in DailyCollectionSMSJob" + exception.getMessage());
    }
  }

}
