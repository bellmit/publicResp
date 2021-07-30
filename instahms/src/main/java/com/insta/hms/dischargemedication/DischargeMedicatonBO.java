package com.insta.hms.dischargemedication;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.modulesactivated.ModulesActivatedService;
import com.insta.hms.core.clinical.dischargemedication.DischargeMedicationDetailsRepository;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.pbmauthorization.PBMPrescriptionsDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class DischargeMedicatonBO.
 *
 * @author krishna
 */
public class DischargeMedicatonBO {

  /** The pat medication details DAO. */
  GenericDAO patMedicationDetailsDAO = new GenericDAO("discharge_medication_details");
  
  /** The pbm presc DAO. */
  PBMPrescriptionsDAO pbmPrescDAO = new PBMPrescriptionsDAO();
  
  private static DischargeMedicationDetailsRepository dischargeMedicationDetailsRepository =
      ApplicationContextProvider.getBean(DischargeMedicationDetailsRepository.class);
  
  /**
   * Save medicine.
   *
   * @param con the con
   * @param medicationId the medication id
   * @param itemPrescriptionId the item prescription id
   * @param params the params
   * @param idx the idx
   * @param patientCenterId the patient center id
   * @param genericPrefs the generic prefs
   * @param visitId the visit id
   * @return the map
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  
  @SuppressWarnings("unchecked")
  public Map saveMedicine(
      Connection con,
      Integer medicationId,
      int itemPrescriptionId,
      Map params,
      int idx,
      int patientCenterId,
      BasicDynaBean genericPrefs,
      String visitId, Map<String, Object> prescIdByOperation)
      throws SQLException, IOException, ParseException {

    String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");
    String prescribedId = ConversionUtils.getParameter(params, "item_prescribed_id", idx);
    String itemName = ConversionUtils.getParameter(params, "item_name", idx);
    String itemId = ConversionUtils.getParameter(params, "item_id", idx);
    String adminStrenth = ConversionUtils.getParameter(params, "admin_strength", idx);
    String medFrequencie = ConversionUtils.getParameter(params, "frequency", idx);
    String duration = ConversionUtils.getParameter(params, "duration", idx);
    String durationUnit = ConversionUtils.getParameter(params, "duration_units", idx);
    String medQty = ConversionUtils.getParameter(params, "medicine_quantity", idx);
    String itemRemark = ConversionUtils.getParameter(params, "item_remarks", idx);
    String delItem = ConversionUtils.getParameter(params, "delItem", idx);
    String routeOfAdmin = ConversionUtils.getParameter(params, "route_id", idx);
    // strength is the dosage to the patient 
    // how much to take (1 tab or 1/2 tab or 5ml or 10ml) 
    String medicineStrength = ConversionUtils.getParameter(params, "strength", idx);
    String genericCode = ConversionUtils.getParameter(params, "generic_code", idx);
    String itemFormId = ConversionUtils.getParameter(params, "item_form_id", idx);
    /** item_strength is the medicine strength (100mg, 200mg, 500ml etc.,) **/
    String itemStrength = ConversionUtils.getParameter(params, "item_strength", idx);
    String itemStrengthUnit = ConversionUtils.getParameter(params, "item_strength_units", idx);
    String consUomId = ConversionUtils.getParameter(params, "cons_uom_id", idx);
    String specialInstructions = ConversionUtils.getParameter(params, "special_instr", idx);
    
    List<Integer> insertPrescIds = (List<Integer>)prescIdByOperation.get("insert");
    List<Integer> updatePrescIds = (List<Integer>)prescIdByOperation.get("update");
    List<Integer> deletePrescIds = (List<Integer>)prescIdByOperation.get("delete");

    String prescByGenerics =
        (String)
            HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(
                    CenterMasterDAO.getHealthAuthorityForCenter(patientCenterId))
                .getPrescriptions_by_generics();
    Boolean prescriptionsByGenerics = useStoreItems.equals("Y") && prescByGenerics.equals("Y");

    boolean deleteItem = new Boolean(delItem);
    BasicDynaBean medicineBean = patMedicationDetailsDAO.getBean();
    boolean flag = true;

