package com.insta.hms.core.medicalrecords;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class MRDCaseFileIssueLogRepository.
 */
@Repository
public class MRDCaseFileIssueLogRepository extends GenericRepository {

  /**
   * Instantiates a new MRD case file issue log repository.
   */
  public MRDCaseFileIssueLogRepository() {
    super("mrd_casefile_issuelog");
  }

}
