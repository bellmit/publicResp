package com.insta.hms.core.inventory.stocks;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StoreCategoryService {

  @LazyAutowired
  private StoreCategoryRepository storeCatRepo;


  /**
   * List all item categories.
   *
   * @param columns Columns of table
   * @param filterMap filtering conditions
   * @param sortColumn Column to sort
   *
   * @return List
   */
  public List<BasicDynaBean> listAll(List<String> columns, Map<String, Object> filterMap, String sortColumn) {
    return storeCatRepo.listAll(columns, filterMap, sortColumn);
  }

  /**
   * Get Category Details By Id.
   * 
   * @param id the category_id
   * @return bean
   */
  public BasicDynaBean getCategoryDetailsById(int id) {
    return storeCatRepo.findByKey("category_id", id);
  }
}
