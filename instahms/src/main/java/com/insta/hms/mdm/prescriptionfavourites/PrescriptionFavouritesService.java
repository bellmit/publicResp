package com.insta.hms.mdm.prescriptionfavourites;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrescriptionFavouritesService {

  @LazyAutowired private PrescriptionFavouritesRepository repo;
  @LazyAutowired private SessionService sessionService;
  @LazyAutowired private GenericPreferencesService genPrefService;
  @LazyAutowired private HealthAuthorityPreferencesService healthAuthorityPrefService;
  @LazyAutowired private PrescriptionDoctorOtherFavouriteRepository presDocOtherFavRepo;
  @LazyAutowired private PrescriptionMedicineFavouriteRepository presMedicineFavRepo;
  @LazyAutowired private PrescriptionOtherMedicineFavouriteRepository presOtherMedicineFavRepo;
  @LazyAutowired private PrescriptionDoctorFavouriteRepository presDocFavRepo;
  @LazyAutowired private PrescriptionOperationFavouriteRepository presOperationFavRepo;
  @LazyAutowired private PrescriptionServiceFavouriteRepository presServiceFavRepo;
  @LazyAutowired private PrescriptionTestFavouriteRepository presTestFavRepo;
  @LazyAutowired private PrescriptionFavouritesValidator presFavValidator;

  /**
   * return consultation prescriptions.
   * @param presType presc type
   * @param doctorId doctor ID 
   * @param patientType patient type
   * @param bedType bed type
   * @param orgId org ID
   * @param planId plan ID
   * @param tpaId tpa ID
   * @param centerId center ID
   * @param searchQuery search query
   * @param pageNo page number
   * @param nonHospMedicine non hospital medicine
   * @return list
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getPrescriptionsForConsultation(
      String presType,
      String doctorId,
      String patientType,
      String bedType,
      String orgId,
      Integer planId,
      String tpaId,
      Integer centerId,
      String searchQuery,
      Integer pageNo,
      Boolean nonHospMedicine) {
    Map<String, Object> sessionnAttributes = sessionService.getSessionAttributes();

    String healthAuthority = (String) sessionnAttributes.get("loginCenterHealthAuthority");

    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("health_authority", sessionnAttributes.get("loginCenterHealthAuthority"));

    boolean generics =
        ((String) healthAuthorityPrefService.findByPk(filterMap).get("prescriptions_by_generics"))
            .equalsIgnoreCase("Y");

    Boolean presFromStores =
        ((String) genPrefService.getAllPreferences().get("prescription_uses_stores"))
            .equalsIgnoreCase("Y");

    return ConversionUtils.listBeanToListMap(
        repo.getPrescriptionsForConsultation(
            presType,
            doctorId,
            patientType,
            bedType,
            orgId,
            planId,
            presFromStores,
            generics,
            tpaId,
            centerId,
            healthAuthority,
            searchQuery,
            PrescriptionsService.ITEMS_LIMIT,
            pageNo,
            nonHospMedicine));
  }

  /**
   * insert prescription doctor other favourites.
   * @param prescOtherDocBean bean
   * @param doctorId doctor ID
   * @param specialInstruction instruction
   * @param errMap error map
   * @return result
   */
  public boolean insertPresDocOtherFavourite(
      BasicDynaBean prescOtherDocBean,
      String doctorId,
      String specialInstruction,
      ValidationErrorMap errMap) {

    boolean success = true;
    List<String> selectedCols = new ArrayList<String>();
    selectedCols.add("frequency");
    selectedCols.add("item_remarks");
    selectedCols.add("strength");
    selectedCols.add("item_name");
    selectedCols.add("cons_uom_id");
    selectedCols.add("item_strength");
    selectedCols.add("item_strength_units");
    selectedCols.add("duration");
    selectedCols.add("duration_units");
    selectedCols.add("medicine_quantity");
    selectedCols.add("item_form_id");
    selectedCols.add("non_hosp_medicine");

    List<BasicDynaBean> docOtherFavList =
        presDocOtherFavRepo.listAll(selectedCols, "doctor_id", doctorId);
    if (!presFavValidator.isDuplicatePresOtherDoctorFavourite(
        docOtherFavList, prescOtherDocBean, doctorId, errMap)) {
      BasicDynaBean prescOtherDocFavBean = presDocOtherFavRepo.getBean();
      prescOtherDocFavBean.set("favourite_id", presDocOtherFavRepo.getNextSequence());
      prescOtherDocFavBean.set("doctor_id", doctorId);
      prescOtherDocFavBean.set("display_order", 1);
      prescOtherDocFavBean.set("item_name", prescOtherDocBean.get("item_name"));
      prescOtherDocFavBean.set("item_remarks", prescOtherDocBean.get("item_remarks"));
      prescOtherDocFavBean.set("frequency", prescOtherDocBean.get("frequency"));
      prescOtherDocFavBean.set("duration", prescOtherDocBean.get("duration"));
      prescOtherDocFavBean.set("duration_units", prescOtherDocBean.get("duration_units"));
      prescOtherDocFavBean.set("medicine_quantity", prescOtherDocBean.get("medicine_quantity"));
      prescOtherDocFavBean.set("strength", prescOtherDocBean.get("strength"));
      prescOtherDocFavBean.set("item_form_id", prescOtherDocBean.get("item_form_id"));
      prescOtherDocFavBean.set("item_strength", prescOtherDocBean.get("item_strength"));
      prescOtherDocFavBean.set("item_strength_units", prescOtherDocBean.get("item_strength_units"));
      prescOtherDocFavBean.set("non_hosp_medicine", prescOtherDocBean.get("non_hosp_medicine"));
      prescOtherDocFavBean.set("cons_uom_id", prescOtherDocBean.get("cons_uom_id"));
      prescOtherDocFavBean.set("admin_strength", prescOtherDocBean.get("admin_strength"));
      prescOtherDocFavBean.set("special_instr", specialInstruction);

      success = presDocOtherFavRepo.insert(prescOtherDocFavBean) == 1;
    }
    return success;
  }

  /**
   * insert prescription medicine favourite.
   * @param presMedBean bean
   * @param doctorId doctor ID
   * @param specialInstruction instruction
   * @param errMap error map
   * @return result
   */
  public boolean insertPresMedicineFavourite(
      BasicDynaBean presMedBean,
      String doctorId,
      String specialInstruction,
      ValidationErrorMap errMap) {

    boolean success = true;
    List<String> selectedCols = new ArrayList<String>();
    selectedCols.add("doctor_id");
    selectedCols.add("frequency");
    selectedCols.add("medicine_remarks");
    selectedCols.add("strength");
    selectedCols.add("generic_code");
    selectedCols.add("cons_uom_id");
    selectedCols.add("item_strength");
    selectedCols.add("item_strength_units");
    selectedCols.add("duration");
    selectedCols.add("duration_units");
    selectedCols.add("medicine_quantity");
    selectedCols.add("medicine_id");
    selectedCols.add("route_of_admin");
    selectedCols.add("item_form_id");

    List<BasicDynaBean> medFavList =
        presMedicineFavRepo.listAll(selectedCols, "doctor_id", doctorId);

    if (!presFavValidator.isDuplicatePresMedicineFavourite(
        medFavList, presMedBean, doctorId, errMap)) {
      BasicDynaBean presMedFavBean = presMedicineFavRepo.getBean();
      presMedFavBean.set("favourite_id", presMedicineFavRepo.getNextSequence());
      presMedFavBean.set("doctor_id", doctorId);
      presMedFavBean.set("display_order", 1);
      presMedFavBean.set("frequency", presMedBean.get("frequency"));
      presMedFavBean.set("duration", presMedBean.get("duration"));
      presMedFavBean.set("duration_units", presMedBean.get("duration_units"));
      presMedFavBean.set("medicine_quantity", presMedBean.get("medicine_quantity"));
      presMedFavBean.set("medicine_remarks", presMedBean.get("medicine_remarks"));
      presMedFavBean.set("medicine_id", presMedBean.get("medicine_id"));
      presMedFavBean.set("route_of_admin", presMedBean.get("route_of_admin"));
      presMedFavBean.set("strength", presMedBean.get("strength"));
      presMedFavBean.set("generic_code", presMedBean.get("generic_code"));
      presMedFavBean.set("item_form_id", presMedBean.get("item_form_id"));
      presMedFavBean.set("item_strength", presMedBean.get("item_strength"));
      presMedFavBean.set("item_strength_units", presMedBean.get("item_strength_units"));
      presMedFavBean.set("cons_uom_id", presMedBean.get("cons_uom_id"));
      presMedFavBean.set("admin_strength", presMedBean.get("admin_strength"));
      presMedFavBean.set("special_instr", specialInstruction);

      success = presMedicineFavRepo.insert(presMedFavBean) == 1;
    } else {
      success = false;
    }
    return success;
  }

  /**
   * insert prescription other medicine favourites.
   * @param presOtherMedBean bean
   * @param doctorId doctor ID
   * @param specialInstruction instruction
   * @param errMap error map
   * @return result
   */
  public boolean insertPresOtherMedicineFavourite(
      BasicDynaBean presOtherMedBean,
      String doctorId,
      String specialInstruction,
      ValidationErrorMap errMap) {

    boolean success = true;
    List<String> selectedCols = new ArrayList<String>();
    selectedCols.add("doctor_id");
    selectedCols.add("frequency");
    selectedCols.add("medicine_remarks");
    selectedCols.add("strength");
    selectedCols.add("medicine_name");
    selectedCols.add("cons_uom_id");
    selectedCols.add("item_strength");
    selectedCols.add("item_strength_units");
    selectedCols.add("duration");
    selectedCols.add("duration_units");
    selectedCols.add("medicine_quantity");
    selectedCols.add("route_of_admin");
    selectedCols.add("item_form_id");

    List<BasicDynaBean> otherMedFavList =
        presOtherMedicineFavRepo.listAll(selectedCols, "doctor_id", doctorId);
    if (!presFavValidator.isDuplicatePresOtherMedicineFavourite(
        otherMedFavList, presOtherMedBean, doctorId, errMap)) {

      BasicDynaBean prescOtherMedFavBean = presOtherMedicineFavRepo.getBean();
      prescOtherMedFavBean.set("favourite_id", presOtherMedicineFavRepo.getNextSequence());
      prescOtherMedFavBean.set("doctor_id", doctorId);
      prescOtherMedFavBean.set("display_order", 1);
      prescOtherMedFavBean.set("medicine_name", presOtherMedBean.get("medicine_name"));
      prescOtherMedFavBean.set("frequency", presOtherMedBean.get("frequency"));
      prescOtherMedFavBean.set("duration", presOtherMedBean.get("duration"));
      prescOtherMedFavBean.set("duration_units", presOtherMedBean.get("duration_units"));
      prescOtherMedFavBean.set("medicine_quantity", presOtherMedBean.get("medicine_quantity"));
      prescOtherMedFavBean.set("medicine_remarks", presOtherMedBean.get("medicine_remarks"));
      prescOtherMedFavBean.set("route_of_admin", presOtherMedBean.get("route_of_admin"));
      prescOtherMedFavBean.set("strength", presOtherMedBean.get("strength"));
      prescOtherMedFavBean.set("item_form_id", presOtherMedBean.get("item_form_id"));
      prescOtherMedFavBean.set("item_strength", presOtherMedBean.get("item_strength"));
      prescOtherMedFavBean.set("item_strength_units", presOtherMedBean.get("item_strength_units"));
      prescOtherMedFavBean.set("cons_uom_id", presOtherMedBean.get("cons_uom_id"));
      prescOtherMedFavBean.set("admin_strength", presOtherMedBean.get("admin_strength"));
      prescOtherMedFavBean.set("special_instr", specialInstruction);

      success = presOtherMedicineFavRepo.insert(prescOtherMedFavBean) == 1;
    } else {
      success = false;
    }
    return success;
  }

  /**
   * insert prescription doctor favourite.
   * @param prescDocBean bean
   * @param prescription prescription
   * @param errMap error map
   * @return result
   */
  public boolean insertPresDoctorFavourite(
      BasicDynaBean prescDocBean, Map<String, Object> prescription, ValidationErrorMap errMap) {
    boolean success = true;
    List<String> selectedCols = new ArrayList<String>();
    selectedCols.add("doctor_id");
    selectedCols.add("consultation_remarks");
    List<BasicDynaBean> doctorFavList =
        presDocFavRepo.listAll(selectedCols, "doctor_id", prescription.get("doctor_id"));
    if (!presFavValidator.isDuplicatePresDoctorFavourite(
        doctorFavList, prescDocBean, (String) prescription.get("doctor_id"), errMap)) {
      BasicDynaBean prescDocFavBean = presDocFavRepo.getBean();
      prescDocFavBean.set("favourite_id", presDocFavRepo.getNextSequence());
      prescDocFavBean.set("doctor_id", (String) prescription.get("doctor_id"));
      prescDocFavBean.set("display_order", 1);
      prescDocFavBean.set("cons_doctor_id", prescDocBean.get("doctor_id"));
      prescDocFavBean.set("consultation_remarks", prescDocBean.get("cons_remarks"));
      prescDocFavBean.set("special_instr", (String) prescription.get("special_instr"));

      success = presDocFavRepo.insert(prescDocFavBean) == 1;
    }
    return success;
  }

  /**
   * insert prescription operation favourites.
   * @param prescOperationBean bean
   * @param doctorId doctor ID
   * @param specialInstruction instruction
   * @param errMap error map
   * @return result
   */
  public boolean insertPresOperationFavourite(
      BasicDynaBean prescOperationBean,
      String doctorId,
      String specialInstruction,
      ValidationErrorMap errMap) {
    boolean success = true;
    List<String> selectedCols = new ArrayList<String>();
    selectedCols.add("doctor_id");
    selectedCols.add("operation_id");
    selectedCols.add("remarks");
    List<BasicDynaBean> operationFavList =
        presOperationFavRepo.listAll(selectedCols, "doctor_id", doctorId);
    if (!presFavValidator.isDuplicatePresOperationFavourite(
        operationFavList, prescOperationBean, doctorId, errMap)) {

      BasicDynaBean prescOperationFavBean = presOperationFavRepo.getBean();
      prescOperationFavBean.set("favourite_id", presOperationFavRepo.getNextSequence());
      prescOperationFavBean.set("doctor_id", doctorId);
      prescOperationFavBean.set("display_order", 1);
      prescOperationFavBean.set("operation_id", prescOperationBean.get("operation_id"));
      prescOperationFavBean.set("remarks", prescOperationBean.get("remarks"));
      prescOperationFavBean.set("special_instr", specialInstruction);

      success = presOperationFavRepo.insert(prescOperationFavBean) == 1;
    } else {
      success = false;
    }
    return success;
  }

  /**
   * insert prescription service favourites.
   * @param prescServiceBean bean
   * @param doctorId doctor ID
   * @param specialInstruction instruction
   * @param errMap error map
   * @return result
   */
  public boolean insertPresServiceFavourite(
      BasicDynaBean prescServiceBean,
      String doctorId,
      String specialInstruction,
      ValidationErrorMap errMap) {
    boolean success = true;
    List<String> selectedCols = new ArrayList<String>();
    selectedCols.add("doctor_id");
    selectedCols.add("service_id");
    selectedCols.add("service_remarks");
    List<BasicDynaBean> serviceFavList =
        presServiceFavRepo.listAll(selectedCols, "doctor_id", doctorId);
    if (!presFavValidator.isDuplicatePresServiceFavourite(
        serviceFavList, prescServiceBean, doctorId, errMap)) {

      BasicDynaBean prescOperationFavBean = presServiceFavRepo.getBean();
      prescOperationFavBean.set("favourite_id", presServiceFavRepo.getNextSequence());
      prescOperationFavBean.set("doctor_id", doctorId);
      prescOperationFavBean.set("display_order", 1);
      prescOperationFavBean.set("service_id", prescServiceBean.get("service_id"));
      prescOperationFavBean.set("service_remarks", prescServiceBean.get("service_remarks"));
      prescOperationFavBean.set("special_instr", specialInstruction);

      success = presServiceFavRepo.insert(prescOperationFavBean) == 1;
    }
    return success;
  }

  /**
   * insert prescription test favourites.
   * @param prescTestBean bean
   * @param doctorId doctor ID
   * @param specialInstruction instruction
   * @param errMap error map
   * @return result
   */
  public boolean insertPresTestFavourite(
      BasicDynaBean prescTestBean,
      String doctorId,
      String specialInstruction,
      ValidationErrorMap errMap) {
    boolean success = true;
    List<String> selectedCols = new ArrayList<String>();
    selectedCols.add("doctor_id");
    selectedCols.add("test_id");
    selectedCols.add("test_remarks");
    selectedCols.add("ispackage");
    List<BasicDynaBean> testFavList = presTestFavRepo.listAll(selectedCols, "doctor_id", doctorId);
    if (!presFavValidator.isDuplicatePresTestFavourite(
        testFavList, prescTestBean, doctorId, errMap)) {

      BasicDynaBean prescTestFavBean = presTestFavRepo.getBean();
      prescTestFavBean.set("favourite_id", presTestFavRepo.getNextSequence());
      prescTestFavBean.set("doctor_id", doctorId);
      prescTestFavBean.set("display_order", 1);
      prescTestFavBean.set("test_id", prescTestBean.get("test_id"));
      prescTestFavBean.set("test_remarks", prescTestBean.get("test_remarks"));
      prescTestFavBean.set("ispackage", prescTestBean.get("ispackage"));
      prescTestFavBean.set("special_instr", specialInstruction);

      success = presTestFavRepo.insert(prescTestFavBean) == 1;
    }
    return success;
  }
}
