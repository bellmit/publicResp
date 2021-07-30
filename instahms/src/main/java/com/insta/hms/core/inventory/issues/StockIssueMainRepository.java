package com.insta.hms.core.inventory.issues;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class StockIssueMainRepository extends GenericRepository {

  public StockIssueMainRepository() {
    super("stock_issue_main");
  }

  @Override
  public Integer getNextSequence() {
    return DatabaseHelper.getInteger("SELECT nextval(?)", "store_issue_sequence");
  }
  
  private static final String GET_ISSUE_ITEMS = " select sim.user_issue_no,"
      + " sim.username as user_name, sim.date_time::date as issue_date, s.dept_name,s.dept_id, "
      + " medicine_name, sid.medicine_id, sibd.batch_no, "
      + " sid.qty, reference as remarks, sim.gatepass_id,"
      + " sim.user_type, sim.issued_to,sibd.item_batch_id, "
      + " coalesce(sm.salutation||' '||pd.patient_name||' '||pd.middle_name||' '||pd.last_name,'')"
      + " as patient_name, "
      + " round((sid.amount*sid.qty),2) as amount, m.issue_units, "
      + " case when scm.billable='t' then 'true' else 'false' end as billable, "
      + " case when issue_type='P' then 'Permanent' when issue_type='C' then 'Consumable' "
      + " when issue_type='R'  then 'Retailable' else 'Reusable' end as issue_type, "
      + " case when consignment_stock='t' then 'Consignment' else 'Normal' end as stocktype,"
      + " sid.indent_no, "
      + " date(indent.date_time) as indent_date,m.cust_item_code " 
      + " from stock_issue_main sim  "
      + " left join stock_issue_details sid on sid.user_issue_no = sim.user_issue_no "
      + " LEFT JOIN store_item_batch_details sibd USING(item_batch_id)"
      + " left join patient_registration pr on (pr.patient_id=sim.issued_to and pr.status='A')  "
      + " left outer join patient_details pd on (pd.mr_no=pr.mr_no) "
      + " left join store_item_details m on m.medicine_id = sid.medicine_id "
      + " join store_category_master scm ON (scm.category_id = m.med_category_id) "
      + " left join stores s on s.dept_id = sim.dept_from  "
      + " left join store_transaction_lot_details stld "
      + " on (stld.transaction_type = 'U' and stld.transaction_id = sid.item_issue_no) "
      + " join store_stock_details ssd on (ssd.dept_id=dept_from and"
      + " ssd.medicine_id=sid.medicine_id and sid.batch_no=ssd.batch_no"
      + " and stld.item_lot_id = ssd.item_lot_id) "
      + "left join salutation_master sm on (sm.salutation_id = pd.salutation)  "
      + " left join store_indent_main indent on (indent.indent_no= sid.indent_no) "; 
      
  private static final String GET_ISSUE_ITEMS_USERNO_FILTER = 
      " WHERE sim.user_issue_no=? AND "
      + " ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )";
  
  private static final String GET_ISSUE_ITEMS_ISSUE_NO_FILTER = 
      " WHERE  sim.item_issue_no = ? AND "
      + "( patient_confidentiality_check(pd.patient_group,pd.mr_no) )";
  
  private static final String GET_ISSUE_ITEM_DETAILS = " SELECT sim.dept_from, sim.issued_to, " 
      + " sim.user_issue_no, sid.medicine_id, sid.item_batch_id, sid.qty, "
      + " sid.issue_pkg_size, sid.item_unit, sid.item_issue_no, bac.charge_id "
      + " FROM bill_charge bc "
      + " JOIN bill_activity_charge bac ON (bc.charge_id = bac.charge_id) "
      + " JOIN stock_issue_details sid ON  (sid.item_issue_no::character varying "
      + " = bac.activity_id and bac.payment_charge_head ='INVITE') "
      + " JOIN stock_issue_main sim ON sim.user_issue_no = sid.user_issue_no "
      + " WHERE bc.bill_no = ? ";

  public List<BasicDynaBean> getIssuedItemList(Integer issueNo) {
    return DatabaseHelper.queryToDynaList(GET_ISSUE_ITEMS + GET_ISSUE_ITEMS_USERNO_FILTER,
        new Object[] { issueNo });
  }

  /**
   * get Visit Issued ItemMap.
   * @param billNo the billNo
   * @return map
   */
  public Map<String, BasicDynaBean> getVisitIssuedItemMap(String billNo) {
    List<BasicDynaBean> issuedItemList = DatabaseHelper.queryToDynaList(GET_ISSUE_ITEM_DETAILS,
        billNo);
    Map<String, BasicDynaBean> map = new HashMap<>();
    for (BasicDynaBean bean : issuedItemList) {
      map.put((String) bean.get("charge_id"), bean);
    }

    return map;
  }
  
  private static final String GET_ISSUE_RETURN_ITEM_DETAILS = " SELECT sirm.dept_to, " 
      + " sirm.returned_by, sirm.user_return_no, sird.medicine_id, sird.item_batch_id, sird.qty, "
      + " sird.rtn_pkg_size, sird.item_unit, sird.item_return_no, bac.charge_id "
      + " FROM store_issue_returns_main  sirm "
      + " JOIN store_issue_returns_details  sird using(user_return_no) " 
      + " JOIN bill_activity_charge bac on "
      + " (sird.item_return_no::character varying = bac.activity_id "
      + " and bac.payment_charge_head ='INVRET') "
      + " JOIN bill_charge bc on (bc.charge_id = bac.charge_id ) "
      + " WHERE bc.bill_no = ? ";
  
  /**
   * get Visit Issue Returned ItemMap.
   * @param billNo the billNo
   * @return map
   */
  public Map<String, BasicDynaBean> getVisitIssueReturnedItemMap(String billNo) {
    List<BasicDynaBean> issuedItemList = DatabaseHelper
        .queryToDynaList(GET_ISSUE_RETURN_ITEM_DETAILS, billNo);
    Map<String, BasicDynaBean> map = new HashMap<>();
    for (BasicDynaBean bean : issuedItemList) {
      map.put((String) bean.get("charge_id"), bean);
    }

    return map;
  }
  
  public BasicDynaBean getIssuedItem(int itemIssueNo) {
    return DatabaseHelper.queryToDynaBean(GET_ISSUE_ITEMS + GET_ISSUE_ITEMS_ISSUE_NO_FILTER,
        new Object[] { itemIssueNo });
  }

}
