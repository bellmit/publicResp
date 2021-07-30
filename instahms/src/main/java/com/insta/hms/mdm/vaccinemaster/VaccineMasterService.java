package com.insta.hms.mdm.vaccinemaster;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.vaccinecategory.VaccineMasterCategoryMappingRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Vaccine master service.
 */
@Service
public class VaccineMasterService extends MasterService {

  @LazyAutowired
  private VaccineMasterService vaccineMasterService;

  @LazyAutowired
  private VaccineMasterCategoryMappingRepository vaccineMasterCategoryMappingRepository;

  /**
   * Instantiates a new master service.
   * 
   * @param vaccineMasterRepository the repository
   * @param vaccineMasterValidator the validator
   */
  public VaccineMasterService(VaccineMasterRepository vaccineMasterRepository,
      VaccineMasterValidator vaccineMasterValidator) {
    super(vaccineMasterRepository, vaccineMasterValidator);
  }

  /**
   * Insert vaccine item.
   * 
   * @param vaccineMasterBean the vaccine master bean
   * @param params request parameters
   * @return returns boolean
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean insertVaccine(BasicDynaBean vaccineMasterBean, Map<String, String[]> params) {
    int res = vaccineMasterService.insert(vaccineMasterBean);
    String[] selectedVaccineCategories = params.get("selectedCategories");
    if (selectedVaccineCategories != null && selectedVaccineCategories.length > 0) {
      List<BasicDynaBean> selectedCategoriesBeanList = new ArrayList<BasicDynaBean>();
      for (String selectedCategoryId : selectedVaccineCategories) {
        BasicDynaBean vaccineMasterCategoryMappingBean = vaccineMasterCategoryMappingRepository
            .getBean();
        vaccineMasterCategoryMappingBean.set("vaccine_id", vaccineMasterBean.get("vaccine_id"));
        vaccineMasterCategoryMappingBean.set("vaccine_category_id", Integer.parseInt(
            selectedCategoryId));
        selectedCategoriesBeanList.add(vaccineMasterCategoryMappingBean);
      }
      int[] results = vaccineMasterCategoryMappingRepository.batchInsert(
          selectedCategoriesBeanList);
      return res != 0 && (results != null && Arrays.stream(results).allMatch(i -> i > 0));
    }
    return true;
  }

  /**
   * Update vaccine item.
   * 
   * @param vaccineMasterBean the vaccine master bean
   * @param params request parameters
   * @return returns boolean
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean updateVaccine(BasicDynaBean vaccineMasterBean, Map<String, String[]> params) {
    int res = vaccineMasterService.update(vaccineMasterBean);
    vaccineMasterCategoryMappingRepository.deleteVaccineCategory((Integer) vaccineMasterBean.get(
        "vaccine_id"));
    String[] selectedVaccineCategories = params.get("selectedCategories");
    if (selectedVaccineCategories != null && selectedVaccineCategories.length > 0) {
      List<BasicDynaBean> selectedCategoriesBeanList = new ArrayList<BasicDynaBean>();
      for (String selectedCategoryId : selectedVaccineCategories) {
        BasicDynaBean vaccineMasterCategoryMappingBean = vaccineMasterCategoryMappingRepository
            .getBean();
        vaccineMasterCategoryMappingBean.set("vaccine_id", vaccineMasterBean.get("vaccine_id"));
        vaccineMasterCategoryMappingBean.set("vaccine_category_id", Integer.parseInt(
            selectedCategoryId));
        selectedCategoriesBeanList.add(vaccineMasterCategoryMappingBean);
      }
      int[] results = vaccineMasterCategoryMappingRepository.batchInsert(
          selectedCategoriesBeanList);
      return res != 0 && (results != null && Arrays.stream(results).allMatch(i -> i > 0));
    }
    return true;
  }
}