package com.insta.hms.mdm.itemforms;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class ItemFormService.
 *
 * @author irshadmohammed
 */
@Service
public class ItemFormService extends MasterService {

  /**
   * Instantiates a new item form service.
   *
   * @param repo the ItemFormRepository
   * @param validator the ItemFormValidator
   */
  public ItemFormService(ItemFormRepository repo, ItemFormValidator validator) {
    super(repo, validator);
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