    txn:
    {
      medicineBean.set("medication_id", medicationId);
      if (!duration.equals("")) {
        medicineBean.set("duration", Integer.parseInt(duration));
        medicineBean.set("duration_units", durationUnit);
      } else {
        medicineBean.set("duration", null);
        medicineBean.set("duration_units", null);
      }
      if (!medQty.equals("")) {
        medicineBean.set("medicine_quantity", Integer.parseInt(medQty));
      } else {
        medicineBean.set("medicine_quantity", null);
      }
      if (useStoreItems.equals("Y")) {
        if (!prescriptionsByGenerics) {
          medicineBean.set("medicine_id", Integer.parseInt(itemId));
        }
        /** update the generic_code always when pharmacy module is enabled */
        medicineBean.set("generic_code", genericCode);
      } else {
        medicineBean.set("medicine_name", itemName);
      }

      medicineBean.set("admin_strength", adminStrenth);
      medicineBean.set("frequency", medFrequencie);
      medicineBean.set("medicine_remarks", itemRemark);
      medicineBean.set("strength", medicineStrength);
      medicineBean.set("item_strength", itemStrength);
      
      if (!consUomId.equals("")) {
        medicineBean.set("cons_uom_id", Integer.parseInt(consUomId));
      } else {
        medicineBean.set("cons_uom_id", null);
      }
      
      medicineBean.set("special_instr", specialInstructions);

      if (!itemStrengthUnit.equals("")) {
        medicineBean.set("item_strength_units", Integer.parseInt(itemStrengthUnit));
      } else {
        medicineBean.set("item_strength_units", null);
      }
      
      if (!itemFormId.equals("")) {
        medicineBean.set("item_form_id", Integer.parseInt(itemFormId));
      } else {
        medicineBean.set("item_form_id", null);
      }

      if (!routeOfAdmin.equals("")) {
        medicineBean.set("route_of_admin", Integer.parseInt(routeOfAdmin));
      } else {
        medicineBean.set("route_of_admin", null);
      }
      medicineBean.set("mod_time", DateUtil.getCurrentTimestamp());
      if (prescribedId.equals("_")) {
        itemPrescriptionId = patMedicationDetailsDAO.getNextSequence();
        medicineBean.set("medicine_presc_id", itemPrescriptionId);
        medicineBean.set("prescribed_date", DateUtil.getCurrentTimestamp());

        // Insert item into discharge_medication_details
        insertPrescIds.add(itemPrescriptionId);
        if (!patMedicationDetailsDAO.insert(con, medicineBean)) {
          flag = false;
          break txn;
        }
        dischargeMedicationDetailsRepository.insertAuditLog(medicineBean);
      } else {
        if (deleteItem) {
          dischargeMedicationDetailsRepository.deleteAuditLog("medicine_presc_id",
              itemPrescriptionId);
          deletePrescIds.add(itemPrescriptionId);
          if (!patMedicationDetailsDAO.delete(con, "medicine_presc_id", itemPrescriptionId)) {
            flag = false;
            break txn;
          }
        } else {
          updatePrescIds.add(itemPrescriptionId);
          Map keys = new HashMap();
          keys.put("medicine_presc_id", itemPrescriptionId);
          medicineBean.set("mod_time", new Timestamp(new java.util.Date().getTime()));
          if (patMedicationDetailsDAO.update(con, medicineBean.getMap(), keys) <= 0) {
            flag = false;
            break txn;
          }
          dischargeMedicationDetailsRepository.updateAuditLog(medicineBean, keys);
        }
      }
    }

    Map resultMap = new HashMap();
    resultMap.put("success", flag);
    resultMap.put("itemBean", medicineBean);
    return resultMap;
  }

  /**
   * Save and update discharge medication.
   *
   * @param con the con
   * @param medicationId the medication id
   * @param saveOrUpdate the save or update
   * @param doctorId the doctor id
   * @param patientId the patient id
   * @param userName the user name
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean saveAndUpdateDischargeMedication(
      Connection con,
      Integer medicationId,
      String saveOrUpdate,
      String doctorId,
      String patientId,
      String userName)
      throws SQLException, IOException {
    boolean flag = false;
    boolean success = true;
    DischargeMedicationDAO dmdao = new DischargeMedicationDAO();
    BasicDynaBean medicationBean = dmdao.getBean();

    if (saveOrUpdate != null && saveOrUpdate.equals("update")) {
      Map<String, Object> columndata = new HashMap<String, Object>();
      columndata.put("doctor_id", doctorId);
      columndata.put("user_name", userName);
      columndata.put("mod_time", DateUtil.getCurrentTimestamp());
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("medication_id", medicationId);
      flag = success && dmdao.update(con, columndata, keys) >= 0;
    } else {
      medicationBean.set("medication_id", medicationId);
      medicationBean.set("visit_id", patientId);
      medicationBean.set("doctor_id", doctorId);
      medicationBean.set("user_name", userName);
      medicationBean.set("mod_time", DateUtil.getCurrentTimestamp());
      flag = success && dmdao.insert(con, medicationBean);
    }

    return flag;
  }
}
