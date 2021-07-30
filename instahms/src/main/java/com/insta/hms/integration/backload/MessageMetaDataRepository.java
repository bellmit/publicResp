package com.insta.hms.integration.backload;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.DatabaseHelper;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MessageMetaDataRepository {

  private static final String MESSAGE_ADT = "ADT";

  private static final String MESSAGE_OMP = "OMP";

  private static final String MESSAGE_ORU = "ORU";

  private static final String SELECT = "SELECT ";

  private static final String FROM = " FROM ";

  private static final String JOIN = " JOIN ";

  private static final String ON = " ON ";

  private static final String WHERE = " WHERE ";

  private static final String AND = " AND ";

  private static final String ORDER_BY = " ORDER BY ";

  private static final String QUERY_SEP = ", ";

  private static final String ITEM_MRNO = "pr.mr_no ";

  private static final String ITEM_VISITID = "pr.patient_id as visit_id ";
  
  private static final String ITEM_REGDATE = "pr.reg_date ";

  private static final String ITEM_PRESCRIPTION_ID = 
      "Medicine_Prescriptions.op_medicine_pres_id as item_ids ";

  private static final String ITEM_TEST_PRESCRIBED_ID = 
      "Tests_Prescribed.prescribed_id as presc_id";

  private static final String PATIENT_REGISTRATION_TABLE = "patient_registration pr ";

  private static final String MEDICINE_PRESCRIPTION_RECORDS = 
      "(SELECT ppc.visit_id, pmp.op_medicine_pres_id "
      + "FROM patient_medicine_prescriptions pmp "
      + "JOIN (select visit_id, patient_presc_id FROM patient_prescription "
      + "WHERE visit_id is not null UNION SELECT dc.patient_id, pp.patient_presc_id "
      + "FROM patient_prescription pp "
      + "LEFT JOIN doctor_consultation dc ON (dc.consultation_id=pp.consultation_id) "
      + "WHERE dc.consultation_id is not null ) as ppc "
      + "ON ppc.patient_presc_id = pmp.op_medicine_pres_id "
      + "LEFT JOIN generic_name gn ON (pmp.generic_code = gn.generic_code) "
      + "LEFT JOIN store_item_details sid ON (sid.medicine_id = pmp.medicine_id) "
      + "LEFT JOIN store_category_master scm ON (sid.med_category_id = scm.category_id) "
      + "WHERE scm.is_drug = 'Y') as Medicine_Prescriptions ";
  
  private static final String TEST_PRESCRIBED_RECORDS = 
      "(SELECT tp.pat_id as visit_id, tp.prescribed_id "
      + "FROM tests_prescribed tp "
      + "JOIN tests_conducted tc USING (prescribed_id) "
      + "JOIN test_visit_reports tvr USING (report_id) "
      + "WHERE tp.conduction_type = 'i' AND tvr.signed_off = 'Y') as Tests_Prescribed ";

  private static final String CONNECT_MEDICINE_PRESCRIPTION_PATIENT_REGISTRATION = 
      "Medicine_Prescriptions.visit_id = pr.patient_id ";

  private static final String CONNECT_TEST_PRESCRIBED_PATIENT_REGISTRATION = 
      "Tests_Prescribed.visit_id = pr.patient_id ";

  private static final String CONDITION_DATE_RANGE = "pr.reg_date >= ? AND pr.reg_date < ? "; 

  private static final String CONDITION_CENTERID = "pr.center_id = ? " ;

  private static final String ORDER_BY_VISITID = "pr.patient_id" ;

  private static final String KEY_VISIT_ID = "visit_id";

  private static final String KEY_ITEM_IDS = "item_ids";

  /**
   * Search records by date range provided.
   * 
   * @param startDate the start date
   * @param endDate the end date
   * @param centerId the center id
   * @param messageType the message type
   * @return list of basic dyna beans
   * @throws ParseException the parse exception
   */
  public List<BasicDynaBean> getRecordsByDateRange(String startDate, String endDate, 
      int centerId, String messageType) throws ParseException {
    Date startdt = DateUtil.parseDate(startDate);
    Date enddt = DateUtil.parseDate(endDate); 
    String queryByMessageType = getQueryByMessageType(messageType);  
    if (centerId != 0) {
      return DatabaseHelper.queryToDynaList(queryByMessageType + AND + CONDITION_CENTERID
        + ORDER_BY + ORDER_BY_VISITID, startdt, enddt, centerId);
    } else {
      return DatabaseHelper.queryToDynaList(queryByMessageType 
        + ORDER_BY + ORDER_BY_VISITID, startdt, enddt);
    }
  }

  private String getQueryByMessageType(String messageType) {
    String msg = messageType.substring(0, 3);
    switch (msg) {
      case MESSAGE_ADT :
        return SELECT + ITEM_MRNO + QUERY_SEP + ITEM_VISITID + QUERY_SEP + ITEM_REGDATE
          + FROM + PATIENT_REGISTRATION_TABLE 
          + WHERE + CONDITION_DATE_RANGE;
      case MESSAGE_OMP:
        return SELECT + ITEM_VISITID + QUERY_SEP + ITEM_PRESCRIPTION_ID
          + FROM + PATIENT_REGISTRATION_TABLE
          + JOIN + MEDICINE_PRESCRIPTION_RECORDS
          + ON + CONNECT_MEDICINE_PRESCRIPTION_PATIENT_REGISTRATION
          + WHERE + CONDITION_DATE_RANGE;
      case MESSAGE_ORU:
        return SELECT + ITEM_VISITID + QUERY_SEP + ITEM_TEST_PRESCRIBED_ID
          + FROM + PATIENT_REGISTRATION_TABLE
          + JOIN + TEST_PRESCRIBED_RECORDS
          + ON + CONNECT_TEST_PRESCRIBED_PATIENT_REGISTRATION
          + WHERE + CONDITION_DATE_RANGE
          + ORDER_BY + ORDER_BY_VISITID;
      default:
        return null;
    }
  }

  /**
   * Formats the records if required by messageType.
   * 
   * @param itemslistMap the list of maps
   * @param messageType message type 
   */
  public void formatItemListMapIfRequired(List<Map<String, Object>> itemslistMap, 
      String messageType) {
    String msg = messageType.substring(0, 3);
    String[] types = {MESSAGE_OMP};
    final List<String> singleMsgForMultipleItems = new ArrayList<String>(Arrays.asList(types));
    List<Map<String,Object>> newItemslistMap = new ArrayList<Map<String,Object>>();
    if (singleMsgForMultipleItems.contains(msg)) {
      String visitId = null; 
      String prevVisitId = null;
      int itemId = 0;
      List<Integer> itemIdList = new ArrayList<Integer>();
      Map<String,Object> newItemMap = new HashMap<String,Object>();
      for (Map<String,Object> itemMap: itemslistMap) {
        visitId = (String)itemMap.get(KEY_VISIT_ID);
        itemId  = (int)itemMap.get(KEY_ITEM_IDS);
        if (null != prevVisitId && prevVisitId.equalsIgnoreCase(visitId)) {
          //same visit
          itemIdList.add(itemId);
        } else if (prevVisitId == null) {
          //first visit
          itemIdList.add(itemId);
        } else if (! prevVisitId.equalsIgnoreCase(visitId)) {
          //new visit: save data for previous visit in new ListMap; 
          //Empty List; Empty Map
          newItemMap.put(KEY_VISIT_ID, prevVisitId);
          newItemMap.put(KEY_ITEM_IDS, itemIdList);
          newItemslistMap.add(newItemMap);
          newItemMap = new HashMap<String,Object>();
          itemIdList = new ArrayList<Integer>();
          itemIdList.add(itemId);
        }
        prevVisitId = visitId;
      }
      if (itemIdList.size() > 0) {
        newItemMap.put(KEY_VISIT_ID, prevVisitId);
        newItemMap.put(KEY_ITEM_IDS, itemIdList);
        newItemslistMap.add(newItemMap);
      }
      itemslistMap.clear();
      itemslistMap.addAll(newItemslistMap);
    }
  }
}
