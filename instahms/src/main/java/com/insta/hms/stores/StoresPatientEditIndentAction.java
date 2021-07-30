package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StoresPatientEditIndentAction extends BaseAction {

  public ActionForward show(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {

    String patientIndentNo = req.getParameter("patient_indent_no");
    StoresPatientIndentDAO indentMainDAO = new StoresPatientIndentDAO();
    BasicDynaBean patIndentMain = indentMainDAO.findByKey("patient_indent_no", patientIndentNo);
    Integer storeId = (Integer) patIndentMain.get("indent_store");
    BasicDynaBean storeDetails = StoreDAO.findByStore(storeId);
    Integer centerId = (Integer) storeDetails.get("center_id");
    String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
    JSONSerializer js = new JSONSerializer().exclude("class");
    List<BasicDynaBean> patIndentDetList = indentMainDAO.getPatientIndentDetails(patientIndentNo,
        healthAuthority);

    req.setAttribute("indentMain", patIndentMain);
    req.setAttribute("indentDetails", patIndentDetList);
    req.setAttribute("indentDetailsJson",
        ConversionUtils.listBeanToMapListMap(patIndentDetList, "medicine_id"));
    req.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
    req.setAttribute("patient",
        VisitDetailsDAO.getPatientVisitDetailsMap((String) patIndentMain.get("visit_id")));
    req.setAttribute("returns", am.getProperty("category") != null);
    req.setAttribute("titlePrefix", "Edit");
    req.setAttribute("returnIndentableItems",
        js.deepSerialize(ConversionUtils
            .listBeanToMapListMap(new StoresPatientIndentDAO().getPatientReturnIndentableItems(
                (String) patIndentMain.get("visit_id"), healthAuthority), "store_id")));
    req.setAttribute("returnIndentableBatchItems",
        js.deepSerialize(ConversionUtils
            .listBeanToMapListMap(new GenericDAO("patient_return_indentable_batch_items")
                .findAllByKey("visit_id", (String) patIndentMain.get("visit_id")), "store_id")));
    req.setAttribute("stock_ts", MedicineStockDAO.getMedicineTimestamp());
    req.setAttribute("master_timestamp", PharmacymasterDAO.getItemMasterTimestamp());
    req.setAttribute("doctorDetails", js.serialize(DoctorMasterDAO.getDoctorsandCharges()));
    req.setAttribute("genericNames", js.serialize(ConversionUtils
        .copyListDynaBeansToMap(new GenericDAO("generic_name").listAll(null, "status", "A"))));

    return am.findForward("editIndent");
  }

  public ActionForward update(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {

    Map reqMap = req.getParameterMap();
    String[] medicineId = (String[]) reqMap.get("medicine_id");
    String[] indentItemId = (String[]) reqMap.get("indent_item_id");
    String[] deleted = (String[]) reqMap.get("deleted");
    String[] dispenseStatus = (String[]) reqMap.get("dispense_status");
    String[] itemBatchId = (String[]) reqMap.get("item_batch_id");

    StoresPatientIndentDAO patIndentDAO = new StoresPatientIndentDAO();
    GenericDAO patIndentDetDAO = new GenericDAO("store_patient_indent_details");

    BasicDynaBean patIndentMainBean = patIndentDAO.findByKey("patient_indent_no",
        req.getParameter("patient_indent_no"));
    String depenseStatusMain = (String) patIndentMainBean.get("dispense_status");
    String statusMain = (String) patIndentMainBean.get("status");
    BasicDynaBean patIndentDetBean = null;
    List<BasicDynaBean> newPatIndentDetList = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> updatePatIndentDetList = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> deletePatIndentDetList = new ArrayList<BasicDynaBean>();

    ConversionUtils.copyToDynaBean(reqMap, patIndentMainBean);// main details
    patIndentMainBean.set("visit_id", req.getParameter("visit_id"));
    patIndentMainBean.set("dispense_status", depenseStatusMain);
    if (statusMain.equals("F")) {
      patIndentMainBean.set("status", "F");
    }
    Object patIndentNo = patIndentMainBean.get("patient_indent_no");
    boolean newIndent = (patIndentNo == null);
    patIndentNo = (patIndentNo == null ? patIndentDAO.getNextIndentNo() : patIndentNo);
    patIndentMainBean.set("patient_indent_no", patIndentNo);
    // cancelled indent can not have open dispense status

    if (patIndentMainBean.get("status").equals("F")
        && patIndentMainBean.get("finalized_date") == null) {
      patIndentMainBean.set("finalized_date", DateUtil.getCurrentTimestamp());
      patIndentMainBean.set("finalized_user", RequestContext.getUserName());

    }

    Connection con = null;
    boolean status = true;
    Map keys = new HashMap<String, Object>();
    keys.put("patient_indent_no", patIndentMainBean.get("patient_indent_no"));
    try {

      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      BasicDynaBean existingDetBean = null;

      for (int i = 0; i < medicineId.length - 1; i++) {

        patIndentDetBean = patIndentDetDAO.getBean();

        Map detailkeys = new HashMap<String, Object>();
        detailkeys.put("patient_indent_no", patIndentMainBean.get("patient_indent_no"));
        detailkeys.put("medicine_id", Integer.parseInt(medicineId[i]));

        existingDetBean = patIndentDetDAO.findByKey(detailkeys);

        ConversionUtils.copyIndexToDynaBean(reqMap, i, patIndentDetBean);
        patIndentDetBean.set("patient_indent_no", patIndentNo);
        if (!itemBatchId[i].equals("")) {
          patIndentDetBean.set("item_batch_id", Integer.parseInt(itemBatchId[i]));
        }

        if (existingDetBean != null) {// do not allow quantity update in edit mode for already added
                                      // items

          patIndentDetBean.set("qty_received", existingDetBean.get("qty_received"));

          if (!patIndentMainBean.get("status").equals("O")) {// no edit qty
            patIndentDetBean.set("qty_required", existingDetBean.get("qty_required"));
          }
        } else {// adding a new item,received qty is never a indent property hence no update or no
                // add,let it be 0 for the first time
          patIndentDetBean.set("qty_received", BigDecimal.ZERO);
        }

        patIndentDetBean.set("dispense_status", dispenseStatus[i]);
        patIndentDetBean.set("dispense_status",
            patIndentMainBean.get("status").equals("C")
                || (existingDetBean != null && existingDetBean.get("dispense_status").equals("C"))
                    ? "C"
                    : patIndentDetBean.get("dispense_status"));

        if (deleted[i].equals("Y")) {
          deletePatIndentDetList.add(patIndentDetBean);
        } else {

          if (patIndentDetBean.get("patient_indent_no") == null || indentItemId[i].isEmpty())
            newPatIndentDetList.add(patIndentDetBean);
          else
            updatePatIndentDetList.add(patIndentDetBean);
        }
      }

      // Insert/Update indent main details
      if (newIndent)
        status &= patIndentDAO.insert(con, patIndentMainBean);
      else
        status &= patIndentDAO.update(con, patIndentMainBean.getMap(), keys) > 0;

      // Insert indent details
      if (newPatIndentDetList.size() > 0)
        status &= patIndentDetDAO.insertAll(con, newPatIndentDetList);

      // Update indent details

      for (BasicDynaBean indentDet : updatePatIndentDetList) {

        keys = new HashMap<String, Object>();
        keys.put("indent_item_id", indentDet.get("indent_item_id"));

        status &= patIndentDetDAO.update(con, indentDet.getMap(), keys) > 0;
      }

      for (BasicDynaBean deletedBean : deletePatIndentDetList) {
        status &= patIndentDetDAO.delete(con, "indent_item_id", deletedBean.get("indent_item_id"));
      }

      status &= patIndentDAO.updateIndentDispenseStatus(con, req.getParameter("visit_id"));
    } finally {
      DataBaseUtil.commitClose(con, status);
    }

    req.setAttribute("titlePrefix", "Edit");
    ActionRedirect redirect = new ActionRedirect(am.findForward("editRedirect"));
    redirect.addParameter("patient_indent_no", patIndentNo);
    redirect.addParameter("stop_doctor_orders", true);
    return redirect;
  }

}
