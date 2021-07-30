package com.insta.hms.common.minio;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class MinioPatientDocumentsRepository extends GenericRepository {

  public MinioPatientDocumentsRepository() {
    super("minio_patient_documents");
  }

}

