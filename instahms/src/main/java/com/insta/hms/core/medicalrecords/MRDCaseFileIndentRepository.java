package com.insta.hms.core.medicalrecords;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class MRDCaseFileIndentRepository.
 */
@Repository
public class MRDCaseFileIndentRepository extends GenericRepository {

  /**
   * Instantiates a new MRD case file indent repository.
   */
  public MRDCaseFileIndentRepository() {
    super("mrd_casefile_attributes");
  }

}
