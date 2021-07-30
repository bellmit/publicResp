package com.insta.hms.core.clinical.operationbillableresources;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class OperationBillableResourcesRepository.
 */
@Repository
public class OperationBillableResourcesRepository extends GenericRepository {

  /**
   * Instantiates a new operation billable resources repository.
   */
  public OperationBillableResourcesRepository() {
    super("operation_billable_resources");
  }

}
