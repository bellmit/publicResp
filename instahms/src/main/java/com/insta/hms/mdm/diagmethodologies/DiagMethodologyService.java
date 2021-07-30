/**
 * 
 */

package com.insta.hms.mdm.diagmethodologies;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DiagMethodologyService.
 *
 * @author anup.v
 */

@Service
public class DiagMethodologyService extends MasterService {

  /**
   * Instantiates a new diag methodology service.
   *
   * @param repository
   *          DiagMethodologyRepository
   * @param validator
   *          DiagMethodologyValidator
   */
  public DiagMethodologyService(DiagMethodologyRepository repository,
      DiagMethodologyValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> refData = new HashMap<>();
    refData.put("methodologyList", lookup(false));
    refData.put("methList", lookup(true));
    return refData;
  }

}
