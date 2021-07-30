package com.insta.hms.mdm.sectionrolerights;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * The Class SectionRoleRigthsService.
 */
@Service
public class SectionRoleRigthsService extends MasterService {

  /**
   * Instantiates a new section role rigths service.
   *
   * @param repository the r
   * @param validator the v
   */
  public SectionRoleRigthsService(SectionRoleRightsRepository repository, 
                                   SectionRoleRightsValidator validator) {
    super(repository, validator);
  }
}
