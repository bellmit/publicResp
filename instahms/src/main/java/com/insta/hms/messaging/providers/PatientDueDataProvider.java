package com.insta.hms.messaging.providers;

import com.insta.hms.billing.BillDAO;
import com.insta.hms.messaging.MessageContext;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientDueDataProvider extends QueryDataProvider {
  static Logger logger = LoggerFactory
      .getLogger(PatientDueDataProvider.class);
  private static String THIS_NAME = "Patient Due ";
  private static final String selectFields = "SELECT * ";
  private static final String selectCount = "SELECT COUNT(*)";
  private static final String fromTables = "FROM (select pd.visit_id ,pd.patient_care_oftext"
      + " as next_of_kin_phone, pd.relation as next_of_kin_name,  "
      + " sm.salutation||' '||pd.patient_name||' '||coalesce(pd.middle_name,'')||' '||"
      + "coalesce(pd.last_name,'')  as patient_name, "
      + " pd.patient_phone , pd.email_id as patient_email, "
      + " pd.mr_no,hcm.center_id, hcm.center_name,hcm.center_contact_phone AS center_phone, "
      + " hcm.center_address, pr.reg_date , pr.reg_time, dep.dept_name as department,"
      + " (select currency_symbol from generic_preferences ) as currency_symbol , "
      + " d.doctor_id as presc_doc_id, d.doctor_name, d.doctor_mobile, "
      + " pd.mr_no as receipient_id__, 'PATIENT' as receipient_type__  "
      + " from patient_details pd "
      + " LEFT JOIN patient_registration pr on(pd.visit_id=pr.patient_id) "
      + " LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id) "
      + " LEFT JOIN store_retail_customers prc ON (prc.customer_id = pd.visit_id) "
      + " LEFT JOIN hospital_center_master hcm  ON(hcm.center_id = pr.center_id) "
      + " LEFT JOIN doctors d on(d.doctor_id = pr.doctor) "
      + " LEFT JOIN department dep on(dep.dept_id = pr.dept_name) " + " ) as foo ";

  public PatientDueDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null);
  }

  String[] tokens = new String[] { "net_patient_due", "total_bill_amount", "total_amount_received",
      "patient_name", "patient_email", "mr_no", "center_name", "center_phone", "center_address",
      "currency_symbol", "reg_date", "reg_time", "department", "doctor_name", "doctor_mobile",
      "next_of_kin_name", "next_of_kin_phone", "patient_phone" };

  @Override
  public List<String> getTokens() throws SQLException {
    List<String> tokenList = null;
    tokenList = new ArrayList<String>();
    for (String token : tokens) {
      if (!tokenList.contains(token)) {
        tokenList.add(token);
      }
    }
    Collections.sort(tokenList);
    return tokenList;

  }

  @Override
  public List<Map> getMessageDataList(MessageContext ctx) throws SQLException, ParseException {
    Map eventData = ctx.getEventData();
    if (null != eventData) {
      Map filter = new HashMap();
      if (null != eventData.get("visit_id")) {
        String[] visitId = { eventData.get("visit_id").toString() };
        filter.put("visit_id", visitId);
        filter.put("visit_id@type", new String[] { "text" });
        filter.put("visit_id@cast", new String[] { "y" });
        filter.put("visit_id@op", new String[] { "in" });
      }
      addCriteriaFilter(filter);
      List<Map> messageDataList = super.getMessageDataList(ctx);
      List<Map> messageDataListWithReportContent = new ArrayList<Map>();
      try {
        for (int i = 0; i < messageDataList.size(); i++) {
          Map item = new HashMap<Object, Object>();
          item.putAll(messageDataList.get(i));
          BillDAO billDao = new BillDAO();
          BasicDynaBean patientduedetails = billDao
              .getVisitTotalBillAmounts(eventData.get("visit_id").toString());
          item.put("net_patient_due", patientduedetails.get("patient_due").toString());
          item.put("total_bill_amount", patientduedetails.get("amount").toString());
          item.put("total_amount_received", patientduedetails.get("total_receipts").toString());
          messageDataListWithReportContent.add(item);
        }
      } catch (Exception ex) {
        logger.error("", ex);
      }
      return messageDataListWithReportContent;
    }
    return Collections.EMPTY_LIST;
  }
}