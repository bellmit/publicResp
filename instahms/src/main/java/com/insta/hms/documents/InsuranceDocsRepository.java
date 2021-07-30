package com.insta.hms.documents;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class InsuranceDocsRepository extends GenericRepository {

  public InsuranceDocsRepository() {
    super("insurance_docs");
  }
}
