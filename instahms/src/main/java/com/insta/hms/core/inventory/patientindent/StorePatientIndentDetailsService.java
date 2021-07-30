package com.insta.hms.core.inventory.patientindent;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.item.StoreItemDetailsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StorePatientIndentDetailsService {

  @LazyAutowired
  StorePatientIndentDetailsRepository storePatientIndentDetailsRepository;

  @LazyAutowired
  StorePatientIndentRepository storePatientIndentRepository;

  @LazyAutowired
  StoreItemDetailsService storeItemDetailsService;

  public boolean updateIndentDetailsDispenseStatus(String visitId,
      Map<String, String> indentDisStatusMap, BigDecimal quantity, Integer medicine_id,
      int processId, String processRefColName) {

    boolean sucess = true;
    Map keys = new HashMap<String, Object>();
    BasicDynaBean medDetail = storeItemDetailsService.getMedicineDetails(medicine_id);

    List<BasicDynaBean> indentDetails = null;
    keys = new HashMap<String, Object>();
    keys.put("medicine_id", medicine_id);
    keys.put("visit_id", visitId);
    indentDetails = getIndentsOfItem(visitId, medicine_id, null, null);
    for (BasicDynaBean indentDetailBean : indentDetails) {

      if (indentDisStatusMap.get((String) indentDetailBean.get("patient_indent_no")) == null) {
        continue;
      }

      BasicDynaBean indentItem = storePatientIndentDetailsRepository.findByKey("indent_item_id",
          indentDetailBean.get("indent_item_id"));

      BigDecimal indentReqQty = medDetail.get("identification").equals("S") ? BigDecimal.ONE
          : (BigDecimal) indentDetailBean.get("qty_required");
      BigDecimal indentRecQty = (BigDecimal) indentDetailBean.get("qty_received");
      BigDecimal actIndentRecQty = (BigDecimal) indentDetailBean.get("qty_required");
      // if user qty is more that req qty of indent this indent is fulfilled
      BigDecimal qtyforThisIndent = quantity.compareTo(actIndentRecQty.subtract(indentRecQty)) > 0
          ? actIndentRecQty.subtract(indentRecQty) : quantity;
      quantity = quantity.subtract(qtyforThisIndent);

      BigDecimal reqQty = indentReqQty;

      sucess &= updateIndentDetails((Integer) indentDetailBean.get("indent_item_id"),
          qtyforThisIndent,
          indentDisStatusMap.get((String) indentDetailBean.get("patient_indent_no")), processId,
          indentItem, processRefColName, reqQty, (BigDecimal) indentDetailBean.get("qty_required"));

      if (quantity.compareTo(BigDecimal.ZERO) == 0)// user qty is processed come ot of this indent
        break;

    }

    return sucess;
  }

  public boolean updateIndentDetails(int indentItemId, BigDecimal recvQty, String dispenseStatus,
      int processItemId, BasicDynaBean indetItem, String processIdColName, BigDecimal reqQty,
      BigDecimal actReqQty) {
    BigDecimal recQty = ((BigDecimal) indetItem.get("qty_received")).add(recvQty.abs());
    String itemDispenseStatus = dispenseStatus.equals("all") ? "C"
        : (dispenseStatus.equals("partiall") && recQty.compareTo(BigDecimal.ZERO) > 0 ? "C"
            : (actReqQty.subtract(recQty).compareTo(BigDecimal.ZERO) == 0 ? "C" : "O"));

    indetItem.set("qty_received", recQty);
    indetItem.set("dispense_status", itemDispenseStatus);
    indetItem.set(processIdColName, processItemId);

    Map<String, Object> keys = new HashMap<>();
    keys.put("indent_item_id", indetItem.get("indent_item_id"));

    return storePatientIndentDetailsRepository.update(indetItem, keys) > 0;

  }

  private static final String visit_item_indents = " SELECT * FROM store_patient_indent_main sm "
      + " JOIN store_patient_indent_details USING(patient_indent_no)"
      + " WHERE visit_id = ? AND medicine_id = ? AND sm.dispense_status != 'C' AND sm.status = 'F' # ORDER BY expected_date";

  public List<BasicDynaBean> getIndentsOfItem(String visitId, int medicineId, String returnType,
      Integer itemBatchId) {
    if (returnType != null && returnType.equals("R")) {
      return DatabaseHelper.queryToDynaList(
          visit_item_indents.replace("#", " AND item_batch_id = ?"), visitId, medicineId,
          itemBatchId);
    } else {
      return DatabaseHelper.queryToDynaList(visit_item_indents.replace("#", ""), visitId,
          medicineId);
    }
  }

  public boolean closeAllIndents(String patientIndentNo) {
    return storePatientIndentDetailsRepository.closeAllIndents(patientIndentNo) > 0;
  }

  public boolean updateProcessType(String visitId, String processType) {
    return storePatientIndentRepository.updateProcessType(visitId, processType) > 0;
  }

  public boolean updateIndentDispenseStatus(String visitId) {
    return storePatientIndentRepository.updateIndentDispenseStatus(visitId) > 0;
  }

}
