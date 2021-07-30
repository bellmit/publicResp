package com.insta.hms.mdm.storescontractor;

/*
 * Owner : Ashok Pal, 5th April 2017
 */
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ContractorService.
 */
@Service
public class ContractorService extends MasterService {

  /**
   * Instantiates a new contractor service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public ContractorService(ContractorRepository repository, ContractorValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> refData = new HashMap<>();
    refData.put("contractorLists", lookup(false));
    return refData;
  }

}
