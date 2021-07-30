package com.insta.hms.core.clinical.eauthorization;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class EAuthActivitiesObservationsRepository.
 */
@Repository
public class EAuthActivitiesObservationsRepository extends GenericRepository {

  /**
   * Instantiates a new e auth activities observations repository.
   */
  public EAuthActivitiesObservationsRepository() {
    super("preauth_activities_observations");
  }

}
