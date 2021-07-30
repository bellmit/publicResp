package com.insta.hms.mdm.strengthunits;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class StrengthUnitService.
 *
 * @author sainathbatthala The service for Strength Unit Master
 */
@Service
public class StrengthUnitService extends MasterService {
  
  /**
   * Instantiates a new strength unit service.
   *
   * @param repository the repository
   * @param validator the Validator
   */
  public StrengthUnitService(StrengthUnitRepository repository, StrengthUnitValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @param params the params
   * @return the adds the edit page data
   */
  
  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map params) {
    Map<String, List<BasicDynaBean>> referenceMap = new HashMap<String, List<BasicDynaBean>>();

    List<BasicDynaBean> strengthUnitsList = lookup(false);
    referenceMap.put("strengthUnitsList", strengthUnitsList);
    return referenceMap;
  }
}
