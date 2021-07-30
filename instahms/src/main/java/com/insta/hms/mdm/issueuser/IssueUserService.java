package com.insta.hms.mdm.issueuser;

/*
 * Owner : Ashok Pal, 7th Aug 2017
 */
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class IssueUserService.
 */
@Service
public class IssueUserService extends MasterService {

  /**
   * Instantiates a new issue user service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public IssueUserService(IssueUserRepository repository, IssueUserValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> refData = new HashMap<String, List<BasicDynaBean>>();
    refData.put("lookupListMap", lookup(false));
    return refData;
  }

}
