package com.insta.hms.mdm.diagnosisstatus;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class DiagnosisStatusRepository.
 *
 * @author sonam
 */
@Repository
public class DiagnosisStatusRepository extends MasterRepository<Integer> {

  /**
  * Instantiates a new diagnosis status repository.
  */
  public DiagnosisStatusRepository() {
   super("diagnosis_statuses", "diagnosis_status_id", "diagnosis_status_name");
  }

}
