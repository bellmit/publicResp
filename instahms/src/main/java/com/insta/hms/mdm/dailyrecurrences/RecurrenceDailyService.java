package com.insta.hms.mdm.dailyrecurrences;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The service for Recurrence Daily Master.
 * @author sainathbatthala
 */
@Service
public class RecurrenceDailyService extends MasterService {
  RecurrenceDailyValidator validator;

  public RecurrenceDailyService(RecurrenceDailyRepository repository,
      RecurrenceDailyValidator validator) {
    super(repository, validator);
    this.validator = validator;
  }

  /**
   * This methods deletes the entries.
   * 
   * @param deleteIds The Parameter of delete method
   * @return The return type is Integer.
   */
  @Transactional
  public Integer delete(String[] deleteIds) {
    validator.validateDelete();
    batchDelete(deleteIds, false);
    return 1;
  }

  /**
   * This method adds more attributes to model.
   * 
   * @param params The Parameter of getAddEditPageData
   * @return The return type is Map
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map params) {
    Map<String, List<BasicDynaBean>> referenceMap = new HashMap<>();

    List<BasicDynaBean> displayNames = lookup(false);
    referenceMap.put("displaynames", displayNames);
    return referenceMap;
  }

  /**
   * List all.
   *
   * @param columns the columns
   * @param filterBy the filter by
   * @param filterValue the filter value
   * @return the list
   */
  public List<BasicDynaBean> listAll(List<String> columns, String filterBy, Object filterValue) {
    return getRepository().listAll(columns, filterBy, filterValue);
  }

}
