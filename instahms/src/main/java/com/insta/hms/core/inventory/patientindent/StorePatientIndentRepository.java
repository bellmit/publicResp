package com.insta.hms.core.inventory.patientindent;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class StorePatientIndentRepository extends GenericRepository {

  public StorePatientIndentRepository() {
    super("store_patient_indent_main");
  }

  private static final String INDENT_ITEM_DETAILS = " SELECT " + " CASE "
      + "   WHEN qty_unit = 'P' "
      + "     THEN (sum(qty_required)-sum(qty_received))/issue_base_unit "
      + "   ELSE sum(qty_required)-sum(qty_received) " + "   END  as qty, "
      + "  sid.medicine_id,medicine_name as indent_medicine_name,indent_store"
      + "  ,qty_unit,CASE WHEN qty_unit = 'I' THEN '' ELSE st.package_uom END as uom,"
      + "  issue_base_unit,st.issue_units as uom_display " + " FROM store_patient_indent_main sim"
      + " JOIN store_patient_indent_details sid USING(patient_indent_no) "
      + " JOIN store_item_details st USING (medicine_id) "
      + " WHERE sim.visit_id = ? and sim.status IN (?) AND indent_type = ?"
      + "  AND sim.dispense_status != 'C' AND sid.dispense_status = 'O' #"
      + " GROUP BY sid.medicine_id, medicine_name," + "          st.issue_units,st.package_UOM"
      + "          ,indent_store,qty_unit,uom,issue_base_unit ";

  private static final String UPDATE_MAIN_DISPENSE_STAUS = "UPDATE store_patient_indent_main sm SET dispense_status = ("
      + " CASE " + "    WHEN "
      + "     (SELECT count(*) FROM store_patient_indent_details sd WHERE  sm.patient_indent_no = sd.patient_indent_no AND "
      + "     sd.dispense_status != 'C') = 0 " + "    THEN 'C' " + "      WHEN "
      + "     (SELECT count(*) FROM store_patient_indent_details sd WHERE sm.patient_indent_no = sd.patient_indent_no AND"
      + "      sd.qty_received != 0 and sd.qty_received < qty_required) > 0 " + "   THEN 'P' "
      + "     WHEN "
      + "   (SELECT count(*) FROM store_patient_indent_details sd WHERE sm.patient_indent_no = sd.patient_indent_no AND"
      + "      sd.dispense_status = 'C' ) > 0 " + "       THEN 'P' " + "     ELSE 'O' "
      + " END) WHERE visit_id = ? AND dispense_status != 'C' ";

  public Integer updateIndentDispenseStatus(String visitId) {
    return DatabaseHelper.update(UPDATE_MAIN_DISPENSE_STAUS, visitId);
  }

  private String INDENTS_FOR_PROCESS = " SELECT * FROM store_patient_indent_main "
      + " WHERE visit_id = ? AND status = 'F' AND dispense_status != 'C' AND indent_type = ? "
      + "   # " + " ORDER BY expected_date ";

  public List<BasicDynaBean> getIndentsForProcess(String visitId, String indentType,
      int indentStore, String patientIndentNo) {

    if (patientIndentNo == null) {
      return DatabaseHelper.queryToDynaList(
          INDENTS_FOR_PROCESS.replace("#", "AND indent_store = ?"), visitId, indentType,
          indentStore);
    } else {
      return DatabaseHelper.queryToDynaList(
          INDENTS_FOR_PROCESS.replace("#", " AND indent_store = ? AND patient_indent_no = ? "),
          visitId, indentType, indentStore, patientIndentNo);
    }
  }

  public List<BasicDynaBean> getIndentDetailsForProcessOfIndentStore(String visitId, String status,
      String indentType, int indentStore, String patientIndentNo) {
    if (patientIndentNo != null) {
      return DatabaseHelper.queryToDynaList(
          INDENT_ITEM_DETAILS.replace("#", " AND indent_store = ?  AND patient_indent_no = ? "),
          visitId, status, indentType, indentStore, patientIndentNo);
    } else {
      return DatabaseHelper.queryToDynaList(
          INDENT_ITEM_DETAILS.replace("#", " AND indent_store = ?"), visitId, status, indentType,
          indentStore);
    }
  }

  
  public Integer updateProcessType(String visitId, String processType) {
    BasicDynaBean storePatientIndentMainBean = getBean();
    storePatientIndentMainBean.set("process_type", processType);
    Map<String, Object> params = new HashMap<>();
    params.put("visit_id", visitId);
    return update(storePatientIndentMainBean, params);
  }

}
