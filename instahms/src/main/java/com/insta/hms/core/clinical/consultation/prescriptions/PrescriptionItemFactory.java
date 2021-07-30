package com.insta.hms.core.clinical.consultation.prescriptions;

import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.stereotype.Component;

@Component
public class PrescriptionItemFactory {

  @LazyAutowired
  private MedicineItemService medicineItemService;
  @LazyAutowired
  private OtherMedicineItemService otherMedicineItemService;
  @LazyAutowired
  private InvestigationItemService investigationItemService;
  @LazyAutowired
  private ServiceItemService serviceItemService;
  @LazyAutowired
  private OperationItemService operationItemService;
  @LazyAutowired
  private DoctorItemService doctorItemService;
  @LazyAutowired
  private NonHospitalItemService nonHospitalItemService;

  /**
   * Gets item service.
   * @param item the string
   * @param usesStores the boolean
   * @param nonHospitalMedicine the boolean
   * @return PrescriptionItem
   */
  public PrescriptionItem getItemService(String item, Boolean usesStores,
      Boolean nonHospitalMedicine) {
    if (PrescriptionsService.NON_HOSPITAL.equals(item)
        || (PrescriptionsService.MEDICINE.equals(item) && nonHospitalMedicine)
        || PrescriptionsService.NON_BILLABLE.equals(item)) {
      return nonHospitalItemService;
    } else if (PrescriptionsService.MEDICINE.equals(item) && usesStores) {
      return medicineItemService;
    } else if (PrescriptionsService.MEDICINE.equals(item) && !usesStores) {
      return otherMedicineItemService;
    } else if (PrescriptionsService.INVESTIGATION.equals(item)) {
      return investigationItemService;
    } else if (PrescriptionsService.SERVICE.equals(item)) {
      return serviceItemService;
    } else if (PrescriptionsService.OPERATION.equals(item)) {
      return operationItemService;
    } else if (PrescriptionsService.DOCTOR.equals(item)) {
      return doctorItemService;
    }
    return null;
  }
}
