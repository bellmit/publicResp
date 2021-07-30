package com.insta.hms.mdm.sectionrolerights;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class SectionRoleRightsRepository.
 */
@Repository
public class SectionRoleRightsRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new section role rights repository.
   */
  public SectionRoleRightsRepository() {
    super("insta_section_rights", "section_role_id", null, new String[] {"role_id", "section_id"});
  }
}
