package com.insta.hms.mdm.servicegroup;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class ServiceGroupService.
 */
@Service
public class ServiceGroupService extends MasterService {

  /** The service group repository. */
  @LazyAutowired private ServiceGroupRepository serviceGroupRepository;

  /**
   * Instantiates a new service group service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public ServiceGroupService(ServiceGroupRepository repo, ServiceGroupValidator validator) {
    super(repo, validator);
  }

  /**
   * Gets the all service groups.
   *
   * @return the all service groups
   */
  public List<BasicDynaBean> getAllServiceGroups() {
    return serviceGroupRepository.getAllServiceGroups();
  }
}
