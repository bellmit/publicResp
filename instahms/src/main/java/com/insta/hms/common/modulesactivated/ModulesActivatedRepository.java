package com.insta.hms.common.modulesactivated;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class ModulesActivatedRepository.
 */
@Repository
public class ModulesActivatedRepository extends GenericRepository {
  
  /**
   * Instantiates a new modules activated repository.
   */
  public ModulesActivatedRepository() {
    super("modules_activated");
  }
}
