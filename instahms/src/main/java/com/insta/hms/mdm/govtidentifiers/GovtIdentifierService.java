package com.insta.hms.mdm.govtidentifiers;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GovtIdentifierService extends MasterService {

  @LazyAutowired
  GovtIdentifierRepository govtIdentifierRepository;
  
  public GovtIdentifierService(GovtIdentifierRepository repository, 
      GovtIdentifierValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
    map.put("identifierTypeDetails", lookup(false));
    return map;
  }
  
  @Override
  public Integer update(BasicDynaBean bean) {
    Integer success = super.update(bean);
    if (success > 0 && bean.get("default_option") != null 
        && bean.get("default_option").equals("Y")) {
      govtIdentifierRepository.markOtherDefaultFalse((Integer) bean.get("identifier_id"));
    }
    return success;
  }

}
