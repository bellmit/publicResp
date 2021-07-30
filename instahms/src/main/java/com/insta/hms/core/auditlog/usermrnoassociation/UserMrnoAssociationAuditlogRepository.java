package com.insta.hms.core.auditlog.usermrnoassociation;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class UserMrnoAssociationAuditlogRepository extends GenericRepository {

  public UserMrnoAssociationAuditlogRepository() {
    super("user_mrno_association_audit_log");
  }
}
