package com.insta.hms.mdm.vaccinecategory;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Vaccine category service.
 */
@Service
public class VaccineCategoryService extends MasterService {

  @LazyAutowired
  private VaccineCategoryRepository vaccineCategoryRepository;

  /**
   * Instantiates a new master service.
   * 
   * @param vaccineCategoryRepository the repository
   * @param vaccineCategoryValidator the validator
   */
  public VaccineCategoryService(VaccineCategoryRepository vaccineCategoryRepository,
      VaccineCategoryValidator vaccineCategoryValidator) {
    super(vaccineCategoryRepository, vaccineCategoryValidator);
  }

  /**
   * To retrieve vaccine category list.
   *
   * @return the vaccine category list
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public List<BasicDynaBean> getVaccineCategoryList() {
    List columns = new ArrayList();
    columns.add("vaccine_category_id");
    columns.add("vaccine_category_name");
    return vaccineCategoryRepository.listAll(columns);
  }
}