package com.insta.hms.core.clinical.operationprocedures;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class OperationProceduresRepository.
 */
@Repository
public class OperationProceduresRepository extends GenericRepository {

  /**
   * Instantiates a new operation procedures repository.
   */
  public OperationProceduresRepository() {
    super("operation_procedures");
  }

}
