package com.insta.hms.core.clinical.operationteams;

import com.insta.hms.common.GenericRepository;
import org.springframework.stereotype.Repository;

@Repository
public class OperationTeamRepository extends GenericRepository {

  public OperationTeamRepository() {
    super("operation_team");
  }

}
