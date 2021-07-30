package com.insta.hms.core.clinical.prescriptions;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class PatientMedicinePrescriptionsService.
 *
 * @author anup vishwas
 */
@Service
public class PatientMedicinePrescriptionsService {

  /** The medicine presc repo. */
  @LazyAutowired
  private PatientMedicinePrescriptionsRepository medicinePrescRepo;
  
  @LazyAutowired
  private HealthAuthorityPreferencesService healthAuthorityPreferencesService;

  /**
   * Gets the presc medicines for consultation.
   *
   * @param consultationId  the consultation id
   * @param helathAuthority the helath authority
   * @return the presc medicines for consultation
   */
  public List<BasicDynaBean> getPrescMedicinesForConsultation(int consultationId,
      String helathAuthority) {
    return medicinePrescRepo.getPrescMedicinesForConsultation(consultationId, helathAuthority);
  }

  /**
   * Gets the pres med for treatment sheet.
   *
   * @param consultationId the consultation id
   * @return the pres med for treatment sheet
   */
  public List<BasicDynaBean> getPresMedForTreatmentSheet(int consultationId) {
    return medicinePrescRepo.getPresMedForTreatmentSheet(consultationId);
  }
  
  /**
   * Filters medicine presc ids which is set to is_drug category if it is not prescribed by
   * generics.
   * 
   * @param prescIds the list of prescids
   * @return list
   */
  public List<Integer> filterMedicinePrescIds(List<Integer> prescIds, boolean isPrescDeleted) {
    List<Integer> medPrescIds = new ArrayList<>();
    if (prescIds != null && !prescIds.isEmpty()) {
      List<BasicDynaBean> prescBeans =
          medicinePrescRepo.filterMedicinePrescOfIsDrugCategory(prescIds, isPrescDeleted);
      for (BasicDynaBean bean : prescBeans) {
        medPrescIds.add((int) bean.get("op_medicine_pres_id"));
      }
    }
    return medPrescIds;
  }

  public BasicDynaBean getBean() {
    return medicinePrescRepo.getBean();
  }

  public Integer update(BasicDynaBean medicinePrescBean, Map<String, Object> updateKeyMap) {
    return medicinePrescRepo.update(medicinePrescBean, updateKeyMap);
  }

}
