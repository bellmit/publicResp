package com.insta.hms.core.clinical.vaccinationsinfo;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.ipemr.IpEmrFormService;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;
import com.insta.hms.mdm.item.StoreItemDetailsRepository;
import com.insta.hms.mdm.medicineroute.MedicineRouteRepository;
import com.insta.hms.mdm.vaccinecategory.VaccineCategoryService;
import com.insta.hms.mdm.vaccinecategory.VaccineMasterCategoryMappingRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VaccinationsInfoService {

  @LazyAutowired
  private StoreItemDetailsRepository storeItemDetailsRepository;

  @LazyAutowired
  private MedicineRouteRepository medicineRouteRepository;

  @LazyAutowired
  private IpEmrFormService ipEmrFormService;

  @LazyAutowired
  private PatientRegistrationRepository patientRegistrationRepository;

  @LazyAutowired
  private VaccineMasterCategoryMappingRepository vaccineMasterCategoryMappingRepository;

  @LazyAutowired
  private VaccineCategoryService vaccineCategoryService;

  /**
   * Fetch medicine item details on query input.
   * 
   * @param query the string query
   * @return map of medicine item details
   */
  public Map<String, Object> searchMedicineItems(String query) {
    Map<String, Object> response = new HashMap<>();
    response.put("items", ConversionUtils.listBeanToListMap(storeItemDetailsRepository
        .getItemsBySearchString(query)));
    return response;
  }

  /**
   * Fetch route of administration mapped to store item.
   * 
   * @param medicineId the medicine id
   * @return map of item route details
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getItemRoutes(Integer medicineId) {
    return medicineRouteRepository.getRouteOfAdminForItem(medicineId).getMap();
    
  }

  /**
   * Fetch medicine batch item details for selected item.
   * 
   * @param mrNo the mr no
   * @param medicineId the medicine id
   * @return map of medicine item details
   */
  @SuppressWarnings("unlikely-arg-type")
  public Map<String, Object> getPatientBatchDetailsForSelectedItem(String mrNo,
      Integer medicineId) {
    BasicDynaBean visitBean = patientRegistrationRepository.getLatestActiveVisit(mrNo);
    Map<String, Object> response = new HashMap<>();
    if (visitBean != null) {
      String visitId = (String) visitBean.get("patient_id");
      Map<String, Object> results = ipEmrFormService.getMedicineBatchDetailsForPatient(visitId,
          Collections.singletonList(medicineId));
      response.put("batchDetails", results.get(String.valueOf(medicineId)));
    }
    return response;
  }

  /**
   * Fetch vaccine category list for mapped vaccine.
   * 
   * @param vaccineId the vaccine id
   * @return map of vaccine categories
   */
  public Map<String, Object> getVaccineCategoryListForSelectedVaccine(Integer vaccineId) {
    Map<String, Object> response = new HashMap<>();
    List<BasicDynaBean> mappedCategoriesBeanList = vaccineMasterCategoryMappingRepository
        .getVaccineCategory(vaccineId);
    response.put("vaccineCategoryList", ConversionUtils.listBeanToListMap(!mappedCategoriesBeanList
        .isEmpty() ? mappedCategoriesBeanList : vaccineCategoryService.getVaccineCategoryList()));
    return response;
  }

}
