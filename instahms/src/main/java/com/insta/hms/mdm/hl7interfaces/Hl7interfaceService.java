package com.insta.hms.mdm.hl7interfaces;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class Hl7interfaceService.
 */
@Service
public class Hl7interfaceService extends MasterService {

  /** The hl 7 interface repository. */
  @LazyAutowired private Hl7interfaceRepository hl7interfaceRepository;

  /**
   * Instantiates a new hl 7 interface service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public Hl7interfaceService(Hl7interfaceRepository repo, Hl7interfaceValidator validator) {
    super(repo, validator);
  }

  /**
   * Gets the hl 7 mapping details.
   *
   * @param testId the test id
   * @return the hl 7 mapping details
   */
  public List<BasicDynaBean> getHl7MappingDetails(String testId) {
    return hl7interfaceRepository.getHl7MappingDetails(testId);
  }

  /**
   * Gets the hl 7 interfaces.
   *
   * @return the hl 7 interfaces
   */
  public List<BasicDynaBean> getHl7Interfaces() {
    return hl7interfaceRepository.getHl7Interfaces();
  }
}
