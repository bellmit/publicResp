package com.insta.hms.mdm.prescriptionfavourites;

import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PrescriptionFavouritesValidator {

  Logger logger = LoggerFactory
      .getLogger(com.insta.hms.mdm.prescriptionfavourites.PrescriptionFavouritesValidator.class);

  /**
   * check for duplicates.
   * @param medFavList favourites list
   * @param presMedBean bean
   * @param doctorId doctor ID
   * @param errMap error map
   * @return result
   */
  public boolean isDuplicatePresMedicineFavourite(
      List<BasicDynaBean> medFavList,
      BasicDynaBean presMedBean,
      String doctorId,
      ValidationErrorMap errMap) {
    boolean isDuplicate = false;
    for (BasicDynaBean bean : medFavList) {
      if (!stringsEqual((String) presMedBean.get("frequency"), (String) bean.get("frequency"))) {
        continue;
      }
      if (!stringsEqual(
          (String) presMedBean.get("medicine_remarks"), (String) bean.get("medicine_remarks"))) {
        continue;
      }
      if (!stringsEqual((String) presMedBean.get("strength"), (String) bean.get("strength"))) {
        continue;
      }
      if (!stringsEqual(
          (String) presMedBean.get("generic_code"), (String) bean.get("generic_code"))) {
        continue;
      }
      if (!intEqual((Integer) presMedBean.get("cons_uom_id"),(Integer) bean.get("cons_uom_id"))) {
        continue;
      }
      if (!stringsEqual(
          (String) presMedBean.get("item_strength"), (String) bean.get("item_strength"))) { 
        continue;
      }
      if (!intEqual(
          (Integer) presMedBean.get("item_strength_units"),
          (Integer) bean.get("item_strength_units"))) {
        continue;
      }
      if (!intEqual((Integer) presMedBean.get("duration"), (Integer) bean.get("duration"))) {
        continue;
      }
      if (!stringsEqual(
          (String) presMedBean.get("duration_units"), (String) bean.get("duration_units"))) {
        continue;
      }
      if (!intEqual(
          (Integer) presMedBean.get("medicine_quantity"), 
          (Integer) bean.get("medicine_quantity"))) {
        continue;
      }
      if (!intEqual((Integer) presMedBean.get("medicine_id"), (Integer) bean.get("medicine_id"))) {
        continue;
      }
      if (!intEqual(
          (Integer) presMedBean.get("route_of_admin"), (Integer) bean.get("route_of_admin"))) {
        continue;
      }
      if (!intEqual((Integer) presMedBean.get("item_form_id"),
          (Integer) bean.get("item_form_id"))) {
        continue;
      }
      // all the values are same
      isDuplicate = true;
      break;
    }
    if (isDuplicate) {
      logger.info("Duplicate Medicine Favourites.");
    }
    return isDuplicate;
  }

  /**
   * check for duplicates.
   * @param otherMedFavList other favourites list
   * @param otherMedBean bean
   * @param doctorId doctor ID
   * @param errMap error map
   * @return result 
   */
  public boolean isDuplicatePresOtherMedicineFavourite(
      List<BasicDynaBean> otherMedFavList,
      BasicDynaBean otherMedBean,
      String doctorId,
      ValidationErrorMap errMap) {
    boolean isDuplicate = false;

    for (BasicDynaBean bean : otherMedFavList) {
      if (!stringsEqual((String) otherMedBean.get("frequency"), (String) bean.get("frequency"))) {
        continue;
      }
      if (!stringsEqual(
          (String) otherMedBean.get("medicine_remarks"), (String) bean.get("medicine_remarks"))) {
        continue;
      }
      if (!stringsEqual((String) otherMedBean.get("strength"), (String) bean.get("strength"))) {
        continue;
      }
      if (!stringsEqual(
          (String) otherMedBean.get("medicine_name"), (String) bean.get("medicine_name"))) {
        continue;
      }
      if (!intEqual((Integer) otherMedBean.get("cons_uom_id"),(Integer) bean.get("cons_uom_id"))) {
        continue;
      }
      if (!stringsEqual(
          (String) otherMedBean.get("item_strength"), (String) bean.get("item_strength"))) {
        continue;
      }
      if (!intEqual(
          (Integer) otherMedBean.get("item_strength_units"),
          (Integer) bean.get("item_strength_units"))) { 
        continue;
      }
      if (!intEqual((Integer) otherMedBean.get("duration"), (Integer) bean.get("duration"))) {
        continue;
      }
      if (!stringsEqual(
          (String) otherMedBean.get("duration_units"), (String) bean.get("duration_units"))) {
        continue;
      }
      if (!intEqual(
          (Integer) otherMedBean.get("medicine_quantity"),
          (Integer) bean.get("medicine_quantity"))) {
        continue;
      }
      if (!intEqual(
          (Integer) otherMedBean.get("route_of_admin"), (Integer) bean.get("route_of_admin"))) {
        continue;
      }
      if (!intEqual((Integer) otherMedBean.get("item_form_id"),
          (Integer) bean.get("item_form_id"))) {
        continue;
      }
      // all the values are same
      isDuplicate = true;
      break;
    }
    if (isDuplicate) {
      logger.info("Duplicate Other Medicine Favourites.");
    }

    return isDuplicate;
  }

  /**
   * check for test duplicates.
   * @param testFavList test favourites list
   * @param testBean bean
   * @param doctorId doctor ID
   * @param errMap error map
   * @return result
   */
  public boolean isDuplicatePresTestFavourite(
      List<BasicDynaBean> testFavList,
      BasicDynaBean testBean,
      String doctorId,
      ValidationErrorMap errMap) {
    boolean isDuplicate = false;
    for (BasicDynaBean bean : testFavList) {
      if (!stringsEqual((String) testBean.get("test_id"), (String) bean.get("test_id"))) {
        continue;
      }
      if (!stringsEqual((String) testBean.get("test_remarks"), (String) bean.get("test_remarks"))) {
        continue;
      }
      if (!((Boolean) testBean.get("ispackage")).equals((Boolean) bean.get("ispackage"))) { 
        continue;
      }

      // all the values are same
      isDuplicate = true;
      break;
    }
    if (isDuplicate) {
      logger.info("Duplicate Investigation Favourites.");
    }

    return isDuplicate;
  }

  /**
   * check for service favourites duplicates.
   * @param serviceFavList service favourites list
   * @param serviceBean bean
   * @param doctorId doctor ID
   * @param errMap error map
   * @return result
   */
  public boolean isDuplicatePresServiceFavourite(
      List<BasicDynaBean> serviceFavList,
      BasicDynaBean serviceBean,
      String doctorId,
      ValidationErrorMap errMap) {
    boolean isDuplicate = false;
    for (BasicDynaBean bean : serviceFavList) {
      if (!stringsEqual((String) serviceBean.get("service_id"), (String) bean.get("service_id"))) {
        continue;
      }
      if (!stringsEqual(
          (String) serviceBean.get("service_remarks"), (String) bean.get("service_remarks"))) {
        continue;
      }

      // all the values are same
      isDuplicate = true;
      break;
    }
    if (isDuplicate) {
      logger.info("Duplicate Service Favourites.");
    }

    return isDuplicate;
  }

  /**
   * check for duplicate operation favourites.
   * @param operationFavList operation favourites list
   * @param operationBean bean
   * @param doctorId doctor ID
   * @param errMap error map
   * @return result
   */
  public boolean isDuplicatePresOperationFavourite(
      List<BasicDynaBean> operationFavList,
      BasicDynaBean operationBean,
      String doctorId,
      ValidationErrorMap errMap) {
    boolean isDuplicate = false;
    for (BasicDynaBean bean : operationFavList) {
      if (!stringsEqual(
          (String) operationBean.get("operation_id"), (String) bean.get("operation_id"))) { 
        continue;
      }
      if (!stringsEqual((String) operationBean.get("remarks"), (String) bean.get("remarks"))) {
        continue;
      }
      // all the values are same
      isDuplicate = true;
      break;
    }
    if (isDuplicate) {
      logger.info("Duplicate Operation Favourites.");
    }

    return isDuplicate;
  }

  /**
   * check for duplicate presc doctor favourite.
   * @param doctorFavList doctor favourites list
   * @param doctorBean bean
   * @param doctorId doctor ID
   * @param errMap error map
   * @return result
   */
  public boolean isDuplicatePresDoctorFavourite(
      List<BasicDynaBean> doctorFavList,
      BasicDynaBean doctorBean,
      String doctorId,
      ValidationErrorMap errMap) {
    boolean isDuplicate = false;
    for (BasicDynaBean bean : doctorFavList) {
      if (!stringsEqual((String) doctorBean.get("doctor_id"), (String) bean.get("doctor_id"))) {
        continue;
      }
      if (!stringsEqual(
          (String) doctorBean.get("cons_remarks"), (String) bean.get("consultation_remarks"))) {
        continue;
      }
      // all the values are same
      isDuplicate = true;
      break;
    }
    if (isDuplicate) {
      logger.info("Duplicate Doctor Favourites.");
    }

    return isDuplicate;
  }

  /**
   * check for duplicate presc other doctor favourites.
   * @param otherDoctorFavList list
   * @param otherDoctorBean bean
   * @param doctorId doctor ID
   * @param errMap error map
   * @return result
   */
  public boolean isDuplicatePresOtherDoctorFavourite(
      List<BasicDynaBean> otherDoctorFavList,
      BasicDynaBean otherDoctorBean,
      String doctorId,
      ValidationErrorMap errMap) {
    boolean isDuplicate = false;
    for (BasicDynaBean bean : otherDoctorFavList) {
      if (!stringsEqual((String) otherDoctorBean.get("frequency"),
          (String) bean.get("frequency"))) {
        continue;
      }
      if (!stringsEqual(
          (String) otherDoctorBean.get("item_remarks"), (String) bean.get("item_remarks"))) {
        continue;
      }
      if (!stringsEqual((String) otherDoctorBean.get("strength"), (String) bean.get("strength"))) {
        continue;
      }
      if (!stringsEqual((String) otherDoctorBean.get("item_name"), 
          (String) bean.get("item_name"))) {
        continue;
      }
      if (!stringsEqual(
          (String) otherDoctorBean.get("consumption_uom"), (String) bean.get("consumption_uom"))) {
        continue;
      }
      if (!stringsEqual(
          (String) otherDoctorBean.get("item_strength"), (String) bean.get("item_strength"))) {
        continue;
      }
      if (!intEqual(
          (Integer) otherDoctorBean.get("item_strength_units"),
          (Integer) bean.get("item_strength_units"))) {
        continue;
      }

      if (!intEqual((Integer) otherDoctorBean.get("duration"), (Integer) bean.get("duration"))) {
        continue;
      }
      if (!stringsEqual(
          (String) otherDoctorBean.get("duration_units"), (String) bean.get("duration_units"))) {
        continue;
      }
      if (!intEqual(
          (Integer) otherDoctorBean.get("medicine_quantity"),
          (Integer) bean.get("medicine_quantity"))) { 
        continue;
      }
      if (!intEqual(
          (Integer) otherDoctorBean.get("item_form_id"), (Integer) bean.get("item_form_id"))) {
        continue;
      }
      if ((Boolean) otherDoctorBean.get("non_hosp_medicine")
          != (Boolean) bean.get("non_hosp_medicine")) { 
        continue;
      }

      // all the values are same
      isDuplicate = true;
      break;
    }
    if (isDuplicate) {
      logger.info("Duplicate Other Doctor Favourites.");
    }

    return isDuplicate;
  }

  /**
   * check equality.
   * @param str first string
   * @param str1 second string
   * @return result
   */
  public static boolean stringsEqual(String str, String str1) {
    str = str == null ? "" : str;
    str1 = str1 == null ? "" : str1;
    return str.equals(str1);
  }

  /**
   * check int equality.
   * @param int1 first int
   * @param int2 second int
   * @return result
   */
  public static boolean intEqual(Integer int1, Integer int2) {
    int1 = int1 == null ? -100 : int1;
    int2 = int2 == null ? -100 : int2;
    return int1.intValue() == int2.intValue();
  }
}
