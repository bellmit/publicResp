package com.insta.hms.mdm.insuranceitemcategories;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class InsuranceItemCategoryService.
 */
@Service("insuranceItemCategoryService")
public class InsuranceItemCategoryService extends MasterService {

  /**
   * Instantiates a new insurance item category service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public InsuranceItemCategoryService(InsuranceItemCategoryRepository repository,
      InsuranceItemCategoryValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
    map.put("InsuranceCategoriesLists", lookup(false));
    return map;
  }

  /**
   * Gets the nsurance category map.
   *
   * @return the nsurance category map
   */
  public Map<String, Integer> getnsuranceCategoryMap() {
    HashMap<String, Integer> insuranceCategoryMap = new HashMap<String, Integer>();
    List<BasicDynaBean> list = ((InsuranceItemCategoryRepository) getRepository())
        .getInsuranceCategoryList();
    for (BasicDynaBean bean : list) {
      insuranceCategoryMap.put((String) bean.get("insurance_category_name"),
          (Integer) bean.get("insurance_category_id"));
    }
    return insuranceCategoryMap;
  }

  /**
   * List insurance category.
   *
   * @param columns the columns
   * @param filterMap the filter map
   * @param sortColumn the sort column
   * @return the list
   */
  public List<BasicDynaBean> listInsuranceCategory(List<String> columns,
      Map<String, Object> filterMap, String sortColumn) {
    return ((InsuranceItemCategoryRepository) getRepository()).listAll(columns, filterMap,
        sortColumn);
  }

  /**
   * Gets the item insurance category.
   *
   * @return the item insurance category
   */
  public List<BasicDynaBean> getItemInsuranceCategory() {
    return ((InsuranceItemCategoryRepository) getRepository()).getInsuranceCategoryList();
  }
}