package com.insta.hms.mdm.consumptionuom;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consumption UOM Service.
 * 
 * @author VinayKumarJavalkar
 */
@Service
public class ConsumptionUOMService extends MasterService {

  @LazyAutowired
  private ConsumptionUOMRepository repository;

  public ConsumptionUOMService(ConsumptionUOMRepository repository,
      ConsumptionUOMValidator validator) {
    super(repository, validator);
  }
  
  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> refData = new HashMap<>();
    refData.put("consumptionuomlist", lookup(false));
    return refData;
  }
  
  public List<BasicDynaBean> listAll(List<String> columns, String filterBy, Object filterValue) {
    return repository.listAll(columns, filterBy, filterValue);
  }
}
