package com.insta.hms.mdm.vaccinecategory;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Vaccine master category mapping repository.
 */
@Repository
public class VaccineMasterCategoryMappingRepository extends MasterRepository<Integer> {

  private static final String GET_VACCINE_CATEGORY_BY_VACCINE_ID = "Select "
      + "vmcm.vaccine_category_id,vcm.vaccine_category_name from "
      + "vaccine_master_category_mapping vmcm "
      + "JOIN vaccine_category_master vcm ON (vmcm.vaccine_category_id = vcm.vaccine_category_id) "
      + "WHERE vaccine_id = ? ";

  private static final String DELETE_VACCINE_CATEGORY_BY_VACCINE_ID = "DELETE from " 
      + " vaccine_master_category_mapping where vaccine_id = ? ";

  public VaccineMasterCategoryMappingRepository() {
    super("vaccine_master_category_mapping", "vaccine_master_category_mapping_id");
  }

  /**
   * Get vaccine categories mapped to vaccine id.
   * 
   * @param vaccineId vaccine id
   * @return returns list of BasicDynaBean
   */
  public List<BasicDynaBean> getVaccineCategory(int vaccineId) {
    return DatabaseHelper.queryToDynaList(GET_VACCINE_CATEGORY_BY_VACCINE_ID,
        new Object[] {vaccineId});
  }

  /**
   * delete vaccine and category mapping by vaccine id.
   * 
   * @param vaccineId vaccine id
   * @return returns list of BasicDynaBean
   */
  public int deleteVaccineCategory(int vaccineId) {
    return DatabaseHelper.delete(DELETE_VACCINE_CATEGORY_BY_VACCINE_ID, new Object[] {vaccineId});
  }
}
