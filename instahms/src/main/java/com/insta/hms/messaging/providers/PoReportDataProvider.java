package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageContext;
import com.insta.hms.stores.POReportGenrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PoReportDataProvider extends QueryDataProvider {

  static Logger logger = LoggerFactory
      .getLogger(PoReportDataProvider.class);

  private static String THIS_NAME = "POReport Ready";
  private static final String selectFields = "SELECT * ";
  private static final String selectCount = "SELECT COUNT(*)";
  private static final String fromTables = " FROM (SELECT distinct   pom.po_no,"
      + " pom.po_no as poNo_key, -1 as key,"
      + " po_no||' Purchase Order Report' as message_attachment_name ,"
      + " to_char(pom.po_date,'dd-mon-yy') as po_date, sm.supplier_name, sm.cust_supplier_code, "
      + " REPLACE(sm.supplier_address,'~','') as supplier_address, supplier_phone1,"
      + " supplier_phone2, COALESCE(supplier_city,'') as city_name,"
      + " COALESCE(supplier_country,'') as country_name, COALESCE(supplier_state ,'')"
      + " as state_name,pom.supplier_terms," + " pom.hospital_terms, pom.delivery_instructions,"
      + " COALESCE(sm.supplier_tin_no,'')as supplier_tin,  pom.qut_no, "
      + " COALESCE(to_char(pom.qut_date,'dd-mon-yy'),'') as qut_date,pom.reference,"
      + " pom.credit_period,pom.delivery_date,"
      + " pom.actual_po_date, pom.user_id,s.dept_name as store_name,"
      + " hcms.center_name as store_center,"
      + " pom.amended_reason,pom.amendment_time,pom.amendment_validated_time,"
      + " pom.amendment_approved_time,"
      + " pom.amended_by,pom.amendment_validated_by,pom.amendment_approved_by,"
      + " pom.amendment_validator_remarks,pom.amendment_approver_remarks,"
      + " hcms.center_address as store_center_address ,supplier_mailid as recipient_email,"
      + " 'Purchase Order '||pom.po_no as message_subject, "
      + " 'Hi, </br> </br>  Please find attached is the purchase order for the"
      + " items required by us.' as message_body,'Pharmacy' as category "
      + " FROM store_po_main pom JOIN supplier_master sm on sm.supplier_code = pom.supplier_id "
      + " JOIN stores s on (s.dept_id=pom.store_id) "
      + " JOIN hospital_center_master hcms on (hcms.center_id=s.center_id)) as foo ";

  public PoReportDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null);
  }

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "recipient_email", "actual_po_date", "amended_by",
        "amended_reason", "amendment_approved_by", "amendment_approved_time",
        "amendment_approver_remarks", "amendment_time", "amendment_validated_by",
        "amendment_validated_time", "amendment_validator_remarks", "category", "city_name",
        "country_name", "credit_period", "cust_supplier_code", "delivery_date",
        "delivery_instructions", "hospital_terms", "message_attachment_name", "message_body",
        "message_subject", "po_date", "po_no", "pono_key", "qut_date", "qut_no", "reference",
        "state_name", "store_center", "store_center_address", "store_name", "supplier_address",
        "supplier_name", "supplier_phone1", "supplier_phone2", "supplier_terms", "supplier_tin",
        "user_id" };
    List<String> tokenList = new ArrayList<>();
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
    POReportGenrator poRep = new POReportGenrator();

    if (null != eventData) {
      Map filter = new HashMap();
      if (null != eventData.get("po_no")) {
        String[] poNo = (String[]) eventData.get("po_no");
        filter.put("poNo_key", poNo);
        filter.put("poNo_key@type", new String[] { "text" });
        filter.put("poNo_key@cast", new String[] { "y" });
      }
      addCriteriaFilter(filter);
    }

    List<Map> messageDataList = super.getMessageDataList(ctx);
    List<Map> messageDataListWithReportContent = new ArrayList<Map>();

    try {
      for (int i = 0; i < messageDataList.size(); i++) {
        Map item = new HashMap<Object, Object>();
        item.putAll(messageDataList.get(i));
        String report = poRep.generatePOReport((String) eventData.get("template_name"),
            (String[]) eventData.get("po_no"), (String) eventData.get("printType"));
        item.put("_report_content", report);
        item.put("_message_attachment", report);
        item.put("printtype", (String) eventData.get("printType"));
        messageDataListWithReportContent.add(item);
      }
    } catch (Exception ex) {
      logger.error("", ex);
    }
    return messageDataListWithReportContent;
  }

}
